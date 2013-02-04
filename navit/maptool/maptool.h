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
#include <setjmp.h>
#ifdef HAVE_LIBCRYPTO
#include <openssl/md5.h>
#endif

#define MAPTOOL_USE_SQL 1 // if u want to use SQL and all the related stuff
#define MAPTOOL_USE_STRINDEX_COMPRESSION 1 // use street index compression
// #define MAPTOOL_USE_ASYNC_SQL 1 // sql writes are done in async thread
#define USE_STREET_INDEX_COMPRESSION 1
#define MAPTOOL_TRIANGULATE 1 // use polygon to triangle conversion
// #define MAPTOOL_SPLIT_NODE_DB 1 // split up node sql db
// #define MAPTOOL_SPLIT_NODE_DB_MORE 1 // split up node sql db into 2 more files
#define MAPTOOL_SPLIT_NODE_BIT 268435456 // split up at bit X (set value of bit e.g. 64)
#define MAPTOOL_SPLIT_WAYNODE_DB 1 // split up way-node sql db
#define MAPTOOL_SPLIT_WAYNODE_BIT 2 // split up at bit X
#define MAPTOOL_SPLIT_WAYNODE_BIT2 256  // split up at bit X

// cfu hash
#include "cfuhash.h"

#define CFUHASH_BUCKETS_NODES 2097152 
#define CFUHASH_BUCKETS_WAYS 2097152
#define CFUHASH_BUCKETS_OTHER 2097152 // also nodes!?

// sqlite 3
#ifdef MAPTOOL_USE_SQL

#define SQLITE_ENABLE_STAT3
#define SQLITE_OMIT_AUTOVACUUM
#define SQLITE_OMIT_AUTOMATIC_INDEX

#include "sqlite3.h"
#include "sqlite3async.h"

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

struct node_item
{
	int id;
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
	struct rect r;
};

char *osm_tag_value(struct item_bin *ib, char *key);
long long *boundary_relid(struct boundary *b);
GList *process_boundaries(FILE *boundaries, FILE *coords, FILE *ways);
void build_boundary_tree(GList *bl);
GList *boundary_find_matches(GList *bl, struct coord *c);
GList *boundary_find_matches_level(GList *l, struct coord *c, int min_admin_level, int max_admin_level);
GList *boundary_find_matches_single(GList *bl, struct coord *c);
void correct_boundary_ref_point(GList *bl);

/* buffer.c */
struct buffer
{
	int malloced_step;
	long long malloced;
	unsigned char *base;
	long long size;
};

void save_buffer(char *filename, struct buffer *b, long long offset);
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

#define MAX_THREADS 8

extern struct buffer node_buffer[MAX_THREADS];
extern struct buffer waytag_buffer;
extern GHashTable *node_hash[MAX_THREADS];
extern 
cfuhash_table_t *node_hash_cfu[MAX_THREADS];

extern char ib_buffer_array[MAX_THREADS][800000];

extern int processed_nodes, processed_nodes_out, processed_ways, processed_relations, processed_tiles;
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
	char street_name[STREET_INDEX_STREET_NAME_SIZE];
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
typedef long int osmid;

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
int copy_tags_to_ways(FILE *in, FILE *out, FILE *tags_in);
void write_countrydir(struct zip_info *zip_info);
GList* osm_process_towns(FILE *in, FILE *coords, FILE *boundaries, FILE *ways);
void load_countries(void);
void remove_countryfiles(void);
struct country_table * country_from_iso2(char *iso);
void osm_init(FILE*);

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

