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

#include <stdio.h>
#include <string.h>
#include <glib.h>
#include "coord.h"
#include "debug.h"
#include "item.h"
#include "map.h"
#include "transform.h"

struct item_name
{
	enum item_type item;
	char *name;
};

struct item_range item_range_all = { type_none, type_last };

struct default_flags
{
	enum item_type type;
	int flags;
};

struct item busy_item;

struct default_flags default_flags2[] = { { type_street_nopass, NAVIT_AF_PBH }, { type_street_0, NAVIT_AF_ALL }, { type_street_1_city, NAVIT_AF_ALL }, { type_street_2_city, NAVIT_AF_ALL }, { type_street_3_city, NAVIT_AF_ALL }, { type_street_4_city, NAVIT_AF_ALL }, { type_highway_city, NAVIT_AF_MOTORIZED_FAST }, { type_street_1_land, NAVIT_AF_ALL }, { type_street_2_land, NAVIT_AF_ALL }, { type_street_3_land, NAVIT_AF_ALL }, { type_street_4_land, NAVIT_AF_ALL }, { type_street_n_lanes, NAVIT_AF_MOTORIZED_FAST }, { type_highway_land, NAVIT_AF_MOTORIZED_FAST }, { type_ramp, NAVIT_AF_MOTORIZED_FAST }, { type_roundabout, NAVIT_AF_ALL }, { type_ferry, NAVIT_AF_ALL }, { type_cycleway, NAVIT_AF_PBH }, { type_track_paved, NAVIT_AF_ALL }, { type_track_gravelled, NAVIT_AF_ALL }, { type_track_unpaved, NAVIT_AF_ALL }, { type_track_ground, NAVIT_AF_ALL }, { type_track_grass, NAVIT_AF_ALL }, { type_footway, NAVIT_AF_PBH }, { type_living_street, NAVIT_AF_ALL }, { type_street_service, NAVIT_AF_ALL }, { type_street_parking_lane, NAVIT_AF_ALL }, { type_bridleway, NAVIT_AF_PBH }, { type_path, NAVIT_AF_PBH }, { type_steps, NAVIT_AF_PBH }, { type_street_pedestrian, NAVIT_AF_PBH }, };

struct item_name item_names[] = {
#define ITEM2(x,y) ITEM(y)
#define ITEM(x) { type_##x, #x },
#include "item_def.h"
#undef ITEM2
#undef ITEM
		};

static GHashTable *default_flags_hash;

int *
item_get_default_flags(enum item_type type)
{
	if (!default_flags_hash)
	{
		int i;
		default_flags_hash = g_hash_table_new(NULL, NULL);
		for (i = 0; i < sizeof(default_flags2) / sizeof(struct default_flags); i++)
		{
			g_hash_table_insert(default_flags_hash, (void *) (long) default_flags2[i].type, &default_flags2[i].flags);
		}
	}
	return g_hash_table_lookup(default_flags_hash, (void *) (long) type);
}

void item_cleanup(void)
{
	if (default_flags_hash)
		g_hash_table_destroy(default_flags_hash);
}

void item_coord_rewind(struct item *it)
{
	it->meth->item_coord_rewind(it->priv_data);
}

int item_coord_get(struct item *it, struct coord *c, int count)
{
	return it->meth->item_coord_get(it->priv_data, c, count);
}

int item_coord_set(struct item *it, struct coord *c, int count, enum change_mode mode)
{
	if (!it->meth->item_coord_set)
		return 0;
	return it->meth->item_coord_set(it->priv_data, c, count, mode);
}

int item_coord_get_within_selection(struct item *it, struct coord *c, int count, struct map_selection *sel)
{
	int i, ret = it->meth->item_coord_get(it->priv_data, c, count);
	struct coord_rect r;
	struct map_selection *curr;
	if (ret <= 0 || !sel)
		return ret;
	r.lu = c[0];
	r.rl = c[0];
	for (i = 1; i < ret; i++)
	{
		if (r.lu.x > c[i].x)
			r.lu.x = c[i].x;
		if (r.rl.x < c[i].x)
			r.rl.x = c[i].x;
		if (r.rl.y > c[i].y)
			r.rl.y = c[i].y;
		if (r.lu.y < c[i].y)
			r.lu.y = c[i].y;
	}
	curr = sel;
	while (curr)
	{
		struct coord_rect *sr = &curr->u.c_rect;
		if (r.lu.x <= sr->rl.x && r.rl.x >= sr->lu.x && r.lu.y >= sr->rl.y && r.rl.y <= sr->lu.y)
			return ret;
		curr = curr->next;
	}
	return 0;
}

