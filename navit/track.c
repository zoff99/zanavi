/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2014 Zoff <zoff@zoff.cc>
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
#include <time.h>
#include <sys/time.h>
#include <math.h>

#include "navit.h"

#include "item.h"
#include "attr.h"
#include "track.h"
#include "debug.h"
#include "transform.h"
#include "coord.h"
#include "route.h"
#include "projection.h"
#include "map.h"
#include "mapset.h"
#include "plugin.h"
#include "vehicleprofile.h"
#include "vehicle.h"
#include "roadprofile.h"
#include "util.h"
#include "config.h"


// #define NAVIT_FUNC_CALLS_DEBUG_PRINT 1

#define TRACKING_MAX_DIST_RELOAD_MAP_DATA_LIMIT 1500
#define TRACKING_MAX_DIST_RELOAD_MAP_DATA_THRESHOLD 750

#define FIVE_KHM	18
#define TEN_KHM 	36
#define CORRECT_FOR_ON_ROUTE_SEG_VALUE 1100

struct tracking_line
{
	struct street_data *street;
	struct tracking_line *next;
	int angle[0];
};

/**
 * @brief Conatins a list of previous speeds
 *
 * This structure is used to hold a list of previously reported
 * speeds. This data is used by the CDF.
 */
struct cdf_speed
{
	struct cdf_speed *next;
	int speed;
	time_t time;
};

/**
 * @brief Contains data for the CDF
 *
 * This structure holds all data needed by the
 * cumulative displacement filter.
 */
struct cdf_data
{
	int extrapolating;
	int available;
	int first_pos;
	int poscount;
	int hist_size;
	struct cdf_speed *speed_hist;
	struct pcoord *pos_hist;
	int *dir_hist;
	double last_dist;
	struct pcoord last_out;
	int last_dir;
};

struct tacking_item_mark
{
	int id_hi;
	int id_lo;
	struct map *map;
};

struct tracking
{
	struct mapset *ms;
	struct route *rt;
	struct map *map;
	struct vehicle *vehicle;
	struct vehicleprofile *vehicleprofile;
	struct coord last_updated;
	struct tracking_line *lines;
	struct tracking_line *curr_line;
	int pos;
	struct coord curr[2], curr_in, curr_out;
	int curr_angle;
	struct coord last[2], last_in, last_out;
	struct cdf_data cdf;
	struct attr *attr;
	int valid;
	int time;
	double direction;
	double speed;
	int coord_geo_valid;
	struct coord_geo coord_geo;
	enum projection pro;
	int street_direction;
	int no_gps;
	int tunnel;
	int angle_pref;
	int connected_pref;
	int nostop_pref;
	int offroad_limit_pref;
	int route_pref;
	int overspeed_pref;
	int overspeed_percent_pref;
	int tunnel_extrapolation;
	int curr_max_speed;
	struct tacking_item_mark curr_item_mark, last_item_mark;
};


int next_lanes_id_hi__prev = 0;
int next_lanes_id_lo__prev = 0;
struct map* next_lanes_id_map__prev = NULL;
int street_dir_next__prev = 1;
struct route_graph_segment *s__prev = NULL;


static void tracking_init_cdf(struct cdf_data *cdf, int hist_size)
{
	cdf->extrapolating = 0;
	cdf->available = 0;
	cdf->poscount = 0;
	cdf->last_dist = 0;
	cdf->hist_size = hist_size;

	cdf->pos_hist = g_new0(struct pcoord, hist_size);
	cdf->dir_hist = g_new0(int, hist_size);
}

// Variables for finetuning the CDF

// Minimum average speed
#define CDF_MINAVG 1.f
// Maximum average speed
#define CDF_MAXAVG 6.f // only ~ 20 km/h 
// We need a low value here because otherwise we would extrapolate whenever we are not accelerating

// Mininum distance (square of it..), below which we ignore gps updates
#define CDF_MINDIST 49 // 7 meters, I guess this value has to be changed for pedestrians.
#if 0
static void
tracking_process_cdf(struct cdf_data *cdf, struct pcoord *pin, struct pcoord *pout, int dirin, int *dirout, int cur_speed, time_t fixtime)
{
struct cdf_speed *speed,*sc,*sl;
double speed_avg;
int speed_num,i;

if (cdf->hist_size == 0)
{
	dbg(1,"No CDF.\n");
	*pout = *pin;
	*dirout = dirin;
	return;
}

speed = g_new0(struct cdf_speed, 1);
speed->speed = cur_speed;
speed->time = fixtime;

speed->next = cdf->speed_hist;
cdf->speed_hist = speed;

sc = speed;
sl = NULL;
speed_num = 0;
speed_avg = 0;
while (sc && ((fixtime - speed->time) < 4))
{ // FIXME static maxtime
	speed_num++;
	speed_avg += sc->speed;
	sl = sc;
	sc = sc->next;
}

speed_avg /= (double)speed_num;

if (sl)
{
	sl->next = NULL;
}

while (sc)
{
	sl = sc->next;
	g_free(sc);
	sc = sl;
}

if (speed_avg < CDF_MINAVG)
{
	speed_avg = CDF_MINAVG;
}
else if (speed_avg > CDF_MAXAVG)
{
	speed_avg = CDF_MAXAVG;
}

if (cur_speed >= speed_avg)
{
	if (cdf->extrapolating)
	{
		cdf->poscount = 0;
		cdf->extrapolating = 0;
	}

	cdf->first_pos--;
	if (cdf->first_pos < 0)
	{
		cdf->first_pos = cdf->hist_size - 1;
	}

	if (cdf->poscount < cdf->hist_size)
	{
		cdf->poscount++;
	}

	cdf->pos_hist[cdf->first_pos] = *pin;
	cdf->dir_hist[cdf->first_pos] = dirin;

	*pout = *pin;
	*dirout = dirin;
}
else if (cdf->poscount > 0)
{

	double mx,my; // Average position's x and y values
	double sx,sy; // Support vector
	double dx,dy; // Difference between average and current position
	double len; // Length of support vector
	double dist;

	mx = my = 0;
	sx = sy = 0;

	for (i = 0; i < cdf->poscount; i++)
	{
		mx += (double)cdf->pos_hist[((cdf->first_pos + i) % cdf->hist_size)].x / cdf->poscount;
		my += (double)cdf->pos_hist[((cdf->first_pos + i) % cdf->hist_size)].y / cdf->poscount;

		if (i != 0)
		{
			sx += cdf->pos_hist[((cdf->first_pos + i) % cdf->hist_size)].x - cdf->pos_hist[((cdf->first_pos + i - 1) % cdf->hist_size)].x;
			sy += cdf->pos_hist[((cdf->first_pos + i) % cdf->hist_size)].y - cdf->pos_hist[((cdf->first_pos + i - 1) % cdf->hist_size)].y;
		}

	}

	if (cdf->poscount > 1)
	{
		// Normalize the support vector
		len = sqrt(sx * sx + sy * sy);
		sx /= len;
		sy /= len;

		// Calculate the new direction
		*dirout = (int)rint(atan(sx / sy) / M_PI * 180 + 180);
	}
	else
	{
		// If we only have one position, we can't use differences of positions, but we have to use the reported
		// direction of that position
		sx = sin((double)cdf->dir_hist[cdf->first_pos] / 180 * M_PI);
		sy = cos((double)cdf->dir_hist[cdf->first_pos] / 180 * M_PI);
		*dirout = cdf->dir_hist[cdf->first_pos];
	}

	dx = pin->x - mx;
	dy = pin->y - my;
	dist = dx * sx + dy * sy;

	if (cdf->extrapolating && (dist < cdf->last_dist))
	{
		dist = cdf->last_dist;
	}

	cdf->last_dist = dist;
	cdf->extrapolating = 1;

	pout->x = (int)rint(mx + sx * dist);
	pout->y = (int)rint(my + sy * dist);
	pout->pro = pin->pro;

}
else
{
	// We should extrapolate, but don't have an old position available
	*pout = *pin;
	*dirout = dirin;
}

if (cdf->available)
{
	int dx,dy;

	dx = pout->x - cdf->last_out.x;
	dy = pout->y - cdf->last_out.y;

	if ((dx*dx + dy*dy) < CDF_MINDIST)
	{
		*pout = cdf->last_out;
		*dirout = cdf->last_dir;
	}
}

cdf->last_out = *pout;
cdf->last_dir = *dirout;

cdf->available = 1;
}
#endif

int tracking_get_angle(struct tracking *tr)
{
	return tr->curr_angle;
}

struct coord *
tracking_get_pos(struct tracking *tr)
{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:tracking_get_pos: enter\n");
#endif
	return &tr->curr_out;
}

int tracking_get_street_direction(struct tracking *tr)
{
	return tr->street_direction;
}

double tracking_get_direction(struct tracking *tr)
{
	return tr->direction;
}

int tracking_get_segment_pos(struct tracking *tr)
{
	return tr->pos;
}

struct street_data *
tracking_get_street_data(struct tracking *tr)
{
	if (tr->curr_line)
		return tr->curr_line->street;

	return NULL;
}

