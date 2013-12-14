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

public class ZANaviOSDStreetname extends View
{
	private Paint paint = new Paint();
	private RectF f;
	String my_text = "";
	String my_text_systematic = "";

	public ZANaviOSDStreetname(Context context)
	{
		super(context);
		this.f = new RectF(0, 0, this.getWidth(), this.getHeight());
	}

	public ZANaviOSDStreetname(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.f = new RectF(0, 0, this.getWidth(), this.getHeight());
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		this.f = new RectF(0, 0, w, h);
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);

		try
		{
			if ((Navit.OSD_nextturn.nextturn_streetname != null) || (Navit.PREF_follow_gps))
			{
				my_text = Navit.OSD_nextturn.nextturn_streetname;
				my_text_systematic = Navit.OSD_nextturn.nextturn_streetname_systematic;

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
					if ((my_text.length() + 1 + my_text_systematic.length()) > (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth))
					{
						if ((my_text.length() + 1 + my_text_systematic.length()) > (2 * (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth)))
						{
							paint.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.40));
						}
						else
						{
							paint.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.70));
						}
					}
					else
					{
						paint.setTextSize(NavitOSDJava.nextt_str_font_size);
					}
					paint.setAntiAlias(true);
					c.drawText(my_text_systematic + " " + my_text, NavitOSDJava.nextt_str_ltxB + NavitOSDJava.nextt_str_start_x, NavitOSDJava.nextt_str_ltyB + NavitOSDJava.nextt_str_start_y, paint);
				}
				else
				{
					paint.setColor(NavitOSDJava.OSD_element_bg_001);
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					c.drawRoundRect(f, 10, 10, paint);
					paint.setColor(Color.BLACK);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					if ((my_text.length() + 1 + my_text_systematic.length()) > (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth))
					{
						if ((my_text.length() + 1 + my_text_systematic.length()) > (2 * (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth)))
						{
							paint.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.40));
						}
						else
						{
							paint.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.70));
						}
					}
					else
					{
						paint.setTextSize(NavitOSDJava.nextt_str_font_size);
					}

					//System.out.println("xx=" + my_text + " " + NavitOSDJava.nextt_str_start_x + " " + NavitOSDJava.nextt_str_start_y + " " + NavitOSDJava.nextt_str_font_size);

					paint.setColor(NavitOSDJava.OSD_element_text_shadow_001);
					paint.setStrokeWidth(NavitOSDJava.OSD_element_text_shadow_width);
					paint.setStyle(Paint.Style.STROKE);
					paint.setAntiAlias(true);
					c.drawText(my_text_systematic + " " + my_text, NavitOSDJava.nextt_str_start_x, NavitOSDJava.nextt_str_start_y, paint);

					paint.setColor(NavitOSDJava.OSD_element_text_001);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					paint.setAntiAlias(true);
					c.drawText(my_text_systematic + " " + my_text, NavitOSDJava.nextt_str_start_x, NavitOSDJava.nextt_str_start_y, paint);
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

		}
	}

}
