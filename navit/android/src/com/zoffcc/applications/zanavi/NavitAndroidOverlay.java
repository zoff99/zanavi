/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 Zoff <zoff@zoff.cc>
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
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.ImageView;

public class NavitAndroidOverlay extends ImageView
{
	public Boolean draw_bubble = false;
	public static Boolean confirmed_bubble = false;
	public static int confirmed_bubble_part = 0; // 0 -> left side, 1 -> right side
	public long bubble_showing_since = 0L;
	public static long bubble_max_showing_timespan = 8000L; // 8 secs.
	public RectF follow_button_rect = new RectF(-100, 1, 1, 1);
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
			this.bubble_showing_since = System.currentTimeMillis();
			//Log.e("Navit", "BubbleThread created");
		}

		public void stop_me()
		{
			this.running = false;
		}

		public void run()
		{
			// Log.e("Navit", "BubbleThread started");
			while (this.running)
			{
				if ((System.currentTimeMillis() - this.bubble_showing_since) > bubble_max_showing_timespan)
				{
					//Log.e("Navit", "BubbleThread: bubble displaying too long, hide it");
					// with invalidate we call the onDraw() function, that will take care of it
					this.a_overlay.postInvalidate();
					this.running = false;
				}
				else
				{
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

	public void show_bubble()
	{
		//Log.e("Navit", "NavitAndroidOverlay -> show_bubble");
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
					this.postInvalidate();
					String dest_name = "Point on Screen";

					// remeber recent dest.
					try
					{
						dest_name = NavitGraphics.CallbackGeoCalc(8, this.bubble_001.x, this.bubble_001.y);
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

					if (NavitGraphics.navit_route_status == 0)
					{
						Navit.destination_set();

						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 4);
						b.putInt("x", this.bubble_001.x);
						b.putInt("y", this.bubble_001.y);
						msg.setData(b);
						Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
					}
					else
					{
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 49);
						b.putInt("x", this.bubble_001.x);
						b.putInt("y", this.bubble_001.y);
						msg.setData(b);
						Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
					}

					//					try
					//					{
					//						Navit.follow_button_on();
					//					}
					//					catch (Exception e2)
					//					{
					//						e2.printStackTrace();
					//					}

					// consume the event
					return true;
				}
				else if (box_rect_right.contains(x, y))
				{
					// bubble touched to confirm destination
					confirmed_bubble = true;
					confirmed_bubble_part = 1;
					// draw confirmed bubble
					this.postInvalidate();

					// whats here?
					String item_dump_pretty = NavitGraphics.CallbackGeoCalc(10, this.bubble_001.x, this.bubble_001.y);
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
					this.postInvalidate();
					// consume the event
					return true;
				}
			}
			else if (this.mapdrawing_button_rect.contains(x, y))
			{
				if (NavitGraphics.in_map)
				{
					try
					{
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
					try
					{
						if (overlay_draw_thread1 == null)
						{
							overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
							overlay_draw_thread1.start();
						}
					}
					catch (Exception e)
					{
						overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
						overlay_draw_thread1.start();
					}

					//					System.out.println("ZZZZZZZZ O.11");
					//					//if (NavitGraphics.wait_for_redraw_map == true)
					//					{
					//						System.out.println("ZZZZZZZZ O.11.A");
					//						// stop drawing the map
					//						try
					//						{
					//							NavitGraphics.CallbackMessageChannel(50, "");
					//							//							Message msg = new Message();
					//							//							Bundle b = new Bundle();
					//							//							b.putInt("Callback", 50);
					//							//							msg.setData(b);
					//							//							Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
					//						}
					//						catch (Exception e)
					//						{
					//							e.printStackTrace();
					//						}
					//					}
					NavitGraphics.wait_for_redraw_map = true;
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
					// consume the event
					return true;
				}
			}
			else if (this.zoomout_button_rect.contains(x, y))
			{
				if (NavitGraphics.in_map)
				{
					try
					{
						if (overlay_draw_thread1 == null)
						{
							overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
							overlay_draw_thread1.start();
						}
					}
					catch (Exception e)
					{
						overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
						overlay_draw_thread1.start();
					}

					//					System.out.println("ZZZZZZZZ O.22");
					//					if (NavitGraphics.wait_for_redraw_map == true)
					//					{
					//						System.out.println("ZZZZZZZZ O.22.A");
					//						// stop drawing the map
					//						try
					//						{
					//							NavitGraphics.CallbackMessageChannel(50, "");
					//							//							Message msg = new Message();
					//							//							Bundle b = new Bundle();
					//							//							b.putInt("Callback", 50);
					//							//							msg.setData(b);
					//							//							Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
					//						}
					//						catch (Exception e)
					//						{
					//							e.printStackTrace();
					//						}
					//					}

					try
					{
						NavitGraphics.wait_for_redraw_map = true;
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
					// consume the event
					return true;
				}
			}
			else
			{
				try
				{
					if (overlay_draw_thread1 == null)
					{
						overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
						overlay_draw_thread1.start();
					}
				}
				catch (Exception e)
				{
					overlay_draw_thread1 = new NavitGraphics.OverlayDrawThread();
					overlay_draw_thread1.start();
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

		int w_1 = (int) ((10f / 1.5f) * draw_factor);
		int h_1 = (int) ((200f / 1.5f) * draw_factor);
		this.follow_button_rect = new RectF(this.mCanvasWidth - Navit.follow_current.getWidth() - w_1, this.mCanvasHeight - Navit.follow_current.getHeight() - h_1, this.mCanvasWidth - w_1, this.mCanvasHeight - h_1);

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

		int mapdrawing_width = 75;
		int mapdrawing_height = 75;
		w_2 = (int) (((this.mCanvasWidth - mapdrawing_width) / 1.5f) * draw_factor) - 5;
		h_2 = (int) ((70f / 1.5f) * draw_factor) + 5 + 25;
		mapdrawing_ltx = w_2;
		mapdrawing_lty = h_2;
		this.mapdrawing_button_rect = new RectF(w_2, h_2, mapdrawing_width + w_2, mapdrawing_height + h_2);
	}

	public void onDraw(Canvas c)
	{
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************
		// ************!!!!!!!!!!!!! optimze me !!!!!!!!!!!*************

		//Log.e("NavitGraphics", "NavitAndroidOverlay -> onDraw");
		//System.out.println("NavitAndroidOverlay -> onDraw");

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
			c.drawRoundRect(this.mapdrawing_button_rect, 10, 10, paint);
			paint.setStrokeWidth(10);
			paint.setAlpha(80);
			c.drawLine(this.mapdrawing_button_rect.left + 10, this.mapdrawing_button_rect.bottom - 10, this.mapdrawing_button_rect.right - 10, this.mapdrawing_button_rect.top + 10, paint);
		}

		if (NavitGraphics.in_map)
		{
			//Log.e("NavitGraphics", "NavitAndroidOverlay -> draw2");
			if (NavitGraphics.wait_for_redraw_map)
			{
				//Log.e("NavitGraphics", "NavitAndroidOverlay -> draw wait rect");
				Paint paint = new Paint(0);
				paint.setAntiAlias(true);
				paint.setStyle(Style.FILL);
				paint.setColor(Color.LTGRAY);
				paint.setAlpha(70);

				RectF r1 = new RectF(20 * draw_factor, 20 * draw_factor, this.mCanvasWidth - 20 * draw_factor, this.mCanvasHeight - 20 * draw_factor);
				c.drawRoundRect(r1, 20, 20, paint);
				paint.setColor(Color.parseColor("#888888"));
				paint.setAlpha(230);
				paint.setTextAlign(Paint.Align.CENTER);
				paint.setStrokeWidth(2);
				paint.setTextSize(30);
				c.drawText(Navit.get_text("wait ..."), this.mCanvasWidth / 2, this.mCanvasHeight / 2, paint); //TRANS
			}
		}

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
				paint.setTextSize(22);

				String[] s = Navit.debug_item_dump.split("\n");
				int i3;
				int i4 = 0;
				int max_char = 38;
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
								indent = 8;
							}
							c.drawText(s[i3].substring(j3 * max_char, ((j3 + 1) * max_char) - 0), 20 + indent, 110 + (i4 * 32), paint);
							i4++;
						}
						if ((((int) (s[i3].length() / max_char)) * max_char) < s[i3].length())
						{
							indent = 8;
							c.drawText(s[i3].substring(j3 * max_char), 20 + indent, 110 + (i4 * 32), paint);
							i4++;
						}
					}
					else
					{
						c.drawText(s[i3], 20, 110 + (i4 * 32), paint);
						i4++;
					}
				}
			}
		}

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
