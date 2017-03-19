/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2016 Zoff <zoff@zoff.cc>
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


void main()
{
	int res = 0;
	char* r;
	r = malloc(10000);
	struct navigation *nav;
	struct navigation_itm *old;
	struct navigation_itm *new;
	int delta;
	int delta_real;

	tests_dbg(0, "-- START UNIT TEST 001 --\n");

	res = maneuver_required2(nav, old, new, &delta, &delta_real, &r);
	tests_dbg(0, "res=%d\n", res);

	tests_dbg(0, "--   END UNIT TEST 001 --\n");

	free(r);
}

#ifdef __cplusplus
}
#endif


