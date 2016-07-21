/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2015 Zoff <zoff@zoff.cc>
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
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>
#include <glib.h>
#include "debug.h"
#include "profile.h"
#include "navigation.h"
#include "coord.h"

#include "route.h"
#include "transform.h"
#include "mapset.h"
#include "projection.h"
#include "map.h"
#include "navit.h"
#include "callback.h"
#include "speech.h"
#include "vehicleprofile.h"
#include "plugin.h"
#include "navit_nls.h"

/* #define DEBUG */






// #define NAVIT_FUNC_CALLS_DEBUG_PRINT 1


// --------------- debug function calls ------------------
// --------------- debug function calls ------------------
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	#undef return2
	#define return2	dbg_func(0, global_func_indent_counter, "return(%d)\n", __LINE__);global_func_indent_counter--;return

	#define __F_START__ global_func_indent_counter++;dbg_func(0, global_func_indent_counter, "enter\n");
	#define __F_END__   dbg_func(0, global_func_indent_counter, "leave\n");global_func_indent_counter--;
#else
	#undef return2
	#define return2	return

	#define __F_START__
	#define __F_END__
#endif
// --------------- debug function calls ------------------
// --------------- debug function calls ------------------







static int roundabout_extra_length = 50;

struct suffix
{
	char *fullname;
	char *abbrev;
	int sex;
} suffixes[] = { { "weg", NULL, 1 }, { "platz", "pl.", 1 }, { "ring", NULL, 1 }, { "allee", NULL, 2 }, { "gasse", NULL, 2 }, { "straße", "str.", 2 }, { "strasse", NULL, 2 }, };


int distances[] = { 1, 2, 3, 4, 5, 10, 25, 50, 75, 100, 150, 200, 250, 300, 400, 500, 750, -1 };






// -- NEW 002 --
/* Allowed values for navigation_maneuver.merge_or_exit
 * The numeric values are chosen in such a way that they can be interpreted as flags:
 * 1=merge, 2=exit, 4=interchange, 8=right, 16=left
 * Identifiers were chosen over flags to enforce certain rules
 * (merge/exit/interchange and left/right are mutually exclusive, left/right requires merge or exit). */
//FIXME: should we make this an enum?

/** Not merging into or exiting from a motorway_like road */
#define mex_none 0

/** Merging into a motorway-like road, direction undefined */
//FIXME: do we need this constant?
#define mex_merge 1

/** Exiting from a motorway-like road, direction undefined.
 * This should only be used for ramps leading to a non-motorway road.
 * For interchanges, use {@code mex_interchange} instead. */
//FIXME: do we need this constant?
#define mex_exit 2

/** Motorway-like road splits in two.
 * This should be used for all cases in which ramps lead to another motorway-like road. */
#define mex_interchange 4

/** Merging into a motorway-like road to the right (coming from the left) */
#define mex_merge_right 9

/** Exiting from a motorway-like road to the right.
 * See {@code mex_exit} for usage. */
#define mex_exit_right 10

/** Merging into a motorway-like road to the left (coming from the right) */
#define mex_merge_left 17

/** Exiting from a motorway-like road to the left.
 * See {@code mex_exit} for usage. */
#define mex_exit_left 18

/**
 * @brief Holds information about a navigation maneuver.
 *
 * This structure is populated when a navigation maneuver is first analyzed. Its members contain all information
 * needed to decide whether or not to announce the maneuver, what type of maneuver it is and the information that
 * was used to determine the former two.
 */
struct navigation_maneuver {
	enum item_type type;       /**< The type of maneuver to perform. Any {@code nav_*} item is permitted here, with one exception:
	                                merge or exit maneuvers are indicated by the {@code merge_or_exit} member. The {@code item_type}
	                                for such maneuvers should be a turn instruction in cases where the maneuver is ambiguous, or
	                                {@code nav_none} for cases in which we would expect the driver to perform this maneuver even
	                                without being instructed to do so. **/
	int delta;                 /**< Bearing difference (the angle the driver has to steer) for the maneuver */
	int merge_or_exit;         /**< Whether we are merging into or exiting from a motorway_like road or we are at an interchange */
	int is_complex_t_junction; /**< Whether we are coming from the "stem" of a T junction whose "bar" is a dual-carriageway road and
	                                crossing the opposite lane of the "bar" first (i.e. turning left in countries that drive on the
	                                right, or turning right in countries that drive on the left). For these maneuvers
	                                {@code num_options} is 1 (which means we normally wouldn't announce the maneuver) but drivers
	                                would expect an announcement in such cases. */
	int num_options;           /**< Number of permitted candidate ways, i.e. ways which we may enter (based on access flags of the
	                                way but without considering turn restrictions). Permitted candidate ways include the route. */
	int num_new_motorways;     /**< Number of permitted candidate ways that are motorway-like */
	int num_other_ways;        /**< Number of permitted candidate ways that are neither ramps nor motorway-like */
	int old_cat;               /**< Maneuver category of the way leading to the maneuver */
	int new_cat;               /**< Maneuver category of the selected way after the maneuver */
	int max_cat;               /**< Highest maneuver category of any permitted candidate way other than the route */
	int num_similar_ways;      /**< Number of candidate ways (including the route) that have a {@code maneuver_category()} similar
	                                to {@code old_cat}. See {@code maneuver_required2()} for definition of "similar". */
	int left;                  /**< Minimum bearing delta of any candidate way left of the route, -180 for none */
	int right;                 /**< Minimum bearing delta of any candidate way right of the route, 180 for none */
	int is_unambiguous;        /**< Whether the maneuver is unambiguous. A maneuver is unambiguous if, despite
	                                multiple candidate way being available, we can reasonable expect the driver to
	                                continue on the route without being told to do so. This is typically the case when
	                                the route stays on the main road and goes straight, while all other candidate ways
	                                are minor roads and involve a significant turn. */
	int is_same_street;        /**< Whether the street keeps its name after the maneuver. */
};
// -- NEW 002 --





struct navigation_command
{
	struct navigation_itm *itm;
	struct navigation_command *next;
	struct navigation_command *prev;
	int delta;
	int delta_real;
	int roundabout_delta;
	int length;
// -- NEW 002 --
	struct navigation_maneuver *maneuver;  /**< Details on the maneuver to perform */
// -- NEW 002 --
};







// -- NEW 002 --
/*@brief A linked list conataining the destination of the road
 *
 *
 * Holds the destination info from the road, that is the place
 * you drive to if you keep following the road as found on
 * traffic sign's (ex. Paris, Senlis ...)
 *
 *
 */
struct street_destination
{
	struct street_destination *next;
	char *destination;
};
// -- NEW 002 --





static void navigation_flush(struct navigation *this_);

/**
 * @brief Calculates the delta between two angles
 * @param angle1 The first angle
 * @param angle2 The second angle
 * @return The difference between the angles: -179..-1=angle2 is left of angle1,0=same,1..179=angle2 is right of angle1,180=angle1 is opposite of angle2
 */

static int angle_delta(int angle1, int angle2)
{
	int delta = angle2 - angle1;

	if (delta <= -180)
		delta += 360;

	if (delta > 180)
		delta -= 360;

	return delta;
}

static int angle_median(int angle1, int angle2)
{
	int delta = angle_delta(angle1, angle2);
	int ret = angle1 + delta / 2;

	if (ret < 0)
		ret += 360;

	if (ret > 360)
		ret -= 360;

	return ret;
}

static int angle_opposite(int angle)
{
	return ((angle + 180) % 360);
}

int navigation_get_attr(struct navigation *this_, enum attr_type type, struct attr *attr, struct attr_iter *iter)
{
	struct map_rect *mr;
	struct item *item;
	// dbg(1, "enter %s\n", attr_to_name(type));
	switch (type)
	{
		case attr_map:
			attr->u.map = this_->map;
			break;
		case attr_item_type:
		case attr_length:
		case attr_navigation_speech:
			mr = map_rect_new(this_->map, NULL);
			while ((item = map_rect_get_item(mr)))
			{
				if (item->type != type_nav_none && item->type != type_nav_position)
				{
					if (type == attr_item_type)
						attr->u.item_type = item->type;
					else
					{
						if (!item_attr_get(item, type, attr))
							item = NULL;
					}
					break;
				}
			}
			map_rect_destroy(mr);
			if (!item)
				return 0;
			break;
		default:
			return 0;
	}
	attr->type = type;
	return 1;
}

int navigation_set_attr(struct navigation *this_, struct attr *attr)
{
	switch (attr->type)
	{
		case attr_speech:
			this_->speech = attr->u.speech;
			return 1;
		default:
			return 0;
	}
}

struct navigation *
navigation_new(struct attr *parent, struct attr **attrs)
{
	int i, j;
	struct attr * attr;
	struct navigation *ret=g_new0(struct navigation, 1);
	ret->hash = item_hash_new();
	ret->callback = callback_list_new("navigation_new:ret->callback");
	ret->callback_speech = callback_list_new("navigation_new:ret->callback_speech");
	ret->level_last = -2;
	ret->distance_turn = 50;
	ret->turn_around_limit = 2; // at first occurence of "turn around" -> say so!
	ret->navit = parent->u.navit;
	ret->tell_street_name = 1;
	ret->previous = NULL;
	ret->cmd_previous = NULL;

	for (j = 0; j <= route_item_last - route_item_first; j++)
	{
		for (i = 0; i < 3; i++)
		{
			ret->announce[j][i] = -1;
		}
	}

	if ((attr = attr_search(attrs, NULL, attr_tell_street_name)))
	{
		ret->tell_street_name = attr->u.num;
	}
	if ((attr = attr_search(attrs, NULL, attr_delay)))
	{
		ret->delay = attr->u.num;
	}

	return ret;
}

int navigation_set_announce(struct navigation *this_, enum item_type type, int *level)
{
	int i;
	if (type < route_item_first || type > route_item_last)
	{
		dbg(0, "street type %d out of range [%d,%d]", type, route_item_first, route_item_last);
		return 0;
	}

	for (i = 0; i < 3; i++)
	{
		// dbg(0, "announce=%d type=%s\n", level[i], item_to_name(type));
		this_->announce[type - route_item_first][i] = level[i];
	}

	return 1;
}

// global var ------------
static int level_static_for_bicycle[3];
// global var ------------



// ------------------------------------
// returns: "0", "1", "2" or "3"
// ------------------------------------
static int navigation_get_announce_level(struct navigation *this_, enum item_type type, int dist, float speed_in_ms)
{
	int i = 3;

	if (type < route_item_first || type > route_item_last)
	{
		return -1;
	}

	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
#if 0
		for (i = 0; i < 3; i++)
		{
			// dbg(0, "loop(a) i=%d type=%s, route_item_first=%x, announce=%d, dist=%d\n", i, item_to_name(type), route_item_first, level_static_for_bicycle[i], dist);
			if (dist <= level_static_for_bicycle[i])
			{
				//dbg(0, "ret(1a)=%d\n", i);
				return i;
			}
		}
#endif

		for (i = 0; i < 3; i++)
		{
			if (speed_in_ms > 19.444f) // > 70 km/h
			{

				if (i == 0)
				{
					if (dist <= (int)(global_b_level0_announcement * global_b_levelx_announcement_factor * speed_in_ms) )
					{
						return i;
					}
				}
				else if (i == 1)
				{
					if (dist <= (int)(global_b_level1_announcement * global_b_levelx_announcement_factor * speed_in_ms) )
					{
						return i;
					}
				}
				else
				{
					if (dist <= (int)(global_b_level2_announcement * global_b_levelx_announcement_factor * speed_in_ms) )
					{
						return i;
					}
				}
			}
			else if (speed_in_ms > 0.9f)
			{
				if (i == 0)
				{
					// always have at least 12 meters to turn for level 0 announcement
					if (dist <= 12)
					{
						return i;
					}

					if (dist <= (int)(global_b_level0_announcement * speed_in_ms) )
					{
						dbg(0, "NCC_:0:%d %f\n", (int)(global_b_level0_announcement * speed_in_ms), speed_in_ms);
						return i;
					}
				}
				else if (i == 1)
				{
					if (dist <= (int)(global_b_level1_announcement * speed_in_ms) )
					{
						dbg(0, "NCC_:1:%d %f\n", (int)(global_b_level1_announcement * speed_in_ms), speed_in_ms);
						return i;
					}
				}
				else
				{
					if (dist <= (int)(global_b_level2_announcement * speed_in_ms) )
					{
						dbg(0, "NCC_:2:%d %f\n", (int)(global_b_level2_announcement * speed_in_ms), speed_in_ms);
						return i;
					}
				}
			}
			else
			{
				if (dist <= level_static_for_bicycle[i])
				{
					//dbg(0, "ret(1a)=%d\n", i);
					return i;
				}
			}

		}

	}
	else
	{

		for (i = 0; i < 3; i++)
		{
			//dbg(0, "loop(b) i=%d type=%s, route_item_first=%x, announce=%d, dist=%d\n", i, item_to_name(type), route_item_first, (this_->announce[type - route_item_first][i]), dist);


			if (speed_in_ms > 19.444f) // > 70 km/h
			{
				if (i == 0)
				{
					if (dist <= (int)(global_level0_announcement * global_levelx_announcement_factor * speed_in_ms) )
					{
						return i;
					}
				}
				else if (i == 1)
				{
					if (dist <= (int)(global_level1_announcement * global_levelx_announcement_factor * speed_in_ms) )
					{
						return i;
					}
				}
				else
				{
					if (dist <= (int)(global_level2_announcement * global_levelx_announcement_factor * speed_in_ms) )
					{
						return i;
					}
				}
			}
			else if (speed_in_ms > 0.9f)
			{
				if (i == 0)
				{
					// always have at least 12 meters to turn for level 0 announcement
					if (dist <= 12)
					{
						return i;
					}

					if (dist <= (int)(global_level0_announcement * speed_in_ms) )
					{
						dbg(0, "NCC_:0:%d %f\n", (int)(global_level0_announcement * speed_in_ms), speed_in_ms);
						return i;
					}
				}
				else if (i == 1)
				{
					if (dist <= (int)(global_level1_announcement * speed_in_ms) )
					{
						dbg(0, "NCC_:1:%d %f\n", (int)(global_level1_announcement * speed_in_ms), speed_in_ms);
						return i;
					}
				}
				else
				{
					if (dist <= (int)(global_level2_announcement * speed_in_ms) )
					{
						dbg(0, "NCC_:2:%d %f\n", (int)(global_level2_announcement * speed_in_ms), speed_in_ms);
						return i;
					}
				}
			}
			else
			{
				if (dist <= this_->announce[type - route_item_first][i])
				{
					//dbg(0, "ret(1b)=%d\n", i);
					return i;
				}
			}
		}
	}


	//dbg(0, "ret(2)=%d\n", i);

	return i;
}


static int navigation_get_announce_dist_for_level_on_item_bicycle(struct navigation *this_, enum item_type type, int level, float speed_in_ms)
{

	if (speed_in_ms > 19.444f) // > 70 km/h
	{
		if (level == 0)
		{
			return (int)(global_b_level0_announcement * global_b_levelx_announcement_factor * speed_in_ms);
		}
		else if (level == 1)
		{
			return (int)(global_b_level1_announcement * global_b_levelx_announcement_factor * speed_in_ms);
		}
		else
		{
			return (int)(global_b_level2_announcement * global_b_levelx_announcement_factor * speed_in_ms);
		}
	}
	else if (speed_in_ms > 0.9f)
	{
		if (level == 0)
		{
			// always have at least 12 meters to turn for level 0 announcement
			if ( (int)(global_b_level0_announcement * speed_in_ms) < 12)
			{
				return 12;
			}
			else
			{
				return (int)(global_b_level0_announcement * speed_in_ms);
			}
		}
		else if (level == 1)
		{
			return (int)(global_b_level1_announcement * speed_in_ms);
		}
		else
		{
			return (int)(global_b_level2_announcement * speed_in_ms);
		}
	}
	else
	{
		return level_static_for_bicycle[level];
	}
}


static int navigation_get_announce_dist_for_level_on_item(struct navigation *this_, enum item_type type, int level, float speed_in_ms)
{

	if (speed_in_ms > 19.444f) // > 70 km/h
	{
		if (level == 0)
		{
			return (int)(global_level0_announcement * global_levelx_announcement_factor * speed_in_ms);
		}
		else if (level == 1)
		{
			return (int)(global_level1_announcement * global_levelx_announcement_factor * speed_in_ms);
		}
		else
		{
			return (int)(global_level2_announcement * global_levelx_announcement_factor * speed_in_ms);
		}
	}
	else if (speed_in_ms > 0.9f)
	{
		if (level == 0)
		{
			// always have at least 12 meters to turn for level 0 announcement
			if ( (int)(global_level0_announcement * speed_in_ms) < 12)
			{
				return 12;
			}
			else
			{
				return (int)(global_level0_announcement * speed_in_ms);
			}
		}
		else if (level == 1)
		{
			return (int)(global_level1_announcement * speed_in_ms);
		}
		else
		{
			return (int)(global_level2_announcement * speed_in_ms);
		}
	}
	else
	{
		return this_->announce[type - route_item_first][level];
	}
}

static int is_way_allowed(struct navigation *nav, struct navigation_way *way, int mode);

static int navigation_get_announce_level_cmd(struct navigation *this_, struct navigation_itm *itm, struct navigation_command *cmd, int distance, float speed_in_ms)
{

	int level2, level = navigation_get_announce_level(this_, itm->way.item.type, distance, speed_in_ms);

	if (this_->cmd_first->itm->prev)
	{
		level2 = navigation_get_announce_level(this_, cmd->itm->prev->way.item.type, distance, speed_in_ms);
		if (level2 > level)
		{
			level = level2;
		}
	}

	return level;
}

/* 0=N,90=E */
static int road_angle_accurate(struct coord *c1, struct coord *c2, int dir)
{
	// COST: 006 acc
	// dbg(0, "COST:006acc\n");

	int ret = transform_get_angle_delta_accurate(c1, c2, dir);
	return ret;
}


static int road_angle(struct coord *c1, struct coord *c2, int dir)
{
	// COST: 006
	// dbg(0, "COST:006\n");

	int ret = transform_get_angle_delta(c1, c2, dir);
	// dbg(1, "road_angle(0x%x,0x%x - 0x%x,0x%x)=%d\n", c1->x, c1->y, c2->x, c2->y, ret);
	return ret;
}

static char *get_count_str(int n)
{
	switch (n)
	{
		case 0:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:first\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the first street)
			return _("first"); // Not sure if this exists, neither if it will ever be needed
		case 1:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:first\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the first street)
			return _("first");
		case 2:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:second\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the second street)
			return _("second");
		case 3:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:third\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the third street)
			return _("third");
		case 4:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:fourth\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the fourth street)
			return _("fourth");
		case 5:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:fifth\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the fifth street)
			return _("fifth");
		case 6:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:sixth\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the sixth street)
			return _("sixth");
		case 7:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:seventh\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the seventh street)
			return _("seventh");
		case 8:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:eighth\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the eighth street)
			return _("eighth");
		case 9:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:ninth\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to streets (example: turn right after the ninth street)
			return _("ninth");
		default:
			return NULL;
	}
}

static char *get_exit_count_str(int n)
{
	switch (n)
	{
		case 0:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:first exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the first exit)
			return _("first exit"); // Not sure if this exists, neither if it will ever be needed
		case 1:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:first exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the first exit)
			return _("first exit");
		case 2:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:second exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the second exit)
			return _("second exit");
		case 3:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:third exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the third exit)
			return _("third exit");
		case 4:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:fourth exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the fourth exit)
			return _("fourth exit");
		case 5:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:fifth exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the fifth exit)
			return _("fifth exit");
		case 6:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:sixth exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the sixth exit)
			return _("sixth exit");
		case 7:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:seventh exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the seventh exit)
			return _("seventh exit");
		case 8:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:eighth exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the eighth exit)
			return _("eighth exit");
		case 9:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:ninth exit\n");
#endif
#endif
			// TRANSLATORS: the following counts refer to roundabout exits (example: leave the roundabout at the ninth exit)
			return _("ninth exit");
		default:
			return NULL;
	}
}

static int round_distance(int dist)
{
	if (dist < 100)
	{
		dist = (dist + 5) / 10;
		return dist * 10;
	}
	if (dist < 250)
	{
		dist = (dist + 13) / 25;
		return dist * 25;
	}
	if (dist < 500)
	{
		dist = (dist + 25) / 50;
		return dist * 50;
	}
	if (dist < 1000)
	{
		dist = (dist + 50) / 100;
		return dist * 100;
	}
	if (dist < 5000)
	{
		dist = (dist + 50) / 100;
		return dist * 100;
	}
	if (dist < 100000)
	{
		dist = (dist + 500) / 1000;
		return dist * 1000;
	}
	dist = (dist + 5000) / 10000;
	return dist * 10000;
}

static int round_for_vocabulary(int vocabulary, int dist, int factor)
{

	//dbg(0, "DIST_FEET:rfv:001:d=%d f=%d\n", dist, factor);

	//if (!(vocabulary & 256))
	//{
		if (factor != 1)
		{
			dist = (dist + factor / 2) / factor;
			//dbg(0, "DIST_FEET:rfv:002:d=%d\n", dist);
		}
	//}
	//else
	//{
	//	factor = 1;
	//}

	if (!(vocabulary & 255))
	{
		int i = 0, d = 0, m = 0;
		while (distances[i] > 0)
		{
			if (!i || abs(distances[i] - dist) <= d)
			{
				d = abs(distances[i] - dist);
				//dbg(0, "DIST_FEET:rfv:003:d=%d\n", dist);
				m = i;
			}

			if (distances[i] > dist)
			{
				break;
			}
			i++;
		}
		// dbg(0, "converted %d to %d with factor %d\n", dist, distances[m], factor);
		dist = distances[m];
	}

	//dbg(0, "DIST_FEET:rfv:002:d=%d f=%d res=%d\n", dist, factor, dist * factor);

	return dist * factor;
}

static int vocabulary_last(int vocabulary)
{
	int i = 0;

	if (vocabulary == 65535)
	{
		return 1000;
	}

	while (distances[i] > 0)
	{
		i++;
	}

	return distances[i - 1];
}

char *
get_distance(struct navigation *nav, int dist, enum attr_type type, int is_length)
{
	int imperial = 0;
	int vocabulary = 65535;
	struct attr attr;

	if (type == attr_navigation_long)
	{
		if (is_length)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:%d m\n");
#endif
#endif
			return g_strdup_printf(_("%d m"), dist);
		}
		else
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:in %d m\n");
#endif
#endif
			return g_strdup_printf(_("in %d m"), dist);
		}
	}

	if (navit_get_attr(nav->navit, attr_imperial, &attr, NULL))
	{
		imperial = attr.u.num;
	}

	if (nav->speech && speech_get_attr(nav->speech, attr_vocabulary_distances, &attr, NULL))
	{
		vocabulary = attr.u.num;
	}

	if (imperial)
	{
		//dbg(0, "DIST_FEET:001:d=%d\n", dist);

		// if (dist * FEET_PER_METER < vocabulary_last(vocabulary))
		if (dist * FEET_PER_METER < 5300) // fix for "0"-miles later!!
		{
			if (( dist * FEET_PER_METER ) > 1900)
			{
				//dbg(0, "DIST_FEET:002a:d=%d m=%d\n", dist, (int)(dist * FEET_PER_METER));
				dist = round_for_vocabulary(vocabulary, dist * FEET_PER_METER, 500);
			}
			else if (( dist * FEET_PER_METER ) > 290)
			{
				//dbg(0, "DIST_FEET:002a:d=%d m=%d\n", dist, (int)(dist * FEET_PER_METER));
				dist = round_for_vocabulary(vocabulary, dist * FEET_PER_METER, 100);
			}
			else if (( dist * FEET_PER_METER ) > 25)
			{
				//dbg(0, "DIST_FEET:002a:d=%d m=%d\n", dist, (int)(dist * FEET_PER_METER));
				dist = round_for_vocabulary(vocabulary, dist * FEET_PER_METER, 20);
			}
			else
			{
				//dbg(0, "DIST_FEET:002b:d=%d m=%d\n", dist, dist * FEET_PER_METER);
				dist = round_for_vocabulary(vocabulary, dist * FEET_PER_METER, 1);
			}

			//dbg(0, "DIST_FEET:002:d=%d v=%d\n", dist, vocabulary);

			if (is_length)
			{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:%d feet\n");
#endif
#endif
				return g_strdup_printf(_("%d feet"), dist);
			}
			else
			{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:in %d feet\n");
#endif
#endif
				return g_strdup_printf(_("in %d feet"), dist);
			}
		}
	}
	else
	{
		if (dist < vocabulary_last(vocabulary))
		{
			dist = round_for_vocabulary(vocabulary, dist, 1);
			if (is_length)
			{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:%d meters\n");
				gchar* xy=g_strdup_printf("+*#1:%d\n", dist);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				return g_strdup_printf(_("%d meters"), dist);
			}
			else
			{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:in %d meters\n");
				gchar* xy=g_strdup_printf("+*#1:%d\n", dist);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				return g_strdup_printf(_("in %d meters"), dist);
			}
		}
	}

	if (imperial)
	{
		//dbg(0, "DIST_FEET:003:d=%d v=%d\n", dist, vocabulary);
		dist = round_for_vocabulary(vocabulary, dist * FEET_PER_METER * 1000 / FEET_PER_MILE, 1000);
		//dbg(0, "DIST_FEET:004:d=%d v=%d\n", dist, vocabulary);
	}
	else
	{
		dist = round_for_vocabulary(vocabulary, dist, 1000);
	}

	if (dist < 5000)
	{
		int rem = (dist / 100) % 10;
		if (rem)
		{
			if (imperial)
			{
				if (is_length)
				{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:%d.%d miles\n");
#endif
#endif
					return g_strdup_printf(_("%d.%d miles"), dist / 1000, rem);
				}
				else
				{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:in %d.%d miles\n");
#endif
#endif
					//dbg(0, "DIST_FEET:005:d/1000=%d rem=%d\n", dist / 1000, rem);

					return g_strdup_printf(_("in %d.%d miles"), dist / 1000, rem);
				}
			}
			else
			{
				if (is_length)
				{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:%d.%d kilometers\n");
#endif
#endif
					return g_strdup_printf(_("%d.%d kilometers"), dist / 1000, rem);
				}
				else
				{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:in %d.%d kilometers\n");
#endif
#endif
					return g_strdup_printf(_("in %d.%d kilometers"), dist / 1000, rem);
				}
			}
		}
	}

	if (imperial)
	{
		if (is_length)
		{
			return g_strdup_printf(ngettext("one mile", "%d miles", dist / 1000), dist / 1000);
		}
		else
		{
			//dbg(0, "DIST_FEET:006:d/1000=%d\n", dist / 1000);

			return g_strdup_printf(ngettext("in one mile", "in %d miles", dist / 1000), dist / 1000);
		}
	}
	else
	{
		if (is_length)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:one kilometer|%d kilometers\n");
#endif
#endif
			return g_strdup_printf(ngettext("one kilometer", "%d kilometers", dist / 1000), dist / 1000);
		}
		else
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:in one kilometer|in %d kilometers\n");
#endif
#endif
			return g_strdup_printf(ngettext("in one kilometer", "in %d kilometers", dist / 1000), dist / 1000);
		}
	}
}

