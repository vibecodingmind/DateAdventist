import { config } from '../config';
import { publicBaseUrl } from './storage.service';

export class EmailService {
  static async send(to: string, subject: string, text: string) {
    if (config.emailApiKey && config.emailApiKey !== 'replace_me') {
      try {
        await fetch('https://api.resend.com/emails', {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${config.emailApiKey}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            from: config.emailFrom,
            to,
            subject,
            text,
          }),
        });
        return;
      } catch (err) {
        console.error('Email send failed, falling back to log', err);
      }
    }
    console.log(`[email] to=${to} subject=${subject}\n${text}`);
  }

  static async sendVerification(to: string, token: string) {
    const link = `${publicBaseUrl()}/api/v1/auth/verify-email?token=${encodeURIComponent(token)}`;
    await EmailService.send(
      to,
      'Verify your AdventHearts email',
      `Welcome to AdventHearts. Verify your email by opening this link:\n${link}\n\nThis link expires in 24 hours.`
    );
    return link;
  }

  static async sendPasswordReset(to: string, token: string) {
    const link = `${publicBaseUrl()}/reset-password?token=${encodeURIComponent(token)}`;
    await EmailService.send(
      to,
      'Reset your AdventHearts password',
      `Use this token in the app to reset your password:\n${token}\n\nOr open:\n${link}\n\nThis token expires in 1 hour.`
    );
    return { token, link };
  }
}
