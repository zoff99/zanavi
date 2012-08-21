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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.zoffcc.applications.zanavi.NavitMapDownloader.ProgressThread;

import de.oberoner.gpx2navit_txt.MainFrame;

public class Navit extends Activity implements Handler.Callback, SensorEventListener
{
	public static final String VERSION_TEXT_LONG_INC_REV = "1937";
	public static String NavitAppVersion = "0";
	public static String NavitAppVersion_prev = "-1";
	public static String NavitAppVersion_string = "0";
	public final Boolean NAVIT_IS_EMULATOR = false; // when running on emulator set to true!!

	// define graphics here (this is bad, please fix me!)
	public static NavitGraphics N_NavitGraphics = null;

	static AlertDialog.Builder generic_alert_box = null;

	private Boolean xmlconfig_unpack_file = true;
	private Boolean write_new_version_file = true;
	final static int Navit_Status_COMPLETE_NEW_INSTALL = 1;
	final static int Navit_Status_UPGRADED_TO_NEW_VERSION = 2;
	final static int Navit_Status_NORMAL_STARTUP = 0;
	Boolean Navit_DonateVersion_Installed = false;
	static Boolean Navit_Largemap_DonateVersion_Installed = false;
	private int startup_status = Navit_Status_NORMAL_STARTUP;
	final static int Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL = 8;
	static Boolean unsupported = false;
	static Boolean Navit_maps_loaded = false;
	final static int Navit_MAX_RECENT_DESTINATIONS = 50;
	static String debug_item_dump = "";

	// for future use ...
	// public static String NavitDataDirectory = "/sdcard/";
	public static String NavitDataDirectory_Maps = "/sdcard/zanavi/maps/";

	public static int GlobalScaleLevel = 0;