static int navigation_get_real_item_first_coord(struct navigation_way *w, struct coord *c)
{
	struct item *ritem = NULL; // the "real" item
	struct map_rect *mr;
	struct attr attr;
	struct coord c2[5];


#if 1
	if (!w)
	{
		return 0;
	}

	if (!w->item.map)
	{
		return 0;
	}

	if ((!w->item.id_hi) && (w->item.id_lo))
	{
		return 0;
	}

	mr = map_rect_new(w->item.map, NULL);

	if (!mr)
	{
		return 0;
	}

	// COST: 001
	dbg(0, "COST:001\n");
	ritem = map_rect_get_item_byid(mr, w->item.id_hi, w->item.id_lo);

	if (!ritem)
	{
		map_rect_destroy(mr);
		return 0;
	}

	if (item_coord_get(ritem, c2, 2) == 2)
	{
		c[0].x = c2[0].x;
		c[0].y = c2[0].y;
		c[1].x = c2[1].x;
		c[1].y = c2[1].y;

		map_rect_destroy(mr);
		return 1;
	}

	map_rect_destroy(mr);
	return 0;

#endif
}

long long navigation_item_get_wayid(struct navigation_way *w)
{
	struct item *ritem; // the "real" item
	struct map_rect *mr;
	struct attr attr;
	long long ret = 0;

	if (!w)
	{
		return ret;
	}

	if (!w->item.map)
	{
		return ret;
	}

	if ((!w->item.id_hi) && (w->item.id_lo))
	{
		return ret;
	}

	mr = map_rect_new(w->item.map, NULL);

	if (!mr)
	{
		return ret;
	}

	// COST: 002
	dbg(0, "COST:002\n");
	ritem = map_rect_get_item_byid(mr, w->item.id_hi, w->item.id_lo);

	if (!ritem)
	{
		map_rect_destroy(mr);
		return ret;
	}

	if (item_attr_get(ritem, attr_osm_wayid, &attr))
	{
		// dbg(0, "START:attr.u.num64=%x attr.u.num64=%lld\n", attr.u.num64, attr.u.num64);
		if (attr.u.num64)
		{
			ret = *attr.u.num64;
		}
		// dbg(0, "END:attr.u.num64\n");
	}

	map_rect_destroy(mr);

	// dbg(0, "return 099\n");

	return ret;
}

int navigation_item_get_flags(struct navigation_way *w)
{
	struct item *ritem; // the "real" item
	struct map_rect *mr;
	struct attr attr;
	long long ret = 0;

	if (!w)
	{
		return ret;
	}


	// -----------------------------------------
	// -----------------------------------------
	// flags should be there already!?
	if (w->flags != 0)
	{
		// we already have flags set!
		return w->flags;
	}
	// -----------------------------------------
	// -----------------------------------------


	if (!w->item.map)
	{
		return ret;
	}

	if ((!w->item.id_hi) && (w->item.id_lo))
	{
		return ret;
	}

	mr = map_rect_new(w->item.map, NULL);

	if (!mr)
	{
		return ret;
	}

	// COST: 003
	dbg(0, "COST:003\n");
	ritem = map_rect_get_item_byid(mr, w->item.id_hi, w->item.id_lo);

	if (!ritem)
	{
		map_rect_destroy(mr);
		return ret;
	}

	if (item_attr_get(ritem, attr_flags, &attr))
	{
		ret = attr.u.num;
		// also set the flags here!!
		w->flags = ret;
	}
	else
	{
		w->flags = 0;
	}

	map_rect_destroy(mr);

	// dbg(0, "return 099\n");

	return ret;
}


/**
 * @brief This calculates the angle with which an item starts or ends
 *
 * This function can be used to get the angle an item (from a route graph map)
 * starts or ends with. Note that the angle will point towards the inner of
 * the item.
 *
 * This is meant to be used with items from a route graph map
 * With other items this will probably not be optimal...
 *
 * @param w The way which should be calculated
 */
static void calculate_angle(struct navigation_way *w)
{
	struct coord cbuf[2];
	struct item *ritem; // the "real" item
	struct coord c;
	struct map_rect *mr;
	struct attr attr;

	w->angle2 = 361;
	mr = map_rect_new(w->item.map, NULL);

	if (!mr)
	{
		return;
	}

	// COST: 004
	dbg(0, "COST:004\n");
	ritem = map_rect_get_item_byid(mr, w->item.id_hi, w->item.id_lo);

	if (!ritem)
	{
		// dbg(1, "Item from segment not found on map!\n");
		map_rect_destroy(mr);
		return;
	}

	if (ritem->type < type_line || ritem->type >= type_area)
	{
		map_rect_destroy(mr);
		return;
	}


	if (item_attr_get(ritem, attr_flags, &attr))
	{
		w->flags = attr.u.num;
	}
	else
	{
		w->flags = 0;
	}
	// dbg(0, "w->flags=%x\n", w->flags);


	if (item_attr_get(ritem, attr_street_name, &attr))
	{
		w->name1 = map_convert_string(ritem->map, attr.u.str);
	}
	else
	{
		w->name1 = NULL;
	}


	if (item_attr_get(ritem, attr_street_name_systematic, &attr))
	{
		w->name2 = map_convert_string(ritem->map, attr.u.str);
	}
	else
	{
		w->name2 = NULL;
	}


	if (w->dir < 0)
	{
		if (item_coord_get(ritem, cbuf, 2) != 2)
		{
			// dbg(1, "Using calculate_angle() with a less-than-two-coords-item?\n");
			map_rect_destroy(mr);
			return;
		}

		while (item_coord_get(ritem, &c, 1))
		{
			cbuf[0] = cbuf[1];
			cbuf[1] = c;
		}

	}
	else
	{
		if (item_coord_get(ritem, cbuf, 2) != 2)
		{
			// dbg(1, "Using calculate_angle() with a less-than-two-coords-item?\n");
			map_rect_destroy(mr);
			return;
		}

		c = cbuf[0];
		cbuf[0] = cbuf[1];
		cbuf[1] = c;
	}

	map_rect_destroy(mr);

	// w->angle2 = road_angle_accurate(&cbuf[1], &cbuf[0], 0);
	w->angle2 = road_angle(&cbuf[1], &cbuf[0], 0);
}

/**
 * @brief Returns the time (in seconds) one will drive between two navigation items
 *
 * This function returns the time needed to drive between two items, including both of them,
 * in seconds.
 *
 * @param from The first item
 * @param to The last item
 * @return The travel time in seconds, or -1 on error
 */
static int navigation_time(struct navigation_itm *from, struct navigation_itm *to)
{
	struct navigation_itm *cur;
	int time;

	time = 0;
	cur = from;
	while (cur)
	{
		time += cur->time;

		if (cur == to)
		{
			break;
		}
		cur = cur->next;
	}

	if (!cur)
	{
		return -1;
	}

	return time;
}



static int navigation_time_real_speed(struct navigation_itm *from, struct navigation_itm *to, int speed_in_kmh)
{
	struct navigation_itm *cur;
	int time;
	int first = 1;
	float speed_in_ms = (float) ((float)speed_in_kmh / 3.6f);

	time = 0;
	cur = from;

	while (cur)
	{

		if (first == 1)
		{
			first = 0;
			if (speed_in_kmh < 1)
			{
				time += cur->time;
			}
			else
			{
				time = time + (int)(     ((float)cur->length / speed_in_ms ) * 10.0f    );
			}
		}
		else
		{
			time += cur->time;
		}

		if (cur == to)
		{
			break;
		}
		cur = cur->next;
	}

	if (!cur)
	{
		return -1;
	}

	return time;
}




// -- NEW 002 --
static void navigation_free_list(struct street_destination *list)
{
	if (list)
	{
		struct street_destination *clist = NULL;
		while (list)
		{
			clist = list->next;
			if (list->destination)
			{
				g_free(list->destination);
			}

			g_free(list);
			list = clist;
		}
		list = NULL;
	}
}
// -- NEW 002 --





/**
 * @brief Clears the ways one can drive from itm
 *
 * @param itm The item that should have its ways cleared
 */
static void navigation_itm_ways_clear(struct navigation_itm *itm)
{
	struct navigation_way *c, *n;

	c = itm->way.next;
	while (c)
	{
		n = c->next;
		map_convert_free(c->name1);
		map_convert_free(c->name2);

		if (c->exit_ref)
		{
			g_free(c->exit_ref);
			c->exit_ref = NULL;
		}

		if (c->exit_label)
		{
			g_free(c->exit_label);
			c->exit_label = NULL;
		}

		if (c->street_dest_text)
		{
			g_free(c->street_dest_text);
			c->street_dest_text = NULL;
		}

		if (c->s_destination)
		{
			navigation_free_list(c->s_destination);
		}

		g_free(c);
		c = NULL;

		c = n;
	}

	itm->way.next = NULL;
}

/**
 * @brief Updates the ways one can drive from itm
 *
 * This updates the list of possible ways to drive to from itm. The item "itm" is on
 * and the next navigation item are excluded.
 *
 * @param itm The item that should be updated
 * @param graph_map The route graph's map that these items are on 
 */
static void navigation_itm_ways_update(struct navigation_itm *itm, struct map *graph_map)
{
	struct map_selection coord_sel;
	struct map_rect *g_rect; // Contains a map rectangle from the route graph's map
	struct item *i, *sitem;
	struct attr sitem_attr, direction_attr;
	// struct attr flags_attr;
	struct navigation_way *w, *l;

	navigation_itm_ways_clear(itm);

	// These values cause the code in route.c to get us only the route graph point and connected segments
	coord_sel.next = NULL;
	coord_sel.u.c_rect.lu = itm->start;
	coord_sel.u.c_rect.rl = itm->start;
	// the selection's order is ignored

	g_rect = map_rect_new(graph_map, &coord_sel);

	i = map_rect_get_item(g_rect);

	if (!i || i->type != type_rg_point)
	{ // probably offroad?
		return;
	}

	w = NULL;

	while (1)
	{
		i = map_rect_get_item(g_rect);

		if (!i)
		{
			break;
		}

		if (i->type != type_rg_segment)
		{
			continue;
		}

		if (!item_attr_get(i, attr_street_item, &sitem_attr))
		{
			// dbg(1, "Got no street item for route graph item in entering_straight()\n");
			continue;
		}

		if (!item_attr_get(i, attr_direction, &direction_attr))
		{
			continue;
		}

		sitem = sitem_attr.u.item;

		if (sitem->type == type_street_turn_restriction_no || sitem->type == type_street_turn_restriction_only)
		{
			continue;
		}

		if (item_is_equal(itm->way.item, *sitem) || ((itm->prev) && item_is_equal(itm->prev->way.item, *sitem)))
		{
			continue;
		}

		//if (!item_attr_get(i, attr_flags, &flags_attr))
		//{
		//	flags_attr.u.num = 0;
		//}
		//dbg(0, "w2->flags=%x\n", flags_attr.u.num);

		l = w;
		w = g_new0(struct navigation_way, 1);
		w->dir = direction_attr.u.num;
		w->item = *sitem;
		w->next = l;
		// w->flags = flags_attr.u.num;

		calculate_angle(w);
	}

	map_rect_destroy(g_rect);

	itm->way.next = w;
}

static void navigation_destroy_itms_cmds(struct navigation *this_, struct navigation_itm *end)
{
	struct navigation_itm *itm;
	struct navigation_command *cmd;

	// dbg(2, "enter this_=%p this_->first=%p this_->cmd_first=%p end=%p\n", this_, this_->first, this_->cmd_first, end);

	if (this_->cmd_first)
	{
		// dbg(2, "this_->cmd_first->itm=%p\n", this_->cmd_first->itm);
	}

	while (this_->first && this_->first != end)
	{
		itm = this_->first;
		// dbg(3, "destroying %p\n", itm);
		item_hash_remove(this_->hash, &itm->way.item);
		this_->first = itm->next;

		if (this_->first)
		{
			this_->first->prev = NULL;
		}

		if (this_->cmd_first && this_->cmd_first->itm == itm->next)
		{
			cmd = this_->cmd_first;
			this_->cmd_first = cmd->next;
			if (cmd->next)
			{
				cmd->next->prev = NULL;
			}
			g_free(cmd);
			// FF: ??
			// cmd = NULL;
		}

		map_convert_free(itm->way.name1);
		map_convert_free(itm->way.name2);

		if (itm->way.s_destination)
		{
			navigation_free_list(itm->way.s_destination);
		}

		if (itm->way.exit_ref)
		{
			g_free(itm->way.exit_ref);
			itm->way.exit_ref = NULL;
		}

		if (itm->way.exit_label)
		{
			g_free(itm->way.exit_label);
			itm->way.exit_label = NULL;
		}

		if (itm->way.street_dest_text)
		{
			g_free(itm->way.street_dest_text);
			itm->way.street_dest_text = NULL;
		}

		navigation_itm_ways_clear(itm);

		g_free(itm);
		itm = NULL;
	}

	if (!this_->first)
	{
		this_->last = NULL;
	}

	if (!this_->first && end)
	{
		// dbg(0, "end wrong\n");
	}
}

static void navigation_itm_update(struct navigation_itm *itm, struct item *ritem)
{
	struct attr length, time, speed;

	if (!item_attr_get(ritem, attr_length, &length))
	{
		// dbg(0, "no length\n");
		return;
	}

	if (!item_attr_get(ritem, attr_time, &time))
	{
		// dbg(0, "no time\n");
		return;
	}

	if (!item_attr_get(ritem, attr_speed, &speed))
	{
		// dbg(0, "no speed\n");
		return;
	}

	// dbg(1, "length=%d time=%d speed=%d\n", length.u.num, time.u.num, speed.u.num);
	itm->length = length.u.num;
	itm->time = time.u.num;
	itm->speed = speed.u.num;
}

/**
 * @brief This check if an item is part of a roundabout
 *
 * @param itm The item to be checked
 * @return True if the item is part of a roundabout
 */
static int check_roundabout(struct navigation_itm *itm, struct map *graph_map)
{
	struct map_selection coord_sel;
	struct map_rect *g_rect; // Contains a map rectangle from the route graph's map
	struct item *i, *sitem;
	struct attr sitem_attr, flags_attr;

	// These values cause the code in route.c to get us only the route graph point and connected segments
	coord_sel.next = NULL;
	coord_sel.u.c_rect.lu = itm->start;
	coord_sel.u.c_rect.rl = itm->start;
	// the selection's order is ignored

	g_rect = map_rect_new(graph_map, &coord_sel);

	i = map_rect_get_item(g_rect);

	if (!i || i->type != type_rg_point)
	{
		// probably offroad?
		map_rect_destroy(g_rect);
		return 0;
	}

	while (1)
	{
		i = map_rect_get_item(g_rect);

		if (!i)
		{
			break;
		}

		if (i->type != type_rg_segment)
		{
			continue;
		}

		if (!item_attr_get(i, attr_street_item, &sitem_attr))
		{
			continue;
		}

		sitem = sitem_attr.u.item;
		if (item_is_equal(itm->way.item, *sitem))
		{
			if (item_attr_get(i, attr_flags, &flags_attr) && (flags_attr.u.num & NAVIT_AF_ROUNDABOUT))
			{
				map_rect_destroy(g_rect);
				return 1;
			}
		}
	}

	map_rect_destroy(g_rect);
	return 0;
}



// -- NEW 002 --
static int navigation_split_string_to_list(struct navigation_way *way, char* raw_string, char sep)
{
	struct street_destination *new_street_destination = NULL;
	struct street_destination *next_street_destination_remember = NULL;
	char *pos1 = raw_string;
	char *pos2 = NULL;
	int count = 0;

	navigation_free_list(way->s_destination); /*in case this is a retry with a different separator.*/

	//dbg(0,"raw_string=%s split with %c\n",raw_string, sep);
	if (strlen(raw_string) > 0)
	{
		count = 1;
		while (pos1)
		{
			new_street_destination = g_new0(struct street_destination, 1);
			new_street_destination->next = next_street_destination_remember;
			next_street_destination_remember = new_street_destination;
			if ((pos2 = strrchr(pos1, sep)) != NULL)
			{
				new_street_destination->destination = g_strdup(pos2 + 1);
				*pos2 = '\0' ;
				//dbg(0,"splitted_off_string=%s\n", new_street_destination->destination);
				count++;
			}
			else
			{
				new_street_destination->destination = g_strdup(pos1);
				pos1 = NULL;
				//dbg(0,"head_of_string=%s\n", new_street_destination->destination);
			}
			way->s_destination = next_street_destination_remember;
		}
	}
	return count;
}

static int navigation_split_string_to_list_2(struct navigation_way *way, char* raw_string, char sep, char sep2)
{
	struct street_destination *new_street_destination = NULL;
	struct street_destination *next_street_destination_remember = NULL;
	char *pos1 = raw_string;
	char *pos2 = NULL;
	char *pos2a = NULL;
	char *pos2b = NULL;
	int count = 0;

	navigation_free_list(way->s_destination); /*in case this is a retry with a different separator.*/

	//dbg(0,"raw_string=%s split with %c\n",raw_string, sep);
	if (strlen(raw_string) > 0)
	{
		count = 1;
		while (pos1)
		{
			new_street_destination = g_new0(struct street_destination, 1);
			new_street_destination->next = next_street_destination_remember;
			next_street_destination_remember = new_street_destination;

			pos2a = strrchr(pos1, sep);
			pos2b = strrchr(pos1, sep2);

			if (pos2a == NULL)
			{
				if (pos2b != NULL)
				{
					pos2 = pos2b;
				}
				else
				{
					pos2 = NULL;
				}
			}
			else if (pos2b == NULL)
			{
				if (pos2a != NULL)
				{
					pos2 = pos2a;					
				}
				else
				{
					pos2 = NULL;
				}
			}
			else // both NOT NULL
			{
				if (pos2a > pos2b)
				{
					pos2 = pos2b;
				}
				else
				{
					pos2 = pos2a;
				}
			}

			if (pos2 != NULL)
			{
				new_street_destination->destination = g_strdup(pos2 + 1);
				*pos2 = '\0' ;
				//dbg(0,"splitted_off_string=%s\n", new_street_destination->destination);
				count++;
			}
			else
			{
				new_street_destination->destination = g_strdup(pos1);
				pos1 = NULL;
				//dbg(0,"head_of_string=%s\n", new_street_destination->destination);
			}
			way->s_destination = next_street_destination_remember;
		}
	}
	return count;
}

// -- NEW 002 --








// -- NEW 002 --
/** @brief Selects the destination-names for the next announcement from the
 *         destination-names that are registered in the following command items.
 *
 *         The aim of this function is to find the destination-name entry that has the most hits in the following
 *         command items so that the destination name has a relevance over several announcements. If there is no 'winner'
 *         the entry is selected that is at top of the destination.
 *
 *         thanks to jandegr
 */
static navigation_select_announced_destinations(struct navigation_command *current_command)
{
	struct street_destination *current_destination = NULL;  /* the list pointer of the destination_names of the current command. */
	struct street_destination *search_destination = NULL;   /* the list pointer of the destination_names of the respective search_command. */

	struct navigation_command *search_command = NULL;   /* loop through every navigation command up to the end. */

	/* limits the number of entries of a destination sign as well as the number of command items to investigate */
	#define DEST_MAX_LOOPS_SIGN_TEXTS 10
	#define DEST_MAX_LOOPS_NAV_ITEMS 3

	int destination_count[DEST_MAX_LOOPS_SIGN_TEXTS] = {0,0,0,0,0,0,0,0,0,0};	/* countains the hits of identical destination signs over all */
						/* investigated command items - a 'high score' of destination names */

	int destination_index = 0;
	int search_command_counter = 0;
	int i;
	int max_hits;
	int max_hit_index;
	struct navigation_way *current_nav_way = NULL;

	// put text into way struct later -----------------
	current_nav_way = &(current_command->itm->way);
	if ((current_nav_way) && (current_nav_way->street_dest_text))
	{
		// already have set a destination --> return
		return;
	}
	current_nav_way->street_dest_text = NULL; // set default to "no value"
	// put text into way struct later -----------------


	/* search over every following command for seeking identical destination_names */
	if (current_command->itm->way.s_destination)
	{
		/* can we investigate over the following commands? */

		if (current_command->next)
		{
			/* loop over every destination sign of the current command, as far as there are not more than 10 entries. */
			destination_index = 0; /* Do only the first DEST_MAX_LOOPS_SIGN_TEXTS destination_signs */
			current_destination = current_command->itm->way.s_destination;

			while (current_destination && (destination_index < DEST_MAX_LOOPS_SIGN_TEXTS))
			{	/* initialize the search command */

				search_command = current_command->next;
				search_command_counter = 0; // Do only the first DEST_MAX_LOOPS_NAV_ITEMS commands.

				while (search_command && (search_command_counter < DEST_MAX_LOOPS_NAV_ITEMS))
				{
					if (search_command->itm)
					{	/* has the search command any destination_signs? */

						if (search_command->itm->way.s_destination)
						{
							search_destination = search_command->itm->way.s_destination;

							while (search_destination)
							{	/* Search this name in the destination list of the current command. */

								if (0 == strcmp(current_destination->destination, search_destination->destination))
								{	/* enter the destination_name in the investigation list*/

									destination_count[destination_index]++;
									//search_destination = NULL; /* break condition */
									break;
								}
								else
								{
									search_destination = search_destination->next;
								}
							}
						}
					}
					search_command_counter++;
					search_command = search_command->next;
				}

				destination_index++;
				current_destination = current_destination->next;
			}

			/* search for the best candidate */
			max_hits = 0;
			max_hit_index = 0;
			for (i = 0; i < destination_index; i++)
			{
				if (destination_count[i] > max_hits)
				{
					max_hits = destination_count[i];
					max_hit_index = i;
				}
			}
			/* jump to the corresponding destination_name */
			current_destination =  current_command->itm->way.s_destination;
			for (i = 0; i < max_hit_index; i++)
			{
				current_destination = current_destination->next;
			}
		}
		else if (current_command->itm->way.exit_label)
		{
			if ((current_nav_way) && (current_nav_way->street_dest_text))
			{
				g_free(current_nav_way->street_dest_text);
			}

			current_nav_way->street_dest_text = g_strdup(current_command->itm->way.exit_label);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "put exit_label into nav way (C) %s\n", current_command->itm->way.exit_label);
#endif

			return;
		}
	}
	else if (current_command->itm->way.exit_label)
	{
		if ((current_nav_way) && (current_nav_way->street_dest_text))
		{
			g_free(current_nav_way->street_dest_text);
		}

		current_nav_way->street_dest_text = g_strdup(current_command->itm->way.exit_label);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "put exit_label into nav way (A) %s\n", current_command->itm->way.exit_label);
#endif

		return;
	}





	/* return the best candidate, if there is any.*/
	if ((current_nav_way) && (current_nav_way->street_dest_text))
	{
		g_free(current_nav_way->street_dest_text);
		current_nav_way->street_dest_text = NULL;
	}

	if ((current_destination) && (current_destination->destination))
	{
		current_nav_way->street_dest_text = g_strdup(current_destination->destination);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "put destination into nav way (B) %s\n", current_nav_way->street_dest_text);
#endif
	}
	else
	{
		// stay NULL
		//current_nav_way->street_dest_text = NULL;
		//dbg(0, "put destination into nav way NULL (C)\n");
	}
}
// -- NEW 002 --




