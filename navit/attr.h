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
 * modify it under the terms of the GNU Library General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

#ifndef NAVIT_ATTR_H
#define NAVIT_ATTR_H

#ifdef __cplusplus
extern "C" {
#endif

#include "projection.h"

enum item_type;

enum attr_type {
#define ATTR2(x,y) attr_##y=x,
#define ATTR(x) attr_##x,
#include "attr_def.h"
#undef ATTR2
#undef ATTR
};

#define NAVIT_AF_ONEWAY		(1<<0)
#define NAVIT_AF_ONEWAYREV		(1<<1)
#define NAVIT_AF_NOPASS		(NAVIT_AF_ONEWAY|NAVIT_AF_ONEWAYREV)
#define NAVIT_AF_ONEWAYMASK		(NAVIT_AF_ONEWAY|NAVIT_AF_ONEWAYREV)
#define NAVIT_AF_SEGMENTED		(1<<2)
#define NAVIT_AF_ROUNDABOUT 		(1<<3)
#define NAVIT_AF_ROUNDABOUT_VALID	(1<<4)
#define NAVIT_AF_ONEWAY_EXCEPTION	(1<<5)
#define NAVIT_AF_SPEED_LIMIT		(1<<6)
#define NAVIT_AF_RESERVED1		(1<<7)
#define NAVIT_AF_SIZE_OR_WEIGHT_LIMIT	(1<<8)
#define NAVIT_AF_THROUGH_TRAFFIC_LIMIT (1<<9)
#define NAVIT_AF_TOLL			(1<<10)
#define NAVIT_AF_SEASONAL		(1<<11)
#define NAVIT_AF_UNPAVED		(1<<12)
#define NAVIT_AF_FORD			(1<<13)
#define NAVIT_AF_UNDERGROUND		(1<<14)
#define NAVIT_AF_BRIDGE		(1<<15) // NEW !!
// 16 - 18 is still free!!
#define NAVIT_AF_DANGEROUS_GOODS	(1<<19)
#define NAVIT_AF_EMERGENCY_VEHICLES	(1<<20)
#define NAVIT_AF_TRANSPORT_TRUCK	(1<<21)
#define NAVIT_AF_DELIVERY_TRUCK	(1<<22)
#define NAVIT_AF_PUBLIC_BUS		(1<<23)
#define NAVIT_AF_TAXI			(1<<24)	
#define NAVIT_AF_HIGH_OCCUPANCY_CAR	(1<<25)	
#define NAVIT_AF_CAR			(1<<26)	
#define NAVIT_AF_MOTORCYCLE		(1<<27)	
#define NAVIT_AF_MOPED		(1<<28)	
#define NAVIT_AF_HORSE		(1<<29)	
#define NAVIT_AF_BIKE			(1<<30)	
#define NAVIT_AF_PEDESTRIAN		(1<<31)	

#define NAVIT_AF_PBH (NAVIT_AF_PEDESTRIAN|NAVIT_AF_BIKE|NAVIT_AF_HORSE)
#define NAVIT_AF_MOTORIZED_FAST (NAVIT_AF_MOTORCYCLE|NAVIT_AF_CAR|NAVIT_AF_HIGH_OCCUPANCY_CAR|NAVIT_AF_TAXI|NAVIT_AF_PUBLIC_BUS|NAVIT_AF_DELIVERY_TRUCK|NAVIT_AF_TRANSPORT_TRUCK|NAVIT_AF_EMERGENCY_VEHICLES)
#define NAVIT_AF_ALL (NAVIT_AF_PBH|NAVIT_AF_MOPED|NAVIT_AF_MOTORIZED_FAST)


#define NAVIT_AF_DG_ANY		(1<<0)
#define NAVIT_AF_DG_WATER_HARMFUL	(1<<1)
#define NAVIT_AF_DG_EXPLOSIVE		(1<<2)
#define NAVIT_AF_DG_FLAMMABLE		(1<<3)

/* Values for attributes that could carry relative values */
#define ATTR_REL_MAXABS			0x40000000
#define ATTR_REL_RELSHIFT		0x60000000

enum attr_position_valid {
	attr_position_valid_invalid,
	attr_position_valid_static,
	attr_position_valid_extrapolated_time,
	attr_position_valid_extrapolated_spatial,
	attr_position_valid_valid,
};

#define ATTR_IS_INT(x) ((x) >= attr_type_int_begin && (x) <= attr_type_int_end)
#define ATTR_IS_DOUBLE(x) ((x) >= attr_type_double_begin && (x) <= attr_type_double_end)
#define ATTR_IS_STRING(x) ((x) >= attr_type_string_begin && (x) <= attr_type_string_end)
#define ATTR_IS_OBJECT(x) ((x) >= attr_type_object_begin && (x) <= attr_type_object_end)
#define ATTR_IS_ITEM(x) ((x) >= attr_type_item_begin && (x) <= attr_type_item_end)
#define ATTR_IS_COORD_GEO(x) ((x) >= attr_type_coord_geo_begin && (x) <= attr_type_coord_geo_end)
#define ATTR_IS_NUMERIC(x) (ATTR_IS_INT(x) || ATTR_IS_DOUBLE(x))
#define ATTR_IS_COLOR(x) ((x) >= attr_type_color_begin && (x) <= attr_type_color_end)
#define ATTR_IS_PCOORD(x) ((x) >= attr_type_pcoord_begin && (x) <= attr_type_pcoord_end)
#define ATTR_IS_COORD(x) ((x) >= attr_type_coord_begin && (x) <= attr_type_coord_end)
#define ATTR_IS_GROUP(x) ((x) >= attr_type_group_begin && (x) <= attr_type_group_end)

#define ATTR_DEF_STR(x,y) (&(struct attr){attr_##x,{y}})
#define ATTR_DEF_INT(x,y) (&(struct attr){attr_##x,{(char *)(y)}})
#define ATTR_DEF_ITEMS(x,...) (&(struct attr){attr_##x,{(char *)((enum item_type[]){__VA_ARGS__ , type_none})}})
#define ATTR_LIST(...) (struct attr *[]) { __VA_ARGS__, NULL}

struct attr {
	enum attr_type type;
	union {
		char *str;
		void *data;
		long num;
		struct item *item;
		enum item_type item_type;
		enum projection projection;
		double * numd;
		struct color *color;
		struct coord_geo *coord_geo;
		struct navit *navit;
		struct callback *callback;
		struct callback_list *callback_list;
		struct vehicle *vehicle;
		struct layout *layout;
		struct layer *layer;
		struct map *map;
		struct mapset *mapset;
		struct log *log;
		struct route *route;
		struct navigation *navigation;
		struct coord *coord;
		struct pcoord *pcoord;
		struct gui *gui;
		struct graphics *graphics;
		struct tracking *tracking;
		struct itemgra *itemgra;
		struct plugin *plugin;
		struct plugins *plugins;
		struct polygon *polygon;
		struct polyline *polyline;
		struct circle *circle;
		struct text *text;
		struct icon *icon;
		struct image *image;
		struct arrows *arrows;
		struct element *element;
		struct speech *speech;
		struct cursor *cursor;
		struct displaylist *displaylist;
		struct transformation *transformation;
		struct vehicleprofile *vehicleprofile;
		struct roadprofile *roadprofile;
		struct bookmarks *bookmarks;
		struct range {
			short min, max;
		} range;
		int *dash;
		enum item_type *item_types;
		enum attr_type *attr_types;
		long long *num64;
		struct attr *attrs;
	} u;
};

struct attr_iter;
/* prototypes */
enum attr_type attr_from_name(const char *name);
char *attr_to_name(enum attr_type attr);
struct attr *attr_new_from_text(const char *name, const char *value);
char *attr_to_text(struct attr *attr, struct map *map, int pretty);
struct attr *attr_search(struct attr **attrs, struct attr *last, enum attr_type attr);
int attr_generic_get_attr(struct attr **attrs, struct attr **def_attrs, enum attr_type type, struct attr *attr, struct attr_iter *iter);
struct attr **attr_generic_set_attr(struct attr **attrs, struct attr *attr);
struct attr **attr_generic_add_attr(struct attr **attrs, struct attr *attr);
struct attr **attr_generic_remove_attr(struct attr **attrs, struct attr *attr);
enum attr_type attr_type_begin(enum attr_type type);
int attr_data_size(struct attr *attr);
void *attr_data_get(struct attr *attr);
void attr_data_set(struct attr *attr, void *data);
void attr_data_set_le(struct attr *attr, void *data);
void attr_free(struct attr *attr);
void attr_dup_content(struct attr *src, struct attr *dst);
struct attr *attr_dup(struct attr *attr);
void attr_list_free(struct attr **attrs);
struct attr **attr_list_dup(struct attr **attrs);
int attr_from_line(char *line, char *name, int *pos, char *val_ret, char *name_ret);
int attr_types_contains(enum attr_type *types, enum attr_type type);
int attr_types_contains_default(enum attr_type *types, enum attr_type type, int deflt);

char *flags_to_text(int flags);

/* end of prototypes */
#ifdef __cplusplus
}
#endif

#endif
