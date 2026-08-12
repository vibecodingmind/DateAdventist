import { Router, Request, Response } from 'express';
import { requireAuth, AuthenticatedRequest } from '../middleware/auth.middleware';

const router = Router();

// Get subscription plans and comparison matrix
router.get('/plans', (req: Request, res: Response) => {
  res.json({
    success: true,
    data: {
      plans: [
        {
          id: 'plan_gold_monthly',
          name: 'AdventHearts Gold',
          tier: 'GOLD',
          priceMonthly: 14.99,
          priceAnnually: 119.88,
          currency: 'USD',
          badgeText: 'MOST POPULAR',
          features: [
            'Unlimited Likes & Swipes',
            'See Who Likes You',
            '5 Free Super Likes per week',
            'Advanced Faith & Lifestyle Search Filters',
            '1 Free Profile Boost per month',
            'Gold Verification Badge'
          ]
        },
        {
          id: 'plan_platinum_monthly',
          name: 'AdventHearts Platinum',
          tier: 'PLATINUM',
          priceMonthly: 24.99,
          priceAnnually: 199.88,
          currency: 'USD',
          badgeText: 'BEST VALUE',
          features: [
            'All Gold Plan Features Included',
            'Priority Messaging in Inbox',
            'Message Before Matching',
            'See Read Receipts',
            'Unlimited Rewinds',
            'Platinum Crown Badge & VIP Support'
          ]
        }
      ]
    }
  });
});

// Trigger Stripe / PayPal Checkout flow from backend
router.post('/checkout', requireAuth, (req: AuthenticatedRequest, res: Response) => {
  const { planId, paymentProvider, billingCycle } = req.body;

  if (!planId || !paymentProvider) {
    return res.status(400).json({
      success: false,
      error: { code: 'INVALID_CHECKOUT_PARAMS', message: 'planId and paymentProvider (stripe | paypal) are required.' }
    });
  }

  const userId = req.user?.userId;
  const transactionId = `tx_${paymentProvider}_${Date.now()}`;
  const checkoutUrl = paymentProvider === 'stripe'
    ? `https://checkout.stripe.com/pay/cs_test_adventhearts_${transactionId}`
    : `https://www.paypal.com/checkoutnow?token=EC-AH_${transactionId}`;

  res.json({
    success: true,
    data: {
      transactionId,
      provider: paymentProvider,
      status: 'INITIATED',
      planId,
      billingCycle: billingCycle || 'MONTHLY',
      checkoutUrl,
      clientSecret: `pi_secret_ah_${Date.now()}`,
      userId
    }
  });
});

// Confirm payment / webhook trigger to activate subscription
router.post('/confirm-payment', requireAuth, (req: AuthenticatedRequest, res: Response) => {
  const { transactionId } = req.body;

  res.json({
    success: true,
    data: {
      active: true,
      tier: 'GOLD',
      expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
      transactionId: transactionId || `tx_confirmed_${Date.now()}`
    }
  });
});

export default router;
