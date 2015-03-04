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
	private Paint paint_bg = new Paint();
	private Paint paint_bg2 = new Paint();
	private Paint paint_p1 = new Paint();
	private Paint paint_p2 = new Paint();
	private Paint paint_p3 = new Paint();
	private RectF f;

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
				String my_text = Navit.OSD_nextturn.nextturn_streetname;
				String my_text_systematic = Navit.OSD_nextturn.nextturn_streetname_systematic;

				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					paint_bg2.setColor(Color.argb(255, 20, 20, 230));
					paint_bg2.setStyle(Paint.Style.FILL_AND_STROKE);
					paint_bg2.setStrokeWidth(2);
					paint_bg2.setAntiAlias(true);
					c.drawRoundRect(f, 10, 10, paint_bg2);
					paint_p1.setColor(Color.WHITE);
					paint_p1.setStrokeWidth(3);
					paint_p1.setStyle(Paint.Style.FILL);
					if ((my_text.length() + 1 + my_text_systematic.length()) > (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth))
					{
						if ((my_text.length() + 1 + my_text_systematic.length()) > (2 * (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth)))
						{
							paint_p1.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.40));
						}
						else
						{
							paint_p1.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.70));
						}
					}
					else
					{
						paint_p1.setTextSize(NavitOSDJava.nextt_str_font_size);
					}
					paint_p1.setAntiAlias(true);
					c.drawText(my_text_systematic + " " + my_text, NavitOSDJava.nextt_str_ltxB + NavitOSDJava.nextt_str_start_x, NavitOSDJava.nextt_str_ltyB + NavitOSDJava.nextt_str_start_y, paint_p1);
				}
				else
				{
					paint_bg2.setColor(NavitOSDJava.OSD_element_bg_001);
					paint_bg2.setStyle(Paint.Style.FILL_AND_STROKE);
					paint_bg2.setStrokeWidth(2);
					paint_bg2.setAntiAlias(true);
					c.drawRoundRect(f, 10, 10, paint_bg2);
					//paint_p1.setColor(Color.BLACK);
					//paint_p1.setStrokeWidth(3);
					//paint_p1.setStyle(Paint.Style.FILL);
					if ((my_text.length() + 1 + my_text_systematic.length()) > (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth))
					{
						if ((my_text.length() + 1 + my_text_systematic.length()) > (2 * (NavitOSDJava.nextt_str_wB / NavitOSDJava.NavitStreetFontLetterWidth)))
						{
							//paint_p1.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.40));
							paint_p2.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.40));
							paint_p3.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.40));
						}
						else
						{
							//paint_p1.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.70));
							paint_p2.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.70));
							paint_p3.setTextSize((int) (NavitOSDJava.nextt_str_font_size * 0.70));
						}
					}
					else
					{
						//paint_p1.setTextSize(NavitOSDJava.nextt_str_font_size);
						paint_p2.setTextSize(NavitOSDJava.nextt_str_font_size);
						paint_p3.setTextSize(NavitOSDJava.nextt_str_font_size);
					}

					//System.out.println("xx=" + my_text + " " + NavitOSDJava.nextt_str_start_x + " " + NavitOSDJava.nextt_str_start_y + " " + NavitOSDJava.nextt_str_font_size);

					paint_p2.setColor(NavitOSDJava.OSD_element_text_shadow_001);
					paint_p2.setStrokeWidth(NavitOSDJava.OSD_element_text_shadow_width);
					paint_p2.setStyle(Paint.Style.STROKE);
					paint_p2.setAntiAlias(true);
					c.drawText(my_text_systematic + " " + my_text, NavitOSDJava.nextt_str_start_x, NavitOSDJava.nextt_str_start_y, paint_p2);

					paint_p3.setColor(NavitOSDJava.OSD_element_text_001);
					paint_p3.setStrokeWidth(3);
					paint_p3.setStyle(Paint.Style.FILL);
					paint_p3.setAntiAlias(true);
					c.drawText(my_text_systematic + " " + my_text, NavitOSDJava.nextt_str_start_x, NavitOSDJava.nextt_str_start_y, paint_p3);
				}
			}
			else
			{
				paint_bg.setColor(Color.argb(0, 0, 0, 0));
				paint_bg.setStyle(Paint.Style.FILL);
				paint_bg.setStrokeWidth(3);
				paint_bg.setAntiAlias(false);
				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					c.drawRect(f, paint_bg);
				}
				else
				{
					c.drawRect(f, paint_bg);
				}
			}
		}
		catch (Exception e)
		{

		}
	}

}
