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

#include <glib.h>
#include <string.h>
#include "config.h"
#include "debug.h"
#include "coord.h"
#include "item.h"
#include "navit.h"
#include "map.h"
#include "route.h"
#include "callback.h"
#include "transform.h"
#include "plugin.h"
#include "vehicle.h"
#include "event.h"
#include "util.h"

struct vehicle_priv
{
	int interval;
	int position_set;
	struct callback_list *cbl;
	struct navit *navit;
	struct coord_geo geo;
	struct coord last;
	double config_speed;
	double speed;
	double direction;
	struct callback *timer_callback;
	struct event_timeout *timer;
	char *timep;
	double speed_diff;
	int speed_dir;
};

static void vehicle_demo_destroy(struct vehicle_priv *priv)
{
	g_free(priv->timep);
	g_free(priv);
}

static int vehicle_demo_position_attr_get(struct vehicle_priv *priv, enum attr_type type, struct attr *attr)
{
	switch (type)
	{
		case attr_position_speed:
			attr->u.numd = &priv->speed;
			break;
		case attr_position_direction:
			attr->u.numd = &priv->direction;
			break;
		case attr_position_coord_geo:
			attr->u.coord_geo = &priv->geo;
			break;
		case attr_position_time_iso8601:
			g_free(priv->timep);
			priv->timep = current_to_iso8601();
			attr->u.str = priv->timep;
			break;
		default:
			return 0;
	}
	attr->type = type;
	return 1;
}

static int vehicle_demo_set_attr(struct vehicle_priv *priv, struct attr *attr)
{
	if (attr->type == attr_navit)
	{
		priv->navit = attr->u.navit;
	}
	return 1;
}

struct vehicle_methods vehicle_demo_methods =
{ vehicle_demo_destroy, vehicle_demo_position_attr_get, vehicle_demo_set_attr, NULL, };

static void vehicle_demo_timer(struct vehicle_priv *priv)
{
	struct coord c, c2, pos, ci;
	int slen, len, dx, dy;
	struct route *route = NULL;
	struct map *route_map = NULL;
	struct map_rect *mr = NULL;
	struct item *item = NULL;

	//dbg(0,"stop demo vehicle=%d\n", global_stop_demo_vehicle);
	if (global_stop_demo_vehicle == 1)
	{
		// demo vehicle should stand still!
		return;
	}

	// vary the speed of demo vehicle from (x - 20) to (x + 20)
	if (priv->speed_dir == 1)
	{
		priv->speed_diff = priv->speed_diff + 2;
		if (priv->speed_diff > 20)
		{
			priv->speed_diff = 20;
			priv->speed_dir = -1;
		}
	}
	else
	{
		priv->speed_diff = priv->speed_diff - 2;
		if (priv->speed_diff < -20)
		{
			priv->speed_diff = -20;
			priv->speed_dir = 1;
		}
	}
	priv->config_speed = priv->config_speed + priv->speed_dir + priv->speed_dir;
	//dbg(0,"demo:speed=%d,speed_diff=%d speed_dir=%d\n", (int)priv->config_speed, (int)priv->speed_diff, (int)priv->speed_dir);

	len = (priv->config_speed * priv->interval / 1000) / 3.6;
	//dbg(0, "###### Entering simulation loop\n");
	if (priv->navit)
	{
		route = navit_get_route(priv->navit);
	}
	//DBG dbg(0,"rr 1\n");
	if (route)
	{
		route_map = route_get_map(route);
	}
	//DBG dbg(0,"rr 2\n");
	if (route_map)
	{
		mr = map_rect_new(route_map, NULL);
	}
	//DBG dbg(0,"rr 3\n");
	if (mr)
	{
		item = map_rect_get_item(mr);
	}
	//DBG dbg(0,"rr 4\n");
	if (item && item->type == type_route_start)
	{
		item = map_rect_get_item(mr);
	}
	//dbg(0,"rr 5\n");
	if (item && item_coord_get(item, &pos, 1))
	{
		priv->position_set = 0;
		//dbg(0, "current pos=0x%x,0x%x\n", pos.x, pos.y);
		////DBG dbg(0, "last pos=0x%x,0x%x\n", priv->last.x, priv->last.y);
		//if (priv->last.x == pos.x && priv->last.y == pos.y)
		//{
		//	//dbg(1, "endless loop\n");
		//}

		priv->last = pos;
		while (item && priv->config_speed)
		{
			if (!item_coord_get(item, &c, 1))
			{
				item = map_rect_get_item(mr);
				continue;
			}
			//dbg(0, "next pos=0x%x,0x%x\n", c.x, c.y);
			slen = transform_distance(projection_mg, &pos, &c);
			////DBG dbg(0, "len=%d slen=%d\n", len, slen);
			if (slen < len)
			{
				len -= slen;
				pos = c;
			}
			else
			{
				if (item_coord_get(item, &c2, 1) || map_rect_get_item(mr))
				{
					dx = c.x - pos.x;
					dy = c.y - pos.y;
					ci.x = pos.x + dx * len / slen;
					ci.y = pos.y + dy * len / slen;
					priv->direction = transform_get_angle_delta(&pos, &c, 0);
					priv->speed = priv->config_speed;
				}
				else
				{
					ci.x = pos.x;
					ci.y = pos.y;
					priv->speed = 0;
					////DBG dbg(0, "destination reached\n");
				}
				//dbg(1, "ci=0x%x,0x%x\n", ci.x, ci.y);
				transform_to_geo(projection_mg, &ci, &priv->geo);
				callback_list_call_attr_0(priv->cbl, attr_position_coord_geo);
				break;
			}
		}
	}
	else
	{
		if (priv->position_set)
		{
			callback_list_call_attr_0(priv->cbl, attr_position_coord_geo);
		}
	}
	//DBG dbg(0,"rr 6\n");
	if (mr)
	{
		map_rect_destroy(mr);
	}
	// dbg(0,"rr F\n");
}

