import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Param,
  Body,
  Query,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import {
  ApiTags,
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiParam,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../../auth/jwt-auth.guard';
import { CurrentUser } from '../../user/decorators/current-user.decorator';
import { PaginationDto } from '../../common/dto/pagination.dto';
import { NoteService } from './note.service';
import { CreateNoteDto } from './dto/create-note.dto';
import { UpdateNoteDto } from './dto/update-note.dto';
import { NoteResponseDto } from './dto/note-response.dto';

@ApiTags('notes')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('notes')
export class NoteController {
  constructor(private readonly noteService: NoteService) {}

  @Get()
  @ApiOperation({ summary: 'List all notes for the authenticated user' })
  @ApiResponse({ status: 200, description: 'Paginated list of notes', type: NoteResponseDto, isArray: true })
  findAll(
    @CurrentUser() user: { userId: string },
    @Query() pagination: PaginationDto,
  ) {
    return this.noteService.findAll(user.userId, pagination);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get a single note by ID' })
  @ApiParam({ name: 'id', description: 'Note UUID' })
  @ApiResponse({ status: 200, description: 'The note', type: NoteResponseDto })
  @ApiResponse({ status: 404, description: 'Note not found' })
  findOne(
    @Param('id') id: string,
    @CurrentUser() user: { userId: string },
  ) {
    return this.noteService.findOne(id, user.userId);
  }

  @Post()
  @ApiOperation({ summary: 'Create a new note' })
  @ApiResponse({ status: 201, description: 'The created note', type: NoteResponseDto })
  create(
    @CurrentUser() user: { userId: string },
    @Body() dto: CreateNoteDto,
  ) {
    return this.noteService.create(user.userId, dto);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update a note' })
  @ApiParam({ name: 'id', description: 'Note UUID' })
  @ApiResponse({ status: 200, description: 'The updated note', type: NoteResponseDto })
  @ApiResponse({ status: 404, description: 'Note not found' })
  update(
    @Param('id') id: string,
    @CurrentUser() user: { userId: string },
    @Body() dto: UpdateNoteDto,
  ) {
    return this.noteService.update(id, user.userId, dto);
  }

  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({ summary: 'Soft-delete a note' })
  @ApiParam({ name: 'id', description: 'Note UUID' })
  @ApiResponse({ status: 204, description: 'Note deleted' })
  @ApiResponse({ status: 404, description: 'Note not found' })
  remove(
    @Param('id') id: string,
    @CurrentUser() user: { userId: string },
  ) {
    return this.noteService.remove(id, user.userId);
  }
}
