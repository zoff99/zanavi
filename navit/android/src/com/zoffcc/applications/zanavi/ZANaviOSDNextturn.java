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

public class ZANaviOSDNextturn extends View
{

	private Paint paint = new Paint();
	private RectF f;
	private RectF f2;
	int end_x;
	int end_y;
	int nt_text_start_x = 0;
	int nt_text_start_y = 0;
	int nt_font_size = 10;
	float draw_factor = 1;
	int OSD_element_bg_001 = Color.argb(255, 80, 80, 150); // Color.argb(255, 190, 190, 190); // Color.argb(140, 136, 136, 136);

	public ZANaviOSDNextturn(Context context)
	{
		super(context);
		f = new RectF(0, 0, 1, 1);
		f2 = new RectF(0, 0, 1, 1);
	}

	public ZANaviOSDNextturn(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		f = new RectF(0, 0, 1, 1);
		f2 = new RectF(0, 0, 1, 1);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

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

		f = new RectF(0 + 12 * draw_factor, 0 + 12 * draw_factor, w - 12 * draw_factor, h - 12 * draw_factor);
		f2 = new RectF(0 + 3 * draw_factor, 0 + 3 * draw_factor, w - 3 * draw_factor, h - 3 * draw_factor);

		nt_text_start_x = (int) (20 * real_factor);
		nt_text_start_y = (int) (30 * real_factor);
		nt_font_size = (int) (24 * real_factor);
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		// int dest_valid = NavitGraphics.CallbackDestinationValid2();

		try
		{
			if ((Navit.OSD_nextturn.nextturn_image_valid) && (NavitGraphics.CallbackDestinationValid2() > 0))
			{
				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					paint.setColor(Color.argb(255, 20, 20, 230));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(3 * draw_factor);
					paint.setAntiAlias(true);
					// c.drawRoundRect(f, 10, 10, paint);
					c.drawOval(f2, paint);
					paint.setColor(Color.rgb(0, 0, 0));
					paint.setStyle(Paint.Style.STROKE);
					c.drawOval(f2, paint);
					c.drawBitmap(Navit.OSD_nextturn.nextturn_image, null, f, null);
				}
				else
				{
					paint.setColor(OSD_element_bg_001);
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(3 * draw_factor);
					paint.setAntiAlias(true);
					// c.drawRoundRect(f, 10, 10, paint);
					c.drawOval(f2, paint);
					paint.setColor(Color.rgb(0, 0, 0));
					paint.setStyle(Paint.Style.STROKE);
					c.drawOval(f2, paint);
					c.drawBitmap(Navit.OSD_nextturn.nextturn_image, null, f, null);
				}
			}
			else
			{
				paint.setColor(Color.argb(0, 0, 0, 0));
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(3);
				paint.setAntiAlias(false);
				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					c.drawRect(f, paint);
				}
				else
				{
					c.drawRect(f, paint);
				}
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}

	}
}
