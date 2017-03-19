/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2013 Zoff <zoff@zoff.cc>
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
 * Copyright (C) 2005-2011 Navit Team
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

#include "maptool.h"
#include "linguistics.h"

#ifdef MAPTOOL_USE_SQL
// sqlite 3
//#include "sqlite3.h"
// sqlite 3
#else
#ifndef SQL_DUMMY_
#define SQL_DUMMY_ 1
int sqlite3_reset(void* v)
{
	return 0;
}
int sqlite3_step(void* v)
{
	return 0;
}
int sqlite3_exec(void* v1, void* v2, void* v3, void* v4, void* v5)
{
	return 0;
}
int sqlite3_bind_int64(void* v, int i, long l)
{
	return 0;
}
int sqlite3_bind_int(void* v, int i, int i2)
{
	return 0;
}
int sqlite3_bind_double(void* v, int i, double d)
{
	return 0;
}
long sqlite3_column_int64(void* v, int i)
{
	return 0;
}
double sqlite3_column_double(void* v, int i)
{
	return 0;
}
int sqlite3_column_int(void* v, int i)
{
	return 0;
}
int sqlite3_bind_text(void* v, int i, char* c, int i2, int i3)
{
	return 0;
}
#endif

#endif

#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION

// compression
#define MINIZ_NO_ZLIB_COMPATIBLE_NAMES

#define MINIZ_NO_STDIO
#define MINIZ_NO_ARCHIVE_APIS
#define MINIZ_NO_TIME
#define MINIZ_NO_ZLIB_APIS
#define MINIZ_NO_MALLOC

#define MINIZ_HEADER_FILE_ONLY
#include "miniz.c"
// compression

#endif

#include <sys/stat.h>
#include <unistd.h>
#include <time.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include "debug.h"
#include "linguistics.h"
#include "country.h"
#include "file.h"

/* triangulate */
/*
 #ifdef SINGLE
 #define REAL float
 #else // not SINGLE
 #define REAL double
 #endif // not SINGLE
 #include "poly_to_tri002/triangle.h"
 */

#include <signal.h>

#ifdef MAPTOOL_TRIANGULATE

#include "p2t/poly2tri.h"
#include "refine/refine.h"
#define MAX_HOLES_PER_POLY 50

#endif

/* triangulate */

#define _FILE_OFFSET_BITS  64
#define _LARGEFILE_SOURCE
#define _LARGEFILE64_SOURCE




#include "osm_countrytable.h"


// function def --
static long long osm_process_street_by_manual_country_borders(GList *bl_manual, struct coord *c);
static int osm_check_all_inner_polys_of_country_id(int country_id, struct coord *c);
// function def --


extern int doway2poi;

static int in_way, in_node, in_relation;
static osmid nodeid, wayid;
osmid current_id;

static GHashTable *attr_hash, *country_table_hash;

void add_boundary_to_db(long long current_id, int admin_level, struct item_bin *item_bin_3, struct relation_member *memb);
void add_waynode_to_db(osmid ref, int c_count);

static char *attr_present;
static int attr_present_count;

static struct item_bin item;

int maxspeed_attr_value;

char debug_attr_buffer[BUFFER_SIZE];

int flags[4];

int flags_attr_value;

struct attr_bin osmid_attr;
osmid osmid_attr_value;

char is_in_buffer[BUFFER_SIZE];

char attr_strings_buffer[BUFFER_SIZE * 16];
int attr_strings_buffer_len;
int alt_name_found = 0;
int real_alt_name_found = 0;

struct coord coord_buffer[65536];
struct coord coord_buffer_3[65536];

struct attr_mapping
{
	enum item_type type;
	int attr_present_idx_count;
	int attr_present_idx[0];
};

static void nodes_ref_item_bin(struct item_bin *ib, int local_thread_num, osmid this_way_id);

static struct attr_mapping **attr_mapping_node;
static int attr_mapping_node_count;
static struct attr_mapping **attr_mapping_way;
static int attr_mapping_way_count;
static struct attr_mapping **attr_mapping_way2poi;
static int attr_mapping_way2poi_count;

static char *attr_present;
static int attr_present_count;

static long long seekpos1;

enum attr_strings
{
	attr_string_phone, attr_string_fax, attr_string_email, attr_string_url, attr_string_street_name, attr_string_street_name_systematic, attr_string_house_number, attr_string_label, attr_string_label_alt, attr_string_label_real_alt, attr_string_postal,
    attr_string_population, attr_string_county_name, attr_string_colour_, attr_string_capacity,
	attr_string_street_name_systematic_nat,
	attr_string_street_name_systematic_int,
	attr_string_ref,
	attr_string_exit_to,
	attr_string_street_destination,
	attr_string_street_lanes,
	// ----------
	attr_string_street_lanes_forward,
	attr_string_street_turn_lanes,
	attr_string_street_destination_lanes,
	// ----- last ----
	attr_string_last,
};

char *attr_strings[attr_string_last];

char *osm_types[] = { "unknown", "node", "way", "relation" };

double save_node_lat;
double save_node_lon;

// #define REF_X 1073741834 --> maptool.h // lat
// #define REF_Y 240000000  --> maptool.h // lon
#define IS_REF(c) (check_decode_nodeid(c)) // --> new method
// #define IS_REF(c) ((c).x >= (1 << 30)) // --> old method again

// #define SET_REF(c,ref) (encode_nodeid(c, ref))
// #define REF(c) (decode_nodeid(c))

void encode_nodeid(struct coord *c, osmid ref)
{
	//fprintf(stderr, "ref=%lld bb=%lld 1=%lld\n", ref, (1 << 31), ((1 << 31) >> 31));
	//fprintf(stderr, "11=%lld 22=%lld\n", (ref >> 31), (1 << 30));
	//fprintf(stderr, "encode_nodeid:ref=%lld\n", ref);

	if (ref > 0xffffffff)
	{
		c->x = (1 << 30) | (ref >> 32 );
		c->y = ref & (0xffffffff);
	}
	else
	{
		c->x = 1 << 30;
		c->y = ref;
	}

#if 0
// lat = y
int x = transform_from_geo_lat(-90);
fprintf(stderr, "y=%d %x\n", x, x);
x = transform_from_geo_lat(0);
fprintf(stderr, "y=%d %x\n", x, x);
x = transform_from_geo_lat(90);
fprintf(stderr, "y=%d %x\n", x, x);

// lon = x
int y = transform_from_geo_lon(-180);
fprintf(stderr, "x=%d %x\n", y, y);
y = transform_from_geo_lon(0);
fprintf(stderr, "x=%d %x\n", y, y);
y = transform_from_geo_lon(180);
fprintf(stderr, "x=%d %x\n", y, y);
#endif

	//fprintf(stderr, "encode_nodeid:cx=%x cy=%x 30th bit=%x ref=%x\n", c->x, c->y, (1 << 30), (ref >> 32) );
}

int check_decode_nodeid(struct coord c)
{
	// fprintf(stderr, "check_decode_nodeid:is ref=%d\n", ((c.x & (1 << 30)) == (1 << 30)) );
	// fprintf(stderr, "check_decode_nodeid:c.x=%d c.y=%d\n", c.x, c.y);

	// if ((c.x == REF_X) && (c.y == REF_Y))
	if (c.x == REF_X)
	{
		// this number is not a real coordiante, its just a dummy to know we need to lookup
		return 1;
	}
	else
	{
		// this is is real GPS coordinate
		// fprintf(stderr, "check_decode_nodeid:c.x=%d c.y=%d\n", c.x, c.y);
		return 0;
	}

	//return ((c.x & (1 << 30)) == (1 << 30));
}

osmid decode_nodeid(struct coord c)
{
	osmid ret;

	//fprintf(stderr, "decode_nodeid:x=%x y=%x val=%x\n", c.x, c.y, ((long long)(c.x ^ 0x40000000)*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2));
	if ( (c.x & (1 << 30)) == (1 << 30) )
	{
		ret = c.y;
		//fprintf(stderr, "decode_nodeid:1=%x\n", ret);
		ret = ret | ((long long)(c.x ^ 0x40000000)*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2*2);
	}
	else
	{
		ret = (osmid)c.y;
	}
	//fprintf(stderr, "decode_nodeid:ret=%lld\n", ret);

	return ret;
}



// first char - item type
//   =w - ways
//   =? - used both for nodes and ways
//   otherwise - nodes


// for coastline-only map
static char *attrmap_cl = { "n	*=*			point_unkn\n"
	"w	*=*			street_unkn\n"
	"w	natural=coastline	water_line\n" };

// for border-only map
static char *attrmap_bo = { "n	*=*			point_unkn\n"
	"w	*=*			street_unkn\n"
	"w	boundary=administrative,admin_level=2	border_country\n"
	"w	boundary=territorial,admin_level=2	border_country\n"
	"w	boundary=maritime,admin_level=2		border_country\n"
	"w	administrative=boundary,admin_level=2	border_country\n"
	"w	boundary=administrative,maritime=yes,admin_level=2	border_country\n"
	"w	boundary=administrative,maritime=yes,admin_level=4	border_country\n"
	"w	boundary=administrative,border_type=state,admin_level=4	border_country\n" };

// ==========================
// for USA
// ==========================
// admin_level = 4
// border_type = state
// boundary = administrative
// ==========================


// for normal map
static char *attrmap_normal = { "n	*=*			point_unkn\n"
////	"n	Annehmlichkeit=Hochsitz	poi_hunting_stand\n"
	"n	addr:housenumber=*	house_number\n"
	"n	aeroway=aerodrome	poi_airport\n"
	"n	aeroway=airport		poi_airport\n"
	"n	aeroway=helipad		poi_heliport\n"
	"n	aeroway=terminal	poi_airport\n"
	"n	amenity=atm		poi_atm\n"
	"n	amenity=bank		poi_bank\n"
	"n	amenity=bar		poi_bar\n"
	//"n	amenity=bench		poi_bench\n"
	"n	amenity=biergarten	poi_biergarten\n"
	"n	amenity=bus_station	poi_bus_station\n"
	"n	amenity=cafe		poi_cafe\n"
	"n	amenity=car_wash	poi_car_wash\n"
	"n	amenity=cinema		poi_cinema\n"
	"n	amenity=college		poi_school_college\n"
	"n	amenity=courthouse	poi_justice\n"
	//"n	amenity=drinking_water	poi_potable_water\n"
	"n	amenity=fast_food	poi_fastfood\n"
	"n	amenity=fire_station	poi_firebrigade\n"
	//"n	amenity=fountain	poi_fountain\n"
	"n	amenity=fuel		poi_fuel\n"
	"n	amenity=grave_yard	poi_cemetery\n"
	"n	amenity=hospital	poi_hospital\n"
	//"n	amenity=hunting_stand	poi_hunting_stand\n"
	"n	amenity=kindergarten	poi_kindergarten\n"
	"n	amenity=library		poi_library\n"
	"n	amenity=nightclub	poi_nightclub\n"
	//"n	amenity=park_bench	poi_bench\n"
	"n	amenity=parking		poi_car_parking\n"
	"n	amenity=bicycle_parking		poi_bicycle_parking\n"
	"n	amenity=bicycle_rental		poi_bicycle_rental\n"
	"n	amenity=pharmacy	poi_pharmacy\n"
	"n	amenity=place_of_worship,religion=christian	poi_church\n"
	"n	amenity=place_of_worship			poi_worship\n"
	"n	amenity=police		poi_police\n"
	//"n	amenity=post_box	poi_post_box\n"
	"n	amenity=post_office	poi_post_office\n"
	"n	amenity=prison		poi_prison\n"
	//"n	amenity=pub		poi_pub\n"
	"n	amenity=public_building	poi_public_office\n"
	//"n	amenity=recycling	poi_recycling\n"
	"n	amenity=restaurant,cuisine=fine_dining		poi_dining\n"
	"n	amenity=restaurant				poi_restaurant\n"
	"n	amenity=school		poi_school\n"
	//"n	amenity=shelter		poi_shelter\n"
	"n	amenity=taxi		poi_taxi\n"
	//"n	amenity=tec_common	tec_common\n"
	//"n	amenity=telephone	poi_telephone\n"
	"n	amenity=theatre		poi_theater\n"
	"n	amenity=toilets		poi_restroom\n"
	"n	amenity=townhall	poi_townhall\n"
	"n	amenity=university	poi_school_university\n"
	//"n	amenity=vending_machine	poi_vending_machine\n"
	"n	barrier=bollard		barrier_bollard\n"
	"n	barrier=cycle_barrier	barrier_cycle\n"
	"n	barrier=lift_gate	barrier_lift_gate\n"
	"n	car=car_rental		poi_car_rent\n"
	"n	highway=bus_station	poi_bus_station\n"
	"n	highway=bus_stop	poi_bus_stop\n"
	"n	highway=mini_roundabout	mini_roundabout\n"
	"n	highway=motorway_junction	highway_exit\n"
	"n	highway=stop		traffic_sign_stop\n"
	"n	highway=toll_booth	poi_toll_booth\n"
	"n	highway=crossing,crossing=traffic_signals	traffic_crossing_signal\n"
	"n	highway=crossing,crossing=uncontrolled	traffic_crossing_uncontrolled\n"
	"n	highway=crossing	traffic_crossing_uncontrolled\n"
	"n	highway=traffic_signals	traffic_signals\n"
	"n	highway=turning_circle	turning_circle\n"
	//"n	historic=boundary_stone	poi_boundary_stone\n"
	"n	historic=castle		poi_castle\n"
	"n	historic=memorial	poi_memorial\n"
	"n	historic=monument	poi_monument\n"
	"n	historic=ruins		poi_ruins\n"
	////	"n	historic=*		poi_ruins\n"
	"n	landuse=cemetery	poi_cemetery\n"
	//"n	leisure=fishing		poi_fish\n"
	"n	leisure=golf_course	poi_golf\n"
	"n	leisure=marina		poi_marine\n"
	//"n	leisure=playground	poi_playground\n"
	//"n	leisure=slipway		poi_boat_ramp\n"
	"n	leisure=sports_centre	poi_sport\n"
	"n	leisure=stadium		poi_stadium\n"
	//"n	man_made=tower		poi_tower\n"
	"n	military=airfield	poi_military\n"
	"n	military=barracks	poi_military\n"
	"n	military=bunker		poi_military\n"
	"n	military=danger_area	poi_danger_area\n"
	"n	military=range		poi_military\n"
	"n	natural=bay		poi_bay\n"
	"n	natural=peak,ele=*		poi_peak\n" // show only major peaks with elevation
	//"n	natural=tree		poi_tree\n"
	"n	place=city		town_label_2e5\n"
	"n	place=town		town_label_2e4\n"
	"n	place=village		town_label_2e3\n"
	"n	place=hamlet		town_label_2e2\n"
	"n	place=locality		town_label_2e0\n"
	"n	place=suburb		district_label\n"
	"n	place=quarter		district_label_1e2\n"
	"n	place=neighbourhood		district_label_1e1\n"
	//"n	power=tower		power_tower\n"
	//"n	power=sub_station	power_substation\n"
	"n	railway=halt		poi_rail_halt\n"
	"n	railway=level_crossing	poi_level_crossing\n"
	"n	railway=station		poi_rail_station\n"
	"n	railway=tram_stop	poi_rail_tram_stop\n"
	"n	shop=baker		poi_shop_baker\n"
	"n	shop=bakery		poi_shop_baker\n"
	"n	shop=beverages		poi_shop_beverages\n"
	"n	shop=bicycle		poi_shop_bicycle\n"
	"n	shop=butcher		poi_shop_butcher\n"
	"n	shop=car		poi_car_dealer_parts\n"
	"n	shop=car_repair		poi_repair_service\n"
	//"n	shop=clothes		poi_shop_apparel\n"
	//"n	shop=convenience	poi_shop_grocery\n"
	//"n	shop=drogist		poi_shop_drugstore\n"
	//"n	shop=florist		poi_shop_florist\n"
	//"n	shop=fruit		poi_shop_fruit\n"
	//"n	shop=furniture		poi_shop_furniture\n"
	//"n	shop=garden_centre	poi_shop_handg\n"
	//"n	shop=hardware		poi_shop_handg\n"
	"n	shop=hairdresser	poi_hairdresser\n"
	//"n	shop=kiosk		poi_shop_kiosk\n"
	//"n	shop=optician		poi_shop_optician\n"
	//"n	shop=parfum		poi_shop_parfum\n"
	//"n	shop=photo		poi_shop_photo\n"
	"n	shop=shoes		poi_shop_shoes\n"
	"n	shop=supermarket	poi_shopping\n"
	//"n	sport=10pin		poi_bowling\n"
	//"n	sport=baseball		poi_baseball\n"
	//"n	sport=basketball	poi_basketball\n"
	//"n	sport=climbing		poi_climbing\n"
	"n	sport=golf		poi_golf\n"
	//"n	sport=motor_sports	poi_motor_sport\n"
	//"n	sport=skiing		poi_skiing\n"
	"n	sport=soccer		poi_soccer\n"
	"n	sport=stadium		poi_stadium\n"
	"n	sport=swimming		poi_swimming\n"
	"n	sport=tennis		poi_tennis\n"
	"n	tourism=attraction	poi_attraction\n"
	"n	tourism=camp_site	poi_camp_rv\n"
	"n	tourism=caravan_site	poi_camp_rv\n"
	"n	tourism=guest_house	poi_guesthouse\n"
	"n	tourism=hostel		poi_hostel\n"
	"n	tourism=hotel		poi_hotel\n"
	"n	tourism=information	poi_information\n"
	"n	tourism=motel		poi_motel\n"
	"n	tourism=museum		poi_museum_history\n"
	//"n	tourism=picnic_site	poi_picnic\n"
	"n	tourism=theme_park	poi_resort\n"
	"n	tourism=viewpoint	poi_viewpoint\n"
	"n	tourism=zoo		poi_zoo\n"
	"n	traffic_sign=city_limit	traffic_sign_city_limit\n"
	"n	highway=speed_camera	tec_common\n"
	"w	*=*			street_unkn\n"
	"w	addr:interpolation=even	house_number_interpolation_even\n"
	"w	addr:interpolation=odd	house_number_interpolation_odd\n"
	"w	addr:interpolation=all	house_number_interpolation_all\n"
	"w	addr:interpolation=alphabetic	house_number_interpolation_alphabetic\n"
	//"w	aerialway=cable_car	lift_cable_car\n"
	//"w	aerialway=chair_lift	lift_chair\n"
	//"w	aerialway=drag_lift	lift_drag\n"
	"w	aeroway=aerodrome	poly_airport\n"
	"w	aeroway=apron		poly_apron\n"
	"w	aeroway=runway		aeroway_runway\n"
	"w	aeroway=taxiway		aeroway_taxiway\n"
	"w	aeroway=terminal	poly_terminal\n"
	"w	amenity=college		poly_college\n"
	//"w	amenity=grave_yard	poly_cemetery\n"
	"w	amenity=parking		poly_car_parking\n"
	"w	amenity=place_of_worship	poly_building\n"
	"w	amenity=university	poly_university\n"
	// "w	boundary=administrative,admin_level=2	border_country\n" --> in border map
	//"w	boundary=civil		border_civil\n"
	"w	boundary=national_park	border_national_park\n"
	"w	boundary=political	border_political\n"
	"w	building=*		poly_building\n"
	"w	contour_ext=elevation_major	height_line_1\n"
	"w	contour_ext=elevation_medium	height_line_2\n"
	"w	contour_ext=elevation_minor	height_line_3\n"
	"w	highway=bridleway	bridleway\n"
	"w	highway=bus_guideway	bus_guideway\n"
	"w	highway=construction	street_construction\n"
	"w	highway=cyclepath	cycleway\n"
	"w	highway=cycleway	cycleway\n"
	"w	highway=footway		footway\n"
	"w	highway=footway,piste:type=nordic	footway_and_piste_nordic\n"
	"w	highway=living_street	living_street\n"
	"w	highway=minor		street_1_land\n"
	"w	highway=parking_lane	street_parking_lane\n"
	"w	highway=path				path\n"
	"w	highway=path,bicycle=designated		cycleway\n"
	"w	highway=path,bicycle=official		cycleway\n"
	"w	highway=path,foot=designated		footway\n"
	"w	highway=path,foot=official		footway\n"
	"w	highway=path,horse=designated		bridleway\n"
	"w	highway=path,horse=official		bridleway\n"
	//"w	highway=path,sac_scale=alpine_hiking			hiking_alpine\n"
	//"w	highway=path,sac_scale=demanding_alpine_hiking		hiking_alpine_demanding\n"
	//"w	highway=path,sac_scale=demanding_mountain_hiking	hiking_mountain_demanding\n"
	//"w	highway=path,sac_scale=difficult_alpine_hiking		hiking_alpine_difficult\n"
	//"w	highway=path,sac_scale=hiking				hiking\n"
	//"w	highway=path,sac_scale=mountain_hiking			hiking_mountain\n"
	"w	highway=pedestrian			street_pedestrian\n"
	"w	highway=pedestrian,area=1		poly_pedestrian\n"
	"w	highway=plaza				poly_plaza\n"
	"w	highway=motorway			highway_land\n"
	"w	highway=motorway,rural=0		highway_city\n"
	"w	highway=motorway_link			ramp_highway_land\n"
	"w	highway=trunk				street_n_lanes\n"
	"w	highway=trunk,name=*,rural=1		street_4_land\n"
	"w	highway=trunk,name=*			street_n_lanes\n"
	"w	highway=trunk,rural=0			street_n_lanes\n"
	"w	highway=trunk_link			ramp_street_4_city\n"
	"w	highway=primary				street_4_land\n"
	"w	highway=primary,name=*,rural=1		street_4_land\n"
	"w	highway=primary,name=*			street_4_city\n"
	"w	highway=primary,rural=0			street_4_city\n"
	"w	highway=primary_link			ramp_street_4_city\n"
	"w	highway=secondary			street_3_land\n"
	"w	highway=secondary,name=*,rural=1	street_3_land\n"
	"w	highway=secondary,name=*		street_3_city\n"
	"w	highway=secondary,rural=0		street_3_city\n"
	"w	highway=secondary,area=1		poly_street_3\n"
	"w	highway=secondary_link			ramp_street_3_city\n"
	"w	highway=tertiary			street_2_land\n"
	"w	highway=tertiary,name=*,rural=1		street_2_land\n"
	"w	highway=tertiary,name=*			street_2_city\n"
	"w	highway=tertiary,rural=0		street_2_city\n"
	"w	highway=tertiary,area=1			poly_street_2\n"
	"w	highway=tertiary_link			ramp_street_2_city\n"
	"w	highway=residential			street_1_city\n"
	"w	highway=residential,area=1		poly_street_1\n"
	"w	highway=unclassified			street_1_city\n"
	"w	highway=unclassified,area=1		poly_street_1\n"
	"w	highway=road				street_1_city\n"
	"w	highway=service				street_service\n"
	"w	highway=service,area=1			poly_service\n"
	"w	highway=service,service=parking_aisle	street_parking_lane\n"
	"w	highway=track				track_gravelled\n"
	"w	highway=track,surface=grass		track_grass\n"
	"w	highway=track,surface=gravel		track_gravelled\n"
	"w	highway=track,surface=ground		track_ground\n"
	"w	highway=track,surface=paved		track_paved\n"
	"w	highway=track,surface=unpaved		track_unpaved\n"
	"w	highway=track,tracktype=grade1		track_paved\n"
	"w	highway=track,tracktype=grade2		track_gravelled\n"
	"w	highway=track,tracktype=grade3		track_unpaved\n"
	"w	highway=track,tracktype=grade4		track_ground\n"
	"w	highway=track,tracktype=grade5		track_grass\n"
	"w	highway=track,surface=paved,tracktype=grade1		track_paved\n"
	"w	highway=track,surface=gravel,tracktype=grade2		track_gravelled\n"
	"w	highway=track,surface=unpaved,tracktype=grade3		track_unpaved\n"
	"w	highway=track,surface=ground,tracktype=grade4		track_ground\n"
	"w	highway=track,surface=grass,tracktype=grade5		track_grass\n"
	"w	highway=unsurfaced			track_gravelled\n"
	"w	highway=steps				steps\n"
	"w	historic=archaeological_site	poly_archaeological_site\n"
	"w	historic=battlefield	poly_battlefield\n"
	"w	historic=ruins		poly_ruins\n"
	"w	historic=town gate	poly_building\n"
	//"w	landuse=allotments	poly_allotments\n"
	//"w	landuse=basin		poly_basin\n"
	//"w	landuse=brownfield	poly_brownfield\n"
	"w	landuse=cemetery	poly_cemetery\n"
	//"w	landuse=commercial	poly_commercial\n"
	//"w	landuse=construction	poly_construction\n"
	//"w	landuse=farm		poly_farm\n"
	//"w	landuse=farmland	poly_farm\n"
	//"w	landuse=farmyard	poly_town\n"
	"w	landuse=forest		poly_wood\n"
	"w	landuse=greenfield	poly_greenfield\n"
	//"w	landuse=industrial	poly_industry\n"
	//"w	landuse=landfill	poly_landfill\n"
	"w	landuse=military	poly_military\n"
	"w	landuse=plaza		poly_plaza\n"
	//"w	landuse=quarry		poly_quarry\n"
	"w	landuse=railway		poly_railway\n"
	//"w	landuse=recreation_ground		poly_recreation_ground\n"
	"w	landuse=reservoir	poly_reservoir\n"
	//"w	landuse=residential	poly_town\n"
	//"w	landuse=residential,area=1	poly_town\n"
	//"w	landuse=retail		poly_retail\n"
	//"w	landuse=village_green	poly_village_green\n"
	//"w	landuse=vineyard	poly_farm\n"
	//"w	leisure=common		poly_common\n"
	//"w	leisure=fishing		poly_fishing\n"
	//"w	leisure=garden		poly_garden\n"
	//"w	leisure=golf_course	poly_golf_course\n"
	//"w	leisure=marina		poly_marina\n"
	//"w	leisure=nature_reserve	poly_nature_reserve\n"
	"w	leisure=park		poly_park\n"
	//"w	leisure=pitch		poly_sports_pitch\n"
	"w	leisure=playground	poly_playground\n"
	//"w	leisure=sports_centre	poly_sport\n"
	"w	leisure=stadium		poly_sports_stadium\n"
	//"w	leisure=track		poly_sports_track\n"
	"w	leisure=water_park	poly_water_park\n"
	"w	military=airfield	poly_airfield\n"
	"w	military=barracks	poly_barracks\n"
	"w	military=danger_area	poly_danger_area\n"
	"w	military=naval_base	poly_naval_base\n"
	"w	military=range		poly_range\n"
	"w	natural=beach		poly_beach\n"
	// "w	natural=coastline	water_line\n" --> in coastline map
	"w	natural=fell		poly_fell\n"
	"w	natural=glacier		poly_glacier\n"
	"w	natural=heath		poly_heath\n"
	"w	natural=land		poly_land\n"
	"w	natural=marsh		poly_marsh\n"
	"w	natural=mud		poly_mud\n"
	"w	natural=scree		poly_scree\n"
	"w	natural=scrub		poly_scrub\n"
	"w	natural=water		poly_water\n"
	"w	natural=wood		poly_wood\n"
	//"w	piste:type=downhill,piste:difficulty=advanced		piste_downhill_advanced\n"
	//"w	piste:type=downhill,piste:difficulty=easy		piste_downhill_easy\n"
	//"w	piste:type=downhill,piste:difficulty=expert		piste_downhill_expert\n"
	//"w	piste:type=downhill,piste:difficulty=freeride		piste_downhill_freeride\n"
	//"w	piste:type=downhill,piste:difficulty=intermediate	piste_downhill_intermediate\n"
	//"w	piste:type=downhill,piste:difficulty=novice		piste_downhill_novice\n"
	//"w	piste:type=nordic	piste_nordic\n"
	"w	place=suburb		poly_place1\n"
	"w	place=hamlet		poly_place2\n"
	"w	place=village		poly_place3\n"
	"w	place=municipality	poly_place4\n"
	"w	place=town		poly_place5\n"
	"w	place=city		poly_place6\n"
	//"w	power=line		powerline\n"
	//"w	railway=abandoned	rail_abandoned\n"
	//"w	railway=disused		rail_disused\n"
	"w	railway=light_rail	rail_light\n"
	"w	railway=monorail	rail_mono\n"
	"w	railway=narrow_gauge	rail_narrow_gauge\n"
	//"w	railway=preserved	rail_preserved\n"
	"w	railway=rail		rail\n"
	"w	railway=subway		rail_subway\n"
	"w	railway=tram		rail_tram\n"
	"w	route=ferry		ferry\n"
	//"w	route=ski		piste_nordic\n"
	//"w	sport=*			poly_sport\n"
	"w	tourism=artwork		poly_artwork\n"
	"w	tourism=attraction	poly_attraction\n"
	"w	tourism=camp_site	poly_camp_site\n"
	"w	tourism=caravan_site	poly_caravan_site\n"
	"w	tourism=picnic_site	poly_picnic_site\n"
	"w	tourism=theme_park	poly_theme_park\n"
	"w	tourism=zoo		poly_zoo\n"
	"w	waterway=canal		water_canal\n"
	"w	waterway=drain		water_drain\n"
	"w	waterway=river		water_river\n"
	"w	waterway=riverbank	poly_water\n"
	"w	waterway=stream		water_stream\n"
	"w	barrier=ditch	ditch\n"
	"w	barrier=hedge	hedge\n"
	"w	barrier=fence	fence\n"
	"w	barrier=wall	wall\n"
	"w	barrier=retaining_wall	retaining_wall\n"
	"w	barrier=city_wall	city_wall\n" };

static void build_attrmap_line(char *line)
{
	char *t = NULL, *kvl = NULL, *i = NULL, *p, *kv;
	struct attr_mapping *attr_mapping = g_malloc0(sizeof(struct attr_mapping));
	int idx, attr_mapping_count = 0;
	t = line;
	p = strchr(t, '\t');
	if (p)
	{
		while (*p == '\t')
			*p++ = '\0';
		kvl = p;
		p = strchr(kvl, '\t');
	}
	if (p)
	{
		while (*p == '\t')
			*p++ = '\0';
		i = p;
	}
	if (t[0] == 'w')
	{
		if (!i)
			i = "street_unkn";
	}
	else
	{
		if (!i)
			i = "point_unkn";
	}

	attr_mapping->type = item_from_name(i);

	if (!attr_mapping->type)
	{
		printf("no id found for '%s'\n", i);
	}

	while ((kv = strtok(kvl, ",")))
	{
		kvl = NULL;
		if (!(idx = (int) (long) g_hash_table_lookup(attr_hash, kv)))
		{
			idx = attr_present_count++;
			g_hash_table_insert(attr_hash, kv, (gpointer) (long) idx);
		}
		attr_mapping = g_realloc(attr_mapping, sizeof(struct attr_mapping) + (attr_mapping_count + 1) * sizeof(int));
		attr_mapping->attr_present_idx[attr_mapping_count++] = idx;
		attr_mapping->attr_present_idx_count = attr_mapping_count;
	}
	if (t[0] == 'w')
	{
		attr_mapping_way = g_realloc(attr_mapping_way, sizeof(*attr_mapping_way) * (attr_mapping_way_count + 1));
		attr_mapping_way[attr_mapping_way_count++] = attr_mapping;
	}
	if (t[0] == '?' && doway2poi)
	{
		attr_mapping_way2poi = g_realloc(attr_mapping_way2poi, sizeof(*attr_mapping_way2poi) * (attr_mapping_way2poi_count + 1));
		attr_mapping_way2poi[attr_mapping_way2poi_count++] = attr_mapping;
	}
	if (t[0] != 'w')
	{
		attr_mapping_node = g_realloc(attr_mapping_node, sizeof(*attr_mapping_node) * (attr_mapping_node_count + 1));
		attr_mapping_node[attr_mapping_node_count++] = attr_mapping;
	}

}

static void build_attrmap(FILE* rule_file)
{
	attr_hash = g_hash_table_new(g_str_hash, g_str_equal);
	attr_present_count = 1;

	// build attribute map from rule file if given
	if (rule_file)
	{
		char buffer[200], *p;
		while (fgets(buffer, 200, rule_file))
		{
			p = strchr(buffer, '\n');
			if (p)
				*p = 0;
			build_attrmap_line(g_strdup(buffer));
		}
		fclose(rule_file);
	}
	// use hardcoded default attributes
	else
	{
		char *p, *map;
		if (border_only_map == 1)
		{
			map = g_strdup(attrmap_bo);
		}
		else if (coastline_only_map == 1)
		{
			map = g_strdup(attrmap_cl);
		}
		else
		{
			map = g_strdup(attrmap_normal);
		}

		while (map)
		{
			p = strchr(map, '\n');
			if (p)
				*p++ = '\0';

			if (strlen(map))
				build_attrmap_line(map);

			map = p;
		}
	}

	attr_present = g_malloc0(sizeof(*attr_present) * attr_present_count);
}

static void build_countrytable(void)
{
	int i;
	char *names, *str, *tok;
	country_table_hash = g_hash_table_new(g_str_hash, g_str_equal);
	for (i = 0; i < sizeof(country_table) / sizeof(struct country_table); i++)
	{
		names = g_strdup(country_table[i].names);
		str = names;
		while ((tok = strtok(str, ",")))
		{
			str = NULL;
			g_hash_table_insert(country_table_hash, tok, (gpointer) & country_table[i]);
		}
	}
}

int string_endswith2(const char* ending, const char* instring)
{
	int l1;
	int l2;

	if (!ending)
	{
		return 0;
	}

	if (!instring)
	{
		return 0;
	}

    l1 = strlen(ending);
    l2 = strlen(instring);

	if (l1 < 1)
	{
		return 0;
	}

    if (l1 > l2)
	{
		return 0;
	}

    int ret = strcmp(ending, instring + (l2 - l1));
	//dbg(0, "ending=%s in=%s ret=%d\n", ending, instring + (l2 - l1), (ret == 0));
	return (ret == 0);
}


void osm_warning(char *type, long long id, int cont, char *fmt, ...)
{
	char str[4096];
	va_list ap;
	va_start(ap, fmt);
	vsnprintf(str, sizeof(str), fmt, ap);
	va_end(ap);
if (verbose_mode) fprintf(stderr,"%shttp://www.openstreetmap.org/browse/%s/"LONGLONG_FMT" %s",cont ? "":"OSM Warning:",type,id,str);
}

void osm_info(char *type, long long id, int cont, char *fmt, ...)
{
	char str[4096];
	va_list ap;
	va_start(ap, fmt);
	vsnprintf(str, sizeof(str), fmt, ap);
	va_end(ap);
if (verbose_mode) fprintf(stderr,"%shttp://www.openstreetmap.org/browse/%s/"LONGLONG_FMT" %s",cont ? "":"OSM Info:",type,id,str);
}

static void attr_strings_clear(void)
{
	alt_name_found = 0;
	real_alt_name_found = 0;
	attr_strings_buffer_len = 0;
	memset(attr_strings, 0, sizeof(attr_strings));
}

static void attr_strings_save(enum attr_strings id, char *str)
{
	attr_strings[id] = attr_strings_buffer + attr_strings_buffer_len;
	strcpy(attr_strings[id], str);
	attr_strings_buffer_len += strlen(str) + 1;
}

long long item_bin_get_nodeid(struct item_bin *ib)
{
	long long *ret = item_bin_get_attr(ib, attr_osm_nodeid, NULL);
	if (ret)
	{
		return *ret;
	}
	return 0;
}

long long item_bin_get_wayid(struct item_bin *ib)
{
	long long *ret = item_bin_get_attr(ib, attr_osm_wayid, NULL);
	if (ret)
	{
		return *ret;
	}
	return 0;
}

long long item_bin_get_relationid(struct item_bin *ib)
{
	long long *ret = item_bin_get_attr(ib, attr_osm_relationid, NULL);
	if (ret)
	{
		return *ret;
	}
	return 0;
}

long long item_bin_get_id(struct item_bin *ib)
{
	long long ret;
	if (ib->type < 0x80000000)
	{
		return item_bin_get_nodeid(ib);
	}
	ret = item_bin_get_wayid(ib);
	if (!ret)
	{
		ret = item_bin_get_relationid(ib);
	}
	return ret;
}

