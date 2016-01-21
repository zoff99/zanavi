/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2015 Zoff <zoff@zoff.cc>
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
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import bpi.sdbm.illuminance.SolarPosition;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location2;
import com.zoffcc.applications.zanavi.NavitMapDownloader.ProgressThread;
import com.zoffcc.applications.zanavi.NavitOSDJava.drawOSDThread;
import com.zoffcc.applications.zanavi.NavitVehicle.location_coords;
import com.zoffcc.applications.zanavi.ZANaviListViewAdapter.ListViewItem;
import com.zoffcc.applications.zanavi_msg.ZanaviCloudApi;

import de.oberoner.gpx2navit_txt.MainFrame;

public class Navit extends ActionBarActivity implements Handler.Callback, SensorEventListener
{
	public static final String VERSION_TEXT_LONG_INC_REV = "3698";
	public static String NavitAppVersion = "0";
	public static String NavitAppVersion_prev = "-1";
	public static String NavitAppVersion_string = "0";
	public final Boolean NAVIT_IS_EMULATOR = false; // when running on emulator set to true!!
	public static boolean has_hw_menu_button = false;
	static int NAVIT_MIN_HORIZONTAL_DP_FOR_ACTIONBAR = 400;
	static int actionbar_item_width = 100;
	static int actionbar_items_will_fit = 2;
	static boolean actionbar_all_items_will_fit = false;
	static boolean actionabar_download_icon_visible = false;
	static boolean is_navigating = false;
	static boolean is_paused = true;

	static int api_version_int = 6;

	static boolean PAINT_OLD_API = true;

	static long SHOWCASEVIEW_ID_001 = 93457181726L;

	//static final int DEFAULT_THEME_DARK = android.R.style.Theme_WithActionBar;
	//static final int DEFAULT_THEME_LIGHT = android.R.style.Theme_Material_Light;
	static final int DEFAULT_THEME_OLD_LIGHT = R.style.CustomActionBarThemeLight;
	static final int DEFAULT_THEME_OLD_DARK = R.style.CustomActionBarTheme;

	static final int DEFAULT_THEME_OLD_LIGHT_M = R.style.CustomActionBarThemeLightM;
	static final int DEFAULT_THEME_OLD_DARK_M = R.style.CustomActionBarThemeM;

	// GLSurfaceView glSurfaceView;

	static int OSD_blueish_bg_color = 0;

	// ----------------- DEBUG ----------------
	// ----------------- DEBUG ----------------
	// ----------------- DEBUG ----------------
	static final boolean METHOD_DEBUG = false; // for debugging only, set this to "false" on release builds!!
	static final boolean DEBUG_DRAW_VEHICLE = true; // if "false" then dont draw green vehicle, set this to "true" on release builds!!
	static final boolean NAVIT_ALWAYS_UNPACK_XMLFILE = false; // always unpacks the navit.xml file, set this to "false" on release builds!!
	static final boolean NAVIT_DEBUG_TEXT_VIEW = false; // show overlay with debug messages, set this to "false" on release builds!!
	static final boolean GFX_OVERSPILL = true; // make gfx canvas bigger for rotation and zoom smoothness
	// ----------------- DEBUG ----------------
	// ----------------- DEBUG ----------------
	// ----------------- DEBUG ----------------

	// ----------------------------------------
	// ----------------------------------------
	static final boolean FDBL = false;
	static final int CIDEBUG = 0;
	static boolean CIRUN = false;
	// ----------------------------------------
	// ----------------------------------------

	static final float OVERSPILL_FACTOR = 1.4f; // 20% percent on each side

	// ------------------ BitCoin Addr --------
	// ------------------ BitCoin Addr --------
	// ------------------ BitCoin Addr --------
	final static String BITCOIN_DONATE_ADDR = "1ZANav18WY8ytM7bhnAEBS3bdrTohsD9p";
	// ------------------ BitCoin Addr --------
	// ------------------ BitCoin Addr --------
	// ------------------ BitCoin Addr --------

	private static ZanaviCloudApi plugin_api = null;
	static final int PLUGIN_MSG_ID = 1;
	static final int PLUGIN_MSG_CAT_zanavi_version = 1;
	static final int PLUGIN_MSG_CAT_installed_maps = 2;
	static final int PLUGIN_MSG_CAT_3d_mode = 3;

	static ZANaviPrefs p = new ZANaviPrefs();
	static ZANaviPrefs p_old = new ZANaviPrefs();
	static final int STREET_SEARCH_STRINGS_SAVE_COUNT = 10;
	static boolean search_ready = false;

	// static AnimationDrawable mFrameAnimation;
	static Menu cur_menu = null;

	static long NAVIT_START_INTENT_DRIVE_HOME = 1L;

	static final int NAVIT_BACKBUTTON_TO_EXIT_TIME = 2000; // 2 secs.

	static int NavitOverflowMenuItemID = -1;

	static ContentResolver content_resolver = null;
	public static final String CR_AUTHORITY = "com.zoffcc.applications.zanavi_udonate.provider";
	public static final Uri CR_CONTENT_URI = Uri.parse("content://" + CR_AUTHORITY + "/" + "table1");

	public static Intent ZANaviMapDownloaderServiceIntent = null;

	static float last_y_bottom_bar_touch = 0;
	static float cur_y_margin_bottom_bar_touch = 0;
	static int map_view_height = 100;
	static float bottom_y_margin_bottom_bar_touch = 0;
	static int actionBarHeight = 0;
	static int bottom_bar_px = 80;
	static int bottom_bar_slider_shadow_px = 4;
	static GestureDetector mGestureDetector = null;
	static int swipeMaxOffPath = 20;
	static ZANaviRoadbookFragment road_book = null;
	static FragmentManager fragmentManager = null;
	static ImageView push_pin_view = null;
	static List<ListViewItem> road_book_items = null;

	TextToSpeech mTts = null;

	static ToneGenerator toneG = null;
	static boolean toneG_heared = false;

	public static int Global_Init_Finished = 0; // 0 -> no init
												// 1 -> all C structures are ready for use
	public static int Global_Location_update_not_allowed = 0; // 0 -> send location update to C functions
																// 1 -> DO NOT send location update to C functions, it may crash in this phase

	//static BackupManager backupManager = null;
	static Object backupManager = null;

	// AlertDialog dialog_info_popup = null;
	Dialog dialog_info_popup = null;
	int info_popup_seen_count = 0;
	final int info_popup_seen_count_max = 2; // must look at the info pop 2 times
	boolean info_popup_seen_count_end = false;

	static Navit Global_Navit_Object = null;
	static AssetManager asset_mgr = null;

	static boolean Navit_doubleBackToExitPressedOnce = false;

	// define graphics here (this is bad, please fix me!)
	public static NavitGraphics N_NavitGraphics = null;

	public static int usedMegs_old = -1;
	public static String usedMegs_str_old = "";
	public static int Routgraph_enabled = 0;

	// -------- SUN / MOON ----------
	long sun_moon__mLastCalcSunMillis = -1L;
	public double azmiuth_cache = -1;
	public double zenith_cache = -1;
	public static String sunrise_cache = "";
	public static boolean is_night = false;
	public static boolean is_twilight = false;
	public static String sunset_cache = "";
	public static double elevation = 0;
	public double moon_azimuth_cache = -1;
	public double moon_evelation_cache = -1;
	Boolean sun_moon__must_calc_new = true;
	SunriseSunsetCalculator sun_moon__calc = null;
	Calendar sun_moon__cx = null;
	SolarPosition.SunCoordinates sun_moon__sc = null;
	public static boolean calc_sun_enabled = true;
	// -------- SUN / MOON ----------

	public static CWorkerThread cwthr = null;
	public static NavitGraphics NG__map_main = null;
	public static NavitGraphics NG__vehicle = null;
	public static NavitVehicle NV = null;
	public static NavitSpeech2 NSp = null;
	public static drawOSDThread draw_osd_thread = null;

	public static boolean use_index_search = false;
	public static boolean index_search_realtime = false;

	static AlertDialog.Builder generic_alert_box = null;

	private Boolean xmlconfig_unpack_file = true;
	private Boolean write_new_version_file = true;
	final static int Navit_Status_COMPLETE_NEW_INSTALL = 1;
	final static int Navit_Status_UPGRADED_TO_NEW_VERSION = 2;
	final static int Navit_Status_NORMAL_STARTUP = 0;
	static Boolean Navit_DonateVersion_Installed = false;
	static Boolean Navit_Plugin_001_Installed = false;
	static Boolean Navit_index_on_but_no_idx_files = false;
	static Boolean Navit_maps_too_old = false;
	static Boolean Navit_Largemap_DonateVersion_Installed = false;
	private int startup_status = Navit_Status_NORMAL_STARTUP;
	final static int Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL = 8;
	static Boolean unsupported = false;
	static Boolean Navit_maps_loaded = false;
	final static int Navit_MAX_RECENT_DESTINATIONS = 50;
	static String debug_item_dump = "";
	static String global_last_destination_name = "";

	// for future use ...
	// public static String NavitDataDirectory = "/sdcard/";
	public static String NavitDataDirectory_Maps = "/sdcard/zanavi/maps/";
	static File[] NavitDataStorageDirs = null;

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

	public static final class Navit_Address_Result_Struct implements Comparable<Navit_Address_Result_Struct>
	{
		String result_type; // TWN,STR,SHN
		String item_id; // H<ddddd>L<ddddd> -> item.id_hi item.id_lo
		float lat;
		float lon;
		String addr;

		// function to sort address result list
		public int compareTo(Navit_Address_Result_Struct comp)
		{
			return this.addr.toLowerCase().compareTo(comp.addr.toLowerCase());
		}
	}

	public static final class Navit_Point_on_Map implements Serializable
	{
		/**
		 * struct for a point on the map
		 */
		private static final long serialVersionUID = 6899215049749155051L;
		String point_name = "";
		String addon = null; // null -> normal, "1" -> home location
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
		Boolean arriving_secs_to_dest_valid = false;
		String arriving_secs_to_dest = "";
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

	// -------------- GPS fix and extrapolation vars -------------
	// -------------- GPS fix and extrapolation vars -------------
	// -------------- GPS fix and extrapolation vars -------------
	static Location mLastLocation = null;
	static long mLastLocationMillis = -1;
	static boolean isGPSFix = false;
	static int pos_is_underground = 0;
	static boolean tunnel_extrapolation = false;
	// -------------- GPS fix and extrapolation vars -------------
	// -------------- GPS fix and extrapolation vars -------------
	// -------------- GPS fix and extrapolation vars -------------

	// public static Vibrator vibrator = null;

	public Handler handler;
	static PowerManager.WakeLock wl;
	static PowerManager.WakeLock wl_cpu;
	static PowerManager.WakeLock wl_navigating;
	private NavitActivityResult ActivityResults[];
	static AudioManager NavitAudioManager = null;
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
	public static final int NavitSendFeedback_id = 973;
	public static final int NavitReplayFileConvChooser_id = 974;
	public static int download_map_id = 0;
	ProgressThread progressThread_pri = null;
	ProgressThread progressThread_sec = null;
	public static int search_results_towns = 0;
	public static int search_results_streets = 0;
	public static int search_results_streets_hn = 0;
	public static int search_results_poi = 0;
	public static Boolean search_hide_duplicates = false;
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
	public static final int NavitAddressSearch_id_sharedest = 77;
	public static final int ZANaviVoiceInput_id = 78;
	public static final int NavitDonateFromSearch_id = 79;
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

	static Bitmap lane_none = null;
	static Bitmap lane_left = null;
	static Bitmap lane_right = null;
	static Bitmap lane_merge_to_left = null;
	static Bitmap lane_merge_to_right = null;
	static String lane_destination = "";
	static String lanes_text = "";
	static String lanes_text1 = "";
	static String lane_choices = "";
	static String lane_choices1 = "";
	static String lane_choices2 = "";
	static int lanes_num = 0;
	static int lanes_num_forward = 0;
	static int lanes_num1 = 0;
	static int lanes_num_forward1 = 0;
	static int seg_len = 0;
	static int cur_max_speed = -1;
	static int cur_max_speed_corr = -1;
	static boolean your_are_speeding = false;

	// ------- new features -----------
	// ------- new features -----------
	// ------- new features -----------
	// static boolean new_features = false; // for development ONLY !!
	// ------- new features -----------
	// ------- new features -----------
	// ------- new features -----------

	public static Bitmap long_green_arrow = null;
	public static Bitmap menu_button = null;
	public static RectF menu_button_rect = new RectF(-100, 1, 1, 1);
	public static RectF menu_button_rect_touch = new RectF(-100, 1, 1, 1);
	public static Bitmap follow_on = null;
	public static Bitmap follow_off = null;
	public static Bitmap follow_current = null;
	public static Bitmap zoomin = null;
	public static Bitmap zoomout = null;
	// public static Bitmap bigmap_bitmap = null;
	public static Bitmap oneway_arrow = null;
	public static Bitmap oneway_bicycle_arrow = null;
	public static Bitmap nav_arrow_stopped = null;
	public static Bitmap nav_arrow_stopped_small = null;
	public static Bitmap nav_arrow_moving = null;
	public static Bitmap nav_arrow_moving_grey = null;
	public static Bitmap nav_arrow_moving_small = null;
	public static Bitmap nav_arrow_moving_shadow = null;
	public static Bitmap nav_arrow_moving_shadow_small = null;

	public static String Navit_last_address_search_string = "";
	public static String Navit_last_address_hn_string = "";
	public static Boolean Navit_last_address_full_file_search = false;
	public static String Navit_last_address_search_country_iso2_string = "";
	public static int Navit_last_address_search_country_flags = 3;
	public static int Navit_last_address_search_country_id = 0;
	public static Boolean Navit_last_address_partial_match = true;
	public static Geocoder Navit_Geocoder = null;
	public static String UserAgentString = null;
	public static String UserAgentString_bind = null;
	public static Boolean first_ever_startup = false;

	public static Boolean Navit_Announcer = true;

	public static final int MAP_NUM_SECONDARY = 12;
	static String MAP_FILENAME_PATH = "/sdcard/zanavi/maps/";
	static String MAPMD5_FILENAME_PATH = "/sdcard/zanavi/md5/";
	static String CFG_FILENAME_PATH = "/sdcard/zanavi/";
	static String NAVIT_DATA_DIR = "/data/data/com.zoffcc.applications.zanavi"; // later use: Context.getFilesDir().getPath();
	static String NAVIT_DATA_SHARE_DIR = NAVIT_DATA_DIR + "/share";
	static String NAVIT_DATA_DEBUG_DIR = CFG_FILENAME_PATH + "../debug/";
	static String FIRST_STARTUP_FILE = NAVIT_DATA_SHARE_DIR + "/has_run_once.txt";
	static String VERSION_FILE = NAVIT_DATA_SHARE_DIR + "/version.txt";
	static final String Navit_DEST_FILENAME = "destinations.dat";
	static final String Navit_CENTER_FILENAME = "center.txt";

	static boolean need_recalc_route = false;

	static Resources res_ = null;
	static Window app_window = null;

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
		Log.e("Navit", "Res Obj " + res);
		Log.e("Navit", "Res Name " + resname);
		Log.e("Navit", "result " + result);
		int id = res.getIdentifier(resname, "raw", "com.zoffcc.applications.zanavi");
		// int id = res.getIdentifier(resname, "raw", getPackageName());

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

				if (resultfilestream != null)
				{
					resultfilestream.close();
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

				if (resultfilestream != null)
				{
					resultfilestream.close();
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

	static OnAudioFocusChangeListener focusChangeListener = new OnAudioFocusChangeListener()
	{
		public void onAudioFocusChange(int focusChange)
		{
			// AudioManager am = Navit.NavitAudioManager;
			switch (focusChange)
			{

			case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
				// Lower the volume while ducking.
				//mediaPlayer.setVolume(0.2f, 0.2f);
				break;
			case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
				//pause();
				break;

			case (AudioManager.AUDIOFOCUS_LOSS):
				//stop();
				//ComponentName component = new ComponentName(AudioPlayerActivity.this, MediaControlReceiver.class);
				//am.unregisterMediaButtonEventReceiver(component);
				break;

			case (AudioManager.AUDIOFOCUS_GAIN):
				// Return the volume to normal and resume if paused.
				//mediaPlayer.setVolume(1f, 1f);
				//mediaPlayer.start();
				break;
			default:
				break;
			}
		}
	};

	//	private boolean checkPlayServices()

	//	{
	//		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	//
	//		Log.i("PlayServices", "isGooglePlayServicesAvailable=" + status);
	//
	//		if (status != ConnectionResult.SUCCESS)
	//		{
	//			if (GooglePlayServicesUtil.isUserRecoverableError(status))
	//			{
	//				Toast.makeText(this, "Recoverable error.", Toast.LENGTH_LONG).show();
	//				// showErrorDialog(status);
	//			}
	//			else
	//			{
	//				Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
	//			}
	//			return false;
	//		}
	//		return true;
	//	}

	// ----------------------------------------------------------------------------------------------------------
	// thanks to: http://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled
	// ----------------------------------------------------------------------------------------------------------
	private void buildAlertMessageNoGps()
	{
		try
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(Navit.get_text("Your GPS is disabled, do you want to enable it?")).setCancelable(false).setPositiveButton(Navit.get_text("Yes"), new DialogInterface.OnClickListener()
			{
				public void onClick(final DialogInterface dialog, final int id)
				{
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			}).setNegativeButton(Navit.get_text("No"), new DialogInterface.OnClickListener()
			{
				public void onClick(final DialogInterface dialog, final int id)
				{
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** Called when the activity is first created. */

	// ----------- remove later -------------
	// ----------- remove later -------------
	@SuppressLint("NewApi")
	// ----------- remove later -------------
	// ----------- remove later -------------
	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// ------- only after API level 9 -------
		// ------- only after API level 9 -------
		//		try
		//		{
		//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDeath().penaltyLog().build());
		//			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
		//
		//			StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
		//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old).permitDiskWrites().build());
		//			old = StrictMode.getThreadPolicy();
		//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old).permitDiskReads().build());
		//
		//		}
		//		catch (NoClassDefFoundError e)
		//		{
		//		}
		// ------- only after API level 9 -------
		// ------- only after API level 9 -------

		// Log.e("Navit", "OnCreate");

		//		if (checkPlayServices())
		//		{
		//		}

		getPrefs_theme();
		getPrefs_theme_main();
		Navit.applySharedTheme(this, p.PREF_current_theme_M);

		super.onCreate(savedInstanceState);

		Global_Navit_Object = this;
		asset_mgr = getAssets();

		OSD_blueish_bg_color = getResources().getColor(R.color.blueish_bg_color);

		// getBaseContext_ = getBaseContext().getApplicationContext();
		getBaseContext_ = getBaseContext();

		last_orientation = getResources().getConfiguration().orientation;

		content_resolver = getContentResolver();
		// get_reglevel();

		Display display_ = getWindowManager().getDefaultDisplay();
		metrics = new DisplayMetrics();
		display_.getMetrics(Navit.metrics);

		road_book_items = new ArrayList<ListViewItem>();
		fragmentManager = getSupportFragmentManager();

		setContentView(R.layout.main_layout);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null)
		{
			try
			{
				setSupportActionBar(toolbar);
				// System.out.println("TTT01:" + toolbar);
			}
			catch (NoClassDefFoundError e)
			{
			}
		}

		try
		{
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			getSupportActionBar().setDisplayUseLogoEnabled(false);
			getSupportActionBar().setIcon(R.drawable.icon);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
		catch (NoClassDefFoundError e)
		{
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------

		bottom_bar_px = (int) getResources().getDimension(R.dimen.gui_top_container_height);
		// System.out.println("VVV:bottom_bar_height:" + bottom_bar_px);
		bottom_bar_slider_shadow_px = (int) getResources().getDimension(R.dimen.bottom_slide_view_shadow_compat_height);
		// System.out.println("VVV:bottom_bar_slider_shadow_px:" + bottom_bar_slider_shadow_px);

		// final RelativeLayout a = (RelativeLayout) findViewById(R.id.bottom_bar_container);
		final FrameLayout a = (FrameLayout) findViewById(R.id.bottom_bar_slide);
		final RelativeLayout.LayoutParams pp22 = (RelativeLayout.LayoutParams) a.getLayoutParams();

		// Calculate ToolBar height
		try
		{
			TypedValue tv = new TypedValue();
			if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
			{
				actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
				System.out.println("VVV:abh:" + actionBarHeight);
			}
			else
			{
				actionBarHeight = NavitGraphics.dp_to_px(144);
			}
		}
		catch (Exception e)
		{
			actionBarHeight = NavitGraphics.dp_to_px(144);
		}

		final android.support.v7.widget.Toolbar view_toolbar_top = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
		ViewTreeObserver vto = view_toolbar_top.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				view_toolbar_top.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				// int width = view_toolbar_top.getMeasuredWidth();
				int height = view_toolbar_top.getMeasuredHeight();
				Navit.actionBarHeight = height;
				// System.out.println("hhh:88=" + Navit.actionBarHeight);
				Navit.cur_y_margin_bottom_bar_touch = Navit.map_view_height + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom

				pp22.setMargins(0, (int) Navit.cur_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
				a.setLayoutParams(pp22);
				a.requestLayout();
			}
		});

		// actionBarHeight = 168;

		//		final int SWIPE_MIN_DISTANCE = NavitGraphics.dp_to_px(25);
		//		final float SWIPE_THRESHOLD_VELOCITY = 5.5f;
		//		final float FLING_PIXELS_PER_SECOND = 100;
		//		final float maxFlingVelocity = ViewConfiguration.get(this).getScaledMaximumFlingVelocity();
		final ViewConfiguration vc = ViewConfiguration.get(this);
		final int swipeMinDistance = vc.getScaledPagingTouchSlop();
		final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
		swipeMaxOffPath = vc.getScaledTouchSlop();
		// (there is also vc.getScaledMaximumFlingVelocity() one could check against)

		// setup some values --------
		NavitGraphics.long_press_on_screen_max_distance = swipeMaxOffPath;
		// setup some values --------

		class MyGestureDetector extends SimpleOnGestureListener
		{
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
			{
				try
				{
					//					float velocityPercentY = velocityY / maxFlingVelocity; // the percent is a value in the range of (0, 1]
					//					float normalizedVelocityY = velocityPercentY * FLING_PIXELS_PER_SECOND; // where PIXELS_PER_SECOND is a device-independent measurement

					//					System.out.println("VVV:" + (e1.getY() - e2.getY()) + " " + NavitGraphics.dp_to_px((int) (e1.getY() - e2.getY())) + " " + maxFlingVelocity + " " + velocityY + " " + velocityPercentY + " " + normalizedVelocityY + " " + SWIPE_THRESHOLD_VELOCITY);

					// System.out.println("VVV:2:" + swipeMinDistance + " " + swipeThresholdVelocity + " " + swipeMaxOffPath);

					// bottom to top
					if (e1.getY() - e2.getY() > swipeMinDistance && Math.abs(velocityY) > swipeThresholdVelocity)
					{
						//int featureWidth = getMeasuredWidth();
						//mActiveFeature = (mActiveFeature < (mItems.size() - 1)) ? mActiveFeature + 1 : mItems.size() - 1;
						//smoothScrollTo(mActiveFeature * featureWidth, 0);
						//System.out.println("GS:002:up:" + velocityY + " " + e2.getY() + " " + e1.getY());

						animate_bottom_bar_up();

						return true;
					}
					// top to bottom
					else if (e2.getY() - e1.getY() > swipeMinDistance && Math.abs(velocityY) > swipeThresholdVelocity)
					{
						//int featureWidth = getMeasuredWidth();
						//mActiveFeature = (mActiveFeature > 0) ? mActiveFeature - 1 : 0;
						//smoothScrollTo(mActiveFeature * featureWidth, 0);
						//System.out.println("GS:003:down:" + velocityY + " " + e1.getY() + " " + e2.getY());

						animate_bottom_bar_down();

						return true;
					}
				}
				catch (Exception e)
				{
					//System.out.println("GS:009:EE:" + e.getMessage());
				}
				return false;
			}
		}
		mGestureDetector = new GestureDetector(new MyGestureDetector());

		push_pin_view = (ImageView) findViewById(R.id.bottom_slide_left_side);
		push_pin_view.setOnClickListener(new ImageView.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					toggle_follow_button();
				}
				catch (Exception e)
				{
				}
			}
		});

		cur_y_margin_bottom_bar_touch = 0; // try to put view at bottom

		a.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			synchronized public boolean onTouch(View v, MotionEvent m)
			{

				int action = m.getAction();

				if (mGestureDetector.onTouchEvent(m))
				{
					//System.out.println("GS:001:fling!!");
					// System.out.println("FRAG:fling:011");
					return true;
				}
				else if (action == MotionEvent.ACTION_DOWN)
				{
					last_y_bottom_bar_touch = m.getY();

					// put roadbook into layout -----------
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

					try
					{
						if (road_book == null)
						{
							road_book = new ZANaviRoadbookFragment();
							// System.out.println("FRAG:attach:001");
							fragmentTransaction.replace(R.id.roadbook_fragment_container, road_book, "");
							fragmentTransaction.commitAllowingStateLoss();
							// fragmentTransaction.show(road_book);
						}
						else
						{
							// System.out.println("FRAG:attached:003");
						}
					}
					catch (Exception ef)
					{
					}
					// put roadbook into layout -----------

					return true;
				}
				else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL))
				{
					// System.out.println("FRAG:up/cancel:012");

					// release
					if (cur_y_margin_bottom_bar_touch > (bottom_y_margin_bottom_bar_touch / 2))
					{
						// snap back to bottom
						animate_bottom_bar_down();
					}
					else
					{
						// snap top top
						animate_bottom_bar_up();
					}
				}
				else
				// if (action == MotionEvent.ACTION_MOVE)
				{
					// System.out.println("FRAG:*else*:012");

					if (Math.abs(last_y_bottom_bar_touch - m.getY()) > 2)
					{
						float last_margin = cur_y_margin_bottom_bar_touch;
						cur_y_margin_bottom_bar_touch = cur_y_margin_bottom_bar_touch - (last_y_bottom_bar_touch - m.getY());

						if ((cur_y_margin_bottom_bar_touch >= 0) && (cur_y_margin_bottom_bar_touch <= bottom_y_margin_bottom_bar_touch))
						{
							// System.out.println("VVV:move:" + cur_y_margin_bottom_bar_touch + " " + bottom_y_margin_bottom_bar_touch);

							last_y_bottom_bar_touch = m.getY() + (last_y_bottom_bar_touch - m.getY());
							RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) a.getLayoutParams();
							relativeParams.setMargins(0, (int) cur_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
							a.setLayoutParams(relativeParams);
							a.requestLayout();
						}
						else
						{
							// System.out.println("VVV:revert");

							// revert position
							cur_y_margin_bottom_bar_touch = last_margin;
						}
					}

				}
				return true;
			}
		});
		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------