	public class CopyFiles
	{
		public void copyFiles(File sourceLocation, File targetLocation) throws IOException
		{
			if (sourceLocation.isDirectory())
			{
				if (!targetLocation.exists())
				{
					targetLocation.mkdir();
				}
				File[] files = sourceLocation.listFiles();
				for (File file : files)
				{
					InputStream in = new FileInputStream(file);
					OutputStream out = new FileOutputStream(targetLocation + "/" + file.getName());

					// Copy the bits from input stream to output stream
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0)
					{
						out.write(buf, 0, len);
					}
					in.close();
					out.close();
				}
			}
		}
	}

	private static void copyFile(File sourceFile, File destFile) throws IOException
	{
		if (!sourceFile.exists())
		{
			return;
		}
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		source = new FileInputStream(sourceFile).getChannel();
		destination = new FileOutputStream(destFile).getChannel();
		if (destination != null && source != null)
		{
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null)
		{
			source.close();
		}
		if (destination != null)
		{
			destination.close();
		}

	}

	public static final class Navit_Address_Result_Struct
	{
		String result_type; // TWN,STR,SHN
		String item_id; // H<ddddd>L<ddddd> -> item.id_hi item.id_lo
		float lat;
		float lon;
		String addr;
	}

	public static final class Navit_Point_on_Map implements Serializable
	{
		/**
		 * struct for a point on the map
		 */
		private static final long serialVersionUID = 6899215049749155051L;
		String point_name = "";
		String addon = null;
		float lat = 0.0f;
		float lon = 0.0f;
	}

	static ArrayList<Navit_Point_on_Map> map_points;

	public static Set<String> Navit_Address_Result_double_index = new HashSet<String>();

	public NavitAndroidOverlay NavitAOverlay2;
	public static NavitAndroidOverlay NavitAOverlay2_s;

	public static final class Navit_OSD_compass
	{
		Boolean angle_north_valid = false;
		float angle_north = 0.0f;
		Boolean angle_target_valid = false;
		float angle_target = 0.0f;
		Boolean direct_distance_to_target_valid = false;
		String direct_distance_to_target = "";
	}

	public static final class Navit_OSD_route_001
	{
		Boolean driving_distance_to_target_valid = false;
		String driving_distance_to_target = "";
		Boolean arriving_time_valid = false;
		String arriving_time = "";
	}

	public static final class Navit_OSD_route_nextturn
	{
		Boolean nextturn_image_filename_valid = false;
		String nextturn_image_filename = "";
		Boolean nextturn_image_valid = false;
		Bitmap nextturn_image = null;
		Boolean nextturn_distance_valid = false;
		String nextturn_distance = "";
		String nextturn_streetname = "";
		String nextturn_streetname_systematic = "";
	}

	public static final class Navit_OSD_scale
	{
		Boolean scale_valid = false;
		String scale_text = "";
		int base = 0;
		int var = 0;
	}

	public static Navit_OSD_compass OSD_compass = new Navit_OSD_compass();
	public static Navit_OSD_route_001 OSD_route_001 = new Navit_OSD_route_001();
	public static Navit_OSD_route_nextturn OSD_nextturn = new Navit_OSD_route_nextturn();
	public static Navit_OSD_scale OSD_scale = new Navit_OSD_scale();

	SimGPS Simulate = null;
	WatchMem watchmem = null;

	public static int sats = 0;
	public static int satsInFix = 0;

	// public static Vibrator vibrator = null;

	public Handler handler;
	private PowerManager.WakeLock wl;
	private NavitActivityResult ActivityResults[];
	public static InputMethodManager mgr = null;
	public static DisplayMetrics metrics = null;
	public static Boolean show_soft_keyboard = false;
	public static Boolean show_soft_keyboard_now_showing = false;
	public static long last_pressed_menu_key = 0L;
	public static long time_pressed_menu_key = 0L;
	private static Intent startup_intent = null;
	private static long startup_intent_timestamp = 0L;
	public static String my_display_density = "mdpi";
	private boolean searchBoxShown = false;
	public static final int MAPDOWNLOAD_PRI_DIALOG = 1;
	public static final int MAPDOWNLOAD_SEC_DIALOG = 2;
	public static final int SEARCHRESULTS_WAIT_DIALOG = 3;
	public static final int SEARCHRESULTS_WAIT_DIALOG_OFFLINE = 4;
	public static final int ADDRESS_RESULTS_DIALOG_MAX = 10;
	public ProgressDialog mapdownloader_dialog_pri = null;
	public ProgressDialog mapdownloader_dialog_sec = null;
	public ProgressDialog search_results_wait = null;
	public ProgressDialog search_results_wait_offline = null;
	public static Handler Navit_progress_h = null;
	public static NavitMapDownloader mapdownloader_pri = null;
	public static NavitMapDownloader mapdownloader_sec = null;
	public static final int NavitDownloaderPriSelectMap_id = 967;
	public static final int NavitDownloaderSecSelectMap_id = 968;
	public static final int NavitDeleteSecSelectMap_id = 969;
	public static final int NavitRecentDest_id = 970;
	public static final int NavitGeoCoordEnter_id = 971;
	public static final int NavitGPXConvChooser_id = 972;
	public static int download_map_id = 0;
	ProgressThread progressThread_pri = null;
	ProgressThread progressThread_sec = null;
	public static int search_results_towns = 0;
	public static int search_results_streets = 0;
	public static int search_results_streets_hn = 0;
	public static Boolean NavitStartupAlreadySearching = false;
	SearchResultsThread searchresultsThread = null;
	SearchResultsThread searchresultsThread_offline = null;
	SearchResultsThreadSpinnerThread spinner_thread = null;
	SearchResultsThreadSpinnerThread spinner_thread_offline = null;
	public static Boolean NavitAddressSearchSpinnerActive = false;
	public static final int MAP_NUM_PRIMARY = 11;
	public static final int NavitAddressSearch_id_offline = 70;
	public static final int NavitAddressSearch_id_online = 73;
	public static final int NavitAddressResultList_id = 71;
	public static final int NavitAddressSearchCountry_id = 74;
	public static final int NavitMapPreview_id = 75;
	public static final int NavitAddressSearch_id_gmaps = 76;
	public static int NavitSearchresultBarIndex = -1;
	public static String NavitSearchresultBar_title = "";
	public static String NavitSearchresultBar_text = "";
	public static List<Navit_Address_Result_Struct> NavitAddressResultList_foundItems = new ArrayList<Navit_Address_Result_Struct>();
	public static Boolean DemoVehicle = false;

	static Typeface NavitStreetnameFont = null;

	public SensorManager sensorManager = null;
	//private static SensorManager sensorManager_ = null;

	public static Context getBaseContext_ = null;
	public static GpsStatus gps_st = null;

	public static Bitmap follow_on = null;
	public static Bitmap follow_off = null;
	public static Bitmap follow_current = null;
	public static Bitmap zoomin = null;
	public static Bitmap zoomout = null;
	public static Bitmap bigmap_bitmap = null;
	public static Bitmap oneway_arrow = null;

	public static String Navit_last_address_search_string = "";
	public static Boolean Navit_last_address_full_file_search = false;
	public static String Navit_last_address_search_country_iso2_string = "";
	public static int Navit_last_address_search_country_flags = 3;
	public static int Navit_last_address_search_country_id = 0;
	public static Boolean Navit_last_address_partial_match = false;
	public static Geocoder Navit_Geocoder = null;
	public static String UserAgentString = null;
	public static String UserAgentString_bind = null;
	public static Boolean first_ever_startup = false;

	public static Boolean Navit_Announcer = true;

	public static final int MAP_NUM_SECONDARY = 12;
	static String MAP_FILENAME_PATH = "/sdcard/zanavi/maps/";
	static String MAPMD5_FILENAME_PATH = "/sdcard/zanavi/md5/";
	static String CFG_FILENAME_PATH = "/sdcard/zanavi/";
	static final String NAVIT_DATA_DIR = "/data/data/com.zoffcc.applications.zanavi";
	static final String NAVIT_DATA_SHARE_DIR = NAVIT_DATA_DIR + "/share";
	static final String FIRST_STARTUP_FILE = NAVIT_DATA_SHARE_DIR + "/has_run_once.txt";
	static final String VERSION_FILE = NAVIT_DATA_SHARE_DIR + "/version.txt";
	static final String Navit_DEST_FILENAME = "destinations.dat";

	static boolean PREF_use_fast_provider;
	static boolean PREF_follow_gps;
	static boolean PREF_use_compass_heading_base;
	static boolean PREF_use_compass_heading_always;
	static boolean PREF_allow_gui_internal;
	static boolean PREF_show_vehicle_in_center;
	static boolean PREF_use_imperial;
	static boolean PREF_use_compass_heading_fast;
	static boolean PREF_use_anti_aliasing;
	static boolean PREF_gui_oneway_arrows;
	static boolean PREF_show_debug_messages;
	static boolean PREF_show_3d_map;
	static boolean PREF_use_lock_on_roads;
	static boolean PREF_use_route_highways;
	static boolean PREF_save_zoomlevel;
	static boolean PREF_show_sat_status;
	static boolean PREF_use_agps;
	static boolean PREF_enable_debug_functions;
	static boolean PREF_speak_street_names;
	static int PREF_search_country = 1; // default=*ALL*
	static int PREF_zoomlevel_num = 2 * 2 * 2 * 2 * 2;
	static boolean PREF_use_custom_font = true;
	static int PREF_map_font_size = 2; // 1 -> small, 2 -> normal, 3 -> large, 4-> extra large, 4-> mega large
	static int PREF_cancel_map_drawing_timeout = 1; // 0 -> short, 1-> normal, 2-> long, 3-> almost unlimited
	static boolean PREF_draw_polyline_circles = true; // true -> yes (default) false -> no
	static int PREF_mapcache = 10 * 1024; // in kbytes
	static String PREF_navit_lang;
	static int PREF_drawatorder = 1;
	static String PREF_streetsearch_r = "1"; // street search radius factor (multiplier)
	static String PREF_route_style = "1"; // 1 -> under green 2 -> on top blue
	static Boolean PREF_item_dump = false;

	static Resources res_ = null;

	public static String get_text(String in)
	{
		return NavitTextTranslations.get_text(in);
	}

	private boolean extractRes(String resname, String result)
	{
		int slash = -1;
		boolean needs_update = false;
		File resultfile;
		Resources res = getResources();
		Log.e("Navit", "Res Name " + resname);
		Log.e("Navit", "result " + result);
		int id = res.getIdentifier(resname, "raw", "com.zoffcc.applications.zanavi");
		Log.e("Navit", "Res ID " + id);

		if (id == 0)
		{
			return false;
		}

		while ((slash = result.indexOf("/", slash + 1)) != -1)
		{
			if (slash != 0)
			{
				Log.e("Navit", "Checking " + result.substring(0, slash));
				resultfile = new File(result.substring(0, slash));
				if (!resultfile.exists())
				{
					Log.e("Navit", "Creating dir");
					if (!resultfile.mkdir()) return false;
					needs_update = true;
				}
			}
		}

		resultfile = new File(result);

		if (!resultfile.exists())
		{
			needs_update = true;
		}

		if (!needs_update)
		{
			try
			{
				InputStream resourcestream = res.openRawResource(id);
				FileInputStream resultfilestream = new FileInputStream(resultfile);
				byte[] resourcebuf = new byte[1024];
				byte[] resultbuf = new byte[1024];
				int i = 0;
				while ((i = resourcestream.read(resourcebuf)) != -1)
				{
					if (resultfilestream.read(resultbuf) != i)
					{
						Log.e("Navit", "Result is too short");
						needs_update = true;
						break;
					}
					for (int j = 0; j < i; j++)
					{
						if (resourcebuf[j] != resultbuf[j])
						{
							Log.e("Navit", "Result is different");
							needs_update = true;
							break;
						}
					}
					if (needs_update) break;
				}
				if (!needs_update && resultfilestream.read(resultbuf) != -1)
				{
					Log.e("Navit", "Result is too long");
					needs_update = true;
				}

			}
			catch (Exception e)
			{
				Log.e("Navit", "Exception " + e.getMessage());
				return false;
			}
		}

		if (needs_update)
		{
			Log.e("Navit", "Extracting resource");
			try
			{
				InputStream resourcestream = res.openRawResource(id);
				FileOutputStream resultfilestream = new FileOutputStream(resultfile);
				byte[] buf = new byte[1024];
				int i = 0;
				while ((i = resourcestream.read(buf)) != -1)
				{
					resultfilestream.write(buf, 0, i);
				}
			}
			catch (Exception e)
			{
				Log.e("Navit", "Exception " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getBaseContext_ = getBaseContext();

		res_ = getResources();
		int ii = 0;
		NavitGraphics.dl_thread_cur = 0;
		for (ii = 0; ii < NavitGraphics.dl_thread_max; ii++)
		{
			NavitGraphics.dl_thread[ii] = null;
		}

		String font_file_name = "Roboto-Regular.ttf"; // "LiberationSans-Regular.ttf";
		NavitStreetnameFont = Typeface.createFromAsset(getBaseContext().getAssets(), font_file_name);
		// System.out.println("NavitStreetnameFont" + NavitStreetnameFont);

		Navit_maps_loaded = false;

		// only take arguments here, onResume gets called all the time (e.g. when screenblanks, etc.)
		Navit.startup_intent = this.getIntent();
		// hack! remeber timstamp, and only allow 4 secs. later in onResume to set target!
		Navit.startup_intent_timestamp = System.currentTimeMillis();
		Log.e("Navit", "**1**A " + startup_intent.getAction());
		Log.e("Navit", "**1**D " + startup_intent.getDataString());

		startup_status = Navit_Status_NORMAL_STARTUP;

		// init translated text
		NavitTextTranslations.init();

		// set the new locale here -----------------------------------
		getPrefs_loc();
		activatePrefs_loc();
		// set the new locale here -----------------------------------

		// set map cache size here -----------------------------------
		getPrefs_mapcache();
		activatePrefs_mapcache();
		// set map cache size here -----------------------------------

		// get map data dir and set it -----------------------------
		getPrefs_mapdir();
		activatePrefs_mapdir(true);
		// get map data dir and set it -----------------------------

		// make sure the new path for the navitmap.bin file(s) exist!!
		File navit_maps_dir = new File(MAP_FILENAME_PATH);
		navit_maps_dir.mkdirs();
		// create nomedia files
		File nomedia_file = new File(MAP_FILENAME_PATH + ".nomedia");
		try
		{
			nomedia_file.createNewFile();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		// create nomedia files

		// check if we already have a borders.bin file (if not, then extract the included simplified one)
		File b_ = new File(MAP_FILENAME_PATH + "/borders.bin");
		try
		{
			if (!b_.exists())
			{
				try
				{
					File c_ = new File(MAPMD5_FILENAME_PATH + "/borders.bin.md5");
					c_.delete();
				}
				catch (Exception e2)
				{

				}
				Log.e("Navit", "trying to extract borders simple resource to:" + MAP_FILENAME_PATH + "/borders.bin");
				if (!extractRes("borders_simple", MAP_FILENAME_PATH + "/borders.bin"))
				{
					Log.e("Navit", "Failed to extract borders simple resource to:" + MAP_FILENAME_PATH + "/borders.bin");
				}
			}
		}
		catch (Exception e)
		{

		}
		// check if we already have a borders.bin file

		// get the local language -------------
		Locale locale = java.util.Locale.getDefault();
		String lang = locale.getLanguage();
		String langu = lang;
		String langc = lang;
		Log.e("Navit", "lang=" + lang);
		int pos = langu.indexOf('_');
		if (pos != -1)
		{
			langc = langu.substring(0, pos);
			langu = langc + langu.substring(pos).toUpperCase(locale);
			Log.e("Navit", "substring lang " + langu.substring(pos).toUpperCase(locale));
			// set lang. for translation
			NavitTextTranslations.main_language = langc;
			NavitTextTranslations.sub_language = langu.substring(pos).toUpperCase(locale);
		}
		else
		{
			String country = locale.getCountry();
			Log.e("Navit", "Country1 " + country);
			Log.e("Navit", "Country2 " + country.toUpperCase(locale));
			langu = langc + "_" + country.toUpperCase(locale);
			// set lang. for translation
			NavitTextTranslations.main_language = langc;
			NavitTextTranslations.sub_language = country.toUpperCase(locale);
		}
		Log.e("Navit", "Language " + lang);
		// get the local language -------------

		// make sure the new path for config files exist
		File navit_cfg_dir = new File(CFG_FILENAME_PATH);
		navit_cfg_dir.mkdirs();

		// make sure the new path for the navitmap.bin file(s) exist!!
		File navit_mapsmd5_dir = new File(MAPMD5_FILENAME_PATH);
		navit_mapsmd5_dir.mkdirs();

		// make sure the share dir exists, otherwise the infobox will not show
		File navit_data_share_dir = new File(NAVIT_DATA_SHARE_DIR);
		navit_data_share_dir.mkdirs();

		// try to create cat. file if it does not exist
		File navit_maps_catalogue = new File(CFG_FILENAME_PATH + NavitMapDownloader.CAT_FILE);
		if (!navit_maps_catalogue.exists())
		{
			FileOutputStream fos_temp;
			try
			{
				fos_temp = new FileOutputStream(navit_maps_catalogue);
				fos_temp.write((NavitMapDownloader.MAP_CAT_HEADER + "\n").getBytes()); // just write header to the file
				fos_temp.flush();
				fos_temp.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// ---------- downloader threads ----------------
		NavitMapDownloader.MULTI_NUM_THREADS = 1;
		PackageInfo pkgInfo;
		Navit_DonateVersion_Installed = false;
		try
		{
			// is the donate version installed?
			pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_donate", 0);
			String sharedUserId = pkgInfo.sharedUserId;
			System.out.println("str nd=" + sharedUserId);
			if (sharedUserId.equals("com.zoffcc.applications.zanavi"))
			{
				System.out.println("##bonus 001##");
				Navit_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = 3;
			}
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// is the "large map" donate version installed?
			pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_largemap_donate", 0);
			String sharedUserId = pkgInfo.sharedUserId;
			System.out.println("str lm=" + sharedUserId);
			if (sharedUserId.equals("com.zoffcc.applications.zanavi"))
			{
				System.out.println("##bonus 002##");
				Navit_DonateVersion_Installed = true;
				Navit_Largemap_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = 3;
			}
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// update map list
		NavitMapDownloader.init_maps_without_donate_largemaps();
		// ---------- downloader threads ----------------

		Navit.follow_on = BitmapFactory.decodeResource(getResources(), R.drawable.follow);
		Navit.follow_off = BitmapFactory.decodeResource(getResources(), R.drawable.follow_off);
		Navit.follow_current = Navit.follow_on;

		Navit.zoomin = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_in_32_32);
		Navit.zoomout = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_out_32_32);

		Navit.oneway_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway);

		// *******************
		// *******************
		// *******************
		// *******************
		// check/init the catalogue file for downloaded maps
		NavitMapDownloader.init_cat_file();
		// *******************
		// *******************
		// *******************
		// *******************

		xmlconfig_unpack_file = false;
		write_new_version_file = false;
		try
		{
			NavitAppVersion = "" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
			NavitAppVersion_string = "" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
			NavitAppVersion = "1";
			NavitAppVersion_string = "1";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			NavitAppVersion = "2";
			NavitAppVersion_string = "2";
		}

		try
		{
			File navit_version = new File(VERSION_FILE);
			if (!navit_version.exists())
			{
				System.out.println("version file does not exist");
				NavitAppVersion_prev = "-1";
				write_new_version_file = true;
			}
			else
			{
				// files exists, read in the prev. verison number
				System.out.println("version file is here");
				FileInputStream fos_temp;
				byte[] buffer = new byte[101];
				fos_temp = new FileInputStream(navit_version);
				int len = fos_temp.read(buffer, 0, 100);
				if (len != -1)
				{
					// use only len bytes to make the string (the rest is garbage!!)
					NavitAppVersion_prev = new String(buffer).substring(0, len);
				}
				else
				{
					NavitAppVersion_prev = "-1";
					write_new_version_file = true;
				}
				fos_temp.close();
			}

		}
		catch (Exception e)
		{
			NavitAppVersion_prev = "-1";
			write_new_version_file = true;
			e.printStackTrace();
		}

		System.out.println("vprev:" + NavitAppVersion_prev + " vcur:" + NavitAppVersion);

		if (NavitAppVersion_prev.compareTo(NavitAppVersion) != 0)
		{
			// different version
			System.out.println("different version!!");
			write_new_version_file = true;
			xmlconfig_unpack_file = true;

			//if ((NavitAppVersion_prev.compareTo("-1") != 0) && (NavitAppVersion.compareTo("-1") != 0))
			//{
			// user has upgraded to a new version of ZANavi
			startup_status = Navit_Status_UPGRADED_TO_NEW_VERSION;
			//}
		}
		else
		{
			// same version
			System.out.println("same version");
			xmlconfig_unpack_file = false;
		}

		// write new version file
		if (write_new_version_file)
		{
			try
			{
				System.out.println("write version file");
				FileOutputStream fos_temp;
				File navit_version = new File(VERSION_FILE);
				navit_version.delete();
				fos_temp = new FileOutputStream(navit_version);
				fos_temp.write(NavitAppVersion.getBytes());
				fos_temp.flush();
				fos_temp.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// Sample useragent strings:
		//
		//		Mozilla/5.0 (Windows NT 6.1; WOW64; rv:7.0a1) Gecko/20110616 Firefox/7.0a1 SeaMonkey/2.4a1
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; GT-I9100 Build/GINGERBREAD)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.1; GT-S5830 Build/FROYO)
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; HTC Desire S Build/GRI40)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.2; MB525 Build/3.4.2-179)
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; HTC Wildfire S A510e Build/GRI40)
		//		Wget/1.10.2
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; sdk Build/GRI34)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.2; MB525 Build/3.4.2-164)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2; GT-I9000 Build/FROYO)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.1; GT-S5570L Build/FROYO)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.1; GT-I9000 Build/FROYO)
		//		Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)
		String ANDROID = android.os.Build.VERSION.SDK; //The current development codename, or the string "REL" if this is a release build.
		//String BOARD = android.os.Build.BOARD; //The name of the underlying board, like "goldfish".    
		//String BOOTLOADER = android.os.Build.BOOTLOADER; //  The system bootloader version number.
		String BRAND = android.os.Build.BRAND; //The brand (e.g., carrier) the software is customized for, if any.
		//String CPU_ABI = android.os.Build.CPU_ABI; //The name of the instruction set (CPU type + ABI convention) of native code.
		//String CPU_ABI2 = android.os.Build.CPU_ABI2; //  The name of the second instruction set (CPU type + ABI convention) of native code.
		String DEVICE = android.os.Build.DEVICE; //  The name of the industrial design.
		String DISPLAY = android.os.Build.DISPLAY; //A build ID string meant for displaying to the user
		//String FINGERPRINT = android.os.Build.FINGERPRINT; //A string that uniquely identifies this build.
		//String HARDWARE = android.os.Build.HARDWARE; //The name of the hardware (from the kernel command line or /proc).
		//String HOST = android.os.Build.HOST;
		//String ID = android.os.Build.ID; //Either a changelist number, or a label like "M4-rc20".
		String MANUFACTURER = android.os.Build.MANUFACTURER; //The manufacturer of the product/hardware.
		//String MODEL = android.os.Build.MODEL; //The end-user-visible name for the end product.
		//String PRODUCT = android.os.Build.PRODUCT; //The name of the overall product.
		//String RADIO = android.os.Build.RADIO; //The radio firmware version number.
		//String TAGS = android.os.Build.TAGS; //Comma-separated tags describing the build, like "unsigned,debug".
		//String TYPE = android.os.Build.TYPE; //The type of build, like "user" or "eng".
		//String USER = android.os.Build.USER;

		String android_version = "Android " + ANDROID;
		String android_device = MANUFACTURER + " " + BRAND + " " + DEVICE;

		// debug
		// debug
		// android_device = "telechips telechips m801";
		// debug
		// debug

		String android_rom_name = DISPLAY;
		if (Navit_DonateVersion_Installed == false)
		{
			UserAgentString = "Mozilla/5.0 (Linux; U; " + "Z" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
			UserAgentString_bind = "Mozilla/5.0 @__THREAD__@ (Linux; U; " + "Z" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
		}
		else
		{
			if (Navit_Largemap_DonateVersion_Installed == false)
			{
				UserAgentString = "Mozilla/5.0 (Linux; U; " + "donateZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
				UserAgentString_bind = "Mozilla/5.0 @__THREAD__@ (Linux; U; " + "donateZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
			}
			else
			{
				UserAgentString = "Mozilla/5.0 (Linux; U; " + "LMdonateLMZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
				UserAgentString_bind = "Mozilla/5.0 @__THREAD__@ (Linux; U; " + "LMdonateLMZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
			}
		}
		// System.out.println("UA=" + UserAgentString);

		unsupported = false;
		try
		{
			if (android_device.toLowerCase().contains("telechips"))
			{
				if (android_device.toLowerCase().contains("m801"))
				{
					// if the donate version is already installed, dont disable the app
					if (Navit_DonateVersion_Installed == false)
					{
						if (Navit_Largemap_DonateVersion_Installed == false)
						{
							// activate [Weltbild] Cat Nova again (19.12.2011)
							// ** // unsupported = true;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// this hangs the emulator, if emulator < 2.3 (only works in emulator >= 2.3)!!
			if (!NAVIT_IS_EMULATOR)
			{
				sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			}
		}
		catch (Exception e3)
		{
			e3.printStackTrace();
		}

		//		try
		//		{
		//			vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}
		//sensorManager_ = sensorManager;

		generic_alert_box = new AlertDialog.Builder(this);
		/*
		 * show info box for first time users
		 */
		AlertDialog.Builder infobox = new AlertDialog.Builder(this);
		//. english text: Welcome to ZANavi
		infobox.setTitle(Navit.get_text("__INFO_BOX_TITLE__")); //TRANS
		infobox.setCancelable(false);
		final TextView message = new TextView(this);
		message.setFadingEdgeLength(20);
		message.setVerticalFadingEdgeEnabled(true);
		message.setPadding(10, 5, 10, 5);
		message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		message.setGravity(Gravity.LEFT);
		// message.setScrollBarStyle(TextView.SCROLLBARS_INSIDE_OVERLAY);
		// message.setVerticalScrollBarEnabled(true);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		rlp.leftMargin = 7;
		rlp.rightMargin = 7;

		Navit.Navit_Geocoder = null;
		try
		{
			// for online search
			Navit.Navit_Geocoder = new Geocoder(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//TRANS
		infobox.setPositiveButton(Navit.get_text("Ok"), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				Log.e("Navit", "Ok, user saw the infobox");
			}
		});

		//TRANS
		infobox.setNeutralButton(Navit.get_text("More info"), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1)
			{
				Log.e("Navit", "user wants more info, show the website");
				// URL to ZANavi Manual (in english language)
				String url = "http://zanavi.cc/index.php/Manual";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		File navit_first_startup = new File(FIRST_STARTUP_FILE);
		// if file does NOT exist, show the info box
		if (!navit_first_startup.exists())
		{
			// set first-ever-startup flag
			first_ever_startup = true;
			startup_status = Navit_Status_COMPLETE_NEW_INSTALL;
			FileOutputStream fos_temp;
			try
			{
				fos_temp = new FileOutputStream(navit_first_startup);
				fos_temp.write((int) 65); // just write an "A" to the file, but it really doesnt matter
				fos_temp.flush();
				fos_temp.close();

				message.setLayoutParams(rlp);
				//. TRANSLATORS: multiline info text for first startup of application (see en_US for english text)
				final SpannableString s = new SpannableString(" " + Navit.get_text("__INFO_BOX_TEXT__")); //TRANS
				Linkify.addLinks(s, Linkify.WEB_URLS);
				message.setText(s);
				message.setMovementMethod(LinkMovementMethod.getInstance());
				infobox.setView(message);

				infobox.show();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		/*
		 * show info box for first time users
		 */

		// show info box for upgrade
		if (startup_status == Navit_Status_UPGRADED_TO_NEW_VERSION)
		{
			try
			{
				message.setLayoutParams(rlp);
				// upgrade message
				String upgrade_summary = "\n\n********\n";
				// upgrade message
				final SpannableString s = new SpannableString("\n" + "ZANavi " + NavitAppVersion_string + "\n\n" + "upgraded" + upgrade_summary);
				Linkify.addLinks(s, Linkify.WEB_URLS);
				message.setText(s);
				message.setMovementMethod(LinkMovementMethod.getInstance());
				infobox.setView(message);

				infobox.show();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// show info box for upgrade

		// make handler statically available for use in "msg_to_msg_handler"
		Navit_progress_h = this.progress_handler;

		Display display_ = getWindowManager().getDefaultDisplay();
		int width_ = display_.getWidth();
		int height_ = display_.getHeight();
		metrics = new DisplayMetrics();
		display_.getMetrics(Navit.metrics);
		Log.e("Navit", "Navit -> pixels x=" + width_ + " pixels y=" + height_);
		Log.e("Navit", "Navit -> dpi=" + Navit.metrics.densityDpi);
		Log.e("Navit", "Navit -> density=" + Navit.metrics.density);
		Log.e("Navit", "Navit -> scaledDensity=" + Navit.metrics.scaledDensity);

		System.gc();
		System.gc();
		Navit.bigmap_bitmap = BitmapFactory.decodeResource(getResources(), R.raw.bigmap_colors_zanavi2);
		// Navit.bigmap_bitmap.setDensity(120); // set our dpi!!

		try
		{
			ActivityResults = new NavitActivityResult[16];
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			// wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");
			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			wl = null;
		}

		try
		{
			if (wl != null)
			{
				wl.acquire();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Log.e("Navit", "trying to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language);
		if (!extractRes(NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language, NAVIT_DATA_DIR + "/locale/" + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language + "/LC_MESSAGES/navit.mo"))
		{
			Log.e("Navit", "Failed to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language);
		}

		Log.e("Navit", "trying to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language.toLowerCase());
		if (!extractRes(NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language.toLowerCase(), NAVIT_DATA_DIR + "/locale/" + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language + "/LC_MESSAGES/navit.mo"))
		{
			Log.e("Navit", "Failed to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language.toLowerCase());
		}

		Log.e("Navit", "trying to extract language resource " + NavitTextTranslations.main_language);
		if (!extractRes(NavitTextTranslations.main_language, NAVIT_DATA_DIR + "/locale/" + NavitTextTranslations.main_language + "/LC_MESSAGES/navit.mo"))
		{
			Log.e("Navit", "Failed to extract language resource " + NavitTextTranslations.main_language);
		}

		// DEBUG - check if language file is on SDCARD -
		try
		{
			File debug_mo_src = new File("/sdcard/zanavi/debug/navit.mo");
			File debug_mo_dest = new File(NAVIT_DATA_DIR + "/locale/" + NavitTextTranslations.main_language + "/LC_MESSAGES/navit.mo");
			//* File navit_debug_dir = new File("/sdcard/zanavi/debug/");
			//* navit_debug_dir.mkdirs();
			copyFile(debug_mo_src, debug_mo_dest);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// DEBUG - check if language file is on SDCARD -

		File navit_config_xml_file = new File(NAVIT_DATA_DIR + "/share/navit.xml");
		if (!navit_config_xml_file.exists())
		{
			xmlconfig_unpack_file = true;
			Log.e("Navit", "navit.xml does not exist, unpacking in any case");
		}

		my_display_density = "mdpi";
		// ldpi display (120 dpi)

		if (Navit.metrics.densityDpi <= 120)
		{
			my_display_density = "ldpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navitldpi", NAVIT_DATA_DIR + "/share/navit.xml"))
				{
					Log.e("Navit", "Failed to extract navit.xml for ldpi device(s)");
				}
			}
		}
		// mdpi display (160 dpi)
		else if ((Navit.metrics.densityDpi > 120) && (Navit.metrics.densityDpi <= 160))
		{
			my_display_density = "mdpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navitmdpi", NAVIT_DATA_DIR + "/share/navit.xml"))
				{
					Log.e("Navit", "Failed to extract navit.xml for mdpi device(s)");
				}
			}
		}
		// hdpi display (240 dpi)
		else if ((Navit.metrics.densityDpi > 160) && (Navit.metrics.densityDpi < 320))
		//else if (Navit.metrics.densityDpi == 240)
		{
			my_display_density = "hdpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navithdpi", NAVIT_DATA_DIR + "/share/navit.xml"))
				{
					Log.e("Navit", "Failed to extract navit.xml for hdpi device(s)");
				}
			}
		}
		// xhdpi display (320 dpi)
		else if (Navit.metrics.densityDpi >= 320)
		{
			Log.e("Navit", "found xhdpi device, this is not fully supported!!");
			Log.e("Navit", "using hdpi values");
			my_display_density = "hdpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navithdpi", NAVIT_DATA_DIR + "/share/navit.xml"))
				{
					Log.e("Navit", "Failed to extract navit.xml for xhdpi device(s)");
				}
			}
		}
		else
		{
			/* default, meaning we just dont know what display this is */
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navitmdpi", NAVIT_DATA_DIR + "/share/navit.xml"))
				{
					Log.e("Navit", "Failed to extract navit.xml (default version)");
				}
			}
		}
		// Debug.startMethodTracing("calc");

		//		if (unsupported)
		//		{
		//			class CustomListener implements View.OnClickListener
		//			{
		//				private final Dialog dialog;
		//
		//				public CustomListener(Dialog dialog)
		//				{
		//					this.dialog = dialog;
		//				}
		//
		//				@Override
		//				public void onClick(View v)
		//				{
		//
		//					// Do whatever you want here
		//
		//					// If tou want to close the dialog, uncomment the line below
		//					//dialog.dismiss();
		//				}
		//			}
		//
		//			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		//			dialog.setTitle(Navit.get_text("WeltBild Tablet")); //TRANS
		//			dialog.setCancelable(false);
		//			dialog.setMessage("Your device is not supported!");
		//			dialog.show();
		//			//Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		//			//theButton.setOnClickListener(new CustomListener(dialog));
		//		}

		// --> dont use!! NavitMain(this, langu, android.os.Build.VERSION.SDK_INT);
		Log.e("Navit", "android.os.Build.VERSION.SDK_INT=" + Integer.valueOf(android.os.Build.VERSION.SDK));
		NavitMain(this, langu, Integer.valueOf(android.os.Build.VERSION.SDK), my_display_density);
		// CAUTION: don't use android.os.Build.VERSION.SDK_INT if <uses-sdk android:minSdkVersion="3" />
		// You will get exception on all devices with Android 1.5 and lower
		// because Build.VERSION.SDK_INT is since SDK 4 (Donut 1.6)

		//		(see: http://developer.android.com/guide/appendix/api-levels.html)
		//		Platform Version   				API Level
		//		=============================================
		//		Android 4.0.3					15
		//		Android 4.0, 4.0.1, 4.0.2		14
		//      Android 3.2         			13
		//      Android 3.1      				12
		//      Android 3.0         			11
		//      Android 2.3.3       			10
		//      Android 2.3.1        			9
		//		Android 2.2          			8
		//		Android 2.1          			7
		//		Android 2.0.1        			6
		//		Android 2.0          			5
		//		Android 1.6          			4
		//		Android 1.5          			3
		//		Android 1.1          			2
		//		Android 1.0          			1

		NavitActivity(3);

		Navit.mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		getPrefs();
		activatePrefs();

		// unpack some localized Strings
		// a test now, later we will unpack all needed strings for java, here at this point!!
		//String x = NavitGraphics.getLocalizedString("Austria");
		//Log.e("Navit", "x=" + x);
		Navit.show_mem_used();

		/*
		 * GpsStatus.Listener listener = new GpsStatus.Listener()
		 * {
		 * public void onGpsStatusChanged(int event)
		 * {
		 * //System.out.println("xxxxx");
		 * if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
		 * {
		 * }
		 * }
		 * };
		 */
	}

	//	private void setOnKeyListener(OnKeyListener onKeyListener)
	//	{
	//
	//	}

	public static void show_mem_used() // wrapper
	{
		try
		{
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 14;
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void show_mem_used_real()
	{
		try
		{
			int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			String usedMegsString = String.format("Memory Used: %d MB", usedMegs);
			// System.out.println("" + usedMegsString);
			Navit.set_debug_messages2(usedMegsString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages(String texta, String textb, String textc)
	{
		try
		{
			NavitGraphics.debug_line_1 = texta;
			NavitGraphics.debug_line_2 = textb;
			NavitGraphics.debug_line_3 = textc;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			NavitGraphics.NavitMsgTv_.invalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages1(String text)
	{
		try
		{
			NavitGraphics.debug_line_1 = text;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			NavitGraphics.NavitMsgTv_.invalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages2(String text)
	{
		try
		{
			NavitGraphics.debug_line_2 = text;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			NavitGraphics.NavitMsgTv_.invalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages3(String text)
	{
		try
		{
			NavitGraphics.debug_line_3 = text;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			NavitGraphics.NavitMsgTv_.invalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages4(String text)
	{
		try
		{
			NavitGraphics.debug_line_4 = text;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			NavitGraphics.NavitMsgTv_.invalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages3_wrapper(String text)
	{
		try
		{
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 15;
			b.putString("text", text);
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onStart()
	{
		Navit.show_mem_used();

		System.gc();
		super.onStart();
		Log.e("Navit", "OnStart");
		NavitActivity(2);

		getPrefs();
		activatePrefs();
		// activate gps AFTER 3g-location
		NavitVehicle.turn_on_precise_provider();

		Navit.show_mem_used();

		// restore points
		read_map_points();
	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		Log.e("Navit", "OnRestart");
		NavitActivity(0);
	}

	@Override
	public void onResume()
	{
		// System.gc();
		super.onResume();

		Log.e("Navit", "OnResume");
		//InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		NavitActivity(1);

		try
		{
			if (wl != null)
			{
				wl.acquire();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//Intent caller = this.getIntent();
		//System.out.println("A=" + caller.getAction() + " D=" + caller.getDataString());
		//System.out.println("C=" + caller.getComponent().flattenToString());

		if (unsupported)
		{
			class CustomListener implements View.OnClickListener
			{
				private final Dialog dialog;

				public CustomListener(Dialog dialog)
				{
					this.dialog = dialog;
				}

				@Override
				public void onClick(View v)
				{

					// Do whatever you want here

					// If tou want to close the dialog, uncomment the line below
					//dialog.dismiss();
				}
			}

			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("WeltBild Tablet");
			dialog.setCancelable(false);
			dialog.setMessage("Your device is not supported!");
			dialog.show();
			//Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			//theButton.setOnClickListener(new CustomListener(dialog));
		}

		if (Navit_maps_loaded == false)
		{
			Navit_maps_loaded = true;
			// activate all maps
			Log.e("Navit", "**** LOAD ALL MAPS **** start");
			Message msg3 = new Message();
			Bundle b3 = new Bundle();
			b3.putInt("Callback", 20);
			msg3.setData(b3);
			N_NavitGraphics.callback_handler.sendMessage(msg3);
			Log.e("Navit", "**** LOAD ALL MAPS **** end");
		}

		String intent_data = null;
		if (startup_intent != null)
		{
			if (System.currentTimeMillis() <= Navit.startup_intent_timestamp + 4000L)
			{
				Log.e("Navit", "**2**A " + startup_intent.getAction());
				Log.e("Navit", "**2**D " + startup_intent.getDataString());
				intent_data = startup_intent.getDataString();
			}
			else
			{
				Log.e("Navit", "timestamp for navigate_to expired! not using data");
			}
		}

		if ((intent_data != null) && (intent_data.substring(0, 18).equals("google.navigation:")))
		{
			// better use regex later, but for now to test this feature its ok :-)
			// better use regex later, but for now to test this feature its ok :-)

			// g: google.navigation:///?ll=49.4086,17.4855&entry=w&opt=
			// d: google.navigation:q=blabla-strasse # (this happens when you are offline, or from contacts)
			// b: google.navigation:q=48.25676,16.643
			// a: google.navigation:ll=48.25676,16.643&q=blabla-strasse
			// e: google.navigation:ll=48.25676,16.643&title=blabla-strasse
			//    sample: -> google.navigation:ll=48.026096,16.023993&title=N%C3%B6stach+43%2C+2571+N%C3%B6stach&entry=w
			//            -> google.navigation:ll=48.014413,16.005579&title=Hainfelder+Stra%C3%9Fe+44%2C+2571%2C+Austria&entry=w
			// f: google.navigation:ll=48.25676,16.643&...
			// c: google.navigation:ll=48.25676,16.643

			String lat;
			String lon;
			String q;

			String temp1 = null;
			String temp2 = null;
			String temp3 = null;
			boolean parsable = false;
			boolean unparsable_info_box = true;
			try
			{
				intent_data = java.net.URLDecoder.decode(intent_data, "UTF-8");
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}

			// DEBUG
			// DEBUG
			// DEBUG
			// intent_data = "google.navigation:q=Wien Burggasse 27";
			// intent_data = "google.navigation:q=48.25676,16.643";
			// intent_data = "google.navigation:ll=48.25676,16.643&q=blabla-strasse";
			// intent_data = "google.navigation:ll=48.25676,16.643";
			// DEBUG
			// DEBUG
			// DEBUG

			try
			{
				Log.e("Navit", "found DEBUG 1: " + intent_data.substring(0, 20));
				Log.e("Navit", "found DEBUG 2: " + intent_data.substring(20, 22));
				Log.e("Navit", "found DEBUG 3: " + intent_data.substring(20, 21));
				Log.e("Navit", "found DEBUG 4: " + intent_data.split("&").length);
				Log.e("Navit", "found DEBUG 4.1: yy" + intent_data.split("&")[1].substring(0, 1).toLowerCase() + "yy");
				Log.e("Navit", "found DEBUG 5: xx" + intent_data.split("&")[1] + "xx");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (!Navit.NavitStartupAlreadySearching)
			{
				if (intent_data.length() > 19)
				{
					// if d: then start target search
					if ((intent_data.substring(0, 20).equals("google.navigation:q=")) && ((!intent_data.substring(20, 21).equals('+')) && (!intent_data.substring(20, 21).equals('-')) && (!intent_data.substring(20, 22).matches("[0-9][0-9]"))))
					{
						Log.e("Navit", "target found (d): " + intent_data.split("q=", -1)[1]);
						Navit.NavitStartupAlreadySearching = true;
						start_targetsearch_from_intent(intent_data.split("q=", -1)[1]);
						// dont use this here, already starting search, so set to "false"
						parsable = false;
						unparsable_info_box = false;
					}
					// if b: then remodel the input string to look like a:
					else if (intent_data.substring(0, 20).equals("google.navigation:q="))
					{
						intent_data = "ll=" + intent_data.split("q=", -1)[1] + "&q=Target";
						Log.e("Navit", "target found (b): " + intent_data);
						parsable = true;
					}
					// if g: [google.navigation:///?ll=49.4086,17.4855&...] then remodel the input string to look like a:
					else if (intent_data.substring(0, 25).equals("google.navigation:///?ll="))
					{
						intent_data = "google.navigation:ll=" + intent_data.split("ll=", -1)[1].split("&", -1)[0] + "&q=Target";
						Log.e("Navit", "target found (g): " + intent_data);
						parsable = true;
					}
					// if e: then remodel the input string to look like a:
					else if ((intent_data.substring(0, 21).equals("google.navigation:ll=")) && (intent_data.split("&").length > 1) && (intent_data.split("&")[1].substring(0, 1).toLowerCase().equals("f")))
					{
						int idx = intent_data.indexOf("&");
						intent_data = intent_data.substring(0, idx) + "&q=Target";
						Log.e("Navit", "target found (e): " + intent_data);
						parsable = true;
					}
					// if f: then remodel the input string to look like a:
					else if ((intent_data.substring(0, 21).equals("google.navigation:ll=")) && (intent_data.split("&").length > 1))
					{
						int idx = intent_data.indexOf("&");
						intent_data = intent_data.substring(0, idx) + "&q=Target";
						Log.e("Navit", "target found (f): " + intent_data);
						parsable = true;
					}
					// already looks like a: just set flag
					else if ((intent_data.substring(0, 21).equals("google.navigation:ll=")) && (intent_data.split("&q=").length > 1))
					{
						// dummy, just set the flag
						Log.e("Navit", "target found (a): " + intent_data);
						Log.e("Navit", "target found (a): " + intent_data.split("&q=").length);
						parsable = true;
					}
					// if c: then remodel the input string to look like a:
					else if ((intent_data.substring(0, 21).equals("google.navigation:ll=")) && (intent_data.split("&q=").length < 2))
					{
						intent_data = intent_data + "&q=Target";
						Log.e("Navit", "target found (c): " + intent_data);
						parsable = true;
					}
				}
			}
			else
			{
				Log.e("Navit", "already started search from startup intent");
				parsable = false;
				unparsable_info_box = false;
			}

			if (parsable)
			{
				// now string should be in form --> a:
				// now split the parts off
				temp1 = intent_data.split("&q=", -1)[0];
				try
				{
					temp3 = temp1.split("ll=", -1)[1];
					temp2 = intent_data.split("&q=", -1)[1];
				}
				catch (Exception e)
				{
					// java.lang.ArrayIndexOutOfBoundsException most likely
					// so let's assume we dont have '&q=xxxx'
					temp3 = temp1;
				}

				if (temp2 == null)
				{
					// use some default name
					temp2 = "Target";
				}

				lat = temp3.split(",", -1)[0];
				lon = temp3.split(",", -1)[1];
				q = temp2;
				// is the "search name" url-encoded? i think so, lets url-decode it here
				q = URLDecoder.decode(q);
				// System.out.println();

				Navit.remember_destination(q, lat, lon);
				Navit.destination_set();

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 3);
				b.putString("lat", lat);
				b.putString("lon", lon);
				b.putString("q", q);
				msg.setData(b);
				N_NavitGraphics.callback_handler.sendMessage(msg);

				// zoom_to_route();
				try
				{
					Thread.sleep(400);
				}
				catch (InterruptedException e)
				{
				}

				try
				{
					show_geo_on_screen(Float.parseFloat(lat), Float.parseFloat(lon));
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}

				try
				{
					Navit.follow_button_on();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
			else
			{
				if (unparsable_info_box && !searchBoxShown)
				{
					try
					{
						searchBoxShown = true;
						String searchString = intent_data.split("q=")[1];
						searchString = searchString.split("&")[0];
						searchString = URLDecoder.decode(searchString); // decode the URL: e.g. %20 -> space
						Log.e("Navit", "Search String :" + searchString);
						executeSearch(searchString);
					}
					catch (Exception e)
					{
						// safety net
						try
						{
							Log.e("Navit", "problem with startup search 7 str=" + intent_data);
						}
						catch (Exception e2)
						{
							e2.printStackTrace();
						}
					}
				}
			}
		}
		else if ((intent_data != null) && (intent_data.substring(0, 10).equals("geo:0,0?q=")))
		{
			// g: geo:0,0?q=wien%20burggasse

			boolean parsable = false;
			boolean unparsable_info_box = true;
			try
			{
				intent_data = java.net.URLDecoder.decode(intent_data, "UTF-8");
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}

			if (!Navit.NavitStartupAlreadySearching)
			{
				if (intent_data.length() > 10)
				{
					// if g: then start target search
					Log.e("Navit", "target found (g): " + intent_data.split("q=", -1)[1]);
					Navit.NavitStartupAlreadySearching = true;
					start_targetsearch_from_intent(intent_data.split("q=", -1)[1]);
					// dont use this here, already starting search, so set to "false"
					parsable = false;
					unparsable_info_box = false;
				}
			}
			else
			{
				Log.e("Navit", "already started search from startup intent");
				parsable = false;
				unparsable_info_box = false;
			}

			if (unparsable_info_box && !searchBoxShown)
			{
				try
				{
					searchBoxShown = true;
					String searchString = intent_data.split("q=")[1];
					searchString = searchString.split("&")[0];
					searchString = URLDecoder.decode(searchString); // decode the URL: e.g. %20 -> space
					Log.e("Navit", "Search String :" + searchString);
					executeSearch(searchString);
				}
				catch (Exception e)
				{
					// safety net
					try
					{
						Log.e("Navit", "problem with startup search 88 str=" + intent_data);
					}
					catch (Exception e2)
					{
						e2.printStackTrace();
					}
				}
			}

		}
		else if ((intent_data != null) && (intent_data.substring(0, 4).equals("geo:")))
		{
			// g: geo:16.8,46.3?z=15

			boolean parsable = false;
			boolean unparsable_info_box = true;

			String tmp1;
			String tmp2;
			String tmp3;
			float lat1 = 0;
			float lon1 = 0;
			int zoom1 = 15;

			try
			{
				intent_data = java.net.URLDecoder.decode(intent_data, "UTF-8");
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}

			if (!Navit.NavitStartupAlreadySearching)
			{
				try
				{
					tmp1 = intent_data.split(":", 2)[1];
					tmp2 = tmp1.split("\\?", 2)[0];
					tmp3 = tmp1.split("\\?", 2)[1];
					lat1 = Float.parseFloat(tmp2.split(",", 2)[0]);
					lon1 = Float.parseFloat(tmp2.split(",", 2)[1]);
					zoom1 = Integer.parseInt(tmp3.split("z=", 2)[1]);
					parsable = true;
				}
				catch (Exception e4)
				{
					e4.printStackTrace();
				}
			}

			if (parsable)
			{
				// geo: intent -> only show destination on map!

				// set zoomlevel before we show destination
				int zoom_want = zoom1;
				//
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 33);
				b.putString("s", Integer.toString(zoom_want));
				msg.setData(b);
				try
				{
					N_NavitGraphics.callback_handler.sendMessage(msg);
					Navit.GlobalScaleLevel = Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL;
					if ((zoom_want > 8) && (zoom_want < 17))
					{
						Navit.GlobalScaleLevel = (int) (Math.pow(2, (18 - zoom_want)));
						System.out.println("GlobalScaleLevel=" + Navit.GlobalScaleLevel);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				if (PREF_save_zoomlevel)
				{
					setPrefs_zoomlevel();
				}
				// set nice zoomlevel before we show destination

				try
				{
					Navit.follow_button_off();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}

				show_geo_on_screen(lat1, lon1);
			}
		}

		// hold all map drawing -----------
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 69);
		msg.setData(b);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// hold all map drawing -----------

		getPrefs();
		activatePrefs();
		// activate gps AFTER 3g-location
		NavitVehicle.turn_on_precise_provider();

		// allow all map drawing -----------
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", 70);
		msg.setData(b);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// allow all map drawing -----------

		NavitVehicle.set_last_known_pos_fast_provider();

		try
		{
			//Simulate = new SimGPS(NavitVehicle.vehicle_handler_);
			//Simulate.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			watchmem = new WatchMem();
			watchmem.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onPause()
	{
		System.out.println("@@ onPause @@");
		try
		{
			setPrefs_zoomlevel();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			watchmem.stop_me();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			//watchmem.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			//Simulate.stop_me();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			//Simulate.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Navit.show_mem_used();

		super.onPause();

		turn_off_compass();

		Log.e("Navit", "OnPause");
		NavitActivity(-1);

		Navit.show_mem_used();

		try
		{
			if (wl != null)
			{
				wl.release();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void onStop()
	{
		super.onStop();
		Log.e("Navit", "OnStop");

		NavitVehicle.turn_off_all_providers();

		NavitActivity(-2);
		System.gc();
		Navit.show_mem_used();

		// save points
		write_map_points();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.e("Navit", "OnDestroy");
		NavitActivity(-3);
		System.gc();
		Navit.show_mem_used();
	}

	public void setActivityResult(int requestCode, NavitActivityResult ActivityResult)
	{
		Log.e("Navit", "setActivityResult " + requestCode);
		ActivityResults[requestCode] = ActivityResult;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		//Log.e("Navit","onCreateOptionsMenu");
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		//Log.e("Navit","onPrepareOptionsMenu");
		// this gets called every time the menu is opened!!
		// change menu items here!
		menu.clear();

		// group-id,item-id,sort order number
		// menu.add(1, 1, 100, get_text("zoom in")); //leave out
		// menu.add(1, 2, 200, get_text("zoom out")); //leave out

		menu.add(1, 6, 300, get_text("address search (online)")); //TRANS
		menu.add(1, 7, 310, get_text("address search (offline)")); //TRANS
		if (NavitGraphics.CallbackDestinationValid2() > 0)
		{
			menu.add(1, 9, 450, get_text("Stop Navigation")); //TRANS
			menu.add(1, 11, 455, get_text("Zoom to Route")); //TRANS
			//. TRANSLATORS: it means: "show current target in google maps"
			//. TRANSLATORS: please keep this text short, to fit in the android menu!
			menu.add(1, 15, 457, get_text("Target in gmaps")); //TRANS
		}
		//. TRANSLATORS: text to translate is: exit ZANavi
		menu.add(1, 99, 480, get_text("exit navit")); //TRANS
		menu.add(1, 5, 485, get_text("toggle POI")); //TRANS
		menu.add(1, 10, 490, get_text("Settings")); //TRANS

		//menu.add(1, 18, 491, get_text("Coord Dialog 2")); //TRANS
		menu.add(1, 19, 492, get_text("Coord Dialog")); //TRANS

		if (Navit_Announcer == true)
		{
			menu.add(1, 12, 496, get_text("Announcer Off")); //TRANS
		}
		else
		{
			menu.add(1, 13, 495, get_text("Announcer On")); //TRANS
		}

		menu.add(1, 14, 497, get_text("Recent destinations")); //TRANS
		menu.add(1, 21, 498, get_text("add Traffic block")); //TRANS
		menu.add(1, 22, 499, get_text("clear Traffic blocks")); //TRANS
		menu.add(1, 3, 500, get_text("download maps")); //TRANS
		menu.add(1, 8, 505, get_text("delete maps")); //TRANS
		menu.add(1, 17, 508, get_text("show Maps age")); //TRANS
		menu.add(1, 20, 510, get_text("convert GPX file")); //TRANS
		menu.add(1, 23, 511, get_text("clear GPX map")); //TRANS

		if (PREF_enable_debug_functions)
		{
			menu.add(1, 88, 9001, "--");
			menu.add(1, 601, 9001, get_text("Demo Vehicle") + " [normal]"); //TRANS
			menu.add(1, 604, 9002, get_text("Demo Vehicle") + " [fast]"); //TRANS
			menu.add(1, 602, 9003, get_text("Speech Texts")); //TRANS
			menu.add(1, 603, 9004, get_text("Nav. Commands")); //TRANS
		}

		menu.add(1, 88, 11000, "--");
		menu.add(1, 16, 11001, get_text("online Help")); //TRANS

		// menu.add(1, 88, 800, "--");
		return true;
	}

	// callback id gets set here when called from NavitGraphics
	/*
	 * public static void setKeypressCallback(int kp_cb_id, NavitGraphics ng)
	 * {
	 * //Log.e("Navit", "setKeypressCallback -> id1=" + kp_cb_id);
	 * //Log.e("Navit", "setKeypressCallback -> ng=" + String.valueOf(ng));
	 * //N_KeypressCallbackID = kp_cb_id;
	 * N_NavitGraphics = ng;
	 * }
	 */

	/*
	 * public static void setMotionCallback(int mo_cb_id, NavitGraphics ng)
	 * {
	 * //Log.e("Navit", "setKeypressCallback -> id2=" + mo_cb_id);
	 * //Log.e("Navit", "setKeypressCallback -> ng=" + String.valueOf(ng));
	 * //N_MotionCallbackID = mo_cb_id;
	 * N_NavitGraphics = ng;
	 * }
	 */

	//public native void KeypressCallback(int id, String s);

	public void start_targetsearch_from_intent(String target_address)
	{
		Navit_last_address_partial_match = false;
		Navit_last_address_search_string = target_address;

		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		Boolean use_online_searchmode_here = false;
		Boolean hide_duplicates_searchmode_here = false;
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------

		int dialog_num_;

		if (use_online_searchmode_here)
		{
			dialog_num_ = Navit.SEARCHRESULTS_WAIT_DIALOG;
		}
		else
		{
			dialog_num_ = Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE;
		}

		// clear results
		Navit.NavitAddressResultList_foundItems.clear();
		Navit.Navit_Address_Result_double_index.clear();
		Navit.NavitSearchresultBarIndex = -1;
		Navit.NavitSearchresultBar_title = "";
		Navit.NavitSearchresultBar_text = "";

		if (Navit_last_address_search_string.equals(""))
		{
			// empty search string entered
			Toast.makeText(getApplicationContext(), Navit.get_text("No address found"), Toast.LENGTH_LONG).show(); //TRANS
		}
		else
		{
			// show dialog
			try
			{
				Log.e("Navit", "call-11: (0)num " + dialog_num_);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (hide_duplicates_searchmode_here)
			{
				// hide duplicates when searching
				// hide duplicates when searching
				Message msg22 = new Message();
				Bundle b22 = new Bundle();
				b22.putInt("Callback", 45);
				msg22.setData(b22);
				N_NavitGraphics.callback_handler.sendMessage(msg22);
				// hide duplicates when searching
				// hide duplicates when searching
			}

			Message msg = progress_handler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 11;
			b.putInt("dialog_num", dialog_num_);
			msg.setData(b);
			progress_handler.sendMessage(msg);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
		case 1:
			// zoom in
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 1);
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);
			// if we zoom, hide the bubble
			if (N_NavitGraphics.NavitAOverlay != null)
			{
				N_NavitGraphics.NavitAOverlay.hide_bubble();
			}
			Log.e("Navit", "onOptionsItemSelected -> zoom in");
			break;
		case 2:
			// zoom out
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 2);
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);
			// if we zoom, hide the bubble
			if (N_NavitGraphics.NavitAOverlay != null)
			{
				N_NavitGraphics.NavitAOverlay.hide_bubble();
			}
			Log.e("Navit", "onOptionsItemSelected -> zoom out");
			break;
		case 3:
			// map download menu
			Intent map_download_list_activity = new Intent(this, NavitDownloadSelectMapActivity.class);
			this.startActivityForResult(map_download_list_activity, Navit.NavitDownloaderPriSelectMap_id);
			break;
		case 5:
			// toggle the normal POI layers (to avoid double POIs)
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 5);
			b.putString("cmd", "toggle_layer(\"POI Symbols\");");
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);
			// toggle full POI icons on/off
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 5);
			b.putString("cmd", "toggle_layer(\"Android-POI-Icons-full\");");
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);
			break;
		case 6:
			// ok startup address search activity (online google maps search)
			Intent search_intent = new Intent(this, NavitAddressSearchActivity.class);
			search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent.putExtra("address_string", Navit_last_address_search_string);
			search_intent.putExtra("type", "online");
			String pm_temp = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp = "1";
			}
			search_intent.putExtra("partial_match", pm_temp);
			this.startActivityForResult(search_intent, NavitAddressSearch_id_online);
			break;
		case 7:
			// ok startup address search activity (offline binfile search)
			Intent search_intent2 = new Intent(this, NavitAddressSearchActivity.class);
			search_intent2.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent2.putExtra("address_string", Navit_last_address_search_string);
			search_intent2.putExtra("type", "offline");
			search_intent2.putExtra("search_country_id", Navit_last_address_search_country_id);

			String pm_temp2 = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp2 = "1";
			}

			search_intent2.putExtra("partial_match", pm_temp2);
			this.startActivityForResult(search_intent2, NavitAddressSearch_id_offline);
			break;
		case 8:
			// map delete menu
			Intent map_delete_list_activity2 = new Intent(this, NavitDeleteSelectMapActivity.class);
			this.startActivityForResult(map_delete_list_activity2, Navit.NavitDeleteSecSelectMap_id);
			break;
		case 9:
			// stop navigation (this menu should only appear when navigation is actually on!)
			Message msg2 = new Message();
			Bundle b2 = new Bundle();
			b2.putInt("Callback", 7);
			msg2.setData(b2);
			N_NavitGraphics.callback_handler.sendMessage(msg2);
			break;
		case 10:
			// open settings menu
			Intent settingsActivity = new Intent(getBaseContext(), NavitPreferences.class);
			startActivity(settingsActivity);
			break;
		case 11:
			//zoom_to_route
			zoom_to_route();
			break;
		case 12:
			// announcer off
			Navit_Announcer = false;
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 34);
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);
			break;
		case 13:
			// announcer on
			Navit_Announcer = true;
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 35);
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);
			break;
		case 14:
			// show recent destination list
			Intent i2 = new Intent(this, NavitRecentDestinationActivity.class);
			this.startActivityForResult(i2, Navit.NavitRecentDest_id);
			break;
		case 15:
			// show current target on googlemaps
			String current_target_string = NavitGraphics.CallbackGeoCalc(4, 1, 1);
			// Log.e("Navit", "got target  1: "+current_target_string);
			if (current_target_string.equals("x:x"))
			{
				Log.e("Navit", "no target set!");
			}
			else
			{
				try
				{
					String tmp[] = current_target_string.split(":", 2);
					googlemaps_show(tmp[0], tmp[1], "ZANavi Target");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e("Navit", "problem with target!");
				}
			}
			break;
		case 16:
			// show online manual
			Log.e("Navit", "user wants online help, show the website lang=" + NavitTextTranslations.main_language.toLowerCase());
			// URL to ZANavi Manual (in english language)
			String url = "http://zanavi.cc/index.php/Manual";
			if (NavitTextTranslations.main_language.toLowerCase().equals("de"))
			{
				// show german manual
				url = "http://zanavi.cc/index.php/Manual/de";
			}

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			break;
		case 17:
			// show age of maps (online)
			Intent i3 = new Intent(Intent.ACTION_VIEW);
			i3.setData(Uri.parse(NavitMapDownloader.ZANAVI_MAPS_AGE_URL));
			startActivity(i3);
			break;
		case 18:
			Intent intent_latlon = new Intent(Intent.ACTION_MAIN);
			//intent_latlon.setAction("android.intent.action.POINTPICK");
			intent_latlon.setPackage("com.cruthu.latlongcalc1");
			intent_latlon.setClassName("com.cruthu.latlongcalc1", "com.cruthu.latlongcalc1.LatLongMain");
			//intent_latlon.setClassName("com.cruthu.latlongcalc1", "com.cruthu.latlongcalc1.LatLongPointPick");
			try
			{
				startActivity(intent_latlon);
			}
			catch (Exception e88)
			{
				e88.printStackTrace();
				// show install page
				try
				{
					// String urlx = "http://market.android.com/details?id=com.cruthu.latlongcalc1";
					String urlx = "market://details?id=com.cruthu.latlongcalc1";
					Intent ix = new Intent(Intent.ACTION_VIEW);
					ix.setData(Uri.parse(urlx));
					startActivity(ix);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			break;
		case 19:
			// GeoCoordEnterDialog
			Intent it001 = new Intent(this, GeoCoordEnterDialog.class);
			this.startActivityForResult(it001, Navit.NavitGeoCoordEnter_id);
			break;
		case 20:
			// convert GPX file
			Intent intent77 = new Intent(getBaseContext(), FileDialog.class);
			File a = new File(MAP_FILENAME_PATH + "/../");
			try
			{
				// convert the "/../" in the path to normal absolut dir
				intent77.putExtra(FileDialog.START_PATH, a.getCanonicalPath());
				//can user select directories or not
				intent77.putExtra(FileDialog.CAN_SELECT_DIR, false);
				// disable the "new" button
				intent77.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
				//alternatively you can set file filter
				//intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "gpx" });
				startActivityForResult(intent77, Navit.NavitGPXConvChooser_id);
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			break;
		case 21:
			// add traffic block (like blocked road, or construction site) at current location of crosshair
			try
			{
				String traffic = NavitGraphics.CallbackGeoCalc(7, (int) (NavitGraphics.mCanvasWidth / 2), (int) (NavitGraphics.mCanvasHeight / 2));
				// System.out.println("traffic=" + traffic);
				File traffic_file_dir = new File(MAP_FILENAME_PATH);
				traffic_file_dir.mkdirs();
				File traffic_file = new File(MAP_FILENAME_PATH + "/traffic.txt");
				FileOutputStream fOut = null;
				OutputStreamWriter osw = null;
				try
				{
					fOut = new FileOutputStream(traffic_file, true);
					osw = new OutputStreamWriter(fOut);
					osw.write("type=traffic_distortion maxspeed=0" + "\n"); // item header
					osw.write(traffic); // item coordinates
					osw.close();
					fOut.close();
				}
				catch (Exception ef)
				{
					ef.printStackTrace();
				}

				// update route, if a route is set
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 73);
				msg.setData(b);
				N_NavitGraphics.callback_handler.sendMessage(msg);

				// draw map async
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 65);
				msg.setData(b);
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case 22:
			// clear all traffic blocks
			try
			{
				File traffic_file = new File(MAP_FILENAME_PATH + "/traffic.txt");
				traffic_file.delete();

				// update route, if a route is set
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 73);
				msg.setData(b);
				N_NavitGraphics.callback_handler.sendMessage(msg);

				// draw map async
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 65);
				msg.setData(b);
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{

			}
			break;
		case 23:
			// clear all GPX maps
			try
			{
				File gpx_file = new File(MAP_FILENAME_PATH + "/gpxtracks.txt");
				gpx_file.delete();

				// draw map async
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 65);
				msg.setData(b);
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{

			}
			break;
		case 88:
			// dummy entry, just to make "breaks" in the menu
			break;
		case 601:
			// DEBUG: activate demo vehicle and set position to position 20px-x, 20px-y on screen

			Navit.DemoVehicle = true;

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 52);
			b.putString("s", "45"); // speed in km/h of Demo-Vehicle
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 51);
			b.putInt("x", 20);
			b.putInt("y", 20);
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);

			break;
		case 602:
			// DEBUG: toggle textview with spoken and translated string (to help with translation)
			try
			{
				if (NavitGraphics.NavitMsgTv2_.getVisibility() == View.VISIBLE)
				{
					NavitGraphics.NavitMsgTv2_.setVisibility(View.GONE);
					NavitGraphics.NavitMsgTv2_.setEnabled(false);
				}
				else
				{
					NavitGraphics.NavitMsgTv2_.setVisibility(View.VISIBLE);
					NavitGraphics.NavitMsgTv2_.setEnabled(true);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case 603:
			// DEBUG: show all possible navigation commands (also translated)
			NavitGraphics.generate_all_speech_commands();
			break;
		case 604:
			// DEBUG: activate FAST driving demo vehicle and set position to position 20px-x, 20px-y on screen

			Navit.DemoVehicle = true;

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 52);
			b.putString("s", "800"); // speed in ~km/h of Demo-Vehicle
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 51);
			b.putInt("x", 20);
			b.putInt("y", 20);
			msg.setData(b);
			N_NavitGraphics.callback_handler.sendMessage(msg);

			break;
		case 99:
			// exit
			this.onPause();
			this.onStop();
			this.exit();
			//msg = new Message();
			//b = new Bundle();
			//b.putInt("Callback", 5);
			//b.putString("cmd", "quit();");
			//msg.setData(b);
			//N_NavitGraphics.callback_handler.sendMessage(msg);
			break;
		}
		return true;
	}

	//private class PickerListener implements NumberPicker.ValueChangeListener
	//{
	//	@Override
	//	public void onNumberPickerValueChange(NumberPicker picker, int value)
	//	{
	//if (picker.getId() == R.id.SpinRate)
	//{
	//	mBeatsPerMin = value;
	//}
	//		return;
	//	}
	//}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//Log.e("Navit", "onActivityResult");
		switch (requestCode)
		{
		case Navit.NavitGPXConvChooser_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					String in_ = data.getStringExtra(FileDialog.RESULT_PATH);
					String out_ = MAP_FILENAME_PATH + "/gpxtracks.txt";
					MainFrame.do_conversion(in_, out_);

					// draw map async
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putInt("Callback", 65);
					msg.setData(b);
					N_NavitGraphics.callback_handler.sendMessage(msg);
				}
			}
			catch (Exception e77)
			{
				e77.printStackTrace();
			}
			break;
		case Navit.NavitDeleteSecSelectMap_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					// remove all sdcard maps
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putInt("Callback", 19);
					msg.setData(b);
					N_NavitGraphics.callback_handler.sendMessage(msg);

					Log.d("Navit", "delete map id=" + Integer.parseInt(data.getStringExtra("selected_id")));
					String map_full_line = NavitMapDownloader.OSM_MAP_NAME_ondisk_ORIG_LIST[Integer.parseInt(data.getStringExtra("selected_id"))];
					Log.d("Navit", "delete map full line=" + map_full_line);

					String del_map_name = MAP_FILENAME_PATH + map_full_line.split(":", 2)[0];
					System.out.println("del map file :" + del_map_name);
					// remove from cat file
					NavitMapDownloader.remove_from_cat_file(map_full_line);
					// remove from disk
					File del_map_name_file = new File(del_map_name);
					del_map_name_file.delete();
					for (int jkl = 1; jkl < 51; jkl++)
					{
						File del_map_name_fileSplit = new File(del_map_name + "." + String.valueOf(jkl));
						del_map_name_fileSplit.delete();
					}
					// remove also any MD5 files for this map that may be on disk
					try
					{
						String tmp = map_full_line.split(":", 2)[1];
						if (!tmp.equals(NavitMapDownloader.MAP_URL_NAME_UNKNOWN))
						{
							tmp = tmp.replace("*", "");
							tmp = tmp.replace("/", "");
							tmp = tmp.replace("\\", "");
							tmp = tmp.replace(" ", "");
							tmp = tmp.replace(">", "");
							tmp = tmp.replace("<", "");
							System.out.println("removing md5 file:" + Navit.MAPMD5_FILENAME_PATH + tmp + ".md5");
							File md5_final_filename = new File(Navit.MAPMD5_FILENAME_PATH + tmp + ".md5");
							md5_final_filename.delete();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					// remove map, and zoom out
					// ***** onStop();
					// ***** onCreate(getIntent().getExtras());

					// add all sdcard maps
					msg = new Message();
					b = new Bundle();
					b.putInt("Callback", 20);
					msg.setData(b);
					N_NavitGraphics.callback_handler.sendMessage(msg);

					zoom_out_full();
				}
			}
			catch (Exception e)
			{
				Log.d("Navit", "error on onActivityResult 3");
				e.printStackTrace();
			}
			break;
		case Navit.NavitDownloaderPriSelectMap_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					try
					{
						Log.d("Navit", "PRI id=" + Integer.parseInt(data.getStringExtra("selected_id")));
						// set map id to download
						Navit.download_map_id = NavitMapDownloader.OSM_MAP_NAME_ORIG_ID_LIST[Integer.parseInt(data.getStringExtra("selected_id"))];
						// show the map download progressbar, and download the map
						if (Navit.download_map_id > -1)
						{
							showDialog(Navit.MAPDOWNLOAD_PRI_DIALOG);
						}
					}
					catch (NumberFormatException e)
					{
						Log.d("Navit", "NumberFormatException selected_id");
					}
				}
				else
				{
					// user pressed back key
				}
			}
			catch (Exception e)
			{
				Log.d("Navit", "error on onActivityResult");
				e.printStackTrace();
			}
			break;
		case Navit.NavitDownloaderSecSelectMap_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					try
					{
						Log.d("Navit", "SEC id=" + Integer.parseInt(data.getStringExtra("selected_id")));
						// set map id to download
						Navit.download_map_id = NavitMapDownloader.OSM_MAP_NAME_ORIG_ID_LIST[Integer.parseInt(data.getStringExtra("selected_id"))];
						// show the map download progressbar, and download the map
						if (Navit.download_map_id > -1)
						{
							showDialog(Navit.MAPDOWNLOAD_SEC_DIALOG);
						}
					}
					catch (NumberFormatException e)
					{
						Log.d("Navit", "NumberFormatException selected_id");
					}
				}
				else
				{
					// user pressed back key
				}
			}
			catch (Exception e)
			{
				Log.d("Navit", "error on onActivityResult");
				e.printStackTrace();
			}
			break;
		case NavitAddressSearch_id_online:
		case NavitAddressSearch_id_offline:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					try
					{
						String addr = data.getStringExtra("address_string");
						Boolean partial_match = false;
						try
						{
							// only from offline mask!
							partial_match = data.getStringExtra("partial_match").equals("1");
						}
						catch (Exception e)
						{
						}

						Message msg2 = new Message();
						Bundle b2 = new Bundle();
						b2.putInt("Callback", 44);
						msg2.setData(b2);
						N_NavitGraphics.callback_handler.sendMessage(msg2);

						if (requestCode == NavitAddressSearch_id_offline)
						{
							try
							{
								Boolean hide_dup = data.getStringExtra("hide_dup").equals("1");
								if (hide_dup)
								{
									Message msg = new Message();
									Bundle b = new Bundle();
									b.putInt("Callback", 45);
									msg.setData(b);
									N_NavitGraphics.callback_handler.sendMessage(msg);
								}
							}
							catch (Exception e)
							{
							}
						}

						Navit_last_address_partial_match = partial_match;
						Navit_last_address_search_string = addr;

						try
						{
							// only from offline mask!
							Navit_last_address_full_file_search = data.getStringExtra("full_file_search").equals("1");
						}
						catch (Exception e)
						{
							Navit_last_address_full_file_search = false;
						}

						try
						{
							// only from offline mask!
							Navit_last_address_search_country_iso2_string = data.getStringExtra("address_country_iso2");
							Navit_last_address_search_country_flags = data.getIntExtra("address_country_flags", 3);
							// System.out.println("Navit_last_address_search_country_flags=" + Navit_last_address_search_country_flags);
							Navit_last_address_search_country_id = data.getIntExtra("search_country_id", 1); // default=*ALL*
							PREF_search_country = Navit_last_address_search_country_id;
							setPrefs_search_country();
						}
						catch (Exception e)
						{

						}

						// clear results
						Navit.NavitAddressResultList_foundItems.clear();
						Navit.Navit_Address_Result_double_index.clear();
						Navit.NavitSearchresultBarIndex = -1;
						Navit.NavitSearchresultBar_title = "";
						Navit.NavitSearchresultBar_text = "";
						Navit.search_results_towns = 0;
						Navit.search_results_streets = 0;
						Navit.search_results_streets_hn = 0;

						if (addr.equals(""))
						{
							// empty search string entered
							Toast.makeText(getApplicationContext(), Navit.get_text("No search string entered"), Toast.LENGTH_LONG).show(); //TRANS
						}
						else
						{
							if (requestCode == NavitAddressSearch_id_online)
							{
								// online googlemaps search
								try
								{
									Log.e("Navit", "call-11: (1)num " + Navit.SEARCHRESULTS_WAIT_DIALOG);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}

								System.out.println("online googlemaps search");
								Message msg = progress_handler.obtainMessage();
								Bundle b = new Bundle();
								msg.what = 11;
								b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG);
								msg.setData(b);
								progress_handler.sendMessage(msg);
							}
							else if (requestCode == NavitAddressSearch_id_offline)
							{
								// offline binfile search
								try
								{
									Log.e("Navit", "call-11: (2)num " + Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}

								// show dialog, and start search for the results
								// make it indirect, to give our activity a chance to startup
								// (remember we come straight from another activity and ours is still paused!)
								Message msg = progress_handler.obtainMessage();
								Bundle b = new Bundle();
								msg.what = 11;
								b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
								msg.setData(b);
								progress_handler.sendMessage(msg);
							}
						}
					}
					catch (NumberFormatException e)
					{
						Log.d("Navit", "NumberFormatException selected_id");
					}
				}
				else
				{
					// user pressed back key
				}
			}
			catch (Exception e)
			{
				Log.d("Navit", "error on onActivityResult");
				e.printStackTrace();
			}
			break;
		case Navit.NavitAddressResultList_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					try
					{
						if (data.getStringExtra("what").equals("view"))
						{
							// get the coords for the destination
							int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

							// set nice zoomlevel before we show destination
							int zoom_want = Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL;
							//
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 33);
							b.putString("s", Integer.toString(zoom_want));
							msg.setData(b);
							try
							{
								N_NavitGraphics.callback_handler.sendMessage(msg);
								Navit.GlobalScaleLevel = zoom_want;
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							if (PREF_save_zoomlevel)
							{
								setPrefs_zoomlevel();
							}
							// set nice zoomlevel before we show destination

							try
							{
								Navit.follow_button_off();
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}

							show_geo_on_screen(Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
						}
						else
						{
							Log.d("Navit", "adress result list id=" + Integer.parseInt(data.getStringExtra("selected_id")));
							// get the coords for the destination
							int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

							// ok now set target
							try
							{
								Navit.remember_destination(Navit.NavitAddressResultList_foundItems.get(destination_id).addr, Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
								// save points
								write_map_points();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

							if (NavitGraphics.navit_route_status == 0)
							{
								Toast.makeText(getApplicationContext(), Navit.get_text("setting destination to") + "\n" + Navit.NavitAddressResultList_foundItems.get(destination_id).addr, Toast.LENGTH_LONG).show(); //TRANS
								Navit.destination_set();

								Message msg = new Message();
								Bundle b = new Bundle();
								b.putInt("Callback", 3);
								b.putString("lat", String.valueOf(Navit.NavitAddressResultList_foundItems.get(destination_id).lat));
								b.putString("lon", String.valueOf(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
								b.putString("q", Navit.NavitAddressResultList_foundItems.get(destination_id).addr);
								msg.setData(b);
								N_NavitGraphics.callback_handler.sendMessage(msg);
							}
							else
							{
								Toast.makeText(getApplicationContext(), Navit.get_text("new Waypoint") + "\n" + Navit.NavitAddressResultList_foundItems.get(destination_id).addr, Toast.LENGTH_LONG).show(); //TRANS
								Message msg = new Message();
								Bundle b = new Bundle();
								b.putInt("Callback", 48);
								b.putString("lat", String.valueOf(Navit.NavitAddressResultList_foundItems.get(destination_id).lat));
								b.putString("lon", String.valueOf(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
								b.putString("q", Navit.NavitAddressResultList_foundItems.get(destination_id).addr);
								msg.setData(b);
								N_NavitGraphics.callback_handler.sendMessage(msg);
							}

							// zoom_to_route();
							try
							{
								Thread.sleep(400);
							}
							catch (InterruptedException e)
							{
							}

							try
							{
								Navit.follow_button_on();
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}

							show_geo_on_screen(Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
						}
					}
					catch (NumberFormatException e)
					{
						Log.d("Navit", "NumberFormatException selected_id");
					}
					catch (Exception e)
					{

					}
				}
				else
				{
					// user pressed back key
				}
			}
			catch (Exception e)
			{
				Log.d("Navit", "error on onActivityResult");
				e.printStackTrace();
			}
			break;
		case NavitAddressSearch_id_gmaps:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{

				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case NavitGeoCoordEnter_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					// lat lon enter activitiy result

					try
					{
						if (data.getStringExtra("what").equals("view"))
						{
							// get the coords for the destination
							float lat = Float.parseFloat(data.getStringExtra("lat"));
							float lon = Float.parseFloat(data.getStringExtra("lon"));

							Log.d("Navit", "coord picker: " + lat);
							Log.d("Navit", "coord picker: " + lon);

							// set nice zoomlevel before we show destination
							int zoom_want = Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL;
							//
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 33);
							b.putString("s", Integer.toString(zoom_want));
							msg.setData(b);
							try
							{
								N_NavitGraphics.callback_handler.sendMessage(msg);
								Navit.GlobalScaleLevel = zoom_want;
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							if (PREF_save_zoomlevel)
							{
								setPrefs_zoomlevel();
							}
							// set nice zoomlevel before we show destination

							try
							{
								Navit.follow_button_off();
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}

							show_geo_on_screen(lat, lon);
						}
						else
						{
							// get the coords for the destination
							float lat = Float.parseFloat(data.getStringExtra("lat"));
							float lon = Float.parseFloat(data.getStringExtra("lat"));
							String dest_name = "manual coordinates";

							// ok now set target
							try
							{
								dest_name = NavitGraphics.CallbackGeoCalc(8, lat, lon);
								if ((dest_name.equals(" ")) || (dest_name == null))
								{
									dest_name = "manual coordinates";
								}
								Navit.remember_destination(dest_name, lat, lon);
								// save points
								write_map_points();
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
								b.putInt("Callback", 3);
								b.putString("lat", String.valueOf(lat));
								b.putString("lon", String.valueOf(lon));
								b.putString("q", dest_name);
								msg.setData(b);
								N_NavitGraphics.callback_handler.sendMessage(msg);
							}
							else
							{
								Message msg = new Message();
								Bundle b = new Bundle();
								b.putInt("Callback", 48);
								b.putString("lat", String.valueOf(lat));
								b.putString("lon", String.valueOf(lon));
								b.putString("q", dest_name);
								msg.setData(b);
								N_NavitGraphics.callback_handler.sendMessage(msg);
							}

							// zoom_to_route();
							try
							{
								Thread.sleep(400);
							}
							catch (InterruptedException e)
							{
							}

							try
							{
								Navit.follow_button_on();
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}

							show_geo_on_screen(lat, lon);
						}
					}
					catch (NumberFormatException e)
					{
						Log.d("Navit", "NumberFormatException selected_id");
					}
					catch (Exception e)
					{

					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case NavitRecentDest_id:
			try
			{
				if (resultCode == Activity.RESULT_OK)
				{
					Log.d("Navit", "recent dest id=" + Integer.parseInt(data.getStringExtra("selected_id")));
					// get the coords for the destination
					int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

					// ok now set target
					String dest_name = Navit.map_points.get(destination_id).point_name;
					float lat = Navit.map_points.get(destination_id).lat;
					float lon = Navit.map_points.get(destination_id).lon;

					if (NavitGraphics.navit_route_status == 0)
					{
						Toast.makeText(getApplicationContext(), Navit.get_text("setting destination to") + "\n" + dest_name, Toast.LENGTH_LONG).show(); //TRANS
						Navit.destination_set();

						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 3);
						b.putString("lat", String.valueOf(lat));
						b.putString("lon", String.valueOf(lon));
						b.putString("q", dest_name);
						msg.setData(b);
						N_NavitGraphics.callback_handler.sendMessage(msg);
					}
					else
					{
						Toast.makeText(getApplicationContext(), Navit.get_text("new Waypoint") + "\n" + dest_name, Toast.LENGTH_LONG).show(); //TRANS
						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 48);
						b.putString("lat", String.valueOf(lat));
						b.putString("lon", String.valueOf(lon));
						b.putString("q", dest_name);
						msg.setData(b);
						N_NavitGraphics.callback_handler.sendMessage(msg);
					}

					// zoom_to_route();
					try
					{
						Thread.sleep(400);
					}
					catch (InterruptedException e)
					{
					}

					try
					{
						Navit.follow_button_on();
					}
					catch (Exception e2)
					{
						e2.printStackTrace();
					}

					show_geo_on_screen(lat, lon);

				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		default:
			Log.e("Navit", "onActivityResult " + requestCode + " " + resultCode);
			try
			{
				ActivityResults[requestCode].onActivityResult(requestCode, resultCode, data);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		}
		Log.e("Navit", "onActivityResult finished");
	}

	public class WatchMem extends Thread
	{
		private Boolean running;

		WatchMem()
		{
			this.running = true;
		}

		public void run()
		{
			System.out.println("WatchMem -- started --");
			while (this.running)
			{
				Navit.show_mem_used();

				try
				{
					Thread.sleep(5000); // 5 secs.
				}
				catch (InterruptedException e)
				{
				}
			}
			System.out.println("WatchMem -- stopped --");
		}

		public void stop_me()
		{
			this.running = false;
		}
	}

	public class SimGPS extends Thread
	{
		private Boolean running;
		private Handler h;

		SimGPS(Handler h_)
		{
			System.out.println("SimGPS -- inited --");
			this.h = h_;
			this.running = true;
		}

		public void run()
		{
			System.out.println("SimGPS -- started --");
			while (this.running)
			{
				float rnd_heading = (float) (Math.random() * 360d);
				float lat = 48.216023f;
				float lng = 16.391664f;
				//Location l = new Location("Network");
				//l.setLatitude(lat);
				//l.setLongitude(lng);
				//l.setBearing(rnd_heading);
				// NavitVehicle.set_mock_location__fast(l);
				// NavitVehicle.update_compass_heading(rnd_heading);
				if (this.h != null)
				{
					Message msg = this.h.obtainMessage();
					Bundle b = new Bundle();
					msg.what = 1;
					b.putFloat("b", rnd_heading);
					b.putFloat("lat", lat);
					b.putFloat("lng", lng);
					msg.setData(b);
					this.h.sendMessage(msg);
				}
				try
				{
					Thread.sleep(800);
				}
				catch (InterruptedException e)
				{
				}
			}
			System.out.println("SimGPS -- stopped --");
		}

		public void stop_me()
		{
			this.running = false;
		}
	}

	public class SearchResultsThreadSpinnerThread extends Thread
	{
		int dialog_num;
		int spinner_current_value;
		private Boolean running;
		Handler mHandler;

		SearchResultsThreadSpinnerThread(Handler h, int dialog_num)
		{
			this.dialog_num = dialog_num;
			this.mHandler = h;
			this.spinner_current_value = 0;

			this.running = true;
			Log.e("Navit", "SearchResultsThreadSpinnerThread created");
		}

		public void run()
		{
			Log.e("Navit", "SearchResultsThreadSpinnerThread started");
			while (this.running)
			{
				if (Navit.NavitAddressSearchSpinnerActive == false)
				{
					this.running = false;
				}
				else
				{
					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					msg.what = 10;
					b.putInt("dialog_num", this.dialog_num);
					b.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
					b.putInt("cur", this.spinner_current_value % (Navit.ADDRESS_RESULTS_DIALOG_MAX + 1));
					if ((Navit.NavitSearchresultBar_title.equals("")) && (Navit.NavitSearchresultBar_text.equals("")))
					{
						b.putString("title", Navit.get_text("getting search results")); //TRANS
						b.putString("text", Navit.get_text("searching ...")); //TRANS
					}
					else
					{
						b.putString("title", Navit.NavitSearchresultBar_title);
						b.putString("text", Navit.NavitSearchresultBar_text);
					}
					msg.setData(b);
					mHandler.sendMessage(msg);
					try
					{
						Thread.sleep(700);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
					this.spinner_current_value++;
				}
			}
			Log.e("Navit", "SearchResultsThreadSpinnerThread ended");
		}
	}

	public class SearchResultsThread extends Thread
	{
		private Boolean running;
		Handler mHandler;
		int my_dialog_num;

		SearchResultsThread(Handler h, int dialog_num)
		{
			this.running = true;
			this.mHandler = h;
			this.my_dialog_num = dialog_num;
			Log.e("Navit", "SearchResultsThread created");
		}

		public void stop_me()
		{
			this.running = false;
		}

		public void run()
		{
			Log.e("Navit", "SearchResultsThread started");

			// initialize the dialog with sane values
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 10;
			b.putInt("dialog_num", this.my_dialog_num);
			b.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
			b.putInt("cur", 0);
			b.putString("title", Navit.get_text("getting search results")); //TRANS
			b.putString("text", Navit.get_text("searching ...")); //TRANS
			msg.setData(b);
			mHandler.sendMessage(msg);

			int partial_match_i = 0;
			if (Navit_last_address_partial_match)
			{
				partial_match_i = 1;
			}

			if (this.my_dialog_num == Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE)
			{
				// start the search, this could take a long time!!
				Log.e("Navit", "SearchResultsThread run1");
				// need lowercase to find stuff !!
				Navit_last_address_search_string = filter_bad_chars(Navit_last_address_search_string).toLowerCase();
				if (Navit_last_address_full_file_search)
				{
					// flags (18)		-> order level to search at
					// ================
					//   0#0   0		-> search full world
					// lat#lon radius	-> search only this area, around lat,lon
					// ================
					N_NavitGraphics.SearchResultList(3, partial_match_i, Navit_last_address_search_string, 18, Navit_last_address_search_country_iso2_string, "0#0", 0);
				}
				else
				{
					// flags --> 3: search all countries
					//           2: search <iso2 string> country
					//           1: search default country (what you have set as language in prefs)
					N_NavitGraphics.SearchResultList(2, partial_match_i, Navit_last_address_search_string, Navit_last_address_search_country_flags, Navit_last_address_search_country_iso2_string, "0#0", 0);
				}
				Log.e("Navit", "SearchResultsThread run2");
			}
			else if (this.my_dialog_num == Navit.SEARCHRESULTS_WAIT_DIALOG)
			{
				// online googlemaps search
				// google search
				Log.e("Navit", "SearchResultsThread run1 -> online googlemaps search");
				String addressInput = filter_bad_chars(Navit_last_address_search_string);
				try
				{
					List<Address> foundAdresses = Navit.Navit_Geocoder.getFromLocationName(addressInput, 30); //Search addresses
					System.out.println("found " + foundAdresses.size() + " results");
					// System.out.println("addr=" + foundAdresses.get(0).getLatitude() + " " + foundAdresses.get(0).getLongitude() + "" + foundAdresses.get(0).getAddressLine(0));

					Navit.NavitAddressSearchSpinnerActive = false;

					for (int results_step = 0; results_step < foundAdresses.size(); results_step++)
					{
						Navit.Navit_Address_Result_Struct tmp_addr = new Navit_Address_Result_Struct();
						tmp_addr.result_type = "STR";
						tmp_addr.item_id = "0";
						tmp_addr.lat = (float) foundAdresses.get(results_step).getLatitude();
						tmp_addr.lon = (float) foundAdresses.get(results_step).getLongitude();
						tmp_addr.addr = "";

						String c_code = foundAdresses.get(results_step).getCountryCode();
						if (c_code != null)
						{
							tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getCountryCode() + ",";
						}

						String p_code = foundAdresses.get(results_step).getPostalCode();
						if (p_code != null)
						{
							tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getPostalCode() + " ";
						}

						if (foundAdresses.get(results_step).getMaxAddressLineIndex() > -1)
						{
							for (int addr_line = 0; addr_line < foundAdresses.get(results_step).getMaxAddressLineIndex(); addr_line++)
							{
								if (addr_line > 0) tmp_addr.addr = tmp_addr.addr + " ";
								tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getAddressLine(addr_line);
							}
						}

						Navit.NavitAddressResultList_foundItems.add(tmp_addr);

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

						// make the dialog move its bar ...
						Bundle b2 = new Bundle();
						b2.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG);
						b2.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
						b2.putInt("cur", Navit.NavitAddressResultList_foundItems.size() % (Navit.ADDRESS_RESULTS_DIALOG_MAX + 1));
						b2.putString("title", Navit.get_text("loading search results")); //TRANS
						b2.putString("text", Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn);
						Navit.msg_to_msg_handler(b2, 10);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("seems googlemaps API is not working, try offline search");
				}
			}

			Navit.NavitAddressSearchSpinnerActive = false;

			if (Navit.NavitAddressResultList_foundItems.size() > 0)
			{
				open_search_result_list();
			}
			else
			{
				// not results found, show toast
				msg = mHandler.obtainMessage();
				b = new Bundle();
				msg.what = 3;
				b.putString("text", Navit.get_text("No Results found!")); //TRANS
				msg.setData(b);
				mHandler.sendMessage(msg);
			}

			// ok, remove dialog
			msg = mHandler.obtainMessage();
			b = new Bundle();
			msg.what = 99;
			b.putInt("dialog_num", this.my_dialog_num);
			msg.setData(b);
			mHandler.sendMessage(msg);

			// reset the startup-search flag
			Navit.NavitStartupAlreadySearching = false;

			Log.e("Navit", "SearchResultsThread ended");
		}
	}

	public static String filter_bad_chars(String in)
	{
		String out = in;
		out = out.replaceAll("\\n", " "); // newline -> space
		out = out.replaceAll("\\r", " "); // return -> space
		out = out.replaceAll("\\t", " "); // tab -> space
		return out;
	}

	public static void msg_to_msg_handler(Bundle b, int id)
	{
		Message msg = Navit_progress_h.obtainMessage();
		msg.what = id;
		msg.setData(b);
		Navit_progress_h.sendMessage(msg);
	}

	public void open_search_result_list()
	{
		// open result list
		Intent address_result_list_activity = new Intent(this, NavitAddressResultListActivity.class);
		this.startActivityForResult(address_result_list_activity, Navit.NavitAddressResultList_id);
	}

	public Handler progress_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 0:
				// dismiss dialog, remove dialog
				try
				{
					Log.e("Navit", "0: dismiss dialog num " + msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				dismissDialog(msg.getData().getInt("dialog_num"));
				removeDialog(msg.getData().getInt("dialog_num"));

				// exit_code=0 -> OK, map was downloaded fine
				if (msg.getData().getInt("exit_code") == 0)
				{
					// try to use the new downloaded map (works fine now!)
					//Log.d("Navit", "instance count=" + Navit.getInstanceCount()); // where did this go to?

					// **** onStop();
					// **** onCreate(getIntent().getExtras());

					// reload sdcard maps
					Message msg2 = new Message();
					Bundle b2 = new Bundle();
					b2.putInt("Callback", 18);
					msg2.setData(b2);
					N_NavitGraphics.callback_handler.sendMessage(msg2);

					zoom_out_full();

					/*
					 * Intent intent = getIntent();
					 * System.out.println("ÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜÜ**********************");
					 * startActivity(intent);
					 * System.out.println("FFFFFFFFFFFFFFFFFFF**********************");
					 * Log.d("Navit", "instance count=" + Navit.getInstanceCount());
					 * onStop();
					 * System.out.println("HHHHHHHHHHHHHHHHHHH**********************");
					 */

					//Message msg2 = new Message();
					//Bundle b2 = new Bundle();
					//b2.putInt("Callback", 6);
					//msg2.setData(b2);
					//N_NavitGraphics.callback_handler.sendMessage(msg2);
				}
				break;
			case 1:
				// change progressbar values
				int what_dialog = msg.getData().getInt("dialog_num");
				if (what_dialog == MAPDOWNLOAD_PRI_DIALOG)
				{
					mapdownloader_dialog_pri.setMax(msg.getData().getInt("max"));
					mapdownloader_dialog_pri.setProgress(msg.getData().getInt("cur"));
					mapdownloader_dialog_pri.setTitle(msg.getData().getString("title"));
					mapdownloader_dialog_pri.setMessage(msg.getData().getString("text"));
				}
				else if (what_dialog == MAPDOWNLOAD_SEC_DIALOG)
				{
					mapdownloader_dialog_sec.setMax(msg.getData().getInt("max"));
					mapdownloader_dialog_sec.setProgress(msg.getData().getInt("cur"));
					mapdownloader_dialog_sec.setTitle(msg.getData().getString("title"));
					mapdownloader_dialog_sec.setMessage(msg.getData().getString("text"));
				}
				break;
			case 2:
				Toast.makeText(getApplicationContext(), msg.getData().getString("text"), Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(getApplicationContext(), msg.getData().getString("text"), Toast.LENGTH_LONG).show();
				break;
			case 10:
				// change values - generic
				int what_dialog_generic = msg.getData().getInt("dialog_num");
				if (what_dialog_generic == SEARCHRESULTS_WAIT_DIALOG)
				{
					search_results_wait.setMax(msg.getData().getInt("max"));
					search_results_wait.setProgress(msg.getData().getInt("cur"));
					search_results_wait.setTitle(msg.getData().getString("title"));
					search_results_wait.setMessage(msg.getData().getString("text"));
				}
				else if (what_dialog_generic == SEARCHRESULTS_WAIT_DIALOG_OFFLINE)
				{
					search_results_wait_offline.setMax(msg.getData().getInt("max"));
					search_results_wait_offline.setProgress(msg.getData().getInt("cur"));
					search_results_wait_offline.setTitle(msg.getData().getString("title"));
					search_results_wait_offline.setMessage(msg.getData().getString("text"));
				}
				break;
			case 11:
				// show dialog - generic
				try
				{
					// just in case, remove the dialog if it should be shown already!
					dismissDialog(msg.getData().getInt("dialog_num"));
					removeDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					// System.out.println("Ex D1: " + e.toString());
				}
				showDialog(msg.getData().getInt("dialog_num"));
				break;
			case 12:
				// turn on compass
				turn_on_compass();
				break;
			case 13:
				// turn off compass
				turn_off_compass();
				break;
			case 14:
				// set used mem in textview
				show_mem_used_real();
				break;
			case 15:
				// set debug text line 3
				Navit.set_debug_messages3(msg.getData().getString("text"));
				break;
			case 16:
				// refresh NavitAndriodOverlay
				try
				{
					//Log.e("NavitGraphics", "xx 1");
					NavitGraphics.NavitAOverlay_s.invalidate();
					//Log.e("NavitGraphics", "xx 2");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 17:
				try
				{

					generic_alert_box.setMessage(Navit.get_text("Possibly not enough space on your device!")).setPositiveButton(Navit.get_text("Ok"), new DialogInterface.OnClickListener() // TRANS
							{
								public void onClick(DialogInterface dialog, int id)
								{
									// Handle Ok
								}
							}).create();
					generic_alert_box.setCancelable(false);
					generic_alert_box.setTitle(Navit.get_text("device space")); // TRANS
					generic_alert_box.show();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 99:
				// dismiss dialog, remove dialog - generic
				try
				{
					Log.e("Navit", "99: dismiss dialog num " + msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					dismissDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					removeDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
		}
	};

	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
		case Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE:
			search_results_wait_offline = new ProgressDialog(this);
			search_results_wait_offline.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			search_results_wait_offline.setTitle("--");
			search_results_wait_offline.setMessage("--");
			search_results_wait_offline.setCancelable(true); // allow to stop search
			search_results_wait_offline.setProgress(0);
			search_results_wait_offline.setMax(10);

			search_results_wait_offline.setOnCancelListener(new OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putInt("Callback", 46);
					msg.setData(b);
					try
					{
						N_NavitGraphics.callback_handler.sendMessage(msg);
					}
					catch (Exception e)
					{
					}
					Log.e("Navit", "onCancel: search_results_wait offline");
				}
			});

			/*
			 * search_results_wait.setButton("stop", new DialogInterface.OnClickListener()
			 * {
			 * public void onClick(DialogInterface dialog, int which)
			 * {
			 * // Use either finish() or return() to either close the activity or just the dialog
			 * return;
			 * }
			 * });
			 */

			DialogInterface.OnDismissListener mOnDismissListener4 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					Log.e("Navit", "onDismiss: search_results_wait offline");
					dialog.dismiss();
					dialog.cancel();
					searchresultsThread_offline.stop_me();
				}
			};
			search_results_wait_offline.setOnDismissListener(mOnDismissListener4);
			searchresultsThread_offline = new SearchResultsThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
			searchresultsThread_offline.start();

			NavitAddressSearchSpinnerActive = true;
			spinner_thread_offline = new SearchResultsThreadSpinnerThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
			spinner_thread_offline.start();

			return search_results_wait_offline;
		case Navit.SEARCHRESULTS_WAIT_DIALOG:
			search_results_wait = new ProgressDialog(this);
			search_results_wait.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			search_results_wait.setTitle("--");
			search_results_wait.setMessage("--");
			search_results_wait.setCancelable(false);
			search_results_wait.setProgress(0);
			search_results_wait.setMax(10);

			DialogInterface.OnDismissListener mOnDismissListener3 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					Log.e("Navit", "onDismiss: search_results_wait");
					dialog.dismiss();
					dialog.cancel();
					searchresultsThread.stop_me();
				}
			};
			search_results_wait.setOnDismissListener(mOnDismissListener3);
			searchresultsThread = new SearchResultsThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG);
			searchresultsThread.start();

			NavitAddressSearchSpinnerActive = true;
			spinner_thread = new SearchResultsThreadSpinnerThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG);
			spinner_thread.start();

			return search_results_wait;
		case Navit.MAPDOWNLOAD_PRI_DIALOG:
			mapdownloader_dialog_pri = new ProgressDialog(this);
			mapdownloader_dialog_pri.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mapdownloader_dialog_pri.setTitle("--");
			mapdownloader_dialog_pri.setMessage("--");
			mapdownloader_dialog_pri.setCancelable(true);
			mapdownloader_dialog_pri.setProgress(0);
			mapdownloader_dialog_pri.setMax(200);
			DialogInterface.OnDismissListener mOnDismissListener1 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					Log.e("Navit", "onDismiss: mapdownloader_dialog pri");
					dialog.dismiss();
					dialog.cancel();
					progressThread_pri.stop_thread();
				}
			};
			mapdownloader_dialog_pri.setOnDismissListener(mOnDismissListener1);
			mapdownloader_pri = new NavitMapDownloader(this);
			progressThread_pri = mapdownloader_pri.new ProgressThread(progress_handler, NavitMapDownloader.z_OSM_MAPS[Navit.download_map_id], MAP_NUM_PRIMARY);
			progressThread_pri.start();
			// show license for OSM maps
			//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
			Toast.makeText(getApplicationContext(), Navit.get_text("Map data (c) OpenStreetMap contributors, CC-BY-SA"), Toast.LENGTH_LONG).show(); //TRANS
			return mapdownloader_dialog_pri;
		case Navit.MAPDOWNLOAD_SEC_DIALOG:
			mapdownloader_dialog_sec = new ProgressDialog(this);
			mapdownloader_dialog_sec.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mapdownloader_dialog_sec.setTitle("--");
			mapdownloader_dialog_sec.setMessage("--");
			mapdownloader_dialog_sec.setCancelable(true);
			mapdownloader_dialog_sec.setProgress(0);
			mapdownloader_dialog_sec.setMax(200);
			DialogInterface.OnDismissListener mOnDismissListener2 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					Log.e("Navit", "onDismiss: mapdownloader_dialog sec");
					dialog.dismiss();
					dialog.cancel();
					progressThread_sec.stop_thread();
				}
			};
			mapdownloader_dialog_sec.setOnDismissListener(mOnDismissListener2);
			mapdownloader_sec = new NavitMapDownloader(this);
			progressThread_sec = mapdownloader_sec.new ProgressThread(progress_handler, NavitMapDownloader.z_OSM_MAPS[Navit.download_map_id], MAP_NUM_SECONDARY);
			progressThread_sec.start();
			// show license for OSM maps
			//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
			Toast.makeText(getApplicationContext(), Navit.get_text("Map data (c) OpenStreetMap contributors, CC-BY-SA"), Toast.LENGTH_LONG).show(); //TRANS
			return mapdownloader_dialog_sec;
		}
		// should never get here!!
		return null;
	}

	public void disableSuspend()
	{
		// wl.acquire();
		// wl.release();
	}

	public void exit2()
	{
		System.out.println("in exit2");
	}

	public void exit()
	{
		NavitVehicle.turn_off_all_providers();
		//try
		//{
		//	NavitSpeech.stop_me();
		//}
		//catch (Exception s)
		//{
		//	s.printStackTrace();
		//}

		try
		{
			NavitSpeech2.stop_me();
		}
		catch (Exception s)
		{
			s.printStackTrace();
		}
		Log.e("Navit", "1***************** exit called ****************");
		Log.e("Navit", "2***************** exit called ****************");
		Log.e("Navit", "3***************** exit called ****************");
		Log.e("Navit", "4***************** exit called ****************");
		Log.e("Navit", "5***************** exit called ****************");
		Log.e("Navit", "6***************** exit called ****************");
		Log.e("Navit", "7***************** exit called ****************");
		Log.e("Navit", "8***************** exit called ****************");
		System.gc();
		NavitActivity(-4);
		Log.e("Navit", "XX1***************** exit called ****************");
		finish();
		Log.e("Navit", "XX2***************** exit called ****************");
		System.runFinalizersOnExit(true);
		Log.e("Navit", "XX3***************** exit called ****************");
		System.exit(0);
		Log.e("Navit", "XX4***************** exit called ****************");
	}

	public boolean handleMessage(Message m)
	{
		//Log.e("Navit", "Handler received message");
		return true;
	}

	//public static void set_zanavi_revision_in_settings()
	//{
	//	// can it be that this is never used anymore??
	//
	//	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
	//	SharedPreferences.Editor editor = prefs.edit();
	//	// editor.commit();
	//}

	public static void follow_button_on()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		Navit.follow_current = Navit.follow_on;
		PREF_follow_gps = true;
		editor.putBoolean("follow_gps", PREF_follow_gps);
		editor.commit();

		// hold all map drawing -----------
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 69);
		msg.setData(b);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// hold all map drawing -----------

		getPrefs();
		activatePrefs(1);

		// allow all map drawing -----------
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", 70);
		msg.setData(b);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// allow all map drawing -----------

		NavitVehicle.set_last_known_pos_fast_provider();
	}

	public static void follow_button_off()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		Navit.follow_current = Navit.follow_off;
		PREF_follow_gps = false;
		editor.putBoolean("follow_gps", PREF_follow_gps);
		editor.commit();
		getPrefs();
		activatePrefs(1);
	}

	public static void toggle_follow_button()
	{
		// the red needle OSD call this function only!!
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		if (PREF_follow_gps)
		{
			Navit.follow_current = Navit.follow_off;
			PREF_follow_gps = false;
		}
		else
		{
			Navit.follow_current = Navit.follow_on;
			PREF_follow_gps = true;
		}
		editor.putBoolean("follow_gps", PREF_follow_gps);
		editor.commit();
		//if (!PREF_follow_gps)
		//{
		//	// no compass turning without follow mode!
		//	PREF_use_compass_heading_base = false;
		//}
		//if (!PREF_use_compass_heading_base)
		//{
		//	// child is always "false" when parent is "false" !!
		//	PREF_use_compass_heading_always = false;
		//}

		// hold all map drawing -----------
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 69);
		msg.setData(b);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// hold all map drawing -----------

		getPrefs();
		activatePrefs(1);

		// allow all map drawing -----------
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", 70);
		msg.setData(b);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// allow all map drawing -----------

		NavitVehicle.set_last_known_pos_fast_provider();
	}

	public static void setPrefs_search_country()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("search_country_id", PREF_search_country);
		editor.commit();
	}

	public static void setPrefs_zoomlevel()
	{
		System.out.println("1 save zoom level: " + Navit.GlobalScaleLevel);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("zoomlevel_num", Navit.GlobalScaleLevel);
		editor.commit();
		System.out.println("2 save zoom level: " + Navit.GlobalScaleLevel);
	}

	private static void getPrefs()
	{
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		PREF_use_fast_provider = prefs.getBoolean("use_fast_provider", true);
		PREF_allow_gui_internal = prefs.getBoolean("allow_gui_internal", false);
		PREF_follow_gps = prefs.getBoolean("follow_gps", true);
		PREF_use_compass_heading_base = prefs.getBoolean("use_compass_heading_base", false);
		PREF_use_compass_heading_always = prefs.getBoolean("use_compass_heading_always", false);
		PREF_use_compass_heading_fast = prefs.getBoolean("use_compass_heading_fast", false);
		PREF_use_anti_aliasing = prefs.getBoolean("use_anti_aliasing", true);
		PREF_gui_oneway_arrows = prefs.getBoolean("gui_oneway_arrows", true);
		PREF_show_debug_messages = prefs.getBoolean("show_debug_messages", false);
		PREF_show_3d_map = prefs.getBoolean("show_3d_map", false);
		PREF_use_lock_on_roads = prefs.getBoolean("use_lock_on_roads", true);
		PREF_use_route_highways = prefs.getBoolean("use_route_highways", true);
		PREF_save_zoomlevel = prefs.getBoolean("save_zoomlevel", true);
		PREF_search_country = prefs.getInt("search_country_id", 1); // default=*ALL*
		PREF_zoomlevel_num = prefs.getInt("zoomlevel_num", 2 * 2 * 2 * 2 * 2);
		PREF_show_sat_status = prefs.getBoolean("show_sat_status", false);
		PREF_use_agps = prefs.getBoolean("use_agps", true);
		PREF_enable_debug_functions = prefs.getBoolean("enable_debug_functions", false);
		PREF_speak_street_names = prefs.getBoolean("speak_street_names", true);
		PREF_use_custom_font = prefs.getBoolean("use_custom_font", true);
		PREF_draw_polyline_circles = prefs.getBoolean("draw_polyline_circles", true);
		PREF_streetsearch_r = prefs.getString("streetsearch_r", "2");
		PREF_route_style = prefs.getString("route_style", "2");
		PREF_item_dump = prefs.getBoolean("item_dump", false);

		try
		{
			PREF_drawatorder = Integer.parseInt(prefs.getString("drawatorder", "0"));
		}
		catch (Exception e)
		{
			PREF_drawatorder = 0;
		}

		try
		{
			PREF_cancel_map_drawing_timeout = Integer.parseInt(prefs.getString("cancel_map_drawing_timeout", "1"));
		}
		catch (Exception e)
		{
			PREF_cancel_map_drawing_timeout = 1;
		}

		try
		{
			PREF_map_font_size = Integer.parseInt(prefs.getString("map_font_size", "2"));
		}
		catch (Exception e)
		{
			PREF_map_font_size = 2;
		}

		Navit_last_address_search_country_id = PREF_search_country;
		Navit_last_address_search_country_iso2_string = NavitAddressSearchCountrySelectActivity.CountryList_Human[PREF_search_country][0];

		if (!PREF_follow_gps)
		{
			// no compass turning without follow mode!
			PREF_use_compass_heading_base = false;
		}

		if (!PREF_use_compass_heading_base)
		{
			// child is always "false" when parent is "false" !!
			PREF_use_compass_heading_always = false;
		}
		PREF_show_vehicle_in_center = prefs.getBoolean("show_vehicle_in_center", false);
		PREF_use_imperial = prefs.getBoolean("use_imperial", false);

		//		System.out.println("get settings");
		//      System.out.println("PREF_search_country=" + PREF_search_country);
		//		System.out.println("PREF_follow_gps=" + PREF_follow_gps);
		//		System.out.println("PREF_use_fast_provider=" + PREF_use_fast_provider);
		//		System.out.println("PREF_allow_gui_internal=" + PREF_allow_gui_internal);
		//		System.out.println("PREF_use_compass_heading_base=" + PREF_use_compass_heading_base);
		//		System.out.println("PREF_use_compass_heading_always=" + PREF_use_compass_heading_always);
		//		System.out.println("PREF_show_vehicle_in_center=" + PREF_show_vehicle_in_center);
		//		System.out.println("PREF_use_imperial=" + PREF_use_imperial);
	}

	private static void activatePrefs()
	{
		activatePrefs(1);

		if (PREF_save_zoomlevel)
		{
			// only if really started, but NOT if returning from our own child activities!!

			System.out.println("3 restore zoom level: " + Navit.GlobalScaleLevel);
			System.out.println("4 restore zoom level: " + PREF_zoomlevel_num);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 33);
			b.putString("s", Integer.toString(PREF_zoomlevel_num));
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
				Navit.GlobalScaleLevel = PREF_zoomlevel_num;
				System.out.println("5 restore zoom level: " + PREF_zoomlevel_num);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			PREF_zoomlevel_num = Navit.GlobalScaleLevel;
		}
	}

	private static void activatePrefs(int dummy)
	{
		// call some functions to activate the new settings
		if (PREF_follow_gps)
		{
			Navit.follow_current = Navit.follow_on;
		}
		else
		{
			Navit.follow_current = Navit.follow_off;
		}

		if (PREF_use_fast_provider)
		{
			NavitVehicle.turn_on_fast_provider();
		}
		else
		{
			NavitVehicle.turn_off_fast_provider();
		}

		if (PREF_show_sat_status)
		{
			NavitVehicle.turn_on_sat_status();
		}
		else
		{
			// status always on !
			//
			// NavitVehicle.turn_off_sat_status();
			NavitVehicle.turn_on_sat_status();
		}

		if (PREF_allow_gui_internal)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 10);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 9);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (PREF_use_compass_heading_base)
		{
			// turn on compass
			msg_to_msg_handler(new Bundle(), 12);
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 11);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			// turn off compass
			msg_to_msg_handler(new Bundle(), 13);
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 12);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (PREF_show_vehicle_in_center)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 14);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 13);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (PREF_use_imperial)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 16);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 15);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (PREF_show_debug_messages)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 24);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 25);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (PREF_show_3d_map)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 31);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 30);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (PREF_use_lock_on_roads)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 36);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 37);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		//		if (PREF_draw_polyline_circles)
		//		{
		//			Message msg = new Message();
		//			Bundle b = new Bundle();
		//			b.putString("s", "0");
		//			b.putInt("Callback", 56);
		//			msg.setData(b);
		//			try
		//			{
		//				N_NavitGraphics.callback_handler.sendMessage(msg);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//		}
		//		else
		//		{
		//			Message msg = new Message();
		//			Bundle b = new Bundle();
		//			b.putString("s", "1");
		//			b.putInt("Callback", 56);
		//			msg.setData(b);
		//			try
		//			{
		//				N_NavitGraphics.callback_handler.sendMessage(msg);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//		}

		if (PREF_use_route_highways)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 42);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 43);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		Message msg7 = new Message();
		Bundle b7 = new Bundle();
		b7.putInt("Callback", 57);
		b7.putString("s", "" + PREF_drawatorder);
		msg7.setData(b7);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}

		msg7 = new Message();
		b7 = new Bundle();
		b7.putInt("Callback", 58);
		b7.putString("s", PREF_streetsearch_r);
		msg7.setData(b7);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}

		if (PREF_speak_street_names)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 54);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 53);
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}

		}

		try
		{
			NavitGraphics.OverlayDrawThread_cancel_drawing_timeout = NavitGraphics.OverlayDrawThread_cancel_drawing_timeout__options[PREF_cancel_map_drawing_timeout];
			NavitGraphics.OverlayDrawThread_cancel_thread_sleep_time = NavitGraphics.OverlayDrawThread_cancel_thread_sleep_time__options[PREF_cancel_map_drawing_timeout];
			NavitGraphics.OverlayDrawThread_cancel_thread_timeout = NavitGraphics.OverlayDrawThread_cancel_thread_timeout__options[PREF_cancel_map_drawing_timeout];
		}
		catch (Exception e)
		{

		}

		// route variant
		Message msg67 = new Message();
		Bundle b67 = new Bundle();
		// turn off 1
		b67.putInt("Callback", 60);
		b67.putString("s", "route_001");
		msg67.setData(b67);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// turn off 2
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 60);
		b67.putString("s", "route_002");
		msg67.setData(b67);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}

		// turn on the wanted route style
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 59);
		b67.putString("s", "route_00" + PREF_route_style);
		msg67.setData(b67);
		try
		{
			N_NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// route variant

		// set vars for mapdir change (only really takes effect after restart!)
		getPrefs_mapdir();
	}

	private static void getPrefs_mapdir()
	{
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		String default_sdcard_dir = Environment.getExternalStorageDirectory().getAbsolutePath();
		Log.e("Navit", "old sdcard dir=" + NavitDataDirectory_Maps);
		Log.e("Navit", "default sdcard dir=" + default_sdcard_dir);
		NavitDataDirectory_Maps = prefs.getString("map_directory", default_sdcard_dir + "/zanavi/maps/");
		// ** DEBUG ** NavitDataDirectory_Maps = prefs.getString("navit_mapsdir", "/sdcard" + "/zanavi/maps/");
		Log.e("Navit", "new sdcard dir=" + NavitDataDirectory_Maps);
	}

	static String sanity_check_maps_dir(String check_dir)
	{
		String ret = check_dir;
		ret = ret.replaceAll("\\n", ""); // newline -> ""
		ret = ret.replaceAll("\\r", ""); // return -> ""
		ret = ret.replaceAll("\\t", ""); // tab -> ""
		ret = ret.replaceAll(" ", ""); // space -> ""
		ret = ret.replaceAll("\"", ""); // \" -> ""
		ret = ret.replaceAll("'", ""); // \' -> ""
		ret = ret.replaceAll("\\\\", ""); // "\" -> ""
		if (!ret.endsWith("/"))
		{
			ret = ret + "/";
		}
		System.out.println("sanity check:" + ret);
		return ret;
	}

	private static void activatePrefs_mapdir(Boolean at_startup)
	{
		// activate the new directory
		NavitDataDirectory_Maps = sanity_check_maps_dir(NavitDataDirectory_Maps);
		MAP_FILENAME_PATH = NavitDataDirectory_Maps;
		MAPMD5_FILENAME_PATH = NavitDataDirectory_Maps + "/../md5/";
		CFG_FILENAME_PATH = NavitDataDirectory_Maps + "/../";

		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");
		System.out.println("xxxxxxxx************XXXXXXXXXXX");

		Handler h_temp = null;
		h_temp = NavitGraphics.callback_handler_s;
		System.out.println("handler 1=" + h_temp.toString());

		Message msg1 = new Message();
		Bundle b1 = new Bundle();
		b1.putInt("Callback", 47);
		b1.putString("s", MAP_FILENAME_PATH);
		msg1.setData(b1);
		h_temp.sendMessage(msg1);

		if (!at_startup)
		{
			Message msg2 = new Message();
			Bundle b2 = new Bundle();
			b2.putInt("Callback", 18);
			msg2.setData(b2);
			h_temp.sendMessage(msg2);
		}
	}

	private static void getPrefs_loc()
	{
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		PREF_navit_lang = prefs.getString("navit_lang", "*DEFAULT*");
		System.out.println("**** ***** **** pref lang=" + PREF_navit_lang);
	}

	private static void activatePrefs_loc()
	{
		// creating locale
		if (!PREF_navit_lang.equals("*DEFAULT*"))
		{
			Locale locale2 = null;
			if (PREF_navit_lang.contains("_"))
			{
				String _lang = PREF_navit_lang.split("_", 2)[0];
				String _country = PREF_navit_lang.split("_", 2)[1];
				System.out.println("l=" + _lang + " c=" + _country);
				locale2 = new Locale(_lang, _country);
			}
			else
			{
				locale2 = new Locale(PREF_navit_lang);
			}
			Locale.setDefault(locale2);
			Configuration config2 = new Configuration();
			config2.locale = locale2;
			// updating locale
			getBaseContext_.getResources().updateConfiguration(config2, null);
		}
	}

	private static void getPrefs_mapcache()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		try
		{
			PREF_mapcache = Integer.parseInt(prefs.getString("mapcache", "" + (10 * 1024)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PREF_mapcache = 10 * 1024;
		}
		System.out.println("**** ***** **** pref mapcache=" + PREF_mapcache);
	}

	private static void activatePrefs_mapcache()
	{
		Handler h_temp2 = null;
		h_temp2 = NavitGraphics.callback_handler_s;
		Message msg7 = new Message();
		Bundle b7 = new Bundle();
		b7.putInt("Callback", 55);
		b7.putString("s", String.valueOf(PREF_mapcache * 1024));
		msg7.setData(b7);
		h_temp2.sendMessage(msg7);
	}

	public native void NavitMain(Navit x, String lang, int version, String display_density_string);

	public native void NavitActivity(int activity);

	/*
	 * this is used to load the 'navit' native library on
	 * application startup. The library has already been unpacked at
	 * installation time by the package manager.
	 */
	static
	{
		System.loadLibrary("navit");
	}

	/*
	 * Show a search activity with the string "search" filled in
	 */
	private void executeSearch(String search)
	{
		Intent search_intent = new Intent(this, NavitAddressSearchActivity.class);
		search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
		search_intent.putExtra("address_string", search);
		search_intent.putExtra("type", "offline");
		search_intent.putExtra("search_country_id", Navit_last_address_search_country_id);
		String pm_temp = "0";
		if (Navit_last_address_partial_match)
		{
			pm_temp = "1";
		}
		search_intent.putExtra("partial_match", pm_temp);
		this.startActivityForResult(search_intent, NavitAddressSearch_id_offline);
	}

	/*
	 * open google maps at a given coordinate
	 */
	private void googlemaps_show(String lat, String lon, String name)
	{
		// geo:latitude,longitude
		String url = null;
		Intent gmaps_intent = new Intent(Intent.ACTION_VIEW);

		//url = "geo:" + lat + "," + lon + "?z=" + "16";
		//url = "geo:0,0?q=" + lat + "," + lon + " (" + name + ")";
		url = "geo:0,0?z=16&q=" + lat + "," + lon + " (" + name + ")";

		gmaps_intent.setData(Uri.parse(url));
		this.startActivityForResult(gmaps_intent, NavitAddressSearch_id_gmaps);
	}

	public void zoom_out_full()
	{
		System.out.println("");
		System.out.println("*** Zoom out FULL ***");
		System.out.println("");
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 8);
		msg.setData(b);
		N_NavitGraphics.callback_handler.sendMessage(msg);
	}

	public void show_geo_on_screen(float lat, float lng)
	{
		// -> let follow mode stay as it now is  ### Navit.follow_button_off();

		// this function sets screen center to "lat, lon", and just returns a dummy string!
		String nix = NavitGraphics.CallbackGeoCalc(3, lat, lng);
	}

	public void zoom_to_route()
	{
		//System.out.println("");
		//System.out.println("*** Zoom to ROUTE ***");
		//System.out.println("");
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 17);
		msg.setData(b);
		N_NavitGraphics.callback_handler.sendMessage(msg);
	}

	public void turn_on_compass()
	{
		try
		{
			if (!PREF_use_compass_heading_fast)
			{
				// Slower
				sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
			}
			else
			{
				// FAST
				sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void turn_off_compass()
	{
		try
		{
			sensorManager.unregisterListener(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onSensorChanged(SensorEvent event)
	{

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			// System.out.println("Sensor.TYPE_MAGNETIC_FIELD");
			return;
		}

		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
			// System.out.println("Sensor.TYPE_ORIENTATION");

			// compass
			float myAzimuth = event.values[0];
			// double myPitch = event.values[1];
			// double myRoll = event.values[2];

			//String out = String.format("Azimuth: %.2f", myAzimuth);
			//System.out.println("compass: " + out);
			NavitVehicle.update_compass_heading(myAzimuth);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// compass
	}

	public void hide_status_bar()
	{
		// Hide the Status Bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void show_status_bar()
	{
		// Hide the Status Bar
		// ??? getWindow().setFlags(0, 0);
	}

	public void hide_title_bar()
	{
		// Hide the Title Bar - this works ONLY before setcontent in onCreate!!
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void show_title_bar()
	{
		// Hide the Title Bar - this works ONLY before setcontent in onCreate!!
		// ?? requestWindowFeature(Window.);
	}

	public static Boolean downloadGPSXtra(Context context)
	{
		Boolean ret = false;
		Boolean ret2 = false;
		try
		{
			LocationManager locationmanager2 = (LocationManager) context.getSystemService("location");
			Bundle bundle = new Bundle();
			//ret2 = locationmanager2.sendExtraCommand("gps", "delete_aiding_data", null);
			//ret = ret2;
			// System.out.println("ret0=" + ret);
			ret2 = locationmanager2.sendExtraCommand("gps", "force_xtra_injection", bundle);
			ret = ret2;
			System.out.println("ret1=" + ret2);
			ret2 = locationmanager2.sendExtraCommand("gps", "force_time_injection", bundle);
			ret = ret || ret2;
			System.out.println("ret2=" + ret2);
		}
		catch (Exception e)
		{
			System.out.println("*XX*");
			e.printStackTrace();
		}
		return ret;
	}

	static void add_map_point(Navit_Point_on_Map element)
	{
		if (element == null)
		{
			return;
		}
		if (map_points == null)
		{
			map_points = new ArrayList<Navit_Point_on_Map>();
		}
		if (map_points.size() > Navit_MAX_RECENT_DESTINATIONS)
		{
			try
			{
				map_points.remove(0);
			}
			catch (Exception e)
			{

			}
		}

		if (!check_dup_destination(element))
		{
			// if not duplicate, then add
			map_points.add(element);
		}
	}

	void read_map_points()
	{
		deserialize_map_points();
	}

	static void write_map_points()
	{
		if (map_points != null)
		{
			serialize_map_points();
		}
	}

	private static void serialize_map_points()
	{
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(CFG_FILENAME_PATH + Navit_DEST_FILENAME);
			// openFileOutput(CFG_FILENAME_PATH + Navit_DEST_FILENAME, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(map_points);
			oos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void deserialize_map_points()
	{
		try
		{
			FileInputStream fis = new FileInputStream(CFG_FILENAME_PATH + Navit_DEST_FILENAME);
			// openFileInput(CFG_FILENAME_PATH + Navit_DEST_FILENAME);
			ObjectInputStream ois = new ObjectInputStream(fis);
			map_points = (ArrayList<Navit_Point_on_Map>) ois.readObject();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			map_points = new ArrayList<Navit_Point_on_Map>();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			map_points = new ArrayList<Navit_Point_on_Map>();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			map_points = new ArrayList<Navit_Point_on_Map>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			map_points = new ArrayList<Navit_Point_on_Map>();
		}

		//		for (int j = 0; j < map_points.size(); j++)
		//		{
		//			System.out.println("####******************" + j + ":" + map_points.get(j).point_name);
		//		}
	}

	static void remember_destination_xy(String name, int x, int y)
	{
		// i=1 -> pixel a,b (x,y)      -> geo   string "lat(float):lng(float)"
		// i=2 -> geo   a,b (lat,lng)  -> pixel string "x(int):y(int)"
		String lat_lon = NavitGraphics.CallbackGeoCalc(1, x, y);
		try
		{
			String tmp[] = lat_lon.split(":", 2);
			//System.out.println("tmp=" + lat_lon);
			float lat = Float.parseFloat(tmp[0]);
			float lon = Float.parseFloat(tmp[1]);
			//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
			remember_destination(name, lat, lon);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void remember_destination(String name, String lat, String lon)
	{
		try
		{
			//System.out.println("22 **## " + name + " " + lat + " " + lon + " ##**");
			remember_destination(name, Float.parseFloat(lat), Float.parseFloat(lon));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void remember_destination(String name, float lat, float lon)
	{
		//System.out.println("11 **## " + name + " " + lat + " " + lon + " ##**");
		Navit_Point_on_Map t = new Navit_Point_on_Map();
		t.point_name = name;
		t.lat = lat;
		t.lon = lon;
		add_map_point(t);
	}

	static void destination_set()
	{
		// status = "destination set"
		NavitGraphics.navit_route_status = 1;
	}

	static Boolean check_dup_destination(Navit_Point_on_Map element)
	{
		Boolean ret = false;
		Navit_Point_on_Map t;
		for (int i = 0; i < map_points.size(); i++)
		{
			t = map_points.get(i);
			if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon))
			{
				return true;
			}
		}
		return ret;
	}

}
