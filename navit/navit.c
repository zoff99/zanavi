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
 * Copyright (C) 2005-2009 Navit Team
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

#define _USE_MATH_DEFINES 1
#include "config.h"
#ifdef HAVE_UNISTD_H
#include <unistd.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>
#include <fcntl.h>
#include <glib.h>
#include <math.h>
#include <time.h>
#include "debug.h"
#include "navit.h"
#include "callback.h"
#include "gui.h"
#include "item.h"
#include "projection.h"
#include "map.h"
#include "mapset.h"
#include "main.h"
#include "coord.h"
#include "point.h"
#include "transform.h"
#include "param.h"
#include "menu.h"
#include "graphics.h"
#include "popup.h"
#include "data_window.h"
#include "route.h"
#include "navigation.h"
#include "speech.h"
#include "track.h"
#include "vehicle.h"
#include "layout.h"
#include "log.h"
#include "attr.h"
#include "event.h"
#include "file.h"
#include "profile.h"
#include "command.h"
#include "navit_nls.h"
#include "map.h"
#include "util.h"
#include "messages.h"
#include "vehicleprofile.h"
#include "sunriset.h"
#include "bookmarks.h"
#include "map.h"
#ifdef HAVE_API_WIN32_BASE
#include <windows.h>
#include "util.h"
#endif
#ifdef HAVE_API_WIN32_CE
#include "libc.h"
#endif













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






/**
 * @defgroup navit the navit core instance. navit is the object containing nearly everything: A set of maps, one or more vehicle, a graphics object for rendering the map, a gui object for displaying the user interface, a route object, a navigation object and so on. Be warned that it is theoretically possible to have more than one navit object
 * @{
 */

struct gui *main_loop_gui;

struct attr_iter
{
	union
	{
		GList *list;
		struct mapset_handle *mapset_handle;
	} u;
};

static int dist_to_street = 100000;

static void navit_vehicle_update(struct navit *this_, struct navit_vehicle *nv);
static void navit_vehicle_draw(struct navit *this_, struct navit_vehicle *nv, struct point *pnt);
static int navit_set_attr_do(struct navit *this_, struct attr *attr, int init);
static int navit_get_cursor_pnt(struct navit *this_, struct point *p, int keep_orientation, int *dir);
static void navit_cmd_zoom_to_route(struct navit *this);
static void navit_cmd_set_center_cursor(struct navit *this_);
static void navit_cmd_announcer_toggle(struct navit *this_);
static void navit_set_vehicle(struct navit *this_, struct navit_vehicle *nv);

int allow_gui_internal = 0; // disable old GUI internal. it will not work anymore!
int routing_mode = 0;
int MYSTERY_SPEED = 2;
int offline_search_filter_duplicates = 0;
int offline_search_break_searching = 0;
char *navit_maps_dir;
char *navit_share_dir;
char *navit_data_dir;
int cancel_drawing_global = 0;
int global_speak_streetnames = 1;
int allow_large_mapfiles = 1; // allow the use of large (>2GB) mapfiles // -> value unused for now
int cache_size_file = 1024 * 1024 * 10; // default value was: 20971520 (~20 MB)
int draw_polylines_fast = 0; // default: 0
int limit_order_corrected = 4; // remain at this order level for drawing streets etc.
int shift_order = 0; // shift order level (for displaying objects) by this values (should only be bigger than zero!!)
int global_search_street_size_factor = 1; // make search radius for streets bigger (not used on indexsearch)
int disable_map_drawing = 0; // dont draw the map and dont read data from file (hopefully saving resources)
int hold_drawing = 0; // 0 -> draw normal , 1 -> dont do any drawing
int global_stop_demo_vehicle = 0; // 0 -> demo vehicle can move, 1 -> demo vehicle stands still
int global_show_route_rectangles = 0; // 1 -> show route rectangles, 0 -> dont show route rectangles
int global_traffic_light_delay = 0; // 0 -> dont account for traffic lights in route, >0 -> calc a delay for each traffic light
int global_clinedrawing_active = 0; // 0 -> java line drawing, 1 -> C line drawing
int global_draw_multipolygons = 1; // 0 -> dont draw lines and triangles from multipolygons, 1 -> draw them
int global_have_dpi_value = 240;
float global_dpi_factor = 1.0f;
int global_order_level_for_fast_draw = 13;
int global_show_english_labels = 1; // 0 -> only "normal" names/labels shown on map
									// 1 -> show "normal, english"
									// 2 -> show only "english" labels
int global_routing_engine = 0; // 0 -> offline ZANavi, 1 -> online OSRM
float global_overspill_factor = 1.0f; // overspill factor from Java code
int global_avoid_sharp_turns_flag = 0; // 0 -> normal routing, 1 -> try to avoid sharp turns / u-turns
int global_avoid_sharp_turns_min_angle = 40; // at what angle is it a sharp turn?
int global_avoid_sharp_turns_min_penalty = 1000; // routing penalty for sharp turns (DEFAULT = 1000)

int global_search_radius_for_housenumbers = 300; // search this far around street-coord to find potential housenumbers for this street
int global_vehicle_profile = 0; // 0 -> car, 1 -> bicycle, 2 -> bicylce no one-ways
int global_cycle_lanes_prio = 5; // how much prio weight will be subtracted from prio weight if a road has a cycle lane (painted white line for bicycles)
int global_cycle_track_prio = 1; // unused for now!!

double global_v_pos_lat = 0.0; // global vehicle position
double global_v_pos_lng = 0.0; // global vehicle position
double global_v_pos_dir = 0.0; // global vehicle direction

struct coord global_vehicle_pos_onscreen;
struct coord_geo global_last_vehicle_pos_geo;
double ggggg_lat = 0;
double ggggg_lon = 0;


int global_demo_vehicle = 0;
int global_demo_vehicle_short_switch = 0;
long global_last_spoken = -1;
long global_last_spoken_base = 0;
float global_road_speed_factor = 0.85f;

float global_level0_announcement = 5.0f;
float global_level1_announcement = 11.0f;
float global_level2_announcement = 24.3f;
float global_levelx_announcement_factor = 6.0f / 4.0f;

float global_b_level0_announcement = 4.8f;
float global_b_level1_announcement = 11.1f;
float global_b_level2_announcement = 21.1f;
float global_b_levelx_announcement_factor = 6.0f / 4.0f;

int global_driven_away_from_route = 0;

int global_enhance_cycleway = 0;
int global_tracking_show_real_gps_pos = 0;
int global_show_maps_debug_view = 0;
int global_cancel_preview_map_drawing = 0;

GList *global_all_cbs = NULL;

struct coord global_debug_route_seg_winner_start;
struct coord global_debug_route_seg_winner_end;
struct coord global_debug_seg_winner_start;
struct coord global_debug_seg_winner_end;
struct coord global_debug_route_seg_winner_p_start;
struct coord global_debug_route_seg_winner_p_end;
struct coord global_debug_seg_winner_p_start;
struct coord global_debug_seg_route_start;
struct coord global_debug_seg_route_end;
struct coord global_debug_trlast_start;
struct coord global_debug_trlast_end;

struct coord *global_debug_coord_list;
int global_debug_coord_list_items = 0;
int global_has_gpsfix = 0;
int global_pos_is_underground = 0;


int global_sharp_turn_list_count = 0;
struct global_sharp_turn *global_sharp_turn_list = NULL;

int global_freetext_list_count = 0;
struct global_freetext *global_freetext_list = NULL;


GHashTable *global_transform_hash = NULL;
GHashTable *global_transform_hash2 = NULL;

long long draw_lines_count_2 = 0;
long long draw_lines_count_3 = 0;
long long draw_lines_count_4 = 0;
int poi_on_map_count = 0;
int label_on_map_count = 0;
int label_district_on_map_count = 0;
int label_major_on_map_count = 0;
int poi_icon_on_map_count = 0;

int mapdraw_time[11 + 5]; // time to draw map on screen (in 1/1000 of a second) [add 5, just in case we inc it 2 times at same time because of threads]
int cur_mapdraw_time_index = 0;

int route_status_previous = 0;
long long global_route_memory_size = 0;
int global_old_vehicle_speed = -1;
int global_old_vehicle_speed_for_autozoom = -1;

void navit_add_mapset(struct navit *this_, struct mapset *ms)
{
	this_->mapsets = g_list_append(this_->mapsets, ms);
}

struct mapset *
navit_get_mapset(struct navit *this_)
{
	if (this_->mapsets)
	{
		return this_->mapsets->data;
	}
	else
	{
		//DBG dbg(0,"No mapsets enabled! Is it on purpose? Navit can't draw a map. Please check your navit.xml\n");
	}
	return NULL;
}

struct tracking *
navit_get_tracking(struct navit *this_)
{
	return this_->tracking;
}

/**
 * @brief	Get the user data directory.
 * @param[in]	 create	- create the directory if it does not exist
 *
 * @return	char * to the data directory string.
 *
 * returns the directory used to store user data files (center.txt,
 * destination.txt, bookmark.txt, ...)
 *
 */
char*
navit_get_user_data_directory(int create)
{
	char *dir;
	// dir = getenv("NAVIT_USER_DATADIR");
	dir = navit_share_dir;
	if (create && !file_exists(dir))
	{
		//DBG dbg(0, "creating dir %s\n", dir);
		if (file_mkdir(dir, 0))
		{
			//DBG dbg(0, "failed creating dir %s\n", dir);
			return NULL;
		}
	}
	return dir;
} /* end: navit_get_user_data_directory(gboolean create) */

void navit_draw_async(struct navit *this_, int async)
{
__F_START__

	//dbg(0,"EEnter this_->blocked=%d\n",this_->blocked);
	if (this_->blocked)
	{
		this_->blocked |= 2;
		//dbg(0,"set this_->blocked=%d\n",this_->blocked);
		// dbg(0,"DO__DRAW:ndasync return 001\n");
		return2;
	}

	transform_setup_source_rect(this_->trans);
	//dbg(0,"call graphics_draw\n");

	// dbg(0,"DO__DRAW:gras_draw call\n");
	graphics_draw(this_->gra, this_->displaylist, this_->mapsets->data, this_->trans, this_->layout_current, async, NULL, this_->graphics_flags | 1);
	// dbg(0,"DO__DRAW:ndasync leave\n");

__F_END__
}

void navit_draw(struct navit *this_)
{
__F_START__

	//dbg(0,"EEnter this_->ready=%d\n",this_->ready);
	if (this_->ready == 3)
	{
		// dbg(0,"navit_draw_async_001\n");
		// dbg(0,"DO__DRAW:navit_draw_async call\n");
		navit_draw_async(this_, 0);
	}

__F_END__
}

int navit_get_ready(struct navit *this_)
{
	return this_->ready;
}

// UNUSED -----
// UNUSED -----
void navit_draw_displaylist(struct navit *this_)
{
	if (this_->ready == 3)
	{
		// //DBG dbg(0,"call graphics_displaylist_draw 2")
		graphics_displaylist_draw(this_->gra, this_->displaylist, this_->trans, this_->layout_current, this_->graphics_flags | 1);
	}
}

static void navit_map_progress(struct navit *this_)
{
	struct map *map;
	struct mapset *ms;
	struct mapset_handle *msh;
	struct attr attr;
	struct point p;
	if (this_->ready != 3)
		return;
	p.x = 10;
	p.y = 32;

	ms = this_->mapsets->data;
	msh = mapset_open(ms);
	while (msh && (map = mapset_next(msh, 0)))
	{
		if (map_get_attr(map, attr_progress, &attr, NULL))
		{
			char *str = g_strdup_printf("%s           ", attr.u.str);
			graphics_draw_mode(this_->gra, draw_mode_begin);
			graphics_draw_text_std(this_->gra, 16, str, &p);
			g_free(str);
			p.y += 32;
			graphics_draw_mode(this_->gra, draw_mode_end);
		}
	}
	mapset_close(msh);
}

static void navit_redraw_route(struct navit *this_, struct route *route, struct attr *attr)
{
__F_START__

	int updated;

	if ((this_->route) && (this_->route->route_status_was_updated == 1))
	{
		this_->route->route_status_was_updated = 0;
		// send route_status to java
#ifdef HAVE_API_ANDROID
		android_return_generic_int(1, this_->route->route_status);
#endif
	}

	if (attr->type != attr_route_status)
	{
		return2;
	}

	updated = attr->u.num;

	if (this_->ready != 3)
	{
		return2;
	}

	if (updated != route_status_path_done_new)
	{
		return2;
	}

	if (this_->vehicle)
	{
		if (this_->vehicle->follow_curr == 1)
		{
			////DBG dbg(0,"disabled -> we want redraw!!\n");
			// return2;
		}

		if (this_->vehicle->follow_curr <= this_->vehicle->follow)
		{
			this_->vehicle->follow_curr = this_->vehicle->follow;
		}
	}

	// *++*-- DISABLED --*++* // navit_draw(this_);

__F_END__
}

void navit_handle_resize(struct navit *this_, int w, int h)
{
__F_START__

	struct map_selection sel;

	int callback = (this_->ready == 1);
	this_->ready = this_->ready | 2;

	memset(&sel, 0, sizeof(sel));

	this_->w = w;
	this_->h = h;
	sel.u.p_rect.rl.x = w;
	sel.u.p_rect.rl.y = h;
	transform_set_screen_selection(this_->trans, &sel);
	graphics_init(this_->gra);
	graphics_set_rect(this_->gra, &sel.u.p_rect);

	if (callback)
	{
		// HINT: this triggers all the OSD drawing (next turn, ETA, etc.)
		callback_list_call_attr_1(this_->attr_cbl, attr_graphics_ready, this_);
	}

	if (this_->ready == 3)
	{
		// dbg(0,"navit_draw_async_007\n");
		navit_draw_async(this_, 0);
	}

__F_END__
}

void navit_resize(void *data, int w, int h)
{
	struct navit *this = data;
	if (!this->ignore_graphics_events)
	{
		//DBG dbg(0,"11\n");
		navit_handle_resize(this, w, h);
	}
}

int navit_get_width(struct navit *this_)
{
	return this_->w;
}

int navit_get_height(struct navit *this_)
{
	return this_->h;
}

static void navit_popup(void *data)
{
	struct navit *this_ = data;
	popup(this_, 1, &this_->pressed);
	this_->button_timeout = NULL;
	this_->popped = 1;
}

int navit_ignore_button(struct navit *this_)
{
	if (this_->ignore_button)
		return 1;

	this_->ignore_button = 1;
	return 0;
}

void navit_ignore_graphics_events(struct navit *this_, int ignore)
{
	this_->ignore_graphics_events = ignore;
}

void update_transformation(struct transformation *tr, struct point *old, struct point *new, struct point *rot)
{
	struct coord co, cn;
	struct coord c, *cp;
	int yaw;
	double angleo, anglen;

	if (!transform_reverse(tr, old, &co))
		return;
	if (rot)
	{
		angleo = atan2(old->y - rot->y, old->x - rot->x) * 180 / M_PI;
		anglen = atan2(new->y - rot->y, new->x - rot->x) * 180 / M_PI;
		yaw = transform_get_yaw(tr) + angleo - anglen;
		transform_set_yaw(tr, yaw % 360);
	}
	if (!transform_reverse(tr, new, &cn))
		return;
	cp = transform_get_center(tr);
	c.x = cp->x + co.x - cn.x;
	c.y = cp->y + co.y - cn.y;
	// dbg(1, "from 0x%x,0x%x to 0x%x,0x%x\n", cp->x, cp->y, c.x, c.y);
	transform_set_center(tr, &c);
}

void navit_set_timeout(struct navit *this_)
{
	// --------- DISABLE -----------
	return;
	// --------- DISABLE -----------
}

int navit_handle_button(struct navit *this_, int pressed, int button, struct point *p, struct callback *popup_callback)
{
	int border = 16;

	// dbg(1, "enter %d %d (ignore %d)\n", pressed, button, this_->ignore_button);
	callback_list_call_attr_4(this_->attr_cbl, attr_button, this_, GINT_TO_POINTER(pressed), GINT_TO_POINTER(button), p);
	if (this_->ignore_button)
	{
		this_->ignore_button = 0;
		return 0;
	}
	if (pressed)
	{
		this_->pressed = *p;
		this_->last = *p;
		this_->zoomed = 0;
		if (button == 1)
		{
			this_->button_pressed = 1;
			this_->moved = 0;
			this_->popped = 0;
			// ---- DISBALED --------
			/*
			 if (popup_callback)
			 this_->button_timeout = event_add_timeout(500, 0, popup_callback);
			 */
		}
		if (button == 2)
			navit_set_center_screen(this_, p, 1);
		if (button == 3)
			popup(this_, button, p);
		if (button == 4 && this_->use_mousewheel)
		{
			this_->zoomed = 1;
			navit_zoom_in(this_, 2, p);
		}
		if (button == 5 && this_->use_mousewheel)
		{
			this_->zoomed = 1;
			navit_zoom_out(this_, 2, p);
		}
	}
	else
	{

		this_->button_pressed = 0;
		if (this_->button_timeout)
		{
			event_remove_timeout(this_->button_timeout);
			this_->button_timeout = NULL;
			if (!this_->moved && !transform_within_border(this_->trans, p, border))
			{
				navit_set_center_screen(this_, p, !this_->zoomed);
			}
		}
		if (this_->motion_timeout)
		{
			event_remove_timeout(this_->motion_timeout);
			this_->motion_timeout = NULL;
		}
		if (this_->moved)
		{
			struct point pr;
			pr.x = this_->w / 2;
			pr.y = this_->h;
#if 0
			update_transformation(this_->trans, &this_->pressed, p, &pr);
#else
			update_transformation(this_->trans, &this_->pressed, p, NULL);
#endif
			graphics_draw_drag(this_->gra, NULL);
			transform_copy(this_->trans, this_->trans_cursor);
			graphics_overlay_disable(this_->gra, 0);
			if (!this_->zoomed)
			{
				navit_set_timeout(this_);
			}
			navit_draw(this_);
		}
		else
			return 1;
	}
	return 0;
}

static void navit_button(void *data, int pressed, int button, struct point *p)
{
	struct navit *this = data;
	// dbg(1, "enter %d %d ignore %d\n", pressed, button, this->ignore_graphics_events);
	if (!this->ignore_graphics_events)
	{
		if (!this->popup_callback)
		{
			this->popup_callback = callback_new_1(callback_cast(navit_popup), this);
			callback_add_names(this->popup_callback, "navit_button", "navit_popup");
		}
		navit_handle_button(this, pressed, button, p, this->popup_callback);
	}
}

// UNUSED ---
// UNUSED ---
static void navit_motion_timeout(struct navit *this_)
{
	int dx, dy;

	if (this_->drag_bitmap)
	{
		struct point point;
		point.x = (this_->current.x - this_->pressed.x);
		point.y = (this_->current.y - this_->pressed.y);
		if (graphics_draw_drag(this_->gra, &point))
		{
			graphics_overlay_disable(this_->gra, 1);
			graphics_draw_mode(this_->gra, draw_mode_end);
			this_->moved = 1;
			this_->motion_timeout = NULL;
			return;
		}
	}
	dx = (this_->current.x - this_->last.x);
	dy = (this_->current.y - this_->last.y);
	if (dx || dy)
	{
		struct transformation *tr;
		struct point pr;
		this_->last = this_->current;
		graphics_overlay_disable(this_->gra, 1);
		tr = transform_dup(this_->trans);
		pr.x = this_->w / 2;
		pr.y = this_->h;
#if 0
		update_transformation(tr, &this_->pressed, &this_->current, &pr);
#else
		update_transformation(tr, &this_->pressed, &this_->current, NULL);
#endif
#if 0
		graphics_displaylist_move(this_->displaylist, dx, dy);
#endif
		graphics_draw_cancel(this_->gra, this_->displaylist); // --> calls "do_draw" normally
		graphics_displaylist_draw(this_->gra, this_->displaylist, tr, this_->layout_current, this_->graphics_flags);

		transform_destroy(tr);
		this_->moved = 1;
	}
	this_->motion_timeout = NULL;
	return;
}

void navit_handle_motion(struct navit *this_, struct point *p)
{
__F_START__

	int dx, dy;

	if (this_->button_pressed && !this_->popped)
	{
		dx = (p->x - this_->pressed.x);
		dy = (p->y - this_->pressed.y);
		if (dx < -8 || dx > 8 || dy < -8 || dy > 8)
		{
			this_->moved = 1;
			if (this_->button_timeout)
			{
				event_remove_timeout(this_->button_timeout);
				this_->button_timeout = NULL;
			}
			this_->current = *p;

			// -------- DISABLE -------
			// -------- DISABLE -------
			// -------- DISABLE -------
			/*
			 if (!this_->motion_timeout_callback)
			 this_->motion_timeout_callback = callback_new_1(callback_cast(navit_motion_timeout), this_);
			 if (!this_->motion_timeout)
			 this_->motion_timeout = event_add_timeout(100, 0, this_->motion_timeout_callback);
			 */
			// -------- DISABLE -------
			// -------- DISABLE -------
			// -------- DISABLE -------
		}
	}

__F_END__
}

void navit_motion(void *data, struct point *p)
{
	struct navit *this = data;
	if (!this->ignore_graphics_events)
	{
		navit_handle_motion(this, p);
	}
}

void navit_predraw(struct navit *this_)
{
__F_START__

	GList *l;
	struct navit_vehicle *nv;
	transform_copy(this_->trans, this_->trans_cursor);
	l = this_->vehicles;
	while (l)
	{
		nv = l->data;
		////DBG dbg(0,"* here *\n");
		////DBG dbg(0,"vehicle_draw_004\n");
		navit_vehicle_draw(this_, nv, NULL);
		l = g_list_next(l);
	}

__F_END__
}

static void navit_scale(struct navit *this_, long scale, struct point *p, int draw)
{
	struct coord c1, c2, *center;

	if (scale < this_->zoom_min)
	{
		scale = this_->zoom_min;
	}

	if (scale > this_->zoom_max)
	{
		scale = this_->zoom_max;
	}

	// return scale value to android
#ifdef HAVE_API_ANDROID
	android_return_generic_int(3, (int)scale);
#endif

	//dbg(0, "zoom to scale=%d", (int)scale);


	if (p)
	{
		transform_reverse(this_->trans, p, &c1);
	}

	transform_set_scale(this_->trans, scale);

	if (p)
	{
		transform_reverse(this_->trans, p, &c2);
		center = transform_center(this_->trans);
		center->x += c1.x - c2.x;
		center->y += c1.y - c2.y;
	}

	//DBG dbg(0,"aa331\n");

	if (draw)
	{
		navit_draw(this_);
	}

	//DBG dbg(0,"leave\n");
}

/**
 * @brief Automatically adjusts zoom level
 *
 * This function automatically adjusts the current
 * zoom level according to the current speed.
 *
 * @param this_ The navit struct
 * @param center The "immovable" point - i.e. the vehicles position if we're centering on the vehicle
 * @param speed The vehicles speed in meters per second
 * @param dir The direction into which the vehicle moves
 */
