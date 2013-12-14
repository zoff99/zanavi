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
 * Copyright (C) 2005-2008 Navit Team
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

#include <stdlib.h>
#include <glib.h>
#include <string.h>
#include <math.h>
#include "debug.h"
#include "projection.h"
#include "item.h"
#include "map.h"
#include "mapset.h"
#include "coord.h"
#include "transform.h"
#include "search.h"
#include "country.h"
#include "navit.h"

#if HAVE_API_ANDROID
#include "android.h"
#endif
#include "layout.h"
#include "map.h"
#include "linguistics.h"

struct search_list_level
{
	struct mapset *ms;
	struct search_list_common *parent;
	struct attr *attr;
	int partial;
	int selected;
	struct mapset_search *search;
	GHashTable *hash;
	GList *list, *curr, *last;
};

struct interpolation
{
	int side, mode, rev;
	char *first, *last, *curr;
};

struct search_list
{
	struct mapset *ms;
	struct item *item;
	int level;
	struct search_list_level levels[4];
	struct search_list_result result;
	struct search_list_result last_result;
	int last_result_valid;
	char *postal;
	struct interpolation inter;
};

// func definition
static int ascii_cmp_local_faster(char *name, char *match, int partial);
// func definition

static guint search_item_hash_hash(gconstpointer key)
{
	const struct item *itm = key;
	gconstpointer hashkey = (gconstpointer) GINT_TO_POINTER(itm->id_hi ^ itm->id_lo);
	return g_direct_hash(hashkey);
}

static gboolean search_item_hash_equal(gconstpointer a, gconstpointer b)
{
	const struct item *itm_a = a;
	const struct item *itm_b = b;
	if (item_is_equal_id(*itm_a, *itm_b))
		return TRUE;
	return FALSE;
}

struct search_list *
search_list_new(struct mapset *ms)
{
	struct search_list *ret;

	ret=g_new0(struct search_list, 1);
	ret->ms = ms;

	return ret;
}

static void search_list_search_free(struct search_list *sl, int level);

static int search_list_level(enum attr_type attr_type)
{
	switch (attr_type)
	{
		case attr_country_all:
		case attr_country_id:
		case attr_country_iso2:
		case attr_country_iso3:
		case attr_country_car:
		case attr_country_name:
			return 0;
		case attr_town_postal:
			return 1;
		case attr_town_name:
		case attr_district_name:
		case attr_town_or_district_name:
			return 1;
		case attr_street_name:
			return 2;
		case attr_house_number:
			return 3;
		case attr_postal:
			return -1;
		default:
			// // dbg(0, "unknown search '%s'\n", attr_to_name(attr_type));
			return -1;
	}
}

static void interpolation_clear(struct interpolation *inter)
{
	inter->mode = inter->side = 0;
	g_free(inter->first);
	g_free(inter->last);
	g_free(inter->curr);
	inter->first = inter->last = inter->curr = NULL;
}

void search_list_search(struct search_list *this_, struct attr *search_attr, int partial)
{
	struct search_list_level *le;
	int level = search_list_level(search_attr->type);
	this_->item = NULL;
	interpolation_clear(&this_->inter);
	//// // dbg(0,"## enter\n");
	//// dbg(0,"## level=%d\n", level);
	if (level != -1)
	{
		this_->result.id = 0;
		this_->level = level;
		le = &this_->levels[level];
		search_list_search_free(this_, level);
		le->attr = attr_dup(search_attr);
		le->partial = partial;
		if (level > 0)
		{
			le = &this_->levels[level - 1];
			le->curr = le->list;
		}
		//// dbg(0,"## le=%p partial=%d\n", le, partial);
	}
	else if (search_attr->type == attr_postal)
	{
		g_free(this_->postal);
		this_->postal = g_strdup(search_attr->u.str);
	}
	//// dbg(0,"## return\n");
}

struct search_list_common *
search_list_select(struct search_list *this_, enum attr_type attr_type, int id, int mode)
{
	int level = search_list_level(attr_type);
	int num = 0;
	struct search_list_level *le;
	struct search_list_common *slc;
	GList *curr;
	le = &this_->levels[level];
	curr = le->list;
	if (mode > 0 || !id)
		le->selected = mode;
	//// dbg(0,"enter level=%d %d %d %p\n", level, id, mode, curr);
	while (curr)
	{
		num++;
		if (!id || num == id)
		{
			slc = curr->data;
			slc->selected = mode;
			if (id)
			{
				le->last = curr;
				//// dbg(0,"found\n");
				return slc;
			}
		}
		curr = g_list_next(curr);
	}
	//// dbg(0,"not found\n");
	return NULL;
}

static void search_list_common_new(struct item *item, struct search_list_common *common)
{
	struct attr attr;
	if (item_attr_get(item, attr_town_name, &attr))
		common->town_name = map_convert_string(item->map, attr.u.str);
	else
		common->town_name = NULL;
	if (item_attr_get(item, attr_county_name, &attr))
		common->county_name = map_convert_string(item->map, attr.u.str);
	else
		common->county_name = NULL;
	if (item_attr_get(item, attr_district_name, &attr))
		common->district_name = map_convert_string(item->map, attr.u.str);
	else
		common->district_name = NULL;
	if (item_attr_get(item, attr_postal, &attr))
		common->postal = map_convert_string(item->map, attr.u.str);
	else if (item_attr_get(item, attr_town_postal, &attr))
		common->postal = map_convert_string(item->map, attr.u.str);
	else
		common->postal = NULL;
	if (item_attr_get(item, attr_postal_mask, &attr))
		common->postal_mask = map_convert_string(item->map, attr.u.str);
	else
		common->postal_mask = NULL;
}

static void search_list_common_destroy(struct search_list_common *common)
{
	map_convert_free(common->town_name);
	map_convert_free(common->district_name);
	map_convert_free(common->county_name);
	map_convert_free(common->postal);
	map_convert_free(common->postal_mask);
}

static struct search_list_country *
search_list_country_new(struct item *item)
{
	struct search_list_country *ret=g_new0(struct search_list_country, 1);
	struct attr attr;

	ret->common.item = ret->common.unique = *item;
	if (item_attr_get(item, attr_country_car, &attr))
		ret->car = g_strdup(attr.u.str);
	if (item_attr_get(item, attr_country_iso2, &attr))
	{
#if HAVE_API_ANDROID
		ret->iso2=g_malloc(strlen(attr.u.str)+1);
		strtolower(ret->iso2, attr.u.str);
#else 
		ret->iso2 = g_strdup(attr.u.str);
#endif		
		ret->flag = g_strdup_printf("country_%s", ret->iso2);
	}
	if (item_attr_get(item, attr_country_iso3, &attr))
		ret->iso3 = g_strdup(attr.u.str);
	if (item_attr_get(item, attr_country_name, &attr))
		ret->name = g_strdup(attr.u.str);
	return ret;
}

static void search_list_country_destroy(struct search_list_country *this_)
{
	g_free(this_->car);
	g_free(this_->iso2);
	g_free(this_->iso3);
	g_free(this_->flag);
	g_free(this_->name);
	g_free(this_);
}

static struct search_list_town *
search_list_town_new(struct item *item)
{
	struct search_list_town *ret=g_new0(struct search_list_town, 1);
	struct attr attr;
	struct coord c;

	ret->itemt = *item;
	ret->common.item = ret->common.unique = *item;

	if (item_attr_get(item, attr_town_streets_item, &attr))
	{
		//// dbg(1, "town_assoc 0x%x 0x%x\n", attr.u.item->id_hi, attr.u.item->id_lo);
		ret->common.unique = *attr.u.item;
	}

	search_list_common_new(item, &ret->common);

	if (item_attr_get(item, attr_county_name, &attr))
	{
		ret->county = map_convert_string(item->map, attr.u.str);
	}
	else
	{
		ret->county = NULL;
	}

	if (item_coord_get(item, &c, 1))
	{
		ret->common.c=g_new(struct pcoord, 1);
		ret->common.c->x = c.x;
		ret->common.c->y = c.y;
		ret->common.c->pro = map_projection(item->map);
	}
	else
	{
		// some error with lat/lng !!
		// but still return something, or app will crash
		ret->common.c=g_new(struct pcoord, 1);
		ret->common.c->x = 0;
		ret->common.c->y = 0;
		ret->common.c->pro = map_projection(item->map);
	}
	return ret;
}

static void search_list_town_destroy(struct search_list_town *this_)
{
	map_convert_free(this_->county);
	search_list_common_destroy(&this_->common);
	if (this_->common.c)
		g_free(this_->common.c);
	g_free(this_);
}

static struct search_list_street *
search_list_street_new(struct item *item)
{
	struct search_list_street *ret=g_new0(struct search_list_street, 1);
	struct attr attr;
	struct coord c;

	ret->common.item = ret->common.unique = *item;
	if (item_attr_get(item, attr_street_name, &attr))
	{
		ret->name = map_convert_string(item->map, attr.u.str);
	}
	else
	{
		ret->name = NULL;
	}
	search_list_common_new(item, &ret->common);
	if (item_coord_get(item, &c, 1))
	{
		ret->common.c=g_new(struct pcoord, 1);
		ret->common.c->x = c.x;
		ret->common.c->y = c.y;
		ret->common.c->pro = map_projection(item->map);
	}
	else
	{
		// some error with lat/lng !!
		// but still return something, or app will crash
		ret->common.c=g_new(struct pcoord, 1);
		ret->common.c->x = 0;
		ret->common.c->y = 0;
		ret->common.c->pro = map_projection(item->map);
	}
	return ret;
}

static void search_list_street_destroy(struct search_list_street *this_)
{
	map_convert_free(this_->name);
	search_list_common_destroy(&this_->common);
	if (this_->common.c)
	{
		g_free(this_->common.c);
	}
	g_free(this_);
}

static char *
search_interpolate(struct interpolation *inter)
{
	// dbg(1, "interpolate %s-%s %s\n", inter->first, inter->last, inter->curr);
	if (!inter->first || !inter->last)
		return NULL;
	if (!inter->curr)
		inter->curr = g_strdup(inter->first);
	else
	{
		if (strcmp(inter->curr, inter->last))
		{
			int next = atoi(inter->curr) + (inter->mode ? 2 : 1);
			g_free(inter->curr);
			if (next == atoi(inter->last))
				inter->curr = g_strdup(inter->last);
			else
				inter->curr = g_strdup_printf("%d", next);
		}
		else
		{
			g_free(inter->curr);
			inter->curr = NULL;
		}
	}
	// dbg(1, "interpolate result %s\n", inter->curr);
	return inter->curr;
}

static void search_interpolation_split(char *str, struct interpolation *inter)
{
	char *pos = strchr(str, '-');
	char *first, *last;
	int len;
	if (!pos)
	{
		inter->first = g_strdup(str);
		inter->last = g_strdup(str);
		inter->rev = 0;
		return;
	}
	len = pos - str;
	first = g_malloc(len + 1);
	strncpy(first, str, len);
	first[len] = '\0';
	last = g_strdup(pos + 1);
	// dbg(1, "%s = %s - %s\n", str, first, last);
	if (atoi(first) > atoi(last))
	{
		inter->first = last;
		inter->last = first;
		inter->rev = 1;
	}
	else
	{
		inter->first = first;
		inter->last = last;
		inter->rev = 0;
	}
}

static int search_setup_interpolation(struct item *item, enum attr_type i0, enum attr_type i1, enum attr_type i2, struct interpolation *inter)
{
	struct attr attr;
	g_free(inter->first);
	g_free(inter->last);
	g_free(inter->curr);
	inter->first = inter->last = inter->curr = NULL;
	// dbg(1, "setup %s\n", attr_to_name(i0));
	if (item_attr_get(item, i0, &attr))
	{
		search_interpolation_split(attr.u.str, inter);
		inter->mode = 0;
	}
	else if (item_attr_get(item, i1, &attr))
	{
		search_interpolation_split(attr.u.str, inter);
		inter->mode = 1;
	}
	else if (item_attr_get(item, i2, &attr))
	{
		search_interpolation_split(attr.u.str, inter);
		inter->mode = 2;
	}
	else
		return 0;
	return 1;
}

static int search_match(char *str, char *search, int partial)
{
	if (!partial)
		return (!g_strcasecmp(str, search));
	else
		return (!g_strncasecmp(str, search, strlen(search)));
}

