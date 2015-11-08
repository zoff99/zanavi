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

public class ZANaviOSDStreetname extends View
{
	// private Paint paint_bg = new Paint();
	// private Paint paint_bg2 = new Paint();
	// private Paint paint_p1 = new Paint();
	private Paint paint_p2 = new Paint();
	private Paint paint_p3 = new Paint();
	// private int OSD_element_bg_001;
	private int OSD_element_text_shadow_001;
	private int OSD_element_text_001;

	int font_size_4 = 20;
	int nextt_str_start_x = 5;
	int nextt_str_start_y = 5;
	int nextt_str_font_size = 0;
	int nextt_str_w = 0;
	int nextt_str_h = 0;
	int nextt_str_wB = 0;
	int w = 10;
	int OSD_element_text_shadow_width;
	int NavitStreetFontLetterWidth = 28;
	static final int NavitStreetFontLetterWidth_base = 28;
	int ml4_real = 10;

	float textHeight_p2;
	float textOffset_p2;
	float textHeight_p3;
	float textOffset_p3;

	static int max_letters1 = 10;
	static int max_letters2 = 15;
	static int max_letters3 = 20;
	static int max_letters4 = 30;

	static float letters_f_2 = 0.7f;
	static float letters_f_3 = 0.55f;
	static float letters_f_4 = 0.42f;

	public ZANaviOSDStreetname(Context context)
	{
		super(context);
	}

	public ZANaviOSDStreetname(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		// **OLD** //   Color.argb(255, 80, 80, 150); // bg color
		// OSD_element_bg_001 = getContext().getResources().getColor(R.color.blueish_bg_color);
		OSD_element_text_shadow_001 = Color.rgb(0, 0, 0); // text shadow
		OSD_element_text_001 = Color.argb(255, 255, 255, 255); // text color

		paint_p2.setTextAlign(Align.LEFT);
		paint_p3.setTextAlign(Align.LEFT);

		this.w = w;
		nextt_str_start_y = h / 2;
		nextt_str_font_size = (Navit.find_max_font_size_for_height("jklgqfM", (int) ((float) h * 0.67f), 190, 10)) - 3;

		// System.out.println("fontsize1=" + nextt_str_font_size + " dp2px=" + NavitGraphics.dp_to_px(1));
		// System.out.println("fontsize2=" + nextt_str_font_size + " dp2px=" + NavitGraphics.dp_to_px(2));
		// System.out.println("fontsize3=" + nextt_str_font_size + " dp2px=" + NavitGraphics.dp_to_px(3));

		OSD_element_text_shadow_width = NavitGraphics.dp_to_px(3);

		max_letters1 = Navit.find_max_letters_for_width_and_fontsize(null, w, nextt_str_font_size, 5);
		max_letters2 = Navit.find_max_letters_for_width_and_fontsize(null, w, (int) ((float) nextt_str_font_size * letters_f_2), 4) - 1;
		max_letters3 = Navit.find_max_letters_for_width_and_fontsize(null, w, (int) ((float) nextt_str_font_size * letters_f_3), 4);
		max_letters4 = Navit.find_max_letters_for_width_and_fontsize(null, w, (int) ((float) nextt_str_font_size * letters_f_4), 2);

		// System.out.println("aaa2:1:" + max_letters1 + " " + h + " " + w + " " + oldh + " " + oldw);
		// System.out.println("aaa2:2:" + max_letters2 + " " + h + " " + w + " " + oldh + " " + oldw);
		// System.out.println("aaa2:3:" + max_letters3 + " " + h + " " + w + " " + oldh + " " + oldw);
		// System.out.println("aaa2:4:" + max_letters4 + " " + h + " " + w + " " + oldh + " " + oldw);
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);

