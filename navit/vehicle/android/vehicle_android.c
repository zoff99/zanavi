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

/** @file vehicle_android.c
 * @brief android uses dbus signals
 *
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
 *
 * @Author Tim Niemeyer <reddog@mastersword.de>
 * @date 2008-2009
 */

#include <config.h>
#include <string.h>
#include <glib.h>
#include <math.h>
#include <time.h>
#include "debug.h"
#include "callback.h"
#include "plugin.h"
#include "coord.h"
#include "item.h"
#include "android.h"
#include "vehicle.h"


#define LOCATION_DAMPEN_BEARING_COUNT 5

struct vehicle_last_bearings
{
	int *ring_buf; // ring buffer
	float *dampen_value; // dampen values
	int max; // max elements
	int cur; // current element num
	int first; // first == 1 --> buffer is not initialised yet
};

struct vehicle_priv
{
	struct callback_list *cbl;
	struct coord_geo geo;
	double speed;
	double direction;
	double height;
	double radius;
	int fix_type;
	time_t fix_time;
	char fixiso8601[128];
	int sats;
	int sats_used;
	int have_coords;
	struct attr ** attrs;
	struct callback *cb;
	jclass NavitVehicleClass;
	jobject NavitVehicle;
	jclass LocationClass;
	jmethodID Location_getLatitude, Location_getLongitude, Location_getSpeed, Location_getBearing, Location_getAltitude, Location_getTime, Location_getAccuracy;
	struct vehicle_last_bearings *lb;
};

// global vars
struct vehicle_priv *priv_global_android = NULL;

jclass NavitClass3 = NULL;
jmethodID Navit_get_vehicle;
// global vars




static int find_static_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	//DBG dbg(0,"EEnter\n");
	*ret = (*jnienv2)->GetStaticMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		//DBG dbg(0, "Failed to get static Method %s with signature %s\n", name, args);
		return 0;
	}
	return 1;
}


/**
 * @brief Free the android_vehicle
 * 
 * @param priv
 * @returns nothing
 */
static void vehicle_android_destroy(struct vehicle_priv *priv)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif

	g_free(priv->lb->dampen_value);
	g_free(priv->lb->ring_buf);
	g_free(priv->lb);

	// //DBG dbg(0,"enter\n");
	priv_global_android = NULL;
	g_free(priv);
}

/**
 * @brief Provide the outside with information
 * 
 * @param priv
 * @param type TODO: What can this be?
 * @param attr
 * @returns true/false
 */
static int vehicle_android_position_attr_get(struct vehicle_priv *priv, enum attr_type type, struct attr *attr)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//dbg(1,"enter %s\n",attr_to_name(type));
	switch (type)
	{
#if 0
		case attr_position_fix_type:
		attr->u.num = priv->fix_type;
		break;
#endif
		case attr_position_height:
			attr->u.numd = &priv->height;
			break;
		case attr_position_speed:
			attr->u.numd = &priv->speed;
			break;
		case attr_position_direction:
			attr->u.numd = &priv->direction;
			break;
		case attr_position_radius:
			attr->u.numd = &priv->radius;
			break;

#if 0
			case attr_position_qual:
			attr->u.num = priv->sats;
			break;
			case attr_position_sats_used:
			attr->u.num = priv->sats_used;
			break;
#endif
		case attr_position_coord_geo:
			attr->u.coord_geo = &priv->geo;
			if (!priv->have_coords)
				return 0;
			break;
		case attr_position_time_iso8601:
			attr->u.str = priv->fixiso8601;
			break;
		default:
			return 0;
	}
	//dbg(1,"ok\n");
	attr->type = type;

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return 1;
}

static void vehicle_android_update_location_direct(double lat, double lon, float speed, float direction, double height, float radius, long gpstime)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif

#ifdef NAVIT_MEASURE_TIME_DEBUG
	clock_t s_ = debug_measure_start();
#endif

	int j;
	int k;
	time_t tnow;
	struct tm *tm;
	struct vehicle_priv *v = priv_global_android;
	float direction_new = 0.0f;

	// +++JNIEnv *jnienv2;
	// +++jnienv2 = jni_getenv();

	if (direction < 0)
	{
		direction = direction + 360.0f;
	}
	else if (direction >= 360)
	{
		direction = direction - 360.0f;
	}

#ifdef NAVIT_GPS_DIRECTION_DAMPING
	if (v->lb->first == 1)
	{
		v->lb->first = 0;
		for (j = 0; j < LOCATION_DAMPEN_BEARING_COUNT; j++)
		{
			v->lb->ring_buf[j] = (int)(direction * 100.0f);
		}
	}
