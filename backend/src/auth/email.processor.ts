import { Processor, Process } from '@nestjs/bull';
import { Logger } from '@nestjs/common';
import { Job } from 'bull';
import * as nodemailer from 'nodemailer';

@Processor('email')
export class EmailProcessor {
  private readonly logger = new Logger(EmailProcessor.name);

  @Process()
  async handleSendEmail(job: Job<{ to: string; code: string }>) {
    const { to, code } = job.data;

    if (!process.env.SMTP_HOST) {
      this.logger.log(`[Email] Sending code ${code} to ${to}`);
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
      text: `Your login code is: ${code}`,
    });

    this.logger.log(`[Email] Code sent to ${to}`);
  }
}
