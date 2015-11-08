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

package com.zoffcc.applications.zanavi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ZANaviOSDSpeeding extends View
{
	int w = 10;
	int h = 10;
	RectF bounds_speedwarning = new RectF(120, 800, 120 + 200, 800 + 200);
	Paint paint_speedwarning = new Paint(0);
	float textHeight = 10;
	float textOffset = 10;

	public ZANaviOSDSpeeding(Context context)
	{
		super(context);
	}

	public ZANaviOSDSpeeding(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public void onSizeChanged(int w1, int h1, int oldw, int oldh)
	{
		super.onSizeChanged(w1, h1, oldw, oldh);
		this.w = w1;
		this.h = h1;
		bounds_speedwarning = new RectF(NavitGraphics.dp_to_px(5), NavitGraphics.dp_to_px(5), w1 - NavitGraphics.dp_to_px(5), h1 - NavitGraphics.dp_to_px(5));
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);

		if (Navit.p.PREF_roadspeed_warning)
		// DEBUG // if (1 == 2 - 1)
		{
			// DEBUG // Navit.cur_max_speed = 50;
			if ((Navit.cur_max_speed != -1) && (Navit.your_are_speeding))
			{
				paint_speedwarning.setAntiAlias(true);
				paint_speedwarning.setColor(Color.WHITE);
				paint_speedwarning.setStyle(Style.FILL);
				c.drawOval(bounds_speedwarning, paint_speedwarning);

				paint_speedwarning.setColor(Color.RED);
				paint_speedwarning.setStyle(Style.STROKE);
				paint_speedwarning.setStrokeWidth(NavitGraphics.dp_to_px(8));
				c.drawOval(bounds_speedwarning, paint_speedwarning);

				paint_speedwarning.setColor(Color.BLACK);
				paint_speedwarning.setStyle(Style.FILL_AND_STROKE);
				paint_speedwarning.setStrokeWidth(2);
				if (Navit.p.PREF_use_imperial)
				{
					Navit.cur_max_speed_corr = (int) ((((float) Navit.cur_max_speed) / 1.6f) + 0.5f);
				}
				else
				{
					Navit.cur_max_speed_corr = Navit.cur_max_speed;
				}

				if (Navit.cur_max_speed_corr > 99)
				{
					paint_speedwarning.setTextSize(NavitGraphics.dp_to_px(20));
				}
				else
				{
					paint_speedwarning.setTextSize(NavitGraphics.dp_to_px(23));
				}
				paint_speedwarning.setTextAlign(Align.CENTER);
				textHeight = paint_speedwarning.descent() - paint_speedwarning.ascent();
				textOffset = (textHeight / 2) - paint_speedwarning.descent();
				c.drawText("" + Navit.cur_max_speed_corr, bounds_speedwarning.centerX(), bounds_speedwarning.centerY() + textOffset, paint_speedwarning);
			}
		}
		else
		{
			c.drawColor(Color.TRANSPARENT);
		}
		
		// System.out.println("onDraw:OSDSpeeding");
	}
}
