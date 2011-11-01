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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zoffcc.applications.zanavi.NavitMapDownloader.ProgressThread;

public class Navit extends Activity implements Handler.Callback, SensorEventListener
{
	public static final String VERSION_TEXT_LONG_INC_REV = "v1.0.0-850";
	public static String NavitAppVersion = "0";
	public static String NavitAppVersion_prev = "-1";
	public static String NavitAppVersion_string = "0";
	private Boolean xmlconfig_unpack_file = true;
	private Boolean write_new_version_file = true;

	// for future use ...
	public static String NavitDataDirectory = "/sdcard/";

	public static int GlobalScaleLevel = 0;

	public static final class Navit_Address_Result_Struct
	{
		String result_type; // TWN,STR,SHN
		String item_id; // H<ddddd>L<ddddd> -> item.id_hi item.id_lo
		float lat;
		float lon;
		String addr;
	}

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
	public static int download_map_id = 0;
	ProgressThread progressThread_pri = null;
	ProgressThread progressThread_sec = null;
	public static int search_results_towns = 0;
	public static int search_results_streets = 0;
	public static int search_results_streets_hn = 0;
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
	public static List<Navit_Address_Result_Struct> NavitAddressResultList_foundItems = new ArrayList<Navit_Address_Result_Struct>();

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
	public static Bitmap onway_arrow = null;

	public static String Navit_last_address_search_string = "";
	public static Boolean Navit_last_address_full_file_search = false;
	public static String Navit_last_address_search_country_iso2_string = "";
	public static int Navit_last_address_search_country_flags = 3;
	public static int Navit_last_address_search_country_id = 0;
	public static Boolean Navit_last_address_partial_match = false;
	public static Geocoder Navit_Geocoder = null;
	public static String UserAgentString = null;
	public static Boolean first_ever_startup = false;

	public static Boolean Navit_Announcer = true;

	public static final int MAP_NUM_SECONDARY = 12;
	static final String MAP_FILENAME_PATH = "/sdcard/zanavi/maps/";
	static final String MAPMD5_FILENAME_PATH = "/sdcard/zanavi/md5/";
	static final String CFG_FILENAME_PATH = "/sdcard/zanavi/";
	static final String NAVIT_DATA_DIR = "/data/data/com.zoffcc.applications.zanavi";
	static final String NAVIT_DATA_SHARE_DIR = NAVIT_DATA_DIR + "/share";
	static final String FIRST_STARTUP_FILE = NAVIT_DATA_SHARE_DIR + "/has_run_once.txt";
	static final String VERSION_FILE = NAVIT_DATA_SHARE_DIR + "/version.txt";

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
		if (id == 0) return false;
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
		if (!resultfile.exists()) needs_update = true;
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

		// only take arguments here, onResume gets called all the time (e.g. when screenblanks, etc.)
		Navit.startup_intent = this.getIntent();
		// hack! remeber timstamp, and only allow 4 secs. later in onResume to set target!
		Navit.startup_intent_timestamp = System.currentTimeMillis();
		Log.e("Navit", "**1**A " + startup_intent.getAction());
		Log.e("Navit", "**1**D " + startup_intent.getDataString());

		// init translated text
		NavitTextTranslations.init();

		// set the new locale here -----------------------------------
		getPrefs_loc();
		activatePrefs_loc();
		// set the new locale here -----------------------------------

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
		File navit_maps_dir = new File(MAP_FILENAME_PATH);
		navit_maps_dir.mkdirs();

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
			// only on complete new start (cat file doesnt yet exist)
			// when user puts old files in dir later on, its users problem
			// because we dont want to overwrite new/good map files in new mapdir
			File old_map1 = new File(CFG_FILENAME_PATH + "navitmap.bin");
			File old_map2 = new File(CFG_FILENAME_PATH + "navitmap_002.bin");
			if (old_map1.exists())
			{
				// move it to new dir
				old_map1.renameTo(new File(MAP_FILENAME_PATH + "/navitmap_001.bin"));
			}
			if (old_map2.exists())
			{
				// move it to new dir
				old_map2.renameTo(new File(MAP_FILENAME_PATH + "/navitmap_002.bin"));
			}

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

		NavitMapDownloader.MULTI_NUM_THREADS = 1;
		// try to create cat. file if it does not exist
		File navit_secret_file = new File(CFG_FILENAME_PATH + "z.txt");
		if (navit_secret_file.exists())
		{
			NavitMapDownloader.MULTI_NUM_THREADS = 6;
		}

		Navit.follow_on = BitmapFactory.decodeResource(getResources(), R.drawable.follow);
		Navit.follow_off = BitmapFactory.decodeResource(getResources(), R.drawable.follow_off);
		Navit.follow_current = Navit.follow_on;

