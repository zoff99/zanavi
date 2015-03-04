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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class NavitAndroidOverlay extends View
{
	public Boolean draw_bubble = false;
	public static Boolean confirmed_bubble = false;
	public static int confirmed_bubble_part = 0; // 0 -> left side, 1 -> right side
	public long bubble_showing_since = 0L;
	public static long bubble_max_showing_timespan = 5000L; // 5 secs.
	public RectF follow_button_rect = new RectF(-100, 1, 1, 1);
	public RectF voice_recog_rect = new RectF(-100, 1, 1, 1);
	public RectF zoomin_button_rect = new RectF(-100, 1, 1, 1);
	public RectF zoomout_button_rect = new RectF(-100, 1, 1, 1);
	public RectF mapdrawing_button_rect = new RectF(-100, 1, 1, 1);
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

	private NavitAndroidOverlayBubble bubble_001 = null;

	public NavitAndroidOverlay(Context context)
	{
		super(context);
	}

	public NavitAndroidOverlay(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void show_bubble()
	{
		//System.out.println("NavitAndroidOverlay -> show_bubble");
		if (!this.draw_bubble)
		{
			confirmed_bubble = false;
			this.draw_bubble = true;
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

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//if (NavitGraphics.wait_for_redraw_map == true)
		//{
		//	Log.e("NavitGraphics", "GD NavitAndroidOverlay GD -> onTouchEvent");
		//	Message msg = new Message();
		//	Bundle b = new Bundle();
		//	b.putInt("Callback", 50);
		//	msg.setData(b);
		//	Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
		//}
		//else
		//{
		//	// Log.e("NavitGraphics", "NavitAndroidOverlay -> onTouchEvent");
		//}
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
						dest_name = NavitGraphics.CallbackGeoCalc(8, this.bubble_001.x * NavitGraphics.Global_dpi_factor, this.bubble_001.y * NavitGraphics.Global_dpi_factor);
						// System.out.println("x:"+dest_name+":y");
						if ((dest_name.equals(" ")) || (dest_name == null))
						{
							dest_name = "Point on Screen";
						}

						Navit.remember_destination_xy(dest_name, this.bubble_001.x, this.bubble_001.y);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					// DEBUG: clear route rectangle list
					NavitGraphics.route_rects.clear();

					if (NavitGraphics.navit_route_status == 0)
					{
						Navit.destination_set();

						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 4);
						b.putInt("x", (int) ((float) this.bubble_001.x * NavitGraphics.Global_dpi_factor));
						b.putInt("y", (int) ((float) this.bubble_001.y * NavitGraphics.Global_dpi_factor));
						msg.setData(b);
						Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
					}
					else
					{
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 49);
						b.putInt("x", (int) ((float) this.bubble_001.x * NavitGraphics.Global_dpi_factor));
						b.putInt("y", (int) ((float) this.bubble_001.y * NavitGraphics.Global_dpi_factor));
						msg.setData(b);
						Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
					}

					final Thread zoom_to_route_008 = new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								Thread.sleep(1000);
								// --------- zoom to route ---------
								Message msg = new Message();
								Bundle b = new Bundle();
								b.putInt("Callback", 17);
								msg.setData(b);
								Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
								// --------- zoom to route ---------
							}
							catch (Exception e)
							{
							}
						}
					};
					zoom_to_route_008.start();

					//					try
					//					{
					//						Navit.follow_button_on();
					//					}
					//					catch (Exception e2)
					//					{
					//						e2.printStackTrace();
					//					}

					this.hide_bubble();
					this.postInvalidate();

					// consume the event
					return true;
				}
				//				else if (2 == 1)
				//				{
				//					// share the current location with your friends
				//					share_location("48.422", "16.34", "Meeting Point");
				//				}
				else if (box_rect_right.contains(x, y))
				{
					// bubble touched to confirm destination
					confirmed_bubble = true;
					confirmed_bubble_part = 1;
					// draw confirmed bubble
					//System.out.println("invalidate 003");
					this.postInvalidate();

					// whats here?
					String item_dump_pretty = NavitGraphics.CallbackGeoCalc(10, this.bubble_001.x * NavitGraphics.Global_dpi_factor, this.bubble_001.y * NavitGraphics.Global_dpi_factor);
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

					return true;
				}

			}
		}

		if (action == MotionEvent.ACTION_DOWN)
		{
			//Log.e("NavitGraphics", "NavitAndroidOverlay -> onTouchEvent ACTION_DOWN");
			if (this.follow_button_rect.contains(x, y))
			{
				if (NavitGraphics.in_map)
				{
					// toggle follow mode
					Navit.toggle_follow_button();
					//System.out.println("invalidate 004");
					this.postInvalidate();
					//NavitGraphics.NavitAOSDJava_.postInvalidate();
					// consume the event
					return true;
				}
			}
			//			else if ((!Navit.has_hw_menu_button) && (Navit.menu_button_rect_touch.contains(x, y)))
			//			{
			//				// open the options menu
			//				try
			//				{
			//					Message msg = Navit.Navit_progress_h.obtainMessage();
			//					Bundle b = new Bundle();
			//					msg.what = 18;
			//					msg.setData(b);
			//					Navit.Navit_progress_h.sendMessage(msg);
			//				}
			//				catch (Exception e)
			//				{
			//					e.printStackTrace();
			//				}
			//			}
			else if (this.mapdrawing_button_rect.contains(x, y))
			{
				if (NavitGraphics.in_map)
				{
					try
					{

						if (Navit.PREF_show_2d3d_toggle)
						{
							// toggle "2d/3d"
							if (Navit.PREF_show_3d_map)
							{
								// swtich to 2d mode
								Navit.PREF_show_3d_map = false;
								Navit.set_2d3d_mode_in_settings();
								// redraw map
								NavitGraphics.view_s.postInvalidate();
							}
							else
							{
								// swtich to 3d mode
								Navit.PREF_show_3d_map = true;
								Navit.set_2d3d_mode_in_settings();
								// redraw map
								NavitGraphics.view_s.postInvalidate();
							}
						}
						else
						{
							// toggle "map on/off"

							Message msg = new Message();
							Bundle b = new Bundle();
							if (NavitGraphics.MAP_DISPLAY_OFF)
							{
								NavitGraphics.MAP_DISPLAY_OFF = false;
								b.putInt("Callback", 63);
							}
							else
							{
								NavitGraphics.MAP_DISPLAY_OFF = true;
								b.putInt("Callback", 62);
							}
							msg.setData(b);
							Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
							// redraw map
							Message msg2 = new Message();
							Bundle b2 = new Bundle();
							b2.putInt("Callback", 64);
							msg2.setData(b2);
							Navit.N_NavitGraphics.callback_handler.sendMessage(msg2);
							// redraw map
							NavitGraphics.view_s.postInvalidate();
							//NavitGraphics.NavitAOSDJava_.postInvalidate();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					// consume the event
					return true;
				}
			}
			else if (this.zoomin_button_rect.contains(x, y))
			{
				if (NavitGraphics.in_map)
				{
					if (Navit.PREF_show_3d_map)
					{
						// if in 3d map -> toggle 3d angle
						NavitGraphics.rotate_3d_map_angle++;
						NavitGraphics.init_3d_mode();
						// redraw map
						NavitGraphics.view_s.postInvalidate();
					}
					else
					{
						// if in 2d map -> zoom in
						NavitGraphics.wait_for_redraw_map = true;
						//System.out.println("invalidate 005");
						this.invalidate();
						//System.out.println("wait_for_redraw_map=true o1");
						//Log.e("NavitGraphics", "wait_for_redraw_map=true o1");
						// zoom in
						try
						{
							// NavitGraphics.CallbackMessageChannel(1, "");
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 1);
							msg.setData(b);
							Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					// consume the event
					return true;
				}
			}
			else if (this.zoomout_button_rect.contains(x, y))
			{
				if (NavitGraphics.in_map)
				{
					if (Navit.PREF_show_3d_map)
					{
						// if in 3d map -> toggle 3d angle
						NavitGraphics.rotate_3d_map_angle--;
						NavitGraphics.init_3d_mode();
						// redraw map
						NavitGraphics.view_s.postInvalidate();
					}
					else
					{
						// if in 2d map -> zoom out
						try
						{
							NavitGraphics.wait_for_redraw_map = true;
							//System.out.println("invalidate 006");
							this.invalidate();
							//System.out.println("wait_for_redraw_map=true o2");
							//Log.e("NavitGraphics", "wait_for_redraw_map=true o2");
							// zoom out

							// NavitGraphics.CallbackMessageChannel(2, "");
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 2);
							msg.setData(b);
							Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					// consume the event
					return true;
				}
			}
			else if (this.voice_recog_rect.contains(x, y))
			{
				System.out.println("VOICE REC rect touched");
				voice_rec_bar_visible = true;
				voice_rec_bar_x = x;
				voice_rec_bar_y = y;
				this.postInvalidate();
			}
			else
			{
				try
				{
					if (overlay_draw_thread1 == null)
					{
						//overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
						//overlay_draw_thread1.start();
					}
				}
				catch (Exception e)
				{
					//overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
					//overlay_draw_thread1.start();
				}
			}
		}

		// test if we touched the grey rectangle
		//
		//		if ((x < 300) && (x > 10) && (y < 200) && (y > 10))
		//		{
		//			Log.e("Navit", "NavitAndroidOverlay -> onTouchEvent -> touch Rect!!");
		//			return true;
		//		}
		//		else
		//		{
		//			return false;
		//		}

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
		this.follow_button_rect = new RectF(this.mCanvasWidth - Navit.follow_current.getWidth() - w_1, this.mCanvasHeight - Navit.follow_current.getHeight() - h_1, this.mCanvasWidth - w_1, this.mCanvasHeight - h_1);

		// rect to slide open voice recognition
		this.voice_recog_rect = new RectF(0, (this.mCanvasHeight / 2f) - 100 * draw_factor, 16 * draw_factor, (this.mCanvasHeight / 2f) + 100 * draw_factor);
		this.voice_rec_bar_limit = this.mCanvasWidth / 2;

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
		this.zoomin_button_rect = new RectF(w_2, h_2, Navit.zoomin.getWidth() + w_2, h_button_zoom + h_2);

		w_2 = (int) ((0 / 1.5f) * draw_factor) + 5;
		h_2 = (int) ((2 * 70f / 1.5f) * draw_factor) + 5 + 25 + 25;
		zoomout_ltx = w_2;
		zoomout_lty = h_2;
		this.zoomout_button_rect = new RectF(w_2, h_2, Navit.zoomout.getWidth() + w_2, h_button_zoom + h_2);

		int mapdrawing_width = (int) ((75f / 1.5f) * draw_factor);
		int mapdrawing_height = (int) ((75f / 1.5f) * draw_factor);
		w_2 = (int) ((this.mCanvasWidth - mapdrawing_width) - 5);
		h_2 = (int) ((70f / 1.5f) * draw_factor) + 5 + 25;
		mapdrawing_ltx = w_2;
		mapdrawing_lty = h_2;
		this.mapdrawing_button_rect = new RectF(w_2, h_2, mapdrawing_width + w_2, mapdrawing_height + h_2);

		// on small screens (like the blackberry Q10) the follow button overlaps the 2d/3d button (mapdrawing button)
		// check and then move 2d/3d button to the left
		boolean must_move_2d3d_button = false;
		if (mapdrawing_button_rect.contains(follow_button_rect.left, follow_button_rect.top))
		{
			must_move_2d3d_button = true;
		}
		else if (mapdrawing_button_rect.contains(follow_button_rect.right, follow_button_rect.top))
		{
			must_move_2d3d_button = true;
		}
		else if (mapdrawing_button_rect.contains(follow_button_rect.left, follow_button_rect.bottom))
		{
			must_move_2d3d_button = true;
		}
		else if (mapdrawing_button_rect.contains(follow_button_rect.left, follow_button_rect.bottom))
		{
			must_move_2d3d_button = true;
		}

		if (must_move_2d3d_button)
		{
			// move to left of follow button
			this.mapdrawing_button_rect = new RectF(w_2 - (this.mCanvasWidth - follow_button_rect.left), h_2, mapdrawing_width + w_2 - (this.mCanvasWidth - follow_button_rect.left), mapdrawing_height + h_2);
		}

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

	public void onDraw(Canvas c)
	{
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************

		// System.out.println("XYZ:NavitAndroidOverlay -> onDraw");

		//		if (2 == 1 + 1)
		//		{
		//			return;
		//		}

		//Log.e("NavitGraphics", "NavitAndroidOverlay -> onDraw");
		//System.out.println("VOICE REC:NavitAndroidOverlay -> onDraw");

		try
		{
			if (NavitAndroidOverlay.voice_rec_bar_visible)
			{
				// System.out.println("VOICE REC:NavitAndroidOverlay -> onDraw");
				// System.out.println("XYZ:NavitAndroidOverlay -> onDraw");
				c.drawBitmap(Navit.long_green_arrow, voice_rec_bar_x - Navit.long_green_arrow.getWidth() + (30 * draw_factor), (mCanvasHeight / 2), null);
			}
			else if (NavitAndroidOverlay.voice_rec_bar_visible2)
			{
				// System.out.println("VOICE REC:NavitAndroidOverlay -> onDraw");
				// System.out.println("XYZ:NavitAndroidOverlay -> onDraw");
				c.drawBitmap(Navit.long_green_arrow, voice_rec_bar_x - Navit.long_green_arrow.getWidth() + (30 * draw_factor), (mCanvasHeight / 2), null);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//Paint pp = new Paint();
		//pp.setColor(Color.RED);
		//pp.setStrokeWidth(10);
		//c.drawRect(voice_recog_rect, pp);

		if (this.draw_bubble)
		{
			if ((System.currentTimeMillis() - this.bubble_showing_since) > bubble_max_showing_timespan)
			{
				// bubble has been showing too log, hide it
				this.hide_bubble();

				// next lines are a hack, without it screen will not get updated anymore!
				// next lines are a hack, without it screen will not get updated anymore!
				// down
				//				Message msg = new Message();
				//				Bundle b = new Bundle();
				//				b.putInt("Callback", 21);
				//				b.putInt("x", 1);
				//				b.putInt("y", 1);
				//				msg.setData(b);
				//				Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
				//
				//				// move
				//				msg = new Message();
				//				b = new Bundle();
				//				b.putInt("Callback", 23);
				//				b.putInt("x", 1 + 10);
				//				b.putInt("y", 1);
				//				msg.setData(b);
				//				Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
				//
				//				// move
				//				msg = new Message();
				//				b = new Bundle();
				//				b.putInt("Callback", 23);
				//				b.putInt("x", 1 - 10);
				//				b.putInt("y", 1);
				//				msg.setData(b);
				//				Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
				//
				//				// up
				//				msg = new Message();
				//				b = new Bundle();
				//				b.putInt("Callback", 22);
				//				b.putInt("x", 1);
				//				b.putInt("y", 1);
				//				msg.setData(b);
				//				Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
				// next lines are a hack, without it screen will not get updated anymore!
				// next lines are a hack, without it screen will not get updated anymore!
			}
		}

		if (this.draw_bubble)
		{
			//Log.e("Navit", "NavitAndroidOverlay -> onDraw -> bubble");

			int dx = (int) ((20 / 1.5f) * draw_factor);
			int dy = (int) ((-100 / 1.5f) * draw_factor);
			Paint bubble_paint = new Paint(0);

			int bubble_size_x = (int) ((150 / 1.5f) * draw_factor);
			int bubble_size_y = (int) ((60 / 1.5f) * draw_factor);

			// yellow-ish funny lines
			int lx = (int) ((15 / 1.5f) * draw_factor);
			int ly = (int) ((15 / 1.5f) * draw_factor);
			bubble_paint.setStyle(Style.FILL);
			bubble_paint.setAntiAlias(true);
			bubble_paint.setStrokeWidth(8 / 1.5f * draw_factor);
			bubble_paint.setColor(Color.parseColor("#FFF8C6"));
			c.drawLine(this.bubble_001.x + dx, this.bubble_001.y + dy + bubble_size_y - ly, this.bubble_001.x, this.bubble_001.y, bubble_paint);
			c.drawLine(this.bubble_001.x + dx + lx, this.bubble_001.y + dy + bubble_size_y, this.bubble_001.x, this.bubble_001.y, bubble_paint);

			// draw black funny lines to target
			bubble_paint.setStyle(Style.STROKE);
			bubble_paint.setAntiAlias(true);
			bubble_paint.setStrokeWidth(3);
			bubble_paint.setColor(Color.parseColor("#000000"));
			c.drawLine(this.bubble_001.x + dx, this.bubble_001.y + dy + bubble_size_y - ly, this.bubble_001.x, this.bubble_001.y, bubble_paint);
			c.drawLine(this.bubble_001.x + dx + lx, this.bubble_001.y + dy + bubble_size_y, this.bubble_001.x, this.bubble_001.y, bubble_paint);

			// filled rect yellow-ish (left side) (fill it all)
			bubble_paint.setStyle(Style.FILL);
			bubble_paint.setStrokeWidth(0);
			bubble_paint.setAntiAlias(false);
			bubble_paint.setColor(Color.parseColor("#FFF8C6"));
			RectF box_rect = new RectF(this.bubble_001.x + dx, this.bubble_001.y + dy, this.bubble_001.x + bubble_size_x + dx, this.bubble_001.y + bubble_size_y + dy);
			int rx = (int) (20 / 1.5f * draw_factor);
			int ry = (int) (20 / 1.5f * draw_factor);
			c.drawRoundRect(box_rect, rx, ry, bubble_paint);

			// filled rect green-ish (right side)
			bubble_paint.setStyle(Style.FILL);
			bubble_paint.setStrokeWidth(0);
			bubble_paint.setAntiAlias(false);
			bubble_paint.setColor(Color.parseColor("#74DF00"));
			RectF box_rect_right_half = new RectF(this.bubble_001.x + dx + (bubble_size_x / 2) - rx, this.bubble_001.y + dy, this.bubble_001.x + bubble_size_x + dx, this.bubble_001.y + bubble_size_y + dy);
			c.drawRoundRect(box_rect_right_half, rx, ry, bubble_paint);

			// correct the overlap
			bubble_paint.setColor(Color.parseColor("#FFF8C6"));
			RectF box_rect_left_half_correction = new RectF(this.bubble_001.x + dx + (bubble_size_x / 2) - rx - 1, this.bubble_001.y + dy, this.bubble_001.x + dx + (bubble_size_x / 2), this.bubble_001.y + bubble_size_y + dy);
			c.drawRect(box_rect_left_half_correction, bubble_paint);

			// black outlined rect
			bubble_paint.setStyle(Style.STROKE);
			bubble_paint.setStrokeWidth(3);
			bubble_paint.setAntiAlias(true);
			bubble_paint.setColor(Color.parseColor("#000000"));
			c.drawRoundRect(box_rect, rx, ry, bubble_paint);

			// black separator lines (outer)
			bubble_paint.setStyle(Style.STROKE);
			bubble_paint.setStrokeWidth(8);
			bubble_paint.setAntiAlias(true);
			bubble_paint.setColor(Color.parseColor("#000000"));
			c.drawLine(box_rect.left + (box_rect.right - box_rect.left) / 2, box_rect.top, box_rect.left + (box_rect.right - box_rect.left) / 2, box_rect.bottom, bubble_paint);

			// black separator lines (inner)
			bubble_paint.setStyle(Style.STROKE);
			bubble_paint.setStrokeWidth(2);
			bubble_paint.setAntiAlias(true);
			bubble_paint.setColor(Color.parseColor("#D8D8D8"));
			c.drawLine(box_rect.left + (box_rect.right - box_rect.left) / 2, box_rect.top, box_rect.left + (box_rect.right - box_rect.left) / 2, box_rect.bottom, bubble_paint);

			if (NavitAndroidOverlay.confirmed_bubble)
			{
				// red outline (for confirmed bubble)
				bubble_paint.setStyle(Style.STROKE);
				bubble_paint.setStrokeWidth(5);
				bubble_paint.setAntiAlias(true);
				bubble_paint.setColor(Color.parseColor("#EC294D"));
				c.drawRoundRect(box_rect, rx, ry, bubble_paint);
			}

			int inner_dx = (int) (30 / 1.5f * draw_factor);
			int inner_dx_left = (int) (30 / 1.5f * draw_factor);
			int inner_dx_right = (int) (30 / 1.5f * draw_factor) + (bubble_size_x / 2);
			int inner_dy = (int) (36 / 1.5f * draw_factor);
			bubble_paint.setAntiAlias(true);
			bubble_paint.setStyle(Style.FILL);
			bubble_paint.setTextSize((int) (20 / 1.5f * draw_factor));
			bubble_paint.setStrokeWidth(3);
			bubble_paint.setColor(Color.parseColor("#3b3131"));
			// c.drawText(Navit.get_text("drive here"), this.bubble_001.x + dx + inner_dx, this.bubble_001.y + dy + inner_dy, bubble_paint);
			c.drawText("X", this.bubble_001.x + dx + inner_dx_left, this.bubble_001.y + dy + inner_dy, bubble_paint);
			c.drawText("?", this.bubble_001.x + dx + inner_dx_right, this.bubble_001.y + dy + inner_dy, bubble_paint);

		}

		if (NavitGraphics.in_map)
		{
			// draw follow
			if (Navit.follow_current != null)
			{
				c.drawBitmap(Navit.follow_current, this.follow_button_rect.left, this.follow_button_rect.top, null);
			}
		}

		if (NavitGraphics.in_map)
		{
			c.drawBitmap(Navit.zoomin, zoomin_ltx, zoomin_lty, null);
			c.drawBitmap(Navit.zoomout, zoomout_ltx, zoomout_lty, null);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Style.STROKE);
			paint.setColor(Color.GRAY);
			paint.setAlpha(30);
			paint.setStrokeWidth(2);
			c.drawRoundRect(this.zoomin_button_rect, 10, 10, paint);
			//paint.setStyle(Style.STROKE);
			//paint.setColor(Color.GRAY);
			//paint.setAlpha(30);
			c.drawRoundRect(this.zoomout_button_rect, 10, 10, paint);

			// draw rectanlge around "2d/3d" or "map on/off" toggle button
			c.drawRoundRect(this.mapdrawing_button_rect, 10, 10, paint);

			if (Navit.PREF_show_2d3d_toggle)
			{
				// draw "2d/3d" toggle button
				if (draw_factor == 0.7f) // ldpi
				{
					paint.setStrokeWidth(1);
					paint.setTextSize(12);
					paint.setAlpha(80);
					c.drawText("2D", this.mapdrawing_button_rect.left + 15, this.mapdrawing_button_rect.top + 15, paint);
					c.drawText("3D", this.mapdrawing_button_rect.left + 8, this.mapdrawing_button_rect.bottom - 6, paint);
				}
				else if (draw_factor == 1.0f) // mdpi
				{
					paint.setStrokeWidth(1);
					paint.setTextSize(18);
					paint.setAlpha(80);
					c.drawText("2D", this.mapdrawing_button_rect.left + 20, this.mapdrawing_button_rect.top + 20, paint);
					c.drawText("3D", this.mapdrawing_button_rect.left + 8, this.mapdrawing_button_rect.bottom - 8, paint);
				}
				else
				// draw_factor == 1.5f // hdpi
				{
					paint.setStrokeWidth(1);
					paint.setStyle(Style.FILL_AND_STROKE);
					paint.setTextSize(28 * draw_factor / 1.5f);
					paint.setAlpha(80);
					c.drawText("2D", this.mapdrawing_button_rect.left + (30 * draw_factor / 1.5f), this.mapdrawing_button_rect.top + (30 * draw_factor / 1.5f), paint);
					c.drawText("3D", this.mapdrawing_button_rect.left + 10, this.mapdrawing_button_rect.bottom - 10, paint);
				}
			}
			else
			{
				// draw "map on/off" toggle button
				if ((draw_factor == 0.7f) || (draw_factor == 1.0f))
				{
					paint.setStrokeWidth(6);
				}
				else
				{
					paint.setStrokeWidth(10);
				}
				paint.setAlpha(80);
				c.drawLine(this.mapdrawing_button_rect.left + 10, this.mapdrawing_button_rect.bottom - 10, this.mapdrawing_button_rect.right - 10, this.mapdrawing_button_rect.top + 10, paint);
			}

			//			if (!Navit.has_hw_menu_button)
			//			{
			//				// draw emulated menu button
			//				c.drawBitmap(Navit.menu_button, Navit.menu_button_rect.left, Navit.menu_button_rect.top, null);
			//				//				Paint px = new Paint();
			//				//				px.setColor(Color.RED);
			//				//				px.setStyle(Style.STROKE);
			//				//				c.drawRect(Navit.menu_button_rect_touch, px);
			//			}
		}

		//if (NavitGraphics.in_map)
		//{
		//Log.e("NavitGraphics", "NavitAndroidOverlay -> draw2");
		//if (NavitGraphics.wait_for_redraw_map)
		//{
		//	//Log.e("NavitGraphics", "NavitAndroidOverlay -> draw wait rect");
		//	Paint paint = new Paint(0);
		//	paint.setAntiAlias(true);
		//	paint.setStyle(Style.FILL);
		//	paint.setColor(Color.LTGRAY);
		//	paint.setAlpha(70);
		//
		//	RectF r1 = new RectF(20 * draw_factor, 20 * draw_factor, this.mCanvasWidth - 20 * draw_factor, this.mCanvasHeight - 20 * draw_factor);
		//	c.drawRoundRect(r1, 20, 20, paint);
		//	paint.setColor(Color.parseColor("#888888"));
		//	paint.setAlpha(230);
		//	paint.setTextAlign(Paint.Align.CENTER);
		//	paint.setStrokeWidth(2);
		//	paint.setTextSize(30);
		//	c.drawText(Navit.get_text("wait ..."), this.mCanvasWidth / 2, this.mCanvasHeight / 2, paint); //TRANS
		//}
		//}

		if (Navit.PREF_item_dump)
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

		// quick dirty lanes and destination display hack ! -----------------------------------
		// quick dirty lanes and destination display hack ! -----------------------------------
		// quick dirty lanes and destination display hack ! -----------------------------------
		if ((Navit.lanes_text == null) || (Navit.lanes_text.equals("")))
		{
			if (Navit.seg_len < 200)
			{
				// if we dont have lane information now, use the lane information of the next segment
				// but only if the current segment is not too long (in meters) !!
				Navit.lanes_num = Navit.lanes_num1;
				Navit.lanes_num_forward = Navit.lanes_num_forward1;
				Navit.lanes_text = Navit.lanes_text1;
			}
		}

		if (Navit.PREF_lane_assist)
		{
			if (Navit.lanes_text != null)
			{
				if (!Navit.lanes_text.equals(""))
				{

					Paint paint = new Paint(0);
					//				paint.setAntiAlias(true);
					//				paint.setColor(Color.BLUE);
					//				paint.setStyle(Style.FILL);
					//				c.drawRect(100, 340, 1000, 440, paint);
					//				paint.setColor(Color.WHITE);
					//				paint.setStyle(Style.FILL_AND_STROKE);
					//				paint.setStrokeWidth(2);
					//				paint.setTextSize(50);
					//				c.drawText(Navit.lanes_num + ":" + Navit.lanes_num_forward + ":" + Navit.lanes_text, 120, 400, paint);

					String lanes_split[] = Navit.lanes_text.split("\\|");
					int parsed_num_lanes = lanes_split.length;

					int lanes_choices_count = 0;
					int lanes_choices_route = -1;
					int[] lanes_choices_split_int = null;
					String[] lanes_choices_split = null;
					int highlight_kind = -1;

					int[] lanes_kind = new int[10];
					int lanes_kind_count = 0;

					// only 1 choice ---------------------
					if (Navit.lane_choices != null)
					{
						if (!Navit.lane_choices.equals(""))
						{
							lanes_choices_split = Navit.lane_choices.split("\\|");
							lanes_choices_count = lanes_choices_split.length;
							int tmp_lanes_kind_count = get_lanes_kind_count(parsed_num_lanes, lanes_split);

							if ((lanes_choices_count == 1) && (tmp_lanes_kind_count > 1))
							{
								// we only have 1 choice to drive to (means: no turns here)
								// so use info from next segment
								Navit.lane_choices = Navit.lane_choices1;
							}
						}
					}
					// only 1 choice ---------------------

					// only 1 choice (again) ---------------------
					if (Navit.lane_choices != null)
					{
						if (!Navit.lane_choices.equals(""))
						{
							lanes_choices_split = Navit.lane_choices.split("\\|");
							lanes_choices_count = lanes_choices_split.length;
							int tmp_lanes_kind_count = get_lanes_kind_count(parsed_num_lanes, lanes_split);

							if ((lanes_choices_count == 1) && (tmp_lanes_kind_count > 1))
							{
								// we only have 1 choice to drive to (means: no turns here)
								// so use info from next next segment
								Navit.lane_choices = Navit.lane_choices2;
							}
						}
					}
					// only 1 choice (again) ---------------------

					// sort and check lane choice -------------------------------------
					if (Navit.lane_choices != null)
					{
						if (!Navit.lane_choices.equals(""))
						{

							lanes_choices_split = Navit.lane_choices.split("\\|");
							lanes_choices_count = lanes_choices_split.length;
							lanes_choices_route = -1;
							lanes_choices_split_int = new int[lanes_choices_split.length];

							//System.out.println("SORTED:---orig---=" + Navit.lane_choices);

							if (lanes_choices_count > 1)
							{
								// find route lane
								int kk = 0;
								for (kk = 0; kk < lanes_choices_count; kk++)
								{
									// System.out.println("SORTED:kk=" + kk + " lcs length=" + lanes_choices_split.length);

									if (lanes_choices_split[kk].startsWith("x"))
									{
										lanes_choices_route = kk;
										//System.out.println("SORTED:route lane=" + lanes_choices_route);
										lanes_choices_split_int[kk] = Integer.parseInt(lanes_choices_split[kk].substring(1));
										//System.out.println("SORTED:res1=" + lanes_choices_split_int[kk] + " " + lanes_choices_split[kk].substring(1) + " " + lanes_choices_split[kk]);
									}
									else
									{
										lanes_choices_split_int[kk] = Integer.parseInt(lanes_choices_split[kk]);
										//System.out.println("SORTED:res2=" + lanes_choices_split_int[kk]);
									}
								}

								// sort entries (remember to also move the found "route" lane!!)
								kk = 0;
								int ll = 0;
								int temp;
								int max;
								for (kk = 1; kk < lanes_choices_count; kk++)
								{
									//System.out.println("SORTED:loop1=" + kk);

									temp = lanes_choices_split_int[kk - 1];
									max = lanes_choices_split_int[kk - 1];
									for (ll = kk; ll < lanes_choices_count; ll++)
									{
										//System.out.println("SORTED:loop2=" + ll + " temp=" + temp + " max=" + max);

										if (lanes_choices_split_int[ll] > max)
										{
											if (lanes_choices_route == ll)
											{
												// move the found "route" lane
												lanes_choices_route = kk - 1;
												//System.out.println("SORTED:move route lane1=" + ll + " -> " + (kk - 1));
											}
											else if (lanes_choices_route == kk - 1)
											{
												// move the found "route" lane
												lanes_choices_route = ll;
												//System.out.println("SORTED:move route lane2=" + (kk - 1) + " -> " + ll);
											}

											temp = lanes_choices_split_int[ll];
											lanes_choices_split_int[ll] = max;
											lanes_choices_split_int[kk - 1] = temp;
										}
									}
								}

								// sorted:
								//for (kk = 0; kk < lanes_choices_count; kk++)
								//{
								//	System.out.println("SORTED:k=" + kk + " v=" + lanes_choices_split_int[kk]);
								//}
								//System.out.println("SORTED:Route=" + lanes_choices_route);

							}
							else if (lanes_choices_count == 1)
							{
								if (lanes_choices_split[0].startsWith("x"))
								{
									lanes_choices_route = 0;
									lanes_choices_split_int[0] = Integer.parseInt(lanes_choices_split[0].substring(1));
								}
								else
								{
									lanes_choices_split_int[0] = Integer.parseInt(lanes_choices_split[0]);
								}
							}

							Paint paint2 = new Paint(0);
							//						paint2.setAntiAlias(true);
							//						paint2.setColor(Color.BLUE);
							//						paint2.setStyle(Style.FILL);
							//						c.drawRect(100, 340, 1000, 440, paint2);
							//						paint2.setColor(Color.WHITE);
							//						paint2.setStyle(Style.FILL_AND_STROKE);
							//						paint2.setStrokeWidth(2);
							//						paint2.setTextSize(50);
							//						c.drawText(Navit.lane_choices, 120, 400, paint2);

							final int num_of_kinds = 6;
							lanes_kind = new int[num_of_kinds];
							lanes_kind_count = 0;

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
									//System.out.println("SORTED:lanes_kind_count=" + lanes_kind_count + " lanes_choices_route=" + lanes_choices_route);
									if (lanes_kind_count == lanes_choices_route)
									{
										highlight_kind = k;
										//System.out.println("SORTED:highlight_kind=" + highlight_kind);
									}
									lanes_kind_count++;
									//System.out.println("SORTED:lanes_kind:" + k + "=" + lanes_kind[k]);
								}
							}
							//System.out.println("SORTED:lanes_kind_count=" + lanes_kind_count);

						}

					}
					// sort and check lane choice -------------------------------------

					// -------- START POINT ----------
					// -------- START POINT ----------
					int xx1 = 0;
					int yy1 = 750;
					int xx1_start = xx1;
					int yy1_start = yy1;
					float fx_factor = 1080;
					float fy_factor = 1701;

					// calc resize factor:
					float fx_ = fx_factor / (float) NavitGraphics.mCanvasWidth;
					float fy_ = fy_factor / (float) NavitGraphics.mCanvasHeight;
					float final_factor = 1.0f / fx_;
					if (fy_ > fx_)
					{
						final_factor = 1.0f / fy_;
					}
					float final_translator = (float) yy1_start * final_factor;
					float final_translator_x = 170.0f * ((float) NavitGraphics.mCanvasWidth / fx_factor);

					//System.out.println("SORTED:tran: " + fx_factor + " " + NavitGraphics.mCanvasWidth + " " + final_translator_x);

					lanes_transMatrix.reset();
					if (NavitGraphics.mCanvasWidth > fx_factor)
					{
						lanes_transMatrix.setTranslate((final_translator_x) - 170.0f, final_translator - (float) yy1_start);
					}
					else
					{
						lanes_transMatrix.setTranslate(0.0f, final_translator - (float) yy1_start);
					}
					//System.out.println("SORTED:tran: x=" + NavitGraphics.mCanvasWidth + " y=" + NavitGraphics.mCanvasHeight + " " + (final_translator - (float) yy1_start) + " f=" + final_factor);
					// -------- START POINT ----------
					// -------- START POINT ----------

					boolean highlight_lane = false;
					if (lanes_kind_count == lanes_choices_count)
					{
						highlight_lane = true;
						//System.out.println("SORTED:highlight_lane=" + highlight_lane);
					}

					if (parsed_num_lanes == 3)
					{
						xx1 = xx1 + (170 / 2) * 2;
					}
					else if (parsed_num_lanes == 2)
					{
						xx1 = xx1 + (170 / 2) * 3;
					}
					else if (parsed_num_lanes == 1)
					{
						xx1 = xx1 + (170 / 2) * 4;
					}

					int j = 0;
					for (j = 0; j < parsed_num_lanes; j++)
					{

						// move to next lane (move to right)
						xx1 = xx1 + 170;

						String lanes_split_sub[] = lanes_split[j].split(";");
						int parsed_num_lanes_sub = lanes_split_sub.length;

						int k = 0;
						String single_arrow = "";
						for (k = 0; k < parsed_num_lanes_sub; k++)
						{

							single_arrow = lanes_split_sub[k].replaceAll("\\s", "");

							// dirty correction hack !! ------------------
							// dirty correction hack !! ------------------
							//						if (single_arrow.equalsIgnoreCase("sharp_right"))
							//						{
							//							single_arrow = "right";
							//						}
							//						else if (single_arrow.equalsIgnoreCase("sharp_left"))
							//						{
							//							single_arrow = "left";
							//						}
							if (single_arrow.equalsIgnoreCase("merge_to_left"))
							{
								single_arrow = "mergeto_left";
							}
							else if (single_arrow.equalsIgnoreCase("merge_to_right"))
							{
								single_arrow = "mergeto_right";
							}
							// dirty correction hack !! ------------------
							// dirty correction hack !! ------------------

							// ---------===================---------------
							// ---------===================---------------
							// ---------===================---------------

							pathForTurn.reset();
							int ha = 72;
							int wa = 72;

							int th = 12 * 3; // (12) thickness
							// pathForTurn.moveTo(wa / 2, ha - 1);

							pathForTurn.moveTo(xx1, yy1);

							float sarrowL = 22 * 4; // (22) side of arrow ?
							float harrowL = (float) (Math.sqrt(2) * sarrowL); // (float) (Math.sqrt(2) * sarrowL)
							float spartArrowL = (float) ((sarrowL - th / Math.sqrt(2)) / 2); // (float) ((sarrowL - th / Math.sqrt(2)) / 2)
							float hpartArrowL = ((float) (harrowL - th) / 2); // ((float) (harrowL - th) / 2)

							// none
							// through
							// left
							// right
							// slight_left
							// slight_right
							// sharp_left
							// sharp_right
							// mergeto_left
							// mergeto_right
							// merge_to_left
							// merge_to_right

							no_draw = false;

							paint.setColor(Color.LTGRAY);

							if ((single_arrow.equalsIgnoreCase("slight_left")) || (single_arrow.equalsIgnoreCase("slight_right")))
							{
								// -----------------------------------------------------
								// turn slighty left or right
								// -----------------------------------------------------
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("slight_left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);
									if ((highlight_lane) && (highlight_kind == 2))
									{
										//System.out.println("SORTED: XXXXXXXXXXXXXXXX");
										paint.setColor(Color.GREEN);
									}

								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == 4))
									{
										paint.setColor(Color.GREEN);
									}
								}

								int h = (int) (24.0f * 3.0f); // (24)
								int quadShiftY = 22 * 4; // (22)
								float quadShiftX = ((float) (quadShiftY / (1 + Math.sqrt(2)))) * 1.5f; // (float) (quadShiftY / (1 + Math.sqrt(2)))
								float nQuadShiftX = ((sarrowL - 2 * spartArrowL) - quadShiftX - th); // (sarrowL - 2 * spartArrowL) - quadShiftX - th
								float nQuadShifty = quadShiftY + (sarrowL - 2 * spartArrowL); // quadShiftY + (sarrowL - 2 * spartArrowL)
								pathForTurn.rMoveTo(-b * 4, 0);
								pathForTurn.rLineTo(0, -h /* + partArrowL */);
								pathForTurn.rQuadTo(0, -quadShiftY + quadShiftX /*- partArrowL*/, b * quadShiftX, -quadShiftY /*- partArrowL*/);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rLineTo(0, -sarrowL); // center
								pathForTurn.rLineTo(-b * sarrowL, 0);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rQuadTo(b * nQuadShiftX, -nQuadShiftX, b * nQuadShiftX, nQuadShifty);
								pathForTurn.rLineTo(0, h);
								// -----------------------------------------------------
								// -----------------------------------------------------
							}
							else if ((single_arrow.equalsIgnoreCase("left")) || (single_arrow.equalsIgnoreCase("right")))
							{
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);

									if ((highlight_lane) && (highlight_kind == 1))
									{
										paint.setColor(Color.GREEN);
									}
								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == 5))
									{
										paint.setColor(Color.GREEN);
									}
								}

								float quadShiftX = 18;
								float quadShiftY = 18;
								int wl = 10; // width
								int h = (int) (ha - quadShiftY - harrowL + hpartArrowL - 5);

								// --
								h = -2 * h;
								// --

								int sl = wl + th / 2;

								// --
								sl = 2;
								// --

								pathForTurn.rMoveTo(-b * sl, 0);
								pathForTurn.rLineTo(0, -h);
								pathForTurn.rQuadTo(0, -quadShiftY, b * quadShiftX, -quadShiftY);
								pathForTurn.rLineTo(b * wl, 0);

								pathForTurn.rLineTo(0, hpartArrowL);
								pathForTurn.rLineTo(b * harrowL / 2, -harrowL / 2); // center
								pathForTurn.rLineTo(-b * harrowL / 2, -harrowL / 2);
								pathForTurn.rLineTo(0, hpartArrowL);

								pathForTurn.rLineTo(-b * wl, 0);
								pathForTurn.rQuadTo(-b * (quadShiftX + th), 0, -b * (quadShiftX + th), quadShiftY + th);
								pathForTurn.rLineTo(0, h);
							}
							else if ((single_arrow.equalsIgnoreCase("sharp_left")) || (single_arrow.equalsIgnoreCase("sharp_right")))
							{
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("sharp_left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);

									if ((highlight_lane) && (highlight_kind == 1))
									{
										paint.setColor(Color.GREEN);
									}
								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == 5))
									{
										paint.setColor(Color.GREEN);
									}
								}

								float quadShiftX = 18;
								float quadShiftY = 18;
								int wl = 10; // width
								int h = (int) (ha - quadShiftY - harrowL + hpartArrowL - 5);

								// --
								h = -2 * h;
								// --

								int sl = wl + th / 2;

								// --
								sl = 2;
								// --

								pathForTurn.rMoveTo(-b * sl, 0);
								pathForTurn.rLineTo(0, -h);
								pathForTurn.rQuadTo(0, -quadShiftY, b * quadShiftX, -quadShiftY);
								pathForTurn.rLineTo(b * wl, 0);

								pathForTurn.rLineTo(0, hpartArrowL);
								pathForTurn.rLineTo(b * harrowL / 2, -harrowL / 2); // center
								pathForTurn.rLineTo(-b * harrowL / 2, -harrowL / 2);
								pathForTurn.rLineTo(0, hpartArrowL);

								pathForTurn.rLineTo(-b * wl, 0);
								pathForTurn.rQuadTo(-b * (quadShiftX + th), 0, -b * (quadShiftX + th), quadShiftY + th);
								pathForTurn.rLineTo(0, h);
							}
							else if ((single_arrow.equalsIgnoreCase("mergeto_left")) || (single_arrow.equalsIgnoreCase("mergeto_right")))
							{
								int b = 1; // right = 1 or left = -1
								if (single_arrow.equalsIgnoreCase("mergeto_left"))
								{
									b = -1;
									pathForTurn.moveTo(xx1 - 4, yy1);

									if ((highlight_lane) && (highlight_kind == -99))
									{
										paint.setColor(Color.GREEN);
									}
								}
								else
								{
									pathForTurn.moveTo(xx1 + 40, yy1);

									if ((highlight_lane) && (highlight_kind == -99))
									{
										paint.setColor(Color.GREEN);
									}
								}

								int h = (int) (24.0f * 3.0f); // (24)

								// --
								h = (int) ((float) h * 0.9f);
								// --

								int quadShiftY = 22 * 4; // (22)
								float quadShiftX = ((float) (quadShiftY / (1 + Math.sqrt(2)))) * 1.2f; // (float) (quadShiftY / (1 + Math.sqrt(2)))
								float nQuadShiftX = ((sarrowL - 2 * spartArrowL) - quadShiftX - th); // (sarrowL - 2 * spartArrowL) - quadShiftX - th
								float nQuadShifty = quadShiftY + (sarrowL - 2 * spartArrowL); // quadShiftY + (sarrowL - 2 * spartArrowL)
								pathForTurn.rMoveTo(-b * 4, 0);
								pathForTurn.rLineTo(0, -h /* + partArrowL */);
								pathForTurn.rQuadTo(0, -quadShiftY + quadShiftX /*- partArrowL*/, b * quadShiftX, -quadShiftY /*- partArrowL*/);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rLineTo(0, -sarrowL); // center
								pathForTurn.rLineTo(-b * sarrowL, 0);
								pathForTurn.rLineTo(b * spartArrowL, spartArrowL);
								pathForTurn.rQuadTo(b * nQuadShiftX, -nQuadShiftX, b * nQuadShiftX, nQuadShifty);
								pathForTurn.rLineTo(0, h);
							}
							else if ((single_arrow.equalsIgnoreCase("none")) || (single_arrow.equalsIgnoreCase("through")))
							{
								int h = ((int) (ha - hpartArrowL - 16)); // (int) (ha - hpartArrowL - 16)

								if ((highlight_lane) && (highlight_kind == 3))
								{
									paint.setColor(Color.GREEN);
									//System.out.println("SORTED:highlight straight");
								}

								h = 18 * h;

								pathForTurn.rMoveTo(th, 0);
								pathForTurn.rLineTo(0, -h);
								pathForTurn.rLineTo(hpartArrowL, 0);
								pathForTurn.rLineTo(-harrowL / 2, -harrowL / 2); // center
								pathForTurn.rLineTo(-harrowL / 2, harrowL / 2);
								pathForTurn.rLineTo(hpartArrowL, 0);
								pathForTurn.rLineTo(0, h);
							}
							else
							{
								no_draw = true;
							}

							if (!no_draw)
							{
								pathForTurn.close();

								// now scale path to correct size ---------------
								// now scale path to correct size ---------------
								// now scale path to correct size ---------------
								lanes_scaleMatrix.reset();
								// pathForTurn.computeBounds(lanes_rectF, true);
								// lanes_scaleMatrix.setScale(0.25f, 0.25f, lanes_rectF.centerX(), lanes_rectF.centerY());
								lanes_scaleMatrix.setScale(final_factor, final_factor, xx1_start, yy1_start);
								pathForTurn.transform(lanes_scaleMatrix);
								pathForTurn.transform(lanes_transMatrix);
								// now scale path to correct size ---------------
								// now scale path to correct size ---------------
								// now scale path to correct size ---------------

								paint.setAntiAlias(true);
								paint.setDither(true);

								paint.setStyle(Style.FILL);
								paint.setStrokeWidth(0);

								c.drawPath(pathForTurn, paint);

								paint.setColor(Color.BLACK);
								paint.setStyle(Style.STROKE);
								paint.setStrokeWidth(4);

								c.drawPath(pathForTurn, paint);
							}
						}

					}
					// ---------===================---------------
					// ---------===================---------------
				}
			}
		}

		// max speed -------------------
		if (Navit.PREF_roadspeed_warning)
		{
			if ((Navit.cur_max_speed != -1) && (Navit.your_are_speeding))
			{
				paint_speedwarning.setAntiAlias(true);
				paint_speedwarning.setColor(Color.WHITE);
				paint_speedwarning.setStyle(Style.FILL);
				c.drawOval(bounds_speedwarning, paint_speedwarning);

				paint_speedwarning.setColor(Color.RED);
				paint_speedwarning.setStyle(Style.STROKE);
				paint_speedwarning.setStrokeWidth(30);
				c.drawOval(bounds_speedwarning, paint_speedwarning);

				paint_speedwarning.setColor(Color.BLACK);
				paint_speedwarning.setStyle(Style.FILL_AND_STROKE);
				paint_speedwarning.setStrokeWidth(2);
				if (Navit.PREF_use_imperial)
				{
					Navit.cur_max_speed_corr = (int) ((((float) Navit.cur_max_speed) / 1.6f) + 0.5f);
				}
				else
				{
					Navit.cur_max_speed_corr = Navit.cur_max_speed;
				}

				if (Navit.cur_max_speed_corr > 99)
				{
					paint_speedwarning.setTextSize(60);
				}
				else
				{
					paint_speedwarning.setTextSize(68);
				}
				paint_speedwarning.setTextAlign(Align.CENTER);
				float textHeight = paint_speedwarning.descent() - paint_speedwarning.ascent();
				float textOffset = (textHeight / 2) - paint_speedwarning.descent();
				c.drawText("" + Navit.cur_max_speed_corr, bounds_speedwarning.centerX(), bounds_speedwarning.centerY() + textOffset, paint_speedwarning);
			}
		}
		//		else // -- draw circle with grey border
		//		{
		//			RectF bounds = new RectF(120, 800, 120 + 200, 800 + 200);
		//
		//			Paint paint = new Paint(0);
		//			paint.setAntiAlias(true);
		//			paint.setColor(Color.WHITE);
		//			paint.setStyle(Style.FILL);
		//			c.drawOval(bounds, paint);
		//
		//			paint.setColor(Color.LTGRAY);
		//			paint.setStyle(Style.STROKE);
		//			paint.setStrokeWidth(30);
		//			c.drawOval(bounds, paint);
		//		}
		// max speed -------------------

		//		if (Navit.lane_destination != null)
		//		{
		//			if (!Navit.lane_destination.equals(""))
		//			{
		//				Paint paint = new Paint(0);
		//				paint.setAntiAlias(true);
		//				paint.setColor(Color.BLUE);
		//				paint.setStyle(Style.FILL);
		//				c.drawRect(100, 200, 1000, 300, paint);
		//				paint.setColor(Color.WHITE);
		//				paint.setStyle(Style.FILL_AND_STROKE);
		//				paint.setStrokeWidth(2);
		//				paint.setTextSize(50);
		//				c.drawText(Navit.lane_destination, 120, 260, paint);
		//			}
		//		}

		//		if (Navit.lane_choices != null)
		//		{
		//			if (!Navit.lane_choices.equals(""))
		//			{
		//				Paint paint = new Paint(0);
		//				paint.setAntiAlias(true);
		//				paint.setColor(Color.BLUE);
		//				paint.setStyle(Style.FILL);
		//				c.drawRect(100, 340, 1000, 440, paint);
		//				paint.setColor(Color.WHITE);
		//				paint.setStyle(Style.FILL_AND_STROKE);
		//				paint.setStrokeWidth(2);
		//				paint.setTextSize(46);
		//				c.drawText(Navit.lane_choices, 120, 380, paint);
		//			}
		//		}
		//		if (Navit.lane_choices1 != null)
		//		{
		//			if (!Navit.lane_choices1.equals(""))
		//			{
		//				Paint paint = new Paint(0);
		//				paint.setAntiAlias(true);
		//				paint.setColor(Color.BLUE);
		//				paint.setStyle(Style.FILL);
		//				// c.drawRect(100, 340, 1000, 440, paint);
		//				paint.setColor(Color.WHITE);
		//				paint.setStyle(Style.FILL_AND_STROKE);
		//				paint.setStrokeWidth(2);
		//				paint.setTextSize(46);
		//				c.drawText(Navit.lane_choices1, 120, 437, paint);
		//			}
		//		}

		// quick dirty lanes and destiantion display hack ! -----------------------------------
		// quick dirty lanes and destiantion display hack ! -----------------------------------
		// quick dirty lanes and destiantion display hack ! -----------------------------------

		//		// test, draw rectangles on top layer!
		//		Paint paint = new Paint(0);
		//		paint.setAntiAlias(false);
		//		paint.setStyle(Style.STROKE);
		//		paint.setColor(Color.GREEN);
		//		c.drawRect(0 * draw_factor, 0 * draw_factor, 64 * draw_factor, 64 * draw_factor, paint);
		//		paint.setColor(Color.RED);
		//		c.drawRect(0 * draw_factor, (0 + 70) * draw_factor, 64 * draw_factor,
		//				(64 + 70) * draw_factor, paint);
	}
}
