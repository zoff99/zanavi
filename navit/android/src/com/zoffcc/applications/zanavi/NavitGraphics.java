/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2013 Zoff <zoff@zoff.cc>
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

/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2008 Navit Team
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zoffcc.applications.zanavi.Navit.Navit_Address_Result_Struct;
import com.zoffcc.applications.zanavi.NavitAndroidOverlay.NavitAndroidOverlayBubble;

public class NavitGraphics
{
	private int parent_num;
	//private ArrayList overlays = new ArrayList();

	static Canvas draw_canvas_s;
	static Bitmap draw_bitmap_s;
	static Canvas draw_canvas_screen_s;
	static Bitmap draw_bitmap_screen_s;
	static Canvas draw_canvas_screen_s2;
	static Bitmap draw_bitmap_screen_s2;
	//static SurfaceView2 view_s;
	static View view_s;
	static int dl_thread_cur = 0;
	static int dl_thread_max = 1;
	static DrawLinesThread[] dl_thread = new DrawLinesThread[dl_thread_max];
	static Boolean draw_map_one_shot = false;
	static Boolean draw_reset_factors = false;
	static Boolean Global_Map_in_onDraw = false;

	static Boolean synch_drawing_for_map = false;
	static ZANaviLinearLayout OSD_new = null;

	// DPI ---------------------------------------------
	// DPI ---------------------------------------------
	// DPI ---------------------------------------------
	static int Global_want_dpi = 210;
	static int Global_want_dpi_other = 210;
	static float Global_dpi_factor = 1; // will be calculated correctly later
	static int Global_Scaled_DPI_normal = 240; // we will scale the whole screen to 240 dpi on high dpi devices
	// DPI ---------------------------------------------
	// DPI ---------------------------------------------
	// DPI ---------------------------------------------

	static Camera camera = new Camera();
	static Matrix cam_m = new Matrix();
	static Matrix cam_m_vehicle = new Matrix();
	static float strech_factor_3d_map = 2.2f;
	static int rotate_3d_map_angle = 61;
	static float h_scaled = 20;

	private Canvas draw_canvas;
	private Bitmap draw_bitmap;
	private Canvas draw_canvas_screen;
	private Bitmap draw_bitmap_screen;
	private Canvas draw_canvas_screen2;
	private Bitmap draw_bitmap_screen2;

	public final int map_bg_color = Color.parseColor("#FEF9EE");

	public final static DashPathEffect dashed_map_lines__high = new DashPathEffect(new float[] { 4, 2 }, 1);
	public final static DashPathEffect dashed_map_lines__low = new DashPathEffect(new float[] { 15, 11 }, 1);
	public final static DashPathEffect dashed_map_lines__no_dash = null;

	public final static DashPathEffect h001 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect l001 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect h002 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect l002 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect h003 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect l003 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect h004 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect l004 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect h005 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect l005 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect h006 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect l006 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect h007 = new DashPathEffect(new float[] { 6, 3 }, 1);
	public final static DashPathEffect l007 = new DashPathEffect(new float[] { 7, 2 }, 1);

	public static Paint paint_for_map_display = new Paint();
	public static Paint paint_sky_day = new Paint();
	public static Paint paint_sky_night = new Paint();
	public static Paint paint_sky_night_stars = new Paint();
	public static Paint paint_sky_twilight1 = new Paint();
	public static Paint paint_sky_twilight2 = new Paint();
	public static Paint paint_sky_twilight3 = new Paint();

	public static final int max_stars = 7;
	public static float[] stars_x = new float[max_stars + 1];
	public static float[] stars_y = new float[max_stars + 1];
	public static int[] stars_size = new int[max_stars + 1];

	public final static DashPathEffect[] dashes__low = { null, l001, l002, l003, l004, l005, l006, l007 };
	public final static DashPathEffect[] dashes__high = { null, h001, h002, h003, h004, h005, h006, h007 };

	public static int navit_route_status = 0;

	public final float BIGMAP_FACTOR = (10465f / 2f) * 0.8f;
	Paint paint_maptile = new Paint();
	Matrix matrix_maptile = new Matrix();

	static Paint strokeTextPaint = new Paint();
	static double s_factor = 1;
	static int s_strokTextSize = 8;
	static int s_strokTextSize_min = 3;

	public static long last_paint_OSD = 0;

	public static Boolean wait_for_redraw_map = false;

	public static Boolean MAP_DISPLAY_OFF = false;

	public static int mCanvasWidth = 1;
	public static int mCanvasHeight = 1;

	public static final Boolean DEBUG_TOUCH = false;
	public static Boolean ZOOM_MODE_ACTIVE = false;
	public static float ZOOM_MODE_SCALE = 1.0f;
	public static Boolean DRAG_MODE_ACTIVE = false;

	public static final Boolean DEBUG_SMOOTH_DRIVING = false;
	private static long smooth_driving_ts001 = 0L;
	private static long smooth_driving_ts002 = 0L;
	private static long smooth_driving_ts002a = 0L;
	private static long smooth_driving_ts003 = 0L;
	private static long smooth_driving_tmptmp = 0L;

	public static float Global_Map_Zoomfactor = 1.0f;
	public static float Global_Map_Rotationangle = 0;
	public static float Global_Map_TransX = 0;
	public static float Global_Map_TransY = 0;
	public static Boolean Global_SmoothDrawing_stop = false;
	public static Boolean Global_onTouch_fingerdown = false;

	public static float draw_factor = 1.0f;

	public static String debug_line_1 = "";
	public static String debug_line_2 = "";
	public static String debug_line_3 = "";
	public static String debug_line_4 = "";

	public static final int DRAW_ONEWAY_ARROWS_AT_ORDER = 13;
	public static final int DRAW_DETAIL_DASHES_AT_ORDER = 13;
	public static final int DRAW_MORE_DETAILS_AT_ORDER = 9;
	public static final int DRAW_MORE_DETAILS_TUNNEL_BRIDGES_AT_ORDER = 8;

	public static long[] OverlayDrawThread_cancel_drawing_timeout__options = { 300L, 900L, 2100L, 20000L }; // 900L normal, 300L short, 2100L long
	public static int[] OverlayDrawThread_cancel_thread_sleep_time__options = { 100, 200, 400, 400 };
	public static long[] OverlayDrawThread_cancel_thread_timeout__options = { 3000L, 3000L, 3000L, 22000L };

	public static long OverlayDrawThread_cancel_drawing_timeout = OverlayDrawThread_cancel_drawing_timeout__options[1];
	public static int OverlayDrawThread_cancel_thread_sleep_time = OverlayDrawThread_cancel_thread_sleep_time__options[1];
	public static long OverlayDrawThread_cancel_thread_timeout = OverlayDrawThread_cancel_thread_timeout__options[1];

	int loc_dot_x = 0;
	int loc_dot_y = 0;
	int loc_dot_x2 = 0;
	int loc_dot_y2 = 0;
	int loc_1_x = 0;
	int loc_1_y = 0;
	int loc_2_x = 0;
	int loc_2_y = 0;
	Boolean loc_12_valid = false;
	Boolean loc_dot_valid = false;
	int gr_type = 0; // 0 -> dummy, 1 -> map

	int bitmap_w;
	int bitmap_h;
	int pos_x;
	int pos_y;
	//
	int vehicle_speed = 0;
	int vehicle_pos_x = 0;
	int vehicle_pos_y = 0;
	int vehicle_direction = 0;
	int vehicle_pos_x_delta = 0;
	int vehicle_pos_y_delta = 0;
	int vehicle_direction_delta = 0;
	int vehicle_zoom_delta = 0;
	// drag pos
	public static int d_pos_x_old = -1;
	public static int d_pos_y_old = -1;
	// drag post
	int pos_wraparound;
	// int overlay_disabled;
	float trackball_x, trackball_y;
	View view;
	RelativeLayout relativelayout;
	// --obsolote --- // NavitCamera camera;
	Activity activity;

	private Bitmap bigmap_bitmap_temp = null;
	private Matrix matrix_oneway_arrows = null;
	private float wsave_003 = 0f;
	private float wsave_004 = 0f;
	private Boolean b_paint_antialias = true;
	private Path b_paint_path = new Path();

	public static Boolean in_map = true; // always in map mode, now

	// for menu key
	private static long time_for_long_press = 300L;
	private static long interval_for_long_press = 200L;

	// for touch screen
	private long last_touch_on_screen = 0L;
	private static long long_press_on_screen_interval = 1000L;
	private static float long_press_on_screen_max_distance = 8f;

	// Overlay View for Android
	//
	// here you can draw all the nice things you want
	// and get touch events for it (without touching C-code)
	public NavitAndroidOverlay NavitAOverlay = null;
	public static NavitAndroidOverlay NavitAOverlay_s = null;
	private NavitOSDJava NavitAOSDJava = null;
	public static NavitOSDJava NavitAOSDJava_ = null;
	private TextView NavitMsgTv = null;
	public static TextView NavitMsgTv_ = null;
	public ZANaviBusySpinner busyspinner = null;
	public static ZANaviBusySpinner busyspinner_ = null;
	public ZANaviBusyText busyspinnertext = null;
	public static ZANaviBusyText busyspinnertext_ = null;

	public static EmulatedMenuView emu_menu_view;

	PointF touch_now_center = new PointF(0, 0);

	private TextView NavitMsgTv2 = null;
	public static TextView NavitMsgTv2_ = null;

	public static NavitGlobalMap NavitGlobalMap_ = null;

	public void SetCamera(int use_camera)
	{
		// ------ obsolete

		/*
		 * if (use_camera != 0 && camera == null)
		 * {
		 * // activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		 * camera = new NavitCamera(activity);
		 * relativelayout.addView(camera);
		 * relativelayout.bringChildToFront(view);
		 * }
		 */
	}

	public static SensorThread touch_sensor_thread = null;

	private class SensorThread extends Thread
	{
		private Boolean running;
		private long last_down_action = 0L;
		private Boolean is_still_pressing;
		//private View v = null;
		//private NavitAndroidOverlay n_overlay = null;
		private float prev_x;
		private float prev_y;
		private float x;
		private float y;

		SensorThread(long last_touch, float x, float y)
		{
			this.prev_x = x;
			this.prev_y = y;
			this.x = x;
			this.y = y;
			this.running = true;
			//this.n_overlay = n_ov;
			//this.v = v;
			this.is_still_pressing = true;
			last_down_action = System.currentTimeMillis();
			if (DEBUG_TOUCH) Log.e("NavitGraphics", "SensorThread created last_down_action=" + last_down_action);
		}

		public void down()
		{
			this.is_still_pressing = true;
		}

		//		public void up()
		//		{
		//			this.is_still_pressing=false;
		//		}

		public void stop_me()
		{
			this.running = false;
		}

		public void run()
		{
			if (DEBUG_TOUCH) Log.e("NavitGraphics", "SensorThread started");
			while (this.running)
			{
				if (DEBUG_TOUCH) Log.e("NavitGraphics", "while loop:last_down_action=" + this.last_down_action + " currentTimeMillis=" + System.currentTimeMillis() + " long_press_on_screen_interval=" + long_press_on_screen_interval);
				if ((System.currentTimeMillis() - this.last_down_action) > long_press_on_screen_interval)
				{
					// ok, we have counted a long press on screen
					// do stuff and then stop this thread
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "SensorThread: LONG PRESS");
					try
					{
						if (this.running)
						{
							if (!NavitAndroidOverlay.confirmed_bubble)
							{
								//Log.e("NavitGraphics", "do_longpress_action enter");
								NavitAndroidOverlayBubble b = new NavitAndroidOverlayBubble();
								b.x = (int) this.x;
								b.y = (int) this.y;
								NavitAOverlay.set_bubble(b);
								NavitAOverlay.show_bubble();
								copy_map_buffer();
								//System.out.println("invalidate 009");
								Navit.NG__map_main.view.postInvalidate();
								//SurfaceView2 vw = (SurfaceView2) Navit.NG__map_main.view;
								//vw.paint_me();
								//System.out.println("invalidate 010");
								NavitAOverlay.postInvalidate();
							}
						}
						// this is called!!! yes really!! via REFLECT (make it better, later)
						// find the class, to get the method "do_longpress_action"
						// and then call the method

						//						Class cls = this.v.getClass();
						//						//Log.e("NavitGraphics", "c=" + String.valueOf(cls));
						//						Class partypes[] = new Class[2];
						//						partypes[0] = Float.TYPE;
						//						partypes[1] = Float.TYPE;
						//						Method meth = cls.getMethod("do_longpress_action", partypes);
						//						View methobj = this.v;
						//						Object arglist[] = new Object[2];
						//						arglist[0] = new Float(this.x);
						//						arglist[1] = new Float(this.y);

						//if (running)
						//{
						//Object retobj = meth.invoke(methobj, arglist);
						//}
					}
					// catch (Throwable e)
					catch (Exception e)
					{
						System.err.println(e);
					}
					this.running = false;
				}
				else if (!this.is_still_pressing)
				{
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "SensorThread: stopped pressing");
					this.running = false;
				}
				else
				{
					// Log.e("NavitGraphics", "SensorThread running");
					try
					{
						Thread.sleep(50);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
			}
			if (DEBUG_TOUCH) Log.e("NavitGraphics", "SensorThread ended");
		}
	}

	// public static OverlayDrawThread overlay_draw_thread = null;

	public static class OverlayDrawThread extends Thread
	{
		private Boolean running = true;
		private Boolean redraw = false;
		private long start_timestamp = 0L;

		OverlayDrawThread()
		{
			// Log.e("NavitGraphics", "OverlayDrawThread created");
			start_timestamp = System.currentTimeMillis();
		}