#endif


#ifdef NAVIT_GPS_DIRECTION_DAMPING
	if (direction == 0.0f)
	{
		// dbg(0, "DAMPING:dir=0.0\n");
		int direction_prev = v->lb->ring_buf[(v->lb->cur + (LOCATION_DAMPEN_BEARING_COUNT - 2)) % LOCATION_DAMPEN_BEARING_COUNT];
		// dbg(0, "DAMPING:dir_prev=%d\n", (int)((float)direction_prev / 100.0f));
		// if direction suddenly jumps to 0 (north), assume some problem or 3G location
		if (abs(direction_prev) > 140) // last direction NOT between -1.4° and 1.4°
		{
			// use previous direction value
			direction = ((float)direction_prev / 100.0f);
			dbg(0, "DAMPING:dir_corr=%f\n", direction);
		}
	}
#endif


#ifdef NAVIT_GPS_DIRECTION_DAMPING
	float direction_new2 = 0.0f;

	// save orig value into slot
	v->lb->ring_buf[v->lb->cur] = (int)(direction * 100.0f);
	// move to next slot
	v->lb->cur = (v->lb->cur + 1) % LOCATION_DAMPEN_BEARING_COUNT;

	for (j = 0; j < LOCATION_DAMPEN_BEARING_COUNT; j++)
	{
		if (j == (LOCATION_DAMPEN_BEARING_COUNT - 1))
		{

			//dbg(0, "DAMPING:info last:direction_new=%f direction=%f j=%d\n", direction_new, direction, j);

#if 0
			// if SUM >= 360
			if (direction_new >= 36000)
			{
				for (k = 0; k < (LOCATION_DAMPEN_BEARING_COUNT - 1); k++)
				{
					v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] = v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] - 36000;
				}
				direction_new = direction_new - 36000;
				dbg(0, "DAMPING:>= 360:direction_new=%f\n", (direction_new / 100.0f));
			}
			// if SUM <  0
			else if (direction_new < 0)
			{
				for (k = 0; k < (LOCATION_DAMPEN_BEARING_COUNT - 1); k++)
				{
					v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] = v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] + 36000;
				}
				direction_new = direction_new + 36000;
				dbg(0, "DAMPING:< 0:direction_new=%f\n", (direction_new / 100.0f));
			}
#endif

			direction_new2 = v->lb->ring_buf[(v->lb->cur + j - 1) % LOCATION_DAMPEN_BEARING_COUNT] ;

			//dbg(0, "DAMPING:info last:direction_new2=%f\n", direction_new2);
			//dbg(0, "DAMPING:info last:dir=%d damp=%f\n", v->lb->ring_buf[(v->lb->cur + j) % LOCATION_DAMPEN_BEARING_COUNT], v->lb->dampen_value[j]);


			// correct for the jump from 0 -> 360 , or 360 -> 0
			if (direction_new2 > (v->lb->ring_buf[(v->lb->cur + j) % LOCATION_DAMPEN_BEARING_COUNT] + 18000) )
			{
				direction_new += ((float)(v->lb->ring_buf[(v->lb->cur + j) % LOCATION_DAMPEN_BEARING_COUNT] + 36000 ) * (v->lb->dampen_value[j]));
				//dbg(0, "DAMPING:jump 360 -> 0:direction_new=%f\n", (direction_new / 100.0f));
			}
			else if ((direction_new2 + 18000) < (v->lb->ring_buf[(v->lb->cur + j) % LOCATION_DAMPEN_BEARING_COUNT]) )
			{
				direction_new += ((float)(v->lb->ring_buf[(v->lb->cur + j) % LOCATION_DAMPEN_BEARING_COUNT] - 36000 ) * (v->lb->dampen_value[j]));
				//dbg(0, "DAMPING:jump 0 -> 360:direction_new=%f\n", (direction_new / 100.0f));
			}
			else
			{
				direction_new += ((float)(v->lb->ring_buf[(v->lb->cur + j) % LOCATION_DAMPEN_BEARING_COUNT]) * (v->lb->dampen_value[j]));
			}
		}
		else
		{
			direction_new += ((float)(v->lb->ring_buf[(v->lb->cur + j) % LOCATION_DAMPEN_BEARING_COUNT]) * (v->lb->dampen_value[j]));
			//dbg(0, "DAMPING:info(%d):direction_new=%f\n", j, direction_new);
		}
	}

	// if SUM >= 360
	if (direction_new >= 36000)
	{
		for (k = 0; k < (LOCATION_DAMPEN_BEARING_COUNT); k++)
		{
			v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] = v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] - 36000;
		}
		direction_new = direction_new - 36000;
		//dbg(0, "DAMPING:2:>= 360:direction_new=%f\n", (direction_new / 100.0f));
	}
	// if SUM <  0
	else if (direction_new < 0)
	{
		for (k = 0; k < (LOCATION_DAMPEN_BEARING_COUNT); k++)
		{
			v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] = v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] + 36000;
		}
		direction_new = direction_new + 36000;
		//dbg(0, "DAMPING:2:< 0:direction_new=%f\n", (direction_new / 100.0f));
	}


	// save corrected value into slot
	v->lb->ring_buf[(v->lb->cur + (LOCATION_DAMPEN_BEARING_COUNT - 1)) % LOCATION_DAMPEN_BEARING_COUNT] = (int)direction_new;

	v->direction = direction_new / 100.0f;


	//for (k = 0; k < (LOCATION_DAMPEN_BEARING_COUNT); k++)
	//{
	//	dbg(0, "DAMPING:damp[%d]=%f", k, ((float)v->lb->ring_buf[(v->lb->cur + k) % LOCATION_DAMPEN_BEARING_COUNT] / 100.0f));
	//}

	//dbg(0, "DAMPING:FIN:direction=%f corrected=%f\n", direction, (direction_new / 100.0f));
	//dbg(0, "DAMPING:----------------------------\n");

