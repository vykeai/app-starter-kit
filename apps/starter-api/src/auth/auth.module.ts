import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { BullModule } from '@nestjs/bull';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { JwtStrategy } from './jwt.strategy';
import { EmailProcessor } from './email.processor';

@Module({
  imports: [
    JwtModule.registerAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        secret: config.get<string>('JWT_SECRET', 'dev-secret'),
        signOptions: { expiresIn: '15m' },
      }),
    }),
    BullModule.registerQueue({ name: 'email' }),
  ],
  providers: [AuthService, JwtStrategy, EmailProcessor],
  controllers: [AuthController],
  exports: [JwtModule],
})
export class AuthModule {}
