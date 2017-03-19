/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2015 Zoff <zoff@zoff.cc>
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



#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION




typedef unsigned char uint8;
typedef unsigned short uint16;
typedef unsigned int uint;

#define MZ_MIN(a,b) (((a)<(b))?(a):(b))
#define my_min(a,b) (((a) < (b)) ? (a) : (b))




// -------------------------------
// -------------------------------

int IN_BUF_SIZE;
static uint8* s_inbuf;
int OUT_BUF_SIZE;
static uint8* s_outbuf;
int COMP_OUT_BUF_SIZE;

//** int s_IN_BUF_SIZE = sizeof(struct streets_index_data_block) * 1024;
//** int t_IN_BUF_SIZE = sizeof(struct town_index_data_block) * 1024;
//** static uint8 s_s_inbuf[(sizeof(struct streets_index_data_block) * 1024)];
//** static uint8 t_s_inbuf[(sizeof(struct town_index_data_block) * 1024)];

//** int s_OUT_BUF_SIZE = sizeof(struct streets_index_data_block) * 1024;
//** int t_OUT_BUF_SIZE = sizeof(struct town_index_data_block) * 1024;
//** static uint8 s_s_outbuf[(sizeof(struct streets_index_data_block) * 1024)];
//** static uint8 t_s_outbuf[(sizeof(struct town_index_data_block) * 1024)];

int s_COMP_OUT_BUF_SIZE;
int t_COMP_OUT_BUF_SIZE;


/* ------ new ------ */
int s_IN_BUF_SIZE = sizeof(struct streets_index_data_block) * 2048;
int t_IN_BUF_SIZE = sizeof(struct town_index_data_block) * 2048;
uint8 *s_s_inbuf;
uint8 *t_s_inbuf;

int s_OUT_BUF_SIZE = (sizeof(struct streets_index_data_block) * 512);
int t_OUT_BUF_SIZE = (sizeof(struct town_index_data_block) * 512);
uint8 *s_s_outbuf;
uint8 *t_s_outbuf;
/* ------ new ------ */


// IN_BUF_SIZE is the size of the file read buffer.
// IN_BUF_SIZE must be >= 1
//**#define IN_BUF_SIZE (1024*512)
//**static uint8 s_inbuf[IN_BUF_SIZE];

// COMP_OUT_BUF_SIZE is the size of the output buffer used during compression.
// COMP_OUT_BUF_SIZE must be >= 1 and <= OUT_BUF_SIZE
//**#define COMP_OUT_BUF_SIZE (1024*512)

// OUT_BUF_SIZE is the size of the output buffer used during decompression.
// OUT_BUF_SIZE must be a power of 2 >= TINFL_LZ_DICT_SIZE (because the low-level decompressor not only writes, but reads from the output buffer as it decompresses)
//#define OUT_BUF_SIZE (TINFL_LZ_DICT_SIZE)
//*#define OUT_BUF_SIZE (1024*512)
//*static uint8 s_outbuf[OUT_BUF_SIZE];

// -------------------------------
// -------------------------------




// compression level
int street_index_compress_level = 9; // = MZ_BEST_COMPRESSION