static long navit_autozoom(struct navit *this_, struct coord *center, int speed, int draw, int *lold, int *lnew)
{
	struct point pc;
	int w, h;
	float distance;
	long new_scale;
	long scale;

	if (!this_->autozoom_active)
	{
		return -1;
	}

	if (global_old_vehicle_speed < 1)
	{
		return -1;
	}

	// --- no autozoom at slow speed ---
	//if (speed < 20)
	//{
	//	return -1;
	//}
	// --- no autozoom at slow speed ---



// this is kaputt!!
#if 0
	if (global_old_vehicle_speed_for_autozoom > 0)
	{
		if (abs(global_old_vehicle_speed_for_autozoom - speed) < 10)
		{
			// change in speed not significant
			return;
		}
	}
	global_old_vehicle_speed_for_autozoom = speed;
#endif
// this is kaputt!!



	// distance = speed * this_->autozoom_secs;
	if (speed > 109)
	{
		distance = (float)speed * 16.4f;
	}
	else if (speed > 75)
	{
		distance = (float)speed * 10.3f;
	}
	else
	{
		distance = (float)speed * 7.3f;
	}

	// dbg(0,"autozoom:   dis1=%f\n", distance);

	// if overspill > 1 ?
	if (global_overspill_factor > 1.0f)
	{
		distance = distance * global_overspill_factor;
	}


	// scale = this_->trans->scale * 16;
	scale = transform_get_scale(this_->trans);

	transform_get_size(this_->trans, &w, &h);
	transform(this_->trans, transform_get_projection(this_->trans), center, &pc, 1, 0, 0, NULL);

#if 0
	// dbg(0,"autozoom:ovrspll=%f\n", global_overspill_factor);
	dbg(0,"autozoom:   dist=%f\n", distance);
	dbg(0,"autozoom:  scale=%d\n", (int)scale);
	dbg(0,"autozoom:o speed=%d\n", speed);
	dbg(0,"autozoom:n speed=%d\n", global_old_vehicle_speed);
#endif

	/* We make sure that the point we want to see is within a certain range
	 * around the vehicle. The radius of this circle is the size of the
	 * screen. This doesn't necessarily mean the point is visible because of
	 * perspective etc. Quite rough, but should be enough. */

	if (w > h)
	{
		new_scale = (long)( (distance / (float)h) * 16);
	}
	else
	{
		new_scale = (long)( (distance / (float)w) * 16);
	}

	if (new_scale < this_->autozoom_min)
	{
		new_scale = this_->autozoom_min;
	}

#if 0
	dbg(0,"autozoom:w n.scale=%d o.scale=%d\n", (int)new_scale, (int)scale);
#endif

	//if (abs(new_scale - scale) < 2)
	//{
	//	return; // Smoothing
	//}

	if (new_scale > scale)
	{
		// zoom out
		if (new_scale > (scale + 20))
		{
			scale = scale + 10;
		}
		else if (new_scale > (scale + 5))
		{
			scale = scale + 4;
		}
		else
		{
			scale = scale + 1;
		}
	}
	else if (new_scale < scale)
	{
		// zoom in
		if ((new_scale + 220500) < scale) // lower threshold
		{
			scale = (int)((float)scale * 0.85f);
			// dbg(0,"autozoom:step 8\n");
		}
		else if ((new_scale + 130500) < scale) // lower threshold
		{
			scale = (int)((float)scale * 0.85f);
			// dbg(0,"autozoom:step 7\n");
		}
		else if ((new_scale + 4000) < scale) // lower threshold
		{
			scale = (int)((float)scale * 0.85f);
			// dbg(0,"autozoom:step 6\n");
		}
		else if ((new_scale + 1850) < scale) // lower threshold
		{
			// scale = scale - 1000;
			scale = (int)((float)scale * 0.85f);
			// dbg(0,"autozoom:step 5\n");
		}
		else if ((new_scale + 90) < scale) // lower threshold
		{
			// scale = scale - 200;
			scale = (int)((float)scale * 0.85f);
			// dbg(0,"autozoom:step 4\n");
		}
		else if ((new_scale + 25) < scale) // lower threshold
		{
			scale = scale - 8;
			// dbg(0,"autozoom:step 3\n");
		}
		else if ((new_scale + 7) < scale) // lower threshold
		{
			scale = scale - 2;
			// dbg(0,"autozoom:step 2\n");
		}
		else
		{
			scale = scale - 1;
			// dbg(0,"autozoom:step 1\n");
		}
	}
	else
	{
		// no change
		return -1;
	}

	// dbg(0,"autozoom:n scale=%d\n", (int)scale);

	// OLD zoom is applied here -------------------------
	struct coord c_left;
	struct point p_left;
	p_left.x = 0;
	p_left.y = 200;
	transform_reverse(this_->trans, &p_left, &c_left);
	struct coord c_right;
	struct point p_right;
	p_right.x = 200;
	p_right.y = 200;
	transform_reverse(this_->trans, &p_right, &c_right);
	// OLD zoom is applied here -------------------------


	if (scale >= this_->autozoom_min)
	{
		navit_scale(this_, (long) scale, &pc, 0);
	}
	else
	{
		//if (scale != this_->autozoom_min)
		//{
			navit_scale(this_, this_->autozoom_min, &pc, 0);
		//}
	}

	// new zoom is applied here already -----------------
	struct point p_new_left;
	transform(global_navit->trans, transform_get_projection(this_->trans), &c_left, &p_new_left, 1, 0, 0, NULL);
	struct point p_new_right;
	transform(global_navit->trans, transform_get_projection(this_->trans), &c_right, &p_new_right, 1, 0, 0, NULL);

	*lold = 200;
	*lnew = abs(p_new_right.x - p_new_left.x);
	// new zoom is applied here already -----------------


	// return new scale value
	return scale;
}

/**
 * Change the current zoom level, zooming closer to the ground
 *
 * @param navit The navit instance
 * @param factor The zoom factor, usually 2
 * @param p The invariant point (if set to NULL, default to center)
 * @returns nothing
 */
void navit_zoom_in(struct navit *this_, int factor, struct point *p)
{
	long scale = transform_get_scale(this_->trans) / factor;
	if (scale < 1)
	{
		scale = 1;
	}
	////DBG dbg(0,"zoom in -> scale=%d",scale);
	navit_scale(this_, scale, p, 1);
}

/**
 * Change the current zoom level
 *
 * @param navit The navit instance
 * @param factor The zoom factor, usually 2
 * @param p The invariant point (if set to NULL, default to center)
 * @returns nothing
 */
void navit_zoom_out(struct navit *this_, int factor, struct point *p)
{
	long scale = transform_get_scale(this_->trans) * factor;
	////DBG dbg(0,"zoom out -> scale=%d",scale);
	navit_scale(this_, scale, p, 1);
}

int navit_get_cur_pnt(struct navit *this_, struct point *p)
{
	return navit_get_cursor_pnt(this_, p, 0, NULL);
}

void navit_zoom_in_cursor(struct navit *this_, int factor)
{
	struct point p;
	if (this_->vehicle && this_->vehicle->follow_curr <= 1 && navit_get_cursor_pnt(this_, &p, 0, NULL))
	{
		navit_zoom_in(this_, factor, &p);
		this_->vehicle->follow_curr = this_->vehicle->follow;
	}
	else
	{
		navit_zoom_in(this_, factor, NULL);
	}
}

void navit_zoom_to_scale_no_draw(struct navit *this_, int new_scale)
{
	long scale = transform_get_scale(this_->trans);
	long new_scale_long = new_scale;

	// only do something if scale changed!
	if (scale != new_scale_long)
	{
		navit_scale(this_, new_scale_long, NULL, 0);
	}
}


void navit_zoom_to_scale(struct navit *this_, int new_scale)
{
	long scale = transform_get_scale(this_->trans);
	long new_scale_long = new_scale;
	//DBG dbg(0,"zoom to scale -> old scale=%d",scale);
	//DBG dbg(0,"zoom to scale -> want scale=%d",new_scale_long);

	// only do something if scale changed!
	if (scale != new_scale_long)
	{
		navit_scale(this_, new_scale_long, NULL, 1);
	}
}

void navit_zoom_to_scale_with_center_point(struct navit *this_, int new_scale, struct point *p)
{
	long scale = transform_get_scale(this_->trans);
	long new_scale_long = new_scale;

	//dbg(0, "zoom to scale -> old scale=%d", scale);
	//dbg(0, "zoom to scale -> want scale=%d", new_scale_long);

	// only do something if scale changed!
	if (scale != new_scale_long)
	{
		navit_scale(this_, new_scale_long, p, 1);
	}
}

void navit_zoom_out_cursor(struct navit *this_, int factor)
{
	struct point p;
	if (this_->vehicle && this_->vehicle->follow_curr <= 1 && navit_get_cursor_pnt(this_, &p, 0, NULL))
	{
		navit_zoom_out(this_, 2, &p);
		this_->vehicle->follow_curr = this_->vehicle->follow;
	}
	else
	{
		navit_zoom_out(this_, 2, NULL);
	}
}

static int navit_cmd_zoom_in(struct navit *this_)
{
	navit_zoom_in_cursor(this_, 2);
	return 0;
}

static int navit_cmd_zoom_out(struct navit *this_)
{
	navit_zoom_out_cursor(this_, 2);
	return 0;
}

static void navit_cmd_say(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	if (in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str)
	{
		navit_say(this, in[0]->u.str);
	}
}

static GHashTable *cmd_int_var_hash = NULL;
static GHashTable *cmd_attr_var_hash = NULL;

/**
 * Store key value pair for the  command system (for int typed values)
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attributes in[0] is the key string, in[1] is the integer value to store
 * @param out output attributes, unused 
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_set_int_var(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	char*key;
	struct attr*val;
	if (!cmd_int_var_hash)
	{
		cmd_int_var_hash = g_hash_table_new(g_str_hash, g_str_equal);
	}

	if ((in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str) && (in && in[1] && ATTR_IS_NUMERIC(in[1]->type)))
	{
		val = g_new(struct attr,1);
		attr_dup_content(in[1], val);
		key = g_strdup(in[0]->u.str);
		g_hash_table_insert(cmd_int_var_hash, key, val);
	}
}

/**
 * Store key value pair for the  command system (for attr typed values, can be used as opaque handles)
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attributes in[0] is the key string, in[1] is the attr* value to store
 * @param out output attributes, unused 
 * @param valid unused 
 * @returns nothing
 */
//TODO free stored attributes on navit_destroy
static void navit_cmd_set_attr_var(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	char*key;
	struct attr*val;
	if (!cmd_attr_var_hash)
	{
		cmd_attr_var_hash = g_hash_table_new(g_str_hash, g_str_equal);
	}

	if ((in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str) && (in && in[1]))
	{
		val = attr_dup(in[1]);
		//val = in[1];
		key = g_strdup(in[0]->u.str);
		g_hash_table_insert(cmd_attr_var_hash, key, val);
	}
}

/**
 * command to toggle the active state of a named layer of the current layout
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attribute in[0] is the name of the layer
 * @param out output unused
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_toggle_layer(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	if (in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str)
	{
		if (this->layout_current && this->layout_current->layers)
		{
			GList* layers = this->layout_current->layers;
			while (layers)
			{
				struct layer*l = layers->data;
				if (l && !strcmp(l->name, in[0]->u.str))
				{
					l->active ^= 1;
					navit_draw(this);
					return;
				}
				layers = g_list_next(layers);
			}
		}
	}
}


void navit_enhance_cycleway(struct navit *this)
{
	GList *itms;
	struct itemgra *itm;
	struct element *e;
	GList *es, *types;
	int found = 0;
	GList* layers = this->layout_current->layers;

	global_enhance_cycleway = 1;

	while (layers)
	{

		struct layer*l = layers->data;
		if (l)
		{

			itms = l->itemgras;
			while (itms)
			{
				itm = itms->data;
				found = 0;

				types = itm->type;
				while (types)
				{
					if (GPOINTER_TO_INT(types->data) == type_cycleway)
					{
						found = 1;
					}
					types = g_list_next(types);
				}

				if (found == 1)
				{

					dbg(0, "CYC:001:min=%d max=%d\n", itm->order.min, itm->order.max);

					if (itm->order.min == 14)
					{
						itm->order.min = 10;
					}

					es = itm->elements;
					while (es)
					{
						e = es->data;

						if (e->type == element_polyline)
						{
							e->u.polyline.width = e->u.polyline.width * 2;
						}

						es = g_list_next(es);
					}
				}

				itms = g_list_next(itms);
			}

		}
		layers = g_list_next(layers);

	}

}


void navit_reset_cycleway(struct navit *this)
{
	GList *itms;
	struct itemgra *itm;
	struct element *e;
	GList *es, *types;
	int found = 0;
	GList* layers = this->layout_current->layers;

	global_enhance_cycleway = 0;

	while (layers)
	{

		struct layer*l = layers->data;
		if (l)
		{

			itms = l->itemgras;
			while (itms)
			{
				itm = itms->data;
				found = 0;

				types = itm->type;
				while (types)
				{
					if (GPOINTER_TO_INT(types->data) == type_cycleway)
					{
						found = 1;
					}
					types = g_list_next(types);
				}

				if (found == 1)
				{
					if (itm->order.min == 10)
					{
						itm->order.min = 14;
					}

					es = itm->elements;
					while (es)
					{
						e = es->data;

						if (e->type == element_polyline)
						{
							e->u.polyline.width = e->u.polyline.width / 2;
						}

						es = g_list_next(es);
					}
				}

				itms = g_list_next(itms);
			}

		}
		layers = g_list_next(layers);

	}

}


void navit_layer_toggle_active(struct navit *this, char *name, int draw)
{
	if (name)
	{
		if (this->layout_current && this->layout_current->layers)
		{
			GList* layers = this->layout_current->layers;
			while (layers)
			{
				struct layer *l = layers->data;
				if (l && !strcmp(l->name, name))
				{
					l->active ^= 1;
					if (draw == 1)
					{
						navit_draw(this);
					}
					return;
				}
				layers = g_list_next(layers);
			}
		}
	}
}



/**
 * command to set the active state of a named layer of the current layout
 *
 * @param navit		The navit instance
 * @param name		name of the layer
 * @param active	0 -> inactive, 1 -> active
 * @param draw		0 -> dont redraw, 1 -> redraw
 * @returns			nothing
 */
void navit_layer_set_active(struct navit *this, char *name, int active, int draw)
{
	if (name)
	{
		if (this->layout_current && this->layout_current->layers)
		{
			GList* layers = this->layout_current->layers;
			while (layers)
			{
				struct layer *l = layers->data;
				if (l && !strcmp(l->name, name))
				{
					l->active = active;
					if (draw == 1)
					{
						navit_draw(this);
					}
					return;
				}
				layers = g_list_next(layers);
			}
		}
	}
}

/**
 * adds an item with the current coordinate of the vehicle to a named map
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attribute in[0] is the name of the map 
 * @param out output attribute, 0 on error or the id of the created item on success
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_map_add_curr_pos(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct attr **list = g_new0(struct attr *,2);
	struct attr*val = g_new0(struct attr,1);
	struct mapset* ms;
	struct map_selection sel;
	const int selection_range = 10;
	enum item_type item_type;
	struct item *it;
	struct map* curr_map = NULL;
	struct coord curr_coord;
	struct map_rect *mr;

	val->type = attr_type_item_begin;
	val->u.item = NULL; //return invalid item on error
	list[0] = val;
	list[1] = NULL;
	*out = list;
	if (in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str && //map name
			in[1] && ATTR_IS_STRING(in[1]->type) && in[1]->u.str //item type
	)
	{

		if (!(ms = navit_get_mapset(this)))
		{
			return;
		}

		if ((item_type = item_from_name(in[1]->u.str)) == type_none)
		{
			return;
		}

		curr_map = mapset_get_map_by_name(ms, in[0]->u.str);

		//no map with the given name found
		if (!curr_map)
		{
			return;
		}

		if (this->vehicle && this->vehicle->vehicle)
		{
			struct attr pos_attr;
			if (vehicle_get_attr(this->vehicle->vehicle, attr_position_coord_geo, &pos_attr, NULL))
			{
				transform_from_geo(projection_mg, pos_attr.u.coord_geo, &curr_coord);
			}
			else
			{
				return;
			}
		}
		else
		{
			return;
		}

		sel.next = NULL;
		sel.order = 18;
		sel.range.min = type_none;
		sel.range.max = type_tec_common;
		sel.u.c_rect.lu.x = curr_coord.x - selection_range;
		sel.u.c_rect.lu.y = curr_coord.y + selection_range;
		sel.u.c_rect.rl.x = curr_coord.x + selection_range;
		sel.u.c_rect.rl.y = curr_coord.y - selection_range;

		mr = map_rect_new(curr_map, &sel);
		if (mr)
		{
			it = map_rect_create_item(mr, item_type);
			item_coord_set(it, &curr_coord, 1, change_mode_modify);
			val->u.item = it;
		}
		map_rect_destroy(mr);
	}
}

/**
 * sets an attribute (name value pair) of a map item specified by map name and item id
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attribute in[0] - name of the map  ; in[1] - item  ; in[2] - attr name ; in[3] - attr value
 * @param out output attribute, 0 on error, 1 on success
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_map_item_set_attr(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	if (in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str && //map name
			in[1] && ATTR_IS_ITEM(in[1]->type) && //item
			in[2] && ATTR_IS_STRING(in[2]->type) && in[2]->u.str && //attr_type str
			in[3] && ATTR_IS_STRING(in[3]->type) && in[3]->u.str //attr_value str
	)
	{
		struct attr attr_to_set;
		struct map* curr_map = NULL;
		struct mapset *ms;
		struct map_selection sel;
		const int selection_range = 500;
		struct coord curr_coord;
		struct item *it;

		if (ATTR_IS_STRING(attr_from_name(in[2]->u.str)))
		{
			attr_to_set.u.str = in[3]->u.str;
			attr_to_set.type = attr_from_name(in[2]->u.str);
		}
		else if (ATTR_IS_INT(attr_from_name(in[2]->u.str)))
		{
			attr_to_set.u.num = atoi(in[3]->u.str);
			attr_to_set.type = attr_from_name(in[2]->u.str);
		}
		else if (ATTR_IS_DOUBLE(attr_from_name(in[2]->u.str)))
		{
			double* val = g_new0(double,1);
			*val = atof(in[3]->u.str);
			attr_to_set.u.numd = val;
			attr_to_set.type = attr_from_name(in[2]->u.str);
		}

		ms = navit_get_mapset(this);

		curr_map = mapset_get_map_by_name(ms, in[0]->u.str);

		if (!curr_map)
		{
			return;
		}
		sel.next = NULL;
		sel.order = 18;
		sel.range.min = type_none;
		sel.range.max = type_tec_common;
		sel.u.c_rect.lu.x = curr_coord.x - selection_range;
		sel.u.c_rect.lu.y = curr_coord.y + selection_range;
		sel.u.c_rect.rl.x = curr_coord.x + selection_range;
		sel.u.c_rect.rl.y = curr_coord.y - selection_range;

		it = in[1]->u.item;
		if (it)
		{
			item_attr_set(it, &attr_to_set, change_mode_modify);
		}
	}
}

/**
 * Get attr variable given a key string for the command system (for opaque usage)
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attribute in[0] is the key string
 * @param out output attribute, the attr for the given key string if exists or NULL  
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_get_attr_var(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct attr **list = g_new0(struct attr *,2);
	if (!cmd_int_var_hash)
	{
		struct attr*val = g_new0(struct attr,1);
		val->type = attr_type_item_begin;
		val->u.item = NULL;
		list[0] = val;
	}
	if (in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str)
	{
		struct attr*ret = g_hash_table_lookup(cmd_attr_var_hash, in[0]->u.str);
		if (ret)
		{
			list[0] = attr_dup(ret);
		}
		else
		{
			struct attr*val = g_new0(struct attr,1);
			val->type = attr_type_int_begin;
			val->u.item = NULL;
			list[0] = val;
		}
	}
	list[1] = NULL;
	*out = list;
}

/**
 * Get value given a key string for the command system
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attribute in[0] is the key string
 * @param out output attribute, the value for the given key string if exists or 0  
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_get_int_var(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct attr **list = g_new0(struct attr *,2);
	if (!cmd_int_var_hash)
	{
		struct attr*val = g_new0(struct attr,1);
		val->type = attr_type_int_begin;
		val->u.num = 0;
		list[0] = val;
	}
	if (in && in[0] && ATTR_IS_STRING(in[0]->type) && in[0]->u.str)
	{
		struct attr*ret = g_hash_table_lookup(cmd_int_var_hash, in[0]->u.str);
		if (ret)
		{
			list[0] = ret;
		}
		else
		{
			struct attr*val = g_new0(struct attr,1);
			val->type = attr_type_int_begin;
			val->u.num = 0;
			list[0] = val;
		}
	}
	list[1] = NULL;
	*out = list;
}

GList *cmd_int_var_stack = NULL;

/**
 * Push an integer to the stack for the command system
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attribute in[0] is the integer attibute to push
 * @param out output attributes, unused 
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_push_int(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	if (in && in[0] && ATTR_IS_NUMERIC(in[0]->type))
	{
		struct attr*val = g_new(struct attr,1);
		attr_dup_content(in[0], val);
		cmd_int_var_stack = g_list_prepend(cmd_int_var_stack, val);
	}
}

/**
 * Pop an integer from the command system's integer stack
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attributes unused
 * @param out output attribute, the value popped if stack isn't empty or 0
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_pop_int(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct attr **list = g_new0(struct attr *,2);
	if (!cmd_int_var_stack)
	{
		struct attr*val = g_new0(struct attr,1);
		val->type = attr_type_int_begin;
		val->u.num = 0;
		list[0] = val;
	}
	else
	{
		list[0] = cmd_int_var_stack->data;
		cmd_int_var_stack = g_list_remove_link(cmd_int_var_stack, cmd_int_var_stack);
	}
	list[1] = NULL;
	*out = list;
}

/**
 * Get current size of command system's integer stack
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attributes unused
 * @param out output attribute, the size of stack
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_int_stack_size(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct attr **list;
	struct attr *attr = g_new0(struct attr ,1);
	attr->type = attr_type_int_begin;
	if (!cmd_int_var_stack)
	{
		attr->u.num = 0;
	}
	else
	{
		attr->u.num = g_list_length(cmd_int_var_stack);
	}list = g_new0(struct attr *,2);
	list[0] = attr;
	list[1] = NULL;
	*out = list;
	cmd_int_var_stack = g_list_remove_link(cmd_int_var_stack, cmd_int_var_stack);
}

static void navit_cmd_set_destination(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct pcoord pc;
	char *description = NULL;
	if (!in)
		return;
	if (!in[0])
		return;
	pc.pro = transform_get_projection(this->trans);
	if (ATTR_IS_COORD(in[0]->type))
	{
		pc.x = in[0]->u.coord->x;
		pc.y = in[0]->u.coord->y;
		in++;
	}
	else if (ATTR_IS_PCOORD(in[0]->type))
	{
		pc = *in[0]->u.pcoord;
		in++;
	}
	else if (in[1] && in[2] && ATTR_IS_INT(in[0]->type) && ATTR_IS_INT(in[1]->type) && ATTR_IS_INT(in[2]->type))
	{
		pc.pro = in[0]->u.num;
		pc.x = in[1]->u.num;
		pc.y = in[2]->u.num;
		in += 3;
	}
	else if (in[1] && ATTR_IS_INT(in[0]->type) && ATTR_IS_INT(in[1]->type))
	{
		pc.x = in[0]->u.num;
		pc.y = in[1]->u.num;
		in += 2;
	}
	else
	{
		return;
	}
	if (in[0] && ATTR_IS_STRING(in[0]->type))
	{
		description = in[0]->u.str;
	}
	navit_set_destination(this, &pc, description, 1);
}

static void navit_cmd_fmt_coordinates(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct attr attr;
	attr.type = attr_type_string_begin;
	attr.u.str = "Fix me";
	if (out)
	{
		*out = attr_generic_add_attr(*out, &attr);
	}
}

/**
 * Join several string attributes into one
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attributes in[0] - separator, in[1..] - attributes to join
 * @param out output attribute joined attribute as string
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_strjoin(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	struct attr attr;
	gchar *ret, *sep;
	int i;
	attr.type = attr_type_string_begin;
	attr.u.str = NULL;
	if (in[0] && in[1])
	{
		sep = attr_to_text(in[0], NULL, 1);
		ret = attr_to_text(in[1], NULL, 1);
		for (i = 2; in[i]; i++)
		{
			gchar *in_i = attr_to_text(in[i], NULL, 1);
			gchar *r = g_strjoin(sep, ret, in_i, NULL);
			g_free(in_i);
			g_free(ret);
			ret = r;
		}
		g_free(sep);
		attr.u.str = ret;
		if (out)
		{
			*out = attr_generic_add_attr(*out, &attr);
		}
		g_free(ret);
	}
}

/**
 * Call external program
 *
 * @param navit The navit instance
 * @param function unused (needed to match command function signiture)
 * @param in input attributes in[0] - name of executable, in[1..] - parameters
 * @param out output attribute unused
 * @param valid unused 
 * @returns nothing
 */
