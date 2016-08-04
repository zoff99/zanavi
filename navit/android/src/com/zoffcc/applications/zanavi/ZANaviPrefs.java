package com.zoffcc.applications.zanavi;

public class ZANaviPrefs
{
	boolean PREF_use_fast_provider;
	boolean PREF_follow_gps;
	boolean PREF_use_compass_heading_base;
	boolean PREF_use_compass_heading_always;
	boolean PREF_allow_gui_internal;
	boolean PREF_show_vehicle_in_center;
	boolean PREF_use_imperial;
	boolean PREF_use_compass_heading_fast;
	boolean PREF_use_anti_aliasing;
	boolean PREF_use_map_filtering;
	boolean PREF_gui_oneway_arrows;
	boolean PREF_c_linedrawing;
	boolean PREF_show_debug_messages;
	boolean PREF_show_3d_map = false;
	boolean PREF_use_lock_on_roads;
	boolean PREF_use_route_highways;
	boolean PREF_save_zoomlevel;
	boolean PREF_show_sat_status;
	boolean PREF_use_agps;
	boolean PREF_enable_debug_functions;
	boolean PREF_enable_debug_write_gpx = false;
	boolean PREF_enable_debug_enable_comm = false;
	boolean PREF_speak_street_names;
	boolean PREF_shrink_on_high_dpi;
	int PREF_search_country = 1; // default=*ALL*
	int PREF_zoomlevel_num = 174698;
	boolean PREF_use_custom_font = true;
	int PREF_map_font_size = 2; // 1 -> small, 2 -> normal, 3 -> large, 4-> extra large, 4-> mega large
	int PREF_cancel_map_drawing_timeout = 1; // 0 -> short, 1-> normal, 2-> long, 3-> almost unlimited
	boolean PREF_draw_polyline_circles = true; // true -> yes (default) false -> no
	int PREF_mapcache = 10 * 1024; // in kbytes
	String PREF_navit_lang;
	int PREF_drawatorder = 1;
	String PREF_streetsearch_r = "1"; // street search radius factor (multiplier)
	String PREF_route_style = "3"; // 1 -> under green 2 -> on top blue 3 -> on top of all and blue
	String PREF_trafficlights_delay = "0"; // 0 -> dont calc traffic lights delay
	String PREF_avoid_sharp_turns = "0"; // 0 -> normal routing, 1 -> avoid sharp turns
	boolean PREF_autozoom_flag = true; // false -> no autozoom true -> use autozoom
	boolean PREF_item_dump = false;
	boolean PREF_use_smooth_drawing = true;
	int PREF_show_real_gps_pos = 0; // show real gps pos on map
	boolean PREF_use_more_smooth_drawing = false;
	boolean PREF_show_route_rects = false;
	int PREF_more_map_detail = 0; // 0 -> *normal* , higher values show more detail (and use a lot more CPU!!)
	boolean PREF_show_multipolygons = true;
	boolean PREF_use_index_search = true;
	boolean PREF_show_2d3d_toggle = true;
	String[] PREF_StreetSearchStrings = new String[Navit.STREET_SEARCH_STRINGS_SAVE_COUNT];
	boolean PREF_speak_filter_special_chars = true;
	boolean PREF_show_vehicle_3d = true;
	boolean PREF_streets_only = false;
	String PREF_routing_profile = "car"; // 'car' -> car , 'bike' -> bicycle
	int PREF_road_prio_weight_street_1_city = 30;

	int PREF_road_priority_001 = 68;
	int PREF_road_priority_002 = 329;
	int PREF_road_priority_003 = 5000;
	int PREF_road_priority_004 = 5;
	int PREF_current_theme = Navit.DEFAULT_THEME_OLD_DARK; // what theme/style to use
	int PREF_current_theme_M = Navit.DEFAULT_THEME_OLD_DARK_M; // what theme/style to use
	boolean PREF_show_status_bar = true;
	boolean PREF_show_poi_on_map = false;
	String PREF_last_selected_dir_gpxfiles = "";
	int PREF_tracking_connected_pref = 280;
	int PREF_tracking_angle_pref = 40;
	boolean PREF_roadspeed_warning = false; // warning of going faster than speed allowed on this road
	int PREF_roadspeed_warning_margin = 20;
	boolean PREF_lane_assist = false; // shows lanes to drive on next
	int PREF_routing_engine = 0; // 0 -> offline-ZANavi, 1 -> online-OSRM
	int PREF_traffic_speed_factor = 83;
	boolean PREF_show_maps_debug_view = false;
	boolean PREF_show_turn_restrictions = false;
	boolean PREF_auto_night_mode = true;
	int PREF_night_mode_lux = 10;
	int PREF_night_mode_buffer = 20;