long long compress_file(char *in, char *out, int keep_tempfile)
{
	long long out_size = 0;
	// compress structure
	tdefl_compressor *g_deflator;
	g_deflator = g_new0(tdefl_compressor, 1);
	// compress structure

	// ok now compress the block (file) -------------------------------
	const void *next_in = s_inbuf;
	size_t avail_in = 0;
	void *next_out = s_outbuf;
	size_t avail_out = OUT_BUF_SIZE;
	size_t total_in = 0, total_out = 0;

	FILE *in_uncompr = tempfile("", in, 0);
	FILE *out_compr = NULL;
	out_compr = tempfile("", out, 1);

	// Determine input file's size.
	fseek(in_uncompr, 0, SEEK_END);
	long file_loc = ftell(in_uncompr);
	fseek(in_uncompr, 0, SEEK_SET);
	uint infile_size = (uint)file_loc;

	// The number of dictionary probes to use at each compression level (0-10). 0=implies fastest/minimal possible probing.
	static const mz_uint s_tdefl_num_probes[11] =
	{	0, 1, 6, 32, 16, 32, 128, 256, 512, 768, 1500};

	tdefl_status status;
	uint infile_remaining = infile_size;

	// create tdefl() compatible flags (we have to compose the low-level flags ourselves, or use tdefl_create_comp_flags_from_zip_params() but that means MINIZ_NO_ZLIB_APIS can't be defined).
	mz_uint comp_flags = TDEFL_WRITE_ZLIB_HEADER | s_tdefl_num_probes[MZ_MIN(10, street_index_compress_level)] | ((street_index_compress_level <= 3) ? TDEFL_GREEDY_PARSING_FLAG : 0);
	if (!street_index_compress_level)
	{
		comp_flags |= TDEFL_FORCE_ALL_RAW_BLOCKS;
	}

	// Initialize the low-level compressor.
	status = tdefl_init(g_deflator, NULL, NULL, comp_flags);
	if (status != TDEFL_STATUS_OKAY)
	{
		fprintf(stderr, "tdefl_init() failed!\n");
		g_free(g_deflator);
		return 0;
	}
	avail_out = COMP_OUT_BUF_SIZE;

	// Compression.
	for (;; )
	{
		size_t in_bytes, out_bytes;

		if (!avail_in)
		{
			// Input buffer is empty, so read more bytes from input file.
			uint n = my_min(IN_BUF_SIZE, infile_remaining);

			if (fread(s_inbuf, 1, n, in_uncompr) != n)
			{
				fprintf(stderr, "Failed reading from input file!\n");
				g_free(g_deflator);
				return 0;
			}

			next_in = s_inbuf;
			avail_in = n;

			infile_remaining -= n;
			//printf("Input bytes remaining: %u\n", infile_remaining);
		}

		in_bytes = avail_in;
		out_bytes = avail_out;
		// Compress as much of the input as possible (or all of it) to the output buffer.
		status = tdefl_compress(g_deflator, next_in, &in_bytes, next_out, &out_bytes, infile_remaining ? TDEFL_NO_FLUSH : TDEFL_FINISH);

		next_in = (const char *)next_in + in_bytes;
		avail_in -= in_bytes;
		total_in += in_bytes;

		next_out = (char *)next_out + out_bytes;
		avail_out -= out_bytes;
		total_out += out_bytes;

		if ((status != TDEFL_STATUS_OKAY) || (!avail_out))
		{
			// Output buffer is full, or compression is done or failed, so write buffer to output file.
			uint n = COMP_OUT_BUF_SIZE - (uint)avail_out;
			if (fwrite(s_outbuf, 1, n, out_compr) != n)
			{
				fprintf(stderr, "Failed writing to output file!\n");
				g_free(g_deflator);
				return 0;
			}
			next_out = s_outbuf;
			avail_out = COMP_OUT_BUF_SIZE;
		}

		if (status == TDEFL_STATUS_DONE)
		{
			// Compression completed successfully.
			break;
		}
		else if (status != TDEFL_STATUS_OKAY)
		{
			// Compression somehow failed.
			fprintf(stderr, "tdefl_compress() failed with status %i!\n", status);
			g_free(g_deflator);
			return 0;
		}
	}

	fprintf_(stderr, "Total input bytes: %u\n", (mz_uint32)total_in);
	fprintf_(stderr, "Total output bytes: %u\n", (mz_uint32)total_out);

	out_size = (long long)total_out;

	fclose(in_uncompr);
	fclose(out_compr);

	if (keep_tempfile == 1)
	{
		char *in2;
		in2 = g_strdup_printf("%s.tmptmp", in);
		tempfile_rename("", in, in2);
		g_free(in2);
		tempfile_rename("", out, in);
	}
	else
	{
		tempfile_unlink("", in);
		tempfile_rename("", out, in);
	}

	g_free(g_deflator);

	return out_size;
}
#endif


void generate_combined_index_file(FILE *towni, FILE *streeti, FILE *out)
{
#ifdef MAPTOOL_USE_SQL
	fseek(towni, 0, SEEK_END);
	fseek(streeti, 0, SEEK_END);
	long long towni_size = (long long)ftello(towni);
	long long streeti_size = (long long)ftello(streeti);
	fprintf(stderr, "ftell towns=%lld\n", towni_size);
	fprintf(stderr, "ftell streets=%lld\n", streeti_size);

	fseek(towni, 0, SEEK_SET);
	fseek(streeti, 0, SEEK_SET);

	// size
	fwrite(&streeti_size, sizeof(long long), 1, out);
	// append street index file
	filecopy(streeti, out);
	// append town index file
	filecopy(towni, out);

#endif
}