static void navit_cmd_spawn(struct navit *this, char *function, struct attr **in, struct attr ***out, int *valid)
{
	int i, j, nparms, nvalid;
	const char ** argv = NULL;
	struct spawn_process_info *pi;

	nparms = 0;
	nvalid = 0;
	if (in)
	{
		while (in[nparms])
		{
			if (in[nparms]->type != attr_none)
				nvalid++;
			nparms++;
		}
	}

	if (nvalid > 0)
	{
		argv=g_new(char*,nvalid+1);
		for (i = 0, j = 0; in[i]; i++)
		{
			if (in[i]->type != attr_none)
			{
				argv[j++] = attr_to_text(in[i], NULL, 1);
			}
			else
			{
				//DBG dbg(0, "Parameter #%i is attr_none - skipping\n", i);
			}
		}
		argv[j] = NULL;
		pi = spawn_process(argv);

		// spawn_process() testing suite - uncomment following code to test.
		//sleep(3);
		// example of non-blocking wait
		//int st=spawn_process_check_status(pi,0);//DBG dbg(0,"status %i\n",st);
		// example of blocking wait
		//st=spawn_process_check_status(pi,1);//DBG dbg(0,"status %i\n",st);
		// example of wait after process is finished and status is
		// already tested
		//st=spawn_process_check_status(pi,1);//DBG dbg(0,"status %i\n",st);
		// example of wait after process is finished and status is
		// already tested - unblocked
		//st=spawn_process_check_status(pi,0);//DBG dbg(0,"status %i\n",st);

		// End testing suite
		spawn_process_info_free(pi);
		for (i = 0; argv[i]; i++)
			g_free(argv[i]);
		g_free(argv);
	}
}

static struct command_table
		commands[] =
				{ { "zoom_in", command_cast(navit_cmd_zoom_in) }, { "zoom_out", command_cast(navit_cmd_zoom_out) }, { "zoom_to_route", command_cast(navit_cmd_zoom_to_route) }, { "say", command_cast(navit_cmd_say) }, { "set_center_cursor", command_cast(navit_cmd_set_center_cursor) }, { "set_destination", command_cast(navit_cmd_set_destination) }, { "announcer_toggle", command_cast(navit_cmd_announcer_toggle) }, { "fmt_coordinates", command_cast(navit_cmd_fmt_coordinates) }, { "set_int_var", command_cast(navit_cmd_set_int_var) }, { "get_int_var", command_cast(navit_cmd_get_int_var) }, { "push_int", command_cast(navit_cmd_push_int) }, { "pop_int", command_cast(navit_cmd_pop_int) }, { "int_stack_size", command_cast(navit_cmd_int_stack_size) }, { "toggle_layer", command_cast(navit_cmd_toggle_layer) }, { "strjoin", command_cast(navit_cmd_strjoin) }, { "spawn", command_cast(navit_cmd_spawn) }, { "map_add_curr_pos", command_cast(navit_cmd_map_add_curr_pos) }, { "map_item_set_attr", command_cast(navit_cmd_map_item_set_attr) }, { "set_attr_var", command_cast(navit_cmd_set_attr_var) }, { "get_attr_var", command_cast(navit_cmd_get_attr_var) }, };

void navit_command_add_table(struct navit*this_, struct command_table *commands, int count)
{
	command_add_table(this_->attr_cbl, commands, count, this_);
}


struct navit *
navit_new(struct attr *parent, struct attr **attrs)
{
	struct navit *this_=g_new0(struct navit, 1);
	struct pcoord center;
	struct coord co;
	struct coord_geo g;
	enum projection pro = projection_mg;
	int zoom = 256;
	g.lat = 53.13;
	g.lng = 11.70;


	global_demo_vehicle = 0;
	global_demo_vehicle_short_switch = 0;


	// set base for timestamps
	struct timeval tv2;
	if (gettimeofday(&tv2, NULL) == -1)
	{
		global_last_spoken_base = 0;
	}
	else
	{
		global_last_spoken_base = (long)tv2.tv_sec;
	}

	global_debug_coord_list = g_new0(struct coord, (2 * (MAX_DEBUG_COORDS + 2)));
	global_debug_coord_list_items = 0;

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

	global_debug_seg_winner_p_start.x = 0;
	global_debug_seg_winner_p_start.y = 0;

	global_debug_seg_route_start.x = 0;
	global_debug_seg_route_start.y = 0;

	global_debug_seg_route_end.x = 0;
	global_debug_seg_route_end.y = 0;



	this_->self.type = attr_navit;
	this_->self.u.navit = this_;
	this_->attr_cbl = callback_list_new("navit_new:this_->attr_cbl");

	this_->orientation = -1;
	this_->tracking_flag = 1;
	this_->recentdest_count = 10;
	this_->osd_configuration = -1;

	// changed default to 1
	this_->center_timeout = 1;
	this_->use_mousewheel = 1;
	this_->autozoom_secs = 10;
	this_->autozoom_min = 5;
	this_->autozoom_active = 0;
	this_->zoom_min = 1;
	this_->zoom_max = 1048576; //-> order=-2  // 2097152 -> order=-3;
	this_->follow_cursor = 1;
	this_->radius = 30;
	this_->border = 16;

	// dbg(0, "GGGGG:set global_navit\n");
	global_navit = this_;

	this_->trans = transform_new();
	this_->trans_cursor = transform_new();
	transform_from_geo(pro, &g, &co);
	center.x = co.x;
	center.y = co.y;
	center.pro = pro;

	transform_init();

	//DBG dbg(0, "setting center from xmlfile [hardcoded]\n");
	transform_setup(this_->trans, &center, zoom, (this_->orientation != -1) ? this_->orientation : 0);

	// initialze trans_cursor here
	transform_copy(this_->trans, this_->trans_cursor);
	// initialze trans_cursor here


	dbg(0, "ii 001\n");
	this_->bookmarks = bookmarks_new(&this_->self, NULL, this_->trans);
	//this_->bookmarks = NULL;
	dbg(0, "ii 002\n");

	this_->prevTs = 0;

	for (; *attrs; attrs++)
	{
		navit_set_attr_do(this_, *attrs, 1);
	}
	this_->displaylist = graphics_displaylist_new();
	command_add_table(this_->attr_cbl, commands, sizeof(commands) / sizeof(struct command_table), this_);

	dbg(0, "ii 009\n");

	// this_->messages = messagelist_new(attrs);

	dbg(0, "111111\n");

	return this_;
}

static int navit_set_gui(struct navit *this_, struct gui *gui)
{
	if (this_->gui)
		return 0;

	this_->gui = gui;

	if (gui_has_main_loop(this_->gui))
	{
		if (!main_loop_gui)
		{
			main_loop_gui = this_->gui;
		}
		else
		{
			//DBG dbg(0, "gui with main loop already active, ignoring this instance");
			return 0;
		}
	}
	return 1;
}

void navit_add_message(struct navit *this_, char *message)
{
	// message_new(this_->messages, message);
}

struct message *navit_get_messages(struct navit *this_)
{
	// return message_get(this_->messages);
}

static int navit_set_graphics(struct navit *this_, struct graphics *gra)
{
	if (this_->gra)
	{
		return 0;
	}

	this_->gra = gra;

	/*
	 this_->resize_callback = callback_new_attr_1(callback_cast(navit_resize), attr_resize, this_);
	 graphics_add_callback(gra, this_->resize_callback);
	 this_->button_callback = callback_new_attr_1(callback_cast(navit_button), attr_button, this_);
	 graphics_add_callback(gra, this_->button_callback);
	 this_->motion_callback = callback_new_attr_1(callback_cast(navit_motion), attr_motion, this_);
	 graphics_add_callback(gra, this_->motion_callback);
	 */

	// this draw the vehicle // very stupid
	this_->predraw_callback = callback_new_attr_1(callback_cast(navit_predraw), attr_predraw, this_);
	callback_add_names(this_->predraw_callback, "navit_set_graphics", "navit_predraw");
	graphics_add_callback(gra, this_->predraw_callback);

	return 1;
}

struct graphics *
navit_get_graphics(struct navit *this_)
{
	return this_->gra;
}

struct vehicleprofile *
navit_get_vehicleprofile(struct navit *this_)
{
	return this_->vehicleprofile;
}

GList *
navit_get_vehicleprofiles(struct navit *this_)
{
	return this_->vehicleprofiles;
}

static void navit_projection_set(struct navit *this_, enum projection pro, int draw)
{


	////DBG dbg(0,"EEnter\n");
	struct coord_geo g;
	struct coord *c;

	c = transform_center(this_->trans);
	transform_to_geo(transform_get_projection(this_->trans), c, &g);
	transform_set_projection(this_->trans, pro);
	transform_from_geo(pro, &g, c);
	if (draw)
	{
		navit_draw(this_);
	}
}

/**
 * Start the route computing to a given set of coordinates
 *
 * @param navit The navit instance
 * @param c The coordinate to start routing to
 * @param description A label which allows the user to later identify this destination in the former destinations selection
 * @returns nothing
 */
void navit_set_destination(struct navit *this_, struct pcoord *c, const char *description, int async)
{


	////DBG dbg(0,"EEnter\n");
	char *destination_file;
	if (c)
	{
		this_->destination = *c;
		this_->destination_valid = 1;
		//dbg(0, "navit->navit_set_destination %i\n", c->x);
		//dbg(0, "navit->navit_set_destination %i\n", c->y);
	}
	else
	{
		this_->destination_valid = 0;
	}
	//destination_file = bookmarks_get_destination_file(TRUE);
	//bookmarks_append_coord(this_->bookmarks, destination_file, c, 1, "former_destination", description, NULL, this_->recentdest_count);
	//g_free(destination_file);
	callback_list_call_attr_0(this_->attr_cbl, attr_destination);
	if (this_->route)
	{
		//dbg(0, "navit->navit_set_destination 2: %i %i\n", c->x, c->y);

		route_set_destination(this_->route, c, async);
		if (this_->ready == 3)
		{
			navit_draw(this_);
		}
	}
}

/**
 * add a waypoint to an active route
 *
 * @param navit The navit instance
 * @param c The coordinate of the waypoint
 * @param description A dummy string
 * @returns nothing
 */
void navit_add_waypoint_to_route(struct navit *this_, struct pcoord *c, const char *description, int async)
{


	if (this_->destination_valid == 1)
	{
		//int count = 0;
		//count = g_list_length(this_->route->destinations);
		//DBG dbg(0, "count=%d\n", count);

		//dbg(0, "navit->navit_add_waypoint_to_route 1: %i %i\n", c->x, c->y);

		route_add_destination(this_->route, c, async);

		this_->destination = *c;
		this_->destination_valid = 1;
	}
	else
	{
		//dbg(0, "navit->navit_add_waypoint_to_route 2: %i %i\n", c->x, c->y);
		navit_set_destination(this_, c, description, async);
	}
}

/**
 * Start the route computing to a given set of coordinates including waypoints
 *
 * @param navit The navit instance
 * @param c The coordinate to start routing to
 * @param description A label which allows the user to later identify this destination in the former destinations selection
 * @returns nothing
 */
void navit_set_destinations(struct navit *this_, struct pcoord *c, int count, const char *description, int async)
{


	////DBG dbg(0,"EEnter\n");
	char *destination_file;
	if (c && count)
	{
		this_->destination = c[count - 1];
		this_->destination_valid = 1;
		//dbg(0, "navit->navit_set_destinations 1: %i %i\n", c[count-1].x, c[count-1].y);
	}
	else
	{
		this_->destination_valid = 0;
	}
	//destination_file = bookmarks_get_destination_file(TRUE);
	//bookmarks_append_coord(this_->bookmarks, destination_file, c, count, "former_itinerary", description, NULL, this_->recentdest_count);
	//g_free(destination_file);
	callback_list_call_attr_0(this_->attr_cbl, attr_destination);
	if (this_->route)
	{
		route_set_destinations(this_->route, c, count, async);
		if (this_->ready == 3)
		{
			navit_draw(this_);
		}
	}
}

/**
 * @brief Checks if a route is calculated
 *
 * This function checks if a route is calculated.
 *
 * @param this_ The navit struct whose route should be checked.
 * @return True if the route is set, false otherwise.
 */
int navit_check_route(struct navit *this_)
{


	////DBG dbg(0,"EEnter\n");
	if (this_->route)
	{
		return route_get_path_set(this_->route);
	}

	return 0;
}

static int navit_former_destinations_active(struct navit *this_)
{


	////DBG dbg(0,"EEnter\n");

	return 0;
	// disable this function!!


	char *destination_file = bookmarks_get_destination_file(FALSE);
	FILE *f;
	int active = 0;
	char buffer[3];
	f = fopen(destination_file, "r");
	if (f)
	{
		if (!fseek(f, -2, SEEK_END) && fread(buffer, 2, 1, f) == 1 && (buffer[0] != '\n' || buffer[1] != '\n'))
		{
			active = 1;
		}
		fclose(f);
	}
	g_free(destination_file);

	return active;
}

static void navit_add_former_destinations_from_file(struct navit *this_)
{


	////DBG dbg(0,"EEnter\n");
	char *destination_file = bookmarks_get_destination_file(FALSE);
	struct attr *attrs[4];
	struct map_rect *mr;
	struct item *item;
	int i, valid = 0, count = 0;
	struct coord c[16];
	struct pcoord pc[16];
	struct attr parent;
	struct attr type;
	struct attr data;
	struct attr flags;

	parent.type = attr_navit;
	parent.u.navit = this_;

	type.type = attr_type;
	type.u.str = "textfile";

	data.type = attr_data;
	data.u.str = destination_file;

	flags.type = attr_flags;
	flags.u.num = 1;

	attrs[0] = &type;
	attrs[1] = &data;
	attrs[2] = &flags;
	attrs[3] = NULL;

	this_->former_destination = map_new(&parent, attrs);
	g_free(destination_file);
	if (!this_->route || !navit_former_destinations_active(this_))
		return;
	mr = map_rect_new(this_->former_destination, NULL);
	while ((item = map_rect_get_item(mr)))
	{
		if ((item->type == type_former_destination || item->type == type_former_itinerary || item->type == type_former_itinerary_part) && (count = item_coord_get(item, c, 16)))
			valid = 1;
	}
	map_rect_destroy(mr);
	if (valid && count > 0)
	{
		for (i = 0; i < count; i++)
		{
			pc[i].pro = map_projection(this_->former_destination);
			pc[i].x = c[i].x;
			pc[i].y = c[i].y;
		}
		if (count == 1)
		{
			route_set_destination(this_->route, &pc[0], 1);
		}
		else
		{
			route_set_destinations(this_->route, pc, count, 1);
		}
		this_->destination = pc[count - 1];
		this_->destination_valid = 1;
	}
}

void navit_textfile_debug_log(struct navit *this_, const char *fmt, ...)
{


	////DBG dbg(0,"EEnter\n");
	va_list ap;
	char *str1, *str2;
	va_start(ap, fmt);
	if (this_->textfile_debug_log && this_->vehicle)
	{
		str1 = g_strdup_vprintf(fmt, ap);
		str2 = g_strdup_printf("0x%x 0x%x%s%s\n", this_->vehicle->coord.x, this_->vehicle->coord.y, strlen(str1) ? " " : "", str1);
		log_write(this_->textfile_debug_log, str2, strlen(str2), 0);
		g_free(str2);
		g_free(str1);
	}
	va_end(ap);
}

void navit_textfile_debug_log_at(struct navit *this_, struct pcoord *pc, const char *fmt, ...)
{


	////DBG dbg(0,"EEnter\n");
	va_list ap;
	char *str1, *str2;
	va_start(ap, fmt);
	if (this_->textfile_debug_log && this_->vehicle)
	{
		str1 = g_strdup_vprintf(fmt, ap);
		str2 = g_strdup_printf("0x%x 0x%x%s%s\n", pc->x, pc->y, strlen(str1) ? " " : "", str1);
		log_write(this_->textfile_debug_log, str2, strlen(str2), 0);
		g_free(str2);
		g_free(str1);
	}
	va_end(ap);
}

void navit_say(struct navit *this_, char *text)
{


	////DBG dbg(0,"EEnter\n");
	if (this_->speech)
	{
		//dbg(0,"say(1) s=%s\n", text);
		speech_say(this_->speech, text);
	}
}

/**
 * @brief Toggles the navigation announcer for navit
 * @param this_ The navit object
 */
static void navit_cmd_announcer_toggle(struct navit *this_)
{


	struct attr attr, speechattr;

	// search for the speech attribute
	if (!navit_get_attr(this_, attr_speech, &speechattr, NULL))
		return;
	// find out if the corresponding attribute attr_active has been set
	if (speech_get_attr(speechattr.u.speech, attr_active, &attr, NULL))
	{
		// flip it then...
		attr.u.num = !attr.u.num;
	}
	else
	{
		// otherwise disable it because voice is enabled by default
		attr.type = attr_active;
		attr.u.num = 0;
	}

	// apply the new state
	if (!speech_set_attr(speechattr.u.speech, &attr))
		return;

	// announce that the speech attribute has changed
	callback_list_call_attr_0(this_->attr_cbl, attr_speech);
}

void navit_cmd_announcer_on(struct navit *this_)
{


	struct attr attr, speechattr;

	// search for the speech attribute
	if (!navit_get_attr(this_, attr_speech, &speechattr, NULL))
		return;

	attr.type = attr_active;
	attr.u.num = 1;

	// apply the new state
	if (!speech_set_attr(speechattr.u.speech, &attr))
		return;

	// announce that the speech attribute has changed
	callback_list_call_attr_0(this_->attr_cbl, attr_speech);
}

void navit_cmd_announcer_off(struct navit *this_)
{


	struct attr attr, speechattr;

	// search for the speech attribute
	if (!navit_get_attr(this_, attr_speech, &speechattr, NULL))
		return;

	attr.type = attr_active;
	attr.u.num = 0;

	// apply the new state
	if (!speech_set_attr(speechattr.u.speech, &attr))
		return;

	// announce that the speech attribute has changed
	callback_list_call_attr_0(this_->attr_cbl, attr_speech);
}

void navit_speak(struct navit *this_)
{


	////DBG dbg(0,"EEnter\n");
	struct navigation *nav = this_->navigation;
	struct map *map = NULL;
	struct map_rect *mr = NULL;
	struct item *item;
	struct attr attr;

	if (!speech_get_attr(this_->speech, attr_active, &attr, NULL))
	{
		attr.u.num = 1;
	}

	// dbg(1, "this_.speech->active %i\n", attr.u.num);

	dbg(0, "NAV_TURNAROUND:008:enter\n");

	if (!attr.u.num)
	{
		return;
	}

	if (nav)
		map = navigation_get_map(nav);

	if (map)
		mr = map_rect_new(map, NULL);

	if (mr)
	{
		while ((item = map_rect_get_item(mr)) && (item->type == type_nav_position || item->type == type_nav_none))
		{
			dbg(0, "NAV_TURNAROUND:008a:%s\n", item_to_name(item->type));
		}

		dbg(0, "NAV_TURNAROUND:008b:item=%p\n", item);

		if (item && item_attr_get(item, attr_navigation_speech, &attr)) // this calls --> navigation_map_item_attr_get(...) --> show_next_maneuvers(...)
		{
			//dbg(0,"say(2) s=X%sX\n", attr.u.str);

			dbg(0, "NAV_TURNAROUND:009:%s\n", attr.u.str);

			if (strlen(attr.u.str) > 0)
			{
				speech_say(this_->speech, attr.u.str);
			}
			//navit_add_message(this_, attr.u.str);
			// navit_textfile_debug_log(this_, "type=announcement label=\"%s\"", attr.u.str);
		}
		map_rect_destroy(mr);
	}
}

static void navit_window_roadbook_update(struct navit *this_)
{


	////DBG dbg(0,"EEnter\n");
	struct navigation *nav = this_->navigation;
	struct map *map = NULL;
	struct map_rect *mr = NULL;
	struct item *item;
	struct attr attr;
	struct param_list param[5];
	int secs;

	// dbg(1, "enter\n");
	datawindow_mode(this_->roadbook_window, 1);
	if (nav)
		map = navigation_get_map(nav);
	if (map)
		mr = map_rect_new(map, NULL);
	////DBG dbg(0,"nav=%p map=%p mr=%p\n", nav, map, mr);
	if (mr)
	{
		////DBG dbg(0,"while loop\n");
		while ((item = map_rect_get_item(mr)))
		{
			////DBG dbg(0,"item=%p\n", item);
			attr.u.str = NULL;
			if (item->type != type_nav_position)
			{
				item_attr_get(item, attr_navigation_long, &attr);
				if (attr.u.str == NULL)
				{
					continue;
				}
				dbg(2, "Command='%s'\n", attr.u.str);
				param[0].value = g_strdup(attr.u.str);
			}
			else
				param[0].value = _("Position");
			param[0].name = _("Command");

			item_attr_get(item, attr_length, &attr);
			dbg(2, "Length=%d\n", attr.u.num);
			param[1].name = _("Length");

			if (attr.u.num >= 2000)
			{
				param[1].value = g_strdup_printf("%5.1f %s", (float) attr.u.num / 1000, _("km"));
			}
			else
			{
				param[1].value = g_strdup_printf("%7d %s", attr.u.num, _("m"));
			}

			item_attr_get(item, attr_time, &attr);
			dbg(2, "Time=%d\n", attr.u.num);
			secs = attr.u.num / 10;
			param[2].name = _("Time");
			if (secs >= 3600)
			{
				param[2].value = g_strdup_printf("%d:%02d:%02d", secs / 60, (secs / 60) % 60, secs % 60);
			}
			else
			{
				param[2].value = g_strdup_printf("%d:%02d", secs / 60, secs % 60);
			}

			item_attr_get(item, attr_destination_length, &attr);
			dbg(2, "Destlength=%d\n", attr.u.num);
			param[3].name = _("Destination Length");
			if (attr.u.num >= 2000)
			{
				param[3].value = g_strdup_printf("%5.1f %s", (float) attr.u.num / 1000, _("km"));
			}
			else
			{
				param[3].value = g_strdup_printf("%d %s", attr.u.num, _("m"));
			}

			item_attr_get(item, attr_destination_time, &attr);
			dbg(2, "Desttime=%d\n", attr.u.num);
			secs = attr.u.num / 10;
			param[4].name = _("Destination Time");
			if (secs >= 3600)
			{
				param[4].value = g_strdup_printf("%d:%02d:%02d", secs / 3600, (secs / 60) % 60, secs % 60);
			}
			else
			{
				param[4].value = g_strdup_printf("%d:%02d", secs / 60, secs % 60);
			}
			datawindow_add(this_->roadbook_window, param, 5);
		}
		map_rect_destroy(mr);
	}
	datawindow_mode(this_->roadbook_window, 0);
}

void navit_window_roadbook_destroy(struct navit *this_)
{


	////DBG dbg(0, "enter\n");
	navigation_unregister_callback(this_->navigation, attr_navigation_long, this_->roadbook_callback);
	this_->roadbook_window = NULL;
	this_->roadbook_callback = NULL;
}

void navit_window_roadbook_new(struct navit *this_)
{


	if (!this_->gui || this_->roadbook_callback || this_->roadbook_window)
	{
		return;
	}

	this_->roadbook_callback = callback_new_1(callback_cast(navit_window_roadbook_update), this_);
	navigation_register_callback(this_->navigation, attr_navigation_long, this_->roadbook_callback);
	this_->roadbook_window = gui_datawindow_new(this_->gui, _("Roadbook"), NULL, callback_new_1(callback_cast(navit_window_roadbook_destroy), this_));
	navit_window_roadbook_update(this_);
}

