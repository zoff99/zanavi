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
#include <string.h>
#include <ctype.h>
#include "maptool.h"

char *
osm_tag_value(struct item_bin *ib, char *key)
{
	char *tag = NULL;
	int len = strlen(key);
	while ((tag = item_bin_get_attr(ib, attr_osm_tag, tag)))
	{
		if (!strncmp(tag, key, len) && tag[len] == '=')
			return tag + len + 1;
	}
	return NULL;
}

static char *
osm_tag_name(struct item_bin *ib)
{
	return osm_tag_value(ib, "name");
}

long long *
boundary_relid(struct boundary *b)
{
	long long *id;
	if (!b)
		return 0;
	if (!b->ib)
		return 0;
	id = item_bin_get_attr(b->ib, attr_osm_relationid, NULL);
	if (id)
		return *id;
	return 0;
}

static void process_boundaries_member(void *func_priv, void *relation_priv, struct item_bin *member, void *member_priv)
{
	//fprintf(stderr,"process_boundaries_member:001\n");

	struct boundary *b = relation_priv;
	enum geom_poly_segment_type role = (long) member_priv;

	int *dup;
	dup=item_bin_get_attr(member,attr_duplicate_way,NULL);
	// only add way to boundary if this way is not already added
	if(!dup || *dup==0)
	{
		b->segments = g_list_prepend(b->segments, item_bin_to_poly_segment(member, role));
	}
	else
	{
		fprintf(stderr, "process_boundaries_member: dup=true wayid=%lld\n", item_bin_get_wayid(member));
	}

	//fprintf(stderr,"process_boundaries_member:099\n");
}

static GList *
process_boundaries_setup(FILE *boundaries, struct relations *relations)
{
	struct item_bin *ib;
	GList *boundaries_list = NULL;
	struct relations_func *relations_func;
	long long b_counter_1 = 0;
	// long long b_counter_2 = 0;

	//fprintf(stderr,"process_boundaries_setup:001\n");

	relations_func = relations_func_new(process_boundaries_member, NULL);
	while ((ib = read_item(boundaries, 0)))
	{

		//fprintf(stderr,"********DUMP b ***********\n");
		//dump_itembin(ib);
		//fprintf(stderr,"********DUMP b ***********\n");


		char *member = NULL;
		struct boundary *boundary=g_new0(struct boundary, 1);
		char *admin_level = osm_tag_value(ib, "admin_level");
		char *iso = osm_tag_value(ib, "ISO3166-1");

		b_counter_1++;
		if ((b_counter_1 % 500) == 0)
		{
			fprintf(stderr,"boundaries:B:%lld\n", b_counter_1);
		}

		//fprintf(stderr,"process_boundaries_setup:002\n");
		//fprintf(stderr,"== b:%s %s ==\n", iso, admin_level);

		/* disable spain for now since it creates a too large index */
		if (admin_level && !strcmp(admin_level, "2") && (!iso || strcasecmp(iso, "es")))
		{
			if (iso)
			{
				struct country_table *country = country_from_iso2(iso);
				if (!country)
				{
					osm_warning("relation", item_bin_get_relationid(ib), 0, "Country Boundary contains unknown ISO3166-1 value '%s'\n", iso);
				}
				else
				{
					boundary->iso2 = g_strdup(iso);
					osm_info("relation", item_bin_get_relationid(ib), 0, "Country Boundary for '%s'\n", iso);
				}
				boundary->country = country;
			}
			else
			{
				osm_warning("relation", item_bin_get_relationid(ib), 0, "Country Boundary doesn't contain an ISO3166-1 tag\n");
			}
		}

		//b_counter_2 = 0;
		while ((member = item_bin_get_attr(ib, attr_osm_member, member)))
		{
			//fprintf(stderr,"process_boundaries_setup:005\n");
			//fprintf(stderr,"********DUMP b ***********\n");
			//dump_itembin(ib);
			//fprintf(stderr,"********DUMP b ***********\n");

			//b_counter_2++;
			//fprintf(stderr,"boundaries:M:%lld\n", b_counter_2);

			long long wayid;
			int read = 0;
			if (sscanf(member, "2:%Ld:%n", &wayid, &read) >= 1)
			{
				char *rolestr = member + read;
				enum geom_poly_segment_type role;
				if (!strcmp(rolestr, "outer") || !strcmp(rolestr, "exclave"))
					role = geom_poly_segment_type_way_outer;
				else if (!strcmp(rolestr, "inner") || !strcmp(rolestr, "enclave"))
					role = geom_poly_segment_type_way_inner;
				else if (!strcmp(rolestr, ""))
					role = geom_poly_segment_type_way_unknown;
				else
				{
					osm_warning("relation", item_bin_get_relationid(ib), 0, "Unknown role %s in member ", rolestr);
					osm_warning("way", wayid, 1, "\n");
					role = geom_poly_segment_type_none;
				}

				//fprintf(stderr,"process_boundaries_setup:006 %s %Ld\n", rolestr,wayid);

				relations_add_func(relations, relations_func, boundary, (gpointer) role, 2, wayid);
			}
		}

		boundary->ib = item_bin_dup(ib);
		boundaries_list = g_list_append(boundaries_list, boundary);
	}

	return boundaries_list;
}