int tracking_get_attr(struct tracking *_this, enum attr_type type, struct attr *attr, struct attr_iter *attr_iter)
{
	struct item *item;
	struct map_rect *mr;
	int result = 0;

	if (_this->attr)
	{
		attr_free(_this->attr);
		_this->attr = NULL;
	}

	switch (type)
	{
		case attr_position_valid:
			attr->u.num = _this->valid;
			return 1;
		case attr_position_direction:
			attr->u.numd = &_this->direction;
			return 1;
		case attr_position_speed:
			attr->u.numd = &_this->speed;
			return 1;
		case attr_directed:
			attr->u.num = _this->street_direction;
			return 1;
		case attr_position_coord_geo:
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "ROUTExxPOSxx:attr_position_coord_geo\n");
#endif
			if (!_this->coord_geo_valid)
			{
				struct coord c;
				c.x = _this->curr_out.x;
				c.y = _this->curr_out.y;
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				dbg(0, "ROUTExxPOSxx:attr_position_coord_geo:(coord_geo_valid == 0): %d %d\n", c.x, c.y);
#endif
				transform_to_geo(_this->pro, &c, &_this->coord_geo);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				dbg(0, "ROUTExxPOSxx:attr_position_coord_geo:(coord_geo_valid == 0)1:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:red|label:AA|%4.6f,%4.6f\n", _this->coord_geo.lat, _this->coord_geo.lng);
				dbg(0, "ROUTExxPOSxx:attr_position_coord_geo:(coord_geo_valid == 0)1: %4.6f,%4.6f\n", _this->coord_geo.lat, _this->coord_geo.lng);
#endif
				_this->coord_geo_valid = 1;
			}
			attr->u.coord_geo = &_this->coord_geo;
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			// ----- DEBUG ------
			// ----- DEBUG ------
			// ----- DEBUG ------
			dbg(0, "ROUTExxPOSxx:attr_position_coord_geo:3: %4.6f,%4.6f\n", _this->coord_geo.lat, _this->coord_geo.lng);
			dbg(0, "ROUTExxPOSxx:attr_position_coord_geo:3.1a:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:red|label:AA|%4.6f,%4.6f\n", _this->coord_geo.lat, _this->coord_geo.lng);
			struct coord c88;
			transform_from_geo(_this->pro, &_this->coord_geo, &c88);
			dbg(0, "ROUTExxPOSxx:attr_position_coord_geo:2: %d %d\n", c88.x, c88.y);
			struct coord_geo gg54;
			transform_to_geo(_this->pro, &c88, &gg54);
			dbg(0, "ROUTExxPOSxx:attr_position_coord_geo:3.1b:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:green|label:A2|%4.6f,%4.6f&markers=color:red|label:AA|%4.6f,%4.6f\n", gg54.lat, gg54.lng, _this->coord_geo.lat, _this->coord_geo.lng);
			// ----- DEBUG ------
			// ----- DEBUG ------
			// ----- DEBUG ------
#endif
			return 1;
		case attr_current_item:
			if (!_this->curr_line || !_this->curr_line->street)
			{
				return 0;
			}
			attr->u.item = &_this->curr_line->street->item;
			return 1;
		default:
			if (!_this->curr_line || !_this->curr_line->street)
			{
				return 0;
			}

			dbg(0,"ATTR %s\n", attr_to_name(type));

			item = &_this->curr_line->street->item;
			mr = map_rect_new(item->map, NULL);
			// COST: 101
			dbg(0, "COST:101\n");
			item = map_rect_get_item_byid(mr, item->id_hi, item->id_lo);

			if (item_attr_get(item, type, attr))
			{
				_this->attr = attr_dup(attr);
				*attr = *_this->attr;
				result = 1;
			}
			map_rect_destroy(mr);
			return result;
	}
}

struct item *
tracking_get_current_item(struct tracking *_this)
{
	if (!_this->curr_line || !_this->curr_line->street)
		return NULL;

	return &_this->curr_line->street->item;
}

int *
tracking_get_current_flags(struct tracking *_this)
{
	if (!_this->curr_line || !_this->curr_line->street)
		return NULL;

	return &_this->curr_line->street->flags;
}

static void tracking_get_angles(struct tracking_line *tl)
{
	int i;
	struct street_data *sd = tl->street;

	// COST: 103
	// dbg(0, "COST:103\n");

	for (i = 0; i < sd->count - 1; i++)
	{
		tl->angle[i] = transform_get_angle_delta(&sd->c[i], &sd->c[i + 1], 0);
	}
}

static int street_data_within_selection(struct street_data *sd, struct map_selection *sel)
{
	struct coord_rect r;
	struct map_selection *curr;
	int i;

	if (!sel)
		return 1;

	r.lu = sd->c[0];
	r.rl = sd->c[0];

	for (i = 1; i < sd->count; i++)
	{
		if (r.lu.x > sd->c[i].x)
			r.lu.x = sd->c[i].x;
		if (r.rl.x < sd->c[i].x)
			r.rl.x = sd->c[i].x;
		if (r.rl.y > sd->c[i].y)
			r.rl.y = sd->c[i].y;
		if (r.lu.y < sd->c[i].y)
			r.lu.y = sd->c[i].y;
	}
	curr = sel;

	while (curr)
	{
		struct coord_rect *sr = &curr->u.c_rect;
		if (r.lu.x <= sr->rl.x && r.rl.x >= sr->lu.x && r.lu.y >= sr->rl.y && r.rl.y <= sr->lu.y)
		{
			return 1;
		}

		curr = curr->next;
	}

	return 0;
}

static void tracking_doupdate_lines(struct tracking *tr, struct coord *pc, enum projection pro, int max_dist)
{
	struct map_selection *sel;
	struct mapset_handle *h;
	struct map *m;
	struct map_rect *mr;
	struct item *item;
	struct street_data *street;
	struct tracking_line *tl;
	struct coord_geo g;
	struct coord cc;

	//dbg(1,"enter\n");
	h = mapset_open(tr->ms);
	while ((m = mapset_next(h, 2)))
	{
#if 0
		struct attr map_name_attr;
		if (map_get_attr(m, attr_name, &map_name_attr, NULL))
		{
			if (strncmp("_ms_sdcard_map:-special-:worldmap", map_name_attr.u.str, 34) == 0)
			{
				dbg(0, "dont use special map %s for tracking\n", map_name_attr.u.str);
				continue;
			}
		}
#endif

		cc.x = pc->x;
		cc.y = pc->y;

		if (map_projection(m) != pro)
		{
			transform_to_geo(pro, &cc, &g);
			transform_from_geo(map_projection(m), &g, &cc);
		}

		sel = route_rect(18, &cc, &cc, 0, max_dist);
		mr = map_rect_new(m, sel);

		if (!mr)
		{
			continue;
		}

		while ((item = map_rect_get_item(mr)))
		{
			if (item_get_default_flags(item->type))
			{
				street = street_get_data(item);
				if (street_data_within_selection(street, sel))
				{
					tl = g_malloc(sizeof(struct tracking_line) + (street->count - 1) * sizeof(int));
					tl->street = street;
					tracking_get_angles(tl);
					tl->next = tr->lines;
					tr->lines = tl;
				}
				else
				{
					street_data_free(street);
				}
			}
		}

		map_selection_destroy(sel);
		map_rect_destroy(mr);

	}

	mapset_close(h);
	//dbg(1, "exit\n");
}

void tracking_flush(struct tracking *tr)
{
	struct tracking_line *tl = tr->lines, *next;
	//dbg(1, "enter(tr=%p)\n", tr);

	while (tl)
	{
		next = tl->next;
		street_data_free(tl->street);
		g_free(tl);
		tl = next;
	}

	tr->lines = NULL;
	tr->curr_line = NULL;
}

static int tracking_angle_diff(int a1, int a2, int full)
{
	int ret = (a1 - a2) % full;

	if (ret > full / 2)
		ret -= full;

	if (ret < -full / 2)
		ret += full;

	return ret;
}

static int tracking_angle_abs_diff(int a1, int a2, int full)
{
	int ret = tracking_angle_diff(a1, a2, full);

	if (ret < 0)
	{
		ret = -ret;
	}

	return ret;
}

static int tracking_angle_delta(struct tracking *tr, int vehicle_angle, int street_angle, int flags)
{
	int full = 180, ret = 360, fwd = 0, rev = 0;
	struct vehicleprofile *profile = tr->vehicleprofile;

	if (profile)
	{
		fwd = ((flags & route_get_real_oneway_mask(flags, profile->flags_forward_mask)) == profile->flags);
		rev = ((flags & route_get_real_oneway_mask(flags, profile->flags_reverse_mask)) == profile->flags);
	}

	if (fwd || rev)
	{
		if (!fwd || !rev)
		{
			full = 360;
			if (rev)
			{
				street_angle = (street_angle + 180) % 360;
			}
		}
		ret = tracking_angle_abs_diff(vehicle_angle, street_angle, full);
	}
	return ret * ret;
}


static int tracking_angle_delta_no_squared(struct tracking *tr, int vehicle_angle, int street_angle, int flags)
{
	int full = 180, ret = 360, fwd = 0, rev = 0;
	struct vehicleprofile *profile = tr->vehicleprofile;

	if (profile)
	{
		fwd = ((flags & route_get_real_oneway_mask(flags, profile->flags_forward_mask)) == profile->flags);
		rev = ((flags & route_get_real_oneway_mask(flags, profile->flags_reverse_mask)) == profile->flags);
	}

	if (fwd || rev)
	{
		if (!fwd || !rev)
		{
			full = 360;
			if (rev)
			{
				street_angle = (street_angle + 180) % 360;
			}
		}
		ret = tracking_angle_abs_diff(vehicle_angle, street_angle, full);
	}
	return ret;
}


static int tracking_is_connected(struct tracking *tr, struct coord *c1, struct coord *c2, struct item *ii)
{

#ifdef NAVIT_ROUTING_DEBUG_PRINT
//		dbg(0, "c1[0].x=%d c1[0].y=%d c1[1].x=%d c1[1].y=%d\n", c1[0].x, c1[0].y, c1[1].x, c1[1].y);
//		dbg(0, "c2[0].x=%d c2[0].y=%d c2[1].x=%d c2[1].y=%d\n", c2[0].x, c2[0].y, c2[1].x, c2[1].y);
#endif

	if (ii)
	{
		if ((tr->last_item_mark.id_hi == ii->id_hi) && (tr->last_item_mark.id_lo == ii->id_lo)&&(tr->last_item_mark.map == ii->map))
		{
			return 0;
		}
	}

	if (c1[0].x == c2[0].x && c1[0].y == c2[0].y)
	{
		return 0;
	}

	if (c1[0].x == c2[1].x && c1[0].y == c2[1].y)
	{
		return 0;
	}

	if (c1[1].x == c2[0].x && c1[1].y == c2[0].y)
	{
		return 0;
	}

	if (c1[1].x == c2[1].x && c1[1].y == c2[1].y)
	{
		return 0;
	}

	return tr->connected_pref;
}

static int tracking_is_no_stop(struct tracking *tr, struct coord *c1, struct coord *c2)
{
	if (c1->x == c2->x && c1->y == c2->y)
	{
		return tr->nostop_pref;
	}

	return 0;
}

static int tracking_is_on_route(struct tracking *tr, struct route *rt, struct item *item)
{
#ifdef USE_ROUTING
	if (! rt)
	{
		return 0;
	}

	if (route_contains(rt, item))
	{
		return 0;
	}

	return tr->route_pref;
#else
	return 0;
#endif	
}

