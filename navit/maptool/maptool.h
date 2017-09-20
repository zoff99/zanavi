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
#include <glib.h>
#include "config.h"
#include "coord.h"
#include "item.h"
#include "attr.h"
#include "trans_lat_lon_geo.h"
#include <setjmp.h>
#ifdef HAVE_LIBCRYPTO
#include <openssl/md5.h>
#endif

#define MAX_THREADS 8

#define MIN_SLICE_SIZE_BYTES 32768

#define NO_GTYPES_ 1

#define NAVIT_TRANS_LAT_LON_GEO_NOFUNCS 1

// --------------------------------------------
// --
// which binfilemap version this maptool will generate
#define GENERATE_BINFILE_MAPVERSION 4
// --
// --------------------------------------------


// --------------------------------------------
// --
// if the turn angle is greater than this (in degrees) in bicycle mode, then speak a turn command
#define ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE 30
// --
// --------------------------------------------


extern int MAPTOOL_QUICK_RUN; // only set this to 1 for testing !!!!!!


#define MAPTOOL_SQL_INPUT_TOO_SMALL 0 // only set this to 1 for testing very small input files!!!!!!

#define MAPTOOL_USE_SQL 1 // if u want to use SQL and all the related stuff
#define MAPTOOL_USE_STRINDEX_COMPRESSION 1 // use street index compression
// #define MAPTOOL_USE_ASYNC_SQL 1 // sql writes are done in async thread
#define USE_STREET_INDEX_COMPRESSION 1
#define MAPTOOL_TRIANGULATE 1 // use polygon to triangle conversion
// #define MAPTOOL_SPLIT_NODE_DB 1 // split up node sql db
// #define MAPTOOL_SPLIT_NODE_DB_MORE 1 // split up node sql db into 2 more files
#define MAPTOOL_SPLIT_NODE_BIT 268435456 // split up at bit X (set value of bit e.g. 64)
//#define MAPTOOL_SPLIT_WAYNODE_DB 1 // split up way-node sql db
#define MAPTOOL_SPLIT_WAYNODE_BIT 2 // split up at bit X
#define MAPTOOL_SPLIT_WAYNODE_BIT2 256  // split up at bit X

// cfu hash
#include "cfuhash.h"

#define CFUHASH_BUCKETS_NODES 8388608 // 2100000 // 4194304 // 8388608 // for planet and 2GB ram, we get about 80million nodes to cache 
#define CFUHASH_BUCKETS_WAYS 65536 // normally unused now
#define CFUHASH_BUCKETS_OTHER 65536 // also nodes!? ---> unused now

// sqlite 3
#ifdef MAPTOOL_USE_SQL

#define SQLITE_ENABLE_STAT3
#define SQLITE_ENABLE_STAT4
#define SQLITE_OMIT_AUTOVACUUM
#define SQLITE_OMIT_AUTOMATIC_INDEX
#define SQLITE_ENABLE_API_ARMOR
#define SQLITE_DEFAULT_TEMP_CACHE_SIZE 1000
#define SQLITE_DEFAULT_WORKER_THREADS 8
#define SQLITE_MAX_WORKER_THREADS 8
//#define SQLITE_DEFAULT_WORKER_THREADS 0
//#define SQLITE_MAX_WORKER_THREADS 0


#include "sqlite3.h"
/* #include "sqlite3async.h" */

extern sqlite3 *sql_handle;
extern sqlite3 *sql_handle002a;
extern sqlite3 *sql_handle003a;
extern sqlite3 *sql_handle002b;
extern sqlite3 *sql_handle003b;
extern sqlite3 *sql_handle004;
extern sqlite3 *sql_handle005;
extern sqlite3 *sql_handle006;
extern sqlite3 *sql_handle007;
#else
extern void *sql_handle;
extern void *sql_handle002a;
extern void *sql_handle003a;
extern void *sql_handle002b;
extern void *sql_handle003b;
extern void *sql_handle004;
extern void *sql_handle005;
extern void *sql_handle006;
extern void *sql_handle007;
#endif

int sql_counter;
int sql_counter2;
int sql_counter3;
int sql_counter4;
#define MAX_ROWS_WO_COMMIT 140000 // nodes
#define MAX_ROWS_WO_COMMIT_2 1000 // town to streets (2)
#define MAX_ROWS_WO_COMMIT_2a 100 // town to streets (1)
#define MAX_ROWS_WO_COMMIT_2b 10000 // town to streets (with boundaries)
#define MAX_ROWS_WO_COMMIT_3 90000 // ways
#define MAX_ROWS_WO_COMMIT_4 220000 // waynodes
#define MAX_ROWS_WO_COMMIT_5 6000 // towns to country
// sqlite 3

#ifdef HAVE_API_WIN32_BASE
#define LONGLONG_FMT "%I64d"
#else
#define LONGLONG_FMT "%lld"
#endif

