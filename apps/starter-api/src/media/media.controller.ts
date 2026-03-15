import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../user/decorators/current-user.decorator';
import { PrepareUploadDto } from './dtos/prepare-upload.dto';
import { CompleteUploadDto } from './dtos/complete-upload.dto';
import { MediaService } from './media.service';

@ApiTags('media')
@Controller('media')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class MediaController {
  constructor(private readonly mediaService: MediaService) {}

  @Post('uploads/prepare')
  @ApiOperation({ summary: 'Prepare a media upload' })
  @ApiResponse({ status: 201, description: 'Returns an upload URL and asset metadata' })
  prepareUpload(
    @CurrentUser() user: { userId: string },
    @Body() dto: PrepareUploadDto,
  ) {
    return this.mediaService.prepareUpload(user.userId, dto);
  }

  @Post('uploads/complete')
  @ApiOperation({ summary: 'Mark a media upload as complete' })
  @ApiResponse({ status: 201, description: 'Returns the completed media asset' })
  completeUpload(
    @CurrentUser() user: { userId: string },
    @Body() dto: CompleteUploadDto,
  ) {
    return this.mediaService.completeUpload(user.userId, dto);
  }

  @Get('assets')
  @ApiOperation({ summary: 'List current user media assets' })
  listAssets(@CurrentUser() user: { userId: string }) {
    return this.mediaService.listAssets(user.userId);
  }

  @Delete('assets/:assetId')
  @ApiOperation({ summary: 'Delete a media asset' })
  deleteAsset(
    @CurrentUser() user: { userId: string },
    @Param('assetId') assetId: string,
  ) {
    return this.mediaService.deleteAsset(user.userId, assetId);
  }
}
