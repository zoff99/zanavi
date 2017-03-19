/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2016 Zoff <zoff@zoff.cc>
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

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>
#include <signal.h>
#include <time.h>
#include <glib.h>


#ifdef __cplusplus
extern "C"
{
#endif


void debug_for_tests_printf(int level, const char *fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);
	fprintf(stderr, fmt, ap);
	va_end(ap);
}

#define dbg(ll, ...)
// #define tests_dbg(ll, ...)
#define tests_dbg(level,...) debug_for_tests_printf(level,__VA_ARGS__);




enum item_type {
#define ITEM2(x,y) type_##y=x,
#define ITEM(x) type_##x,
#include "item_def.h"
#undef ITEM2
#undef ITEM
};


struct item_methods {
	int dummy;
};

struct map {
	int dummy;
};


struct item {
	enum item_type type;
	int id_hi;
	int id_lo;
	struct map *map;
	struct item_methods *meth;
	void *priv_data;
	long flags;
};


struct item_name
{
	enum item_type item;
	char *name;
};


struct item_name item_names[] = {
#define ITEM2(x,y) ITEM(y)
#define ITEM(x) { type_##x, #x },
#include "item_def.h"
#undef ITEM2
#undef ITEM
		};



char *
item_to_name(enum item_type item)
{
	int i;

	for (i = 0; i < sizeof(item_names) / sizeof(struct item_name); i++)
	{
		if (item_names[i].item == item)
		{
			return item_names[i].name;
		}
	}

	return NULL;
}


struct street_destination
{
	struct street_destination *next;
	char *destination;
};

struct navigation_way
{
	struct navigation_way *next; /**< Pointer to a linked-list of all navigation_ways from this navigation item */
	short dir; /**< The direction -1 or 1 of the way */
	short angle2; /**< The angle one has to steer to drive from the old item to this street */
	int flags; /**< The flags of the way */
	struct item item; /**< The item of the way */
	char *name1; // = streetname
	char *name2; // = streetname systematic (road number e.g.: E51)
	char *exit_ref;					/**< Exit_ref if found on the first node of the way*/
	char *exit_label;				/**< Exit_label if found on the first node of the way*/
	struct street_destination *s_destination;				/**< The destination this way leads to (OSM: {@code destination}) */
	char *street_dest_text; /* selected destination to display in GUI */
};

#define NAVIT_AF_ROUNDABOUT 		(1<<3)
#define NAVIT_AF_ROUNDABOUT_VALID	(1<<4)
#define NAVIT_AF_ONEWAY_BICYCLE_NO	(1<<16)

#define ROAD_ANGLE_IS_STRAIGHT_ABS 10
#define ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC 65
#define ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC__2 25
#define ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE 65
#define ROAD_ANGLE_MIN__FOR_TURN_BICYCLEMODE_ONLY_1_POSSIBILITY 25
#define ROAD_ANGLE_DISTANCE_FOR_STRAIGHT 78


struct navigation
{
	struct navigation_itm *first;
	struct navigation_itm *last;
	int level_last;
	struct item item_last;
	int turn_around;
	int turn_around_limit;
	int distance_turn;
	int announce[999999 - 0 + 1][3];
	int tell_street_name;
	int delay;
	int curr_delay;
	struct navigation_itm *previous;
};


int global_vehicle_profile = 1;


enum projection {
	projection_none, projection_mg, projection_garmin, projection_screen, projection_utm, projection_gk
};

enum map_datum {
	map_datum_none, map_datum_wgs84, map_datum_dhdn
};


/*! A integer mercator coordinate */
struct coord {
	int x; /*!< X-Value */
	int y; /*!< Y-Value */
};

/*! A integer mercator coordinate carrying its projection */
struct pcoord {
	enum projection pro;
	int x; /*!< X-Value */
	int y; /*!< Y-Value */
};

struct coord_rect {
	struct coord lu;
	struct coord rl;
};

struct navigation_itm
{
	struct navigation_way way;
	int angle_end;
	struct coord start, end;
	int time;
	int length;
	int speed;
	int dest_time;
	int dest_length;
	int told; /**< Indicates if this item's announcement has been told earlier and should not be told again*/
	int streetname_told; /**< Indicates if this item's streetname has been told in speech navigation*/
	int dest_count;
	struct navigation_itm *next;
	struct navigation_itm *prev;
};


#define item_is_street(item) (((item).type >= type_street_0 && (item).type <= type_highway_land) \
                               ||  (item).type == type_street_service \
                               ||  (item).type == type_street_parking_lane \
                               ||  (item).type == type_ramp_highway_land \
                               ||  (item).type == type_ramp_street_4_city \
                               ||  (item).type == type_ramp_street_3_city \
                               ||  (item).type == type_ramp_street_2_city \
                               ||  (item).type == type_street_pedestrian \
                               ||  (item).type == type_living_street)