static struct pcoord *
search_house_number_coordinate(struct item *item, struct interpolation *inter)
{
	struct pcoord *ret=g_new(struct pcoord, 1);
	ret->pro = map_projection(item->map);

	// // dbg(0,"001t: %s\n", item_to_name(item->type));

	if (item_is_point(*item))
	{
		struct coord c;
		if (item_coord_get(item, &c, 1))
		{
			ret->x = c.x;
			ret->y = c.y;
		}
		else
		{
			g_free(ret);
			ret = NULL;
		}
	}
	else
	{
		int count, max = 1024;
		int hn_pos, hn_length;
		struct coord *c = g_alloca(sizeof(struct coord) * max);
		item_coord_rewind(item);
		count = item_coord_get(item, c, max);
		hn_length = atoi(inter->last) - atoi(inter->first);
		if (inter->rev)
			hn_pos = atoi(inter->last) - atoi(inter->curr);
		else
			hn_pos = atoi(inter->curr) - atoi(inter->first);

		if (count)
		{
			int i, distance_sum = 0, hn_distance;
			int *distances = g_alloca(sizeof(int) * (count - 1));
			// dbg(1, "count=%d hn_length=%d hn_pos=%d (%s of %s-%s)\n", count, hn_length, hn_pos, inter->curr, inter->first, inter->last);
			if (!hn_length)
			{
				hn_length = 2;
				hn_pos = 1;
			}
			if (count == max)
				// dbg(0, "coordinate overflow\n");
				for (i = 0; i < count - 1; i++)
				{
					distances[i] = navit_sqrt(transform_distance_sq(&c[i], &c[i + 1]));
					distance_sum += distances[i];
					// dbg(1, "distance[%d]=%d\n", i, distances[i]);
				}
			// dbg(1, "sum=%d\n", distance_sum);
			hn_distance = distance_sum * hn_pos / hn_length;
			// dbg(1, "hn_distance=%d\n", hn_distance);
			i = 0;
			while (i < count - 1 && hn_distance > distances[i])
				hn_distance -= distances[i++];
			// dbg(1, "remaining distance=%d from %d\n", hn_distance, distances[i]);
			ret->x = (c[i + 1].x - c[i].x) * hn_distance / distances[i] + c[i].x;
			ret->y = (c[i + 1].y - c[i].y) * hn_distance / distances[i] + c[i].y;
		}
	}
	return ret;
}

static struct search_list_house_number *
search_list_house_number_new(struct item *item, struct interpolation *inter, char *inter_match, int inter_partial)
{
	struct search_list_house_number *ret=g_new0(struct search_list_house_number, 1);
	struct attr attr;
	char *hn;

	//// dbg(0,"@@@@ enter @@@@\n");

	ret->common.item = ret->common.unique = *item;
	//if (item_attr_get(item, attr_street_name, &attr))
	//	// dbg(0,"xx1 %s\n",attr.u.str);
	if (item_attr_get(item, attr_house_number, &attr))
	{
		ret->house_number = map_convert_string(item->map, attr.u.str);
	}
	else
	{
		//if (item_attr_get(item, attr_street_name, &attr))
		//	// dbg(0,"xx2 %s\n",attr.u.str);
		for (;;)
		{
			//// dbg(0,"interpolate 11");
			ret->interpolation = 1;
			switch (inter->side)
			{
				case 0:
					//// dbg(0,"interpolate 11 0");
					inter->side = -1;
					search_setup_interpolation(item, attr_house_number_left, attr_house_number_left_odd, attr_house_number_left_even, inter);
				case -1:
					//// dbg(0,"interpolate 11 -1");
					if ((hn = search_interpolate(inter)))
						break;
					inter->side = 1;
					search_setup_interpolation(item, attr_house_number_right, attr_house_number_right_odd, attr_house_number_right_even, inter);
				case 1:
					//// dbg(0,"interpolate 11 1");
					if ((hn = search_interpolate(inter)))
						break;
				default:
					//// dbg(0,"interpolate 11 default");
					g_free(ret);
					return NULL;
			}
			if (search_match(hn, inter_match, inter_partial))
			{
				//// dbg(0,"interpolate 22");
				//// dbg(0,"match %s %s-%s\n",hn, inter->first, inter->last);
				ret->house_number = map_convert_string(item->map, hn);
				break;
			}
		}
	}
	search_list_common_new(item, &ret->common);
	ret->common.c = search_house_number_coordinate(item, ret->interpolation ? inter : NULL);
	return ret;
}

static void search_list_house_number_destroy(struct search_list_house_number *this_)
{
	map_convert_free(this_->house_number);
	search_list_common_destroy(&this_->common);
	if (this_->common.c)
		g_free(this_->common.c);
	g_free(this_);
}

static void search_list_result_destroy(int level, void *p)
{
	switch (level)
	{
		case 0:
			search_list_country_destroy(p);
			break;
		case 1:
			search_list_town_destroy(p);
			break;
		case 2:
			search_list_street_destroy(p);
			break;
		case 3:
			search_list_house_number_destroy(p);
			break;
	}
}

static void search_list_search_free(struct search_list *sl, int level)
{
	//// dbg(0,"enter\n");

	struct search_list_level *le = &sl->levels[level];
	GList *next, *curr;
	if (le->search)
	{
		mapset_search_destroy(le->search);
		le->search = NULL;
	}
#if 0 /* FIXME */
	if (le->hash)
	{
		g_hash_table_destroy(le->hash);
		le->hash=NULL;
	}
#endif
	curr = le->list;
	while (curr)
	{
		search_list_result_destroy(level, curr->data);
		next = g_list_next(curr);
		curr = next;
	}
	attr_free(le->attr);
	g_list_free(le->list);
	le->list = NULL;
	le->curr = NULL;
	le->last = NULL;

	//// dbg(0,"return\n");
}

char *
search_postal_merge(char *mask, char *new)
{
	int i;
	char *ret = NULL;
	// dbg(1, "enter %s %s\n", mask, new);
	if (!new)
		return NULL;
	if (!mask)
		return g_strdup(new);
	i = 0;
	while (mask[i] && new[i])
	{
		if (mask[i] != '.' && mask[i] != new[i])
			break;
		i++;

	}
	if (mask[i])
	{
		ret = g_strdup(mask);
		while (mask[i])
			ret[i++] = '.';
	}
	// dbg(1, "merged %s with %s as %s\n", mask, new, ret);
	return ret;
}

char *
search_postal_merge_replace(char *mask, char *new)
{
	char *ret = search_postal_merge(mask, new);
	if (!ret)
		return mask;
	g_free(mask);
	return ret;
}

static int postal_match(char *postal, char *mask)
{
	for (;;)
	{
		if ((*postal != *mask) && (*mask != '.'))
			return 0;
		if (!*postal)
		{
			if (!*mask)
				return 1;
			else
				return 0;
		}
		postal++;
		mask++;
	}
}

static int search_add_result(struct search_list_level *le, struct search_list_common *slc)
{
	struct search_list_common *slo;
	char *merged;

	//// dbg(0,"enter\n");

	//slo=g_hash_table_lookup(le->hash, &slc->unique);
	//if (!slo) {
	//g_hash_table_insert(le->hash, &slc->unique, slc);
	if (slc->postal && !slc->postal_mask)
	{
		slc->postal_mask = g_strdup(slc->postal);
	}
	// ******
	g_list_free(le->list);
	le->list = NULL;
	// ******
	le->list = g_list_append(le->list, slc);
	return 1;
	//}
	merged = search_postal_merge(slo->postal_mask, slc->postal);
	if (merged)
	{
		g_free(slo->postal_mask);
		slo->postal_mask = merged;
	}
	return 0;
}

struct search_list_result *
search_list_get_result(struct search_list *this_)
{
	struct search_list_level *le, *leu;
	int level = this_->level;
	struct attr attr2;
	int has_street_name = 0;

	//// dbg(0,"******* enter *******\n");
	le = &this_->levels[level];
	//// dbg(0,"le=%p\n", le);
	for (;;)
	{
		//// dbg(0,"le->search=%p\n", le->search);
		if (!le->search)
		{
			//// dbg(0,"partial=%d level=%d\n", le->partial, level);
			if (!level)
				le->parent = NULL;
			else
			{
				leu = &this_->levels[level - 1];
				//// dbg(0,"leu->curr=%p\n", leu->curr);
				for (;;)
				{
					//// dbg(0,"*********########");

					struct search_list_common *slc;
					if (!leu->curr)
					{
						return NULL;
					}
					le->parent = leu->curr->data;
					leu->last = leu->curr;
					leu->curr = g_list_next(leu->curr);
					slc = (struct search_list_common *) (le->parent);
					if (!slc)
						break;
					if (slc->selected == leu->selected)
						break;
				}
			}
#if 0
			if (le->parent)
			{
				// dbg(0,"mapset_search_new with item(%d,%d)\n", le->parent->item.id_hi, le->parent->item.id_lo);
			}
			else
			{
				// dbg(0,"NO parent!!\n");
			}
			// dbg(0,"############## attr=%s\n", attr_to_name(le->attr->type));
#endif
			le->search = mapset_search_new(this_->ms, &le->parent->item, le->attr, le->partial);
			// ** DOC ** mapset_search_new(struct mapset *ms, struct item *item, struct attr *search_attr, int partial)
			le->hash = g_hash_table_new(search_item_hash_hash, search_item_hash_equal);
		}

		//// dbg(0,"le->search=%p\n", le->search);

		if (!this_->item)
		{
			//// dbg(0,"sssss 1");
			this_->item = mapset_search_get_item(le->search);
			//// dbg(0,"sssss 1 %p\n",this_->item);
		}

		if (this_->item)
		{
			void *p = NULL;
			//// dbg(0,"id_hi=%d id_lo=%d\n", this_->item->id_hi, this_->item->id_lo);
			if (this_->postal)
			{
				struct attr postal;
				if (item_attr_get(this_->item, attr_postal_mask, &postal))
				{
					if (!postal_match(this_->postal, postal.u.str))
						continue;
				}
				else if (item_attr_get(this_->item, attr_postal, &postal))
				{
					if (strcmp(this_->postal, postal.u.str))
						continue;
				}
			}
			this_->result.country = NULL;
			this_->result.town = NULL;
			this_->result.street = NULL;
			this_->result.c = NULL;
			//// dbg(0,"case x LEVEL start %d\n",level);
			switch (level)
			{
				case 0:
					//// dbg(0,"case 0 COUNTRY");
					p = search_list_country_new(this_->item);
					this_->result.country = p;
					this_->result.country->common.parent = NULL;
					this_->item = NULL;
					break;
				case 1:
					//// dbg(0,"case 1 TOWN");
					p = search_list_town_new(this_->item);
					this_->result.town = p;
					this_->result.town->common.parent = this_->levels[0].last->data;
					this_->result.country = this_->result.town->common.parent;
					this_->result.c = this_->result.town->common.c;
					this_->item = NULL;
					break;
				case 2:
					//// dbg(0,"case 2 STREET");
					p = search_list_street_new(this_->item);
					this_->result.street = p;
					this_->result.street->common.parent = this_->levels[1].last->data;
					this_->result.town = this_->result.street->common.parent;
					this_->result.country = this_->result.town->common.parent;
					this_->result.c = this_->result.street->common.c;
					this_->item = NULL;
					break;
				case 3:
					//// dbg(0,"case 3 HOUSENUMBER");
					has_street_name = 0;

					// if this housenumber has a streetname tag, set the name now
					if (item_attr_get(this_->item, attr_street_name, &attr2))
					{
						// // dbg(0,"streetname: %s\n",attr2.u.str);
						has_street_name = 1;
					}

					//// dbg(0,"case 3 XXXX 1\n");
					p = search_list_house_number_new(this_->item, &this_->inter, le->attr->u.str, le->partial);
					//// dbg(0,"case 3 XXXX 2\n");
					if (!p)
					{
						interpolation_clear(&this_->inter);
						this_->item = NULL;
						continue;
					}
					//// dbg(0,"case 3 XXXX 3\n");
					this_->result.house_number = p;
					if (!this_->result.house_number->interpolation)
					{
						this_->item = NULL;
					}

					this_->result.house_number->common.parent = this_->levels[2].last->data;
					this_->result.street = this_->result.house_number->common.parent;
					this_->result.town = this_->result.street->common.parent;
					this_->result.country = this_->result.town->common.parent;
					this_->result.c = this_->result.house_number->common.c;

					//// dbg(0,"case 3 XXXX 4\n");
					if (has_street_name == 1)
					{
						gchar *tmp_name = g_strdup(attr2.u.str);
						this_->result.street->name = tmp_name;
						//// dbg(0,"res streetname=%s\n",this_->result.street->name);
					}
					else
					{
						//
						// this crashes all the time -> so dont use!
						//static struct search_list_street null_street;
						//this_->result.street=&null_street;
						// this crashes all the time -> so dont use!
						//
						this_->result.street->name = NULL;
					}
					//// dbg(0,"case 3 XXXX 5\n");
					break;
			}
			// CASE END *********

			//// dbg(0,"case end\n");

			if (p)
			{
				if (search_add_result(le, p))
				{
					//** this_->result.id++;
					this_->result.id = 0;
					//// dbg(0,"++++ return result\n");
					return &this_->result;
				}
				else
				{
					search_list_result_destroy(level, p);
					// return &this_->result;
				}
			}
		}
		else
		{
			mapset_search_destroy(le->search);
			le->search = NULL;
			g_hash_table_destroy(le->hash);
			if (!level)
			{
				break;
			}
		}
	}
	return NULL;
}