static struct navigation_itm *
navigation_itm_new(struct navigation *this_, struct item *ritem)
{
	struct navigation_itm *ret=g_new0(struct navigation_itm, 1);
	int i = 0;
	struct item *sitem;
	struct map *graph_map = NULL;
	struct attr street_item, direction, route_attr;
	struct map_rect *mr;
	struct attr attr;
	struct coord c[5];

	if (ritem)
	{
		ret->streetname_told = 0;

		if (ritem->type == type_street_route_waypoint)
		{
			dbg(0, "NAVR:XX:0:make street item\n");

			while (item_coord_get(ritem, &c[i], 1))
			{
				if (i < 4)
				{
					i++;
				}
				else
				{
					c[2] = c[3];
					c[3] = c[4];
				}
			}

			i--;
			ret->start = c[0];
			ret->end = c[i];

			if (item_attr_get(ritem, attr_direction, &direction))
			{
				if (direction.u.num < 0)
				{
					ret->way.dir = -99; // mark waypoint type
				}
				else
				{
					ret->way.dir = 99; // mark waypoint type
				}
			}
			else
			{
				ret->way.dir = 99; // mark waypoint type
			}

			navigation_itm_update(ret, ritem);
			ret->length = 1;
			ret->time = 1;
			ret->speed = 30;

			if (!this_->first)
			{
				this_->first = ret;
			}

			if (this_->last)
			{
				this_->last->next = ret;
				ret->prev = this_->last;
				//if (graph_map)
				//{
				//	navigation_itm_ways_update(ret, graph_map);
				//}
			}
			this_->last = ret;

			return ret;
		}

		if (!item_attr_get(ritem, attr_street_item, &street_item))
		{
			dbg(0, "NAVR:XX:1:no street item\n");
			g_free(ret);
			ret = NULL;
			return ret;
		}

		if (item_attr_get(ritem, attr_direction, &direction))
		{
			ret->way.dir = direction.u.num;
		}
		else
		{
			ret->way.dir = 0;
		}

		sitem = street_item.u.item;
		ret->way.item = *sitem;
		item_hash_insert(this_->hash, sitem, ret);
		mr = map_rect_new(sitem->map, NULL);

		// -- NEW 002 --
		struct map *tmap = sitem->map;  /*find better name for backup pointer to map*/
		// -- NEW 002 --

		// COST: 005
		dbg(0, "COST:005\n");
		if (!(sitem = map_rect_get_item_byid(mr, sitem->id_hi, sitem->id_lo)))
		{
			// -- NEW 002 --
			if (mr)
			{
				map_rect_destroy(mr);
			}
			g_free(ret);
			ret = NULL;
			// -- NEW 002 --

			return NULL;
		}

		// -- NEW 002 --
		if (item_attr_get(sitem, attr_flags, &attr))
		{
			ret->way.flags=attr.u.num;
		}
		// -- NEW 002 --

		if (item_attr_get(sitem, attr_street_name, &attr))
		{
			ret->way.name1 = map_convert_string(sitem->map, attr.u.str);
		}

		if (item_attr_get(sitem, attr_street_name_systematic, &attr))
		{
			ret->way.name2 = map_convert_string(sitem->map, attr.u.str);
		}

		navigation_itm_update(ret, ritem);

		while (item_coord_get(ritem, &c[i], 1))
		{
			if (i < 4)
			{
				i++;
			}
			else
			{
				c[2] = c[3];
				c[3] = c[4];
			}
		}

		i--;



		// -- NEW 002 --
		if (item_attr_get(sitem, attr_street_destination, &attr))
		{
			char *destination_raw;
			destination_raw = map_convert_string(sitem->map,attr.u.str);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "DEST::destination_raw=%s\n", destination_raw);
#endif

#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
			char *tt = g_strdup_printf("D:%s", attr.u.str);
			route_add_to_freetext_list(&c[0], tt);
			g_free(tt);
#endif

			// also save full text into "exit_label" --------------
			if (ret->way.exit_label)
			{
				g_free(ret->way.exit_label);
			}
			ret->way.exit_label = map_convert_string(sitem->map, destination_raw);
			// also save full text into "exit_label" --------------

			navigation_split_string_to_list(&(ret->way), destination_raw, ';');
			g_free(destination_raw);
		}
		else if (item_attr_get(sitem, attr_street_destination_lanes, &attr))
		{
			char *destination_raw;
			destination_raw = map_convert_string(sitem->map,attr.u.str);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "DEST::destination_lanes_raw=%s\n", destination_raw);
#endif

#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
			char *tt = g_strdup_printf("d_l:%s", attr.u.str);
			route_add_to_freetext_list(&c[0], tt);
			g_free(tt);
#endif

			// also save full text into "exit_label" --------------
			if (ret->way.exit_label)
			{
				g_free(ret->way.exit_label);
			}
			ret->way.exit_label = map_convert_string(sitem->map, destination_raw);
			// also save full text into "exit_label" --------------

			navigation_split_string_to_list_2(&(ret->way), destination_raw, ';', '|');
			g_free(destination_raw);
		}
		// -- NEW 002 --


		// ret->way.angle2 = road_angle_accurate(&c[0], &c[1], 0);		// angle at start of way
		// ret->angle_end = road_angle_accurate(&c[i - 1], &c[i], 0);	// angle at end of way
		ret->way.angle2 = road_angle(&c[0], &c[1], 0);		// angle at start of way
		ret->angle_end = road_angle(&c[i - 1], &c[i], 0);	// angle at end of way

		ret->start = c[0];
		ret->end = c[i];

		// -- NEW 002 --
		/*	If we have a ramp check the map for higway_exit info,
		 *  but only on the first node of the ramp.
		 *  Ramps with nodes in reverse order and oneway=-1 are not
		 *  specifically handled, but no occurence known so far either.
		 *  If present, obtain exit_ref, exit_label and exit_to
		 *  from the map.
		 *  exit_to holds info similar to attr_street_destination, and
		 *  we place it in way.s_destination as well, unless the street_destination info
		 *  is already present. In the future it will have to be skipped if destiantion:lanes
		 *	info exists as well.
		 *
		 *	Now it still holds a bug, if a ramp splits in 2, the exit_to info can end up on
		 *	both continuations of the ramp. Maybe this can be solved by passing the struct
		 *	navigation_maneuver up to here to help decide on exit_to.
		 *
		 */

		if (item_is_ramp(*sitem)) /* hier motorway_link en trunk_link toevoegen */
		{
			struct map_selection mselexit;
			struct item *rampitem;
			struct map_rect *mr2;
			struct coord exitcoord;

			mselexit.next = NULL;
			mselexit.u.c_rect.lu = c[0];
			mselexit.u.c_rect.rl = c[0];
			mselexit.range = item_range_all;
			mselexit.order = 18;

			mr2 = map_rect_new(tmap, &mselexit);

			while ((rampitem = map_rect_get_item(mr2)))
			{
				if (rampitem->type == type_highway_exit && item_coord_get(rampitem, &exitcoord, 1)
							&& exitcoord.x == c[0].x && exitcoord.y == c[0].y)
				{
					while (item_attr_get(rampitem, attr_any, &attr))
					{
						if (attr.type && attr.type == attr_label)
						{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
							dbg(0,"DEST::exit_label=%s\n",attr.u.str);
#endif
							if (ret->way.exit_label)
							{
								g_free(ret->way.exit_label);
							}
							ret->way.exit_label = map_convert_string(sitem->map,attr.u.str);

#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
							char *tt = g_strdup_printf("exit_label:%s", attr.u.str);
							route_add_to_freetext_list(&c[0], tt);
							g_free(tt);
#endif

						}

						if (attr.type == attr_ref)
						{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
							dbg(0,"DEST::exit_ref=%s\n",attr.u.str);
#endif
							if (ret->way.exit_ref)
							{
								g_free(ret->way.exit_ref);
							}
							ret->way.exit_ref = map_convert_string(sitem->map,attr.u.str);

#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
							char *tt = g_strdup_printf("exit_ref:%s", attr.u.str);
							route_add_to_freetext_list(&c[0], tt);
							g_free(tt);
#endif

						}

						if (attr.type == attr_exit_to)
						{
							if (attr.u.str && !ret->way.s_destination)
							{
								char *destination_raw;
								destination_raw = map_convert_string(sitem->map,attr.u.str);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
								dbg(0,"DEST::destination_raw from exit_to =%s\n",destination_raw);
#endif

								// also save full text into "exit_label" --------------
								if (ret->way.exit_label)
								{
									g_free(ret->way.exit_label);
								}
								ret->way.exit_label = map_convert_string(sitem->map, destination_raw);
								// also save full text into "exit_label" --------------

#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
								char *tt = g_strdup_printf("exit_to:%s", destination_raw);
								route_add_to_freetext_list(&c[0], tt);
								g_free(tt);
#endif

								if ((navigation_split_string_to_list(&(ret->way),destination_raw, ';')) < 2)
								{
									/*
									 * if a first try did not result in an actual splitting
									 * retry with ',' as a separator
									 *
									 * */
									navigation_split_string_to_list(&(ret->way),destination_raw, ',');
								}
								g_free(destination_raw);
							}
						}
					}
				}
			}

			if (mr2)
			{
				map_rect_destroy(mr2);
			}
		}
		// -- NEW 002 --












		item_attr_get(ritem, attr_route, &route_attr);
		graph_map = route_get_graph_map(route_attr.u.route);
		if (check_roundabout(ret, graph_map))
		{
			ret->way.flags |= NAVIT_AF_ROUNDABOUT;
		}

		// dbg(1, "i=%d start %d end %d '%s' '%s'\n", i, ret->way.angle2, ret->angle_end, ret->way.name1, ret->way.name2);
		map_rect_destroy(mr);
	}
	else
	{
		if (this_->last)
		{
			ret->start = ret->end = this_->last->end;
		}
	}

	if (!this_->first)
	{
		this_->first = ret;
	}

	if (this_->last)
	{
		this_->last->next = ret;
		ret->prev = this_->last;
		if (graph_map)
		{
			navigation_itm_ways_update(ret, graph_map);
		}
	}
	//dbg(1, "ret=%p\n", ret);
	this_->last = ret;
	return ret;
}

/**
 * @brief Counts how many times a driver could turn right/left 
 *
 * This function counts how many times the driver theoretically could
 * turn right/left between two navigation items, not counting the final
 * turn itself.
 *
 * @param from The navigation item which should form the start
 * @param to The navigation item which should form the end
 * @param direction Set to < 0 to count turns to the left >= 0 for turns to the right
 * @return The number of possibilities to turn or -1 on error
 */
static int count_possible_turns(struct navigation *nav, struct navigation_itm *from, struct navigation_itm *to, int direction)
{
	int count;
	struct navigation_itm *curr;
	struct navigation_way *w;

	if (direction == 0)
	{
		// we are going straight!!
		return -1;
	}

	count = 0;
	curr = from->next;

	int cur_next_is_lower_level_street = navigation_is_low_level_street(to->way.item.type);

	while (curr && (curr != to))
	{
		w = curr->way.next;

		while (w)
		{
			if (is_way_allowed(nav, w, 4))
			{

				// dont count lower level streets, if next turn is NOT also a lower level street
				if ((cur_next_is_lower_level_street == 1) || (navigation_is_low_level_street(w->item.type) == 0))
				{
					if (direction < 0)
					{
						if (angle_delta(curr->prev->angle_end, w->angle2) < 0)
						{
							count++;

#ifdef NAVIT_DEBUG_COORD_DIE2TE_LIST
// ------- DEBUG ---------
// ------- DEBUG ---------
// ------- DEBUG ---------
						if ((global_debug_coord_list_items + 2) > MAX_DEBUG_COORDS)
						{
							global_debug_coord_list_items = 0;
						}

						struct coord c2[5];

						if (navigation_get_real_item_first_coord(w, c2))
						{
							global_debug_coord_list[global_debug_coord_list_items].x = c2[0].x;
							global_debug_coord_list[global_debug_coord_list_items].y = c2[0].y;
							global_debug_coord_list_items++;
							global_debug_coord_list[global_debug_coord_list_items].x = c2[1].x;
							global_debug_coord_list[global_debug_coord_list_items].y = c2[1].y;
							global_debug_coord_list_items++;
						}
// ------- DEBUG ---------
// ------- DEBUG ---------
// ------- DEBUG ---------
#endif


							break;
						}
					}
					else if (direction > 0)
					{
						if (angle_delta(curr->prev->angle_end, w->angle2) > 0)
						{
							count++;
#ifdef NAVIT_DEBUG_COORD_DIE2TE_LIST
// ------- DEBUG ---------
// ------- DEBUG ---------
// ------- DEBUG ---------
						if ((global_debug_coord_list_items + 2) > MAX_DEBUG_COORDS)
						{
							global_debug_coord_list_items = 0;
						}

						struct coord c2[5];

						if (navigation_get_real_item_first_coord(w, c2))
						{
							global_debug_coord_list[global_debug_coord_list_items].x = c2[0].x;
							global_debug_coord_list[global_debug_coord_list_items].y = c2[0].y;
							global_debug_coord_list_items++;
							global_debug_coord_list[global_debug_coord_list_items].x = c2[1].x;
							global_debug_coord_list[global_debug_coord_list_items].y = c2[1].y;
							global_debug_coord_list_items++;
						}
// ------- DEBUG ---------
// ------- DEBUG ---------
// ------- DEBUG ---------
#endif

							break;
						}
					}
				}
			}
			w = w->next;
		}
		curr = curr->next;
	}

	if (!curr)
	{
		// from does not lead to to?
		return -1;
	}

	return count;
}

/**
 * @brief Calculates distance and time to the destination
 *
 * This function calculates the distance and the time to the destination of a
 * navigation. If incr is set, this is only calculated for the first navigation
 * item, which is a lot faster than re-calculation the whole destination, but works
 * only if the rest of the navigation already has been calculated.
 *
 * @param this_ The navigation whose destination / time should be calculated
 * @param incr Set this to true to only calculate the first item. See description.
 */
static void calculate_dest_distance(struct navigation *this_, int incr)
{
	int len = 0, time = 0, count = 0;
	struct navigation_itm *next = NULL;
	struct navigation_itm *itm = this_->last;

	//dbg(1, "enter this_=%p, incr=%d\n", this_, incr);

	if (incr)
	{
		if (itm)
		{
			//dbg(2, "old values: (%p) time=%d lenght=%d\n", itm,
			//		itm->dest_length, itm->dest_time);
		}
		else
		{
			//dbg(2, "old values: itm is null\n");
		}
		itm = this_->first;
		next = itm->next;
		//dbg(2, "itm values: time=%d lenght=%d\n", itm->length, itm->time);
		//dbg(2, "next values: (%p) time=%d lenght=%d\n", next, next->dest_length, next->dest_time);
		itm->dest_length = next->dest_length + itm->length;
		itm->dest_count = next->dest_count + 1;
		itm->dest_time = next->dest_time + itm->time;
		//dbg(2, "new values: time=%d lenght=%d\n", itm->dest_length, itm->dest_time);
		return;
	}

	while (itm)
	{
		len += itm->length;
		time += itm->time;
		itm->dest_length = len;
		itm->dest_time = time;
		itm->dest_count = count++;
		itm = itm->prev;
	}
	//dbg(1, "len %d time %d\n", len, time);
}

/**
 *
 * check it w1 is the same as w2 (duplicate way)
 *
 * @param w1 The first way to be checked
 * @param w2 The second way to be checked
 * @return 0 -> no, 1 -> yes:it's the same way
 *
 */
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

/**
 * @brief Checks if two navigation items are on the same street
 *
 * This function checks if two navigation items are on the same street. It returns
 * true if either their name or their "systematic name" (e.g. "A6" or "B256") are the
 * same.
 *
 * @param old The first item to be checked
 * @param new The second item to be checked
 * @return True if both old and new are on the same street
 */
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

#if 0
/**
 * @brief Checks if two navigation items are on the same street
 *
 * This function checks if two navigation items are on the same street. It returns
 * true if the first part of their "systematic name" is equal. If the "systematic name" is
 * for example "A352/E3" (a german highway which at the same time is part of the international
 * E-road network), it would only search for "A352" in the second item's systematic name.
 *
 * @param old The first item to be checked
 * @param new The second item to be checked
 * @return True if the "systematic name" of both items matches. See description.
 */
static int
is_same_street_systematic(struct navigation_itm *old, struct navigation_itm *new)
{
	int slashold,slashnew;
	if (!old->name2 || !new->name2)
	return 1;
	slashold=strcspn(old->name2, "/");
	slashnew=strcspn(new->name2, "/");
	if (slashold != slashnew || strncmp(old->name2, new->name2, slashold))
	return 0;
	return 1;
}

/**
 * @brief Check if there are multiple possibilities to drive from old
 *
 * This function checks, if there are multiple streets connected to the exit of "old".
 * Sometimes it happens that an item on a map is just segmented, without any other streets
 * being connected there, and it is not useful if navit creates a maneuver there.
 *
 * @param new The navigation item we're driving to
 * @return True if there are multiple streets
 */
static int
maneuver_multiple_streets(struct navigation_itm *new)
{
	if (new->way.next)
	{
		return 1;
	}
	else
	{
		return 0;
	}
}

/**
 * @brief Check if the new item is entered "straight"
 *
 * This function checks if the new item is entered "straight" from the old item, i.e. if there
 * is no other street one could take from the old item on with less steering.
 *
 * @param new The navigation item we're driving to
 * @param diff The absolute angle one needs to steer to drive to this item
 * @return True if the new item is entered "straight"
 */
static int
maneuver_straight(struct navigation_itm *new, int diff)
{
	int curr_diff;
	struct navigation_way *w;

	w = new->way.next;
	dbg(1,"diff=%d\n", diff);
	while (w)
	{
		curr_diff=abs(angle_delta(new->prev->angle_end, w->angle2));
		dbg(1,"curr_diff=%d\n", curr_diff);
		if (curr_diff < diff)
		{
			return 0;
		}
		w = w->next;
	}
	return 1;
}
#endif

static int maneuver_category(enum item_type type)
{
	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
		switch (type)
		{
			case type_cycleway:
				return 1;
			case type_footway:
				return 1;
			case type_street_service:
				return 1;
			case type_street_parking_lane:
				return 1;
			case type_living_street:
				return 1;
			case type_street_0:
				return 1;
			case type_street_1_city:
				return 2;
			case type_street_2_city:
			case type_ramp_street_2_city:
				return 3;
			case type_street_3_city:
			case type_ramp_street_3_city:
				return 4;
			case type_street_4_city:
			case type_ramp_street_4_city:
				return 5;
			case type_highway_city:
				return 7;
			case type_street_1_land:
				return 2;
			case type_street_2_land:
				return 3;
			case type_street_3_land:
				return 4;
			case type_street_4_land:
				return 5;
			case type_street_n_lanes:
				return 6;
			case type_ramp_highway_land:
				return 6;
			case type_highway_land:
				return 7;
			//case type_ramp:
			//	return 0;
			case type_roundabout:
				return 0;
			case type_ferry:
				return 0;
			default:
				return 0;
		}
	}
	else // car mode ---------------
	{
		switch (type)
		{
			case type_street_0:
				return 1;
			case type_street_1_city:
				return 2;
			case type_street_2_city:
			case type_ramp_street_2_city:
				return 3;
			case type_street_3_city:
			case type_ramp_street_3_city:
				return 4;
			case type_street_4_city:
			case type_ramp_street_4_city:
				return 5;
			case type_highway_city:
				return 7;
			case type_street_1_land:
				return 2;
			case type_street_2_land:
				return 3;
			case type_street_3_land:
				return 4;
			case type_street_4_land:
				return 5;
			case type_street_n_lanes:
				return 6;
			case type_ramp_highway_land:
				return 6;
			case type_highway_land:
				return 7;
			//case type_ramp:
			//	return 0;
			case type_roundabout:
				return 0;
			case type_ferry:
				return 0;
			default:
				return 0;
		}
	}
}





static int maneuver_category_calc2(enum item_type type)
{
	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
		switch (type)
		{
			case type_cycleway:
				return 1;
			case type_footway:
				return 1;
			case type_street_service:
				return 1;
			case type_street_parking_lane:
				return 1;
			case type_living_street:
				return 1;
			case type_street_0:
				return 1;
			case type_street_1_city:
				return 2;
			case type_street_2_city:
			case type_ramp_street_2_city:
				return 3;
			case type_street_3_city:
			case type_ramp_street_3_city:
				return 4;
			case type_street_4_city:
			case type_ramp_street_4_city:
				return 5;
			case type_highway_city:
				return 7;
			case type_street_1_land:
				return 2;
			case type_street_2_land:
				return 3;
			case type_street_3_land:
				return 4;
			case type_street_4_land:
				return 5;
			case type_street_n_lanes:
				return 6;
			case type_ramp_highway_land:
				return 6;
			case type_highway_land:
				return 7;
			//case type_ramp:
			//	return 0;
			case type_roundabout:
				return 0;
			case type_ferry:
				return 0;
			default:
				return 0;
		}
	}
	else
	{
		switch (type)
		{
			case type_street_0:
				return 1;
			case type_street_1_city:
				return 1;
			case type_street_2_city:
			case type_ramp_street_2_city:
				return 4;
			case type_street_3_city:
			case type_ramp_street_3_city:
				return 4;
			case type_street_4_city:
			case type_ramp_street_4_city:
				return 4;
			case type_highway_city:
				return 7;
			case type_street_1_land:
				return 1;
			case type_street_2_land:
				return 4;
			case type_street_3_land:
				return 4;
			case type_street_4_land:
				return 4;
			case type_street_n_lanes:
				return 6;
			case type_ramp_highway_land:
				return 6;
			case type_highway_land:
				return 7;
			//case type_ramp: // ramp need to be high enough
			//	return 4;
			case type_roundabout:
				return 0;
			case type_ferry:
				return 0;
			default:
				return 0;
		}
	}
}










static int is_way_allowed(struct navigation *nav, struct navigation_way *way, int mode)
{
	if (!nav->vehicleprofile)
	{
		return 1;
	}

	return !way->flags || ((way->flags & (way->dir >= 0 ? route_get_real_oneway_mask(way->flags, nav->vehicleprofile->flags_forward_mask) : route_get_real_oneway_mask(way->flags, nav->vehicleprofile->flags_reverse_mask))) == nav->vehicleprofile->flags);
}

/**
 * @brief Checks if navit has to create a maneuver to drive from old to new (run only once!!)
 *
 * This function checks if it has to create a "maneuver" - i.e. guide the user - to drive 
 * from "old" to "new".
 *
 * @param old The old navigation item, where we're coming from
 * @param new The new navigation item, where we're going to
 * @param delta The angle the user has to steer to navigate from old to new
 * @param reason A text string explaining how the return value resulted
 * @return True if navit should guide the user, false otherwise
 */