char* get_town_name_recursive(long long border_id, int level, GList* man_borders, int country_id_of_town, const char *town_name)
{
	char *ret = NULL;
	char *ret2 = NULL;
	char *str2 = NULL;
	int rc = 0;
	int admin_level;
	long long parent_rel_id = 0;
	long long my_rel_id = 0;
	struct coord c;
	struct country_table *result = NULL;
	double lat, lon;

	// fprintf(stderr, "get town name:bid:%lld level:%d country_id:%d\n", border_id, level, country_id_of_town);

	if ((border_id == 0) && (town_name))
	{
		if (country_id_of_town != 999)
		{
			result = osm_process_item_by_country_id(country_id_of_town);
		}

		if (result)
		{
			// fprintf(stderr,"== town recursive by country id 1 == country_id:%d country_name:%s townname:%s ==\n", result->countryid, result->names, town_name);
			if (result->names)
			{
				if (strcmp(town_name, result->names))
				{
					ret2 = g_strdup_printf("%s, %s", town_name, result->names);
					ret = ret2;
				}
				else
				{
					ret2 = g_strdup_printf("%s", town_name);
					ret = ret2;
				}
				return ret;
			}
		}
	}
	else
	{

	// select this boundary
	sqlite3_bind_int64(stmt_bd_005, 1, border_id);
	rc = sqlite3_step(stmt_bd_005);
	switch (rc)
	{
		case SQLITE_DONE:
			break;
		case SQLITE_ROW:
			// rel_id, parent_rel_id, name
			my_rel_id = sqlite3_column_int64(stmt_bd_005, 0);
			parent_rel_id = sqlite3_column_int64(stmt_bd_005, 1);
			if (my_rel_id < -1)
			{
				// manual country border names are excluded (--> shitty crappy hack fix)
				ret = NULL;
			}
			else
			{
				ret = g_strdup_printf("%s", sqlite3_column_text(stmt_bd_005, 2));
			}
			admin_level = sqlite3_column_int(stmt_bd_005, 3);
			lat = sqlite3_column_double(stmt_bd_005, 4);
			lon = sqlite3_column_double(stmt_bd_005, 5);
			sqlite3_reset(stmt_bd_005);

			// fprintf(stderr, "get town name:parentrel_id=%lld my_rel_id=%lld ret=%s admin_level=%d\n", parent_rel_id, my_rel_id, ret, admin_level);

			if (level < 12)
			{
				// fprintf(stderr, "call get_town_name_recursive townname:%s\n", town_name);
				str2 = get_town_name_recursive(parent_rel_id, (level + 1), man_borders, country_id_of_town, town_name);
				// fprintf(stderr, "ret2=%s %p\n", str2, str2);
				if (str2)
				{
					if ((ret) && (strcmp(ret, "(null)")) && (strcmp(ret, str2)))
					{
						// fprintf(stderr, "ret=%s str2=%s\n", ret, str2);
						// only non-null townname strings
						ret2 = g_strdup_printf("%s, %s", ret, str2);
					}
					else
					{
						// fprintf(stderr, "str2=%s\n", str2);
						ret2 = g_strdup_printf("%s", str2);
					}
					g_free(ret);
					g_free(str2);
					ret = ret2;
				}
				else if (admin_level > 2)
				{
					if ((lat != 999) && (lon != 999))
					{
						if (country_id_of_town != 999)
						{
							result = osm_process_item_by_country_id(country_id_of_town);
						}

						if (result)
						{
							// fprintf(stderr,"== town recursive by country id 2 == country_id:%d country_name:%s townname:%s ==\n", result->countryid, result->names, ret);
							if (result->names)
							{
								if ((ret) && (strcmp(ret, "(null)")))
								{
									// only non-null townname strings
									ret2 = g_strdup_printf("%s, %s", ret, result->names);
								}
								else
								{
									ret2 = g_strdup_printf("%s", result->names);
								}
								g_free(ret);
								ret = ret2;
							}
						}
						else
						{
							// recursion has ended before admin_level 2 --> try to connect to manual country border
							c.x = transform_from_geo_lon(lon);
							c.y = transform_from_geo_lat(lat);
							result = osm_process_town_by_manual_country_borders(man_borders, &c);
							if (result)
							{
								// fprintf(stderr,"== town recursive by manual_country_borders == country_id:%d country_name:%s townname:%s ==\n", result->countryid, result->names, ret);
								if (result->names)
								{
									if ((ret) && (strcmp(ret, "(null)")))
									{
										// only non-null townname strings
										ret2 = g_strdup_printf("%s, %s", ret, result->names);
									}
									else
									{
										ret2 = g_strdup_printf("%s", result->names);
									}
									g_free(ret);
									ret = ret2;
								}
							}
						}
					}
				}
			}

			// check again for (null) string
			//if ((!ret)||(!strcmp(ret, "(null)")))
			//{
			//	ret2 = g_strdup(" ");
			//	g_free(ret);
			//	ret = ret2;
			//}

			break;
		default:
			fprintf(stderr, "SQL Error: %d\n", rc);
			break;
	}

	if (level == 0)
	{
		sqlite3_reset(stmt_bd_005);
	}

	}

	// fprintf(stderr, "return level=%d border_id=%lld ret=%s p.ret=%p\n", level, border_id, ret, ret);
	return ret;
}

