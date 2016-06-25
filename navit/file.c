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
#include "config.h"

#ifdef HAVE_UNISTD_H
#include <unistd.h>
#endif

#ifdef _MSC_VER
#include <windows.h>
#else
#include <dirent.h>
#endif /* _MSC_VER */

#include <string.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <stdio.h>
#include <stdlib.h>
#include <wordexp.h>
#include <glib.h>
#include <zlib.h>
#include "debug.h"
#include "cache.h"
#include "file.h"
#include "atom.h"
#include "item.h"
#include "util.h"
#include "types.h"

#ifdef HAVE_SOCKET
#include <sys/socket.h>
#include <netdb.h>
#endif

#include "navit.h"

extern char *version;

#ifdef HAVE_LIBCRYPTO
#include <openssl/sha.h>
#include <openssl/hmac.h>
#include <openssl/aes.h>
#include <openssl/evp.h>
#include <openssl/rand.h>
#endif

#ifdef HAVE_API_ANDROID
#define lseek lseek64
#endif

#ifndef O_LARGEFILE
#define O_LARGEFILE 0
#endif

#ifndef O_BINARY
#define O_BINARY 0
#endif

#ifdef CACHE_SIZE
static GHashTable *file_name_hash;
#endif

static struct cache *file_cache;

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct file_cache_id
{
	long long offset;
	int size;
	int file_name_id;
	int method;
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#ifdef HAVE_SOCKET
static int
file_socket_connect(char *host, char *service)
{
	//dbg(0,"_enterXX %s %s\n", host, service);
	int fd;
	return fd;
}

static void
file_http_request(struct file *file, char *method, char *host, char *path, char *header, int persistent)
{
	//dbg(0,"_enterXX %s\n", path);
}

static int
file_request_do(struct file *file, struct attr **options, int connect)
{
	//dbg(0,"_enterXX %s\n", file->name);
	return 1;
}
#endif

static unsigned char *
file_http_header_end(unsigned char *str, int len)
{
	//dbg(0,"_enterXX %s\n", str);
	return NULL;
}

int file_request(struct file *f, struct attr **options)
{
	//dbg(0,"_enterXX %s\n", f->name);
	return 0;
}

char *
file_http_header(struct file *f, char *header)
{
	//dbg(0,"_enterXX %s\n", f->name);
	return NULL;
}

struct file *
file_create(char *name, struct attr **options)
{
	//dbg(0,"_enterPP %s\n", name);
	struct stat stat;
	struct file *file= g_new0(struct file,1);
	struct attr *attr;
	int open_flags = O_LARGEFILE | O_BINARY;

	if (options && (attr = attr_search(options, NULL, attr_url)))
	{
#ifdef HAVE_SOCKET
		//dbg(0,"_HAVE_SOCKET\n");
		file_request_do(file, options, 1);
#endif
	}
	else
	{
		if (options && (attr = attr_search(options, NULL, attr_readwrite)) && attr->u.num)
		{
			open_flags |= O_RDWR;
			if ((attr = attr_search(options, NULL, attr_create)) && attr->u.num)
			{
				open_flags |= O_CREAT;
			}
		}
		else
		{
			open_flags |= O_RDONLY;
		}

		file->name = g_strdup(name);
		file->fd = open(name, open_flags, 0666);
		file->current_splitter = 0; // ORIG file opened
		if (file->fd == -1)
		{
			g_free(file);
			return NULL;
		}
		dbg(0, "_fd=%d\n", file->fd);

		// set default value
		file->num_splits = 0;
		file->size = file_size(file);

		dbg(0,"_size="LONGLONG_FMT"\n", file->size);
		file->name_id = (long) atom(name);
	}
#ifdef CACHE_SIZE
	//if (!options || !(attr=attr_search(options, NULL, attr_cache)) || attr->u.num)
	//{
	dbg(0,"_cache=1 for %s\n",name);
	file->cache=1;
	//}
#endif
	dbg_assert(file != NULL);
	return file;
}

#ifndef S_ISDIR
#define S_ISDIR(m) (((m) & S_IFMT) == S_IFDIR)
#endif
#ifndef S_ISREG
#define S_ISREG(m) (((m) & S_IFMT) == S_IFREG)
#endif

int file_is_dir(char *name)
{
	//dbg(0,"_enterOO %s\n", name);
	struct stat buf;
	if (!stat(name, &buf))
	{
		return S_ISDIR(buf.st_mode);
	}
	return 0;
}

int file_is_reg(char *name)
{
	//dbg(0,"_enterOO\n");
	struct stat buf;
	if (!stat(name, &buf))
	{
		return S_ISREG(buf.st_mode);
	}
	return 0;
}

long long file_size(struct file *file)
{
	dbg(0, "_enterPP %s\n", file->name);

	// first get and set orig file size
	struct stat buf2;
	stat(file->name, &buf2);
	file->size = buf2.st_size;
	dbg(0,"_orig_size="LONGLONG_FMT"\n", file->size);

	// use initial size
	long long f_size = file->size;
	file->split_size_in_bytes = 0;
	file->last_splitter_size_in_bytes = 0;

	// size of all the splitters (assume all the splitter have SAME byte size!!)
	file->split_size_in_bytes = file->size;

	int i;
	int j = 0;
	int finished = 0;
	for (i = 0; i < MAX_SPLIT_FILES; i++)
	{
		if (finished == 0)
		{
			char *name = g_strdup_printf("%s.%d", file->name, (i + 1));
			dbg(0, "_i=%d name=%s\n", (i + 1), name);
			struct stat buf;
			if (!stat(name, &buf))
			{
				dbg(0,"_st_size="LONGLONG_FMT"\n", buf.st_size);
				f_size = f_size + buf.st_size;
				j = i + 1;
				if (i == 0)
				{
				}
				else
				{
					// size of last splitter (will be most likely smaller since its the last part)
					file->last_splitter_size_in_bytes = buf.st_size;
				}
			}
			else
			{
				finished = 1;
			}
			g_free(name);
		}
	}

	// set num. of split files
	file->num_splits = j;
	dbg(0, "_num_splits=%d\n", file->num_splits);

	dbg(0,"_all_size="LONGLONG_FMT"\n", f_size);
	return f_size;
	// return file->size;
}

int file_mkdir(char *name, int pflag)
{
	//dbg(0,"_enterOO %s\n", name);
	char *buffer = g_alloca(sizeof(char) * (strlen(name) + 1));
	int ret;
	char *next;
	// dbg(1, "enter %s %d\n", name, pflag);
	if (!pflag)
	{
		if (file_is_dir(name))
		{
			return 0;
		}
#if defined HAVE_API_WIN32_BASE || defined _MSC_VER
		return mkdir(name);
#else
		return mkdir(name, 0777);
#endif
	}
	strcpy(buffer, name);
	next = buffer;
	while ((next = strchr(next, '/')))
	{
		*next = '\0';
		if (*buffer)
		{
			ret = file_mkdir(buffer, 0);
			if (ret)
			{
				return ret;
			}
		}
		*next++ = '/';
	}
	if (pflag == 2)
	{
		return 0;
	}
	return file_mkdir(buffer, 0);
}

int file_mmap(struct file *file)
{
	//dbg(0,"_enterP? %s\n", file->name);
	int mmap_size = file->size;

	// turn off mmap!!
	if (file->num_splits > 0)
	{
		file->begin = NULL;
		return 0;
	}
	// turn off mmap!!

#ifdef HAVE_API_WIN32_BASE
	file->begin = (char*)mmap_readonly_win32( file->name, &file->map_handle, &file->map_file );
#else
	file->begin = mmap(NULL, mmap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, file->fd, 0);
	dbg_assert(file->begin != NULL);
	if (file->begin == (void *) 0xffffffff)
	{
		perror("mmap");
		return 0;
	}
#endif

	dbg_assert(file->begin != (void *) 0xffffffff);
	file->mmap_end = file->begin + mmap_size;
	file->end = file->begin + file->size;

	return 1;
}

unsigned char *
file_data_read(struct file *file, long long offset, int size)
{
	//dbg(0,"_enterPP %s "LONGLONG_FMT" %d\n", file->name, offset, size);
	void *ret;
	if (file->special)
	{
		return NULL;
	}

	// turn off mmap
	if (file->num_splits == 0)
	// turn off mmap
	{
		if (file->begin)
		{
			//dbg(0,"_leave:begin\n");
			return file->begin + offset;
		}
	}

	if (file_cache)
	{
		//dbg(0,"_file_cache\n");
		struct file_cache_id id = { offset, size, file->name_id, 0 };
		ret = cache_lookup(file_cache, &id);
		if (ret)
		{
			//dbg(0,"_file_cache:found\n");
			return ret;
		}
		//dbg(0,"_file_cache:insert\n");
		ret = cache_insert_new(file_cache, &id, size);
	}
	else
	{
		//dbg(0,"_g_malloc\n");
		ret = g_malloc(size);
	}

	if (file->num_splits == 0)
	{
		//dbg(0,"_num_splits=0\n");
		lseek(file->fd, offset, SEEK_SET);
		if (read(file->fd, ret, size) != size)
		{
			file_data_free(file, ret);
			ret = NULL;
		}
	}
	else
	{
		//dbg(0,"_num_splits=1\n");

		/* ++++++++++++++++++++++++++++++++++++++++++ */
		/* ++++++++++++++++++++++++++++++++++++++++++ */
		int start_splitter;
		long long offset_split;
		long long offset2;
		int size2;
		int read_size;
		int need_more = 1;
		char *ret2; // just define any sort of pointer, maybe this can be done better??

		ret2 = ret; // copy startaddress of buffer
		offset2 = offset;
		size2 = size;

		while (need_more == 1)
		{
			//dbg(0,"_X:of2="LONGLONG_FMT" split_size_in_bytes=%d\n", offset2, file->split_size_in_bytes);
			start_splitter = (offset2 / file->split_size_in_bytes);
			offset_split = offset2 - (file->split_size_in_bytes * start_splitter);
			//dbg(0,"_X:sp=%d ofs="LONGLONG_FMT"\n", start_splitter, offset_split);

			// open
			if (file->current_splitter != start_splitter)
			{
				//dbg(0,"_X:curr=%d want=%d\n", file->current_splitter, start_splitter);
				// close file
				close(file->fd);
				// open new file
				int open_flags = O_LARGEFILE | O_BINARY;
				open_flags |= O_RDONLY;
				char *name;
				if (start_splitter == 0)
				{
					name = g_strdup_printf("%s", file->name);
				}
				else
				{
					name = g_strdup_printf("%s.%d", file->name, start_splitter);
				}
				//dbg(0,"_X:open=%s\n", name);
				file->fd = open(name, open_flags, 0666);
				//dbg(0,"_X:isopen=%d\n", file->fd);
				file->current_splitter = start_splitter; // set which file is currently open
				g_free(name);
			}

			if (start_splitter == file->num_splits)
			{
				// in last splitter, so just read to the end
				read_size = size2;
				// read
				lseek(file->fd, offset_split, SEEK_SET);
				// ***** int result = lseek(file->fd, offset_split, SEEK_SET);
				// dbg(0,"_1:seek="LONGLONG_FMT" read=%d result=%d\n", offset_split, read_size, result);
				int did_read = read(file->fd, ret2, read_size);
				if (did_read != read_size)
				{
					file_data_free(file, ret);
					ret = NULL;
					//dbg(0,"_leave:error 001 read:%d\n", did_read);
					return ret;
				}
				//dbg(0,"_1:read ok:%d\n", did_read);
				// +++
				need_more = 0;
			}
			else if ((offset_split + size2) > file->split_size_in_bytes)
			{
				read_size = file->split_size_in_bytes - offset_split;
				// read
				lseek(file->fd, offset_split, SEEK_SET);
				//dbg(0,"_2:seek="LONGLONG_FMT" read=%d\n", offset_split, read_size);
				int did_read = read(file->fd, ret2, read_size);
				if (did_read != read_size)
				{
					file_data_free(file, ret);
					ret = NULL;
					//dbg(0,"_leave:error 002 read:%d\n", did_read);
					return ret;
				}
				//dbg(0,"_2:read ok:%d\n", did_read);
				// +++
				if (read_size < size2)
				{
					// set new values
					offset2 = offset2 + read_size; // advance offset
					ret2 = ret2 + read_size; // advance buffer
					size2 = size2 - read_size; // calc new size
					need_more = 1;
				}
				else
				{
					need_more = 0;
				}
			}
			else
			{
				// the read size is inside 1 splitter, so just read it
				read_size = size2;
				// read
				lseek(file->fd, offset_split, SEEK_SET);
				//dbg(0,"_3:seek="LONGLONG_FMT" read=%d\n", offset_split, read_size);
				int did_read = read(file->fd, ret2, read_size);
				if (did_read != read_size)
				{
					file_data_free(file, ret);
					ret = NULL;
					//dbg(0,"_leave:error 003 read:%d\n", did_read);
					return ret;
				}
				//dbg(0,"_3:read ok:%d\n", did_read);
				// +++
				need_more = 0;
			}
		}
		/* ++++++++++++++++++++++++++++++++++++++++++ */
		/* ++++++++++++++++++++++++++++++++++++++++++ */
	}

	//dbg(0,"_leave:normal\n");
	return ret;
}

static void file_process_headers(struct file *file, unsigned char *headers)
{
	//dbg(0,"_enter %s\n", file->name);
	char *tok;
	char *cl;
	if (file->headers)
	{
		g_hash_table_destroy(file->headers);
	}
	file->headers = g_hash_table_new_full(g_str_hash, g_str_equal, g_free_func, NULL);
	while ((tok = strtok((char*) headers, "\r\n")))
	{
		char *sep;
		tok = g_strdup(tok);
		sep = strchr(tok, ':');
		if (!sep)
		{
			sep = strchr(tok, '/');
		}
		if (!sep)
		{
			g_free(tok);
			continue;
		}
		*sep++ = '\0';
		if (*sep == ' ')
		{
			sep++;
		}
		strtolower(tok, tok);
		// dbg(1, "header '%s'='%s'\n", tok, sep);
		g_hash_table_insert(file->headers, tok, sep);
		headers = NULL;
	}
	cl = g_hash_table_lookup(file->headers, "content-length");
	if (cl)
	{
#ifdef HAVE__ATOI64
		file->size=_atoi64(cl);
#else
		file->size = atoll(cl);
#endif
	}
}

static void file_shift_buffer(struct file *file, int amount)
{
	//dbg(0,"_enter %s %d\n", file->name, amount);
	memmove(file->buffer, file->buffer + amount, file->buffer_len - amount);
	file->buffer_len -= amount;
}

unsigned char *
file_data_read_special(struct file *file, int size, int *size_ret)
{
	//dbg(0,"_enter %s %d\n", file->name, size);
	unsigned char *ret, *hdr;
	int rets = 0, rd;
	int buffer_size = 8192;
	int eof = 0;

	if (!file->special)
	{
		return NULL;
	}

	if (!file->buffer)
	{
		file->buffer = g_malloc(buffer_size);
	}

	ret = g_malloc(size);
	while ((size > 0 || file->requests) && (!eof || file->buffer_len))
	{
		int toread = buffer_size - file->buffer_len;
		if (toread >= 4096 && !eof)
		{
			if (!file->requests && toread > size)
				toread = size;
			rd = read(file->fd, file->buffer + file->buffer_len, toread);
			if (rd > 0)
			{
				file->buffer_len += rd;
			}
			else
				eof = 1;
		}
		if (file->requests)
		{
			// dbg(1, "checking header\n");
			if ((hdr = file_http_header_end(file->buffer, file->buffer_len)))
			{
				hdr[-1] = '\0';
				// dbg(1, "found %s (%d bytes)\n", file->buffer, sizeof(file->buffer));
				file_process_headers(file, file->buffer);
				file_shift_buffer(file, hdr - file->buffer);
				file->requests--;
				if (file_http_header(file, "location"))
				{
					break;
				}
			}
		}
		if (!file->requests)
		{
			rd = file->buffer_len;
			if (rd > size)
				rd = size;
			memcpy(ret + rets, file->buffer, rd);
			file_shift_buffer(file, rd);
			rets += rd;
			size -= rd;
		}
	}
	*size_ret = rets;
	return ret;
}

unsigned char *
file_data_read_all(struct file *file)
{
	//dbg(0,"_enter %s\n", file->name);
	return file_data_read(file, 0, file->size);
}

void file_data_flush(struct file *file, long long offset, int size)
{
	//dbg(0,"_enter %s "LONGLONG_FMT" %d\n", file->name, offset, size);
}

int file_data_write(struct file *file, long long offset, int size, unsigned char *data)
{
	//dbg(0,"_enter %s "LONGLONG_FMT" %d\n", file->name, offset, size);
	return 1;
}

int file_get_contents(char *name, unsigned char **buffer, int *size)
{
	//dbg(0,"_enter %s\n", name);
	struct file *file;
	file = file_create(name, 0);
	if (!file)
	{
		return 0;
	}
	*size = file_size(file);
	*buffer = file_data_read_all(file);
	file_destroy(file);
	return 1;
}

static int uncompress_int(Bytef *dest, uLongf *destLen, const Bytef *source, uLong sourceLen)
{
	//dbg(0,"_enter %d %d\n", sourceLen, destLen);
	z_stream stream;
	int err;

	stream.next_in = (Bytef*) source;
	stream.avail_in = (uInt) sourceLen;
	stream.next_out = dest;
	stream.avail_out = (uInt) * destLen;

	stream.zalloc = (alloc_func) 0;
	stream.zfree = (free_func) 0;

	err = inflateInit2(&stream, -MAX_WBITS);
	if (err != Z_OK)
	{
		return err;
	}

	err = inflate(&stream, Z_FINISH);
	if (err != Z_STREAM_END)
	{
		inflateEnd(&stream);
		if (err == Z_NEED_DICT || (err == Z_BUF_ERROR && stream.avail_in == 0))
		{
			return Z_DATA_ERROR;
		}
		return err;
	}

	*destLen = stream.total_out;

	err = inflateEnd(&stream);
	return err;
}

unsigned char *
file_data_read_compressed(struct file *file, long long offset, int size, int size_uncomp)
{
	//dbg(0,"_enterPP %s "LONGLONG_FMT" %d\n", file->name, offset, size);
	void *ret;
	char *buffer = 0;
	uLongf destLen = size_uncomp;

	if (file_cache)
	{
		//dbg(0,"_file_cache 2\n");
		struct file_cache_id id = { offset, size, file->name_id, 1 };
		ret = cache_lookup(file_cache, &id);
		if (ret)
		{
			//dbg(0,"_file_cache found 2\n");
			return ret;
		}
		//dbg(0,"_file_cache create new 2\n");
		ret = cache_insert_new(file_cache, &id, size_uncomp);
	}
	else
	{
		ret = g_malloc(size_uncomp);
	}

	buffer = (char *) g_malloc(size);

	if (file->num_splits == 0)
	{
		//dbg(0,"_num_splits=0\n");
		lseek(file->fd, offset, SEEK_SET);
		if (read(file->fd, buffer, size) != size)
		{
			g_free(ret);
			ret = NULL;
		}
		else
		{
			if (uncompress_int(ret, &destLen, (Bytef *) buffer, size) != Z_OK)
			{
				//dbg(0, "_uncompress failed\n");
				g_free(ret);
				ret = NULL;
			}
		}
	}
	else
	{
		//dbg(0,"_num_splits=1\n");

		/* ++++++++++++++++++++++++++++++++++++++++++ */
		/* ++++++++++++++++++++++++++++++++++++++++++ */
		int start_splitter;
		long long offset_split;
		long long offset2;
		int size2;
		int read_size;
		int need_more = 1;
		char *ret2 = 0; // just define any sort of pointer, maybe this can be done better??

		ret2 = buffer; // copy startaddress of buffer
		offset2 = offset;
		size2 = size;

		while (need_more == 1)
		{
			//dbg(0,"_X:of2="LONGLONG_FMT" split_size_in_bytes=%d\n", offset2, file->split_size_in_bytes);
			start_splitter = (offset2 / file->split_size_in_bytes);
			offset_split = offset2 - (file->split_size_in_bytes * start_splitter);
			//dbg(0,"_X:sp=%d ofs="LONGLONG_FMT"\n", start_splitter, offset_split);

			// open
			if (file->current_splitter != start_splitter)
			{
				//dbg(0,"_X:curr=%d want=%d\n", file->current_splitter, start_splitter);
				// close file
				close(file->fd);
				// open new file
				int open_flags = O_LARGEFILE | O_BINARY;
				open_flags |= O_RDONLY;
				char *name;
				if (start_splitter == 0)
				{
					name = g_strdup_printf("%s", file->name);
				}
				else
				{
					name = g_strdup_printf("%s.%d", file->name, start_splitter);
				}
				//dbg(0,"_X:open=%s\n", name);
				file->fd = open(name, open_flags, 0666);
				//dbg(0,"_X:isopen=%d\n", file->fd);
				file->current_splitter = start_splitter; // set which file is currently open
				g_free(name);
			}

			if (start_splitter == file->num_splits)
			{
				// in last splitter, so just read to the end
				read_size = size2;
				// read
				lseek(file->fd, offset_split, SEEK_SET);
				// *** int result = lseek(file->fd, offset_split, SEEK_SET);
				//dbg(0,"_1:seek="LONGLONG_FMT" read=%d result=%d\n", offset_split, read_size, result);
				int did_read = read(file->fd, ret2, read_size);
				if (did_read != read_size)
				{
					g_free(buffer);
					buffer = NULL;
					g_free(ret);
					ret = NULL;
					//dbg(0,"_leave:error 001 read:%d\n", did_read);
					return ret;
				}
				//dbg(0,"_1:read ok:%d\n", did_read);
				// +++
				need_more = 0;
			}
			else if ((offset_split + size2) > file->split_size_in_bytes)
			{
				read_size = file->split_size_in_bytes - offset_split;
				// read
				lseek(file->fd, offset_split, SEEK_SET);
				//dbg(0,"_2:seek="LONGLONG_FMT" read=%d\n", offset_split, read_size);
				int did_read = read(file->fd, ret2, read_size);
				if (did_read != read_size)
				{
					g_free(buffer);
					buffer = NULL;
					g_free(ret);
					ret = NULL;
					//dbg(0,"_leave:error 002 read:%d\n", did_read);
					return ret;
				}
				//dbg(0,"_2:read ok:%d\n", did_read);
				// +++
				if (read_size < size2)
				{
					// set new values
					offset2 = offset2 + read_size; // advance offset
					ret2 = ret2 + read_size; // advance buffer
					size2 = size2 - read_size; // calc new size
					need_more = 1;
				}
				else
				{
					need_more = 0;
				}
			}
			else
			{
				// the read size is inside 1 splitter, so just read it
				read_size = size2;
				// read
				lseek(file->fd, offset_split, SEEK_SET);
				//dbg(0,"_3:seek="LONGLONG_FMT" read=%d\n", offset_split, read_size);
				int did_read = read(file->fd, ret2, read_size);
				if (did_read != read_size)
				{
					g_free(buffer);
					buffer = NULL;
					g_free(ret);
					ret = NULL;
					//dbg(0,"_leave:error 003 read:%d\n", did_read);
					return ret;
				}
				//dbg(0,"_3:read ok:%d\n", did_read);
				// +++
				need_more = 0;
			}
		}
		/* ++++++++++++++++++++++++++++++++++++++++++ */
		/* ++++++++++++++++++++++++++++++++++++++++++ */

		if (buffer != NULL)
		{
			if (uncompress_int(ret, &destLen, (Bytef *) buffer, size) != Z_OK)
			{
				//dbg(0, "_uncompress failed\n");
				g_free(ret);
				ret = NULL;
			}
		}

	}

	g_free(buffer);

	return ret;
}

unsigned char *
file_data_read_encrypted(struct file *file, long long offset, int size, int size_uncomp, int compressed, char *passwd)
{
	//dbg(0,"_enter %s "LONGLONG_FMT" %d\n", file->name, offset, size);
#ifdef HAVE_LIBCRYPTO
	void *ret;
	unsigned char *buffer = 0;
	uLongf destLen=size_uncomp;

	if (file_cache)
	{
		//dbg(0,"_file_cache 3\n");
		struct file_cache_id id =
		{	offset,size,file->name_id,1};
		ret=cache_lookup(file_cache,&id);
		if (ret)
		{
			//dbg(0,"_file_cache found 3\n");
			return ret;
		}
		//dbg(0,"_file_cache create new 3\n");
		ret=cache_insert_new(file_cache,&id,size_uncomp);
	}
	else
	{
		ret=g_malloc(size_uncomp);
	}
	lseek(file->fd, offset, SEEK_SET);

	buffer = (unsigned char *)g_malloc(size);
	if (read(file->fd, buffer, size) != size)
	{
		g_free(ret);
		ret=NULL;
	}
	else
	{
		unsigned char key[34], salt[8], verify[2], counter[16], xor[16], mac[10], *datap;
		int overhead=sizeof(salt)+sizeof(verify)+sizeof(mac);
		int esize=size-overhead;
		PKCS5_PBKDF2_HMAC_SHA1(passwd, strlen(passwd), (unsigned char *)buffer, 8, 1000, 34, key);
		if (key[32] == buffer[8] && key[33] == buffer[9] && esize >= 0)
		{
			AES_KEY aeskey;
			AES_set_encrypt_key(key, 128, &aeskey);
			datap=buffer+sizeof(salt)+sizeof(verify);
			memset(counter, 0, sizeof(counter));
			while (esize > 0)
			{
				int i,curr_size,idx=0;
				do
				{
					counter[idx]++;
				}while (!counter[idx++]);

				AES_encrypt(counter, xor, &aeskey);
				curr_size=esize;
				if (curr_size > sizeof(xor))
				curr_size=sizeof(xor);
				for (i = 0; i < curr_size; i++)
				*datap++^=xor[i];
				esize-=curr_size;
			}
			size-=overhead;
			datap=buffer+sizeof(salt)+sizeof(verify);
			if (compressed)
			{
				if (uncompress_int(ret, &destLen, (Bytef *)datap, size) != Z_OK)
				{
					//dbg(0,"_uncompress failed\n");
					g_free(ret);
					ret=NULL;
				}
			}
			else
			{
				if (size == destLen)
				memcpy(ret, buffer, destLen);
				else
				{
					//dbg(0,"_memcpy failed\n");
					g_free(ret);
					ret=NULL;
				}
			}
		}
		else
		{
			g_free(ret);
			ret=NULL;
		}
	}
	g_free(buffer);

	return ret;
#else
	return NULL;
#endif
}

void file_data_free(struct file *file, unsigned char *data)
{
	//dbg(0,"_enter %s\n", file->name);
	if (file->begin)
	{
		if (data == file->begin)
			return;
		if (data >= file->begin && data < file->end)
			return;
	}

	if (file->cache && data)
	{
		// print statistics to DEBUG OUT
		//if (file_cache)
		//{
		//cache_stats(file_cache);
		//}
		// print statistics to DEBUG OUT
		cache_entry_destroy(file_cache, data);
	}
	else
	{
		g_free(data);
	}
	//dbg(0,"_leave\n");
}

void file_data_remove(struct file *file, unsigned char *data)
{
	//dbg(0,"_enter %s\n", file->name);
	if (file->begin)
	{
		if (data == file->begin)
		{
			return;
		}
		if (data >= file->begin && data < file->end)
		{
			return;
		}
	}
	if (file->cache && data)
	{
		cache_flush_data(file_cache, data);
	}
	else
	{
		g_free(data);
	}
}

int file_exists(char const *name)
{
	//dbg(0,"_enter %s\n", name);
	struct stat buf;
	if (!stat(name, &buf))
	{
		return 1;
	}
	return 0;
}

void file_remap_readonly(struct file *f)
{
	//dbg(0,"_enter %s\n", f->name);
#if defined(_WIN32) || defined(__CEGCC__)
#else
	void *begin;
	munmap(f->begin, f->size);
	begin = mmap(f->begin, f->size, PROT_READ, MAP_PRIVATE, f->fd, 0);
	if (f->begin != begin)
	{
		printf("remap failed\n");
	}
#endif
}

void file_unmap(struct file *f)
{
	//dbg(0,"_enter %s\n", f->name);
#if defined(_WIN32) || defined(__CEGCC__)
	mmap_unmap_win32( f->begin, f->map_handle , f->map_file );
#else
	munmap(f->begin, f->size);
#endif
}

#ifndef _MSC_VER
void *
file_opendir(char *dir)
{
	//dbg(0,"_enter %s\n", dir);
	return opendir(dir);
}
#else 
void *
file_opendir(char *dir)
{
	//dbg(0,"_enter %s\n", dir);
	WIN32_FIND_DATAA FindFileData;
	HANDLE hFind = INVALID_HANDLE_VALUE;
#undef UNICODE         // we need FindFirstFileA() which takes an 8-bit c-string
	char* fname=g_alloca(sizeof(char)*(strlen(dir)+4));
	sprintf(fname,"%s\\*",dir);
	hFind = FindFirstFileA(fname, &FindFileData);
	return hFind;
}
#endif

#ifndef _MSC_VER
char *
file_readdir(void *hnd)
{
	//dbg(0,"_enter\n");
	struct dirent *ent;

	ent = readdir(hnd);
	if (!ent)
		return NULL;
	return ent->d_name;
}
#else
char *
file_readdir(void *hnd)
{
	//dbg(0,"_enter\n");
	WIN32_FIND_DATA FindFileData;

	if (FindNextFile(hnd, &FindFileData) )
	{
		return FindFileData.cFileName;
	}
	else
	{
		return NULL;
	}
}
#endif /* _MSC_VER */

#ifndef _MSC_VER
void file_closedir(void *hnd)
{
	//dbg(0,"_enter\n");
	closedir(hnd);
}
#else
void
file_closedir(void *hnd)
{
	//dbg(0,"_enter\n");
	FindClose(hnd);
}
#endif /* _MSC_VER */

struct file *
file_create_caseinsensitive(char *name, struct attr **options)
{
	//dbg(0,"_enter %s\n", name);
	char *dirname = g_alloca(sizeof(char) * (strlen(name) + 1));
	char *filename;
	char *p;
	void *d;
	struct file *ret;

	ret = file_create(name, options);
	if (ret)
	{
		return ret;
	}

	strcpy(dirname, name);
	p = dirname + strlen(name);
	while (p > dirname)
	{
		if (*p == '/')
			break;
		p--;
	}
	*p = 0;
	d = file_opendir(dirname);
	if (d)
	{
		*p++ = '/';
		while ((filename = file_readdir(d)))
		{
			if (!g_strcasecmp(filename, p))
			{
				strcpy(p, filename);
				ret = file_create(dirname, options);
				if (ret)
					break;
			}
		}
		file_closedir(d);
	}
	return ret;
}

void file_destroy(struct file *f)
{
	dbg(0, "_enter %s\n", f->name);

	// print statistics to DEBUG OUT
	//if (file_cache)
	//{
	//cache_stats(file_cache);
	//}
	// print statistics to DEBUG OUT

	if (f->headers)
	{
		g_hash_table_destroy(f->headers);
	}
	switch (f->special)
	{
		case 0:
		case 1:
			close(f->fd);
			break;
	}

	if (f->begin != NULL)
	{
		file_unmap(f);
	}

	g_free(f->buffer);
	g_free(f->name);
	g_free(f);
}

struct file_wordexp
{
	int err;
	char *pattern;
	wordexp_t we;
};

struct file_wordexp *
file_wordexp_new(const char *pattern)
{
	dbg(0, "_enter\n");
	struct file_wordexp *ret=g_new0(struct file_wordexp, 1);
	dbg(0, "fexp 001 p=%p\n", ret);

	ret->pattern = g_strdup(pattern);
	dbg(0, "fexp 002 p=%p p2=%p, str1=%s, str2=%s\n", ret->pattern, pattern, ret->pattern, pattern);
	ret->err = wordexp(pattern, &ret->we, 0);
	dbg(0, "fexp 003\n");
	if (ret->err)
	{
		// dbg(0, "wordexp('%s') returned %d\n", pattern, ret->err);
	}
	dbg(0, "leave\n");
	return ret;
}

int file_wordexp_get_count(struct file_wordexp *wexp)
{
	//dbg(0,"_enter\n");

	if (wexp->err)
	{
		return 1;
	}
	return wexp->we.we_wordc;
}

char **
file_wordexp_get_array(struct file_wordexp *wexp)
{
	//dbg(0,"_enter\n");

	if (wexp->err)
	{
		return &wexp->pattern;
	}
	return wexp->we.we_wordv;
}

void file_wordexp_destroy(struct file_wordexp *wexp)
{
	//dbg(0,"_enter\n");
	if (!wexp->err)
	{
		wordfree(&wexp->we);
	}
	g_free(wexp->pattern);
	g_free(wexp);
}

int file_get_param(struct file *file, struct param_list *param, int count)
{
	//dbg(0,"_enter %s\n", file->name);
	int i = count;
	param_add_string("Filename", file->name, &param, &count);
	param_add_hex("Size", file->size, &param, &count);
	return i - count;
}

int file_version(struct file *file, int mode)
{
	//dbg(0,"_enter %s\n", file->name);
#ifndef HAVE_API_WIN32_BASE
	struct stat st;
	int error;
	if (mode == 3)
	{
		long long size = lseek(file->fd, 0, SEEK_END);
		if (file->begin && file->begin + size > file->mmap_end)
		{
			file->version++;
		}
		else
		{
			file->size = size;
			if (file->begin)
			{
				file->end = file->begin + file->size;
			}
		}
	}
	else
	{
		if (mode == 2)
		{
			error = stat(file->name, &st);
		}
		else
		{
			error = fstat(file->fd, &st);
		}
		if (error || !file->version || file->mtime != st.st_mtime || file->ctime != st.st_ctime)
		{
			file->mtime = st.st_mtime;
			file->ctime = st.st_ctime;
			file->version++;
			// dbg(1, "%s now version %d\n", file->name, file->version);
		}
	}
	return file->version;
#else
	return 0;
#endif
}

void *
file_get_os_handle(struct file *file)
{
	//dbg(0,"_enter %s\n", file->name);
	return GINT_TO_POINTER(file->fd);
}

void file_cache_init(void)
{
#ifdef CACHE_SIZE
	dbg(0,"enter\n");
	dbg(0,"_have 2 CACHE_SIZE=%d\n", CACHE_SIZE);
	dbg(0,"_have 2 cache_size_file=%d\n", cache_size_file);
	if (file_cache == NULL)
	{
		file_cache=cache_new(sizeof(struct file_cache_id), cache_size_file);
		dbg(0,"_file_cache created!\n");
	}
	dbg(0,"leave\n");
#endif
}

void file_init(void)
{
#ifdef CACHE_SIZE
	dbg(0,"enter\n");
	//**// dbg(0,"_have 1 CACHE_SIZE=%d\n", CACHE_SIZE);
	//**// dbg(0,"_have 1 cache_size_file=%d\n", cache_size_file);
	file_name_hash=g_hash_table_new(g_str_hash, g_str_equal);
	// file_cache=cache_new(sizeof(struct file_cache_id), cache_size_file);
	file_cache = NULL; // set later!!
	dbg(0,"leave\n");
#endif
}

