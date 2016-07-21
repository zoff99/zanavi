/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2012 - 2015 Zoff <zoff@zoff.cc>
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

#ifndef NAVIT_DEBUG_H
#define NAVIT_DEBUG_H

/*
 *
 *
 *
 * define some debug stuff here
 *
 *
 *
 */
#define NAVIT_ATTR_SAFETY_CHECK 1 // leave this always ON !!
// #define NAVIT_FUNC_CALLS_DEBUG_PRINT 1
// #define NAVIT_SAY_DEBUG_PRINT 1
// #define NAVIT_MEASURE_TIME_DEBUG 1
// #define NAVIT_CALLBACK_DEBUG_PRINT 1
// #define NAVIT_ROUTING_DEBUG_PRINT 1
// #define NAVIT_GPS_DIRECTION_DAMPING 1
// #define NAVIT_FREE_TEXT_DEBUG_PRINT 1
// #define NAVIT_ANGLE_LIST_DEBUG_PRINT_DRAW 1
// #define NAVIT_ANGLE_LIST_DEBUG_PRINT_2 1
// #define NAVIT_ANGLE_LIST_DEBUG_PRINT_1 1
// #define NAVIT_DEBUG_BAREMETAL 1
// #define NAVIT_DEBUG_COORD_LIST 1
// #define NAVIT_DEBUG_COORD_DIE2TE_LIST 1
//
#define NAVIT_DEBUG_SPEECH_POSITION 1
//
#define NAVIT_TRACKING_SHOW_REAL_GPS_POS 1
#define NAVIT_NAVIGATION_REMOVE_DUPL_WAYS 1
#define NAVIT_SHOW_ROUTE_ARROWS 1
#define NAVIT_CALC_ALLOWED_NEXT_WAYS 1
#define NAVIT_CALC_LANES 1
#define NAVIT_TRACKING_STICK_TO_ROUTE 1
//
//
#define CAR_STICK_TO_ROUTE_001 1 // stick to route harder in car mode
//
// #define NAVIT_ROUTE_DIJKSTRA_REVERSE 1
//
// #define DEBUG_GLIB_MALLOC 1 // <-- does not work yet!
// #define DEBUG_GLIB_FREE 1
// #define DEBUG_GLIB_REALLY_FREE 1
// #define DEBUG_GLIB_MEM_FUNCTIONS 1

extern int global_func_indent_counter;
extern const char* global_func_indent_spaces;

/*
 *
 *
 *
 *
 *
 *
 *
 */

