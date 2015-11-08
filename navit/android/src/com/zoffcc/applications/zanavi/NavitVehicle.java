/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2014 Zoff <zoff@zoff.cc>
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

public class NavitVehicle
{
	private LocationManager locationManager = null;
	private static LocationManager locationManager_s = null;
	private String preciseProvider = null;
	private String fastProvider = null;
	private static LocationListener fastLocationListener_s = null;
	private static LocationListener preciseLocationListener_s = null;
	private static GpsStatus.Listener gps_status_listener_s = null;
	private static float compass_heading;
	private static float current_accuracy = 99999999F;
	private static long last_real_gps_update = -1;
	static boolean sat_status_enabled = false;
	static boolean sat_status_icon_updated = false;
	static int sat_status_icon_last = -1;
	static int sat_status_icon_now = -1;
	static float gps_last_bearing = 0.0f;
	static double gps_last_lat = 0.0d;
	static double gps_last_lon = 0.0d;
	static int gps_last_lat_1000 = 0;
	static int gps_last_lon_1000 = 0;
	static int fast_provider_status = 0;
	static int disregard_first_fast_location = 0;
	static String[] cmd_name = new String[4];

	static long MILLIS_AFTER_GPS_FIX_IS_LOST = 2000;

	static TunnelExtrapolationThread te_thread = null;

	static boolean is_pos_recording = false;
	static File pos_recording_file;
	// static File pos_recording_file_gpx;
	static File speech_recording_file_gpx;
	static BufferedWriter pos_recording_writer;
	// static BufferedWriter pos_recording_writer_gpx;
	static BufferedWriter speech_recording_writer_gpx;
	static boolean speech_recording_started = false;
	// DateFormat sdf = new DateFormat();

	int sats1_old = -1;
	int satsInFix1_old = -1;

	public static Handler vehicle_handler_ = null;
	public static long lastcompass_update_timestamp = 0L;

	public static final float GPS_SPEED_ABOVE_USE_FOR_HEADING = (float) (9 / 3.6f); //  (9 km/h) / (3.6) ~= m/s

	private static String preciseProvider_s = null;
	static String fastProvider_s = null;

	public static long last_p_fix = 0;
	public static long last_f_fix = 0;
	public Bundle gps_extras = null;
	public static GpsStatus gps_status = null;
	public static Boolean update_location_in_progress = false;

	static long last_gps_status_update = 0L;

	static DecimalFormat df2 = new DecimalFormat("#.####");

	public static Location last_location = null;

	public static native void VehicleCallback(double lat, double lon, float speed, float direction, double height, float radius, long gpstime);