int item_coord_get_pro(struct item *it, struct coord *c, int count, enum projection to)
{
	int ret = item_coord_get(it, c, count);
	int i;
	enum projection from = map_projection(it->map);
	if (from != to)
		for (i = 0; i < count; i++)
			transform_from_to(c + i, from, c + i, to);
	return ret;
}

int item_coord_is_node(struct item *it)
{
	if (it->meth->item_coord_is_node)
		return it->meth->item_coord_is_node(it->priv_data);
	return 0;
}

void item_attr_rewind(struct item *it)
{
	it->meth->item_attr_rewind(it->priv_data);
}

int item_attr_get(struct item *it, enum attr_type attr_type, struct attr *attr)
{
	if (it->meth)
	{
		return it->meth->item_attr_get(it->priv_data, attr_type, attr);
	}
	else
	{
		dbg(0, "not method found\n");
		return 0;
	}
}

int item_attr_set(struct item *it, struct attr *attr, enum change_mode mode)
{
	if (!it->meth->item_attr_set)
		return 0;
	return it->meth->item_attr_set(it->priv_data, attr, mode);
}

struct item * item_new(char *type, int zoom)
{
	struct item * it;

	it = g_new0(struct item, 1);

	/* FIXME evaluate arguments */

	return it;
}

// not working yet!!!!! ------
/*
 void item_dup(struct item *src, struct item *dst)
 {
 int size;
 void *priv_data;

 dst = g_new0(struct item, 1);
 dst->type=src->type;
 dst->id_hi=src->id_hi;
 dst->id_lo=src->id_lo;

 dst->priv_data=g_malloc(size);
 memcpy(dst->priv_data, src->priv_data, size);
 // int len = (ib->len + 1) * 4;

 }
 */
// not working yet!!!!! ------

// return: 1 -> don't put it into search index
//         0 -> yes, put it into search index
int item_not_for_search_index(enum item_type i_type)
{

	if (i_type == type_house_number_interpolation_even)
	{
		return 1;
	}
	// exclude "unknown street" types from search index --> dont do this, otherwise we dont find all the good things :-)
	//else if (i_type == type_street_unkn)
	//{
	//	return 1;
	//}
	else if (i_type == type_house_number_interpolation_odd)
	{
		return 1;
	}
	else if (i_type == type_house_number_interpolation_all)
	{
		return 1;
	}
	else if (i_type == type_house_number_interpolation_alphabetic)
	{
		return 1;
	}
	else if (i_type == type_poly_airport)
	{
		return 1;
	}
	else if (i_type == type_poly_apron)
	{
		return 1;
	}
	else if (i_type == type_poly_terminal)
	{
		return 1;
	}
	else if (i_type == type_border_national_park)
	{
		return 1;
	}
	else if (i_type == type_border_political)
	{
		return 1;
	}
	else if (i_type == type_height_line_1)
	{
		return 1;
	}
	else if (i_type == type_height_line_2)
	{
		return 1;
	}
	else if (i_type == type_height_line_3)
	{
		return 1;
	}
	else if (i_type == type_poly_wood)
	{
		return 1;
	}
	else if (i_type == type_poly_greenfield)
	{
		return 1;
	}
	else if (i_type == type_poly_reservoir)
	{
		return 1;
	}
	else if (i_type == type_poly_playground)
	{
		return 1;
	}
	else if (i_type == type_poly_land)
	{
		return 1;
	}
	else if (i_type == type_poly_marsh)
	{
		return 1;
	}
	else if (i_type == type_poly_mud)
	{
		return 1;
	}
	else if (i_type == type_poly_water)
	{
		return 1;
	}
	else if (i_type == type_poly_wood)
	{
		return 1;
	}
	else if (i_type == type_poly_place1)
	{
		return 1;
	}
	else if (i_type == type_poly_place2)
	{
		return 1;
	}
	else if (i_type == type_poly_place3)
	{
		return 1;
	}
	else if (i_type == type_poly_place4)
	{
		return 1;
	}
	else if (i_type == type_poly_place5)
	{
		return 1;
	}
	else if (i_type == type_poly_place6)
	{
		return 1;
	}
	else if (i_type == type_ferry)
	{
		return 1;
	}
	else if (i_type == type_water_canal)
	{
		return 1;
	}
	else if (i_type == type_water_drain)
	{
		return 1;
	}
	else if (i_type == type_water_river)
	{
		return 1;
	}
	else if (i_type == type_poly_water)
	{
		return 1;
	}
	else if (i_type == type_water_stream)
	{
		return 1;
	}
	else
	{
		return 0;
	}

	return 0;
}