#define sq(x) ((double)(x)*(x))

#define BUFFER_SIZE 1280

#define debug_tile(x) 0
#define debug_itembin(x) 0

// ---------EXCEPTIONS--------
char stack[SIGSTKSZ];
struct sigaction sa;
stack_t ss;
void catch_signal(int param);

jmp_buf ex_buf__; // global var!

// #define TRY do{ jmp_buf ex_buf__; switch( setjmp(ex_buf__) ){ case 0: while(1){
#define TRY do{ switch( setjmp(ex_buf__) ){ case 0: while(1){
#define CATCH(x) break; case x:
#define FINALLY break; } default:
#define ETRY } }while(0)
#define THROW(x) longjmp(ex_buf__, x)

#define MAPTOOL_00001_EXCEPTION (1)
#define MAPTOOL_00002_EXCEPTION (2)
#define MAPTOOL_00003_EXCEPTION (3)
// ---------EXCEPTIONS--------


#ifdef MAPTOOL_USE_SQL
sqlite3_stmt *stmt_nodea;
sqlite3_stmt *stmt_node__2a;
sqlite3_stmt *stmt_nodeb;
sqlite3_stmt *stmt_node__2b;
sqlite3_stmt *stmt_nodei;
sqlite3_stmt *stmt_way;
sqlite3_stmt *stmt_way2;
sqlite3_stmt *stmt_way_node;
sqlite3_stmt *stmt_way_node__2;
sqlite3_stmt *stmt_way_nodeb;
sqlite3_stmt *stmt_way_node__2b;
sqlite3_stmt *stmt_way3;
sqlite3_stmt *stmt_way3a;
sqlite3_stmt *stmt_way3b;
sqlite3_stmt *stmt_town_sel001;
sqlite3_stmt *stmt_sel001;
sqlite3_stmt *stmt_sel001__2;
sqlite3_stmt *stmt_sel001b;
sqlite3_stmt *stmt_sel001__2b;
sqlite3_stmt *stmt_sel0012;
sqlite3_stmt *stmt_sel0012__2;
sqlite3_stmt *stmt_sel0012b;
sqlite3_stmt *stmt_sel0012__2b;
sqlite3_stmt *stmt_sel002a;
sqlite3_stmt *stmt_sel002__2a;
sqlite3_stmt *stmt_sel002b;
sqlite3_stmt *stmt_sel002__2b;
sqlite3_stmt *stmt_town_sel002;
sqlite3_stmt *stmt_town_sel005;
sqlite3_stmt *stmt_town_sel006;
sqlite3_stmt *stmt_town_sel007;
sqlite3_stmt *stmt_town_sel008;
sqlite3_stmt *stmt_town;
sqlite3_stmt *stmt_sel003;
sqlite3_stmt *stmt_sel003u;
sqlite3_stmt *stmt_sel004;
sqlite3_stmt *stmt_bd_001;
sqlite3_stmt *stmt_bd_002;
sqlite3_stmt *stmt_bd_003;
sqlite3_stmt *stmt_bd_004;
sqlite3_stmt *stmt_bd_005;
sqlite3_stmt *stmt_sel0012_tt[MAX_THREADS];
sqlite3_stmt *stmt_sel0012__2_tt[MAX_THREADS];
sqlite3_stmt *stmt_sel0012b_tt[MAX_THREADS];
sqlite3_stmt *stmt_sel0012__2b_tt[MAX_THREADS];
#else

typedef void sqlite3_stmt;

void *stmt_nodea;
void *stmt_node__2a;
void *stmt_nodeb;
void *stmt_node__2b;
void *stmt_nodei;
void *stmt_way;
void *stmt_way2;
void *stmt_way_node;
void *stmt_way_node__2;
void *stmt_way_nodeb;
void *stmt_way_node__2b;
void *stmt_way3;
void *stmt_way3a;
void *stmt_way3b;
void *stmt_town_sel001;
void *stmt_sel001;
void *stmt_sel001__2;
void *stmt_sel001b;
void *stmt_sel001__2b;
void *stmt_sel0012;
void *stmt_sel0012__2;
void *stmt_sel0012b;
void *stmt_sel0012__2b;
void *stmt_sel002a;
void *stmt_sel002__2a;
void *stmt_sel002b;
void *stmt_sel002__2b;
void *stmt_town_sel002;
void *stmt_town_sel005;
void *stmt_town_sel006;
void *stmt_town_sel007;
void *stmt_town_sel008;
void *stmt_town;
void *stmt_sel003;
void *stmt_sel003u;
void *stmt_sel004;
void *stmt_bd_001;
void *stmt_bd_002;
void *stmt_bd_003;
void *stmt_bd_004;
void *stmt_bd_005;
void *stmt_sel0012_tt[MAX_THREADS];
void *stmt_sel0012__2_tt[MAX_THREADS];
void *stmt_sel0012b_tt[MAX_THREADS];
void *stmt_sel0012__2b_tt[MAX_THREADS];