static int node_is_tagged;
static void relation_add_tag(char *k, char *v);
osmid get_waynode_num_have_seekpos(osmid way_id, int coord_num, int local_thread_num, off_t seek_pos);


static int access_value(char *v)
{
	if (!strcmp(v, "1"))
		return 1;
	if (!strcmp(v, "yes"))
		return 1;
	if (!strcmp(v, "designated"))
		return 1;
	if (!strcmp(v, "official"))
		return 1;
	if (!strcmp(v, "permissive"))
		return 1;
	if (!strcmp(v, "0"))
		return 2;
	if (!strcmp(v, "no"))
		return 2;
	if (!strcmp(v, "agricultural"))
		return 2;
	if (!strcmp(v, "forestry"))
		return 2;
	if (!strcmp(v, "private"))
		return 2;
	if (!strcmp(v, "delivery"))
		return 2;
	if (!strcmp(v, "destination"))
		return 2;
	return 3;
}

void osm_add_tag(char *k, char *v)
{
	int idx, level = 2;
	char buffer[BUFFER_SIZE * 2 + 2];

	if (in_relation)
	{
		relation_add_tag(k, v);
		return;
	}

	if (!strcmp(k, "ele"))
		level = 9;

	if (!strcmp(k, "time"))
		level = 9;

	if (!strcmp(k, "created_by"))
		level = 9;

	if (!strncmp(k, "tiger:", 6) || !strcmp(k, "AND_nodes"))
		level = 9;

	if (!strcmp(k, "converted_by") || !strcmp(k, "source"))
		level = 8;

	if (!strncmp(k, "osmarender:", 11) || !strncmp(k, "svg:", 4))
		level = 8;

	if (!strcmp(k, "layer"))
		level = 7;

	if (!strcasecmp(v, "true") || !strcasecmp(v, "yes"))
		v = "1";

	if (!strcasecmp(v, "false") || !strcasecmp(v, "no"))
		v = "0";





	if (!strcmp(k, "oneway"))
	{
		if (!strcmp(v, "1"))
		{
			flags[0] |= NAVIT_AF_ONEWAY | NAVIT_AF_ROUNDABOUT_VALID;
		}

		if (!strcmp(v, "-1"))
		{
			flags[0] |= NAVIT_AF_ONEWAYREV | NAVIT_AF_ROUNDABOUT_VALID;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}

	if (!strcmp(k, "cycleway"))
	{
		if (!strcmp(v, "opposite"))
		{
			flags[0] |= NAVIT_AF_ONEWAY_BICYCLE_NO;
		}
		if (!strcmp(v, "opposite_lane"))
		{
			flags[0] |= NAVIT_AF_ONEWAY_BICYCLE_NO;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}


	if (!strcmp(k, "bicycle:oneway"))
	{
		if (!strcmp(v, "0"))
		{
			flags[0] |= NAVIT_AF_ONEWAY_BICYCLE_NO;
		}
		if (!strcmp(v, "1"))
		{
			flags[0] |= NAVIT_AF_ONEWAY_BICYCLE_YES;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}

	if (!strcmp(k, "oneway:bicycle"))
	{
		if (!strcmp(v, "0"))
		{
			flags[0] |= NAVIT_AF_ONEWAY_BICYCLE_NO;
		}
		if (!strcmp(v, "1"))
		{
			flags[0] |= NAVIT_AF_ONEWAY_BICYCLE_YES;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}


/*
blue: cycleway=lane + oneway=yes
blue: cycleway=lane + oneway=yes + bicycle=yes

xxxx: cycleway=opposite_lane + cycleway:left=lane + oneway=yes
xxxx: bicycle=yes + cycleway:right=lane + oneway=yes
xxxx: cycleway:left=track + cycleway:right=lane + oneway=yes + oneway:bicycle=no
xxxx: cycleway:left=share_busway + cycleway:right=lane + oneway=yes + oneway:bicycle=no

dots: cycleway=track + oneway=yes
dots: cycleway=track

xxxx: cycleway:left=track + oneway=yes
xxxx: cycleway:right=track + oneway=yes

xxxx: cycleway:right=track
xxxx: cycleway:right=track + cycleway:right:oneway=no
*/

	if (!strcmp(k, "cycleway"))
	{
		if (!strcmp(v, "track"))
		{
			flags[0] |= NAVIT_AF_BICYCLE_TRACK;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}



/*
	cycleway=lane
	cycleway:right=lane
	cycleway:left=lane

	cycleway=opposite_lane
	cycleway:left=opposite_lane
	cycleway:right=opposite_lane
*/

	if (!strcmp(k, "cycleway"))
	{
		if (!strcmp(v, "lane"))
		{
			flags[0] |= NAVIT_AF_BICYCLE_LANE;
		}
		else if (!strcmp(v, "opposite_lane"))
		{
			flags[0] |= NAVIT_AF_BICYCLE_LANE;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}


	if (!strcmp(k, "cycleway:left"))
	{
		if (!strcmp(v, "lane"))
		{
			flags[0] |= NAVIT_AF_BICYCLE_LANE;
		}
		else if (!strcmp(v, "opposite_lane"))
		{
			flags[0] |= NAVIT_AF_BICYCLE_LANE;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}

	if (!strcmp(k, "cycleway:right"))
	{
		if (!strcmp(v, "lane"))
		{
			flags[0] |= NAVIT_AF_BICYCLE_LANE;
		}
		else if (!strcmp(v, "opposite_lane"))
		{
			flags[0] |= NAVIT_AF_BICYCLE_LANE;
		}

		if (!in_way)
		{
			level = 6;
		}
		else
		{
			level = 5;
		}
	}




	if (!strcmp(k, "junction"))
	{
		if (!strcmp(v, "roundabout"))
			flags[0] |= NAVIT_AF_ONEWAY | NAVIT_AF_ROUNDABOUT | NAVIT_AF_ROUNDABOUT_VALID;
	}

	if (!strcmp(k, "maxspeed"))
	{
		if (strstr(v, "mph"))
		{
			maxspeed_attr_value = (int) floor(atof(v) * 1.609344);
		}
		else
		{
			maxspeed_attr_value = atoi(v);
		}

		if (maxspeed_attr_value)

			flags[0] |= NAVIT_AF_SPEED_LIMIT;
		level = 5;
	}
	if (!strcmp(k, "toll"))
	{
		if (!strcmp(v, "1"))
		{
			flags[0] |= NAVIT_AF_TOLL;
		}
	}
	if (!strcmp(k, "access"))
	{
		flags[access_value(v)] |= NAVIT_AF_DANGEROUS_GOODS | NAVIT_AF_EMERGENCY_VEHICLES | NAVIT_AF_TRANSPORT_TRUCK | NAVIT_AF_DELIVERY_TRUCK | NAVIT_AF_PUBLIC_BUS | NAVIT_AF_TAXI | NAVIT_AF_HIGH_OCCUPANCY_CAR | NAVIT_AF_CAR | NAVIT_AF_MOTORCYCLE | NAVIT_AF_MOPED | NAVIT_AF_HORSE | NAVIT_AF_BIKE | NAVIT_AF_PEDESTRIAN;
		level = 5;
	}
	if (!strcmp(k, "vehicle"))
	{
		flags[access_value(v)] |= NAVIT_AF_DANGEROUS_GOODS | NAVIT_AF_EMERGENCY_VEHICLES | NAVIT_AF_TRANSPORT_TRUCK | NAVIT_AF_DELIVERY_TRUCK | NAVIT_AF_PUBLIC_BUS | NAVIT_AF_TAXI | NAVIT_AF_HIGH_OCCUPANCY_CAR | NAVIT_AF_CAR | NAVIT_AF_MOTORCYCLE | NAVIT_AF_MOPED | NAVIT_AF_BIKE;
		level = 5;
	}
	if (!strcmp(k, "motorvehicle"))
	{
		flags[access_value(v)] |= NAVIT_AF_DANGEROUS_GOODS | NAVIT_AF_EMERGENCY_VEHICLES | NAVIT_AF_TRANSPORT_TRUCK | NAVIT_AF_DELIVERY_TRUCK | NAVIT_AF_PUBLIC_BUS | NAVIT_AF_TAXI | NAVIT_AF_HIGH_OCCUPANCY_CAR | NAVIT_AF_CAR | NAVIT_AF_MOTORCYCLE | NAVIT_AF_MOPED;
		level = 5;
	}
	if (!strcmp(k, "bicycle"))
	{
		flags[access_value(v)] |= NAVIT_AF_BIKE;
		level = 5;
	}
	if (!strcmp(k, "foot"))
	{
		flags[access_value(v)] |= NAVIT_AF_PEDESTRIAN;
		level = 5;
	}
	if (!strcmp(k, "horse"))
	{
		flags[access_value(v)] |= NAVIT_AF_HORSE;
		level = 5;
	}
	if (!strcmp(k, "moped"))
	{
		flags[access_value(v)] |= NAVIT_AF_MOPED;
		level = 5;
	}
	if (!strcmp(k, "motorcycle"))
	{
		flags[access_value(v)] |= NAVIT_AF_MOTORCYCLE;
		level = 5;
	}
	if (!strcmp(k, "motorcar"))
	{
		flags[access_value(v)] |= NAVIT_AF_CAR;
		level = 5;
	}
	if (!strcmp(k, "hov"))
	{
		flags[access_value(v)] |= NAVIT_AF_HIGH_OCCUPANCY_CAR;
		level = 5;
	}
	if (!strcmp(k, "bus"))
	{
		flags[access_value(v)] |= NAVIT_AF_PUBLIC_BUS;
		level = 5;
	}
	if (!strcmp(k, "taxi"))
	{
		flags[access_value(v)] |= NAVIT_AF_TAXI;
		level = 5;
	}
	if (!strcmp(k, "goods"))
	{
		flags[access_value(v)] |= NAVIT_AF_DELIVERY_TRUCK;
		level = 5;
	}
	if (!strcmp(k, "hgv"))
	{
		flags[access_value(v)] |= NAVIT_AF_TRANSPORT_TRUCK;
		level = 5;
	}
	if (!strcmp(k, "emergency"))
	{
		flags[access_value(v)] |= NAVIT_AF_EMERGENCY_VEHICLES;
		level = 5;
	}
	if (!strcmp(k, "hazmat"))
	{
		flags[access_value(v)] |= NAVIT_AF_DANGEROUS_GOODS;
		level = 5;
	}
	if (!strcmp(k, "tunnel") && !strcmp(v, "1"))
	{
		flags[0] |= NAVIT_AF_UNDERGROUND;
	}
	if (!strcmp(k, "bridge") && !strcmp(v, "1"))
	{
		flags[0] |= NAVIT_AF_BRIDGE;
	}

	if (!strcmp(k, "note"))
	{
		level = 5;
	}

	if (!strcmp(k, "name"))
	{
		attr_strings_save(attr_string_label, v);
		level = 5;
	}

	if (!strcmp(k, "capacity"))
	{
		attr_strings_save(attr_string_capacity, v);
		level = 5;
	}

	if (!strcmp(k, "alt_name"))
	{
		// alternative name for some places (is sometimes what people call it in the local area)
		attr_strings_save(attr_string_label_real_alt, v);
		real_alt_name_found = 1;
		level = 5;
	}

	if ((!strcmp(k, "int_name")) && (alt_name_found == 0))
	{
		// only use "int_name" if we dont have "name:en"
		attr_strings_save(attr_string_label_alt, v);
		alt_name_found = 1; // lowest priority
		level = 5;
	}
	if (!strcmp(k, "name:en"))
	{
		attr_strings_save(attr_string_label_alt, v);
		alt_name_found = 2; // highest priority
		level = 5;
	}
	if (!strcmp(k, "addr:email"))
	{
		attr_strings_save(attr_string_email, v);
		level = 5;
	}
	if (!strcmp(k, "addr:housenumber"))
	{
		attr_strings_save(attr_string_house_number, v);
		level = 5;
	}
	if (!strcmp(k, "addr:street"))
	{
		attr_strings_save(attr_string_street_name, v);
		level = 5;
	}
	if (!strcmp(k, "colour"))
	{
		attr_strings_save(attr_string_colour_, v);
		level = 5;
	}
	if (!strcmp(k, "phone"))
	{
		attr_strings_save(attr_string_phone, v);
		level = 5;
	}
	if (!strcmp(k, "fax"))
	{
		attr_strings_save(attr_string_fax, v);
		level = 5;
	}
	if (!strcmp(k, "postal_code"))
	{
		attr_strings_save(attr_string_postal, v);
		level = 5;
	}
	if (!strcmp(k, "openGeoDB:postal_codes") && !attr_strings[attr_string_postal])
	{
		attr_strings_save(attr_string_postal, v);
		level = 5;
	}
	if (!strcmp(k, "population"))
	{
		attr_strings_save(attr_string_population, v);
		level = 5;
	}
	if (!strcmp(k, "openGeoDB:population") && !attr_strings[attr_string_population])
	{
		attr_strings_save(attr_string_population, v);
		level = 5;
	}
	if (!strcmp(k, "ref"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_name_systematic, v);
		}
		else
		{
			attr_strings_save(attr_string_ref, v);
		}
		level = 5;
	}
	if (! strcmp(k,"nat_ref"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_name_systematic_nat, v);
			//fprintf(stderr, "XYZ123!?:nat_ref=%s\n", v);
		}
		level=5;
	}
	if (! strcmp(k,"int_ref"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_name_systematic_int, v);
			//fprintf(stderr, "XYZ123!?:int_ref=%s\n", v);
		}
		level=5;
	}
	if (! strcmp(k,"destination:lanes"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_destination_lanes, v);
			//fprintf(stderr, "XYZ123!?:destination:lanes=%s\n", v);
		}
		level=5;
	}
	if (! strcmp(k,"destination"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_destination, v);
			//fprintf(stderr, "XYZ123!?:destination=%s\n", v);
		}
		level=5;
	}
	if (! strcmp(k,"exit_to"))
	{
		attr_strings_save(attr_string_exit_to, v);
		//fprintf(stderr, "XYZ123!?:exit_to=%s\n", v);
		level=5;
	}
	if (!strcmp(k, "openGeoDB:is_in"))
	{
		if (!is_in_buffer[0])
		{
			strcpy(is_in_buffer, v);
		}
		level = 5;
	}
	if (!strcmp(k, "is_in"))
	{
		if (!is_in_buffer[0])
		{
			strcpy(is_in_buffer, v);
		}
		level = 5;
	}
	if (!strcmp(k, "is_in:country"))
	{
		/**
		 * Sometimes there is no is_in tag, only is_in:country.
		 * I put this here so it can be overwritten by the previous if clause if there IS an is_in tag.
		 */
		strcpy(is_in_buffer, v);
		level = 5;
	}
	if (!strcmp(k, "place_county"))
	{
		/** 
		 * Ireland uses the place_county OSM tag to describe what county a town is in.
		 * This would be equivalent to is_in: Town; Locality; Country
		 * A real world example would be Node: Moycullen (52234625)
		 * The tag is processed as Moycullen; Galway; Ireland
		 * where Galway is the county
		 */
		strcpy(is_in_buffer, "Ireland");
		attr_strings_save(attr_string_county_name, v);
		level = 5;
	}
	if (!strcmp(k, "gnis:ST_alpha"))
	{
		/*	assume a gnis tag means it is part of the USA:
		 http://en.wikipedia.org/wiki/Geographic_Names_Information_System
		 many US towns do not have is_in tags
		 */
		strcpy(is_in_buffer, "USA");
		level = 5;
	}

	if (!strcmp(k, "turn:lanes"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_turn_lanes, v);
			//fprintf(stderr, "XYZ123!?:turn:lanes=%s\n", v);
		}
		level = 5;
	}


	if (!strcmp(k, "lanes:forward"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_lanes_forward, v);
			//fprintf(stderr, "XYZ123!?:lanes:forward=%s\n", v);
		}
		level = 5;
	}

	if (!strcmp(k, "lanes"))
	{
		if (in_way)
		{
			attr_strings_save(attr_string_street_lanes, v);
			//fprintf(stderr, "XYZ123!?:lanes=%s\n", v);
		}
		level = 5;
	}


	if (attr_debug_level >= level)
	{
		int bytes_left = sizeof(debug_attr_buffer) - strlen(debug_attr_buffer) - 1;
		if (bytes_left > 0)
		{
			snprintf(debug_attr_buffer + strlen(debug_attr_buffer), bytes_left, " %s=%s", k, v);
			debug_attr_buffer[sizeof(debug_attr_buffer) - 1] = '\0';
			node_is_tagged = 1;
		}
	}

	if (level < 6)
	{
		node_is_tagged = 1;
	}

	strcpy(buffer, "*=*");
	if ((idx = (int) (long) g_hash_table_lookup(attr_hash, buffer)))
	{
		attr_present[idx] = 1;
	}

	sprintf(buffer, "%s=*", k);
	if ((idx = (int) (long) g_hash_table_lookup(attr_hash, buffer)))
	{
		attr_present[idx] = 2;
	}

	sprintf(buffer, "*=%s", v);
	if ((idx = (int) (long) g_hash_table_lookup(attr_hash, buffer)))
	{
		attr_present[idx] = 2;
	}

	sprintf(buffer, "%s=%s", k, v);
	if ((idx = (int) (long) g_hash_table_lookup(attr_hash, buffer)))
	{
		attr_present[idx] = 4;
	}
}

int coord_count;

static void extend_buffer(struct buffer *b)
{
	b->malloced += b->malloced_step;
	b->base = realloc(b->base, (size_t) b->malloced);

	if (b->base == NULL)
	{
		fprintf(stderr, "realloc of "LONGLONG_FMT" bytes failed\n", (size_t) b->malloced);
		exit(1);
	}

	if (verbose_mode) fprintf(stderr, "extend_buffer: "LONGLONG_FMT" bytes\n", (size_t) b->malloced);
}

osmid nodeid_last;
GHashTable *way_hash, *waytag_hash;
cfuhash_table_t *way_hash_cfu = NULL;

typedef struct dummy_cfuhash_entry
{
	long long key;
	int data;
	struct dummy_cfuhash_entry *next;
} dummy_cfuhash_entry;

static void node_buffer_to_hash(int local_thread_num)
{
	if (verbose_mode)
	{
		fprintf(stderr, "node_buffer_to_hash t:%d nb->size:%lu\n", local_thread_num, node_buffer[local_thread_num].size);
		fprintf(stderr, "node_buffer_to_hash t:%d nb->base:%lu\n", local_thread_num, node_buffer[local_thread_num].base);
		fprintf(stderr, "node_buffer_to_hash t:%d nh:%p\n", local_thread_num, node_hash[local_thread_num]);
	}

	int i = 0;
	int count2 = node_buffer[local_thread_num].size / sizeof(struct node_item);
	struct node_item *ni = (struct node_item *) node_buffer[local_thread_num].base;

#if 0
	fprintf(stderr, "[THREAD] #%d fill hash node: count=%d size=%d\n", local_thread_num, count2, sizeof(dummy_cfuhash_entry));
#endif

	struct dummy_dummy_entry
	{
		long long key;
		int value_ptr;
	};

	fprintf_(stderr, "[THREAD] #%d fill hash node: count=%d size=%d\n", local_thread_num, count2, sizeof(struct dummy_dummy_entry));

	for (i = 0; i < count2; i++)
	{
		/*
		 if (i % 5000)
		 {
		 }
		 */

		// fprintf_(stderr, "thread #%d fill #%d/%d\n", local_thread_num, i, count2);

		// g_hash_table_insert(node_hash[local_thread_num], (gpointer) (long) (ni[i].id), (gpointer) (long) i);
		//cfuhash_put(node_hash_cfu[local_thread_num], &(long long)ni[i].id, xxx);

#if 0
		//fprintf(stderr, "node id:%lld i:%d\n", (long long)ni[i].id, i);
		cfuhash_put_data(node_hash_cfu[local_thread_num], (long long)ni[i].id, sizeof(long long), i, sizeof(int), NULL);
#endif

		// fprintf(stderr, "node id:%lld mem:%p\n", (long long)ni->id, ni);
		quick_hash_add_entry(node_hash_cfu[local_thread_num], (long long)ni[i].id, i);
		// ni++;
	}

	// fprintf_(stderr, "thread #%d node_buffer_to_hash ready\n", local_thread_num);

	if (verbose_mode)
	{
		fprintf(stderr, "node_buffer_to_hash ready\n");
	}
}

static void waytag_buffer_to_hash(void)
{
	int i, count = waytag_buffer.size / sizeof(struct way_tag);
	struct way_tag *wt = (struct way_tag *) waytag_buffer.base;
	for (i = 0; i < count; i++)
	{
		// fprintf(stderr, "waytag_hash insert:%lu %lu\n", (long) (wt[i].way_id), (long) i);
		g_hash_table_insert(waytag_hash, (gpointer) (long) (wt[i].way_id), (gpointer) (long) i);
	}
}

static struct node_item *ni;

void init_node_hash(int threads, int clear)
{
	int i;
	for (i = 0; i < threads; i++)
	{
		//if (clear)
		//{
		//*node_hash[i] = NULL;
		//}
		//else
		//{
		//*node_hash[i] = g_hash_table_new(NULL, NULL);
		//}
#if 0
		node_hash_cfu[i] = cfuhash_new_with_initial_size(CFUHASH_BUCKETS_NODES);
		cfuhash_set_flag(node_hash_cfu[i], CFUHASH_FROZEN);
#endif
		node_hash_cfu[i] = quick_hash_init(CFUHASH_BUCKETS_NODES);
	}
}

void fill_hash_node(int local_thread_num)
{
	//fprintf(stderr, "001t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);

	if (verbose_mode)
	{
		fprintf(stderr, "fill_hash_node - START\n");
	}
	//if (node_hash[local_thread_num])
	if (node_hash_cfu[local_thread_num])
	{
#if 0
		//g_hash_table_destroy(node_hash[local_thread_num]);
		//node_hash[local_thread_num] = NULL;
		cfuhash_clear(node_hash_cfu[local_thread_num]);
		cfuhash_destroy(node_hash_cfu[local_thread_num]);
		node_hash_cfu[local_thread_num] = NULL;
#endif
		quick_hash_destroy(node_hash_cfu[local_thread_num]);
		node_hash_cfu[local_thread_num] = NULL;
	}

#if 0
	// node_hash[local_thread_num] = g_hash_table_new(NULL, NULL);
	node_hash_cfu[local_thread_num] = cfuhash_new_with_initial_size(CFUHASH_BUCKETS_NODES);
	cfuhash_set_flag(node_hash_cfu[local_thread_num], CFUHASH_FROZEN);
#endif
	node_hash_cfu[local_thread_num] = quick_hash_init(CFUHASH_BUCKETS_NODES);
	

	//fprintf(stderr, "002t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);
	node_buffer_to_hash(local_thread_num);

	quick_hash_print_stats(node_hash_cfu[local_thread_num]);

	//fprintf(stderr, "003t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);
	if (verbose_mode)
	{
		fprintf(stderr, "fill_hash_node - END\n");
	}
}

void flush_nodes(int final, int local_thread_num)
{
	save_buffer("coords.tmp", &node_buffer[local_thread_num], slices * slice_size);
	//fprintf(stderr, "004t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);

	if (verbose_mode)
	{
		fprintf(stderr, "flush_nodes %d\n", final);
	}

	if (!final)
	{
		//fprintf(stderr, "005t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);
		node_buffer[local_thread_num].size = 0;
		//fprintf(stderr, "006t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);

		//if (node_hash[local_thread_num])
		if (node_hash_cfu[local_thread_num])
		{
			if (verbose_mode) fprintf(stderr,"node_hash size="LONGLONG_FMT"\n", g_hash_table_size (node_hash[local_thread_num]));
#if 0
			//g_hash_table_destroy(node_hash[local_thread_num]);
			//node_hash[local_thread_num] = NULL;
			cfuhash_clear(node_hash_cfu[local_thread_num]);
			cfuhash_destroy(node_hash_cfu[local_thread_num]);
			node_hash_cfu[local_thread_num] = NULL;
#endif
			quick_hash_destroy(node_hash_cfu[local_thread_num]);
			node_hash_cfu[local_thread_num] = NULL;
		}
	}

	if (verbose_mode) fprintf(stderr,"node: node_buffer size="LONGLONG_FMT"\n", node_buffer[local_thread_num].size);
	if (verbose_mode) fprintf(stderr,"node: node_buffer malloced="LONGLONG_FMT"\n", node_buffer[local_thread_num].malloced);
	//fprintf(stderr, "007t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);

	slices++;
}



inline double transform_to_geo_lat(int y)
{
	/* ZZ GEO TRANS ZZ */
	return TO_GEO_LAT_(y);
}

inline double transform_to_geo_lon(int x)
{
	/* ZZ GEO TRANS ZZ */
	return TO_GEO_LON_(x);
}

inline int transform_from_geo_lat(double lat)
{
	/* ZZ GEO TRANS ZZ */
	return FROM_GEO_LAT_(lat);
}

inline int transform_from_geo_lon(double lon)
{
	/* ZZ GEO TRANS ZZ */
	return FROM_GEO_LON_(lon);
}

