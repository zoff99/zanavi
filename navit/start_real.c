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

#include <stdio.h>
#include <stdlib.h>
#include <glib.h>
#include "config.h"
#ifdef HAVE_GETOPT_H
#include <getopt.h>
#else
#include <XGetopt.h>
#endif
#include "config_.h"
#include "version.h"
#include "navit.h"
#include "item.h"
#include "coord.h"
#include "main.h"
#include "route.h"
#include "navigation.h"
#include "track.h"
#include "debug.h"
#include "event.h"
#include "event_glib.h"
#include "xmlconfig.h"
#include "file.h"
#include "search.h"
#include "start_real.h"
#include "linguistics.h"
#include "navit_nls.h"
#include "atom.h"
#ifdef HAVE_API_WIN32_CE
#include <windows.h>
#include <winbase.h>
#endif

char *version=PACKAGE_VERSION" "SVN_VERSION""NAVIT_VARIANT;
int main_argc;
char **main_argv;

static void print_usage(void)
{
	printf("ZANavi usage:\nZANavi [options] [configfile]\n\t-c <file>: use <file> as config file\n\t-d <n>: set the debug output level to <n>. (TODO)\n\t-h: print this usage info and exit.\n\t-v: Print the version and exit.\n");
}

#ifndef USE_PLUGINS
extern void builtin_init(void);
#endif /* USE_PLUGINS*/

int main_real(int argc, char **argv)
{
	dbg(0, "in main loop 001 ##########################\n");

	xmlerror *error = NULL;
	char *config_file = NULL;
	int opt;
	char *cp;
	struct attr navit;

	GList *list = NULL, *li;
	main_argc = argc;
	main_argv = argv;

	//dbg(0, "in main loop 002 ##########################\n");

#ifdef HAVE_GLIB
	event_glib_init();
	//dbg(0,"in main loop 003 ##########################\n");
#else
	_g_slice_thread_init_nomessage();
	//dbg(0, "in main loop 004 ##########################\n");
#endif
	atom_init();
	main_init(argv[0]);
	main_init_nls();
	debug_init(argv[0]);

	cp = getenv("NAVIT_LOGFILE");
	if (cp)
	{
		debug_set_logfile(cp);
	}
#ifdef HAVE_API_WIN32_CE
	else
	{
		debug_set_logfile("/Storage Card/navit.log");
	}
#endif

	//dbg(0, "in main loop 005 ##########################\n");
	file_init();
	//dbg(0, "in main loop 006 ##########################\n");

#ifndef USE_PLUGINS
	//dbg(0, "in main loop 007 ##########################\n");
	builtin_init();
#endif

	//dbg(0, "in main loop 008 ##########################\n");
	route_init();
	//dbg(0, "in main loop 008.1 ##########################\n");
	navigation_init();
	//dbg(0, "in main loop 008.2 ##########################\n");
	tracking_init();
	//dbg(0, "in main loop 008.3 ##########################\n");
	search_init();
	//dbg(0, "in main loop 008.4 ##########################\n");
	linguistics_init();
	//dbg(0, "in main loop 0014 ##########################\n");

	config_file = NULL;
#ifdef HAVE_GETOPT_H
	opterr=0; //don't bomb out on errors.
#endif /* _MSC_VER */
	if (argc > 1)
	{
		/* DEVELOPPERS : don't forget to update the manpage if you modify theses options */
		while ((opt = getopt(argc, argv, ":hvc:d:")) != -1)
		{
			switch (opt)
			{
				case 'h':
					print_usage();
					exit(0);
					break;
				case 'v':
					printf("%s %s\n", "navit", version);
					exit(0);
					break;
				case 'c':
					printf("config file n is set to `%s'\n", optarg);
					config_file = optarg;
					break;
				case 'd':
					printf("TODO Verbose option is set to `%s'\n", optarg);
					break;
#ifdef HAVE_GETOPT_H
					case ':':
					fprintf(stderr, "navit: Error - Option `%c' needs a value\n", optopt);
					print_usage();
					exit(1);
					break;
					case '?':
					fprintf(stderr, "navit: Error - No such option: `%c'\n", optopt);
					print_usage();
					exit(1);
#endif
			}
		}
		// use 1st cmd line option that is left for the config file
		if (optind < argc)
			config_file = argv[optind];
	}

	// if config file is explicitely given only look for it, otherwise try std paths
	//if (config_file)
	//{
	//	list = g_list_append(list, g_strdup(config_file));
	//}
	//else
	//{

		dbg(0, "navit_share_dir=%s\n", navit_share_dir);
		list = g_list_append(list, g_strjoin(NULL, navit_share_dir, "/navit.xml", NULL));
		//list = g_list_append(list, g_strdup("navit.xml"));

#ifdef HAVE_API_ANDROID
		// **disabled** // new preferred location (the new one should have priority over the legacy!)
		// **disabled** // list = g_list_append(list,g_strdup("/sdcard/zanavi/navit.xml"));
#endif

		//list = g_list_append(list, g_strjoin(NULL, getenv("NAVIT_SHAREDIR"), "/navit.xml", NULL));

#ifndef _WIN32
		//list = g_list_append(list, g_strdup("/etc/navit/navit.xml"));
#endif

	//}
	li = list;
	for (;;)
	{
		if (li == NULL)
		{
			// We have not found an existing config file from all possibilities
			dbg(0, "No config file navit.xml, navit.xml.local found\n");
			return 1;
		}
		// Try the next config file possibility from the list
		config_file = li->data;
		if (file_exists(config_file))
		{
			break;
		}
		else
		{
			g_free(config_file);
		}
		li = g_list_next(li);
	}

	// ############### load XML config file, and call all the init/new functions ################
	// ############### load XML config file, and call all the init/new functions ################
	// ############### load XML config file, and call all the init/new functions ################
	clock_t s_ = debug_measure_start();
	if (!config_load(config_file, &error))
	{
	}
	debug_mrp("load and init xmlconfig:", debug_measure_end(s_));
	// ############### load XML config file, and call all the init/new functions ################
	// ############### load XML config file, and call all the init/new functions ################
	// ############### load XML config file, and call all the init/new functions ################


	while (li)
	{
		g_free(li->data);
		li = g_list_next(li);
	}

	g_list_free(list);

	if (!config_get_attr(config, attr_navit, &navit, NULL) && !config_empty_ok)
	{
		dbg(0, "No instance has been created, exiting\n");
		exit(1);
	}

	dbg(0, "in main loop 026 ##########################\n");
	event_main_loop_run();
	dbg(0, "after main loop ##########################");

#ifndef HAVE_API_ANDROID
	debug_finished();
#endif
	return 0;
}