static struct vehicle_priv *
vehicle_demo_new(struct vehicle_methods *meth, struct callback_list *cbl, struct attr **attrs)
{
	struct vehicle_priv *ret;
	struct attr *interval, *speed, *position_coord_geo;

	//DBG dbg(0, "enter\n");
	ret = g_new0(struct vehicle_priv, 1);
	ret->cbl = cbl;
	ret->interval = 1200;
	ret->config_speed = 41;
	ret->speed_diff = 0;
	ret->speed_dir = 1;

	//dbg(0, "vd 3.1 %d %d\n", ret->interval, ret->config_speed);

	//DBG dbg(0, "vd 1\n");
	if ((speed = attr_search(attrs, NULL, attr_speed)))
	{
		ret->config_speed = speed->u.num;
	}

	//DBG dbg(0, "vd 2\n");
	if ((interval = attr_search(attrs, NULL, attr_interval)))
	{
		ret->interval = interval->u.num;
	}

	// ret->geo.lat = 0;
	// ret->geo.lng = 0;
	// //DBG dbg(0, "position_default %f %f\n", ret->geo.lat, ret->geo.lng);

	//dbg(0, "vd 3.2 %d %d\n", ret->interval, ret->config_speed);
	if ((position_coord_geo = attr_search(attrs, NULL, attr_position_coord_geo)))
	{
		ret->geo = *(position_coord_geo->u.coord_geo);
		ret->position_set = 1;
		//DBG dbg(0, "position_set %f %f\n", ret->geo.lat, ret->geo.lng);
	}

	//DBG dbg(0, "vd 4\n");
	*meth = vehicle_demo_methods;
	ret->timer_callback = callback_new_1(callback_cast(vehicle_demo_timer), ret);
	callback_add_names(ret->timer_callback, "vehicle_demo_new", "vehicle_demo_timer");
	//DBG dbg(0, "vd 5\n");
	dbg(0, "event_add_timeout %d,%d,%p", ret->interval, 1, ret->timer_callback);
	ret->timer = event_add_timeout(ret->interval, 1, ret->timer_callback);
	//DBG dbg(0, "leave\n");
	return ret;
}

void plugin_init(void)
{
	//DBG dbg(0, "enter\n");
	plugin_register_vehicle_type("demo", vehicle_demo_new);
}
