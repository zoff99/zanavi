/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 -2012 Zoff <zoff@zoff.cc>
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zoffcc.applications.zanavi.Navit.geo_coord;

public class NavitAndroidOverlay extends View
{
	public Boolean draw_bubble = false;
	public Boolean draw_bubble_first = false;
	public static Boolean confirmed_bubble = false;
	public static int confirmed_bubble_part = 0; // 0 -> left side, 1 -> right side
	public long bubble_showing_since = 0L;
	public static long bubble_max_showing_timespan = 5000L; // 5 secs.
	// public RectF follow_button_rect = new RectF(-100, 1, 1, 1);
	//public RectF voice_recog_rect = new RectF(-100, 1, 1, 1);
	//public RectF zoomin_button_rect = new RectF(-100, 1, 1, 1);
	//public RectF zoomout_button_rect = new RectF(-100, 1, 1, 1);
	//public RectF mapdrawing_button_rect = new RectF(-100, 1, 1, 1);
	public static int zoomin_ltx = 0;
	public static int zoomin_lty = 0;
	public static int zoomout_ltx = 0;
	public static int zoomout_lty = 0;
	public static int mapdrawing_ltx = 0;
	public static int mapdrawing_lty = 0;
	public int mCanvasHeight = 1;
	public int mCanvasWidth = 1;
	public static float draw_factor = 1.0f;
	public static boolean voice_rec_bar_visible = false;
	public static boolean voice_rec_bar_visible2 = false;
	public static int voice_rec_bar_x = -10;
	public static int voice_rec_bar_y = -10;
	public static int voice_rec_bar_limit = 10;
	Matrix lanes_scaleMatrix = new Matrix();
	Matrix lanes_transMatrix = new Matrix();
	RectF lanes_rectF = new RectF();

	public static boolean measure_mode = false;
	public static int measure_result_meters = -1;
	static int measure_1_x = 0;
	static int measure_1_y = 0;
	static int measure_2_x = 0;
	static int measure_2_y = 0;
	static boolean measure_first = true;
	static boolean measure_valid = false;
	static Paint paint_measure = new Paint();
	static int measure_point_radius = 3;

	private Paint paint_replay = new Paint();
	private Paint paint_rewind_small = new Paint();
	private Paint paint_bubble_hotpoint = new Paint();

	Message msg_dd = null;
	Bundle b_dd = new Bundle();

	RectF bounds_speedwarning = new RectF(120, 800, 120 + 200, 800 + 200);
	Paint paint_speedwarning = new Paint(0);

	static boolean no_draw = false;

	static Path pathForTurn = new Path();

	public static BubbleThread bubble_thread = null;

	public static NavitGraphics.OverlayDrawThread overlay_draw_thread1;

	private class BubbleThread extends Thread
	{
		private Boolean running;
		private long bubble_showing_since = 0L;
		private NavitAndroidOverlay a_overlay = null;

		BubbleThread(NavitAndroidOverlay a_ov)
		{
			this.running = true;
			this.a_overlay = a_ov;
			this.bubble_showing_since = 0L;
			//Log.e("Navit", "BubbleThread created");
		}

		public void stop_me()
		{
			this.running = false;
		}

