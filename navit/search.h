/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2008 Navit Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

#ifndef NAVIT_SEARCH_H
#define NAVIT_SEARCH_H

#ifdef __cplusplus
extern "C"
{
#endif



// from maptool.h !!!!! keep in sync
// from maptool.h !!!!! keep in sync
// ------ STREET INDEX FILE ------

#define STREET_INDEX_STREET_NAME_SIZE 48 // this value + 16 must be a 2^n number!!!

struct streets_index_index_block_start
{
	long long count_of_index_blocks;
}__attribute__ ((packed));

struct streets_index_index_block
{
	char first_letter;
	long long offset;
	long long len;
}__attribute__ ((packed));

struct streets_index_data_block
{
	long long town_id;
	int lat;
	int lon;
	char street_name[STREET_INDEX_STREET_NAME_SIZE];
}__attribute__ ((packed));
// ------ STREET INDEX FILE ------


// ------ TOWN INDEX FILE ------

#define MAX_TOWNNAME_SPLIT 8 // how many parts of a splitstring are we reading?
#define TOWN_INDEX_TOWN_NAME_SIZE 52 // (this value + 12) must be a 2^n number!!!
struct town_index_index_block_start
{
	long long count_of_index_blocks;
}__attribute__ ((packed));

struct town_index_index_block
{
	long long first_id;
	long long offset;
	long long len;
}__attribute__ ((packed));

struct town_index_data_block
{
	long long town_id;
	int country_id;
	char town_name[TOWN_INDEX_TOWN_NAME_SIZE];
}__attribute__ ((packed));

struct town_index_data_block_c
{
	long long town_id;
	int country_id;
	char town_name[TOWN_INDEX_TOWN_NAME_SIZE * MAX_TOWNNAME_SPLIT];
	// char town_name_fold[TOWN_INDEX_TOWN_NAME_SIZE * MAX_TOWNNAME_SPLIT];
};
// ------ TOWN INDEX FILE ------
// from maptool.h !!!!! keep in sync
// from maptool.h !!!!! keep in sync



// from osm.c !!!!! keep in sync
// from osm.c !!!!! keep in sync
// compression level
#define USE_STREET_INDEX_COMPRESSION 1

// compression
#define MINIZ_NO_ZLIB_COMPATIBLE_NAMES

#define MINIZ_NO_STDIO
#define MINIZ_NO_ARCHIVE_APIS
#define MINIZ_NO_TIME
#define MINIZ_NO_ZLIB_APIS
#define MINIZ_NO_MALLOC

#define MINIZ_HEADER_FILE_ONLY
#include "maptool/miniz.c"

typedef unsigned char uint8;
typedef unsigned short uint16;
//typedef unsigned int uint;
// compression

// from osm.c !!!!! keep in sync
// from osm.c !!!!! keep in sync

struct street_index_head
{
	FILE *sif; // street index file handle
	int comp_status; // (streets) 0 -> not initialized, 1 -> initialized, 2 -> closed
	int t_comp_status;  // (towns) 0 -> not initialized, 1 -> initialized, 2 -> closed
	tinfl_status miniz_status;
	tinfl_status t_miniz_status;
	tinfl_decompressor *inflator;
	tinfl_decompressor *t_inflator;
	uint infile_size;
	uint t_infile_size;
	uint infile_remaining;
	uint t_infile_remaining;
	const void *next_in;
	const void *t_next_in;
	size_t avail_in;
	size_t t_avail_in;
	void *next_out;
	void *t_next_out;
	size_t avail_out;
	size_t t_avail_out;
	size_t in_bytes;
	size_t t_in_bytes;
	size_t out_bytes;
	size_t t_out_bytes;
	int data_count;
	int t_data_count;

	struct streets_index_index_block_start si_ibs;
	struct streets_index_index_block *si_ib_mem; // mem pointer, to free the mem
	struct streets_index_index_block *si_ib; // data pointer, to read data
	// struct streets_index_data_block si_db; // street-index data block
	struct streets_index_data_block *si_db_ptr; // street-index data block (pointer)

	struct town_index_index_block_start ti_ibs;
	struct town_index_index_block *ti_ib_mem; // mem pointer, to free the mem
	struct town_index_index_block *ti_ib; // data pointer, to read data
	struct town_index_data_block *ti_db_ptr; // town-index data block (pointer)
};


void street_index_init_compr(struct street_index_head *sih, long long size);
int street_index_read_data(struct street_index_head *sih);
void street_index_close_compr(struct street_index_head *sih);

void town_index_init_compr(struct street_index_head *sih, long long size);
int town_index_read_data(struct street_index_head *sih);
void town_index_close_compr(struct street_index_head *sih);
void town_index_setpos(struct street_index_head *sih, int town_data_block_num);





struct search_list_common
{
	void *parent;
	struct item unique, item;
	int selected;
	struct pcoord *c;
	char *town_name;
	char *district_name;
	char *postal;
	char *postal_mask;
	char *county_name;
};

struct search_list_country
{
	struct search_list_common common;
	char *car;
	char *iso2;
	char *iso3;
	char *name;
	char *flag;
};

struct search_list_town
{
	struct search_list_common common;
	struct item itemt;
	char *county;
};

struct search_list_street
{
	struct search_list_common common;
	char *name;
};

struct search_list_house_number
{
	struct search_list_common common;
	char *house_number;
	int interpolation;
};

struct search_list_result
{
	int id;
	struct pcoord *c;
	struct search_list_country *country;
	struct search_list_town *town;
	struct search_list_street *street;
	struct search_list_house_number *house_number;
};

/* prototypes */
struct attr;
struct mapset;
struct search_list;
struct search_list_result;
struct jni_object;
struct search_list *search_list_new(struct mapset *ms);
void search_list_search(struct search_list *this_, struct attr *search_attr,
		int partial);
char *search_postal_merge(char *mask, char *new_);
char *search_postal_merge_replace(char *mask, char *new_);
struct search_list_common *search_list_select(struct search_list *this_,
		enum attr_type attr_type, int id, int mode);
struct search_list_result *search_list_get_result(struct search_list *this_);
void search_list_destroy(struct search_list *this_);
void search_init(void);
GList * search_by_address(GList *result_list, struct mapset *ms, char *addr,
		int partial, struct jni_object *jni, int search_country_flags,
		char *search_country_string);
void search_full_world(char *addr, int partial, int search_order,
		struct jni_object *jni, struct coord_geo *search_center,
		int search_radius);

/* end of prototypes */
#ifdef __cplusplus
}
#endif

#endif