int item_is_town_label_major(enum item_type i_type)
{
	if (i_type == type_town_label_1e6)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e6)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e6)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e7)
	{
		return 1;
	}
	else
	{
		return 0;
	}

	return 0;
}

int item_is_town_label_no_major(enum item_type i_type)
{
	if (i_type == type_town_label_0e0)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e0)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e0)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e0)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e1)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e1)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e1)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e2)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e2)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e2)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e3)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e3)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e3)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e4)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e4)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e4)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e5)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e5)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e5)
	{
		return 1;
	}
	else if (i_type == type_town_label)
	{
		return 1;
	}
/* major town */
/*
	else if (i_type == type_town_label_1e6)
	{
		return 1;
	}
	else if (i_type == type_town_label_2e6)
	{
		return 1;
	}
	else if (i_type == type_town_label_5e6)
	{
		return 1;
	}
	else if (i_type == type_town_label_1e7)
	{
		return 1;
	}
*/
	else
	{
		return 0;
	}

	return 0;
}

int item_is_district_label(enum item_type i_type)
{
	if (i_type == type_district_label_0e0)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e0)
	{
		return 1;
	}
	else if (i_type == type_district_label_2e0)
	{
		return 1;
	}
	else if (i_type == type_district_label_5e0)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e1)
	{
		return 1;
	}
	else if (i_type == type_district_label_2e1)
	{
		return 1;
	}
	else if (i_type == type_district_label_5e1)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e2)
	{
		return 1;
	}
	else if (i_type == type_district_label_2e2)
	{
		return 1;
	}
	else if (i_type == type_district_label_5e2)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e3)
	{
		return 1;
	}
	else if (i_type == type_district_label_2e3)
	{
		return 1;
	}
	else if (i_type == type_district_label_5e3)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e4)
	{
		return 1;
	}
	else if (i_type == type_district_label_2e4)
	{
		return 1;
	}
	else if (i_type == type_district_label_5e4)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e5)
	{
		return 1;
	}
	else if (i_type == type_district_label_2e5)
	{
		return 1;
	}
	else if (i_type == type_district_label_5e5)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e6)
	{
		return 1;
	}
	else if (i_type == type_district_label_2e6)
	{
		return 1;
	}
	else if (i_type == type_district_label_5e6)
	{
		return 1;
	}
	else if (i_type == type_district_label_1e7)
	{
		return 1;
	}
	else if (i_type == type_district_label)
	{
		return 1;
	}
	else
	{
		return 0;
	}

	return 0;
}