void navit_remove_all_maps(struct navit *this_)
{



	dbg(0,"ROUTExxPOSxx:navit_remove_all_maps:enter\n");

	struct mapset *ms;
	struct map *map3;

	// hold map drawing
	// this_->ready = 1;

	// first: stop navigation!
	//if (global_navit->destination_valid != 0)
	//{
	navit_set_destination(global_navit, NULL, NULL, 0);
	//}


	if (this_->tracking)
	{
		tracking_flush(this_->tracking);
	}

	if (this_->route)
	{
		struct attr callback;
		// this_->route_cb=callback_new_attr_1(callback_cast(navit_redraw_route), attr_route_status, this_);


		callback.type = attr_callback;
		callback.u.callback = this_->route_cb;
		route_remove_attr(this_->route, &callback);

		this_->route->ms = NULL;
		// route_set_mapset(this_->route, ms);
		// route_set_projection(this_->route, transform_get_projection(this_->trans));

		//*********route_destroy(this_->route);

		//route_path_destroy(this_->route->path2,1);
		//this_->route->path2 = NULL;
		//route_graph_destroy(this_->route->graph);
		//this_->route->graph=NULL;
	}

	/*
	 map_rect_destroy(displaylist->mr);
	 if (!route_selection)
	 map_selection_destroy(displaylist->sel);
	 mapset_close(displaylist->msh);
	 displaylist->mr=NULL;
	 displaylist->sel=NULL;
	 displaylist->m=NULL;
	 displaylist->msh=NULL;
	 profile(1,"callback\n");
	 callback_call_1(displaylist->cb, cancel);
	 */

	struct displaylist *dl = navit_get_displaylist(this_);
	dl->m = NULL;
	dl->msh = NULL;

	if (this_->mapsets)
	{
		struct mapset_handle *msh;
		ms = this_->mapsets->data;
		msh = mapset_open(ms);
		////DBG dbg(0,"removing map bb0\n");
		while (msh && (map3 = mapset_next(msh, 0)))
		{
			////DBG dbg(0,"removing map bb1\n");
			struct attr map_name_attr;
			if (map_get_attr(map3, attr_name, &map_name_attr, NULL))
			{
				//DBG dbg(0, "map name=%s", map_name_attr.u.str);
				if (strncmp("_ms_sdcard_map:", map_name_attr.u.str, 15) == 0)
				{
					dbg(0, "removing map name=%s", map_name_attr.u.str);
					//DBG dbg(0, "removing map a0\n");
					struct attr active;
					active.type = attr_active;
					active.u.num = 0;
					//map_set_attr(map3, &active);

					//DBG dbg(0, "removing map a1\n");
					struct attr map_attr;
					map_attr.u.map = map3;
					map_attr.type = attr_map;
					mapset_remove_attr(ms, &map_attr);

					//DBG dbg(0, "removing map a2\n");
					map3->refcount = 1;
					map_destroy(map3);
					//DBG dbg(0, "removing map a3\n");
					map3 = NULL;
				}
				else if (strncmp("-special-:", map_name_attr.u.str, 10) == 0)
				{
					dbg(0, "removing (special) map name=%s", map_name_attr.u.str);
					struct attr active;
					active.type = attr_active;
					active.u.num = 0;

					struct attr map_attr;
					map_attr.u.map = map3;
					map_attr.type = attr_map;
					mapset_remove_attr(ms, &map_attr);

					map3->refcount = 1;
					map_destroy(map3);
					map3 = NULL;
				}
			}
		}
		mapset_close(msh);
		//DBG dbg(0, "removing map bb4\n");
	}

	dl->ms = this_->mapsets->data;

	// int async = 0;
	// transform_setup_source_rect(this_->trans);
	// graphics_draw(this_->gra, this_->displaylist, this_->mapsets->data, this_->trans, this_->layout_current, async, NULL, this_->graphics_flags|1);
	//this_->displaylist->ms=this_->mapsets->data;

}

void navit_map_active_flag(struct navit *this_, int activate, const char *mapname)
{
	// activate = 0 -> deactivate
	// activate = 1 -> activate



	struct mapset *ms;
	struct map *map3;

	if (this_->mapsets)
	{
		struct mapset_handle *msh;
		ms = this_->mapsets->data;
		msh = mapset_open(ms);
		while (msh && (map3 = mapset_next(msh, 0)))
		{
			struct attr map_name_attr;
			if (map_get_attr(map3, attr_name, &map_name_attr, NULL))
			{
				dbg(0, "map name=%s\n", map_name_attr.u.str);
				if (strcmp(mapname, map_name_attr.u.str) == 0)
				{
					dbg(0, "setting active flag on map:%s\n", map_name_attr.u.str);

					struct attr active;
					active.type = attr_active;
					active.u.num = activate;
					map_set_attr(map3, &active);
				}
			}
		}
		mapset_close(msh);
	}

}

void navit_add_all_maps(struct navit *this_)
{
__F_START__

	struct map *map3;

	if (this_->mapsets)
	{
		//DBG dbg(0, "xADDx all maps - start\n");

		struct mapset *ms;
		ms = this_->mapsets->data;

		struct attr type;
		struct attr parent;
		struct attr data;
		struct attr flags;
		struct map *map2;
		struct attr map2_attr;
		struct attr *attrs[4];
		char *map_file;

		dbg(0, "001\n");

		parent.type = attr_navit;
		parent.u.navit = this_;
		type.type = attr_type;
		type.u.str = "binfile";
		data.type = attr_data;
		map_file = g_strdup_printf("%sborders.bin", navit_maps_dir);
		data.u.str = map_file;

		////DBG dbg(0,"map name=%s",map_file);

		flags.type = attr_flags;
		flags.u.num = 0;
		attrs[0] = &type;
		attrs[1] = &data;
		attrs[2] = &flags;
		attrs[3] = NULL;
		map2 = map_new(&parent, attrs);
		if (map2)
		{
			map2_attr.u.data = map2;
			map2_attr.type = attr_map;
			// mapset_add_attr_name(ms, &map2_attr);
			mapset_add_attr_name_str(ms, &map2_attr, "/sdcard/zanavi/maps/borders.bin");
			struct attr active;
			active.type = attr_active;
			active.u.num = 0;
			//map_set_attr(map2, &active);

			active.type = attr_route_active;
			active.u.num = 0; // by default deactivate rounting on this map
			map_set_attr(map2, &active);
		}
		g_free(map_file);



/*
		parent.type = attr_navit;
		parent.u.navit = this_;
		type.type = attr_type;
		type.u.str = "binfile";
		data.type = attr_data;
		map_file = g_strdup_printf("%scoastline.bin", navit_maps_dir);
		data.u.str = map_file;

		////DBG dbg(0,"map name=%s",map_file);

		flags.type = attr_flags;
		flags.u.num = 0;
		attrs[0] = &type;
		attrs[1] = &data;
		attrs[2] = &flags;
		attrs[3] = NULL;
		map2 = map_new(&parent, attrs);
		if (map2)
		{
			map2_attr.u.data = map2;
			map2_attr.type = attr_map;
			// mapset_add_attr_name(ms, &map2_attr);
			mapset_add_attr_name_str(ms, &map2_attr, "/sdcard/zanavi/maps/coastline.bin");
			struct attr active;
			active.type = attr_active;
			active.u.num = 0;
			//map_set_attr(map2, &active);

			active.type = attr_route_active;
			active.u.num = 0; // by default deactivate rounting on this map
			map_set_attr(map2, &active);
		}
		g_free(map_file);


*/


		// gpx tracks map --------------------
		parent.type = attr_navit;
		parent.u.navit = this_;
		type.type = attr_type;
		type.u.str = "textfile";
		data.type = attr_data;
		map_file = g_strdup_printf("%sgpxtracks.txt", navit_maps_dir);
		data.u.str = map_file;

		flags.type = attr_flags;
		flags.u.num = 0;
		attrs[0] = &type;
		attrs[1] = &data;
		attrs[2] = &flags;
		attrs[3] = NULL;
		map2 = map_new(&parent, attrs);
		if (map2)
		{
			map2_attr.u.data = map2;
			map2_attr.type = attr_map;
			mapset_add_attr_name_str(ms, &map2_attr, "-special-:gpxtracks.txt");
			struct attr active;
			active.type = attr_active;
			active.u.num = 0;
			//map_set_attr(map2, &active);
		}
		g_free(map_file);
		// gpx tracks map --------------------


		// traffic map --------------------
		parent.type = attr_navit;
		parent.u.navit = this_;
		type.type = attr_type;
		type.u.str = "textfile";
		data.type = attr_data;
		map_file = g_strdup_printf("%straffic.txt", navit_maps_dir);
		data.u.str = map_file;

		flags.type = attr_flags;
		flags.u.num = 0;
		attrs[0] = &type;
		attrs[1] = &data;
		attrs[2] = &flags;
		attrs[3] = NULL;
		map2 = map_new(&parent, attrs);
		if (map2)
		{
			map2_attr.u.data = map2;
			map2_attr.type = attr_map;
			mapset_add_attr_name_str(ms, &map2_attr, "-special-:traffic.txt");
			struct attr active;
			active.type = attr_active;
			active.u.num = 0;
			//map_set_attr(map2, &active);
		}
		g_free(map_file);
		// traffic map --------------------


		dbg(0, "002\n");


		// world map2 --------------------
		parent.type = attr_navit;
		parent.u.navit = this_;
		type.type = attr_type;
		type.u.str = "textfile";
		data.type = attr_data;
		map_file = g_strdup_printf("%sworldmap2.txt", navit_maps_dir);
		data.u.str = map_file;

		dbg(0, "activate map:%s\n", map_file);

		flags.type = attr_flags;
		flags.u.num = 0;
		attrs[0] = &type;
		attrs[1] = &data;
		attrs[2] = &flags;
		attrs[3] = NULL;
		map2 = map_new(&parent, attrs);
		if (map2)
		{
			map2_attr.u.data = map2;
			map2_attr.type = attr_map;
			mapset_add_attr_name_str(ms, &map2_attr, "-special-:worldmap2.txt");
			struct attr active;
			active.type = attr_active;
			active.u.num = 1; // by default activate map
			// map_set_attr(map2, &active);

			active.type = attr_route_active;
			active.u.num = 0; // by default deactivate routing on this map
			map_set_attr(map2, &active);

		}
		g_free(map_file);
		// world map2 --------------------


		// world map5 --------------------
		parent.type = attr_navit;
		parent.u.navit = this_;
		type.type = attr_type;
		type.u.str = "textfile";
		data.type = attr_data;
		map_file = g_strdup_printf("%sworldmap5.txt", navit_maps_dir);
		data.u.str = map_file;

		dbg(0, "activate map:%s\n", map_file);

		flags.type = attr_flags;
		flags.u.num = 0;
		attrs[0] = &type;
		attrs[1] = &data;
		attrs[2] = &flags;
		attrs[3] = NULL;
		map2 = map_new(&parent, attrs);
		if (map2)
		{
			map2_attr.u.data = map2;
			map2_attr.type = attr_map;
			mapset_add_attr_name_str(ms, &map2_attr, "-special-:worldmap5.txt");
			struct attr active;
			active.type = attr_active;
			active.u.num = 1; // by default activate map
			// map_set_attr(map2, &active);

			active.type = attr_route_active;
			active.u.num = 0; // by default deactivate routing on this map
			map_set_attr(map2, &active);
		}
		g_free(map_file);
		// world map5 --------------------

		dbg(0, "003\n");


#if 0
		// world map6 --------------------
		parent.type = attr_navit;
		parent.u.navit = this_;
		type.type = attr_type;
		type.u.str = "textfile";
		data.type = attr_data;
		map_file = g_strdup_printf("%sworldmap6.txt", navit_maps_dir);
		data.u.str = map_file;

		dbg(0, "activate map:%s\n", map_file);

		flags.type = attr_flags;
		flags.u.num = 0;
		attrs[0] = &type;
		attrs[1] = &data;
		attrs[2] = &flags;
		attrs[3] = NULL;
		map2 = map_new(&parent, attrs);
		if (map2)
		{
			map2_attr.u.data = map2;
			map2_attr.type = attr_map;
			mapset_add_attr_name_str(ms, &map2_attr, "-special-:worldmap6.txt");
			struct attr active;
			active.type = attr_active;
			active.u.num = 1; // by default activate map
			// map_set_attr(map2, &active);

			active.type = attr_route_active;
			active.u.num = 0; // by default deactivate routing on this map
			map_set_attr(map2, &active);
		}
		g_free(map_file);
		// world map6 --------------------
#endif

		dbg(0, "004\n");


		int i = 1;
		for (i = 1; i < 10; i++)
		{
			struct map *map22;
			struct attr map22_attr;
			parent.type = attr_navit;
			parent.u.navit = this_;
			type.type = attr_type;
			type.u.str = "binfile";
			data.type = attr_data;
			map_file = g_strdup_printf("%snavitmap_00%d.bin", navit_maps_dir, i);
			data.u.str = map_file;
			flags.type = attr_flags;
			flags.u.num = 0;
			attrs[0] = &type;
			attrs[1] = &data;
			attrs[2] = &flags;
			attrs[3] = NULL;
			map22 = map_new(&parent, attrs);
			if (map22)
			{
				//DBG dbg(0, "*add* map name=%s\n", map_file);
				map22_attr.u.data = map22;
				map22_attr.type = attr_map;
				// mapset_add_attr_name(ms, &map22_attr);
				char *map_name_str;
				map_name_str = g_strdup_printf("/sdcard/zanavi/maps/navitmap_00%d.bin", i);
				mapset_add_attr_name_str(ms, &map22_attr, map_name_str);
				struct attr active;
				active.type = attr_active;
				active.u.num = 0;
				//map_set_attr(map22, &active);
				g_free(map_name_str);
			}
			g_free(map_file);
		}

		i = 10;
		for (i = 10; i < 61; i++)
		{
			parent.type = attr_navit;
			parent.u.navit = this_;
			type.type = attr_type;
			type.u.str = "binfile";
			data.type = attr_data;
			map_file = g_strdup_printf("%snavitmap_0%d.bin", navit_maps_dir, i);
			data.u.str = map_file;
			////DBG dbg(0,"map name=%s",map_file);
			flags.type = attr_flags;
			flags.u.num = 0;
			attrs[0] = &type;
			attrs[1] = &data;
			attrs[2] = &flags;
			attrs[3] = NULL;
			map2 = map_new(&parent, attrs);
			if (map2)
			{
				map2_attr.u.data = map2;
				map2_attr.type = attr_map;
				// mapset_add_attr_name(ms, &map2_attr);
				char *map_name_str;
				map_name_str = g_strdup_printf("/sdcard/zanavi/maps/navitmap_0%d.bin", i);
				mapset_add_attr_name_str(ms, &map2_attr, map_name_str);
				struct attr active;
				active.type = attr_active;
				active.u.num = 0;
				//map_set_attr(map2, &active);
				g_free(map_name_str);
			}
			g_free(map_file);
		}
	}

	/*
	 if (this_->mapsets)
	 {
	 struct mapset_handle *msh;
	 struct map *map;
	 struct mapset *ms;

	 //DBG dbg(0,"xx ms callbacks xx\n");

	 ms=this_->mapsets->data;
	 this_->progress_cb=callback_new_attr_1(callback_cast(navit_map_progress), attr_progress, this_);
	 msh=mapset_open(ms);
	 while (msh && (map=mapset_next(msh, 0)))
	 {
	 //pass new callback instance for each map in the mapset to make map callback list destruction work correctly
	 struct callback *pcb = callback_new_attr_1(callback_cast(navit_map_progress), attr_progress, this_);
	 map_add_callback(map, pcb);
	 }
	 mapset_close(msh);
	 }
	 */

	/*
	 struct attr parent;
	 parent.type = attr_navit;
	 parent.u.navit = global_navit;

	 struct attr *attrs_r[2];
	 attrs_r[0] = NULL;
	 attrs_r[1] = NULL;
	 */

	//***this_->route = route_new(&parent, attrs_r);


	//int async = 0;
	//transform_setup_source_rect(this_->trans);
	//graphics_draw(this_->gra, this_->displaylist, this_->mapsets->data, this_->trans, this_->layout_current, async, NULL, this_->graphics_flags|1);

	if (this_->mapsets)
	{
		dbg(0, "005\n");

		struct displaylist *dl = navit_get_displaylist(this_);
		dbg(0, "005a dl=%p\n", dl);
		dbg(0, "005a1 ms=%p\n", this_->mapsets);
		dbg(0, "005a2 ms=%p\n", this_->mapsets->data);
		dl->ms = this_->mapsets->data;
		dbg(0, "005b\n");
		dl->m = NULL;
		dbg(0, "005c\n");
		dl->msh = NULL;

		dbg(0, "005.1\n");

		if (this_->route)
		{
			dbg(0, "005.2\n");

			struct mapset *ms;
			dbg(0, "005.3\n");
			ms = this_->mapsets->data;
			dbg(0, "005.4\n");
			route_set_mapset(this_->route, ms);

			dbg(0, "005.5\n");

			struct attr callback;
			this_->route_cb = callback_new_attr_1(callback_cast(navit_redraw_route), attr_route_status, this_);
			dbg(0, "005.6\n");
			callback_add_names(this_->route_cb, "navit_add_all_maps", "navit_redraw_route");
			dbg(0, "005.7\n");
			callback.type = attr_callback;
			dbg(0, "005.8\n");
			callback.u.callback = this_->route_cb;
			dbg(0, "005.9\n");
			route_add_attr(this_->route, &callback);
			dbg(0, "005.10\n");
			// ***** route_set_projection(this_->route, transform_get_projection(this_->trans));


		}


		dbg(0, "006\n");


		if (this_->tracking)
		{
			struct mapset *ms;
			ms = this_->mapsets->data;

			tracking_set_mapset(this_->tracking, ms);
			if (this_->route)
			{
				tracking_set_route(this_->tracking, this_->route);
			}
		}

	}

	// ready for drawing map
	// this_->ready = 3;

	// draw map
	// navit_draw(this_);

__F_END__
}

void navit_reload_maps(struct navit *this_)
{



	dbg(0,"ROUTExxPOSxx:navit_reload_maps:enter\n");

	navit_remove_all_maps(this_);
	navit_add_all_maps(this_);
}


// --- forward def ----
void navit_set_vehicle_position_to_screen_center(struct navit *this_);
// --- forward def ----

void navit_init(struct navit *this_)
{


	////DBG dbg(0,"EEnter\n");
	struct mapset *ms;
	struct map *map;
	int callback;
	char *center_file;

	// dbg(0,"GGGGG:set global_navit\n");
	// global_navit = this_;

	// default value
	navit_maps_dir = "/sdcard/zanavi/maps/";

	global_img_waypoint = NULL;

	//DBG dbg(0, "enter gui %p graphics %p\n", this_->gui, this_->gra);

	if (!this_->gui && !(this_->flags & 2))
	{
		dbg(0, "no gui\n");
		navit_destroy(this_);
		return;
	}

	if (!this_->gra && !(this_->flags & 1))
	{
		dbg(0, "no graphics\n");
		navit_destroy(this_);
		return;
	}


#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
	dbg(0, "DEST::route_clear_freetext_list(000)\n");
	route_clear_freetext_list();
#endif


	//DBG dbg(0, "Connecting gui to graphics\n");

	if (this_->gui && this_->gra && gui_set_graphics(this_->gui, this_->gra))
	{
		struct attr attr_type_gui, attr_type_graphics;
		gui_get_attr(this_->gui, attr_type, &attr_type_gui, NULL);
		graphics_get_attr(this_->gra, attr_type, &attr_type_graphics, NULL);

		dbg(0, "failed to connect to graphics\n");
		navit_destroy(this_);
		return;
	}

	if (this_->speech && this_->navigation)
	{
		struct attr speech;
		speech.type = attr_speech;
		speech.u.speech = this_->speech;
		navigation_set_attr(this_->navigation, &speech);
	}

	//DBG dbg(0, "Initializing graphics\n");
	//DBG dbg(0, "Setting Vehicle\n");
	navit_set_vehicle(this_, this_->vehicle);

	//DBG dbg(0, "Adding dynamic maps to mapset %p\n", this_->mapsets);
	if (this_->mapsets)
	{
		struct mapset_handle *msh;
		ms = this_->mapsets->data;
		// **D** // this_->progress_cb=callback_new_attr_1(callback_cast(navit_map_progress), attr_progress, this_);
		msh = mapset_open(ms);
		while (msh && (map = mapset_next(msh, 0)))
		{
			//pass new callback instance for each map in the mapset to make map callback list destruction work correctly
			// **D** // struct callback *pcb = callback_new_attr_1(callback_cast(navit_map_progress), attr_progress, this_);
			// **D** // map_add_callback(map, pcb);
		}
		mapset_close(msh);

		if (this_->route)
		{
			if ((map = route_get_map(this_->route)))
			{
				struct attr map_a, map_name;
				map_a.type = attr_map;
				map_a.u.map = map;
				map_name.type = attr_name;
				map_name.u.str = "_ms_route";
				map_set_attr(map_a.u.map, &map_name);
				mapset_add_attr(ms, &map_a);
			}

			if ((map = route_get_graph_map(this_->route)))
			{
				struct attr map_a, active, map_name;
				map_a.type = attr_map;
				map_a.u.map = map;
				active.type = attr_active;
				active.u.num = 0;
				map_name.type = attr_name;
				map_name.u.str = "_ms_route_graph";
				map_set_attr(map_a.u.map, &map_name);
				mapset_add_attr(ms, &map_a);
				map_set_attr(map, &active);
			}
			route_set_mapset(this_->route, ms);
			route_set_projection(this_->route, transform_get_projection(this_->trans));
		}

		if (this_->tracking)
		{
			tracking_set_mapset(this_->tracking, ms);
			if (this_->route)
			{
				tracking_set_route(this_->tracking, this_->route);
			}
		}

		if (this_->navigation)
		{
			if ((map = navigation_get_map(this_->navigation)))
			{
				struct attr map_a, active, map_name;
				map_a.type = attr_map;
				map_a.u.map = map;
				active.type = attr_active;
				active.u.num = 0;
				map_name.type = attr_name;
				map_name.u.str = "_ms_navigation";
				map_set_attr(map_a.u.map, &map_name);
				mapset_add_attr(ms, &map_a);
				map_set_attr(map, &active);
			}
		}

		if (this_->tracking)
		{
			if ((map = tracking_get_map(this_->tracking)))
			{
				struct attr map_a, active, map_name;
				map_a.type = attr_map;
				map_a.u.map = map;
				active.type = attr_active;
				active.u.num = 0;
				map_name.type = attr_name;
				map_name.u.str = "_ms_tracking";
				map_set_attr(map_a.u.map, &map_name);
				mapset_add_attr(ms, &map_a);
				map_set_attr(map, &active);
			}
		}
		// *DISABLED* navit_add_former_destinations_from_file(this_);
	}

	if (this_->route)
	{
/*
*DISABLED* done in "navit_add_all_maps"

		struct attr callback;
		this_->route_cb = callback_new_attr_1(callback_cast(navit_redraw_route), attr_route_status, this_);
		callback_add_names(this_->route_cb, "navit_init", "navit_redraw_route");
		callback.type = attr_callback;
		callback.u.callback = this_->route_cb;
		route_add_attr(this_->route, &callback);
*/
	}

	if (this_->navigation)
	{
		if (this_->speech)
		{
			this_->nav_speech_cb = callback_new_1(callback_cast(navit_speak), this_);
			callback_add_names(this_->nav_speech_cb, "navit_init", "navit_speak");
			navigation_register_callback(this_->navigation, attr_navigation_speech, this_->nav_speech_cb);
		}

		if (this_->route)
		{
			navigation_set_route(this_->navigation, this_->route);
		}
	}

	dbg(0, "Setting Center\n");
	center_file = bookmarks_get_center_file(FALSE);
	//dbg(0, "g0\n");
	bookmarks_set_center_from_file(this_->bookmarks, center_file);
	g_free(center_file);

	dbg(0, "Set Vehicle Position to Center\n");
	navit_set_vehicle_position_to_screen_center(this_);

#if 0
	if (this_->menubar)
	{
		men=menu_add(this_->menubar, "Data", menu_type_submenu, NULL);
		if (men)
		{
			navit_add_menu_windows_items(this_, men);
		}
	}
#endif

#if 0
	navit_window_roadbook_new(this_);
	navit_window_items_new(this_);
#endif

	//dbg(0, "g1\n");
	//messagelist_init(this_->messages);

	//dbg(0, "g2\n");
	navit_set_cursors(this_);

	callback_list_call_attr_1(this_->attr_cbl, attr_navit, this_);
	callback = (this_->ready == 2);
	dbg(0, "pre this_->ready=%d\n", this_->ready);
	this_->ready = this_->ready | 1;
	dbg(0, "set this_->ready=%d\n", this_->ready);
	////DBG dbg(0,"ready=%d\n",this_->ready);


	//if (this_->ready == 3)
	//{
	//	////DBG dbg(0,"navit_draw_async_003\n");
	//	navit_draw_async(this_, 1);
	//}

	dbg(0, "init ready=%d\n", this_->ready);

	// draw???????
	// dbg(0,"init DRAW 11\n");
	// ready to draw map
	// navit_draw(this_);
	// dbg(0,"init DRAW 22\n");
	// draw???????

	if (callback)
	{
		callback_list_call_attr_1(this_->attr_cbl, attr_graphics_ready, this_);
	}
#if 0
	routech_test(this_);
#endif
	//dbg(0, "1111111111\n");
}



// ----- forward def! -----
void navit_set_position_without_map_drawing(struct navit *this_, struct pcoord *c);
// ----- forward def! -----

