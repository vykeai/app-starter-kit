import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { UpdateUserDto } from './dtos/update-user.dto';

@Injectable()
export class UserService {
  constructor(private readonly prisma: PrismaService) {}

  async getMe(userId: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      include: { preferences: true },
    });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return {
      id: user.id,
      email: user.email,
      displayName: user.displayName,
      preferences: user.preferences
        ? {
            theme: user.preferences.theme,
            pushMarketingEnabled: user.preferences.pushMarketingEnabled,
            pushActivityEnabled: user.preferences.pushActivityEnabled,
            pushTransactionalEnabled: user.preferences.pushTransactionalEnabled,
            pushSystemEnabled: user.preferences.pushSystemEnabled,
            emailNotificationsEnabled: user.preferences.emailNotificationsEnabled,
          }
        : null,
    };
  }

  async updateMe(userId: string, dto: UpdateUserDto) {
    if (dto.displayName !== undefined) {
      await this.prisma.user.update({
        where: { id: userId },
        data: { displayName: dto.displayName },
      });
    }

    if (dto.preferences) {
      await this.prisma.userPreferences.upsert({
        where: { userId },
        update: {
          theme: dto.preferences.theme ?? undefined,
          pushMarketingEnabled: dto.preferences.pushMarketingEnabled ?? undefined,
          pushActivityEnabled: dto.preferences.pushActivityEnabled ?? undefined,
          pushTransactionalEnabled: dto.preferences.pushTransactionalEnabled ?? undefined,
          pushSystemEnabled: dto.preferences.pushSystemEnabled ?? undefined,
          emailNotificationsEnabled:
            dto.preferences.emailNotificationsEnabled ?? undefined,
        },
        create: {
          userId,
          theme: dto.preferences.theme ?? 'system',
          pushMarketingEnabled: dto.preferences.pushMarketingEnabled ?? false,
          pushActivityEnabled: dto.preferences.pushActivityEnabled ?? true,
          pushTransactionalEnabled:
            dto.preferences.pushTransactionalEnabled ?? true,
          pushSystemEnabled: dto.preferences.pushSystemEnabled ?? true,
          emailNotificationsEnabled:
            dto.preferences.emailNotificationsEnabled ?? true,
        },
      });
    }

    return this.getMe(userId);
  }
}
