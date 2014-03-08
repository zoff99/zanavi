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

import java.util.Iterator;

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

	int sats1_old = -1;
	int satsInFix1_old = -1;

	public static Handler vehicle_handler_ = null;
	public static long lastcompass_update_timestamp = 0L;

	public static final float GPS_SPEED_ABOVE_USE_FOR_HEADING = (float) (9 / 3.6f); //  (9 km/h) / (3.6) ~= m/s

	private static String preciseProvider_s = null;
	private static String fastProvider_s = null;

	public static long last_p_fix = 0;
	public static long last_f_fix = 0;
	public Bundle gps_extras = null;
	public static GpsStatus gps_status = null;
	public static Boolean update_location_in_progress = false;

	static long last_gps_status_update = 0L;

	public static Location last_location = null;

	public static native void VehicleCallback(double lat, double lon, float speed, float direction, double height, float radius, long gpstime);

	public static void VehicleCallback2(Location location)
	{
		if (Navit.Global_Init_Finished != 0)
		{
			if (Navit.Global_Location_update_not_allowed == 0)
			{
				if (NavitGraphics.DEBUG_SMOOTH_DRIVING) System.out.println("DEBUG_SMOOTH_DRIVING:TMG-DEBUG:Gps");
				Navit.cwthr.VehicleCallback3(location);
			}
		}
		else
		{
			System.out.println("VehicleCallback2:Global_Init_Finished == 0 !!!!!!!");
		}
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
					Thread.sleep(3000);
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
		vehicle_handler_ = Navit.vehicle_handler;

		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationManager_s = locationManager;

		LocationListener fastLocationListener = new LocationListener()
		{
			public void onLocationChanged(Location location)
			{
				last_f_fix = location.getTime();
				// if last gps fix was longer than 4 secs. ago, use this fix
				// and we dont have a GPS lock
				if ((last_p_fix + 4000 < last_f_fix) && (Navit.satsInFix < 3))
				{
					if (Navit.PREF_follow_gps)
					{
						if (Navit.PREF_use_compass_heading_base)
						{
							if ((Navit.PREF_use_compass_heading_always) || (location.getSpeed() < GPS_SPEED_ABOVE_USE_FOR_HEADING))
							{
								// use compass heading
								location.setBearing(compass_heading);
							}
						}

						//System.out.println("send values 1");
						Log.e("NavitVehicle", "LocationChanged provider=fast Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
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

				if (Navit.PREF_follow_gps)
				{
					if (Navit.PREF_use_compass_heading_base)
					{
						if ((Navit.PREF_use_compass_heading_always) || (location.getSpeed() < GPS_SPEED_ABOVE_USE_FOR_HEADING))
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
			locationManager.requestLocationUpdates(preciseProvider, 0, 0, preciseLocationListener);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// If the 2 providers is the same, only activate one listener
			if (fastProvider == null || preciseProvider.compareTo(fastProvider) == 0)
			{
				fastProvider = null;
			}
			else
			{
				if (Navit.PREF_use_fast_provider)
				{
					locationManager.requestLocationUpdates(fastProvider, 0, 0, fastLocationListener);
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
					if (last_gps_status_update + 2200 < System.currentTimeMillis())
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
								if (Navit.PREF_show_sat_status)
								{
									// redraw NavitOSDJava
									Message msg = NavitOSDJava.progress_handler_.obtainMessage();
									Bundle b = new Bundle();
									msg.what = 1;
									msg.setData(b);
									NavitOSDJava.progress_handler_.sendMessage(msg);
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
			}
		};
	}

	public static void set_mock_location__fast(Location mock_location)
	{
		try
		{
			//locationManager_s.setTestProviderLocation("ZANavi_mock", mock_location);
			// mock_location;
			// System.out.println("llllllll" + mock_location.getLatitude() + " " + mock_location.getLongitude());
			if (mock_location != null)
			{
				if (mock_location.getSpeed() == 0.0f)
				{
					float save_speed = last_location.getSpeed();
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
						if (Navit.PREF_follow_gps)
						{
							Log.e("NavitVehicle", "getLastKnownLocation precise (2) l=" + l.toString());
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
		try
		{
			// If the 2 providers is the same, only activate one listener
			if (fastProvider_s != null)
			{
				if (Navit.PREF_use_fast_provider)
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
									if (Navit.PREF_follow_gps)
									{
										Log.e("NavitVehicle", "getLastKnownLocation fast (3) l=" + l.toString());
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
		try
		{
			// If the 2 providers is the same, only activate one listener
			if (fastProvider_s != null)
			{
				if (Navit.PREF_use_fast_provider)
				{
					if (!Navit.DemoVehicle)
					{
						locationManager_s.requestLocationUpdates(fastProvider_s, 0, 0, fastLocationListener_s);
						//System.out.println("ZANAVI:turn on fast provider");
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void turn_on_precise_provider()
	{
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
			if (Navit.PREF_use_agps)
			{
				Navit.downloadGPSXtra(Navit.getBaseContext_);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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

	public static void turn_off_all_providers()
	{
		turn_off_precise_provider();
		turn_off_fast_provider();
	}

	/*
	 * public Handler vehicle_handler = new Handler()
	 * {
	 * public void handleMessage(Message msg)
	 * {
	 * switch (msg.what)
	 * {
	 * case 1:
	 * // dismissDialog(msg.getData().getInt("dialog_num"));
	 * // removeDialog(msg.getData().getInt("dialog_num"));
	 * Location l = new Location("Network");
	 * l.setLatitude(msg.getData().getFloat("lat"));
	 * l.setLongitude(msg.getData().getFloat("lng"));
	 * l.setBearing(msg.getData().getFloat("b"));
	 * l.setSpeed(0.8f);
	 * NavitVehicle.set_mock_location__fast(l);
	 * break;
	 * case 2:
	 * if (update_location_in_progress)
	 * {
	 * }
	 * else
	 * {
	 * update_location_in_progress = true;
	 * VehicleCallback(last_location);
	 * update_location_in_progress = false;
	 * }
	 * break;
	 * }
	 * }
	 * };
	 */

	public static void update_compass_heading(float heading)
	{
		compass_heading = heading;
		// use compass heading
		try
		{
			if (Navit.PREF_use_compass_heading_base)
			{
				if ((Navit.PREF_use_compass_heading_always) || (last_location.getSpeed() < GPS_SPEED_ABOVE_USE_FOR_HEADING))
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
}
