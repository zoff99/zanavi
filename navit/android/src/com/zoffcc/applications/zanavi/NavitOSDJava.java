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
import android.graphics.Paint;
import android.graphics.RectF;
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

	// public static Handler progress_handler_ = new Handler();

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
	//static int nextt_str_w = 0;
	static int nextt_str_h = 0;
	static int nextt_str_wB = 0;
	static int nextt_str_hB = 0;
	// static int nextt_str_font_size = 0;
	// static int nextt_str_start_x = 0;
	// static int nextt_str_start_y = 0;

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
		// System.out.println("OSD:draw_real_wrapper:begin");
		Navit.draw_osd_thread.buzz(redraw_osd, redraw_overlay);
		//++ draw_real();
		// System.out.println("OSD:draw_real_wrapper:end");
	}

	synchronized public static void draw_real()
	{

	}

	public void onDraw(Canvas c)
	{
		// System.out.println("XYZ:NavitOSDJava -> onDraw");
		// NOTHING HERE anymore !!!!!!
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
