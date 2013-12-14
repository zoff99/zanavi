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

#include <unistd.h>
#include <glib.h>
#include "config.h"
#include "window.h"
#include "point.h"
#include "graphics.h"
#include "color.h"
#include "plugin.h"
#include "event.h"
#include "debug.h"
#include "callback.h"
#include "android.h"

int dummy;

jclass NavitClass2 = NULL;
jmethodID Navit_get_graphics_object_by_name;

struct graphics_priv
{
	jclass NavitGraphicsClass;
	jmethodID NavitGraphics_draw_polyline, NavitGraphics_draw_polyline2, NavitGraphics_draw_polyline3, NavitGraphics_draw_polyline4, NavitGraphics_draw_polyline_dashed, NavitGraphics_set_dashes, NavitGraphics_draw_polygon, NavitGraphics_draw_polygon2, NavitGraphics_draw_rectangle, NavitGraphics_draw_circle, NavitGraphics_draw_text, NavitGraphics_draw_image,
			NavitGraphics_draw_bigmap, NavitGraphics_draw_image_warp, NavitGraphics_draw_mode, NavitGraphics_draw_drag, NavitGraphics_overlay_disable, NavitGraphics_overlay_resize, NavitGraphics_SetCamera, NavitGraphicsClass_rotate_and_scale_bitmap;

	jclass PaintClass;
	jmethodID Paint_init, Paint_setStrokeWidth, Paint_setARGB;

	jobject NavitGraphics;
	jobject Paint;

	jclass BitmapFactoryClass;
	jmethodID BitmapFactory_decodeFile, BitmapFactory_decodeResource;

	jclass BitmapClass;
	jmethodID Bitmap_getHeight, Bitmap_getWidth;

	jclass ContextClass;
	jmethodID Context_getResources;

	jclass ResourcesClass;
	jobject Resources;
	jmethodID Resources_getIdentifier;

	struct callback_list *cbl;
	struct window win;
};

struct graphics_font_priv
{
	int size;
};

struct graphics_gc_priv
{
	struct graphics_priv *gra;
	int linewidth;
	enum draw_mode_num mode;
	int a, r, g, b;
};

struct graphics_image_priv
{
	jobject Bitmap;
	int width;
	int height;
	struct point hot;
};

static GHashTable *image_cache_hash = NULL;

static int find_class_global(char *name, jclass *ret)
{
	//DBG // dbg(0,"EEnter\n");
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	*ret = (*jnienv2)->FindClass(jnienv2, name);
	if (!*ret)
	{
		//DBG // dbg(0, "Failed to get Class %s\n", name);
		return 0;
	}
	*ret = (*jnienv2)->NewGlobalRef(jnienv2, *ret);
	return 1;
}

static int find_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	//DBG // dbg(0,"EEnter\n");
	*ret = (*jnienv2)->GetMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		//DBG // dbg(0, "Failed to get Method %s with signature %s\n", name, args);
		return 0;
	}
	return 1;
}

static int find_static_method(jclass class, char *name, char *args, jmethodID *ret)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	//DBG // dbg(0,"EEnter\n");
	*ret = (*jnienv2)->GetStaticMethodID(jnienv2, class, name, args);
	if (*ret == NULL)
	{
		//DBG // dbg(0, "Failed to get static Method %s with signature %s\n", name, args);
		return 0;
	}
	return 1;
}

static void graphics_destroy(struct graphics_priv *gr)
{
}

static void font_destroy(struct graphics_font_priv *font)
{
	g_free(font);
}

static struct graphics_font_methods font_methods =
{ font_destroy };

static struct graphics_font_priv *font_new(struct graphics_priv *gr, struct graphics_font_methods *meth, char *font, int size, int flags)
{
	struct graphics_font_priv *ret=g_new0(struct graphics_font_priv, 1);
	*meth = font_methods;

	ret->size = size;
	return ret;
}

static void gc_destroy(struct graphics_gc_priv *gc)
{
	//DBG // dbg(0,"EEnter\n");

	g_free(gc);
}

static void gc_set_linewidth(struct graphics_gc_priv *gc, int w)
{
	gc->linewidth = w;
}


// UNUSED --------------
static void gc_set_dashes(struct graphics_priv *gra, struct graphics_gc_priv *gc, int w, int offset, int dash_list[], int order)
{
	//JNIEnv *jnienv2;
	//jnienv2 = jni_getenv();
	//(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_set_dashes, gc->gra->Paint, dash_list[0], order);
}


static void gc_set_foreground(struct graphics_gc_priv *gc, struct color *c)
{
	gc->r = c->r >> 8;
	gc->g = c->g >> 8;
	gc->b = c->b >> 8;
	gc->a = c->a >> 8;
}

static void gc_set_background(struct graphics_gc_priv *gc, struct color *c)
{
}

static struct graphics_gc_methods gc_methods =
{ gc_destroy, gc_set_linewidth, gc_set_dashes, gc_set_foreground, gc_set_background };

static struct graphics_gc_priv *gc_new(struct graphics_priv *gr, struct graphics_gc_methods *meth)
{
	////DBG // dbg(0,"EEnter\n");

	struct graphics_gc_priv *ret=g_new0(struct graphics_gc_priv, 1);
	*meth = gc_methods;

	ret->gra = gr;
	ret->a = ret->r = ret->g = ret->b = 255;
	ret->linewidth = 1;
	return ret;
}

static void image_destroy(struct graphics_image_priv *img)
{
	// unused?
}

static struct graphics_image_methods image_methods =
{ image_destroy };

