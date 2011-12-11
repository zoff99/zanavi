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

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zoffcc.applications.zanavi.Navit.Navit_Address_Result_Struct;
import com.zoffcc.applications.zanavi.NavitAndroidOverlay.NavitAndroidOverlayBubble;

public class NavitGraphics
{
	private NavitGraphics parent_graphics;
	private ArrayList overlays = new ArrayList();

	public static DashPathEffect dashed_map_lines__high = new DashPathEffect(new float[] { 8, 3 }, 1);
	public static DashPathEffect dashed_map_lines__low = new DashPathEffect(new float[] { 10, 10 }, 1);
	public static DashPathEffect dashed_map_lines__no_dash = null;

	public static int navit_route_status = 0;

	// public static NavitOSDJava NavitAOSDJava_ = null;
	public static long last_paint_OSD = 0;

	public static Boolean wait_for_redraw_map = false;

	public static final Boolean DEBUG_TOUCH = false;

	public static float draw_factor = 1.0f;

	public static String debug_line_1 = "";
	public static String debug_line_2 = "";
	public static String debug_line_3 = "";

	public static final int DRAW_ONEWAY_ARROWS_AT_ORDER = 13;

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

	int bitmap_w;
	int bitmap_h;
	int pos_x;
	int pos_y;
	// drag pos
	public static int d_pos_x_old = -1;
	public static int d_pos_y_old = -1;
	// drag post
	int pos_wraparound;
	int overlay_disabled;
	float trackball_x, trackball_y;
	View view;
	RelativeLayout relativelayout;
	NavitCamera camera;
	Activity activity;

	private Bitmap bigmap_bitmap_temp = null;

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
	private TextView NavitMsgTv = null;
	public static TextView NavitMsgTv_ = null;

	public static NavitGlobalMap NavitGlobalMap_ = null;

	public void SetCamera(int use_camera)
	{
		if (use_camera != 0 && camera == null)
		{
			// activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
			camera = new NavitCamera(activity);
			relativelayout.addView(camera);
			relativelayout.bringChildToFront(view);
		}
	}

	public static SensorThread touch_sensor_thread = null;

	private class SensorThread extends Thread
	{
		private Boolean running;
		private long last_down_action = 0L;
		private Boolean is_still_pressing;
		private View v = null;
		private NavitAndroidOverlay n_overlay = null;
		private float prev_x;
		private float prev_y;
		private float x;
		private float y;