void search_list_destroy(struct search_list *this_)
{
	g_free(this_->postal);
	g_free(this_);
}

void search_init(void)
{
}

static char *
search_fix_spaces(char *str)
{
	int i;
	int len = strlen(str);
	char c, *s, *d, *ret = g_strdup(str);

	for (i = 0; i < len; i++)
	{
		if (ret[i] == ',' || ret[i] == ',' || ret[i] == '/')
			ret[i] = ' ';
	}
	s = ret;
	d = ret;
	len = 0;
	do
	{
		c = *s++;
		if (c != ' ' || len != 0)
		{
			*d++ = c;
			len++;
		}
		while (c == ' ' && *s == ' ')
			s++;
		if (c == ' ' && *s == '\0')
		{
			d--;
			len--;
		}
	}
	while (c);
	return ret;
}

static GList *
search_split_phrases(char *str)
{
	char *tmp, *s, *d;
	GList *ret = NULL;
	s = str;
	do
	{
		tmp = g_strdup(s);
		d = tmp + strlen(s) - 1;
		ret = g_list_append(ret, g_strdup(s));
		while (d >= tmp)
		{
			if (*d == ' ')
			{
				*d = '\0';
				ret = g_list_append(ret, g_strdup(tmp));
			}
			d--;
		}
		g_free(tmp);
		do
		{
			s++;
			if (*s == ' ')
			{
				s++;
				break;
			}
		}
		while (*s != '\0');
	}
	while (*s != '\0');
	return ret;
}

#define MAX_INDEXSEARCH_TOWNNAME 300

static int search_address_housenumber_for_street(char *hn_name_match, char *street_name_match, char *town_string, struct coord *c, int partial, struct jni_object *jni)
{
	struct item *item;
	struct map_rect *mr = NULL;
	struct mapset *ms;
	struct mapset_handle *msh;
	struct map* map = NULL;
	struct attr map_name_attr;
	struct attr attr;
	struct pcoord center99;
	struct map_selection *sel;
	int search_radius_this;
	int search_order;
	struct attr att;
	struct attr att2;
	char *town_string2;
	char *hn_fold = NULL;
	char *street_name_fold = NULL;
	int search_results_found_ = 0;
	int max_townname_plus_1 = MAX_INDEXSEARCH_TOWNNAME + 1;

	ms = global_navit->mapsets->data;
	msh = mapset_open(ms);

	search_order = 18;
	search_radius_this = 20;

	hn_fold = linguistics_fold_and_prepare_complete(hn_name_match, 0);
	street_name_fold = linguistics_fold_and_prepare_complete(street_name_match, 0);

	if (!street_name_fold)
	{
		return;
	}

	if (!hn_fold)
	{
		return;
	}

	if (strlen(street_name_fold) < 1)
	{
		if (hn_fold)
		{
			g_free(hn_fold);
		}

		if (street_name_fold)
		{
			g_free(street_name_fold);
		}
		return;
	}

	if (strlen(hn_fold) < 1)
	{
		if (hn_fold)
		{
			g_free(hn_fold);
		}

		if (street_name_fold)
		{
			g_free(street_name_fold);
		}
		return;
	}

	if (strlen(town_string) > MAX_INDEXSEARCH_TOWNNAME)
	{
		town_string2 = town_string;
		town_string2[max_townname_plus_1] = '\0';
		town_string2 = linguistics_check_utf8_string(town_string2);
	}
	else
	{
		town_string2 = town_string;
	}


	center99.x = c->x;
	center99.y = c->y;
	sel = map_selection_rect_new(&center99, search_radius_this, search_order);
	sel->range.min = type_house_number;
	sel->range.max = type_house_number;
	sel->u.c_rect.lu.x = center99.x - search_radius_this;
	sel->u.c_rect.lu.y = center99.y + search_radius_this;
	sel->u.c_rect.rl.x = center99.x + search_radius_this;
	sel->u.c_rect.rl.y = center99.y - search_radius_this;

	// dbg(0, "hn=%s sn=%s\n", hn_name_match, street_name_match);
	// dbg(0, "cx=%d cy=%d\n", c->x, c->y);

	while (msh && (map = mapset_next(msh, 0)))
	{
		if (offline_search_break_searching == 1)
		{
			break;
		}

		if (map_get_attr(map, attr_name, &map_name_attr, NULL))
		{
			if (strncmp("_ms_sdcard_map:", map_name_attr.u.str, 15) == 0)
			{
				if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/navitmap", map_name_attr.u.str, 43) == 0)
				{
					// its an sdcard map
					mr = map_rect_new(map, sel);
					if (mr)
					{
						while ((item = map_rect_get_item(mr)))
						{
							if (offline_search_break_searching == 1)
							{
								break;
							}

#ifdef DEBUG_GLIB_MEM_FUNCTIONS
							g_mem_profile();
#endif

							if (item->type == type_house_number)
							{
								// does it have a housenumber?
								if (item_attr_get(item, attr_house_number, &att))
								{
									// match housenumber to our string
									if (!ascii_cmp_local_faster(att.u.str, hn_fold, partial))
									{
										// if this housenumber has a streetname tag, compare it now
										if (item_attr_get(item, attr_street_name, &att2))
										{
											if (!ascii_cmp_local_faster(att2.u.str, street_name_fold, partial))
											{
												// coords of result
												struct coord c2;
												char *buffer;

												if (item_coord_get(item, &c2, 1))
												{
													// SHN -> street with house number
													// return a string like: "SHN:H111L5555:16.766:48.76:full address name is at the end"
													// ca. 9 chars : ca. 9 chars : max. 100 max. 100 max. 100 max. 15 chars -> this sould be max. about 335 chars long
													if (town_string2)
													{
														buffer = g_strdup_printf("SHN:H0L0:%d:%d:%s %s, %.*s", c2.y, c2.x, att2.u.str, att.u.str, max_townname_plus_1, town_string2);
													}
													else
													{
														buffer = g_strdup_printf("SHN:H0L0:%d:%d:%s %s", c2.y, c2.x, att2.u.str, att.u.str);
													}
													//dbg(0,"buffer HN=%s\n", buffer);

#ifdef HAVE_API_ANDROID
													// return results to android as they come in ...
													android_return_search_result(jni,buffer);
													search_results_found_++;
#endif
													g_free(buffer);
												}
											}
										}
									}
								}
							}
						}
						map_rect_destroy(mr);
					}
				}
			}
		}
	}
	map_selection_destroy(sel);
	mapset_close(msh);

	if (hn_fold)
	{
		g_free(hn_fold);
	}

	if (street_name_fold)
	{
		g_free(street_name_fold);
	}


#ifdef DEBUG_GLIB_MEM_FUNCTIONS
	g_mem_profile();
#endif

	return search_results_found_;
}

static GList *
search_address_housenumber_real(GList *result_list, struct search_list *sl, char *street_name, GList *phrases, GList *exclude1, GList *exclude2, GList *exclude3, int partial, struct jni_object *jni)
{
	struct search_list_result *slr;
	struct coord_geo g;
	struct coord c;

	//// dbg(0,"enter\n");

	while ((slr = search_list_get_result(sl)))
	{

		if (offline_search_break_searching == 1)
		{
			break;
		}

		// does the streetname of the housenumber match the street we want?
		if (slr != NULL)
		{
			if (slr->street != NULL)
			{
				if ((street_name != NULL) && (slr->street->name != NULL))
				{
					if (strcmp(slr->street->name, street_name) == 0)
					{
						char *buffer;
						// coords of result
						c.x = slr->house_number->common.c->x;
						c.y = slr->house_number->common.c->y;
						transform_to_geo(slr->house_number->common.c->pro, &c, &g);
						// SHN -> street with house number
						// return a string like: "SHN:H111L5555:16.766:48.76:full address name is at the end"
						// ca. 9 chars : ca. 9 chars : max. 100 max. 100 max. 100 max. 15 chars -> this sould be max. about 335 chars long
						if (slr->town->common.postal == NULL)
						{
							buffer = g_strdup_printf("SHN:H%dL%d:%f:%f:%.101s,%.101s, %.101s %.15s", slr->street->common.item.id_hi, slr->street->common.item.id_lo, g.lat, g.lng, slr->country->name, slr->town->common.town_name, slr->street->name, slr->house_number->house_number);
						}
						else
						{
							buffer = g_strdup_printf("SHN:H%dL%d:%f:%f:%.101s,%.7s %.101s, %.101s %.15s", slr->street->common.item.id_hi, slr->street->common.item.id_lo, g.lat, g.lng, slr->country->name, slr->town->common.postal, slr->town->common.town_name, slr->street->name, slr->house_number->house_number);
						}

						//// dbg(0,"res=%s\n",buffer);

						// deactivated now * result_list=g_list_prepend(result_list,g_strdup(buffer));
#ifdef HAVE_API_ANDROID
						// return results to android as they come in ...
						android_return_search_result(jni,buffer);
#endif
						g_free(buffer);
					}
				}
			}
		}
	}

	return result_list;
}

static GList *
search_address__street(GList *result_list, struct search_list *sl, GList *phrases, GList *exclude1, GList *exclude2, GList *exclude3, int partial, struct jni_object *jni)
{
	//// dbg(0,"enter\n");

	struct search_list_result *slr = NULL;
	GList *tmp = phrases;
	int count = 0;
	struct coord_geo g;
	struct coord c;
	struct attr attr2;

	struct item *save_item;
	int save_level;
	int save_last_result_valid;

	while ((slr = search_list_get_result(sl)))
	{
		char *buffer;
		char *buffer2;

		if (offline_search_break_searching == 1)
		{
			break;
		}

		if (slr->street)
		{
			// coords of result
			c.x = slr->street->common.c->x;
			c.y = slr->street->common.c->y;
			transform_to_geo(slr->street->common.c->pro, &c, &g);

			// STR -> street
			// return a string like: "STR:H1111L5555:16.766:-48.76:full address name is at the end"
			// ca. 9 chars : ca. 9 chars : max. 100 max. 100 max. 100 chars -> this sould be max. about 320 chars long
			if (slr->town->common.postal == NULL)
			{
				buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.101s,%.101s, %.101s", slr->street->common.item.id_hi, slr->street->common.item.id_lo, g.lat, g.lng, slr->country->name, slr->town->common.town_name, slr->street->name);
			}
			else
			{
				buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.101s,%.7s %.101s, %.101s", slr->street->common.item.id_hi, slr->street->common.item.id_lo, g.lat, g.lng, slr->country->name, slr->town->common.postal, slr->town->common.town_name, slr->street->name);
			}
			// deactivated now * result_list=g_list_prepend(result_list,g_strdup(buffer));

			//// dbg(0,"res=%s\n",buffer);

#ifdef HAVE_API_ANDROID
			// return results to android as they come in ...
			android_return_search_result(jni,buffer);
#endif
			count++;

			buffer2 = g_strdup_printf("%s", slr->street->name);
			//// dbg(0,"b2:%s\n",buffer2);


			save_item = sl->item;
			save_level = sl->level;
			save_last_result_valid = sl->last_result_valid;

#if 1
			// put words back to start!!
			tmp = phrases;
			while (tmp)
			{
				if (offline_search_break_searching == 1)
				{
					break;
				}

				//// dbg(0,"s0=%s\n",tmp->data);
				if (tmp != exclude1 && tmp != exclude2 && tmp != exclude3)
				{
					//// dbg(0,"s=%s\n",tmp->data);

					attr2.type = attr_house_number;
					attr2.u.str = tmp->data;
					search_list_search(sl, &attr2, partial);
					result_list = search_address_housenumber_real(result_list, sl, buffer2, phrases, exclude1, exclude2, exclude3, partial, jni);
				}
				tmp = g_list_next(tmp);
			}
#endif

			// restore again
			sl->item = save_item;
			sl->level = save_level;
			sl->last_result_valid = save_last_result_valid;

		}

		if (buffer2)
		{
			g_free(buffer2);
		}

		if (buffer)
		{
			g_free(buffer);
		}
	}

	//// dbg(0,"return 2\n");
	return result_list;
}

