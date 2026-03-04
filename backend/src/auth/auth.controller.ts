import {
  Controller,
  Post,
  Delete,
  Body,
  UseGuards,
  HttpCode,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { Throttle, SkipThrottle } from '@nestjs/throttler';
import { AuthService } from './auth.service';
import { RequestMagicLinkDto } from './dtos/request-magic-link.dto';
import { VerifyMagicLinkDto } from './dtos/verify-magic-link.dto';
import { RefreshTokenDto } from './dtos/refresh-token.dto';
import { SocialAuthDto } from './dtos/social-auth.dto';
import { JwtAuthGuard } from './jwt-auth.guard';
import { CurrentUser } from '../user/decorators/current-user.decorator';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('magic-link/request')
  @Throttle({ default: { ttl: 60000, limit: 5 } })
  @ApiOperation({ summary: 'Request a magic link OTP code via email' })
  @ApiResponse({ status: 200, description: 'Code sent successfully' })
  @ApiResponse({ status: 429, description: 'Too many requests' })
  requestMagicLink(@Body() dto: RequestMagicLinkDto) {
    return this.authService.requestMagicLink(dto.email);
  }

  @Post('magic-link/verify')
  @ApiOperation({ summary: 'Verify magic link OTP code and obtain tokens' })
  @ApiResponse({ status: 200, description: 'Returns accessToken, refreshToken, and user' })
  @ApiResponse({ status: 401, description: 'Invalid email or code' })
  verifyMagicLink(@Body() dto: VerifyMagicLinkDto) {
    return this.authService.verifyMagicLink(dto.email, dto.code);
  }

  @Post('refresh')
  @SkipThrottle()
  @ApiOperation({ summary: 'Exchange a refresh token for a new access token' })
  @ApiResponse({ status: 200, description: 'Returns a new accessToken' })
  @ApiResponse({ status: 401, description: 'Invalid or expired refresh token' })
  refresh(@Body() dto: RefreshTokenDto) {
    return this.authService.refreshAccessToken(dto.refreshToken);
  }

  @Post('logout')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Revoke a single refresh token' })
  @ApiResponse({ status: 200, description: 'Refresh token revoked' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  logout(@Body() dto: RefreshTokenDto) {
    return this.authService.revokeRefreshToken(dto.refreshToken);
  }

  @Delete('session')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @HttpCode(204)
  @ApiOperation({ summary: 'Sign out everywhere — invalidates all refresh tokens for the user' })
  @ApiResponse({ status: 204, description: 'All sessions invalidated' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async logoutAll(@CurrentUser() user: { userId: string }): Promise<void> {
    await this.authService.logout(user.userId);
  }

  @Post('social')
  @HttpCode(200)
  @ApiOperation({ summary: 'Authenticate with Apple or Google identity token' })
  @ApiResponse({ status: 200, description: 'Authentication successful' })
  async socialAuth(@Body() dto: SocialAuthDto) {
    return this.authService.authenticateWithSocial(dto);
  }
}
