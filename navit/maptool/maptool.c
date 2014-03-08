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
#include <time.h>
#include <sys/resource.h>
#include <glib.h>
#include <assert.h>
#include <string.h>
#include <signal.h>
#include <stdio.h>
#include <math.h>
#include <getopt.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <zlib.h>

// pthreads
#include <pthread.h>
// pthreads

#include "version_maptool.h"

#include "file.h"
#include "item.h"
#include "map.h"
#include "zipfile.h"
#include "main.h"
#include "config.h"
#include "linguistics.h"
#include "plugin.h"
#include "util.h"
#include "maptool.h"

void *sql_thread(void *ptr);

time_t start_tt, end_tt;
double diff_tt;
double diff2_tt;

time_t global_start_tt, global_end_tt;
double global_diff_tt;

long long slice_size = 50 * 1024 * 1024; // default to 50 MByte
int attr_debug_level = 1;
int ignore_unkown = 0;
int border_only_map = 0;
int border_only_map_as_xml = 0;
int coastline_only_map = 0;
// GHashTable *dedupe_ways_hash;
int phase;
int slices;
int unknown_country;
int doway2poi = 0;
char ch_suffix[] = "r"; /* Used to make compiler happy due to Bug 35903 in gcc */
int experimental = 0;
int use_global_fixed_country_id = 0;
int verbose_mode = 0;
int global_fixed_country_id = 999;
long long dummy_town_id = -2;

int cur_thread_num = 0;
int threads = 1;
int max_threads = MAX_THREADS;
int thread_is_working_[MAX_THREADS];
const char* sqlite_db_dir_extra = "./db/";
char *sqlite_db_dir = "./";
int sqlite_temp_dir = 0;

FILE *ways_ref_file;
FILE *ways_ref_file_thread[MAX_THREADS];

char ib_buffer_array[MAX_THREADS][2400000];

// char ib_buffer_2[800000]; // --> this would be a independent buffer
// char *ib_buffer_2; // = ib_buffer_array[0];

struct buffer node_buffer[MAX_THREADS]; // buffer for nodes // ARRAY (max max_threads)
struct buffer waytag_buffer = { 64 * 1024 * 1024, }; // buffer for relations // extend in 64MBytes steps

GHashTable *node_hash[MAX_THREADS]; // ARRAY (max max_threads)
cfuhash_table_t *node_hash_cfu[MAX_THREADS];

long long seekpos_waynode[MAX_THREADS];
long long last_seekpos_waynode[MAX_THREADS];
osmid last_seek_wayid[MAX_THREADS];

struct item_bin *item_bin_2 = (struct item_bin *) &ib_buffer_array[0];

pthread_t sqlite_thread001;

#ifdef MAPTOOL_USE_SQL
sqlite3 *sql_handle;
sqlite3 *sql_handle002a;
sqlite3 *sql_handle003a;
sqlite3 *sql_handle002b;
sqlite3 *sql_handle003b;
sqlite3 *sql_handle004;
sqlite3 *sql_handle005;
sqlite3 *sql_handle006;
sqlite3 *sql_handle007;
#else
void *sql_handle;
void *sql_handle002a;
void *sql_handle003a;
void *sql_handle002b;
void *sql_handle003b;
void *sql_handle004;
void *sql_handle005;
void *sql_handle006;
void *sql_handle007;
#endif

int processed_nodes, processed_nodes_out, processed_ways, processed_relations, processed_tiles = 0;

int overlap = 1;

int bytes_read;
int verbose_mem = 0;

void sig_alrm(int sig)
{
#ifndef _WIN32
	signal(SIGALRM, sig_alrm);
	alarm(60);
#endif
	//if (verbose_mode)
	//	fprintf(stderr, "PROGRESS%d: Processed %d nodes (%d out) %d ways %d relations %d tiles\n", phase, processed_nodes, processed_nodes_out, processed_ways, processed_relations, processed_tiles);

	if (verbose_mem)
	{
		print_mem();
	}
}

void sig_alrm_end(void)
{
#ifndef _WIN32
	alarm(0);
#endif
}

print_mem()
{
	struct rusage usage;
	int ret;
	char outstring2[200];
	long i1, i2, i3;
	FILE *f1;
	char c[10];

	long all_bytes = (long) sysconf(_SC_PHYS_PAGES) * (long) sysconf(_SC_PAGE_SIZE);
	fprintf(stderr, "#+MEM+#all:%ld\n", all_bytes);
	convert_to_human_bytes2((long long) all_bytes, outstring2);
	fprintf(stderr, "#+MEM+#H#all:       %s\n", outstring2);

	long free_bytes = (long) sysconf(_SC_AVPHYS_PAGES) * (long) sysconf(_SC_PAGE_SIZE);
	fprintf(stderr, "#+MEM+#free:%ld\n", free_bytes);
	convert_to_human_bytes2((long long) free_bytes, outstring2);
	fprintf(stderr, "#+MEM+#H#free:      %s\n", outstring2);

#if 0
	ret = getrusage(RUSAGE_SELF, &usage);
	if (ret == 0)
	{
		// long   ru_maxrss;        /* maximum resident set size */
		// long   ru_ixrss;         /* integral shared memory size */
		// long   ru_idrss;         /* integral unshared data size */
		// long   ru_isrss;         /* integral unshared stack size */
		fprintf(stderr, "#+MEM+#used1:%ld\n", usage.ru_maxrss * (long) 1024);
		fprintf(stderr, "#+MEM+#used2:%ld\n", usage.ru_ixrss * (long) 1024);
		fprintf(stderr, "#+MEM+#used3:%ld\n", usage.ru_idrss * (long) 1024);
		fprintf(stderr, "#+MEM+#used4:%ld\n", usage.ru_isrss * (long) 1024);
		convert_to_human_bytes2((long long) (usage.ru_maxrss * (long) 1024), outstring2);
		fprintf(stderr, "#+MEM+#H#used1:     %s\n", outstring2);
	}
#endif

	int ps = getpagesize();
	//fprintf(stderr, "ps=%d\n", ps);

	f1 = fopen("/proc/self/statm", "r");
	fscanf(f1, "%ld\t%ld\t%ld\t", &i1, &i2, &i3);
	fclose(f1);

	fprintf(stderr, "#+MEM+#VIRT:%ld\n", (long) (i1 * ps));
	fprintf(stderr, "#+MEM+#RES :%ld\n", (long) (i2 * ps));

	convert_to_human_bytes2((long long) (i1 * ps), outstring2);
	fprintf(stderr, "#+MEM+#H#VIRT:      %s\n", outstring2);
	convert_to_human_bytes2((long long) (i2 * ps), outstring2);
	fprintf(stderr, "#+MEM+#H#RES :      %s\n", outstring2);
}

static struct plugins *plugins;

static void add_plugin(char *path)
{
	struct attr **attrs;

	if (!plugins)
	{
		// file_init(); // must define some CACHE thingy first!!
		plugins = plugins_new();
	}
	attrs = (struct attr*[])
			{	&(struct attr)
				{	attr_path,
					{	path}},NULL};
			plugin_new(&(struct attr)
					{	attr_plugins,.u.plugins=plugins}, attrs);
		}

static void maptool_init(FILE* rule_file)
{
	if (plugins)
	{
		plugins_init(plugins);
	}
	osm_init(rule_file);
}

void my_sleep(int delay)
{
	clock_t t1 = clock(), t2;
	double elapsed;

	do
	{
		t2 = clock();
		elapsed = ((double) (t2 - t1)) / CLOCKS_PER_SEC;

	}
	while ((elapsed) < delay);
}

void convert_to_human_time(long long seconds, char *outstring)
{
	if (seconds < 1)
	{
		sprintf(outstring, "0s");
		return;
	}

	int days = (int) seconds / 86400;
	int hours = (int) (seconds / 3600) - (days * 24);
	int mins = (int) (seconds / 60) - (days * 1440) - (hours * 60);
	int secs = (int) seconds % 60;

	if (days > 0)
	{
		sprintf(outstring, "%dd %dh %dm %ds", days, hours, mins, secs);
	}
	else if (hours > 0)
	{
		sprintf(outstring, "%dh %dm %ds", hours, mins, secs);
	}
	else
	{
		sprintf(outstring, "%dm %ds", mins, secs);
	}
}

void convert_to_human_bytes(long long bytes, char *outstring)
{
	if (bytes < 1)
	{
		sprintf(outstring, "0b");
		return;
	}

	unsigned long g_ = bytes / (1024 * 1024 * 1024);
	int m_ = (bytes / (1024 * 1024)) - (g_ * 1024);
	int k_ = (bytes / 1024) - (g_ * (1024 * 1024)) - (m_ * 1024);
	int b_ = bytes % 1024;

	if (g_ > 0)
	{
		sprintf(outstring, "%luGb", g_);
	}
	else if (m_ > 0)
	{
		sprintf(outstring, "%dMb", m_);
	}
	else if (k_ > 0)
	{
		sprintf(outstring, "%dkb", k_);
	}
	else
	{
		sprintf(outstring, "%db", b_);
	}
}

void convert_to_human_bytes2(long long bytes, char *outstring)
{
	if (bytes < 1)
	{
		sprintf(outstring, "0b");
		return;
	}

	int m_ = (bytes / (1024 * 1024));
	int k_ = (bytes / 1024) - (m_ * 1024);
	int b_ = bytes % 1024;

	if (m_ > 0)
	{
		sprintf(outstring, "%dMb", m_);
	}
	else if (k_ > 0)
	{
		sprintf(outstring, "%dkb", k_);
	}
	else
	{
		sprintf(outstring, "%db", b_);
	}
}

void sql_db_open()
{
#ifdef MAPTOOL_USE_SQL

	int retval;

	fprintf(stderr, "SQL: -- INIT --\n");

#ifdef MAPTOOL_USE_ASYNC_SQL
	retval = sqlite3async_initialize(NULL, 1);
	if (retval)
	{
		fprintf(stderr, "SQL ASYNC module init failed\n");
	}

	fprintf(stderr, "SQL: -- INIT 1 --\n");

	retval = sqlite3async_control(SQLITEASYNC_DELAY, 0);
	if (retval)
	{
		fprintf(stderr, "SQL ASYNC module config 1 failed\n");
	}

	retval = sqlite3async_control(SQLITEASYNC_HALT, SQLITEASYNC_HALT_NEVER);
	if (retval)
	{
		fprintf(stderr, "SQL ASYNC module config 3 failed\n");
	}

	retval = sqlite3async_control(SQLITEASYNC_LOCKFILES, 0);
	if (retval)
	{
		fprintf(stderr, "SQL ASYNC module config 2 failed\n");
	}

	fprintf(stderr, "SQL: -- INIT 2 --\n");
	pthread_create(&sqlite_thread001, NULL, sql_thread, NULL);
	fprintf(stderr, "SQL: -- INIT 3 --\n");
#endif

	sql_handle = NULL;
	retval = sqlite3_open(g_strdup_printf("%stemp_data.db",sqlite_db_dir), &sql_handle);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection failed\n");
		return -1;
	}

	sql_handle002a = NULL;

	retval = sqlite3_open(g_strdup_printf("%stemp_data002a.db",sqlite_db_dir), &sql_handle002a);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection002 failed\n");
		return -1;
	}