static struct graphics_image_priv *
image_new(struct graphics_priv *gra, struct graphics_image_methods *meth, char *path, int *w, int *h, struct point *hot, int rotation)
{
	//DBG // dbg(0,"EEnter\n");

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);


	struct graphics_image_priv* ret = NULL;

	if (!g_hash_table_lookup_extended(image_cache_hash, path, NULL, (gpointer) & ret))
	{
		ret=g_new0(struct graphics_image_priv, 1);
		jstring string;
		int id;

		//dbg(0, "enter %s\n", path);
		if (!strncmp(path, "res/drawable/", 13))
		{
			jstring a = (*jnienv2)->NewStringUTF(jnienv2, "drawable");
			jstring b = (*jnienv2)->NewStringUTF(jnienv2, "com.zoffcc.applications.zanavi");
			char *path_noext = g_strdup(path + 13);
			char *pos = strrchr(path_noext, '.');
			if (pos)
			{
				*pos = '\0';
			}
			// dbg(0, "path_noext=%s a=%s b=%s\n", path_noext, a, b);
			string = (*jnienv2)->NewStringUTF(jnienv2, path_noext);
			g_free(path_noext);
			id = (*jnienv2)->CallIntMethod(jnienv2, gra->Resources, gra->Resources_getIdentifier, string, a, b);
			//DBG // dbg(0, "id=%d\n", id);
			//DBG // dbg(0,"JNI\n");
			if (id)
			{
				ret->Bitmap = (*jnienv2)->CallStaticObjectMethod(jnienv2, gra->BitmapFactoryClass, gra->BitmapFactory_decodeResource, gra->Resources, id);
			}
			(*jnienv2)->DeleteLocalRef(jnienv2, b);
			(*jnienv2)->DeleteLocalRef(jnienv2, a);
		}
		else
		{
			string = (*jnienv2)->NewStringUTF(jnienv2, path);
			//DBG // dbg(0,"JNI\n");
			ret->Bitmap = (*jnienv2)->CallStaticObjectMethod(jnienv2, gra->BitmapFactoryClass, gra->BitmapFactory_decodeFile, string);
			// there should be a check here, if we really want any rotation/scaling
			// otherwise the call is overkill
			//DBG // dbg(0,"JNI\n");
			ret->Bitmap = (*jnienv2)->CallStaticObjectMethod(jnienv2, gra->NavitGraphicsClass, gra->NavitGraphicsClass_rotate_and_scale_bitmap, ret->Bitmap, *w, *h, rotation);
		}
		//// dbg(1, "result=%p\n", ret->Bitmap);
		if (ret->Bitmap)
		{
			//DBG // dbg(0,"JNI\n");
			ret->Bitmap = (*jnienv2)->NewGlobalRef(jnienv2, ret->Bitmap);
			// ICS (*jnienv2)->DeleteLocalRef(jnienv2, ret->Bitmap);
			//DBG // dbg(0,"JNI\n");
			ret->width = (*jnienv2)->CallIntMethod(jnienv2, ret->Bitmap, gra->Bitmap_getWidth);
			//DBG // dbg(0,"JNI\n");
			ret->height = (*jnienv2)->CallIntMethod(jnienv2, ret->Bitmap, gra->Bitmap_getHeight);
			//// dbg(1, "w=%d h=%d for %s\n", ret->width, ret->height, path);
			ret->hot.x = ret->width / 2;
			ret->hot.y = ret->height / 2;
		}
		else
		{
			g_free(ret);
			ret = NULL;
			//DBG // dbg(0, "Failed to open %s\n", path);
		}
		//DBG // dbg(0,"JNI\n");
		(*jnienv2)->DeleteLocalRef(jnienv2, string);
		//DBG // dbg(0,"JNI\n");
		g_hash_table_insert(image_cache_hash, g_strdup(path), (gpointer) ret);
	}

	if (ret)
	{
		*w = ret->width;
		*h = ret->height;
		if (hot)
		{
			*hot = ret->hot;
		}
	}

	return ret;
}

static void initPaint(struct graphics_priv *gra, struct graphics_gc_priv *gc)
{
	//DBG // dbg(0,"EEnter\n");

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	float wf = gc->linewidth;
	(*jnienv2)->CallVoidMethod(jnienv2, gc->gra->Paint, gra->Paint_setStrokeWidth, wf);
	(*jnienv2)->CallVoidMethod(jnienv2, gc->gra->Paint, gra->Paint_setARGB, gc->a, gc->r, gc->g, gc->b);
}

static void draw_lines(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int count)
{
	//DBG // dbg(0,"EEnter\n");
	jint pc[count * 2];
	int i;
	jintArray points;

	if (count <= 0)
		return;

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	points = (*jnienv2)->NewIntArray(jnienv2, count * 2);
	for (i = 0; i < count; i++)
	{
		pc[i * 2] = p[i].x;
		pc[i * 2 + 1] = p[i].y;
	}
	initPaint(gra, gc);
	(*jnienv2)->SetIntArrayRegion(jnienv2, points, 0, count * 2, pc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polyline, gc->gra->Paint, points);
	(*jnienv2)->DeleteLocalRef(jnienv2, points);
}