void generate_town_index_file(FILE *out, GList *man_borders)
{
#ifdef MAPTOOL_USE_SQL

	struct town_index_data_block db;
	struct town_index_index_block_start is;
	struct town_index_index_block ib;
	char *townname = NULL;
	char *townname2 = NULL;
	long long town_index_pos1 = 0;
	long long town_index_pos2 = 0;
	long long last_len = 0;
	long long border_id = 0;
	int town_count = 0;
	int index_blocks;
	int index_block_towns;
	int i;
	int first;
	int rc = 0;
	int current_index_block;
	int current_index_block_old;
	char tmp_letter[TOWN_INDEX_TOWN_NAME_SIZE];
	char *newfilename = NULL;
	char *newfilename_compr = NULL;

	int chunkSize;
	int stringLength;
	int ii22;

	FILE *town_index_index = NULL;
	FILE *town_index_index_data_block = NULL;

	struct coord c;
	double lat;
	double lon;

	// init compression for towns -------------------
	// init compression for towns -------------------

	/* s_s_outbuf = g_malloc(s_OUT_BUF_SIZE); */
	t_s_outbuf = g_malloc0(t_OUT_BUF_SIZE);
	/* s_s_inbuf = g_malloc(s_IN_BUF_SIZE); */
	t_s_inbuf = g_malloc0(t_IN_BUF_SIZE);

	IN_BUF_SIZE = t_IN_BUF_SIZE;
	s_inbuf = t_s_inbuf;
	OUT_BUF_SIZE = t_OUT_BUF_SIZE;
	s_outbuf = t_s_outbuf;
	COMP_OUT_BUF_SIZE = t_OUT_BUF_SIZE;

	// init compression for towns -------------------
	// init compression for towns -------------------




	purge_unused_towns();


	sqlite3_step(stmt_town_sel006);
	town_count = sqlite3_column_int(stmt_town_sel006, 0);
	sqlite3_reset(stmt_town_sel006);


	// calculate number of index blocks ------------------------------------------
	// calculate number of index blocks ------------------------------------------
	//
	index_blocks = 2;
	//
#if 0
	if (town_count > 1000000)
	{
		index_blocks = 1000; // investigate why this cant be higher?? towns will be broken if it is higher ???????? ****** ???????
	}
	else if (town_count > 100000)
	{
		index_blocks = 100; // investigate why this cant be higher?? towns will be broken if it is higher ???????? ****** ???????
	}
	else if (town_count > 10000)
	{
		index_blocks = 50; // investigate why this cant be higher?? towns will be broken if it is higher ???????? ****** ???????
	}
	else if (town_count > 2000)
	{
		index_blocks = 20;
	}
#endif

#if 1
	index_blocks = (town_count / 100); // only 100 towns per block
	if (index_blocks == 0)
	{
		index_blocks = 1;
	}

	if ((index_blocks * 100) < town_count)
	{
		index_blocks++;
	}

	if (index_blocks > 4000)
	{
		index_blocks = 4000;
	}
#endif
	//
	// calculate number of index blocks ------------------------------------------
	// calculate number of index blocks ------------------------------------------


	is.count_of_index_blocks = index_blocks;
	ib.offset = sizeof(is.count_of_index_blocks) + is.count_of_index_blocks * sizeof(struct town_index_index_block); // start offset = index size
	town_index_index = tempfile("", "town_index_index", 1);

	index_block_towns = (town_count / index_blocks);
	if ((index_block_towns * index_blocks) < town_count)
	{
		index_block_towns++;
	}

	fprintf_(stderr, "towns per block=%d\n", index_block_towns);

	fprintf_(stderr, "index size=%d\n", ib.offset);

	fprintf_(stderr, "ftell=%d\n", ftell(town_index_index));
	// remember pos1
	town_index_pos1 = ftello(town_index_index);
	fwrite(&is, sizeof(struct town_index_index_block_start), 1, town_index_index);
	// remember pos2
	town_index_pos1 = ftello(town_index_index);
	fprintf_(stderr, "ftell=%d\n", ftell(town_index_index));

	current_index_block = 1;
	current_index_block_old = 0;
	ib.len = 0;
	i = -1;
	first = 1;

	// loop thru all the towns
	do
	{
		rc = sqlite3_step(stmt_town_sel005);
		switch (rc)
		{
			case SQLITE_DONE:
			break;

			case SQLITE_ROW:
			i++;
			townname2 = NULL;
			db.town_id = sqlite3_column_int64(stmt_town_sel005, 0);
			db.country_id = sqlite3_column_int(stmt_town_sel005, 1);
			border_id = sqlite3_column_int64(stmt_town_sel005, 3);

			//fprintf(stderr, "indextown_0:townid=%lld country_id=%d borderid=%lld townname=%s\n", db.town_id, db.country_id, border_id, sqlite3_column_text(stmt_town_sel005, 2));

			if (border_id != -1)
			{
				townname = get_town_name_recursive(border_id, 0, man_borders, db.country_id, sqlite3_column_text(stmt_town_sel005, 2));
				//fprintf(stderr, "indextown_1:%s pnt:%p\n", townname, (int)townname);
			}
			else
			{
				townname = NULL;
			}


			if (townname == NULL)
			{
				//fprintf(stderr, "indextown_2.a\n");
				// ok see if we can assign a manual border to this town
				if ((db.town_id != 0) && (db.town_id != -1))
				{
					// fprintf(stderr, "indextown_2.a.1\n");
					if (db.country_id != 999)
					{
						// fprintf(stderr, "indextown_2.a.2\n");
						// try to find a manual country dummy-border-id
						border_id = osm_process_town_by_manual_country_id(man_borders, db.country_id);
						if (border_id != -1)
						{
							townname = get_town_name_recursive(border_id, 0, man_borders, db.country_id, sqlite3_column_text(stmt_town_sel005, 2));
							if (townname)
							{
								if (strcmp(townname,sqlite3_column_text(stmt_town_sel005, 2)))
								{
									townname2 = g_strdup_printf("%s, %s", sqlite3_column_text(stmt_town_sel005, 2), townname);
									g_free(townname);
									townname = townname2;
									townname2 = NULL;
									// fprintf(stderr, "indextown_2.1a:%s\n", townname);
								}
								else
								{
									townname2 = g_strdup_printf("%s", townname);
									g_free(townname);
									townname = townname2;
									townname2 = NULL;
									// fprintf(stderr, "indextown_2.1b:%s\n", townname);
								}
							}
						}
					}
					else
					{
						// fprintf(stderr, "indextown_2.a.7\n");

						// try to find a manual country dummy-border-id
						lat = sqlite3_column_double(stmt_town_sel005, 4);
						lon = sqlite3_column_double(stmt_town_sel005, 5);
						c.x = transform_from_geo_lon(lon);
						c.y = transform_from_geo_lat(lat);

						border_id = osm_process_street_by_manual_country_borders(man_borders, &c);
						if (border_id != -1)
						{
							// fprintf(stderr, "indextown_2.a.8:bid=%lld\n", border_id);

							townname = get_town_name_recursive(border_id, 0, man_borders, db.country_id, sqlite3_column_text(stmt_town_sel005, 2));
							if (townname)
							{
								// fprintf(stderr, "indextown_2.a.9\n");

								townname2 = g_strdup_printf("%s, %s", sqlite3_column_text(stmt_town_sel005, 2), townname);
								g_free(townname);
								townname = townname2;
								townname2 = NULL;
								// fprintf(stderr, "indextown_2.2:%s\n", townname);
							}
						}
					}
				}
			}

			if (townname == NULL)
			{
				townname2 = g_strdup_printf("%s", sqlite3_column_text(stmt_town_sel005, 2));
				//fprintf(stderr, "indextown_3:%s\n", townname2);
			}
			else
			{
				townname2 = townname;
				// townname2 = g_strdup_printf("%s", townname); // dont use the column result here, or string will be double!
				// g_free(townname);
			}

			fprintf_(stderr, "indextown99:%s\n", townname2);
			fprintf_(stderr, "i=%d\n", i);
			fprintf_(stderr, "block    =%d\n", current_index_block);
			fprintf_(stderr, "block old=%d\n", current_index_block_old);

			if ((i + 1) > index_block_towns)
			{
				// start new index data block
				i = 0;
				current_index_block++;
				fprintf_(stderr, "incr block=%d\n", current_index_block);
			}

			if (current_index_block != current_index_block_old)
			{

				if (first != 1)
				{
					// close old datafile
					fclose(town_index_index_data_block);
					town_index_index_data_block = NULL;

					if (USE_STREET_INDEX_COMPRESSION == 1)
					{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
						ib.len = compress_file(newfilename, newfilename_compr, global_keep_tmpfiles);
#endif
					}

					// append to indexfile
					fprintf_(stderr, "first_id=%lld offset=%lld len=%lld\n", ib.first_id, ib.offset, ib.len);
					fwrite(&ib, sizeof(struct town_index_index_block), 1, town_index_index);

					if (newfilename)
					{
						g_free(newfilename);
						newfilename = NULL;
					}

					if (newfilename_compr)
					{
						g_free(newfilename_compr);
						newfilename_compr = NULL;
					}
				}

				current_index_block_old = current_index_block;
				ib.first_id = db.town_id;
				ib.offset = ib.offset + ib.len;

				// open new datafile
				newfilename = g_strdup_printf("town_index_index_%d", current_index_block);
				fprintf_(stderr, "new data file: %s first_id=%lld\n", newfilename, ib.first_id);
				newfilename_compr = g_strdup_printf("town_index_index_compr_%d", current_index_block);
				town_index_index_data_block = tempfile("", newfilename, 1);
				fprintf_(stderr, "town index file %d\n", current_index_block);

				ib.len = 0;
			}


			// now check if we need to split the string into parts
			if ((strlen(townname2) + 1) > TOWN_INDEX_TOWN_NAME_SIZE)
			{
				fprintf_(stderr, " block-split: START\n");
				chunkSize = TOWN_INDEX_TOWN_NAME_SIZE - 1;
				stringLength = strlen(townname2);
				for (ii22 = 0; ii22 < stringLength ; ii22 += chunkSize)
				{
					if (ii22 + chunkSize > stringLength)
					{
						chunkSize = stringLength - ii22;
						db.town_name[chunkSize] = '\0'; // make sure string is terminated
					}
					strncpy(&db.town_name, (townname2 + ii22), chunkSize);
					db.town_name[TOWN_INDEX_TOWN_NAME_SIZE - 1] = '\0'; // make sure string is terminated
					if (ii22 > 0)
					{
						// set "split"-marker
						db.town_id = 0;
						db.country_id = 0; // setting this to zero is actually not needed
					}
					ib.len = ib.len + sizeof(struct town_index_data_block);
					fprintf_(stderr, "->block-split: town_id=%lld country_id=%d town_name=%s\n", db.town_id, db.country_id, db.town_name);
					fwrite(&db, sizeof(struct town_index_data_block), 1, town_index_index_data_block);
				}
				fprintf_(stderr, " block-split: END\n");
				g_free(townname2);
			}
			else
			{
				strncpy(&db.town_name, townname2, TOWN_INDEX_TOWN_NAME_SIZE);
				g_free(townname2);
				db.town_name[TOWN_INDEX_TOWN_NAME_SIZE - 1]= '\0'; // make sure string is terminated

				ib.len = ib.len + sizeof(struct town_index_data_block);
				fwrite(&db, sizeof(struct town_index_data_block), 1, town_index_index_data_block);
			}

			if (first == 1)
			{
				first = 0;
			}

			break;

			default:
			fprintf(stderr, "SQL Error: %d\n", rc);
			break;
		}
	}
	while (rc == SQLITE_ROW);

	sqlite3_reset(stmt_town_sel005);

	fprintf_(stderr, "end i=%d\n", i);
	fprintf_(stderr, "end block    =%d\n", current_index_block);
	fprintf_(stderr, "end block old=%d\n", current_index_block_old);

	// rest of the towns
	if (i > -1)
	{
		if (town_index_index_data_block)
		{
			fclose(town_index_index_data_block);
			town_index_index_data_block = NULL;
		}

		if (USE_STREET_INDEX_COMPRESSION == 1)
		{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
			ib.len = compress_file(newfilename, newfilename_compr, global_keep_tmpfiles);
#endif
		}

		// append to indexfile
		fprintf_(stderr, "(last)first_id=%lld offset=%lld len=%lld\n", ib.first_id, ib.offset, ib.len);
		fwrite(&ib, sizeof(struct town_index_index_block), 1, town_index_index);
	}

	fprintf_(stderr, "real num of town index blocks=%d\n", current_index_block);
	fprintf_(stderr, "real size of town index:ftell=%lld\n", (long long)ftello(town_index_index));
	fprintf_(stderr, "real count index blocks      =%lld\n", (long long)is.count_of_index_blocks);

	if (town_index_index != NULL)
	{
		fclose(town_index_index);
		town_index_index = NULL;
	}

	// write corrections back to file -----------------------------------
	// write corrections back to file -----------------------------------
	tempfile_rename("", "town_index_index", "town_index_index_ORIG");
	FILE *fcorro = tempfile("", "town_index_index_ORIG", 0);
	FILE *fcorr = tempfile("", "town_index_index", 1);
	// part 1
	fwrite(&is, sizeof(struct town_index_index_block_start), 1, fcorr);

	// part 2
	struct town_index_index_block *ti_ib_mem1 = NULL; // mem pointer, to free the mem
	struct town_index_index_block *ti_ib1 = NULL; // data pointer, to read data
	long long s1 = sizeof(struct town_index_index_block) * is.count_of_index_blocks;
	fprintf_(stderr, "s1 size=%lld\n", (long long)s1);
	ti_ib_mem1 = g_malloc0(s1);
	fseeko(fcorro, sizeof(struct town_index_index_block_start), SEEK_SET);
	fprintf_(stderr, "size of town_index_index_block_start=%lld\n", (long long)sizeof(struct town_index_index_block_start));
	fread(ti_ib_mem1, sizeof(struct town_index_index_block), is.count_of_index_blocks, fcorro);

	fprintf_(stderr, "read count index blocks      =%lld\n", (long long)is.count_of_index_blocks);

	// HINT: ok this is crappy, why don't u fix it if you want? :-)
	int jj;
	long long offset_add = 0;
	for (jj=0;jj<is.count_of_index_blocks;jj++)
	{
		ti_ib1 = ti_ib_mem1 + jj;

		// len=266 offset=32
		fprintf_(stderr, "tblock num      =%d len=%lld offset=%lld\n", jj, ti_ib1->len, ti_ib1->offset);
		// correct offset for all blocks
		if (jj == 0)
		{
			ti_ib1->offset = sizeof(struct town_index_index_block_start) + (is.count_of_index_blocks * sizeof(struct town_index_index_block));
			offset_add = ti_ib1->offset;
		}
		else
		{
			ti_ib1->offset = offset_add;
		}
		offset_add = offset_add + ti_ib1->len;

		fprintf_(stderr, "tblock num(corr)=%d add=%lld len=%lld offset=%lld\n", jj, offset_add, ti_ib1->len, ti_ib1->offset);
	}

	fwrite(ti_ib_mem1, sizeof(struct town_index_index_block), is.count_of_index_blocks, fcorr);
	fclose(fcorr);
	fclose(fcorro);
	g_free(ti_ib_mem1);
	if (global_keep_tmpfiles != 1)
	{
		tempfile_unlink("", "town_index_index_ORIG");
	}
	// write corrections back to file -----------------------------------
	// write corrections back to file -----------------------------------



	if (town_index_index != NULL)
	{
		fclose(town_index_index);
	}

	if (town_index_index_data_block != NULL)
	{
		fclose(town_index_index_data_block);
	}

	if (newfilename)
	{
		g_free(newfilename);
		newfilename = NULL;
	}

	if (newfilename_compr)
	{
		g_free(newfilename_compr);
		newfilename_compr = NULL;
	}





	// put all parts together
	town_index_index = tempfile("", "town_index_index", 0);
	filecopy(town_index_index, out);
	fclose(town_index_index);
	if (global_keep_tmpfiles != 1)
	{
		tempfile_unlink("", "town_index_index");
	}

	for (i=1;i < (current_index_block + 2);i++)
	{
		fprintf_(stderr, "index block #%d\n", i);
		newfilename = g_strdup_printf("town_index_index_%d", i);
		town_index_index_data_block = tempfile("", newfilename, 0);

		if (town_index_index_data_block)
		{
			fprintf_(stderr, "using: index block #%d in %s\n", i, newfilename);
			filecopy(town_index_index_data_block, out);
			fclose(town_index_index_data_block);
			if (global_keep_tmpfiles != 1)
			{
				tempfile_unlink("", newfilename);
			}
		}

		if (newfilename)
		{
			g_free(newfilename);
			newfilename = NULL;
		}
	}

#endif
}