static int tracking_value(struct tracking *tr, struct tracking_line *t, int offset, struct coord *lpnt, int min, int flags, struct item *ii)
{
	int value = 0;
	struct street_data *sd = t->street;

	if (flags & 1)
	{
		struct coord c1, c2, cp;
		c1.x = sd->c[offset].x;
		c1.y = sd->c[offset].y;
		c2.x = sd->c[offset + 1].x;
		c2.y = sd->c[offset + 1].y;
		cp.x = tr->curr_in.x;
		cp.y = tr->curr_in.y;
		// set "lpnt" coords to the point in the line (from "c1" -> "c2") that is closest to "cp"
		value += transform_distance_line_sq(&c1, &c2, &cp, lpnt);

#ifdef NAVIT_ROUTING_DEBUG_PRINT
//		dbg(0, "flags=%d:1:value=%d min=%d\n", flags, value, min);
#endif

	}

	if (value >= min)
	{
		return value;
	}

	// if road is not connected to previously used tracking road -> penalty
	if ((flags & 4) && tr->connected_pref)
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
//		int value_before = value;
#endif
		value += tracking_is_connected(tr, tr->last, &sd->c[offset], ii);

#ifdef NAVIT_ROUTING_DEBUG_PRINT
//		if (value_before != value)
//		{
//			dbg(0, "flags=%d:4:value_before=%d\n", flags, value_before);
//			dbg(0, "flags=%d:4:value=%d min=%d tr->connected_pref=%d\n", flags, value, min, tr->connected_pref);
//		}
#endif
	}

	if (value >= min)
	{
		return value;
	}

	// if road goes against our driving angle -> penalty
	if (flags & 2)
	{
		// value too high!!! // value += ( (tracking_angle_delta(tr, tr->curr_angle, t->angle[offset], sd->flags) * tr->angle_pref) >> 4 );
		value += (  tracking_angle_delta_no_squared(tr, tr->curr_angle, t->angle[offset], sd->flags) *  tr->angle_pref);

#ifdef NAVIT_ROUTING_DEBUG_PRINT
//		dbg(0, "flags=%d:2:value=%d min=%d curr_angle=%f road_angle=%f\n", flags, value, min, (float)tr->curr_angle, (float)t->angle[offset]);
#endif
	}

	if (value >= min)
	{
		return value;
	}

	// if position that is currently evaluated is the same as the last position -> penalty
	if ((flags & 8) && tr->nostop_pref)
	{
		value += tracking_is_no_stop(tr, lpnt, &tr->last_out);

#ifdef NAVIT_ROUTING_DEBUG_PRINT
		// dbg(0, "flags=%d:8:value=%d min=%d\n", flags, value, min);
#endif
	}

	if (value >= min)
	{
		return value;
	}

	if ((flags & 16) && tr->route_pref)
	{
		value += tracking_is_on_route(tr, tr->rt, &sd->item);

#ifdef NAVIT_ROUTING_DEBUG_PRINT
		// dbg(0, "flags=%d:16:value=%d min=%d\n", flags, value, min);
#endif
	}

	if ((flags & 32) && tr->overspeed_percent_pref && tr->overspeed_pref)
	{
		struct roadprofile *roadprofile = g_hash_table_lookup(tr->vehicleprofile->roadprofile_hash, (void *) t->street->item.type);

		if (roadprofile && tr->speed > roadprofile->speed * tr->overspeed_percent_pref / 100)
		{
			value += tr->overspeed_pref;
		}
	}

	return value;
}


static int tracking_time_millis()
{
	struct timeval  tv;
	gettimeofday(&tv, NULL);
	double time_in_mill = (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000;
	return (int)(time_in_mill);
}






void tracking_calc_and_send_possbile_turn_info(struct route_graph_point *rgp, struct route_graph_segment *s, int street_dir, int depth)
{

	struct route_graph_segment *start_3; // other segments that i could drive on next (but not on route)
	struct route_graph_segment *start_3_save; // other segments that i could drive on next (but not on route)
	struct route_graph_segment *drive_here; // i should drive on this segment next
	int dir = 1;


#if 1

	#define MAX_DUPL_CHECK_ENTRIES 20
	int angle_dupl_check[MAX_DUPL_CHECK_ENTRIES + 1];
	int angle_dupl_check_count = 0;
	int jk;
	int found_dupl = 0;
	int angles_found[MAX_DUPL_CHECK_ENTRIES + 1];
	int angles_found_count = 0;
	int drive_here_angle = -999;

	drive_here = rgp->seg;

	int found_first = 0;

	char *next_roads_and_angles = NULL;
	char *next_roads_and_angles_old = NULL;

	struct route_graph_segment *tmp1 = NULL;

	int cur_is_lower_type = navigation_is_low_level_street(s->data.item.type);

	tmp1 = rgp->start;
	while (tmp1)
	{
		if ((cur_is_lower_type == 1) || (navigation_is_low_level_street(tmp1->data.item.type) == 0 ))
		{

			if (tmp1 != s
				&& tmp1->data.item.type != type_street_turn_restriction_no 
				&& tmp1->data.item.type != type_street_turn_restriction_only
				&& !(route_get_real_oneway_flag(tmp1->data.flags, NAVIT_AF_ONEWAYREV)) 
				&& is_turn_allowed(rgp, s, tmp1))
			{
				//dbg(0, "RR:x:001:sn:tmp1=%p\n", tmp1);

				//dbg(0, "RR:x:001:sn:item hi:%d lo:%d type=%s\n", tmp1->data.item.id_hi, tmp1->data.item.id_lo, item_to_name(tmp1->data.item.type));

				// item_dump_attr_stdout(&tmp1->data.item, tmp1->data.item.map);

				struct coord ccc_cc, ccc_ss, ccc_ee;
				int turn_angle3 = route_road_to_road_angle_get_segs(s, tmp1, street_dir, &ccc_cc, &ccc_ss, &ccc_ee, 0);
				//dbg(0, "RR:04.22.2:angle (real)=%d\n", turn_angle3);
#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_2
				route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, turn_angle3, street_dir);
#endif


				angles_found[angles_found_count] = turn_angle3;
				angles_found_count++;
				if (angles_found_count > MAX_DUPL_CHECK_ENTRIES)
				{
					angles_found_count--;
				}


				if (drive_here == tmp1)
				{
					//dbg(0, "RR:04.22.1:DRIVE HERE!!\n");

					drive_here_angle = turn_angle3;
				}

				if (found_first == 0)
				{
					found_first = 1;
				}


			}
		}

		tmp1 = tmp1->start_next;

	}

	tmp1 = rgp->end;
	while (tmp1)
	{
		if ((cur_is_lower_type == 1) || (navigation_is_low_level_street(tmp1->data.item.type) == 0 ))
		{

			if (tmp1 != s
			&& tmp1->data.item.type != type_street_turn_restriction_no
			&& tmp1->data.item.type != type_street_turn_restriction_only
			&& !(route_get_real_oneway_flag(tmp1->data.flags, NAVIT_AF_ONEWAY))
			&& is_turn_allowed(rgp, s, tmp1))
			{
				//dbg(0, "RR:x:001:EN:tmp1=%p\n", tmp1);

				//dbg(0, "RR:x:001:EN:item hi:%d lo:%d type=%s\n", tmp1->data.item.id_hi, tmp1->data.item.id_lo, item_to_name(tmp1->data.item.type));

				// item_dump_attr_stdout(&tmp1->data.item, tmp1->data.item.map);

				struct coord ccc_cc, ccc_ss, ccc_ee;
				int turn_angle3 = route_road_to_road_angle_get_segs(s, tmp1, street_dir, &ccc_cc, &ccc_ss, &ccc_ee, 0);
				//dbg(0, "RR:04.22.2:angle (real)=%d\n", turn_angle3);
#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_2
				route_add_to_sharp_turn_list(&ccc_cc, &ccc_ss, &ccc_ee, turn_angle3, street_dir);
#endif


				angles_found[angles_found_count] = turn_angle3;
				angles_found_count++;
				if (angles_found_count > MAX_DUPL_CHECK_ENTRIES)
				{
					angles_found_count--;
				}


				if (drive_here == tmp1)
				{
					//dbg(0, "RR:04.22.1:DRIVE HERE!!\n");

					drive_here_angle = turn_angle3;
				}

				if (found_first == 0)
				{
					found_first = 1;
				}

			}
		}

		tmp1 = tmp1->end_next;

	}

	if (found_first == 1)
	{
		//dbg(0, "RR:x:008:\n");

		if (angles_found_count > 0)
		{
			int jk;
			int ad;
			int is_dupl = 0;
			for (jk = 0; jk < angles_found_count; jk++)
			{
				//dbg(0, "RR:x:009:\n");

				is_dupl = 0;

				if (jk > 0)
				{
					for (ad = 0; ad < angles_found_count; ad++)
					{
						if ((ad != jk) && (angles_found[ad] == angles_found[jk]))
						{
							// we already have this angle, continue to the next one
							//dbg(0, "RR:x:dupl\n");
							is_dupl = 1;
							continue;
						}
					}
				}

				if (is_dupl == 1)
				{
					continue;
				}

				// dbg(0, "RR:x:010\n");

				if (angles_found[jk] == drive_here_angle)
				{
					next_roads_and_angles_old = next_roads_and_angles;
					if (jk != 0)
					{
						next_roads_and_angles = g_strdup_printf("%s|x%d", next_roads_and_angles, angles_found[jk]);
						g_free(next_roads_and_angles_old);
					}
					else
					{
						next_roads_and_angles = g_strdup_printf("x%d", angles_found[jk]);
					}
				}
				else
				{
					next_roads_and_angles_old = next_roads_and_angles;
					if (jk != 0)
					{
						next_roads_and_angles = g_strdup_printf("%s|%d", next_roads_and_angles, angles_found[jk]);
						g_free(next_roads_and_angles_old);
					}
					else
					{
						next_roads_and_angles = g_strdup_printf("%d", angles_found[jk]);
					}
				}
			}

			// example: "175|180|x250" // "x" mean this is the road we should drive on next, others are angles
			//           0 -> straight
			// less than 0 -> right
			// more than 0 -> left
			// ** // dbg(0, "RR:x:RES=%s\n", next_roads_and_angles);
#ifdef HAVE_API_ANDROID
			android_send_generic_text((4 + depth), next_roads_and_angles);
#endif
			g_free(next_roads_and_angles);
		}
		else
		{
#ifdef HAVE_API_ANDROID
			android_send_generic_text((4 + depth), "");
#endif
		}
	}
	else
	{
#ifdef HAVE_API_ANDROID
		android_send_generic_text((4 + depth), "");
#endif
	}


#endif

}


