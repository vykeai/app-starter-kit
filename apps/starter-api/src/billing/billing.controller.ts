import { Body, Controller, Get, Post, UseGuards } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../user/decorators/current-user.decorator';
import { BillingService } from './billing.service';
import { VerifyNativePurchaseDto } from './dtos/verify-native-purchase.dto';

@ApiTags('billing')
@Controller('billing')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class BillingController {
  constructor(private readonly billingService: BillingService) {}

  @Get('entitlements')
  @ApiOperation({ summary: 'Return current user entitlements' })
  getEntitlements(@CurrentUser() user: { userId: string }) {
    return this.billingService.getEntitlements(user.userId);
  }

  @Post('verify/native')
  @ApiOperation({ summary: 'Verify a native store purchase token' })
  @ApiResponse({ status: 201, description: 'Purchase verified and entitlement updated' })
  verifyNativePurchase(
    @CurrentUser() user: { userId: string },
    @Body() dto: VerifyNativePurchaseDto,
  ) {
    return this.billingService.verifyNativePurchase(user.userId, dto);
  }
}
