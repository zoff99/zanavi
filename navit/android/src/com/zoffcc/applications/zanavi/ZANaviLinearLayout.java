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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class ZANaviLinearLayout extends LinearLayout
{
	private boolean need_size_change = false;
	private Paint paint_crosshair = new Paint();
	private Paint paint_sat_status = new Paint();
	int delta_1 = 8;
	int delta_2 = 35;
	int wm;
	int hm;
	int sat_status_max_sats = 13;
	int sat_status_lt_x = 0;
	int sat_status_lt_y = 0;
	int sat_status_lt_w = 1;
	int sat_status_lt_h = 1;
	Rect r1 = new Rect(0, 0, 1, 1);
	Rect r2 = new Rect(0, 0, 1, 1);
	Rect r3 = new Rect(0, 0, 1, 1);
	Rect r4 = new Rect(0, 0, 1, 1);
	View child1;
	View child2;
	View child3;
	View child4;
	View child5;
	View child6;
	View child7;
	View child8;
	View child9;

	public ZANaviLinearLayout(Context context)
	{
		super(context);
		paint_crosshair.setColor(Color.DKGRAY);
		paint_crosshair.setStyle(Paint.Style.STROKE);
	}

	public ZANaviLinearLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		paint_crosshair.setColor(Color.DKGRAY);
		paint_crosshair.setStyle(Paint.Style.STROKE);
	}

	@Override
	public void onDraw(Canvas c)
	{

		// System.out.println("XYZ:ZANaviLinearLayout -> onDraw");

		//		if (2 == 1 + 1)
		//		{
		//			return;
		//		}

		if (this.need_size_change)
		{
			// System.out.println("XYZ:ZANaviLinearLayout -> onDraw [need_size_change]");

			int w = this.getWidth();
			int h = this.getHeight();

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

			float draw_factor2 = draw_factor;
			// correct for ultra high DPI
			if (Navit.metrics.densityDpi >= 320) //&& (Navit.PREF_shrink_on_high_dpi))
			{
				draw_factor2 = 1.8f * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other;
				NavitOSDJava.NavitStreetFontLetterWidth = (int) ((float) NavitOSDJava.NavitStreetFontLetterWidth_base * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other);
			}

			float real_factor = draw_factor2 / 1.5f;
			//
			//
			//

			sat_status_lt_w = (int) (8f * draw_factor);
			sat_status_lt_h = (int) (h * 0.35);
			sat_status_lt_x = (int) (2f * draw_factor);
			sat_status_lt_y = (int) ((h / 2) - (sat_status_lt_h / 2));

			r1.set(sat_status_lt_x, sat_status_lt_y, sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h);

			int mCanvasWidth = w;
			int mCanvasHeight = h;

			LayoutParams params = null;

			//
			child1 = Navit.Global_Navit_Object.findViewById(R.id.top_bar);
			child2 = Navit.Global_Navit_Object.findViewById(R.id.osd_compass_new);
			child3 = Navit.Global_Navit_Object.findViewById(R.id.osd_nextturn_new);
			child4 = Navit.Global_Navit_Object.findViewById(R.id.osd_timetoturn_new);
			child5 = Navit.Global_Navit_Object.findViewById(R.id.osd_timetodest_new);
			child6 = Navit.Global_Navit_Object.findViewById(R.id.osd_roaddistance_new);
			child7 = Navit.Global_Navit_Object.findViewById(R.id.osd_eta_new);
			child8 = Navit.Global_Navit_Object.findViewById(R.id.view_speeding);
			child9 = Navit.Global_Navit_Object.findViewById(R.id.view_laneassist);
			//

			this.need_size_change = false;
		}

		super.onDraw(c);

		// System.out.println("onDraw:ZANaviLinearLayout");

		try
		{
			if (!Navit.PAINT_OLD_API)
			{
				child1.postInvalidate();
				child2.postInvalidate();
				child3.postInvalidate();
				child4.postInvalidate();
				child5.postInvalidate();
				child6.postInvalidate();
				child7.postInvalidate();
				child8.postInvalidate();
				child9.postInvalidate();
			}
			else
			{
				//				child1.postInvalidate();
				//				child2.postInvalidate();
				//				child3.postInvalidate();
				//				child4.postInvalidate();
				//				child5.postInvalidate();
				//				child6.postInvalidate();
				//				child7.postInvalidate();
				//				child8.postInvalidate();
				//				child9.postInvalidate();
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			if (Navit.p.PREF_show_sat_status)
			{
				if (Navit.sats > sat_status_max_sats)
				{
					sat_status_max_sats = Navit.sats;
				}

				// old status
				NavitVehicle.sat_status_icon_last = NavitVehicle.sat_status_icon_now;

				// get new status
				//if (Navit.satsInFix > 3)
				if (Navit.isGPSFix)
				{
					// GPS found position
					NavitVehicle.sat_status_icon_now = 1;
				}
				else
				{
					// GPS lost position
					NavitVehicle.sat_status_icon_now = 0;
				}

				if (NavitVehicle.sat_status_icon_now != NavitVehicle.sat_status_icon_last)
				{
					try
					{
						//if (NavitVehicle.sat_status_icon_now == 1)
						if (Navit.isGPSFix)
						{
							Navit.Global_Navit_Object.getSupportActionBar().setIcon(R.drawable.ic_action_location_found);
						}
						else
						{
							Navit.Global_Navit_Object.getSupportActionBar().setIcon(R.drawable.ic_action_location_searching);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				r2.set(sat_status_lt_x, sat_status_lt_y + sat_status_lt_h - (sat_status_lt_h / sat_status_max_sats * Navit.sats), sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h);
				r3.set(sat_status_lt_x, sat_status_lt_y + sat_status_lt_h - (sat_status_lt_h / sat_status_max_sats * Navit.satsInFix), sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h);
				r4.set(sat_status_lt_x, sat_status_lt_y, sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h);

				// fill rect
				paint_sat_status.setColor(Color.GRAY);
				paint_sat_status.setStyle(Paint.Style.FILL);
				paint_sat_status.setStrokeWidth(0);
				paint_sat_status.setAntiAlias(true);
				c.drawRect(r1, paint_sat_status);

				// fill inactive sats
				paint_sat_status.setColor(Color.YELLOW);
				paint_sat_status.setStyle(Paint.Style.FILL);
				paint_sat_status.setStrokeWidth(0);
				paint_sat_status.setAntiAlias(true);
				c.drawRect(r2, paint_sat_status);

				// fill active sats
				paint_sat_status.setColor(Color.GREEN);
				paint_sat_status.setStyle(Paint.Style.FILL);
				paint_sat_status.setStrokeWidth(0);
				paint_sat_status.setAntiAlias(true);
				c.drawRect(r3, paint_sat_status);

				// black rect around it all
				paint_sat_status.setColor(Color.BLACK);
				paint_sat_status.setStyle(Paint.Style.STROKE);
				paint_sat_status.setStrokeWidth(1);
				paint_sat_status.setAntiAlias(true);
				c.drawRect(r4, paint_sat_status);
			}
			else
			// sat status is turned off!
			{
				// old status
				NavitVehicle.sat_status_icon_last = NavitVehicle.sat_status_icon_now;

				// get new status
				NavitVehicle.sat_status_icon_now = -1;

				if (NavitVehicle.sat_status_icon_now != NavitVehicle.sat_status_icon_last)
				{
					try
					{
						Navit.Global_Navit_Object.getSupportActionBar().setIcon(R.drawable.icon);
					}
					catch (Exception e)
					{
					}
				}

			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}

		if (!Navit.p.PREF_follow_gps)
		{
			if (!NavitGraphics.MAP_DISPLAY_OFF)
			{
				// show cross hair
				delta_1 = 8;
				delta_2 = 35;
				if (Navit.metrics.densityDpi >= 320) //&& (Navit.PREF_shrink_on_high_dpi))
				{
					paint_crosshair.setStrokeWidth(2);
					delta_1 = 8 * 2;
					delta_2 = 35 * 2;
				}
				else
				{
					paint_crosshair.setStrokeWidth(1);
				}
				paint_crosshair.setAntiAlias(true);
				c.drawLine(wm - delta_1, hm, wm - delta_2, hm, paint_crosshair);
				c.drawLine(wm + delta_1, hm, wm + delta_2, hm, paint_crosshair);
				c.drawLine(wm, hm - delta_1, wm, hm - delta_2, paint_crosshair);
				c.drawLine(wm, hm + delta_1, wm, hm + delta_2, paint_crosshair);
			}
		}

	}

	static void redraw_OSD(final int i)
	{
		Navit.runOnUI(new Runnable()
		{
			@Override
			public void run()
			{
				// System.out.println("onDraw:redraw_OSD");
				NavitGraphics.OSD_new.redraw_OSD_view(i);
			}
		});
	}

	public void redraw_OSD_view(int i)
	{
		//		child1 = Navit.Global_Navit_Object.findViewById(R.id.top_bar); // streetname
		//		child2 = Navit.Global_Navit_Object.findViewById(R.id.osd_compass_new);
		//		child3 = Navit.Global_Navit_Object.findViewById(R.id.osd_nextturn_new);
		//		child4 = Navit.Global_Navit_Object.findViewById(R.id.osd_timetoturn_new);
		//		child5 = Navit.Global_Navit_Object.findViewById(R.id.osd_timetodest_new);
		//		child6 = Navit.Global_Navit_Object.findViewById(R.id.osd_roaddistance_new);
		//		child7 = Navit.Global_Navit_Object.findViewById(R.id.osd_eta_new);
		//		child8 = Navit.Global_Navit_Object.findViewById(R.id.view_speeding);
		//		child9 = Navit.Global_Navit_Object.findViewById(R.id.view_laneassist);		

		switch (i)
		{
		case 1:
			child1.postInvalidate();
			break;
		case 2:
			child2.postInvalidate();
			break;
		case 3:
			child3.postInvalidate();
			break;
		case 4:
			child4.postInvalidate();
			break;
		case 5:
			child5.postInvalidate();
			break;
		case 6:
			child6.postInvalidate();
			break;
		case 7:
			child7.postInvalidate();
			break;
		case 8:
			child8.postInvalidate();
			break;
		case 9:
			child9.postInvalidate();
			break;
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		this.wm = w / 2;
		this.hm = h / 2;

		System.out.println("XYZ:ZANaviLinearLayout -> onSizeChanged w=" + w + " oldw=" + oldw + " h=" + h + " oldh=" + oldh);
		if ((w == oldw) & (h == oldh))
		{
		}
		else
		{
			this.need_size_change = true;
		}

	}
}
