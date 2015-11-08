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

/** @file
 * @brief Contains code related to finding a route from a position to a destination
 *
 * Routing uses segments, points and items. Items are items from the map: Streets, highways, etc.
 * Segments represent such items, or parts of it. Generally, a segment is a driveable path. An item
 * can be represented by more than one segment - in that case it is "segmented". Each segment has an
 * "offset" associated, that indicates at which position in a segmented item this segment is - a 
 * segment representing a not-segmented item always has the offset 1.
 * A point is located at the end of segments, often connecting several segments.
 * 
 * The code in this file will make navit find a route between a position and a destination.
 * It accomplishes this by first building a "route graph". This graph contains segments and
 * points.
 *
 * After building this graph in route_graph_build(), the function route_graph_flood() assigns every 
 * point and segment a "value" which represents the "costs" of traveling from this point to the
 * destination. This is done by Dijkstra's algorithm.
 *
 * When the graph is built a "route path" is created, which is a path in this graph from a given
 * position to the destination determined at time of building the graph.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if 0
#include <math.h>
#include <assert.h>
#include <unistd.h>
#include <sys/time.h>
#endif

#include "glib_slice.h"
#include "config.h"
#include "point.h"
#include "graphics.h"
#include "profile.h"
#include "coord.h"
#include "projection.h"
#include "item.h"
#include "map.h"
#include "mapset.h"
#include "route.h"
#include "track.h"
#include "transform.h"
#include "plugin.h"
#include "fib.h"
#include "event.h"
#include "callback.h"
#include "vehicle.h"
#include "vehicleprofile.h"
#include "roadprofile.h"
#include "debug.h"

#include "navit.h"



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



struct map_priv
{
	struct route *route;
};

int debug_route = 0;


#define RP_TRAFFIC_DISTORTION 1
#define RP_TURN_RESTRICTION 2
#define RP_TURN_RESTRICTION_RESOLVED 4
#define RP_TRAFFIC_LIGHT 8
#define RP_TRAFFIC_LIGHT_RESOLVED 16



#define RSD_OFFSET(x) *((int *)route_segment_data_field_pos((x), attr_offset))
#define RSD_MAXSPEED(x) *((int *)route_segment_data_field_pos((x), attr_maxspeed))
#define RSD_SIZE_WEIGHT(x) *((struct size_weight_limit *)route_segment_data_field_pos((x), attr_vehicle_width))
#define RSD_DANGEROUS_GOODS(x) *((int *)route_segment_data_field_pos((x), attr_vehicle_dangerous_goods))



/**
 * @brief A traffic distortion
 *
 * This is distortion in the traffic where you can't drive as fast as usual or have to wait for some time
 */
struct route_traffic_distortion
{
	int maxspeed; /**< Maximum speed possible in km/h */
	int delay; /**< Delay in tenths of seconds */
};

/**
 * @brief A segment in the route path
 *
 * This is a segment in the route path.
 */
struct route_path_segment
{
	struct route_path_segment *next; /**< Pointer to the next segment in the path */
	struct route_segment_data *data; /**< The segment data */
	int direction; /**< Order in which the coordinates are ordered. >0 means "First
	 *  coordinate of the segment is the first coordinate of the item", <=0
	 *  means reverse. */
	unsigned ncoords; /**< How many coordinates does this segment have? */
	struct coord c[0]; /**< Pointer to the ncoords coordinates of this segment */
/* WARNING: There will be coordinates following here, so do not create new fields after c! */
};

/**
 * @brief A complete route path
 *
 * This structure describes a whole routing path
 */
struct route_path
{
	int in_use; /**< The path is in use and can not be updated */
	int update_required; /**< The path needs to be updated after it is no longer in use */
	int updated; /**< The path has only been updated */
	int path_time; /**< Time to pass the path */
	int path_len; /**< Length of the path */
	struct route_path_segment *path; /**< The first segment in the path, i.e. the segment one should
	 *  drive in next */
	struct route_path_segment *path_last; /**< The last segment in the path */
	/* XXX: path_hash is not necessery now */
	struct item_hash *path_hash; /**< A hashtable of all the items represented by this route's segements */
	struct route_path *next; /**< Next route path in case of intermediate destinations */
};

/**
 * @brief A complete route
 * 
 * This struct holds all information about a route.
 */

/**
 * @brief A complete route graph
 *
 * This structure describes a whole routing graph
 */
struct route_graph
{
	int busy; /**< The graph is being built */
	struct map_selection *sel; /**< The rectangle selection for the graph */
	struct mapset_handle *h; /**< Handle to the mapset */
	struct map *m; /**< Pointer to the currently active map */
	struct map_rect *mr; /**< Pointer to the currently active map rectangle */
	struct vehicleprofile *vehicleprofile; /**< The vehicle profile */
	struct callback *idle_cb; /**< Idle callback to process the graph */
	struct callback *done_cb; /**< Callback when graph is done */
	struct event_idle *idle_ev; /**< The pointer to the idle event */
	struct route_graph_segment *route_segments; /**< Pointer to the first route_graph_segment in the linked list of all segments */
	// *ORIG* #define HASH_SIZE 8192
	//        #define HASH_SIZE 65536
#define HASH_SIZE 16384
	struct route_graph_point *hash[HASH_SIZE]; /**< A hashtable containing all route_graph_points in this graph */
};

#define HASHCOORD(c) ((((c)->x +(c)->y) * 2654435761UL) & (HASH_SIZE-1))

/**
 * @brief Iterator to iterate through all route graph segments in a route graph point
 *
 * This structure can be used to iterate through all route graph segments connected to a
 * route graph point. Use this with the rp_iterator_* functions.
 */
struct route_graph_point_iterator
{
	struct route_graph_point *p; /**< The route graph point whose segments should be iterated */
	int end; /**< Indicates if we have finished iterating through the "start" segments */
	struct route_graph_segment *next; /**< The next segment to be returned */
};

struct attr_iter
{
	union
	{
		GList *list;
	} u;
};

static struct route_info * route_find_nearest_street(struct vehicleprofile *vehicleprofile, struct mapset *ms, struct pcoord *c);
static struct route_info * route_find_nearest_street_harder(struct vehicleprofile *vehicleprofile, struct mapset *ms, struct pcoord *pc, int max_dist_wanted);
static struct route_graph_point *route_graph_get_point(struct route_graph *this, struct coord *c);
static void route_graph_update(struct route *this, struct callback *cb, int async);
static void route_graph_build_done(struct route_graph *rg, int cancel);
static struct route_path *route_path_new(struct route *rr, struct route_graph *this, struct route_path *oldpath, struct route_info *pos, struct route_info *dst, struct vehicleprofile *profile);
static void route_process_street_graph(struct route_graph *this, struct item *item, struct vehicleprofile *profile);
//static void route_graph_destroy(struct route_graph *this);
void route_path_update(struct route *this, int cancel, int async);
static int route_time_seg(struct vehicleprofile *profile, struct route_segment_data *over, struct route_traffic_distortion *dist);
static void route_graph_flood(struct route_graph *this, struct route_info *dst, struct route_info *pos, struct vehicleprofile *profile, struct callback *cb);
static void route_graph_reset(struct route_graph *this);


char *route_status_to_name(enum route_status s)
{
	if (s == route_status_no_destination)
	{
		return "route_status_no_destination";
	}
	else if (s == route_status_destination_set)
	{
		return "route_status_destination_set";
	}
	else if (s == route_status_not_found)
	{
		return "route_status_not_found";
	}
	else if (s == route_status_building_path)
	{
		return "route_status_building_path";
	}
	else if (s == route_status_building_graph)
	{
		return "route_status_building_graph";
	}
	else if (s == route_status_path_done_new)
	{
		return "route_status_path_done_new";
	}
	else if (s == route_status_path_done_incremental)
	{
		return "route_status_path_done_incremental";
	}

	return NULL; 
}


/**
 * @brief Returns the projection used for this route
 *
 * @param route The route to return the projection for
 * @return The projection used for this route
 */
static enum projection route_projection(struct route *route)
{
	struct street_data *street;
	struct route_info *dst = route_get_dst(route);
	if (!route->pos && !dst)
	{
		return projection_none;
	}
	street = route->pos ? route->pos->street : dst->street;
	if (!street || !street->item.map)
	{
		return projection_none;
	}
	return map_projection(street->item.map);
}

/**
 * @brief Creates a new graph point iterator 
 *
 * This function creates a new route graph point iterator, that can be used to
 * iterate through all segments connected to the point.
 *
 * @param p The route graph point to create the iterator from
 * @return A new iterator.
 */
static struct route_graph_point_iterator rp_iterator_new(struct route_graph_point *p)
{
	//// dbg(0, "enter\n");

	struct route_graph_point_iterator it;

	it.p = p;
	if (p->start)
	{
		it.next = p->start;
		it.end = 0;
	}
	else
	{
		it.next = p->end;
		it.end = 1;
	}

	return it;
}

/**
 * @brief Gets the next segment connected to a route graph point from an iterator
 *
 * @param it The route graph point iterator to get the segment from
 * @return The next segment or NULL if there are no more segments
 */
static struct route_graph_segment *rp_iterator_next(struct route_graph_point_iterator *it)
{
	//// dbg(0, "enter\n");

	struct route_graph_segment *ret;

	ret = it->next;
	if (!ret)
	{
		return NULL;
	}

	if (!it->end)
	{
		if (ret->start_next)
		{
			it->next = ret->start_next;
		}
		else
		{
			it->next = it->p->end;
			it->end = 1;
		}
	}
	else
	{
		it->next = ret->end_next;
	}

	return ret;
}

/**
 * @brief Checks if the last segment returned from a route_graph_point_iterator comes from the end
 *
 * @param it The route graph point iterator to be checked
 * @return 1 if the last segment returned comes from the end of the route graph point, 0 otherwise
 */
static int rp_iterator_end(struct route_graph_point_iterator *it)
{
	//// dbg(0, "enter\n");

	if (it->end && (it->next != it->p->end))
	{
		return 1;
	}
	else
	{
		return 0;
	}
}

/**
 * @brief Destroys a route_path
 *
 * @param this The route_path to be destroyed
 */
void route_path_destroy(struct route_path *this, int recurse)
{
__F_START__

	struct route_path_segment *c, *n;
	struct route_path *next;

	while (this)
	{
		next = this->next;
		if (this->path_hash)
		{
			item_hash_destroy(this->path_hash);
			this->path_hash = NULL;
		}
		c = this->path;
		while (c)
		{
			n = c->next;
			g_free(c);
			c = n;
		}

		this->in_use--;

		if (!this->in_use)
			g_free(this);

		if (!recurse)
			break;

		this = next;
	}

__F_END__
}

/**
 * @brief Creates a completely new route structure
 *
 * @param attrs Not used
 * @return The newly created route
 */
struct route *
route_new(struct attr *parent, struct attr **attrs)
{
__F_START__

	struct route *this=g_new0(struct route, 1);
	struct attr dest_attr;

	if (attr_generic_get_attr(attrs, NULL, attr_destination_distance, &dest_attr, NULL))
	{
		this->destination_distance = dest_attr.u.num;
	}
	else
	{
		this->destination_distance = 50; // Default value
	}
	this->cbl2 = callback_list_new("route_new:this->cbl2");

	return2 this;
	//dbg(0, "return(%d)\n", __LINE__);return this;

__F_END__
}

int route_get_real_oneway_mask(int road_flag, int mask)
{
	//dbg(0, "road_flag = %x mask = %x gvp = %d\n", road_flag, mask, global_vehicle_profile);

	if (global_vehicle_profile == 2) // bicycle no one-ways
	{
		// dont care about one-ways
		return mask;
	}
	else if (global_vehicle_profile == 1) // bicycle
	{
		if (road_flag & NAVIT_AF_ONEWAY_BICYCLE_NO)
		{
			// one-ways does not apply to bicycles here
			//dbg(0, "ret01m = %x\n", (mask & ~NAVIT_AF_ONEWAYMASK));
			return (mask & ~NAVIT_AF_ONEWAYMASK);
		}
		else
		{
			//dbg(0, "ret02 = %x\n", mask);
			return mask;
		}
	}
	else // normal one-way handling
	{
		//dbg(0, "ret03 = %x\n", (mask));
		return mask;
	}
}

int route_get_real_oneway_flag(int road_flag, int oneway_flag_value)
{
	//dbg(0, "road_flag = %x oneway_flag = %x\n", road_flag, oneway_flag_value);

	if (global_vehicle_profile == 2) // bicycle no one-ways
	{
		// dont care about one-ways
		//dbg(0, "ret00 = 0\n");
		return 0;
	}
	else if (global_vehicle_profile == 1) // bicycle
	{
		if (road_flag & NAVIT_AF_ONEWAY_BICYCLE_NO)
		{
			// one-ways does not apply to bicycles here
			//dbg(0, "ret01 = 0\n");
			return 0;
		}
		else
		{
			//dbg(0, "ret02 = %x\n", (road_flag & oneway_flag_value));
			return (road_flag & oneway_flag_value);
		}
	}
	else // normal one-way handling
	{
		//dbg(0, "ret03 = %x\n", (road_flag & oneway_flag_value));
		return (road_flag & oneway_flag_value);
	}
}


/**
 * @brief Checks if a segment is part of a roundabout
 *
 * This function checks if a segment is part of a roundabout.
 *
 * @param seg The segment to be checked
 * @param level How deep to scan the route graph
 * @param direction Set this to 1 if we're entering the segment through its end, to 0 otherwise
 * @param origin Used internally, set to NULL
 * @return 1 If a roundabout was detected, 0 otherwise
 */
static int route_check_roundabout(struct route_graph_segment *seg, int level, int direction, struct route_graph_segment *origin)
{
	//// dbg(0, "enter\n");

	struct route_graph_point_iterator it, it2;
	struct route_graph_segment *cur;
	int count = 0;

	if (!level)
	{
		return 0;
	}
	if (!direction && !(route_get_real_oneway_flag(seg->data.flags, NAVIT_AF_ONEWAY)))
	{
		return 0;
	}
	if (direction && !(route_get_real_oneway_flag(seg->data.flags, NAVIT_AF_ONEWAYREV)))
	{
		return 0;
	}
	if (seg->data.flags & NAVIT_AF_ROUNDABOUT_VALID)
	{
		return 0;
	}

	if (!origin)
	{
		origin = seg;
	}

	if (!direction)
	{
		it = rp_iterator_new(seg->end);
	}
	else
	{
		it = rp_iterator_new(seg->start);
	}
	it2 = it;

	while ((cur = rp_iterator_next(&it2)))
	{
		count++;
	}

	if (count > 3) // 3 z8z8
	{
		return 0;
	}

	cur = rp_iterator_next(&it);
	while (cur)
	{
		if (cur == seg)
		{
			cur = rp_iterator_next(&it);
			continue;
		}


		if (cur->data.item.type != origin->data.item.type)
		{
			// This street is of another type, can't be part of the roundabout
			cur = rp_iterator_next(&it);
			continue;
		}

		if (cur == origin)
		{
			seg->data.flags |= NAVIT_AF_ROUNDABOUT;
			return 1;
		}

		if (route_check_roundabout(cur, (level - 1), rp_iterator_end(&it), origin))
		{
			seg->data.flags |= NAVIT_AF_ROUNDABOUT;
			return 1;
		}

		cur = rp_iterator_next(&it);
	}

	return 0;
}

/**
 * @brief Sets the mapset of the route passed
 *
 * @param this The route to set the mapset for
 * @param ms The mapset to set for this route
 */
void route_set_mapset(struct route *this, struct mapset *ms)
{
__F_START__

	this->ms = ms;

__F_END__
}

/**
 * @brief Sets the vehicle profile of a route
 *
 * @param this The route to set the profile for
 * @param prof The vehicle profile
 */

void route_set_profile(struct route *this, struct vehicleprofile *prof)
{
__F_START__

	if (this->vehicleprofile != prof)
	{
		this->vehicleprofile = prof;
		route_path_update(this, 1, 1);
	}
__F_END__
}

/**
 * @brief Returns the mapset of the route passed
 *
 * @param this The route to get the mapset of
 * @return The mapset of the route passed
 */
struct mapset *
route_get_mapset(struct route *this)
{
__F_START__

	return2 this->ms;

__F_END__
}

/**
 * @brief Returns the current position within the route passed
 *
 * @param this The route to get the position for
 * @return The position within the route passed
 */
struct route_info *
route_get_pos(struct route *this)
{
__F_START__

	return2 this->pos;

__F_END__
}

/**
 * @brief Returns the destination of the route passed
 *
 * @param this The route to get the destination for
 * @return The destination of the route passed
 */
struct route_info *
route_get_dst(struct route *this)
{
	//// dbg(0, "enter\n");

	struct route_info *dst = NULL;

	if (this->destinations)
		dst = g_list_last(this->destinations)->data;
	return dst;
}

/**
 * @brief Checks if the path is calculated for the route passed
 *
 * @param this The route to check
 * @return True if the path is calculated, false if not
 */
int route_get_path_set(struct route *this)
{
__F_START__

	return2 this->path2 != NULL;
__F_END__
}

/**
 * @brief Checks if the route passed contains a certain item within the route path
 *
 * This function checks if a certain items exists in the path that navit will guide
 * the user to his destination. It does *not* check if this item exists in the route 
 * graph!
 *
 * @param this The route to check for this item
 * @param item The item to search for
 * @return True if the item was found, false if the item was not found or the route was not calculated
 */
int route_contains(struct route *this, struct item *item)
{
	if (!this->path2 || !this->path2->path_hash)
	{
		return 0;
	}

	if (item_hash_lookup(this->path2->path_hash, item))
	{
		return 1;
	}

	if (!this->pos || !this->pos->street)
	{
		return 0;
	}

	return item_is_equal(this->pos->street->item, *item);
}

static struct route_info *
route_next_destination(struct route *this)
{
__F_START__

	if (!this->destinations)
	{
		return2 NULL;
	}

	return2 this->destinations->data;
__F_END__
}

/**
 * @brief Checks if a route has reached its destination
 *
 * @param this The route to be checked
 * @return True if the destination is "reached", false otherwise.
 */
int route_destination_reached(struct route *this)
{
__F_START__

	struct street_data *sd = NULL;
	enum projection pro;
	struct route_info *dst = route_next_destination(this);

	if (!this->pos)
	{
		return2 0;
	}

	if (!dst)
	{
		return2 0;
	}

	sd = this->pos->street;

	if (!this->path2)
	{
		return2 0;
	}

	// this fixed a crash for large offroad start segments
	if (!this->pos->street)
	{
		return2 0;
	}

	if (!item_is_equal(this->pos->street->item, dst->street->item))
	{
		return2 0;
	}

	if ((route_get_real_oneway_flag(sd->flags, NAVIT_AF_ONEWAY)) && (this->pos->lenneg >= dst->lenneg))
	{ // We would have to drive against the one-way road
		return2 0;
	}

	if ((route_get_real_oneway_flag(sd->flags, NAVIT_AF_ONEWAYREV)) && (this->pos->lenpos >= dst->lenpos))
	{
		return2 0;
	}

	pro = route_projection(this);
	if (pro == projection_none)
	{
		return2 0;
	}

	if (transform_distance(pro, &this->pos->c, &dst->lp) > this->destination_distance)
	{
		return2 0;
	}

	if (g_list_next(this->destinations))
	{
		return2 1;
	}
	else
	{
		return2 2;
	}
__F_END__
}


static struct route_info *
route_previous_destination(struct route *this)
{
__F_START__

	GList *l = g_list_find(this->destinations, this->current_dst);
	if (!l)
	{
		return2 this->pos;
	}

	l = g_list_previous(l);
	if (!l)
	{
		return2 this->pos;
	}

	return2 l->data;

__F_END__

}

static void route_path_update_done(struct route *this, int new_graph)
{
__F_START__

	struct route_path *oldpath = this->path2;
	struct attr route_status;
	struct route_info *prev_dst;
	route_status.type = attr_route_status;

	if (this->path2 && (this->path2->in_use > 1))
	{
		this->path2->update_required = 1 + new_graph;
		return2;
	}

	route_status.u.num = route_status_building_path;
	// this call is needed to update navigation and speak directions!!
	route_set_attr(this, &route_status);

	prev_dst = route_previous_destination(this);

	dbg(0, "AAA:001 lp=%d\n", this->link_path);

	if (this->link_path)
	{
		dbg(0, "AAA:002 lp=%d\n", this->link_path);

		// generate route path from "prev_dst" to "this->current_dst"

		dbg(0, "RPNEW:CALL 001\n");

		this->path2 = route_path_new(this, this->graph, NULL, prev_dst, this->current_dst, this->vehicleprofile);
		if (this->path2)
		{
			this->path2->next = oldpath;
		}
	}
	else
	{
		dbg(0, "AAA:003 lp=%d\n", this->link_path);

		dbg(0, "RPNEW:CALL 002\n");

		// generate route path from "prev_dst" to "this->current_dst"
		this->path2 = route_path_new(this, this->graph, oldpath, prev_dst, this->current_dst, this->vehicleprofile);
		if (oldpath && this->path2)
		{
			this->path2->next = oldpath->next;
			route_path_destroy(oldpath, 0);
		}
	}

	dbg(0, "AAA:004 lp=%d\n", this->link_path);

	if (this->path2)
	{
		struct route_path_segment *seg = this->path2->path;
		int path_time = 0, path_len = 0;
		while (seg)
		{
			/* FIXME */
			int seg_time = route_time_seg(this->vehicleprofile, seg->data, NULL);

			if (seg_time == INT_MAX)
			{
				// dbg(1, "error\n");
			}
			else
			{
				path_time += seg_time;
			}
			path_len += seg->data->len;
			seg = seg->next;
		}

		this->path2->path_time = path_time;
		this->path2->path_len = path_len;

		dbg(0, "AAA:005 lp=%d\n", this->link_path);
		if (prev_dst != this->pos) // do we have more destinations? (waypoints)
		{
			dbg(0, "AAA:006 lp=%d\n", this->link_path);

			dbg(0, "RPNEW:SET PREV DEST -1\n");

			this->link_path = 1;
			this->current_dst = prev_dst;	// set current destination back 1 waypoint, to calculate the next part of the route
											// (actually previous part, since we walk backwards to position)
			route_graph_reset(this->graph);
			// generate next route graph (and later route path)
			route_graph_flood(this->graph, this->current_dst, this->pos, this->vehicleprofile, this->route_graph_flood_done_cb);

			return2;
		}

		if (!new_graph && this->path2->updated)
		{
			// this is called on every GPS update!!
			route_status.u.num = route_status_path_done_incremental;

//#ifdef NAVIT_ROUTING_DEBUG_PRINT
//			dbg(0,"== DRAW MAP 001 ==\n");
//#endif
//			// need to paint map 1 time here!!
//			if (global_navit)
//			{
//				navit_draw(global_navit);
//			}
		}
		else
		{
			// this is called only when the route has been calculated
			route_status.u.num = route_status_path_done_new;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0,"== DRAW MAP 002 ==\n");
#endif
			// need to paint map 1 time here!!
			if (global_navit)
			{
				navit_draw(global_navit);
			}
		}
	}
	else
	{
		dbg(0, "AAA:007 lp=%d\n", this->link_path);

		// dbg(0, "try harder\n");
		// Try to rebuild the graph with smaller roads
		if (this->try_harder == 0)
		{
			this->try_harder = 1;
			route_graph_destroy(this->graph);
			this->graph = NULL;
			route_path_update(this, 1, 1);
		}
		else
		{
			route_status.u.num = route_status_not_found;
		}
	}

	dbg(0, "AAA:099 lp=%d\n", this->link_path);


	this->link_path = 0;
	route_set_attr(this, &route_status);

__F_END__
}

/**
 * @brief Updates the route graph and the route path if something changed with the route
 *
 * This will update the route graph and the route path of the route if some of the
 * route's settings (destination, position) have changed. 
 * 
 * @attention For this to work the route graph has to be destroyed if the route's 
 * @attention destination is changed somewhere!
 *
 * @param this The route to update
 */