#ifdef __cplusplus
extern "C"
{
#endif

#include <stdarg.h>
#include <string.h>

#ifdef _MSC_VER
#define __PRETTY_FUNCTION__ __FUNCTION__
#endif

#ifndef HAVE_API_ANDROID
#define g_free_func g_free
#endif

/** Possible debug levels (inspired by SLF4J). */
typedef enum {
/** Internal use only, do not use for logging. */
lvl_unset=-1,

/** Informational message. Should make sense to non-programmers. */
lvl_info, // = 0

/** Error: something did not work. */
lvl_error, // = 1

/** Warning: something may not have worked. */
lvl_warning, // = 2

/** Debug output: (almost) anything goes. */
lvl_debug // = 3

} dbg_level; 


extern int debug_level;


#ifdef _DEBUG_BUILD_

#define dbg_str2(x) #x
#define dbg_str1(x) dbg_str2(x)
#define dbg_module dbg_str1(MODULE)
#define dbg(level,...) { if (debug_level >= level) debug_printf(level,dbg_module,strlen(dbg_module),__PRETTY_FUNCTION__, strlen(__PRETTY_FUNCTION__),1,__VA_ARGS__); }
#define dbg_func(level,indent,...) { if (debug_level >= level) debug_printf_func(level,indent,dbg_module,strlen(dbg_module),__PRETTY_FUNCTION__, strlen(__PRETTY_FUNCTION__),1,__VA_ARGS__); }
#define dbg_assert(expr) ((expr) ? (void) 0 : debug_assert_fail(dbg_module,strlen(dbg_module),__PRETTY_FUNCTION__, strlen(__PRETTY_FUNCTION__),__FILE__,__LINE__,dbg_str1(expr)))
#define tests_dbg(level,...) debug_for_tests_printf(level,__VA_ARGS__);

#else

#define dbg_str2(x) #x
#define dbg_str1(x) #x
#define dbg_module dbg_str1(MODULE)
#define dbg(level,...) #level
#define dbg_func(level,indent,...) #level
#define dbg_assert(expr) ((expr) ? (void) 0 : (void) 0)
#define tests_dbg(level,...) #level

#endif



//#ifdef DEBUG_MALLOC
// ----------------
//#undef g_new
//#undef g_new0
//#define g_new(type, size) (type *)debug_malloc(__FILE__,__LINE__,__PRETTY_FUNCTION__,sizeof(type)*(size))
//#define g_new0(type, size) (type *)debug_malloc0(__FILE__,__LINE__,__PRETTY_FUNCTION__,sizeof(type)*(size))
//#define g_malloc(size) debug_malloc(__FILE__,__LINE__,__PRETTY_FUNCTION__,(size))
//#define g_malloc0(size) debug_malloc0(__FILE__,__LINE__,__PRETTY_FUNCTION__,(size))
//#define g_realloc(ptr,size) debug_realloc(__FILE__,__LINE__,__PRETTY_FUNCTION__,ptr,(size))
//#define g_free(ptr) debug_free(__FILE__,__LINE__,__PRETTY_FUNCTION__,ptr)
//#define g_strdup(ptr) debug_strdup(__FILE__,__LINE__,__PRETTY_FUNCTION__,ptr)
//#define g_strdup_printf(fmt...) debug_guard(__FILE__,__LINE__,__PRETTY_FUNCTION__,g_strdup_printf(fmt))
//#define graphics_icon_path(x) debug_guard(__FILE__,__LINE__,__PRETTY_FUNCTION__,graphics_icon_path(x))
//#define dbg_guard(x) debug_guard(__FILE__,__LINE__,__PRETTY_FUNCTION__,x)
//#define g_free_func debug_free_func
// ----------------
//#else
// ----------------
//#define g_free_func g_free
#define dbg_guard(x) x
// ----------------
//#endif

/* prototypes */
struct attr;
struct debug;
void debug_init(const char *program_name);
void debug_level_set(const char *name, int level);
struct debug *debug_new(struct attr *parent, struct attr **attrs);
int debug_level_get(const char *name);
void debug_vprintf(int level, const char *module, const int mlen, const char *function, const int flen, int prefix, const char *fmt, va_list ap);
void debug_vprintf_func(int level, int indent, const char *module, const int mlen, const char *function, const int flen, int prefix, const char *fmt, va_list ap);
void debug_printf(int level, const char *module, const int mlen, const char *function, const int flen, int prefix, const char *fmt, ...);
void debug_for_tests_vprintf(int level, const char *fmt, va_list ap);
void debug_for_tests_printf(int level, const char *fmt, ...);
void debug_printf_func(int level, int indent, const char *module, const int mlen, const char *function, const int flen, int prefix, const char *fmt, ...);
void debug_assert_fail(const char *module, const int mlen, const char *function, const int flen, const char *file, int line, const char *expr);
void debug_destroy(void);
void debug_set_logfile(const char *path);
void debug_dump_mallocs(void);
void *debug_malloc(const char *where, int line, const char *func, int size);
void *debug_malloc0(const char *where, int line, const char *func, int size);
char *debug_strdup(const char *where, int line, const char *func, const char *ptr);
char *debug_guard(const char *where, int line, const char *func, char *str);
void debug_free(const char *where, int line, const char *func, void *ptr);
void debug_free_func(void *ptr);
void debug_finished(void);
void *debug_realloc(const char *where, int line, const char *func, void *ptr, int size);
void debug_get_timestamp_millis(long *ts_millis);
/* end of prototypes */









// printf -----------

#if 0
#include <stdarg.h>

#define NEED_ASPRINTF
#define NEED_ASNPRINTF
#define NEED_VASPRINTF
#define NEED_VASNPRINTF

/* #define PREFER_PORTABLE_SNPRINTF */

#include "snprintf.h"


#undef g_strdup_printf
#define g_strdup_printf(format, ...) g_strdup_printf_custom(format, __VA_ARGS__)

#undef g_strdup_vprintf
#define g_strdup_vprintf(format, args) g_strdup_vprintf_custom(format, args)

#undef g_vasprintf
#define g_vasprintf(str, format, args) g_vasprintf_custom(str, format, args)

// ---- BAD !!!! ----
typedef char   gchar;
typedef short  gshort;
typedef long   glong;
typedef int    gint;
typedef gint   gboolean;
// ---- BAD !!!! ----

gchar* g_strdup_printf_custom(const gchar *format, ...);
gchar* g_strdup_vprintf_custom(const gchar *format, va_list args);
gint g_vasprintf_custom(gchar **string, gchar const *format, va_list args);
#endif


#define     	HAVE_STDARG_H 1
//#define     	HAVE_STDDEF_H 1
//#define     	HAVE_STDINT_H 1
//#define			HAVE_STDLIB_H 1
//#define     	HAVE_INTTYPES_H 1
//#define     	HAVE_LOCALE_H 1
//#define     	HAVE_LOCALECONV 1
//#define     	HAVE_LCONV_DECIMAL_POINT
//#define     	HAVE_LCONV_THOUSANDS_SEP
#define     	HAVE_LONG_DOUBLE 1
#define     	HAVE_LONG_LONG_INT 1
#define     	HAVE_UNSIGNED_LONG_LONG_INT 1
//#define     	HAVE_INTMAX_T
//#define     	HAVE_UINTMAX_T
//#define     	HAVE_UINTPTR_T
//#define     	HAVE_PTRDIFF_T
//#define     	HAVE_VA_COPY 1
//#define     	HAVE___VA_COPY 1

#ifndef NO_GTYPES_

#include <stdarg.h>



#undef g_strdup_printf
#define g_strdup_printf(format, ...) g_strdup_printf_custom(format, __VA_ARGS__)

#undef g_strdup_vprintf
#define g_strdup_vprintf(format, args) g_strdup_vprintf_custom(format, args)

#undef g_vasprintf
#define g_vasprintf(str, format, args) g_vasprintf_custom(str, format, args)

// ---- BAD !!!! ----
typedef char   gchar;
typedef short  gshort;
typedef long   glong;
typedef int    gint;
typedef gint   gboolean;
// ---- BAD !!!! ----

gchar* g_strdup_printf_custom(const gchar *format, ...);
gchar* g_strdup_vprintf_custom(const gchar *format, va_list args);
gint g_vasprintf_custom(gchar **string, gchar const *format, va_list args);


//#if !HAVE_VSNPRINTF
int rpl_vsnprintf(char *, size_t, const char *, va_list);
//#endif
//#if !HAVE_SNPRINTF
int rpl_snprintf(char *, size_t, const char *, ...);
//#endif
//#if !HAVE_VASPRINTF
int rpl_vasprintf(char **, const char *, va_list);
//#endif
//#if !HAVE_ASPRINTF
int rpl_asprintf(char **, const char *, ...);

#define vsnprintf rpl_vsnprintf
#define snprintf rpl_snprintf
#define vasprintf rpl_vasprintf
#define asprintf rpl_asprintf


#else

#ifndef XXMAPTOOL
#include <glib.h>
gchar* g_strdup_printf_custom(const gchar *format, ...);
gchar* g_strdup_vprintf_custom(const gchar *format, va_list args);
gint g_vasprintf_custom(gchar **string, gchar const *format, va_list args);
#else
#define g_strdup_printf_custom g_strdup_printf
#define g_strdup_vprintf_custom g_strdup_vprintf
#define g_vasprintf_custom g_vasprintf
#endif


#endif

// printf -----------









#ifdef __cplusplus
}
#endif

#endif

