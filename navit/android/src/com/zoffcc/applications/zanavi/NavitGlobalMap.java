/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 Zoff <zoff@zoff.cc>
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

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.ImageView;

public class NavitGlobalMap extends ImageView
{
	public int mCanvasHeight = 1;
	public int mCanvasWidth = 1;

	public NavitGlobalMap(Context context)
	{
		super(context);
		// this.setImageResource(R.drawable.bigmap_colors_zanavi2);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);

		// int action = event.getAction();
		// int x = (int) event.getX();
		// int y = (int) event.getY();

		return false;
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		// super.onSizeChanged(w, h, oldw, oldh);
		this.mCanvasWidth = w;
		this.mCanvasHeight = h;
	}

	public void onDraw(Canvas c)
	{
		//Log.e("Navit", "NavitAndroidOverlay -> onDraw");
		float draw_factor = 1.0f;
		if (Navit.my_display_density.compareTo("mdpi") == 0)
		{
			draw_factor = 1.0f;
		}
		else if (Navit.my_display_density.compareTo("ldpi") == 0)
		{
			draw_factor = 0.7f;
		}
		else if (Navit.my_display_density.compareTo("hdpi") == 0)
		{
			draw_factor = 1.5f;
		}

		if (NavitGraphics.in_map)
		{
			c.scale(5f, 5f, this.mCanvasWidth / 2, this.mCanvasHeight / 2);
			c.rotate(13, this.mCanvasWidth / 2, this.mCanvasHeight / 2);
			//super.onDraw(c);
		}
	}
}