void route_path_update(struct route *this, int cancel, int async)
{
__F_START__

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_path_update:enter\n");
#endif

	//dbg(0, "enter\n");

	//dbg(1, "enter %d\n", cancel);
	if (!this->pos || !this->destinations)
	{
		//dbg(0, "destroy\n");
		route_path_destroy(this->path2, 1);
		this->path2 = NULL;

		return2;
	}

	if (cancel)
	{
		route_graph_destroy(this->graph);
		this->graph = NULL;
	}

	/* the graph is destroyed when setting the destination */
	if (this->graph)
	{
		if (this->graph->busy)
		{
			//dbg(0, "busy building graph\n");
			return2;
		}
		// we can try to update
		//dbg(0, "try update\n");
		route_path_update_done(this, 0); // calls route_set_attr -> navigation_update !! (first loop)
	}
	else
	{
		route_path_destroy(this->path2, 1);
		this->path2 = NULL;
	}

	if (!this->graph || !this->path2)
	{
		//dbg(0, "rebuild graph\n");
		if (!this->route_graph_flood_done_cb)
		{
			this->route_graph_flood_done_cb = callback_new_2(callback_cast(route_path_update_done), this, (long) 1);
			callback_add_names(this->route_graph_flood_done_cb, "route_path_update", "route_path_update_done");
		}
		//dbg(0, "route_graph_update\n");
		route_graph_update(this, this->route_graph_flood_done_cb, async); // calls route_set_attr -> navigation_update !! (2nd loop)
	}

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_path_update:leave\n");
#endif

__F_END__
}

/** 
 * @brief This will calculate all the distances stored in a route_info
 *
 * @param ri The route_info to calculate the distances for
 * @param pro The projection used for this route
 */
static void route_info_distances(struct route_info *ri, enum projection pro)
{
__F_START__

	int npos = ri->pos + 1;
	struct street_data *sd = ri->street;

	/* 0 1 2 X 3 4 5 6 pos=2 npos=3 count=7 0,1,2 3,4,5,6*/

	ri->lenextra = transform_distance(pro, &ri->lp, &ri->c);

	ri->lenneg = transform_polyline_length(pro, sd->c, npos) + transform_distance(pro, &sd->c[ri->pos], &ri->lp);

	ri->lenpos = transform_polyline_length(pro, sd->c + npos, sd->count - npos) + transform_distance(pro, &sd->c[npos], &ri->lp);

	if (ri->lenneg || ri->lenpos)
	{
		ri->percent = (ri->lenneg * 100) / (ri->lenneg + ri->lenpos);
	}
	else
	{
		ri->percent = 50;
	}

	dbg(0,"AAA:(1)len extra=%d\n", ri->lenextra);

	if (ri->lenextra < 1)
	{
		ri->lenextra = 1; // always set this, to avoid holes in route path display
	}

	dbg(0,"AAA:(2)len extra=%d\n", ri->lenextra);

__F_END__

}

/**
 * @brief This sets the current position of the route passed
 *
 * This will set the current position of the route passed to the street that is nearest to the
 * passed coordinates. It also automatically updates the route.
 *
 * @param this The route to set the position of
 * @param pos Coordinates to set as position
 */
void route_set_position(struct route *this, struct pcoord *pos)
{
__F_START__

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_set_position: enter\n");
#endif


#ifdef NAVIT_ROUTING_DEBUG_PRINT
	// pcoord to geo
	struct coord_geo g22;
	struct coord c22;
	c22.x = pos->x;
	c22.y = pos->y;
	transform_to_geo(projection_mg, &c22, &g22);

	dbg(0, "ROUTExxPOSxx:route_set_position: %d %d %f %f\n", pos->x, pos->y, g22.lat, g22.lng);
#endif

	if (this->pos)
	{
		route_info_free(this->pos);
	}

	this->pos = NULL;
	this->pos = route_find_nearest_street(this->vehicleprofile, this->ms, pos);

	//dbg(0,"this->pos=%p\n", this->pos);

	// try harder
	if (this->pos == NULL)
	{
		this->pos = route_find_nearest_street_harder(this->vehicleprofile, this->ms, pos, 5000);
		//dbg(0,"this->pos=%p\n", this->pos);
	}

	// If there is no nearest street, bail out.
	if (!this->pos)
	{
		//dbg(0,"this->pos=%p\n", this->pos);
		// this->pos=g_new0(struct route_info, 1); *******
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		//dbg(0,"ROUTExxPOSxx:route_set_position:return 001\n");
		dbg(0, "ROUTExxPOSxx:route_set_position:(there is no nearest street, bail out) return 001\n");
#endif
		return2;
	}

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_set_position:1: x y: %d %d\n", pos->x, pos->y);
	//dbg(0, "2: x y: %i %i\n", this->pos->c.x, this->pos->c.y);
	//dbg(0, "3: x y: %i %i\n", this->pos->lp.x, this->pos->lp.y);
#endif

	this->pos->street_direction = 0;
	//dbg(1, "this->pos=%p\n", this->pos);
	route_info_distances(this->pos, pos->pro);

	//dbg(0,"sp 002\n");

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_path_update: 004\n");
#endif
	route_path_update(this, 0, 1);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_path_update: 004-a\n");
#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_set_position: leave\n");
#endif

__F_END__
}

/**
 * @brief Sets a route's current position based on coordinates from tracking
 *
 * @param this The route to set the current position of
 * @param tracking The tracking to get the coordinates from
 */
void route_set_position_from_tracking(struct route *this, struct tracking *tracking, enum projection pro)
{
__F_START__

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_set_position_from_tracking: enter\n");
#endif

	struct coord *c;
	struct route_info *ret;
	struct street_data *sd;

	//dbg(2, "enter\n");
	c = tracking_get_pos(tracking);
	ret=g_new0(struct route_info, 1);

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_set_position_from_tracking: %d %d\n", c->x, c->y);
#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	// ------- DEBUG --------
	// ------- DEBUG --------
	// ------- DEBUG --------
	struct coord_geo gg4;
	transform_to_geo(pro, c, &gg4);
	dbg(0, "ROUTExxPOSxx:1:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);
	// ------- DEBUG --------
	// ------- DEBUG --------
	// ------- DEBUG --------
#endif

	if (!ret)
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:(Out of memory) return 001\n");
#endif
		return2;
	}

	if (this->pos)
	{
		route_info_free(this->pos);
	}

	this->pos = NULL;
	ret->c = *c;
	ret->lp = *c;
	ret->pos = tracking_get_segment_pos(tracking);
	ret->street_direction = tracking_get_street_direction(tracking);
	sd = tracking_get_street_data(tracking);

	// int road_angle = tracking_get_angle(tracking);
	//dbg(0, "ROAD angle=%d\n", road_angle);

	if (sd)
	{
		ret->street = street_data_dup(sd);
		route_info_distances(ret, pro);
	}
	//dbg(
	//		3,
	//		"position 0x%x,0x%x item 0x%x,0x%x direction %d pos %d lenpos %d lenneg %d\n",
	//		c->x, c->y, sd ? sd->item.id_hi : 0, sd ? sd->item.id_lo : 0,
	//		ret->street_direction, ret->pos, ret->lenpos, ret->lenneg);
	//dbg(3, "c->x=0x%x, c->y=0x%x pos=%d item=(0x%x,0x%x)\n", c->x, c->y,
	//		ret->pos, ret->street ? ret->street->item.id_hi : 0,
	//		ret->street ? ret->street->item.id_lo : 0);
	//dbg(3, "street 0=(0x%x,0x%x) %d=(0x%x,0x%x)\n",
	//		ret->street ? ret->street->c[0].x : 0,
	//		ret->street ? ret->street->c[0].y : 0,
	//		ret->street ? ret->street->count - 1 : 0,
	//		ret->street ? ret->street->c[ret->street->count - 1].x : 0,
	//		ret->street ? ret->street->c[ret->street->count - 1].y : 0);
	this->pos = ret;

	if (this->destinations)
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:route_path_update: 001\n");
#endif
		route_path_update(this, 0, 1);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:route_path_update: 001-a\n");
#endif
	}
	//dbg(2, "ret\n");

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:route_set_position_from_tracking: leave\n");
#endif

__F_END__

}

/* Used for debuging of route_rect, what routing sees */
struct map_selection *route_selection;

/**
 * @brief Returns a single map selection
 */
struct map_selection *
route_rect(int order, struct coord *c1, struct coord *c2, int rel, int abs)
{
	//// dbg(0, "enter\n");

	int dx, dy, sx = 1, sy = 1, d, m;
	struct map_selection *sel=g_new0(struct map_selection, 1);

	if (!sel)
	{
		dbg(0, "Out of memory\n");
		// printf("%s:Out of memory\n", __FUNCTION__);
		return sel;
	}

	sel->order = order;
	sel->range.min = route_item_first;
	sel->range.max = route_item_last;
	// dbg(1, "%p %p\n", c1, c2);
	dx = c1->x - c2->x;
	dy = c1->y - c2->y;

	if (dx < 0)
	{
		sx = -1;
		sel->u.c_rect.lu.x = c1->x;
		sel->u.c_rect.rl.x = c2->x;
	}
	else
	{
		sel->u.c_rect.lu.x = c2->x;
		sel->u.c_rect.rl.x = c1->x;
	}

	if (dy < 0)
	{
		sy = -1;
		sel->u.c_rect.lu.y = c2->y;
		sel->u.c_rect.rl.y = c1->y;
	}
	else
	{
		sel->u.c_rect.lu.y = c1->y;
		sel->u.c_rect.rl.y = c2->y;
	}

	if (dx * sx > dy * sy)
	{
		d = dx * sx;
	}
	else
	{
		d = dy * sy;
	}

	m = d * rel / 100 + abs;

	//dbg(0,"m=%d d=%d rel=%d abs=%d\n",m,d,rel,abs);

	sel->u.c_rect.lu.x -= m;
	sel->u.c_rect.rl.x += m;
	sel->u.c_rect.lu.y += m;
	sel->u.c_rect.rl.y -= m;
	sel->next = NULL;
	return sel;
}

/**
 * @brief Returns a list of map selections useable to create a route graph
 *
 * Returns a list of  map selections useable to get a  map rect from which items can be
 * retrieved to build a route graph. The selections are a rectangle with
 * c1 and c2 as two corners.
 *
 * @param c1 Corner 1 of the rectangle
 * @param c2 Corder 2 of the rectangle
 */
static struct map_selection *
route_calc_selection(struct coord *c, int count, int try_harder)
{
	// dbg(0, "enter\n");

	struct map_selection *ret, *sel;
	int i;
	struct coord_rect r;

	if (!count)
	{
		return NULL;
	}

	r.lu = c[0];
	r.rl = c[0];
	for (i = 1; i < count; i++)
	{
		// extend the rectangle to include all waypoints -> make 1 big rectangle
		coord_rect_extend(&r, &c[i]);
	}

	// the route selection rectangle will be extened further, to find all routes (by 25%)
#ifdef HAVE_API_ANDROID
	if (global_show_route_rectangles)
	{
		send_route_rect_to_java(r.lu.x, r.lu.y, r.rl.x, r.rl.y, -99);
	}
#endif


	double len_from_start_to_dest = transform_distance(projection_mg, &c[0], &c[count - 1]);
	dbg(0, "distdist=%f\n", len_from_start_to_dest);


	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))  // ------------ BICYCLE MODE -----------
	{
		if (len_from_start_to_dest > 100000)
		{
			sel = route_rect(try_harder ? 6 : 4, &r.lu, &r.rl, 25, 0);
		}
		else
		{
			sel = route_rect(try_harder ? 18 : 16, &r.lu, &r.rl, 25, 0);
		}

	}   // ------------ BICYCLE MODE -----------



	else // ------------ CAR MODE -----------
	{

		if (routing_mode == 0)
		{
			// normal highway routing
			// sel=route_rect(4, &r.lu, &r.rl, 25, 0);
			if (len_from_start_to_dest > 100000)
			{
				sel = route_rect(try_harder ? 6 : 4, &r.lu, &r.rl, 25, 0); // ORIG: try_harder ? 6 : 4, &r.lu, &r.rl, 25, 0
			}
			else
			{
				sel = route_rect(try_harder ? 8 : 7, &r.lu, &r.rl, 25, 0);
			}

			// the route selection rectangle will be extened further, to find all routes (by 25%)
	#ifdef HAVE_API_ANDROID
			if (global_show_route_rectangles)
			{
				send_route_rect_to_java(sel->u.c_rect.lu.x, sel->u.c_rect.lu.y, sel->u.c_rect.rl.x, sel->u.c_rect.rl.y, try_harder ? 6 : 4);
			}
	#endif
		}
		else if (routing_mode == 1)
		{
			// normal roads routing (should take longer and use more roads)
			// sel=route_rect(6, &r.lu, &r.rl, 25, 0);
			sel = route_rect(try_harder ? 7 : 6, &r.lu, &r.rl, 25, 0);

	#ifdef HAVE_API_ANDROID
			if (global_show_route_rectangles)
			{
				send_route_rect_to_java(sel->u.c_rect.lu.x, sel->u.c_rect.lu.y, sel->u.c_rect.rl.x, sel->u.c_rect.rl.y, try_harder ? 7 : 6);
			}
	#endif
		}
		else
		{
			// DEFAULT setting
			// normal highway routing
			// sel=route_rect(4, &r.lu, &r.rl, 25, 0);
			if (len_from_start_to_dest > 100000)
			{
				sel = route_rect(try_harder ? 6 : 4, &r.lu, &r.rl, 25, 0); // ORIG: try_harder ? 6 : 4, &r.lu, &r.rl, 25, 0
			}
			else
			{
				sel = route_rect(try_harder ? 8 : 7, &r.lu, &r.rl, 25, 0);
			}

			// the route selection rectangle will be extened further, to find all routes (by 25%)
	#ifdef HAVE_API_ANDROID
			if (global_show_route_rectangles)
			{
				send_route_rect_to_java(sel->u.c_rect.lu.x, sel->u.c_rect.lu.y, sel->u.c_rect.rl.x, sel->u.c_rect.rl.y, try_harder ? 6 : 4);
			}
	#endif

		}
	}  // ------------ CAR MODE -----------







	ret = sel;
	for (i = 0; i < count; i++)
	{
		// make 2 rectangles around every waypoint
		// 1 rect small but with high detail (every small street)
		// 1 rect a bit bigger but with lower detail

		if (global_routing_engine != 1) // not OSRM routing
		{
			sel->next = route_rect(8, &c[i], &c[i], 0, try_harder ? 40000 : 8500); // DEFAULT = 0, 40000
		}
		else
		{
			if (i < (count -1))
			{
				// sel->next = route_rect(8, &c[i], &c[i + 1], 0, 2);
				sel->next = route_rect(8, &c[i], &c[i], 0, try_harder ? 140 : 220);
			}
			else
			{
				sel->next = route_rect(8, &c[i], &c[i], 0, try_harder ? 140 : 220);
			}
		}
		sel = sel->next;
#ifdef HAVE_API_ANDROID
		if (global_show_route_rectangles)
		{
			send_route_rect_to_java(sel->u.c_rect.lu.x, sel->u.c_rect.lu.y, sel->u.c_rect.rl.x, sel->u.c_rect.rl.y, 8);
		}
#endif

		if (global_routing_engine != 1) // not OSRM routing
		{
			sel->next = route_rect(18, &c[i], &c[i], 0, try_harder ? 10000 : 2500); // DEFAULT = 0, 10000
		}
		else
		{
			if (i < (count -1))
			{
				// sel->next = route_rect(18, &c[i], &c[i + 1], 0, 2);
				sel->next = route_rect(18, &c[i], &c[i], 0, try_harder ? 100 : 120);
			}
			else
			{
				sel->next = route_rect(18, &c[i], &c[i], 0, try_harder ? 100 : 120);
			}
		}
		sel = sel->next;
#ifdef HAVE_API_ANDROID
		if (global_show_route_rectangles)
		{
			send_route_rect_to_java(sel->u.c_rect.lu.x, sel->u.c_rect.lu.y, sel->u.c_rect.rl.x, sel->u.c_rect.rl.y, 18);
		}
#endif
	}
	/* route_selection=ret; */
	return ret;
}

/**
 * @brief Destroys a list of map selections
 *
 * @param sel Start of the list to be destroyed
 */
static void route_free_selection(struct map_selection *sel)
{
__F_START__

	struct map_selection *next;
	while (sel)
	{
		next = sel->next;
		g_free(sel);
		sel = next;
	}
__F_END__
}

static void route_clear_destinations(struct route *this_)
{
__F_START__

	g_list_foreach(this_->destinations, (GFunc) route_info_free, NULL);
	g_list_free(this_->destinations);
	this_->destinations = NULL;
__F_END__
}

/**
 * @brief Sets the destination of a route
 *
 * This sets the destination of a route to the street nearest to the coordinates passed
 * and updates the route.
 *
 * @param this The route to set the destination for
 * @param dst Coordinates to set as destination
 * @param count: Number of destinations (last one is final)
 * @param async: If set, do routing asynchronously
 */

void route_set_destinations(struct route *this, struct pcoord *dst, int count, int async)
{
__F_START__

	struct attr route_status;
	struct route_info *dsti;
	int i;
	route_status.type = attr_route_status;

	//profile(0,NULL);
	route_clear_destinations(this);

	if (dst && count)
	{
		for (i = 0; i < count; i++)
		{
			dsti = route_find_nearest_street(this->vehicleprofile, this->ms, &dst[i]);

			// try harder
			if (dsti == NULL)
			{
				dsti = route_find_nearest_street_harder(this->vehicleprofile, this->ms, &dst[i], 16000);
			}

			if (dsti)
			{

				//dbg(0, "1: x y: %i %i\n", dst[i].x, dst[i].y);
				//dbg(0, "2: x y: %i %i\n", dsti->c.x, dsti->c.y);
				//dbg(0, "3: x y: %i %i\n", dsti->lp.x, dsti->lp.y);
				route_info_distances(dsti, dst->pro);
				this->destinations = g_list_append(this->destinations, dsti);
			}
		}
		route_status.u.num = route_status_destination_set;
	}
	else
	{
		route_status.u.num = route_status_no_destination;
	}


	dbg(0, "zzz3.1\n");
	//zzz3//
	// callback_list_call_attr_1(this->cbl2, attr_destination, this);
	route_set_attr(this, &route_status);
	//profile(1,"find_nearest_street");

	/* The graph has to be destroyed and set to NULL, otherwise route_path_update() doesn't work */
	route_graph_destroy(this->graph);
	this->graph = NULL;
	this->current_dst = route_get_dst(this);
	this->try_harder = 0;
	route_path_update(this, 1, async);
	//profile(0,"end");

__F_END__
}




void route_add_destination_no_calc(struct route *this, struct pcoord *dst, int async)
{
__F_START__

	// struct attr route_status;
	struct route_info *dsti;
	// route_status.type = attr_route_status;

	dsti = route_find_nearest_street(this->vehicleprofile, this->ms, &dst[0]);

	// try harder
	if (dsti == NULL)
	{
		dsti = route_find_nearest_street_harder(this->vehicleprofile, this->ms, &dst[0], 16000);
	}

	if (dsti)
	{
		//dbg(0, "1: x y: %i %i\n", dst[0].x, dst[0].y);
		//dbg(0, "2: x y: %i %i\n", dsti->c.x, dsti->c.y);
		//dbg(0, "3: x y: %i %i\n", dsti->lp.x, dsti->lp.y);

		route_info_distances(dsti, dst->pro);
		this->destinations = g_list_append(this->destinations, dsti);

	}

	this->try_harder = 0;

__F_END__
}



void route_set_destination_no_calc(struct route *this, struct pcoord *dst, int async)
{
__F_START__

	// route_set_destinations(this, dst, dst ? 1 : 0, async);

	route_clear_destinations(this);
	route_add_destination_no_calc(this, dst, async);

__F_END__
}



void route_after_destination_start_calc(struct route *this, int async)
{
__F_START__

	struct attr route_status;
	route_status.u.num = route_status_destination_set;

	dbg(0, "zzz3.1\n");
	//zzz3//
	// callback_list_call_attr_1(this->cbl2, attr_destination, this);
	route_set_attr(this, &route_status);
	//profile(1,"find_nearest_street");

	/* The graph has to be destroyed and set to NULL, otherwise route_path_update() doesn't work */
	route_graph_destroy(this->graph);
	this->graph = NULL;
	this->current_dst = route_get_dst(this);
	this->try_harder = 0;
	route_path_update(this, 1, async);

__F_END__
}







void route_add_destination(struct route *this, struct pcoord *dst, int async)
{
__F_START__

	struct attr route_status;
	struct route_info *dsti;
	route_status.type = attr_route_status;

	dsti = route_find_nearest_street(this->vehicleprofile, this->ms, &dst[0]);

	// try harder
	if (dsti == NULL)
	{
		dsti = route_find_nearest_street_harder(this->vehicleprofile, this->ms, &dst[0], 16000);
	}

	if (dsti)
	{
		//dbg(0, "1: x y: %i %i\n", dst[0].x, dst[0].y);
		//dbg(0, "2: x y: %i %i\n", dsti->c.x, dsti->c.y);
		//dbg(0, "3: x y: %i %i\n", dsti->lp.x, dsti->lp.y);

		route_info_distances(dsti, dst->pro);
		this->destinations = g_list_append(this->destinations, dsti);

		route_status.u.num = route_status_destination_set;
	}

	dbg(0, "zzz3.2\n");
	//zzz3//
	// callback_list_call_attr_1(this->cbl2, attr_destination, this);
	route_set_attr(this, &route_status);

	/* The graph has to be destroyed and set to NULL, otherwise route_path_update() doesn't work */
	route_graph_destroy(this->graph);
	this->graph = NULL;
	this->current_dst = route_get_dst(this);
	this->try_harder = 0;
	route_path_update(this, 1, async);
__F_END__
}

int route_get_destinations(struct route *this, struct pcoord *pc, int count)
{
__F_START__

	int ret = 0;
	GList *l = this->destinations;
	while (l && ret < count)
	{
		struct route_info *dst = l->data;
		pc->x = dst->c.x;
		pc->y = dst->c.y;
		pc->pro = projection_mg; /* FIXME */
		pc++;
		ret++;
		l = g_list_next(l);
	}

	return2 ret;

__F_END__

}

void route_set_destination(struct route *this, struct pcoord *dst, int async)
{
__F_START__

	route_set_destinations(this, dst, dst ? 1 : 0, async);

__F_END__
}

void route_remove_waypoint(struct route *this)
{
__F_START__

	struct route_path *path = this->path2;
	this->destinations = g_list_remove(this->destinations, this->destinations->data);
	this->path2 = this->path2->next;
	route_path_destroy(path, 0);

	if (!this->destinations)
	{
		return2;
	}

	route_graph_reset(this->graph);
	this->current_dst = this->destinations->data;

	route_graph_flood(this->graph, this->current_dst, this->pos, this->vehicleprofile, this->route_graph_flood_done_cb);

__F_END__

}

/**
 * @brief Gets the route_graph_point with the specified coordinates
 *
 * @param this The route in which to search
 * @param c Coordinates to search for
 * @param last The last route graph point returned to iterate over multiple points with the same coordinates
 * @return The point at the specified coordinates or NULL if not found
 */
static struct route_graph_point *
route_graph_get_point_next(struct route_graph *this, struct coord *c, struct route_graph_point *last)
{
	//// dbg(0, "enter\n");

	struct route_graph_point *p;
	int seen = 0, hashval = HASHCOORD(c);
	p = this->hash[hashval];
	while (p)
	{
		if (p->c.x == c->x && p->c.y == c->y)
		{
			if (!last || seen)
				return p;

			if (p == last)
				seen = 1;

		}
		p = p->hash_next;
	}
	return NULL;
}

static struct route_graph_point *
route_graph_get_point(struct route_graph *this, struct coord *c)
{
	//// dbg(0, "enter\n");

	return route_graph_get_point_next(this, c, NULL);
}

/**
 * @brief Gets the last route_graph_point with the specified coordinates 
 *
 * @param this The route in which to search
 * @param c Coordinates to search for
 * @return The point at the specified coordinates or NULL if not found
 */
static struct route_graph_point *
route_graph_get_point_last(struct route_graph *this, struct coord *c)
{
	//// dbg(0, "enter\n");

	struct route_graph_point *p, *ret = NULL;
	int hashval = HASHCOORD(c);
	p = this->hash[hashval];
	while (p)
	{
		if (p->c.x == c->x && p->c.y == c->y)
			ret = p;
		p = p->hash_next;
	}
	return ret;
}

/**
 * @brief Create a new point for the route graph with the specified coordinates
 *
 * @param this The route to insert the point into
 * @param f The coordinates at which the point should be created
 * @return The point created
 */