void tracking_send_lanes_info(struct map_rect *mr, int id_hi, int id_lo, int street_dir, int depth)
{

	struct item *item_lanes;

	// COST: 102
	dbg(0, "COST:102\n");
	item_lanes = map_rect_get_item_byid(mr, id_hi, id_lo);

	if (item_lanes)
	{
		//dbg(0, "LL01:000.1\n");

		struct attr attr_l;
		gchar *lanes_info_l = NULL;
		gchar *lanes_info_l_for = NULL;
		gchar *lanes_info_tl = NULL;

		if (item_attr_get(item_lanes, attr_street_lanes, &attr_l))
		{
			lanes_info_l = g_strdup_printf("%s", attr_l.u.str);
			//dbg(0, "LL01:000.2 %s\n", attr_l.u.str);
		}

		if (item_attr_get(item_lanes, attr_street_lanes_forward, &attr_l))
		{
			lanes_info_l_for = g_strdup_printf("%s", attr_l.u.str);
			//dbg(0, "LL01:000.3 %s\n", attr_l.u.str);
		}

		if (item_attr_get(item_lanes, attr_street_turn_lanes, &attr_l))
		{
			lanes_info_tl = g_strdup_printf("%s", attr_l.u.str);
			//dbg(0, "LL01:000.4 %s\n", attr_l.u.str);
		}

		if ((lanes_info_l) && (lanes_info_tl))
		{
			//dbg(0, "LL01:001\n");

			gchar *lanes_info = NULL;

			if (lanes_info_l_for)
			{
				//dbg(0, "LL01:002\n");
				lanes_info = g_strdup_printf("%d:%s:%s:%s", street_dir, lanes_info_l, lanes_info_l_for, lanes_info_tl);
				g_free(lanes_info_l_for);
			}
			else
			{
				//dbg(0, "LL01:003\n");
				lanes_info = g_strdup_printf("%d:%s:%s:%s", street_dir, lanes_info_l, lanes_info_l, lanes_info_tl);
			}
			g_free(lanes_info_l);
			g_free(lanes_info_tl);

			//dbg(0, "LL01:004 %s\n", lanes_info);

#ifdef HAVE_API_ANDROID
			// example:  "1:3:2:none|through;left"
			android_send_generic_text((8 + depth), lanes_info);
#endif
			g_free(lanes_info);
		}
		else
		{
#ifdef HAVE_API_ANDROID
			android_send_generic_text((8 + depth), "");
#endif
		}
	}
}