void navit_set_vehicle_position_to_screen_center_only_for_route_struct(struct navit *this_)
{
	struct coord c;
	char *center_file;
	center_file = bookmarks_get_center_file(FALSE);
	bookmarks_get_center_from_file(this_->bookmarks, center_file, &c);
	g_free(center_file);

	struct pcoord pc;
	pc.x = c.x;
	pc.y = c.y;
	pc.pro = transform_get_projection(this_->trans);
	// result: pc.x, pc.y

	// set position
	navit_set_position_without_map_drawing(this_, &pc);
}

void navit_set_vehicle_position_to_screen_center(struct navit *this_)
{

	// map center to pixel x,y on screen
	// enum projection pro = transform_get_projection(this_->trans);
	// struct point pnt;
	// struct coord *c992;
	// c992 = transform_get_center(this_->trans);
	// transform(this_->trans, pro, c992, &pnt, 1, 0, 0, NULL);
	// dbg(0, "navit_set_vehicle_position_to_screen_center pnt.x=%d pnt.y=%d\n", pnt.x, pnt.y);
	// result: pnt.x, pnt.y


	// geo to pixel-on-screen
	// struct coord c99;
	// struct coord_geo g99;
	// g99.lat = lat;
	// g99.lng = lon;
	// dbg(0,"zzzzz %f, %f\n",a, b);
	// dbg(0,"yyyyy %f, %f\n",g99.lat, g99.lng);
	// transform_from_geo(projection_mg, &g99, &c99);
	// dbg(0,"%d %d %f %f\n",c99.x, c99.y, g99.lat, g99.lng);

	// enum projection pro = transform_get_projection(global_navit->trans_cursor);
	// struct point pnt;
	// transform(global_navit->trans, pro, &c99, &pnt, 1, 0, 0, NULL);
	// dbg(0,"x=%d\n",pnt.x);
	// dbg(0,"y=%d\n",pnt.y);


	struct coord c;
	char *center_file;
	center_file = bookmarks_get_center_file(FALSE);
	bookmarks_get_center_from_file(this_->bookmarks, center_file, &c);
	g_free(center_file);


	// coord to geo
	struct coord_geo g22;
	// struct coord c22;
	////DBG // dbg(0,"%f, %f\n",a, b);
	////DBG // dbg(0,"%d, %d\n",p.x, p.y);
	transform_to_geo(projection_mg, &c, &g22);
	dbg(0,"navit_set_vehicle_position_to_screen_center: %d, %d, %f, %f\n",c.x, c.y, g22.lat, g22.lng);
	// result = g_strdup_printf("%f:%f", g22.lat, g22.lng);


	// set vehicle position to pixel x,y
	//struct point p;
	//struct coord c;

	// pixel-x
	//p.x = pnt.x;
	// pixel-y
	//p.y = pnt.y;

	//transform_reverse(this_->trans, &p, &c);

	struct pcoord pc;
	pc.x = c.x;
	pc.y = c.y;
	pc.pro = transform_get_projection(this_->trans);
	// result: pc.x, pc.y

	// set position
	navit_set_position_without_map_drawing(this_, &pc);


	//center.pro = projection_screen;
	//center.x = 0;
	//center.y = 0;
	//DBG dbg(0, "veh new 5\n");
	// transform_setup(this_->vehicle->vehicle->trans, &pc, 16, 0);
	// transform_set_center(this_->vehicle->vehicle->trans, &c);
	// zzzzzzzzzzzzzz int vehicle_set_cursor_data_01(struct vehicle *this, struct point *pnt)

	if ((this_) && (this_->vehicle))
	{
		this_->vehicle->coord.x = c.x;
		this_->vehicle->coord.y = c.y;
	}

	if ((this_) && (this_->vehicle) && (this_->vehicle->vehicle))
	{
		float speed = 0;
		float direction = 0;
		double height = 0;
		float radius = 0;
		long gpstime = 0;
		vehicle_update_(this_->vehicle->vehicle, g22.lat, g22.lng, speed, direction, height, radius, gpstime);
	}
	else
	{
		dbg(0, "navit_set_vehicle_position_to_screen_center: no vehicle set !!\n");
	}
}




void navit_zoom_to_rect(struct navit *this_, struct coord_rect *r)
{
	struct coord c;
	int scale = 16;

	c.x = (r->rl.x + r->lu.x) / 2;
	c.y = (r->rl.y + r->lu.y) / 2;
	transform_set_center(this_->trans, &c);

	while (scale < 1 << 20)
	{
		struct point p1, p2;
		transform_set_scale(this_->trans, scale);
		transform_setup_source_rect(this_->trans);
		transform(this_->trans, transform_get_projection(this_->trans), &r->lu, &p1, 1, 0, 0, NULL);
		transform(this_->trans, transform_get_projection(this_->trans), &r->rl, &p2, 1, 0, 0, NULL);

		if (p1.x < 0 || p2.x < 0 || p1.x > this_->w || p2.x > this_->w || p1.y < 0 || p2.y < 0 || p1.y > this_->h || p2.y > this_->h)
		{
			scale *= 2;
		}
		else
		{
			break;
		}
	}

	dbg(0, "scale=%d\n", scale);
	dbg(0, "this_->ready=%d\n", this_->ready);

	if (this_->ready == 3)
	{
		// dbg(0,"navit_draw_async_004\n");
		//dbg(0,"DO__DRAW:navit_draw_async call\n");
		navit_draw_async(this_, 0);
	}
}

void navit_zoom_to_route(struct navit *this_, int orientation)
{
	struct map *map;
	struct map_rect *mr = NULL;
	struct item *item;
	struct coord c;
	struct coord_rect r;
	int count = 0;

	if (!this_->route)
	{
		return;
	}

	map = route_get_map(this_->route);

	if (map)
	{
		mr = map_rect_new(map, NULL);
	}

	if (mr)
	{
		while ((item = map_rect_get_item(mr)))
		{
			while (item_coord_get(item, &c, 1))
			{
				if (!count)
				{
					r.lu = r.rl = c;
				}
				else
				{
					coord_rect_extend(&r, &c);
				}
				count++;
			}
		}
		map_rect_destroy(mr);
	}

	if (!count)
	{
		return;
	}

	if (orientation != -1)
	{
		transform_set_yaw(this_->trans, orientation);
	}

	// if overspill > 1 ?
	if (global_overspill_factor > 1.0f)
	{
		coord_rect_extend_by_percent(&r, (global_overspill_factor - 1.0f));
	}

	navit_zoom_to_rect(this_, &r);
}

static void navit_cmd_zoom_to_route(struct navit *this)
{


	////DBG dbg(0,"EEnter\n");
	navit_zoom_to_route(this, 0);
}

/**
 * show point on map
 *
 * @param navit The navit instance
 * @param center The point where to center the map, including its projection
 * @returns nothing
 */
void navit_set_center(struct navit *this_, struct pcoord *center, int set_timeout)
{


	////DBG dbg(0,"EEnter\n");
	struct coord *c = transform_center(this_->trans);
	struct coord c1, c2;
	enum projection pro = transform_get_projection(this_->trans);

	if (pro != center->pro)
	{
		c1.x = center->x;
		c1.y = center->y;
		transform_from_to(&c1, center->pro, &c2, pro);
	}
	else
	{
		c2.x = center->x;
		c2.y = center->y;
	}

	*c = c2;
	if (set_timeout)
	{
		navit_set_timeout(this_);
	}

	if (this_->ready == 3)
	{
		navit_draw(this_);
	}
}


void navit_set_center_no_draw(struct navit *this_, struct pcoord *center, int set_timeout)
{


	////DBG dbg(0,"EEnter\n");
	struct coord *c = transform_center(this_->trans);
	struct coord c1, c2;
	enum projection pro = transform_get_projection(this_->trans);

	if (pro != center->pro)
	{
		c1.x = center->x;
		c1.y = center->y;
		transform_from_to(&c1, center->pro, &c2, pro);
	}
	else
	{
		c2.x = center->x;
		c2.y = center->y;
	}

	*c = c2;
	if (set_timeout)
	{
		navit_set_timeout(this_);
	}
}

static void navit_set_center_coord_screen(struct navit *this_, struct coord *c, struct point *p, int set_timeout)
{


	////DBG dbg(0,"EEnter\n");
	int width, height;
	struct point po;
	transform_set_center(this_->trans, c);
	transform_get_size(this_->trans, &width, &height);
	po.x = width / 2;
	po.y = height / 2;
	update_transformation(this_->trans, &po, p, NULL);
	if (set_timeout)
	{
		navit_set_timeout(this_);
	}
}

/**
 * Links all vehicles to a cursor depending on the current profile.
 *
 * @param this_ A navit instance
 * @author Ralph Sennhauser (10/2009)
 */
void navit_set_cursors(struct navit *this_)
{


	struct attr name;
	struct navit_vehicle *nv;
	struct cursor *c;
	GList *v;

	//dbg(0, "Enter\n");

	v = g_list_first(this_->vehicles); // GList of navit_vehicles
	while (v)
	{
		dbg(0, "* found vehicle *\n");
		nv = v->data;
		if (vehicle_get_attr(nv->vehicle, attr_cursorname, &name, NULL))
		{
			if (!strcmp(name.u.str, "none"))
			{
				c = NULL;
			}
			else
			{
				c = layout_get_cursor(this_->layout_current, name.u.str);
			}
		}
		else
		{
			c = layout_get_cursor(this_->layout_current, "default");
		}
		vehicle_set_cursor(nv->vehicle, c, 0);
		v = g_list_next(v);
	}
	return;
}

void navit_remove_cursors(struct navit *this_)
{


	struct attr name;
	struct navit_vehicle *nv;
	struct cursor *c;
	GList *v;

	//dbg(0, "Enter\n");
	name.type = attr_cursor;

	v = g_list_first(this_->vehicles); // GList of navit_vehicles
	while (v)
	{
		dbg(0, "* found vehicle *\n");
		nv = v->data;
		vehicle_remove_attr(nv->vehicle, &name);
		v = g_list_next(v);
	}
	return;
}

static int navit_get_cursor_pnt(struct navit *this_, struct point *p, int keep_orientation, int *dir)
{
	//// dbg(0,"EEnter\n");

	int width, height;
	struct navit_vehicle *nv = this_->vehicle;

	// valid values:  0 - 50 (0 -> center of screen, 50 -> bottom of screen)
	// float offset = this_->radius; // Cursor offset from the center of the screen (percent). // percent of what??

	float offset = 0;


#if 0

/*  Better improve track.c to get that issue resolved or make it configurable with being off the default, the jumping back to the center is a bit annoying */
	float min_offset = 0.; // Percent offset at min_offset_speed.
	float max_offset = 30.; // Percent offset at max_offset_speed.
	int min_offset_speed = 2; // Speed in km/h
	int max_offset_speed = 50; // Speed ini km/h
	// Calculate cursor offset from the center of the screen, upon speed.
	if (nv->speed <= min_offset_speed)
	{
		offset = min_offset;
	}
	else if (nv->speed > max_offset_speed)
	{
		offset = max_offset;
	}
	else
	{
		offset = (max_offset - min_offset) / (max_offset_speed - min_offset_speed) * (nv->speed - min_offset_speed);
	}

#endif

	transform_get_size(this_->trans, &width, &height);

	if (height == 0)
	{
		offset = 0;
	}
	else
	{
		// dbg(0, "VEHICLE_OFFSET:a:r=%d %d %d\n", (int)this_->radius, (int)(height - this_->radius), (int)((height - this_->radius) - (height / 2)));
		offset = (float)((height - this_->radius) - (height / 2)) / (float)height * (float)50.0;
		// dbg(0, "VEHICLE_OFFSET:b:o=%d\n", (int)offset);
	}


	// dbg(0, "VEHICLE_OFFSET:or=%d keep_or=%d\n", this_->orientation, keep_orientation);

	if (this_->orientation == -1 || keep_orientation)
	{
		p->x = 50 * width / 100; // = (width / 2) // why doesnt it just say that?
		p->y = (50 + (int)offset) * height / 100;

		// dbg(0, "VEHICLE_OFFSET:2:%d %d %d\n", p->y, width, height);

		if (dir)
		{
			*dir = keep_orientation ? this_->orientation : nv->dir;
		}
	}
	else
	{
		int mdir;
		if (this_->tracking && this_->tracking_flag)
		{
			mdir = tracking_get_angle(this_->tracking) - this_->orientation;
			// dbg(0, "+++++tr angle=%d\n", tracking_get_angle(this_->tracking));
			// dbg(0, "+++++this ori=%d\n", this_->orientation);
		}
		else
		{
			mdir = nv->dir - this_->orientation;
		}

		p->x = (50 - offset * sin(M_PI * mdir / 180.)) * width / 100;
		p->y = (50 + offset * cos(M_PI * mdir / 180.)) * height / 100;

		// dbg(0, "VEHICLE_OFFSET:3:%d %d %d\n", p->y, width, height);

		if (dir)
		{
			*dir = this_->orientation;
		}
	}
	return 1;
}

void navit_set_center_cursor(struct navit *this_, int autozoom, int keep_orientation)
{


	////DBG dbg(0,"EEnter\n");
	int dir;
	struct point pn;
	struct navit_vehicle *nv = this_->vehicle;
	navit_get_cursor_pnt(this_, &pn, keep_orientation, &dir);
	transform_set_yaw(this_->trans, dir);
	navit_set_center_coord_screen(this_, &nv->coord, &pn, 0);
	// OLD // navit_aXXutozoom(this_, &nv->coord, nv->speed, 0);
}

static void navit_set_center_cursor_draw(struct navit *this_)
{


	//dbg(0,"EEnter\n");
	navit_set_center_cursor(this_, 1, 0);
	if (this_->ready == 3)
	{
		// dbg(0,"navit_draw_async_005\n");
		//dbg(0,"DO__DRAW:navit_draw_async call (AS)\n");
		// zzz554 // navit_draw_async(this_, 1);
	}
}

static void navit_cmd_set_center_cursor(struct navit *this_)
{


	////DBG dbg(0,"EEnter\n");
	navit_set_center_cursor_draw(this_);
}

void navit_set_center_screen(struct navit *this_, struct point *p, int set_timeout)
{


	////DBG dbg(0,"EEnter\n");
	struct coord c;
	struct pcoord pc;
	transform_reverse(this_->trans, p, &c);
	pc.x = c.x;
	pc.y = c.y;
	pc.pro = transform_get_projection(this_->trans);
	navit_set_center(this_, &pc, set_timeout);
}