static struct route_graph_point *
route_graph_point_new(struct route_graph *this, struct coord *f)
{
	//// dbg(0, "enter\n");

	int hashval;
	struct route_graph_point *p;

	hashval = HASHCOORD(f);
	//if (debug_route)
	//	printf("p (0x%x,0x%x)\n", f->x, f->y);

	p=g_slice_new0(struct route_graph_point);
	//global_route_memory_size = global_route_memory_size + sizeof(struct route_graph_point);
	//dbg(0,"(A)route mem=%lu\n", global_route_memory_size);

	p->hash_next = this->hash[hashval];
	this->hash[hashval] = p;
	p->value = INT_MAX;
	p->c = *f;

	return p;
}

/**
 * @brief Inserts a point into the route graph at the specified coordinates
 *
 * This will insert a point into the route graph at the coordinates passed in f.
 * Note that the point is not yet linked to any segments.
 *
 * @param this The route to insert the point into
 * @param f The coordinates at which the point should be inserted
 * @return The point inserted or NULL on failure
 */
static struct route_graph_point *
route_graph_add_point(struct route_graph *this, struct coord *f)
{
	//// dbg(0, "enter\n");

	struct route_graph_point *p;

	p = route_graph_get_point(this, f);

	if (!p)
	{
		p = route_graph_point_new(this, f);
	}

	return p;
}

/**
 * @brief Frees all the memory used for points in the route graph passed
 *
 * @param this The route graph to delete all points from
 */
static void route_graph_free_points(struct route_graph *this)
{
	//// dbg(0, "enter\n");

	struct route_graph_point *curr, *next;
	int i;
	for (i = 0; i < HASH_SIZE; i++)
	{
		curr = this->hash[i];
		while (curr)
		{
			next = curr->hash_next;
			g_slice_free(struct route_graph_point, curr);
			//global_route_memory_size = global_route_memory_size - sizeof(struct route_graph_point);
			//dbg(0,"(F)route mem=%lu\n", global_route_memory_size);

			curr = next;
		}
		this->hash[i] = NULL;
	}
}

/**
 * @brief Resets all nodes
 *
 * @param this The route graph to reset
 */
static void route_graph_reset(struct route_graph *this)
{
__F_START__

	struct route_graph_point *curr;
	int i;
	for (i = 0; i < HASH_SIZE; i++)
	{
		curr = this->hash[i];
		while (curr)
		{
			curr->value = INT_MAX;
			curr->seg = NULL;
			curr->el = NULL;
			curr = curr->hash_next;
		}
	}

__F_END__
}

/**
 * @brief Returns the position of a certain field appended to a route graph segment
 *
 * This function returns a pointer to a field that is appended to a route graph
 * segment.
 *
 * @param seg The route graph segment the field is appended to
 * @param type Type of the field that should be returned
 * @return A pointer to a field of a certain type, or NULL if no such field is present
 */
static void *
route_segment_data_field_pos(struct route_segment_data *seg, enum attr_type type)
{
	// // dbg(0, "enter\n");

	unsigned char *ptr;

	ptr = ((unsigned char*) seg) + sizeof(struct route_segment_data);

	if (seg->flags & NAVIT_AF_SPEED_LIMIT)
	{
		if (type == attr_maxspeed)
			return (void*) ptr;

		ptr += sizeof(int);
	}
	if (seg->flags & NAVIT_AF_SEGMENTED)
	{
		if (type == attr_offset)
			return (void*) ptr;

		ptr += sizeof(int);
	}
	if (seg->flags & NAVIT_AF_SIZE_OR_WEIGHT_LIMIT)
	{
		if (type == attr_vehicle_width)
			return (void*) ptr;

		ptr += sizeof(struct size_weight_limit);
	}
	if (seg->flags & NAVIT_AF_DANGEROUS_GOODS)
	{
		if (type == attr_vehicle_dangerous_goods)
			return (void*) ptr;

		ptr += sizeof(int);
	}
	return NULL;
}

/**
 * @brief Calculates the size of a route_segment_data struct with given flags
 *
 * @param flags The flags of the route_segment_data
 */
static int route_segment_data_size(int flags)
{
	//// dbg(0, "enter\n");

	int ret = sizeof(struct route_segment_data);

	if (flags & NAVIT_AF_SPEED_LIMIT)
		ret += sizeof(int);

	if (flags & NAVIT_AF_SEGMENTED)
		ret += sizeof(int);

	if (flags & NAVIT_AF_SIZE_OR_WEIGHT_LIMIT)
		ret += sizeof(struct size_weight_limit);

	if (flags & NAVIT_AF_DANGEROUS_GOODS)
		ret += sizeof(int);

	return ret;
}

static int route_graph_segment_is_duplicate(struct route_graph_point *start, struct route_graph_segment_data *data)
{
	//// dbg(0, "enter\n");

	struct route_graph_segment *s;
	s = start->start;
	while (s)
	{
		if (item_is_equal(*data->item, s->data.item))
		{
			if (data->flags & NAVIT_AF_SEGMENTED)
			{
				if (RSD_OFFSET(&s->data) == data->offset)
				{
					return 1;
				}
			}
			else
				return 1;
		}
		s = s->start_next;
	}
	return 0;
}

/**
 * @brief Inserts a new segment into the route graph
 *
 * This function performs a check if a segment for the item specified already exists, and inserts
 * a new segment representing this item if it does not.
 *
 * @param this The route graph to insert the segment into
 * @param start The graph point which should be connected to the start of this segment
 * @param end The graph point which should be connected to the end of this segment
 * @param len The length of this segment
 * @param item The item that is represented by this segment
 * @param flags Flags for this segment
 * @param offset If the item passed in "item" is segmented (i.e. divided into several segments), this indicates the position of this segment within the item
 * @param maxspeed The maximum speed allowed on this segment in km/h. -1 if not known.
 */
static void route_graph_add_segment(struct route_graph *this, struct route_graph_point *start, struct route_graph_point *end, struct route_graph_segment_data *data)
{
	//// dbg(0, "enter\n");

	struct route_graph_segment *s;
	int size;

	size = sizeof(struct route_graph_segment) - sizeof(struct route_segment_data) + route_segment_data_size(data->flags);

	s = g_slice_alloc0(size);
	//global_route_memory_size = global_route_memory_size + size;
	//dbg(0,"(A)route mem=%lu\n", global_route_memory_size);

	if (!s)
	{
		printf("%s:Out of memory\n", __FUNCTION__);
		return;
	}

	s->start = start;
	s->start_next = start->start;
	start->start = s;

	s->end = end;
	s->end_next = end->end;
	end->end = s;

	dbg_assert(data->len >= 0);

	s->data.len = data->len;
	s->data.item = *data->item;
	s->data.flags = data->flags;

	// save coords to calculate road angles later -------------
	s->c_start_plus_1.x = 0;
	s->c_start_plus_1.y = 0;
	s->c_end_minus_1.x = 0;
	s->c_end_minus_1.y = 0;
	// save coords to calculate road angles later -------------

	if (data->flags & NAVIT_AF_SPEED_LIMIT)
		RSD_MAXSPEED(&s->data) = data->maxspeed;

	if (data->flags & NAVIT_AF_SEGMENTED)
		RSD_OFFSET(&s->data) = data->offset;

	if (data->flags & NAVIT_AF_SIZE_OR_WEIGHT_LIMIT)
		RSD_SIZE_WEIGHT(&s->data) = data->size_weight;

	if (data->flags & NAVIT_AF_DANGEROUS_GOODS)
		RSD_DANGEROUS_GOODS(&s->data) = data->dangerous_goods;

	s->next = this->route_segments;
	this->route_segments = s;

	//if (debug_route)
	//{
	//	printf("l (0x%x,0x%x)-(0x%x,0x%x)\n", start->c.x, start->c.y, end->c.x, end->c.y);
	//}
}

static void route_graph_add_segment_with_coords(struct route_graph *this, struct route_graph_point *start, struct route_graph_point *end, struct route_graph_segment_data *data, struct coord *c2, struct coord *c98)
{
	//// dbg(0, "enter\n");

	struct route_graph_segment *s;
	int size;

	size = sizeof(struct route_graph_segment) - sizeof(struct route_segment_data) + route_segment_data_size(data->flags);

	s = g_slice_alloc0(size);
	//global_route_memory_size = global_route_memory_size + size;
	//dbg(0,"(A)route mem=%lu\n", global_route_memory_size);

	if (!s)
	{
		printf("%s:Out of memory\n", __FUNCTION__);
		return;
	}

	s->start = start;
	s->start_next = start->start;
	start->start = s;

	s->end = end;
	s->end_next = end->end;
	end->end = s;

	dbg_assert(data->len >= 0);

	s->data.len = data->len;
	s->data.item = *data->item;
	s->data.flags = data->flags;

	// save coords to calculate road angles later -------------
	s->c_start_plus_1.x = c2->x;
	s->c_start_plus_1.y = c2->y;
	s->c_end_minus_1.x = c98->x;
	s->c_end_minus_1.y = c98->y;
	// save coords to calculate road angles later -------------

	if (data->flags & NAVIT_AF_SPEED_LIMIT)
		RSD_MAXSPEED(&s->data) = data->maxspeed;

	if (data->flags & NAVIT_AF_SEGMENTED)
		RSD_OFFSET(&s->data) = data->offset;

	if (data->flags & NAVIT_AF_SIZE_OR_WEIGHT_LIMIT)
		RSD_SIZE_WEIGHT(&s->data) = data->size_weight;

	if (data->flags & NAVIT_AF_DANGEROUS_GOODS)
		RSD_DANGEROUS_GOODS(&s->data) = data->dangerous_goods;

	s->next = this->route_segments;
	this->route_segments = s;
}


/**
 * @brief Gets all the coordinates of an item
 *
 * This will get all the coordinates of the item i and return them in c,
 * up to max coordinates. Additionally it is possible to limit the coordinates
 * returned to all the coordinates of the item between the two coordinates
 * "start" and "end".
 *
 * @important Make sure that whatever c points to has enough memory allocated
 * @important to hold max coordinates!
 *
 * @param i The item to get the coordinates of
 * @param c Pointer to memory allocated for holding the coordinates
 * @param max Maximum number of coordinates to return
 * @param start First coordinate to get
 * @param end Last coordinate to get
 * @return The number of coordinates returned
 */
static int get_item_seg_coords(struct item *i, struct coord *c, int max, struct coord *start, struct coord *end)
{
	//// dbg(0, "enter\n");

	struct map_rect *mr;
	struct item *item;
	int rc = 0, p = 0;
	struct coord c1;
	mr = map_rect_new(i->map, NULL);

	if (!mr)
	{
		return 0;
	}

	item = map_rect_get_item_byid(mr, i->id_hi, i->id_lo);

	if (item)
	{
		rc = item_coord_get(item, &c1, 1);

		while (rc && (c1.x != start->x || c1.y != start->y))
		{
			rc = item_coord_get(item, &c1, 1);
		}

		while (rc && p < max)
		{
			c[p++] = c1;
			if (c1.x == end->x && c1.y == end->y)
			{
				break;
			}
			rc = item_coord_get(item, &c1, 1);
		}
	}
	map_rect_destroy(mr);
	return p;
}

/**
 * @brief Returns and removes one segment from a path
 *
 * @param path The path to take the segment from
 * @param item The item whose segment to remove
 * @param offset Offset of the segment within the item to remove. If the item is not segmented this should be 1.
 * @return The segment removed
 */
static struct route_path_segment *
route_extract_segment_from_path(struct route_path *path, struct item *item, int offset)
{
	//// dbg(0, "enter\n");

	int soffset;
	struct route_path_segment *sp = NULL, *s;
	s = path->path;

	while (s)
	{
		if (item_is_equal(s->data->item, *item))
		{
			if (s->data->flags & NAVIT_AF_SEGMENTED)
				soffset = RSD_OFFSET(s->data);
			else
				soffset = 1;

			if (soffset == offset)
			{
				if (sp)
				{
					sp->next = s->next;
					break;
				}
				else
				{
					path->path = s->next;
					break;
				}
			}
		}
		sp = s;
		s = s->next;
	}

	if (s)
		item_hash_remove(path->path_hash, item);

	return s;
}

/**
 * @brief Adds a segment and the end of a path
 *
 * @param this The path to add the segment to
 * @param segment The segment to add
 */
static void route_path_add_segment(struct route_path *this, struct route_path_segment *segment)
{
	//// dbg(0, "enter\n");

	if (!this->path)
		this->path = segment;

	if (this->path_last)
		this->path_last->next = segment;

	this->path_last = segment;
}

/**
 * @brief Adds a two coordinate line to a path
 *
 * This adds a new line to a path, creating a new segment for it.
 *
 * @param this The path to add the item to
 * @param start coordinate to add to the start of the item. If none should be added, make this NULL.
 * @param end coordinate to add to the end of the item. If none should be added, make this NULL.
 * @param len The length of the item
 */
static void route_path_add_line(struct route_path *this, struct coord *start, struct coord *end, int len)
{
	//dbg(0, "enter\n");

	int ccnt = 2;
	struct route_path_segment *segment;
	int seg_size, seg_dat_size;

	//dbg(0, "line from 0x%x,0x%x-0x%x,0x%x\n", start->x, start->y, end->x, end->y);

	seg_size = sizeof(*segment) + sizeof(struct coord) * ccnt;
	seg_dat_size = sizeof(struct route_segment_data);
	segment = g_malloc0(seg_size + seg_dat_size);
	//global_route_memory_size = global_route_memory_size + seg_size + seg_dat_size;
	//dbg(0,"route mem=%lu\n", global_route_memory_size);

	segment->data = (struct route_segment_data *) ((char *) segment + seg_size);
	segment->ncoords = ccnt;
	segment->direction = 0;
	segment->c[0] = *start;
	segment->c[1] = *end;
	segment->data->len = len;
	route_path_add_segment(this, segment);
}


static void route_path_add_line_as_waypoint(struct route_path *this, struct coord *start, struct coord *end, int len)
{
	//dbg(0, "enter\n");

	int ccnt = 2;
	struct route_path_segment *segment;
	int seg_size, seg_dat_size;

	//dbg(0, "line from 0x%x,0x%x-0x%x,0x%x\n", start->x, start->y, end->x, end->y);

	seg_size = sizeof(*segment) + sizeof(struct coord) * ccnt;
	seg_dat_size = sizeof(struct route_segment_data);
	segment = g_malloc0(seg_size + seg_dat_size);
	//global_route_memory_size = global_route_memory_size + seg_size + seg_dat_size;
	//dbg(0,"route mem=%lu\n", global_route_memory_size);

	segment->data = (struct route_segment_data *) ((char *) segment + seg_size);
	segment->ncoords = ccnt;
	segment->direction = 0;
	segment->c[0] = *start;
	segment->c[1] = *end;
	segment->data->len = len;

	dbg(0, "RPNEW:WW:add type_street_route_waypoint prev type=%s\n", item_to_name(segment->data->item.type));
	segment->data->item.type = type_street_route_waypoint; // mark waypoint item

	route_path_add_segment(this, segment);
}

/**
 * @brief Inserts a new item into the path
 * 
 * This function does almost the same as "route_path_add_item()", but identifies
 * the item to add by a segment from the route graph. Another difference is that it "copies" the
 * segment from the route graph, i.e. if the item is segmented, only the segment passed in rgs will
 * be added to the route path, not all segments of the item. 
 *
 * The function can be sped up by passing an old path already containing this segment in oldpath - 
 * the segment will then be extracted from this old path. Please note that in this case the direction
 * parameter has no effect.
 *
 * @param this The path to add the item to
 * @param oldpath Old path containing the segment to be added. Speeds up the function, but can be NULL.
 * @param rgs Segment of the route graph that should be "copied" to the route path
 * @param dir Order in which to add the coordinates. See route_path_add_item()
 * @param pos  Information about start point if this is the first segment
 * @param dst  Information about end point if this is the last segment
 */
static int route_path_add_item_from_graph(struct route_path *this, struct route_path *oldpath, struct route_graph_segment *rgs, int dir, struct route_info *pos, struct route_info *dst)
{
	//// dbg(0, "enter\n");

	struct route_path_segment *segment;
	int i, ccnt, extra = 0, ret = 0;
	struct coord *c, *cd, ca[2048];
	int offset = 1;
	int seg_size, seg_dat_size;
	int len = rgs->data.len;
	if (rgs->data.flags & NAVIT_AF_SEGMENTED)
	{
		offset = RSD_OFFSET(&rgs->data);
	}

	//dbg(1, "enter (0x%x,0x%x) dir=%d pos=%p dst=%p\n", rgs->data.item.id_hi,
	//		rgs->data.item.id_lo, dir, pos, dst);
	if (oldpath)
	{
		segment = item_hash_lookup(oldpath->path_hash, &rgs->data.item);
		if (segment && segment->direction == dir)
		{
			segment = route_extract_segment_from_path(oldpath, &rgs->data.item, offset);
			if (segment)
			{
				ret = 1;

				if (!pos)
					goto linkold;

			}
		}
	}

	if (pos)
	{
		if (dst)
		{
			extra = 2;
			if (dst->lenneg >= pos->lenneg)
			{
				dir = 1;
				ccnt = dst->pos - pos->pos;
				c = pos->street->c + pos->pos + 1;
				len = dst->lenneg - pos->lenneg;
			}
			else
			{
				dir = -1;
				ccnt = pos->pos - dst->pos;
				c = pos->street->c + dst->pos + 1;
				len = pos->lenneg - dst->lenneg;
			}
		}
		else
		{
			extra = 1;
			//dbg(1, "pos dir=%d\n", dir);
			//dbg(1, "pos pos=%d\n", pos->pos);
			//dbg(1, "pos count=%d\n", pos->street->count);
			if (dir > 0)
			{
				c = pos->street->c + pos->pos + 1;
				ccnt = pos->street->count - pos->pos - 1;
				len = pos->lenpos;
			}
			else
			{
				c = pos->street->c;
				ccnt = pos->pos + 1;
				len = pos->lenneg;
			}
		}
		pos->dir = dir;
	}
	else if (dst)
	{
		extra = 1;
		//dbg(1, "dst dir=%d\n", dir);
		//dbg(1, "dst pos=%d\n", dst->pos);
		if (dir > 0)
		{
			c = dst->street->c;
			ccnt = dst->pos + 1;
			len = dst->lenpos;
		}
		else
		{
			c = dst->street->c + dst->pos + 1;
			ccnt = dst->street->count - dst->pos - 1;
			len = dst->lenneg;
		}
	}
	else
	{
		ccnt = get_item_seg_coords(&rgs->data.item, ca, 2047, &rgs->start->c, &rgs->end->c);
		c = ca;
	}
	seg_size = sizeof(*segment) + sizeof(struct coord) * (ccnt + extra);
	seg_dat_size = route_segment_data_size(rgs->data.flags);
	segment = g_malloc0(seg_size + seg_dat_size);
	//global_route_memory_size = global_route_memory_size + seg_size + seg_dat_size;
	//dbg(0,"route mem=%lu\n", global_route_memory_size);

	segment->data = (struct route_segment_data *) ((char *) segment + seg_size);
	segment->direction = dir;
	cd = segment->c;

	if (pos && (c[0].x != pos->lp.x || c[0].y != pos->lp.y))
		*cd++ = pos->lp;

	if (dir < 0)
		c += ccnt - 1;

	for (i = 0; i < ccnt; i++)
	{
		*cd++ = *c;
		c += dir;
	}

	segment->ncoords += ccnt;

	if (dst && (cd[-1].x != dst->lp.x || cd[-1].y != dst->lp.y))
		*cd++ = dst->lp;

	segment->ncoords = cd - segment->c;

	if (segment->ncoords <= 1)
	{
		g_free(segment);
		return 1;
	}

	/* We check if the route graph segment is part of a roundabout here, because this
	 * only matters for route graph segments which form parts of the route path */
	if (!(rgs->data.flags & NAVIT_AF_ROUNDABOUT))
	{
		// We identified this roundabout earlier
		route_check_roundabout(rgs, 13, (dir < 1), NULL); // 13 z8z8
	}

	memcpy(segment->data, &rgs->data, seg_dat_size);
	linkold: segment->data->len = len;
	segment->next = NULL;
	item_hash_insert(this->path_hash, &rgs->data.item, segment);

	route_path_add_segment(this, segment);

	return ret;
}

/**
 * @brief Destroys all segments of a route graph
 *
 * @param this The graph to destroy all segments from
 */
static void route_graph_free_segments(struct route_graph *this)
{
	//// dbg(0, "enter\n");

	struct route_graph_segment *curr, *next;
	int size;
	curr = this->route_segments;
	while (curr)
	{
		next = curr->next;
		size = sizeof(struct route_graph_segment) - sizeof(struct route_segment_data) + route_segment_data_size(curr->data.flags);
		g_slice_free1(size, curr);
		//global_route_memory_size = global_route_memory_size - size;
		//dbg(0,"(F)route mem=%lu\n", global_route_memory_size);
		curr = next;
	}
	this->route_segments = NULL;
}

/**
 * @brief Destroys a route graph
 * 
 * @param this The route graph to be destroyed
 */
void route_graph_destroy(struct route_graph *this)
{
__F_START__

	if (this)
	{
		route_graph_build_done(this, 1);
		route_graph_free_points(this);
		route_graph_free_segments(this);
		g_free(this);
	}

__F_END__
}

/**
 * @brief Returns the estimated speed on a segment
 *
 * This function returns the estimated speed to be driven on a segment, 0=not passable
 *
 * @param profile The routing preferences
 * @param over The segment which is passed
 * @param dist A traffic distortion if applicable
 * @return The estimated speed
 */
static int route_seg_speed(struct vehicleprofile *profile, struct route_segment_data *over, struct route_traffic_distortion *dist)
{
	// dbg(0, "enter\n");

	struct roadprofile *roadprofile = vehicleprofile_get_roadprofile(profile, over->item.type);
	int speed, maxspeed;

	if (!roadprofile || !roadprofile->route_weight)
	{
		return 0;
	}

	/* maxspeed_handling: 0=always, 1 only if maxspeed restricts the speed, 2 never */
	speed = roadprofile->route_weight;

	if (profile->maxspeed_handling != 2) // "0" or "1"
	{
		if (over->flags & NAVIT_AF_SPEED_LIMIT)
		{
			maxspeed = RSD_MAXSPEED(over);

			if (!profile->maxspeed_handling) // "0"
			{
				speed = maxspeed;
			}
		}
		else
		{
			maxspeed = INT_MAX;
		}

		if (dist && maxspeed > dist->maxspeed)
		{
			maxspeed = dist->maxspeed;
		}

		if (maxspeed != INT_MAX && (profile->maxspeed_handling != 1 || maxspeed < speed))
		{
			speed = maxspeed;
		}
	}

	// -- bicycle mode START --
	if ((global_vehicle_profile == 1) || (global_vehicle_profile == 2))
	{
	}
	else
	{
		if (speed < 150) // in km/h
		{
			// to calcuate the time to drive on street, speed should be reduced to <global_road_speed_factor> (~ 85%)
			speed = (int)((float)speed * global_road_speed_factor);
		}
	}


	if (over->flags & NAVIT_AF_DANGEROUS_GOODS)
	{
		if (profile->dangerous_goods & RSD_DANGEROUS_GOODS(over))
		{
			return 0;
		}
	}

	if (over->flags & NAVIT_AF_SIZE_OR_WEIGHT_LIMIT)
	{
		struct size_weight_limit *size_weight = &RSD_SIZE_WEIGHT(over);

		if (size_weight->width != -1 && profile->width != -1 && profile->width > size_weight->width)
			return 0;

		if (size_weight->height != -1 && profile->height != -1 && profile->height > size_weight->height)
			return 0;

		if (size_weight->length != -1 && profile->length != -1 && profile->length > size_weight->length)
			return 0;

		if (size_weight->weight != -1 && profile->weight != -1 && profile->weight > size_weight->weight)
			return 0;

		if (size_weight->axle_weight != -1 && profile->axle_weight != -1 && profile->axle_weight > size_weight->axle_weight)
			return 0;

	}

	return speed;
}