static int maneuver_required2(struct navigation *nav, struct navigation_itm *old, struct navigation_itm *new, int *delta, int *delta_real, char **reason)
{
	int ret = 0, d, dw, dlim;
	char *r = NULL;
	struct navigation_way *w = NULL;
	int cat, ncat, wcat, maxcat, left = -180, right = 180, is_unambigous = 0, is_same_street;
	int cat_2 = 0;
	int ncat_2 = 0;
	int highest_other_cat = 0;
	int original_d = 0;

	dbg(0, "STRAI:000:\n");
	dbg(0, "STRAI:000:======================================\n");

	// ---------------------------
	//
	// HINT: angle < 0 --> left
	//             > 0 --> right
	//             = 0 --> straight
	//
	// ---------------------------

	d = angle_delta(old->angle_end, new->way.angle2);
	original_d = d;

	//long long wayid_old = navigation_item_get_wayid(&(old->way));
	//long long wayid_new = navigation_item_get_wayid(&(new->way));
	//dbg(0, "Enter d=%d old->angle_end=%d new->way.angle2=%d old_way_id=%lld new_way_id=%lld\n", d, old->angle_end, new->way.angle2, wayid_old, wayid_new);

	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)) // bicycle mode
	{
		int flags_old = navigation_item_get_flags(&(old->way));
		int flags_new = navigation_item_get_flags(&(new->way));

		//dbg(0, "(b1)old flags=%x new flags=%x old dir=%d new dir=%d\n", old->way.flags, new->way.flags, old->way.dir, new->way.dir);
		//dbg(0, "(b2)old flags=%x new flags=%x\n", (flags_old & NAVIT_AF_ONEWAY), (flags_new & NAVIT_AF_ONEWAY));
		//dbg(0, "(b3)old flags=%x new flags=%x\n", (flags_old & NAVIT_AF_ONEWAYREV), (flags_new & NAVIT_AF_ONEWAYREV));

		if ((old->way.dir == 1) && ((new->way.dir == -1) && (new->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)))
		{
			r = "yes: bicycle starts going against oneway here (1)";
			tests_dbg(0, "yes: bicycle starts going against oneway here (1)");
			ret = 1;
			//dbg(0, "%s\n", r);
		}
		else if (         ((old->way.dir == -1) && (!(old->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)) )         && ((new->way.dir == -1) && (new->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)))
		{
			r = "yes: bicycle starts going against oneway here (2)";
			ret = 1;
			//dbg(0, "%s\n", r);
		}
	}




	// z2z2
	int have_more_than_one_way_to_turn = 0;
	int have_more_than_one_way_to_turn_cycleways = 0;
	struct navigation_way *w22;
	w22 = new->way.next;
	int new_angle_abs = 999;
	int new_angle_abs_min = 999;
	int new_angle_abs_min_allowed = 999;
	int new_angle_real = 999;
	int new_angle_min_allowed = 999;
	int new_angle_abs_min_ramp_allowed = 999;
	int old_angle_abs = abs(d);
	int new_angle_closest_to_cur = 999;
	int no_correction = 0;

	dbg(0, "STRAI:001:d=%d original_d=%d\n", old_angle_abs, original_d);

	while (w22)
	{

		dbg(0, "STRAI:002\n");

		//if ((w22->dir == -1) && (w22->flags & NAVIT_AF_ONEWAY))
		//{
		//	// against oneway not allowed
		//}
		if (((global_vehicle_profile != 1) && (global_vehicle_profile != 2)) // NOT bicycle mode
			&& (navigation_is_low_level_street(old->way.item.type) == 0)
			&& (navigation_is_low_level_street(new->way.item.type) == 0)
			&& (navigation_is_low_level_street(w22->item.type) == 1))
		{
			// dont count "lower" streets when not on "lower" street now or next
		}
		else
		{

			if ( (is_maybe_same_item(&(new->way), w22, 0) == 0)   &&  (is_maybe_same_item(&(old->way), w22, 1) == 0)   )
			{

				new_angle_real = angle_delta(old->angle_end, w22->angle2);
				new_angle_abs = abs(new_angle_real);
				if (new_angle_abs < new_angle_abs_min)
				{
					new_angle_abs_min = new_angle_abs;
				}

				if (is_way_allowed(nav, w22, 1))
				{
					have_more_than_one_way_to_turn = 1;

					if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)) // bicycle mode
					{
						if (w22->item.type == type_cycleway)
						{
							have_more_than_one_way_to_turn_cycleways = 1;
						}
					}

					if (maneuver_category_calc2(w22->item.type) > highest_other_cat)
					{
						highest_other_cat = maneuver_category_calc2(w22->item.type);
					}

					if (item_is_ramp(w22->item))
					{
						if (new_angle_abs < new_angle_abs_min_ramp_allowed)
						{
							new_angle_abs_min_ramp_allowed = new_angle_abs;
						}
					}

					if (abs(new_angle_real - d) < abs(new_angle_closest_to_cur))
					{
						new_angle_closest_to_cur = new_angle_real;
					}

					if (new_angle_abs < new_angle_abs_min_allowed)
					{
						new_angle_abs_min_allowed = new_angle_abs;
						new_angle_min_allowed = new_angle_real;
					}
				}

				dbg(0, "STRAI:003:new angle abs=%d min_allowed=%d have_more_than_one_way_to_turn=%d new_angle_closest_to_cur=%d d=%d\n", new_angle_abs, new_angle_abs_min_allowed, have_more_than_one_way_to_turn, new_angle_closest_to_cur, d);

			}
		}
		w22 = w22->next;
	}

	dbg(0, "STRAI:004 new_angle_abs_min=%d new_angle_abs_min_allowed=%d\n", new_angle_abs_min, new_angle_abs_min_allowed);

	if ((new_angle_abs_min_allowed > ROAD_ANGLE_IS_STRAIGHT_ABS) && (old_angle_abs <= ROAD_ANGLE_IS_STRAIGHT_ABS))
	{
		dbg(0, "STRAI:005 new_abs=%d old_abs=%d\n", new_angle_abs_min_allowed, old_angle_abs);
		tests_dbg(0, "we want to drive almost straight, set angle to 0");
		// we want to drive almost straight, set angle to "0"
		d = 0;
	}

	dbg(0, "STRAI:005a:d=%d\n", d);

	if (!r)
	{
		if (have_more_than_one_way_to_turn == 1) // more than 1 possibility
		{
			if (new->way.exit_label)
			{
				// highway to highway, nearest ramp is more than 15° angle turn
				if ( (cat > 6) && (ncat > 6) && (highest_other_cat <= 6) && (abs(d) < 12) && (new_angle_abs_min_ramp_allowed > 15) )
				{
					dbg(0, "STRAI:005a:d=%d\n", d);
					r = "no: driving almost straight on highway and no other highway possibilities (1)";
					tests_dbg(0, "no: driving almost straight on highway and no other highway possibilities (1)");
				}
				else
				{
					// r = "yes: we have an exit-sign to tell the user";
					// ret = 1;
				}
			}
			else if (new->way.s_destination)
			{
				if ( (cat > 6) && (ncat > 6) && (highest_other_cat <= 6) && (abs(d) < 12) && (new_angle_abs_min_ramp_allowed > 15) )
				{
					dbg(0, "STRAI:005a:d=%d\n", d);
					r = "no: driving almost straight on highway and no other highway possibilities (2)";
					tests_dbg(0, "no: driving almost straight on highway and no other highway possibilities (2)");
				}
				else
				{
					// r = "yes: we have a road-sign to tell the user";
					// ret = 1;
				}
			}
			else if (item_is_ramp(old->way.item))
			{
				r = "yes: we are currently on a ramp and have more than 1 road to take";
				tests_dbg(0, "yes: we are currently on a ramp and have more than 1 road to take");
				ret = 1;
			}
		}
	}


	if ((global_vehicle_profile != 1) && (global_vehicle_profile != 2))
	{
		if (!new->way.next)
		{
			/* No announcement necessary */
			r = "no: Only one possibility";
			tests_dbg(0, "no: Only one possibility");
		}
		else if (!new->way.next->next && item_is_ramp(new->way.next->item) && !is_way_allowed(nav, new->way.next, 1))
		{
			/* If the other way is only a ramp and it is one-way in the wrong direction, no announcement necessary */
			r = "no: Only ramp";
			tests_dbg(0, "no: Only ramp");
		}
	}
	else // bicycle mode --------------------
	{
		if (!r)
		{
#if 0
			if ((!new->way.next) && (abs(d) < 20))
			{
				/* No announcement necessary */
				r = "no: Only one possibility and less than 20° turn";
				tests_dbg(0, "no: Only one possibility and less than 20° turn");
				dbg(0, "%s\n", r);
			}
			else
			{
				if (abs(d) > 2)
				{
					r = "yes: bicycle mode";
					dbg(0, "%s\n", r);
					ret = 1;
				}
				else
				{
					r = "no: less than 3° turn";
					dbg(0, "%s\n", r);
				}
			}
#endif

#if 0
			if (!new->way.next)
			{
				/* No announcement necessary */
				r = "no: Only one possibility";
			}
#endif

#if 1

			if ((old->way.item.type == type_cycleway) && (new->way.item.type == type_cycleway))
			{
				if (have_more_than_one_way_to_turn_cycleways == 0)
				{
					if (abs(d) > ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC)
					{
						r = "yes: delta over ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC in bicycle mode (Only one possibility cyc-2-cyc)";
						//dbg(0, "%s\n", r);
						ret = 1;
					}
					else
					{
						r = "no: delta less than ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC in bicycle mode (Only one possibility cyc-2-cyc)";
						//dbg(0, "%s\n", r);
						ret = 0;
					}
				}
				else
				{
					if (abs(d) > ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC__2)
					{
						r = "yes:  delta over ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC__2 in bicycle mode (cyc-2cyc)";
						//dbg(0, "%s\n", r);
						ret = 1;
					}
					else
					{
						r = "no:  delta less than ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC__2 in bicycle mode (cyc-2cyc)";
						//dbg(0, "%s\n", r);
						ret = 0;
					}
				}
			}
			else
			{

				if (!new->way.next)
				{
					if (abs(d) > ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE)
					{
						r = "yes: delta over ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE in bicycle mode (Only one possibility)";
						//dbg(0, "%s\n", r);
						ret = 1;
					}
					else
					{
						r = "no: delta less than ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE in bicycle mode (Only one possibility)";
						//dbg(0, "%s\n", r);
						ret = 0;
					}
				}
				else
				{
					if (abs(d) > ROAD_ANGLE_MIN__FOR_TURN_BICYCLEMODE_ONLY_1_POSSIBILITY)
					{
						r = "yes:  delta over ROAD_ANGLE_MIN__FOR_TURN_BICYCLEMODE_ONLY_1_POSSIBILITY in bicycle mode";
						//dbg(0, "%s\n", r);
						ret = 1;
					}
					else
					{
						r = "no:  delta less than ROAD_ANGLE_MIN__FOR_TURN_BICYCLEMODE_ONLY_1_POSSIBILITY in bicycle mode";
						//dbg(0, "%s\n", r);
						ret = 0;
					}
				}

			}
#endif

		}
	}

	if (!r)
	{
		if ((old->way.flags & NAVIT_AF_ROUNDABOUT) && !(new->way.flags & NAVIT_AF_ROUNDABOUT))
		{
			r = "yes: leaving roundabout";
			tests_dbg(0, "yes: leaving roundabout");
			ret = 1;
		}
		else if (!(old->way.flags & NAVIT_AF_ROUNDABOUT) && (new->way.flags & NAVIT_AF_ROUNDABOUT))
		{
			r = "no: entering roundabout";
			tests_dbg(0, "no: entering roundabout");
		}
		else if ((old->way.flags & NAVIT_AF_ROUNDABOUT) && (new->way.flags & NAVIT_AF_ROUNDABOUT))
		{
			r = "no: staying in roundabout";
			tests_dbg(0, "no: staying in roundabout");
		}
	}

	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
		if (!r && abs(d) > ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE)
		{
			/* always make an announcement if you have to make a turn */
			r = "yes: delta over ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE in bicycle mode";
			//dbg(0, "%s\n", r);
			ret = 1;
		}
	}
	else // car mode ---------------------
	{
		if (!r && abs(d) > 75)
		{
			/* always make an announcement if you have to make a sharp turn */
			r = "yes: delta over 75";
			tests_dbg(0, "yes: delta over 75");
			ret = 1;
		}
	}

	cat = maneuver_category(old->way.item.type);
	ncat = maneuver_category(new->way.item.type);

	cat_2 = maneuver_category_calc2(old->way.item.type);
	ncat_2 = maneuver_category_calc2(new->way.item.type);


	if (!r)
	{
		/* Check whether the street keeps its name */
		is_same_street = is_same_street2(old->way.name1, old->way.name2, new->way.name1, new->way.name2);

		dbg(0, "STRAI:011.01 is_same_street=%d old->way.name1=%s old->way.name2=%s new->way.name1=%s new->way.name2=%s\n", is_same_street, old->way.name1, old->way.name2, new->way.name1, new->way.name2);

		w = new->way.next;
		maxcat = -1;
		while (w)
		{
			if ( (is_maybe_same_item(&(new->way), w, 0) == 0)   &&  (is_maybe_same_item(&(old->way), w, 1) == 0)   )
			{
				dw = angle_delta(old->angle_end, w->angle2);
				dbg(0, "STRAI:011.02 dw=%d l=%d r=%d\n", dw, left, right);

				if (dw < 0)
				{
					if (dw > left)
					{
						left = dw;
					}
				}
				else
				{
					if (dw < right)
					{
						right = dw;
					}
				}

				wcat = maneuver_category(w->item.type);
				dbg(0, "STRAI:011.03 wcat=%d\n", wcat);

				/* If any other street has the same name [ removed:"but isn't a highway (a highway might split up temporarily)" ], then
				 we can't use the same name criterium  */
				// if (is_same_street && is_same_street2(old->way.name1, old->way.name2, w->name1, w->name2) && (cat != 7 || wcat != 7) && is_way_allowed(nav, w, 2))
				if (is_same_street && is_same_street2(old->way.name1, old->way.name2, w->name1, w->name2) && is_way_allowed(nav, w, 2))
				{
					is_same_street = 0;
					dbg(0, "STRAI:011.04 is_same_street=%d\n", is_same_street);
				}

				/* Mark if the street has a higher or the same category */
				if (wcat > maxcat)
				{
					maxcat = wcat;
					dbg(0, "STRAI:011.06 maxcat=%d wcat=%d\n", maxcat, wcat);
				}

			}

			w = w->next;
		}

		if (have_more_than_one_way_to_turn == 1) // more than 1 possibility
		{
			/* Even if the ramp has the same name, announce it */
			if (item_is_ramp(new->way.item) && !item_is_ramp(old->way.item))
			{
				is_same_street = 0;
				dbg(0, "STRAI:011.05.xx is_same_street=%d\n", is_same_street);
			}
		}

		/* get the delta limit for checking for other streets. It is lower if the street has no other
		 streets of the same or higher category */
		if (ncat < cat)
		{
			dlim = 80;
		}
		else
		{
			dlim = 120;
		}

		/* if the street is really straight, the others might be closer to straight */
		if (abs(d) < 20)
		{
			dlim /= 2;
		}

		if ((maxcat == ncat && maxcat == cat) || (ncat == 0 && cat == 0))
		{
			dlim = abs(d) * (620 / 256); // abs(d) * 2.4
		}
		else if (maxcat < ncat && maxcat < cat)
		{
			dlim = abs(d) * (128 / 256); // abs(d) * 0.5
		}

		if (left < -dlim && right > dlim) // no other road is between -dlim : dlim angle (no other road is "dlim" close to straight)
		{
			is_unambigous = 1;
		}

		if (!is_same_street && is_unambigous < 1)
		{
			ret = 1;
			r = "yes: (not same street) or (ambigous [nicht eindeutig])";
			tests_dbg(0, "yes: (not same street) or (ambigous [nicht eindeutig])");
		}
		else
		{
			r = "no: (same street) and (unambigous [eindeutig])";
			tests_dbg(0, "no: (same street) and (unambigous [eindeutig])");
		}

		if (ret == 0)
		{
			// add a new check here:
			if (have_more_than_one_way_to_turn == 1)
			{

				dbg(0, "STRAI:11.07:4.0: cat=%d, ncat=%d, highest_other_cat=%d, d=%d, abs(d)=%d new_angle_closest_to_cur=%d original_d=%d\n", cat, ncat, highest_other_cat, d, abs(d), new_angle_closest_to_cur, original_d);

				if ( (cat > 6) && (ncat > 6) && (highest_other_cat <= 6) && (abs(d) < 70) )
				{
					r = "no: from highway to highway (no other highway possibilities)";
					tests_dbg(0, "no: from highway to highway (no other highway possibilities)");
					dbg(0, "STRAI:011.07:4 abs(d)=%d cat=%d ncat=%d highest_other_cat=%d\n", abs(d), cat, ncat, highest_other_cat);
				}
				else
				{

					if ((ncat == 6) && (highest_other_cat == 6) && (abs(d) < 50) && (abs(new_angle_closest_to_cur - original_d) < 65))
					{
						ret = 1;
						r = "yes: we are driving onto a ramp and there a other ramps near (<50 degrees) to it";
						tests_dbg(0, "yes: we are driving onto a ramp and there a other ramps near (<50 degrees) to it");
						dbg(0, "STRAI:011.07:3.001 ncat=%d highest_other_cat=%d d=%d (new_angle_closest_to_cur=%d - original_d=%d)\n", ncat, highest_other_cat, d, new_angle_closest_to_cur, original_d);
					}


					if ((d == 0) && (new_angle_abs_min_allowed >= 25))
					{
						r = "no: driving almost straight, and other ways not very close to straight";
						tests_dbg(0, "no: driving almost straight, and other ways not very close to straight");
						dbg(0, "STRAI:011.07:3 abs(d)=%d new_angle_abs_min_allowed=%d\n", abs(d), new_angle_abs_min_allowed);
					}
					else
					{

						if ( (d == 0) && (abs(new_angle_closest_to_cur - original_d) < 16) )
						{
							ret = 1;
							r = "yes: we are going straight and some other way is very close to it";
							tests_dbg(0, "yes: we are going straight and some other way is very close to it");
							dbg(0, "STRAI:011.07:0 abs(d)=%d new_angle_abs_min_allowed=%d\n", abs(d), new_angle_abs_min_allowed);
						}
						else if ( (ncat_2 <= highest_other_cat) && (d == 0) && (abs(new_angle_closest_to_cur - original_d) < 30) )
						{
							ret = 1;
							r = "yes: we are going straight and some other way is very close to it (same or higher cat)";
							tests_dbg(0, "yes: we are going straight and some other way is very close to it (same or higher cat)");"
							dbg(0, "STRAI:011.07:7 abs(d)=%d new_angle_abs_min_allowed=%d\n", abs(d), new_angle_abs_min_allowed);
						}
						else if ((abs(d) > 0) && (new_angle_abs_min_allowed < abs(d)))
						{
							ret = 1;
							r = "yes: some other way is going more straight";
							tests_dbg(0, "yes: some other way is going more straight");
							dbg(0, "STRAI:011.07:0 abs(d)=%d new_angle_abs_min_allowed=%d\n", abs(d), new_angle_abs_min_allowed);


							if (abs(d) < 10)
							{

							// ----------########### more way left/right of way ? ###########----------
							int more_ways_to_left_ = 0;
							int more_ways_to_right_ = 0;
							w = new->way.next;
							while (w)
							{
								if (is_way_allowed(nav, w, 1))
								{
									if ( (is_maybe_same_item(&(new->way), w, 0) == 0)   &&  (is_maybe_same_item(&(old->way), w, 1) == 0)   )
									{
										dbg(0, "STRAI:108.02a delta=%d\n", angle_delta(old->angle_end, w->angle2));

										if (is_way_allowed(nav, w, 1))
										{
											if (angle_delta(old->angle_end, w->angle2) < d) // other ways are going more to the left?
											{
												more_ways_to_left_++;
											}
											else if (angle_delta(old->angle_end, w->angle2) > d) // other ways are going more to the right?
											{
												more_ways_to_right_++;
											}
										}
									}
								}
								w = w->next;
							}

							dbg(0, "STRAI:108.02 %d %d\n", more_ways_to_left_, more_ways_to_right_);


							if ((d < 0) && (more_ways_to_left_ == 0)) // && (more_ways_to_right_ > 0))
							{

								dbg(0, "STRAI:108.03:left\n");
								*delta_real = 0;
								d = -8;
								no_correction = 1;
							}
							else if ((d > 0) && (more_ways_to_left_ > 0)) // && (more_ways_to_right_ == 0))
							{
								dbg(0, "STRAI:108.04:right\n");
								*delta_real = 0;
								d = 8;
								no_correction = 1;
							}



							// ----------########### more way left/right of way ? ###########----------

							}


						}
						else if ((abs(d) > 0) && (new_angle_abs_min_allowed < 39) && (is_same_street))
						{
							if ( (cat == ncat) && (ncat_2 > highest_other_cat) )
							{
								r = "no: we need to make a turn, but other possibilites are much lower cat roads";
								tests_dbg(0, "no: we need to make a turn, but other possibilites are much lower cat roads");
								dbg(0, "STRAI:011.07:5iss cat=%d ncat=%d cat_2=%d ncat_2=%d highest_other_cat=%d\n", cat, ncat, cat_2, ncat_2, highest_other_cat);
							}
							else
							{
								ret = 1;
								r = "yes: we need to make a turn";
								tests_dbg(0, "yes: we need to make a turn");
								dbg(0, "STRAI:011.07:1iss abs(d)=%d new_angle_abs_min_allowed=%d\n", abs(d), new_angle_abs_min_allowed);
							}
						}
						else if ((abs(d) > 0) && (new_angle_abs_min_allowed < 52) && (!is_same_street))
						{
							if ( (cat == ncat) && (ncat_2 > highest_other_cat) )
							{
								r = "no: we need to make a turn, but other possibilites are much lower cat roads";
								tests_dbg(0, "no: we need to make a turn, but other possibilites are much lower cat roads");
								dbg(0, "STRAI:011.07:5nss cat=%d ncat=%d cat_2=%d ncat_2=%d highest_other_cat=%d\n", cat, ncat, cat_2, ncat_2, highest_other_cat);
							}
							else
							{
								ret = 1;
								r = "yes: we need to make a turn";
								tests_dbg(0, "yes: we need to make a turn");
								dbg(0, "STRAI:011.07:1nss abs(d)=%d new_angle_abs_min_allowed=%d\n", abs(d), new_angle_abs_min_allowed);
							}
						}
						else
						{
							dbg(0, "STRAI:011.07:6 abs(d)=%d new_angle_abs_min_allowed=%d cat=%d ncat=%d cat_2=%d ncat_2=%d highest_other_cat=%d new_angle_closest_to_cur=%d original_d=%d\n", abs(d), new_angle_abs_min_allowed, cat, ncat, cat_2, ncat_2, highest_other_cat, new_angle_closest_to_cur, original_d);
						}
					}
				}
			}
		}

		dbg(0, "STRAI:011.07 is_unambigous[eindeutig]=%d ret=%d r=%s\n", is_unambigous, ret, r);
		tests_dbg(0, "STRAI:011.07 is_unambigous[eindeutig]=%d ret=%d r=%s", is_unambigous, ret, r);

#ifdef DEBUG
		// r=g_strdup_printf("yes: d %d left %d right %d dlim=%d cat old:%d new:%d max:%d unambigous=%d same_street=%d", d, left, right, dlim, cat, ncat, maxcat, is_unambigous, is_same_street);
#endif
	}






		dbg(0, "STRAI:007.00aa:d=%d ******************++++++++++++\n", d);





	// correct "delta" (turn angle) here !! ---------------------------
	// correct "delta" (turn angle) here !! ---------------------------

	if (no_correction == 0)
	{
		*delta_real = d;
	}

	if ((ret == 1) && (have_more_than_one_way_to_turn == 1) && (no_correction == 0))
	{
		w = new->way.next;
		//int d2 = angle_delta(old->angle_end, new->way.angle2);
		int d2 = d;
		//if (d == 0)
		//{
		//	d2 = 0;
		//}

		int correct_direction = 1;

		dbg(0, "STRAI:007.01:d=%d d2=%d\n", d, d2);

		if (d2 < 0) // left
		{
			while (w)
			{
				if (is_way_allowed(nav, w, 1))
				{
					if ( (is_maybe_same_item(&(new->way), w, 0) == 0)   &&  (is_maybe_same_item(&(old->way), w, 1) == 0)   )
					{
						if (angle_delta(old->angle_end, w->angle2) > d2) // other ways are going more to the right?
						{
							correct_direction = 0;
							break;
						}
					}
				}
				w = w->next;
			}

			if (correct_direction == 1)
			{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				dbg(0, "MAV:001:correct to right\n");
#endif
				d = 8; // set to "slight right"
			}
		}
		else if (d2 > 0) // right
		{
			while (w)
			{
				if (is_way_allowed(nav, w, 1))
				{
					if ( (is_maybe_same_item(&(new->way), w, 0) == 0)   &&  (is_maybe_same_item(&(old->way), w, 1) == 0)   )
					{
						if (angle_delta(old->angle_end, w->angle2) < d2) // other ways are going more to the left?
						{
							correct_direction = 0;
							break;
						}
					}
				}
				w = w->next;
			}

			if (correct_direction == 1)
			{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				dbg(0, "MAV:002:correct to left\n");
#endif
				d = -8; // set to "slight left"
			}
		}
		else // (d2 == 0) // straight
		{

			dbg(0, "STRAI:008.01:%d (%d < %d) new_angle_abs_min_allowed=%d\n", d2, new_angle_abs_min, ROAD_ANGLE_DISTANCE_FOR_STRAIGHT, new_angle_abs_min_allowed);

			int more_ways_to_left = 0;
			int more_ways_to_right = 0;

			if (new_angle_abs_min_allowed < ROAD_ANGLE_DISTANCE_FOR_STRAIGHT) // if other angles are far different from straight, than let it still be straight! otherwise correct direction
			{
				dbg(0, "STRAI:008.02\n");


				if ((abs(new_angle_closest_to_cur - original_d) <= 25) || (abs(original_d) >= 10))
				// if (1 == 1)
				{

					while (w)
					{
						if (is_way_allowed(nav, w, 1))
						{
							if ( (is_maybe_same_item(&(new->way), w, 0) == 0)   &&  (is_maybe_same_item(&(old->way), w, 1) == 0)   )
							{
								dbg(0, "STRAI:008.02a delta=%d\n", angle_delta(old->angle_end, w->angle2));

								if (is_way_allowed(nav, w, 1))
								{
									if (angle_delta(old->angle_end, w->angle2) < d2) // other ways are going more to the left?
									{
										more_ways_to_left++;
									}
									else if (angle_delta(old->angle_end, w->angle2) > d2) // other ways are going more to the right?
									{
										more_ways_to_right++;
									}
								}
							}
						}
						w = w->next;
					}

					dbg(0, "STRAI:008.02 %d %d\n", more_ways_to_left, more_ways_to_right);

					if ((more_ways_to_left == 0) && (more_ways_to_right > 0))
					{

						dbg(0, "STRAI:008.03:left\n");

#ifdef NAVIT_ROUTING_DEBUG_PRINT
						dbg(0, "MAV:003:correct to left\n");
#endif
						d = -8;
					}
					else if ((more_ways_to_left > 0) && (more_ways_to_right == 0))
					{
						dbg(0, "STRAI:008.04:right\n");

#ifdef NAVIT_ROUTING_DEBUG_PRINT
						dbg(0, "MAV:003:correct to right\n");
#endif
						d = 8;
					}

				}
				else
				{
					dbg(0, "STRAI:008.02f street almost straight and nearest other street at least 25 away: closest=%d orig_d=%d\n", new_angle_closest_to_cur, original_d);
				}

			}

		}

	}

	// correct "delta" (turn angle) here !! ---------------------------
	// correct "delta" (turn angle) here !! ---------------------------








	dbg(0, "STRAI:099:ret=%d r=%s d=%d d_real=%d\n", ret, r, d, *delta_real);
	dbg(0, "STRAI:099:======================================\n");

	*delta = d;
	if (reason)
	{
		*reason = r;
	}

	return ret;
}

static struct navigation_command *command_new(struct navigation *this_, struct navigation_itm *itm, int delta, int delta_real)
{
	struct navigation_command *ret=g_new0(struct navigation_command, 1);

	//dbg(1, "enter this_=%p itm=%p delta=%d\n", this_, itm, delta);
	ret->delta = delta;
	ret->delta_real = delta_real;
	ret->itm = itm;

	if (itm && itm->prev && itm->way.next && itm->prev->way.next && !(itm->way.flags & NAVIT_AF_ROUNDABOUT) && (itm->prev->way.flags & NAVIT_AF_ROUNDABOUT))
	{
		int len = 0;
		int angle = 0;
		int entry_angle;
		struct navigation_itm *itm2 = itm->prev;
		int exit_angle = angle_median(itm->prev->angle_end, itm->way.next->angle2);
		//dbg(1, "exit %d median from %d,%d\n", exit_angle, itm->prev->angle_end, itm->way.next->angle2);

		while (itm2 && (itm2->way.flags & NAVIT_AF_ROUNDABOUT))
		{
			len += itm2->length;
			angle = itm2->angle_end;
			itm2 = itm2->prev;
		}

		if (itm2 && itm2->next && itm2->next->way.next)
		{
			itm2 = itm2->next;
			entry_angle = angle_median(angle_opposite(itm2->way.angle2), itm2->way.next->angle2);
			// dbg(1, "entry %d median from %d(%d),%d\n", entry_angle, angle_opposite(itm2->way.angle2), itm2->way.angle2, itm2->way.next->angle2);
		}
		else
		{
			entry_angle = angle_opposite(angle);
		}
		//dbg(0, "entry %d exit %d\n", entry_angle, exit_angle);
		ret->roundabout_delta = angle_delta(entry_angle, exit_angle);
		ret->length = len + roundabout_extra_length;
	}

	if (this_->cmd_last)
	{
		this_->cmd_last->next = ret;
		ret->prev = this_->cmd_last;
	}

	this_->cmd_last = ret;

	if (!this_->cmd_first)
	{
		this_->cmd_first = ret;
	}

	return ret;
}



// ----------- main place where maneuvers generated (run only once) ------------
// ----------- main place where maneuvers generated (run only once) ------------
// ----------- main place where maneuvers generated (run only once) ------------
static void make_maneuvers(struct navigation *this_, struct route *route)
{
__F_START__

	struct navigation_itm *itm, *last = NULL, *last_itm = NULL;
	int delta;
	int delta_real;
	itm = this_->first;
	this_->cmd_last = NULL;
	this_->cmd_first = NULL;
	while (itm)
	{
		if (last)
		{
			if (maneuver_required2(this_, last_itm, itm, &delta, &delta_real, NULL))
			{
				//dbg(0, "maneuver_required2 = true\n");
				command_new(this_, itm, delta, delta_real);
			}
		}
		else
		{
			last = itm;
		}
		last_itm = itm;
		itm = itm->next;
	}
	command_new(this_, last_itm, 0, 0);

__F_END__
}
// ----------- main place where maneuvers generated ------------
// ----------- main place where maneuvers generated ------------
// ----------- main place where maneuvers generated ------------



static int contains_suffix(char *name, char *suffix)
{
	if (!suffix)
	{
		return 0;
	}

	if (strlen(name) < strlen(suffix))
	{
		return 0;
	}

	return !g_strcasecmp(name + strlen(name) - strlen(suffix), suffix);
}

static char *
replace_suffix(char *name, char *search, char *replace)
{
	int len = strlen(name) - strlen(search);
	char *ret = g_malloc(len + strlen(replace) + 1);

	strncpy(ret, name, len);
	strcpy(ret + len, replace);

	if (isupper(name[len]))
	{
		ret[len] = toupper(ret[len]);
	}

	return ret;
}