#if 0
switch((*attrs)->type)
{
	case attr_zoom:
	zoom=(*attrs)->u.num;
	break;
	case attr_center:
	g=*((*attrs)->u.coord_geo);
	break;
#endif

static int navit_set_attr_do(struct navit *this_, struct attr *attr, int init)
{

	int dir = 0, orient_old = 0, attr_updated = 0;
	struct coord co;
	long zoom;
	GList *l;
	struct navit_vehicle *nv;
	struct layout *lay;
	struct attr active;
	active.type = attr_active;
	active.u.num = 0;

	switch (attr->type)
	{
		case attr_autozoom:
			attr_updated = (this_->autozoom_secs != attr->u.num);
			this_->autozoom_secs = attr->u.num;
			break;
		case attr_autozoom_active:
			attr_updated = (this_->autozoom_active != attr->u.num);
			this_->autozoom_active = attr->u.num;
			break;
		case attr_center:
			transform_from_geo(transform_get_projection(this_->trans), attr->u.coord_geo, &co);
			// dbg(1, "0x%x,0x%x\n", co.x, co.y);
			transform_set_center(this_->trans, &co);
			break;
		case attr_drag_bitmap:
			attr_updated = (this_->drag_bitmap != !!attr->u.num);
			this_->drag_bitmap = !!attr->u.num;
			break;
		case attr_flags:
			attr_updated = (this_->flags != attr->u.num);
			this_->flags = attr->u.num;
			break;
		case attr_flags_graphics:
			attr_updated = (this_->graphics_flags != attr->u.num);
			this_->graphics_flags = attr->u.num;
			break;
		case attr_follow:
			if (!this_->vehicle)
				return 0;
			attr_updated = (this_->vehicle->follow_curr != attr->u.num);
			this_->vehicle->follow_curr = attr->u.num;
			break;
		case attr_layout:
			if (this_->layout_current != attr->u.layout)
			{
				this_->layout_current = attr->u.layout;
				graphics_font_destroy_all(this_->gra);
				navit_set_cursors(this_);
				if (this_->ready == 3)
					navit_draw(this_);
				attr_updated = 1;
			}
			break;
		case attr_layout_name:
			l = this_->layouts;
			while (l)
			{
				lay = l->data;
				if (!strcmp(lay->name, attr->u.str))
				{
					struct attr attr;
					attr.type = attr_layout;
					attr.u.layout = lay;
					return navit_set_attr_do(this_, &attr, init);
				}
				l = g_list_next(l);
			}
			return 0;
		case attr_map_border:
			if (this_->border != attr->u.num)
			{
				this_->border = attr->u.num;
				attr_updated = 1;
			}
			break;
		case attr_orientation:
			orient_old = this_->orientation;
			this_->orientation = attr->u.num;
			if (!init)
			{
				if (this_->orientation != -1)
				{
					dir = this_->orientation;
				}
				else
				{
					if (this_->vehicle)
					{
						dir = this_->vehicle->dir;
					}
				}
				transform_set_yaw(this_->trans, dir);
				if (orient_old != this_->orientation)
				{
#if 0
					if (this_->ready == 3)
					navit_draw(this_);
#endif
					attr_updated = 1;
				}
			}
			break;
		case attr_osd_configuration:
			//DBG dbg(0, "setting osd_configuration to %d (was %d)\n", attr->u.num, this_->osd_configuration);
			attr_updated = (this_->osd_configuration != attr->u.num);
			this_->osd_configuration = attr->u.num;
			break;
		case attr_pitch:
			attr_updated = (this_->pitch != attr->u.num);
			this_->pitch = attr->u.num;
			transform_set_pitch(this_->trans, this_->pitch);
			if (!init && attr_updated && this_->ready == 3)
				navit_draw(this_);
			break;
		case attr_projection:
			if (this_->trans && transform_get_projection(this_->trans) != attr->u.projection)
			{
				navit_projection_set(this_, attr->u.projection, !init);
				attr_updated = 1;
			}
			break;
		case attr_radius:
			attr_updated = (this_->radius != attr->u.num);
			this_->radius = attr->u.num;
			break;
		case attr_recent_dest:
			attr_updated = (this_->recentdest_count != attr->u.num);
			this_->recentdest_count = attr->u.num;
			break;
		case attr_speech:
			if (this_->speech && this_->speech != attr->u.speech)
			{
				attr_updated = 1;
				this_->speech = attr->u.speech;
			}
			break;
		case attr_timeout:
			attr_updated = (this_->center_timeout != attr->u.num);
			this_->center_timeout = attr->u.num;
			break;
		case attr_tracking:
			attr_updated = (this_->tracking_flag != !!attr->u.num);
			// dbg(0, "set attr:attr_tracking old=%d\n", this_->tracking_flag);
			this_->tracking_flag = !!attr->u.num;
			// dbg(0, "set attr:attr_tracking new=%d\n", this_->tracking_flag);
			break;
		case attr_transformation:
			this_->trans = attr->u.transformation;
			break;
		case attr_use_mousewheel:
			attr_updated = (this_->use_mousewheel != !!attr->u.num);
			this_->use_mousewheel = !!attr->u.num;
			break;
		case attr_vehicle:
			l = this_->vehicles;
			while (l)
			{
				nv = l->data;
				if (nv->vehicle == attr->u.vehicle)
				{
					if (!this_->vehicle || this_->vehicle->vehicle != attr->u.vehicle)
					{
						if (this_->vehicle)
						{
							vehicle_set_attr(this_->vehicle->vehicle, &active);
						}
						active.u.num = 1;
						vehicle_set_attr(nv->vehicle, &active);
						attr_updated = 1;
					}
					navit_set_vehicle(this_, nv);
				}
				l = g_list_next(l);
			}
			break;
		case attr_zoom:
			zoom = transform_get_scale(this_->trans);
			attr_updated = (zoom != attr->u.num);
			transform_set_scale(this_->trans, attr->u.num);
			if (attr_updated && !init)
				navit_draw(this_);
			break;
		case attr_zoom_min:
			attr_updated = (attr->u.num != this_->zoom_min);
			this_->zoom_min = attr->u.num;
			break;
		case attr_zoom_max:
			attr_updated = (attr->u.num != this_->zoom_max);
			this_->zoom_max = attr->u.num;
			break;
		case attr_message:
			//navit_add_message(this_, attr->u.str);
			break;
		case attr_follow_cursor:
			attr_updated = (this_->follow_cursor != !!attr->u.num);
			this_->follow_cursor = !!attr->u.num;
			break;
		case attr_imperial:
			attr_updated = (this_->imperial != attr->u.num);
			this_->imperial = attr->u.num;
			break;
		default:
			return 0;
	}

	if (attr_updated && !init)
	{
		// dbg(0, "set attr:call callback_list_call_attr_2\n");
		callback_list_call_attr_2(this_->attr_cbl, attr->type, this_, attr);

		if (attr->type == attr_osd_configuration)
		{
			graphics_draw_mode(this_->gra, draw_mode_end);
		}
	}

	return 1;
}

int navit_set_attr(struct navit *this_, struct attr *attr)
{
	return navit_set_attr_do(this_, attr, 0);
}

int navit_get_attr(struct navit *this_, enum attr_type type, struct attr *attr, struct attr_iter *iter)
{

	struct message *msg;
	int len, offset;
	int ret = 1;

	switch (type)
	{
		case attr_message:
			return 0;
			/*
			 msg = navit_get_messages(this_);

			 if (!msg)
			 {
			 return 0;
			 }

			 len = 0;
			 while (msg)
			 {
			 len += strlen(msg->text) + 1;
			 msg = msg->next;
			 }
			 attr->u.str = g_malloc(len + 1);

			 msg = navit_get_messages(this_);
			 offset = 0;
			 while (msg)
			 {
			 g_stpcpy((attr->u.str + offset), msg->text);
			 offset += strlen(msg->text);
			 attr->u.str[offset] = '\n';
			 offset++;

			 msg = msg->next;
			 }

			 attr->u.str[len] = '\0';
			 */
			break;
		case attr_imperial:
			attr->u.num = this_->imperial;
			break;
		case attr_bookmark_map:
			attr->u.map = bookmarks_get_map(this_->bookmarks);
			break;
		case attr_bookmarks:
			attr->u.bookmarks = this_->bookmarks;
			break;
		case attr_callback_list:
			attr->u.callback_list = this_->attr_cbl;
			break;
		case attr_destination:
			if (!this_->destination_valid)
				return 0;
			attr->u.pcoord = &this_->destination;
			break;
		case attr_displaylist:
			attr->u.displaylist = this_->displaylist;
			return (attr->u.displaylist != NULL);
		case attr_follow:
			if (!this_->vehicle)
				return 0;
			attr->u.num = this_->vehicle->follow_curr;
			break;
		case attr_former_destination_map:
			attr->u.map = this_->former_destination;
			break;
		case attr_graphics:
			attr->u.graphics = this_->gra;
			ret = (attr->u.graphics != NULL);
			break;
		case attr_gui:
			attr->u.gui = this_->gui;
			ret = (attr->u.gui != NULL);
			break;
		case attr_layout:
			if (iter)
			{
				if (iter->u.list)
				{
					iter->u.list = g_list_next(iter->u.list);
				}
				else
				{
					iter->u.list = this_->layouts;
				}
				if (!iter->u.list)
				{
					return 0;
				}
				attr->u.layout = (struct layout *) iter->u.list->data;
			}
			else
			{
				attr->u.layout = this_->layout_current;
			}
			break;
		case attr_map:
			if (iter && this_->mapsets)
			{
				if (!iter->u.mapset_handle)
				{
					iter->u.mapset_handle = mapset_open((struct mapset *) this_->mapsets->data);
				}
				attr->u.map = mapset_next(iter->u.mapset_handle, 0);
				if (!attr->u.map)
				{
					mapset_close(iter->u.mapset_handle);
					return 0;
				}
			}
			else
			{
				return 0;
			}
			break;
		case attr_mapset:
			attr->u.mapset = this_->mapsets->data;
			ret = (attr->u.mapset != NULL);
			break;
		case attr_navigation:
			attr->u.navigation = this_->navigation;
			break;
		case attr_orientation:
			attr->u.num = this_->orientation;
			break;
		case attr_osd_configuration:
			attr->u.num = this_->osd_configuration;
			break;
		case attr_pitch:
			attr->u.num = transform_get_pitch(this_->trans);
			break;
		case attr_projection:
			if (this_->trans)
			{
				attr->u.num = transform_get_projection(this_->trans);
			}
			else
			{
				return 0;
			}
			break;
		case attr_route:
			attr->u.route = this_->route;
			break;
		case attr_speech:
			attr->u.speech = this_->speech;
			break;
		case attr_tracking:
			attr->u.num = this_->tracking_flag;
			break;
		case attr_trackingo:
			attr->u.tracking = this_->tracking;
			break;
		case attr_transformation:
			attr->u.transformation = this_->trans;
			break;
		case attr_vehicle:
			if (iter)
			{
				if (iter->u.list)
				{
					iter->u.list = g_list_next(iter->u.list);
				}
				else
				{
					iter->u.list = this_->vehicles;
				}
				if (!iter->u.list)
					return 0;
				attr->u.vehicle = ((struct navit_vehicle*) iter->u.list->data)->vehicle;
			}
			else
			{
				if (this_->vehicle)
				{
					attr->u.vehicle = this_->vehicle->vehicle;
				}
				else
				{
					return 0;
				}
			}
			break;
		case attr_vehicleprofile:
			attr->u.vehicleprofile = this_->vehicleprofile;
			break;
		case attr_zoom:
			attr->u.num = transform_get_scale(this_->trans);
			break;
		case attr_autozoom_active:
			attr->u.num = this_->autozoom_active;
			break;
		case attr_follow_cursor:
			attr->u.num = this_->follow_cursor;
			break;
		default:
			return 0;
	}
	attr->type = type;

	return ret;
}

void displaylist_shift_order_in_map_layers(struct navit *this_, int shift_value)
{



	GList *l;
	struct layout *lay;
	GList *l2;
	struct layer *layer;
	GList *ig;
	struct itemgra *itemgr;
	GList *elements;
	struct element *e;

	// loop through all the layouts
	l = this_->layouts;
	while (l)
	{
		lay = l->data;
		//dbg(0,"layout name=%s\n", lay->name);
		if (!strcmp(lay->name, "Android-Car"))
		{
			//dbg(0,"layout found\n");
			l2 = lay->layers;
			while (l2)
			{
				layer = l2->data;
				//dbg(0,"layer name=%s\n", layer->name);
				// only change the zoom of these layers
				if ((!strcmp(layer->name, "polygons001"))
					|| (!strcmp(layer->name, "polygons"))
					|| (!strcmp(layer->name, "streets"))
					|| (!strcmp(layer->name, "streets_STR_ONLY"))
					|| (!strcmp(layer->name, "streets_1"))
					|| (!strcmp(layer->name, "streets_1_STR_ONLY"))
					|| (!strcmp(layer->name, "streets_2"))
					|| (!strcmp(layer->name, "streets_2_STR_ONLY"))
					|| (!strcmp(layer->name, "route_001"))
					|| (!strcmp(layer->name, "route_002"))
					|| (!strcmp(layer->name, "route_003"))
					)
				{
					//dbg(0,"layer found\n");
					ig = layer->itemgras;
					while (ig)
					{
						//dbg(0,"*itgr*\n");
						itemgr = ig->data;

						// now shift "order"-value of itemgra by "shift_value"
						// ! max order == 18 !
						// ! min order == -2 !


						int was_shifted = 0;

						//if (itemgr->order.min < 20)
						//{
						itemgr->order.min = itemgr->order.min - shift_value;
						was_shifted = 1;
						//}
						//if (itemgr->order.min < -2)
						//{
						//	itemgr->order.min = -2;
						//}
						if (itemgr->order.min > 18)
						{
							itemgr->order.min = 18;
						}

						// ------------------------------

						if (itemgr->order.max < 18)
						{
							itemgr->order.max = itemgr->order.max - shift_value;
							was_shifted = 1;
						}
						//
						//if (itemgr->order.max < -2)
						//{
						//	itemgr->order.max = -2;
						//}
						if (itemgr->order.max > 18)
						{
							itemgr->order.max = 18;
						}

						float sv_001 = ((float) shift_value * 1.34f);
						float sv_002 = ((float) shift_value * 0.75f);

						if (was_shifted == 1)
						{
							// loop thru all the elements in this "itemgra"
							elements = itemgr->elements;
							while (elements)
							{
								e = elements->data;

								if (e->type == element_polyline)
								{
									// shift polyline width
									e->u.polyline.width = ((float) e->u.polyline.width / sv_001) + 0;
									if (e->u.polyline.width < 1)
									{
										e->u.polyline.width = 1;
									}
								}

								if (e->type == element_circle)
								{
									// shift circle witdh
									e->u.circle.width = ((float) e->u.circle.width / sv_001) + 0;
									if (e->u.circle.width < 1)
									{
										e->u.circle.width = 1;
									}

									e->u.circle.radius = ((float) e->u.circle.radius / sv_001) + 0;
									if (e->u.circle.radius < 1)
									{
										e->u.circle.radius = 1;
									}
								}

								if (e->type == element_text)
								{
									// shift text size
									e->text_size = (float) e->text_size / sv_002;
									if (e->text_size < 1)
									{
										e->text_size = 1;
									}
								}
								elements = g_list_next(elements);
							}
							// loop thru all the elements in this "itemgra"
						}

						ig = g_list_next(ig);
					}
				}
				l2 = g_list_next(l2);
			}
		}
		l = g_list_next(l);
	}

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif
}

void displaylist_shift_for_dpi_value_in_layers(struct navit *this_, double factor)
{



	GList *l;
	struct layout *lay;
	GList *l2;
	struct layer *layer;
	GList *ig;
	struct itemgra *itemgr;
	GList *elements;
	struct element *e;

	// loop through all the layouts
	l = this_->layouts;
	while (l)
	{
		lay = l->data;
		//dbg(0,"layout name=%s\n", lay->name);
		if (!strcmp(lay->name, "Android-Car"))
		{
			//dbg(0,"layout found\n");
			l2 = lay->layers;
			while (l2)
			{
				layer = l2->data;
				//dbg(0,"layer name=%s\n", layer->name);
				// only change the zoom of these layers
/*
				if ((!strcmp(layer->name, "polygons001"))
					|| (!strcmp(layer->name, "polygons"))
					|| (!strcmp(layer->name, "streets"))
					|| (!strcmp(layer->name, "streets_1"))
					|| (!strcmp(layer->name, "streets_2"))
					|| (!strcmp(layer->name, "route_001"))
					|| (!strcmp(layer->name, "route_002"))
					|| (!strcmp(layer->name, "route_003"))
					)
*/
				//{
					//dbg(0,"layer found\n");
					ig = layer->itemgras;
					while (ig)
					{
						//dbg(0,"*itgr*\n");
						itemgr = ig->data;

							// loop thru all the elements in this "itemgra"
							elements = itemgr->elements;
							while (elements)
							{
								e = elements->data;

								if (e->type == element_polyline)
								{
									// polyline width
									e->u.polyline.width = (int)((float)e->u.polyline.width * factor) + 0;
									if (e->u.polyline.width < 1)
									{
										e->u.polyline.width = 1;
									}
								}

								if (e->type == element_circle)
								{
									// circle witdh
									e->u.circle.width = (int)((float)e->u.circle.width * factor) + 0;
									if (e->u.circle.width < 1)
									{
										e->u.circle.width = 1;
									}

									e->u.circle.radius = (int)((float)e->u.circle.radius * factor) + 0;
									if (e->u.circle.radius < 1)
									{
										e->u.circle.radius = 1;
									}

									// text size
									e->text_size = (int)((float)(e->text_size * factor));
									if (e->text_size < 1)
									{
										e->text_size = 1;
									}

								}

								if (e->type == element_text)
								{
									// text size
									e->text_size = (int)((float)(e->text_size * factor));
									if (e->text_size < 1)
									{
										e->text_size = 1;
									}
								}
								elements = g_list_next(elements);
							}
							// loop thru all the elements in this "itemgra"
						ig = g_list_next(ig);
					}
				//}
				l2 = g_list_next(l2);
			}
		}
		l = g_list_next(l);
	}

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif
}


static int navit_add_log(struct navit *this_, struct log *log)
{


	struct attr type_attr;
	if (!log_get_attr(log, attr_type, &type_attr, NULL))
		return 0;
	if (!strcmp(type_attr.u.str, "textfile_debug"))
	{
		char *header = "type=track_tracked\n";
		if (this_->textfile_debug_log)
			return 0;
		log_set_header(log, header, strlen(header));
		this_->textfile_debug_log = log;
		return 1;
	}
	return 0;
}

static int navit_add_layout(struct navit *this_, struct layout *layout)
{


	////DBG dbg(0,"EEnter\n");
	struct attr active;
	this_->layouts = g_list_append(this_->layouts, layout);
	layout_get_attr(layout, attr_active, &active, NULL);
	if (active.u.num || !this_->layout_current)
	{
		this_->layout_current = layout;
		return 1;
	}
	return 0;
}

int navit_add_attr(struct navit *this_, struct attr *attr)
{


	////DBG dbg(0,"EEnter\n");

	int ret = 1;
	switch (attr->type)
	{
		case attr_callback:
			navit_add_callback(this_, attr->u.callback);
			break;
		case attr_log:
			ret = navit_add_log(this_, attr->u.log);
			break;
		case attr_gui:
			ret = navit_set_gui(this_, attr->u.gui);
			break;
		case attr_graphics:
			ret = navit_set_graphics(this_, attr->u.graphics);
			break;
		case attr_layout:
			ret = navit_add_layout(this_, attr->u.layout);
			break;
		case attr_route:
			this_->route = attr->u.route;
			break;
		case attr_mapset:
			this_->mapsets = g_list_append(this_->mapsets, attr->u.mapset);
			break;
		case attr_navigation:
			this_->navigation = attr->u.navigation;
			break;
		case attr_recent_dest:
			this_->recentdest_count = attr->u.num;
			break;
		case attr_speech:
			this_->speech = attr->u.speech;
			break;
		case attr_tracking:
			this_->tracking = attr->u.tracking;
			break;
		case attr_vehicle:
			ret = navit_add_vehicle(this_, attr->u.vehicle);
			break;
		case attr_vehicleprofile:
			this_->vehicleprofiles = g_list_prepend(this_->vehicleprofiles, attr->u.vehicleprofile);
			break;
		case attr_autozoom_min:
			this_->autozoom_min = attr->u.num;
			break;
		default:
			return 0;
	}
	callback_list_call_attr_2(this_->attr_cbl, attr->type, this_, attr);

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return ret;
}

int navit_remove_attr(struct navit *this_, struct attr *attr)
{


	int ret = 1;
	switch (attr->type)
	{
		case attr_callback:
			navit_remove_callback(this_, attr->u.callback);
			break;
		default:
			return 0;
	}

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return ret;
}

struct attr_iter *
navit_attr_iter_new(void)
{


return g_new0(struct attr_iter, 1);
}

void navit_attr_iter_destroy(struct attr_iter *iter)
{


	g_free(iter);
}

void navit_add_callback(struct navit *this_, struct callback *cb)
{


	////DBG dbg(0,"EEnter\n");

	callback_list_add(this_->attr_cbl, cb);
}

void navit_remove_callback(struct navit *this_, struct callback *cb)
{


	////DBG dbg(0,"EEnter\n");

	callback_list_remove(this_->attr_cbl, cb);
}

/**
 * Toggle the cursor update : refresh the map each time the cursor has moved (instead of only when it reaches a border)
 *
 * @param navit The navit instance
 * @returns nothing
 */
static void navit_vehicle_draw(struct navit *this_, struct navit_vehicle *nv, struct point *pnt)
{
__F_START__

	struct point cursor_pnt;
	enum projection pro;

	if (this_->blocked)
	{
		return2;
	}

	if (pnt)
	{
		cursor_pnt = *pnt;
	}
	else
	{
		pro = transform_get_projection(this_->trans_cursor);
		if (!pro)
		{
			return2;
		}
		transform(this_->trans_cursor, pro, &nv->coord, &cursor_pnt, 1, 0, 0, NULL);
	}

	//dbg(0,"xx=%d\n",cursor_pnt.x);
	//dbg(0,"yy=%d\n",cursor_pnt.y);

	global_vehicle_pos_onscreen.x = cursor_pnt.x;
	global_vehicle_pos_onscreen.y = cursor_pnt.y;

	//dbg(0,"xx=%d\n",cursor_pnt.x);
	//dbg(0,"yy=%d\n",cursor_pnt.y);
	//dbg(0,"vehicle_draw_001\n");
	vehicle_draw(nv->vehicle, this_->gra, &cursor_pnt, 0, nv->dir - transform_get_yaw(this_->trans_cursor), nv->speed);
#if 0	
	if (pnt)
	pnt2=*pnt;
	else
	{
		pro=transform_get_projection(this_->trans);
		transform(this_->trans, pro, &nv->coord, &pnt2, 1);
	}
#if 1
	cursor_draw(nv->cursor, &pnt2, nv->dir-transform_get_angle(this_->trans, 0), nv->speed > 2, pnt == NULL);
#else
	cursor_draw(nv->cursor, &pnt2, nv->dir-transform_get_angle(this_->trans, 0), nv->speed > 2, 1);
#endif
#endif

__F_END__
}



// --- this gets called at every positon update (from GPS, or demo vehicle!!) !! ------
// --- this gets called at every positon update (from GPS, or demo vehicle!!) !! ------
// --- this gets called at every positon update (from GPS, or demo vehicle!!) !! ------
static void navit_vehicle_update(struct navit *this_, struct navit_vehicle *nv)
{

__F_START__

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "\n");
	dbg(0, "==================================================================================\n");
	dbg(0, "==================================================================================\n");
	dbg(0, "\n");
#endif


#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:navit_vehicle_update:enter\n");
#endif

#ifdef NAVIT_MEASURE_TIME_DEBUG
	clock_t s_ = debug_measure_start();
#endif

	struct attr attr_valid, attr_dir, attr_speed, attr_pos;
	struct pcoord cursor_pc;
	struct point cursor_pnt, *pnt = &cursor_pnt;
	struct point old_cursor_pnt;
	struct tracking *tracking = NULL;
	struct pcoord pc[16];

	enum projection pro = transform_get_projection(this_->trans_cursor);

	int count;
	int old_dir;
	int old_pos_invalid;
	int (*get_attr)(void *, enum attr_type, struct attr *, struct attr_iter *);
	void *attr_object;
	char *destination_file;
	long new_scale_value;
	long old_scale_value;
	int l_old;
	int l_new;

	if (this_->ready != 3)
	{
		//profile(0,"return 1\n");
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:return 003\n");
#endif


#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "\n");
		dbg(0, "==================================================================================\n");
		dbg(0, "==================================================================================\n");
		dbg(0, "\n");
#endif

		return2;
	}

	// **OLD** navit_layout_switch(this_);
	if (this_->vehicle == nv && this_->tracking_flag)
	{
		tracking = this_->tracking;
	}
	//// else tracking = NULL !! -> important for next "if" clause!!

	// ------ DEBUG ------ remember real GPS postion (unchanged) -------
	// ------ DEBUG ------ remember real GPS postion (unchanged) -------
	// ------ DEBUG ------ remember real GPS postion (unchanged) -------
	struct attr attr_pos_unchanged;
	if (vehicle_get_attr(nv->vehicle, attr_position_coord_geo, &attr_pos_unchanged, NULL))
	{
		if (attr_pos_unchanged.u.coord_geo)
		{
			global_v_pos_lat = attr_pos_unchanged.u.coord_geo->lat;
			global_v_pos_lng = attr_pos_unchanged.u.coord_geo->lng;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
			struct coord cc999;
			transform_from_geo(pro, attr_pos_unchanged.u.coord_geo, &cc999);
			dbg(0, "ROUTExxPOSxx:navit_vehicle_update: %d %d\n", cc999.x, cc999.y);
#endif

		}
	}

	if (vehicle_get_attr(nv->vehicle, attr_position_direction, &attr_pos_unchanged, NULL))
	{
		global_v_pos_dir = *attr_pos_unchanged.u.numd;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:navit_vehicle_update: dir=%f\n", (float)global_v_pos_dir);
#endif
	}
	// ------ DEBUG ------ remember real GPS postion (unchanged) -------
	// ------ DEBUG ------ remember real GPS postion (unchanged) -------
	// ------ DEBUG ------ remember real GPS postion (unchanged) -------


	if (tracking)
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:tracking_update: 001\n");
#endif

		// set postition from tracking (changing it to nearest street)
		tracking_update(tracking, nv->vehicle, this_->vehicleprofile, pro);
		attr_object = tracking;
		get_attr = (int(*)(void *, enum attr_type, struct attr *, struct attr_iter *)) tracking_get_attr;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:tracking_update: 001-a\n");
#endif
	}
	else
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:tracking_update (unchanged): 001.bb\n");
#endif

		// set position from vehicle (unchanged)
		attr_object = nv->vehicle;
		get_attr = (int(*)(void *, enum attr_type, struct attr *, struct attr_iter *)) vehicle_get_attr;

#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:tracking_update (unchanged): 001.bb-a\n");
#endif
	}

	if (get_attr(attr_object, attr_position_valid, &attr_valid, NULL))
	{
		if (!attr_valid.u.num != attr_position_valid_invalid)
		{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "ROUTExxPOSxx:return 001\n");
#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "\n");
			dbg(0, "==================================================================================\n");
			dbg(0, "==================================================================================\n");
			dbg(0, "\n");
#endif

			return2;
		}
	}


#ifdef NAVIT_ROUTING_DEBUG_PRINT
	struct attr attr_pos_99;
	if (get_attr(attr_object, attr_position_coord_geo, &attr_pos_99, NULL))
	{
		if (attr_pos_99.u.coord_geo)
		{
			struct coord cc999;
			transform_from_geo(pro, attr_pos_99.u.coord_geo, &cc999);
			dbg(0, "ROUTExxPOSxx:get pos: %d %d\n", cc999.x, cc999.y);
		}
	}
#endif


	// load attrs with data from vehicle
	if (!get_attr(attr_object, attr_position_direction, &attr_dir, NULL) || !get_attr(attr_object, attr_position_speed, &attr_speed, NULL) || !get_attr(attr_object, attr_position_coord_geo, &attr_pos, NULL))
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:return 002\n");
#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "\n");
		dbg(0, "==================================================================================\n");
		dbg(0, "==================================================================================\n");
		dbg(0, "\n");
#endif

		return2;
	}
	// load attrs with data from vehicle


	// save old value
	old_dir = nv->dir;
	global_old_vehicle_speed = nv->speed;
	old_scale_value = transform_get_scale(this_->trans);

	nv->dir = *attr_dir.u.numd;
	nv->speed = *attr_speed.u.numd;

	// old values ---------
	if ((global_last_vehicle_pos_geo.lat != 0.0) && (global_last_vehicle_pos_geo.lng != 0.0))
	{
		transform_from_geo(pro, &global_last_vehicle_pos_geo, &nv->coord);
		transform(this_->trans_cursor, pro, &nv->coord, &old_cursor_pnt, 1, 0, 0, NULL);
		old_pos_invalid = 0;

		// XXX // dbg(0,"old values lat:%f lon:%f px:%d py:%d\n", global_last_vehicle_pos_geo.lat, global_last_vehicle_pos_geo.lng, old_cursor_pnt.x, old_cursor_pnt.y);
	}
	else
	{
		old_pos_invalid = 1;
	}
	// old values ---------

	transform_from_geo(pro, attr_pos.u.coord_geo, &nv->coord);

	// save this position
	global_last_vehicle_pos_geo.lat = attr_pos.u.coord_geo->lat;
	global_last_vehicle_pos_geo.lng = attr_pos.u.coord_geo->lng;

	ggggg_lat = attr_pos.u.coord_geo->lat;
	ggggg_lon = attr_pos.u.coord_geo->lng;
	// dbg(0, "PPPOS:%f %f lll=%f", global_last_vehicle_pos_geo.lat, global_last_vehicle_pos_geo.lng, ggggg_lat);

	// save this position


	// XXX // dbg(0,"v1 lat:%f lon:%f x:%d y:%d\n",attr_pos.u.coord_geo->lat, attr_pos.u.coord_geo->lng, nv->coord.x, nv->coord.y);

	//if (nv != this_->vehicle)
	//{
	//	if (hold_drawing == 0)
	//	{
	//		navit_vehicle_draw(this_, nv, NULL);
	//	}
	//	return;
	//}
	cursor_pc.x = nv->coord.x;
	cursor_pc.y = nv->coord.y;
	cursor_pc.pro = pro;

	if (this_->route)
	{
		if (tracking)
		{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "ROUTExxPOSxx:navit_vehicle_update: 001\n");
#endif
			route_set_position_from_tracking(this_->route, tracking, pro);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "ROUTExxPOSxx:navit_vehicle_update: 001-a\n");
#endif
		}
		else
		{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "ROUTExxPOSxx:YYYYY:navit_vehicle_update: 002\n");
#endif
			route_set_position(this_->route, &cursor_pc);
#ifdef NAVIT_ROUTING_DEBUG_PRINT
			dbg(0, "ROUTExxPOSxx:navit_vehicle_update: 002-a\n");
#endif
		}
	}

	// --------------------------------------------------------------
	// --------------------------------------------------------------
	// this calls: graphics_load_mapset (and draws the map)
	// --------------------------------------------------------------
	/// -------**++**-- DISABLE --**++**---- callback_list_call_attr_0(this_->attr_cbl, attr_position);
	// --------------------------------------------------------------
	// --------------------------------------------------------------

	// navit_textfile_debug_log(this_, "type=trackpoint_tracked");
	/*
	 if (this_->gui && nv->speed > MYSTERY_SPEED)
	 {
	 // stupid!!!! this gets called every second!!! fixme!!!!!!
	 navit_disable_suspend();
	 // stupid!!!! this gets called every second!!! fixme!!!!!!
	 }
	 */

	transform(this_->trans_cursor, pro, &nv->coord, &cursor_pnt, 1, 0, 0, NULL);
	// XXX // dbg(0,"v2 px:%d py:%d x:%d y:%d\n", cursor_pnt.x, cursor_pnt.y, nv->coord.x, nv->coord.y);

	// ------- AUTOZOOM ---------
	l_old = 0;
	l_new = 0;
	new_scale_value = navit_autozoom(this_, &nv->coord, nv->speed, 0, &l_old, &l_new);
	// dbg(0, "l_old=%d l_new=%d\n", l_old, l_new);
	// ------- AUTOZOOM ---------

	if (old_pos_invalid == 0)
	{
		int delta_x = cursor_pnt.x - old_cursor_pnt.x;
		int delta_y = cursor_pnt.y - old_cursor_pnt.y;
		int delta_angle = nv->dir - old_dir;
		int delta_zoom = 0;

		if (new_scale_value != -1)
		{
			delta_zoom = (int)(new_scale_value - old_scale_value);
		}

#ifdef HAVE_API_ANDROID
		//dbg(0,"delta x=%d, y=%d, angle=%d\n", delta_x, delta_y, delta_angle);
		set_vehicle_values_to_java_delta(delta_x, delta_y, delta_angle, delta_zoom, l_old, l_new);
#endif
	}



// -------- ??????????
// -------- ??????????
// -------- ??????????
// -------- ??????????
// -------- ??????????
	if (this_->button_pressed != 1 && this_->follow_cursor && nv->follow_curr <= nv->follow && (nv->follow_curr == 1 || !transform_within_border(this_->trans_cursor, &cursor_pnt, this_->border)))
	{
		if (hold_drawing == 0)
		{
			//dbg(0,"call:navit_set_center_cursor_draw:start\n");
			navit_set_center_cursor_draw(this_);
			//dbg(0,"call:navit_set_center_cursor_draw:end\n");
		}
	}
	else
	{
		if (hold_drawing == 0)
		{
			navit_vehicle_draw(this_, nv, pnt);
		}
	}
