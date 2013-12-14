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

public class ZANaviOSDCompass extends View
{

	private Paint paint = new Paint();
	private RectF f;
	int end_x;
	int end_y;
	float draw_factor = 1;
	int radius = 0;
	int compass_center_x = 0;
	int compass_center_y = 0;

	public ZANaviOSDCompass(Context context)
	{
		super(context);
		f = new RectF(0 + 3, 0 + 3, NavitOSDJava.compass_w - 3, NavitOSDJava.compass_h - 3);
	}

	public ZANaviOSDCompass(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		f = new RectF(0 + 3, 0 + 3, NavitOSDJava.compass_w - 3, NavitOSDJava.compass_h - 3);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		f = new RectF(0 + 12 * draw_factor, 0 + 12 * draw_factor, w - 12 * draw_factor, h - 12 * draw_factor);

		radius = (int) ((w / 2) - (13 * draw_factor));
		compass_center_x = w / 2;
		compass_center_y = h / 2;

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
	}

	//	private void resizeView(View view, int newWidth, int newHeight)
	//	{
	//		try
	//		{
	//			Constructor<? extends LayoutParams> ctor = view.getLayoutParams().getClass().getDeclaredConstructor(int.class, int.class);
	//			view.setLayoutParams(ctor.newInstance(newWidth, newHeight));
	//		}
	//		catch (Exception e)
	//		{
	//			e.printStackTrace();
	//		}
	//	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		int dest_valid = NavitGraphics.CallbackDestinationValid2();

		boolean did_draw_circle = false;
		if (!NavitGraphics.MAP_DISPLAY_OFF)
		{
			if ((Navit.OSD_compass.angle_north_valid) || ((Navit.OSD_compass.angle_target_valid) && (dest_valid > 0)))
			{
				// compass_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(2);
				paint.setAntiAlias(true);
				paint.setColor(NavitOSDJava.OSD_element_bg_001_compass);
				// c.drawRoundRect(f, 10, 10, paint);
				// c.drawCircle(compass_center_x, compass_center_y, radius, paint);
				c.drawOval(f, paint);
			}

			if (Navit.OSD_compass.angle_north_valid)
			{
				paint.setColor(Color.BLACK);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(3 * draw_factor);
				paint.setAntiAlias(true);
				//c.drawCircle(compass_center_x, compass_center_y, radius, paint);
				c.drawOval(f, paint);
				did_draw_circle = true;
				end_x = (int) ((float) Math.sin((float) Math.toRadians(Navit.OSD_compass.angle_north)) * radius);
				end_y = (int) ((float) Math.cos((float) Math.toRadians(Navit.OSD_compass.angle_north)) * radius);
				// System.out.println("x " + end_x + " y " + end_y);
				paint.setStrokeWidth(3 * draw_factor);
				if (Navit.metrics.densityDpi >= 320)
				{
					paint.setStrokeWidth(3 * draw_factor);
				}
				c.drawLine(compass_center_x - end_x, compass_center_y + end_y, compass_center_x, compass_center_y, paint);
				paint.setColor(Color.RED);
				paint.setStrokeWidth(3 * draw_factor);
				if (Navit.metrics.densityDpi >= 320)
				{
					paint.setStrokeWidth(3 * draw_factor);
				}
				c.drawLine(compass_center_x + end_x, compass_center_y - end_y, compass_center_x, compass_center_y, paint);
			}

			if ((Navit.OSD_compass.angle_target_valid) && (dest_valid > 0))
			{
				paint.setColor(Color.BLACK);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(2 * draw_factor);
				paint.setAntiAlias(true);
				if (!did_draw_circle)
				{
					// c.drawCircle(compass_center_x, compass_center_y, radius, paint);
					c.drawOval(f, paint);
					did_draw_circle = true;
				}
				end_x = (int) ((float) Math.sin((float) Math.toRadians(Navit.OSD_compass.angle_target)) * radius);
				end_y = (int) ((float) Math.cos((float) Math.toRadians(Navit.OSD_compass.angle_target)) * radius);
				// System.out.println("x " + end_x + " y " + end_y);
				paint.setStrokeWidth(3 * draw_factor);
				if (Navit.metrics.densityDpi >= 320)
				{
					paint.setStrokeWidth(3 * draw_factor);
				}
				paint.setColor(Color.GREEN);
				c.drawLine(compass_center_x, compass_center_y, compass_center_x + end_x, compass_center_y - end_y, paint);
			}
		}

	}
}