// ###############
//
// this gets called (also) every time the gps sends new coord data (called from navit.c --> navit_vehicle_update) only if "tracking_flag" is "true" !!
//
// ###############
void tracking_update(struct tracking *tr, struct vehicle *v, struct vehicleprofile *vehicleprofile, enum projection pro)
{
	struct tracking_line *t;
	int i, value, min, time;
	struct coord lpnt;
	struct coord cin;
	struct attr valid, speed_attr, direction_attr, coord_geo, lag, time_attr, static_speed, static_distance;
	double speed, direction;
	int too_slow_or_little_movement = 0;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:routetracking_update: enter\n");
#endif

	if (v)
	{
		tr->vehicle = v;
	}

	if (vehicleprofile)
	{
		tr->vehicleprofile = vehicleprofile;
	}

	if (!tr->vehicle)
	{
		return;
	}

	if (!vehicle_get_attr(tr->vehicle, attr_position_valid, &valid, NULL))
	{
		valid.u.num = attr_position_valid_valid;
	}

	if (valid.u.num == attr_position_valid_invalid)
	{
		tr->valid = valid.u.num;
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:(position invalid):return 002\n");
#endif
		return;
	}

	if (!vehicle_get_attr(tr->vehicle, attr_position_speed, &speed_attr, NULL) || !vehicle_get_attr(tr->vehicle, attr_position_direction, &direction_attr, NULL) || !vehicle_get_attr(tr->vehicle, attr_position_coord_geo, &coord_geo, NULL) || !vehicle_get_attr(tr->vehicle, attr_position_time_iso8601, &time_attr, NULL))
	{
		//dbg(0,"failed to get position data %d %d %d %d\n",
		//vehicle_get_attr(tr->vehicle, attr_position_speed, &speed_attr, NULL),
		//vehicle_get_attr(tr->vehicle, attr_position_direction, &direction_attr, NULL),
		//vehicle_get_attr(tr->vehicle, attr_position_coord_geo, &coord_geo, NULL),
		//vehicle_get_attr(tr->vehicle, attr_position_time_iso8601, &time_attr, NULL));
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:(some or all position attributes missing):return 003\n");
#endif
		return;
	}

	if (!vehicleprofile_get_attr(vehicleprofile, attr_static_speed, &static_speed, NULL) || !vehicleprofile_get_attr(vehicleprofile, attr_static_distance, &static_distance, NULL))
	{
		static_speed.u.num = 3;
		static_distance.u.num = 10;
		//dbg(1,"Using defaults for static position detection\n");
	}

	//dbg(2,"Static speed: %u, static distance: %u\n",static_speed.u.num, static_distance.u.num);
	time = iso8601_to_secs(time_attr.u.str);
	speed = *speed_attr.u.numd;
	direction = *direction_attr.u.numd;
	tr->valid = attr_position_valid_valid;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:routetracking_update: v direction=%f lat=%f lon=%f\n", direction, coord_geo.u.coord_geo->lat, coord_geo.u.coord_geo->lng);
	struct coord cc999;
	transform_from_geo(pro, coord_geo.u.coord_geo, &cc999);
	dbg(0, "ROUTExxPOSxx:routetracking_update (unchanged GPS pos): v x=%d y=%d\n", cc999.x, cc999.y);
#endif

	transform_from_geo(pro, coord_geo.u.coord_geo, &tr->curr_in);

	if ((speed < static_speed.u.num && transform_distance(pro, &tr->last_in, &tr->curr_in) < static_distance.u.num))
	{
		//dbg(1,"static speed %f coord 0x%x,0x%x vs 0x%x,0x%x\n",speed,tr->last_in.x,tr->last_in.y, tr->curr_in.x, tr->curr_in.y);
		tr->valid = attr_position_valid_static;
		// tr->valid = attr_position_valid_valid;
		tr->speed = 0;

		too_slow_or_little_movement = 1;
		//dbg(0, "return 001\n");
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:(too slow or to little movement):return 001 speed=%f movement=%f\n", speed, transform_distance(pro, &tr->last_in, &tr->curr_in));
#endif
		// -- dont end here!! -- // return;
	}

	if (vehicle_get_attr(tr->vehicle, attr_lag, &lag, NULL) && lag.u.num > 0)
	{
		//dbg(0, "Vehicle LAG on. lag=%d\n", lag.u.num);

		double espeed;
		int edirection;
		if (time - tr->time == 1)
		{
			/* dbg(1,"extrapolating speed from %f and %f (%f)\n",tr->speed, speed, speed-tr->speed); */
			espeed = speed + (speed - tr->speed) * lag.u.num / 10;
			/* dbg(1,"extrapolating angle from %f and %f (%d)\n",tr->direction, direction, tracking_angle_diff(direction,tr->direction,360)); */
			edirection = direction + tracking_angle_diff(direction, tr->direction, 360) * lag.u.num / 10;
		}
		else
		{
			//dbg(1,"no speed and direction extrapolation\n");
			espeed = speed;
			edirection = direction;
		}
		//dbg(1,"lag %d speed %f direction %d\n",lag.u.num,espeed,edirection);
		//dbg(1,"old 0x%x,0x%x\n",tr->curr_in.x, tr->curr_in.y);
		transform_project(pro, &tr->curr_in, espeed * lag.u.num / 36, edirection, &tr->curr_in);
		//dbg(1,"new 0x%x,0x%x\n",tr->curr_in.x, tr->curr_in.y);
	}

	tr->time = time;
	tr->pro = pro;
#if 0

	tracking_process_cdf(&tr->cdf, pc, &pcf, angle, &anglef, speed, fixtime);
#endif
	//dbg(0,"curr_IN :20 0x%x,0x%x\n", tr->curr_in.x, tr->curr_in.y);
	//dbg(0,"curr_out:21 0x%x,0x%x\n", tr->curr_out.x, tr->curr_out.y);

	tr->curr_angle = tr->direction = direction;
	tr->speed = speed;
	tr->last_in = tr->curr_in;
	tr->last_out = tr->curr_out;

	// ---------------
	// tr->last[0] = tr->curr[0];
	// tr->last[1] = tr->curr[1];

	tr->last[0].x = tr->curr[0].x;
	tr->last[0].y = tr->curr[0].y;

	tr->last[1].x = tr->curr[1].x;
	tr->last[1].y = tr->curr[1].y;

	tr->last_item_mark.id_hi = tr->curr_item_mark.id_hi;
	tr->last_item_mark.id_lo = tr->curr_item_mark.id_lo;
	tr->last_item_mark.map = tr->curr_item_mark.map;

	global_debug_trlast_start.x = tr->last[0].x;
	global_debug_trlast_start.y = tr->last[0].y;
	global_debug_trlast_end.x = tr->last[1].x;
	global_debug_trlast_end.y = tr->last[1].y;

	// ---------------

	//dbg(0,"curr_out:60 0x%x,0x%x\n", tr->curr_out.x, tr->curr_out.y);

	if (!tr->lines || transform_distance(pro, &tr->last_updated, &tr->curr_in) > TRACKING_MAX_DIST_RELOAD_MAP_DATA_THRESHOLD) // 500 (default)
	{
		dbg(0, "**TRACKING:flush**\n");
		tracking_flush(tr);
		tracking_doupdate_lines(tr, &tr->curr_in, pro, TRACKING_MAX_DIST_RELOAD_MAP_DATA_LIMIT); // max_dist = 1000 (default)
		tr->last_updated = tr->curr_in;
		//dbg(0,"curr_IN :61 0x%x,0x%x\n", tr->curr_in.x, tr->curr_in.y);
		//dbg(0,"curr_out:62 0x%x,0x%x\n", tr->curr_out.x, tr->curr_out.y);
		//dbg(1,"update end\n");
	}

	//dbg(0,"curr_out:70 0x%x,0x%x\n", tr->curr_out.x, tr->curr_out.y);
	tr->street_direction = 0;
	t = tr->lines;
	tr->curr_line = NULL;
	min = INT_MAX / 2;

	int angle_new = tr->direction;
	int angle_delta_save;





	// -------- full route ---------------------------------------------
	// -------- full route ---------------------------------------------
	struct route *route2 = NULL;
	struct map *route_map2 = NULL;
	struct map_rect *mr2 = NULL;
	struct item *item2 = NULL;
	struct coord c2;
	struct coord c2_prev;
	struct coord_geo g1;
	struct coord_geo g2;
	struct coord_geo g3;
	struct coord_geo g4;
	struct coord cp_xx;
	struct attr attr_si;
	struct item *sitem;
	int first2 = 1;
	int max_route_items_to_check = 5;
	int route_items_checked = 0;
	int min2 = INT_MAX / 2;
	int winner_hi = 0;
	int winner_lo = 0;
	int winner_offset = 0;
	struct map *winner_map = NULL;
	int temp_hi = 0;
	int temp_lo = 0;
	struct map *temp_map = NULL;
	int temp_offset = 0;
	int first_seg = 1;
	int route_ongoing_penalty = 0;
	int on_what_route_seg = -1;
	int route_seg_winner_hi = -1;
	int route_seg_winner_lo = -1;


	global_debug_route_seg_winner_start.x = 0;
	global_debug_route_seg_winner_start.y = 0;
	global_debug_route_seg_winner_end.x = 0;
	global_debug_route_seg_winner_end.y = 0;

	global_debug_seg_winner_start.x = 0;
	global_debug_seg_winner_start.y = 0;
	global_debug_seg_winner_end.x = 0;
	global_debug_seg_winner_end.y = 0;

	global_debug_route_seg_winner_p_start.x = 0;
	global_debug_route_seg_winner_p_start.y = 0;

	global_debug_seg_route_start.x = 0;
	global_debug_seg_route_start.y = 0;

	global_debug_seg_route_end.x = 0;
	global_debug_seg_route_end.y = 0;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	int millis_saved = tracking_time_millis();
	dbg(0,"ROUTExxPOSxx:millis_saved=%d\n", millis_saved);
#endif

	route2 = tr->rt;

#ifdef NAVIT_TRACKING_STICK_TO_ROUTE

	if ((route2) && (route2->route_status != route_status_no_destination))
	{
		route_map2 = route_get_map(route2);

		if (route_map2)
		{
			mr2 = map_rect_new(route_map2, NULL);
		}

		if (mr2)
		{
			item2 = map_rect_get_item(mr2);
		}

		if (item2 && item2->type == type_route_start_reverse) // we need to turn around! --> then disable stick to route-seg
		{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0,"ROUTExxPOSxx:turn around, disable snap to route-seg\n");
#endif
		}
		else
		{

			if (item2 && item2->type == type_route_start)
			{
				item2 = map_rect_get_item(mr2);
			}
			//dbg(0,"curr_out:loopR.00:----------------------------------\n");

			if (item2)
			{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				//char *f11 = g_strdup_printf("/sdcard/zanavi_route_001_%d.gpx", millis_saved);
				//FILE *fp2 = navit_start_gpx_file(f11);
				//g_free(f11);
#endif
				if (item2)
				{
					temp_offset = -1;

					//dbg(0,"curr_out:loopR.02i:id_hi=%d id_lo=%d\n", item2->id_hi, item2->id_lo);
					if (item_attr_get(item2, attr_street_item, &attr_si))
					{
						sitem = attr_si.u.item;
						// item_dump_attr_stdout(sitem, route_map2);
						if (sitem)
						{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
//							dbg(0,"curr_out:loopR.02i2:id_hi=%d id_lo=%d\n", sitem->id_hi, sitem->id_lo);
#endif
							temp_hi = sitem->id_hi;
							temp_lo = sitem->id_lo;
							temp_map = sitem->map;
						}
					}
					else
					{
						temp_hi = 0;
						temp_lo = 0;
						temp_map = NULL;
					}
					//item_dump_attr_stdout(item2, route_map2);
				}

				int num = item_coord_get(item2, &c2, 1);
				if (num == 1)
				{
					global_debug_seg_route_start.x = c2.x;
					global_debug_seg_route_start.y = c2.y;

					global_debug_seg_route_end.x = 0;
					global_debug_seg_route_end.y = 0;

					temp_offset = -1;
					first2 = 0;

					c2_prev.x = c2.x;
					c2_prev.y = c2.y;

					//dbg(0,"curr_out:loopR.01:%d\n", route_items_checked);
					route_items_checked++;
					while (item2)
					{
						//dbg(0,"curr_out:loopR.02:%d\n", route_items_checked);
						if (!item_coord_get(item2, &c2, 1))
						{
							item2 = map_rect_get_item(mr2);

							temp_offset = -1;

							if (item2)
							{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
//								dbg(0,"curr_out:loopR.02i:id_hi=%d id_lo=%d\n", item2->id_hi, item2->id_lo);
#endif
								if (item_attr_get(item2, attr_street_item, &attr_si))
								{
									sitem = attr_si.u.item;
									// item_dump_attr_stdout(sitem, route_map2);
									if (sitem)
									{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
//										dbg(0,"curr_out:loopR.02i2:id_hi=%d id_lo=%d\n", sitem->id_hi, sitem->id_lo);
#endif
										temp_hi = sitem->id_hi;
										temp_lo = sitem->id_lo;
										temp_map = sitem->map;
									}
								}
								//item_dump_attr_stdout(item2, route_map2);
							}

							route_items_checked++;
							//dbg(0,"curr_out:loopR.03:%d\n", route_items_checked);
							if (route_items_checked > max_route_items_to_check)
							{
								// check no more route items
								break;
							}
							first2 = 1;
							// we favor route segments nearer to start of route (penalty increases the further we go alone the route)
							route_ongoing_penalty = route_ongoing_penalty + 4;
							continue;
						}

						temp_offset++;
						//c2_prev.x = c2.x;
						//c2_prev.y = c2.y;

						if (first2 == 1)
						{
							//dbg(0,"curr_out:loopR.02f\n");
#ifdef NAVIT_ROUTING_DEBUG_PRINT
						//	navit_add_trkpoint_to_gpx_file(fp2, &c2);
#endif
							first2 = 0;
						}
						else
						{

							if (first_seg == 1)
							{
								global_debug_seg_route_end.x = c2.x;
								global_debug_seg_route_end.y = c2.y;
								first_seg = 0;
							}


							//dbg(0,"curr_out:loopR.04:%d\n", route_items_checked);

							cp_xx.x = tr->curr_in.x;
							cp_xx.y = tr->curr_in.y;
							// set "lpnt" coords to the point in the line (from "c1" -> "c2") that is closest to "cp"
							value = transform_distance_line_sq(&c2_prev, &c2, &cp_xx, &lpnt);

#ifdef NAVIT_ROUTING_DEBUG_PRINT
#if 0
							dbg(0,"curr_out:loopR.05:v=%d hi=%d lo=%d off=%d\n", value, temp_hi, temp_lo, temp_offset);
							transform_to_geo(projection_mg, &c2_prev, &g1);
							transform_to_geo(projection_mg, &c2, &g2);
							transform_to_geo(projection_mg, &cp_xx, &g3);
							transform_to_geo(projection_mg, &lpnt, &g4);
							//dbg(0,"curr_out:loopR.06:route seg %4.16f,%4.16f --> %4.16f,%4.16f\n", g1.lat, g1.lng, g2.lat, g2.lng);
							dbg(0,"curr_out:loopR.07:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:START|%4.6f,%4.6f&markers=color:red|label:END|%4.6f,%4.6f&markers=color:green|label:POS|%4.6f,%4.6f\n", g1.lat, g1.lng, g2.lat, g2.lng, g3.lat, g3.lng);
							//navit_add_trkpoint_to_gpx_file(fp2, &c2);
#endif
#endif

							if ((value + route_ongoing_penalty) < min2)
							{
								min2 = value + route_ongoing_penalty;
								winner_hi = temp_hi;
								winner_lo = temp_lo;
								winner_map = temp_map;
								winner_offset = temp_offset;

								route_seg_winner_hi = item2->id_hi;
								route_seg_winner_lo = item2->id_lo;

								on_what_route_seg = route_items_checked;
								// dbg(0, "on_what_route_seg=%d hi=%d lo=%d\n", on_what_route_seg, route_seg_winner_hi, route_seg_winner_lo);

								global_debug_route_seg_winner_start.x = c2_prev.x;
								global_debug_route_seg_winner_start.y = c2_prev.y;
								global_debug_route_seg_winner_end.x = c2.x;
								global_debug_route_seg_winner_end.y = c2.y;

								global_debug_route_seg_winner_p_start.x = lpnt.x;
								global_debug_route_seg_winner_p_start.y = lpnt.y;

							}
						}

						c2_prev.x = c2.x;
						c2_prev.y = c2.y;
					}
				}
#ifdef NAVIT_ROUTING_DEBUG_PRINT
				//navit_end_gpx_file(fp2);
#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT
				dbg(0,"curr_out:loopR.99:++++++++++++++++++++++++++++++++++ min2=%d w_hi=%d w_lo=%d w_off=%d\n", min2, winner_hi, winner_lo, winner_offset);
#endif
				winner_offset--;

				if (min2 < 800)
				{
					// --> min3 and min2 should be the same, so no need to calculate min3 here
					//int min3 = transform_distance_sq(&global_debug_route_seg_winner_p_start, &tr->curr_in);
					//if (min3 < 800)
					//{
						// set gps input coord to found "winner" point on route
						tr->curr_in.x = global_debug_route_seg_winner_p_start.x;
						tr->curr_in.y = global_debug_route_seg_winner_p_start.y;
					//}
				}

			}
		}

		if (mr2)
		{
			map_rect_destroy(mr2);
		}
	}

#endif

	// -------- full route ---------------------------------------------
	// -------- full route ---------------------------------------------




	//dbg(0,"curr_out:loop.00:----------------------------------\n");
	int count_001 = 0;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	//char *f22 = g_strdup_printf("/sdcard/zanavi_route_002_%d.gpx", millis_saved);
	//FILE *fp3 = navit_start_gpx_file(f22);
	//g_free(f22);

	//char *f23 = g_strdup_printf("/sdcard/zanavi_route_002_%d_chosen.gpx", millis_saved);
	//FILE *fp3a = navit_start_gpx_file(f23);
	//g_free(f23);
