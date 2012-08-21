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

#include <stdlib.h>
#include <string.h>
#include <poll.h>
#include <glib.h>
#include "android.h"
#include <android/log.h>
#include "navit.h"
#include "config_.h"
#include "command.h"
#include "debug.h"
#include "event.h"
#include "callback.h"
#include "projection.h"
#include "map.h"
#include "transform.h"
#include "color.h"
#include "types.h"
#include "search.h"
#include "start_real.h"
#include "route.h"
#include "file.h"

// #include "layout.h"

JNIEnv *jnienv;
jobject *android_activity;
struct callback_list *android_activity_cbl;
int android_version;

jclass NavitGraphicsClass2 = NULL;
jmethodID return_generic_int;
jmethodID send_generic_text;
jclass NavitMapPreviewActivityClass = NULL;
jmethodID DrawMapPreview_target;
jmethodID DrawMapPreview_polyline = NULL;
jmethodID DrawMapPreview_text = NULL;

struct attr attr;







// ------------------------- COPIED STUFF --- this is generally bad ------------------
// ------------------------- COPIED STUFF --- this is generally bad ------------------
// ------------------------- COPIED STUFF --- this is generally bad ------------------


// copyied from config_.c !!! (always keep in sync!!!)
struct config {
	struct attr **attrs;
	struct callback_list *cbl;
} *config;


// copied from gui_internal.c (always keep in sync!!!)
struct gui_config_settings {
  int font_size;
  int icon_xs;
  int icon_s;
  int icon_l;
  int spacing;
};


// dummy def!!
struct gui_internal_methods {
	int dummy;
};

// dummy def!!
struct gui_internal_widget_methods {
	int dummy;
};


// forward def
struct gui_priv;

// copied from gui_internal.h (always keep in sync!!!)
struct gui_internal_data {
	struct gui_priv *priv;
	struct gui_internal_methods *gui;
	struct gui_internal_widget_methods *widget;
};

// copied from gui_internal.c (always keep in sync!!!)
struct route_data {
  struct widget * route_table;
  int route_showing;

};

// copied from gui_internal.h (always keep in sync!!!)
enum flags {
	gravity_none=0x00,
	gravity_left=1,
	gravity_xcenter=2,
	gravity_right=4,
	gravity_top=8,
	gravity_ycenter=16,
	gravity_bottom=32,
	gravity_left_top=gravity_left|gravity_top,
	gravity_top_center=gravity_xcenter|gravity_top,
	gravity_right_top=gravity_right|gravity_top,
	gravity_left_center=gravity_left|gravity_ycenter,
	gravity_center=gravity_xcenter|gravity_ycenter,
	gravity_right_center=gravity_right|gravity_ycenter,
	gravity_left_bottom=gravity_left|gravity_bottom,
	gravity_bottom_center=gravity_xcenter|gravity_bottom,
	gravity_right_bottom=gravity_right|gravity_bottom,
	flags_expand=0x100,
	flags_fill=0x200,
	orientation_horizontal=0x10000,
	orientation_vertical=0x20000,
	orientation_horizontal_vertical=0x40000,
};

// copied from gui_internal.h (always keep in sync!!!)
enum widget_type {
	widget_box=1,
	widget_button,
	widget_label,
	widget_image,
	widget_table,
	widget_table_row
};

// copied from gui_internal.c (always keep in sync!!!)
struct widget {
	enum widget_type type;
	struct graphics_gc *background,*text_background;
	struct graphics_gc *foreground_frame;
	struct graphics_gc *foreground;
	char *text;
	struct graphics_image *img;
	void (*func)(struct gui_priv *priv, struct widget *widget, void *data);
	int reason;
	int datai;
	void *data;
	void (*data_free)(void *data);
	void (*free) (struct gui_priv *this_, struct widget * w);
	char *prefix;
	char *name;
	char *speech;
	char *command;
	struct pcoord c;
	struct item item;
	int selection_id;
	int state;
	struct point p;
	int wmin,hmin;
	int w,h;
	int textw,texth;
	int font_idx;
	int bl,br,bt,bb,spx,spy;
	int border;
	int packed;
	int cols;
	enum flags flags;
	int flags2;
	void *instance;
	int (*set_attr)(void *, struct attr *);
	int (*get_attr)(void *, enum attr_type, struct attr *, struct attr_iter *);
	void (*remove_cb)(void *, struct callback *cb);
	struct callback *cb;
	struct attr on;
	struct attr off;
	int deflt;
	int is_on;
	int redraw;
	struct menu_data *menu_data;
	struct form *form;
	GList *children;
};


// copied from gui_internal.c !!!!!! (always keep in sync!!!)
struct gui_priv {
	struct navit *nav;
	struct attr self;
	struct window *win;
	struct graphics *gra;
	struct graphics_gc *background;
	struct graphics_gc *background2;
	struct graphics_gc *highlight_background;
	struct graphics_gc *foreground;
	struct graphics_gc *text_foreground;
	struct graphics_gc *text_background;
	struct color background_color, background2_color, text_foreground_color, text_background_color;
	int spacing;
	int font_size;
	int fullscreen;
	struct graphics_font *fonts[3];
	int icon_xs;
	int icon_s;
	int icon_l;
	int pressed;
	struct widget *widgets;
	int widgets_count;
	int redraw;
	struct widget root;
	struct widget *highlighted,*editable;
	struct widget *highlighted_menu;
	int clickp_valid, vehicle_valid;
	struct pcoord clickp, vehiclep;
	struct attr *click_coord_geo, *position_coord_geo;
	struct search_list *sl;
	int ignore_button;
	int menu_on_map_click;
	int signal_on_map_click;
	char *country_iso2;
	int speech;
	int keyboard;
	int keyboard_required;
	struct gui_config_settings config;
	struct event_idle *idle;
	struct callback *motion_cb,*button_cb,*resize_cb,*keypress_cb,*window_closed_cb,*idle_cb, *motion_timeout_callback;
	struct event_timeout *motion_timeout_event;
	struct point current;
	struct callback * vehicle_cb;
	struct route_data route_data;
	struct gui_internal_data data;
	struct callback_list *cbl;
	int flags;
	int cols;
	struct attr osd_configuration;
	int pitch;
	int flags_town,flags_street,flags_house_number;
	int radius;
	char *html_text;
	int html_depth;
	struct widget *html_container;
	int html_skip;
	char *html_anchor;
	char *href;
	int html_anchor_found;
	struct form *form;
	struct html {
		int skip;
		enum html_tag {
			html_tag_none,
			html_tag_a,
			html_tag_h1,
			html_tag_html,
			html_tag_img,
			html_tag_script,
			html_tag_form,
			html_tag_input,
			html_tag_div,
		} tag;
		char *command;
		char *name;
		char *href;
		char *refresh_cond;
		struct widget *w;
		struct widget *container;
	} html[10];
};


// ------------------------- COPIED STUFF --- this is generally bad ------------------
// ------------------------- COPIED STUFF --- this is generally bad ------------------
// ------------------------- COPIED STUFF --- this is generally bad ------------------




JavaVM *cachedJVM = NULL;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    JNIEnv *env_this;
    cachedJVM = jvm;
    if ((*jvm)->GetEnv(jvm, (void**)&env_this, JNI_VERSION_1_6))
	{
        dbg(0,"Could not get JVM\n");
        return JNI_ERR;
    }

	dbg(0,"++ Found JWM ++\n");
    return JNI_VERSION_1_6;
}

JNIEnv* jni_getenv()
{
    JNIEnv* env_this;
    (*cachedJVM)->GetEnv(cachedJVM, (void**)&env_this, JNI_VERSION_1_6);
    return env_this;
}


