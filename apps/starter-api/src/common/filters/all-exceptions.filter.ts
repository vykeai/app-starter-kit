import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';
import { Prisma } from '@prisma/client';

interface ErrorResponse {
  error: {
    code: string;
    message: string;
    details?: unknown;
    correlationId?: string;
    path: string;
    timestamp: string;
  };
}

@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  private readonly logger = new Logger(AllExceptionsFilter.name);

  catch(exception: unknown, host: ArgumentsHost): void {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();
    const correlationId = request.headers['x-correlation-id'] as string;

    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let code = 'INTERNAL_ERROR';
    let message = 'An unexpected error occurred';
    let details: unknown;

    if (exception instanceof HttpException) {
      status = exception.getStatus();
      const res = exception.getResponse();
      code = this.statusToCode(status);
      message = typeof res === 'string' ? res : (res as any).message ?? message;
      details = typeof res === 'object' ? (res as any).details : undefined;
    } else if (exception instanceof Prisma.PrismaClientKnownRequestError) {
      switch (exception.code) {
        case 'P2002':
          status = HttpStatus.CONFLICT;
          code = 'DUPLICATE_ENTRY';
          message = 'A record with this value already exists';
          break;
        case 'P2025':
          status = HttpStatus.NOT_FOUND;
          code = 'NOT_FOUND';
          message = 'Record not found';
          break;
        default:
          status = HttpStatus.BAD_REQUEST;
          code = 'DATABASE_ERROR';
          message = 'Database operation failed';
      }
    }

    if (status >= 500) {
      this.logger.error(
        `${request.method} ${request.path} → ${status}`,
        exception instanceof Error ? exception.stack : String(exception),
        { correlationId },
      );
    }

    const body: ErrorResponse = {
      error: {
        code,
        message,
        ...(details ? { details } : {}),
        ...(correlationId ? { correlationId } : {}),
        path: request.path,
        timestamp: new Date().toISOString(),
      },
    };

    response.status(status).json(body);
  }

  private statusToCode(status: number): string {
    const map: Record<number, string> = {
      400: 'BAD_REQUEST',
      401: 'UNAUTHORIZED',
      403: 'FORBIDDEN',
      404: 'NOT_FOUND',
      409: 'CONFLICT',
      422: 'UNPROCESSABLE',
      429: 'RATE_LIMITED',
    };
    return map[status] ?? 'ERROR';
  }
}
