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
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;

public class NavitOSDJava // extends View
{
	public static int mCanvasWidth = 1;
	public static int mCanvasHeight = 1;
	public static float draw_factor = 1.0f;

	public static int OSD_element_bg_001;
	public static int OSD_element_bg_001_compass;
	public static int OSD_element_text_001;
	public static int OSD_element_text_shadow_001;
	public static int OSD_element_text_shadow_width;

	int delta_1 = 8;
	int delta_2 = 35;
	int wm;
	int hm;
	static int dest_valid;
	static RectF dst;
	static int end_x;
	static int end_y;

	private static Object sync_dummy_001 = new Object();
	private static Boolean allow_drawing = true;
	public static Integer synchro_obj = 0;
	// private static Boolean in_draw_real = false;

	public static Handler progress_handler_ = new Handler();

	private static Boolean show_scale = false;

	//private static Boolean one_shot = false;
	private static long last_paint_me = 0L;

	// this number is an estimate
	static int NavitStreetFontLetterWidth = 28;
	static final int NavitStreetFontLetterWidth_base = 28;

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

	Bitmap scale_b = null;
	Canvas scale_c = null;
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

	Bitmap rest_osd_b = null;
	Canvas rest_osd_c = null;

	Bitmap buffer_osd_b = null;
	Canvas buffer_osd_c = null;

	// Bitmap buffer2_osd_b = null;
	// Canvas buffer2_osd_c = null;

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
	static Paint paint_crosshair = new Paint();

	// private long last_timestamp = 0L;
	// public static final int UPDATE_INTERVAL = 400; // in ms

	
	// ------ UNUSED !!!!!!!! --------
	// ------ UNUSED !!!!!!!! --------
	// ------ UNUSED !!!!!!!! --------
	// ------ UNUSED !!!!!!!! --------
	public NavitOSDJava(Context context)
	{
		//		// super(context);
		//
		//		progress_handler_ = this.progress_handler;
		//
		//		OSD_element_bg_001 = Color.argb(255, 80, 80, 150); // Color.argb(255, 190, 190, 190); // Color.argb(140, 136, 136, 136);
		//		OSD_element_bg_001_compass = Color.argb(255, 236, 229, 182); //  236, 229, 182
		//		OSD_element_text_001 = Color.argb(255, 255, 255, 255); // text color
		//		OSD_element_text_shadow_001 = Color.rgb(0, 0, 0); // text shadow
		//		OSD_element_text_shadow_width = 5; // 3 + 2;
		//
		//		// b_ = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		// compass_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		//		compass_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		compass_c = new Canvas(compass_b);
		//		ddtt_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		ddtt_c = new Canvas(ddtt_b);
		//		scale_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		scale_c = new Canvas(ddtt_b);
		//		dttarget_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		dttarget_c = new Canvas(dttarget_b);
		//		eta_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		eta_c = new Canvas(eta_b);
		//		nt_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		nt_c = new Canvas(nt_b);
		//
		//		rest_osd_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		rest_osd_c = new Canvas(rest_osd_b);
		//
		//		buffer_osd_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		buffer_osd_c = new Canvas(buffer_osd_b);
		//
		//		// buffer2_osd_b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		//		// buffer2_osd_c = new Canvas(buffer2_osd_b);
	}

	//	@Override
	//	public void onSizeChanged(int w, int h, int oldw, int oldh)
	//	{
	//		super.onSizeChanged(w, h, oldw, oldh);
	//	}

