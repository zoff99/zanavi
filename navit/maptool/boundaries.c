/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2013 Zoff <zoff@zoff.cc>
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
#include <dirent.h>

#include <locale.h>
#include <stdlib.h>

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
	if(!dup || *dup == 0)
	{
		b->segments = g_list_prepend(b->segments, item_bin_to_poly_segment(member, role));
		// fprintf(stderr, "process_boundaries_member: dupdup wayid=%lld %p\n", item_bin_get_wayid(member), dup);
	}
	else
	{
		// fprintf(stderr, "process_boundaries_member: dup=true wayid=%lld\n", item_bin_get_wayid(member));
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
			fprintf_(stderr,"boundaries:B:%lld\n", b_counter_1);
		}

		//fprintf(stderr,"process_boundaries_setup:002\n");
		//fprintf(stderr,"== b:%s %s ==\n", iso, admin_level);

		/* disable spain for now since it creates a too large index */
		// if (admin_level && !strcmp(admin_level, "2") && (!iso || strcasecmp(iso, "es")))
		if (admin_level && !strcmp(admin_level, "2"))
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
				{
					role = geom_poly_segment_type_way_outer;
				}
				else if (!strcmp(rolestr, "inner") || !strcmp(rolestr, "enclave"))
				{
					role = geom_poly_segment_type_way_inner;
				}
				else if (!strcmp(rolestr, ""))
				{
					role = geom_poly_segment_type_way_unknown;
				}
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

	GList *l = NULL, *sl = NULL, *l2 = NULL, *ln = NULL;
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
			fprintf_(stderr,"boundaries_f1:B:%lld\n", b_counter_1);
		}

		//fprintf(stderr,"process_boundaries_finish:002\n");

		// only uppercase country code
		if (boundary->iso2)
		{
			int i99;
			for (i99 = 0; boundary->iso2[i99]; i99++)
			{
				boundary->iso2[i99] = toupper(boundary->iso2[i99]);
			}
		}
		// only uppercase country code

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
						fprintf(stderr, "*BROKEN*(1) country_%s_broken relid=%lld\n", boundary->iso2, item_bin_get_relationid(boundary->ib));
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
				fprintf_(stderr, "*BROKEN*(2) country polygon '%s' relid=%lld\n", boundary->iso2, item_bin_get_relationid(boundary->ib));
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


void free_boundaries(GList *l)
{
	while (l)
	{
		struct boundary *boundary=l->data;
		GList *s=boundary->segments;

		while (s)
		{
			struct geom_poly_segment *seg=s->data;

			g_free(seg->first);
			g_free(seg);
			s=g_list_next(s);
		}

		s=boundary->sorted_segments;

		while (s)
		{
			struct geom_poly_segment *seg=s->data;

			g_free(seg->first);
			g_free(seg);
			s=g_list_next(s);
		}

		g_list_free(boundary->segments);
		g_list_free(boundary->sorted_segments);
		g_free(boundary->ib);

		free_boundaries(boundary->children);

		g_free(boundary);

		l=g_list_next(l);
	}
}


GList *
process_boundaries(FILE *boundaries, FILE *coords, FILE *ways)
{
	GList *boundaries_list = NULL;
	struct relations *relations = relations_new();

	//fprintf(stderr,"process_boundaries:001\n");
	boundaries_list = process_boundaries_setup(boundaries, relations);
	//fprintf(stderr,"process_boundaries:001.rp1\n");
	relations_process(relations, NULL, ways, NULL);
	//fprintf(stderr,"process_boundaries:001.rp2\n");
	relations_destroy(relations);
	return process_boundaries_finish(boundaries_list);
}

static char* only_ascii(char *instr)
{
	char *outstr;
	int i;
	int j;

	if (!instr)
	{
		return NULL;
	}

	outstr = g_strdup(instr);

	j = 0;
	for (i = 0; i<strlen(instr); i++)
	{
		if ((instr[i] >= 'a')&&(instr[i] <= 'z'))
		{
			outstr[j] = instr[i];
			j++;
		}
		else if ((instr[i] >= 'A')&&(instr[i] <= 'Z'))
		{
			outstr[j] = instr[i];
			j++;
		}
		else
		{
			// non ascii char encountered -> ingore it
		}
	}

	if (j == 0)
	{
		if (outstr)
		{
			g_free(outstr);
		}
		return NULL;
	}
	else
	{
		outstr[j] = '\0';
	}

	return outstr;
}