		SensorThread(long last_touch, View v, NavitAndroidOverlay n_ov, float x, float y)
		{
			this.prev_x = x;
			this.prev_y = y;
			this.x = x;
			this.y = y;
			this.running = true;
			this.n_overlay = n_ov;
			this.v = v;
			this.is_still_pressing = true;
			last_down_action = System.currentTimeMillis();
			if (DEBUG_TOUCH) Log.e("NavitGraphics", "SensorThread created");
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
				if ((System.currentTimeMillis() - this.last_down_action) > long_press_on_screen_interval)
				{
					// ok, we have counted a long press on screen
					// do stuff and then stop this thread
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "SensorThread: LONG PRESS");
					try
					{
						// find the class, to get the method "do_longpress_action"
						// and then call the method
						Class cls = this.v.getClass();
						//Log.e("NavitGraphics", "c=" + String.valueOf(cls));
						Class partypes[] = new Class[2];
						partypes[0] = Float.TYPE;
						partypes[1] = Float.TYPE;
						Method meth = cls.getMethod("do_longpress_action", partypes);
						View methobj = this.v;
						Object arglist[] = new Object[2];
						arglist[0] = new Float(this.x);
						arglist[1] = new Float(this.y);
						if (running)
						{
							Object retobj = meth.invoke(methobj, arglist);
						}
					}
					catch (Throwable e)
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

	public static OverlayDrawThread overlay_draw_thread = null;

	public static class OverlayDrawThread extends Thread
	{
		private Boolean running = true;
		private Boolean redraw = false;
		private long start_timestamp = 0L;

		OverlayDrawThread()
		{
			//Log.e("NavitGraphics", "OverlayDrawThread created");
			start_timestamp = System.currentTimeMillis();
		}

		public void run()
		{
			this.running = true;
			this.redraw = true;

			//Log.e("NavitGraphics", "OverlayDrawThread starting");

			while (this.running)
			{
				if (System.currentTimeMillis() < start_timestamp + 5000L)
				{
					// just to be safe, stop after 5 seconds
					this.running = false;
				}

				//Log.e("NavitGraphics", "OverlayDrawThread running");
				if (this.redraw)
				{
					//Log.e("NavitGraphics", "OverlayDrawThread -> redraw");
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
					Thread.sleep(200);
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

	public NavitGraphics(Activity activity, NavitGraphics parent, int x, int y, int w, int h, int alpha, int wraparound, int use_camera)
	{
		if (parent == null)
		{
			this.activity = activity;
			view = new View(activity)
			{
				int touch_mode = NONE;
				float oldDist = 0;
				PointF touch_now = new PointF(0, 0);
				PointF touch_start = new PointF(0, 0);
				PointF touch_prev = new PointF(0, 0);
				static final int NONE = 0;
				static final int DRAG = 1;
				static final int ZOOM = 2;
				static final int PRESS = 3;

				@Override
				protected void onDraw(Canvas canvas)
				{
					super.onDraw(canvas);
					canvas.drawBitmap(draw_bitmap, pos_x, pos_y, null);
					if (overlay_disabled == 0)
					{
						//Log.e("NavitGraphics", "view -> onDraw 1");
						// assume we ARE in map view mode!
						in_map = true;

						Object overlays_array[];
						overlays_array = overlays.toArray();
						//int ov_count = 0;
						for (Object overlay : overlays_array)
						{
							//ov_count++;
							//Log.e("NavitGraphics", "view -> onDraw 2:" + ov_count);

							NavitGraphics overlay_graphics = (NavitGraphics) overlay;
							if (overlay_graphics.overlay_disabled == 0)
							{
								//Log.e("NavitGraphics", "view -> onDraw 3:" + ov_count);

								int x = overlay_graphics.pos_x;
								int y = overlay_graphics.pos_y;
								if (overlay_graphics.pos_wraparound != 0 && x < 0) x += bitmap_w;
								if (overlay_graphics.pos_wraparound != 0 && y < 0) y += bitmap_h;
								canvas.drawBitmap(overlay_graphics.draw_bitmap, x, y, null);
							}
						}
					}
				}

				@Override
				protected void onSizeChanged(int w, int h, int oldw, int oldh)
				{
					Log.e("Navit", "NavitGraphics -> onSizeChanged pixels x=" + w + " pixels y=" + h);
					Log.e("Navit", "NavitGraphics -> onSizeChanged dpi=" + Navit.metrics.densityDpi);
					Log.e("Navit", "NavitGraphics -> onSizeChanged density=" + Navit.metrics.density);
					Log.e("Navit", "NavitGraphics -> onSizeChanged scaledDensity=" + Navit.metrics.scaledDensity);
					super.onSizeChanged(w, h, oldw, oldh);
					if (draw_bitmap != null)
					{
						// try to avoid out of memory errors
						draw_bitmap.recycle();
					}

					try
					{
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

					draw_canvas = new Canvas(draw_bitmap);
					bitmap_w = w;
					bitmap_h = h;
					SizeChangedCallback(SizeChangedCallbackID, w, h);

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
						System.out.println("");
						System.out.println("*** Zoom out FULL (#startup#) ***");
						System.out.println("");
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 8);
						msg.setData(b);
						callback_handler.sendMessage(msg);
					}
				}

				public void do_longpress_action(float x, float y)
				{
					if (!NavitAndroidOverlay.confirmed_bubble)
					{
						Log.e("NavitGraphics", "do_longpress_action enter");
						NavitAndroidOverlayBubble b = new NavitAndroidOverlayBubble();
						b.x = (int) x;
						b.y = (int) y;
						NavitAOverlay.set_bubble(b);
						NavitAOverlay.show_bubble();
						this.postInvalidate();
						NavitAOverlay.invalidate();
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

					// calculate value
					int switch_value = (event.getAction() & _ACTION_MASK_);
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "switch_value=" + switch_value);
					if (DEBUG_TOUCH) Log.e("NavitGraphics", "_ACTION_MASK_=" + _ACTION_MASK_);
					// calculate value

					if (DEBUG_TOUCH) Log.e("NavitGraphics", "event x=" + event.getX() + " y=" + event.getY());

					if (switch_value == MotionEvent.ACTION_DOWN)
					{
						this.touch_now.set(event.getX(), event.getY());
						this.touch_start.set(event.getX(), event.getY());
						this.touch_prev.set(event.getX(), event.getY());
						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_DOWN start");

						d_pos_x_old = pos_x;
						d_pos_y_old = pos_y;

						if (in_map)
						{
							if (!NavitAOverlay.get_show_bubble())
							{
								if (DEBUG_TOUCH) Log.e("NavitGraphics", "hide bubble X1");
								NavitAOverlay.hide_bubble();
								// remember last press on screen time
								last_touch_on_screen = event.getEventTime(); // System.currentTimeMillis();
								touch_sensor_thread = new SensorThread(last_touch_on_screen, this, NavitAOverlay, this.touch_now.x, this.touch_now.y);
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
					}
					else if ((switch_value == MotionEvent.ACTION_UP) || (switch_value == _ACTION_POINTER_UP_))
					{
						this.touch_now.set(event.getX(), event.getY());
						touch_now2 = touch_now;
						touch_start2 = touch_start;
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
								NavitAndroidOverlay.overlay_draw_thread1.stop_redraw();
								NavitAndroidOverlay.overlay_draw_thread1.stop_me();
								NavitAndroidOverlay.overlay_draw_thread1 = null;
							}
							catch (Exception e)
							{
								//e.printStackTrace();
							}
							try
							{
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
								pos_x = d_pos_x_old;
								pos_y = d_pos_y_old;
								ButtonCallback(ButtonCallbackID, 1, 1, (int) this.touch_start.x, (int) this.touch_start.y); // down
								//this.invalidate();
								//this.postInvalidate();
								// DRAG ----------- new END --------------
								// DRAG ----------- new END --------------
								// DRAG ----------- new END --------------

								MotionCallback(MotionCallbackID, x, y);
								ButtonCallback(ButtonCallbackID, 0, 1, x, y); // up

								touch_mode = NONE;
							}
							else
							{
								if (touch_mode == ZOOM)
								{
									// end of "pinch zoom" move

									if (DEBUG_TOUCH) Log.e("NavitGraphics", "onTouch zoom");

									float newDist = spacing(event);
									float scale = 0;
									if (newDist > 10f)
									{
										scale = newDist / oldDist;
									}

									if (scale > 1.3)
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
										// CallbackMessageChannel(1, "");
										Message msg = new Message();
										Bundle b = new Bundle();
										b.putInt("Callback", 1);
										msg.setData(b);
										callback_handler.sendMessage(msg);

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
									else if (scale < 0.8)
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
										// CallbackMessageChannel(2, "");
										Message msg = new Message();
										Bundle b = new Bundle();
										b.putInt("Callback", 2);
										msg.setData(b);
										callback_handler.sendMessage(msg);

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
										// move was not zoom-out OR zoom-in
										// so just ignore and remove any pending stuff
										if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=false 10");
										NavitGraphics.wait_for_redraw_map = false;
										try
										{
											if (DEBUG_TOUCH) Log.e("NavitGraphics", "overlay thread stop X10");
											NavitAndroidOverlay.overlay_draw_thread1.stop_redraw();
											NavitAndroidOverlay.overlay_draw_thread1.stop_me();
											NavitAndroidOverlay.overlay_draw_thread1 = null;
										}
										catch (Exception e)
										{
											//e.printStackTrace();
										}
										try
										{
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
					}
					else if (switch_value == MotionEvent.ACTION_MOVE)
					{
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

								if (DEBUG_TOUCH) Log.e("NavitGraphics", "in move: still a RESS action");

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
								this.invalidate();
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
						}
					}
					else if (switch_value == _ACTION_POINTER_DOWN_)
					{
						if (DEBUG_TOUCH) Log.e("NavitGraphics", "ACTION_POINTER_DOWN");

						oldDist = spacing(event);
						if (oldDist > 10f)
						{
							touch_mode = ZOOM;
							if (DEBUG_TOUCH) Log.e("NavitGraphics", "--> zoom");
						}
					}
					return true;
				}

				private float spacing(PointF a, PointF b)
				{
					float x = a.x - b.x;
					float y = a.y - b.y;
					return FloatMath.sqrt(x * x + y * y);
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
					if (keyCode == KeyEvent.KEYCODE_BACK)
					{
						// override back button, so it does NOT quit the application
						return true;
					}
					return super.onKeyDown(keyCode, event);
				}

				@Override
				protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect)
				{
					super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
					Log.e("NavitGraphics", "FocusChange " + gainFocus);
				}
			};

			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.setKeepScreenOn(true);
			relativelayout = new RelativeLayout(activity);
			//if (use_camera != 0)
			//{
			//	SetCamera(use_camera);
			//}
			relativelayout.addView(view);

			// android overlay
			Log.e("Navit", "create android overlay");
			NavitAOverlay = new NavitAndroidOverlay(relativelayout.getContext());
			NavitAOverlay_s = NavitAOverlay;
			RelativeLayout.LayoutParams NavitAOverlay_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			relativelayout.addView(NavitAOverlay, NavitAOverlay_lp);
			NavitAOverlay.bringToFront();
			NavitAOverlay.invalidate();
			// android overlay

			// android OSDJava
			Log.e("Navit", "create android OSDJava");
			NavitAOSDJava = new NavitOSDJava(relativelayout.getContext());
			// NavitAOSDJava_ = NavitAOSDJava;
			RelativeLayout.LayoutParams NavitAOSDJava_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			relativelayout.addView(NavitAOSDJava, NavitAOSDJava_lp);
			NavitAOSDJava.bringToFront();
			NavitAOSDJava.invalidate();
			// android OSDJava

			// android Messages TextView
			Log.e("Navit", "create android Messages TextView");
			NavitMsgTv = new TextView(relativelayout.getContext());
			NavitMsgTv_ = NavitMsgTv;
			RelativeLayout.LayoutParams NavitMsgTv_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			// NavitMsgTv_lp.height = 50;
			NavitMsgTv_lp.leftMargin = 120;
			// NavitMsgTv.setHeight(10);
			int tc = Color.argb(125, 0, 0, 0); // half transparent black
			NavitMsgTv.setBackgroundColor(tc);
			NavitMsgTv.setLines(3);
			NavitMsgTv.setTextSize(12);
			NavitMsgTv.setTextColor(Color.argb(255, 200, 200, 200)); // almost white
			relativelayout.addView(NavitMsgTv, NavitMsgTv_lp);
			NavitMsgTv.bringToFront();
			NavitMsgTv.invalidate();
			// android Messages TextView

			// big map overlay
			//			NavitGlobalMap_ = new NavitGlobalMap(relativelayout.getContext());
			//			RelativeLayout.LayoutParams NavitGlobalMap_lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
			//			relativelayout.addView(NavitGlobalMap_, NavitGlobalMap_lp);
			//			NavitGlobalMap_.bringToFront();
			//			NavitGlobalMap_.invalidate();
			// big map overlay

			activity.setContentView(relativelayout);
			view.requestFocus();
		}
		else
		{
			if (draw_bitmap != null)
			{
				// try to avoid out of memory errors
				draw_bitmap.recycle();
			}

			try
			{
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
			parent.overlays.add(this);
		}
		parent_graphics = parent;
	}

	public Handler callback_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.getData().getInt("Callback") == 1)
			{
				CallbackMessageChannel(1, "");
			}
			else if (msg.getData().getInt("Callback") == 2)

			{
				CallbackMessageChannel(2, "");
			}
			else if (msg.getData().getInt("Callback") == 3)

			{
				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// save dest.
				//				try
				//				{
				//					Navit.remember_destination(q, lat, lon);
				//				}
				//				catch (Exception e)
				//				{
				//					e.printStackTrace();
				//				}
				//Navit.destination_set();
				// set routing target to lat,lon
				CallbackMessageChannel(3, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 4)

			{
				// set routing target to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");

				//				try
				//				{
				//					Navit.remember_destination_xy("Point on Screen", x, y);
				//				}
				//				catch (Exception e)
				//				{
				//					e.printStackTrace();
				//				}
				//Navit.destination_set();

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
				ButtonCallback(ButtonCallbackID, 1, 1, x, y); // down
			}
			else if (msg.getData().getInt("Callback") == 22)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				ButtonCallback(ButtonCallbackID, 0, 1, x, y); // up
			}
			else if (msg.getData().getInt("Callback") == 23)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				MotionCallback(MotionCallbackID, x, y);
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
		}
	};

	public native void SizeChangedCallback(int id, int x, int y);

	public native void ButtonCallback(int id, int pressed, int button, int x, int y);

	public native void MotionCallback(int id, int x, int y);

	public native void KeypressCallback(int id, String s);

	private Canvas draw_canvas;
	private Bitmap draw_bitmap;
	private int SizeChangedCallbackID, ButtonCallbackID, MotionCallbackID, KeypressCallbackID;

	// private int count;
	public void setSizeChangedCallback(int id)
	{
		SizeChangedCallbackID = id;
	}

	public void setButtonCallback(int id)
	{
		ButtonCallbackID = id;
	}

	public void setMotionCallback(int id)
	{
		MotionCallbackID = id;
		Navit.setMotionCallback(id, this);
	}

	public void setKeypressCallback(int id)
	{
		KeypressCallbackID = id;
		// set callback id also in main intent (for menus)
		Navit.setKeypressCallback(id, this);
	}

	protected void draw_polyline(Paint paint, int c[])
	{
		//	Log.e("NavitGraphics","draw_polyline");
		paint.setStyle(Paint.Style.STROKE);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		//paint.setStrokeWidth(0);
		Path path = new Path();
		path.moveTo(c[0], c[1]);
		for (int i = 2; i < c.length; i += 2)
		{
			path.lineTo(c[i], c[i + 1]);
		}
		//global_path.close();
		draw_canvas.drawPath(path, paint);
		paint.setAntiAlias(b);
	}

	protected void draw_polyline2(Paint paint, int c[], int order, int oneway)
	{
		//	Log.e("NavitGraphics","draw_polyline2");
		paint.setStyle(Paint.Style.STROKE);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);

		Boolean normal = false;

		// create matrix for the manipulation
		Matrix matrix = new Matrix();

		int middle_x = c[0] + (int) ((c[2] - c[0]) / 2);
		int middle_y = c[1] + (int) ((c[3] - c[1]) / 2);
		double d_x = ((c[2] - c[0]) / 6);
		double d_y = ((c[3] - c[1]) / 6);
		int angle = (int) (Math.toDegrees(Math.atan2(d_y, d_x)));
		// System.out.println("arrow angle=" + angle);
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
			Path path = new Path();
			path.moveTo(c[0], c[1]);
			for (int i = 2; i < c.length; i += 2)
			{
				path.lineTo(c[i], c[i + 1]);
			}
			draw_canvas.drawPath(path, paint);
		}
		paint.setAntiAlias(b);
	}

	protected void draw_polyline_dashed(Paint paint, int c[], int order, int oneway)
	{
		paint.setStyle(Paint.Style.STROKE);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);

		Boolean normal = false;

		// create matrix for the manipulation
		Matrix matrix = new Matrix();

		int middle_x = c[0] + (int) ((c[2] - c[0]) / 2);
		int middle_y = c[1] + (int) ((c[3] - c[1]) / 2);
		double d_x = ((c[2] - c[0]) / 6);
		double d_y = ((c[3] - c[1]) / 6);
		int angle = (int) (Math.toDegrees(Math.atan2(d_y, d_x)));
		// System.out.println("arrow angle=" + angle);
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
			if (order > 12)
			{
				paint.setPathEffect(dashed_map_lines__low);
			}
			else
			{
				paint.setPathEffect(dashed_map_lines__high);
			}
			Path path = new Path();
			path.moveTo(c[0], c[1]);
			for (int i = 2; i < c.length; i += 2)
			{
				path.lineTo(c[i], c[i + 1]);
			}

			draw_canvas.drawPath(path, paint);
			paint.setPathEffect(dashed_map_lines__no_dash);
		}
		paint.setAntiAlias(b);
	}