#define SQLITE_STATIC 1
#define SQLITE_ROW 2
#define SQLITE_DONE 3
int sqlite3_reset(void*);
int sqlite3_step(void*);
int sqlite3_exec(void*,void*, void*, void*,void*);
int sqlite3_bind_int64(void*, int, long);
int sqlite3_bind_int(void*, int, int);
int sqlite3_bind_double(void*, int, double);
long sqlite3_column_int64(void*, int);
double sqlite3_column_double(void*, int);
int sqlite3_column_int(void*, int);
int sqlite3_bind_text(void*, int, char*, int, int);

#endif

#define TOWN_BY_BOUNDARY_SIZE_FACTOR 1000000
#define TOWN_ADMIN_LEVEL_CORR_BASE 99999
#define TOWN_ADMIN_LEVEL_START 8

long long ways_processed_count;
extern int global_keep_tmpfiles;
extern int global_use_runtime_db;
extern char *runtime_db_filename_with_path;


void fprintf_(FILE *f, const char *fmt, ...);


















// quick_hash.c -------------------




//-----------------------------------------------------------------------------
// MurmurHash2 was written by Austin Appleby, and is placed in the public
// domain. The author hereby disclaims copyright to this source code.

#ifndef _MURMURHASH2_H_
#define _MURMURHASH2_H_

//-----------------------------------------------------------------------------
// Platform-specific functions and macros

// typedef unsigned char uint8_t;
// typedef unsigned long uint32_t;
// typedef unsigned __int64 uint64_t;

#include <stdint.h>

//-----------------------------------------------------------------------------

//uint32_t MurmurHash2        ( const void * key, int len, uint32_t seed );
//uint64_t MurmurHash64A      ( const void * key, int len, uint64_t seed );
uint64_t MurmurHash64B      ( const void * key, int len, uint64_t seed );
//uint32_t MurmurHash2A       ( const void * key, int len, uint32_t seed );
//uint32_t MurmurHashNeutral2 ( const void * key, int len, uint32_t seed );
//uint32_t MurmurHashAligned2 ( const void * key, int len, uint32_t seed );

unsigned int MurmurHash1Aligned ( const void * key, int len, unsigned int seed );

//-----------------------------------------------------------------------------


struct quickhash_table* quick_hash_init(int num_buckets);
void quick_hash_destroy(struct quickhash_table* table);
int quick_hash_lookup(struct quickhash_table* table, long long key);
void quick_hash_add_entry(struct quickhash_table* table, long long key, int value_ptr);
void* quick_hash_print_stats(struct quickhash_table* table);

#define HASH_SIMPLE 1 // for simple hash algo

#endif // _MURMURHASH2_H_


// quick_hash.c -------------------








// 24 MBytes for item buffer
#define MAX_ITEMBIN_BYTES_ 24000000










struct rect
{
	struct coord l, h;
};

struct rect_lat_lon
{
	double lu_lat;
	double lu_lon;
	double rl_lat;
	double rl_lon;
};

struct node_lat_lon
{
	double lat;
	double lon;
	int valid; // 0 -> invalid, 1 -> valid
};

struct tile_data
{
	char buffer[1024];
	int tile_depth;
	struct rect item_bbox;
	struct rect tile_bbox;
};

struct tile_parameter
{
	int min;
	int max;
	int overlap;
};

struct tile_info
{
	int write;
	int maxlen;
	char *suffix;
	GList **tiles_list;
	FILE *tilesdir_out;
};

extern struct tile_head
{
	int num_subtiles;
	int total_size;
	char *name;
	char *zip_data;
	int total_size_used;
	int zipnum;
	int process;
	struct tile_head *next;
	// char subtiles[0];
}*tile_head_root;

struct item_bin
{
	int len;
	enum item_type type;
	int clen;
};

struct attr_bin
{
	int len;
	enum attr_type type;
};

struct item_bin_sink_func
{
	int (*func)(struct item_bin_sink_func *func, struct item_bin *ib, struct tile_data *tile_data);
	void *priv_data[8];
};

struct item_bin_sink
{
	void *priv_data[8];
	GList *sink_funcs;
};

typedef long long osmid;

struct node_item
{
	osmid id;
	char ref_node;
	char ref_way;
	char ref_ref;
	char dummy;
	struct coord c;
};

struct way_tag
{
	long long way_id;
	int tag_id;
};

struct relation_member
{
	int type;
	long long id;
	char *role;
};

struct zip_info;

struct country_table;