static void gui_internal_search_list_set_default_country2(struct gui_priv *this)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct attr search_attr, country_name, country_iso2, *country_attr;
	struct item *item;
	struct country_search *cs;
	struct tracking *tracking;
	struct search_list_result *res;

	////DBG dbg(0,"### 1");

	country_attr = country_default();
	tracking = navit_get_tracking(this->nav);

	if (tracking && tracking_get_attr(tracking, attr_country_id, &search_attr, NULL))
		country_attr = &search_attr;
	if (country_attr)
	{
		////DBG dbg(0,"### 2");
		cs = country_search_new(country_attr, 0);
		item = country_search_get_item(cs);
		if (item && item_attr_get(item, attr_country_name, &country_name))
		{
			search_attr.type = attr_country_all;
			////DBG dbg(0,"country %s\n", country_name.u.str);
			search_attr.u.str = country_name.u.str;
			search_list_search(this->sl, &search_attr, 0);
			while ((res = search_list_get_result(this->sl)))
				;
			if (this->country_iso2)
			{
				// this seems to cause a crash, no idea why
				//g_free(this->country_iso2);
				this->country_iso2 = NULL;
			}
			if (item_attr_get(item, attr_country_iso2, &country_iso2))
			{
				this->country_iso2 = g_strdup(country_iso2.u.str);
			}
		}
		country_search_destroy(cs);
	}
	else
	{
		//DBG dbg(0, "warning: no default country found\n");
		if (this->country_iso2)
		{
			//DBG dbg(0, "attempting to use country '%s'\n", this->country_iso2);
			search_attr.type = attr_country_iso2;
			search_attr.u.str = this->country_iso2;
			search_list_search(this->sl, &search_attr, 0);
			while ((res = search_list_get_result(this->sl)))
				;
		}
	}
	////DBG dbg(0,"### 99");
}

struct navit *global_navit;

int android_find_class_global(char *name, jclass *ret)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	*ret = (*jnienv)->FindClass(jnienv, name);
	if (!*ret)
	{
		//DBG dbg(0, "Failed to get Class %s\n", name);
		return 0;
	}
	//DBG dbg(0,"lclass %p\n", *ret);
	*ret = (*jnienv)->NewGlobalRef(jnienv, *ret);
	// ICS (*jnienv)->DeleteGlobalRef(jnienv, *lret);
	//DBG dbg(0,"gclass %p\n", *ret);
	return 1;
}

int android_find_method(jclass class, char *name, char *args, jmethodID *ret)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	*ret = (*jnienv)->GetMethodID(jnienv, class, name, args);
	if (*ret == NULL)
	{
		//DBG dbg(0, "Failed to get Method %s with signature %s\n", name, args);
		return 0;
	}
	//DBG dbg(0,"l meth %p\n", *ret);
	return 1;
}

int android_find_static_method(jclass class, char *name, char *args, jmethodID *ret)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	*ret = (*jnienv)->GetStaticMethodID(jnienv, class, name, args);
	if (*ret == NULL)
	{
		//DBG dbg(0, "Failed to get static Method %s with signature %s\n", name, args);
		return 0;
	}
	//DBG dbg(0,"l meth %p\n", *ret);
	return 1;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_Navit_NavitMain(JNIEnv* env, jobject thiz, jobject activity, jobject lang, int version, jobject display_density_string)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	char *strings[] =
	{ "/data/data/com.zoffcc.applications.zanavi/bin/navit", NULL };
	const char *langstr;
	const char *displaydensitystr;
	android_version = version;
	//__android_log_print(ANDROID_LOG_ERROR,"test","called");
	android_activity_cbl = callback_list_new();

	// set global JNIenv here ----------
	// set global JNIenv here ----------
	// set global JNIenv here ----------
	jnienv = env;
	//dbg(0,"jnienv=%p\n", jnienv);
	// set global JNIenv here ----------
	// set global JNIenv here ----------
	// set global JNIenv here ----------

	//jclass someClass = env->FindClass("SomeClass");
	//gSomeClass = env->NewGlobalRef(someClass);


	// *only local* android_activity = activity;
	// android_activity = (*jnienv)->NewGlobalRef(jnienv, activity);
	android_activity = (*env)->NewGlobalRef(env, activity);
	langstr = (*env)->GetStringUTFChars(env, lang, NULL);
	//DBG dbg(0, "enter env=%p thiz=%p activity=%p lang=%s version=%d\n", env, thiz, activity, langstr, version);
	//DBG dbg(0, "enter env=%p thiz=%p activity=%p lang=%s version=%d\n", env, thiz, android_activity, langstr, version);
	setenv("LANG", langstr, 1);
	(*env)->ReleaseStringUTFChars(env, lang, langstr);

	displaydensitystr = (*env)->GetStringUTFChars(env, display_density_string, NULL);
	//DBG dbg(0, "*****displaydensity=%s\n", displaydensitystr);
	setenv("ANDROID_DENSITY", displaydensitystr, 1);
	(*env)->ReleaseStringUTFChars(env, display_density_string, displaydensitystr);
	main_real(1, strings);

	dbg(0,"after main_real call\n");

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_Navit_NavitActivity(JNIEnv* env, jobject thiz, int param)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	dbg(0, "enter %d\n", param);

	if (param == -2)
	{
		// onStop() -> called in Java app
		// save center.txt
		if (global_navit->bookmarks)
		{
			config_get_attr(config, attr_navit, &attr, NULL);
			//DBG dbg(0, "save position to file");
			char *center_file = bookmarks_get_center_file(TRUE);
			bookmarks_write_center_to_file(attr.u.navit->bookmarks, center_file);
			dbg(0, "save pos to file -> ready");
			g_free(center_file);
			// bookmarks_destroy(global_navit->bookmarks);
		}
	}

	// param ==  3 // onCreate
	// param ==  2 // onStart
	// param ==  1 // onResume
	// param ==  0 // onRestart
	// param == -1 // onPause
	// param == -2 // onStop
	// param == -3 // onDestroy
	// param == -4 // exit() [java function]

	if (param == 3)
	{
		navit_draw(global_navit);
	}

	dbg(0, "acti: 001\n");
	// callback_list_call_1(android_activity_cbl, param);
	//dbg(0, "acti: 002\n");

	//if (param == -4)
	//{
	//	dbg(0, "acti: 003\n");
	//	// *********EXIT******EXIT******** // exit(0);
	//}

	if (param == -4)
	{
		dbg(0, "acti: 004\n");
		navit_destroy(global_navit);
		dbg(0, "acti: 005\n");
		event_main_loop_quit();
		dbg(0, "acti: 006\n");
	}
}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_SizeChangedCallback(JNIEnv* env, jobject thiz, int w, int h)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	dbg(0,"enter %d %d\n", w, h);
	navit_handle_resize(global_navit, w, h);
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif
}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_MotionCallback(JNIEnv* env, jobject thiz, int x1, int y1, int x2, int y2)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif

	struct point p_end;
	struct point p_start;

	p_start.x = x1;
	p_start.y = y1;
	p_end.x = x2;
	p_end.y = y2;
	update_transformation(global_navit->trans, &p_start, &p_end, NULL);
	// graphics_draw_drag(this_->gra, NULL);
	transform_copy(global_navit->trans, global_navit->trans_cursor);
	global_navit->moved = 1;

	/*
	struct coord c;
	struct pcoord pc;
	p.x = x;
	p.y = y;
	transform_reverse(global_navit->trans, &p, &c);
	pc.x = c.x;
	pc.y = c.y;
	pc.pro = transform_get_projection(global_navit->trans);
	navit_set_position(global_navit, &pc);
	*/

	dbg(0,"call async java draw -start-\n");
	navit_draw(global_navit);
	// navit_draw_async(global_navit, 1);
	dbg(0,"call async java draw --end--\n");

	// remove the "wait" screen
#ifdef HAVE_API_ANDROID
	android_return_generic_int(2, 0);
