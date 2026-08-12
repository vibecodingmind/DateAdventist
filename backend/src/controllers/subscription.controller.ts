import { Request, Response } from 'express';
import { AuthenticatedRequest } from '../middleware/auth.middleware';
import { prisma } from '../db/prisma';
import { fail, ok } from '../utils/http';
import { SUBSCRIPTION_PLANS } from '../config';
import { getUserTier } from '../middleware/subscription.middleware';

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
    const transactionId = `tx_${paymentProvider}_${Date.now()}`;
    const checkoutUrl =
      paymentProvider === 'stripe'
        ? `https://checkout.stripe.com/pay/cs_test_adventhearts_${transactionId}`
        : `https://www.paypal.com/checkoutnow?token=EC-AH_${transactionId}`;

    return ok(res, {
      transactionId,
      provider: paymentProvider,
      status: 'INITIATED',
      planId: plan?.id ?? planId,
      billingCycle: billingCycle || 'MONTHLY',
      checkoutUrl,
      clientSecret: `pi_secret_ah_${Date.now()}`,
      userId,
    });
  }

  static async confirmPayment(req: AuthenticatedRequest, res: Response) {
    const userId = req.user?.userId;
    if (!userId) return fail(res, 'UNAUTHORIZED', 'Authentication required', 401);

    const planId = String(req.body.planId || 'plan_gold_monthly');
    const plan = SUBSCRIPTION_PLANS.find((p) => p.id === planId) ?? SUBSCRIPTION_PLANS[1];
    const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);

    await prisma.subscription.upsert({
      where: { userId },
      update: {
        plan: plan.tier,
        status: 'ACTIVE',
        currentPeriodEnd: expiresAt,
        stripeSubscriptionId: String(req.body.transactionId || `tx_confirmed_${Date.now()}`),
      },
      create: {
        userId,
        plan: plan.tier,
        status: 'ACTIVE',
        currentPeriodEnd: expiresAt,
        stripeSubscriptionId: String(req.body.transactionId || `tx_confirmed_${Date.now()}`),
      },
    });

    await prisma.profile.updateMany({ where: { userId }, data: { isPremium: true } });

    return ok(res, {
      active: true,
      tier: plan.tier,
      expiresAt: expiresAt.toISOString(),
      transactionId: req.body.transactionId || `tx_confirmed_${Date.now()}`,
    });
  }
}