/* boundaries.c */
struct boundary
{
	struct item_bin *ib;
	struct country_table *country;
	char *iso2;
	GList *segments, *sorted_segments;
	GList *children;
	struct rect r; // bbox
};

struct boundary_manual
{
//	struct item_bin *ib;
	struct coord *c;
	int coord_count;
	int countryid;
	struct country_table *country;
	struct rect r; // bbox
	long long town_id;
};

GList *boundary_list_inner_manual;


char *osm_tag_value(struct item_bin *ib, char *key);
long long *boundary_relid(struct boundary *b);
GList *process_boundaries(FILE *boundaries, FILE *coords, FILE *ways);
void build_boundary_tree(GList *bl, GList *man_bl);
GList *boundary_find_matches(GList *bl, struct coord *c);
GList *boundary_find_matches_level(GList *l, struct coord *c, int min_admin_level, int max_admin_level);
GList *boundary_find_matches_single(GList *bl, struct coord *c);
void correct_boundary_ref_point(GList *bl);
void free_boundaries(GList *l);
GList *load_manual_country_borders();
void save_manual_country_borders(GList *borders);

/* buffer.c */
struct buffer
{
	int malloced_step;
	long long malloced;
	unsigned char *base;
	long long size;
};

void save_buffer(char *filename, struct buffer *b, long long offset);
void free_buffer(char *filename, struct buffer *b);
void load_buffer(char *filename, struct buffer *b, long long offset, long long size);

/* ch.c */

void ch_generate_tiles(char *map_suffix, char *suffix, FILE *tilesdir_out, struct zip_info *zip_info);
void ch_assemble_map(char *map_suffix, char *suffix, struct zip_info *zip_info);

/* coastline.c */

void process_coastlines(FILE *in, FILE *out);

/* geom.c */

enum geom_poly_segment_type
{
	geom_poly_segment_type_none, geom_poly_segment_type_way_inner, geom_poly_segment_type_way_outer, geom_poly_segment_type_way_left_side, geom_poly_segment_type_way_right_side, geom_poly_segment_type_way_unknown,

};

struct geom_poly_segment
{
	enum geom_poly_segment_type type;
	struct coord *first, *last;
};

void geom_coord_copy(struct coord *from, struct coord *to, int count, int reverse);
void geom_coord_revert(struct coord *c, int count);
long long geom_poly_area(struct coord *c, int count);
GList *geom_poly_segments_insert(GList *list, struct geom_poly_segment *first, struct geom_poly_segment *second, struct geom_poly_segment *third);
void geom_poly_segment_destroy(struct geom_poly_segment *seg);
GList *geom_poly_segments_remove(GList *list, struct geom_poly_segment *seg);
int geom_poly_segment_compatible(struct geom_poly_segment *s1, struct geom_poly_segment *s2, int dir);
GList *geom_poly_segments_sort(GList *in, enum geom_poly_segment_type type);
struct geom_poly_segment *item_bin_to_poly_segment(struct item_bin *ib, int type);
int geom_poly_segments_point_inside(GList *in, struct coord *c);
void clip_line(struct item_bin *ib, struct rect *r, struct tile_parameter *param, struct item_bin_sink *out);
void clip_polygon(struct item_bin *ib, struct rect *r, struct tile_parameter *param, struct item_bin_sink *out);
int item_bin_is_closed_poly(struct item_bin *ib);

/* itembin.c */

int item_bin_read(struct item_bin *ib, FILE *in);
void item_bin_set_type(struct item_bin *ib, enum item_type type);
void item_bin_init(struct item_bin *ib, enum item_type type);
void item_bin_add_coord(struct item_bin *ib, struct coord *c, int count);
void item_bin_add_coord_reverse(struct item_bin *ib, struct coord *c, int count);
void item_bin_bbox(struct item_bin *ib, struct rect *r);
void item_bin_copy_coord(struct item_bin *ib, struct item_bin *from, int dir);
void item_bin_add_coord_rect(struct item_bin *ib, struct rect *r);
int attr_bin_write_data(struct attr_bin *ab, enum attr_type type, void *data, int size);
int attr_bin_write_attr(struct attr_bin *ab, struct attr *attr);
void item_bin_add_attr_data(struct item_bin *ib, enum attr_type type, void *data, int size);
void item_bin_add_attr(struct item_bin *ib, struct attr *attr);
void item_bin_add_attr_int(struct item_bin *ib, enum attr_type type, int val);
void *item_bin_get_attr(struct item_bin *ib, enum attr_type type, void *last);
struct attr_bin * item_bin_get_attr_bin_last(struct item_bin *ib);
void item_bin_add_attr_longlong(struct item_bin *ib, enum attr_type type, long long val);
void item_bin_add_attr_string(struct item_bin *ib, enum attr_type type, char *str);
void item_bin_add_attr_range(struct item_bin *ib, enum attr_type type, short min, short max);
void item_bin_remove_attr(struct item_bin *ib, void *ptr);
void item_bin_write(struct item_bin *ib, FILE *out);
void item_bin_write_xml(struct item_bin *ib, char *filename);
struct item_bin *item_bin_dup(struct item_bin *ib);
void item_bin_write_range(struct item_bin *ib, FILE *out, int min, int max);
void item_bin_write_clipped(struct item_bin *ib, struct tile_parameter *param, struct item_bin_sink *out);
void item_bin_dump(struct item_bin *ib, FILE *out);
void dump_itembin(struct item_bin *ib);
void item_bin_set_type_by_population(struct item_bin *ib, int population);
void item_bin_write_match(struct item_bin *ib, enum attr_type type, enum attr_type match, FILE *out);
void item_bin_town_write_match(struct item_bin *ib, enum attr_type type, enum attr_type match, FILE *out);
int item_bin_sort_file(char *in_file, char *out_file, struct rect *r, int *size);

