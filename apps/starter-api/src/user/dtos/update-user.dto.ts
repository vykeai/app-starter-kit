import { IsBoolean, IsOptional, IsString, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiPropertyOptional } from '@nestjs/swagger';

class UpdatePreferencesDto {
  @ApiPropertyOptional({ example: 'dark' })
  @IsOptional()
  @IsString()
  theme?: string;

  @ApiPropertyOptional({ example: false })
  @IsOptional()
  @IsBoolean()
  pushMarketingEnabled?: boolean;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  pushActivityEnabled?: boolean;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  pushTransactionalEnabled?: boolean;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  pushSystemEnabled?: boolean;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  emailNotificationsEnabled?: boolean;
}

export class UpdateUserDto {
  @ApiPropertyOptional({ example: 'John Doe' })
  @IsOptional()
  @IsString()
  displayName?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @ValidateNested()
  @Type(() => UpdatePreferencesDto)
  preferences?: UpdatePreferencesDto;
}