void save_manual_country_borders(GList *borders)
{
	struct country_table2
	{
		int countryid;
		char *names;
		char *admin_levels;
		FILE *file;
		int size;
		struct rect r;
	};

	struct boundary *b;
	struct country_table2 *country;
	int i;
	FILE *fp;
	char filename[300];
	int admin_l_boundary;
	int num;
	char broken_string[100];
	char *broken_str = broken_string[0];
	char *country_name = NULL;

	GList *l = borders;
	while (l)
	{
		if (l)
		{

			b = l->data;
			if (b)
			{
				//if (item_bin_is_closed_poly(b->ib) == 1)
				{
					char *admin_l_boundary_str = osm_tag_value(b->ib, "admin_level");
					if (!admin_l_boundary_str)
					{
						admin_l_boundary = 9999;
					}
					else
					{
						admin_l_boundary = atoi(admin_l_boundary_str);
					}
			
					if (admin_l_boundary < 2)
					{
						admin_l_boundary = 9999;
					}

					country = b->country;
					if (country)
					{
						if (admin_l_boundary < 3)
						{
							fprintf_(stderr, "adminlevel=%d countryid=%d name=%s\n", admin_l_boundary, country->countryid, country->names);
							num = 0;

							GList *sl=b->sorted_segments;
							while (sl)
							{

								//fprintf(stderr, "sorted segs\n");

								struct geom_poly_segment *gs = sl->data;
								struct coord *c2 = gs->first;
								broken_str = "";

								if ((gs->first == NULL)||(gs->last == NULL))
								{
									// only 1 coord in poly
									broken_str = "._BROKEN1_";
								}
								else if (gs->first == gs->last)
								{
									// only 1 coord in poly
									broken_str = "._BROKEN2_";
								}
								else if (!coord_is_equal(*gs->first, *gs->last))
								{
									// first coord not equal to last coord -> poly is not closed
									broken_str = "._BROKEN3_";
								}

								if (country->names)
								{
									char *converted_to_ascii = only_ascii(country->names);
									if (converted_to_ascii)
									{
										country_name = g_strdup_printf("%s_", converted_to_ascii);
										g_free(converted_to_ascii);
									}
									else
									{
										country_name = g_strdup_printf("");
									}
								}
								else
								{
									country_name = g_strdup_printf("");
								}

								num++;

								if (gs->type == geom_poly_segment_type_none)
								{
									sprintf(filename, "XXman_country_poly.%s%d.%d%s.%s.txt", country_name, country->countryid, num, broken_str, "none");
								}
								else if (gs->type == geom_poly_segment_type_way_inner)
								{
									sprintf(filename, "XXman_country_poly.%s%d.%d%s.%s.txt", country_name, country->countryid, num, broken_str, "inner");
								}
								else if (gs->type == geom_poly_segment_type_way_outer)
								{
									sprintf(filename, "XXman_country_poly.%s%d.%d%s.%s.txt", country_name, country->countryid, num, broken_str, "outer");
								}
								else if (gs->type == geom_poly_segment_type_way_left_side)
								{
									sprintf(filename, "XXman_country_poly.%s%d.%d%s.%s.txt", country_name, country->countryid, num, broken_str, "left");
								}
								else if (gs->type == geom_poly_segment_type_way_right_side)
								{
									sprintf(filename, "XXman_country_poly.%s%d.%d%s.%s.txt", country_name, country->countryid, num, broken_str, "right");
								}
								else if (gs->type == geom_poly_segment_type_way_unknown)
								{
									sprintf(filename, "XXman_country_poly.%s%d.%d%s.%s.txt", country_name, country->countryid, num, broken_str, "unknown");
								}

								if (!global_less_verbose)
								{
									fp = fopen(filename,"wb");
									if (fp)
									{
										fprintf(fp, "%d\n", country->countryid); // country id
										fprintf(fp, "1\n"); // poly num -> here always 1

										while (c2 <= gs->last)
										{
											// fprintf(stderr, "%d,%d\n", c2[0].x, c2[0].y);
											fprintf(fp, "   %lf   %lf\n", transform_to_geo_lon(c2[0].x), transform_to_geo_lat(c2[0].y));
											c2++;
										}

										fprintf(fp, "END\n"); // poly END marker
										fprintf(fp, "END\n"); // file END marker
									}
									fclose(fp);
								}

								sl = g_list_next(sl);

								if (country_name)
								{
									g_free(country_name);
									country_name = NULL;
								}
							}
						}

					}

				}
			}

			l = g_list_next(l);
		}
	}
}

