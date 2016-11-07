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

package com.zoffcc.applications.zanavi;

import android.graphics.Color;

public class ColorUtils
{
	// --------------------
	// Thanks to: http://stackoverflow.com/questions/4414673/android-color-between-two-colors-based-on-percentage
	// --------------------
	public static int getColor(int col_1, int col_2, float p)
	{
		int c0;
		int c1;
		// if (p <= 0.5f)
		{
			// p *= 2;
			c0 = col_1;
			c1 = col_2;
		}
		//		else
		//		{
		//			p = (p - 0.5f) * 2;
		//			c0 = col_right;
		//			c1 = col_left;
		//		}
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);
		return Color.argb(a, r, g, b);
	}

	private static int ave(int src, int dst, float p)
	{
		return src + java.lang.Math.round(p * (dst - src));
	}
}