/* itembin_buffer.c */
struct node_item *read_node_item(FILE *in, int local_thread_num);
struct item_bin *read_item(FILE *in, int local_thread_num);
struct item_bin *read_item_range(FILE *in, int *min, int *max, int local_thread_num);
struct item_bin *init_item(enum item_type type, int local_thread_num);

/* maptool.c */

extern long long slice_size;
extern int attr_debug_level;
extern char *suffix;
extern int ignore_unkown;
// extern GHashTable *dedupe_ways_hash;
extern int phase;
extern int slices;

extern FILE *ways_ref_file;
extern FILE *ways_ref_file_thread[MAX_THREADS];

extern struct buffer node_buffer[MAX_THREADS];
extern struct buffer waytag_buffer;
extern GHashTable *node_hash[MAX_THREADS];

#if 0
extern cfuhash_table_t *node_hash_cfu[MAX_THREADS];
#endif
extern struct quickhash_table *node_hash_cfu[MAX_THREADS];

extern char *ib_buffer_array[MAX_THREADS]; // [2400000]; // 24 MB max size for 1 item

extern int processed_nodes_out, processed_tiles;
extern long long processed_nodes;
extern long long processed_ways;
extern long long processed_relations;

extern long long processed_nodes_sum;
extern long long processed_ways_sum;
extern long long processed_relations_sum;

extern struct item_bin *item_bin;
extern int bytes_read;
extern int overlap;
extern int experimental;
extern int use_global_fixed_country_id;
extern int global_fixed_country_id;
extern int unknown_country;
void sig_alrm(int sig);
void sig_alrm_end(void);
extern int border_only_map;
extern int coastline_only_map;
extern int border_only_map_as_xml;
extern int verbose_mode;
extern long long dummy_town_id;
extern char* manual_country_border_dir;

extern int global_less_verbose;


void convert_to_human_time(long long seconds, char *outstring);
void convert_to_human_bytes(long long bytes, char *outstring);
void convert_to_human_bytes2(long long bytes, char *outstring);

struct phase_001_thread_var
{
	int thread_num;
	int count;
	FILE* file1;
};

// ------ STREET INDEX FILE ------

#define REF_X 1073741834 // lat
#define REF_Y 240000000  // lon

#define STREET_INDEX_STREET_NAME_SIZE 48 // (this value + 16) must be a 2^n number!!!
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
	char street_type;
	char street_name[STREET_INDEX_STREET_NAME_SIZE - 1];
}__attribute__ ((packed));
// ------ STREET INDEX FILE ------


// ------ TOWN INDEX FILE ------

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
// ------ TOWN INDEX FILE ------


/* misc.c */
extern struct rect world_bbox;

void bbox_extend(struct coord *c, struct rect *r);
void bbox(struct coord *c, int count, struct rect *r);
int contains_bbox(int xl, int yl, int xh, int yh, struct rect *r);
int bbox_contains_coord(struct rect *r, struct coord *c);
int bbox_contains_bbox(struct rect *out, struct rect *in);
long long bbox_area(struct rect const *r);
void phase1_map(GList *maps, FILE *out_ways, FILE *out_nodes);
void dump(FILE *in);
int phase4(FILE **in, int in_count, int with_range, char *suffix, FILE *tilesdir_out, struct zip_info *zip_info);
int phase5(FILE **in, FILE **references, int in_count, int with_range, char *suffix, struct zip_info *zip_info);
void process_binfile(FILE *in, FILE *out);
void add_aux_tiles(char *name, struct zip_info *info);
void cat(FILE *in, FILE *out);

/* osm.c */

extern long long seekpos_waynode[MAX_THREADS];
extern long long last_seekpos_waynode[MAX_THREADS];
extern osmid last_seek_wayid[MAX_THREADS];