static GList *
search_address__town(GList *result_list, struct search_list *sl, GList *phrases, GList *exclude1, GList *exclude2, int partial, struct jni_object *jni)
{
	//// dbg(0,"enter\n");
	struct search_list_result *slr;
	GList *tmp = phrases;
	int count = 0;
	struct coord_geo g;
	struct coord c;
	struct attr attr;
	struct attr attrx;
	// struct search_list *sl2=search_list_new(sl->ms);
	struct item *save_item;
	int save_level;
	int save_last_result_valid;
	// int first=1;

	while ((slr = search_list_get_result(sl)))
	{
		char *buffer;

		if (offline_search_break_searching == 1)
		{
			break;
		}

		// coords of result
		c.x = slr->town->common.c->x;
		c.y = slr->town->common.c->y;
		transform_to_geo(slr->town->common.c->pro, &c, &g);

		// TWN -> town
		if (slr->town->common.postal == NULL)
		{
			buffer = g_strdup_printf("TWN:H%dL%d:%f:%f:%.101s,%.101s", slr->town->common.item.id_hi, slr->town->common.item.id_lo, g.lat, g.lng, slr->country->name, slr->town->common.town_name);
		}
		else
		{
			buffer = g_strdup_printf("TWN:H%dL%d:%f:%f:%.101s,%.7s %.101s", slr->town->common.item.id_hi, slr->town->common.item.id_lo, g.lat, g.lng, slr->country->name, slr->town->common.postal, slr->town->common.town_name);
		}

		//// dbg(0,"**res=%s\n",buffer);

		// deactivated now * result_list=g_list_prepend(result_list,g_strdup(buffer));
#ifdef HAVE_API_ANDROID
		// return results to android as they come in ...
		android_return_search_result(jni,buffer);
#endif

		count++;
		if (buffer)
		{
			g_free(buffer);
		}

		save_item = sl->item;
		save_level = sl->level;
		save_last_result_valid = sl->last_result_valid;

		// put words back to start!!
		tmp = phrases;

		while (tmp)
		{
			if (offline_search_break_searching == 1)
			{
				break;
			}

			if (tmp != exclude1 && tmp != exclude2)
			{
				//// dbg(0,"s=%s\n",tmp->data);
				attr.type = attr_street_name;
				attr.u.str = tmp->data;
				search_list_search(sl, &attr, partial);
				result_list = search_address__street(result_list, sl, phrases, exclude1, exclude2, tmp, partial, jni);
			}
			tmp = g_list_next(tmp);
		}

		// restore again
		sl->item = save_item;
		sl->level = save_level;
		sl->last_result_valid = save_last_result_valid;
	}

	//// dbg(0,"return 2\n");
	return result_list;
}

static GList *
search_address__country(GList *result_list, struct search_list *sl, GList *phrases, GList *exclude, int partial, struct jni_object *jni)
{
	GList *tmp = phrases;
	int count = 0;
	struct attr attr;
	struct search_list_result *slr;
	//// dbg(0,"enter\n");

	while ((slr = search_list_get_result(sl)))
	{
		//// dbg(0,"1 slr=%p\n",slr->country);
		//// dbg(0,"2 slr=%s\n",slr->country->name);
		//// dbg(0,"3 slr=%s\n",slr->country->iso2);
		count++;
	}
	//// dbg(0,"count %d\n",count);
	if (!count)
	{
		//// dbg(0,"return 1");
		return result_list;
	}

	while (tmp)
	{
		if (tmp != exclude)
		{
			//// dbg(0,"Is=%s\n",tmp->data);
			attr.type = attr_town_or_district_name;
			attr.u.str = tmp->data;
			search_list_search(sl, &attr, partial);
			result_list = search_address__town(result_list, sl, phrases, exclude, tmp, partial, jni);
		}
		//else
		//{
		//// dbg(0,"Xs=%s\n",tmp->data);
		//}
		tmp = g_list_next(tmp);
	}
	//// dbg(0,"return 2");
	return result_list;
}



struct country2
{
	int id;
	char *car;
	char *iso2;
	char *iso3;
	char *name;
};

#include "search_countrytable.h"



// @return  =0 strings matched, =1 not matched
static int ascii_cmp_local(char *name, char *match, int partial)
{
	char *s1_a;
	char *s2_a;

	char *s1 = linguistics_casefold(name);
	char *s2 = linguistics_casefold(match);

	if (s1)
	{
		s1_a = linguistics_remove_all_specials(s1);
		if (s1_a)
		{
			g_free(s1);
			s1 = s1_a;
		}
		s1_a = linguistics_expand_special(s1, 1);
		if (s1_a)
		{
			g_free(s1);
			s1 = s1_a;
		}
	}

	if (s2)
	{
		s2_a = linguistics_remove_all_specials(s2);
		if (s2_a)
		{
			g_free(s2);
			s2 = s2_a;
		}
		s2_a = linguistics_expand_special(s2, 1);
		if (s2_a)
		{
			g_free(s2);
			s2 = s2_a;
		}
	}

	int ret = linguistics_compare(s1, s2, partial);

	if (s1)
	{
		g_free(s1);
	}

	if (s2)
	{
		g_free(s2);
	}

	return ret;
}

// @return  =0 strings matched, =1 not matched
static int ascii_cmp_local_faster(char *name, char *match, int partial)
{
	char *s1_a;
	char *s1 = name;
	int ret;

	s1_a = linguistics_fold_and_prepare_complete(s1, 0);

	if (!s1_a)
	{
		return 1;
	}

	// dbg(0,"s1=%s match=%s\n", s1_a, match);

	if (strlen(s1_a) == 0)
	{
		// only special chars in string, return "no match"
		return 1;
	}


	// --- old ---
	//ret = linguistics_compare(s1, match, partial);
	// --- old ---

	if (partial == 1)
	{
		ret = strncmp(s1_a, match, strlen(match));
	}
	else
	{
		if (strlen(s1_a) == strlen(match))
		{
			ret = strncmp(s1_a, match, strlen(match));
		}
		else
		{
			ret = 1;
		}
	}


	if (s1_a)
	{
		g_free(s1_a);
	}

	return ret;
}

struct navit *global_navit;

void search_full_world(char *addr, int partial, int search_order, struct jni_object *jni, struct coord_geo *search_center, int search_radius)
{
	struct item *item;
	struct map_rect *mr = NULL;
	struct mapset *ms;
	struct mapset_handle *msh;
	struct map* map = NULL;
	struct attr map_name_attr;
	struct attr attr;

	char *str = search_fix_spaces(addr);
	GList *phrases = search_split_phrases(str);
	GList *phrases_first;
	phrases_first = phrases;

	ms = global_navit->mapsets->data;
	msh = mapset_open(ms);

	struct pcoord center99;
	int search_radius_this = 0;
	// dbg(0, "in lat=%f,lng=%f\n", search_center->lat, search_center->lng);
	if ((search_center->lat == 0) && (search_center->lng == 0))
	{
		center99.x = 0;
		center99.y = 0;
		search_radius_this = 21000000;
	}
	else
	{
		struct coord c99;
		transform_from_geo(projection_mg, search_center, &c99);
		center99.x = c99.x;
		center99.y = c99.y;
		search_radius_this = search_radius;
	}
	// dbg(0, "out x=%d,y=%d,r=%d\n", center99.x, center99.y, search_radius_this);

	struct map_selection *sel = map_selection_rect_new(&center99, search_radius_this, search_order);
	sel->range.min = type_town_label;
	sel->range.max = type_area;

	while (msh && (map = mapset_next(msh, 0)))
	{
		if (offline_search_break_searching == 1)
		{
			break;
		}

		if (map_get_attr(map, attr_name, &map_name_attr, NULL))
		{
			if (strncmp("_ms_sdcard_map:", map_name_attr.u.str, 15) == 0)
			{
				if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/navitmap", map_name_attr.u.str, 43) == 0)
				{
					// its an sdcard map
					//// dbg(0,"map name=%s",map_name_attr.u.str);
					// mr=map_rect_new(map, NULL);
					mr = map_rect_new(map, sel);
					if (mr)
					{
						char *streetname_last = NULL;

						while ((item = map_rect_get_item(mr)))
						{

							if (offline_search_break_searching == 1)
							{
								break;
							}

#ifdef DEBUG_GLIB_MEM_FUNCTIONS
							g_mem_profile();
#endif

							if ((item_is_town(*item)) || (item_is_district(*item)))
							{
								struct search_list_town *p = NULL;

								if (item_attr_get(item, attr_town_name, &attr))
								{
									p = search_list_town_new(item);
									char *buffer = NULL;
									// coords of result
									struct coord_geo g;
									struct coord c;
									c.x = p->common.c->x;
									c.y = p->common.c->y;
									int calc_geo = 0;

									// // dbg(0,"town name=%s\n", attr.u.str);

									phrases = phrases_first;
									while (phrases)
									{

										if (offline_search_break_searching == 1)
										{
											break;
										}

										if (!ascii_cmp_local(attr.u.str, phrases->data, partial))
										{
											// // dbg(0,"matched town name=%s want=%s\n", attr.u.str, phrases->data);
											if (calc_geo == 0)
											{
												transform_to_geo(p->common.c->pro, &c, &g);
												// TWN -> town
												calc_geo = 1;
											}
											if (p->common.postal == NULL)
											{
												buffer = g_strdup_printf("TWN:H%dL%d:%f:%f:%.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, p->common.town_name);
											}
											else
											{
												buffer = g_strdup_printf("TWN:H%dL%d:%f:%f:%.7s %.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, p->common.postal, p->common.town_name);
											}
#ifdef HAVE_API_ANDROID
											// return results to android as they come in ...
											android_return_search_result(jni,buffer);
#endif
										}
										phrases = g_list_next(phrases);

									}

									if (buffer)
									{
										g_free(buffer);
									}
									search_list_town_destroy(p);
								}

								if (item_attr_get(item, attr_town_name_match, &attr))
								{
									p = search_list_town_new(item);
									char *buffer = NULL;
									// coords of result
									struct coord_geo g;
									struct coord c;
									c.x = p->common.c->x;
									c.y = p->common.c->y;
									int calc_geo = 0;

									// // dbg(0,"town name=%s\n", attr.u.str);

									phrases = phrases_first;
									while (phrases)
									{
										if (offline_search_break_searching == 1)
										{
											break;
										}

										if (!ascii_cmp_local(attr.u.str, phrases->data, partial))
										{
											// // dbg(0,"matched town name=%s want=%s\n", attr.u.str, phrases->data);
											if (calc_geo == 0)
											{
												transform_to_geo(p->common.c->pro, &c, &g);
												// TWN -> town
												calc_geo = 1;
											}
											if (p->common.postal == NULL)
											{
												buffer = g_strdup_printf("TWN:H%dL%d:%f:%f:%.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, p->common.town_name);
											}
											else
											{
												buffer = g_strdup_printf("TWN:H%dL%d:%f:%f:%.7s %.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, p->common.postal, p->common.town_name);
											}
#ifdef HAVE_API_ANDROID
											// return results to android as they come in ...
											android_return_search_result(jni,buffer);
#endif
										}
										phrases = g_list_next(phrases);

									}

									if (buffer)
									{
										g_free(buffer);
									}
									search_list_town_destroy(p);
								}
							}
							else if (item_is_street(*item))
							{

								struct search_list_street *p = NULL;

								if (item_attr_get(item, attr_label, &attr))
								{
									// // dbg(0,"street1=%s\n",map_convert_string(item->map, attr.u.str));
									if ((streetname_last == NULL) || (strcmp(streetname_last, attr.u.str) != 0))
									{
										// // dbg(0,"street2=%s\n",map_convert_string(item->map, attr.u.str));
										streetname_last = g_strdup_printf("%s", attr.u.str);

										p = search_list_street_new(item);
										char *buffer = NULL;
										// coords of result
										struct coord_geo g;
										struct coord c;
										c.x = p->common.c->x;
										c.y = p->common.c->y;
										int calc_geo = 0;

										phrases = phrases_first;
										while (phrases)
										{
											if (offline_search_break_searching == 1)
											{
												break;
											}

											if (!ascii_cmp_local(attr.u.str, phrases->data, partial))
											{
												if (calc_geo == 0)
												{
													transform_to_geo(p->common.c->pro, &c, &g);
													calc_geo = 1;
												}
												if (p->common.postal == NULL)
												{
													buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, attr.u.str);
												}
												else
												{
													buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.7s %.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, p->common.postal, attr.u.str);
												}
												//// dbg(0,"street3=%s\n",buffer);
#ifdef HAVE_API_ANDROID
												// return results to android as they come in ...
												android_return_search_result(jni,buffer);
#endif
											}
											phrases = g_list_next(phrases);
										}
										if (buffer)
										{
											g_free(buffer);
										}
										search_list_street_destroy(p);
									}
								}

								if (item_attr_get(item, attr_street_name_match, &attr))
								{
									//// dbg(0,"street systematic=%s\n",map_convert_string(item->map, attr.u.str));

									p = search_list_street_new(item);
									char *buffer = NULL;
									// coords of result
									struct coord_geo g;
									struct coord c;
									c.x = p->common.c->x;
									c.y = p->common.c->y;
									int calc_geo = 0;

									phrases = phrases_first;
									while (phrases)
									{
										if (offline_search_break_searching == 1)
										{
											break;
										}

										if (!ascii_cmp_local(attr.u.str, phrases->data, partial))
										{
											if (calc_geo == 0)
											{
												transform_to_geo(p->common.c->pro, &c, &g);
												calc_geo = 1;
											}
											if (p->common.postal == NULL)
											{
												buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, attr.u.str);
											}
											else
											{
												buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.7s %.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, p->common.postal, attr.u.str);
											}
#ifdef HAVE_API_ANDROID
											// return results to android as they come in ...
											android_return_search_result(jni,buffer);
#endif
										}
										phrases = g_list_next(phrases);
									}
									if (buffer)
									{
										g_free(buffer);
									}
									search_list_street_destroy(p);

								}

								if (item_attr_get(item, attr_street_name_systematic, &attr))
								{
									//// dbg(0,"street systematic=%s\n",map_convert_string(item->map, attr.u.str));

									p = search_list_street_new(item);
									char *buffer = NULL;
									// coords of result
									struct coord_geo g;
									struct coord c;
									c.x = p->common.c->x;
									c.y = p->common.c->y;
									int calc_geo = 0;

									phrases = phrases_first;
									while (phrases)
									{
										if (offline_search_break_searching == 1)
										{
											break;
										}

										if (!ascii_cmp_local(attr.u.str, phrases->data, partial))
										{
											if (calc_geo == 0)
											{
												transform_to_geo(p->common.c->pro, &c, &g);
												calc_geo = 1;
											}
											if (p->common.postal == NULL)
											{
												buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, attr.u.str);
											}
											else
											{
												buffer = g_strdup_printf("STR:H%dL%d:%f:%f:%.7s %.101s", p->common.item.id_hi, p->common.item.id_lo, g.lat, g.lng, p->common.postal, attr.u.str);
											}
#ifdef HAVE_API_ANDROID
											// return results to android as they come in ...
											android_return_search_result(jni,buffer);
#endif
										}
										phrases = g_list_next(phrases);
									}
									if (buffer)
									{
										g_free(buffer);
									}
									search_list_street_destroy(p);

								}
							}
						}
						g_free(streetname_last);
						map_rect_destroy(mr);
					}
				}
			}
		}
	}

	map_selection_destroy(sel);

	if (phrases)
	{
		g_list_free(phrases);
	}
	g_free(str);

	mapset_close(msh);

