import { Injectable, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectQueue } from '@nestjs/bull';
import { Queue } from 'bull';
import * as crypto from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { SocialAuthDto } from './dtos/social-auth.dto';

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

  async logout(userId: string): Promise<void> {
    await this.prisma.refreshToken.deleteMany({ where: { userId } });
  }

  async authenticateWithSocial(dto: SocialAuthDto) {
    // Verify identity token with provider
    let email: string;
    let providerUserId: string;
    let displayName: string | undefined;

    if (dto.provider === 'apple') {
      // TODO: Verify Apple identity token
      // npm install apple-signin-auth
      // const appleUser = await appleSignin.verifyIdToken(dto.idToken, { audience: 'com.appstarterkit.app' });
      // email = appleUser.email; providerUserId = appleUser.sub;
      throw new BadRequestException('Apple Sign-In not yet configured. See auth.service.ts TODO.');
    } else {
      // TODO: Verify Google identity token
      // npm install google-auth-library
      // const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
      // const ticket = await client.verifyIdToken({ idToken: dto.idToken, audience: process.env.GOOGLE_CLIENT_ID });
      // const payload = ticket.getPayload();
      // email = payload.email; providerUserId = payload.sub; displayName = payload.name;
      throw new BadRequestException('Google Sign-In not yet configured. See auth.service.ts TODO.');
    }

    // Upsert user (would be used once TODOs are resolved)
    // const user = await this.prisma.user.upsert({
    //   where: { email },
    //   create: { email, displayName },
    //   update: {},
    // });
    // return this.generateTokens(user);
  }
}
