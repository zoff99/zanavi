/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2013 Zoff <zoff@zoff.cc>
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
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ZANaviOSDTimeToDest extends View
{

	private Paint paint = new Paint();
	private RectF f;
	int end_x;
	int end_y;
	int nt_text_start_x = 0;
	int nt_text_start_y = 0;
	int nt_font_size = 10;
	float draw_factor = 1;
	int OSD_element_bg_001 = Color.argb(255, 80, 80, 150); // Color.argb(255, 190, 190, 190); // Color.argb(140, 136, 136, 136);
	String my_text = "";
	int w2;
	int h2;
	int w;
	int h;

	public ZANaviOSDTimeToDest(Context context)
	{
		super(context);
		f = new RectF(0, 0, 1, 1);
		paint.setTextAlign(Paint.Align.CENTER);
	}

	public ZANaviOSDTimeToDest(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		f = new RectF(0, 0, 1, 1);
		paint.setTextAlign(Paint.Align.CENTER);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		this.w2 = w / 2;
		this.h2 = h / 2;
		this.w = w;
		this.h = h;

		draw_factor = 1.0f;
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

		// correct for ultra high DPI
		if (Navit.metrics.densityDpi >= 320) //&& (Navit.PREF_shrink_on_high_dpi))
		{
			draw_factor = 1.8f * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other;
		}

		float real_factor = draw_factor / 1.5f;

		f = new RectF(0 + 6, 0 + 6, w - 6, h - 6);

		nt_text_start_x = (int) (20 * real_factor);
		nt_text_start_y = (int) (30 * real_factor);
		nt_font_size = (int) (24 * real_factor);
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		int dest_valid = NavitGraphics.CallbackDestinationValid2();

		try
		{
			if ((Navit.OSD_route_001.arriving_time_valid) && (dest_valid > 0))
			{
				my_text = Navit.OSD_route_001.arriving_time;

				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					paint.setColor(Color.argb(255, 20, 20, 230));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					c.drawRoundRect(f, 10, 10, paint);

					paint.setColor(Color.WHITE);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(nt_font_size);
					paint.setAntiAlias(true);
					c.drawText(my_text, nt_text_start_x, nt_text_start_y, paint);
				}
				else
				{
					paint.setColor(OSD_element_bg_001);
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					c.drawRoundRect(f, 10, 10, paint);

					paint.setColor(NavitOSDJava.OSD_element_text_shadow_001);
					paint.setStrokeWidth(NavitOSDJava.OSD_element_text_shadow_width);
					paint.setStyle(Paint.Style.STROKE);
					paint.setTextSize(nt_font_size);
					paint.setAntiAlias(true);
					c.drawText(my_text, w2, h + (paint.ascent() / 2), paint);

					paint.setColor(NavitOSDJava.OSD_element_text_001);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(nt_font_size);
					paint.setAntiAlias(true);
					c.drawText(my_text, w2, h + (paint.ascent() / 2), paint);
				}
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}

	}
}