#endif

}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitTimeout_TimeoutCallback(JNIEnv* env, jobject thiz, int delete, int id)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0,"enter %p %d %p\n",thiz, delete, (void *)id);
	//dbg(0,"timeout 1\n");
	// ICS
	callback_call_0((struct callback *) id);
	// ICS
	//dbg(0,"timeout 2\n");
	if (delete)
	{
		dbg(0,"timeout 3\n");
		// ICS
		jobject this_global = (*jnienv)->NewGlobalRef(jnienv, thiz);
		dbg(0,"timeout 3.1\n");
		(*jnienv)->DeleteGlobalRef(jnienv, this_global);
		// ICS
		dbg(0,"timeout 4\n");
	}
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitIdle_IdleCallback(JNIEnv* env, jobject thiz, int id)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0,"enter %p %p\n",thiz, (void *)id);
	callback_call_0((struct callback *) id);
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitWatch_poll(JNIEnv* env, jobject thiz, int fd, int cond)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct pollfd pfd;
	pfd.fd = fd;
	//DBG dbg(0, "%p poll called for %d %d\n", env, fd, cond);
	switch ((enum event_watch_cond) cond)
	{
		case event_watch_cond_read:
			pfd.events = POLLIN;
			break;
		case event_watch_cond_write:
			pfd.events = POLLOUT;
			break;
		case event_watch_cond_except:
			pfd.events = POLLERR;
			break;
		default:
			pfd.events = 0;
	}
	pfd.revents = 0;
	poll(&pfd, 1, -1);
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitWatch_WatchCallback(JNIEnv* env, jobject thiz, int id)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0, "enter %p %p\n", thiz, (void *) id);
	callback_call_0((struct callback *) id);
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitSensors_SensorCallback(JNIEnv* env, jobject thiz, int id, int sensor, float x, float y, float z)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0, "enter %p %p %f %f %f\n", thiz, (void *) id, x, y, z);
	callback_call_4((struct callback *) id, sensor, &x, &y, &z);
}


JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitVehicle_VehicleCallback(JNIEnv * env, jobject thiz, jobject location)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//dbg(0,"location=%p\n", location);
	jobject location2 = (*jnienv)->NewGlobalRef(jnienv, location);
	(*jnienv)->DeleteLocalRef(jnienv, location);
	//dbg(0,"location=%p\n", location2);

	vehicle_update_(global_navit->vehicle->vehicle, location2);

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif
}


void android_return_search_result(struct jni_object *jni_o, char *str)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0,"EEnter\n");
	jstring js2 = NULL;
	JNIEnv* env2;
	env2 = jni_o->env;
	js2 = (*env2)->NewStringUTF(jni_o->env, str);
	(*env2)->CallVoidMethod(jni_o->env, jni_o->jo, jni_o->jm, js2);
	(*env2)->DeleteLocalRef(jni_o->env, js2);
}

void android_return_generic_int(int id, int i)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0,"Enter\n");

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	if (NavitGraphicsClass2 == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/NavitGraphics", &NavitGraphicsClass2))
		{
			NavitGraphicsClass2 = NULL;
			return;
		}
	}
	//DBG dbg(0,"xx1\n");
	if (return_generic_int == NULL)
	{
		android_find_static_method(NavitGraphicsClass2, "return_generic_int", "(II)V", &return_generic_int);
	}
	//DBG dbg(0,"xx2\n");
	if (return_generic_int == NULL)
	{
		//DBG dbg(0, "no method found\n");
		return; /* exception thrown */
	}
	//DBG dbg(0,"xa1\n");
	// -crash- (*jnienv2)->CallVoidMethod(jnienv2, NavitGraphicsClass2, return_generic_int, id, i);
	(*jnienv2)->CallStaticVoidMethod(jnienv2, NavitGraphicsClass2, return_generic_int, id, i);
	// -works- (*jnienv2)->CallStaticObjectMethod(jnienv2, NavitGraphicsClass2, return_generic_int, id, i);
	//DBG dbg(0,"xa2\n");
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_CallbackSearchResultList(JNIEnv* env, jobject thiz, int id, int partial, jobject str, int search_flags, jobject search_country, jobject latlon, int radius)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	const char *s;
	s = (*env)->GetStringUTFChars(env, str, NULL);
	////DBG dbg(0,"*****string=%s\n",s);


	config_get_attr(config, attr_navit, &attr, NULL);
	// attr.u.navit

	jstring js2 = NULL;
	jclass cls_local = (*env)->GetObjectClass(env, thiz);

	// ICS ???
	jclass cls = (*env)->NewGlobalRef(env, cls_local);
	// ICS ???

	jmethodID aMethodID = (*env)->GetMethodID(env, cls, "fillStringArray", "(Ljava/lang/String;)V");
	if (aMethodID == 0)
	{
		////DBG dbg(0,"**** Unable to get methodID: fillStringArray");
		return;
	}

	if (id)
	{
		// search for town in variable "s" within current country -> return a list of towns as result
		if (id == 1)
		{
			// unused now!!
		}
		// search for street in variable "s" within "search_country" -> return a list of streets as result
		else if (id == 2)
		{
			//struct attr s_attr4;
			//struct gui_priv *gp4;
			//struct gui_priv gp_24;

			offline_search_break_searching = 0;

			struct jni_object my_jni_object;
			my_jni_object.env = env;
			my_jni_object.jo = thiz;
			my_jni_object.jm = aMethodID;

			//gp4=&gp_24;
			//gp4->nav=attr.u.navit;
			struct mapset *ms4 = navit_get_mapset(attr.u.navit);
			GList *ret = NULL;
			int flags = search_flags;
			char *search_country_string = (*env)->GetStringUTFChars(env, search_country, NULL);
			ret = search_by_address(ret, ms4, s, partial, &my_jni_object, flags, search_country_string);
			(*env)->ReleaseStringUTFChars(env, search_country, search_country_string);

			// free the memory
			g_list_free(ret);
			////DBG dbg(0,"ret=%p\n",ret);


			//if (gp4->sl)
			//{
			//	//search_list_destroy(gp4->sl);
			//	gp4->sl=NULL;
			//}
		}
		// do a full search in all mapfiles for string in variable "s" -> return a list of streets as result
		else if (id == 3)
		{
			const char *s3;
			s3 = (*env)->GetStringUTFChars(env, latlon, NULL);
			char parse_str[strlen(s3) + 1];
			strcpy(parse_str, s3);
			(*env)->ReleaseStringUTFChars(env, latlon, s3);

			struct coord_geo g7;
			char *p;
			char *stopstring;

			// lat
			p = strtok(parse_str, "#");
			g7.lat = strtof(p, &stopstring);
			// lon
			p = strtok(NULL, "#");
			g7.lng = strtof(p, &stopstring);

			struct jni_object my_jni_object;
			my_jni_object.env = env;
			my_jni_object.jo = thiz;
			my_jni_object.jm = aMethodID;

			offline_search_break_searching = 0;

			// search_flags --> is search_order (search at what "order" level)
			search_full_world(s, partial, search_flags, &my_jni_object, &g7, radius);
		}
	}

	(*env)->ReleaseStringUTFChars(env, str, s);
}

JNIEXPORT jint JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_CallbackDestinationValid(JNIEnv* env, jobject thiz)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0,"EEnter\n");
	config_get_attr(config, attr_navit, &attr, NULL);
	// //DBG dbg(0,"destination_valid=%d\n",attr.u.navit->destination_valid);
	jint i = 0;
	if (attr.u.navit->route)
	{
		struct route *r;
		r = attr.u.navit->route;
		i = r->route_status;
		// //DBG dbg(0,"route_status=%d\n",i);
	}
	return i;
}