#ifdef DEBUG_GLIB_MEM_FUNCTIONS
	g_mem_profile();
#endif
}

GList *
search_by_address(GList *result_list, struct mapset *ms, char *addr, int partial, struct jni_object *jni, int search_country_flags, char *search_country_string)
{
	char *str = search_fix_spaces(addr);
	GList *tmp, *phrases = search_split_phrases(str);
	GList *phrases_first;
	GList *ret = NULL;
	struct search_list *sl;
	struct attr attr;
	attr.type = attr_country_all;
	tmp = phrases;
	phrases_first = phrases;
	sl = search_list_new(ms);

	// dbg(0, "-- START --\n");

	// normal search stuff -------- START ----------
	if (search_country_flags == 1)
	{
		// dbg(0, "-- country default start --\n");
		//while (phrases)
		//{
		// // dbg(0,"s=%s\n",phrases->data);
		// set default country
		search_list_search(sl, country_default(), 0);
		ret = search_address__country(ret, sl, phrases, NULL, partial, jni);
		// phrases=g_list_next(phrases);
		//}
		// dbg(0, "-- country default end --\n");
	}
	else if (search_country_flags == 2)
	{
		// dbg(0, "-- country sel:%s start --\n", search_country_string);
		// set a country
		struct attr country;
		country.type = attr_country_iso2;
		country.u.str = search_country_string;
		//while (phrases)
		//{
		// // dbg(0,"s=%s\n",phrases->data);
		search_list_search(sl, &country, 0);
		// set a country
		ret = search_address__country(ret, sl, phrases, NULL, partial, jni);
		//phrases=g_list_next(phrases);
		//}
		// dbg(0, "-- country sel:%s end --\n", search_country_string);
	}
	else // flags==3
	{
		// dbg(0, "-- country all start --\n");
		// search all countries!! could take a really long time!!
		struct attr country;
		int j2 = sizeof(all_country_list) / sizeof(all_country_list[0]);
		int j1;
		for (j1 = 0; j1 < j2; j1++)
		{
			if (all_country_list[j1].iso2 != NULL)
			{
				phrases = phrases_first;
				//while (phrases)
				//{
				// // dbg(0,"s country=%s\n",all_country_list[j1].iso2);
				// // dbg(0,"s=%s\n",phrases->data);
				country.type = attr_country_iso2;
				country.u.str = all_country_list[j1].iso2;
				search_list_search(sl, &country, 0);
				ret = search_address__country(ret, sl, phrases, NULL, partial, jni);
				//phrases=g_list_next(phrases);
				//}

				if (offline_search_break_searching == 1)
				{
					break;
				}
			}
		}
		// dbg(0, "-- country all end --\n");
	}
	// normal search stuff --------  END  ----------

	if (phrases_first)
	{
		g_list_free(phrases_first);
	}

	// dbg(0, "--  END  --\n");

	g_free(str);
	return ret;
}

// IN_BUF_SIZE2 is the size of the file read buffer.
// IN_BUF_SIZE2 must be >= 1
//#define IN_BUF_SIZE2 (1024*16)
//x/ int IN_BUF_SIZE2 = sizeof(struct streets_index_data_block) * 1024;
//x/ int t_IN_BUF_SIZE2 = sizeof(struct town_index_data_block) * 1024;
//x/ static uint8 s_inbuf[(sizeof(struct streets_index_data_block) * 1024)];
//x/ static uint8 t_s_inbuf[(sizeof(struct town_index_data_block) * 1024)];
//static uint8 s_inbuf[IN_BUF_SIZE2];

int IN_BUF_SIZE2 = sizeof(struct streets_index_data_block) * 2048;
int t_IN_BUF_SIZE2 = sizeof(struct town_index_data_block) * 2048;
uint8 *s_inbuf;
uint8 *t_s_inbuf;

// OUT_BUF_SIZE2 is the size of the output buffer used during decompression.
// OUT_BUF_SIZE2 must be a power of 2 >= TINFL_LZ_DICT_SIZE(==32768) (because the low-level decompressor
//              not only writes, but reads from the output buffer as it decompresses)
//#define OUT_BUF_SIZE2 (TINFL_LZ_DICT_SIZE)
//#define OUT_BUF_SIZE2 (1024*32)


// -- OLD --
//int OUT_BUF_SIZE2 = sizeof(struct streets_index_data_block) * 1024;
//int t_OUT_BUF_SIZE2 = sizeof(struct town_index_data_block) * 1024;
//static uint8 s_outbuf[(sizeof(struct streets_index_data_block) * 1024)];
//static uint8 t_s_outbuf[(sizeof(struct town_index_data_block) * 1024)];
// -- OLD --
// -- NEW --
//x/ int OUT_BUF_SIZE2 = TINFL_LZ_DICT_SIZE * 32;
//x/ int t_OUT_BUF_SIZE2 = TINFL_LZ_DICT_SIZE * 32;
//x/ static uint8 s_outbuf[(sizeof(struct streets_index_data_block) * 1024 * 32)];
//x/ static uint8 t_s_outbuf[(sizeof(struct town_index_data_block) * 1024 * 32)];
// -- NEW --

// TINFL_LZ_DICT_SIZE = 32768
int OUT_BUF_SIZE2 =   (sizeof(struct streets_index_data_block) * 512);
int t_OUT_BUF_SIZE2 = (sizeof(struct town_index_data_block) * 512);
uint8 *s_outbuf;
uint8 *t_s_outbuf;

//static uint8 s_outbuf[OUT_BUF_SIZE2];

static long long street_index_size = 0; // this is the offset for town index start
// street index starts at "+sizeof(long long)"

#define my_min(a,b) (((a) < (b)) ? (a) : (b))
#define NUMBER_OF_TOWNS_TO_CACHE 1024

static struct town_index_data_block_c *town_lookup_cache = NULL;
static struct town_index_data_block_c *town_lookup_cache_cur = NULL;
static struct town_index_data_block_c *town_lookup_cache_found = NULL;
static int town_lookup_cache_items = 0;
static int town_lookup_cache_cur_item = 0;

void town_index_init_cache()
{
	//dbg(0,"+#+:enter\n");

	int s = sizeof(struct town_index_data_block_c) * NUMBER_OF_TOWNS_TO_CACHE;
	// dbg(0, "cache size=%d\n", s);

	town_lookup_cache = g_malloc(s);
	town_lookup_cache_cur = town_lookup_cache;
	town_lookup_cache_found = NULL;
	town_lookup_cache_items = 0;
	town_lookup_cache_cur_item = 0;

	//dbg(0,"+#+:leave\n");
}

void town_index_insert_cache(struct town_index_data_block* t, char* townname_long)
{
	//dbg(0,"+#+:enter\n");

	if (town_lookup_cache_items < NUMBER_OF_TOWNS_TO_CACHE)
	{
		// fill up cache until all slots are filled
		town_lookup_cache_cur->town_id = t->town_id;
		town_lookup_cache_cur->country_id = t->country_id;
		sprintf(town_lookup_cache_cur->town_name, "%s", townname_long);
		town_lookup_cache_items++;
	}
	else
	{
		// just fill cache and rotate if at end
		town_lookup_cache_cur->town_id = t->town_id;
		town_lookup_cache_cur->country_id = t->country_id;
		sprintf(town_lookup_cache_cur->town_name, "%s", townname_long);
	}

	if (town_lookup_cache_items == NUMBER_OF_TOWNS_TO_CACHE)
	{
		town_lookup_cache_cur_item = 0;
		town_lookup_cache_cur = town_lookup_cache;
	}
	else
	{
		town_lookup_cache_cur_item++;
		town_lookup_cache_cur++;
	}
}