#else

	// --------------------------------------------------------
	// normal direction, without DAMPING !!

	v->direction = direction;

	// --------------------------------------------------------

#endif




	//dbg(0,"jnienv=%p\n", jnienv);
	//dbg(0,"priv_global_android=%p\n", priv_global_android);
	//dbg(0,"v=%p\n", v);
	//dbg(0,"location=%p\n", location);

	// this seems to slow and stupid, try to give those values directly (instead of calling those functions every time!!)
	// this seems to slow and stupid, try to give those values directly (instead of calling those functions every time!!)
	// this seems to slow and stupid, try to give those values directly (instead of calling those functions every time!!)
	v->geo.lat = lat;
	v->geo.lng = lon;
	v->speed = speed;
	// ** DAMPING ** // v->direction = direction;
	// dbg(0, "v->direction=%f\n", direction);
	v->height = height;
	v->radius = radius;
	tnow = gpstime;
	// this seems to slow and stupid, try to give those values directly (instead of calling those functions every time!!)
	// this seems to slow and stupid, try to give those values directly (instead of calling those functions every time!!)
	// this seems to slow and stupid, try to give those values directly (instead of calling those functions every time!!)

	tm = gmtime(&tnow);
	strftime(v->fixiso8601, sizeof(v->fixiso8601), "%Y-%m-%dT%TZ", tm);
	// //DBG dbg(0,"lat %f lon %f\n",v->geo.lat,v->geo.lng);
	v->have_coords = 1;

	// remove globalref again
	//+++(*jnienv2)->DeleteGlobalRef(jnienv2, location);



	// xxx stupid callback stuff -> remove me!!  xxx ----------------------------
	// xxx stupid callback stuff -> remove me!!  xxx ----------------------------
	//
	// ***** calls: navit.c -> navit_vehicle_update
	callback_list_call_attr_0(v->cbl, attr_position_coord_geo);
	//
	// xxx stupid callback stuff -> remove me!!  xxx ----------------------------
	// xxx stupid callback stuff -> remove me!!  xxx ----------------------------




#ifdef NAVIT_MEASURE_TIME_DEBUG
	debug_mrp(__PRETTY_FUNCTION__, debug_measure_end(s_));
#endif

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

}

struct vehicle_methods vehicle_android_methods =
{ vehicle_android_destroy, vehicle_android_position_attr_get, NULL, vehicle_android_update_location_direct};