static char *
navigation_item_destination(struct navigation *nav, struct navigation_itm *itm, struct navigation_itm *next, char *prefix)
{
	char *ret = NULL, *name1, *sep, *name2;
	char *n1, *n2;
	int i, sex;
	int vocabulary1 = 65535;
	int vocabulary2 = 65535;
	struct attr attr;

	if (!prefix)
	{
		prefix = "";
	}

	if (nav->speech && speech_get_attr(nav->speech, attr_vocabulary_name, &attr, NULL))
	{
		vocabulary1 = attr.u.num;
	}

	if (nav->speech && speech_get_attr(nav->speech, attr_vocabulary_name_systematic, &attr, NULL))
	{
		vocabulary2 = attr.u.num;
	}

	n1 = itm->way.name1;
	n2 = itm->way.name2;

	if (!vocabulary1)
		n1 = NULL;

	if (!vocabulary2)
		n2 = NULL;

	if (!n1 && !n2 && (item_is_ramp(itm->way.item)) && vocabulary2)
	{
		//dbg(1,">> Next is ramp %lx current is %lx \n", itm->way.item.type, next->way.item.type);


// disabled for now !!!!!!!! -------------------------
#if 0
		if (next->way.item.type == type_ramp)
		{
			return NULL;
		}
#endif
// disabled for now !!!!!!!! -------------------------


		//else
		//{
		//	return g_strdup_printf("%s%s",prefix,_("into the ramp"));
		//}

#if 0
		if (itm->way.item.type == type_highway_city || itm->way.item.type == type_highway_land)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:exit\n");
			gchar* xy=g_strdup_printf("+*#1:%s\n", prefix);
			android_send_generic_text(1,xy);
			g_free(xy);
#endif
#endif
			return g_strdup_printf("%s%s", prefix, _("exit")); /* %FIXME Can this even be reached?  and "exit" is the wrong text anyway ! */
		}
		else
#endif
		{
			if (itm->way.street_dest_text)
			{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				gchar* xy=g_strdup_printf("+*#0:%s\n", itm->way.street_dest_text);
				android_send_generic_text(1,xy);
				g_free(xy);
				gchar* xy=g_strdup_printf("+*#1:%s\n", prefix);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif

				// say the name on the exit sign/destinaion sign
				return g_strdup_printf("%s%s", prefix, itm->way.street_dest_text);
			}
			else
			{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:into the ramp\n");
				gchar* xy=g_strdup_printf("+*#1:%s\n", prefix);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif

				return g_strdup_printf("%s%s", prefix, _("into the ramp"));
			}
		}

	}

	if (!n1 && !n2 && !itm->way.street_dest_text)
	{
		return NULL;
	}

	if (itm->way.street_dest_text)
	{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
		gchar* xy=g_strdup_printf("+*#0:%s\n", itm->way.street_dest_text);
		android_send_generic_text(1,xy);
		g_free(xy);
		gchar* xy=g_strdup_printf("+*#1:%s\n", prefix);
		android_send_generic_text(1,xy);
		g_free(xy);
#endif
#endif
		// say the name on the exit sign/destinaion sign
		return g_strdup_printf("%s%s", prefix, itm->way.street_dest_text);
	}
	else if (n1)
	{
		sex = -1;
		name1 = NULL;
		for (i = 0; i < sizeof(suffixes) / sizeof(suffixes[0]); i++)
		{
			if (contains_suffix(n1, suffixes[i].fullname))
			{
				sex = suffixes[i].sex;
				name1 = g_strdup(n1);
				break;
			}
			if (contains_suffix(n1, suffixes[i].abbrev))
			{
				sex = suffixes[i].sex;

				// TODO: replacing strings here maybe not so good?!?
				name1 = replace_suffix(n1, suffixes[i].abbrev, suffixes[i].fullname);
				break;
			}
		}

		if (n2)
		{
			name2 = n2;
			sep = " ";
		}
		else
		{
			name2 = "";
			sep = "";
		}

		gchar* xy;
		switch (sex)
		{
			case -1:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:%sinto the street %s%s%s\n");
				xy=g_strdup_printf("+*#1:%s\n", prefix);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", n1);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", sep);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", name2);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: Arguments: 1: Prefix (Space if required) 2: Street Name 3: Separator (Space if required), 4: Systematic Street Name
				ret = g_strdup_printf(_("%sinto the street %s%s%s"), prefix, n1, sep, name2);
				break;
			case 1:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:%sinto the %s%s%s|male form\n");
				xy=g_strdup_printf("+*#1:%s\n", prefix);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", name1);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", sep);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", name2);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: Arguments: 1: Prefix (Space if required) 2: Street Name 3: Separator (Space if required), 4: Systematic Street Name. Male form. The stuff after | doesn't have to be included
				ret = g_strdup_printf(_("%sinto the %s%s%s|male form"), prefix, name1, sep, name2);
				break;
			case 2:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:%sinto the %s%s%s|female form\n");
				xy=g_strdup_printf("+*#1:%s\n", prefix);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", name1);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", sep);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", name2);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: Arguments: 1: Prefix (Space if required) 2: Street Name 3: Separator (Space if required), 4: Systematic Street Name. Female form. The stuff after | doesn't have to be included
				ret = g_strdup_printf(_("%sinto the %s%s%s|female form"), prefix, name1, sep, name2);
				break;
			case 3:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:%sinto the %s%s%s|neutral form\n");
				xy=g_strdup_printf("+*#1:%s\n", prefix);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", name1);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", sep);
				android_send_generic_text(1,xy);
				g_free(xy);
				xy=g_strdup_printf("+*#1:%s\n", name2);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: Arguments: 1: Prefix (Space if required) 2: Street Name 3: Separator (Space if required), 4: Systematic Street Name. Neutral form. The stuff after | doesn't have to be included
				ret = g_strdup_printf(_("%sinto the %s%s%s|neutral form"), prefix, name1, sep, name2);
				break;
		}
		g_free(name1);

	}
	else
	{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
		android_send_generic_text(1,"+*#O:sinto the %s\n");
		gchar* xy=g_strdup_printf("+*#1:%s\n", prefix);
		android_send_generic_text(1,xy);
		g_free(xy);
		xy=g_strdup_printf("+*#1:%s\n", n2);
		android_send_generic_text(1,xy);
		g_free(xy);
#endif
#endif
		// TRANSLATORS: gives the name of the next road to turn into (into the E17)
		ret = g_strdup_printf(_("%sinto the %s"), prefix, n2);
	}

	name1 = ret;

	while (name1 && *name1)
	{
		switch (*name1)
		{
			case '|':
				*name1 = '\0';
				break;
			case '/':
				*name1++ = ' ';
				break;
			default:
				name1++;
		}
	}

	return ret;
}




#define DONT_KNOW_LEVEL -997


static char *
show_maneuver_bicycle(struct navigation *nav, struct navigation_itm *itm, struct navigation_command *cmd, enum attr_type type, int connect, int want_this_level)
{
__F_START__

	// TRANSLATORS: right, as in 'Turn right'
	char *dir = _("right");
	char *strength = "";
	int distance = itm->dest_length - cmd->itm->dest_length;
	char *d, *ret = NULL;
	int delta = cmd->delta;
	int delta_real = cmd->delta_real;
	int level;
	int level_now = 99;
	int level3;
	int strength_needed;
	int skip_roads;
	int count_roundabout;
	struct navigation_itm *cur;
	struct navigation_way *w;
	int against_oneway = 0;
	int keep_dir = 0;
	int old_dir = 0;

	if (want_this_level == DONT_KNOW_LEVEL)
	{
		level3 = 1;
	}
	else
	{
		level3 = want_this_level;
	}


	int cur_vehicle_speed = 0;
	if ((global_navit) && (global_navit->vehicle))
	{
		cur_vehicle_speed = global_navit->vehicle->speed; // in km/h
	}

	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
		//dbg(0, "itm->way.flags=%x\n", itm->way.flags);
		//dbg(0, "itm->next->way.flags=%x\n", itm->next->way.flags);
		//dbg(0, "itm->way.dir=%d itm->next->way.dir=%d\n", itm->way.dir, itm->next->way.dir);

		//dbg(0, "2 itm->way.flags=%x\n", cmd->itm->way.flags);
		//dbg(0, "2 itm->next->way.flags=%x\n", cmd->itm->next->way.flags);
		//dbg(0, "2 itm->way.dir=%d itm->next->way.dir=%d\n", cmd->itm->way.dir, cmd->itm->next->way.dir);

		int flags_old = navigation_item_get_flags(&itm->way);
		int flags_new = 0;
		if (itm->next)
		{
			flags_new = navigation_item_get_flags(&itm->next->way);
		}
		//dbg(0, "(2211)old flags=%x new flags=%x\n", flags_old, flags_new);

		// long long wayid_old = navigation_item_get_wayid(&(itm->way));
		// long long wayid_new = navigation_item_get_wayid(&(itm->next->way));
		// dbg(0, "WID old_way_id=%lld new_way_id=%lld\n", wayid_old, wayid_new);


		if ( (itm->way.dir == 1) && ((itm->next->way.dir == -1) && (itm->next->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)) )
		{
			against_oneway = 1;
			//dbg(0, "SPEAK: (1)going against oneway street!\n");
		}
		else if (      ((itm->way.dir == -1) && (!(itm->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)))          && ((itm->next->way.dir == -1) && (itm->next->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO))            )
		{
			against_oneway = 1;
			//dbg(0, "SPEAK: (2)going against oneway street!\n");
		}
	}

	//dbg(0, "d=%d d3=%d\n", distance, (distance - cmd->length));
	level_now = navigation_get_announce_level(nav, itm->way.item.type, distance - cmd->length, 0);
	//dbg(0, "level_now=%d\n", level_now);

	w = itm->next->way.next;
	strength_needed = 0;

	if (angle_delta(itm->next->way.angle2, itm->angle_end) < 0) // left
	{
		while (w)
		{
			if (angle_delta(w->angle2, itm->angle_end) < 0) // other ways are going left
			{
				strength_needed = 1;
				break;
			}
			w = w->next;
		}
	}
	else // right
	{
		while (w)
		{
			if (angle_delta(w->angle2, itm->angle_end) > 0) // other ways are going right
			{
				strength_needed = 1;
				break;
			}
			w = w->next;
		}
	}


	if ((strength_needed == 0) && (delta < 9)) // for corrected turn (delta will be 8), use strength ("slight")
	{
		strength_needed = 1;
	}

	//dbg(0, "cmd->delta=%d\n", delta);

	if (delta < 0)
	{
		// TRANSLATORS: left, as in 'Turn left'
		dir = _("left");
		delta = -delta;

		old_dir = -1; // left
	}
	else if (delta > 0)
	{

		old_dir = 1; // right

		// dir = right
	}
	else // delta == 0 // go straight
	{
		dir = _("straight");
		strength_needed = 0;
	}


	keep_dir = 0;

	if (strength_needed)
	{
		if (delta < 45)
		{
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn slight right
			strength = _("slighty ");
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn slight right
			strength = _("slight ");

			if (delta_real == 0) // keep left/right
			{
				if (old_dir == -1)
				{
					keep_dir = -1;
				}
				else
				{
					keep_dir = 1;
				}
			}
		}
		else if (delta < 105)
		{
			strength = "";
		}
		else if (delta < 165)
		{
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn hard right
			strength = _("hard ");
		}
		else if (delta < 180)
		{
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn really hard right
			strength = _("really hard ");
		}
		else
		{
			// TRANSLATORS: Don't forget the ending space
			strength = "";
		}
	}


	if (type != attr_navigation_long_exact)
	{
		//dbg(0, "round distance d=%d dr=%d\n", distance, round_distance(distance));
		distance = round_distance(distance);
	}




	if (type == attr_navigation_speech)
	{
		dbg(0, "NAV_TURNAROUND:003 ta=%d talimit=%d\n", nav->turn_around, nav->turn_around_limit);

		if (nav->turn_around && nav->turn_around == nav->turn_around_limit)
		{
			dbg(0, "NAV_TURNAROUND:004\n");

			return2 g_strdup(_("When possible, please turn around"));
		}

		if (!connect)
		{
			if (want_this_level == DONT_KNOW_LEVEL)
			{
				level3 = navigation_get_announce_level_cmd(nav, itm, cmd, distance - cmd->length, ((float)cur_vehicle_speed / 3.6f) );
			}
			else
			{
				level3 = want_this_level;
			}
		}

		if (want_this_level == DONT_KNOW_LEVEL)
		{
			level_now = navigation_get_announce_level(nav, itm->way.item.type, distance - cmd->length, ((float)cur_vehicle_speed / 3.6f) );
		}
		else
		{
			level_now = want_this_level;
		}
	}



	switch (level3)
	{
		case 3:
			if (distance > 500)
			{
				d = get_distance(nav, distance, type, 1);
				ret = g_strdup_printf(_("Follow the road for the next %s"), d);
				g_free(d);
			}
			else
			{
				ret = g_strdup("");
			}
			return2 ret;
			// break;
		case 2:
			d = g_strdup(_("soon"));
			break;
		case 1:
			d = get_distance(nav, distance, attr_navigation_short, 0);
			break;
		case 0:
			// d = g_strdup(_("now"));
			d = g_strdup("");
			break;
	}


	// are there more commands left?
	if (cmd->itm->next)
	{
		int tellstreetname = 0;
		char *destination = NULL;

		if (type == attr_navigation_speech)
		{
			if (level3 == 1)
			{
				tellstreetname = 1; // Ok so we tell the name of the street
			}
			else // if (level3 == 0)
			{
				tellstreetname = 0;
			}
		}
		else
		{
			tellstreetname = 1;
		}

		if (global_speak_streetnames == 0)
		{
			// never speak streetnames (user config option)
			tellstreetname = 0;
		}

		if (nav->tell_street_name && tellstreetname)
		{
			destination = navigation_item_destination(nav, cmd->itm, itm, " ");
		}


		if (connect == 0)
		{
			//dbg(0, "level3=%5$d str=%1$s%2$s %3$s%4$s\n", strength, dir, d, destination ? destination : "", level3);

			if (level3 == 0)
			{
				if (against_oneway == 1)
				{
#if 1
					if (delta < 10)
					{
						ret = g_strdup_printf("%s", _("oncoming traffic!")); // just say "attention oneway street!"
					}
					else
					{
						ret = g_strdup_printf("%s, %s", dir, _("oncoming traffic!")); // just say "left" or "right" at the turn + "attention oneway street!"
					}
#endif

				}
				else
				{
#if 1
					if (delta >= 10)
					{
						ret = g_strdup(dir); // just say "left" or "right" at the turn
					}
					else
					{
						ret = g_strdup("");
					}
#endif

#if 0
					if (delta == 0)
					{
						ret = g_strdup(dir); // just say "left" or "right" or "straight" at the turn
					}
					else
					{
						if (keep_dir != 0)
						{
							if (keep_dir == -1)
							{
								// TRANSLATORS: The argument is the distance. Example: 'keep left in 100 meters'
								ret = g_strdup_printf(_("keep left %s"), "");
							}
							else
							{
								// TRANSLATORS: The argument is the distance. Example: 'keep right in 100 meters'
								ret = g_strdup_printf(_("keep right %s"), "");
							}
						}
						else
						{
							if (delta >= 10)
							{
								ret = g_strdup(dir); // just say "left" or "right" at the turn
							}
							else
							{
								ret = g_strdup("");
							}
						}
					}
#endif

				}
			}
			else // if (level3 == 1)
			{
				if ((against_oneway == 1) && (delta < 10))
				{
					ret = g_strdup(""); // probably just going against a oneway street, but travelling straight on. dont say anything now
				}
				else
				{
					if (delta >= 10)
					{
						// TRANSLATORS: The first argument is strength, the second direction, the third distance and the fourth destination Example: 'Turn 'slightly' 'left' in '100 m' 'onto baker street'
						ret = g_strdup_printf_4_str(_("Turn %1$s%2$s %3$s%4$s"), strength, dir, d, destination ? destination : "");
					}
					else
					{
						ret = g_strdup("");
					}
				}
			}

		}
		else // (connect == 1)
		{
			if (level3 == 0)
			{
				if (against_oneway == 1)
				{
					if (delta < 10)
					{
						ret = g_strdup_printf("%s %s", _("then"), _("oncoming traffic!"));
					}
					else
					{
						ret = g_strdup_printf("%s %s %s", _("then"), dir, _("oncoming traffic!"));
					}
				}
				else
				{
					if (delta >= 10)
					{
						ret = g_strdup_printf("%s %s", _("then"), dir);
					}
					else
					{
						ret = g_strdup("");
					}
				}
			}
			else // if (level3 == 1)
			{
				if (against_oneway == 1)
				{
					if (delta < 10)
					{
						// nothing
						ret = g_strdup("");
					}
					else
					{
						// TRANSLATORS: First argument is strength, second direction, third distance, fourth destination
						ret = g_strdup_printf_4_str(_("then turn %1$s%2$s %3$s%4$s"), strength, dir, d, destination ? destination : "");
					}
				}
				else
				{
					if (delta >= 10)
					{
						// TRANSLATORS: First argument is strength, second direction, third distance, fourth destination
						ret = g_strdup_printf_4_str(_("then turn %1$s%2$s %3$s%4$s"), strength, dir, d, destination ? destination : "");
					}
					else
					{
						ret = g_strdup("");
					}
				}
			}
		}


		if (destination)
		{
			g_free(destination);
			destination = NULL;
		}
	}
	// no more commands left, must be at destination
	else
	{
		if (!connect)
		{
			d = get_distance(nav, distance, type, 1);

			// TRANSLATORS: EXAMPLE: You have reached your destination in 300 meters
			ret = g_strdup_printf(_("You have reached your destination %s"), d);

			g_free(d);
		}
		else
		{
			ret = g_strdup(_("then you have reached your destination."));
		}
	}

	return2 ret;

__F_END__
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



static char *
show_maneuver_at_level(struct navigation *nav, struct navigation_itm *itm, struct navigation_command *cmd, enum attr_type type, int connect, int want_this_level, int override_tellstreetname)
{
__F_START__

	// TRANSLATORS: right, as in 'Turn right'
	char *dir = _("right");
	char *strength = "";
	int distance = itm->dest_length - cmd->itm->dest_length;
	char *d, *ret = NULL;
	int delta = cmd->delta;
	int delta_real = cmd->delta_real;
	int level;
	int level_now = 99;
	int strength_needed;
	int skip_roads;
	int count_roundabout; // car mode ----
	struct navigation_itm *cur;
	struct navigation_way *w;
	int against_oneway = 0;
	int keep_dir = 0; // car mode ----
	int old_dir = 0;

	if (connect)
	{
		level = -2; // level = -2 means "connect to another maneuver via 'then ...'"
	}
	else
	{
		if (want_this_level == DONT_KNOW_LEVEL)
		{
			level = 1;
		}
		else
		{
			level = want_this_level;
		}
	}

	int cur_vehicle_speed = 0;
	if ((global_navit) && (global_navit->vehicle))
	{
		cur_vehicle_speed = global_navit->vehicle->speed; // in km/h
	}


	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
		//dbg(0, "itm->way.flags=%x\n", itm->way.flags);
		//dbg(0, "itm->next->way.flags=%x\n", itm->next->way.flags);

		// int flags_old = navigation_item_get_flags(&itm->way);
		// int flags_new = navigation_item_get_flags(&itm->next->way);
		// dbg(0, "old flags=%x new flags=%x\n", flags_old, flags_new);

		if ( (itm->way.dir == 1) && ((itm->next->way.dir == -1) && (itm->next->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)) )
		{
			against_oneway = 1;
			//dbg(0, "SPEAK: (1)going against oneway street!\n");
		}
		else if (      ((itm->way.dir == -1) && (!(itm->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)))          && ((itm->next->way.dir == -1) && (itm->next->way.flags & NAVIT_AF_ONEWAY_BICYCLE_NO))            )
		{
			against_oneway = 1;
			//dbg(0, "SPEAK: (2)going against oneway street!\n");
		}
	}

	w = itm->next->way.next;
	strength_needed = 0;

	//dbg(0, "STRENGTH:001:strength_needed = %d delta = %d\n", strength_needed, delta);

	if (angle_delta(itm->next->way.angle2, itm->angle_end) < 0) // left
	{
		while (w)
		{
			if (angle_delta(w->angle2, itm->angle_end) < 0) // other ways are going left
			{
				strength_needed = 1;
				// dbg(0, "STRENGTH:002:strength_needed = %d\n", strength_needed);
				break;
			}
			w = w->next;
		}
	}
	else // right
	{
		while (w)
		{
			if (angle_delta(w->angle2, itm->angle_end) > 0) // other ways are going right
			{
				strength_needed = 1;
				// dbg(0, "STRENGTH:003:strength_needed = %d\n", strength_needed);
				break;
			}
			w = w->next;
		}
	}

	if ((strength_needed == 0) && (delta < 9)) // for corrected turn (delta will be 8), use strength ("slight")
	{
		strength_needed = 1;
	}

	// dbg(0, "STRAI:K001:strength_needed=%d delta=%d delta_real=%d\n", strength_needed, delta, delta_real);

	//dbg(0, "cmd->delta=%d\n", delta);

	if (delta < 0)
	{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
		android_send_generic_text(1,"+*#O:left\n");
#endif
#endif
		// TRANSLATORS: left, as in 'Turn left'
		dir = _("left");
		delta = -delta;

		old_dir = -1; // left

	}
	else if (delta > 0)
	{

		old_dir = 1; // right

		// dir = right
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
		android_send_generic_text(1,"+*#O:right\n");
#endif
#endif
	}
	else // delta == 0 // go straight
	{
		dir = _("straight");
		strength_needed = 0;
		// dbg(0, "STRENGTH:004:strength_needed = %d\n", strength_needed);
	}

	dbg(0, "STRAI:K002:*:strength_needed=%d delta=%d delta_real=%d\n", strength_needed, delta, delta_real);


	keep_dir = 0;

	if (strength_needed)
	{
		// dbg(0, "STRENGTH:005:\n");


		if (delta < 45)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:slight \n");
#endif
#endif
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn slight right
			strength = _("slighty ");
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn slight right
			strength = _("slight ");

			if (delta_real == 0) // keep left/right
			{
				if (old_dir == -1)
				{
					keep_dir = -1;
				}
				else
				{
					keep_dir = 1;
				}
			}

			dbg(0, "STRENGTH:006:strength_needed = %s\n", strength);

		}
		else if (delta < 105)
		{
			strength = "";
		}
		else if (delta < 165)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:hard \n");
#endif
#endif
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn hard right
			strength = _("hard ");
		}
		else if (delta < 180)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:really hard \n");
#endif
#endif
			// TRANSLATORS: Don't forget the ending space
			// TRANSLATORS: EXAMPLE: turn really hard right
			strength = _("really hard ");
		}
		else
		{
			// dbg(1,"delta=%d\n", delta);
			strength = "";
		}
	}

	// dbg(0, "STRENGTH:010:strength_needed = %s\n", strength);


	if (type != attr_navigation_long_exact)
	{
		distance = round_distance(distance);
	}

	if (type == attr_navigation_speech)
	{
		dbg(0, "NAV_TURNAROUND:001 ta=%d talimit=%d\n", nav->turn_around, nav->turn_around_limit);

		if (nav->turn_around && nav->turn_around == nav->turn_around_limit)
		{

			dbg(0, "NAV_TURNAROUND:002:*******\n");

#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:When possible, please turn around\n");
#endif
#endif
			return2 g_strdup(_("When possible, please turn around"));
		}

		if (!connect)
		{
			if (want_this_level == DONT_KNOW_LEVEL)
			{
				level = navigation_get_announce_level_cmd(nav, itm, cmd, distance - cmd->length, ((float)cur_vehicle_speed / 3.6f) );
			}
			else
			{
				level = want_this_level;
			}
		}

		if (want_this_level == DONT_KNOW_LEVEL)
		{
			level_now = navigation_get_announce_level(nav, itm->way.item.type, distance - cmd->length, ((float)cur_vehicle_speed / 3.6f) );
		}
		else
		{
			level_now = want_this_level;
		}

	}

#ifdef NAVIT_DEBUG_COORD_LIST
	int need_clear = 1;
#endif


#if 1
// ------------------- jandegr -------------------

	struct navigation_way *candidate_way;

	if (cmd->itm->prev->way.flags & NAVIT_AF_ROUNDABOUT)
	{
		cur = cmd->itm->prev;
		count_roundabout = 0;

		// ----- fix -----
		struct navigation_itm *cur_orig = cur->next;

		enum item_type cur_street_type = 0;
		int exit_is_lover_street_type = 0;
		cur_street_type = cur_orig->way.item.type;
		// dbg(0, "curr item type=%s\n", item_to_name(cur_orig->way.item.type));
		exit_is_lover_street_type = navigation_is_low_level_street(cur_street_type);

		int next_exit_is_lower_street_type = 0;
		// ----- fix -----


		dbg(0, "ROUNDABT:001:enter\n");

		while (cur && (cur->way.flags & NAVIT_AF_ROUNDABOUT))
		{

			// dbg(0, "ROUNDABT:002:loop-1:w\n");

			candidate_way=cur->next->way.next;
			while (candidate_way)
			{

				dbg(0, "ROUNDABT:002:loop-2:w2 type=%s\n", item_to_name(candidate_way->item.type));

				// If the next segment has no exit or the exit isn't allowed, don't count it
				if (candidate_way && is_way_allowed(nav, candidate_way, 3))
				{

					dbg(0, "ROUNDABT:003:is_allowed type=%s\n", item_to_name(candidate_way->item.type));

					// only count street_service (and lesser streets) if we also exit on street_service (or any lesser street)
					if (cur->next)
					{
						next_exit_is_lower_street_type =  navigation_is_low_level_street(candidate_way->item.type);

						dbg(0, "ROUNDABT:004:next_exit_is_lower_street_type=%d type=%s\n", next_exit_is_lower_street_type, item_to_name(candidate_way->item.type));
					}

					if ((exit_is_lover_street_type == 1) || (next_exit_is_lower_street_type == 0))
					{
						count_roundabout++;

						dbg(0, "ROUNDABT:005:found:count_roundabout=%d\n", count_roundabout);

						/* As soon as we have an allowed one on this node,
						 * stop further counting for this node.
						 */
						candidate_way = candidate_way->next;
						break;
					}
				}
				candidate_way=candidate_way->next;
			}
			cur = cur->prev;
		}
		
		/*try to figure out if the entry node has a usable exit as well
		*
		* this will fail for left-hand driving areas
		*/
		if (cur && cur->next)
		{
			dbg(0, "ROUNDABT:007:entry-check\n");

			candidate_way = cur->next->way.next;
			while (candidate_way)
			{
				dbg(0, "ROUNDABT:008:loop-3:w3 type=%s\n", item_to_name(candidate_way->item.type));

				if (candidate_way && is_way_allowed(nav,candidate_way,3)
					&& (cur->angle_end < candidate_way->angle2) && ( candidate_way->angle2 > cur->next->way.angle2 ))
					/*for the entry node only count exits to the right ?*/
				{
					count_roundabout++;

					dbg(0, "ROUNDABT:009:entry:found:count_roundabout=%d\n", count_roundabout);
					/* As soon as we have an allowed one on this node,
					* stop further counting for this node.
					*/
					break;
				}
				candidate_way = candidate_way->next;
			}
		}
		
		switch (level)
		{
#if 0
			case 3:
				d=get_distance(nav, distance, type, 1);
				return g_strdup_printf(_("Follow the road for the next %s"), d);
#endif
			case 2:
				return2 g_strdup(_("Enter the roundabout soon"));
			case 1:
				// TRANSLATORS: EXAMPLE: Leave the roundabout at the second exit
				return2 g_strdup_printf(_("Leave the roundabout at the %s"), get_exit_count_str(count_roundabout));
			case -2:
				// TRANSLATORS: EXAMPLE: ... then leave the roundabout at the second exit
				return2 g_strdup_printf(_("then leave the roundabout at the %s"), get_exit_count_str(count_roundabout));
			case 0:
				// TRANSLATORS: EXAMPLE: Leave the roundabout at the second exit
				return2 g_strdup_printf(_("Leave the roundabout at the %s"), get_exit_count_str(count_roundabout));
		}
	}