int town_index_lookup_cache(long long townid)
{
	//dbg(0,"+#+:enter\n");

	int i;
	struct town_index_data_block_c* t;

	if (town_lookup_cache_items < 1)
	{
		return 0;
	}

	t = town_lookup_cache;
	for (i = 0; i < town_lookup_cache_items; i++)
	{
		if (t->town_id == townid)
		{
			// set pointer to found datablock
			town_lookup_cache_found = t;
			return 1;
		}
		t++;
	}

	return 0;
}

char* town_index_lookup(struct street_index_head *sih, long long townid)
{
	//dbg(0,"+#+:enter\n");

	char *townname = NULL;
	char *townname2 = NULL;
	int found = 0;
	int i;
	int split = 0;
	int split_count = 0;
	long long save_town_id;
	int save_country_id;

	if (townid == 0)
	{
		return townname;
	}

	if (town_lookup_cache == NULL)
	{
		town_index_init_cache();
	}

	if (town_index_lookup_cache(townid) == 1)
	{
		townname = g_strdup_printf("%s", town_lookup_cache_found->town_name);
		return townname;
	}

	sih->ti_ib = sih->ti_ib_mem;

	// find townid block
	found = sih->ti_ibs.count_of_index_blocks - 1; // set to last block
	for (i = 0; i < sih->ti_ibs.count_of_index_blocks; i++)
	{
		//dbg(0, "i=%d %lld %lld\n", i, townid, sih->ti_ib->first_id);

		if (townid < sih->ti_ib->first_id)
		{
			found = i - 1;
			break;
		}

		sih->ti_ib++;
	}

	if (found != -1)
	{
		//dbg(0, "found town block num=%d\n", found);

		town_index_setpos(sih, found); // move to correct index block

		while (town_index_read_data(sih))
		{
			//dbg(0, "id=%lld\n", sih->ti_db_ptr->town_id);

			if (offline_search_break_searching == 1)
			{
				break;
			}

			if (sih->ti_db_ptr->town_id == townid)
			{
				townname = g_strdup_printf("%s", sih->ti_db_ptr->town_name);
				//dbg(0,"found town:%s\n", townname);
				save_town_id = sih->ti_db_ptr->town_id;
				save_country_id = sih->ti_db_ptr->country_id;
				split = 1;
				split_count = 0;
				while ((town_index_read_data(sih))&&(split == 1))
				{
					split_count++;
					if ((split_count + 1) > MAX_TOWNNAME_SPLIT)
					{
						break;
					}

					if (sih->ti_db_ptr->town_id == 0)
					{
						//dbg(0," town-split:%s\n", sih->ti_db_ptr->town_name);
						townname2 = g_strdup_printf("%s%s", townname, sih->ti_db_ptr->town_name);
						g_free(townname);
						townname = townname2;
					}
					else
					{
						split = 0;
					}
				}
				break;
			}
		}
	}

	if (townname != NULL)
	{
		sih->ti_db_ptr->town_id = save_town_id; // set town and country to values before we read the "split"-blocks!
		sih->ti_db_ptr->country_id = save_country_id;
		town_index_insert_cache(sih->ti_db_ptr, townname);
	}

	//dbg(0, "return\n");

	return townname;
}

struct street_index_head* street_index_init(const char* idxfile_name)
{
	//dbg(0,"+#+:enter\n");

	struct street_index_head *ret=g_new0(struct street_index_head, 1);
	long s1;
	int b;
	char *index_file;

	index_file = g_strdup_printf("%s%s", navit_maps_dir, idxfile_name);
	ret->sif = fopen(index_file, "rb");
	g_free(index_file);

	fread(&street_index_size, sizeof(struct streets_index_index_block_start), 1, ret->sif);
	//dbg(0, "street_index_size=%lld\n", street_index_size);

	b = fread(&ret->si_ibs, sizeof(struct streets_index_index_block_start), 1, ret->sif);
	//dbg(0, "ftell=%d\n", ftell(ret->sif));
	//dbg(0, "items read=%d\n", b);

	//dbg(0, "struct size=%d\n", sizeof(struct streets_index_data_block));

	//dbg(0, "index entries=%d\n", ret->si_ibs.count_of_index_blocks);
	//dbg(0, "index entry size=%d\n", sizeof(struct streets_index_index_block));
	s1 = sizeof(struct streets_index_index_block) * ret->si_ibs.count_of_index_blocks;
	//dbg(0, "s1=%ld\n", s1);

	ret->si_ib_mem = g_malloc(s1);
	ret->si_ib = ret->si_ib_mem;

	ret->comp_status = 0;
	ret->t_comp_status = 0;

	//dbg(0, "ftell=%d\n", ftell(ret->sif));
	fread(ret->si_ib_mem, sizeof(struct streets_index_index_block), ret->si_ibs.count_of_index_blocks, ret->sif);
	//dbg(0, "ftell=%d\n", ftell(ret->sif));

	//dbg(0, "len=%lld\n", ret->si_ib->len);
	//dbg(0, "offset=%lld\n", ret->si_ib->offset);

	fseek(ret->sif, street_index_size + sizeof(long long), SEEK_SET); // seek to townindex header
	fread(&ret->ti_ibs, sizeof(struct town_index_index_block_start), 1, ret->sif);
	//dbg(0, "len=%lld\n", ret->ti_ibs.count_of_index_blocks);

	//dbg(0, "town index entries=%d\n", ret->ti_ibs.count_of_index_blocks);
	//dbg(0, "town index entry size=%d\n", sizeof(struct town_index_index_block));
	s1 = sizeof(struct town_index_index_block) * ret->ti_ibs.count_of_index_blocks;
	//dbg(0, "s1=%ld\n", s1);

	ret->ti_ib_mem = g_malloc(s1);
	ret->ti_ib = ret->ti_ib_mem;

	//dbg(0, "ftell=%d\n", ftell(ret->sif));
	fread(ret->ti_ib_mem, sizeof(struct town_index_index_block), ret->ti_ibs.count_of_index_blocks, ret->sif);
	//dbg(0, "ftell=%d\n", ftell(ret->sif));

	//dbg(0, "town len=%lld\n", ret->ti_ib->len);
	//dbg(0, "town offset=%lld\n", ret->ti_ib->offset);

	return ret;
}

void town_index_setpos(struct street_index_head *sih, int town_data_block_num)
{
	//dbg(0,"+#+:enter\n");

	if (sih->t_comp_status == 1)
	{
		town_index_close_compr(sih);
	}

	sih->ti_ib = (sih->ti_ib_mem + town_data_block_num);

	town_index_init_compr(sih, sih->ti_ib->len);

	//dbg(0, "len=%lld\n", sih->ti_ib->len);
	//dbg(0, "fid=%lld\n", sih->ti_ib->first_id);
	//dbg(0, "off=%lld\n", sih->ti_ib->offset);

	// if (sih->ti_ib->len >= sizeof(struct town_index_data_block))
	if (sih->ti_ib->len > 1)
	{
		//dbg(0, "fpos1=%d\n", ftell(sih->sif));
		fseek(sih->sif, sih->ti_ib->offset + sizeof(long long) + street_index_size, SEEK_SET);
		//dbg(0, "fpos2=%d\n", ftell(sih->sif));

		sih->t_data_count = 0;
		// move ptr to first data
		sih->ti_db_ptr = t_s_outbuf;
	}

}

void street_index_setpos(struct street_index_head *sih, int data_block_num)
{
	//dbg(0,"+#+:enter\n");

	if (sih->comp_status == 1)
	{
		street_index_close_compr(sih);
	}

	sih->si_ib = (sih->si_ib_mem + data_block_num);

	//dbg(0, "len=%lld\n", sih->si_ib->len);
	//dbg(0, "fl=%c off=%lld\n", sih->si_ib->first_letter, sih->si_ib->offset);

	street_index_init_compr(sih, sih->si_ib->len);

	//if (sih->si_ib->len >= sizeof(struct streets_index_data_block))
	if (sih->si_ib->len > 1) // what is the minimum compressed block size? (about 55 bytes now)
	{
		//dbg(0, "mem start=%d, cur pos=%d\n", sih->si_ib_mem, sih->si_ib);
		//dbg(0, "file offset=%d\n", sih->si_ib->offset);
		//dbg(0, "fpos s1=%d\n", ftell(sih->sif));
		fseek(sih->sif, sih->si_ib->offset + sizeof(long long), SEEK_SET); // add the "long long" from start of file to offset
		//dbg(0, "fpos s2=%d\n", ftell(sih->sif));

		sih->data_count = 0;
		// move ptr to first data
		sih->si_db_ptr = s_outbuf;
	}
}

int street_index_read_data(struct street_index_head *sih)
{
	//dbg(0,"+#+:enter\n");

	// fread(&sih->si_db, sizeof(struct streets_index_data_block), 1, sih->sif);


	//if (sih->si_ib->len < sizeof(struct streets_index_data_block))
	if (sih->si_ib->len <= 1) // minimum size of compressed block?
	{
		//dbg(0, "len=%d sof=%d\n", sih->si_ib->len, sizeof(struct streets_index_data_block));
		// no data for this letter
		return 0;
	}

	if (sih->data_count == 0)
	{
		// init
		sih->next_out = s_outbuf;
		sih->avail_out = OUT_BUF_SIZE2;

		// read data
		sih->data_count = street_index_decompress_data_block(sih);
		//dbg(0, "stat=%d\n", sih->data_count);

		if (sih->data_count <= 0)
		{
			// end of data
			return 0;
		}

		// move ptr to next data
		sih->si_db_ptr = s_outbuf;
	}
	else
	{
		sih->data_count = sih->data_count - sizeof(struct streets_index_data_block);

		if (sih->data_count > 0)
		{
			sih->si_db_ptr++;
			//dbg(0, "dc=%d ptr=%p\n", sih->data_count, sih->si_db_ptr);
		}
		else
		{
			// init
			sih->next_out = s_outbuf;
			sih->avail_out = OUT_BUF_SIZE2;

			// read data
			sih->data_count = street_index_decompress_data_block(sih);
			//dbg(0, "stat2=%d\n", sih->data_count);

			if (sih->data_count <= 0)
			{
				// end of data
				return 0;
			}

			// move ptr to next data
			sih->si_db_ptr = s_outbuf;
		}
	}

	//dbg(0, "data=%s, %d, %d, %lld\n", sih->si_db_ptr->street_name, sih->si_db_ptr->lat, sih->si_db_ptr->lon, sih->si_db_ptr->town_id);

	return 1;

	//if (ftell(sih->sif) > (sih->si_ib->offset + sih->si_ib->len))
	//{
	//	// end of data
	//	return 0;
	//}

	// more data found
	// return 1;
}

int town_index_read_data(struct street_index_head *sih)
{
	//dbg(0,"+#+:enter\n");

	// fread(&sih->si_db, sizeof(struct streets_index_data_block), 1, sih->sif);


	// if (sih->ti_ib->len < sizeof(struct town_index_data_block))
	if (sih->ti_ib->len <= 1)
	{
		// no data for this block
		//fprintf(stderr, "no data for this block\n");
		return 0;
	}

	if (sih->t_data_count == 0)
	{
		// init
		sih->t_next_out = t_s_outbuf;
		sih->t_avail_out = t_OUT_BUF_SIZE2;

		// read data
		sih->t_data_count = town_index_decompress_data_block(sih);
		//dbg(0, "stat=%d\n", sih->data_count);

		if (sih->t_data_count <= 0)
		{
			// end of data
			//fprintf(stderr, "end of data\n");
			return 0;
		}

		// move ptr to next data
		sih->ti_db_ptr = t_s_outbuf;
	}
	else
	{
		sih->t_data_count = sih->t_data_count - sizeof(struct town_index_data_block);

		if (sih->t_data_count > 0)
		{
			sih->ti_db_ptr++;
			//dbg(0, "dc=%d ptr=%p\n", sih->data_count, sih->si_db_ptr);
		}
		else
		{
			// init
			sih->t_next_out = t_s_outbuf;
			sih->t_avail_out = t_OUT_BUF_SIZE2;

			// read data
			sih->t_data_count = town_index_decompress_data_block(sih);
			//dbg(0, "stat2=%d\n", sih->data_count);

			if (sih->t_data_count <= 0)
			{
				// end of data
				//fprintf(stderr, "end of data (2)\n");
				return 0;
			}

			// move ptr to next data
			sih->ti_db_ptr = t_s_outbuf;
		}
	}

	//dbg(0, "data=%s, %d, %d, %lld\n", sih->si_db_ptr->street_name, sih->si_db_ptr->lat, sih->si_db_ptr->lon, sih->si_db_ptr->town_id);

	return 1;

	//if (ftell(sih->sif) > (sih->si_ib->offset + sih->si_ib->len))
	//{
	//	// end of data
	//	return 0;
	//}

	// more data found
	// return 1;
}