static int route_seg_speed_real(struct vehicleprofile *profile, struct route_segment_data *over, struct route_traffic_distortion *dist)
{
	// dbg(0, "enter\n");

	struct roadprofile *roadprofile = vehicleprofile_get_roadprofile(profile, over->item.type);
	int speed, maxspeed;

	if (!roadprofile || !roadprofile->route_weight)
	{
		return 0;
	}

	/* maxspeed_handling: 0=always, 1 only if maxspeed restricts the speed, 2 never */
	speed = roadprofile->route_weight;

	if (profile->maxspeed_handling != 2) // "0" or "1"
	{
		if (over->flags & NAVIT_AF_SPEED_LIMIT)
		{
			maxspeed = RSD_MAXSPEED(over);

			if (!profile->maxspeed_handling) // "0"
			{
				speed = maxspeed;
			}
		}
		else
		{
			maxspeed = INT_MAX;
		}

		if (dist && maxspeed > dist->maxspeed)
		{
			maxspeed = dist->maxspeed;
		}

		if (maxspeed != INT_MAX && (profile->maxspeed_handling != 1 || maxspeed < speed))
		{
			speed = maxspeed;
		}
	}

	if (over->flags & NAVIT_AF_DANGEROUS_GOODS)
	{
		if (profile->dangerous_goods & RSD_DANGEROUS_GOODS(over))
		{
			return 0;
		}
	}

	if (over->flags & NAVIT_AF_SIZE_OR_WEIGHT_LIMIT)
	{
		struct size_weight_limit *size_weight = &RSD_SIZE_WEIGHT(over);

		if (size_weight->width != -1 && profile->width != -1 && profile->width > size_weight->width)
			return 0;

		if (size_weight->height != -1 && profile->height != -1 && profile->height > size_weight->height)
			return 0;

		if (size_weight->length != -1 && profile->length != -1 && profile->length > size_weight->length)
			return 0;

		if (size_weight->weight != -1 && profile->weight != -1 && profile->weight > size_weight->weight)
			return 0;

		if (size_weight->axle_weight != -1 && profile->axle_weight != -1 && profile->axle_weight > size_weight->axle_weight)
			return 0;

	}

	return speed;
}


/**
 * @brief Returns the time needed to drive len on item
 *
 * This function returns the time needed to drive len meters on 
 * the item passed in item in tenth of seconds.
 *
 * @param profile The routing preferences
 * @param over The segment which is passed
 * @param dist A traffic distortion if applicable
 * @return The time needed to drive len on item in thenth of seconds
 */
static int route_time_seg(struct vehicleprofile *profile, struct route_segment_data *over, struct route_traffic_distortion *dist)
{
	// dbg(0, "enter\n");

	int speed = route_seg_speed(profile, over, dist);
	if (!speed)
	{
		return INT_MAX;
	}
	return over->len * 36 / speed + (dist ? dist->delay : 0);
}

static int route_get_traffic_distortion(struct route_graph_segment *seg, struct route_traffic_distortion *ret)
{
	// // dbg(0, "enter\n");

	struct route_graph_point *start = seg->start;
	struct route_graph_point *end = seg->end;
	struct route_graph_segment *tmp, *found = NULL;

	tmp = start->start;

	while (tmp && !found)
	{
		if (tmp->data.item.type == type_traffic_distortion && tmp->start == start && tmp->end == end)
			found = tmp;
		tmp = tmp->start_next;
	}

	tmp = start->end;
	while (tmp && !found)
	{
		if (tmp->data.item.type == type_traffic_distortion && tmp->end == start && tmp->start == end)
			found = tmp;
		tmp = tmp->end_next;
	}

	if (found)
	{
		ret->delay = found->data.len;

		if (found->data.flags & NAVIT_AF_SPEED_LIMIT)
			ret->maxspeed = RSD_MAXSPEED(&found->data);
		else
			ret->maxspeed = INT_MAX;

		return 1;
	}

	return 0;
}

static int route_through_traffic_allowed(struct vehicleprofile *profile, struct route_graph_segment *seg)
{
	return (seg->data.flags & NAVIT_AF_THROUGH_TRAFFIC_LIMIT) == 0;
}

/**
 * @brief Returns the "costs" of driving from point "from" over segment "over" in direction "dir"
 *
 * @param profile The routing preferences
 * @param from The point where we are starting (can be NULL)
 * @param over The segment we are using
 * @param dir The direction of segment which we are driving
 * @return The "costs" needed to drive len on item
 */
static int route_value_seg(struct vehicleprofile *profile, struct route_graph_point *from, struct route_graph_segment *over, int dir)
{
	//// dbg(0, "enter\n");

	int ret;
	struct route_traffic_distortion dist, *distp = NULL;

#if 0
	dbg(0,"flags 0x%x mask 0x%x flags 0x%x\n", over->flags, dir >= 0 ? profile->flags_forward_mask : profile->flags_reverse_mask, profile->flags);
#endif

	//dbg(0, "over data fl = %x\n", over->data.flags);
	if ((over->data.flags & (dir >= 0 ? route_get_real_oneway_mask(over->data.flags, profile->flags_forward_mask) : route_get_real_oneway_mask(over->data.flags, profile->flags_reverse_mask))) != profile->flags)
	{
		//dbg(0, "one way:001: INT_MAX\n");
		return INT_MAX;
	}

	if (dir > 0 && (over->start->flags & RP_TURN_RESTRICTION))
	{
		return INT_MAX;
	}

	if (dir < 0 && (over->end->flags & RP_TURN_RESTRICTION))
	{
		return INT_MAX;
	}

	if (from && from->seg == over)
	{
		return INT_MAX;
	}

	if ((over->start->flags & RP_TRAFFIC_DISTORTION) && (over->end->flags & RP_TRAFFIC_DISTORTION) && route_get_traffic_distortion(over, &dist))
	{
		distp = &dist;
	}

	ret = route_time_seg(profile, &over->data, distp);


	// add new "route_prio_weight" attr here ----------------------------------------------------------
	struct roadprofile *roadprofile = vehicleprofile_get_roadprofile(profile, (&over->data)->item.type);

	if (!roadprofile || !roadprofile->route_prio_weight)
	{
	}
	else
	{
		// cycle track is the same as real cycleway, its not on the road!
		if ((over->data.flags & NAVIT_AF_BICYCLE_TRACK) && ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)))
		{
			roadprofile = vehicleprofile_get_roadprofile(profile, type_cycleway);
			if ((roadprofile) && (roadprofile->route_prio_weight))
			{
				ret = (int)( (float)ret * ((float)roadprofile->route_prio_weight / 10.0f ));
			}
			else
			{
				ret = (int)( (float)ret * ((float)roadprofile->route_prio_weight / 10.0f ));
			}
		}
		else if ((over->data.flags & NAVIT_AF_BICYCLE_LANE) && ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)))
		{
			// road with cyclelane is a bit better than normal road!
			ret = (int)( (float)ret * ((float)(roadprofile->route_prio_weight - global_cycle_lanes_prio) / 10.0f ));
		}
		else
		{
			//dbg(0, "ret    =%d w=%d\n", ret, roadprofile->route_prio_weight);
			//int ret2 = ( ret * 100 * (roadprofile->route_prio_weight * 10.0 ) / 10000);
			//dbg(0, "ret2 new=%d\n", ret2);

			ret = (int)( (float)ret * ((float)roadprofile->route_prio_weight / 10.0f ));
			//dbg(0, "ret new=%d\n", ret);
		}
	}
	// add new "route_prio_weight" attr here ----------------------------------------------------------



	if (ret == INT_MAX)
	{
		return ret;
	}

	if (!route_through_traffic_allowed(profile, over) && from && route_through_traffic_allowed(profile, from->seg))
	{
		ret += profile->through_traffic_penalty;
	}

	// calc delay from traffic lights
	if ((from) && (global_traffic_light_delay > 0))
	{
		if (from->flags & RP_TRAFFIC_LIGHT)
		{
			// dbg(0, "traffic light delay:%d w/o:%d\n", (ret+global_traffic_light_delay), ret);
			// in 1/10 of a second !!
			ret += global_traffic_light_delay;
		}
	}

	return ret;
}



static int route_value_seg_r(struct vehicleprofile *profile, struct route_graph_point *from, struct route_graph_segment *over, int dir)
{
	//// dbg(0, "enter\n");

	int ret;
	struct route_traffic_distortion dist, *distp = NULL;

#if 0
	dbg(0,"flags 0x%x mask 0x%x flags 0x%x\n", over->flags, dir >= 0 ? profile->flags_forward_mask : profile->flags_reverse_mask, profile->flags);
#endif

	//dbg(0, "over data fl = %x\n", over->data.flags);
	if ((over->data.flags & (dir >= 0 ? route_get_real_oneway_mask(over->data.flags, profile->flags_forward_mask) : route_get_real_oneway_mask(over->data.flags, profile->flags_reverse_mask))) != profile->flags)
	{
		//dbg(0, "one way:001: INT_MAX\n");
		return INT_MAX;
	}

	if (dir > 0 && (over->start->flags & RP_TURN_RESTRICTION))
	{
		return INT_MAX;
	}

	if (dir < 0 && (over->end->flags & RP_TURN_RESTRICTION))
	{
		return INT_MAX;
	}

	if (from && from->seg_rev == over)
	{
		return INT_MAX;
	}

	if ((over->start->flags & RP_TRAFFIC_DISTORTION) && (over->end->flags & RP_TRAFFIC_DISTORTION) && route_get_traffic_distortion(over, &dist))
	{
		distp = &dist;
	}

	ret = route_time_seg(profile, &over->data, distp);


	// add new "route_prio_weight" attr here ----------------------------------------------------------
	struct roadprofile *roadprofile = vehicleprofile_get_roadprofile(profile, (&over->data)->item.type);

	if (!roadprofile || !roadprofile->route_prio_weight)
	{
	}
	else
	{
		// cycle track is the same as real cycleway, its not on the road!
		if ((over->data.flags & NAVIT_AF_BICYCLE_TRACK) && ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)))
		{
			roadprofile = vehicleprofile_get_roadprofile(profile, type_cycleway);
			if ((roadprofile) && (roadprofile->route_prio_weight))
			{
				ret = (int)( (float)ret * ((float)roadprofile->route_prio_weight / 10.0f ));
			}
			else
			{
				ret = (int)( (float)ret * ((float)roadprofile->route_prio_weight / 10.0f ));
			}
		}
		else if ((over->data.flags & NAVIT_AF_BICYCLE_LANE) && ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)))
		{
			// road with cyclelane is a bit better than normal road!
			ret = (int)( (float)ret * ((float)(roadprofile->route_prio_weight - global_cycle_lanes_prio) / 10.0f ));
		}
		else
		{
			//dbg(0, "ret    =%d w=%d\n", ret, roadprofile->route_prio_weight);
			//int ret2 = ( ret * 100 * (roadprofile->route_prio_weight * 10.0 ) / 10000);
			//dbg(0, "ret2 new=%d\n", ret2);

			ret = (int)( (float)ret * ((float)roadprofile->route_prio_weight / 10.0f ));
			//dbg(0, "ret new=%d\n", ret);
		}
	}
	// add new "route_prio_weight" attr here ----------------------------------------------------------



	if (ret == INT_MAX)
	{
		return ret;
	}

	if (!route_through_traffic_allowed(profile, over) && from && route_through_traffic_allowed(profile, from->seg_rev))
	{
		ret += profile->through_traffic_penalty;
	}

	// calc delay from traffic lights
	if ((from) && (global_traffic_light_delay > 0))
	{
		if (from->flags & RP_TRAFFIC_LIGHT)
		{
			// dbg(0, "traffic light delay:%d w/o:%d\n", (ret+global_traffic_light_delay), ret);
			// in 1/10 of a second !!
			ret += global_traffic_light_delay;
		}
	}

	return ret;
}




/**
 * @brief Adds a traffic light item to the route graph
 *
 * @param this The route graph to add to
 * @param item The item to add
 */
static void route_process_traffic_light(struct route_graph *this, struct item *item)
{

	struct route_graph_point *s_pnt, *e_pnt;
	struct coord l;
	struct route_graph_segment_data data;

	data.item = item;
	data.len = 0;
	data.flags = 0;
	data.offset = 1;
	data.maxspeed = INT_MAX;

	if (item_coord_get(item, &l, 1))
	{
		s_pnt = route_graph_add_point(this, &l);
		s_pnt->flags |= RP_TRAFFIC_LIGHT;
	}

}





/**
 * @brief Adds a route distortion item to the route graph
 *
 * @param this The route graph to add to
 * @param item The item to add
 */
static void route_process_traffic_distortion(struct route_graph *this, struct item *item)
{
__F_START__

	struct route_graph_point *s_pnt, *e_pnt;
	struct coord c, l;
	struct attr delay_attr, maxspeed_attr;
	struct route_graph_segment_data data;

	data.item = item;
	data.len = 0;
	data.flags = 0;
	data.offset = 1;
	data.maxspeed = INT_MAX;

	if (item_coord_get(item, &l, 1))
	{
		s_pnt = route_graph_add_point(this, &l);
		while (item_coord_get(item, &c, 1))
		{
			l = c;
		}

		e_pnt = route_graph_add_point(this, &l);
		s_pnt->flags |= RP_TRAFFIC_DISTORTION;
		e_pnt->flags |= RP_TRAFFIC_DISTORTION;

		if (item_attr_get(item, attr_maxspeed, &maxspeed_attr))
		{
			data.flags |= NAVIT_AF_SPEED_LIMIT;
			data.maxspeed = maxspeed_attr.u.num;
		}

		if (item_attr_get(item, attr_delay, &delay_attr))
		{
			data.len = delay_attr.u.num;
		}
		//dbg(0,"add traffic distortion segment, speed=%d\n", data.maxspeed);
		route_graph_add_segment(this, s_pnt, e_pnt, &data);
	}

__F_END__
}


/**
 * @brief Adds a route distortion item to the route graph
 *
 * @param this The route graph to add to
 * @param item The item to add
 */
static void route_process_turn_restriction(struct route_graph *this, struct item *item)
{

	struct route_graph_point *pnt[4];
	struct coord c[5];
	int i, count;
	struct route_graph_segment_data data;

	count = item_coord_get(item, c, 5);

	if (count != 3 && count != 4)
	{
		//dbg(0, "wrong count %d\n", count);
		return;
	}

	if (count == 4)
	{
		return;
	}

	for (i = 0; i < count; i++)
	{
		pnt[i] = route_graph_add_point(this, &c[i]);
	}

	//dbg(1, "%s: (0x%x,0x%x)-(0x%x,0x%x)-(0x%x,0x%x) %p-%p-%p\n",
	//		item_to_name(item->type), c[0].x, c[0].y, c[1].x, c[1].y, c[2].x,
	//		c[2].y, pnt[0], pnt[1], pnt[2]);
	data.item = item;
	data.flags = 0;
	data.len = 0;
	route_graph_add_segment(this, pnt[0], pnt[1], &data);
	route_graph_add_segment(this, pnt[1], pnt[2], &data);

#if 1
	if (count == 4)
	{
		pnt[1]->flags |= RP_TURN_RESTRICTION;
		pnt[2]->flags |= RP_TURN_RESTRICTION;
		route_graph_add_segment(this, pnt[2], pnt[3], &data);
	}
	else
	{
		pnt[1]->flags |= RP_TURN_RESTRICTION;
	}
#endif	

}

/**
 * @brief Adds an item to the route graph
 *
 * This adds an item (e.g. a street) to the route graph, creating as many segments as needed for a
 * segmented item.
 *
 * @param this The route graph to add to
 * @param item The item to add
 * @param profile		The vehicle profile currently in use
 */
static void route_process_street_graph(struct route_graph *this, struct item *item, struct vehicleprofile *profile)
{

#ifdef AVOID_FLOAT
	int len=0;
#else
	double len = 0;
#endif
	int segmented = 0;
	struct roadprofile *roadp;
	struct route_graph_point *s_pnt, *e_pnt;
	struct coord c2, c99, c98;
	struct coord c, l;
	struct attr attr;
	struct route_graph_segment_data data;
	data.flags = 0;
	data.offset = 1;
	data.maxspeed = -1;
	data.item = item;

	roadp = vehicleprofile_get_roadprofile(profile, item->type);

	if (!roadp)
	{
		// Don't include any roads that don't have a road profile in our vehicle profile
		return;
	}

	if (item_coord_get(item, &l, 1))
	{
		int *default_flags = item_get_default_flags(item->type);
		if (!default_flags)
		{
			return;
		}

		if (item_attr_get(item, attr_flags, &attr))
		{
			data.flags = attr.u.num;
			if (data.flags & NAVIT_AF_SEGMENTED)
			{
				segmented = 1;
			}
		}
		else
		{
			data.flags = *default_flags;
		}

		if (data.flags & NAVIT_AF_SPEED_LIMIT)
		{
			if (item_attr_get(item, attr_maxspeed, &attr))
			{
				data.maxspeed = attr.u.num;
			}
		}

		if (data.flags & NAVIT_AF_DANGEROUS_GOODS)
		{
			if (item_attr_get(item, attr_vehicle_dangerous_goods, &attr))
			{
				data.dangerous_goods = attr.u.num;
			}
			else
			{
				data.flags &= ~NAVIT_AF_DANGEROUS_GOODS;
			}
		}

		if (data.flags & NAVIT_AF_SIZE_OR_WEIGHT_LIMIT)
		{
			if (item_attr_get(item, attr_vehicle_width, &attr))
				data.size_weight.width = attr.u.num;
			else
				data.size_weight.width = -1;


			if (item_attr_get(item, attr_vehicle_height, &attr))
				data.size_weight.height = attr.u.num;
			else
				data.size_weight.height = -1;


			if (item_attr_get(item, attr_vehicle_length, &attr))
				data.size_weight.length = attr.u.num;
			else
				data.size_weight.length = -1;


			if (item_attr_get(item, attr_vehicle_weight, &attr))
				data.size_weight.weight = attr.u.num;
			else
				data.size_weight.weight = -1;


			if (item_attr_get(item, attr_vehicle_axle_weight, &attr))
				data.size_weight.axle_weight = attr.u.num;
			else
				data.size_weight.axle_weight = -1;

		}

		// add start point
		s_pnt = route_graph_add_point(this, &l);

		if (!segmented)
		{
			int count_coords = 0;

			c2.x = l.x;
			c2.y = l.y;
			c99.x = l.x;
			c99.y = l.y;
			c98.x = l.x;
			c98.y = l.y;

			while (item_coord_get(item, &c, 1))
			{
				len += transform_distance(map_projection(item->map), &l, &c);
				l = c;

				if (count_coords == 0)
				{
					// save second coord
					c2.x = c.x;
					c2.y = c.y;
					// fill in some values for the second to last coord (in case we only have 2 coords total!)
				}

				c98.x = c99.x;
				c98.y = c99.y;
				c99.x = c.x;
				c99.y = c.y;

				count_coords++;
			}
			// add end point
			e_pnt = route_graph_add_point(this, &l);
			dbg_assert(len >= 0);
			data.len = len;

			if (!route_graph_segment_is_duplicate(s_pnt, &data))
			{
				route_graph_add_segment_with_coords(this, s_pnt, e_pnt, &data, &c2, &c98);
			}
		}
		else
		{
			int isseg, rc;
			int sc = 0;
			do
			{
				isseg = item_coord_is_node(item);
				rc = item_coord_get(item, &c, 1);
				if (rc)
				{
					len += transform_distance(map_projection(item->map), &l, &c);
					l = c;
					if (isseg)
					{
						e_pnt = route_graph_add_point(this, &l);
						data.len = len;

						if (!route_graph_segment_is_duplicate(s_pnt, &data))
						{
							route_graph_add_segment(this, s_pnt, e_pnt, &data);
						}

						data.offset++;
						s_pnt = route_graph_add_point(this, &l);
						len = 0;
					}
				}
			}
			while (rc);



			e_pnt = route_graph_add_point(this, &l);
			dbg_assert(len >= 0);
			sc++;
			data.len = len;

			if (!route_graph_segment_is_duplicate(s_pnt, &data))
			{
				route_graph_add_segment(this, s_pnt, e_pnt, &data);
			}
		}
	}

}

struct route_graph_segment *route_graph_get_segment(struct route_graph *graph, struct street_data *sd, struct route_graph_segment *last)
{
	struct route_graph_point *start = NULL;
	struct route_graph_segment *s;
	int seen = 0;

	while ((start = route_graph_get_point_next(graph, &sd->c[0], start)))
	{
		s = start->start;
		while (s)
		{
			if (item_is_equal(sd->item, s->data.item))
			{
				if (!last || seen)
				{
					return s;
				}

				if (last == s)
				{
					seen = 1;
				}
			}
			s = s->start_next;
		}
	}

	return NULL;
}


static int route_angle_diff(int a1, int a2, int full)
{
	int ret = (a1 - a2) % full;

	if (ret > full / 2)
	{
		ret -= full;
	}

	if (ret < -full / 2)
	{
		ret += full;
	}

	return ret;
}

static int route_angle_abs_diff(int a1, int a2, int full)
{
	int ret = route_angle_diff(a1, a2, full);

	if (ret < 0)
	{
		ret = -ret;
	}

	return ret;

}

// unused, and maybe broken!
int route_road_to_road_angle____XXXXXXXX(struct route_graph_segment *s1, struct route_graph_segment *s2, int dir1)
{

	int dir2 = 1;

	if (dir1 == -1)
	{
		if ((s1->start->c.x == s2->start->c.x) && (s1->start->c.y == s2->start->c.y))
		{
			dir2 = 1;
		}
		else
		{
			dir2 = -1;
		}
	}
	else
	{
		if ((s1->end->c.x == s2->start->c.x) && (s1->end->c.y == s2->start->c.y))
		{
			dir2 = 1;
		}
		else
		{
			dir2 = -1;
		}
	}

	// COST: 204
	dbg(0, "COST:204\n");

	struct coord *c1 = &(s1->start->c);
	struct coord *c2 = &(s1->end->c);
	int angle1 = transform_get_angle_delta(c1, c2, -dir1);

	c1 = &(s2->start->c);
	c2 = &(s2->end->c);
	int angle2 = transform_get_angle_delta(c1, c2, dir2);

	int ret = route_angle_abs_diff(angle1, angle2, 360);

	return ret;

}

static int route_real_item_2nd_node(struct route_graph_segment *s, struct coord *c)
{
	struct item *i;
	struct item *i2;

	if ((s->c_start_plus_1.x != 0) && (s->c_start_plus_1.y != 0))
	{
		// COST: 201a
		// dbg(0, "COST:201a\n");

		c->x = s->c_start_plus_1.x;
		c->y = s->c_start_plus_1.y;
		return 1;
	}

	// COST: 201
	// dbg(0, "COST:201\n");

	i = &(s->data.item);

	if (i)
	{
		struct map_rect *mr33 = NULL;
		mr33 = map_rect_new(i->map, NULL);

		if (mr33)
		{
			i2 = map_rect_get_item_byid(mr33, i->id_hi, i->id_lo);
			if (i2)
			{
				if (item_coord_get(i2, c, 1))
				{
					if (item_coord_get(i2, c, 1))
					{
						map_rect_destroy(mr33);

						// save into struct -------------
						s->c_start_plus_1.x = c->x;
						s->c_start_plus_1.y = c->y;
						// save into struct -------------

						return 1;
					}
				}
			}
			map_rect_destroy(mr33);
		}
	}

	c->x = 0;
	c->y = 0;

	return 0;
}

static int route_real_item_2nd_last_node(struct route_graph_segment *s, struct coord *c)
{
	struct item *i;
	struct item *i2;
	struct coord c_temp1;
	struct coord c_temp2;
	int count = 0;

	if ((s->c_end_minus_1.x != 0) && (s->c_end_minus_1.y != 0))
	{
		// COST: 202a
		// dbg(0, "COST:202a\n");

		c->x = s->c_end_minus_1.x;
		c->y = s->c_end_minus_1.y;
		return 1;
	}

	// COST: 202
	// dbg(0, "COST:202\n");

	i = &(s->data.item);

	if (i)
	{
		struct map_rect *mr33 = NULL;
		mr33 = map_rect_new(i->map, NULL);

		if (mr33)
		{
			i2 = map_rect_get_item_byid(mr33, i->id_hi, i->id_lo);
			if (i2)
			{
				// get all coords, and remeber the 2nd last
				while (item_coord_get(i2, c, 1))
				{
					count++;

					c_temp2.x = c_temp1.x;
					c_temp2.y = c_temp1.y;
					c_temp1.x = c->x;
					c_temp1.y = c->y;
				}

				if (count > 1)
				{
					c->x = c_temp2.x;
					c->y = c_temp2.y;

					map_rect_destroy(mr33);

					// save into struct -------------
					s->c_end_minus_1.x = c->x;
					s->c_end_minus_1.y = c->y;
					// save into struct -------------

					return 1;
				}
			}
			map_rect_destroy(mr33);
		}
	}

	c->x = 0;
	c->y = 0;

	return 0;
}

