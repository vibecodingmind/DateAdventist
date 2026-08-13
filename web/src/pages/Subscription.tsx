import { useEffect, useState } from 'react';
import { api } from '../api';
import type { Subscription } from '../types';

const PLANS = [
  { id: 'plan_plus_monthly', name: 'Plus', price: '$9.99' },
  { id: 'plan_gold_monthly', name: 'Gold', price: '$14.99' },
  { id: 'plan_platinum_monthly', name: 'Platinum', price: '$24.99' },
];

export default function SubscriptionPage() {
  const [sub, setSub] = useState<Subscription | null>(null);
  const [planId, setPlanId] = useState('plan_gold_monthly');
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  function refresh() {
    api
      .subscription()
      .then(setSub)
      .catch((err: Error) => setError(err.message));
  }

  useEffect(() => {
    refresh();
  }, []);

  return (
    <div className="stack">
      <h2 style={{ margin: 0 }}>Membership</h2>
      {error && <div className="error">{error}</div>}
      {info && <div className="success">{info}</div>}
      <div className="card">
        Current: <strong>{sub?.tier || 'FREE'}</strong> {sub?.active ? <span className="badge gold">ACTIVE</span> : null}
      </div>
      {PLANS.map((plan) => (
        <button
          key={plan.id}
          className="card"
          style={{ borderColor: planId === plan.id ? 'var(--gold)' : undefined, textAlign: 'left', cursor: 'pointer' }}
          onClick={() => setPlanId(plan.id)}
        >
          <strong>{plan.name}</strong> · {plan.price}/mo
        </button>
      ))}
      <button
        className="btn btn-primary"
        onClick={async () => {
          setError(null);
          try {
            const checkout = await api.checkout(planId);
            setInfo('Opening checkout…');
            if (checkout.checkoutUrl?.startsWith('http')) {
              window.open(checkout.checkoutUrl, '_blank');
            }
          } catch (err) {
            setError(err instanceof Error ? err.message : 'Checkout failed');
          }
        }}
      >
        Checkout with Stripe
      </button>
      <button className="btn btn-ghost" onClick={refresh}>
        I have finished paying — refresh
      </button>
    </div>
  );
}