		Navit.zoomin = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_in_32_32);
		Navit.zoomout = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_out_32_32);

		Navit.onway_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway);

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
		String android_rom_name = DISPLAY;
		UserAgentString = "Mozilla/5.0 (Linux; U; " + "Z" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
		System.out.println("UA=" + UserAgentString);

		try
		{
			sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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

		/*
		 * show info box for first time users
		 */
		AlertDialog.Builder infobox = new AlertDialog.Builder(this);
		infobox.setTitle(Navit.get_text("__INFO_BOX_TITLE__")); //TRANS
		infobox.setCancelable(false);
		final TextView message = new TextView(this);
		message.setFadingEdgeLength(20);
		message.setVerticalFadingEdgeEnabled(true);
		message.setPadding(10, 5, 10, 5);
		//message.setVerticalScrollBarEnabled(true);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		// margins seem not to work, hmm strange
		// so add a " " at start of every line. well whadda you gonna do ...
		//rlp.leftMargin = 8; -> we use "m" string

		message.setLayoutParams(rlp);
		final SpannableString s = new SpannableString(" " + Navit.get_text("__INFO_BOX_TEXT__")); //TRANS
		Linkify.addLinks(s, Linkify.WEB_URLS);
		message.setText(s);
		message.setMovementMethod(LinkMovementMethod.getInstance());
		infobox.setView(message);

		// for online search
		Navit.Navit_Geocoder = new Geocoder(this);

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
			FileOutputStream fos_temp;
			try
			{
				fos_temp = new FileOutputStream(navit_first_startup);
				fos_temp.write((int) 65); // just write an "A" to the file, but really doesnt matter
				fos_temp.flush();
				fos_temp.close();
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

		ActivityResults = new NavitActivityResult[16];
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");

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

		// --> dont use!! NavitMain(this, langu, android.os.Build.VERSION.SDK_INT);
		Log.e("Navit", "android.os.Build.VERSION.SDK_INT=" + Integer.valueOf(android.os.Build.VERSION.SDK));
		NavitMain(this, langu, Integer.valueOf(android.os.Build.VERSION.SDK), my_display_density);
		// CAUTION: don't use android.os.Build.VERSION.SDK_INT if <uses-sdk android:minSdkVersion="3" />
		// You will get exception on all devices with Android 1.5 and lower
		// because Build.VERSION.SDK_INT is since SDK 4 (Donut 1.6)

		//		Platform Version   API Level
		//      Android 3.1         12
		//      Android 3.0         11
		//      Android 2.3.3       10
		//      Android 2.3.1        9
		//		Android 2.2          8
		//		Android 2.1          7
		//		Android 2.0.1        6
		//		Android 2.0          5
		//		Android 1.6          4
		//		Android 1.5          3
		//		Android 1.1          2
		//		Android 1.0          1

		NavitActivity(3);

		Navit.mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		getPrefs();
		activatePrefs();

		// unpack some localized Strings
		// a test now, later we will unpack all needed strings for java, here at this point!!
		//String x = NavitGraphics.getLocalizedString("Austria");
		//Log.e("Navit", "x=" + x);
		Navit.show_mem_used();

		GpsStatus.Listener listener = new GpsStatus.Listener()
		{
			public void onGpsStatusChanged(int event)
			{
				System.out.println("xxxxx");
				if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
				{
				}
			}
		};
	}

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
			NavitGraphics.NavitMsgTv_.setMaxLines(3);
			NavitGraphics.NavitMsgTv_.setLines(3);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3);
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
			NavitGraphics.NavitMsgTv_.setMaxLines(3);
			NavitGraphics.NavitMsgTv_.setLines(3);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3);
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
			NavitGraphics.NavitMsgTv_.setMaxLines(3);
			NavitGraphics.NavitMsgTv_.setLines(3);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3);
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
			NavitGraphics.NavitMsgTv_.setMaxLines(3);
			NavitGraphics.NavitMsgTv_.setLines(3);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3);
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

		//Intent caller = this.getIntent();
		//System.out.println("A=" + caller.getAction() + " D=" + caller.getDataString());
		//System.out.println("C=" + caller.getComponent().flattenToString());

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

			// d: google.navigation:q=blabla-strasse # (this happens when you are offline, or from contacts)
			// b: google.navigation:q=48.25676,16.643
			// a: google.navigation:ll=48.25676,16.643&q=blabla-strasse
			// e: google.navigation:ll=48.25676,16.643&title=blabla-strasse
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

			if (intent_data.length() > 19)
			{
				// if d: then start target search
				if ((intent_data.substring(0, 20).equals("google.navigation:q=")) && ((!intent_data.substring(20, 21).equals('+')) && (!intent_data.substring(20, 21).equals('-')) && (!intent_data.substring(20, 22).matches("[0-9][0-9]"))))
				{
					Log.e("Navit", "target found (d): " + intent_data.split("q=", -1)[1]);
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

				System.out.println();

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

					searchBoxShown = true;
					String searchString = intent_data.split("q=")[1];
					searchString = searchString.split("&")[0];
					searchString = URLDecoder.decode(searchString); // decode the URL: e.g. %20 -> space
					Log.e("Navit", "Search String :" + searchString);
					executeSearch(searchString);

				}
			}
		}

		getPrefs();
		activatePrefs();
		// activate gps AFTER 3g-location
		NavitVehicle.turn_on_precise_provider();
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
		}
		menu.add(1, 99, 480, get_text("exit navit")); //TRANS
		menu.add(1, 5, 485, get_text("toggle POI")); //TRANS
		menu.add(1, 10, 490, get_text("Settings")); //TRANS

		if (Navit_Announcer == true)
		{
			menu.add(1, 12, 496, get_text("Announcer Off")); //TRANS
		}
		else
		{
			menu.add(1, 13, 495, get_text("Announcer On")); //TRANS
		}

		menu.add(1, 3, 500, get_text("download maps")); //TRANS
		menu.add(1, 8, 505, get_text("delete maps")); //TRANS

		// menu.add(1, 88, 800, "--");
		return true;
	}

	// define graphics here
	public static NavitGraphics N_NavitGraphics = null;

	// callback id gets set here when called from NavitGraphics
	public static void setKeypressCallback(int kp_cb_id, NavitGraphics ng)
	{
		//Log.e("Navit", "setKeypressCallback -> id1=" + kp_cb_id);
		//Log.e("Navit", "setKeypressCallback -> ng=" + String.valueOf(ng));
		//N_KeypressCallbackID = kp_cb_id;
		N_NavitGraphics = ng;
	}

	public static void setMotionCallback(int mo_cb_id, NavitGraphics ng)
	{
		//Log.e("Navit", "setKeypressCallback -> id2=" + mo_cb_id);
		//Log.e("Navit", "setKeypressCallback -> ng=" + String.valueOf(ng));
		//N_MotionCallbackID = mo_cb_id;
		N_NavitGraphics = ng;
	}

	//public native void KeypressCallback(int id, String s);

	public void start_targetsearch_from_intent(String target_address)
	{
		Navit_last_address_partial_match = false;
		Navit_last_address_search_string = target_address;

		// clear results
		Navit.NavitAddressResultList_foundItems.clear();
		Navit.Navit_Address_Result_double_index.clear();

		if (Navit_last_address_search_string.equals(""))
		{
			// empty search string entered
			Toast.makeText(getApplicationContext(), Navit.get_text("No address found"), Toast.LENGTH_LONG).show(); //TRANS
		}
		else
		{
			// show dialog
			Message msg = progress_handler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 11;
			b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG);
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

			// TEST TEST // googlemaps_show("48.28696", "16.48816", "XXyy ö2432ä ä3");

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
		case 88:
			// dummy entry, just to make "breaks" in the menu
			break;
		case 99:
			// exit
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.e("Navit", "onActivityResult");
		switch (requestCode)
		{
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
						Log.d("Navit", "adress result list id=" + Integer.parseInt(data.getStringExtra("selected_id")));
						// get the coords for the destination
						int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

						// ok now set target
						Toast.makeText(getApplicationContext(), Navit.get_text("setting destination to") + "\n" + Navit.NavitAddressResultList_foundItems.get(destination_id).addr, Toast.LENGTH_LONG).show(); //TRANS

						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 3);
						b.putString("lat", String.valueOf(Navit.NavitAddressResultList_foundItems.get(destination_id).lat));
						b.putString("lon", String.valueOf(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
						b.putString("q", Navit.NavitAddressResultList_foundItems.get(destination_id).addr);
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
							Navit.follow_button_on();
						}
						catch (Exception e2)
						{
							e2.printStackTrace();
						}

						show_geo_on_screen(Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
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
		default:
			Log.e("Navit", "onActivityResult " + requestCode + " " + resultCode);
			ActivityResults[requestCode].onActivityResult(requestCode, resultCode, data);
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
					b.putString("title", Navit.get_text("getting search results")); //TRANS
					b.putString("text", Navit.get_text("searching ...")); //TRANS
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
					// flags			-> order level to search at
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
			case 99:
				// dismiss dialog, remove dialog - generic
				dismissDialog(msg.getData().getInt("dialog_num"));
				removeDialog(msg.getData().getInt("dialog_num"));
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
			search_results_wait_offline.setCancelable(false);
			search_results_wait_offline.setProgress(0);
			search_results_wait_offline.setMax(10);
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
			Toast.makeText(getApplicationContext(), Navit.get_text("Map data (c) OpenStreetMap contributors, CC-BY-SA"), Toast.LENGTH_LONG).show(); //TRANS
			return mapdownloader_dialog_sec;
		}
		// should never get here!!
		return null;
	}

	public void disableSuspend()
	{
		wl.acquire();
		wl.release();
	}

	public void exit()
	{
		NavitVehicle.turn_off_all_providers();
		try
		{
			NavitSpeech.stop_me();
		}
		catch (Exception s)
		{
			s.printStackTrace();
		}
		try
		{
			NavitSpeech2.stop_me();
		}
		catch (Exception s)
		{
			s.printStackTrace();
		}
		//Intent resultIntent = new Intent();
		//resultIntent.putExtra("exitcode", String.valueOf(exitcode));
		//setResult(Activity.RESULT_OK, resultIntent);
		Log.e("Navit", "1***************** exit called ****************");
		Log.e("Navit", "2***************** exit called ****************");
		Log.e("Navit", "3***************** exit called ****************");
		Log.e("Navit", "4***************** exit called ****************");
		Log.e("Navit", "5***************** exit called ****************");
		Log.e("Navit", "6***************** exit called ****************");
		Log.e("Navit", "7***************** exit called ****************");
		Log.e("Navit", "8***************** exit called ****************");
		// NavitActivity(-3);
		System.gc();
		finish();
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
		getPrefs();
		activatePrefs(1);
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
		getPrefs();
		activatePrefs(1);
		NavitVehicle.set_last_known_pos_fast_provider();
	}

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
	static int PREF_search_country = 1; // default=*ALL*
	static int PREF_zoomlevel_num = 2 * 2 * 2 * 2 * 2;

	public static void setPrefs_search_country()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("search_country_id", PREF_search_country);
		editor.commit();
	}

	public static void setPrefs_zoomlevel()
	{
		//System.out.println("1 save zoom level: " + Navit.GlobalScaleLevel);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("zoomlevel_num", Navit.GlobalScaleLevel);
		editor.commit();
		//System.out.println("2 save zoom level: " + Navit.GlobalScaleLevel);
	}

	private static void getPrefs()
	{
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		PREF_use_fast_provider = prefs.getBoolean("use_fast_provider", false);
		PREF_allow_gui_internal = prefs.getBoolean("allow_gui_internal", false);
		PREF_follow_gps = prefs.getBoolean("follow_gps", true);
		PREF_use_compass_heading_base = prefs.getBoolean("use_compass_heading_base", false);
		PREF_use_compass_heading_always = prefs.getBoolean("use_compass_heading_always", false);
		PREF_use_compass_heading_fast = prefs.getBoolean("use_compass_heading_fast", false);
		PREF_use_anti_aliasing = prefs.getBoolean("use_anti_aliasing", true);
		PREF_gui_oneway_arrows = prefs.getBoolean("gui_oneway_arrows", false);
		PREF_show_debug_messages = prefs.getBoolean("show_debug_messages", false);
		PREF_show_3d_map = prefs.getBoolean("show_3d_map", false);
		PREF_use_lock_on_roads = prefs.getBoolean("use_lock_on_roads", true);
		PREF_use_route_highways = prefs.getBoolean("use_route_highways", true);
		PREF_save_zoomlevel = prefs.getBoolean("save_zoomlevel", true);
		PREF_search_country = prefs.getInt("search_country_id", 1); // default=*ALL*
		PREF_zoomlevel_num = prefs.getInt("zoomlevel_num", 2 * 2 * 2 * 2 * 2);
		PREF_show_sat_status = prefs.getBoolean("show_sat_status", false);
		PREF_use_agps = prefs.getBoolean("use_agps", false);

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

			//System.out.println("3 restore zoom level: " + Navit.GlobalScaleLevel);
			//System.out.println("4 restore zoom level: " + PREF_zoomlevel_num);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 33);
			b.putString("s", Integer.toString(PREF_zoomlevel_num));
			msg.setData(b);
			try
			{
				N_NavitGraphics.callback_handler.sendMessage(msg);
				Navit.GlobalScaleLevel = PREF_zoomlevel_num;
				// System.out.println("5 restore zoom level: " + PREF_zoomlevel_num);
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
			NavitVehicle.turn_off_sat_status();
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
	}

	static String PREF_navit_lang;

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
	 * open google maps at a given coordinate, to make bookmarks
	 */
	private void googlemaps_show(String lat, String lon, String name)
	{
		// geo:latitude,longitude
		String url = "geo:" + lat + "," + lon + "?z=" + "16";
		Intent gmaps_intent = new Intent(Intent.ACTION_VIEW);
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
		// -> let follow mode stay turned on  ### Navit.follow_button_off();
		String nix = NavitGraphics.CallbackGeoCalc(3, lat, lng);
	}

	public void zoom_to_route()
	{
		System.out.println("");
		System.out.println("*** Zoom to ROUTE ***");
		System.out.println("");
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
}