GList *
boundary_find_matches(GList *l, struct coord *c)
{
	GList *ret = NULL;
	//fprintf(stderr,"boundary_find_matches:001\n");
	while (l)
	{
		//fprintf(stderr,"boundary_find_matches:002\n");

		struct boundary *boundary = l->data;
		if (bbox_contains_coord(&boundary->r, c))
		{
			//fprintf(stderr,"boundary_find_matches:003 id=%lld name=%s\n", item_bin_get_relationid(boundary->ib), osm_tag_name(boundary->ib));
			if (geom_poly_segments_point_inside(boundary->sorted_segments, c) > 0)
			{
				//fprintf(stderr,"boundary_find_matches:004\n");
				ret = g_list_prepend(ret, boundary);
			}
			// children stuff disabled!!
			// ret = g_list_concat(ret, boundary_find_matches(boundary->children, c));
			// children stuff disabled!!
		}
		l = g_list_next(l);
	}

	//fprintf(stderr,"boundary_find_matches:099\n");

	return ret;
}

GList *
boundary_find_matches_level(GList *l, struct coord *c, int min_admin_level, int max_admin_level)
{
	GList *ret = NULL;
	char *al;
	int admin_level;
	struct boundary *boundary = NULL;

	while (l)
	{
		boundary = l->data;
		al = osm_tag_value(boundary->ib, "admin_level");
		if (!al)
		{
			admin_level = 9999;
		}
		else
		{
			admin_level = atoi(al);
		}

		if (admin_level < 2)
		{
			admin_level = 9999;
		}

		//fprintf(stderr, "matches 001:this:%d min:%d max:%d\n", admin_level, min_admin_level, max_admin_level);

		if ((admin_level >= min_admin_level) && (admin_level <= max_admin_level))
		{
			//fprintf(stderr, "matches 002:level\n");
			if (bbox_contains_coord(&boundary->r, c))
			{
				//fprintf(stderr, "matches 003:bbox\n");
				if (geom_poly_segments_point_inside(boundary->sorted_segments, c) > 0)
				{
					//fprintf(stderr, "matches 004:**found**\n");
					ret = g_list_prepend(ret, boundary);
				}
			}
		}
		l = g_list_next(l);
	}

	return ret;
}


GList *
boundary_find_matches_single(GList *l, struct coord *c)
{
	GList *ret = NULL;

	if (l)
	{
		//fprintf(stderr, "bbox:001\n");
		struct boundary *boundary = l->data;
		//fprintf(stderr, "bbox:%d %d %d %d\n", boundary->r.l.x, boundary->r.l.y, boundary->r.h.x, boundary->r.h.y);
		//fprintf(stderr, "c:%d %d\n", c->x, c->y);
		if (bbox_contains_coord(&boundary->r, c))
		{
			//fprintf(stderr, "inside bbox\n");
			if (geom_poly_segments_point_inside(boundary->sorted_segments, c) > 0)
			{
				//fprintf(stderr, "bbox:002\n");
				ret = g_list_prepend(ret, boundary);
			}
		}
	}

	return ret;
}