#endif

	struct item *item33 = NULL;
	struct item *item34 = NULL;
	struct map_rect *mr33 = NULL;
	struct roadprofile *roadp;

#if 0
	global_debug_coord_list_items = 0;
#endif

	while (t)
	{
		count_001++;
		//dbg(0,"curr_out:loop.01:%d\n", count_001);

		struct street_data *sd = t->street;
		item33 = &(sd->item);

		// if (route2) // are we in routing mode?
		{
			roadp = vehicleprofile_get_roadprofile(tr->vehicleprofile, item33->type);
			// only include any roads that have a road profile in our vehicle profile
			if (roadp)
			{

				// if ( ((winner_hi == 0) && (winner_lo == 0)) || (!item33) || ((winner_hi == item33->id_hi) && (winner_lo == item33->id_lo)) )
				{
					for (i = 0; i < sd->count - 1; i++)
					{

						count_001++;
						// dbg(0,"curr_out:loop.02:%d\n", i);

						// value = distance from point tr->curr_in.(x,y) to closest point on line (sd[i] -> sd[i+1]) squared
						// lpnt is set to point on line (sd[i] -> sd[i+1]) which is closest to tr->curr_in.(x,y)

						if (global_demo_vehicle_short_switch == 1)
						{
							value = tracking_value(tr, t, i, &lpnt, min, 1, item33);
						}
						else
						{
							value = tracking_value(tr, t, i, &lpnt, min, ( 1 | 2 | 4 ), item33);
						}

#ifdef NAVIT_ROUTING_DEBUG_PRINT
//						if (value < min)
//						{
//							dbg(0,"curr_out:loop.02a:i=%d v=%d min=%d id_hi=%d id_lo=%d w_off=%d w_value=%d\n", i, value, min, item33->id_hi, item33->id_lo, winner_offset, min2);
//						}
#endif

						if ( ((winner_hi != 0) && (winner_lo != 0)) && (item33) && ((winner_hi == item33->id_hi) && (winner_lo == item33->id_lo)) && (winner_map == item33->map) )
						{
							//if (winner_offset == i)
							//{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
								// dbg(0,"curr_out:loop.02a1:i=%d v=%d min=%d\n", i, value, min);
#endif
								value = value - CORRECT_FOR_ON_ROUTE_SEG_VALUE; // default = 2600
#ifdef NAVIT_ROUTING_DEBUG_PRINT
								// dbg(0,"curr_out:loop.02a2:i=%d v=%d min=%d\n", i, value, min);
#endif
							//}
						}


#ifdef NAVIT_ROUTING_DEBUG_PRINT
						//navit_add_trkpoint_to_gpx_file(fp3, &sd->c[i]);
						//navit_add_trkpoint_to_gpx_file(fp3, &sd->c[i + 1]);
						//navit_end_gpx_track_seg(fp3);
						//navit_start_gpx_track_seg(fp3);
#endif


#if 0
						if ((sd) && (item33))
						{
							if ((item33->id_hi == 8013)&&(item33->id_lo == 108612))
							{
								if ((global_debug_coord_list_items + 2) > MAX_DEBUG_COORDS)
								{
									global_debug_coord_list_items = 0;
								}

								dbg(0,"curr_out:debug:START:coord_list=%d\n", global_debug_coord_list_items);
								global_debug_coord_list[global_debug_coord_list_items].x = sd->c[i].x;
								global_debug_coord_list[global_debug_coord_list_items].y = sd->c[i].y;
								global_debug_coord_list_items++;
								dbg(0,"curr_out:debug:END:coord_list=%d\n", global_debug_coord_list_items);
								global_debug_coord_list[global_debug_coord_list_items].x = sd->c[i + 1].x;
								global_debug_coord_list[global_debug_coord_list_items].y = sd->c[i + 1].y;
								global_debug_coord_list_items++;
							}
						}
#endif


#if 0
							//dbg(0,"curr_out:loop.02i1:id_hi=%d id_lo=%d\n", item33->id_hi, item33->id_lo);
							if (item33->map)
							{
								mr33 = map_rect_new(item33->map, NULL);
								if (mr33)
								{
									item33 = map_rect_get_item_byid(mr33, item33->id_hi, item33->id_lo);
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									if (item33)
									{
										dbg(0,"curr_out:loop.02i2:id_hi=%d id_lo=%d\n", item33->id_hi, item33->id_lo);
										item_dump_attr_stdout(item33, item33->map);
									}
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									map_rect_destroy(mr33);
								}
							}
#endif

						if (value < min)
						{
							//dbg(0,"curr_out:loop.03\n");

							struct coord lpnt_tmp;
							int angle_delta = tracking_angle_abs_diff(tr->curr_angle, t->angle[i], 360);
							tr->curr_line = t;
							tr->pos = i;

							// -------------------
							// -------------------
							// tr->curr[0] = sd->c[i];
							// tr->curr[1] = sd->c[i + 1];

							tr->curr[0].x = sd->c[i].x;
							tr->curr[0].y = sd->c[i].y;

							tr->curr[1].x = sd->c[i + 1].x;
							tr->curr[1].y = sd->c[i + 1].y;

							if ((sd) && (item33))
							{
								tr->curr_item_mark.id_hi = item33->id_hi;
								tr->curr_item_mark.id_lo = item33->id_lo;
								tr->curr_item_mark.map = item33->map;
							}
							else
							{
								tr->curr_item_mark.id_hi = 0x0;
								tr->curr_item_mark.id_lo = 0x0;
								tr->curr_item_mark.map = NULL;
							}
							// -------------------
							// -------------------


							if (sd)
							{
								tr->curr_max_speed = sd->maxspeed;
							}
							else
							{
								tr->curr_max_speed = -1;
							}



							global_debug_seg_winner_start.x = sd->c[i].x;
							global_debug_seg_winner_start.y = sd->c[i].y;
							global_debug_seg_winner_end.x = sd->c[i + 1].x;
							global_debug_seg_winner_end.y = sd->c[i + 1].y;


#if 0
							dbg(0,"curr_out:loop.02i1:id_hi=%d id_lo=%d\n", item33->id_hi, item33->id_lo);
							if (item33->map)
							{
								mr33 = map_rect_new(item33->map, NULL);
								if (mr33)
								{
									item34 = map_rect_get_item_byid(mr33, item33->id_hi, item33->id_lo);
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									if (item34)
									{
										dbg(0,"curr_out:loop.02i2:id_hi=%d id_lo=%d map=%p\n", item34->id_hi, item34->id_lo, item34->map);
										item_dump_attr_stdout(item34, item34->map);
									}
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									// ------ DEBUG ---------
									map_rect_destroy(mr33);
								}
								item34 = NULL;
							}
#endif


#ifdef NAVIT_ROUTING_DEBUG_PRINT
							//transform_to_geo(projection_mg, &sd->c[i], &g1);
							//transform_to_geo(projection_mg, &sd->c[i + 1], &g2);
							//dbg(0,"curr_out:loop.06:route seg %4.6f,%4.6f --> %4.6f,%4.6f\n", g1.lat, g1.lng, g2.lat, g2.lng);
							//dbg(0,"curr_out:loop.07:http://maps.google.com/maps/api/staticmap?size=512x512&markers=color:blue|label:START|%4.6f,%4.6f&markers=color:red|label:END|%4.6f,%4.6f\n", g1.lat, g1.lng, g2.lat, g2.lng);
#endif

							/*
							 dbg(1,"lpnt.x=0x%x,lpnt.y=0x%x pos=%d %d+%d+%d+%d=%d\n", lpnt.x, lpnt.y, i,
							 transform_distance_line_sq(&sd->c[i], &sd->c[i+1], &cin, &lpnt_tmp),
							 tracking_angle_delta(tr, tr->curr_angle, t->angle[i], 0)*tr->angle_pref,
							 tracking_is_connected(tr, tr->last, &sd->c[i]) ? tr->connected_pref : 0,
							 lpnt.x == tr->last_out.x && lpnt.y == tr->last_out.y ? tr->nostop_pref : 0,
							 value
							 );
							 */

							// ---------------------------------------------------------------------------------
							// ---------------------------------------------------------------------------------
							// change the current Vehicle (GPS-)Position to the point on the closest line/street
							tr->curr_out.x = lpnt.x;
							tr->curr_out.y = lpnt.y;

							global_debug_seg_winner_p_start.x = lpnt.x;
							global_debug_seg_winner_p_start.y = lpnt.y;

							angle_new = t->angle[i]; // also set angle/direction to the angle of the line/street
							// dbg(0, "ROUTExxPOSxx:coord_geo_valid=0 001\n");
							tr->coord_geo_valid = 0;
							//dbg(0,"curr_out:70.2 0x%x,0x%x\n", tr->curr_out.x, tr->curr_out.y);
							// change the current Vehicle (GPS-)Position to the point on the closest line/street
							// ---------------------------------------------------------------------------------
							// ---------------------------------------------------------------------------------

							angle_delta_save = angle_delta;
							if (angle_delta < 70) // 90° - 20°
							{
								tr->street_direction = 1;
							}
							else if (angle_delta > 110) // 90° + 20°
							{
								tr->street_direction = -1;
							}
							else
							{
								tr->street_direction = 0;
							}

							min = value;
						}
					}
				}
			}
#ifdef NAVIT_ROUTING_DEBUG_PRINT

#if 0
			else
			{
				dbg(0, "ROUTExxPOSxx:(no roadprofile for this road)\n");
			}
#endif

#endif

		}
		t = t->next;
	}

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	//navit_add_trkpoint_to_gpx_file(fp3a, &(tr->curr_out));
	//navit_end_gpx_file(fp3a);
#endif


#ifdef NAVIT_ROUTING_DEBUG_PRINT
	//navit_end_gpx_file(fp3);
	dbg(0,"curr_out:loop.99:++++++++++++++++++++++++++++++++++ count=%d\n", count_001);
