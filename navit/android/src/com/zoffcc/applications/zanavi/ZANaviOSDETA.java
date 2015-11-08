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

public class ZANaviOSDETA extends View
{

	private Paint paint = new Paint();
	private RectF f;
	int end_x;
	int end_y;
	int nt_text_start_x = 0;
	int nt_text_start_y = 0;
	int nt_font_size = 10;
	float draw_factor = 1;
	int OSD_element_bg_001 = Navit.OSD_blueish_bg_color; // Color.argb(255, 190, 190, 190); // Color.argb(140, 136, 136, 136);
	int OSD_element_text_shadow_001 = Color.rgb(255, 255, 255); // text shadow
	int OSD_element_text_001 = Color.rgb(117, 117, 117);
	int OSD_element_text_shadow_width = 1;
	String my_text = "";
	int w2;
	int h2;
	int h3;
	int w;
	int h;
	int font_size = 10;
	float textHeight = 0;
	float textOffset = 0;
	static long last_ondraw = -1L;

	public ZANaviOSDETA(Context context)
	{
		super(context);
		paint.setTextAlign(Paint.Align.CENTER);
	}

	public ZANaviOSDETA(Context context, AttributeSet attrs)
	{
		super(context, attrs);
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
		this.h3 = (int) (h + (paint.ascent() / 2));

		OSD_element_text_shadow_width = NavitGraphics.dp_to_px(2);

		font_size = Navit.find_max_font_size_for_height("7,544 km", h, 93, 9);

		// System.out.println("aAA:2:" + font_size + " " + w + " " + h);

		paint.setTextSize(font_size);
		paint.setAntiAlias(true);

		textHeight = paint.descent() - paint.ascent();
		textOffset = (textHeight / 2) - paint.descent();
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		int dest_valid = NavitGraphics.CallbackDestinationValid2();

		last_ondraw = System.currentTimeMillis();

		try
		{
			if ((Navit.OSD_route_001.arriving_time_valid) && (dest_valid > 0))
			{
				my_text = Navit.OSD_route_001.arriving_time;

				paint.setColor(OSD_element_text_shadow_001);
				paint.setStrokeWidth(OSD_element_text_shadow_width);
				paint.setStyle(Paint.Style.STROKE);
				c.drawText(my_text, w2, h2 + textOffset, paint);

				paint.setColor(OSD_element_text_001);
				paint.setStrokeWidth(3);
				paint.setStyle(Paint.Style.FILL);
				c.drawText(my_text, w2, h2 + textOffset, paint);
			}
			else
			{
				c.drawColor(Color.TRANSPARENT);
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}

		// System.out.println("onDraw:OSDETA");
	}
}