static void map_preview_label_line(struct point *p, int count, char *label, int font_size)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	int i, x, y, tl, tlm, th, thm, tlsq, l;
	float lsq;
	double dx, dy;
	struct point p_t;
	struct point pb[5];

	int len = g_utf8_strlen(label, -1);
	int xMin = 0;
	int yMin = 0;
	int yMax = 13 * font_size / 256;
	int xMax = 9 * font_size * len / 256;

	////DBG dbg(0,"xMax=%d\n", xMax);
	////DBG dbg(0,"yMax=%d\n", yMax);

	pb[0].x = xMin;
	pb[0].y = -yMin;
	pb[1].x = xMin;
	pb[1].y = -yMax;
	pb[2].x = xMax;
	pb[2].y = -yMax;
	pb[3].x = xMax;
	pb[3].y = -yMin;

	tl = (pb[2].x - pb[0].x);
	th = (pb[0].y - pb[1].y);

	// calc "tl" text length
	// tl=strlen(label)*4;
	// calc "th" text height
	// th=8;

	tlm = tl * 32;
	thm = th * 36;
	tlsq = (tlm * 0.7) * (tlm * 0.7);

	for (i = 0; i < count - 1; i++)
	{
		dx = p[i + 1].x - p[i].x;
		dx *= 32;
		dy = p[i + 1].y - p[i].y;
		dy *= 32;
		lsq = dx * dx + dy * dy;

		if (lsq > tlsq)
		{
			////DBG dbg(0,"-------- label=%s\n",label);
			////DBG dbg(0,"px i=%d py i=%d px i+1=%d py i+1=%d\n",p[i].x,p[i].y,p[i+1].x,p[i+1].y);
			////DBG dbg(0,"dx=%f dy=%f\n",dx,dy);
			l = (int) sqrtf(lsq);
			////DBG dbg(0,"l=%d lsq=%f\n",l,lsq);
			x = p[i].x;
			y = p[i].y;
			if (dx < 0)
			{
				dx = -dx;
				dy = -dy;
				x = p[i + 1].x;
				y = p[i + 1].y;
			}
			x += (l - tlm) * dx / l / 64;
			y += (l - tlm) * dy / l / 64;
			x -= dy * thm / l / 64;
			y += dx * thm / l / 64;
			p_t.x = x;
			p_t.y = y;

			////DBG dbg(0,"dx=%f dy=%f\n",dx,dy);
			////DBG dbg(0,"dx=%d dy=%d\n",(int)dx,(int)dy);
			////DBG dbg(0,"draw px=%d py=%d\n",p_t.x,p_t.y);
			////DBG dbg(0,"l=%d\n",l);
			////DBG dbg(0,"+++++++++++++\n");
			// **OLD and wrong** android_DrawMapPreview_text(p_t.x, p_t.y, label, font_size, dx*0x10000/l, dy*0x10000/l);
			android_DrawMapPreview_text(p_t.x, p_t.y, label, font_size, (int) dx, (int) dy);
		}
	}
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitMapPreviewActivity_DrawMapPreview(JNIEnv* env, jobject thiz, jobject latlonzoom, int width, int height, int font_size, int scale, int sel_range)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	// config_get_attr(config, attr_navit, &attr, NULL);

	const char *s;
	int zoom;
	s = (*env)->GetStringUTFChars(env, latlonzoom, NULL);
	char parse_str[strlen(s) + 1];
	strcpy(parse_str, s);
	(*env)->ReleaseStringUTFChars(env, latlonzoom, s);
	////DBG dbg(0,"*****string=%s\n",s);

	// show map preview for (lat#lon#zoom)
	struct coord_geo g;
	char *p;
	char *stopstring;

	// lat
	p = strtok(parse_str, "#");
	g.lat = strtof(p, &stopstring);
	// lon
	p = strtok(NULL, "#");
	g.lng = strtof(p, &stopstring);
	// zoom
	p = strtok(NULL, "#");
	zoom = atoi(p);

	////DBG dbg(0,"lat=%f\n",g.lat);
	////DBG dbg(0,"lng=%f\n",g.lng);
	////DBG dbg(0,"zoom=%d\n",zoom);
	////DBG dbg(0,"w=%d\n",width);
	////DBG dbg(0,"h=%d\n",height);

	struct coord c;
	transform_from_geo(projection_mg, &g, &c);

	// struct pcoord pc;
	// pc.x=c.x;
	// pc.y=c.y;
	// pc.pro=projection_mg;


	// ----------------------- big draw loop -----------------------
	// ----------------------- big draw loop -----------------------
	// ----------------------- big draw loop -----------------------
	// ----------------------- big draw loop -----------------------
	struct item *item;
	struct map_rect *mr = NULL;
	struct mapset *ms;
	struct mapset_handle *msh;
	struct map* map = NULL;
	struct attr map_name_attr;
	struct attr attr;

	struct map_selection sel;
	const int selection_range = sel_range; // should be something with "width" and "height" i guess ??!!

	const int max = 100;
	int count;
	struct coord *ca = g_alloca(sizeof(struct coord) * max);
	struct point *pa = g_alloca(sizeof(struct point) * max);

	sel.next = NULL;
	sel.order = zoom;
	sel.range.min = type_none;
	sel.range.max = type_last;
	sel.u.c_rect.lu.x = c.x - selection_range;
	sel.u.c_rect.lu.y = c.y + selection_range;
	sel.u.c_rect.rl.x = c.x + selection_range;
	sel.u.c_rect.rl.y = c.y - selection_range;

	struct transformation *tr;
	tr = transform_dup(global_navit->trans);
	struct point p_center;
	p_center.x = width / 2;
	p_center.y = height / 2;
	transform_set_screen_center(tr, &p_center);
	transform_set_center(tr, &c);
	transform_set_scale(tr, scale);
	enum projection pro = transform_get_projection(global_navit->trans_cursor);

	ms = global_navit->mapsets->data;
	msh = mapset_open(ms);
	while (msh && (map = mapset_next(msh, 0)))
	{
		if (map_get_attr(map, attr_name, &map_name_attr, NULL))
		{
			if (strncmp("_ms_sdcard_map:", map_name_attr.u.str, 15) == 0)
			{
				if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/borders.bin", map_name_attr.u.str, 41) == 0)
				{
					// country borders
					// //DBG dbg(0,"map name=%s",map_name_attr.u.str);
					mr = map_rect_new(map, NULL);
					if (mr)
					{
						while ((item = map_rect_get_item(mr)))
						{

							// count=item_coord_get_within_selection(item, ca, item->type < type_line ? 1: max, &sel);
							count = item_coord_get_within_selection(item, ca, max, &sel);
							if (!count)
							{
								continue;
							}
							count = transform(tr, pro, ca, pa, count, 0, 0, NULL);

							// //DBG dbg(0,"uu %s\n",item_to_name(item->type));

							if (item->type == type_border_country)
							{
								// //DBG dbg(0,"BB** %s\n",item_to_name(item->type));
								android_DrawMapPreview_polyline(pa, count, 2);
							}
						}
						map_rect_destroy(mr);
					}
				}
				else if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/navitmap", map_name_attr.u.str, 38) == 0)
				{
					// its an sdcard map
					// //DBG dbg(0,"map name=%s",map_name_attr.u.str);
					mr = map_rect_new(map, &sel);
					if (mr)
					{
						//char *streetname_last=NULL;

						while ((item = map_rect_get_item(mr)))
						{
							int label_count = 0;
							char *labels[2];

							// count=item_coord_get_within_selection(item, ca, item->type < type_line ? 1: max, &sel);
							count = item_coord_get_within_selection(item, ca, max, &sel);

							// count=item_coord_get_within_selection(item, ca, max, &sel);
							// count=item_coord_get(item, ca, item->type < type_line ? 1: max);
							if (!count)
							{
								continue;
							}
							////DBG dbg(0,"count 1=%d\n", count);

							if (count == max)
							{
								////DBG dbg(0,"count overflow!!\n");
							}

							struct attr attr_77;
							if (item_attr_get(item, attr_flags, &attr_77))
							{
								////DBG dbg(0,"uuuuuuuuuuuuu %s uuuuu %d\n",item_to_name(item->type), attr_77.u.num);
								item->flags = attr_77.u.num;
							}
							else
							{
								item->flags = 0;
							}

							//if (item_is_street(*item))
							//{
							//	int i3;
							//	for (i3 = 0 ; i3 < count ; i3++)
							//	{
							//		if (i3)
							//		{
							//			//DBG dbg(0,"1 x1=%d\n",ca[i3-1].x);
							//			//DBG dbg(0,"1 y1=%d\n",ca[i3-1].y);
							//			//DBG dbg(0,"1 x2=%d\n",ca[i3].x);
							//			//DBG dbg(0,"1 y2=%d\n",ca[i3].y);
							//		}
							//	}
							//}

							count = transform(tr, pro, ca, pa, count, 0, 0, NULL);

							////DBG dbg(0,"count 2=%d\n", count);

							// --- LABEL ---
							labels[1] = NULL;
							label_count = 0;
							if (item_attr_get(item, attr_label, &attr))
							{
								labels[0] = attr.u.str;
								////DBG dbg(0,"labels[0]=%s\n",attr.u.str);
								if (!label_count)
								{
									label_count = 2;
								}
							}
							else
							{
								labels[0] = NULL;
							}
							// --- LABEL ---

							if (item_is_street(*item))
							{
								//int i3;
								//for (i3 = 0 ; i3 < count ; i3++)
								//{
								//	if (i3)
								//	{
								//		//DBG dbg(0,"2 x1=%d\n",pa[i3-1].x);
								//		//DBG dbg(0,"2 y1=%d\n",pa[i3-1].y);
								//		//DBG dbg(0,"2 x2=%d\n",pa[i3].x);
								//		//DBG dbg(0,"2 y2=%d\n",pa[i3].y);
								//	}
								//}
								android_DrawMapPreview_polyline(pa, count, 0);
								if (labels[0] != NULL)
								{
									map_preview_label_line(pa, count, labels[0], font_size);
								}
							}
							else if (item_is_district(*item))
							{
								if (zoom > 6)
								{
									// //DBG dbg(0,"xx** %s - %s\n",item_to_name(item->type),labels[0]);
									if (count >= 1)
									{
										android_DrawMapPreview_text(pa[0].x, pa[0].y, labels[0], font_size * 2, 0x10000, 0);
									}
								}
							}
							else if (item_is_town(*item))
							{
								// //DBG dbg(0,"yy** %s - %s\n",item_to_name(item->type),labels[0]);
								if (count >= 1)
								{
									android_DrawMapPreview_text(pa[0].x, pa[0].y, labels[0], font_size * 3, 0x10000, 0);
								}
							}

							//if (item_is_street(*item))
							//{
							//	if (item_attr_get(item, attr_label, &attr))
							//	{
							//		////DBG dbg(0,"street1=%s\n",map_convert_string(item->map, attr.u.str));
							//		if ( (streetname_last==NULL) || (strcmp(streetname_last,attr.u.str) != 0) )
							//		{
							//			////DBG dbg(0,"street2=%s\n",map_convert_string(item->map, attr.u.str));
							//		}
							//	}
							//}
						}
						//g_free(streetname_last);
						map_rect_destroy(mr);
					}
				}
			}
		}
	}
	mapset_close(msh);

	enum projection pro2 = transform_get_projection(global_navit->trans_cursor);
	struct point pnt;
	transform(tr, pro2, &c, &pnt, 1, 0, 0, NULL);
	transform_destroy(tr);
	// ----------------------- big draw loop -----------------------
	// ----------------------- big draw loop -----------------------
	// ----------------------- big draw loop -----------------------
	// ----------------------- big draw loop -----------------------

	android_DrawMapPreview_target(pnt.x, pnt.y);
}

