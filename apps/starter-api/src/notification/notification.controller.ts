import { Body, Controller, Delete, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../user/decorators/current-user.decorator';
import { NotificationService } from './notification.service';
import { UpdateNotificationPreferencesDto } from './dtos/update-notification-preferences.dto';
import { RegisterDeviceDto } from './dtos/register-device.dto';

@ApiTags('notifications')
@Controller('notifications')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class NotificationController {
  constructor(private readonly notificationService: NotificationService) {}

  @Get('preferences')
  @ApiOperation({ summary: 'Get notification preferences for the authenticated user' })
  @ApiResponse({ status: 200, description: 'Notification preferences returned' })
  getPreferences(@CurrentUser() user: { userId: string }) {
    return this.notificationService.getPreferences(user.userId);
  }

  @Patch('preferences')
  @ApiOperation({ summary: 'Update notification preferences for the authenticated user' })
  @ApiResponse({ status: 200, description: 'Updated notification preferences returned' })
  updatePreferences(
    @CurrentUser() user: { userId: string },
    @Body() dto: UpdateNotificationPreferencesDto,
  ) {
    return this.notificationService.updatePreferences(user.userId, dto);
  }

  @Post('devices')
  @ApiOperation({ summary: 'Register or refresh a push-capable device token' })
  @ApiResponse({ status: 201, description: 'Push device registered' })
  registerDevice(
    @CurrentUser() user: { userId: string },
    @Body() dto: RegisterDeviceDto,
  ) {
    return this.notificationService.registerDevice(user.userId, dto);
  }

  @Delete('devices/:token')
  @ApiOperation({ summary: 'Revoke a push device token' })
  @ApiResponse({ status: 200, description: 'Push device revoked' })
  revokeDevice(
    @CurrentUser() user: { userId: string },
    @Param('token') token: string,
  ) {
    return this.notificationService.revokeDevice(user.userId, token);
  }
}
