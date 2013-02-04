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

extern int doway2poi;

static int in_way, in_node, in_relation;
static int nodeid, wayid;
long long current_id;

static GHashTable *attr_hash, *country_table_hash;

void add_boundary_to_db(long long current_id, int admin_level, struct item_bin *item_bin_3, struct relation_member *memb);

static char *attr_present;
static int attr_present_count;

static struct item_bin item;

int maxspeed_attr_value;

char debug_attr_buffer[BUFFER_SIZE];

int flags[4];

int flags_attr_value;

struct attr_bin osmid_attr;
long int osmid_attr_value;

char is_in_buffer[BUFFER_SIZE];

char attr_strings_buffer[BUFFER_SIZE * 16];
int attr_strings_buffer_len;
int alt_name_found = 0;

struct coord coord_buffer[65536];

struct attr_mapping
{
	enum item_type type;
	int attr_present_idx_count;
	int attr_present_idx[0];
};

static void nodes_ref_item_bin(struct item_bin *ib, int local_thread_num);

static struct attr_mapping **attr_mapping_node;
static int attr_mapping_node_count;
static struct attr_mapping **attr_mapping_way;
static int attr_mapping_way_count;
static struct attr_mapping **attr_mapping_way2poi;
static int attr_mapping_way2poi_count;

static char *attr_present;
static int attr_present_count;

enum attr_strings
{
	attr_string_phone, attr_string_fax, attr_string_email, attr_string_url, attr_string_street_name, attr_string_street_name_systematic, attr_string_house_number, attr_string_label, attr_string_label_alt, attr_string_postal, attr_string_population, attr_string_county_name, attr_string_last,
};

char *attr_strings[attr_string_last];

char *osm_types[] = { "unknown", "node", "way", "relation" };

#define IS_REF(c) ((c).x >= (1 << 30))
#define REF(c) ((c).y)
#define SET_REF(c,ref) do { (c).x = 1 << 30; (c).y = ref ; } while(0)