// ------------------- jandegr -------------------
#endif







#if 0
// -------------------  zoff   -------------------

	if (cmd->itm->prev->way.flags & NAVIT_AF_ROUNDABOUT)
	{
		cur = cmd->itm->prev;
		count_roundabout = 0;

		struct navigation_itm *cur_orig = cur->next;

		enum item_type cur_street_type = 0;
		int exit_is_lover_street_type = 0;
		cur_street_type = cur_orig->way.item.type;
		// dbg(0, "curr item type=%s\n", item_to_name(cur_orig->way.item.type));
		exit_is_lover_street_type = navigation_is_low_level_street(cur_street_type);

		int next_exit_is_lower_street_type = 0;

		while (cur && (cur->way.flags & NAVIT_AF_ROUNDABOUT))
		{
			// If the next segment has no exit or the exit isn't allowed, don't count it
			if (cur->next->way.next && is_way_allowed(nav, cur->next->way.next, 3))
			{

				// only count street_service (and lesser streets) if we also exit on street_service (or any lesser street)
				if (cur->next)
				{
					next_exit_is_lower_street_type =  navigation_is_low_level_street(cur->next->way.next->item.type);
				}

				if ((exit_is_lover_street_type == 1) || (next_exit_is_lower_street_type == 0))
				{
					count_roundabout++;

#ifdef NAVIT_DEBUG_COORD_LIST
					if (need_clear == 1)
					{
						need_clear = 0;
						global_debug_coord_list_items = 0;

						struct coord c2[5];

						if (navigation_get_real_item_first_coord(&(cur_orig->way), c2))
						{
							global_debug_coord_list[global_debug_coord_list_items].x = c2[0].x;
							global_debug_coord_list[global_debug_coord_list_items].y = c2[0].y;
							global_debug_coord_list_items++;
							global_debug_coord_list[global_debug_coord_list_items].x = c2[1].x;
							global_debug_coord_list[global_debug_coord_list_items].y = c2[1].y;
							global_debug_coord_list_items++;
						}

					}

					struct coord c1[5];
					if (navigation_get_real_item_first_coord(cur->next->way.next, c1))
					{
						if ((global_debug_coord_list_items + 2) > MAX_DEBUG_COORDS)
						{
							global_debug_coord_list_items = 0;
						}

						global_debug_coord_list[global_debug_coord_list_items].x = c1[0].x;
						global_debug_coord_list[global_debug_coord_list_items].y = c1[0].y;
						global_debug_coord_list_items++;
						global_debug_coord_list[global_debug_coord_list_items].x = c1[1].x;
						global_debug_coord_list[global_debug_coord_list_items].y = c1[1].y;
						global_debug_coord_list_items++;
					}
#endif

				}
			}
			cur = cur->prev;
		}

		gchar* xy;

		switch (level)
		{
			case 2:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:Enter the roundabout soon\n");
#endif
#endif
				return2 g_strdup(_("Enter the roundabout soon"));




			case 1:


#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:Leave the roundabout at the %s\n");
				xy=g_strdup_printf("+*#1:%s\n", get_exit_count_str(count_roundabout));
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: EXAMPLE: Leave the roundabout at the second exit
				return2 g_strdup_printf(_("Leave the roundabout at the %s"), get_exit_count_str(count_roundabout));


// ---- DISABLED ----
#if 0
				d = get_distance(nav, distance, type, 1);

#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:In %s, enter the roundabout\n");
				xy=g_strdup_printf("+*#1:%s\n", d);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: %s is the distance to the roundabout
				// TRANSLATORS: EXAMPLE: In 300m, enter the roundabout
				ret = g_strdup_printf(_("In %s, enter the roundabout"), d);
				g_free(d);
				return2 ret;
#endif
// ---- DISABLED ----



			case -2:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:then leave the roundabout at the %s\n");
				xy=g_strdup_printf("+*#1:%s\n", get_exit_count_str(count_roundabout));
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: EXAMPLE: ... then leave the roundabout at the second exit
				return2 g_strdup_printf(_("then leave the roundabout at the %s"), get_exit_count_str(count_roundabout));


			case 0:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:Leave the roundabout at the %s\n");
				xy=g_strdup_printf("+*#1:%s\n", get_exit_count_str(count_roundabout));
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: EXAMPLE: Leave the roundabout at the second exit
				return2 g_strdup_printf(_("Leave the roundabout at the %s"), get_exit_count_str(count_roundabout));
		}
	}

// -------------------  zoff   -------------------
#endif









	// -- NEW 002 --
	if (cmd->itm)
	{
		// put correct destination into struct (to later show in GUI)
		navigation_select_announced_destinations(cmd);
		// string is in cmd->itm->way.street_dest_text
	}
	// -- NEW 002 --



	switch (level)
	{
		case 3:
			if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
			{
				if (distance > 500)
				{
					d = get_distance(nav, distance, type, 1);

#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:Follow the road for the next %s\n");
					gchar* xy=g_strdup_printf("+*#1:%s\n", d);
					android_send_generic_text(1,xy);
					g_free(xy);
#endif
#endif

					ret = g_strdup_printf(_("Follow the road for the next %s"), d);
					g_free(d);
				}
				else
				{
					ret = g_strdup("");
				}
			}
			else
			{
				d = get_distance(nav, distance, type, 1);

#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:Follow the road for the next %s\n");
				gchar* xy=g_strdup_printf("+*#1:%s\n", d);
				android_send_generic_text(1,xy);
				g_free(xy);
#endif
#endif
				// TRANSLATORS: EXAMPLE: Follow the road for the next 300 meters
				ret = g_strdup_printf(_("Follow the road for the next %s"), d);
				g_free(d);
			}
			return2 ret;
		case 2:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:soon\n");
#endif
#endif

			// dbg(0, "SPK:*soon*\n");


			d = g_strdup(_("soon"));
			break;
		case 1:
			d = get_distance(nav, distance, attr_navigation_short, 0);
			break;
		case 0:
			// DISABLE S_R // skip_roads = count_possible_turns(nav, cmd->prev ? cmd->prev->itm : nav->first, cmd->itm, cmd->delta);
			skip_roads = 0;

			dbg(0, "count_possible_turns:1:%d", skip_roads);

			if ((skip_roads > 0) && ((global_vehicle_profile != 1) && (global_vehicle_profile != 2)))
			{
				if (get_count_str(skip_roads + 1))
				{

// marker -1- //

#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:Take the %1$s road to the %2$s\n");
					gchar* xy=g_strdup_printf("+*#1:%s\n", get_count_str(skip_roads + 1));
					android_send_generic_text(1,xy);
					g_free(xy);
					xy=g_strdup_printf("+*#1:%s\n", dir);
					android_send_generic_text(1,xy);
					g_free(xy);
#endif
#endif
					// TRANSLATORS: First argument is the how manieth street to take, second the direction
					ret = g_strdup_printf_2_str(_("Take the %1$s road to the %2$s"), get_count_str(skip_roads + 1), dir);
					return2 ret;
				}
				else
				{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:after %i roads\n");
					gchar* xy=g_strdup_printf("+*#1:%i\n", skip_roads);
					android_send_generic_text(1,xy);
					g_free(xy);
#endif
#endif
					d = g_strdup_printf(_("after %i roads"), skip_roads);
				}
			}
			else
			{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:now\n");
#endif
#endif
				d = g_strdup(_("now"));
			}

			break;

		case -2:
			// DISABLE S_R // skip_roads = count_possible_turns(nav, cmd->prev->itm, cmd->itm, cmd->delta);
			skip_roads = 0;

			dbg(0, "count_possible_turns:2:%d", skip_roads);

			if ((skip_roads > 0) && ((global_vehicle_profile != 1) && (global_vehicle_profile != 2)))
			{
				// TRANSLATORS: First argument is the how manieth street to take, second the direction
				// TRANSLATORS: EXAMPLE: ... then take the second road to the right
				if (get_count_str(skip_roads + 1))
				{

// marker -2- //

#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:then take the %1$s road to the %2$s\n");
					gchar* xy=g_strdup_printf("+*#1:%s\n", get_count_str(skip_roads + 1));
					android_send_generic_text(1,xy);
					g_free(xy);
					xy=g_strdup_printf("+*#1:%s\n", dir);
					android_send_generic_text(1,xy);
					g_free(xy);
#endif
#endif
					ret = g_strdup_printf_2_str(_("then take the %1$s road to the %2$s"), get_count_str(skip_roads + 1), dir);
					return2 ret;
				}
				else
				{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#O:after %i roads\n");
					gchar* xy=g_strdup_printf("+*#1:%i\n", skip_roads);
					android_send_generic_text(1,xy);
					g_free(xy);
#endif
#endif
					d = g_strdup_printf(_("after %i roads"), skip_roads);
				}

			}
			else
			{
				d = g_strdup("");
			}
			break;
		default:
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:error\n");
#endif
#endif
			d = g_strdup(_("error"));
	}



	if (cmd->itm->next)
	{
		// dbg(0, "SPK:000a\n");

		int tellstreetname = 0;
		char *destination = NULL;

		if (type == attr_navigation_speech)
		{
			if (level == 1)
			{
				cmd->itm->streetname_told = 1;
				tellstreetname = 1; // Ok so we tell the name of the street 
			}

			if (level == 0)
			{
				if (cmd->itm->streetname_told == 0) // we are right at the intersection
				{
					tellstreetname = 1;
				}
				else
				{
					cmd->itm->streetname_told = 0; // reset just in case we come to the same street again
				}
			}

		}
		else
		{
			tellstreetname = 1;
		}

		if (global_speak_streetnames == 0)
		{
			// never speak streetnames (user config option)
			tellstreetname = 0;
		}



		if (override_tellstreetname != -1)
		{

#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "SPK:level=%d street_dest_text=%p tellstreetname=%d\n", level, cmd->itm->way.street_dest_text, tellstreetname);
#endif
			if (nav->tell_street_name && tellstreetname)
			{
				destination = navigation_item_destination(nav, cmd->itm, itm, " ");
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				dbg(0, "SPK:levelX=%d dest=%s\n", level, destination);
#endif
			}
			else if ((global_speak_streetnames == 1) && (cmd->itm->way.street_dest_text))
			{
				destination = navigation_item_destination(nav, cmd->itm, itm, " ");
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				dbg(0, "SPK:levely=%d dest=%s d=%s\n", level, destination, cmd->itm->way.street_dest_text);
#endif

				cmd->itm->streetname_told = 0;
			}
		}


		// dbg(0, "SPK:001\n");

		if (level != -2)
		{

			//dbg(0, "SPK:002\n");

#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:Turn %1$s%2$s %3$s%4$s\n");
			gchar* xy=g_strdup_printf("+*#1:%s\n", strength);
			android_send_generic_text(1,xy);
			g_free(xy);
			xy=g_strdup_printf("+*#1:%s\n", dir);
			android_send_generic_text(1,xy);
			g_free(xy);
			xy=g_strdup_printf("+*#1:%s\n", d);
			android_send_generic_text(1,xy);
			g_free(xy);
			xy=g_strdup_printf("+*#1:%s\n", destination ? destination : "");
			android_send_generic_text(1,xy);
			g_free(xy);
#endif
#endif

			if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)) // bicycle mode
			{
				//dbg(0, "level=%5$d level_now=%6$d str=%1$s%2$s %3$s%4$s\n", strength, dir, d, destination ? destination : "", level, level_now);

				if (level_now == 0)
				{
					if (against_oneway == 1)
					{
						if (delta < 8)
						{
							ret = g_strdup_printf("%s", _("oncoming traffic!")); // just say "attention oneway street!"
						}
						else
						{
							ret = g_strdup_printf("%s, %s", dir, _("oncoming traffic!")); // just say "left" or "right" at the turn + "attention oneway street!"
						}
					}
					else
					{
						ret = g_strdup(dir); // just say "left" or "right" at the turn
					}
				}
				else
				{
					if ((against_oneway == 1) && (delta < 8))
					{
						ret = g_strdup(""); // probably just going against a oneway street, but travelling straight on. dont say anything now
					}
					else
					{
						if (delta == 0)
						{
							// TRANSLATORS: The first argument is strength, the second direction, the third distance and the fourth destination Example: 'Turn 'slightly' 'left' in '100 m' 'onto baker street'
							ret = g_strdup_printf_4_str(_("%1$s%2$s %3$s%4$s"), "", dir, d, destination ? destination : "");
						}
						else
						{
							// TRANSLATORS: The first argument is strength, the second direction, the third distance and the fourth destination Example: 'Turn 'slightly' 'left' in '100 m' 'onto baker street'
							ret = g_strdup_printf_4_str(_("Turn %1$s%2$s %3$s%4$s"), strength, dir, d, destination ? destination : "");
						}
					}
				}
			}
			else // car mode
			{
				if (delta == 0)
				{
					// TRANSLATORS: The first argument is strength, the second direction, the third distance and the fourth destination Example: 'Turn 'slightly' 'left' in '100 m' 'onto baker street'
					ret = g_strdup_printf_4_str(_("%1$s%2$s %3$s%4$s"), "", dir, d, destination ? destination : "");
				}
				else
				{
					if (keep_dir != 0)
					{
						if (keep_dir == -1)
						{
							// TRANSLATORS: The argument is the distance. Example: 'keep left in 100 meters'
							ret = g_strdup_printf(_("keep left %s"), d);

							// dirty hack, so we don't need new translations
							char *ret2 = g_strdup_printf("%s%s", ret, destination ? destination : "");
							g_free(ret);
							ret = ret2;
							// ret2 = NULL;
						}
						else
						{
							// TRANSLATORS: The argument is the distance. Example: 'keep right in 100 meters'
							ret = g_strdup_printf(_("keep right %s"), d);

							// dirty hack, so we don't need new translations
							char *ret2 = g_strdup_printf("%s%s", ret, destination ? destination : "");
							g_free(ret);
							ret = ret2;
							// ret2 = NULL;
						}
					}
					else
					{
						// TRANSLATORS: The first argument is strength, the second direction, the third distance and the fourth destination Example: 'Turn 'slightly' 'left' in '100 m' 'onto baker street'
						ret = g_strdup_printf_4_str(_("Turn %1$s%2$s %3$s%4$s"), strength, dir, d, destination ? destination : "");
					}
				}
			}
		}
		else // (level == -2)
		{

			//dbg(0, "SPK:007\n");


#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:then turn %1$s%2$s %3$s%4$s\n");
			gchar* xy=g_strdup_printf("+*#1:%s\n", strength);
			android_send_generic_text(1,xy);
			g_free(xy);
			xy=g_strdup_printf("+*#1:%s\n", dir);
			android_send_generic_text(1,xy);
			g_free(xy);
			xy=g_strdup_printf("+*#1:%s\n", d);
			android_send_generic_text(1,xy);
			g_free(xy);
			xy=g_strdup_printf("+*#1:%s\n", destination ? destination : "");
			android_send_generic_text(1,xy);
			g_free(xy);
#endif
#endif
			if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)) // bicycle mode
			{
				if ((against_oneway == 1) && (delta < 8))
				{
					ret = g_strdup(""); // probably just going against a oneway street, but travelling straight on. dont say anything now
				}
				else
				{
					if (delta == 0)
					{
						// TRANSLATORS: First argument is strength, second direction, third how many roads to skip, fourth destination
						ret = g_strdup_printf_4_str(_("then %1$s%2$s %3$s%4$s"), "", dir, d, destination ? destination : "");
					}
					else
					{
						// TRANSLATORS: First argument is strength, second direction, third how many roads to skip, fourth destination
						ret = g_strdup_printf_4_str(_("then turn %1$s%2$s %3$s%4$s"), strength, dir, d, destination ? destination : "");
					}
				}
			}
			else // car mode
			{
				if (delta == 0)
				{
					// TRANSLATORS: First argument is strength, second direction, third how many roads to skip, fourth destination
					ret = g_strdup_printf_4_str(_("then %1$s%2$s %3$s%4$s"), "", dir, d, destination ? destination : "");
				}
				else
				{
					if (keep_dir != 0)
					{
						if (keep_dir == -1)
						{
							ret = g_strdup_printf("%s", _("then keep left"));
						}
						else
						{
							ret = g_strdup_printf("%s", _("then keep right"));
						}
					}
					else
					{
						// TRANSLATORS: First argument is strength, second direction, third how many roads to skip, fourth destination
						ret = g_strdup_printf_4_str(_("then turn %1$s%2$s %3$s%4$s"), strength, dir, d, ""); // dont speak destination when maneuvers are connected!
						// ret = g_strdup_printf_4_str(_("then turn %1$s%2$s %3$s%4$s"), strength, dir, d, destination ? destination : "");
					}
				}
			}
		}
		g_free(destination);
	}
	else
	{
		if (!connect)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:You have reached your destination %s\n");
			gchar* xy=g_strdup_printf("+*#1:%s\n", d);
			android_send_generic_text(1,xy);
			g_free(xy);
#endif
#endif
			// TRANSLATORS: EXAMPLE: You have reached your destination in 300 meters
			ret = g_strdup_printf(_("You have reached your destination %s"), d);
		}
		else
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#O:then you have reached your destination.\n");
#endif
#endif
			ret = g_strdup(_("then you have reached your destination."));
		}
	}
	g_free(d);

	return2 ret;

__F_END__
}



static char *
show_maneuver(struct navigation *nav, struct navigation_itm *itm, struct navigation_command *cmd, enum attr_type type, int connect)
{
	return show_maneuver_at_level(nav, itm, cmd, type, connect, DONT_KNOW_LEVEL, 0);
}




/**
 * @brief Creates announcements for maneuvers, plus maneuvers immediately following the next maneuver
 *
 * This function does create an announcement for the current maneuver and for maneuvers
 * immediately following that maneuver, if these are too close and we're in speech navigation.
 *
 * @return An announcement that should be made
 */
