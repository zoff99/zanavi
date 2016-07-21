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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.view.inputmethod.InputMethodManager;

public class ZANaviDebugReceiver extends BroadcastReceiver
{

	static boolean stop_me = false;
	static boolean dont_save_loc = false;

	private static int skip_count = 0;
	private static int rewind_count = 0;
	final private static int skip = 30;
	static boolean is_replaying = false;
	static boolean flag_route_ready = false;
	static String file_name_global = "";

	static String success_source = "";
	static String success_item = "";
	static String success_value = "";
	static String success_operator = "";
	static int result_code = -1;
	static int local_meters_value = 0;

	static int yaml_sum = 0;
	static int yaml_err = 0;

	static double lat_pos = 0.0;
	static double lon_pos = 0.0;
	static double lat_dst = 0.0;
	static double lon_dst = 0.0;
	static double heading_pos = 0.0;

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

	static void enable_normal_location()
	{
		Navit.runOnUI(new Runnable()
		{
			@Override
			public void run()
			{
				System.out.println("ZANaviDebugReceiver:" + "ENABLE normal location");

				if (Navit.p.PREF_use_fast_provider)
				{
					NavitVehicle.turn_on_fast_provider();
				}
				else
				{
					NavitVehicle.turn_off_fast_provider();
				}

				// activate gps AFTER 3g-location
				NavitVehicle.turn_on_precise_provider();
			}
		});
	}

	static void disable_normal_location()
	{
		Navit.runOnUI(new Runnable()
		{
			@Override
			public void run()
			{
				NavitVehicle.turn_off_all_providers();
				NavitVehicle.turn_off_sat_status();
				System.out.println("ZANaviDebugReceiver:" + "disable normal location");
			}
		});
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
			NavitGraphics.callback_handler.sendMessage(msg);
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
			NavitGraphics.callback_handler.sendMessage(msg);
		}

		System.out.println("ZANaviDebugReceiver:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
	}

	static void DR_set_position(String key, Bundle extras, boolean disable_loc)
	{
		if (disable_loc)
		{
			disable_normal_location();
		}

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
		NavitGraphics.callback_handler.sendMessage(msg);
		System.out.println("ZANaviDebugReceiver:" + "file=" + filename);
	}