#endif


	if (tr->speed < FIVE_KHM)
	{
		tr->street_direction = 0; // less than 5 km/h vehicle can turn freely on map
	}
	else if (tr->speed < TEN_KHM)
	{
		if (tr->street_direction != 0)
		{
			if (angle_delta_save < 85) // 90° - 5°
			{
				tr->street_direction = 1;
			}
			else if (angle_delta_save > 95) // 90° + 5°
			{
				tr->street_direction = -1;
			}
			else
			{
				tr->street_direction = 0; // vehicle can turn freely on map
			}
		}
	}


	// dbg(0, "SD:max_speed=%d\n", tr->curr_max_speed);
#ifdef HAVE_API_ANDROID
	android_return_generic_int(8, tr->curr_max_speed);
#endif


	// also set angle/direction to the angle of the line/street
	//dbg(0, "v-angle=%d\n", tr->curr_angle);
	if (tr->street_direction == -1)
	{
		// we need to drive the street in the other direction
		angle_new = (angle_new + 180) % 360;

		tr->curr_angle = angle_new;
		tr->direction = tr->curr_angle;
	}
	else if (tr->street_direction == 1)
	{
		tr->curr_angle = angle_new;
		tr->direction = tr->curr_angle;
	}
	else // if (tr->street_direction == 0)
	{
		// we are almost 90° to the street/line, leave direction unchanged (for now) or we disabled this function (because of low speed)
		// tr->curr_angle = angle_new;
		// tr->direction = tr->curr_angle;
	}
	//dbg(0, "s-angle=%d\n", tr->curr_angle);
	// tr->coord_geo_valid = 0;
	// also set angle/direction to the angle of the line/street


	//dbg(1,"tr->curr_line=%p min=%d\n", tr->curr_line, min);

	if (!tr->curr_line || min > tr->offroad_limit_pref)
	{
		tr->curr_out = tr->curr_in;
		//dbg(0,"curr_out:80 0x%x,0x%x\n", tr->curr_out.x, tr->curr_out.y);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:YYYYYYY:coord_geo_valid=0 002\n");
#endif
		tr->coord_geo_valid = 0;
		tr->street_direction = 0;
	}


#if 1
#ifdef HAVE_API_ANDROID
	// send lanes information to Java code

	if ((tr->curr_item_mark.map) && (tr->curr_item_mark.id_hi != 0))
	{
		int next_lanes_id_hi = 0;
		int next_lanes_id_lo = 0;
		struct map* next_lanes_id_map = NULL;
		int street_dir_next = 1;
		int same_segment = 0;


// -------------- all ways allowed to drive next ----------------------
// -------------- all ways allowed to drive next ----------------------

#ifdef NAVIT_CALC_ALLOWED_NEXT_WAYS

		if ((route_seg_winner_hi > -1) && (route_seg_winner_lo > -1))
		{
			struct route *route2 = NULL;
			struct map *route_map2 = NULL;
			struct map_rect *mr2 = NULL;
			struct item *item2 = NULL;

			route2 = navit_get_route(global_navit);


			if ((route2) && (route2->graph))
			{

				//dbg(0, "RR:03.1:\n");
				struct street_data *sd2 = tr->curr_line->street;
				//dbg(0, "RR:03.2:\n");
				// tr->street_direction;
				// tr->curr_angle;
				// struct route_info *ri = route_get_pos(route2);
				struct route_graph *rg = route2->graph;
				struct route_graph_segment *s = NULL; // i am currently on this segment
				struct route_segment_data *data2 = NULL;
				// s = route_graph_get_segment(rg, sd2, s);
				// struct route_graph_segment *start_next2;
				struct route_graph_segment *start_3; // other segments that i could drive on next (but not on route)
				struct route_graph_segment *drive_here; // i should drive on this segment next
				struct route_graph_segment *next_route_seg; // route segment one should drive next after next
				struct route_graph_point *rgp_next; // next route graph point the we should drive next after next
				int street_dir = 1;


				if ((s = route_graph_get_segment(rg, sd2, s)))
				{

					// if we are on same segment, dont calc it all again!
					if (s != s__prev)
					{
						s__prev = s;

#ifdef HAVE_API_ANDROID
						// length of this segment
						char *seg_len = g_strdup_printf("%d", s->data.len);
						android_send_generic_text(13, seg_len);
						g_free(seg_len);
#endif

						struct route_graph_segment *cur;
						struct route_graph_point *rgp;
						if (tr->street_direction == -1)
						{
							//dbg(0, "RR:03.5:\n");
							rgp = s->start;
							street_dir = -1;
						}
						else // tr->street_direction == 1 or 0
						{
							//dbg(0, "RR:03.6:\n");
							rgp = s->end;
							street_dir = 1;
						}


						//dbg(0, "RR:04.0:\n");
						struct coord_geo coord_geo54;

#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_2
						route_clear_sharp_turn_list();
#endif


						tracking_calc_and_send_possbile_turn_info(rgp, s, street_dir, 0);

						// save the next segment data --------------
						if (rgp->seg)
						{
							next_route_seg = rgp->seg;
							//dbg(0, "RR:04.0a:%p\n", next_route_seg);
							if (next_route_seg->start == rgp)
							{
								rgp_next = next_route_seg->end;
								street_dir_next = 1;
								//dbg(0, "RR:04.2:%p %p %p\n", rgp, next_route_seg->start, next_route_seg->end);
							}
							else
							{
								rgp_next = next_route_seg->start;
								street_dir_next = -1;
								//dbg(0, "RR:04.3:%p %p %p\n", rgp, next_route_seg->start, next_route_seg->end);
							}
							//dbg(0, "RR:04.4:%p\n", rgp_next);

							tracking_calc_and_send_possbile_turn_info(rgp_next, next_route_seg, street_dir_next, 1);

							next_lanes_id_hi = next_route_seg->data.item.id_hi;
							next_lanes_id_lo = next_route_seg->data.item.id_lo;
							next_lanes_id_map = next_route_seg->data.item.map;

							next_lanes_id_hi__prev = next_lanes_id_hi;
							next_lanes_id_lo__prev = next_lanes_id_lo;
							next_lanes_id_map__prev = next_lanes_id_map;
							street_dir_next__prev = street_dir_next;

							// --- and also the next next seg ----
							// --- and also the next next seg ----
							if (rgp_next->seg)
							{
								next_route_seg = rgp_next->seg;
								//dbg(0, "RR:04.0a:%p\n", next_route_seg);
								if (next_route_seg->start == rgp_next)
								{
									rgp_next = next_route_seg->end;
									street_dir_next = 1;
									//dbg(0, "RR:04.2:%p %p %p\n", rgp_next, next_route_seg->start, next_route_seg->end);
								}
								else
								{
									rgp_next = next_route_seg->start;
									street_dir_next = -1;
									//dbg(0, "RR:04.3:%p %p %p\n", rgp_next, next_route_seg->start, next_route_seg->end);
								}
								//dbg(0, "RR:04.4:%p\n", rgp_next);

								tracking_calc_and_send_possbile_turn_info(rgp_next, next_route_seg, street_dir_next, 2);
								street_dir_next__prev = street_dir_next;

							}
							else
							{
#ifdef HAVE_API_ANDROID
								android_send_generic_text((4 + 2), "");
#endif
							}
							// --- and also the next next seg ----
							// --- and also the next next seg ----


						}
						else
						{
#ifdef HAVE_API_ANDROID
							android_send_generic_text((4 + 1), "");
#endif

							next_lanes_id_hi = 0;
							next_lanes_id_lo = 0;
							next_lanes_id_map = NULL;

						}
						// save the next segment data --------------

					}
					else
					{
						next_lanes_id_hi = next_lanes_id_hi__prev;
						next_lanes_id_lo = next_lanes_id_lo__prev;
						next_lanes_id_map = next_lanes_id_map__prev;
						street_dir_next = street_dir_next__prev;

						same_segment = 1;
					}
				}
			}
		}
#endif


// -------------- all way allowed to drive next ----------------------
// -------------- all way allowed to drive next ----------------------

#ifdef NAVIT_CALC_LANES
		if (same_segment == 0)
		{
			struct map_rect *mr_lanes;
			mr_lanes = map_rect_new(tr->curr_item_mark.map, NULL);

			if (mr_lanes)
			{
				tracking_send_lanes_info(mr_lanes, tr->curr_item_mark.id_hi, tr->curr_item_mark.id_lo, tr->street_direction, 0);


				if (next_lanes_id_map != NULL)
				{

					if (next_lanes_id_map != tr->curr_item_mark.map)
					{
						map_rect_destroy(mr_lanes);
						mr_lanes = map_rect_new(next_lanes_id_map, NULL);
						if (mr_lanes)
						{
							tracking_send_lanes_info(mr_lanes, next_lanes_id_hi, next_lanes_id_lo, street_dir_next, 1);
						}
					}
					else
					{
						tracking_send_lanes_info(mr_lanes, next_lanes_id_hi, next_lanes_id_lo, street_dir_next, 1);
					}
				}

				map_rect_destroy(mr_lanes);
			}
		}

#endif


	}


#endif
#endif


	if (tr->curr_line && (tr->curr_line->street->flags & NAVIT_AF_UNDERGROUND))
	{

		// the current position is underground (= tunnel)
#ifdef HAVE_API_ANDROID
		if (global_pos_is_underground == 0)
		{
			android_return_generic_int(7, 1);
		}
#endif
		global_pos_is_underground = 1;

		//dbg(0,"NAVIT_AF_UNDERGROUND 1");
		//if (tr->no_gps)
		//{
		//	tr->tunnel = 1;
		//}
	}
	else
	{
#ifdef HAVE_API_ANDROID
		if (global_pos_is_underground == 1)
		{
			android_return_generic_int(7, 0);
		}
#endif
		global_pos_is_underground = 0;
	}
	//else if (tr->tunnel)
	//{
	//	//dbg(0,"NAVIT_AF_UNDERGROUND 2");
	//	tr->speed = 0;
	//}


	//dbg(0,"curr_IN :98 0x%x,0x%x\n", tr->curr_in.x, tr->curr_in.y);
	//dbg(0,"curr_out:99 0x%x,0x%x\n", tr->curr_out.x, tr->curr_out.y);

	if (too_slow_or_little_movement == 1)
	{
		tr->valid = attr_position_valid_static;
		tr->speed = 0;
	}

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:003 coord_geo_valid=%d\n", tr->coord_geo_valid);
	dbg(0, "ROUTExxPOSxx:routetracking_update: leave x=%d y=%d in.x=%d in.y=%d\n", tr->curr_out.x, tr->curr_out.y, tr->curr_in.x, tr->curr_in.y);