		public void run()
		{
			this.running = true;
			this.redraw = true;

			// start_timestamp = System.currentTimeMillis();
			// Log.e("NavitGraphics", "OverlayDrawThread starting"+start_timestamp);

			while (this.running)
			{

				if (System.currentTimeMillis() > (start_timestamp + OverlayDrawThread_cancel_drawing_timeout))
				{
					// after xxx milliseconds of delay, stop drawing the map!
					// most likely the device is too slow, or there are too much items to draw
					try
					{
						//Log.e("NavitGraphics", "## stop map drawing x1: NOW ##" + System.currentTimeMillis());
						//***NavitGraphics.CallbackMessageChannel(50, "");
						//Message msg = new Message();
						//Bundle b = new Bundle();
						//b.putInt("Callback", 50);
						//msg.setData(b);
						//callback_handler.sendMessage(msg);
						//Log.e("NavitGraphics", "## stop map drawing x2: NOW ##" + System.currentTimeMillis());
						this.running = false;
						break;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				if (System.currentTimeMillis() > (start_timestamp + OverlayDrawThread_cancel_thread_timeout))
				{
					// just to be safe, stop after 5 seconds
					this.running = false;
				}

				//Log.e("NavitGraphics", "OverlayDrawThread running");
				if (this.redraw)
				{
					// Log.e("NavitGraphics", "OverlayDrawThread -> redraw");
					try
					{
						//NavitAOverlay_s.invalidate();
						Message msg = Navit.Navit_progress_h.obtainMessage();
						Bundle b = new Bundle();
						msg.what = 16;
						msg.setData(b);
						Navit.Navit_progress_h.sendMessage(msg);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					//this.redraw = false;
				}

				try
				{
					Thread.sleep(OverlayDrawThread_cancel_thread_sleep_time);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			//Log.e("NavitGraphics", "OverlayDrawThread finished");
		}

		public void redraw_overlay()
		{
			//Log.e("NavitGraphics", "OverlayDrawThread set redraw");
			this.redraw = true;
		}

		public void stop_me()
		{
			this.running = false;
		}

		public void stop_redraw()
		{
			this.redraw = false;
		}
	}

	public static void init_3d_mode()
	{
		// 3D modus -----------------
		NavitGraphics.cam_m.reset();
		NavitGraphics.camera.save();
		//NavitGraphics.camera.translate(0, 0, 40);
		NavitGraphics.camera.rotateX(rotate_3d_map_angle);
		NavitGraphics.camera.getMatrix(NavitGraphics.cam_m);
		NavitGraphics.camera.restore();
		// C-Code: (50 + offset) * height / 100 // offset = 30%
		//float y_point = (bitmap_h - (bitmap_h * 0.7f));
		float y_offset = 0; // 20
		float y_point = Navit.NG__map_main.bitmap_h * 0.7f;
		NavitGraphics.cam_m.preTranslate(-Navit.NG__map_main.bitmap_w / 2, -y_offset - y_point);
		NavitGraphics.cam_m.postTranslate(Navit.NG__map_main.bitmap_w / 2, y_offset + y_point);
		NavitGraphics.cam_m.postScale(strech_factor_3d_map, strech_factor_3d_map, Navit.NG__map_main.bitmap_w / 2, y_offset + y_point);
		//
		Matrix matrix_tmp = new Matrix();
		//RectF src_rect = new RectF(0, 0, Navit.NG__map_main.bitmap_w, Navit.NG__map_main.bitmap_h);
		//RectF dst_rect = new RectF(0, 0, Navit.NG__map_main.bitmap_w, Navit.NG__map_main.bitmap_h);
		float[] src = new float[8];
		float[] dst = new float[8];
		src[0] = 0;
		src[1] = 0;
		src[2] = Navit.NG__map_main.bitmap_w;
		src[3] = 0;
		src[4] = Navit.NG__map_main.bitmap_w;
		src[5] = Navit.NG__map_main.bitmap_h;
		src[6] = 0;
		src[7] = Navit.NG__map_main.bitmap_h;
		//
		float _3d_skew_factor_top = 0f;
		float _3d_skew_factor_bottom = 0.8f;
		dst[0] = 0 + ((float) (Navit.NG__map_main.bitmap_w) * _3d_skew_factor_top);
		dst[1] = 0;
		dst[2] = Navit.NG__map_main.bitmap_w - ((float) (Navit.NG__map_main.bitmap_w) * _3d_skew_factor_top);
		dst[3] = 0;
		dst[4] = Navit.NG__map_main.bitmap_w + ((float) (Navit.NG__map_main.bitmap_w) * _3d_skew_factor_bottom);
		dst[5] = Navit.NG__map_main.bitmap_h;
		dst[6] = 0f - ((float) (Navit.NG__map_main.bitmap_w) * _3d_skew_factor_bottom);
		dst[7] = Navit.NG__map_main.bitmap_h;
		//
		matrix_tmp.setPolyToPoly(src, 0, dst, 0, 4);
		//*boolean bb = false;
		//*bb = NavitGraphics.cam_m.setConcat(matrix_tmp, cam_m);
		//*System.out.println("matrix b=" + bb);
		//
		//NavitGraphics.cam_m.postTranslate(0, 50);
		//
		float[] pts = new float[2];
		pts[0] = Navit.NG__map_main.bitmap_w / 2; // x0
		pts[1] = 0; // y0
		cam_m.mapPoints(pts); // now transform the points with the 3d matrix
		//System.out.println("x1=" + pts[0] + " y1=" + pts[1]);
		// -- offset for horizon --
		NavitGraphics.h_scaled = pts[1] + 15; // y coord of upper border after transformation with matrix (plus extra 15 pixels that scrolling looks better)
		//
		//
		// --- vehicle matrix
		cam_m_vehicle.reset();
		NavitGraphics.camera.save();
		NavitGraphics.camera.rotateX(rotate_3d_map_angle);
		NavitGraphics.camera.getMatrix(NavitGraphics.cam_m_vehicle);
		NavitGraphics.camera.restore();
		NavitGraphics.cam_m_vehicle.preTranslate(-Navit.NG__map_main.bitmap_w / 2, -y_offset - y_point);
		NavitGraphics.cam_m_vehicle.postTranslate(Navit.NG__map_main.bitmap_w / 2, y_offset + y_point);
		NavitGraphics.cam_m_vehicle.postScale(strech_factor_3d_map, strech_factor_3d_map, Navit.NG__map_main.bitmap_w / 2, y_offset + y_point);
		//matrix_tmp.reset();
		//matrix_tmp.preScale(1 / strech_factor_3d_map, 1 / strech_factor_3d_map, Navit.NG__map_main.bitmap_w / 2, y_offset + y_point);
		//NavitGraphics.cam_m_vehicle.setConcat(matrix_tmp, NavitGraphics.cam_m_vehicle);
		// --- vehicle matrix
		//
		// 3D modus -----------------

	}

	public NavitGraphics(Activity activity, int parent, int x, int y, int w, int h, int alpha, int wraparound, int use_camera)
	{

		paint_maptile.setFilterBitmap(false);
		paint_maptile.setAntiAlias(false);
		paint_maptile.setDither(false);

		// shadow for text on map --------------
		s_factor = 1;
		if ((Navit.metrics.densityDpi >= 320) && (!Navit.PREF_shrink_on_high_dpi))
		{
			s_factor = (double) Navit.metrics.densityDpi / (double) NavitGraphics.Global_Scaled_DPI_normal;
			// s_factor = 1.4;
		}
		s_strokTextSize = (int) (8f);
		s_strokTextSize_min = (int) (4f * s_factor);

		strokeTextPaint.setARGB(255, 255, 255, 255);
		strokeTextPaint.setTextAlign(android.graphics.Paint.Align.LEFT);
		strokeTextPaint.setStyle(Paint.Style.STROKE);
		strokeTextPaint.setStrokeWidth(s_strokTextSize);
		strokeTextPaint.setFilterBitmap(false);
		strokeTextPaint.setAntiAlias(true);
		strokeTextPaint.setDither(false);
		// shadow for text on map --------------

		if (parent == 0)
		{
			this.gr_type = 1;

			this.activity = activity;
			// view = new SurfaceView2(activity)
			view = new View(activity)
			{
				int touch_mode = NONE;
				float oldDist = 0;
				PointF touch_now = new PointF(0, 0);
				// PointF touch_now_center = new PointF(0, 0);
				PointF touch_start = new PointF(0, 0);
				PointF touch_prev = new PointF(0, 0);
				static final int NONE = 0;
				static final int DRAG = 1;
				static final int ZOOM = 2;
				static final int PRESS = 3;

				//				public void surfaceCreated(SurfaceHolder holder)
				//				{
				//					System.out.println("surfaceCreated");
				//				}
				//
				//				public void surfaceDestroyed(SurfaceHolder holder)
				//				{
				//					System.out.println("surfaceDestroyed");
				//				}

				@Override
				protected void onDraw(Canvas canvas)
				{
					//System.out.println("DO__DRAW:onDraw():- enter");
					//System.out.println("draw main map:isHardwareAccelerated=" + canvas.isHardwareAccelerated());

					synchronized (synch_drawing_for_map)
					{
						//System.out.println("DO__DRAW:onDraw():- start");
						//System.out.println("*******DRAW INIT******* " + gr_type);
						//super.onDraw(canvas);

						if (!MAP_DISPLAY_OFF)
						{

							Global_Map_in_onDraw = true;

							if (draw_reset_factors)
							{
								//System.out.println("DO__DRAW:Java:reset factors");
								draw_reset_factors = false;
								pos_x = 0;
								pos_y = 0;
								ZOOM_MODE_ACTIVE = false;
								ZOOM_MODE_SCALE = 1.0f;
							}

							//System.out.println("DO__DRAW:Java:onDraw:draw bitmap to SCREEN");
							//System.out.println("DO__DRAW:Java:onDraw:gzf=" + Global_Map_Zoomfactor + " scale=" + ZOOM_MODE_SCALE);
							//System.out.println("DO__DRAW:Java:onDraw:gx=" + Global_Map_TransX + " pos_x=" + pos_x);
							//System.out.println("DO__DRAW:Java:onDraw:gy=" + Global_Map_TransY + " pos_y=" + pos_y);

							if (Navit.PREF_show_3d_map)
							{
								draw_canvas_screen2.save();
								if ((ZOOM_MODE_ACTIVE) || (Global_Map_Zoomfactor != 1.0f))
								{
									draw_canvas_screen2.scale(ZOOM_MODE_SCALE * Global_Map_Zoomfactor, ZOOM_MODE_SCALE * Global_Map_Zoomfactor, Global_Map_TransX + Navit.NG__map_main.touch_now_center.x, Global_Map_TransY + Navit.NG__map_main.touch_now_center.y);
								}

								if (Global_Map_Rotationangle != 0.0f)
								{
									draw_canvas_screen2.rotate(Global_Map_Rotationangle, Navit.NG__vehicle.vehicle_pos_x, Navit.NG__vehicle.vehicle_pos_y);
								}

								if (Global_Map_Zoomfactor == 1.0f)
								{
									draw_canvas_screen2.translate(Global_Map_TransX + pos_x, Global_Map_TransY + pos_y);
								}
								else
								{
									draw_canvas_screen2.translate((Global_Map_TransX + pos_x) / Global_Map_Zoomfactor, (Global_Map_TransY + pos_y) / Global_Map_Zoomfactor);
								}

								//System.out.println("DO__DRAW:onDraw():drawBitmap start");
								draw_canvas_screen2.drawColor(map_bg_color); // fill with yellow-ish bg color
								// draw the bitmap in the offscreen buffer (offset 30 pixels to center!!)
								draw_canvas_screen2.drawBitmap(draw_bitmap_screen, 0, 0, paint_for_map_display);
								//System.out.println("DO__DRAW:onDraw():drawBitmap end");
								canvas.save();
								canvas.drawColor(map_bg_color); // fill with yellow-ish bg color

								// 3D modus -----------------
								canvas.concat(cam_m);
								// 3D modus -----------------

								// draw bitmap to screen
								canvas.drawBitmap(draw_bitmap_screen2, 0, 0, paint_for_map_display);

								// ------ DEBUG -------
								// ------ DEBUG -------
								//							Paint paint79 = new Paint();
								//							paint79.setColor(Color.MAGENTA);
								//							paint79.setStrokeWidth(16);
								//							paint79.setStyle(Style.STROKE);
								//							// float y_point = (bitmap_h - (bitmap_h * 0.7f));
								//							float y_point = bitmap_h * 0.7f;
								//							canvas.drawLine(bitmap_w / 2, y_point - 20, bitmap_w / 2, y_point + 20, paint79);
								//							canvas.drawLine(bitmap_w / 2 - 100, y_point, bitmap_w / 2 + 100, y_point, paint79);
								//
								//							Paint paint78 = new Paint();
								//							paint78.setColor(Color.RED);
								//							paint78.setStrokeWidth(20);
								//							paint78.setStyle(Style.STROKE);
								//							canvas.drawRect(30, 30, this.getWidth() - 30, this.getHeight() - 30, paint78);
								// ------ DEBUG -------
								// ------ DEBUG -------

								canvas.restore();
								draw_canvas_screen2.restore();

								if (Navit.is_night)
								{
									if (Navit.is_twilight)
									{
										// draw twilight
										// elevation ->  -0.83 to -10.00
										float a = h_scaled / 10f * (float) (-Navit.elevation);
										float b = h_scaled / 10f * (float) ((-Navit.elevation / 2f) + 5);
										canvas.drawRect(0, 0, this.getWidth(), a, paint_sky_twilight1);
										canvas.drawRect(0, a, this.getWidth(), b, paint_sky_twilight2);
										canvas.drawRect(0, b, this.getWidth(), h_scaled, paint_sky_twilight3);
									}
									else
									{
										// draw sky - at night
										canvas.drawRect(0, 0, this.getWidth(), h_scaled, paint_sky_night);
										// stars
										canvas.drawCircle(NavitGraphics.stars_x[0] * this.getWidth(), NavitGraphics.stars_y[0] * h_scaled, NavitGraphics.stars_size[0], paint_sky_night_stars);
										canvas.drawCircle(NavitGraphics.stars_x[1] * this.getWidth(), NavitGraphics.stars_y[1] * h_scaled, NavitGraphics.stars_size[1], paint_sky_night_stars);
										canvas.drawCircle(NavitGraphics.stars_x[2] * this.getWidth(), NavitGraphics.stars_y[2] * h_scaled, NavitGraphics.stars_size[2], paint_sky_night_stars);
										canvas.drawCircle(NavitGraphics.stars_x[3] * this.getWidth(), NavitGraphics.stars_y[3] * h_scaled, NavitGraphics.stars_size[3], paint_sky_night_stars);
										canvas.drawCircle(NavitGraphics.stars_x[4] * this.getWidth(), NavitGraphics.stars_y[4] * h_scaled, NavitGraphics.stars_size[4], paint_sky_night_stars);
										canvas.drawCircle(NavitGraphics.stars_x[5] * this.getWidth(), NavitGraphics.stars_y[5] * h_scaled, NavitGraphics.stars_size[5], paint_sky_night_stars);
										canvas.drawCircle(NavitGraphics.stars_x[6] * this.getWidth(), NavitGraphics.stars_y[6] * h_scaled, NavitGraphics.stars_size[6], paint_sky_night_stars);
										canvas.drawCircle(NavitGraphics.stars_x[7] * this.getWidth(), NavitGraphics.stars_y[7] * h_scaled, NavitGraphics.stars_size[7], paint_sky_night_stars);
									}
								}
								else
								{
									// draw sky - at day
									canvas.drawRect(0, 0, this.getWidth(), h_scaled, paint_sky_day);
								}
							}
							else
							{
								// ---------- 2D map -----------------------------------
								//
								if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:onDraw:draw bitmap to SCREEN 2D -- START ------");
								canvas.save();
								if ((ZOOM_MODE_ACTIVE) || (Global_Map_Zoomfactor != 1.0f))
								{
									canvas.scale(ZOOM_MODE_SCALE * Global_Map_Zoomfactor, ZOOM_MODE_SCALE * Global_Map_Zoomfactor, Global_Map_TransX + Navit.NG__map_main.touch_now_center.x, Global_Map_TransY + Navit.NG__map_main.touch_now_center.y);
								}

								if (Global_Map_Rotationangle != 0.0f)
								{
									canvas.rotate(Global_Map_Rotationangle, Navit.NG__vehicle.vehicle_pos_x, Navit.NG__vehicle.vehicle_pos_y);
								}

								if (Global_Map_Zoomfactor == 1.0f)
								{
									canvas.translate(Global_Map_TransX + pos_x, Global_Map_TransY + pos_y);
								}
								else
								{
									canvas.translate((Global_Map_TransX + pos_x) / Global_Map_Zoomfactor, (Global_Map_TransY + pos_y) / Global_Map_Zoomfactor);
								}

								//System.out.println("DO__DRAW:onDraw():drawBitmap2D start");
								canvas.drawColor(map_bg_color); // fill with yellow-ish bg color
								// draw bitmap to screen
								canvas.drawBitmap(draw_bitmap_screen, 0, 0, paint_for_map_display);
								//System.out.println("DO__DRAW:onDraw():drawBitmap2D end");

								canvas.restore();
								// if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:onDraw:draw bitmap to SCREEN 2D -- READY ------");
							}

							// draw vehicle also (it seems on newer android API the vehicle view does not get updated automatically anymore)
							Navit.NG__vehicle.view.postInvalidate();

							Global_Map_in_onDraw = false;
						}
						//System.out.println("DO__DRAW:onDraw():- end");
					}
				}

				@Override
				protected void onSizeChanged(int w, int h, int oldw, int oldh)
				{
					mCanvasWidth = w;
					mCanvasHeight = h;
					int h_dpi = h;
					int w_dpi = w;

					// DPI
					int have_dpi = Navit.metrics.densityDpi;
					if (Global_want_dpi == have_dpi)
					{
						Global_dpi_factor = 1;
					}
					{
						Global_dpi_factor = ((float) Global_want_dpi / (float) have_dpi);
					}
					h_dpi = (int) ((float) h * Global_dpi_factor);
					w_dpi = (int) ((float) w * Global_dpi_factor);
					System.out.println("Global_dpi_factor=" + Global_dpi_factor + " h_dpi=" + h_dpi + " w_dpi=" + w_dpi);
					// DPI

					//Log.e("Navit", "NavitGraphics -> onSizeChanged pixels x=" + w + " pixels y=" + h);
					//Log.e("Navit", "NavitGraphics -> onSizeChanged dpi=" + Navit.metrics.densityDpi);
					//Log.e("Navit", "NavitGraphics -> onSizeChanged density=" + Navit.metrics.density);
					//Log.e("Navit", "NavitGraphics -> onSizeChanged scaledDensity=" + Navit.metrics.scaledDensity);

					// now layout the new OSD views --------------------------
					// now layout the new OSD views --------------------------
					// now layout the new OSD views --------------------------
					//					float draw_factor2 = draw_factor;
					//					// correct for ultra high DPI
					//					if (Navit.metrics.densityDpi >= 320) //&& (Navit.PREF_shrink_on_high_dpi))
					//					{
					//						draw_factor2 = 1.8f * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other;
					//						NavitOSDJava.NavitStreetFontLetterWidth = (int) ((float) NavitOSDJava.NavitStreetFontLetterWidth_base * Navit.metrics.densityDpi / NavitGraphics.Global_want_dpi_other);
					//					}
					//
					//					float real_factor = draw_factor2 / 1.5f;
					//
					//					// overwrite locally here!!!! bad!!!
					//					System.out.println("xx=" + mCanvasWidth);
					//					System.out.println("yy=" + mCanvasHeight);
					//					// overwrite locally here!!!! bad!!!
					//
					//					// abs
					//					NavitOSDJava.compass_radius = (int) (50 * real_factor);
					//					NavitOSDJava.compass_lt_x = (int) (mCanvasWidth - (2 * NavitOSDJava.compass_radius) - (16 * real_factor));
					//					NavitOSDJava.compass_lt_y = (int) (mCanvasHeight - (2 * NavitOSDJava.compass_radius) - ((16 + 50) * real_factor));
					//					// rel
					//					NavitOSDJava.compass_w = (int) (2 * NavitOSDJava.compass_radius + 16 * real_factor);
					//					NavitOSDJava.compass_h = (int) (2 * NavitOSDJava.compass_radius + 16 * real_factor);
					//
					//					ZANaviOSDCompass OSD_comp01 = (ZANaviOSDCompass) OSD_new.findViewById(R.id.osd_compass);
					//					android.view.ViewGroup.LayoutParams params = (LayoutParams) OSD_comp01.getLayoutParams();
					//					params.width = NavitOSDJava.compass_w;
					//					params.height = NavitOSDJava.compass_h;
					//					OSD_comp01.requestLayout();
					//
					//					// streetname --
					//					NavitOSDJava.nextt_str_w = mCanvasWidth;
					//					NavitOSDJava.nextt_str_h = (int) (65 * real_factor);
					//					View OSD_streetname = (View) OSD_new.findViewById(R.id.osd_streetname);
					//					params = (LayoutParams) OSD_streetname.getLayoutParams();
					//					System.out.println("xx=" + mCanvasWidth + " view=" + OSD_streetname);
					//					if (mCanvasWidth > 1900)
					//					{
					//						params.width = 1000;
					//					}
					//					else
					//					{
					//						params.width = 200;
					//					}
					//					params.height = NavitOSDJava.nextt_str_h;
					//					OSD_streetname.requestLayout();
					//					OSD_streetname.postInvalidate();
					//					//
					//					View fill_1 = (View) OSD_new.findViewById(R.id.fill_1);
					//					params = (LayoutParams) fill_1.getLayoutParams();
					//					params.width = 2;
					//					if (mCanvasWidth > 1900)
					//					{
					//						params.height = 150;
					//					}
					//					else
					//					{
					//						params.height = 15;
					//					}
					//					fill_1.requestLayout();
					//					//
					//					View fill_2 = (View) OSD_new.findViewById(R.id.fill_2);
					//					params = (LayoutParams) fill_2.getLayoutParams();
					//					params.width = 2;
					//					params.height = 300;
					//					fill_2.requestLayout();
					//					//
					//					//OSD_new.requestLayout();
					//					//OSD_new.postInvalidate();
					// now layout the new OSD views --------------------------
					// now layout the new OSD views --------------------------
					// now layout the new OSD views --------------------------

					super.onSizeChanged(w, h, oldw, oldh);

					if (draw_bitmap != null)
					{
						System.out.println("Graphics: draw_bitmap new:" + w + " x " + h + " old:" + oldw + " x " + oldh);

						// try to avoid out of memory errors
						if ((oldw >= w) && (oldh >= h))
						{
							System.out.println("Graphics: draw_bitmap: reuse");
						}
						else
						{
							System.out.println("Graphics: draw_bitmap: create new");
							draw_bitmap.recycle();
							draw_bitmap = null;
						}
					}

					if (draw_bitmap_screen != null)
					{
						System.out.println("Graphics: draw_bitmap_screen new:" + w + " x " + h + " old:" + oldw + " x " + oldh);

						// try to avoid out of memory errors
						if ((oldw >= w) && (oldh >= h))
						{

						}
						else
						{
							draw_bitmap_screen.recycle();
							draw_bitmap_screen = null;
						}
					}

					if (draw_bitmap_screen2 != null)
					{
						System.out.println("Graphics: draw_bitmap_screen2 new:" + w + " x " + h + " old:" + oldw + " x " + oldh);

						// try to avoid out of memory errors
						if ((oldw >= w) && (oldh >= h))
						{

						}
						else
						{
							draw_bitmap_screen2.recycle();
							draw_bitmap_screen2 = null;
						}
					}

					if (draw_bitmap == null)
					{
						try
						{
							draw_bitmap = Bitmap.createBitmap(w_dpi, h_dpi, Bitmap.Config.ARGB_8888);
							// draw_bitmap.setDensity(Global_want_dpi);
						}
						catch (OutOfMemoryError e)
						{
							int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
							String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
							System.out.println("" + usedMegsString);
							System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
							draw_bitmap = null;
							draw_canvas = null;
							System.gc();
							usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
							usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
							System.out.println("" + usedMegsString);
							// try again
							draw_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
							//draw_bitmap.setDensity(Global_want_dpi);
						}
					}

					if (draw_bitmap_screen == null)
					{
						try
						{
							draw_bitmap_screen = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
							// DPI
							//draw_bitmap_screen.setDensity(Global_want_dpi);
						}
						catch (OutOfMemoryError e)
						{
							int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
							String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
							System.out.println("" + usedMegsString);
							System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
							draw_bitmap_screen = null;
							draw_canvas_screen = null;
							System.gc();
							usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
							usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
							System.out.println("" + usedMegsString);
							// try again
							draw_bitmap_screen = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
							//draw_bitmap_screen.setDensity(Global_want_dpi);
						}
					}

					if (draw_bitmap_screen2 == null)
					{
						try
						{
							draw_bitmap_screen2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
							// DPI
							//draw_bitmap_screen2.setDensity(Global_want_dpi);
						}
						catch (OutOfMemoryError e)
						{
							int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
							String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
							System.out.println("" + usedMegsString);
							System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
							draw_bitmap_screen2 = null;
							draw_canvas_screen2 = null;
							System.gc();
							usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
							usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
							System.out.println("" + usedMegsString);
							// try again
							draw_bitmap_screen2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
							//draw_bitmap_screen2.setDensity(Global_want_dpi);
						}
					}

					draw_canvas = new Canvas(draw_bitmap);
					// DPI
					draw_canvas.setDensity(Global_want_dpi);

					draw_canvas_screen = new Canvas(draw_bitmap_screen);
					// DPI
					//draw_canvas_screen.setDensity(Global_want_dpi);

					draw_canvas_screen2 = new Canvas(draw_bitmap_screen2);
					// DPI
					//draw_canvas_screen2.setDensity(Global_want_dpi);
					if (gr_type == 1)
					{
						//System.out.println("*map*gra*");
						draw_bitmap_s = draw_bitmap;
						draw_canvas_s = draw_canvas;
						draw_bitmap_screen_s = draw_bitmap_screen;
						draw_canvas_screen_s = draw_canvas_screen;
						draw_bitmap_screen_s2 = draw_bitmap_screen2;
						draw_canvas_screen_s2 = draw_canvas_screen2;
					}
					bitmap_w = w;
					bitmap_h = h;

					// DPI
					SizeChangedCallback(w_dpi, h_dpi);
					//SizeChangedCallback(w, h);

					// 3D modus -----------------
					init_3d_mode();
					// 3D modus -----------------

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

					if (Navit.first_ever_startup)
					{
						Navit.first_ever_startup = false;
						//System.out.println("");
						//System.out.println("*** Zoom out FULL (#startup#) ***");
						//System.out.println("");
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 8);
						msg.setData(b);
						callback_handler.sendMessage(msg);
					}
				}

				void printSamples(MotionEvent ev)
				{
					final int historySize = ev.getHistorySize();
					final int pointerCount = ev.getPointerCount();
					for (int h = 0; h < historySize; h++)
					{
						System.out.printf("At time %d:", ev.getHistoricalEventTime(h));
						for (int p = 0; p < pointerCount; p++)
						{
							System.out.printf("  pointer %d: (%f,%f)", ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
						}
					}
					System.out.printf("At time %d:", ev.getEventTime());
					for (int p = 0; p < pointerCount; p++)
					{
						System.out.printf("  pointer %d: (%f,%f)", ev.getPointerId(p), ev.getX(p), ev.getY(p));
					}
				}

				@Override
				public boolean onTouchEvent(MotionEvent event)
				{
					PointF touch_now2 = null;
					PointF touch_start2 = null;
					PointF touch_prev2 = null;
					PointF touch_last_load_tiles2 = null;

					if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouchEvent");
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "systime1:" + System.currentTimeMillis());
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "eventtime1:" + event.getEventTime());

					super.onTouchEvent(event);
					int action = event.getAction();
					int x = (int) event.getX();
					int y = (int) event.getY();

					int _ACTION_POINTER_UP_ = MotionEvent.ACTION_POINTER_UP;
					int _ACTION_POINTER_DOWN_ = MotionEvent.ACTION_POINTER_DOWN;
					int _ACTION_MASK_ = MotionEvent.ACTION_MASK;

					if (NavitAndroidOverlay.voice_rec_bar_visible)
					{
						if (action == MotionEvent.ACTION_UP)
						{
							NavitAndroidOverlay.voice_rec_bar_visible = false;
							NavitAOverlay_s.postInvalidate();
						}
						else
						{
							if (event.getPointerCount() < 2)
							{
								NavitAndroidOverlay.voice_rec_bar_x = x;
								NavitAndroidOverlay.voice_rec_bar_y = y;

								if (NavitAndroidOverlay.voice_rec_bar_x > NavitAndroidOverlay.voice_rec_bar_limit)
								{
									NavitAndroidOverlay.voice_rec_bar_visible = false;
									NavitAndroidOverlay.voice_rec_bar_visible2 = true;
									NavitAOverlay_s.postInvalidate();

									// open voice search screen
									try
									{
										Message msg = Navit.Navit_progress_h.obtainMessage();
										Bundle b = new Bundle();
										msg.what = 19;
										msg.setData(b);
										Navit.Navit_progress_h.sendMessage(msg);
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
								}
								else
								{
									NavitAOverlay_s.postInvalidate();
								}
							}
							else
							{
								NavitAndroidOverlay.voice_rec_bar_visible = false;
								NavitAOverlay_s.postInvalidate();
							}
						}

						// dont do anything else with this event
						return true;
					}

					// calculate value
					int switch_value = (event.getAction() & _ACTION_MASK_);
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "switch_value=" + switch_value);
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "getAction=" + action);
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_CANCEL=" + MotionEvent.ACTION_CANCEL);
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "_ACTION_MASK_=" + _ACTION_MASK_);
					// calculate value

					if (DEBUG_TOUCH) Log.e("NavitGraphics", "event x=" + event.getX() + " y=" + event.getY());

					if (switch_value == MotionEvent.ACTION_DOWN)
					{
						Global_onTouch_fingerdown = true;

						this.touch_now.set(event.getX(), event.getY());
						this.touch_start.set(event.getX(), event.getY());
						this.touch_prev.set(event.getX(), event.getY());
						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_DOWN start");
						//System.out.println("DO__DRAW:Java:ACTION DOWN start");

						d_pos_x_old = pos_x;
						d_pos_y_old = pos_y;
						//Log.e("NavitGraphics", "pos_x=" + pos_x);
						//Log.e("NavitGraphics", "pos_y=" + pos_y);

						// cancel previous map drawing
						//System.out.println("DO__DRAW:Java:cancel map drawing");
						// CallbackMessageChannel(50, "");
						CallbackMessageChannelReal(50, "");

						if (in_map)
						{
							if (!NavitAOverlay.get_show_bubble())
							{
								if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble X1");
								NavitAOverlay.hide_bubble();
								// remember last press on screen time
								last_touch_on_screen = event.getEventTime(); // System.currentTimeMillis();
								touch_sensor_thread = new SensorThread(last_touch_on_screen, this.touch_now.x, this.touch_now.y);
								touch_sensor_thread.start();
							}
						}

						wait_for_redraw_map = true;
						if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=true");

						// down
						//						Message msg = new Message();
						//						Bundle b = new Bundle();
						//						b.putInt("Callback", 21);
						//						b.putInt("x", x);
						//						b.putInt("y", y);
						//						msg.setData(b);
						//						callback_handler.sendMessage(msg);
						// ButtonCallback(ButtonCallbackID, 1, 1, x, y); // down

						// wait_for_redraw_map = false;

						touch_mode = DRAG;

						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_DOWN end");

						// hold all map drawing -----------
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 69);
						msg.setData(b);
						try
						{
							callback_handler.sendMessage(msg);
						}
						catch (Exception e)
						{
						}
						// hold all map drawing -----------

						//System.out.println("DO__DRAW:Java:ACTION DOWN end");

					}
					else if ((switch_value == MotionEvent.ACTION_UP) || (switch_value == _ACTION_POINTER_UP_))
					{
						this.touch_now.set(event.getX(), event.getY());
						touch_now2 = touch_now;
						touch_start2 = touch_start;

						//System.out.println("DO__DRAW:Java:ACTION UP start");

						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_UP start");
						if (DEBUG_TOUCH) Log.e("NavitGraphics", "xxxxxxxxxx");
						try
						{
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop X1");
							//touch_sensor_thread.down();
							touch_sensor_thread.stop_me();
							// touch_sensor_thread.stop();
						}
						catch (Exception e)
						{

						}
						// if it was a real longpress, dont hide the bubble
						long real_event_time = event.getEventTime();
						// long real_event_time = System.currentTimeMillis();
						if ((in_map) && ((real_event_time - last_touch_on_screen) > long_press_on_screen_interval))
						{
							// real long press
						}
						else
						{
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble 1");
							NavitAOverlay.hide_bubble();
						}

						if ((touch_mode == DRAG) && (spacing(touch_start2, touch_now2) < long_press_on_screen_max_distance))
						{
							// just a single press down
							touch_mode = PRESS;

							try
							{
								if (DEBUG_TOUCH) Log.e("NavitGraphics", "touch_sensor_thread.stop 1");
								//touch_sensor_thread.down();
								touch_sensor_thread.stop_me();
								// touch_sensor_thread.stop();
							}
							catch (Exception e)
							{

							}

							// was it a long press? or normal quick touch?
							//if ((in_map) && ((System.currentTimeMillis() - last_touch_on_screen) > long_press_on_screen_interval))
							//{
							//	Log.e("NavitGraphics", "onTouch up (LONG PRESS 1)");
							//	do_longpress_action(touch_now.x, touch_now.y);
							//}
							//else
							//{
							//Log.e("NavitGraphics", "onTouch up (quick touch)");
							//wait_for_redraw_map = true;
							//System.out.println("wait_for_redraw_map=true 2");

							//*ButtonCallback(ButtonCallbackID, 0, 1, x, y); // up

							// quick touch --> here we could repeat the last spoken direction ...
							// quick touch --> here we could repeat the last spoken direction ...
							// quick touch --> here we could repeat the last spoken direction ...
							// quick touch --> here we could repeat the last spoken direction ...
							// quick touch --> here we could repeat the last spoken direction ...
							// quick touch --> here we could repeat the last spoken direction ...
							// quick touch --> here we could repeat the last spoken direction ...
							// quick touch --> here we could repeat the last spoken direction ...

							if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=false 9");
							NavitGraphics.wait_for_redraw_map = false;
							try
							{
								if (DEBUG_TOUCH) Log.e("NavitGraphics", "overlay thread stop X1");
								//NavitAndroidOverlay.overlay_draw_thread1.stop_redraw();
								//NavitAndroidOverlay.overlay_draw_thread1.stop_me();
								//NavitAndroidOverlay.overlay_draw_thread1 = null;
							}
							catch (Exception e)
							{
								//e.printStackTrace();
							}
							try
							{
								//System.out.println("invalidate 011");
								NavitAOverlay_s.postInvalidate();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							//}
							touch_mode = NONE;
						}
						else
						{
							if (touch_mode == DRAG)
							{
								touch_now2 = touch_now;
								touch_start2 = touch_start;

								if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouch move");

								try
								{
									if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop X2");
									//touch_sensor_thread.down();
									touch_sensor_thread.stop_me();
									// touch_sensor_thread.stop();
								}
								catch (Exception e)
								{

								}

								// if we drag, hide the bubble --> NO !!
								// NavitAOverlay.hide_bubble();

								// wait_for_redraw_map = true;
								//System.out.println("wait_for_redraw_map=true 3");

								// DRAG ----------- new END --------------
								// DRAG ----------- new END --------------
								// DRAG ----------- new END --------------
								// this.touch_start
								// this.touch_now
								//pos_x = (int) (this.touch_now.x - this.touch_start.x);
								//pos_y = (int) (this.touch_now.y - this.touch_start.y);

								// pos_x = d_pos_x_old; // wait for redraw !!
								// pos_y = d_pos_y_old; // wait for redraw !!

								// ??? // ButtonCallback(1, 1, (int) this.touch_start.x, (int) this.touch_start.y); // down
								//this.invalidate();
								//this.postInvalidate();
								// DRAG ----------- new END --------------
								// DRAG ----------- new END --------------
								// DRAG ----------- new END --------------

								// allow all map drawing -----------
								Message msg2 = new Message();
								Bundle b2 = new Bundle();
								b2.putInt("Callback", 70);
								msg2.setData(b2);
								try
								{
									callback_handler.sendMessage(msg2);
								}
								catch (Exception e)
								{
								}

								// MotionCallback(x, y);

								//System.out.println("DO__DRAW:Java:motion call back JAVA -START-");
								// -- new call --
								// if (dl_thread[0] != null)
								// {
								//	dl_thread[0].motion_callback((int) this.touch_start.x, (int) this.touch_start.y, (int) this.touch_now.x, (int) this.touch_now.y);
								// }
								// -- old call --
								MotionCallback((int) this.touch_start.x, (int) this.touch_start.y, (int) this.touch_now.x, (int) this.touch_now.y);
								//System.out.println("DO__DRAW:Java:motion call back JAVA --END--");

								//msg2 = new Message();
								//b2 = new Bundle();
								//b2.putInt("Callback", 23);
								//b2.putInt("x",(int) this.touch_start.x);
								//b2.putInt("y",(int) this.touch_start.y);
								//b2.putInt("x2",(int) this.touch_now.x);
								//b2.putInt("y2",(int) this.touch_now.y);
								//msg2.setData(b2);
								//try
								//{
								//	callback_handler.sendMessage(msg2);
								//}
								//catch (Exception e)
								//{
								//}

								// ??? // ButtonCallback(0, 1, x, y); // up

								if (!Navit.PREF_follow_gps)
								{
									Navit.cwthr.CallbackGeoCalc2(1, 0, NavitGraphics.Global_dpi_factor * mCanvasWidth / 2, NavitGraphics.Global_dpi_factor * mCanvasHeight / 2);
								}

								//								try
								//								{
								//									// try to show current location/streetname
								//									Message msg1 = new Message();
								//									Bundle b1 = new Bundle();
								//									b1.putInt("Callback", 9901);
								//									msg1.setData(b1);
								//									NavitGraphics.callback_handler_s.sendMessage(msg1);
								//								}
								//								catch (Exception e)
								//								{
								//
								//								}

								touch_mode = NONE;
							}
							else
							{
								if (touch_mode == ZOOM)
								{
									// end of "pinch zoom" move
									// ZOOM_MODE_ACTIVE = false;  // wait for redraw !!
									// ZOOM_MODE_SCALE = 1.0f;  // wait for redraw !!

									if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouch zoom");

									float newDist = spacing(event);
									float scale = 1.0f;
									if (newDist > 10f)
									{
										scale = newDist / oldDist;
									}

									if (scale > 1.0f)
									{
										wait_for_redraw_map = true;
										if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=true 4");

										try
										{
											if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop X3");
											//touch_sensor_thread.down();
											touch_sensor_thread.stop_me();
											// touch_sensor_thread.stop();
										}
										catch (Exception e)
										{

										}
										if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble 2");
										// if we drag, hide the bubble
										NavitAOverlay.hide_bubble();

										// zoom in
										if (scale < 0.1f)
										{
											scale = 0.1f;
										}
										else if (scale > 6)
										{
											scale = 6;
										}

										// pos_x = d_pos_x_old; // wait for redraw !!
										// pos_y = d_pos_y_old; // wait for redraw !!

										Message msg = new Message();
										Bundle b = new Bundle();
										b.putInt("Callback", 61);
										//System.out.println("sc1:" + Navit.GlobalScaleLevel);
										Navit.GlobalScaleLevel = (int) (Navit.GlobalScaleLevel / scale);
										//System.out.println("sc1.1:" + Navit.GlobalScaleLevel);

										Navit.NG__map_main.touch_now_center = calc_center(event);
										b.putString("s", (int) (Navit.NG__map_main.touch_now_center.x * Global_dpi_factor) + "#" + (int) (Navit.NG__map_main.touch_now_center.y * Global_dpi_factor) + "#" + Integer.toString(Navit.GlobalScaleLevel));
										msg.setData(b);
										try
										{
											callback_handler.sendMessage(msg);
											//System.out.println("touch: set zoom(in) level: " + Navit.GlobalScaleLevel);
										}
										catch (Exception e)
										{
										}

										// next lines are a hack, without it screen will not get updated anymore!
										//*ButtonCallback(ButtonCallbackID, 1, 1, x, y); // down
										//*MotionCallback(MotionCallbackID, x + 15, y);
										//*MotionCallback(MotionCallbackID, x - 15, y);
										//*ButtonCallback(ButtonCallbackID, 0, 1, x, y); // up
										//*this.postInvalidate();

										try
										{
											if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop X4");
											//touch_sensor_thread.down();
											touch_sensor_thread.stop_me();
											// touch_sensor_thread.stop();
										}
										catch (Exception e)
										{

										}
										// if we drag, hide the bubble
										if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble X3");
										NavitAOverlay.hide_bubble();

										if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouch zoom in");
									}
									else if (scale < 1.0f)
									{
										wait_for_redraw_map = true;
										if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=true 5");

										try
										{
											if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop X5");
											//touch_sensor_thread.down();
											touch_sensor_thread.stop_me();
											// touch_sensor_thread.stop();
										}
										catch (Exception e)
										{

										}
										if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble 3");
										// if we drag, hide the bubble
										NavitAOverlay.hide_bubble();

										// zoom out    

										if (scale < 0.07f)
										{
											scale = 0.07f;
										}
										else if (scale > 6)
										{
											scale = 6;
										}

										// pos_x = d_pos_x_old; // wait for redraw !!
										// pos_y = d_pos_y_old; // wait for redraw !!

										Message msg = new Message();
										Bundle b = new Bundle();
										b.putInt("Callback", 61);
										//System.out.println("sc2:" + Navit.GlobalScaleLevel);
										Navit.GlobalScaleLevel = (int) (Navit.GlobalScaleLevel / scale);
										if (Navit.GlobalScaleLevel < 2)
										{
											Navit.GlobalScaleLevel = 2;
										}
										//System.out.println("sc2.1:" + Navit.GlobalScaleLevel);
										Navit.NG__map_main.touch_now_center = calc_center(event);
										b.putString("s", (int) (Navit.NG__map_main.touch_now_center.x * Global_dpi_factor) + "#" + (int) (Navit.NG__map_main.touch_now_center.y * Global_dpi_factor) + "#" + Integer.toString(Navit.GlobalScaleLevel));
										msg.setData(b);
										try
										{
											callback_handler.sendMessage(msg);
											//System.out.println("touch: set zoom(out) level: " + Navit.GlobalScaleLevel);
										}
										catch (Exception e)
										{
										}

										// next lines are a hack, without it screen will not get updated anymore!
										//*ButtonCallback(ButtonCallbackID, 1, 1, x, y); // down
										//*MotionCallback(MotionCallbackID, x + 15, y);
										//*MotionCallback(MotionCallbackID, x - 15, y);
										//*ButtonCallback(ButtonCallbackID, 0, 1, x, y); // up
										//*this.postInvalidate();

										try
										{
											if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop X6");
											//touch_sensor_thread.down();
											touch_sensor_thread.stop_me();
											// touch_sensor_thread.stop();
										}
										catch (Exception e)
										{

										}
										if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble 4");
										// if we drag, hide the bubble
										NavitAOverlay.hide_bubble();

										if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouch zoom out");
									}
									else
									{
										// pos_x = d_pos_x_old; // wait for redraw !!
										// pos_y = d_pos_y_old; // wait for redraw !!

										// move was not zoom-out OR zoom-in
										// so just ignore and remove any pending stuff
										if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=false 10");
										NavitGraphics.wait_for_redraw_map = false;
										try
										{
											if (DEBUG_TOUCH) Log.e("NavitGraphics", "overlay thread stop X10");
											//NavitAndroidOverlay.overlay_draw_thread1.stop_redraw();
											//NavitAndroidOverlay.overlay_draw_thread1.stop_me();
											//NavitAndroidOverlay.overlay_draw_thread1 = null;
										}
										catch (Exception e)
										{
											//e.printStackTrace();
										}
										try
										{
											//System.out.println("invalidate 012");
											NavitAOverlay_s.postInvalidate();
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}
									}
									touch_mode = NONE;
								}
								else
								{
									if (DEBUG_TOUCH) Log.d("NavitGraphics", "touch_mode=NONE (END of ZOOM part 2)");
									touch_mode = NONE;
								}
							}
						}

						//Log.e("NavitGraphics", "pos_x=" + pos_x);
						//Log.e("NavitGraphics", "pos_y=" + pos_y);
						//Log.e("NavitGraphics", "d_pos_x_old=" + d_pos_x_old);
						//Log.e("NavitGraphics", "d_pos_y_old=" + d_pos_y_old);

						// on final "UP" action, always reset the display center
						if (switch_value == MotionEvent.ACTION_UP)
						{
							// pos_x = d_pos_x_old; // wait for redraw !!
							// pos_y = d_pos_y_old; // wait for redraw !!

							//Log.e("NavitGraphics", "DO__DRAW:Java:Final Up action");
							//copy_backwards_map_buffer();
							//draw_reset_factors = true;
							//this.invalidate();

							//System.out.println("DO__DRAW:Java:xchange global factors");
							Global_Map_TransX = Global_Map_TransX + pos_x;
							Global_Map_TransY = Global_Map_TransY + pos_y;
							Global_Map_Rotationangle = 0f;
							Global_Map_Zoomfactor = Global_Map_Zoomfactor * ZOOM_MODE_SCALE;

							//System.out.println("DO__DRAW:Java:reset local factors");
							pos_x = 0;
							pos_y = 0;
							ZOOM_MODE_ACTIVE = false;
							ZOOM_MODE_SCALE = 1.0f;

							try
							{
								if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop final88");
								touch_sensor_thread.stop_me();
							}
							catch (Exception e)
							{

							}

							Global_onTouch_fingerdown = false;

							// allow all map drawing -----------
							Message msg2 = new Message();
							Bundle b2 = new Bundle();
							b2.putInt("Callback", 70);
							msg2.setData(b2);
							try
							{
								callback_handler.sendMessage(msg2);
							}
							catch (Exception e)
							{
							}
							// allow all map drawing -----------
						}

						//System.out.println("DO__DRAW:Java:ACTION UP end");

					}
					else if (switch_value == MotionEvent.ACTION_MOVE)
					{
						//System.out.println("DO__DRAW:Java:ACTION MOVE start");

						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_MOVE");

						if (touch_mode == DRAG)
						{
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "mode = DRAG");

							this.touch_now.set(event.getX(), event.getY());
							touch_now2 = touch_now;
							touch_start2 = touch_start;
							touch_prev2 = touch_prev;
							this.touch_prev.set(event.getX(), event.getY());

							//							try
							//							{
							//								//touch_sensor_thread.down();
							//								touch_sensor_thread.stop_me();
							//								// touch_sensor_thread.stop();
							//							}
							//							catch (Exception e)
							//							{
							//
							//							}

							if (DEBUG_TOUCH) Log.e("NavitGraphics", "systime2:" + System.currentTimeMillis());
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "eventtime2:" + event.getEventTime());
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "last touch screen:" + last_touch_on_screen);
							//							if ((System.currentTimeMillis() - last_touch_on_screen) > long_press_on_screen_interval)
							//							{
							//								Log.e("NavitGraphics", "onTouch up (LONG PRESS 2) ***** 11 *****");
							//								// do_longpress_action(touch_now.x, touch_now.y);
							//							}

							if ((in_map) && (spacing(touch_start2, touch_now2) < long_press_on_screen_max_distance))
							{
								// now its still a PRESS, because we have not moved around too much!

								if (DEBUG_TOUCH) Log.e("NavitGraphics", "in move: still a PRESS action");

								// is it a still ongoing long press?
								if ((System.currentTimeMillis() - last_touch_on_screen) > long_press_on_screen_interval)
								{
									if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouch up (LONG PRESS 2) ***** 22 *****");
									// do_longpress_action(touch_now.x, touch_now.y);
								}
							}
							else
							{
								if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouch move2");
								// MotionCallback(MotionCallbackID, x, y);
								// DRAG ----------- new --------------
								// DRAG ----------- new --------------
								// DRAG ----------- new --------------

								try
								{
									if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop X6");
									//touch_sensor_thread.down();
									touch_sensor_thread.stop_me();
									// touch_sensor_thread.stop();
								}
								catch (Exception e)
								{

								}

								pos_x = (int) (this.touch_now.x - this.touch_start.x);
								pos_y = (int) (this.touch_now.y - this.touch_start.y);
								//**dl_thread[dl_thread_cur].add_lines2(null, null, 0, 0, 98);
								//draw_map_one_shot = true;
								//copy_map_buffer();
								//System.out.println("invalidate 013");
								this.invalidate();
								//this.paint_me();
								// this.postInvalidate();
								// DRAG ----------- new --------------
								// DRAG ----------- new --------------
								// DRAG ----------- new --------------

							}
						}
						else if (touch_mode == ZOOM)
						{
							this.touch_now.set(event.getX(), event.getY());
							this.touch_prev.set(event.getX(), event.getY());

							if (DEBUG_TOUCH) Log.e("NavitGraphics", "zoom 2");

							if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble 73");
							NavitAOverlay.hide_bubble();
							try
							{
								if (DEBUG_TOUCH) Log.e("NavitGraphics", "touch_sensor_thread.stop 73");
								touch_sensor_thread.stop_me();
							}
							catch (Exception e)
							{
							}
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble 74");
							NavitAOverlay.hide_bubble();

							// were are in the middle of a zooming action here ----------
							// were are in the middle of a zooming action here ----------

							Navit.NG__map_main.touch_now_center = calc_center(event);
							float newDist = spacing(event);
							float scale = 1.0f;
							try
							{
								scale = newDist / oldDist;
							}
							catch (Exception ee)
							{

							}

							if (scale != 0)
							{
								ZOOM_MODE_SCALE = scale;
							}
							else
							{
								ZOOM_MODE_SCALE = 1.0f;
							}

							ZOOM_MODE_ACTIVE = true;
							//**dl_thread[dl_thread_cur].add_lines2(null, null, 0, 0, 98);
							//draw_map_one_shot = true;
							//copy_map_buffer();
							//System.out.println("invalidate 014");
							this.invalidate();
							//this.paint_me();
							// Log.e("NavitGraphics", "x:" + this.touch_now.x + " y:" + this.touch_now.y);
							// were are in the middle of a zooming action here ----------
							// were are in the middle of a zooming action here ----------
						}

						//System.out.println("DO__DRAW:Java:ACTION MOVE end");

					}
					else if (switch_value == _ACTION_POINTER_DOWN_)
					{
						//System.out.println("DO__DRAW:Java:ACTION POINTER DOWN start");
						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_POINTER_DOWN");

						oldDist = spacing(event);
						if (oldDist > 10f)
						{
							touch_mode = ZOOM;
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "--> zoom");
							// zoom action starts here --------
							// zoom action starts here --------
							// zoom action starts here --------
							// zoom action starts here --------
						}

						//System.out.println("DO__DRAW:Java:ACTION POINTER DOWN end");

					}
					else
					{
						// most likely an ACTION_CANCEL !

						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION=" + action);

						try
						{
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "sensor thread stop 88X6");
							//touch_sensor_thread.down();
							touch_sensor_thread.stop_me();
							// touch_sensor_thread.stop();
						}
						catch (Exception e)
						{

						}

