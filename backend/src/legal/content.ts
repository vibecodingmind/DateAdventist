export const TERMS_TITLE = 'AdventHearts Terms of Service';
export const PRIVACY_TITLE = 'AdventHearts Privacy Policy';

export const TERMS_TEXT = `Last updated: 13 August 2026

AdventHearts is a faith-first dating service for adults who identify with the Seventh-day Adventist Christian community. By creating an account you agree to these terms.

1. Eligibility
You must be at least 18 years old. AdventHearts is for people seeking respectful, marriage-oriented relationships consistent with Adventist Christian values. You are responsible for the accuracy of your profile.

2. Your account
You must keep your password confidential. You may not impersonate another person, post another person's photos without permission, or operate more than one member account. We may suspend or delete accounts that violate these terms, including harassment, spam, fraud, or illegal activity.

3. Community standards
Treat other members with dignity. Do not share sexual content involving minors (which we will report to authorities), scams, hate, threats, or commercially spam other members. Keep conversations on AdventHearts until you trust the other person. Report and block tools are provided for your safety.

4. Content you post
You retain rights to your photos and text. You grant AdventHearts a limited license to host and display that content so the service can function (profiles, matches, and chat). Do not upload content you do not have the right to share.

5. Subscriptions
Paid plans are billed through Stripe (or another processor we name at checkout). Fees are described at purchase. Unless required by law, fees are non-refundable after the billing period starts. You can cancel to stop future renewals.

6. Safety
AdventHearts does not conduct criminal background checks on every member. Verification badges only mean a reviewer accepted a submitted selfie. Meet in public places, tell a friend, and never send money to someone you met on the app.

7. Disclaimers
The service is provided as-is. We do not guarantee matches, marriage, or uninterrupted availability. To the extent allowed by law, AdventHearts is not liable for disputes between members.

8. Termination
You may delete your account in Settings. We may delete accounts that violate these terms. Some records (for example safety reports) may be retained as required for security and law.

9. Contact
Questions: support@adventhearts.com`;

export const PRIVACY_TEXT = `Last updated: 13 August 2026

This policy explains what AdventHearts collects and how we use it.

1. Data we collect
- Account data: email, password hash (Argon2id; we do not store your raw password), name, date of birth / age, gender, city, country.
- Profile data: photos, bio, church and faith details, lifestyle answers, and preferences you enter.
- Activity: likes, passes, matches, messages, reports, blocks, subscription status, and approximate last-active time.
- Technical: IP address, device/app version, and logs needed to operate and secure the API.
- Payments: handled by Stripe. We store plan status and a processor reference, not your full card number.

2. How we use data
We use this data to run AdventHearts: authentication, discovery, matching, chat, safety/moderation, subscriptions, and customer support. We do not sell your personal information.

3. Who can see your profile
Other members may see the profile information you publish (photos, bio, faith details, city). Chat is visible to the two people in a match and to moderators if a report is filed.

4. Sharing
We share data with infrastructure providers (hosting, email delivery such as Resend, and Stripe for payments) only as needed to run the service, and with law enforcement when legally required.

5. Retention
We keep your account until you delete it or we close it. After deletion we anonymize login email and mark the account deleted. Backups may persist for a limited period. Safety reports may be kept longer.

6. Your choices
You can edit your profile, unmatch, block, report, and delete your account in the app. You can request a copy of your data by emailing support@adventhearts.com.

7. Children
AdventHearts is not for anyone under 18. We do not knowingly collect data from children.

8. Security
We use HTTPS in production, hashed passwords, and access-controlled admin tools. No method of transmission is perfectly secure.

9. Contact
Privacy questions: privacy@adventhearts.com`;

export function legalHtml(title: string, body: string) {
  const paragraphs = body
    .split('\n\n')
    .map((p) => `<p>${p.replace(/</g, '&lt;').replace(/\n/g, '<br/>')}</p>`)
    .join('\n');
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${title}</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 720px; margin: 40px auto; padding: 0 16px; line-height: 1.5; color: #111; }
    h1 { font-size: 1.6rem; }
  </style>
</head>
<body>
  <h1>${title}</h1>
  ${paragraphs}
</body>
</html>`;
}