struct maptool_osm
{
	FILE *boundaries;
	FILE *turn_restrictions;
	FILE *nodes;
	FILE *ways;
	FILE *line2poi;
	FILE *poly2poi;
	FILE *towns;
	FILE *relations_riverbank;
	FILE *ways_with_coords;
};

struct country_table *country_from_countryid(int id);
void append_pre_resolved_ways(FILE *out, struct maptool_osm *osm2);
void osm_warning(char *type, long long id, int cont, char *fmt, ...);
void osm_add_tag(char *k, char *v);
void osm_add_node(osmid id, double lat, double lon);
void osm_add_way(osmid id);
void osm_add_relation(osmid id);
void osm_end_relation(struct maptool_osm *osm);
void osm_add_member(int type, osmid ref, char *role);
void osm_end_way(struct maptool_osm *osm);
void osm_end_node(struct maptool_osm *osm);
void osm_add_nd(osmid ref);
long long item_bin_get_id(struct item_bin *ib);
void flush_nodes(int final, int local_thread_num);
void sort_countries(int keep_tmpfiles);
void process_turn_restrictions(FILE *in, FILE *coords, FILE *ways, FILE *ways_index, FILE *out);
void clear_node_item_buffer(void);
void ref_ways(FILE *in, int local_thread_num);
void resolve_ways(FILE *in, FILE *out);
long long item_bin_get_nodeid(struct item_bin *ib);
long long item_bin_get_wayid(struct item_bin *ib);
long long item_bin_get_relationid(struct item_bin *ib);
FILE *resolve_ways_file(FILE *in, char *suffix, char *filename);
void process_way2poi(FILE *in, FILE *out, int type);
int map_find_intersections(FILE *in, FILE *out, FILE *out_index, FILE *out_graph, FILE *out_coastline, int final);
int map_find_intersections__quick__for__debug(FILE *in, FILE *out, FILE *out_index, FILE *out_graph, FILE *out_coastline, int final);
int copy_tags_to_ways(FILE *in, FILE *out, FILE *tags_in);
void write_countrydir(struct zip_info *zip_info);
GList* osm_process_towns(FILE *in, FILE *coords, FILE *boundaries, FILE *ways, GList *bl_manual);
void load_countries(void);
void remove_countryfiles(void);
struct country_table * country_from_iso2(char *iso);
void osm_init(FILE*);
osmid get_waynode_num(osmid way_id, int coord_num, int local_thread_num);
void map_find_housenumbers_interpolation(FILE *in, FILE *out);
void add_point_as_way_to_db(char *label, osmid id, int waytype, double lat, double lon);
int transform_from_geo_lat(double lat);
int transform_from_geo_lon(double lon);
double transform_to_geo_lat(int y);
double transform_to_geo_lon(int x);
void save_manual_country_borders_to_db(GList *man_borders);
int string_endswith2(const char* ending, const char* instring);
struct node_lat_lon* get_first_coord_of_boundary(struct item_bin *item_bin_3, struct relation_member *memb);
void purge_unused_towns();

extern struct item_bin *item_bin_2;

/* osm_psql.c */
int map_collect_data_osm_db(char *dbstr, struct maptool_osm *osm);

/* osm_protobuf.c */
//int map_collect_data_osm_protobuf(FILE *in, struct maptool_osm *osm);
//int osm_protobufdb_load(FILE *in, char *dir);

/* osm_relations.c */
struct relations * relations_new(void);
struct relations_func *relations_func_new(void(*func)(void *func_priv, void *relation_priv, struct item_bin *member, void *member_priv), void *func_priv);
void relations_add_func(struct relations *rel, struct relations_func *func, void *relation_priv, void *member_priv, int type, osmid id);
void relations_process(struct relations *rel, FILE *nodes, FILE *ways, FILE *relations);
void relations_destroy(struct relations *rel);

/* osm_xml.c */
int osm_xml_get_attribute(char *xml, char *attribute, char *buffer, int buffer_size);
void osm_xml_decode_entities(char *buffer);
int map_collect_data_osm(FILE *in, struct maptool_osm *osm);

/* sourcesink.c */

struct item_bin_sink *item_bin_sink_new(void);
struct item_bin_sink_func *item_bin_sink_func_new(int(*func)(struct item_bin_sink_func *func, struct item_bin *ib, struct tile_data *tile_data));
void item_bin_sink_func_destroy(struct item_bin_sink_func *func);
void item_bin_sink_add_func(struct item_bin_sink *sink, struct item_bin_sink_func *func);
void item_bin_sink_destroy(struct item_bin_sink *sink);
int item_bin_write_to_sink(struct item_bin *ib, struct item_bin_sink *sink, struct tile_data *tile_data);
struct item_bin_sink *file_reader_new(FILE *in, int limit, int offset);
int file_reader_finish(struct item_bin_sink *sink);
int file_writer_process(struct item_bin_sink_func *func, struct item_bin *ib, struct tile_data *tile_data);
struct item_bin_sink_func *file_writer_new(FILE *out);
int file_writer_finish(struct item_bin_sink_func *file_writer);
int tile_collector_process(struct item_bin_sink_func *tile_collector, struct item_bin *ib, struct tile_data *tile_data);
struct item_bin_sink_func *tile_collector_new(struct item_bin_sink *out);

