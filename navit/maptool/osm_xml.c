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
#include <math.h>
#include <time.h>
#include <unistd.h>
#include "maptool.h"

int osm_xml_get_attribute(char *xml, char *attribute, char *buffer, int buffer_size)
{
	int len = strlen(attribute);
	char *pos, *i, s, attr[len + 2];
	strcpy(attr, attribute);
	strcpy(attr + len, "=");
	pos = strstr(xml, attr);
	if (!pos)
		return 0;
	pos += len + 1;
	s = *pos++;
	if (!s)
		return 0;
	i = strchr(pos, s);
	if (!i)
		return 0;
	if (i - pos > buffer_size)
	{
		fprintf(stderr, "Buffer overflow %ld vs %d\n", (long) (i - pos), buffer_size);
		return 0;
	}
	strncpy(buffer, pos, i - pos);
	buffer[i - pos] = '\0';
	return 1;
}

static struct entity
{
	char *entity;
	char c;
} entities[] = { { "&quot;", '"' }, { "&apos;", '\'' }, { "&amp;", '&' }, { "&lt;", '<' }, { "&gt;", '>' }, { "&#34;", '"' }, { "&#39;", '\'' }, { "&#38;", '&' }, { "&#60;", '<' }, { "&#62;", '>' }, { "&#123;", '{' }, { "&#125;", '}' }, };

void osm_xml_decode_entities(char *buffer)
{
	char *pos = buffer;
	int i, len, found;

	while ((pos = strchr(pos, '&')))
	{
		found = 0;
		for (i = 0; i < sizeof(entities) / sizeof(struct entity); i++)
		{
			len = strlen(entities[i].entity);
			if (!strncmp(pos, entities[i].entity, len))
			{
				*pos = entities[i].c;
				memmove(pos + 1, pos + len, strlen(pos + len) + 1);
				found = 1;
				break;
			}
		}
		pos++;
	}
}

static int parse_tag(char *p)
{
	char k_buffer[BUFFER_SIZE];
	char v_buffer[BUFFER_SIZE];
	if (!osm_xml_get_attribute(p, "k", k_buffer, BUFFER_SIZE))
	{
		return 0;
	}
	if (!osm_xml_get_attribute(p, "v", v_buffer, BUFFER_SIZE))
	{
		return 0;
	}
	osm_xml_decode_entities(v_buffer);
	osm_add_tag(k_buffer, v_buffer);
	return 1;
}

static int parse_node(char *p)
{
	char id_buffer[BUFFER_SIZE];
	char lat_buffer[BUFFER_SIZE];
	char lon_buffer[BUFFER_SIZE];
	if (!osm_xml_get_attribute(p, "id", id_buffer, BUFFER_SIZE))
		return 0;
	if (!osm_xml_get_attribute(p, "lat", lat_buffer, BUFFER_SIZE))
		return 0;
	if (!osm_xml_get_attribute(p, "lon", lon_buffer, BUFFER_SIZE))
		return 0;
	osm_add_node(atoll(id_buffer), atof(lat_buffer), atof(lon_buffer));
	return 1;
}

static int parse_way(char *p)
{
	char id_buffer[BUFFER_SIZE];
	if (!osm_xml_get_attribute(p, "id", id_buffer, BUFFER_SIZE))
		return 0;
	osm_add_way(atoll(id_buffer));
	return 1;
}

static int parse_relation(char *p)
{
	char id_buffer[BUFFER_SIZE];
	if (!osm_xml_get_attribute(p, "id", id_buffer, BUFFER_SIZE))
		return 0;
	osm_add_relation(atoll(id_buffer));
	return 1;
}

static int parse_member(char *p)
{
	char type_buffer[BUFFER_SIZE];
	char ref_buffer[BUFFER_SIZE];
	char role_buffer[BUFFER_SIZE];
	int type;
	if (!osm_xml_get_attribute(p, "type", type_buffer, BUFFER_SIZE))
		return 0;
	if (!osm_xml_get_attribute(p, "ref", ref_buffer, BUFFER_SIZE))
		return 0;
	if (!osm_xml_get_attribute(p, "role", role_buffer, BUFFER_SIZE))
		return 0;
	if (!strcmp(type_buffer, "node"))
		type = 1;
	else if (!strcmp(type_buffer, "way"))
		type = 2;
	else if (!strcmp(type_buffer, "relation"))
		type = 3;
	else
	{
		//fprintf(stderr,"Unknown type %s\n",type_buffer);
		type = 0;
	}
	osm_add_member(type, atoll(ref_buffer), role_buffer);

	return 1;
}

static int parse_nd(char *p)
{
	char ref_buffer[BUFFER_SIZE];
	if (!osm_xml_get_attribute(p, "ref", ref_buffer, BUFFER_SIZE))
	{
		return 0;
	}
	osm_add_nd(atoll(ref_buffer));

	return 1;
}