	public static void VehicleCallback2(Location location)
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);
		String dd_text = "";

		if (Navit.Global_Init_Finished != 0)
		{
			if (Navit.Global_Location_update_not_allowed == 0)
			{
				if (NavitGraphics.DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:Gps");

				// change bearing/direction to last good bearing -------------------
				// change bearing/direction to last good bearing -------------------
				// change bearing/direction to last good bearing -------------------
				if ((location.getSpeed() < 5f) && (location.getBearing() == 0.0f) && (gps_last_bearing != 0.0f))
				{
					//if ((gps_last_lat_1000 == (int) (location.getLatitude() * 1000)) && (gps_last_lon_1000 == (int) (location.getLongitude() * 1000)))
					//{
					dd_text = dd_text + "a:";
					location.setBearing(gps_last_bearing);
					//}
				}

				if (location.getBearing() != 0.0f)
				{
					dd_text = dd_text + "n:";
					gps_last_bearing = location.getBearing();
				}
				// change bearing/direction to last good bearing -------------------
				// change bearing/direction to last good bearing -------------------
				// change bearing/direction to last good bearing -------------------

				if (Navit.p.PREF_enable_debug_write_gpx)
				{
					pos_recording_add(1, location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getBearing(), location.getTime());
				}

				if (Navit.NAVIT_DEBUG_TEXT_VIEW) ZANaviOSDDebug01.add_text(dd_text + "b=" + location.getBearing() + " lb=" + gps_last_bearing);

				Navit.cwthr.VehicleCallback3(location);
				gps_last_lat = location.getLatitude();
				gps_last_lon = location.getLongitude();
				gps_last_lat_1000 = (int) (gps_last_lat * 1000);
				gps_last_lon_1000 = (int) (gps_last_lon * 1000);
			}
		}
		else
		{
			System.out.println("VehicleCallback2:Global_Init_Finished == 0 !!!!!!!");
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);

	}

	// private static SatStatusThread st = null;

	private class SatStatusThread extends Thread
	{
		// get new gpsstatus --------
		int sats1 = 0;
		int satsInFix1 = 0;
		Boolean running = true;

		public void run()
		{
			this.running = true;

			while (this.running)
			{
				try
				{
					if (!Navit.DemoVehicle)
					{
						GpsStatus stat = locationManager.getGpsStatus(gps_status);
						gps_status = stat;
						Iterator<GpsSatellite> localIterator = stat.getSatellites().iterator();
						while (localIterator.hasNext())
						{
							GpsSatellite localGpsSatellite = (GpsSatellite) localIterator.next();
							sats1++;
							if (localGpsSatellite.usedInFix())
							{
								satsInFix1++;
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				// System.out.println("Statellites (Thread): " + satsInFix1 + "/" + sats1);
				// Navit.set_debug_messages3_wrapper("sat: " + satsInFix1 + "/" + sats1);
				// get new gpsstatus --------

				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		public void stop_me()
		{
			this.running = false;
		}
	}

	NavitVehicle(Context context)
	{
		// ---------------------
		// ---------------------
		// ------- DEBUG: test #ifdef equivalent --------
		// ---------------------
		//		final long ccccc = 2000000000L; // 2.000.000.000 (2 billion) iterations!
		//		// ---------------------
		//		final boolean flag1 = true;
		//		final boolean flag2 = false;
		//		// ---------------------
		//		long aa1 = System.currentTimeMillis();
		//		long j = 0;
		//		while (j < ccccc)
		//		{
		//			if (flag1)
		//			{
		//				if (flag2)
		//				{
		//					j++;
		//				}
		//				else
		//				{
		//					j++;
		//				}
		//			}
		//		}
		//		long aa2 = System.currentTimeMillis();
		//		System.out.println("NVVVVV:3a:" + ((float) (aa2 - aa1) / 1000f));
		//		// ---------------------
		//		boolean flag1b = true;
		//		boolean flag2b = false;
		//		// ---------------------
		//		aa1 = System.currentTimeMillis();
		//		j = 0;
		//		while (j < ccccc)
		//		{
		//			if (flag1b)
		//			{
		//				if (flag2b)
		//				{
		//					j++;
		//				}
		//				else
		//				{
		//					j++;
		//				}
		//			}
		//		}
		//		aa2 = System.currentTimeMillis();
		//		System.out.println("NVVVVV:3b:" + ((float) (aa2 - aa1) / 1000f));
		//		// ---------------------
		//		// ---------------------
		//		aa1 = System.currentTimeMillis();
		//		j = 0;
		//		while (j < ccccc)
		//		{
		//			j++;
		//		}
		//		aa2 = System.currentTimeMillis();
		//		System.out.println("NVVVVV:3c:" + ((float) (aa2 - aa1) / 1000f));
		// ---------------------
		// ---------------------
		// ------- DEBUG test #ifdef equivalent --------
		// ---------------------
		// ---------------------

		vehicle_handler_ = Navit.vehicle_handler;

		cmd_name[0] = "-";
		cmd_name[1] = "POS";
		cmd_name[2] = "CLR";
		cmd_name[3] = "DST";

		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager_s = locationManager;

		LocationListener fastLocationListener = new LocationListener()
		{
			public void onLocationChanged(Location location)
			{
				if (disregard_first_fast_location > 0)
				{
					Log.e("NavitVehicle", "LocationChanged provider=fast" + " disregard_first_fast_location=" + disregard_first_fast_location);

					disregard_first_fast_location--;
					return;
				}

				last_f_fix = location.getTime();
				// if last gps fix was longer than 8 secs. ago, use this fix
				// and we dont have a GPS lock
				if ((last_p_fix + 8000 < last_f_fix) && (Navit.satsInFix < 3))
				{
					if (Navit.p.PREF_follow_gps)
					{
						if (Navit.p.PREF_use_compass_heading_base)
						{
							if ((Navit.p.PREF_use_compass_heading_always) || (location.getSpeed() < GPS_SPEED_ABOVE_USE_FOR_HEADING))
							{
								// use compass heading
								location.setBearing(compass_heading);
							}
						}

						//System.out.println("send values 1");
						// Log.e("NavitVehicle", "LocationChanged provider=fast Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
						last_location = location;
						if (!Navit.DemoVehicle)
						{
							//Log.e("NavitVehicle", "call VehicleCallback 001");
							VehicleCallback2(location);
						}
					}
				}
			}

			public void onProviderDisabled(String provider)
			{
				//Log.e("NavitVehicle", "onProviderDisabled -> provider=" + provider);
			}

			public void onProviderEnabled(String provider)
			{
				//Log.e("NavitVehicle", "onProviderEnabled -> provider=" + provider);
			}

			public void onStatusChanged(String provider, int status, Bundle extras)
			{
				//Log.e("NavitVehicle", "onStatusChanged -> provider=" + provider + " status=" + status);
				try
				{
					switch (status)
					{
					case LocationProvider.OUT_OF_SERVICE:
						System.out.println("*** No Service ***");
						break;
					case LocationProvider.TEMPORARILY_UNAVAILABLE:
						System.out.println("*** No Fix ***");
						break;
					case LocationProvider.AVAILABLE:
						System.out.println("@@@  Fix   @@@");
						break;
					}
				}
				catch (Exception e)
				{

				}
			}
		};
		fastLocationListener_s = fastLocationListener;

		LocationListener preciseLocationListener = new LocationListener()
		{
			public void onLocationChanged(Location location)
			{
				last_p_fix = location.getTime();
				current_accuracy = location.getAccuracy();

				if (location != null)
				{
					Navit.mLastLocationMillis = SystemClock.elapsedRealtime();
					Navit.mLastLocation = location;
				}

				if (Navit.p.PREF_follow_gps)
				{
					if (Navit.p.PREF_use_compass_heading_base)
					{
						if ((Navit.p.PREF_use_compass_heading_always) || (location.getSpeed() < GPS_SPEED_ABOVE_USE_FOR_HEADING))
						{
							// use compass heading
							location.setBearing(compass_heading);
						}
					}
					//System.out.println("send values 2");
					//Log.e("NavitVehicle", "LocationChanged provider=precise Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
					last_location = location;
					//Log.e("NavitVehicle", "call VehicleCallback 002");
					if (NavitGraphics.DEBUG_SMOOTH_DRIVING)
					{
						if (last_real_gps_update > -1)
						{
							Log.e("NavitVehicle", "gps-gap:" + (System.currentTimeMillis() - last_real_gps_update));
						}
						last_real_gps_update = System.currentTimeMillis();
					}
					VehicleCallback2(location);
				}
			}

			public void onProviderDisabled(String provider)
			{
				//Log.e("NavitVehicle", "onProviderDisabled -> provider=" + provider);
			}

			public void onProviderEnabled(String provider)
			{
				//Log.e("NavitVehicle", "onProviderEnabled -> provider=" + provider);
			}

			public void onStatusChanged(String provider, int status, Bundle extras)
			{
				//Log.e("NavitVehicle", "onStatusChanged -> provider=" + provider + " status=" + status);

				try
				{
					if (status == GpsStatus.GPS_EVENT_FIRST_FIX)
					{
						System.out.println("*** GPS first fix ***");
					}
					switch (status)
					{
					case LocationProvider.OUT_OF_SERVICE:
						System.out.println("*** No Service ***");
						break;
					case LocationProvider.TEMPORARILY_UNAVAILABLE:
						System.out.println("*** No Fix ***");
						break;
					case LocationProvider.AVAILABLE:
						System.out.println("@@@  Fix   @@@");
						break;
					}
				}
				catch (Exception e)
				{

				}
			}
		};
		preciseLocationListener_s = preciseLocationListener;

		/*
		 * Use 2 LocationProviders, one precise (usually GPS), and one
		 * not so precise, but possible faster.
		 */
		Criteria highCriteria = null;
		Criteria lowCriteria = null;
		try
		{
			// Selection criterias for the precise provider
			highCriteria = new Criteria();
			highCriteria.setAccuracy(Criteria.ACCURACY_FINE);
			highCriteria.setAltitudeRequired(false);
			highCriteria.setBearingRequired(true);
			//highCriteria.setCostAllowed(true);
			//highCriteria.setPowerRequirement(Criteria.POWER_HIGH);

			// Selection criterias for the fast provider
			lowCriteria = new Criteria();
			lowCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
			lowCriteria.setAltitudeRequired(false);
			lowCriteria.setBearingRequired(false);
			lowCriteria.setCostAllowed(true);
			lowCriteria.setPowerRequirement(Criteria.POWER_LOW);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Log.e("NavitVehicle", "Providers " + locationManager.getAllProviders());
			//
			preciseProvider = locationManager.getBestProvider(highCriteria, false);
			preciseProvider_s = preciseProvider;
			Log.e("NavitVehicle", "Precise Provider " + preciseProvider);
			fastProvider = locationManager.getBestProvider(lowCriteria, false);
			fastProvider_s = fastProvider;
			Log.e("NavitVehicle", "Fast Provider " + fastProvider);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			//*in onresume()*// locationManager.requestLocationUpdates(preciseProvider, 0, 0, preciseLocationListener);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// If the 2 providers are the same, only activate one listener
			if (fastProvider == null || preciseProvider.compareTo(fastProvider) == 0)
			{
				fastProvider = null;
			}
			else
			{
				if (Navit.p.PREF_use_fast_provider)
				{
					//*in onresume()*//locationManager.requestLocationUpdates(fastProvider, 30000L, 8.0f, fastLocationListener); // (long)time [milliseconds], (float)minDistance [meters]
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		gps_status_listener_s = new GpsStatus.Listener()
		{
			public void onGpsStatusChanged(int event)
			{
				if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
				{

					boolean old_fix = Navit.isGPSFix;
					// ------------------------------
					// thanks to: http://stackoverflow.com/questions/2021176/how-can-i-check-the-current-status-of-the-gps-receiver
					// ------------------------------
					if (Navit.mLastLocation != null)
					{
						Navit.isGPSFix = (SystemClock.elapsedRealtime() - Navit.mLastLocationMillis) < MILLIS_AFTER_GPS_FIX_IS_LOST;
					}
					//					if (Navit.isGPSFix)
					//					{
					//						// A fix has been acquired.
					//					}
					//					else
					//					{
					//						// The fix has been lost.
					//					}
					if (old_fix != Navit.isGPSFix)
					{
						try
						{
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 102);
							if (Navit.isGPSFix)
							{
								b.putString("s", "1");
							}
							else
							{
								b.putString("s", "0");
							}
							msg.setData(b);
							NavitGraphics.callback_handler.sendMessage(msg);
						}
						catch (Exception e)
						{
						}

						if (Navit.p.PREF_show_sat_status)
						{
							// redraw NavitOSDJava
							// System.out.println("onDraw:show_sat_status:1");
							NavitGraphics.OSD_new.postInvalidate();
						}

						if (Navit.want_tunnel_extrapolation())
						{
							turn_on_tunnel_extrapolation();
						}
						else
						{
							turn_off_tunnel_extrapolation();
						}
					}

					// ------------------------------

					if (last_gps_status_update + 4000 < System.currentTimeMillis())
					{
						last_gps_status_update = System.currentTimeMillis();

						// get new gpsstatus --------

						sats1_old = Navit.sats;
						satsInFix1_old = Navit.satsInFix;

						GpsStatus stat = locationManager.getGpsStatus(null);

						Navit.sats = 0;
						Navit.satsInFix = 0;

						try
						{
							Iterator<GpsSatellite> localIterator = stat.getSatellites().iterator();
							while (localIterator.hasNext())
							{
								GpsSatellite localGpsSatellite = (GpsSatellite) localIterator.next();
								Navit.sats++;
								if (localGpsSatellite.usedInFix())
								{
									Navit.satsInFix++;
								}
							}

							// System.out.println("checking sat status update");

							if ((sats1_old != Navit.sats) || (satsInFix1_old != Navit.satsInFix))
							{
								//System.out.println("sat status update -> changed");
								if (Navit.p.PREF_show_sat_status)
								{
									// redraw NavitOSDJava
									// System.out.println("onDraw:show_sat_status:2");
									NavitGraphics.OSD_new.postInvalidate();
								}
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					// Navit.set_debug_messages3_wrapper("sat: " + Navit.satsInFix + "/" + Navit.sats);
					// System.out.println("Statellites: " + Navit.satsInFix + "/" + Navit.sats);
					// get new gpsstatus --------
				}
				else if (event == GpsStatus.GPS_EVENT_FIRST_FIX)
				{
					Navit.isGPSFix = true;
					turn_off_tunnel_extrapolation();

					try
					{
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 102);
						b.putString("s", "1");
						msg.setData(b);
						NavitGraphics.callback_handler.sendMessage(msg);
					}
					catch (Exception e)
					{
					}

					if (Navit.p.PREF_show_sat_status)
					{
						// redraw NavitOSDJava
						// System.out.println("onDraw:show_sat_status:3");
						NavitGraphics.OSD_new.postInvalidate();
					}
				}
				else if (event == GpsStatus.GPS_EVENT_STOPPED)
				{
					Navit.isGPSFix = false;

					if (Navit.want_tunnel_extrapolation())
					{
						turn_on_tunnel_extrapolation();
					}

					try
					{
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 102);
						b.putString("s", "0");
						msg.setData(b);
						NavitGraphics.callback_handler.sendMessage(msg);
					}
					catch (Exception e)
					{
					}

					if (Navit.p.PREF_show_sat_status)
					{
						// redraw NavitOSDJava
						// System.out.println("onDraw:show_sat_status:4");
						NavitGraphics.OSD_new.postInvalidate();
					}
				}
			}
		};

	}

	public static void set_mock_location__fast(Location mock_location)
	{
		float save_speed;

		try
		{
			//locationManager_s.setTestProviderLocation("ZANavi_mock", mock_location);
			// mock_location;
			// System.out.println("llllllll" + mock_location.getLatitude() + " " + mock_location.getLongitude());
			if (mock_location != null)
			{
				if (mock_location.getSpeed() == 0.0f)
				{
					if (last_location != null)
					{
						save_speed = last_location.getSpeed();
					}
					else
					{
						save_speed = 0.0f;
						last_location = mock_location;
					}
					mock_location.setSpeed(0.2f);
					//Log.e("NavitVehicle", "call VehicleCallback 003");
					VehicleCallback2(mock_location);
					mock_location.setSpeed(save_speed);
				}
				else
				{
					//Log.e("NavitVehicle", "call VehicleCallback 004");
					VehicleCallback2(mock_location);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_mock_location__fast_no_speed(Location mock_location)
	{
		try
		{
			//locationManager_s.setTestProviderLocation("ZANavi_mock", mock_location);
			// mock_location;
			// System.out.println("llllllll" + mock_location.getLatitude() + " " + mock_location.getLongitude());
			if (mock_location != null)
			{
				//Log.e("NavitVehicle", "call VehicleCallback 004");
				VehicleCallback2(mock_location);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class location_coords
	{
		double lat;
		double lon;
	}

	public static location_coords get_last_known_pos()
	{
		location_coords ret = new location_coords();

		try
		{
			Location l = locationManager_s.getLastKnownLocation(preciseProvider_s);
			if (l != null)
			{
				if (l.getAccuracy() > 0)
				{
					if ((l.getLatitude() != 0) && (l.getLongitude() != 0))
					{
						ret.lat = l.getLatitude();
						ret.lon = l.getLongitude();
						return ret;
					}
				}
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			// If the 2 providers are the same, only activate one listener
			if (fastProvider_s != null)
			{
				if (Navit.p.PREF_use_fast_provider)
				{
					if (!Navit.DemoVehicle)
					{
						Location l = locationManager_s.getLastKnownLocation(fastProvider_s);
						//System.out.println("ZANAVI:getLastKnownLocation=" + l);
						if (l != null)
						{
							if (l.getAccuracy() > 0)
							{
								if ((l.getLatitude() != 0) && (l.getLongitude() != 0))
								{
									ret.lat = l.getLatitude();
									ret.lon = l.getLongitude();
									return ret;
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
		}

		return null;
	}

	public static void set_last_known_pos_precise_provider()
	{
		try
		{
			Location l = locationManager_s.getLastKnownLocation(preciseProvider_s);
			if (l != null)
			{
				if (l.getAccuracy() > 0)
				{
					if ((l.getLatitude() != 0) && (l.getLongitude() != 0))
					{
						if (Navit.p.PREF_follow_gps)
						{
							// Log.e("NavitVehicle", "getLastKnownLocation precise (2) l=" + l.toString());
							last_location = l;
							//Log.e("NavitVehicle", "call VehicleCallback 005");
							VehicleCallback2(l);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_last_known_pos_fast_provider()
	{
		// System.out.println("fast_provider_status=" + fast_provider_status);

		if (fast_provider_status == 0)
		{
			return;
		}

		try
		{
			// If the 2 providers are the same, only activate one listener
			if (fastProvider_s != null)
			{
				if (Navit.p.PREF_use_fast_provider)
				{
					if (!Navit.DemoVehicle)
					{
						Location l = locationManager_s.getLastKnownLocation(fastProvider_s);
						//System.out.println("ZANAVI:getLastKnownLocation=" + l);
						if (l != null)
						{
							if (l.getAccuracy() > 0)
							{
								if ((l.getLatitude() != 0) && (l.getLongitude() != 0))
								{
									if (Navit.p.PREF_follow_gps)
									{
										// Log.e("NavitVehicle", "getLastKnownLocation fast (3) l=" + l.toString());
										last_location = l;
										//Log.e("NavitVehicle", "call VehicleCallback 006");
										//System.out.println("ZANAVI:set_last_known_pos_fast_provider");
										VehicleCallback2(l);
									}
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void turn_on_fast_provider()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		try
		{
			// If the 2 providers are the same, only activate one listener
			if (fastProvider_s != null)
			{
				if (Navit.p.PREF_use_fast_provider)
				{
					if (!Navit.DemoVehicle)
					{
						disregard_first_fast_location = 2;
						locationManager_s.requestLocationUpdates(fastProvider_s, 30000L, 8.0f, fastLocationListener_s); // (long)time [milliseconds], (float)minDistance [meters]

						fast_provider_status = 1;
						//System.out.println("ZANAVI:turn on fast provider");
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	public static void turn_on_precise_provider()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		try
		{
			locationManager_s.requestLocationUpdates(preciseProvider_s, 0, 0, preciseLocationListener_s);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		turn_on_sat_status();

		try
		{
			// try to download aGPS data!!
			if (Navit.p.PREF_use_agps)
			{
				Navit.downloadGPSXtra(Navit.getBaseContext_);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);

	}

	public static void turn_off_precise_provider()
	{
		try
		{
			if (preciseProvider_s != null)
			{
				locationManager_s.removeUpdates(preciseLocationListener_s);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		turn_off_sat_status();
	}

	public static void turn_on_sat_status()
	{
		try
		{
			Navit.sats = 0;
			Navit.satsInFix = 0;
			if (preciseProvider_s != null)
			{
				try
				{
					locationManager_s.removeGpsStatusListener(gps_status_listener_s);
				}
				catch (Exception e3)
				{
					e3.printStackTrace();
				}
				locationManager_s.addGpsStatusListener(gps_status_listener_s);
				sat_status_enabled = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println("turn_ON_sat_status");
	}

	public static void turn_off_sat_status()
	{
		try
		{
			Navit.sats = 0;
			Navit.satsInFix = 0;
			sat_status_enabled = true;

			if (preciseProvider_s != null)
			{
				locationManager_s.removeGpsStatusListener(gps_status_listener_s);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println("turn_off_sat_status");
	}

	public static void turn_off_fast_provider()
	{
		try
		{
			fast_provider_status = 0;
			if (fastProvider_s != null)
			{
				locationManager_s.removeUpdates(fastLocationListener_s);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class TunnelExtrapolationThread extends Thread
	{
		private Boolean running;
		private static int interval_millis = 990;

		TunnelExtrapolationThread()
		{
			this.running = true;
		}

		public void run()
		{
			System.out.println("TunnelExtrapolationThread -- started --");
			while (this.running)
			{
				// request tunnel extrapolation from C code ------------------
				// request tunnel extrapolation from C code ------------------

				String extrapolated_post_string = NavitGraphics.CallbackGeoCalc(12, 1, interval_millis);

				if (extrapolated_post_string.equals("*ERROR*"))
				{
					System.out.println("extrapolated pos:*ERROR*");
				}
				else
				{
					try
					{
						// System.out.println("extrapolated pos:" + extrapolated_post_string);
						String tmp[] = extrapolated_post_string.split(":", 3);
						float lat = Float.parseFloat(tmp[0]);
						float lon = Float.parseFloat(tmp[1]);
						float dir = Float.parseFloat(tmp[2]);
						// System.out.println("extrapolated pos:" + lat + " " + lon + " " + dir);

						Location l = new Location("ZANavi Tunnel Extrapolation");
						l.setLatitude(lat);
						l.setLongitude(lon);
						l.setBearing(dir);
						l.setSpeed(50.0f / 3.6f); // in m/s
						l.setAccuracy(4.0f); // accuracy 4 meters
						// NavitVehicle.update_compass_heading(dir);
						NavitVehicle.set_mock_location__fast(l);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				try
				{
					Thread.sleep(interval_millis);
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		public void stop_me()
		{
			this.running = false;
			this.interrupt();
		}
	}

	static synchronized void turn_on_tunnel_extrapolation()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		if (!Navit.tunnel_extrapolation)
		{
			if (te_thread != null)
			{
				try
				{
					// try to clean up old thread
					te_thread.stop_me();
					te_thread = null;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			te_thread = new TunnelExtrapolationThread();
			te_thread.start();
		}
		Navit.tunnel_extrapolation = true;

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	static synchronized void turn_off_tunnel_extrapolation()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		if (Navit.tunnel_extrapolation)
		{
			if (te_thread != null)
			{
				try
				{
					// try to stop thread
					te_thread.stop_me();
					te_thread = null;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		Navit.tunnel_extrapolation = false;

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	public static void turn_off_all_providers()
	{
		turn_off_precise_provider();
		turn_off_fast_provider();
	}

	public static void update_compass_heading_force(float heading)
	{
		try
		{
			Location l = new Location("ZANavi Dummy");
			l.setLatitude(0.0);
			l.setLongitude(0.0);
			l.setAccuracy(4.0f); // accuracy 4 meters
			l.setBearing(heading);
			l.setSpeed(4.0f);
			VehicleCallback2(l);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
	}

	public static void update_compass_heading(float heading)
	{
		compass_heading = heading;
		// use compass heading
		try
		{
			if (Navit.p.PREF_use_compass_heading_base)
			{
				if ((Navit.p.PREF_use_compass_heading_always) || (last_location.getSpeed() < GPS_SPEED_ABOVE_USE_FOR_HEADING))
				{
					if ((lastcompass_update_timestamp + 400) > System.currentTimeMillis())
					{
						//Log.e("NavitVehicle", "compass update to fast!");
						return;
					}
					lastcompass_update_timestamp = System.currentTimeMillis();

					last_location.setBearing(compass_heading);
					// !! ugly hack to make map redraw !!
					// !! ugly hack to make map redraw !!
					// !! ugly hack to make map redraw !!
					if (last_location.getSpeed() == 0.0f)
					{
						float save_speed = last_location.getSpeed();
						last_location.setSpeed(0.2f);
						if (!Navit.DemoVehicle)
						{
							//Log.e("NavitVehicle", "call VehicleCallback 007:start");
							VehicleCallback2(last_location);
							/*
							 * Message m2 = new Message();
							 * m2.what = 2;
							 * vehicle_handler_.handleMessage(m2);
							 */
							//Log.e("NavitVehicle", "call VehicleCallback 007:end");
						}
						last_location.setSpeed(save_speed);
					}
					else
					{
						if (!Navit.DemoVehicle)
						{
							//Log.e("NavitVehicle", "call VehicleCallback 008");
							VehicleCallback2(last_location);
						}
					}
					// !! ugly hack to make map redraw !!
					// !! ugly hack to make map redraw !!
					// !! ugly hack to make map redraw !!
				}
			}
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
	}

	static void speech_recording_start()
	{
		speech_recording_started = true;

		String pos_recording_filename_gpx_base = Navit.NAVIT_DATA_DEBUG_DIR + "zanavi_speech_recording";
		String pos_recording_filename_gpx = pos_recording_filename_gpx_base + ".gpx";
		String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
		String pos_recording_filename_gpx_archive = pos_recording_filename_gpx_base + "_" + date + ".gpx";

		String gpx_header_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" + "<gpx version=\"1.1\" creator=\"ZANavi http://zanavi.cc\"\n" + "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + "     xmlns=\"http://www.topografix.com/GPX/1/1\"\n" + "     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" + "<metadata>\n" + "	<name>ZANavi Debug log</name>\n" + "	<desc>ZANavi</desc>\n" + "	<author>\n"
				+ "		<name>ZANavi</name>\n" + "	</author>\n" + "</metadata>\n";

		speech_recording_file_gpx = new File(pos_recording_filename_gpx);

		try
		{
			if (speech_recording_file_gpx.exists())
			{
				// archive old GPX file
				speech_recording_file_gpx.renameTo(new File(pos_recording_filename_gpx_archive));
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			speech_recording_file_gpx = new File(pos_recording_filename_gpx);
		}
		catch (Exception e)
		{
		}

		try
		{
			speech_recording_writer_gpx = new BufferedWriter(new FileWriter(speech_recording_file_gpx, true));
			speech_recording_writer_gpx.write(gpx_header_1);
			// speech_recording_writer_gpx.write("<rte>\n");
		}
		catch (Exception e)
		{
		}
	}

	static void speech_recording_end()
	{

		String gpx_trailer_1 = "</gpx>\n";

		try
		{
			// speech_recording_writer_gpx.write("</rte>\n");
			speech_recording_writer_gpx.write(gpx_trailer_1);
			speech_recording_writer_gpx.flush();
			speech_recording_writer_gpx.close();
		}
		catch (Exception e)
		{
		}
	}

	static void pos_recording_start()
	{
		is_pos_recording = true;

		String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
		String pos_recording_filename_base = Navit.NAVIT_DATA_DEBUG_DIR + date + "-" + "zanavi_pos_recording";
		String pos_recording_filename = pos_recording_filename_base + ".txt";
		// String pos_recording_filename_gpx_base = Navit.NAVIT_DATA_DEBUG_DIR + "zanavi_pos_recording";
		//String pos_recording_filename_gpx = pos_recording_filename_gpx_base + ".gpx";
		// String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
		//String pos_recording_filename_gpx_archive = pos_recording_filename_gpx_base + "_" + date + ".gpx";
		//String pos_recording_filename_archive = pos_recording_filename_base + "_" + date + ".txt";

		//	String gpx_header_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" + "<gpx version=\"1.1\" creator=\"ZANavi http://zanavi.cc\"\n" + "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + "     xmlns=\"http://www.topografix.com/GPX/1/1\"\n" + "     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n" + "<metadata>\n" + "	<name>ZANavi Debug log</name>\n" + "	<desc>ZANavi</desc>\n" + "	<author>\n"
		//		+ "		<name>ZANavi</name>\n" + "	</author>\n" + "</metadata>\n" + "<trk>\n" + "<trkseg>\n" + " <name>ACTIVE LOG</name>\n";

		pos_recording_file = new File(pos_recording_filename);
		//pos_recording_file_gpx = new File(pos_recording_filename_gpx);

		//		try
		//		{
		//			if (pos_recording_file_gpx.exists())
		//			{
		//				// archive old GPX file
		//				pos_recording_file_gpx.renameTo(new File(pos_recording_filename_gpx_archive));
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//		}

		try
		{
			pos_recording_writer = new BufferedWriter(new FileWriter(pos_recording_file, true));
			// pos_recording_file_gpx = new File(pos_recording_filename_gpx);
		}
		catch (Exception e)
		{
		}
	}

	static void pos_recording_end()
	{
		is_pos_recording = false;

		// String gpx_trailer_1 = "</trkseg>\n" + "</trk>\n" + "</gpx>\n";

		try
		{
			//pos_recording_writer_gpx.write(gpx_trailer_1);

			pos_recording_writer.close();
			// pos_recording_writer_gpx.close();
		}
		catch (Exception e)
		{
		}
	}

	static public String customNumberFormat_(String pattern, double value)
	{
		// NumberFormat myFormatter = DecimalFormat.getInstance(Locale.US);

		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat(pattern, otherSymbols);

		String output = df.format(value);
		return (output);
	}

	static void speech_recording_add(double lat, double lon, String text, long time)
	{
		try
		{
			String date_time_gpx = (String) DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", time);
			// System.out.println("33XX"+date_time_gpx+"YY");

			// speech_recording_writer_gpx.write(" <trkpt lat=\"" + customNumberFormat_("####.######", lat) + "\" lon=\"" + customNumberFormat_("####.######", lon) + "\"><time>2014-10-02T09:30:10Z</time><speed>" + customNumberFormat_("####.##", speed) + "</speed><course>" + customNumberFormat_("####.#", bearing) + "</course></trkpt>\n");
			// speech_recording_writer_gpx.write(" <rtept lat=\"" + customNumberFormat_("####.######", lat) + "\" lon=\"" + customNumberFormat_("####.######", lon) + "\"><name>" + text + "</name></rtept>" + "\n");
			speech_recording_writer_gpx.write(" <wpt lat=\"" + customNumberFormat_("####.######", lat) + "\" lon=\"" + customNumberFormat_("####.######", lon) + "\"><time>" + date_time_gpx + "</time>" + "<name>" + text + "</name><sym>Dot</sym><type>Dot</type></wpt>" + "\n");
		}
		catch (Exception e)
		{
		}
	}

	static void pos_recording_add(int cmd, double lat, double lon, double speed, double bearing, long time)
	{
		if (ZANaviDebugReceiver.dont_save_loc == true)
		{
			// dont save (already saved) log
			return;
		}

		//System.out.println("PRF:CMD=" + cmd_name[cmd] + " dst=" + Navit.global_last_destination_name);

		if (cmd == 0) // empty lines
		{
			try
			{
				pos_recording_writer.write("\n");
				pos_recording_writer.write("\n");
				pos_recording_writer.write("\n");
				pos_recording_writer.write("\n");
			}
			catch (Exception e)
			{
			}
		}
		else if (cmd == 1) // POS
		{

			//			try
			//			{
			//				pos_recording_writer_gpx.write(" <trkpt lat=\"" + customNumberFormat_("####.######", lat) + "\" lon=\"" + customNumberFormat_("####.######", lon) + "\"><time>2014-10-02T09:30:10Z</time><speed>" + customNumberFormat_("####.##", speed) + "</speed><course>" + customNumberFormat_("####.#", bearing) + "</course></trkpt>\n");
			//			}
			//			catch (Exception e)
			//			{
			//			}

			try
			{
				pos_recording_writer.write("POS:" + "\"" + customNumberFormat_("####.######", lat) + "," + customNumberFormat_("####.######", lon) + "," + customNumberFormat_("####.##", speed) + "," + customNumberFormat_("####.##", bearing) + "\"" + "\n");
				if (Navit.OSD_route_001.arriving_time_valid)
				{
					pos_recording_writer.write("REM:" + "\"ETA:" + Navit.OSD_route_001.arriving_time + "\"" + "\n");
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (cmd == 2) // CLR
		{
			try
			{
				if (time == 111)
				{
					pos_recording_writer.write("CLR:" + "\"\"" + "\n");
				}

				if (time == 111)
				{

					// before "CLR" rotate recording file --------------------------
					// before "CLR" rotate recording file --------------------------
					String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
					String pos_recording_filename_base = Navit.NAVIT_DATA_DEBUG_DIR + date + "-" + "zanavi_pos_recording";
					String pos_recording_filename = pos_recording_filename_base + ".txt";
					String pos_recording_filename_archive = pos_recording_filename_base + "_" + date + ".txt";

					//System.out.println("PRF:CLR:" + pos_recording_file.getName() + " " + pos_recording_filename_archive);
					pos_recording_filename_archive = Navit.NAVIT_DATA_DEBUG_DIR + "arch_" + pos_recording_file.getName();

					//System.out.println("PRF:CLR:004:" + pos_recording_filename_archive);

					try
					{
						if (pos_recording_file.exists())
						{
							//System.out.println("PRF:CLR:005");

							try
							{
								pos_recording_writer.close();
							}
							catch (Exception cl001)
							{
							}

							// archive old GPX file
							pos_recording_file.renameTo(new File(pos_recording_filename_archive));
							pos_recording_file = new File(pos_recording_filename);
							pos_recording_writer = new BufferedWriter(new FileWriter(pos_recording_file, true));

							//System.out.println("PRF:CLR:006");
						}
					}
					catch (Exception e)
					{
					}
					// before "CLR" rotate recording file --------------------------
					// before "CLR" rotate recording file --------------------------

					//System.out.println("PRF:CLR:007");
				}

				if (time == 0)
				{
					pos_recording_writer.write("CLR:" + "\"\"" + "\n");
				}

			}
			catch (Exception e)
			{
			}
		}
		else if (cmd == 3) // DST
		{
			try
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
				String currentDateandTime = sdf.format(new Date());

				try
				{
					//System.out.println("PRF:X01:" + Navit.global_last_destination_name + " " + pos_recording_file.getName());

					if ((Navit.global_last_destination_name.compareTo("") != 0) && (pos_recording_file.getName().contains("zanavi_pos_recording")))
					{
						String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
						String pos_recording_filename_newname = Navit.NAVIT_DATA_DEBUG_DIR + "route-" + date + "-" + Navit.global_last_destination_name + ".txt";

						//System.out.println("PRF:X02:" + pos_recording_filename_newname);

						if (pos_recording_file.exists())
						{
							pos_recording_writer.close();
							pos_recording_file.renameTo(new File(pos_recording_filename_newname));
							pos_recording_file = new File(pos_recording_filename_newname);
							pos_recording_writer = new BufferedWriter(new FileWriter(pos_recording_file, true));
						}
					}
				}
				catch (Exception e)
				{
				}

				pos_recording_writer.write("REM:" + "\"Date:" + currentDateandTime + "\"" + "\n");
				pos_recording_writer.write("DST:" + "\"" + customNumberFormat_("####.######", lat) + "," + customNumberFormat_("####.######", lon) + "\"" + "\n");
				if (Navit.global_last_destination_name.compareTo("") != 0)
				{
					pos_recording_writer.write("REM:" + "\"TO:" + Navit.global_last_destination_name + "\"" + "\n");
				}

			}
			catch (Exception e)
			{
			}
		}
	}

	static int distanceBetween(float lat1, float lon1, float lat2, float lon2)
	{
		int ret = -1;

		float[] res = new float[3];

		try
		{
			android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, res);
			ret = (int) (res[0]);
		}
		catch (Exception e)
		{
		}

		return ret;
	}

	static int distanceBetween(double lat1, double lon1, double lat2, double lon2)
	{
		int ret = -1;

		float[] res = new float[3];

		try
		{
			android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, res);
			ret = (int) (res[0]);
		}
		catch (Exception e)
		{
		}

		return ret;
	}

}