static void draw_lines3(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int count, int order, int width, int dashes, struct color *c)
{
	//DBG // dbg(0,"EEnter\n");
	jint pc[count * 2];
	int i;
	jintArray points;
	if (count <= 0)
	{
		return;
	}

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	points = (*jnienv2)->NewIntArray(jnienv2, count * 2);
	for (i = 0; i < count; i++)
	{
		pc[i * 2] = p[i].x;
		pc[i * 2 + 1] = p[i].y;
	}
	//initPaint(gra, gc);
	(*jnienv2)->SetIntArrayRegion(jnienv2, points, 0, count * 2, pc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polyline3, points, order, width, dashes, c->r >> 8, c->g >> 8, c->b >> 8, c->a >> 8);
	(*jnienv2)->DeleteLocalRef(jnienv2, points);
}

static void draw_lines4(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int count, int order, int width, int type, int dashes, struct color *c)
{
	//DBG // dbg(0,"EEnter\n");

	// draw tunnel-street or bridge-street
	// type:1     -> tunnel
	// type:2     -> bridge
	// ------------------------------------------
	// type > 90  -> some signal (not a real line)

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();


	if (type > 90)
	{
		// "***" signal
		(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polyline4, NULL, order, width, type, 0, 0, 0, 0, 0);
	}
	else
	{
		jint pc[count * 2];
		int i;
		jintArray points;
		if (count <= 0)
		{
			return;
		}
		points = (*jnienv2)->NewIntArray(jnienv2, count * 2);
		for (i = 0; i < count; i++)
		{
			pc[i * 2] = p[i].x;
			pc[i * 2 + 1] = p[i].y;
		}
		// initPaint(gra, gc);
		(*jnienv2)->SetIntArrayRegion(jnienv2, points, 0, count * 2, pc);
		(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polyline4, points, order, width, type, dashes, c->r >> 8, c->g >> 8, c->b >> 8, c->a >> 8);
		(*jnienv2)->DeleteLocalRef(jnienv2, points);
	}
}

static void draw_lines2(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int count, int order, int oneway)
{
	//DBG // dbg(0,"EEnter\n");
	jint pc[count * 2];
	int i;
	jintArray points;
	if (count <= 0)
		return;

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	points = (*jnienv2)->NewIntArray(jnienv2, count * 2);
	for (i = 0; i < count; i++)
	{
		pc[i * 2] = p[i].x;
		pc[i * 2 + 1] = p[i].y;
	}
	initPaint(gra, gc);
	(*jnienv2)->SetIntArrayRegion(jnienv2, points, 0, count * 2, pc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polyline2, gc->gra->Paint, points, order, oneway);
	(*jnienv2)->DeleteLocalRef(jnienv2, points);
}

static void draw_lines_dashed(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int count, int order, int oneway)
{
	jint pc[count * 2];
	int i;
	jintArray points;
	if (count <= 0)
		return;

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	points = (*jnienv2)->NewIntArray(jnienv2, count * 2);
	for (i = 0; i < count; i++)
	{
		pc[i * 2] = p[i].x;
		pc[i * 2 + 1] = p[i].y;
	}
	initPaint(gra, gc);
	(*jnienv2)->SetIntArrayRegion(jnienv2, points, 0, count * 2, pc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polyline_dashed, gc->gra->Paint, points, order, oneway);
	(*jnienv2)->DeleteLocalRef(jnienv2, points);
}

static void draw_polygon(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int count)
{
	jint pc[count * 2];
	int i;
	jintArray points;
	if (count <= 0)
		return;

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	points = (*jnienv2)->NewIntArray(jnienv2, count * 2);
	for (i = 0; i < count; i++)
	{
		pc[i * 2] = p[i].x;
		pc[i * 2 + 1] = p[i].y;
	}
	initPaint(gra, gc);
	(*jnienv2)->SetIntArrayRegion(jnienv2, points, 0, count * 2, pc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polygon, gc->gra->Paint, points);
	(*jnienv2)->DeleteLocalRef(jnienv2, points);
}

static void draw_polygon2(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int count, int order, int oneway)
{
	jint pc[count * 2];
	int i;
	jintArray points;
	if (count <= 0)
		return;

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	points = (*jnienv2)->NewIntArray(jnienv2, count * 2);
	for (i = 0; i < count; i++)
	{
		pc[i * 2] = p[i].x;
		pc[i * 2 + 1] = p[i].y;
	}
	initPaint(gra, gc);
	(*jnienv2)->SetIntArrayRegion(jnienv2, points, 0, count * 2, pc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_polygon2, gc->gra->Paint, points, order, oneway);
	(*jnienv2)->DeleteLocalRef(jnienv2, points);
}

static void draw_rectangle(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int w, int h)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	initPaint(gra, gc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_rectangle, gc->gra->Paint, p->x, p->y, w, h);
}

static void draw_circle(struct graphics_priv *gra, struct graphics_gc_priv *gc, struct point *p, int r)
{
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	// use gc->linewidth as width;

	initPaint(gra, gc);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_circle, gc->gra->Paint, p->x, p->y, r);
}

static void draw_text(struct graphics_priv *gra, struct graphics_gc_priv *fg, struct graphics_gc_priv *bg, struct graphics_font_priv *font, char *text, struct point *p, int dx, int dy)
{
	//// dbg(1, "enter %s\n", text);
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	initPaint(gra, fg);
	jstring string = (*jnienv2)->NewStringUTF(jnienv2, text);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_text, fg->gra->Paint, p->x, p->y, string, font->size, dx, dy);
	(*jnienv2)->DeleteLocalRef(jnienv2, string);
}

static void draw_image(struct graphics_priv *gra, struct graphics_gc_priv *fg, struct point *p, struct graphics_image_priv *img)
{
	//DBG // dbg(0,"EEnter\n");

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	//// dbg(1, "enter %p\n", img);
	initPaint(gra, fg);
	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_image, fg->gra->Paint, p->x, p->y, img->Bitmap);

}

