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

    const linkToken = this.signMagicLinkToken({
      email: user.email,
      code,
      expiresAt,
    });
    const linkUrl = this.buildMagicLinkUrl({
      email: user.email,
      code,
      linkToken,
    });
    const deliveryMode = this.getDeliveryMode();

    await this.emailQueue.add({
      to: email,
      email: user.email,
      code,
      linkToken,
      linkUrl,
      deliveryMode,
    });

    const response: Record<string, unknown> = {
      message: 'Code sent',
      deliveryMode,
    };

    if (deliveryMode !== 'email') {
      response.email = user.email;
      response.code = code;
      response.linkToken = linkToken;
      response.linkUrl = linkUrl;
    }

    return response;
  }

  async verifyMagicLink(input: { email?: string; code?: string; linkToken?: string }) {
    const bypassResult = await this.tryVerifyDevBypass(input);
    if (bypassResult) {
      return this.generateTokens(bypassResult);
    }

    const resolvedInput = input.linkToken
      ? this.verifyMagicLinkToken(input.linkToken)
      : { email: input.email, code: input.code };

    if (!resolvedInput.email || !resolvedInput.code) {
      throw new UnauthorizedException('Invalid email or code');
    }

    const user = await this.prisma.user.findUnique({ where: { email: resolvedInput.email } });
    if (!user) {
      throw new UnauthorizedException('Invalid email or code');
    }

    const magicLink = await this.prisma.magicLink.findFirst({
      where: {
        userId: user.id,
        code: resolvedInput.code,
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

    return this.generateTokens(user);
  }

  private getDeliveryMode(): 'email' | 'console' | 'disabled' {
    const raw = process.env.AUTH_DELIVERY_MODE;
    if (raw === 'email' || raw === 'disabled') {
      return raw;
    }
    return 'console';
  }

  private buildMagicLinkUrl(input: {
    email: string;
    code: string;
    linkToken: string;
  }): string {
    const baseUrl = process.env.AUTH_LINK_BASE_URL || 'appstarterkit://auth/verify';
    const url = new URL(baseUrl);
    url.searchParams.set('email', input.email);
    url.searchParams.set('code', input.code);
    url.searchParams.set('linkToken', input.linkToken);
    return url.toString();
  }

  private signMagicLinkToken(input: {
    email: string;
    code: string;
    expiresAt: Date;
  }): string {
    const expiresInSeconds = Math.max(
      60,
      Math.floor((input.expiresAt.getTime() - Date.now()) / 1000),
    );
    return this.jwtService.sign(
      {
        type: 'magic_link',
        email: input.email,
        code: input.code,
      },
      { expiresIn: expiresInSeconds },
    );
  }

  private verifyMagicLinkToken(linkToken: string): { email: string; code: string } {
    try {
      const payload = this.jwtService.verify<{
        type?: string;
        email?: string;
        code?: string;
      }>(linkToken);

      if (payload.type !== 'magic_link' || !payload.email || !payload.code) {
        throw new UnauthorizedException('Invalid magic link token');
      }

      return {
        email: payload.email,
        code: payload.code,
      };
    } catch {
      throw new UnauthorizedException('Invalid magic link token');
    }
  }

  private async tryVerifyDevBypass(input: {
    email?: string;
    code?: string;
    linkToken?: string;
  }) {
    if (process.env.AUTH_DEV_BYPASS_ENABLED !== 'true') {
      return null;
    }

    const bypassEmail = process.env.AUTH_DEV_BYPASS_EMAIL;
    const bypassCode = process.env.AUTH_DEV_BYPASS_CODE;
    const bypassLinkToken = process.env.AUTH_DEV_BYPASS_LINK_TOKEN;

    const emailMatches = Boolean(
      bypassEmail &&
      ((input.email && input.email === bypassEmail) ||
        (input.linkToken &&
          bypassLinkToken &&
          input.linkToken === bypassLinkToken)),
    );
    const codeMatches = Boolean(bypassCode && input.code === bypassCode);
    const linkMatches = Boolean(
      bypassLinkToken &&
      input.linkToken &&
      input.linkToken === bypassLinkToken,
    );

    if (!emailMatches || (!codeMatches && !linkMatches)) {
      return null;
    }

    return this.prisma.user.upsert({
      where: { email: bypassEmail! },
      update: {},
      create: { email: bypassEmail! },
    });
  }

  private async generateTokens(user: {
    id: string;
    email: string;
    displayName: string | null;
  }) {
    const accessToken = this.jwtService.sign({ sub: user.id });
    const refreshTokenValue = crypto.randomUUID();
    const refreshExpiryDays = Number(process.env.REFRESH_TOKEN_EXPIRY_DAYS) || 30;
    const refreshExpiresAt = new Date(
      Date.now() + refreshExpiryDays * 24 * 60 * 60 * 1000,
    );

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
      // const appleUser = await appleSignin.verifyIdToken(dto.idToken, { audience: 'com.onlystack.starterapp' });
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
