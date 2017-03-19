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
#include <unistd.h>
#include "maptool.h"
#include "debug.h"

char *
tempfile_name(char *suffix, char *name)
{
	return g_strdup_printf("%s_%s.tmp", name, suffix);
}

FILE *
tempfile(char *suffix, char *name, int mode)
{
	char *buffer = tempfile_name(suffix, name);
	FILE *ret = NULL;
	switch (mode)
	{
		case 0:
			ret = fopen(buffer, "rb");
			break;
		case 1:
			ret = fopen(buffer, "wb+");
			break;
		case 2:
			ret = fopen(buffer, "ab");
			break;
		case 13:
			ret = fopen(buffer, "rb+");
			break;
	}

	if (debug_itembin(6))
	{
		fprintf(stderr, "== tempfile == open == FILENAME: %s FILEPOINTER: %p ==\n", buffer, ret);
	}
	g_free(buffer);

	return ret;
}

void tempfile_unlink(char *suffix, char *name)
{
	char buffer[4096];
	sprintf(buffer, "%s_%s.tmp", name, suffix);

	//fprintf(stderr, "== tempfile == unlink == FILENAME: %s ==\n", buffer);

	unlink(buffer);
}

void tempfile_rename(char *suffix, char *from, char *to)
{
	char buffer_from[4096], buffer_to[4096];
	sprintf(buffer_from, "%s_%s.tmp", from, suffix);
	sprintf(buffer_to, "%s_%s.tmp", to, suffix);

	//fprintf(stderr, "== tempfile == rename == FILENAME FROM: %s FILENAME TO: %s ==\n", buffer_from, buffer_to);

	dbg_assert(rename(buffer_from, buffer_to) == 0);
}


#include <fcntl.h>
#include <errno.h>
int tempfile_cp(const char *to, const char *from)
{
    int fd_to, fd_from;
    char buf[4096];
    ssize_t nread;
    int saved_errno;

    fd_from = open(from, O_RDONLY);
    if (fd_from < 0)
	{
        return -1;
	}

    fd_to = open(to, O_WRONLY | O_CREAT | O_EXCL, 0666);
    if (fd_to < 0)
	{
        goto out_error;
	}

    while (nread = read(fd_from, buf, sizeof buf), nread > 0)
    {
        char *out_ptr = buf;
        ssize_t nwritten;

        do {
            nwritten = write(fd_to, out_ptr, nread);

            if (nwritten >= 0)
            {
                nread -= nwritten;
                out_ptr += nwritten;
            }
            else if (errno != EINTR)
            {
                goto out_error;
            }
        } while (nread > 0);
    }

    if (nread == 0)
    {
        if (close(fd_to) < 0)
        {
            fd_to = -1;
            goto out_error;
        }
        close(fd_from);

        /* Success! */
        return 0;
    }

  out_error:
    saved_errno = errno;

    close(fd_from);
    if (fd_to >= 0)
	{
        close(fd_to);
	}

    errno = saved_errno;
    return -1;
}

void tempfile_copyrename(char *suffix, char *from, char *to)
{
	char buffer_from[4096], buffer_to[4096];
	sprintf(buffer_from, "%s_%s.tmp", from, suffix);
	sprintf(buffer_to, "%s_%s.tmp", to, suffix);

	//fprintf(stderr, "== tempfile == copyrename == FILENAME FROM: %s FILENAME TO: %s ==\n", buffer_from, buffer_to);

	tempfile_cp(buffer_to, buffer_from);
	unlink(buffer_from);
}

