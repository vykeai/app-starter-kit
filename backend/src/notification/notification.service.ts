import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { RegisterDeviceDto } from './dtos/register-device.dto';
import { UpdateNotificationPreferencesDto } from './dtos/update-notification-preferences.dto';

// TODO: Install firebase-admin and configure with service account key
// import * as admin from 'firebase-admin';

@Injectable()
export class NotificationService {
  private readonly logger = new Logger(NotificationService.name);

  constructor(private readonly prisma: PrismaService) {}

  async getPreferences(userId: string) {
    const preferences = await this.prisma.userPreferences.upsert({
      where: { userId },
      update: {},
      create: {
        userId,
      },
    });

    return {
      theme: preferences.theme,
      pushMarketingEnabled: preferences.pushMarketingEnabled,
      pushActivityEnabled: preferences.pushActivityEnabled,
      pushTransactionalEnabled: preferences.pushTransactionalEnabled,
      pushSystemEnabled: preferences.pushSystemEnabled,
      emailNotificationsEnabled: preferences.emailNotificationsEnabled,
      updatedAt: preferences.updatedAt,
    };
  }

  async updatePreferences(
    userId: string,
    dto: UpdateNotificationPreferencesDto,
  ) {
    const preferences = await this.prisma.userPreferences.upsert({
      where: { userId },
      update: {
        theme: dto.theme ?? undefined,
        pushMarketingEnabled: dto.pushMarketingEnabled ?? undefined,
        pushActivityEnabled: dto.pushActivityEnabled ?? undefined,
        pushTransactionalEnabled: dto.pushTransactionalEnabled ?? undefined,
        pushSystemEnabled: dto.pushSystemEnabled ?? undefined,
        emailNotificationsEnabled: dto.emailNotificationsEnabled ?? undefined,
      },
      create: {
        userId,
        theme: dto.theme ?? 'system',
        pushMarketingEnabled: dto.pushMarketingEnabled ?? false,
        pushActivityEnabled: dto.pushActivityEnabled ?? true,
        pushTransactionalEnabled: dto.pushTransactionalEnabled ?? true,
        pushSystemEnabled: dto.pushSystemEnabled ?? true,
        emailNotificationsEnabled: dto.emailNotificationsEnabled ?? true,
      },
    });

    return {
      theme: preferences.theme,
      pushMarketingEnabled: preferences.pushMarketingEnabled,
      pushActivityEnabled: preferences.pushActivityEnabled,
      pushTransactionalEnabled: preferences.pushTransactionalEnabled,
      pushSystemEnabled: preferences.pushSystemEnabled,
      emailNotificationsEnabled: preferences.emailNotificationsEnabled,
      updatedAt: preferences.updatedAt,
    };
  }

  async registerDevice(userId: string, dto: RegisterDeviceDto) {
    const device = await this.prisma.pushDevice.upsert({
      where: { token: dto.token },
      update: {
        userId,
        platform: dto.platform,
        locale: dto.locale ?? undefined,
        appVersion: dto.appVersion ?? undefined,
        lastSeenAt: new Date(),
        revokedAt: null,
      },
      create: {
        userId,
        platform: dto.platform,
        token: dto.token,
        locale: dto.locale,
        appVersion: dto.appVersion,
      },
    });

    return {
      id: device.id,
      platform: device.platform,
      token: device.token,
      locale: device.locale,
      appVersion: device.appVersion,
      lastSeenAt: device.lastSeenAt,
      revokedAt: device.revokedAt,
    };
  }

  async revokeDevice(userId: string, token: string) {
    await this.prisma.pushDevice.updateMany({
      where: {
        userId,
        token,
        revokedAt: null,
      },
      data: {
        revokedAt: new Date(),
      },
    });

    return { message: 'Device revoked' };
  }

  async sendToDevice(
    fcmToken: string,
    title: string,
    body: string,
    data?: Record<string, string>,
  ): Promise<void> {
    this.logger.log(
      `[FCM] TODO: send "${title}" to token ${fcmToken.slice(0, 8)}...`,
    );
    // TODO: admin.messaging().send({ token: fcmToken, notification: { title, body }, data })
  }

  async sendToTopic(topic: string, title: string, body: string): Promise<void> {
    this.logger.log(`[FCM] TODO: send "${title}" to topic ${topic}`);
  }
}