void generate_street_index_file(FILE *out)
{
#ifdef MAPTOOL_USE_SQL
	int rc = 0;
	int i;
	struct streets_index_data_block db;
	struct streets_index_index_block_start is;
	struct streets_index_index_block ib;
	long long last_len = 0;
	static const char *alpha = "abcdefghijklmnopqrstuvwxyz";
	char tmp_letter[STREET_INDEX_STREET_NAME_SIZE];
	char *newfilename = NULL;
	char *newfilename_compr = NULL;
	int count_of_blocks;
	int do_rest;
	int num1;
	int num2;
	char waytype;

	FILE *street_index_index;
	FILE *street_index_index_data_block;

	// init compression for streets ----------------------
	// init compression for streets ----------------------

	s_s_outbuf = g_malloc0(s_OUT_BUF_SIZE);
	/* t_s_outbuf = g_malloc(t_OUT_BUF_SIZE); */
	s_s_inbuf = g_malloc0(s_IN_BUF_SIZE);
	/* t_s_inbuf = g_malloc(t_IN_BUF_SIZE); */

	IN_BUF_SIZE = s_IN_BUF_SIZE;
	s_inbuf = s_s_inbuf;
	OUT_BUF_SIZE = s_OUT_BUF_SIZE;
	s_outbuf = s_s_outbuf;
	COMP_OUT_BUF_SIZE = s_OUT_BUF_SIZE;

	// init compression for streets ----------------------
	// init compression for streets ----------------------


	is.count_of_index_blocks = 703; // 26+1 letters ((26+1)*26 + 1) = 703
	ib.offset = sizeof (is.count_of_index_blocks) + is.count_of_index_blocks * sizeof(struct streets_index_index_block); // start offset = index size
	street_index_index = tempfile("", "street_index_index", 1);
	fprintf_(stderr, "index size=%d\n", ib.offset);

	fprintf_(stderr, "ftell=%d\n", ftell(street_index_index));
	fwrite(&is, sizeof(struct streets_index_index_block_start), 1, street_index_index);
	fprintf_(stderr, "ftell=%d\n", ftell(street_index_index));

	// fprintf(stderr, "len=%d\n", strlen(alpha));

	count_of_blocks = 702;
	do_rest = 0;
	num1 = 1;
	num2 = 0;
	for (i=0; i < count_of_blocks; i++)
	{
		num2++;
		do_rest = 0;
		if (num2 == 27)
		{
			do_rest = 1;
		}
		else if (num2 > 27)
		{
			num2 = 1;
			num1++;
		}

		fprintf_(stderr, "i=%d num1=%d num2=%d\n", i, num1, num2);

		if (do_rest)
		{
			sprintf(tmp_letter, "%c", alpha[num1 - 1]);
		}
		else
		{
			sprintf(tmp_letter, "%c%c", alpha[num1 - 1], alpha[num2 - 1]);
		}
		fprintf_(stderr, "letter=%s\n", tmp_letter);

		ib.first_letter = alpha[num1 - 1];
		ib.len = 0;

		newfilename = g_strdup_printf("street_index_index_%d", i);
		newfilename_compr = g_strdup_printf("street_index_index_compr_%d", i);
		street_index_index_data_block = tempfile("", newfilename, 1);


		// ------ TIMER -------
		time_t start_tt_4, end_tt_4;
		double diff_tt_4;
		char outstring_4[200];
		// ------ TIMER -------

		// ------ TIMER -------
		time(&start_tt_4);
		// ------ TIMER -------

		// loop thru all the streets that match '<letter>...'
		sqlite3_bind_text(stmt_sel003, 1, tmp_letter, -1, SQLITE_STATIC);
		do
		{
			rc = sqlite3_step(stmt_sel003);
			switch (rc)
			{
				case SQLITE_DONE:
				break;
				case SQLITE_ROW:
				db.town_id = sqlite3_column_int64(stmt_sel003, 0);
				db.lat = transform_from_geo_lat(sqlite3_column_double(stmt_sel003, 1));
				db.lon = transform_from_geo_lon(sqlite3_column_double(stmt_sel003, 2));
				strncpy(&db.street_name, sqlite3_column_text(stmt_sel003, 3), (STREET_INDEX_STREET_NAME_SIZE - 1));
				waytype = sqlite3_column_int(stmt_sel003, 5);
				db.street_type = (char)waytype;
				db.street_name[(STREET_INDEX_STREET_NAME_SIZE - 2)]= '\0'; // make sure string is terminated

				if ((&db.street_name)&&(strlen(&db.street_name) > 1))
				{
					ib.len = ib.len + sizeof(struct streets_index_data_block);
					//fprintf(stderr ,"gen_street_index id:%lld lat:%d lon:%d name:%s\n", db.town_id, db.lat, db.lon, db.street_name);
					fwrite(&db, sizeof(struct streets_index_data_block), 1, street_index_index_data_block);
				}
				else
				{
					//fprintf(stderr, "streetname1 Error: %s\n", &db.street_name);
				}

				break;
				default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
			}
		}
		while (rc == SQLITE_ROW);

		sqlite3_reset(stmt_sel003);
		fclose(street_index_index_data_block);

		// ------ TIMER -------
		time(&end_tt_4);
		diff_tt_4 = difftime(end_tt_4, start_tt_4);
		convert_to_human_time(diff_tt_4, outstring_4);
		fprintf_(stderr, "-TIME-IND-001: %s\n", outstring_4);
		// ------ TIMER -------


		// ------ TIMER -------
		time(&start_tt_4);
		// ------ TIMER -------

		sqlite3_bind_int(stmt_sel003u, 1, (i + 1));
		sqlite3_bind_text(stmt_sel003u, 2, tmp_letter, -1, SQLITE_STATIC);
		sqlite3_step(stmt_sel003u);
		sqlite3_reset(stmt_sel003u);
		// sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);


		// ------ TIMER -------
		time(&end_tt_4);
		diff_tt_4 = difftime(end_tt_4, start_tt_4);
		convert_to_human_time(diff_tt_4, outstring_4);
		fprintf_(stderr, "-TIME-IND-002: %s\n", outstring_4);
		// ------ TIMER -------


		// ------ TIMER -------
		time(&start_tt_4);
		// ------ TIMER -------


		if (USE_STREET_INDEX_COMPRESSION == 1)
		{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
			ib.len = compress_file(newfilename, newfilename_compr, global_keep_tmpfiles);
#endif
		}

		// ------ TIMER -------
		time(&end_tt_4);
		diff_tt_4 = difftime(end_tt_4, start_tt_4);
		convert_to_human_time(diff_tt_4, outstring_4);
		fprintf_(stderr, "-TIME-IND-COMPR: %s\n", outstring_4);
		// ------ TIMER -------


		last_len = ib.len;
		fprintf_(stderr ,"letter=%c offset=%lld len=%lld\n", ib.first_letter, ib.offset, ib.len);
		fprintf_(stderr, "ftell=%d\n", ftell(street_index_index));
		fwrite(&ib, sizeof(struct streets_index_index_block), 1, street_index_index);
		fprintf_(stderr, "ftell=%d\n", ftell(street_index_index));
		ib.offset = ib.offset + last_len;

		if (newfilename)
		{
			g_free(newfilename);
			newfilename = NULL;
		}

		if (newfilename_compr)
		{
			g_free(newfilename_compr);
			newfilename_compr = NULL;
		}
	}

	// rest of the streets
	fprintf_(stderr, "rest of letters\n");

	ib.first_letter = 65; // dummy "A"
	ib.len = 0;

	newfilename = g_strdup_printf("street_index_index_%d", i);
	newfilename_compr = g_strdup_printf("street_index_index_compr_%d", i);
	street_index_index_data_block = tempfile("", newfilename, 1);

	do
	{
		rc = sqlite3_step(stmt_sel004);
		switch (rc)
		{
			case SQLITE_DONE:
			break;
			case SQLITE_ROW:
			db.town_id = sqlite3_column_int64(stmt_sel004, 0);
			db.lat = transform_from_geo_lat(sqlite3_column_double(stmt_sel004, 1));
			db.lon = transform_from_geo_lon(sqlite3_column_double(stmt_sel004, 2));

			strncpy(&db.street_name, sqlite3_column_text(stmt_sel004, 3), (STREET_INDEX_STREET_NAME_SIZE - 1));
			waytype = sqlite3_column_int(stmt_sel004, 5);
			db.street_type = (char)waytype;
			db.street_name[(STREET_INDEX_STREET_NAME_SIZE - 2)]= '\0'; // make sure string is terminated

			if ((&db.street_name)&&(strlen(&db.street_name) > 1))
			{
				ib.len = ib.len + sizeof(struct streets_index_data_block);
				// fprintf(stderr ,"id:%lld lat:%d lon:%d name:%s waytype:%d\n", db.town_id, db.lat, db.lon, db.street_name, waytype);
				fwrite(&db, sizeof(struct streets_index_data_block), 1, street_index_index_data_block);
			}
			else
			{
				//fprintf(stderr, "streetname2 Error: %s\n", &db.street_name);
			}

			break;

			default:
			fprintf(stderr, "SQL Error: %d\n", rc);
			break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_sel004);
	fclose(street_index_index_data_block);

	if (USE_STREET_INDEX_COMPRESSION == 1)
	{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
		ib.len = compress_file(newfilename, newfilename_compr, global_keep_tmpfiles);
#endif
	}

	last_len = ib.len;
	fprintf_(stderr ,"letter=%c offset=%lld len=%lld\n", ib.first_letter, ib.offset, ib.len);
	fprintf_(stderr, "ftell=%d\n", ftell(street_index_index));
	fwrite(&ib, sizeof(struct streets_index_index_block), 1, street_index_index);
	fprintf_(stderr, "ftell=%d\n", ftell(street_index_index));
	// ib.offset = ib.offset + last_len;


	fclose(street_index_index);

	if (newfilename)
	{
		g_free(newfilename);
		newfilename = NULL;
	}

	if (newfilename_compr)
	{
		g_free(newfilename_compr);
		newfilename_compr = NULL;
	}

	// put all parts together
	street_index_index = tempfile("", "street_index_index", 0);
	filecopy(street_index_index, out);
	fclose(street_index_index);

	if (global_keep_tmpfiles != 1)
	{
		tempfile_unlink("", "street_index_index");
	}

	for (i=0;i < (is.count_of_index_blocks + 1);i++)
	{
		//fprintf(stderr, "cat #%d\n", i);
		newfilename = g_strdup_printf("street_index_index_%d", i);
		street_index_index_data_block = tempfile("", newfilename, 0);

		if (street_index_index_data_block)
		{
			filecopy(street_index_index_data_block, out);
			fclose(street_index_index_data_block);
			if (global_keep_tmpfiles != 1)
			{
				tempfile_unlink("", newfilename);
			}
		}

		if (newfilename)
		{
			g_free(newfilename);
			newfilename = NULL;
		}
	}
#endif
}