// -------- ??????????
// -------- ??????????
// -------- ??????????
// -------- ??????????
// -------- ??????????




	if (nv->follow_curr > 1)
	{
		nv->follow_curr--;
	}
	else
	{
		nv->follow_curr = nv->follow;
	}



	// where does this go????? ----------
	// where does this go????? ----------
	//
	// i think this updates the OSD GUIs
	//
	callback_list_call_attr_2(this_->attr_cbl, attr_position_coord_geo, this_, nv->vehicle);
	// where does this go????? ----------
	// where does this go????? ----------



	/* Finally, if we reached our destination, stop navigation. */
	if (this_->route)
	{
		switch (route_destination_reached(this_->route))
		{
			case 1:
				route_remove_waypoint(this_->route);
				count = route_get_destinations(this_->route, pc, 16);

				// destination_file = bookmarks_get_destination_file(TRUE);
				// bookmarks_append_coord(this_->bookmarks, destination_file, pc, count, "former_itinerary_part", NULL, NULL, this_->recentdest_count);

#ifdef HAVE_API_ANDROID
				// waypoint reached
				android_return_generic_int(5, 1);
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:Waypoint reached\n");
#endif
				if (global_routing_engine != 1) // not OSRM routing
				{
					// say it
					navit_say(this_, _("Waypoint reached"));
				}
#endif
				break;
			case 2:
				navit_set_destination(this_, NULL, NULL, 0);
				// ** inform java that we reached our destination **
#ifdef HAVE_API_ANDROID
				android_return_generic_int(4, 1);
#ifdef NAVIT_SAY_DEBUG_PRINT
				android_send_generic_text(1,"+*#O:You have reached your destination\n");
#endif
				// say it
				navit_say(this_, _("You have reached your destination"));
#endif
				break;
		}
	}

	if (hold_drawing == 0)
	{
		// draw???????
		// navit_draw(this_);
		if (this_->ready == 3)
		{
			//dbg(0,"location update:draw:start\n");
			// dbg(0,"navit_draw_async_006\n");
			// zzz554 //
			navit_draw_async(this_, 0);
			//dbg(0,"location update:draw:end\n");
		}
		// draw???????
	}

#ifdef NAVIT_MEASURE_TIME_DEBUG
	debug_mrp("navit_vehicle_update:", debug_measure_end(s_));
#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "ROUTExxPOSxx:navit_vehicle_update:leave\n");
#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT
	dbg(0, "\n");
	dbg(0, "==================================================================================\n");
	dbg(0, "==================================================================================\n");
	dbg(0, "\n");
#endif

__F_END__

}
// --- this gets called at every positon update (from GPS, or demo vehicle!!) !! ------
// --- this gets called at every positon update (from GPS, or demo vehicle!!) !! ------
// --- this gets called at every positon update (from GPS, or demo vehicle!!) !! ------



int navit_is_demo_vehicle()
{
	if (global_demo_vehicle == 1)
	{
		return 1;
	}

	return 0;
}


/**
 * Set the position of the vehicle
 *
 * @param navit The navit instance
 * @param c The coordinate to set as position
 * @returns nothing
 */
void navit_set_position(struct navit *this_, struct pcoord *c)
{
__F_START__

	//DBG dbg(0,"EEnter\n");

	if (this_->route)
	{
//#ifdef NAVIT_ROUTING_DEBUG_PRINT
//		dbg(0, "ROUTExxPOSxx:YYYYY:navit_set_position: 001\n");
//#endif
		// ******* NOT USE ******** // route_set_position(this_->route, c);
		// ******* NOT USE ******** // callback_list_call_attr_0(this_->attr_cbl, attr_position);
	}

	if (this_->ready == 3)
	{
		navit_draw(this_);
	}

__F_END__
}

/**
 * Set the position of the vehicle, without drawing the map
 *
 */
void navit_set_position_without_map_drawing(struct navit *this_, struct pcoord *c)
{



	if (this_->route)
	{
#ifdef NAVIT_ROUTING_DEBUG_PRINT
		dbg(0, "ROUTExxPOSxx:YYYYY:navit_set_position_without_map_drawing: 001\n");
#endif
		route_set_position(this_->route, c);
		callback_list_call_attr_0(this_->attr_cbl, attr_position);
	}
}


int navit_set_vehicleprofile(struct navit *this_, char *name)
{


	////DBG dbg(0,"EEnter\n");

	struct attr attr;
	GList *l;
	l = this_->vehicleprofiles;
	while (l)
	{
		if (vehicleprofile_get_attr(l->data, attr_name, &attr, NULL))
		{
			if (!strcmp(attr.u.str, name))
			{
				this_->vehicleprofile = l->data;
				if (this_->route)
				{
					route_set_profile(this_->route, this_->vehicleprofile);
				}
				return 1;
			}
		}
		l = g_list_next(l);
	}
	return 0;
}

static void navit_set_vehicle(struct navit *this_, struct navit_vehicle *nv)
{


	////DBG dbg(0,"EEnter\n");

	struct attr attr;
	this_->vehicle = nv;

	if (nv && vehicle_get_attr(nv->vehicle, attr_profilename, &attr, NULL))
	{
		if (navit_set_vehicleprofile(this_, attr.u.str))
		{
			return;
		}
	}

	if (!navit_set_vehicleprofile(this_, "car"))
	{
		/* We do not have a fallback "car" profile
		 * so lets set any profile */
		GList *l;
		l = this_->vehicleprofiles;
		if (l)
		{
			this_->vehicleprofile = l->data;
			if (this_->route)
			{
				route_set_profile(this_->route, this_->vehicleprofile);
			}
		}
	}
}

/**
 * Register a new vehicle
 *
 * @param navit The navit instance
 * @param v The vehicle instance
 * @returns 1 for success
 */
int navit_add_vehicle(struct navit *this_, struct vehicle *v)
{



	struct navit_vehicle *nv=g_new0(struct navit_vehicle, 1);
	struct attr follow, active, animate;
	nv->vehicle = v;
	nv->follow = 0;
	nv->last.x = 0;
	nv->last.y = 0;

	global_last_vehicle_pos_geo.lat = 0;
	global_last_vehicle_pos_geo.lng = 0;

	// global_cur_vehicle_pos_geo.lat = 0;
	// global_cur_vehicle_pos_geo.lon = 0;

	nv->animate_cursor = 0;
	if ((vehicle_get_attr(v, attr_follow, &follow, NULL)))
		nv->follow = follow.u.num;

	nv->follow_curr = nv->follow;
	this_->vehicles = g_list_append(this_->vehicles, nv);

	if ((vehicle_get_attr(v, attr_active, &active, NULL)) && active.u.num)
		navit_set_vehicle(this_, nv);

	if ((vehicle_get_attr(v, attr_animate, &animate, NULL)))
		nv->animate_cursor = animate.u.num;

	nv->callback.type = attr_callback;

	// gets called via this callback in vehicle_android.c [in function: vehicle_android_callback]
	nv->callback.u.callback = callback_new_attr_2(callback_cast(navit_vehicle_update), attr_position_coord_geo, this_, nv);
	callback_add_names(nv->callback.u.callback, "navit_add_vehicle", "navit_vehicle_update");

	//dbg(0,"EEnter 11\n");
	vehicle_add_attr(nv->vehicle, &nv->callback);
	//dbg(0,"EEnter 22\n");
	vehicle_set_attr(nv->vehicle, &this_->self);
	//dbg(0,"EEnter 33\n");

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return 1;
}

struct gui *
navit_get_gui(struct navit *this_)
{
	return this_->gui;
}

struct transformation *
navit_get_trans(struct navit *this_)
{
	return this_->trans;
}

struct route *
navit_get_route(struct navit *this_)
{
	return this_->route;
}

struct navigation *
navit_get_navigation(struct navit *this_)
{
	return this_->navigation;
}

struct displaylist *
navit_get_displaylist(struct navit *this_)
{
	return this_->displaylist;
}

void navit_layout_switch(struct navit *n)
{


	////DBG dbg(0,"EEnter\n");

	int currTs = 0;
	struct attr iso8601_attr, geo_attr, valid_attr, layout_attr;
	double trise, tset, trise_actual;
	struct layout *l;
	int year, month, day;

	if (navit_get_attr(n, attr_layout, &layout_attr, NULL) != 1)
	{
		return; //No layout - nothing to switch
	}
	if (!n->vehicle)
		return;
	l = layout_attr.u.layout;

	if (l->dayname || l->nightname)
	{
		//Ok, we know that we have profile to switch

		//Check that we aren't calculating too fast
		if (vehicle_get_attr(n->vehicle->vehicle, attr_position_time_iso8601, &iso8601_attr, NULL) == 1)
		{
			currTs = iso8601_to_secs(iso8601_attr.u.str);
			// dbg(1, "currTs: %u:%u\n", currTs % 86400 / 3600, ((currTs % 86400) % 3600) / 60);
		}
		if (currTs - (n->prevTs) < 60)
		{
			//We've have to wait a little
			return;
		}
		if (sscanf(iso8601_attr.u.str, "%d-%02d-%02dT", &year, &month, &day) != 3)
			return;
		if (vehicle_get_attr(n->vehicle->vehicle, attr_position_valid, &valid_attr, NULL) && valid_attr.u.num == attr_position_valid_invalid)
		{
			return; //No valid fix yet
		}
		if (vehicle_get_attr(n->vehicle->vehicle, attr_position_coord_geo, &geo_attr, NULL) != 1)
		{
			//No position - no sun
			return;
		}

		//We calculate sunrise anyway, cause it is needed both for day and for night
		if (__sunriset__(year, month, day, geo_attr.u.coord_geo->lng, geo_attr.u.coord_geo->lat, -5, 1, &trise, &tset) != 0)
		{
			//near the pole sun never rises/sets, so we should never switch profiles
			// dbg(1, "trise: %u:%u, sun never visible, never switch profile\n", HOURS(trise), MINUTES(trise));
			n->prevTs = currTs;
			return;
		}

		trise_actual = trise;
		// dbg(1, "trise: %u:%u\n", HOURS(trise), MINUTES(trise));
		if (l->dayname)
		{

			if ((HOURS(trise) * 60 + MINUTES(trise) == (currTs % 86400) / 60) || (n->prevTs == 0 && ((HOURS(trise) * 60 + MINUTES(trise) < (currTs % 86400) / 60))))
			{
				//The sun is rising now!
				if (strcmp(l->name, l->dayname))
				{
					navit_set_layout_by_name(n, l->dayname);
				}
			}
		}
		if (l->nightname)
		{
			if (__sunriset__(year, month, day, geo_attr.u.coord_geo->lng, geo_attr.u.coord_geo->lat, -5, 1, &trise, &tset) != 0)
			{
				//near the pole sun never rises/sets, so we should never switch profiles
				// dbg(1,"tset: %u:%u, sun always visible, never switch profile\n",HOURS(tset), MINUTES(tset));
				n->prevTs = currTs;
				return;
			}
			// dbg(1, "tset: %u:%u\n", HOURS(tset), MINUTES(tset));
			if (HOURS(tset) * 60 + MINUTES(tset) == ((currTs % 86400) / 60) || (n->prevTs == 0 && (((HOURS(tset) * 60 + MINUTES(tset) < (currTs % 86400) / 60)) || ((HOURS(trise_actual) * 60 + MINUTES(trise_actual) > (currTs % 86400) / 60)))))
			{
				//Time to sleep
				if (strcmp(l->name, l->nightname))
				{
					navit_set_layout_by_name(n, l->nightname);
				}
			}
		}

		n->prevTs = currTs;
	}
}

int navit_set_vehicle_by_name(struct navit *n, const char *name)
{


	////DBG dbg(0,"EEnter\n");

	struct vehicle *v;
	struct attr_iter *iter;
	struct attr vehicle_attr, name_attr;

	iter = navit_attr_iter_new();

	while (navit_get_attr(n, attr_vehicle, &vehicle_attr, iter))
	{
		v = vehicle_attr.u.vehicle;
		vehicle_get_attr(v, attr_name, &name_attr, NULL);
		if (name_attr.type == attr_name)
		{
			if (!strcmp(name, name_attr.u.str))
			{
				navit_set_attr(n, &vehicle_attr);
				navit_attr_iter_destroy(iter);
				return 1;
			}
		}
	}
	navit_attr_iter_destroy(iter);
	return 0;
}

int navit_set_layout_by_name(struct navit *n, const char *name)
{


	////DBG dbg(0,"EEnter\n");

	struct layout *l;
	struct attr_iter iter;
	struct attr layout_attr;

	iter.u.list = 0x00;

	if (navit_get_attr(n, attr_layout, &layout_attr, &iter) != 1)
	{
		return 0; //No layouts - nothing to do
	}
	if (iter.u.list == NULL)
	{
		return 0;
	}

	iter.u.list = g_list_first(iter.u.list);

	while (iter.u.list)
	{
		l = (struct layout*) iter.u.list->data;
		if (!strcmp(name, l->name))
		{
			layout_attr.u.layout = l;
			layout_attr.type = attr_layout;
			navit_set_attr(n, &layout_attr);
			iter.u.list = g_list_first(iter.u.list);
			return 1;
		}
		iter.u.list = g_list_next(iter.u.list);
	}

	iter.u.list = g_list_first(iter.u.list);
	return 0;
}

void navit_disable_suspend()
{


	////DBG dbg(0,"EEnter\n");

	gui_disable_suspend(global_navit->gui);
	callback_list_call_attr_0(global_navit->attr_cbl, attr_unsuspend);
}

/**
 * @brief Dumps item attrs to string
 *
 * @param item		item
 * @param pretty	0 -> normal, 1 -> pretty output
 * @return string with attrs separated by '\n'
 */
char* navit_item_dump(struct item *item, int pretty)
{
	char *temp_str = NULL;
	char *ret_value = NULL;
	struct attr attr;
	struct attr attr2;
	int had_flags = 0;
	int *f;
	int flags;

	if (item == NULL)
	{
		ret_value = g_strdup("");
		return ret_value;
	}

	if (pretty == 1)
	{
		ret_value = g_strdup_printf("+*TYPE*+:%s", item_to_name(item->type));
	}
	else
	{
		ret_value = g_strdup_printf("type=%s", item_to_name(item->type));
	}

	while (item_attr_get(item, attr_any, &attr))
	{
		if (attr.type == attr_flags)
		{
			had_flags = 1;

			flags = attr.u.num;
			if (flags == 0)
			{
				f = item_get_default_flags(item->type);
				if (f)
				{
					flags = *f;
				}
				else
				{
					flags = 0;
				}
			}

			if (pretty == 1)
			{
				temp_str = g_strdup_printf("%s\n%s=%s", ret_value, attr_to_name(attr.type), flags_to_text(flags));
			}
			else
			{
				temp_str = g_strdup_printf("%s\n%s='%s'", ret_value, attr_to_name(attr.type), flags_to_text(flags));
			}
		}
		else
		{
			if (pretty == 1)
			{
				temp_str = g_strdup_printf("%s\n%s=%s", ret_value, attr_to_name(attr.type), attr_to_text(&attr, NULL, 1));
			}
			else
			{
				temp_str = g_strdup_printf("%s\n%s='%s'", ret_value, attr_to_name(attr.type), attr_to_text(&attr, NULL, 1));
			}
		}
		g_free(ret_value);
		ret_value = g_strdup(temp_str);
		g_free(temp_str);
	}

	if (had_flags == 0)
	{
		f = item_get_default_flags(item->type);
		if (f)
		{
			flags = *f;

			if (pretty == 1)
			{
				temp_str = g_strdup_printf("%s\n%s=%s", ret_value, attr_to_name(attr_flags), flags_to_text(flags));
			}
			else
			{
				temp_str = g_strdup_printf("%s\n%s='%s'", ret_value, attr_to_name(attr_flags), flags_to_text(flags));
			}

			g_free(ret_value);
			ret_value = g_strdup(temp_str);
			g_free(temp_str);
		}
	}

	// g_free(item);

	return ret_value;
}

int navit_normal_item(enum item_type type)
{
	if ((type > type_none) && (type < type_waypoint))
	{
		return 1;
	}
	else if ((type >= type_poi_land_feature) && (type <= type_poi_zoo))
	{
		return 1;
	}
	else if ((type >= type_traffic_signals) && (type <= type_poi_cafe))
	{
		return 1;
	}
	else if ((type >= type_poi_peak) && (type <= type_poi_ruins))
	{
		return 1;
	}
	else if ((type >= type_poi_post_box) && (type <= type_house_number))
	{
		return 1;
	}
	else if ((type >= type_poi_playground) && (type <= type_poi_shop_photo))
	{
		return 1;
	}
	else if (type == type_place_label)
	{
		return 1;
	}
	else if ((type >= type_line) && (type <= type_ferry))
	{
		return 1;
	}
	else if (type == type_street_unkn)
	{
		return 1;
	}
	else if (type == type_street_service)
	{
		return 1;
	}
	else if (type == type_street_pedestrian)
	{
		return 1;
	}
	else if (type == type_street_parking_lane)
	{
		return 1;
	}
	else if (type == type_ramp_highway_land)
	{
		return 1;
	}
	else if (type == type_ramp_street_4_city)
	{
		return 1;
	}
	else if (type == type_ramp_street_3_city)
	{
		return 1;
	}
	else if (type == type_ramp_street_2_city)
	{
		return 1;
	}
	else if ((type >= type_aeroway_runway) && (type <= type_footway_and_piste_nordic))
	{
		return 1;
	}
	else if ((type >= type_house_number_interpolation_even) && (type <= type_city_wall))
	{
		return 1;
	}
	else if ((type >= type_border_city) && (type <= type_border_county))
	{
		return 1;
	}
	else if ((type >= type_forest_way_1) && (type <= type_forest_way_4))
	{
		return 1;
	}
	else if ((type >= type_area) && (type <= type_poly_museum))
	{
		return 1;
	}
	else if ((type >= type_poly_commercial_center) && (type <= type_tundra))
	{
		return 1;
	}
	else if ((type >= type_poly_building) && (type <= type_poly_terminal))
	{
		return 1;
	}
	else if ((type >= type_poly_sports_centre) && (type <= type_poly_aeroway_runway))
	{
		return 1;
	}

	return 0;
}

char* navit_find_nearest_item_dump(struct mapset *ms, struct pcoord *pc, int pretty)
{
	int max_dist = 0; // smallest rectangle possible
	int dist, mindist = 0, pos;
	int mindist_hn = 0;
	struct mapset_handle *h;
	struct map *m;
	struct map_rect *mr;
	struct item *item;
	struct coord lp;
	struct street_data *sd;
	struct coord c;
	struct coord_geo g;
	struct map_selection sel;
	struct attr street_name_attr;
	struct attr hn_attr;
	char *ret_str = NULL;




	mindist = 1000;

	h = mapset_open(ms);
	if (!h)
	{
		// dbg(0,"return 1\n");
		ret_str = g_strdup("");
		return ret_str;
	}

	while ((m = mapset_next(h, 0)))
	{
		c.x = pc->x;
		c.y = pc->y;
		if (map_projection(m) != pc->pro)
		{
			transform_to_geo(pc->pro, &c, &g);
			transform_from_geo(map_projection(m), &g, &c);
		}

		sel.next = NULL;
		sel.order = 18;
		sel.range.min = type_none;
		sel.range.max = type_last;
		sel.u.c_rect.lu.x = c.x - max_dist;
		sel.u.c_rect.lu.y = c.y + max_dist;
		sel.u.c_rect.rl.x = c.x + max_dist;
		sel.u.c_rect.rl.y = c.y - max_dist;

		mr = map_rect_new(m, &sel);
		if (!mr)
		{
			continue;
		}

		while ((item = map_rect_get_item(mr)))
		{
			if (navit_normal_item(item->type) == 1)
			{
				//dbg(0,"*type=%s\n", item_to_name(item->type));

				struct coord c2[101];
				int count22 = item_coord_get(item, c2, 100);
				if (count22 == 0)
				{
					continue;
				}
				else if (count22 > 1)
				{
					dist = transform_distance_polyline_sq__v2(c2, count22, &c);
					if (dist < mindist)
					{
						mindist = dist;
						if (ret_str != NULL)
						{
							g_free(ret_str);
						}
						ret_str = navit_item_dump(item, pretty);
					}
				}
				else
				{
					dist = transform_distance_sq(&c, &c2);
					if (dist <= (mindist + 4))
					{
						mindist = dist;
						if (ret_str != NULL)
						{
							g_free(ret_str);
						}
						ret_str = navit_item_dump(item, pretty);
					}
				}
				// dbg(0,"*end\n");
			}
		}

		if (mr)
		{
			map_rect_destroy(mr);
		}
	}
	mapset_close(h);

	if (ret_str == NULL)
	{
		// dbg(0,"was NULL\n");
		ret_str = g_strdup("");
	}

	return ret_str;
}

/**
 * @brief Finds the nearest street to a given coordinate
 *
 * @param ms The mapset to search in for the street
 * @param pc The coordinate to find a street nearby [ input in pcoord(x,y) ]
 * @return The nearest street
 */
char*
navit_find_nearest_street(struct mapset *ms, struct pcoord *pc)
{
	int max_dist = 0; // smallest rectangle possible
	int dist, mindist = 0, pos;
	struct mapset_handle *h;
	struct map *m;
	struct map_rect *mr;
	struct item *item;
	struct coord lp;
	struct street_data *sd;
	struct coord c;
	struct coord_geo g;
	struct map_selection sel;
	struct attr street_name_attr;
	char *street_name = NULL;




	mindist = 10000; // start with small radius at the beginning!
	street_name = g_strdup(" ");

	h = mapset_open(ms);

	if (!h)
	{
		// set global value :-(
		dist_to_street = mindist;
		return street_name;
	}

	while ((m = mapset_next(h, 0)))
	{
		c.x = pc->x;
		c.y = pc->y;
		if (map_projection(m) != pc->pro)
		{
			transform_to_geo(pc->pro, &c, &g);
			transform_from_geo(map_projection(m), &g, &c);
		}

		sel.next = NULL;
		sel.order = 18;
		sel.range.min = type_line;
		sel.range.max = type_area;
		sel.u.c_rect.lu.x = c.x - max_dist;
		sel.u.c_rect.lu.y = c.y + max_dist;
		sel.u.c_rect.rl.x = c.x + max_dist;
		sel.u.c_rect.rl.y = c.y - max_dist;

		mr = map_rect_new(m, &sel);
		if (!mr)
		{
			continue;
		}

		while ((item = map_rect_get_item(mr)))
		{
			if (item_get_default_flags(item->type))
			{
				sd = street_get_data(item);
				if (!sd)
				{
					continue;
				}

				//dbg(0,"6 sd x:%d sd y:%d count:%d\n", sd->c->x, sd->c->y, sd->count);
				//dbg(0,"6 c x:%d c y:%d\n", c.x, c.y);
				dist = transform_distance_polyline_sq__v2(sd->c, sd->count, &c);
				//dbg(0,"mindist:%d dist:%d\n", mindist, dist);
				if (dist < mindist)
				{
					//dbg(0,"6.a\n");
					mindist = dist;

					if (item_attr_get(item, attr_street_name, &street_name_attr))
					{
						if (street_name)
						{
							g_free(street_name);
							street_name = NULL;
						}
						street_name = g_strdup_printf("%s", street_name_attr.u.str);
						//dbg(0,"r3 %s\n", street_name);
					}
					else if (item_attr_get(item, attr_label, &street_name_attr))
					{
						if (street_name)
						{
							g_free(street_name);
							street_name = NULL;
						}
						street_name = g_strdup_printf("%s", street_name_attr.u.str);
						//dbg(0,"r1 %s\n", street_name);
					}
					else if (item_attr_get(item, attr_street_name_systematic, &street_name_attr))
					{
						if (street_name)
						{
							g_free(street_name);
							street_name = NULL;
						}
						street_name = g_strdup_printf("%s", street_name_attr.u.str);
						//dbg(0,"r4 %s\n", street_name);
					}
					else
					{
						//if (street_name)
						//{
						//	g_free(street_name);
						//	street_name = NULL;
						//}
						//street_name = g_strdup_printf("---");
					}
				}
				street_data_free(sd);
			}
		}

		if (mr)
		{
			map_rect_destroy(mr);
		}
	}
	mapset_close(h);
	// set global value :-(
	dist_to_street = mindist;
	return street_name;
}

/**
 * @brief Finds the nearest street or housenumber to a given coordinate
 *
 * @param ms The mapset to search in for the street
 * @param pc The coordinate to find a street nearby [ input in pcoord(x,y) ]
 * @return The nearest street or housenumber
 */