void android_DrawMapPreview_target(int x, int y)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	if (NavitMapPreviewActivityClass == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/NavitMapPreviewActivity", &NavitMapPreviewActivityClass))
		{
			NavitMapPreviewActivityClass = NULL;
			return;
		}
	}
	if (DrawMapPreview_target == NULL)
	{
		android_find_static_method(NavitMapPreviewActivityClass, "DrawMapPreview_target", "(II)V", &DrawMapPreview_target);
	}
	if (DrawMapPreview_target == NULL)
	{
		//DBG dbg(0, "no method found\n");
		return; /* exception thrown */
	}
	(*jnienv)->CallStaticVoidMethod(jnienv, NavitMapPreviewActivityClass, DrawMapPreview_target, x, y);
}

void android_DrawMapPreview_text(int x, int y, char *text, int size, int dx, int dy)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif

	if (NavitMapPreviewActivityClass == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/NavitMapPreviewActivity", &NavitMapPreviewActivityClass))
		{
			NavitMapPreviewActivityClass = NULL;
			return;
		}
	}
	if (DrawMapPreview_text == NULL)
	{
		android_find_static_method(NavitMapPreviewActivityClass, "DrawMapPreview_text", "(IILjava/lang/String;III)V", &DrawMapPreview_text);
	}
	if (DrawMapPreview_text == NULL)
	{
		//DBG dbg(0, "no method found\n");
		return; /* exception thrown */
	}

	////DBG dbg(0,"** dx=%d,dy=%d\n",dx,dy);

	jstring string1 = (*jnienv)->NewStringUTF(jnienv, text);
	(*jnienv)->CallStaticVoidMethod(jnienv, NavitMapPreviewActivityClass, DrawMapPreview_text, x, y, string1, size, dx, dy);
	(*jnienv)->DeleteLocalRef(jnienv, string1);
}