static void draw_bigmap(struct graphics_priv *gra, struct graphics_gc_priv *fg, int yaw, int order, float clat, float clng, int x, int y, int scx, int scy, int px, int py, int valid)
{
	//DBG // dbg(0,"EEnter\n");

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_bigmap, yaw, order, clat, clng, x, y, scx, scy, px, py, valid);

	//DBG // dbg(0,"leave\n");
}

static void draw_image_warp(struct graphics_priv *gr, struct graphics_gc_priv *fg, struct point *p, int count, char *data)
{
	//dbg(0,"draw_image_warp:filename=%s\n", data);
	//dbg(0,"draw_image_warp:count=%d\n", count);

/*
        void imlib_render_image_on_drawable_skewed(int source_x, int source_y,
                                                   int source_width,
                                                   int source_height,
                                                   int destination_x,
                                                   int destination_y,
                                                   int h_angle_x, int h_angle_y,
                                                   int v_angle_x, int v_angle_y);

	if (count == 3) {
		imlib_render_image_on_drawable_skewed(0, 0, w, h, p[0].x, p[0].y, p[1].x-p[0].x, p[1].y-p[0].y, p[2].x-p[0].x, p[2].y-p[0].y);
	}
	if (count == 2) {
		imlib_render_image_on_drawable_skewed(0, 0, w, h, p[0].x, p[0].y, p[1].x-p[0].x, 0, 0, p[1].y-p[0].y);
	}
	if (count == 1) {
		imlib_render_image_on_drawable_skewed(0, 0, w, h, p[0].x-w/2, p[0].y-h/2, w, 0, 0, h);
	}

*/

	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	jstring string = (*jnienv2)->NewStringUTF(jnienv2, data);

	if (count == 3)
	{
		/* 0 1
        	   2   */
		(*jnienv2)->CallVoidMethod(jnienv2, gr->NavitGraphics, gr->NavitGraphics_draw_image_warp, string, count, p[0].x, p[0].y, p[1].x, p[1].y, p[2].x, p[2].y);
	}
	else if (count == 2)
	{
		/* 0 
        	     1 */
		(*jnienv2)->CallVoidMethod(jnienv2, gr->NavitGraphics, gr->NavitGraphics_draw_image_warp, string, count, p[0].x, p[0].y, p[1].x, p[1].y, 0, 0);
	}
	else if (count == 1)
	{
		/* 
                   0 
        	     */
		(*jnienv2)->CallVoidMethod(jnienv2, gr->NavitGraphics, gr->NavitGraphics_draw_image_warp, string, count, p[0].x, p[0].y, 0, 0, 0, 0);
	}

	(*jnienv2)->DeleteLocalRef(jnienv2, string);

}

static void draw_restore(struct graphics_priv *gr, struct point *p, int w, int h)
{
}

static void draw_drag(struct graphics_priv *gra, struct point *p)
{
	//DBG // dbg(0,"EEnter\n");
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_drag, p ? p->x : 0, p ? p->y : 0);
}

static void background_gc(struct graphics_priv *gr, struct graphics_gc_priv *gc)
{
}

static void draw_mode(struct graphics_priv *gra, enum draw_mode_num mode)
{
	//DBG // dbg(0,"EEnter\n");
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_draw_mode, (int) mode);
}

static struct graphics_priv * overlay_new(const char* name, struct graphics_priv *gr, struct graphics_methods *meth, struct point *p, int w, int h, int alpha, int wraparound);

static void *
get_data(struct graphics_priv *this, const char *type)
{
	//DBG // dbg(0,"EEnter\n");

	if (strcmp(type, "window"))
	{
		return NULL;
	}

	return &this->win;
}

static void image_free(struct graphics_priv *gr, struct graphics_image_priv *priv)
{
}

static void get_text_bbox(struct graphics_priv *gr, struct graphics_font_priv *font, char *text, int dx, int dy, struct point *ret, int estimate)
{
	////DBG // dbg(0,"EEnter\n");

	// this is a rough estimate!! otherwise java methods would be called, and thats too slow!

	int len = g_utf8_strlen(text, -1);
	int xMin = 0;
	int yMin = 0;
	int yMax = 13 * font->size / 256;
	int xMax = 9 * font->size * len / 256;

	ret[0].x = xMin;
	ret[0].y = -yMin;
	ret[1].x = xMin;
	ret[1].y = -yMax;
	ret[2].x = xMax;
	ret[2].y = -yMax;
	ret[3].x = xMax;
	ret[3].y = -yMin;
}

static void overlay_disable(struct graphics_priv *gra, int disable)
{
	// UNUSED ----

	//DBG // dbg(0,"EEnter\n");
	//JNIEnv *jnienv2;
	//jnienv2 = jni_getenv();
	//
	//(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_overlay_disable, disable);
}

static void overlay_resize(struct graphics_priv *gra, struct point *pnt, int w, int h, int alpha, int wraparound)
{
	// UNUSED ----

	//DBG // dbg(0,"EEnter\n");
	//JNIEnv *jnienv2;
	//jnienv2 = jni_getenv();
	//
	//(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_overlay_resize, pnt ? pnt->x : 0, pnt ? pnt->y : 0, w, h, alpha, wraparound);
}