int map_collect_data_osm(FILE *in, struct maptool_osm *osm)
{
	int size = 1024 * 5; // 5kb
	// char buffer[size];
	char *buffer_ptr;
	char *p;

	time_t start_tt, end_tt;
	double diff_tt;
	long long diff2_tt;
	long long pos_in;
	long long pos_in_local;
	int _c = 0;
	int _e = 5000000;
	int first_rel = 1;
	int first_way = 1;
	int line_length = 0;

	buffer_ptr = malloc(size);

	//sig_alrm(0);

	// reset
	pos_in = 0;
	pos_in_local = 0;
	diff2_tt = 0;

	while (fgets(buffer_ptr, size, in))
	{
		// we just read "size" bytes from "in"

		line_length = strlen(buffer_ptr);

		pos_in = pos_in + line_length;
		pos_in_local = pos_in_local + line_length;

		if (_c == 0)
		{
			time(&start_tt);
			pos_in_local = 0;
		}
		_c++;

		p = strchr(buffer_ptr, '<');
		if (!p)
		{
			//fprintf(stderr,"WARNING: wrong line %s\n", buffer_ptr);
			continue;
		}
		if (!strncmp(p, "<?xml ", 6))
		{
		}
		else if (!strncmp(p, "<osm ", 5))
		{
		}
		else if (!strncmp(p, "<bound ", 7))
		{
		}
		else if (!strncmp(p, "<node ", 6))
		{
			if (!parse_node(p))
			{
				//fprintf(stderr,"WARNING: failed to parse %s\n", buffer_ptr);
			}
			processed_nodes++;
		}
		else if (!strncmp(p, "<tag ", 5))
		{
			if (!parse_tag(p))
			{
				//fprintf(stderr,"WARNING: failed to parse %s\n", buffer_ptr);
			}
		}
		else if (!strncmp(p, "<way ", 5))
		{
			if (first_way == 1)
			{
				first_way = 0;

				flush_nodes(1, 0); // flush remaining nodes to "coords.tmp" from "osm_end_node"
				free_buffer("dummy", &node_buffer[0]); // and free the memory

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
				fprintf(stderr, "nodes processed:%lld\n", processed_nodes);

				if (! MAPTOOL_SQL_INPUT_TOO_SMALL)
				{
					// reopen
					sql_db_close();
					sql_db_open();
					sql_db_init(0);
				}

				fprintf(stderr, "SQL: (create index 003) start\n");
				sql_create_index003();
				fprintf(stderr, "SQL: (create index 003) ready\n");

				if (! MAPTOOL_SQL_INPUT_TOO_SMALL)
				{
					// reopen for indexes to be used
					sql_db_close();
					sql_db_open();
					sql_db_init(0);
				}

				fprintf(stderr, "SQL: (first way) COMMIT\n");

			}

			if (!parse_way(p))
			{
				//fprintf(stderr,"WARNING: failed to parse %s\n", buffer_ptr);
			}
			processed_ways++;
		}
		else if (!strncmp(p, "<nd ", 4))
		{
			if (!parse_nd(p))
			{
				//fprintf(stderr,"WARNING: failed to parse %s\n", buffer_ptr);
			}
		}
		else if (!strncmp(p, "<relation ", 10))
		{
			if (first_rel == 1)
			{
				first_rel = 0;

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

				fprintf(stderr, "ways processed:%lld\n", processed_ways);

				if (! MAPTOOL_SQL_INPUT_TOO_SMALL)
				{
					// reopen
					sql_db_close();
					sql_db_open();
					sql_db_init(0);
				}

				fprintf(stderr, "SQL: (create index 001) start\n");
				sql_create_index001();
				fprintf(stderr, "SQL: (create index 001) ready\n");

				if (! MAPTOOL_SQL_INPUT_TOO_SMALL)
				{
					// reopen for indexes to be used
					sql_db_close();
					sql_db_open();
					sql_db_init(0);
				}

				fprintf(stderr, "SQL: (first relation) COMMIT\n");
			}

			if (!parse_relation(p))
			{
				if (verbose_mode)
					fprintf(stderr, "WARNING: failed to parse %s\n", buffer_ptr);
			}
			processed_relations++;
		}
		else if (!strncmp(p, "<member ", 8))
		{
			if (!parse_member(p))
			{
				//fprintf(stderr,"WARNING: failed to parse %s\n", buffer_ptr);
			}
		}
		else if (!strncmp(p, "</node>", 7))
		{
			osm_end_node(osm);
		}
		else if (!strncmp(p, "</way>", 6))
		{
			osm_end_way(osm);
		}
		else if (!strncmp(p, "</relation>", 11))
		{
			osm_end_relation(osm);
		}
		else if (!strncmp(p, "</osm>", 6))
		{
		}
		else
		{
			// fprintf(stderr,"WARNING: unknown tag in %s\n", buffer_ptr);
		}

		if (_c > _e)
		{
			_c = 0;

			time(&end_tt);
			diff_tt = difftime(end_tt, start_tt);
			diff2_tt = diff2_tt + (long) diff_tt;

			char outstring[200];
			char outstring2[200];
			convert_to_human_time(diff2_tt, outstring);
			convert_to_human_bytes(pos_in, outstring2);
			fprintf(stderr, "-RUNTIME-LOOP-COLLECT-DATA: %s elapsed (%s read)\n", outstring, outstring2);
			convert_to_human_bytes((pos_in / diff2_tt), outstring2);
			fprintf(stderr, "-RUNTIME-LOOP-COLLECT-DATA: %s/s read\n", outstring2);

			convert_to_human_bytes((pos_in_local / diff_tt), outstring2);
			fprintf(stderr, "-RUNTIME-LOOP-COLLECT-DATA (local loop): %s/s read\n", outstring2);
		}
	}

	// just in case, commit all we got left over
	//if (sql_counter > 0)
	//{
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
	fprintf(stderr, "SQL: (final) COMMIT\n");
	fprintf(stderr, "relations processed:%d\n", processed_relations);

	//}

	//sig_alrm(0);
	//sig_alrm_end();

	free(buffer_ptr);

	return 1;
}

