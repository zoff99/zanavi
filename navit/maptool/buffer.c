/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2012 Zoff <zoff@zoff.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2011 Navit Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

#define _FILE_OFFSET_BITS 64
#define _LARGEFILE_SOURCE
#define _LARGEFILE64_SOURCE
#include <stdlib.h>
#include "maptool.h"
#include "debug.h"

void save_buffer(char *filename, struct buffer *b, long long offset)
{
	FILE *f;
	f = fopen(filename, "rb+");
	if (!f)
	{
		f = fopen(filename, "wb+");
	}

	dbg_assert(f != NULL);

	off_t offset_64 = (off_t) offset;

	if (verbose_mode) fprintf(stderr,"save_buffer:seeking bytes in %s to "LONGLONG_FMT"\n", filename, offset_64);
	if (verbose_mode) fprintf(stderr,"save_buffer: base "LONGLONG_FMT"\n", b->base);

	int ret;
	// ret=fseek(f, offset, SEEK_SET);
	ret = fseeko(f, offset_64, SEEK_SET);
	//fprintf(stderr,"ret.code of seek=%d\n", ret);
	size_t ret2;
	ret2 = fwrite(b->base, b->size, 1, f);
	//fprintf(stderr,"ret.code of fwrite=%d\n", ret2);
	fclose(f);
}

void load_buffer_fp(FILE *f, struct buffer *b, long long offset, long long size)
{
	long long len;
	int ret;

	if (verbose_mode) fprintf(stderr,"load_buffer:offset="LONGLONG_FMT" size="LONGLONG_FMT"\n", offset, size);

	// fprintf(stderr,"load_buffer 1 buffer ptr=%d\n", b->base);

	int reuse_buffer = 0;

	if (verbose_mode) fprintf(stderr,"load_buffer:info has b->malloced=%d\n", b->malloced);
	if (verbose_mode) fprintf(stderr,"load_buffer:info     has b->size="LONGLONG_FMT"\n", (size_t)b->size);
	if (verbose_mode) fprintf(stderr,"load_buffer:info       want size="LONGLONG_FMT"\n", size);
	if (verbose_mode) fprintf(stderr,"load_buffer:info            base="LONGLONG_FMT"\n", b->base);
	if (b->base)
	{
		if (size > b->size)
		{
			if (verbose_mode) fprintf(stderr, "load_buffer:# freeing buffer #\n");
			free(b->base);
			//fprintf(stderr,"load_buffer 2 buffer ptr=%d\n", b->base);
			b->base = NULL;
		}
		else
		{
			if (verbose_mode) fprintf(stderr, "load_buffer:@ re-using buffer @\n");
			reuse_buffer = 1;
		}
	}
	else
	{
		//fprintf(stderr,"* no buffer to free *\n");
	}
	//fprintf(stderr,"load_buffer 3 buffer ptr=%d\n", b->base);

	if (!reuse_buffer)
	{
		b->malloced = 0;
	}

	fseek(f, 0, SEEK_END);
	len = ftello(f); // 64bit
	//fprintf(stderr,"filep=%p\n", f);
	//fprintf(stderr,"load_buffer ftell="LONGLONG_FMT"\n", len);
	if (offset + size > len)
	{
		size = len - offset;
		if (verbose_mode) fprintf(stderr,"load_buffer new size="LONGLONG_FMT"\n", size);
		ret = 1;
	}

	b->size = size;
	b->malloced = size;

	off_t offset_64 = (off_t) offset;
	if (verbose_mode) fprintf(stderr,"load_buffer:reading "LONGLONG_FMT" bytes at "LONGLONG_FMT"\n", b->size, offset_64);

	int ret3;
	ret3 = fseeko(f, offset_64, SEEK_SET); // 64bit

	if (!reuse_buffer)
	{
		b->base = malloc((size_t) size);
		if (verbose_mode) fprintf(stderr, "load_buffer:ret.code of malloc="LONGLONG_FMT"\n", b->base);
	}

	dbg_assert(b->base != NULL);
	fread(b->base, b->size, 1, f);
}

void load_buffer(char *filename, struct buffer *b, long long offset, long long size)
{
	// wrapper for "filename" -> "file pointer"
	FILE *f;

	f = fopen(filename, "rb");
	load_buffer_fp(f, b, offset, size);
	fclose(f);
}