	//@Override
	public void onSizeChangedXX(int w, int h, int oldw, int oldh)
	{
		// super.onSizeChanged(w, h, oldw, oldh);
		this.mCanvasWidth = w;
		this.mCanvasHeight = h;

		wm = w / 2;
		hm = h / 2;

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

		// correct for ultra high DPI
		if (Navit.metrics.densityDpi >= 320) //&& (Navit.PREF_shrink_on_high_dpi))
		{
			draw_factor = 1.8f * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other;
			NavitStreetFontLetterWidth = (int) ((float) NavitStreetFontLetterWidth_base * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other);
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
			compass_b = null;
		}
		compass_b = Bitmap.createBitmap(compass_w, compass_h, Bitmap.Config.ARGB_8888);
		compass_c = new Canvas(compass_b);

		if (ddtt_b != null)
		{
			ddtt_b.recycle();
			ddtt_b = null;
		}
		ddtt_b = Bitmap.createBitmap(ddtt_w, ddtt_h, Bitmap.Config.ARGB_8888);
		ddtt_c = new Canvas(ddtt_b);

		if (scale_b != null)
		{
			scale_b.recycle();
			scale_b = null;
		}
		scale_b = Bitmap.createBitmap(scale_w, scale_h, Bitmap.Config.ARGB_8888);
		scale_c = new Canvas(scale_b);

		if (dttarget_b != null)
		{
			dttarget_b.recycle();
			dttarget_b = null;
		}
		dttarget_b = Bitmap.createBitmap(dttarget_w, dttarget_h, Bitmap.Config.ARGB_8888);
		dttarget_c = new Canvas(dttarget_b);

		if (eta_b != null)
		{
			eta_b.recycle();
			eta_b = null;
		}
		eta_b = Bitmap.createBitmap(eta_w, eta_h, Bitmap.Config.ARGB_8888);
		eta_c = new Canvas(eta_b);

		if (nt_b != null)
		{
			nt_b.recycle();
			nt_b = null;
		}
		nt_b = Bitmap.createBitmap(nt_w, nt_h, Bitmap.Config.ARGB_8888);
		nt_c = new Canvas(nt_b);

		boolean need_create_bitmap = true;
		if (rest_osd_b != null)
		{
			System.out.println("OSD:have bitmap 001 new:" + w + " x " + h + " old:" + rest_osd_b.getWidth() + " x " + rest_osd_b.getHeight());

			if (rest_osd_b.getHeight() >= h)
			{
				if (rest_osd_b.getWidth() >= w)
				{
					need_create_bitmap = false;
				}
			}
		}

		if (need_create_bitmap)
		{
			System.out.println("OSD:Creating bitmap 001");

			if (rest_osd_b != null)
			{
				rest_osd_b.recycle();
				rest_osd_b = null;
				rest_osd_c = null;
				System.gc();
			}

			try
			{
				rest_osd_b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				rest_osd_c = new Canvas(rest_osd_b);
			}
			catch (OutOfMemoryError e)
			{
				int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				System.out.println("OOM:OSD:001");
				String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);
				System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
				System.gc();
				System.gc();
				usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);
				// try again
				rest_osd_b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				rest_osd_c = new Canvas(rest_osd_b);
			}
		}
		else
		{
			System.out.println("OSD:Reusing bitmap 001");
		}

		need_create_bitmap = true;
		if (buffer_osd_b != null)
		{
			System.out.println("OSD:have bitmap 002 new:" + w + " x " + h + " old:" + buffer_osd_b.getWidth() + " x " + buffer_osd_b.getHeight());

			if (buffer_osd_b.getHeight() >= h)
			{
				if (buffer_osd_b.getWidth() >= w)
				{
					need_create_bitmap = false;
				}
			}
		}

		if (need_create_bitmap)
		{
			System.out.println("OSD:Creating bitmap 002");

			if (buffer_osd_b != null)
			{
				buffer_osd_b.recycle();
				buffer_osd_b = null;
				buffer_osd_c = null;
				System.gc();
			}

			try
			{
				buffer_osd_b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				buffer_osd_c = new Canvas(buffer_osd_b);
			}
			catch (OutOfMemoryError e)
			{
				int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				System.out.println("OOM:OSD:002");
				String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);
				System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
				System.gc();
				System.gc();
				usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);
				// try again
				buffer_osd_b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				buffer_osd_c = new Canvas(buffer_osd_b);
			}
		}
		else
		{
			System.out.println("OSD:Reusing bitmap 002");
		}

		//		if (buffer2_osd_b != null)
		//		{
		//			buffer2_osd_b.recycle();
		//			buffer2_osd_b = null;
		//		}
		//
		//		try
		//		{
		//			buffer2_osd_b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		//			buffer2_osd_c = new Canvas(buffer2_osd_b);
		//		}
		//		catch (OutOfMemoryError e)
		//		{
		//			int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
		//			System.out.println("OOM:OSD:003");
		//			String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
		//			System.out.println("" + usedMegsString);
		//			System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
		//			System.gc();
		//			usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
		//			usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
		//			System.out.println("" + usedMegsString);
		//			// try again
		//			buffer2_osd_b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		//			buffer2_osd_c = new Canvas(buffer2_osd_b);
		//		}

		// ----------------- make all bitmaps seethru ---------------------		
		// rest_osd_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		NavitGraphics.NavitAOSDJava_.rest_osd_b.eraseColor(Color.TRANSPARENT);

		compass_b.eraseColor(Color.TRANSPARENT);
		ddtt_b.eraseColor(Color.TRANSPARENT);
		NavitGraphics.NavitAOSDJava_.scale_b.eraseColor(Color.TRANSPARENT);
		dttarget_b.eraseColor(Color.TRANSPARENT);
		eta_b.eraseColor(Color.TRANSPARENT);
		nt_b.eraseColor(Color.TRANSPARENT);
	}

	public static class drawOSDThread extends Thread
	{
		private Boolean running = true;
		private boolean start_work = false;
		private boolean need_redraw_osd = false;
		private boolean need_redraw_overlay = false;

		drawOSDThread()
		{
			this.running = true;
		}

		public void run()
		{

			while (running)
			{
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
				}

				if (this.start_work)
				{
					work(need_redraw_osd, need_redraw_overlay);
					this.start_work = false;
					this.need_redraw_osd = false;
					this.need_redraw_overlay = false;
				}
			}
		}

		synchronized void work(boolean redraw_osd, boolean redraw_overlay)
		{
			// draw_real();

			if (redraw_osd)
			{
				//NavitGraphics.NavitAOSDJava_.postInvalidate();
				NavitGraphics.OSD_new.postInvalidate();
			}

			if (redraw_overlay)
			{
				NavitGraphics.NavitAOverlay_s.postInvalidate();
			}
		}

		public void buzz(boolean redraw_osd, boolean redraw_overlay)
		{
			this.start_work = true;
			this.need_redraw_osd = redraw_osd;
			this.need_redraw_overlay = redraw_overlay;

			this.interrupt();
		}
	}

	public static void draw_real_wrapper(boolean redraw_osd, boolean redraw_overlay)
	{
		//System.out.println("OSD:draw_real_wrapper:begin");
		Navit.draw_osd_thread.buzz(redraw_osd, redraw_overlay);
		//++ draw_real();
		//System.out.println("OSD:draw_real_wrapper:end");
	}

	synchronized public static void draw_real()
	{

	}

	synchronized public static void draw_realXX()
	{
		//System.out.println("OSD:004:begin");

		//		if (in_draw_real == true)
		//		{
		//			System.out.println("OSD:parallel001");
		//			return;
		//		}

		//in_draw_real = true;

		try
		{
			// compass_c.drawColor(Color.LTGRAY);
			// ddtt_c.drawColor(Color.CYAN);

			dest_valid = NavitGraphics.CallbackDestinationValid2();

			did_draw_circle = false;
			if (!NavitGraphics.MAP_DISPLAY_OFF)
			{
				if ((Navit.OSD_compass.angle_north_valid) || ((Navit.OSD_compass.angle_target_valid) && (dest_valid > 0)))
				{
					// compass_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					paint.setColor(OSD_element_bg_001_compass);
					compass_c.drawRoundRect(new RectF(0 + 3, 0 + 3, compass_w - 3, compass_h - 3), 18, 18, paint);
				}

				if (Navit.OSD_compass.angle_north_valid)
				{
					paint.setColor(Color.BLACK);
					paint.setStyle(Paint.Style.STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					compass_c.drawCircle(compass_center_x, compass_center_y, compass_radius, paint);
					did_draw_circle = true;
					end_x = (int) ((float) Math.sin((float) Math.toRadians(Navit.OSD_compass.angle_north)) * compass_radius);
					end_y = (int) ((float) Math.cos((float) Math.toRadians(Navit.OSD_compass.angle_north)) * compass_radius);
					// System.out.println("x " + end_x + " y " + end_y);
					paint.setStrokeWidth(2);
					if (Navit.metrics.densityDpi >= 320)
					{
						paint.setStrokeWidth(4);
					}
					compass_c.drawLine(compass_center_x - end_x, compass_center_y + end_y, compass_center_x, compass_center_y, paint);
					paint.setColor(Color.RED);
					paint.setStrokeWidth(4);
					if (Navit.metrics.densityDpi >= 320)
					{
						paint.setStrokeWidth(8);
					}
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
					end_x = (int) ((float) Math.sin((float) Math.toRadians(Navit.OSD_compass.angle_target)) * compass_radius);
					end_y = (int) ((float) Math.cos((float) Math.toRadians(Navit.OSD_compass.angle_target)) * compass_radius);
					// System.out.println("x " + end_x + " y " + end_y);
					paint.setStrokeWidth(2);
					if (Navit.metrics.densityDpi >= 320)
					{
						paint.setStrokeWidth(4);
					}
					paint.setColor(Color.GREEN);
					compass_c.drawLine(compass_center_x, compass_center_y, compass_center_x + end_x, compass_center_y - end_y, paint);
				}
			}

			if ((Navit.OSD_compass.direct_distance_to_target_valid) && (dest_valid > 0))
			{
				paint.setColor(OSD_element_bg_001);
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(2);
				paint.setAntiAlias(true);
				ddtt_c.drawRoundRect(new RectF(0 + 2, 0 + 2, ddtt_w - 2, ddtt_h - 2), 10, 10, paint);

				paint.setColor(OSD_element_text_shadow_001);
				paint.setStrokeWidth(OSD_element_text_shadow_width);
				paint.setStyle(Paint.Style.STROKE);
				paint.setTextSize(ddtt_font_size);
				paint.setAntiAlias(true);
				ddtt_c.drawText(Navit.OSD_compass.direct_distance_to_target, ddtt_text_start_x, ddtt_text_start_y, paint);

				paint.setColor(OSD_element_text_001);
				paint.setStrokeWidth(3);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(ddtt_font_size);
				paint.setAntiAlias(true);
				ddtt_c.drawText(Navit.OSD_compass.direct_distance_to_target, ddtt_text_start_x, ddtt_text_start_y, paint);
			}
			if ((Navit.OSD_route_001.arriving_time_valid) && (dest_valid > 0))
			{
				paint.setColor(OSD_element_bg_001);
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(2);
				paint.setAntiAlias(true);
				eta_c.drawRoundRect(new RectF(0 + 2, 0 + 2, eta_w - 2, eta_h - 2), 10, 10, paint);

				paint.setColor(OSD_element_text_shadow_001);
				paint.setStrokeWidth(OSD_element_text_shadow_width);
				paint.setStyle(Paint.Style.STROKE);
				paint.setTextSize(eta_font_size);
				paint.setAntiAlias(true);
				eta_c.drawText(Navit.OSD_route_001.arriving_time, eta_text_start_x, eta_text_start_y, paint);

				paint.setColor(OSD_element_text_001);
				paint.setStrokeWidth(3);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(eta_font_size);
				paint.setAntiAlias(true);
				eta_c.drawText(Navit.OSD_route_001.arriving_time, eta_text_start_x, eta_text_start_y, paint);
			}
			if ((Navit.OSD_route_001.driving_distance_to_target_valid) && (dest_valid > 0))
			{
				paint.setColor(OSD_element_bg_001);
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(2);
				paint.setAntiAlias(true);
				dttarget_c.drawRoundRect(new RectF(0 + 2, 0 + 2, dttarget_w - 2, dttarget_h - 2), 10, 10, paint);

				paint.setColor(OSD_element_text_shadow_001);
				paint.setStrokeWidth(OSD_element_text_shadow_width);
				paint.setStyle(Paint.Style.STROKE);
				paint.setTextSize(dttarget_font_size);
				paint.setAntiAlias(true);
				dttarget_c.drawText(Navit.OSD_route_001.driving_distance_to_target, dttarget_text_start_x, dttarget_text_start_y, paint);

				paint.setColor(OSD_element_text_001);
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
					nt_c.drawRoundRect(new RectF(0 + 2, 0 + 2, nt_w - 2, nt_h - 2), 10, 10, paint);

					paint.setColor(Color.WHITE);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(nt_font_size);
					paint.setAntiAlias(true);
					nt_c.drawText(Navit.OSD_nextturn.nextturn_distance, nt_text_start_x, nt_text_start_y, paint);
				}
				else
				{
					paint.setColor(OSD_element_bg_001);
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					nt_c.drawRoundRect(new RectF(0 + 2, 0 + 2, nt_w - 2, nt_h - 2), 10, 10, paint);

					paint.setColor(OSD_element_text_shadow_001);
					paint.setStrokeWidth(OSD_element_text_shadow_width);
					paint.setStyle(Paint.Style.STROKE);
					paint.setTextSize(nt_font_size);
					paint.setAntiAlias(true);
					nt_c.drawText(Navit.OSD_nextturn.nextturn_distance, nt_text_start_x, nt_text_start_y, paint);

					paint.setColor(OSD_element_text_001);
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
					NavitGraphics.NavitAOSDJava_.scale_c.drawLine(scale_line_start_x, scale_line_middle_y, scale_line_start_x + Navit.OSD_scale.var, scale_line_middle_y, paint);
					// method b
					// scale_c.drawLine(scale_line_start_x, scale_line_middle_y, scale_line_end_x, scale_line_middle_y, paint);
					NavitGraphics.NavitAOSDJava_.scale_c.drawLine(scale_line_start_x, scale_line_start_y, scale_line_start_x, scale_line_end_y, paint);
					NavitGraphics.NavitAOSDJava_.scale_c.drawLine(scale_line_start_x + Navit.OSD_scale.var, scale_line_start_y, scale_line_start_x + Navit.OSD_scale.var, scale_line_end_y, paint);

					paint.setColor(OSD_element_text_shadow_001);
					paint.setStrokeWidth(OSD_element_text_shadow_width);
					paint.setStyle(Paint.Style.STROKE);
					paint.setTextSize(scale_font_size);
					NavitGraphics.NavitAOSDJava_.scale_c.drawText(Navit.OSD_scale.scale_text, scale_text_start_x, scale_text_start_y, paint);

					paint.setStyle(Paint.Style.FILL);
					paint.setColor(OSD_element_text_001);
					paint.setStrokeWidth(3);
					paint.setTextSize(scale_font_size);
					NavitGraphics.NavitAOSDJava_.scale_c.drawText(Navit.OSD_scale.scale_text, scale_text_start_x, scale_text_start_y, paint);
				}
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
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
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRoundRect(new RectF(nextt_str_ltxB, nextt_str_ltyB, nextt_str_ltxB + nextt_str_wB, nextt_str_ltyB + nextt_str_hB), 10, 10, paint);
					paint.setColor(Color.WHITE);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					if ((Navit.OSD_nextturn.nextturn_streetname.length() + 1 + Navit.OSD_nextturn.nextturn_streetname_systematic.length()) > (nextt_str_wB / NavitStreetFontLetterWidth))
					{
						if ((Navit.OSD_nextturn.nextturn_streetname.length() + 1 + Navit.OSD_nextturn.nextturn_streetname_systematic.length()) > (2 * (nextt_str_wB / NavitStreetFontLetterWidth)))
						{
							paint.setTextSize((int) (nextt_str_font_size * 0.40));
						}
						else
						{
							paint.setTextSize((int) (nextt_str_font_size * 0.70));
						}
					}
					else
					{
						paint.setTextSize(nextt_str_font_size);
					}
					paint.setAntiAlias(true);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawText(Navit.OSD_nextturn.nextturn_streetname_systematic + " " + Navit.OSD_nextturn.nextturn_streetname, nextt_str_ltxB + nextt_str_start_x, nextt_str_ltyB + nextt_str_start_y, paint);
				}
				else
				{
					paint.setColor(OSD_element_bg_001);
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRoundRect(new RectF(nextt_str_ltx, nextt_str_lty, nextt_str_ltx + nextt_str_w, nextt_str_lty + nextt_str_h), 10, 10, paint);
					paint.setColor(Color.BLACK);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					if ((Navit.OSD_nextturn.nextturn_streetname.length() + 1 + Navit.OSD_nextturn.nextturn_streetname_systematic.length()) > (nextt_str_wB / NavitStreetFontLetterWidth))
					{
						if ((Navit.OSD_nextturn.nextturn_streetname.length() + 1 + Navit.OSD_nextturn.nextturn_streetname_systematic.length()) > (2 * (nextt_str_wB / NavitStreetFontLetterWidth)))
						{
							paint.setTextSize((int) (nextt_str_font_size * 0.40));
						}
						else
						{
							paint.setTextSize((int) (nextt_str_font_size * 0.70));
						}
					}
					else
					{
						paint.setTextSize(nextt_str_font_size);
					}

					paint.setColor(OSD_element_text_shadow_001);
					paint.setStrokeWidth(OSD_element_text_shadow_width);
					paint.setStyle(Paint.Style.STROKE);
					paint.setAntiAlias(true);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawText(Navit.OSD_nextturn.nextturn_streetname_systematic + " " + Navit.OSD_nextturn.nextturn_streetname, nextt_str_ltx + nextt_str_start_x, nextt_str_lty + nextt_str_start_y, paint);

					paint.setColor(OSD_element_text_001);
					paint.setStrokeWidth(3);
					paint.setStyle(Paint.Style.FILL);
					paint.setAntiAlias(true);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawText(Navit.OSD_nextturn.nextturn_streetname_systematic + " " + Navit.OSD_nextturn.nextturn_streetname, nextt_str_ltx + nextt_str_start_x, nextt_str_lty + nextt_str_start_y, paint);
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
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new RectF(nextt_str_ltxB, nextt_str_ltyB, nextt_str_ltxB + nextt_str_wB, nextt_str_ltyB + nextt_str_hB), paint);
				}
				else
				{
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new RectF(nextt_str_ltx, nextt_str_lty, nextt_str_ltx + nextt_str_w, nextt_str_lty + nextt_str_h), paint);
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
					dst = new RectF(nextt_lt_xB, nextt_lt_yB, nextt_lt_xB + nextt_wB, nextt_lt_yB + nextt_hB);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRoundRect(new RectF(nextt_lt_xB, nextt_lt_yB, nextt_lt_xB + nextt_wB, nextt_lt_yB + nextt_hB), 10, 10, paint);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(Navit.OSD_nextturn.nextturn_image, null, dst, null);
				}
				else
				{
					paint.setColor(OSD_element_bg_001);
					paint.setStyle(Paint.Style.FILL_AND_STROKE);
					paint.setStrokeWidth(2);
					paint.setAntiAlias(true);
					dst = new RectF(nextt_lt_x, nextt_lt_y, nextt_lt_x + nextt_w, nextt_lt_y + nextt_h);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRoundRect(new RectF(nextt_lt_x, nextt_lt_y, nextt_lt_x + nextt_w, nextt_lt_y + nextt_h), 10, 10, paint);
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(Navit.OSD_nextturn.nextturn_image, null, dst, null);
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
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new RectF(nextt_lt_xB, nextt_lt_yB, nextt_lt_xB + nextt_wB, nextt_lt_yB + nextt_hB), paint);
				}
				else
				{
					NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new RectF(nextt_lt_x, nextt_lt_y, nextt_lt_x + nextt_w, nextt_lt_y + nextt_h), paint);
				}
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
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
				NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y, sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);

				if (Navit.sats > sat_status_max_sats)
				{
					sat_status_max_sats = Navit.sats;
				}

				// fill inactive sats
				paint.setColor(Color.YELLOW);
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(0);
				paint.setAntiAlias(true);
				NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y + sat_status_lt_h - (sat_status_lt_h / sat_status_max_sats * Navit.sats), sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);

				// fill active sats
				paint.setColor(Color.GREEN);
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(0);
				paint.setAntiAlias(true);
				NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y + sat_status_lt_h - (sat_status_lt_h / sat_status_max_sats * Navit.satsInFix), sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);

				// black rect around it all
				paint.setColor(Color.BLACK);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(1);
				paint.setAntiAlias(true);
				NavitGraphics.NavitAOSDJava_.rest_osd_c.drawRect(new Rect(sat_status_lt_x, sat_status_lt_y, sat_status_lt_x + sat_status_lt_w, sat_status_lt_y + sat_status_lt_h), paint);
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}

		//if (NavitGraphics.MAP_DISPLAY_OFF)
		//{
		//	one_shot = true;
		//}
		// allow_drawing = true;

		onDraw_part2();

		//in_draw_real = false;

		//System.out.println("OSD:004:end");
	}

	public void onDraw(Canvas c)
	{
		// System.out.println("XYZ:NavitOSDJava -> onDraw");
		// NOTHING HERE anymore !!!!!!
	}

	public void onDrawXX(Canvas c)
	{

		//System.out.println("draw:isHardwareAccelerated=" + c.isHardwareAccelerated());

		try
		{
			//			synchronized (sync_dummy_001)
			//			{
			allow_drawing = false;
			c.drawBitmap(buffer_osd_b, 0, 0, null);
			allow_drawing = true;
			//			}

			//++boolean got_it = take_synchro();
			//System.out.println("OO:002:synchro_obj=" + synchro_obj);
			//++while (got_it == false)
			//++{
			//++	//System.out.println("do_draw:blocking wait ...");
			//++	Thread.sleep(5);
			//++	got_it = take_synchro();
			//++}
			//System.out.println("OO:002:start");
			// c.drawBitmap(NavitGraphics.NavitAOSDJava_.buffer_osd_b, 0, 0, null);
			//c.drawBitmap(rest_osd_b, 0, 0, null);
			//System.out.println("OO:002:end");
			//++if (got_it)
			//++{
			//++	release_synchro();
			//++}

			if (!Navit.PREF_follow_gps)
			{
				if (!NavitGraphics.MAP_DISPLAY_OFF)
				{
					// show cross hair
					paint_crosshair.setColor(Color.DKGRAY);
					paint_crosshair.setStyle(Paint.Style.STROKE);
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
		catch (Exception e)
		{
			// e.printStackTrace();
		}

		// System.out.println("OSD:002:draw:end");
	}

	public static boolean take_synchro()
	{
		synchronized (synchro_obj)
		{
			if (synchro_obj < 1)
			{
				synchro_obj++;
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public static boolean release_synchro()
	{
		synchronized (synchro_obj)
		{
			if (synchro_obj > 0)
			{
				synchro_obj = 0;
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public static void onDraw_part2()
	{
		//if (allow_drawing == false)
		//{
		//	return;
		//}

		//synchronized (sync_dummy_001)
		//{
		//System.out.println("draw real 2");

		try
		{
			// NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(NavitGraphics.NavitAOSDJava_.rest_osd_b, 0, 0, null);
			NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(compass_b, compass_lt_x, compass_lt_y, null);
			NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(ddtt_b, ddtt_lt_x, ddtt_lt_y, null);
			NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(dttarget_b, dttarget_lt_x, dttarget_lt_y, null);
			NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(eta_b, eta_lt_x, eta_lt_y, null);

			if (NavitGraphics.MAP_DISPLAY_OFF)
			{
				NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(nt_b, nt_lt_xB, nt_lt_yB, null);
			}
			else
			{
				NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(nt_b, nt_lt_x, nt_lt_y, null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (show_scale)
		{
			try
			{
				NavitGraphics.NavitAOSDJava_.rest_osd_c.drawBitmap(NavitGraphics.NavitAOSDJava_.scale_b, scale_lt_x, scale_lt_y, null);
			}
			catch (Exception e)
			{
				//e.printStackTrace();
			}
		}

		//++try
		//++{
		// Thread.sleep(5);
		//++boolean got_it = take_synchro();
		// System.out.println("OO:001:synchro_obj=" + synchro_obj);
		//++if (got_it)
		//++{
		//System.out.println("OO:001:start");

		// ------------------ CLEAR ---------------
		//NavitGraphics.NavitAOSDJava_.buffer_osd_b.eraseColor(Color.TRANSPARENT);
		// just clear where we really need it
		// ++NavitGraphics.NavitAOSDJava_.buffer_osd_c.clipRect(compass_lt_x, compass_lt_y, compass_lt_x + compass_w + 1, compass_lt_y + compass_h + 1, Region.Op.REPLACE);
		// NavitGraphics.NavitAOSDJava_.buffer_osd_c.clipRect(scale_lt_x, scale_lt_y, scale_lt_x + scale_w + 1, scale_lt_y + scale_h + 1, Region.Op.UNION);
		//
		// ++NavitGraphics.NavitAOSDJava_.buffer_osd_c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		// **TEST** // NavitGraphics.NavitAOSDJava_.buffer_osd_c.drawColor(Color.BLUE, PorterDuff.Mode.XOR);
		// ------------------ CLEAR ---------------

		// ------------------ RESET ---------------
		// ++NavitGraphics.NavitAOSDJava_.buffer_osd_c.clipRect(0, 0, NavitOSDJava.mCanvasWidth, NavitOSDJava.mCanvasHeight, Region.Op.REPLACE);
		// ------------------ RESET ---------------

		if (allow_drawing == false)
		{
			try
			{
				Thread.sleep(5);
			}
			catch (Exception e)
			{

			}
		}
		// ------------------ DRAW  ---------------
		try
		{
			//if (!NavitGraphics.NavitAOSDJava_.rest_osd_b.isRecycled())
			//{
			NavitGraphics.NavitAOSDJava_.buffer_osd_c.drawBitmap(NavitGraphics.NavitAOSDJava_.rest_osd_b, 0, 0, null);
			//}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// ------------------ DRAW  ---------------

		//System.out.println("OO:001:end");
		//++	release_synchro();
		//++}
		//++}
		//++catch (Exception e)
		//++{
		//++	//e.printStackTrace();
		//++}

		//if (NavitGraphics.MAP_DISPLAY_OFF)
		//{
		//	if (one_shot)
		//	{
		//		//System.out.println("one shot");
		//		one_shot = false;
		//		//this.postInvalidate();
		//	}
		//}
		//}
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
				//System.out.println("OSDJava:handleMessage:1");
				//System.out.println("invalidate 007");
				// ** // postInvalidate();
				break;
			}
		}
	};

	//	@Override
	//	protected void onLayout(boolean changed, int l, int t, int r, int b)
	//	{
	//	}
}