//
// return: 1 == seg start is at point, 0 == seg end is at point
//
int route_find_seg_dir_at_point(struct route_graph_point *p, struct route_graph_segment *s1)
{
	if ((p->c.x == s1->start->c.x) && (p->c.y == s1->start->c.y))
	{
		return 1;
	}
	else
	{
		return -1;
	}
}

int route_road_to_road_angle_get_segs(struct route_graph_segment *s1, struct route_graph_segment *s2, int dir1, int *dir2, struct coord *cc, struct coord *cs, struct coord *ce, int abs)
{

	struct coord ci;
	struct coord z1;
	struct coord z2;
	struct coord z1e;
	struct coord z2e;

//      dir1 ==  1	(center point is s1->end)
//		dir1 == -1	(center point is s1->start)


//      dir ==  1 --> goes forward on item
//		dir == -1 --> does backwards on item



	*dir2 = 1;

	if (dir1 == -1)
	{
		if ((s1->start->c.x == s2->start->c.x) && (s1->start->c.y == s2->start->c.y))
		{
			*dir2 = 1;
			cc->x = s1->start->c.x; // s2->start
			cc->y = s1->start->c.y;

			//cs->x = s1->end->c.x;
			//cs->y = s1->end->c.y;
			if (route_real_item_2nd_node(s1, &ci) == 0)
			{
				return 180;
			}
			else
			{
				cs->x = ci.x;
				cs->y = ci.y;
			}

			// ce->x = s2->end->c.x;
			// ce->y = s2->end->c.y;
			if (route_real_item_2nd_node(s2, &ci) == 0)
			{
				return 180;
			}
			else
			{
				ce->x = ci.x;
				ce->y = ci.y;
			}


			z1.x = s1->start->c.x;
			z1.y = s1->start->c.y;
			z2.x = cs->x;
			z2.y = cs->y;

			z1e.x = s2->start->c.x;
			z1e.y = s2->start->c.y;
			z2e.x = ce->x;
			z2e.y = ce->y;

		}
		else
		{
			*dir2 = -1;
			cc->x = s1->start->c.x; // s2->end
			cc->y = s1->start->c.y;

			// cs->x = s1->end->c.x;
			// cs->y = s1->end->c.y;
			if (route_real_item_2nd_node(s1, &ci) == 0)
			{
				return 180;
			}
			else
			{
				cs->x = ci.x;
				cs->y = ci.y;
			}

			// ce->x = s2->start->c.x;
			// ce->y = s2->start->c.y;
			if (route_real_item_2nd_last_node(s2, &ci) == 0)
			{
				return 180;
			}
			else
			{
				ce->x = ci.x;
				ce->y = ci.y;
			}

			z1.x = s1->start->c.x;
			z1.y = s1->start->c.y;
			z2.x = cs->x;
			z2.y = cs->y;

			z1e.x = ce->x;
			z1e.y = ce->y;
			z2e.x = s2->end->c.x;
			z2e.y = s2->end->c.y;

		}
	}
	else
	{
		if ((s1->end->c.x == s2->start->c.x) && (s1->end->c.y == s2->start->c.y))
		{
			*dir2 = 1;
			cc->x = s1->end->c.x; // s2->start
			cc->y = s1->end->c.y;

			// cs->x = s1->start->c.x;
			// cs->y = s1->start->c.y;
			if (route_real_item_2nd_last_node(s1, &ci) == 0)
			{
				return 180;
			}
			else
			{
				cs->x = ci.x;
				cs->y = ci.y;
			}


			// ce->x = s2->end->c.x;
			// ce->y = s2->end->c.y;
			if (route_real_item_2nd_node(s2, &ci) == 0)
			{
				return 180;
			}
			else
			{
				ce->x = ci.x;
				ce->y = ci.y;
			}

			z1.x = cs->x;
			z1.y = cs->y;
			z2.x = s1->end->c.x;
			z2.y = s1->end->c.y;

			z1e.x = s2->start->c.x;
			z1e.y = s2->start->c.y;
			z2e.x = ce->x;
			z2e.y = ce->y;

		}
		else
		{
			*dir2 = -1;
			cc->x = s1->end->c.x; // s2->end
			cc->y = s1->end->c.y;

			// cs->x = s1->start->c.x;
			// cs->y = s1->start->c.y;
			if (route_real_item_2nd_last_node(s1, &ci) == 0)
			{
				return 180;
			}
			else
			{
				cs->x = ci.x;
				cs->y = ci.y;
			}


			// ce->x = s2->start->c.x;
			// ce->y = s2->start->c.y;
			if (route_real_item_2nd_last_node(s2, &ci) == 0)
			{
				return 180;
			}
			else
			{
				ce->x = ci.x;
				ce->y = ci.y;
			}

			z1.x = cs->x;
			z1.y = cs->y;
			z2.x = s1->end->c.x;
			z2.y = s1->end->c.y;

			z1e.x = ce->x;
			z1e.y = ce->y;
			z2e.x = s2->end->c.x;
			z2e.y = s2->end->c.y;

		}
	}

/*
	z1.x = s1->start->c.x;
	z1.y = s1->start->c.y;
	z2.x = s1->end->c.x;
	z2.y = s1->end->c.y;
*/
	// COST: 203
	// dbg(0, "COST:203\n");

	int angle1 = transform_get_angle_delta(&z1, &z2, -dir1);

	// dbg(0, "RR:55:a1=%d\n", angle1);

/*
	z1e.x = s2->start->c.x;
	z1e.y = s2->start->c.y;
	z2e.x = s2->end->c.x;
	z2e.y = s2->end->c.y;
*/
	int angle2 = transform_get_angle_delta(&z1e, &z2e, *dir2);

	// dbg(0, "RR:55:a2=%d\n", angle2);

	int ret;

	if (abs == 1)
	{
		ret = route_angle_abs_diff(angle1, angle2, 360);
	}
	else
	{
		ret = route_angle_diff(angle1, angle2, 360);
		if (ret > 0)
		{
			ret = ret - 180;
		}
		else // ret <= 0
		{
			ret = ret + 180;
		}
	}

	// dbg(0, "RR:55:res=%d\n", ret);
	return ret;
}


void route_clear_sharp_turn_list()
{
	if (global_sharp_turn_list)
	{
		g_free(global_sharp_turn_list);
	}

	global_sharp_turn_list_count = 0;
	global_sharp_turn_list = g_new0(struct global_sharp_turn, MAX_SHARP_TURN_LIST_ENTRIES + 2 );

}

void route_add_to_sharp_turn_list(struct coord *c, struct coord *cs, struct coord *ce, int turn_angle, int dir)
{
	global_sharp_turn_list[global_sharp_turn_list_count].dir = dir;
	global_sharp_turn_list[global_sharp_turn_list_count].angle = turn_angle;

	// dbg(0, "st.angle=%d\n", turn_angle);

	global_sharp_turn_list[global_sharp_turn_list_count].c1.x = c->x;
	global_sharp_turn_list[global_sharp_turn_list_count].c1.y = c->y;

	global_sharp_turn_list[global_sharp_turn_list_count].cs.x = cs->x;
	global_sharp_turn_list[global_sharp_turn_list_count].cs.y = cs->y;

	global_sharp_turn_list[global_sharp_turn_list_count].ce.x = ce->x;
	global_sharp_turn_list[global_sharp_turn_list_count].ce.y = ce->y;

	global_sharp_turn_list_count++;
	if (global_sharp_turn_list_count > MAX_SHARP_TURN_LIST_ENTRIES)
	{
		global_sharp_turn_list_count--;
	}
}



void route_clear_freetext_list()
{
	if (global_freetext_list)
	{
		g_free(global_freetext_list);
	}

	global_freetext_list_count = 0;
	global_freetext_list = g_new0(struct global_freetext, MAX_FREETEXT_LIST_ENTRIES + 2 );

}

void route_add_to_freetext_list(struct coord *c, const char* txt)
{
	global_freetext_list[global_freetext_list_count].c1.x = c->x;
	global_freetext_list[global_freetext_list_count].c1.y = c->y;

	snprintf(global_freetext_list[global_freetext_list_count].text, (MAX_FREETEXT_ENTRY_LEN - 2), "%s", txt);
	global_freetext_list[global_freetext_list_count].text[(MAX_FREETEXT_ENTRY_LEN - 1)] = '\0';

	global_freetext_list_count++;
	if (global_freetext_list_count > MAX_FREETEXT_LIST_ENTRIES)
	{
		global_freetext_list_count--;
	}
}




/**
 * @brief Calculates the routing costs for each point
 *
 * This function is the heart of routing. It assigns each point in the route graph a
 * cost at which one can reach the destination from this point on. Additionally it assigns
 * each point a segment one should follow from this point on to reach the destination at the
 * stated costs.
 *
 *  elements are: seg, value
 * 
 * This function uses Dijkstra's algorithm to do the routing. To understand it you should have a look
 * at this algorithm.
 *
 * call "cb" at the end
 *
 */