int item_is_poi(enum item_type i_type)
// int item_is_poi(int item_type i_type)
{
	if (i_type == type_poi_lake)
	{
		return 1;
	}
	else if (i_type == type_poi_island)
	{
		return 1;
	}
	else if (i_type == type_poi_land_feature)
	{
		return 1;
	}
	else if (i_type == type_poi_cape)
	{
		return 1;
	}
	else if (i_type == type_poi_rock)
	{
		return 1;
	}
	else if (i_type == type_poi_airport)
	{
		return 1;
	}
	else if (i_type == type_poi_toll_booth)
	{
		return 1;
	}
	else if (i_type == type_poi_fuel)
	{
		return 1;
	}
	else if (i_type == type_poi_hotel)
	{
		return 1;
	}
	else if (i_type == type_poi_camp_rv)
	{
		return 1;
	}
	else if (i_type == type_poi_marina)
	{
		return 1;
	}
	else if (i_type == type_poi_attraction)
	{
		return 1;
	}
	else if (i_type == type_poi_museum_history)
	{
		return 1;
	}
	else if (i_type == type_poi_shopping)
	{
		return 1;
	}
	else if (i_type == type_poi_car_dealer_parts)
	{
		return 1;
	}
	else if (i_type == type_poi_car_parking)
	{
		return 1;
	}
	else if (i_type == type_poi_wreck)
	{
		return 1;
	}
	else if (i_type == type_poi_building)
	{
		return 1;
	}
	else if (i_type == type_poi_bridge)
	{
		return 1;
	}
	else if (i_type == type_poi_park)
	{
		return 1;
	}
	else if (i_type == type_poi_water_feature)
	{
		return 1;
	}
	else if (i_type == type_poi_bar)
	{
		return 1;
	}
	else if (i_type == type_poi_picnic)
	{
		return 1;
	}
	else if (i_type == type_poi_hospital)
	{
		return 1;
	}
	else if (i_type == type_poi_camping)
	{
		return 1;
	}
	else if (i_type == type_poi_public_utilities)
	{
		return 1;
	}
	else if (i_type == type_poi_burgerking)
	{
		return 1;
	}
	else if (i_type == type_poi_kfc)
	{
		return 1;
	}
	else if (i_type == type_poi_mcdonalds)
	{
		return 1;
	}
	else if (i_type == type_poi_wienerwald)
	{
		return 1;
	}
	else if (i_type == type_poi_dining)
	{
		return 1;
	}
	else if (i_type == type_poi_fastfood)
	{
		return 1;
	}
	else if (i_type == type_poi_police)
	{
		return 1;
	}
	else if (i_type == type_poi_auto_club)
	{
		return 1;
	}
	else if (i_type == type_poi_autoservice)
	{
		return 1;
	}
	else if (i_type == type_poi_bank)
	{
		return 1;
	}
	else if (i_type == type_poi_bay)
	{
		return 1;
	}
	else if (i_type == type_poi_bend)
	{
		return 1;
	}
	else if (i_type == type_poi_boat_ramp)
	{
		return 1;
	}
	else if (i_type == type_poi_border_station)
	{
		return 1;
	}
	else if (i_type == type_poi_bowling)
	{
		return 1;
	}
	else if (i_type == type_poi_bus_station)
	{
		return 1;
	}
	else if (i_type == type_poi_bus_stop)
	{
		return 1;
	}
	else if (i_type == type_poi_business_service)
	{
		return 1;
	}
	else if (i_type == type_poi_car_rent)
	{
		return 1;
	}
	else if (i_type == type_poi_car_wash)
	{
		return 1;
	}
	else if (i_type == type_poi_casino)
	{
		return 1;
	}
	else if (i_type == type_poi_cemetery)
	{
		return 1;
	}
	else if (i_type == type_poi_church)
	{
		return 1;
	}
	else if (i_type == type_poi_cinema)
	{
		return 1;
	}
	else if (i_type == type_poi_civil_removeme)
	{
		return 1;
	}
	else if (i_type == type_poi_communication)
	{
		return 1;
	}
	else if (i_type == type_poi_concert)
	{
		return 1;
	}
	else if (i_type == type_poi_cove)
	{
		return 1;
	}
	else if (i_type == type_poi_crossing)
	{
		return 1;
	}
	else if (i_type == type_poi_dam)
	{
		return 1;
	}
	else if (i_type == type_poi_danger_area)
	{
		return 1;
	}
	else if (i_type == type_poi_danger_sea_wreck)
	{
		return 1;
	}
	else if (i_type == type_poi_daymark)
	{
		return 1;
	}
	else if (i_type == type_poi_diving)
	{
		return 1;
	}
	else if (i_type == type_poi_drinking_water)
	{
		return 1;
	}
	else if (i_type == type_poi_emergency)
	{
		return 1;
	}
	else if (i_type == type_poi_fair)
	{
		return 1;
	}
	else if (i_type == type_poi_firebrigade)
	{
		return 1;
	}
	else if (i_type == type_poi_fish)
	{
		return 1;
	}
	else if (i_type == type_poi_forbidden_area)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_gps)
	{
		return 1;
	}
	else if (i_type == type_poi_golf)
	{
		return 1;
	}
	else if (i_type == type_poi_government_building)
	{
		return 1;
	}
	else if (i_type == type_poi_height)
	{
		return 1;
	}
	else if (i_type == type_poi_heliport)
	{
		return 1;
	}
	else if (i_type == type_poi_hotspring)
	{
		return 1;
	}
	else if (i_type == type_poi_icesport)
	{
		return 1;
	}
	else if (i_type == type_poi_information)
	{
		return 1;
	}
	else if (i_type == type_poi_justice)
	{
		return 1;
	}
	else if (i_type == type_poi_landmark)
	{
		return 1;
	}
	else if (i_type == type_poi_levee)
	{
		return 1;
	}
	else if (i_type == type_poi_library)
	{
		return 1;
	}
	else if (i_type == type_poi_locale)
	{
		return 1;
	}
	else if (i_type == type_poi_loudspeaker)
	{
		return 1;
	}
	else if (i_type == type_poi_mall)
	{
		return 1;
	}
	else if (i_type == type_poi_manmade_feature)
	{
		return 1;
	}
	else if (i_type == type_poi_marine)
	{
		return 1;
	}
	else if (i_type == type_poi_marine_type)
	{
		return 1;
	}
	else if (i_type == type_poi_mark)
	{
		return 1;
	}
	else if (i_type == type_poi_military)
	{
		return 1;
	}
	else if (i_type == type_poi_mine)
	{
		return 1;
	}
	else if (i_type == type_poi_nondangerous)
	{
		return 1;
	}
	else if (i_type == type_poi_oil_field)
	{
		return 1;
	}
	else if (i_type == type_poi_personal_service)
	{
		return 1;
	}
	else if (i_type == type_poi_pharmacy)
	{
		return 1;
	}
	else if (i_type == type_poi_post_removeme)
	{
		return 1;
	}
	else if (i_type == type_poi_public_office)
	{
		return 1;
	}
	else if (i_type == type_poi_repair_service)
	{
		return 1;
	}
	else if (i_type == type_poi_resort)
	{
		return 1;
	}
	else if (i_type == type_poi_rest_room_removeme)
	{
		return 1;
	}
	else if (i_type == type_poi_restaurant)
	{
		return 1;
	}
	else if (i_type == type_poi_restricted_area)
	{
		return 1;
	}
	else if (i_type == type_poi_restroom)
	{
		return 1;
	}
	else if (i_type == type_poi_sailing)
	{
		return 1;
	}
	else if (i_type == type_poi_scenic_area)
	{
		return 1;
	}
	else if (i_type == type_poi_school)
	{
		return 1;
	}
	else if (i_type == type_poi_service)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_apparel)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_computer)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_department)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_furnish_removeme)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_grocery)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_handg)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_merchandise)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_retail)
	{
		return 1;
	}
	else if (i_type == type_poi_shower)
	{
		return 1;
	}
	else if (i_type == type_poi_skiing)
	{
		return 1;
	}
	else if (i_type == type_poi_social_service)
	{
		return 1;
	}
	else if (i_type == type_poi_sounding)
	{
		return 1;
	}
	else if (i_type == type_poi_sport)
	{
		return 1;
	}
	else if (i_type == type_poi_stadium)
	{
		return 1;
	}
	else if (i_type == type_poi_subdivision_removeme)
	{
		return 1;
	}
	else if (i_type == type_poi_swimming)
	{
		return 1;
	}
	else if (i_type == type_poi_telephone)
	{
		return 1;
	}
	else if (i_type == type_poi_theater)
	{
		return 1;
	}
	else if (i_type == type_poi_tide)
	{
		return 1;
	}
	else if (i_type == type_poi_tower)
	{
		return 1;
	}
	else if (i_type == type_poi_trail)
	{
		return 1;
	}
	else if (i_type == type_poi_truck_stop)
	{
		return 1;
	}
	else if (i_type == type_poi_tunnel)
	{
		return 1;
	}
	else if (i_type == type_poi_wine)
	{
		return 1;
	}
	else if (i_type == type_poi_worship)
	{
		return 1;
	}
	else if (i_type == type_poi_wrecker)
	{
		return 1;
	}
	else if (i_type == type_poi_zoo)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_multi)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_tradi)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_event)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_mystery)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_question)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_stages)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_reference)
	{
		return 1;
	}
	else if (i_type == type_poi_gc_webcam)
	{
		return 1;
	}
	else if (i_type == type_poi_cafe)
	{
		return 1;
	}
	else if (i_type == type_poi_peak)
	{
		return 1;
	}
	else if (i_type == type_poi_rail_station)
	{
		return 1;
	}
	else if (i_type == type_poi_image)
	{
		return 1;
	}
	else if (i_type == type_poi_townhall)
	{
		return 1;
	}
	else if (i_type == type_poi_level_crossing)
	{
		return 1;
	}
	else if (i_type == type_poi_rail_halt)
	{
		return 1;
	}
	else if (i_type == type_poi_rail_tram_stop)
	{
		return 1;
	}
	else if (i_type == type_poi_wifi)
	{
		return 1;
	}
	else if (i_type == type_poi_bench)
	{
		return 1;
	}
	else if (i_type == type_poi_biergarten)
	{
		return 1;
	}
	else if (i_type == type_poi_boundary_stone)
	{
		return 1;
	}
	else if (i_type == type_poi_castle)
	{
		return 1;
	}
	else if (i_type == type_poi_hunting_stand)
	{
		return 1;
	}
	else if (i_type == type_poi_memorial)
	{
		return 1;
	}
	else if (i_type == type_poi_monument)
	{
		return 1;
	}
	else if (i_type == type_poi_shelter)
	{
		return 1;
	}
	else if (i_type == type_poi_fountain)
	{
		return 1;
	}
	else if (i_type == type_poi_potable_water)
	{
		return 1;
	}
	else if (i_type == type_poi_toilets)
	{
		return 1;
	}
	else if (i_type == type_poi_viewpoint)
	{
		return 1;
	}
	else if (i_type == type_poi_ruins)
	{
		return 1;
	}
	else if (i_type == type_poi_post_box)
	{
		return 1;
	}
	else if (i_type == type_poi_post_office)
	{
		return 1;
	}
	else if (i_type == type_poi_school_university)
	{
		return 1;
	}
	else if (i_type == type_poi_school_college)
	{
		return 1;
	}
	else if (i_type == type_poi_motel)
	{
		return 1;
	}
	else if (i_type == type_poi_guesthouse)
	{
		return 1;
	}
	else if (i_type == type_poi_hostel)
	{
		return 1;
	}
	else if (i_type == type_poi_taxi)
	{
		return 1;
	}
	else if (i_type == type_poi_prison)
	{
		return 1;
	}
	else if (i_type == type_poi_kindergarten)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_butcher)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_baker)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_kiosk)
	{
		return 1;
	}
	else if (i_type == type_poi_soccer)
	{
		return 1;
	}
	else if (i_type == type_poi_basketball)
	{
		return 1;
	}
	else if (i_type == type_poi_baseball)
	{
		return 1;
	}
	else if (i_type == type_poi_climbing)
	{
		return 1;
	}
	else if (i_type == type_poi_motor_sport)
	{
		return 1;
	}
	else if (i_type == type_poi_tennis)
	{
		return 1;
	}
	else if (i_type == type_poi_playground)
	{
		return 1;
	}
	else if (i_type == type_poi_vending_machine)
	{
		return 1;
	}
	else if (i_type == type_poi_recycling)
	{
		return 1;
	}
	else if (i_type == type_poi_hairdresser)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_fruit)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_bicycle)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_florist)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_optician)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_beverages)
	{
		return 1;
	}
	else if (i_type == type_poi_nightclub)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_shoes)
	{
		return 1;
	}
	else if (i_type == type_poi_tree)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_furniture)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_parfum)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_drugstore)
	{
		return 1;
	}
	else if (i_type == type_poi_shop_photo)
	{
		return 1;
	}
	else if (i_type == type_poi_atm)
	{
		return 1;
	}
	else if (i_type == type_poi_custom1)
	{
		return 1;
	}
	else if (i_type == type_poi_custom2)
	{
		return 1;
	}
	else if (i_type == type_poi_custom3)
	{
		return 1;
	}
	else if (i_type == type_poi_custom4)
	{
		return 1;
	}
	else if (i_type == type_poi_custom5)
	{
		return 1;
	}
	else if (i_type == type_poi_custom6)
	{
		return 1;
	}
	else if (i_type == type_poi_custom7)
	{
		return 1;
	}
	else if (i_type == type_poi_custom8)
	{
		return 1;
	}
	else if (i_type == type_poi_custom9)
	{
		return 1;
	}
	else if (i_type == type_poi_customa)
	{
		return 1;
	}
	else if (i_type == type_poi_customb)
	{
		return 1;
	}
	else if (i_type == type_poi_customc)
	{
		return 1;
	}
	else if (i_type == type_poi_customd)
	{
		return 1;
	}
	else if (i_type == type_poi_custome)
	{
		return 1;
	}
	else if (i_type == type_poi_customf)
	{
		return 1;
	}
	else
	{
		return 0;
	}

	return 0;
}