#ifdef MAPTOOL_SPLIT_NODE_DB_MORE
	sql_handle003a = NULL;

	retval = sqlite3_open(g_strdup_printf("%stemp_data003a.db",sqlite_db_dir), &sql_handle003a);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection003 failed\n");
		return -1;
	}
#else
	sql_handle003a = sql_handle002a;
#endif

	sql_handle002b = NULL;

	retval = sqlite3_open(g_strdup_printf("%stemp_data002b.db",sqlite_db_dir), &sql_handle002b);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection002 failed\n");
		return -1;
	}

#ifdef MAPTOOL_SPLIT_NODE_DB_MORE
	sql_handle003b = NULL;

	retval = sqlite3_open(g_strdup_printf("%stemp_data003b.db",sqlite_db_dir), &sql_handle003b);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection003 failed\n");
		return -1;
	}
#else
	sql_handle003b = sql_handle002b;
#endif

	sql_handle004 = NULL;

	retval = sqlite3_open(g_strdup_printf("%stemp_data004.db",sqlite_db_dir), &sql_handle004);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection004 failed\n");
		return -1;
	}

	sql_handle005 = NULL;

	retval = sqlite3_open(g_strdup_printf("%stemp_data005.db",sqlite_db_dir), &sql_handle005);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection005 failed\n");
		return -1;
	}

	sql_handle006 = NULL;
	retval = sqlite3_open(g_strdup_printf("%stemp_data006.db",sqlite_db_dir), &sql_handle006);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection006 failed\n");
		return -1;
	}

	sql_handle007 = NULL;
	retval = sqlite3_open(g_strdup_printf("%stemp_data007.db",sqlite_db_dir), &sql_handle007);
	// If connection failed, sql_handle returns NULL
	if (retval)
	{
		fprintf(stderr, "SQL Database connection007 failed\n");
		return -1;
	}

	// char* errorMessage2;
	//sqlite3_exec(sql_handle004, "PRAGMA page_size = 4096", NULL, NULL, &errorMessage2);
	//sqlite3_exec(sql_handle005, "PRAGMA page_size = 4096", NULL, NULL, &errorMessage2);
	//sqlite3_exec(sql_handle006, "PRAGMA page_size = 4096", NULL, NULL, &errorMessage2);
	//sqlite3_exec(sql_handle007, "PRAGMA page_size = 4096", NULL, NULL, &errorMessage2);

	//sqlite3_exec(sql_handle004, "PRAGMA max_page_count = 2073741823", NULL, NULL, &errorMessage2);
	//sqlite3_exec(sql_handle005, "PRAGMA max_page_count = 2073741823", NULL, NULL, &errorMessage2);
	//sqlite3_exec(sql_handle006, "PRAGMA max_page_count = 2073741823", NULL, NULL, &errorMessage2);
	//sqlite3_exec(sql_handle007, "PRAGMA max_page_count = 2073741823", NULL, NULL, &errorMessage2);


	char* errorMessage;

	if (sqlite_temp_dir == 1)
	{
		sqlite3_exec(sql_handle, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);

		sqlite3_exec(sql_handle004, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle005, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle006, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle007, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);

		sqlite3_exec(sql_handle002a, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle002b, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle003a, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle003b, g_strdup_printf("PRAGMA temp_store_directory = '%s/'", sqlite_db_dir), NULL, NULL, &errorMessage);
	}
	else
	{
		sqlite3_exec(sql_handle, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);

		sqlite3_exec(sql_handle004, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle005, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle006, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle007, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);

		sqlite3_exec(sql_handle002a, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle002b, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle003a, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);
		sqlite3_exec(sql_handle003b, "PRAGMA temp_store_directory = '.'", NULL, NULL, &errorMessage);
	}

	sqlite3_busy_timeout(sql_handle, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle002a, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle003a, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle002b, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle003b, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle004, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle005, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle006, 50000); // wait for 5 seconds
	sqlite3_busy_timeout(sql_handle007, 50000); // wait for 5 seconds

	fprintf(stderr, "SQL Connection successful\n");
#endif
}

void sql_db_close()
{
#ifdef MAPTOOL_USE_SQL
	int retval;

	sql_counter = 0;
	sql_counter2 = 0;
	sql_counter3 = 0;
	sql_counter4 = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle002a, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle003a, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle002b, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle003b, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle004, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle005, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle006, "COMMIT", 0, 0, 0);
	sqlite3_exec(sql_handle007, "COMMIT", 0, 0, 0);

	retval = sqlite3_finalize(stmt_nodea);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_node__2a);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_nodeb);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_node__2b);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_nodei);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way);
	fprintf(stderr, "fin:%d\n", retval);
	//retval = sqlite3_finalize(stmt_way2);
	//fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way_node);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way_node__2);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way_nodeb);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way_node__2b);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_sel001);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel001__2);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel001b);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel001__2b);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_sel0012);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel0012__2);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel0012b);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel0012__2b);
	fprintf(stderr, "fin:%d\n", retval);

	int jj;
	for (jj = 0; jj < max_threads; jj++)
	{
		sqlite3_finalize(stmt_sel0012_tt[jj]);
		sqlite3_finalize(stmt_sel0012__2_tt[jj]);
		sqlite3_finalize(stmt_sel0012b_tt[jj]);
		sqlite3_finalize(stmt_sel0012__2b_tt[jj]);
	}

	retval = sqlite3_finalize(stmt_sel002a);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel002__2a);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_sel002b);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel002__2b);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_town);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_town_sel001);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way3);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way3a);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_way3b);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_town_sel002);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_town_sel005);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_town_sel006);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_town_sel007);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_town_sel008);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_bd_001);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_bd_002);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_bd_003);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_bd_004);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_bd_005);
	fprintf(stderr, "fin:%d\n", retval);

	retval = sqlite3_finalize(stmt_sel003);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel003u);
	fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_sel004);
	fprintf(stderr, "fin:%d\n", retval);

	// set temp_store back to default
	// sqlite3_exec(sql_handle, "PRAGMA temp_store=DEFAULT", NULL, NULL, &errorMessage);

	// close sql file
	retval = sqlite3_close(sql_handle);
	fprintf(stderr, "close:%d\n", retval);

	retval = sqlite3_close(sql_handle002a);
	fprintf(stderr, "close002a:%d\n", retval);
	retval = sqlite3_close(sql_handle003a);
	fprintf(stderr, "close003a:%d\n", retval);

	retval = sqlite3_close(sql_handle002b);
	fprintf(stderr, "close002b:%d\n", retval);
	retval = sqlite3_close(sql_handle003b);
	fprintf(stderr, "close003b:%d\n", retval);

	retval = sqlite3_close(sql_handle004);
	fprintf(stderr, "close004:%d\n", retval);
	retval = sqlite3_close(sql_handle005);
	fprintf(stderr, "close005:%d\n", retval);
	retval = sqlite3_close(sql_handle006);
	fprintf(stderr, "close006:%d\n", retval);
	retval = sqlite3_close(sql_handle007);
	fprintf(stderr, "close007:%d\n", retval);

#ifdef MAPTOOL_USE_ASYNC_SQL

	retval = sqlite3async_control(SQLITEASYNC_HALT, SQLITEASYNC_HALT_IDLE);
	if (retval)
	{
		fprintf(stderr, "SQL ASYNC module config 3a failed\n");
	}

	sqlite3async_shutdown();

	//int st = pthread_cancel(sqlite_thread001);
	//if (st != 0)
	//{
	//	fprintf(stderr, "cancel async:%d\n", st);
	//}

	fprintf(stderr, "close async 002:%d\n", retval);
	if (sqlite_thread001)
	{
		fprintf(stderr, "close async 002.1:%d\n", retval);
		pthread_join(sqlite_thread001, NULL);
	}
	fprintf(stderr, "close async 003:%d\n", retval);

	fprintf(stderr, "close async 001:%d\n", retval);
#endif

#endif
}