void street_index_close_compr(struct street_index_head *sih)
{
	//dbg(0,"+#+:enter\n");

	g_free(sih->inflator);
	sih->comp_status = 2;

	//dbg(0,"+#+:leave\n");
}

void town_index_close_compr(struct street_index_head *sih)
{
	//dbg(0,"+#+:enter\n");

	g_free(sih->t_inflator);
	sih->t_comp_status = 2;

	//dbg(0,"+#+:leave\n");
}

void street_index_init_compr(struct street_index_head *sih, long long size)
{
	//dbg(0,"+#+:enter\n");

	// decompress structure
	sih->inflator = g_new0(tinfl_decompressor, 1);
	sih->comp_status = 1;
	// decompress structure

	// Decompression.
	sih->infile_size = (uint) size;
	sih->infile_remaining = sih->infile_size;

	sih->next_in = s_inbuf;
	sih->avail_in = 0;
	sih->next_out = s_outbuf;
	sih->avail_out = OUT_BUF_SIZE2;

	sih->data_count = 0;

	tinfl_init(sih->inflator);

	//dbg(0,"+#+:leave\n");
}

void town_index_init_compr(struct street_index_head *sih, long long size)
{
	//dbg(0,"+#+:enter\n");

	// decompress structure
	sih->t_inflator = g_new0(tinfl_decompressor, 1);
	sih->t_comp_status = 1;
	// decompress structure

	// Decompression.
	sih->t_infile_size = (uint) size;
	sih->t_infile_remaining = sih->t_infile_size;

	sih->t_next_in = t_s_inbuf;
	sih->t_avail_in = 0;
	sih->t_next_out = t_s_outbuf;
	sih->t_avail_out = t_OUT_BUF_SIZE2;

	sih->t_data_count = 0;

	tinfl_init(sih->t_inflator);

	//dbg(0,"+#+:leave\n");
}

int street_index_decompress_data_block(struct street_index_head *sih)
{
	//dbg(0,"+#+:enter\n");

	// size_t total_in = 0, total_out = 0;
	// long file_loc;

	// Decompression.
	for (;;)
	{
		sih->in_bytes = 0;
		sih->out_bytes = 0;
		if (!sih->avail_in)
		{
			// Input buffer is empty, so read more bytes from input file.
			uint n = my_min(IN_BUF_SIZE2, sih->infile_remaining);

			//dbg(0, "reading bytes:%d remain=%d\n", n, sih->infile_remaining);

			if (fread(s_inbuf, 1, n, sih->sif) != n)
			{
				//printf("Failed reading from input file!\n");
				dbg(0, "Failed reading from input file!\n");
				//g_free(sih->inflator);
				return -1;
			}

			sih->next_in = s_inbuf;
			sih->avail_in = n;

			sih->infile_remaining -= n;
		}

		sih->in_bytes = sih->avail_in;
		sih->out_bytes = sih->avail_out;
		sih->miniz_status = tinfl_decompress(sih->inflator, (const mz_uint8 *) sih->next_in, &sih->in_bytes, s_outbuf, (mz_uint8 *) sih->next_out, &sih->out_bytes, (sih->infile_remaining ? TINFL_FLAG_HAS_MORE_INPUT : 0) | TINFL_FLAG_PARSE_ZLIB_HEADER);

		sih->avail_in -= sih->in_bytes;
		sih->next_in = (const mz_uint8 *) sih->next_in + sih->in_bytes;
		//total_in += sih->in_bytes;

		sih->avail_out -= sih->out_bytes;
		sih->next_out = (mz_uint8 *) sih->next_out + sih->out_bytes;
		//total_out += sih->out_bytes;

		if ((sih->miniz_status <= TINFL_STATUS_DONE) || (!sih->avail_out))
		{
			// Output buffer is full, or decompression is done, so write buffer to output file.
			uint n = OUT_BUF_SIZE2 - (uint) sih->avail_out;

			//dbg(0, "decompr: start=%p len=%d\n", (void *) sih->next_out, (int) n);
			//dbg(0, "decompr: start=%p len=%d\n", (void *) s_outbuf, (int) n);
			//dbg(0, "decompr: av in=%d av out=%d\n", sih->avail_in, sih->avail_out);
			//dbg(0, "decompr: nx in=%d nx out=%d\n", sih->next_in, sih->next_out);

			//struct streets_index_data_block *tmp = (struct streets_index_data_block *)s_outbuf;
			//dbg(0,"data=%s, %d, %d, %lld\n", tmp->street_name, tmp->lat, tmp->lon, tmp->town_id);

			//sih->next_out = s_outbuf;
			//sih->avail_out = OUT_BUF_SIZE2;

			return (int) n;

			//if (fwrite(s_outbuf, 1, n, pOutfile) != n)
			//{
			//  // printf("Failed writing to output file!\n");
			//	//g_free(inflator);
			//  return;
			//}
		}

		// If sih->miniz_status is <= TINFL_STATUS_DONE then either decompression is done or something went wrong.
		if (sih->miniz_status <= TINFL_STATUS_DONE)
		{
			if (sih->miniz_status == TINFL_STATUS_DONE)
			{
				// Decompression completed successfully.
				//dbg(0, "Decompression completed successfully\n");
				//break;
				return -2;
			}
			else
			{
				// Decompression failed.
				//printf("tinfl_decompress() failed with status %i!\n", sih->miniz_status);
				dbg(0, "tinfl_decompress() failed with status %i!\n", sih->miniz_status);

				//g_free(inflator);
				return -1;
			}
		}
	}

	//g_free(inflator);
	return -3;

}

int town_index_decompress_data_block(struct street_index_head *sih)
{
	//dbg(0,"+#+:enter\n");

	// size_t total_in = 0, total_out = 0;
	// long file_loc;

	// Decompression.
	for (;;)
	{
		sih->t_in_bytes = 0;
		sih->t_out_bytes = 0;
		if (!sih->t_avail_in)
		{
			// Input buffer is empty, so read more bytes from input file.
			uint n = my_min(t_IN_BUF_SIZE2, sih->t_infile_remaining);

			//dbg(0, "reading bytes:%d remain=%d\n", n, sih->t_infile_remaining);

			if (fread(t_s_inbuf, 1, n, sih->sif) != n)
			{
				//printf("Failed reading from input file!\n");
				dbg(0, "Failed reading from input file!\n");
				//g_free(sih->inflator);
				return -1;
			}

			sih->t_next_in = t_s_inbuf;
			sih->t_avail_in = n;

			sih->t_infile_remaining -= n;
		}

		sih->t_in_bytes = sih->t_avail_in;
		sih->t_out_bytes = sih->t_avail_out;
		sih->t_miniz_status = tinfl_decompress(sih->t_inflator, (const mz_uint8 *) sih->t_next_in, &sih->t_in_bytes, t_s_outbuf, (mz_uint8 *) sih->t_next_out, &sih->t_out_bytes, (sih->t_infile_remaining ? TINFL_FLAG_HAS_MORE_INPUT : 0) | TINFL_FLAG_PARSE_ZLIB_HEADER);

		sih->t_avail_in -= sih->t_in_bytes;
		sih->t_next_in = (const mz_uint8 *) sih->t_next_in + sih->t_in_bytes;
		//total_in += sih->in_bytes;

		sih->t_avail_out -= sih->t_out_bytes;
		sih->t_next_out = (mz_uint8 *) sih->t_next_out + sih->t_out_bytes;
		//total_out += sih->out_bytes;

		if ((sih->t_miniz_status <= TINFL_STATUS_DONE) || (!sih->t_avail_out))
		{
			// Output buffer is full, or decompression is done, so write buffer to output file.
			uint n = t_OUT_BUF_SIZE2 - (uint) sih->t_avail_out;

			//dbg(0, "decompr: start=%p len=%d\n", (void *) sih->next_out, (int) n);
			//dbg(0, "decompr: start=%p len=%d\n", (void *) s_outbuf, (int) n);
			//dbg(0, "decompr: av in=%d av out=%d\n", sih->avail_in, sih->avail_out);
			//dbg(0, "decompr: nx in=%d nx out=%d\n", sih->next_in, sih->next_out);

			//struct town_index_data_block *tmpt = (struct town_index_data_block *)t_s_outbuf;
			//dbg(0,"data=%lld %s\n", tmpt->town_id, tmpt->town_name);

			//sih->next_out = s_outbuf;
			//sih->avail_out = t_OUT_BUF_SIZE2;

			return (int) n;

			//if (fwrite(s_outbuf, 1, n, pOutfile) != n)
			//{
			//  // printf("Failed writing to output file!\n");
			//	//g_free(inflator);
			//  return;
			//}
		}

		// If sih->miniz_status is <= TINFL_STATUS_DONE then either decompression is done or something went wrong.
		if (sih->t_miniz_status <= TINFL_STATUS_DONE)
		{
			if (sih->t_miniz_status == TINFL_STATUS_DONE)
			{
				// Decompression completed successfully.
				//dbg(0, "Decompression completed successfully\n");
				//break;
				return -2;
			}
			else
			{
				// Decompression failed.
				//printf("tinfl_decompress() failed with status %i!\n", sih->miniz_status);
				dbg(0, "tinfl_decompress() failed with status %i!\n", sih->t_miniz_status);

				//g_free(inflator);
				return -1;
			}
		}
	}

	//g_free(inflator);
	return -3;

}

void street_index_close(struct street_index_head *sih)
{
	//dbg(0,"+#+:enter\n");

	g_free(sih->si_ib_mem);
	g_free(sih->ti_ib_mem);
	if (town_lookup_cache)
	{
		g_free(town_lookup_cache);
		town_lookup_cache = NULL;
	}
	fclose(sih->sif);
	g_free(sih);

	//dbg(0,"+#+:leave\n");
}


// func defs
int search_v2_work(char *addr, char *town, char* hn, int partial, struct jni_object *jni, const char* idxfile_name);
void search_v2(char *addr, char *town, char* hn, int partial, struct jni_object *jni);
// func defs


#include <sys/types.h>
#include <dirent.h>

void spill_index()
{
	// do not use anymore !!!!!!!
}