static void dump_hierarchy(GList *l, char *prefix)
{
	char *newprefix = g_alloca(sizeof(char) * (strlen(prefix) + 2));
	strcpy(newprefix, prefix);
	strcat(newprefix, " ");
	while (l)
	{
		struct boundary *boundary = l->data;
		fprintf(stderr, "%s:childs:%d:%lld:%s\n", prefix, g_list_length(boundary->children), item_bin_get_relationid(boundary->ib), osm_tag_name(boundary->ib));
		dump_hierarchy(boundary->children, newprefix);
		l = g_list_next(l);
	}
}

static gint boundary_bbox_compare(gconstpointer a, gconstpointer b)
{
	const struct boundary *boundarya = a;
	const struct boundary *boundaryb = b;
	long long areaa = bbox_area(&boundarya->r);
	long long areab = bbox_area(&boundaryb->r);
	if (areaa > areab)
		return 1;
	if (areaa < areab)
		return -1;
	return 0;
}

static GList *
process_boundaries_insert(GList *list, struct boundary *boundary)
{
	// children stuff is totally broken, so it is disabled now!!
	/*
	GList *l = list;
	while (l)
	{
		struct boundary *b = l->data;
		if (bbox_contains_bbox(&boundary->r, &b->r))
		{
			list = g_list_remove(list, b);
			boundary->children = g_list_prepend(boundary->children, b);
			l = list;
		}
		else if (bbox_contains_bbox(&b->r, &boundary->r))
		{
			b->children = process_boundaries_insert(b->children, boundary);
			return list;
		}
		else
		{
			l = g_list_next(l);
		}
	}
	*/
	// children stuff is totally broken, so it is disabled now!!

	return g_list_prepend(list, boundary);
}