void sql_create_index001()
{
#ifdef MAPTOOL_USE_SQL
	int retval;
	char* errorMessage;

	sqlite3_exec(sql_handle004, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA cache_size = -16000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA cache_size = -16000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA cache_size = -16000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA cache_size = -16000", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle004, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle005, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle006, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle007, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);


	//sqlite3_exec(sql_handle, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA cache_size = -9000", NULL, NULL, &errorMessage);

	char create_table5[100] = "CREATE index wn1 on way_node(way_id, s)";
	retval = sqlite3_exec(sql_handle004, create_table5, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle005, create_table5, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle006, create_table5, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle007, create_table5, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);

	/*
	 char create_table5a[100] = "alter table way_node rename to way_node2";
	 char create_table5b[100] = "CREATE TABLE way_node (way_id integer,node_id integer,s integer)";
	 char create_table5c[100] = "CREATE index way_node_i01 on way_node (way_id)";
	 char create_table5d[100] = "insert into way_node select * from way_node2";
	 char create_table5e[100] = "drop table way_node2";

	 retval = sqlite3_exec(sql_handle004, create_table5a, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle004, create_table5b, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle004, create_table5c, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle004, create_table5d, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle004, create_table5e, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);

	 retval = sqlite3_exec(sql_handle005, create_table5a, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle005, create_table5b, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle005, create_table5c, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle005, create_table5d, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle005, create_table5e, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);

	 retval = sqlite3_exec(sql_handle006, create_table5a, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle006, create_table5b, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle006, create_table5c, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle006, create_table5d, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle006, create_table5e, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);

	 retval = sqlite3_exec(sql_handle007, create_table5a, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle007, create_table5b, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle007, create_table5c, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle007, create_table5d, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle007, create_table5e, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 */

	//char create_table10[100] = "CREATE index w2 on way(lat)";
	//retval = sqlite3_exec(sql_handle, create_table10, 0, 0, 0);
	//fprintf(stderr, "index:%d\n", retval);

	//char create_table11[100] = "CREATE index w3 on way(lon)";
	//retval = sqlite3_exec(sql_handle, create_table11, 0, 0, 0);
	//fprintf(stderr, "index:%d\n", retval);

	//char create_table4[100] = "CREATE index w1 on way(town_id)";
	//retval = sqlite3_exec(sql_handle,create_table4,0,0,0);
	//fprintf(stderr, "index:%d\n", retval);

	char create_table11[100] = "create index w5 on way(town_id,lat,lon)";
	retval = sqlite3_exec(sql_handle,create_table11,0,0,0);
	fprintf(stderr, "index:%d\n", retval);

	char create_table12[100] = "CREATE index w4 on way(id)";
	retval = sqlite3_exec(sql_handle, create_table12, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);

	//char create_table13[100] = "create index w6 on way(ind)";
	//retval = sqlite3_exec(sql_handle,create_table13,0,0,0);
	//fprintf(stderr, "index:%d\n", retval);

	char create_table14[100] = "create index w8 on way(name_fold,town_id,ind)";
	retval = sqlite3_exec(sql_handle,create_table14,0,0,0);
	fprintf(stderr, "index:%d\n", retval);

	//char create_table15[100] = "create index w9 on way(name_fold)";
	//retval = sqlite3_exec(sql_handle,create_table15,0,0,0);
	//fprintf(stderr, "index:%d\n", retval);

	char create_table16[100] = "create index w10 on way(town_id,ind)";
	retval = sqlite3_exec(sql_handle,create_table16,0,0,0);
	fprintf(stderr, "index:%d\n", retval);

	char create_table17[100] = "create index w11 on way(ind,name_fold)";
	retval = sqlite3_exec(sql_handle,create_table17,0,0,0);
	fprintf(stderr, "index:%d\n", retval);

	char create_table18[100] = "create index w12 on way(ind,name_fold_idx)";
	retval = sqlite3_exec(sql_handle,create_table18,0,0,0);
	fprintf(stderr, "index:%d\n", retval);

	retval = sqlite3_exec(sql_handle, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle004, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle005, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle006, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle007, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);

	sqlite3_exec(sql_handle004, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle004, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);

#endif
}

void sql_create_index002()
{
#ifdef MAPTOOL_USE_SQL
	int retval;
	char* errorMessage;

	sqlite3_exec(sql_handle004, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);

	char create_table9[100] = "CREATE index twn1 on town(country_id)";
	retval = sqlite3_exec(sql_handle, create_table9, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);
	char create_table12[100] = "CREATE index twn2 on town(size)";
	retval = sqlite3_exec(sql_handle, create_table12, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);
	char create_table13[100] = "CREATE index twn3 on town(lat)";
	retval = sqlite3_exec(sql_handle, create_table13, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);
	char create_table14[100] = "CREATE index twn4 on town(lon)";
	retval = sqlite3_exec(sql_handle, create_table14, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);
	char create_table15[100] = "CREATE index twn5 on town(done)";
	retval = sqlite3_exec(sql_handle, create_table15, 0, 0, 0);
	fprintf(stderr, "index:%d\n", retval);

	retval = sqlite3_exec(sql_handle, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle004, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle005, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle006, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle007, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);

	sqlite3_exec(sql_handle004, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);

#endif
}

void sql_create_index003()
{
#ifdef MAPTOOL_USE_SQL
	int retval;
	char* errorMessage;

	/*
	 sqlite3_exec(sql_handle002a, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle002a, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003a, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003a, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle002b, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle002b, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003b, "PRAGMA temp_store=FILE", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003b, "PRAGMA cache_size = -10000", NULL, NULL, &errorMessage);
	 */

	// char create_table9[100] = "CREATE index nd01 on node(id)";
	/*
	 retval = sqlite3_exec(sql_handle002a, create_table9, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle003a, create_table9, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle002b, create_table9, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 retval = sqlite3_exec(sql_handle003b, create_table9, 0, 0, 0);
	 fprintf(stderr, "index:%d\n", retval);
	 */

	retval = sqlite3_exec(sql_handle002a, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle003a, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle002b, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);
	retval = sqlite3_exec(sql_handle003b, "analyze;", NULL, NULL, &errorMessage);
	fprintf(stderr, "index:%d\n", retval);

	/*
	 sqlite3_exec(sql_handle002a, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle002a, "PRAGMA cache_size = -30000", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003a, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003a, "PRAGMA cache_size = -30000", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle002b, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle002b, "PRAGMA cache_size = -30000", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003b, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	 sqlite3_exec(sql_handle003b, "PRAGMA cache_size = -30000", NULL, NULL, &errorMessage);
	 */

#endif
}

void sql_db_init(int startup)
{
#ifdef MAPTOOL_USE_SQL
	int retval;

	if (startup == 1)
	{
		// Create the SQL query for creating a table
		//char create_table1[200] = "CREATE TABLE IF NOT EXISTS way (id integer primary key asc, town_id integer, lat real,lon real, name text)";
		char create_table1[200] = "CREATE TABLE IF NOT EXISTS way (id integer, town_id integer, ind integer, lat real,lon real, name text, name_fold text, name_fold_idx text)";
		retval = sqlite3_exec(sql_handle, create_table1, 0, 0, 0);

		char create_table2[200] = "CREATE TABLE IF NOT EXISTS way_node(way_id integer,node_id integer, s integer, seekpos1)";
		retval = sqlite3_exec(sql_handle004, create_table2, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);
		retval = sqlite3_exec(sql_handle005, create_table2, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);
		retval = sqlite3_exec(sql_handle006, create_table2, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);
		retval = sqlite3_exec(sql_handle007, create_table2, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);

		char create_table15[200] = "CREATE TABLE IF NOT EXISTS boundary(rel_id integer primary key asc,admin_level integer, done integer, parent_rel_id integer, country_id integer, lat real, lon real, name text)";
		retval = sqlite3_exec(sql_handle, create_table15, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);

		char create_table3[200] = "CREATE TABLE IF NOT EXISTS node(id integer primary key asc,lat real,lon real)";
		//char create_table3[200] = "CREATE TABLE IF NOT EXISTS node(id integer ,lat real,lon real)";
		retval = sqlite3_exec(sql_handle002a, create_table3, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);
		retval = sqlite3_exec(sql_handle003a, create_table3, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);
		retval = sqlite3_exec(sql_handle002b, create_table3, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);
		retval = sqlite3_exec(sql_handle003b, create_table3, 0, 0, 0);
		fprintf(stderr, "table:%d\n", retval);

		//char create_table7[200] = "CREATE TABLE IF NOT EXISTS nodei(id integer primary key asc,lat integer,lon integer)";
		//retval = sqlite3_exec(sql_handle002, create_table7, 0, 0, 0);

		char create_table8[200] = "CREATE TABLE IF NOT EXISTS town(id integer primary key asc, country_id integer,size integer, lat real, lon real, postal text, name text, done integer, border_id integer)";
		retval = sqlite3_exec(sql_handle, create_table8, 0, 0, 0);

		char create_table88[200] = "CREATE TABLE IF NOT EXISTS town2(id integer primary key asc, border_id integer, admin_level integer)";
		retval = sqlite3_exec(sql_handle, create_table88, 0, 0, 0);

		char create_table4[100] = "CREATE index w1 on way(town_id)";
		//retval = sqlite3_exec(sql_handle,create_table4,0,0,0);

		char create_table5[100] = "CREATE index wn1 on way_node(way_id)";
		//retval = sqlite3_exec(sql_handle004, create_table5, 0, 0, 0);
		//retval = sqlite3_exec(sql_handle005, create_table5, 0, 0, 0);
		//retval = sqlite3_exec(sql_handle006, create_table5, 0, 0, 0);
		//retval = sqlite3_exec(sql_handle007, create_table5, 0, 0, 0);

		//char create_table6[100] = "CREATE index n1 on node(id)";
		//retval = sqlite3_exec(sql_handle002,create_table6,0,0,0);

		char create_table10[100] = "CREATE index w2 on way(lat)";
		//retval = sqlite3_exec(sql_handle, create_table10, 0, 0, 0);
		char create_table11[100] = "CREATE index w3 on way(lon)";
		//retval = sqlite3_exec(sql_handle, create_table11, 0, 0, 0);


		char create_table9[100] = "CREATE index twn1 on town(country_id)";
		//retval = sqlite3_exec(sql_handle, create_table9, 0, 0, 0);
		char create_table12[100] = "CREATE index twn2 on town(size)";
		//retval = sqlite3_exec(sql_handle, create_table12, 0, 0, 0);
		char create_table13[100] = "CREATE index twn1 on town(lat)";
		//retval = sqlite3_exec(sql_handle, create_table13, 0, 0, 0);
		char create_table14[100] = "CREATE index twn1 on town(lon)";
		//retval = sqlite3_exec(sql_handle, create_table14, 0, 0, 0);
	}

	char* errorMessage;

	// WAYS and TOWNS -------
	sqlite3_exec(sql_handle, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	// WAYS and TOWNS -------


	// NODE -------
	sqlite3_exec(sql_handle002a, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002a, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle002a, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle002a, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002a, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle002a, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002a, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002a, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle002b, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002b, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle002b, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle002b, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002b, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle002b, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002b, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle002b, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	// NODE -------


	// NODE -------
	sqlite3_exec(sql_handle003a, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003a, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle003a, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle003a, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003a, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle003a, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003a, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003a, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle003b, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003b, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle003b, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle003b, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003b, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle003b, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003b, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle003b, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	// NODE -------


	// WAY NODE -------
	sqlite3_exec(sql_handle004, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle004, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle004, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle004, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle004, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	// WAY NODE -------


	// WAY NODE -------
	sqlite3_exec(sql_handle005, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle005, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle005, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle005, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle005, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	// WAY NODE -------


	// WAY NODE -------
	sqlite3_exec(sql_handle006, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle006, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle006, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle006, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle006, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	// WAY NODE -------


	// WAY NODE -------
	sqlite3_exec(sql_handle007, "PRAGMA synchronous=OFF", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA count_changes=OFF", NULL, NULL, &errorMessage);

	//sqlite3_exec(sql_handle007, "PRAGMA journal_mode=MEMORY", NULL, NULL, &errorMessage);
	//sqlite3_exec(sql_handle007, "PRAGMA journal_mode=PERSIST", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA journal_mode=OFF", NULL, NULL, &errorMessage);

	sqlite3_exec(sql_handle007, "PRAGMA temp_store=MEMORY", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA locking_mode=EXCLUSIVE", NULL, NULL, &errorMessage);
	sqlite3_exec(sql_handle007, "PRAGMA cache_size = -20000", NULL, NULL, &errorMessage);
	// WAY NODE -------


	retval = sqlite3_prepare_v2(sql_handle002a, "INSERT INTO node (id, lat, lon) VALUES (?,?,?);", -1, &stmt_nodea, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle003a, "INSERT INTO node (id, lat, lon) VALUES (?,?,?);", -1, &stmt_node__2a, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle002b, "INSERT INTO node (id, lat, lon) VALUES (?,?,?);", -1, &stmt_nodeb, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle003b, "INSERT INTO node (id, lat, lon) VALUES (?,?,?);", -1, &stmt_node__2b, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	//retval = sqlite3_prepare_v2(sql_handle002, "INSERT INTO nodei (id, lat, lon) VALUES (?,?,?);", -1, &stmt_nodei, NULL);
	//fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle, "INSERT INTO way (id, name, town_id, lat, lon, name_fold, ind, name_fold_idx) VALUES (?,?,?,?,?,?,0,?);", -1, &stmt_way, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle004, "INSERT INTO way_node (way_id, node_id,s,seekpos1) VALUES (?,?,?,?);", -1, &stmt_way_node, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle005, "INSERT INTO way_node (way_id, node_id,s,seekpos1) VALUES (?,?,?,?);", -1, &stmt_way_node__2, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle006, "INSERT INTO way_node (way_id, node_id,s,seekpos1) VALUES (?,?,?,?);", -1, &stmt_way_nodeb, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle007, "INSERT INTO way_node (way_id, node_id,s,seekpos1) VALUES (?,?,?,?);", -1, &stmt_way_node__2b, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle004, "select node_id from way_node where way_id=? order by s;", -1, &stmt_sel001, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle005, "select node_id from way_node where way_id=? order by s;", -1, &stmt_sel001__2, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle006, "select node_id from way_node where way_id=? order by s;", -1, &stmt_sel001b, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle007, "select node_id from way_node where way_id=? order by s;", -1, &stmt_sel001__2b, NULL);
	fprintf(stderr, "prep:%d\n", retval);


	int jj;
	for (jj = 0; jj < max_threads; jj++)
	{
		sqlite3_prepare_v2(sql_handle004, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012_tt[jj], NULL);
		sqlite3_prepare_v2(sql_handle005, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012__2_tt[jj], NULL);
		sqlite3_prepare_v2(sql_handle006, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012b_tt[jj], NULL);
		sqlite3_prepare_v2(sql_handle007, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012__2b_tt[jj], NULL);
	}

	retval = sqlite3_prepare_v2(sql_handle004, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle005, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012__2, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle006, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012b, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle007, "select node_id,seekpos1 from way_node where way_id=? and s=?;", -1, &stmt_sel0012__2b, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle002a, "select lat,lon from node where id=?;", -1, &stmt_sel002a, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle003a, "select lat,lon from node where id=?;", -1, &stmt_sel002__2a, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle002b, "select lat,lon from node where id=?;", -1, &stmt_sel002b, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle003b, "select lat,lon from node where id=?;", -1, &stmt_sel002__2b, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "INSERT INTO town (done, id, country_id, name, size, postal, lat, lon, border_id) VALUES (0,?,?,?,?,?,?,?,?);", -1, &stmt_town, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	//retval = sqlite3_prepare_v2(sql_handle, "UPDATE way set lat=?, lon=? WHERE id=?;", -1, &stmt_way2, NULL);
	//fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select id,size,lat,lon,name,border_id from town where done = 0 order by size desc;", -1, &stmt_town_sel001, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle, "update way set town_id = ? where lat >= ? and lat <= ? and lon >= ? and lon <= ? and town_id = -1;", -1, &stmt_way3, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select id, lat, lon from way where lat >= ? and lat <= ? and lon >= ? and lon <= ? and town_id = -1;", -1, &stmt_way3a, NULL);
	fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle, "update way set town_id = ? where id = ?;", -1, &stmt_way3b, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select count(*) from town where done = 0;", -1, &stmt_town_sel002, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "update town set done = 1 where id = ?;", -1, &stmt_town_sel007, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "insert into town2 (border_id, id, admin_level) values (?,?,?);", -1, &stmt_town_sel008, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select id, country_id, name, border_id from town order by id;", -1, &stmt_town_sel005, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select count(id) from town;", -1, &stmt_town_sel006, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "insert into boundary (done, rel_id, admin_level, lat, lon, name) values (0, ?,?,?,?,?);", -1, &stmt_bd_001, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select rel_id, admin_level, lat, lon, name from boundary where done = 0;", -1, &stmt_bd_002, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "update boundary set done = 1, parent_rel_id = ? where rel_id = ?;", -1, &stmt_bd_003, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "update boundary set lat = ?, lon = ? where rel_id = ?;", -1, &stmt_bd_004, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select rel_id, parent_rel_id, name from boundary where rel_id = ?;", -1, &stmt_bd_005, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select t.id,w.lat, w.lon, w.name, t.country_id from way w left outer join town t on w.town_id=t.id where w.ind = 0 and w.name_fold_idx = ? order by w.name_fold,w.town_id;", -1, &stmt_sel003, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "update way set ind = ? where ind = 0 and name_fold_idx = ?;", -1, &stmt_sel003u, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	retval = sqlite3_prepare_v2(sql_handle, "select t.id,w.lat, w.lon, w.name, t.country_id from way w left outer join town t on w.town_id=t.id \
where ind = 0 \
order by w.name_fold,w.town_id;", -1, &stmt_sel004, NULL);
	fprintf(stderr, "prep:%d\n", retval);

	sql_counter = 0;
	sql_counter2 = 0;
	sql_counter3 = 0;
	sql_counter4 = 0;
#endif
}

static void usage(FILE *f)
{
	fprintf(f, "\n");
	fprintf(f, "ZANavi maptool - parse osm textfile and convert to [ZANavi] binfile format\n");
	fprintf(f, "version: "SVN_VERSION"\n\n");
	fprintf(f, "Usage :\n");
	fprintf(f, "bzcat planet.osm.bz2 | maptool mymap.bin\n");
	fprintf(f, "Available switches:\n");
	fprintf(f, "-h (--help)              : this screen\n");
	//	fprintf(f, "-5 (--md5)               : set file where to write md5 sum\n");
	fprintf(f, "-6 (--64bit)             : set zip 64 bit compression\n");
	fprintf(f, "-a (--attr-debug-level)  : control which data is included in the debug attribute\n");
	//	fprintf(f, "-c (--dump-coordinates)  : dump coordinates after phase 1\n");
	fprintf(f, "-X                       : generate country-border-ONLY map\n");
	fprintf(f, "-Y                       : generate coastline-ONLY map\n");
	fprintf(f, "-Z                       : output coastline-ONLY map as XML file\n");
	//#ifdef HAVE_POSTGRESQL
	//	fprintf(f," -d (--db)                : get osm data out of a postgresql database with osm simple scheme and given connect string\n");
	//#endif
	//	fprintf(f, "-e (--end)               : end at specified phase\n");
	fprintf(f, "-g                       : use ./db/ as directory for SQLite db files\n");
	fprintf(f, "-f                       : use ./db/ as directory for SQLite db files and SQLite temp files\n");
	fprintf(f, "-F                       : specify a fixed country id for this input file\n");
	fprintf(f, "-i (--input-file)        : specify the input file name (OSM), overrules default stdin\n");
	fprintf(f, "-j                       : specify number of worker threads (max. %d !!)\n", MAX_THREADS);
	fprintf(f, "-k (--keep-tmpfiles)     : do not delete temp files after processing\n");
	fprintf(f, "-N (--nodes-only)        : process only nodes\n");
	fprintf(f, "-m                       : print memory info\n");
	fprintf(f, "-n                       : ignore unknown types\n");
	// fprintf(f, "-o (--coverage)          : map every street to item coverage\n");
	// fprintf(f, "-P (--protobuf)          : input file is protobuf\n");
	fprintf(f, "-r (--rule-file)         : read mapping rules from specified file\n");
	//	fprintf(f, "-s (--start)             : start at specified phase\n");
	fprintf(f, "-S (--slice-size)        : defines the amount of memory to use, in bytes. Default is 1GB\n");
	fprintf(f, "-v                       : verbose mode\n");
	fprintf(f, "-w (--dedupe-ways)       : ensure no duplicate ways or nodes. useful when using several input files\n");
	fprintf(f, "-W (--ways-only)         : process only ways\n");
	fprintf(f, "-z (--compression-level) : set the compression level\n");
	fprintf(f, "-U (--unknown-country)   : add objects with unknown country to index\n");

	exit(1);
}

void *sql_thread(void *ptr)
{
	// this call never returns!!
#ifdef MAPTOOL_USE_SQL
	sqlite3async_run();
#endif
}

void *multi_threaded_phase_001(void *ptr)
{
	struct phase_001_thread_var *vars = (struct phase_001_thread_var*) ptr;

	long long i_x_slice_base = vars->count * slice_size;

	fprintf(stderr, "[THREAD] #%d load buffer\n", vars->thread_num);
	load_buffer("coords.tmp", &node_buffer[vars->thread_num], i_x_slice_base, slice_size);
	fprintf(stderr, "[THREAD] #%d load buffer ready\n", vars->thread_num);

	// try to fillup the node_hash again
	fprintf(stderr, "[THREAD] #%d fill hash node\n", vars->thread_num);
	fill_hash_node(vars->thread_num);
	fprintf(stderr, "[THREAD] #%d fill hash node ready\n", vars->thread_num);

	fprintf(stderr, "[THREAD] #%d ref ways\n", vars->thread_num);
	ref_ways(vars->file1, vars->thread_num); // --> this just sets "ref_way" count, of nodes (and takes hours of time!!)
	fprintf(stderr, "[THREAD] #%d ref ways ready\n", vars->thread_num);

	fprintf(stderr, "[THREAD] #%d save buffer\n", vars->thread_num);
	save_buffer("coords.tmp", &node_buffer[vars->thread_num], i_x_slice_base); // this saves the "ref_way" count back to file
	fprintf(stderr, "[THREAD] #%d save buffer ready\n", vars->thread_num);

	fprintf(stderr, "[THREAD] #%d free buffer\n", vars->thread_num);
	// just to be save always free buffer here (dont want to have mem leaks)
	free_buffer("coords.tmp", &node_buffer[vars->thread_num]);
	fprintf(stderr, "[THREAD] #%d free buffer ready\n", vars->thread_num);

	thread_is_working_[vars->thread_num] = 0;
	fprintf(stderr, "[THREAD] #%d ready\n", vars->thread_num);
}

int main(int argc, char **argv)
{
	FILE *ways = NULL, *ways_split = NULL, *ways_split_index = NULL, *towns = NULL, *nodes = NULL;
	FILE *turn_restrictions = NULL, *graph = NULL, *coastline = NULL;
	FILE *tilesdir, *coords, *relations = NULL, *boundaries = NULL, *relations_riverbank = NULL;
	FILE *files[10];
	FILE *references[10];

	// -------------------------------------------
	// initialize gthreads
	g_thread_init(NULL);
	fprintf(stderr, "glib thread support=%d\n", g_thread_supported());
	// -------------------------------------------

	time(&global_start_tt);

	linguistics_init();

#if 0
	char *map=g_strdup(attrmap);
#endif

	int jk;
	for (jk = 0; jk < max_threads; jk++)
	{
		node_buffer[jk].malloced_step = 64 * 1024 * 1024; // extend in 64MByte steps
		node_buffer[jk].malloced = 0;
		node_buffer[jk].base = NULL;
		node_buffer[jk].size = 0;

		seekpos_waynode[jk] = -1;
		last_seekpos_waynode[jk] = -1;
		last_seek_wayid[jk] = -1;
	}

	int zipnum, c, start = 1, end = 99, dump_coordinates = 0;
	int keep_tmpfiles = 0;
	int process_nodes = 1, process_ways = 1, process_relations = 1;
#ifdef HAVE_ZLIB
	int compression_level = 9;
#else
	int compression_level = 0;
#endif
	int zip64 = 0;
	int output = 0;
	int input = 0;
	int protobuf = 0;
	int f, pos;
	char *result, *optarg_cp, *attr_name, *attr_value;
	char *protobufdb = NULL, *protobufdb_operation = NULL, *md5file = NULL;
	GList *boundaries_list = NULL;

	diff_tt = 0;
	diff2_tt = 0;

	use_global_fixed_country_id = 0;

#ifdef HAVE_POSTGRESQL
	char *dbstr=NULL;
#endif

	FILE* input_file = stdin; // input data

	FILE* rule_file = NULL; // external rule file

	struct attr *attrs[10];
	GList *map_handles = NULL;
	struct map *handle;
	struct maptool_osm osm;
#if 0
	char *suffixes[]=
	{	"m0l0", "m0l1","m0l2","m0l3","m0l4","m0l5","m0l6"};
	char *suffixes[]=
	{	"m","r"};
#else
	char *suffixes[] = { "" };
#endif
	char *suffix = suffixes[0];

	int suffix_count = sizeof(suffixes) / sizeof(char *);
	int i;
	char r[] = "r"; /* Used to make compiler happy due to Bug 35903 in gcc */
	main_init(argv[0]);
	struct zip_info *zip_info = NULL;
	int suffix_start = 0;
	char *timestamp = current_to_iso8601();
	char *url = NULL;
#ifndef HAVE_GLIB
	//	_g_slice_thread_init_nomessage();
#endif

	osm.boundaries = NULL;
	osm.relations_riverbank = NULL;
	osm.turn_restrictions = NULL;
	osm.nodes = NULL;
	osm.ways = NULL;
	osm.line2poi = NULL;
	osm.poly2poi = NULL;
	osm.towns = NULL;
	osm.ways_with_coords = NULL;

	// init the ling. hashes!!
	linguistics_init();

	while (1)
	{
#if 0
		int this_option_optind = optind ? optind : 1;
#endif
		int option_index = 0;
		static struct option long_options[] = { { "md5", 1, 0, '5' }, { "64bit", 0, 0, '6' }, { "attr-debug-level", 1, 0, 'a' }, { "binfile", 0, 0, 'b' }, { "compression-level", 1, 0, 'z' },
#ifdef HAVE_POSTGRESQL
				{	"db", 1, 0, 'd'},
#endif
				{ "dedupe-ways", 0, 0, 'w' }, { "dump", 0, 0, 'D' }, { "dump-coordinates", 0, 0, 'c' }, { "end", 1, 0, 'e' }, { "help", 0, 0, 'h' }, { "keep-tmpfiles", 0, 0, 'k' }, { "nodes-only", 0, 0, 'N' }, { "plugin", 1, 0, 'p' },
				// { "protobuf", 0, 0, 'P' },
				{ "start", 1, 0, 's' }, { "input-file", 1, 0, 'i' }, { "rule-file", 1, 0, 'r' }, { "ignore-unknown", 0, 0, 'n' }, { "url", 1, 0, 'u' }, { "ways-only", 0, 0, 'W' }, { "slice-size", 1, 0, 'S' }, { "unknown-country", 0, 0, 'U' }, { 0, 0, 0, 0 } };
		c = getopt_long(argc, argv, "5:6B:DF:NO:PWS:a:bc"
#ifdef HAVE_POSTGRESQL
				"d:"
#endif
					"e:fghj:i:knmp:r:s:wvu:z:UXYZ", long_options, &option_index);
		if (c == -1)
		{
			break;
		}

		switch (c)
		{
			case '5':
				md5file = optarg;
				break;
			case '6':
				fprintf(stderr, "I will generate a ZIP64 map\n");
				zip64 = 1;
				break;
				//case 'B':
				//	protobufdb = optarg;
				//	break;
			case 'D':
				output = 1;
				break;
			case 'N':
				process_ways = 0;
				break;
			case 'R':
				process_relations = 0;
				break;
				//case 'O':
				//	protobufdb_operation = optarg;
				//	output = 1;
				//	break;
				//case 'P':
				//	protobuf = 1;
				//	break;
			case 'S':
				slice_size = atoll(optarg);
				break;
			case 'W':
				process_nodes = 0;
				break;
			case 'U':
				fprintf(stderr, "Towns in UNKNOWN Country will be added to index\n");
				unknown_country = 1;
				break;
			case 'X':
				fprintf(stderr, "I will GENERATE a country-border-only map\n");
				border_only_map = 1;
				break;
			case 'Y':
				fprintf(stderr, "I will GENERATE a coastline-only map\n");
				coastline_only_map = 1;
				break;
			case 'Z':
				fprintf(stderr, "I will output a country-border-only map as XML file\n");
				border_only_map = 1;
				border_only_map_as_xml = 1;

				FILE *out_ = tempfile("", "borders.xml", 1);
				fprintf(out_, "<?xml version='1.0' encoding='UTF-8'?>\n");
				fprintf(out_, "<osm version=\"0.6\" generator=\"ZANavi maptool\">\n");
				fclose(out_);

				break;
			case 'a':
				attr_debug_level = atoi(optarg);
				break;
			case 'j':
				threads = atoi(optarg);
				if (threads > MAX_THREADS)
				{
					threads = MAX_THREADS;
				}
				else if (threads < 1)
				{
					threads = 1;
				}
				fprintf(stderr, "using %d THREADS\n", threads);
				break;
			case 'b':
				input = 1;
				break;
			case 'c':
				dump_coordinates = 1;
				break;
#ifdef HAVE_POSTGRESQL
				case 'd':
				dbstr=optarg;
				break;
#endif
			case 'e':
				end = atoi(optarg);
				break;
			case 'g':
				sqlite_db_dir = sqlite_db_dir_extra;
				sqlite_temp_dir = 0;
				break;
			case 'f':
				sqlite_db_dir = sqlite_db_dir_extra;
				sqlite_temp_dir = 1;
				break;
			case 'h':
				usage(stdout);
				break;
			case 'm':
				fprintf(stderr, "I will print MEMORY usage\n");
				verbose_mem = 1;
				break;
			case 'n':
				fprintf(stderr, "I will IGNORE unknown types\n");
				ignore_unkown = 1;
				break;
			case 'k':
				fprintf(stderr, "I will KEEP tmp files\n");
				keep_tmpfiles = 1;
				break;
			case 'p':
				add_plugin(optarg);
				break;
			case 's':
				start = atoi(optarg);
				break;
			case 'w':
				// dedupe_ways_hash = g_hash_table_new(NULL, NULL);
				break;
			case 'v':
				verbose_mode = 1;
				break;
			case 'F':
				use_global_fixed_country_id = 1;
				global_fixed_country_id = atoi(optarg);
				fprintf(stderr, "ASSUME map is country id: %d\n", global_fixed_country_id);
				break;
			case 'i':
				input_file = fopen(optarg, "r");
				if (input_file == NULL)
				{
					fprintf(stderr, "\nInput file (%s) not found\n", optarg);
					exit(-1);
				}
				break;
			case 'r':
				rule_file = fopen(optarg, "r");
				if (rule_file == NULL)
				{
					fprintf(stderr, "\nRule file (%s) not found\n", optarg);
					exit(-1);
				}
				break;
			case 'u':
				url = optarg;
				break;
#ifdef HAVE_ZLIB
				case 'z':
				compression_level=atoi(optarg);
				break;
#endif
			case '?':
				usage(stderr);
				break;
			default:
				fprintf(stderr, "c=%d\n", c);
		}

	}

	if (optind != argc - (output == 1 ? 0 : 1))
	{
		usage(stderr);
	}
	result = argv[optind];

	sig_alrm(0);

	// initialize plugins and OSM mappings
	maptool_init(rule_file);
	//if (protobufdb_operation)
	//{
	//	osm_protobufdb_load(input_file, protobufdb);
	//	return 0;
	//}




#if 0
	int y_;
	int x_;
	double lat_;
	double lon_;

	lat_ = 0.0;
	lon_ = 0.0;
	y_ = transform_from_geo_lat(lat_);
	x_ = transform_from_geo_lon(lon_);
	fprintf(stderr, "lat=%f lon=%f x=%d y=%d\n",lat_, lon_, x_, y_);

	lat_ = 91.0;
	lon_ = 181.0;
	y_ = transform_from_geo_lat(lat_);
	x_ = transform_from_geo_lon(lon_);
	fprintf(stderr, "lat=%f lon=%f x=%d y=%d\n",lat_, lon_, x_, y_);

	lat_ = 90.0;
	lon_ = -180.0;
	y_ = transform_from_geo_lat(lat_);
	x_ = transform_from_geo_lon(lon_);
	fprintf(stderr, "lat=%f lon=%f x=%d y=%d\n",lat_, lon_, x_, y_);

	lat_ = -90.0;
	lon_ = 180.0;
	y_ = transform_from_geo_lat(lat_);
	x_ = transform_from_geo_lon(lon_);
	fprintf(stderr, "lat=%f lon=%f x=%d y=%d\n",lat_, lon_, x_, y_);

	lat_ = -90.0;
	lon_ = -180.0;
	y_ = transform_from_geo_lat(lat_);
	x_ = transform_from_geo_lon(lon_);
	fprintf(stderr, "lat=%f lon=%f x=%d y=%d\n",lat_, lon_, x_, y_);


	x_ =  21000000;
	y_ = 240000000;
	lat_ = transform_to_geo_lat(y_);
	lon_ = transform_to_geo_lon(x_);
	fprintf(stderr, "lat=%f lon=%f x=%d y=%d\n",lat_, lon_, x_, y_);
#endif








	unlink(g_strdup_printf("%stemp_data.db",sqlite_db_dir));
	unlink("temp_data.db");

	unlink(g_strdup_printf("%stemp_data002a.db",sqlite_db_dir));
	unlink(g_strdup_printf("%stemp_data003a.db",sqlite_db_dir));
	unlink(g_strdup_printf("%stemp_data002b.db",sqlite_db_dir));
	unlink(g_strdup_printf("%stemp_data003b.db",sqlite_db_dir));
	unlink("temp_data002a.db");
	unlink("temp_data003a.db");
	unlink("temp_data002b.db");
	unlink("temp_data003b.db");

	unlink(g_strdup_printf("%stemp_data004.db",sqlite_db_dir));
	unlink(g_strdup_printf("%stemp_data005.db",sqlite_db_dir));
	unlink(g_strdup_printf("%stemp_data006.db",sqlite_db_dir));
	unlink(g_strdup_printf("%stemp_data007.db",sqlite_db_dir));
	unlink("temp_data004.db");
	unlink("temp_data005.db");
	unlink("temp_data006.db");
	unlink("temp_data007.db");

	sql_db_open();
	sql_db_init(1);


	unlink(g_strdup_printf("%sways_ref_file.db",sqlite_db_dir));
	unlink("ways_ref_file.db");
	ways_ref_file = fopen(g_strdup_printf("%sways_ref_file.db",sqlite_db_dir), "wb+");

	int jk2;
	for (jk2 = 0; jk2 < max_threads; jk2++)
	{
		ways_ref_file_thread[jk2] = fopen(g_strdup_printf("%sways_ref_file.db",sqlite_db_dir), "rb");
	}

	unlink("coords.tmp");
	if (process_ways)
	{
		ways = tempfile(suffix, "ways", 1);
	}
	if (process_nodes)
	{
		nodes = tempfile(suffix, "nodes", 1);
		towns = tempfile(suffix, "towns", 1);
	}
	if (process_ways && process_nodes)
	{
		turn_restrictions = tempfile(suffix, "turn_restrictions", 1);
	}
	if (process_relations)
	{
		boundaries = tempfile(suffix, "boundaries", 1);
		relations_riverbank = tempfile(suffix, "relations_riverbank", 1);
	}

	osm.ways_with_coords = tempfile(suffix, "ways_with_coords", 1);

	phase = 1;
	fprintf(stderr, "PROGRESS: Phase 1: collecting data\n");
	osm.ways = ways;
	osm.nodes = nodes;
	osm.towns = towns;
	osm.turn_restrictions = turn_restrictions;
	osm.boundaries = boundaries;
	osm.relations_riverbank = relations_riverbank;

	init_node_hash(max_threads, 1); // initialze to maximum array size

	if (map_handles)
	{
		if (verbose_mode)
			fprintf(stderr, "**phase1:A**\n");
		GList *l;
		phase1_map(map_handles, ways, nodes);
		l = map_handles;
		while (l)
		{
			map_destroy(l->data);
			l = g_list_next(l);
		}
	}
	else if (protobuf)
	{
		if (verbose_mode)
			fprintf(stderr, "**phase1:B**\n");
		// ---obsolete--- // map_collect_data_osm_protobuf(input_file, &osm);
	}
	else
	{
		time(&start_tt);

		if (verbose_mode)
			fprintf(stderr, "**phase1:C**\n");

		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------

		ways_processed_count = 0;
		map_collect_data_osm(input_file, &osm);

		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------
		// ---------------- MAIN routine to read in data from OSM !!! ----------


		time(&end_tt);
		diff_tt = difftime(end_tt, start_tt);
		char outstring[200];
		convert_to_human_time(diff_tt, outstring);
		fprintf(stderr, "-RUNTIME-PHASE1: %s\n", outstring);
	}


	// flush waynodes to file
	fflush(ways_ref_file);
	// just seek to cur pos of file
	fseeko(ways_ref_file, 0L, SEEK_CUR);

	if (! MAPTOOL_SQL_INPUT_TOO_SMALL)
	{
		sql_db_close();
		sql_db_open();
		sql_db_init(0);
	}

	//fprintf(stderr, "slices=====%d\n", slices);

	//if (slices)
	//{
	// THREADS -----------
	cur_thread_num = 0;
	// GThread *thread_[threads];
	pthread_t thread_[threads];
	int iret_[threads];
	FILE *ways_file[threads];
	struct phase_001_thread_var vars[threads];
	// THREADS -----------

	for (i = 0; i < threads; i++)
	{
		thread_is_working_[i] = 0;
		ways_file[i] = NULL;
	}

	init_node_hash(threads, 0);

	fprintf(stderr, "%d slices\n", slices);



	for (jk2 = 0; jk2 < max_threads; jk2++)
	{
		seekpos_waynode[jk2] = -1;
		last_seekpos_waynode[jk2] = -1;
		last_seek_wayid[jk2] = -1;
	}


	// --> too late for it here // flush_nodes(1, cur_thread_num); // flush remains to "coords.tmp" from "osm_end_node"
	// free big item buffer memory
	// free_buffer("dummy", &node_buffer[0]);





	// fprintf(stderr, "slices==2==%d\n", slices);

	long long i_x_slice_size;
	// for (i = slices - 2; i >= 0; i--)
	for (i = 0; i < slices; i++)
	{
		if (verbose_mode)
		{
			fprintf(stderr, "thread #%d\n", cur_thread_num);
		}
		// fprintf(stderr, "slice %d of %d\n", slices - i - 1, slices - 1);
		fprintf(stderr, "slice %d of %d\n", i + 1, slices);

		if (thread_is_working_[cur_thread_num] == 1)
		{
			// find next free thread
			// if all threads working, then return any thread num
			int ij;
			for (ij = 0; ij < threads; ij++)
			{
				if ((cur_thread_num + ij + 1) <= threads)
				{
					if (thread_is_working_[cur_thread_num + ij] == 0)
					{
						cur_thread_num = cur_thread_num + ij;
						fprintf(stderr, "found free thread #%d\n", cur_thread_num);
						pthread_join(thread_[cur_thread_num], NULL);
						break;
					}
				}
				else
				{
					if (thread_is_working_[cur_thread_num + ij - threads] == 0)
					{
						cur_thread_num = cur_thread_num + ij - threads;
						fprintf(stderr, "found free thread #%d\n", cur_thread_num);
						pthread_join(thread_[cur_thread_num], NULL);
						break;
					}
				}
			}
		}

		if (thread_is_working_[cur_thread_num] == 1)
		{
			time(&start_tt);

			fprintf(stderr, "need to wait for thread #%d to finish first ...\n", cur_thread_num);
			//iret_[cur_thread_num] = g_thread_join(thread_[cur_thread_num]);
			pthread_join(thread_[cur_thread_num], NULL);
			//thread_is_working_[cur_thread_num] = 0;
			if (verbose_mode)
				printf("Thread %d returns: %d\n", cur_thread_num, iret_[cur_thread_num]);
			fprintf(stderr, "... thread #%d ready\n", cur_thread_num);

			time(&end_tt);
			diff_tt = difftime(end_tt, start_tt);
			char outstring[200];
			convert_to_human_time(diff_tt, outstring);
			fprintf(stderr, "-RUNTIME-LOOP1: %s this loop run\n", outstring);
			diff2_tt = diff2_tt + diff_tt;
			if (i > 0)
			{
				double eta_time = (diff2_tt / i) * (slices - i);
				convert_to_human_time(eta_time, outstring);
				fprintf(stderr, "-RUNTIME-LOOP1: %s left\n", outstring);
			}
		}

		if (ways_file[cur_thread_num] == NULL)
		{
			ways_file[cur_thread_num] = tempfile(suffix, "ways", 0);
		}

		vars[cur_thread_num].count = i;
		vars[cur_thread_num].thread_num = cur_thread_num;
		vars[cur_thread_num].file1 = ways_file[cur_thread_num];
		fprintf(stderr, "starting thread #%d\n", cur_thread_num);

		thread_is_working_[cur_thread_num] = 1;

		//fprintf(stderr, "before start thread\n");
		//my_sleep(40);
		// thread_[cur_thread_num] = g_thread_new("t", multi_threaded_phase_001, (void*) &vars[cur_thread_num]);
		iret_[cur_thread_num] = pthread_create(&thread_[cur_thread_num], NULL, multi_threaded_phase_001, (void*) &vars[cur_thread_num]);
		//fprintf(stderr, "after start thread\n");
		//my_sleep(40);

		cur_thread_num++;
		if ((cur_thread_num + 1) > threads)
		{
			cur_thread_num = 0;
		}
	}

	for (i = 0; i < threads; i++)
	{
		if (thread_is_working_[i] == 1)
		{
			fprintf(stderr, "[LAST] need to wait for thread #%d to finish first ...\n", i);
			// iret_[i] = g_thread_join(thread_[i]);
			pthread_join(thread_[i], NULL);
			if (verbose_mode)
			{
				printf("[LAST] Thread %d returns: %d\n", i, iret_[i]);
			}
			//thread_is_working_[i] = 0;
			fprintf(stderr, "[LAST] ... ready\n");
		}
	}

	for (i = 0; i < threads; i++)
	{
		if (ways_file[i])
		{
			fclose(ways_file[i]);
			ways_file[i] = NULL;
		}
	}
	//}
	//else
	//{
	//	//fprintf(stderr, "calling save_buffer 2\n");
	//	cur_thread_num = 0;
	//	// --old-- save_buffer("coords.tmp", &node_buffer[cur_thread_num], 0); // where did we "load_buffer" first?? this seems kaputt!!
	//	flush_nodes(1, cur_thread_num); // flush remains to "coords.tmp" from "osm_end_node"
	//
	//	FILE *file1 = tempfile(suffix, "ways", 0);
	//	long long i_x_slice_base = 0 * slice_size;
	//	load_buffer("coords.tmp", &node_buffer[0], i_x_slice_base, slice_size);
	//	// try to fillup the node_hash again
	//	fill_hash_node(0);
	//	ref_ways(file1, 0); // --> this just sets "ref_way" count, of nodes
	//	save_buffer("coords.tmp", &node_buffer[0], i_x_slice_base); // this saves the "ref_way" count back to file
	//	fclose(file1);
	//}

	cur_thread_num = 0;

	// clean up buffer(s)
	for (i = 0; i < threads; i++)
	{
		int jj = 0;
		for (jj = 0; jj < 2400000; jj++)
		{
			ib_buffer_array[i][jj] = 0;
		}
	}

	for (jk = 0; jk < max_threads; jk++)
	{
		if (node_buffer[jk].base != NULL)
		{
			free(node_buffer[jk].base);
		}
		node_buffer[jk].malloced = 0;
		node_buffer[jk].base = NULL;
		node_buffer[jk].size = 0;
	}

	if (osm.ways_with_coords)
	{
		fclose(osm.ways_with_coords);
	}

	if (ways)
		fclose(ways);
	if (nodes)
		fclose(nodes);
	if (towns)
		fclose(towns);
	if (turn_restrictions)
		fclose(turn_restrictions);
	if (boundaries)
	{
		fclose(boundaries);
		fclose(relations_riverbank);
	}

	// ------ add tags from "relations" back down to "ways" -- (nonrecursive, meaning only 1 depth) -----
	fprintf(stderr, "PROGRESS: Phase 8: add tags from relations back down to ways\n");

	time(&start_tt);
	ways = tempfile(suffix, "ways", 0);
	FILE *ways_relationtags = tempfile(suffix, "ways_relationtags", 1);
	relations_riverbank = tempfile(suffix, "relations_riverbank", 0);
	copy_tags_to_ways(ways, ways_relationtags, relations_riverbank);
	fclose(relations_riverbank);
	fclose(ways_relationtags);
	fclose(ways);
	tempfile_unlink(suffix, "ways");
	tempfile_rename(suffix, "ways_relationtags", "ways");
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring2[200];
	convert_to_human_time(diff_tt, outstring2);
	fprintf(stderr, "-RUNTIME-PHASE8: %s\n", outstring2);
	// ------ add tags from "relations" back down to "ways" -- (nonrecursive, meaning only 1 depth) -----


#if 0
	// triple memory here
	long long slice_size99 = slice_size * 3;
	int slices99 = slices / 3;
	if ((slices99 * 3) < slices)
	{
		slices99++;
	}
#endif
#if 1
	// keep mem same size
	long long slice_size99 = slice_size * 1;
	int slices99 = slices / 1;
	if ((slices99 * 1) < slices)
	{
		slices99++;
	}
#endif

	if (process_ways)
	{
		ways = tempfile(suffix, "ways", 0); // start with "ways" file
		phase = 2;
		fprintf(stderr, "PROGRESS: Phase 2: finding intersections\n"); // and resolve ways (write coords from nodes to ways)!!
		diff2_tt = 0;
		for (i = 0; i < slices99; i++)
		{
			time(&start_tt);

			fprintf(stderr, "slice %d of %d\n", i + 1, slices99);
			int final = (i >= slices99 - 1);
			ways_split = tempfile(suffix, "ways_split", 1);
			ways_split_index = final ? tempfile(suffix, "ways_split_index", 1) : NULL;
			graph = tempfile(suffix, "graph", 1);
			coastline = tempfile(suffix, "coastline", 1);

			load_buffer("coords.tmp", &node_buffer[cur_thread_num], i * slice_size99, slice_size99);
			// try to fillup the node_hash again
			fill_hash_node(cur_thread_num);

			// required for fread to work!
			// fseeko(ways_ref_file, 0L, SEEK_CUR);

			// fill in lat,long for way, and find intersections
			map_find_intersections(ways, ways_split, ways_split_index, graph, coastline, final);
			// write to "ways_split" file

			fclose(ways_split);
			if (ways_split_index)
			{
				fclose(ways_split_index);
			}
			fclose(ways);
			fclose(graph);
			fclose(coastline);
			if (!final)
			{
				// rename to "ways_to_resolve" and use that as new input
				tempfile_rename(suffix, "ways_split", "ways_to_resolve");
				ways = tempfile(suffix, "ways_to_resolve", 0);
			}

			if (i == 0)
			{
				// after first loop, delete unused original ways file (to save diskspace)
				if (!keep_tmpfiles)
				{
					tempfile_unlink(suffix, "ways");
				}
			}

			time(&end_tt);
			diff_tt = difftime(end_tt, start_tt);
			char outstring[200];
			convert_to_human_time(diff_tt, outstring);
			fprintf(stderr, "-RUNTIME-LOOP2: %s this loop run\n", outstring);
			diff2_tt = diff2_tt + diff_tt;
			if ((i + 1) > 0)
			{
				double eta_time = (diff2_tt / (i + 1)) * (slices99 - (i + 1));
				convert_to_human_time(eta_time, outstring);
				fprintf(stderr, "-RUNTIME-LOOP2: %s left\n", outstring);
			}
		}

		tempfile_unlink(suffix, "ways_to_resolve");
	}
	else
	{
		fprintf(stderr, "PROGRESS: Skipping Phase 2\n");
	}

	for (jk = 0; jk < max_threads; jk++)
	{
		if (node_buffer[jk].base != NULL)
		{
			free(node_buffer[jk].base);
		}
		node_buffer[jk].malloced = 0;
		node_buffer[jk].base = NULL;
		node_buffer[jk].size = 0;
	}

	// ------ append already resolved ways to ways file -----
	fprintf(stderr, "PROGRESS: Phase 12: append already resolved ways to ways file\n");

	time(&start_tt);
	ways = tempfile(suffix, "ways_split", 2);
	osm.ways_with_coords = tempfile(suffix, "ways_with_coords", 0);
	append_pre_resolved_ways(ways, &osm);
	if (ways)
	{
		fclose(ways);
	}
	if (osm.ways_with_coords)
	{
		fclose(osm.ways_with_coords);
	}
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring6[200];
	convert_to_human_time(diff_tt, outstring6);
	fprintf(stderr, "-RUNTIME-PHASE12: %s\n", outstring6);
	// ------ append already resolved ways to ways file -----


	// ------ take attr housenumber from ways (building=yes) and add as housenumber node -----
	fprintf(stderr, "PROGRESS: Phase 9.1: take attr housenumber and add as housenumber node\n");

	time(&start_tt);
	ways = tempfile(suffix, "ways_split", 0);
	FILE *nodes2 = tempfile(suffix, "nodes", 2);
	process_way2poi_housenumber(ways, nodes2); // append to "nodes" file
	if (ways)
		fclose(ways);
	if (nodes2)
		fclose(nodes2);
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring3[200];
	convert_to_human_time(diff_tt, outstring3);
	fprintf(stderr, "-RUNTIME-PHASE9.1: %s\n", outstring3);
	// ------ take attr housenumber from ways (building=yes) and add as housenumber node -----


#if 0
	// ------ take attr housenumber_interpolate* from ways and add as housenumber node -------
	fprintf(stderr, "PROGRESS: Phase 9.2: take attr housenumber_interpolate and add as housenumber node\n");

	time(&start_tt);
	ways = tempfile(suffix, "ways_split", 0);
	FILE *nodes3 = tempfile(suffix, "nodes", 2);
	map_find_housenumbers_interpolation(ways, nodes3); // append to "nodes" file
	if (ways)
		fclose(ways);
	if (nodes3)
		fclose(nodes3);
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring3a[200];
	convert_to_human_time(diff_tt, outstring3a);
	fprintf(stderr, "-RUNTIME-PHASE9.2: %s\n", outstring3a);
	// ------ take attr housenumber_interpolate* from ways and add as housenumber node -------
#endif


	FILE *coastline2 = tempfile(suffix, "coastline", 0);
	if (coastline2)
	{
		time(&start_tt);

		fprintf(stderr, "PROGRESS: Processing Coastlines\n");
		FILE *coastline_result = tempfile(suffix, "coastline_result", 1);
		process_coastlines(coastline2, coastline_result); // complex coastline stuff (at normal mapgen it does nothing!!)
		fclose(coastline_result);
		fclose(coastline2);

		time(&end_tt);
		diff_tt = difftime(end_tt, start_tt);
		char outstring[200];
		convert_to_human_time(diff_tt, outstring);
		fprintf(stderr, "-RUNTIME-COASTLINES: %s\n", outstring);
	}

	// sql_db_open();
	// sql_db_init(0);

	FILE *towns2 = tempfile(suffix, "towns", 0);
	FILE *boundaries2 = NULL;
	FILE *ways2 = NULL;
	FILE *coords2 = NULL;
	if (towns2)
	{
		time(&start_tt);
		boundaries2 = tempfile(suffix, "boundaries", 0);
		ways2 = tempfile(suffix, "ways_split", 0);
		coords2 = fopen("coords.tmp", "rb");

#ifdef MAPTOOL_USE_SQL
		sql_counter = 0;
		sql_counter2 = 0;
		sql_counter3 = 0;
		sql_counter4 = 0;
		sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle002a, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle003a, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle002b, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle003b, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle004, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle005, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle006, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle007, "COMMIT", 0, 0, 0);
#endif
		boundaries_list = osm_process_towns(towns2, coords2, boundaries2, ways2); // check where towns are located with boundaries and "is_in"

		fprintf(stderr, "PROGRESS: Correcting Boundary Refpoints\n");
		correct_boundary_ref_point(boundaries_list);
		fprintf(stderr, "PROGRESS: Building Boundarytree\n");
		build_boundary_tree(boundaries_list);
#ifdef MAPTOOL_USE_SQL
		sql_counter = 0;
		sql_counter2 = 0;
		sql_counter3 = 0;
		sql_counter4 = 0;
		sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle002a, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle003a, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle002b, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle003b, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle004, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle005, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle006, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle007, "COMMIT", 0, 0, 0);

		fprintf(stderr, "SQL: (create index 002) start\n");
		sql_create_index002();
		fprintf(stderr, "SQL: (create index 002) ready\n");

		if (! MAPTOOL_SQL_INPUT_TOO_SMALL)
		{
			// reopen for indexes to be used
			sql_db_close();
			sql_db_open();
			sql_db_init(0);
		}

#endif

		fclose(ways2);
		fclose(boundaries2);
		fclose(towns2);
		fclose(coords2);
		if (!keep_tmpfiles)
		{
			tempfile_unlink(suffix, "towns");
		}
		time(&end_tt);
		diff_tt = difftime(end_tt, start_tt);
		char outstring[200];
		convert_to_human_time(diff_tt, outstring);
		fprintf(stderr, "-RUNTIME-TOWNS: %s\n", outstring);
	}



	// ------ remove useless tags from ways --------------------------------------------------
	fprintf(stderr, "PROGRESS: Phase 10: remove useless tags from ways\n");

	time(&start_tt);
	ways = tempfile(suffix, "ways_split", 0);
	FILE *ways28 = tempfile(suffix, "ways_tags_removed", 1);
	remove_useless_tags_from_ways(ways, ways28);
	if (ways)
		fclose(ways);
	if (ways28)
		fclose(ways28);
	tempfile_unlink(suffix, "ways_split");
	tempfile_rename(suffix, "ways_tags_removed", "ways_split");
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring4[200];
	convert_to_human_time(diff_tt, outstring4);
	fprintf(stderr, "-RUNTIME-PHASE10: %s\n", outstring4);
	// ------ remove useless tags from ways --------------------------------------------------


	// ------ assign town to streets --------------------------------------------------
	fprintf(stderr, "PROGRESS: Phase 13: assign town to streets\n");

	time(&start_tt);
	assign_town_to_streets(boundaries_list);
	// we should free the GList boundaries_list here!!!
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring7[200];
	convert_to_human_time(diff_tt, outstring7);
	fprintf(stderr, "-RUNTIME-PHASE13: %s\n", outstring7);
	// ------ assign town to streets --------------------------------------------------


	// ------ generate street index file --------------------------------------------------
	fprintf(stderr, "PROGRESS: Phase 14: generate street index file\n");

	time(&start_tt);

	FILE *ways14_2 = tempfile(suffix, "town_index", 1);
	generate_town_index_file(ways14_2);
	fclose(ways14_2);

	FILE *ways14_1 = tempfile(suffix, "street_index", 1);
	generate_street_index_file(ways14_1);
	fclose(ways14_1);

	ways14_1 = tempfile(suffix, "street_index", 0);
	ways14_2 = tempfile(suffix, "town_index", 0);
	FILE *ways14_3 = tempfile(suffix, "street_town_index", 1);
	generate_combined_index_file(ways14_2, ways14_1, ways14_3);
	fclose(ways14_1);
	fclose(ways14_2);
	fclose(ways14_3);

	tempfile_unlink(suffix, "town_index");
	tempfile_unlink(suffix, "street_index");

	// street_town_index_.tmp  ->  <$result>.idx
	char* outfilename_idx = g_strdup_printf("%s.idx", result);
	rename("street_town_index_.tmp", outfilename_idx);
	g_free(outfilename_idx);

	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring8[200];
	convert_to_human_time(diff_tt, outstring8);
	fprintf(stderr, "-RUNTIME-PHASE14: %s\n", outstring8);
	// ------ generate street index file --------------------------------------------------



	if (! MAPTOOL_SQL_INPUT_TOO_SMALL)
	{
		sql_db_close();
	}

	if (!keep_tmpfiles)
	{
		unlink(g_strdup_printf("%stemp_data.db",sqlite_db_dir));
		unlink("temp_data.db");

		unlink(g_strdup_printf("%stemp_data002a.db",sqlite_db_dir));
		unlink(g_strdup_printf("%stemp_data003a.db",sqlite_db_dir));
		unlink(g_strdup_printf("%stemp_data002b.db",sqlite_db_dir));
		unlink(g_strdup_printf("%stemp_data003b.db",sqlite_db_dir));
		unlink("temp_data002a.db");
		unlink("temp_data003a.db");
		unlink("temp_data002b.db");
		unlink("temp_data003b.db");

		unlink(g_strdup_printf("%stemp_data004.db",sqlite_db_dir));
		unlink(g_strdup_printf("%stemp_data005.db",sqlite_db_dir));
		unlink(g_strdup_printf("%stemp_data006.db",sqlite_db_dir));
		unlink(g_strdup_printf("%stemp_data007.db",sqlite_db_dir));
		unlink("temp_data004.db");
		unlink("temp_data005.db");
		unlink("temp_data006.db");
		unlink("temp_data007.db");

		int jk2;
		for (jk2 = 0; jk2 < max_threads; jk2++)
		{
			fclose(ways_ref_file_thread[jk2]);
		}

		fclose(ways_ref_file);

		unlink(g_strdup_printf("%sways_ref_file.db",sqlite_db_dir));
		unlink("ways_ref_file.db");
	}










	fprintf(stderr, "PROGRESS: Phase 3: sorting countries, generating turn restrictions\n");
	sort_countries(keep_tmpfiles); // sort the temp country files

	if (process_relations)
	{
		turn_restrictions = tempfile(suffix, "turn_restrictions", 0);
		if (turn_restrictions)
		{
			time(&start_tt);
			relations = tempfile(suffix, "relations", 1);
			coords = fopen("coords.tmp", "rb");
			ways_split = tempfile(suffix, "ways_split", 0);
			ways_split_index = tempfile(suffix, "ways_split_index", 0);
			process_turn_restrictions(turn_restrictions, coords, ways_split, ways_split_index, relations); // process turn restrictions (osm releations!)
			fclose(ways_split_index);
			fclose(ways_split);
			fclose(coords);
			fclose(relations);
			fclose(turn_restrictions);
			if (!keep_tmpfiles)
			{
				tempfile_unlink(suffix, "turn_restrictions");
			}
			time(&end_tt);
			diff_tt = difftime(end_tt, start_tt);
			char outstring[200];
			convert_to_human_time(diff_tt, outstring);
			fprintf(stderr, "-RUNTIME-TURN_RESTRICTIONS: %s\n", outstring);
		}
	}

	if (!keep_tmpfiles)
	{
		tempfile_unlink(suffix, "ways_split_index");
	}

	if (output == 1)
	{
		fprintf(stderr, "PROGRESS: Phase 4: dumping\n");
		if (process_nodes)
		{
			nodes = tempfile(suffix, "nodes", 0);
			if (nodes)
			{
				dump(nodes);
				fclose(nodes);
			}
		}
		if (process_ways)
		{
			ways_split = tempfile(suffix, "ways_split", 0);
			if (ways_split)
			{
				dump(ways_split);
				fclose(ways_split);
			}
		}
		if (process_relations)
		{
			relations = tempfile(suffix, "relations", 0);
			if (relations)
			{
				dump(relations);
				fclose(relations);
			}
		}
		exit(0);
	}

	// ------ remove useless tags from nodes --------------------------------------------------
	fprintf(stderr, "PROGRESS: Phase 11: remove useless tags from nodes\n");

	time(&start_tt);
	nodes = tempfile(suffix, "nodes", 0);
	FILE *nodes28 = tempfile(suffix, "nodes_tags_removed", 1);
	remove_useless_tags_from_nodes(nodes, nodes28);
	if (nodes)
		fclose(nodes);
	if (nodes28)
		fclose(nodes28);
	tempfile_unlink(suffix, "nodes");
	tempfile_rename(suffix, "nodes_tags_removed", "nodes");
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring5[200];
	convert_to_human_time(diff_tt, outstring5);
	fprintf(stderr, "-RUNTIME-PHASE11: %s\n", outstring5);
	// ------ remove useless tags from nodes --------------------------------------------------


	// remove DB
	// ** unlink("temp_data.db");
	// ** unlink("temp_data002.db");


	for (i = suffix_start; i < suffix_count; i++)
	{
		suffix = suffixes[i];
		if (start <= 4)
		{
			phase = 3;
			if (i == suffix_start)
			{
				zip_info = zip_new();
				zip_set_zip64(zip_info, zip64);
				zip_set_timestamp(zip_info, timestamp);
			}
			zipnum = zip_get_zipnum(zip_info);
			fprintf(stderr, "PROGRESS: Phase 4: generating tiles %s\n", suffix);
			tilesdir = tempfile(suffix, "tilesdir", 1);
			if (!strcmp(suffix, r))
			{ /* Makes compiler happy due to bug 35903 in gcc */
				ch_generate_tiles(suffixes[0], suffix, tilesdir, zip_info);
			}
			else
			{
				for (f = 0; f < 3; f++)
				{
					files[f] = NULL;
				}
				if (process_relations)
				{
					files[0] = tempfile(suffix, "relations", 0);
				}
				if (process_ways)
				{
					files[1] = tempfile(suffix, "ways_split", 0);
				}
				if (process_nodes)
				{
					files[2] = tempfile(suffix, "nodes", 0);
				}

				time(&start_tt);
				phase4(files, 3, 0, suffix, tilesdir, zip_info);
				time(&end_tt);
				diff_tt = difftime(end_tt, start_tt);
				char outstring[200];
				convert_to_human_time(diff_tt, outstring);
				fprintf(stderr, "-RUNTIME-PHASE4: %s\n", outstring);

				for (f = 0; f < 3; f++)
				{
					if (files[f])
					{
						fclose(files[f]);
					}
				}
			}
			fclose(tilesdir);
			zip_set_zipnum(zip_info, zipnum);
		}

		if (end == 4)
			exit(0);

		if (zip_info)
		{
			zip_destroy(zip_info);
			zip_info = NULL;
		}

		if (start <= 5)
		{
			phase = 4;
			fprintf(stderr, "PROGRESS: Phase 5: assembling map %s\n", suffix);
			if (i == suffix_start)
			{
				char *zipdir = tempfile_name("zipdir", "");
				char *zipindex = tempfile_name("index", "");
				zip_info = zip_new();
				zip_set_zip64(zip_info, zip64);
				zip_set_timestamp(zip_info, timestamp);
				zip_set_maxnamelen(zip_info, 14 + strlen(suffixes[0]));
				zip_set_compression_level(zip_info, compression_level);

				if (md5file)
				{
					zip_set_md5(zip_info, 1);
				}

				zip_open(zip_info, result, zipdir, zipindex);

				if (url)
				{
					map_information_attrs[1].type = attr_url;
					map_information_attrs[1].u.str = url;
				}
				index_init(zip_info, 1);
			}

			if (!strcmp(suffix, r))
			{ /* Makes compiler happy due to bug 35903 in gcc */
				ch_assemble_map(suffixes[0], suffix, zip_info);
			}
			else
			{
				for (f = 0; f < 3; f++)
				{
					files[f] = NULL;
					references[f] = NULL;
				}

				if (process_relations)
				{
					files[0] = tempfile(suffix, "relations", 0);
				}

				if (process_ways)
				{
					files[1] = tempfile(suffix, "ways_split", 0);
					references[1] = tempfile(suffix, "ways_split_ref", 1);
				}

				if (process_nodes)
				{
					files[2] = tempfile(suffix, "nodes", 0);
				}

				fprintf(stderr, "Slice %d\n", i);

				time(&start_tt);
				phase5(files, references, 3, 0, suffix, zip_info);
				time(&end_tt);
				diff_tt = difftime(end_tt, start_tt);
				char outstring[200];
				convert_to_human_time(diff_tt, outstring);
				fprintf(stderr, "-RUNTIME-PHASE5: %s\n", outstring);

				for (f = 0; f < 3; f++)
				{
					if (files[f])
					{
						fclose(files[f]);
					}
					if (references[f])
					{
						fclose(references[f]);
					}
				}
			}

			if (!keep_tmpfiles)
			{
				tempfile_unlink(suffix, "relations");
				tempfile_unlink(suffix, "nodes");
				tempfile_unlink(suffix, "ways_split");
				tempfile_unlink(suffix, "ways_split_ref");
				tempfile_unlink(suffix, "ways_with_coords");
				tempfile_unlink(suffix, "coastline");
				tempfile_unlink(suffix, "turn_restrictions");
				tempfile_unlink(suffix, "graph");
				tempfile_unlink(suffix, "tilesdir");
				tempfile_unlink(suffix, "boundaries");
				tempfile_unlink(suffix, "relations_riverbank");
				tempfile_unlink(suffix, "coastline_result");

				unlink("coords.tmp");
			}

			if (i == suffix_count - 1)
			{
				unsigned char md5_data[16];

				zipnum = zip_get_zipnum(zip_info);
				add_aux_tiles("auxtiles.txt", zip_info);

				write_countrydir(zip_info);
				zip_set_zipnum(zip_info, zipnum);
				write_aux_tiles(zip_info);
				zip_write_index(zip_info);
				zip_write_directory(zip_info);
				zip_close(zip_info);

				if (md5file && zip_get_md5(zip_info, md5_data))
				{
					FILE *md5 = fopen(md5file, "w");
					int i;
					for (i = 0; i < 16; i++)
					{
						fprintf(md5, "%02x", md5_data[i]);
					}
					fprintf(md5, "\n");
					fclose(md5);
				}

				if (!keep_tmpfiles)
				{
					remove_countryfiles();
					tempfile_unlink("index", "");
					tempfile_unlink("zipdir", "");
				}
			}
		}
	}

	if (border_only_map_as_xml == 1)
	{
		FILE *out_ = tempfile("", "borders.xml", 2);
		fprintf(out_, "</osm>\n");
		fclose(out_);
	}

	sig_alrm(0);
	sig_alrm_end();

	time(&global_end_tt);
	global_diff_tt = difftime(global_end_tt, global_start_tt);
	char global_outstring[200];
	convert_to_human_time(global_diff_tt, global_outstring);
	fprintf(stderr, "PROGRESS: Phase 999:%s:### Map Ready ### -RUNTIME-OVERALL: %s\n", result, global_outstring);

	return 0;
}

