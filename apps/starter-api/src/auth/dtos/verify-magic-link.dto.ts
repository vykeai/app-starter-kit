import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsDefined, IsEmail, IsOptional, IsString, Length, ValidateIf } from 'class-validator';

export class VerifyMagicLinkDto {
  @ApiPropertyOptional({ example: 'user@example.com' })
  @ValidateIf((dto) => !dto.linkToken)
  @IsDefined()
  @IsEmail()
  email: string;

  @ApiPropertyOptional({ example: '12345678' })
  @ValidateIf((dto) => !dto.linkToken)
  @IsDefined()
  @IsString()
  @Length(8, 8)
  code: string;

  @ApiPropertyOptional({
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.magic-link.example',
    description: 'Opaque one-tap token embedded in auth emails and review links.',
  })
  @ValidateIf((dto) => !dto.email || !dto.code)
  @IsString()
  @IsOptional()
  linkToken?: string;
}