		// init cancel dialog!! ----------
		// init cancel dialog!! ----------
		Message msg2 = new Message();
		Bundle b2 = new Bundle();
		b2.putString("text", "");
		msg2.what = 0;
		msg2.setData(b2);
		ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg2);
		// init cancel dialog!! ----------
		// init cancel dialog!! ----------

		app_window = getWindow();

		// ---------------- set some directories -----------------
		// ---------------- set some directories -----------------
		NAVIT_DATA_DIR = this.getFilesDir().getPath();
		this.getFilesDir().mkdirs();
		// ---
		// System.out.println("data dir=" + NAVIT_DATA_DIR);
		NAVIT_DATA_SHARE_DIR = NAVIT_DATA_DIR + "/share/";
		File tmp3 = new File(NAVIT_DATA_SHARE_DIR);
		tmp3.mkdirs();
		// ---
		FIRST_STARTUP_FILE = NAVIT_DATA_SHARE_DIR + "/has_run_once.txt";
		VERSION_FILE = NAVIT_DATA_SHARE_DIR + "/version.txt";
		// ---------------- set some directories -----------------
		// ---------------- set some directories -----------------

		try
		{
			toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
		}
		catch (Exception e)
		{
		}

		try
		{
			Class.forName("android.app.backup.BackupManager");
			backupManager = new BackupManager(this);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		int width_ = display_.getWidth();
		int height_ = display_.getHeight();
		Log.e("Navit", "Navit -> pixels x=" + width_ + " pixels y=" + height_);
		Log.e("Navit", "Navit -> dpi=" + Navit.metrics.densityDpi);
		Log.e("Navit", "Navit -> density=" + Navit.metrics.density);
		Log.e("Navit", "Navit -> scaledDensity=" + Navit.metrics.scaledDensity);

		try
		{
			// send overspill factor to C-code
			Message msg33 = new Message();
			Bundle b33 = new Bundle();
			b33.putInt("Callback", 104);
			msg33.setData(b33);
			NavitGraphics.callback_handler.sendMessage(msg33);
		}
		catch (Exception eee)
		{
		}

		// ----- service -----
		// ----- service -----
		ZANaviMapDownloaderServiceIntent = new Intent(Navit.getBaseContext_, ZANaviMapDownloaderService.class);
		// ----- service -----
		// ----- service -----

		System.out.println("Navit:onCreate:JTHREAD ID=" + Thread.currentThread().getId());
		System.out.println("Navit:onCreate:THREAD ID=" + NavitGraphics.GetThreadId());

		// bitmaps for lanes
		lane_left = BitmapFactory.decodeResource(getResources(), R.drawable.lane_left);
		lane_right = BitmapFactory.decodeResource(getResources(), R.drawable.lane_right);
		lane_merge_to_left = BitmapFactory.decodeResource(getResources(), R.drawable.lane_merge_to_left);
		lane_merge_to_right = BitmapFactory.decodeResource(getResources(), R.drawable.lane_merge_to_right);
		lane_none = BitmapFactory.decodeResource(getResources(), R.drawable.lane_none);
		// bitmaps for lanes

		// paint for bitmapdrawing on map
		NavitGraphics.paint_for_map_display.setAntiAlias(true);
		NavitGraphics.paint_for_map_display.setFilterBitmap(true);

		// sky
		NavitGraphics.paint_sky_day.setAntiAlias(true);
		NavitGraphics.paint_sky_day.setColor(Color.parseColor("#79BAEC"));
		NavitGraphics.paint_sky_night.setAntiAlias(true);
		NavitGraphics.paint_sky_night.setColor(Color.parseColor("#090909"));
		// stars
		NavitGraphics.paint_sky_night_stars.setColor(Color.parseColor("#DEDDEF"));
		// twilight
		NavitGraphics.paint_sky_twilight1.setColor(Color.parseColor("#090909"));
		NavitGraphics.paint_sky_twilight2.setColor(Color.parseColor("#113268"));
		NavitGraphics.paint_sky_twilight3.setColor(Color.parseColor("#79BAEC"));

		Random m = new Random();
		int i6 = 0;
		for (i6 = 0; i6 < (NavitGraphics.max_stars + 1); i6++)
		{
			NavitGraphics.stars_x[i6] = m.nextFloat();
			NavitGraphics.stars_y[i6] = m.nextFloat();
			NavitGraphics.stars_size[i6] = m.nextInt(3) + 1;
		}

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
		Log.e("Navit", "**1**I " + startup_intent.toString());
		try
		{
			Log.e("Navit", "**1**DH E " + startup_intent.getExtras().describeContents());
		}
		catch (Exception ee)
		{
		}

		startup_status = Navit_Status_NORMAL_STARTUP;

		//		glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView_001);
		//		glSurfaceView.setEGLContextClientVersion(2); // enable OpenGL 2.0
		//		glSurfaceView.setRenderer(new GlRenderer());
		//		glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // only render on demand
		//
		//		// draw some sample lines ----
		//		// draw some sample lines ----
		//		// draw some sample lines ----
		//		ZANaviGlLine vertLine = new ZANaviGlLine();
		//		vertLine.SetVerts(1000f, 1000f, 0f, -1000f, -1000f, 0f);
		//		vertLine.SetColor(.8f, .8f, 0f, 1.0f);
		//
		//		float[] mMVPMatrix = new float[16];
		//
		//		// Position the eye behind the origin.
		//		final float eyeX = 0.0f;
		//		final float eyeY = 0.0f;
		//		final float eyeZ = 1.5f;
		//
		//		// We are looking toward the distance
		//		final float lookX = 0.0f;
		//		final float lookY = 0.0f;
		//		final float lookZ = -5.0f;
		//
		//		// Set our up vector. This is where our head would be pointing were we holding the camera.
		//		final float upX = 0.0f;
		//		final float upY = 1.0f;
		//		final float upZ = 0.0f;
		//
		//		// Set the view matrix. This matrix can be said to represent the camera position.
		//		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		//		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		//		Matrix.setLookAtM(mMVPMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
		//
		//		vertLine.draw(mMVPMatrix);
		//
		//		glSurfaceView.postInvalidate();
		//		glSurfaceView.requestRender();
		//		glSurfaceView.postInvalidate();
		//		// draw some sample lines ----
		//		// draw some sample lines ----
		//		// draw some sample lines ----

		// setup graphics objects
		// setup graphics objects
		// setup graphics objects
		NG__vehicle = new NavitGraphics(this, 1, 0, 0, 50, 50, 65535, 0, 0);
		NG__map_main = new NavitGraphics(this, 0, 0, 0, 100, 100, 0, 0, 0);
		Navit.N_NavitGraphics = NG__map_main;
		// setup graphics objects
		// setup graphics objects
		// setup graphics objects

		NV = new NavitVehicle(this);
		NSp = new NavitSpeech2(this);

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

		TextView no_maps_text = (TextView) this.findViewById(R.id.no_maps_text);
		no_maps_text.setText("\n\n\n" + Navit.get_text("No Maps installed") + "\n" + Navit.get_text("Please download a map") + "\n\n");
		// no_maps_text.postInvalidate();

		// set map cache size here -----------------------------------
		getPrefs_mapcache();
		activatePrefs_mapcache();
		// set map cache size here -----------------------------------

		// get map data dir and set it -----------------------------
		getPrefs_mapdir();
		activatePrefs_mapdir(true);
		// get map data dir and set it -----------------------------

		// get special prefs here ------------------------------------
		get_prefs_highdpi();
		// get special prefs here ------------------------------------

		// make sure the new path for the navitmap.bin file(s) exist!!
		File navit_maps_dir = new File(MAP_FILENAME_PATH);
		navit_maps_dir.mkdirs();
		// create nomedia files
		File nomedia_file = new File(MAP_FILENAME_PATH + ".nomedia");
		try
		{
			nomedia_file.createNewFile();
		}
		catch (Exception e1)
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

		// make sure the new path for config files exist
		File navit_cfg_dir = new File(CFG_FILENAME_PATH);
		navit_cfg_dir.mkdirs();

		// make sure the new path for the navitmap.bin file(s) exist!!
		File navit_mapsmd5_dir = new File(MAPMD5_FILENAME_PATH);
		navit_mapsmd5_dir.mkdirs();

		// make sure the share dir exists, otherwise the infobox will not show
		File navit_data_share_dir = new File(NAVIT_DATA_SHARE_DIR);
		navit_data_share_dir.mkdirs();

		File dd = new File(NAVIT_DATA_DEBUG_DIR);
		dd.mkdirs();

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
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
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
			if (get_reglevel() == 1)
			{
				System.out.println("##U:bonus 001##");
				Navit_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
			}
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
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
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
			if (get_reglevel() == 1)
			{
				System.out.println("##U:bonus 002##");
				Navit_DonateVersion_Installed = true;
				Navit_Largemap_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// update map list
		NavitMapDownloader.init_maps_without_donate_largemaps();
		// ---------- downloader threads ----------------

		// ---- detect menu button ----
		detect_menu_button();

		if (Navit.metrics.densityDpi >= 320) //&& (PREF_shrink_on_high_dpi))
		{
			Navit.menu_button = BitmapFactory.decodeResource(getResources(), R.drawable.menu_001);
		}
		else
		{
			Navit.menu_button = BitmapFactory.decodeResource(getResources(), R.drawable.menu_001_small);
		}

		Navit.long_green_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.long_green_arrow);

		Navit.follow_on = BitmapFactory.decodeResource(getResources(), R.drawable.follow);
		Navit.follow_off = BitmapFactory.decodeResource(getResources(), R.drawable.follow_off);
		Navit.follow_current = Navit.follow_on;

		if ((Navit.metrics.densityDpi >= 320) && (p.PREF_shrink_on_high_dpi))
		{
			float factor;
			factor = (float) NavitGraphics.Global_Scaled_DPI_normal / (float) Navit.metrics.densityDpi;
			factor = factor * 1.7f;
			//
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inDither = true;
			//o.inScaled = true;
			//o.inTargetDensity = NavitGraphics.Global_Scaled_DPI_normal;
			Navit.nav_arrow_stopped = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_stopped, o);
			Navit.nav_arrow_moving = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving, o);
			Navit.nav_arrow_moving_grey = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_grey, o);
			Navit.nav_arrow_moving_shadow = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_shadow, o);

			Navit.nav_arrow_stopped_small = Bitmap.createScaledBitmap(Navit.nav_arrow_stopped, (int) (Navit.nav_arrow_stopped.getWidth() / NavitGraphics.strech_factor_3d_map * factor), (int) (Navit.nav_arrow_stopped.getHeight() / NavitGraphics.strech_factor_3d_map * factor), true);
			Navit.nav_arrow_moving_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving, (int) (Navit.nav_arrow_moving.getWidth() / NavitGraphics.strech_factor_3d_map * factor), (int) (Navit.nav_arrow_moving.getHeight() / NavitGraphics.strech_factor_3d_map * factor), true);
			Navit.nav_arrow_moving_shadow_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving_shadow, (int) (Navit.nav_arrow_moving_shadow.getWidth() / NavitGraphics.strech_factor_3d_map * factor), (int) (Navit.nav_arrow_moving_shadow.getHeight() / NavitGraphics.strech_factor_3d_map * factor), true);
		}
		else
		{
			Navit.nav_arrow_stopped = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_stopped);
			Navit.nav_arrow_moving = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving);
			Navit.nav_arrow_moving_grey = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_grey);
			Navit.nav_arrow_moving_shadow = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_shadow);

			Navit.nav_arrow_stopped_small = Bitmap.createScaledBitmap(Navit.nav_arrow_stopped, (int) (Navit.nav_arrow_stopped.getWidth() / NavitGraphics.strech_factor_3d_map), (int) (Navit.nav_arrow_stopped.getHeight() / NavitGraphics.strech_factor_3d_map), true);
			Navit.nav_arrow_moving_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving, (int) (Navit.nav_arrow_moving.getWidth() / NavitGraphics.strech_factor_3d_map), (int) (1.5 * Navit.nav_arrow_moving.getHeight() / NavitGraphics.strech_factor_3d_map), true);
			Navit.nav_arrow_moving_shadow_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving_shadow, (int) (Navit.nav_arrow_moving_shadow.getWidth() / NavitGraphics.strech_factor_3d_map), (int) (1.5 * Navit.nav_arrow_moving_shadow.getHeight() / NavitGraphics.strech_factor_3d_map), true);
		}

		Navit.zoomin = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_in_32_32);
		Navit.zoomout = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_out_32_32);

		//Navit.oneway_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway);
		Navit.oneway_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway_large);
		Navit.oneway_bicycle_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway_bicycle_large);

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

		api_version_int = Integer.valueOf(android.os.Build.VERSION.SDK);
		System.out.println("XXX:API=" + api_version_int);
		if (api_version_int > 10)
		{
			Navit.PAINT_OLD_API = false;
		}
		else
		{
			Navit.PAINT_OLD_API = true;
		}

		if (MANUFACTURER.equalsIgnoreCase("amazon"))
		{
			// we are on amazon device
			ZANaviNormalDonateActivity.on_amazon_device = true;
		}

		// debug
		// debug
		// android_device = "telechips telechips m801";
		// debug
		// debug

		String android_rom_name = DISPLAY;

		if (FDBL)
		{
			android_rom_name = android_rom_name + "; FD";
		}

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

		// --------- enable GPS ? --------------
		// --------- enable GPS ? --------------
		//		try
		//		{
		//			final LocationManager llmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//			if (!llmanager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		//			{
		//				buildAlertMessageNoGps();
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}
		// --------- enable GPS ? --------------
		// --------- enable GPS ? --------------

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
				if (FDBL)
				{
					url = "http://fd.zanavi.cc/manual";
				}
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		info_popup_seen_count_end = false;
		File navit_first_startup = new File(FIRST_STARTUP_FILE);
		// if file does NOT exist, show the info box
		if (!navit_first_startup.exists())
		{
			// set first-ever-startup flag
			first_ever_startup = true;
			info_popup_seen_count_end = true; // don't show on first ever start of the app
			startup_status = Navit_Status_COMPLETE_NEW_INSTALL;
			FileOutputStream fos_temp;
			try
			{
				info_popup_seen_count++;
				fos_temp = new FileOutputStream(navit_first_startup);
				fos_temp.write((int) info_popup_seen_count); // use to store info popup seen count
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
		else
		{
			FileOutputStream fos_temp;
			FileInputStream fis_temp;
			try
			{
				fis_temp = new FileInputStream(navit_first_startup);
				info_popup_seen_count = fis_temp.read();
				fis_temp.close();

				if (info_popup_seen_count < 0)
				{
					info_popup_seen_count = 0;
				}

				// we wrote "A" -> (int)65 previously, so account for that
				if (info_popup_seen_count == 65)
				{
					info_popup_seen_count = 0;
				}

				if (info_popup_seen_count > info_popup_seen_count_max)
				{
					info_popup_seen_count_end = true;
				}
				else
				{
					info_popup_seen_count++;
					fos_temp = new FileOutputStream(navit_first_startup);
					fos_temp.write((int) info_popup_seen_count); // use to store info popup seen count
					fos_temp.flush();
					fos_temp.close();
				}
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
		//		if (startup_status == Navit_Status_UPGRADED_TO_NEW_VERSION)
		//		{
		//			try
		//			{
		//				message.setLayoutParams(rlp);
		//				// upgrade message
		//				String upgrade_summary = "\n\n***********\n";
		//				// upgrade message
		//				final SpannableString s = new SpannableString("\n" + "ZANavi " + NavitAppVersion_string + "\n\n" + "upgraded" + upgrade_summary);
		//				Linkify.addLinks(s, Linkify.WEB_URLS);
		//				message.setText(s);
		//				message.setMovementMethod(LinkMovementMethod.getInstance());
		//				infobox.setView(message);
		//
		//				infobox.show();
		//			}
		//			catch (Exception e)
		//			{
		//				e.printStackTrace();
		//			}
		//
		//		}
		// show info box for upgrade

		//
		// ----------- info popup
		// ----------- info popup
		// ----------- info popup
		// ----------- info popup
		//
		if ((!info_popup_seen_count_end) || (startup_status == Navit_Status_UPGRADED_TO_NEW_VERSION))
		{
			try
			{
				//Builder a1 = new AlertDialog.Builder(this);
				//dialog_info_popup = a1.show();
				dialog_info_popup = new Dialog(this);

				dialog_info_popup.setContentView(R.layout.info_popup);
				Button b_i1 = (Button) dialog_info_popup.findViewById(R.id.dialogButtonOK_i1);

				TextView tv_i1 = (TextView) dialog_info_popup.findViewById(R.id.text_i1);
				final String ZANAVI_MSG_PLUGIN_MARKET_LINK = "https://play.google.com/store/apps/details?id=com.zoffcc.applications.zanavi_msg";
				final String ZANAVI_UDONATE_LINK = "http://more.zanavi.cc/donate/";

				if (FDBL)
				{
					tv_i1.setText(Html.fromHtml("\n<br>Try the Donate Version and help us keep the mapservers running.<br>\nyou will activate the super fast index search.\n" + "<br><a href=\"" + ZANAVI_UDONATE_LINK + "\">get the donate version</a>\n<br>\n<br>"));
				}
				else
				{
					tv_i1.setText(Html.fromHtml("\n<br>Try the Donate Version and help us keep the mapservers running.<br>\nyou will activate the super fast index search.\n" + "<br><a href=\"" + ZANAVI_UDONATE_LINK + "\">get the donate version</a>\n<br>\n<br>"));
					// tv_i1.setText(Html.fromHtml("\n<br>Try the new Plugin to be notified when there are updates to your downloaded maps.\n" + "<br><a href=\"" + ZANAVI_MSG_PLUGIN_MARKET_LINK + "\">install Plugin</a>\n<br>\n<br>" + "Probier das neue Plugin damit du immer benachrichtigt wirst wenn es Kartenupdates gibt.\n<br>" + "<a href=\"" + ZANAVI_MSG_PLUGIN_MARKET_LINK + "\">Plugin installieren</a>\n<br>"));
				}

				try
				{
					tv_i1.setMovementMethod(LinkMovementMethod.getInstance());
				}
				catch (Exception ee3)
				{
				}

				b_i1.setText("Ok (" + (1 + info_popup_seen_count_max - info_popup_seen_count) + ")");
				b_i1.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View v)
					{
						try
						{
							dialog_info_popup.cancel();
						}
						catch (Exception e)
						{

						}

						try
						{
							dialog_info_popup.dismiss();
						}
						catch (Exception e)
						{

						}

						try
						{
							// draw map no-async
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 64);
							msg.setData(b);
							NavitGraphics.callback_handler.sendMessage(msg);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				});
				dialog_info_popup.setCancelable(true);
				dialog_info_popup.show();
				dialog_info_popup.getWindow().getDecorView().setBackgroundResource(R.drawable.rounded_bg);
				if (FDBL)
				{
					dialog_info_popup.setTitle("  Support us");
				}
				else
				{
					dialog_info_popup.setTitle("  Try the new Plugin");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		//
		// ----------- info popup
		// ----------- info popup
		// ----------- info popup
		//

		// make handler statically available for use in "msg_to_msg_handler"
		Navit_progress_h = this.progress_handler;

		//		try
		//		{
		//			Navit.bigmap_bitmap = BitmapFactory.decodeResource(getResources(), R.raw.bigmap_colors_zanavi2);
		//		}
		//		catch (Exception e)
		//		{
		//			// when not enough memory is available, then disable large world overview map!
		//			System.gc();
		//			Navit.bigmap_bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		//		}
		//		// ------no----- // Navit.bigmap_bitmap.setDensity(120); // set our dpi!!

		try
		{
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			ActivityResults = new NavitActivityResult[16];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			NavitAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		PowerManager pm = null;
		try
		{
			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// -- // pm.wakeUp(SystemClock.uptimeMillis()); // -- //
			// **screen always full on** // wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");
			// **screen can go off, cpu will stay on** // wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");

			// this works so far, lets the screen dim, but it cpu and screen stays on
			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			wl = null;
		}

		try
		{
			wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZANaviNeedCpu");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			wl_cpu = null;
		}

		try
		{
			wl_navigating = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "ZANaviNavigationOn");
		}
		catch (Exception e)
		{
			Log.e("Navit", "WakeLock NAV: create failed!!");
			e.printStackTrace();
			wl_navigating = null;
		}

		//		try
		//		{
		//			if (wl_navigating != null)
		//			{
		//				wl_navigating.acquire();
		//				Log.e("Navit", "WakeLock NAV: acquire 00");
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			Log.e("Navit", "WakeLock NAV: something wrong 00");
		//			e.printStackTrace();
		//		}

		//		try
		//		{
		//			if (wl != null)
		//			{
		//				try
		//				{
		//					wl.release();
		//				}
		//				catch (Exception e2)
		//				{
		//				}
		//				wl.acquire();
		//				Log.e("Navit", "WakeLock: acquire 1");
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}

		// -- extract overview maps --
		// -- extract overview maps --

		// File navit_worldmap2_file = new File(NAVIT_DATA_DIR + "/share/worldmap2.txt");
		File navit_worldmap2_file = new File(MAP_FILENAME_PATH + "/worldmap2.txt");
		if (!navit_worldmap2_file.exists())
		{
			if (!extractRes("worldmap2", MAP_FILENAME_PATH + "/worldmap2.txt"))
			{
				Log.e("Navit", "Failed to extract worldmap2.txt");
			}
		}

		File navit_worldmap5_file = new File(MAP_FILENAME_PATH + "/worldmap5.txt");
		if (!navit_worldmap5_file.exists())
		{
			if (!extractRes("worldmap5", MAP_FILENAME_PATH + "/worldmap5.txt"))
			{
				Log.e("Navit", "Failed to extract worldmap5.txt");
			}
		}
		// -- extract overview maps --
		// -- extract overview maps --

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

		File navit_config_xml_file = new File(NAVIT_DATA_SHARE_DIR + "/navit.xml");
		if ((!navit_config_xml_file.exists()) || (NAVIT_ALWAYS_UNPACK_XMLFILE))
		{
			xmlconfig_unpack_file = true;
			Log.e("Navit", "navit.xml does not exist, unpacking in any case");
		}

		my_display_density = "mdpi";
		// ldpi display (120 dpi)

		NavitGraphics.Global_want_dpi = Navit.metrics.densityDpi;
		NavitGraphics.Global_want_dpi_other = Navit.metrics.densityDpi;

		if (Navit.metrics.densityDpi <= 120)
		{
			my_display_density = "ldpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navitldpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
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
				if (!extractRes("navitmdpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
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
				if (!extractRes("navithdpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
				{
					Log.e("Navit", "Failed to extract navit.xml for hdpi device(s)");
				}
			}
		}
		// xhdpi display (320 dpi)
		else if (Navit.metrics.densityDpi >= 320)
		{
			// set the map display DPI down. otherwise everything will be very small and unreadable
			// and performance will be very low
			if (p.PREF_shrink_on_high_dpi)
			{
				NavitGraphics.Global_want_dpi = NavitGraphics.Global_Scaled_DPI_normal;
			}
			NavitGraphics.Global_want_dpi_other = NavitGraphics.Global_Scaled_DPI_normal;

			Log.e("Navit", "found xhdpi device, this is not fully supported yet");
			Log.e("Navit", "using hdpi values for compatibility");
			my_display_density = "hdpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navithdpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
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
				if (!extractRes("navitmdpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
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

		int have_dpi = Navit.metrics.densityDpi;
		if (NavitGraphics.Global_want_dpi == have_dpi)
		{
			NavitGraphics.Global_dpi_factor = 1;
		}
		{
			NavitGraphics.Global_dpi_factor = ((float) NavitGraphics.Global_want_dpi / (float) have_dpi);
		}

		// gggggggggggggggggggggggggg new !!!!!!!!!!!!!!!!!!!!

		// --> dont use!! NavitMain(this, langu, android.os.Build.VERSION.SDK_INT);
		Log.e("Navit", "android.os.Build.VERSION.SDK_INT=" + Integer.valueOf(android.os.Build.VERSION.SDK));

		// -- report share dir back to C-code --
		//Message msg2 = new Message();
		//Bundle b2 = new Bundle();
		//b2.putInt("Callback", 82);
		//b2.putString("s", NAVIT_DATA_DIR + "/share/");
		//msg2.setData(b2);
		//N_NavitGraphics.callback_handler.sendMessage(msg2);
		// -- report share dir back to C-code --

		// -- report data dir back to C-code --
		//msg2 = new Message();
		//b2 = new Bundle();
		//b2.putInt("Callback", 84);
		//b2.putString("s", NAVIT_DATA_DIR + "/");
		//msg2.setData(b2);
		//N_NavitGraphics.callback_handler.sendMessage(msg2);
		// -- report share dir back to C-code --

		draw_osd_thread = new drawOSDThread();
		draw_osd_thread.start();

		cwthr = new CWorkerThread();
		cwthr.start();

		// --new--
		cwthr.StartMain(this, langu, Integer.valueOf(android.os.Build.VERSION.SDK), "" + Navit.metrics.densityDpi, NAVIT_DATA_DIR, NAVIT_DATA_SHARE_DIR);

		// --old--
		// NavitMain(this, langu, Integer.valueOf(android.os.Build.VERSION.SDK), my_display_density);
		// --old--
		// NavitActivity(3);

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

		Navit.mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		//try
		//{
		//	Thread.sleep(2000);
		//}
		//catch (InterruptedException e)
		//{
		//}

		//getPrefs();
		//activatePrefs();

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

		try
		{
			Intent sintent = new Intent();
			sintent.setPackage("com.zoffcc.applications.zanavi_msg");
			sintent.setAction("com.zoffcc.applications.zanavi_msg.ZanaviCloudService");
			// ComponentName cname = startService(sintent);
			// Log.i("NavitPlugin", "start Service res=" + cname);
			// System.out.println("NavitPlugin:bind to Service");
			boolean res_bind = bindService(sintent, serviceConnection, Context.BIND_AUTO_CREATE);
			// Log.i("NavitPlugin", "bind to Service res=" + res_bind);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
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
			if (p.PREF_show_debug_messages)
			{
				// --------- OLD method -----------
				// --------- OLD method -----------
				//				int usedMegs;
				//				//System.gc();
				//				usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				//				//Debug.MemoryInfo meminfo = new Debug.MemoryInfo();
				//				//Debug.getMemoryInfo(meminfo);
				//
				//				if (usedMegs_old != usedMegs)
				//				{
				//					String usedMegsString = String.format("Memory Used: %d MB", usedMegs);
				//					//System.out.println("" + meminfo.dalvikPrivateDirty + " " + meminfo.dalvikPss + " " + meminfo.dalvikSharedDirty + " nP:" + meminfo.nativePrivateDirty + " nPss:" + meminfo.nativePss + " nSh:" + meminfo.nativeSharedDirty + " o1:" + meminfo.otherPrivateDirty + " o2:" + meminfo.otherPss + " o3:" + meminfo.otherSharedDirty);
				//					Navit.set_debug_messages2(usedMegsString);
				//				}
				//				usedMegs_old = usedMegs;
				// --------- OLD method -----------
				// --------- OLD method -----------

				// --------- NEW method -----------
				// --------- NEW method -----------
				String usedMegs = logHeap(Global_Navit_Object.getClass());
				if (usedMegs_str_old.compareTo(usedMegs) != 0)
				{
					Navit.set_debug_messages2(usedMegs);
				}
				usedMegs_str_old = usedMegs;
				// --------- NEW method -----------
				// --------- NEW method -----------
			}
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
			//System.out.println("invalidate 022");
			NavitGraphics.NavitMsgTv_.postInvalidate();
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
			//System.out.println("invalidate 023");
			NavitGraphics.NavitMsgTv_.postInvalidate();
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
			//NavitGraphics.NavitMsgTv_.setMaxLines(4);
			//NavitGraphics.NavitMsgTv_.setLines(4);

			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			//System.out.println("invalidate 024");
			NavitGraphics.NavitMsgTv_.postInvalidate();
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
			//System.out.println("invalidate 025");
			NavitGraphics.NavitMsgTv_.postInvalidate();
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
			//System.out.println("invalidate 026");
			NavitGraphics.NavitMsgTv_.postInvalidate();
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

	public static void set_debug_messages_say_wrapper(String text)
	{
		try
		{
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 30;
			b.putString("text", text);
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		try
		{
			System.out.println("XXIIXX(2):111");
			String mid_str = intent.getExtras().getString("com.zoffcc.applications.zanavi.mid");

			System.out.println("XXIIXX(2):111a:mid_str=" + mid_str);

			if (mid_str != null)
			{
				if (mid_str.equals("201:UPDATE-APP"))
				{
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
				}
				else if (mid_str.startsWith("202:UPDATE-MAP:"))
				{
					// System.out.println("need to update1:" + mid_str);
					// System.out.println("need to update2:" + mid_str.substring(15));

					auto_start_update_map(mid_str.substring(15));
				}
			}

			System.out.println("XXIIXX(2):111b:mid_str=" + mid_str);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("XXIIXX(2):111:EEEE");
		}

		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----
		try
		{
			System.out.println("XXIIXX(2):" + intent);
			Bundle bundle77 = intent.getExtras();
			System.out.println("XXIIXX(2):" + intent_flags_to_string(intent.getFlags()));
			if (bundle77 == null)
			{
				System.out.println("XXIIXX(2):" + "null");
			}
			else
			{
				for (String key : bundle77.keySet())
				{
					Object value = bundle77.get(key);
					System.out.println("XXIIXX(2):" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
				}
			}
		}
		catch (Exception ee22)
		{
			String exst = Log.getStackTraceString(ee22);
			System.out.println("XXIIXX(2):ERR:" + exst);
		}
		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----

		Log.e("Navit", "3:**1**A " + intent.getAction());
		Log.e("Navit", "3:**1**D " + intent.getDataString());
		Log.e("Navit", "3:**1**S " + intent.toString());
		try
		{
			Log.e("Navit", "3:**1**S " + intent.getExtras().describeContents());
		}
		catch (Exception ee3)
		{
		}

		// if (Navit.startup_intent == null)
		{
			try
			{
				// make a copy of the given intent object
				// Navit.startup_intent = intent.cloneFilter();
				Navit.startup_intent = intent;

				Log.e("Navit", "3a:**1**001");
				Bundle extras2 = intent.getExtras();
				Log.e("Navit", "3a:**1**002");
				try
				{
					Navit.startup_intent.putExtras(extras2);
					Log.e("Navit", "3a:**1**003");
				}
				catch (Exception e4)
				{
					if (startup_intent.getDataString() != null)
					{
						// we have a "geo:" thingy intent, use it
						// or "gpx file"
						Log.e("Navit", "3c:**1**A " + startup_intent.getAction());
						Log.e("Navit", "3c:**1**D " + startup_intent.getDataString());
						Log.e("Navit", "3c:**1**S " + startup_intent.toString());
					}
					else
					{
						Log.e("Navit", "3X:**1**X ");
						Navit.startup_intent = null;
					}

					// hack! remeber timstamp, and only allow 4 secs. later in onResume to set target!
					Navit.startup_intent_timestamp = System.currentTimeMillis();

					return;
				}

				// Intent { act=android.intent.action.VIEW
				// cat=[android.intent.category.DEFAULT]
				// dat=file:///mnt/sdcard/zanavi_pos_recording_347834278.gpx
				// cmp=com.zoffcc.applications.zanavi/.Navit }

				// hack! remeber timstamp, and only allow 4 secs. later in onResume to set target!
				Navit.startup_intent_timestamp = System.currentTimeMillis();
				Log.e("Navit", "3a:**1**A " + startup_intent.getAction());
				Log.e("Navit", "3a:**1**D " + startup_intent.getDataString());
				Log.e("Navit", "3a:**1**S " + startup_intent.toString());
				if (extras2 != null)
				{
					long l = extras2.getLong("com.zoffcc.applications.zanavi.ZANAVI_INTENT_type");
					// System.out.println("DH:a007 l=" + l);
					if (l != 0L)
					{
						if (l == Navit.NAVIT_START_INTENT_DRIVE_HOME)
						{
							// Log.e("Navit", "2:**1** started via drive home");
							// we have been called from "drive home" widget

							// drive home

							// check if we have a home location
							int home_id = find_home_point();

							if (home_id != -1)
							{
								Message msg7 = progress_handler.obtainMessage();
								Bundle b7 = new Bundle();
								msg7.what = 2; // long Toast message
								b7.putString("text", Navit.get_text("driving to Home Location")); //TRANS
								msg7.setData(b7);
								progress_handler.sendMessage(msg7);

								// clear any previous destinations
								Message msg2 = new Message();
								Bundle b2 = new Bundle();
								b2.putInt("Callback", 7);
								msg2.setData(b2);
								NavitGraphics.callback_handler.sendMessage(msg2);

								// set position to middle of screen -----------------------
								// set position to middle of screen -----------------------
								// set position to middle of screen -----------------------
								//					Message msg67 = new Message();
								//					Bundle b67 = new Bundle();
								//					b67.putInt("Callback", 51);
								//					b67.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
								//					b67.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
								//					msg67.setData(b67);
								//					N_NavitGraphics.callback_handler.sendMessage(msg67);
								// set position to middle of screen -----------------------
								// set position to middle of screen -----------------------
								// set position to middle of screen -----------------------

								try
								{
									Thread.sleep(60);
								}
								catch (Exception e)
								{
								}

								route_wrapper(map_points.get(home_id).point_name, 0, 0, false, map_points.get(home_id).lat, map_points.get(home_id).lon, true);

								//								Navit.destination_set();
								//
								//								// set destination to home location
								//								String lat = String.valueOf(map_points.get(home_id).lat);
								//								String lon = String.valueOf(map_points.get(home_id).lon);
								//								String q = map_points.get(home_id).point_name;
								//
								//								// System.out.println("lat=" + lat + " lon=" + lon + " name=" + q);
								//
								//								Message msg55 = new Message();
								//								Bundle b55 = new Bundle();
								//								b55.putInt("Callback", 3);
								//								b55.putString("lat", lat);
								//								b55.putString("lon", lon);
								//								b55.putString("q", q);
								//								msg55.setData(b55);
								//								NavitGraphics.callback_handler.sendMessage(msg55);

								final Thread zoom_to_route_001 = new Thread()
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
													zoom_to_route();
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
								zoom_to_route_001.start();

								//					try
								//					{
								//						show_geo_on_screen(Float.parseFloat(lat), Float.parseFloat(lon));
								//					}
								//					catch (Exception e2)
								//					{
								//						e2.printStackTrace();
								//					}

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
								// no home location set
								Message msg = progress_handler.obtainMessage();
								Bundle b = new Bundle();
								msg.what = 2; // long Toast message
								b.putString("text", Navit.get_text("No Home Location set")); //TRANS
								msg.setData(b);
								progress_handler.sendMessage(msg);
							}

						}
					}
					else
					{
						if (startup_intent.getDataString() != null)
						{
							// we have a "geo:" thingy intent, use it
							// or "gpx file"
						}
						else
						{
							Navit.startup_intent = null;
						}
					}
				}
				else
				{
					if (startup_intent.getDataString() != null)
					{
						// we have a "geo:" thingy intent, use it
					}
					else
					{
						Navit.startup_intent = null;
					}
				}
			}
			catch (Exception e99)
			{
				Navit.startup_intent = null;
			}

		}

	}

	@Override
	public void onStart()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		Navit.show_mem_used();

		super.onStart();

		Log.e("Navit", "OnStart");

		while (Global_Init_Finished == 0)
		{
			Log.e("Navit", "OnStart:Global_Init_Finished==0 !!!!!");
			try
			{
				Thread.sleep(60, 0); // sleep
			}
			catch (InterruptedException e)
			{
			}
		}

		cwthr.NavitActivity2(2);

		getPrefs();
		activatePrefs();
		sun_moon__mLastCalcSunMillis = -1L;

		// paint for bitmapdrawing on map
		if (p.PREF_use_anti_aliasing)
		{
			NavitGraphics.paint_for_map_display.setAntiAlias(true);
		}
		else
		{
			NavitGraphics.paint_for_map_display.setAntiAlias(false);
		}
		if (p.PREF_use_map_filtering)
		{
			NavitGraphics.paint_for_map_display.setFilterBitmap(true);
		}
		else
		{
			NavitGraphics.paint_for_map_display.setFilterBitmap(false);
		}

		// activate gps AFTER 3g-location
		NavitVehicle.turn_on_precise_provider();

		Navit.show_mem_used();

		// restore points
		read_map_points();

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		Log.e("Navit", "OnRestart");

		while (Global_Init_Finished == 0)
		{
			Log.e("Navit", "onRestart:Global_Init_Finished==0 !!!!!");
			try
			{
				Thread.sleep(60, 0); // sleep
			}
			catch (InterruptedException e)
			{
			}
		}

		cwthr.NavitActivity2(0);
		NavitVehicle.turn_on_sat_status();
	}

	@SuppressLint("NewApi")
	@Override
	public void onResume()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// System.gc();
		super.onResume();

		// get the intent fresh !! ----------
		startup_intent = this.getIntent();
		// get the intent fresh !! ----------

		PackageInfo pkgInfo;
		Navit_Plugin_001_Installed = false;
		try
		{
			// is the donate version installed?
			pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_msg", 0);
			String sharedUserId = pkgInfo.sharedUserId;
			System.out.println("str nd=" + sharedUserId);
			if (sharedUserId.equals("com.zoffcc.applications.zanavi"))
			{
				System.out.println("##plugin 001##");
				Navit_Plugin_001_Installed = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----

		try
		{
			System.out.println("XXIIXX:111");
			String mid_str = this.getIntent().getExtras().getString("com.zoffcc.applications.zanavi.mid");
			System.out.println("XXIIXX:111a:mid_str=" + mid_str);

			if (mid_str != null)
			{
				if (mid_str.equals("201:UPDATE-APP"))
				{
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
				}
				else if (mid_str.startsWith("202:UPDATE-MAP:"))
				{
					System.out.println("need to update1:" + mid_str);
					System.out.println("need to update2:" + mid_str.substring(15));

					auto_start_update_map(mid_str.substring(15));
				}
			}

			System.out.println("XXIIXX:111b:mid_str=" + mid_str);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("XXIIXX:111:EEEE");
		}

		try
		{
			System.out.println("XXIIXX:" + this.getIntent());
			Bundle bundle77 = this.getIntent().getExtras();
			System.out.println("XXIIXX:" + intent_flags_to_string(this.getIntent().getFlags()));
			if (bundle77 == null)
			{
				System.out.println("XXIIXX:" + "null");
			}
			else
			{
				for (String key : bundle77.keySet())
				{
					Object value = bundle77.get(key);
					System.out.println("XXIIXX:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
				}
			}
		}
		catch (Exception ee22)
		{
			String exst = Log.getStackTraceString(ee22);
			System.out.println("XXIIXX:ERR:" + exst);
		}
		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----
		// ---- Intent dump ----

		is_paused = false;

		Navit_doubleBackToExitPressedOnce = false;

		app_window = getWindow();

		Log.e("Navit", "OnResume");

		while (Global_Init_Finished == 0)
		{
			Log.e("Navit", "OnResume:Global_Init_Finished==0 !!!!!");
			try
			{
				Thread.sleep(30, 0); // sleep
			}
			catch (InterruptedException e)
			{
			}
		}

		//InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		cwthr.NavitActivity2(1);

		try
		{
			NSp.resume_me();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		NavitVehicle.turn_on_sat_status();

		try
		{
			if (wl != null)
			{
				//				try
				//				{
				//					wl.release();
				//				}
				//				catch (Exception e2)
				//				{
				//				}
				wl.acquire();
				Log.e("Navit", "WakeLock: acquire 2");
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

					// If you want to close the dialog, uncomment the line below
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

		// reset "maps too old" flag
		Navit_maps_too_old = false;

		if (Navit_maps_loaded == false)
		{
			Navit_maps_loaded = true;
			// activate all maps
			Log.e("Navit", "**** LOAD ALL MAPS **** start");
			Message msg3 = new Message();
			Bundle b3 = new Bundle();
			b3.putInt("Callback", 20);
			msg3.setData(b3);
			NavitGraphics.callback_handler.sendMessage(msg3);
			Log.e("Navit", "**** LOAD ALL MAPS **** end");
		}

		try
		{
			if (!have_maps_installed())
			{
				// System.out.println("MMMM=no maps installed");
				// show semi transparent box "no maps installed" ------------------
				// show semi transparent box "no maps installed" ------------------
				NavitGraphics.no_maps_container.setVisibility(View.VISIBLE);
				try
				{
					NavitGraphics.no_maps_container.setActivated(true);
				}
				catch (NoSuchMethodError e)
				{
				}

				show_case_001();

				// show semi transparent box "no maps installed" ------------------
				// show semi transparent box "no maps installed" ------------------
			}
			else
			{
				NavitGraphics.no_maps_container.setVisibility(View.INVISIBLE);
				try
				{
					NavitGraphics.no_maps_container.setActivated(false);
				}
				catch (NoSuchMethodError e)
				{
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// draw map no-async
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 64);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String intent_data = null;
		try
		{
			//Log.e("Navit", "**9**A " + startup_intent.getAction());
			//Log.e("Navit", "**9**D " + startup_intent.getDataString());

			int type = 1; // default = assume it's a map coords intent

			try
			{
				String si = startup_intent.getDataString();
				String tmp2 = si.split(":", 2)[0];
				Log.e("Navit", "**9a**A " + startup_intent.getAction());
				Log.e("Navit", "**9a**D " + startup_intent.getDataString() + " " + tmp2);
				if (tmp2.equals("file"))
				{
					Log.e("Navit", "**9b**D " + startup_intent.getDataString() + " " + tmp2);
					if (si.toLowerCase().endsWith(".gpx"))
					{
						Log.e("Navit", "**9c**D " + startup_intent.getDataString() + " " + tmp2);
						type = 4;
					}
				}
			}
			catch (Exception e2)
			{
			}

			if (type != 4)
			{
				Bundle extras = startup_intent.getExtras();
				// System.out.println("DH:001");
				if (extras != null)
				{
					// System.out.println("DH:002");
					long l = extras.getLong("com.zoffcc.applications.zanavi.ZANAVI_INTENT_type");
					// System.out.println("DH:003 l=" + l);
					if (l != 0L)
					{
						// System.out.println("DH:004");
						if (l == Navit.NAVIT_START_INTENT_DRIVE_HOME)
						{
							// System.out.println("DH:005");
							type = 2; // call from drive-home-widget
						}
						// ok, now remove that key
						extras.remove("com.zoffcc.applications.zanavi");
						startup_intent.replaceExtras((Bundle) null);
						// System.out.println("DH:006");
					}
				}
			}

			// ------------------------  BIG LOOP  ------------------------
			// ------------------------  BIG LOOP  ------------------------
			if (type == 2)
			{
				// drive home

				// check if we have a home location
				int home_id = find_home_point();

				if (home_id != -1)
				{
					Message msg7 = progress_handler.obtainMessage();
					Bundle b7 = new Bundle();
					msg7.what = 2; // long Toast message
					b7.putString("text", Navit.get_text("driving to Home Location")); //TRANS
					msg7.setData(b7);
					progress_handler.sendMessage(msg7);

					// clear any previous destinations
					Message msg2 = new Message();
					Bundle b2 = new Bundle();
					b2.putInt("Callback", 7);
					msg2.setData(b2);
					NavitGraphics.callback_handler.sendMessage(msg2);

					// set position to middle of screen -----------------------
					// set position to middle of screen -----------------------
					// set position to middle of screen -----------------------
					//					Message msg67 = new Message();
					//					Bundle b67 = new Bundle();
					//					b67.putInt("Callback", 51);
					//					b67.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
					//					b67.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
					//					msg67.setData(b67);
					//					N_NavitGraphics.callback_handler.sendMessage(msg67);
					// set position to middle of screen -----------------------
					// set position to middle of screen -----------------------
					// set position to middle of screen -----------------------

					try
					{
						Thread.sleep(60);
					}
					catch (Exception e)
					{
					}

					Navit.destination_set();

					// set destination to home location
					//					String lat = String.valueOf(map_points.get(home_id).lat);
					//					String lon = String.valueOf(map_points.get(home_id).lon);
					//					String q = map_points.get(home_id).point_name;
					route_wrapper(map_points.get(home_id).point_name, 0, 0, false, map_points.get(home_id).lat, map_points.get(home_id).lon, true);

					final Thread zoom_to_route_001 = new Thread()
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
										zoom_to_route();
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
					zoom_to_route_001.start();

					//					try
					//					{
					//						show_geo_on_screen(Float.parseFloat(lat), Float.parseFloat(lon));
					//					}
					//					catch (Exception e2)
					//					{
					//						e2.printStackTrace();
					//					}

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
					// no home location set
					Message msg = progress_handler.obtainMessage();
					Bundle b = new Bundle();
					msg.what = 2; // long Toast message
					b.putString("text", Navit.get_text("No Home Location set")); //TRANS
					msg.setData(b);
					progress_handler.sendMessage(msg);
				}
			}
			else if (type == 4)
			{

				if (startup_intent != null)
				{
					// Log.e("Navit", "**7**A " + startup_intent.getAction() + System.currentTimeMillis() + " " + Navit.startup_intent_timestamp);
					if (System.currentTimeMillis() <= Navit.startup_intent_timestamp + 4000L)
					{
						Log.e("Navit", "**7**A " + startup_intent.getAction());
						Log.e("Navit", "**7**D " + startup_intent.getDataString());
						intent_data = startup_intent.getDataString();
						try
						{
							intent_data = java.net.URLDecoder.decode(intent_data, "UTF-8");
						}
						catch (Exception e1)
						{
							e1.printStackTrace();
						}

						// we consumed the intent, so reset timestamp value to avoid double consuming of event
						Navit.startup_intent_timestamp = 0L;

						if (intent_data != null)
						{
							// file:///mnt/sdcard/zanavi_pos_recording_347834278.gpx
							String tmp1;
							tmp1 = intent_data.split(":", 2)[1].substring(2);

							Log.e("Navit", "**7**f=" + tmp1);

							// convert gpx file ---------------------
							convert_gpx_file_real(tmp1);
						}
					}
				}
			}
			else if (type == 1)
			{
				if (startup_intent != null)
				{
					if (System.currentTimeMillis() <= Navit.startup_intent_timestamp + 4000L)
					{
						Log.e("Navit", "**2**A " + startup_intent.getAction());
						Log.e("Navit", "**2**D " + startup_intent.getDataString());
						intent_data = startup_intent.getDataString();
						// we consumed the intent, so reset timestamp value to avoid double consuming of event
						Navit.startup_intent_timestamp = 0L;

						if (intent_data != null)
						{
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
							//							Message msg67 = new Message();
							//							Bundle b67 = new Bundle();
							//							b67.putInt("Callback", 51);
							//							b67.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
							//							b67.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
							//							msg67.setData(b67);
							//							N_NavitGraphics.callback_handler.sendMessage(msg67);
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
						}
					}
					else
					{
						Log.e("Navit", "timestamp for navigate_to expired! not using data");
					}
				}

				System.out.println("SUI:000a " + intent_data);

				if ((intent_data != null) && ((substring_without_ioobe(intent_data, 0, 18).equals("google.navigation:")) || (substring_without_ioobe(intent_data, 0, 23).equals("http://maps.google.com/")) || (substring_without_ioobe(intent_data, 0, 24).equals("https://maps.google.com/"))))
				{

					System.out.println("SUI:000b");

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
					// h: http://maps.google.com/?q=48.222210,16.387058&z=16
					// i: https://maps.google.com/?q=48.222210,16.387058&z=16
					// i:,h: https://maps.google.com/maps/place?ftid=0x476d07075e933fc5:0xccbeba7fe1e3dd36&q=48.222210,16.387058&ui=maps_mini
					//
					// ??!!new??!!: http://maps.google.com/?cid=10549738100504591748&hl=en&gl=gb

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
							// if h: then show target
							if (substring_without_ioobe(intent_data, 0, 23).equals("http://maps.google.com/"))
							{
								Uri uri = Uri.parse(intent_data);
								Log.e("Navit", "target found (h): " + uri.getQueryParameter("q"));
								parsable = true;
								intent_data = "google.navigation:ll=" + uri.getQueryParameter("q") + "&q=Target";
							}
							// if i: then show target
							else if (substring_without_ioobe(intent_data, 0, 24).equals("https://maps.google.com/"))
							{
								Uri uri = Uri.parse(intent_data);
								Log.e("Navit", "target found (i): " + uri.getQueryParameter("q"));
								parsable = true;
								intent_data = "google.navigation:ll=" + uri.getQueryParameter("q") + "&q=Target";
							}
							// if d: then start target search
							else if ((substring_without_ioobe(intent_data, 0, 20).equals("google.navigation:q=")) && ((!substring_without_ioobe(intent_data, 20, 21).equals('+')) && (!substring_without_ioobe(intent_data, 20, 21).equals('-')) && (!substring_without_ioobe(intent_data, 20, 22).matches("[0-9][0-9]"))))
							{
								Log.e("Navit", "target found (d): " + intent_data.split("q=", -1)[1]);
								Navit.NavitStartupAlreadySearching = true;
								start_targetsearch_from_intent(intent_data.split("q=", -1)[1]);
								// dont use this here, already starting search, so set to "false"
								parsable = false;
								unparsable_info_box = false;
							}
							// if b: then remodel the input string to look like a:
							else if (substring_without_ioobe(intent_data, 0, 20).equals("google.navigation:q="))
							{
								intent_data = "ll=" + intent_data.split("q=", -1)[1] + "&q=Target";
								Log.e("Navit", "target found (b): " + intent_data);
								parsable = true;
							}
							// if g: [google.navigation:///?ll=49.4086,17.4855&...] then remodel the input string to look like a:
							else if (substring_without_ioobe(intent_data, 0, 25).equals("google.navigation:///?ll="))
							{
								intent_data = "google.navigation:ll=" + intent_data.split("ll=", -1)[1].split("&", -1)[0] + "&q=Target";
								Log.e("Navit", "target found (g): " + intent_data);
								parsable = true;
							}
							// if e: then remodel the input string to look like a:
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&").length > 1) && (substring_without_ioobe(intent_data.split("&")[1], 0, 1).toLowerCase().equals("f")))
							{
								int idx = intent_data.indexOf("&");
								intent_data = substring_without_ioobe(intent_data, 0, idx) + "&q=Target";
								Log.e("Navit", "target found (e): " + intent_data);
								parsable = true;
							}
							// if f: then remodel the input string to look like a:
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&").length > 1))
							{
								int idx = intent_data.indexOf("&");
								intent_data = intent_data.substring(0, idx) + "&q=Target";
								Log.e("Navit", "target found (f): " + intent_data);
								parsable = true;
							}
							// already looks like a: just set flag
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&q=").length > 1))
							{
								// dummy, just set the flag
								Log.e("Navit", "target found (a): " + intent_data);
								Log.e("Navit", "target found (a): " + intent_data.split("&q=").length);
								parsable = true;
							}
							// if c: then remodel the input string to look like a:
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&q=").length < 2))
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
						NavitGraphics.callback_handler.sendMessage(msg);

						final Thread zoom_to_route_002 = new Thread()
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
											zoom_to_route();
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
						zoom_to_route_002.start();

						//						try
						//						{
						//							Thread.sleep(400);
						//						}
						//						catch (InterruptedException e)
						//						{
						//						}
						//
						//						//						try
						//						//						{
						//						//							show_geo_on_screen(Float.parseFloat(lat), Float.parseFloat(lon));
						//						//						}
						//						//						catch (Exception e2)
						//						//						{
						//						//							e2.printStackTrace();
						//						//						}

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
				else if ((intent_data != null) && (substring_without_ioobe(intent_data, 0, 10).equals("geo:0,0?q=")))
				{
					// g: geo:0,0?q=wien%20burggasse

					System.out.println("SUI:001");

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

					System.out.println("SUI:002");

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
				else if ((intent_data != null) && (substring_without_ioobe(intent_data, 0, 4).equals("geo:")))
				{
					// g: geo:16.8,46.3?z=15

					System.out.println("SUI:002a");

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

						// set nice zoomlevel before we show destination
						//						int zoom_want = zoom1;
						//						//
						//						Message msg = new Message();
						//						Bundle b = new Bundle();
						//						b.putInt("Callback", 33);
						//						b.putString("s", Integer.toString(zoom_want));
						//						msg.setData(b);
						//						try
						//						{
						//							N_NavitGraphics.callback_handler.sendMessage(msg);
						//							Navit.GlobalScaleLevel = Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL;
						//							if ((zoom_want > 8) && (zoom_want < 17))
						//							{
						//								Navit.GlobalScaleLevel = (int) (Math.pow(2, (18 - zoom_want)));
						//								System.out.println("GlobalScaleLevel=" + Navit.GlobalScaleLevel);
						//							}
						//						}
						//						catch (Exception e)
						//						{
						//							e.printStackTrace();
						//						}
						//						if (PREF_save_zoomlevel)
						//						{
						//							setPrefs_zoomlevel();
						//						}
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
						//						final Thread zoom_to_route_003 = new Thread()
						//						{
						//							@Override
						//							public void run()
						//							{
						//								try
						//								{
						//									Thread.sleep(200);
						//									show_geo_on_screen(lat1, lon1);
						//								}
						//								catch (Exception e)
						//								{
						//								}
						//							}
						//						};
						//						zoom_to_route_003.start();

					}
				}
			}

			System.out.println("SUI:099 XX" + substring_without_ioobe(intent_data, 0, 10) + "XX");

			// clear intent
			startup_intent = null;
			// ------------------------  BIG LOOP  ------------------------
			// ------------------------  BIG LOOP  ------------------------
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("SUI:199");
		}

		// clear intent
		startup_intent = null;

		// hold all map drawing -----------
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 69);
		msg.setData(b);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// hold all map drawing -----------

		getPrefs();
		activatePrefs();
		sun_moon__mLastCalcSunMillis = -1L;

		push_pin_view = (ImageView) findViewById(R.id.bottom_slide_left_side);
		if (p.PREF_follow_gps)
		{
			push_pin_view.setImageResource(R.drawable.pin1_down);
		}
		else
		{
			push_pin_view.setImageResource(R.drawable.pin1_up);
		}

		// paint for bitmapdrawing on map
		if (p.PREF_use_anti_aliasing)
		{
			NavitGraphics.paint_for_map_display.setAntiAlias(true);
		}
		else
		{
			NavitGraphics.paint_for_map_display.setAntiAlias(false);
		}
		if (p.PREF_use_map_filtering)
		{
			NavitGraphics.paint_for_map_display.setFilterBitmap(true);
		}
		else
		{
			NavitGraphics.paint_for_map_display.setFilterBitmap(false);
		}

		// activate gps AFTER 3g-location
		NavitVehicle.turn_on_precise_provider();

		// allow all map drawing -----------
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", 70);
		msg.setData(b);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// allow all map drawing -----------

		// --- disabled --- NavitVehicle.set_last_known_pos_fast_provider();

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

		// ----- check if we have some index files downloaded -----
		if (Navit.have_maps_installed())
		{
			if (Navit_maps_too_old)
			{
				TextView no_maps_text = (TextView) this.findViewById(R.id.no_maps_text);
				no_maps_text.setText("\n\n\n" + Navit.get_text("Some Maps are too old!") + "\n" + Navit.get_text("Please update your maps") + "\n\n");

				try
				{
					NavitGraphics.no_maps_container.setVisibility(View.VISIBLE);
					try
					{
						NavitGraphics.no_maps_container.setActivated(true);
					}
					catch (NoSuchMethodError e)
					{
					}
					NavitGraphics.no_maps_container.bringToFront();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				allow_use_index_search();
				if (Navit_index_on_but_no_idx_files)
				{
					TextView no_maps_text = (TextView) this.findViewById(R.id.no_maps_text);
					no_maps_text.setText("\n\n\n" + Navit.get_text("No Index for some Maps") + "\n" + Navit.get_text("Please update your maps") + "\n\n");

					try
					{
						NavitGraphics.no_maps_container.setVisibility(View.VISIBLE);
						try
						{
							NavitGraphics.no_maps_container.setActivated(true);
						}
						catch (NoSuchMethodError e)
						{
						}
						NavitGraphics.no_maps_container.bringToFront();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					try
					{
						NavitGraphics.no_maps_container.setVisibility(View.INVISIBLE);
						try
						{
							NavitGraphics.no_maps_container.setActivated(false);
						}
						catch (NoSuchMethodError e)
						{
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		// ----- check if we have some index files downloaded -----

		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----
		try
		{
			if (!NavitVehicle.is_pos_recording)
			{
				if (p.PREF_enable_debug_write_gpx)
				{
					NavitVehicle.pos_recording_start();
					NavitVehicle.pos_recording_add(0, 0, 0, 0, 0, 0);
				}
			}
		}
		catch (Exception e)
		{
		}
		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----

		// glSurfaceView.onResume();

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);

		if (Navit.CIDEBUG == 1)
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						System.out.println("DR_run_all_yaml_tests --> want");

						if (CIRUN == false)
						{
							System.out.println("DR_run_all_yaml_tests --> do");
							CIRUN = true;
							Thread.sleep(20000); // 20 min.
							ZANaviDebugReceiver.DR_run_all_yaml_tests();
						}
					}
					catch (Exception e)
					{
					}
				}
			}.start();
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onPause()
	{

		// if COMM stuff is running, stop it!
		ZANaviDebugReceiver.stop_me = true;

		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----
		// -- dump all callbacks --
		try
		{
			if (p.PREF_enable_debug_functions)
			{
				Message msg99a = new Message();
				Bundle b99a = new Bundle();
				b99a.putInt("Callback", 100);
				msg99a.setData(b99a);
				N_NavitGraphics.callback_handler.sendMessage(msg99a);
			}
		}
		catch (Exception e)
		{
		}
		// -- dump all callbacks --
		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----

		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----
		try
		{
			if (!Navit.is_navigating)
			{
				if (p.PREF_enable_debug_write_gpx)
				{
					NavitVehicle.pos_recording_end();
				}
			}
		}
		catch (Exception e)
		{
		}
		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----

		// System.out.println("@@ onPause @@");
		Log.e("Navit", "OnPause");
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

		//		if (!Navit.is_navigating)
		//		{
		//			try
		//			{
		//				mTts.stop();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			try
		//			{
		//				mTts.shutdown();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			mTts = null;
		//		}

		super.onPause();

		// signal to backupmanager that data "is / could have" changed
		try
		{
			Class.forName("android.app.backup.BackupManager");
			BackupManager b = (BackupManager) backupManager;
			b.dataChanged();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		turn_off_compass();

		// System.out.println("XXNAV: onpause:001");
		if (!Navit.is_navigating)
		{
			// System.out.println("XXNAV: onpause:002");
			NavitVehicle.turn_off_all_providers();
			NavitVehicle.turn_off_sat_status();
			// System.out.println("XXNAV: onpause:003");
		}

		// Log.e("Navit", "OnPause");
		cwthr.NavitActivity2(-1);

		Navit.show_mem_used();

		try
		{
			if (wl != null)
			{
				wl.release();
				Log.e("Navit", "WakeLock: release 1");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (wl_cpu != null)
			{
				if (wl_cpu.isHeld())
				{
					wl_cpu.release();
					Log.e("Navit", "WakeLock CPU: release 1");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// glSurfaceView.onPause();

		is_paused = true;
	}

	@Override
	public void onStop()
	{
		super.onStop();
		Log.e("Navit", "OnStop");

		if (!Navit.is_navigating)
		{
			NavitVehicle.turn_off_all_providers();
			NavitVehicle.turn_off_sat_status();
		}

		cwthr.NavitActivity2(-2);
		Navit.show_mem_used();

		//		if (!Navit.is_navigating)
		//		{
		//			try
		//			{
		//				mTts.stop();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			try
		//			{
		//				mTts.shutdown();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			mTts = null;
		//		}

		// save points
		write_map_points();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.e("Navit", "OnDestroy");

		try
		{
			try
			{
				plugin_api.removeListener(zclientListener);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i("NavitPlugin", "Failed to remove Listener", e);
			}
			unbindService(serviceConnection);
			Log.i("NavitPlugin", "Unbind from the service");
		}
		catch (Throwable t)
		{
			// catch any issues, typical for destroy routines
			// even if we failed to destroy something, we need to continue destroying
			Log.i("NavitPlugin", "Failed to unbind from the service", t);
		}

		try
		{
			mTts.stop();
		}
		catch (Exception e)
		{

		}

		try
		{
			mTts.shutdown();
		}
		catch (Exception e)
		{

		}

		mTts = null;

		// ----- service stop -----
		// ----- service stop -----
		System.out.println("Navit:onDestroy -> stop ZANaviMapDownloaderService ---------");
		stopService(Navit.ZANaviMapDownloaderServiceIntent);
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
		}
		// ----- service stop -----
		// ----- service stop -----

		NavitActivity(-3);
		Navit.show_mem_used();
	}

	public void setActivityResult(int requestCode, NavitActivityResult ActivityResult)
	{
		Log.e("Navit", "setActivityResult " + requestCode);
		ActivityResults[requestCode] = ActivityResult;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			if (cur_menu != null)
			{
				// open the overflow menu
				cur_menu.performIdentifierAction(R.id.item_overflow, 0);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	//	@Override
	//	public boolean onKeyDown(int keyCode, KeyEvent event)
	//	{
	//		if (keyCode == KeyEvent.KEYCODE_MENU)
	//		{
	//			return true;
	//		}
	//		return super.onKeyUp(keyCode, event);
	//	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		//
		menu.clear();

		// load the menu from XML
		getMenuInflater().inflate(R.menu.actionbaricons, menu);

		// NavitOverflowMenuItemID = R.id.item_overflow_menu_button;
		menu.findItem(R.id.share_menu_destination).setTitle(Navit.get_text("Share Destination"));
		menu.findItem(R.id.share_menu_location).setTitle(Navit.get_text("Share my Location"));
		menu.findItem(R.id.search_menu_offline).setTitle(get_text("address search (offline)"));
		menu.findItem(R.id.search_menu_online).setTitle(get_text("address search (online)"));
		menu.findItem(R.id.item_recentdest_menu_button).setTitle(get_text("Recent destinations"));
		menu.findItem(R.id.item_settings_menu_button).setTitle(get_text("Settings"));
		menu.findItem(R.id.item_search_menu_button).setTitle(get_text("Search"));
		menu.findItem(R.id.item_download_menu_button).setTitle(get_text("downloading map"));
		//
		menu.findItem(R.id.overflow_share_location).setTitle(Navit.get_text("Share my Location"));
		menu.findItem(R.id.overflow_share_destination).setTitle(Navit.get_text("Share Destination"));
		menu.findItem(R.id.overflow_settings).setTitle(Navit.get_text("Settings"));
		menu.findItem(R.id.overflow_zoom_to_route).setTitle(Navit.get_text("Zoom to Route"));

		if (ZANaviNormalDonateActivity.on_amazon_device)
		{
			menu.findItem(R.id.overflow_donate_item).setTitle(Navit.get_text("Donate"));
		}
		else
		{
			menu.findItem(R.id.overflow_donate_item).setTitle(Navit.get_text("Donate with Google Play"));
		}
		menu.findItem(R.id.overflow_donate_bitcoins_item).setTitle(Navit.get_text("Donate with Bitcoin"));
		//. TRANSLATORS: text to translate is: exit ZANavi
		menu.findItem(R.id.overflow_exit).setTitle(Navit.get_text("exit navit"));
		menu.findItem(R.id.overflow_toggle_poi).setTitle(Navit.get_text("toggle POI"));
		menu.findItem(R.id.overflow_announcer_on).setTitle(Navit.get_text("Announcer On"));
		menu.findItem(R.id.overflow_announcer_off).setTitle(Navit.get_text("Announcer Off"));
		menu.findItem(R.id.overflow_download_maps).setTitle(Navit.get_text("download maps"));
		menu.findItem(R.id.overflow_delete_maps).setTitle(Navit.get_text("delete maps"));
		menu.findItem(R.id.overflow_maps_age).setTitle(Navit.get_text("show Maps age"));
		menu.findItem(R.id.overflow_coord_dialog).setTitle(Navit.get_text("Coord Dialog"));
		menu.findItem(R.id.overflow_add_traffic_block).setTitle(Navit.get_text("add Traffic block"));
		menu.findItem(R.id.overflow_clear_traffic_block).setTitle(Navit.get_text("clear Traffic blocks"));
		menu.findItem(R.id.overflow_convert_gpx_file).setTitle(Navit.get_text("convert GPX file"));
		menu.findItem(R.id.overflow_replay_gps_file).setTitle(Navit.get_text("replay a ZANavi gps file"));
		menu.findItem(R.id.overflow_yaml_tests).setTitle(Navit.get_text("run YAML tests"));
		menu.findItem(R.id.overflow_clear_gpx_map).setTitle(Navit.get_text("clear GPX map"));
		// menu.findItem(R.id.overflow_dummy2)
		menu.findItem(R.id.overflow_demo_v_normal).setTitle(get_text("Demo Vehicle") + " [normal]");
		menu.findItem(R.id.overflow_demo_v_fast).setTitle(get_text("Demo Vehicle") + " [fast]");
		menu.findItem(R.id.overflow_speech_texts).setTitle(Navit.get_text("Speech Texts"));
		menu.findItem(R.id.overflow_nav_commands).setTitle(Navit.get_text("Nav. Commands"));
		menu.findItem(R.id.overflow_toggle_route_graph).setTitle(Navit.get_text("toggle Routegraph"));
		//menu.findItem(R.id.overflow_dummy1)
		menu.findItem(R.id.overflow_export_map_points_to_sdcard).setTitle(Navit.get_text("export Destinations"));
		menu.findItem(R.id.overflow_import_map_points_from_sdcard).setTitle(Navit.get_text("import Destinations"));
		menu.findItem(R.id.overflow_send_feedback).setTitle(Navit.get_text("send feedback"));
		menu.findItem(R.id.overflow_online_help).setTitle(Navit.get_text("online Help"));
		//. TRANSLATORS: it means: "show current target in google maps"
		//. TRANSLATORS: please keep this text short, to fit in the android menu!
		menu.findItem(R.id.overflow_target_in_gmaps).setTitle(Navit.get_text("Target in gmaps"));
		//
		//
		menu.findItem(R.id.item_share_menu_button).setTitle(get_text("Share"));

		Display display_ = getWindowManager().getDefaultDisplay();
		Log.e("Navit", "Navit width in DP -> " + display_.getWidth() / Navit.metrics.density);
		Log.e("Navit", "Navit width in DP -> density=" + Navit.metrics.density);

		try
		{
			View v4 = findViewById(R.id.item_settings_menu_button);
			// Log.e("Navit", "Navit width in DP -> v4=" + v4);
			if ((v4 != null) && (v4.getWidth() > 0))
			{
				Log.e("Navit", "Navit width in DP -> v4.w=" + v4.getWidth());
				MenuItem menuItem = menu.findItem(R.id.item_settings_menu_button);
				// Log.e("Navit", "Navit width in DP -> mi=" + menuItem);
				// Log.e("Navit", "Navit width in DP -> i=" + menuItem.getIcon());
				Log.e("Navit", "Navit width in DP -> i.w=" + menuItem.getIcon().getIntrinsicWidth());
				actionbar_item_width = (int) ((v4.getWidth() + (menuItem.getIcon().getIntrinsicWidth() * 1.5f)) / 2);
			}
			else
			{
				MenuItem menuItem = menu.findItem(R.id.item_settings_menu_button);
				// Log.e("Navit", "Navit width in DP -> mi=" + menuItem);
				// Log.e("Navit", "Navit width in DP -> i=" + menuItem.getIcon());
				Log.e("Navit", "Navit width in DP -> i.w=" + menuItem.getIcon().getIntrinsicWidth());
				actionbar_item_width = (int) ((menuItem.getIcon().getIntrinsicWidth()) * 1.7f);
			}

			actionbar_items_will_fit = display_.getWidth() / actionbar_item_width;
			Log.e("Navit", "Navit width in DP -> number of items that will fit=" + actionbar_items_will_fit);
			if (actionbar_items_will_fit > 6) // now we need to fit max. 6 items on actionbar
			{
				actionbar_all_items_will_fit = true;
			}
			else
			{
				actionbar_all_items_will_fit = false;
			}
		}
		catch (Exception e)
		{
			if ((display_.getWidth() / Navit.metrics.density) < NAVIT_MIN_HORIZONTAL_DP_FOR_ACTIONBAR)
			{
				actionbar_all_items_will_fit = false;
			}
			else
			{
				actionbar_all_items_will_fit = true;
			}
		}

		if (actionbar_all_items_will_fit == false)
		{
			menu.findItem(R.id.item_share_menu_button).setVisible(false);
			menu.findItem(R.id.overflow_share_location).setVisible(true);
			if (NavitGraphics.CallbackDestinationValid2() == 0)
			{
				menu.findItem(R.id.overflow_share_destination).setVisible(false);
			}
			else
			{
				menu.findItem(R.id.overflow_share_destination).setVisible(true);
			}

			if (actionbar_items_will_fit < 6)
			{
				// also push the settings icons to overflow menu
				menu.findItem(R.id.item_settings_menu_button).setVisible(false);
				menu.findItem(R.id.overflow_settings).setVisible(true);
			}
			else
			{
				menu.findItem(R.id.item_settings_menu_button).setVisible(true);
				menu.findItem(R.id.overflow_settings).setVisible(false);
			}
		}
		else
		{
			menu.findItem(R.id.item_settings_menu_button).setVisible(true);
			menu.findItem(R.id.overflow_settings).setVisible(false);
			menu.findItem(R.id.overflow_share_location).setVisible(false);
			menu.findItem(R.id.overflow_share_destination).setVisible(false);
			menu.findItem(R.id.item_share_menu_button).setVisible(true);
		}

		cur_menu = menu;

		if (actionabar_download_icon_visible)
		{
			menu.findItem(R.id.item_download_menu_button).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.item_download_menu_button).setVisible(false);
		}

		if (NavitGraphics.CallbackDestinationValid2() > 0)
		{
			menu.findItem(R.id.item_endnavigation_menu_button).setVisible(true);
			menu.findItem(R.id.item_endnavigation_menu_button).setTitle(get_text("Stop Navigation"));
			menu.findItem(R.id.overflow_zoom_to_route).setVisible(true);
			menu.findItem(R.id.overflow_target_in_gmaps).setVisible(true);
			menu.findItem(R.id.share_menu_destination).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.item_endnavigation_menu_button).setVisible(false);
			menu.findItem(R.id.overflow_zoom_to_route).setVisible(false);
			menu.findItem(R.id.overflow_target_in_gmaps).setVisible(false);
			menu.findItem(R.id.share_menu_destination).setVisible(false);
		}

		if (Navit_Announcer == true)
		{
			menu.findItem(R.id.overflow_announcer_off).setVisible(true);
			menu.findItem(R.id.overflow_announcer_on).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.overflow_announcer_off).setVisible(false);
			menu.findItem(R.id.overflow_announcer_on).setVisible(true);
		}

		if (p.PREF_enable_debug_functions)
		{
			menu.findItem(R.id.overflow_dummy2).setVisible(true);
			menu.findItem(R.id.overflow_demo_v_normal).setVisible(true);
			menu.findItem(R.id.overflow_demo_v_fast).setVisible(true);
			menu.findItem(R.id.overflow_speech_texts).setVisible(true);
			menu.findItem(R.id.overflow_nav_commands).setVisible(true);
			menu.findItem(R.id.overflow_toggle_route_graph).setVisible(true);
			menu.findItem(R.id.overflow_replay_gps_file).setVisible(true);
			menu.findItem(R.id.overflow_yaml_tests).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.overflow_dummy2).setVisible(false);
			menu.findItem(R.id.overflow_demo_v_normal).setVisible(false);
			menu.findItem(R.id.overflow_demo_v_fast).setVisible(false);
			menu.findItem(R.id.overflow_speech_texts).setVisible(false);
			menu.findItem(R.id.overflow_nav_commands).setVisible(false);
			menu.findItem(R.id.overflow_toggle_route_graph).setVisible(false);
			menu.findItem(R.id.overflow_replay_gps_file).setVisible(false);
			menu.findItem(R.id.overflow_yaml_tests).setVisible(false);
		}

		return true;
	}

	public void start_targetsearch_from_intent(String target_address)
	{
		Navit_last_address_partial_match = true; // this will overwrite the default setting --> this is not good
		Navit_last_address_search_string = target_address;
		Navit_last_address_hn_string = "";

		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		Boolean use_online_searchmode_here = true;
		Boolean hide_duplicates_searchmode_here = false;
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------

		int dialog_num_;

		if (use_online_searchmode_here)
		{
			dialog_num_ = Navit.SEARCHRESULTS_WAIT_DIALOG;
			Navit.use_index_search = false;
			Log.e("Navit", "*google*:online search");
		}
		else
		{
			dialog_num_ = Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE;
			Navit.use_index_search = Navit.allow_use_index_search();
		}

		// clear results
		Navit.NavitAddressResultList_foundItems.clear();
		Navit.Navit_Address_Result_double_index.clear();
		Navit.NavitSearchresultBarIndex = -1;
		Navit.NavitSearchresultBar_title = "";
		Navit.NavitSearchresultBar_text = "";
		search_hide_duplicates = false;

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
				Log.e("Navit", "*google*:call-11: (0)num " + dialog_num_);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (hide_duplicates_searchmode_here)
			{
				search_hide_duplicates = true;
				// hide duplicates when searching
				// hide duplicates when searching
				Message msg22 = new Message();
				Bundle b22 = new Bundle();
				b22.putInt("Callback", 45);
				msg22.setData(b22);
				NavitGraphics.callback_handler.sendMessage(msg22);
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//System.out.println("menu button pressed ID=" + item.getItemId());

		if ((item.getItemId() == R.id.share_menu_destination) || (item.getItemId() == R.id.overflow_share_destination) || (item.getItemId() == 23020))
		{
			// System.out.println("share destination pressed ID=" + item.getItemId());
			// ------------
			// ------------
			// share the current destination with your friends
			String current_target_string2 = NavitGraphics.CallbackGeoCalc(4, 1, 1);
			if (current_target_string2.equals("x:x"))
			{
				Log.e("Navit", "no target set!");
			}
			else
			{
				try
				{
					String tmp[] = current_target_string2.split(":", 2);

					if (Navit.OSD_route_001.arriving_time_valid)
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), Navit.OSD_route_001.arriving_time, true);
					}
					else
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), "", true);
					}

				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e("Navit", "problem with target!");
				}
			}
			return true;
		}
		else if (item.getItemId() == R.id.item_download_menu_button)
		{
			// System.out.println("download icon pressed(1) ID=" + item.getItemId());

			Intent mapdownload_cancel_activity = new Intent(this, ZANaviDownloadMapCancelActivity.class);
			mapdownload_cancel_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(mapdownload_cancel_activity);

			return true;
		}
		else if ((item.getItemId() == R.id.share_menu_location) || (item.getItemId() == R.id.overflow_share_location) || (item.getItemId() == 23000))
		{
			// System.out.println("share location pressed ID=" + item.getItemId());
			// ------------
			// ------------
			// share the current location with your friends
			location_coords cur_target = null;
			try
			{
				geo_coord tmp = get_current_vehicle_position();
				cur_target.lat = tmp.Latitude;
				cur_target.lon = tmp.Longitude;
				// cur_target = NavitVehicle.get_last_known_pos();
			}
			catch (Exception e)
			{
			}

			if (cur_target == null)
			{
				Log.e("Navit", "no location found!");
			}
			else
			{
				try
				{
					share_location(String.valueOf(cur_target.lat), String.valueOf(cur_target.lon), Navit.get_text("my Location"), Navit.get_text("my Location"), "", false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e("Navit", "problem with location!");
				}
			}
			return true;
		}
		else if ((item.getItemId() == R.id.item_settings_menu_button) || (item.getItemId() == R.id.overflow_settings) || (item.getItemId() == 490))
		{
			// open settings menu
			Intent settingsActivity = new Intent(getBaseContext(), NavitPreferences.class);
			startActivity(settingsActivity);

			return true;
		}
		else if (item.getItemId() == R.id.search_menu_offline)
		{
			// ok startup address search activity (offline binfile search)
			Navit.use_index_search = Navit.allow_use_index_search();
			Intent search_intent2 = new Intent(this, NavitAddressSearchActivity.class);
			search_intent2.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent2.putExtra("address_string", Navit_last_address_search_string);
			search_intent2.putExtra("hn_string", Navit_last_address_hn_string);
			search_intent2.putExtra("type", "offline");
			search_intent2.putExtra("search_country_id", Navit_last_address_search_country_id);

			String pm_temp2 = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp2 = "1";
			}

			search_intent2.putExtra("partial_match", pm_temp2);

			Navit.use_index_search = Navit.allow_use_index_search();
			if (Navit.use_index_search)
			{
				this.startActivityForResult(search_intent2, NavitAddressResultList_id);
			}
			else
			{
				this.startActivityForResult(search_intent2, NavitAddressSearch_id_offline);
			}

			return true;
		}
		else if (item.getItemId() == R.id.search_menu_online)
		{
			// ok startup address search activity (online google maps search)
			Navit.use_index_search = false;
			Intent search_intent = new Intent(this, NavitAddressSearchActivity.class);
			search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent.putExtra("address_string", Navit_last_address_search_string);
			//search_intent.putExtra("hn_string", Navit_last_address_hn_string);
			search_intent.putExtra("type", "online");
			String pm_temp = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp = "1";
			}
			search_intent.putExtra("partial_match", pm_temp);
			this.startActivityForResult(search_intent, NavitAddressSearch_id_online);

			return true;
		}
		else if (item.getItemId() == R.id.item_endnavigation_menu_button)
		{
			// stop navigation (this menu should only appear when navigation is actually on!)
			NavitGraphics.deactivate_nav_wakelock();
			Message msg2 = new Message();
			Bundle b2 = new Bundle();
			b2.putInt("Callback", 7);
			msg2.setData(b2);
			NavitGraphics.callback_handler.sendMessage(msg2);
			Log.e("Navit", "stop navigation");

			if (p.PREF_enable_debug_write_gpx)
			{
				NavitVehicle.speech_recording_end();
			}

			// redraw all OSD elements
			Navit.OSD_route_001.arriving_time_valid = false;
			Navit.OSD_route_001.arriving_secs_to_dest_valid = false;
			Navit.OSD_route_001.driving_distance_to_target_valid = false;
			Navit.OSD_nextturn.nextturn_distance_valid = false;
			Navit.OSD_nextturn.nextturn_image_valid = false;
			Navit.OSD_compass.direct_distance_to_target_valid = false;
			NavitGraphics.OSD_new.postInvalidate();

			return true;
		}
		else if (item.getItemId() == R.id.item_recentdest_menu_button)
		{
			// show recent destination list
			Intent i2 = new Intent(this, NavitRecentDestinationActivity.class);
			this.startActivityForResult(i2, Navit.NavitRecentDest_id);

			return true;
		}
		else if (item.getItemId() == R.id.overflow_zoom_to_route)
		{
			return onOptionsItemSelected_wrapper(11);
		}
		else if (item.getItemId() == R.id.overflow_donate_item)
		{
			return onOptionsItemSelected_wrapper(26);
		}
		else if (item.getItemId() == R.id.overflow_donate_bitcoins_item)
		{
			return onOptionsItemSelected_wrapper(27);
		}
		else if (item.getItemId() == R.id.overflow_exit)
		{
			return onOptionsItemSelected_wrapper(99);
		}
		else if (item.getItemId() == R.id.overflow_toggle_poi)
		{
			return onOptionsItemSelected_wrapper(5);
		}
		else if (item.getItemId() == R.id.overflow_announcer_on)
		{
			return onOptionsItemSelected_wrapper(13);
		}
		else if (item.getItemId() == R.id.overflow_announcer_off)
		{
			return onOptionsItemSelected_wrapper(12);
		}
		else if (item.getItemId() == R.id.overflow_download_maps)
		{
			return onOptionsItemSelected_wrapper(3);
		}
		else if (item.getItemId() == R.id.overflow_delete_maps)
		{
			return onOptionsItemSelected_wrapper(8);
		}
		else if (item.getItemId() == R.id.overflow_maps_age)
		{
			return onOptionsItemSelected_wrapper(17);
		}
		else if (item.getItemId() == R.id.overflow_coord_dialog)
		{
			return onOptionsItemSelected_wrapper(19);
		}
		else if (item.getItemId() == R.id.overflow_add_traffic_block)
		{
			return onOptionsItemSelected_wrapper(21);
		}
		else if (item.getItemId() == R.id.overflow_clear_traffic_block)
		{
			return onOptionsItemSelected_wrapper(22);
		}
		else if (item.getItemId() == R.id.overflow_convert_gpx_file)
		{
			return onOptionsItemSelected_wrapper(20);
		}
		else if (item.getItemId() == R.id.overflow_clear_gpx_map)
		{
			return onOptionsItemSelected_wrapper(23);
		}
		else if (item.getItemId() == R.id.overflow_replay_gps_file)
		{
			return onOptionsItemSelected_wrapper(28);
		}
		else if (item.getItemId() == R.id.overflow_yaml_tests)
		{
			return onOptionsItemSelected_wrapper(609);
		}
		else if (item.getItemId() == R.id.overflow_demo_v_normal)
		{
			return onOptionsItemSelected_wrapper(601);
		}
		else if (item.getItemId() == R.id.overflow_demo_v_fast)
		{
			return onOptionsItemSelected_wrapper(604);
		}
		else if (item.getItemId() == R.id.overflow_speech_texts)
		{
			return onOptionsItemSelected_wrapper(602);
		}
		else if (item.getItemId() == R.id.overflow_nav_commands)
		{
			return onOptionsItemSelected_wrapper(603);
		}
		else if (item.getItemId() == R.id.overflow_toggle_route_graph)
		{
			return onOptionsItemSelected_wrapper(605);
		}
		else if (item.getItemId() == R.id.overflow_export_map_points_to_sdcard)
		{
			return onOptionsItemSelected_wrapper(607);
		}
		else if (item.getItemId() == R.id.overflow_import_map_points_from_sdcard)
		{
			return onOptionsItemSelected_wrapper(608);
		}
		else if (item.getItemId() == R.id.overflow_send_feedback)
		{
			return onOptionsItemSelected_wrapper(24);
		}
		else if (item.getItemId() == R.id.overflow_online_help)
		{
			return onOptionsItemSelected_wrapper(16);
		}
		else if (item.getItemId() == R.id.overflow_target_in_gmaps)
		{
			return onOptionsItemSelected_wrapper(15);
		}
		//		else
		//		{
		//			return onOptionsItemSelected_wrapper(item.getItemId());
		//		}

		return false;
	}

	@SuppressLint("NewApi")
	public boolean onOptionsItemSelected_wrapper(int id)
	{
		// Handle item selection
		switch (id)
		{
		case 1:
			// zoom in
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 1);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
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
			NavitGraphics.callback_handler.sendMessage(msg);
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
			toggle_poi_pref();
			set_poi_layers();
			draw_map();
			break;
		case 6:
			// ok startup address search activity (online google maps search)
			Navit.use_index_search = false;
			Intent search_intent = new Intent(this, NavitAddressSearchActivity.class);
			search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent.putExtra("address_string", Navit_last_address_search_string);
			//search_intent.putExtra("hn_string", Navit_last_address_hn_string);
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
			Navit.use_index_search = Navit.allow_use_index_search();
			Intent search_intent2 = new Intent(this, NavitAddressSearchActivity.class);
			search_intent2.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent2.putExtra("address_string", Navit_last_address_search_string);
			search_intent2.putExtra("hn_string", Navit_last_address_hn_string);
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
			NavitGraphics.callback_handler.sendMessage(msg2);
			Log.e("Navit", "stop navigation");
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
			NavitGraphics.callback_handler.sendMessage(msg);
			try
			{
				invalidateOptionsMenu();
			}
			catch (Exception e)
			{
			}
			break;
		case 13:
			// announcer on
			Navit_Announcer = true;
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 35);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
			try
			{
				invalidateOptionsMenu();
			}
			catch (Exception e)
			{
			}
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
			if (FDBL)
			{
				url = "http://fd.zanavi.cc/manual";
			}
			if (NavitTextTranslations.main_language.toLowerCase().equals("de"))
			{
				// show german manual
				url = "http://zanavi.cc/index.php/Manual/de";
				if (FDBL)
				{
					url = "http://fd.zanavi.cc/manualde";
				}
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
			File a = new File(p.PREF_last_selected_dir_gpxfiles);
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
				String traffic = "";
				if (Navit.GFX_OVERSPILL)
				{
					traffic = NavitGraphics.CallbackGeoCalc(7, (int) (NavitGraphics.Global_dpi_factor * (NavitGraphics.mCanvasWidth / 2 + NavitGraphics.mCanvasWidth_overspill)), (int) (NavitGraphics.Global_dpi_factor * (NavitGraphics.mCanvasHeight / 2 + NavitGraphics.mCanvasHeight_overspill)));
				}
				else
				{
					traffic = NavitGraphics.CallbackGeoCalc(7, (int) (NavitGraphics.Global_dpi_factor * NavitGraphics.mCanvasWidth / 2), (int) (NavitGraphics.Global_dpi_factor * NavitGraphics.mCanvasHeight / 2));
				}

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
				NavitGraphics.callback_handler.sendMessage(msg);

				// draw map no-async
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 64);
				msg.setData(b);
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);

				// draw map no-async
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 64);
				msg.setData(b);
				NavitGraphics.callback_handler.sendMessage(msg);
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

				// draw map no-async
				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 64);
				msg.setData(b);
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
			break;
		case 24:
			// show feedback form
			Intent i4 = new Intent(this, NavitFeedbackFormActivity.class);
			this.startActivityForResult(i4, Navit.NavitSendFeedback_id);
			break;
		case 25:
			// share the current destination with your friends			
			String current_target_string2 = NavitGraphics.CallbackGeoCalc(4, 1, 1);
			if (current_target_string2.equals("x:x"))
			{
				Log.e("Navit", "no target set!");
			}
			else
			{
				try
				{
					String tmp[] = current_target_string2.split(":", 2);

					if (Navit.OSD_route_001.arriving_time_valid)
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), Navit.OSD_route_001.arriving_time, true);
					}
					else
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), "", true);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e("Navit", "problem with target!");
				}
			}
			break;
		case 26:
			// donate
			Log.e("Navit", "start donate app");
			donate();
			break;
		case 27:
			// donate
			Log.e("Navit", "donate bitcoins");
			donate_bitcoins();
			break;
		case 28:
			// replay GPS file
			Intent intent771 = new Intent(getBaseContext(), FileDialog.class);
			File a1 = new File(Navit.NAVIT_DATA_DEBUG_DIR);
			try
			{
				// convert the "/../" in the path to normal absolut dir
				intent771.putExtra(FileDialog.START_PATH, a1.getCanonicalPath());
				//can user select directories or not
				intent771.putExtra(FileDialog.CAN_SELECT_DIR, false);
				// disable the "new" button
				intent771.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
				//alternatively you can set file filter
				intent771.putExtra(FileDialog.FORMAT_FILTER, new String[] { "txt", "yaml" });
				startActivityForResult(intent771, Navit.NavitReplayFileConvChooser_id);
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			break;

		case 88:
			// dummy entry, just to make "breaks" in the menu
			break;
		case 601:
			// DEBUG: activate demo vehicle and set position to position to screen center

			Navit.DemoVehicle = true;

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 101);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);

			final Thread demo_v_001 = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(1000); // wait 1 seconds before we start

						try
						{
							float lat = 0;
							float lon = 0;

							String lat_lon = "";
							if (Navit.GFX_OVERSPILL)
							{
								lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * (NG__map_main.view.getWidth() / 2 + NavitGraphics.mCanvasWidth_overspill), NavitGraphics.Global_dpi_factor * (NG__map_main.view.getHeight() / 2 + NavitGraphics.mCanvasHeight_overspill));
							}
							else
							{
								lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
							}
							String tmp[] = lat_lon.split(":", 2);
							//System.out.println("tmp=" + lat_lon);
							lat = Float.parseFloat(tmp[0]);
							lon = Float.parseFloat(tmp[1]);
							//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
							Location l = null;
							l = new Location("ZANavi Demo 001");
							l.setLatitude(lat);
							l.setLongitude(lon);
							l.setBearing(0.0f);
							l.setSpeed(0);
							l.setAccuracy(4.0f); // accuracy 4 meters
							// NavitVehicle.update_compass_heading(0.0f);
							NavitVehicle.set_mock_location__fast(l);
						}
						catch (Exception e)
						{
						}

						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 52);
						b.putString("s", "45"); // speed in km/h of Demo-Vehicle
						// b.putString("s", "20");

						msg.setData(b);
						NavitGraphics.callback_handler.sendMessage(msg);
					}
					catch (Exception e)
					{
					}
				}
			};
			demo_v_001.start();

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 51);

			if (Navit.GFX_OVERSPILL)
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * ((Navit.NG__map_main.view.getWidth() / 2) + NavitGraphics.mCanvasWidth_overspill)));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * ((Navit.NG__map_main.view.getHeight() / 2) + NavitGraphics.mCanvasHeight_overspill)));
			}
			else
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
			}
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);

			break;
		case 602:
			// DEBUG: toggle textview with spoken and translated string (to help with translation)
			try
			{
				if (NavitGraphics.NavitMsgTv2_.getVisibility() == View.VISIBLE)
				{
					NavitGraphics.NavitMsgTv2_.setVisibility(View.GONE);
					NavitGraphics.NavitMsgTv2_.setEnabled(false);
					NavitGraphics.NavitMsgTv2sc_.setVisibility(View.GONE);
					NavitGraphics.NavitMsgTv2sc_.setEnabled(false);
				}
				else
				{
					NavitGraphics.NavitMsgTv2sc_.setVisibility(View.VISIBLE);
					NavitGraphics.NavitMsgTv2sc_.setEnabled(true);
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
			// DEBUG: activate FAST driving demo vehicle and set position to screen center

			Navit.DemoVehicle = true;

			msg = new Message();

			b = new Bundle();
			b.putInt("Callback", 52);
			b.putString("s", "800"); // speed in ~km/h of Demo-Vehicle
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 51);
			if (Navit.GFX_OVERSPILL)
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * ((Navit.NG__map_main.view.getWidth() / 2) + NavitGraphics.mCanvasWidth_overspill)));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * ((Navit.NG__map_main.view.getHeight() / 2) + NavitGraphics.mCanvasHeight_overspill)));
			}
			else
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
			}
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);

			try
			{
				float lat = 0;
				float lon = 0;

				lat = 0;
				lon = 0;
				String lat_lon = "";
				if (Navit.GFX_OVERSPILL)
				{
					lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * (NG__map_main.view.getWidth() / 2 + NavitGraphics.mCanvasWidth_overspill), NavitGraphics.Global_dpi_factor * (NG__map_main.view.getHeight() / 2 + NavitGraphics.mCanvasHeight_overspill));
				}
				else
				{
					lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
				}

				String tmp[] = lat_lon.split(":", 2);
				//System.out.println("tmp=" + lat_lon);
				lat = Float.parseFloat(tmp[0]);
				lon = Float.parseFloat(tmp[1]);
				//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
				Location l = null;
				l = new Location("ZANavi Demo 001");
				l.setLatitude(lat);
				l.setLongitude(lon);
				l.setBearing(0.0f);
				l.setSpeed(0);
				l.setAccuracy(4.0f); // accuracy 4 meters
				// NavitVehicle.update_compass_heading(0.0f);
				NavitVehicle.set_mock_location__fast(l);
			}
			catch (Exception e)
			{
			}

			break;
		case 605:
			// DEBUG: toggle Routgraph on/off
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 71);
			Navit.Routgraph_enabled = 1 - Navit.Routgraph_enabled;
			b.putString("s", "" + Navit.Routgraph_enabled);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
			break;
		case 606:
			// DEBUG: spill contents of index file(s)
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 83);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
			break;
		case 607:
			export_map_points_to_sdcard();
			break;
		case 608:
			import_map_points_from_sdcard();
			break;
		case 609:
			// run yaml tests
			new Thread()
			{
				public void run()
				{
					try
					{
						ZANaviDebugReceiver.DR_run_all_yaml_tests();
					}
					catch (Exception e)
					{
					}
				}
			}.start();
			break;
		case 99:
			try
			{
				if (wl_navigating != null)
				{
					//if (wl_navigating.isHeld())
					//{
					wl_navigating.release();
					Log.e("Navit", "WakeLock Nav: release 1");
					//}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.e("Navit", "onActivityResult");
		switch (requestCode)
		{
		case Navit.NavitGPXConvChooser_id:
			try
			{
				Log.e("Navit", "onActivityResult 001");
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					String in_ = data.getStringExtra(FileDialog.RESULT_PATH);
					convert_gpx_file_real(in_);
				}
			}
			catch (Exception e77)
			{
				e77.printStackTrace();
			}
			break;

		case NavitReplayFileConvChooser_id:
			try
			{
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					final String in_ = data.getStringExtra(FileDialog.RESULT_PATH);
					final Thread replay_gpx_file_001 = new Thread()
					{
						@Override
						public void run()
						{
							try
							{
								Thread.sleep(2000); // wait 2 seconds before we start
								String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
								ZANaviDebugReceiver.DR_replay_gps_file(in_, date);
							}
							catch (Exception e)
							{
							}
						}
					};
					replay_gpx_file_001.start();
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
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					System.out.println("Global_Location_update_not_allowed = 1");
					Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

					// remove all sdcard maps
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putInt("Callback", 19);
					msg.setData(b);
					NavitGraphics.callback_handler.sendMessage(msg);

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
					}

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
					// also remove index file
					File del_map_name_file_idx = new File(del_map_name + ".idx");
					del_map_name_file_idx.delete();
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

					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
					}

					// add all sdcard maps
					msg = new Message();
					b = new Bundle();
					b.putInt("Callback", 20);
					msg.setData(b);
					NavitGraphics.callback_handler.sendMessage(msg);

					final Thread zoom_to_route_004 = new Thread()
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
										zoom_to_route();
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
					zoom_to_route_004.start();

					System.out.println("Global_Location_update_not_allowed = 0");
					Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!
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
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					try
					{
						// Log.d("Navit", "PRI id=" + Integer.parseInt(data.getStringExtra("selected_id")));
						// set map id to download
						Navit.download_map_id = NavitMapDownloader.OSM_MAP_NAME_ORIG_ID_LIST[Integer.parseInt(data.getStringExtra("selected_id"))];
						// show the map download progressbar, and download the map
						if (Navit.download_map_id > -1)
						{
							// --------- start a map download (highest level) ---------
							// --------- start a map download (highest level) ---------
							// --------- start a map download (highest level) ---------
							// showDialog(Navit.MAPDOWNLOAD_PRI_DIALOG); // old method in app

							// new method in service
							Message msg = progress_handler.obtainMessage();
							// Bundle b = new Bundle();
							msg.what = 22;
							progress_handler.sendMessage(msg);

							// show license for OSM maps
							//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
							Toast.makeText(getApplicationContext(), Navit.get_text("Map data (c) OpenStreetMap contributors, CC-BY-SA"), Toast.LENGTH_SHORT).show(); //TRANS
							// --------- start a map download (highest level) ---------
							// --------- start a map download (highest level) ---------
							// --------- start a map download (highest level) ---------
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
		case Navit.NavitDownloaderSecSelectMap_id: // unused!!! unused!!! unused!!! unused!!! unused!!!
			break;
		case ZANaviVoiceInput_id:
			if (resultCode == ActionBarActivity.RESULT_OK)
			{
				try
				{
					String addr = data.getStringExtra("address_string");
					double lat = data.getDoubleExtra("lat", 0);
					double lon = data.getDoubleExtra("lon", 0);
					String hn = "";

					// save last address entry string
					p.PREF_StreetSearchStrings = pushToArray(p.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
					saveArray(p.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

					Boolean partial_match = true;
					Navit.use_index_search = false;

					Navit_last_address_partial_match = partial_match;
					Navit_last_address_search_string = addr;
					Navit_last_address_hn_string = hn;

					Navit_last_address_full_file_search = false;

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

					if (addr.equals(""))
					{
						// empty search string entered
						Toast.makeText(getApplicationContext(), Navit.get_text("No search string"), Toast.LENGTH_LONG).show(); //TRANS
					}
					else
					{
						System.out.println("Global_Location_update_not_allowed = 1");
						Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

						// --> this still does the search // google_online_search_and_set_destination(addr);
						result_set_destination(lat, lon, addr);

						System.out.println("Global_Location_update_not_allowed = 0");
						Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!
					}

				}
				catch (Exception e)
				{

				}
			}
			break;
		case NavitAddressSearch_id_online:
		case NavitAddressSearch_id_offline:
			Log.e("Navit", "NavitAddressSearch_id_:001");
			try
			{
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					try
					{
						String addr = data.getStringExtra("address_string");
						String hn = "";
						try
						{
							// only from offline mask!
							hn = data.getStringExtra("hn_string");
						}
						catch (Exception e)
						{
							hn = "";
						}

						// save last address entry string
						p.PREF_StreetSearchStrings = pushToArray(p.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
						saveArray(p.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

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
						NavitGraphics.callback_handler.sendMessage(msg2);

						if (requestCode == NavitAddressSearch_id_offline)
						{
							search_hide_duplicates = false;
							try
							{
								Boolean hide_dup = data.getStringExtra("hide_dup").equals("1");
								if (hide_dup)
								{
									search_hide_duplicates = true;
									Message msg = new Message();
									Bundle b = new Bundle();
									b.putInt("Callback", 45);
									msg.setData(b);
									NavitGraphics.callback_handler.sendMessage(msg);
								}
							}
							catch (Exception e)
							{
							}
							Navit.use_index_search = Navit.allow_use_index_search();
						}
						else
						{
							Navit.use_index_search = false;
						}

						Navit_last_address_partial_match = partial_match;
						Navit_last_address_search_string = addr;
						Navit_last_address_hn_string = hn;

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
							p.PREF_search_country = Navit_last_address_search_country_id;
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
						Navit.search_results_poi = 0;

						if (addr.equals(""))
						{
							// empty search string entered
							Toast.makeText(getApplicationContext(), Navit.get_text("No search string entered"), Toast.LENGTH_LONG).show(); //TRANS
						}
						else
						{
							System.out.println("Global_Location_update_not_allowed = 1");
							Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

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
					Log.e("Navit", "NavitAddressSearch_id_:900");
				}
			}
			catch (Exception e)
			{
				Log.d("Navit", "error on onActivityResult");
				e.printStackTrace();
			}
			Log.e("Navit", "NavitAddressSearch_id_:999");
			break;
		case Navit.NavitAddressResultList_id:
			try
			{
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					try
					{
						if (data.getStringExtra("what").equals("view"))
						{
							// get the coords for the destination
							int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

							// save last address entry string
							String addr = data.getStringExtra("address_string");
							p.PREF_StreetSearchStrings = pushToArray(p.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
							saveArray(p.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

							try
							{
								Navit.follow_button_off();
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}

							System.out.println("XSOM:009");

							if (Navit.use_index_search)
							{
								show_geo_on_screen_with_zoom_and_delay((float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
							}
							else
							{
								show_geo_on_screen_with_zoom_and_delay(Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
							}
						}
						else if (data.getStringExtra("what").equals("set"))
						{
							Log.d("Navit", "adress result list id=" + Integer.parseInt(data.getStringExtra("selected_id")));

							// save last address entry string
							String addr = data.getStringExtra("address_string");
							p.PREF_StreetSearchStrings = pushToArray(p.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
							saveArray(p.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

							// get the coords for the destination
							int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

							String _addr = "";
							double _lat = 0;
							double _lon = 0;

							// (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon)
							// (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat)

							// ok now set target
							try
							{
								if (Navit.use_index_search)
								{
									_addr = Navit.NavitAddressResultList_foundItems.get(destination_id).addr;
									_lat = Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat);
									_lon = Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
									//Navit.remember_destination(Navit.NavitAddressResultList_foundItems.get(destination_id).addr, (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
								}
								else
								{
									_addr = Navit.NavitAddressResultList_foundItems.get(destination_id).addr;
									_lat = Navit.NavitAddressResultList_foundItems.get(destination_id).lat;
									_lon = Navit.NavitAddressResultList_foundItems.get(destination_id).lon;
									//Navit.remember_destination(Navit.NavitAddressResultList_foundItems.get(destination_id).addr, Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
								}
								// save points
								//write_map_points();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

							route_wrapper(_addr, 0, 0, false, _lat, _lon, true);

							final Thread zoom_to_route_005 = new Thread()
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
												zoom_to_route();
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
							zoom_to_route_005.start();
							// zoom_to_route();

							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------
							if (p.PREF_enable_debug_write_gpx)
							{
								write_route_to_gpx_file();
							}
							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------

							try
							{
								Navit.follow_button_on();
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}

							//							if (Navit.use_index_search)
							//							{
							//								show_geo_on_screen((float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
							//							}
							//							else
							//							{
							//								show_geo_on_screen(Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
							//							}
						}
						else
						{
							// -- nothing --
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
				if (resultCode == ActionBarActivity.RESULT_OK)
				{

				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case NavitAddressSearch_id_sharedest:
			try
			{
				if (resultCode == ActionBarActivity.RESULT_OK)
				{

				}
				Log.d("Navit", "sharedest: finished");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case NavitGeoCoordEnter_id:
			try
			{
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					// lat lon enter activitiy result

					try
					{
						if (data.getStringExtra("what").equals("view"))
						{
							// get the coords for the destination
							float lat = Float.parseFloat(data.getStringExtra("lat"));
							float lon = Float.parseFloat(data.getStringExtra("lon"));

							// Log.d("Navit", "coord picker: " + lat);
							// Log.d("Navit", "coord picker: " + lon);

							// set nice zoomlevel before we show destination
							//							int zoom_want = Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL;
							//							//
							//							Message msg = new Message();
							//							Bundle b = new Bundle();
							//							b.putInt("Callback", 33);
							//							b.putString("s", Integer.toString(zoom_want));
							//							msg.setData(b);
							//							try
							//							{
							//								N_NavitGraphics.callback_handler.sendMessage(msg);
							//								Navit.GlobalScaleLevel = zoom_want;
							//							}
							//							catch (Exception e)
							//							{
							//								e.printStackTrace();
							//							}
							//							if (PREF_save_zoomlevel)
							//							{
							//								setPrefs_zoomlevel();
							//							}
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
								//								Navit.remember_destination(dest_name, lat, lon);
								//								// save points
								//								write_map_points();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

							//							// DEBUG: clear route rectangle list
							//							NavitGraphics.route_rects.clear();
							//
							//							if (NavitGraphics.navit_route_status == 0)
							//							{
							//								Navit.destination_set();
							//
							//								Message msg = new Message();
							//								Bundle b = new Bundle();
							//								b.putInt("Callback", 3);
							//								b.putString("lat", String.valueOf(lat));
							//								b.putString("lon", String.valueOf(lon));
							//								b.putString("q", dest_name);
							//								msg.setData(b);
							//								NavitGraphics.callback_handler.sendMessage(msg);
							//							}
							//							else
							//							{
							//								Message msg = new Message();
							//								Bundle b = new Bundle();
							//								b.putInt("Callback", 48);
							//								b.putString("lat", String.valueOf(lat));
							//								b.putString("lon", String.valueOf(lon));
							//								b.putString("q", dest_name);
							//								msg.setData(b);
							//								NavitGraphics.callback_handler.sendMessage(msg);
							//							}
							//

							route_wrapper(dest_name, 0, 0, false, lat, lon, true);

							final Thread zoom_to_route_006 = new Thread()
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
												zoom_to_route();
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
							zoom_to_route_006.start();
							// zoom_to_route();

							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------
							if (p.PREF_enable_debug_write_gpx)
							{
								write_route_to_gpx_file();
							}
							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------

							try
							{
								Navit.follow_button_on();
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}

							// show_geo_on_screen(lat, lon);
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
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					Log.d("Navit", "recent dest id=" + Integer.parseInt(data.getStringExtra("selected_id")));
					// get the coords for the destination
					int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

					if (data.getStringExtra("what").equals("view"))
					{
						try
						{
							Navit.follow_button_off();
						}
						catch (Exception e2)
						{
							e2.printStackTrace();
						}

						float lat = Navit.map_points.get(destination_id).lat;
						float lon = Navit.map_points.get(destination_id).lon;
						show_geo_on_screen_with_zoom_and_delay(lat, lon, 150);
					}
					else
					{
						// ok now set target
						String dest_name = Navit.map_points.get(destination_id).point_name;
						float lat = Navit.map_points.get(destination_id).lat;
						float lon = Navit.map_points.get(destination_id).lon;

						// System.out.println("XXXXXX:" + lat + " " + lon);

						route_wrapper(dest_name, 0, 0, false, lat, lon, true);

						final Thread zoom_to_route_007 = new Thread()
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
											zoom_to_route();
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
						zoom_to_route_007.start();

						// ---------- DEBUG: write route to file ----------
						// ---------- DEBUG: write route to file ----------
						// ---------- DEBUG: write route to file ----------
						if (p.PREF_enable_debug_write_gpx)
						{
							write_route_to_gpx_file();
						}
						// ---------- DEBUG: write route to file ----------
						// ---------- DEBUG: write route to file ----------

						try
						{
							Navit.follow_button_on();
						}
						catch (Exception e2)
						{
							e2.printStackTrace();
						}

					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;

		case NavitSendFeedback_id:
			try
			{
				if (resultCode == ActionBarActivity.RESULT_OK)
				{
					String feedback_text = data.getStringExtra("feedback_text");

					String subject_d_version = "";
					if (Navit_DonateVersion_Installed)
					{
						subject_d_version = subject_d_version + "D,";
					}

					if (Navit_Largemap_DonateVersion_Installed)
					{
						subject_d_version = subject_d_version + "L,";
					}

					try
					{
						int rl = get_reglevel();

						if (rl > 0)
						{
							subject_d_version = "U" + rl + ",";
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					String FD_addon = "";
					if (FDBL)
					{
						FD_addon = ",FD";
					}

					sendEmail("feedback@zanavi.cc", "ZANavi Feedback (v:" + subject_d_version + FD_addon + NavitAppVersion + " a:" + android.os.Build.VERSION.SDK + ")", feedback_text);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), Navit.get_text("there was a problem with sending feedback"), Toast.LENGTH_SHORT).show(); //TRANS
			}
			break;

		default:
			Log.e("Navit", "onActivityResult " + requestCode + " " + resultCode);
			try
			{
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ActivityResults[requestCode].onActivityResult(requestCode, resultCode, data);
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
				// ---------- what is this doing ????? ----------
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		}
		Log.e("Navit", "onActivityResult finished");
	}

	public class SCCB_object
	{
		int w;
		int h;
		Bitmap mb;
	}

	public class CMC_object
	{
		int i;
		String s;
	}

	public class MCB_object
	{
		int x1;
		int y1;
		int x2;

		int y2;
	}

	public class TCB_object
	{
		int del;
		int id;
		NavitTimeout nt;
	}

	public class GeCB_Object
	{
		int type;
		int a;
		float b;
		float c;
	}

	public class CWorkerThread extends Thread
	{
		private Boolean running;
		Boolean startmain = false;
		private CMC_object l2;
		private Integer l3;
		private MCB_object l4;
		private TCB_object l5;
		private SCCB_object l6;
		private Location l7;
		private GeCB_Object l8;

		Navit x;
		String lang;
		int version;
		String display_density_string;
		int timeout_loop_counter = 0;
		String n_datadir;
		String n_sharedir;

		private final LinkedBlockingQueue<CMC_object> queue = new LinkedBlockingQueue<CMC_object>();
		private final LinkedBlockingQueue<Integer> queue2 = new LinkedBlockingQueue<Integer>();
		private final LinkedBlockingQueue<MCB_object> queue3 = new LinkedBlockingQueue<MCB_object>();
		private final LinkedBlockingQueue<TCB_object> queue4 = new LinkedBlockingQueue<TCB_object>();
		private final LinkedBlockingQueue<SCCB_object> queue5 = new LinkedBlockingQueue<SCCB_object>();
		private final LinkedBlockingQueue<Location> queue6 = new LinkedBlockingQueue<Location>();
		private final LinkedBlockingQueue<GeCB_Object> queue7 = new LinkedBlockingQueue<GeCB_Object>();

		CWorkerThread()
		{
			this.running = true;
		}

		public void SizeChangedCallback(int w, int h, Bitmap main_map_bitmap)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			SCCB_object sccbo = new SCCB_object();
			sccbo.w = w;
			sccbo.h = h;
			sccbo.mb = main_map_bitmap;
			queue5.offer(sccbo);
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void TimeoutCallback2(NavitTimeout nt, int del, int id)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			TCB_object tcbo = new TCB_object();
			tcbo.del = del;
			tcbo.id = id;
			tcbo.nt = nt;
			queue4.offer(tcbo);
			this.interrupt();
			//timeout_loop_counter++;

			//if (timeout_loop_counter > 100)
			//{
			//	timeout_loop_counter = 0;
			//	// run GC at every 100th loop
			//	// System.gc();
			//}
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void CallbackMessageChannel(int i, String s)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0, "id=" + i);
			CMC_object cmco = new CMC_object();
			cmco.i = i;
			cmco.s = s;
			queue.offer(cmco);
			this.interrupt();
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void MotionCallback(int x1, int y1, int x2, int y2)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			//System.out.println("MotionCallback:enter queue=" + queue3.size());
			MCB_object mcbo = new MCB_object();
			mcbo.x1 = x1;
			mcbo.y1 = y1;
			mcbo.x2 = x2;
			mcbo.y2 = y2;
			queue3.offer(mcbo);
			this.interrupt();
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void NavitActivity2(int i)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			queue2.offer(Integer.valueOf(i));
			this.interrupt();
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void CallbackGeoCalc2(int type, int a, float b, float c)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			GeCB_Object gcco = new GeCB_Object();
			gcco.type = type;
			gcco.a = a;
			gcco.b = b;
			gcco.c = c;
			queue7.offer(gcco);
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void StartMain(Navit x, String lang, int version, String display_density_string, String n_datadir, String n_sharedir)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			//System.out.println("CWorkerThread:StartMain:JTHREAD ID=" + this.getId());
			//System.out.println("CWorkerThread:StartMain:THREAD ID=" + NavitGraphics.GetThreadId());

			this.startmain = true;
			this.x = x;
			this.lang = lang;
			this.version = version;
			this.n_datadir = n_datadir;
			this.n_sharedir = n_sharedir;
			this.display_density_string = display_density_string;
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void VehicleCallback3(Location location)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			boolean your_are_speeding_old = Navit.your_are_speeding;

			if ((Navit.cur_max_speed != -1) && (Navit.isGPSFix))
			{
				if ((location.getSpeed() * 3.6f) > ((float) Navit.cur_max_speed * (((float) p.PREF_roadspeed_warning_margin + 100.0f) / 100.0f)))
				{
					Navit.your_are_speeding = true;

					try
					{

						if (!toneG_heared)
						{
							// make "beep" sound to indicate we are going to fast!!
							if (toneG != null)
							{
								if (p.PREF_roadspeed_warning)
								{
									toneG.stopTone();
									toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
								}
								toneG_heared = true;
							}

						}
					}
					catch (Exception e)
					{
					}
				}
				else
				{
					// reset "beep" flag
					Navit.toneG_heared = false;
					Navit.your_are_speeding = false;
				}
			}
			else
			{
				Navit.your_are_speeding = false;
			}

			if (your_are_speeding_old != Navit.your_are_speeding)
			{
				//System.out.println("xx paint 6 xx");
				// NavitOSDJava.draw_real_wrapper(false, true);
				ZANaviLinearLayout.redraw_OSD(8);
			}

			if (queue6.size() > 5)
			{
				while (queue6.size() > 5)
				{
					try
					{
						// if too many gps updates are waiting, then only process the last few entry!!
						queue6.poll();
					}
					catch (Exception e)
					{
					}
				}
			}

			queue6.offer(location);
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void calc_sun_stats()
		{
			//
			//
			// SUN ----------------
			//
			//
			sun_moon__must_calc_new = (SystemClock.elapsedRealtime() - sun_moon__mLastCalcSunMillis) > (60000 * 3); // calc new every 3 minutes

			if ((sun_moon__must_calc_new) || (azmiuth_cache == -1))
			{
				float lat = 0;
				float lon = 0;
				try
				{
					// String lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
					String lat_lon = "";
					if (Navit.GFX_OVERSPILL)
					{
						lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * (NG__map_main.view.getWidth() / 2 + NavitGraphics.mCanvasWidth_overspill), NavitGraphics.Global_dpi_factor * (NG__map_main.view.getHeight() / 2 + NavitGraphics.mCanvasHeight_overspill));
					}
					else
					{
						lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
					}

					String tmp[] = lat_lon.split(":", 2);
					//System.out.println("tmp=" + lat_lon);
					lat = Float.parseFloat(tmp[0]);
					lon = Float.parseFloat(tmp[1]);
					//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
				}
				catch (Exception e)
				{
				}

				try
				{
					sun_moon__mLastCalcSunMillis = SystemClock.elapsedRealtime();
					TimeZone t = TimeZone.getDefault(); // Since the user's time zone changes dynamically, avoid caching this value. Instead, use this method to look it up for each use. 
					//System.out.println(t.getID());
					sun_moon__calc = new SunriseSunsetCalculator(new Location2(String.valueOf(lat), String.valueOf(lon)), t.getID());
					sun_moon__cx = Calendar.getInstance();
					sun_moon__sc = SolarPosition.getSunPosition(new Date(), lat, lon);

					azmiuth_cache = sun_moon__sc.azimuth;
					zenith_cache = sun_moon__sc.zenithAngle;
					sunrise_cache = sun_moon__calc.getOfficialSunriseForDate(sun_moon__cx);
					sunset_cache = sun_moon__calc.getOfficialSunsetForDate(sun_moon__cx);
					//System.out.println("calc moon");
					SolarEventCalculator.moonCoor_ret moon_stats = sun_moon__calc.computeMoon(sun_moon__cx);
					moon_azimuth_cache = moon_stats.az;
					moon_evelation_cache = moon_stats.alt;
				}
				catch (Exception e)
				{
				}
			}
			//
			elevation = 90 - zenith_cache;
			//
			// day          -> +90.0  to - 0.83
			// evening dusk -> - 0.83 to -10.00
			if (elevation < -0.83)
			{
				is_night = true;
				if (elevation < -10.00)
				{
					is_twilight = false;
				}
				else
				{
					is_twilight = true;
				}
				//System.out.println("***NIGHT***");
			}
			else
			{
				is_night = false;
				//System.out.println("###DAY###");
			}
			//
			// SUN ----------------
			//
			//
		}

		public void do_sun_calc()
		{
			//
			//
			// SUN ----------------
			//
			//
			try
			{
				this.calc_sun_stats();
			}
			catch (Exception e)
			{
				// on some systems BigInteger seems to crash, or maybe some values are out of range
				// until the bug is found, night modus is deactivated
				calc_sun_enabled = false;
				is_twilight = false;
				is_night = false;
			}
			//System.out.println("sunrise: " + sunrise_cache);
			//System.out.println("sunset: " + sunset_cache);
			//System.out.println("azimuth: " + roundTwoDecimals(azmiuth_cache));
			//System.out.println("elevation: " + elevation);
			//
			//
			// SUN ----------------
			//
			//
		}

		public void run()
		{
			//System.out.println("CWorkerThread -- started --");
			while (this.running)
			{
				if ((queue4.size() == 0) && (queue6.size() == 0))
				{
					try
					{
						Thread.sleep(2000); // 2 secs.
					}
					catch (InterruptedException e)
					{
					}
				}

				if (this.startmain)
				{
					//System.out.println("CWorkerThread:startup_calls:JTHREAD ID=" + this.getId());
					//System.out.println("CWorkerThread:startup_calls:THREAD ID=" + NavitGraphics.GetThreadId());

					this.startmain = false;
					System.out.println("CWorkerThread -- NavitMain --");
					NavitMain(x, lang, version, display_density_string, n_datadir, n_sharedir, NavitGraphics.draw_bitmap_s);
					System.out.println("CWorkerThread -- NavitActivity(3) --");
					NavitActivity(3);

					// -- set map detail level (after app startup) --
					// -- set map detail level (after app startup) --
					// -- set map detail level (after app startup) --
					try
					{
						getPrefs_more_map_detail();
						if (p.PREF_more_map_detail > 0)
						{
							Message msg2 = new Message();
							Bundle b2 = new Bundle();
							b2.putInt("Callback", 78);
							b2.putString("s", "" + p.PREF_more_map_detail);
							msg2.setData(b2);
							NavitGraphics.callback_handler.sendMessage(msg2);
						}
					}
					catch (Exception e)
					{
						// e.printStackTrace();
					}
					// -- set map detail level (after app startup) --
					// -- set map detail level (after app startup) --
					// -- set map detail level (after app startup) --

					// -- set map DPI factor (after app startup) --
					// -- set map DPI factor (after app startup) --
					// -- set map DPI factor (after app startup) --
					try
					{
						if ((Navit.metrics.densityDpi >= 320) && (!p.PREF_shrink_on_high_dpi))
						{
							double factor;
							factor = (double) Navit.metrics.densityDpi / (double) NavitGraphics.Global_Scaled_DPI_normal;

							Message msg2 = new Message();
							Bundle b2 = new Bundle();
							b2.putInt("Callback", 81);
							b2.putString("s", "" + factor);
							msg2.setData(b2);
							NavitGraphics.callback_handler.sendMessage(msg2);
						}
					}
					catch (Exception e)
					{
						// e.printStackTrace();
					}
					// -- set map DPI factor (after app startup) --
					// -- set map DPI factor (after app startup) --
					// -- set map DPI factor (after app startup) --
					Global_Init_Finished = 1;
					//x.runOnUiThread(new Runnable()
					//{
					//	public void run()
					//	{
					//		NavitActivity(3);
					//	}
					//});

					//**getPrefs();
					//**activatePrefs();

					System.out.println("CWorkerThread -- calling:ready --");
				}

				while (queue6.size() > 0)
				{
					try
					{
						// blocking call
						// l2 = queue6.take();
						// non-blocking call
						l7 = queue6.poll();
						if (l2 != null)
						{
							NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
						}
					}

					catch (Exception e)
					{
					}

				}

				while (queue.size() > 0)
				{
					try
					{
						// blocking call
						// l2 = queue.take();
						// non-blocking call
						l2 = queue.poll();
						if (l2 != null)
						{
							//System.out.println("CWorkerThread:CallbackMessageChannelReal_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:CallbackMessageChannelReal_call:THREAD ID=" + NavitGraphics.GetThreadId());
							//System.out.println("CWorkerThread:CallbackMessageChannelReal:" + l2.i);
							NavitGraphics.CallbackMessageChannelReal(l2.i, l2.s);
							//System.out.println("CWorkerThread:CallbackMessageChannelReal:finished");
						}
					}
					catch (Exception e)
					{
					}

					// if GPS updates are pending, process them
					if (queue6.size() > 0)
					{
						try
						{
							// blocking call
							// l2 = queue6.take();
							// non-blocking call
							l7 = queue6.poll();
							if (l2 != null)
							{
								NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
							}
						}
						catch (Exception e)
						{
						}
					}

				}

				while (queue5.size() > 0)
				{
					try
					{
						// blocking call
						// l6 = queue5.take();
						// non-blocking call
						l6 = queue5.poll();
						if (l6 != null)
						{
							//System.out.println("CWorkerThread:SizeChangedCallbackReal_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:SizeChangedCallbackReal_call:THREAD ID=" + NavitGraphics.GetThreadId());
							NavitGraphics.SizeChangedCallbackReal(l6.w, l6.h, l6.mb);
						}
					}
					catch (Exception e)
					{
					}

				}

				int count_timeout_callbacks = 0;
				while (count_timeout_callbacks < 10 && queue4.size() > 0)
				{
					count_timeout_callbacks++;
					try
					{
						// blocking call
						// l5 = queue4.take();
						// non-blocking call
						l5 = queue4.poll();
						if (l5 != null)
						{
							//System.out.println("CWorkerThread:TimeoutCallback_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:TimeoutCallback_call:THREAD ID=" + NavitGraphics.GetThreadId());
							if ((l5.nt.running) || (!l5.nt.event_multi))
							{
								NavitGraphics.TimeoutCallback(l5.del, l5.id);
							}
							else
							{
								//	System.out.println("CWorkerThread:TimeoutCallback_call:running=false! cid=" + l5.id);
							}
						}
					}
					catch (Exception e)
					{
					}

					// if GPS updates are pending, process them
					if (queue6.size() > 0)
					{
						try
						{
							// blocking call
							// l2 = queue6.take();
							// non-blocking call
							l7 = queue6.poll();
							if (l2 != null)
							{
								NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
							}
						}
						catch (Exception e)
						{
						}
					}

				}

				while (queue3.size() > 0)
				{
					try
					{
						// blocking call
						// l4 = queue3.take();
						// non-blocking call
						l4 = queue3.poll();
						if (l4 != null)
						{
							//System.out.println("CWorkerThread:MotionCallbackReal_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:MotionCallbackReal_call:THREAD ID=" + NavitGraphics.GetThreadId());
							if (queue3.size() > 0)
							{
								// if more moves are queued up, disable map drawing!
								NavitGraphics.MotionCallbackReal(l4.x1, l4.y1, l4.x2, l4.y2, 0);
							}
							else
							{
								// ok, also draw the map
								NavitGraphics.MotionCallbackReal(l4.x1, l4.y1, l4.x2, l4.y2, 1);
							}
						}
					}
					catch (Exception e)
					{
					}

				}

				while (queue7.size() > 0)
				{
					try
					{

						// if GPS updates are pending, process them
						if (queue6.size() > 0)
						{
							try
							{
								// blocking call
								// l2 = queue6.take();
								// non-blocking call
								l7 = queue6.poll();
								if (l2 != null)
								{
									NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
								}
							}
							catch (Exception e)
							{
							}
						}

						l8 = queue7.poll();
						if (l8 != null)
						{
							if (l8.type == 1)
							{
								Navit.OSD_nextturn.nextturn_streetname_systematic = "";
								Navit.OSD_nextturn.nextturn_streetname = NavitGraphics.CallbackGeoCalc(8, l8.b, l8.c);
								if (p.PREF_item_dump)
								{
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
									Navit.debug_item_dump = NavitGraphics.CallbackGeoCalc(9, l8.b, l8.c);
									//System.out.println("xx paint 22 xx");
									NavitGraphics.NavitAOverlay_s.postInvalidate();
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
								}
								//System.out.println("OSD postinvalidate***");
								//System.out.println("xx paint 7 xx");
								// NavitOSDJava.draw_real_wrapper(true, false);
								ZANaviLinearLayout.redraw_OSD(1);
								//++ NavitGraphics.NavitAOSDJava_.postInvalidate();
							}
							else if (l8.type == 2)
							{
								NavitGraphics.CallbackGeoCalc(l8.a, l8.b, l8.c);
							}
							else if (l8.type == 13)
							{
								try
								{
									// get roadbook
									// String road_book_res = NavitGraphics.CallbackGeoCalc(13, l8.b, l8.c);
									String[] separated = NavitGraphics.GetRoadBookItems(12345);
									// System.out.println("ROADBOOK_RES=" + separated);

									// parse roadbook data ------------
									road_book_items.clear();
									ZANaviListViewAdapter.ListViewItem l = null;

									// String[] separated = road_book_res.split("\13");
									int jk = 0;
									int ResId = 0;
									for (jk = 0; jk < separated.length; jk++)
									{
										if (jk > 1)
										{
											// System.out.println("ROADBOOK_RES=" + jk + ":" + separated[jk]);
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
											}
											// 0 string:distance short form
											// 1 lat
											// 2 lon
											// 3 icon name
											// 4 text

											// System.out.println("RBI:008:img=" + values[3]);

											if (values[3].compareTo("nav_waypoint") == 0)
											{
												try
												{
													ResId = Navit.res_.getIdentifier("com.zoffcc.applications.zanavi:drawable/" + "nav_waypoint_bk_center", null, null);
												}
												catch (Exception e_res_id)
												{
													ResId = 0;
													e_res_id.printStackTrace();
												}
											}
											else if (values[3].compareTo("nav_destination") == 0)
											{
												try
												{
													ResId = Navit.res_.getIdentifier("com.zoffcc.applications.zanavi:drawable/" + "nav_destination_bk_center", null, null);
												}
												catch (Exception e_res_id)
												{
													ResId = 0;
													e_res_id.printStackTrace();
												}
											}
											else
											{

												try
												{
													ResId = Navit.res_.getIdentifier("com.zoffcc.applications.zanavi:drawable/" + values[3] + "_bk", null, null);
												}
												catch (Exception e_res_id)
												{
													ResId = 0;
													e_res_id.printStackTrace();
												}
											}

											try
											{
												// System.out.println("RBI:008+" + ResId);
												if (ResId != 0)
												{
													l = new ListViewItem(values[0], res_.getDrawable(ResId), "", values[4], Float.parseFloat(values[1]), Float.parseFloat(values[2]));
												}
												else
												{
													l = new ListViewItem(values[0], res_.getDrawable(R.drawable.mini_roundabout), "", values[4], Float.parseFloat(values[1]), Float.parseFloat(values[2]));
												}
												// System.out.println("RBI:008");
												road_book_items.add(l);
												// System.out.println("RBI:009");
											}
											catch (Exception ee)
											{
												// System.out.println("item=" + separated[jk] + " EEXX:" + ee.getMessage());
											}
										}
									}
									// System.out.println("RBI:010");

									try
									{
										Message msg = Navit_progress_h.obtainMessage();
										Bundle b = new Bundle();
										msg.what = 33;
										msg.setData(b);
										Navit_progress_h.sendMessage(msg);
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
									// System.out.println("RBI:011");
								}
								catch (Exception eerb)
								{
									eerb.printStackTrace();
								}
								// parse roadbook data ------------

							}
						}
					}
					catch (Exception e)
					{
					}

				}

				while (queue2.size() > 0)
				{
					try
					{
						// blocking call
						// l3 = queue2.take();
						// non-blocking call
						l3 = queue2.poll();
						if (l3 != null)
						{
							int i3 = l3.intValue();
							//System.out.println("CWorkerThread:NavitActivity_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:NavitActivity_call:THREAD ID=" + NavitGraphics.GetThreadId());
							//System.out.println("CWorkerThread:NavitActivity:" + i3);
							NavitActivity(i3);
						}
					}
					catch (Exception e)
					{
					}
				}

				// check sun position (and after interval, recalc values)
				do_sun_calc();
			}
			//System.out.println("CWorkerThread -- stopped --");
		}

		public void stop_me()
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			this.running = false;
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}
	}

	public class SmoothVehicle extends Thread
	{
		private Boolean running;

		SmoothVehicle()
		{
			this.running = true;
		}

		public void run()
		{
			while (this.running)
			{
				try
				{
					Thread.sleep(5000); // 5 secs.
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

	public class WatchMem extends Thread
	{
		private Boolean running;

		WatchMem()
		{
			this.running = true;
		}

		public void run()
		{
			//System.out.println("WatchMem -- started --");
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
			//System.out.println("WatchMem -- stopped --");
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

			System.out.println("Global_Location_update_not_allowed = 1");
			Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

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
				if ((Navit_last_address_hn_string != null) && (Navit_last_address_hn_string.equals("")))
				{
					Navit_last_address_hn_string = filter_bad_chars(Navit_last_address_hn_string).toLowerCase();
				}

				if (Navit_last_address_full_file_search)
				{
					// flags (18)		-> order level to search at
					// ================
					//   0#0   0		-> search full world
					// lat#lon radius	-> search only this area, around lat,lon
					// ================
					N_NavitGraphics.SearchResultList(3, partial_match_i, Navit_last_address_search_string, "", "", 18, Navit_last_address_search_country_iso2_string, "0#0", 0);
				}
				else
				{
					if (Navit.use_index_search)
					{
						// new method with index search
						// -----------------
						//Navit_last_address_search_string
						String street_ = "";
						String town_ = "";
						String hn_ = Navit_last_address_hn_string;

						int last_space = Navit_last_address_search_string.lastIndexOf(" ");
						if (last_space != -1)
						{
							street_ = Navit_last_address_search_string.substring(0, last_space);
							town_ = Navit_last_address_search_string.substring(last_space + 1);
							// System.out.println("XX" + street_ + "YY" + town_ + "ZZ");
						}
						else
						{
							street_ = Navit_last_address_search_string;
							town_ = "";
						}
						N_NavitGraphics.SearchResultList(2, partial_match_i, street_, town_, hn_, Navit_last_address_search_country_flags, Navit_last_address_search_country_iso2_string, "0#0", 0);

						// sort result list
						Collections.sort(Navit.NavitAddressResultList_foundItems);
					}
					else
					{
						// old method search
						// -----------------
						// flags --> 3: search all countries
						//           2: search <iso2 string> country
						//           1: search default country (what you have set as language in prefs)
						N_NavitGraphics.SearchResultList(29, partial_match_i, Navit_last_address_search_string, "", "", Navit_last_address_search_country_flags, Navit_last_address_search_country_iso2_string, "0#0", 0);

						// sort result list
						Collections.sort(Navit.NavitAddressResultList_foundItems);
					}
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
					List<Address> foundAdresses = Navit.Navit_Geocoder.getFromLocationName(addressInput, 3); //Search addresses
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
						else if (tmp_addr.result_type.equals("POI"))
						{
							Navit.search_results_poi++;
						}

						// make the dialog move its bar ...
						Bundle b2 = new Bundle();
						b2.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG);
						b2.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
						b2.putInt("cur", Navit.NavitAddressResultList_foundItems.size() % (Navit.ADDRESS_RESULTS_DIALOG_MAX + 1));
						b2.putString("title", Navit.get_text("loading search results")); //TRANS
						b2.putString("text", Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn + " " + Navit.get_text("POI") + ":" + Navit.search_results_poi);

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
				// no results found, show toast
				msg = mHandler.obtainMessage();
				b = new Bundle();
				msg.what = 2;
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

			System.out.println("Global_Location_update_not_allowed = 0");
			Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!

			Log.e("Navit", "SearchResultsThread ended");
		}
	}

	public static String filter_bad_chars(String in)
	{
		String out = in;
		out = out.replaceAll("\\n", " "); // newline -> space
		out = out.replaceAll("\\r", " "); // return -> space
		out = out.replaceAll("\\t", " "); // tab -> space
		out = out.trim();
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

	public static Handler callback_handler_55 = new Handler()
	{
		public void handleMessage(Message msg)
		{
			// handle 111111
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0, "" + msg.getData().getInt("Callback"));

			if (msg.getData().getInt("Callback") == 1)
			{
				// zoom in
				NavitGraphics.CallbackMessageChannel(1, "");
			}
			else if (msg.getData().getInt("Callback") == 2)
			{
				// zoom out
				NavitGraphics.CallbackMessageChannel(2, "");
			}
			else if (msg.getData().getInt("Callback") == 55599)
			{
				// calc route after adding points
				NavitGraphics.CallbackMessageChannel(55599, "");
			}
			else if (msg.getData().getInt("Callback") == 55503)
			{
				try
				{
					NavitVehicle.pos_recording_add(2, 0, 0, 0, 0, 0); // CLR
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// set routing target to lat,lon
				NavitGraphics.CallbackMessageChannel(55503, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 3)
			{
				try
				{
					NavitVehicle.pos_recording_add(2, 0, 0, 0, 0, 0); // CLR
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// set routing target to lat,lon
				NavitGraphics.CallbackMessageChannel(3, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 55548)
			{
				try
				{
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// append to routing, add waypoint at lat,lon
				NavitGraphics.CallbackMessageChannel(55548, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 48)
			{
				try
				{
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// append to routing, add waypoint at lat,lon
				NavitGraphics.CallbackMessageChannel(48, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 4)
			{
				// set routing target to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");

				NavitGraphics.CallbackMessageChannel(4, "" + x + "#" + y);
				try
				{
					Navit.follow_button_on();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
			else if (msg.getData().getInt("Callback") == 49)
			{
				// set routing target to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");

				NavitGraphics.CallbackMessageChannel(49, "" + x + "#" + y);
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
				// toggle layer on/off
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(5, s);
			}
			else if (msg.getData().getInt("Callback") == 7)
			{
				NavitGraphics.CallbackMessageChannel(7, "");
			}
			else if ((msg.getData().getInt("Callback") > 7) && (msg.getData().getInt("Callback") < 21))
			{
				NavitGraphics.CallbackMessageChannel(msg.getData().getInt("Callback"), "");
			}
			else if (msg.getData().getInt("Callback") == 21)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				// ??? // ButtonCallback(1, 1, x, y); // down
			}
			else if (msg.getData().getInt("Callback") == 22)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				// ??? // ButtonCallback(0, 1, x, y); // up
			}
			else if (msg.getData().getInt("Callback") == 23)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				int x2 = msg.getData().getInt("x2");
				int y2 = msg.getData().getInt("y2");
				NavitGraphics.MotionCallback(x, y, x2, y2);
			}
			else if (msg.getData().getInt("Callback") == 24)
			{
				try
				{
					NavitGraphics.NavitMsgTv_.setEnabled(true);
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
					NavitGraphics.NavitMsgTv_.setEnabled(false);
					NavitGraphics.NavitMsgTv_.setVisibility(View.GONE);
				}
				catch (Exception e)
				{

				}
			}
			else if (msg.getData().getInt("Callback") == 30)
			{
				// 2D
				// String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(30, "");
			}
			else if (msg.getData().getInt("Callback") == 31)
			{
				// 3D
				// String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(31, "");
			}
			else if (msg.getData().getInt("Callback") == 32)
			{
				// switch to specific 3D pitch
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(32, s);
			}
			else if (msg.getData().getInt("Callback") == 33)
			{
				// zoom to specific zoomlevel
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(33, s);
			}
			else if (msg.getData().getInt("Callback") == 34)
			{
				// announcer voice OFF
				NavitGraphics.CallbackMessageChannel(34, "");
			}
			else if (msg.getData().getInt("Callback") == 35)
			{
				// announcer voice ON
				NavitGraphics.CallbackMessageChannel(35, "");
			}
			else if (msg.getData().getInt("Callback") == 36)
			{
				// switch "Lock on road" ON
				NavitGraphics.CallbackMessageChannel(36, "");
			}
			else if (msg.getData().getInt("Callback") == 37)
			{
				// switch "Lock on road" OFF
				NavitGraphics.CallbackMessageChannel(37, "");
			}
			else if (msg.getData().getInt("Callback") == 38)
			{
				// switch "Northing" ON
				NavitGraphics.CallbackMessageChannel(38, "");
			}
			else if (msg.getData().getInt("Callback") == 39)
			{
				// switch "Northing" OFF
				NavitGraphics.CallbackMessageChannel(39, "");
			}
			else if (msg.getData().getInt("Callback") == 40)
			{
				// switch "Map follows Vehicle" ON
				NavitGraphics.CallbackMessageChannel(40, "");
			}
			else if (msg.getData().getInt("Callback") == 41)
			{
				// switch "Map follows Vehicle" OFF
				NavitGraphics.CallbackMessageChannel(41, "");
			}
			else if (msg.getData().getInt("Callback") == 42)
			{
				// routing mode "highways"
				NavitGraphics.CallbackMessageChannel(42, "");
			}
			else if (msg.getData().getInt("Callback") == 43)
			{
				// routing mode "normal roads"
				NavitGraphics.CallbackMessageChannel(43, "");
			}
			else if (msg.getData().getInt("Callback") == 44)
			{
				// show duplicates in search results
				NavitGraphics.CallbackMessageChannel(44, "");
			}
			else if (msg.getData().getInt("Callback") == 45)
			{
				// filter duplicates in search results
				NavitGraphics.CallbackMessageChannel(45, "");
			}
			else if (msg.getData().getInt("Callback") == 46)
			{
				// stop searching and show results found until now
				NavitGraphics.CallbackMessageChannel(46, "");
			}
			else if (msg.getData().getInt("Callback") == 47)
			{
				// change maps data dir
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(47, s);
			}
			else if (msg.getData().getInt("Callback") == 50)
			{
				// we request to stop drawing the map
				NavitGraphics.CallbackMessageChannel(50, "");
			}
			else if (msg.getData().getInt("Callback") == 51)
			{
				// set position to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				NavitGraphics.CallbackMessageChannel(51, "" + x + "#" + y);
			}
			else if (msg.getData().getInt("Callback") == 52)
			{
				// switch to demo vehicle
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(52, s);
			}
			else if (msg.getData().getInt("Callback") == 53)
			{
				// dont speak streetnames
				NavitGraphics.CallbackMessageChannel(53, "");
			}
			else if (msg.getData().getInt("Callback") == 54)
			{
				// speak streetnames
				NavitGraphics.CallbackMessageChannel(54, "");
			}
			else if (msg.getData().getInt("Callback") == 55)
			{
				// set cache size for (map-)files
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(55, s);
			}
			//			else if (msg.getData().getInt("Callback") == 56)
			//			{
			//				// draw polylines with/without circles at the end
			//				String s = msg.getData().getString("s");
			//				NavitGraphics.CallbackMessageChannel(56, s); // 0 -> draw circles, 1 -> DO NOT draw circles
			//			}
			else if (msg.getData().getInt("Callback") == 57)
			{
				// keep drawing streets as if at "order" level xxx
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(57, s);
			}
			else if (msg.getData().getInt("Callback") == 58)
			{
				// street search radius factor (multiplier)
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(58, s);
			}
			else if (msg.getData().getInt("Callback") == 59)
			{
				// enable layer "name"
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(59, s);
			}
			else if (msg.getData().getInt("Callback") == 60)
			{
				// disable layer "name"
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(60, s);
			}
			else if (msg.getData().getInt("Callback") == 61)
			{
				// zoom to specific zoomlevel at given point as center
				// pixel-x#pixel-y#zoom-level
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(61, s);
			}
			else if (msg.getData().getInt("Callback") == 62)
			{
				// disable map drawing
				NavitGraphics.CallbackMessageChannel(62, "");
			}
			else if (msg.getData().getInt("Callback") == 63)
			{
				// enable map drawing
				NavitGraphics.CallbackMessageChannel(63, "");
			}
			else if (msg.getData().getInt("Callback") == 64)
			{
				// draw map
				NavitGraphics.CallbackMessageChannel(64, "");
			}
			else if (msg.getData().getInt("Callback") == 65)
			{
				// draw map async
				NavitGraphics.CallbackMessageChannel(65, "");
			}
			else if (msg.getData().getInt("Callback") == 66)
			{
				// enable "multipolygons"
				NavitGraphics.CallbackMessageChannel(66, "");
			}
			else if (msg.getData().getInt("Callback") == 67)
			{
				// disable "multipolygons"
				NavitGraphics.CallbackMessageChannel(67, "");
			}
			else if (msg.getData().getInt("Callback") == 68)
			{
				// shift "order" by this value (only for drawing objects)
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(68, s);
			}
			else if (msg.getData().getInt("Callback") == 69)
			{
				// stop drawing map
				NavitGraphics.CallbackMessageChannel(69, "");
			}
			else if (msg.getData().getInt("Callback") == 70)
			{
				// allow drawing map
				NavitGraphics.CallbackMessageChannel(70, "");
			}
			else if (msg.getData().getInt("Callback") == 71)
			{
				// activate/deactivate "route graph" display
				// 0 -> deactivate
				// 1 -> activate
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(71, s);
			}
			else if (msg.getData().getInt("Callback") == 72)
			{
				// update the route path and route graph (e.g. after setting new roadblocks)
				// does not update destinations!!!
				NavitGraphics.CallbackMessageChannel(72, "");
			}
			else if (msg.getData().getInt("Callback") == 73)
			{
				// update the route path and route graph (e.g. after setting new roadblocks)
				// this destroys the route graph and calcs everything totally new!
				NavitGraphics.CallbackMessageChannel(73, "");
			}

			else if (msg.getData().getInt("Callback") == 74)
			{
				// allow demo vechile to move
				NavitGraphics.CallbackMessageChannel(74, "");
			}
			else if (msg.getData().getInt("Callback") == 75)
			{
				// stop demo vechile
				NavitGraphics.CallbackMessageChannel(75, "");
			}
			else if (msg.getData().getInt("Callback") == 76)
			{
				// show route rectangles
				NavitGraphics.CallbackMessageChannel(76, "");
			}
			else if (msg.getData().getInt("Callback") == 77)
			{
				// do not show route rectangles
				NavitGraphics.CallbackMessageChannel(77, "");
			}
			else if (msg.getData().getInt("Callback") == 78)
			{
				// shift layout "order" values
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(78, s);
			}
			else if (msg.getData().getInt("Callback") == 79)
			{
				// set traffic light delay/cost
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(79, s);
			}
			else if (msg.getData().getInt("Callback") == 80)
			{
				// set autozoom flag to 0 or 1
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(80, s);
			}
			else if (msg.getData().getInt("Callback") == 81)
			{
				// resize layout items by factor
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(81, s);
			}
			else if (msg.getData().getInt("Callback") == 82)
			{
				// report share dir
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(82, s);
			}
			else if (msg.getData().getInt("Callback") == 83)
			{
				// spill all the index files to log output
				NavitGraphics.CallbackMessageChannel(83, "");
			}
			else if (msg.getData().getInt("Callback") == 84)
			{
				// report data dir
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(84, s);
			}
			else if (msg.getData().getInt("Callback") == 85)
			{
				// C linedrawing flag
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(85, s);
			}
			else if (msg.getData().getInt("Callback") == 86)
			{
				// avoid sharp turns flag to 0 or 1
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(86, s);
			}
			else if (msg.getData().getInt("Callback") == 87)
			{
				// // avoid sharp turns minimum angle. if turn is harder than this angle then set penalty
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(87, s);
			}
			else if (msg.getData().getInt("Callback") == 88)
			{
				// avoid sharp turns penalty
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(88, s);
			}
			else if (msg.getData().getInt("Callback") == 89)
			{
				// search radius for housenumbers for streets
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(89, s);
			}
			else if (msg.getData().getInt("Callback") == 90)
			{
				// set vehicleprofile to value of string s ('car','bike')
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(90, s);
			}
			else if (msg.getData().getInt("Callback") == 91)
			{
				// change vehicle profile's roadprofile values
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(91, s);
			}
			else if (msg.getData().getInt("Callback") == 92)
			{
				// change vehicle profile's roadprofile values 2
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(92, s);
			}
			else if (msg.getData().getInt("Callback") == 93)
			{
				// change vehicle profile's roadprofile values 3
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(93, s);
			}
			else if (msg.getData().getInt("Callback") == 94)
			{
				// change priority for cycle lanes
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(94, s);
			}
			//else if (msg.getData().getInt("Callback") == 95)
			//{
			//	// change priority for cycle tracks
			//	String s = msg.getData().getString("s");
			//	NavitGraphics.CallbackMessageChannel(95, s);
			//}
			else if (msg.getData().getInt("Callback") == 96)
			{
				// dump route to GPX file, "s" -> full pathname to output file
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(96, s);
			}
			else if (msg.getData().getInt("Callback") == 97)
			{
				// set positon to lat#lon#name
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				NavitGraphics.CallbackMessageChannel(97, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 98)
			{
				// set connected_pref value
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(98, s);
			}
			else if (msg.getData().getInt("Callback") == 99)
			{
				// set angle_pref value
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(99, s);
			}
			else if (msg.getData().getInt("Callback") == 100)
			{
				// dump callbacks to log
				NavitGraphics.CallbackMessageChannel(100, "");
			}
			else if (msg.getData().getInt("Callback") == 101)
			{
				// set demo vehicle flag for tracking
				NavitGraphics.CallbackMessageChannel(101, "");
			}
			else if (msg.getData().getInt("Callback") == 102)
			{
				// set gpsfix flag
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(102, s);
			}
			else if (msg.getData().getInt("Callback") == 103)
			{
				// draw location of self (car) x% lower than screen center
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(103, s);
			}
			else if (msg.getData().getInt("Callback") == 104)
			{
				// send OVERSPILL_FACTOR to C-code
				String s;
				if (Navit.GFX_OVERSPILL)
				{
					s = "" + (OVERSPILL_FACTOR * 100);
				}
				else
				{
					s = "" + 100;
				}

				NavitGraphics.CallbackMessageChannel(104, s);
			}
			else if (msg.getData().getInt("Callback") == 105)
			{
				// zoom to specific zoomlevel without redrawing the map!
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(105, s);
			}
			else if (msg.getData().getInt("Callback") == 106)
			{
				// factor for routing/road speed
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(106, s);
			}
			else if (msg.getData().getInt("Callback") == 107)
			{
				// level 0 announcement seconds
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(107, s);
			}
			else if (msg.getData().getInt("Callback") == 108)
			{
				// level 1 announcement seconds
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(108, s);
			}
			else if (msg.getData().getInt("Callback") == 109)
			{
				// level 2 announcement seconds
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(109, s);
			}
			else if (msg.getData().getInt("Callback") == 110)
			{
				// generic int option CallBack [<option name>:<option value "int">]
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(110, s);
			}
			else if (msg.getData().getInt("Callback") == 112)
			{
				// show maps debug view
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(112, s);
			}
			else if (msg.getData().getInt("Callback") == 111)
			{
				// show real gps position on map
				String s = msg.getData().getString("s");
				NavitGraphics.CallbackMessageChannel(111, s);
			}
			else if (msg.getData().getInt("Callback") == 9901)
			{
				// if follow mode is on, then dont show freeview streetname
				//if (!Navit.PREF_follow_gps)
				//{
				//	Navit.cwthr.CallbackGeoCalc2(1, 0, mCanvasWidth / 2, mCanvasHeight / 2);
				//}
			}
			else if (msg.getData().getInt("Callback") == 98001)
			{
				int id = msg.getData().getInt("id");
				int i = msg.getData().getInt("i");
				NavitGraphics.return_generic_int_real(id, i);
			}
			else if (msg.getData().getInt("Callback") == 9001)
			{
				NavitGraphics.busyspinner_.setVisibility(View.INVISIBLE);
				NavitGraphics.busyspinnertext_.setVisibility(View.INVISIBLE);
			}
			else if (msg.getData().getInt("Callback") == 9002)
			{
				NavitGraphics.busyspinner_.setVisibility(View.VISIBLE);
				NavitGraphics.busyspinnertext_.setVisibility(View.VISIBLE);
			}

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}
	};

	public Handler progress_handler = new Handler()
	{
		@SuppressLint("NewApi")
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

				try
				{
					dismissDialog(msg.getData().getInt("dialog_num"));
					removeDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
				}

				// exit_code=0 -> OK, map was downloaded fine
				if (msg.getData().getInt("exit_code") == 0)
				{
					// try to use the new downloaded map (works fine now!)
					//Log.d("Navit", "instance count=" + Navit.getInstanceCount()); // where did this go to?

					// **** onStop();
					// **** onCreate(getIntent().getExtras());

					String this_map_name = "map";
					try
					{
						this_map_name = msg.getData().getString("map_name");
					}
					catch (Exception e)
					{
					}

					// reload sdcard maps
					Message msg2 = new Message();
					Bundle b2 = new Bundle();
					b2.putInt("Callback", 18);
					msg2.setData(b2);
					NavitGraphics.callback_handler.sendMessage(msg2);

					// ----- service stop -----
					// ----- service stop -----
					Navit.getBaseContext_.stopService(Navit.ZANaviMapDownloaderServiceIntent);
					// ----- service stop -----
					// ----- service stop -----

					try
					{
						// show notification that map is ready
						String Notification_header = "ZANavi";
						String Notification_text = this_map_name + " ready";

						NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						Notification notification = new Notification(R.drawable.icon, "ZANavi download finished", System.currentTimeMillis());
						notification.flags = Notification.FLAG_AUTO_CANCEL;
						Intent in = new Intent();
						in.setClass(getBaseContext_, Navit.class);
						in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						PendingIntent p_activity = PendingIntent.getActivity(getBaseContext_, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
						notification.setLatestEventInfo(getBaseContext_, Notification_header, Notification_text, p_activity);

						try
						{
							nm.notify(ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2, notification);
						}
						catch (Exception e)
						{
							e.printStackTrace();

							try
							{
								p_activity = PendingIntent.getActivity(getBaseContext_, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);

								notification.setLatestEventInfo(getBaseContext_, Notification_header, Notification_text, p_activity);
								nm.notify(ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2, notification);
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

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
				else
				{
					// there was a problem downloading the map
					// ----- service stop -----
					// ----- service stop -----
					Navit.getBaseContext_.stopService(Navit.ZANaviMapDownloaderServiceIntent);
					// ----- service stop -----
					// ----- service stop -----

					String this_map_name = "map";
					try
					{
						this_map_name = msg.getData().getString("map_name");
					}
					catch (Exception e)
					{
					}

					try
					{

						// show notification that there was a download problem
						String Notification_header = "ZANavi";
						String Notification_text = "ERROR while downloading " + this_map_name;

						NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						Notification notification = new Notification(R.drawable.icon, "ZANavi download ERROR", System.currentTimeMillis());
						notification.flags = Notification.FLAG_AUTO_CANCEL;
						Intent in = new Intent();
						in.setClass(getBaseContext_, Navit.class);
						in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						PendingIntent p_activity = PendingIntent.getActivity(getBaseContext_, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
						notification.setLatestEventInfo(getBaseContext_, Notification_header, Notification_text, p_activity);

						try
						{
							nm.notify(ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2, notification);
							//							ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2++;
							//							if (ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2 > 2000)
							//							{
							//								ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2 = 1001;
							//							}

						}
						catch (Exception e)
						{
							e.printStackTrace();

							try
							{
								p_activity = PendingIntent.getActivity(getBaseContext_, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);

								notification.setLatestEventInfo(getBaseContext_, Notification_header, Notification_text, p_activity);
								nm.notify(ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2, notification);
								//								ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2++;
								//								if (ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2 > 2000)
								//								{
								//									ZANaviMapDownloaderService.NOTIFICATION_ID__DUMMY2 = 1001;
								//								}
							}
							catch (Exception e2)
							{
								e2.printStackTrace();
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				break;
			case 1:
				// change progressbar values
				try
				{
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
				}
				catch (Exception e)
				{
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
				try
				{
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
				}
				catch (Exception e)
				{
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
					//System.out.println("invalidate 027");
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
			//			case 18:
			//				try
			//				{
			//					openOptionsMenu_wrapper();
			//				}
			//				catch (Exception e)
			//				{
			//				}
			//				break;
			case 19:
				open_voice_recog_screen();
				break;
			case 20:
				dim_screen();
				break;
			case 21:
				default_brightness_screen();
				break;
			case 22:
				try
				{
					// ----- service start -----
					// ----- service start -----
					startService(Navit.ZANaviMapDownloaderServiceIntent);
					// ----- service start -----
					// ----- service start -----

					//					try
					//					{
					//						Thread.sleep(200);
					//					}
					//					catch (InterruptedException e)
					//					{
					//					}

					//					if (!ZANaviMapDownloaderService.service_running)
					//					{
					//						System.out.println("ZANaviMapDownloaderService -> not running yet ...");
					//						try
					//						{
					//							Thread.sleep(2000);
					//						}
					//						catch (InterruptedException e)
					//						{
					//						}
					//					}
					//
					//					if (!ZANaviMapDownloaderService.service_running)
					//					{
					//						System.out.println("ZANaviMapDownloaderService -> not running yet ...");
					//						try
					//						{
					//							Thread.sleep(2000);
					//						}
					//						catch (InterruptedException e)
					//						{
					//						}
					//					}

					// -------- // ZANaviMapDownloaderService.start_map_download();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 23:

				// show actionbar download icon
				try
				{
					// show download actionbar icon
					//cur_menu.findItem(R.id.item_download_menu_button).setTitle("");
					actionabar_download_icon_visible = true;
					cur_menu.findItem(R.id.item_download_menu_button).setVisible(true);
					//cur_menu.findItem(R.id.item_download_menu_button).setEnabled(true);
					// ****** // cur_menu.findItem(R.id.item_download_menu_button).setIcon(R.drawable.anim_download_icon);
					// cur_menu.findItem(R.id.item_download_menu_button).setIcon((Drawable) null);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					show_status_bar();
					getSupportActionBar().setDisplayShowTitleEnabled(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				//				try
				//				{
				//					View menuItemView = findViewById(R.id.item_download_menu_button);
				//					menuItemView.setBackgroundResource(R.drawable.anim_download_icon_2);
				//					//					menuItemView.setOnClickListener(new View.OnClickListener()
				//					//					{
				//					//						public void onClick(View v)
				//					//						{
				//					//							try
				//					//							{
				//					//								//menuItemView.setBackgroundResource(R.drawable.anim_download_icon_1);
				//					//								//AnimationDrawable frameAnimation = (AnimationDrawable) menuItemView.getBackground();
				//					//								//frameAnimation.start();
				//					//								// menuItemView.setAlpha(100);
				//					//								View menuItemView = findViewById(R.id.item_download_menu_button);
				//					//								menuItemView.setBackgroundResource(R.drawable.anim_download_icon_1);
				//					//
				//					//								System.out.println("download icon pressed(2)");
				//					//
				//					//								Intent mapdownload_cancel_activity = new Intent(Navit.getBaseContext_, ZANaviDownloadMapCancelActivity.class);
				//					//								mapdownload_cancel_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				//					//								startActivity(mapdownload_cancel_activity);
				//					//
				//					//								new Handler().postDelayed(new Runnable()
				//					//								{
				//					//									@Override
				//					//									public void run()
				//					//									{
				//					//										if (Navit.cur_menu.findItem(R.id.item_download_menu_button).isVisible())
				//					//										{
				//					//											View menuItemView = findViewById(R.id.item_download_menu_button);
				//					//											menuItemView.setBackgroundResource(R.drawable.anim_download_icon_2);
				//					//											AnimationDrawable frameAnimation = (AnimationDrawable) menuItemView.getBackground();
				//					//											frameAnimation.start();
				//					//										}
				//					//									}
				//					//								}, 50);
				//					//							}
				//					//							catch (Exception e)
				//					//							{
				//					//							}
				//					//						}
				//					//					});
				//					AnimationDrawable frameAnimation = (AnimationDrawable) menuItemView.getBackground();
				//					frameAnimation.start();
				//				}
				//				catch (Exception e)
				//				{
				//					e.printStackTrace();
				//				}

				break;
			case 24:
				// hide actionbar download icon

				//				try
				//				{
				//					View menuItemView = findViewById(R.id.item_download_menu_button);
				//					menuItemView.setBackground((Drawable) null);
				//
				//					new Handler().postDelayed(new Runnable()
				//					{
				//						@Override
				//						public void run()
				//						{
				//							if (Navit.cur_menu.findItem(R.id.item_download_menu_button).isVisible())
				//							{
				//								View menuItemView = findViewById(R.id.item_download_menu_button);
				//								menuItemView.setBackground((Drawable) null);
				//							}
				//						}
				//					}, 50);
				//				}
				//				catch (Exception e)
				//				{
				//					e.printStackTrace();
				//				}

				try
				{
					// hide download actionbar icon
					actionabar_download_icon_visible = false;
					cur_menu.findItem(R.id.item_download_menu_button).setVisible(false);
					//cur_menu.findItem(R.id.item_download_menu_button).setEnabled(false);
					// cur_menu.findItem(R.id.item_download_menu_button).setIcon((Drawable) null);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					getSupportActionBar().setDisplayShowTitleEnabled(false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					hide_status_bar();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				break;
			case 25:
				// Log.e("Navit", "nav: 25");
				NavitGraphics.deactivate_nav_wakelock_real();
				break;
			case 26:
				// Log.e("Navit", "nav: 26");
				NavitGraphics.activate_nav_wakelock_real();
				break;
			case 27:
				show_status_bar();
				break;
			case 28:
				hide_status_bar();
				break;
			case 29:
				invalidateOptionsMenu();
				break;
			case 30:
				try
				{
					NavitGraphics.NavitMsgTv2_.append(msg.getData().getString("text"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 31:
				try
				{
					// map download menu
					Intent map_download_list_activity = new Intent(getBaseContext_, NavitDownloadSelectMapActivity.class);
					startActivityForResult(map_download_list_activity, Navit.NavitDownloaderPriSelectMap_id);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 32:
				try
				{
					if (p.PREF_follow_gps)
					{
						push_pin_view.setImageResource(R.drawable.pin1_down);
					}
					else
					{
						push_pin_view.setImageResource(R.drawable.pin1_up);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 33:
				if (road_book != null)
				{
					if (road_book_items != null)
					{
						try
						{
							// send new roadbook items to fragment and reload it
							road_book.reload_items(road_book_items);
						}
						catch (Exception e)
						{
						}
					}
				}
				break;
			case 34:
				// show bubble
				NavitGraphics.whats_here_container_wrap.setVisibility(View.VISIBLE);
				NavitGraphics.whats_here_text.setText("  " + Navit.get_text("loading ...") + "  ");
				break;
			case 35:
				// hide bubble
				NavitGraphics.whats_here_container_wrap.setVisibility(View.INVISIBLE);
				NavitGraphics.whats_here_text.setText("  " + Navit.get_text("loading ...") + "  ");
				// and move out of screen

				if (!Navit.PAINT_OLD_API)
				{
					NavitGraphics.whats_here_container_wrap.setX(-2000);
					NavitGraphics.whats_here_container_wrap.setY(-2000);
					// NavitGraphics.whats_here_container_wrap.requestLayout();

					NavitGraphics.whats_here_pointer_image.setX(0);
				}
				else
				{
					NavitGraphics.params_whats_here_container_wrap = (android.widget.RelativeLayout.LayoutParams) NavitGraphics.whats_here_container_wrap.getLayoutParams();
					NavitGraphics.params_whats_here_container_wrap.leftMargin = -2000;
					NavitGraphics.params_whats_here_container_wrap.topMargin = -2000;
					NavitGraphics.whats_here_container_wrap.setLayoutParams(NavitGraphics.params_whats_here_container_wrap);
					NavitGraphics.whats_here_container_wrap.requestLayout();

					NavitGraphics.params_whats_here_pointer_image = (RelativeLayout.LayoutParams) NavitGraphics.whats_here_pointer_image.getLayoutParams();
					// NavitGraphics.params_whats_here_pointer_image.leftMargin = 0;
					NavitGraphics.whats_here_pointer_image.setLayoutParams(NavitGraphics.params_whats_here_pointer_image);
					NavitGraphics.whats_here_pointer_image.requestLayout();

				}
				break;
			case 36:
				// move bubble to x,y
				int width_in_px = getResources().getDimensionPixelSize(R.dimen.whats_here_container_width);
				int new_x = NavitGraphics.NavitAOverlay_s.bubble_001.x;
				int new_diff = 0;
				if ((NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) > NavitGraphics.mCanvasWidth)
				{
					new_diff = (NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) - NavitGraphics.mCanvasWidth;
					new_x = NavitGraphics.NavitAOverlay_s.bubble_001.x - ((NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) - NavitGraphics.mCanvasWidth);
				}

				new_x = new_x - NavitGraphics.dp_to_px(17);

				if (!Navit.PAINT_OLD_API)
				{
					NavitGraphics.whats_here_container_wrap.setX(new_x);
					NavitGraphics.whats_here_container_wrap.setY(NavitGraphics.NavitAOverlay_s.bubble_001.y + NavitGraphics.dp_to_px(17));
					// NavitGraphics.whats_here_container_wrap.requestLayout();

					NavitGraphics.whats_here_pointer_image.setX(new_diff);
				}
				else
				{
					NavitGraphics.params_whats_here_container_wrap = (android.widget.RelativeLayout.LayoutParams) NavitGraphics.whats_here_container_wrap.getLayoutParams();
					NavitGraphics.params_whats_here_container_wrap.leftMargin = new_x;
					NavitGraphics.params_whats_here_container_wrap.topMargin = NavitGraphics.NavitAOverlay_s.bubble_001.y - NavitGraphics.whats_here_container_wrap.getHeight() + NavitGraphics.dp_to_px(17);
					NavitGraphics.whats_here_container_wrap.setLayoutParams(NavitGraphics.params_whats_here_container_wrap);
					NavitGraphics.whats_here_container_wrap.requestLayout();

					NavitGraphics.params_whats_here_pointer_image = (RelativeLayout.LayoutParams) NavitGraphics.whats_here_pointer_image.getLayoutParams();
					// has some bugs // NavitGraphics.params_whats_here_pointer_image.leftMargin = (NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) - NavitGraphics.mCanvasWidth;
					NavitGraphics.whats_here_pointer_image.setLayoutParams(NavitGraphics.params_whats_here_pointer_image);
					NavitGraphics.whats_here_pointer_image.requestLayout();
					NavitGraphics.whats_here_pointer_image.setVisibility(View.INVISIBLE);
				}
				break;
			case 37:
				// set text for point on screen
				String dest_name = "Point on Screen";

				try
				{
					if (Navit.GFX_OVERSPILL)
					{
						dest_name = NavitGraphics.CallbackGeoCalc(8, (NavitGraphics.NavitAOverlay_s.bubble_001.x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (NavitGraphics.NavitAOverlay_s.bubble_001.y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
					}
					else
					{
						dest_name = NavitGraphics.CallbackGeoCalc(8, NavitGraphics.NavitAOverlay_s.bubble_001.x * NavitGraphics.Global_dpi_factor, NavitGraphics.NavitAOverlay_s.bubble_001.y * NavitGraphics.Global_dpi_factor);
					}

					if ((dest_name.equals(" ")) || (dest_name == null))
					{
						dest_name = "Point on Screen";
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				NavitGraphics.whats_here_text.setText(dest_name);
				break;
			case 38:
				try
				{
					NavitAddressSearchActivity.adapter.clear();
					NavitAddressSearchActivity.adapter.notifyDataSetChanged();
					// NavitAddressSearchActivity.listview.postInvalidate();
				}
				catch (Exception e)
				{
					System.out.println("AAEE:001");
				}
				break;
			case 39:
				try
				{
					NavitAddressSearchActivity.adapter.notifyDataSetChanged();
				}
				catch (Exception e)
				{
					System.out.println("AAEE:008");
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

	@TargetApi(Build.VERSION_CODES.FROYO)
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
						NavitGraphics.callback_handler.sendMessage(msg);
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
			mapdownloader_dialog_pri.setCancelable(false);
			mapdownloader_dialog_pri.setCanceledOnTouchOutside(false);
			mapdownloader_dialog_pri.setProgress(0);
			mapdownloader_dialog_pri.setMax(200);

			WindowManager.LayoutParams dialog_lparams = mapdownloader_dialog_pri.getWindow().getAttributes();
			dialog_lparams.screenBrightness = 0.1f;
			mapdownloader_dialog_pri.getWindow().setAttributes(dialog_lparams);

			DialogInterface.OnDismissListener mOnDismissListener1 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					android.view.WindowManager.LayoutParams dialog_lparams = mapdownloader_dialog_pri.getWindow().getAttributes();
					mapdownloader_dialog_pri.getWindow().setAttributes((WindowManager.LayoutParams) dialog_lparams);
					mapdownloader_dialog_pri.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
					mapdownloader_dialog_pri.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					Log.e("Navit", "onDismiss: mapdownloader_dialog pri");
					dialog.dismiss();
					dialog.cancel();
					progressThread_pri.stop_thread();
				}
			};

			try
			{
				mapdownloader_dialog_pri.setButton(AlertDialog.BUTTON_NEGATIVE, Navit.get_text("Cancel"), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						mapdownloader_dialog_pri.dismiss();
					}
				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			mapdownloader_dialog_pri.setOnDismissListener(mOnDismissListener1);
			mapdownloader_pri = new NavitMapDownloader(this);
			progressThread_pri = mapdownloader_pri.new ProgressThread(progress_handler, NavitMapDownloader.z_OSM_MAPS[Navit.download_map_id], MAP_NUM_PRIMARY);
			progressThread_pri.start();
			//
			// show license for OSM maps
			//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
			Toast.makeText(getApplicationContext(), Navit.get_text("Map data (c) OpenStreetMap contributors, CC-BY-SA"), Toast.LENGTH_SHORT).show(); //TRANS
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
			//
			// show license for OSM maps
			//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
			Toast.makeText(getApplicationContext(), Navit.get_text("Map data (c) OpenStreetMap contributors, CC-BY-SA"), Toast.LENGTH_SHORT).show(); //TRANS
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
		try
		{
			if (toneG != null)
			{
				toneG.stopTone();
				toneG.release();

			}
		}
		catch (Exception e)
		{
		}

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
			if (p.PREF_enable_debug_write_gpx)
			{
				NavitVehicle.pos_recording_end();
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			mTts.stop();
		}
		catch (Exception e)
		{

		}

		try
		{
			mTts.shutdown();
		}
		catch (Exception e)
		{

		}
		mTts = null;

		try
		{
			try
			{
				plugin_api.removeListener(zclientListener);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i("NavitPlugin", "Failed to remove Listener", e);
			}
			unbindService(serviceConnection);
			Log.i("NavitPlugin", "Unbind from the service");
		}
		catch (Throwable t)
		{
			// catch any issues, typical for destroy routines
			// even if we failed to destroy something, we need to continue destroying
			Log.i("NavitPlugin", "Failed to unbind from the service", t);
		}

		try
		{
			if (wl_navigating != null)
			{
				//if (wl_navigating.isHeld())
				//{
				wl_navigating.release();
				Log.e("Navit", "WakeLock Nav: release 1");
				//}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Log.e("Navit", "1***************** exit called ****************");
		Log.e("Navit", "2***************** exit called ****************");
		Log.e("Navit", "3***************** exit called ****************");
		Log.e("Navit", "4***************** exit called ****************");
		Log.e("Navit", "5***************** exit called ****************");
		Log.e("Navit", "6***************** exit called ****************");
		Log.e("Navit", "7***************** exit called ****************");
		Log.e("Navit", "8***************** exit called ****************");

		//		try
		//		{
		//			// hide download actionbar icon
		//			Navit.cur_menu.findItem(R.id.item_download_menu_button).setVisible(false);
		//			Navit.cur_menu.findItem(R.id.item_download_menu_button).setEnabled(false);
		//		}
		//		catch (Exception e)
		//		{
		//		}

		// ----- service stop -----
		// ----- service stop -----
		System.out.println("Navit:exit -> stop ZANaviMapDownloaderService ---------");
		ZANaviMapDownloaderService.stop_downloading();
		stopService(Navit.ZANaviMapDownloaderServiceIntent);
		// ----- service stop -----
		// ----- service stop -----

		// +++++ // System.gc();
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

	public static void set_2d3d_mode_in_settings()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("show_3d_map", p.PREF_show_3d_map);
		editor.commit();
	}

	public static void follow_button_on()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		Navit.follow_current = Navit.follow_on;
		p.PREF_follow_gps = true;
		editor.putBoolean("follow_gps", p.PREF_follow_gps);
		editor.commit();

		// hold all map drawing -----------
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 69);
		msg.setData(b);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// hold all map drawing -----------

		msg = Navit_progress_h.obtainMessage();
		b = new Bundle();
		msg.what = 32;
		try
		{
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
		}

		getPrefs();
		activatePrefs(1);

		// follow mode ON -----------
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", 74);
		msg.setData(b);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// follow mode ON -----------

		// allow all map drawing -----------
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", 70);
		msg.setData(b);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// allow all map drawing -----------

		NavitVehicle.set_last_known_pos_fast_provider();

		// JB fix
		//NavitGraphics.NavitAOSDJava_.postInvalidate();
		//System.out.println("xx paint 12 xx");
		NavitGraphics.OSD_new.postInvalidate();
		NavitGraphics.NavitAOverlay_s.postInvalidate();
	}

	public static void follow_button_off()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		Navit.follow_current = Navit.follow_off;
		p.PREF_follow_gps = false;
		editor.putBoolean("follow_gps", p.PREF_follow_gps);
		editor.commit();
		getPrefs();
		activatePrefs(1);

		// follow mode OFF -----------
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 75);
		msg.setData(b);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// follow mode OFF -----------

		msg = Navit_progress_h.obtainMessage();
		b = new Bundle();
		msg.what = 32;
		try
		{
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
		}

		// JB fix
		//NavitGraphics.NavitAOSDJava_.postInvalidate();
		//System.out.println("xx paint 13 xx");
		NavitGraphics.OSD_new.postInvalidate();
		NavitGraphics.NavitAOverlay_s.postInvalidate();

	}

	public static void toggle_poi_pref()
	{
		// PREF_show_poi_on_map
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		if (p.PREF_show_poi_on_map)
		{
			p.PREF_show_poi_on_map = false;
		}
		else
		{
			p.PREF_show_poi_on_map = true;
		}
		editor.putBoolean("show_poi_on_map", p.PREF_show_poi_on_map);
		editor.commit();
	}

	public static void toggle_follow_button()
	{
		// the "red needle" OSD calls this function only!!
		//Log.e("NavitVehicle", "toggle_follow_button");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		if (p.PREF_follow_gps)
		{
			Navit.follow_current = Navit.follow_off;
			p.PREF_follow_gps = false;

			// follow mode OFF -----------
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 75);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
			}

			catch (Exception e)
			{
				e.printStackTrace();
			}
			// follow mode OFF -----------
		}
		else
		{
			Navit.follow_current = Navit.follow_on;
			p.PREF_follow_gps = true;

			// follow mode ON -----------
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 74);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			// follow mode ON -----------
		}
		editor.putBoolean("follow_gps", p.PREF_follow_gps);
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

		Message msg = Navit_progress_h.obtainMessage();
		Bundle b = new Bundle();
		msg.what = 32;
		try
		{
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
		}

		// hold all map drawing -----------
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", 69);
		msg.setData(b);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
		// allow all map drawing -----------

		NavitVehicle.set_last_known_pos_fast_provider();

		// JB fix
		//NavitGraphics.NavitAOSDJava_.postInvalidate();
		//System.out.println("xx paint 14 xx");
		NavitGraphics.OSD_new.postInvalidate();
		NavitGraphics.NavitAOverlay_s.postInvalidate();
	}

	public static void setPrefs_search_country()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("search_country_id", p.PREF_search_country);
		editor.commit();

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	public static void setPrefs_zoomlevel()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		//System.out.println("1 save zoom level: " + Navit.GlobalScaleLevel);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("zoomlevel_num", Navit.GlobalScaleLevel);
		editor.commit();
		//System.out.println("2 save zoom level: " + Navit.GlobalScaleLevel);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	public static void setPrefs_selected_gpx_dir()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("last_selected_dir_gpxfiles", p.PREF_last_selected_dir_gpxfiles);
		editor.commit();

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void getPrefs_more_map_detail()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// int ret = 0;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		try
		{
			p.PREF_more_map_detail = Integer.parseInt(prefs.getString("more_map_detail", "0"));
		}
		catch (Exception e)
		{
			p.PREF_more_map_detail = 0;
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void get_prefs_highdpi()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		try
		{
			p.PREF_shrink_on_high_dpi = prefs.getBoolean("shrink_on_high_dpi", true);
		}
		catch (Exception e)
		{
			p.PREF_shrink_on_high_dpi = true;
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	public static boolean saveArray(String[] array, String arrayName, int size)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(arrayName + "_size", size);
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == null)
			{
				editor.putString(arrayName + "_" + i, "");
			}
			else
			{
				editor.putString(arrayName + "_" + i, array[i]);
			}
		}
		return editor.commit();
	}

	public static String[] loadArray(String arrayName, int size)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		String[] array = new String[size];
		for (int i = 0; i < size; i++)
		{
			try

			{
				array[i] = prefs.getString(arrayName + "_" + i, "");
			}
			catch (Exception e)
			{
				array[i] = "";
			}
			//System.out.println("array" + i + "=" + array[i]);
		}

		return array;
	}

	public static String[] pushToArray(String[] array_in, String value, int size)
	{
		for (int j = 0; j < size; j++)
		{
			if (array_in[j].equals(value))
			{
				// our value is already in the array, dont add it twice!
				return array_in;
			}
		}

		String[] array = new String[size];
		for (int i = size - 1; i > 0; i--)
		{
			array[i] = array_in[i - 1];
		}
		array[0] = value;
		return array;
	}

	private static void getPrefs()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// save old pref values ---------------
		ZANaviPrefs.deep_copy(p, p_old);
		// save old pref values ---------------

		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		p.PREF_use_fast_provider = prefs.getBoolean("use_fast_provider", true);
		p.PREF_allow_gui_internal = prefs.getBoolean("allow_gui_internal", false);
		p.PREF_follow_gps = prefs.getBoolean("follow_gps", true);
		p.PREF_use_compass_heading_base = prefs.getBoolean("use_compass_heading_base", false);
		p.PREF_use_compass_heading_always = prefs.getBoolean("use_compass_heading_always", false);
		p.PREF_use_compass_heading_fast = prefs.getBoolean("use_compass_heading_fast", false);
		p.PREF_use_anti_aliasing = prefs.getBoolean("use_anti_aliasing", true);
		p.PREF_use_map_filtering = prefs.getBoolean("use_map_filtering", true);
		p.PREF_gui_oneway_arrows = prefs.getBoolean("gui_oneway_arrows", true);
		p.PREF_c_linedrawing = prefs.getBoolean("c_linedrawing", false);

		p.PREF_show_debug_messages = prefs.getBoolean("show_debug_messages", false);

		p.PREF_show_3d_map = prefs.getBoolean("show_3d_map", false);
		send_data_to_plugin_bg(PLUGIN_MSG_CAT_3d_mode, String.valueOf(p.PREF_show_3d_map));

		p.PREF_use_smooth_drawing = prefs.getBoolean("use_smooth_drawing", true);
		p.PREF_use_more_smooth_drawing = prefs.getBoolean("use_more_smooth_drawing", false);
		if (p.PREF_use_smooth_drawing == false)
		{
			p.PREF_use_more_smooth_drawing = false;
		}
		if (p.PREF_use_more_smooth_drawing == true)
		{
			p.PREF_use_smooth_drawing = true;
		}

		boolean b1 = prefs.getBoolean("show_real_gps_pos", false);
		if (b1 == false)
		{
			p.PREF_show_real_gps_pos = 0;
		}
		else
		{
			p.PREF_show_real_gps_pos = 1;
		}

		if (p.PREF_use_more_smooth_drawing)
		{
			NavitGraphics.Vehicle_delay_real_gps_position = 595;
		}
		else
		{
			NavitGraphics.Vehicle_delay_real_gps_position = 450;
		}

		p.PREF_use_lock_on_roads = prefs.getBoolean("use_lock_on_roads", true);
		p.PREF_use_route_highways = prefs.getBoolean("use_route_highways", true);
		p.PREF_save_zoomlevel = prefs.getBoolean("save_zoomlevel", true);
		p.PREF_search_country = prefs.getInt("search_country_id", 1); // default=*ALL*
		p.PREF_zoomlevel_num = prefs.getInt("zoomlevel_num", 174698); // default zoom level = 174698 // shows almost the whole world
		p.PREF_show_sat_status = prefs.getBoolean("show_sat_status", true);
		p.PREF_use_agps = prefs.getBoolean("use_agps", true);
		p.PREF_enable_debug_functions = prefs.getBoolean("enable_debug_functions", false);
		p.PREF_show_turn_restrictions = prefs.getBoolean("show_turn_restrictions", false);

		try
		{
			// recreate the menu items
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 29;
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		p.PREF_enable_debug_write_gpx = prefs.getBoolean("enable_debug_write_gpx", false);
		p.PREF_enable_debug_enable_comm = prefs.getBoolean("enable_debug_enable_comm", false);

		p.PREF_speak_street_names = prefs.getBoolean("speak_street_names", true);
		p.PREF_use_custom_font = prefs.getBoolean("use_custom_font", true);
		p.PREF_draw_polyline_circles = prefs.getBoolean("draw_polyline_circles", true);
		p.PREF_streetsearch_r = prefs.getString("streetsearch_r", "2");
		p.PREF_route_style = prefs.getString("route_style", "3");
		p.PREF_item_dump = prefs.getBoolean("item_dump", false);
		p.PREF_show_route_rects = prefs.getBoolean("show_route_rects", false);
		p.PREF_trafficlights_delay = prefs.getString("trafficlights_delay", "0");
		boolean tmp = prefs.getBoolean("avoid_sharp_turns", false);
		p.PREF_avoid_sharp_turns = "0";
		//if (tmp)
		//{
		//	p.PREF_avoid_sharp_turns = "1";
		//}
		p.PREF_autozoom_flag = prefs.getBoolean("autozoom_flag", true);

		p.PREF_show_multipolygons = prefs.getBoolean("show_multipolygons", true);
		p.PREF_use_index_search = true; // prefs.getBoolean("use_index_search", true);

		// PREF_show_2d3d_toggle = prefs.getBoolean("show_2d3d_toggle", true);
		p.PREF_show_2d3d_toggle = true;

		// PREF_show_vehicle_3d = prefs.getBoolean("show_vehicle_3d", true);
		p.PREF_show_vehicle_3d = true;

		p.PREF_speak_filter_special_chars = prefs.getBoolean("speak_filter_special_chars", true);
		try
		{
			p.PREF_routing_engine = Integer.parseInt(prefs.getString("routing_engine", "0"));
		}
		catch (Exception e)
		{
			p.PREF_routing_engine = 0;
		}

		// send to C code --------
		NavitGraphics.CallbackMessageChannel(55598, "" + p.PREF_routing_engine);
		// send to C code --------

		p.PREF_routing_profile = prefs.getString("routing_profile", "car");
		p.PREF_road_priority_001 = (prefs.getInt("road_priority_001", (68 - 10)) + 10); // must ADD minimum value!!
		p.PREF_road_priority_002 = (prefs.getInt("road_priority_002", (329 - 10)) + 10); // must ADD minimum value!!
		p.PREF_road_priority_003 = (prefs.getInt("road_priority_003", (5000 - 10)) + 10); // must ADD minimum value!!
		p.PREF_road_priority_004 = (prefs.getInt("road_priority_004", (5 - 0)) + 0); // must ADD minimum value!!

		// p.PREF_road_prio_weight_street_1_city = (prefs.getInt("road_prio_weight_street_1_city", (30 - 10)) + 10); // must ADD minimum value!!

		p.PREF_traffic_speed_factor = (prefs.getInt("traffic_speed_factor", (83 - 20)) + 20); // must ADD minimum value!!

		p.PREF_tracking_connected_pref = (prefs.getInt("tracking_connected_pref", (250 - 0)) + 0); // must ADD minimum value!!
		p.PREF_tracking_angle_pref = (prefs.getInt("tracking_angle_pref", (40 - 0)) + 0); // must ADD minimum value!!

		p.PREF_streets_only = prefs.getBoolean("streets_only", false);
		p.PREF_show_status_bar = prefs.getBoolean("show_status_bar", true);
		p.PREF_show_poi_on_map = prefs.getBoolean("show_poi_on_map", false);
		p.PREF_last_selected_dir_gpxfiles = prefs.getString("last_selected_dir_gpxfiles", MAP_FILENAME_PATH + "/../");

		p.PREF_roadspeed_warning = prefs.getBoolean("roadspeed_warning", false);
		p.PREF_lane_assist = prefs.getBoolean("lane_assist", false);

		try
		{
			p.PREF_roadspeed_warning_margin = Integer.parseInt(prefs.getString("roadspeed_warning_margin", "20"));
		}
		catch (Exception e)
		{
			p.PREF_roadspeed_warning_margin = 20;
		}

		p.PREF_StreetSearchStrings = loadArray("xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

		try
		{
			p.PREF_drawatorder = Integer.parseInt(prefs.getString("drawatorder", "0"));
		}
		catch (Exception e)
		{
			p.PREF_drawatorder = 0;
		}

		//try
		//{
		//	PREF_cancel_map_drawing_timeout = Integer.parseInt(prefs.getString("cancel_map_drawing_timeout", "1"));
		//}
		//catch (Exception e)
		//{
		p.PREF_cancel_map_drawing_timeout = 1;
		//}

		try
		{
			p.PREF_map_font_size = Integer.parseInt(prefs.getString("map_font_size", "3"));
		}
		catch (Exception e)
		{
			p.PREF_map_font_size = 2;
		}

		Navit_last_address_search_country_id = p.PREF_search_country;
		Navit_last_address_search_country_iso2_string = NavitAddressSearchCountrySelectActivity.CountryList_Human[p.PREF_search_country][0];

		if (!p.PREF_follow_gps)
		{
			// no compass turning without follow mode!
			p.PREF_use_compass_heading_base = false;
		}

		if (!p.PREF_use_compass_heading_base)
		{
			// child is always "false" when parent is "false" !!
			p.PREF_use_compass_heading_always = false;
		}

		p.PREF_show_maps_debug_view = prefs.getBoolean("show_maps_debug_view", false);

		p.PREF_show_vehicle_in_center = prefs.getBoolean("show_vehicle_in_center", false);
		p.PREF_use_imperial = prefs.getBoolean("use_imperial", false);
		Navit.cur_max_speed = -1; // to update speedwarning graphics

		//		System.out.println("get settings");
		//      System.out.println("PREF_search_country=" + PREF_search_country);
		//		System.out.println("PREF_follow_gps=" + PREF_follow_gps);
		//		System.out.println("PREF_use_fast_provider=" + PREF_use_fast_provider);
		//		System.out.println("PREF_allow_gui_internal=" + PREF_allow_gui_internal);
		//		System.out.println("PREF_use_compass_heading_base=" + PREF_use_compass_heading_base);
		//		System.out.println("PREF_use_compass_heading_always=" + PREF_use_compass_heading_always);
		//		System.out.println("PREF_show_vehicle_in_center=" + PREF_show_vehicle_in_center);
		//		System.out.println("PREF_use_imperial=" + PREF_use_imperial);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void activatePrefs()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		activatePrefs(1);

		if (p.PREF_save_zoomlevel)
		{
			// only if really started, but NOT if returning from our own child activities!!

			//System.out.println("3 restore zoom level: " + Navit.GlobalScaleLevel);
			//System.out.println("4 restore zoom level: " + PREF_zoomlevel_num);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 33);
			b.putString("s", Integer.toString(p.PREF_zoomlevel_num));
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
				Navit.GlobalScaleLevel = p.PREF_zoomlevel_num;
				//System.out.println("5 restore zoom level: " + PREF_zoomlevel_num);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			p.PREF_zoomlevel_num = Navit.GlobalScaleLevel;
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void set_TurnRestrictions_layers()
	{
		int on_ = 59; // on

		if (!p.PREF_show_turn_restrictions)
		{
			on_ = 60; // off
		}

		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", on_);
		b.putString("s", "TurnRestrictions");
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);
	}

	private static void set_poi_layers()
	{
		int on_ = 59;
		int off_ = 60;

		// System.out.println("POI:1");
		if (p.PREF_show_poi_on_map)
		{
			// System.out.println("POI:2");
			on_ = 60;
			off_ = 59;
		}

		// toggle the normal POI layers (to avoid double POIs)
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", on_);
		b.putString("s", "POI Symbols");
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);

		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", on_);
		b.putString("s", "POI Labels");
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);

		// toggle full POI icons on/off
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", off_);
		b.putString("s", "Android-POI-Icons-full");
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);

		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", off_);
		b.putString("s", "Android-POI-Labels-full");
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);

		// draw_map();
	}

	private static void activatePrefs(int dummy)
	{

		need_recalc_route = false; // do we need to recalc the route?

		// call some functions to activate the new settings
		if (p.PREF_follow_gps)
		{
			Navit.follow_current = Navit.follow_on;
		}
		else
		{
			Navit.follow_current = Navit.follow_off;
		}

		if (p.PREF_use_fast_provider)
		{
			NavitVehicle.turn_on_fast_provider();
		}
		else
		{
			NavitVehicle.turn_off_fast_provider();
		}

		if (p.PREF_show_sat_status)
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

		if (p.PREF_show_status_bar)
		{
			show_status_bar_wrapper();
		}
		else
		{
			hide_status_bar_wrapper();
		}

		if (p.PREF_allow_gui_internal)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 10);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (p.PREF_use_compass_heading_base)
		{
			// turn on compass
			msg_to_msg_handler(new Bundle(), 12);
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 11);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (p.PREF_show_maps_debug_view == true)
		{
			// show real gps pos
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 112);
			b.putString("s", "1");
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 112);
			b.putString("s", "0");
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (p.PREF_show_real_gps_pos == 1)
		{
			// show real gps pos
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 111);
			b.putString("s", "1");
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 111);
			b.putString("s", "0");
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		// search radius for housenumbers near streets -----------------
		Message msg43 = new Message();
		Bundle b43 = new Bundle();
		b43.putInt("Callback", 89);
		b43.putString("s", "1500");
		msg43.setData(b43);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg43);
		}
		catch (Exception e)
		{
		}
		// search radius for housenumbers near streets -----------------

		// set routing profile -----------------
		if (Navit_Largemap_DonateVersion_Installed == true)
		{
			Message msg43a = new Message();
			Bundle b43a = new Bundle();
			b43a.putInt("Callback", 90);
			b43a.putString("s", p.PREF_routing_profile); // set routing profile
			msg43a.setData(b43a);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg43a);
			}
			catch (Exception e)
			{
			}

			// need_recalc_route = true;
		}
		// set routing profile -----------------

		Message msg99a = new Message();
		Bundle b99a = new Bundle();
		b99a.putInt("Callback", 98);
		// System.out.println("tracking_connected_pref=" + PREF_tracking_connected_pref);
		b99a.putString("s", "" + p.PREF_tracking_connected_pref); // set routing profile
		msg99a.setData(b99a);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg99a);
		}
		catch (Exception e)
		{
		}

		msg99a = new Message();
		b99a = new Bundle();
		b99a.putInt("Callback", 99);
		// System.out.println("tracking_angle_pref=" + PREF_tracking_angle_pref);
		b99a.putString("s", "" + p.PREF_tracking_angle_pref); // set routing profile
		msg99a.setData(b99a);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg99a);
		}
		catch (Exception e)
		{
		}

		// change road profile -----------------
		if (Navit_Largemap_DonateVersion_Installed == true)
		{
			if (p.PREF_routing_profile.equals("bike-normal"))
			{
				Message msg43b = new Message();
				Bundle b43b = new Bundle();
				b43b.putInt("Callback", 91);
				System.out.println("road_priority_001=" + p.PREF_road_priority_001);
				b43b.putString("s", "" + p.PREF_road_priority_001); // set routing profile
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 92);
				System.out.println("road_priority_002=" + p.PREF_road_priority_002);
				b43b.putString("s", "" + p.PREF_road_priority_002); // set routing profile
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 93);
				System.out.println("road_priority_003=" + p.PREF_road_priority_003);
				b43b.putString("s", "" + p.PREF_road_priority_003); // set routing profile
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 94);
				System.out.println("road_priority_004=" + p.PREF_road_priority_004);
				b43b.putString("s", "" + p.PREF_road_priority_004); // set routing profile
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				// switch off layers --------------------
				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 60);
				b43b.putString("s", "POI traffic lights");
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch off layers --------------------

				// switch ON layers --------------------
				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 59);
				b43b.putString("s", "POI bicycle");
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch ON layers --------------------

			}
			else
			{
				// switch off layers --------------------
				Message msg43b = new Message();
				Bundle b43b = new Bundle();
				b43b.putInt("Callback", 60);
				b43b.putString("s", "POI bicycle");
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch off layers --------------------

				// switch ON layers --------------------
				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 59);
				b43b.putString("s", "POI traffic lights");
				msg43b.setData(b43b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch ON layers --------------------
			}
		}
		// change road profile -----------------

		// -- debug -- change some prio weights --
		//		if ((!p.PREF_routing_profile.equals("bike-normal")) && (!p.PREF_routing_profile.equals("bike-no-oneway")) && (!p.PREF_routing_profile.equals("bike-avoid-roads")))
		//		{
		//			Message msg93 = new Message();
		//			Bundle b93 = new Bundle();
		//			b93.putInt("Callback", 110);
		//			b93.putString("s", "street_1_city#route_prio_weight:" + p.PREF_road_prio_weight_street_1_city);
		//			msg93.setData(b93);
		//			try
		//			{
		//				NavitGraphics.callback_handler.sendMessage(msg93);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//
		//			if (p_old.PREF_road_prio_weight_street_1_city != p.PREF_road_prio_weight_street_1_city)
		//			{
		//				need_recalc_route = true;
		//			}
		//		}
		// -- debug -- change some prio weights --

		if (NavitGraphics.navit_route_status == 0)
		{
			if (p.PREF_c_linedrawing)
			{
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 85);
				b.putString("s", "1");
				msg.setData(b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg);
				}
				catch (Exception e)
				{
				}
			}
			else
			{
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 85);
				b.putString("s", "0");
				msg.setData(b);
				try
				{
					NavitGraphics.callback_handler.sendMessage(msg);
				}
				catch (Exception e)
				{
				}
			}
		}

		Message msg33 = new Message();
		Bundle b33 = new Bundle();
		b33.putInt("Callback", 103);
		if (p.PREF_show_vehicle_in_center)
		{
			b33.putString("s", "0");
		}
		else
		{
			b33.putString("s", "" + NavitGraphics.lower_than_center_percent);
		}
		msg33.setData(b33);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg33);
		}
		catch (Exception e88)
		{
		}

		if (p.PREF_use_imperial)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 16);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (p.PREF_show_debug_messages)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 24);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		//		if (PREF_show_3d_map)
		//		{
		//			Message msg = new Message();
		//			Bundle b = new Bundle();
		//			b.putInt("Callback", 31);
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
		//			b.putInt("Callback", 30);
		//			msg.setData(b);
		//			try
		//			{
		//				N_NavitGraphics.callback_handler.sendMessage(msg);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//		}

		if (p.PREF_use_lock_on_roads)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 36);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);
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

		if (p.PREF_use_route_highways)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 42);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		Message msg7 = new Message();
		Bundle b7 = new Bundle();
		b7.putInt("Callback", 57);
		b7.putString("s", "" + p.PREF_drawatorder);
		msg7.setData(b7);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}

		msg7 = new Message();
		b7 = new Bundle();
		b7.putInt("Callback", 58);
		b7.putString("s", p.PREF_streetsearch_r);
		msg7.setData(b7);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}

		if (p.PREF_speak_street_names)
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 54);
			msg.setData(b);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg);
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
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			catch (Exception e)
			{
			}

		}

		try
		{
			NavitGraphics.OverlayDrawThread_cancel_drawing_timeout = NavitGraphics.OverlayDrawThread_cancel_drawing_timeout__options[p.PREF_cancel_map_drawing_timeout];
			NavitGraphics.OverlayDrawThread_cancel_thread_sleep_time = NavitGraphics.OverlayDrawThread_cancel_thread_sleep_time__options[p.PREF_cancel_map_drawing_timeout];
			NavitGraphics.OverlayDrawThread_cancel_thread_timeout = NavitGraphics.OverlayDrawThread_cancel_thread_timeout__options[p.PREF_cancel_map_drawing_timeout];
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
			NavitGraphics.callback_handler.sendMessage(msg67);
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
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// turn off 3
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 60);
		b67.putString("s", "route_003");
		msg67.setData(b67);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}

		// turn on the wanted route style
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 59);
		b67.putString("s", "route_00" + p.PREF_route_style);
		msg67.setData(b67);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// route variant

		// show route rectanlges -----
		if (p.PREF_show_route_rects)
		{
			msg67 = new Message();
			b67 = new Bundle();
			b67.putInt("Callback", 76);
			msg67.setData(b67);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg67);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			msg67 = new Message();
			b67 = new Bundle();
			b67.putInt("Callback", 77);
			msg67.setData(b67);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg67);
			}
			catch (Exception e)
			{
			}
		}
		// show route rectanlges -----

		// show route multipolygons -----
		if (p.PREF_show_multipolygons)
		{
			msg67 = new Message();
			b67 = new Bundle();
			b67.putInt("Callback", 66);
			msg67.setData(b67);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg67);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			msg67 = new Message();
			b67 = new Bundle();
			b67.putInt("Callback", 67);
			msg67.setData(b67);
			try
			{
				NavitGraphics.callback_handler.sendMessage(msg67);
			}
			catch (Exception e)
			{
			}
		}
		// show route multipolygons -----

		// traffic lights delay ----
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 79);
		//System.out.println("traffic lights delay:" + PREF_trafficlights_delay);
		// (PREF_trafficlights_delay / 10) seconds delay for each traffic light
		b67.putString("s", p.PREF_trafficlights_delay); // (delay in 1/10 of a second)
		msg67.setData(b67);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// traffic lights delay ----

		// avoid sharp turns ----
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 86);
		b67.putString("s", p.PREF_avoid_sharp_turns);
		msg67.setData(b67);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}

		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 87);
		b67.putString("s", "47"); // **DANGER** sharp turn max angle hardcoded here!! **DANGER**
		msg67.setData(b67);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}

		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 88);
		b67.putString("s", "6000");
		msg67.setData(b67);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// avoid sharp turns ----

		// autozoom flag ----
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 80);
		if (p.PREF_autozoom_flag)
		{
			b67.putString("s", "1"); // (0 or 1)
		}
		else
		{
			b67.putString("s", "0"); // (0 or 1)			
		}
		msg67.setData(b67);
		try
		{
			NavitGraphics.callback_handler.sendMessage(msg67);
		}
		catch (Exception e)
		{

		}
		// autozoom flag ----

		if ((Navit.Navit_Largemap_DonateVersion_Installed) || (Navit.Navit_DonateVersion_Installed))
		{
			// use pref
		}
		else
		{
			p.PREF_roadspeed_warning = false;
		}

		if ((Navit.Navit_Largemap_DonateVersion_Installed) || (Navit.Navit_DonateVersion_Installed))
		{
			// use pref
		}
		else
		{
			p.PREF_lane_assist = false;
		}

		if (p.PREF_streets_only)
		{
			// ----------------------- streets only pref -------------------
			// 59 -> enable
			// 60 -> disable
			Message msg31 = new Message();
			Bundle b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "polygons001");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "polygons");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "POI Symbols");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "POI Labels");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Icons-full");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Labels-full");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_1");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_2");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_STR_ONLY");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_1_STR_ONLY");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_2_STR_ONLY");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);
			// ----------------------- streets only pref -------------------
		}
		else
		{
			// ----------------------- streets only pref -------------------
			// 59 -> enable
			// 60 -> disable
			Message msg31 = new Message();
			Bundle b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "polygons001");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "polygons");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "POI Symbols");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "POI Labels");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Icons-full");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Labels-full");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_1");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_2");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_STR_ONLY");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_1_STR_ONLY");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_2_STR_ONLY");
			msg31.setData(b31);
			NavitGraphics.callback_handler.sendMessage(msg31);
			// ----------------------- streets only pref -------------------
		}

		// ---------- set traffic factor for road speeds ------------
		try
		{
			Message msg_ss1 = new Message();
			Bundle b_ss1 = new Bundle();
			b_ss1.putInt("Callback", 106);
			b_ss1.putString("s", Integer.toString(p.PREF_traffic_speed_factor));
			msg_ss1.setData(b_ss1);
			NavitGraphics.callback_handler.sendMessage(msg_ss1);
		}
		catch (Exception e)
		{
		}
		// ---------- set traffic factor for road speeds ------------

		// ------- PREF_show_poi_on_map ---------------------------------
		set_poi_layers();
		// ------- PREF_show_poi_on_map ---------------------------------

		set_TurnRestrictions_layers();

		// set vars for mapdir change (only really takes effect after restart!)
		getPrefs_mapdir();

		if (need_recalc_route)
		{
			recalculate_route();
			need_recalc_route = false;
		}

	}

	@SuppressLint("NewApi")
	private static void getPrefs_mapdir()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		String default_sdcard_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/zanavi/maps/";
		String default_sdcard_dir_1 = default_sdcard_dir;
		System.out.println("DataStorageDir[s1]=" + default_sdcard_dir_1);

		// check for Android KitKat 4.4 ---------------
		try
		{
			if (Integer.valueOf(android.os.Build.VERSION.SDK) > 18)
			{
				// use app private dir
				default_sdcard_dir = Navit.getBaseContext_.getExternalFilesDir(null).getAbsolutePath();
			}
		}
		catch (Exception e)
		{
		}
		// check for Android KitKat 4.4 ---------------

		try
		{
			NavitDataStorageDirs = android.support.v4.content.ContextCompat.getExternalFilesDirs(Navit.getBaseContext_, null);

			if (NavitDataStorageDirs.length > 0)
			{
				// use new method
				default_sdcard_dir = NavitDataStorageDirs[0].getAbsolutePath() + "/zanavi/maps/";
				System.out.println("DataStorageDir count=" + NavitDataStorageDirs.length);

				for (int jj2 = 0; jj2 < NavitDataStorageDirs.length; jj2++)
				{
					if (NavitDataStorageDirs[jj2] != null)
					{
						System.out.println("DataStorageDir[" + jj2 + "]=" + NavitDataStorageDirs[jj2].getAbsolutePath() + "/zanavi/maps/");
					}
				}
			}

			if (NavitDataStorageDirs.length == 1)
			{
				File tf = null;
				File[] NavitDataStorageDirs_ = new File[NavitDataStorageDirs.length + 1];

				try
				{
					Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
					// String sdCard__ = externalLocations.get(ExternalStorage.SD_CARD).getAbsolutePath();
					String externalSdCard__ = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD).getAbsolutePath();
					// System.out.println("DataStorageDir[sd]=" + sdCard__);
					System.out.println("DataStorageDir[external sd]=" + externalSdCard__);

					for (int jj2 = 0; jj2 < NavitDataStorageDirs.length; jj2++)
					{
						if (NavitDataStorageDirs[jj2] == null)
						{
							NavitDataStorageDirs_[jj2] = null;
						}
						else
						{
							NavitDataStorageDirs_[jj2] = new File(NavitDataStorageDirs[jj2].getAbsolutePath() + "/zanavi/maps/");
						}
					}

					tf = new File(externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD).getAbsolutePath() + "/Android/data/com.zoffcc.applications.zanavi/files" + "/zanavi/maps/");
				}
				catch (Exception e)
				{
					tf = null;
				}

				if (tf != null)
				{
					NavitDataStorageDirs_[NavitDataStorageDirs.length] = tf;
					NavitDataStorageDirs = null;
					NavitDataStorageDirs = NavitDataStorageDirs_;
				}
			}

		}
		catch (Exception e)
		{
			System.out.println("DataStorageDir Ex002");
			e.printStackTrace();
		}

		//Log.e("Navit", "old sdcard dir=" + NavitDataDirectory_Maps);
		//Log.e("Navit", "default sdcard dir=" + default_sdcard_dir);
		NavitDataDirectory_Maps = prefs.getString("map_directory", default_sdcard_dir + "/zanavi/maps/");
		String Navit_storage_directory_select = prefs.getString("storage_directory", "-1");
		int Navit_storage_directory_select_i = 0;
		try
		{
			Navit_storage_directory_select_i = Integer.parseInt(Navit_storage_directory_select);
		}
		catch (Exception e)
		{

		}
		System.out.println("DataStorageDir[sel 1]=" + NavitDataDirectory_Maps);
		System.out.println("DataStorageDir[sel 2]=" + Navit_storage_directory_select);

		if (Navit_storage_directory_select_i > 0)
		{
			NavitDataDirectory_Maps = NavitDataStorageDirs[Navit_storage_directory_select_i - 1].getAbsolutePath();
		}
		System.out.println("DataStorageDir[*in use*]=" + NavitDataDirectory_Maps);

		// Navit_storage_directory_select:
		// -1    --> first run -> select best dir for user
		//  0    --> use custom directory
		//  1..n --> select default dir on SD Card number 1..n

		// ** DEBUG ** set dir manually ** // NavitDataDirectory_Maps = default_sdcard_dir + "/zanavi/maps/";
		// ** DEBUG ** NavitDataDirectory_Maps = prefs.getString("navit_mapsdir", "/sdcard" + "/zanavi/maps/");
		//Log.e("Navit", "new sdcard dir=" + NavitDataDirectory_Maps);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
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
		// System.out.println("sanity check:" + ret);
		return ret;
	}

	private static void activatePrefs_mapdir(Boolean at_startup)
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// activate the new directory
		NavitDataDirectory_Maps = sanity_check_maps_dir(NavitDataDirectory_Maps);
		MAP_FILENAME_PATH = NavitDataDirectory_Maps;
		MAPMD5_FILENAME_PATH = NavitDataDirectory_Maps + "/../md5/";
		CFG_FILENAME_PATH = NavitDataDirectory_Maps + "/../";
		NAVIT_DATA_DEBUG_DIR = CFG_FILENAME_PATH + "../debug/";

		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");

		Handler h_temp = null;
		h_temp = NavitGraphics.callback_handler_s;
		//System.out.println("handler 1=" + h_temp.toString());

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

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private void getPrefs_theme()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int theme_tmp = Integer.parseInt(prefs.getString("current_theme", "0"));
		// 0 -> Navit.DEFAULT_THEME_OLD_DARK
		// 1 -> Navit.DEFAULT_THEME_OLD_LIGHT
		p.PREF_current_theme = Navit.DEFAULT_THEME_OLD_DARK;
		if (theme_tmp == 1)
		{
			p.PREF_current_theme = Navit.DEFAULT_THEME_OLD_LIGHT;
		}
		else
		{
			p.PREF_current_theme = Navit.DEFAULT_THEME_OLD_DARK;
		}

	}

	private void getPrefs_theme_main()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int theme_tmp = Integer.parseInt(prefs.getString("current_theme", "0"));
		// 0 -> Navit.DEFAULT_THEME_OLD_DARK
		// 1 -> Navit.DEFAULT_THEME_OLD_LIGHT
		p.PREF_current_theme_M = Navit.DEFAULT_THEME_OLD_DARK_M;
		if (theme_tmp == 1)
		{
			p.PREF_current_theme_M = Navit.DEFAULT_THEME_OLD_LIGHT_M;
		}
		else
		{
			p.PREF_current_theme_M = Navit.DEFAULT_THEME_OLD_DARK_M;
		}

	}

	private static void getPrefs_loc()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		p.PREF_navit_lang = prefs.getString("navit_lang", "*DEFAULT*");
		System.out.println("**** ***** **** pref lang=" + p.PREF_navit_lang);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void activatePrefs_loc()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// creating locale
		if (!p.PREF_navit_lang.equals("*DEFAULT*"))
		{
			Locale locale2 = null;
			if (p.PREF_navit_lang.contains("_"))
			{
				String _lang = p.PREF_navit_lang.split("_", 2)[0];
				String _country = p.PREF_navit_lang.split("_", 2)[1];
				System.out.println("l=" + _lang + " c=" + _country);
				locale2 = new Locale(_lang, _country);
			}
			else
			{
				locale2 = new Locale(p.PREF_navit_lang);
			}
			Locale.setDefault(locale2);
			Configuration config2 = new Configuration();
			config2.locale = locale2;
			// updating locale
			getBaseContext_.getResources().updateConfiguration(config2, null);
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void getPrefs_mapcache()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Navit.getBaseContext_);
		try
		{
			p.PREF_mapcache = Integer.parseInt(prefs.getString("mapcache", "" + (10 * 1024)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			p.PREF_mapcache = 10 * 1024;
		}
		System.out.println("**** ***** **** pref mapcache=" + p.PREF_mapcache);
	}

	private static void activatePrefs_mapcache()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		Handler h_temp2 = null;
		h_temp2 = NavitGraphics.callback_handler_s;
		System.out.println("activatePrefs_mapcache " + NavitGraphics.callback_handler_s);
		Message msg7 = new Message();
		Bundle b7 = new Bundle();
		b7.putInt("Callback", 55);
		b7.putString("s", String.valueOf(p.PREF_mapcache * 1024));
		msg7.setData(b7);
		h_temp2.sendMessage(msg7);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	public native void NavitMain(Navit x, String lang, int version, String display_density_string, String n_datadir, String n_sharedir, Bitmap main_map_bitmap);

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
		Navit.use_index_search = Navit.allow_use_index_search();
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

	private void share_location(String lat, String lon, String name, String subject_text, String time_at_destination, boolean is_dest)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		String url;
		final String map_zoomlevel = "18";
		// url = "" + lat + "," + lon + "\n" + name;
		url = "http://maps.google.com/?q=" + lat + "," + lon + "&z=" + map_zoomlevel + "\n\n" + name;

		if (is_dest)
		{
			if (time_at_destination.compareTo("") != 0)
			{
				url = url + " " + time_at_destination;
			}
		}

		intent.putExtra(Intent.EXTRA_TEXT, url);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject_text);
		//Uri uri = Uri.parse("geo:0,0?z=16&q=" + lat + "," + lon);
		//intent.putExtra(Intent.EXTRA_STREAM, uri);

		// shut down TTS ---------------
		//		if (!Navit.is_navigating)
		//		{
		//			try
		//			{
		//				mTts.stop();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			try
		//			{
		//				mTts.shutdown();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//		}
		// shut down TTS ---------------

		startActivityForResult(Intent.createChooser(intent, Navit.get_text("Share")), NavitAddressSearch_id_sharedest); // TRANS
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
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		System.out.println("");
		System.out.println("*** Zoom out FULL ***");
		System.out.println("");
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 8);
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	static public void show_geo_on_screen_with_zoom_and_delay(float lat, float lon)
	{
		show_geo_on_screen_with_zoom_and_delay(lat, lon, 0);
	}

	@SuppressLint("NewApi")
	static public void show_geo_on_screen_with_zoom_and_delay(final float lat, final float lon, final int millis_start_delay)
	{
		System.out.println("XSOM:010");

		//		if (api_version_int < 11)
		//		{
		//
		//			new AsyncTask<Void, Void, String>()
		//			{
		//				@Override
		//				protected String doInBackground(Void... params)
		//				{
		//					System.out.println("XSOM:011");
		//
		//					try
		//					{
		//						System.out.println("XSOM:001");
		//						Thread.sleep((long) millis_start_delay);
		//						Navit.show_geo_on_screen_no_draw(lat, lon);
		//						System.out.println("XSOM:002");
		//						Thread.sleep(100);
		//						Navit.set_zoom_level_no_draw(Navit.Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL);
		//						System.out.println("XSOM:004");
		//						Thread.sleep(120);
		//						Navit.draw_map();
		//						System.out.println("XSOM:005");
		//					}
		//					catch (Exception e)
		//					{
		//					}
		//
		//					return "";
		//				}
		//
		//				@Override
		//				protected void onPostExecute(String msg)
		//				{
		//
		//				}
		//			}.execute(null, null, null);
		//
		//		}
		//		else
		//		{
		//			new AsyncTask<Void, Void, String>()
		//			{
		//				@Override
		//				protected String doInBackground(Void... params)
		//				{
		//					System.out.println("XSOM:011");
		//
		//					try
		//					{
		//						System.out.println("XSOM:001");
		//						Thread.sleep((long) millis_start_delay);
		//						Navit.show_geo_on_screen_no_draw(lat, lon);
		//						System.out.println("XSOM:002");
		//						Thread.sleep(100);
		//						Navit.set_zoom_level_no_draw(Navit.Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL);
		//						System.out.println("XSOM:004");
		//						Thread.sleep(120);
		//						Navit.draw_map();
		//						System.out.println("XSOM:005");
		//					}
		//					catch (Exception e)
		//					{
		//					}
		//
		//					return "";
		//				}
		//
		//				@Override
		//				protected void onPostExecute(String msg)
		//				{
		//
		//				}
		//			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		//
		//		}

		final Thread temp_work_thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep((long) millis_start_delay);
					Navit.show_geo_on_screen_no_draw(lat, lon);
					Thread.sleep(500);
					Navit.set_zoom_level_no_draw(Navit.Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL);
					Thread.sleep(120);
					Navit.draw_map();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
		};
		temp_work_thread.start();
	}

	static public void show_geo_on_screen(float lat, float lng)
	{
		// this function sets screen center to "lat, lon", and just returns a dummy string!
		Navit.cwthr.CallbackGeoCalc2(2, 3, lat, lng);
	}

	static public void show_geo_on_screen_no_draw(float lat, float lng)
	{
		// this function sets screen center to "lat, lon", and just returns a dummy string!
		Navit.cwthr.CallbackGeoCalc2(2, 15, lat, lng);
	}

	public static void zoom_to_route()
	{
		try
		{
			//System.out.println("");
			//System.out.println("*** Zoom to ROUTE ***");
			//System.out.println("");
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 17);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);

			set_map_position_to_screen_center();
		}
		catch (Exception e)
		{
		}
	}

	static void set_map_position_to_screen_center()
	{
		try
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 51);

			if (Navit.GFX_OVERSPILL)
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * ((Navit.NG__map_main.view.getWidth() / 2) + NavitGraphics.mCanvasWidth_overspill)));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * ((Navit.NG__map_main.view.getHeight() / 2) + NavitGraphics.mCanvasHeight_overspill)));
			}
			else
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
			}
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
	}

	public void turn_on_compass()
	{
		try
		{
			if (!p.PREF_use_compass_heading_fast)

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
		if (!p.PREF_show_status_bar)
		{
			// Hide the Status Bar
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	public void show_status_bar()
	{
		// Show the Status Bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void hide_title_bar()
	{
		// -- unsued !!!! --
		// -- unsued !!!! --
		// -- unsued !!!! --

		// Hide the Title Bar - this works ONLY before setcontent in onCreate!!
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void show_title_bar()
	{
		// -- unsued !!!! --
		// -- unsued !!!! --
		// -- unsued !!!! --

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
			//System.out.println("ret1=" + ret2);
			ret2 = locationmanager2.sendExtraCommand("gps", "force_time_injection", bundle);
			ret = ret || ret2;
			//System.out.println("ret2=" + ret2);
		}
		catch (Exception e)
		{
			//System.out.println("*XX*");
			e.printStackTrace();
		}
		return ret;
	}

	static void remove_oldest_normal_point()
	{
		int i;
		for (i = 0; i < map_points.size(); i++)
		{
			Navit_Point_on_Map element_temp = map_points.get(i);
			if (element_temp.addon == null)
			{
				// its a normal (non home, non special item), so can remove it, and return.
				break;
			}
		}
	}

	static int find_home_point()
	{
		int home_id = -1;
		int i;

		for (i = 0; i < map_points.size(); i++)
		{
			Navit_Point_on_Map element_temp = map_points.get(i);
			if (element_temp.addon != null)
			{
				if (element_temp.addon.equals("1"))
				{
					// found home
					return i;
				}
			}
		}
		return home_id;
	}

	static void readd_home_point()
	{
		try
		{
			int home_id = find_home_point();
			if (home_id != -1)
			{
				Navit_Point_on_Map element_old = map_points.get(home_id);
				map_points.remove(home_id);
				map_points.add(element_old);
			}
		}
		catch (Exception e)
		{
		}
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

		int el_pos = get_destination_pos(element);
		// int home_id = find_home_point();
		boolean is_home = is_home_element(element);

		// System.out.println("EEPOS:el_pos=" + el_pos + " is_home=" + is_home);

		if (is_home)
		{
			// its the "home" destination
			return;
		}

		if (map_points.size() > Navit_MAX_RECENT_DESTINATIONS)
		{
			try
			{
				// map_points.remove(0);
				remove_oldest_normal_point();
			}
			catch (Exception e)
			{
			}
		}

		if (el_pos == -1)
		{
			// if not duplicate, then add
			map_points.add(element);
			readd_home_point();
			write_map_points();
		}
		else
		{
			try
			{
				// if already in list, then first remove and add again
				// that moves it to the top of the list
				Navit_Point_on_Map element_old = map_points.get(el_pos);
				map_points.remove(el_pos);
				map_points.add(element_old);
				readd_home_point();
				write_map_points();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
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
			fos = new FileOutputStream(NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME);
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

	private void export_map_points_to_sdcard()
	{
		String orig_file = NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME;
		String dest_file_dir = CFG_FILENAME_PATH + "../export/";

		try
		{
			File dir = new File(dest_file_dir);
			dir.mkdirs();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			File source = new File(orig_file);
			File destination = new File(dest_file_dir + Navit_DEST_FILENAME);

			if (source.exists())
			{
				FileInputStream fi = new FileInputStream(source);
				FileOutputStream fo = new FileOutputStream(destination);
				FileChannel src = fi.getChannel();
				FileChannel dst = fo.getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				fi.close();
				fo.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void import_map_points_from_sdcard()
	{
		String orig_file = NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME;
		String dest_file_dir = CFG_FILENAME_PATH + "../export/";

		try
		{
			File source = new File(dest_file_dir + Navit_DEST_FILENAME);
			File destination = new File(orig_file);

			if (source.exists())
			{
				FileInputStream fi = new FileInputStream(source);
				FileOutputStream fo = new FileOutputStream(destination);
				FileChannel src = fi.getChannel();
				FileChannel dst = fo.getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				fi.close();
				fo.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		read_map_points();
	}

	//@SuppressWarnings("unchecked")
	private void deserialize_map_points()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try
		{
			fis = new FileInputStream(NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME);
			// openFileInput(CFG_FILENAME_PATH + Navit_DEST_FILENAME);
			ois = new ObjectInputStream(fis);
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

		try
		{
			if (ois != null)
			{
				ois.close();
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			if (fis != null)
			{
				ois.close();
			}
		}
		catch (Exception e)
		{
		}

		//		for (int j = 0; j < map_points.size(); j++)
		//		{
		//			System.out.println("####******************" + j + ":" + map_points.get(j).point_name);
		//		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);

	}

	static void remember_destination_xy(String name, int x, int y)
	{
		// i=1 -> pixel a,b (x,y)      -> geo   string "lat(float):lng(float)"
		// i=2 -> geo   a,b (lat,lng)  -> pixel string "x(int):y(int)"
		String lat_lon = NavitGraphics.CallbackGeoCalc(1, (x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
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
			if (t.addon == null)
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (element.addon == null))
				{
					return true;
				}
			}
			else
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (t.addon.equals(element.addon)))
				{
					return true;
				}
			}
		}
		return ret;
	}

	static int get_destination_pos(Navit_Point_on_Map element)
	{
		int ret = -1;
		Navit_Point_on_Map t;
		for (int i = 0; i < map_points.size(); i++)
		{
			t = map_points.get(i);
			if (t.addon == null)
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (element.addon == null))
				{
					return i;
				}
			}
			else
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (t.addon.equals(element.addon)))
				{
					return i;
				}
			}
		}
		return ret;
	}

	static boolean is_home_element(Navit_Point_on_Map element)
	{
		int home_id = find_home_point();
		if (home_id != -1)
		{
			Navit_Point_on_Map home_element = map_points.get(home_id);

			if ((home_element.point_name.equals(element.point_name)) && (home_element.lat == element.lat) && (home_element.lon == element.lon))
			{
				return true;
			}

		}

		return false;
	}

	static NavitSpeech2 get_speech_object()
	{
		System.out.println("get_speech_object");
		return NSp;
	}

	static NavitVehicle get_vehicle_object()
	{
		System.out.println("get_vehicle_object");
		return NV;
	}

	static NavitGraphics get_graphics_object_by_name(String name)
	{
		System.out.println("get_graphics_object_by_name:*" + name + "*");

		if (name.equals("type:map-main"))
		{
			System.out.println("map-main");
			return NG__map_main;
		}
		else
		{
			System.out.println("vehicle");
			return NG__vehicle;
		}
	}

	public static Handler vehicle_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			//System.out.println("vehicle_handler:handleMessage:JTHREAD ID=" + Thread.currentThread().getId());
			//System.out.println("vehicle_handler:handleMessage:THREAD ID=" + NavitGraphics.GetThreadId());

			switch (msg.what)
			{
			case 1:
				Location l = new Location("Network");
				l.setLatitude(msg.getData().getFloat("lat"));
				l.setLongitude(msg.getData().getFloat("lng"));
				l.setBearing(msg.getData().getFloat("b"));
				l.setSpeed(0.8f);
				NavitVehicle.set_mock_location__fast(l);
				break;
			case 2:
				if (NavitVehicle.update_location_in_progress)
				{
				}
				else
				{
					NavitVehicle.update_location_in_progress = true;
					NavitVehicle.VehicleCallback2(NavitVehicle.last_location);
					NavitVehicle.update_location_in_progress = false;
				}
				break;
			}
		}
	};

	public String roundTwoDecimals(double d)
	{
		return String.format(Locale.US, "%.2f", d);
	}

	/* ZZ GEO TRANS ZZ */
	static double __EARTH_RADIUS__ = 6378137.000;
	static double __GEO_ACCURACY_FACTOR__ = 2.000;
	final static double M_PI_div_360 = 0.008726646259971647884618;
	final static double M_PI_div_180 = 0.01745329251994329576;
	final static double M_PI_mul_360 = 1130.97335529232556584560;
	final static double M_PI_mul_180 = 565.48667764616278292280;

	/* ZZ GEO TRANS ZZ */

	public static int transform_from_geo_lat(double lat)
	{
		/* ZZ GEO TRANS ZZ */
		int ret = (int) ((Math.log(Math.tan((Math.PI / 4f) + lat * M_PI_div_360)) * __EARTH_RADIUS__) * __GEO_ACCURACY_FACTOR__);
		return ret;
	}

	public static int transform_from_geo_lon(double lon)
	{
		/* ZZ GEO TRANS ZZ */
		int ret = (int) ((lon * __EARTH_RADIUS__ * M_PI_div_180) * __GEO_ACCURACY_FACTOR__);

		return ret;
	}

	public static double transform_to_geo_lat(float y) // y
	{
		/* ZZ GEO TRANS ZZ */
		return (Math.atan(Math.exp((y / __GEO_ACCURACY_FACTOR__) / __EARTH_RADIUS__)) / M_PI_div_360 - 90);
	}

	public static double transform_to_geo_lon(float x) // x
	{
		/* ZZ GEO TRANS ZZ */
		return ((x / __GEO_ACCURACY_FACTOR__) / __EARTH_RADIUS__ / M_PI_div_180);
	}

	public static boolean allow_use_index_search()
	{
		if ((!Navit_DonateVersion_Installed) && (!Navit_Largemap_DonateVersion_Installed))
		{
			// no donate version installed
			Log.e("Navit", "no donate version installed");

			Navit_index_on_but_no_idx_files = false;
			return false;
		}

		boolean ret = false;

		// MAP_FILENAME_PATH
		File folder = new File(MAP_FILENAME_PATH);
		File[] listOfFiles = folder.listFiles();
		File idx = null;
		File md5_file = null;
		FileOutputStream fos = null;

		int files = 0;
		int file_without_index = 0;

		for (File file : listOfFiles)
		{
			if (file.isFile())
			{
				// System.out.println("FFNN:" + file.getName());

				if (file.getName().endsWith(".bin"))
				{
					if (!file.getName().equals("borders.bin"))
					{
						if (!file.getName().equals("coastline.bin"))
						{
							files++;
							idx = new File(folder, file.getName() + ".idx");
							if (idx.exists())
							{
							}
							else
							{
								file_without_index++;

								String servername = "";
								try
								{
									servername = NavitMapDownloader.is_in_cat_file_disk_name(file.getName()).split(":", 2)[1];
								}
								catch (Exception ee)
								{
								}

								if (!servername.equals(""))
								{

									// index for this map is missing. hack MD5 file so we can download it again
									md5_file = new File(Navit.MAPMD5_FILENAME_PATH + "/" + servername + ".md5");

									// System.out.println("FFNN:hack MD5:" + md5_file.getAbsolutePath() + " s=" + servername);

									if ((md5_file.exists()) && (md5_file.canWrite()))
									{
										try
										{
											fos = new FileOutputStream(md5_file);
											fos.write(65);
											fos.write(65);
											fos.write(65);
											fos.close();
										}
										catch (Exception e1)
										{
											// System.out.println("FFNN:EEEEEEEEEEEEEE");
										}
									}
								}
							}
						}
					}
				}

				if (file.getName().endsWith(".bin.idx"))
				{
					ret = true;
				}
			}
		}

		if (files > 0)
		{
			if (file_without_index > 0)
			{
				Navit_index_on_but_no_idx_files = true;
			}
			else
			{
				Navit_index_on_but_no_idx_files = false;
			}
		}

		return ret;
	}

	private void sendEmail(String recipient, String subject, String message)
	{
		try
		{
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			if (recipient != null) emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { recipient });
			if (subject != null) emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			if (message != null) emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);

			startActivity(Intent.createChooser(emailIntent, Navit.get_text("Send feedback via email ...")));

		}
		catch (ActivityNotFoundException e)
		{
			// cannot send email for some reason
		}
	}

	@SuppressLint("NewApi")
	void detect_menu_button()
	{
		// default: we dont have a real menu button
		has_hw_menu_button = false;

		try
		{
			if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 14)
			{
				if (ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey())
				{
					has_hw_menu_button = true;
				}
				else
				{
					has_hw_menu_button = false;
				}
			}
			else
			{
				has_hw_menu_button = true;
			}
		}
		catch (Exception e)
		{
			// on error we must be on android API < 14 and therfore we have a menu button (that is what we assume)
			has_hw_menu_button = true;
		}

		// now always show menu button icon
		has_hw_menu_button = false;
	}

	public void onBackPressed()
	{
		// do something on back.
		//System.out.println("no back key!");
		// super.onBackPressed();
		// !!disable the back key otherwise!!

		if (Navit_doubleBackToExitPressedOnce)
		{

			try
			{
				if (wl_navigating != null)
				{
					//if (wl_navigating.isHeld())
					//{
					wl_navigating.release();
					Log.e("Navit", "WakeLock Nav: release 1");
					//}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			// super.onBackPressed(); --> this would only put the app in background
			// --------
			// exit the app here
			this.onPause();
			this.onStop();
			this.exit();
			// --------
			return;
		}

		try
		{
			Navit_doubleBackToExitPressedOnce = true;
			Toast.makeText(this, Navit.get_text("Please press BACK again to Exit"), Toast.LENGTH_SHORT).show();

			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					Navit_doubleBackToExitPressedOnce = false;
				}
			}, NAVIT_BACKBUTTON_TO_EXIT_TIME);
		}
		catch (Exception e)
		{
		}

		try
		{
			// if bottom bar is up, animate it down on "back" press
			animate_bottom_bar_down();
		}
		catch (Exception e)
		{
		}

	}

	//	public void openOptionsMenu_wrapper()
	//	{
	//		//openOptionsMenu();
	//
	//		prepare_emu_options_menu();
	//		NavitGraphics.emu_menu_view.set_adapter(EmulatedMenuView.MenuItemsList2, EmulatedMenuView.MenuItemsIdMapping2);
	//		NavitGraphics.emu_menu_view.bringToFront();
	//		NavitGraphics.emu_menu_view.setVisibility(View.VISIBLE);
	//	}

	public static String logHeap(Class clazz)
	{
		Double allocated = Double.valueOf(Debug.getNativeHeapAllocatedSize()) / Double.valueOf((1048576));
		Double sum_size = Double.valueOf(Debug.getNativeHeapSize() / Double.valueOf(1048576.0));
		Double free = Double.valueOf(Debug.getNativeHeapFreeSize() / Double.valueOf(1048576.0));
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		// Log.d("Navit", "MemMem:DEBUG: =================================");
		Log.d("Navit", "MemMem:DEBUG:heap native: allc " + df.format(allocated) + "MB sum=" + df.format(sum_size) + "MB (" + df.format(free) + "MB free) in [" + clazz.getName().replaceAll("com.zoffcc.applications.zanavi.", "") + "]");
		Log.d("Navit", "MemMem:DEBUG:java memory: allc: " + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "MB sum=" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)");

		calcAvailableMemory();

		String mem_type = "NATIVE";
		try
		{
			if (android.os.Build.VERSION.SDK_INT >= 11)
			{
				mem_type = "JAVA";
			}
		}
		catch (Exception e)
		{
		}
		return ("" + df.format(allocated) + "/" + df.format(sum_size) + "(" + df.format(free) + ")" + ":" + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "/" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "(" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + ") " + mem_type);
	}

	public static String logHeap_for_batch(Class clazz)
	{
		try
		{
			Double allocated = Double.valueOf(Debug.getNativeHeapAllocatedSize()) / Double.valueOf((1048576));
			Double sum_size = Double.valueOf(Debug.getNativeHeapSize() / Double.valueOf(1048576.0));
			Double free = Double.valueOf(Debug.getNativeHeapFreeSize() / Double.valueOf(1048576.0));
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(2);

			// Log.d("Navit", "MemMem:DEBUG: =================================");
			Log.d("Navit", "MemMem:DEBUG:heap native: allc " + df.format(allocated) + "MB sum=" + df.format(sum_size) + "MB (" + df.format(free) + "MB free) in [" + clazz.getName().replaceAll("com.zoffcc.applications.zanavi.", "") + "]");
			Log.d("Navit", "MemMem:DEBUG:java memory: allc: " + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "MB sum=" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)");

			// calcAvailableMemory();

			String mem_type = "NATIVE";
			try
			{
				if (android.os.Build.VERSION.SDK_INT >= 11)
				{
					mem_type = "JAVA";
				}
			}
			catch (Exception e)
			{
			}
			// return ("" + df.format(allocated) + "/" + df.format(sum_size) + "(" + df.format(free) + ")" + ":" + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "/" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "(" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + ") " + mem_type);
			return ("==MEM==:" + "J:" + (Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + ":" + (Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + ",N:" + allocated + ":" + sum_size);
		}
		catch (Exception e2)
		{
			return ("==MEM==:ERROR");
		}
	}

	private static long calcAvailableMemory()
	{
		try
		{
			long value = Runtime.getRuntime().maxMemory();
			String type = "";
			if (android.os.Build.VERSION.SDK_INT >= 11)
			{
				value = (value / 1024 / 1024) - (Runtime.getRuntime().totalMemory() / 1024 / 1024);
				type = "JAVA";
			}
			else
			{
				value = (value / 1024 / 1024) - (Debug.getNativeHeapAllocatedSize() / 1024 / 1024);
				type = "NATIVE";
			}
			Log.i("Navit", "avail.mem size=" + value + "MB, type=" + type);
			return value;
		}
		catch (Exception e)
		{
			return 0L;
		}
	}

	public void google_online_search_and_set_destination(String address_string)
	{
		// online googlemaps search
		// String addressInput = filter_bad_chars(address_string);
		String addressInput = address_string;
		try
		{
			List<Address> foundAdresses = Navit.Navit_Geocoder.getFromLocationName(addressInput, 1); //Search addresses
			//System.out.println("found " + foundAdresses.size() + " results");
			//System.out.println("addr=" + foundAdresses.get(0).getLatitude() + " " + foundAdresses.get(0).getLongitude() + "" + foundAdresses.get(0).getAddressLine(0));

			int results_step = 0;
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

			//			// DEBUG: clear route rectangle list
			//			NavitGraphics.route_rects.clear();
			//
			//			System.out.println("found:" + tmp_addr.addr + " " + tmp_addr.lat + " " + tmp_addr.lon);
			//
			//			try
			//			{
			//				Navit.remember_destination(tmp_addr.addr, tmp_addr.lat, tmp_addr.lon);
			//				// save points
			//				write_map_points();
			//			}
			//			catch (Exception e)
			//			{
			//				e.printStackTrace();
			//			}
			//
			//			if (NavitGraphics.navit_route_status == 0)
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("setting destination to") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Navit.destination_set();
			//
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 3);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}
			//			else
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("new Waypoint") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 48);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}

			route_wrapper(tmp_addr.addr, 0, 0, false, tmp_addr.lat, tmp_addr.lon, true);

			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			if (p.PREF_enable_debug_write_gpx)
			{
				write_route_to_gpx_file();
			}
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------

			try
			{
				Navit.follow_button_on();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}

			show_geo_on_screen(tmp_addr.lat, tmp_addr.lon);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), Navit.get_text("google search API is not working at this moment, try offline search"), Toast.LENGTH_SHORT).show();
		}
	}

	public void result_set_destination(double lat, double lon, String addr)
	{
		try
		{
			int results_step = 0;
			Navit.Navit_Address_Result_Struct tmp_addr = new Navit_Address_Result_Struct();
			tmp_addr.result_type = "STR";
			tmp_addr.item_id = "0";
			tmp_addr.lat = (float) lat;
			tmp_addr.lon = (float) lon;
			tmp_addr.addr = addr;

			//			// DEBUG: clear route rectangle list
			//			NavitGraphics.route_rects.clear();
			//
			//			System.out.println("found:" + tmp_addr.addr + " " + tmp_addr.lat + " " + tmp_addr.lon);
			//
			//			try
			//			{
			//				Navit.remember_destination(tmp_addr.addr, tmp_addr.lat, tmp_addr.lon);
			//				// save points
			//				write_map_points();
			//			}
			//			catch (Exception e)
			//			{
			//				e.printStackTrace();
			//			}
			//
			//			if (NavitGraphics.navit_route_status == 0)
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("setting destination to") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Navit.destination_set();
			//
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 3);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}
			//			else
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("new Waypoint") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 48);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}

			route_wrapper(tmp_addr.addr, 0, 0, false, tmp_addr.lat, tmp_addr.lon, true);

			try
			{
				Navit.follow_button_on();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}

			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			if (p.PREF_enable_debug_write_gpx)
			{
				write_route_to_gpx_file();
			}
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------

			show_geo_on_screen(tmp_addr.lat, tmp_addr.lon);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), Navit.get_text("google search API is not working at this moment, try offline search"), Toast.LENGTH_SHORT).show();
		}
	}

	void open_voice_recog_screen()
	{
		Intent ii = new Intent(this, ZANaviVoiceInput.class);
		this.startActivityForResult(ii, Navit.ZANaviVoiceInput_id);
	}

	static void dim_screen_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 20;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void show_status_bar_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 27;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void hide_status_bar_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 28;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void default_brightness_screen_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 21;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void dim_screen()
	{
		try
		{
			WindowManager.LayoutParams params_wm = app_window.getAttributes();
			// params_wm.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			params_wm.screenBrightness = 0.1f;
			app_window.setAttributes(params_wm);
			//app_window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void default_brightness_screen()
	{
		try
		{
			WindowManager.LayoutParams params_wm = app_window.getAttributes();
			// params_wm.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			params_wm.screenBrightness = -1f;
			app_window.setAttributes(params_wm);
			//app_window.clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	void donate_bitcoins()
	{
		try
		{
			Intent donate_activity = new Intent(this, ZANaviDonateActivity.class);
			// donate_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
			startActivity(donate_activity);
		}
		catch (Exception e)
		{
		}
	}

	void donate()
	{
		try
		{
			Intent donate_activity = new Intent(this, ZANaviNormalDonateActivity.class);
			// donate_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
			startActivity(donate_activity);
		}
		catch (Exception e)
		{
		}
	}

	static int debug_indent = -1;
	static String debug_indent_spaces = "                                                                                                                                                                                     ";

	// type: 0 -> enter, 1 -> leave, 2 .. n -> return(#n)
	static void my_func_name(int type)
	{
		int debug_indent2;

		// -- switch off !! --
		// -- switch off !! --
		debug_indent = 0;
		// -- switch off !! --
		// -- switch off !! --

		try
		{
			StackTraceElement[] a = Thread.currentThread().getStackTrace();
			if (type == 0)
			{
				//debug_indent++;
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":enter");
			}
			else if (type == 1)
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":leave");
				//debug_indent--;
			}
			else
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":return(" + type + ")");
				//debug_indent--;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// type: 0 -> enter, 1 -> leave, 2 .. n -> return(#n)
	static void my_func_name(int type, String msg)
	{
		int debug_indent2;

		// -- switch off !! --
		// -- switch off !! --
		debug_indent = 0;
		// -- switch off !! --
		// -- switch off !! --

		try
		{
			StackTraceElement[] a = Thread.currentThread().getStackTrace();
			if (type == 0)
			{
				//debug_indent++;
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":enter" + ":" + msg);
			}
			else if (type == 1)
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":leave" + ":" + msg);
				//debug_indent--;
			}
			else
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":return(" + type + ")" + ":" + msg);
				//debug_indent--;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void write_route_to_gpx_file()
	{
		final Thread write_route_to_gpx_file_001 = new Thread()
		{
			int wait = 1;
			int count = 0;
			int max_count = 300; // wait 2 minutes for route to be calculated

			@Override
			public void run()
			{
				while (wait == 1)
				{
					try
					{
						if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33))
						{
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 96);
							String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
							String filename = Navit.NAVIT_DATA_DEBUG_DIR + "zanavi_route_" + date + ".gpx";
							b.putString("s", filename);
							System.out.println("Debug:" + "file=" + filename);
							msg.setData(b);
							NavitGraphics.callback_handler.sendMessage(msg);

							Message msg7 = Navit_progress_h.obtainMessage();
							Bundle b7 = new Bundle();
							msg7.what = 2; // long Toast message
							b7.putString("text", Navit.get_text("saving route to GPX-file") + " " + filename); //TRANS
							msg7.setData(b7);
							Navit_progress_h.sendMessage(msg7);

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

							Message msg7 = Navit_progress_h.obtainMessage();
							Bundle b7 = new Bundle();
							msg7.what = 2; // long Toast message
							b7.putString("text", Navit.get_text("saving route to GPX-file failed")); //TRANS
							msg7.setData(b7);
							Navit_progress_h.sendMessage(msg7);
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
		write_route_to_gpx_file_001.start();
	}

	void convert_gpx_file_real(String gpx_file)
	{
		File tt2 = new File(gpx_file);
		p.PREF_last_selected_dir_gpxfiles = tt2.getParent();
		Log.e("Navit", "last_selected_dir_gpxfiles " + p.PREF_last_selected_dir_gpxfiles);
		setPrefs_selected_gpx_dir();

		String out_ = MAP_FILENAME_PATH + "/gpxtracks.txt";
		Log.e("Navit", "onActivityResult 002 " + gpx_file + " " + out_);
		MainFrame.do_conversion(gpx_file, out_);

		// draw map no-async
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", 64);
		msg.setData(b);
		NavitGraphics.callback_handler.sendMessage(msg);
	}

	String intent_flags_to_string(int flags)
	{
		String ret = "(" + String.format("%#x", flags) + ") ";

		// Intent flags
		final int FLAG_GRANT_READ_URI_PERMISSION = 0x00000001;
		final int FLAG_GRANT_WRITE_URI_PERMISSION = 0x00000002;
		final int FLAG_FROM_BACKGROUND = 0x00000004;
		final int FLAG_DEBUG_LOG_RESOLUTION = 0x00000008;
		final int FLAG_EXCLUDE_STOPPED_PACKAGES = 0x00000010;
		final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x00000020;
		final int FLAG_ACTIVITY_NO_HISTORY = 0x40000000;
		final int FLAG_ACTIVITY_SINGLE_TOP = 0x20000000;
		final int FLAG_ACTIVITY_NEW_TASK = 0x10000000;
		final int FLAG_ACTIVITY_MULTIPLE_TASK = 0x08000000;
		final int FLAG_ACTIVITY_CLEAR_TOP = 0x04000000;
		final int FLAG_ACTIVITY_FORWARD_RESULT = 0x02000000;
		final int FLAG_ACTIVITY_PREVIOUS_IS_TOP = 0x01000000;
		final int FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS = 0x00800000;
		final int FLAG_ACTIVITY_BROUGHT_TO_FRONT = 0x00400000;
		final int FLAG_ACTIVITY_RESET_TASK_IF_NEEDED = 0x00200000;
		final int FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY = 0x00100000;
		final int FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET = 0x00080000;
		final int FLAG_ACTIVITY_NO_USER_ACTION = 0x00040000;
		final int FLAG_ACTIVITY_REORDER_TO_FRONT = 0X00020000;
		final int FLAG_ACTIVITY_NO_ANIMATION = 0X00010000;
		final int FLAG_ACTIVITY_CLEAR_TASK = 0X00008000;
		final int FLAG_ACTIVITY_TASK_ON_HOME = 0X00004000;
		/*
		 * final int FLAG_RECEIVER_REGISTERED_ONLY = 0x40000000;
		 * final int FLAG_RECEIVER_REPLACE_PENDING = 0x20000000;
		 * final int FLAG_RECEIVER_FOREGROUND = 0x10000000;
		 * final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 0x08000000;
		 * final int FLAG_RECEIVER_BOOT_UPGRADE = 0x04000000;
		 */

		int first = 1;
		String sep = "";

		if ((flags & FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) == FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS";
		}
		if ((flags & FLAG_ACTIVITY_BROUGHT_TO_FRONT) == FLAG_ACTIVITY_BROUGHT_TO_FRONT)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_BROUGHT_TO_FRONT";
		}
		if ((flags & FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) == FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED";
		}
		if ((flags & FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY";
		}
		if ((flags & FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) == FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET";
		}
		if ((flags & FLAG_ACTIVITY_NO_USER_ACTION) == FLAG_ACTIVITY_NO_USER_ACTION)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_NO_USER_ACTION";
		}
		if ((flags & FLAG_ACTIVITY_REORDER_TO_FRONT) == FLAG_ACTIVITY_REORDER_TO_FRONT)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_REORDER_TO_FRONT";
		}
		if ((flags & FLAG_ACTIVITY_NO_ANIMATION) == FLAG_ACTIVITY_NO_ANIMATION)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_NO_ANIMATION";
		}
		if ((flags & FLAG_ACTIVITY_CLEAR_TASK) == FLAG_ACTIVITY_CLEAR_TASK)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_CLEAR_TASK";
		}
		if ((flags & FLAG_ACTIVITY_TASK_ON_HOME) == FLAG_ACTIVITY_TASK_ON_HOME)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_TASK_ON_HOME";
		}

		if ((flags & FLAG_GRANT_READ_URI_PERMISSION) == FLAG_GRANT_READ_URI_PERMISSION)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_GRANT_READ_URI_PERMISSION";
		}

		if ((flags & FLAG_GRANT_WRITE_URI_PERMISSION) == FLAG_GRANT_WRITE_URI_PERMISSION)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_GRANT_WRITE_URI_PERMISSION";
		}

		if ((flags & FLAG_FROM_BACKGROUND) == FLAG_FROM_BACKGROUND)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_FROM_BACKGROUND";
		}
		if ((flags & FLAG_DEBUG_LOG_RESOLUTION) == FLAG_DEBUG_LOG_RESOLUTION)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_DEBUG_LOG_RESOLUTION";
		}
		if ((flags & FLAG_EXCLUDE_STOPPED_PACKAGES) == FLAG_EXCLUDE_STOPPED_PACKAGES)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_EXCLUDE_STOPPED_PACKAGES";
		}
		if ((flags & FLAG_INCLUDE_STOPPED_PACKAGES) == FLAG_INCLUDE_STOPPED_PACKAGES)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_INCLUDE_STOPPED_PACKAGES";
		}
		if ((flags & FLAG_ACTIVITY_NO_HISTORY) == FLAG_ACTIVITY_NO_HISTORY)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_NO_HISTORY";
		}
		if ((flags & FLAG_ACTIVITY_SINGLE_TOP) == FLAG_ACTIVITY_SINGLE_TOP)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_SINGLE_TOP";
		}
		if ((flags & FLAG_ACTIVITY_NEW_TASK) == FLAG_ACTIVITY_NEW_TASK)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_NEW_TASK";
		}
		if ((flags & FLAG_ACTIVITY_MULTIPLE_TASK) == FLAG_ACTIVITY_MULTIPLE_TASK)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_MULTIPLE_TASK";
		}
		if ((flags & FLAG_ACTIVITY_CLEAR_TOP) == FLAG_ACTIVITY_CLEAR_TOP)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_CLEAR_TOP";
		}
		if ((flags & FLAG_ACTIVITY_FORWARD_RESULT) == FLAG_ACTIVITY_FORWARD_RESULT)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_FORWARD_RESULT";
		}
		if ((flags & FLAG_ACTIVITY_PREVIOUS_IS_TOP) == FLAG_ACTIVITY_PREVIOUS_IS_TOP)
		{
			if (first == 1)
			{
				first = 0;
				sep = "";
			}
			else
			{
				sep = ",";
			}
			ret = ret + sep + "FLAG_ACTIVITY_PREVIOUS_IS_TOP";
		}

		return ret;
	}

	static boolean want_tunnel_extrapolation()
	{
		if ((!isGPSFix) && (pos_is_underground == 1) && (NavitGraphics.CallbackDestinationValid2() > 0))
		{
			// gps fix is lost
			//	and
			// our position is underground
			//	and
			// we have a destination set
			//
			// --> we want tunnel extrapolation
			return true;
		}

		return false;
	}

	public static void applySharedTheme(Activity act, int Theme_id)
	{
		act.setTheme(Theme_id);
	}

	private com.zoffcc.applications.zanavi_msg.ZListener.Stub zclientListener = new com.zoffcc.applications.zanavi_msg.ZListener.Stub()
	{
		@Override
		public String handleUpdated(String data) throws RemoteException
		{
			// Log.i("NavitPlugin", "update from Plugin=" + data);
			return "Navit says:\"PONG\"";
		}
	};

	static Object sync_plugin_send = new Object();

	public static void send_data_to_plugin_bg(final int msg_cat, final String data)
	{
		// send data to plugin (plugin will send to server) in another task! --------------------------
		new AsyncTask<Void, Void, String>()
		{
			@Override
			protected String doInBackground(Void... params)
			{
				synchronized (sync_plugin_send)
				{
					try
					{
						if (plugin_api == null)
						{
							try
							{
								Thread.sleep(3000); // wait until the service is bound
							}
							catch (Exception e)
							{
							}
						}

						String response = plugin_api.getResult(PLUGIN_MSG_ID, msg_cat, data);

					}
					catch (RemoteException e)
					{
						// Log.e("NavitPlugin", "Failed(1) to send msg to plugin:cat=" + PLUGIN_MSG_CAT_zanavi_version + " data=" + data, e);
					}
					catch (Exception e)
					{
						// Log.e("NavitPlugin", "Failed(2) to send msg to plugin:cat=" + PLUGIN_MSG_CAT_zanavi_version + " data=" + data, e);
					}
				}
				return "";
			}

			@Override
			protected void onPostExecute(String msg)
			{

			}
		}.execute(null, null, null);
		// send data to plugin (plugin will send to server) in another task! --------------------------
	}

	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.i("NavitPlugin", "Service connection established");

			// that's how we get the client side of the IPC connection
			plugin_api = ZanaviCloudApi.Stub.asInterface((IBinder) service);
			try
			{
				plugin_api.addListener(zclientListener);
			}
			catch (RemoteException e)
			{
				Log.e("NavitPlugin", "Failed(1) to add listener", e);
			}
			catch (Exception e)
			{
				Log.e("NavitPlugin", "Failed(2) to add listener", e);
			}

			send_installed_maps_to_plugin();
			send_data_to_plugin_bg(PLUGIN_MSG_CAT_zanavi_version, Navit.NavitAppVersion);
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			plugin_api = null;
			Log.i("NavitPlugin", "Service connection closed");
		}
	};

	/**
	 * send installed maps and download date/time (time up to minute) in UTC to plugin
	 */
	static void send_installed_maps_to_plugin()
	{
		try
		{
			/*
			 * { "201502032311":"austria.bin","201502032312":"germany.bin" }
			 */

			String data = "";

			// dirty hardcode JSON data struct
			final String maps_and_timestamp_data_start = "{ \"";
			final String maps_and_timestamp_data_sep1 = "\":\"";
			final String maps_and_timestamp_data_sep2 = "\",\"";
			final String maps_and_timestamp_data_end = "\" }";

			NavitMapDownloader.init_cat_file_maps_timestamps();

			data = data + maps_and_timestamp_data_start;

			String map_name = "";
			Iterator<String> k = NavitMapDownloader.map_catalogue_date.listIterator();
			while (k.hasNext())
			{
				map_name = k.next();
				data = data + map_name.split(":", 2)[1];
				data = data + maps_and_timestamp_data_sep1;
				data = data + map_name.split(":", 2)[0];
				if (k.hasNext())
				{
					data = data + maps_and_timestamp_data_sep2;
				}
			}
			data = data + maps_and_timestamp_data_end;

			// System.out.println("PLUGIN:MAPS:" + data);

			send_data_to_plugin_bg(PLUGIN_MSG_CAT_installed_maps, data);
		}
		catch (Exception e)
		{
		}
	}

	static boolean have_maps_installed()
	{
		int count_maps_installed = NavitMapDownloader.cat_file_maps_have_installed_any();
		if (count_maps_installed == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * auto start map download/update
	 * 
	 * @param map_name
	 *            mapfilename with ending ".bin"
	 */
	void auto_start_update_map(String map_name)
	{

		int count = NavitMapDownloader.z_OSM_MAPS.length;
		Navit.download_map_id = -1;
		for (int i = 0; i < count; i++)
		{
			if (!NavitMapDownloader.z_OSM_MAPS[i].is_continent)
			{
				// System.out.println("mmm1=" + NavitMapDownloader.z_OSM_MAPS[i].url);
				if (NavitMapDownloader.z_OSM_MAPS[i].url.equals(map_name))
				{
					Navit.download_map_id = i;
					System.out.println("mMM2=" + NavitMapDownloader.z_OSM_MAPS[i].map_name);
				}
			}
		}

		// show the map download progressbar, and download the map
		if (Navit.download_map_id > -1)
		{
			// new method in service
			Message msg = progress_handler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 22;
			progress_handler.sendMessage(msg);
		}
	}

	void show_case_001()
	{
		//		try
		//		{
		//			new ShowcaseView.Builder(this).setTarget(new ViewTarget(R.id.no_maps_button, this)).setContentTitle("").setContentText("").singleShot(Navit.SHOWCASEVIEW_ID_001).build();
		//		}
		//		catch (Exception e1)
		//		{
		//		}
		//		catch (NoSuchMethodError e2)
		//		{
		//		}
		//		catch (NoClassDefFoundError e3)
		//		{
		//		}
	}

	static void set_zoom_level(int want_zoom_level)
	{
		try
		{
			Bundle b = new Bundle();
			Message msg = new Message();
			b.putInt("Callback", 33);
			b.putString("s", Integer.toString(want_zoom_level));
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void set_zoom_level_no_draw(int want_zoom_level)
	{
		try
		{
			Bundle b = new Bundle();
			Message msg = new Message();
			b.putInt("Callback", 105);
			b.putString("s", Integer.toString(want_zoom_level));
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void draw_map()
	{
		try
		{
			// draw map no-async
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 64);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	final static int bottom_bar_snap_duration = 190;

	void animate_bottom_bar_up()
	{
		final FrameLayout a = (FrameLayout) findViewById(R.id.bottom_bar_slide);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -cur_y_margin_bottom_bar_touch);
		animation.setDuration(bottom_bar_snap_duration); // animation duration
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setRepeatCount(0); // animation repeat count
		animation.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				cur_y_margin_bottom_bar_touch = 0;
				RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) a.getLayoutParams();
				relativeParams.setMargins(0, (int) cur_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
				a.setLayoutParams(relativeParams);
				a.requestLayout();

				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				anim.setDuration(1);
				a.startAnimation(anim);
			}
		});
		a.startAnimation(animation);
	}

	static void animate_bottom_bar_down()
	{
		final FrameLayout a = (FrameLayout) Global_Navit_Object.findViewById(R.id.bottom_bar_slide);

		// System.out.println("FRAG:animate_bottom_bar_down:014");

		// set bottom end positon correctly??
		bottom_y_margin_bottom_bar_touch = Navit.map_view_height + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px;

		final int move_by = (int) (bottom_y_margin_bottom_bar_touch - cur_y_margin_bottom_bar_touch);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, move_by); //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
		animation.setDuration(bottom_bar_snap_duration); // animation duration
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setRepeatCount(0); // animation repeat count
		animation.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				// set bottom end positon correctly??
				bottom_y_margin_bottom_bar_touch = Navit.map_view_height + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px;

				cur_y_margin_bottom_bar_touch = bottom_y_margin_bottom_bar_touch;
				RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) a.getLayoutParams();
				relativeParams.setMargins(0, (int) bottom_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
				a.setLayoutParams(relativeParams);
				a.requestLayout();

				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				anim.setDuration(1);
				a.startAnimation(anim);

				// remove roadbook fragment -----------
				try
				{
					if (road_book != null)
					{
						FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
						// System.out.println("FRAG:dettach:002");
						fragmentTransaction.detach(road_book);
						fragmentTransaction.remove(road_book).commit();
						road_book = null;
					}
				}
				catch (Exception ef)
				{
				}
				// remove roadbook fragment -----------

			}
		});
		a.startAnimation(animation);
	}

	synchronized static int find_max_font_size_for_height(String sample_text, int height, int max_font_size, int padding_in_dp)
	{
		String s = sample_text;
		int bh = 0;
		Paint p = new Paint();
		Rect bounds = new Rect();
		p.setTextSize(max_font_size);
		// p.measureText(s);
		p.getTextBounds(s, 0, s.length(), bounds);

		int ret_font_size = max_font_size;

		int loop_counter_max = 400;
		int loop_counter = 0;
		int padding_in_px = 0;
		if (padding_in_dp > 0)
		{
			padding_in_px = NavitGraphics.dp_to_px(padding_in_dp);
		}

		bh = bounds.height();
		//System.out.println("bh(1)=" + bh);
		while ((bh + padding_in_px) > height)
		{
			loop_counter++;
			if (loop_counter > loop_counter_max)
			{
				break;
			}

			ret_font_size--;
			p.setTextSize(ret_font_size);
			// p.measureText(s);
			p.getTextBounds(s, 0, s.length(), bounds);
			bh = bounds.height();
		}

		return ret_font_size;
	}

	synchronized static int find_max_letters_for_width_and_fontsize(String max_length_text, int width, int max_font_size, int padding_in_dp)
	{
		int ret_max_letters = 10;

		int padding_in_px = 0;
		if (padding_in_dp > 0)
		{
			//System.out.println("aaa2:l:1:x:" + padding_in_dp);
			padding_in_px = NavitGraphics.dp_to_px(padding_in_dp);
			//System.out.println("aaa2:l:1:x:2=" + padding_in_dp + " " + padding_in_px + " " + NavitGraphics.dp_to_px(padding_in_dp) + " " + NavitGraphics.Global_dpi_factor_better);
		}
		//System.out.println("aaa2:l:1:x:1=" + padding_in_px);

		int c = 0;
		Paint p = new Paint();
		p.setTextSize(max_font_size);

		String s = max_length_text;

		if ((s == null) || (s.equals("")))
		{
			s = "Mlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwo";
		}

		int l = s.length();
		ret_max_letters = l;
		float mt = p.measureText(s);
		//System.out.println("aaa2:l:1:" + l + " " + s);
		//System.out.println("aaa2:l:22:." + mt + " padding_in_px=" + padding_in_px + " " + width);

		while (mt + padding_in_px > width)
		{
			//System.out.println("aaa2:l:22:+" + mt + " " + padding_in_px + " " + width);

			c++;
			if (c == l)
			{
				// no more letters
				//System.out.println("aaa2:l:1:no more letters:" + l + " " + s);
				break;
			}
			s = s.substring(0, s.length() - 1);
			mt = p.measureText(s);
			ret_max_letters--;
		}

		//System.out.println("aaa2:l:1:res=" + ret_max_letters);
		return ret_max_letters;
	}

	synchronized static int find_max_font_size_for_width(String sample_text, int width, int max_font_size, int padding_in_dp)
	{
		final String s = sample_text;
		int bh = 0;
		Paint p = new Paint();
		Rect bounds = new Rect();
		p.setTextSize(max_font_size);
		p.getTextBounds(s, 0, s.length(), bounds);

		int ret_font_size = max_font_size;

		int loop_counter_max = 400;
		int loop_counter = 0;
		int padding_in_px = 0;
		if (padding_in_dp > 0)
		{
			padding_in_px = NavitGraphics.dp_to_px(padding_in_dp);
		}

		bh = bounds.width();
		while ((bh + padding_in_px) > width)
		{
			loop_counter++;
			if (loop_counter > loop_counter_max)
			{
				break;
			}

			ret_font_size--;
			p.setTextSize(ret_font_size);
			p.getTextBounds(s, 0, s.length(), bounds);
			bh = bounds.width();
			// mt = p.measureText(s);
		}

		return ret_font_size;
	}

	static int last_orientation = Configuration.ORIENTATION_LANDSCAPE;

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		if (last_orientation != newConfig.orientation)
		{
			// Checks the orientation of the screen
			if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
			{
				// setContentView(R.layout.main_layout);

				// -- bottom bar --
				int h = NavitGraphics.mCanvasHeight;
				try
				{
					int h001;
					android.view.ViewGroup.LayoutParams lp001;

					View v003 = (View) findViewById(R.id.osd_nextturn_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_nextturn_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v003 = (View) findViewById(R.id.bottom_bar);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					LinearLayout v002 = (LinearLayout) findViewById(R.id.bottom_slide_view);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_slide_view_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					v003 = (View) findViewById(R.id.osd_timetodest_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_timetodest_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v002 = (LinearLayout) findViewById(R.id.bottom_line_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_line_container_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					RelativeLayout v001 = (RelativeLayout) findViewById(R.id.gui_top_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.gui_top_container_height);
					lp001 = v001.getLayoutParams();
					lp001.height = h001;
					v001.requestLayout();

					int ml = getResources().getDimensionPixelSize(R.dimen.margin_left_speeding);
					int mb = getResources().getDimensionPixelSize(R.dimen.margin_bottom_speeding);
					v003 = (View) findViewById(R.id.view_speeding);
					RelativeLayout.LayoutParams relativeParams_001 = (RelativeLayout.LayoutParams) v003.getLayoutParams();
					relativeParams_001.setMargins(ml, 0, 0, mb); // left, top, right, bottom
					v003.setLayoutParams(relativeParams_001);
					v003.requestLayout();

					// Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();

					bottom_bar_px = (int) getResources().getDimension(R.dimen.gui_top_container_height);
					bottom_bar_slider_shadow_px = (int) getResources().getDimension(R.dimen.bottom_slide_view_shadow_compat_height);

					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				catch (Exception e)
				{
					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				Navit.bottom_y_margin_bottom_bar_touch = Navit.cur_y_margin_bottom_bar_touch;
				// -- bottom bar --
			}
			else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				// setContentView(R.layout.main_layout);

				// -- bottom bar --
				int h = NavitGraphics.mCanvasHeight;
				try
				{
					int h001;
					android.view.ViewGroup.LayoutParams lp001;

					View v003 = (View) findViewById(R.id.osd_nextturn_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_nextturn_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v003 = (View) findViewById(R.id.bottom_bar);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					LinearLayout v002 = (LinearLayout) findViewById(R.id.bottom_slide_view);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_slide_view_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					v003 = (View) findViewById(R.id.osd_timetodest_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_timetodest_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v002 = (LinearLayout) findViewById(R.id.bottom_line_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_line_container_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					RelativeLayout v001 = (RelativeLayout) findViewById(R.id.gui_top_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.gui_top_container_height);
					lp001 = v001.getLayoutParams();
					lp001.height = h001;
					v001.requestLayout();

					int ml = getResources().getDimensionPixelSize(R.dimen.margin_left_speeding);
					int mb = getResources().getDimensionPixelSize(R.dimen.margin_bottom_speeding);
					v003 = (View) findViewById(R.id.view_speeding);
					RelativeLayout.LayoutParams relativeParams_001 = (RelativeLayout.LayoutParams) v003.getLayoutParams();
					relativeParams_001.setMargins(ml, 0, 0, mb); // left, top, right, bottom
					v003.setLayoutParams(relativeParams_001);
					v003.requestLayout();

					// Toast.makeText(this, "protrait", Toast.LENGTH_SHORT).show();

					bottom_bar_px = (int) getResources().getDimension(R.dimen.gui_top_container_height);
					bottom_bar_slider_shadow_px = (int) getResources().getDimension(R.dimen.bottom_slide_view_shadow_compat_height);

					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				catch (Exception e)
				{
					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				Navit.bottom_y_margin_bottom_bar_touch = Navit.cur_y_margin_bottom_bar_touch;
				// -- bottom bar --
			}

			last_orientation = newConfig.orientation;
		}
	}

	static void long_toast(String msg)
	{
		try
		{
			Message msg7 = Navit_progress_h.obtainMessage();
			Bundle b7 = new Bundle();
			msg7.what = 2; // long Toast message
			b7.putString("text", msg);
			msg7.setData(b7);
			Navit_progress_h.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}
	}

	static void short_toast(String msg)
	{
		try
		{
			Message msg7 = Navit_progress_h.obtainMessage();
			Bundle b7 = new Bundle();
			msg7.what = 3; // short Toast message
			b7.putString("text", msg);
			msg7.setData(b7);
			Navit_progress_h.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}
	}

	public static void route_wrapper(String addr, float lat_start, float lon_start, boolean start_coords_valid, double lat_end, double lon_end, boolean remember_dest)
	{
		global_last_destination_name = NavitSpeech2.filter_out_special_chars_for_dest_string(addr);
		// System.out.println("HOME002:" + addr + " = " + global_last_destination_name);

		if (p.PREF_routing_engine == 1)
		{
			route_online_OSRM(addr, lat_start, lon_start, start_coords_valid, lat_end, lon_end, remember_dest);
		}
		else if (p.PREF_routing_engine == 0)
		{
			route_offline_ZANavi(addr, lat_start, lon_start, start_coords_valid, lat_end, lon_end, remember_dest);
		}
	}

	public static void route_offline_ZANavi(String addr, float lat_start, float lon_start, boolean start_coords_valid, double lat_end, double lon_end, boolean remember_dest)
	{
		if (remember_dest)
		{
			try
			{
				Navit.remember_destination(addr, "" + lat_end, "" + lon_end);
				// save points
				write_map_points();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// DEBUG: clear route rectangle list
		NavitGraphics.route_rects.clear();

		if (NavitGraphics.navit_route_status == 0)
		{
			short_toast(Navit.get_text("setting destination to") + "\n" + addr);

			Navit.destination_set();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 3);
			b.putString("lat", "" + lat_end);
			b.putString("lon", "" + lon_end);
			b.putString("q", addr);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		else
		{
			short_toast(Navit.get_text("new Waypoint") + "\n" + addr);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 48);
			b.putString("lat", "" + lat_end);
			b.putString("lon", "" + lon_end);
			b.putString("q", addr);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}

	}

	public static void route_online_OSRM(final String addr, float lat_start, float lon_start, boolean start_coords_valid, final double lat_end, final double lon_end, final boolean remember_dest)
	{
		// http://router.project-osrm.org/viaroute?loc=46.3456438,17.450&loc=47.34122,17.5332&instructions=false&alt=false

		if (!start_coords_valid)
		{
			location_coords cur_target = new location_coords();
			try
			{
				geo_coord tmp = get_current_vehicle_position();
				cur_target.lat = tmp.Latitude;
				cur_target.lon = tmp.Longitude;
			}
			catch (Exception e)
			{
			}

			try
			{
				lat_start = (float) cur_target.lat;
				lon_start = (float) cur_target.lon;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.e("Navit", "problem with location!");
			}
		}

		final String request_url = String.format(Locale.US, "http://router.project-osrm.org/viaroute?loc=%4.6f,%4.6f&loc=%4.6f,%4.6f&instructions=true&alt=false", lat_start, lon_start, lat_end, lon_end);

		// StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		// StrictMode.setThreadPolicy(policy);

		try
		{
			// System.out.println("XML:S:001 url=" + request_url);
			final URL url = new URL(request_url);
			// System.out.println("XML:S:002");
			//			SAXParserFactory factory = SAXParserFactory.newInstance();
			//			System.out.println("XML:S:003");
			//			SAXParser parser = factory.newSAXParser();
			//			System.out.println("XML:S:004");
			//			XMLReader xmlreader = parser.getXMLReader();
			//			System.out.println("XML:S:005");
			//			xmlreader.setContentHandler(new ZANaviXMLHandler());
			//			System.out.println("XML:S:006");

			final Thread add_to_route = new Thread()
			{
				@Override
				public void run()
				{
					try
					{

						// --------------
						// --------------
						// --------------
						// ------- allow this HTTPS cert ---
						// --------------
						// --------------
						// --------------
						//						X509HostnameVerifier hnv = new X509HostnameVerifier()
						//						{
						//
						//							@Override
						//							public void verify(String hostname, SSLSocket arg1) throws IOException
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//							}
						//
						//							@Override
						//							public void verify(String hostname, X509Certificate cert) throws SSLException
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//							}
						//
						//							@Override
						//							public void verify(String hostname, String[] cns, String[] subjectAlts) throws SSLException
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//							}
						//
						//							@Override
						//							public boolean verify(String hostname, SSLSession session)
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//								return true;
						//							}
						//						};
						//
						//						SSLContext context = SSLContext.getInstance("TLS");
						//						context.init(null, new X509TrustManager[] { new X509TrustManager()
						//						{
						//							public java.security.cert.X509Certificate[] getAcceptedIssuers()
						//							{
						//								return new java.security.cert.X509Certificate[0];
						//							}
						//
						//							@Override
						//							public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
						//							{
						//							}
						//
						//							@Override
						//							public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
						//							{
						//							}
						//						} }, new SecureRandom());
						//						javax.net.ssl.SSLSocketFactory sslf = context.getSocketFactory();
						//
						//						HostnameVerifier hnv_default = HttpsURLConnection.getDefaultHostnameVerifier();
						//						javax.net.ssl.SSLSocketFactory sslf_default = HttpsURLConnection.getDefaultSSLSocketFactory();
						//						HttpsURLConnection.setDefaultHostnameVerifier(hnv);
						//						HttpsURLConnection.setDefaultSSLSocketFactory(sslf);
						//
						//						DefaultHttpClient client = new DefaultHttpClient();
						//
						//						SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
						//						SchemeRegistry registry = new SchemeRegistry();
						//						registry.register(new Scheme("https", socketFactory, 443));
						//						ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(client.getParams(), registry);
						//						DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
						//
						//						socketFactory.setHostnameVerifier(hnv);
						//
						//						HttpGet get_request = new HttpGet(request_url);
						//						HttpResponse http_response = httpClient.execute(get_request);
						//						HttpEntity responseEntity = http_response.getEntity();
						//
						//						HttpsURLConnection.setDefaultHostnameVerifier(hnv_default);
						//						HttpsURLConnection.setDefaultSSLSocketFactory(sslf_default);
						// --------------
						// --------------
						// --------------
						// ------- allow this HTTPS cert ---
						// --------------
						// --------------
						// --------------

						InputSource is = new InputSource();
						is.setEncoding("utf-8");
						// is.setByteStream(responseEntity.getContent());
						is.setByteStream(url.openStream());
						// System.out.println("XML:S:007");

						String response = slurp(is.getByteStream(), 16384);
						// response = response.replaceAll("&", "&amp;");

						// System.out.println("XML:S:007.a res=" + response);

						final JSONObject obj = new JSONObject(response);

						//   System.out.println(person.getInt("id"));

						final String route_geometry = obj.getString("route_geometry");
						final JSONArray route_instructions_array = obj.getJSONArray("route_instructions");

						int loop_i = 0;
						JSONArray instruction;
						int[] instruction_pos = new int[route_instructions_array.length()];
						for (loop_i = 0; loop_i < route_instructions_array.length(); loop_i++)
						{
							instruction = (JSONArray) route_instructions_array.get(loop_i);
							instruction_pos[loop_i] = Integer.parseInt(instruction.get(3).toString());
							// System.out.println("XML:instr. pos=" + instruction_pos[loop_i]);
						}

						// System.out.println("XML:S:009 o=" + route_geometry);

						List<geo_coord> gc_list = decode_function(route_geometry, 6);

						if (gc_list.size() < 2)
						{
							// no real route found!! (only 1 point)
						}
						else
						{

							Message msg = new Message();
							Bundle b = new Bundle();

							int loop = 0;

							geo_coord cur = new geo_coord();
							geo_coord old = new geo_coord();
							geo_coord corr = new geo_coord();

							cur.Latitude = gc_list.get(loop).Latitude;
							cur.Longitude = gc_list.get(loop).Longitude;

							int first_found = 1;

							if (gc_list.size() > 2)
							{
								int instr_count = 1;

								for (loop = 1; loop < gc_list.size(); loop++)
								{

									old.Latitude = cur.Latitude;
									old.Longitude = cur.Longitude;
									cur.Latitude = gc_list.get(loop).Latitude;
									cur.Longitude = gc_list.get(loop).Longitude;

									if ((instruction_pos[instr_count] == loop) || (loop == (gc_list.size() - 1)))
									{

										if (loop == (gc_list.size() - 1))
										{
											corr = cur;
										}
										else
										{
											corr = get_point_on_line(old, cur, 70);
										}

										// -- add waypoint --
										//									b.putInt("Callback", 55548);
										//									b.putString("lat", "" + corr.Latitude);
										//									b.putString("lon", "" + corr.Longitude);
										//									b.putString("q", " ");
										//									msg.setData(b);
										try
										{
											// NavitGraphics.callback_handler.sendMessage(msg);
											if (first_found == 1)
											{
												first_found = 0;
												NavitGraphics.CallbackMessageChannel(55503, corr.Latitude + "#" + corr.Longitude + "#" + "");
												// System.out.println("XML:rR:" + loop + " " + corr.Latitude + " " + corr.Longitude);
											}
											else
											{
												NavitGraphics.CallbackMessageChannel(55548, corr.Latitude + "#" + corr.Longitude + "#" + "");
												// System.out.println("XML:rw:" + loop + " " + corr.Latitude + " " + corr.Longitude);
											}
											// Thread.sleep(25);
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}
										// -- add waypoint --

										instr_count++;

									}

								}
							}

							if (remember_dest)
							{
								try
								{
									Navit.remember_destination(addr, "" + lat_end, "" + lon_end);
									// save points
									write_map_points();
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}

							b.putInt("Callback", 55599);
							msg.setData(b);
							try
							{
								// System.out.println("XML:calc:");
								Thread.sleep(10);
								NavitGraphics.callback_handler.sendMessage(msg);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

						}

					}
					catch (Exception e2)
					{
						e2.printStackTrace();
					}
				}
			};
			add_to_route.start();

			// convert to coords -------------
			// convert to coords -------------

		}
		catch (Exception e)
		{
			// System.out.println("XML:S:EEE");
			e.printStackTrace();
		}
	}

	static List<geo_coord> decode_function(String encoded, double precision)
	{

		precision = Math.pow(10, -precision);
		int len = encoded.length();
		int index = 0;
		int lat = 0;
		int lng = 0;
		double lat_f;
		double lon_f;

		final List<geo_coord> latLongList = new ArrayList<geo_coord>();
		latLongList.clear();

		while (index < len)
		{

			int b;
			int shift = 0;
			int result = 0;

			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;

			}
			while (b >= 0x20);

			int dlat = 0;
			if ((result & 1) != 0)
			{
				dlat = ~(result >> 1);
			}
			else
			{
				dlat = (result >> 1);
			}

			lat += dlat;
			shift = 0;
			result = 0;

			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			}
			while (b >= 0x20);

			int dlng = 0;
			if ((result & 1) != 0)
			{
				dlng = ~(result >> 1);
			}
			else
			{
				dlng = (result >> 1);
			}

			lng += dlng;
			//array.push( {lat: lat * precision, lng: lng * precision} );
			// array.push( [lat * precision, lng * precision] );

			// System.out.println("XML:lat=" + (lat * precision) + " lon=" + (lng * precision));

			lat_f = lat * precision;
			lon_f = lng * precision;

			geo_coord gc = new geo_coord();
			gc.Latitude = lat_f;
			gc.Longitude = lon_f;
			latLongList.add(gc);
		}

		return latLongList;
	}

	public static class geo_coord
	{
		public double Latitude;
		public double Longitude;
	}

	static geo_coord get_current_vehicle_position()
	{
		geo_coord ret = new geo_coord();
		String current_target_string2 = NavitGraphics.CallbackGeoCalc(14, 1, 1);
		// System.out.println("GET CUR POS:" + current_target_string2);
		ret.Latitude = 0;
		ret.Longitude = 0;
		try
		{
			String tmp[] = current_target_string2.split(":", 2);
			ret.Latitude = Double.parseDouble(tmp[0]);
			ret.Longitude = Double.parseDouble(tmp[1]);
		}
		catch (Exception e)
		{
			// System.out.println("GET CUR POS:ERROR " + e.getMessage());
		}
		return ret;
	}

	static int[] geo_to_px(float lat, float lon)
	{
		int[] ret = new int[3];

		ret[0] = -100;
		ret[1] = -100;
		ret[2] = 0; // invalid

		try
		{
			String x_y = NavitGraphics.CallbackGeoCalc(2, lat, lon);

			if (Navit.GFX_OVERSPILL)
			{
				String tmp[] = x_y.split(":", 2);
				int x = Integer.parseInt(tmp[0]);
				int y = Integer.parseInt(tmp[1]);

				ret[0] = (int) (((float) x + (float) NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor);
				ret[1] = (int) (((float) y + (float) NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);

				ret[2] = 1; // VALID
			}
			else
			{
				String tmp[] = x_y.split(":", 2);
				int x = Integer.parseInt(tmp[0]);
				int y = Integer.parseInt(tmp[1]);

				ret[0] = (int) ((float) x * NavitGraphics.Global_dpi_factor);
				ret[1] = (int) ((float) y * NavitGraphics.Global_dpi_factor);

				ret[2] = 1; // VALID
			}

		}
		catch (Exception e)
		{
		}

		return ret;
	}

	static geo_coord px_to_geo(int x, int y)
	{
		geo_coord out = new geo_coord();
		try
		{

			String lat_lon = "";
			if (Navit.GFX_OVERSPILL)
			{
				lat_lon = NavitGraphics.CallbackGeoCalc(1, (x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
			}
			else
			{
				lat_lon = NavitGraphics.CallbackGeoCalc(1, x * NavitGraphics.Global_dpi_factor, y * NavitGraphics.Global_dpi_factor);
			}

			String tmp[] = lat_lon.split(":", 2);
			out.Latitude = Float.parseFloat(tmp[0]);
			out.Longitude = Float.parseFloat(tmp[1]);
		}
		catch (Exception e)
		{
		}

		return out;
	}

	static double get_percent_coord(double start, double end, int perecent_pos)
	{
		double out = start + (end - start) * ((float) perecent_pos / 100.0f);
		return out;
	}

	static geo_coord get_point_on_line(geo_coord start, geo_coord end, int perecent_pos)
	{
		geo_coord out = new geo_coord();

		out.Latitude = start.Latitude + (end.Latitude - start.Latitude) * ((float) perecent_pos / 100.0f);
		out.Longitude = start.Longitude + (end.Longitude - start.Longitude) * ((float) perecent_pos / 100.0f);

		return out;
	}

	public static String slurp(final InputStream is, final int bufferSize)
	{
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		Reader in = null;

		try
		{

			in = new InputStreamReader(is, "UTF-8");

			for (;;)
			{
				int rsz;
				rsz = in.read(buffer, 0, buffer.length);

				if (rsz < 0)
				{
					break;
				}
				out.append(buffer, 0, rsz);
			}

		}
		catch (IOException e)
		{
		}
		catch (Exception ex)
		{
		}

		return out.toString();
	}

	public static Handler UIHandler;
	static
	{
		UIHandler = new Handler(Looper.getMainLooper());
	}

	public static void recalculate_route()
	{
		try
		{
			// update route, if a route is set
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 73);
			msg.setData(b);
			NavitGraphics.callback_handler.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
	}

	public static void runOnUI(Runnable runnable)
	{
		UIHandler.post(runnable);
	}

	String substring_without_ioobe(String in, int start, int end)
	{
		String ret = ";:;:****no match****;:;:";

		try
		{
			ret = in.substring(start, end);
		}
		catch (Exception e)
		{
			// return dummy-no-match String
		}

		return ret;
	}

	static private Cursor c = null;
	static private Uri uri = CR_CONTENT_URI;

	static private int get_reglevel()
	{
		int ret = 0;

		try
		{
			c = null;
			Thread thread = new Thread()
			{
				public void run()
				{
					try
					{
						c = content_resolver.query(uri, null, null, null, null);
					}
					catch (Exception c1)
					{
						System.out.println("CPVD:reg(e002)=" + c1.getMessage());
					}
				}
			};
			thread.start();
			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
			}

			if (c == null)
			{
				System.out.println("CPVD:Cursor c == null.");
				System.out.println("CPVD:reg(0)=" + ret);
				return ret;
			}

			while (c.moveToNext())
			{
				String column1 = c.getString(0);
				String column2 = c.getString(1);
				String column3 = c.getString(2);

				System.out.println("CPVD:column1=" + column1 + " column2=" + column2 + " column3=" + column3);

				if (Integer.parseInt(column1) == 1)
				{
					if (column2.equals("reg"))
					{
						ret = Integer.parseInt(column3);
						System.out.println("CPVD:reg(1)=" + ret);
					}
				}
			}
			c.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("CPVD:reg(e001)=" + e.getMessage());
		}

		System.out.println("CPVD:reg(2)=" + ret);
		return ret;
	}

	static void take_map_screenshot(String dir_name, String name_base)
	{
		try
		{
			View v1 = Navit.N_NavitGraphics.view;
			v1.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			v1.setDrawingCacheEnabled(true);
			Bitmap bm = v1.getDrawingCache();

			FileOutputStream out = null;
			try
			{
				out = new FileOutputStream(dir_name + "/" + name_base + ".png");
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("TSCR:004 " + e.getMessage());
			}
			finally
			{
				v1.setDrawingCacheEnabled(false);

				try
				{
					if (out != null)
					{
						out.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e4)
		{
		}
	}

	static void take_phone_screenshot(Activity a, String dir_name, String name_base)
	{
		try
		{
			View v1 = a.getWindow().getDecorView().getRootView();
			v1.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			v1.setDrawingCacheEnabled(true);
			Bitmap bm = v1.getDrawingCache();

			FileOutputStream out = null;
			try
			{
				out = new FileOutputStream(dir_name + "/" + name_base + ".png");
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("TSCR:004 " + e.getMessage());
			}
			finally
			{
				v1.setDrawingCacheEnabled(false);

				try
				{
					if (out != null)
					{
						out.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e4)
		{
		}
	}

	static String stacktrace_to_string(Exception e)
	{
		try
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			return errors.toString();
		}
		catch (Exception e2)
		{
			try
			{
				return e.getMessage();
			}
			catch (Exception e3)
			{
				return "xxx";
			}
		}
	}

	static void static_show_route_graph(int v)
	{
		// DEBUG: toggle Routgraph on/off
		try
		{
			if (v == 1)
			{
				Navit.Routgraph_enabled = 1;

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + Navit.Routgraph_enabled);
				msg.setData(b);
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			else if (v == 0)
			{
				Navit.Routgraph_enabled = 0;

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + Navit.Routgraph_enabled);
				msg.setData(b);
				NavitGraphics.callback_handler.sendMessage(msg);
			}
			else
			{
				Navit.Routgraph_enabled = 0;

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + Navit.Routgraph_enabled);
				msg.setData(b);
				NavitGraphics.callback_handler.sendMessage(msg);

				Thread.sleep(350);

				System.out.println("static_show_route_graph:v=" + v);

				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + v);
				msg.setData(b);
				NavitGraphics.callback_handler.sendMessage(msg);
			}
		}
		catch (Exception e)
		{
		}

	}

	/*
	 * start a search with given values
	 */
	static void executeSearch_with_values(String street, String town, String hn, boolean offline, boolean index, boolean partialmatch)
	{
		Navit.use_index_search = Navit.allow_use_index_search();
		Intent search_intent = new Intent(Global_Navit_Object, NavitAddressSearchActivity.class);
		search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS

		if ((town != null) && (street != null))
		{
			if (index)
			{
				search_intent.putExtra("address_string", street + " " + town);
			}
			else
			{
				search_intent.putExtra("address_string", town + " " + street);
			}
		}
		else if (town != null)
		{
			search_intent.putExtra("address_string", town);
		}
		else if (street != null)
		{
			search_intent.putExtra("address_string", street);
		}

		if (hn != null)
		{
			search_intent.putExtra("hn_string", hn);
		}

		if (offline)
		{
			search_intent.putExtra("type", "offline");
		}
		else
		{
			search_intent.putExtra("type", "online");
		}
		// search_intent.putExtra("search_country_id", 999);
		String pm_temp = "0";
		if (partialmatch)
		{
			pm_temp = "1";
		}
		search_intent.putExtra("partial_match", pm_temp);

		if (index)
		{
			Global_Navit_Object.startActivityForResult(search_intent, NavitAddressSearch_id_offline);
		}
		else
		{

		}
	}

}
