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
			dbg(0, "unknown search '%s'\n", attr_to_name(attr_type));
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
	//dbg(0,"## enter\n");
	//dbg(0,"## level=%d\n", level);
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
		//dbg(0,"## le=%p partial=%d\n", le, partial);
	}
	else if (search_attr->type == attr_postal)
	{
		g_free(this_->postal);
		this_->postal = g_strdup(search_attr->u.str);
	}
	//dbg(0,"## return\n");
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
	//dbg(0,"enter level=%d %d %d %p\n", level, id, mode, curr);
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
				//dbg(0,"found\n");
				return slc;
			}
		}
		curr = g_list_next(curr);
	}
	//dbg(0,"not found\n");
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
		//dbg(1, "town_assoc 0x%x 0x%x\n", attr.u.item->id_hi, attr.u.item->id_lo);
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
	dbg(1, "interpolate %s-%s %s\n", inter->first, inter->last, inter->curr);
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
	dbg(1, "interpolate result %s\n", inter->curr);
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
	dbg(1, "%s = %s - %s\n", str, first, last);
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
	dbg(1, "setup %s\n", attr_to_name(i0));
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

	// dbg(0,"001t: %s\n", item_to_name(item->type));

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
			dbg(1, "count=%d hn_length=%d hn_pos=%d (%s of %s-%s)\n", count, hn_length, hn_pos, inter->curr, inter->first, inter->last);
			if (!hn_length)
			{
				hn_length = 2;
				hn_pos = 1;
			}
			if (count == max)
				dbg(0, "coordinate overflow\n");
			for (i = 0; i < count - 1; i++)
			{
				distances[i] = navit_sqrt(transform_distance_sq(&c[i], &c[i + 1]));
				distance_sum += distances[i];
				dbg(1, "distance[%d]=%d\n", i, distances[i]);
			}
			dbg(1, "sum=%d\n", distance_sum);
			hn_distance = distance_sum * hn_pos / hn_length;
			dbg(1, "hn_distance=%d\n", hn_distance);
			i = 0;
			while (i < count - 1 && hn_distance > distances[i])
				hn_distance -= distances[i++];
			dbg(1, "remaining distance=%d from %d\n", hn_distance, distances[i]);
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

	//dbg(0,"@@@@ enter @@@@\n");

	ret->common.item = ret->common.unique = *item;
	//if (item_attr_get(item, attr_street_name, &attr))
	//	dbg(0,"xx1 %s\n",attr.u.str);
	if (item_attr_get(item, attr_house_number, &attr))
	{
		ret->house_number = map_convert_string(item->map, attr.u.str);
	}
	else
	{
		//if (item_attr_get(item, attr_street_name, &attr))
		//	dbg(0,"xx2 %s\n",attr.u.str);
		for (;;)
		{
			//dbg(0,"interpolate 11");
			ret->interpolation = 1;
			switch (inter->side)
			{
				case 0:
					//dbg(0,"interpolate 11 0");
					inter->side = -1;
					search_setup_interpolation(item, attr_house_number_left, attr_house_number_left_odd, attr_house_number_left_even, inter);
				case -1:
					//dbg(0,"interpolate 11 -1");
					if ((hn = search_interpolate(inter)))
						break;
					inter->side = 1;
					search_setup_interpolation(item, attr_house_number_right, attr_house_number_right_odd, attr_house_number_right_even, inter);
				case 1:
					//dbg(0,"interpolate 11 1");
					if ((hn = search_interpolate(inter)))
						break;
				default:
					//dbg(0,"interpolate 11 default");
					g_free(ret);
					return NULL;
			}
			if (search_match(hn, inter_match, inter_partial))
			{
				//dbg(0,"interpolate 22");
				//dbg(0,"match %s %s-%s\n",hn, inter->first, inter->last);
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
	//dbg(0,"enter\n");

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

	//dbg(0,"return\n");
}

char *
search_postal_merge(char *mask, char *new)
{
	int i;
	char *ret = NULL;
	dbg(1, "enter %s %s\n", mask, new);
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
	dbg(1, "merged %s with %s as %s\n", mask, new, ret);
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

	//dbg(0,"enter\n");

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

	//dbg(0,"******* enter *******\n");
	le = &this_->levels[level];
	//dbg(0,"le=%p\n", le);
	for (;;)
	{
		//dbg(0,"le->search=%p\n", le->search);
		if (!le->search)
		{
			//dbg(0,"partial=%d level=%d\n", le->partial, level);
			if (!level)
				le->parent = NULL;
			else
			{
				leu = &this_->levels[level - 1];
				//dbg(0,"leu->curr=%p\n", leu->curr);
				for (;;)
				{
					//dbg(0,"*********########");

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
				dbg(0,"mapset_search_new with item(%d,%d)\n", le->parent->item.id_hi, le->parent->item.id_lo);
			}
			else
			{
				dbg(0,"NO parent!!\n");
			}
			dbg(0,"############## attr=%s\n", attr_to_name(le->attr->type));
#endif
			le->search = mapset_search_new(this_->ms, &le->parent->item, le->attr, le->partial);
			// ** DOC ** mapset_search_new(struct mapset *ms, struct item *item, struct attr *search_attr, int partial)
			le->hash = g_hash_table_new(search_item_hash_hash, search_item_hash_equal);
		}

		//dbg(0,"le->search=%p\n", le->search);

		if (!this_->item)
		{
			//dbg(0,"sssss 1");
			this_->item = mapset_search_get_item(le->search);
			//dbg(0,"sssss 1 %p\n",this_->item);
		}

		if (this_->item)
		{
			void *p = NULL;
			//dbg(0,"id_hi=%d id_lo=%d\n", this_->item->id_hi, this_->item->id_lo);
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
			//dbg(0,"case x LEVEL start %d\n",level);
			switch (level)
			{
				case 0:
					//dbg(0,"case 0 COUNTRY");
					p = search_list_country_new(this_->item);
					this_->result.country = p;
					this_->result.country->common.parent = NULL;
					this_->item = NULL;
					break;
				case 1:
					//dbg(0,"case 1 TOWN");
					p = search_list_town_new(this_->item);
					this_->result.town = p;
					this_->result.town->common.parent = this_->levels[0].last->data;
					this_->result.country = this_->result.town->common.parent;
					this_->result.c = this_->result.town->common.c;
					this_->item = NULL;
					break;
				case 2:
					//dbg(0,"case 2 STREET");
					p = search_list_street_new(this_->item);
					this_->result.street = p;
					this_->result.street->common.parent = this_->levels[1].last->data;
					this_->result.town = this_->result.street->common.parent;
					this_->result.country = this_->result.town->common.parent;
					this_->result.c = this_->result.street->common.c;
					this_->item = NULL;
					break;
				case 3:
					//dbg(0,"case 3 HOUSENUMBER");
					has_street_name = 0;

					// if this housenumber has a streetname tag, set the name now
					if (item_attr_get(this_->item, attr_street_name, &attr2))
					{
						// dbg(0,"streetname: %s\n",attr2.u.str);
						has_street_name = 1;
					}

					//dbg(0,"case 3 XXXX 1\n");
					p = search_list_house_number_new(this_->item, &this_->inter, le->attr->u.str, le->partial);
					//dbg(0,"case 3 XXXX 2\n");
					if (!p)
					{
						interpolation_clear(&this_->inter);
						this_->item = NULL;
						continue;
					}
					//dbg(0,"case 3 XXXX 3\n");
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

					//dbg(0,"case 3 XXXX 4\n");
					if (has_street_name == 1)
					{
						gchar *tmp_name = g_strdup(attr2.u.str);
						this_->result.street->name = tmp_name;
						//dbg(0,"res streetname=%s\n",this_->result.street->name);
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
					//dbg(0,"case 3 XXXX 5\n");
					break;
			}
			// CASE END *********

			//dbg(0,"case end\n");

			if (p)
			{
				if (search_add_result(le, p))
				{
					//** this_->result.id++;
					this_->result.id = 0;
					//dbg(0,"++++ return result\n");
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

static GList *
search_address_housenumber_real(GList *result_list, struct search_list *sl, char *street_name, GList *phrases, GList *exclude1, GList *exclude2, GList *exclude3, int partial, struct jni_object *jni)
{
	struct search_list_result *slr;
	struct coord_geo g;
	struct coord c;

	//dbg(0,"enter\n");

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
							buffer = g_strdup_printf("SHN:H%dL%d:%f:%f:%.101s,%.7s %.101s, %.101s %.15s", slr->street->common.item.id_hi, slr->street->common.item.id_lo, g.lat, g.lng, slr->country->name, slr->town->common.postal, slr->town->common.town_name, slr->street->name,
									slr->house_number->house_number);
						}

						//dbg(0,"res=%s\n",buffer);

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
	//dbg(0,"enter\n");

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

			//dbg(0,"res=%s\n",buffer);

#ifdef HAVE_API_ANDROID
			// return results to android as they come in ...
			android_return_search_result(jni,buffer);
#endif
			count++;

			buffer2 = g_strdup_printf("%s", slr->street->name);
			//dbg(0,"b2:%s\n",buffer2);


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

				//dbg(0,"s0=%s\n",tmp->data);
				if (tmp != exclude1 && tmp != exclude2 && tmp != exclude3)
				{
					//dbg(0,"s=%s\n",tmp->data);

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

	//dbg(0,"return 2\n");
	return result_list;
}

static GList *
search_address__town(GList *result_list, struct search_list *sl, GList *phrases, GList *exclude1, GList *exclude2, int partial, struct jni_object *jni)
{
	//dbg(0,"enter\n");
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

		//dbg(0,"**res=%s\n",buffer);

		// deactivated now * result_list=g_list_prepend(result_list,g_strdup(buffer));
#ifdef HAVE_API_ANDROID
		// return results to android as they come in ...
		android_return_search_result(jni,buffer);
#endif

		count++;
		if (buffer)
			g_free(buffer);

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
				//dbg(0,"s=%s\n",tmp->data);
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

	//dbg(0,"return 2\n");
	return result_list;
}

static GList *
search_address__country(GList *result_list, struct search_list *sl, GList *phrases, GList *exclude, int partial, struct jni_object *jni)
{
	GList *tmp = phrases;
	int count = 0;
	struct attr attr;
	struct search_list_result *slr;
	//dbg(0,"enter\n");

	while ((slr = search_list_get_result(sl)))
	{
		//dbg(0,"1 slr=%p\n",slr->country);
		//dbg(0,"2 slr=%s\n",slr->country->name);
		//dbg(0,"3 slr=%s\n",slr->country->iso2);
		count++;
	}
	//dbg(0,"count %d\n",count);
	if (!count)
	{
		//dbg(0,"return 1");
		return result_list;
	}

	while (tmp)
	{
		if (tmp != exclude)
		{
			//dbg(0,"Is=%s\n",tmp->data);
			attr.type = attr_town_or_district_name;
			attr.u.str = tmp->data;
			search_list_search(sl, &attr, partial);
			result_list = search_address__town(result_list, sl, phrases, exclude, tmp, partial, jni);
		}
		//else
		//{
		//dbg(0,"Xs=%s\n",tmp->data);
		//}
		tmp = g_list_next(tmp);
	}
	//dbg(0,"return 2");
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

static struct country2 all_country_list[] =
{
{ 20, "AND", "AD", "AND", /* 020 */"Andorra" },
{ 784, "UAE", "AE", "ARE", /* 784 */
"United Arab Emirates" },
{ 4, "AFG", "AF", "AFG", /* 004 */"Afghanistan" },
{ 28, "AG", "AG", "ATG", /* 028 */"Antigua and Barbuda" },
{ 660, NULL, "AI", "AIA", /* 660 */"Anguilla" },
{ 8, "AL", "AL", "ALB", /* 008 */"Albania" },
{ 51, "ARM", "AM", "ARM", /* 051 */"Armenia" },
{ 530, "NA", "AN", "ANT", /* 530 */
"Netherlands Antilles" },
{ 24, "ANG", "AO", "AGO", /* 024 */"Angola" },
{ 10, NULL, "AQ", "ATA", /* 010 */"Antarctica" },
{ 32, "RA", "AR", "ARG", /* 032 */"Argentina" },
{ 16, NULL, "AS", "ASM", /* 016 */"American Samoa" },
{ 40, "A", "AT", "AUT", /* 040 */"Austria" },
{ 36, "AUS", "AU", "AUS", /* 036 */"Australia" },
{ 533, "ARU", "AW", "ABW", /* 533 */"Aruba" },
{ 248, "AX", "AX", "ALA", /* 248 */"Aland Islands" },
{ 31, "AZ", "AZ", "AZE", /* 031 */"Azerbaijan" },
{ 70, "BiH", "BA", "BIH", /* 070 */
"Bosnia and Herzegovina" },
{ 52, "BDS", "BB", "BRB", /* 052 */"Barbados" },
{ 50, "BD", "BD", "BGD", /* 050 */"Bangladesh" },
{ 56, "B", "BE", "BEL", /* 056 */"Belgium" },
{ 854, "BF", "BF", "BFA", /* 854 */"Burkina Faso" },
{ 100, "BG", "BG", "BGR", /* 100 */"Bulgaria" },
{ 48, "BRN", "BH", "BHR", /* 048 */"Bahrain" },
{ 108, "RU", "BI", "BDI", /* 108 */"Burundi" },
{ 204, "BJ", "BJ", "BEN", /* 204 */"Benin" },
{ 652, NULL, "BL", "BLM", /* 652 */"Saint Barthelemy" },
{ 60, NULL, "BM", "BMU", /* 060 */"Bermuda" },
{ 96, "BRU", "BN", "BRN", /* 096 */"Brunei Darussalam" },
{ 68, "BOL", "BO", "BOL", /* 068 */"Bolivia" },
{ 76, "BR", "BR", "BRA", /* 076 */"Brazil" },
{ 44, "BS", "BS", "BHS", /* 044 */"Bahamas" },
{ 64, "BHT", "BT", "BTN", /* 064 */"Bhutan" },
{ 74, NULL, "BV", "BVT", /* 074 */"Bouvet Island" },
{ 72, "RB", "BW", "BWA", /* 072 */"Botswana" },
{ 112, "BY", "BY", "BLR", /* 112 */"Belarus" },
{ 84, "BZ", "BZ", "BLZ", /* 084 */"Belize" },
{ 124, "CDN", "CA", "CAN", /* 124 */"Canada" },
{ 166, NULL, "CC", "CCK", /* 166 */
"Cocos (Keeling) Islands" },
{ 180, "CGO", "CD", "COD", /* 180 */
"Congo, Democratic Republic of the" },
{ 140, "RCA", "CF", "CAF", /* 140 */
"Central African Republic" },
{ 178, NULL, "CG", "COG", /* 178 */"Congo" },
{ 756, "CH", "CH", "CHE", /* 756 */"Switzerland" },
{ 384, "CI", "CI", "CIV", /* 384 */"Cote d'Ivoire" },
{ 184, NULL, "CK", "COK", /* 184 */"Cook Islands" },
{ 152, "RCH", "CL", "CHL", /* 152 */"Chile" },
{ 120, "CAM", "CM", "CMR", /* 120 */"Cameroon" },
{ 156, "RC", "CN", "CHN", /* 156 */"China" },
{ 170, "CO", "CO", "COL", /* 170 */"Colombia" },
{ 188, "CR", "CR", "CRI", /* 188 */"Costa Rica" },
{ 192, "C", "CU", "CUB", /* 192 */"Cuba" },
{ 132, "CV", "CV", "CPV", /* 132 */"Cape Verde" },
{ 162, NULL, "CX", "CXR", /* 162 */"Christmas Island" },
{ 196, "CY", "CY", "CYP", /* 196 */"Cyprus" },
{ 203, "CZ", "CZ", "CZE", /* 203 */"Czech Republic" },
{ 276, "D", "DE", "DEU", /* 276 */"Germany" },
{ 262, "DJI", "DJ", "DJI", /* 262 */"Djibouti" },
{ 208, "DK", "DK", "DNK", /* 208 */"Denmark" },
{ 212, "WD", "DM", "DMA", /* 212 */"Dominica" },
{ 214, "DOM", "DO", "DOM", /* 214 */
"Dominican Republic" },
{ 12, "DZ", "DZ", "DZA", /* 012 */"Algeria" },
{ 218, "EC", "EC", "ECU", /* 218 */"Ecuador" },
{ 233, "EST", "EE", "EST", /* 233 */"Estonia" },
{ 818, "ET", "EG", "EGY", /* 818 */"Egypt" },
{ 732, "WSA", "EH", "ESH", /* 732 */"Western Sahara" },
{ 232, "ER", "ER", "ERI", /* 232 */"Eritrea" },
{ 724, "E", "ES", "ESP", /* 724 */"Spain" },
{ 231, "ETH", "ET", "ETH", /* 231 */"Ethiopia" },
{ 246, "FIN", "FI", "FIN", /* 246 */"Finland" },
{ 242, "FJI", "FJ", "FJI", /* 242 */"Fiji" },
{ 238, NULL, "FK", "FLK", /* 238 */
"Falkland Islands (Malvinas)" },
{ 583, "FSM", "FM", "FSM", /* 583 */
"Micronesia, Federated States of" },
{ 234, "FO", "FO", "FRO", /* 234 */"Faroe Islands" },
{ 250, "F", "FR", "FRA", /* 250 */"France" },
{ 266, "G", "GA", "GAB", /* 266 */"Gabon" },
{ 826, "GB", "GB", "GBR", /* 826 */"United Kingdom" },
{ 308, "WG", "GD", "GRD", /* 308 */"Grenada" },
{ 268, "GE", "GE", "GEO", /* 268 */"Georgia" },
{ 254, NULL, "GF", "GUF", /* 254 */"French Guiana" },
{ 831, NULL, "GG", "GGY", /* 831 */"Guernsey" },
{ 288, "GH", "GH", "GHA", /* 288 */"Ghana" },
{ 292, "GBZ", "GI", "GIB", /* 292 */"Gibraltar" },
{ 304, "KN", "GL", "GRL", /* 304 */"Greenland" },
{ 270, "WAG", "GM", "GMB", /* 270 */"Gambia" },
{ 324, "RG", "GN", "GIN", /* 324 */"Guinea" },
{ 312, NULL, "GP", "GLP", /* 312 */"Guadeloupe" },
{ 226, "GQ", "GQ", "GNQ", /* 226 */"Equatorial Guinea" },
{ 300, "GR", "GR", "GRC", /* 300 */"Greece" },
{ 239, NULL, "GS", "SGS", /* 239 */
"South Georgia and the South Sandwich Islands" },
{ 320, "GCA", "GT", "GTM", /* 320 */"Guatemala" },
{ 316, NULL, "GU", "GUM", /* 316 */"Guam" },
{ 624, "GUB", "GW", "GNB", /* 624 */"Guinea-Bissau" },
{ 328, "GUY", "GY", "GUY", /* 328 */"Guyana" },
{ 344, "HK", "HK", "HKG", /* 344 */"Hong Kong" },
{ 334, NULL, "HM", "HMD", /* 334 */
"Heard Island and McDonald Islands" },
{ 340, "HN", "HN", "HND", /* 340 */"Honduras" },
{ 191, "HR", "HR", "HRV", /* 191 */"Croatia" },
{ 332, "RH", "HT", "HTI", /* 332 */"Haiti" },
{ 348, "H", "HU", "HUN", /* 348 */"Hungary" },
{ 360, "RI", "ID", "IDN", /* 360 */"Indonesia" },
{ 372, "IRL", "IE", "IRL", /* 372 */"Ireland" },
{ 376, "IL", "IL", "ISR", /* 376 */"Israel" },
{ 833, NULL, "IM", "IMN", /* 833 */"Isle of Man" },
{ 356, "IND", "IN", "IND", /* 356 */"India" },
{ 86, NULL, "IO", "IOT", /* 086 */
"British Indian Ocean Territory" },
{ 368, "IRQ", "IQ", "IRQ", /* 368 */"Iraq" },
{ 364, "IR", "IR", "IRN", /* 364 */
"Iran, Islamic Republic of" },
{ 352, "IS", "IS", "ISL", /* 352 */"Iceland" },
{ 380, "I", "IT", "ITA", /* 380 */"Italy" },
{ 832, NULL, "JE", "JEY", /* 832 */"Jersey" },
{ 388, "JA", "JM", "JAM", /* 388 */"Jamaica" },
{ 400, "JOR", "JO", "JOR", /* 400 */"Jordan" },
{ 392, "J", "JP", "JPN", /* 392 */"Japan" },
{ 404, "EAK", "KE", "KEN", /* 404 */"Kenya" },
{ 417, "KS", "KG", "KGZ", /* 417 */"Kyrgyzstan" },
{ 116, "K", "KH", "KHM", /* 116 */"Cambodia" },
{ 296, "KIR", "KI", "KIR", /* 296 */"Kiribati" },
{ 174, "COM", "KM", "COM", /* 174 */"Comoros" },
{ 659, "KAN", "KN", "KNA", /* 659 */
"Saint Kitts and Nevis" },
{ 408, "KP", "KP", "PRK", /* 408 */
"Korea, Democratic People's Republic of" },
{ 410, "ROK", "KR", "KOR", /* 410 */
"Korea, Republic of" },
{ 414, "KWT", "KW", "KWT", /* 414 */"Kuwait" },
{ 136, NULL, "KY", "CYM", /* 136 */"Cayman Islands" },
{ 398, "KZ", "KZ", "KAZ", /* 398 */"Kazakhstan" },
{ 418, "LAO", "LA", "LAO", /* 418 */
"Lao People's Democratic Republic" },
{ 422, "RL", "LB", "LBN", /* 422 */"Lebanon" },
{ 662, "WL", "LC", "LCA", /* 662 */"Saint Lucia" },
{ 438, "FL", "LI", "LIE", /* 438 */"Liechtenstein" },
{ 144, "CL", "LK", "LKA", /* 144 */"Sri Lanka" },
{ 430, "LB", "LR", "LBR", /* 430 */"Liberia" },
{ 426, "LS", "LS", "LSO", /* 426 */"Lesotho" },
{ 440, "LT", "LT", "LTU", /* 440 */"Lithuania" },
{ 442, "L", "LU", "LUX", /* 442 */"Luxembourg" },
{ 428, "LV", "LV", "LVA", /* 428 */"Latvia" },
{ 434, "LAR", "LY", "LBY", /* 434 */
"Libyan Arab Jamahiriya" },
{ 504, "MA", "MA", "MAR", /* 504 */"Morocco" },
{ 492, "MC", "MC", "MCO", /* 492 */"Monaco" },
{ 498, "MD", "MD", "MDA", /* 498 */
"Moldova, Republic of" },
{ 499, "MNE", "ME", "MNE", /* 499 */"Montenegro" },
{ 663, NULL, "MF", "MAF", /* 663 */
"Saint Martin (French part)" },
{ 450, "RM", "MG", "MDG", /* 450 */"Madagascar" },
{ 584, "MH", "MH", "MHL", /* 584 */"Marshall Islands" },
{ 807, "MK", "MK", "MKD", /* 807 */
"Macedonia, the former Yugoslav Republic of" },
{ 466, "RMM", "ML", "MLI", /* 466 */"Mali" },
{ 104, "MYA", "MM", "MMR", /* 104 */"Myanmar" },
{ 496, "MGL", "MN", "MNG", /* 496 */"Mongolia" },
{ 446, NULL, "MO", "MAC", /* 446 */"Macao" },
{ 580, NULL, "MP", "MNP", /* 580 */
"Northern Mariana Islands" },
{ 474, NULL, "MQ", "MTQ", /* 474 */"Martinique" },
{ 478, "RIM", "MR", "MRT", /* 478 */"Mauritania" },
{ 500, NULL, "MS", "MSR", /* 500 */"Montserrat" },
{ 470, "M", "MT", "MLT", /* 470 */"Malta" },
{ 480, "MS", "MU", "MUS", /* 480 */"Mauritius" },
{ 462, "MV", "MV", "MDV", /* 462 */"Maldives" },
{ 454, "MW", "MW", "MWI", /* 454 */"Malawi" },
{ 484, "MEX", "MX", "MEX", /* 484 */"Mexico" },
{ 458, "MAL", "MY", "MYS", /* 458 */"Malaysia" },
{ 508, "MOC", "MZ", "MOZ", /* 508 */"Mozambique" },
{ 516, "NAM", "NA", "NAM", /* 516 */"Namibia" },
{ 540, "NCL", "NC", "NCL", /* 540 */"New Caledonia" },
{ 562, "RN", "NE", "NER", /* 562 */"Niger" },
{ 574, NULL, "NF", "NFK", /* 574 */"Norfolk Island" },
{ 566, "NGR", "NG", "NGA", /* 566 */"Nigeria" },
{ 558, "NIC", "NI", "NIC", /* 558 */"Nicaragua" },
{ 528, "NL", "NL", "NLD", /* 528 */"Netherlands" },
{ 578, "N", "NO", "NOR", /* 578 */"Norway" },
{ 524, "NEP", "NP", "NPL", /* 524 */"Nepal" },
{ 520, "NAU", "NR", "NRU", /* 520 */"Nauru" },
{ 570, NULL, "NU", "NIU", /* 570 */"Niue" },
{ 554, "NZ", "NZ", "NZL", /* 554 */"New Zealand" },
{ 512, "OM", "OM", "OMN", /* 512 */"Oman" },
{ 591, "PA", "PA", "PAN", /* 591 */"Panama" },
{ 604, "PE", "PE", "PER", /* 604 */"Peru" },
{ 258, NULL, "PF", "PYF", /* 258 */"French Polynesia" },
{ 598, "PNG", "PG", "PNG", /* 598 */"Papua New Guinea" },
{ 608, "RP", "PH", "PHL", /* 608 */"Philippines" },
{ 586, "PK", "PK", "PAK", /* 586 */"Pakistan" },
{ 616, "PL", "PL", "POL", /* 616 */"Poland" },
{ 666, NULL, "PM", "SPM", /* 666 */
"Saint Pierre and Miquelon" },
{ 612, NULL, "PN", "PCN", /* 612 */"Pitcairn" },
{ 630, "PRI", "PR", "PRI", /* 630 */"Puerto Rico" },
{ 275, "AUT", "PS", "PSE", /* 275 */
"Palestinian Territory, Occupied" },
{ 620, "P", "PT", "PRT", /* 620 */"Portugal" },
{ 585, "PAL", "PW", "PLW", /* 585 */"Palau" },
{ 600, "PY", "PY", "PRY", /* 600 */"Paraguay" },
{ 634, "Q", "QA", "QAT", /* 634 */"Qatar" },
{ 638, NULL, "RE", "REU", /* 638 */"Reunion" },
{ 642, "RO", "RO", "ROU", /* 642 */"Romania" },
{ 688, "SRB", "RS", "SRB", /* 688 */"Serbia" },
{ 643, "RUS", "RU", "RUS", /* 643 */
"Russian Federation" },
{ 646, "RWA", "RW", "RWA", /* 646 */"Rwanda" },
{ 682, "KSA", "SA", "SAU", /* 682 */"Saudi Arabia" },
{ 90, "SOL", "SB", "SLB", /* 090 */"Solomon Islands" },
{ 690, "SY", "SC", "SYC", /* 690 */"Seychelles" },
{ 736, "SUD", "SD", "SDN", /* 736 */"Sudan" },
{ 752, "S", "SE", "SWE", /* 752 */"Sweden" },
{ 702, "SGP", "SG", "SGP", /* 702 */"Singapore" },
{ 654, NULL, "SH", "SHN", /* 654 */"Saint Helena" },
{ 705, "SLO", "SI", "SVN", /* 705 */"Slovenia" },
{ 744, NULL, "SJ", "SJM", /* 744 */
"Svalbard and Jan Mayen" },
{ 703, "SK", "SK", "SVK", /* 703 */"Slovakia" },
{ 694, "WAL", "SL", "SLE", /* 694 */"Sierra Leone" },
{ 674, "RSM", "SM", "SMR", /* 674 */"San Marino" },
{ 686, "SN", "SN", "SEN", /* 686 */"Senegal" },
{ 706, "SO", "SO", "SOM", /* 706 */"Somalia" },
{ 740, "SME", "SR", "SUR", /* 740 */"Suriname" },
{ 678, "STP", "ST", "STP", /* 678 */
"Sao Tome and Principe" },
{ 222, "ES", "SV", "SLV", /* 222 */"El Salvador" },
{ 760, "SYR", "SY", "SYR", /* 760 */
"Syrian Arab Republic" },
{ 748, "SD", "SZ", "SWZ", /* 748 */"Swaziland" },
{ 796, NULL, "TC", "TCA", /* 796 */
"Turks and Caicos Islands" },
{ 148, "TD", "TD", "TCD", /* 148 */"Chad" },
{ 260, "ARK", "TF", "ATF", /* 260 */
"French Southern Territories" },
{ 768, "RT", "TG", "TGO", /* 768 */"Togo" },
{ 764, "T", "TH", "THA", /* 764 */"Thailand" },
{ 762, "TJ", "TJ", "TJK", /* 762 */"Tajikistan" },
{ 772, NULL, "TK", "TKL", /* 772 */"Tokelau" },
{ 626, "TL", "TL", "TLS", /* 626 */"Timor-Leste" },
{ 795, "TM", "TM", "TKM", /* 795 */"Turkmenistan" },
{ 788, "TN", "TN", "TUN", /* 788 */"Tunisia" },
{ 776, "TON", "TO", "TON", /* 776 */"Tonga" },
{ 792, "TR", "TR", "TUR", /* 792 */"Turkey" },
{ 780, "TT", "TT", "TTO", /* 780 */
"Trinidad and Tobago" },
{ 798, "TUV", "TV", "TUV", /* 798 */"Tuvalu" },
{ 158, NULL, "TW", "TWN", /* 158 */
"Taiwan, Province of China" },
{ 834, "EAT", "TZ", "TZA", /* 834 */
"Tanzania, United Republic of" },
{ 804, "UA", "UA", "UKR", /* 804 */"Ukraine" },
{ 800, "EAU", "UG", "UGA", /* 800 */"Uganda" },
{ 581, NULL, "UM", "UMI", /* 581 */
"United States Minor Outlying Islands" },
{ 840, "USA", "US", "USA", /* 840 */"United States" },
{ 858, "ROU", "UY", "URY", /* 858 */"Uruguay" },
{ 860, "UZ", "UZ", "UZB", /* 860 */"Uzbekistan" },
{ 336, "SCV", "VA", "VAT", /* 336 */
"Holy See (Vatican City State)" },
{ 670, "WV", "VC", "VCT", /* 670 */"Saint Vincent and the Grenadines" },
{ 862, "YV", "VE", "VEN", /* 862 */"Venezuela" },
{ 92, NULL, "VG", "VGB", /* 092 */ "Virgin Islands, British" },
{ 850, NULL, "VI", "VIR", /* 850 */ "Virgin Islands, U.S." },
{ 704, "VN", "VN", "VNM", /* 704 */"Viet Nam" },
{ 548, "VAN", "VU", "VUT", /* 548 */"Vanuatu" },
{ 876, NULL, "WF", "WLF", /* 876 */"Wallis and Futuna" },
{ 882, "WS", "WS", "WSM", /* 882 */"Samoa" },
{ 887, "YAR", "YE", "YEM", /* 887 */"Yemen" },
{ 175, NULL, "YT", "MYT", /* 175 */"Mayotte" },
{ 710, "ZA", "ZA", "ZAF", /* 710 */"South Africa" },
{ 894, "Z", "ZM", "ZMB", /* 894 */"Zambia" },
{ 716, "ZW", "ZW", "ZWE", /* 716 */"Zimbabwe" },
{ 999, "*", "*", "*", /* 999 */"Unknown" }, };

static int ascii_cmp_local(char *name, char *match, int partial)
{
	char *s1 = linguistics_casefold(name);
	char *s2 = linguistics_casefold(match);
	int ret = linguistics_compare(s1, s2, partial);
	g_free(s1);
	g_free(s2);
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
	dbg(0, "in lat=%f,lng=%f\n", search_center->lat, search_center->lng);
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
	dbg(0, "out x=%d,y=%d,r=%d\n", center99.x, center99.y, search_radius_this);

	struct map_selection *sel = map_selection_rect_new(&center99, search_radius_this, search_order);
	sel->range.min = type_town_label;
	sel->range.max = type_area;

	while (msh && (map = mapset_next(msh, 0)))
	{
		if (map_get_attr(map, attr_name, &map_name_attr, NULL))
		{
			if (strncmp("_ms_sdcard_map:", map_name_attr.u.str, 15) == 0)
			{
				if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/navitmap", map_name_attr.u.str, 38) == 0)
				{
					// its an sdcard map
					//dbg(0,"map name=%s",map_name_attr.u.str);
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

									// dbg(0,"town name=%s\n", attr.u.str);

									phrases = phrases_first;
									while (phrases)
									{

										if (offline_search_break_searching == 1)
										{
											break;
										}

										if (!ascii_cmp_local(attr.u.str, phrases->data, partial))
										{
											// dbg(0,"matched town name=%s want=%s\n", attr.u.str, phrases->data);
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

									// dbg(0,"town name=%s\n", attr.u.str);

									phrases = phrases_first;
									while (phrases)
									{
										if (offline_search_break_searching == 1)
										{
											break;
										}

										if (!ascii_cmp_local(attr.u.str, phrases->data, partial))
										{
											// dbg(0,"matched town name=%s want=%s\n", attr.u.str, phrases->data);
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
									// dbg(0,"street1=%s\n",map_convert_string(item->map, attr.u.str));
									if ((streetname_last == NULL) || (strcmp(streetname_last, attr.u.str) != 0))
									{
										// dbg(0,"street2=%s\n",map_convert_string(item->map, attr.u.str));
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
												//dbg(0,"street3=%s\n",buffer);
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
									//dbg(0,"street systematic=%s\n",map_convert_string(item->map, attr.u.str));

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
									//dbg(0,"street systematic=%s\n",map_convert_string(item->map, attr.u.str));

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

	dbg(0, "-- START --\n");

	// normal search stuff -------- START ----------
	if (search_country_flags == 1)
	{
		dbg(0, "-- country default start --\n");
		//while (phrases)
		//{
		// dbg(0,"s=%s\n",phrases->data);
		// set default country
		search_list_search(sl, country_default(), 0);
		ret = search_address__country(ret, sl, phrases, NULL, partial, jni);
		// phrases=g_list_next(phrases);
		//}
		dbg(0, "-- country default end --\n");
	}
	else if (search_country_flags == 2)
	{
		dbg(0, "-- country sel:%s start --\n", search_country_string);
		// set a country
		struct attr country;
		country.type = attr_country_iso2;
		country.u.str = search_country_string;
		//while (phrases)
		//{
		// dbg(0,"s=%s\n",phrases->data);
		search_list_search(sl, &country, 0);
		// set a country
		ret = search_address__country(ret, sl, phrases, NULL, partial, jni);
		//phrases=g_list_next(phrases);
		//}
		dbg(0, "-- country sel:%s end --\n", search_country_string);
	}
	else // flags==3
	{
		dbg(0, "-- country all start --\n");
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
				// dbg(0,"s country=%s\n",all_country_list[j1].iso2);
				// dbg(0,"s=%s\n",phrases->data);
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
		dbg(0, "-- country all end --\n");
	}
	// normal search stuff --------  END  ----------

	if (phrases_first)
	{
		g_list_free(phrases_first);
	}

	dbg(0, "--  END  --\n");

	g_free(str);
	return ret;
}