		try
		{
			if (NavitAndroidOverlay.measure_mode)
			{
				String my_text = "";

				if (NavitAndroidOverlay.measure_result_meters < 1800)
				{
					my_text = "Distanz: " + NavitAndroidOverlay.measure_result_meters + "m";
				}
				else if (NavitAndroidOverlay.measure_result_meters < 100000)
				{
					my_text = "Distanz: " + String.format("%.2f", ((double) NavitAndroidOverlay.measure_result_meters / (double) 1000.0)) + "km";
				}
				else
				{
					my_text = "Distanz: " + (NavitAndroidOverlay.measure_result_meters / 1000) + "km";
				}

				paint_p2.setTextSize((int) ((float) nextt_str_font_size * letters_f_3));
				paint_p3.setTextSize((int) ((float) nextt_str_font_size * letters_f_3));

				paint_p2.setColor(OSD_element_text_shadow_001);
				paint_p2.setStrokeWidth(OSD_element_text_shadow_width);
				paint_p2.setStyle(Paint.Style.STROKE);
				paint_p2.setAntiAlias(true);

				paint_p3.setColor(OSD_element_text_001);
				paint_p3.setStrokeWidth(3);
				paint_p3.setStyle(Paint.Style.FILL);
				paint_p3.setAntiAlias(true);

				textHeight_p2 = paint_p2.descent() - paint_p2.ascent();
				textOffset_p2 = (textHeight_p2 / 2) - paint_p2.descent();
				textHeight_p3 = paint_p3.descent() - paint_p3.ascent();
				textOffset_p3 = (textHeight_p3 / 2) - paint_p3.descent();

				c.drawText(my_text, 5, nextt_str_start_y + textOffset_p2, paint_p2);
				c.drawText(my_text, 5, nextt_str_start_y + textOffset_p3, paint_p3);

			}
			else
			{

				if ((Navit.OSD_nextturn.nextturn_streetname != null) || (Navit.p.PREF_follow_gps))
				{
					String my_text = Navit.OSD_nextturn.nextturn_streetname;
					String my_text_systematic = Navit.OSD_nextturn.nextturn_streetname_systematic;

					if ((my_text.length() + 1 + my_text_systematic.length()) > (max_letters1))
					{
						if ((my_text.length() + 1 + my_text_systematic.length()) > (max_letters2))
						{
							ml4_real = Navit.find_max_letters_for_width_and_fontsize(my_text_systematic + " " + my_text, w, (int) ((float) nextt_str_font_size * letters_f_3), 4);

							if ((my_text.length() + 1 + my_text_systematic.length()) > (ml4_real))
							{

								try
								{
									// ok now really calculate it here
									font_size_4 = Navit.find_max_font_size_for_width("  " + my_text_systematic + " " + my_text, w, 90, 20);
									paint_p2.setTextSize(font_size_4);
									paint_p3.setTextSize(font_size_4);
									// ok now really calculate it here

									//									
									//									paint_p2.setTextSize((int) ((float) nextt_str_font_size * letters_f_4));
									//									paint_p3.setTextSize((int) ((float) nextt_str_font_size * letters_f_4));
									//
									// ml4_real = Navit.find_max_letters_for_width_and_fontsize(my_text_systematic + " " + my_text, w, (int) ((float) nextt_str_font_size * letters_f_4), 4);
									ml4_real = Navit.find_max_letters_for_width_and_fontsize(my_text_systematic + " " + my_text, w, font_size_4, 6);

									// System.out.println("ml4_real=" + ml4_real);
									if ((my_text.length() + 1 + my_text_systematic.length()) > (ml4_real))
									{
										my_text = my_text.substring(0, (ml4_real - 4)) + "..";
										my_text_systematic = "";
									}
								}
								catch (Exception ee44)
								{
								}
							}
							else
							{
								paint_p2.setTextSize((int) ((float) nextt_str_font_size * letters_f_3));
								paint_p3.setTextSize((int) ((float) nextt_str_font_size * letters_f_3));
							}
						}
						else
						{
							paint_p2.setTextSize((int) ((float) nextt_str_font_size * letters_f_2));
							paint_p3.setTextSize((int) ((float) nextt_str_font_size * letters_f_2));
						}
					}
					else
					{
						paint_p2.setTextSize(nextt_str_font_size);
						paint_p3.setTextSize(nextt_str_font_size);
					}

					paint_p2.setColor(OSD_element_text_shadow_001);
					paint_p2.setStrokeWidth(OSD_element_text_shadow_width);
					paint_p2.setStyle(Paint.Style.STROKE);
					paint_p2.setAntiAlias(true);

					paint_p3.setColor(OSD_element_text_001);
					paint_p3.setStrokeWidth(3);
					paint_p3.setStyle(Paint.Style.FILL);
					paint_p3.setAntiAlias(true);

					textHeight_p2 = paint_p2.descent() - paint_p2.ascent();
					textOffset_p2 = (textHeight_p2 / 2) - paint_p2.descent();
					textHeight_p3 = paint_p3.descent() - paint_p3.ascent();
					textOffset_p3 = (textHeight_p3 / 2) - paint_p3.descent();

					c.drawText(my_text_systematic + " " + my_text, 5, nextt_str_start_y + textOffset_p2, paint_p2);
					c.drawText(my_text_systematic + " " + my_text, 5, nextt_str_start_y + textOffset_p3, paint_p3);
				}
				else
				{
					c.drawColor(Color.TRANSPARENT);
				}
			}
		}
		catch (Exception e)
		{
		}

		// System.out.println("onDraw:OSDStreetname");
	}

}
