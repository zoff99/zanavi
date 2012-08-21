/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2012 Zoff <zoff@zoff.cc>
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class NavitOSDJava extends ImageView
{
	public int mCanvasWidth = 1;
	public int mCanvasHeight = 1;
	public static float draw_factor = 1.0f;

	public static Handler progress_handler_ = new Handler();

	private static Boolean show_scale = false;

	private static Boolean one_shot = false;
	private static long last_paint_me = 0L;

	// this number is an estimate
	final int NavitStreetFontLetterWidth = 28;

	static Bitmap compass_b = null;
	static Canvas compass_c = null;
	static int compass_radius = 0;
	static int compass_center_x = 0;
	static int compass_center_y = 0;
	static int compass_lt_x = 0;
	static int compass_lt_y = 0;
	static int compass_w = 0;
	static int compass_h = 0;
	static Boolean did_draw_circle = false;

	static Bitmap ddtt_b = null;
	static Canvas ddtt_c = null;
	static int ddtt_lt_x = 0;
	static int ddtt_lt_y = 0;
	static int ddtt_text_start_x = 0;
	static int ddtt_text_start_y = 0;
	static int ddtt_w = 0;
	static int ddtt_h = 0;
	static int ddtt_font_size = 0;

	static Bitmap eta_b = null;
	static Canvas eta_c = null;
	static int eta_lt_x = 0;
	static int eta_lt_y = 0;
	static int eta_text_start_x = 0;
	static int eta_text_start_y = 0;
	static int eta_w = 0;
	static int eta_h = 0;
	static int eta_font_size = 0;

	static Bitmap nt_b = null;
	static Canvas nt_c = null;
	static int nt_lt_x = 0;
	static int nt_lt_y = 0;
	static int nt_lt_xB = 0;
	static int nt_lt_yB = 0;
	static int nt_text_start_x = 0;
	static int nt_text_start_y = 0;
	static int nt_w = 0;
	static int nt_h = 0;
	static int nt_font_size = 0;

	static Bitmap dttarget_b = null;
	static Canvas dttarget_c = null;
	static int dttarget_lt_x = 0;
	static int dttarget_lt_y = 0;
	static int dttarget_text_start_x = 0;
	static int dttarget_text_start_y = 0;
	static int dttarget_w = 0;
	static int dttarget_h = 0;
	static int dttarget_font_size = 0;

	static Bitmap scale_b = null;
	static Canvas scale_c = null;
	static int scale_lt_x = 0;
	static int scale_lt_y = 0;
	static int scale_text_start_x = 0;
	static int scale_text_start_y = 0;
	static int scale_line_start_x = 0;
	static int scale_line_end_x = 0;
	static int scale_line_start_y = 0;
	static int scale_line_middle_y = 0;
	static int scale_line_end_y = 0;
	static int scale_w = 0;
	static int scale_h = 0;
	static int scale_font_size = 0;

	static int nextt_lt_x = 0;
	static int nextt_lt_y = 0;
	static int nextt_w = 0;
	static int nextt_h = 0;
	static int nextt_lt_xB = 0;
	static int nextt_lt_yB = 0;
	static int nextt_wB = 0;
	static int nextt_hB = 0;

	static int nextt_str_ltx = 0;
	static int nextt_str_lty = 0;
	static int nextt_str_ltxB = 0;
	static int nextt_str_ltyB = 0;
	static int nextt_str_w = 0;
	static int nextt_str_h = 0;
	static int nextt_str_wB = 0;
	static int nextt_str_hB = 0;
	static int nextt_str_font_size = 0;
	static int nextt_str_start_x = 0;
	static int nextt_str_start_y = 0;

	static int sat_status_lt_x = 0;
	static int sat_status_lt_y = 0;
	static int sat_status_lt_w = 0;
	static int sat_status_lt_h = 0;
	static int sat_status_max_sats = 13;

	static Paint paint = new Paint();

	// private long last_timestamp = 0L;
	// public static final int UPDATE_INTERVAL = 400; // in ms

	public NavitOSDJava(Context context)
	{
		super(context);

		progress_handler_ = this.progress_handler;

		// b_ = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		// compass_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		compass_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		compass_c = new Canvas(compass_b);
		ddtt_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		ddtt_c = new Canvas(ddtt_b);
		scale_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		scale_c = new Canvas(ddtt_b);
		dttarget_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		dttarget_c = new Canvas(dttarget_b);
		eta_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		eta_c = new Canvas(eta_b);
		nt_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		nt_c = new Canvas(nt_b);

		//		this.setOnLongClickListener(new OnLongClickListener()
		//		{
		//			@Override
		//			public boolean onLongClick(View v)
		//			{
		//				return true;
		//			}
		//		});
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		this.mCanvasWidth = w;
		this.mCanvasHeight = h;

		sat_status_lt_x = 2;
		sat_status_lt_y = (int) (this.mCanvasHeight * 0.15);
		sat_status_lt_w = 6;
		sat_status_lt_h = this.mCanvasHeight - (int) (this.mCanvasHeight * (0.35 + 0.15));
		sat_status_max_sats = 13;

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

		float real_factor = draw_factor / 1.5f;

		// abs
		compass_radius = (int) (50 * real_factor);
		compass_lt_x = (int) (mCanvasWidth - (2 * compass_radius) - (16 * real_factor));
		compass_lt_y = (int) (mCanvasHeight - (2 * compass_radius) - ((16 + 50) * real_factor));
		// rel
		compass_w = (int) (2 * compass_radius + 16 * real_factor);
		compass_h = (int) (2 * compass_radius + 16 * real_factor);
		compass_center_x = (int) (compass_w - compass_radius - 8 * real_factor);
		compass_center_y = (int) (compass_h - compass_radius - 8 * real_factor);

		ddtt_w = (int) (compass_w);
		ddtt_h = (int) (40 * real_factor);
		ddtt_lt_x = (int) (mCanvasWidth - (2 * compass_radius) - (16 * real_factor));
		ddtt_lt_y = (int) (mCanvasHeight - 50 * real_factor);
		ddtt_text_start_x = (int) (20 * real_factor);
		ddtt_text_start_y = (int) (30 * real_factor);
		ddtt_font_size = (int) (24 * real_factor);

		dttarget_w = (int) (2 * (int) (50 * real_factor) + 16 * real_factor);
		dttarget_h = (int) (40 * real_factor);
		dttarget_lt_x = (int) ((mCanvasWidth / 2) - (dttarget_w / 2));
		dttarget_lt_y = (int) (mCanvasHeight - 50 * real_factor);
		dttarget_text_start_x = (int) (20 * real_factor);
		dttarget_text_start_y = (int) (30 * real_factor);
		dttarget_font_size = (int) (24 * real_factor);

		eta_w = (int) (2 * (int) (50 * real_factor) + 16 * real_factor);
		eta_h = (int) (40 * real_factor);
		eta_lt_x = (int) (int) ((mCanvasWidth / 2) - (eta_w / 2));
		eta_lt_y = (int) (mCanvasHeight - 50 * real_factor) - dttarget_h - 5;
		eta_text_start_x = (int) (20 * real_factor);
		eta_text_start_y = (int) (30 * real_factor);
		eta_font_size = (int) (24 * real_factor);

		scale_w = (int) (202 * real_factor);
		scale_h = (int) (40 * real_factor);
		scale_line_start_x = (int) (1 * real_factor);
		scale_line_end_x = (int) (201 * real_factor);
		scale_line_start_y = (int) (1 * real_factor);
		scale_line_middle_y = (int) (1 * real_factor);
		scale_line_end_y = (int) (8 * real_factor);
		scale_lt_x = (int) (mCanvasWidth - scale_w - 4);
		scale_lt_y = (int) (65);
		scale_text_start_x = (int) (10 * real_factor);
		scale_text_start_y = (int) (30 * real_factor);
		scale_font_size = (int) (24 * real_factor);

		// next turn icons
		nextt_w = (int) (100 * real_factor);
		nextt_h = (int) (100 * real_factor);
		nextt_lt_x = (int) (10 * real_factor);
		nextt_lt_y = (int) (mCanvasHeight - 50 * real_factor) - nextt_h - 5;
		// next turn icons

		// next turn icons -- BIG --
		int smaller_size = mCanvasWidth;
		float shrink_factor = 0.65f;
		if (mCanvasWidth > mCanvasHeight)
		{
			// phone is turned in landscape-mode
			smaller_size = mCanvasHeight;
			shrink_factor = 0.43f;
		}

		nextt_wB = (int) (smaller_size * shrink_factor);
		nextt_hB = (int) (smaller_size * shrink_factor);
		nextt_lt_xB = (int) ((mCanvasWidth - nextt_wB) / 2);
		nextt_lt_yB = (int) (60 * real_factor);
		// next turn icons -- BIG --

		// next turn streetname --
		nextt_str_w = mCanvasWidth;
		nextt_str_h = (int) (65 * real_factor);
		nextt_str_ltx = 0;
		nextt_str_lty = 20;
		nextt_str_font_size = (int) (41 * real_factor);
		nextt_str_start_x = 8;
		nextt_str_start_y = (int) (46 * real_factor);
		// next turn streetname --

		// next turn streetname -- BIG --
		nextt_str_wB = mCanvasWidth;
		nextt_str_hB = (int) (65 * real_factor);
		nextt_str_ltxB = 0;
		nextt_str_ltyB = nextt_lt_yB + nextt_hB + 4;
		// next turn streetname -- BIG --

		// next turn in XX m --
		nt_w = (int) (100 * real_factor);
		nt_h = (int) (40 * real_factor);
		nt_lt_x = (int) (10 * real_factor);
		nt_lt_y = (int) (mCanvasHeight - 50 * real_factor);
		nt_text_start_x = (int) (20 * real_factor);
		nt_text_start_y = (int) (30 * real_factor);
		nt_font_size = (int) (24 * real_factor);
		// next turn in XX m --

		// next turn in XX m -- BIG --
		nt_lt_xB = nextt_lt_xB;
		nt_lt_yB = nextt_lt_yB - nt_h - 4;
		// next turn in XX m -- BIG --

		if (compass_b != null)
		{
			compass_b.recycle();
		}
		compass_b = Bitmap.createBitmap(compass_w, compass_h, Bitmap.Config.ARGB_4444);
		compass_c = new Canvas(compass_b);

		if (ddtt_b != null)
		{
			ddtt_b.recycle();
		}
		ddtt_b = Bitmap.createBitmap(ddtt_w, ddtt_h, Bitmap.Config.ARGB_4444);
		ddtt_c = new Canvas(ddtt_b);

		if (scale_b != null)
		{
			scale_b.recycle();
		}
		scale_b = Bitmap.createBitmap(scale_w, scale_h, Bitmap.Config.ARGB_4444);
		scale_c = new Canvas(scale_b);

		if (dttarget_b != null)
		{
			dttarget_b.recycle();
		}
		dttarget_b = Bitmap.createBitmap(dttarget_w, dttarget_h, Bitmap.Config.ARGB_4444);
		dttarget_c = new Canvas(dttarget_b);

		if (eta_b != null)
		{
			eta_b.recycle();
		}
		eta_b = Bitmap.createBitmap(eta_w, eta_h, Bitmap.Config.ARGB_4444);
		eta_c = new Canvas(eta_b);

		if (nt_b != null)
		{
			nt_b.recycle();
		}
		nt_b = Bitmap.createBitmap(nt_w, nt_h, Bitmap.Config.ARGB_4444);
		nt_c = new Canvas(nt_b);

	}

	public static void draw_real()
	{
		//if (!NavitGraphics.MAP_DISPLAY_OFF)
		//{
		/*
		 * if ((last_paint_me + 100) < System.currentTimeMillis())
		 * {
		 * try
		 * {
		 * last_paint_me = System.currentTimeMillis();
		 * }
		 * catch (Exception r)
		 * {
		 * //r.printStackTrace();
		 * }
		 * }
		 * else
		 * {
		 * return;
		 * }
		 */
		//}
		//System.out.println("draw real 1");

		try
		{
			compass_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			ddtt_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			scale_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			dttarget_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			eta_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			nt_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

			// compass_c.drawColor(Color.LTGRAY);
			// ddtt_c.drawColor(Color.CYAN);

			int dest_valid = NavitGraphics.CallbackDestinationValid2();

			did_draw_circle = false;
			if (!NavitGraphics.MAP_DISPLAY_OFF)
			{

				if (Navit.OSD_compass.angle_north_valid)
				{
					paint.setColor(Color.BLACK);
					paint.setStyle(Paint.Style.STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					compass_c.drawCircle(compass_center_x, compass_center_y, compass_radius, paint);
					did_draw_circle = true;
					int end_x = (int) ((float) Math.sin((float) Math.toRadians(Navit.OSD_compass.angle_north)) * compass_radius);
					int end_y = (int) ((float) Math.cos((float) Math.toRadians(Navit.OSD_compass.angle_north)) * compass_radius);
					// System.out.println("x " + end_x + " y " + end_y);
					compass_c.drawLine(compass_center_x - end_x, compass_center_y + end_y, compass_center_x, compass_center_y, paint);
					paint.setColor(Color.RED);
					paint.setStrokeWidth(4);
					compass_c.drawLine(compass_center_x + end_x, compass_center_y - end_y, compass_center_x, compass_center_y, paint);
				}
				if ((Navit.OSD_compass.angle_target_valid) && (dest_valid > 0))
				{
					paint.setColor(Color.BLACK);
					paint.setStyle(Paint.Style.STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					if (!did_draw_circle)
					{
						compass_c.drawCircle(compass_center_x, compass_center_y, compass_radius, paint);
						did_draw_circle = true;
					}
					int end_x = (int) ((float) Math.sin((float) Math.toRadians(Navit.OSD_compass.angle_target)) * compass_radius);
					int end_y = (int) ((float) Math.cos((float) Math.toRadians(Navit.OSD_compass.angle_target)) * compass_radius);
					// System.out.println("x " + end_x + " y " + end_y);
					paint.setColor(Color.GREEN);
					compass_c.drawLine(compass_center_x, compass_center_y, compass_center_x + end_x, compass_center_y - end_y, paint);
				}
			}

			if ((Navit.OSD_compass.direct_distance_to_target_valid) && (dest_valid > 0))
			{
				paint.setColor(Color.argb(140, 136, 136, 136));
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(2);
				paint.setAntiAlias(true);
				ddtt_c.drawRoundRect(new RectF(0, 0, ddtt_w, ddtt_h), 10, 10, paint);

				paint.setColor(Color.BLACK);
				paint.setStrokeWidth(3);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(ddtt_font_size);
				paint.setAntiAlias(true);
				ddtt_c.drawText(Navit.OSD_compass.direct_distance_to_target, ddtt_text_start_x, ddtt_text_start_y, paint);
			}
			if ((Navit.OSD_route_001.arriving_time_valid) && (dest_valid > 0))
			{
				paint.setColor(Color.argb(140, 136, 136, 136));
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(2);
				paint.setAntiAlias(true);
				eta_c.drawRoundRect(new RectF(0, 0, eta_w, eta_h), 10, 10, paint);

				paint.setColor(Color.BLACK);
				paint.setStrokeWidth(3);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(eta_font_size);
				paint.setAntiAlias(true);
				eta_c.drawText(Navit.OSD_route_001.arriving_time, eta_text_start_x, eta_text_start_y, paint);
			}
			if ((Navit.OSD_route_001.driving_distance_to_target_valid) && (dest_valid > 0))
			{
				paint.setColor(Color.argb(140, 136, 136, 136));
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(2);
				paint.setAntiAlias(true);
				dttarget_c.drawRoundRect(new RectF(0, 0, dttarget_w, dttarget_h), 10, 10, paint);

				paint.setColor(Color.BLACK);
				paint.setStrokeWidth(3);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(dttarget_font_size);
				paint.setAntiAlias(true);
				dttarget_c.drawText(Navit.OSD_route_001.driving_distance_to_target, dttarget_text_start_x, dttarget_text_start_y, paint);
			}
			if ((Navit.OSD_nextturn.nextturn_distance_valid) && (dest_valid > 0))
			{
				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					paint.setColor(Color.argb(255, 20, 20, 230));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					nt_c.drawRoundRect(new RectF(0, 0, nt_w, nt_h), 10, 10, paint);

					paint.setColor(Color.WHITE);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(nt_font_size);
					paint.setAntiAlias(true);
					nt_c.drawText(Navit.OSD_nextturn.nextturn_distance, nt_text_start_x, nt_text_start_y, paint);
				}
				else
				{
					paint.setColor(Color.argb(140, 136, 136, 136));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					nt_c.drawRoundRect(new RectF(0, 0, nt_w, nt_h), 10, 10, paint);

					paint.setColor(Color.BLACK);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(nt_font_size);
					paint.setAntiAlias(true);
					nt_c.drawText(Navit.OSD_nextturn.nextturn_distance, nt_text_start_x, nt_text_start_y, paint);
				}
			}

			if (show_scale)
			{
				if (Navit.OSD_scale.scale_valid)
				{
					paint.setColor(Color.BLACK);
					paint.setStrokeWidth(3);
					paint.setAntiAlias(true);
					paint.setStyle(Paint.Style.STROKE);
					// mothod a
					scale_c.drawLine(scale_line_start_x, scale_line_middle_y, scale_line_start_x + Navit.OSD_scale.var, scale_line_middle_y, paint);
					// method b
					// scale_c.drawLine(scale_line_start_x, scale_line_middle_y, scale_line_end_x, scale_line_middle_y, paint);
					scale_c.drawLine(scale_line_start_x, scale_line_start_y, scale_line_start_x, scale_line_end_y, paint);
					scale_c.drawLine(scale_line_start_x + Navit.OSD_scale.var, scale_line_start_y, scale_line_start_x + Navit.OSD_scale.var, scale_line_end_y, paint);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(scale_font_size);
					scale_c.drawText(Navit.OSD_scale.scale_text, scale_text_start_x, scale_text_start_y, paint);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (NavitGraphics.MAP_DISPLAY_OFF)
		{
			one_shot = true;
		}
	}

	public void onDraw(Canvas c)
	{
		//System.out.println("draw real 2");

		try
		{
			c.drawBitmap(compass_b, compass_lt_x, compass_lt_y, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			c.drawBitmap(ddtt_b, ddtt_lt_x, ddtt_lt_y, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			c.drawBitmap(dttarget_b, dttarget_lt_x, dttarget_lt_y, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			c.drawBitmap(eta_b, eta_lt_x, eta_lt_y, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (show_scale)
		{
			try
			{
				c.drawBitmap(scale_b, scale_lt_x, scale_lt_y, null);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		try
		{
			if (NavitGraphics.MAP_DISPLAY_OFF)
			{
				c.drawBitmap(nt_b, nt_lt_xB, nt_lt_yB, null);
			}
			else
			{
				c.drawBitmap(nt_b, nt_lt_x, nt_lt_y, null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if ((Navit.OSD_nextturn.nextturn_streetname != null) || (Navit.PREF_follow_gps))
			{
				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					paint.setColor(Color.argb(255, 20, 20, 230));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					c.drawRoundRect(new RectF(nextt_str_ltxB, nextt_str_ltyB, nextt_str_ltxB + nextt_str_wB, nextt_str_ltyB + nextt_str_hB), 10, 10, paint);
					paint.setColor(Color.WHITE);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					if (Navit.OSD_nextturn.nextturn_streetname.length() > (nextt_str_wB) / NavitStreetFontLetterWidth)
					{
						paint.setTextSize((int) (nextt_str_font_size * 0.70));
					}
					else
					{
						paint.setTextSize(nextt_str_font_size);
					}
					paint.setAntiAlias(true);
					c.drawText(Navit.OSD_nextturn.nextturn_streetname_systematic + " " + Navit.OSD_nextturn.nextturn_streetname, nextt_str_ltxB + nextt_str_start_x, nextt_str_ltyB + nextt_str_start_y, paint);
				}
				else
				{
					paint.setColor(Color.argb(140, 136, 136, 136));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					c.drawRoundRect(new RectF(nextt_str_ltxB, nextt_str_lty, nextt_str_ltx + nextt_str_w, nextt_str_lty + nextt_str_h), 10, 10, paint);
					paint.setColor(Color.BLACK);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					if (Navit.OSD_nextturn.nextturn_streetname.length() > (nextt_str_wB) / NavitStreetFontLetterWidth)
					{
						paint.setTextSize((int) (nextt_str_font_size * 0.70));
					}
					else
					{
						paint.setTextSize(nextt_str_font_size);
					}
					paint.setAntiAlias(true);
					c.drawText(Navit.OSD_nextturn.nextturn_streetname_systematic + " " + Navit.OSD_nextturn.nextturn_streetname, nextt_str_ltx + nextt_str_start_x, nextt_str_lty + nextt_str_start_y, paint);
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
					c.drawRect(new RectF(nextt_str_ltxB, nextt_str_ltyB, nextt_str_ltxB + nextt_str_wB, nextt_str_ltyB + nextt_str_hB), paint);
				}
				else
				{
					c.drawRect(new RectF(nextt_str_ltx, nextt_str_lty, nextt_str_ltx + nextt_str_w, nextt_str_lty + nextt_str_h), paint);
				}
			}
		}
		catch (Exception e)
		{

		}

		try
		{
			if ((Navit.OSD_nextturn.nextturn_image_valid) && (NavitGraphics.CallbackDestinationValid2() > 0))
			{
				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					paint.setColor(Color.argb(255, 20, 20, 230));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					c.drawRoundRect(new RectF(nextt_lt_xB, nextt_lt_yB, nextt_lt_xB + nextt_wB, nextt_lt_yB + nextt_hB), 10, 10, paint);
					Rect dst = new Rect(nextt_lt_xB, nextt_lt_yB, nextt_lt_xB + nextt_wB, nextt_lt_yB + nextt_hB);
					c.drawBitmap(Navit.OSD_nextturn.nextturn_image, null, dst, null);
				}
				else
				{
					paint.setColor(Color.argb(140, 136, 136, 136));
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					c.drawRoundRect(new RectF(nextt_lt_x, nextt_lt_y, nextt_lt_x + nextt_w, nextt_lt_y + nextt_h), 10, 10, paint);
					Rect dst = new Rect(nextt_lt_x, nextt_lt_y, nextt_lt_x + nextt_w, nextt_lt_y + nextt_h);
					c.drawBitmap(Navit.OSD_nextturn.nextturn_image, null, dst, null);
				}
				// c.drawBitmap(Navit.OSD_nextturn.nextturn_image, nextt_lt_x, nextt_lt_y, null);
			}
			else
			{
				paint.setColor(Color.argb(0, 0, 0, 0));
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(3);
				paint.setAntiAlias(false);
				if (NavitGraphics.MAP_DISPLAY_OFF)
				{
					c.drawRect(new RectF(nextt_lt_xB, nextt_lt_yB, nextt_lt_xB + nextt_wB, nextt_lt_yB + nextt_hB), paint);
				}
				else
				{
					c.drawRect(new RectF(nextt_lt_x, nextt_lt_y, nextt_lt_x + nextt_w, nextt_lt_y + nextt_h), paint);
				}
				//c.clipRect(nextt_lt_x, nextt_lt_y, nextt_lt_x + nextt_w, nextt_lt_y + nextt_h);
				//c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (Navit.PREF_show_sat_status)
			{
				// fill rect
				paint.setColor(Color.GRAY);
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(0);
				paint.setAntiAlias(true);
				c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y, sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);

				// fill inactive sats
				paint.setColor(Color.YELLOW);
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(0);
				paint.setAntiAlias(true);
				c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y + sat_status_lt_h - (sat_status_lt_h / sat_status_max_sats * Navit.sats), sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);

				// fill active sats
				paint.setColor(Color.GREEN);
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(0);
				paint.setAntiAlias(true);
				c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y + sat_status_lt_h - (sat_status_lt_h / sat_status_max_sats * Navit.satsInFix), sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);

				// black rect around it all
				paint.setColor(Color.BLACK);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(1);
				paint.setAntiAlias(true);
				c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y, sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (!Navit.PREF_follow_gps)
		{
			if (!NavitGraphics.MAP_DISPLAY_OFF)
			{
				// show cross hair
				paint.setColor(Color.DKGRAY);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(1);
				paint.setAntiAlias(true);
				int wm = mCanvasWidth / 2;
				int hm = mCanvasHeight / 2;
				c.drawLine(wm - 8, hm, wm - 35, hm, paint);
				c.drawLine(wm + 8, hm, wm + 35, hm, paint);
				c.drawLine(wm, hm - 8, wm, hm - 35, paint);
				c.drawLine(wm, hm + 8, wm, hm + 35, paint);
			}
		}

		if (NavitGraphics.MAP_DISPLAY_OFF)
		{
			if (one_shot)
			{
				//System.out.println("one shot");
				one_shot = false;
				this.postInvalidate();
			}
		}
	}

	//	@Override
	//	public boolean onTouchEvent(MotionEvent event)
	//	{
	//		Boolean x = super.onTouchEvent(event);
	//		return false;
	//	}

	public Handler progress_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
				postInvalidate();
				break;
			}
		}
	};
}
