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

#ifndef NAVIT_NAVIT_H
#define NAVIT_NAVIT_H

#ifdef __cplusplus
extern "C"
{
#endif
extern struct gui *main_loop_gui;
// defined in glib.h.
#ifndef __G_LIST_H__
struct _GList;
typedef struct _GList GList;
#endif


// color codes for colour attribute in OS;
#define __c_black	"000000"
#define __c_gray	"808080"
#define __c_maroon	"800000"
#define __c_olive	"808000"
#define __c_green	"008000"
#define __c_teal	"008080"
#define __c_navy	"000080"
#define __c_purple	"800080"

#define __c_white	"FFFFFF"
#define __c_silver	"C0C0C0"
#define __c_red		"FF0000"
#define __c_yellow	"FFFF00"
#define __c_lime	"00FF00"
#define __c_aqua	"00FFFF"
#define __c_blue	"0000FF"
#define __c_fuchsia	"FF00FF"

#define __c_brown	"A52A2A"


// --------------------------------------------
// --
// minimum binfile map version needed for this version of ZANavi to work
#define NEED_MIN_BINFILE_MAPVERSION 4
// --
// --------------------------------------------

#define ROAD_ANGLE_IS_STRAIGHT_ABS 10
#define ROAD_ANGLE_DISTANCE_FOR_STRAIGHT 78

// --------------------------------------------
// --
// if the turn angle is greater than this (in degrees) in bicycle mode, then speak a turn command
#define ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE 65
#define ROAD_ANGLE_MIN__FOR_TURN_BICYCLEMODE_ONLY_1_POSSIBILITY 25

#define ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC 65
#define ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE_CYC_2_CYC__2 25
// --
// --------------------------------------------


extern int allow_gui_internal;
extern int draw_display_at_speed;
extern int routing_mode; // 0-> normal highway routing
// 1-> normal roads routing
// 2-> future use
extern int offline_search_filter_duplicates;
extern int offline_search_break_searching;
extern char *navit_maps_dir;
extern char *navit_share_dir;
extern char* navit_data_dir;
extern int cancel_drawing_global;
extern int disable_map_drawing;
extern int global_speak_streetnames;

extern int allow_large_mapfiles;
extern int cache_size_file; /* cache size in bytes */
extern int draw_polylines_fast; // 1 -> dont draw circles at the end of polylines 0-> do draw circles

extern int limit_order_corrected;
extern int shift_order;
extern int global_search_street_size_factor;
extern int hold_drawing;
extern int global_stop_demo_vehicle;
extern int global_show_route_rectangles;
extern int global_traffic_light_delay;
extern int global_clinedrawing_active;
extern int global_draw_multipolygons;
extern int global_have_dpi_value;
extern float global_dpi_factor;
extern int global_order_level_for_fast_draw;
extern int global_show_english_labels;
extern int global_routing_engine;
extern float global_overspill_factor;

extern long long draw_lines_count_2;
extern long long draw_lines_count_3;
extern long long draw_lines_count_4;
extern int poi_on_map_count;
extern int label_on_map_count;
extern int label_district_on_map_count;
extern int label_major_on_map_count;
extern int poi_icon_on_map_count;


extern int mapdraw_time[11 + 5];
extern int cur_mapdraw_time_index;

extern int route_status_previous;
extern long long global_route_memory_size;
extern int global_old_vehicle_speed;
extern int global_old_vehicle_speed_for_autozoom;

extern int global_avoid_sharp_turns_flag;
extern int global_avoid_sharp_turns_min_angle;
extern int global_avoid_sharp_turns_min_penalty;

extern int global_search_radius_for_housenumbers;
extern int global_vehicle_profile;
extern int global_cycle_lanes_prio;
extern int global_cycle_track_prio;

extern double global_v_pos_lat;
extern double global_v_pos_lng;
extern double global_v_pos_dir;

extern int global_demo_vehicle;
extern int global_demo_vehicle_short_switch;
extern long global_last_spoken;
extern long global_last_spoken_base;
extern float global_road_speed_factor;

extern float global_level0_announcement;
extern float global_level1_announcement;
extern float global_level2_announcement;
extern float global_levelx_announcement_factor;
extern float global_b_level0_announcement;
extern float global_b_level1_announcement;
extern float global_b_level2_announcement;
extern float global_b_levelx_announcement_factor;

extern int global_driven_away_from_route;

extern int global_enhance_cycleway;
extern int global_tracking_show_real_gps_pos;
extern int global_show_maps_debug_view;
extern int global_cancel_preview_map_drawing;

extern int global_test_number;

#define MAX_DEBUG_COORDS 100

extern struct coord global_debug_route_seg_winner_start;
extern struct coord global_debug_route_seg_winner_end;
extern struct coord global_debug_seg_winner_start;
extern struct coord global_debug_seg_winner_end;
extern struct coord global_debug_route_seg_winner_p_start;
extern struct coord global_debug_seg_winner_p_start;
extern struct coord global_debug_seg_route_start;
extern struct coord global_debug_seg_route_end;
extern struct coord global_debug_trlast_start;
extern struct coord global_debug_trlast_end;
extern struct coord *global_debug_coord_list;
extern int global_debug_coord_list_items;
extern int global_has_gpsfix;
extern int global_pos_is_underground;

extern GList *global_all_cbs;

#include "coord.h"


#define MAX_SHARP_TURN_LIST_ENTRIES 500

struct global_sharp_turn
{
	int dir;
	int angle;
	struct coord c1;
	struct coord cs;
	struct coord ce;
};


extern int global_sharp_turn_list_count;
extern struct global_sharp_turn *global_sharp_turn_list;




#define MAX_FREETEXT_LIST_ENTRIES 500
#define MAX_FREETEXT_ENTRY_LEN 300

struct global_freetext
{
	struct coord c1;
	char text[MAX_FREETEXT_ENTRY_LEN];
};


extern int global_freetext_list_count;
extern struct global_freetext *global_freetext_list;



// #ifndef MAPTOOL
extern GHashTable *global_transform_hash;
extern GHashTable *global_transform_hash2;
// #endif


/* prototypes */
enum attr_type;
struct attr;
struct attr_iter;
struct callback;
struct coord_rect;
struct displaylist;
struct graphics;
struct gui;
struct mapset;
struct message;
struct navigation;
struct navit;
struct pcoord;
struct point;
struct route;
struct tracking;
struct transformation;
struct vehicleprofile;
struct command_table;
void navit_add_mapset(struct navit *this_, struct mapset *ms);
struct mapset *navit_get_mapset(struct navit *this_);
struct tracking *navit_get_tracking(struct navit *this_);
char *navit_get_user_data_directory(int create);
void navit_draw_async(struct navit *this_, int async);
void navit_draw(struct navit *this_);
int navit_get_ready(struct navit *this_);
void navit_draw_displaylist(struct navit *this_);
void navit_handle_resize(struct navit *this_, int w, int h);
int navit_get_width(struct navit *this_);
int navit_get_height(struct navit *this_);
int navit_ignore_button(struct navit *this_);
void navit_ignore_graphics_events(struct navit *this_, int ignore);
void navit_set_timeout(struct navit *this_);
int navit_handle_button(struct navit *this_, int pressed, int button, struct point *p, struct callback *popup_callback);
void navit_handle_motion(struct navit *this_, struct point *p);
void navit_zoom_in(struct navit *this_, int factor, struct point *p);
void navit_zoom_out(struct navit *this_, int factor, struct point *p);
void navit_zoom_in_cursor(struct navit *this_, int factor);
void navit_zoom_out_cursor(struct navit *this_, int factor);
struct navit *navit_new(struct attr *parent, struct attr **attrs);
void navit_add_message(struct navit *this_, char *message);
struct message *navit_get_messages(struct navit *this_);
struct graphics *navit_get_graphics(struct navit *this_);
struct vehicleprofile *navit_get_vehicleprofile(struct navit *this_);
GList *navit_get_vehicleprofiles(struct navit *this_);
void navit_set_destination(struct navit *this_, struct pcoord *c, const char *description, int async);
void navit_set_destinations(struct navit *this_, struct pcoord *c, int count, const char *description, int async);
int navit_check_route(struct navit *this_);
void navit_textfile_debug_log(struct navit *this_, const char *fmt, ...);
void navit_textfile_debug_log_at(struct navit *this_, struct pcoord *pc, const char *fmt, ...);
int navit_speech_estimate(struct navit *this_, char *str);
void navit_say(struct navit *this_, char *text);
void navit_speak(struct navit *this_);
void navit_window_roadbook_destroy(struct navit *this_);
void navit_window_roadbook_new(struct navit *this_);
void navit_reload_maps(struct navit *this_);
void navit_predraw(struct navit *this_);
void navit_init(struct navit *this_);
void navit_zoom_to_rect(struct navit *this_, struct coord_rect *r);
void navit_zoom_to_route(struct navit *this_, int orientation);
void navit_set_center_no_draw(struct navit *this_, struct pcoord *center, int set_timeout);
void navit_set_center(struct navit *this_, struct pcoord *center, int set_timeout);
void navit_set_center_cursor(struct navit *this_, int autozoom, int keep_orientation);
void navit_set_center_screen(struct navit *this_, struct point *p, int set_timeout);
int navit_set_attr(struct navit *this_, struct attr *attr);
int navit_get_attr(struct navit *this_, enum attr_type type, struct attr *attr, struct attr_iter *iter);
int navit_add_attr(struct navit *this_, struct attr *attr);
int navit_remove_attr(struct navit *this_, struct attr *attr);
struct attr_iter *navit_attr_iter_new(void);
void navit_attr_iter_destroy(struct attr_iter *iter);
void navit_add_callback(struct navit *this_, struct callback *cb);
void navit_remove_callback(struct navit *this_, struct callback *cb);
void navit_set_position(struct navit *this_, struct pcoord *c);
struct gui *navit_get_gui(struct navit *this_);
struct transformation *navit_get_trans(struct navit *this_);
struct route *navit_get_route(struct navit *this_);
struct navigation *navit_get_navigation(struct navit *this_);
struct displaylist *navit_get_displaylist(struct navit *this_);
void navit_layout_switch(struct navit *n);
int navit_set_vehicle_by_name(struct navit *n, const char *name);
void navit_layer_set_active(struct navit *this, char *name, int active, int draw);
void navit_motion(void *data, struct point *p);
void displaylist_shift_order_in_map_layers(struct navit *this_, int shift_value);
void displaylist_shift_for_dpi_value_in_layers(struct navit *this_, double factor);
int navit_is_demo_vehicle();
void navit_zoom_to_scale_no_draw(struct navit *this_, int new_scale);
void navit_zoom_to_scale(struct navit *this_, int new_scale);


void navit_set_cursors(struct navit *this_);

#include "vehicle.h"
int navit_add_vehicle(struct navit *this_, struct vehicle *v);

int navit_set_layout_by_name(struct navit *n, const char *name);
void navit_disable_suspend(void);
int navit_block(struct navit *this_, int block);
void navit_destroy(struct navit *this_);
void navit_command_add_table(struct navit*this_, struct command_table *commands, int count);
//
// extern static int navit_get_cursor_pnt(struct navit *this_, struct point *p, int keep_orientation, int *dir);
int navit_get_cur_pnt(struct navit *this_, struct point *p);


#include "point.h"
#include "item.h"
#include "attr.h"

extern struct coord global_vehicle_pos_onscreen;
extern struct coord_geo global_last_vehicle_pos_geo;
// extern struct coord_geo global_cur_vehicle_pos_geo;
extern double ggggg_lat;
extern double ggggg_lon;


//! The navit_vehicule
struct navit_vehicle
{
	int follow;
	/*! Limit of the follow counter. See navit_add_vehicle */
	int follow_curr;
	/*! Deprecated : follow counter itself. When it reaches 'update' counts, map is recentered*/
	struct coord coord;
	int dir;
	int speed;
	struct coord last; /*< Position of the last update of this vehicle */
	struct vehicle *vehicle;
	struct attr callback;
	int animate_cursor;
};

struct navit
{
	struct attr self;
	GList *mapsets;
	GList *layouts;
	struct gui *gui;
	struct layout *layout_current;
	struct graphics *gra;
	struct action *action;
	struct transformation *trans, *trans_cursor;
	struct compass *compass;
	struct route *route;
	struct navigation *navigation;
	struct speech *speech;
	struct tracking *tracking;
	int ready;
	struct window *win;
	struct displaylist *displaylist;
	int tracking_flag;
	int orientation;
	int recentdest_count;
	int osd_configuration;
	GList *vehicles;
	GList *windows_items;
	struct navit_vehicle *vehicle;
	struct callback_list *attr_cbl;
	struct callback *nav_speech_cb, *roadbook_callback, *popup_callback, *route_cb, *progress_cb;
	struct datawindow *roadbook_window;
	struct map *former_destination;
	struct point pressed, last, current;
	int button_pressed, moved, popped, zoomed;
	int center_timeout;
	int autozoom_secs;
	int autozoom_min;
	int autozoom_active;
	struct event_timeout *button_timeout, *motion_timeout;
	struct callback *motion_timeout_callback;
	int ignore_button;
	int ignore_graphics_events;
	struct log *textfile_debug_log;
	struct pcoord destination;
	int destination_valid;
	int blocked;
	int w, h;
	int drag_bitmap;
	int use_mousewheel;
	struct messagelist *messages;
	struct callback *resize_callback, *button_callback, *motion_callback, *predraw_callback;
	struct vehicleprofile *vehicleprofile;
	GList *vehicleprofiles;
	int pitch;
	int follow_cursor;
	int prevTs;
	int graphics_flags;
	int zoom_min, zoom_max;
	int radius;
	struct bookmarks *bookmarks;
	int flags;
	/* 1=No graphics ok */
	/* 2=No gui ok */
	int border;
	int imperial;
};

// global navit struct, needed almost everywhere
struct navit *global_navit;

// dirty global var for waypoint bitmap
struct graphics_image *global_img_waypoint;

char* g_strdup_printf_4_str(const char *format, const char *str1, const char *str2, const char *str3, const char *str4);
char* g_strdup_printf_2_str(const char *format, const char *str1, const char *str2);

/* end of prototypes */
#ifdef __cplusplus
}
#endif

#endif