	protected void draw_polygon(Paint paint, int c[])
	{
		paint.setStyle(Paint.Style.FILL);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		Path path = new Path();
		path.moveTo(c[0], c[1]);
		for (int i = 2; i < c.length; i += 2)
		{
			path.lineTo(c[i], c[i + 1]);
		}
		draw_canvas.drawPath(path, paint);
		paint.setAntiAlias(b);
	}

	protected void draw_polygon2(Paint paint, int c[], int order, int oneway)
	{
		paint.setStyle(Paint.Style.FILL);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		Path path = new Path();
		path.moveTo(c[0], c[1]);
		for (int i = 2; i < c.length; i += 2)
		{
			path.lineTo(c[i], c[i + 1]);
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

		draw_canvas.drawPath(path, paint);
		paint.setAntiAlias(b);
	}

	protected void draw_rectangle(Paint paint, int x, int y, int w, int h)
	{
		//Log.e("NavitGraphics","draw_rectangle");
		Rect r = new Rect(x, y, x + w, y + h);
		paint.setStyle(Paint.Style.FILL);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		//paint.setStrokeWidth(0);
		draw_canvas.drawRect(r, paint);
		paint.setAntiAlias(b);
	}

	protected void draw_circle(Paint paint, int x, int y, int r)
	{
		//Log.e("NavitGraphics","draw_circle");
		//		float fx = x;
		//		float fy = y;
		//		float fr = r / 2;
		paint.setStyle(Paint.Style.STROKE);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		draw_canvas.drawCircle(x, y, r / 2, paint);
		paint.setAntiAlias(b);
	}

	protected void draw_text(Paint paint, int x, int y, String text, int size, int dx, int dy)
	{
		//		float fx = x;
		//		float fy = y;
		//Log.e("NavitGraphics","Text size "+size + " vs " + paint.getTextSize());
		paint.setTextSize(size / 15);
		paint.setStyle(Paint.Style.FILL);
		Boolean b = paint.isAntiAlias();
		paint.setAntiAlias(Navit.PREF_use_anti_aliasing);
		if (dx == 0x10000 && dy == 0)
		{
			draw_canvas.drawText(text, x, y, paint);
		}
		else
		{
			Path path = new Path();
			path.moveTo(x, y);
			path.rLineTo(dx, dy);
			paint.setTextAlign(android.graphics.Paint.Align.LEFT);
			draw_canvas.drawTextOnPath(text, path, 0, 0, paint);
		}
		paint.setAntiAlias(b);
	}

	protected void draw_image(Paint paint, int x, int y, Bitmap bitmap)
	{
		//Log.e("NavitGraphics","draw_image");
		//		float fx = x;
		//		float fy = y;
		draw_canvas.drawBitmap(bitmap, x, y, paint);
	}

	protected void draw_bigmap(int yaw, int order, float clng, float clat, int x_, int y_, int mcx, int mcy, int px_, int py_, int valid)
	{
		if (Navit.bigmap_bitmap != null)
		{
			// input: x,y --> screen coords of lat=0,lng=0 point!!

			//System.out.println("in:" + clat + "#" + clng);
			//String aaaa1 = CallbackGeoCalc(1, 1, 1);
			//System.out.println("out:" + aaaa1);
			//String aaaa2 = CallbackGeoCalc(2, clat, clng);
			//System.out.println("out:" + aaaa2);

			//int yaw2 = -yaw;
			//int x = (int) ((float) x_ * Math.cos(Math.toRadians(yaw2)) - (float) y_ * Math.sin(Math.toRadians(yaw2)));
			//int y = (int) ((float) x_ * Math.sin(Math.toRadians(yaw2)) + (float) y_ * Math.cos(Math.toRadians(yaw2)));

			//**int x = x_;
			//**int y = y_;

			//int scx = (int) ((float) scx_ * Math.cos(Math.toRadians(yaw2)) - (float) scy_ * Math.sin(Math.toRadians(yaw2)));
			//int scy = (int) ((float) scx_ * Math.sin(Math.toRadians(yaw2)) + (float) scy_ * Math.cos(Math.toRadians(yaw2)));
			int scx = (int) (draw_canvas.getWidth() / 2);
			int scy = (int) (draw_canvas.getHeight() / 2);

			//int px = (int) ((float) px_ * Math.cos(Math.toRadians(yaw2)) - (float) py_ * Math.sin(Math.toRadians(yaw2)));
			//int py = (int) ((float) px_ * Math.sin(Math.toRadians(yaw2)) + (float) py_ * Math.cos(Math.toRadians(yaw2)));
			int px = px_;
			int py = py_;

			// c.scale(5f, 5f, this.mCanvasWidth / 2, this.mCanvasHeight / 2);
			// c.rotate(13, this.mCanvasWidth / 2, this.mCanvasHeight / 2);

			//yaw = 30;
			//System.out.println("yaw=" + yaw);
			//System.out.println("x_=" + x_);
			//System.out.println("y_=" + y_);

			// calculate the scale
			float scaleWidth = 1;
			float scaleHeight = 1;
			if (order == -1)
			{
				scaleWidth = scaleHeight = 0.8f * (1.5f / draw_factor);
			}
			else if (order == -2)
			{
				scaleWidth = scaleHeight = (float) ((1 / 2f) * 0.8f) * (1.5f / draw_factor);
			}

			if ((scaleWidth == 0) || (scaleHeight == 0))
			{
				//System.out.println(" " + scaleWidth + " " + scaleHeight + " " + order);
				return;
			}

			if (valid == 1)
			{
				//System.out.println(" px " + px + " py " + py);
			}

			// createa matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bit map
			//matrix.postScale(scaleWidth, scaleHeight);
			// rotate the Bitmap
			//matrix.setRotate(13, Navit.bigmap_bitmap.getWidth() / 2, Navit.bigmap_bitmap.getHeight() / 2);
			//int half_x = (int) ((Navit.bigmap_bitmap.getWidth() / 1.5f * draw_factor) / 2);
			//int half_y = (int) ((Navit.bigmap_bitmap.getHeight() / 1.5f * draw_factor) / 2);
			int half_x = (int) (Navit.bigmap_bitmap.getWidth() / 2);
			int half_y = (int) (Navit.bigmap_bitmap.getHeight() / 2);
			matrix.setScale(scaleWidth, scaleHeight, half_x, half_y);

			// matrix.postRotate(yaw);

			int usedMegs;
			String usedMegsString;
			//			int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			//			String usedMegsString = String.format("00 - Memory Used: %d MB", usedMegs);
			//			System.out.println("" + usedMegsString);

			// recreate the new Bitmap
			try
			{
				bigmap_bitmap_temp = Bitmap.createBitmap(Navit.bigmap_bitmap, 0, 0, Navit.bigmap_bitmap.getWidth(), Navit.bigmap_bitmap.getHeight(), matrix, true);
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
					bigmap_bitmap_temp = Bitmap.createBitmap(Navit.bigmap_bitmap, 0, 0, Navit.bigmap_bitmap.getWidth(), Navit.bigmap_bitmap.getHeight(), matrix, true);
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
			float bottom = -85.2f;
			float left = -180f;
			float right = 180f;

			String left_top_on_screen_string = CallbackGeoCalc(2, top, left);
			//String right_top_on_screen_string = CallbackGeoCalc(2, top, right);
			String tmp[] = left_top_on_screen_string.split(":", 2);
			int pixel_top_left_x = Integer.parseInt(tmp[0]);
			int pixel_top_left_y = Integer.parseInt(tmp[1]);
			//int pixel_top_right_x = Integer.parseInt(right_top_on_screen_string.split(":", 2)[0]);
			//int pixel_top_right_y = Integer.parseInt(right_top_on_screen_string.split(":", 2)[1]);
			//double xx = pixel_top_left_x - pixel_top_right_x;
			//double yy = pixel_top_left_y - pixel_top_right_y;
			//double alpha = 0;
			//if (yy != 0)
			//{
			//	alpha = Math.atan(xx / yy);
			//	alpha = Math.toDegrees(alpha);
			//}
			//System.out.println("alpha=" + alpha);

			//			System.out.println("lt:" + left_top_on_screen_string + " rt:" + right_top_on_screen_string);
			//			left_top_on_screen_string = CallbackGeoCalc(2, bottom, left);
			//			right_top_on_screen_string = CallbackGeoCalc(2, bottom, right);
			//			System.out.println("lb:" + left_top_on_screen_string + " rb:" + right_top_on_screen_string);

			/*
			 * right_top_on_screen_string = CallbackGeoCalc(2, 0.0f, 0.0f);
			 * System.out.println("1lb:" + left_top_on_screen_string + " rb:" + right_top_on_screen_string);
			 * 
			 * right_top_on_screen_string = CallbackGeoCalc(2, 80.0f, 180.0f);
			 * System.out.println("2lb:" + left_top_on_screen_string + " rb:" + right_top_on_screen_string);
			 * 
			 * right_top_on_screen_string = CallbackGeoCalc(2, 0.0f, 0.0f);
			 * System.out.println("3lb:" + left_top_on_screen_string + " rb:" + right_top_on_screen_string);
			 * 
			 * right_top_on_screen_string = CallbackGeoCalc(2, -80.0f, -180.0f);
			 * System.out.println("4lb:" + left_top_on_screen_string + " rb:" + right_top_on_screen_string);
			 */

			float p_karte_pixel_x;
			float p_karte_pixel_y;
			//p_karte_pixel_x = (clat - left) * ((float) (bigmap_bitmap_temp.getWidth()) / (float) (right - left));
			//p_karte_pixel_y = (clng - top) * ((float) (bigmap_bitmap_temp.getHeight()) / (float) (bottom - top));

			// pixel point of 0/0 geo coord
			p_karte_pixel_x = bigmap_bitmap_temp.getWidth() / 2;
			p_karte_pixel_y = bigmap_bitmap_temp.getHeight() / 2;

			if (!loc_12_valid)
			{
				// initialize them the first time
				loc_1_x = x_;
				loc_1_y = y_;
				loc_2_x = mcx;
				loc_2_y = mcy;
				loc_12_valid = true;
			}

			if ((loc_1_x == x_) && (loc_1_y == y_))
			{
				p_karte_pixel_x = p_karte_pixel_x - (loc_2_x - mcx);
				p_karte_pixel_y = p_karte_pixel_y - (loc_2_y - mcy);
				// System.out.println("SSSSSSSS SSSSSSSS");
			}
			else
			{
				loc_2_x = mcx;
				loc_2_y = mcy;
			}

			loc_1_x = x_;
			loc_1_y = y_;

			int ssschx = (int) (x_ - p_karte_pixel_x);
			int ssschy = (int) (y_ - p_karte_pixel_y);

			//System.out.println(" kpx " + p_karte_pixel_x + " kpy " + p_karte_pixel_y);
			//System.out.println(" sw " + scaleWidth + " sh " + scaleHeight);
			//System.out.println(" x " + x + " y " + y + " sx " + scx + " sy " + scy + " w " + draw_canvas.getWidth() + " h " + draw_canvas.getHeight());

			// fill canvas with ocean color
			draw_canvas.drawColor(Color.parseColor("#82C8EA"));

			//               x   ,   y
			// 556,355 -> 1822610,36020399
			//
			// y ++ -> oben
			// y -- -> unten
			// x ++ -> rechts
			// x -- -> links

			int green_dot_x = scx;
			int green_dot_y = scy;
			loc_dot_valid = true;

			int rotation_point_x = scx;
			int rotation_point_y = scy;

			if (valid == 1)
			{
				/*
				 * if (order == -2)
				 * {
				 * rotation_point_x = (int) ((-x + 1822610f) / 100000f * 1.5f) - 150 + 50 + px + 70;
				 * rotation_point_y = (int) ((y - 36020399f) / 100000f * 1.5f) + 350 + 270 + py - 312;
				 * }
				 * else if (order == -1)
				 * {
				 * rotation_point_x = (int) ((-x + 1822610f) / 100000f * 3f) - 550 + 120 + px + 373;
				 * rotation_point_y = (int) ((y - 36020399f) / 100000f * 3f) + 260 + 500 + 90 + py - 8;
				 * }
				 */

				green_dot_x = px;
				green_dot_y = py;

				rotation_point_x = px;
				rotation_point_y = py;
				//System.out.println("valid");
			}
			//System.out.println("rotation_point_x=" + rotation_point_x);
			//System.out.println("rotation_point_y=" + rotation_point_y);

			/*
			 * int new_x = 0;
			 * int new_y = 0;
			 * if (order == -2)
			 * {
			 * new_x = (int) ((-x + 1822610f) / 100000f * 1.5f) - 150 + 50;
			 * new_y = (int) ((y - 36020399f) / 100000f * 1.5f) + 350 + 270;
			 * }
			 * else if (order == -1)
			 * {
			 * new_x = (int) ((-x + 1822610f) / 100000f * 3f) - 550 + 120;
			 * new_y = (int) ((y - 36020399f) / 100000f * 3f) + 260 + 500 + 90;
			 * }
			 */
			//System.out.println("nx=" + new_x);
			//System.out.println("ny=" + new_y);

			//if ((new_x != loc_dot_x) && (new_y != loc_dot_y))
			//{
			/*
			 * if ((loc_dot_x2 == rotation_point_x) && (loc_dot_y2 == rotation_point_y))
			 * {
			 * green_dot_x = rotation_point_x - (loc_dot_x - new_x);
			 * green_dot_y = rotation_point_y - (loc_dot_y - new_y);
			 * // System.out.println("============");
			 * // System.out.println("1 n " + new_x);
			 * // System.out.println("2 n " + new_y);
			 * // System.out.println("3 r " + rotation_point_x);
			 * // System.out.println("4 r " + rotation_point_y);
			 * // System.out.println("5 g " + green_dot_x);
			 * // System.out.println("6 g " + green_dot_y);
			 * // System.out.println("7 l " + loc_dot_x);
			 * // System.out.println("8 l " + loc_dot_y);
			 * }
			 * //}
			 * if ((loc_dot_x2 != rotation_point_x) && (loc_dot_y2 != rotation_point_y))
			 * {
			 * loc_dot_x = new_x;
			 * loc_dot_y = new_y;
			 * }
			 * loc_dot_x2 = rotation_point_x;
			 * loc_dot_y2 = rotation_point_y;
			 */

			//int xx = (int) ((float) new_x * Math.cos(Math.toRadians(yaw)) - (float) new_y * Math.sin(Math.toRadians(yaw)));
			//int yy = (int) ((float) new_x * Math.sin(Math.toRadians(yaw)) + (float) new_y * Math.cos(Math.toRadians(yaw)));

			//
			draw_canvas.save(); // SAVE
			//
			// *********** draw_canvas.translate(green_dot_x, green_dot_y);
			draw_canvas.translate(pixel_top_left_x, pixel_top_left_y);
			//draw_canvas.rotate(yaw, green_dot_x, green_dot_y);
			draw_canvas.rotate(yaw, 0, 0);
			////draw_canvas.drawBitmap(bigmap_bitmap_temp, new_x, new_y, null);

			//Paint paint4 = new Paint();
			//paint4.setColor(Color.RED);
			//paint4.setStyle(Style.FILL);

			//int dx = ((draw_canvas.getWidth() / 2) - green_dot_x);
			//int dy = ((draw_canvas.getHeight() / 2) - green_dot_y);
			//int dx2 = (int) ((draw_canvas.getWidth() / 2) - p_karte_pixel_x);
			//int dy2 = (int) ((draw_canvas.getHeight() / 2) - p_karte_pixel_y);
			//draw_canvas.drawBitmap(bigmap_bitmap_temp, new_x - green_dot_x, new_y - green_dot_y, null);
			// ******** draw_canvas.drawBitmap(bigmap_bitmap_temp, ssschx - green_dot_x, ssschy - green_dot_y, null);
			// draw_canvas.drawBitmap(bigmap_bitmap_temp, ssschx, ssschy, null);
			draw_canvas.drawBitmap(bigmap_bitmap_temp, 0, 0, null);

			//draw_canvas.drawCircle(pixel_top_left_x, pixel_top_left_y, 40, paint4);
			//draw_canvas.drawCircle(pixel_top_right_x, pixel_top_right_y, 40, paint4);
			//draw_canvas.drawLine(pixel_top_left_x, pixel_top_left_y, pixel_top_right_x, pixel_top_right_y, paint4);

			//draw_canvas.drawCircle(0, 0, 20, paint4);
			//paint4.setColor(Color.GREEN);
			//draw_canvas.drawCircle(ssschx, ssschy, 25, paint4);
			//paint4.setColor(Color.MAGENTA);
			//draw_canvas.drawCircle(ssschx - green_dot_x, ssschy - green_dot_y, 25, paint4);

			//System.out.println("schieb: " + (ssschx) + " " + (ssschy));

			//Paint paint = new Paint();
			//paint.setColor(Color.RED);
			//paint.setStyle(Style.FILL);
			//draw_canvas.drawCircle(rotation_point_x, rotation_point_y, 10, paint);
			//paint.setStyle(Style.STROKE);
			//paint.setStrokeWidth(3);
			//paint.setColor(Color.GREEN);
			//draw_canvas.drawCircle(green_dot_x, green_dot_y, 16, paint);
			//paint.setColor(Color.YELLOW);
			//draw_canvas.drawCircle(dx2, dy2, 26, paint);
			//paint.setColor(Color.GREEN);
			//draw_canvas.drawCircle(p_karte_pixel_x, p_karte_pixel_y, 36, paint);
			//int u = 40;
			//draw_canvas.drawRect(green_dot_x - u, green_dot_y - u, green_dot_x + u, green_dot_y + u, paint);
			//paint.setColor(Color.MAGENTA);
			//draw_canvas.drawRect(0 + new_x, 0 + new_y, bigmap_bitmap_temp.getWidth() + new_x, bigmap_bitmap_temp.getHeight() + new_y, paint);
			//draw_canvas.drawLine(bigmap_bitmap_temp.getWidth() / 2 + new_x, 0 + new_y, bigmap_bitmap_temp.getWidth() / 2 + new_x, bigmap_bitmap_temp.getHeight() + new_y, paint);
			//draw_canvas.drawLine(0 + new_x, bigmap_bitmap_temp.getHeight() / 2 + new_y, bigmap_bitmap_temp.getWidth() + new_x, bigmap_bitmap_temp.getHeight() / 2 + new_y, paint);

			//Paint paint2 = new Paint();
			//paint2.setColor(Color.YELLOW);
			//paint2.setStyle(Style.STROKE);
			//paint2.setStrokeWidth(3);
			//draw_canvas.drawCircle(green_dot_x - new_x, green_dot_y - new_y, 15, paint2);

			//
			draw_canvas.restore(); // RESTORE
			//

			//paint.setTextSize(30);
			//paint.setStrokeWidth(3);
			//paint.setColor(Color.BLACK);
			//paint.setStyle(Paint.Style.FILL);

			//			draw_canvas.drawText("yaw:" + yaw, 480 / 2, 800 / 2, paint);
			//			draw_canvas.drawText("mcx :" + mcx + "," + mcy, 480 / 2, (800 / 2) + 40, paint);
			//			draw_canvas.drawText("x :" + x_ + "," + y_, (480 / 2) - 150, (800 / 2) + 80, paint);
			//			draw_canvas.drawText("px :" + px_ + "," + py_, 480 / 2, (800 / 2) + 120, paint);
			//			draw_canvas.drawText("sssschx :" + ssschx + "," + ssschy, (480 / 2) - 150, (800 / 2) + 160, paint);
			//			draw_canvas.drawText("clat :" + clat + "," + clng, (480 / 2) - 150, (800 / 2) + 200, paint);

			//			usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			//			usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
			//			System.out.println("" + usedMegsString);

			bigmap_bitmap_temp.recycle();

			//			usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			//			usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
			//			System.out.println("" + usedMegsString);

			bigmap_bitmap_temp = null;

			//			usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			//			usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
			//			System.out.println("" + usedMegsString);

			//System.gc();

			//			usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			//			usedMegsString = String.format("FF - Memory Used: %d MB", usedMegs);
			//			System.out.println("" + usedMegsString);

			//System.out.println("yaw:" + yaw + " o:" + order + " x:" + x + " y:" + y);
		}
	}

	protected void send_osd_values(String id, String text1, String text2, String text3, int i1, int i2, int i3, int i4, float f1, float f2, float f3)
	{
		// Log.e("NavitOSDJava", "" + id + " " + text1 + " " + text2 + " " + text3 + " " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + f1 + " " + f2 + " " + f3);
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
					}
					else if ((text2 != null) && (text2.equals("navigation:nav_position:destination_length")))
					{
						// text3 = 575m -> driving distance to target
						// System.out.println("destination_length:" + text3);
						Navit.OSD_route_001.driving_distance_to_target = text3;
						Navit.OSD_route_001.driving_distance_to_target_valid = true;
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
								Navit.OSD_nextturn.nextturn_distance = text3;
								Navit.OSD_nextturn.nextturn_distance_valid = true;
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
				}
				else if ((text1 != null) && (text1.equals("direction")))
				{
					try
					{
						// Navit.OSD_compass.angle_north = Float.parseFloat(text2);
						Navit.OSD_compass.angle_north = i1;
						Navit.OSD_compass.angle_north_valid = true;
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

		// paint only every 300ms
		if (last_paint_OSD + 300 < System.currentTimeMillis())
		{
			try
			{
				//*****  NavitAOSDJava.postInvalidate();
				last_paint_OSD = System.currentTimeMillis();
				NavitOSDJava.draw_real();
			}
			catch (Exception r)
			{
				//r.printStackTrace();
			}
		}
	}

	protected void draw_mode(int mode)
	{
		//Log.e("NavitGraphics", "draw_mode mode=" + mode + " parent_graphics="
		//		+ String.valueOf(parent_graphics));

		if (mode == 2 && parent_graphics == null) view.invalidate();
		if (mode == 1 || (mode == 0 && parent_graphics != null)) draw_bitmap.eraseColor(0);

	}

	protected void draw_drag(int x, int y)
	{
		//Log.e("NavitGraphics","draw_drag");
		pos_x = x;
		pos_y = y;
	}

	protected void overlay_disable(int disable)
	{
		//Log.e("NavitGraphics","overlay_disable");
		// assume we are NOT in map view mode!
		// -> always in map mode now !!!! #  in_map = false;

		// check if overlay has been initialized yet
		//if (NavitAOverlay != null)
		//{
		//	NavitAOverlay.hide_bubble();
		//}

		overlay_disabled = disable;
	}

	protected void overlay_resize(int x, int y, int w, int h, int alpha, int wraparond)
	{
		//Log.e("NavitGraphics","overlay_resize");
		pos_x = x;
		pos_y = y;
	}

	public static String getLocalizedString(String text)
	{
		String ret = CallbackLocalizedString(text);
		//Log.e("NavitGraphics", "callback_handler -> lozalized string=" + ret);
		return ret;
	}

	/**
	 * generic message channel to C-code
	 */
	public native int CallbackMessageChannel(int i, String s);

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
				tmp_addr.lat = Float.parseFloat(tmp_s[2]);
				tmp_addr.lon = Float.parseFloat(tmp_s[3]);
				// the rest ist address
				tmp_addr.addr = s.substring(4 + tmp_s[0].length() + tmp_s[1].length() + tmp_s[2].length() + tmp_s[3].length(), s.length());

				// if "type" and "H id" and "text" is the same, treat it as a double entry!
				//    this is just a crude estimate now
				// *OLD* String hash_id = tmp_addr.result_type + ":" + tmp_addr.item_id.split("L")[0] + ":" + tmp_addr.addr;
				// String hash_id = tmp_addr.result_type + ":" + tmp_addr.item_id + ":" + tmp_addr.addr;
				String hash_id = tmp_addr.result_type + ":" + tmp_addr.lat + ":" + tmp_addr.lon + ":" + tmp_addr.addr;
				//System.out.println("hash_id=" + hash_id);
				if (!Navit.Navit_Address_Result_double_index.contains(hash_id))
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

					Navit.NavitSearchresultBar_title = Navit.get_text("loading search results");
					Navit.NavitSearchresultBar_text = Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn;

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

	public void SearchResultList(int i, int partial_match, String text, int flags, String country_iso2, String search_latlon, int search_radius)
	{
		CallbackSearchResultList(i, partial_match, text, flags, country_iso2, search_latlon, search_radius);
	}

	public native void CallbackSearchResultList(int i, int partial_match, String s, int flags, String country_iso2, String search_latlon, int search_radius);

	/**
	 * get localized string
	 */
	public static native String CallbackLocalizedString(String s);

	//
	//
	//
	// get route_status value from C-lib
	/*
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

	//
	//
	//

	// i=1 -> pixel a,b (x,y)      -> geo   string "lat(float)#lng(float)"
	// i=2 -> geo   a,b (lat,lng)  -> pixel string "x(int)#y(int)"
	public static native String CallbackGeoCalc(int i, float a, float b);

	public static void return_generic_int(int id, int i)
	{
		// System.out.println("id=" + id + " i=" + i);

		if (id == 1)
		{
			// id=1 -> route_status
			NavitGraphics.navit_route_status = i;
			if (i == 0)
			{
				Navit.set_debug_messages1("");
			}
			else if (i == 1)
			{
				Navit.set_debug_messages1("Destination set");
			}
			else if (i == 3)
			{
				Navit.set_debug_messages1("No route found / Route blocked");
			}
			else if (i == 5)
			{
				Navit.set_debug_messages1("Building route path");
			}
			else if (i == 13)
			{
				Navit.set_debug_messages1("Building route graph");
			}
			else if (i == 17)
			{
				Navit.set_debug_messages1("Route found");
			}
			else if (i == 33)
			{
				Navit.set_debug_messages1("Route found");
			}
		}
		else if (id == 2)
		{
			// id=2 -> map draw finished
			if (DEBUG_TOUCH) System.out.println("wait_for_redraw_map=false xx1");
			NavitGraphics.wait_for_redraw_map = false;
			try
			{
				NavitAndroidOverlay.overlay_draw_thread1.stop_redraw();
				NavitAndroidOverlay.overlay_draw_thread1.stop_me();
				NavitAndroidOverlay.overlay_draw_thread1 = null;
			}
			catch (Exception e)
			{
				// e.printStackTrace();
			}
			try
			{
				NavitAOverlay_s.postInvalidate();
			}
			catch (Exception e)
			{
				e.printStackTrace();
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

}
