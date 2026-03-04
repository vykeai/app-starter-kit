import { ApiProperty } from '@nestjs/swagger';
import { IsString, IsIn } from 'class-validator';

export class SocialAuthDto {
  @ApiProperty({ enum: ['apple', 'google'], description: 'OAuth provider' })
  @IsString()
  @IsIn(['apple', 'google'])
  provider: 'apple' | 'google';

  @ApiProperty({ description: 'Identity token from provider' })
  @IsString()
  idToken: string;
}