/* tempfile.c */

char *tempfile_name(char *suffix, char *name);
FILE *tempfile(char *suffix, char *name, int mode);
void tempfile_unlink(char *suffix, char *name);
void tempfile_rename(char *suffix, char *from, char *to);
void tempfile_copyrename(char *suffix, char *from, char *to);

/* tile.c */
extern GHashTable *tile_hash, *tile_hash2;

struct aux_tile
{
	char *name;
	char *filename;
	int size;
};

extern GList *aux_tile_list;

int tile(struct rect *r, char *suffix, char *ret, int max, int overlap, struct rect *tr);
void tile_bbox(char *tile, struct rect *r, int overlap);
int tile_len(char *tile);
void tile_write_item_to_tile(struct tile_info *info, struct item_bin *ib, FILE *reference, char *name);
void tile_write_item_minmax(struct tile_info *info, struct item_bin *ib, FILE *reference, int min, int max);
int add_aux_tile(struct zip_info *zip_info, char *name, char *filename, int size);
int write_aux_tiles(struct zip_info *zip_info);
int create_tile_hash(void);
void write_tilesdir(struct tile_info *info, struct zip_info *zip_info, FILE *out);
void merge_tiles(struct tile_info *info);
struct attr map_information_attrs[32];
void index_init(struct zip_info *info, int version);
void index_submap_add(struct tile_info *info, struct tile_head *th);

/* zip.c */
void write_zipmember(struct zip_info *zip_info, char *name, int filelen, char *data, int data_size);
void zip_write_index(struct zip_info *info);
int zip_write_directory(struct zip_info *info);
struct zip_info *zip_new(void);
void zip_set_md5(struct zip_info *info, int on);
int zip_get_md5(struct zip_info *info, unsigned char *out);
void zip_set_zip64(struct zip_info *info, int on);
void zip_set_compression_level(struct zip_info *info, int level);
void zip_set_maxnamelen(struct zip_info *info, int max);
int zip_get_maxnamelen(struct zip_info *info);
int zip_add_member(struct zip_info *info);
int zip_set_timestamp(struct zip_info *info, char *timestamp);
int zip_set_password(struct zip_info *info, char *password);
void zip_open(struct zip_info *info, char *out, char *dir, char *index);
FILE *zip_get_index(struct zip_info *info);
int zip_get_zipnum(struct zip_info *info);
void zip_set_zipnum(struct zip_info *info, int num);
void zip_close(struct zip_info *info);
void zip_destroy(struct zip_info *info);


// color codes for colour attribute in OS;

unsigned int color_int_value_from_string(char* color_str);

struct css_color
{
    char *col_name;
    unsigned int col_value;
};

/*
unsinged int color_int;

unsinged int b=color_int & 0xff;
unsinged int g=(color_int >> 8) & 0xff;
unsinged int r=(color_int >> 16) & 0xff;

b=b << 8;
g=g << 8;
r=r << 8;
*/


