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
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class ZANaviOSDDistToNextturn extends View
{

	private Paint paint = new Paint();
	private Paint paint_p3 = new Paint();
	private int OSD_element_text_shadow_001;
	private int OSD_element_text_001;

	private String my_text = "";

	int nextt_str_start_x = 5;
	int nextt_str_start_y = 5;
	int nextt_str_font_size = 0;
	int nextt_str_w = 0;
	int nextt_str_h = 0;
	int nextt_str_wB = 0;
	int OSD_element_text_shadow_width;

	float textHeight_p2;
	float textOffset_p2;
	float textHeight_p3;
	float textOffset_p3;

	public ZANaviOSDDistToNextturn(Context context)
	{
		super(context);
		paint.setTextAlign(Paint.Align.CENTER);
		paint_p3.setTextAlign(Paint.Align.CENTER);
	}

	public ZANaviOSDDistToNextturn(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		paint.setTextAlign(Paint.Align.CENTER);
		paint_p3.setTextAlign(Paint.Align.CENTER);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		OSD_element_text_shadow_001 = Color.rgb(0, 0, 0); // text shadow
		OSD_element_text_001 = Color.argb(255, 255, 255, 255); // text color

		paint.setTextAlign(Align.CENTER);
		paint_p3.setTextAlign(Paint.Align.CENTER);

		nextt_str_start_x = w / 2;
		nextt_str_start_y = h / 2;

		nextt_str_w = w;
		nextt_str_h = h;

		OSD_element_text_shadow_width = NavitGraphics.dp_to_px(2);

		int nextt_str_font_size_hh = Navit.find_max_font_size_for_height("3.000m", h, 190, 8);
		int nextt_str_font_size_ww = Navit.find_max_font_size_for_width("3.000m", w, 190, 6);
		if (nextt_str_font_size_hh < nextt_str_font_size_ww)
		{
			nextt_str_font_size = nextt_str_font_size_hh;
		}
		else
		{
			nextt_str_font_size = nextt_str_font_size_ww;
		}

		// System.out.println("xsds:" + nextt_str_font_size_ww + " " + nextt_str_font_size_hh + " w=" + w + " h=" + h);

		paint.setTextSize(nextt_str_font_size);
		paint_p3.setTextSize(nextt_str_font_size);

		//		nextt_str_wB = w;
		//
		//		float draw_factor = 1.0f;
		//		if (Navit.my_display_density.compareTo("mdpi") == 0)
		//		{
		//			draw_factor = 1.0f;
		//		}
		//		else if (Navit.my_display_density.compareTo("ldpi") == 0)
		//		{
		//			draw_factor = 0.7f;
		//		}
		//		else if (Navit.my_display_density.compareTo("hdpi") == 0)
		//		{
		//			draw_factor = 1.5f;
		//		}
		//		float draw_factor2 = draw_factor;
		//		// correct for ultra high DPI
		//		if (Navit.metrics.densityDpi >= 320) //&& (Navit.PREF_shrink_on_high_dpi))
		//		{
		//			draw_factor2 = 1.8f * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other;
		//			NavitStreetFontLetterWidth = (int) ((float) NavitStreetFontLetterWidth_base * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other);
		//		}

		paint.setColor(OSD_element_text_shadow_001);
		paint.setStrokeWidth(OSD_element_text_shadow_width);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);

		paint_p3.setColor(OSD_element_text_001);
		paint_p3.setStrokeWidth(3);
		paint_p3.setStyle(Paint.Style.FILL);
		paint_p3.setAntiAlias(true);

		textHeight_p2 = paint.descent() - paint.ascent();
		textOffset_p2 = (textHeight_p2 / 2) - paint.descent();
		textHeight_p3 = paint_p3.descent() - paint_p3.ascent();
		textOffset_p3 = (textHeight_p3 / 2) - paint_p3.descent();
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);
		int dest_valid = NavitGraphics.CallbackDestinationValid2();

		try
		{
			if ((Navit.OSD_nextturn.nextturn_distance_valid) && (dest_valid > 0))
			{
				my_text = Navit.OSD_nextturn.nextturn_distance;
				c.drawText(my_text, nextt_str_start_x, nextt_str_start_y + textOffset_p2, paint);
				c.drawText(my_text, nextt_str_start_x, nextt_str_start_y + textOffset_p3, paint_p3);
			}
			else
			{
				c.drawColor(Color.TRANSPARENT);
			}
		}
		catch (Exception e)
		{
		}

		// System.out.println("onDraw:OSDDist2nextturn");

	}
}
