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
#include <string.h>
#include <stdlib.h>
#include "maptool.h"
#include "debug.h"

// static char buffer[800000]; --> not thread safe!!
// struct item_bin *item_bin = (struct item_bin *) (void *) ib_buffer_; --> not thread safe

struct node_item *
read_node_item(FILE *in, int local_thread_num)
{
	struct node_item *node_item = (struct node_item *) (void *) ib_buffer_array[local_thread_num];
	if (fread(node_item, sizeof(struct node_item), 1, in) != 1)
		return NULL;
	return node_item;
}

struct item_bin *
read_item(FILE *in, int local_thread_num)
{
	struct item_bin *ib = (struct item_bin *) ib_buffer_array[local_thread_num];
	for (;;)
	{
		switch (item_bin_read(ib, in))
		{
			case 0:
				return NULL;
			case 2:
				// dbg_assert((ib->len + 1) * 4 < sizeof(ib_buffer_array[local_thread_num]));
				bytes_read += (ib->len + 1) * sizeof(int);
				return ib;
			default:
				continue;
		}
	}
}

struct item_bin *
read_item_range(FILE *in, int *min, int *max, int local_thread_num)
{
	struct range r;

	if (fread(&r, sizeof(r), 1, in) != 1)
		return NULL;
	*min = r.min;
	*max = r.max;
	return read_item(in, local_thread_num);
}

struct item_bin *
init_item(enum item_type type, int local_thread_num)
{
	struct item_bin *ib = (struct item_bin *) ib_buffer_array[local_thread_num];

	item_bin_init(ib, type);
	return ib;
}