void osm_add_node(osmid id, double lat, double lon)
{
	in_node = 1;
	if (node_buffer[0].size + sizeof(struct node_item) > node_buffer[0].malloced)
	{
		extend_buffer(&node_buffer[0]);
	}
	attr_strings_clear();
	node_is_tagged = 0;
	nodeid = id;
	item.type = type_point_unkn;
	debug_attr_buffer[0] = '\0';
	is_in_buffer[0] = '\0';
	debug_attr_buffer[0] = '\0';
	osmid_attr.type = attr_osm_nodeid;
	osmid_attr.len = 3;
	osmid_attr_value = id;
	if (node_buffer[0].size + sizeof(struct node_item) > slice_size)
	{
		flush_nodes(0, 0);
	}
	ni = (struct node_item *) (node_buffer[0].base + node_buffer[0].size);
	ni->id = id;
	ni->ref_node = 0;
	ni->ref_way = 0;
	ni->ref_ref = 0;
	ni->dummy = 0;

	// fprintf(stderr, "NNDD:000:%f %f\n", lat, lon);

	/* ZZ GEO TRANS ZZ */
	ni->c.x = FROM_GEO_LON_(lon);
	ni->c.y = FROM_GEO_LAT_(lat);
	/* ZZ GEO TRANS ZZ */

	// fprintf(stderr, "NNDD:002:%d %d\n", ni->c.x, ni->c.y);


	// save values -----
	save_node_lat = lat;
	save_node_lon = lon;
	// save values -----


#ifdef MAPTOOL_USE_SQL

	if (sql_counter2 == 0)
	{
		sqlite3_exec(sql_handle002a, "BEGIN", 0, 0, 0);
		sqlite3_exec(sql_handle003a, "BEGIN", 0, 0, 0);
		sqlite3_exec(sql_handle002b, "BEGIN", 0, 0, 0);
		sqlite3_exec(sql_handle003b, "BEGIN", 0, 0, 0);
	}
	sql_counter2++;

#ifdef MAPTOOL_SPLIT_NODE_DB
	if (nodeid % 2)
	{
		if ((nodeid & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
			sqlite3_bind_int64(stmt_node__2a, 1, nodeid);
			sqlite3_bind_double(stmt_node__2a, 2, lat);
			sqlite3_bind_double(stmt_node__2a, 3, lon);
			sqlite3_step(stmt_node__2a);
			sqlite3_reset(stmt_node__2a);
		}
		else
		{
			sqlite3_bind_int64(stmt_node__2b, 1, nodeid);
			sqlite3_bind_double(stmt_node__2b, 2, lat);
			sqlite3_bind_double(stmt_node__2b, 3, lon);
			sqlite3_step(stmt_node__2b);
			sqlite3_reset(stmt_node__2b);
		}
	}
	else
	{
		if ((nodeid & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
#endif
			sqlite3_bind_int64(stmt_nodea, 1, nodeid);
			sqlite3_bind_double(stmt_nodea, 2, lat);
			sqlite3_bind_double(stmt_nodea, 3, lon);
			sqlite3_step(stmt_nodea);
			sqlite3_reset(stmt_nodea);
#ifdef MAPTOOL_SPLIT_NODE_DB
		}
		else
		{
			sqlite3_bind_int64(stmt_nodeb, 1, nodeid);
			sqlite3_bind_double(stmt_nodeb, 2, lat);
			sqlite3_bind_double(stmt_nodeb, 3, lon);
			sqlite3_step(stmt_nodeb);
			sqlite3_reset(stmt_nodeb);
		}
	}
#endif

	if (sql_counter2 > MAX_ROWS_WO_COMMIT)
	{
		sql_counter2 = 0;
		sqlite3_exec(sql_handle002a, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle003a, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle002b, "COMMIT", 0, 0, 0);
		sqlite3_exec(sql_handle003b, "COMMIT", 0, 0, 0);
		//fprintf(stderr, "SQL: COMMIT\n");
	}

#endif

	//fprintf(stderr,"node: %d %d\n", ni->c.x, ni->c.y);
	//fprintf(stderr,"node: %f %f\n", lat, lon);


	node_buffer[0].size += sizeof(struct node_item);
	//fprintf(stderr,"node: inc node_buffer by %d\n", sizeof(struct node_item));
	//fprintf(stderr,"node: node_buffer size="LONGLONG_FMT"\n", node_buffer.size);

#if 0
	//if (!node_hash[0])
	if (!node_hash_cfu[0])
	{
		if (ni->id > nodeid_last)
		{
			nodeid_last = ni->id;
		}
		else
		{
			//fprintf(stderr,"INFO: Nodes out of sequence (new %d vs old %d), adding hash\n", ni->id, nodeid_last);
			//node_hash[0] = g_hash_table_new(NULL, NULL);

			node_hash_cfu[0] = cfuhash_new_with_initial_size(CFUHASH_BUCKETS_OTHER);
			cfuhash_set_flag(node_hash_cfu[0], CFUHASH_FROZEN);

			node_buffer_to_hash(0);
		}
	}
	//else if (!g_hash_table_lookup(node_hash[0], (gpointer) (long) (ni->id)))
	// else if (!cfuhash_exists(node_hash_cfu[0], (long long) ni->id ))

	else if (!cfuhash_exists_data(node_hash_cfu[0], (long long) ni->id, sizeof(long long)))
	{
		//g_hash_table_insert(node_hash[0], (gpointer) (long) (ni->id), (gpointer) (long) (ni - (struct node_item *) node_buffer[0].base));
		//cfuhash_put(node_hash_cfu[0], (gpointer)ni->id, (gpointer)(long) (ni - (struct node_item *) node_buffer[0].base) );
		cfuhash_put_data(node_hash_cfu[0], (long long)ni->id, sizeof(long long), (long) (ni - (struct node_item *) node_buffer[0].base), 1, NULL);

	}
	else
	{
		node_buffer[0].size -= sizeof(struct node_item);
		if (verbose_mode)
		fprintf(stderr, "node: Decr. node_buffer by %d\n", sizeof(struct node_item));
		if (verbose_mode) fprintf(stderr,"node: node_buffer size="LONGLONG_FMT"\n", node_buffer[0].size);
		nodeid = 0;
	}
#endif

}

void clear_node_item_buffer(void)
{
	int j, count = node_buffer[0].size / sizeof(struct node_item);
	struct node_item *ni = (struct node_item *) (node_buffer[0].base);
	for (j = 0; j < count; j++)
	{
		ni[j].ref_way = 0;
	}
}

static struct node_item *
node_item_get_fast(osmid id, int local_thread_num)
{
#if 1
	int i;
	//void *p_tmp;
	//int rr;
	//size_t data_size2 = sizeof(int);
	struct node_item *ni = (struct node_item *) (node_buffer[local_thread_num].base);
#endif

	//if (node_hash[local_thread_num])
	if (node_hash_cfu[local_thread_num])
	{
#if 0
		//p_tmp = (gpointer)(g_hash_table_lookup(node_hash[local_thread_num], (gpointer) (long) id));
		rr = cfuhash_get_data(node_hash_cfu[local_thread_num], (long long) id, sizeof(long long), &p_tmp, &data_size2);
		if (rr == 1)
		{
			//fprintf(stderr, "got key=%lld value=%d\n", (long long) id, p_tmp);
			i = p_tmp;
			return ni + i;
		}
#endif

		//struct node_item *ni = quick_hash_lookup(node_hash_cfu[local_thread_num], id);
		//fprintf(stderr, "got key=%lld value=%p\n", (long long)id, ni);
		//if (ni)
		//{
		//	fprintf(stderr, "x=%d y=%d\n", ni->c.x, ni->c.y);
		//}
		i = quick_hash_lookup(node_hash_cfu[local_thread_num], id);
		if (i != -1)
		{
			return ni + i; // move pointer "i" number of items forward
		}

		//if (p_tmp != NULL)
		//{
		//	// only if node is found in hash!
		//	i = (int) (long) (p_tmp);
		//	return ni + i;
		//}
	}
	return NULL;
}

static int load_node(FILE *coords, int p, struct node_item *ret)
{
	fseek(coords, p * sizeof(struct node_item), SEEK_SET);
	if (fread(ret, sizeof(*ret), 1, coords) != 1)
	{
		fprintf(stderr, "read failed\n");
		return 0;
	}
	return 1;
}

static osmid first_node_of_current_way;

void osm_add_way(osmid id)
{
	static osmid wayid_last;

	in_way = 1;
	wayid = id;
	coord_count = 0;
	attr_strings_clear();
	item.type = type_street_unkn;
	debug_attr_buffer[0] = '\0';
	maxspeed_attr_value = 0;
	flags_attr_value = 0;
	memset(flags, 0, sizeof(flags));
	debug_attr_buffer[0] = '\0';
	osmid_attr_value = id;

	if (wayid < wayid_last && !way_hash_cfu)
	{
		if (verbose_mode)
		{
			fprintf(stderr, "INFO: Ways out of sequence (new %d vs old %d), adding hash\n", wayid, wayid_last);
		}
		// way_hash = g_hash_table_new(NULL, NULL);
		way_hash_cfu = cfuhash_new_with_initial_size(CFUHASH_BUCKETS_WAYS);
		cfuhash_set_flag(way_hash_cfu, CFUHASH_FROZEN);

	}
	wayid_last = wayid;
}

char relation_type[BUFFER_SIZE];
char iso_code[BUFFER_SIZE];
int admin_level;
int boundary;

void osm_add_relation(osmid id)
{
	current_id = id;
	in_relation = 1;
	debug_attr_buffer[0] = '\0';
	relation_type[0] = '\0';
	iso_code[0] = '\0';
	admin_level = -1;
	boundary = 0;

	// zero out buffer -----------------------
	// bzero(item_bin_2, MAX_ITEMBIN_BYTES_);
	// zero out buffer -----------------------

	item_bin_init(item_bin_2, type_none);
	item_bin_add_attr_longlong(item_bin_2, attr_osm_relationid, current_id);
}

static int country_id_from_iso2(char *iso)
{
	int ret = 0;
	if (iso)
	{
		// make uppercase
		int i99;
		for (i99 = 0; iso[i99]; i99++)
		{
			iso[i99] = toupper(iso[i99]);
		}
		struct country_search *search;
		struct attr country_iso2, country_id;
		struct item *item;
		country_iso2.type = attr_country_iso2;
		country_iso2.u.str = iso;
		search = country_search_new(&country_iso2, 0);
		if ((item = country_search_get_item(search)) && item_attr_get(item, attr_country_id, &country_id))
		{
			ret = country_id.u.num;
			if (debug_itembin(3))
			{
				fprintf(stderr, "country id(1)=%d\n", ret);
			}
		}
		else
		{
			if (debug_itembin(3))
			{
				fprintf(stderr, "country id(2)=%d\n", ret);
			}
		}
		country_search_destroy(search);
	}
	return ret;
}

struct country_table *country_from_countryid(int id)
{
	int i;
	for (i = 0; i < sizeof(country_table) / sizeof(struct country_table); i++)
	{
		//if (debug_itembin(3))
		//{
		//	fprintf(stderr,"== country_from_countryid: %d ==\n", country_table[i].countryid);
		//}

		if (country_table[i].countryid == id)
		{
			return &country_table[i];
		}
	}

	//if (debug_itembin(3))
	//{
	//	fprintf(stderr,"== country_from_countryid: return NULL ==\n");
	//}

	return NULL;
}

struct country_table *
country_from_iso2(char *iso)
{
	return country_from_countryid(country_id_from_iso2(iso));
}

static int get_relation_member(char *str, struct relation_member *memb)
{
	int len;
	sscanf(str,"%d:"LONGLONG_FMT":%n",&memb->type,&memb->id,&len);
	memb->role = str + len;
	return 1;
}

#if 0
void report(io, markers, reporttriangles, reportneighbors, reportsegments, reportedges, reportnorms)
struct triangulateio *io;int markers;int reporttriangles;int reportneighbors;int reportsegments;int reportedges;int reportnorms;
{
	int i, j;

	for (i = 0; i < io->numberofpoints; i++)
	{
		fprintf(stderr, "Point %4d:", i);
		for (j = 0; j < 2; j++)
		{
			fprintf(stderr, "  %.6g", io->pointlist[i * 2 + j]);
		}
		if (io->numberofpointattributes > 0)
		{
			fprintf(stderr, "   attributes");
		}
		for (j = 0; j < io->numberofpointattributes; j++)
		{
			fprintf(stderr, "  %.6g", io->pointattributelist[i * io->numberofpointattributes + j]);
		}
		if (markers)
		{
			fprintf(stderr, "   marker %d\n", io->pointmarkerlist[i]);
		}
		else
		{
			fprintf(stderr, "\n");
		}
	}
	fprintf(stderr, "\n");

	if (reporttriangles || reportneighbors)
	{
		for (i = 0; i < io->numberoftriangles; i++)
		{
			if (reporttriangles)
			{
				fprintf(stderr, "Triangle %4d points:", i);
				for (j = 0; j < io->numberofcorners; j++)
				{
					fprintf(stderr, "  %4d", io->trianglelist[i * io->numberofcorners + j]);
				}
				if (io->numberoftriangleattributes > 0)
				{
					fprintf(stderr, "   attributes");
				}
				for (j = 0; j < io->numberoftriangleattributes; j++)
				{
					fprintf(stderr, "  %.6g", io->triangleattributelist[i * io->numberoftriangleattributes + j]);
				}
				fprintf(stderr, "\n");
			}
			if (reportneighbors)
			{
				fprintf(stderr, "Triangle %4d neighbors:", i);
				for (j = 0; j < 3; j++)
				{
					fprintf(stderr, "  %4d", io->neighborlist[i * 3 + j]);
				}
				fprintf(stderr, "\n");
			}
		}
		fprintf(stderr, "\n");
	}

	if (reportsegments)
	{
		for (i = 0; i < io->numberofsegments; i++)
		{
			fprintf(stderr, "Segment %4d points:", i);
			for (j = 0; j < 2; j++)
			{
				fprintf(stderr, "  %4d", io->segmentlist[i * 2 + j]);
			}
			if (markers)
			{
				fprintf(stderr, "   marker %d\n", io->segmentmarkerlist[i]);
			}
			else
			{
				fprintf(stderr, "\n");
			}
		}
		fprintf(stderr, "\n");
	}

	if (reportedges)
	{
		for (i = 0; i < io->numberofedges; i++)
		{
			fprintf(stderr, "Edge %4d points:", i);
			for (j = 0; j < 2; j++)
			{
				fprintf(stderr, "  %4d", io->edgelist[i * 2 + j]);
			}
			if (reportnorms && (io->edgelist[i * 2 + 1] == -1))
			{
				for (j = 0; j < 2; j++)
				{
					fprintf(stderr, "  %.6g", io->normlist[i * 2 + j]);
				}
			}
			if (markers)
			{
				fprintf(stderr, "   marker %d\n", io->edgemarkerlist[i]);
			}
			else
			{
				fprintf(stderr, "\n");
			}
		}
		fprintf(stderr, "\n");
	}
}

#endif

void get_lat_lon_for_node(long long nd, struct node_lat_lon *node);

void get_lat_lon_way_first_node(long long way_id, struct node_lat_lon *node)
{
	int rc2;
	sqlite3_stmt *st;
	long long nd = 0;


	node->valid = 0;

#ifdef MAPTOOL_SPLIT_WAYNODE_DB
	if ((way_id & MAPTOOL_SPLIT_WAYNODE_BIT) > 0)
	{
		if ((way_id & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
		{
#endif
			st = stmt_sel001;
#ifdef MAPTOOL_SPLIT_WAYNODE_DB
		}
		else
		{
			st = stmt_sel001b;
		}
	}
	else
	{
		if ((way_id & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
		{
			st = stmt_sel001__2;
		}
		else
		{
			st = stmt_sel001__2b;
		}
	}
#endif
	sqlite3_bind_int64(st, 1, way_id);

	// execute the statement
	rc2 = sqlite3_step(st);
	switch (rc2)
	{
		case SQLITE_DONE:
		break;
		case SQLITE_ROW:
		nd = sqlite3_column_int64(st, 0);
		break;
		default:
		fprintf(stderr, "Error: %d\n", rc2);
		break;
	}
	sqlite3_reset(st);
	// sqlite3_finalize(st);

	if (nd != 0)
	{
		get_lat_lon_for_node(nd, node);
	}
}

void get_lat_lon_for_node(long long nd, struct node_lat_lon *node)
{
	int rc2;

	// now read the coords of this node
	node->valid = 0;

#ifdef MAPTOOL_USE_SQL

#ifdef MAPTOOL_SPLIT_NODE_DB
	if (nd % 2)
	{
		if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
			sqlite3_bind_int64(stmt_sel002__2a, 1, nd);
		}
		else
		{
			sqlite3_bind_int64(stmt_sel002__2b, 1, nd);
		}
	}
	else
	{
		if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
#endif
			sqlite3_bind_int64(stmt_sel002a, 1, nd);
#ifdef MAPTOOL_SPLIT_NODE_DB
		}
		else
		{
			sqlite3_bind_int64(stmt_sel002b, 1, nd);
		}
	}
#endif

	rc2 = 0;
	//do
	//{
#ifdef MAPTOOL_SPLIT_NODE_DB
	if (nd % 2)
	{
		if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
			rc2 = sqlite3_step(stmt_sel002__2a);
		}
		else
		{
			rc2 = sqlite3_step(stmt_sel002__2b);
		}
	}
	else
	{
		if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
#endif
			rc2 = sqlite3_step(stmt_sel002a);
#ifdef MAPTOOL_SPLIT_NODE_DB
		}
		else
		{
			rc2 = sqlite3_step(stmt_sel002b);
		}
	}
#endif
	//cols2 = sqlite3_column_count(stmt_sel002);
	switch (rc2)
	{
		//case SQLITE_DONE:
		//	break;
		case SQLITE_ROW:

#ifdef MAPTOOL_SPLIT_NODE_DB
		if (nd % 2)
		{
			if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
			{
				node->lat = (double) sqlite3_column_double(stmt_sel002__2a, 0);
				node->lon = (double) sqlite3_column_double(stmt_sel002__2a, 1);
			}
			else
			{
				node->lat = (double) sqlite3_column_double(stmt_sel002__2b, 0);
				node->lon = (double) sqlite3_column_double(stmt_sel002__2b, 1);
			}
		}
		else
		{
			if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
			{
#endif
				node->lat = (double) sqlite3_column_double(stmt_sel002a, 0);
				node->lon = (double) sqlite3_column_double(stmt_sel002a, 1);
#ifdef MAPTOOL_SPLIT_NODE_DB
			}
			else
			{
				node->lat = (double) sqlite3_column_double(stmt_sel002b, 0);
				node->lon = (double) sqlite3_column_double(stmt_sel002b, 1);
			}
		}
#endif
		node->valid = 1;
		break;
		default:
		fprintf(stderr, "get_lat_lon_for_node:SQL Error: %d nodeid:%lld\n", rc2, nd);
		break;
	}
	//}
	//while (rc2 == SQLITE_ROW);
#ifdef MAPTOOL_SPLIT_NODE_DB
	if (nd % 2)
	{
		if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
			sqlite3_reset(stmt_sel002__2a);
		}
		else
		{
			sqlite3_reset(stmt_sel002__2b);
		}
	}
	else
	{
		if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
		{
#endif
			sqlite3_reset(stmt_sel002a);
#ifdef MAPTOOL_SPLIT_NODE_DB
		}
		else
		{
			sqlite3_reset(stmt_sel002b);
		}
	}
#endif
#endif

}

void catch_signal(int param)
{
	// fprintf (stderr, "Terminating program...\n");
	fprintf(stderr, "****** got SIGSEGV ******\n");
	THROW(MAPTOOL_00002_EXCEPTION);
	// exit(1);
}

struct way_and_endnodes
{
	unsigned long long way_id;
	unsigned long long first_node_id;
	unsigned long long last_node_id;
	int reverse;
	int role;
	GList *nodes;
};

// if same value	-> 0
// otherwise		-> 1
gint comp_func(gconstpointer item1, gconstpointer item2)
{
	long long a;
	long long b;

	a = *((long long *) item1);
	b = *((long long *) item2);

	//fprintf(stderr," item1=%lld\n", a);
	//fprintf(stderr," item2=%lld\n", b);

	if (a == b)
	{
		return 0;
	}

	return 1;
}

// return 0 if item is not found in GList
int glist_find_already_inside(GList* list, gconstpointer item)
{
	if (g_list_find_custom(list, item, (GCompareFunc) comp_func) == NULL)
	{
		return 0;
	}
	return 1;
}

void osm_end_relation(struct maptool_osm *osm)
{
	//fprintf(stderr, "XXX000\n");

	struct item_bin *item_bin_tri;
	struct coord coord_tri;
	struct node_lat_lon node_coords;
	int i9;
	int j9;
	int tmp9;

	int save_relation = 0;
	int save_relations_other_method = 0;
	int tag_id = 1;
	char *str = NULL;
	int triangulate_not_done = 1;
	int problem = 0;
	struct relation_member memb;
	struct relation_member memb2;
	struct way_and_endnodes *way_nodes1;
	struct way_and_endnodes *way_nodes2;
	int found_new_way = 0;
	gpointer *gptr;
	int rc = 0;
	int rc2 = 0;
	int cols;
	int col;
	int first_node_found = 0;
	long long nd;
	long long first_nd;
	long long search_nd;
	long long *ndptr;

	/*
	 long lat_nd;
	 long lon_nd;
	 */

	double lat_nd;
	double lon_nd;

	int number_of_points_in_poly = 0;
	int number_of_points_current_poly = 0;
	int exception = 0;
	int role_ = 0;

	double factor = 10000000;

	struct node_lat_lon* lat_lon_c;

	// triangulate vars
#if 0
	struct triangulateio in, mid, out, vorout;
#endif

#ifdef MAPTOOL_TRIANGULATE
	GPtrArray *current_points;
	GPtrArray *current_hole;
	P2tCDT *cdt;
	P2trCDT *rcdt;
	P2trRefiner *refiner;
	P2tTrianglePtrArray triangles;
	gint refine_max_steps = 1000;
	double x;
	double y;
	int i;
	int j;
	GHashTable *point_map;
	P2trCDT *rmesh;
#endif
	GList *all_holes;

	int count_of_outer_ways = 0;

	GList *unsorted_outer_ways = NULL;
	GList *unsorted_inner_ways = NULL;
	GList *sorted_outer_ways = NULL;
	GList *temp_nodes_list = NULL;
	all_holes = NULL;

	// -- save relations for riverbanks --
	while ((str = item_bin_get_attr(item_bin_2, attr_osm_tag, str)))
	{

		if (!strcmp(str, "waterway=riverbank"))
		{
			// fprintf(stderr,"k:v=%s\n", str);
			save_relation = 1;
			save_relations_other_method = 0;
			tag_id = 1;
		}
		else if (!strcmp(str, "natural=water"))
		{
			save_relation = 1;
			save_relations_other_method = 0;
			tag_id = 2;
		}
		/*
		 else if (!strcmp(str, "water=river"))
		 {
		 save_relation = 1;
		 tag_id = 3;
		 }
		 */
		else if (!strcmp(str, "natural=lake"))
		{
			save_relation = 1;
			save_relations_other_method = 0;
			tag_id = 4;
		}
		else if (!strcmp(str, "landuse=forest"))
		{
			// ------ DISABLED ------
			// save_relation = 1;
			// save_relations_other_method = 1;
			// tag_id = 5;
			// ------ DISABLED ------
		}
		else if (!strcmp(str, "natural=wood"))
		{
			// ------ DISABLED ------
			// save_relation = 1;
			// save_relations_other_method = 1;
			// tag_id = 6;
			// ------ DISABLED ------
		}
		else if (!strcmp(str, "building=yes"))
		{
			save_relation = 1;
			save_relations_other_method = 0;
			tag_id = 9;
			//fprintf(stderr,"k:v=%s id=%lld\n", str, current_id);
		}

	}

	if (save_relation == 1)
	{

#ifdef MAPTOOL_TRIANGULATE

		if (save_relations_other_method == 0)
		{

			//fprintf(stderr, "START relation id=%lld tag=%d\n", current_id, tag_id);

			sqlite3_stmt *st;

			triangulate_not_done = 1;
			str = NULL;
			number_of_points_in_poly = 0;
			count_of_outer_ways = 0;
			//fprintf(stderr, "\n\n----------START---------\n");
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if (!strcmp(memb.role, "outer"))
					{
						if (memb.type == 2)
						{
							count_of_outer_ways++;
						}
					}
				}
			}
			//fprintf(stderr, "number of outer ways: %d\n", count_of_outer_ways);

			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					role_=0;
					if (!strcmp(memb.role, "outer"))
					{
						role_=1;
						//fprintf(stderr, "1:role=%s id=%lld\n", memb.role, memb.id);
					}
					else if (!strcmp(memb.role, "inner"))
					{
						role_=2;
						//fprintf(stderr, "2:role=%s id=%lld\n", memb.role, memb.id);
					}

					if ((memb.type == 2) && ( (role_ == 1)||(role_ == 2) )) // use "outer" and "inner" ways
					{
						way_nodes1 = (struct way_and_endnodes *) malloc(sizeof(struct way_and_endnodes));
						way_nodes1->nodes = NULL;
						// get id for last and first nodes

#ifdef MAPTOOL_SPLIT_WAYNODE_DB
						if ((memb.id & MAPTOOL_SPLIT_WAYNODE_BIT) > 0)
						{
							if ((memb.id & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
							{
#endif
								st = stmt_sel001;
#ifdef MAPTOOL_SPLIT_WAYNODE_DB
							}
							else
							{
								st = stmt_sel001b;
							}
						}
						else
						{
							if ((memb.id & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
							{
								st = stmt_sel001__2;
							}
							else
							{
								st = stmt_sel001__2b;
							}
						}
#endif
						sqlite3_bind_int64(st, 1, memb.id);

						first_node_found = 0;
						way_nodes1->first_node_id = 0;
						nd = 0;
						number_of_points_current_poly = 0;
						// execute the statement
						do
						{
							rc = sqlite3_step(st);
							// cols = sqlite3_column_count(st);
							switch (rc)
							{
								case SQLITE_DONE:
								break;
								case SQLITE_ROW:
								nd = sqlite3_column_int64(st, 0);
								number_of_points_current_poly++;
								//fprintf(stderr, "------ %s = %d\n", sqlite3_column_name(st, 0), nd ? nd : "NULL");
								if (first_node_found == 0)
								{
									way_nodes1->first_node_id = nd;
									way_nodes1->role = role_;
									first_node_found = 1;
									//fprintf(stderr, "------ first node found\n");
								}
								ndptr = malloc(sizeof(long long));
								*ndptr = nd;
								way_nodes1->nodes = g_list_append(way_nodes1->nodes, ndptr);
								break;
								default:
								fprintf(stderr, "Error: %d\n", rc);
								break;
							}
						}
						while (rc == SQLITE_ROW);

						// dont count the last node of each way
						number_of_points_current_poly--;

						sqlite3_reset(st);

						way_nodes1->last_node_id = nd;
						way_nodes1->way_id = memb.id;

						if (way_nodes1->last_node_id == 0)
						{
							if (way_nodes1->nodes)
							{
								way_nodes1->nodes = g_list_first(way_nodes1->nodes);
								while (way_nodes1->nodes)
								{
									ndptr = way_nodes1->nodes->data;
									way_nodes1->nodes = g_list_remove(way_nodes1->nodes, ndptr);
									g_free(ndptr);
									ndptr = NULL;
									way_nodes1->nodes = g_list_next(way_nodes1->nodes);
								}
							}
							g_free(way_nodes1);
						}
						else if (way_nodes1->last_node_id == way_nodes1->first_node_id)
						{
							//fprintf(stderr, "ln id=%lld, fn id=%lld\n", way_nodes1->last_node_id, way_nodes1->first_node_id);

							if (way_nodes1->nodes)
							{
								if (role_ == 2)
								{
									//fprintf(stderr, "role=2\n");
									unsorted_inner_ways = g_list_append(unsorted_inner_ways, way_nodes1);
								}
								else if (role_ == 1)
								{
									if (count_of_outer_ways == 1)
									{
										// we have only 1 outer way, it must be a closed loop. add it
										unsorted_outer_ways = g_list_append(unsorted_outer_ways, way_nodes1);
										number_of_points_in_poly = number_of_points_in_poly + number_of_points_current_poly;
										//fprintf(stderr, "w,sn,en %lld,%lld,%lld\n", way_nodes1->way_id, way_nodes1->first_node_id, way_nodes1->last_node_id);
										//fprintf(stderr, "num points=%d\n", number_of_points_current_poly);
										//fprintf(stderr, "SUM points=%d\n", number_of_points_in_poly);
									}
									else
									{
										// we have an outer way that is a loop (remove it)
										//fprintf(stderr, "remove node ... start\n");
										way_nodes1->nodes = g_list_first(way_nodes1->nodes);
										while (way_nodes1->nodes)
										{
											//fprintf(stderr, "remove node\n");
											ndptr = way_nodes1->nodes->data;
											way_nodes1->nodes = g_list_remove(way_nodes1->nodes, ndptr);
											g_free(ndptr);
											ndptr = NULL;
											way_nodes1->nodes = g_list_next(way_nodes1->nodes);
										}
										g_free(way_nodes1);
										//fprintf(stderr, "remove node ... ready\n");
									}
								}
							}
						}
						else
						{
							if (role_ == 1)
							{
								unsorted_outer_ways = g_list_append(unsorted_outer_ways, way_nodes1);
								number_of_points_in_poly = number_of_points_in_poly + number_of_points_current_poly;
							}
							//fprintf(stderr, "w,sn,en %lld,%lld,%lld\n", way_nodes1->way_id, way_nodes1->first_node_id, way_nodes1->last_node_id);
							//fprintf(stderr, "num points=%d\n", number_of_points_current_poly);
							//fprintf(stderr, "SUM points=%d\n", number_of_points_in_poly);
						}
					}
				}
			}

			// now sort ways to have a linked polygon
			if (unsorted_outer_ways)
			{
				unsorted_outer_ways = g_list_first(unsorted_outer_ways);
				way_nodes1 = (struct way_and_endnodes *) malloc(sizeof(struct way_and_endnodes));
				way_nodes2 = (struct way_and_endnodes *) unsorted_outer_ways->data;
				way_nodes1->way_id = way_nodes2->way_id;
				//fprintf(stderr, "(START)way id=%llu\n", way_nodes1->way_id);
				way_nodes1->last_node_id = way_nodes2->last_node_id;
				way_nodes1->first_node_id = way_nodes2->first_node_id;
				// only copy over the pointer to the node list!!
				way_nodes1->nodes = way_nodes2->nodes;
				// first node is always in the right order
				way_nodes1->reverse = 0;

				gptr = unsorted_outer_ways->data;
				sorted_outer_ways = g_list_append(sorted_outer_ways, way_nodes1);
				// save the very first node of the poly
				first_nd = way_nodes1->first_node_id;
				// what node we want to find
				search_nd = way_nodes1->last_node_id;
				unsorted_outer_ways = g_list_remove(unsorted_outer_ways, gptr);
				g_free(gptr);
				gptr = NULL;

				//fprintf(stderr,"remaining=%d sorted=%d\n", g_list_length(unsorted_outer_ways), g_list_length(sorted_outer_ways));
			}

			if (unsorted_outer_ways)
			{

				problem = 0;
				//fprintf(stderr, "start length unsorted_outer_ways=%d\n", g_list_length(unsorted_outer_ways));

				while ((g_list_length(unsorted_outer_ways) > 0) && (problem == 0))
				{
					//fprintf(stderr, "loop thru remaining ways 1\n");

					// loop thru all the remaining ways
					found_new_way = 0;
					unsorted_outer_ways = g_list_first(unsorted_outer_ways);
					while (unsorted_outer_ways)
					{
						//fprintf(stderr, "loop thru remaining ways 2 search=%lld\n", search_nd);

						if (unsorted_outer_ways->data)
						{
							way_nodes2 = (struct way_and_endnodes *) unsorted_outer_ways->data;
							if (way_nodes2->first_node_id == search_nd)
							{

								if (way_nodes2->last_node_id == first_nd)
								{
									if (g_list_length(unsorted_outer_ways) > 1)
									{
										// we have closed the poly, but there are still "ways" left
										// sounds broken
										problem = 1;
										break;
									}
								}

								// what node we want to find
								search_nd = way_nodes2->last_node_id;

								// now move the way from unsorted to sorted list
								way_nodes1 = (struct way_and_endnodes *) malloc(sizeof(struct way_and_endnodes));
								way_nodes1->way_id = way_nodes2->way_id;

								// ok next way found
								//fprintf(stderr, "(F)way id=%llu\n", way_nodes1->way_id);
								//fprintf(stderr, "now search id=%llu\n", search_nd);

								way_nodes1->last_node_id = way_nodes2->last_node_id;
								way_nodes1->first_node_id = way_nodes2->first_node_id;
								// only copy over the pointer to the node list!!
								way_nodes1->nodes = way_nodes2->nodes;
								// first node is always in the right order
								way_nodes1->reverse = 0;
								sorted_outer_ways = g_list_append(sorted_outer_ways, way_nodes1);
								if (unsorted_outer_ways)
								{
									unsorted_outer_ways = g_list_first(unsorted_outer_ways);
								}
								unsorted_outer_ways = g_list_remove(unsorted_outer_ways, way_nodes2);
								g_free(way_nodes2);
								way_nodes2 = NULL;

								found_new_way = 1;
								//fprintf(stderr, "length unsorted_outer_ways=%d\n", g_list_length(unsorted_outer_ways));
								break;
							}
							else if (way_nodes2->last_node_id == search_nd)
							{
								if (way_nodes2->first_node_id == first_nd)
								{
									if (g_list_length(unsorted_outer_ways) > 1)
									{
										// we have closed the poly, but there are still "ways" left
										// sounds broken
										problem = 1;
										break;
									}
								}

								// what node we want to find
								search_nd = way_nodes2->first_node_id;

								// now move the way from unsorted to sorted list
								way_nodes1 = (struct way_and_endnodes *) malloc(sizeof(struct way_and_endnodes));
								way_nodes1->way_id = way_nodes2->way_id;

								// ok next way found
								//fprintf(stderr, "(R)way id=%llu\n", way_nodes1->way_id);
								//fprintf(stderr, "now search id=%llu\n", search_nd);

								way_nodes1->first_node_id = way_nodes2->last_node_id;
								way_nodes1->last_node_id = way_nodes2->first_node_id;
								// only copy over the pointer to the node list!!
								way_nodes1->nodes = way_nodes2->nodes;
								way_nodes1->nodes = g_list_reverse(way_nodes1->nodes);
								// first node is always in the right order
								way_nodes1->reverse = 1;
								sorted_outer_ways = g_list_append(sorted_outer_ways, way_nodes1);
								if (unsorted_outer_ways)
								{
									unsorted_outer_ways = g_list_first(unsorted_outer_ways);
								}
								unsorted_outer_ways = g_list_remove(unsorted_outer_ways, way_nodes2);
								g_free(way_nodes2);
								way_nodes2 = NULL;

								found_new_way = 1;
								//fprintf(stderr, "length unsorted_outer_ways=%d\n", g_list_length(unsorted_outer_ways));
								break;
							}
						}
						unsorted_outer_ways = g_list_next(unsorted_outer_ways);
					}
					//fprintf(stderr, "after while loop 2\n");

					if (found_new_way == 0)
					{
						// some problem with this multipolygon
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "some problem with this multipolygon\n");
						triangulate_not_done = 1;
						problem = 1;
						break;
					}
				}
				//fprintf(stderr, "after while loop 1\n");

			}

			// now at last check if very first node matches the last found node
			// and less than xxx 1000 points in polygon
			// if ((search_nd == first_nd) && (number_of_points_in_poly > 2) && (number_of_points_in_poly < 1000))
			if ((problem == 0) && ((search_nd == first_nd) && (number_of_points_in_poly > 2) && (number_of_points_in_poly < 30000)))
			{

				//fprintf(stderr, "in relation id=%lld\n", current_id);

				point_map = g_hash_table_new(g_direct_hash, g_direct_equal);
				current_points = g_ptr_array_new();

				number_of_points_in_poly = 0;

				sorted_outer_ways = g_list_first(sorted_outer_ways);
				while (sorted_outer_ways)
				{
					if (sorted_outer_ways->data)
					{
						way_nodes1 = (struct way_and_endnodes *) sorted_outer_ways->data;
						// now read all the nodes of this way
						//**alread in GList** //sqlite3_bind_int64(stmt_sel001, 1, way_nodes1->way_id);
						way_nodes1->nodes = g_list_first(way_nodes1->nodes);
						first_node_found = 0;
						while (way_nodes1->nodes)
						{
							ndptr = (long long *) way_nodes1->nodes->data;
							nd = *ndptr;

							get_lat_lon_for_node(nd, &node_coords);
							if (node_coords.valid == 1)
							{
								// lat
								lat_nd = node_coords.lat;
								// lon
								lon_nd = node_coords.lon;

								if (first_node_found == 0)
								{
									first_node_found = 1;
								}
								else
								{
									y = lat_nd * factor;
									x = lon_nd * factor;

									// ------ ok use this node and coords for triangulation ----
									//fprintf(stderr, "nd,lat,lon %lld,%f,%f\n", nd,lat_nd,lon_nd);
									//fprintf(stderr, "x,y %f,%f\n", x, y);

									g_ptr_array_add(current_points, p2t_point_new_dd(x, y));
								}
							}
							way_nodes1->nodes = g_list_next(way_nodes1->nodes);
						}
					}
					//sqlite3_reset(st);

					sorted_outer_ways = g_list_next(sorted_outer_ways);
				}

				//fprintf(stderr, "after sorting ways\n");


				exception = 0;

				TRY
				{
					// catch SIGSEGV !!
					signal(SIGSEGV, catch_signal);

					// alternate signal stack ------------
					//ss.ss_size = SIGSTKSZ;
					//ss.ss_sp = stack;
					//sa.sa_handler = catch_signal;
					//sa.sa_flags = SA_ONSTACK;
					//sigfillset(&sa.sa_mask);

					//sigaltstack(&ss, 0);
					//sigfillset(&sa.sa_mask);
					//sigaction(SIGSEGV, &sa, 0);
					// alternate signal stack ------------

					// catch SIGSEGV !!

					//fprintf(stderr, "In Try Statement (p2t_cdt_new)\n");
					cdt = p2t_cdt_new(current_points);
					//fprintf(stderr, "all OK (p2t_cdt_new)\n");
				}
				CATCH( MAPTOOL_00001_EXCEPTION )
				{
						fprintf_(stderr, "relation id=%lld\n", current_id);
					fprintf_(stderr, "Got Exception 1!\n");
					exception = 1;
				}
				CATCH( MAPTOOL_00002_EXCEPTION )
				{
						fprintf_(stderr, "relation id=%lld\n", current_id);
					fprintf_(stderr, "Got segv Exception 1!\n");
					exception = 1;
				}
				FINALLY
				{
					// set default
					signal (SIGSEGV, SIG_DFL);
					// set default

					//fprintf(stderr, "finally\n");
				}
				ETRY;

				//fprintf(stderr, "after cdt_new\n");

				GList* save = unsorted_inner_ways;

				int no_holes = 0;
				if (exception == 0)
				{

					// first run a check loop
					int number_of_nodes_in_this_way;
					int cur_node_count_in_way;
					unsorted_inner_ways = g_list_first(unsorted_inner_ways);

					int number_of_nodes_in_this_way3 = g_list_length(unsorted_inner_ways);
					//fprintf(stderr, "uiw 001.0 c=%d\n", number_of_nodes_in_this_way3);
					while (unsorted_inner_ways)
					{
						if ((unsorted_inner_ways->data)&&(no_holes == 0))
						{
							way_nodes1 = (struct way_and_endnodes *) unsorted_inner_ways->data;
							number_of_nodes_in_this_way = g_list_length(way_nodes1->nodes);
							// now read all the nodes of this "hole"-way
							GList* save0 = way_nodes1->nodes;
							way_nodes1->nodes = g_list_first(way_nodes1->nodes);
							first_node_found = 0;
							cur_node_count_in_way = 1;
							while (way_nodes1->nodes)
							{
								ndptr = (long long *) way_nodes1->nodes->data;
								// fprintf(stderr, "hole node:%lld\n", (long long)*ndptr);

								if (temp_nodes_list != NULL)
								{
									//fprintf(stderr, "cur_node_count_in_way %d number_of_nodes_in_this_way %d\n", cur_node_count_in_way, number_of_nodes_in_this_way);
									// the last node should always be same as first node (for now we only support holes that dont span over multiple ways!)
									if (cur_node_count_in_way < number_of_nodes_in_this_way)
									{
										if (glist_find_already_inside(temp_nodes_list, ndptr) != 0)
										{
											fprintf_(stderr, "relation id=%lld\n", current_id);
											fprintf_(stderr, "problem with hole!\n");
											no_holes = 1;
											break;
										}
										else
										{
											//fprintf(stderr, "hole check ok\n");
										}
									}
								}

								temp_nodes_list = g_list_append(temp_nodes_list, ndptr);

								way_nodes1->nodes = g_list_next(way_nodes1->nodes);
								cur_node_count_in_way++;
							}
							way_nodes1->nodes = save0;
						}
						unsorted_inner_ways = g_list_next(unsorted_inner_ways);
					}

				}

				unsorted_inner_ways = save;

				//fprintf(stderr, "ex=%d nohole=%d\n", exception, no_holes);

				

				if ((exception == 0) && (no_holes == 0))
				{
					//fprintf(stderr, "uiw 000\n");

					// now do the real loop
					int number_of_added_holes = 0;

					unsorted_inner_ways = g_list_first(unsorted_inner_ways);
					//int number_of_nodes_in_this_way4 = g_list_length(unsorted_inner_ways);
					//fprintf(stderr, "uiw 001.2 c=%d\n", number_of_nodes_in_this_way4);
					while (unsorted_inner_ways)
					{
						//fprintf(stderr, "uiw 001\n");

						if (unsorted_inner_ways->data)
						{
							//fprintf(stderr, "uiw 002\n");

							current_hole = g_ptr_array_new();

							//fprintf(stderr, "uiw 003\n");

							way_nodes1 = (struct way_and_endnodes *) unsorted_inner_ways->data;
							// now read all the nodes of this "hole"-way
							way_nodes1->nodes = g_list_first(way_nodes1->nodes);
							first_node_found = 0;
							while (way_nodes1->nodes)
							{
								//fprintf(stderr, "uiw 004\n");

								ndptr = (long long *) way_nodes1->nodes->data;
								nd = *ndptr;

								// now read the coords of this node

#ifdef MAPTOOL_SPLIT_NODE_DB
								if (nd % 2)
								{
									if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
									{
										sqlite3_bind_int64(stmt_sel002__2a, 1, nd);
									}
									else
									{
										sqlite3_bind_int64(stmt_sel002__2b, 1, nd);
									}
								}
								else
								{
									if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
									{
#endif
										sqlite3_bind_int64(stmt_sel002a, 1, nd);
#ifdef MAPTOOL_SPLIT_NODE_DB
									}
									else
									{
										sqlite3_bind_int64(stmt_sel002b, 1, nd);
									}
								}
#endif
								rc2 = 0;
								do
								{
#ifdef MAPTOOL_SPLIT_NODE_DB
									if (nd % 2)
									{
										if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
										{
											rc2 = sqlite3_step(stmt_sel002__2a);
										}
										else
										{
											rc2 = sqlite3_step(stmt_sel002__2b);
										}
									}
									else
									{
										if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
										{
#endif
											rc2 = sqlite3_step(stmt_sel002a);
#ifdef MAPTOOL_SPLIT_NODE_DB
										}
										else
										{
											rc2 = sqlite3_step(stmt_sel002b);
										}
									}
#endif

									switch (rc2)
									{
										case SQLITE_DONE:
										break;
										case SQLITE_ROW:

#ifdef MAPTOOL_SPLIT_NODE_DB
										if (nd % 2)
										{
											if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
											{
												lat_nd = (double) sqlite3_column_double(stmt_sel002__2a, 0);
												lon_nd = (double) sqlite3_column_double(stmt_sel002__2a, 1);
											}
											else
											{
												lat_nd = (double) sqlite3_column_double(stmt_sel002__2b, 0);
												lon_nd = (double) sqlite3_column_double(stmt_sel002__2b, 1);
											}
										}
										else
										{
											if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
											{
#endif
												lat_nd = (double) sqlite3_column_double(stmt_sel002a, 0);
												lon_nd = (double) sqlite3_column_double(stmt_sel002a, 1);
#ifdef MAPTOOL_SPLIT_NODE_DB
											}
											else
											{
												lat_nd = (double) sqlite3_column_double(stmt_sel002b, 0);
												lon_nd = (double) sqlite3_column_double(stmt_sel002b, 1);
											}
										}
#endif

										if (first_node_found == 0)
										{
											first_node_found = 1;
										}
										else
										{
											// ------ ok use this node and coords for triangulation ----
											//fprintf(stderr, "HOLE:nd,lat,lon %lld,%f,%f\n", nd,lat_nd,lon_nd);

											//fprintf(stderr, "np=%d\n", number_of_points_in_poly);
											//in.pointlist[number_of_points_in_poly] = (REAL) (lat_nd*10);
											//number_of_points_in_poly++;
											//fprintf(stderr, "np=%d\n", number_of_points_in_poly);
											//in.pointlist[number_of_points_in_poly] = (REAL) (lon_nd*10);
											//number_of_points_in_poly++;
#if 1
											y = lat_nd * factor;
											x = lon_nd * factor;

											TRY
											{
												signal(SIGSEGV, catch_signal);

												// hole complete, now add it
												//fprintf(stderr, "hole new\n");
												g_ptr_array_add(current_hole, p2t_point_new_dd(x, y));
												//fprintf(stderr, "hole new done\n");
											}
											CATCH( MAPTOOL_00001_EXCEPTION )
											{
												fprintf_(stderr, "relation id=%lld\n", current_id);
												fprintf_(stderr, "Got Exception A!\n");
												exception = 1;
											}
											CATCH( MAPTOOL_00002_EXCEPTION )
											{
												fprintf_(stderr, "relation id=%lld\n", current_id);
												fprintf_(stderr, "Got segv Exception A!\n");
												exception = 1;
											}
											FINALLY
											{
												// set default
												signal (SIGSEGV, SIG_DFL);
												// set default

												//fprintf(stderr, "finally\n");
											}
											ETRY;

#endif
										}
										break;
										default:
										fprintf(stderr, "Error: %d\n", rc2);
										break;
									}
								}
								while (rc2 == SQLITE_ROW);

#ifdef MAPTOOL_SPLIT_NODE_DB
								if (nd % 2)
								{
									if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
									{
										sqlite3_reset(stmt_sel002__2a);
									}
									else
									{
										sqlite3_reset(stmt_sel002__2b);
									}
								}
								else
								{
									if ((nd & MAPTOOL_SPLIT_NODE_BIT) > 0)
									{
#endif
										sqlite3_reset(stmt_sel002a);
#ifdef MAPTOOL_SPLIT_NODE_DB
									}
									else
									{
										sqlite3_reset(stmt_sel002b);
									}
								}
#endif

								way_nodes1->nodes = g_list_next(way_nodes1->nodes);
							}

							//fprintf(stderr, "uiw 005\n");

							// remember the pointer, to free the memory later
							all_holes = g_list_append(all_holes, current_hole);

							//fprintf(stderr, "uiw 005.1\n");

							TRY
							{
								signal(SIGSEGV, catch_signal);

								// hole complete, now add it
								//fprintf(stderr, "adding hole\n");
								if (number_of_added_holes < MAX_HOLES_PER_POLY)
								{
									p2t_cdt_add_hole(cdt, current_hole);
									number_of_added_holes++;
								}
								//fprintf(stderr, "adding done\n");
							}
							CATCH( MAPTOOL_00001_EXCEPTION )
							{
								fprintf_(stderr, "relation id=%lld\n", current_id);
								fprintf_(stderr, "Got Exception 8!\n");
								exception = 1;
							}
							CATCH( MAPTOOL_00002_EXCEPTION )
							{
								fprintf_(stderr, "relation id=%lld\n", current_id);
								fprintf_(stderr, "Got segv Exception 8!\n");
								exception = 1;
							}
							FINALLY
							{
								// set default
								signal (SIGSEGV, SIG_DFL);
								// set default

								//fprintf(stderr, "finally\n");
							}
							ETRY;

						}

						//fprintf(stderr, "uiw 006\n");
						unsorted_inner_ways = g_list_next(unsorted_inner_ways);

					}
					//sqlite3_reset(st);

					//fprintf(stderr, "uiw 007\n");
				}

				//fprintf(stderr, "after adding holes\n");

#if 1

				if (exception == 0)
				{
					exception = 0;

					TRY
					{
						// catch SIGSEGV !!
						signal(SIGSEGV, catch_signal);
						//signal(SIGILL, catch_signal);

						// alternate signal stack ------------
						//ss.ss_size = SIGSTKSZ;
						//ss.ss_sp = stack;
						//sa.sa_handler = catch_signal;
						//sa.sa_flags = SA_ONSTACK;
						//sigfillset(&sa.sa_mask);

						//sigaltstack(&ss, 0);
						//sigfillset(&sa.sa_mask);
						//sigaction(SIGSEGV, &sa, 0);
						// alternate signal stack ------------

						// catch SIGSEGV !!

						//fprintf(stderr, "triangulate start ...\n");
						p2t_cdt_triangulate(cdt);
						//fprintf(stderr, "triangulate end\n");
					}
					CATCH( MAPTOOL_00001_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got Exception 2! (in triangulate)\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got segv Exception 2! (in triangulate)\n");
						exception = 1;
					}
					FINALLY
					{
						//fprintf(stderr, "finally 2 (in triangulate)\n");

						//sa.sa_handler = SIG_DFL;
						// sigfillset(&sa.sa_mask);
						//sigaction(SIGSEGV, &sa, 0);

						// set default
						signal (SIGSEGV, SIG_DFL);
						// set default
					}
					ETRY;

					if (exception == 0)
					{
						rmesh = g_slice_new (P2trCDT);
						rmesh->mesh = p2tr_mesh_new ();
						// rmesh->outline = p2tr_pslg_new ();

						/* First iteration over the CDT - create all the points */
						//fprintf(stderr, "trinew 006\n");
						triangles = p2t_cdt_get_triangles(cdt);
						//fprintf(stderr, "trinew 007\n");
						for (i = 0; i < triangles->len; i++)
						{
							P2tTriangle *cdt_tri = triangle_index (triangles, i);
							for (j = 0; j < 3; j++)
							{
								P2tPoint *cdt_pt = p2t_triangle_get_point(cdt_tri, j);
								P2trPoint *new_pt = (P2trPoint*) g_hash_table_lookup (point_map, cdt_pt);

								if (new_pt == NULL)
								{
									//fprintf(stderr, "p1 x=%d, y=%d\n",(int)cdt_pt->x, (int)cdt_pt->y);
									new_pt = p2tr_mesh_new_point2 (rmesh->mesh, (cdt_pt->x / factor), (cdt_pt->y / factor));
									g_hash_table_insert (point_map, cdt_pt, new_pt);
								}
							}
						}

						for (i = 0; i < triangles->len; i++)
						{
							P2tTriangle *cdt_tri = triangle_index(triangles, i);
							//fprintf(stderr, "tri# %d\n", i);
							gboolean ii = p2t_triangle_is_interior(cdt_tri);
							//fprintf(stderr, "is int=%d\n", ii);

							P2trPoint *pt1 = (P2trPoint*) g_hash_table_lookup (point_map, p2t_triangle_get_point (cdt_tri, 0));
							P2trPoint *pt2 = (P2trPoint*) g_hash_table_lookup (point_map, p2t_triangle_get_point (cdt_tri, 1));
							P2trPoint *pt3 = (P2trPoint*) g_hash_table_lookup (point_map, p2t_triangle_get_point (cdt_tri, 2));

							//fprintf(stderr, "0 x=%f, y=%f\n",pt1->c.x, pt1->c.y);
							//fprintf(stderr, "1 x=%f, y=%f\n",pt2->c.x, pt2->c.y);
							//fprintf(stderr, "2 x=%f, y=%f\n",pt3->c.x, pt3->c.y);

							// make item and write it to ways file -------------------
							// make item and write it to ways file -------------------
							if (tag_id == 1)
							{
								item_bin_tri = init_item(type_poly_water_from_triang, 0);
								//fprintf(stderr, "T:water\n");
							}
							else if (tag_id == 2)
							{
								item_bin_tri = init_item(type_poly_water_from_triang, 0);
								//fprintf(stderr, "T:water\n");
							}
							else if (tag_id == 4)
							{
								item_bin_tri = init_item(type_poly_water_from_triang, 0);
								//fprintf(stderr, "T:water\n");
							}
							else if (tag_id == 5)
							{
								item_bin_tri = init_item(type_poly_wood_from_triang, 0);
								//fprintf(stderr, "T:forest\n");
							}
							else if (tag_id == 6)
							{
								item_bin_tri = init_item(type_poly_wood_from_triang, 0);
								//fprintf(stderr, "T:forest\n");
							}
							else if (tag_id == 9)
							{
								item_bin_tri = init_item(type_poly_building_from_triang, 0);
								//fprintf(stderr, "T:building\n");
							}
							else
							{
								// default
								item_bin_tri = init_item(type_poly_water_from_triang, 0);
								//fprintf(stderr, "T:water\n");
							}

							// first add coords
							coord_tri.x = transform_from_geo_lon(pt1->c.x);
							coord_tri.y = transform_from_geo_lat(pt1->c.y);
							item_bin_add_coord(item_bin_tri, &coord_tri, 1);
							coord_tri.x = transform_from_geo_lon(pt2->c.x);
							coord_tri.y = transform_from_geo_lat(pt2->c.y);
							item_bin_add_coord(item_bin_tri, &coord_tri, 1);
							coord_tri.x = transform_from_geo_lon(pt3->c.x);
							coord_tri.y = transform_from_geo_lat(pt3->c.y);
							item_bin_add_coord(item_bin_tri, &coord_tri, 1);
							// now add dummy osm wayid
							item_bin_add_attr_longlong(item_bin_tri, attr_osm_wayid, current_id);

							// DEBUG -- DEBUG -- DEBUG --
							// DEBUG -- DEBUG -- DEBUG --
							// dump_itembin(item_bin_tri);
							// DEBUG -- DEBUG -- DEBUG --
							// DEBUG -- DEBUG -- DEBUG --
							item_bin_write(item_bin_tri, osm->ways_with_coords);

							// make item and write it to ways file -------------------
							// make item and write it to ways file -------------------

						}

						//fprintf(stderr, "trinew 091\n");

					}

					//fprintf(stderr, "trinew 092.0\n");


					TRY
					{
						signal(SIGSEGV, catch_signal);

						/* Now finally unref the points we added into the map */
						if (point_map != NULL)
						{
							GHashTableIter iter;
							P2trPoint *pt_iter = NULL;
							g_hash_table_iter_init (&iter, point_map);
							while (g_hash_table_iter_next (&iter, NULL, (gpointer*)&pt_iter))
							{
								p2tr_point_unref(pt_iter);
							}
							g_hash_table_destroy(point_map);
						}
					}
					CATCH( MAPTOOL_00001_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got Exception 3!\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got segv Exception 3!\n");
						exception = 1;
					}
					FINALLY
					{
						signal (SIGSEGV, SIG_DFL);
					}
					ETRY;

					//fprintf(stderr, "trinew 092.1\n");


					TRY
					{
						signal(SIGSEGV, catch_signal);
						// p2tr_mesh_clear(rmesh->mesh);
						// --- maybe need this?? --- p2tr_mesh_unref(rmesh->mesh);
					}
					CATCH( MAPTOOL_00001_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got Exception 4!\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got segv Exception 4!\n");
						exception = 1;
					}
					FINALLY
					{
						signal (SIGSEGV, SIG_DFL);
					}
					ETRY;

					//fprintf(stderr, "trinew 092.2\n");

					TRY
					{
						signal(SIGSEGV, catch_signal);
						p2t_cdt_free(cdt);
					}
					CATCH( MAPTOOL_00001_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got Exception 5!\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf_(stderr, "relation id=%lld\n", current_id);
						fprintf_(stderr, "Got segv Exception 5!\n");
						exception = 1;
					}
					FINALLY
					{
						signal (SIGSEGV, SIG_DFL);
					}
					ETRY;

					//fprintf(stderr, "trinew 092.3\n");

				} // exception == 0 ? -- END

				// rcdt = p2tr_cdt_new(cdt);
				/*
				 if (refine_max_steps > 0)
				 {
				 fprintf(stderr, "Refining the mesh!\n");
				 refiner = p2tr_refiner_new(G_PI / 6, p2tr_refiner_false_too_big, rcdt);
				 p2tr_refiner_refine(refiner, refine_max_steps, NULL);
				 p2tr_refiner_free(refiner);

				 p2tr_cdt_validate_edges(rcdt);
				 }
				 */

				//fprintf(stderr, "free holes\n");

				if (all_holes)
				{
					all_holes = g_list_first(all_holes);
					while (all_holes)
					{
						current_hole = all_holes->data;
						for (i = 0; i < current_hole->len; ++i)
						{
							p2t_point_free(g_ptr_array_index(current_hole, i));
						}
						g_ptr_array_free(current_hole, TRUE);

						all_holes = g_list_next(all_holes);
					}
					g_list_free(all_holes);
					all_holes = NULL;
				}

				//fprintf(stderr, "free points\n");

				for (i = 0; i < current_points->len; ++i)
				{
					p2t_point_free(g_ptr_array_index(current_points, i));
				}
				g_ptr_array_free(current_points, TRUE);

				//			p2tr_cdt_free(rcdt);

#endif

#if 0
				item_bin_write(item_bin_tri, osm->ways_with_coords);
#endif
				triangulate_not_done = 0;
			}

			// free everything!! ----------------
			if (unsorted_outer_ways)
			{
				unsorted_outer_ways = g_list_first(unsorted_outer_ways);
				while (unsorted_outer_ways)
				{
					gptr = unsorted_outer_ways->data;
					way_nodes1 = unsorted_outer_ways->data;
					if (way_nodes1->nodes)
					{
						way_nodes1->nodes = g_list_first(way_nodes1->nodes);
						while (way_nodes1->nodes)
						{
							ndptr = way_nodes1->nodes->data;
							way_nodes1->nodes = g_list_remove(way_nodes1->nodes, ndptr);
							g_free(ndptr);
							ndptr = NULL;
							way_nodes1->nodes = g_list_next(way_nodes1->nodes);
						}
					}
					unsorted_outer_ways = g_list_remove(unsorted_outer_ways, gptr);
					g_free(gptr);
					gptr = NULL;
					unsorted_outer_ways = g_list_next(unsorted_outer_ways);
				}
			}

			if (unsorted_inner_ways)
			{
				unsorted_inner_ways = g_list_first(unsorted_inner_ways);
				while (unsorted_inner_ways)
				{
					gptr = unsorted_inner_ways->data;
					way_nodes1 = unsorted_inner_ways->data;
					if (way_nodes1->nodes)
					{
						way_nodes1->nodes = g_list_first(way_nodes1->nodes);
						while (way_nodes1->nodes)
						{
							ndptr = way_nodes1->nodes->data;
							way_nodes1->nodes = g_list_remove(way_nodes1->nodes, ndptr);
							g_free(ndptr);
							ndptr = NULL;
							way_nodes1->nodes = g_list_next(way_nodes1->nodes);
						}
					}
					unsorted_inner_ways = g_list_remove(unsorted_inner_ways, gptr);
					g_free(gptr);
					gptr = NULL;
					unsorted_inner_ways = g_list_next(unsorted_inner_ways);
				}
			}

			if (sorted_outer_ways)
			{
				sorted_outer_ways = g_list_first(sorted_outer_ways);
				while (sorted_outer_ways)
				{
					gptr = sorted_outer_ways->data;
					way_nodes1 = sorted_outer_ways->data;
					if (way_nodes1->nodes)
					{
						way_nodes1->nodes = g_list_first(way_nodes1->nodes);
						while (way_nodes1->nodes)
						{
							ndptr = way_nodes1->nodes->data;
							way_nodes1->nodes = g_list_remove(way_nodes1->nodes, ndptr);
							g_free(ndptr);
							ndptr = NULL;
							way_nodes1->nodes = g_list_next(way_nodes1->nodes);
						}
					}
					sorted_outer_ways = g_list_remove(sorted_outer_ways, gptr);
					g_free(gptr);
					gptr = NULL;
					sorted_outer_ways = g_list_next(sorted_outer_ways);
				}
			}
			// free everything!! ----------------
		}
		// save relations other method --------------

#endif

		if (triangulate_not_done == 1)
		{
			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if ((memb.type == 2) && (!strcmp(memb.role, "outer"))) // use only "outer" "ways" here!!
					{
						struct way_tag wt;
						wt.way_id = memb.id;
						wt.tag_id = tag_id;
						// only use "water" related ways here
						if ((tag_id == 1) || (tag_id == 2) || (tag_id == 4) || (tag_id == 5) || (tag_id == 6))
						{
							//fprintf(stderr,"rel way=%llu\n", memb.id);
							fwrite(&wt, sizeof(struct way_tag), 1, osm->relations_riverbank);
						}
					}
					else if ((memb.type == 2) && (!strcmp(memb.role, "inner"))) // use only "outer" "ways" here!!
					{
						struct way_tag wt;
						wt.way_id = memb.id;
						wt.tag_id = tag_id;
						// only use "water" related ways here
						if (tag_id == 6)
						{
							wt.tag_id = 8;
							//fprintf(stderr,"rel way(8)=%llu\n", memb.id);
							fwrite(&wt, sizeof(struct way_tag), 1, osm->relations_riverbank);
						}
						else if (tag_id == 5)
						{
							wt.tag_id = 7;
							//fprintf(stderr,"rel way(7)=%llu\n", memb.id);
							fwrite(&wt, sizeof(struct way_tag), 1, osm->relations_riverbank);
						}
					}

				}
			}
		}
	}
	// -- save relations for riverbanks --


	in_relation = 0;

	if ((!strcmp(relation_type, "multipolygon") || !strcmp(relation_type, "boundary")) && boundary)
	{
		int found_town_rel = 0;

		fprintf_(stderr, "town to relation 00: r:%lld\n", current_id);

		if ((found_town_rel == 0) && (admin_level >= TOWN_ADMIN_LEVEL_START))
		{
			// save relation id for towns into DB
			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if (memb.type == 1) 
					{
						fprintf_(stderr, "role=%s\n", memb.role);

						if (!strcmp(memb.role, "label"))
						{
							fprintf_(stderr, "town to relation 1: rel-name:%s r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), current_id, memb.id);
							sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
							// fprintf(stderr, "ret=%d\n", rr);
							sqlite3_bind_int64(stmt_town_sel008, 1, current_id);
							sqlite3_bind_int64(stmt_town_sel008, 2, memb.id);
							sqlite3_bind_int(stmt_town_sel008, 3, admin_level);
							sqlite3_step(stmt_town_sel008);
							//fprintf(stderr, "ret=%d\n", rr);
							sqlite3_reset(stmt_town_sel008);
							//fprintf(stderr, "ret=%d\n", rr);

							add_boundary_to_db(current_id, admin_level, item_bin_2, &memb);

							lat_lon_c = get_first_coord_of_boundary(item_bin_2, &memb);
							if (lat_lon_c)
							{
								sqlite3_bind_double(stmt_town, 6, lat_lon_c->lat);
								sqlite3_bind_double(stmt_town, 7, lat_lon_c->lon);
								g_free(lat_lon_c);
							}

							sqlite3_bind_int64(stmt_town, 1, dummy_town_id);
							sqlite3_bind_int(stmt_town, 2, 999);
							sqlite3_bind_int(stmt_town, 4, (TOWN_ADMIN_LEVEL_CORR_BASE + admin_level) * TOWN_BY_BOUNDARY_SIZE_FACTOR);
							sqlite3_bind_text(stmt_town, 3, osm_tag_value(item_bin_2, "name"), -1, SQLITE_STATIC);
							sqlite3_bind_text(stmt_town, 5, NULL, -1, SQLITE_STATIC);
							sqlite3_bind_int64(stmt_town, 8, current_id);
							sqlite3_step(stmt_town);
							sqlite3_reset(stmt_town);
							dummy_town_id--;

							sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
							//fprintf(stderr, "ret=%d\n", rr);

							found_town_rel = 1;
						}
					}
				}
			}
		}



		if ((found_town_rel == 0) && (admin_level >= TOWN_ADMIN_LEVEL_START))
		{
			// save relation id for towns into DB
			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if (memb.type == 1) 
					{
						fprintf_(stderr, "role=%s\n", memb.role);

						if (!strcmp(memb.role, "admin_centre"))
						{
							fprintf_(stderr, "town to relation 2: rel-name:%s r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), current_id, memb.id);
							sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
							// fprintf(stderr, "ret=%d\n", rr);
							sqlite3_bind_int64(stmt_town_sel008, 1, current_id);
							sqlite3_bind_int64(stmt_town_sel008, 2, memb.id);
							sqlite3_bind_int(stmt_town_sel008, 3, admin_level);
							sqlite3_step(stmt_town_sel008);
							//fprintf(stderr, "ret=%d\n", rr);
							sqlite3_reset(stmt_town_sel008);
							//fprintf(stderr, "ret=%d\n", rr);

							add_boundary_to_db(current_id, admin_level, item_bin_2, &memb);

							lat_lon_c = get_first_coord_of_boundary(item_bin_2, &memb);
							if (lat_lon_c)
							{
								sqlite3_bind_double(stmt_town, 6, lat_lon_c->lat);
								sqlite3_bind_double(stmt_town, 7, lat_lon_c->lon);
								g_free(lat_lon_c);
							}

							sqlite3_bind_int64(stmt_town, 1, dummy_town_id);
							sqlite3_bind_int(stmt_town, 2, 999);
							sqlite3_bind_int(stmt_town, 4, (TOWN_ADMIN_LEVEL_CORR_BASE + admin_level) * TOWN_BY_BOUNDARY_SIZE_FACTOR);
							sqlite3_bind_text(stmt_town, 3, osm_tag_value(item_bin_2, "name"), -1, SQLITE_STATIC);
							sqlite3_bind_text(stmt_town, 5, NULL, -1, SQLITE_STATIC);
							sqlite3_bind_int64(stmt_town, 8, current_id);
							sqlite3_step(stmt_town);
							sqlite3_reset(stmt_town);
							dummy_town_id--;

							sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
							//fprintf(stderr, "ret=%d\n", rr);

							found_town_rel = 1;
						}
					}
				}
			}
		}

		if ((found_town_rel == 0) && (admin_level >= TOWN_ADMIN_LEVEL_START))
		{
			// save relation id for towns into DB
			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if (memb.type == 1) 
					{
						//fprintf(stderr, "role=%s\n", memb.role);

						if (!strcmp(memb.role, "admin_center"))
						{
							fprintf_(stderr, "town to relation 3: rel-name:%s r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), current_id, memb.id);
							sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
							// fprintf(stderr, "ret=%d\n", rr);
							sqlite3_bind_int64(stmt_town_sel008, 1, current_id);
							sqlite3_bind_int64(stmt_town_sel008, 2, memb.id);
							sqlite3_bind_int(stmt_town_sel008, 3, admin_level);
							sqlite3_step(stmt_town_sel008);
							//fprintf(stderr, "ret=%d\n", rr);
							sqlite3_reset(stmt_town_sel008);
							//fprintf(stderr, "ret=%d\n", rr);

							add_boundary_to_db(current_id, admin_level, item_bin_2, &memb);

							lat_lon_c = get_first_coord_of_boundary(item_bin_2, &memb);
							if (lat_lon_c)
							{
								sqlite3_bind_double(stmt_town, 6, lat_lon_c->lat);
								sqlite3_bind_double(stmt_town, 7, lat_lon_c->lon);
								g_free(lat_lon_c);
							}

							sqlite3_bind_int64(stmt_town, 1, dummy_town_id);
							sqlite3_bind_int(stmt_town, 2, 999);
							sqlite3_bind_int(stmt_town, 4, (TOWN_ADMIN_LEVEL_CORR_BASE + admin_level) * TOWN_BY_BOUNDARY_SIZE_FACTOR);
							sqlite3_bind_text(stmt_town, 3, osm_tag_value(item_bin_2, "name"), -1, SQLITE_STATIC);
							sqlite3_bind_text(stmt_town, 5, NULL, -1, SQLITE_STATIC);
							sqlite3_bind_int64(stmt_town, 8, current_id);
							sqlite3_step(stmt_town);
							sqlite3_reset(stmt_town);
							dummy_town_id--;

							sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
							//fprintf(stderr, "ret=%d\n", rr);

							found_town_rel = 1;
						}
					}
				}
			}
		}



		if ((found_town_rel == 0) && (admin_level >= TOWN_ADMIN_LEVEL_START))
		{
			// save relation id for towns into DB (use dummy town data)
			fprintf_(stderr, "town to relation(*): rel-name:%s admlevel:%d r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), admin_level, current_id, dummy_town_id);
			sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
			// fprintf(stderr, "ret=%d\n", rr);
			sqlite3_bind_int64(stmt_town_sel008, 1, current_id);
			sqlite3_bind_int64(stmt_town_sel008, 2, dummy_town_id);
			sqlite3_bind_int(stmt_town_sel008, 3, admin_level);
			sqlite3_step(stmt_town_sel008);
			//fprintf(stderr, "ret=%d\n", rr);
			sqlite3_reset(stmt_town_sel008);
			//fprintf(stderr, "ret=%d\n", rr);

			int did_write_boundary_to_db = 0;
			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if ((memb.type == 2) && (strcmp(memb.role, "inner")))
					{
						// way-id is "memb.id"
						// use the first nodes coords
						//get_lat_lon_way_first_node(memb.id, &node_coords);
						//if (node_coords.valid == 1)
						//{
						//	sqlite3_bind_double(stmt_town, 6, node_coords.lat);
						//	sqlite3_bind_double(stmt_town, 7, node_coords.lon);
						//}

						add_boundary_to_db(current_id, admin_level, item_bin_2, &memb);
						did_write_boundary_to_db = 1;

						break;
					}
				}
			}

			lat_lon_c = get_first_coord_of_boundary(item_bin_2, &memb);
			if (lat_lon_c)
			{
				sqlite3_bind_double(stmt_town, 6, lat_lon_c->lat);
				sqlite3_bind_double(stmt_town, 7, lat_lon_c->lon);
				fprintf_(stderr, "town to relation(*):coords %f %f\n", lat_lon_c->lat, lat_lon_c->lon);
				g_free(lat_lon_c);
			}


			sqlite3_bind_int64(stmt_town, 1, dummy_town_id);
			sqlite3_bind_int(stmt_town, 2, 999);
			sqlite3_bind_int(stmt_town, 4, (TOWN_ADMIN_LEVEL_CORR_BASE + admin_level) * TOWN_BY_BOUNDARY_SIZE_FACTOR);
			sqlite3_bind_text(stmt_town, 3, osm_tag_value(item_bin_2, "name"), -1, SQLITE_STATIC);
			sqlite3_bind_text(stmt_town, 5, NULL, -1, SQLITE_STATIC);

			if (did_write_boundary_to_db == 1)
			{
				sqlite3_bind_int64(stmt_town, 8, current_id);
			}
			else
			{
				// boundary not in DB
				// ----- what? i have no idea what to do here -----
			}
			sqlite3_step(stmt_town);
			sqlite3_reset(stmt_town);

			found_town_rel = 1;

			sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
			//fprintf(stderr, "ret=%d\n", rr);

			dummy_town_id--;
		}

		// rest of the boundaries!!!
		if (found_town_rel == 0)
		{
			sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);

			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if ((memb.type == 2) && (!strcmp(memb.role, "outer")))
					{
						fprintf_(stderr, "town to relation(+): rel-name:%s admlevel:%d r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), admin_level, current_id, 9797979797);

						add_boundary_to_db(current_id, admin_level, item_bin_2, &memb);
						break;
					}
				}
			}
			sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

			found_town_rel = 0;
		}