void search_v2(char *addr, char *town, char* hn, int partial, struct jni_object *jni)
{
	//dbg(0,"+#+:enter\n");

	int len;
	int len2;
	int len3;
	char *last_four;
	DIR* dirp;
	struct dirent *dp;
	int result_count = 0;
	int this_res_count = 0;
	void *p1;
	void *p2;
	void *p3;
	void *p4;

	s_outbuf = g_malloc(OUT_BUF_SIZE2);
	t_s_outbuf = g_malloc(t_OUT_BUF_SIZE2);
	p1 = s_outbuf;
	p2 = t_s_outbuf;

	s_inbuf = g_malloc(IN_BUF_SIZE2);
	t_s_inbuf = g_malloc(t_IN_BUF_SIZE2);
	p3 = s_inbuf;
	p4 = t_s_inbuf;


	len2 = strlen("navitmap_");
	len = len2 + 11; // should be 21 'navitmap_0%%.bin.idx'

	if ((town == NULL) || (strlen(town) == 0))
	{
		result_count = 0;
		this_res_count = 0;

		// ------------- SEARCH LOOP -------------
		// look for all the navitmap_0**.bin.idx files in mapdir, then call search in all of them
		// we dont have a TOWN name, so just search for "addr"
		dirp = opendir(navit_maps_dir);
		while ((dp = readdir(dirp)) != NULL)
		{
			if ((strlen(dp->d_name) == len) && (!strncmp(dp->d_name, "navitmap_", len2)))
			{
				len3 = strlen(dp->d_name);
				last_four = &dp->d_name[len3 - 4];
				if (!strcmp(last_four, ".idx"))
				{
					this_res_count = search_v2_work(addr, NULL, hn, partial, jni, dp->d_name);
					//dbg(0, "SLOOP:s=%s t=%s c=%i\n", addr, NULL, this_res_count);
					result_count = result_count + this_res_count;
				}
			}
		}
		closedir(dirp);
		//dbg(0, "SLOOP:sumc=%i\n", result_count)
		// ------------- SEARCH LOOP -------------
	}
	else
	{
		result_count = 0;
		this_res_count = 0;

		// ------------- SEARCH LOOP 1 -------------
		// look for all the navitmap_0**.bin.idx files in mapdir, then call search in all of them
		// first search for "addr<space>town" concated as street name
		char *addr_concat = g_strdup_printf("%s %s", addr, town);
		dirp = opendir(navit_maps_dir);
		while ((dp = readdir(dirp)) != NULL)
		{
			if ((strlen(dp->d_name) == len) && (!strncmp(dp->d_name, "navitmap_", len2)))
			{
				len3 = strlen(dp->d_name);
				last_four = &dp->d_name[len3 - 4];
				if (!strcmp(last_four, ".idx"))
				{
					this_res_count = search_v2_work(addr_concat, NULL, hn, partial, jni, dp->d_name);
					//dbg(0, "SLOOP1:s=%s t=%s c=%i\n", addr_concat, NULL, this_res_count);
					result_count = result_count + this_res_count;
				}
			}
		}
		closedir(dirp);
		//dbg(0, "SLOOP1:sumc=%i\n", result_count)
		g_free(addr_concat);
		// ------------- SEARCH LOOP 1 -------------

		// ------------- SEARCH LOOP 2 -------------
		// look for all the navitmap_0**.bin.idx files in mapdir, then call search in all of them
		// then search for "addr" and "town"
		dirp = opendir(navit_maps_dir);
		while ((dp = readdir(dirp)) != NULL)
		{
			if ((strlen(dp->d_name) == len) && (!strncmp(dp->d_name, "navitmap_", len2)))
			{
				len3 = strlen(dp->d_name);
				last_four = &dp->d_name[len3 - 4];
				if (!strcmp(last_four, ".idx"))
				{
					this_res_count = search_v2_work(addr, town, hn, partial, jni, dp->d_name);
					//dbg(0, "SLOOP2:s=%s t=%s c=%i\n", addr, town, this_res_count);
					result_count = result_count + this_res_count;
				}
			}
		}
		closedir(dirp);
		//dbg(0, "SLOOP2:sumc=%i\n", result_count)
		// ------------- SEARCH LOOP 2 -------------

		// if still no result then search only for "addr" as streetname
		if (result_count == 0)
		{
			// ------------- SEARCH LOOP 3 -------------
			// look for all the navitmap_0**.bin.idx files in mapdir, then call search in all of them
			dirp = opendir(navit_maps_dir);
			while ((dp = readdir(dirp)) != NULL)
			{
				if ((strlen(dp->d_name) == len) && (!strncmp(dp->d_name, "navitmap_", len2)))
				{
					len3 = strlen(dp->d_name);
					last_four = &dp->d_name[len3 - 4];
					if (!strcmp(last_four, ".idx"))
					{
						this_res_count = search_v2_work(addr, NULL, hn, partial, jni, dp->d_name);
						//dbg(0, "SLOOP3:s=%s t=%s c=%i\n", addr, NULL, this_res_count);
						result_count = result_count + this_res_count;
					}
				}
			}
			closedir(dirp);
			//dbg(0, "SLOOP3:sumc=%i\n", result_count)
			// ------------- SEARCH LOOP 3 -------------
		}

	}

	g_free(p1);
	g_free(p2);

	g_free(p3);
	g_free(p4);

	//dbg(0,"+#+:leave\n");
}

int search_v2_work(char *addr, char *town, char* hn, int partial, struct jni_object *jni, const char* idxfile_name)
{
	//dbg(0,"+#+:enter\n");

	char *buffer = NULL;
	float lat;
	float lng;
	char *address;
	char *address2;
	char *townname = NULL;
	char *addr_copy;
	char *addr2;
	char *addr3;
	char *addr3a;
	char *town_fold;
	char *tt;
	int i;
	int j;
	int br;
	int charlen;
	int found;
	int starts_with_utf8 = 0;
	int nd_with_utf8 = 0;
	int want_result;
	struct coord c3;
	static const char *alpha = "abcdefghijklmnopqrstuvwxyz";
	// char tmp_letter[STREET_INDEX_STREET_NAME_SIZE - 1];
	struct street_index_head *sih;
	int res_hn = 0;
	int search_results_found_ = 0;
	long long pos_now_pre;
	long long pos_now_aft;
	int max_townname_plus_1 = MAX_INDEXSEARCH_TOWNNAME + 1;


	if ((!addr) || (strlen(addr) < 1))
	{
		return;
	}

	// prepare search string
	addr2 = linguistics_casefold(addr);
	if (addr2)
	{
		addr3 = linguistics_remove_all_specials(addr2);
		if (addr3)
		{
			g_free(addr2);
			addr2 = addr3;
		}
		addr3 = linguistics_expand_special(addr2, 1);
		if (addr3)
		{
			g_free(addr2);
			addr2 = addr3;
		}
		addr_copy = addr2;
	}
	else
	{
		addr2 = g_strdup(addr);
		addr_copy = addr2;
	}

	// prepare town search string
	town_fold = linguistics_fold_and_prepare_complete(town, 0);


	//dbg(0,"town=%s townfold=%s street=%s hn=%s\n", town, town_fold, addr_copy, hn);

	sih = street_index_init(idxfile_name);

	// is first letter ascii or UTF-8?
	addr3 = g_utf8_find_next_char(addr_copy, NULL);
	charlen = addr3 - addr_copy;
	if (charlen > 1)
	{
		starts_with_utf8 = 1;
	}
	//dbg(0, "charlen=%d starts_with_utf8=%d\n", charlen, starts_with_utf8);

	// is second letter ascii or UTF-8?
	addr3a = g_utf8_find_next_char(addr3, NULL);
	charlen = addr3a - addr3;
	if (charlen > 1)
	{
		nd_with_utf8 = 1;
	}
	//dbg(0, "charlen=%d nd_with_utf8=%d\n", charlen, nd_with_utf8);


	// find starting letter of search string
	found = (703 - 1); // 26+1 letters ((26+1)*26 + 1) = 703
	br = 0;
	if (starts_with_utf8 == 0)
	{
		// check the first letter
		for (i = 0; i < 26; i++)
		{
			if (addr_copy[0] == alpha[i])
			{
				if ((strlen(addr_copy) > 1) && (nd_with_utf8 == 0))
				{
					//dbg(0, "i=%d\n", i);
					// check the second letter
					for (j = 0; j < 26; j++)
					{
						//dbg(0, "j=%d\n", j);
						if (addr_copy[1] == alpha[j])
						{
							found = (27 * i) + j;
							br = 1;
							break;
						}
					}
					if (br == 0)
					{
						// second letter has no match, use generic first letter block
						found = (27 * i) + 26;
					}
					br = 1;
					break;
				}
				else
				{
					// use generic first letter block
					found = (27 * i) + 26;
					br = 1;
					break;
				}
			}

			if (br)
			{
				break;
			}
		}
	}

	// dbg(0, "found pos=%d\n", found);

	street_index_setpos(sih, found);
	int found_data = 0;
	//int ddd = 0;

	while (street_index_read_data(sih))
	{
		// ------------------------------------------
		// ------------------------------------------
		// what streettypes do we want to include in search:
		// 1  -> normal street in native language
		// 2  -> english streetname
		// 3  -> alternative name
		// 4  -> town name
		// 40 -> POI
		// ------------------------------------------
		// ------------------------------------------
		//
		// dbg(0,"STREETINDEX:type=%d name=%s\n", (int)sih->si_db_ptr->street_type, sih->si_db_ptr->street_name);
#if 0
		if ((int)sih->si_db_ptr->street_type == 2)
		{
			continue;
		}

		if ((int)sih->si_db_ptr->street_type == 3)
		{
			continue;
		}
#endif
		//
		// ------------------------------------------
		// ------------------------------------------

		//ddd++;

		//if (ddd > 3)
		//{
		//	break;
		//}

		if (offline_search_break_searching == 1)
		{
			break;
		}


		// -- SANITY CHECK -- ----------------------
		if (! sih->si_db_ptr->street_name)
		{
			// the end is here
			break;
		}

		if (strlen(sih->si_db_ptr->street_name) == 0)
		{
			// the end is here
			break;
		}
		// -- SANITY CHECK -- ----------------------


		//dbg(0,"data=%s addr=%s\n", sih->si_db_ptr->street_name, addr_copy);
		if (!ascii_cmp_local_faster(sih->si_db_ptr->street_name, addr_copy, 1))
		{
			found_data = 1;
			if ((partial == 1) || (!ascii_cmp_local_faster(sih->si_db_ptr->street_name, addr_copy, partial)))
			{
				// townname = NULL;
				pos_now_pre = ftello(sih->sif); // 64bit
				townname = town_index_lookup(sih, sih->si_db_ptr->town_id);
				pos_now_aft = ftello(sih->sif); // 64bit
				if (pos_now_pre != pos_now_aft)
				{
					// dbg(0, "correct seek position\n");
					fseeko(sih->sif, (off_t)pos_now_pre, SEEK_SET);
				}

				// if we also have a search-town-name then check here
				if ((!town_fold)||(strlen(town_fold) < 1))
				{
					want_result = 1;
				}
				else
				{
					tt = linguistics_fold_and_prepare_complete(townname, 0);
					want_result = 1-(linguistics_compare_anywhere(tt, town_fold));
					//dbg(0, "want_result=%d tt=%s\n", want_result, tt);
				}

				if (want_result == 1)
				{

					// check for housenumber
					if ((hn != NULL) && (strlen(hn) > 0))
					{
						// now set coord of this item/street
						c3.y = sih->si_db_ptr->lat;
						c3.x = sih->si_db_ptr->lon;
						res_hn = search_address_housenumber_for_street(hn, sih->si_db_ptr->street_name, townname, &c3, partial, jni);
						search_results_found_ = search_results_found_ + res_hn;
					}

					if ((townname)&&(strcmp(townname, "(null)")))
					{
						address = g_strdup_printf("%s, %s", sih->si_db_ptr->street_name, townname);
					}
					else
					{
						address = g_strdup_printf("%s", sih->si_db_ptr->street_name);
					}


					if (strlen(address) > MAX_INDEXSEARCH_TOWNNAME)
					{
						address2 = address;
						address2[max_townname_plus_1] = '\0';
						address2 = linguistics_check_utf8_string(address2);
					}
					else
					{
						address2 = address;
					}

					if ((int)sih->si_db_ptr->street_type == 4)
					{
						// result is town/district
						buffer = g_strdup_printf("TWN:H0L0:%d:%d:%.*s", sih->si_db_ptr->lat, sih->si_db_ptr->lon, max_townname_plus_1, address2);
					}
					else if ((int)sih->si_db_ptr->street_type == 40)
					{
						// result is town/district
						buffer = g_strdup_printf("POI:H0L0:%d:%d:%.*s", sih->si_db_ptr->lat, sih->si_db_ptr->lon, max_townname_plus_1, address2);
					}
					else
					{
						// result is street
						buffer = g_strdup_printf("STR:H0L0:%d:%d:%.*s", sih->si_db_ptr->lat, sih->si_db_ptr->lon, max_townname_plus_1, address2);
					}
					// dbg(0,"buffer=%s\n", buffer);
#ifdef HAVE_API_ANDROID
					// return results to android as they come in ...
					android_return_search_result(jni, buffer);
					search_results_found_++;
#endif
					if (townname)
					{
						g_free(townname);
						townname = NULL;
					}

					if (address)
					{
						g_free(address);
						address = NULL;
					}

					if (buffer)
					{
						g_free(buffer);
						buffer = NULL;
					}
				}
			}
		}
		else
		{
			if (found_data == 1)
			{
				// no more matching data, stop searching
				break;
			}
		}
	}
	g_free(addr_copy);
	street_index_close(sih);

	//dbg(0,"+#+:leave\n");

	return search_results_found_;
}

