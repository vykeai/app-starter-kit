import { ApiProperty } from '@nestjs/swagger';
import { IsIn, IsOptional, IsString, MaxLength } from 'class-validator';

export class RegisterDeviceDto {
  @ApiProperty({ example: 'ios' })
  @IsString()
  @IsIn(['ios', 'android', 'web'])
  platform!: string;

  @ApiProperty({ example: 'fcm-token-or-apns-token' })
  @IsString()
  @MaxLength(512)
  token!: string;

  @ApiProperty({ example: 'en-GB', required: false })
  @IsOptional()
  @IsString()
  @MaxLength(32)
  locale?: string;

  @ApiProperty({ example: '1.0.0', required: false })
  @IsOptional()
  @IsString()
  @MaxLength(32)
  appVersion?: string;
}