#if 0
		if (admin_level == 2)
		{
			FILE *f;
			fprintf(stderr,"Multipolygon for %s\n", iso_code);
			char *name=g_strdup_printf("country_%s.tmp",iso_code);
			f=fopen(name,"w");
			item_bin_write(item_bin_2, f);
			fclose(f);
		}
#endif
		item_bin_write(item_bin_2, osm->boundaries);
	}

	if (!strcmp(relation_type, "restriction") && (item_bin_2->type == type_street_turn_restriction_no || item_bin_2->type == type_street_turn_restriction_only))
	{
		item_bin_write(item_bin_2, osm->turn_restrictions);
	}
}


// #define DEBUG_CORRECT_BOUNDARY_REF_POINT 1

void correct_boundary_ref_point(GList *bl)
{
	long long rid;
	int retval;
	int rc = 0;
	int commit_after = 20000;
	int count = 0;
	int admin_l = 99;
	double lat, lon;
	struct coord c;
	GList *l = NULL;
	GList *l2 = NULL;
	GList *match = NULL;
	struct boundary *b = NULL;
	int admin_l_boundary;
	int max_admin_l;

	//fprintf(stderr, "BRP:001\n");

	// loop thru all the boundaries
	do
	{

		//fprintf(stderr, "BRP:002\n");

		rc = sqlite3_step(stmt_bd_002);

		//fprintf(stderr, "BRP:003\n");

		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:

				//fprintf(stderr, "BRP:004\n");
				//  select rel_id, admin_level, lat, lon, name from boundary where done = 0;
				rid = sqlite3_column_int64(stmt_bd_002, 0);
				//fprintf(stderr, "BRP:005\n");
				admin_l = sqlite3_column_int(stmt_bd_002, 1);
				//fprintf(stderr, "BRP:006\n");
				lat = sqlite3_column_double(stmt_bd_002, 2);
				//fprintf(stderr, "BRP:007\n");
				lon = sqlite3_column_double(stmt_bd_002, 3);
				//fprintf(stderr, "BRP:008\n");
				c.x = transform_from_geo_lon(lon);
				//fprintf(stderr, "BRP:009\n");
				c.y = transform_from_geo_lat(lat);
				//fprintf(stderr, "BRP:010\n");

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
				fprintf(stderr, "correct_boundary_ref_point: relid:%lld(%d) x=%d y=%d\n", rid, admin_l, c.x, c.y);
#endif

				// find this boundary in list
				l = bl;
				match = NULL;

				//fprintf(stderr, "BRP:011\n");

				while (l)
				{
					// fprintf(stderr, "BRP:012 l=%p l->data=%p\n", l, l->data);

					b = l->data;

					// fprintf(stderr, "BRP:012a b=%p\n", b);

					if (b)
					{
						// fprintf(stderr, "BRP:012b\n");
						if (b->ib)
						{
							// fprintf(stderr, "BRP:012c\n");

							// fprintf(stderr, "BRP:013a %p\n", b);
							// fprintf(stderr, "BRP:013b %p\n", b->ib);
							// fprintf(stderr, "BRP:013c %lld\n", (long long)rid);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
							fprintf(stderr, "correct_boundary_ref_point: %lld %lld\n", item_bin_get_relationid(b->ib), rid);
#endif
							if (item_bin_get_relationid(b->ib) == rid)
							{
								// fprintf(stderr, "BRP:014\n");
								match = l;
								break;
							}
							// fprintf(stderr, "BRP:015\n");

						}
					}

					l = g_list_next(l);

					// fprintf(stderr, "BRP:016\n");

				}

				//fprintf(stderr, "BRP:017\n");

				if (match)
				{
					//fprintf(stderr, "BRP:018\n");

					b = match->data;

					if (b)
					{

						//fprintf(stderr, "BRP:019\n");

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
						fprintf(stderr, "correct_boundary_ref_point: relid:%lld(%d) parentid:%lld(%d)\n", rid, admin_l, item_bin_get_relationid(b->ib), max_admin_l);
#endif
						// -------------------------------------------
						/*
						GList *sl;
						sl = b->sorted_segments;
						if (sl)
						{
							struct geom_poly_segment *gs = sl->data;
							struct coord *c2 = gs->first;
							fprintf(stderr, "1a c=%d y=%d\n", c2->x, c2->y);
						}
						*/
						// -------------------------------------------

						if (find_correct_point_in_boundary(match, &lat, &lon) == 1)
						{

							//fprintf(stderr, "BRP:020\n");
							sqlite3_bind_double(stmt_bd_004, 1, lat);
							//fprintf(stderr, "BRP:021\n");
							sqlite3_bind_double(stmt_bd_004, 2, lon);
							//fprintf(stderr, "BRP:022\n");
							sqlite3_bind_int64(stmt_bd_004, 3, rid);
							//fprintf(stderr, "BRP:023\n");
							sqlite3_step(stmt_bd_004);
							//fprintf(stderr, "BRP:024\n");
							sqlite3_reset(stmt_bd_004);
							//fprintf(stderr, "BRP:025\n");
						}

						//fprintf(stderr, "BRP:026\n");

					}

				}

				//fprintf(stderr, "BRP:027\n");

				if (count == 0)
				{
					sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
				}
				count++;

				//fprintf(stderr, "BRP:028\n");

				if (count > commit_after)
				{
					sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
					count = 0;
				}

				//fprintf(stderr, "BRP:029\n");

				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);

	//fprintf(stderr, "BRP:091\n");

	sqlite3_reset(stmt_bd_002);

	//fprintf(stderr, "BRP:092\n");

	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	//fprintf(stderr, "BRP:093\n");

}


int find_correct_point_in_boundary(GList *bl, double* lat, double* lon)
{
	double lat2, lon2;
	struct coord c;
	struct boundary *b;
	GList *matches;
	double lat_d = 0.00006;
	double lon_d = 0.00006;

	lat2 = *lat;
	lon2 = *lon;

	c.x = transform_from_geo_lon(lon2 + lon_d);
	c.y = transform_from_geo_lat(lat2 + lat_d);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "2 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2 + lon_d;
		*lat = lat2 + lat_d;
		return 1;
	}

	c.x = transform_from_geo_lon(lon2 + lon_d);
	c.y = transform_from_geo_lat(lat2 - lat_d);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "3 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2 + lon_d;
		*lat = lat2 - lat_d;
		return 1;
	}

	c.x = transform_from_geo_lon(lon2 - lon_d);
	c.y = transform_from_geo_lat(lat2 + lat_d);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "4 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2 - lon_d;
		*lat = lat2 + lat_d;
		return 1;
	}

	c.x = transform_from_geo_lon(lon2 - lon_d);
	c.y = transform_from_geo_lat(lat2 - lat_d);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "5 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2 - lon_d;
		*lat = lat2 - lat_d;
		return 1;
	}


	c.x = transform_from_geo_lon(lon2 + lon_d);
	c.y = transform_from_geo_lat(lat2);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "6 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2 + lon_d;
		*lat = lat2;
		return 1;
	}

	c.x = transform_from_geo_lon(lon2 - lon_d);
	c.y = transform_from_geo_lat(lat2);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "7 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2 - lon_d;
		*lat = lat2;
		return 1;
	}

	c.x = transform_from_geo_lon(lon2);
	c.y = transform_from_geo_lat(lat2 + lat_d);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "8 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2;
		*lat = lat2 + lat_d;
		return 1;
	}


	c.x = transform_from_geo_lon(lon2);
	c.y = transform_from_geo_lat(lat2 - lat_d);

#ifdef DEBUG_CORRECT_BOUNDARY_REF_POINT
	fprintf(stderr, "9 c=%d y=%d\n", c.x, c.y);
#endif

	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2;
		*lat = lat2 - lat_d;
		return 1;
	}

	return 0;

}

void build_boundary_tree(GList *bl, GList *man_bl)
{
	long long rid;
	int retval;
	int rc = 0;
	int commit_after = 20000;
	int count = 0;
	int admin_l = 99;
	double lat, lon;
	struct coord c;
	GList *l = NULL;
	GList *l2 = NULL;
	GList *match = NULL;
	struct boundary *b;
	int admin_l_boundary;
	int max_admin_l;
	long long b_count = 0;
	long long b_max_count = 0;


	sqlite3_stmt *stmt_mm_1;
	int retval8 = sqlite3_prepare_v2(sql_handle, "select count(*) from boundary where done = 0;", -1, &stmt_mm_1, NULL);
	fprintf_(stderr, "prep:%d\n", retval8);
	sqlite3_step(stmt_mm_1);
	b_max_count = sqlite3_column_int64(stmt_mm_1, 0);
	fprintf_(stderr, "b_max_count:%lld\n", b_max_count);
	sqlite3_reset(stmt_mm_1);
	sqlite3_finalize(stmt_mm_1);


	// loop thru all the boundaries
	do
	{
		rc = sqlite3_step(stmt_bd_002);
		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:

				//  select rel_id, admin_level, lat, lon, name from boundary where done = 0;
				rid = sqlite3_column_int64(stmt_bd_002, 0);
				admin_l = sqlite3_column_int(stmt_bd_002, 1);
				lat = sqlite3_column_double(stmt_bd_002, 2);
				lon = sqlite3_column_double(stmt_bd_002, 3);
				c.x = transform_from_geo_lon(lon);
				c.y = transform_from_geo_lat(lat);

				b_count++;
				if ((b_count % 100) == 0)
				{
					fprintf_(stderr, "rel:%lld/%lld\n", b_count, b_max_count);
					// fprintf(stderr, "relid:%lld(%d) x=%d y=%d\n", rid, admin_l, c.x, c.y);
				}

				max_admin_l = 0;
				l2 = boundary_find_matches_level(bl, &c, 0, (admin_l - 1));
				l = l2;
				match = NULL;
				while (l)
				{
					b = l->data;
					char *admin_l_boundary_str = osm_tag_value(b->ib, "admin_level");
					if (!admin_l_boundary_str)
					{
						admin_l_boundary = 9999;
					}
					else
					{
						admin_l_boundary = atoi(admin_l_boundary_str);
					}
					
					if (admin_l_boundary < 2)
					{
						admin_l_boundary = 9999;
					}

					//fprintf(stderr, "b=%d\n", admin_l_boundary);
					if (admin_l_boundary > max_admin_l)
					{
						max_admin_l = admin_l_boundary;
						match = l;
						//fprintf(stderr, "b2=%d\n", max_admin_l);
					}
					l = g_list_next(l);
				}

				if (match)
				{
					b = match->data;
					//fprintf(stderr, "relid:%lld(%d) parentid:%lld(%d)\n", rid, admin_l, item_bin_get_relationid(b->ib), max_admin_l);

					sqlite3_bind_int64(stmt_bd_003, 1, item_bin_get_relationid(b->ib));
					sqlite3_bind_int64(stmt_bd_003, 2, rid);
					sqlite3_step(stmt_bd_003);
					sqlite3_reset(stmt_bd_003);
				}


				if (count == 0)
				{
					sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
				}
				count++;

				if (count > commit_after)
				{
					sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
					count = 0;
				}

				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);

	sqlite3_reset(stmt_bd_002);

	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);


	// ------- enter manual countries into boundary DB ----------
	GList *bl2 = man_bl;
	struct boundary_manual *bb3;

	struct country_table3
	{
		int countryid;
		char *names;
		char *admin_levels;
		FILE *file;
		int size;
		struct rect r;
	};


	sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
	while (bl2)
	{
		bb3 = bl2->data;
		if (bb3)
		{
			struct country_table3* cc3 = bb3->country;
			sqlite3_bind_int64(stmt_bd_001, 1, bb3->town_id); // dummy id
			sqlite3_bind_int(stmt_bd_001, 2, 2); // admin level
			sqlite3_bind_text(stmt_bd_001, 5, cc3->names, -1, SQLITE_STATIC); // name
			sqlite3_step(stmt_bd_001);
			sqlite3_reset(stmt_bd_001);
		}
		bl2 = g_list_next(bl2);
	}
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
	count = 0;


	// ------- now assign all the left overs towns/boundaries to countries ----------
	long long dummy_town_id2 = 0;
	count = 0;
	admin_l = 99;
	rc = 0;

	// loop thru all the boundaries
	do
	{
		rc = sqlite3_step(stmt_bd_002);
		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:

				//  select rel_id, admin_level, lat, lon, name from boundary where done = 0;
				rid = sqlite3_column_int64(stmt_bd_002, 0);
				admin_l = sqlite3_column_int(stmt_bd_002, 1);
				lat = sqlite3_column_double(stmt_bd_002, 2);
				lon = sqlite3_column_double(stmt_bd_002, 3);
				c.x = transform_from_geo_lon(lon);
				c.y = transform_from_geo_lat(lat);

				//fprintf(stderr, "relid:%lld(%d) x=%d y=%d\n", rid, admin_l, c.x, c.y);
				if (admin_l > 2)
				{

					// check if c is inside manual country border
					dummy_town_id2 = osm_process_street_by_manual_country_borders(man_bl, &c);

					if (dummy_town_id2 != -1)
					{
						sqlite3_bind_int64(stmt_bd_003, 1, dummy_town_id2); // parent rel_id
						sqlite3_bind_int64(stmt_bd_003, 2, rid); // rel_id
						sqlite3_step(stmt_bd_003);
						sqlite3_reset(stmt_bd_003);
					}


					if (count == 0)
					{
						sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
					}
					count++;

					if (count > commit_after)
					{
						sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
						count = 0;
					}

				}
				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_bd_002);

	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

}