#endif
}

static int tracking_set_attr_do(struct tracking *tr, struct attr *attr, int initial)
{
	switch (attr->type)
	{
		case attr_angle_pref:
			tr->angle_pref = attr->u.num;
			return 1;
		case attr_connected_pref:
			tr->connected_pref = attr->u.num;
			dbg(0, "connected_pref=%d\n", attr->u.num);
			return 1;
		case attr_nostop_pref:
			tr->nostop_pref = attr->u.num;
			return 1;
		case attr_offroad_limit_pref:
			tr->offroad_limit_pref = attr->u.num;
			return 1;
		case attr_route_pref:
			tr->route_pref = attr->u.num;
			return 1;
		case attr_overspeed_pref:
			tr->overspeed_pref = attr->u.num;
			return 1;
		case attr_overspeed_percent_pref:
			tr->overspeed_percent_pref = attr->u.num;
			return 1;
		case attr_tunnel_extrapolation:
			tr->tunnel_extrapolation = attr->u.num;
			return 1;
		default:
			return 0;
	}
}

int tracking_set_attr(struct tracking *tr, struct attr *attr)
{
	return tracking_set_attr_do(tr, attr, 0);
}

struct tracking *
tracking_new(struct attr *parent, struct attr **attrs)
{
	struct tracking *this=g_new0(struct tracking, 1);
	struct attr hist_size;
	this->angle_pref = 4; // not used anymore !!!
	this->connected_pref = 10;
	this->nostop_pref = 4;
	this->offroad_limit_pref = 98000; // was 5000 originally! // find nearst road, even when it is very far away (maybe in some desert, or somewhere with only very few roads!)
	this->route_pref = 300;

	if (!attr_generic_get_attr(attrs, NULL, attr_cdf_histsize, &hist_size, NULL))
	{
		hist_size.u.num = 0;
	}

	if (attrs)
	{
		for (; *attrs; attrs++)
		{
			tracking_set_attr_do(this, *attrs, 1);
		}
	}

	tracking_init_cdf(&this->cdf, hist_size.u.num);

	return this;
}

void tracking_set_mapset(struct tracking *this, struct mapset *ms)
{
	this->ms = ms;
}

void tracking_set_route(struct tracking *this, struct route *rt)
{
	this->rt = rt;
}

void tracking_destroy(struct tracking *tr)
{
	if (tr->attr)
		attr_free(tr->attr);

	tracking_flush(tr);
	g_free(tr);
}

struct map *
tracking_get_map(struct tracking *this_)
{
	struct attr *attrs[5];
	struct attr type, navigation, data, description;
	type.type = attr_type;
	type.u.str = "tracking";
	navigation.type = attr_trackingo;
	navigation.u.tracking = this_;
	data.type = attr_data;
	data.u.str = "";
	description.type = attr_description;
	description.u.str = "Tracking";

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
	struct tracking *tracking;
};

struct map_rect_priv
{
	struct tracking *tracking;
	struct item item;
	struct tracking_line *curr, *next;
	int coord;
	enum attr_type attr_next;
	int ccount;
	int debug_idx;
	char *str;
};

static int tracking_map_item_coord_get(void *priv_data, struct coord *c, int count)
{
	struct map_rect_priv *this = priv_data;
	enum projection pro;
	int ret = 0;
	//dbg(1, "enter\n");
	while (this->ccount < 2 && count > 0)
	{
		pro = map_projection(this->curr->street->item.map);

		if (projection_mg != pro)
		{
			transform_from_to(&this->curr->street->c[this->ccount + this->coord], pro, c, projection_mg);
		}
		else
		{
			*c = this->curr->street->c[this->ccount + this->coord];
		}

		//dbg(1, "coord %d 0x%x,0x%x\n", this->ccount, c->x, c->y);
		this->ccount++;
		ret++;
		c++;
		count--;
	}
	return ret;
}

static int tracking_map_item_attr_get(void *priv_data, enum attr_type attr_type, struct attr *attr)
{
	struct map_rect_priv *this_ = priv_data;
	struct coord lpnt, *c;
	struct tracking *tr = this_->tracking;
	int value;
	attr->type = attr_type;

	if (this_->str)
	{
		g_free(this_->str);
		this_->str = NULL;
	}

	switch (attr_type)
	{
		case attr_debug:
			switch (this_->debug_idx)
			{
#if 0
				case 0:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("overall: %d (limit %d)", tracking_value(tr, this_->curr, this_->coord, &lpnt, INT_MAX / 2, -1), tr->offroad_limit_pref);
					return 1;
				case 1:
					this_->debug_idx++;
					c = &this_->curr->street->c[this_->coord];
					value = tracking_value(tr, this_->curr, this_->coord, &lpnt, INT_MAX / 2, 1);
					this_->str = attr->u.str = g_strdup_printf("distance: (0x%x,0x%x) from (0x%x,0x%x)-(0x%x,0x%x) at (0x%x,0x%x) %d", tr->curr_in.x, tr->curr_in.y, c[0].x, c[0].y, c[1].x, c[1].y, lpnt.x, lpnt.y, value);
					return 1;
				case 2:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("angle: %d to %d (flags %d) %d", tr->curr_angle, this_->curr->angle[this_->coord], this_->curr->street->flags & 3, tracking_value(tr, this_->curr, this_->coord, &lpnt, INT_MAX / 2, 2));
					return 1;
				case 3:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("connected: %d", tracking_value(tr, this_->curr, this_->coord, &lpnt, INT_MAX / 2, 4));
					return 1;
				case 4:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("no_stop: %d", tracking_value(tr, this_->curr, this_->coord, &lpnt, INT_MAX / 2, 8));
					return 1;
				case 5:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("route: %d", tracking_value(tr, this_->curr, this_->coord, &lpnt, INT_MAX / 2, 16));
					return 1;
				case 6:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("overspeed: %d", tracking_value(tr, this_->curr, this_->coord, &lpnt, INT_MAX / 2, 32));
					return 1;
				case 7:
					this_->debug_idx++;
					this_->str = attr->u.str = g_strdup_printf("line %p", this_->curr);
					return 1;
#endif
				default:
					this_->attr_next = attr_none;
					return 0;
			}
		case attr_any:
			while (this_->attr_next != attr_none)
			{
				if (tracking_map_item_attr_get(priv_data, this_->attr_next, attr))
					return 1;
			}
			return 0;
		default:
			attr->type = attr_none;
			return 0;
	}
}

static struct item_methods tracking_map_item_methods = { NULL, tracking_map_item_coord_get, NULL, tracking_map_item_attr_get, };

static void tracking_map_destroy(struct map_priv *priv)
{
	g_free(priv);
}

static void tracking_map_rect_init(struct map_rect_priv *priv)
{
	priv->next = priv->tracking->lines;
	priv->curr = NULL;
	priv->coord = 0;
	priv->item.id_lo = 0;
	priv->item.id_hi = 0;
}

static struct map_rect_priv *
tracking_map_rect_new(struct map_priv *priv, struct map_selection *sel)
{
	struct tracking *tracking = priv->tracking;
	struct map_rect_priv *ret=g_new0(struct map_rect_priv, 1);
	ret->tracking = tracking;
	tracking_map_rect_init(ret);
	ret->item.meth = &tracking_map_item_methods;
	ret->item.priv_data = ret;
	ret->item.type = type_tracking_100;
	return ret;
}

static void tracking_map_rect_destroy(struct map_rect_priv *priv)
{
	g_free(priv);
}

static struct item *
tracking_map_get_item(struct map_rect_priv *priv)
{
	struct item *ret = &priv->item;
	int value;
	struct coord lpnt;

	if (!priv->next)
	{
		return NULL;
	}

	if (!priv->curr || priv->coord + 2 >= priv->curr->street->count)
	{
		priv->curr = priv->next;
		priv->next = priv->curr->next;
		priv->coord = 0;
		priv->item.id_lo = 0;
		priv->item.id_hi++;
	}
	else
	{
		priv->coord++;
		priv->item.id_lo++;
	}

	value = tracking_value(priv->tracking, priv->curr, priv->coord, &lpnt, INT_MAX / 2, -1, NULL);

	if (value < 64)
		priv->item.type = type_tracking_100;
	else if (value < 128)
		priv->item.type = type_tracking_90;
	else if (value < 256)
		priv->item.type = type_tracking_80;
	else if (value < 512)
		priv->item.type = type_tracking_70;
	else if (value < 1024)
		priv->item.type = type_tracking_60;
	else if (value < 2048)
		priv->item.type = type_tracking_50;
	else if (value < 4096)
		priv->item.type = type_tracking_40;
	else if (value < 8192)
		priv->item.type = type_tracking_30;
	else if (value < 16384)
		priv->item.type = type_tracking_20;
	else if (value < 32768)
		priv->item.type = type_tracking_10;
	else
		priv->item.type = type_tracking_0;


	//dbg(1, "item %d %d points\n", priv->coord, priv->curr->street->count);
	priv->ccount = 0;
	priv->attr_next = attr_debug;
	priv->debug_idx = 0;

	return ret;
}

static struct item *
tracking_map_get_item_byid(struct map_rect_priv *priv, int id_hi, int id_lo)
{
	struct item *ret;
	tracking_map_rect_init(priv);
	while ((ret = tracking_map_get_item(priv)))
	{
		if (ret->id_hi == id_hi && ret->id_lo == id_lo)
		{
			return ret;
		}
	}
	return NULL;
}

static struct map_methods tracking_map_meth = { projection_mg, "utf-8", tracking_map_destroy, tracking_map_rect_new, tracking_map_rect_destroy, tracking_map_get_item, tracking_map_get_item_byid, NULL, NULL, NULL, };

struct map_priv *
tracking_map_new(struct map_methods *meth, struct attr **attrs, struct callback_list *cbl)
{
	struct map_priv *ret;
	struct attr *tracking_attr;

	tracking_attr = attr_search(attrs, NULL, attr_trackingo);

	if (!tracking_attr)
	{
		return NULL;
	}

	ret=g_new0(struct map_priv, 1);

	*meth = tracking_map_meth;
	ret->tracking = tracking_attr->u.tracking;

	return ret;
}

void tracking_init(void)
{
#ifdef PLUGSSS
	plugin_register_map_type("tracking", tracking_map_new);
#endif
}

