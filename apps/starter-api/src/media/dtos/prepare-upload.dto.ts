import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsIn, IsNumber, IsOptional, IsString, Min } from 'class-validator';

export class PrepareUploadDto {
  @ApiProperty({ example: 'avatar' })
  @IsString()
  kind!: string;

  @ApiProperty({ example: 'image/webp' })
  @IsString()
  mimeType!: string;

  @ApiPropertyOptional({ example: 'profile-photo.webp' })
  @IsOptional()
  @IsString()
  fileName?: string;

  @ApiPropertyOptional({ example: 262144 })
  @IsOptional()
  @IsNumber()
  @Min(1)
  sizeBytes?: number;

  @ApiPropertyOptional({ example: 'private', enum: ['private', 'public'] })
  @IsOptional()
  @IsString()
  @IsIn(['private', 'public'])
  visibility?: 'private' | 'public';
}