struct node_lat_lon* get_first_coord_of_boundary(struct item_bin *item_bin_3, struct relation_member *memb)
{
	char *str = NULL;
	struct node_lat_lon* node_coords=g_new0(struct node_lat_lon, 1);

	while ((str = item_bin_get_attr(item_bin_3, attr_osm_member, str)))
	{
		if (get_relation_member(str, memb))
		{
			if (memb->type == 2) 
			{
				// way-id is "memb.id"
				// use the first nodes coords
				get_lat_lon_way_first_node(memb->id, node_coords);
				if (node_coords->valid == 1)
				{
					return node_coords;
				}
				break;
			}
		}
	}

	return NULL;
}

void add_boundary_to_db(long long current_id, int admin_level, struct item_bin *item_bin_3, struct relation_member *memb)
{
	char *str = NULL;
	struct node_lat_lon node_coords;

	// fprintf(stderr, "add_boundary_to_db:bid:%lld admin_level:%d\n", current_id, admin_level);

	sqlite3_bind_int64(stmt_bd_001, 1, current_id);

	if (admin_level < 2)
	{
		sqlite3_bind_int(stmt_bd_001, 2, 9999);
	}
	else
	{
		sqlite3_bind_int(stmt_bd_001, 2, admin_level);
	}

	while ((str = item_bin_get_attr(item_bin_3, attr_osm_member, str)))
	{
		if (get_relation_member(str, memb))
		{
			if (memb->type == 2) 
			{
				// way-id is "memb.id"
				// use the first nodes coords
				get_lat_lon_way_first_node(memb->id, &node_coords);
				if (node_coords.valid == 1)
				{
					sqlite3_bind_double(stmt_bd_001, 3, node_coords.lat);
					sqlite3_bind_double(stmt_bd_001, 4, node_coords.lon);
				}
				break;
			}
		}
	}
	sqlite3_bind_text(stmt_bd_001, 5, osm_tag_value(item_bin_3, "name"), -1, SQLITE_STATIC);
	sqlite3_step(stmt_bd_001);
	sqlite3_reset(stmt_bd_001);
}


void osm_add_member(int type, osmid ref, char *role)
{
	char member_buffer[BUFFER_SIZE * 3 + 3];
	struct attr memberattr = { attr_osm_member };

	sprintf(member_buffer,"%d:"LONGLONG_FMT":%s", type, (long long) ref, role);
	memberattr.u.str = member_buffer;
	item_bin_add_attr(item_bin_2, &memberattr);
}

static void relation_add_tag(char *k, char *v)
{
	int add_tag = 1;
#if 0
	fprintf(stderr,"add tag %s %s\n",k,v);
#endif
	if (!strcmp(k, "type"))
	{
		strcpy(relation_type, v);
		add_tag = 0;
	}
	else if (!strcmp(k, "restriction"))
	{
		if (!strncmp(v, "no_", 3))
		{
			item_bin_2->type = type_street_turn_restriction_no;
			add_tag = 0;
		}
		else if (!strncmp(v, "only_", 5))
		{
			item_bin_2->type = type_street_turn_restriction_only;
			add_tag = 0;
		}
		else
		{
			item_bin_2->type = type_none;
			//osm_warning("relation", current_id, 0, "Unknown restriction %s\n", v);
		}
	}
	else if (!strcmp(k, "admin_level"))
	{
		admin_level = atoi(v);
	}
	else if (!strcmp(k, "boundary"))
	{
		if (!strcmp(v, "administrative") || (experimental && !strcmp(v, "postal_code")))
		{
			boundary = 1;
		}
	}
	else if (!strcmp(k, "ISO3166-1"))
	{
		strcpy(iso_code, v);
	}

	if (add_tag)
	{
		//fprintf(stderr,"*TAG*%s=%s\n", k, v);
		char tag[strlen(k) + strlen(v) + 2];
		sprintf(tag, "%s=%s", k, v);
		item_bin_add_attr_string(item_bin_2, attr_osm_tag, tag);
	}
}

static int attr_longest_match(struct attr_mapping **mapping, int mapping_count, enum item_type *types, int types_count)
{
	int i, j, longest = 0, ret = 0, sum, val;
	struct attr_mapping *curr;
	for (i = 0; i < mapping_count; i++)
	{
		sum = 0;
		curr = mapping[i];
		for (j = 0; j < curr->attr_present_idx_count; j++)
		{
			val = attr_present[curr->attr_present_idx[j]];
			if (val)
				sum += val;
			else
			{
				sum = -1;
				break;
			}
		}
		if (sum > longest)
		{
			longest = sum;
			ret = 0;
		}
		if (sum > 0 && sum == longest && ret < types_count)
			types[ret++] = curr->type;
	}
	return ret;
}

static void attr_longest_match_clear(void)
{
	memset(attr_present, 0, sizeof(*attr_present) * attr_present_count);
}

char* remove_all_spaces_non_utf8(char *str)
{
	char *write = str, *read = str;

	if (str == NULL)
	{
		return NULL;
	}

	do
	{
		if (!g_ascii_isspace(*read))
		{
			*write++ = *read;
		}
	} while (*read++);

	return str;
}


char* get_index_for_string(const char *in)
{
	// first letters for indexing faster later
	char *sub = g_strndup(in, 2);


	// check first letter
	if (strlen(sub) > 0)
	{
		if ((sub[0] < 97) || (sub[0]) > 122)
		{
			sub[0]='\0';
			// fprintf(stderr, "(1)in=%s sub=%s\n", in, sub);
			return sub;
		}
	}

	// if second letter != a-z then delete it from index
	if (strlen(sub) > 1)
	{
		//            'a'               'z'
		if ((sub[1] < 97) || (sub[1]) > 122)
		{
			sub[1]='\0';
		}
	}

	// fprintf(stderr, "(2)in=%s sub=%s\n", in, sub);
	return sub;
}



static int tab_atan[]=
{	0,262,524,787,1051,1317,1584,1853,2126,2401,2679,2962,3249,3541,3839,4142,4452,4770,5095,5430,5774,6128,6494,6873,7265,7673,8098,8541,9004,9490,10000,10538};

static int
atan2_int_lookup(int val)
{
	int len=sizeof(tab_atan)/sizeof(int);
	int i=len/2;
	int p=i-1;

	for (;;)
	{
		i>>=1;
		if (val < tab_atan[p])
		{
			p-=i;
		}
		else
		{
			if (val < tab_atan[p+1])
			{
				return p+(p>>1);
			}
			else
			{
				p+=i;
			}
		}
	}
}


inline double my_atan2_double(double x, double y)
{
	double result;

	asm (
	"fpatan\n\t"
	: "=t" (result) // outputs; t = top of fpu stack
	: "0" (x), // inputs; 0 = same as result
	  "u" (y) // u = 2nd floating point register
	);

	return result;
}


static int
atan2_int(int dx, int dy)
{
	int mul=1,add=0,ret;

	if (! dx)
	{
		return dy < 0 ? 180 : 0;
	}

	if (! dy)
	{
		return dx < 0 ? -90 : 90;
	}

	if (dx < 0)
	{
		dx=-dx;
		mul=-1;
	}

	if (dy < 0)
	{
		dy=-dy;
		add=180*mul;
		mul*=-1;
	}

	while (dx > 20000 || dy > 20000)
	{
		dx/=10;
		dy/=10;
	}

	if (dx > dy)
	{
		ret=90-atan2_int_lookup(dy*10000/dx);
	}
	else
	{
		ret=atan2_int_lookup(dx*10000/dy);
	}

	return ret*mul+add;
}



static int maptool_get_angle_delta(struct coord *c1, struct coord *c2, int dir)
{
	int dx = c2->x - c1->x;
	int dy = c2->y - c1->y;

	// fprintf(stderr, "angle: %d %d %d %d\n", c1->x, c1->y, c2->x, c2->y);

#if 0
	double angle;
	angle = my_atan2_double(dy, dx);
	angle *= 57.29577952019129709110; // ( 180/ M_PI ) = 57.29577952019129709110
	//fprintf(stderr, "1 angle=%lf\n", angle);
#endif

#if 0
	double angle;
	angle = atan2(dx, dy);
	angle *= 57.29577952019129709110; // ( 180/ M_PI ) = 57.29577952019129709110
	fprintf(stderr, "2 angle=%lf\n", angle);
#endif

#if 1
	int angle;
	angle=atan2_int(dx,dy);
	//fprintf(stderr, "3 angle=%d\n", (int)(angle));
#endif

	if (dir == -1)
	{
		angle = angle - 180;
	}

	if (angle < 0)
	{
		angle += 360;
	}

	return angle;
}


/**
 * @brief Calculates the delta between two angles
 * @param angle1 The first angle
 * @param angle2 The second angle
 * @return The difference between the angles: -179..-1=angle2 is left of angle1,0=same,1..179=angle2 is right of angle1,180=angle1 is opposite of angle2
 */
static int maptool_delta_angle(int angle1, int angle2)
{
	int delta = angle2 - angle1;

	if (delta <= -180)
	{
		delta += 360;
	}

	if (delta > 180)
	{
		delta -= 360;
	}

	return delta;
}


clock_t maptool_debug_measure_start(void)
{
	clock_t start = clock();
	return start;
}

clock_t maptool_debug_measure_end(clock_t start_time)
{
	clock_t diff_time = clock() - start_time;
	return diff_time;
}

int maptool_debug_measure_end_tsecs(clock_t start_time)
{
	clock_t diff_time = clock() - start_time;
	return (int) (((double) diff_time / (double) CLOCKS_PER_SEC) * 1000);
}

void maptool_debug_measure_result_str(clock_t diff, char *buffer)
{
	sprintf(buffer, "elapsed: %fs\n", (diff / CLOCKS_PER_SEC));
}

void maptool_debug_mrp(clock_t diff)
{
	fprintf(stderr, "angle el:%fs\n", (double) ((double) diff / (double) CLOCKS_PER_SEC));
}



static int maptool_get_node_x_y(long long way_id, int node_num_in_way, struct coord *c_ret)
{
	int ret = 0;
	struct node_lat_lon node_coords2;

	// fprintf(stderr, "angle way id=%lld\n", way_id);

	long long way_node = get_waynode_num_have_seekpos(way_id, node_num_in_way, 0, (off_t)seekpos1);

	if (way_node > 0)
	{
		// fprintf(stderr, "angle way node id=%lld\n", way_node);

		get_lat_lon_for_node(way_node, &node_coords2);
		if (node_coords2.valid == 1)
		{
			//c_ret->x = transform_from_geo_lon(node_coords2.lon);
			c_ret->x = (int)(node_coords2.lon * 100000.0);

			//c_ret->y = transform_from_geo_lat(node_coords2.lat);
			c_ret->y = (int)(node_coords2.lat * 100000.0);

			// save coord of way_node
			// coord_buffer_3[node_num_in_way].x = c_ret->x;
			// coord_buffer_3[node_num_in_way].y = c_ret->y;

			// fprintf(stderr, "angle node_num=%d x=%d y=%d lon=%f lat=%f\n", node_num_in_way, c_ret->x, c_ret->y, node_coords2.lon, node_coords2.lat);
			ret = 1;
		}
	}

	return ret;
}


static void osm_write_cycle_way(FILE *ways_file, int i, int type, struct coord *coord_buffer_2, int start_node_num_in_way, int node_count, int part_num)
{
	int *def_flags, add_flags;
	struct item_bin *item_bin;

	item_bin = init_item(type, 0);
	def_flags = item_get_default_flags(type);

	// struct coord *coord_buffer_4 = &coord_buffer_3[start_node_num_in_way];
	struct coord *coord_buffer_4 = &coord_buffer[start_node_num_in_way];
	item_bin_add_coord(item_bin, coord_buffer_4, node_count);

	// fprintf(stderr, "Xangle node_num=%d count=%d wayid=%lld\n", start_node_num_in_way, node_count, osmid_attr_value);
	//int ii;
	//for (ii=0;ii<node_count;ii++)
	//{
	//	fprintf(stderr, "Yangle node_num=%d x=%d y=%d\n", ii, coord_buffer_4[ii].x, coord_buffer_4[ii].y);
	//}

	if (def_flags)
	{
		flags_attr_value = (*def_flags | flags[0] | flags[1]) & ~flags[2];
		// only add flags if they differ from the default flags!!
		if (flags_attr_value != *def_flags)
		{
			add_flags = 1;
		}
	}
	else // maybe should explicitly specify which "types" to consider? now its all "types"
	{
		def_flags = 0;
		flags_attr_value = (flags[0] | flags[1]) & ~flags[2];

		// only add flags if we really have a value
		if (flags_attr_value != 0)
		{
			add_flags = 1;
		}
	}

	item_bin_add_attr_string(item_bin, def_flags ? attr_street_name : attr_label, attr_strings[attr_string_label]);

	item_bin_add_attr_longlong(item_bin, attr_osm_wayid, osmid_attr_value);

	if (add_flags)
	{
		item_bin_add_attr_int(item_bin, attr_flags, flags_attr_value);
	}

	// ------- add DEBUG ATTR --------
	item_bin_add_attr_int(item_bin, attr_debugsplitway, start_node_num_in_way);
	// ------- add DEBUG ATTR --------

	if (maxspeed_attr_value)
	{
		item_bin_add_attr_int(item_bin, attr_maxspeed, maxspeed_attr_value);
	}

	// custom color attribute
	if (attr_strings[attr_string_colour_])
	{
		unsigned int cc = color_int_value_from_string(attr_strings[attr_string_colour_]);
		//fprintf(stderr, "col:ret=%d\n", cc);
		if (cc != 0)
		{
			cc = (cc >> 8);
			//fprintf(stderr, "col:in map=%d\n", (int)cc);
			item_bin_add_attr_int(item_bin, attr_colour2, (int)cc);
		}
	}

	item_bin_write(item_bin, ways_file);
}

long long cycle_w_processed_count = 0;

void osm_end_way(struct maptool_osm *osm)
{
	int i, count;
	int *def_flags, add_flags;
	enum item_type types[10];
	struct item_bin *item_bin;
	int count_lines = 0, count_areas = 0;
	int dont_save_to_db = 1;
	int first = 1;
	int retval;
	char *name_folded = NULL;
	char *name_folded2 = NULL;
	struct node_lat_lon node_coords;
	osmid w_node;
	int write_this_item_to_binfile;
	off_t seek_posx = 0;

	in_way = 0;

	if (!osm->ways)
	{
		return;
	}

	ways_processed_count++;

	write_this_item_to_binfile = 1;

	count = attr_longest_match(attr_mapping_way, attr_mapping_way_count, types, sizeof(types) / sizeof(enum item_type));
	if (!count)
	{
		count = 1;
		types[0] = type_street_unkn;
	}

	if (count >= 10)
	{
		fprintf_(stderr, "way id %lld\n", osmid_attr_value);
		dbg_assert(count < 10);
	}

	if (attr_strings[attr_string_label] != NULL)
	{
		if (strlen(attr_strings[attr_string_label]) > 0)
		{
			dont_save_to_db = 0;

			// check if "alt name" is same as normal name
			if (attr_strings[attr_string_label_alt] != NULL)
			{
				if (!strcmp(attr_strings[attr_string_label], attr_strings[attr_string_label_alt]))
				{
					// strings are the same, clear "alt name"
					attr_strings[attr_string_label_alt] = NULL;
				}
			}
		}
	}

	// required for fread to work!
	fseeko(ways_ref_file, 0L, SEEK_CUR);


	// correct bicycle flags ------------------------------------------------------------------------
	if ((flags[0] & NAVIT_AF_ONEWAY) || (flags[0] & NAVIT_AF_ONEWAYREV)) // it's a one way road
	{
		if (flags[0] & NAVIT_AF_ONEWAY_BICYCLE_NO) // bike against oneway
		{
			flags[0] = (flags[0] & ~NAVIT_AF_BICYCLE_LANE); // remove the lane flag
			flags[0] = (flags[0] & ~NAVIT_AF_BICYCLE_TRACK); // remove the track flag
		}
		flags[0] = (flags[0] & ~NAVIT_AF_ONEWAY_BICYCLE_YES); // its already oneway, so remove additional oneway-bike flag
	}
	else // no oneway
	{
		flags[0] = (flags[0] & ~NAVIT_AF_ONEWAY_BICYCLE_NO); // remove the bike against-oneway-road flag, if no oneway road
	}
	// correct bicycle flags ------------------------------------------------------------------------





	// check if its a bicycle way and also a footway, remove footway then!! -------------------------
	int is_cycleway = -1;
	int is_footway = -1;
	for (i = 0; i < count; i++)
	{
		// cycleway
		// footway
		if (types[i] == type_cycleway)
		{
			is_cycleway = i;
		}
		else if (types[i] == type_footway)
		{
			is_footway = i;
		}
	}

	if ((is_footway > -1) && (is_cycleway > -1))
	{
		types[is_footway] = type_none; // remove the footway
	}
	// check if its a bicycle way and also a footway, remove footway then!! -------------------------



	for (i = 0; i < count; i++) // loop thru all the attributes of this way (and add a way for every attribute known -> so ways get duplicated here!!)
	{
		add_flags = 0;
		write_this_item_to_binfile = 1;
		if (types[i] == type_none)
		{
			continue;
		}

		//if (ignore_unkown && (types[i] == type_street_unkn || types[i] == type_point_unkn))
		//	continue;

		// DO NOT exclude unknown streets here, or city-to-countrypolygon matching won't work!!
		if (ignore_unkown && (types[i] == type_point_unkn))
		{
			continue;
		}


		if (types[i] == type_cycleway)
		{

			// fprintf(stderr, "C=%lld W=%lld\n", cycle_w_processed_count, ways_processed_count);

			// check if we have a turn inside this way with angle greater than ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE
			int i_coord;
			int i_coord_prev = 0;
			struct coord c1_;
			struct coord c2_;
			struct coord c3_;
			int angle_way_1;
			int angle_way_2;
			int angle_way_delta;
			osmid way_node;
			int is_valid_;
			int fff = 1;
			int split_part_num;
			struct coord *coord_buffer_2 = NULL;

			if (coord_count > 2)
			{
				if (osmid_attr_value > 0)
				{
					cycle_w_processed_count++;

					coord_buffer_2 = &coord_buffer[0]; // point to first real coordinate of way
					split_part_num = 1;

					for (i_coord = 1; i_coord < (coord_count - 1); i_coord++)
					{
						is_valid_ = 0;

						//fprintf(stderr, "angle i_coord=%d\n", i_coord);

						if (fff == 1)
						{
							is_valid_ = is_valid_ + (maptool_get_node_x_y(osmid_attr_value, (i_coord - 1), &c1_));
							is_valid_ = is_valid_ + (maptool_get_node_x_y(osmid_attr_value, i_coord, &c2_));
							fff = 0;
						}
						else
						{
							c1_.x = c2_.x;
							c1_.y = c2_.y;
							c2_.x = c3_.x;
							c2_.y = c3_.y;
							is_valid_++;
							is_valid_++;
						}

						if (is_valid_ == 2)
						{
							angle_way_1 = maptool_get_angle_delta(&c1_, &c2_, 1);
							//fprintf(stderr, "angle1=%d\n", angle_way_1);
						}

						is_valid_++;
						is_valid_ = is_valid_ + (maptool_get_node_x_y(osmid_attr_value, (i_coord + 1), &c3_));

						if (is_valid_ == 4)
						{
							angle_way_2 = maptool_get_angle_delta(&c2_, &c3_, 1);
							//fprintf(stderr, "angle2=%d\n", angle_way_2);

							angle_way_delta = abs(maptool_delta_angle(angle_way_1, angle_way_2));
							//fprintf(stderr, "angle_way_delta=%d\n", angle_way_delta);

							if (angle_way_delta >= ROAD_ANGLE_MIN_FOR_TURN_BICYCLEMODE)
							{
									// dont write this item to binfile in the normal way
									write_this_item_to_binfile = 0;

									//fprintf(stderr, "angle cut at node:%d prevnode:%d\n", i_coord, i_coord_prev);
									osm_write_cycle_way(osm->ways, i, types[i], coord_buffer_2, i_coord_prev, (i_coord - i_coord_prev + 1), split_part_num);
									i_coord_prev = i_coord;
									coord_buffer_2 = coord_buffer_2 + (i_coord - i_coord_prev); // move pointer to next starting coord of split-way
									split_part_num++;
							}
						}
					}

					if (write_this_item_to_binfile == 0)
					{
						//fprintf(stderr, "angle: write last part node:%d prevnode:%d\n", i_coord, i_coord_prev);
						// write last part of split-way
						osm_write_cycle_way(osm->ways, i, types[i], coord_buffer_2, i_coord_prev, (i_coord - i_coord_prev + 1), split_part_num);
					}
				}
			}
		}


		if (types[i] != type_street_unkn)
		{
			if (types[i] < type_area)
			{
				count_lines++;
			}
			else
			{
				count_areas++;
			}
		}
		item_bin = init_item(types[i], 0);
		item_bin_add_coord(item_bin, coord_buffer, coord_count);
		def_flags = item_get_default_flags(types[i]);

		// save nodes of way -----------------
		// save nodes of way -----------------

		if (coastline_only_map == 1)
		{
			first = 0;
		}

		if (first == 1)
		{
			first = 0;
			int i788 = 0;
			struct coord *c788 = (struct coord *) (item_bin + 1); // set pointer to coord struct of this item
			for (i788 = 0; i788 < item_bin->clen / 2; i788++)
			{
				if (dont_save_to_db == 0)
				{
					// save lat,lon of first node of this way
					if (i788 == 0)
					{
#ifdef MAPTOOL_USE_SQL
						//fprintf(stderr, "insert -w\n");
						//fprintf(stderr, "ways DEBUG: wid:%ld\n", osmid_attr_value);
						//fprintf(stderr, "ways DEBUG: nid:%lld\n", first_node_of_current_way);
						get_lat_lon_for_node(first_node_of_current_way, &node_coords);

						if (sql_counter == 0)
						{
							retval = sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
							if ((retval > 0) && (retval < 100))
							{
								fprintf_(stderr, "==================================\n");
								fprintf_(stderr, "begin: ways code:%d\n", retval);
								retval = sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
								fprintf_(stderr, "begin:(commit) ways code:%d\n", retval);
								retval = sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
								fprintf_(stderr, "begin:(begin) ways code:%d\n", retval);
								fprintf_(stderr, "==================================\n");
							}
							else
							{
								//fprintf(stderr, "begin: ways\n");
							}
						}


						// only save this way info to way table (used for index search) if its not a nonsearch way (like river, or some district outline)

						if (!item_not_for_search_index(types[i]))
						{

						sql_counter++;

						// save ORIG name to DB -------------------------------------
						sqlite3_bind_int64(stmt_way, 1, osmid_attr_value);
						sqlite3_bind_text(stmt_way, 2, attr_strings[attr_string_label], -1, SQLITE_STATIC);

						// streetname folded for later sort/search
						char *sub = NULL;
						name_folded = linguistics_casefold(attr_strings[attr_string_label]);
						if (name_folded)
						{
							name_folded2 = linguistics_remove_all_specials(name_folded);
							if (name_folded2)
							{
								g_free(name_folded);
								name_folded = name_folded2;
							}

							name_folded2 = linguistics_expand_special(name_folded, 1);
							if (name_folded2)
							{
								sqlite3_bind_text(stmt_way, 6, name_folded2, -1, SQLITE_STATIC);
								sub = get_index_for_string(name_folded2);
								sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
							}
							else
							{
								sqlite3_bind_text(stmt_way, 6, name_folded, -1, SQLITE_STATIC);
								sub = get_index_for_string(name_folded);
								sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
							}
						}
						else
						{
							// use original string
							sqlite3_bind_text(stmt_way, 6, attr_strings[attr_string_label], -1, SQLITE_STATIC);
							sub = get_index_for_string(attr_strings[attr_string_label]);
							sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
						}

						// town id
						sqlite3_bind_int(stmt_way, 3, -1);

						if (node_coords.valid == 1)
						{
							// lat
							sqlite3_bind_double(stmt_way, 4, node_coords.lat);
							// lon
							sqlite3_bind_double(stmt_way, 5, node_coords.lon);
						}

						// waytype
						if (types[i] != type_street_unkn)
						{
							sqlite3_bind_int(stmt_way, 8, 1);
						}
						else
						{
							// type -> POI
							sqlite3_bind_int(stmt_way, 8, 40);
						}

						// retval = 
						sqlite3_step(stmt_way);
						//if ((retval > 0) && (retval < 100))
						//{
						//	fprintf(stderr, "ways step: code:%d wid:%lld\n", retval, osmid_attr_value);
						//}
						sqlite3_reset(stmt_way);

						if (name_folded)
						{
							g_free(name_folded);
						}

						if (name_folded2)
						{
							g_free(name_folded2);
						}

						if (sub)
						{
								g_free(sub);
						}
						// save ORIG name to DB -------------------------------------

						// save alternate name to DB aswell -------------------------------------
						if (attr_strings[attr_string_label_real_alt])
						{
							sqlite3_bind_int64(stmt_way, 1, osmid_attr_value);
							sqlite3_bind_text(stmt_way, 2, attr_strings[attr_string_label_real_alt], -1, SQLITE_STATIC);


							// streetname folded for later sort/search
							char *sub = NULL;
							name_folded = linguistics_casefold(attr_strings[attr_string_label_real_alt]);
							if (name_folded)
							{
								name_folded2 = linguistics_remove_all_specials(name_folded);
								if (name_folded2)
								{
									g_free(name_folded);
									name_folded = name_folded2;
								}

								name_folded2 = linguistics_expand_special(name_folded, 1);
								if (name_folded2)
								{
									sqlite3_bind_text(stmt_way, 6, name_folded2, -1, SQLITE_STATIC);
									sub = get_index_for_string(name_folded2);
									sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
								}
								else
								{
									sqlite3_bind_text(stmt_way, 6, name_folded, -1, SQLITE_STATIC);
									sub = get_index_for_string(name_folded);
									sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
								}
							}
							else
							{
								// use original string
								sqlite3_bind_text(stmt_way, 6, attr_strings[attr_string_label_real_alt], -1, SQLITE_STATIC);
								sub = get_index_for_string(attr_strings[attr_string_label_real_alt]);
								sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
							}

							// town id
							sqlite3_bind_int(stmt_way, 3, -1);

							if (node_coords.valid == 1)
							{
								// lat
								sqlite3_bind_double(stmt_way, 4, node_coords.lat);
								// lon
								sqlite3_bind_double(stmt_way, 5, node_coords.lon);
							}

							// waytype
							if (types[i] != type_street_unkn)
							{
								sqlite3_bind_int(stmt_way, 8, 3);
							}
							else
							{
								// type -> POI
								sqlite3_bind_int(stmt_way, 8, 40);
							}

							// retval = 
							sqlite3_step(stmt_way);
							//if ((retval > 0) && (retval < 100))
							//{
							//	fprintf(stderr, "ways step: code:%d wid:%lld\n", retval, osmid_attr_value);
							//}
							sqlite3_reset(stmt_way);

							if (name_folded)
							{
								g_free(name_folded);
							}

							if (name_folded2)
							{
								g_free(name_folded2);
							}

							if (sub)
							{
									g_free(sub);
							}

						}
						// save alternate name to DB aswell -------------------------------------


						// save english name to DB aswell -------------------------------------
						if (attr_strings[attr_string_label_alt])
						{
							sqlite3_bind_int64(stmt_way, 1, osmid_attr_value);
							sqlite3_bind_text(stmt_way, 2, attr_strings[attr_string_label_alt], -1, SQLITE_STATIC);


							// streetname folded for later sort/search
							char *sub = NULL;
							name_folded = linguistics_casefold(attr_strings[attr_string_label_alt]);
							if (name_folded)
							{
								name_folded2 = linguistics_remove_all_specials(name_folded);
								if (name_folded2)
								{
									g_free(name_folded);
									name_folded = name_folded2;
								}

								name_folded2 = linguistics_expand_special(name_folded, 1);
								if (name_folded2)
								{
									sqlite3_bind_text(stmt_way, 6, name_folded2, -1, SQLITE_STATIC);
									sub = get_index_for_string(name_folded2);
									sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
								}
								else
								{
									sqlite3_bind_text(stmt_way, 6, name_folded, -1, SQLITE_STATIC);
									sub = get_index_for_string(name_folded);
									sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
								}
							}
							else
							{
								// use original string
								sqlite3_bind_text(stmt_way, 6, attr_strings[attr_string_label_alt], -1, SQLITE_STATIC);
								sub = get_index_for_string(attr_strings[attr_string_label_alt]);
								sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
							}

							// town id
							sqlite3_bind_int(stmt_way, 3, -1);

							if (node_coords.valid == 1)
							{
								// lat
								sqlite3_bind_double(stmt_way, 4, node_coords.lat);
								// lon
								sqlite3_bind_double(stmt_way, 5, node_coords.lon);
							}

							// waytype
							if (types[i] != type_street_unkn)
							{
								sqlite3_bind_int(stmt_way, 8, 2);
							}
							else
							{
								// type -> POI
								sqlite3_bind_int(stmt_way, 8, 40);
							}

							// retval = 
							sqlite3_step(stmt_way);
							//if ((retval > 0) && (retval < 100))
							//{
							//	fprintf(stderr, "ways step: code:%d wid:%lld\n", retval, osmid_attr_value);
							//}
							sqlite3_reset(stmt_way);

							if (name_folded)
							{
								g_free(name_folded);
							}

							if (name_folded2)
							{
								g_free(name_folded2);
							}

							if (sub)
							{
									g_free(sub);
							}

						}
						// save english name to DB aswell -------------------------------------

						}


						if (sql_counter > MAX_ROWS_WO_COMMIT_3)
						{
							sql_counter = 0;
							retval = sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
							if ((retval > 0) && (retval < 100))
							{
								fprintf_(stderr, "ways: %lld code:%d\n", ways_processed_count, retval);
							}
							else
							{
								//fprintf(stderr, "ways: %lld\n", ways_processed_count);
							}
						}
#endif
						// set ref file to start of waynodes of current way
						// fprintf(stderr, "w seek1 pos=%lld\n", seekpos1);
						seek_posx = ftello(ways_ref_file);
						if ((off_t)seek_posx != (off_t)seekpos1)
						{
							fseeko(ways_ref_file, (off_t)seekpos1, SEEK_SET);
						}
					}
					// save all the way nodes for streets
					int fret = (int)fread(&w_node, sizeof(osmid), 1, ways_ref_file);
					if (fret == 0)
					{
						fprintf(stderr, "**ERROR** at fread 001a: wayid:%lld count=%d\n", osmid_attr_value, (int)i788);
					}
					else if ((osmid)w_node > 4994968164L)
					{
						fprintf(stderr, "**ERROR** at fread 001b: wayid:%lld count=%d w_node=%lld\n", osmid_attr_value, (int)i788, (osmid)w_node);
					}
					// fprintf(stderr, "at fread: w_node:%lld\n", (osmid)w_node);
					// save first node of WAYS with name/label into db
					add_waynode_to_db((osmid)w_node, (int)i788);
				}
				else // rest of ways
				{
					// save all the waynodes to db
					if (i788 == 0)
					{
						// set ref file to start of waynodes of current way
						// fprintf(stderr, "w seek1.1 pos=%lld\n", seekpos1);
						seek_posx = ftello(ways_ref_file);
						if ((off_t)seek_posx != (off_t)seekpos1)
						{
							fseeko(ways_ref_file, (off_t)seekpos1, SEEK_SET);
						}
					}

					int fret = (int)fread(&w_node, sizeof(osmid), 1, ways_ref_file);
					if (fret == 0)
					{
						fprintf(stderr, "**ERROR** at fread 001.1a: wayid:%lld count=%d\n", osmid_attr_value, (int)i788);
					}
					else if ((osmid)w_node > 4994968164L)
					{
						fprintf(stderr, "**ERROR** at fread 001.1b: wayid:%lld count=%d w_node=%lld\n", osmid_attr_value, (int)i788, (osmid)w_node);
					}
					// save first node of ALL WAYS into db
					add_waynode_to_db((osmid)w_node, (int)i788);
				}
			}

			if (dont_save_to_db == 0)
			{
				// seek to end of file (this should not be needed, because we read values until EOF anyway)
				// fprintf(stderr, "w seek2\n");
				fseeko(ways_ref_file, 0, SEEK_END);
			}
		}
		// save nodes of way -----------------
		// save nodes of way -----------------


		if (def_flags)
		{
			flags_attr_value = (*def_flags | flags[0] | flags[1]) & ~flags[2];
			// only add flags if they differ from the default flags!!
			if (flags_attr_value != *def_flags)
			{
				add_flags = 1;
			}
		}
		else // maybe should explicitly specify which "types" to consider? now its all "types"
		{
			def_flags = 0;
			flags_attr_value = (flags[0] | flags[1]) & ~flags[2];

			// only add flags if we really have a value
			if (flags_attr_value != 0)
			{
				add_flags = 1;
			}
		}
		item_bin_add_attr_string(item_bin, def_flags ? attr_street_name : attr_label, attr_strings[attr_string_label]);

		// housenumber from buildings --------------------
		if (types[i] == type_poly_building)
		{
			// addr:street
			item_bin_add_attr_string(item_bin, attr_street_name_dummy, attr_strings[attr_string_street_name]);
			// fprintf(stderr, "addr:street     :%s\n", attr_strings[attr_string_street_name]);
			// addr:housenumber
			item_bin_add_attr_string(item_bin, attr_house_number_dummy, attr_strings[attr_string_house_number]);
			// fprintf(stderr, "addr:housenumber:%s\n", attr_strings[attr_string_house_number]);
		}
		// housenumber from buildings --------------------


		if (attr_strings[attr_string_label_alt])
		{
			item_bin_add_attr_string(item_bin, attr_street_name_match, attr_strings[attr_string_label_alt]);
			if (debug_itembin(ib))
			{
				fprintf(stderr, "street name    : %s\n", attr_strings[attr_string_label]);
				fprintf(stderr, "street name sys: %s\n", attr_strings[attr_string_street_name_systematic]);
				fprintf(stderr, "street name alt: %s\n", attr_strings[attr_string_label_alt]);
			}
		}
		item_bin_add_attr_string(item_bin, attr_street_name_systematic, attr_strings[attr_string_street_name_systematic]);

		// -- NEW 001 --
		item_bin_add_attr_string(item_bin, attr_street_name_systematic_nat, attr_strings[attr_string_street_name_systematic_nat]);
		item_bin_add_attr_string(item_bin, attr_street_lanes, attr_strings[attr_string_street_lanes]);
		item_bin_add_attr_string(item_bin, attr_street_lanes_forward, attr_strings[attr_string_street_lanes_forward]);
		item_bin_add_attr_string(item_bin, attr_street_turn_lanes, attr_strings[attr_string_street_turn_lanes]);
		item_bin_add_attr_string(item_bin, attr_street_destination, attr_strings[attr_string_street_destination]);
		item_bin_add_attr_string(item_bin, attr_street_destination_lanes, attr_strings[attr_string_street_destination_lanes]);
		// -- NEW 001 --

		item_bin_add_attr_longlong(item_bin, attr_osm_wayid, osmid_attr_value);

		if (debug_attr_buffer[0])
		{
			item_bin_add_attr_string(item_bin, attr_debug, debug_attr_buffer);
		}

		if (add_flags)
		{
			item_bin_add_attr_int(item_bin, attr_flags, flags_attr_value);
		}

		if (maxspeed_attr_value)
		{
			item_bin_add_attr_int(item_bin, attr_maxspeed, maxspeed_attr_value);
		}

		// custom color attribute
		if (attr_strings[attr_string_colour_])
		{
			unsigned int cc = color_int_value_from_string(attr_strings[attr_string_colour_]);
			//fprintf(stderr, "col:ret=%d\n", cc);
			if (cc != 0)
			{
				cc = (cc >> 8);
				//fprintf(stderr, "col:in map=%d\n", (int)cc);
				item_bin_add_attr_int(item_bin, attr_colour2, (int)cc);
			}
		}

		// if we duplicated this way here (because of multiple attributes), then set "duplicate_way" attr
		if (i > 0)
		{
			item_bin_add_attr_int(item_bin, attr_duplicate_way, 1);
			//fprintf(stderr, "attr_duplicate_way:1: dup=true wayid=%lld\n", item_bin_get_wayid(item_bin));
		}

		if (write_this_item_to_binfile == 1)
		{
			item_bin_write(item_bin, osm->ways);
		}

		if (border_only_map_as_xml == 1)
		{
			item_bin_write_xml(item_bin, "borders.xml");
		}
	}

	/*
	 if(osm->line2poi) {
	 count=attr_longest_match(attr_mapping_way2poi, attr_mapping_way2poi_count, types, sizeof(types)/sizeof(enum item_type));
	 dbg_assert(count < 10);
	 for (i = 0 ; i < count ; i++) {
	 if (types[i] == type_none || types[i] == type_point_unkn)
	 continue;
	 item_bin=init_item(types[i], 0);
	 item_bin_add_coord(item_bin, coord_buffer, coord_count);
	 item_bin_add_attr_string(item_bin, attr_label, attr_strings[attr_string_label]);
	 item_bin_add_attr_string(item_bin, attr_house_number, attr_strings[attr_string_house_number]);
	 item_bin_add_attr_string(item_bin, attr_street_name, attr_strings[attr_string_street_name]);
	 item_bin_add_attr_string(item_bin, attr_phone, attr_strings[attr_string_phone]);
	 item_bin_add_attr_string(item_bin, attr_fax, attr_strings[attr_string_fax]);
	 item_bin_add_attr_string(item_bin, attr_email, attr_strings[attr_string_email]);
	 item_bin_add_attr_string(item_bin, attr_county_name, attr_strings[attr_string_county_name]);
	 item_bin_add_attr_string(item_bin, attr_url, attr_strings[attr_string_url]);
	 item_bin_add_attr_longlong(item_bin, attr_osm_wayid, osmid_attr_value);
	 item_bin_write(item_bin, count_areas<count_lines?osm->line2poi:osm->poly2poi);
	 }
	 }
	 */

	attr_longest_match_clear();
}