#define _css_aliceblue 0xf0f8ff
#define _css_antiquewhite 0xfaebd7
#define _css_aqua 0x00ffff
#define _css_aquamarine 0x7fffd4
#define _css_azure 0xf0ffff
#define _css_beige 0xf5f5dc
#define _css_bisque 0xffe4c4
#define _css_black 0x000000
#define _css_blanchedalmond 0xffebcd
#define _css_blue 0x0000ff
#define _css_blueviolet 0x8a2be2
#define _css_brown 0xa52a2a
#define _css_burlywood 0xdeb887
#define _css_cadetblue 0x5f9ea0
#define _css_chartreuse 0x7fff00
#define _css_chocolate 0xd2691e
#define _css_coral 0xff7f50
#define _css_cornflowerblue 0x6495ed
#define _css_cornsilk 0xfff8dc
#define _css_crimson 0xdc143c
#define _css_cyan 0x00ffff
#define _css_darkblue 0x00008b
#define _css_darkcyan 0x008b8b
#define _css_darkgoldenrod 0xb8860b
#define _css_darkgray 0xa9a9a9
#define _css_darkgreen 0x006400
#define _css_darkkhaki 0xbdb76b
#define _css_darkmagenta 0x8b008b
#define _css_darkolivegreen 0x556b2f
#define _css_darkorange 0xff8c00
#define _css_darkorchid 0x9932cc
#define _css_darkred 0x8b0000
#define _css_darksalmon 0xe9967a
#define _css_darkseagreen 0x8fbc8f
#define _css_darkslateblue 0x483d8b
#define _css_darkslategray 0x2f4f4f
#define _css_darkturquoise 0x00ced1
#define _css_darkviolet 0x9400d3
#define _css_deeppink 0xff1493
#define _css_deepskyblue 0x00bfff
#define _css_dimgray 0x696969
#define _css_dodgerblue 0x1e90ff
#define _css_firebrick 0xb22222
#define _css_floralwhite 0xfffaf0
#define _css_forestgreen 0x228b22
#define _css_fuchsia 0xff00ff
#define _css_gainsboro 0xdcdcdc
#define _css_ghostwhite 0xf8f8ff
#define _css_gold 0xffd700
#define _css_goldenrod 0xdaa520
#define _css_gray 0x808080
#define _css_green 0x008000
#define _css_greenyellow 0xadff2f
#define _css_honeydew 0xf0fff0
#define _css_hotpink 0xff69b4
#define _css_indianred 0xcd5c5c
#define _css_indigo 0x4b0082
#define _css_ivory 0xfffff0
#define _css_khaki 0xf0e68c
#define _css_lavender 0xe6e6fa
#define _css_lavenderblush 0xfff0f5
#define _css_lawngreen 0x7cfc00
#define _css_lemonchiffon 0xfffacd
#define _css_lightblue 0xadd8e6
#define _css_lightcoral 0xf08080
#define _css_lightcyan 0xe0ffff
#define _css_lightgoldenrodyellow 0xfafad2
#define _css_lightgrey 0xd3d3d3
#define _css_lightgreen 0x90ee90
#define _css_lightpink 0xffb6c1
#define _css_lightsalmon 0xffa07a
#define _css_lightseagreen 0x20b2aa
#define _css_lightskyblue 0x87cefa
#define _css_lightslategray 0x778899
#define _css_lightsteelblue 0xb0c4de
#define _css_lightyellow 0xffffe0
#define _css_lime 0x00ff00
#define _css_limegreen 0x32cd32
#define _css_linen 0xfaf0e6
#define _css_magenta 0xff00ff
#define _css_maroon 0x800000
#define _css_mediumaquamarine 0x66cdaa
#define _css_mediumblue 0x0000cd
#define _css_mediumorchid 0xba55d3
#define _css_mediumpurple 0x9370d8
#define _css_mediumseagreen 0x3cb371
#define _css_mediumslateblue 0x7b68ee
#define _css_mediumspringgreen 0x00fa9a
#define _css_mediumturquoise 0x48d1cc
#define _css_mediumvioletred 0xc71585
#define _css_midnightblue 0x191970
#define _css_mintcream 0xf5fffa
#define _css_mistyrose 0xffe4e1
#define _css_moccasin 0xffe4b5
#define _css_navajowhite 0xffdead
#define _css_navy 0x000080
#define _css_oldlace 0xfdf5e6
#define _css_olive 0x808000
#define _css_olivedrab 0x6b8e23
#define _css_orange 0xffa500
#define _css_orangered 0xff4500
#define _css_orchid 0xda70d6
#define _css_palegoldenrod 0xeee8aa
#define _css_palegreen 0x98fb98
#define _css_paleturquoise 0xafeeee
#define _css_palevioletred 0xd87093
#define _css_papayawhip 0xffefd5
#define _css_peachpuff 0xffdab9
#define _css_peru 0xcd853f
#define _css_pink 0xffc0cb
#define _css_plum 0xdda0dd
#define _css_powderblue 0xb0e0e6
#define _css_purple 0x800080
#define _css_red 0xff0000
#define _css_rosybrown 0xbc8f8f
#define _css_royalblue 0x4169e1
#define _css_saddlebrown 0x8b4513
#define _css_salmon 0xfa8072
#define _css_sandybrown 0xf4a460
#define _css_seagreen 0x2e8b57
#define _css_seashell 0xfff5ee
#define _css_sienna 0xa0522d
#define _css_silver 0xc0c0c0
#define _css_skyblue 0x87ceeb
#define _css_slateblue 0x6a5acd
#define _css_slategray 0x708090
#define _css_snow 0xfffafa
#define _css_springgreen 0x00ff7f
#define _css_steelblue 0x4682b4
#define _css_tan 0xd2b48c
#define _css_teal 0x008080
#define _css_thistle 0xd8bfd8
#define _css_tomato 0xff6347
#define _css_turquoise 0x40e0d0
#define _css_violet 0xee82ee
#define _css_wheat 0xf5deb3
#define _css_white 0xffffff
#define _css_whitesmoke 0xf5f5f5
#define _css_yellow 0xffff00
#define _css_yellowgreen 0x9acd32



// #define dbg(level,...) { fprintf(stderr , __VA_ARGS__); }



