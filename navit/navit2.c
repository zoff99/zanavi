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

 
int main(int argc, char **argv)
{
	struct coord_geo g;
	struct coord c;
	transform_from_geo(projection_mg, &g, &c);

	struct pcoord pc;
	pc.x = c.x;
	pc.y = c.y;
	pc.pro = projection_mg;

	navit_set_position(global_navit, &pc);

	return 0;
}
 