// central entry point for TTS maneuvers --------------
// central entry point for TTS maneuvers --------------
// central entry point for TTS maneuvers --------------
static char *
show_next_maneuvers(struct navigation *nav, struct navigation_itm *itm, struct navigation_command *cmd, enum attr_type type)
{
__F_START__

	struct navigation_command *cur, *prev;
	int distance = itm->dest_length - cmd->itm->dest_length;
	int level, dist, i, time, level2;
	int speech_time, time2nav;
	char *ret, *old, *buf, *next;
	char *ret22;

	char *temp_txt = NULL;


	//dbg(0, "Enter\n");

	if (type != attr_navigation_speech)
	{
		if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
		{
			return2 show_maneuver_bicycle(nav, itm, cmd, type, 0, DONT_KNOW_LEVEL); // not for speech, so just return the real values
		}
		else
		{
			return2 show_maneuver(nav, itm, cmd, type, 0); // not for speech, so just return the real values
		}
	}


	int cur_vehicle_speed = 0;
	if ((global_navit) && (global_navit->vehicle))
	{
		cur_vehicle_speed = global_navit->vehicle->speed; // in km/h
	}


	dbg(0, "SPEECH:[-0v-] current speed=%d\n", cur_vehicle_speed);


	// -- bicycle mode START -------------------------------------
	// -- bicycle mode START -------------------------------------
	// -- bicycle mode START -------------------------------------
	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
		level = navigation_get_announce_level(nav, itm->way.item.type, distance - cmd->length, 0);

		//dbg(0, "level = %d\n", level);
		//dbg(0, "(nn)itm->way.flags=%x\n", itm->way.flags);
		//dbg(0, "(nn)itm->next->way.flags=%x\n", itm->next->way.flags);
		//dbg(0, "(nn)distance=%d cmd->length=%d (minus)=%d\n", distance, cmd->length, (distance - cmd->length));

		// in bike mode level should only be "0" or "1" or "3" !

		if (level > 2)
		{
			//dbg(0, "just say the current command\n");
			return2 show_maneuver_bicycle(nav, itm, cmd, type, 0, level); // just say the current command
		}

		if (cmd->itm->told)
		{
			// current command should not be spoken again!
			//dbg(0, "current command should not be spoken again\n");
			return2 g_strdup("");
		}


		//dbg(0, "xx 017a fsp, itm->told=%d l=%d\n", cmd->itm->told, level);
		if (level == 0)
		{
			// this command is spoken (now)
			//dbg(0, "this command is spoken (now)\n");
			cmd->itm->told = 1;
		}
		//dbg(0, "xx 017b fsp, itm->told=%d\n", cmd->itm->told);


		// current maneuver -------
		ret = show_maneuver_bicycle(nav, itm, cmd, type, 0, level); // generate TTS text for current command
		//dbg(0, "ret cmd=%s\n", ret);
		time2nav = navigation_time(itm, cmd->itm->prev);
		//dbg(0, "time2nav = %d\n", time2nav);
		old = NULL;
		// current maneuver -------

		cur = cmd->next;
		prev = cmd;

		if (cur && cur->itm) // we have a next command
		{
			dist = prev->itm->dest_length - cur->itm->dest_length;
			level2 = navigation_get_announce_level(nav, itm->next->way.item.type, dist, 0);
			//dbg(0, "(next)level2 = %d\n", level2);
			//dbg(0, "(next)dist=%d\n", dist);

			if ((level2 < 2) && (dist < (2 * level_static_for_bicycle[0])))
			{
				//dbg(0, "link next command, and dont say it again!\n");
				old = ret;
				buf = show_maneuver_bicycle(nav, prev->itm, cur, type, 1, 0); // generate TTS text for next command, and dont say the next command again!
				//dbg(0, "next cmd=%s\n", next);
				ret = g_strdup_printf("%s, %s", old, buf);
				//dbg(0, "next cmd concat=%s\n", ret);
				g_free(buf);
				g_free(old);
				if (level == 0)
				{
					// dont say this next command again!
					cur->itm->told = 1;
				}
			}
		}
	}
	// -- bicycle mode END   -------------------------------------
	// -- bicycle mode END   -------------------------------------
	// -- bicycle mode END   -------------------------------------
	else
	{



		// -------------------------------------------------
		// -------------------------------------------------
		// -- ******************************************* --
		//                   CAR MODE
		// -- ******************************************* --
		// -------------------------------------------------
		// -------------------------------------------------

		level = navigation_get_announce_level(nav, itm->way.item.type, distance - cmd->length, ((float)cur_vehicle_speed / 3.6f));
		//dbg(0, "level = %d\n", level);
		//dbg(0, "(nn)itm->way.flags=%x\n", itm->way.flags);
		//dbg(0, "(nn)itm->next->way.flags=%x\n", itm->next->way.flags);

		long temp_ts = -1;
		debug_get_timestamp_millis(&temp_ts);
		if (global_last_spoken == -1)
		{
			temp_ts = 99999999;
		}

		int time2nav_2 = navigation_time_real_speed(itm, cmd->itm->prev, cur_vehicle_speed);

		dbg(0, "SPEECH:[-00-] last spoken ago=%f\n", (float)((temp_ts - global_last_spoken) / 1000.0f));
		dbg(0, "SPEECH:[-01-] meters to next maneuver=%d\n", (distance - cmd->length));
		dbg(0, "SPEECH:[-02-] level=%d\n", level);

#ifdef HAVE_API_ANDROID
		temp_txt = g_strdup_printf("SPEECH:[-00-] last spoken ago=%f\n", (float)((temp_ts - global_last_spoken) / 1000.0f));
		android_send_generic_text(20, temp_txt);
		g_free(temp_txt);

		temp_txt = g_strdup_printf("SPEECH:[-01-] meters to next maneuver=%d\n", (distance - cmd->length));
		android_send_generic_text(20, temp_txt);
		g_free(temp_txt);

		temp_txt = g_strdup_printf("SPEECH:[-02-] level=%d\n", level);
		android_send_generic_text(20, temp_txt);
		g_free(temp_txt);
#endif

		dbg(0, "SPEECH:[-03-] time2nav secs=%f\n", ((float)time2nav_2 / 10.0f));

		int dist_to_next_spoken_command = -1;
		float secs_to_next_spoken_command = -1;
		if (level > 0)
		{
			dist_to_next_spoken_command = (distance - cmd->length) - (navigation_get_announce_dist_for_level_on_item(nav, itm->way.item.type, (level - 1), ((float)cur_vehicle_speed / 3.6f)   )  );

			if (cur_vehicle_speed > 0)
			{
				secs_to_next_spoken_command = ((float)dist_to_next_spoken_command / ((float)cur_vehicle_speed / 3.6f));
			}
			else
			{
				secs_to_next_spoken_command = ((float)dist_to_next_spoken_command / ((float)itm->speed / 3.6f));
			}
		}


		// current maneuver -------
		ret22 = show_maneuver_at_level(nav, itm, cmd, type, 0, level, 0);
		// current maneuver -------

		if (nav->speech)
		{
			speech_time = speech_estimate_duration(nav->speech, ret22);
			dbg(0, "SPEECH:[-CUR-] secs=%f text=%s\n", ((float)speech_time)/10.0f, ret22);
		}
		else
		{
			speech_time = -1;
			dbg(0, "SPEECH:[-CUR-] secs=-1 text=%s\n", ret22);
		}


		dbg(0, "SPEECH:[-03-] meters to next announcement=%d secs to next announcement=%f\n", dist_to_next_spoken_command, secs_to_next_spoken_command);

		if (level == 3)
		{

// ===========----------- LEVEL 3 -----------===========

#if 0
			if (((float)((temp_ts - global_last_spoken) / 1000.0f)) < 3.0f)
			{
				dbg(0, "SPEECH:[-NOOP-] 004: already spoken in the last 3 secs.\n");
				return2 g_strdup(""); // dont speak this command now!
			}
#endif

			if (secs_to_next_spoken_command < 7 )
			{
				if (ret22)
				{
					g_free(ret22);
					ret22 = NULL;
				}

#ifdef _DEBUG_BUILD_
				// speak debug ----
				navit_say(global_navit, "level 3a wieder in 7 sekunden");
				// speak debug ----
#endif

				dbg(0, "SPEECH:[-NOOP-] 006b: want to speak again in less than %d secs. level=%d\n", 7, level);
				return2 g_strdup(""); // dont speak this command now!
			}

			if (secs_to_next_spoken_command < (((float)speech_time/10.0f) + 3) )
			{
				if (ret22)
				{
					g_free(ret22);
					ret22 = NULL;
				}

#ifdef _DEBUG_BUILD_
				// speak debug ----
				temp_txt = g_strdup_printf("level 3b wieder in %d sekunden", (int)(((float)speech_time/10.0f) + 3));
				navit_say(global_navit, temp_txt);
				g_free(temp_txt);
				// speak debug ----
#endif

				dbg(0, "SPEECH:[-NOOP-] 006: want to speak again in less than %d secs. level=%d\n", (((float)speech_time/10.0f) + 3), level );
				return2 g_strdup(""); // dont speak this command now!
			}
// ===========----------- LEVEL 3 -----------===========


		}
		else if (level == 2)
		{

// ===========----------- LEVEL 2 -----------===========


#if 0
			if (secs_to_next_spoken_command < 3 )
			{
				if (ret22)
				{
					g_free(ret22);
					ret22 = NULL;
				}

#ifdef _DEBUG_BUILD_
				// speak debug ----
				navit_say(global_navit, "level 2a wieder in 3 sekunden");
				// speak debug ----
#endif

				dbg(0, "SPEECH:[-NOOP-] 007b: want to speak again in less than %d secs. level=%d\n", 3, level);
				return2 g_strdup(""); // dont speak this command now!
			}
#endif

#if 1
			if (secs_to_next_spoken_command < (((float)speech_time/10.0f) + 1) )
			{
				if (ret22)
				{
					g_free(ret22);
					ret22 = NULL;
				}

#ifdef _DEBUG_BUILD_
				// speak debug ----
				temp_txt = g_strdup_printf("level 2b wieder in %d sekunden", (int)(((float)speech_time/10.0f) + 1));
				navit_say(global_navit, temp_txt);
				// speak debug ----
#endif

				dbg(0, "SPEECH:[-NOOP-] 007: want to speak again in less than %d secs. level=%d\n", (int)(((float)speech_time/10.0f) + 1), level);
				return2 g_strdup(""); // dont speak this command now!
			}
#endif

#if 0
			if (((float)((temp_ts - global_last_spoken) / 1000.0f)) < 5.0f)
			{
				if (ret22)
				{
					g_free(ret22);
					ret22 = NULL;
				}

#ifdef _DEBUG_BUILD_
				// speak debug ----
				navit_say(global_navit, "level 2c schon vor 5 sekunden");
				// speak debug ----
#endif

				dbg(0, "SPEECH:[-NOOP-] 005: already spoken in the last 5 secs. level=%d\n", level);
				return2 g_strdup(""); // dont speak this command now!
			}
#endif

// ===========----------- LEVEL 2 -----------===========


		}
		else if (level == 1)
		{

// ===========----------- LEVEL 1 -----------===========


#if 0
			if (((float)time2nav_2 / 10.0f) < 5.0f)
			{
				dbg(0, "SPEECH:[-NOOP-] 001: less than 5 secs. to maneuver\n");
				return2 g_strdup(""); // dont speak this command now!
			}
#endif

#if 0
			if ((distance - cmd->length) < 50)
			{
				dbg(0, "SPEECH:[-NOOP-] 003: less than 50 meters to maneuver\n");
				return2 g_strdup(""); // dont speak this command now!
			}
#endif


			if (dist_to_next_spoken_command < 18)
			{
				if (ret22)
				{
					g_free(ret22);
					ret22 = NULL;
				}

#ifdef _DEBUG_BUILD_
				// speak debug ----
				temp_txt = g_strdup_printf("level 1a wieder in %d sekunden", 18);
				navit_say(global_navit, temp_txt);
				g_free(temp_txt);
				// speak debug ----
#endif


				dbg(0, "SPEECH:[-NOOP-] 011: less than 18 meters to next announcement\n");
				return2 g_strdup(""); // dont speak this command now!
			}

#if 0
			if (((float)((temp_ts - global_last_spoken) / 1000.0f)) < 4.0f)
			{
				if (ret22)
				{
					g_free(ret22);
					ret22 = NULL;
				}

				dbg(0, "SPEECH:[-NOOP-] 002: already spoken in the last 4 secs. level=%d\n", level);
				return2 g_strdup(""); // dont speak this command now!
			}
#endif

			if (secs_to_next_spoken_command < (((float)speech_time/10.0f) + 1) )
			{
				if (secs_to_next_spoken_command < 3.0f)
				{
					if (ret22)
					{
						g_free(ret22);
						ret22 = NULL;
					}

#ifdef _DEBUG_BUILD_
					// speak debug ----
					temp_txt = g_strdup_printf("level 1b wieder in %d sekunden", 3);
					navit_say(global_navit, temp_txt);
					g_free(temp_txt);
					// speak debug ----
#endif


					dbg(0, "SPEECH:[-NOOP-] 008: want to speak again in less than %d secs. level=%d\n", (((float)speech_time/10.0f) + 1), level);
					return2 g_strdup(""); // dont speak this command now!
				}
				else
				{
					// if we have at least 3 seconds left, still say level 1 announcement (without the streetname/destination name)
					dbg(0, "SPEECH:[-NOOP-] 008lo: use level=%d announcement without destination name\n", level);
					if (ret22)
					{
						g_free(ret22);
						ret22 = NULL;
					}
					ret22 = show_maneuver_at_level(nav, itm, cmd, type, 0, level, -1);
					if (nav->speech)
					{
						speech_time = speech_estimate_duration(nav->speech, ret22);
						dbg(0, "SPEECH:[-CUR-] :lo: secs=%f text=%s\n", ((float)speech_time)/10.0f, ret22);
					}
					else
					{
						speech_time = -1;
						dbg(0, "SPEECH:[-CUR-] :lo: secs=-1 text=%s\n", next);
					}

					if (secs_to_next_spoken_command < (((float)speech_time/10.0f) + 0) )
					{
						if (ret22)
						{
							g_free(ret22);
							ret22 = NULL;
						}

#ifdef _DEBUG_BUILD_
						// speak debug ----
						temp_txt = g_strdup_printf("level 1c wieder in %d sekunden", (int)(((float)speech_time/10.0f) + 0));
						navit_say(global_navit, temp_txt);
						g_free(temp_txt);
						// speak debug ----
#endif


						dbg(0, "SPEECH:[-NOOP-] 008loEE: giving up on this announcement\n");
						return2 g_strdup(""); // dont speak this command now!
					}
				}
			}

// ===========----------- LEVEL 1 -----------===========

		}






#ifdef HAVE_API_ANDROID
		temp_txt = g_strdup_printf("SPEECH:[-03-] time2nav secs=%f\n", ((float)time2nav_2 / 10.0f));
		android_send_generic_text(20, temp_txt);
		g_free(temp_txt);
#endif

		if (level > 1)
		{
			dbg(0, "SPEECH:[-R1-]\n");
			return2 ret22; // We accumulate maneuvers only if they are close (level-0 and level-1)
		}

		if (cmd->itm->told)
		{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
			android_send_generic_text(1,"+*#C1:*CANCEL*\n");
#endif
#endif
			dbg(0, "SPEECH:[-R2-]\n");
#ifdef HAVE_API_ANDROID
			temp_txt = g_strdup("SPEECH:[-R2-]\n");
			android_send_generic_text(20, temp_txt);
			g_free(temp_txt);
#endif
			if (ret22)
			{
				g_free(ret22);
				ret22 = NULL;
			}


			return2 g_strdup("");
		}


		// current maneuver -------
		ret = ret22;
		time2nav = navigation_time_real_speed(itm, cmd->itm->prev, cur_vehicle_speed);
		// current maneuver -------


#if 0
		if (nav->speech)
		{
			speech_time = speech_estimate_duration(nav->speech, ret);
			dbg(0, "SPEECH:[-CUR-] secs=%f text=%s\n", ((float)speech_time)/10.0f, ret);
		}
		else
		{
			speech_time = -1;
			dbg(0, "SPEECH:[-CUR-] secs=-1 text=%s\n", next);
		}
#endif


		old = NULL;

		dbg(0, "SPEECH:[-04-] time2nav secs=%f\n", ((float)time2nav / 10.0f));
#ifdef HAVE_API_ANDROID
		temp_txt = g_strdup_printf("SPEECH:[-04-] time2nav secs=%f\n", ((float)time2nav / 10.0f));
		android_send_generic_text(20, temp_txt);
		g_free(temp_txt);
#endif




		// next maneuver -------
		cur = cmd->next;
		prev = cmd;
		// next maneuver -------


		i = 0;
		int max_announcements = 3;
		if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
		{
			max_announcements = 2;
		}

		while (cur && cur->itm) // only accumulate at "level 0" or "level 1"
		{
			// We don't merge more than "max_announcements" announcements...
			if (i > (max_announcements - 2))
			{
				break;
			}

			dist = prev->itm->dest_length - cur->itm->dest_length; // distance between next 2 maneuvers in meters

			if (dist > 420) // too far apart, bail out
			{
				break;
			}

			next = show_maneuver(nav, prev->itm, cur, type, 0);
			if (nav->speech)
			{
				speech_time = speech_estimate_duration(nav->speech, next);
				dbg(0, "SPEECH:[-NXT-] secs=%f text=%s\n", ((float)speech_time)/10.0f, next);
			}
			else
			{
				speech_time = -1;
				dbg(0, "SPEECH:[-NXT-] secs=-1 text=%s\n", next);
			}
			g_free(next);

			if (speech_time == -1)
			{
				// user didn't set cps
				speech_time = 25; // assume 2.5 seconds
				dbg(0, "SPEECH:[-NXT-](2) secs=%f\n", ((float)speech_time)/10.0f);
			}

			//if (cur_vehicle_speed > 0)
			//{
			//	time = ((float)dist / ((float)cur_vehicle_speed / 3.6f)) * 10;
			//}
			//else
			//{
			//	time = navigation_time_real_speed(prev->itm, cur->itm->prev, cur_vehicle_speed);
			time = navigation_time(prev->itm, cur->itm->prev);
			//}

			dbg(0, "SPEECH:[-NXT-][-05-] dist meters=%d time secs=%f speech_time=%f\n", dist, ((float)time)/10.0f, ((float)speech_time)/10.0f);
#ifdef HAVE_API_ANDROID
			temp_txt = g_strdup_printf("SPEECH:[-05-] dist meters=%d time secs=%f speech_time=%f\n", dist, ((float)time)/10.0f, ((float)speech_time)/10.0f);
			android_send_generic_text(20, temp_txt);
			g_free(temp_txt);
#endif


			if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
			{
				// !! should never get here !!
			}
			else
			{
				dbg(0, "SPEECH: time secs=%f speech_time secs=%f [if (time >= (speech_time + 35))]\n", ((float)time)/10.0f, ((float)speech_time)/10.0f);

				if (i == 0)
				{
					if (dist < 170) // distance between maneuvers less than 170m --> always merge maneuvers
					{
					}
					else
					{
						// **OLD** // if (time >= (speech_time + 35)) // (time to turn) >= (time to speak command + 3.5 secs.)
						if (time > ((global_level1_announcement * 10.0f) - 3) ) // more than "global_level1_announcement" seconds to next maneuver -> don't merge it
						{
							dbg(0, "SPEECH:*break first*\n");
							break;
						}
					}
				}
				else
				{
					if (time > (global_level0_announcement * 10.0f)) // more than "global_level0_announcement" seconds to next maneuver -> don't merge it
					{
						dbg(0, "SPEECH:*break next*\n");
						break;
					}
				}
			}

			old = ret;
			buf = show_maneuver(nav, prev->itm, cur, type, 1);
			ret = g_strdup_printf("%s, %s", old, buf);
			g_free(buf);

			dbg(0, "SPEECH: speech_est_dur secs=%f time2nav secs=%f\n", (float)(speech_estimate_duration(nav->speech, ret))/10.0f, (float)time2nav/10.0f);
			// if (nav->speech && speech_estimate_duration(nav->speech, ret) > time2nav)
			// {
			// 	g_free(ret);
			// 	ret = old;
			// 	i = (max_announcements - 1); // This will terminate the loop
			// 	dbg(0, "SPEECH:*terminate loop*\n");
			// }
			// else
			// {
				g_free(old);
			// }


			if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
			{
				// !! should never get here !!
			}
			else
			{
#if 0
				dbg(0, "SPEECH: time secs=%f speech_time secs=%f [if (time <= speech_time)]\n", ((float)time)/10.0f, ((float)speech_time)/10.0f);
				// If the two maneuvers are *really* close, we shouldn't tell the second one again, because TTS won't be fast enough
				if (time <= speech_time)
				{
#ifdef HAVE_API_ANDROID
#ifdef NAVIT_SAY_DEBUG_PRINT
					android_send_generic_text(1,"+*#C2:*CANCEL*\n");
#endif
#endif
					//dbg(0, "cancel speak:%s\n", ret);
					cur->itm->told = 1;
				}
#endif
			}

			prev = cur;
			cur = cur->next;
			i++;
		}

		// -------------------------------------------------
		// -------------------------------------------------
		// -- ******************************************* --
		//                   CAR MODE
		// -- ******************************************* --
		// -------------------------------------------------
		// -------------------------------------------------

	}

	dbg(0, "SPEECH:[-Re-]\n");
#ifdef HAVE_API_ANDROID
	temp_txt = g_strdup("SPEECH:[-Re-]\n");
	android_send_generic_text(20, temp_txt);
	g_free(temp_txt);
#endif

	return2 ret;

__F_END__
}
// central entry point for TTS maneuvers --------------
// central entry point for TTS maneuvers --------------
// central entry point for TTS maneuvers --------------



// ----- global var, BAD!! -------
int global_spoke_last_position_update = 0;
int previous_dest_length = -1;
int previous_dest_length_save = -1;
struct navigation_command *last_first_cmd_save = NULL;
int global_turn_around_spoken = 0;
// ----- global var, BAD!! -------

static void navigation_call_callbacks(struct navigation *this_, int force_speech)
{
__F_START__

	int distance, level = 0;
	int level_cur;
	int distance_orig;
	void *p = this_;
	int dont_speak_yet = 0;
	int level_last_save = 99;

	if (!this_->cmd_first)
	{
		//dbg(0, "ret 0001\n");
		global_spoke_last_position_update = 0;
		return2;
	}

	// EMPTY: calls nothing!!
	// ****** // callback_list_call(this_->callback, 1, &p);

	//if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	//{
	//	force_speech = 7;
	//}

	//dbg(0, "force_speech=%d turn_around=%d turn_around_limit=%d\n", force_speech, this_->turn_around, this_->turn_around_limit);

	distance = this_->first->dest_length - this_->cmd_first->itm->dest_length;
	//dbg(0, "fdl=%d cmdidl=%d\n", this_->first->dest_length, this_->cmd_first->itm->dest_length);
	distance_orig = distance;
	distance = round_distance(distance);

	int cur_vehicle_speed = 0;
	if ((global_navit) && (global_navit->vehicle))
	{
		cur_vehicle_speed = global_navit->vehicle->speed;
	}

	dbg(0, "NCC_:distance_orig=%d distance=%d curr vehicle speed=%d force_speech=%d\n", distance_orig, distance, global_navit->vehicle->speed, force_speech);


	if (this_->turn_around_limit && this_->turn_around == this_->turn_around_limit)
	{

		dbg(0, "NAV_TURNAROUND:005 ta=%d talimit=%d\n", this_->turn_around, this_->turn_around_limit);

		//dbg(0, "xx 001 d=%d rd=%d dt=%d\n", distance_orig, distance_orig, this_->distance_turn);
#if 0
		while (distance > this_->distance_turn)
		{
			this_->level_last = 4;
			level = 4;
			force_speech = 2;

			if (this_->distance_turn >= 500)
			{
				this_->distance_turn *= 2;
			}
			else
			{
				this_->distance_turn = 500;
			}
#endif

			// we need to force a maneuver for "turn around" ------------------
			this_->level_last = 4;
			level = 4;
			force_speech = 2;
			// this_->distance_turn *= 100;

#if 1
			global_driven_away_from_route = 0;
			global_spoke_last_position_update = 0;
			global_last_spoken = -1;
			previous_dest_length = -1;
			previous_dest_length_save = -1;
			last_first_cmd_save = NULL;
#endif

			this_->level_last = 99;


			this_->level_last = level;
			this_->curr_delay = 0;

			dbg(0, "NCC_:force_speech=%d distance=%d level=%d global_spoke_last_position_update=%d\n", force_speech, distance, level, global_spoke_last_position_update);
			dbg(0, "NCC_:========= END ==========\n");

			global_spoke_last_position_update = 1;

			// ----------------------------------
			// ----------------------------------
			// calls -> calls navit_speak
			//
			callback_list_call(this_->callback_speech, 1, &p);
			// ----------------------------------
			// ----------------------------------


			return2;

			// we need to force a maneuver for "turn around" ------------------

#if 0
			dbg(0, "NCC_:loop 001 d=%d dt=%d force_speech=%d\n", distance, this_->distance_turn, force_speech);
		}
#endif

	}
	else if (!this_->turn_around_limit || this_->turn_around != this_->turn_around_limit)
	{
		this_->distance_turn = 50;

		global_turn_around_spoken = 0; // reset turn around speak flag

		//dbg(0, "lcur=%d lprev=%d lnext=%d\n", this_->cmd_first->itm->length, this_->cmd_first->itm->prev->length, this_->cmd_first->itm->next->length);
		//dbg(0, "xx 002a d=%d d2=%d diff=%d\n", distance, (distance - this_->cmd_first->length), (this_->cmd_first->itm->length - distance_orig));

		//dbg(0, "xx 002a ll001=%d\n", (this_->cmd_first->itm->prev->dest_length - this_->cmd_first->itm->dest_length));

		distance -= this_->cmd_first->length;


		level = navigation_get_announce_level_cmd(this_, this_->first, this_->cmd_first, distance_orig, ((float)cur_vehicle_speed / 3.6f) );
		level_cur = navigation_get_announce_level(this_, this_->first->way.item.type, distance_orig, ((float)cur_vehicle_speed / 3.6f));



		if (last_first_cmd_save != this_->cmd_first)
		{
			last_first_cmd_save = this_->cmd_first;
			previous_dest_length = previous_dest_length_save;
		}
		previous_dest_length_save = this_->cmd_first->itm->dest_length;
		// level_last_save = this_->level_last;

		//dbg(0, "NCC_:\n");
		//dbg(0, "NCC_:0---passed item?---\n");
		// dbg(0, "NCC_:level=%d level_cur=%d this_->level_last=%d distance_orig=%d distance=%d\n", level, level_cur, this_->level_last, distance_orig, distance);

#if 1
		if (previous_dest_length == -1)
		{
			dbg(0, "NCC_:no prev nav command\n");
		}
		else
		{
			if (distance_orig > 40)
			{
				if ((distance_orig + 25) >= (previous_dest_length - this_->cmd_first->itm->dest_length))
				{
					// we are still within 25 meters of the previous navigation command item
					dont_speak_yet = 1;
					dbg(0, "NCC_:too close to last command, speak later level=%d level_cur=%d back-distance=%d\n", level, level_cur, (previous_dest_length - this_->cmd_first->itm->dest_length));
				}
			}
#if 1
			else if (distance_orig > 20)
			{
				if ((distance_orig) > (previous_dest_length - this_->cmd_first->itm->dest_length))
				{
					// we are still at the position of the previous navigation command item
					dont_speak_yet = 1;
					dbg(0, "NCC_:too close to last command, speak later level=%d level_cur=%d back-distance=%d\n", level, level_cur, (previous_dest_length - this_->cmd_first->itm->dest_length));
				}
			}
#endif
		}
#endif

		//dbg(0, "NCC_:1---passed item?---\n");
		//dbg(0, "NCC_:\n");
		



		//dbg(0, "xx 002 d=%d rd=%d dt=%d l=%d lcur=%d ll=%d\n", distance_orig, distance, this_->distance_turn, level, level_cur, this_->level_last);

		if (level < this_->level_last)
		{
			/* only tell if the level is valid for more than 3 seconds */
			int speed_distance = this_->first->speed * 30 / 36;

			dbg(0, "NCC_:speed_distance=%d\n", speed_distance);


			//dbg(0, "xx 003 speed_distance=%d this_->first->speed=%d globalslp=%d\n", speed_distance, this_->first->speed, global_spoke_last_position_update);

			if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
			{

				level = level_cur;

				if ((level_cur > 0) && (global_spoke_last_position_update > 0))
				{
					// skip this time, speak command on the next update
					dbg(0, "NCC_:SKIP[1] this update, speak next update (%d)! level=%d level_cur=%d\n", global_spoke_last_position_update, level, level_cur);
				}
				else
				{
					this_->level_last = level_cur;
					force_speech = 3;
					dbg(0, "NCC_:force_speech(2)=%d\n", force_speech);
				}
			}
			else
			{
				if (distance < speed_distance || navigation_get_announce_level_cmd(this_, this_->first, this_->cmd_first, distance - speed_distance, ((float)cur_vehicle_speed / 3.6f) ) == level)
				{
					//dbg(0, "distance %d speed_distance %d\n", distance, speed_distance);
					//dbg(0, "level %d < %d\n", level, this_->level_last);
					this_->level_last = level;
					force_speech = 3;
					dbg(0, "NCC_:force_speech(3)=%d\n", force_speech);
					//dbg(0, "xx 004\n");
				}
			}
		}


		if (!item_is_equal(this_->cmd_first->itm->way.item, this_->item_last))
		{
			//dbg(0, "xx 005\n");

			this_->item_last = this_->cmd_first->itm->way.item;
			this_->level_last = 99; // new item, reset command level

			dbg(0, "NCC_:Ni---new navigation command item!!---\n");

			if (this_->delay)
			{
				this_->curr_delay = this_->delay;
			}
			else
			{
//				if ((level_cur > 0) && (global_spoke_last_position_update > 0) && (distance_orig > 50))
//				{
//					// skip this time, speak command on the next update
//					dbg(0, "NCC_:SKIP[2] speech this update, speak at next update (%d)!\n", global_spoke_last_position_update);
//				}
//				else
//				{
					force_speech = 5;
					dbg(0, "NCC_:force_speech(4)=%d\n", force_speech);
//				}
			}
		}
		else
		{
			//dbg(0, "xx 006\n");

			if (this_->curr_delay)
			{
				this_->curr_delay--;

				if (!this_->curr_delay)
				{
					force_speech = 4;
					dbg(0, "NCC_:force_speech(5)=%d\n", force_speech);
				}
			}
		}
	}

	if (global_spoke_last_position_update > 0)
	{
		//dbg(0,"globalslp(1)=%d\n", global_spoke_last_position_update);
		global_spoke_last_position_update--;
		//dbg(0,"globalslp(2)=%d\n", global_spoke_last_position_update);
	}


#if 0
	//dbg(0,"XA 000\n");
	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)) // bicycle mode
	{
		if ((this_->previous) && (!item_is_equal(this_->cmd_first->itm->way.item, this_->previous->way.item)))
		{
			// item has changed, check if we missed a spoken level 0 command
			if (this_->previous->told == 0)
			{
				dbg(0, "NCC_:MISSED a level 0 spoken command!\n");

				char *dir2 = _("right");
				int delta2 = this_->cmd_previous->delta;
				dbg(0, "NCC_:missed delta = %d\n", delta2);
				if (delta2 < 0)
				{
					// TRANSLATORS: left, as in 'Turn left'
					dir2 = _("left");
					delta2 = -delta2;
				}

				if (delta2 > 20)
				{
					dbg(0,"NCC_:XA 002a\n");
					navit_say(this_->navit, dir2);
					this_->previous->told = 1;
				}
			}
		}
	}
#endif


	this_->previous = this_->cmd_first->itm;
	this_->cmd_previous = this_->cmd_first;

#if 1
	if ((dont_speak_yet == 1) && (force_speech))
	{
		dbg(0,"NCC_:this_->level_last = 99\n");
		this_->level_last = 99;
	}
#endif

	if (global_driven_away_from_route == 1)
	{
		dbg(0,"NCC_:** NEW PATH **\n");
		global_driven_away_from_route = 0;
		global_spoke_last_position_update = 0;
		global_last_spoken = -1;
		previous_dest_length = -1;
		previous_dest_length_save = -1;
		last_first_cmd_save = NULL;

		this_->level_last = 99;
	}

	if ( (dont_speak_yet == 0) || ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)) )
	{
		if (force_speech)
		{
			this_->level_last = level;
			this_->curr_delay = 0;

			dbg(0, "NCC_:force_speech=%d distance=%d level=%d global_spoke_last_position_update=%d\n", force_speech, distance, level, global_spoke_last_position_update);
			dbg(0, "NCC_:========= END ==========\n");

			global_spoke_last_position_update = 1;

			// ----------------------------------
			// ----------------------------------
			// calls -> calls navit_speak
			//
			callback_list_call(this_->callback_speech, 1, &p);
			// ----------------------------------
			// ----------------------------------
		}
	}

__F_END__
}