						NavitGraphics.wait_for_redraw_map = false;

						pos_x = 0;
						pos_y = 0;
						ZOOM_MODE_ACTIVE = false;
						ZOOM_MODE_SCALE = 1.0f;

						Global_onTouch_fingerdown = false;

						// allow all map drawing -----------
						Message msg2 = new Message();
						Bundle b2 = new Bundle();
						b2.putInt("Callback", 70);
						msg2.setData(b2);
						try
						{
							callback_handler.sendMessage(msg2);
						}
						catch (Exception e)
						{
						}
						// allow all map drawing -----------

						if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble 883");
						NavitAOverlay.hide_bubble();

						touch_mode = NONE;
						last_touch_on_screen = System.currentTimeMillis();

						this.invalidate();
						//this.paint_me();
					}
					return true;
				}

				private float spacing(PointF a, PointF b)
				{
					float x = a.x - b.x;
					float y = a.y - b.y;
					return FloatMath.sqrt(x * x + y * y);
				}

				public PointF calc_center(MotionEvent event)
				{
					//float x;
					//float y;
					PointF ret = new PointF(0, 0);
					try
					{
						float y0 = 0;
						float y1 = 0;
						float x0 = 0;
						float x1 = 0;

						x0 = event.getX(0);
						y0 = event.getY(0);
						try
						{
							x1 = event.getX(1);
							y1 = event.getY(1);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
						ret.x = x0 - ((x0 - x1) / 2);
						ret.y = y0 - ((y0 - y1) / 2);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						ret.x = event.getX(0);
						ret.y = event.getY(0);
					}
					return ret;
				}

				public float spacing(MotionEvent event)
				{
					float x;
					float y;
					try
					{
						float y0 = 0;
						float y1 = 0;
						float x0 = 0;
						float x1 = 0;

						x0 = event.getX(0);
						y0 = event.getY(0);
						try
						{
							x1 = event.getX(1);
							y1 = event.getY(1);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
						x = x0 - x1;
						y = y0 - y1;
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						x = 0;
						y = 0;
					}
					return FloatMath.sqrt(x * x + y * y);
				}

				@Override
				public boolean onKeyDown(int keyCode, KeyEvent event)
				{
					//if (keyCode == KeyEvent.KEYCODE_BACK)
					//{
					//	// override back button, so it does NOT quit the application
					//	return true;
					//}
					return super.onKeyDown(keyCode, event);
				}

				@Override
				protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
				{
					super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
					//Log.e("NavitGraphics", "FocusChange " + gainFocus);
				}
			};

			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.setKeepScreenOn(true);
			relativelayout = new RelativeLayout(activity);

			if (this.gr_type == 1)
			{
				// view_s = (SurfaceView2) view;
				view_s = view;
			}

			//if (use_camera != 0)
			//{
			//	SetCamera(use_camera);
			//}
			relativelayout.addView(view);

			// vehicle view
			RelativeLayout.LayoutParams NavitVehicleGraph_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			relativelayout.addView(Navit.NG__vehicle.view, NavitVehicleGraph_lp);
			Navit.NG__vehicle.view.bringToFront();
			Navit.NG__vehicle.view.postInvalidate();
			// vehicle view

			// android overlay
			//Log.e("Navit", "create android overlay");
			NavitAOverlay = new NavitAndroidOverlay(relativelayout.getContext());
			NavitAOverlay_s = NavitAOverlay;
			RelativeLayout.LayoutParams NavitAOverlay_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			relativelayout.addView(NavitAOverlay, NavitAOverlay_lp);
			NavitAOverlay.bringToFront();
			NavitAOverlay.postInvalidate();
			// android overlay

			// android OSDJava
			//Log.e("Navit", "create android OSDJava");
			//**//NavitAOSDJava = new NavitOSDJava(relativelayout.getContext());
			//**//NavitAOSDJava_ = NavitAOSDJava;
			//**//RelativeLayout.LayoutParams NavitAOSDJava_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			//**//relativelayout.addView(NavitAOSDJava, NavitAOSDJava_lp);
			//**//NavitAOSDJava.bringToFront();
			//**//NavitAOSDJava.postInvalidate();

			// OSD new -------------------------------------------------------
			// OSD new -------------------------------------------------------
			// OSD new -------------------------------------------------------
			LayoutInflater inflater = activity.getLayoutInflater();
			OSD_new = (ZANaviLinearLayout) inflater.inflate(R.layout.zanavi_osd, relativelayout, false);
			RelativeLayout.LayoutParams NavitAOSDJava_lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			relativelayout.addView(OSD_new, NavitAOSDJava_lp2);
			OSD_new.bringToFront();
			// OSD new -------------------------------------------------------
			// OSD new -------------------------------------------------------
			// OSD new -------------------------------------------------------

			// android OSDJava

			// android Messages TextView
			//Log.e("Navit", "create android Messages TextView");
			NavitMsgTv = new TextView(relativelayout.getContext());
			NavitMsgTv_ = NavitMsgTv;
			RelativeLayout.LayoutParams NavitMsgTv_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			// NavitMsgTv_lp.height = 50;
			NavitMsgTv_lp.leftMargin = 120;
			NavitMsgTv_lp.topMargin = 176;
			// NavitMsgTv.setHeight(10);
			int tc = Color.argb(125, 0, 0, 0); // half transparent black
			NavitMsgTv.setBackgroundColor(tc);
			NavitMsgTv.setLines(4);
			NavitMsgTv.setTextSize(12);
			NavitMsgTv.setTextColor(Color.argb(255, 200, 200, 200)); // almost white
			relativelayout.addView(NavitMsgTv, NavitMsgTv_lp);
			NavitMsgTv.bringToFront();
			NavitMsgTv.postInvalidate();
			// android Messages TextView

			// android Speech Messages TextView
			//Log.e("Navit", "create android Speech Messages TextView");
			NavitMsgTv2 = new TextView(relativelayout.getContext());
			NavitMsgTv2_ = NavitMsgTv2;
			RelativeLayout.LayoutParams NavitMsgTv_lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			NavitMsgTv_lp2.leftMargin = 10;
			NavitMsgTv_lp2.rightMargin = 10;
			int tc2 = Color.argb(125, 0, 0, 0); // half transparent black
			NavitMsgTv2.setBackgroundColor(tc2);
			NavitMsgTv2.setTextSize(15);
			NavitMsgTv2.setTextColor(Color.argb(255, 200, 200, 200)); // almost white

			ScrollView sc = new ScrollView(relativelayout.getContext());
			RelativeLayout.LayoutParams NavitMsgTv_lp3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			sc.addView(NavitMsgTv2, NavitMsgTv_lp2);
			sc.setFadingEdgeLength(20);
			sc.setScrollbarFadingEnabled(true);
			sc.setHorizontalScrollBarEnabled(true);
			sc.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			NavitMsgTv2.setGravity(Gravity.BOTTOM);
			relativelayout.addView(sc, NavitMsgTv_lp3);

			NavitMsgTv2.bringToFront();
			NavitMsgTv2.postInvalidate();
			NavitMsgTv2.setEnabled(false);
			NavitMsgTv2.setVisibility(View.GONE);
			// android Speech Messages TextView

			// busy spinner view on top of everything
			ZANaviBusySpinner busyspinner = new ZANaviBusySpinner(relativelayout.getContext());
			busyspinner_ = busyspinner;
			RelativeLayout.LayoutParams ZANaviBusySpinner_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			relativelayout.addView(busyspinner, ZANaviBusySpinner_lp);
			busyspinner.bringToFront();
			busyspinner.postInvalidate();
			busyspinner.setVisibility(View.INVISIBLE);

			ZANaviBusyText busyspinnertext = new ZANaviBusyText(relativelayout.getContext());
			busyspinnertext_ = busyspinnertext;
			RelativeLayout.LayoutParams ZANaviBusyText_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			relativelayout.addView(busyspinnertext, ZANaviBusyText_lp);
			busyspinnertext.bringToFront();
			busyspinnertext.postInvalidate();
			busyspinnertext.setVisibility(View.INVISIBLE);
			// busy spinner view on top of everything

			// big map overlay
			//			NavitGlobalMap_ = new NavitGlobalMap(relativelayout.getContext());
			//			RelativeLayout.LayoutParams NavitGlobalMap_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			//			relativelayout.addView(NavitGlobalMap_, NavitGlobalMap_lp);
			//			NavitGlobalMap_.bringToFront();
			//			NavitGlobalMap_.invalidate();
			// big map overlay

			emu_menu_view = new EmulatedMenuView(relativelayout.getContext(), Navit.Global_Navit_Object);
			RelativeLayout.LayoutParams emvlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			emvlp.setMargins(dp_to_px(40), dp_to_px(30), dp_to_px(40), dp_to_px(30));
			relativelayout.addView(emu_menu_view, emvlp);
			emu_menu_view.bringToFront();
			emu_menu_view.setVisibility(View.INVISIBLE);

			activity.setContentView(relativelayout);
			view.requestFocus();
		}
		// parent == 0 ---------------
		else
		// parent == 1 ---------------
		{
			if (draw_bitmap != null)
			{
				// try to avoid out of memory errors
				draw_bitmap.recycle();
			}

			//int h_dpi_v = (int) ((float) h * Global_dpi_factor);
			//int w_dpi_v = (int) ((float) w * Global_dpi_factor);

			try
			{
				// DPI
				draw_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			}
			catch (OutOfMemoryError e)
			{
				int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);
				System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
				System.gc();
				usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);
				// try again
				draw_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			}

			bitmap_w = w;
			bitmap_h = h;
			pos_x = x;
			pos_y = y;
			pos_wraparound = wraparound;
			draw_canvas = new Canvas(draw_bitmap);
			// DPI
			//draw_canvas.setDensity(Global_want_dpi);

			this.activity = activity;
			view = new View(activity)
			{
				protected void onDraw(Canvas canvas)
				{
					//System.out.println("V.Draw x");
					//System.out.println("V.Draw x="+NG__vehicle.vehicle_pos_x+" y="+NG__vehicle.vehicle_pos_y);
					//System.out.println("V.Draw d=" + Navit.nav_arrow_stopped.getDensity());
					//System.out.println("V.Draw w=" + Navit.nav_arrow_stopped.getWidth());
					//System.out.println("V.Draw h=" + Navit.nav_arrow_stopped.getHeight());
					//super.onDraw(canvas);

					if (!NavitGraphics.MAP_DISPLAY_OFF)
					{
						if (Navit.NG__vehicle.vehicle_speed < 3)
						{
							if (Navit.PREF_show_3d_map)
							{
								// 3D modus -----------------
								canvas.save();
								if (Navit.PREF_show_vehicle_3d)
								{
									canvas.concat(cam_m_vehicle);
									canvas.drawBitmap(Navit.nav_arrow_stopped_small, Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_stopped_small.getWidth() / 2, Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_stopped_small.getHeight() / 2, null);
								}
								// 3D modus -----------------
								else
								{
									canvas.drawBitmap(Navit.nav_arrow_stopped, Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_stopped.getWidth() / 2, Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_stopped.getHeight() / 2, null);
								}
							}
							else
							{
								canvas.drawBitmap(Navit.nav_arrow_stopped, Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_stopped.getWidth() / 2, Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_stopped.getHeight() / 2, null);
							}
							if (Navit.PREF_show_3d_map)
							{
								// 3D modus -----------------
								canvas.restore();
								// 3D modus -----------------
							}
						}
						else
						{
							if ((Navit.NG__vehicle.vehicle_direction != 0) || (Navit.PREF_show_3d_map))
							{
								canvas.save();
							}

							if (Navit.NG__vehicle.vehicle_direction != 0)
							{
								// rotate nav icon if needed
								canvas.rotate(Navit.NG__vehicle.vehicle_direction, Navit.NG__vehicle.vehicle_pos_x, Navit.NG__vehicle.vehicle_pos_y);
							}

							if (Navit.PREF_show_3d_map)
							{
								// 3D modus -----------------
								if (Navit.PREF_show_vehicle_3d)
								{
									canvas.concat(cam_m_vehicle);
									// offset shadow x+2 , y+8
									canvas.drawBitmap(Navit.nav_arrow_moving_shadow_small, 2 + Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_moving_shadow_small.getWidth() / 2, 8 + Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_moving_shadow_small.getHeight() / 2, null);
									canvas.drawBitmap(Navit.nav_arrow_moving_small, Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_moving_small.getWidth() / 2, Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_moving_small.getHeight() / 2, null);
								}
								// 3D modus -----------------
								else
								{
									// offset shadow x+2 , y+8
									canvas.drawBitmap(Navit.nav_arrow_moving_shadow, 2 + Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_moving_shadow.getWidth() / 2, 8 + Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_moving_shadow.getHeight() / 2, null);
									canvas.drawBitmap(Navit.nav_arrow_moving, Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_moving.getWidth() / 2, Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_moving.getHeight() / 2, null);
								}
							}
							else
							{
								// offset shadow x+2 , y+8
								canvas.drawBitmap(Navit.nav_arrow_moving_shadow, 2 + Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_moving_shadow.getWidth() / 2, 8 + Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_moving_shadow.getHeight() / 2, null);
								canvas.drawBitmap(Navit.nav_arrow_moving, Navit.NG__vehicle.vehicle_pos_x - Navit.nav_arrow_moving.getWidth() / 2, Navit.NG__vehicle.vehicle_pos_y - Navit.nav_arrow_moving.getHeight() / 2, null);
							}

							if ((Navit.NG__vehicle.vehicle_direction != 0) || (Navit.PREF_show_3d_map))
							{
								canvas.restore();
							}
						}

						// paint the sweep spot of the vehicle position!!
						//						Paint paint22 = new Paint();
						//						paint22.setStyle(Paint.Style.FILL);
						//						paint22.setStrokeWidth(0);
						//						paint22.setColor(Color.RED);
						//						canvas.drawCircle(Navit.NG__vehicle.vehicle_pos_x, Navit.NG__vehicle.vehicle_pos_y, 5, paint22);
						// paint the sweep spot of the vehicle position!!
					}
				}

				@Override
				protected void onSizeChanged(int w, int h, int oldw, int oldh)
				{
					System.out.println("new width=" + w + " new height=" + h);
				}
			};

		} // END IF: parent == 1 ---------------

		parent_num = parent;
	}

	public Handler callback_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.getData().getInt("Callback") == 1)
			{
				// zoom in
				CallbackMessageChannel(1, "");
			}
			else if (msg.getData().getInt("Callback") == 2)
			{
				// zoom out
				CallbackMessageChannel(2, "");
			}
			else if (msg.getData().getInt("Callback") == 3)
			{
				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// set routing target to lat,lon
				CallbackMessageChannel(3, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 48)
			{
				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// append to routing, add waypoint at lat,lon
				CallbackMessageChannel(48, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 4)
			{
				// set routing target to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");

				CallbackMessageChannel(4, "" + x + "#" + y);
				try
				{
					Navit.follow_button_on();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
			else if (msg.getData().getInt("Callback") == 49)
			{
				// set routing target to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");

				CallbackMessageChannel(49, "" + x + "#" + y);
				try
				{
					Navit.follow_button_on();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
			else if (msg.getData().getInt("Callback") == 5)
			{
				String cmd = msg.getData().getString("cmd");
				CallbackMessageChannel(5, cmd);
			}
			else if (msg.getData().getInt("Callback") == 7)
			{
				CallbackMessageChannel(7, "");
			}
			else if ((msg.getData().getInt("Callback") > 7) && (msg.getData().getInt("Callback") < 21))
			{
				CallbackMessageChannel(msg.getData().getInt("Callback"), "");
			}
			else if (msg.getData().getInt("Callback") == 21)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				// ??? // ButtonCallback(1, 1, x, y); // down
			}
			else if (msg.getData().getInt("Callback") == 22)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				// ??? // ButtonCallback(0, 1, x, y); // up
			}
			else if (msg.getData().getInt("Callback") == 23)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				int x2 = msg.getData().getInt("x2");
				int y2 = msg.getData().getInt("y2");
				MotionCallback(x, y, x2, y2);
			}
			else if (msg.getData().getInt("Callback") == 24)
			{
				try
				{
					NavitGraphics.NavitMsgTv_.setVisibility(View.VISIBLE);
				}
				catch (Exception e)
				{

				}
			}
			else if (msg.getData().getInt("Callback") == 25)
			{
				try
				{
					NavitGraphics.NavitMsgTv_.setVisibility(View.INVISIBLE);
				}
				catch (Exception e)
				{

				}
			}
			else if (msg.getData().getInt("Callback") == 30)
			{
				// 2D
				// String s = msg.getData().getString("s");
				CallbackMessageChannel(30, "");
			}
			else if (msg.getData().getInt("Callback") == 31)
			{
				// 3D
				// String s = msg.getData().getString("s");
				CallbackMessageChannel(31, "");
			}
			else if (msg.getData().getInt("Callback") == 32)
			{
				// switch to specific 3D pitch
				String s = msg.getData().getString("s");
				CallbackMessageChannel(32, s);
			}
			else if (msg.getData().getInt("Callback") == 33)
			{
				// zoom to specific zoomlevel
				String s = msg.getData().getString("s");
				CallbackMessageChannel(33, s);
			}
			else if (msg.getData().getInt("Callback") == 34)
			{
				// announcer voice OFF
				CallbackMessageChannel(34, "");
			}
			else if (msg.getData().getInt("Callback") == 35)
			{
				// announcer voice ON
				CallbackMessageChannel(35, "");
			}
			else if (msg.getData().getInt("Callback") == 36)
			{
				// switch "Lock on road" ON
				CallbackMessageChannel(36, "");
			}
			else if (msg.getData().getInt("Callback") == 37)
			{
				// switch "Lock on road" OFF
				CallbackMessageChannel(37, "");
			}
			else if (msg.getData().getInt("Callback") == 38)
			{
				// switch "Northing" ON
				CallbackMessageChannel(38, "");
			}
			else if (msg.getData().getInt("Callback") == 39)
			{
				// switch "Northing" OFF
				CallbackMessageChannel(39, "");
			}
			else if (msg.getData().getInt("Callback") == 40)
			{
				// switch "Map follows Vehicle" ON
				CallbackMessageChannel(40, "");
			}
			else if (msg.getData().getInt("Callback") == 41)
			{
				// switch "Map follows Vehicle" OFF
				CallbackMessageChannel(41, "");
			}
			else if (msg.getData().getInt("Callback") == 42)
			{
				// routing mode "highways"
				CallbackMessageChannel(42, "");
			}
			else if (msg.getData().getInt("Callback") == 43)
			{
				// routing mode "normal roads"
				CallbackMessageChannel(43, "");
			}
			else if (msg.getData().getInt("Callback") == 44)
			{
				// show duplicates in search results
				CallbackMessageChannel(44, "");
			}
			else if (msg.getData().getInt("Callback") == 45)
			{
				// filter duplicates in search results
				CallbackMessageChannel(45, "");
			}
			else if (msg.getData().getInt("Callback") == 46)
			{
				// stop searching and show results found until now
				CallbackMessageChannel(46, "");
			}
			else if (msg.getData().getInt("Callback") == 47)
			{
				// change maps data dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(47, s);
			}
			else if (msg.getData().getInt("Callback") == 50)
			{
				// we request to stop drawing the map
				CallbackMessageChannel(50, "");
			}
			else if (msg.getData().getInt("Callback") == 51)
			{
				// set position to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				CallbackMessageChannel(51, "" + x + "#" + y);
			}
			else if (msg.getData().getInt("Callback") == 52)
			{
				// switch to demo vehicle
				String s = msg.getData().getString("s");
				CallbackMessageChannel(52, s);
			}
			else if (msg.getData().getInt("Callback") == 53)
			{
				// dont speak streetnames
				CallbackMessageChannel(53, "");
			}
			else if (msg.getData().getInt("Callback") == 54)
			{
				// speak streetnames
				CallbackMessageChannel(54, "");
			}
			else if (msg.getData().getInt("Callback") == 55)
			{
				// set cache size for (map-)files
				String s = msg.getData().getString("s");
				CallbackMessageChannel(55, s);
			}
			//			else if (msg.getData().getInt("Callback") == 56)
			//			{
			//				// draw polylines with/without circles at the end
			//				String s = msg.getData().getString("s");
			//				CallbackMessageChannel(56, s); // 0 -> draw circles, 1 -> DO NOT draw circles
			//			}
			else if (msg.getData().getInt("Callback") == 57)
			{
				// keep drawing streets as if at "order" level xxx
				String s = msg.getData().getString("s");
				CallbackMessageChannel(57, s);
			}
			else if (msg.getData().getInt("Callback") == 58)
			{
				// street search radius factor (multiplier)
				String s = msg.getData().getString("s");
				CallbackMessageChannel(58, s);
			}
			else if (msg.getData().getInt("Callback") == 59)
			{
				// enable layer "name"
				String s = msg.getData().getString("s");
				CallbackMessageChannel(59, s);
			}
			else if (msg.getData().getInt("Callback") == 60)
			{
				// disable layer "name"
				String s = msg.getData().getString("s");
				CallbackMessageChannel(60, s);
			}
			else if (msg.getData().getInt("Callback") == 61)
			{
				// zoom to specific zoomlevel at given point as center
				// pixel-x#pixel-y#zoom-level
				String s = msg.getData().getString("s");
				CallbackMessageChannel(61, s);
			}
			else if (msg.getData().getInt("Callback") == 62)
			{
				// disable map drawing
				CallbackMessageChannel(62, "");
			}
			else if (msg.getData().getInt("Callback") == 63)
			{
				// enable map drawing
				CallbackMessageChannel(63, "");
			}
			else if (msg.getData().getInt("Callback") == 64)
			{
				// draw map
				CallbackMessageChannel(64, "");
			}
			else if (msg.getData().getInt("Callback") == 65)
			{
				// draw map async
				CallbackMessageChannel(65, "");
			}
			else if (msg.getData().getInt("Callback") == 66)
			{
				// enable "multipolygons"
				CallbackMessageChannel(66, "");
			}
			else if (msg.getData().getInt("Callback") == 67)
			{
				// disable "multipolygons"
				CallbackMessageChannel(67, "");
			}
			else if (msg.getData().getInt("Callback") == 68)
			{
				// shift "order" by this value (only for drawing objects)
				String s = msg.getData().getString("s");
				CallbackMessageChannel(68, s);
			}
			else if (msg.getData().getInt("Callback") == 69)
			{
				// stop drawing map
				CallbackMessageChannel(69, "");
			}
			else if (msg.getData().getInt("Callback") == 70)
			{
				// allow drawing map
				CallbackMessageChannel(70, "");
			}
			else if (msg.getData().getInt("Callback") == 71)
			{
				// activate/deactivate "route graph" display
				// 0 -> deactivate
				// 1 -> activate
				String s = msg.getData().getString("s");
				CallbackMessageChannel(71, s);
			}
			else if (msg.getData().getInt("Callback") == 72)
			{
				// update the route path and route graph (e.g. after setting new roadblocks)
				// does not update destinations!!!
				CallbackMessageChannel(72, "");
			}
			else if (msg.getData().getInt("Callback") == 73)
			{
				// update the route path and route graph (e.g. after setting new roadblocks)
				// this destroys the route graph and calcs everything totally new!
				CallbackMessageChannel(73, "");
			}

			else if (msg.getData().getInt("Callback") == 74)
			{
				// allow demo vechile to move
				CallbackMessageChannel(74, "");
			}
			else if (msg.getData().getInt("Callback") == 75)
			{
				// stop demo vechile
				CallbackMessageChannel(75, "");
			}
			else if (msg.getData().getInt("Callback") == 76)
			{
				// show route rectangles
				CallbackMessageChannel(76, "");
			}
			else if (msg.getData().getInt("Callback") == 77)
			{
				// do not show route rectangles
				CallbackMessageChannel(77, "");
			}
			else if (msg.getData().getInt("Callback") == 78)
			{
				// shift layout "order" values
				String s = msg.getData().getString("s");
				CallbackMessageChannel(78, s);
			}
			else if (msg.getData().getInt("Callback") == 79)
			{
				// set traffic light delay/cost
				String s = msg.getData().getString("s");
				CallbackMessageChannel(79, s);
			}
			else if (msg.getData().getInt("Callback") == 80)
			{
				// set autozoom flag to 0 or 1
				String s = msg.getData().getString("s");
				CallbackMessageChannel(80, s);
			}
			else if (msg.getData().getInt("Callback") == 81)
			{
				// resize layout items by factor
				String s = msg.getData().getString("s");
				CallbackMessageChannel(81, s);
			}
			else if (msg.getData().getInt("Callback") == 82)
			{
				// report share dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(82, s);
			}
			else if (msg.getData().getInt("Callback") == 83)
			{
				// spill all the index files to log output
				CallbackMessageChannel(83, "");
			}
			else if (msg.getData().getInt("Callback") == 84)
			{
				// report data dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(84, s);
			}
		}
	};

	public static native void TimeoutCallback(int del, int id);

	public static native void SizeChangedCallbackReal(int w, int h);

	public static void SizeChangedCallback(int w, int h)
	{
		Navit.cwthr.SizeChangedCallback(w, h);
	}

	// public native void ButtonCallback(int pressed, int button, int x, int y);

	public static native void MotionCallbackReal(int x1, int y1, int x2, int y2, int draw);

	public static void MotionCallback(int x1, int y1, int x2, int y2)
	{
		Navit.cwthr.MotionCallback((int) (x1 * Global_dpi_factor), (int) (y1 * Global_dpi_factor), (int) (x2 * Global_dpi_factor), (int) (y2 * Global_dpi_factor));
	}

	// public native void KeypressCallback(String s);

	// private int SizeChangedCallbackID, ButtonCallbackID, MotionCallbackID, KeypressCallbackID;

	// private int count;
	/*
	 * public void setSizeChangedCallback(int id)
	 * {
	 * SizeChangedCallbackID = id;
	 * }
	 */

	/*
	 * public void setButtonCallback(int id)
	 * {
	 * ButtonCallbackID = id;
	 * }
	 */

	/*
	 * public void setMotionCallback(int id)
	 * {
	 * MotionCallbackID = id;
	 * Navit.setMotionCallback(id, this);
	 * }
	 */

	/*
	 * public void setKeypressCallback(int id)
	 * {
	 * KeypressCallbackID = id;
	 * // set callback id also in main intent (for menus)
	 * Navit.setKeypressCallback(id, this);
	 * }
	 */

	public void NavitSetGrObj()
	{
		// set the (static) graphics object (this is bad, please fix me!!)
		// **disabled** Navit.N_NavitGraphics = this;
	}

	protected void draw_polyline(Paint paint, int c[])
	{
		//	Log.e("NavitGraphics","draw_polyline");
		paint.setStyle(Paint.Style.STROKE);
		b_paint_antialias = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		//paint.setStrokeWidth(0);
		b_paint_path.reset();
		b_paint_path.moveTo(c[0], c[1]);
		for (int i = 2; i < c.length; i += 2)
		{
			b_paint_path.lineTo(c[i], c[i + 1]);
		}
		//global_path.close();
		draw_canvas.drawPath(b_paint_path, paint);
		paint.setAntiAlias(b_paint_antialias);
		//paint.setPathEffect(dashed_map_lines__no_dash);
	}

	protected void draw_polyline2__NEW(Paint paint, int c[], int order, int oneway)
	{
		if (dl_thread[0] == null)
		{
			int ii = 0;
			for (ii = 0; ii < NavitGraphics.dl_thread_max; ii++)
			{
				NavitGraphics.dl_thread[ii] = new NavitGraphics.DrawLinesThread(ii);
				NavitGraphics.dl_thread[ii].start();
			}
		}

		if (dl_thread_cur + 1 < dl_thread_max)
		{
			dl_thread_cur++;
		}
		else
		{
			dl_thread_cur = 0;
		}
		dl_thread[dl_thread_cur].add_lines3(paint, c, order, oneway);
	}

	static void draw_polyline2_threads(Paint paint, int c[], int order, int oneway)
	{
		if (!Navit.PREF_gui_oneway_arrows)
		{
			return;
		}

		// line less than 44px -> dont draw arrow!
		int len = (c[0] - c[2]) * (c[1] - c[3]);
		if (len < 0)
		{
			len = -len;
		}
		if (len > (2000))
		{
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(Navit.PREF_use_anti_aliasing);

			// create matrix for the manipulation
			Matrix matrix_oneway_arrows2 = new Matrix();

			int middle_x = c[0] + (int) ((c[2] - c[0]) / 2);
			int middle_y = c[1] + (int) ((c[3] - c[1]) / 2);
			double d_x = ((c[2] - c[0]) / 6);
			double d_y = ((c[3] - c[1]) / 6);
			int angle = (int) (Math.toDegrees(Math.atan2(d_y, d_x)));
			// System.out.println("arrow angle=" + angle);
			matrix_oneway_arrows2.postTranslate(-Navit.oneway_arrow.getWidth() / 2, -Navit.oneway_arrow.getHeight() / 2);

			if (oneway == 1)
			{
				// rotate the Bitmap
				matrix_oneway_arrows2.postRotate(angle);
			}
			else if (oneway == 2)
			{
				// rotate the Bitmap
				matrix_oneway_arrows2.postRotate(angle + 180);
			}

			if (oneway > 0)
			{
				if (c.length == 4)
				{
					matrix_oneway_arrows2.postTranslate(middle_x, middle_y);
					draw_canvas_s.drawBitmap(Navit.oneway_arrow, matrix_oneway_arrows2, paint);
				}
			}
			else
			{
				return;
			}
		}
	}

	protected void draw_polyline2(Paint paint, int c[], int order, int oneway)
	{
		// Log.e("NavitGraphics", "draw_polyline2 count=" + c.length);
		if (!Navit.PREF_gui_oneway_arrows)
		{
			return;
		}

		// Boolean normal = false;
		// Matrix matrix = null;

		// this gets already checked in c-source!!
		//if (order > DRAW_ONEWAY_ARROWS_AT_ORDER)
		//{

		// line less than 44px -> dont draw arrow!
		int len = (c[0] - c[2]) * (c[1] - c[3]);
		if (len < 0)
		{
			len = -len;
		}

		if (len > (1600))
		{
			paint.setStyle(Paint.Style.STROKE);
			b_paint_antialias = paint.isAntiAlias();
			paint.setAntiAlias(Navit.PREF_use_anti_aliasing);

			// create matrix for the manipulation
			matrix_oneway_arrows = new Matrix();

			// calc this in c-code !! will be much faster!!
			// calc this in c-code !! will be much faster!!
			// calc this in c-code !! will be much faster!!
			double d_x = ((c[2] - c[0]));
			double d_y = ((c[3] - c[1]));
			int middle_x = c[0] + (int) (d_x / 6);
			int middle_y = c[1] + (int) (d_y / 6);
			int angle = (int) (Math.toDegrees(Math.atan2(d_y, d_x)));
			// calc this in c-code !! will be much faster!!
			// calc this in c-code !! will be much faster!!
			// calc this in c-code !! will be much faster!!

			// System.out.println("arrow angle=" + angle);
			matrix_oneway_arrows.postTranslate(-Navit.oneway_arrow.getWidth() / 2, -Navit.oneway_arrow.getHeight() / 2);

			//System.out.println("order=" + order);
			// resize the Bitmap
			if (order > 16)
			{
				matrix_oneway_arrows.postScale(4.5f, 4.5f);
			}
			else if (order > 14)
			{
				matrix_oneway_arrows.postScale(1.5f, 1.5f);
			}

			if (oneway == 1)
			{
				// rotate the Bitmap
				matrix_oneway_arrows.postRotate(angle);
			}
			else if (oneway == 2)
			{
				// rotate the Bitmap
				matrix_oneway_arrows.postRotate(angle + 180);
			}

			if (oneway > 0)
			{
				if (c.length == 4)
				{
					matrix_oneway_arrows.postTranslate(middle_x, middle_y);
					draw_canvas.drawBitmap(Navit.oneway_arrow, matrix_oneway_arrows, paint);
				}

				paint.setAntiAlias(b_paint_antialias);

			}
			else
			{
				// normal = true;
				return;
			}
		}
		//}
		//else
		//{
		//	// normal = true;
		//	// draw nothing, just return!
		//	return;
		//}

		//paint.setPathEffect(dashed_map_lines__no_dash);
	}

	public static class draw_object
	{
		int type;
		Paint paint;

		// -- 99 - "ready" signal
		// -- 0 -- normal line
		int[] c;
		int order;
		int width;
		// -- 2 -- tunnel/bridge line
		int line_type;

		// -- 1 -- text
		int x;
		int y;
		String text;
		int size;
		int dx;
		int dy;
	}

	public static class DrawLinesThread extends Thread
	{
		private Boolean running;
		private Boolean start_drawing;
		private final LinkedBlockingQueue<draw_object> queue = new LinkedBlockingQueue<draw_object>();

		int i = 0;
		private draw_object l2;
		int thread_num;
		int counter = 0;

		int m_x;
		int m_y;
		int m_x2;
		int m_y2;
		Boolean m_c = false;

		DrawLinesThread(int thread_num)
		{
			this.thread_num = thread_num;
			this.running = true;
			this.start_drawing = false;
		}

		public void motion_callback(int x, int y, int x2, int y2)
		{
			m_x = x;
			m_y = y;
			m_x2 = x2;
			m_y2 = y2;
			m_c = true;
		}

		public void add_text(Paint paint, int x, int y, String text, int size, int dx, int dy)
		{
			draw_object l = new draw_object();
			l.type = 1; // text
			l.x = x;
			l.y = y;
			l.text = text;
			l.size = size;
			l.dx = dx;
			l.dy = dy;
			// -- paint --
			l.paint = new Paint();
			l.paint.setColor(paint.getColor());
			l.paint.setAlpha(paint.getAlpha());
			l.paint.setTypeface(paint.getTypeface());
			// l.paint.setPathEffect(paint.getPathEffect());
			// -- paint --
			queue.offer(l);
		}

		public void add_lines(Paint paint, int c[], int order, int width)
		{
			draw_object l = new draw_object();
			l.type = 0; // line
			l.c = c;
			// -- paint --
			l.paint = new Paint();
			l.paint.setColor(paint.getColor());
			l.paint.setAlpha(paint.getAlpha());
			l.paint.setPathEffect(paint.getPathEffect());
			// -- paint --
			l.order = order;
			l.width = width;
			queue.offer(l);
		}

		public void add_lines3(Paint paint, int c[], int order, int oneway)
		{
			draw_object l = new draw_object();
			l.type = 3; // line (one way arrows)
			l.c = c;
			// -- paint --
			l.paint = new Paint();
			l.paint.setColor(paint.getColor());
			l.paint.setAlpha(paint.getAlpha());
			l.paint.setPathEffect(paint.getPathEffect());
			// -- paint --
			l.order = order;
			l.width = oneway; // misuse this field here!!
			queue.offer(l);
		}

		public void add_lines2(Paint paint, int c[], int order, int width, int line_type)
		{
			draw_object l = new draw_object();

			if (line_type == 98)
			{
				// clear queue
				queue.clear();
				// stop drawing
				start_drawing = false;
				// and wake up from sleeping
				this.interrupt();
				return;
			}
			if (line_type == 97)
			{
				// start drawing from queue
				//System.out.println("start drawing");
				start_drawing = true;
				// and wake up from sleeping
				this.interrupt();
				return;
			}
			else if (line_type > 90)
			{
				l.type = line_type; // "***" signal
			}
			else
			{
				l.type = 2; // bridge/tunnel line
				l.c = c;
				l.line_type = line_type;
				// -- paint --
				l.paint = new Paint();
				l.paint.setColor(paint.getColor());
				l.paint.setAlpha(paint.getAlpha());
				l.paint.setPathEffect(paint.getPathEffect());
				// -- paint --
				l.order = order;
				l.width = width;
			}
			queue.offer(l);
		}

		public void stop_me()
		{
			this.running = false;
		}

		public void run()
		{
			while (this.running)
			{

				if (m_c)
				{
					NavitGraphics.MotionCallback(m_x, m_y, m_x2, m_y2);
					m_c = false;
				}

				counter = 0;
				// while ((start_drawing) && (queue.size() > 0))
				while (queue.size() > 0)
				{
					//					if (counter > 20000)
					//					{
					//						// give a breather
					//						try
					//						{
					//							Thread.sleep(10);
					//						}
					//						catch (InterruptedException e)
					//						{
					//							// e.printStackTrace();
					//						}
					//						counter = 0;
					//					}

					try
					{
						// blocking call
						l2 = queue.take();
						// non-blocking call
						// l2 = queue.poll();
						if (l2 != null)
						{
							switch (l2.type)
							{
							case 0:
								draw_polyline3_threads(l2.paint, l2.c, l2.order, l2.width);
								counter++;
								break;
							case 1:
								draw_text_threads(l2.paint, l2.x, l2.y, l2.text, l2.size, l2.dx, l2.dy);
								counter++;
								break;
							case 2:
								draw_polyline4_threads(l2.paint, l2.c, l2.order, l2.width, l2.line_type);
								counter++;
								break;
							case 3:
								draw_polyline2_threads(l2.paint, l2.c, l2.order, l2.width);
								counter++;
								break;
							case 96:
								//System.out.println("refresh map (layers)");
								// draw_map_one_shot = true;
								//copy_map_buffer();
								//draw_reset_factors = true;
								//view_s.postInvalidate();
								break;
							case 99:
								//System.out.println("refresh map OLDOLD");
								// draw_map_one_shot = true;
								copy_map_buffer();
								draw_reset_factors = true;
								//System.out.println("invalidate 015");
								view_s.postInvalidate();
								//view_s.paint_me();
								try
								{
									Thread.sleep(800);
								}
								catch (InterruptedException e)
								{
									// e.printStackTrace();
								}
								break;
							}
						}
					}
					catch (Exception e)
					{
						//	e.printStackTrace();
						//	// System.out.println("" + this.thread_num + " *ERR*");
					}
				}

				//if (counter > 0)
				//{
				//	// System.out.println("" + this.thread_num + " counter=" + counter);
				//	view_s.postInvalidate();
				//}

				try
				{
					Thread.sleep(60);
				}
				catch (InterruptedException e)
				{
					// e.printStackTrace();
				}
			}
		}
	}

	protected void draw_polyline3___NEW(Paint paint, int c[], int order, int width)
	{
		if (dl_thread[0] == null)
		{
			int ii = 0;
			for (ii = 0; ii < NavitGraphics.dl_thread_max; ii++)
			{
				NavitGraphics.dl_thread[ii] = new NavitGraphics.DrawLinesThread(ii);
				NavitGraphics.dl_thread[ii].start();
			}
		}

		if (dl_thread_cur + 1 < dl_thread_max)
		{
			dl_thread_cur++;
		}
		else
		{
			dl_thread_cur = 0;
		}
		dl_thread[dl_thread_cur].add_lines(paint, c, order, width);
	}

	static void draw_polyline3_threads(Paint paint, int c[], int order, int width)
	{
		//	Log.e("NavitGraphics","draw_polyline3_threads");
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		paint.setStrokeWidth(width);
		// *******************
		// *******************
		// ****** paint.setColor(Color.RED);
		// *******************
		// *******************

		if (order > DRAW_MORE_DETAILS_AT_ORDER)
		{
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(0);
			draw_canvas_s.drawCircle(c[0], c[1], (width / 2), paint);
		}
		for (int i = 2; i < c.length; i += 2)
		{
			if (order > DRAW_MORE_DETAILS_AT_ORDER)
			{
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(0);
				draw_canvas_s.drawCircle(c[i], c[i + 1], (width / 2), paint);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(width);
			}
			draw_canvas_s.drawLine(c[i - 2], c[i - 1], c[i], c[i + 1], paint);
		}
	}

	// draw normal polylines -> this function gets called the most!! XX-thousand times
	// draw normal polylines -> this function gets called the most!! XX-thousand times
	// draw normal polylines -> this function gets called the most!! XX-thousand times
	protected void draw_polyline3(int c[], int order, int width, int dashes, int r, int g, int b, int a)
	{

		Paint paint2 = new Paint();
		paint2.setARGB(a, r, g, b);
		set_dashes(paint2, dashes, order);

		//Log.e("NavitGraphics","draw_polyline3");
		paint2.setStyle(Paint.Style.STROKE);
		//b_paint_antialias = paint.isAntiAlias();
		paint2.setAntiAlias(Navit.PREF_use_anti_aliasing);
		//wsave_003 = paint.getStrokeWidth();
		paint2.setStrokeWidth(width);

		if (order > DRAW_MORE_DETAILS_AT_ORDER)
		{
			paint2.setStyle(Paint.Style.FILL);
			paint2.setStrokeWidth(0);
			draw_canvas.drawCircle(c[0], c[1], (width / 2), paint2);
		}
		for (int i = 2; i < c.length; i += 2)
		{
			if (order > DRAW_MORE_DETAILS_AT_ORDER)
			{
				//if (i < (c.length - 2))
				//{
				paint2.setStyle(Paint.Style.FILL);
				paint2.setStrokeWidth(0);
				draw_canvas.drawCircle(c[i], c[i + 1], (width / 2), paint2);
				//}
				paint2.setStyle(Paint.Style.STROKE);
				paint2.setStrokeWidth(width);
			}
			draw_canvas.drawLine(c[i - 2], c[i - 1], c[i], c[i + 1], paint2);
		}
		//paint.setAntiAlias(b_paint_antialias);
		//paint.setStrokeWidth(wsave_003);
	}

	// draw normal polylines -> this function gets called the most!! XX-thousand times
	// draw normal polylines -> this function gets called the most!! XX-thousand times
	// draw normal polylines -> this function gets called the most!! XX-thousand times

	protected void draw_polyline4(int c[], int order, int width, int type, int dashes, int r, int g, int b, int a)
	{
		// type:0 -> normal line
		// type:1 -> underground (tunnel)
		// type:2 -> bridge

		if (type > 90)
		{
			if (type == 96)
			{
				//System.out.println("refresh map (layers)");
				// draw_map_one_shot = true;
				//copy_map_buffer();
				//draw_reset_factors = true;
				//view_s.postInvalidate();
			}
			else if (type == 99)
			{
				//System.out.println("refresh map");
				//// draw_map_one_shot = true;
				//copy_map_buffer();
				//draw_reset_factors = true;
				//view_s.postInvalidate();
			}
			else if (type == 95)
			{
				//System.out.println("cancel map");
				// draw_map_one_shot = true;
				//copy_backwards_map_buffer();
				//draw_reset_factors = true;
				//view_s.postInvalidate();
			}
			return;
		}

		Paint paint2 = new Paint();
		paint2.setARGB(a, r, g, b);
		set_dashes(paint2, dashes, order);

		//b_paint_antialias = paint.isAntiAlias();
		paint2.setAntiAlias(Navit.PREF_use_anti_aliasing);
		//wsave_004 = paint.getStrokeWidth();

		if (order <= DRAW_MORE_DETAILS_TUNNEL_BRIDGES_AT_ORDER)
		{
			type = 0;
		}

		if (type == 2)
		{
			// bridge
			//
			//int csave = paint.getColor();
			paint2.setAlpha(120); // 0 .. 255   //    255 -> no seethru
			paint2.setStyle(Paint.Style.STROKE);
			paint2.setStrokeWidth(width + 2);
			// paint.setColor(Color.BLACK);
			if (order > DRAW_DETAIL_DASHES_AT_ORDER)
			{
				paint2.setStrokeWidth(width + 4);
			}
			for (int i = 2; i < c.length; i += 2)
			{
				draw_canvas.drawLine(c[i - 2], c[i - 1], c[i], c[i + 1], paint2);
			}
			//paint.setColor(csave);

			// -- circles --
			/*
			 * paint.setAlpha(120);
			 * paint.setStyle(Paint.Style.FILL);
			 * paint.setStrokeWidth(0);
			 * draw_canvas.drawCircle(c[0], c[1], (width / 2), paint);
			 * for (int i = 2; i < c.length; i += 2)
			 * {
			 * paint.setStyle(Paint.Style.FILL);
			 * paint.setStrokeWidth(0);
			 * draw_canvas.drawCircle(c[i], c[i + 1], (width / 2), paint);
			 * }
			 */
			// -- circles --
		}

		// ---------------------------------------
		paint2.setStyle(Paint.Style.STROKE);
		paint2.setStrokeWidth(width);

		if (type == 1)
		{
			// tunnel
			//paint2.setAlpha(70); // 0 .. 255   //    255 -> no seethru
			paint2.setAlpha(150); // 0 .. 255   //    255 -> no seethru
			if (order > DRAW_DETAIL_DASHES_AT_ORDER)
			{
				paint2.setPathEffect(dashed_map_lines__low);
			}
			else
			{
				paint2.setPathEffect(dashed_map_lines__high);
			}
		}
		else if (type == 2)
		{
			// bridge
			paint2.setAlpha(70); // 0 .. 255   //    255 -> no seethru			
		}

		paint2.setStyle(Paint.Style.STROKE);
		paint2.setStrokeWidth(width);
		for (int i = 2; i < c.length; i += 2)
		{
			draw_canvas.drawLine(c[i - 2], c[i - 1], c[i], c[i + 1], paint2);
		}

		//paint.setPathEffect(dashed_map_lines__no_dash);
		//paint.setAntiAlias(b_paint_antialias);
		//paint.setStrokeWidth(wsave_004);
	}

	protected void draw_polyline4__NEW(Paint paint, int c[], int order, int width, int type)
	{
		if (dl_thread[0] == null)
		{
			int ii = 0;
			for (ii = 0; ii < NavitGraphics.dl_thread_max; ii++)
			{
				NavitGraphics.dl_thread[ii] = new NavitGraphics.DrawLinesThread(ii);
				NavitGraphics.dl_thread[ii].start();
			}
		}

		if (dl_thread_cur + 1 < dl_thread_max)
		{
			dl_thread_cur++;
		}
		else
		{
			dl_thread_cur = 0;
		}
		dl_thread[dl_thread_cur].add_lines2(paint, c, order, width, type);
	}

	// for bridge or tunnel this function is used
	static void draw_polyline4_threads(Paint paint, int c[], int order, int width, int type)
	{
		// type:0 -> normal line
		// type:1 -> underground (tunnel)
		// type:2 -> bridge

		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);

		if (order <= DRAW_MORE_DETAILS_TUNNEL_BRIDGES_AT_ORDER)
		{
			type = 0;
		}

		if (type == 2)
		{
			// bridge
			//
			//int csave = paint.getColor();
			paint.setAlpha(120); // 0 .. 255   //    255 -> no seethru
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(width + 2);
			// paint.setColor(Color.BLACK);
			if (order > DRAW_DETAIL_DASHES_AT_ORDER)
			{
				paint.setStrokeWidth(width + 4);
			}
			for (int i = 2; i < c.length; i += 2)
			{
				draw_canvas_s.drawLine(c[i - 2], c[i - 1], c[i], c[i + 1], paint);
			}
			//paint.setColor(csave);

			// -- circles --
			/*
			 * paint.setAlpha(120);
			 * paint.setStyle(Paint.Style.FILL);
			 * paint.setStrokeWidth(0);
			 * draw_canvas.drawCircle(c[0], c[1], (width / 2), paint);
			 * for (int i = 2; i < c.length; i += 2)
			 * {
			 * paint.setStyle(Paint.Style.FILL);
			 * paint.setStrokeWidth(0);
			 * draw_canvas.drawCircle(c[i], c[i + 1], (width / 2), paint);
			 * }
			 */
			// -- circles --
		}

		// ---------------------------------------
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(width);

		if (type == 1)
		{
			// tunnel
			paint.setAlpha(70); // 0 .. 255   //    255 -> no seethru
			if (order > DRAW_DETAIL_DASHES_AT_ORDER)
			{
				paint.setPathEffect(dashed_map_lines__low);
			}
			else
			{
				paint.setPathEffect(dashed_map_lines__high);
			}
		}
		else if (type == 2)
		{
			// bridge
			paint.setAlpha(70); // 0 .. 255   //    255 -> no seethru			
		}

		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(width);
		for (int i = 2; i < c.length; i += 2)
		{
			draw_canvas_s.drawLine(c[i - 2], c[i - 1], c[i], c[i + 1], paint);
		}
	}

	// for bridge or tunnel this function is used

	protected void set_dashes(Paint paint, int variant, int order)
	{
		if (variant == 0)
		{
			paint.setPathEffect(dashed_map_lines__no_dash);
			return;
		}

		if (order > DRAW_DETAIL_DASHES_AT_ORDER)
		{
			paint.setPathEffect(dashes__low[variant]);
		}
		else
		{
			paint.setPathEffect(dashes__high[variant]);
		}
	}

	protected void draw_polyline_dashed(Paint paint, int c[], int order, int oneway)
	{
		// emtpy dummy for C-Code
	}

	protected void draw_polyline_dashed______UNUSED(Paint paint, int c[], int order, int oneway)
	{
		//
		//
		// !! this function is unsed now !!
		//
		//

		paint.setStyle(Paint.Style.STROKE);
		b_paint_antialias = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);

		Boolean normal = false;

		// create matrix for the manipulation
		Matrix matrix = new Matrix();

		int middle_x = c[0] + (int) ((c[2] - c[0]) / 2);
		int middle_y = c[1] + (int) ((c[3] - c[1]) / 2);
		double d_x = ((c[2] - c[0]) / 6);
		double d_y = ((c[3] - c[1]) / 6);
		int angle = (int) (Math.toDegrees(Math.atan2(d_y, d_x)));
		matrix.postTranslate(-Navit.oneway_arrow.getWidth() / 2, -Navit.oneway_arrow.getHeight() / 2);

		if (order > DRAW_ONEWAY_ARROWS_AT_ORDER)
		{
			if (oneway == 1)
			{
				if (!Navit.PREF_gui_oneway_arrows)
				{
					return;
				}
				// rotate the Bitmap
				matrix.postRotate(angle);
			}
			else if (oneway == 2)
			{
				if (!Navit.PREF_gui_oneway_arrows)
				{
					return;
				}
				// rotate the Bitmap
				matrix.postRotate(angle + 180);
			}

			if (oneway > 0)
			{
				if (c.length == 4)
				{
					matrix.postTranslate(middle_x, middle_y);
					draw_canvas.drawBitmap(Navit.oneway_arrow, matrix, paint);
				}
			}
			else
			{
				normal = true;
			}
		}
		else
		{
			normal = true;
		}

		if (normal)
		{
			// normal line
			if (order > DRAW_DETAIL_DASHES_AT_ORDER)
			{
				paint.setPathEffect(dashed_map_lines__low);
			}
			else
			{
				paint.setPathEffect(dashed_map_lines__high);
			}
			b_paint_path.reset();
			b_paint_path.moveTo(c[0], c[1]);
			for (int i = 2; i < c.length; i += 2)
			{
				b_paint_path.lineTo(c[i], c[i + 1]);
			}

			draw_canvas.drawPath(b_paint_path, paint);
			paint.setPathEffect(dashed_map_lines__no_dash);
		}
		paint.setAntiAlias(b_paint_antialias);
	}

	protected void draw_polygon(Paint paint, int c[])
	{
		paint.setStyle(Paint.Style.FILL);
		b_paint_antialias = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		b_paint_path.reset();
		b_paint_path.moveTo(c[0], c[1]);
		for (int i = 2; i < c.length; i += 2)
		{
			b_paint_path.lineTo(c[i], c[i + 1]);
		}
		draw_canvas.drawPath(b_paint_path, paint);
		paint.setAntiAlias(b_paint_antialias);
	}

	protected void draw_polygon2(Paint paint, int c[], int order, int oneway)
	{
		paint.setStyle(Paint.Style.FILL);
		b_paint_antialias = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		b_paint_path.reset();
		b_paint_path.moveTo(c[0], c[1]);
		for (int i = 2; i < c.length; i += 2)
		{
			b_paint_path.lineTo(c[i], c[i + 1]);
		}

		if (order > DRAW_ONEWAY_ARROWS_AT_ORDER)
		{
			if (oneway == 1)
			{
				paint.setColor(Color.RED);
				if (!Navit.PREF_gui_oneway_arrows)
				{
					return;
				}
			}
			else if (oneway == 2)
			{
				paint.setColor(Color.BLUE);
				if (!Navit.PREF_gui_oneway_arrows)
				{
					return;
				}
			}
		}

		draw_canvas.drawPath(b_paint_path, paint);
		paint.setAntiAlias(b_paint_antialias);
	}

	protected void draw_rectangle(Paint paint, int x, int y, int w, int h)
	{
		//Log.e("NavitGraphics","draw_rectangle");
		Rect r = new Rect(x, y, x + w, y + h);
		paint.setStyle(Paint.Style.FILL);
		b_paint_antialias = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		//paint.setStrokeWidth(0);d
		draw_canvas.drawRect(r, paint);
		paint.setAntiAlias(b_paint_antialias);
	}

	protected void draw_circle(Paint paint, int x, int y, int r)
	{
		// Log.e("NavitGraphics", "draw_circle " + x + " " + y + " " + r);
		//		float fx = x;
		//		float fy = y;
		//		float fr = r / 2;
		paint.setStyle(Paint.Style.STROKE);
		b_paint_antialias = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		draw_canvas.drawCircle(x, y, r / 2, paint);
		paint.setAntiAlias(b_paint_antialias);
	}

	protected void draw_text__NEW(Paint paint, int x, int y, String text, int size, int dx, int dy)
	{
		if (dl_thread[0] == null)
		{
			int ii = 0;
			for (ii = 0; ii < NavitGraphics.dl_thread_max; ii++)
			{
				NavitGraphics.dl_thread[ii] = new NavitGraphics.DrawLinesThread(ii);
				NavitGraphics.dl_thread[ii].start();
			}
		}

		if (dl_thread_cur + 1 < dl_thread_max)
		{
			dl_thread_cur++;
		}
		else
		{
			dl_thread_cur = 0;
		}

		// FONT ------------------
		// FONT ------------------
		if (Navit.PREF_use_custom_font == true)
		{
			if (paint.getTypeface() == null)
			{
				try
				{
					paint.setTypeface(Navit.NavitStreetnameFont);
					//System.out.println("Calling setTypeface");
				}
				catch (Exception e)
				{
					//e.printStackTrace();
				}
			}
		}
		else
		{
			if (paint.getTypeface() != null)
			{
				paint.setTypeface(null);
			}
		}
		// FONT ------------------
		// FONT ------------------

		dl_thread[dl_thread_cur].add_text(paint, x, y, text, size, dx, dy);
	}

	static void draw_text_threads(Paint paint, int x, int y, String text, int size, int dx, int dy)
	{
		//		float fx = x;
		//		float fy = y;
		//Log.e("NavitGraphics","Text size "+size + " vs " + paint.getTextSize());
		if (Navit.PREF_map_font_size != 2)
		{
			if (Navit.PREF_map_font_size == 3)
			{
				// large
				paint.setTextSize((int) ((size / 15) * 1.4));
			}
			else if (Navit.PREF_map_font_size == 4)
			{
				// extra large
				paint.setTextSize((int) ((size / 15) * 1.7));
			}
			else if (Navit.PREF_map_font_size == 5)
			{
				// extra large
				paint.setTextSize((int) ((size / 15) * 2.2));
			}
			else if (Navit.PREF_map_font_size == 1)
			{
				// small
				paint.setTextSize((int) ((size / 15) * 0.72));
			}
			else
			{
				// other? use normal size
				paint.setTextSize(size / 15);
			}
		}
		else
		{
			// normal size
			paint.setTextSize(size / 15);
		}
		paint.setStyle(Paint.Style.FILL);
		// FONT ------------------
		// FONT ------------------
		if (Navit.PREF_use_custom_font == true)
		{
			if (paint.getTypeface() == null)
			{
				try
				{
					paint.setTypeface(Navit.NavitStreetnameFont);
					//System.out.println("Calling setTypeface");
				}
				catch (Exception e)
				{
					//e.printStackTrace();
				}
			}
		}
		else
		{
			if (paint.getTypeface() != null)
			{
				paint.setTypeface(null);
			}
		}
		// FONT ------------------
		// FONT ------------------
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		if (dx == 0x10000 && dy == 0)
		{
			draw_canvas_s.drawText(text, x, y, paint);
		}
		else
		{
			Path b_paint_path_ = new Path();
			b_paint_path_.reset();
			b_paint_path_.moveTo(x, y);
			b_paint_path_.rLineTo(dx, dy);
			paint.setTextAlign(android.graphics.Paint.Align.LEFT);
			draw_canvas_s.drawTextOnPath(text, b_paint_path_, 0, 0, paint);
		}
	}

	protected void draw_text(Paint paint, int x, int y, String text, int size, int dx, int dy)
	{
		//		float fx = x;
		//		float fy = y;
		//Log.e("NavitGraphics","Text size "+size + " vs " + paint.getTextSize());
		if (Navit.PREF_map_font_size != 2)
		{
			if (Navit.PREF_map_font_size == 3)
			{
				// large
				paint.setTextSize((int) ((size / 15) * 1.4));
			}
			else if (Navit.PREF_map_font_size == 4)
			{
				// extra large
				paint.setTextSize((int) ((size / 15) * 1.7));
			}
			else if (Navit.PREF_map_font_size == 5)
			{
				// extra large
				paint.setTextSize((int) ((size / 15) * 2.2));
			}
			else if (Navit.PREF_map_font_size == 1)
			{
				// small
				paint.setTextSize((int) ((size / 15) * 0.72));
			}
			else
			{
				// other? use normal size
				paint.setTextSize(size / 15);
			}
		}
		else
		{
			// normal size
			paint.setTextSize(size / 15);
		}
		paint.setStyle(Paint.Style.FILL);
		// FONT ------------------
		// FONT ------------------
		if (Navit.PREF_use_custom_font == true)
		{
			if (paint.getTypeface() == null)
			{
				try
				{
					strokeTextPaint.setTypeface(Navit.NavitStreetnameFont);
					paint.setTypeface(Navit.NavitStreetnameFont);
					//System.out.println("Calling setTypeface");
				}
				catch (Exception e)
				{
					//e.printStackTrace();
				}
			}
		}
		else
		{
			if (paint.getTypeface() != null)
			{
				strokeTextPaint.setTypeface(null);
				paint.setTypeface(null);
			}
		}
		// FONT ------------------
		// FONT ------------------
		b_paint_antialias = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);

		if (paint.getTextSize() < 30)
		{
			strokeTextPaint.setStrokeWidth(s_strokTextSize_min);
		}
		else
		{
			strokeTextPaint.setStrokeWidth(s_strokTextSize);
		}

		paint.setTextAlign(android.graphics.Paint.Align.LEFT);

		if (dx == 0x10000 && dy == 0)
		{
			strokeTextPaint.setTextSize(paint.getTextSize());
			draw_canvas.drawText(text, x, y, strokeTextPaint);
			draw_canvas.drawText(text, x, y, paint);
		}
		else
		{
			b_paint_path.reset();
			b_paint_path.moveTo(x, y);
			b_paint_path.rLineTo(dx, dy);

			strokeTextPaint.setTextSize(paint.getTextSize());
			draw_canvas.drawTextOnPath(text, b_paint_path, 0, 0, strokeTextPaint);
			draw_canvas.drawTextOnPath(text, b_paint_path, 0, 0, paint);
		}
		paint.setAntiAlias(b_paint_antialias);
	}

	protected void draw_image(Paint paint, int x, int y, Bitmap bitmap)
	{
		//Log.e("NavitGraphics","draw_image");
		//		float fx = x;
		//		float fy = y;
		//System.out.println("DO__DRAW:draw_image:drawBitmap start");
		draw_canvas.drawBitmap(bitmap, x, y, paint);
		//System.out.println("DO__DRAW:draw_image:drawBitmap end");
	}

	protected void draw_warp__YYY(String imagepath, int count, int p0x, int p0y, int p1x, int p1y, int p2x, int p2y)
	{
		// dummy -> do nothing!!
	}

	// ", "(Ljava/lang/String;IIIIIIIII)V
	protected void draw_warp(String imagepath, int count, int p0x, int p0y, int p1x, int p1y, int p2x, int p2y)
	{
		//System.out.println("draw_warp: image=" + imagepath + " count=" + count);
		//System.out.println("draw_warp: p0x=" + p0x + ", p0y=" + p0y + ", p1x=" + p1x + ", p1y=" + p1y + ", p2x=" + p2x + ", p2y=" + p2y);

		final int map_tile_x = 256;
		final int map_tile_y = 256;

		// orig map tile size = 256px * 256px
		// p0(x,y)=position of left lower corner of image!! 

		/*
		 * void imlib_render_image_on_drawable_skewed(int source_x, int source_y,
		 * int source_width,
		 * int source_height,
		 * int destination_x,
		 * int destination_y,
		 * int h_angle_x, int h_angle_y,
		 * int v_angle_x, int v_angle_y);
		 */

		try
		{
			Bitmap bitmap = null;

			//if (imagepath.startsWith("6/"))
			//{
			//	bitmap = BitmapFactory.decodeFile(Navit.MAP_FILENAME_PATH + "/" + imagepath);
			//}
			//else
			//{
			try
			{
				InputStream infile = Navit.asset_mgr.open(imagepath);
				bitmap = BitmapFactory.decodeStream(infile);
				infile.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			//}

			if (count == 3)
			{
				// imlib_render_image_on_drawable_skewed(0, 0, w, h, p[0].x, p[0].y, p[1].x-p[0].x, p[1].y-p[0].y, p[2].x-p[0].x, p[2].y-p[0].y);
				//matrix.setRotate(90.0f); // Degrees
				float new_len = (float) (Math.sqrt(((p1x - p0x) * (p1x - p0x)) + ((p1y - p0y) * (p1y - p0y))));
				float scale_x_y = new_len / map_tile_y;
				float deltaY = -p1y + p0y;
				float deltaX = p1x - p0x;
				float angle_deg = -(float) (Math.atan2(deltaY, deltaX) * 180d / Math.PI);
				//System.out.println("_warp: angle=" + angle_deg);
				matrix_maptile.reset();
				matrix_maptile.postTranslate(p0x, p0y - (map_tile_y * scale_x_y));
				matrix_maptile.preScale(scale_x_y, scale_x_y);
				matrix_maptile.postRotate(angle_deg, p0x, p0y);
			}
			else
			{
				bitmap.recycle();
				bitmap = null;
				return;
			}

			draw_canvas.drawBitmap(bitmap, matrix_maptile, paint_maptile);
			bitmap.recycle();
			bitmap = null;
		}
		catch (Exception e)
		{

		}
	}

	protected void draw_bigmap(int yaw, int order, float clng, float clat, int x_, int y_, int mcx, int mcy, int px_, int py_, int valid)
	{
		Bitmap bigmap_bitmap = null;

		if (bigmap_bitmap != null)
		{
			// input: x,y --> screen coords of lat=0,lng=0 point!!
			/*
			 * int scx = (int) (draw_canvas.getWidth() / 2);
			 * int scy = (int) (draw_canvas.getHeight() / 2);
			 * int px = px_;
			 * int py = py_;
			 */

			// calculate the scale
			float scaleWidth = 1;
			float scaleHeight = 1;

			/*
			 * if (order == -1)
			 * {
			 * scaleWidth = scaleHeight = 0.8f * (1.5f / draw_factor);
			 * }
			 * else if (order == -2)
			 * {
			 * scaleWidth = scaleHeight = (float) ((1 / 2f) * 0.8f) * (1.5f / draw_factor);
			 * }
			 */

			//System.out.println("bigmap order:" + (long) (order * 100));
			scaleWidth = scaleHeight = BIGMAP_FACTOR / (float) order * (1.5f / draw_factor);
			//System.out.println("draw_factor:" + draw_factor);

			if ((scaleWidth == 0) || (scaleHeight == 0))
			{
				//System.out.println(" " + scaleWidth + " " + scaleHeight + " " + order);
				return;
			}

			if (valid == 1)
			{
				//System.out.println(" px " + px + " py " + py);
			}

			// create a matrix for the manipulation
			Matrix matrix = new Matrix();
			int half_x = (int) (bigmap_bitmap.getWidth() / 2);
			int half_y = (int) (bigmap_bitmap.getHeight() / 2);
			matrix.setScale(scaleWidth, scaleHeight, half_x, half_y);

			int usedMegs;
			String usedMegsString;
			//			int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			//			String usedMegsString = String.format("00 - Memory Used: %d MB", usedMegs);
			//			System.out.println("" + usedMegsString);

			// recreate the new Bitmap
			try
			{
				bigmap_bitmap_temp = Bitmap.createBitmap(bigmap_bitmap, 0, 0, bigmap_bitmap.getWidth(), bigmap_bitmap.getHeight(), matrix, true);
			}
			catch (OutOfMemoryError e)
			{
				usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);
				System.out.println("@@@@@@@@ out of VM Memory @@@@@@@@");
				System.gc();
				usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
				System.out.println("" + usedMegsString);

				try
				{
					bigmap_bitmap_temp = Bitmap.createBitmap(bigmap_bitmap, 0, 0, bigmap_bitmap.getWidth(), bigmap_bitmap.getHeight(), matrix, true);
				}
				catch (OutOfMemoryError e2)
				{
					e2.printStackTrace();
					return;
				}
			}

			if (bigmap_bitmap_temp == null)
			{
				return;
			}
			else
			{
				try
				{
					if (bigmap_bitmap_temp.getWidth() <= 0)
					{
						return;
					}
					else if (bigmap_bitmap_temp.getHeight() <= 0)
					{
						return;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					return;
				}
			}
			//			usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			//			usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
			//			System.out.println("" + usedMegsString);

			float top = 85.2f;
			//float bottom = -85.2f;
			float left = -180f;
			//float right = 180f;

			String left_top_on_screen_string = CallbackGeoCalc(2, top, left);
			String tmp[] = left_top_on_screen_string.split(":", 2);
			int pixel_top_left_x = (int) (Integer.parseInt(tmp[0]) / NavitGraphics.Global_dpi_factor);
			int pixel_top_left_y = (int) (Integer.parseInt(tmp[1]) / NavitGraphics.Global_dpi_factor);

			//float p_karte_pixel_x;
			//float p_karte_pixel_y;

			// pixel point of 0/0 geo coord
			/*
			 * p_karte_pixel_x = bigmap_bitmap_temp.getWidth() / 2;
			 * p_karte_pixel_y = bigmap_bitmap_temp.getHeight() / 2;
			 */

			/*
			 * if (!loc_12_valid)
			 * {
			 * // initialize them the first time
			 * loc_1_x = x_;
			 * loc_1_y = y_;
			 * loc_2_x = mcx;
			 * loc_2_y = mcy;
			 * loc_12_valid = true;
			 * }
			 * 
			 * if ((loc_1_x == x_) && (loc_1_y == y_))
			 * {
			 * p_karte_pixel_x = p_karte_pixel_x - (loc_2_x - mcx);
			 * p_karte_pixel_y = p_karte_pixel_y - (loc_2_y - mcy);
			 * // System.out.println("SSSSSSSS SSSSSSSS");
			 * }
			 * else
			 * {
			 * loc_2_x = mcx;
			 * loc_2_y = mcy;
			 * }
			 * 
			 * loc_1_x = x_;
			 * loc_1_y = y_;
			 */

			//int ssschx = (int) (x_ - p_karte_pixel_x);
			//int ssschy = (int) (y_ - p_karte_pixel_y);

			// fill canvas with ocean color
			draw_canvas.drawColor(Color.parseColor("#82C8EA"));

			//int green_dot_x = scx;
			//int green_dot_y = scy;
			loc_dot_valid = true;

			//int rotation_point_x = scx;
			//int rotation_point_y = scy;

			/*
			 * if (valid == 1)
			 * {
			 * green_dot_x = px;
			 * green_dot_y = py;
			 * 
			 * rotation_point_x = px;
			 * rotation_point_y = py;
			 * }
			 */
			//
			draw_canvas.save(); // SAVE
			//
			// *********** draw_canvas.translate(green_dot_x, green_dot_y);
			draw_canvas.translate(pixel_top_left_x, pixel_top_left_y);
			draw_canvas.rotate(yaw, 0, 0);

			draw_canvas.drawBitmap(bigmap_bitmap_temp, 0, 0, null);

			//
			draw_canvas.restore(); // RESTORE
			//
			bigmap_bitmap_temp.recycle();
			bigmap_bitmap_temp = null;
		}
	}

	public static void send_osd_values(String id, String text1, String text2, String text3, int i1, int i2, int i3, int i4, float f1, float f2, float f3)
	{
		//System.out.println("NavitOSDJava:" + id + " " + text1 + " " + text2 + " " + text3 + " " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + f1 + " " + f2 + " " + f3);
		//System.out.println("NavitOSDJava:" + last_paint_OSD);
		Boolean needed_value = false;
		try
		{
			if (id.equals("scale"))
			{
				if (text1 != null)
				{
					if (text1.equals("draw_rectangle1"))
					{
						Navit.OSD_scale.base = i1;
					}
					else if (text1.equals("draw_rectangle2"))
					{
						Navit.OSD_scale.scale_valid = true;
						Navit.OSD_scale.var = i1;
					}
					else if (text1.equals("draw_text"))
					{
						Navit.OSD_scale.scale_valid = true;
						Navit.OSD_scale.scale_text = text2;
					}
				}
				needed_value = true;
			}
			else if (id.equals("osd_text_draw"))
			{
				if ((text1 != null) && (text1.equals("draw_text")))
				{
					if ((text2 != null) && (text2.equals("navigation:nav_position:destination_time")))
					{
						// text3 = 20:38 --> ETA time (+1 means next day!)
						// System.out.println("destination_time:" + text3);
						Navit.OSD_route_001.arriving_time = text3;
						Navit.OSD_route_001.arriving_time_valid = true;
						needed_value = true;
					}
					else if ((text2 != null) && (text2.equals("navigation:nav_position:destination_length")))
					{
						// text3 = 575m -> driving distance to target
						// System.out.println("destination_length:" + text3);
						Navit.OSD_route_001.driving_distance_to_target = text3;
						Navit.OSD_route_001.driving_distance_to_target_valid = true;
						needed_value = true;
					}
					else if ((text2 != null) && (text2.length() > 10) && (text2.substring(0, 11).equals("navigation:")))
					{
						String[] tmp_string = text2.split(":", 3);
						if (tmp_string.length == 3)
						{
							if (tmp_string[2].equals("length"))
							{
								// "navigation:******:length"
								// text3 = 250m --> when to do the next turn
								//System.out.println("nextturn_distance:" + text3);
								Navit.OSD_nextturn.nextturn_distance = text3;
								Navit.OSD_nextturn.nextturn_distance_valid = true;
								// we need to paint the OSD in any case
								last_paint_OSD = -1;
								needed_value = true;
							}
							else if (tmp_string[2].equals("street_name_systematic"))
							{
								// "navigation:******:street_name_systematic"
								//System.out.println("nextturn_streetname_systematic:" + text3);
								Navit.OSD_nextturn.nextturn_streetname_systematic = text3;
								needed_value = true;
							}
							else if (tmp_string[2].equals("street_name"))
							{
								// "navigation:******:street_name"
								//System.out.println("nextturn_streetname:" + text3);
								Navit.OSD_nextturn.nextturn_streetname = text3;
								needed_value = true;
							}
						}
					}
					else if ((text2 != null) && (text2.length() > 8) && (text2.substring(0, 9).equals("tracking:")))
					{
						if (NavitGraphics.navit_route_status == 0)
						{
							if (text2.equals("tracking:street_name:"))
							{
								//System.out.println("t2" + text2 + text3);
								Navit.OSD_nextturn.nextturn_streetname = text3;
								last_paint_OSD = -1;
								needed_value = true;
							}
							else if (text2.equals("tracking:street_name_systematic:"))
							{
								//System.out.println("t1" + text2 + text3);
								Navit.OSD_nextturn.nextturn_streetname_systematic = text3;
								needed_value = true;
							}
						}
					}
					else
					{
						// Log.e("NavitOSDJava", "" + id + " " + text1 + " " + text2 + " " + text3 + " " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + f1 + " " + f2 + " " + f3);
					}
				}
			}
			else if (id.equals("nav_next_turn"))
			{
				if ((text1 != null) && (text1.equals("draw_image1")))
				{
					//System.out.println("tttt222:" + text2);
					// text2 = res/drawable/xx.png
					if ((text2 == null) || (text2.equals("")))
					{
						Navit.OSD_nextturn.nextturn_image_valid = false;
						Navit.OSD_nextturn.nextturn_image.recycle();
					}
					if (!Navit.OSD_nextturn.nextturn_image_filename.equals(text2))
					{
						// only if image is different from current image
						Navit.OSD_nextturn.nextturn_image_filename = text2;
						Navit.OSD_nextturn.nextturn_image_filename_valid = true;
						if (Navit.OSD_nextturn.nextturn_image != null)
						{
							try
							{
								Navit.OSD_nextturn.nextturn_image.recycle();
							}
							catch (Exception e)
							{
							}
						}
						// System.out.println("load image: " + Navit.OSD_nextturn.nextturn_image_filename);
						String x = Navit.OSD_nextturn.nextturn_image_filename.substring(13).replace(".png", "");
						// System.out.println("load image: " + x);
						int ResId = Navit.res_.getIdentifier("com.zoffcc.applications.zanavi:drawable/" + x, null, null);
						// System.out.println("ResId: " + ResId);
						Navit.OSD_nextturn.nextturn_image = BitmapFactory.decodeResource(Navit.res_, ResId);
						Navit.OSD_nextturn.nextturn_image_valid = true;
					}
				}
				needed_value = true;
			}
			else if (id.equals("compass"))
			{
				if ((text1 != null) && (text1.equals("text_and_dst_angle")))
				{
					if ((text2 == null) || (text2.equals("")))
					{
						Navit.OSD_compass.direct_distance_to_target = "";
						Navit.OSD_compass.direct_distance_to_target_valid = false;
					}
					else
					{
						Navit.OSD_compass.direct_distance_to_target = text2;
						Navit.OSD_compass.direct_distance_to_target_valid = true;
					}
					try
					{
						// Navit.OSD_compass.angle_target = Float.parseFloat(text3);
						Navit.OSD_compass.angle_target = i1;
						Navit.OSD_compass.angle_target_valid = true;
					}
					catch (Exception e)
					{
						//e.printStackTrace();
					}
					needed_value = true;
				}
				else if ((text1 != null) && (text1.equals("direction")))
				{
					try
					{
						// Navit.OSD_compass.angle_north = Float.parseFloat(text2);
						Navit.OSD_compass.angle_north = i1;
						Navit.OSD_compass.angle_north_valid = true;
						needed_value = true;
					}
					catch (Exception e)
					{
						//e.printStackTrace();

					}
				}
			}
		}
		catch (Exception x)
		{
			x.printStackTrace();
		}

		if (!needed_value)
		{
			// we got values that we dont use
			//System.out.println("xx paint 0 xx");
			return;
		}

		if (NavitGraphics.MAP_DISPLAY_OFF)
		{
			// paint only every 800ms
			if ((last_paint_OSD + 800) < System.currentTimeMillis())
			{
				try
				{
					last_paint_OSD = System.currentTimeMillis();
					//System.out.println("xx paint 1 xx");
					NavitOSDJava.draw_real_wrapper(true, false);
					//++NavitAOSDJava_.postInvalidate();
				}
				catch (Exception r)
				{
					//r.printStackTrace();
				}
			}
		}
		else
		{
			// paint only every 600ms
			if ((last_paint_OSD + 600) < System.currentTimeMillis())
			{
				try
				{
					last_paint_OSD = System.currentTimeMillis();
					//System.out.println("xx paint 2 xx");
					NavitOSDJava.draw_real_wrapper(true, true);
					//++NavitAOSDJava_.postInvalidate();
					//++NavitAOverlay_s.postInvalidate();
				}
				catch (Exception r)
				{
					//r.printStackTrace();
				}
			}
		}
	}

	static void copy_map_buffer()
	{
		//System.out.println("DO__DRAW:copy_map_buffer:drawBitmap enter");
		synchronized (synch_drawing_for_map)
		{
			// stop any smooth drawing/moving first!
			Global_SmoothDrawing_stop = true;
			//System.out.println("copy_map_buffer: Enter ******");

			//System.out.println("DO__DRAW:Java:reset GLOBAL factors");
			Global_Map_TransX = 0;
			Global_Map_TransY = 0;
			Global_Map_Rotationangle = 0f;
			Global_Map_Zoomfactor = 1.0f;
			try
			{
				//System.out.println("DO__DRAW:copy_map_buffer:drawBitmap start");
				draw_canvas_screen_s.drawBitmap(draw_bitmap_s, 0, 0, null);
				//System.out.println("DO__DRAW:copy_map_buffer:drawBitmap end");

				if (Navit.PREF_show_route_rects)
				{
					// --- draw debug route rects ----
					int i3 = 0;
					for (i3 = 0; i3 < route_rects.size(); i3++)
					{
						route_rect rr = route_rects.get(i3);
						Paint paint78 = new Paint();
						if (rr.order == -99)
						{
							// rectangle to just include all waypoints
							paint78.setColor(Color.RED);
						}
						else if (rr.order == 8)
						{
							// rectangle around every waypoint with low detail (order = 8)
							paint78.setColor(Color.GREEN);
						}
						else if (rr.order == 18)
						{
							// rectangle around every waypoint with high detail (order = 18)
							paint78.setColor(Color.MAGENTA);
						}
						else if (rr.order == 4)
						{
							// rectangles ??
							paint78.setColor(Color.BLUE);
						}
						else if (rr.order == 6)
						{
							// rectangles ??
							paint78.setColor(Color.CYAN);
						}
						else if (rr.order == 7)
						{
							// rectangles ??
							paint78.setColor(Color.YELLOW);
						}

						paint78.setStrokeWidth(15);
						paint78.setStyle(Style.STROKE);

						String left_top_on_screen_string = CallbackGeoCalc(11, rr.x1, rr.y1);
						String tmp[] = left_top_on_screen_string.split(":", 2);
						int xx1 = Integer.parseInt(tmp[0]);
						int yy1 = Integer.parseInt(tmp[1]);
						//System.out.println(" " + xx1 + "," + yy1);

						left_top_on_screen_string = CallbackGeoCalc(11, rr.x2, rr.y2);
						tmp = left_top_on_screen_string.split(":", 2);
						int xx2 = Integer.parseInt(tmp[0]);
						int yy2 = Integer.parseInt(tmp[1]);
						//System.out.println(" " + xx2 + "," + yy2);

						// draw the route rectanlge
						draw_canvas_screen_s.drawRect(xx1, yy1, xx2, yy2, paint78);
					}
					// --- draw debug route rects ----
				}
			}
			catch (Exception e)
			{
				// if screen is rotated, bitmaps are not valid, and this would crash
				// find a better solution later!!
			}
			//System.out.println("copy_map_buffer: Ready ******");

		}

	}

	static void copy_backwards_map_buffer()
	{
		// draw_canvas_screen_s.drawColor(Color.parseColor("#FEF9EE")); // fill with yellow-ish bg color

		//if (ZOOM_MODE_ACTIVE)
		//{
		//	draw_canvas_screen_s.save();
		//	draw_canvas_screen_s.scale(ZOOM_MODE_SCALE, ZOOM_MODE_SCALE, touch_now_center.x, touch_now_center.y);
		//}
		//draw_canvas_screen_s.drawBitmap(draw_bitmap_screen_s, pos_x, pos_y, null);
		//if (ZOOM_MODE_ACTIVE)
		//{
		//	draw_canvas_screen_s.restore();
		//}
	}

	protected void draw_mode(int mode)
	{
		//Log.e("NavitGraphics", "draw_mode mode=" + mode + " parent_graphics=" + String.valueOf(parent_graphics));

		if (mode == 1 || (mode == 0 && parent_num != 0))
		{
			//System.out.println("DO__DRAW:draw_mode:erase start");
			draw_bitmap.eraseColor(0);
			//System.out.println("DO__DRAW:draw_mode:erase start");
		}

		//if ((parent_num != 0) && ((mode == 2) || (mode == 4)))
		//{
		// vehicle graphics overlay
		// copy_map_buffer();
		//			Paint paint43 = new Paint();
		//			paint43.setStyle(Paint.Style.STROKE);
		//			paint43.setAntiAlias(Navit.PREF_use_anti_aliasing);
		//			paint43.setStrokeWidth(12);
		//			paint43.setColor(Color.RED);
		//			draw_canvas.drawLine(0, 0, 50, 50, paint43);
		//}

	}

	protected void draw_drag(int x, int y)
	{
		// Log.e("NavitGraphics","draw_drag"+pos_x+" "+pos_y+" "+x+" "+y);
		pos_x = x;
		pos_y = y;
	}

	protected void overlay_disable(int disable)
	{
		// UNUSED ------

		//Log.e("NavitGraphics","overlay_disable");
		// assume we are NOT in map view mode!
		// -> always in map mode now !!!! #  in_map = false;

		// check if overlay has been initialized yet
		//if (NavitAOverlay != null)
		//{
		//	NavitAOverlay.hide_bubble();
		//}

		// overlay_disabled = disable;
	}

	protected void overlay_resize(int x, int y, int w, int h, int alpha, int wraparond)
	{
		// UNUSED ------

		//Log.e("NavitGraphics", "overlay_resize: " + x + "" + y + "" + w + "" + h);
		//pos_x = x;
		//pos_y = y;
	}

	public static String getLocalizedString(String text)
	{
		String ret = CallbackLocalizedString(text);
		//Log.e("NavitGraphics", "callback_handler -> lozalized string=" + ret);
		return ret;
	}

	static String __n_distance(int i)
	{
		String ret = "";
		if ((i > 0) && (i < 10))
		{
			ret = __get_distance(i, 0);
		}
		else
		{
			switch (i)
			{
			case 10:
				ret = "";
				break;
			case 11:
				ret = "soon";
				break;
			case 12:
				ret = "after %i roads";
				break;
			case 13:
				ret = "now";
				break;
			default:
				ret = "";
			}
		}
		return ret;
	}

	static String __get_distance(int i, int is_length)
	{
		String ret = "";

		if (is_length == 0)
		{
			switch (i)
			{
			case 1:
				ret = "in %d m";
				break;
			case 2:
				ret = "in %d feet";
				break;
			case 3:
				ret = "in %d meters";
				break;
			case 4:
				ret = "in %d.%d miles";
				break;
			case 5:
				ret = "in %d.%d kilometers";
				break;
			case 6:
				ret = "in one mile";
				break;
			case 7:
				ret = "in %d miles";
				break;
			case 8:
				ret = "in one kilometer";
				break;
			case 9:
				ret = "in %d kilometer";
				break;
			default:
				ret = "";
			}
		}
		else
		{
			switch (i)
			{
			case 1:
				ret = "%d m";
				break;
			case 2:
				ret = "%d feet";
				break;
			case 3:
				ret = "%d meters";
				break;
			case 4:
				ret = "%d.%d miles";
				break;
			case 5:
				ret = "%d.%d kilometers";
				break;
			case 6:
				ret = "one mile";
				break;
			case 7:
				ret = "%d miles";
				break;
			case 8:
				ret = "one kilometer";
				break;
			case 9:
				ret = "%d kilometer";
				break;
			default:
				ret = "";
			}
		}

		return ret;
	}

	static String __direction(int i)
	{
		String ret = "";

		switch (i)
		{
		case 1:
			ret = "left";
			break;
		case 2:
			ret = "right";
			break;
		default:
			ret = "";
		}

		return ret;
	}

	static String __strength(int i)
	{
		String ret = "";

		switch (i)
		{
		case 1:
			ret = "";
			break;
		case 2:
			ret = "slight ";
			break;
		case 3:
			ret = "hard ";
			break;
		case 4:
			ret = "really hard ";
			break;
		default:
			ret = "";
		}

		return ret;
	}

	static String __navigation_item_destination(int i)
	{
		String ret = "";

		switch (i)
		{
		case 1:
			ret = "";
			break;
		case 2:
			ret = "exit";
			break;
		case 3:
			ret = "into the ramp";
			break;
		case 4:
			ret = "%sinto the street %s%s%s";
			break;
		case 5:
			ret = "%sinto the %s%s%s|male form";
			break;
		case 6:
			ret = "%sinto the %s%s%s|female form";
			break;
		case 7:
			ret = "%sinto the %s%s%s|neutral form";
			break;
		case 8:
			ret = "%sinto the %s";
			break;
		default:
			ret = "";
		}

		return ret;
	}

	public static void generate_all_speech_commands()
	{

		try
		{
			NavitGraphics.NavitMsgTv2_.setVisibility(View.VISIBLE);
			NavitGraphics.NavitMsgTv2_.setEnabled(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String a = null;
		String b = null;
		String a1 = null;
		String b1 = null;
		String c = null;
		String c1 = null;
		String d = null;
		String d1 = null;
		int j;
		//
		//
		//
		a = "When possible, please turn around";
		a1 = CallbackLocalizedString(a);
		NavitGraphics.NavitMsgTv2_.append(a + "\n");
		NavitGraphics.NavitMsgTv2_.append(a1 + "\n");
		System.out.println(a);
		System.out.println(a1);
		//
		a = "Enter the roundabout soon";
		a1 = CallbackLocalizedString(a);
		NavitGraphics.NavitMsgTv2_.append(a + "\n");
		NavitGraphics.NavitMsgTv2_.append(a1 + "\n");
		System.out.println(a);
		System.out.println(a1);
		//
		a = "then you have reached your destination.";
		a1 = CallbackLocalizedString(a);
		NavitGraphics.NavitMsgTv2_.append(a + "\n");
		NavitGraphics.NavitMsgTv2_.append(a1 + "\n");
		System.out.println(a);
		System.out.println(a1);
		//
		a = "In %s, enter the roundabout";
		a1 = CallbackLocalizedString(a);
		for (j = 1; j < 10; j++)
		{
			if ((j == 4) || (j == 5))
			{
				b = __get_distance(j, 1);
				b1 = CallbackLocalizedString(b);
				c = String.format(b, 1, 4);
				c1 = String.format(b1, 1, 4);
				d = String.format(a, c);
				d1 = String.format(a1, c1);
				try
				{
					d = String.format(d, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				try
				{
					d1 = String.format(d1, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				NavitGraphics.NavitMsgTv2_.append(d + "\n");
				NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
				System.out.println(d);
				System.out.println(d1);
			}
			else
			{
				b = __get_distance(j, 1);
				b1 = CallbackLocalizedString(b);
				c = String.format(b, 250);
				c1 = String.format(b1, 250);
				d = String.format(a, c);
				d1 = String.format(a1, c1);
				try
				{
					d = String.format(d, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				try
				{
					d1 = String.format(d1, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				NavitGraphics.NavitMsgTv2_.append(d + "\n");
				NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
				System.out.println(d);
				System.out.println(d1);
			}
		}
		//
		a = "Follow the road for the next %s";
		a1 = CallbackLocalizedString(a);
		for (j = 1; j < 10; j++)
		{
			if ((j == 4) || (j == 5))
			{
				b = __get_distance(j, 1);
				b1 = CallbackLocalizedString(b);
				c = String.format(b, 1, 4);
				c1 = String.format(b1, 1, 4);
				d = String.format(a, c);
				d1 = String.format(a1, c1);
				try
				{
					d = String.format(d, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				try
				{
					d1 = String.format(d1, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				NavitGraphics.NavitMsgTv2_.append(d + "\n");
				NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
				System.out.println(d);
				System.out.println(d1);
			}
			else
			{
				b = __get_distance(j, 1);
				b1 = CallbackLocalizedString(b);
				c = String.format(b, 250);
				c1 = String.format(b1, 250);
				d = String.format(a, c);
				d1 = String.format(a1, c1);
				try
				{
					d = String.format(d, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				try
				{
					d1 = String.format(d1, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				NavitGraphics.NavitMsgTv2_.append(d + "\n");
				NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
				System.out.println(d);
				System.out.println(d1);
			}
		}
		//
		a = "Leave the roundabout at the %s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "first exit");
		b1 = String.format(a1, CallbackLocalizedString("first exit"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "Leave the roundabout at the %s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "third exit");
		b1 = String.format(a1, CallbackLocalizedString("third exit"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "then leave the roundabout at the %s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "first exit");
		b1 = String.format(a1, CallbackLocalizedString("first exit"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "then leave the roundabout at the %s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "third exit");
		b1 = String.format(a1, CallbackLocalizedString("third exit"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "Take the %1$s road to the %2$s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "first", "left");
		b1 = String.format(a1, CallbackLocalizedString("first"), CallbackLocalizedString("left"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "Take the %1$s road to the %2$s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "first", "right");
		b1 = String.format(a1, CallbackLocalizedString("first"), CallbackLocalizedString("right"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "Take the %1$s road to the %2$s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "third", "left");
		b1 = String.format(a1, CallbackLocalizedString("third"), CallbackLocalizedString("left"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "Take the %1$s road to the %2$s";
		a1 = CallbackLocalizedString(a);
		b = String.format(a, "third", "right");
		b1 = String.format(a1, CallbackLocalizedString("third"), CallbackLocalizedString("right"));
		NavitGraphics.NavitMsgTv2_.append(b + "\n");
		NavitGraphics.NavitMsgTv2_.append(b1 + "\n");
		System.out.println(b);
		System.out.println(b1);
		//
		a = "You have reached your destination %s";
		a1 = CallbackLocalizedString(a);
		for (j = 1; j < 14; j++)
		{
			if (j == 10)
			{
				d = String.format(a, "");
				d1 = String.format(a1, "");
				try
				{
					d = String.format(d, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				try
				{
					d1 = String.format(d1, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				NavitGraphics.NavitMsgTv2_.append(d + "\n");
				NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
				System.out.println(d + "\n");
				System.out.println(d1 + "\n");
			}
			else if (j == 12)
			{
				b = __n_distance(j);
				b1 = CallbackLocalizedString(b);
				c = b.replace("%i", "3");
				c1 = b1.replace("%i", "3");
				d = String.format(a, c);
				d1 = String.format(a1, c1);
				try
				{
					d = String.format(d, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				try
				{
					d1 = String.format(d1, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				NavitGraphics.NavitMsgTv2_.append(d + "\n");
				NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
				System.out.println(d);
				System.out.println(d1);
			}
			else
			{
				b = __n_distance(j);
				b1 = CallbackLocalizedString(b);
				d = String.format(a, b);
				d1 = String.format(a1, b1);
				try
				{
					d = String.format(d, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				try
				{
					d1 = String.format(d1, 3, 4, 5);
				}
				catch (Exception e)
				{

				}
				NavitGraphics.NavitMsgTv2_.append(d + "\n");
				NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
				System.out.println(d);
				System.out.println(d1);
			}
		}
		//
		a = "Turn %1$s%2$s %3$s%4$s";
		a1 = CallbackLocalizedString(a);
		String xx = null;
		String xx1 = null;
		String yy = null;
		String yy1 = null;
		for (j = 1; j < 5; j++)
		{
			if (j == 1)
			{
				b = __strength(j);
				b1 = "";
			}
			else
			{
				b = __strength(j);
				b1 = CallbackLocalizedString(b);
			}

			for (int k = 1; k < 3; k++)
			{
				c = __direction(k);
				c1 = CallbackLocalizedString(c);

				for (int m = 1; m < 14; m++)
				{
					if (m == 10)
					{
						xx = "";
						xx1 = "";
					}
					else if (m == 12)
					{
						String zz = __n_distance(m);
						String zz1 = CallbackLocalizedString(zz);
						xx = zz.replace("%i", "3");
						xx1 = zz1.replace("%i", "3");
					}
					else
					{
						xx = __n_distance(m);
						xx1 = CallbackLocalizedString(xx);
					}

					for (int o = 1; o < 9; o++)
					{
						if (o == 2)
						{
							// leave out "exit"
							break;
						}

						if (o == 1)
						{
							yy = __navigation_item_destination(o);
							yy1 = "";
						}
						else if (o == 4)
						{
							String zz;
							String zz1;
							zz = __navigation_item_destination(o);
							zz1 = CallbackLocalizedString(zz);
							yy = String.format(zz, " ", "somestreet", " ", "A23");
							yy1 = String.format(zz1, " ", "blablastrasse", " ", "A23");
						}
						else if ((o == 5) || (o == 6) || (o == 7))
						{
							String zz;
							String zz1;
							zz = __navigation_item_destination(o);
							zz1 = CallbackLocalizedString(zz);
							try
							{
								zz = zz.substring(0, zz.lastIndexOf("|"));
							}
							catch (Exception e)
							{

							}
							try
							{
								zz1 = zz1.substring(0, zz1.lastIndexOf("|"));
							}
							catch (Exception e)
							{

							}
							yy = String.format(zz, " ", "somestreet", " ", "A23");
							yy1 = String.format(zz1, " ", "blablastrasse", " ", "A23");
						}
						else if (o == 8)
						{
							String zz;
							String zz1;
							zz = __navigation_item_destination(o);
							zz1 = CallbackLocalizedString(zz);
							yy = String.format(zz, " ", "A23");
							yy1 = String.format(zz1, " ", "A23");
						}
						else
						{
							yy = __navigation_item_destination(o);
							yy1 = " " + CallbackLocalizedString(yy);
							yy = " " + yy;
						}

						// apply parts
						d = String.format(a, b, c, xx, yy);
						d1 = String.format(a1, b1, c1, xx1, yy1);
						try
						{
							d = String.format(d, 3, 4, 5);
						}
						catch (Exception e)
						{

						}
						try
						{
							d1 = String.format(d1, 3, 4, 5);
						}
						catch (Exception e)
						{

						}
						NavitGraphics.NavitMsgTv2_.append(d + "\n");
						NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
						System.out.println(d);
						System.out.println(d1);
					}
				}
			}
		}
		//
		a = "then turn %1$s%2$s %3$s%4$s";
		a1 = CallbackLocalizedString(a);
		xx = null;
		xx1 = null;
		yy = null;
		yy1 = null;
		for (j = 1; j < 5; j++)
		{
			if (j == 1)
			{
				b = __strength(j);
				b1 = "";
			}
			else
			{
				b = __strength(j);
				b1 = CallbackLocalizedString(b);
			}

			for (int k = 1; k < 3; k++)
			{
				c = __direction(k);
				c1 = CallbackLocalizedString(c);

				for (int m = 1; m < 14; m++)
				{
					if (m == 10)
					{
						xx = "";
						xx1 = "";
					}
					else if (m == 12)
					{
						String zz = __n_distance(m);
						String zz1 = CallbackLocalizedString(zz);
						xx = zz.replace("%i", "3");
						xx1 = zz1.replace("%i", "3");
					}
					else
					{
						xx = __n_distance(m);
						xx1 = CallbackLocalizedString(xx);
					}

					for (int o = 1; o < 9; o++)
					{
						if (o == 2)
						{
							// leave out "exit"
							break;
						}

						if (o == 1)
						{
							yy = __navigation_item_destination(o);
							yy1 = "";
						}
						else if (o == 4)
						{
							String zz;
							String zz1;
							zz = __navigation_item_destination(o);
							zz1 = CallbackLocalizedString(zz);
							yy = String.format(zz, " ", "somestreet", " ", "A23");
							yy1 = String.format(zz1, " ", "blablastrasse", " ", "A23");
						}
						else if ((o == 5) || (o == 6) || (o == 7))
						{
							String zz;
							String zz1;
							zz = __navigation_item_destination(o);
							zz1 = CallbackLocalizedString(zz);
							try
							{
								zz = zz.substring(0, zz.lastIndexOf("|"));
							}
							catch (Exception e)
							{

							}
							try
							{
								zz1 = zz1.substring(0, zz1.lastIndexOf("|"));
							}
							catch (Exception e)
							{

							}
							yy = String.format(zz, " ", "somestreet", " ", "A23");
							yy1 = String.format(zz1, " ", "blablastrasse", " ", "A23");
						}
						else if (o == 8)
						{
							String zz;
							String zz1;
							zz = __navigation_item_destination(o);
							zz1 = CallbackLocalizedString(zz);
							yy = String.format(zz, " ", "A23");
							yy1 = String.format(zz1, " ", "A23");
						}
						else
						{
							yy = __navigation_item_destination(o);
							yy1 = " " + CallbackLocalizedString(yy);
							yy = " " + yy;
						}

						// apply parts
						d = String.format(a, b, c, xx, yy);
						d1 = String.format(a1, b1, c1, xx1, yy1);
						try
						{
							d = String.format(d, 3, 4, 5);
						}
						catch (Exception e)
						{

						}
						try
						{
							d1 = String.format(d1, 3, 4, 5);
						}
						catch (Exception e)
						{

						}
						NavitGraphics.NavitMsgTv2_.append(d + "\n");
						NavitGraphics.NavitMsgTv2_.append(d1 + "\n");
						System.out.println(d);
						System.out.println(d1);
					}
				}
			}
		}
		//
		//
		//

		/*
		 * speech commands:
		 * ================
		 * 
		 * 
		 * When possible, please turn around
		 * Enter the roundabout soon
		 * then you have reached your destination.
		 * 
		 * In %s, enter the roundabout (get_distance(is_length=1))
		 * Follow the road for the next %s (get_distance(is_length=1))
		 * 
		 * Leave the roundabout at the %s (get_exit_count_str)
		 * then leave the roundabout at the %s (get_exit_count_str)
		 * 
		 * Take the %1$s road to the %2$s (get_count_str(),direction)
		 * then take the %1$s road to the %2$s (get_count_str(),direction)
		 * 
		 * Turn %1$s%2$s %3$s%4$s (strength,direction,distance,navigation_item_destination(" "))
		 * then turn %1$s%2$s %3$s%4$s (strength,direction,distance,navigation_item_destination(" "))
		 * 
		 * You have reached your destination %s (distance)
		 * 
		 * 
		 * 
		 * distance:
		 * ""
		 * soon
		 * get_distance(is_length=0)
		 * after %i roads
		 * now
		 * 
		 * 
		 * direction:
		 * left
		 * right
		 * 
		 * strength:
		 * ""
		 * slight
		 * hard
		 * really hard
		 * 
		 * navigation_item_destination:
		 * ============================
		 * ""
		 * (prefix)exit
		 * (prefix)into the ramp
		 * (prefix)into the street (streetname)(sep)(systematic streetname)
		 * (prefix)into the (streetname)(sep)(systematic streetname) |male form %s%s%s
		 * (prefix)into the (streetname)(sep)(systematic streetname) |female form %s%s%s
		 * (prefix)into the (streetname)(sep)(systematic streetname) |neutral form %s%s%s
		 * (prefix)into the (systematic streetname) %s
		 * 
		 * 
		 * 
		 * get_count_str:
		 * ==============
		 * first
		 * second
		 * fifth
		 * 
		 * 
		 * get_exit_count_str:
		 * ===================
		 * first exit
		 * second exit
		 * fifth exit
		 * 
		 * 
		 * 
		 * get_distance:
		 * =============
		 * %d m (is_length 1)
		 * in %d m
		 * %d feet (is_length 1)
		 * in %d feet
		 * %d meters (is_length 1)
		 * in %d meters
		 * %d.%d miles (is_length 1)
		 * in %d.%d miles
		 * %d.%d kilometers (is_length 1)
		 * in %d.%d kilometers
		 * one mile,%d miles (is_length 1)
		 * in one mile,in %d miles
		 * one kilometer,%d kilometers (is_length 1)
		 * in one kilometer,in %d kilometers
		 */

	}

	/**
	 * generic message channel to C-code
	 */
	public static native void CallbackMessageChannelReal(int i, String s);

	public static native int GetThreadId();

	public static void CallbackMessageChannel(int i, String s)
	{
		Navit.cwthr.CallbackMessageChannel(i, s);
	}

	/**
	 * return search result from C-code
	 */
	public void fillStringArray(String s)
	{
		//Log.e("NavitGraphics", "**** fillStringArray s=" + s);
		// deactivate the spinner
		// --> no we want to spin ** Navit.NavitAddressSearchSpinnerActive = false;

		if (s.equals("D:D"))
		{
			// ok its a dummy, just move the percent bar
			// Log.e("NavitGraphics", "**** fillStringArray s=" + s);
		}
		else
		{
			try
			{
				// we hope its a real result value
				Navit.Navit_Address_Result_Struct tmp_addr = new Navit_Address_Result_Struct();
				String[] tmp_s = s.split(":");
				tmp_addr.result_type = tmp_s[0];
				tmp_addr.item_id = tmp_s[1];

				if (Navit.use_index_search)
				{
					tmp_addr.lat = Integer.parseInt(tmp_s[2]);
					tmp_addr.lon = Integer.parseInt(tmp_s[3]);
				}
				else
				{
					tmp_addr.lat = Float.parseFloat(tmp_s[2]);
					tmp_addr.lon = Float.parseFloat(tmp_s[3]);
				}
				// the rest ist address
				tmp_addr.addr = s.substring(4 + tmp_s[0].length() + tmp_s[1].length() + tmp_s[2].length() + tmp_s[3].length(), s.length());

				// String hash_id = tmp_addr.result_type + ":" + tmp_addr.lat + ":" + tmp_addr.lon + ":" + tmp_addr.addr;
				String hash_id = tmp_addr.result_type + ":" + tmp_addr.addr;
				//System.out.println("hash_id=" + hash_id);
				if ((!Navit.search_hide_duplicates) || (!Navit.Navit_Address_Result_double_index.contains(hash_id)))
				{
					Navit.NavitAddressResultList_foundItems.add(tmp_addr);
					Navit.Navit_Address_Result_double_index.add(hash_id);
					//System.out.println("*add*=" + hash_id);

					if (tmp_addr.result_type.equals("TWN"))
					{
						Navit.search_results_towns++;
					}
					else if (tmp_addr.result_type.equals("STR"))
					{
						Navit.search_results_streets++;
					}
					else if (tmp_addr.result_type.equals("SHN"))
					{
						Navit.search_results_streets_hn++;
					}
					else if (tmp_addr.result_type.equals("POI"))
					{
						Navit.search_results_poi++;
					}

					Navit.NavitSearchresultBar_title = Navit.get_text("loading search results");
					Navit.NavitSearchresultBar_text = Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn + " " + Navit.get_text("POI") + ":" + Navit.search_results_poi;

					// make the dialog move its bar ...
					//			Bundle b = new Bundle();
					//			b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
					//			b.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
					//			b.putInt("cur", Navit.NavitAddressResultList_foundItems.size() % (Navit.ADDRESS_RESULTS_DIALOG_MAX + 1));
					//			b.putString("title", Navit.get_text("loading search results")); //TRANS
					//			b.putString("text", Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn);
					//			Navit.msg_to_msg_handler(b, 10);
				}
				//else
				//{
				//	//System.out.println("double " + tmp_addr.addr);
				//}
			}
			catch (Exception e)
			{

			}
		}

		// always move the bar, so that the user knows app is still doing something (and has not crashed!)
		//		Navit.NavitSearchresultBarIndex++;
		//		if (Navit.NavitSearchresultBarIndex > Navit.ADDRESS_RESULTS_DIALOG_MAX)
		//		{
		//			Navit.NavitSearchresultBarIndex = 0;
		//		}
		// make the dialog move its bar ...
		//		Bundle b = new Bundle();
		//		b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
		//		b.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
		//		b.putInt("cur", Navit.NavitSearchresultBarIndex);
		//		b.putString("title", Navit.get_text("loading search results")); //TRANS
		//		b.putString("text", Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn);
		//		Navit.msg_to_msg_handler(b, 10);
	}

	public void SearchResultList(int i, int partial_match, String text, String t_town, String t_hn, int flags, String country_iso2, String search_latlon, int search_radius)
	{
		CallbackSearchResultList(i, partial_match, text, t_town, t_hn, flags, country_iso2, search_latlon, search_radius);
	}

	public native void CallbackSearchResultList(int i, int partial_match, String s, String s_town, String s_hn, int flags, String country_iso2, String search_latlon, int search_radius);

	/**
	 * get localized string
	 */
	public static native String CallbackLocalizedString(String s);

	//
	//
	//
	// get route_status value from C-lib
	/*
	 * 
	 * 0 -> 1 -> 13 -> 5 -> 17 -> 5 ... 33 -> 5 -> 33 -> 5 ...
	 * 
	 * route_status_no_destination=0, # 0 --> no dest. set
	 * route_status_destination_set=1, # 1
	 * route_status_not_found=1|2, # 3 --> no route to destination found / route blocked
	 * route_status_building_path=1|4, # 5
	 * route_status_building_graph=1|4|8, # 13
	 * route_status_path_done_new=1|16, # 17 --> route found
	 * route_status_path_done_incremental=1|32, # 33 --> route found
	 */
	// now we get the value pushed from C automatically
	public static int CallbackDestinationValid2()
	{
		return NavitGraphics.navit_route_status;
	}

	// call C-function to get value --> not used anymore now!!
	public static native int CallbackDestinationValid();

	public static Handler callback_handler_s = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.getData().getInt("Callback") == 18)
			{
				CallbackMessageChannel(18, "");
			}
			else if (msg.getData().getInt("Callback") == 47)
			{
				// get values
				String s = msg.getData().getString("s");
				// set routing target to lat,lon
				CallbackMessageChannel(47, s);
			}
			else if (msg.getData().getInt("Callback") == 55)
			{
				// set cache size for (map-)files
				String s = msg.getData().getString("s");
				CallbackMessageChannel(55, s);
			}
			else if (msg.getData().getInt("Callback") == 59)
			{
				// enable layer "name"
				String s = msg.getData().getString("s");
				CallbackMessageChannel(59, s);
			}
			else if (msg.getData().getInt("Callback") == 60)
			{
				// disable layer "name"
				String s = msg.getData().getString("s");
				CallbackMessageChannel(60, s);
			}
			else if (msg.getData().getInt("Callback") == 78)
			{
				// shift layout "order" values
				String s = msg.getData().getString("s");
				CallbackMessageChannel(78, s);
			}
			else if (msg.getData().getInt("Callback") == 79)
			{
				// set traffic light delay
				String s = msg.getData().getString("s");
				CallbackMessageChannel(79, s);
			}
			else if (msg.getData().getInt("Callback") == 80)
			{
				// set autozoom flag to 0 or 1
				String s = msg.getData().getString("s");
				CallbackMessageChannel(80, s);
			}
			else if (msg.getData().getInt("Callback") == 81)
			{
				// resize layout items by factor
				String s = msg.getData().getString("s");
				CallbackMessageChannel(81, s);
			}
			else if (msg.getData().getInt("Callback") == 82)
			{
				// report share dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(82, s);
			}
			else if (msg.getData().getInt("Callback") == 84)
			{
				// report data dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(84, s);
			}
			else if (msg.getData().getInt("Callback") == 9901)
			{
				// if follow mode is on, then dont show freeview streetname
				//if (!Navit.PREF_follow_gps)
				//{
				//	Navit.cwthr.CallbackGeoCalc2(1, 0, mCanvasWidth / 2, mCanvasHeight / 2);
				//}
			}
			else if (msg.getData().getInt("Callback") == 98001)
			{
				int id = msg.getData().getInt("id");
				int i = msg.getData().getInt("i");
				return_generic_int_real(id, i);
			}
			else if (msg.getData().getInt("Callback") == 9001)
			{
				busyspinner_.setVisibility(View.INVISIBLE);
				busyspinnertext_.setVisibility(View.INVISIBLE);
			}
			else if (msg.getData().getInt("Callback") == 9002)
			{
				busyspinner_.setVisibility(View.VISIBLE);
				busyspinnertext_.setVisibility(View.VISIBLE);
			}
		}
	};

	//
	//
	//

	// i=1 -> pixel a,b (x,y)      -> geo   string "lat(float)#lng(float)"
	// i=2 -> geo   a,b (lat,lng)  -> pixel string "x(int)#y(int)"
	public static native String CallbackGeoCalc(int i, float a, float b);

	public static void send_generic_text(int id, String text)
	{
		//System.out.println("send_generic_text");

		if (id == 1)
		{
			// speech textblock
			if (NavitGraphics.NavitMsgTv2_.getVisibility() == View.VISIBLE)
			{
				NavitMsgTv2_.append("TEXT:" + text);
			}
		}
	}

	public static void return_generic_int(int id, int i)
	{
		try
		{
			Message msg1 = new Message();
			Bundle b1 = new Bundle();
			b1.putInt("Callback", 98001);
			b1.putInt("id", id);
			b1.putInt("i", i);
			msg1.setData(b1);
			NavitGraphics.callback_handler_s.sendMessage(msg1);
		}
		catch (Exception e)
		{
		}
	}

	public static void set_vehicle_values(int x, int y, int angle, int speed)
	{
		//System.out.println("DO__DRAW:set_vehicle_values:enter");
		synchronized (synch_drawing_for_map)
		{
			// stop any smooth drawing/moving first!
			//System.out.println("set vehicle pos...");
			//System.out.println("DO__DRAW:set_vehicle_values:set");
			//--> don't set here // Global_SmoothDrawing_stop = true;
			last_vehicle_position_timestamp = System.currentTimeMillis();

			//System.out.println("vehiclepos:1:x=" + x + " y=" + y + " Global_dpi_factor=" + NavitGraphics.Global_dpi_factor);
			Navit.NG__vehicle.vehicle_speed = speed;
			Navit.NG__vehicle.vehicle_pos_x = (int) ((float) x / NavitGraphics.Global_dpi_factor);
			Navit.NG__vehicle.vehicle_pos_y = (int) ((float) y / NavitGraphics.Global_dpi_factor);
			//System.out.println("vehiclepos:2:x=" + Navit.NG__vehicle.vehicle_pos_x + " y=" + Navit.NG__vehicle.vehicle_pos_y);
			Navit.NG__vehicle.vehicle_direction = angle;
			//System.out.println("DO__DRAW:set_vehicle_values:end");
		}
	}

	public static float ddx_last = 0;
	public static float ddy_last = 0;
	public static float dda_last = 0;
	public static float ddx = 0;
	public static float ddz = 0;
	public static float ddy = 0;
	public static float dda = 0f;
	public static long last_vehicle_position_timestamp = 0L;
	public static long Vehicle_updates_interval = 1000; // normal android phone gives position every 1000ms (=1 sec)
	public static int Vehicle_smooth_moves_count = 3; // how many intermediate moves do we want to draw
	public static int Vehicle_smooth_move_delay = 180;
	public static int Vehicle_smooth_move_delay_real_used = Vehicle_smooth_move_delay;
	public static int Vehicle_delay_real_gps_position = 650; // normal value for smooth delay = 450
	public static final int max_turn_angle = 190;

	static class SmoothDriveThread_t_A extends Thread
	{
		int cur_count = 0;
		int xxx2 = 0;

		public void run()
		{
			if (DEBUG_SMOOTH_DRIVING)
			{
				xxx2 = (int) (Math.random() * 1000f);
			}

			smooth_driving_ts002a = System.currentTimeMillis();

			//draw_canvas_screen2.scale(ZOOM_MODE_SCALE * Global_Map_Zoomfactor, ZOOM_MODE_SCALE * Global_Map_Zoomfactor, Global_Map_TransX + this.touch_now_center.x, Global_Map_TransY + this.touch_now_center.y);
			//draw_canvas_screen2.rotate(Global_Map_Rotationangle, Navit.NG__vehicle.vehicle_pos_x, Navit.NG__vehicle.vehicle_pos_y);

			// ------ make vehicle smooth zoom with vehicle as center point --------------------
			// ------ because center point may be any point on screen from last finger movement!
			//System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:sszz:tnc x=" + Navit.NG__map_main.touch_now_center.x + " y=" + Navit.NG__map_main.touch_now_center.y);
			//System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:sszz:vhp x=" + Navit.NG__vehicle.vehicle_pos_x + " y=" + Navit.NG__vehicle.vehicle_pos_y);
			//System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:sszz:gmt x=" + Global_Map_TransX + " y=" + Global_Map_TransY);
			Navit.NG__map_main.touch_now_center.x = Navit.NG__vehicle.vehicle_pos_x;
			Navit.NG__map_main.touch_now_center.y = Navit.NG__vehicle.vehicle_pos_y;
			// ------ make vehicle smooth zoom with vehicle as center point --------------------

			for (cur_count = 0; cur_count < Vehicle_smooth_moves_count; cur_count++)
			{
				if (Global_SmoothDrawing_stop == false)
				{
					// dont delay the first smooth move!
					if (cur_count != 0)
					{
						try
						{
							Thread.sleep(Vehicle_smooth_move_delay_real_used);
							if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:a=" + Vehicle_smooth_move_delay_real_used);
						}
						catch (InterruptedException e)
						{
							// e.printStackTrace();
						}
					}

				}

				if (Global_SmoothDrawing_stop == false)
				{
					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:smooth move #" + (cur_count + 1) + " delay=" + Vehicle_smooth_move_delay);
					//if (cur_count == (Vehicle_smooth_moves_count - 1))
					//{
					//	Global_Map_TransX = Global_Map_TransX - ddx_last;
					//	Global_Map_TransY = Global_Map_TransY - ddy_last;
					//	Global_Map_Rotationangle = Global_Map_Rotationangle - dda_last;
					//}
					//else
					//{
					Global_Map_TransX = Global_Map_TransX - ddx;
					Global_Map_TransY = Global_Map_TransY - ddy;
					Global_Map_Rotationangle = Global_Map_Rotationangle - dda;

					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:globalzoom factor o=" + Global_Map_Zoomfactor + " ddz=" + ddz);
					if (ddz != 0)
					{
						Global_Map_Zoomfactor = Global_Map_Zoomfactor * (1 + (cur_count + 1) * ddz) / (1 + cur_count * ddz);
					}

					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:globalzoom factor n=" + Global_Map_Zoomfactor);
					//}
					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:this #" + (cur_count + 1) + ":ddx=" + Global_Map_TransX + " ddy=" + Global_Map_TransY + " dda=" + Global_Map_Rotationangle);

					if (cur_count == 0)
					{
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:c=" + (System.currentTimeMillis() - smooth_driving_ts003));
						if (Math.abs(System.currentTimeMillis() - smooth_driving_ts001 - Vehicle_updates_interval) < 100)
						{
							Vehicle_updates_interval = System.currentTimeMillis() - smooth_driving_ts001;
						}
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:g=" + Vehicle_updates_interval);
						smooth_driving_ts001 = System.currentTimeMillis();
						Vehicle_smooth_move_delay_real_used = (int) (Vehicle_updates_interval / (Vehicle_smooth_moves_count + 1));
					}
					//else if (cur_count == (Vehicle_smooth_moves_count - 1))
					//{
					//}
					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:" + (cur_count + 1));
					Navit.NG__map_main.view.postInvalidate();
					//SurfaceView2 vw = (SurfaceView2) Navit.NG__map_main.view;
					//vw.paint_me();
				}
			}
			smooth_driving_ts002 = System.currentTimeMillis();
			if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:--set_vehicle_values_delta-- SLEEP - END");
		}
	}

	static SmoothDriveThread_t_B STT_B;
	static SmoothDriveThread_t_A STT_A;

	static Thread SmoothDriveThread_A = new Thread(new Runnable()
	{
		int cur_count = 0;
		int xxx2 = 0;

		public void run()
		{
			if (DEBUG_SMOOTH_DRIVING)
			{
				xxx2 = (int) (Math.random() * 1000f);
			}

			smooth_driving_ts002a = System.currentTimeMillis();

			for (cur_count = 0; cur_count < Vehicle_smooth_moves_count; cur_count++)
			{
				if (Global_SmoothDrawing_stop == false)
				{
					// dont delay the first smooth move!
					if (cur_count != 0)
					{
						try
						{
							Thread.sleep(Vehicle_smooth_move_delay_real_used);
							if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:a=" + Vehicle_smooth_move_delay_real_used);
						}
						catch (InterruptedException e)
						{
							// e.printStackTrace();
						}
					}

				}

				if (Global_SmoothDrawing_stop == false)
				{
					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:smooth move #" + (cur_count + 1) + " delay=" + Vehicle_smooth_move_delay);
					//if (cur_count == (Vehicle_smooth_moves_count - 1))
					//{
					//	Global_Map_TransX = Global_Map_TransX - ddx_last;
					//	Global_Map_TransY = Global_Map_TransY - ddy_last;
					//	Global_Map_Rotationangle = Global_Map_Rotationangle - dda_last;
					//}
					//else
					//{
					Global_Map_TransX = Global_Map_TransX - ddx;
					Global_Map_TransY = Global_Map_TransY - ddy;
					Global_Map_Rotationangle = Global_Map_Rotationangle - dda;
					//}
					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:this #" + (cur_count + 1) + ":ddx=" + Global_Map_TransX + " ddy=" + Global_Map_TransY + " dda=" + Global_Map_Rotationangle);

					if (cur_count == 0)
					{
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:c=" + (System.currentTimeMillis() - smooth_driving_ts003));
						if (Math.abs(System.currentTimeMillis() - smooth_driving_ts001 - Vehicle_updates_interval) < 100)
						{
							Vehicle_updates_interval = System.currentTimeMillis() - smooth_driving_ts001;
						}
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:g=" + Vehicle_updates_interval);
						smooth_driving_ts001 = System.currentTimeMillis();
						Vehicle_smooth_move_delay_real_used = (int) (Vehicle_updates_interval / (Vehicle_smooth_moves_count + 1));
					}
					//else if (cur_count == (Vehicle_smooth_moves_count - 1))
					//{
					//}
					if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:" + (cur_count + 1));
					Navit.NG__map_main.view.postInvalidate();
					//SurfaceView2 vw = (SurfaceView2) Navit.NG__map_main.view;
					//vw.paint_me();
				}
			}
			smooth_driving_ts002 = System.currentTimeMillis();
			if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:--set_vehicle_values_delta-- SLEEP - END");
		}
	});

	static class SmoothDriveThread_t_B extends Thread
	{
		public void run()
		{
			//System.out.println("DO__DRAW:Java:refresh map -> run");
			//							try
			//							{
			//								Thread.sleep(Vehicle_delay_real_gps_position);
			//							}
			//							catch (InterruptedException e)
			//							{
			//								// e.printStackTrace();
			//							}
			//System.out.println("DO__DRAW:Java:refresh map -> delay ready");

			if (!Global_onTouch_fingerdown)
			{
				smooth_driving_tmptmp = -(System.currentTimeMillis() - (smooth_driving_ts002a + (Vehicle_smooth_moves_count + 0) * Vehicle_smooth_move_delay_real_used));
				if (smooth_driving_tmptmp <= 0)
				{
					smooth_driving_tmptmp = 0;
				}
				if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b pre=" + smooth_driving_tmptmp);
				try
				{
					if (smooth_driving_tmptmp > 0)
					{
						Thread.sleep(smooth_driving_tmptmp);
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b corr=" + (smooth_driving_tmptmp));
					}
					else
					{
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b corr=NO DELAY");
					}
				}
				catch (InterruptedException e)
				{
				}

				// if we are in onDraw right now, give some extra little delay
				// this is not 100% super good, but it will work :-)
				// do not make a while or for loop! keep it hardcoded for speed!!
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				// do not make a while or for loop! keep it hardcoded for speed!!

				if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> draw!");
				copy_map_buffer();
				draw_reset_factors = true;
				if (DEBUG_SMOOTH_DRIVING)
				{
					System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b=" + (System.currentTimeMillis() - smooth_driving_ts002));
					System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:RR");
				}
				smooth_driving_ts003 = System.currentTimeMillis();
				view_s.postInvalidate();
				//view_s.paint_me();
				//System.out.println("DO__DRAW:Java:refresh map -> draw ready");
			}
		}
	}

	static Thread SmoothDriveThread_B = new Thread(new Runnable()
	{
		public void run()
		{
			//System.out.println("DO__DRAW:Java:refresh map -> run");
			//							try
			//							{
			//								Thread.sleep(Vehicle_delay_real_gps_position);
			//							}
			//							catch (InterruptedException e)
			//							{
			//								// e.printStackTrace();
			//							}
			//System.out.println("DO__DRAW:Java:refresh map -> delay ready");

			if (!Global_onTouch_fingerdown)
			{
				smooth_driving_tmptmp = -(System.currentTimeMillis() - (smooth_driving_ts002a + (Vehicle_smooth_moves_count + 0) * Vehicle_smooth_move_delay_real_used));
				if (smooth_driving_tmptmp <= 0)
				{
					smooth_driving_tmptmp = 0;
				}
				if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b pre=" + smooth_driving_tmptmp);
				try
				{
					if (smooth_driving_tmptmp > 0)
					{
						Thread.sleep(smooth_driving_tmptmp);
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b corr=" + (smooth_driving_tmptmp));
					}
					else
					{
						if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b corr=NO DELAY");
					}
				}
				catch (InterruptedException e)
				{
				}

				// if we are in onDraw right now, give some extra little delay
				// this is not 100% super good, but it will work :-)
				// do not make a while or for loop! keep it hardcoded for speed!!
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				if (Global_Map_in_onDraw == true)
				{
					try
					{
						//System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> extra delay");
						Thread.sleep(8);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
				// do not make a while or for loop! keep it hardcoded for speed!!

				if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:refresh map -> draw!");
				copy_map_buffer();
				draw_reset_factors = true;
				if (DEBUG_SMOOTH_DRIVING)
				{
					System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:b=" + (System.currentTimeMillis() - smooth_driving_ts002));
					System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:RR");
				}
				smooth_driving_ts003 = System.currentTimeMillis();
				view_s.postInvalidate();
				//view_s.paint_me();
				//System.out.println("DO__DRAW:Java:refresh map -> draw ready");
			}
		}
	});

	public static void set_vehicle_values_delta(int dx, int dy, int dangle2, int dzoom)
	{
		// DPI
		dx = (int) ((float) dx / NavitGraphics.Global_dpi_factor);
		dy = (int) ((float) dy / NavitGraphics.Global_dpi_factor);

		int xxx = 0;
		if (DEBUG_SMOOTH_DRIVING)
		{
			xxx = (int) (Math.random() * 1000f);
		}
		//System.out.println("" + xxx + "--set_vehicle_values_delta-- ENTER ++++++++++++");

		if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:00");

		Navit.NG__vehicle.vehicle_pos_x_delta = dx;
		Navit.NG__vehicle.vehicle_pos_y_delta = dy;
		int dangle = dangle2;
		if (dangle2 > max_turn_angle)
		{
			// try to turn the correct way, sometimes there seems to be errors!
			dangle = dangle2 - 360;
		}
		else if (dangle2 < -max_turn_angle)
		{
			// try to turn the correct way, sometimes there seems to be errors!
			dangle = dangle2 + 360;
		}

		Navit.NG__vehicle.vehicle_direction_delta = dangle;
		Navit.NG__vehicle.vehicle_zoom_delta = dzoom;

		Global_SmoothDrawing_stop = false;

		if (!Navit.PREF_use_smooth_drawing)
		{
			// disbaled via prefs
			return;
		}

		//if (Navit.NG__vehicle.vehicle_speed < 3)
		//{
		//	// too slow, dont use smooth moving
		//	return;
		//}

		//
		// Navit.NG__vehicle.vehicle_speed --> is in "km/h" !!
		// System.out.println("DEBUG_SMOOTH_DRIVING:v speed=" + Navit.NG__vehicle.vehicle_speed);
		//
		if ((Navit.NG__vehicle.vehicle_speed > 2) || (Navit.NG__vehicle.vehicle_direction_delta > 0))
		{
			if (Navit.PREF_use_more_smooth_drawing)
			{
				// really awesome smooth (80 steps / 80 fps)
				//Vehicle_smooth_moves_count = 79;
				//Vehicle_smooth_move_delay = 8;
				//
				// really awesome smooth (55 steps / 55 fps)
				//Vehicle_smooth_moves_count = 54;
				//Vehicle_smooth_move_delay = 17;
				//
				// really awesome smooth (30 steps / 30 fps)
				//Vehicle_smooth_moves_count = 29;
				//Vehicle_smooth_move_delay = 30;
				//
				// really awesome smooth (12 steps)
				//Vehicle_smooth_moves_count = 11;
				//Vehicle_smooth_move_delay = 81;
				//
				// really awesome smooth (11 steps)
				//Vehicle_smooth_moves_count = 10;
				//Vehicle_smooth_move_delay = 89;
				//
				// really awesome smooth (10 steps)
				//Vehicle_smooth_moves_count = 9;
				//Vehicle_smooth_move_delay = 96;
				//
				// really awesome smooth (9 steps)
				Vehicle_smooth_moves_count = 8;
				Vehicle_smooth_move_delay = 100;
			}
			else
			{
				// normal smooth (5 steps)
				Vehicle_smooth_moves_count = 4;
				Vehicle_smooth_move_delay = 205;
			}
		}
		else if ((Navit.NG__vehicle.vehicle_speed >= 0) && (Navit.NG__vehicle.vehicle_direction_delta > 0))
		{
			// normal smooth (5 steps)
			Vehicle_smooth_moves_count = 4;
			Vehicle_smooth_move_delay = 205;
			// (4 steps)
			//Vehicle_smooth_moves_count = 3;
			//Vehicle_smooth_move_delay = 160;
		}
		else
		{
			// +++ (on slow speed and no turn) --> no smooth move! +++
			return;
		}

		if (System.currentTimeMillis() > last_vehicle_position_timestamp + 1700L)
		{
			// last vehicle position was too long ago (1.7 secs ago, or longer)
			return;
		}

		if (Global_onTouch_fingerdown)
		{
			// dont use smooth moving while user moves the map
			return;
		}

		if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:angle1:dx=" + dx + " dy=" + dy + " da=" + dangle2);
		if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:angle2:dx=" + dx + " dy=" + dy + " da=" + dangle);
		if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:globalzoom:dzoom=" + Navit.NG__vehicle.vehicle_zoom_delta);
		if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:globalzoom:scale=" + Navit.GlobalScaleLevel);

		if ((Math.abs(dangle) < 2) && (Math.abs(dx) < 1) && (Math.abs(dy) < 1))
		{
			// the move is very small only
			return;
		}

		final int compensate = 1;
		dda = (float) dangle / (float) (Vehicle_smooth_moves_count + compensate);
		//dda_last = dangle - ((Vehicle_smooth_moves_count + compensate) * dda);

		ddx = (float) dx / (float) (Vehicle_smooth_moves_count + compensate);
		ddy = (float) dy / (float) (Vehicle_smooth_moves_count + compensate);
		//ddx_last = (float) dx - (float) (Vehicle_smooth_moves_count + compensate) * ddx;
		//ddy_last = (float) dy - (float) (Vehicle_smooth_moves_count + compensate) * ddy;

		final float k__ = 1f / 9f;
		final float d__ = 1f;
		if (Navit.NG__vehicle.vehicle_zoom_delta != 0)
		{
			// *FIX ME* ----------------------------
			ddz = (((k__ * (float) (-Navit.NG__vehicle.vehicle_zoom_delta) + d__) * 1) - 1) / (float) (Vehicle_smooth_moves_count + compensate);

			if (Navit.NG__vehicle.vehicle_zoom_delta == -4)
			{
				ddz = (0.23f) / (float) (Vehicle_smooth_moves_count + compensate);
			}
			else if (Navit.NG__vehicle.vehicle_zoom_delta == 4)
			{
				ddz = -(0.23f) / (float) (Vehicle_smooth_moves_count + compensate);
			}
			else if (Navit.NG__vehicle.vehicle_zoom_delta == -10)
			{
				ddz = (0.091f) / (float) (Vehicle_smooth_moves_count + compensate);
			}
			else if (Navit.NG__vehicle.vehicle_zoom_delta == 10)
			{
				ddz = -(0.091f) / (float) (Vehicle_smooth_moves_count + compensate);
			}
			// *FIX ME* ----------------------------

			// correct the smooth zoom value ----------------------------
			//
			// *FIX ME*
			// yes this is aweful, but i have not figured out how its calcuated correctly
			// so this will have to do for now
			// *FIX ME*
			if (Navit.GlobalScaleLevel > 300)
			{
				// no more smooth zoom at this zoom level!
				ddz = 0;
			}
			else if (Navit.GlobalScaleLevel > 200)
			{
				ddz = ddz / 4f;
			}
			else if (Navit.GlobalScaleLevel > 150)
			{
				ddz = ddz / 2f;
			}
			// correct the smooth zoom value
			//
			// *FIX ME*
			// yes this is aweful, but i have not figured out how its calcuated correctly
			// so this will have to do for now
			// *FIX ME*
			//
			// correct the smooth zoom value ----------------------------
		}
		else
		{
			ddz = 0;
		}

		if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:angle:ddx=" + ddx + " ddy=" + ddy + " dda=" + dda + " ddz=" + ddz);
		//if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:angle:ddx_l=" + ddx_last + " ddy_l=" + ddy_last + " dda_l=" + dda_last);

		// now move the map (to have smooth driving)
		//System.out.println("" + xxx + "--set_vehicle_values_delta-- START");
		//System.out.println("" + xxx + "--set_vehicle_values_delta-- Thread start");
		STT_A = new SmoothDriveThread_t_A();
		STT_A.start();
		//System.out.println("translate, rotate");		
		if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:" + xxx + "--set_vehicle_values_delta-- END ++++++++++++");
	}

	public static void return_generic_int_real(int id, int i)
	{
		//System.out.println("id=" + id + " i=" + i);

		if (id == 1)
		{
			// id=1 -> route_status
			NavitGraphics.navit_route_status = i;
			if (i == 0)
			{
				ZANaviBusySpinner.active = false;
				ZANaviBusySpinner.cancelAnim();
				busyspinner_.setVisibility(View.INVISIBLE);
				busyspinnertext_.setVisibility(View.INVISIBLE);
				busyspinnertext_.setText("");
				Navit.set_debug_messages1("");
				// DEBUG: clear route rectangle list
				route_rects.clear();
			}
			else if (i == 1)
			{
				ZANaviBusySpinner.active = true;
				busyspinnertext_.setText(Navit.get_text("Destination set")); // TRANS
				busyspinner_.setVisibility(View.VISIBLE);
				busyspinnertext_.setVisibility(View.VISIBLE);
				//System.out.println("invalidate 017");
				busyspinner_.postInvalidate();
				Navit.set_debug_messages1("Destination set");
			}
			else if (i == 3)
			{
				ZANaviBusySpinner.active = true;
				ZANaviBusySpinner.cancelAnim();
				busyspinner_.setVisibility(View.VISIBLE);
				busyspinnertext_.setText(Navit.get_text("No route found / Route blocked")); // TRANS
				busyspinnertext_.setVisibility(View.VISIBLE);
				Navit.set_debug_messages1("No route found / Route blocked");
			}
			else if (i == 5)
			{
				// status "5" is now ignored in c-code!!!
				ZANaviBusySpinner.active = true;
				busyspinner_.setVisibility(View.VISIBLE);
				//System.out.println("invalidate 018");
				busyspinner_.postInvalidate();
				busyspinnertext_.setText(Navit.get_text("Building route path")); // TRANS
				busyspinnertext_.setVisibility(View.VISIBLE);
				Navit.set_debug_messages1("Building route path");
			}
			else if (i == 13)
			{
				ZANaviBusySpinner.active = true;
				busyspinner_.setVisibility(View.VISIBLE);
				//System.out.println("invalidate 019");
				busyspinner_.postInvalidate();
				busyspinnertext_.setText(Navit.get_text("Building route graph")); // TRANS
				busyspinnertext_.setVisibility(View.VISIBLE);
				Navit.set_debug_messages1("Building route graph");
			}
			else if (i == 17)
			{
				ZANaviBusySpinner.active = false;
				ZANaviBusySpinner.cancelAnim();
				busyspinner_.setVisibility(View.INVISIBLE);
				busyspinnertext_.setText("");
				busyspinnertext_.setVisibility(View.INVISIBLE);
				Navit.set_debug_messages1("Route found");
			}
			else if (i == 33)
			{
				ZANaviBusySpinner.active = false;
				ZANaviBusySpinner.cancelAnim();
				busyspinner_.setVisibility(View.INVISIBLE);
				busyspinnertext_.setText("");
				busyspinnertext_.setVisibility(View.INVISIBLE);
				Navit.set_debug_messages1("Route found");
			}
		}
		else if (id == 2)
		{
			if (i == 1)
			{
				// id=2,1 -> map draw finished
				if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=false xx1");
				NavitGraphics.wait_for_redraw_map = false;
				try
				{
					//NavitAndroidOverlay.overlay_draw_thread1.stop_redraw();
					//NavitAndroidOverlay.overlay_draw_thread1.stop_me();
					//NavitAndroidOverlay.overlay_draw_thread1 = null;
				}
				catch (Exception e)
				{
					// e.printStackTrace();
				}
				try
				{
					//System.out.println("DO__DRAW:Java:remove wait text");
					//System.out.println("invalidate 020");
					NavitAOverlay_s.postInvalidate();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else if (i == 3)
			{
				// id=2,3 -> mapdraw cancel signal
				//System.out.println("DO__DRAW:Java:cancel map");
				// draw_map_one_shot = true;
				//copy_backwards_map_buffer();
				//draw_reset_factors = true;
				//view_s.postInvalidate();

			}
			else if (i == 2)
			{
				// id=2,2 -> mapdraw ready signal
				//System.out.println("DO__DRAW:Java:refresh map");
				// draw_map_one_shot = true;
				if (DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:map draw ready");

				if ((Navit.PREF_use_smooth_drawing) && (Navit.NG__vehicle.vehicle_speed > 12))
				{
					// delay vehicle and map update just a tiny little bit here!!  -- very experimental, also delays gps position !!!

					STT_B = new SmoothDriveThread_t_B();
					STT_B.start();
				}
				else
				{
					copy_map_buffer();
					draw_reset_factors = true;
					view_s.postInvalidate();
					//view_s.paint_me();
				}
			}
		}
		else if (id == 3)
		{
			// id=3 -> new scale (zoom) number
			// 1       (=2^0)	order: ~16	-> fully zoomed in
			// 1048576 (=2^20)  order:  -1	-> zoomed out to world
			if (DEBUG_TOUCH) System.out.println("scale=" + i);
			Navit.GlobalScaleLevel = i;
		}
		else if (id == 4)
		{
			Navit.set_debug_messages1("Destination reached");
			NavitGraphics.navit_route_status = 0;
		}
		else if (id == 5)
		{
			Navit.set_debug_messages1("Waypoint reached");
		}
		else if (id == 6)
		{
			// id=6 -> mean time for drawing map to screen (in 1/1000 of a second)
			Navit.set_debug_messages4("draw:" + (float) ((float) i / 1000f) + "s");
		}
	}

	public static Bitmap rotate_and_scale_bitmap(Bitmap in, int w, int h, int angle)
	{
		int width = in.getWidth();
		int height = in.getHeight();

		if ((width == w) && (height == h) && (angle == 0))
		{
			return in;
		}

		// System.out.println(" @@@@@@ w="+w+" h="+h);

		// calculate the scale
		float scaleWidth = ((float) w) / width;
		float scaleHeight = ((float) h) / height;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		matrix.postRotate(angle);

		// recreate the new Bitmap
		return (Bitmap.createBitmap(in, 0, 0, width, height, matrix, true));
	}

	public static class route_rect
	{
		int x1;
		int y1;
		int x2;
		int y2;
		int order;
	};

	public static ArrayList<route_rect> route_rects = new ArrayList<route_rect>();

	// value are NOT in pixels!! they need to be converted to pixels before drawing
	public static void send_route_rect_to_java(int x1, int y1, int x2, int y2, int order)
	{
		//System.out.println("send_route_rect_to_java: " + x1 + "," + y1 + " " + x2 + "," + y2 + " o=" + order);

		//String left_top_on_screen_string = CallbackGeoCalc(11, x1, y1);
		//String tmp[] = left_top_on_screen_string.split(":", 2);
		//int pixel_top_left_x = Integer.parseInt(tmp[0]);
		//int pixel_top_left_y = Integer.parseInt(tmp[1]);
		//System.out.println(" " + pixel_top_left_x + "," + pixel_top_left_y);

		//left_top_on_screen_string = CallbackGeoCalc(11, x2, y2);
		//tmp = left_top_on_screen_string.split(":", 2);
		//pixel_top_left_x = Integer.parseInt(tmp[0]);
		//pixel_top_left_y = Integer.parseInt(tmp[1]);
		//System.out.println(" " + pixel_top_left_x + "," + pixel_top_left_y);

		if (route_rects == null)
		{
			route_rects = new ArrayList<route_rect>();
		}

		route_rect rr = new route_rect();
		rr.x1 = x1;
		rr.y1 = y1;
		rr.x2 = x2;
		rr.y2 = y2;
		rr.order = order;
		route_rects.add(rr);
	}

	public static int dp_to_px(int dp)
	{
		return (int) (((float) dp / Global_dpi_factor) + 0.5);
	}

	public static int px_to_dp(int px)
	{
		return (int) (((float) px * Global_dpi_factor) + 0.5);
	}
}