static int set_attr(struct graphics_priv *gra, struct attr *attr)
{
	//DBG // dbg(0,"EEnter\n");
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	switch (attr->type)
	{
		case attr_use_camera:
			(*jnienv2)->CallVoidMethod(jnienv2, gra->NavitGraphics, gra->NavitGraphics_SetCamera, attr->u.num);
			return 1;
		default:
			return 0;
	}
}

static struct graphics_methods graphics_methods =
{ graphics_destroy, draw_mode, draw_lines, draw_lines2, draw_lines3, draw_lines4, draw_lines_dashed, draw_polygon, draw_polygon2, draw_rectangle, draw_circle, draw_text, draw_image, draw_bigmap, draw_image_warp, draw_restore, draw_drag, font_new, gc_new, background_gc, overlay_new, image_new, get_data,
		image_free, get_text_bbox, overlay_disable, overlay_resize, set_attr, };

/*
static void resize_callback(struct graphics_priv *gra, int w, int h)
{
	//DBG // dbg(0,"EEnter\n");
	// //DBG // dbg(0,"w=%d h=%d ok\n",w,h);
	// callback_list_call_attr_2(gra->cbl, attr_resize, (void *) w, (void *) h);
	navit_resize(attr_resize, this_);
}
*/

/*
static void motion_callback(struct graphics_priv *gra, int x, int y)
{
	//DBG // dbg(0,"EEnter\n");

	struct point p;
	p.x = x;
	p.y = y;
	callback_list_call_attr_1(gra->cbl, attr_motion, (void *) &p);
}
*/

/*
static void keypress_callback(struct graphics_priv *gra, char *s)
{
	//DBG // dbg(0,"EEnter\n");
	callback_list_call_attr_1(gra->cbl, attr_keypress, s);
}
*/

/*
static void button_callback(struct graphics_priv *gra, int pressed, int button, int x, int y)
{
	//DBG // dbg(0,"EEnter\n");

	struct point p;
	p.x = x;
	p.y = y;
	// //DBG // dbg(0,"XXXXXXXYYYYYYYYY\n");
	callback_list_call_attr_3(gra->cbl, attr_button, (void *) pressed, (void *) button, (void *) &p);
}
*/

static int set_activity(jobject graphics)
{
	// dbg(0,"EEnter\n");
	// ---- DISABLED -------
	return 1;
}