		public void run()
		{
			this.bubble_showing_since = System.currentTimeMillis();

			// Log.e("Navit", "BubbleThread started");
			while (this.running)
			{
				if ((System.currentTimeMillis() - this.bubble_showing_since) > bubble_max_showing_timespan)
				{
					//Log.e("Navit", "BubbleThread: bubble displaying too long, hide it");
					// with invalidate we call the onDraw() function, that will take care of it
					//System.out.println("invalidate 001");
					this.a_overlay.postInvalidate();
					this.running = false;
				}
				else
				{
					try
					{
						Thread.sleep(280);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
			}
			//Log.e("Navit", "BubbleThread ended");
		}
	}

	public static class NavitAndroidOverlayBubble
	{
		int x;
		int y;
		String text = null;
	}

	NavitAndroidOverlayBubble bubble_001 = null;

	public NavitAndroidOverlay(Context context)
	{
		super(context);

		paint_measure.setColor(Color.RED);
		paint_measure.setStyle(Paint.Style.STROKE);
		paint_measure.setStrokeWidth(NavitGraphics.dp_to_px(6));
		measure_point_radius = NavitGraphics.dp_to_px(10);

		paint_replay.setColor(0xaa444444); // grey
		paint_replay.setStyle(Paint.Style.FILL_AND_STROKE);

		paint_rewind_small.setColor(0xaa440202); // red
		paint_rewind_small.setStyle(Paint.Style.FILL_AND_STROKE);

		paint_bubble_hotpoint.setColor(Color.RED); // red
		paint_bubble_hotpoint.setStyle(Paint.Style.STROKE);
		paint_bubble_hotpoint.setStrokeWidth(NavitGraphics.dp_to_px(6));
		paint_bubble_hotpoint.setAntiAlias(true);
		paint_bubble_hotpoint.setDither(true);
	}

	public NavitAndroidOverlay(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		paint_measure.setColor(Color.RED);
		paint_measure.setStyle(Paint.Style.STROKE);
		paint_measure.setStrokeWidth(NavitGraphics.dp_to_px(6));
		measure_point_radius = NavitGraphics.dp_to_px(10);

		paint_replay.setColor(0xaa444444);
		paint_replay.setStyle(Paint.Style.FILL_AND_STROKE);

		paint_rewind_small.setColor(0xaa440202); // red
		paint_rewind_small.setStyle(Paint.Style.FILL_AND_STROKE);

		paint_bubble_hotpoint.setColor(Color.RED); // red
		paint_bubble_hotpoint.setStyle(Paint.Style.STROKE);
		paint_bubble_hotpoint.setStrokeWidth(NavitGraphics.dp_to_px(6));
		paint_bubble_hotpoint.setAntiAlias(true);
		paint_bubble_hotpoint.setDither(true);
	}

	public void show_bubble()
	{
		//System.out.println("NavitAndroidOverlay -> show_bubble");
		if (!this.draw_bubble)
		{
			try
			{
				Message msg = Navit.Navit_progress_h.obtainMessage();
				Bundle b = new Bundle();
				msg.what = 34;
				msg.setData(b);
				Navit.Navit_progress_h.sendMessage(msg);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			confirmed_bubble = false;
			this.draw_bubble = true;
			this.draw_bubble_first = true;
			this.bubble_showing_since = System.currentTimeMillis();
			bubble_thread = new BubbleThread(this);
			bubble_thread.start();

			// test test DEBUG
			/*
			 * Message msg = new Message();
			 * Bundle b = new Bundle();
			 * b.putInt("Callback", 4);
			 * b.putInt("x", this.bubble_001.x);
			 * b.putInt("y", this.bubble_001.y);
			 * msg.setData(b);
			 * Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
			 */
		}
	}

	public Boolean get_show_bubble()
	{
		return this.draw_bubble;
	}

	public void hide_bubble()
	{
		// Log.e("NavitGraphics", "NavitAndroidOverlay -> hide_bubble");
		confirmed_bubble = false;
		this.draw_bubble = false;
		this.draw_bubble_first = false;

		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 35;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.bubble_showing_since = 0L;
		try
		{
			bubble_thread.stop_me();
			// bubble_thread.stop();
		}
		catch (Exception e)
		{

		}
		//this.postInvalidate();
	}

	public void set_bubble(NavitAndroidOverlayBubble b)
	{
		this.bubble_001 = b;
	}

	public void whats_button_clicked()
	{
		// bubble touched to confirm destination
		confirmed_bubble = true;
		confirmed_bubble_part = 1;
		// draw confirmed bubble
		//System.out.println("invalidate 003");
		this.postInvalidate();

		// whats here?
		String item_dump_pretty = "";
		if (Navit.GFX_OVERSPILL)
		{
			item_dump_pretty = NavitGraphics.CallbackGeoCalc(10, (this.bubble_001.x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (this.bubble_001.y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
		}
		else
		{
			item_dump_pretty = NavitGraphics.CallbackGeoCalc(10, this.bubble_001.x * NavitGraphics.Global_dpi_factor, this.bubble_001.y * NavitGraphics.Global_dpi_factor);
		}

		try
		{
			String item_dump_pretty_parsed = "";
			String[] item_dump_lines = item_dump_pretty.split("\n");
			int jk = 0;
			String sep = "";
			for (jk = 0; jk < item_dump_lines.length; jk++)
			{
				if (item_dump_lines[jk].startsWith("+*TYPE*+:"))
				{
					item_dump_pretty_parsed = item_dump_pretty_parsed + sep + item_dump_lines[jk].substring(9);
				}
				else if (item_dump_lines[jk].startsWith("flags="))
				{

				}
				else if (item_dump_lines[jk].startsWith("maxspeed="))
				{
					item_dump_pretty_parsed = item_dump_pretty_parsed + sep + item_dump_lines[jk];
				}
				else if (item_dump_lines[jk].startsWith("label="))
				{
					item_dump_pretty_parsed = item_dump_pretty_parsed + sep + item_dump_lines[jk].substring(6);
				}
				else if (item_dump_lines[jk].startsWith("street_name="))
				{
					item_dump_pretty_parsed = item_dump_pretty_parsed + sep + item_dump_lines[jk].substring(12);
				}
				else if (item_dump_lines[jk].startsWith("street_name_systematic="))
				{
					item_dump_pretty_parsed = item_dump_pretty_parsed + sep + item_dump_lines[jk].substring(23);
				}
				else
				{
					item_dump_pretty_parsed = item_dump_pretty_parsed + sep + item_dump_lines[jk];
				}

				if (jk == 0)
				{
					sep = "\n";
				}
			}

			Navit.generic_alert_box.setMessage(item_dump_pretty_parsed).setPositiveButton(Navit.get_text("Ok"), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					// Handle Ok
				}
			}).create();
			Navit.generic_alert_box.setCancelable(true);
			Navit.generic_alert_box.setTitle(Navit.get_text("What's here")); // TRANS
			Navit.generic_alert_box.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.hide_bubble();
		this.postInvalidate();
	}

	public void addto_button_clicked()
	{
		// bubble touched to confirm destination
		confirmed_bubble = true;
		confirmed_bubble_part = 0;
		// draw confirmed bubble
		//System.out.println("invalidate 002");
		this.postInvalidate();
		String dest_name = "Point on Screen";

		// remeber recent dest.
		try
		{
			if (Navit.GFX_OVERSPILL)
			{
				dest_name = NavitGraphics.CallbackGeoCalc(8, (this.bubble_001.x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (this.bubble_001.y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
			}
			else
			{
				dest_name = NavitGraphics.CallbackGeoCalc(8, this.bubble_001.x * NavitGraphics.Global_dpi_factor, this.bubble_001.y * NavitGraphics.Global_dpi_factor);
			}

			// System.out.println("x:"+dest_name+":y");
			if ((dest_name.equals(" ")) || (dest_name == null))
			{
				dest_name = "Point on Screen";
			}

			// Navit.remember_destination_xy(dest_name, this.bubble_001.x, this.bubble_001.y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Navit.geo_coord gc1 = Navit.px_to_geo(this.bubble_001.x, this.bubble_001.y);

		try
		{
			Navit.remember_destination(dest_name, "" + gc1.Latitude, "" + gc1.Longitude);
			// save points
			Navit.write_map_points();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.hide_bubble();
		this.postInvalidate();
	}

	public void drive_button_clicked()
	{
		// bubble touched to confirm destination
		confirmed_bubble = true;
		confirmed_bubble_part = 0;
		// draw confirmed bubble
		//System.out.println("invalidate 002");
		this.postInvalidate();
		String dest_name = "Point on Screen";

		// remeber recent dest.
		try
		{
			if (Navit.GFX_OVERSPILL)
			{
				dest_name = NavitGraphics.CallbackGeoCalc(8, (this.bubble_001.x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (this.bubble_001.y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
			}
			else
			{
				dest_name = NavitGraphics.CallbackGeoCalc(8, this.bubble_001.x * NavitGraphics.Global_dpi_factor, this.bubble_001.y * NavitGraphics.Global_dpi_factor);
			}

			// System.out.println("x:"+dest_name+":y");
			if ((dest_name.equals(" ")) || (dest_name == null))
			{
				dest_name = "Point on Screen";
			}

			// Navit.remember_destination_xy(dest_name, this.bubble_001.x, this.bubble_001.y);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Navit.geo_coord gc1 = Navit.px_to_geo(this.bubble_001.x, this.bubble_001.y);

		Navit.route_wrapper(dest_name, 0, 0, false, gc1.Latitude, gc1.Longitude, true);

		final Thread zoom_to_route_008 = new Thread()
		{
			int wait = 1;
			int count = 0;
			int max_count = 60;

			@Override
			public void run()
			{
				while (wait == 1)
				{
					try
					{
						if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33))
						{
							Navit.zoom_to_route();
							wait = 0;
						}
						else
						{
							wait = 1;
						}

						count++;
						if (count > max_count)
						{
							wait = 0;
						}
						else
						{
							Thread.sleep(400);
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		};
		zoom_to_route_008.start();

		try
		{
			Navit.follow_button_on();
		}
		catch (Exception e2)
		{
			e2.printStackTrace();
		}

		this.hide_bubble();
		this.postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);

		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();

		//Log.e("NavitGraphics", "NavitAndroidOverlay -> action=" + action);

		//if (action == MotionEvent.ACTION_UP)
		//{
		//	Log.e("NavitGraphics", "NavitAndroidOverlay -> onTouchEvent ACTION_UP");
		//	if (NavitGraphics.in_map)
		//	{
		//		return false;
		//	}
		//}

		if (NavitAndroidOverlay.measure_mode)
		{

			if (action == MotionEvent.ACTION_UP)
			{
				if (measure_first)
				{
					measure_first = false;
					measure_1_x = x;
					measure_1_y = y;

					measure_valid = false;
				}
				else
				{
					measure_first = true;
					measure_2_x = x;
					measure_2_y = y;

					geo_coord gc1 = Navit.px_to_geo(measure_1_x, measure_1_y);
					geo_coord gc2 = Navit.px_to_geo(measure_2_x, measure_2_y);

					// System.out.println("XXXXXXXXXXXXXX:3a " + gc1.Latitude + " " + gc1.Longitude + " " + gc2.Latitude + " " + gc2.Longitude);

					int m = NavitVehicle.distanceBetween(gc1.Latitude, gc1.Longitude, gc2.Latitude, gc2.Longitude);

					measure_valid = true;

					measure_result_meters = m;
					ZANaviLinearLayout.redraw_OSD(1);
					NavitGraphics.NavitAOverlay_s.postInvalidate();
				}
			}

			return true;
		}

		if (ZANaviDebugReceiver.is_replaying)
		{
			if (action == MotionEvent.ACTION_DOWN)
			{
				Rect r = new Rect(40, mCanvasHeight / 5, 40 + NavitGraphics.dp_to_px(50), (mCanvasHeight / 5) + NavitGraphics.dp_to_px(50));
				if (r.contains(x, y))
				{
					ZANaviDebugReceiver.DR_skip();
				}

				r = new Rect(40 + NavitGraphics.dp_to_px(80), mCanvasHeight / 5, 40 + NavitGraphics.dp_to_px(50) + NavitGraphics.dp_to_px(80), (mCanvasHeight / 5) + NavitGraphics.dp_to_px(50));
				if (r.contains(x, y))
				{
					ZANaviDebugReceiver.DR_rewind_small();
				}

			}
		}

		if (action == MotionEvent.ACTION_DOWN)
		{
			if ((this.draw_bubble) && (!confirmed_bubble))
			{
				int dx = (int) ((20 / 1.5f) * draw_factor);
				int dy = (int) ((-100 / 1.5f) * draw_factor);
				int bubble_size_x = (int) ((150 / 1.5f) * draw_factor);
				int bubble_size_y = (int) ((60 / 1.5f) * draw_factor);
				RectF box_rect_left = new RectF(this.bubble_001.x + dx, this.bubble_001.y + dy, this.bubble_001.x + bubble_size_x / 2 + dx, this.bubble_001.y + bubble_size_y + dy);
				RectF box_rect_right = new RectF(this.bubble_001.x + bubble_size_x / 2 + dx, this.bubble_001.y + dy, this.bubble_001.x + bubble_size_x + dx, this.bubble_001.y + bubble_size_y + dy);

				if (box_rect_left.contains(x, y))
				{
				}
				//				else if (2 == 1)
				//				{
				//					// share the current location with your friends
				//					share_location("48.422", "16.34", "Meeting Point");
				//				}
				else if (box_rect_right.contains(x, y))
				{
				}
			}
		}

		// false -> we dont use this event, give it to other layers
		return false;
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		// super.onSizeChanged(w, h, oldw, oldh);
		this.mCanvasWidth = w;
		this.mCanvasHeight = h;

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
		}

		int w_1 = (int) ((10f / 1.5f) * draw_factor);
		int h_1 = (int) ((200f / 1.5f) * draw_factor);
		// this.follow_button_rect = new RectF(this.mCanvasWidth - Navit.follow_current.getWidth() - w_1, this.mCanvasHeight - Navit.follow_current.getHeight() - h_1, this.mCanvasWidth - w_1, this.mCanvasHeight - h_1);

		// rect to slide open voice recognition
		// this.voice_recog_rect = new RectF(0, (this.mCanvasHeight / 2f) - 100 * draw_factor, 16 * draw_factor, (this.mCanvasHeight / 2f) + 100 * draw_factor);
		NavitAndroidOverlay.voice_rec_bar_limit = this.mCanvasWidth / 2;

		/*
		 * int w_2 = (int) ((0 / 1.5f) * draw_factor) + 5;
		 * int h_2 = (int) ((0f / 1.5f) * draw_factor) + 5;
		 * int h_button_zoom = Navit.zoomin.getHeight();
		 * zoomin_ltx = w_2;
		 * zoomin_lty = h_2;
		 * this.zoomin_button_rect = new RectF(w_2, h_2, Navit.zoomin.getWidth() + w_2, h_button_zoom + h_2);
		 */

		int w_2 = (int) ((0 / 1.5f) * draw_factor) + 5;
		int h_2 = (int) ((70f / 1.5f) * draw_factor) + 5 + 25;
		int h_button_zoom = Navit.zoomin.getHeight();
		zoomin_ltx = w_2;
		zoomin_lty = h_2;
		// this.zoomin_button_rect = new RectF(w_2, h_2, Navit.zoomin.getWidth() + w_2, h_button_zoom + h_2);

		w_2 = (int) ((0 / 1.5f) * draw_factor) + 5;
		h_2 = (int) ((2 * 70f / 1.5f) * draw_factor) + 5 + 25 + 25;
		zoomout_ltx = w_2;
		zoomout_lty = h_2;
		// this.zoomout_button_rect = new RectF(w_2, h_2, Navit.zoomout.getWidth() + w_2, h_button_zoom + h_2);

		int mapdrawing_width = (int) ((75f / 1.5f) * draw_factor);
		int mapdrawing_height = (int) ((75f / 1.5f) * draw_factor);
		w_2 = (int) ((this.mCanvasWidth - mapdrawing_width) - 5);
		h_2 = (int) ((70f / 1.5f) * draw_factor) + 5 + 25;
		mapdrawing_ltx = w_2;
		mapdrawing_lty = h_2;
		// this.mapdrawing_button_rect = new RectF(w_2, h_2, mapdrawing_width + w_2, mapdrawing_height + h_2);

		// on small screens (like the blackberry Q10) the follow button overlaps the 2d/3d button (mapdrawing button)
		// check and then move 2d/3d button to the left
		// boolean must_move_2d3d_button = false;
		//		if (mapdrawing_button_rect.contains(follow_button_rect.left, follow_button_rect.top))
		//		{
		//			must_move_2d3d_button = true;
		//		}
		//		else if (mapdrawing_button_rect.contains(follow_button_rect.right, follow_button_rect.top))
		//		{
		//			must_move_2d3d_button = true;
		//		}
		//		else if (mapdrawing_button_rect.contains(follow_button_rect.left, follow_button_rect.bottom))
		//		{
		//			must_move_2d3d_button = true;
		//		}
		//		else if (mapdrawing_button_rect.contains(follow_button_rect.left, follow_button_rect.bottom))
		//		{
		//			must_move_2d3d_button = true;
		//		}
		//
		//		if (must_move_2d3d_button)
		//		{
		//			// move to left of follow button
		//			this.mapdrawing_button_rect = new RectF(w_2 - (this.mCanvasWidth - follow_button_rect.left), h_2, mapdrawing_width + w_2 - (this.mCanvasWidth - follow_button_rect.left), mapdrawing_height + h_2);
		//		}

		// put menu button below "2D/3D" button
		if (Navit.metrics.densityDpi >= 320) // && (Navit.PREF_shrink_on_high_dpi))
		{
			Navit.menu_button_rect = new RectF(this.mCanvasWidth - 10 - Navit.menu_button.getWidth(), 10 + mapdrawing_height + h_2 + 5, this.mCanvasWidth - 10, Navit.menu_button.getHeight() + 10 + mapdrawing_height + h_2 + 5);
			Navit.menu_button_rect_touch = new RectF(this.mCanvasWidth - 10 - Navit.menu_button.getWidth(), 10 + mapdrawing_height + h_2 + 5, this.mCanvasWidth - 10, Navit.menu_button.getHeight() + 10 + mapdrawing_height + h_2 + 5);
		}
		else
		{
			final int addon_left = 0; //50;
			final int addon_down = 0; //40;
			Navit.menu_button_rect = new RectF(this.mCanvasWidth - 10 - Navit.menu_button.getWidth(), 10 + mapdrawing_height + h_2 + 5, this.mCanvasWidth - 10, Navit.menu_button.getHeight() + 10 + mapdrawing_height + h_2 + 5);
			Navit.menu_button_rect_touch = new RectF(this.mCanvasWidth - 10 - Navit.menu_button.getWidth() - addon_left, 10 + mapdrawing_height + h_2 + 5, this.mCanvasWidth - 10, Navit.menu_button.getHeight() + 10 + mapdrawing_height + h_2 + 5 + addon_down);
		}
	}

	int get_lanes_kind_count(int parsed_num_lanes, String[] lanes_split)
	{
		final int num_of_kinds = 6;
		int[] lanes_kind = new int[num_of_kinds];
		int lanes_kind_count = 0;

		int k = 0;
		for (k = 0; k < num_of_kinds; k++)
		{
			lanes_kind[k] = 0; // reset all
		}

		int j = 0;
		for (j = 0; j < parsed_num_lanes; j++)
		{
			String lanes_split_sub[] = lanes_split[j].split(";");
			int parsed_num_lanes_sub = lanes_split_sub.length;

			k = 0;
			String single_arrow = "";
			for (k = 0; k < parsed_num_lanes_sub; k++)
			{
				single_arrow = lanes_split_sub[k].replaceAll("\\s", "");

				// none					3
				// through				3
				// left					1
				// right				5
				// slight_left			2
				// slight_right			4
				// sharp_left			1
				// sharp_right			5
				// mergeto_left			-
				// mergeto_right		-
				// merge_to_left		-
				// merge_to_right		-

				if (single_arrow.equalsIgnoreCase("sharp_left"))
				{
					lanes_kind[1] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("sharp_right"))
				{
					lanes_kind[5] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("none"))
				{
					lanes_kind[3] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("through"))
				{
					lanes_kind[3] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("left"))
				{
					lanes_kind[1] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("right"))
				{
					lanes_kind[5] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("slight_left"))
				{
					lanes_kind[2] = 1;
				}
				else if (single_arrow.equalsIgnoreCase("slight_right"))
				{
					lanes_kind[4] = 1;
				}

			}
		}

		lanes_kind_count = 0;
		for (k = 0; k < num_of_kinds; k++)
		{
			if (lanes_kind[k] == 1)
			{
				lanes_kind_count++;
			}
		}

		return lanes_kind_count;
	}

	@SuppressLint("NewApi")
	public void onDraw(Canvas c)
	{

		if (measure_mode)
		{
			if (measure_valid)
			{
				// draw the line
				c.drawLine(measure_1_x, measure_1_y, measure_2_x, measure_2_y, paint_measure);
				c.drawCircle(measure_1_x, measure_1_y, measure_point_radius, paint_measure);
				c.drawCircle(measure_2_x, measure_2_y, measure_point_radius, paint_measure);
			}

			return;
		}

		if (ZANaviDebugReceiver.is_replaying)
		{
			c.drawRect(40, mCanvasHeight / 5, 40 + NavitGraphics.dp_to_px(50), (mCanvasHeight / 5) + NavitGraphics.dp_to_px(50), paint_replay);
			c.drawRect(40 + NavitGraphics.dp_to_px(80), mCanvasHeight / 5, 40 + NavitGraphics.dp_to_px(50) + NavitGraphics.dp_to_px(80), (mCanvasHeight / 5) + NavitGraphics.dp_to_px(50), paint_rewind_small);
		}

		if (this.draw_bubble)
		{
			if ((System.currentTimeMillis() - this.bubble_showing_since) > bubble_max_showing_timespan)
			{
				// bubble has been showing too log, hide it
				this.hide_bubble();
			}
		}

		if (this.draw_bubble)
		{
			if (Navit.PAINT_OLD_API)
			{
				c.drawCircle(this.bubble_001.x, this.bubble_001.y, NavitGraphics.dp_to_px(10), paint_bubble_hotpoint);
			}

			if (draw_bubble_first)
			{
				try
				{
					System.out.println("BB:09");
					msg_dd = Navit.Navit_progress_h.obtainMessage();
					msg_dd.what = 36;
					msg_dd.setData(b_dd);
					Navit.Navit_progress_h.sendMessage(msg_dd);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					System.out.println("BB:10");
					msg_dd = Navit.Navit_progress_h.obtainMessage();
					msg_dd.what = 37;
					msg_dd.setData(b_dd);
					Navit.Navit_progress_h.sendMessage(msg_dd);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				draw_bubble_first = false;
			}
		}

		if (Navit.p.PREF_item_dump)
		{
			if (!Navit.debug_item_dump.equals(""))
			{
				Paint paint = new Paint(0);
				paint.setAntiAlias(true);
				paint.setColor(Color.BLUE);
				// paint.setAlpha(240);
				// paint.setColor(Color.parseColor("#888888"));
				paint.setStrokeWidth(2);
				paint.setTextSize(52);

				// System.out.println("XX11 " + Navit.debug_item_dump);
				String[] s = Navit.debug_item_dump.split("\n");
				// System.out.println("XX11 " + s.length);

				int i3;
				int i4 = 0;
				int max_char = 37;

				int start_x = 110;
				int start_y = 300;
				int line_size_y = 54;
				int one_indent = 24;

				for (i3 = 0; i3 < s.length; i3++)
				{
					if (s[i3].length() > max_char)
					{
						int j3;
						int indent = 0;
						for (j3 = 0; j3 < (int) (s[i3].length() / max_char); j3++)
						{
							if (j3 > 0)
							{
								indent = one_indent;
							}
							c.drawText(s[i3].substring(j3 * max_char, ((j3 + 1) * max_char) - 0), start_x + indent, start_y + (i4 * line_size_y), paint);
							i4++;
						}
						if ((((int) (s[i3].length() / max_char)) * max_char) < s[i3].length())
						{
							indent = one_indent;
							c.drawText(s[i3].substring(j3 * max_char), start_x + indent, start_y + (i4 * line_size_y), paint);
							i4++;
						}
					}
					else
					{
						c.drawText(s[i3], start_x, start_y + (i4 * line_size_y), paint);
						i4++;
					}
				}
			}
		}

	}

	public static void cleanup_measure_mode()
	{
		measure_valid = false;
		measure_result_meters = -1;
		NavitGraphics.NavitAOverlay_s.postInvalidate();
	}
}