char*
navit_find_nearest_street_hn(struct mapset *ms, struct pcoord *pc)
{
	int max_dist = 0; // smallest rectangle possible
	int dist, mindist = 0, pos;
	int mindist_hn = 0;
	struct mapset_handle *h;
	struct map *m;
	struct map_rect *mr;
	struct item *item;
	struct coord lp;
	struct street_data *sd;
	struct coord c;
	struct coord_geo g;
	struct map_selection sel;
	struct attr street_name_attr;
	struct attr hn_attr;
	char *street_name = NULL;
	char *street_name_saved = NULL;




	// first find a street
	street_name_saved = navit_find_nearest_street(ms, pc);
	// street_name = g_strdup_printf(" ");
	street_name = g_strdup(street_name_saved);
	// first find a street

	mindist = dist_to_street; // start with small radius at the beginning! (only use housenumber of different street, if we are really close to it!!)
	// global value -> this is naughty :-)

	if (mindist < 8)
	{
		// so we can find other housenumbers if we are very close
		mindist = 8;
	}

	mindist_hn = 10000;

	//dbg(0,"given streetname %s %s dist %d\n", street_name, street_name_saved, dist_to_street);


	h = mapset_open(ms);

	if (!h)
	{
		if (street_name_saved)
		{
			g_free(street_name_saved);
			street_name_saved = NULL;
		}
		return street_name;
	}

	while ((m = mapset_next(h, 0)))
	{
		c.x = pc->x;
		c.y = pc->y;
		if (map_projection(m) != pc->pro)
		{
			transform_to_geo(pc->pro, &c, &g);
			transform_from_geo(map_projection(m), &g, &c);
		}

		sel.next = NULL;
		sel.order = 18;
		sel.range.min = type_none;
		sel.range.max = type_area;
		sel.u.c_rect.lu.x = c.x - max_dist;
		sel.u.c_rect.lu.y = c.y + max_dist;
		sel.u.c_rect.rl.x = c.x + max_dist;
		sel.u.c_rect.rl.y = c.y - max_dist;

		mr = map_rect_new(m, &sel);
		if (!mr)
		{
			continue;
		}

		while ((item = map_rect_get_item(mr)))
		{
			if (item->type == type_house_number)
			{
				//dbg(0,"hn found\n");
				struct coord c2;
				int rrr = item_coord_get(item, &c2, 1);
				if (rrr)
				{
					dist = transform_distance_sq(&c, &c2);
					//dbg(0,"dist=%d\n", dist);
					if (dist < mindist)
					{
						if (item_attr_get(item, attr_street_name, &street_name_attr))
						{
							if (item_attr_get(item, attr_house_number, &hn_attr))
							{
								if (street_name)
								{
									g_free(street_name);
									street_name = NULL;
								}
								street_name = g_strdup_printf("%s %s", street_name_attr.u.str, hn_attr.u.str);
								//dbg(0,"sn 1\n");
								mindist = dist;
								mindist_hn = dist;
							}
						}
					}
					// else try to find housenumbers for our current street
					// just take the nearest housenumber for our current street
					else if (dist < mindist_hn)
					{
						//dbg(0,"sn 2.1\n");
						if (item_attr_get(item, attr_street_name, &street_name_attr))
						{
							//dbg(0,"sn 2.2\n");
							if (item_attr_get(item, attr_house_number, &hn_attr))
							{
								//dbg(0,"sn 2.3\n");
								if ((street_name != NULL) && (street_name_saved != NULL))
								{
									// dbg(0,"sn 2.4 -%s- -%s-\n", street_name_saved, street_name_attr.u.str);
									if (!strcmp(street_name_saved, street_name_attr.u.str))
									{
										g_free(street_name);
										street_name = NULL;

										// dbg(0,"sn 2.99\n");

										street_name = g_strdup_printf("%s %s", street_name_attr.u.str, hn_attr.u.str);
										mindist_hn = dist;
									}
								}
							}
						}
					}
				}
			}

#if 0
			//else if (item->type > type_line)
			if (1 == 0) // DISABLED !!!!!!

			{
				if (item_get_default_flags(item->type))
				{
					sd = street_get_data(item);
					if (!sd)
					{
						continue;
					}
					dist = transform_distance_polyline_sq(sd->c, sd->count, &c, &lp, &pos);
					if (dist < mindist)
					{
						mindist = dist;

						if (item_attr_get(item, attr_label, &street_name_attr))
						{
							if (street_name)
							{
								g_free(street_name);
								street_name = NULL;
							}
							if (street_name_saved)
							{
								g_free(street_name_saved);
								street_name_saved = NULL;
							}
							street_name = g_strdup_printf("%s", street_name_attr.u.str);
							street_name_saved = g_strdup(street_name);
						}
						else if (item_attr_get(item, attr_street_name, &street_name_attr))
						{
							if (street_name)
							{
								g_free(street_name);
								street_name = NULL;
							}
							if (street_name_saved)
							{
								g_free(street_name_saved);
								street_name_saved = NULL;
							}
							street_name = g_strdup_printf("%s", street_name_attr.u.str);
							street_name_saved = g_strdup(street_name);
						}
						else if (item_attr_get(item, attr_street_name_systematic, &street_name_attr))
						{
							if (street_name)
							{
								g_free(street_name);
								street_name = NULL;
							}
							if (street_name_saved)
							{
								g_free(street_name_saved);
								street_name_saved = NULL;
							}
							street_name = g_strdup_printf("%s", street_name_attr.u.str);
							street_name_saved = g_strdup(street_name);
						}
					}
					street_data_free(sd);
				}
			}
#endif
		}

		if (mr)
		{
			map_rect_destroy(mr);
		}
	}

	if (street_name_saved)
	{
		g_free(street_name_saved);
		street_name_saved = NULL;
	}

	mapset_close(h);
	return street_name;
}

/**
 * @brief Finds the nearest street to a given coordinate
 *
 * @param ms The mapset to search in for the street
 * @param pc The coordinate to find a street nearby [ input in pcoord(x,y) ]
 * @return The nearest street (as a string of coords "0xFFF 0xFFF\n..." seperated by "\n")
 */
char*
navit_find_nearest_street_coords(struct mapset *ms, struct pcoord *pc)
{
	int max_dist = 0; // smallest rectangle possible
	int dist, mindist = 0, pos;
	struct mapset_handle *h;
	struct map *m;
	struct map_rect *mr;
	struct item *item;
	struct coord lp;
	struct street_data *sd = NULL;
	struct street_data *sd_copy = NULL;
	struct coord c;
	struct coord_geo g;
	struct map_selection sel;
	int i;
	int found_good = 0;
	struct attr street_name_attr;
	char *street_coords = NULL;
	char *street_coords_tmp = NULL;




	mindist = 10000; // start with small radius at the beginning!
	street_coords = g_strdup_printf("", "");

	h = mapset_open(ms);

	if (!h)
	{
		return street_coords;
	}

	while ((m = mapset_next(h, 0)))
	{
		c.x = pc->x;
		c.y = pc->y;
		if (map_projection(m) != pc->pro)
		{
			transform_to_geo(pc->pro, &c, &g);
			transform_from_geo(map_projection(m), &g, &c);
		}

		sel.next = NULL;
		sel.order = 18;
		sel.range.min = type_line;
		sel.range.max = type_area;
		sel.u.c_rect.lu.x = c.x - max_dist;
		sel.u.c_rect.lu.y = c.y + max_dist;
		sel.u.c_rect.rl.x = c.x + max_dist;
		sel.u.c_rect.rl.y = c.y - max_dist;

		mr = map_rect_new(m, &sel);
		if (!mr)
		{
			continue;
		}

		while ((item = map_rect_get_item(mr)))
		{
			if (item_get_default_flags(item->type))
			{
				sd = street_get_data(item);
				if (!sd)
				{
					continue;
				}
				found_good = 0;
				// OLD // dist = transform_distance_polyline_sq(sd->c, sd->count, &c, &lp, &pos);
				dist = transform_distance_polyline_sq__v2(sd->c, sd->count, &c);
				if (dist < mindist)
				{
					mindist = dist;

					/*
					 if (item_attr_get(item, attr_label, &street_name_attr))
					 {
					 found_good = 1;
					 }
					 else if (item_attr_get(item, attr_street_name, &street_name_attr))
					 {
					 found_good = 1;
					 }
					 else if (item_attr_get(item, attr_street_name_systematic, &street_name_attr))
					 {
					 found_good = 1;
					 }
					 */

					// allow any street/line, so you can select streets without name also!
					found_good = 1;
				}

				if (found_good == 1)
				{
					if (sd_copy)
					{
						street_data_free(sd_copy);
					}
					sd_copy = street_data_dup(sd);
				}
				street_data_free(sd);
			}
		}

		if (mr)
		{
			map_rect_destroy(mr);
		}
	}
	mapset_close(h);

	if (sd_copy)
	{
		//struct coord ca[sd_copy->count];
		//item_coord_get(&sd_copy->item, ca, sd_copy->count);

		// dbg(0,"sd_copy->count=%d\n", sd_copy->count);

		for (i = 0; i < sd_copy->count; i++)
		{
			/*
			 unsigned int x;
			 unsigned int y;
			 char *sign_x = "";
			 char *sign_y = "";

			 if ( c->x < 0 ) {
			 x = -c->x;
			 sign_x = "-";
			 } else {
			 x = c->x;
			 }
			 if ( c->y < 0 ) {
			 y = -c->y;
			 sign_y = "-";
			 } else {
			 y = c->y;
			 }
			 */

			street_coords_tmp = street_coords;
			if (street_coords == NULL)
			{
				street_coords = g_strdup_printf("0x%x 0x%x\n", sd_copy->c[i].x, sd_copy->c[i].y);
			}
			else
			{
				char *tmp2 = g_strdup_printf("0x%x 0x%x\n", sd_copy->c[i].x, sd_copy->c[i].y);
				street_coords = g_strconcat(street_coords_tmp, tmp2, NULL);
				g_free(street_coords_tmp);
				g_free(tmp2);
			}
		}

		street_data_free(sd_copy);
	}

	return street_coords;
}

// UNUSDED -------
// UNUSDED -------
int navit_block(struct navit *this_, int block)
{


	////DBG dbg(0,"EEnter\n");

	if (block > 0)
	{
		this_->blocked |= 1;
		if (graphics_draw_cancel(this_->gra, this_->displaylist))
			this_->blocked |= 2;
		return 0;
	}
	if ((this_->blocked & 2) || block < 0)
	{
		this_->blocked = 0;
		navit_draw(this_);
		return 1;
	}
	this_->blocked = 0;
	return 0;
}

FILE * navit_start_gpx_file(char *filename)
{
    char *header1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>"
                    "<gpx version=\"1.1\" creator=\"ZANavi http://zanavi.cc\"\n"
                    "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    "     xmlns=\"http://www.topografix.com/GPX/1/1\"\n"
                    "     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n"
					"<metadata>\n"
					"	<name>ZANavi Debug log</name>\n"
					"	<desc>ZANavi</desc>\n"
					"	<author>\n"
					"		<name>ZANavi</name>\n"
					"	</author>\n"
					"</metadata>\n"
					"<trk>\n"
					"<trkseg>\n"
					" <name>ACTIVE LOG</name>\n";

    FILE *fp;

	if (filename)
	{
    	fp = fopen(filename,"w");
	}
	else
	{
    	fp = fopen("/sdcard/zanavi_debug_001.gpx","w");
	}

    fprintf(fp, "%s", header1);

	return fp;
}

void navit_add_trkpoint_to_gpx_file(FILE *fp, struct coord *c)
{
	struct coord_geo g;
	transform_to_geo(projection_mg, c, &g);
	fprintf(fp, " <trkpt lat='%4.6f' lon='%4.6f'><time>2014-10-02T09:30:10Z</time></trkpt>\n", g.lat, g.lng);
}


void navit_end_gpx_track_seg(FILE *fp)
{
	fprintf(fp,"</trkseg>\n</trk>\n");
}

void navit_start_gpx_track_seg(FILE *fp)
{
	fprintf(fp,"<trk>\n<trkseg>\n");
}


void navit_end_gpx_file(FILE *fp)
{
    char *trailer1= "</trkseg>\n"
					"</trk>\n"
					"</gpx>\n";

	fprintf(fp,"%s",trailer1);
	fclose(fp);
}

GList* navit_route_export_to_java_string(struct navit *this_, int result_id)
{
	struct point p;
	struct map *map=NULL;
	struct navigation *nav = NULL;
	struct map_rect *mr=NULL;
	struct item *item =NULL;
	struct attr attr,route;
	struct coord c;
	// struct coord c_end;
	struct coord_geo g;
	struct transformation *trans;
	char *d = NULL;
	char *result_string = NULL;
	GList* result = NULL;

    nav = navit_get_navigation(this_);

    if (!nav)
	{
		return NULL;
    }

    map = navigation_get_map(nav);

    if (map)
	{
		mr = map_rect_new(map,NULL);
	}
	else
	{
		return;
	}

  	trans = navit_get_trans(this_);

	mr = map_rect_new(map,NULL);
	while ((item = map_rect_get_item(mr)))
	{

		// dbg(0, "005 ============== = %s : %d\n", item_to_name(item->type), item->id_lo);

		//if (item_attr_get(item, attr_navigation_short, &attr))
		//{
		//	dbg(0, "005.c.01:%s\n", attr.u.str);
		//}

		if (item_attr_get(item, attr_length, &attr))
		{
			if (attr.u.num > 0)
			{
				d = get_distance(nav, attr.u.num, attr_navigation_short, 1);
			}

			// dbg(0, "005.c.02:%d %s\n", attr.u.num, d); // dist to next turn in meters! (take care when in imperial mode!)
		}
		else
		{
			d = NULL;
		}

		if ((item_attr_get(item, attr_navigation_long_exact, &attr)) || (item->type == type_nav_waypoint))
		{
			dbg(0, "NAVICG:call type=%s\n", item_to_name(item->type));
			item_coord_get(item, &c, 1);
			dbg(0, "NAVICG:call END\n");

			//int num_coords = 0;
			//while (item_coord_get(item, &c_end, 1))
			//{
			//	num_coords++;
			//}

			transform_to_geo(projection_mg, &c, &g);

			if (result)
			{
			}
			else
			{
				// 1st line is the ID
				result = g_list_append(result, g_strdup_printf("%d", result_id));
				if (result_id == 9990001)
				{
					// 2nd line is distance to target in meters
					int len_meters_to_target = 0;
					if (nav)
					{
						if (nav->first)
						{
							len_meters_to_target = nav->first->dest_length;
						}
					}
					result = g_list_append(result, g_strdup_printf("meters:%d", len_meters_to_target));
				}
			}

			dbg(0, "013 %s %s\n", item_to_name(item->type), map_convert_string(item->map, attr.u.str));

			if (item->type == type_nav_waypoint)
			{
				result = g_list_append(result, g_strdup_printf("%s:%4.8f:%4.8f:%s:%s", d ? d : "", g.lat, g.lng, item_to_name(item->type), _("Waypoint")));
			}
			else
			{
				result = g_list_append(result, g_strdup_printf("%s:%4.8f:%4.8f:%s:%s", d ? d : "", g.lat, g.lng, item_to_name(item->type), map_convert_string(item->map,attr.u.str)));
			}
		}
		else
		{
			// must be the start point (without navigation command)
			item_coord_get(item, &c, 1);
			transform_to_geo(projection_mg, &c, &g);

			if (result)
			{
			}
			else
			{
				// 1st line is the ID
				result = g_list_append(result, g_strdup_printf("%d", result_id));
				if (result_id == 9990001)
				{
					// 2nd line is distance to target in meters
					int len_meters_to_target = 0;
					if (nav)
					{
						if (nav->first)
						{
							len_meters_to_target = nav->first->dest_length;
						}
					}
					result = g_list_append(result, g_strdup_printf("meters:%d", len_meters_to_target));
				}
			}

			// dbg(0, "019.0 %p\n", attr.u.str);
			// dbg(0, "019.b %d\n", item->type);
			// dbg(0, "019.c %s\n", item_to_name(item->type));
			result = g_list_append(result, g_strdup_printf("%s:%4.8f:%4.8f:+start+:", d ? d : "", g.lat, g.lng));
		}

		if (d)
		{
			g_free(d);
			d = NULL;
		}

	}

	map_rect_destroy(mr);

	return result;

}

void navit_route_export_gpx_to_file(struct navit *this_, char *filename)
{



	dbg(0,"Dumping route to %s\n", filename);

	struct point p;
	struct map *map=NULL;
	struct navigation *nav = NULL;
	struct map_rect *mr=NULL;
	struct item *item =NULL;
	struct attr attr,route;
	struct coord c;
	struct coord_geo g;
	struct transformation *trans;
	
    char *header1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>"
                    "<gpx version=\"1.1\" creator=\"ZANavi http://zanavi.cc\"\n"
                    "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    "     xmlns=\"http://www.topografix.com/GPX/1/1\"\n"
                    "     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n"
					"<metadata>\n"
					"	<name>ZANavi Track log</name>\n"
					"	<desc>ZANavi</desc>\n"
					"	<author>\n"
					"		<name>ZANavi</name>\n"
					"	</author>\n"
					"</metadata>\n"
					"<trk>\n"
					"<trkseg>\n"
					" <name>ACTIVE LOG</name>\n";

    char *trailer1= "</trkseg>\n"
					"</trk>\n";

//	char *trkcolor= "<extensions>\n"
//					"	<gpxx:TrackExtension>\n"
//					"		<gpxx:DisplayColor>Magenta</gpxx:DisplayColor>\n"
//					"	</gpxx:TrackExtension>\n"
//					"</extensions>\n";

    char *header2 = "<rte>\n";

    char *trailer2= "</rte>\n"
					"</gpx>\n";


	// -------- full route ---------
	// -------- full route ---------
	struct route *route2 = NULL;
	struct map *route_map2 = NULL;
	struct map_rect *mr2 = NULL;
	struct item *item2 = NULL;
	struct coord c2;

	route2 = navit_get_route(this_);

	if (route2)
	{
		route_map2 = route_get_map(route2);
	}

	if (route_map2)
	{
		mr2 = map_rect_new(route_map2, NULL);
	}

	if (mr2)
	{
		item2 = map_rect_get_item(mr2);
	}

	if (item2 && item2->type == type_route_start)
	{
		item2 = map_rect_get_item(mr2);
	}

    FILE *fp;
    fp = fopen(filename,"w");
    fprintf(fp, "%s", header1);
	int first3 = 0;

	if (item2 && item_coord_get(item2, &c2, 1))
	{
		transform_to_geo(projection_mg, &c2, &g);
		fprintf(fp, " <trkpt lat='%4.16f' lon='%4.16f'><time>2014-10-02T09:30:10Z</time></trkpt>\n", g.lat, g.lng);

		while (item2)
		{
			if (!item_coord_get(item2, &c2, 1))
			{
				item2 = map_rect_get_item(mr2);
				first3 = 1;
				continue;
			}

			if (first3 == 0)
			{
				transform_to_geo(projection_mg, &c2, &g);
				fprintf(fp, " <trkpt lat='%4.16f' lon='%4.16f'><time>2014-10-02T09:30:10Z</time></trkpt>\n", g.lat, g.lng);
			}
			else
			{
				first3 = 0;
				// fprintf(fp, "Y**********\n");
			}
		}
	}

	map_rect_destroy(mr2);

	// -------- full route ---------
	// -------- full route ---------





    nav = navit_get_navigation(this_);

    if (!nav)
	{
            return;
    }

    map = navigation_get_map(nav);

    if (map)
	{
		mr = map_rect_new(map,NULL);
	}
	else
	{
		return;
	}

  	trans = navit_get_trans(this_);


	//while((item = map_rect_get_item(mr)))
	//{
	//	item_coord_get(item, &c, 1);
	//	transform_to_geo(projection_mg, &c, &g);
	//	fprintf(fp, " <trkpt lon='%4.16f' lat='%4.16f'><time>2014-10-02T09:30:10Z</time></trkpt>\n", g.lng, g.lat);
	//}
	map_rect_destroy(mr);



	fprintf(fp,"%s",trailer1);



	mr = map_rect_new(map,NULL);
	while ((item = map_rect_get_item(mr)))
	{
		if (item_attr_get(item, attr_navigation_long, &attr))
		{
			item_coord_get(item, &c, 1);
			transform_to_geo(projection_mg, &c, &g);
			fprintf(fp, "<wpt lat=\"%4.16f\" lon=\"%4.16f\"><time>2014-10-02T09:30:10Z</time><name>%s:%s</name><sym>Dot</sym><type>Dot></type></wpt>\n", g.lat, g.lng, item_to_name(item->type), map_convert_string(item->map,attr.u.str));
		}
		else
		{
			// must be the start point (without navigation command)
			item_coord_get(item, &c, 1);
			transform_to_geo(projection_mg, &c, &g);
			fprintf(fp, "<wpt lat=\"%4.16f\" lon=\"%4.16f\"><time>2014-10-02T09:30:10Z</time><name>START</name><sym>Dot</sym><type>Dot></type></wpt>\n", g.lat, g.lng);
		}
	}
	map_rect_destroy(mr);



	fprintf(fp,"%s",header2);



	mr = map_rect_new(map,NULL);
	while ((item = map_rect_get_item(mr)))
	{
		if (item_attr_get(item, attr_navigation_long, &attr))
		{
			item_coord_get(item, &c, 1);
			transform_to_geo(projection_mg, &c, &g);
			fprintf(fp, "<rtept lat='%4.16f' lon='%4.16f'><name>%s:%s</name></rtept>\n", g.lat, g.lng, item_to_name(item->type), map_convert_string(item->map,attr.u.str));
		}
		else
		{
			// must be the start point (without navigation command)
			item_coord_get(item, &c, 1);
			transform_to_geo(projection_mg, &c, &g);
			fprintf(fp, "<rtept lat='%4.16f' lon='%4.16f'><name>START</name></rtept>\n", g.lat, g.lng);
		}
	}
	map_rect_destroy(mr);

	fprintf(fp,"%s",trailer2);

	fclose(fp);

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

}


void navit_destroy(struct navit *this_)
{


	dbg(0, "EEnter\n");

	struct mapset*ms;
	callback_list_call_attr_1(this_->attr_cbl, attr_destroy, this_);

	// //DBG dbg(0,"enter");

	/* TODO: destroy objects contained in this_ */
	if (this_->vehicle)
	{
		vehicle_destroy(this_->vehicle->vehicle);
	}

	/*
	 if (this_->bookmarks)
	 {
	 dbg(0, "save position to file\n");
	 char *center_file = bookmarks_get_center_file(TRUE);
	 bookmarks_write_center_to_file(this_->bookmarks, center_file);
	 g_free(center_file);
	 bookmarks_destroy(this_->bookmarks);
	 dbg(0, "save position to file -> ready\n");
	 }
	 */

	dbg(0, "ex 001\n");
	callback_destroy(this_->nav_speech_cb);
	dbg(0, "ex 002\n");
	callback_destroy(this_->roadbook_callback);
	dbg(0, "ex 003\n");
	callback_destroy(this_->popup_callback);
	dbg(0, "ex 004\n");
	callback_destroy(this_->motion_timeout_callback);
	dbg(0, "ex 005\n");
	callback_destroy(this_->progress_cb);
	dbg(0, "ex 006\n");
	/*
	 if (this_->gra)
	 graphics_remove_callback(this_->gra, this_->resize_callback);
	 callback_destroy(this_->resize_callback);
	 */
	dbg(0, "ex 007\n");
	/*
	 if (this_->gra)
	 graphics_remove_callback(this_->gra, this_->button_callback);
	 callback_destroy(this_->button_callback);
	 */
	dbg(0, "ex 008\n");
	/*
	 if (this_->gra)
	 graphics_remove_callback(this_->gra, this_->motion_callback);
	 callback_destroy(this_->motion_callback);
	 */
	dbg(0, "ex 009\n");
	if (this_->gra)
	{
		graphics_remove_callback(this_->gra, this_->predraw_callback);
	}
	callback_destroy(this_->predraw_callback);
	dbg(0, "ex 010\n");
	route_destroy(this_->route);
	dbg(0, "ex 011\n");
	ms = navit_get_mapset(this_);
	dbg(0, "ex 012\n");

	if (ms)
	{
		mapset_destroy(ms);
	}

	dbg(0, "ex 013\n");
	graphics_free(this_->gra);
	dbg(0, "ex 014\n");
	g_free(this_);
	dbg(0, "ex 015\n");
}

/** @} */