enum item_type item_from_name(const char *name)
{
	int i;

	for (i = 0; i < sizeof(item_names) / sizeof(struct item_name); i++)
	{
		if (!strcmp(item_names[i].name, name))
			return item_names[i].item;
	}
	return type_none;
}

char *
item_to_name(enum item_type item)
{
	int i;

	for (i = 0; i < sizeof(item_names) / sizeof(struct item_name); i++)
	{
		if (item_names[i].item == item)
			return item_names[i].name;
	}
	return NULL;
}

struct item_hash
{
	GHashTable *h;
};

static guint item_hash_hash(gconstpointer key)
{
	const struct item *itm = key;
	gconstpointer hashkey = (gconstpointer) GINT_TO_POINTER(itm->id_hi ^ itm->id_lo ^ (GPOINTER_TO_INT(itm->map)));
	return g_direct_hash(hashkey);
}

static gboolean item_hash_equal(gconstpointer a, gconstpointer b)
{
	const struct item *itm_a = a;
	const struct item *itm_b = b;
	if (item_is_equal(*itm_a, *itm_b))
		return TRUE;
	return FALSE;
}

unsigned int item_id_hash(const void *key)
{
	const struct item_id *id = key;
	return id->id_hi ^ id->id_lo;
}

int item_id_equal(const void *a, const void *b)
{
	const struct item_id *id_a = a;
	const struct item_id *id_b = b;
	return (id_a->id_hi == id_b->id_hi && id_a->id_lo == id_b->id_lo);
}