static GList *
process_boundaries_finish(GList *boundaries_list)
{
	//fprintf(stderr,"process_boundaries_finish:001\n");

	GList *l, *sl, *l2, *ln;
	GList *ret = NULL;
	l = boundaries_list;
	char *f1_name = NULL;
	char *f2_name = NULL;
	long long b_counter_1 = 0;
	long long nodes_counter_ = 0;
	long long ways_counter_ = 0;

	while (l)
	{
		struct boundary *boundary = l->data;
		int first = 1;
		FILE *f = NULL, *fu = NULL;

		b_counter_1++;
		if ((b_counter_1 % 500) == 0)
		{
			fprintf(stderr,"boundaries_f1:B:%lld\n", b_counter_1);
		}

		//fprintf(stderr,"process_boundaries_finish:002\n");

		// only lowercase country code
		if (boundary->iso2)
		{
			int i99;
			for (i99 = 0; boundary->iso2[i99]; i99++)
			{
				boundary->iso2[i99] = tolower(boundary->iso2[i99]);
			}
		}
		// only lowercase country code

		if (boundary->country)
		{
			//fprintf(stderr,"process_boundaries_finish:003\n");

			char *name = g_strdup_printf("country_%s_poly", boundary->iso2);
			f1_name = g_strdup_printf("country_%s_poly", boundary->iso2);
			f = tempfile("", name, 1);
			g_free(name);
		}



		// calc bounding box
		first = 1;
		nodes_counter_ = 0;
		ways_counter_ = 0;
		sl = boundary->segments;
		while (sl)
		{
			struct geom_poly_segment *gs = sl->data;
			struct coord *c = gs->first;
			while (c <= gs->last)
			{
				if (first)
				{
					boundary->r.l = *c;
					boundary->r.h = *c;
					first = 0;
				}
				else
				{
					bbox_extend(c, &boundary->r);
				}
				c++;
				nodes_counter_++;
			}
			sl = g_list_next(sl);
			ways_counter_++;
		}

		//fprintf(stderr, "relid:%lld\n", item_bin_get_relationid(boundary->ib));
		//fprintf(stderr, "ways:%lld nodes:%lld\n", ways_counter_, nodes_counter_);

		boundary->sorted_segments = geom_poly_segments_sort(boundary->segments, geom_poly_segment_type_way_right_side);
		sl = boundary->sorted_segments;

		first = 1;
		while (sl)
		{
			//fprintf(stderr,"process_boundaries_finish:004.1\n");

			struct geom_poly_segment *gs = sl->data;
			struct coord *c = gs->first;

			/*
			while (c <= gs->last)
			{
				if (first)
				{
					boundary->r.l = *c;
					boundary->r.h = *c;
					first = 0;
				}
				else
				{
					bbox_extend(c, &boundary->r);
				}
				c++;

				//fprintf(stderr,"process_boundaries_finish:004.2 lx=%d ly=%d hx=%d hy=%d\n",boundary->r.l.x,boundary->r.l.y,boundary->r.h.x,boundary->r.h.y);

			}
			*/

			if (f)
			{
				struct item_bin *ib = item_bin_2;
				item_bin_init(ib, type_selected_line);
				item_bin_add_coord(ib, gs->first, gs->last - gs->first + 1);
				item_bin_write(ib, f);
			}

			if (boundary->country)
			{
				if (!coord_is_equal(*gs->first, *gs->last))
				{
					if (!fu)
					{
						char *name = g_strdup_printf("country_%s_broken", boundary->iso2);
						f2_name = g_strdup_printf("country_%s_broken", boundary->iso2);
						fprintf(stderr, "*BROKEN* country_%s_broken\n", boundary->iso2);
						fu = tempfile("", name, 1);
						g_free(name);
					}
					struct item_bin *ib = item_bin_2;
					item_bin_init(ib, type_selected_point);
					item_bin_add_coord(ib, gs->first, 1);
					item_bin_write(ib, fu);

					item_bin_init(ib, type_selected_point);
					item_bin_add_coord(ib, gs->last, 1);
					item_bin_write(ib, fu);
				}
			}
			sl = g_list_next(sl);

			if (f2_name)
			{
				tempfile_unlink("", f2_name);
				g_free(f2_name);
				f2_name = NULL;
			}
		}

		ret = process_boundaries_insert(ret, boundary);
		l = g_list_next(l);

		if (f)
		{
			fclose(f);
		}

		if (fu)
		{
			if (boundary->country)
			{
				//osm_warning("relation", item_bin_get_relationid(boundary->ib), 0, "Broken country polygon '%s'\n", boundary->iso2);
				fprintf(stderr, "*BROKEN* country polygon '%s'\n", boundary->iso2);
			}
			fclose(fu);
		}

		if (f1_name)
		{
			tempfile_unlink("", f1_name);
			g_free(f1_name);
			f1_name = NULL;
		}
	}
#if 0
	printf("hierarchy\n");
#endif

	// boundaries_list = g_list_sort(boundaries_list, boundary_bbox_compare); // disable sorting, does not seem to do any good

// children stuff totally broken!!!
#if 0
	b_counter_1 = 0;
	l = boundaries_list;
	while (l)
	{
		b_counter_1++;
		if ((b_counter_1 % 500) == 0)
		{
			fprintf(stderr,"boundaries_f2:B:%lld\n", b_counter_1);
		}

		struct boundary *boundary = l->data;
		ln = l2 = g_list_next(l);
		while (l2)
		{
			struct boundary *boundary2 = l2->data;
			if (bbox_contains_bbox(&boundary2->r, &boundary->r))
			{
				boundaries_list = g_list_remove(boundaries_list, boundary);
				boundary2->children = g_list_append(boundary2->children, boundary);
				break;
			}
			l2 = g_list_next(l2);
		}
		l = ln;
	}
#endif
// children stuff totally broken!!!

	// -- DEBUG --
	// -- DEBUG --
	// -- DEBUG --
	// dump_hierarchy(boundaries_list,""); // --> prints huge amounts of data!! be careful
	// -- DEBUG --
	// -- DEBUG --
	// -- DEBUG --

	return boundaries_list;
}

GList *
process_boundaries(FILE *boundaries, FILE *coords, FILE *ways)
{
	GList *boundaries_list;
	struct relations *relations = relations_new();

	//fprintf(stderr,"process_boundaries:001\n");
	boundaries_list = process_boundaries_setup(boundaries, relations);
	//fprintf(stderr,"process_boundaries:001.rp1\n");
	relations_process(relations, NULL, ways, NULL);
	//fprintf(stderr,"process_boundaries:001.rp2\n");
	return process_boundaries_finish(boundaries_list);
}