GList* load_manual_country_borders()
{
	struct country_table
	{
		int countryid;
		char *names;
		char *admin_levels;
		FILE *file;
		int size;
		struct rect r;
	};

	DIR* dirp;
	struct dirent *dp;
	FILE * fp;
	char *line = NULL;
	size_t len = 0;
	ssize_t read;
	GList *ret = NULL;
	int linecounter = 0;
	char *iso = NULL;
	int c_id = 999; // set to unkown country
	int faktor;
	struct coord *c;
	struct coord *cur_coord;
	int coord_count;
	struct rect *boundary_bbox;
	double lat;
	double lon;
	char lat_str[200];
	char lon_str[200];
	char *saved_locale;
	char *endptr;
	char *full_file_name = NULL;
	int inner_polygon;
	struct boundary_manual *boundary;

	saved_locale = setlocale(LC_NUMERIC, "C");

	// initialise inner manual border list
	GList *boundary_list_inner_manual = NULL;

	dirp = opendir(manual_country_border_dir);
	// "man_country_poly.<ISO num>.txt"
	while ((dp = readdir(dirp)) != NULL)
	{
		//fprintf(stderr, "consider:%s\n", dp->d_name);

		if ((strlen(dp->d_name) > 20) && (!strncmp(dp->d_name, "man_country_poly.", 17)))
		{
			if (strcasestr(dp->d_name, ".inner.") == NULL)
			{
				// NOT an inner polygon
				inner_polygon = 0;
			}
			else
			{
				inner_polygon = 1;
			}

			full_file_name = g_strdup_printf("%s/%s", manual_country_border_dir, dp->d_name);

			fp = fopen(full_file_name, "rb");
			// fprintf(stderr, "fopen=%s\n", dp->d_name);

			if (fp != NULL)
			{
				// fprintf(stderr, "fopen:OK\n");
				coord_count = 0;
				while ((read = getline(&line, &len, fp)) != -1)
				{
					if (strlen(line) > 1)
					{
						if ((strlen(line) > 2)&&(!strncmp(line, "END", 3)))
						{
							// IGNORE "END" lines
							//fprintf(stderr, "load_manual_country_borders:ignore END line\n");
						}
						else
						{
							coord_count++;
						}
					}
					else
					{
						fprintf_(stderr, "load_manual_country_borders:ignore empty line\n");
					}
				}
				coord_count = coord_count - 2; // subtract header to get number of coords
				c = (struct coord*) malloc(sizeof(struct coord) * coord_count);
				cur_coord = c;

				fseeko(fp, 0L, SEEK_SET);
				// rewind(fp);

				linecounter = 0;

				while ((read = getline(&line, &len, fp)) != -1)
				{
					if (linecounter == 0)
					{
						if (inner_polygon == 1)
						{
							boundary=g_new0(struct boundary_manual, 1);
						}
						else
						{
							// first line has the ISO code of the country
							boundary=g_new0(struct boundary_manual, 1);
						}

						c_id = atoi(line);

						struct country_table *country = country_from_countryid(c_id);
						if (!country)
						{
							// problem with this file -> skip it
							fprintf(stderr, "load_manual_country_borders:PROBLEM WITH THIS FILE:%s cid=%d\n", dp->d_name, c_id);
							g_free(boundary);
							boundary = NULL;

							break;
						}
						else
						{
							// boundary->iso2 = g_strdup("XXX");
							boundary->country = country;
							boundary->c = c;
							boundary->coord_count = coord_count;
							boundary->countryid = c_id;
							if (country->names)
							{
								//fprintf(stderr, "load_manual_country_borders:country %d:name=%s\n", c_id, country->names);
							}
							else
							{
								//fprintf(stderr, "load_manual_country_borders:country %d:name=\n", c_id);
							}
						}
					}
					else if (linecounter == 1)
					{
						// faktor = atoi(line);
						//
						// unsused anymore, line 2 ist just the polygon number, usually 1 in our case! so just ignore
						faktor = 1;
						//fprintf(stderr, "faktor1: %d\n", faktor);
					}
					else
					{
						if (strlen(line) > 1)
						{
							if ((strlen(line) > 2)&&(!strncmp(line, "END", 3)))
							{
								// IGNORE "END" lines
								//fprintf(stderr, "load_manual_country_borders:ignore END line\n");
							}
							else
							{
								// fprintf(stderr, "found1: line=%s", line);
								// fprintf(stderr, "faktor2: %d\n", faktor);

								// line format:LON LAT
								lat_str[0] = '\0';
								lon_str[0] = '\0';
								// sscanf(line, " %lf %lf\n", &lon, &lat);
								sscanf(line, "%s%s", lon_str, lat_str);
								lon = strtod(lon_str, &endptr);
								lat = strtod(lat_str, &endptr);

								// fprintf(stderr, "found2: lat_str=%s lon_str=%s\n", lat_str, lon_str);
								// 7.566702E+00   4.805764E+01

								lat = lat * faktor;
								lon = lon * faktor;
								// fprintf(stderr, "found4: lat=%lf lon=%lf\n", lat, lon);
								cur_coord->x = transform_from_geo_lon(lon);
								cur_coord->y = transform_from_geo_lat(lat);
								cur_coord++;
							}
						}
						else
						{
							fprintf_(stderr, "load_manual_country_borders:ignore empty line\n");
						}

					}
					linecounter++;
				}

				if (line)
				{
					free(line);
					line = NULL;
				}

				// add country boundary to result list
				if (boundary != NULL)
				{
					bbox(boundary->c, boundary->coord_count, &boundary->r);
					if (inner_polygon == 1)
					{
						fprintf_(stderr, "load_manual_country_borders:add inner poly\n");
						boundary_list_inner_manual = g_list_append(boundary_list_inner_manual, boundary);
					}
					else
					{
						//fprintf(stderr, "load_manual_country_borders:add normal poly\n");
						ret = g_list_append(ret, boundary);
					}
				}

				fclose(fp);
			}

			if (full_file_name)
			{
				g_free(full_file_name);
				full_file_name = NULL;
			}

		}
	}

	closedir(dirp);

	setlocale(LC_NUMERIC, saved_locale);

	// return result list
	return ret;
}