struct item_hash *
item_hash_new(void)
{
	struct item_hash *ret=g_new(struct item_hash, 1);

	ret->h = g_hash_table_new_full(item_hash_hash, item_hash_equal, g_free_func, NULL);
	return ret;
}

void item_hash_insert(struct item_hash *h, struct item *item, void *val)
{
	struct item *hitem=g_new(struct item, 1);
	*hitem = *item;
	//dbg(2, "inserting (0x%x,0x%x) into %p\n", item->id_hi, item->id_lo, h->h);
	g_hash_table_insert(h->h, hitem, val);
}

int item_hash_remove(struct item_hash *h, struct item *item)
{
	int ret;

	//dbg(2, "removing (0x%x,0x%x) from %p\n", item->id_hi, item->id_lo, h->h);
	ret = g_hash_table_remove(h->h, item);
	//dbg(2, "ret=%d\n", ret);

	return ret;
}

void *
item_hash_lookup(struct item_hash *h, struct item *item)
{
	return g_hash_table_lookup(h->h, item);
}

void item_hash_destroy(struct item_hash *h)
{
	g_hash_table_destroy(h->h);
	g_free(h);
}

int item_range_intersects_range(struct item_range *range1, struct item_range *range2)
{
	if (range1->max < range2->min)
		return 0;
	if (range1->min > range2->max)
		return 0;
	return 1;
}
int item_range_contains_item(struct item_range *range, enum item_type type)
{
	if (type >= range->min && type <= range->max)
		return 1;
	return 0;
}

