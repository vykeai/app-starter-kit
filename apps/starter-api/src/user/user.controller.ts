import { Controller, Get, Patch, Body, UseGuards } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { SkipThrottle } from '@nestjs/throttler';
import { UserService } from './user.service';
import { UpdateUserDto } from './dtos/update-user.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from './decorators/current-user.decorator';

@ApiTags('user')
@Controller('user')
export class UserController {
  constructor(private readonly userService: UserService) {}

  @Get('me')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @SkipThrottle()
  @ApiOperation({ summary: 'Get the authenticated user profile' })
  @ApiResponse({ status: 200, description: 'Returns the current user and preferences' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  getMe(@CurrentUser() user: { userId: string }) {
    return this.userService.getMe(user.userId);
  }

  @Patch('me')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Update the authenticated user profile and preferences' })
  @ApiResponse({ status: 200, description: 'Returns the updated user' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  updateMe(
    @CurrentUser() user: { userId: string },
    @Body() dto: UpdateUserDto,
  ) {
    return this.userService.updateMe(user.userId, dto);
  }
}
