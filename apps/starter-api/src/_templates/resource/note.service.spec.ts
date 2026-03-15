import { Test, TestingModule } from '@nestjs/testing';
import { NotFoundException } from '@nestjs/common';
import { NoteService } from './note.service';
import { PrismaService } from '../../prisma/prisma.service';

const mockNote = {
  id: 'note-uuid-1',
  userId: 'user-uuid-1',
  title: 'Test Note',
  content: 'Some content',
  createdAt: new Date(),
  updatedAt: new Date(),
  deletedAt: null,
};

const mockPrisma = {
  note: {
    findMany: jest.fn(),
    findFirst: jest.fn(),
    create: jest.fn(),
    update: jest.fn(),
    count: jest.fn(),
  },
};

describe('NoteService', () => {
  let service: NoteService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        NoteService,
        { provide: PrismaService, useValue: mockPrisma },
      ],
    }).compile();

    service = module.get<NoteService>(NoteService);
    jest.clearAllMocks();
  });

  describe('create', () => {
    it('creates a note and returns it', async () => {
      mockPrisma.note.create.mockResolvedValue(mockNote);

      const result = await service.create('user-uuid-1', {
        title: 'Test Note',
        content: 'Some content',
      });

      expect(mockPrisma.note.create).toHaveBeenCalledWith({
        data: {
          userId: 'user-uuid-1',
          title: 'Test Note',
          content: 'Some content',
        },
      });
      expect(result).toEqual(mockNote);
    });
  });

  describe('findOne', () => {
    it('returns a note when found', async () => {
      mockPrisma.note.findFirst.mockResolvedValue(mockNote);

      const result = await service.findOne('note-uuid-1', 'user-uuid-1');

      expect(mockPrisma.note.findFirst).toHaveBeenCalledWith({
        where: { id: 'note-uuid-1', userId: 'user-uuid-1', deletedAt: null },
      });
      expect(result).toEqual(mockNote);
    });

    it('throws NotFoundException when note not found', async () => {
      mockPrisma.note.findFirst.mockResolvedValue(null);

      await expect(service.findOne('missing-id', 'user-uuid-1')).rejects.toThrow(
        NotFoundException,
      );
    });
  });
});