static int graphics_android_init(const char* name, struct graphics_priv *ret, struct graphics_priv *parent, struct point *pnt, int w, int h, int alpha, int wraparound, int use_camera)
{
	struct callback *cb;

	// overwrite JNIenv!!
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	// dbg(0, "at 2 jnienv2=%p\n", jnienv2);
	int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);

	//DBG // dbg(0,"a1\n");
	if (!find_class_global("android/graphics/Paint", &ret->PaintClass))
		return 0;
	//DBG // dbg(0,"a2\n");
	if (!find_method(ret->PaintClass, "<init>", "(I)V", &ret->Paint_init))
		return 0;
	//DBG // dbg(0,"a3\n");
	if (!find_method(ret->PaintClass, "setARGB", "(IIII)V", &ret->Paint_setARGB))
		return 0;
	//DBG // dbg(0,"a4\n");
	if (!find_method(ret->PaintClass, "setStrokeWidth", "(F)V", &ret->Paint_setStrokeWidth))
		return 0;

	//DBG // dbg(0,"a4.1\n");
	if (!find_class_global("android/graphics/BitmapFactory", &ret->BitmapFactoryClass))
		return 0;
	//DBG // dbg(0,"a4.2\n");
	if (!find_static_method(ret->BitmapFactoryClass, "decodeFile", "(Ljava/lang/String;)Landroid/graphics/Bitmap;", &ret->BitmapFactory_decodeFile))
		return 0;
	//DBG // dbg(0,"a4.3\n");
	if (!find_static_method(ret->BitmapFactoryClass, "decodeResource", "(Landroid/content/res/Resources;I)Landroid/graphics/Bitmap;", &ret->BitmapFactory_decodeResource))
		return 0;

	//DBG // dbg(0,"a4.4\n");
	if (!find_class_global("android/graphics/Bitmap", &ret->BitmapClass))
		return 0;
	//DBG // dbg(0,"a4.5\n");
	if (!find_method(ret->BitmapClass, "getHeight", "()I", &ret->Bitmap_getHeight))
		return 0;
	//DBG // dbg(0,"a4.6\n");
	if (!find_method(ret->BitmapClass, "getWidth", "()I", &ret->Bitmap_getWidth))
		return 0;
	//DBG // dbg(0,"a4.7\n");
	if (!find_class_global("android/content/Context", &ret->ContextClass))
		return 0;
	//DBG // dbg(0,"a4.8\n");
	if (!find_method(ret->ContextClass, "getResources", "()Landroid/content/res/Resources;", &ret->Context_getResources))
		return 0;
	//DBG // dbg(0,"a5\n");
	ret->Resources = (*jnienv2)->CallObjectMethod(jnienv2, android_activity, ret->Context_getResources);
	//DBG // dbg(0,"a6\n");
	if (ret->Resources)
	{
		ret->Resources = (*jnienv2)->NewGlobalRef(jnienv2, ret->Resources);
	}
	//DBG // dbg(0,"a7\n");
	if (!find_class_global("android/content/res/Resources", &ret->ResourcesClass))
		return 0;
	//DBG // dbg(0,"a8\n");
	if (!find_method(ret->ResourcesClass, "getIdentifier", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", &ret->Resources_getIdentifier))
		return 0;
	//DBG // dbg(0,"a9\n");
	if (!find_class_global("com/zoffcc/applications/zanavi/NavitGraphics", &ret->NavitGraphicsClass))
		return 0;
	//DBG // dbg(0, "at 3\n");


	// --------------- Init the new Graphics Object here -----------------
	// --------------- Init the new Graphics Object here -----------------
	// --------------- Init the new Graphics Object here -----------------
	// dbg(0,"Init the new Graphics Object here: %s\n", name);

	//DBG // dbg(0, "at 4 android_activity=%p\n", android_activity);




	if (NavitClass2 == NULL)
	{
		if (!android_find_class_global("com/zoffcc/applications/zanavi/Navit", &NavitClass2))
		{
			NavitClass2 = NULL;
			return 0;
		}
	}

	if (!find_static_method(NavitClass2, "get_graphics_object_by_name", "(Ljava/lang/String;)Lcom/zoffcc/applications/zanavi/NavitGraphics;", &Navit_get_graphics_object_by_name))
		return 0;


	/// --old-- ret->NavitGraphics = (*jnienv2)->NewObject(jnienv2, ret->NavitGraphicsClass, cid, android_activity, parent ? parent->NavitGraphics : NULL, pnt ? pnt->x : 0, pnt ? pnt->y : 0, w, h, alpha, wraparound, use_camera);

	/// --new--
	jstring string = (*jnienv2)->NewStringUTF(jnienv2, name);
	ret->NavitGraphics = (*jnienv2)->CallStaticObjectMethod(jnienv2, NavitClass2, Navit_get_graphics_object_by_name, string);
	(*jnienv2)->DeleteLocalRef(jnienv2, string);
	/// --new--


	// dbg(0, "result=%p\n", ret->NavitGraphics);
	if (ret->NavitGraphics)
	{
		ret->NavitGraphics = (*jnienv2)->NewGlobalRef(jnienv2, ret->NavitGraphics);
	}

	// --------------- Init the new Graphics Object here -----------------
	// --------------- Init the new Graphics Object here -----------------
	// --------------- Init the new Graphics Object here -----------------






	/* Create a single global Paint, otherwise android will quickly run out
	 * of global refs.*/
	/* 0x101 = text kerning (default), antialiasing */
	ret->Paint = (*jnienv2)->NewObject(jnienv2, ret->PaintClass, ret->Paint_init, 0x101);

	//DBG // dbg(0, "l result=%p\n", ret->Paint);
	if (ret->Paint)
	{
		ret->Paint = (*jnienv2)->NewGlobalRef(jnienv2, ret->Paint);
	}
	//DBG // dbg(0, "g result=%p\n", ret->Paint);


	// public Bitmap rotate_and_scale_bitmap(Bitmap in, int w, int h, int angle)

	if (!find_static_method(ret->NavitGraphicsClass, "rotate_and_scale_bitmap", "(Landroid/graphics/Bitmap;III)Landroid/graphics/Bitmap;", &ret->NavitGraphicsClass_rotate_and_scale_bitmap))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_polyline", "(Landroid/graphics/Paint;[I)V", &ret->NavitGraphics_draw_polyline))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_polyline2", "(Landroid/graphics/Paint;[III)V", &ret->NavitGraphics_draw_polyline2))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_polyline3", "([IIIIIIII)V", &ret->NavitGraphics_draw_polyline3))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_polyline4", "([IIIIIIIII)V", &ret->NavitGraphics_draw_polyline4))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_polyline_dashed", "(Landroid/graphics/Paint;[III)V", &ret->NavitGraphics_draw_polyline_dashed))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "set_dashes", "(Landroid/graphics/Paint;II)V", &ret->NavitGraphics_set_dashes))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_polygon", "(Landroid/graphics/Paint;[I)V", &ret->NavitGraphics_draw_polygon))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_polygon2", "(Landroid/graphics/Paint;[III)V", &ret->NavitGraphics_draw_polygon2))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_rectangle", "(Landroid/graphics/Paint;IIII)V", &ret->NavitGraphics_draw_rectangle))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_circle", "(Landroid/graphics/Paint;III)V", &ret->NavitGraphics_draw_circle))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_text", "(Landroid/graphics/Paint;IILjava/lang/String;III)V", &ret->NavitGraphics_draw_text))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_image", "(Landroid/graphics/Paint;IILandroid/graphics/Bitmap;)V", &ret->NavitGraphics_draw_image))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_bigmap", "(IIFFIIIIIII)V", &ret->NavitGraphics_draw_bigmap))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_warp", "(Ljava/lang/String;IIIIIII)V", &ret->NavitGraphics_draw_image_warp))
		return 0;

	if (!find_method(ret->NavitGraphicsClass, "draw_mode", "(I)V", &ret->NavitGraphics_draw_mode))
		return 0;
	if (!find_method(ret->NavitGraphicsClass, "draw_drag", "(II)V", &ret->NavitGraphics_draw_drag))
		return 0;
	//if (!find_method(ret->NavitGraphicsClass, "overlay_disable", "(I)V", &ret->NavitGraphics_overlay_disable))
	//	return 0;
	//if (!find_method(ret->NavitGraphicsClass, "overlay_resize", "(IIIIII)V", &ret->NavitGraphics_overlay_resize))
	//	return 0;
	/*
	if (!find_method(ret->NavitGraphicsClass, "SetCamera", "(I)V", &ret->NavitGraphics_SetCamera))
		return 0;
	*/

	//DBG // dbg(0,"99\n");
#if 0
	set_activity(ret->NavitGraphics);
#endif
	return 1;
}