static struct css_color css_cols[]= {
{"aliceblue",0xf0f8ff},
{"antiquewhite",0xfaebd7},
{"aqua",0x00ffff},
{"aquamarine",0x7fffd4},
{"azure",0xf0ffff},
{"beige",0xf5f5dc},
{"bisque",0xffe4c4},
{"black",0x000000},
{"blanchedalmond",0xffebcd},
{"blue",0x0000ff},
{"blueviolet",0x8a2be2},
{"brown",0xa52a2a},
{"burlywood",0xdeb887},
{"cadetblue",0x5f9ea0},
{"chartreuse",0x7fff00},
{"chocolate",0xd2691e},
{"coral",0xff7f50},
{"cornflowerblue",0x6495ed},
{"cornsilk",0xfff8dc},
{"crimson",0xdc143c},
{"cyan",0x00ffff},
{"darkblue",0x00008b},
{"darkcyan",0x008b8b},
{"darkgoldenrod",0xb8860b},
{"darkgray",0xa9a9a9},
{"darkgreen",0x006400},
{"darkkhaki",0xbdb76b},
{"darkmagenta",0x8b008b},
{"darkolivegreen",0x556b2f},
{"darkorange",0xff8c00},
{"darkorchid",0x9932cc},
{"darkred",0x8b0000},
{"darksalmon",0xe9967a},
{"darkseagreen",0x8fbc8f},
{"darkslateblue",0x483d8b},
{"darkslategray",0x2f4f4f},
{"darkturquoise",0x00ced1},
{"darkviolet",0x9400d3},
{"deeppink",0xff1493},
{"deepskyblue",0x00bfff},
{"dimgray",0x696969},
{"dodgerblue",0x1e90ff},
{"firebrick",0xb22222},
{"floralwhite",0xfffaf0},
{"forestgreen",0x228b22},
{"fuchsia",0xff00ff},
{"gainsboro",0xdcdcdc},
{"ghostwhite",0xf8f8ff},
{"gold",0xffd700},
{"goldenrod",0xdaa520},
{"gray",0x808080},
{"green",0x008000},
{"greenyellow",0xadff2f},
{"honeydew",0xf0fff0},
{"hotpink",0xff69b4},
{"indianred",0xcd5c5c},
{"indigo",0x4b0082},
{"ivory",0xfffff0},
{"khaki",0xf0e68c},
{"lavender",0xe6e6fa},
{"lavenderblush",0xfff0f5},
{"lawngreen",0x7cfc00},
{"lemonchiffon",0xfffacd},
{"lightblue",0xadd8e6},
{"lightcoral",0xf08080},
{"lightcyan",0xe0ffff},
{"lightgoldenrodyellow",0xfafad2},
{"lightgrey",0xd3d3d3},
{"lightgreen",0x90ee90},
{"lightpink",0xffb6c1},
{"lightsalmon",0xffa07a},
{"lightseagreen",0x20b2aa},
{"lightskyblue",0x87cefa},
{"lightslategray",0x778899},
{"lightsteelblue",0xb0c4de},
{"lightyellow",0xffffe0},
{"lime",0x00ff00},
{"limegreen",0x32cd32},
{"linen",0xfaf0e6},
{"magenta",0xff00ff},
{"maroon",0x800000},
{"mediumaquamarine",0x66cdaa},
{"mediumblue",0x0000cd},
{"mediumorchid",0xba55d3},
{"mediumpurple",0x9370d8},
{"mediumseagreen",0x3cb371},
{"mediumslateblue",0x7b68ee},
{"mediumspringgreen",0x00fa9a},
{"mediumturquoise",0x48d1cc},
{"mediumvioletred",0xc71585},
{"midnightblue",0x191970},
{"mintcream",0xf5fffa},
{"mistyrose",0xffe4e1},
{"moccasin",0xffe4b5},
{"navajowhite",0xffdead},
{"navy",0x000080},
{"oldlace",0xfdf5e6},
{"olive",0x808000},
{"olivedrab",0x6b8e23},
{"orange",0xffa500},
{"orangered",0xff4500},
{"orchid",0xda70d6},
{"palegoldenrod",0xeee8aa},
{"palegreen",0x98fb98},
{"paleturquoise",0xafeeee},
{"palevioletred",0xd87093},
{"papayawhip",0xffefd5},
{"peachpuff",0xffdab9},
{"peru",0xcd853f},
{"pink",0xffc0cb},
{"plum",0xdda0dd},
{"powderblue",0xb0e0e6},
{"purple",0x800080},
{"red",0xff0000},
{"rosybrown",0xbc8f8f},
{"royalblue",0x4169e1},
{"saddlebrown",0x8b4513},
{"salmon",0xfa8072},
{"sandybrown",0xf4a460},
{"seagreen",0x2e8b57},
{"seashell",0xfff5ee},
{"sienna",0xa0522d},
{"silver",0xc0c0c0},
{"skyblue",0x87ceeb},
{"slateblue",0x6a5acd},
{"slategray",0x708090},
{"snow",0xfffafa},
{"springgreen",0x00ff7f},
{"steelblue",0x4682b4},
{"tan",0xd2b48c},
{"teal",0x008080},
{"thistle",0xd8bfd8},
{"tomato",0xff6347},
{"turquoise",0x40e0d0},
{"violet",0xee82ee},
{"wheat",0xf5deb3},
{"white",0xffffff},
{"whitesmoke",0xf5f5f5},
{"yellow",0xffff00},
{"yellowgreen",0x9acd32},
};


unsigned int color_int_value_from_string(char* color_str)
{
	int count = 0;
	unsigned int ret = 0; // 0 -> invalid color!
	struct css_color* c;

	for (count = 0;count < (sizeof(css_cols)/sizeof(struct css_color));count++)
	{
		c = &css_cols[count];
		if (!strcmp(c->col_name, color_str))
		{
			//fprintf(stderr, "col=%d\n", c->col_value);
			ret = (c->col_value << 8) | 0x1; // set alpha to 0x1
			//fprintf(stderr, "col ret1=%d\n", ret);
			return ret;
		}
	}

	return ret;
}


void osm_append_housenumber_node(FILE *out, struct coord *c, char *house_number, char *street_name)
{

	// c is inside the same buffer as the new item will be. so make a copy of c now!
	struct coord *c_cpy = coord_new(c->x, c->y);

	struct item_bin *item_bin;
	item_bin = init_item(type_house_number, 0);
	// item_bin->type = type_house_number;

	item_bin_add_coord(item_bin, c_cpy, 1);
	item_bin_add_attr_string(item_bin, attr_house_number, house_number);
	item_bin_add_attr_string(item_bin, attr_street_name, street_name);

	// DEBUG -- DEBUG -- DEBUG --
	// DEBUG -- DEBUG -- DEBUG --
	// dump_itembin(item_bin);
	// DEBUG -- DEBUG -- DEBUG --
	// DEBUG -- DEBUG -- DEBUG --

	item_bin_write(item_bin, out);

	g_free(c_cpy);
}


void add_point_as_way_to_db(char *label, osmid id, int waytype, double lat, double lon)
{
	char *sub = NULL;
	char *name_folded2 = NULL;
	char *name_folded = NULL;
	int retval;

	if (label)
	{
		if (sql_counter == 0)
		{
			sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
		}


		sqlite3_bind_int64(stmt_way, 1, id);
		sqlite3_bind_text(stmt_way, 2, label, -1, SQLITE_STATIC);


		// streetname folded for later sort/search
		name_folded = linguistics_casefold(label);
		if (name_folded)
		{
			name_folded2 = linguistics_remove_all_specials(name_folded);
			if (name_folded2)
			{
				g_free(name_folded);
				name_folded = name_folded2;
			}

			name_folded2 = linguistics_expand_special(name_folded, 1);
			if (name_folded2)
			{
				sqlite3_bind_text(stmt_way, 6, name_folded2, -1, SQLITE_STATIC);
				sub = get_index_for_string(name_folded2);
				sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
			}
			else
			{
				sqlite3_bind_text(stmt_way, 6, name_folded, -1, SQLITE_STATIC);
				sub = get_index_for_string(name_folded);
				sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
			}
		}
		else
		{
			// use original string
			sqlite3_bind_text(stmt_way, 6, label, -1, SQLITE_STATIC);
			sub = get_index_for_string(label);
			sqlite3_bind_text(stmt_way, 7, sub, -1, SQLITE_STATIC);
		}

		// town id
		sqlite3_bind_int(stmt_way, 3, -1);

		// lat
		sqlite3_bind_double(stmt_way, 4, lat);
		// lon
		sqlite3_bind_double(stmt_way, 5, lon);

		// waytype
		sqlite3_bind_int(stmt_way, 8, waytype);

		// retval = 
		sqlite3_step(stmt_way);
		//if ((retval > 0) && (retval < 100))
		//{
		//	fprintf(stderr, "ways step: code:%d wid:%lld\n", retval, osmid_attr_value);
		//}
		sqlite3_reset(stmt_way);

		if (name_folded)
		{
			g_free(name_folded);
		}

		if (name_folded2)
		{
			g_free(name_folded2);
		}

		if (sub)
		{
			g_free(sub);
		}

		sql_counter++;
		if (sql_counter > MAX_ROWS_WO_COMMIT_3)
		{
			sql_counter = 0;
			retval = sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
			if ((retval > 0) && (retval < 100))
			{
				fprintf_(stderr, "add_point_as_way_to_db:code:%d\n", retval);
			}
		}

	}

}


void osm_end_node(struct maptool_osm *osm)
{
	int count, i;
	char *postal;
	enum item_type types[10];
	struct item_bin *item_bin;
	in_node = 0;

	if (!osm->nodes || !node_is_tagged || !nodeid)
	{
		return;
	}

	count = attr_longest_match(attr_mapping_node, attr_mapping_node_count, types, sizeof(types) / sizeof(enum item_type));

	if (!count)
	{
		types[0] = type_point_unkn;
		count = 1;
	}

	dbg_assert(count < 10);


	if (attr_strings[attr_string_label] != NULL)
	{
		// check if "alt name" is same as normal name
		if (attr_strings[attr_string_label_alt] != NULL)
		{
			if (!strcmp(attr_strings[attr_string_label], attr_strings[attr_string_label_alt]))
			{
				// strings are the same, clear "alt name"
				attr_strings[attr_string_label_alt] = NULL;
			}
		}
	}


	for (i = 0; i < count; i++)
	{
		if (types[i] == type_none)
		{
			continue;
		}

		//if (ignore_unkown && (types[i] == type_street_unkn || types[i] == type_point_unkn))
		//	continue;

		if (ignore_unkown && (types[i] == type_point_unkn))
		{
			continue;
		}

		item_bin = init_item(types[i], 0);

		if (item_is_town(*item_bin) && attr_strings[attr_string_label])
		{
			if (debug_itembin(ib))
			{
				fprintf(stderr, "osm_end_node: have town: %s\n", attr_strings[attr_string_label]);
			}
		}

		if (item_is_district(*item_bin) && attr_strings[attr_string_label])
		{
			if (debug_itembin(ib))
			{
				fprintf(stderr, "osm_end_node: have district: %s\n", attr_strings[attr_string_label]);
			}
		}

		if (item_is_town(*item_bin) && attr_strings[attr_string_population])
		{
			item_bin_set_type_by_population(item_bin, atoi(attr_strings[attr_string_population]));
		}

		// dont forget to qualify districts
		if (item_is_district(*item_bin) && attr_strings[attr_string_population])
		{
			item_bin_set_type_by_population(item_bin, atoi(attr_strings[attr_string_population]));
		}

		item_bin_add_coord(item_bin, &ni->c, 1);
		if (((types[i] == type_poi_bicycle_parking) || (types[i] == type_poi_bicycle_rental)) && (attr_strings[attr_string_capacity] != NULL) && (strlen(attr_strings[attr_string_capacity]) > 0))
		{
			char *label_new;
			if ((attr_strings[attr_string_label] != NULL) && (strlen(attr_strings[attr_string_label]) > 1))
			{
				// fprintf(stderr, "XXX1:%s:%s\n", attr_strings[attr_string_label], attr_strings[attr_string_capacity]);
				label_new = g_strdup_printf("%s:%s", attr_strings[attr_string_label], attr_strings[attr_string_capacity]);
			}
			else
			{
				label_new = g_strdup_printf("%s", attr_strings[attr_string_capacity]);
			}
			item_bin_add_attr_string(item_bin, attr_label, label_new);
			g_free(label_new);
		}
		else
		{
			item_bin_add_attr_string(item_bin, item_is_town(*item_bin) ? attr_town_name : attr_label, attr_strings[attr_string_label]);
		}
		item_bin_add_attr_string(item_bin, attr_house_number, attr_strings[attr_string_house_number]);
		item_bin_add_attr_string(item_bin, attr_street_name, attr_strings[attr_string_street_name]);
		item_bin_add_attr_string(item_bin, attr_phone, attr_strings[attr_string_phone]);
		item_bin_add_attr_string(item_bin, attr_fax, attr_strings[attr_string_fax]);
		item_bin_add_attr_string(item_bin, attr_email, attr_strings[attr_string_email]);
		item_bin_add_attr_string(item_bin, attr_county_name, attr_strings[attr_string_county_name]);
		item_bin_add_attr_string(item_bin, attr_url, attr_strings[attr_string_url]);

		// -- NEW 001 --
		item_bin_add_attr_string(item_bin, attr_ref, attr_strings[attr_string_ref]);
		item_bin_add_attr_string(item_bin, attr_exit_to, attr_strings[attr_string_exit_to]);
		// -- NEW 001 --

		if ((types[i] == type_poi_bicycle_parking) || (types[i] == type_poi_bicycle_rental))
		{
			int capacity = 0;
			if (attr_strings[attr_string_capacity] != NULL)
			{
				if (strlen(attr_strings[attr_string_capacity]) > 0)
				{
					capacity = atoi(attr_strings[attr_string_capacity]);
					item_bin_add_attr_int(item_bin, attr_capacity, attr_strings[attr_string_capacity]);
				}
			}
		}

		item_bin_add_attr_longlong(item_bin, attr_osm_nodeid, osmid_attr_value);
		item_bin_add_attr_string(item_bin, attr_debug, debug_attr_buffer);

		if ((item_is_town(*item_bin)) || (item_is_district(*item_bin)))
		{
			if (attr_strings[attr_string_label_alt])
			{
				item_bin_add_attr_string(item_bin, attr_town_name_match, attr_strings[attr_string_label_alt]);
				// if (debug_itembin(item_bin))
				// {
				//	fprintf(stderr, "town name    : %s\n", attr_strings[attr_string_label]);
				//	fprintf(stderr, "town name alt: %s\n", attr_strings[attr_string_label_alt]);
				// }
			}
		}

		postal = attr_strings[attr_string_postal];
		if (postal)
		{
			char *sep = strchr(postal, ',');
			if (sep)
			{
				*sep = '\0';
			}
			item_bin_add_attr_string(item_bin, item_is_town(*item_bin) ? attr_town_postal : attr_postal, postal);
		}

		item_bin_write(item_bin, osm->nodes);




		// town waytype = 4
		if ((item_is_town(*item_bin)) || (item_is_district(*item_bin)))
		{
			if (attr_strings[attr_string_label])
			{
				add_point_as_way_to_db(attr_strings[attr_string_label], osmid_attr_value, 4, save_node_lat, save_node_lon);
			}
			
			if (attr_strings[attr_string_label_alt])
			{
				add_point_as_way_to_db(attr_strings[attr_string_label_alt], osmid_attr_value, 4, save_node_lat, save_node_lon);
			}
		}

		// POI waytype = 40
		if (item_is_poi(types[i]))
		{

			if (!item_not_for_search_index(types[i]))
			{
				if (attr_strings[attr_string_label])
				{
					add_point_as_way_to_db(attr_strings[attr_string_label], osmid_attr_value, 40, save_node_lat, save_node_lon);
					// fprintf(stderr, "POI name    : %s id=%lld\n", attr_strings[attr_string_label], osmid_attr_value);
				}
		
				if (attr_strings[attr_string_label_alt])
				{
					add_point_as_way_to_db(attr_strings[attr_string_label_alt], osmid_attr_value, 40, save_node_lat, save_node_lon);
				}
			}
		}



		if (item_is_town(*item_bin) && attr_strings[attr_string_label] && osm->towns)
		{
			item_bin = init_item(item_bin->type, 0);
			item_bin_add_coord(item_bin, &ni->c, 1);
			item_bin_add_attr_string(item_bin, attr_osm_is_in, is_in_buffer);
			item_bin_add_attr_longlong(item_bin, attr_osm_nodeid, osmid_attr_value);
			item_bin_add_attr_string(item_bin, attr_town_postal, postal);
			item_bin_add_attr_string(item_bin, attr_county_name, attr_strings[attr_string_county_name]);
			item_bin_add_attr_string(item_bin, attr_town_name, attr_strings[attr_string_label]);

			//if (attr_strings[attr_string_label_alt])
			//{
			//	item_bin_add_attr_string(item_bin, attr_town_name_match, attr_strings[attr_string_label_alt]);
			//	if (debug_itembin(item_bin))
			//	{
			//		fprintf(stderr, "town name    : %s\n", attr_strings[attr_string_label]);
			//		fprintf(stderr, "town name alt: %s\n", attr_strings[attr_string_label_alt]);
			//	}
			//}

			item_bin_write(item_bin, osm->towns);
		}

		// put POI into DB for index search
		// put town into DB for index search
		// put district into DB for index search
		// ****** retval = sqlite3_prepare_v2(sql_handle, "INSERT INTO way (id, name, town_id, lat, lon, name_fold, ind, name_fold_idx, waytype) VALUES (?,?,?,?,?,?,0,?,?);", -1, &stmt_way, NULL);


		//fprintf(stderr,"********DUMP nd1***********\n");
		//if (types[i] == type_house_number)
		//{
		//	dump_itembin(item_bin);
		//}
		//fprintf(stderr,"********DUMP nd1***********\n");

	}
	processed_nodes_out++;
	attr_longest_match_clear();
}

static struct country_table *
osm_process_town_unknown_country(void)
{
	static struct country_table *unknown;
	unknown = country_from_countryid(999);
	return unknown;
}

static struct country_table *
osm_process_item_by_country_id(int id)
{
	static struct country_table *ct;
	ct = country_from_countryid(id);
	return ct;
}

static struct country_table *
osm_process_item_fixed_country(void)
{
	static struct country_table *fixed;
	fixed = country_from_countryid(global_fixed_country_id);
	return fixed;
}

static struct country_table *
osm_process_town_by_is_in(struct item_bin *ib, char *is_in)
{
	struct country_table *result = NULL, *lookup;
	char *tok, *dup = g_strdup(is_in), *buf = dup;
	int conflict;

	while ((tok = strtok(buf, ",;")))
	{
		while (*tok == ' ')
		{
			tok++;
		}
		lookup = g_hash_table_lookup(country_table_hash, tok);
		if (lookup)
		{
			if (result && result->countryid != lookup->countryid)
			{
				//char *label = item_bin_get_attr(ib, attr_town_name, NULL);
				//osm_warning("node", item_bin_get_nodeid(ib), 0, "conflict for %s is_in=%s country %d vs %d\n", label, is_in, lookup->countryid, result->countryid);
				conflict = 1;
			}
			result = lookup;
		}
		buf = NULL;
	}
	g_free(dup);
	return result;
}

static int osm_process_street_by_boundary(GList *bl, long long town_osm_id, long long town_relation_id, struct coord *c)
{
	GList *matches = NULL;

	matches = boundary_find_matches_single(bl, c);
	if (g_list_length(matches) > 0)
	{
			return 1;
	}

	return 0;
}


static struct country_table* osm_process_town_by_manual_country_borders(GList *bl_manual, struct coord *c)
{
	GList *l = bl_manual;
	while (l)
	{
		if (l)
		{
			struct boundary_manual *b = l->data;
			if (b)
			{
				if (bbox_contains_coord(&b->r, c) == 1)
				{
					if (geom_poly_point_inside(b->c, b->coord_count, c) == 1)
					{
						// ok, now check all innery polys of this country id
						if (osm_check_all_inner_polys_of_country_id(b->countryid, c) == 1)
						{
							return b->country;
						}
					}
				}
			}
			l = g_list_next(l);
		}
	}

	return NULL;
}

// return: 1 -> coord is not in ANY inner of this country id
//         0 -> coord is at least in 1 inner of this country id
static int osm_check_all_inner_polys_of_country_id(int country_id, struct coord *c)
{
	GList *l = boundary_list_inner_manual;

	while (l)
	{
		if (l)
		{
			struct boundary_manual *b = l->data;
			if (b)
			{
				if (b->countryid == country_id)
				{
					if (bbox_contains_coord(&b->r, c) == 1)
					{
						if (geom_poly_point_inside(b->c, b->coord_count, c) == 1)
						{
							// c is inside 1 inner poly
							return 0;
						}
					}
				}
			}
			l = g_list_next(l);
		}
	}

	return 1;
}


static long long osm_process_street_by_manual_country_borders(GList *bl_manual, struct coord *c)
{
	GList *l = bl_manual;

	while (l)
	{
		if (l)
		{
			struct boundary_manual *b = l->data;
			if (b)
			{
				if (bbox_contains_coord(&b->r, c) == 1)
				{
					if (geom_poly_point_inside(b->c, b->coord_count, c) == 1)
					{
						// ok, now check all innery polys of this country id
						if (osm_check_all_inner_polys_of_country_id(b->countryid, c) == 1)
						{
							return b->town_id;
						}
					}
				}
			}
			l = g_list_next(l);
		}
	}

	return -1;
}

static long long osm_process_town_by_manual_country_id(GList *bl_manual, int country_id)
{
	struct country_table4
	{
		int countryid;
		char *names;
		char *admin_levels;
		FILE *file;
		int size;
		struct rect r;
	};

	struct country_table4 *c4; 
	struct boundary_manual *b;

	GList *l = bl_manual;
	while (l)
	{
		if (l)
		{
			b = l->data;
			if (b)
			{
				c4 = b->country;
				if (c4)
				{
					if (c4->countryid == country_id)
					{
						return b->town_id;
					}
				}
			}
			l = g_list_next(l);
		}
	}

	return -1;
}

static struct country_table *
osm_process_town_by_boundary(GList *bl, struct item_bin *ib, struct coord *c, struct attr *attrs)
{
	GList *l, *matches = boundary_find_matches(bl, c);
	struct boundary *match = NULL;

	//fprintf(stderr,"town_by_boundary:001\n");

	l = matches;
	while (l)
	{
		//fprintf(stderr,"town_by_boundary:002\n");
		struct boundary *b = l->data;
		if (b->country)
		{
			//fprintf(stderr,"town_by_boundary:003\n");
			//if (match)
			//{
			//osm_warning("node", item_bin_get_nodeid(ib), 0, "node (x=0x%x,y=0x%x) conflict country ", c->x, c->y);
			//osm_warning("relation", boundary_relid(match), 1, "country %d vs ", match->country->countryid);
			//osm_warning("relation", boundary_relid(b), 1, "country %d\n", b->country->countryid);
			//}
			match = b;
		}
		l = g_list_next(l);
	}

	//fprintf(stderr,"town_by_boundary:004\n");

	if (match)
	{
		//fprintf(stderr,"town_by_boundary:005\n");
		if (match && match->country && match->country->admin_levels)
		{
			//fprintf(stderr,"town_by_boundary:006\n");
			l = matches;
			while (l)
			{
				//fprintf(stderr,"town_by_boundary:007\n");

				struct boundary *b = l->data;
				char *admin_level = osm_tag_value(b->ib, "admin_level");
				char *postal = osm_tag_value(b->ib, "postal_code");
				if (admin_level)
				{
					//fprintf(stderr,"town_by_boundary:008\n");

					int a = atoi(admin_level);
					int end = strlen(match->country->admin_levels) + 3;
					char *name;
					if (a > 2 && a < end)
					{
						//fprintf(stderr,"town_by_boundary:009\n");

						enum attr_type attr_type = attr_none;
						switch (match->country->admin_levels[a - 3])
						{
							case 's':
								attr_type = attr_state_name;
								break;
							case 'c':
								attr_type = attr_county_name;
								break;
							case 'm':
								attr_type = attr_municipality_name;
								break;
						}
						name = osm_tag_value(b->ib, "name");
						if (name && attr_type != attr_none)
						{
							//fprintf(stderr,"town_by_boundary:010\n");
							attrs[a - 2].type = attr_type;
							attrs[a - 2].u.str = name;
						}
					}
				}
				if (postal)
				{
					//fprintf(stderr,"town_by_boundary:011\n");
					attrs[0].type = attr_town_postal;
					attrs[0].u.str = postal;
				}
				l = g_list_next(l);
			}
		}
		g_list_free(matches);
		return match->country;
	}
	else
	{
		//fprintf(stderr,"town_by_boundary:099\n");
		g_list_free(matches);
		return NULL;
	}
}

int town_size_estimate(int type)
{
	int size = 0; // default -> not use this area as a town!

	switch (type)
	{
		case type_town_label_1e7:
		case type_town_label_5e6:
		case type_town_label_2e6:
		case type_town_label_1e6:
		case type_town_label_5e5:
		case type_town_label_2e5:
			size = 10000;
			break;
		case type_town_label_1e5:
		case type_town_label_5e4:
		case type_town_label_2e4:
			size = 3100;
			break;
		case type_town_label_1e4:
		case type_town_label_5e3:
		case type_town_label_2e3:
			size = 1460;
			break;
		case type_town_label_1e3:
		case type_town_label_5e2:
		case type_town_label_2e2:
		case type_town_label_1e2:
		case type_town_label_5e1:
		case type_town_label_2e1:
		case type_town_label_1e1:
		case type_town_label_5e0:
		case type_town_label_2e0:
		case type_town_label_1e0:
		case type_town_label_0e0:
			size = 660;
			break;
		default:
			break;
	}

	// size = size * global_search_street_size_factor;

	// lat factor 0.072
	// lon factor 0.101

	// size = 10000
	// lat lon size = size / 100000

	/*
	 sel->u.c_rect.lu.x = c->x - size;
	 sel->u.c_rect.lu.y = c->y + size;
	 sel->u.c_rect.rl.x = c->x + size;
	 sel->u.c_rect.rl.y = c->y - size;
	 */
	return size;
}

void assign_town_to_streets_v1();
void assign_town_to_streets_by_boundary(GList *bl, GList *man_borders);
void assign_town_to_country_by_manual_borders(GList *bl, GList *man_borders);
void copy_town_border_data();
void assign_streets_to_towns_by_manual_country_borders(GList *man_borders);

void assign_town_to_streets(GList *bl, GList *man_borders)
{
	int skipped = 0;

	copy_town_border_data();
	assign_town_to_country_by_manual_borders(bl, man_borders);
	assign_town_to_streets_by_boundary(bl, man_borders);

	// now process all the left over towns
	assign_town_to_streets_v1(1);
	assign_town_to_streets_v1(2); // make townsize double on second pass

	assign_streets_to_towns_by_manual_country_borders(man_borders);
}

void purge_unused_towns()
{
	int retval8;
	sqlite3_stmt *stmt_mm_1;

	sql_counter = 0;
	retval8 = sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
	fprintf_(stderr, "prep:%d (error here is ok!)\n", retval8);

	retval8 = sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
	fprintf_(stderr, "prep:%d\n", retval8);

	retval8 = sqlite3_prepare_v2(sql_handle, "select count(*) from town where id not in (select town_id from way);", -1, &stmt_mm_1, NULL);
	fprintf_(stderr, "prep:%d\n", retval8);
	sqlite3_step(stmt_mm_1);
	fprintf(stderr, "purge_unused_towns:%lld\n", sqlite3_column_int64(stmt_mm_1, 0));
	sqlite3_reset(stmt_mm_1);

	// SQL:fin:
	retval8 = sqlite3_finalize(stmt_mm_1);
	fprintf_(stderr, "fin:%d\n", retval8);

	retval8 = sqlite3_exec(sql_handle, "delete from town where id not in (select town_id from way);", 0, 0, 0);
	fprintf_(stderr, "prep:%d\n", retval8);

	sql_counter = 0;
	retval8 = sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
	fprintf_(stderr, "prep:%d\n", retval8);
}

void assign_town_to_country_by_manual_borders(GList *bl, GList *man_borders)
{
	long long nd;
	long long wid;
	long long town_rel_id;
	int size;
	double lat;
	double lon;
	double temp;
	double lat_min;
	double lat_max;
	double lon_min;
	double lon_max;
	int rc;
	int rc2;
	long long town_count;
	long long town_processed_count;
	int town_steps = 1;
	int result = 0;
	struct boundary *bound_temp;
	int retval8;
	sqlite3_stmt *stmt_mm_2;
	struct coord c;
	long long dummy_town_id2 = 0;
	int commit_after = 20000;
	int count = 0;


	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	// number of towns in DB
	sqlite3_reset(stmt_town_sel002);
	sqlite3_step(stmt_town_sel002);
	town_count = sqlite3_column_int64(stmt_town_sel002, 0);
	town_processed_count = 0;

	fprintf(stderr, "towns0a: %lld/%lld\n", town_processed_count, town_count);

	if (town_count == 0)
	{
		sqlite3_reset(stmt_town_sel002);
		return;
	}

	retval8 = sqlite3_prepare_v2(sql_handle, "update town set border_id = ? where id = ?;", -1, &stmt_mm_2, NULL);
	fprintf_(stderr, "prep:%d\n", retval8);


	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	if (town_count > 1000000)
	{
		town_steps = 500;
	}
	else if (town_count > 100000)
	{
		town_steps = 100;
	}
	else if (town_count > 10000)
	{
		town_steps = 10;
	}

	// DEBUG
	// town_steps = 1;
	// DEBUG

	// loop thru all the towns
	do
	{
		rc = sqlite3_step(stmt_town_sel001);
		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:
				nd = sqlite3_column_int64(stmt_town_sel001, 0);
				town_rel_id = sqlite3_column_int64(stmt_town_sel001, 5);
				lat = sqlite3_column_double(stmt_town_sel001, 2);
				lon = sqlite3_column_double(stmt_town_sel001, 3);

				if (town_rel_id == -1)
				{
					if ((lat != 999) && (lon != 999))
					{
						c.x = transform_from_geo_lon(lon);
						c.y = transform_from_geo_lat(lat);
						// check if c is inside manual country border
						dummy_town_id2 = osm_process_street_by_manual_country_borders(man_borders, &c);

						if (dummy_town_id2 != -1)
						{
							//fprintf(stderr, "== town_to_country_by_manual_borders: townid=%lld\n", nd);
							sqlite3_bind_int64(stmt_mm_2, 1, dummy_town_id2); // parent country rel id
							sqlite3_bind_int64(stmt_mm_2, 2, nd); // town id
							sqlite3_step(stmt_mm_2);
							sqlite3_reset(stmt_mm_2);
						}

						if (count == 0)
						{
							sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
						}
						count++;

						if (count > commit_after)
						{
							sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
							count = 0;
						}
					}
				}
				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_town_sel001);

	// SQL:fin:
	rc = sqlite3_finalize(stmt_mm_2);
	fprintf_(stderr, "fin:%d\n", rc);

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);


}