static void route_graph_flood(struct route_graph *this, struct route_info *dst, struct route_info *pos, struct vehicleprofile *profile, struct callback *cb)
{
__F_START__

	dbg(0, "RR_TIM:g:001\n");

	struct route_graph_point *p_min = NULL;
	struct route_graph_segment *s = NULL;
	int min, new, old, val;
	int turn_angle;
	struct fibheap *heap; /* This heap will hold all points with "temporarily" calculated costs */

	heap = fh_makekeyheap();

	// start from "wanted destination" and loop until "start position" is reached

	clock_t s_ = debug_measure_start();


#ifndef NAVIT_ROUTE_DIJKSTRA_REVERSE // NAVIT_ROUTE_DIJKSTRA_REVERSE == 0

	// calc segments lengths and values ------------
	// loop over all segments that connect to the destination street ONLY !!
	// calc segments lengths and values ------------
	// -------
	dbg(0, "RR_SEG_DEBUG:001\n");
	while ((s = route_graph_get_segment(this, dst->street, s)))
	{
		dbg(0, "RR_SEG_DEBUG:002:1:s=%p\n", s);
		val = route_value_seg(profile, NULL, s, -1);
		dbg(0, "RR_SEG_DEBUG:002:2:val=%d\n", val);
		if (val != INT_MAX)
		{
			val = val * (100 - dst->percent) / 100;
			dbg(0, "RR_SEG_DEBUG:002:P=%p dst->percent=%d val=%d end value=%d\n", s->end, dst->percent, val, s->end->value);
			s->end->seg = s;		// set segment "s" to be used to drive to dest. at point "s->end"
			s->end->value = val;	// set value to point "s->end"
			s->end->el = fh_insertkey(heap, s->end->value, s->end);
		}

		val = route_value_seg(profile, NULL, s, 1);
		dbg(0, "RR_SEG_DEBUG:003:3:val=%d\n", val);
		if (val != INT_MAX)
		{
			val = val * dst->percent / 100;
			dbg(0, "RR_SEG_DEBUG:003:P=%p dst->percent=%d val=%d start value=%d\n", s->start, dst->percent, val, s->start->value);
			s->start->seg = s;		// set segment "s" to be used to drive to dest. at point "s->start"
			s->start->value = val;	// set value to point "s->start"
			s->start->el = fh_insertkey(heap, s->start->value, s->start);
		}
		dbg(0, "RR_SEG_DEBUG:009:9:val=%d\n", val);
	}
	dbg(0, "RR_SEG_DEBUG:090\n");
	// -------
	// calc segments lengths and values ------------
	// calc segments lengths and values ------------


	debug_mrp("RR_TIM:g:002", debug_measure_end(s_));
	// dbg(0, "RR_TIM:g:002 %s\n");



#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_1
	route_clear_sharp_turn_list();
#endif
	struct coord ccc_cc;
	struct coord ccc_ss;
	struct coord ccc_ee;

	int val_sharp_turn = INT_MAX;
	int dir2 = 1;

	s_ = debug_measure_start();

	// start Dijkstra here ------------
	// start Dijkstra here ------------
	for (;;)
	{
		p_min = fh_extractmin(heap); /* Starting Dijkstra by selecting the point with the minimum costs on the heap */
									 // starts with the point that has the least cost to reach the destination!

		if (!p_min) /* There are no more points with temporarily calculated costs, Dijkstra has finished */
		{
			break;
		}

		min = p_min->value;
		//if (debug_route)
		//{
		//	printf("extract p=%p free el=%p min=%d, 0x%x, 0x%x\n", p_min, p_min->el, min, p_min->c.x, p_min->c.y);
		//}

		p_min->el = NULL; /* This point is permanently calculated now, we've taken it out of the heap */

		s = p_min->start; // route_graph_point "p_min": "s" = first route_graph_segment in the list of segments that start at point "p_min"

		while (s)
		{ /* Iterating all the segments leading away from our point to update the points at their ends */
			val = route_value_seg(profile, p_min, s, -1);
			if (val != INT_MAX && !item_is_equal(s->data.item, p_min->seg->data.item))
			{

				if (global_avoid_sharp_turns_flag == 91)
				{
					// calc "sharp-turn / turnaround" value
					turn_angle = route_road_to_road_angle_get_segs(s, p_min->seg, -1, &dir2, &ccc_cc, &ccc_ss, &ccc_ee, 1);
					if (turn_angle < global_avoid_sharp_turns_min_angle)
					{
						// dbg(0, "set turn angle penalty(1)\n");
						val_sharp_turn = val + (global_avoid_sharp_turns_min_penalty * turn_angle);
#if 1
						val = val_sharp_turn;
#endif

						//if ((s->end->value + (global_avoid_sharp_turns_min_penalty * turn_angle)) < INT_MAX)
						//{
						//	s->end->value = s->end->value + (global_avoid_sharp_turns_min_penalty * turn_angle);
						//}


#if 0
						// kill the prev point
						if (dir2 == 1)
						{
							p_min->seg->start->value = p_min->seg->start->value + (global_avoid_sharp_turns_min_penalty * turn_angle);
						}
						else // -1
						{
							p_min->seg->end->value = p_min->seg->end->value + (global_avoid_sharp_turns_min_penalty * turn_angle);
						}

						// now get the seg and value of the next lowest one
						// this sets p_min->value und p_min->seg new!!
						route_find_next_lowest_segment_and_pin_it(p_min, s, (val_sharp_turn - 1), (global_avoid_sharp_turns_min_penalty * turn_angle));
#endif

#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_1
						// route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, turn_angle, -1);
						route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, val, -1);
#endif
					}
				}

				new = min + val;
				//if (debug_route)
				//{
				//	printf("begin %d len %d vs %d (0x%x,0x%x)\n", new, val, s->end->value, s->end->c.x, s->end->c.y);
				//}

				if (new < s->end->value)
				{ /* We've found a less costly way to reach the end of s, update it */
					s->end->value = new;
					s->end->seg = s;

					if (!s->end->el)
					{
						//if (debug_route)
						//{
						//	printf("insert_end p=%p el=%p val=%d ", s->end, s->end->el, s->end->value);
						//}

						s->end->el = fh_insertkey(heap, new, s->end);

						//if (debug_route)
						//{
						//	printf("el new=%p\n", s->end->el);
						//}
					}
					else
					{
						//if (debug_route)
						//{
						//	printf("replace_end p=%p el=%p val=%d\n", s->end, s->end->el, s->end->value);
						//}
						fh_replacekey(heap, s->end->el, new);
					}
				}

				//if (debug_route)
				//{
				//	printf("\n");
				//}
			}
			s = s->start_next;
		}

		s = p_min->end;

		while (s)
		{ /* Doing the same as above with the segments leading towards our point */
			val = route_value_seg(profile, p_min, s, 1);
			if (val != INT_MAX && !item_is_equal(s->data.item, p_min->seg->data.item))
			{

				if (global_avoid_sharp_turns_flag == 1) // Pr.Strasse.CASE !!
				{
					// calc "sharp-turn / turnaround" value
					turn_angle = route_road_to_road_angle_get_segs(s, p_min->seg, 1, &dir2, &ccc_cc, &ccc_ss, &ccc_ee, 1);
					if (turn_angle < global_avoid_sharp_turns_min_angle)
					{
						// dbg(0, "set turn angle penalty(2)\n");
						// val_sharp_turn = val + (global_avoid_sharp_turns_min_penalty * turn_angle * 10);
						val_sharp_turn = val + 4000;
#if 1
						val = val_sharp_turn;
#endif
						//if ((s->start->value + (global_avoid_sharp_turns_min_penalty * turn_angle)) < INT_MAX)
						//{
						//	s->start->value = s->start->value + (global_avoid_sharp_turns_min_penalty * turn_angle * 10);
						//}


#if 0
						// kill the prev point
						if (dir2 == 1)
						{
							p_min->seg->end->value = p_min->seg->end->value + (global_avoid_sharp_turns_min_penalty * turn_angle);
						}
						else // -1
						{
							p_min->seg->start->value = p_min->seg->start->value + (global_avoid_sharp_turns_min_penalty * turn_angle);
						}

						// now get the seg and value of the next lowest one
						// this sets p_min->value und p_min->seg new!!
						route_find_next_lowest_segment_and_pin_it(p_min, s, (val_sharp_turn - 1), (global_avoid_sharp_turns_min_penalty * turn_angle));
#endif

						struct coord_geo gg4;
						transform_to_geo(projection_mg, &(s->start->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N1:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);
						transform_to_geo(projection_mg, &(s->end->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N2:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);

						transform_to_geo(projection_mg, &(p_min->seg->start->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N3:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);
						transform_to_geo(projection_mg, &(p_min->seg->end->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N4:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);

						transform_to_geo(projection_mg, &ccc_cc, &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:C9:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);

						dbg(0, "ROUTExxPOSxx:sharp_turn:S-start:v=%d:%d %d\n", (min + val), s->start->c.x, s->start->c.y);
						dbg(0, "ROUTExxPOSxx:sharp_turn:S-end:v=%d:%d %d\n", (min + val), s->end->c.x, s->end->c.y);
						dbg(0, "ROUTExxPOSxx:sharp_turn:A-center:v=%d:%d %d\n", (min + val), ccc_cc.x, ccc_cc.y);


						dbg(0, "ROUTExxPOSxx:sharp_turn:xxxxxxxxxxxxxxxxxxxxx\n");

#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_1
						route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, turn_angle, 1);
						// route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, val, 1);
#endif
					}
				}

				new = min + val;
				//if (debug_route)
				//{
				//	printf("end %d len %d vs %d (0x%x,0x%x)\n", new, val, s->start->value, s->start->c.x, s->start->c.y);
				//}

				if (new < s->start->value)
				{
					old = s->start->value;
					s->start->value = new;
					s->start->seg = s;

					if (!s->start->el)
					{
						//if (debug_route)
						//{
						//	printf("insert_start p=%p el=%p val=%d ", s->start, s->start->el, s->start->value);
						//}
						s->start->el = fh_insertkey(heap, new, s->start);
						//if (debug_route)
						//{
						//	printf("el new=%p\n", s->start->el);
						//}
					}
					else
					{
						//if (debug_route)
						//{
						//	printf("replace_start p=%p el=%p val=%d\n", s->start, s->start->el, s->start->value);
						//}
						fh_replacekey(heap, s->start->el, new);
					}
				}
				//if (debug_route)
				//{
				//	printf("\n");
				//}
			}
			s = s->end_next;
		}
	}




#else // NAVIT_ROUTE_DIJKSTRA_REVERSE == 1

	p_min = NULL;
	s = NULL;

	struct route_graph_segment *s_pos = NULL;

	dbg(0, "RR_SEG_DEBUG:001\n");
	while ((s = route_graph_get_segment(this, pos->street, s)))
	{
		dbg(0, "RR_SEG_DEBUG:002:1:s=%p\n", s);

		s_pos = s;

		val = route_value_seg_r(profile, NULL, s, 1);
		dbg(0, "RR_SEG_DEBUG:002:2:val=%d\n", val);
		if (val != INT_MAX)
		{
			val = val * (100 - pos->percent) / 100;
			if (val < 1)
			{
				val = 1;
			}
			dbg(0, "RR_SEG_DEBUG:002:P=%p pos->percent=%d val=%d end value=%d\n", s->end, pos->percent, val, s->end->value);
			s->end->seg = s;		// set segment "s" to be used to drive to dest. at point "s->end"
			s->end->seg_rev = s;
			s->end->value = val;	// set value to point "s->end"
			s->end->el = fh_insertkey(heap, s->end->value, s->end);
			dbg(0, "RR_SEG_DEBUG:002:EL:el=%p\n", s->end->el);
		}

		val = route_value_seg_r(profile, NULL, s, -1);
		dbg(0, "RR_SEG_DEBUG:003:3:val=%d\n", val);
		if (val != INT_MAX)
		{
			val = val * pos->percent / 100;
			if (val < 1)
			{
				val = 1;
			}
			dbg(0, "RR_SEG_DEBUG:003:P=%p pos->percent=%d val=%d start value=%d\n", s->start, pos->percent, val, s->start->value);
			s->start->seg = s;		// set segment "s" to be used to drive to dest. at point "s->start"
			s->start->seg_rev = s;
			s->start->value = val;	// set value to point "s->start"
			s->start->el = fh_insertkey(heap, s->start->value, s->start);
			dbg(0, "RR_SEG_DEBUG:003:EL:el=%p\n", s->start->el);
		}
		dbg(0, "RR_SEG_DEBUG:009:9:val=%d\n", val);
	}
	dbg(0, "RR_SEG_DEBUG:090\n");


#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_1
	route_clear_sharp_turn_list();
#endif
	struct coord ccc_cc;
	struct coord ccc_ss;
	struct coord ccc_ee;

	int val_sharp_turn = INT_MAX;
	int dir2 = 1;

	s_ = debug_measure_start();

	int start_dijkstra_loop = 1;

	// start (REVERSED-)Dijkstra here ------------
	// start (REVERSED-)Dijkstra here ------------
	dbg(0, "RR_SEG_DEBUG:R-DIJK-START\n");
	for (;;)
	{

		dbg(0, "RR_SEG_DEBUG:LOOP:start\n");

		p_min = fh_extractmin(heap); /* Starting Dijkstra by selecting the point with the minimum costs on the heap */
									 // starts with the point that has the least cost to reach the destination!


		if (!p_min) /* There are no more points with temporarily calculated costs, Dijkstra has finished */
		{
			dbg(0, "RR_SEG_DEBUG:BREAK:p_min=%p\n", p_min);
			break;
		}
		else
		{
			dbg(0, "RR_SEG_DEBUG:LOOP:p_min=%p v=%d el=%p\n", p_min, p_min->value, p_min->el);
		}

		// min = p_min->value;
		if (p_min->seg_rev)
		{
			int dir_from_seg = route_find_seg_dir_at_point(p_min, p_min->seg_rev);

			if (dir_from_seg == 1)
			{
				min = p_min->seg_rev->end->value;
			}
			else
			{
				min = p_min->seg_rev->start->value;
			}
		}
		else
		{
			min = INT_MAX;
			dbg(0, "RR_SEG_DEBUG:p_min->seg_rev == NULL !!!\n");
		}

		if (start_dijkstra_loop == 1)
		{
			start_dijkstra_loop = 0;
			min = 2;
		}

		dbg(0, "RR_SEG_DEBUG:new min=%d\n", min);

		p_min->el = NULL; /* This point is permanently calculated now, we've taken it out of the heap */

		s = p_min->start; // route_graph_point "p_min": "s" = first route_graph_segment in the list of segments that start at point "p_min"

		while (s)
		{
			/* Iterating all the segments leading away from our point to update the points at their ends */
			val = route_value_seg_r(profile, p_min, s, 1);

			dbg(0, "RR_SEG_DEBUG:LOOP-1:s=%p p_min=%p p_min->seg_rev=%p v=%d\n", s, p_min, p_min->seg_rev, val);

			if (val != INT_MAX && !item_is_equal(s->data.item, p_min->seg_rev->data.item))
			{

				if (global_avoid_sharp_turns_flag == 1)
				{
					// calc "sharp-turn / turnaround" value
					turn_angle = route_road_to_road_angle_get_segs(s, p_min->seg_rev, -1, &dir2, &ccc_cc, &ccc_ss, &ccc_ee, 1);
					if (turn_angle < global_avoid_sharp_turns_min_angle)
					{
						// dbg(0, "set turn angle penalty(1)\n");
						dbg(0, "RR_SEG_DEBUG:LOOP-1:turn angle=%d\n", turn_angle);

						val_sharp_turn = val + (global_avoid_sharp_turns_min_penalty * ((global_avoid_sharp_turns_min_angle + 10) - turn_angle));
#if 1
						val = val_sharp_turn;
#endif


#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_1
						route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, turn_angle, -1);
						// route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, val, -1);
#endif
					}
				}

				new = min + val;

				int val_cmp = s->start->value;
				if (s_pos->start == s->start)
				{
					val_cmp = INT_MAX;
				}

				dbg(0, "RR_SEG_DEBUG:LOOP-1:new=%d min=%d sv=%d val_cmp=%d p_min->el=%d p_min=%p\n", new, min, s->start->value, val_cmp, p_min->el, p_min);


				if (new < val_cmp)
				{

					dbg(0, "RR_SEG_DEBUG:LOOP-1:x001\n");
					/* We've found a less costly way to reach the end of s, update it */
					s->start->value = new;
					dbg(0, "RR_SEG_DEBUG:LOOP-1:x002\n");
					s->start->seg = s;
					dbg(0, "RR_SEG_DEBUG:LOOP-1:x003\n");
					s->end->seg_rev = s;
					dbg(0, "RR_SEG_DEBUG:LOOP-1:x004 heap=%p\n", heap);

					if (!s->end->el)
					{
						dbg(0, "RR_SEG_DEBUG:LOOP-1:x005\n");

						s->end->el = fh_insertkey(heap, new, s->end);
						dbg(0, "RR_SEG_DEBUG:LOOP-1:insert:s->end->el=%p s->end=%p p_min=%p\n", s->end->el, s->end, p_min);
					}
					else
					{
						dbg(0, "RR_SEG_DEBUG:LOOP-1:x006:s->end=%p s->end->el=%p heap=%p p_min=%p new=%d\n", s->end, s->end->el, heap, p_min, new);

						fh_replacekey(heap, s->end->el, new);
						dbg(0, "RR_SEG_DEBUG:LOOP-1:repl:x007\n");

					}

					dbg(0, "RR_SEG_DEBUG:LOOP-1:x008\n");
	
				}

				dbg(0, "RR_SEG_DEBUG:LOOP-1:x009\n");

			}

			dbg(0, "RR_SEG_DEBUG:LOOP-1:x010\n");

			s = s->start_next;

			dbg(0, "RR_SEG_DEBUG:LOOP-1:x011\n");

		}

		dbg(0, "RR_SEG_DEBUG:LOOP-1:x012\n");

		s = p_min->end;

		dbg(0, "RR_SEG_DEBUG:LOOP-1:x013\n");

		while (s)
		{
			/* Doing the same as above with the segments leading towards our point */
			val = route_value_seg_r(profile, p_min, s, -1);

			dbg(0, "RR_SEG_DEBUG:LOOP--2:s=%p p_min=%p p_min->seg_rev=%p v=%d\n", s, p_min, p_min->seg_rev, val);


			if (val != INT_MAX && !item_is_equal(s->data.item, p_min->seg_rev->data.item))
			{

				if (global_avoid_sharp_turns_flag == 1) // Pr.Strasse.CASE !!
				{
					// calc "sharp-turn / turnaround" value
					turn_angle = route_road_to_road_angle_get_segs(s, p_min->seg_rev, 1, &dir2, &ccc_cc, &ccc_ss, &ccc_ee, 1);
					if (turn_angle < global_avoid_sharp_turns_min_angle)
					{

						dbg(0, "RR_SEG_DEBUG:LOOP--2:turn angle=%d\n", turn_angle);
						val_sharp_turn = val + (global_avoid_sharp_turns_min_penalty * ((global_avoid_sharp_turns_min_angle + 10) - turn_angle));

						// val_sharp_turn = val + 4000;
#if 1
						val = val_sharp_turn;
#endif

#if 0
						struct coord_geo gg4;
						transform_to_geo(projection_mg, &(s->start->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N1:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);
						transform_to_geo(projection_mg, &(s->end->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N2:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);

						transform_to_geo(projection_mg, &(p_min->seg_rev->start->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N3:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);
						transform_to_geo(projection_mg, &(p_min->seg_rev->end->c), &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:N4:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);

						transform_to_geo(projection_mg, &ccc_cc, &gg4);
						dbg(0, "ROUTExxPOSxx:sharp_turn:C9:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:CC|%4.6f,%4.6f\n", gg4.lat, gg4.lng);

						dbg(0, "ROUTExxPOSxx:sharp_turn:S-start:v=%d:%d %d\n", (min + val), s->start->c.x, s->start->c.y);
						dbg(0, "ROUTExxPOSxx:sharp_turn:S-end:v=%d:%d %d\n", (min + val), s->end->c.x, s->end->c.y);
						dbg(0, "ROUTExxPOSxx:sharp_turn:A-center:v=%d:%d %d\n", (min + val), ccc_cc.x, ccc_cc.y);


						dbg(0, "ROUTExxPOSxx:sharp_turn:xxxxxxxxxxxxxxxxxxxxx\n");
#endif

#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_1
						route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, turn_angle, 1);
						// route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, val, 1);
#endif
					}
				}

				new = min + val;


				int val_cmp = s->end->value;
				if (s_pos->end == s->end)
				{
					val_cmp = INT_MAX;
				}

				dbg(0, "RR_SEG_DEBUG:LOOP--2:new=%d min=%d ev=%d val_cmp=%d p_min->el=%d p_min=%p\n", new, min, s->end->value, val_cmp, p_min->el, p_min);

				if (new < val_cmp)
				{
					old = s->end->value;
					s->end->value = new;
					s->end->seg = s;
					s->start->seg_rev = s;

					if (!s->start->el)
					{
						s->start->el = fh_insertkey(heap, new, s->start);
						dbg(0, "RR_SEG_DEBUG:LOOP--2:insert:s->start->el=%p s->start=%p p_min=%p\n", s->start->el, s->start, p_min);
					}
					else
					{
						dbg(0, "RR_SEG_DEBUG:LOOP--2:x006:s->start->el=%p heap=%p p_min=%p\n", s->start->el, heap, p_min);

						fh_replacekey(heap, s->start->el, new);
						dbg(0, "RR_SEG_DEBUG:LOOP--2:repl:x007\n");
					}


				}

			}

			s = s->end_next;

		}


	}



#endif







	debug_mrp("RR_TIM:g:003", debug_measure_end(s_));

	fh_deleteheap(heap);

	// CB: calls -> "route_path_update_done" !!
	callback_call_0(cb);

	dbg(0, "RR_TIM:g:099\n");


__F_END__
}




void route_find_next_lowest_segment_and_pin_it(struct route_graph_point *p, struct route_graph_segment *s, int min_value, int penalty)
{
	struct route_graph_segment *tmp = NULL;
	int cur_min = min_value;

	// -------------------------------------------------------------
	// p->start ==> a list of all streets that start from this point
	// -------------------------------------------------------------
	tmp = p->start;
	while (tmp)
	{
		if (tmp != s)
		{
			if (tmp->data.item.type != type_street_turn_restriction_no && tmp->data.item.type != type_street_turn_restriction_only)
			{
				if (tmp->end->value <= min_value)
				{
					min_value = tmp->end->value;
					p->value = min_value;
					p->seg = tmp;
				}
			}
		}
		tmp = tmp->start_next;
	}

	// -------------------------------------------------------------
	// p->end ==> a list of all streets that end at this point
	// -------------------------------------------------------------
	tmp = p->end;
	while (tmp)
	{
		if (tmp != s)
		{
			if (tmp->data.item.type != type_street_turn_restriction_no && tmp->data.item.type != type_street_turn_restriction_only)
			{
				if (tmp->start->value <= min_value)
				{
					min_value = tmp->start->value;
					p->value = min_value;
					p->seg = tmp;
				}
			}
		}
		tmp = tmp->end_next;
	}
}



/**
 * @brief Starts an "offroad" path
 *
 * This starts a path that is not located on a street. It creates a new route path
 * adding only one segment, that leads from pos to dest, and which is not associated with an item.
 *
 * @param this Not used
 * @param pos The starting position for the new path
 * @param dst The destination of the new path
 * @param dir Not used
 * @return The new path
 */
static struct route_path *
route_path_new_offroad(struct route_graph *this, struct route_info *pos, struct route_info *dst)
{
__F_START__

	struct route_path *ret;

	ret=g_new0(struct route_path, 1);
	ret->in_use = 1;
	ret->path_hash = item_hash_new();
	route_path_add_line(ret, &pos->c, &dst->c, pos->lenextra + dst->lenextra);
	ret->updated = 1;

	return2 ret;

__F_END__
}

/**
 * @brief Returns a coordinate at a given distance
 *
 * This function returns the coordinate, where the user will be if he
 * follows the current route for a certain distance.
 *
 * @param this_ The route we're driving upon
 * @param dist The distance in meters
 * @return The coordinate where the user will be in that distance
 */
struct coord route_get_coord_dist(struct route *this_, int dist)
{
__F_START__

	int d, l, i, len;
	int dx, dy;
	double frac;
	struct route_path_segment *cur;
	struct coord ret;
	enum projection pro = route_projection(this_);
	struct route_info *dst = route_get_dst(this_);

	d = dist;

	if (!this_->path2 || pro == projection_none)
	{
		return2 this_->pos->c;
	}

	ret = this_->pos->c;
	cur = this_->path2->path;
	while (cur)
	{
		if (cur->data->len < d)
		{
			d -= cur->data->len;
		}
		else
		{
			for (i = 0; i < (cur->ncoords - 1); i++)
			{
				l = d;
				len = (int) transform_polyline_length(pro, (cur->c + i), 2);
				d -= len;
				if (d <= 0)
				{
					// We interpolate a bit here...
					frac = (double) l / len;

					dx = (cur->c + i + 1)->x - (cur->c + i)->x;
					dy = (cur->c + i + 1)->y - (cur->c + i)->y;

					ret.x = (cur->c + i)->x + (frac * dx);
					ret.y = (cur->c + i)->y + (frac * dy);
					return2 ret;
				}
			}
			return2 cur->c[(cur->ncoords - 1)];
		}
		cur = cur->next;
	}

	return2 dst->c;

__F_END__
}

/**
 * @brief Creates a new route path
 * 
 * This creates a new non-trivial route. It therefore needs the routing information created by route_graph_flood, so
 * make sure to run route_graph_flood() after changing the destination before using this function.
 *
 * @param this The route graph to create the route from
 * @param oldpath (Optional) old path which may contain parts of the new part - this speeds things up a bit. May be NULL.
 * @param pos The starting position of the route
 * @param dst The destination of the route
 * @param preferences The routing preferences
 * @return The new route path
 */
static struct route_path *
route_path_new(struct route *rr, struct route_graph *this, struct route_path *oldpath, struct route_info *pos, struct route_info *dst, struct vehicleprofile *profile)
{
__F_START__

	struct route_graph_segment *first, *s = NULL, *s1 = NULL, *s2 = NULL;
	struct route_graph_point *start;
	struct route_info *posinfo, *dstinfo;
	int segs = 0;
	int val1 = INT_MAX, val2 = INT_MAX;
	int val, val1_new, val2_new;
	struct route_path *ret;


	dbg(0, "RPNEW:ENTER\n");

	dbg(0, "RR_TIM:001\n");

	if (!pos->street || !dst->street)
	{
		// dbg(0, "pos or dest not set\n");
		dbg(0, "RR_TIM:002:return\n");

		dbg(0, "RPNEW:RET001\n");
		return2 NULL;
	}

	if (profile->mode == 2 || (profile->mode == 0 && pos->lenextra + dst->lenextra > transform_distance(map_projection(pos->street->item.map), &pos->c, &dst->c)))
	{
		dbg(0, "RPNEW:RET002\n");
		return2 route_path_new_offroad(this, pos, dst);
	}

	// ------ just calculate the smallest cost to reach destination ----------
	// only segments connected to my current street position!
	// ------ just calculate the smallest cost to reach destination ----------
	while ((s = route_graph_get_segment(this, pos->street, s)))
	{
		val = route_value_seg(profile, NULL, s, 1);
		if (val != INT_MAX && s->end->value != INT_MAX)
		{
			val = val * (100 - pos->percent) / 100;
			val1_new = s->end->value + val;
			if (val1_new < val1)
			{
				val1 = val1_new;
				s1 = s;
			}
		}
		val = route_value_seg(profile, NULL, s, -1);
		if (val != INT_MAX && s->start->value != INT_MAX)
		{
			val = val * pos->percent / 100;
			val2_new = s->start->value + val;
			if (val2_new < val2)
			{
				val2 = val2_new;
				s2 = s;
			}
		}
	}
	// ------ just calculate the smallest cost to reach destination ----------
	// ------ just calculate the smallest cost to reach destination ----------

	dbg(0, "RR_TIM:002\n");


	if (val1 == INT_MAX && val2 == INT_MAX)
	{
		//dbg(0, "no route found, pos blocked\n");
		dbg(0, "RR_TIM:003:return\n");
		dbg(0, "RPNEW:RET003\n");

		return2 NULL;
	}

	if (val1 == val2)
	{
		val1 = s1->end->value;
		val2 = s2->start->value;
	}

	if (val1 < val2)
	{
		start = s1->start;	// start point
		s = s1;				// start segment
	}
	else
	{
		start = s2->end;	// start point
		s = s2;				// start segment
	}

	ret=g_new0(struct route_path, 1);
	ret->in_use = 1;
	ret->updated = 1;

	if (pos->lenextra)
	{
		//dbg(0,"l extra1=%d\n", pos->lenextra);
		if (rr->pos != pos)
		{
			dbg(0, "RPNEW:PP:not pos\n");
			// route_path_add_line_as_waypoint(ret, &pos->c, &pos->lp, pos->lenextra); // mark as waypoint!!
			route_path_add_line(ret, &pos->c, &pos->lp, pos->lenextra); // ORIG
		}
		else
		{
			dbg(0, "RPNEW:PP:IS pos\n");
			route_path_add_line(ret, &pos->c, &pos->lp, pos->lenextra); // ORIG
		}
	}

	ret->path_hash = item_hash_new();
	dstinfo = NULL;
	posinfo = pos;
	first = s; // first segment = start segment


	dbg(0, "RR_TIM:004\n");

	clock_t s_;
	s_ = debug_measure_start();


	// ------- build the real route here ------------------------
	// ------- build the real route here ------------------------
	while (s && !dstinfo)
	{ /* following start->seg, which indicates the least costly way to reach our destination */
		segs++;
#if 0
		printf("start->value=%d 0x%x,0x%x\n", start->value, start->c.x, start->c.y);
#endif
		if (s->start == start)
		{
			if (item_is_equal(s->data.item, dst->street->item) && (s->end->seg == s || !posinfo))
			{
				dstinfo = dst;
			}

			if (!route_path_add_item_from_graph(ret, oldpath, s, 1, posinfo, dstinfo))
			{
				ret->updated = 0;
			}

			start = s->end; // new start point
		}
		else
		{
			if (item_is_equal(s->data.item, dst->street->item) && (s->start->seg == s || !posinfo))
			{
				dstinfo = dst;
			}
			if (!route_path_add_item_from_graph(ret, oldpath, s, -1, posinfo, dstinfo))
			{
				ret->updated = 0;
			}
			start = s->start; // new start point
		}

		posinfo = NULL;
		s = start->seg; // segment to use in direction of destination

	}
	// ------- build the real route here ------------------------
	// ------- build the real route here ------------------------

	debug_mrp("RR_TIM:005", debug_measure_end(s_));

#if 0
	if (dst->lenextra)
	{
		//dbg(0,"l extra2=%d\n", dst->lenextra);
		route_path_add_line(ret, &dst->lp, &dst->c, dst->lenextra);
	}
#endif

	if (dst->lenextra)
	{
		struct route_info *rr_last = g_list_last(rr->destinations)->data;

		//dbg(0,"l extra1=%d\n", pos->lenextra);
		if (rr_last != dst)
		{
			dbg(0, "RPNEW:PP:not dst\n");
			route_path_add_line_as_waypoint(ret, &dst->lp, &dst->c, dst->lenextra);
		}
		else
		{
			dbg(0, "RPNEW:PP:IS dst\n");
			route_path_add_line(ret, &dst->lp, &dst->c, dst->lenextra);
		}
	}


	//dbg(1, "%d segments\n", segs);

	dbg(0, "RR_TIM:099:segs=%d\n", segs);

	dbg(0, "RPNEW:LEAVE\n");

	return2 ret;

__F_END__
}

static int route_graph_build_next_map(struct route_graph *rg)
{
__F_START__

	do
	{
		rg->m = mapset_next(rg->h, 2);
		if (!rg->m)
		{
			return2 0;
		}

#if 0
		struct attr map_name_attr;
		if (map_get_attr(rg->m, attr_name, &map_name_attr, NULL))
		{
			if (strncmp("_ms_sdcard_map:-special-:worldmap", map_name_attr.u.str, 34) == 0)
			{
				continue;
			}
		}
#endif

		map_rect_destroy(rg->mr);
		rg->mr = map_rect_new(rg->m, rg->sel);
	}
	while (!rg->mr);

	return2 1;

__F_END__
}

int is_turn_allowed(struct route_graph_point *p, struct route_graph_segment *from, struct route_graph_segment *to)
{
	struct route_graph_point *prev, *next;
	struct route_graph_segment *tmp1, *tmp2;

	if (item_is_equal(from->data.item, to->data.item))
	{
		return 0;
	}

	if (from->start == p)
		prev = from->end;
	else
		prev = from->start;

	if (to->start == p)
		next = to->end;
	else
		next = to->start;

	tmp1 = p->end;

	while (tmp1)
	{
		if (tmp1->start->c.x == prev->c.x && tmp1->start->c.y == prev->c.y && (tmp1->data.item.type == type_street_turn_restriction_no || tmp1->data.item.type == type_street_turn_restriction_only))
		{
			tmp2 = p->start;
			//dbg(1, "found %s (0x%x,0x%x) (0x%x,0x%x)-(0x%x,0x%x) %p-%p\n",
			//		item_to_name(tmp1->data.item.type), tmp1->data.item.id_hi,
			//		tmp1->data.item.id_lo, tmp1->start->c.x, tmp1->start->c.y,
			//		tmp1->end->c.x, tmp1->end->c.y, tmp1->start, tmp1->end);
			while (tmp2)
			{
				//dbg(
				//		1,
				//		"compare %s (0x%x,0x%x) (0x%x,0x%x)-(0x%x,0x%x) %p-%p\n",
				//		item_to_name(tmp2->data.item.type),
				//		tmp2->data.item.id_hi, tmp2->data.item.id_lo,
				//		tmp2->start->c.x, tmp2->start->c.y, tmp2->end->c.x,
				//		tmp2->end->c.y, tmp2->start, tmp2->end);
				if (item_is_equal(tmp1->data.item, tmp2->data.item))
				{
					break;
				}
				tmp2 = tmp2->start_next;
			}
			//dbg(1, "tmp2=%p\n", tmp2);
			if (tmp2)
			{
				//dbg(1, "%s tmp2->end=%p next=%p\n",
				//		item_to_name(tmp1->data.item.type), tmp2->end, next);
			}
			if (tmp1->data.item.type == type_street_turn_restriction_no && tmp2 && tmp2->end->c.x == next->c.x && tmp2->end->c.y == next->c.y)
			{
				//dbg(
				//		1,
				//		"from 0x%x,0x%x over 0x%x,0x%x to 0x%x,0x%x not allowed (no)\n",
				//		prev->c.x, prev->c.y, p->c.x, p->c.y, next->c.x,
				//		next->c.y);
				return 0;
			}
			if (tmp1->data.item.type == type_street_turn_restriction_only && tmp2 && (tmp2->end->c.x != next->c.x || tmp2->end->c.y != next->c.y))
			{
				//dbg(
				//		1,
				//		"from 0x%x,0x%x over 0x%x,0x%x to 0x%x,0x%x not allowed (only)\n",
				//		prev->c.x, prev->c.y, p->c.x, p->c.y, next->c.x,
				//		next->c.y);
				return 0;
			}
		}
		tmp1 = tmp1->end_next;
	}
	//dbg(1, "from 0x%x,0x%x over 0x%x,0x%x to 0x%x,0x%x allowed\n", prev->c.x,
	//		prev->c.y, p->c.x, p->c.y, next->c.x, next->c.y);
	return 1;

}

static void route_graph_clone_segment(struct route_graph *this, struct route_graph_segment *s, struct route_graph_point *start, struct route_graph_point *end, int flags)
{
	struct route_graph_segment_data data;
	data.flags = s->data.flags | flags;
	data.offset = 1;
	data.maxspeed = -1;
	data.item = &s->data.item;
	data.len = s->data.len + 1;

	if (s->data.flags & NAVIT_AF_SPEED_LIMIT)
		data.maxspeed = RSD_MAXSPEED(&s->data);

	if (s->data.flags & NAVIT_AF_SEGMENTED)
		data.offset = RSD_OFFSET(&s->data);

	//dbg(1, "cloning segment from %p (0x%x,0x%x) to %p (0x%x,0x%x)\n", start,
	//		start->c.x, start->c.y, end, end->c.x, end->c.y);
	route_graph_add_segment(this, start, end, &data);

}

static void route_graph_process_restriction_segment(struct route_graph *this, struct route_graph_point *p, struct route_graph_segment *s, int dir)
{
	struct route_graph_segment *tmp;
	struct route_graph_point *pn;
	struct coord c = p->c;
	int dx = 0;
	int dy = 0;
	c.x += dx;
	c.y += dy;

	//dbg(1, "From %s %d,%d\n", item_to_name(s->data.item.type), dx, dy);
	pn = route_graph_point_new(this, &c);
	if (dir > 0)
	{ /* going away */
		//dbg(1, "other 0x%x,0x%x\n", s->end->c.x, s->end->c.y);
		//dbg(0, "fl 001 = %x\n", s->data.flags);
		if (route_get_real_oneway_flag(s->data.flags, NAVIT_AF_ONEWAY))
		{
			//dbg(1, "Not possible\n");
			return;
		}
		// route_graph_clone_segment(this, s, pn, s->end, NAVIT_AF_ONEWAYREV|(s->data.flags & NAVIT_AF_ONEWAY_BICYCLE_NO));
		route_graph_clone_segment(this, s, pn, s->end, NAVIT_AF_ONEWAYREV);
	}
	else
	{ /* coming in */
		//dbg(1, "other 0x%x,0x%x\n", s->start->c.x, s->start->c.y);
		//dbg(0, "fl 002 = %x\n", s->data.flags);
		if (route_get_real_oneway_flag(s->data.flags, NAVIT_AF_ONEWAYREV))
		{
			//dbg(1, "Not possible\n");
			return;
		}
		// route_graph_clone_segment(this, s, s->start, pn, NAVIT_AF_ONEWAY|(s->data.flags & NAVIT_AF_ONEWAY_BICYCLE_NO));
		route_graph_clone_segment(this, s, s->start, pn, NAVIT_AF_ONEWAY);
	}

	tmp = p->start;
	while (tmp)
	{
		if (tmp != s && tmp->data.item.type != type_street_turn_restriction_no && tmp->data.item.type != type_street_turn_restriction_only && !(route_get_real_oneway_flag(tmp->data.flags, NAVIT_AF_ONEWAYREV)) && is_turn_allowed(p, s, tmp))
		{
			//dbg(0, "fl 003 = %x\n", tmp->data.flags);
			// route_graph_clone_segment(this, tmp, pn, tmp->end, NAVIT_AF_ONEWAY|(tmp->data.flags & NAVIT_AF_ONEWAY_BICYCLE_NO));
			route_graph_clone_segment(this, tmp, pn, tmp->end, NAVIT_AF_ONEWAY);
			//dbg(1, "To start %s\n", item_to_name(tmp->data.item.type));
		}
		tmp = tmp->start_next;
	}

	tmp = p->end;
	while (tmp)
	{
		if (tmp != s && tmp->data.item.type != type_street_turn_restriction_no && tmp->data.item.type != type_street_turn_restriction_only && !(route_get_real_oneway_flag(tmp->data.flags, NAVIT_AF_ONEWAY)) && is_turn_allowed(p, s, tmp))
		{
			//dbg(0, "fl 004 = %x\n", tmp->data.flags);
			// route_graph_clone_segment(this, tmp, tmp->start, pn, NAVIT_AF_ONEWAYREV|(tmp->data.flags & NAVIT_AF_ONEWAY_BICYCLE_NO));
			route_graph_clone_segment(this, tmp, tmp->start, pn, NAVIT_AF_ONEWAYREV);
			//dbg(1, "To end %s\n", item_to_name(tmp->data.item.type));
		}
		tmp = tmp->end_next;
	}

}

// -- UNUSED --
// -- UNUSED --
static void route_graph_process_traffic_light_segment(struct route_graph *this, struct route_graph_point *p, struct route_graph_segment *s, int dir)
{
__F_START__

	//struct route_graph_segment *tmp;
	//s->data.len = s->data.len + global_traffic_light_delay;

__F_END__
}


static void route_graph_process_restriction_point(struct route_graph *this, struct route_graph_point *p)
{

	struct route_graph_segment *tmp;

	// -------------------------------------------------------------
	// p->start ==> a list of all streets that start from this point
	// -------------------------------------------------------------
	tmp = p->start;
	//dbg(1, "node 0x%x,0x%x\n", p->c.x, p->c.y);
	while (tmp)
	{
		if (tmp->data.item.type != type_street_turn_restriction_no && tmp->data.item.type != type_street_turn_restriction_only)
		{
			route_graph_process_restriction_segment(this, p, tmp, 1);
		}
		tmp = tmp->start_next;
	}

	// -------------------------------------------------------------
	// p->end ==> a list of all streets that end at this point
	// -------------------------------------------------------------
	tmp = p->end;
	while (tmp)
	{
		if (tmp->data.item.type != type_street_turn_restriction_no && tmp->data.item.type != type_street_turn_restriction_only)
		{
			route_graph_process_restriction_segment(this, p, tmp, -1);
		}
		tmp = tmp->end_next;
	}

	p->flags |= RP_TURN_RESTRICTION_RESOLVED;

}

// -- UNUSED --
// -- UNUSED --
static void route_graph_process_traffic_light_point(struct route_graph *this, struct route_graph_point *p)
{
__F_START__

	struct route_graph_segment *tmp;

	// -------------------------------------------------------------
	// p->start ==> a list of all streets that start from this point
	// -------------------------------------------------------------
	tmp = p->start;
	while (tmp)
	{
		route_graph_process_traffic_light_segment(this, p, tmp, 1);
		tmp = tmp->start_next;
	}

	// -------------------------------------------------------------
	// p->end ==> a list of all streets that end at this point
	// -------------------------------------------------------------
	tmp = p->end;
	while (tmp)
	{
		route_graph_process_traffic_light_segment(this, p, tmp, -1);
		tmp = tmp->end_next;
	}

	// p->flags |= RP_TRAFFIC_LIGHT_RESOLVED;

__F_END__
}


static void route_graph_process_restrictions(struct route_graph *this)
{
__F_START__

	struct route_graph_point *curr;
	int i;
	//dbg(1, "enter\n");
	for (i = 0; i < HASH_SIZE; i++)
	{
		curr = this->hash[i];
		while (curr)
		{
			if (curr->flags & RP_TURN_RESTRICTION)
			{
				route_graph_process_restriction_point(this, curr);
			}
			curr = curr->hash_next;
		}
	}

__F_END__
}

// -- UNUSED --
// -- UNUSED --
static void route_graph_process_traffic_lights(struct route_graph *this)
{
__F_START__

	// check if we should calc delay for traffic lights?
	if (global_traffic_light_delay > 0)
	{
		struct route_graph_point *curr;
		int i;
		//dbg(1, "enter\n");
		for (i = 0; i < HASH_SIZE; i++)
		{
			curr = this->hash[i];
			while (curr)
			{
				if (curr->flags & RP_TRAFFIC_LIGHT)
				{
					route_graph_process_traffic_light_point(this, curr);
				}
				curr = curr->hash_next;
			}
		}
	}

__F_END__
}


static void route_graph_build_done(struct route_graph *rg, int cancel)
{
__F_START__

	//dbg(1, "cancel=%d\n", cancel);
	if (rg->idle_ev)
	{
		event_remove_idle(rg->idle_ev);
		rg->idle_ev = NULL;
	}

	if (rg->idle_cb)
	{
		callback_destroy(rg->idle_cb);
		rg->idle_cb = NULL;
	}

	map_rect_destroy(rg->mr);
	mapset_close(rg->h);
	route_free_selection(rg->sel);
	rg->mr = NULL;
	rg->h = NULL;
	rg->sel = NULL;

	if (!cancel)
	{
		route_graph_process_restrictions(rg);
		// --unused-- route_graph_process_traffic_lights(rg);
		//dbg(0, "callback\n");
		callback_call_0(rg->done_cb);
	}

	rg->busy = 0;

__F_END__
}

// this function gets called in a loop until route is ready ----------
// this function gets called in a loop until route is ready ----------
// this function gets called in a loop until route is ready ----------
static void route_graph_build_idle(struct route_graph *rg, struct vehicleprofile *profile)
{
__F_START__

#ifdef DEBUG_GLIB_MEM_FUNCTIONS
	g_mem_profile();
#endif

	// int count = 1000; // *ORIG* value
	// int count = 1200; // process 5000 items in one step
	int count = 4000; // process 15000 items in one step
	struct item *item;

	while (count > 0)
	{
		// loop until an item is found ---------
		for (;;)
		{
			item = map_rect_get_item(rg->mr);
			if (item)
			{
				break;
			}

			if (!route_graph_build_next_map(rg))
			{
				route_graph_build_done(rg, 0);
				return2;
			}
		}
		// loop until an item is found ---------


		if (item->type == type_traffic_distortion)
		{
			route_process_traffic_distortion(rg, item);
		}
/*
		else if (
				(item->type == type_street_2_city) ||
				(item->type == type_street_3_city) ||
				(item->type == type_street_4_city) ||
				(item->type == type_highway_city) ||
				(item->type == type_street_1_land) ||
				(item->type == type_street_2_land) ||
				(item->type == type_street_3_land) ||
				(item->type == type_street_4_land) ||
				(item->type == type_street_n_lanes) ||
				(item->type == type_highway_land) ||
				(item->type == type_ramp) ||
				)
		{
*/
/*
ITEM(street_0)
ITEM(street_1_city)
ITEM(street_2_city)
ITEM(street_3_city)
ITEM(street_4_city)
ITEM(highway_city)
ITEM(street_1_land)
ITEM(street_2_land)
ITEM(street_3_land)
ITEM(street_4_land)
ITEM(street_n_lanes)
ITEM(highway_land)
ITEM(ramp)
*/
			// route_process_sharp_turn(rg, item);
//		}
		else if (item->type == type_traffic_signals)
		{
			route_process_traffic_light(rg, item);
		}
		else if (item->type == type_street_turn_restriction_no || item->type == type_street_turn_restriction_only)
		{
			route_process_turn_restriction(rg, item);
		}
		else
		{
			//dbg(0,"*[xx]type=%s\n", item_to_name(item->type));
			route_process_street_graph(rg, item, profile);
		}

		count--;
	}

__F_END__
}

/**
 * @brief Builds a new route graph from a mapset
 *
 * This function builds a new route graph from a map. Please note that this function does not
 * add any routing information to the route graph - this has to be done via the route_graph_flood()
 * function.
 *
 * The function does not create a graph covering the whole map, but only covering the rectangle
 * between c1 and c2.
 *
 * @param ms The mapset to build the route graph from
 * @param c1 Corner 1 of the rectangle to use from the map
 * @param c2 Corner 2 of the rectangle to use from the map
 * @param done_cb The callback which will be called when graph is complete
 * @return The new route graph.
 */
static struct route_graph *
route_graph_build(struct mapset *ms, struct coord *c, int count, struct callback *done_cb, int async, struct vehicleprofile *profile, int try_harder)
{
__F_START__

	dbg(0, "RR_TIM:rgb:001\n");

	struct route_graph *ret=g_new0(struct route_graph, 1);

	ret->sel = route_calc_selection(c, count, try_harder);

	ret->h = mapset_open(ms);
	ret->done_cb = done_cb;
	ret->busy = 1;

	if (route_graph_build_next_map(ret))
	{
		// ------ xxxxxx ----
		// dbg(0,"async=%d\n", async);
		if (async)
		{
			//dbg(0,"route_graph_build_idle sync callback\n");
			ret->idle_cb = callback_new_2(callback_cast(route_graph_build_idle), ret, profile);
			callback_add_names(ret->idle_cb, "route_graph_build", "route_graph_build_idle");
			ret->idle_ev = event_add_idle(10, ret->idle_cb);
		}
		else
		{
			// ?? do we need this ?? // route_graph_build_idle(ret, profile);
		}
	}
	else
	{
		route_graph_build_done(ret, 0);
	}

	dbg(0, "RR_TIM:rgb:099\n");

	return2 ret;

__F_END__
}

static void route_graph_update_done(struct route *this, struct callback *cb)
{
__F_START__

	dbg(0, "RR_TIM:gud:001\n");

	route_graph_flood(this->graph, this->current_dst, this->pos, this->vehicleprofile, cb);

	dbg(0, "RR_TIM:gud:099\n");

__F_END__
}

/**
 * @brief Updates the route graph
 *
 * This updates the route graph after settings in the route have changed. It also
 * adds routing information afterwards by calling route_graph_flood().
 * 
 * @param this The route to update the graph for
 */
static void route_graph_update(struct route *this, struct callback *cb, int async)
{
__F_START__

	dbg(0, "RR_TIM:gu:001\n");

	struct attr route_status;
	struct coord *c = g_alloca(sizeof(struct coord) * (1 + g_list_length(this->destinations)));
	int i = 0;
	GList *tmp;

	route_status.type = attr_route_status;
	route_graph_destroy(this->graph);
	this->graph = NULL;
	callback_destroy(this->route_graph_done_cb);

	this->route_graph_done_cb = callback_new_2(callback_cast(route_graph_update_done), this, cb);
	callback_add_names(this->route_graph_done_cb, "route_graph_update", "route_graph_update_done");

	route_status.u.num = route_status_building_graph;
	route_set_attr(this, &route_status);

	c[i++] = this->pos->c;
	tmp = this->destinations;
	while (tmp)
	{
		struct route_info *dst = tmp->data;
		c[i++] = dst->c;
		tmp = g_list_next(tmp);
	}

	this->graph = route_graph_build(this->ms, c, i, this->route_graph_done_cb, async, this->vehicleprofile, this->try_harder);

	if (!async)
	{
		while (this->graph->busy)
		{
			route_graph_build_idle(this->graph, this->vehicleprofile);
		}
	}

	dbg(0, "RR_TIM:gu:099\n");

__F_END__
}

/**
 * @brief Gets street data for an item
 *
 * @param item The item to get the data for
 * @return Street data for the item
 */
struct street_data *
street_get_data(struct item *item)
{
	//// dbg(0, "enter\n");

	int count = 0, *flags;
	struct street_data *ret = NULL, *ret1;
	struct attr flags_attr, maxspeed_attr;
	const int step = 128;
	int c;

	do
	{
		ret1 = g_realloc(ret, sizeof(struct street_data) + (count + step) * sizeof(struct coord));
		if (!ret1)
		{
			if (ret)
				g_free(ret);
			return NULL;
		}
		ret = ret1;
		c = item_coord_get(item, &ret->c[count], step);
		count += c;
	}
	while (c && c == step);

	ret1 = g_realloc(ret, sizeof(struct street_data) + count * sizeof(struct coord));

	if (ret1)
		ret = ret1;

	ret->item = *item;
	ret->count = count;

	if (item_attr_get(item, attr_flags, &flags_attr))
	{
		ret->flags = flags_attr.u.num;
	}
	else
	{
		flags = item_get_default_flags(item->type);

		if (flags)
			ret->flags = *flags;
		else
			ret->flags = 0;
	}

	ret->maxspeed = -1;
	if (ret->flags & NAVIT_AF_SPEED_LIMIT)
	{
		if (item_attr_get(item, attr_maxspeed, &maxspeed_attr))
		{
			ret->maxspeed = maxspeed_attr.u.num;
		}
	}

	return ret;
}

/**
 * @brief Copies street data
 * 
 * @param orig The street data to copy
 * @return The copied street data
 */
struct street_data *
street_data_dup(struct street_data *orig)
{
	//// dbg(0, "enter\n");

	struct street_data *ret;
	int size = sizeof(struct street_data) + orig->count * sizeof(struct coord);

	ret = g_malloc(size);
	// xxx global_route_memory_size = global_route_memory_size + seg_size + seg_dat_size;
	// xxx dbg(0,"route mem=%lu\n", global_route_memory_size);
	memcpy(ret, orig, size);

	return ret;
}

/**
 * @brief Frees street data
 *
 * @param sd Street data to be freed
 */
void street_data_free(struct street_data *sd)
{
	//// dbg(0, "enter\n");

	g_free(sd);
}

/**
 * @brief Finds the nearest street to a given coordinate (result should be a road that is in vehicle profile for routing!)
 *
 * @param ms The mapset to search in for the street
 * @param pc The coordinate to find a street nearby
 * @return The nearest street
 */
static struct route_info *
route_find_nearest_street(struct vehicleprofile *vehicleprofile, struct mapset *ms, struct pcoord *pc)
{
__F_START__

	struct route_info *ret = NULL;
	int max_dist = 1000; // was 1000 originally!!
	struct map_selection *sel;
	int dist, mindist = 0, pos;
	struct mapset_handle *h;
	struct map *m;
	struct map_rect *mr;
	struct item *item;
	struct coord lp;
	struct street_data *sd;
	struct coord c;
	struct coord_geo g;
	struct roadprofile *roadp;

	ret=g_new0(struct route_info, 1);
	mindist = INT_MAX;

	h = mapset_open(ms);
	while ((m = mapset_next(h, 2)))
	{
		c.x = pc->x;
		c.y = pc->y;

		if (map_projection(m) != pc->pro)
		{
			transform_to_geo(pc->pro, &c, &g);
			transform_from_geo(map_projection(m), &g, &c);
		}

		sel = route_rect(18, &c, &c, 0, max_dist);

		if (!sel)
		{
			continue;
		}

		mr = map_rect_new(m, sel);
		//dbg(0, "sel lu=%d,%d rl=%d,%d\n", sel->u.c_rect.lu.x, sel->u.c_rect.lu.y, sel->u.c_rect.rl.x, sel->u.c_rect.rl.y);

		if (!mr)
		{
			map_selection_destroy(sel);
			continue;
		}

		while ((item = map_rect_get_item(mr)))
		{
			roadp = vehicleprofile_get_roadprofile(vehicleprofile, item->type);
			if (roadp)
			{
				if (item_get_default_flags(item->type))
				{
					sd = street_get_data(item);
					if (!sd)
					{
						continue;
					}

					//dbg(0,"*type=%s\n", item_to_name(item->type));

					dist = transform_distance_polyline_sq(sd->c, sd->count, &c, &lp, &pos);

					if (dist < mindist && ((sd->flags & route_get_real_oneway_mask(sd->flags, vehicleprofile->flags_forward_mask)) == vehicleprofile->flags || (sd->flags & route_get_real_oneway_mask(sd->flags, vehicleprofile->flags_reverse_mask)) == vehicleprofile->flags))
					{
						mindist = dist;
						if (ret->street)
						{
							street_data_free(ret->street);
						}
						ret->c = c;
						ret->lp = lp;
						ret->pos = pos;
						ret->street = sd;

						//dbg(0,"*(N)type=%s\n", item_to_name(item->type));
						/*
						struct attr street_name_attr;
						if (item_attr_get(item, attr_label, &street_name_attr))
						{
							dbg(0, "*name=%s\n", street_name_attr.u.str);
						}
						else if (item_attr_get(item, attr_street_name, &street_name_attr))
						{
							dbg(0, "*name=%s\n", street_name_attr.u.str);
						}
						*/
						//dbg(0, "dist=%d id 0x%x 0x%x pos=%d\n", dist, item->id_hi, item->id_lo, pos);
					}
					else
					{
						street_data_free(sd);
					}
				}
			}
		}
		map_selection_destroy(sel);
		map_rect_destroy(mr);
	}
	mapset_close(h);

	if (!ret->street || mindist > max_dist * max_dist)
	{
		if (ret->street)
		{
			street_data_free(ret->street);
			//dbg(0, "Much too far %d > %d\n", mindist, max_dist);
		}

		//dbg(0,"return NULL\n");

		g_free(ret);
		ret = NULL;
	}

	return2 ret;

__F_END__
}



static struct route_info *
route_find_nearest_street_harder(struct vehicleprofile *vehicleprofile, struct mapset *ms, struct pcoord *pc, int max_dist_wanted)
{
__F_START__

	struct route_info *ret = NULL;
	int max_dist = max_dist_wanted; // was 1000 originally!!
	struct map_selection *sel;
	int dist, mindist = 0, pos;
	struct mapset_handle *h;
	struct map *m;
	struct map_rect *mr;
	struct item *item;
	struct coord lp;
	struct street_data *sd;
	struct coord c;
	struct coord_geo g;
	struct roadprofile *roadp;

	ret=g_new0(struct route_info, 1);
	mindist = INT_MAX;

	h = mapset_open(ms);
	while ((m = mapset_next(h, 2)))
	{
		c.x = pc->x;
		c.y = pc->y;

		if (map_projection(m) != pc->pro)
		{
			transform_to_geo(pc->pro, &c, &g);
			transform_from_geo(map_projection(m), &g, &c);
		}

		sel = route_rect(18, &c, &c, 0, max_dist);

		if (!sel)
		{
			continue;
		}

		//dbg(0, "sel lu=%d,%d rl=%d,%d\n", sel->u.c_rect.lu.x, sel->u.c_rect.lu.y, sel->u.c_rect.rl.x, sel->u.c_rect.rl.y);

		mr = map_rect_new(m, sel);

		if (!mr)
		{
			map_selection_destroy(sel);
			continue;
		}

		while ((item = map_rect_get_item(mr)))
		{
			roadp = vehicleprofile_get_roadprofile(vehicleprofile, item->type);
			if (roadp)
			{
				if (item_get_default_flags(item->type))
				{
					sd = street_get_data(item);
					if (!sd)
					{
						continue;
					}

					//dbg(0,"*type=%s\n", item_to_name(item->type));

					dist = transform_distance_polyline_sq(sd->c, sd->count, &c, &lp, &pos);

					if (dist < mindist && ((sd->flags & route_get_real_oneway_mask(sd->flags, vehicleprofile->flags_forward_mask)) == vehicleprofile->flags || (sd->flags & route_get_real_oneway_mask(sd->flags, vehicleprofile->flags_reverse_mask)) == vehicleprofile->flags))
					{
						mindist = dist;
						if (ret->street)
						{
							street_data_free(ret->street);
						}
						ret->c = c;
						ret->lp = lp;
						ret->pos = pos;
						ret->street = sd;

						/*
						dbg(0,"*(h)type=%s\n", item_to_name(item->type));
						struct attr street_name_attr;
						if (item_attr_get(item, attr_label, &street_name_attr))
						{
							dbg(0, "*name=%s\n", street_name_attr.u.str);
						}
						else if (item_attr_get(item, attr_street_name, &street_name_attr))
						{
							dbg(0, "*name=%s\n", street_name_attr.u.str);
						}
						*/
						//dbg(0, "dist=%d id 0x%x 0x%x pos=%d\n", dist, item->id_hi, item->id_lo, pos);
					}
					else
					{
						street_data_free(sd);
					}
				}
			}
		}
		map_selection_destroy(sel);
		map_rect_destroy(mr);
	}
	mapset_close(h);

	if (!ret->street || mindist > (max_dist * max_dist))
	{

		//dbg(0,"no street found!\n");

		if (ret->street)
		{
			street_data_free(ret->street);
			//dbg(0, "Much too far %d > %d\n", mindist, max_dist);
		}
		g_free(ret);
		ret = NULL;
	}

	return2 ret;

__F_END__
}


/**
 * @brief Destroys a route_info
 *
 * @param info The route info to be destroyed
 */
void route_info_free(struct route_info *inf)
{
__F_START__

	if (!inf)
	{
		return2;
	}

	if (inf->street)
		street_data_free(inf->street);

	g_free(inf);

__F_END__
}

#include "point.h"

/**
 * @brief Returns street data for a route info 
 *
 * @param rinf The route info to return the street data for
 * @return Street data for the route info
 */
struct street_data *
route_info_street(struct route_info *rinf)
{
__F_START__

	return2 rinf->street;

__F_END__
}

#if 0
struct route_crossings *
route_crossings_get(struct route *this, struct coord *c)
{
	struct route_point *pnt;
	struct route_segment *seg;
	int crossings=0;
	struct route_crossings *ret;

	pnt=route_graph_get_point(this, c);
	seg=pnt->start;
	while (seg)
	{
		printf("start: 0x%x 0x%x\n", seg->item.id_hi, seg->item.id_lo);
		crossings++;
		seg=seg->start_next;
	}
	seg=pnt->end;
	while (seg)
	{
		printf("end: 0x%x 0x%x\n", seg->item.id_hi, seg->item.id_lo);
		crossings++;
		seg=seg->end_next;
	}
	ret=g_malloc(sizeof(struct route_crossings)+crossings*sizeof(struct route_crossing));
	ret->count=crossings;
	return ret;
}
#endif

struct map_rect_priv
{
	struct route_info_handle *ri;
	enum attr_type attr_next;
	int pos;
	struct map_priv *mpriv;
	struct item item;
	unsigned int last_coord;
	struct route_path *path;
	struct route_path_segment *seg, *seg_next;
	struct route_graph_point *point;
	struct route_graph_segment *rseg;
	char *str;
	int hash_bucket;
	struct coord *coord_sel; /**< Set this to a coordinate if you want to filter for just a single route graph point */
	struct route_graph_point_iterator it;
};

static void rm_coord_rewind(void *priv_data)
{
	//// dbg(0, "enter\n");

	struct map_rect_priv *mr = priv_data;
	mr->last_coord = 0;
}

static void rm_attr_rewind(void *priv_data)
{
	//// dbg(0, "enter\n");

	struct map_rect_priv *mr = priv_data;
	mr->attr_next = attr_street_item;
}

static int rm_attr_get(void *priv_data, enum attr_type attr_type, struct attr *attr)
{
	//// dbg(0, "enter\n");

	struct map_rect_priv *mr = priv_data;
	struct route_path_segment *seg = mr->seg;
	struct route *route = mr->mpriv->route;

	if ((mr->item.type != type_street_route) && (mr->item.type != type_street_route_waypoint))
	{
		return 0;
	}

	attr->type = attr_type;
	switch (attr_type)
	{
		case attr_any:
			while (mr->attr_next != attr_none)
			{
				if (rm_attr_get(priv_data, mr->attr_next, attr))
					return 1;
			}
			return 0;
		case attr_maxspeed:
			mr->attr_next = attr_street_item;
			if (seg && seg->data->flags & NAVIT_AF_SPEED_LIMIT)
			{
				attr->u.num = RSD_MAXSPEED(seg->data);
			}
			else
			{
				return 0;
			}
			return 1;
		case attr_details: // use this dummy attr "details" to get street flags
			mr->attr_next = attr_street_item;
			attr->u.num = seg->data->flags;
			return 1;
		case attr_street_item:
			mr->attr_next = attr_direction;
			if (seg && seg->data->item.map)
				attr->u.item = &seg->data->item;
			else
				return 0;
			return 1;
		case attr_direction:
			mr->attr_next = attr_route;
			if (seg)
				attr->u.num = seg->direction;
			else
				return 0;
			return 1;
		case attr_route:
			mr->attr_next = attr_length;
			attr->u.route = mr->mpriv->route;
			return 1;
		case attr_length:
			mr->attr_next = attr_time;
			if (seg)
				attr->u.num = seg->data->len;
			else
				return 0;
			return 1;
		case attr_time:
			mr->attr_next = attr_speed;
			if (seg)
				attr->u.num = route_time_seg(route->vehicleprofile, seg->data, NULL);
			else
				return 0;
			return 1;
		case attr_speed:
			mr->attr_next = attr_none;
			if (seg)
				attr->u.num = route_seg_speed_real(route->vehicleprofile, seg->data, NULL);
			else
				return 0;
			return 1;
		case attr_label:
			mr->attr_next = attr_none;
			return 0;
		default:
			mr->attr_next = attr_none;
			attr->type = attr_none;
			return 0;
	}
	return 0;
}

static int rm_coord_get(void *priv_data, struct coord *c, int count)
{
	//// dbg(0, "enter\n");

	struct map_rect_priv *mr = priv_data;
	struct route_path_segment *seg = mr->seg;
	int i, rc = 0;
	struct route *r = mr->mpriv->route;
	enum projection pro = route_projection(r);

	if (pro == projection_none)
		return 0;

	if (mr->item.type == type_route_start || mr->item.type == type_route_start_reverse || mr->item.type == type_route_end)
	{
		if (!count || mr->last_coord)
			return 0;

		mr->last_coord = 1;

		if (mr->item.type == type_route_start || mr->item.type == type_route_start_reverse)
		{
			c[0] = r->pos->c;
		}
		else
		{
			c[0] = route_get_dst(r)->c;
		}
		return 1;
	}

	if (!seg)
		return 0;

	for (i = 0; i < count; i++)
	{
		if (mr->last_coord >= seg->ncoords)
			break;

		if (i >= seg->ncoords)
			break;

		if (pro != projection_mg)
		{
			transform_from_to(&seg->c[mr->last_coord++], pro, &c[i], projection_mg);
		}
		else
		{
			c[i] = seg->c[mr->last_coord++];
		}

		rc++;
	}
	//dbg(1, "return %d\n", rc);
	return rc;
}

static struct item_methods methods_route_item = { rm_coord_rewind, rm_coord_get, rm_attr_rewind, rm_attr_get, };

static void rp_attr_rewind(void *priv_data)
{
	//// dbg(0, "enter\n");

	struct map_rect_priv *mr = priv_data;
	mr->attr_next = attr_label;
}

static int rp_attr_get(void *priv_data, enum attr_type attr_type, struct attr *attr)
{
	//// dbg(0, "enter\n");

	struct map_rect_priv *mr = priv_data;
	struct route_graph_point *p = mr->point;
	struct route_graph_segment *seg = mr->rseg;
	struct route *route = mr->mpriv->route;

	attr->type = attr_type;
	switch (attr_type)
	{
		case attr_any: // works only with rg_points for now
			while (mr->attr_next != attr_none)
			{
				//dbg(0, "querying %s\n", attr_to_name(mr->attr_next));
				if (rp_attr_get(priv_data, mr->attr_next, attr))
					return 1;
			}
			return 0;

		case attr_maxspeed:
			mr->attr_next = attr_label;
			if (mr->item.type != type_rg_segment)
			{
				return 0;
			}

			if (seg && (seg->data.flags & NAVIT_AF_SPEED_LIMIT))
			{
				attr->type = attr_maxspeed;
				attr->u.num = RSD_MAXSPEED(&seg->data);
				return 1;
			}
			else
			{
				return 0;
			}

		case attr_label:
			mr->attr_next = attr_street_item;
			if ((mr->item.type != type_rg_point) && (mr->item.type != type_rg_segment))
			{
				return 0;
			}

			if (mr->item.type == type_rg_point)
			{
				attr->type = attr_label;
				if (mr->str)
				{
					g_free(mr->str);
				}

				if (p->value != INT_MAX)
				{
					mr->str = g_strdup_printf("%d", p->value);
				}
				else
				{
					mr->str = g_strdup("-");
				}
				attr->u.str = mr->str;
			}
			else if (mr->item.type == type_rg_segment)
			{
				attr->type = attr_label;
				if (mr->str)
				{
					g_free(mr->str);
				}

				// z5z5
				if (seg)
				{
					mr->str = g_strdup_printf("%dl, %dt, %ds sv=%d ev=%d", seg->data.len, route_time_seg(route->vehicleprofile, &seg->data, NULL), route_seg_speed(route->vehicleprofile, &seg->data, NULL), seg->start->value, seg->end->value);
				}
				else
				{
					mr->str = g_strdup("??");
				}
				attr->u.str = mr->str;
			}

			return 1;

		case attr_street_item:
			mr->attr_next = attr_flags;
			if (mr->item.type != type_rg_segment)
				return 0;
			if (seg && seg->data.item.map)
				attr->u.item = &seg->data.item;
			else
				return 0;
			return 1;

		case attr_flags:
			mr->attr_next = attr_direction;
			if (mr->item.type != type_rg_segment)
				return 0;
			if (seg)
			{
				attr->u.num = seg->data.flags;
			}
			else
			{
				return 0;
			}
			return 1;

		case attr_direction:
			mr->attr_next = attr_debug;
			// This only works if the map has been opened at a single point, and in that case indicates if the
			// segment returned last is connected to this point via its start (1) or its end (-1)
			if (!mr->coord_sel || (mr->item.type != type_rg_segment))
				return 0;

			if (seg->start == mr->point)
			{
				attr->u.num = 1;
			}
			else if (seg->end == mr->point)
			{
				attr->u.num = -1;
			}
			else
			{
				return 0;
			}

			return 1;

		case attr_debug:
			mr->attr_next = attr_none;

			if (mr->str)
				g_free(mr->str);

			switch (mr->item.type)
			{
				case type_rg_point:
				{
					struct route_graph_segment *tmp;
					int start = 0;
					int end = 0;
					tmp = p->start;
					while (tmp)
					{
						start++;
						tmp = tmp->start_next;
					}
					tmp = p->end;
					while (tmp)
					{
						end++;
						tmp = tmp->end_next;
					}
					mr->str = g_strdup_printf("%d %d %p (0x%x,0x%x)", start, end, p, p->c.x, p->c.y);
					attr->u.str = mr->str;
				}
					return 1;
				case type_rg_segment:
					if (!seg)
						return 0;
					mr->str = g_strdup_printf("len %d time %d start %p end %p", seg->data.len, route_time_seg(route->vehicleprofile, &seg->data, NULL), seg->start, seg->end);
					attr->u.str = mr->str;
					return 1;
				default:
					return 0;
			}
		default:
			mr->attr_next = attr_none;
			attr->type = attr_none;
			return 0;
	}
}

/**
 * @brief Returns the coordinates of a route graph item
 *
 * @param priv_data The route graph item's private data
 * @param c Pointer where to store the coordinates
 * @param count How many coordinates to get at a max?
 * @return The number of coordinates retrieved
 */
static int rp_coord_get(void *priv_data, struct coord *c, int count)
{
	//// dbg(0, "enter\n");

	struct map_rect_priv *mr = priv_data;
	struct route_graph_point *p = mr->point;
	struct route_graph_segment *seg = mr->rseg;
	int rc = 0, i, dir;
	struct route *r = mr->mpriv->route;
	enum projection pro = route_projection(r);

	if (pro == projection_none)
	{
		return 0;
	}

	for (i = 0; i < count; i++)
	{
		if (mr->item.type == type_rg_point)
		{
			if (mr->last_coord >= 1)
				break;

			if (pro != projection_mg)
				transform_from_to(&p->c, pro, &c[i], projection_mg);
			else
				c[i] = p->c;
		}
		else
		{
			if (mr->last_coord >= 2)
				break;

			dir = 0;

			if (seg->end->seg == seg)
				dir = 1;

			if (mr->last_coord)
				dir = 1 - dir;

			if (dir)
			{
				if (pro != projection_mg)
					transform_from_to(&seg->end->c, pro, &c[i], projection_mg);
				else
					c[i] = seg->end->c;
			}
			else
			{
				if (pro != projection_mg)
					transform_from_to(&seg->start->c, pro, &c[i], projection_mg);
				else
					c[i] = seg->start->c;
			}
		}
		mr->last_coord++;
		rc++;
	}
	return rc;
}

static struct item_methods methods_point_item = { rm_coord_rewind, rp_coord_get, rp_attr_rewind, rp_attr_get, };

static void rp_destroy(struct map_priv *priv)
{
__F_START__

	g_free(priv);

__F_END__
}

static void rm_destroy(struct map_priv *priv)
{
__F_START__

	g_free(priv);

__F_END__
}

static struct map_rect_priv *
rm_rect_new(struct map_priv *priv, struct map_selection *sel)
{
	struct map_rect_priv * mr;
	//dbg(1, "enter\n");

#if 0
	if (! route_get_pos(priv->route))
	return NULL;
	if (! route_get_dst(priv->route))
	return NULL;
#endif

#if 0
	if (! priv->route->path2)
	return NULL;
#endif

	mr=g_new0(struct map_rect_priv, 1);
	mr->mpriv = priv;
	mr->item.priv_data = mr;
	mr->item.type = type_none;
	mr->item.meth = &methods_route_item;
	if (priv->route->path2)
	{
		mr->path = priv->route->path2;
		mr->seg_next = mr->path->path;
		mr->path->in_use++;
	}
	else
	{
		mr->seg_next = NULL;
	}

	return mr;
}

/**
 * @brief Opens a new map rectangle on the route graph's map
 *
 * This function opens a new map rectangle on the route graph's map.
 * The "sel" parameter enables you to only search for a single route graph
 * point on this map (or exactly: open a map rectangle that only contains
 * this one point). To do this, pass here a single map selection, whose 
 * c_rect has both coordinates set to the same point. Otherwise this parameter
 * has no effect.
 *
 * @param priv The route graph map's private data
 * @param sel Here it's possible to specify a point for which to search. Please read the function's description.
 * @return A new map rect's private data
 */
static struct map_rect_priv *
rp_rect_new(struct map_priv *priv, struct map_selection *sel)
{
	struct map_rect_priv * mr;

	if (!priv->route->graph)
	{
		return NULL;
	}

	mr=g_new0(struct map_rect_priv, 1);
	mr->mpriv = priv;
	mr->item.priv_data = mr;
	mr->item.type = type_rg_point;
	mr->item.meth = &methods_point_item;

	if (sel)
	{
		if ((sel->u.c_rect.lu.x == sel->u.c_rect.rl.x) && (sel->u.c_rect.lu.y == sel->u.c_rect.rl.y))
		{
			mr->coord_sel = g_malloc(sizeof(struct coord));
			// xxx
			*(mr->coord_sel) = sel->u.c_rect.lu;
		}
	}

	return mr;

}

static void rm_rect_destroy(struct map_rect_priv *mr)
{

	if (mr->str)
		g_free(mr->str);

	if (mr->coord_sel)
	{
		g_free(mr->coord_sel);
	}

	if (mr->path)
	{
		mr->path->in_use--;

		if (mr->path->update_required && (mr->path->in_use == 1))
			route_path_update_done(mr->mpriv->route, mr->path->update_required - 1);

		else if (!mr->path->in_use)
			g_free(mr->path);
	}

	g_free(mr);

}

static struct item *
rp_get_item(struct map_rect_priv *mr)
{
	//// dbg(0, "enter\n");

	struct route *r = mr->mpriv->route;
	struct route_graph_point *p = mr->point;
	struct route_graph_segment *seg = mr->rseg;

	if (mr->item.type == type_rg_point)
	{
		if (mr->coord_sel)
		{
			// We are supposed to return only the point at one specified coordinate...
			if (!p)
			{
				p = route_graph_get_point_last(r->graph, mr->coord_sel);
				if (!p)
				{
					mr->point = NULL; // This indicates that no point has been found
				}
				else
				{
					mr->it = rp_iterator_new(p);
				}
			}
			else
			{
				p = NULL;
			}
		}
		else
		{
			if (!p)
			{
				mr->hash_bucket = 0;
				p = r->graph->hash[0];
			}
			else
			{
				p = p->hash_next;
			}

			while (!p)
			{
				mr->hash_bucket++;
				if (mr->hash_bucket >= HASH_SIZE)
				{
					break;
				}
				p = r->graph->hash[mr->hash_bucket];
			}
		}

		if (p)
		{
			mr->point = p;
			mr->item.id_lo++;
			rm_coord_rewind(mr);
			rp_attr_rewind(mr);

			return &mr->item;
		}
		else
		{
			mr->item.type = type_rg_segment;
		}
	}

	if (mr->coord_sel)
	{
		if (!mr->point)
		{ // This means that no point has been found
			return NULL;
		}
		seg = rp_iterator_next(&(mr->it));
	}
	else
	{
		if (!seg)
		{
			seg = r->graph->route_segments;
		}
		else
		{
			seg = seg->next;
		}
	}

	if (seg)
	{
		mr->rseg = seg;
		mr->item.id_lo++;
		rm_coord_rewind(mr);
		rp_attr_rewind(mr);

		return &mr->item;
	}
	return NULL;

__F_END__
}

static struct item *
rp_get_item_byid(struct map_rect_priv *mr, int id_hi, int id_lo)
{
	struct item *ret = NULL;

	while (id_lo-- > 0)
		ret = rp_get_item(mr);

	return ret;
}

static struct item *
rm_get_item(struct map_rect_priv *mr)
{
	struct route *route = mr->mpriv->route;

	int is_waypoint = 0;

#if 1
	dbg(0, "NAVR:ROUTE:001.r0:%s ===================================\n", item_to_name(mr->item.type));
	if (mr->seg_next)
	{
		dbg(0, "NAVR:ROUTE:001.r1:%s ===================================\n", item_to_name(mr->seg_next->data->item.type));
	}
	if ((mr->seg)&&(mr->seg->next))
	{
		dbg(0, "NAVR:ROUTE:001.r2:%s ===================================\n", item_to_name(mr->seg->next->data->item.type));
	}
	if (mr->seg)
	// mr->seg->next
	// if (mr->seg_next)
	{
		if (mr->seg->data)
		// if (mr->seg_next->data)
		{
			dbg(0, "NAVR:ROUTE:001:%s ===================================\n", item_to_name(mr->seg->data->item.type));
			// dbg(0, "NAVR:ROUTE:001:%s ===================================\n", item_to_name(mr->seg_next->data->item.type));
			if (mr->seg->data->item.type == type_street_route_waypoint)
			// if (mr->seg_next->data->item.type == type_street_route_waypoint)
			{
				item_dump_coords(&mr->item, route_get_map(route));
				is_waypoint = 1;
			}
		}
	}
	// item_dump_coords(&mr->item, route_get_map(route));

#endif

	switch (mr->item.type)
	{
		case type_none:

			if (route->pos)
			{
				dbg(0, "NAVR:ROUTE:001ppp:route->pos->street_direction=%d route->pos->dir=%d\n", route->pos->street_direction, route->pos->dir);
			}

			if (route->pos && route->pos->street_direction && route->pos->street_direction != route->pos->dir)
			{
				mr->item.type = type_route_start_reverse;
			}
			else
			{
				mr->item.type = type_route_start;
			}

			if (route->pos)
			{
				break;
			}

		default:
			mr->item.type = type_street_route;
			mr->seg = mr->seg_next;

#if 0
			if (mr->seg)
			{
				if (mr->seg->data)
				{
					dbg(0, "NAVR:ROUTE:001:%s ===================================\n", item_to_name(mr->seg->data->item.type));
					if (mr->seg->data->item.type == type_street_route_waypoint)
					{
						is_waypoint = 1;
					}
				}
			}
#endif

			if (!mr->seg && mr->path && mr->path->next)
			{
				// dbg(0, "NAVR:ROUTE:002:WAYPOINT/DESTINATION---------------------------------------------------------");

				struct route_path *p = NULL;
				mr->path->in_use--;

				if (!mr->path->in_use)
				{
					p = mr->path;
					// dbg(0, "NAVR:ROUTE:003:+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				}

				mr->path = mr->path->next;
				mr->path->in_use++;
				mr->seg = mr->path->path;

				if (p)
				{
					g_free(p);
				}

				// set waypoint type
				// ?? seems to not be the correct spot for waypoint ?? // mr->item.type = type_street_route_waypoint;
			}

			if (mr->seg)
			{
				mr->seg_next = mr->seg->next;
				break;
			}

			mr->item.type = type_route_end;
			// dbg(0,"* set route_end *\n");

			if (mr->mpriv->route->destinations)
			{
				break;
			}

		case type_route_end:
			return NULL;
	}

	mr->last_coord = 0;
	mr->item.id_lo++;
	rm_attr_rewind(mr);


	if (is_waypoint == 1)
	{
		mr->item.type = type_street_route_waypoint;
	}

	return &mr->item;
}

static struct item *
rm_get_item_byid(struct map_rect_priv *mr, int id_hi, int id_lo)
{
	//// dbg(0, "enter\n");

	struct item *ret = NULL;

	while (id_lo-- > 0)
	{
		ret = rm_get_item(mr);
	}

	return ret;
}

static struct map_methods route_meth = { projection_mg, "utf-8", rm_destroy, rm_rect_new, rm_rect_destroy, rm_get_item, rm_get_item_byid, NULL, NULL, NULL, };

static struct map_methods route_graph_meth = { projection_mg, "utf-8", rp_destroy, rp_rect_new, rm_rect_destroy, rp_get_item, rp_get_item_byid, NULL, NULL, NULL, };

static struct map_priv *
route_map_new_helper(struct map_methods *meth, struct attr **attrs, int graph)
{
__F_START__

	struct map_priv *ret;
	struct attr *route_attr;

	route_attr = attr_search(attrs, NULL, attr_route);

	if (!route_attr)
	{
		return2 NULL;
	}

	ret=g_new0(struct map_priv, 1);

	if (graph)
		*meth = route_graph_meth;
	else
		*meth = route_meth;

	ret->route = route_attr->u.route;

	return2 ret;

__F_END__
}

struct map_priv *
route_map_new(struct map_methods *meth, struct attr **attrs, struct callback_list *cbl)
{
__F_START__

	return2 route_map_new_helper(meth, attrs, 0);

__F_END__
}

struct map_priv *
route_graph_map_new(struct map_methods *meth, struct attr **attrs, struct callback_list *cbl)
{
__F_START__

	return2 route_map_new_helper(meth, attrs, 1);

__F_END__
}

static struct map *
route_get_map_helper(struct route *this_, struct map **map, char *type, char *description)
{
	struct attr *attrs[5];
	struct attr a_type, navigation, data, a_description;
	a_type.type = attr_type;
	a_type.u.str = type;
	navigation.type = attr_route;
	navigation.u.route = this_;
	data.type = attr_data;
	data.u.str = "";
	a_description.type = attr_description;
	a_description.u.str = description;

	attrs[0] = &a_type;
	attrs[1] = &navigation;
	attrs[2] = &data;
	attrs[3] = &a_description;
	attrs[4] = NULL;

	if (!*map)
	{
		*map = map_new(NULL, attrs);
		map_ref(*map);
	}

	return *map;
}

/**
 * @brief Returns a new map containing the route path
 *
 * This function returns a new map containing the route path.
 *
 * @important Do not map_destroy() this!
 *
 * @param this_ The route to get the map of
 * @return A new map containing the route path
 */
struct map *
route_get_map(struct route *this_)
{
	return route_get_map_helper(this_, &this_->map, "route", "Route");
}

/**
 * @brief Returns a new map containing the route graph
 *
 * This function returns a new map containing the route graph.
 *
 * @important Do not map_destroy()  this!
 *
 * @param this_ The route to get the map of
 * @return A new map containing the route graph
 */
struct map *
route_get_graph_map(struct route *this_)
{
	return route_get_map_helper(this_, &this_->graph_map, "route_graph", "Route Graph");
}

void route_set_projection(struct route *this_, enum projection pro)
{
}

int route_set_attr(struct route *this_, struct attr *attr)
{
__F_START__

	int attr_updated = 0;
	switch (attr->type)
	{
		case attr_route_status:
			// update global route_status notifier

#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "RS:002P:route_status=%s\n", route_status_to_name(route_status_previous));
			dbg(0, "RS:002C:route_status=%s\n", route_status_to_name(this_->route_status));
			dbg(0, "RS:002N:route_status=%s\n", route_status_to_name(attr->u.num));
#endif
			//dbg(0,"previous=%d\n", route_status_previous);
			//dbg(0,"this_->route_status=%d attr->u.num=%d\n", this_->route_status, attr->u.num);

			if (this_->route_status != attr->u.num)
			{
				if (attr->u.num == 5)
				{
				}
				else
				{
					if (route_status_previous != attr->u.num)
					{
						//dbg(0,"update\n");
						this_->route_status_was_updated = 1;
					}
					route_status_previous = attr->u.num;
				}
			}

			attr_updated = (this_->route_status != attr->u.num);
			this_->route_status = attr->u.num;
			break;

		case attr_destination:
			route_set_destination(this_, attr->u.pcoord, 1);
			return2 1;

		case attr_vehicle:
			attr_updated = (this_->v != attr->u.vehicle);
			this_->v = attr->u.vehicle;
			if (attr_updated)
			{
				struct attr g;
				struct pcoord pc;
				struct coord c;
				if (vehicle_get_attr(this_->v, attr_position_coord_geo, &g, NULL))
				{
					pc.pro = projection_mg;
					transform_from_geo(projection_mg, g.u.coord_geo, &c);
					pc.x = c.x;
					pc.y = c.y;
#ifdef NAVIT_ROUTING_DEBUG_PRINT
					dbg(0, "ROUTExxPOSxx:route_set_attr:YYYYYYYY: %d %d\n", c.x, c.y);
#endif
					route_set_position(this_, &pc);
				}
			}
			break;

		default:
			// dbg(0, "unsupported attribute: %s\n", attr_to_name(attr->type));
			return2 0;
	}

	if (attr_updated)
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:route_set_attr:attr->type=%s\n", attr_to_name(attr->type));
#endif
		if (attr->type == attr_route_status)
		{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "RS:004:route_status=%s\n", route_status_to_name(attr->u.num));
#endif

			dbg(0, "NAVR:ROUTE:007r:route_status=%s\n", route_status_to_name(attr->u.num));

			// calls: "navit_redraw_route" and "navigation_update"
			callback_list_call_attr_2(this_->cbl2, attr->type, this_, attr);
		}
	}

	return2 1;

__F_END__
}

int route_add_attr(struct route *this_, struct attr *attr)
{
__F_START__

	switch (attr->type)
	{
		case attr_callback:
			callback_list_add(this_->cbl2, attr->u.callback);
			return2 1;
		default:
			return2 0;
	}

__F_END__
}

int route_remove_attr(struct route *this_, struct attr *attr)
{
__F_START__

	switch (attr->type)
	{
		case attr_callback:
			callback_list_remove(this_->cbl2, attr->u.callback);
			return2 1;
		case attr_vehicle:
			this_->v = NULL;
			return2 1;
		default:
			return2 0;
	}

__F_END__
}

int route_get_attr(struct route *this_, enum attr_type type, struct attr *attr, struct attr_iter *iter)
{
__F_START__

	int ret = 1;
	switch (type)
	{
		case attr_map:
			attr->u.map = route_get_map(this_);
			ret = (attr->u.map != NULL);
			break;
		case attr_destination:
			if (this_->destinations)
			{
				struct route_info *dst;
				if (iter)
				{
					if (iter->u.list)
					{
						iter->u.list = g_list_next(iter->u.list);
					}
					else
					{
						iter->u.list = this_->destinations;
					}
					if (!iter->u.list)
					{
						return2 0;
					}
					dst = (struct route_info*) iter->u.list->data;
				}
				else
				{ //No iter handling
					dst = route_get_dst(this_);
				}
				attr->u.pcoord = &this_->pc;
				this_->pc.pro = projection_mg; /* fixme */
				this_->pc.x = dst->c.x;
				this_->pc.y = dst->c.y;
			}
			else
				ret = 0;
			break;
		case attr_vehicle:
			attr->u.vehicle = this_->v;
			ret = (this_->v != NULL);
			//dbg(0,"get vehicle %p\n",this_->v);
			break;
		case attr_vehicleprofile:
			attr->u.vehicleprofile = this_->vehicleprofile;
			ret = (this_->vehicleprofile != NULL);
			break;
		case attr_route_status:
			attr->u.num = this_->route_status;
			break;
		case attr_destination_time:
			if (this_->path2 && (this_->route_status == route_status_path_done_new || this_->route_status == route_status_path_done_incremental))
			{

				attr->u.num = this_->path2->path_time;
				//dbg(1, "path_time %d\n", attr->u.num);
			}
			else
				ret = 0;

			break;
		case attr_destination_length:
			if (this_->path2 && (this_->route_status == route_status_path_done_new || this_->route_status == route_status_path_done_incremental))
				attr->u.num = this_->path2->path_len;
			else
				ret = 0;

			break;
		default:
			return2 0;
	}
	attr->type = type;

	return2 ret;

__F_END__
}

struct attr_iter *
route_attr_iter_new(void)
{
	//// dbg(0, "enter\n");

	return g_new0(struct attr_iter, 1);
}

void route_attr_iter_destroy(struct attr_iter *iter)
{
	//// dbg(0, "enter\n");

	g_free(iter);
}

void route_init(void)
{
__F_START__

#ifdef PLUGSSS
	plugin_register_map_type("route", route_map_new);
	plugin_register_map_type("route_graph", route_graph_map_new);
#endif

__F_END__
}

void route_destroy(struct route *this_)
{
__F_START__

	route_path_destroy(this_->path2, 1);
	route_graph_destroy(this_->graph);
	route_clear_destinations(this_);
	route_info_free(this_->pos);
	map_destroy(this_->map);
	map_destroy(this_->graph_map);
	g_free(this_);

__F_END__
}