struct country_table
{
	int countryid;
	char *names;
	char *admin_levels;
	FILE *file;
	int size;
	struct rect r;
}
		country_table[] =
				{ { 4, "Afghanistan" }, { 8, "Albania" }, { 10, "Antarctica" }, { 12, "Algeria" }, { 16, "American Samoa" }, { 20, "Andorra" }, { 24, "Angola" }, { 28, "Antigua and Barbuda" }, { 31, "Azerbaijan" }, { 32, "Argentina,República Argentina,AR " }, { 36, "Australia,AUS" }, { 40, "Austria,Österreich,AUT" }, { 44, "Bahamas" }, { 48, "Bahrain" }, { 50, "Bangladesh" }, { 51, "Armenia" }, { 52, "Barbados" }, { 56, "Belgium,Belgique,Belgie,België,Belgien" }, { 60, "Bermuda" }, { 64, "Bhutan" }, { 68, "Bolivia, Plurinational State of" }, { 70, "Bosnia and Herzegovina,Bosna i Hercegovina,Босна и Херцеговина" }, { 72, "Botswana" }, { 74, "Bouvet Island" }, { 76, "Brazil" }, { 84, "Belize" }, { 86, "British Indian Ocean Territory" }, { 90, "Solomon Islands" }, { 92, "Virgin Islands, British" }, { 96, "Brunei Darussalam" }, { 100, "Bulgaria,България" }, { 104, "Myanmar" }, { 108, "Burundi" }, { 112, "Belarus" }, { 116, "Cambodia" }, { 120, "Cameroon" }, { 124, "Canada" }, { 132, "Cape Verde" }, { 136, "Cayman Islands" }, { 140, "Central African Republic" }, { 144, "Sri Lanka" }, { 148, "Chad" }, { 152, "Chile" }, { 156, "China" }, { 158, "Taiwan, Province of China" }, { 162, "Christmas Island" }, { 166, "Cocos (Keeling) Islands" }, { 170, "Colombia" }, { 174, "Comoros" }, { 175, "Mayotte" }, { 178, "Congo" }, { 180, "Congo, the Democratic Republic of the" }, { 184, "Cook Islands" }, { 188, "Costa Rica" }, { 191, "Croatia,Republika Hrvatska,HR" }, { 192, "Cuba" }, { 196, "Cyprus" }, { 203, "Czech Republic,Česká republika,CZ" }, { 204, "Benin" }, { 208, "Denmark,Danmark,DK" }, { 212, "Dominica" }, { 214, "Dominican Republic" }, { 218, "Ecuador" }, { 222, "El Salvador" }, { 226, "Equatorial Guinea" }, { 231, "Ethiopia" }, { 232, "Eritrea" }, { 233, "Estonia" }, { 234, "Faroe Islands,Føroyar" }, { 238, "Falkland Islands (Malvinas)" }, { 239, "South Georgia and the South Sandwich Islands" }, { 242, "Fiji" }, { 246, "Finland,Suomi" }, { 248, "Åland Islands" }, { 250, "France,République française,FR" }, { 254, "French Guiana" }, { 258, "French Polynesia" }, { 260, "French Southern Territories" }, { 262, "Djibouti" }, { 266, "Gabon" }, { 268, "Georgia" }, { 270, "Gambia" }, { 275, "Palestinian Territory, Occupied" }, { 276, "Germany,Deutschland,Bundesrepublik Deutschland", "345c7m" }, { 288, "Ghana" }, { 292, "Gibraltar" }, { 296, "Kiribati" }, { 300, "Greece" }, { 304, "Greenland" }, { 308, "Grenada" }, { 312, "Guadeloupe" }, { 316, "Guam" }, { 320, "Guatemala" }, { 324, "Guinea" }, { 328, "Guyana" }, { 332, "Haiti" }, { 334, "Heard Island and McDonald Islands" }, { 336, "Holy See (Vatican City State)" }, { 340, "Honduras" }, { 344, "Hong Kong" }, { 348, "Hungary,Magyarország" }, { 352, "Iceland" }, { 356, "India" }, { 360, "Indonesia" }, { 364, "Iran, Islamic Republic of" }, { 368, "Iraq" }, { 372, "Ireland" }, { 376, "Israel" }, { 380, "Italy,Italia" }, { 384, "Côte d'Ivoire" }, { 388, "Jamaica" }, { 392, "Japan" }, { 398, "Kazakhstan" }, { 400, "Jordan" }, { 404, "Kenya" }, { 408, "Korea, Democratic People's Republic of" }, { 410, "Korea, Republic of" }, { 414, "Kuwait" }, { 417, "Kyrgyzstan" }, { 418, "Lao People's Democratic Republic" }, { 422, "Lebanon" }, { 426, "Lesotho" }, { 428, "Latvia" }, { 430, "Liberia" }, { 434, "Libyan Arab Jamahiriya" }, { 438, "Liechtenstein" }, { 440, "Lithuania,Lietuva" }, { 442, "Luxembourg" }, { 446, "Macao" }, { 450, "Madagascar" }, { 454, "Malawi" }, { 458, "Malaysia" }, { 462, "Maldives" }, { 466, "Mali" }, { 470, "Malta" }, { 474, "Martinique" }, { 478, "Mauritania" }, { 480, "Mauritius" }, { 484, "Mexico" }, { 492, "Monaco" }, { 496, "Mongolia" }, { 498, "Moldova, Republic of" }, { 499, "Montenegro,Црна Гора,Crna Gora" }, { 500, "Montserrat" }, { 504, "Morocco" }, { 508, "Mozambique" }, { 512, "Oman" }, { 516, "Namibia" }, { 520, "Nauru" }, { 524, "Nepal" }, { 528, "Nederland,The Netherlands,Niederlande,NL,Netherlands" }, { 530, "Netherlands Antilles" }, { 533, "Aruba" }, { 540, "New Caledonia" }, { 548, "Vanuatu" }, { 554, "New Zealand" }, { 558, "Nicaragua" }, { 562, "Niger" }, { 566, "Nigeria" }, { 570, "Niue" }, { 574, "Norfolk Island" }, { 578, "Norway,Norge,Noreg,NO" }, { 580, "Northern Mariana Islands" }, { 581, "United States Minor Outlying Islands" }, { 583, "Micronesia, Federated States of" }, { 584, "Marshall Islands" }, { 585, "Palau" }, { 586, "Pakistan" }, { 591, "Panama" }, { 598, "Papua New Guinea" }, { 600, "Paraguay" }, { 604, "Peru" }, { 608, "Philippines" }, { 612, "Pitcairn" }, { 616, "Poland,Polska,PL" }, { 620, "Portugal" }, { 624, "Guinea-Bissau" }, { 626, "Timor-Leste" }, { 630, "Puerto Rico" }, { 634, "Qatar" }, { 638, "Réunion" }, { 642, "România,Romania,RO" }, { 643, "Россия,Российская Федерация,Russia,Russian Federation" }, { 646, "Rwanda" }, { 652, "Saint Barthélemy" }, { 654, "Saint Helena, Ascension and Tristan da Cunha" }, { 659, "Saint Kitts and Nevis" }, { 660, "Anguilla" }, { 662, "Saint Lucia" }, { 663, "Saint Martin (French part)" }, { 666, "Saint Pierre and Miquelon" }, { 670, "Saint Vincent and the Grenadines" }, { 674, "San Marino" }, { 678, "Sao Tome and Principe" }, { 682, "Saudi Arabia" }, { 686, "Senegal" }, { 688, "Srbija,Србија,Serbia" }, { 690, "Seychelles" }, { 694, "Sierra Leone" }, { 702, "Singapore" }, { 703, "Slovakia,Slovensko,SK" }, { 704, "Viet Nam" }, { 705, "Slovenia,Republika Slovenija,SI" }, { 706, "Somalia" }, { 710, "South Africa" }, { 716, "Zimbabwe" }, { 724, "Spain,Espana,España,Reino de Espana,Reino de España" }, { 732, "Western Sahara" }, { 736, "Sudan" }, { 740, "Suriname" }, { 744, "Svalbard and Jan Mayen" }, { 748, "Swaziland" }, { 752, "Sweden,Sverige,Konungariket Sverige,SE" }, { 756, "Switzerland,Schweiz" }, { 760, "Syrian Arab Republic" }, { 762, "Tajikistan" }, { 764, "Thailand" }, { 768, "Togo" }, { 772, "Tokelau" }, { 776, "Tonga" }, { 780, "Trinidad and Tobago" }, { 784, "United Arab Emirates" }, { 788, "Tunisia" }, { 792, "Turkey" }, { 795, "Turkmenistan" }, { 796, "Turks and Caicos Islands" }, { 798, "Tuvalu" }, { 800, "Uganda" }, { 804, "Ukraine" }, { 807, "Macedonia,Македонија" }, { 818, "Egypt" }, { 826, "United Kingdom,UK" }, { 831, "Guernsey" }, { 832, "Jersey" }, { 833, "Isle of Man" }, { 834, "Tanzania, United Republic of" }, { 840, "USA" }, { 850, "Virgin Islands, U.S." }, { 854, "Burkina Faso" }, { 858, "Uruguay" }, { 860, "Uzbekistan" }, { 862, "Venezuela, Bolivarian Republic of" }, { 876, "Wallis and Futuna" }, { 882, "Samoa" }, { 887, "Yemen" }, { 894, "Zambia" }, { 999, "Unknown" }, };

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
	"n	amenity=atm		poi_bank\n"
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
	//"n	car=car_rental		poi_car_rent\n"
	"n	highway=bus_station	poi_bus_station\n"
	"n	highway=bus_stop	poi_bus_stop\n"
	"n	highway=mini_roundabout	mini_roundabout\n"
	"n	highway=motorway_junction	highway_exit\n"
	"n	highway=stop		traffic_sign_stop\n"
	"n	highway=toll_booth	poi_toll_booth\n"
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
	//"n	leisure=sports_centre	poi_sport\n"
	"n	leisure=stadium		poi_stadium\n"
	//"n	man_made=tower		poi_tower\n"
	"n	military=airfield	poi_military\n"
	//"n	military=barracks	poi_military\n"
	//"n	military=bunker		poi_military\n"
	"n	military=danger_area	poi_danger_area\n"
	"n	military=range		poi_military\n"
	"n	natural=bay		poi_bay\n"
	"n	natural=peak,ele=*		poi_peak\n" // show only major peaks with elevation
	//"n	natural=tree		poi_tree\n"
	"n	place=city		town_label_2e5\n"
	"n	place=hamlet		town_label_2e2\n"
	"n	place=locality		town_label_2e0\n"
	"n	place=suburb		district_label\n"
	"n	place=town		town_label_2e4\n"
	"n	place=village		town_label_2e3\n"
	//"n	power=tower		power_tower\n"
	//"n	power=sub_station	power_substation\n"
	"n	railway=halt		poi_rail_halt\n"
	"n	railway=level_crossing	poi_level_crossing\n"
	"n	railway=station		poi_rail_station\n"
	"n	railway=tram_stop	poi_rail_tram_stop\n"
	//"n	shop=baker		poi_shop_baker\n"
	//"n	shop=bakery		poi_shop_baker\n"
	//"n	shop=beverages		poi_shop_beverages\n"
	//"n	shop=bicycle		poi_shop_bicycle\n"
	//"n	shop=butcher		poi_shop_butcher\n"
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
	//"n	shop=hairdresser	poi_hairdresser\n"
	//"n	shop=kiosk		poi_shop_kiosk\n"
	//"n	shop=optician		poi_shop_optician\n"
	//"n	shop=parfum		poi_shop_parfum\n"
	//"n	shop=photo		poi_shop_photo\n"
	//"n	shop=shoes		poi_shop_shoes\n"
	"n	shop=supermarket	poi_shopping\n"
	//"n	sport=10pin		poi_bowling\n"
	//"n	sport=baseball		poi_baseball\n"
	//"n	sport=basketball	poi_basketball\n"
	//"n	sport=climbing		poi_climbing\n"
	//"n	sport=golf		poi_golf\n"
	//"n	sport=motor_sports	poi_motor_sport\n"
	//"n	sport=skiing		poi_skiing\n"
	//"n	sport=soccer		poi_soccer\n"
	"n	sport=stadium		poi_stadium\n"
	"n	sport=swimming		poi_swimming\n"
	//"n	sport=tennis		poi_tennis\n"
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
	//"n	tourism=viewpoint	poi_viewpoint\n"
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
	"w	highway=motorway_link			ramp\n"
	"w	highway=trunk				street_4_land\n"
	"w	highway=trunk,name=*,rural=1		street_4_land\n"
	"w	highway=trunk,name=*			street_4_city\n"
	"w	highway=trunk,rural=0			street_4_city\n"
	"w	highway=trunk_link			ramp\n"
	"w	highway=primary				street_4_land\n"
	"w	highway=primary,name=*,rural=1		street_4_land\n"
	"w	highway=primary,name=*			street_4_city\n"
	"w	highway=primary,rural=0			street_4_city\n"
	"w	highway=primary_link			ramp\n"
	"w	highway=secondary			street_3_land\n"
	"w	highway=secondary,name=*,rural=1	street_3_land\n"
	"w	highway=secondary,name=*		street_3_city\n"
	"w	highway=secondary,rural=0		street_3_city\n"
	"w	highway=secondary,area=1		poly_street_3\n"
	"w	highway=secondary_link			ramp\n"
	"w	highway=tertiary			street_2_land\n"
	"w	highway=tertiary,name=*,rural=1		street_2_land\n"
	"w	highway=tertiary,name=*			street_2_city\n"
	"w	highway=tertiary,rural=0		street_2_city\n"
	"w	highway=tertiary,area=1			poly_street_2\n"
	"w	highway=tertiary_link			ramp\n"
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
	//"w	military=barracks	poly_barracks\n"
	"w	military=danger_area	poly_danger_area\n"
	"w	military=naval_base	poly_naval_base\n"
	//"w	military=range		poly_range\n"
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
			flags[0] |= AF_ONEWAY | AF_ROUNDABOUT_VALID;
		}
		if (!strcmp(v, "-1"))
		{
			flags[0] |= AF_ONEWAYREV | AF_ROUNDABOUT_VALID;
		}
		if (!in_way)
			level = 6;
		else
			level = 5;
	}
	if (!strcmp(k, "junction"))
	{
		if (!strcmp(v, "roundabout"))
			flags[0] |= AF_ONEWAY | AF_ROUNDABOUT | AF_ROUNDABOUT_VALID;
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
			flags[0] |= AF_SPEED_LIMIT;
		level = 5;
	}
	if (!strcmp(k, "toll"))
	{
		if (!strcmp(v, "1"))
		{
			flags[0] |= AF_TOLL;
		}
	}
	if (!strcmp(k, "access"))
	{
		flags[access_value(v)] |= AF_DANGEROUS_GOODS | AF_EMERGENCY_VEHICLES | AF_TRANSPORT_TRUCK | AF_DELIVERY_TRUCK | AF_PUBLIC_BUS | AF_TAXI | AF_HIGH_OCCUPANCY_CAR | AF_CAR | AF_MOTORCYCLE | AF_MOPED | AF_HORSE | AF_BIKE | AF_PEDESTRIAN;
		level = 5;
	}
	if (!strcmp(k, "vehicle"))
	{
		flags[access_value(v)] |= AF_DANGEROUS_GOODS | AF_EMERGENCY_VEHICLES | AF_TRANSPORT_TRUCK | AF_DELIVERY_TRUCK | AF_PUBLIC_BUS | AF_TAXI | AF_HIGH_OCCUPANCY_CAR | AF_CAR | AF_MOTORCYCLE | AF_MOPED | AF_BIKE;
		level = 5;
	}
	if (!strcmp(k, "motorvehicle"))
	{
		flags[access_value(v)] |= AF_DANGEROUS_GOODS | AF_EMERGENCY_VEHICLES | AF_TRANSPORT_TRUCK | AF_DELIVERY_TRUCK | AF_PUBLIC_BUS | AF_TAXI | AF_HIGH_OCCUPANCY_CAR | AF_CAR | AF_MOTORCYCLE | AF_MOPED;
		level = 5;
	}
	if (!strcmp(k, "bicycle"))
	{
		flags[access_value(v)] |= AF_BIKE;
		level = 5;
	}
	if (!strcmp(k, "foot"))
	{
		flags[access_value(v)] |= AF_PEDESTRIAN;
		level = 5;
	}
	if (!strcmp(k, "horse"))
	{
		flags[access_value(v)] |= AF_HORSE;
		level = 5;
	}
	if (!strcmp(k, "moped"))
	{
		flags[access_value(v)] |= AF_MOPED;
		level = 5;
	}
	if (!strcmp(k, "motorcycle"))
	{
		flags[access_value(v)] |= AF_MOTORCYCLE;
		level = 5;
	}
	if (!strcmp(k, "motorcar"))
	{
		flags[access_value(v)] |= AF_CAR;
		level = 5;
	}
	if (!strcmp(k, "hov"))
	{
		flags[access_value(v)] |= AF_HIGH_OCCUPANCY_CAR;
		level = 5;
	}
	if (!strcmp(k, "bus"))
	{
		flags[access_value(v)] |= AF_PUBLIC_BUS;
		level = 5;
	}
	if (!strcmp(k, "taxi"))
	{
		flags[access_value(v)] |= AF_TAXI;
		level = 5;
	}
	if (!strcmp(k, "goods"))
	{
		flags[access_value(v)] |= AF_DELIVERY_TRUCK;
		level = 5;
	}
	if (!strcmp(k, "hgv"))
	{
		flags[access_value(v)] |= AF_TRANSPORT_TRUCK;
		level = 5;
	}
	if (!strcmp(k, "emergency"))
	{
		flags[access_value(v)] |= AF_EMERGENCY_VEHICLES;
		level = 5;
	}
	if (!strcmp(k, "hazmat"))
	{
		flags[access_value(v)] |= AF_DANGEROUS_GOODS;
		level = 5;
	}
	if (!strcmp(k, "tunnel") && !strcmp(v, "1"))
	{
		flags[0] |= AF_UNDERGROUND;
	}
	if (!strcmp(k, "bridge") && !strcmp(v, "1"))
	{
		flags[0] |= AF_BRIDGE;
	}
	if (!strcmp(k, "note"))
		level = 5;
	if (!strcmp(k, "name"))
	{
		attr_strings_save(attr_string_label, v);
		level = 5;
	}

	if ((!strcmp(k, "name:en")) && (alt_name_found == 0))
	{
		attr_strings_save(attr_string_label_alt, v);
		alt_name_found = 1;
		level = 5;
	}
	if ((!strcmp(k, "alt_name:en")) && (alt_name_found < 2))
	{
		// only use "alt_name:en" if we dont have "alt_name"
		attr_strings_save(attr_string_label_alt, v);
		alt_name_found = 2;
		level = 5;
	}
	if (!strcmp(k, "alt_name"))
	{
		attr_strings_save(attr_string_label_alt, v);
		alt_name_found = 3;
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
			attr_strings_save(attr_string_street_name_systematic, v);
		level = 5;
	}
	if (!strcmp(k, "openGeoDB:is_in"))
	{
		if (!is_in_buffer[0])
			strcpy(is_in_buffer, v);
		level = 5;
	}
	if (!strcmp(k, "is_in"))
	{
		if (!is_in_buffer[0])
			strcpy(is_in_buffer, v);
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
	if (!strcmp(k, "lanes"))
	{
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
		attr_present[idx] = 1;

	sprintf(buffer, "%s=*", k);
	if ((idx = (int) (long) g_hash_table_lookup(attr_hash, buffer)))
		attr_present[idx] = 2;

	sprintf(buffer, "*=%s", v);
	if ((idx = (int) (long) g_hash_table_lookup(attr_hash, buffer)))
		attr_present[idx] = 2;

	sprintf(buffer, "%s=%s", k, v);
	if ((idx = (int) (long) g_hash_table_lookup(attr_hash, buffer)))
		attr_present[idx] = 4;
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

int nodeid_last;
GHashTable *way_hash, *waytag_hash;
cfuhash_table_t *way_hash_cfu = NULL;

static void node_buffer_to_hash(int local_thread_num)
{
	if (verbose_mode)
		fprintf(stderr, "node_buffer_to_hash t:%d nb->size:%lu\n", local_thread_num, node_buffer[local_thread_num].size);
	if (verbose_mode)
		fprintf(stderr, "node_buffer_to_hash t:%d nb->base:%lu\n", local_thread_num, node_buffer[local_thread_num].base);
	if (verbose_mode)
		fprintf(stderr, "node_buffer_to_hash t:%d nh:%p\n", local_thread_num, node_hash[local_thread_num]);

	int i = 0;
	int count2 = node_buffer[local_thread_num].size / sizeof(struct node_item);
	struct node_item *ni = (struct node_item *) node_buffer[local_thread_num].base;

	if (verbose_mode)
		fprintf(stderr, "node_buffer_to_hash %d %d %d\n", local_thread_num, i, count2);

	for (i = 0; i < count2; i++)
	{
		/*
		 if (i % 5000)
		 {
		 fprintf(stderr, "thread #%d fill #%d\n", local_thread_num, i);
		 }
		 */
		// g_hash_table_insert(node_hash[local_thread_num], (gpointer) (long) (ni[i].id), (gpointer) (long) i);
		//cfuhash_put(node_hash_cfu[local_thread_num], &(long long)ni[i].id, xxx);

		//fprintf(stderr, "node id:%lld i:%d\n", (long long)ni[i].id, i);
		cfuhash_put_data(node_hash_cfu[local_thread_num], (long long)ni[i].id, sizeof(long long), i, 1, NULL);
	}

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
		node_hash_cfu[i] = cfuhash_new_with_initial_size(CFUHASH_BUCKETS_NODES);
		cfuhash_set_flag(node_hash_cfu[i], CFUHASH_FROZEN);
	}
}

void fill_hash_node(int local_thread_num)
{
	//fprintf(stderr, "001t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);

	if (verbose_mode)
		fprintf(stderr, "fill_hash_node - START\n");
	//if (node_hash[local_thread_num])
	if (node_hash_cfu[local_thread_num])
	{
		//g_hash_table_destroy(node_hash[local_thread_num]);
		//node_hash[local_thread_num] = NULL;
		cfuhash_clear(node_hash_cfu[local_thread_num]);
		cfuhash_destroy(node_hash_cfu[local_thread_num]);
		node_hash_cfu[local_thread_num] = NULL;
	}

	// node_hash[local_thread_num] = g_hash_table_new(NULL, NULL);
	node_hash_cfu[local_thread_num] = cfuhash_new_with_initial_size(CFUHASH_BUCKETS_NODES);
	cfuhash_set_flag(node_hash_cfu[local_thread_num], CFUHASH_FROZEN);

	//fprintf(stderr, "002t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);
	node_buffer_to_hash(local_thread_num);
	//fprintf(stderr, "003t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);
	if (verbose_mode)
		fprintf(stderr, "fill_hash_node - END\n");
}

void flush_nodes(int final, int local_thread_num)
{
	save_buffer("coords.tmp", &node_buffer[local_thread_num], slices * slice_size);
	//fprintf(stderr, "004t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);

	if (verbose_mode)
		fprintf(stderr, "flush_nodes %d\n", final);
	if (!final)
	{
		// ------ this part optional ------
		// //free buffer
		//fprintf(stderr, "flush_nodes:# freeing buffer #\n");
		//free(node_buffer.base);
		//node_buffer.base = NULL;
		//node_buffer.malloced = 0;
		// //malloc a new buffer
		//node_buffer.malloced = 10 * 1024 * 1024;
		//node_buffer.base = malloc((size_t) node_buffer.malloced); // 10 MByte
		// ------ this part optional ------

		// ------- need this always --------

		//fprintf(stderr, "005t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);
		node_buffer[local_thread_num].size = 0;
		//fprintf(stderr, "006t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);

		//if (node_hash[local_thread_num])
		if (node_hash_cfu[local_thread_num])
		{
			if (verbose_mode) fprintf(stderr,"node_hash size="LONGLONG_FMT"\n", g_hash_table_size (node_hash[local_thread_num]));
			//g_hash_table_destroy(node_hash[local_thread_num]);
			//node_hash[local_thread_num] = NULL;
			cfuhash_clear(node_hash_cfu[local_thread_num]);
			cfuhash_destroy(node_hash_cfu[local_thread_num]);
			node_hash_cfu[local_thread_num] = NULL;
		}
	}
	if (verbose_mode) fprintf(stderr,"node: node_buffer size="LONGLONG_FMT"\n", node_buffer[local_thread_num].size);
	if (verbose_mode) fprintf(stderr,"node: node_buffer malloced="LONGLONG_FMT"\n", node_buffer[local_thread_num].malloced);
	//fprintf(stderr, "007t:%d base:%lu\n", local_thread_num, node_buffer[local_thread_num]);
	slices++;
}

double transform_to_geo_lat(int y)
{
	double lat = navit_atan(exp(y / 6371000.0)) / M_PI * 360 - 90;
	return lat;
}

double transform_to_geo_lon(int x)
{
	double lon = (x * 0.00000899322);
	return lon;
}

int transform_from_geo_lat(double lat)
{
	/* slower */
	// int ret = log(tan(M_PI_4 + lat * M_PI / 360)) * 6371000.0;
	//fprintf(stderr, "y=%d\n", ret);
	/* slower */

	/* fast */
	int ret = log(tan(M_PI_4 + lat * 0.008726646259971647884618)) * 6371000.0; // already calced (M_PI/360)
	/* fast */

	return ret;
}

int transform_from_geo_lon(double lon)
{
	/* slower */
	//int ret = lon * 6371000.0 * M_PI / 180;
	// fprintf(stderr, "x=%d\n", ret);
	/* slower */

	/* fast */
	int ret = lon * 111194.9266445587373; // already calced (6371000.0*M_PI/180)
	/* fast */

	return ret;
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

	/* slower */
	// ni->c.x = lon * 6371000.0 * M_PI / 180;
	// ni->c.y = log(tan(M_PI_4 + lat * M_PI / 360)) * 6371000.0;
	/* slower */

	/* fast */
	ni->c.x = lon * 111194.9266445587373; // already calced (6371000.0*M_PI/180)
	ni->c.y = log(tan(M_PI_4 + lat * 0.008726646259971647884618)) * 6371000.0; // already calced (M_PI/360)
	/* fast */

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
node_item_get_fast(int id, int local_thread_num)
{
	int i;
	void *p_tmp;
	int rr;
	size_t data_size2 = sizeof(int *);
	struct node_item *ni = (struct node_item *) (node_buffer[local_thread_num].base);

	//if (node_hash[local_thread_num])
	if (node_hash_cfu[local_thread_num])
	{
		//p_tmp = (gpointer)(g_hash_table_lookup(node_hash[local_thread_num], (gpointer) (long) id));
		rr = cfuhash_get_data(node_hash_cfu[local_thread_num], (long long) id, sizeof(long long), &p_tmp, &data_size2);
		if (rr == 1)
		{
			// printf("got value %ld\n", p_tmp);
			i = p_tmp;
			return ni + i;
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

static struct node_item *
node_item_get(int id, int local_thread_num)
{
	struct node_item *ni = (struct node_item *) (node_buffer[local_thread_num].base);
	int count = node_buffer[local_thread_num].size / sizeof(struct node_item);
	int interval = count / 4;
	int p = count / 2;
	if (interval == 0)
	{
		// If fewer than 4 nodes defined so far set interval to 1 to
		// avoid infinite loop
		interval = 1;
	}

	if (node_hash[local_thread_num])
	{
		int i;
		void* p_tmp = (gpointer)(g_hash_table_lookup(node_hash[local_thread_num], (gpointer) (long) id));
		if (p_tmp != NULL)
		{
			// only if node is found in hash!
			i = (int) (long) (p_tmp);
			return ni + i;
		}
	}

	fprintf(stderr, "[THREAD] #%d **BAD**\n", local_thread_num);
	// return NULL;

	if (ni[0].id > id)
	{
		return NULL;
	}

	if (ni[count - 1].id < id)
	{
		return NULL;
	}

	while (ni[p].id != id)
	{
#if 0
		fprintf(stderr,"p=%d count=%d interval=%d id=%d ni[p].id=%d\n", p, count, interval, id, ni[p].id);
#endif
		if (ni[p].id < id)
		{
			p += interval;
			if (interval == 1)
			{
				if (p >= count)
					return NULL;

				if (ni[p].id > id)
					return NULL;
			}
			else
			{
				if (p >= count)
					p = count - 1;
			}
		}
		else
		{
			p -= interval;
			if (interval == 1)
			{
				if (p < 0)
					return NULL;
				if (ni[p].id < id)
					return NULL;
			}
			else
			{
				if (p < 0)
					p = 0;
			}
		}

		if (interval > 1)
			interval /= 2;
	}

	return &ni[p];
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

void osm_add_way(osmid id)
{
	static int wayid_last;

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
		cfuhash_set_flag(node_hash_cfu, CFUHASH_FROZEN);

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
	item_bin_init(item_bin_2, type_none);
	item_bin_add_attr_longlong(item_bin_2, attr_osm_relationid, current_id);
}

static int country_id_from_iso2(char *iso)
{
	int ret = 0;
	if (iso)
	{
		// make lowercase
		int i99;
		for (i99 = 0; iso[i99]; i99++)
		{
			iso[i99] = tolower(iso[i99]);
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

static struct country_table *
country_from_countryid(int id)
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
			save_relation = 1;
			save_relations_other_method = 1;
			tag_id = 5;
		}
		else if (!strcmp(str, "natural=wood"))
		{
			save_relation = 1;
			save_relations_other_method = 1;
			tag_id = 6;
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
										// we have close the poly, but there are still "ways" left
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
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "some problem with this multipolygon\n");
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
						fprintf(stderr, "relation id=%lld\n", current_id);
					fprintf(stderr, "Got Exception 1!\n");
					exception = 1;
				}
				CATCH( MAPTOOL_00002_EXCEPTION )
				{
						fprintf(stderr, "relation id=%lld\n", current_id);
					fprintf(stderr, "Got segv Exception 1!\n");
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
											fprintf(stderr, "relation id=%lld\n", current_id);
											fprintf(stderr, "problem with hole!\n");
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
												fprintf(stderr, "relation id=%lld\n", current_id);
												fprintf(stderr, "Got Exception A!\n");
												exception = 1;
											}
											CATCH( MAPTOOL_00002_EXCEPTION )
											{
												fprintf(stderr, "relation id=%lld\n", current_id);
												fprintf(stderr, "Got segv Exception A!\n");
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
								fprintf(stderr, "relation id=%lld\n", current_id);
								fprintf(stderr, "Got Exception 8!\n");
								exception = 1;
							}
							CATCH( MAPTOOL_00002_EXCEPTION )
							{
								fprintf(stderr, "relation id=%lld\n", current_id);
								fprintf(stderr, "Got segv Exception 8!\n");
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
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got Exception 2! (in triangulate)\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got segv Exception 2! (in triangulate)\n");
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

							// add dummy osm wayid
							item_bin_add_attr_longlong(item_bin_tri, attr_osm_wayid, current_id);
							coord_tri.x = transform_from_geo_lon(pt1->c.x);
							coord_tri.y = transform_from_geo_lat(pt1->c.y);
							item_bin_add_coord(item_bin_tri, &coord_tri, 1);
							coord_tri.x = transform_from_geo_lon(pt2->c.x);
							coord_tri.y = transform_from_geo_lat(pt2->c.y);
							item_bin_add_coord(item_bin_tri, &coord_tri, 1);
							coord_tri.x = transform_from_geo_lon(pt3->c.x);
							coord_tri.y = transform_from_geo_lat(pt3->c.y);
							item_bin_add_coord(item_bin_tri, &coord_tri, 1);

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
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got Exception 3!\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got segv Exception 3!\n");
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
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got Exception 4!\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got segv Exception 4!\n");
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
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got Exception 5!\n");
						exception = 1;
					}
					CATCH( MAPTOOL_00002_EXCEPTION )
					{
						fprintf(stderr, "relation id=%lld\n", current_id);
						fprintf(stderr, "Got segv Exception 5!\n");
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

						if (!strcmp(memb.role, "label"))
						{
							fprintf(stderr, "town to relation: rel-name:%s r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), current_id, memb.id);
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

						if (!strcmp(memb.role, "admin_centre"))
						{
							fprintf(stderr, "town to relation: rel-name:%s r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), current_id, memb.id);
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
							fprintf(stderr, "town to relation: rel-name:%s r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), current_id, memb.id);
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
			fprintf(stderr, "town to relation(*): rel-name:%s admlevel:%d r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), admin_level, current_id, dummy_town_id);
			sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
			// fprintf(stderr, "ret=%d\n", rr);
			sqlite3_bind_int64(stmt_town_sel008, 1, current_id);
			sqlite3_bind_int64(stmt_town_sel008, 2, dummy_town_id);
			sqlite3_bind_int(stmt_town_sel008, 3, admin_level);
			sqlite3_step(stmt_town_sel008);
			//fprintf(stderr, "ret=%d\n", rr);
			sqlite3_reset(stmt_town_sel008);
			//fprintf(stderr, "ret=%d\n", rr);


			str = NULL;
			while ((str = item_bin_get_attr(item_bin_2, attr_osm_member, str)))
			{
				if (get_relation_member(str, &memb))
				{
					if ((memb.type == 2) && (!strcmp(memb.role, "outer")))
					{
						// way-id is "memb.id"
						// use the first nodes coords
						get_lat_lon_way_first_node(memb.id, &node_coords);
						if (node_coords.valid == 1)
						{
							sqlite3_bind_double(stmt_town, 6, node_coords.lat);
							sqlite3_bind_double(stmt_town, 7, node_coords.lon);
						}

						add_boundary_to_db(current_id, admin_level, item_bin_2, &memb);

						break;
					}
				}
			}

			sqlite3_bind_int64(stmt_town, 1, dummy_town_id);
			sqlite3_bind_int(stmt_town, 2, 999);
			sqlite3_bind_int(stmt_town, 4, (TOWN_ADMIN_LEVEL_CORR_BASE + admin_level) * TOWN_BY_BOUNDARY_SIZE_FACTOR);
			sqlite3_bind_text(stmt_town, 3, osm_tag_value(item_bin_2, "name"), -1, SQLITE_STATIC);
			sqlite3_bind_text(stmt_town, 5, NULL, -1, SQLITE_STATIC);
			sqlite3_bind_int64(stmt_town, 8, current_id);
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
						fprintf(stderr, "town to relation(+): rel-name:%s admlevel:%d r:%lld t:%lld\n",osm_tag_value(item_bin_2, "name"), admin_level, current_id, 9797979797);

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
	struct boundary *b;
	int admin_l_boundary;
	int max_admin_l;

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

				//fprintf(stderr, "correct_boundary_ref_point: relid:%lld(%d) x=%d y=%d\n", rid, admin_l, c.x, c.y);

				// find this boundary in list
				l = bl;
				match = NULL;
				while (l)
				{
					b = l->data;
					//fprintf(stderr, "correct_boundary_ref_point: %lld %lld\n", item_bin_get_relationid(b->ib), rid);
					if (item_bin_get_relationid(b->ib) == rid)
					{
						match = l;
						break;
					}
					l = g_list_next(l);
				}

				if (match)
				{
					b = match->data;
					//fprintf(stderr, "correct_boundary_ref_point: relid:%lld(%d) parentid:%lld(%d)\n", rid, admin_l, item_bin_get_relationid(b->ib), max_admin_l);

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
						sqlite3_bind_double(stmt_bd_004, 1, lat);
						sqlite3_bind_double(stmt_bd_004, 2, lon);
						sqlite3_bind_int64(stmt_bd_004, 3, rid);
						sqlite3_step(stmt_bd_004);
						sqlite3_reset(stmt_bd_004);
					}
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
	//fprintf(stderr, "2 c=%d y=%d\n", c.x, c.y);
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
	//fprintf(stderr, "3 c=%d y=%d\n", c.x, c.y);
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
	//fprintf(stderr, "4 c=%d y=%d\n", c.x, c.y);
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
	//fprintf(stderr, "5 c=%d y=%d\n", c.x, c.y);
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
	//fprintf(stderr, "6 c=%d y=%d\n", c.x, c.y);
	matches = NULL;
	matches = boundary_find_matches_single(bl, &c);
	if (g_list_length(matches) > 0)
	{
		*lon = lon2 - lon_d;
		*lat = lat2;
		return 1;
	}

	c.x = transform_from_geo_lon(lon2 - lon_d);
	c.y = transform_from_geo_lat(lat2);
	//fprintf(stderr, "7 c=%d y=%d\n", c.x, c.y);
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
	//fprintf(stderr, "8 c=%d y=%d\n", c.x, c.y);
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
	//fprintf(stderr, "9 c=%d y=%d\n", c.x, c.y);
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

void build_boundary_tree(GList *bl)
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
}

void add_boundary_to_db(long long current_id, int admin_level, struct item_bin *item_bin_3, struct relation_member *memb)
{
	char *str = NULL;
	struct node_lat_lon node_coords;

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

	in_way = 0;

	if (!osm->ways)
	{
		return;
	}

	/*
	 if (dedupe_ways_hash)
	 {
	 if (g_hash_table_lookup(dedupe_ways_hash, (gpointer) (long) wayid))
	 {
	 return;
	 }
	 g_hash_table_insert(dedupe_ways_hash, (gpointer) (long) wayid, (gpointer) 1);
	 }
	 */

	ways_processed_count++;

	count = attr_longest_match(attr_mapping_way, attr_mapping_way_count, types, sizeof(types) / sizeof(enum item_type));
	if (!count)
	{
		count = 1;
		types[0] = type_street_unkn;
	}

	if (count >= 10)
	{
		fprintf(stderr, "way id %ld\n", osmid_attr_value);
		dbg_assert(count < 10);
	}

	if (attr_strings[attr_string_label] != NULL)
	{
		if (strlen(attr_strings[attr_string_label]) > 0)
		{
			dont_save_to_db = 0;
		}
	}

	for (i = 0; i < count; i++) // loop thru all the attributes of this way (and add a way for every attribute known -> so ways get duplicated here!!)
	{
		add_flags = 0;
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
		nodes_ref_item_bin(item_bin, 0);
		def_flags = item_get_default_flags(types[i]);

		// save nodes of way -----------------
		// save nodes of way -----------------
		// * now save ALL way_nodes to DB !! could be a very big DB !!
		if (first == 1)
		{
			first = 0;
			int i788;
			struct coord *c788 = (struct coord *) (item_bin + 1); // set pointer to coord struct of this item
			for (i788 = 0; i788 < item_bin->clen / 2; i788++)
			{
				if (i788 == 0)
				{
					// save lat,lon of first node of this way
					if (dont_save_to_db == 0)
					{

#ifdef MAPTOOL_USE_SQL

						//fprintf(stderr, "insert -w\n");

						get_lat_lon_for_node(REF(c788[i788]), &node_coords);

						if (sql_counter == 0)
						{
							retval = sqlite3_exec(sql_handle, "BEGIN", 0, 0, 0);
							if ((retval > 0) && (retval < 100))
							{
								fprintf(stderr, "begin: ways code:%d\n", retval);
							}
							else
							{
								//fprintf(stderr, "begin: ways\n");
							}
						}

						sql_counter++;

						sqlite3_bind_int64(stmt_way, 1, osmid_attr_value);
						sqlite3_bind_text(stmt_way, 2, attr_strings[attr_string_label], -1, SQLITE_STATIC);

						// streetname folded for later sort/search
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
							}
							else
							{
								sqlite3_bind_text(stmt_way, 6, name_folded, -1, SQLITE_STATIC);
							}
						}
						else
						{
							// use original string
							sqlite3_bind_text(stmt_way, 6, attr_strings[attr_string_label], -1, SQLITE_STATIC);
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

						if (sql_counter > MAX_ROWS_WO_COMMIT_3)
						{
							sql_counter = 0;
							retval = sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);
							if ((retval > 0) && (retval < 100))
							{
								fprintf(stderr, "ways: %lld code:%d\n", ways_processed_count, retval);
							}
							else
							{
								//fprintf(stderr, "ways: %lld\n", ways_processed_count);
							}
						}

#endif

					}

				}

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
				// fprintf(stderr, "insert #WN\n");

#ifdef MAPTOOL_SPLIT_WAYNODE_DB
				if ((osmid_attr_value & MAPTOOL_SPLIT_WAYNODE_BIT) > 0)
				{
					if ((osmid_attr_value & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
					{
#endif
						sqlite3_bind_int64(stmt_way_node, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_node, 2, REF(c788[i788]));
						sqlite3_bind_int64(stmt_way_node, 3, (i788 + 1));
						sqlite3_step(stmt_way_node);
						sqlite3_reset(stmt_way_node);
#ifdef MAPTOOL_SPLIT_WAYNODE_DB
					}
					else
					{
						sqlite3_bind_int64(stmt_way_nodeb, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_nodeb, 2, REF(c788[i788]));
						sqlite3_bind_int64(stmt_way_nodeb, 3, (i788 + 1));
						sqlite3_step(stmt_way_nodeb);
						sqlite3_reset(stmt_way_nodeb);
					}
				}
				else
				{
					if ((osmid_attr_value & MAPTOOL_SPLIT_WAYNODE_BIT2) > 0)
					{
						sqlite3_bind_int64(stmt_way_node__2, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_node__2, 2, REF(c788[i788]));
						sqlite3_bind_int64(stmt_way_node__2, 3, (i788 + 1));
						sqlite3_step(stmt_way_node__2);
						sqlite3_reset(stmt_way_node__2);
					}
					else
					{
						sqlite3_bind_int64(stmt_way_node__2b, 1, osmid_attr_value);
						sqlite3_bind_int64(stmt_way_node__2b, 2, REF(c788[i788]));
						sqlite3_bind_int64(stmt_way_node__2b, 3, (i788 + 1));
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
					//fprintf(stderr, "way nodes: %lld\n", sql_counter4);
					sql_counter4 = 0;
				}
#endif
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

		item_bin_add_attr_longlong(item_bin, attr_osm_wayid, osmid_attr_value);
		if (debug_attr_buffer[0])
			item_bin_add_attr_string(item_bin, attr_debug, debug_attr_buffer);
		if (add_flags)
			item_bin_add_attr_int(item_bin, attr_flags, flags_attr_value);
		if (maxspeed_attr_value)
			item_bin_add_attr_int(item_bin, attr_maxspeed, maxspeed_attr_value);


		// if we duplicated this way here (because of multiple attributes), then set "duplicate_way" attr
		if (i > 0)
		{
			item_bin_add_attr_int(item_bin, attr_duplicate_way, 1);
		}

		item_bin_write(item_bin, osm->ways);

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

void osm_append_housenumber_node(FILE *out, struct coord *c, char *house_number, char *street_name)
{
	struct item_bin *item_bin;
	item_bin = init_item(type_house_number, 0);
	// item_bin->type = type_house_number;

	item_bin_add_coord(item_bin, c, 1);
	item_bin_add_attr_string(item_bin, attr_house_number, house_number);
	item_bin_add_attr_string(item_bin, attr_street_name, street_name);

	// DEBUG -- DEBUG -- DEBUG --
	// DEBUG -- DEBUG -- DEBUG --
	// dump_itembin(item_bin);
	// DEBUG -- DEBUG -- DEBUG --
	// DEBUG -- DEBUG -- DEBUG --

	item_bin_write(item_bin, out);
}

void osm_end_node(struct maptool_osm *osm)
{
	int count, i;
	char *postal;
	enum item_type types[10];
	struct item_bin *item_bin;
	in_node = 0;

	if (!osm->nodes || !node_is_tagged || !nodeid)
		return;

	count = attr_longest_match(attr_mapping_node, attr_mapping_node_count, types, sizeof(types) / sizeof(enum item_type));

	if (!count)
	{
		types[0] = type_point_unkn;
		count = 1;
	}

	dbg_assert(count < 10);

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

		if (item_is_town(*item_bin) && attr_strings[attr_string_population])
		{
			item_bin_set_type_by_population(item_bin, atoi(attr_strings[attr_string_population]));
		}

		item_bin_add_coord(item_bin, &ni->c, 1);
		item_bin_add_attr_string(item_bin, item_is_town(*item_bin) ? attr_town_name : attr_label, attr_strings[attr_string_label]);
		item_bin_add_attr_string(item_bin, attr_house_number, attr_strings[attr_string_house_number]);
		item_bin_add_attr_string(item_bin, attr_street_name, attr_strings[attr_string_street_name]);
		item_bin_add_attr_string(item_bin, attr_phone, attr_strings[attr_string_phone]);
		item_bin_add_attr_string(item_bin, attr_fax, attr_strings[attr_string_fax]);
		item_bin_add_attr_string(item_bin, attr_email, attr_strings[attr_string_email]);
		item_bin_add_attr_string(item_bin, attr_county_name, attr_strings[attr_string_county_name]);
		item_bin_add_attr_string(item_bin, attr_url, attr_strings[attr_string_url]);
		item_bin_add_attr_longlong(item_bin, attr_osm_nodeid, osmid_attr_value);
		item_bin_add_attr_string(item_bin, attr_debug, debug_attr_buffer);

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

		if (item_is_town(*item_bin) && attr_strings[attr_string_label] && osm->towns)
		{
			item_bin = init_item(item_bin->type, 0);
			item_bin_add_coord(item_bin, &ni->c, 1);
			item_bin_add_attr_string(item_bin, attr_osm_is_in, is_in_buffer);
			item_bin_add_attr_longlong(item_bin, attr_osm_nodeid, osmid_attr_value);
			item_bin_add_attr_string(item_bin, attr_town_postal, postal);
			item_bin_add_attr_string(item_bin, attr_county_name, attr_strings[attr_string_county_name]);
			item_bin_add_attr_string(item_bin, attr_town_name, attr_strings[attr_string_label]);
			if (attr_strings[attr_string_label_alt])
			{
				item_bin_add_attr_string(item_bin, attr_town_name_match, attr_strings[attr_string_label_alt]);
				if (debug_itembin(item_bin))
				{
					fprintf(stderr, "town name    : %s\n", attr_strings[attr_string_label]);
					fprintf(stderr, "town name alt: %s\n", attr_strings[attr_string_label_alt]);
				}
			}

			item_bin_write(item_bin, osm->towns);
		}

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
		return match->country;
	}
	else
	{
		//fprintf(stderr,"town_by_boundary:099\n");
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
		// case type_town_label_2e2:
		case type_town_label_1e2:
		case type_town_label_5e1:
		case type_town_label_2e1:
		case type_town_label_1e1:
		case type_town_label_5e0:
		// case type_town_label_2e0:
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
void assign_town_to_streets_by_boundary(GList *bl);
void copy_town_border_data();

void assign_town_to_streets(GList *bl)
{
	int skipped = 0;

	copy_town_border_data();
	assign_town_to_streets_by_boundary(bl);

	// now process all the left over towns
	assign_town_to_streets_v1(1);
	assign_town_to_streets_v1(2); // make townsize double on second pass
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
	//fprintf(stderr, "prep:%d\n", retval);
	retval = sqlite3_prepare_v2(sql_handle, "update town set border_id = ?, size = ? where id = ?;", -1, &stmt_d_2, NULL);
	//fprintf(stderr, "prep:%d\n", retval);

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

				//fprintf(stderr, "bid=%lld tid=%lld amdin_level=%d\n", bid, tid, admin_l);

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
	//fprintf(stderr, "fin:%d\n", retval);
	retval = sqlite3_finalize(stmt_d_1);
	//fprintf(stderr, "fin:%d\n", retval);

	sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);

}

void assign_town_to_streets_by_boundary(GList *bl)
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

	fprintf(stderr, "towns0: %lld/%lld\n", town_processed_count, town_count);

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

				if (town_rel_id != 0)
				{
					// fprintf(stderr ,"processing town name:%s id:%lld border_id:%lld\n", sqlite3_column_text(stmt_town_sel001, 4), nd, town_rel_id);

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

								//fprintf(stderr,"== street by boundary == x:%d y:%d ==\n", c.x, c.y);

								if (town_rel_id != 0)
								{
									// fprintf(stderr, "town:%lld\n", nd);
									// fprintf(stderr,"== street by boundary == x:%d y:%d ==\n", c.x, c.y);
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
					fprintf(stderr, "towns0: %lld/%lld\n", town_processed_count, town_count);
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

	fprintf(stderr, "towns1: %lld/%lld\n", town_processed_count, town_count);

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
					fprintf(stderr, "towns1: %lld/%lld\n", town_processed_count, town_count);
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

GList* osm_process_towns(FILE *in, FILE *coords, FILE *boundaries, FILE *ways)
{
	struct item_bin *ib;
	GList *bl = NULL;
	struct attr attrs[10];
	time_t start_tt, end_tt;
	double diff_tt;
	double diff2_tt;
	long long size_in;
	long long pos_in;
	struct node_lat_lon node_coords;

	if (debug_itembin(1))
	{
		fprintf(stderr, "osm_process_towns == START ==\n");
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
	fseeko(in, pos_now, SEEK_SET);

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
			//char *name=item_bin_get_attr(ib, attr_town_name, NULL);
			//fprintf(stderr,"== town by boundary == t:%s ==\n", name);

			result = osm_process_town_by_boundary(bl, ib, c, attrs);
			if (result) // DEBUG
			{
				//fprintf(stderr,"== town by boundary == country_id:%d country_name:%s ==\n", result->countryid, result->names);
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
			fprintf(stderr, "-RUNTIME-LOOP-TOWN: %s elapsed (POS:%s of %s)\n", outstring, outstring2, outstring3);
			if (pos_in > 0)
			{
				double eta_time = (diff_tt / (pos_in)) * (size_in);
				convert_to_human_time(eta_time, outstring);
				fprintf(stderr, "-RUNTIME-LOOP-TOWN: %s left\n", outstring);
			}
		}
	}

	if (debug_itembin(1))
	{
		fprintf(stderr, "osm_process_towns == END ==\n");
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
	size_t data_size2 = sizeof(int *);

	fprintf(stderr, "seek_to_way ---\n");

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
	}turn_restriction->c[type]=g_renew(struct coord, turn_restriction->c[type], turn_restriction->c_count[type]+count);
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
		ni->ref_way++;
	}
}

static void nodes_ref_item_bin(struct item_bin *ib, int local_thread_num)
{
	int i;
	struct coord *c = (struct coord *) (ib + 1); // set pointer to coord struct of this item
	for (i = 0; i < ib->clen / 2; i++)
	{
		node_ref_way(REF(c[i]), local_thread_num); // give c[i].y as parameter (which seems to be NODE ID)
	}

	//fprintf(stderr,"********DUMP rw ***********\n");
	//dump_itembin(ib);
	//fprintf(stderr,"********DUMP rw ***********\n");

}

void osm_add_nd(osmid ref)
{
	SET_REF(coord_buffer[coord_count], ref);
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
				cfuhash_put_data(way_hash_cfu, (long long) idx[0], sizeof(long long), (long) idx[1], 1, NULL);
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

	fseek(in, 0, SEEK_SET);
	while ((ib = read_item(in, local_thread_num))) // loop all "ways" from file "in"
	{
		nodes_ref_item_bin(ib, local_thread_num);
		ways_count++;

		if ((ways_count % 1000000) == 0)
		{
			fprintf(stderr, "[THREAD] #%d ways: %lld\n", local_thread_num, ways_count);
		}
	}
}

/*
 void resolve_ways(FILE *in, FILE *out)
 {
 struct item_bin *ib;
 struct coord *c;
 int i;
 struct node_item *ni;

 fseek(in, 0, SEEK_SET);
 while ((ib = read_item(in)))
 {
 c = (struct coord *) (ib + 1);
 for (i = 0; i < ib->clen / 2; i++)
 {
 if (!IS_REF(c[i]))
 continue;
 ni = node_item_get(REF(c[i]));
 if (ni)
 {
 c[i].x = ni->c.x;
 c[i].y = ni->c.y;
 }
 }
 item_bin_write(ib, out);
 }
 }
 */

/*
 FILE *
 resolve_ways_file(FILE *in, char *suffix, char *filename)
 {
 char *newfilename = g_strdup_printf("%s_new", filename);
 FILE *new = tempfile(suffix, newfilename, 1);
 resolve_ways(in, new);
 fclose(in);
 fclose(new);
 tempfile_rename(suffix, newfilename, filename);
 g_free(newfilename);
 return tempfile(suffix, filename, 0);
 }
 */

/**
 * Get POI coordinates from area/line coordinates.
 * @param in *in input file with area/line coordinates.
 * @param in *out output file with POI coordinates
 * @param in type input file original contents type: type_line or type_area
 * @returns nothing
 */
// --- NOT NEEDED ------
// --- NOT NEEDED ------
void process_way2poi(FILE *in, FILE *out, int type)
{
	struct item_bin *ib;
	while ((ib = read_item(in, 0)))
	{
		int count = ib->clen / 2;
		if (count > 1 && ib->type < type_line)
		{
			struct coord *c = (struct coord *) (ib + 1), c1, c2;
			int done = 0;
			if (type == type_area)
			{
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
						c[0] = c1;
					}
					else
					{
						geom_poly_closest_point(c, count, &c1, &c2);
						c[0] = c2;
					}
					done = 1;
				}
			}
			if (!done)
			{
				geom_line_middle(c, count, &c1);
				c[0] = c1;
			}
			write_item_part(out, NULL, NULL, ib, 0, 0, NULL);
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

#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
typedef unsigned char uint8;
typedef unsigned short uint16;
typedef unsigned int uint;

#define MZ_MIN(a,b) (((a)<(b))?(a):(b))
#define my_min(a,b) (((a) < (b)) ? (a) : (b))




// -------------------------------
// -------------------------------

int IN_BUF_SIZE;
static uint8* s_inbuf;
int OUT_BUF_SIZE;
static uint8* s_outbuf;
int COMP_OUT_BUF_SIZE;

int s_IN_BUF_SIZE = sizeof(struct streets_index_data_block) * 1024;
int t_IN_BUF_SIZE = sizeof(struct town_index_data_block) * 1024;
static uint8 s_s_inbuf[(sizeof(struct streets_index_data_block) * 1024)];
static uint8 t_s_inbuf[(sizeof(struct town_index_data_block) * 1024)];

int s_OUT_BUF_SIZE = sizeof(struct streets_index_data_block) * 1024;
int t_OUT_BUF_SIZE = sizeof(struct town_index_data_block) * 1024;
static uint8 s_s_outbuf[(sizeof(struct streets_index_data_block) * 1024)];
static uint8 t_s_outbuf[(sizeof(struct town_index_data_block) * 1024)];

int s_COMP_OUT_BUF_SIZE;
int t_COMP_OUT_BUF_SIZE;

// IN_BUF_SIZE is the size of the file read buffer.
// IN_BUF_SIZE must be >= 1
//**#define IN_BUF_SIZE (1024*512)
//**static uint8 s_inbuf[IN_BUF_SIZE];

// COMP_OUT_BUF_SIZE is the size of the output buffer used during compression.
// COMP_OUT_BUF_SIZE must be >= 1 and <= OUT_BUF_SIZE
//**#define COMP_OUT_BUF_SIZE (1024*512)

// OUT_BUF_SIZE is the size of the output buffer used during decompression.
// OUT_BUF_SIZE must be a power of 2 >= TINFL_LZ_DICT_SIZE (because the low-level decompressor not only writes, but reads from the output buffer as it decompresses)
//#define OUT_BUF_SIZE (TINFL_LZ_DICT_SIZE)
//*#define OUT_BUF_SIZE (1024*512)
//*static uint8 s_outbuf[OUT_BUF_SIZE];

// -------------------------------
// -------------------------------


// compression level
int street_index_compress_level = 9; // = MZ_BEST_COMPRESSION

long long compress_file(char *in, char *out, int keep_tempfile)
{
	long long out_size = 0;
	// compress structure
	tdefl_compressor *g_deflator;
	g_deflator = g_new0(tdefl_compressor, 1);
	// compress structure

	// ok now compress the block (file) -------------------------------
	const void *next_in = s_inbuf;
	size_t avail_in = 0;
	void *next_out = s_outbuf;
	size_t avail_out = OUT_BUF_SIZE;
	size_t total_in = 0, total_out = 0;

	FILE *in_uncompr = tempfile("", in, 0);
	FILE *out_compr = NULL;
	out_compr = tempfile("", out, 1);

	// Determine input file's size.
	fseek(in_uncompr, 0, SEEK_END);
	long file_loc = ftell(in_uncompr);
	fseek(in_uncompr, 0, SEEK_SET);
	uint infile_size = (uint)file_loc;

	// The number of dictionary probes to use at each compression level (0-10). 0=implies fastest/minimal possible probing.
	static const mz_uint s_tdefl_num_probes[11] =
	{	0, 1, 6, 32, 16, 32, 128, 256, 512, 768, 1500};

	tdefl_status status;
	uint infile_remaining = infile_size;

	// create tdefl() compatible flags (we have to compose the low-level flags ourselves, or use tdefl_create_comp_flags_from_zip_params() but that means MINIZ_NO_ZLIB_APIS can't be defined).
	mz_uint comp_flags = TDEFL_WRITE_ZLIB_HEADER | s_tdefl_num_probes[MZ_MIN(10, street_index_compress_level)] | ((street_index_compress_level <= 3) ? TDEFL_GREEDY_PARSING_FLAG : 0);
	if (!street_index_compress_level)
	{
		comp_flags |= TDEFL_FORCE_ALL_RAW_BLOCKS;
	}

	// Initialize the low-level compressor.
	status = tdefl_init(g_deflator, NULL, NULL, comp_flags);
	if (status != TDEFL_STATUS_OKAY)
	{
		fprintf(stderr, "tdefl_init() failed!\n");
		g_free(g_deflator);
		return 0;
	}
	avail_out = COMP_OUT_BUF_SIZE;

	// Compression.
	for (;; )
	{
		size_t in_bytes, out_bytes;

		if (!avail_in)
		{
			// Input buffer is empty, so read more bytes from input file.
			uint n = my_min(IN_BUF_SIZE, infile_remaining);

			if (fread(s_inbuf, 1, n, in_uncompr) != n)
			{
				fprintf(stderr, "Failed reading from input file!\n");
				g_free(g_deflator);
				return 0;
			}

			next_in = s_inbuf;
			avail_in = n;

			infile_remaining -= n;
			//printf("Input bytes remaining: %u\n", infile_remaining);
		}

		in_bytes = avail_in;
		out_bytes = avail_out;
		// Compress as much of the input as possible (or all of it) to the output buffer.
		status = tdefl_compress(g_deflator, next_in, &in_bytes, next_out, &out_bytes, infile_remaining ? TDEFL_NO_FLUSH : TDEFL_FINISH);

		next_in = (const char *)next_in + in_bytes;
		avail_in -= in_bytes;
		total_in += in_bytes;

		next_out = (char *)next_out + out_bytes;
		avail_out -= out_bytes;
		total_out += out_bytes;

		if ((status != TDEFL_STATUS_OKAY) || (!avail_out))
		{
			// Output buffer is full, or compression is done or failed, so write buffer to output file.
			uint n = COMP_OUT_BUF_SIZE - (uint)avail_out;
			if (fwrite(s_outbuf, 1, n, out_compr) != n)
			{
				fprintf(stderr, "Failed writing to output file!\n");
				g_free(g_deflator);
				return 0;
			}
			next_out = s_outbuf;
			avail_out = COMP_OUT_BUF_SIZE;
		}

		if (status == TDEFL_STATUS_DONE)
		{
			// Compression completed successfully.
			break;
		}
		else if (status != TDEFL_STATUS_OKAY)
		{
			// Compression somehow failed.
			fprintf(stderr, "tdefl_compress() failed with status %i!\n", status);
			g_free(g_deflator);
			return 0;
		}
	}

	fprintf(stderr, "Total input bytes: %u\n", (mz_uint32)total_in);
	fprintf(stderr, "Total output bytes: %u\n", (mz_uint32)total_out);

	out_size = (long long)total_out;

	fclose(in_uncompr);
	fclose(out_compr);

	if (keep_tempfile == 1)
	{
		char *in2;
		in2 = g_strdup_printf("%s.tmptmp", in);
		tempfile_rename("", in, in2);
		g_free(in2);
		tempfile_rename("", out, in);
	}
	else
	{
		tempfile_unlink("", in);
		tempfile_rename("", out, in);
	}

	g_free(g_deflator);

	return out_size;
}
#endif

void generate_combined_index_file(FILE *towni, FILE *streeti, FILE *out)
{
#ifdef MAPTOOL_USE_SQL
	fseek(towni, 0, SEEK_END);
	fseek(streeti, 0, SEEK_END);
	long long towni_size = (long long)ftello(towni);
	long long streeti_size = (long long)ftello(streeti);
	fprintf(stderr, "ftell towns=%lld\n", towni_size);
	fprintf(stderr, "ftell streets=%lld\n", streeti_size);

	fseek(towni, 0, SEEK_SET);
	fseek(streeti, 0, SEEK_SET);

	// size
	fwrite(&streeti_size, sizeof(long long), 1, out);
	// append street index file
	filecopy(streeti, out);
	// append town index file
	filecopy(towni, out);

#endif
}

char* get_town_name_recursive(long long border_id, int level)
{
	char *ret = NULL;
	char *ret2 = NULL;
	char *str2 = NULL;
	int rc = 0;
	long long parent_rel_id = 0;

	//fprintf(stderr, "get town name:bid:%lld level:%d\n", border_id, level);

	// select this boundary
	sqlite3_bind_int64(stmt_bd_005, 1, border_id);
	rc = sqlite3_step(stmt_bd_005);
	switch (rc)
	{
		case SQLITE_DONE:
			break;
		case SQLITE_ROW:
			// rel_id, parent_rel_id, name
			parent_rel_id = sqlite3_column_int(stmt_bd_005, 1);
			ret = g_strdup_printf("%s", sqlite3_column_text(stmt_bd_005, 2));
			sqlite3_reset(stmt_bd_005);

			//fprintf(stderr, "get town name:ret=%s\n", ret);

			if (level < 12)
			{
				str2 = get_town_name_recursive(parent_rel_id, (level + 1));
				if (str2)
				{
					ret2 = g_strdup_printf("%s, %s", ret, str2);
					g_free(ret);
					g_free(str2);
					ret = ret2;
				}
			}
			break;
		default:
			fprintf(stderr, "SQL Error: %d\n", rc);
			break;
	}

	if (level == 0)
	{
		sqlite3_reset(stmt_bd_005);
	}

	return ret;
}

void generate_town_index_file(FILE *out)
{
#ifdef MAPTOOL_USE_SQL

	struct town_index_data_block db;
	struct town_index_index_block_start is;
	struct town_index_index_block ib;
	char *townname = NULL;
	char *townname2 = NULL;
	long long last_len = 0;
	long long border_id = 0;
	int town_count = 0;
	int index_blocks;
	int index_block_towns;
	int i;
	int first;
	int rc = 0;
	int current_index_block;
	int current_index_block_old;
	char tmp_letter[TOWN_INDEX_TOWN_NAME_SIZE];
	char *newfilename = NULL;
	char *newfilename_compr = NULL;

	int chunkSize;
	int stringLength;
	int ii22;

	FILE *town_index_index = NULL;
	FILE *town_index_index_data_block = NULL;


	// init compression for towns
	// init compression for towns
	IN_BUF_SIZE = t_IN_BUF_SIZE;
	s_inbuf = t_s_inbuf;
	OUT_BUF_SIZE = t_OUT_BUF_SIZE;
	s_outbuf = t_s_outbuf;
	COMP_OUT_BUF_SIZE = t_OUT_BUF_SIZE;
	// init compression for towns
	// init compression for towns


	sqlite3_step(stmt_town_sel006);
	town_count = sqlite3_column_int(stmt_town_sel006, 0);
	sqlite3_reset(stmt_town_sel006);

	// calculate number of index blocks
	index_blocks = 2;
	if (town_count > 1000000)
	{
		index_blocks = 100;
	}
	else if (town_count > 100000)
	{
		index_blocks = 50;
	}
	else if (town_count > 10000)
	{
		index_blocks = 10;
	}
	else if (town_count > 2000)
	{
		index_blocks = 5;
	}

	is.count_of_index_blocks = index_blocks;
	ib.offset = sizeof (is.count_of_index_blocks) + is.count_of_index_blocks * sizeof(struct town_index_index_block); // start offset = index size
	town_index_index = tempfile("", "town_index_index", 1);

	index_block_towns = (town_count / index_blocks);
	if ((index_block_towns * index_blocks) < town_count)
	{
		index_block_towns++;
	}

	fprintf(stderr, "towns per block=%d\n", index_block_towns);

	fprintf(stderr, "index size=%d\n", ib.offset);

	fprintf(stderr, "ftell=%d\n", ftell(town_index_index));
	fwrite(&is, sizeof(struct town_index_index_block_start), 1, town_index_index);
	fprintf(stderr, "ftell=%d\n", ftell(town_index_index));

	current_index_block = 1;
	current_index_block_old = 0;
	ib.len = 0;
	i = -1;
	first = 1;

	// loop thru all the towns
	do
	{
		rc = sqlite3_step(stmt_town_sel005);
		switch (rc)
		{
			case SQLITE_DONE:
			break;
			case SQLITE_ROW:
			i++;
			townname2 = NULL;
			db.town_id = sqlite3_column_int64(stmt_town_sel005, 0);
			db.country_id = sqlite3_column_int(stmt_town_sel005, 1);
			border_id = sqlite3_column_int64(stmt_town_sel005, 3);

			townname = get_town_name_recursive(border_id, 0);

			if (townname == NULL)
			{
				townname2 = g_strdup_printf("%s", sqlite3_column_text(stmt_town_sel005, 2));
			}
			else
			{
				townname2 = townname;
				// townname2 = g_strdup_printf("%s", townname); // dont use the column result here, or string will be double!
				// g_free(townname);
			}
			//fprintf(stderr, "indextown:%s\n", townname2);

			//fprintf(stderr, "i=%d\n", i);
			//fprintf(stderr, "block=%d\n", current_index_block);
			if ((i + 1) > index_block_towns)
			{
				// start new index data block
				i = 0;
				current_index_block++;
				//fprintf(stderr, "incr block=%d\n", current_index_block);
			}

			if (current_index_block != current_index_block_old)
			{

				if (first != 1)
				{
					// close old datafile
					fclose(town_index_index_data_block);
					town_index_index_data_block = NULL;

					if (USE_STREET_INDEX_COMPRESSION == 1)
					{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
						ib.len = compress_file(newfilename, newfilename_compr, 0);
#endif
					}

					// append to indexfile
					fprintf(stderr, "first_id=%lld offset=%lld len=%lld\n", ib.first_id, ib.offset, ib.len);
					fwrite(&ib, sizeof(struct town_index_index_block), 1, town_index_index);

					if (newfilename)
					{
						g_free(newfilename);
						newfilename = NULL;
					}

					if (newfilename_compr)
					{
						g_free(newfilename_compr);
						newfilename_compr = NULL;
					}
				}

				current_index_block_old = current_index_block;
				ib.first_id = db.town_id;
				ib.offset = ib.offset + ib.len;

				// open new datafile
				newfilename = g_strdup_printf("town_index_index_%d", current_index_block);
				fprintf(stderr, "new data file: %s first_id=%lld\n", newfilename, ib.first_id);
				newfilename_compr = g_strdup_printf("town_index_index_compr_%d", current_index_block);
				town_index_index_data_block = tempfile("", newfilename, 1);
				fprintf(stderr, "town index file %d\n", current_index_block);

				ib.len = 0;
			}


			// now check if we need to split the string into parts
			if ((strlen(townname2) + 1) > TOWN_INDEX_TOWN_NAME_SIZE)
			{
				//fprintf(stderr, " block-split: START\n");
				chunkSize = TOWN_INDEX_TOWN_NAME_SIZE - 1;
				stringLength = strlen(townname2);
				for (ii22 = 0; ii22 < stringLength ; ii22 += chunkSize)
				{
					if (ii22 + chunkSize > stringLength)
					{
						chunkSize = stringLength - ii22;
						db.town_name[chunkSize] = '\0'; // make sure string is terminated
					}
					strncpy(&db.town_name, (townname2 + ii22), chunkSize);
					db.town_name[TOWN_INDEX_TOWN_NAME_SIZE - 1] = '\0'; // make sure string is terminated
					if (ii22 > 0)
					{
						// set "split"-marker
						db.town_id = 0;
						db.country_id = 0; // setting this to zero is actually not needed
					}
					ib.len = ib.len + sizeof(struct town_index_data_block);
					//fprintf(stderr, "->block-split: town_id=%lld country_id=%d town_name=%s\n", db.town_id, db.country_id, db.town_name);
					fwrite(&db, sizeof(struct town_index_data_block), 1, town_index_index_data_block);
				}
				//fprintf(stderr, " block-split: END\n");
				g_free(townname2);
			}
			else
			{
				strncpy(&db.town_name, townname2, TOWN_INDEX_TOWN_NAME_SIZE);
				g_free(townname2);
				db.town_name[TOWN_INDEX_TOWN_NAME_SIZE - 1]= '\0'; // make sure string is terminated

				ib.len = ib.len + sizeof(struct town_index_data_block);
				fwrite(&db, sizeof(struct town_index_data_block), 1, town_index_index_data_block);
			}

			if (first == 1)
			{
				first = 0;
			}

			break;
			default:
			fprintf(stderr, "SQL Error: %d\n", rc);
			break;
		}
	}
	while (rc == SQLITE_ROW);

	sqlite3_reset(stmt_town_sel005);

	// rest of the towns
	if (i > 0)
	{
		if (town_index_index_data_block)
		{
			fclose(town_index_index_data_block);
			town_index_index_data_block = NULL;
		}

		if (USE_STREET_INDEX_COMPRESSION == 1)
		{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
			ib.len = compress_file(newfilename, newfilename_compr, 0);
#endif
		}

		// append to indexfile
		fprintf(stderr, "(last)first_id=%lld offset=%lld len=%lld\n", ib.first_id, ib.offset, ib.len);
		fwrite(&ib, sizeof(struct town_index_index_block), 1, town_index_index);
	}

	if (town_index_index != NULL)
	{
		fclose(town_index_index);
	}

	if (town_index_index_data_block != NULL)
	{
		fclose(town_index_index_data_block);
	}

	if (newfilename)
	{
		g_free(newfilename);
		newfilename = NULL;
	}

	if (newfilename_compr)
	{
		g_free(newfilename_compr);
		newfilename_compr = NULL;
	}





	// put all parts together
	town_index_index = tempfile("", "town_index_index", 0);
	filecopy(town_index_index, out);
	fclose(town_index_index);
	tempfile_unlink("", "town_index_index");

	for (i=1;i < (current_index_block + 2);i++)
	{
		fprintf(stderr, "index block #%d\n", i);
		newfilename = g_strdup_printf("town_index_index_%d", i);
		town_index_index_data_block = tempfile("", newfilename, 0);

		if (town_index_index_data_block)
		{
			fprintf(stderr, "using: index block #%d in %s\n", i, newfilename);
			filecopy(town_index_index_data_block, out);
			fclose(town_index_index_data_block);
			tempfile_unlink("", newfilename);
		}

		if (newfilename)
		{
			g_free(newfilename);
			newfilename = NULL;
		}
	}



#endif
}

void generate_street_index_file(FILE *out)
{
#ifdef MAPTOOL_USE_SQL
	int rc = 0;
	int i;
	struct streets_index_data_block db;
	struct streets_index_index_block_start is;
	struct streets_index_index_block ib;
	long long last_len = 0;
	static const char *alpha = "abcdefghijklmnopqrstuvwxyz";
	char tmp_letter[STREET_INDEX_STREET_NAME_SIZE];
	char *newfilename = NULL;
	char *newfilename_compr = NULL;
	int count_of_blocks;
	int do_rest;
	int num1;
	int num2;

	FILE *street_index_index;
	FILE *street_index_index_data_block;

	// init compression for streets
	// init compression for streets
	IN_BUF_SIZE = s_IN_BUF_SIZE;
	s_inbuf = s_s_inbuf;
	OUT_BUF_SIZE = s_OUT_BUF_SIZE;
	s_outbuf = s_s_outbuf;
	COMP_OUT_BUF_SIZE = s_OUT_BUF_SIZE;
	// init compression for streets
	// init compression for streets


	is.count_of_index_blocks = 703; // 26+1 letters ((26+1)*26 + 1) = 703
	ib.offset = sizeof (is.count_of_index_blocks) + is.count_of_index_blocks * sizeof(struct streets_index_index_block); // start offset = index size
	street_index_index = tempfile("", "street_index_index", 1);
	fprintf(stderr, "index size=%d\n", ib.offset);

	fprintf(stderr, "ftell=%d\n", ftell(street_index_index));
	fwrite(&is, sizeof(struct streets_index_index_block_start), 1, street_index_index);
	fprintf(stderr, "ftell=%d\n", ftell(street_index_index));

	// fprintf(stderr, "len=%d\n", strlen(alpha));

	count_of_blocks = 702;
	do_rest = 0;
	num1 = 1;
	num2 = 0;
	for (i=0; i < count_of_blocks; i++)
	{
		num2++;
		do_rest = 0;
		if (num2 == 27)
		{
			do_rest = 1;
		}
		else if (num2 > 27)
		{
			num2 = 1;
			num1++;
		}

		fprintf(stderr, "i=%d num1=%d num2=%d\n", i, num1, num2);

		if (do_rest)
		{
			sprintf(tmp_letter, "%c%%", alpha[num1 - 1]);
		}
		else
		{
			sprintf(tmp_letter, "%c%c%%", alpha[num1 - 1], alpha[num2 - 1]);
		}
		fprintf(stderr, "letter=%s\n", tmp_letter);

		ib.first_letter = alpha[num1 - 1];
		ib.len = 0;

		newfilename = g_strdup_printf("street_index_index_%d", i);
		newfilename_compr = g_strdup_printf("street_index_index_compr_%d", i);
		street_index_index_data_block = tempfile("", newfilename, 1);

		// loop thru all the streets that match '<letter>...'
		sqlite3_bind_text(stmt_sel003, 1, tmp_letter, -1, SQLITE_STATIC);
		do
		{
			rc = sqlite3_step(stmt_sel003);
			switch (rc)
			{
				case SQLITE_DONE:
				break;
				case SQLITE_ROW:
				db.town_id = sqlite3_column_int64(stmt_sel003, 0);
				db.lat = transform_from_geo_lat(sqlite3_column_double(stmt_sel003, 1));
				db.lon = transform_from_geo_lon(sqlite3_column_double(stmt_sel003, 2));
				strncpy(&db.street_name, sqlite3_column_text(stmt_sel003, 3), STREET_INDEX_STREET_NAME_SIZE);
				db.street_name[STREET_INDEX_STREET_NAME_SIZE - 1]= '\0'; // make sure string is terminated

				ib.len = ib.len + sizeof(struct streets_index_data_block);

				// fprintf(stderr ,"gen_street_index id:%lld lat:%d lon:%d name:%s\n", db.town_id, db.lat, db.lon, db.street_name);
				fwrite(&db, sizeof(struct streets_index_data_block), 1, street_index_index_data_block);

				break;
				default:
				fprintf(stderr, "SQL Error: %d\n", rc);
				break;
			}
		}
		while (rc == SQLITE_ROW);

		sqlite3_reset(stmt_sel003);
		fclose(street_index_index_data_block);

		sqlite3_bind_int(stmt_sel003u, 1, (i + 1));
		sqlite3_bind_text(stmt_sel003u, 2, tmp_letter, -1, SQLITE_STATIC);
		sqlite3_step(stmt_sel003u);
		sqlite3_reset(stmt_sel003u);
		// sqlite3_exec(sql_handle, "COMMIT", 0, 0, 0);


		if (USE_STREET_INDEX_COMPRESSION == 1)
		{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
			ib.len = compress_file(newfilename, newfilename_compr, 0);
#endif
		}

		last_len = ib.len;
		fprintf(stderr ,"letter=%c offset=%lld len=%lld\n", ib.first_letter, ib.offset, ib.len);
		fprintf(stderr, "ftell=%d\n", ftell(street_index_index));
		fwrite(&ib, sizeof(struct streets_index_index_block), 1, street_index_index);
		fprintf(stderr, "ftell=%d\n", ftell(street_index_index));
		ib.offset = ib.offset + last_len;

		if (newfilename)
		{
			g_free(newfilename);
			newfilename = NULL;
		}

		if (newfilename_compr)
		{
			g_free(newfilename_compr);
			newfilename_compr = NULL;
		}
	}

	// rest of the streets
	fprintf(stderr, "rest of letters\n");

	ib.first_letter = 65; // dummy "A"
	ib.len = 0;

	newfilename = g_strdup_printf("street_index_index_%d", i);
	newfilename_compr = g_strdup_printf("street_index_index_compr_%d", i);
	street_index_index_data_block = tempfile("", newfilename, 1);

	do
	{
		rc = sqlite3_step(stmt_sel004);
		switch (rc)
		{
			case SQLITE_DONE:
			break;
			case SQLITE_ROW:
			db.town_id = sqlite3_column_int64(stmt_sel004, 0);
			db.lat = transform_from_geo_lat(sqlite3_column_double(stmt_sel004, 1));
			db.lon = transform_from_geo_lon(sqlite3_column_double(stmt_sel004, 2));
			strncpy(&db.street_name, sqlite3_column_text(stmt_sel004, 3), STREET_INDEX_STREET_NAME_SIZE);
			db.street_name[STREET_INDEX_STREET_NAME_SIZE - 1]= '\0'; // make sure string is terminated

			ib.len = ib.len + sizeof(struct streets_index_data_block);

			//fprintf(stderr ,"id:%lld lat:%d lon:%d name:%s\n", db.town_id, db.lat, db.lon, db.street_name);
			fwrite(&db, sizeof(struct streets_index_data_block), 1, street_index_index_data_block);

			break;
			default:
			fprintf(stderr, "SQL Error: %d\n", rc);
			break;
		}
	}
	while (rc == SQLITE_ROW);
	sqlite3_reset(stmt_sel004);
	fclose(street_index_index_data_block);

	if (USE_STREET_INDEX_COMPRESSION == 1)
	{
#ifdef MAPTOOL_USE_STRINDEX_COMPRESSION
		ib.len = compress_file(newfilename, newfilename_compr, 0);
#endif
	}

	last_len = ib.len;
	fprintf(stderr ,"letter=%c offset=%lld len=%lld\n", ib.first_letter, ib.offset, ib.len);
	fprintf(stderr, "ftell=%d\n", ftell(street_index_index));
	fwrite(&ib, sizeof(struct streets_index_index_block), 1, street_index_index);
	fprintf(stderr, "ftell=%d\n", ftell(street_index_index));
	// ib.offset = ib.offset + last_len;


	fclose(street_index_index);

	if (newfilename)
	{
		g_free(newfilename);
		newfilename = NULL;
	}

	if (newfilename_compr)
	{
		g_free(newfilename_compr);
		newfilename_compr = NULL;
	}

	// put all parts together
	street_index_index = tempfile("", "street_index_index", 0);
	filecopy(street_index_index, out);
	fclose(street_index_index);
	tempfile_unlink("", "street_index_index");

	for (i=0;i < (is.count_of_index_blocks + 1);i++)
	{
		fprintf(stderr, "cat #%d\n", i);
		newfilename = g_strdup_printf("street_index_index_%d", i);
		street_index_index_data_block = tempfile("", newfilename, 0);

		if (street_index_index_data_block)
		{
			filecopy(street_index_index_data_block, out);
			fclose(street_index_index_data_block);
			tempfile_unlink("", newfilename);
		}

		if (newfilename)
		{
			g_free(newfilename);
			newfilename = NULL;
		}
	}
#endif
}

void remove_attr_str(struct item_bin *ib, int attr_type)
{
	char *attr_str = item_bin_get_attr(ib, attr_type, NULL);
	if (attr_str)
	{
		item_bin_remove_attr(ib, attr_str);
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

int map_find_intersections(FILE *in, FILE *out, FILE *out_index, FILE *out_graph, FILE *out_coastline, int final)
{
	struct coord *c;
	int i, ccount, last, remaining;
	osmid ndref;
	struct item_bin *ib;
	struct node_item *ni;
	long long last_id = 0;
	long long ways_count = 0;

	processed_nodes = processed_nodes_out = processed_ways = processed_relations = processed_tiles = 0;
	//sig_alrm(0);
	while ((ib = read_item(in, 0))) // loop thru all "ways" from file "in"
	{

		//long long *id;
		//id = item_bin_get_attr(ib, attr_osm_wayid, NULL);
		//if (id)
		//{
		//	fprintf(stderr,"wayid:%lld type:0x%x len:%d clen:%d\n", *id, ib->type, ib->len, (ib->clen / 2));
		//}

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
			fprintf(stderr, "ways: %lld\n", ways_count);
		}

		c = (struct coord *) (ib + 1);
		last = 0;
		for (i = 0; i < ccount; i++) // loop thru all the coordinates (nodes) of this way
		{
			//fprintf(stderr, "i=%d ccount=%d\n", i, ccount);

			if (IS_REF(c[i]))
			{
				//fprintf(stderr, "is ref\n");
				ndref = REF(c[i]);
				//fprintf(stderr, "ndref=%d\n", ndref);
				ni = node_item_get_fast(ndref, 0);
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
	//sig_alrm(0);
	//sig_alrm_end();
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
					fprintf(stderr, "write_countrydir: tilename=%s country_%d.tmp\n", tilename, co->countryid);
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
			fprintf(stderr, "load_countries: country_%d.tmp\n", co->countryid);
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