static int graphics_android_fullscreen(struct window *win, int on)
{
	return 1;
}

static jclass NavitClass;
static jmethodID Navit_disableSuspend, Navit_exit;

static void graphics_android_disable_suspend(struct window *win)
{
	//DBG // dbg(0,"enter\n");
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	(*jnienv2)->CallVoidMethod(jnienv2, android_activity, Navit_disableSuspend);
}

static struct graphics_priv *
graphics_android_new(struct navit *nav, struct graphics_methods *meth, struct attr **attrs, struct callback_list *cbl)
{
	// dbg(0,"EEnter\n");

	int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);

	struct graphics_priv *ret;
	struct attr *attr;
	int use_camera = 0;

	// dbg(0,"event system - start\n");
	if (!event_request_system("android", "graphics_android"))
	{
		return NULL;
	}
	// dbg(0,"event system - end\n");

	ret=g_new0(struct graphics_priv, 1);
	ret->cbl = cbl;
	*meth = graphics_methods;
	ret->win.priv = ret;
	ret->win.fullscreen = graphics_android_fullscreen;
	ret->win.disable_suspend = graphics_android_disable_suspend;
	if ((attr = attr_search(attrs, NULL, attr_use_camera)))
	{
		use_camera = attr->u.num;
	}
	image_cache_hash = g_hash_table_new(g_str_hash, g_str_equal);


	// --------------- try to get a new Graphics Object  -----------------
	// --------------- try to get a new Graphics Object  -----------------
	// --------------- try to get a new Graphics Object  -----------------
	// dbg(0,"try to get a new Graphics Object\n");
	if (graphics_android_init("type:map-main", ret, NULL, NULL, 0, 0, 0, 0, use_camera))
	{
		////DBG // dbg(0,"returning %p\n",ret);
		return ret;
	}
	else
	{
		g_free(ret);
		return NULL;
	}
}

static struct graphics_priv *
overlay_new(const char* name, struct graphics_priv *gr, struct graphics_methods *meth, struct point *p, int w, int h, int alpha, int wraparound)
{
	// dbg(0,"EEnter\n");

	int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);

	struct graphics_priv *ret=g_new0(struct graphics_priv, 1);
	*meth = graphics_methods;

	// --------------- try to get a new Graphics Object  -----------------
	// --------------- try to get a new Graphics Object  -----------------
	// --------------- try to get a new Graphics Object  -----------------
	// dbg(0,"try to get a new Graphics Object: %s\n", name);
	if (graphics_android_init(name ,ret, gr, p, w, h, alpha, wraparound, 0))
	{
		// dbg(0, "returning %p\n", ret);
		return ret;
	}
	else
	{
		g_free(ret);
		return NULL;
	}
}

static void event_android_main_loop_run(void)
{
	// dbg(0, "enter\n");
}

static void event_android_main_loop_quit(void)
{
	// dbg(0, "enter\n");

	// overwrite JNIenv!!
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);

	// ******* exit(0);
	(*jnienv2)->CallVoidMethod(jnienv2, android_activity, Navit_exit);
}

static jclass NavitTimeoutClass;
static jmethodID NavitTimeout_init;
static jmethodID NavitTimeout_remove;

static jclass NavitIdleClass;
static jmethodID NavitIdle_init;
static jmethodID NavitIdle_remove;

static jclass NavitWatchClass;
static jmethodID NavitWatch_init;
static jmethodID NavitWatch_remove;

static struct event_watch *
event_android_add_watch(void *h, enum event_watch_cond cond, struct callback *cb)
{
	// dbg(0,"enter\n");
	jobject ret;
	ret = (*jnienv)->NewObject(jnienv, NavitWatchClass, NavitWatch_init, (int) h, (int) cond, (int) cb);
	// //DBG // dbg(0,"result for %p,%d,%p=%p\n",h,cond,cb,ret);
	if (ret)
	{
		//DBG // dbg(0,"result for %p,%p\n",h,ret);
		ret = (*jnienv)->NewGlobalRef(jnienv, ret);
		//DBG // dbg(0,"g ret %p\n",ret);
	}
	return (struct event_watch *) ret;
}

static void event_android_remove_watch(struct event_watch *ev)
{
	// dbg(0,"enter\n");
	if (ev)
	{
		//DBG // dbg(0,"enter %p\n",ev);
		jobject obj = (jobject) ev;
		// maybe need this ? ICS  obj = (*jnienv)->NewGlobalRef(jnienv, obj);
		(*jnienv)->CallVoidMethod(jnienv, obj, NavitWatch_remove);
		// ICS (*jnienv)->DeleteGlobalRef(jnienv, obj);
	}
}

static struct event_timeout *
event_android_add_timeout(int timeout, int multi, struct callback *cb)
{
	// dbg(0,"EEnter\n");

	// overwrite JNIenv!!
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);

	// timeout -> delay in milliseconds

	jobject ret;
	jobject ret_g = NULL;
	ret = (*jnienv2)->NewObject(jnienv2, NavitTimeoutClass, NavitTimeout_init, timeout, multi, (int) cb);
	//// dbg(0, "result for %d,%d,%p\n", timeout, multi, cb);

	if (ret)
	{
		//// dbg(0,"l ret=%p\n",ret);
		ret_g = (*jnienv2)->NewGlobalRef(jnienv2, ret);
		// dbg(0,"g ret=%p\n",ret_g);
	}
	//// dbg(0,"leave\n");
	return (struct event_timeout *) ret_g;
}