void item_dump_attr_stdout(struct item *item, struct map *map)
{
	struct attr attr;
	dbg(0, "type=%d:%s\n", item->type, item_to_name(item->type));
	while (item_attr_get(item, attr_any, &attr))
	{
		dbg(0, " %d:%s='%s'", attr.type, attr_to_name(attr.type), attr_to_text(&attr, map, 1));
		// dbg(0," %s\n", attr_to_name(attr.type));
	}
}

void item_dump_attr(struct item *item, struct map *map, FILE *out)
{
	struct attr attr;
	fprintf(out, "type=%s", item_to_name(item->type));
	while (item_attr_get(item, attr_any, &attr))
	{
		fprintf(out, " %s='%s'", attr_to_name(attr.type), attr_to_text(&attr, map, 1));
	}
}

void item_dump_filedesc(struct item *item, struct map *map, FILE *out)
{

	int i, count, max = 16384;
	struct coord *ca = g_alloca(sizeof(struct coord) * max);

	count = item_coord_get(item, ca, item->type < type_line ? 1 : max);
	if (item->type < type_line)
		fprintf(out, "mg:0x%x 0x%x ", ca[0].x, ca[0].y);
	item_dump_attr(item, map, out);
	fprintf(out, "\n");
	if (item->type >= type_line)
		for (i = 0; i < count; i++)
			fprintf(out, "mg:0x%x 0x%x\n", ca[i].x, ca[i].y);
}