	static void deep_copy(ZANaviPrefs src, ZANaviPrefs dst)
	{
		dst.PREF_use_fast_provider = src.PREF_use_fast_provider;
		dst.PREF_follow_gps = src.PREF_follow_gps;
		dst.PREF_use_compass_heading_base = src.PREF_use_compass_heading_base;
		dst.PREF_use_compass_heading_always = src.PREF_use_compass_heading_always;
		dst.PREF_allow_gui_internal = src.PREF_allow_gui_internal;
		dst.PREF_show_vehicle_in_center = src.PREF_show_vehicle_in_center;
		dst.PREF_use_imperial = src.PREF_use_imperial;
		dst.PREF_use_compass_heading_fast = src.PREF_use_compass_heading_fast;
		dst.PREF_use_anti_aliasing = src.PREF_use_anti_aliasing;
		dst.PREF_use_map_filtering = src.PREF_use_map_filtering;
		dst.PREF_gui_oneway_arrows = src.PREF_gui_oneway_arrows;
		dst.PREF_c_linedrawing = src.PREF_c_linedrawing;
		dst.PREF_show_debug_messages = src.PREF_show_debug_messages;
		dst.PREF_show_3d_map = src.PREF_show_3d_map;
		dst.PREF_use_lock_on_roads = src.PREF_use_lock_on_roads;
		dst.PREF_use_route_highways = src.PREF_use_route_highways;
		dst.PREF_save_zoomlevel = src.PREF_save_zoomlevel;
		dst.PREF_show_sat_status = src.PREF_show_sat_status;
		dst.PREF_use_agps = src.PREF_use_agps;
		dst.PREF_enable_debug_functions = src.PREF_enable_debug_functions;
		dst.PREF_enable_debug_write_gpx = src.PREF_enable_debug_write_gpx;
		dst.PREF_enable_debug_enable_comm = src.PREF_enable_debug_enable_comm;
		dst.PREF_speak_street_names = src.PREF_speak_street_names;
		dst.PREF_shrink_on_high_dpi = src.PREF_shrink_on_high_dpi;
		dst.PREF_search_country = src.PREF_search_country;
		dst.PREF_zoomlevel_num = src.PREF_zoomlevel_num;
		dst.PREF_use_custom_font = src.PREF_use_custom_font;
		dst.PREF_map_font_size = src.PREF_map_font_size;
		dst.PREF_cancel_map_drawing_timeout = src.PREF_cancel_map_drawing_timeout;
		dst.PREF_draw_polyline_circles = src.PREF_draw_polyline_circles;
		dst.PREF_mapcache = src.PREF_mapcache;
		dst.PREF_navit_lang = src.PREF_navit_lang;
		dst.PREF_drawatorder = src.PREF_drawatorder;
		dst.PREF_streetsearch_r = src.PREF_streetsearch_r;
		dst.PREF_route_style = src.PREF_route_style;
		dst.PREF_trafficlights_delay = src.PREF_trafficlights_delay;
		dst.PREF_avoid_sharp_turns = src.PREF_avoid_sharp_turns;
		dst.PREF_autozoom_flag = src.PREF_autozoom_flag;
		dst.PREF_item_dump = src.PREF_item_dump;
		dst.PREF_use_smooth_drawing = src.PREF_use_smooth_drawing;
		dst.PREF_show_real_gps_pos = src.PREF_show_real_gps_pos;
		dst.PREF_use_more_smooth_drawing = src.PREF_use_more_smooth_drawing;
		dst.PREF_show_route_rects = src.PREF_show_route_rects;
		dst.PREF_more_map_detail = src.PREF_more_map_detail;
		dst.PREF_show_multipolygons = src.PREF_show_multipolygons;
		dst.PREF_use_index_search = src.PREF_use_index_search;
		dst.PREF_show_2d3d_toggle = src.PREF_show_2d3d_toggle;
		dst.PREF_speak_filter_special_chars = src.PREF_speak_filter_special_chars;
		dst.PREF_show_vehicle_3d = src.PREF_show_vehicle_3d;
		dst.PREF_streets_only = src.PREF_streets_only;
		dst.PREF_routing_profile = src.PREF_routing_profile;
		dst.PREF_road_prio_weight_street_1_city = src.PREF_road_prio_weight_street_1_city;
		dst.PREF_road_priority_001 = src.PREF_road_priority_001;
		dst.PREF_road_priority_002 = src.PREF_road_priority_002;
		dst.PREF_road_priority_003 = src.PREF_road_priority_003;
		dst.PREF_road_priority_004 = src.PREF_road_priority_004;
		dst.PREF_current_theme = src.PREF_current_theme;
		dst.PREF_current_theme_M = src.PREF_current_theme_M;
		dst.PREF_show_status_bar = src.PREF_show_status_bar;
		dst.PREF_show_poi_on_map = src.PREF_show_poi_on_map;
		dst.PREF_last_selected_dir_gpxfiles = src.PREF_last_selected_dir_gpxfiles;
		dst.PREF_tracking_connected_pref = src.PREF_tracking_connected_pref;
		dst.PREF_tracking_angle_pref = src.PREF_tracking_angle_pref;
		dst.PREF_roadspeed_warning = src.PREF_roadspeed_warning;
		dst.PREF_roadspeed_warning_margin = src.PREF_roadspeed_warning_margin;
		dst.PREF_lane_assist = src.PREF_lane_assist;
		dst.PREF_routing_engine = src.PREF_routing_engine;
		dst.PREF_traffic_speed_factor = src.PREF_traffic_speed_factor;
		dst.PREF_show_maps_debug_view = src.PREF_show_maps_debug_view;
		dst.PREF_show_turn_restrictions = src.PREF_show_turn_restrictions;
		dst.PREF_auto_night_mode = src.PREF_auto_night_mode;
		dst.PREF_night_mode_lux = src.PREF_night_mode_lux;
		dst.PREF_night_mode_buffer = src.PREF_night_mode_buffer;

		int j = 0;
		for (j = 0; j < src.PREF_StreetSearchStrings.length; j++)
		{
			dst.PREF_StreetSearchStrings[j] = src.PREF_StreetSearchStrings[j];
		}

	}
}
