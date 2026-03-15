import { Processor, Process } from '@nestjs/bull';
import { Logger } from '@nestjs/common';
import { Job } from 'bull';
import * as nodemailer from 'nodemailer';

@Processor('email')
export class EmailProcessor {
  private readonly logger = new Logger(EmailProcessor.name);

  @Process()
  async handleSendEmail(job: Job<{
    to: string;
    email: string;
    code: string;
    linkToken: string;
    linkUrl: string;
    deliveryMode: 'email' | 'console' | 'disabled';
  }>) {
    const {
      to,
      code,
      linkToken,
      linkUrl,
      deliveryMode,
    } = job.data;

    if (deliveryMode === 'disabled') {
      this.logger.log(`[Email] Delivery disabled for ${to}; code=${code} link=${linkUrl}`);
      return;
    }

    if (deliveryMode === 'console' || !process.env.SMTP_HOST) {
      this.logger.log(
        `[Email] Review auth for ${to}; code=${code} token=${linkToken} link=${linkUrl}`,
      );
      return;
    }

    const transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST,
      port: Number(process.env.SMTP_PORT) || 587,
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS,
      },
    });

    await transporter.sendMail({
      from: process.env.SMTP_FROM || 'noreply@yourapp.com',
      to,
      subject: 'Your login code',
      text: [
        `Your login code is: ${code}`,
        '',
        `Open the app: ${linkUrl}`,
        '',
        'If the app does not open, enter the code manually.',
      ].join('\n'),
    });

    this.logger.log(`[Email] Code sent to ${to}`);
  }
}