#define item_is_ramp(item) (       (item).type == type_ramp_highway_land \
                               ||  (item).type == type_ramp_street_4_city \
                               ||  (item).type == type_ramp_street_3_city \
                               ||  (item).type == type_ramp_street_2_city)

static int is_way_allowed(struct navigation *nav, struct navigation_way *way, int mode)
{
	return 1;
}


static int angle_delta(int angle1, int angle2)
{
	int delta = angle2 - angle1;

	if (delta <= -180)
		delta += 360;

	if (delta > 180)
		delta -= 360;

	return delta;
}

static int is_maybe_same_item(struct navigation_way *w1, struct navigation_way *w2, int reverse_match)
{

	dbg(0, "======================================\n");

	dbg(0, "w1=%p w2=%p\n", w1, w2);

	dbg(0, "w1 type=%s w2 type=%s\n", item_to_name(w1->item.type), item_to_name(w2->item.type));
	dbg(0, "w1 dir=%d w2 dir=%d\n", w1->dir, w2->dir);
	dbg(0, "w1 angle2=%d w2 angle2=%d\n", w1->angle2, w2->angle2);
	dbg(0, "w1 n1=%s w2 n1=%s\n", w1->name1, w2->name1);
	dbg(0, "w1 n2=%s w2 n2=%s\n", w1->name2, w2->name2);

#ifdef NAVIT_NAVIGATION_REMOVE_DUPL_WAYS

	int dir_reverse = 0;

	if (w1->item.type != w2->item.type)
	{
		dbg(0, "x:w1 type=%s w2 type=%s\n", item_to_name(w1->item.type), item_to_name(w2->item.type));
		return 0;
	}

#if 0
	if (w1->dir != w2->dir)
	{
		dir_reverse = 1;
	}
#endif

	if (reverse_match != 0)
	{
		dir_reverse = 1 - dir_reverse; // toggle "dir_reverse"
	}

	if (dir_reverse == 0)
	{
		if ((abs(w1->angle2 - w2->angle2) != 0) && (abs(w1->angle2 - w2->angle2) != 360))
		{
			dbg(0, "x:000:w1 angle2=%d w2 angle2=%d\n", w1->angle2, w2->angle2);
			return 0;
		}
	}
	else
	{
		if (abs(w1->angle2 - w2->angle2) != 180)
		{
			dbg(0, "x:180:w1 angle2=%d w2 angle2=%d\n", w1->angle2, w2->angle2);
			return 0;
		}
	}
	

	if ((w1->name1 == NULL) && (w2->name1 == NULL))
	{
	}
	else
	{
		if ((w1->name1 == NULL) || (w2->name1 == NULL))
		{
			return 0;
		}
		else if (strcmp(w1->name1, w2->name1) != 0)
		{
			dbg(0, "x:w1 n1=%s w2 n1=%s\n", w1->name1, w2->name1);
			return 0;
		}
	}

	if ((w1->name2 == NULL) && (w2->name2 == NULL))
	{
	}
	else
	{
		if ((w1->name2 == NULL) || (w2->name2 == NULL))
		{
			return 0;
		}
		else if (strcmp(w1->name2, w2->name2) != 0)
		{
			dbg(0, "x:w1 n2=%s w2 n2=%s\n", w1->name2, w2->name2);
			return 0;
		}
	}

	return 1;

#else

	return 0;

#endif

}


static int XXis_maybe_same_item(struct navigation_way *w1, struct navigation_way *w2, int reverse_match)
{
	return 0;
}


int navigation_is_low_level_street(enum item_type t)
{
	if (global_vehicle_profile == 0) // only in car profile!
	{
		if (t == type_street_service)
		{
			return 1;
		}
		else if (t == type_street_parking_lane)
		{
			return 1;
		}
		else if (t == type_track_ground)
		{
			return 1;
		}
		else if (t == type_track_grass)
		{
			return 1;
		}
		else if (t == type_track_gravelled)
		{
			return 1;
		}
		else if (t == type_track_unpaved)
		{
			return 1;
		}
		else if (t == type_track_paved)
		{
			return 1;
		}
	}

	return 0;
}



static int is_same_street2(char *old_name1, char *old_name2, char *new_name1, char *new_name2)
{
	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
		// always return false when in bicycle mode
		return 0;
	}

	if (old_name1 && new_name1 && !strcmp(old_name1, new_name1))
	{
		// dbg(1, "is_same_street: '%s' '%s' vs '%s' '%s' yes (1.)\n", old_name2, new_name2, old_name1, new_name1);
		return 1;
	}

	if (old_name2 && new_name2 && !strcmp(old_name2, new_name2))
	{
		// dbg(1, "is_same_street: '%s' '%s' vs '%s' '%s' yes (2.)\n", old_name2, new_name2, old_name1, new_name1);
		return 1;
	}

	// dbg(1, "is_same_street: '%s' '%s' vs '%s' '%s' no\n", old_name2, new_name2, old_name1, new_name1);
	return 0;
}




