import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { PaginationDto, PaginatedResult } from '../../common/dto/pagination.dto';
import { paginateQuery } from '../../common/utils/paginate';
import { CreateNoteDto } from './dto/create-note.dto';
import { UpdateNoteDto } from './dto/update-note.dto';
import { NoteEntity } from './note.entity';

@Injectable()
export class NoteService {
  constructor(private readonly prisma: PrismaService) {}

  async findAll(userId: string, pagination: PaginationDto): Promise<PaginatedResult<NoteEntity>> {
    return paginateQuery<NoteEntity>(
      (args) =>
        (this.prisma as any).note.findMany({
          ...args,
          where: { userId, deletedAt: null },
          orderBy: { createdAt: 'desc' },
        }),
      () => (this.prisma as any).note.count({ where: { userId, deletedAt: null } }),
      pagination,
    );
  }

  async findOne(id: string, userId: string): Promise<NoteEntity> {
    const note = await (this.prisma as any).note.findFirst({
      where: { id, userId, deletedAt: null },
    });
    if (!note) {
      throw new NotFoundException(`Note ${id} not found`);
    }
    return note as NoteEntity;
  }

  async create(userId: string, dto: CreateNoteDto): Promise<NoteEntity> {
    return (this.prisma as any).note.create({
      data: {
        userId,
        title: dto.title,
        content: dto.content ?? null,
      },
    }) as Promise<NoteEntity>;
  }

  async update(id: string, userId: string, dto: UpdateNoteDto): Promise<NoteEntity> {
    await this.findOne(id, userId); // Ownership check — throws NotFoundException if not found
    return (this.prisma as any).note.update({
      where: { id },
      data: {
        ...(dto.title !== undefined ? { title: dto.title } : {}),
        ...(dto.content !== undefined ? { content: dto.content } : {}),
      },
    }) as Promise<NoteEntity>;
  }

  async remove(id: string, userId: string): Promise<void> {
    await this.findOne(id, userId); // Ownership check — throws NotFoundException if not found
    await (this.prisma as any).note.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }
}
