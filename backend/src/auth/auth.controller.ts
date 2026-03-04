import { Controller, Post, Body, UseGuards, Req } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { RequestMagicLinkDto } from './dtos/request-magic-link.dto';
import { VerifyMagicLinkDto } from './dtos/verify-magic-link.dto';
import { RefreshTokenDto } from './dtos/refresh-token.dto';
import { JwtAuthGuard } from './jwt-auth.guard';
import { Request } from 'express';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('magic-link/request')
  requestMagicLink(@Body() dto: RequestMagicLinkDto) {
    return this.authService.requestMagicLink(dto.email);
  }

  @Post('magic-link/verify')
  verifyMagicLink(@Body() dto: VerifyMagicLinkDto) {
    return this.authService.verifyMagicLink(dto.email, dto.code);
  }

  @Post('refresh')
  refresh(@Body() dto: RefreshTokenDto) {
    return this.authService.refreshAccessToken(dto.refreshToken);
  }

  @Post('logout')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  logout(@Body() dto: RefreshTokenDto, @Req() _req: Request) {
    return this.authService.revokeRefreshToken(dto.refreshToken);
  }
}
