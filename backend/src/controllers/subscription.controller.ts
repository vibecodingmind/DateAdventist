import { Request, Response } from 'express';
import Stripe from 'stripe';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { SUBSCRIPTION_PLANS, config } from '../config';
import { getUserTier } from '../middleware/subscription.middleware';
import { publicBaseUrl } from '../services/storage.service';

function stripeClient(): Stripe | null {
  if (!config.stripeSecretKey || config.stripeSecretKey.includes('replace_me') || config.stripeSecretKey.startsWith('sk_test_replace')) {
    return null;
  }
  return new Stripe(config.stripeSecretKey);
}

async function activateSubscription(userId: string, planId: string, providerRef?: string) {
  const plan = SUBSCRIPTION_PLANS.find((p) => p.id === planId || p.tier === String(planId).toUpperCase()) ?? SUBSCRIPTION_PLANS[1];
  const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);
  await prisma.subscription.upsert({
    where: { userId },
    update: {
      plan: plan.tier,
      status: 'ACTIVE',
      currentPeriodEnd: expiresAt,
      stripeSubscriptionId: providerRef || undefined,
    },
    create: {
      userId,
      plan: plan.tier,
      status: 'ACTIVE',
      currentPeriodEnd: expiresAt,
      stripeSubscriptionId: providerRef,
    },
  });
  await prisma.profile.updateMany({ where: { userId }, data: { isPremium: true } });
  return { active: true, tier: plan.tier, expiresAt: expiresAt.toISOString(), planId: plan.id };
}

export class SubscriptionController {
  static async plans(_req: Request, res: Response) {
    return ok(res, { plans: SUBSCRIPTION_PLANS });
  }

  static async current(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);
    const subscription = await prisma.subscription.findUnique({ where: { userId } });
    const tier = await getUserTier(userId);
    return ok(res, {
      active: tier !== 'FREE',
      tier,
      plan: subscription?.plan ?? 'FREE',
      status: subscription?.status ?? 'INACTIVE',
      expiresAt: subscription?.currentPeriodEnd?.toISOString() ?? null,
    });
  }

  static async checkout(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const { planId, paymentProvider, billingCycle } = req.body;
    if (!planId || !paymentProvider) {
      return fail(res, 'INVALID_CHECKOUT_PARAMS', 'planId and paymentProvider (stripe | paypal) are required.');
    }

    const plan = SUBSCRIPTION_PLANS.find((p) => p.id === planId || p.tier === String(planId).toUpperCase());
    if (!plan) return fail(res, 'UNKNOWN_PLAN', 'Unknown subscription plan.');

    const stripe = paymentProvider === 'stripe' ? stripeClient() : null;
    if (stripe) {
      const session = await stripe.checkout.sessions.create({
        mode: 'payment',
        client_reference_id: userId,
        metadata: { userId, planId: plan.id, tier: plan.tier },
        line_items: [
          {
            quantity: 1,
            price_data: {
              currency: 'usd',
              unit_amount: Math.round(plan.priceMonthly * 100),
              product_data: { name: plan.name },
            },
          },
        ],
        success_url: `${publicBaseUrl()}/subscription/success?session_id={CHECKOUT_SESSION_ID}`,
        cancel_url: `${publicBaseUrl()}/subscription/cancel`,
      });
      return ok(res, {
        transactionId: session.id,
        provider: 'stripe',
        status: 'INITIATED',
        planId: plan.id,
        billingCycle: billingCycle || 'MONTHLY',
        checkoutUrl: session.url,
        userId,
      });
    }

    if (config.env === 'production') {
      return fail(res, 'PAYMENTS_NOT_CONFIGURED', 'Stripe is not configured on this server yet.', 503);
    }

    const transactionId = `tx_${paymentProvider}_${Date.now()}`;
    return ok(res, {
      transactionId,
      provider: paymentProvider,
      status: 'INITIATED',
      planId: plan.id,
      billingCycle: billingCycle || 'MONTHLY',
      checkoutUrl: `${publicBaseUrl()}/api/v1/subscriptions/dev-complete?planId=${plan.id}&userId=${userId}`,
      userId,
    });
  }

  static async confirmPayment(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    if (config.env === 'production' && stripeClient()) {
      return fail(res, 'USE_WEBHOOK', 'Live payments are confirmed by the Stripe webhook, not this endpoint.', 400);
    }

    const activated = await activateSubscription(
      userId,
      String(req.body.planId || 'plan_gold_monthly'),
      String(req.body.transactionId || `tx_confirmed_${Date.now()}`)
    );
    return ok(res, { ...activated, transactionId: req.body.transactionId });
  }

  static async webhook(req: Request, res: Response) {
    const stripe = stripeClient();
    if (!stripe || !config.stripeWebhookSecret) {
      return fail(res, 'PAYMENTS_NOT_CONFIGURED', 'Stripe webhook is not configured.', 503);
    }

    const signature = req.headers['stripe-signature'];
    if (!signature || typeof signature !== 'string') {
      return fail(res, 'MISSING_SIGNATURE', 'Stripe signature required.', 400);
    }

    let event: Stripe.Event;
    try {
      event = stripe.webhooks.constructEvent(req.body, signature, config.stripeWebhookSecret);
    } catch {
      return fail(res, 'INVALID_SIGNATURE', 'Webhook signature verification failed.', 400);
    }

    if (event.type === 'checkout.session.completed') {
      const session = event.data.object as Stripe.Checkout.Session;
      const userId = session.metadata?.userId || session.client_reference_id;
      const planId = session.metadata?.planId || 'plan_gold_monthly';
      if (userId) {
        await activateSubscription(userId, planId, session.id);
      }
    }

    return ok(res, { received: true });
  }
}
