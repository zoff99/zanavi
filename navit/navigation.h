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

#ifndef NAVIT_NAVIGATION_H
#define NAVIT_NAVIGATION_H

#define FEET_PER_METER 3.2808399
#define FEET_PER_MILE  5280

#ifdef __cplusplus
extern "C" {
#endif
/* prototypes */
enum attr_type;
enum item_type;
struct attr;
struct attr_iter;
struct callback;
struct map;

#include "item.h"
#include "coord.h"

/**
 * @brief Holds a way that one could possibly drive from a navigation item
 */
struct navigation_way
{
	struct navigation_way *next; /**< Pointer to a linked-list of all navigation_ways from this navigation item */
	short dir; /**< The direction -1 or 1 of the way */
	short angle2; /**< The angle one has to steer to drive from the old item to this street */
	int flags; /**< The flags of the way */
	struct item item; /**< The item of the way */
	char *name1; // = streetname
	char *name2; // = streetname systematic (road number e.g.: E51)

// -- NEW 002 --
//	char *name_systematic;			/**< The road number ({@code street_name_systematic} attribute, OSM: {@code ref}) */
	char *exit_ref;					/**< Exit_ref if found on the first node of the way*/
	char *exit_label;				/**< Exit_label if found on the first node of the way*/
	struct street_destination *s_destination;				/**< The destination this way leads to (OSM: {@code destination}) */
	char *street_dest_text; /* selected destination to display in GUI */
// -- NEW 002 --

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

struct navigation
{
	struct route *route;
	struct map *map;
	struct item_hash *hash;
	struct vehicleprofile *vehicleprofile;
	struct navigation_itm *first;
	struct navigation_itm *last;
	struct navigation_command *cmd_first;
	struct navigation_command *cmd_last;
	struct callback_list *callback_speech;
	struct callback_list *callback;
	struct navit *navit;
	struct speech *speech;
	int level_last;
	struct item item_last;
	int turn_around;
	int turn_around_limit;
	int distance_turn;
	struct callback *route_cb;
	int announce[route_item_last - route_item_first + 1][3];
	int tell_street_name;
	int delay;
	int curr_delay;
	struct navigation_itm *previous;
	struct navigation_command *cmd_previous;
};

struct route;
int navigation_get_attr(struct navigation *this_, enum attr_type type, struct attr *attr, struct attr_iter *iter);
int navigation_set_attr(struct navigation *this_, struct attr *attr);
struct navigation *navigation_new(struct attr *parent, struct attr **attrs);
int navigation_set_announce(struct navigation *this_, enum item_type type, int *level);
void navigation_destroy(struct navigation *this_);
int navigation_register_callback(struct navigation *this_, enum attr_type type, struct callback *cb);
void navigation_unregister_callback(struct navigation *this_, enum attr_type type, struct callback *cb);
struct map *navigation_get_map(struct navigation *this_);
void navigation_set_route(struct navigation *this_, struct route *route);
char *get_distance(struct navigation *nav, int dist, enum attr_type type, int is_length);
void navigation_init(void);
/* end of prototypes */
#ifdef __cplusplus
}
#endif

#endif
