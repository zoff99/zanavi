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
#include <stdio.h>
#include <time.h>
#include <string.h>
#include "maptool.h"
#include "attr.h"

struct relations
{
	GHashTable *member_hash[3];
};

struct relations_func
{
	void (*func)(void *func_priv, void *relation_priv, struct item_bin *member, void *member_priv);
	void *func_priv;
};

struct relations_member
{
	osmid memberid;
	void *relation_priv, *member_priv;
	struct relations_func *func;
};

static guint relations_member_hash(gconstpointer key)
{
	const struct relations_member *memb = key;
	return (memb->memberid >> 32) ^ (memb->memberid & 0xffffffff);
}

static gboolean relations_member_equal(gconstpointer a, gconstpointer b)
{
	const struct relations_member *memba = a;
	const struct relations_member *membb = b;
	return (memba->memberid == membb->memberid);
}

struct relations *
relations_new(void)
{
	struct relations *ret=g_new(struct relations, 1);
	int i;

	for (i = 0; i < 3; i++)
	{
		ret->member_hash[i] = g_hash_table_new_full(relations_member_hash, relations_member_equal, NULL, NULL);
	}
	return ret;
}

struct relations_func *
relations_func_new(void(*func)(void *func_priv, void *relation_priv, struct item_bin *member, void *member_priv), void *func_priv)
{
	struct relations_func *relations_func=g_new(struct relations_func, 1);
	relations_func->func = func;
	relations_func->func_priv = func_priv;
	return relations_func;
}

void relations_add_func(struct relations *rel, struct relations_func *func, void *relation_priv, void *member_priv, int type, osmid id)
{
	//fprintf(stderr,"relations_add_func:001\n");

	GHashTable *member_hash = rel->member_hash[type - 1];
	struct relations_member *memb=g_new(struct relations_member, 1);

	memb->memberid = id;
	memb->relation_priv = relation_priv;
	memb->member_priv = member_priv;
	memb->func = func;
	g_hash_table_insert(member_hash, memb, g_list_append(g_hash_table_lookup(member_hash, memb), memb));
}

void relations_process(struct relations *rel, FILE *nodes, FILE *ways, FILE *relations)
{
	char buffer[128];
	struct item_bin *ib = (struct item_bin *) buffer;
	long long *id;
	struct coord *c = (struct coord *) (ib + 1), cn =
	{ 0, 0 };
	struct node_item *ni;
	GList *l;

	time_t start_tt, end_tt;
	double diff_tt;
	double diff2_tt;
	long long size_in;
	long long pos_in;
	int _c = 0;
	int _e = 8000000;

	//fprintf(stderr,"relations_process:001\n");

	if (nodes)
	{
		//fprintf(stderr,"relations_process:002 nodes\n");

		long long pos_now = (long long)ftello(nodes); // 64bit
		fseeko(nodes, 0, SEEK_END);
		size_in = (long long)ftello(nodes); // 64bit
		fprintf(stderr, "relations_process: pos nodes file=%lld\n", pos_now);
		fprintf(stderr, "relations_process:size nodes file=%lld\n", size_in);
		// seek to start of file
		fseeko(nodes, 0, SEEK_SET);
		// reset timer
		diff2_tt = 0;
		_c = 0;
		time(&start_tt);

		item_bin_init(ib, type_point_unkn);
		item_bin_add_coord(ib, &cn, 1);
		item_bin_add_attr_longlong(ib, attr_osm_nodeid, 0);
		id = item_bin_get_attr(ib, attr_osm_nodeid, NULL);
		while ((ni = read_node_item(nodes, 0)))
		{
			_c++;

			*id = ni->id;
			*c = ni->c;
			l = g_hash_table_lookup(rel->member_hash[0], id);
			while (l)
			{
				struct relations_member *memb = l->data;
				memb->func->func(memb->func->func_priv, memb->relation_priv, ib, memb->member_priv);
				l = g_list_next(l);
			}

			if (_c > _e)
			{
				_c = 0;

				pos_in = ftello(nodes); // 64bit
				time(&end_tt);
				diff_tt = difftime(end_tt,start_tt);
				char outstring[200];
				char outstring2[200];
				char outstring3[200];
				convert_to_human_time(diff_tt, outstring);
				convert_to_human_bytes(pos_in, outstring2);
				convert_to_human_bytes(size_in, outstring3);
				fprintf(stderr, "-RUNTIME-LOOP-REL-PROC-NODES: %s elapsed (POS:%s of %s)\n", outstring, outstring2, outstring3);
				if (pos_in > 0)
				{
					double eta_time = (diff_tt / pos_in) * (size_in - pos_in);
					convert_to_human_time(eta_time, outstring);
					fprintf(stderr, "-RUNTIME-LOOP-REL-PROC-NODES: %s left\n", outstring);
				}
			}
		}
	}

	//fprintf(stderr,"relations_process:002.9\n");

	if (ways)
	{
		//fprintf(stderr,"relations_process:003 ways\n");

		long long pos_now = (long long)ftello(ways); // 64bit
		fseeko(ways, 0, SEEK_END);
		size_in = (long long)ftello(ways); // 64bit
		fprintf(stderr, "relations_process: pos ways file=%lld\n", pos_now);
		fprintf(stderr, "relations_process:size ways file=%lld\n", size_in);
		// seek to start of file
		fseeko(ways, 0, SEEK_SET);
		// reset timer
		diff2_tt = 0;
		_c = 0;
		time(&start_tt);

		while ((ib = read_item(ways, 0)))
		{
			_c++;

			//fprintf(stderr,"relations_process:003.1\n");

			id = item_bin_get_attr(ib, attr_osm_wayid, NULL);

			//fprintf(stderr,"********DUMP relw***********\n");
			//dump_itembin(ib);
			//fprintf(stderr,"********DUMP relw***********\n");

			//char *labelxx=item_bin_get_attr(ib, attr_name, NULL);
			//fprintf(stderr,"relations_process:003.2 %s\n", labelxx);
			//labelxx=item_bin_get_attr(ib, attr_label, NULL);
			//fprintf(stderr,"relations_process:003.3 %s\n", labelxx);

			if (id)
			{
				//fprintf(stderr,"relations_process:004 wayid:"LONGLONG_FMT"\n", id);

				l = g_hash_table_lookup(rel->member_hash[1], id);
				while (l)
				{
					//fprintf(stderr,"relations_process:005\n");
					struct relations_member *memb = l->data;
					//fprintf(stderr,"relations_process:005.1 %d\n", memb->memberid);
					memb->func->func(memb->func->func_priv, memb->relation_priv, ib, memb->member_priv);
					l = g_list_next(l);
				}
			}

			if (_c > _e)
			{
				_c = 0;

				pos_in = ftello(ways); // 64bit
				time(&end_tt);
				diff_tt = difftime(end_tt,start_tt);
				char outstring[200];
				char outstring2[200];
				char outstring3[200];
				convert_to_human_time(diff_tt, outstring);
				convert_to_human_bytes(pos_in, outstring2);
				convert_to_human_bytes(size_in, outstring3);
				fprintf(stderr, "-RUNTIME-LOOP-REL-PROC-WAYS: %s elapsed (POS:%s of %s)\n", outstring, outstring2, outstring3);
				if (pos_in > 0)
				{
					double eta_time = (diff_tt / pos_in) * (size_in - pos_in);
					convert_to_human_time(eta_time, outstring);
					fprintf(stderr, "-RUNTIME-LOOP-REL-PROC-WAYS: %s left\n", outstring);
				}
			}
		}
	}
}

