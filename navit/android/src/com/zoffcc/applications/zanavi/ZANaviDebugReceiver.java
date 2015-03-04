/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2014 Zoff <zoff@zoff.cc>
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;

public class ZANaviDebugReceiver extends BroadcastReceiver
{

	static boolean stop_me = false;
	static boolean dont_save_loc = false;

	/*
	 * 
	 * Examples:
	 * 
	 * (params: "lat, lon")
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es set_position2 "48.656, 15.6777"
	 * 
	 * lat, lon, speed (m/s), direction (degrees)
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es set_position "48.656, 15.6777, 12, -75"
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es add_destination "48.656, 15.6777"
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es clear_route ""
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es disable_normal_location ""
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es enable_normal_location ""
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es export_route_gpx ""
	 * 
	 * 
	 * adb shell am broadcast -a com.zoffcc.applications.zanavi.comm --es set_pos_and_dest "Via_Graf 9.1439748 45.5133242 9.1391345 45.5146592"
	 * 
	 * -
	 */

	static void disable_normal_location()
	{
		NavitVehicle.turn_off_all_providers();
		NavitVehicle.turn_off_sat_status();
		System.out.println("ZANaviDebugReceiver:" + "disable normal location");
	}

	static void DR_add_destination(String key, Bundle extras)
	{
		Object value = extras.get(key);
		String value2 = value.toString().replaceAll("\\s+", "").replaceAll("\"", "");
		float lat = Float.parseFloat(value2.split(",", 2)[0]);
		float lon = Float.parseFloat(value2.split(",", 2)[1]);

		if (NavitGraphics.navit_route_status == 0)
		{
			Navit.destination_set();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 3);
			b.putString("lat", String.valueOf(lat));
			b.putString("lon", String.valueOf(lon));
			b.putString("q", "DEST 001");
			msg.setData(b);
			Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 48);
			b.putString("lat", String.valueOf(lat));
			b.putString("lon", String.valueOf(lon));
			b.putString("q", "DEST");
			msg.setData(b);
			Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
		}

		System.out.println("ZANaviDebugReceiver:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
	}

	static void DR_set_position(String key, Bundle extras)
	{
		disable_normal_location();

		Object value = extras.get(key);
		String value2 = value.toString().replaceAll("\\s+", "").replaceAll("\"", "");
		float lat = Float.parseFloat(value2.split(",", 4)[0]);
		float lon = Float.parseFloat(value2.split(",", 4)[1]);
		float speed = Float.parseFloat(value2.split(",", 4)[2]);
		float direction = Float.parseFloat(value2.split(",", 4)[3]);
		if (direction < 0)
		{
			direction = direction + 360;
		}
		else if (direction > 360)
		{
			direction = direction - 360;
		}

		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 97);
		b.putString("lat", String.valueOf(lat));
		b.putString("lon", String.valueOf(lon));
		b.putString("q", "POSITION");
		msg.setData(b);
		// *DISABLE* Navit.N_NavitGraphics.callback_handler.sendMessage(msg);

		Location l = new Location("ZANavi Comm");
		//		if (NavitVehicle.fastProvider_s == null)
		//		{
		//			l = new Location("ZANavi Comm");
		//		}
		//		else
		//		{
		//			l = new Location(NavitVehicle.fastProvider_s);
		//		}
		l.setLatitude(lat);
		l.setLongitude(lon);
		l.setBearing(direction);
		l.setSpeed(speed);
		l.setAccuracy(4.0f); // accuracy 4 meters
		// NavitVehicle.update_compass_heading(direction);
		NavitVehicle.set_mock_location__fast(l);

		// System.out.println("ZANaviDebugReceiver:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
		// System.out.println("ZANaviDebugReceiver:" + "speed=" + speed + " dir=" + direction);
	}

	static void DR_save_route_to_gpx_file()
	{
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 96);
		String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
		String filename = Navit.NAVIT_DATA_DEBUG_DIR + "/zanavi_route_" + date + ".gpx";
		b.putString("s", filename);
		msg.setData(b);
		Navit.N_NavitGraphics.callback_handler.sendMessage(msg);
		System.out.println("ZANaviDebugReceiver:" + "file=" + filename);
	}

	static void DR_clear_route()
	{
		// clear any previous destinations
		Message msg2 = new Message();
		Bundle b2 = new Bundle();
		b2.putInt("Callback", 7);
		msg2.setData(b2);
		Navit.N_NavitGraphics.callback_handler.sendMessage(msg2);
		System.out.println("ZANaviDebugReceiver:" + "clear route");
	}

	static void DR_replay_gps_file(String filename)
	{

		System.out.println("ZANaviDebugReceiver:" + "Enter!!");

		stop_me = false;
		dont_save_loc = true;

		try
		{
			if ((filename != null) && (!filename.equals("")))
			{
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(filename));

				Bundle extras2;

				String line = "";
				String[] line_parts;

				while ((line = br.readLine()) != null)
				{
					if (line.length() > 1)
					{
						System.out.println("ZANaviDebugReceiver:" + "line=" + line);
					}

					if (stop_me == true)
					{
						try
						{
							br.close();
						}
						catch (Exception ce)
						{
							ce.printStackTrace();
						}
						return;
					}

					if (line.length() > 3)
					{
						extras2 = new Bundle();
						line_parts = line.split(":", 2);

						if (line_parts[0].equals("CLR"))
						{
							DR_clear_route();
							Thread.sleep(200);
						}
						else if (line_parts[0].equals("DST"))
						{
							extras2.putString("add_destination", line_parts[1]);
							DR_add_destination("add_destination", extras2);
							Thread.sleep(5000); // wait 5 seconds to calc route
						}
						else if (line_parts[0].equals("POS"))
						{
							extras2.putString("set_position", line_parts[1]);
							DR_set_position("set_position", extras2);
							Thread.sleep(950);
						}
						else
						{
						}

					}
				}
				System.out.println("ZANaviDebugReceiver:" + "while loop end");
				br.close();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("ZANaviDebugReceiver:" + "EX:" + e.getMessage());
		}

		dont_save_loc = true;

		System.out.println("ZANaviDebugReceiver:" + "Leave!!");
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (Navit.PREF_enable_debug_enable_comm)
		{
			System.out.println("ZANaviDebugReceiver:" + "enter");
			try
			{
				Bundle extras = intent.getExtras();
				// System.out.println("ZANaviDebugReceiver:" + "command " + extras.toString());

				if (extras != null)
				{
					for (String key : extras.keySet())
					{
						if (key.equals("set_position2"))
						{
							Object value = extras.get(key);
							String value2 = value.toString().replaceAll("\\s+", "").replaceAll("\"", "");
							float lat = Float.parseFloat(value2.split(",", 2)[0]);
							float lon = Float.parseFloat(value2.split(",", 2)[1]);
							System.out.println("ZANaviDebugReceiver:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));

							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 97);
							b.putString("lat", String.valueOf(lat));
							b.putString("lon", String.valueOf(lon));
							b.putString("q", "POSITION");
							msg.setData(b);
							Navit.N_NavitGraphics.callback_handler.sendMessage(msg);

							break;
						}
						else if (key.equals("set_pos_and_dest"))
						{
							Object value = extras.get(key);
							String value2 = value.toString().replaceAll("\"", "");
							String route_name = value2.split(" ", 5)[0];
							float lat1 = Float.parseFloat(value2.split(" ", 5)[2]);
							float lon1 = Float.parseFloat(value2.split(" ", 5)[1]);
							float lat2 = Float.parseFloat(value2.split(" ", 5)[4]);
							float lon2 = Float.parseFloat(value2.split(" ", 5)[3]);

							System.out.println("ZANaviDebugReceiver:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));

							DR_clear_route();
							Thread.sleep(200);
							extras = new Bundle();
							extras.putString("set_position", "" + lat1 + "," + lon1 + "," + "0.0" + "," + "0");
							DR_set_position("set_position", extras);
							Thread.sleep(1000);
							extras = new Bundle();
							extras.putString("add_destination", "" + lat2 + "," + lon2);
							DR_add_destination("add_destination", extras);

							final Thread debug_zoom_to_route_001 = new Thread()
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
												// zoom to route
												Message msg = new Message();
												Bundle b = new Bundle();
												b.putInt("Callback", 17);
												msg.setData(b);
												Navit.N_NavitGraphics.callback_handler.sendMessage(msg);

												Navit.set_map_position_to_screen_center();

												// save route to gpx file
												DR_save_route_to_gpx_file();

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

												Message msg7 = Navit.Navit_progress_h.obtainMessage();
												Bundle b7 = new Bundle();
												msg7.what = 2; // long Toast message
												b7.putString("text", Navit.get_text("saving route to GPX-file failed")); //TRANS
												msg7.setData(b7);
												Navit.Navit_progress_h.sendMessage(msg7);
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
							debug_zoom_to_route_001.start();

							break;
						}
						else if (key.equals("set_position"))
						{
							DR_set_position(key, extras);
							break;
						}
						else if (key.equals("add_destination"))
						{
							DR_add_destination(key, extras);

							final Thread debug_zoom_to_route_002 = new Thread()
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
												// zoom to route
												Message msg = new Message();
												Bundle b = new Bundle();
												b.putInt("Callback", 17);
												msg.setData(b);
												Navit.N_NavitGraphics.callback_handler.sendMessage(msg);

												Navit.set_map_position_to_screen_center();

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
							debug_zoom_to_route_002.start();

							break;
						}
						else if (key.equals("export_route_gpx"))
						{
							DR_save_route_to_gpx_file();
							break;
						}
						else if (key.equals("clear_route"))
						{
							DR_clear_route();
							break;
						}
						else if (key.equals("disable_normal_location"))
						{
							disable_normal_location();
							break;
						}
						else if (key.equals("enable_normal_location"))
						{
							NavitVehicle.turn_on_fast_provider();
							NavitVehicle.turn_on_precise_provider();
							NavitVehicle.turn_on_sat_status();
							System.out.println("ZANaviDebugReceiver:" + "enable normal location");
							break;
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}