void android_DrawMapPreview_polyline(struct point *p, int count, int type)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	// type:
	// 0 -> normal street
	// 2 -> country border

	if (NavitMapPreviewActivityClass == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/NavitMapPreviewActivity", &NavitMapPreviewActivityClass))
		{
			NavitMapPreviewActivityClass = NULL;
			return;
		}
	}
	if (DrawMapPreview_polyline == NULL)
	{
		android_find_static_method(NavitMapPreviewActivityClass, "DrawMapPreview_polyline", "(I[I)V", &DrawMapPreview_polyline);
	}
	if (DrawMapPreview_polyline == NULL)
	{
		//DBG dbg(0, "no method found\n");
		return; /* exception thrown */
	}

	jint pc[count * 2];
	int i;
	jintArray points;
	if (count <= 0)
	{
		return;
	}
	points = (*jnienv)->NewIntArray(jnienv, count * 2);
	for (i = 0; i < count; i++)
	{
		pc[i * 2] = p[i].x;
		pc[i * 2 + 1] = p[i].y;
	}
	(*jnienv)->SetIntArrayRegion(jnienv, points, 0, count * 2, pc);
	(*jnienv)->CallStaticVoidMethod(jnienv, NavitMapPreviewActivityClass, DrawMapPreview_polyline, type, points);
	(*jnienv)->DeleteLocalRef(jnienv, points);
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_CallbackGeoCalc(JNIEnv* env, jobject thiz, int i, float a, float b)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	// dbg(0,"EEnter i=%d\n", i);

	// const char *result;
	gchar *result;

	if (i == 1)
	{
		// pixel-on-screen to geo
		struct coord_geo g22;
		struct coord c22;
		struct point p;
		p.x = a;
		p.y = b;
		transform_reverse(global_navit->trans, &p, &c22);
		////DBG dbg(0,"%f, %f\n",a, b);
		////DBG dbg(0,"%d, %d\n",p.x, p.y);
		transform_to_geo(projection_mg, &c22, &g22);
		////DBG dbg(0,"%d, %d, %f, %f\n",c22.x, c22.y, g22.lat, g22.lng);
		result = g_strdup_printf("%f:%f", g22.lat, g22.lng);
	}
	else if (i == 2)
	{
		// geo to pixel-on-screen
		struct coord c99;
		struct coord_geo g99;
		g99.lat = a;
		g99.lng = b;
		////DBG dbg(0,"zzzzz %f, %f\n",a, b);
		////DBG dbg(0,"yyyyy %f, %f\n",g99.lat, g99.lng);
		transform_from_geo(projection_mg, &g99, &c99);
		////DBG dbg(0,"%d %d %f %f\n",c99.x, c99.y, g99.lat, g99.lng);

		enum projection pro = transform_get_projection(global_navit->trans_cursor);
		struct point pnt;
		transform(global_navit->trans, pro, &c99, &pnt, 1, 0, 0, NULL);
		////DBG dbg(0,"x=%d\n",pnt.x);
		////DBG dbg(0,"y=%d\n",pnt.y);

		result = g_strdup_printf("%d:%d", pnt.x, pnt.y);
	}
	else if (i == 3)
	{
		// show lat,lng position on screen center
		struct coord c99;
		struct pcoord pc99;
		struct coord_geo g99;
		g99.lat = a;
		g99.lng = b;
		////DBG dbg(0,"zzzzz %f, %f\n",a, b);
		////DBG dbg(0,"yyyyy %f, %f\n",g99.lat, g99.lng);
		transform_from_geo(projection_mg, &g99, &c99);
		////DBG dbg(0,"%d %d %f %f\n",c99.x, c99.y, g99.lat, g99.lng);

		//enum projection pro=transform_get_projection(global_navit->trans_cursor);
		//struct point pnt;
		//transform(global_navit->trans, pro, &c99, &pnt, 1, 0, 0, NULL);
		////DBG dbg(0,"x=%d\n",pnt.x);
		////DBG dbg(0,"y=%d\n",pnt.y);
		pc99.x = c99.x;
		pc99.y = c99.y;
		pc99.pro = projection_mg;

		navit_set_center(global_navit, &pc99, 0);

		result = g_strdup_printf("1:1");
	}
	else if (i == 4)
	{
		// return current target (the end point, not waypoints)
		struct coord_geo g22;
		struct pcoord c22;
		struct coord c99;

		c22 = global_navit->destination;
		c99.x = c22.x;
		c99.y = c22.y;

		transform_to_geo(projection_mg, &c99, &g22);
		result = g_strdup_printf("%f:%f", g22.lat, g22.lng);
	}
	else if (i == 5)
	{
		// input:	x,y pixel on screen
		// output:	streetname nearest that position
		struct coord c22;
		struct point p;
		struct pcoord c24;
		p.x = a;
		p.y = b;
		transform_reverse(global_navit->trans, &p, &c22);
		c24.x = c22.x;
		c24.y = c22.y;
		c24.pro = transform_get_projection(global_navit->trans);
		result = navit_find_nearest_street(global_navit->mapsets->data, &c24);
	}
	else if (i == 6)
	{
		// input:	lat, lon
		// output:	streetname nearest that position
		struct coord c22;
		struct point p;
		struct pcoord c24;
		struct coord_geo g99;
		g99.lat = a;
		g99.lng = b;
		transform_from_geo(projection_mg, &g99, &c22);
		c24.x = c22.x;
		c24.y = c22.y;
		c24.pro = transform_get_projection(global_navit->trans);
		result = navit_find_nearest_street(global_navit->mapsets->data, &c24);
	}
	else if (i == 7)
	{
		// input:	x,y pixel on screen
		// output:	0xFFFF 0xFFFF\n... -> string that can be used for traffic distortion file
		struct coord c22;
		struct point p;
		struct pcoord c24;
		p.x = a;
		p.y = b;
		transform_reverse(global_navit->trans, &p, &c22);
		c24.x = c22.x;
		c24.y = c22.y;
		c24.pro = transform_get_projection(global_navit->trans);
		result = navit_find_nearest_street_coords(global_navit->mapsets->data, &c24);
	}
	else if (i == 8)
	{
		// input:	x,y pixel on screen
		// output:	nearest street or housenumber
		struct coord c22;
		struct point p;
		struct pcoord c24;
		p.x = a;
		p.y = b;
		transform_reverse(global_navit->trans, &p, &c22);
		c24.x = c22.x;
		c24.y = c22.y;
		c24.pro = transform_get_projection(global_navit->trans);
		result = navit_find_nearest_street_hn(global_navit->mapsets->data, &c24);
	}
	else if (i == 9)
	{
		// input:	x,y pixel on screen
		// output:	item dump of nearest item
		struct coord c22;
		struct point p;
		struct pcoord c24;
		p.x = a;
		p.y = b;
		transform_reverse(global_navit->trans, &p, &c22);
		c24.x = c22.x;
		c24.y = c22.y;
		c24.pro = transform_get_projection(global_navit->trans);
		result = navit_find_nearest_item_dump(global_navit->mapsets->data, &c24, 0);
	}
	else if (i == 10)
	{
		// input:	x,y pixel on screen
		// output:	item dump of nearest item (pretty output to show to user)
		struct coord c22;
		struct point p;
		struct pcoord c24;
		p.x = a;
		p.y = b;
		transform_reverse(global_navit->trans, &p, &c22);
		c24.x = c22.x;
		c24.y = c22.y;
		c24.pro = transform_get_projection(global_navit->trans);
		result = navit_find_nearest_item_dump(global_navit->mapsets->data, &c24, 1);
	}

	// dbg(0, "result=%s\n", result);
	jstring js = (*env)->NewStringUTF(env, result);
	g_free(result);

	return js;
}

JNIEXPORT jstring JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_CallbackLocalizedString(JNIEnv* env, jobject thiz, jobject str)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0,"EEnter\n");

	const char *s;
	const char *localized_str;

	s = (*env)->GetStringUTFChars(env, str, NULL);
	////DBG dbg(0,"*****string=%s\n",s);

	localized_str = gettext(s);
	////DBG dbg(0,"localized string=%s",localized_str);

	// jstring dataStringValue = (jstring) localized_str;
	jstring js = (*env)->NewStringUTF(env, localized_str);

	(*env)->ReleaseStringUTFChars(env, str, s);

	return js;
}

JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_CallbackMessageChannel(JNIEnv* env, jobject thiz, int i, jobject str2)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	const char *s;
	jobject str = (*env)->NewGlobalRef(env, str2);

	//DBG dbg(0,"enter %d %p\n",i,str);

	config_get_attr(config, attr_navit, &attr, NULL);
	// attr.u.navit

	//DBG dbg(0,"c1\n");

	if (i)
	{
		if (i == 1)
		{
			// zoom in
			navit_zoom_in_cursor(global_navit, 2);
			// navit_zoom_in_cursor(attr.u.navit, 2);
		}
		else if (i == 2)
		{
			// zoom out
			navit_zoom_out_cursor(global_navit, 2);
			// navit_zoom_out_cursor(attr.u.navit, 2);
		}
		else if (i == 73)
		{
			// update the route path and route graph (e.g. after setting new roadblocks)
			// this destroys the route graph and calcs everything totally new!
			if (global_navit->route)
			{
				if (global_navit->route->destinations)
				{
					route_path_update(global_navit->route, 1, 1);
				}
			}
		}
		else if (i == 72)
		{
			// update the route path and route graph (e.g. after setting new roadblocks)
			// does not update destinations!!!
			if (global_navit->route)
			{
				if (global_navit->route->destinations)
				{
					route_path_update(global_navit->route, 0, 1);
				}
			}
		}
		else if (i == 71)
		{
			// activate/deactivate "route graph" display
			// 0 -> deactivate
			// 1 -> activate

			// _ms_route_graph
			// _ms_navigation
			s = (*env)->GetStringUTFChars(env, str, NULL);
			navit_map_active_flag(global_navit, atoi(s), "_ms_route_graph");
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 70)
		{
			// allow drawing map
			hold_drawing = 0;
		}
		else if (i == 69)
		{
			// stop drawing map
			hold_drawing = 1;
		}
		else if (i == 68)
		{
			// shift "order" by this value (only for drawing objects)
			s = (*env)->GetStringUTFChars(env, str, NULL);
			shift_order = atoi(s);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 67)
		{
			// disable "water from relations"
			enable_water_from_relations = 0;
		}
		else if (i == 66)
		{
			// enable "water from relations"
			enable_water_from_relations = 1;
		}
		else if (i == 65)
		{
			// draw map async
			navit_draw_async(global_navit, 1);
		}
		else if (i == 64)
		{
			// draw map
			navit_draw(global_navit);
		}
		else if (i == 63)
		{
			// enable map drawing
			disable_map_drawing = 0;
		}
		else if (i == 62)
		{
			// disable map drawing
			disable_map_drawing = 1;
		}
		else if (i == 61)
		{
			// zoom to specific zoomlevel at given point as center
			struct point p;
			char *pstr;

			s = (*env)->GetStringUTFChars(env, str, NULL);
			char parse_str[strlen(s) + 1];
			strcpy(parse_str, s);
			(*env)->ReleaseStringUTFChars(env, str, s);
			// (pixel-x#pixel-y#zoom-level)
			// pixel-x
			pstr = strtok(parse_str, "#");
			p.x = atoi(pstr);
			// pixel-y
			pstr = strtok(NULL, "#");
			p.y = atoi(pstr);
			// zoom
			pstr = strtok(NULL, "#");
			int zoom_level = atoi(pstr);
			// now call zoom function
			navit_zoom_to_scale_with_center_point(global_navit, zoom_level, &p);
		}
		else if (i == 60)
		{
			// disable layer "name"
			s = (*env)->GetStringUTFChars(env, str, NULL);
			navit_layer_set_active(global_navit, s, 0, 0);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 59)
		{
			// enable layer "name"
			s = (*env)->GetStringUTFChars(env, str, NULL);
			navit_layer_set_active(global_navit, s, 1, 0);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 58)
		{
			// make street search radius bigger to the factor "s"
			s = (*env)->GetStringUTFChars(env, str, NULL);
			global_search_street_size_factor = atoi(s);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 57)
		{
			// keep drawing streets as if at "order" level xxx
			s = (*env)->GetStringUTFChars(env, str, NULL);
			limit_order_corrected = atoi(s);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 56)
		{
			// draw polylines with/without circles at the end
			//dbg(0, "dpf1\n");
			s = (*env)->GetStringUTFChars(env, str, NULL);
			//dbg(0, "dpf2\n");
			draw_polylines_fast = atoi(s);
			//dbg(0, "dpf=%d\n", draw_polylines_fast);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 55)
		{
			// set cache size for (map-)files
			//dbg(0, "csf1\n");
			s = (*env)->GetStringUTFChars(env, str, NULL);
			//dbg(0, "csf2\n");
			cache_size_file = atoi(s);
			file_cache_init();
			dbg(0, "csf=%d\n", cache_size_file);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 54)
		{
			// speak streetnames
			global_speak_streetnames = 1;
		}
		else if (i == 53)
		{
			// dont speak streetnames
			global_speak_streetnames = 0;
		}
		else if (i == 52)
		{
			// switch to demo vehicle

			s = (*env)->GetStringUTFChars(env, str, NULL);

			if (global_navit->vehicle)
			{
				navit_remove_cursors(global_navit);
				vehicle_destroy(global_navit->vehicle->vehicle);
				global_navit->vehicles = NULL;
				global_navit->vehicle = NULL;
			}

			struct attr parent;
			struct attr source;
			struct attr active;
			struct attr follow;
			struct attr speed;
			struct attr interval;
			struct attr *attrs[6];
			parent.type = attr_navit;
			parent.u.navit = global_navit;

			source.type = attr_source;
			source.u.str = "demo://";

			active.type = attr_active;
			active.u.num = 1;

			follow.type = attr_follow;
			follow.u.num = 1;

			speed.type = attr_speed;
			speed.u.num = atoi(s);

			interval.type = attr_interval;
			interval.u.num = 1000;

			attrs[0] = &source;
			attrs[1] = &active;
			attrs[2] = &follow;
			attrs[3] = &speed;
			attrs[4] = &interval;
			attrs[5] = NULL;
			// attr_source -> source->u.str='demo://'
			// 		<!-- vehicle name="Demo" profilename="car" enabled="no" active="yes" source="demo://" -->
			struct vehicle *v;
			//DBG dbg(0, "demo vehicle new start\n");
			v = vehicle_new(&parent, attrs);
			//DBG dbg(0, "demo vehicle new end\n");

			if (v != NULL)
			{
				//DBG dbg(0, "adding demo vehicle\n");
				navit_add_vehicle(global_navit, v);
				//DBG dbg(0, "setting cursor\n");
				navit_set_cursors(global_navit);

				struct attr follow2;
				follow2.type = attr_follow;
				follow2.u.num = 1;
				navit_set_attr(global_navit, &follow2);

				// switch "Map follows Vehicle" ON
				struct attr attrx;
				attrx.type = attr_follow_cursor;
				attrx.u.num = 1;
				navit_set_attr(global_navit, &attrx);
			}
			else
			{
				//DBG dbg(0, "ERROR adding demo vehicle\n");
			}
			// **no** navit_set_vehicle(global_navit, global_navit->vehicle);
			//DBG dbg(0, "ready\n");

			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 51)
		{
			// set position to pixel x,y
			//DBG dbg(0, "sp 1\n");
			char *pstr;
			struct point p;
			struct coord c;
			struct pcoord pc;

			s = (*env)->GetStringUTFChars(env, str, NULL);
			char parse_str[strlen(s) + 1];
			strcpy(parse_str, s);
			(*env)->ReleaseStringUTFChars(env, str, s);

			// (pixel-x#pixel-y)
			// pixel-x
			pstr = strtok(parse_str, "#");
			p.x = atoi(pstr);
			// pixel-y
			pstr = strtok(NULL, "#");
			p.y = atoi(pstr);

			//DBG dbg(0, "sp 2\n");
			transform_reverse(global_navit->trans, &p, &c);
			//DBG dbg(0, "sp 3\n");

			pc.x = c.x;
			pc.y = c.y;
			//DBG dbg(0, "sp 4\n");
			pc.pro = transform_get_projection(global_navit->trans);
			//DBG dbg(0, "sp 5\n");

			//DBG dbg(0, "%d %d\n", pc.x, pc.y);
			// set position
			//DBG dbg(0, "sp 6\n");
			navit_set_position(global_navit, &pc);
			//DBG dbg(0, "ready\n");
		}
		else if (i == 50)
		{
			// we request to stop drawing the map
			// //DBG dbg(0, "cancel_drawing_global=1\n");
			cancel_drawing_global = 1;
		}
		else if (i == 47)
		{
			// change maps data dir
			s = (*env)->GetStringUTFChars(env, str, NULL);
			navit_maps_dir = g_strdup(s);
			(*env)->ReleaseStringUTFChars(env, str, s);
			// //DBG dbg(0,"*****string use=%s\n",navit_maps_dir);
		}
		else if (i == 46)
		{
			// stop searching and show results found until now
			offline_search_break_searching = 1;
		}
		else if (i == 45)
		{
			// filter duplicates in search results
			offline_search_filter_duplicates = 1;
		}
		else if (i == 44)
		{
			// show duplicates in search results
			offline_search_filter_duplicates = 0;
		}
		else if (i == 43)
		{
			// routing mode "normal roads"
			routing_mode = 1;
		}
		else if (i == 42)
		{
			// routing mode "highways"
			routing_mode = 0;
		}
		else if (i == 41)
		{
			// switch "Map follows Vehicle" OFF
			struct attr attrx;
			attrx.type = attr_follow_cursor;
			attrx.u.num = 0;
			navit_set_attr(global_navit, &attrx);
		}
		else if (i == 40)
		{
			// switch "Map follows Vehicle" ON
			struct attr attrx;
			attrx.type = attr_follow_cursor;
			attrx.u.num = 1;
			navit_set_attr(global_navit, &attrx);
		}
		else if (i == 39)
		{
			// switch "Northing" OFF
			struct attr attrx;
			attrx.type = attr_orientation;
			attrx.u.num = 0;
			navit_set_attr(global_navit, &attrx);
		}
		else if (i == 38)
		{
			// switch "Northing" ON
			struct attr attrx;
			attrx.type = attr_orientation;
			attrx.u.num = 1;
			navit_set_attr(global_navit, &attrx);
		}
		else if (i == 37)
		{
			// switch "Lock on road" OFF
			struct attr attrx;
			attrx.type = attr_tracking;
			attrx.u.num = 0;
			navit_set_attr(global_navit, &attrx);
		}
		else if (i == 36)
		{
			// switch "Lock on road" ON
			struct attr attrx;
			attrx.type = attr_tracking;
			attrx.u.num = 1;
			navit_set_attr(global_navit, &attrx);
		}
		else if (i == 35)
		{
			// announcer voice ON
			navit_cmd_announcer_on(global_navit);
		}
		else if (i == 34)
		{
			// announcer voice OFF
			navit_cmd_announcer_off(global_navit);
		}
		else if (i == 33)
		{
			// zoom to specific zoomlevel
			s = (*env)->GetStringUTFChars(env, str, NULL);
			int zoom_level = atoi(s);
			navit_zoom_to_scale(global_navit, zoom_level);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 32)
		{
			// switch to specific 3D pitch
			struct attr pitch_attr;
			s = (*env)->GetStringUTFChars(env, str, NULL);
			pitch_attr.type = attr_pitch;
			pitch_attr.u.num = atoi(s);
			navit_set_attr(global_navit, &pitch_attr);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 31)
		{
			// switch to 3D
			struct attr pitch_attr;
			pitch_attr.type = attr_pitch;
			pitch_attr.u.num = 30;
			navit_set_attr(global_navit, &pitch_attr);
		}
		else if (i == 30)
		{
			// switch to 2D
			struct attr pitch_attr;
			pitch_attr.type = attr_pitch;
			pitch_attr.u.num = 0;
			navit_set_attr(global_navit, &pitch_attr);
		}
		// 21 - 25 are used in java, so leave a hole here to make it easier to understand

		else if (i == 20)
		{
			// add all scdard maps
			navit_add_all_maps(global_navit);
		}
		else if (i == 19)
		{
			// remove all scdard maps
			navit_remove_all_maps(global_navit);
		}
		else if (i == 18)
		{
			// reload scdard maps
			navit_reload_maps(global_navit);
		}
		else if (i == 17)
		{
			// zoom to route
			navit_zoom_to_route(global_navit, 0);
		}
		else if (i == 16)
		{
			// use imperial units
			global_navit->imperial = 1;
		}
		else if (i == 15)
		{
			// use metric units
			global_navit->imperial = 0;
		}
		else if (i == 14)
		{
			// draw location of self (car) in the screen center
			global_navit->radius = 0;
		}
		else if (i == 13)
		{
			// draw location of self (car) 30% lower than screen center
			global_navit->radius = 30;
		}
		else if (i == 12)
		{
			// draw map only at speeds higher than 5 km/h
			struct attr static_speed_attr;
			static_speed_attr.type = attr_static_speed;
			static_speed_attr.u.num = 5;
			vehicleprofile_set_attr(global_navit->vehicleprofile, &static_speed_attr);
		}
		else if (i == 11)
		{
			// allow redraw map at ZERO speed
			struct attr static_speed_attr;
			static_speed_attr.type = attr_static_speed;
			static_speed_attr.u.num = 0;
			vehicleprofile_set_attr(global_navit->vehicleprofile, &static_speed_attr);
		}
		else if (i == 10)
		{
			// allow internal GUI
			allow_gui_internal = 1;
		}
		else if (i == 9)
		{
			// disable internal GUI
			allow_gui_internal = 0;
		}
		else if (i == 8)
		{
			// zoom to zoomlevel
			//DBG dbg(0,"-8- a\n");
			navit_zoom_to_scale(global_navit, 524288);
			//DBG dbg(0,"-8- b\n");
		}
		else if (i == 7)
		{
			// stop navigation
			if (attr.u.navit->destination_valid != 0)
			{
				navit_set_destination(&attr.u.navit->self, NULL, NULL, 0);
			}
		}
		else if (i == 6)
		{
			// not used now!
		}
		else if (i == 5)
		{
			// call a command (like in gui) --> seems not many commands really work with this :-(
			s = (*env)->GetStringUTFChars(env, str, NULL);
			////DBG dbg(0,"*****string=%s\n",s);
			command_evaluate(&attr.u.navit->self, s);
			(*env)->ReleaseStringUTFChars(env, str, s);
		}
		else if (i == 49)
		{
			// add waypoint at pixel x,y on screen

			char *pstr;
			struct point p;
			struct coord c;
			struct pcoord pc;

			s = (*env)->GetStringUTFChars(env, str, NULL);
			char parse_str[strlen(s) + 1];
			strcpy(parse_str, s);
			(*env)->ReleaseStringUTFChars(env, str, s);

			// add waypoint (pixel-x#pixel-y)
			// pixel-x
			pstr = strtok(parse_str, "#");
			p.x = atoi(pstr);
			// pixel-y
			pstr = strtok(NULL, "#");
			p.y = atoi(pstr);

			transform_reverse(global_navit->trans, &p, &c);

			pc.x = c.x;
			pc.y = c.y;
			pc.pro = transform_get_projection(global_navit->trans);

			// append new waypoint to navigation
			navit_add_waypoint_to_route(global_navit, &pc, parse_str, 1);
		}
		else if (i == 4)
		{
			// set destination to pixel x,y on screen

			char *pstr;
			struct point p;
			struct coord c;
			struct pcoord pc;

			s = (*env)->GetStringUTFChars(env, str, NULL);
			char parse_str[strlen(s) + 1];
			strcpy(parse_str, s);
			(*env)->ReleaseStringUTFChars(env, str, s);

			// set destination to (pixel-x#pixel-y)
			// pixel-x
			pstr = strtok(parse_str, "#");
			p.x = atoi(pstr);
			// pixel-y
			pstr = strtok(NULL, "#");
			p.y = atoi(pstr);

			transform_reverse(global_navit->trans, &p, &c);

			pc.x = c.x;
			pc.y = c.y;
			pc.pro = transform_get_projection(global_navit->trans);

			// start navigation asynchronous
			navit_set_destination(global_navit, &pc, parse_str, 1);
		}
		else if (i == 48)
		{
			// append waypoint at lat, lng

			char *name;
			s = (*env)->GetStringUTFChars(env, str, NULL);
			char parse_str[strlen(s) + 1];
			strcpy(parse_str, s);
			(*env)->ReleaseStringUTFChars(env, str, s);

			// waypoint (lat#lon#title)
			struct coord_geo g;
			char *p;
			char *stopstring;

			// lat
			p = strtok(parse_str, "#");
			g.lat = strtof(p, &stopstring);
			// lon
			p = strtok(NULL, "#");
			g.lng = strtof(p, &stopstring);
			// description
			name = strtok(NULL, "#");

			////DBG dbg(0,"lat=%f\n",g.lat);
			////DBG dbg(0,"lng=%f\n",g.lng);
			////DBG dbg(0,"str1=%s\n",name);

			struct coord c;
			transform_from_geo(projection_mg, &g, &c);

			struct pcoord pc;
			pc.x = c.x;
			pc.y = c.y;
			pc.pro = projection_mg;

			// append new waypoint to navigation
			navit_add_waypoint_to_route(global_navit, &pc, name, 1);
		}
		else if (i == 3)
		{
			// set destination to lat, lng

			char *name;
			s = (*env)->GetStringUTFChars(env, str, NULL);
			char parse_str[strlen(s) + 1];
			strcpy(parse_str, s);
			(*env)->ReleaseStringUTFChars(env, str, s);
			////DBG dbg(0,"*****string=%s\n",s);

			// set destination to (lat#lon#title)
			struct coord_geo g;
			char *p;
			char *stopstring;

			// lat
			p = strtok(parse_str, "#");
			g.lat = strtof(p, &stopstring);
			// lon
			p = strtok(NULL, "#");
			g.lng = strtof(p, &stopstring);
			// description
			name = strtok(NULL, "#");

			////DBG dbg(0,"lat=%f\n",g.lat);
			////DBG dbg(0,"lng=%f\n",g.lng);
			////DBG dbg(0,"str1=%s\n",name);

			struct coord c;
			transform_from_geo(projection_mg, &g, &c);

			struct pcoord pc;
			pc.x = c.x;
			pc.y = c.y;
			pc.pro = projection_mg;

			// start navigation asynchronous
			navit_set_destination(global_navit, &pc, name, 1);

		}
	}

	// dbg(0,"deleteglobalref\n");

	(*env)->DeleteGlobalRef(env, str);
	str = NULL;

	// dbg(0,"leave\n");

}

void android_send_generic_text(int id, char *text)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	//DBG dbg(0,"Enter\n");

	if (NavitGraphicsClass2 == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/NavitGraphics", &NavitGraphicsClass2))
		{
			NavitGraphicsClass2 = NULL;
			return;
		}
	}
	//DBG dbg(0,"x1\n");
	if (send_generic_text == NULL)
	{
		android_find_static_method(NavitGraphicsClass2, "send_generic_text", "(ILjava/lang/String;)V", &send_generic_text);
	}
	//DBG dbg(0,"x2\n");
	if (send_generic_text == NULL)
	{
		//DBG dbg(0, "no method found\n");
		return; /* exception thrown */
	}
	//DBG dbg(0,"x3\n");

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jstring string1 = (*jnienv2)->NewStringUTF(jnienv2, text);
	(*jnienv2)->CallStaticVoidMethod(jnienv2, NavitGraphicsClass2, send_generic_text, id, string1);
	(*jnienv2)->DeleteLocalRef(jnienv2, string1);

	//DBG dbg(0,"leave\n");
}