static int vehicle_android_init(struct vehicle_priv *ret)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif

	int thread_id = gettid();
	dbg(0, "THREAD ID=%d\n", thread_id);

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jmethodID cid;

	//dbg(0,"priv_global_android=%p\n", priv_global_android);

	if (!android_find_class_global("android/location/Location", &ret->LocationClass))
		return 0;
	if (!android_find_method(ret->LocationClass, "getLatitude", "()D", &ret->Location_getLatitude))
		return 0;
	if (!android_find_method(ret->LocationClass, "getLongitude", "()D", &ret->Location_getLongitude))
		return 0;
	if (!android_find_method(ret->LocationClass, "getSpeed", "()F", &ret->Location_getSpeed))
		return 0;
	if (!android_find_method(ret->LocationClass, "getBearing", "()F", &ret->Location_getBearing))
		return 0;
	if (!android_find_method(ret->LocationClass, "getAltitude", "()D", &ret->Location_getAltitude))
		return 0;
	if (!android_find_method(ret->LocationClass, "getTime", "()J", &ret->Location_getTime))
		return 0;
	if (!android_find_method(ret->LocationClass, "getAccuracy", "()F", &ret->Location_getAccuracy))
		return 0;
	if (!android_find_class_global("com/zoffcc/applications/zanavi/NavitVehicle", &ret->NavitVehicleClass))
	{
		return 0;
	}

	//dbg(0,"jnienv2=%p\n", jnienv2);

	//DBG dbg(0,"at 3\n");
	//cid = (*jnienv2)->GetMethodID(jnienv2, ret->NavitVehicleClass, "<init>", "(Landroid/content/Context;I)V");
	//if (cid == NULL)
	//{
	//	//DBG dbg(0,"no method found\n");
	//	return 0;
	//}

	// --------------- Init the new Vehicle Object here -----------------
	// --------------- Init the new Vehicle Object here -----------------
	// --------------- Init the new Vehicle Object here -----------------
	dbg(0,"Init the new Vehicle Object here\n");

	if (NavitClass3 == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/Navit", &NavitClass3))
		{
			NavitClass3 = NULL;
			return 0;
		}
	}

	if (!find_static_method(NavitClass3, "get_vehicle_object", "()Lcom/zoffcc/applications/zanavi/NavitVehicle;", &Navit_get_vehicle))
	{
		return 0;
	}

	/// --old-- ret->NavitVehicle = (*jnienv2)->NewObject(jnienv2, ret->NavitVehicleClass, cid, android_activity, (int) ret->cb);
	/// --new--
	ret->NavitVehicle = (*jnienv2)->CallStaticObjectMethod(jnienv2, NavitClass3, Navit_get_vehicle);
	/// --new--
	// --------------- Init the new Vehicle Object here -----------------
	// --------------- Init the new Vehicle Object here -----------------
	// --------------- Init the new Vehicle Object here -----------------

	if (!ret->NavitVehicle)
	{
		return 0;
	}

	if (ret->NavitVehicle)
	{
		ret->NavitVehicle = (*jnienv2)->NewGlobalRef(jnienv2, ret->NavitVehicle);
	}

	dbg(0,"leave\n");

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return 1;
}

/**
 * @brief Create android_vehicle
 * 
 * @param meth
 * @param cbl
 * @param attrs
 * @returns vehicle_priv
 */
struct vehicle_priv *
vehicle_android_new_android(struct vehicle_methods *meth, struct callback_list *cbl, struct attr **attrs)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct vehicle_priv *ret;
	struct vehicle_last_bearings *lb;
	int size;

	ret = g_new0(struct vehicle_priv, 1);
	ret->cbl = cbl;
	// *********** // ret->cb = callback_new_1(callback_cast(vehicle_android_callback), ret);
	*meth = vehicle_android_methods;
	priv_global_android = ret;


	lb = g_new0(struct vehicle_last_bearings, 1);
	size = sizeof(int) * LOCATION_DAMPEN_BEARING_COUNT;
	lb->ring_buf = g_malloc(size);
	lb->max = LOCATION_DAMPEN_BEARING_COUNT;
	lb->first = 1;
	lb->cur = 0;

	size = sizeof(float) * LOCATION_DAMPEN_BEARING_COUNT;
	lb->dampen_value = g_malloc(size);


#ifdef NAVIT_GPS_DIRECTION_DAMPING

//	lb->dampen_value[0] = 0.00f;
//	lb->dampen_value[1] = 0.00f;
//	lb->dampen_value[2] = 0.00f;
//	lb->dampen_value[3] = 0.00f;
//	lb->dampen_value[4] = 0.01f;
//	lb->dampen_value[5] = 0.02f;
//	lb->dampen_value[6] = 0.04f;
//	lb->dampen_value[7] = 0.10f;
//	lb->dampen_value[8] = 0.16f;
//	lb->dampen_value[9] = 0.67f;

	lb->dampen_value[0] = 0.00f;
	lb->dampen_value[1] = 0.00f;
	lb->dampen_value[2] = 0.00f;
	lb->dampen_value[3] = 0.10f;
	lb->dampen_value[4] = 0.90f;

#endif

	ret->lb = lb;

	//dbg(0,"priv_global_android=%p\n", priv_global_android);
	vehicle_android_init(ret);

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return ret;
}

/**
 * @brief register vehicle_android
 * 
 * @returns nothing
 */
#ifdef PLUGSSS
void plugin_init(void)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0, "enter\n");
	plugin_register_vehicle_type("android", vehicle_android_new_android);

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

}
#endif

