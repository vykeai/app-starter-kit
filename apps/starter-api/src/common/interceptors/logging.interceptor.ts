import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
  Logger,
} from '@nestjs/common';
import { Observable, tap } from 'rxjs';
import { Request, Response } from 'express';

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  private readonly logger = new Logger('HTTP');

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const ctx = context.switchToHttp();
    const req = ctx.getRequest<Request>();
    const { method, path } = req;
    const correlationId = req.headers['x-correlation-id'] as string;
    const start = Date.now();

    return next.handle().pipe(
      tap(() => {
        const res = ctx.getResponse<Response>();
        const ms = Date.now() - start;
        this.logger.log(
          `${method} ${path} → ${res.statusCode} [${ms}ms]${correlationId ? ` [${correlationId}]` : ''}`,
        );
      }),
    );
  }
}