void save_manual_country_borders_to_db(GList *man_borders)
{
	GList *l = man_borders;
	while (l)
	{
		struct boundary_manual *b = l->data;

		// also save dummy id to border list, to be able to compare it later
		b->town_id = dummy_town_id;

		sqlite3_bind_int64(stmt_town, 1, dummy_town_id); // town id
		sqlite3_bind_int(stmt_town, 2, b->country->countryid); // country id
		sqlite3_bind_text(stmt_town, 3, b->country->names, -1, SQLITE_STATIC); // name
		sqlite3_bind_int(stmt_town, 4, (TOWN_ADMIN_LEVEL_CORR_BASE + 2) * TOWN_BY_BOUNDARY_SIZE_FACTOR); // town size
		sqlite3_bind_text(stmt_town, 5, NULL, -1, SQLITE_STATIC); // postal

		sqlite3_bind_double(stmt_town, 6, 999); // lat -> 999:exclude
		sqlite3_bind_double(stmt_town, 7, 999); // lon -> 999:exclude

		sqlite3_bind_int64(stmt_town, 8, -1); // border relation id
		sqlite3_step(stmt_town);
		sqlite3_reset(stmt_town);
		dummy_town_id--;

		sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

		l = g_list_next(l);
	}
}

void assign_streets_to_towns_by_manual_country_borders(GList *man_borders)
{
	long long nd;
	int size;
	double lat;
	double lon;
	double lat_min;
	double lat_max;
	double lon_min;
	double lon_max;
	double temp;
	int rc;
	sqlite3_stmt *stmt_mm_1;
	sqlite3_stmt *stmt_mm_2;
	int retval9;
	int retval8;
	int street_steps;
	long long streets_processed_count;
	long long wid;
	struct coord c;
	char *wname;

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);


	retval9 = sqlite3_prepare_v2(sql_handle, "select w.lat, w.lon, w.id, w.name from way w where w.town_id='-1';", -1, &stmt_mm_1, NULL);
	fprintf_(stderr, "prep:%d\n", retval9);

	retval8 = sqlite3_prepare_v2(sql_handle, "update way set town_id = ? where id = ?;", -1, &stmt_mm_2, NULL);
	fprintf_(stderr, "prep:%d\n", retval8);

	street_steps = 1000; // set to better value!!
	streets_processed_count = 0;

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	// loop thru all the ways
	do
	{
		rc = sqlite3_step(stmt_mm_1);
		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:
				lat = sqlite3_column_double(stmt_mm_1, 0);
				lon = sqlite3_column_double(stmt_mm_1, 1);
				wid = sqlite3_column_int64(stmt_mm_1, 2);
				wname = g_strdup_printf("%s", sqlite3_column_text(stmt_mm_1, 3));

				// now update the ways
				if (sql_counter == 0)
				{
					sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
				}

				c.x = transform_from_geo_lon(lon);
				c.y = transform_from_geo_lat(lat);
				nd = osm_process_street_by_manual_country_borders(man_borders, &c);

				streets_processed_count++;
				if ((streets_processed_count % street_steps) == 0)
				{
					fprintf_(stderr, "streets0: %lld\n", streets_processed_count);
				}

				if (nd != -1)
				{
					// fprintf(stderr, "== streets by manual_country_borders: way=%s townid=%lld\n", wname, nd);

					sqlite3_bind_int64(stmt_mm_2, 1, nd);
					sqlite3_bind_int64(stmt_mm_2, 2, wid);
					sqlite3_step(stmt_mm_2);
					sqlite3_reset(stmt_mm_2);

					sql_counter++;
					if (sql_counter > MAX_ROWS_WO_COMMIT_2a)
					{
						sql_counter = 0;
						sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
					}
				}

				if (wname)
				{
					g_free(wname);
				}

				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_mm_1);

	// SQL:fin:
	rc = sqlite3_finalize(stmt_mm_1);
	fprintf_(stderr, "fin:%d\n", rc);

	// SQL:fin:
	rc = sqlite3_finalize(stmt_mm_2);
	fprintf_(stderr, "fin:%d\n", rc);

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

}


void copy_town_border_data()
{
	long long bid;
	long long tid;
	int retval;
	int rc = 0;
	int commit_after = 20000;
	int count = 0;
	int admin_l = 99;

	sqlite3_stmt *stmt_d_1;
	sqlite3_stmt *stmt_d_2;
	retval = sqlite3_prepare_v2(sql_handle, "select border_id, id, admin_level from town2;", -1, &stmt_d_1, NULL);
	// fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle, "update town set border_id = ?, size = ? where id = ?;", -1, &stmt_d_2, NULL);
	// fprintf(stderr, "prep:%d\n", retval);

	// loop thru all the towns
	do
	{
		rc = sqlite3_step(stmt_d_1);
		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:
				bid = sqlite3_column_int64(stmt_d_1, 0);
				tid = sqlite3_column_int64(stmt_d_1, 1);
				admin_l = sqlite3_column_int(stmt_d_1, 2);

				// fprintf(stderr, "bid=%lld tid=%lld amdin_level=%d\n", bid, tid, admin_l);

				if (count == 0)
				{
					sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
				}
				count++;

				sqlite3_bind_int64(stmt_d_2, 1, bid);
				sqlite3_bind_int64(stmt_d_2, 2, tid);
				sqlite3_bind_int(stmt_d_2, 3, (TOWN_ADMIN_LEVEL_CORR_BASE + admin_l) * TOWN_BY_BOUNDARY_SIZE_FACTOR);
				sqlite3_step(stmt_d_2);
				sqlite3_reset(stmt_d_2);

				if (count > commit_after)
				{
					sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
					count = 0;
				}

				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_d_1);

	retval = sqlite3_finalize(stmt_d_2);
	// fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_d_1);
	// fprintf(stderr, "fin:%d\n", retval);

	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

}

void assign_town_to_streets_by_boundary(GList *bl, GList *man_borders)
{
	long long nd;
	long long wid;
	long long town_rel_id;
	int size;
	double lat;
	double lon;
	double temp;
	double lat_min;
	double lat_max;
	double lon_min;
	double lon_max;
	int rc;
	int rc2;
	long long town_count;
	long long town_processed_count;
	int town_steps = 1;
	int result = 0;
	struct boundary *bound_temp;

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	// number of towns in DB
	sqlite3_reset(stmt_town_sel002);
	sqlite3_step(stmt_town_sel002);
	town_count = sqlite3_column_int64(stmt_town_sel002, 0);
	town_processed_count = 0;

	fprintf(stderr, "towns0b: %lld/%lld\n", town_processed_count, town_count);

	if (town_count == 0)
	{
		sqlite3_reset(stmt_town_sel002);
		return;
	}

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	if (town_count > 1000000)
	{
		town_steps = 500;
	}
	else if (town_count > 100000)
	{
		town_steps = 100;
	}
	else if (town_count > 10000)
	{
		town_steps = 10;
	}

	// DEBUG
	// town_steps = 1;
	// DEBUG

	// loop thru all the towns
	do
	{
		rc = sqlite3_step(stmt_town_sel001);
		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:
				nd = sqlite3_column_int64(stmt_town_sel001, 0);
				town_rel_id = sqlite3_column_int64(stmt_town_sel001, 5);
				//size = sqlite3_column_int(stmt_town_sel001, 1);
				//lat = sqlite3_column_double(stmt_town_sel001, 2);
				//lon = sqlite3_column_double(stmt_town_sel001, 3);


				GList *match_town = NULL;
				GList *l = bl;
				int has_found = 0;

				if ((town_rel_id != 0) && (town_rel_id != -1))
				{
					//fprintf(stderr ,"processing town name:%s id:%lld border_id:%lld\n", sqlite3_column_text(stmt_town_sel001, 4), nd, town_rel_id);

					while (l)
					{
						struct boundary *b = l->data;
						//fprintf(stderr, "bid=%lld tid=%lld\n", item_bin_get_relationid(b->ib), town_rel_id);
						if (item_bin_get_relationid(b->ib) == town_rel_id)
						{
							match_town = l;
							has_found = 1;
							//fprintf(stderr, "bid=%lld tid=%lld\n", item_bin_get_relationid(b->ib), town_rel_id);
							//fprintf(stderr, "*** town border found***\n");
							break;
						}
						l = g_list_next(l);
					}
				}

				if (has_found == 1)
				{
					// now run thru all the ways
					do
					{
						// only select ways that are in the bounding box of boundary!

						bound_temp = match_town->data;
						lat_min = transform_to_geo_lat(bound_temp->r.l.y);
						lat_max = transform_to_geo_lat(bound_temp->r.h.y);
						lon_min = transform_to_geo_lon(bound_temp->r.l.x);
						lon_max = transform_to_geo_lon(bound_temp->r.h.x);

						//fprintf(stderr, "lat min:%f max:%f lon min:%f max:%f \n", lat_min, lat_max, lon_min, lon_max);

						sqlite3_bind_double(stmt_way3a, 1, lat_min);
						sqlite3_bind_double(stmt_way3a, 2, lat_max);
						sqlite3_bind_double(stmt_way3a, 3, lon_min);
						sqlite3_bind_double(stmt_way3a, 4, lon_max);
						rc2 = sqlite3_step(stmt_way3a);
						switch (rc2)
						{
							case SQLITE_DONE:
								break;
							case SQLITE_ROW:
								wid = sqlite3_column_int64(stmt_way3a, 0);
								lat = sqlite3_column_double(stmt_way3a, 1);
								lon = sqlite3_column_double(stmt_way3a, 2);

								struct coord c;
								c.x = transform_from_geo_lon(lon);
								c.y = transform_from_geo_lat(lat);

								//fprintf(stderr,"== street by boundary == x:%d y:%d name:%s ==\n", c.x, c.y, sqlite3_column_text(stmt_way3a, 3));

								if (town_rel_id != 0)
								{
									//fprintf(stderr, "town:%lld\n", nd);
									//fprintf(stderr,"== street by boundary == x:%d y:%d ==\n", c.x, c.y);
									result = osm_process_street_by_boundary(match_town, nd, town_rel_id, &c);
									if (result == 1)
									{
										//fprintf(stderr,"== street by boundary == wid:%lld townid:%lld ==\n", wid, nd);
										// write "town id" to "way"
										if (sql_counter == 0)
										{
											sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
										}
										sql_counter++;
										sqlite3_bind_int64(stmt_way3b, 1, nd);
										sqlite3_bind_int64(stmt_way3b, 2, wid);
										sqlite3_step(stmt_way3b);
										sqlite3_reset(stmt_way3b);
										if (sql_counter > MAX_ROWS_WO_COMMIT_2b)
										{
											sql_counter = 0;
											sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
										}
									}
								}

								break;
							default:
								fprintf(stderr, "SQL Error: %d\n", rc);
								break;
						}
					}
					while (rc2 == SQLITE_ROW);
					sqlite3_reset(stmt_way3a);


					// mark town as "done"
					if (sql_counter == 0)
					{
						sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
					}
					sql_counter++;
					sqlite3_bind_int64(stmt_town_sel007, 1, nd);
					sqlite3_step(stmt_town_sel007);
					sqlite3_reset(stmt_town_sel007);
					if (sql_counter > MAX_ROWS_WO_COMMIT_2b)
					{
						sql_counter = 0;
						sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
					}

				}

				town_processed_count++;
				if ((town_processed_count % town_steps) == 0)
				{
					fprintf_(stderr, "towns0b: %lld/%lld\n", town_processed_count, town_count);
				}

				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_town_sel001);

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
}


