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
        ? { theme: user.preferences.theme }
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
        update: { theme: dto.preferences.theme ?? undefined },
        create: {
          userId,
          theme: dto.preferences.theme ?? 'system',
        },
      });
    }

    return this.getMe(userId);
  }
}
