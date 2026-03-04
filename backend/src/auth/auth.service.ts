import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectQueue } from '@nestjs/bull';
import { Queue } from 'bull';
import * as crypto from 'crypto';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly jwtService: JwtService,
    @InjectQueue('email') private readonly emailQueue: Queue,
  ) {}

  async requestMagicLink(email: string) {
    const user = await this.prisma.user.upsert({
      where: { email },
      update: {},
      create: { email },
    });

    const code = crypto.randomInt(10000000, 99999999).toString();
    const expiresAt = new Date(Date.now() + 15 * 60 * 1000);

    await this.prisma.magicLink.create({
      data: {
        userId: user.id,
        code,
        expiresAt,
      },
    });

    await this.emailQueue.add({
      to: email,
      code,
    });

    return { message: 'Code sent' };
  }

  async verifyMagicLink(email: string, code: string) {
    const user = await this.prisma.user.findUnique({ where: { email } });
    if (!user) {
      throw new UnauthorizedException('Invalid email or code');
    }

    const magicLink = await this.prisma.magicLink.findFirst({
      where: {
        userId: user.id,
        code,
        usedAt: null,
        expiresAt: { gt: new Date() },
      },
    });

    if (!magicLink) {
      throw new UnauthorizedException('Invalid email or code');
    }

    await this.prisma.magicLink.update({
      where: { id: magicLink.id },
      data: { usedAt: new Date() },
    });

    const accessToken = this.jwtService.sign({ sub: user.id });

    const refreshTokenValue = crypto.randomUUID();
    const refreshExpiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);

    await this.prisma.refreshToken.create({
      data: {
        userId: user.id,
        token: refreshTokenValue,
        expiresAt: refreshExpiresAt,
      },
    });

    return {
      accessToken,
      refreshToken: refreshTokenValue,
      user: {
        id: user.id,
        email: user.email,
        displayName: user.displayName,
      },
    };
  }

  async refreshAccessToken(refreshToken: string) {
    const stored = await this.prisma.refreshToken.findUnique({
      where: { token: refreshToken },
    });

    if (!stored || stored.revokedAt || stored.expiresAt < new Date()) {
      throw new UnauthorizedException('Invalid refresh token');
    }

    const accessToken = this.jwtService.sign({ sub: stored.userId });
    return { accessToken };
  }

  async revokeRefreshToken(refreshToken: string) {
    await this.prisma.refreshToken.updateMany({
      where: { token: refreshToken, revokedAt: null },
      data: { revokedAt: new Date() },
    });
  }
}
