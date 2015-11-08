/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2015 Zoff <zoff@zoff.cc>
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


#include <math.h>


/*
 * ---------------------------------------------
 *
 * single point of GEO transformation functions
 *
 * ---------------------------------------------
 */

#ifndef NAVIT_TRANS_LAT_LON_GEO_H
#define NAVIT_TRANS_LAT_LON_GEO_H


#define M_PI            			3.14159265358979323846  /* pi */
#define M_PI_4          			0.78539816339744830962  /* pi/4 */

#define __EARTH_RADIUS__ 			6378137.000

// must be the same value!! and must be 2^n !!
#define __GEO_ACCURACY_FACTOR__ 	1.0
#define __GEO_ACCURACY_BIT_FAC__	0
// must be the same value!! and must be 2^n !!

#define M_PI_div_360 				0.008726646259971647884618
#define M_PI_div_180				0.01745329251994329576
#define M_PI_mul_360				1130.97335529232556584560
#define M_PI_mul_180				565.48667764616278292280

#define FROM_GEO_LAT_(_lat_) 		( (int)( ( log(tan(M_PI_4 + _lat_ * M_PI_div_360)) * __EARTH_RADIUS__ ) * __GEO_ACCURACY_FACTOR__ ) )
#define FROM_GEO_LON_(_lon_) 		( (int)( ((_lon_ * __EARTH_RADIUS__ * M_PI_div_180) * __GEO_ACCURACY_FACTOR__) ) )

#define FROM_GEO_LAT_FAST_(_lat_) 	( (int)( ( logf(tanf(M_PI_4 + _lat_ * M_PI_div_360)) * __EARTH_RADIUS__ ) * __GEO_ACCURACY_FACTOR__ ) )
#define FROM_GEO_LON_FAST_(_lon_) 	FROM_GEO_LON_(_lon_)


#define TO_GEO_LAT_(_y_)  			( atan(exp(  (_y_/__GEO_ACCURACY_FACTOR__) / __EARTH_RADIUS__)) / M_PI_div_360 - 90.00 )
#define TO_GEO_LON_(_x_)  			( (_x_/__GEO_ACCURACY_FACTOR__) / __EARTH_RADIUS__ / M_PI_div_180 )

#define TO_GEO_LAT_FAST_(_y_)  		( atanf(expf( (_y_/__GEO_ACCURACY_FACTOR__) / __EARTH_RADIUS__)) / M_PI_div_360 - 90.00 )
#define TO_GEO_LON_FAST_(_x_)  		TO_GEO_LON_(_x_)


#define TO_SCREEN_(xy)				(xy) // ( xy << __GEO_ACCURACY_BIT_FAC__ )
#define FROM_SCREEN_(xy)			(xy) // ( (int)( xy >> __GEO_ACCURACY_BIT_FAC__ ) )


#ifndef NAVIT_TRANS_LAT_LON_GEO_NOFUNCS

double transform_to_geo_lat(int y);
double transform_to_geo_lon(int x);
int transform_from_geo_lat(double lat);
int transform_from_geo_lon(double lon);

double transform_to_geo_lat_fast(int y);
double transform_to_geo_lon_fast(int x);
int transform_from_geo_lat_fast(double lat);
int transform_from_geo_lon_fast(double lon);

#endif

/*
 *
 * actual functions are in "transform.c" !!
 *
 */

#endif