void assign_town_to_streets_v1(int pass_num)
{

	long long nd;
	int size;
	double lat;
	double lon;
	double lat_min;
	double lat_max;
	double lon_min;
	double lon_max;
	double temp;
	int rc;
	long long town_count;
	long long town_processed_count;
	int town_steps = 1;

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	// number of towns in DB
	sqlite3_reset(stmt_town_sel002);
	sqlite3_step(stmt_town_sel002);
	town_count = sqlite3_column_int64(stmt_town_sel002, 0);
	town_processed_count = 0;

	fprintf(stderr, "towns1-%d: %lld/%lld\n", pass_num, town_processed_count, town_count);

	if (town_count == 0)
	{
		sqlite3_reset(stmt_town_sel002);
		return;
	}

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

	if (town_count > 1000000)
	{
		town_steps = 500;
	}
	else if (town_count > 100000)
	{
		town_steps = 100;
	}
	else if (town_count > 10000)
	{
		town_steps = 10;
	}

	// DEBUG
	// town_steps = 1;
	// DEBUG

	// loop thru all the towns
	do
	{
		rc = sqlite3_step(stmt_town_sel001);
		switch (rc)
		{
			case SQLITE_DONE:
				break;
			case SQLITE_ROW:
				nd = sqlite3_column_int64(stmt_town_sel001, 0);
				size = sqlite3_column_int(stmt_town_sel001, 1);
				lat = sqlite3_column_double(stmt_town_sel001, 2);
				lon = sqlite3_column_double(stmt_town_sel001, 3);

				if ((lat == 999) && (lon == 999))
				{
					// this should be excluded!!
				}
				else
				{
					//fprintf(stderr ,"processing (size:%d) town name:%s id:%lld\n", size, sqlite3_column_text(stmt_town_sel001, 4), nd);

					// now update the ways
					if (sql_counter == 0)
					{
						sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
					}

					sqlite3_bind_int64(stmt_way3, 1, nd);

					if ((pass_num == 2) && (size > 0))
					{
						size = size * 3;
					}

					temp = ((double) size) / 100000;
					lat_min = lat - temp;
					lat_max = lat + temp;
					lon_min = lon - temp;
					lon_max = lon + temp;
					sqlite3_bind_double(stmt_way3, 2, lat_min);
					sqlite3_bind_double(stmt_way3, 3, lat_max);
					sqlite3_bind_double(stmt_way3, 4, lon_min);
					sqlite3_bind_double(stmt_way3, 5, lon_max);
					//fprintf(stderr, "size korr:%f %f %f %f %f\n", temp, lat_min, lat_max, lon_min, lon_max);

					sqlite3_step(stmt_way3);
					sqlite3_reset(stmt_way3);

					sql_counter++;
					if (sql_counter > MAX_ROWS_WO_COMMIT_2a)
					{
						sql_counter = 0;
						sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
					}
					town_processed_count++;

					if ((town_processed_count % town_steps) == 0)
					{
						fprintf_(stderr, "towns1-%d: %lld/%lld\n", pass_num, town_processed_count, town_count);
					}
				}

				break;
			default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_town_sel001);

	sql_counter = 0;
	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

}


int lat_lon_inside_rect(struct node_lat_lon *n, struct rect_lat_lon *r)
{
	if ((n->lat >= r->lu_lat) && (n->lon >= r->lu_lon) && (n->lat <= r->rl_lat) && (n->lon <= r->rl_lon))
	{
		return 1;
	}

	return 0;
}

GList* osm_process_towns(FILE *in, FILE *coords, FILE *boundaries, FILE *ways, GList *bl_manual)
{
	struct item_bin *ib = NULL;
	GList *bl = NULL;
	struct attr attrs[10];
	time_t start_tt, end_tt;
	double diff_tt;
	double diff2_tt;
	long long size_in;
	long long pos_in;
	struct node_lat_lon node_coords;
	FILE *file_coords_for_map = NULL;
	FILE *file_coords_for_map2 = NULL;

	if (debug_itembin(1))
	{
		fprintf(stderr, "osm_process_towns == START ==\n");
	}

	if (!global_less_verbose)
	{
		file_coords_for_map = fopen("towns_no_country.coords.txt", "wb");
		fprintf(file_coords_for_map, "-175.0|85.0|-175_85\n");
		fprintf(file_coords_for_map, "175|85.0|175_85\n");
		fprintf(file_coords_for_map, "-175.0|-85.0|-175_-85\n");
		fprintf(file_coords_for_map, "175.0|-85.0|175_-85\n");

		file_coords_for_map2 = fopen("towns_no_country.coords_names.txt", "wb");
		fprintf(file_coords_for_map2, "-175.0|85.0|-175_85\n");
		fprintf(file_coords_for_map2, "175|85.0|175_85\n");
		fprintf(file_coords_for_map2, "-175.0|-85.0|-175_-85\n");
		fprintf(file_coords_for_map2, "175.0|-85.0|175_-85\n");
	}

	//fprintf(stderr,"osm_process_towns == PB 001 %p,%p ==\n", boundaries, ways);

	time(&start_tt);
	bl = process_boundaries(boundaries, coords, ways);
	time(&end_tt);
	diff_tt = difftime(end_tt, start_tt);
	char outstring[200];
	char outstring2[200];
	char outstring3[200];
	convert_to_human_time(diff_tt, outstring);
	fprintf(stderr, "-RUNTIME-BOUNDARIES: %s\n", outstring);

	int _c = 0;
	int _e = 10000;

	//fprintf(stderr,"osm_process_towns == PB 002 ==\n");

	long long pos_now = ftello(in); // 64bit
	fseeko(in, 0, SEEK_END);
	size_in = ftello(in); // 64bit
	fseeko(in, (off_t)pos_now, SEEK_SET);

	// reset timer
	diff2_tt = 0;
	_c = 0;
	time(&start_tt);

	while ((ib = read_item(in, 0)))
	{
		struct coord *c = (struct coord *) (ib + 1);
		struct country_table *result = NULL;
		char *is_in = item_bin_get_attr(ib, attr_osm_is_in, NULL);
		int i;

		_c++;

		if (debug_itembin(ib))
		{
			fprintf(stderr, "== item ==\n");
			dump_itembin(ib);
		}

		memset(attrs, 0, sizeof(attrs));
		if (debug_itembin(ib))
		{
			fprintf(stderr, "== osm_process_town_by_boundary ==\n");
		}

		if (use_global_fixed_country_id == 1)
		{
			result = osm_process_item_fixed_country();
			if (debug_itembin(ib))
			{
				if (result == NULL)
				{
					fprintf(stderr, "== osm_process_item_fixed_country == #NULL# ==\n");
				}
				else
				{
					fprintf(stderr, "== osm_process_item_fixed_country == %d %s ==\n", result->countryid, result->names);
				}
			}
		}

		if (!result)
		{
			result = osm_process_town_by_boundary(bl, ib, c, attrs);
			if (result) // DEBUG
			{
				if (debug_itembin(ib))
				{
					char *name=item_bin_get_attr(ib, attr_town_name, NULL);
					//fprintf(stderr,"== town by boundary == t:%s ==\n", name);
					//fprintf(stderr,"== town by boundary == country_id:%d country_name:%s townname:%s ==\n", result->countryid, result->names, name);
				}
			}
		}

		if (!result)
		{
			if (debug_itembin(ib))
			{
				fprintf(stderr, "== osm_process_town_by_is_in == %s ==\n", is_in);
			}
			result = osm_process_town_by_is_in(ib, is_in);
			if (result) // DEBUG
			{
				// fprintf(stderr,"== town by is_in == country_id:%d country_name:%s ==\n", result->countryid, result->names);
			}
		}


		// ok as a last resort check in manual country borders
		if (!result)
		{
			result = osm_process_town_by_manual_country_borders(bl_manual, c);
			if (result)
			{
				if (debug_itembin(ib))
				{
					char *name=item_bin_get_attr(ib, attr_town_name, NULL);
					fprintf(stderr,"== town by manual_country_borders == country_id:%d country_name:%s town_name=%s ==\n", result->countryid, result->names, name);
				}
			}
		}

		if (!result && unknown_country)
		{
			if (debug_itembin(ib))
			{
				fprintf(stderr, "== osm_process_town_unknown_country ==\n");
			}
			result = osm_process_town_unknown_country();
		}

		if (result)
		{
			if (!result->file)
			{
				char *name = g_strdup_printf("country_%d.unsorted.tmp", result->countryid);
				result->file = fopen(name, "wb");

				if (debug_itembin(ib))
				{
					fprintf(stderr, "== create: country_%d.unsorted.tmp == FILEP: %p ==\n", result->countryid, result->file);
				}

				g_free(name);
			}

			// generate file to plot coords on world map -------------------
			if (result->countryid == 999)
			{
				if (!global_less_verbose)
				{
					// format: lon|lat|[townname]\n
					fprintf(file_coords_for_map, "%lf|%lf|\n", transform_to_geo_lon(c->x), transform_to_geo_lat(c->y));

					char *town_name_tmp = item_bin_get_attr(ib, attr_town_name, NULL);
					fprintf(file_coords_for_map2, "%lf|%lf|%s\n", transform_to_geo_lon(c->x), transform_to_geo_lat(c->y), town_name_tmp);
				}
			}
			// generate file to plot coords on world map -------------------

			if (result->file)
			{
				long long *nodeid;
				long long nd_id = item_bin_get_nodeid(ib);
				if (is_in)
				{
					item_bin_remove_attr(ib, is_in);
				}
				nodeid = item_bin_get_attr(ib, attr_osm_nodeid, NULL);
				if (nodeid)
				{
					item_bin_remove_attr(ib, nodeid);
				}

				char *postal = NULL;
				char *postal2 = NULL;
				//if (attrs[0].type != attr_none) // what does this line do??
				//{
				postal = item_bin_get_attr(ib, attr_town_postal, NULL);
				if (postal)
				{
					postal2 = g_strdup_printf("%s", postal);
					// --- item_bin_remove_attr(ib, postal);
					// fprintf(stderr, "town postal:%s\n", postal2);
				}
				//}

				for (i = 0; i < 10; i++)
				{
					if (attrs[i].type != attr_none)
					{
						item_bin_add_attr(ib, &attrs[i]);
					}
				}

				if (sql_counter == 0)
				{
					sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
				}

				if (nd_id)
				{
					// town size (will later be translated into radius)
					int twn_size = town_size_estimate(ib->type);

					if (twn_size > 0)
					{

						sqlite3_bind_int64(stmt_town, 1, nd_id);
						if (result->countryid)
						{
							sqlite3_bind_int(stmt_town, 2, result->countryid);
						}
						else
						{
							sqlite3_bind_int(stmt_town, 2, 999);
						}

						sqlite3_bind_int(stmt_town, 4, twn_size);

						char *label_t = item_bin_get_attr(ib, attr_town_name, NULL);
						//fprintf(stderr, "town: %lld,%d,%s\n", nd_id, result->countryid, label_t);

						sqlite3_bind_text(stmt_town, 3, label_t, -1, SQLITE_STATIC);

						if (postal2)
						{
							sqlite3_bind_text(stmt_town, 5, postal2, -1, SQLITE_STATIC);
						}
						else
						{
							sqlite3_bind_text(stmt_town, 5, NULL, -1, SQLITE_STATIC);
						}

						get_lat_lon_for_node(nd_id, &node_coords);
						if (node_coords.valid == 1)
						{
							sqlite3_bind_double(stmt_town, 6, node_coords.lat);
							sqlite3_bind_double(stmt_town, 7, node_coords.lon);
						}

						sqlite3_step(stmt_town);
						sqlite3_reset(stmt_town);
						sql_counter++;

						if (postal2)
						{
							g_free(postal2);
						}

					}
				}

				if (sql_counter > MAX_ROWS_WO_COMMIT_5)
				{
					sql_counter = 0;
					sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
					//fprintf(stderr, "SQL: COMMIT\n");
				}

				// ** used until 2011-11-06 seems bad ** item_bin_write_match(ib, attr_town_name, attr_town_name_match, result->file);
				item_bin_town_write_match(ib, attr_town_name, attr_town_name_match, result->file);
				// ** origname ** item_bin_write(ib,result->file);
			}
		}
		else
		{
			if (debug_itembin(ib))
			{
				fprintf(stderr, "== no result ==\n");
			}
		}

		if (_c > _e)
		{
			_c = 0;

			pos_in = ftello(in); // 64bit
			time(&end_tt);
			diff_tt = difftime(end_tt, start_tt);
			convert_to_human_time(diff_tt, outstring);
			convert_to_human_bytes(pos_in, outstring2);
			convert_to_human_bytes(size_in, outstring3);
			fprintf_(stderr, "-RUNTIME-LOOP-TOWN: %s elapsed (POS:%s of %s)\n", outstring, outstring2, outstring3);
			if (pos_in > 0)
			{
				double eta_time = ((diff_tt / (pos_in)) * (size_in)) - diff_tt;
				convert_to_human_time(eta_time, outstring);
				fprintf_(stderr, "-RUNTIME-LOOP-TOWN: %s left\n", outstring);
			}
		}
	}

	if (debug_itembin(1))
	{
		fprintf(stderr, "osm_process_towns == END ==\n");
	}

	if (!global_less_verbose)
	{
		fclose(file_coords_for_map);
		fclose(file_coords_for_map2);
	}

	return bl;

}

void sort_countries(int keep_tmpfiles)
{
	int i;
	struct country_table *co;
	char *name_in, *name_out;
	for (i = 0; i < sizeof(country_table) / sizeof(struct country_table); i++)
	{
		co = &country_table[i];
		if (co->file)
		{
			fclose(co->file);
			co->file = NULL;
		}
		name_in = g_strdup_printf("country_%d.unsorted.tmp", co->countryid);
		name_out = g_strdup_printf("country_%d.tmp", co->countryid);

		if (debug_itembin(2))
		{
			fprintf(stderr, "in=country_%d.unsorted.tmp\n", co->countryid);
			fprintf(stderr, "out=country_%d.tmp\n", co->countryid);
		}

		co->r = world_bbox;
		item_bin_sort_file(name_in, name_out, &co->r, &co->size);
		if (!keep_tmpfiles)
		{
			unlink(name_in);
		}
		g_free(name_in);
		g_free(name_out);
	}
}

static int search_relation_member(struct item_bin *ib, char *role, struct relation_member *memb, int *min_count)
{
	char *str = NULL;
	int count = 0;
	while ((str = item_bin_get_attr(ib, attr_osm_member, str)))
	{
		if (!get_relation_member(str, memb))
			return 0;
		count++;
		if (!strcmp(memb->role, role) && (!min_count || *min_count < count))
		{
			if (min_count)
				*min_count = count;
			return 1;
		}
	}
	return 0;
}

static int load_way_index(FILE *ways_index, int p, long long *idx)
{
	int step = sizeof(*idx) * 2;
	fseek(ways_index, p * step, SEEK_SET);
	if (fread(idx, step, 1, ways_index) != 1)
	{
		fprintf(stderr, "read failed\n");
		return 0;
	}
	return 1;
}

static int seek_to_way(FILE *way, FILE *ways_index, long long wayid)
{
	long offset;
	long long idx[2];
	int count, interval, p;
	void *p_tmp;
	int rr;
	size_t data_size2 = sizeof(int);

	fprintf_(stderr, "seek_to_way ---\n");

	if (way_hash_cfu)
	{
		//if (!(g_hash_table_lookup_extended(way_hash, (gpointer) (long) wayid, NULL, (gpointer) & offset)))
		//{
		//	return 0;
		//}

		rr = cfuhash_get_data(way_hash_cfu, (long long) wayid, sizeof(long long), &p_tmp, &data_size2);
		if (rr == 0)
		{
			return 0;
		}

		// printf("got value %ld\n", p_tmp);
		offset = p_tmp;

		fseek(way, offset, SEEK_SET);
		return 1;
	}

	fprintf(stderr, "**BAD**WAYSTUFF**\n");

	fseek(ways_index, 0, SEEK_END);
	count = ftell(ways_index) / sizeof(idx);
	interval = count / 4;
	p = count / 2;
	if (interval == 0)
	{
		// If fewer than 4 nodes defined so far set interval to 1 to
		// avoid infinite loop
		interval = 1;
	}
	if (!load_way_index(ways_index, p, idx))
		return 0;
	for (;;)
	{
		if (idx[0] == wayid)
		{
			fseek(way, idx[1], SEEK_SET);
			return 1;
		}
		if (idx[0] < wayid)
		{
			p += interval;
			if (interval == 1)
			{
				if (p >= count)
					return 0;
				if (!load_way_index(ways_index, p, idx))
					return 0;
				if (idx[0] > wayid)
					return 0;
			}
			else
			{
				if (p >= count)
					p = count - 1;
				if (!load_way_index(ways_index, p, idx))
					return 0;
			}
		}
		else
		{
			p -= interval;
			if (interval == 1)
			{
				if (p < 0)
					return 0;
				if (!load_way_index(ways_index, p, idx))
					return 0;
				if (idx[0] < wayid)
					return 0;
			}
			else
			{
				if (p < 0)
					p = 0;
				if (!load_way_index(ways_index, p, idx))
					return 0;
			}
		}
		if (interval > 1)
			interval /= 2;
	}
}

static struct coord *
get_way(FILE *way, FILE *ways_index, struct coord *c, long long wayid, struct item_bin *ret, int debug)
{
	long long currid;
	int last;
	struct coord *ic;
	if (!seek_to_way(way, ways_index, wayid))
	{
		if (debug)
		{
			fprintf(stderr, "not found in index");
		}
		return NULL;
	}

	while (item_bin_read(ret, way))
	{
		currid = item_bin_get_wayid(ret);
		if (debug)
		fprintf(stderr,LONGLONG_FMT":",currid);
		if (currid != wayid)
			return NULL;
		ic = (struct coord *) (ret + 1);
		last = ret->clen / 2 - 1;
		if (debug)
			fprintf(stderr, "(0x%x,0x%x)-(0x%x,0x%x)", ic[0].x, ic[0].y, ic[last].x, ic[last].y);
		if (!c)
			return &ic[0];
		if (ic[0].x == c->x && ic[0].y == c->y)
			return &ic[last];
		if (ic[last].x == c->x && ic[last].y == c->y)
			return &ic[0];
	}
	return NULL;
}

struct turn_restriction
{
	osmid relid;
	enum item_type type;
	struct coord *c[3];
	int c_count[3];
};

static void process_turn_restrictions_member(void *func_priv, void *relation_priv, struct item_bin *member, void *member_priv)
{
	int count, type = (long) member_priv;
	struct turn_restriction *turn_restriction = relation_priv;
	struct coord *c = (struct coord *) (member + 1);
	int ccount = member->clen / 2;

	if (member->type < type_line)
	{
		count = 1;
	}
	else
	{
		count = 2;
	}

	turn_restriction->c[type]=g_renew(struct coord, turn_restriction->c[type], turn_restriction->c_count[type]+count);
	turn_restriction->c[type][turn_restriction->c_count[type]++] = c[0];

	if (count > 1)
	{
		turn_restriction->c[type][turn_restriction->c_count[type]++] = c[ccount - 1];
	}
}

static void process_turn_restrictions_fromto(struct turn_restriction *t, int type, struct coord **c)
{
	int i, j;
	for (i = 0; i < t->c_count[type]; i += 2)
	{
		for (j = 0; j < t->c_count[1]; j++)
		{
			if (coord_is_equal(t->c[type][i], t->c[1][j]))
			{
				c[0] = &t->c[type][i + 1];
				c[1] = &t->c[type][i];
				return;
			}
			if (coord_is_equal(t->c[type][i + 1], t->c[1][j]))
			{
				c[0] = &t->c[type][i];
				c[1] = &t->c[type][i + 1];
				return;
			}
		}
	}
}

static void process_turn_restrictions_dump_coord(struct coord *c, int count)
{
	int i;
	for (i = 0; i < count; i++)
	{
		fprintf(stderr, "(0x%x,0x%x)", c[i].x, c[i].y);
	}
}

static void process_turn_restrictions_finish(GList *tr, FILE *out)
{
	GList *l = tr;
	while (l)
	{
		struct turn_restriction *t = l->data;
		struct coord *c[4];
		struct item_bin *ib = item_bin_2;

		// init array
		c[0] = NULL;
		c[1] = NULL;
		c[2] = NULL;
		c[3] = NULL;

		if (!t->c_count[0])
		{
			//osm_warning("relation",t->relid,0,"turn restriction: from member not found\n");
		}
		else if (!t->c_count[1])
		{
			//osm_warning("relation",t->relid,0,"turn restriction: via member not found\n");
		}
		else if (!t->c_count[2])
		{
			//osm_warning("relation",t->relid,0,"turn restriction: to member not found\n");
		}
		else
		{
			process_turn_restrictions_fromto(t, 0, c);
			process_turn_restrictions_fromto(t, 2, c + 2);
			if (!c[0] || !c[2])
			{
				//osm_warning("relation",t->relid,0,"turn restriction: via (");
				//process_turn_restrictions_dump_coord(t->c[1], t->c_count[1]);
				//fprintf(stderr,")");
				if (!c[0])
				{
					//fprintf(stderr," failed to connect to from (");
					//process_turn_restrictions_dump_coord(t->c[0], t->c_count[0]);
					//fprintf(stderr,")");
				}
				if (!c[2])
				{
					//fprintf(stderr," failed to connect to to (");
					//process_turn_restrictions_dump_coord(t->c[2], t->c_count[2]);
					//fprintf(stderr,")");
				}
				//fprintf(stderr,"\n");
			}
			else
			{
				if (t->c_count[1] <= 2)
				{
					item_bin_init(ib, t->type);
					item_bin_add_coord(ib, c[0], 1);
					item_bin_add_coord(ib, c[1], 1);
					if (t->c_count[1] > 1)
					{
						item_bin_add_coord(ib, c[3], 1);
					}
					item_bin_add_coord(ib, c[2], 1);
					item_bin_write(ib, out);
				}

			}
		}
		g_free(t);
		l = g_list_next(l);
	}
	g_list_free(tr);
}

static GList *
process_turn_restrictions_setup(FILE *in, struct relations *relations)
{
	struct relation_member fromm, tom, viam, tmpm;
	long long relid;
	struct item_bin *ib;
	struct relations_func *relations_func;
	int min_count;
	GList *turn_restrictions = NULL;

	fseek(in, 0, SEEK_SET);
	relations_func = relations_func_new(process_turn_restrictions_member, NULL);
	while ((ib = read_item(in, 0)))
	{
		struct turn_restriction *turn_restriction=g_new0(struct turn_restriction, 1);
		relid = item_bin_get_relationid(ib);
		turn_restriction->relid = relid;
		turn_restriction->type = ib->type;
		min_count = 0;
		if (!search_relation_member(ib, "from", &fromm, &min_count))
		{
			osm_warning("relation", relid, 0, "turn restriction: from member missing\n");
			continue;
		}
		if (search_relation_member(ib, "from", &tmpm, &min_count))
		{
			osm_warning("relation", relid, 0, "turn restriction: multiple from members\n");
			continue;
		}
		min_count = 0;
		if (!search_relation_member(ib, "to", &tom, &min_count))
		{
			osm_warning("relation", relid, 0, "turn restriction: to member missing\n");
			continue;
		}
		if (search_relation_member(ib, "to", &tmpm, &min_count))
		{
			osm_warning("relation", relid, 0, "turn restriction: multiple to members\n");
			continue;
		}
		min_count = 0;
		if (!search_relation_member(ib, "via", &viam, &min_count))
		{
			osm_warning("relation", relid, 0, "turn restriction: via member missing\n");
			continue;
		}
		if (search_relation_member(ib, "via", &tmpm, &min_count))
		{
			osm_warning("relation", relid, 0, "turn restriction: multiple via member\n");
			continue;
		}
		if (fromm.type != 2)
		{
			osm_warning("relation", relid, 0, "turn restriction: wrong type for from member ");
			osm_warning(osm_types[fromm.type], fromm.id, 1, "\n");
			continue;
		}
		if (tom.type != 2)
		{
			osm_warning("relation", relid, 0, "turn restriction: wrong type for to member ");
			osm_warning(osm_types[tom.type], tom.id, 1, "\n");
			continue;
		}
		if (viam.type != 1 && viam.type != 2)
		{
			osm_warning("relation", relid, 0, "turn restriction: wrong type for via member ");
			osm_warning(osm_types[viam.type], viam.id, 1, "\n");
			continue;
		}
		relations_add_func(relations, relations_func, turn_restriction, (gpointer) 0, fromm.type, fromm.id);
		relations_add_func(relations, relations_func, turn_restriction, (gpointer) 1, viam.type, viam.id);
		relations_add_func(relations, relations_func, turn_restriction, (gpointer) 2, tom.type, tom.id);
		turn_restrictions = g_list_append(turn_restrictions, turn_restriction);
	}
	return turn_restrictions;
}

void process_turn_restrictions(FILE *in, FILE *coords, FILE *ways, FILE *ways_index, FILE *out)
{
	struct relations *relations = relations_new();
	GList *turn_restrictions;
	fseek(in, 0, SEEK_SET);
	if (verbose_mode)
		fprintf(stderr, "process_turn_restrictions A1\n");
	turn_restrictions = process_turn_restrictions_setup(in, relations);
	if (verbose_mode)
		fprintf(stderr, "process_turn_restrictions B2\n");
	relations_process(relations, coords, ways, NULL);
	if (verbose_mode)
		fprintf(stderr, "process_turn_restrictions C3\n");
	process_turn_restrictions_finish(turn_restrictions, out);
	if (verbose_mode)
		fprintf(stderr, "process_turn_restrictions D4\n");
}

static void node_ref_way(osmid node, int local_thread_num)
{
	struct node_item *ni;
	ni = node_item_get_fast(node, local_thread_num);

	if (ni)
	{
		// fprintf(stderr, "node id=%lld ref=%d\n", node, (ni->ref_way + 1));
		ni->ref_way++;
	}
}

static void nodes_ref_item_bin(struct item_bin *ib, int local_thread_num, osmid this_way_id)
{
	int i;
	int *node_num_offset;
	int offset = 0;
	osmid way_node;
	struct coord *c = (struct coord *) (ib + 1); // set pointer to coord struct of this item

	for (i = 0; i < ib->clen / 2; i++)
	{
		// fprintf(stderr, "1     i=%d this_way_id=%lld\n", i, this_way_id);

		// offset for split cycleways!
		node_num_offset = item_bin_get_attr(ib, attr_debugsplitway, NULL);

		if (node_num_offset)
		{
			offset = (int)*node_num_offset;
		}

		way_node = get_waynode_num(this_way_id, (i + offset), local_thread_num);
		// fprintf(stderr, "2 ref2=%lld this_way_id=%lld offset=%d\n", way_node, this_way_id, offset);
		node_ref_way(way_node, local_thread_num);
		// fprintf(stderr, "3 ref2=%lld this_way_id=%lld\n", way_node, this_way_id);
	}

	//fprintf(stderr,"********DUMP rw ***********\n");
	// dump_itembin(ib);
	//fprintf(stderr,"********DUMP rw ***********\n");
}

osmid get_waynode_num(osmid way_id, int coord_num, int local_thread_num)
{
	int rc2;
	sqlite3_stmt *st;
	osmid nd;
	long long seekpos_waynode2;
#define __USE_WAYNODEFILE_CACHE__ 1

	//fprintf(stderr, "get_waynode_num:w=%lld cnum=%d\n", way_id, (coord_num + 1));

	if (last_seek_wayid[local_thread_num] != way_id)
	{

		last_seek_wayid[local_thread_num] = way_id;

#ifdef MAPTOOL_SPLIT_WAYNODE_DB
		if ((way_id & MAPTOOL_SPLIT_WAYNODE_BIT) > 0)
		{
			if ((way_id & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
			{
#endif
				st = stmt_sel0012_tt[local_thread_num];
#ifdef MAPTOOL_SPLIT_WAYNODE_DB
			}
			else
			{
				st = stmt_sel0012b_tt[local_thread_num];
			}
		}
		else
		{
			if ((way_id & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
			{
				st = stmt_sel0012__2_tt[local_thread_num];
			}
			else
			{
				st = stmt_sel0012__2b_tt[local_thread_num];
			}
		}
#endif

		sqlite3_bind_int64(st, 1, way_id);
		sqlite3_bind_int(st, 2, (int)1);

		// execute the statement
		rc2 = sqlite3_step(st);

		switch (rc2)
		{
			case SQLITE_DONE:
			break;
			case SQLITE_ROW:
			seekpos_waynode[local_thread_num] = sqlite3_column_int64(st, 1);
			break;
			default:
			fprintf(stderr, "Error: %d\n", rc2);
			break;
		}
		sqlite3_reset(st);
	}

	seekpos_waynode2 = seekpos_waynode[local_thread_num] + (sizeof(osmid) * coord_num); // seek to coord in way

	if ((! __USE_WAYNODEFILE_CACHE__) || (seekpos_waynode2 != last_seekpos_waynode[local_thread_num]))
	{
		//fprintf(stderr, "w seek3 seekpos_waynode=%lld last_seekpos_waynode=%lld\n", seekpos_waynode[local_thread_num], last_seekpos_waynode[local_thread_num]);
		fseeko(ways_ref_file_thread[local_thread_num], (off_t)seekpos_waynode2, SEEK_SET);
	}
	//else
	//{
	//	fprintf(stderr, "w seek3:NO SEEK\n");
	//}

	int fret = (int)fread(&nd, sizeof(osmid), 1, ways_ref_file_thread[local_thread_num]);
	if (fret == 0)
	{
		fprintf(stderr, "**ERROR** at fread 002b: wayid:%lld count=%d\n", way_id, coord_num);
	}
	else if ((osmid)nd > 4994968164L)
	{
		fprintf(stderr, "**ERROR** at fread 002b: wayid:%lld count=%d w_node=%lld\n", way_id, coord_num, (osmid)nd);
	}

	last_seekpos_waynode[local_thread_num] = seekpos_waynode2 + sizeof(osmid);
	//fprintf(stderr, "get_waynode_num:result node=%lld last_seekpos_waynode=%lld\n", nd, last_seekpos_waynode[local_thread_num]);

	return nd;
}


osmid get_waynode_num_have_seekpos(osmid way_id, int coord_num, int local_thread_num, off_t seek_pos)
{
	int rc2;
	sqlite3_stmt *st;
	osmid nd;
	long long seekpos_waynode2;
	#define __USE_WAYNODEFILE_CACHE__ 1

	//fprintf(stderr, "get_waynode_num:w=%lld cnum=%d\n", way_id, (coord_num + 1));

	if (last_seek_wayid[local_thread_num] != way_id)
	{
		last_seek_wayid[local_thread_num] = way_id;
	}

	seekpos_waynode[local_thread_num] = seek_pos;
	seekpos_waynode2 = seekpos_waynode[local_thread_num] + (sizeof(osmid) * coord_num); // seek to coord in way

	if ((! __USE_WAYNODEFILE_CACHE__) || (seekpos_waynode2 != last_seekpos_waynode[local_thread_num]))
	{
		//fprintf(stderr, "w seek3 seekpos_waynode=%lld last_seekpos_waynode=%lld\n", seekpos_waynode[local_thread_num], last_seekpos_waynode[local_thread_num]);
		fseeko(ways_ref_file_thread[local_thread_num], (off_t)seekpos_waynode2, SEEK_SET);
	}
	//else
	//{
	//	fprintf(stderr, "w seek3:NO SEEK\n");
	//}

	int fret = (int)fread(&nd, sizeof(osmid), 1, ways_ref_file_thread[local_thread_num]);
	if (fret == 0)
	{
		fprintf(stderr, "**ERROR** at fread 002b: wayid:%lld count=%d\n", way_id, coord_num);
	}
	else if ((osmid)nd > 4994968164L)
	{
		fprintf(stderr, "**ERROR** at fread 002b: wayid:%lld count=%d w_node=%lld\n", way_id, coord_num, (osmid)nd);
	}


	last_seekpos_waynode[local_thread_num] = seekpos_waynode2 + sizeof(osmid);
	// fprintf(stderr, "get_waynode_num:result node=%lld last_seekpos_waynode=%lld\n", nd, last_seekpos_waynode[local_thread_num]);

	return nd;
}


void add_waynode_to_db(osmid ref, int c_count)
{
	// ------- save way node to SQL db ------------
#ifdef MAPTOOL_USE_SQL

				if (sql_counter4 == 0)
				{
					sqlite3_exec(sql_handle004, "BEGIN", 0, 0, 0);
					sqlite3_exec(sql_handle005, "BEGIN", 0, 0, 0);
					sqlite3_exec(sql_handle006, "BEGIN", 0, 0, 0);
					sqlite3_exec(sql_handle007, "BEGIN", 0, 0, 0);
					//fprintf(stderr, "begin: way nodes\n");
				}
				sql_counter4++;
				//fprintf(stderr, "insert #WN:wid=%lld ccount=%d ref=%lld\n", osmid_attr_value, c_count + 1, ref);

#ifdef MAPTOOL_SPLIT_WAYNODE_DB
				if ((osmid_attr_value & MAPTOOL_SPLIT_WAYNODE_BIT) > 0)
				{
					if ((osmid_attr_value & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
					{
#endif
						sqlite3_bind_int64(stmt_way_node, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_node, 2, ref);
						sqlite3_bind_int(stmt_way_node, 3, c_count + 1);
						sqlite3_bind_int64(stmt_way_node, 4, seekpos1);
						sqlite3_step(stmt_way_node);
						sqlite3_reset(stmt_way_node);
#ifdef MAPTOOL_SPLIT_WAYNODE_DB
					}
					else
					{
						sqlite3_bind_int64(stmt_way_nodeb, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_nodeb, 2, ref);
						sqlite3_bind_int(stmt_way_nodeb, 3, c_count + 1);
						sqlite3_bind_int64(stmt_way_nodeb, 4, seekpos1);
						sqlite3_step(stmt_way_nodeb);
						sqlite3_reset(stmt_way_nodeb);
					}
				}
				else
				{
					if ((osmid_attr_value & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
					{
						sqlite3_bind_int64(stmt_way_node__2, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_node__2, 2, ref);
						sqlite3_bind_int(stmt_way_node__2, 3, c_count + 1);
						sqlite3_bind_int64(stmt_way_node__2, 4, seekpos1);
						sqlite3_step(stmt_way_node__2);
						sqlite3_reset(stmt_way_node__2);
					}
					else
					{
						sqlite3_bind_int64(stmt_way_node__2b, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_node__2b, 2, ref);
						sqlite3_bind_int(stmt_way_node__2b, 3, c_count + 1);
						sqlite3_bind_int64(stmt_way_node__2b, 4, seekpos1);
						sqlite3_step(stmt_way_node__2b);
						sqlite3_reset(stmt_way_node__2b);
					}
				}
#endif

				if (sql_counter4 > MAX_ROWS_WO_COMMIT_4)
				{
					sqlite3_exec(sql_handle004, "COMMIT", 0, 0, 0);
					sqlite3_exec(sql_handle005, "COMMIT", 0, 0, 0);
					sqlite3_exec(sql_handle006, "COMMIT", 0, 0, 0);
					sqlite3_exec(sql_handle007, "COMMIT", 0, 0, 0);
					//fprintf(stderr, "COMMIT:way nodes: %lld\n", sql_counter4);
					sql_counter4 = 0;
				}
#endif
	// ------- save way node to SQL db ------------


}

void osm_add_nd(osmid ref)
{
	// SET_REF(&coord_buffer[coord_count], ref);

	//fprintf(stderr, "osm_add_nd:result way=%lld node=%lld\n", osmid_attr_value, ref);

	// put lat, lon into &coord_buffer[coord_count] -------- START ----------
	struct coord *c = &coord_buffer[coord_count];
	c->x = REF_X;
	// c->y = REF_Y;
	c->y = coord_count; // number of this node in the way (starting at zero)
	// put lat, lon into &coord_buffer[coord_count] -------- START ----------

	if (coord_count == 0)
	{
		first_node_of_current_way = ref;
		//fprintf(stderr, "w ftell1\n");
		seekpos1 = (long long)ftello(ways_ref_file); // 64bit
	}

	//fprintf(stderr, "write:wid=%lld nd=%lld seekpos1=%lld\n", osmid_attr_value, ref, seekpos1);
	fwrite(&ref, sizeof(osmid), 1, ways_ref_file); // write way node to ref file
	//ftello(ways_ref_file); // --> sometimes this is needed!!
	//fprintf(stderr, "coord_count=%d filepos after write=%lld\n", coord_count, (long long)ff);

	coord_count++;
	if (coord_count > 65536)
	{
		fprintf(stderr, "ERROR: Overflow\n");
		exit(1);
	}
}

static void write_item_part(FILE *out, FILE *out_index, FILE *out_graph, struct item_bin *orig, int first, int last, long long *last_id)
{
	struct item_bin new;
	struct coord *c = (struct coord *) (orig + 1);
	char *attr = (char *) (c + orig->clen / 2);
	int attr_len = orig->len - orig->clen - 2;
	processed_ways++;
	new.type = orig->type;
	new.clen = (last - first + 1) * 2;
	new.len = new.clen + attr_len + 2;

	if (out_index)
	{
		long long idx[2];
		idx[0] = item_bin_get_wayid(orig);
		idx[1] = ftello(out);
		if (way_hash_cfu)
		{
			//if (!(g_hash_table_lookup_extended(way_hash, (gpointer) (long) idx[0], NULL, NULL)))
			//{
			//	g_hash_table_insert(way_hash, (gpointer) (long) idx[0], (gpointer) (long) idx[1]);
			//	// fprintf(stderr,"way_hash size="LONGLONG_FMT"\n", g_hash_table_size(way_hash));
			//}

			if (!cfuhash_exists_data(way_hash_cfu, (long long) idx[0], sizeof(long long)))
			{
				cfuhash_put_data(way_hash_cfu, (long long) idx[0], sizeof(long long), (int) idx[1], sizeof(int), NULL);
			}
		}
		else
		{
			if (!last_id || *last_id != idx[0])
			{
				fwrite(idx, sizeof(idx), 1, out_index);
			}

			if (last_id)
			{
				*last_id = idx[0];
			}
		}

	}

	fwrite(&new, sizeof(new), 1, out);
	fwrite(c + first, new.clen * 4, 1, out);
	fwrite(attr, attr_len * 4, 1, out);
}

void ref_ways(FILE *in, int local_thread_num)
{
	struct item_bin *ib;

	long long ways_count = 0;
	osmid *this_way_id;

	// ---------- reset the cached values ----------
	seekpos_waynode[local_thread_num] = -1;
	last_seekpos_waynode[local_thread_num] = -1;
	last_seek_wayid[local_thread_num] = -1;
	fseeko(ways_ref_file_thread[local_thread_num], (off_t)0, SEEK_SET);
	// ---------- reset the cached values ----------

	fseek(in, 0, SEEK_SET);
	while ((ib = read_item(in, local_thread_num))) // loop all "ways" from file "in"
	{
		this_way_id = item_bin_get_attr(ib, attr_osm_wayid, NULL);

		if (this_way_id)
		{
			nodes_ref_item_bin(ib, local_thread_num, *this_way_id);
		}
		else
		{
			// no "way id"? so we do nothing
			// nodes_ref_item_bin(ib, local_thread_num, 0);
		}
		ways_count++;

		if ((ways_count % 1000000) == 0)
		{
			fprintf_(stderr, "[THREAD] #%d ways: %lld\n", local_thread_num, ways_count);
		}
	}
}


/**
 * Get POI coordinates from line coordinates.
 * @param in *in input file with line coordinates.
 * @param in *out output file with POI coordinates
 * @param in type input file original contents type: type_line or type_area
 * @returns nothing
 */
void process_way2poi_housenumber(FILE *in, FILE *out)
{
	struct item_bin *ib;
	// char *house_number = NULL;
	// char *street_name = NULL;

	while ((ib = read_item(in, 0)))
	{
		int count = ib->clen / 2;
		if (count > 1 && ib->type == type_poly_building)
		{
			struct coord *c = (struct coord *) (ib + 1), c1, c2;
			int done = 0;
			//if (ib->type == type_area) // --> buildings are "lines" now, not polys
			//{
			if (count < 3)
			{
				osm_warning("way", item_bin_get_wayid(ib), 0, "Broken polygon, less than 3 points defined\n");
			}
			else if (!geom_poly_centroid(c, count, &c1))
			{
				osm_warning("way", item_bin_get_wayid(ib), 0, "Broken polygon, area is 0\n");
			}
			else
			{
				if (geom_poly_point_inside(c, count, &c1))
				{
					//fprintf(stderr, "geom_poly_point_inside\n");
					c[0] = c1;
				}
				else
				{
					//fprintf(stderr, "geom_poly_closest_point\n");
					geom_poly_closest_point(c, count, &c1, &c2);
					c[0] = c2;
				}
				done = 1;
			}
			//}

			if (!done)
			{
				//fprintf(stderr, "geom_line_middle\n");
				geom_line_middle(c, count, &c1);
				c[0] = c1;
			}

			char *street_name = item_bin_get_attr(ib, attr_street_name_dummy, NULL);
			char *house_number = item_bin_get_attr(ib, attr_house_number_dummy, NULL);
			if ((street_name != NULL) && (house_number != NULL))
			{
				osm_append_housenumber_node(out, c, house_number, street_name);
			}
		}
	}
}

void fill_hash_waytag(void)
{
	if (verbose_mode)
		fprintf(stderr, "fill_hash_waytag - START\n");
	if (waytag_hash)
	{
		g_hash_table_destroy(waytag_hash);
		waytag_hash = NULL;
	}
	waytag_hash = g_hash_table_new(NULL, NULL);
	waytag_buffer_to_hash();
	if (verbose_mode)
		fprintf(stderr, "fill_hash_waytag - END\n");
}

void append_pre_resolved_ways(FILE *out, struct maptool_osm *osm2)
{
	struct item_bin *ib;

	// fseek(out, 0, SEEK_END); // seek to the end of current way-file
	rewind(osm2->ways_with_coords); // start with the first "way"

	while ((ib = read_item(osm2->ways_with_coords, 0))) // loop thru all "ways" from file "in"
	{
		// write way to outfile
		item_bin_write(ib, out);
	}
}

int copy_tags_to_ways(FILE *in, FILE *out, FILE *tags_in)
{
	struct item_bin *ib;

	long long slice_size2 = sizeof(struct way_tag) * 1024 * 1024; // xx MByte
	long long final_slice_size2;
	struct way_tag *wt3;
	int slices2 = 0;
	int i;
	struct stat st;
	int final_slice = 0;
	long long size2;

	fseek(tags_in, 0, SEEK_END);
	size2 = ftello(tags_in); // 64bit

	slices2 = (size2 / slice_size2) + 1;
	final_slice_size2 = size2 - ((slices2 - 1) * slice_size2);

	if (slices2 == 1)
	{
		final_slice_size2 = size2;
	}

	if (verbose_mode) fprintf(stderr, "relationsfile size="LONGLONG_FMT" bytes\n", size2);
	if (verbose_mode) fprintf(stderr, "final_slice_size2="LONGLONG_FMT" bytes\n", final_slice_size2);
	if (verbose_mode)
		fprintf(stderr, "slices=%d\n", slices2);

	for (i = 0; i < slices2; i++)
	{
		if (i == (slices2 - 1))
		{
			final_slice = 1;
		}

		if (final_slice == 1)
		{
			load_buffer_fp(tags_in, &waytag_buffer, i * slice_size2, final_slice_size2); // load in the relations
		}
		else
		{
			load_buffer_fp(tags_in, &waytag_buffer, i * slice_size2, slice_size2); // load in the relations
		}
		fill_hash_waytag();
		struct way_tag *wt2 = (struct way_tag *) (waytag_buffer.base);

		rewind(in); // start with the first "way"
		while ((ib = read_item(in, 0))) // loop thru all "ways" from file "in"
		{
			if (waytag_hash)
			{
				int tag_start_pointer;
				long long wid = item_bin_get_wayid(ib);

				//fprintf(stderr,"looking for wid=%lu\n", wid);

				long long p_tmp = 0;
				int ret = 0;
				ret = g_hash_table_lookup_extended(waytag_hash, (gpointer) (long long) wid, NULL, (gpointer) & p_tmp);
				//fprintf(stderr,"ret=%d\n", ret);
				if (ret == 1)
				{
					// only if wayid is found in hash!
					tag_start_pointer = (int) (long long) (p_tmp);
					wt3 = wt2 + tag_start_pointer;

					if ((long long) wt3->way_id == (long long) wid)
					{
						if (wt3->tag_id == 1)
						{
							// add "type" to "way"
							// ## char tag[strlen("waterway") + strlen("riverbank") + 2];
							// ## sprintf(tag, "%s=%s", "waterway", "riverbank");
							// ## item_bin_add_attr_string(ib, attr_osm_tag, tag);
							if (item_bin_is_closed_poly(ib) == 1)
							{
								item_bin_set_type(ib, type_poly_water_from_relations);
							}
							else
							{
								item_bin_set_type(ib, type_water_river);
							}
						}
						else if (wt3->tag_id == 2)
						{
							if (item_bin_is_closed_poly(ib) == 1)
							{
								item_bin_set_type(ib, type_poly_water_from_relations);
							}
							else
							{
								item_bin_set_type(ib, type_water_river);
							}
						}
						else if (wt3->tag_id == 4)
						{
							if (item_bin_is_closed_poly(ib) == 1)
							{
								item_bin_set_type(ib, type_poly_water_from_relations);
							}
							else
							{
								item_bin_set_type(ib, type_water_river);
							}
						}
						else if (wt3->tag_id == 5)
						{
							if (item_bin_is_closed_poly(ib) == 1)
							{
								item_bin_set_type(ib, type_poly_wood_from_triang);
							}
							else
							{
								//fprintf(stderr, "wood_from_relations(5)\n");
								item_bin_set_type(ib, type_wood_from_relations);
							}
						}
						else if (wt3->tag_id == 6)
						{
							if (item_bin_is_closed_poly(ib) == 1)
							{
								item_bin_set_type(ib, type_poly_wood_from_triang);
							}
							else
							{
								//fprintf(stderr, "wood_from_relations(6)\n");
								item_bin_set_type(ib, type_wood_from_relations);
							}
						}
						else if (wt3->tag_id == 7)
						{
							// "inner" way
							//fprintf(stderr, "wood_from_relations(7)\n");
							item_bin_set_type(ib, type_wood_from_relations);
						}
						else if (wt3->tag_id == 8)
						{
							// "inner" way
							//fprintf(stderr, "wood_from_relations(8)\n");
							item_bin_set_type(ib, type_wood_from_relations);
						}
					}
				}
			}
			// write way to outfile
			item_bin_write(ib, out);
		}

	}
}

/* filecopy:  copy file ifp to file ofp */
void filecopy(FILE *ifp, FILE *ofp)
{
	int c;
	while ((c = getc(ifp)) != EOF)
	{
		putc(c, ofp);
	}
}



#include "osm_s_index.h"







void remove_attr_str(struct item_bin *ib, int attr_type)
{
	char *attr_str = item_bin_get_attr(ib, attr_type, NULL);
	if (attr_str)
	{
		item_bin_remove_attr(ib, attr_str);
	}
}

int remove_useless_ways(FILE *in, FILE *out)
{
	struct item_bin *ib;
	long long *wayid;
	int *dup;

	while ((ib = read_item(in, 0))) // loop thru all "ways" from file "in"
	{
		if (ib->type != type_street_unkn) // remove unknown streets here
		{
			// write way to outfile
			item_bin_write(ib, out);
		}
	}
}

int remove_useless_tags_from_ways(FILE *in, FILE *out)
{
	struct item_bin *ib;
	long long *wayid;
	int *dup;

	while ((ib = read_item(in, 0))) // loop thru all "ways" from file "in"
	{
		// attr_street_name_dummy
		remove_attr_str(ib, attr_street_name_dummy);
		// attr_house_number_dummy
		remove_attr_str(ib, attr_house_number_dummy);
		// attr_debug
		remove_attr_str(ib, attr_debug);

		// osm wayid
		//wayid = item_bin_get_attr(ib, attr_osm_wayid, NULL);
		//if (wayid)
		//{
		//	item_bin_remove_attr(ib, wayid);
		//}

		// duplicate_way
		dup = item_bin_get_attr(ib, attr_duplicate_way, NULL);
		if (dup)
		{
			item_bin_remove_attr(ib, dup);
			//fprintf(stderr, "attr_duplicate_way:2: dup=true wayid=%lld\n", item_bin_get_wayid(ib));
		}

		// write way to outfile
		item_bin_write(ib, out);
	}
}

int remove_useless_tags_from_nodes(FILE *in, FILE *out)
{
	struct item_bin *ib;
	long long *nodeid;

	while ((ib = read_item(in, 0))) // loop thru all "nodes" from file "in"
	{
		// attr_debug
		remove_attr_str(ib, attr_debug);

		// osm nodeid
		nodeid = item_bin_get_attr(ib, attr_osm_nodeid, NULL);
		if (nodeid)
		{
			item_bin_remove_attr(ib, nodeid);
		}

		// write node to outfile
		item_bin_write(ib, out);
	}
}

void map_find_housenumbers_interpolation(FILE *in, FILE *out)
{
	int ccount;
	struct item_bin *ib;
	long long ways_count = 0;
	struct coord c;

	while ((ib = read_item(in, 0))) // loop thru all "ways" from file "in"
	{
		ways_count++;
		if ((ways_count % 10000000) == 0)
		{
			fprintf_(stderr, "ways: %lld\n", ways_count);
		}

		// we need at least 2 points in way
		if (ccount <= 1)
		{
			continue;
		}

		// only type "house_number_interpolate*"
		if (
			(ib->type != type_house_number_interpolation_even) &&
			(ib->type != type_house_number_interpolation_odd) &&
			(ib->type != type_house_number_interpolation_all) &&
			(ib->type != type_house_number_interpolation_alphabetic)
			)
		{
			continue;
		}

		ccount = ib->clen / 2;

		// set coord of housenumber here
		c.x = 0;
		c.y = 0;
		// set coord of housenumber here
		char *street_name = item_bin_get_attr(ib, attr_street_name_dummy, NULL);
		char *house_number = item_bin_get_attr(ib, attr_house_number_dummy, NULL);
		if ((street_name != NULL) && (house_number != NULL))
		{
			//fprintf(stderr, "ADDR:Interpolate:%s, %s\n", house_number, street_name);
			osm_append_housenumber_node(out, &c, house_number, street_name);
		}
	}
}

int map_find_intersections(FILE *in, FILE *out, FILE *out_index, FILE *out_graph, FILE *out_coastline, int final)
{
	struct coord *c;
	int i, ccount, last, remaining;
	osmid ndref;
	struct item_bin *ib;
	struct node_item *ni;
	long long last_id = 0;
	long long ways_count = 0;
	osmid *this_way_id;
	osmid this_way_id_real;
	int i_real = -1;

	processed_nodes = 0;
	processed_nodes_out = 0;
	processed_ways = 0;
	processed_relations = 0;
	processed_tiles = 0;


	// ---------- reset the cached values ----------
	seekpos_waynode[0] = -1;
	last_seekpos_waynode[0] = -1;
	last_seek_wayid[0] = -1;
	fseeko(ways_ref_file_thread[0], (off_t)0, SEEK_SET);
	// ---------- reset the cached values ----------


	while ((ib = read_item(in, 0))) // loop thru all "ways" from file "in"
	{

		this_way_id = item_bin_get_attr(ib, attr_osm_wayid, NULL);
		if (this_way_id)
		{
			this_way_id_real = *this_way_id;
		//	fprintf(stderr,"wayid:%lld type:0x%x len:%d clen:%d\n", *this_way_id, ib->type, ib->len, (ib->clen / 2));
		}

		ccount = ib->clen / 2;

		if (ccount <= 1)
		{
			continue;
		}

		//fprintf(stderr,"********DUMP ww1***********\n");
		//dump_itembin(ib);
		//fprintf(stderr,"********DUMP ww1***********\n");


		ways_count++;
		if ((ways_count % 10000000) == 0)
		{
			fprintf_(stderr, "ways: %lld\n", ways_count);
		}

		c = (struct coord *) (ib + 1);
		last = 0;
		i_real = -1;
		for (i = 0; i < ccount; i++) // loop thru all the coordinates (nodes) of this way
		{
			//if (this_way_id_real != 0)
			//{
			//	fprintf(stderr, "this_way_id_real=%lld this_way_id=%lld i=%d ccount=%d\n", this_way_id_real, *this_way_id, i, ccount);
			//}

			if (IS_REF(c[i]))
			{
				//fprintf(stderr, "is ref\n");
				if (this_way_id_real != 0)
				{
					i_real = c[i].y; // number of this node in the way (starting at zero)
					ndref = get_waynode_num(this_way_id_real, i_real, 0);
					//fprintf(stderr, "wayid:%lld wid(p)=%p i_real=%d i=%d ndref(1)=%lld\n", this_way_id_real, this_way_id, i_real, i, ndref);
					ni = node_item_get_fast(ndref, 0);
					//fprintf(stderr, "ni(1)=%p\n", ni);
				}
				else
				{
					ni = NULL;
				}

				//fprintf(stderr, "ndref(2)=%lld\n", ndref);

				if (ni)
				{
					//fprintf(stderr, "ni TRUE\n");

					c[i] = ni->c; // write "lat,long" from node into way !!
					if (ni->ref_way > 1 && i != 0 && i != ccount - 1 && i != last && item_get_default_flags(ib->type))
					{
						//fprintf(stderr, "wr i 001\n");
						// if "ref_way > 1" , means this node belongs to more than 1 way, so it must be an intersection
						// *** write_item_part(out, out_index, out_graph, ib, last, i, &last_id);
						write_item_part(out, NULL, out_graph, ib, last, i, &last_id);
						last = i;
					}
				}
				else if (final)
				{
					//fprintf(stderr, "wr i 002(f)\n");

					//osm_warning("way",item_bin_get_wayid(ib),0,"Non-existing reference to ");
					//osm_warning("node",ndref,1,"\n");
					remaining = (ib->len + 1) * 4 - sizeof(struct item_bin) - i * sizeof(struct coord);
					memmove(&c[i], &c[i + 1], remaining);
					ib->clen -= 2;
					ib->len -= 2;
					i--;
					ccount--;
				}
			}
			//else
			//{
			//	fprintf(stderr, "is NOT ref\n");
			//}
		}

		if (ccount)
		{
			//fprintf(stderr, "wr i 003(ccount)\n");

			//fprintf(stderr,"*x*coastline*x*\n");
			// ***** write_item_part(out, out_index, out_graph, ib, last, ccount - 1, &last_id);
			write_item_part(out, NULL, out_graph, ib, last, ccount - 1, &last_id);
			if (final && ib->type == type_water_line && out_coastline)
			{
				//fprintf(stderr,"write out_coastline\n");
				write_item_part(out_coastline, NULL, NULL, ib, last, ccount - 1, NULL);
			}
		}
	}

	return 0;
}

int map_find_intersections__quick__for__debug(FILE *in, FILE *out, FILE *out_index, FILE *out_graph, FILE *out_coastline, int final)
{
	struct coord *c;
	int i, ccount, last, remaining;
	osmid ndref;
	struct item_bin *ib;
	struct node_item *ni;
	long long last_id = 0;
	long long ways_count = 0;
	osmid *this_way_id;
	osmid this_way_id_real;
	int i_real = -1;

	fprintf(stderr, "DEBUG:map_find_intersections__quick__for__debug\n");

	processed_nodes = 0;
	processed_nodes_out = 0;
	processed_ways = 0;
	processed_relations = 0;
	processed_tiles = 0;


	// ---------- reset the cached values ----------
	seekpos_waynode[0] = -1;
	last_seekpos_waynode[0] = -1;
	last_seek_wayid[0] = -1;
	fseeko(ways_ref_file_thread[0], (off_t)0, SEEK_SET);
	// ---------- reset the cached values ----------


	while ((ib = read_item(in, 0))) // loop thru all "ways" from file "in"
	{

		this_way_id = item_bin_get_attr(ib, attr_osm_wayid, NULL);
		if (this_way_id)
		{
			this_way_id_real = *this_way_id;
		//	fprintf(stderr,"wayid:%lld type:0x%x len:%d clen:%d\n", *this_way_id, ib->type, ib->len, (ib->clen / 2));
		}

		ccount = ib->clen / 2;

		if (ccount <= 1)
		{
			continue;
		}

		//fprintf(stderr,"********DUMP ww1***********\n");
		//dump_itembin(ib);
		//fprintf(stderr,"********DUMP ww1***********\n");


		ways_count++;
		if ((ways_count % 10000000) == 0)
		{
			fprintf_(stderr, "ways: %lld\n", ways_count);
		}

		c = (struct coord *) (ib + 1);
		last = 0;
		i_real = -1;
		for (i = 0; i < ccount; i++) // loop thru all the coordinates (nodes) of this way
		{
			//if (this_way_id_real != 0)
			//{
			//	fprintf(stderr, "this_way_id_real=%lld this_way_id=%lld i=%d ccount=%d\n", this_way_id_real, *this_way_id, i, ccount);
			//}

			if (IS_REF(c[i]))
			{
				//fprintf(stderr, "is ref\n");
				if (this_way_id_real != 0)
				{
					i_real = c[i].y; // number of this node in the way (starting at zero)
					ndref = get_waynode_num(this_way_id_real, i_real, 0);
					//fprintf(stderr, "wayid:%lld wid(p)=%p i_real=%d i=%d ndref(1)=%lld\n", this_way_id_real, this_way_id, i_real, i, ndref);
					ni = node_item_get_fast(ndref, 0);
					//fprintf(stderr, "ni(1)=%p\n", ni);
				}
				else
				{
					ni = NULL;
				}

				//fprintf(stderr, "ndref(2)=%lld\n", ndref);

				if (ni)
				{
					//fprintf(stderr, "ni TRUE\n");

					c[i] = ni->c; // write "lat,long" from node into way !!
				}
			}
		}
		item_bin_write(ib, out);

	}

	return 0;
}


static void index_country_add(struct zip_info *info, int country_id, int zipnum)
{
	struct item_bin *item_bin = init_item(type_countryindex, 0);
	item_bin_add_attr_int(item_bin, attr_country_id, country_id);
	item_bin_add_attr_int(item_bin, attr_zipfile_ref, zipnum);
	item_bin_write(item_bin, zip_get_index(info));
}

void write_countrydir(struct zip_info *zip_info)
{
	int i, zipnum, num;
	int max = 11;
	char tilename[32];
	char filename[32];
	char suffix[32];
	struct country_table *co;
	for (i = 0; i < sizeof(country_table) / sizeof(struct country_table); i++)
	{
		co = &country_table[i];
		if (co->size)
		{
			num = 0;
			do
			{
				tilename[0] = '\0';
				sprintf(suffix, "s%d", num);
				num++;
				tile(&co->r, suffix, tilename, max, overlap, NULL);

				sprintf(filename, "country_%d.tmp", co->countryid);
				if (debug_itembin(4))
				{
					fprintf_(stderr, "write_countrydir: tilename=%s country_%d.tmp\n", tilename, co->countryid);
				}
				zipnum = add_aux_tile(zip_info, tilename, filename, co->size);
			}
			while (zipnum == -1);
			index_country_add(zip_info, co->countryid, zipnum);
		}
	}
}

void load_countries(void)
{
	char filename[32];
	FILE *f;
	int i;
	struct country_table *co;

	for (i = 0; i < sizeof(country_table) / sizeof(struct country_table); i++)
	{
		co = &country_table[i];
		sprintf(filename, "country_%d.tmp", co->countryid);
		if (debug_itembin(4))
		{
			fprintf_(stderr, "load_countries: country_%d.tmp\n", co->countryid);
		}

		f = fopen(filename, "rb");
		if (f)
		{
			int i, first = 1;
			struct item_bin *ib;
			while ((ib = read_item(f, 0)))
			{
				struct coord *c = (struct coord *) (ib + 1);
				co->size += ib->len * 4 + 4;
				for (i = 0; i < ib->clen / 2; i++)
				{
					if (first)
					{
						co->r.l = c[i];
						co->r.h = c[i];
						first = 0;
					}
					else
					{
						bbox_extend(&c[i], &co->r);
					}
				}
			}
			fseek(f, 0, SEEK_END);
			co->size = ftell(f);
			fclose(f);
		}
	}
}

void remove_countryfiles(void)
{
	int i;
	char filename[32];
	struct country_table *co;

	for (i = 0; i < sizeof(country_table) / sizeof(struct country_table); i++)
	{
		co = &country_table[i];
		if (co->size)
		{
			sprintf(filename, "country_%d.tmp", co->countryid);
			unlink(filename);
		}
	}
}

void osm_init(FILE* rule_file)
{
	build_attrmap(rule_file);
	build_countrytable();
}