static void event_android_remove_timeout(struct event_timeout *to)
{
	// dbg(0,"EEnter\n");

	// overwrite JNIenv!!
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	// int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);


	if (to)
	{
		// dbg(0, "remove %p\n", to);
		jobject obj = (jobject) to;
		(*jnienv2)->CallVoidMethod(jnienv2, obj, NavitTimeout_remove);
		// ICS
		//// dbg(0, "remove 1\n");
		(*jnienv2)->DeleteGlobalRef(jnienv2, obj);
		//// dbg(0, "remove 2\n");
	}
}

static struct event_idle *
event_android_add_idle(int priority, struct callback *cb)
{
	// ----------------------------------------------------
	// ----------------------------------------------------
	// "priority" param is now misused here as "multi"
	// priority == 1000 -> set multi = 0
	// ----------------------------------------------------
	// ----------------------------------------------------

	//// dbg(0,"EEnter\n");

#if 0
	jobject ret;
	// dbg(1,"enter\n");
	ret=(*jnienv)->NewObject(jnienv, NavitIdleClass, NavitIdle_init, (int)cb);
	// dbg(1,"result for %p=%p\n",cb,ret);
	if (ret)
	(*jnienv)->NewGlobalRef(jnienv, ret);
	return (struct event_idle *)ret;
#endif
	// ----- xxxxxxxx ------
	if (priority == 1000)
	{
		//dbg(0,"event add idle timeout=10 multi=0\n", priority);
		return (struct event_idle *) event_android_add_timeout(10, 0, cb);
	}
	else
	{
		//dbg(0,"event add idle timeout=%d multi=1\n", priority);
		return (struct event_idle *) event_android_add_timeout(priority, 1, cb);
	}
}

static void event_android_remove_idle(struct event_idle *ev)
{
	//// dbg(0,"EEnter\n");

#if 0
	// dbg(1,"enter %p\n",ev);
	if (ev)
	{
		jobject obj=(jobject )ev;
		(*jnienv)->CallVoidMethod(jnienv, obj, NavitIdle_remove);
		(*jnienv)->DeleteGlobalRef(jnienv, obj);
	}
#endif
	event_android_remove_timeout((struct event_timeout *) ev);
}

static void event_android_call_callback(struct callback_list *cb)
{
	//DBG // dbg(0,"enter\n");
}

static struct event_methods event_android_methods =
{ event_android_main_loop_run, event_android_main_loop_quit, event_android_add_watch, event_android_remove_watch, event_android_add_timeout, event_android_remove_timeout, event_android_add_idle, event_android_remove_idle, event_android_call_callback, };

static struct event_priv *
event_android_new(struct event_methods *meth)
{

	// overwrite JNIenv!!
	JNIEnv *jnienv2;
	jnienv2 = jni_getenv();

	// int thread_id = gettid();
	// dbg(0, "THREAD ID=%d\n", thread_id);


	// dbg(0,"enter\n");
	if (!find_class_global("com/zoffcc/applications/zanavi/NavitTimeout", &NavitTimeoutClass))
		return NULL;

	// dbg(0,"ev 001\n");
	NavitTimeout_init = (*jnienv2)->GetMethodID(jnienv2, NavitTimeoutClass, "<init>", "(IZI)V");
	if (NavitTimeout_init == NULL)
		return NULL;
	// dbg(0,"ev 002\n");
	NavitTimeout_remove = (*jnienv2)->GetMethodID(jnienv2, NavitTimeoutClass, "remove", "()V");
	if (NavitTimeout_remove == NULL)
		return NULL;
#if 0
	if (!find_class_global("com/zoffcc/applications/zanavi/NavitIdle", &NavitIdleClass))
	return NULL;
	NavitIdle_init = (*jnienv2)->GetMethodID(jnienv2, NavitIdleClass, "<init>", "(I)V");
	if (NavitIdle_init == NULL)
	return NULL;
	NavitIdle_remove = (*jnienv2)->GetMethodID(jnienv2, NavitIdleClass, "remove", "()V");
	if (NavitIdle_remove == NULL)
	return NULL;
#endif

	// dbg(0,"ev 003\n");
	if (!find_class_global("com/zoffcc/applications/zanavi/NavitWatch", &NavitWatchClass))
		return NULL;
	// dbg(0,"ev 004\n");
	NavitWatch_init = (*jnienv2)->GetMethodID(jnienv2, NavitWatchClass, "<init>", "(III)V");
	if (NavitWatch_init == NULL)
		return NULL;
	// dbg(0,"ev 005\n");
	NavitWatch_remove = (*jnienv2)->GetMethodID(jnienv2, NavitWatchClass, "remove", "()V");
	if (NavitWatch_remove == NULL)
		return NULL;

	// dbg(0,"ev 006\n");
	if (!find_class_global("com/zoffcc/applications/zanavi/Navit", &NavitClass))
		return NULL;
	// dbg(0,"ev 007\n");
	Navit_disableSuspend = (*jnienv2)->GetMethodID(jnienv2, NavitClass, "disableSuspend", "()V");
	if (Navit_disableSuspend == NULL)
		return NULL;
	// dbg(0,"ev 008\n");
	Navit_exit = (*jnienv2)->GetMethodID(jnienv2, NavitClass, "exit2", "()V");
	if (Navit_exit == NULL)
		return NULL;

	// dbg(0,"ok\n");

	*meth = event_android_methods;
	return NULL;
}

void plugin_init(void)
{
	dbg(0,"enter\n");
	plugin_register_graphics_type("android", graphics_android_new);
	plugin_register_event_type("android", event_android_new);
}