// ----------- main place where navigation commands are generated ------------
// ----------- main place where navigation commands are generated ------------
// ----------- main place where navigation commands are generated ------------
static void navigation_update(struct navigation *this_, struct route *route, struct attr *attr)
{
__F_START__

	struct map *map;
	struct map_rect *mr;
	struct item *ritem; /* Holds an item from the route map */
	struct item *sitem; /* Holds the corresponding item from the actual map */
	struct attr street_item, street_direction;
	struct navigation_itm *itm;
	struct attr vehicleprofile;
	int mode = 0, incr = 0, first = 1;

	//dbg(0, "Enter\n");

	if (attr->type != attr_route_status)
	{
		//dbg(0, "return 001\n");
		return2;
	}

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "RS:001:route_status=%s\n", route_status_to_name(attr->u.num));
#endif


	dbg(0, "NCC_:NU:RRRRSSSSSS::route_status=%s\n", route_status_to_name(attr->u.num));

	if (attr->u.num == route_status_path_done_new)
	{
		// route calculated new, you drove a different path?
		global_driven_away_from_route = 1;
	}


	//dbg(1, "enter %d\n", mode);
	if (attr->u.num == route_status_no_destination || attr->u.num == route_status_not_found || attr->u.num == route_status_path_done_new)
	{
		dbg(0, "NCC_:NU:navigation_flush\n");
		navigation_flush(this_);
	}

	if (attr->u.num != route_status_path_done_new && attr->u.num != route_status_path_done_incremental)
	{
		//dbg(0, "return 002\n");
		dbg(0, "NCC_:NU:ret 001\n");

		return2;
	}

	if (!this_->route)
	{
		//dbg(0, "return 003\n");
		dbg(0, "NCC_:NU:ret 002\n");

		return2;
	}

	map = route_get_map(this_->route);
	if (!map)
	{
		//dbg(0, "return 004\n");
		return2;
	}

	mr = map_rect_new(map, NULL);

	if (!mr)
	{
		//dbg(0, "return 005\n");
		return2;
	}

	if (route_get_attr(route, attr_vehicleprofile, &vehicleprofile, NULL))
	{
		this_->vehicleprofile = vehicleprofile.u.vehicleprofile;
	}
	else
	{
		this_->vehicleprofile = NULL;
	}

	//dbg(1,"enter\n");

	dbg(0, "NCC_:NU:------ LOOP START ------\n");

	int first_item = 1;
	this_->turn_around = 0;

	while ((ritem = map_rect_get_item(mr)))
	{

		dbg(0, "NAVR:001:%s\n", item_to_name(ritem->type));

#if 0
		if (ritem->type == type_route_start && this_->turn_around > -this_->turn_around_limit + 1)
		{
			this_->turn_around--;
		}

		if (ritem->type == type_route_start_reverse && this_->turn_around < this_->turn_around_limit)
		{
			this_->turn_around++;
			dbg(0, "NAVR:001.1:** TURN AROUND **\n");
		}
#endif

#if 1
		if (first_item == 1)
		{
			first_item = 0;
			if (ritem->type == type_route_start_reverse)
			{
				this_->turn_around = this_->turn_around_limit;

				dbg(0, "NAV_TURNAROUND:006 ta=%d talimit=%d\n", this_->turn_around, this_->turn_around_limit);

				dbg(0, "NAVR:001.1:T_A:** TURN AROUND **:%s\n", item_to_name(ritem->type));
			}
			else
			{
				dbg(0, "NAVR:001.1:T_A:first item=%s\n", item_to_name(ritem->type));
			}
		}
#endif


		dbg(0, "NAVR:001:X1:%s\n", item_to_name(ritem->type));
		if ((ritem->type != type_street_route) && (ritem->type != type_street_route_waypoint))
		{
			dbg(0, "NAVR:001:X2R:%s\n", item_to_name(ritem->type));
			continue;
		}
		dbg(0, "NAVR:001:X3:%s\n", item_to_name(ritem->type));

		if (first && item_attr_get(ritem, attr_street_item, &street_item))
		{
			first = 0;
			if (!item_attr_get(ritem, attr_direction, &street_direction))
			{
				street_direction.u.num = 0;
			}

			sitem = street_item.u.item;
			//dbg(1,"sitem=%p\n", sitem);
			itm = item_hash_lookup(this_->hash, sitem);
			//dbg(2,"itm for item with id (0x%x,0x%x) is %p\n", sitem->id_hi, sitem->id_lo, itm);

			if (itm && itm->way.dir != street_direction.u.num)
			{
				//dbg(2,"wrong direction\n");
				dbg(0, "NAVR:001.2:*+ WRONG DIRECTION +*\n");
				itm = NULL;
			}

			dbg(0, "NCC_:NU:navigation_destroy_itms_cmds\n");
			navigation_destroy_itms_cmds(this_, itm);

			if (itm)
			{
				dbg(0, "NCC_:NU:navigation_itm_update\n");

				navigation_itm_update(itm, ritem);
				break;
			}
			//dbg(1,"not on track\n");
		}

		dbg(0, "NAVR:001:X4:%s\n", item_to_name(ritem->type));
		dbg(0, "NCC_:NU:navigation_itm_new\n");
		navigation_itm_new(this_, ritem);
		dbg(0, "NAVR:001:X5:%s\n", item_to_name(ritem->type));

	}


	dbg(0, "NCC_:NU:------ LOOP END ------\n");


	//dbg(2, "turn_around=%d\n", this_->turn_around);

	if (first)
	{

#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
		dbg(0, "DEST::route_clear_freetext_list\n");
		route_clear_freetext_list();
#endif

		dbg(0, "NCC_:NU:navigation_destroy_itms_cmds[first]\n");

		navigation_destroy_itms_cmds(this_, NULL);
	}
	else
	{
		if (!ritem)
		{
			dbg(0, "NCC_:NU:navigation_itm_new[!ritem]\n");
			navigation_itm_new(this_, NULL);
			//dbg(0, "Enter: make_maneuvers\n");
			dbg(0, "NAVR:001.2:*= MAKE MANEUVERS =*\n");
			dbg(0, "NCC_:NU:make_maneuvers[!ritem]\n");
			make_maneuvers(this_, this_->route);
			//dbg(0, "end  : make_maneuvers\n");
		}

		dbg(0, "NCC_:NU:calculate_dest_distance\n");
		calculate_dest_distance(this_, incr);

		// calls navit_speak later !! ----------
		navigation_call_callbacks(this_, FALSE);
		// calls navit_speak later !! ----------
	}
	map_rect_destroy(mr);

__F_END__
}
// ----------- main place where navigation commands are generated ------------
// ----------- main place where navigation commands are generated ------------
// ----------- main place where navigation commands are generated ------------


static void navigation_flush(struct navigation *this_)
{
__F_START__
	navigation_destroy_itms_cmds(this_, NULL);
__F_END__
}

void navigation_destroy(struct navigation *this_)
{
	navigation_flush(this_);
	item_hash_destroy(this_->hash);
	callback_list_destroy(this_->callback);
	callback_list_destroy(this_->callback_speech);
	g_free(this_);
}

int navigation_register_callback(struct navigation *this_, enum attr_type type, struct callback *cb)
{
__F_START__

	if (type == attr_navigation_speech)
	{
		callback_list_add(this_->callback_speech, cb);
	}
	else
	{
		callback_list_add(this_->callback, cb);
	}

	return2 1;

__F_END__
}

void navigation_unregister_callback(struct navigation *this_, enum attr_type type, struct callback *cb)
{
__F_START__

	if (type == attr_navigation_speech)
	{
		callback_list_remove_destroy(this_->callback_speech, cb);
	}
	else
	{
		callback_list_remove_destroy(this_->callback, cb);
	}

__F_END__
}

struct map *
navigation_get_map(struct navigation *this_)
{
	struct attr *attrs[5];
	struct attr type, navigation, data, description;
	type.type = attr_type;
	type.u.str = "navigation";
	navigation.type = attr_navigation;
	navigation.u.navigation = this_;
	data.type = attr_data;
	data.u.str = "";
	description.type = attr_description;
	description.u.str = "Navigation";

	attrs[0] = &type;
	attrs[1] = &navigation;
	attrs[2] = &data;
	attrs[3] = &description;
	attrs[4] = NULL;

	if (!this_->map)
	{
		this_->map = map_new(NULL, attrs);
	}

	return this_->map;
}

struct map_priv
{
	struct navigation *navigation;
};

struct map_rect_priv
{
	struct navigation *nav;
	struct navigation_command *cmd;
	struct navigation_command *cmd_next;
	struct navigation_itm *itm;
	struct navigation_itm *itm_next;
	struct navigation_itm *cmd_itm;
	struct navigation_itm *cmd_itm_next;
	struct item item;
	enum attr_type attr_next;
	int ccount;
	int debug_idx;
	struct navigation_way *ways;
	int show_all;
	char *str;
};

static int navigation_map_item_coord_get(void *priv_data, struct coord *c, int count)
{
	struct map_rect_priv *this = priv_data;

	if (this->ccount || !count)
	{
		// dbg(0, "NAVICG:return 001 %d %d\n", this->ccount, count);
		return 0;
	}

	if (this->item.type == type_nav_waypoint)
	{
		if (this->itm->way.dir == 99)
		{
			dbg(0, "NAVICG:waypoint:END of seg\n");
			*c = this->itm->end;
		}
		else
		{
			dbg(0, "NAVICG:waypoint:start of seg\n");
			*c = this->itm->start;
		}
	}
	else
	{
		// dbg(0, "NAVICG:normal type=%s cc=%d\n", item_to_name(this->item.type), this->ccount);
		*c = this->itm->start;
	}

	this->ccount = 1;

	return 1;
}

static int navigation_map_item_attr_get(void *priv_data, enum attr_type attr_type, struct attr *attr)
{
	struct map_rect_priv *this_ = priv_data;
	struct navigation_command *cmd = this_->cmd;
	struct navigation_itm *itm = this_->itm;
	struct navigation_itm *prev = itm->prev;
	attr->type = attr_type;

	if (this_->str)
	{
		g_free(this_->str);
		this_->str = NULL;
	}

	if (cmd)
	{
		if (cmd->itm != itm)
		{
			cmd = NULL;
		}
	}

	switch (attr_type)
	{
		case attr_navigation_short:
			this_->attr_next = attr_navigation_long;
			if (cmd)
			{
				//dbg(0, "attr_navigation_short\n");
				this_->str = attr->u.str = show_next_maneuvers(this_->nav, this_->cmd_itm, cmd, attr_type);
				return 1;
			}
			return 0;
		case attr_navigation_long:
			this_->attr_next = attr_navigation_long_exact;
			if (cmd)
			{
				//dbg(0, "attr_navigation_long\n");
				this_->str = attr->u.str = show_next_maneuvers(this_->nav, this_->cmd_itm, cmd, attr_type);
				return 1;
			}
			return 0;
		case attr_navigation_long_exact:
			this_->attr_next = attr_navigation_speech;
			if (cmd)
			{
				//dbg(0, "attr_navigation_long_exact\n");
				this_->str = attr->u.str = show_next_maneuvers(this_->nav, this_->cmd_itm, cmd, attr_type);
				return 1;
			}
			return 0;
		case attr_navigation_speech:
			this_->attr_next = attr_length;
			if (cmd)
			{
				//dbg(0, "attr_navigation_speech\n");
				//dbg(0, "Enter: attr_navigation_speech\n");
				this_->str = attr->u.str = show_next_maneuvers(this_->nav, this_->cmd_itm, this_->cmd, attr_type);
				//dbg(0, "back : attr_navigation_speech\n");
				return 1;
			}
			else if (this_->nav->turn_around_limit && this_->nav->turn_around == this_->nav->turn_around_limit)
			{
				if (global_turn_around_spoken == 0)
				{
					this_->str = attr->u.str = g_strdup(_("When possible, please turn around"));
					global_turn_around_spoken = 1;
					return 1;
				}
				else
				{
					return 0;
				}
			}
			return 0;
		case attr_length:
			this_->attr_next = attr_time;
			if (cmd)
			{
				attr->u.num = this_->cmd_itm->dest_length - cmd->itm->dest_length;
				return 1;
			}
			return 0;
		case attr_time:
			this_->attr_next = attr_destination_length;
			if (cmd)
			{
				attr->u.num = this_->cmd_itm->dest_time - cmd->itm->dest_time;
				return 1;
			}
			return 0;

		case attr_destination_length:
			attr->u.num = itm->dest_length;
			this_->attr_next = attr_destination_time;
			return 1;

		case attr_destination_time:
			attr->u.num = itm->dest_time;
			this_->attr_next = attr_street_name;
			return 1;

		case attr_street_name:
			attr->u.str = itm->way.name1;
			this_->attr_next = attr_street_name_systematic;
			if (attr->u.str)
				return 1;
			return 0;

		case attr_street_name_systematic:
			attr->u.str = itm->way.name2;
			this_->attr_next = attr_street_destination;
			if (attr->u.str)
				return 1;
			return 0;

		case attr_street_destination:
			attr->u.str = itm->way.street_dest_text;
			this_->attr_next = attr_debug;
			if (attr->u.str)
			{
				return 1;
			}
			return 0;

		case attr_debug:
			switch (this_->debug_idx)
			{
				case 0:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("angle:%d (- %d)", itm->way.angle2, itm->angle_end);
					return 1;
				case 1:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("item type:%s", item_to_name(itm->way.item.type));
					return 1;
				case 2:
					this_->debug_idx++;
					if (cmd)
					{
						this_->str = attr->u.str = g_strdup_printf("delta:%d", cmd->delta);
						return 1;
					}
				case 3:
					this_->debug_idx++;
					if (prev)
					{
						this_->str = attr->u.str = g_strdup_printf("prev street_name:%s", prev->way.name1);
						return 1;
					}
				case 4:
					this_->debug_idx++;
					if (prev)
					{
						this_->str = attr->u.str = g_strdup_printf("prev street_name_systematic:%s", prev->way.name2);
						return 1;
					}
				case 5:
					this_->debug_idx++;
					if (prev)
					{
						this_->str = attr->u.str = g_strdup_printf("prev angle:(%d -) %d", prev->way.angle2, prev->angle_end);
						return 1;
					}
				case 6:
					this_->debug_idx++;
					this_->ways = itm->way.next;
					if (prev)
					{
						this_->str = attr->u.str = g_strdup_printf("prev item type:%s", item_to_name(prev->way.item.type));
						return 1;
					}
				case 7:
					if (this_->ways && prev)
					{
						this_->str = attr->u.str = g_strdup_printf("other item angle:%d delta:%d flags:%d dir:%d type:%s id:(0x%x,0x%x)", this_->ways->angle2, angle_delta(prev->angle_end, this_->ways->angle2), this_->ways->flags, this_->ways->dir, item_to_name(this_->ways->item.type), this_->ways->item.id_hi, this_->ways->item.id_lo);
						this_->ways = this_->ways->next;
						return 1;
					}
					this_->debug_idx++;
				case 8:
					this_->debug_idx++;
					if (prev)
					{
						int delta = 0;
						int delta_real = 0;
						char *reason = NULL;
						maneuver_required2(this_->nav, prev, itm, &delta, &delta_real, &reason);
						this_->str = attr->u.str = g_strdup_printf("reason:%s", reason);
						return 1;
					}

				default:
					this_->attr_next = attr_none;
					return 0;
			}

		case attr_any:
			while (this_->attr_next != attr_none)
			{
				if (navigation_map_item_attr_get(priv_data, this_->attr_next, attr))
					return 1;
			}
			return 0;

		default:
			attr->type = attr_none;
			return 0;
	}
}

static struct item_methods navigation_map_item_methods = { NULL, navigation_map_item_coord_get, NULL, navigation_map_item_attr_get, };

static void navigation_map_destroy(struct map_priv *priv)
{
	g_free(priv);
}

static void navigation_map_rect_init(struct map_rect_priv *priv)
{
	priv->cmd_next = priv->nav->cmd_first;
	priv->cmd_itm_next = priv->itm_next = priv->nav->first;
}

static struct map_rect_priv *
navigation_map_rect_new(struct map_priv *priv, struct map_selection *sel)
{
	struct navigation *nav = priv->navigation;
	struct map_rect_priv *ret=g_new0(struct map_rect_priv, 1);
	ret->nav = nav;

	navigation_map_rect_init(ret);

	ret->item.meth = &navigation_map_item_methods;
	ret->item.priv_data = ret;

#ifdef DEBUG
	ret->show_all=1;
#endif

	return ret;
}

static void navigation_map_rect_destroy(struct map_rect_priv *priv)
{
	g_free(priv);
}



void navigation_dump_items(struct navigation *this_)
{
	if (this_->first)
	{
		dbg(0, "NAVR:dump:=================++++++++++++================\n");
		struct navigation_itm *i;
		int count = 0;

		i = this_->first;
		while (i)
		{
			count++;
			i = i->next;
		}

		i = this_->first;
		while (i)
		{
			count--;
			dbg(0, "NAVR:dump:count=%d %p %d\n", count, i, i->way.dir);
			i = i->next;
		}
		dbg(0, "NAVR:dump:=================++++++++++++================\n");
	}
}


// -----------------------------------
static int save_last_dest_count = -1;
// -----------------------------------

static struct item *
navigation_map_get_item(struct map_rect_priv *priv)
{
	struct item *ret = &priv->item;
	int delta;

	int fake_dir1;
	int fake_dir2;

	dbg(0, "NAVR:ROUTE:Enter:\n");
	dbg(0, "NAVR:ROUTE:Enter:------------------------\n");

	if (!priv->itm_next)
	{
		dbg(0, "NAVR:ROUTE:006:0000\n");
		return NULL;
	}

	int stepped_to_waypoint = 0;

	// now check if we missed a waypoint, if so -> move back to waypoint -------------------------
	if (priv->itm)
	{
		if (save_last_dest_count != -1)
		{
			int count_back = (save_last_dest_count - priv->itm->dest_count - 1);
			int count_forw = count_back;
			dbg(0, "NAVR:ROUTE:006:cback:%d = (%d - %d - 1)\n", count_back, save_last_dest_count, priv->itm->dest_count);

			struct navigation_itm *i = priv->itm;

			// go to first item to check
			while (count_back > 0)
			{
				count_back--;
				if (i->prev)
				{
					i = i->prev;
					dbg(0, "NAVR:ROUTE:006:cback:stepping back to item #%d dir=%d\n", i->dest_count, i->way.dir);
				}
			}

			// now step forward until waypoint found, or just skip the whole thing if we dont find any waypoint
			while (count_forw > 0)
			{
				if (i)
				{
					dbg(0, "NAVR:ROUTE:006:cback:stepping forw to item #%d dir=%d\n", i->dest_count, i->way.dir);

					if ((i->way.dir == 99) || (i->way.dir == -99))
					{
						// found a waypoint
						priv->itm = i;
						stepped_to_waypoint = 1;

						dbg(0, "NAVR:ROUTE:006:cback:stepping found waypoint item #%d dir=%d itm=%p\n", priv->itm->dest_count, priv->itm->way.dir, priv->itm);

						break;
					}

					count_forw--;
					if (i->next)
					{
						i = i->next;
					}
				}
			}

		}
	}
	// now check if we missed a waypoint, if so -> move back to waypoint -------------------------


	if (priv->itm)
	{
		dbg(0, "NAVR:ROUTE:006:8888:1: %p dir=%d DST_COUNT(1)=%d\n", priv->itm, priv->itm->way.dir, priv->itm->dest_count);
		fake_dir1 = priv->itm->way.dir;
		save_last_dest_count = priv->itm->dest_count;
	}
	else
	{
		dbg(0, "NAVR:ROUTE:006:8888:1: NULL\n");
		fake_dir1 = 0;
		save_last_dest_count = -1;
	}

	if (stepped_to_waypoint != 1)
	{
		priv->itm = priv->itm_next;
	}


	fake_dir2 = priv->itm->way.dir;
	dbg(0, "NAVR:ROUTE:006:8888:2: %p dir=%d DST_COUNT(2)=%d\n", priv->itm, priv->itm->way.dir, priv->itm->dest_count);
	priv->cmd = priv->cmd_next;
	priv->cmd_itm = priv->cmd_itm_next;

	if ((priv->itm->way.dir == 99) || (priv->itm->way.dir == -99))
	{
		// return fake waypoint item!! ---------
		ret->type = type_nav_waypoint;
		ret->id_lo = priv->itm->dest_count;
		priv->ccount = 0;

		priv->itm_next = priv->itm->next;

		dbg(0, "NAVR:ROUTE:006:fake:%s dir=%d ,,,,,\n", item_to_name(ret->type), priv->itm->way.dir);

		return ret;
	}



	// navigation_dump_items(priv->nav);

	if (!priv->cmd)
	{
		dbg(0, "NAVR:ROUTE:006:1111\n");
		return NULL;
	}


	if (!priv->show_all && priv->itm->prev != NULL)
	{
		dbg(0, "NAVR:ROUTE:006:112a\n");
		priv->itm = priv->cmd->itm;
	}

	priv->itm_next = priv->itm->next;

	if (priv->itm->prev)
	{
		ret->type = type_nav_none;
	}
	else
	{
		ret->type = type_nav_position;
	}

	dbg(0, "NAVR:ROUTE:006:2222 %p dir=%d\n", priv->itm, priv->itm->way.dir);

	if (priv->cmd->itm == priv->itm)
	{

		dbg(0, "NAVR:ROUTE:006:3333 %p dir=%d %p\n", priv->itm, priv->itm->way.dir, route_get_map(global_navit->route));
		// item_dump_coords(priv->itm, route_get_map(global_navit->route));

		priv->cmd_itm_next = priv->cmd->itm;
		priv->cmd_next = priv->cmd->next;

		if (priv->cmd_itm_next && !priv->cmd_itm_next->next)
		{
			ret->type = type_nav_destination;
		}
		else
		{
			if (priv->itm && priv->itm->prev && !(priv->itm->way.flags & NAVIT_AF_ROUNDABOUT) && (priv->itm->prev->way.flags & NAVIT_AF_ROUNDABOUT))
			{
				enum item_type r = type_none, l = type_none;
				switch (((180 + 22) - priv->cmd->roundabout_delta) / 45)
				{
					case 0:
					case 1:
						r = type_nav_roundabout_r1;
						l = type_nav_roundabout_l7;
						break;
					case 2:
						r = type_nav_roundabout_r2;
						l = type_nav_roundabout_l6;
						break;
					case 3:
						r = type_nav_roundabout_r3;
						l = type_nav_roundabout_l5;
						break;
					case 4:
						r = type_nav_roundabout_r4;
						l = type_nav_roundabout_l4;
						break;
					case 5:
						r = type_nav_roundabout_r5;
						l = type_nav_roundabout_l3;
						break;
					case 6:
						r = type_nav_roundabout_r6;
						l = type_nav_roundabout_l2;
						break;
					case 7:
						r = type_nav_roundabout_r7;
						l = type_nav_roundabout_l1;
						break;
					case 8:
						r = type_nav_roundabout_r8;
						l = type_nav_roundabout_l8;
						break;
				}
				// dbg(0, "delta %d\n", priv->cmd->delta);

				if (priv->cmd->delta < 0)
				{
					ret->type = l;
				}
				else
				{
					ret->type = r;
				}
			}
			else
			{
				delta = priv->cmd->delta;
				if (delta < 0)
				{
					delta = -delta;
					if (delta < 45)
						ret->type = type_nav_left_1;
					else if (delta < 105)
						ret->type = type_nav_left_2;
					else if (delta < 165)
						ret->type = type_nav_left_3;
					else
						ret->type = type_none;
				}
				else if (delta > 0)
				{
					if (delta < 45)
						ret->type = type_nav_right_1;
					else if (delta < 105)
						ret->type = type_nav_right_2;
					else if (delta < 165)
						ret->type = type_nav_right_3;
					else
						ret->type = type_none;
				}
				else // delta == 0
				{
					ret->type = type_nav_straight;
				}
			}
		}
	}

	priv->ccount = 0;
	priv->debug_idx = 0;
	priv->attr_next = attr_navigation_short;

	ret->id_lo = priv->itm->dest_count;

	// dbg(0, "NAVR:ROUTE:006:%s ,,,,, ta=%d talim=%d\n", item_to_name(ret->type), priv->nav->turn_around, priv->nav->turn_around_limit);


	// check for "turn around" and return "type_nav_turnaround" !! ---------------
	if ((priv->nav->turn_around && priv->nav->turn_around == priv->nav->turn_around_limit) && (ret->type == type_nav_position))
	{
		// dbg(0, "priv->itm->dest_count=%d\n", priv->itm->dest_count);
		ret->type = type_nav_turnaround_right;
	}
	// check for "turn around" and return "type_nav_turnaround" !! ---------------

	// dbg(0, "NAVR:ROUTE:007a:%s ,,,,, priv->itm->dest_count=%d\n", item_to_name(ret->type), priv->itm->dest_count);

	return ret;
}

static struct item *
navigation_map_get_item_byid(struct map_rect_priv *priv, int id_hi, int id_lo)
{
	struct item *ret;
	navigation_map_rect_init(priv);
	while ((ret = navigation_map_get_item(priv)))
	{
		if (ret->id_hi == id_hi && ret->id_lo == id_lo)
			return ret;
	}
	return NULL;
}

static struct map_methods navigation_map_meth = { projection_mg, "utf-8", navigation_map_destroy, navigation_map_rect_new, navigation_map_rect_destroy, navigation_map_get_item, navigation_map_get_item_byid, NULL, NULL, NULL, };

struct map_priv *
navigation_map_new(struct map_methods *meth, struct attr **attrs, struct callback_list *cbl)
{
	struct map_priv *ret;
	struct attr *navigation_attr;

	navigation_attr = attr_search(attrs, NULL, attr_navigation);

	if (!navigation_attr)
	{
		return NULL;
	}

	ret=g_new0(struct map_priv, 1);
	*meth = navigation_map_meth;
	ret->navigation = navigation_attr->u.navigation;

	level_static_for_bicycle[0] = 22; // in meters
	level_static_for_bicycle[1] = 100; // in meters
	level_static_for_bicycle[2] = -1; // dont announce

	return ret;
}

void navigation_set_route(struct navigation *this_, struct route *route)
{
__F_START__

	struct attr callback;
	this_->route = route;
	this_->route_cb = callback_new_attr_1(callback_cast(navigation_update), attr_route_status, this_);
	callback_add_names(this_->route_cb, "navigation_set_route", "navigation_update");
	callback.type = attr_callback;
	callback.u.callback = this_->route_cb;
	route_add_attr(route, &callback);

__F_END__
}

void navigation_init(void)
{
#ifdef PLUGSSS
	plugin_register_map_type("navigation", navigation_map_new);
#endif
}