	static void DR_save_route_to_gpx_file_with_name(String name)
	{
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 96);
		// String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
		String filename = name;
		b.putString("s", filename);
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);
		System.out.println("ZANaviDebugReceiver:" + "file=" + filename);
	}

	static void DR_clear_route()
	{
		// clear any previous destinations
		Message msg2 = new Message();
		Bundle b2 = new Bundle();
		b2.putInt("Callback", 7);
		msg2.setData(b2);
		NavitGraphics.callback_handler.sendMessage(msg2);
		System.out.println("ZANaviDebugReceiver:" + "clear route");
	}

	static void DR_rewind_small()
	{
		rewind_count = 20;
		System.out.println("ZANaviDebugReceiver:" + "rewind_small");
	}

	static void DR_skip()
	{
		skip_count = 0;
		System.out.println("ZANaviDebugReceiver:" + "skip");
	}

	static void DR_replay_yaml_file(String filename, final String date)
	{

		file_name_global = filename;

		yaml_err++; // incr. error count (on success later -> decrease it again)

		try
		{
			if ((filename != null) && (!filename.equals("")))
			{
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(filename));

				is_replaying = true;
				NavitGraphics.NavitAOverlay_s.postInvalidate();

				disable_normal_location();

				String mode = "-";
				lat_pos = 0.0;
				lon_pos = 0.0;
				lat_dst = 0.0;
				lon_dst = 0.0;
				heading_pos = 0.0;

				String line = "";
				while ((line = br.readLine()) != null)
				{
					if ((line.length() >= "from:".length()) && (line.equals("from:")))
					{
						mode = "from";
					}
					else if ((line.length() >= "to:".length()) && (line.equals("to:")))
					{
						mode = "to";
					}
					else if ((line.length() >= "success:".length()) && (line.equals("success:")))
					{
						mode = "success";
					}
					else if ((line.length() >= "capture:".length()) && (line.equals("capture:")))
					{
						mode = "capture";
					}
					else if ((line.length() > 2) && (!line.startsWith("#")))
					{
						try
						{
							if (mode == "success")
							{
								/*
								 * source: 'dbus'
								 * item: 'status'
								 * value: 33
								 * operator: '=='
								 */

								System.out.println("roadbook:su:" + line);

								String name_str = line.split(":", 2)[0].replace(" ", "");
								String value_str = line.split(":", 2)[1].replace(" ", "");

								if (name_str.equalsIgnoreCase("source"))
								{
									success_source = value_str;
								}
								else if (name_str.equalsIgnoreCase("item"))
								{
									success_item = value_str;
								}
								else if (name_str.equalsIgnoreCase("value"))
								{
									success_value = value_str;
								}
								else if (name_str.equalsIgnoreCase("operator"))
								{
									success_operator = value_str;
								}
							}
							else
							{
								// read lat,lon
								String name_str = line.split(":", 2)[0];
								String value_str = line.split(":", 2)[1];
								double lat = 0.0;
								double lon = 0.0;
								double heading = 0.0;

								if (name_str.contains("lat"))
								{
									lat = Double.parseDouble(value_str);
									if (mode.equals("from"))
									{
										lat_pos = lat;
									}
									else if (mode.equals("to"))
									{
										lat_dst = lat;
									}
								}
								else if (name_str.contains("lng"))
								{
									lon = Double.parseDouble(value_str);
									if (mode.equals("from"))
									{
										lon_pos = lon;
									}
									else if (mode.equals("to"))
									{
										lon_dst = lon;
									}
								}
								else if (name_str.contains("heading"))
								{
									heading = Double.parseDouble(value_str);
									if (mode.equals("from"))
									{
										heading_pos = heading;
									}
								}
							}
						}
						catch (Exception e2)
						{
							System.out.println("_DREX_:001" + e2.getMessage());
						}
					}
				}

				DR_clear_route();
				Thread.sleep(1000);

				try
				{
					int jj = 0;
					while ((NavitGraphics.navit_route_status != 0) && (jj < 120))
					{
						jj++;
						System.out.println("ZANaviDebugReceiver:" + "waiting for route to clear (status=" + NavitGraphics.navit_route_status + "):" + jj);
						// wait for old route to be cleared
						Thread.sleep(1000);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// DR_clear_route();
				// Thread.sleep(3000);

				Bundle extras = new Bundle();
				System.out.println("ZANaviDebugReceiver:" + "set_position" + lat_pos + "," + lon_pos + "," + "0.0" + "," + heading_pos);
				extras.putString("set_position", "" + lat_pos + "," + lon_pos + "," + "0.0" + "," + heading_pos);
				DR_set_position("set_position", extras, true);
				Thread.sleep(2500);
				//xy//DR_set_position("set_position", extras, true);
				//xy//Thread.sleep(3200);

				extras = new Bundle();
				System.out.println("ZANaviDebugReceiver:" + "add_destination" + lat_dst + "," + lon_dst);
				extras.putString("add_destination", "" + lat_dst + "," + lon_dst);
				DR_add_destination("add_destination", extras);
				Thread.sleep(2000);

				flag_route_ready = false;

				// Thread.sleep(1200);
				// Navit.draw_map();

				System.out.println("ZANaviDebugReceiver:" + "mem0:" + Navit.logHeap_for_batch(Navit.Global_Navit_Object.getClass()));

				final Thread debug_zoom_to_route_001 = new Thread()
				{
					int wait = 1;
					int count = 0;
					int max_count = 380; // seconds
					int first_status = -999;
					int status_wrong = 0;

					@Override
					public void run()
					{
						while (wait == 1)
						{
							try
							{
								System.out.println("XXXX:#" + count + ":rstatus=" + NavitGraphics.navit_route_status);

								if (first_status == -999)
								{
									first_status = NavitGraphics.navit_route_status;
								}

								if ((first_status == -999) || (first_status == 17) || (first_status == 33))
								{
									if (count < 30)
									{
										// wait some more! status is wrong it seems!
										status_wrong = 1;
									}
									else
									{
										status_wrong = 0;
									}
								}

								System.out.println("ZANaviDebugReceiver:" + "mem1:" + Navit.logHeap_for_batch(Navit.Global_Navit_Object.getClass()));

								if (((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33)) && (status_wrong == 0))
								{
									System.out.println("XXXX:--:001");

									// Navit.static_show_route_graph(4);

									Navit.zoom_to_route();
									Thread.sleep(2000);
									Navit.zoom_to_route();
									Thread.sleep(15000);
									//xy//Navit.draw_map();
									//xy//Thread.sleep(15000);

									System.out.println("XXXX:--:002");

									File f = new File(file_name_global);
									File d2 = new File(f.getParent() + "/" + date + "/");
									d2.mkdirs();

									System.out.println("XXXX:--:003");

									// save route to gpx file
									DR_save_route_to_gpx_file_with_name(f.getParent() + "/" + date + "/" + f.getName() + ".gpx");

									System.out.println("XXXX:--:004");

									wait = 0;
								}
								else
								{
									System.out.println("XXXX:--:005");

									wait = 1;
								}

								if (count > 0)
								{
									System.out.println("XXXX:--:006");

									//if (NavitGraphics.navit_route_status == 1)
									if (NavitGraphics.navit_route_status == 999199) // disable !!
									{
										System.out.println("XXXX:--:007");
										wait = 0;
										count = max_count;
									}
									else if (NavitGraphics.navit_route_status == 3)
									{
										System.out.println("XXXX:--:008");
										wait = 0;
										count = max_count;
									}
								}

								System.out.println("XXXX:--:009");

								count++;
								if (count > max_count)
								{
									System.out.println("XXXX:--:010");

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
									System.out.println("XXXX:--:011");

									Thread.sleep(1000);
								}
							}
							catch (Exception e)
							{
								System.out.println("XXXX:--:012");

								System.out.println("_DREX_:002" + Navit.stacktrace_to_string(e));
							}
						}

						System.out.println("XXXX:--:013");

						System.out.println("XXXX:rstatus[FIN]=" + NavitGraphics.navit_route_status);

						try
						{

							if (NavitGraphics.navit_route_status == 3)
							{
								// route blocked / no route found
								DR_clear_route();
								Thread.sleep(1000);
								System.out.println("XXXX:route blocked");
							}
							else if (NavitGraphics.navit_route_status == 1)
							{
								// still calculating route
								DR_clear_route();
								Thread.sleep(1000);
								System.out.println("XXXX:still calculating route");
							}
							else
							{
								// save map screenshot
								File f = new File(file_name_global);
								// System.out.println("NNNNN=" + f.getParent() + "/" + date + "/" + f.getName());
								File d2 = new File(f.getParent() + "/" + date + "/");
								d2.mkdirs();
								Navit.take_map_screenshot(f.getParent() + "/" + date + "/", f.getName());
								// Navit.take_phone_screenshot(Navit.Global_Navit_Object, f.getParent() + "/" + date + "/", f.getName() + "_full");

								// save roadbook
								String[] separated = NavitGraphics.GetRoadBookItems(9990001);

								if (separated == null)
								{
									System.out.println("XXXX:Roadbook try #2");

									try
									{
										Thread.sleep(3000);
									}
									catch (InterruptedException e)
									{
									}
									separated = NavitGraphics.GetRoadBookItems(9990001);

									if (separated == null)
									{
										System.out.println("XXXX:Roadbook try #3");

										try
										{
											Bundle extras77 = new Bundle();
											System.out.println("ZANaviDebugReceiver:77:" + "set_position" + lat_pos + "," + lon_pos + "," + "0.0" + "," + "0");
											extras77.putString("set_position", "" + lat_pos + "," + lon_pos + "," + "0.0" + "," + "0");
											DR_set_position("set_position", extras77, true);
											Thread.sleep(1000);
										}
										catch (InterruptedException e)
										{
										}

										try
										{
											Thread.sleep(6000);
										}
										catch (InterruptedException e)
										{
										}
										separated = NavitGraphics.GetRoadBookItems(9990001);

										if (separated == null)
										{
											separated = new String[4];
											separated[0] = "broken";
											separated[1] = "broken";
											separated[2] = "broken";
											separated[3] = "broken";
										}
									}
								}

								System.out.println("XXXX:Roadbook(1)=" + separated);
								if (separated != null)
								{
									try
									{
										System.out.println("XXXX:Roadbook(2)=" + Arrays.toString(separated));
									}
									catch (Exception e)
									{
									}
								}

								if ((separated != null) && (separated.length < 3))
								{
									System.out.println("_DRxx_:018" + "Roadbook items < 3 !!");
								}

								int jk = 0;
								if (separated != null)
								{
									if (separated.length > 2)
									{
										FileOutputStream outf = null;
										try
										{
											outf = new FileOutputStream(f.getParent() + "/" + date + "/" + f.getName() + ".result.txt");
										}
										catch (Exception ef)
										{
											//System.out.println("EE002:" + ef.getMessage());
											System.out.println("_DREX_:003" + ef.getMessage());
										}

										OutputStreamWriter out = null;
										try
										{
											out = new OutputStreamWriter(outf);
										}
										catch (Exception e)
										{
											// System.out.println("EE003:" + e.getMessage());
											System.out.println("_DREX_:004" + e.getMessage());
										}

										//System.out.println("Roadbook:length=" + (separated.length - 2));
										try
										{
											out.write("Roadbook:length=" + (separated.length - 2) + "\n");
										}
										catch (Exception e)
										{
											//System.out.println("EE004:" + e.getMessage());
											System.out.println("_DREX_:005" + e.getMessage());
										}

										for (jk = 0; jk < separated.length; jk++)
										{
											System.out.println("ROADBOOK_RES=" + jk + ":" + separated[jk]);
											if (jk > 2)
											{
												String[] values = new String[5];
												String[] values2 = separated[jk].split(":");
												values[0] = values2[0];
												values[1] = values2[1];
												values[2] = values2[2];
												values[3] = values2[3];
												try
												{
													values[4] = values2[4];
												}
												catch (Exception ee)
												{
													values[4] = "";
													System.out.println("_DREX_:006" + ee.getMessage());
												}
												// 0 string:distance short form
												// 1 lat
												// 2 lon
												// 3 icon name
												// 4 text

												//									if (values[3].compareTo("nav_waypoint") == 0)
												//									{
												//									}
												//									else if (values[3].compareTo("nav_destination") == 0)
												//									{
												//									}
												//									else
												//									{
												//									}

												try
												{
													System.out.println("Roadbook:" + jk + ":" + values[0] + ":" + values[1] + ":" + values[2] + ":" + values[3] + ":" + values[4]);
													out.write("Roadbook:" + jk + ":" + values[0] + ":" + values[1] + ":" + values[2] + ":" + values[3] + ":" + values[4] + "\n");
												}
												catch (Exception ee)
												{
													System.out.println("_DREX_:007" + ee.getMessage());
												}
											}
											else if (jk == 1)
											{
												String[] values_local = separated[jk].split(":");
												try
												{
													local_meters_value = Integer.parseInt(values_local[1]);
												}
												catch (Exception e)
												{
													local_meters_value = 0;
													System.out.println("_DREX_:008" + Navit.stacktrace_to_string(e));
												}

												System.out.println("Roadbook:distance=" + local_meters_value);
												try
												{
													out.write("Roadbook:distance [m]=" + local_meters_value + "\n");
												}
												catch (Exception e)
												{
													System.out.println("_DREX_:009" + e.getMessage());
												}
											}
											else if (jk == 2)
											{
												System.out.println("Roadbook:" + jk + ":" + "0" + ":" + "0" + ":" + "0" + ":" + "start" + ":" + "");
												try
												{
													out.write("Roadbook:" + jk + ":" + "0" + ":" + "0" + ":" + "0" + ":" + "start" + ":" + "" + "\n");
												}
												catch (Exception e)
												{
													System.out.println("_DREX_:010" + e.getMessage());
												}
											}
										}

										try
										{
											out.write("URL1:" + "http://map.project-osrm.org/?z=10&loc=" + lat_pos + "%2C" + lon_pos + "&loc=" + lat_dst + "%2C" + lon_dst + "&hl=en&alt=0\n");
											out.write("URL2:" + "https://graphhopper.com/maps/?point=" + lat_pos + "%2C" + lon_pos + "&point=" + lat_dst + "%2C" + lon_dst + "\n");
											out.write("URL3:" + "http://www.google.com/maps/dir/" + lat_pos + "," + lon_pos + "/" + lat_dst + "," + lon_dst + "\n");
											out.write("URL4:" + "http://www.openstreetmap.org/directions?engine=osrm_car&route=" + lat_pos + "%2C" + lon_pos + "%3B" + lat_dst + "%2C" + lon_dst + "\n");
										}
										catch (Exception e)
										{
											System.out.println("_DREX_:046" + e.getMessage());
										}

										try
										{
											out.write(Navit.CI_TEST_CASE_TEXT);
										}
										catch (Exception e)
										{
											System.out.println("_DREX_:046freetext" + e.getMessage());
										}

										try
										{
											out.flush();
											out.close();
											outf.flush();
											outf.close();
										}
										catch (Exception e)
										{
											System.out.println("_DREX_:016" + e.getMessage());
										}
									}
								}
								//								else
								//								{
								//									System.out.println("_DRxx_:017" + "Roadbook items = NULL !!");
								//								}

								// calculate success criterion ----------------------
								// calculate success criterion ----------------------

								result_code = -1;

								if ((!success_operator.equals("")) && (!success_value.equals("")))
								{
									System.out.println("roadbook:so=" + success_source);
									System.out.println("roadbook:it=" + success_item);
									System.out.println("roadbook:sv=" + success_value);

									if (success_source.equalsIgnoreCase("'dbus'"))
									{
										if (success_item.equalsIgnoreCase("'status'"))
										{
											int s = NavitGraphics.navit_route_status;
											int v = Integer.parseInt(success_value);

											if ((success_operator.contains(">")) || ((success_operator.contains("<"))))
											{
												if (s == 17)
												{
													s = 33;
												}
											}
											else
											{
												if (v == 33)
												{
													v = 17;
													if (s == 33)
													{
														s = 17;
													}
												}
												else if (v == 17)
												{
													if (s == 33)
													{
														s = 17;
													}
												}
											}
											result_code = success_value_compare(s, v);

											System.out.println("roadbook:003:" + s + " " + v);
										}
										else if (success_item.equalsIgnoreCase("'distance'"))
										{
											int s = local_meters_value;
											int v = Integer.parseInt(success_value);
											result_code = success_value_compare(s, v);

											System.out.println("roadbook:001:" + s + " " + v);
										}
									}
									else if (success_source.equalsIgnoreCase("'gpx'"))
									{
										if (success_item.equalsIgnoreCase("'nodes'"))
										{
											int s = -99;
											s = (separated.length - 2);
											int v = Integer.parseInt(success_value);
											result_code = success_value_compare(s, v);

											System.out.println("roadbook:002:" + s + " " + v);
										}
										else if (success_item.startsWith("'nav"))
										{
											String nav_num_str = success_item.replace("'nav", "").replace("'", "");
											int nav_num = Integer.parseInt(nav_num_str);

											if (separated.length > (nav_num + 2))
											{
												// ok we have enough entries in roadbook

												String[] values = new String[5];
												String[] values2 = separated[nav_num + 2].split(":");
												values[0] = values2[0];
												values[1] = values2[1];
												values[2] = values2[2];
												values[3] = values2[3];
												try
												{
													values[4] = values2[4];
												}
												catch (Exception ee)
												{
													values[4] = "";
													System.out.println("_DREX_:006entries" + ee.getMessage());
												}
												// 0 string:distance short form
												// 1 lat
												// 2 lon
												// 3 icon name
												// 4 text

												if (values[3].equalsIgnoreCase(success_value))
												{
													result_code = 0;
												}
												else
												{
													result_code = -1;
												}
											}
											else
											{
												// not enough entries in roadbook --> fail
												result_code = -1;
											}

											System.out.println("roadbook:002:" + success_item + " " + success_value);
										}

									}
								}

								System.out.println("++--------++ roadbook:RES=" + result_code);

								if (result_code == 0)
								{
									String orig = f.getParent() + "/" + date + "/" + f.getName() + ".result.txt";
									String rename_to = f.getParent() + "/" + date + "/" + f.getName() + "._SUCCESS_.result.txt";
									File f2 = new File(orig);
									File f2_to = new File(rename_to);
									f2.renameTo(f2_to);

									yaml_err--;
								}
								else
								{
								}

								// calculate success criterion ----------------------
								// calculate success criterion ----------------------

							}

						}
						catch (Exception ebig)
						{
							System.out.println("_DREX_:033" + Navit.stacktrace_to_string(ebig));
						}

						flag_route_ready = true;
					}
				};
				debug_zoom_to_route_001.start();
				debug_zoom_to_route_001.join();
				Thread.sleep(500);

				is_replaying = false;
				NavitGraphics.NavitAOverlay_s.postInvalidate();
				br.close();
			}
		}
		catch (Exception e)
		{
			System.out.println("_DREX_:011" + e.getMessage());
			is_replaying = false;
			NavitGraphics.NavitAOverlay_s.postInvalidate();
		}
	}

	static void DR_replay_yaml_file_search_n(String filename, final String date)
	{

		file_name_global = filename;

		yaml_err++; // incr. error count (on success later -> decrease it again)

		try
		{
			if ((filename != null) && (!filename.equals("")))
			{
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(filename));

				is_replaying = true;
				NavitGraphics.NavitAOverlay_s.postInvalidate();

				disable_normal_location();

				DR_clear_route();
				Thread.sleep(1000);

				String str_str = "";
				String city_str = "";
				String hn_str = "";

				String mode = "-";
				String line = "";
				while ((line = br.readLine()) != null)
				{
					if ((line.length() >= "type:".length()) && (line.equals("type:")))
					{
						mode = "type";
					}
					else if ((line.length() >= "input:".length()) && (line.equals("input:")))
					{
						mode = "input";
					}
					else if ((line.length() >= "success:".length()) && (line.equals("success:")))
					{
						mode = "success";
					}
					else if ((line.length() > 2) && (!line.startsWith("#")))
					{
						try
						{
							if (mode == "success")
							{
								String name_str = line.split(":", 2)[0].replace(" ", "");
								String value_str = line.split(":", 2)[1].replace(" ", "");

								if (name_str.equalsIgnoreCase("item"))
								{
									success_item = value_str;
								}
								else if (name_str.equalsIgnoreCase("value"))
								{
									success_value = value_str;
								}
								else if (name_str.equalsIgnoreCase("operator"))
								{
									success_operator = value_str;
								}
							}
							else if (mode == "type")
							{
								// do nothing
							}
							else
							{
								String name_str = line.split(":", 2)[0];
								String value_str = line.split(":", 2)[1];

								if (name_str.contains("street"))
								{
									str_str = value_str;
								}
								else if (name_str.contains("city"))
								{
									city_str = value_str;
								}
								else if (name_str.contains("housenumber"))
								{
									hn_str = value_str;
								}
							}
						}
						catch (Exception e2)
						{
							System.out.println("_DREX_:001" + e2.getMessage());
						}
					}
				}

				try
				{
					int jj = 0;
					while ((NavitGraphics.navit_route_status != 0) && (jj < 120))
					{
						jj++;
						System.out.println("ZANaviDebugReceiver:" + "waiting for route to clear (status=" + NavitGraphics.navit_route_status + "):" + jj);
						// wait for old route to be cleared
						Thread.sleep(1000);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				final String str_str_f = str_str;
				final String city_str_f = city_str;
				final String hn_str_f = hn_str;

				System.out.println("ZANaviDebugReceiver:" + "mem0:" + Navit.logHeap_for_batch(Navit.Global_Navit_Object.getClass()));

				final Thread search_n_002 = new Thread()
				{
					int wait = 1;

					@Override
					public void run()
					{
						while (wait == 1)
						{
							try
							{
								// -------- ST N --------
								// -------- ST N --------
								// -------- ST N --------

								// clear results
								Navit.NavitAddressResultList_foundItems.clear();
								Navit.Navit_Address_Result_double_index.clear();
								Navit.NavitSearchresultBarIndex = -1;
								Navit.NavitSearchresultBar_title = "";
								Navit.NavitSearchresultBar_text = "";
								Navit.search_results_towns = 0;
								Navit.search_results_streets = 0;
								Navit.search_results_streets_hn = 0;
								Navit.search_results_poi = 0;

								Navit.search_list_ready = false;

								final Thread tttt2 = new Thread()
								{
									@Override
									public void run()
									{
										System.out.println("search test 001");
										Navit.use_index_search = false;
										Navit.executeSearch_with_values(str_str_f, city_str_f, hn_str_f, true, false, true, true);
									}
								};
								tttt2.start();
								System.out.println("search test 002");
								Thread.sleep(2000);
								System.out.println("search test 003");

								// -- wait for search result --
								int c4 = 0;
								boolean no_result = false;
								while (Navit.search_ready == false)
								{
									System.out.println("search test 004:" + c4);
									System.out.println("ZANaviDebugReceiver:" + "mem1:" + Navit.logHeap_for_batch(Navit.Global_Navit_Object.getClass()));

									Thread.sleep(500);
									c4++;

									if (c4 > (2 * 60) * 40)
									{
										System.out.println("search test 004a");
										Navit.search_ready = true;
										no_result = true;
									}
								}
								// -- wait for search result --

								Thread.sleep(1500);

								System.out.println("search test 005");
								c4 = 0;
								if (no_result == false)
								{
									while (Navit.search_list_ready == false)
									{
										System.out.println("search test 004b:" + c4);
										System.out.println("ZANaviDebugReceiver:" + "mem1:" + Navit.logHeap_for_batch(Navit.Global_Navit_Object.getClass()));

										Thread.sleep(500);
										c4++;

										if (c4 > (2 * 60) * 2)
										{
											System.out.println("search test 004ab");
											break;
										}
									}
								}
								System.out.println("search test 006");

								Thread.sleep(5000);

								// save screenshot
								System.out.println("SCREENSHOT 001");
								try
								{
									File f = new File(file_name_global);
									File d2 = new File(f.getParent() + "/" + date + "/");
									d2.mkdirs();
									Navit.take_phone_screenshot(NavitAddressResultListActivity.NavitAddressResultListActivity_s, f.getParent() + "/" + date + "/", f.getName());
								}
								catch (Exception ee4)
								{
									System.out.println("SCREENSHOT:error1:" + Navit.stacktrace_to_string(ee4));
									ee4.printStackTrace();
								}
								System.out.println("SCREENSHOT 002");

								try
								{
									NavitAddressResultListActivity.force_done();
								}
								catch (Exception ee4)
								{
									System.out.println("SCREENSHOT:error2:" + Navit.stacktrace_to_string(ee4));
									ee4.printStackTrace();
								}

								System.out.println("SCREENSHOT 003");

								wait = 0;
								// -------- ST N --------
								// -------- ST N --------
								// -------- ST N --------

								FileOutputStream outf = null;
								try
								{
									File f = new File(file_name_global);
									outf = new FileOutputStream(f.getParent() + "/" + date + "/" + f.getName() + ".result.txt");
								}
								catch (Exception ef)
								{
									//System.out.println("EE002:" + ef.getMessage());
									System.out.println("_DREX_:003" + ef.getMessage());
								}

								OutputStreamWriter out = null;
								try
								{
									out = new OutputStreamWriter(outf);
								}
								catch (Exception e)
								{
									// System.out.println("EE003:" + e.getMessage());
									System.out.println("_DREX_:004" + e.getMessage());
								}

								//System.out.println("Roadbook:length=" + (separated.length - 2));
								try
								{
									out.write("Search:length=" + Navit.NavitAddressResultList_foundItems.size() + "\n");
								}
								catch (Exception e)
								{
									//System.out.println("EE004:" + e.getMessage());
									System.out.println("_DREX_:005" + e.getMessage());
								}

								try
								{
									int i = 0;
									for (i = 0; i < Navit.NavitAddressResultList_foundItems.size(); i++)
									{
										out.write("result:" + i + ":" + Navit.NavitAddressResultList_foundItems.get(i).result_type + ":" + Navit.NavitAddressResultList_foundItems.get(i).lat + ":" + Navit.NavitAddressResultList_foundItems.get(i).lon + ":" + Navit.NavitAddressResultList_foundItems.get(i).addr + "\n");
									}
								}
								catch (Exception e)
								{
									//System.out.println("EE004:" + e.getMessage());
									System.out.println("_DREX_:005a" + e.getMessage());
								}

								try
								{
									out.write("URL1:" + "http://nominatim.openstreetmap.org/search.php?q=" + URLEncoder.encode(city_str_f, "UTF-8") + "+" + URLEncoder.encode(str_str_f, "UTF-8") + "+" + URLEncoder.encode(hn_str_f, "UTF-8") + "\n");
									out.write("URL2:" + "https://www.google.at/maps/place/" + URLEncoder.encode(city_str_f, "UTF-8") + "+" + URLEncoder.encode(str_str_f, "UTF-8") + "+" + URLEncoder.encode(hn_str_f, "UTF-8") + "\n");
									// out.write("URL3:" + "about:\n");
								}
								catch (Exception e)
								{
									System.out.println("_DREX_:046" + e.getMessage());
								}

								try
								{
									out.flush();
									out.close();
									outf.flush();
									outf.close();
								}
								catch (Exception e)
								{
									System.out.println("_DREX_:016" + e.getMessage());
								}

								// calculate success criterion ----------------------
								// calculate success criterion ----------------------

								result_code = -1;

								if ((!success_operator.equals("")) && (!success_value.equals("")))
								{
									// System.out.println("search:so=" + success_source);
									System.out.println("search:it=" + success_item);
									System.out.println("search:sv=" + success_value);

									if (success_item.equalsIgnoreCase("'resultcount'"))
									{
										try
										{
											int s = Navit.NavitAddressResultList_foundItems.size();
											int v = Integer.parseInt(success_value);
											result_code = success_value_compare(s, v);
										}
										catch (Exception e)
										{
											result_code = -1;
										}
									}
								}

								// dummy for circleCI
								File f3 = new File(file_name_global);
								System.out.println("ZANaviDebugReceiver:" + "file=" + (f3.getParent() + "/" + date + "/" + f3.getName()));
								// dummy for circleCI

								if (result_code == 0)
								{
									File f = new File(file_name_global);
									String orig = f.getParent() + "/" + date + "/" + f.getName() + ".result.txt";
									String rename_to = f.getParent() + "/" + date + "/" + f.getName() + "._SUCCESS_.result.txt";
									File f2 = new File(orig);
									File f2_to = new File(rename_to);
									f2.renameTo(f2_to);

									yaml_err--;
								}
								else
								{
								}

								// calculate success criterion ----------------------
								// calculate success criterion ----------------------

							}
							catch (Exception ebig)
							{
								System.out.println("_DREX_:033" + Navit.stacktrace_to_string(ebig));
							}
						}
					}
				};
				search_n_002.start();
				search_n_002.join();
				Thread.sleep(500);

				is_replaying = false;
				NavitGraphics.NavitAOverlay_s.postInvalidate();
				br.close();
			}
		}
		catch (Exception e)
		{
			System.out.println("_DREX_:011" + e.getMessage());
			is_replaying = false;
			NavitGraphics.NavitAOverlay_s.postInvalidate();
		}
	}

	static void DR_replay_yaml_file_search_i(String filename, final String date)
	{

		file_name_global = filename;

		yaml_err++; // incr. error count (on success later -> decrease it again)

		try
		{
			if ((filename != null) && (!filename.equals("")))
			{
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(filename));

				is_replaying = true;
				NavitGraphics.NavitAOverlay_s.postInvalidate();

				disable_normal_location();

				DR_clear_route();
				Thread.sleep(1000);

				String str_str = "";
				String city_str = "";
				String hn_str = "";

				String mode = "-";
				String line = "";
				while ((line = br.readLine()) != null)
				{
					if ((line.length() >= "type:".length()) && (line.equals("type:")))
					{
						mode = "type";
					}
					else if ((line.length() >= "input:".length()) && (line.equals("input:")))
					{
						mode = "input";
					}
					else if ((line.length() >= "success:".length()) && (line.equals("success:")))
					{
						mode = "success";
					}
					else if ((line.length() > 2) && (!line.startsWith("#")))
					{
						try
						{
							if (mode == "success")
							{
								String name_str = line.split(":", 2)[0].replace(" ", "");
								String value_str = line.split(":", 2)[1].replace(" ", "");

								if (name_str.equalsIgnoreCase("item"))
								{
									success_item = value_str;
								}
								else if (name_str.equalsIgnoreCase("value"))
								{
									success_value = value_str;
								}
								else if (name_str.equalsIgnoreCase("operator"))
								{
									success_operator = value_str;
								}
							}
							else if (mode == "type")
							{
								// do nothing
							}
							else
							{
								String name_str = line.split(":", 2)[0];
								String value_str = line.split(":", 2)[1];

								if (name_str.contains("street"))
								{
									str_str = value_str;
								}
								else if (name_str.contains("city"))
								{
									city_str = value_str;
								}
								else if (name_str.contains("housenumber"))
								{
									hn_str = value_str;
								}
							}
						}
						catch (Exception e2)
						{
							System.out.println("_DREX_:001" + e2.getMessage());
						}
					}
				}

				try
				{
					int jj = 0;
					while ((NavitGraphics.navit_route_status != 0) && (jj < 120))
					{
						jj++;
						System.out.println("ZANaviDebugReceiver:" + "waiting for route to clear (status=" + NavitGraphics.navit_route_status + "):" + jj);
						// wait for old route to be cleared
						Thread.sleep(1000);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				final String str_str_f = str_str;
				final String city_str_f = city_str;
				final String hn_str_f = hn_str;

				System.out.println("ZANaviDebugReceiver:" + "mem0:" + Navit.logHeap_for_batch(Navit.Global_Navit_Object.getClass()));

				final Thread search_n_001 = new Thread()
				{
					int wait = 1;

					@Override
					public void run()
					{
						while (wait == 1)
						{
							try
							{

								// -------- ST I --------
								// -------- ST I --------
								// -------- ST I --------

								// clear results
								Navit.NavitAddressResultList_foundItems.clear();
								Navit.Navit_Address_Result_double_index.clear();
								Navit.NavitSearchresultBarIndex = -1;
								Navit.NavitSearchresultBar_title = "";
								Navit.NavitSearchresultBar_text = "";
								Navit.search_results_towns = 0;
								Navit.search_results_streets = 0;
								Navit.search_results_streets_hn = 0;
								Navit.search_results_poi = 0;

								final Thread tttt2 = new Thread()
								{
									@Override
									public void run()
									{
										System.out.println("search test 001");
										Navit.use_index_search = true;
										Navit.executeSearch_with_values(str_str_f, city_str_f, hn_str_f, true, true, true, true);
									}
								};
								tttt2.start();
								System.out.println("search test 002");
								Thread.sleep(2000);
								System.out.println("search test 003");

								// -- wait for search result --
								int c4 = 0;
								boolean no_result = false;
								Navit.search_list_ready = false;
								while (Navit.search_ready == false)
								{
									System.out.println("search test 004:" + c4);
									System.out.println("ZANaviDebugReceiver:" + "mem1:" + Navit.logHeap_for_batch(Navit.Global_Navit_Object.getClass()));

									Thread.sleep(500);
									c4++;

									if (c4 > (2 * 60) * 40)
									{
										System.out.println("search test 004a");
										Navit.search_ready = true;
										no_result = true;
									}
								}
								// -- wait for search result --

								System.out.println("search test 005");

								Thread.sleep(6000);

								Navit.runOnUI(new Runnable()
								{
									@Override
									public void run()
									{
										try
										{
											System.out.println("hide keyboard-1");
											// now hide the keyboard
											InputMethodManager inputManager = (InputMethodManager) Navit.Global_Navit_Object.getSystemService(Context.INPUT_METHOD_SERVICE);
											inputManager.hideSoftInputFromWindow(Navit.Global_Navit_Object.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
											inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
											System.out.println("hide keyboard-2");
										}
										catch (Exception e)
										{
											e.printStackTrace();
											System.out.println("hide keyboard-3");
										}
									}
								});

								System.out.println("search test 005");
								Thread.sleep(1500);

								// save screenshot
								System.out.println("SCREENSHOT 001");
								try
								{
									File f = new File(file_name_global);
									File d2 = new File(f.getParent() + "/" + date + "/");
									d2.mkdirs();
									Navit.take_phone_screenshot(NavitAddressSearchActivity.NavitAddressSearchActivity_s, f.getParent() + "/" + date + "/", f.getName());
								}
								catch (Exception ee4)
								{
									System.out.println("SCREENSHOT:error1:" + Navit.stacktrace_to_string(ee4));
									ee4.printStackTrace();
								}
								System.out.println("SCREENSHOT 002");

								try
								{
									NavitAddressSearchActivity.force_done();
								}
								catch (Exception ee4)
								{
									System.out.println("SCREENSHOT:error2:" + Navit.stacktrace_to_string(ee4));
									ee4.printStackTrace();
								}

								System.out.println("SCREENSHOT 003");

								wait = 0;
								// -------- ST I --------
								// -------- ST I --------
								// -------- ST I --------

								FileOutputStream outf = null;
								try
								{
									File f = new File(file_name_global);
									outf = new FileOutputStream(f.getParent() + "/" + date + "/" + f.getName() + ".result.txt");
								}
								catch (Exception ef)
								{
									//System.out.println("EE002:" + ef.getMessage());
									System.out.println("_DREX_:003" + ef.getMessage());
								}

								OutputStreamWriter out = null;
								try
								{
									out = new OutputStreamWriter(outf);
								}
								catch (Exception e)
								{
									// System.out.println("EE003:" + e.getMessage());
									System.out.println("_DREX_:004" + e.getMessage());
								}

								//System.out.println("Roadbook:length=" + (separated.length - 2));
								try
								{
									out.write("Search:length=" + Navit.NavitAddressResultList_foundItems.size() + "\n");
								}
								catch (Exception e)
								{
									//System.out.println("EE004:" + e.getMessage());
									System.out.println("_DREX_:005" + e.getMessage());
								}

								try
								{
									int i = 0;
									for (i = 0; i < Navit.NavitAddressResultList_foundItems.size(); i++)
									{
										out.write("result:" + i + ":" + Navit.NavitAddressResultList_foundItems.get(i).result_type + ":" + Navit.NavitAddressResultList_foundItems.get(i).lat + ":" + Navit.NavitAddressResultList_foundItems.get(i).lon + ":" + Navit.NavitAddressResultList_foundItems.get(i).addr + "\n");
									}
								}
								catch (Exception e)
								{
									//System.out.println("EE004:" + e.getMessage());
									System.out.println("_DREX_:005a" + e.getMessage());
								}

								try
								{
									// http://nominatim.openstreetmap.org/search.php?q=wienerneustadt+blubgasse+99
									out.write("URL1:" + "http://nominatim.openstreetmap.org/search.php?q=" + URLEncoder.encode(city_str_f, "UTF-8") + "+" + URLEncoder.encode(str_str_f, "UTF-8") + "+" + URLEncoder.encode(hn_str_f, "UTF-8") + "\n");
									out.write("URL2:" + "https://www.google.at/maps/place/" + URLEncoder.encode(city_str_f, "UTF-8") + "+" + URLEncoder.encode(str_str_f, "UTF-8") + "+" + URLEncoder.encode(hn_str_f, "UTF-8") + "\n");
									// out.write("URL3:" + "\n");
								}
								catch (Exception e)
								{
									System.out.println("_DREX_:046" + e.getMessage());
								}

								try
								{
									out.flush();
									out.close();
									outf.flush();
									outf.close();
								}
								catch (Exception e)
								{
									System.out.println("_DREX_:016" + e.getMessage());
								}

								// calculate success criterion ----------------------
								// calculate success criterion ----------------------

								result_code = -1;

								if ((!success_operator.equals("")) && (!success_value.equals("")))
								{
									// System.out.println("search:so=" + success_source);
									System.out.println("search:it=" + success_item);
									System.out.println("search:sv=" + success_value);

									if (success_item.equalsIgnoreCase("'resultcount'"))
									{
										try
										{
											int s = Navit.NavitAddressResultList_foundItems.size();
											int v = Integer.parseInt(success_value);
											result_code = success_value_compare(s, v);
										}
										catch (Exception e)
										{
											result_code = -1;
										}
									}
								}

								// dummy for circleCI
								File f3 = new File(file_name_global);
								System.out.println("ZANaviDebugReceiver:" + "file=" + (f3.getParent() + "/" + date + "/" + f3.getName()));
								// dummy for circleCI

								if (result_code == 0)
								{
									File f = new File(file_name_global);
									String orig = f.getParent() + "/" + date + "/" + f.getName() + ".result.txt";
									String rename_to = f.getParent() + "/" + date + "/" + f.getName() + "._SUCCESS_.result.txt";
									File f2 = new File(orig);
									File f2_to = new File(rename_to);
									f2.renameTo(f2_to);

									yaml_err--;
								}
								else
								{
								}

								// calculate success criterion ----------------------
								// calculate success criterion ----------------------

							}
							catch (Exception ebig)
							{
								System.out.println("_DREX_:033" + Navit.stacktrace_to_string(ebig));
							}
						}
					}
				};
				search_n_001.start();
				search_n_001.join();
				Thread.sleep(500);

				is_replaying = false;
				NavitGraphics.NavitAOverlay_s.postInvalidate();
				br.close();
			}
		}
		catch (Exception e)
		{
			System.out.println("_DREX_:011" + e.getMessage());
			is_replaying = false;
			NavitGraphics.NavitAOverlay_s.postInvalidate();
		}
	}

	static int success_value_compare(int s, int v)
	{
		if (success_operator.equalsIgnoreCase("'=='"))
		{
			if (s == v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'<>'"))
		{
			if (s != v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'!='"))
		{
			if (s != v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'<'"))
		{
			if (s < v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'<='"))
		{
			if (s <= v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'=<'"))
		{
			if (s <= v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'>'"))
		{
			if (s > v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'>='"))
		{
			if (s >= v)
			{
				return 0;
			}
		}
		else if (success_operator.equalsIgnoreCase("'=>'"))
		{
			if (s >= v)
			{
				return 0;
			}
		}

		return -1;
	}

	static void DR_run_all_yaml_tests()
	{
		String yaml_dir = Navit.NAVIT_DATA_DEBUG_DIR + "../yamltests/";
		String duration_string = "";

		try
		{
			File dir = new File(yaml_dir);
			try
			{
				dir.mkdirs();
			}
			catch (Exception e2)
			{
				// System.out.println("XXXX:E01" + e2.getMessage());
				System.out.println("_DREX_:012" + e2.getMessage());
			}

			Navit.static_show_route_graph(4);

			String date_str = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());

			FilenameFilter textFilter = new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					String lowercaseName = name.toLowerCase(Locale.GERMAN);
					if (lowercaseName.endsWith(".yaml"))
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			};

			System.out.println("XXXX:1:" + dir);

			yaml_sum = 0;
			yaml_err = 0;
			BufferedReader br = null;
			String line2 = "";

			File[] directoryListing = dir.listFiles(textFilter);
			if (directoryListing != null)
			{
				for (File yamlfile : directoryListing)
				{
					yaml_sum++;

					final Thread temp_work_thread = new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								//if (Navit.CIDEBUG > 0) // automatic debug run
								//{
								//	Navit.show_geo_on_screen_no_draw(0, 0);
								//	Thread.sleep(200);
								//	Navit.set_zoom_level_no_draw((int) Math.pow(2, 5)); // zoom level: (18 - 5) = 13
								//	Thread.sleep(200);
								//}
								//else
								//{
								Navit.show_geo_on_screen_no_draw(0, 0);
								Thread.sleep(150);
								Navit.set_zoom_level_no_draw((int) Math.pow(2, 17));
								Thread.sleep(120);
								//}
							}
							catch (Exception e2)
							{
								System.out.println("_DREX_:013" + e2.getMessage());
								e2.printStackTrace();
							}
						}
					};
					temp_work_thread.start();
					temp_work_thread.join();

					System.out.println("XXXX:2:1:" + "");

					Navit.CI_TEST_CASE_NUM++;
					Navit.CI_TEST_CASE_TEXT = Navit.CI_TEST_CASE_TEXT + "\n" + "===========" + Navit.CI_TEST_CASE_NUM + "===========" + "\n";
					try
					{
						NavitGraphics.CallbackMessageChannelReal(114, "" + Navit.CI_TEST_CASE_NUM);
					}
					catch (Exception e)
					{
					}

					System.out.println("XXXX:2:2:" + "");
					System.out.println("XXXX:2:3:" + "======= START =======");
					long startTime = System.currentTimeMillis();
					System.out.println("XXXX:2:4:" + yamlfile.getAbsolutePath());

					br = null;
					line2 = "";
					try
					{
						br = new BufferedReader(new FileReader(yamlfile.getAbsolutePath()));
						line2 = br.readLine();
					}
					catch (Exception e66)
					{
					}

					if ((line2.length() >= "type:".length()) && (line2.equals("type: 'NS'")))
					{
						DR_replay_yaml_file_search_n(yamlfile.getAbsolutePath(), date_str);
					}
					else if ((line2.length() >= "type:".length()) && (line2.equals("type: 'IS'")))
					{
						DR_replay_yaml_file_search_i(yamlfile.getAbsolutePath(), date_str);
					}
					else
					{
						DR_replay_gps_file(yamlfile.getAbsolutePath(), date_str);
					}

					System.out.println("XXXX:2:5:" + yamlfile.getAbsolutePath());
					float difference = (float) (System.currentTimeMillis() - startTime);
					duration_string = duration_string + yamlfile.getName() + ":" + (difference / 1000.0f) + "\n";
					System.out.println("XXXX:2:6:" + "=======  END  ======= duration: " + (difference / 1000.0f) + " secs.");
					System.out.println("XXXX:2:7:" + "");
					Navit.CI_TEST_CASE_TEXT = "";
					System.out.println("XXXX:2:8:" + "");

					//xy//Thread.sleep(1500);

				}
			}
		}
		catch (Exception e)
		{
			// System.out.println("XXXX:E02" + e.getMessage());
			System.out.println("_DREX_:019" + e.getMessage());
		}

		try
		{
			FileOutputStream outf = null;
			OutputStreamWriter out = null;
			outf = new FileOutputStream(yaml_dir + "/" + "_XX_XX_DURATION_XX_XX_.txt");
			out = new OutputStreamWriter(outf);
			out.write(duration_string);
			out.flush();
			out.close();
		}
		catch (Exception ef2)
		{
			System.out.println("_DREX_:034" + ef2.getMessage());
		}

		try
		{
			FileOutputStream outf = null;
			OutputStreamWriter out = null;
			outf = new FileOutputStream(yaml_dir + "/" + "_XX_XX_SUMMARY_XX_XX_.txt");
			out = new OutputStreamWriter(outf);
			out.write("tests:" + yaml_sum + "\n");
			out.write("errors:" + yaml_err + "\n");
			if (yaml_err > 0)
			{
				out.write("RES:" + "ERR" + "\n");
			}
			else
			{
				out.write("RES:" + "OK" + "\n");
			}
			out.flush();
			out.close();
		}
		catch (Exception ef2)
		{
			System.out.println("_DREX_:014" + ef2.getMessage());
			// System.out.println("EE00SUM:" + ef2.getMessage());
		}

		DR_clear_route();

		// restore normal state ----------------------
		Navit.static_show_route_graph(3);
		enable_normal_location();
		// restore normal state ----------------------
	}

	static void DR_replay_gps_file(String filename, String date_str)
	{

		System.out.println("ZANaviDebugReceiver:" + "Enter!!");

		stop_me = false;
		dont_save_loc = true;

		try
		{
			if ((filename != null) && (!filename.equals("")))
			{

				if (filename.endsWith("yaml"))
				{
					DR_replay_yaml_file(filename, date_str);
					return;
				}

				BufferedReader br = null;
				br = new BufferedReader(new FileReader(filename));

				Bundle extras2;

				String line = "";
				String[] line_parts;

				skip_count = skip;
				is_replaying = true;
				NavitGraphics.NavitAOverlay_s.postInvalidate();

				long line_number = 0;

				disable_normal_location();

				while ((line = br.readLine()) != null)
				{
					//if (line.length() > 1)
					//{
					//	System.out.println("ZANaviDebugReceiver:" + "line=" + line);
					//}

					line_number++;

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

						is_replaying = false;
						NavitGraphics.NavitAOverlay_s.postInvalidate();
						return;
					}

					if ((rewind_count > 0) && (skip_count == skip))
					{
						try
						{
							// seek to start of file and skip lines
							br.close();
							br = new BufferedReader(new FileReader(filename));

							line_number = line_number - rewind_count;
							rewind_count = 0;
							int l_new = 0;

							while (line_number > 0)
							{
								// just skip lines
								line = br.readLine();
								// System.out.println("ZANaviDebugReceiver:" + "skip line:" + line + " l=" + l_new);
								line_number--;
								l_new++;
							}

							line_number = l_new;
						}
						catch (Exception ce)
						{
							ce.printStackTrace();
						}
					}

					if (line.length() > 3)
					{
						extras2 = new Bundle();
						line_parts = line.split(":", 2);

						if (line_parts[0].equals("CLR"))
						{
							if (skip_count >= skip)
							{
								DR_clear_route();
								Thread.sleep(200);
							}
							else
							{
								skip_count++;
							}
						}
						else if (line_parts[0].equals("DST"))
						{
							if (skip_count >= skip)
							{
								extras2.putString("add_destination", line_parts[1]);
								DR_add_destination("add_destination", extras2);
								Thread.sleep(5000); // wait 5 seconds to calc route
							}
							else
							{
								skip_count++;
							}
						}
						else if (line_parts[0].equals("POS"))
						{
							while (Navit.follow_current == Navit.follow_off)
							{
								try
								{
									Thread.sleep(600);
								}
								catch (Exception sl1)
								{
								}
							}

							if (skip_count >= skip)
							{
								extras2.putString("set_position", line_parts[1]);
								DR_set_position("set_position", extras2, false);
								Thread.sleep(950); // about 1 second between locationpoints
							}
							else
							{
								skip_count++;
							}
						}
						else
						{
						}

					}
				}

				is_replaying = false;
				System.out.println("ZANaviDebugReceiver:" + "while loop end");
				br.close();
			}

		}
		catch (Exception e)
		{
			System.out.println("_DREX_:015" + e.getMessage());
			e.printStackTrace();
			System.out.println("ZANaviDebugReceiver:" + "EX:" + e.getMessage());
		}

		is_replaying = false;
		dont_save_loc = true;

		NavitGraphics.NavitAOverlay_s.postInvalidate();

		System.out.println("ZANaviDebugReceiver:" + "Leave!!");
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (Navit.p.PREF_enable_debug_enable_comm)
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
							NavitGraphics.callback_handler.sendMessage(msg);

							break;
						}
						else if (key.equals("set_pos_and_dest"))
						{
							Object value = extras.get(key);
							String value2 = value.toString().replaceAll("\"", "");
							// String route_name = value2.split(" ", 5)[0];
							float lat1 = Float.parseFloat(value2.split(" ", 5)[2]);
							float lon1 = Float.parseFloat(value2.split(" ", 5)[1]);
							float lat2 = Float.parseFloat(value2.split(" ", 5)[4]);
							float lon2 = Float.parseFloat(value2.split(" ", 5)[3]);

							System.out.println("ZANaviDebugReceiver:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));

							DR_clear_route();
							Thread.sleep(200);
							extras = new Bundle();
							extras.putString("set_position", "" + lat1 + "," + lon1 + "," + "0.0" + "," + "0");
							DR_set_position("set_position", extras, true);
							Thread.sleep(1000);
							extras = new Bundle();
							extras.putString("add_destination", "" + lat2 + "," + lon2);
							DR_add_destination("add_destination", extras);

							final Thread debug_zoom_to_route_001a = new Thread()
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
												NavitGraphics.callback_handler.sendMessage(msg);

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
							debug_zoom_to_route_001a.start();

							break;
						}
						else if (key.equals("set_position"))
						{
							DR_set_position(key, extras, true);
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
												NavitGraphics.callback_handler.sendMessage(msg);

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
