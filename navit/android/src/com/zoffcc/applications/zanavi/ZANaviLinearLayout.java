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
			System.out.println("XYZ:ZANaviLinearLayout -> onDraw [need_size_change]");

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

			//System.out.println("xx3=" + w);
			//System.out.println("yy3=" + h);
			// now layout the new OSD views --------------------------
			// now layout the new OSD views --------------------------
			// now layout the new OSD views --------------------------

			// android.view.ViewGroup.LayoutParams params_osd = this.getLayoutParams();
			//params_osd.width = w;
			//this.setLayoutParams(params_osd);
			//System.out.println("axx3=" + this.getWidth());
			//System.out.println("ayy3=" + this.getHeight());

			int mCanvasWidth = w;
			int mCanvasHeight = h;
			//System.out.println("xx4=" + mCanvasWidth);
			//System.out.println("yy4=" + mCanvasHeight);

			NavitOSDJava.OSD_element_bg_001 = Color.argb(255, 80, 80, 150); // Color.argb(255, 190, 190, 190); // Color.argb(140, 136, 136, 136);
			NavitOSDJava.OSD_element_bg_001_compass = Color.argb(255, 236, 229, 182); //  236, 229, 182
			NavitOSDJava.OSD_element_text_001 = Color.argb(255, 255, 255, 255); // text color
			NavitOSDJava.OSD_element_text_shadow_001 = Color.rgb(0, 0, 0); // text shadow
			NavitOSDJava.OSD_element_text_shadow_width = (int) (5.0f * real_factor); // 3 + 2;

			// streetname --
			NavitOSDJava.nextt_str_w = mCanvasWidth;
			NavitOSDJava.nextt_str_h = (int) (65 * real_factor);
			NavitOSDJava.nextt_str_start_x = 8;
			NavitOSDJava.nextt_str_start_y = (int) (46 * real_factor);
			NavitOSDJava.nextt_str_font_size = (int) (41 * real_factor);
			// streetname --

			// compass --
			// abs
			NavitOSDJava.compass_radius = (int) (50 * real_factor);
			NavitOSDJava.compass_lt_x = (int) (mCanvasWidth - (2 * NavitOSDJava.compass_radius) - (16 * real_factor));
			NavitOSDJava.compass_lt_y = (int) (mCanvasHeight - (2 * NavitOSDJava.compass_radius) - ((16 + 50) * real_factor));
			// rel
			NavitOSDJava.compass_w = (int) (2 * NavitOSDJava.compass_radius + 16 * real_factor);
			NavitOSDJava.compass_h = (int) (2 * NavitOSDJava.compass_radius + 16 * real_factor);
			//
			NavitOSDJava.compass_center_x = (int) (NavitOSDJava.compass_w - NavitOSDJava.compass_radius - 8 * real_factor);
			NavitOSDJava.compass_center_y = (int) (NavitOSDJava.compass_h - NavitOSDJava.compass_radius - 8 * real_factor);
			// compass --

			// next turn icons
			// NavitOSDJava.nextt_w = (int) (100 * real_factor);
			// NavitOSDJava.nextt_h = (int) (100 * real_factor);
			// make the same size as compass!
			NavitOSDJava.nextt_w = NavitOSDJava.compass_w;
			NavitOSDJava.nextt_h = NavitOSDJava.compass_h;
			//
			NavitOSDJava.nextt_lt_x = (int) (10 * real_factor);
			NavitOSDJava.nextt_lt_y = (int) (mCanvasHeight - 50 * real_factor) - NavitOSDJava.nextt_h - 5;
			NavitOSDJava.nextt_lt_yB = (int) (60 * real_factor);
			int smaller_size = mCanvasWidth;
			float shrink_factor = 0.65f;
			if (mCanvasWidth > mCanvasHeight)
			{
				// phone is turned in landscape-mode
				smaller_size = mCanvasHeight;
				shrink_factor = 0.43f;
			}
			NavitOSDJava.nextt_hB = (int) (smaller_size * shrink_factor);
			// next turn icons

			// next turn streetname -- BIG --
			NavitOSDJava.nextt_str_wB = mCanvasWidth;
			NavitOSDJava.nextt_str_hB = (int) (65 * real_factor);
			NavitOSDJava.nextt_str_ltxB = 0;
			NavitOSDJava.nextt_str_ltyB = NavitOSDJava.nextt_lt_yB + NavitOSDJava.nextt_hB + 4;
			// next turn streetname -- BIG --

			// dist. to target
			NavitOSDJava.dttarget_w = (int) (2 * (int) (50 * real_factor) + 16 * real_factor);
			NavitOSDJava.dttarget_h = (int) (40 * real_factor);
			NavitOSDJava.dttarget_lt_x = (int) ((mCanvasWidth / 2) - (NavitOSDJava.dttarget_w / 2));
			NavitOSDJava.dttarget_lt_y = (int) (mCanvasHeight - 50 * real_factor);
			// dist. to target

			// next turn in
			NavitOSDJava.nt_w = (int) (100 * real_factor);
			NavitOSDJava.nt_h = (int) (40 * real_factor);
			NavitOSDJava.nt_lt_x = (int) (10 * real_factor);
			NavitOSDJava.nt_lt_y = (int) (mCanvasHeight - 50 * real_factor);
			// next turn in

			ZANaviOSDCompass OSD_comp01 = (ZANaviOSDCompass) this.findViewById(R.id.osd_compass);
			android.view.ViewGroup.LayoutParams params = (LayoutParams) OSD_comp01.getLayoutParams();
			params.width = NavitOSDJava.compass_w;
			params.height = NavitOSDJava.compass_h;
			OSD_comp01.setLayoutParams(params);
			OSD_comp01.requestLayout();
			OSD_comp01.postInvalidate();

			// streetname --
			ZANaviOSDStreetname OSD_streetname = (ZANaviOSDStreetname) this.findViewById(R.id.osd_streetname);
			params = (LayoutParams) OSD_streetname.getLayoutParams();
			//System.out.println("1xx=" + OSD_streetname.getWidth() + " view=" + OSD_streetname);
			params.width = NavitOSDJava.nextt_str_w;
			params.height = NavitOSDJava.nextt_str_h;
			OSD_streetname.setLayoutParams(params);
			OSD_streetname.requestLayout();
			OSD_streetname.postInvalidate();
			//
			int fill_1_height = 20;
			View fill_1 = (View) this.findViewById(R.id.fill_1);
			params = (LayoutParams) fill_1.getLayoutParams();
			params.width = 1;
			params.height = fill_1_height;
			fill_1.setLayoutParams(params);
			fill_1.requestLayout();
			fill_1.invalidate();
			//
			View fill_2 = (View) this.findViewById(R.id.fill_2);
			params = (LayoutParams) fill_2.getLayoutParams();
			params.width = 1;
			params.height = NavitOSDJava.compass_lt_y - NavitOSDJava.nextt_str_h - fill_1_height;
			fill_2.setLayoutParams(params);
			fill_2.requestLayout();
			fill_2.invalidate();
			//
			// ------------- next line
			//
			View fill_3 = (View) this.findViewById(R.id.fill_3);
			params = (LayoutParams) fill_3.getLayoutParams();
			params.width = NavitOSDJava.nextt_lt_x;
			params.height = 1;
			fill_3.setLayoutParams(params);
			fill_3.requestLayout();
			fill_3.invalidate();
			//

			// ------ next turn icon --------
			ZANaviOSDNextturn osd_nextturn = (ZANaviOSDNextturn) this.findViewById(R.id.osd_nextturn);
			params = (LayoutParams) osd_nextturn.getLayoutParams();
			params.width = NavitOSDJava.nextt_w + 8;
			params.height = NavitOSDJava.nextt_h + 8;
			osd_nextturn.setLayoutParams(params);
			osd_nextturn.requestLayout();
			osd_nextturn.invalidate();
			// ------ next turn icon --------

			//
			View fill_4 = (View) this.findViewById(R.id.fill_4);
			params = (LayoutParams) fill_4.getLayoutParams();
			params.width = NavitOSDJava.dttarget_lt_x - NavitOSDJava.nextt_w - NavitOSDJava.nextt_lt_x - 8;
			params.height = 1;
			fill_4.setLayoutParams(params);
			fill_4.requestLayout();
			fill_4.invalidate();
			//
			ZANaviOSDTimeToDest osd_timetodest = (ZANaviOSDTimeToDest) this.findViewById(R.id.osd_timetodest);
			params = (LayoutParams) osd_timetodest.getLayoutParams();
			params.width = NavitOSDJava.dttarget_w;
			params.height = NavitOSDJava.dttarget_h;
			osd_timetodest.setLayoutParams(params);
			osd_timetodest.requestLayout();
			osd_timetodest.invalidate();
			//
			View fill_5 = (View) this.findViewById(R.id.fill_5);
			params = (LayoutParams) fill_5.getLayoutParams();
			params.width = NavitOSDJava.compass_lt_x - NavitOSDJava.dttarget_w - (NavitOSDJava.dttarget_lt_x - NavitOSDJava.nextt_w - NavitOSDJava.nextt_lt_x) - NavitOSDJava.nextt_w - NavitOSDJava.nextt_lt_x;
			params.height = 1;
			fill_5.setLayoutParams(params);
			fill_5.requestLayout();
			fill_5.invalidate();
			//
			// ------------- next line
			//
			View fill_7 = (View) this.findViewById(R.id.fill_7);
			params = (LayoutParams) fill_7.getLayoutParams();
			params.width = 1;
			params.height = NavitOSDJava.nt_lt_y - (NavitOSDJava.compass_lt_y + NavitOSDJava.compass_h);
			fill_7.setLayoutParams(params);
			fill_7.requestLayout();
			fill_7.invalidate();
			//
			// ------------- next line
			//
			View fill_8 = (View) this.findViewById(R.id.fill_8);
			params = (LayoutParams) fill_8.getLayoutParams();
			params.width = NavitOSDJava.nextt_lt_x;
			params.height = 1;
			fill_8.setLayoutParams(params);
			fill_8.requestLayout();
			fill_8.invalidate();

			//
			ZANaviOSDDistToNextturn osd_timetoturn = (ZANaviOSDDistToNextturn) this.findViewById(R.id.osd_timetoturn);
			params = (LayoutParams) osd_timetoturn.getLayoutParams();
			params.width = NavitOSDJava.nextt_w;
			params.height = (int) (40 * real_factor);
			osd_timetoturn.setLayoutParams(params);
			osd_timetoturn.requestLayout();
			osd_timetoturn.invalidate();

			//
			View fill_9 = (View) this.findViewById(R.id.fill_9);
			params = (LayoutParams) fill_9.getLayoutParams();
			params.width = NavitOSDJava.dttarget_lt_x - NavitOSDJava.nextt_w - NavitOSDJava.nextt_lt_x;
			params.height = 1;
			fill_9.setLayoutParams(params);
			fill_9.requestLayout();
			fill_9.invalidate();

			//
			ZANaviOSDRoadDist osd_roaddistance = (ZANaviOSDRoadDist) this.findViewById(R.id.osd_roaddistance);
			params = (LayoutParams) osd_roaddistance.getLayoutParams();
			params.width = NavitOSDJava.dttarget_w;
			params.height = (int) (40 * real_factor);
			osd_roaddistance.setLayoutParams(params);
			osd_roaddistance.requestLayout();
			osd_roaddistance.invalidate();

			//
			View fill_10 = (View) this.findViewById(R.id.fill_10);
			params = (LayoutParams) fill_10.getLayoutParams();
			params.width = NavitOSDJava.compass_lt_x - NavitOSDJava.dttarget_w - (NavitOSDJava.dttarget_lt_x - NavitOSDJava.nextt_w - NavitOSDJava.nextt_lt_x) - NavitOSDJava.nextt_w - NavitOSDJava.nextt_lt_x;
			params.height = 1;
			fill_10.setLayoutParams(params);
			fill_10.requestLayout();
			fill_10.invalidate();

			//
			ZANaviOSDAirDist osd_airdistance = (ZANaviOSDAirDist) this.findViewById(R.id.osd_airdistance);
			params = (LayoutParams) osd_airdistance.getLayoutParams();
			params.width = NavitOSDJava.compass_w;
			params.height = (int) (40 * real_factor);
			osd_airdistance.setLayoutParams(params);
			osd_airdistance.requestLayout();
			osd_airdistance.invalidate();

			//
			child1 = this.findViewById(R.id.osd_streetname);
			child2 = this.findViewById(R.id.osd_compass);
			child3 = this.findViewById(R.id.osd_nextturn);
			child4 = this.findViewById(R.id.osd_timetoturn);
			child5 = this.findViewById(R.id.osd_timetodest);
			child6 = this.findViewById(R.id.osd_roaddistance);
			child7 = this.findViewById(R.id.osd_airdistance);
			//

			//this.requestLayout();
			//this.postInvalidate();
			this.need_size_change = false;
		}

		super.onDraw(c);

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
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			if (Navit.PREF_show_sat_status)
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

		if (!Navit.PREF_follow_gps)
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
