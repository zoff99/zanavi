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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class NavitMapDownloader
{
	static final String ZANAVI_MAPS_AGE_URL = "http://maps.zanavi.cc/maps_age.html";

	// ------- RELEASE SETTINGS --------
	// ------- RELEASE SETTINGS --------
	static String ZANAVI_MAPS_BASE_URL = "http://dl.zanavi.cc/data/";
	static String ZANAVI_MAPS_SEVERTEXT_URL = "http://dl.zanavi.cc/server.txt";
	static final String ZANAVI_MAPS_BASE_URL_PROTO = "http://";
	static final String ZANAVI_MAPS_BASE_URL_WO_SERVERNAME = "/data/";
	// ------- RELEASE SETTINGS --------
	// ------- RELEASE SETTINGS --------

	// ------- DEBUG DEBUG SETTINGS --------
	// ------- DEBUG DEBUG SETTINGS --------
	//static final String ZANAVI_MAPS_BASE_URL = "https://192.168.0.3:446/maps/";
	//static final String ZANAVI_MAPS_SEVERTEXT_URL = "https://192.168.0.3:446/maps/server.txt";
	//static final String ZANAVI_MAPS_BASE_URL_PROTO = "https://";
	//static final String ZANAVI_MAPS_BASE_URL_WO_SERVERNAME = "/maps/";
	// ------- DEBUG DEBUG SETTINGS --------
	// ------- DEBUG DEBUG SETTINGS --------

	final static boolean USE_OKHTTPCLIENT = false;

	static int MULTI_NUM_THREADS_MAX = 5; // 5
	static int MULTI_NUM_THREADS = 3; // 3 // how many download streams for a file
	static int MULTI_NUM_THREADS_LOCAL = 1; // how many download streams for the current file from the current server

	public static class zanavi_osm_map_values
	{
		String map_name = "";
		String url = "";
		long est_size_bytes = 0;
		String est_size_bytes_human_string = "";
		String text_for_select_list = "";
		Boolean is_continent = false;
		int continent_id = 0;

		public zanavi_osm_map_values(String mapname, String url, long bytes_est, Boolean is_con, int con_id)
		{
			this.is_continent = is_con;
			this.continent_id = con_id;
			this.map_name = mapname;
			this.url = url;
			this.est_size_bytes = bytes_est;
			if (this.est_size_bytes <= 0)
			{
				// dummy entry, dont show size!
				this.est_size_bytes_human_string = "";
			}
			else
			{
				if (((int) ((float) (this.est_size_bytes) / 1024f / 1024f)) > 0)
				{
					this.est_size_bytes_human_string = " " + (int) ((float) (this.est_size_bytes) / 1024f / 1024f) + "MB";
				}
				else
				{
					this.est_size_bytes_human_string = " " + (int) ((float) (this.est_size_bytes) / 1024f) + "kB";
				}
			}
			this.text_for_select_list = this.map_name + " " + this.est_size_bytes_human_string;
		}

		public String toString()
		{
			String ret = "continent_id=" + this.continent_id + " est_size_bytes=" + this.est_size_bytes + " est_size_bytes_human_string=\"" + this.est_size_bytes_human_string + "\" map_name=\"" + this.map_name + "\" text_for_select_list=\"" + this.text_for_select_list + "\" url=" + this.url;
			return ret;
		}
	}

	//
	// define the maps here
	//
	//
	static final zanavi_osm_map_values z_Caribbean = new zanavi_osm_map_values("Caribbean", "caribbean.bin", -2, true, 136);
	//
	static final zanavi_osm_map_values z_North_America = new zanavi_osm_map_values("North America", "north_america.bin", -2, true, 6);
	static final zanavi_osm_map_values z_Country_borders = new zanavi_osm_map_values("Country borders [detail]", "borders.bin", 6271108L, true, 0);
	static final zanavi_osm_map_values z_Coastline = new zanavi_osm_map_values("Coastline", "coastline.bin", 173729342L, true, 0);
	static final zanavi_osm_map_values z_Restl_welt = new zanavi_osm_map_values("Rest of the World", "restl_welt.bin", 657152543L, true, 0);
	static zanavi_osm_map_values z_Planet = new zanavi_osm_map_values("Planet", "planet.bin", 17454169665L, true, 1);
	static zanavi_osm_map_values z_Europe = new zanavi_osm_map_values("Europe", "europe.bin", 4591607513L, true, 3);
	static final zanavi_osm_map_values z_Africa = new zanavi_osm_map_values("Africa", "africa.bin", 1037269390L, true, 4);
	static final zanavi_osm_map_values z_Asia = new zanavi_osm_map_values("Asia", "asia.bin", 2747994511L, true, 5);
	static zanavi_osm_map_values z_USA = new zanavi_osm_map_values("USA", "usa.bin", 2795629945L, true, 27);
	static final zanavi_osm_map_values z_Central_America = new zanavi_osm_map_values("Central America", "central_america.bin", 157073876L, true, 7);
	static final zanavi_osm_map_values z_South_America = new zanavi_osm_map_values("South America", "south_america.bin", 616648903L, true, 8);
	static final zanavi_osm_map_values z_Australia_and_Oceania = new zanavi_osm_map_values("Australia and Oceania", "australia_oceania.bin", 290908706L, true, 9);
	static final zanavi_osm_map_values z_Albania = new zanavi_osm_map_values("Albania", "albania.bin", 11868865L, false, 3);
	static final zanavi_osm_map_values z_Alps = new zanavi_osm_map_values("Alps", "alps.bin", 1093343815L, false, 3);
	static final zanavi_osm_map_values z_Andorra = new zanavi_osm_map_values("Andorra", "andorra.bin", 830143L, false, 3);
	static final zanavi_osm_map_values z_Austria = new zanavi_osm_map_values("Austria", "austria.bin", 251905975L, false, 3);
	static final zanavi_osm_map_values z_Azores = new zanavi_osm_map_values("Azores", "azores.bin", 2586936L, false, 3);
	static final zanavi_osm_map_values z_Belarus = new zanavi_osm_map_values("Belarus", "belarus.bin", 35370454L, false, 3);
	static final zanavi_osm_map_values z_Belgium = new zanavi_osm_map_values("Belgium", "belgium.bin", 59776296L, false, 3);
	static final zanavi_osm_map_values z_Bosnia_Herzegovina = new zanavi_osm_map_values("Bosnia-Herzegovina", "bosnia-herzegovina.bin", 20965766L, false, 3);
	static final zanavi_osm_map_values z_British_Isles = new zanavi_osm_map_values("British Isles", "british_isles.bin", 287714717L, false, 3);
	static final zanavi_osm_map_values z_Bulgaria = new zanavi_osm_map_values("Bulgaria", "bulgaria.bin", 187458L, false, 3);
	static final zanavi_osm_map_values z_Croatia = new zanavi_osm_map_values("Croatia", "croatia.bin", 19816810L, false, 3);
	static final zanavi_osm_map_values z_Cyprus = new zanavi_osm_map_values("Cyprus", "cyprus.bin", 3062844L, false, 3);
	static final zanavi_osm_map_values z_Czech_Republic = new zanavi_osm_map_values("Czech Republic", "czech_republic.bin", 263104401L, false, 3);
	static final zanavi_osm_map_values z_Denmark = new zanavi_osm_map_values("Denmark", "denmark.bin", 67254793L, false, 3);
	static final zanavi_osm_map_values z_Estonia = new zanavi_osm_map_values("Estonia", "estonia.bin", 16299316L, false, 3);
	static final zanavi_osm_map_values z_Faroe_Islands = new zanavi_osm_map_values("Faroe Islands", "faroe_islands.bin", 601782L, false, 3);
	static final zanavi_osm_map_values z_Finland = new zanavi_osm_map_values("Finland", "finland.bin", 79130118L, false, 3);
	static final zanavi_osm_map_values z_France = new zanavi_osm_map_values("France", "france.bin", 1994494949L, false, 3);
	static final zanavi_osm_map_values z_Germany = new zanavi_osm_map_values("Germany", "germany.bin", 1524238730L, false, 3);
	static final zanavi_osm_map_values z_Great_Britain = new zanavi_osm_map_values("Great Britain", "great_britain.bin", 471140932L, false, 3);
	static final zanavi_osm_map_values z_Greece = new zanavi_osm_map_values("Greece", "greece.bin", 36331613L, false, 3);
	static final zanavi_osm_map_values z_Hungary = new zanavi_osm_map_values("Hungary", "hungary.bin", 20996247L, false, 3);
	static final zanavi_osm_map_values z_Iceland = new zanavi_osm_map_values("Iceland", "iceland.bin", 5949356L, false, 3);
	static final zanavi_osm_map_values z_Ireland = new zanavi_osm_map_values("Ireland", "ireland.bin", 30020687L, false, 3);
	static final zanavi_osm_map_values z_Isle_of_man = new zanavi_osm_map_values("Isle of man", "isle_of_man.bin", 876966L, false, 3);
	static final zanavi_osm_map_values z_Italy = new zanavi_osm_map_values("Italy", "italy.bin", 720459213L, false, 3);
	static final zanavi_osm_map_values z_Kosovo = new zanavi_osm_map_values("Kosovo", "kosovo.bin", 2726077L, false, 3);
	static final zanavi_osm_map_values z_Latvia = new zanavi_osm_map_values("Latvia", "latvia.bin", 19153876L, false, 3);
	static final zanavi_osm_map_values z_Liechtenstein = new zanavi_osm_map_values("Liechtenstein", "liechtenstein.bin", 329042L, false, 3);
	static final zanavi_osm_map_values z_Lithuania = new zanavi_osm_map_values("Lithuania", "lithuania.bin", 15062324L, false, 3);
	static final zanavi_osm_map_values z_Luxembourg = new zanavi_osm_map_values("Luxembourg", "luxembourg.bin", 5555131L, false, 3);
	static final zanavi_osm_map_values z_Macedonia = new zanavi_osm_map_values("Macedonia", "macedonia.bin", 3652744L, false, 3);
	static final zanavi_osm_map_values z_Malta = new zanavi_osm_map_values("Malta", "malta.bin", 665042L, false, 3);
	static final zanavi_osm_map_values z_Moldova = new zanavi_osm_map_values("Moldova", "moldova.bin", 7215780L, false, 3);
	static final zanavi_osm_map_values z_Monaco = new zanavi_osm_map_values("Monaco", "monaco.bin", 91311L, false, 3);
	static final zanavi_osm_map_values z_Montenegro = new zanavi_osm_map_values("Montenegro", "montenegro.bin", 2377175L, false, 3);
	static final zanavi_osm_map_values z_Netherlands = new zanavi_osm_map_values("Netherlands", "netherlands.bin", 231414741L, false, 3);
	static final zanavi_osm_map_values z_Norway = new zanavi_osm_map_values("Norway", "norway.bin", 45117034L, false, 3);
	static final zanavi_osm_map_values z_Poland = new zanavi_osm_map_values("Poland", "poland.bin", 547877032L, false, 3);
	static final zanavi_osm_map_values z_Portugal = new zanavi_osm_map_values("Portugal", "portugal.bin", 30029993L, false, 3);
	static final zanavi_osm_map_values z_Romania = new zanavi_osm_map_values("Romania", "romania.bin", 39689553L, false, 3);
	static final zanavi_osm_map_values z_Russia_European_part = new zanavi_osm_map_values("Russia European part", "russia-european-part.bin", 476175240L, false, 3);
	static final zanavi_osm_map_values z_Serbia = new zanavi_osm_map_values("Serbia", "serbia.bin", 11166698L, false, 3);
	static final zanavi_osm_map_values z_Slovakia = new zanavi_osm_map_values("Slovakia", "slovakia.bin", 100067631L, false, 3);
	static final zanavi_osm_map_values z_Slovenia = new zanavi_osm_map_values("Slovenia", "slovenia.bin", 59260959L, false, 3);
	static final zanavi_osm_map_values z_Spain = new zanavi_osm_map_values("Spain", "spain.bin", 355510377L, false, 3);
	static final zanavi_osm_map_values z_Sweden = new zanavi_osm_map_values("Sweden", "sweden.bin", 173543640L, false, 3);
	static final zanavi_osm_map_values z_Switzerland = new zanavi_osm_map_values("Switzerland", "switzerland.bin", 152254413L, false, 3);
	static final zanavi_osm_map_values z_Turkey = new zanavi_osm_map_values("Turkey", "turkey.bin", 33231427L, false, 3);
	static final zanavi_osm_map_values z_Ukraine = new zanavi_osm_map_values("Ukraine", "ukraine.bin", 58070734L, false, 3);
	static final zanavi_osm_map_values z_Canari_Islands = new zanavi_osm_map_values("Canari Islands", "canari_islands.bin", 7125254L, false, 4);
	static final zanavi_osm_map_values z_India = new zanavi_osm_map_values("India", "india.bin", 46569907L, false, 5);
	static final zanavi_osm_map_values z_Israel_and_Palestine = new zanavi_osm_map_values("Israel and Palestine", "israel_and_palestine.bin", 15630491L, false, 5);
	static final zanavi_osm_map_values z_China = new zanavi_osm_map_values("China", "china.bin", 49738512L, false, 5);
	static final zanavi_osm_map_values z_Japan = new zanavi_osm_map_values("Japan", "japan.bin", 413065433L, false, 5);
	static final zanavi_osm_map_values z_Taiwan = new zanavi_osm_map_values("Taiwan", "taiwan.bin", 5689334L, false, 5);
	static final zanavi_osm_map_values z_Canada = new zanavi_osm_map_values("Canada", "canada.bin", 830908722L, false, 6);
	static final zanavi_osm_map_values z_Greenland = new zanavi_osm_map_values("Greenland", "greenland.bin", 500803L, false, 6);
	static final zanavi_osm_map_values z_Mexico = new zanavi_osm_map_values("Mexico", "mexico.bin", 29858297L, false, 6);
	static zanavi_osm_map_values z_US_Midwest = new zanavi_osm_map_values("US-Midwest", "us-midwest.bin", 495302706L, false, 27);
	static zanavi_osm_map_values z_US_Northeast = new zanavi_osm_map_values("US-Northeast", "us-northeast.bin", 206503509L, false, 27);
	static zanavi_osm_map_values z_US_Pacific = new zanavi_osm_map_values("US-Pacific", "us-pacific.bin", 11527206L, false, 27);
	static zanavi_osm_map_values z_US_South = new zanavi_osm_map_values("US-South", "us-south.bin", 803391332L, false, 27);
	static zanavi_osm_map_values z_US_West = new zanavi_osm_map_values("US-West", "us-west.bin", 506304481L, false, 27);
	static final zanavi_osm_map_values z_Alabama = new zanavi_osm_map_values("Alabama", "alabama.bin", 35081542L, false, 27);
	static final zanavi_osm_map_values z_Alaska = new zanavi_osm_map_values("Alaska", "alaska.bin", 7844950L, false, 27);
	static final zanavi_osm_map_values z_Arizona = new zanavi_osm_map_values("Arizona", "arizona.bin", 33938704L, false, 27);
	static final zanavi_osm_map_values z_Arkansas = new zanavi_osm_map_values("Arkansas", "arkansas.bin", 22029069L, false, 27);
	static final zanavi_osm_map_values z_California = new zanavi_osm_map_values("California", "california.bin", 203758150L, false, 27);
	static final zanavi_osm_map_values z_North_Carolina = new zanavi_osm_map_values("North Carolina", "north-carolina.bin", 128642801L, false, 27);
	static final zanavi_osm_map_values z_South_Carolina = new zanavi_osm_map_values("South Carolina", "south-carolina.bin", 41257655L, false, 27);
	static final zanavi_osm_map_values z_Colorado = new zanavi_osm_map_values("Colorado", "colorado.bin", 67174144L, false, 27);
	static final zanavi_osm_map_values z_North_Dakota = new zanavi_osm_map_values("North Dakota", "north-dakota.bin", 45925872L, false, 27);
	static final zanavi_osm_map_values z_South_Dakota = new zanavi_osm_map_values("South Dakota", "south-dakota.bin", 13888265L, false, 27);
	static final zanavi_osm_map_values z_District_of_Columbia = new zanavi_osm_map_values("District of Columbia", "district-of-columbia.bin", 5693682L, false, 27);
	static final zanavi_osm_map_values z_Connecticut = new zanavi_osm_map_values("Connecticut", "connecticut.bin", 7189491L, false, 27);
	static final zanavi_osm_map_values z_Delaware = new zanavi_osm_map_values("Delaware", "delaware.bin", 3089068L, false, 27);
	static final zanavi_osm_map_values z_Florida = new zanavi_osm_map_values("Florida", "florida.bin", 49772033L, false, 27);
	static final zanavi_osm_map_values z_Georgia = new zanavi_osm_map_values("Georgia", "georgia.bin", 83076475L, false, 27);
	static final zanavi_osm_map_values z_New_Hampshire = new zanavi_osm_map_values("New Hampshire", "new-hampshire.bin", 13900082L, false, 27);
	static final zanavi_osm_map_values z_Hawaii = new zanavi_osm_map_values("Hawaii", "hawaii.bin", 3670926L, false, 27);
	static final zanavi_osm_map_values z_Idaho = new zanavi_osm_map_values("Idaho", "idaho.bin", 27122570L, false, 27);
	static final zanavi_osm_map_values z_Illinois = new zanavi_osm_map_values("Illinois", "illinois.bin", 64992622L, false, 27);
	static final zanavi_osm_map_values z_Indiana = new zanavi_osm_map_values("Indiana", "indiana.bin", 23877847L, false, 27);
	static final zanavi_osm_map_values z_Iowa = new zanavi_osm_map_values("Iowa", "iowa.bin", 47021367L, false, 27);
	static final zanavi_osm_map_values z_New_Jersey = new zanavi_osm_map_values("New Jersey", "new-jersey.bin", 25412463L, false, 27);
	static final zanavi_osm_map_values z_Kansas = new zanavi_osm_map_values("Kansas", "kansas.bin", 22432235L, false, 27);
	static final zanavi_osm_map_values z_Kentucky = new zanavi_osm_map_values("Kentucky", "kentucky.bin", 34892443L, false, 27);
	static final zanavi_osm_map_values z_Louisiana = new zanavi_osm_map_values("Louisiana", "louisiana.bin", 40739401L, false, 27);
	static final zanavi_osm_map_values z_Maine = new zanavi_osm_map_values("Maine", "maine.bin", 14716304L, false, 27);
	static final zanavi_osm_map_values z_Maryland = new zanavi_osm_map_values("Maryland", "maryland.bin", 27470086L, false, 27);
	static final zanavi_osm_map_values z_Massachusetts = new zanavi_osm_map_values("Massachusetts", "massachusetts.bin", 42398142L, false, 27);
	static final zanavi_osm_map_values z_New_Mexico = new zanavi_osm_map_values("New Mexico", "new-mexico.bin", 26815652L, false, 27);
	static final zanavi_osm_map_values z_Michigan = new zanavi_osm_map_values("Michigan", "michigan.bin", 44192808L, false, 27);
	static final zanavi_osm_map_values z_Minnesota = new zanavi_osm_map_values("Minnesota", "minnesota.bin", 82203062L, false, 27);
	static final zanavi_osm_map_values z_Mississippi = new zanavi_osm_map_values("Mississippi", "mississippi.bin", 31680044L, false, 27);
	static final zanavi_osm_map_values z_Missouri = new zanavi_osm_map_values("Missouri", "missouri.bin", 39762823L, false, 27);
	static final zanavi_osm_map_values z_Montana = new zanavi_osm_map_values("Montana", "montana.bin", 22707427L, false, 27);
	static final zanavi_osm_map_values z_Nebraska = new zanavi_osm_map_values("Nebraska", "nebraska.bin", 25624308L, false, 27);
	static final zanavi_osm_map_values z_Nevada = new zanavi_osm_map_values("Nevada", "nevada.bin", 21979548L, false, 27);
	static final zanavi_osm_map_values z_Ohio = new zanavi_osm_map_values("Ohio", "ohio.bin", 40769081L, false, 27);
	static final zanavi_osm_map_values z_Oklahoma = new zanavi_osm_map_values("Oklahoma", "oklahoma.bin", 55866120L, false, 27);
	static final zanavi_osm_map_values z_Oregon = new zanavi_osm_map_values("Oregon", "oregon.bin", 36940277L, false, 27);
	static final zanavi_osm_map_values z_Pennsylvania = new zanavi_osm_map_values("Pennsylvania", "pennsylvania.bin", 51500049L, false, 27);
	static final zanavi_osm_map_values z_Rhode_Island = new zanavi_osm_map_values("Rhode Island", "rhode-island.bin", 3691209L, false, 27);
	static final zanavi_osm_map_values z_Tennessee = new zanavi_osm_map_values("Tennessee", "tennessee.bin", 30511825L, false, 27);
	static final zanavi_osm_map_values z_Texas = new zanavi_osm_map_values("Texas", "texas.bin", 108367999L, false, 27);
	static final zanavi_osm_map_values z_Utah = new zanavi_osm_map_values("Utah", "utah.bin", 19254246L, false, 27);
	static final zanavi_osm_map_values z_Vermont = new zanavi_osm_map_values("Vermont", "vermont.bin", 7917383L, false, 27);
	static final zanavi_osm_map_values z_Virginia = new zanavi_osm_map_values("Virginia", "virginia.bin", 98109314L, false, 27);
	static final zanavi_osm_map_values z_West_Virginia = new zanavi_osm_map_values("West Virginia", "west-virginia.bin", 12267128L, false, 27);
	static final zanavi_osm_map_values z_Washington = new zanavi_osm_map_values("Washington", "washington.bin", 34281164L, false, 27);
	static final zanavi_osm_map_values z_Wisconsin = new zanavi_osm_map_values("Wisconsin", "wisconsin.bin", 44033160L, false, 27);
	static final zanavi_osm_map_values z_Wyoming = new zanavi_osm_map_values("Wyoming", "wyoming.bin", 15865183L, false, 27);
	static final zanavi_osm_map_values z_New_York = new zanavi_osm_map_values("New York", "new-york.bin", 39570304L, false, 27);
	static final zanavi_osm_map_values z_USA_minor_Islands = new zanavi_osm_map_values("USA minor Islands", "usa_minor_islands.bin", 24705L, false, 27);
	static final zanavi_osm_map_values z_Panama = new zanavi_osm_map_values("Panama", "panama.bin", 4836548L, false, 7);
	static final zanavi_osm_map_values z_Haiti_and_Dom_Rep_ = new zanavi_osm_map_values("Haiti and Dom.Rep.", "haiti_and_domrep.bin", 35886979L, false, 136);
	static final zanavi_osm_map_values z_Cuba = new zanavi_osm_map_values("Cuba", "cuba.bin", 11981430L, false, 136);
	static final zanavi_osm_map_values z_Rest_of_World = new zanavi_osm_map_values("Rest of World", "restl_welt.bin", 657152543L, false, 1);
	//
	//
	//
	static final zanavi_osm_map_values[] z_OSM_MAPS = new zanavi_osm_map_values[] { z_Country_borders, z_Coastline, z_Rest_of_World, z_Planet, z_Europe, z_North_America, z_USA, z_Central_America, z_South_America, z_Africa, z_Asia, z_Australia_and_Oceania, z_Caribbean, z_Albania, z_Alps, z_Andorra, z_Austria, z_Azores, z_Belarus, z_Belgium, z_Bosnia_Herzegovina, z_British_Isles, z_Bulgaria, z_Croatia, z_Cyprus, z_Czech_Republic, z_Denmark, z_Estonia, z_Faroe_Islands, z_Finland, z_France,
			z_Germany, z_Great_Britain, z_Greece, z_Hungary, z_Iceland, z_Ireland, z_Isle_of_man, z_Italy, z_Kosovo, z_Latvia, z_Liechtenstein, z_Lithuania, z_Luxembourg, z_Macedonia, z_Malta, z_Moldova, z_Monaco, z_Montenegro, z_Netherlands, z_Norway, z_Poland, z_Portugal, z_Romania, z_Russia_European_part, z_Serbia, z_Slovakia, z_Slovenia, z_Spain, z_Sweden, z_Switzerland, z_Turkey, z_Ukraine, z_Canari_Islands, z_India, z_Israel_and_Palestine, z_China, z_Japan, z_Taiwan, z_Canada,
			z_Greenland, z_Mexico, z_US_Midwest, z_US_Northeast, z_US_Pacific, z_US_South, z_US_West, z_Alabama, z_Alaska, z_Arizona, z_Arkansas, z_California, z_North_Carolina, z_South_Carolina, z_Colorado, z_North_Dakota, z_South_Dakota, z_District_of_Columbia, z_Connecticut, z_Delaware, z_Florida, z_Georgia, z_New_Hampshire, z_Hawaii, z_Idaho, z_Illinois, z_Indiana, z_Iowa, z_New_Jersey, z_Kansas, z_Kentucky, z_Louisiana, z_Maine, z_Maryland, z_Massachusetts, z_New_Mexico, z_Michigan,
			z_Minnesota, z_Mississippi, z_Missouri, z_Montana, z_Nebraska, z_Nevada, z_Ohio, z_Oklahoma, z_Oregon, z_Pennsylvania, z_Rhode_Island, z_Tennessee, z_Texas, z_Utah, z_Vermont, z_Virginia, z_West_Virginia, z_Washington, z_Wisconsin, z_Wyoming, z_New_York, z_USA_minor_Islands, z_Panama, z_Haiti_and_Dom_Rep_, z_Cuba };

	//
	//
	//
	//
	//
	public static String[] OSM_MAP_NAME_LIST_inkl_SIZE_ESTIMATE = null;
	public static String[] OSM_MAP_NAME_LIST_ondisk = null;

	public static int[] OSM_MAP_NAME_ORIG_ID_LIST = null;
	public static String[] OSM_MAP_NAME_ondisk_ORIG_LIST = null;

	private static Boolean already_inited = false;

	public Boolean stop_me = false;
	static final int SOCKET_CONNECT_TIMEOUT = 6000; // 22000; // 22 secs.
	static final int SOCKET_READ_TIMEOUT = 9000; // 18000; // 18 secs.
	// static final int MAP_WRITE_FILE_BUFFER = 1024 * 8;
	static final int MAP_WRITE_MEM_BUFFER = 2 * 1024; //2048; // don't make this too large
	static final int MAP_READ_FILE_BUFFER = 64 * 1024; // 102400; // don't make this too large
	static final int UPDATE_PROGRESS_EVERY_CYCLE = 512; // **UNUSED NOW** 12; // 8 -> is nicer, but maybe too fast for some devices
	static final int PROGRESS_UPDATE_INTERVAL_VALUE = 1100; // lower is faster progress update, but maybe too fast for some devices
	static final int RETRIES = 20000; // 75; // this many retries on map download
	static final int MD5_CALC_BUFFER_KB = 128;

	static final long MAX_SINGLE_BINFILE_SIZE = 3 * 512 * 1024 * 1024; // split map at this size into pieces [1.5GB] [1.610.612.736 Bytes]	
	// static final long MAX_SINGLE_BINFILE_SIZE = 800 * 1024 * 1024; // split map at this size into pieces [~800 MBytes]	
	// --- DEBUG ONLY --- // static final long MAX_SINGLE_BINFILE_SIZE = 80 * 1024 * 1024; // 80 MBytes	 // --- DEBUG ONLY --- //

	static final String DOWNLOAD_FILENAME = "navitmap.tmp";
	static final String MD5_DOWNLOAD_TEMPFILE = "navitmap_tmp.md5";
	static final String CAT_FILE = "maps_cat.txt";
	public static List<String> map_catalogue = new ArrayList<String>();
	public static List<String> map_catalogue_date = new ArrayList<String>();
	public static List<String> map_servers_used = new ArrayList<String>();
	static final String MAP_CAT_HEADER = "# ZANavi maps -- do not edit by hand --";
	public static final String MAP_URL_NAME_UNKNOWN = "* unknown map *";
	public static final String MAP_DISK_NAME_UNKNOWN = "-> unknown map";

	static final String MAP_FILENAME_PRI = "navitmap_001.bin";
	static final String MAP_FILENAME_SEC = "navitmap_002.bin";
	static final String MAP_FILENAME_BASE = "navitmap_%03d.bin";
	static final int MAP_MAX_FILES = 45; // 19; // should be 45
	static final String MAP_FILENAME_BORDERS = "borders.bin";
	static final String MAP_FILENAME_COASTLINE = "coastline.bin";

	static long[] mapdownload_already_read = null;
	static float[] mapdownload_byte_per_second_overall = null;
	static float[] mapdownload_byte_per_second_now = null;
	static int mapdownload_error_code = 0;
	static Boolean mapdownload_stop_all_threads = false;

	@SuppressWarnings("deprecation")
	static okhttp3.OkUrlFactory http_client_new_urlfactory = null;
	static okhttp3.OkHttpClient http_client_new = null;

	static final int MAX_MAP_COUNT = 500;
	static boolean download_active = false;
	static boolean download_active_start = false;

	public class ProgressThread extends Thread
	{
		Handler mHandler;
		zanavi_osm_map_values map_values;
		int map_num;
		int my_dialog_num;

		ProgressThread(Handler h, zanavi_osm_map_values map_values, int map_num2)
		{
			this.mHandler = h;
			this.map_values = map_values;
			this.map_num = map_num2;
			if (this.map_num == Navit.MAP_NUM_PRIMARY)
			{
				this.my_dialog_num = Navit.MAPDOWNLOAD_PRI_DIALOG;
			}
			else if (this.map_num == Navit.MAP_NUM_SECONDARY)
			{
				this.my_dialog_num = Navit.MAPDOWNLOAD_SEC_DIALOG;
			}
		}

		public void run()
		{

			download_active = true;
			System.out.println("download_active=true");

			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download_active = true");

			try
			{
				// show main progress bar
				Message msg7 = Navit.Navit_progress_h.obtainMessage();
				Bundle b7 = new Bundle();
				// b7.putInt("pg", 0);
				msg7.what = 42;
				msg7.setData(b7);
				Navit.Navit_progress_h.sendMessage(msg7);
			}
			catch (Exception e)
			{
			}

			try
			{
				// set progress bar to "Indeterminate"
				Message msg_prog = new Message();
				Bundle b_prog = new Bundle();
				b_prog.putInt("pg", 1);
				msg_prog.what = 5;
				msg_prog.setData(b_prog);
				ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);
			}
			catch (Exception e)
			{
			}

			try
			{
				// --------- busy spinner ---------
				ZANaviBusySpinner.active = true;
				NavitGraphics.busyspinnertext_.setText2(Navit.get_text("downloading")); // TRANS
				//NavitGraphics.busyspinnertext_.bringToFront();
				NavitGraphics.busyspinner_.setVisibility(View.VISIBLE);
				//NavitGraphics.busyspinnertext_.setVisibility(View.VISIBLE);
				//NavitGraphics.busyspinnertext_.postInvalidate();
				NavitGraphics.busyspinner_.postInvalidate();
				// --------- busy spinner ---------
			}
			catch (Exception e)
			{
				System.out.println("NagitMapDownloader:" + "EX:" + e.getMessage());
			}

			stop_me = false;
			mapdownload_stop_all_threads = false;
			System.out.println("DEBUG_MAP_DOWNLOAD::map_num=" + this.map_num + " v=" + map_values.map_name + " " + map_values.url);

			// ----- service start -----
			// ----- service start -----
			// Navit.getBaseContext_.startService(Navit.ZANaviMapDownloaderServiceIntent);
			// ----- service start -----
			// ----- service start -----

			//
			//Navit.dim_screen_wrapper(); // brightness ---------
			int exit_code = download_osm_map(mHandler, map_values, this.map_num);
			//Navit.default_brightness_screen_wrapper(); // brightness ---------
			//

			try
			{
				// hide download actionbar icon
				Message msg2 = Navit.Navit_progress_h.obtainMessage();
				Bundle b2 = new Bundle();
				msg2.what = 24;
				msg2.setData(b2);
				Navit.Navit_progress_h.sendMessage(msg2);
			}
			catch (Exception e)
			{
			}

			// ----- service stop -----
			// ----- service stop -----
			Navit.getBaseContext_.stopService(Navit.ZANaviMapDownloaderServiceIntent);
			// ----- service stop -----
			// ----- service stop -----

			// clean up always
			File tmp_downloadfile = new File(Navit.CFG_FILENAME_PATH, DOWNLOAD_FILENAME);
			tmp_downloadfile.delete();
			for (int jkl = 1; jkl < 51; jkl++)
			{
				File tmp_downloadfileSplit = new File(Navit.CFG_FILENAME_PATH, DOWNLOAD_FILENAME + "." + String.valueOf(jkl));
				tmp_downloadfileSplit.delete();
			}
			//
			File tmp_downloadfile_md5 = new File(Navit.MAPMD5_FILENAME_PATH, MD5_DOWNLOAD_TEMPFILE);
			tmp_downloadfile_md5.delete();
			File tmp_downloadfile_idx = new File(Navit.CFG_FILENAME_PATH, DOWNLOAD_FILENAME + ".idx");
			tmp_downloadfile_idx.delete();
			Log.d("NavitMapDownloader", "(a)removed " + tmp_downloadfile.getAbsolutePath());
			// ok, remove dialog
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 0;
			b.putInt("dialog_num", this.my_dialog_num);
			// only exit_code=0 will try to use the new map
			b.putInt("exit_code", exit_code);
			b.putString("map_name", map_values.map_name);
			msg.setData(b);
			mHandler.sendMessage(msg);

			download_active = false;
			download_active_start = false;

			try
			{
				// hide main progress bar
				Message msg7 = Navit.Navit_progress_h.obtainMessage();
				Bundle b7 = new Bundle();
				msg7.what = 41;
				msg7.setData(b7);
				Navit.Navit_progress_h.sendMessage(msg7);
			}
			catch (Exception e)
			{
			}

			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download_active = false (1)");

			try
			{
				// --------- busy spinner ---------
				ZANaviBusySpinner.active = false;
				// ZANaviBusySpinner.cancelAnim();
				NavitGraphics.busyspinner_.getHandler().post(new Runnable()
				{
					public void run()
					{
						ZANaviBusySpinner.cancelAnim();
					}
				});

				NavitGraphics.busyspinnertext_.getHandler().post(new Runnable()
				{
					public void run()
					{
						NavitGraphics.busyspinnertext_.setText2("");
					}
				});

				NavitGraphics.busyspinner_.getHandler().post(new Runnable()
				{
					public void run()
					{
						NavitGraphics.busyspinner_.setVisibility(View.INVISIBLE);
					}
				});
				// --------- busy spinner ---------
			}
			catch (Exception e)
			{
				System.out.println("NagitMapDownloader:" + "EX2:" + e.getMessage());
			}

			System.out.println("download_active=*false*");
		}

		public void stop_thread()
		{
			stop_me = true;
			mapdownload_stop_all_threads = true;
			Log.d("NavitMapDownloader", "stop_me -> true");

			// ----- service stop -----
			// ----- service stop -----
			// Navit.getBaseContext_.stopService(Navit.ZANaviMapDownloaderServiceIntent);
			// ----- service stop -----
			// ----- service stop -----

			//
			//Navit.default_brightness_screen(); // brightness ---------
			//
			// remove the tmp download file (if there is one)
			File tmp_downloadfile = new File(Navit.CFG_FILENAME_PATH, DOWNLOAD_FILENAME);
			tmp_downloadfile.delete();
			for (int jkl = 1; jkl < 51; jkl++)
			{
				File tmp_downloadfileSplit = new File(Navit.CFG_FILENAME_PATH, DOWNLOAD_FILENAME + "." + String.valueOf(jkl));
				tmp_downloadfileSplit.delete();
			}
			//
			File tmp_downloadfile_md5 = new File(Navit.MAPMD5_FILENAME_PATH, MD5_DOWNLOAD_TEMPFILE);
			tmp_downloadfile_md5.delete();
			File tmp_downloadfile_idx = new File(Navit.CFG_FILENAME_PATH, DOWNLOAD_FILENAME + ".idx");
			tmp_downloadfile_idx.delete();
			Log.d("NavitMapDownloader", "(b)removed " + tmp_downloadfile.getAbsolutePath());

			try
			{
				Message msg2 = Navit.Navit_progress_h.obtainMessage();
				Bundle b2 = new Bundle();
				msg2.what = 24;
				msg2.setData(b2);
				Navit.Navit_progress_h.sendMessage(msg2);
			}
			catch (Exception e)
			{
			}

			// close cancel dialog activity
			Message msg2 = new Message();
			Bundle b2 = new Bundle();
			msg2.what = 1;
			msg2.setData(b2);
			ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg2);

			download_active = false;
			download_active_start = false;

			try
			{
				// hide main progress bar
				Message msg7 = Navit.Navit_progress_h.obtainMessage();
				Bundle b7 = new Bundle();
				msg7.what = 41;
				msg7.setData(b7);
				Navit.Navit_progress_h.sendMessage(msg7);
			}
			catch (Exception e)
			{
			}

			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download_active = false (2)");

			try
			{
				// --------- busy spinner ---------
				ZANaviBusySpinner.active = false;
				ZANaviBusySpinner.cancelAnim();
				// NavitGraphics.busyspinnertext_.setVisibility(View.INVISIBLE);
				NavitGraphics.busyspinnertext_.setText2("");
				NavitGraphics.busyspinner_.setVisibility(View.INVISIBLE);
				// --------- busy spinner ---------
			}
			catch (Exception e)
			{
				System.out.println("NagitMapDownloader:" + "EX3:" + e.getMessage());
			}

			System.out.println("download_active=*false*");
		}
	}

	private class MultiStreamDownloaderThread extends Thread
	{
		Boolean running = false;
		Handler handler;
		zanavi_osm_map_values map_values;
		int map_num;
		int my_dialog_num;
		int my_num = 0;
		int num_threads = 0;
		String PATH = null;
		String PATH2 = null;
		String fileName = null;
		String final_fileName = null;
		String this_server_name = null;
		String up_map = null;
		long start_byte = 0L;
		long start_byte_count_now = 0L;
		long end_byte = 0L;

		MultiStreamDownloaderThread(int num_threads, Handler h, zanavi_osm_map_values map_values, int map_num2, int c, String p, String p2, String fn, String ffn, String sn, String upmap, long start_byte, long end_byte)
		{
			running = false;

			this.start_byte = start_byte;
			this.end_byte = end_byte;

			// System.out.println("DEBUG_MAP_DOWNLOAD::" + c + "bytes=" + this.start_byte + ":" + this.end_byte);
			// System.out.println("DEBUG_MAP_DOWNLOAD::" + c + "Server=" + sn);

			PATH = p;
			PATH2 = p2;
			fileName = fn;
			final_fileName = ffn;
			this_server_name = sn;
			up_map = upmap;
			this.my_num = c;
			this.handler = h;
			this.map_values = map_values;
			this.map_num = map_num2;
			this.num_threads = num_threads;
			if (this.map_num == Navit.MAP_NUM_PRIMARY)
			{
				this.my_dialog_num = Navit.MAPDOWNLOAD_PRI_DIALOG;
			}
			else if (this.map_num == Navit.MAP_NUM_SECONDARY)
			{
				this.my_dialog_num = Navit.MAPDOWNLOAD_SEC_DIALOG;
			}
			// System.out.println("MultiStreamDownloaderThread " + this.my_num + " init");
		}

		public void run()
		{
			running = true;

			try
			{
				// update srv text
				Message msg_prog88 = new Message();
				Bundle b_prog88 = new Bundle();
				b_prog88.putString("srv", this.this_server_name);
				b_prog88.putInt("threadnum", (this.my_num - 1));
				msg_prog88.what = 4;
				msg_prog88.setData(b_prog88);
				ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog88);
			}
			catch (Exception e_srv)
			{
			}

			// System.out.println("DEBUG_MAP_DOWNLOAD::MultiStreamDownloaderThread " + this.my_num + " run");
			while (running)
			{
				if (mapdownload_stop_all_threads == true)
				{
					running = false;
					mapdownload_error_code_inc();
					break;
				}

				int try_number = 0;
				Boolean download_success = false;

				File file = new File(PATH);
				File file2 = new File(PATH2);
				File outputFile = new File(file2, fileName);
				File final_outputFile = new File(file, final_fileName);
				// tests have shown that deleting the file first is sometimes faster -> so we delete it (who knows)
				//**outputFile.delete();
				RandomAccessFile f_rnd = null;

				if (this.start_byte > (MAX_SINGLE_BINFILE_SIZE - 1))
				{
					// split file, we need to compensate
					int split_num = (int) (this.start_byte / MAX_SINGLE_BINFILE_SIZE);
					long real_start_byte = this.start_byte - (MAX_SINGLE_BINFILE_SIZE * split_num);
					f_rnd = (RandomAccessFile) d_open_file(PATH2 + "/" + fileName + "." + split_num, real_start_byte, this.my_num);
					// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "split file calc(a2):split_num=" + split_num + " real_start_byte=" + real_start_byte + " this.start_byte=" + this.start_byte);
					// Log.d("NavitMapDownloader", this.my_num + "split file calc(a2):split_num=" + split_num + " real_start_byte=" + real_start_byte + " this.start_byte=" + this.start_byte);
				}
				else
				{
					f_rnd = (RandomAccessFile) d_open_file(PATH2 + "/" + fileName, this.start_byte, this.my_num);
					// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "split file calc(a1): this.start_byte=" + this.start_byte);
					// Log.d("NavitMapDownloader", this.my_num + "split file calc(a1): this.start_byte=" + this.start_byte);
				}

				if (f_rnd == null)
				{
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					msg.what = 2;
					b.putInt("dialog_num", my_dialog_num);
					b.putString("text", Navit.get_text("Error downloading map!")); //TRANS
					msg.setData(b);
					handler.sendMessage(msg);

					this.running = false;
					mapdownload_error_code_inc();
					break;
					// return 1;
				}

				byte[] buffer = new byte[MAP_WRITE_MEM_BUFFER]; // buffer
				int len1 = 0;
				long already_read = this.start_byte;
				long read_per_interval = 0L;
				int count_read_per_interval = 0;

				int alt = UPDATE_PROGRESS_EVERY_CYCLE; // show progress about every xx cylces
				int alt_cur = 0;
				long alt_progress_update_timestamp = 0L;
				int progress_update_intervall = PROGRESS_UPDATE_INTERVAL_VALUE; // show progress about every xx milliseconds

				String kbytes_per_second = "";
				long start_timestamp = System.currentTimeMillis();
				long start_timestamp_count_now = System.currentTimeMillis();
				NumberFormat formatter = new DecimalFormat("00000.0");
				String eta_string = "";
				float per_second_overall = 0f;
				float per_second_now = 0f;
				long bytes_remaining = 0;
				int eta_seconds = 0;

				int current_split = 0;
				int next_split = 0;

				Message msg;
				Bundle b = new Bundle();

				// while -------
				while ((try_number < RETRIES) && (!download_success))
				{
					if (mapdownload_stop_all_threads == true)
					{
						running = false;
						mapdownload_error_code_inc();
						break;
					}

					if (stop_me)
					{
						// ok we need to be stopped! close all files and end
						this.running = false;
						mapdownload_error_code_inc();
						break;
						// return 2;
					}

					try_number++;
					// Log.d("NavitMapDownloader", this.my_num + "download try number " + try_number);
					// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "download try number " + try_number);

					HttpURLConnection c = d_url_connect(map_values, this_server_name, map_num, this.my_num);
					// set http header to resume download
					//if (this.end_byte == map_values.est_size_bytes)
					//{
					//	c = d_url_resume_download_at(c, already_read, this.my_num);
					//}
					//else
					//{

					if (already_read >= this.end_byte)
					{
						// something has gone wrong, the server seems to give us more bytes than there are in the file? what now?
						// just start from a few bytes back and lets hope it will work this time
						// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + " problem with resuming: already_read=" + already_read + " this.end_byte=" + this.end_byte + " need to correct!!");
						already_read = this.end_byte - 10;
					}

					// Log.d("NavitMapDownloader", this.my_num + "resume values: already_read=" + already_read + " this.start_byte=" + this.start_byte + " this.end_byte=" + this.end_byte);
					// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "resume values: already_read=" + already_read + " this.start_byte=" + this.start_byte + " this.end_byte=" + this.end_byte);
					c = d_url_resume_download_at(c, already_read, this.end_byte, this.my_num);
					//}

					if (try_number > 1)
					{
						// seek to resume position in download file
						// ---------------------
						// close file
						d_close_file(f_rnd, this.my_num);

						// open file
						if (already_read > (MAX_SINGLE_BINFILE_SIZE - 1))
						{
							// split file, we need to compensate
							int split_num = (int) (already_read / MAX_SINGLE_BINFILE_SIZE);
							long real_already_read = already_read - (MAX_SINGLE_BINFILE_SIZE * split_num);
							f_rnd = (RandomAccessFile) d_open_file(PATH2 + "/" + fileName + "." + split_num, real_already_read, this.my_num);
							// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "split file calc(b2):split_num=" + split_num + " real_already_read=" + real_already_read + " already_read=" + already_read);
							// Log.d("NavitMapDownloader", this.my_num + "split file calc(b2):split_num=" + split_num + " real_already_read=" + real_already_read + " already_read=" + already_read);
						}
						else
						{
							f_rnd = (RandomAccessFile) d_open_file(PATH2 + "/" + fileName, already_read, this.my_num);
							// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "split file calc(b1): already_read=" + already_read);
							// Log.d("NavitMapDownloader", this.my_num + "split file calc(b1): already_read=" + already_read);
						}
					}

					if (stop_me)
					{
						// ok we need to be stopped! close all files and end
						d_url_disconnect(c);
						this.running = false;
						mapdownload_error_code_inc();
						break;
					}

					BufferedInputStream bif = d_url_get_bif(c);
					// InputStream bif = (InputStream) d_url_get_bif(c);
					if (bif != null)
					{

						// do the real downloading here
						try
						{
							if (stop_me)
							{
								// ok we need to be stopped! close all files and end
								bif.close();
								d_url_disconnect(c);
								this.running = false;
								mapdownload_error_code_inc();
								break;
							}

							// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "buf avail1=" + bif.available());

							// len1 -> number of bytes actually read
							//while (((len1 = bif.read(buffer)) != -1) && (already_read <= this.end_byte))

							Message msg_prog = null;
							Bundle b_prog = null;

							while ((len1 = bif.read(buffer)) != -1)
							{
								// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + " buf avail2=" + bif.available() + " len1=" + len1);

								if (stop_me)
								{
									// ok we need to be stopped! close all files and end
									bif.close();
									d_url_disconnect(c);
									this.running = false;
									mapdownload_error_code_inc();
									break;
									// return 2;
								}

								if (len1 > 0)
								{
									already_read = already_read + len1;
									read_per_interval = read_per_interval + len1;
									alt_cur++;
									if (alt_progress_update_timestamp + progress_update_intervall < System.currentTimeMillis())
									{
										// ======================== small delay ========================
										// ======================== small delay ========================
										// ======================== small delay ========================
										//										try
										//										{
										//											Thread.sleep(70); // give a little time to catch breath, and write to disk
										//										}
										//										catch (Exception sleep_e)
										//										{
										//											sleep_e.printStackTrace();
										//										}
										// ======================== small delay ========================
										// ======================== small delay ========================
										// ======================== small delay ========================

										alt_cur = 0;
										alt_progress_update_timestamp = System.currentTimeMillis();

										count_read_per_interval++;

										if (count_read_per_interval == 15)
										{
											//											int rate = (int) ((float) read_per_interval / ((float) progress_update_intervall * 10f / 1000f * 1024f));
											//											if (rate < 1)
											//											{
											//												System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + " downloadrate: 0 kbytes/s");
											//											}
											//											else
											//											{
											//												if (this.my_num == 1)
											//												{
											//													System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "+downloadrate:" + rate + " kbytes/s");
											//												}
											//												else if (this.my_num == 2)
											//												{
											//													System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "O  downloadrate:" + rate + " kbytes/s");
											//												}
											//												else
											//												{
											//													System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "M    downloadrate:" + rate + " kbytes/s");
											//												}
											//											}
											read_per_interval = 0L;
											count_read_per_interval = 0;
										}

										msg = handler.obtainMessage();
										b = new Bundle();
										msg.what = 1;

										b.putInt("dialog_num", my_dialog_num);
										b.putString("title", Navit.get_text("Mapdownload")); //TRANS
										// per_second_overall = (float) (already_read - this.start_byte) / (float) ((System.currentTimeMillis() - start_timestamp) / 1000);
										per_second_now = (int) (already_read - this.start_byte_count_now) / (((float) (System.currentTimeMillis() - start_timestamp_count_now)) / 1000.0f);
										// System.out.println("DL:TH=" + (this.my_num - 1) + " psn=" + per_second_now + " " + already_read + " " + this.start_byte_count_now + " " + System.currentTimeMillis() + " " + start_timestamp_count_now + " :" + ((long) (System.currentTimeMillis() - start_timestamp_count_now)));
										this.start_byte_count_now = already_read;
										start_timestamp_count_now = System.currentTimeMillis();

										mapdownload_already_read[this.my_num - 1] = already_read - this.start_byte;
										// mapdownload_byte_per_second_overall[this.my_num - 1] = per_second_overall;

										if (Math.abs(mapdownload_byte_per_second_now[this.my_num - 1] - per_second_now) > 600000)
										{
											mapdownload_byte_per_second_now[this.my_num - 1] = per_second_now;
										}
										else
										{
											mapdownload_byte_per_second_now[this.my_num - 1] = per_second_now + ((mapdownload_byte_per_second_now[this.my_num - 1] - per_second_now) / 2.0f);
										}

										// if download speed seems to be higher than 20MB/sec it must be bogus -> set to 20MB/sec
										if (mapdownload_byte_per_second_now[this.my_num - 1] > 20000000)
										{
											mapdownload_byte_per_second_now[this.my_num - 1] = 20000000;
										}

										//b.putInt("max", (int) (this.end_byte / 1024));
										//b.putInt("cur", (int) ((already_read - this.start_byte) / 1024));
										float f1 = 0;
										long l1 = 0L;
										int k;
										for (k = 0; k < mapdownload_already_read.length; k++)
										{
											l1 = l1 + mapdownload_already_read[k];
											// *long time* // f1 = f1 + mapdownload_byte_per_second_overall[k];
											//
											// *now*
											f1 = f1 + mapdownload_byte_per_second_now[k];
										}
										b.putInt("max", (int) (map_values.est_size_bytes / 1024));
										b.putInt("cur", (int) (l1 / 1024));

										// update progressbar
										int percentage = (int) (((l1 / 1024.0f) / (map_values.est_size_bytes / 1024.0f)) * 100.0f);
										if (percentage > 0)
										{
											msg_prog = new Message();
											b_prog = new Bundle();
											b_prog.putInt("pg", percentage);
											msg_prog.what = 2;
											msg_prog.setData(b_prog);
											ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);
										}

										// update speedbar
										msg_prog = new Message();
										b_prog = new Bundle();
										b_prog.putInt("speed_kb_per_sec", (int) (mapdownload_byte_per_second_now[this.my_num - 1] / 1024.0f));
										b_prog.putInt("threadnum", (this.my_num - 1));
										b_prog.putString("srv", this.this_server_name);
										msg_prog.what = 3;
										msg_prog.setData(b_prog);
										ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);

										kbytes_per_second = formatter.format((f1 / 1024f));
										// kbytes_per_second = formatter.format((per_second_overall / 1024f));
										// bytes_remaining = this.end_byte - already_read;
										bytes_remaining = map_values.est_size_bytes - l1;
										// eta_seconds = (int) ((float) bytes_remaining / (float) per_second_overall);
										eta_seconds = (int) ((float) bytes_remaining / (float) f1);
										if (eta_seconds > 60)
										{
											eta_string = (int) (eta_seconds / 60f) + " m";
										}
										else
										{
											eta_string = eta_seconds + " s";
										}
										//b.putString("text", Navit.get_text("downloading") + ": " + map_values.map_name + "\n" + " " + (int) (already_read / 1024f / 1024f) + "Mb / " + (int) (map_values.est_size_bytes / 1024f / 1024f) + "Mb" + "\n" + " " + kbytes_per_second + "kb/s" + " " + Navit.get_text("ETA") + ": " + eta_string); //TRANS
										b.putString("text", Navit.get_text("downloading") + ": " + map_values.map_name + "\n" + " " + (int) (l1 / 1024f / 1024f) + "Mb / " + (int) (map_values.est_size_bytes / 1024f / 1024f) + "Mb" + "\n" + " " + kbytes_per_second + "kb/s" + " " + Navit.get_text("ETA") + ": " + eta_string); //TRANS
										msg.setData(b);
										//if (this.my_num == num_threads - 1)
										//{
										handler.sendMessage(msg);
										//}

										try
										{
											ZANaviMapDownloaderService.set_noti_text(map_values.map_name + ":" + (int) (l1 / 1024f / 1024f) + "Mb/" + (int) (map_values.est_size_bytes / 1024f / 1024f) + "Mb" + " " + Navit.get_text("ETA") + ": " + eta_string, percentage);
											ZANaviMapDownloaderService.set_large_text(Navit.get_text("downloading") + ": " + map_values.map_name + "\n" + " " + (int) (l1 / 1024f / 1024f) + "Mb / " + (int) (map_values.est_size_bytes / 1024f / 1024f) + "Mb" + "\n" + " " + kbytes_per_second + "kb/s" + " " + Navit.get_text("ETA") + ": " + eta_string);
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}

										//										try
										//										{
										//											Thread.sleep(70); // give a little time to catch breath, and write to disk
										//										}
										//										catch (Exception sleep_e)
										//										{
										//											sleep_e.printStackTrace();
										//										}

										// System.out.println("" + this.my_num + " " + already_read + " - " + (already_read - this.start_byte));
										// System.out.println("+++++++++++++ still downloading +++++++++++++");
									}
									//								if (already_read > this.end_byte)
									//								{
									//									int len2 = len1 - (int) (already_read - this.end_byte);
									//									if (len2 > 0)
									//									{
									//										f_rnd.write(buffer, 0, len2);
									//									}
									//								}
									//								else
									//								{

									// ********
									current_split = 0;
									next_split = 0;
									if (already_read > (MAX_SINGLE_BINFILE_SIZE - 1))
									{
										// split file, we need to compensate
										current_split = (int) ((long) (already_read - len1) / MAX_SINGLE_BINFILE_SIZE);
										next_split = (int) (already_read / MAX_SINGLE_BINFILE_SIZE);

										// System.out.println("DEBUG_MAP_DOWNLOAD::" + "split file, we need to compensate: current_split=" + current_split + " next_split=" + next_split + " already_read=" + already_read + " MAX_SINGLE_BINFILE_SIZE=" + MAX_SINGLE_BINFILE_SIZE + " len1=" + len1);
									}

									if (current_split != next_split)
									{
										int len1_part2 = (int) (already_read % MAX_SINGLE_BINFILE_SIZE);
										int len1_part1 = len1 - len1_part2;
										// part1
										f_rnd.write(buffer, 0, len1_part1);
										// close file
										d_close_file(f_rnd, this.my_num);
										// open next split file (and seek to pos ZERO)
										f_rnd = (RandomAccessFile) d_open_file(PATH2 + "/" + fileName + "." + next_split, 0, this.my_num);
										// part2, only if more than ZERO bytes left to write
										if (len1_part2 > 0)
										{
											f_rnd.write(buffer, len1_part1, len1_part2);
										}
										// System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "next split file: current_split=" + current_split + " next_split=" + next_split + " already_read=" + already_read + " MAX_SINGLE_BINFILE_SIZE=" + MAX_SINGLE_BINFILE_SIZE + " len1=" + len1 + " len1_part2=" + len1_part2 + " len1_part1=" + len1_part1);
									}
									else
									{
										// actually write len1 bytes to output file
										// System.out.println("MultiStreamDownloaderThread " + this.my_num + " XX 011.08 len1=" + len1 + " cur pos=" + f_rnd.getFilePointer());
										// !!!!!! this command can take up to 2 minutes to finish on large files !!!!!!
										f_rnd.write(buffer, 0, len1);
										// System.out.println("MultiStreamDownloaderThread " + this.my_num + " XX 011.09");
									}
									// ********

									//								}
									//									try
									//									{
									//										// System.out.println("" + this.my_num + " pos=" + f_rnd.getFilePointer() + " len=" + f_rnd.length());
									//									}
									//									catch (Exception e)
									//									{
									//										e.printStackTrace();
									//									}
								}
							}

							d_close_file(f_rnd, this.my_num);
							bif.close();
							d_url_disconnect(c);

							mapdownload_byte_per_second_now[this.my_num - 1] = 0;
							// update speedbar
							Message msg_prog77 = new Message();
							Bundle b_prog77 = new Bundle();
							b_prog77.putInt("speed_kb_per_sec", 0);
							b_prog77.putInt("threadnum", (this.my_num - 1));
							msg_prog77.what = 3;
							msg_prog77.setData(b_prog77);
							ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog77);

							// delete an already final filename, first
							//**final_outputFile.delete();
							// rename file to final name
							//**outputFile.renameTo(final_outputFile);

							// delete an already there md5 file, first
							//**File md5_final_filename = new File(Navit.MAPMD5_FILENAME_PATH + map_values.url + ".md5");
							//**md5_final_filename.delete();
							// rename file to final name
							//**File tmp_downloadfile_md5 = new File(Navit.MAPMD5_FILENAME_PATH, MD5_DOWNLOAD_TEMPFILE);
							//**tmp_downloadfile_md5.renameTo(md5_final_filename);

							// remove
							//**NavitMapDownloader.remove_from_cat_file(up_map);
							// remove any duplicates (junk in file)
							//**NavitMapDownloader.remove_from_cat_file_disk_name(final_fileName);
							// add to the catalogue file for downloaded maps
							//**NavitMapDownloader.add_to_cat_file(final_fileName, map_values.url);

							// ------ check if we got enough bytes from the server here! if NOT, then continue downloading and reconnect
							if (already_read < this.end_byte)
							{
								// continue download loop
								System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + " server did NOT send enough bytes! already_read=" + already_read + " this.end_byte=" + this.end_byte);
							}
							else
							{
								// ok downloaded ok, set flag!!
								download_success = true;
								this.running = false;
							}
						}
						catch (IOException e)
						{
							mapdownload_byte_per_second_now[this.my_num - 1] = 0;
							// update speedbar
							Message msg_prog77 = new Message();
							Bundle b_prog77 = new Bundle();
							b_prog77.putInt("speed_kb_per_sec", 0);
							b_prog77.putInt("threadnum", (this.my_num - 1));
							msg_prog77.what = 3;
							msg_prog77.setData(b_prog77);
							ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog77);

							msg = handler.obtainMessage();
							b = new Bundle();
							msg.what = 2;
							b.putInt("dialog_num", my_dialog_num);
							b.putString("text", Navit.get_text("Error downloading map, resuming")); //TRANS
							msg.setData(b);
							handler.sendMessage(msg);

							Log.d("NavitMapDownloader", this.my_num + " Error7: " + e);
							// ******* ********* D/NavitMapDownloader(  266): 1 Error7: java.io.IOException: No space left on device
							System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "IOException11: already_read=" + already_read + " MAX_SINGLE_BINFILE_SIZE=" + MAX_SINGLE_BINFILE_SIZE + " len1=" + len1);

							try
							{
								bif.close();
							}
							catch (IOException e1)
							{
								e1.printStackTrace();
							}
							d_url_disconnect(c);
						}
						catch (Exception e)
						{

							try
							{
								mapdownload_byte_per_second_now[this.my_num - 1] = 0;
								// update speedbar
								Message msg_prog77 = new Message();
								Bundle b_prog77 = new Bundle();
								b_prog77.putInt("speed_kb_per_sec", 0);
								b_prog77.putInt("threadnum", (this.my_num - 1));
								msg_prog77.what = 3;
								msg_prog77.setData(b_prog77);
								ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog77);
							}
							catch (Exception e4)
							{
								e4.printStackTrace();
							}

							try
							{
								bif.close();
							}
							catch (IOException e1)
							{
								e1.printStackTrace();
							}
							d_url_disconnect(c);

							System.out.println("DEBUG_MAP_DOWNLOAD::" + this.my_num + "Exception22: already_read=" + already_read + " MAX_SINGLE_BINFILE_SIZE=" + MAX_SINGLE_BINFILE_SIZE + " len1=" + len1);

							e.printStackTrace();
							if (stop_me)
							{
								// ok we need to be stopped! close all files and end
								this.running = false;
								mapdownload_error_code_inc();
								break;
								// return 2;
							}
						}
					}
					else
					// bif == null
					{
						mapdownload_byte_per_second_now[this.my_num - 1] = 0;
						// update speedbar
						Message msg_prog77 = new Message();
						Bundle b_prog77 = new Bundle();
						b_prog77.putInt("speed_kb_per_sec", 0);
						b_prog77.putInt("threadnum", (this.my_num - 1));
						msg_prog77.what = 3;
						msg_prog77.setData(b_prog77);
						ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog77);

						d_url_disconnect(c);
						try
						{
							// sleep for 2 second
							Thread.sleep(2000);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
					// bif null ------

					if (!download_success)
					{
						try
						{
							mapdownload_byte_per_second_now[this.my_num - 1] = 0;
							// update speedbar
							Message msg_prog77 = new Message();
							Bundle b_prog77 = new Bundle();
							b_prog77.putInt("speed_kb_per_sec", 0);
							b_prog77.putInt("threadnum", (this.my_num - 1));
							msg_prog77.what = 3;
							msg_prog77.setData(b_prog77);
							ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog77);
						}
						catch (Exception ee)
						{
							// catch crash reported in google play
							ee.printStackTrace();
						}

						try
						{
							// sleep for 2 second (also here)
							Thread.sleep(2000);
						}
						catch (Exception ex2)
						{
							ex2.printStackTrace();
						}
					}
				}
				// while -------

				try
				{
					Thread.sleep(150);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				mapdownload_byte_per_second_now[this.my_num - 1] = 0;
				// update speedbar
				Message msg_prog77 = new Message();
				Bundle b_prog77 = new Bundle();
				b_prog77.putInt("speed_kb_per_sec", 0);
				b_prog77.putInt("threadnum", (this.my_num - 1));
				msg_prog77.what = 3;
				msg_prog77.setData(b_prog77);
				ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog77);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			System.out.println("MultiStreamDownloaderThread " + this.my_num + " finished");
		}
	}

	public NavitMapDownloader(Navit main)
	{
		//this.navit_jmain = main;

		if (Navit.FDBL)
		{
			ZANAVI_MAPS_BASE_URL = "http://dlfd.zanavi.cc/data/";
			ZANAVI_MAPS_SEVERTEXT_URL = "http://dlfd.zanavi.cc/server.txt";
		}
	}

	public static void init_maps_without_donate_largemaps()
	{
		if (Navit.Navit_Largemap_DonateVersion_Installed != true)
		{
			z_Planet.est_size_bytes = -2;
			z_Planet.est_size_bytes_human_string = "";
			z_Planet.text_for_select_list = z_Planet.map_name + " " + z_Planet.est_size_bytes_human_string;

			z_USA.est_size_bytes = -2;
			z_USA.est_size_bytes_human_string = "";
			z_USA.text_for_select_list = z_USA.map_name + " " + z_USA.est_size_bytes_human_string;

			z_Europe.est_size_bytes = -2;
			z_Europe.est_size_bytes_human_string = "";
			z_Europe.text_for_select_list = z_Europe.map_name + " " + z_Europe.est_size_bytes_human_string;
		}
	}

	public static void init_cat_file()
	{
		Log.v("NavitMapDownloader", "init_cat_file");

		// read the file from sdcard
		read_cat_file();
		// make a copy
		List<String> temp_list = new ArrayList<String>();
		temp_list.clear();
		Iterator<String> k = map_catalogue.listIterator();

		while (k.hasNext())
		{
			temp_list.add(k.next());
		}

		int temp_list_prev_size = temp_list.size();
		Boolean[] bits = new Boolean[temp_list_prev_size];

		for (int h = 0; h < temp_list_prev_size; h++)
		{
			bits[h] = false;
		}

		System.out.println("CI:Looking for maps in:" + Navit.MAP_FILENAME_PATH);
		ZANaviLogMessages.ap("MapFilenamePath", Navit.MAP_FILENAME_PATH);

		// compare it with directory contents
		File map_dir = new File(Navit.MAP_FILENAME_PATH);
		File map_file_absolute_path = null;
		String dateInUTC = "";
		SimpleDateFormat lv_formatter = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
		lv_formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		if (map_dir.isDirectory())
		{
			String[] files_in_mapdir = map_dir.list();
			if (files_in_mapdir != null)
			{
				for (int i = 0; i < files_in_mapdir.length; i++)
				{
					dateInUTC = "";
					System.out.println("found file in mapdir: " + files_in_mapdir[i]);
					// ignore filename with ":" in them
					if (!files_in_mapdir[i].contains(":"))
					{
						// use only files with ending ".bin"
						if (files_in_mapdir[i].endsWith(".bin"))
						{
							// ignore cat. file itself
							if (!files_in_mapdir[i].equals(CAT_FILE))
							{
								// ignore tmp download file
								if (!files_in_mapdir[i].equals(DOWNLOAD_FILENAME))
								{
									System.out.println("checking file in mapdir: " + files_in_mapdir[i]);
									Boolean found_in_maplist = false;
									Iterator<String> j = temp_list.listIterator();
									int t = 0;

									while (j.hasNext())
									{
										String st = j.next();
										if (st.split(":", 2)[0].equals(files_in_mapdir[i]))
										{
											found_in_maplist = true;
											bits[t] = true;
											System.out.println("found map: t=" + t + " map: " + files_in_mapdir[i]);

											map_file_absolute_path = new File(map_dir + "/" + files_in_mapdir[i]);
											if (map_file_absolute_path.exists())
											{
												Date lastModified = new Date(map_file_absolute_path.lastModified());
												dateInUTC = lv_formatter.format(lastModified);
												System.out.println("found map: st=" + st + " modified=" + dateInUTC);
											}
										}
										t++;
									}

									if (!found_in_maplist)
									{
										// if file is on sdcard but not in maplist
										// then add this line:
										//
										// line=mapfilename on sdcard:mapfilename on server
										if (files_in_mapdir[i].equals("borders.bin"))
										{
											System.out.println("adding to maplist: " + files_in_mapdir[i] + ":" + "borders.bin");
											temp_list.add(files_in_mapdir[i] + ":" + "borders.bin");
										}
										else if (files_in_mapdir[i].equals("coastline.bin"))
										{
											System.out.println("adding to maplist: " + files_in_mapdir[i] + ":" + "coastline.bin");
											temp_list.add(files_in_mapdir[i] + ":" + "coastline.bin");
										}
										else
										{
											System.out.println("adding to maplist: " + files_in_mapdir[i] + ":" + MAP_URL_NAME_UNKNOWN);
											temp_list.add(files_in_mapdir[i] + ":" + MAP_URL_NAME_UNKNOWN);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		// check for all maps that are in the maplist, but are missing from sdcard
		// use prev size, because values have been added to the end of the list!!
		for (int h = 0; h < temp_list_prev_size; h++)
		{
			if (bits[h] == false)
			{
				String unknown_map = temp_list.get(h);
				// check if its already commented out
				if (!unknown_map.startsWith("#"))
				{
					System.out.println("commenting out: h=" + h + " map: " + unknown_map);
					// temp_list.set(h, "#" + unknown_map);
					// to avoid download to wrong filename
					temp_list.set(h, "#################");
				}
			}
		}

		// use the corrected copy
		map_catalogue.clear();
		Iterator<String> m = temp_list.listIterator();

		while (m.hasNext())
		{
			map_catalogue.add(m.next());
		}

		// write the corrected file back to sdcard
		write_cat_file();
	}

	public static int cat_file_maps_have_installed_any()
	{
		Iterator<String> k = map_catalogue.listIterator();
		int ret = 0;
		while (k.hasNext())
		{
			String m = k.next();

			if (!m.equals("borders.bin:borders.bin"))
			{
				// System.out.println("MMMM=" + m);
				ret++;
			}
		}

		return ret;
	}

	public static void init_cat_file_maps_timestamps()
	{
		Log.v("NavitMapDownloader", "init_cat_file_maps_timestamps");

		map_catalogue_date.clear();
		// make a copy of current map_catalogue
		List<String> temp_list = new ArrayList<String>();
		temp_list.clear();
		Iterator<String> k = map_catalogue.listIterator();
		while (k.hasNext())
		{
			temp_list.add(k.next());
		}
		int temp_list_prev_size = temp_list.size();
		Boolean[] bits = new Boolean[temp_list_prev_size];
		for (int h = 0; h < temp_list_prev_size; h++)
		{
			bits[h] = false;
		}
		// compare it with directory contents
		File map_dir = new File(Navit.MAP_FILENAME_PATH);
		File map_file_absolute_path = null;
		String dateInUTC = "";
		SimpleDateFormat lv_formatter = new SimpleDateFormat("yyyyMMddHHmm", Locale.US);
		lv_formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		if (map_dir.isDirectory())
		{
			String[] files_in_mapdir = map_dir.list();
			if (files_in_mapdir != null)
			{
				for (int i = 0; i < files_in_mapdir.length; i++)
				{
					dateInUTC = "";
					System.out.println("found file in mapdir: " + files_in_mapdir[i]);
					// ignore filename with ":" in them
					if (!files_in_mapdir[i].contains(":"))
					{
						// use only files with ending ".bin"
						if (files_in_mapdir[i].endsWith(".bin"))
						{
							// ignore cat. file itself
							if (!files_in_mapdir[i].equals(CAT_FILE))
							{
								// ignore tmp download file
								if (!files_in_mapdir[i].equals(DOWNLOAD_FILENAME))
								{
									// ignore borders.bin
									if (!files_in_mapdir[i].equals("borders.bin"))
									{
										System.out.println("checking file in mapdir: " + files_in_mapdir[i]);
										Boolean found_in_maplist = false;
										Iterator<String> j = temp_list.listIterator();
										int t = 0;
										while (j.hasNext())
										{
											String st = j.next();
											if (st.split(":", 2)[0].equals(files_in_mapdir[i]))
											{
												found_in_maplist = true;
												bits[t] = true;
												System.out.println("found map: t=" + t + " map: " + files_in_mapdir[i]);

												map_file_absolute_path = new File(map_dir + "/" + files_in_mapdir[i]);
												if (map_file_absolute_path.exists())
												{
													Date lastModified = new Date(map_file_absolute_path.lastModified());
													dateInUTC = lv_formatter.format(lastModified);
													System.out.println("found map: st=" + st + " modified=" + dateInUTC);
													map_catalogue_date.add(dateInUTC + ":" + st.split(":", 2)[1]);
												}
											}
											t++;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static void read_cat_file()
	{
		Log.v("NavitMapDownloader", "read_cat_file");

		//Get the text file
		File file = new File(Navit.CFG_FILENAME_PATH + CAT_FILE);

		//Read text from file
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file), 1000);
			map_catalogue.clear();
			String line;
			while ((line = br.readLine()) != null)
			{
				// line=mapfilename on sdcard:mapfilename on server
				// or
				// line=#comment
				if (!line.startsWith("#"))
				{
					if (line != null)
					{
						if (line.startsWith("coastline.bin:"))
						{
							line = "coastline.bin:coastline.bin";
						}
						else if (line.startsWith("borders.bin:"))
						{
							line = "borders.bin:borders.bin";
						}
						map_catalogue.add(line);
						System.out.println("line=" + line);
					}
				}
			}

			if (br != null)
			{
				br.close();
			}
		}
		catch (IOException e)
		{
		}

		Navit.send_installed_maps_to_plugin();
	}

	@SuppressLint("NewApi")
	public static void write_cat_file()
	{
		Log.v("NavitMapDownloader", "write_cat_file");

		//Get the text file
		File file = new File(Navit.CFG_FILENAME_PATH + CAT_FILE);
		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;
		try
		{
			fOut = new FileOutputStream(file);
			osw = new OutputStreamWriter(fOut);
			osw.write(MAP_CAT_HEADER + "\n");

			Iterator<String> i = map_catalogue.listIterator();
			while (i.hasNext())
			{
				String st = i.next();
				osw.write(st + "\n");
				System.out.println("write line=" + st);
			}
			osw.close();
			fOut.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}

		Navit.send_installed_maps_to_plugin();

		try
		{
			if (!Navit.have_maps_installed())
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

				Navit.Global_Navit_Object.show_case_001();

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
	}

	public static void add_to_cat_file(String disk_name, String server_name)
	{
		Log.v("NavitMapDownloader", "add_to_cat_file");

		System.out.println("adding: " + disk_name + ":" + server_name);
		map_catalogue.add(disk_name + ":" + server_name);
		write_cat_file();
	}

	public static void remove_from_cat_file(String disk_name, String server_name)
	{
		System.out.println("removing: " + disk_name + ":" + server_name);
		map_catalogue.remove(disk_name + ":" + server_name);
		write_cat_file();
	}

	public static void remove_from_cat_file_disk_name(String disk_name)
	{
		System.out.println("removing: " + disk_name);

		int find_count = 0;
		String[] saved_lines = new String[50];
		for (int x = 0; x < 50; x++)
		{
			saved_lines[x] = new String();

		}

		Iterator<String> i = map_catalogue.listIterator();
		while (i.hasNext())
		{
			String st = i.next();
			if (st.split(":", 2)[0].equals(disk_name))
			{
				saved_lines[find_count] = st;
				find_count++;
			}
		}

		if (find_count == 0)
		{
			// clean up
			saved_lines = null;
			System.out.println("nothing to delete");
			return;
		}
		for (int x = 0; x < find_count; x++)
		{
			System.out.println("removing: " + saved_lines[x]);
			map_catalogue.remove(saved_lines[x]);
		}
		// clean up
		saved_lines = null;
		// write file
		write_cat_file();
	}

	public static void remove_from_cat_file(String full_line)
	{
		System.out.println("removing: " + full_line);
		map_catalogue.remove(full_line);
		write_cat_file();
	}

	public static Boolean is_in_cat_file(String disk_name, String server_name)
	{
		return map_catalogue.contains(disk_name + ":" + server_name);
	}

	public static String is_in_cat_file_disk_name(String name)
	{
		Log.v("NavitMapDownloader", "is_in_cat_file_disk_name");

		String is_here = null;
		Iterator<String> i = map_catalogue.listIterator();
		while (i.hasNext())
		{
			String st = i.next();
			if (st.split(":", 2)[0].equals(name))
			{
				// map is here
				is_here = st;
				return is_here;
			}
		}
		return is_here;
	}

	public static String is_in_cat_file_server_name(String name)
	{
		Log.v("NavitMapDownloader", "is_in_cat_file_server_name");

		String is_here = null;
		Iterator<String> i = map_catalogue.listIterator();
		while (i.hasNext())
		{
			String st = i.next();
			if (!st.startsWith("#"))
			{
				if (st.split(":", 2)[1].equals(name))
				{
					// map is here
					is_here = st;
					return is_here;
				}
			}
		}
		return is_here;
	}

	public static int find_lowest_mapnumber_free()
	{
		int ret = MAP_MAX_FILES;
		String tmp_name = null;
		for (int j = 1; j < MAP_MAX_FILES + 1; j++)
		{
			tmp_name = String.format(MAP_FILENAME_BASE, j);
			Iterator<String> i = map_catalogue.listIterator();
			Boolean is_here = false;
			while (i.hasNext())
			{
				String st = i.next();
				if (st.split(":", 2)[0].equals(tmp_name))
				{
					// map is here
					is_here = true;
				}
			}
			if (!is_here)
			{
				ret = j;
				return j;
			}
		}
		return ret;
	}

	public static void init()
	{
		// need only init once
		if (already_inited)
		{
			return;
		}

		//String[] temp_m = new String[MAX_MAP_COUNT];
		String[] temp_ml = new String[MAX_MAP_COUNT];
		int[] temp_i = new int[MAX_MAP_COUNT];
		Boolean[] already_added = new Boolean[z_OSM_MAPS.length];
		int cur_continent = -1;
		int count = 0;
		Boolean last_was_continent = false;
		int last_continent_id = -1;
		Log.v("NavitMapDownloader", "init maps");
		for (int i = 0; i < z_OSM_MAPS.length; i++)
		{
			already_added[i] = false;
		}
		for (int i = 0; i < z_OSM_MAPS.length; i++)
		{
			//Log.v("NavitMapDownloader", "i=" + i);
			// look for continents only
			if (z_OSM_MAPS[i].is_continent)
			{
				if (!((last_was_continent) && (last_continent_id == z_OSM_MAPS[i].continent_id)))
				{
					if (count > 0)
					{
						// add a break into list
						//temp_m[count] = "*break*";
						temp_ml[count] = "======";
						temp_i[count] = -1;
						count++;
					}
				}
				last_was_continent = true;
				last_continent_id = z_OSM_MAPS[i].continent_id;

				cur_continent = z_OSM_MAPS[i].continent_id;
				if (z_OSM_MAPS[i].est_size_bytes == -2)
				{
					// only dummy entry to have a nice structure
					temp_ml[count] = z_OSM_MAPS[i].text_for_select_list;
					temp_i[count] = -1;
				}
				else
				{
					temp_ml[count] = z_OSM_MAPS[i].text_for_select_list;
					temp_i[count] = i;
				}
				count++;
				already_added[i] = true;
				Boolean skip = false;
				try
				{
					// get next item
					if ((z_OSM_MAPS[i + 1].is_continent) && (cur_continent == z_OSM_MAPS[i + 1].continent_id))
					{
						skip = true;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				// only if not 2 contitents following each other
				if (!skip)
				{
					for (int j = 0; j < z_OSM_MAPS.length; j++)
					{
						// if (already_added[j] == null)
						if (!already_added[j])
						{
							// look for maps in that continent
							if ((z_OSM_MAPS[j].continent_id == cur_continent) && (!z_OSM_MAPS[j].is_continent))
							{
								//Log.v("NavitMapDownloader", "found map=" + j + " c=" + cur_continent);
								// add this map.
								//temp_m[count] = z_OSM_MAPS[j].map_name;
								temp_ml[count] = " * " + z_OSM_MAPS[j].text_for_select_list;
								temp_i[count] = j;
								count++;
								already_added[j] = true;
							}
						}
					}
				}
			}
			else
			{
				last_was_continent = false;
			}
		}
		// add the rest of the list (dont have a continent)
		cur_continent = 9999; // unknown
		int found = 0;
		for (int i = 0; i < z_OSM_MAPS.length; i++)
		{
			if (!already_added[i])
			{
				if (found == 0)
				{
					found = 1;
					// add a break into list
					//temp_m[count] = "*break*";
					temp_ml[count] = "======";
					temp_i[count] = -1;
					count++;
				}

				//Log.v("NavitMapDownloader", "found map(loose)=" + i + " c=" + cur_continent);
				// add this map.
				//temp_m[count] = z_OSM_MAPS[i].map_name;
				temp_ml[count] = " # " + z_OSM_MAPS[i].text_for_select_list;
				temp_i[count] = i;
				count++;
				already_added[i] = true;
			}
		}

		Log.e("NavitMapDownloader", "count=" + count);
		Log.e("NavitMapDownloader", "size1 " + z_OSM_MAPS.length);
		//Log.e("NavitMapDownloader", "size2 " + temp_m.length);
		Log.e("NavitMapDownloader", "size3 " + temp_ml.length);

		//OSM_MAP_NAME_LIST = new String[count];
		OSM_MAP_NAME_LIST_inkl_SIZE_ESTIMATE = new String[count];
		OSM_MAP_NAME_ORIG_ID_LIST = new int[count];

		for (int i = 0; i < count; i++)
		{
			//OSM_MAP_NAME_LIST[i] = temp_m[i];
			OSM_MAP_NAME_ORIG_ID_LIST[i] = temp_i[i];
			OSM_MAP_NAME_LIST_inkl_SIZE_ESTIMATE[i] = temp_ml[i];
		}

		already_inited = true;
	}

	public static void init_ondisk_maps()
	{
		Log.v("NavitMapDownloader", "init ondisk maps");

		OSM_MAP_NAME_LIST_ondisk = new String[map_catalogue.size()];
		OSM_MAP_NAME_ondisk_ORIG_LIST = new String[map_catalogue.size()];

		Iterator<String> i = map_catalogue.listIterator();
		int c = 0;
		String t;
		while (i.hasNext())
		{
			String st = i.next();

			if (!st.startsWith("#"))
			{
				// full line <on disk name:server filename>
				OSM_MAP_NAME_ondisk_ORIG_LIST[c] = st;
				// server file name
				OSM_MAP_NAME_LIST_ondisk[c] = null;
				try
				{
					t = st.split(":", 2)[1];

					for (int j = 0; j < z_OSM_MAPS.length; j++)
					{
						// Log.v("NavitMapDownloader", "u=" + z_OSM_MAPS[j].url + " m=" + z_OSM_MAPS[j].url + " t=" + t + " st=" + st);
						if (z_OSM_MAPS[j].url.equals(t))
						{
							OSM_MAP_NAME_LIST_ondisk[c] = z_OSM_MAPS[j].map_name;
						}
					}

					if (OSM_MAP_NAME_LIST_ondisk[c] == null)
					{
						// for unkown maps
						OSM_MAP_NAME_LIST_ondisk[c] = st.split(":", 2)[0] + MAP_DISK_NAME_UNKNOWN;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			c++;
		}
	}

	public String find_file_on_other_mapserver(List<String> map_servers_list, String md5_sum, zanavi_osm_map_values map_values, int map_num3)
	{
		System.out.println("find_file_on_other_mapserver:first server name=" + map_servers_list);

		String other_server_name = d_get_servername(false, map_values);
		int i = 0;
		for (i = 0; i < 15; i++)
		{
			System.out.println("find_file_on_other_mapserver:found other mapserver (" + i + "): " + other_server_name);
			if ((other_server_name == null) || (map_servers_list.contains(other_server_name)))
			{
				// try again
				try
				{
					Thread.sleep(940);
				}
				catch (InterruptedException e)
				{
				}
				other_server_name = d_get_servername(false, map_values);
			}
			else
			{
				break;
			}
		}

		if ((other_server_name == null) || (map_servers_list.contains(other_server_name)))
		{
			System.out.println("find_file_on_other_mapserver:no other server found");
			return null;
		}

		String md5_server = d_get_md5_from_server(map_values, other_server_name, map_num3);
		if ((md5_server == null) || (!md5_server.equals(md5_sum)))
		{
			System.out.println("find_file_on_other_mapserver:wrong md5 on " + other_server_name);
			return null;
		}

		System.out.println("find_file_on_other_mapserver:found other mapserver: " + other_server_name);
		return other_server_name;
	}

	public int download_osm_map(Handler handler, zanavi_osm_map_values map_values, int map_num3)
	{
		int exit_code = 1;

		int my_dialog_num = Navit.MAPDOWNLOAD_PRI_DIALOG;
		if (map_num3 == Navit.MAP_NUM_PRIMARY)
		{
			my_dialog_num = Navit.MAPDOWNLOAD_PRI_DIALOG;
		}
		else if (map_num3 == Navit.MAP_NUM_SECONDARY)
		{
			my_dialog_num = Navit.MAPDOWNLOAD_SEC_DIALOG;
		}

		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		msg.what = 1;
		b.putInt("max", 20); // use a dummy number here
		b.putInt("cur", 0);
		b.putInt("dialog_num", my_dialog_num);
		b.putString("title", Navit.get_text("Mapdownload")); //TRANS
		b.putString("text", Navit.get_text("downloading") + ": " + map_values.map_name); //TRANS
		msg.setData(b);
		handler.sendMessage(msg);

		// output filename
		String PATH = Navit.MAP_FILENAME_PATH;
		String PATH2 = Navit.CFG_FILENAME_PATH;
		String fileName = DOWNLOAD_FILENAME;
		String final_fileName = "xxx";

		Boolean mode_update = false;
		String up_map = null;

		if (map_values.url.equals("borders.bin"))
		{
			final_fileName = MAP_FILENAME_BORDERS;
			mode_update = true;
			up_map = is_in_cat_file_server_name("borders.bin");
		}
		else if (map_values.url.equals("coastline.bin"))
		{
			final_fileName = MAP_FILENAME_COASTLINE;
			mode_update = true;
			up_map = is_in_cat_file_server_name("coastline.bin");
		}
		else
		{
			// is it an update?
			up_map = is_in_cat_file_server_name(map_values.url);
			if (up_map == null)
			{
				final_fileName = String.format(MAP_FILENAME_BASE, find_lowest_mapnumber_free());
			}
			else
			{
				final_fileName = up_map.split(":", 2)[0];
				mode_update = true;
			}
		}

		System.out.println("update=" + mode_update);
		System.out.println("final_fileName=" + final_fileName);
		System.out.println("up_map=" + up_map);

		try
		{
			// show download actionbar icon
			Message msg2 = Navit.Navit_progress_h.obtainMessage();
			Bundle b2 = new Bundle();
			msg2.what = 23;
			msg2.setData(b2);
			Navit.Navit_progress_h.sendMessage(msg2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String this_server_name = null;
		String md5_server = null;

		int server_err = 0;
		int server_retries = 0;
		int server_max_retries = 8;

		while (server_retries < server_max_retries)
		{
			Log.d("NavitMapDownloader", "init:try #" + server_retries);

			this_server_name = d_get_servername(true, map_values);
			if (this_server_name == null)
			{
				Log.d("NavitMapDownloader", "init:try #" + server_retries + " srvname=null");

				server_err = 1;
				server_retries++;
				continue;
			}

			md5_server = d_get_md5_from_server(map_values, this_server_name, map_num3);
			if (md5_server == null)
			{
				Log.d("NavitMapDownloader", "init:try #" + server_retries + " md5=null" + " srvname=" + this_server_name);

				server_err = 1;
				server_retries++;
				continue;
			}
			else
			{
				Log.d("NavitMapDownloader", "init:try #" + server_retries + " md5=" + md5_server + " srvname=" + this_server_name);

				server_err = 0;
				break;
			}

		}

		if (server_err == 1)
		{
			//if (this_server_name == null)
			//{
			msg = handler.obtainMessage();
			b = new Bundle();
			msg.what = 2;
			b.putInt("dialog_num", my_dialog_num);
			b.putString("text", Navit.get_text("Error downloading map!")); //TRANS
			msg.setData(b);
			handler.sendMessage(msg);

			return 1;
			//}
		}

		// on disk md5 can be "null" , when downloading new map
		String md5_disk = d_get_md5_from_disk(map_values, this_server_name, map_num3);

		if (d_match_md5sums(md5_disk, md5_server))
		{
			// ok we have a match, no need to update!
			msg = handler.obtainMessage();
			b = new Bundle();
			msg.what = 2;
			b.putInt("dialog_num", my_dialog_num);
			b.putString("text", Navit.get_text("Map already up to date")); //TRANS
			msg.setData(b);
			handler.sendMessage(msg);

			Log.d("NavitMapDownloader", "MD5 matches, no need to update map");
			return 11;
		}

		long real_file_size = d_get_real_download_filesize(map_values, this_server_name, map_num3);
		if (real_file_size <= 0)
		{
			msg = handler.obtainMessage();
			b = new Bundle();
			msg.what = 2;
			b.putInt("dialog_num", my_dialog_num);
			b.putString("text", Navit.get_text("Error downloading map!")); //TRANS
			msg.setData(b);
			handler.sendMessage(msg);

			return 1;
		}
		map_values.est_size_bytes = real_file_size;

		// check if file fits onto maps dir
		// check if file fits onto maps dir
		try
		{
			long avail_space = NavitAvailableSpaceHandler.getExternalAvailableSpaceInBytes(Navit.MAP_FILENAME_PATH);
			System.out.println("avail space=" + avail_space + " map size=" + real_file_size);
			if (avail_space <= real_file_size)
			{
				Message msg2 = Navit.Navit_progress_h.obtainMessage();
				Bundle b2 = new Bundle();
				msg2.what = 17;
				msg2.setData(b2);
				Navit.Navit_progress_h.sendMessage(msg2);
			}
		}
		catch (Exception e)
		{
			// just in case the device does not support this,
			// we catch the exception and continue with the download
			e.printStackTrace();
		}
		// check if file fits onto maps dir
		// check if file fits onto maps dir

		int num_threads = 1;
		long bytes_diff = 0L;
		long bytes_leftover = 0;
		if (map_values.est_size_bytes < 1000000)
		{
			num_threads = 1;
			map_servers_used.clear();
			map_servers_used.add(this_server_name);
			bytes_diff = map_values.est_size_bytes;
		}
		else
		{
			num_threads = MULTI_NUM_THREADS;
			int num_threads2 = 0;
			map_servers_used.clear();
			String new_map_server = null;
			int k2 = 0;
			System.out.println("find mapservers:" + "================ START ===================");
			for (k2 = 0; k2 < num_threads; k2++)
			{
				System.out.println("find mapservers:" + k2 + "/" + num_threads + " ==================");

				if (k2 == 0)
				{
					// first thread always uses this server name
					new_map_server = this_server_name;
					map_servers_used.add(new_map_server);
				}
				else
				{
					new_map_server = find_file_on_other_mapserver(map_servers_used, md5_server, map_values, map_num3);
					if (new_map_server != null)
					{
						map_servers_used.add(new_map_server);
					}
				}

				if (new_map_server != null)
				{
					num_threads2++;
				}
			}
			num_threads = num_threads2;

			System.out.println("find mapservers:" + "================  END  ===================");

			bytes_diff = (long) (map_values.est_size_bytes / num_threads);
			if (bytes_diff * num_threads < map_values.est_size_bytes)
			{
				bytes_leftover = map_values.est_size_bytes - (bytes_diff * num_threads);
				System.out.println("bytes_leftover=" + bytes_leftover);
			}
			//}

			// bytes_diff is the part size in bytes!! but you must read from (start) to (bytes_diff - 1)!!
		}

		// stupid workaround to have this value available :-(
		MULTI_NUM_THREADS_LOCAL = num_threads;

		System.out.println("num_threads=" + num_threads + " bytes_diff=" + bytes_diff);

		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "num_threads = " + num_threads);
		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "map_servers_used = " + map_servers_used);

		Boolean split_mapfile = false;
		int num_splits = 0;
		// check if we need to split the file into pieces
		if (map_values.est_size_bytes > MAX_SINGLE_BINFILE_SIZE)
		{
			split_mapfile = true;
			num_splits = (int) ((map_values.est_size_bytes - 1) / MAX_SINGLE_BINFILE_SIZE);
		}
		System.out.println("split_mapfile=" + split_mapfile);
		System.out.println("num_splits=" + num_splits);
		// check if we need to split the file into pieces

		File file99 = new File(PATH2);
		File outputFile = new File(file99, fileName);
		outputFile.delete();

		for (int jkl = 1; jkl < 51; jkl++)
		{
			File outputFileSplit = new File(file99, fileName + "." + String.valueOf(jkl));
			System.out.println("delete:" + file99 + "/" + fileName + "." + String.valueOf(jkl));
			outputFileSplit.delete();
		}

		// -- DISABLED --
		// -- DISABLED --
		//		// pre create the big file
		//		msg = handler.obtainMessage();
		//		b = new Bundle();
		//		msg.what = 1;
		//		b.putInt("max", 20); // use a dummy number here
		//		b.putInt("cur", 0);
		//		b.putInt("dialog_num", my_dialog_num);
		//		b.putString("title", Navit.get_text("Mapdownload")); //TRANS
		//		b.putString("text", Navit.get_text("Creating outputfile, long time")); //TRANS
		//		msg.setData(b);
		//		handler.sendMessage(msg);
		//
		//		d_pre_create_file(PATH2 + fileName, map_values.est_size_bytes, handler, my_dialog_num);
		// -- DISABLED --
		// -- DISABLED --

		//
		//
		MultiStreamDownloaderThread[] m = new MultiStreamDownloaderThread[num_threads];
		int k;
		mapdownload_error_code_clear();
		mapdownload_already_read = new long[num_threads];
		mapdownload_byte_per_second_overall = new float[num_threads];
		mapdownload_byte_per_second_now = new float[num_threads];
		for (k = 0; k < num_threads; k++)
		{
			mapdownload_already_read[k] = 0;
			mapdownload_byte_per_second_overall[k] = 0;
			mapdownload_byte_per_second_now[k] = 0;
		}

		for (k = 0; k < num_threads; k++)
		{
			// enale speedbar(s)
			// System.out.println("SPB:enable:#" + k);

			Message msg_prog = new Message();
			Bundle b_prog = new Bundle();
			b_prog.putInt("speed_kb_per_sec", -2);
			b_prog.putInt("threadnum", k);
			msg_prog.what = 3;
			msg_prog.setData(b_prog);
			ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);
		}

		// start downloader threads here --------------------------
		// start downloader threads here --------------------------

		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download start");
		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "map name = " + map_values.map_name);
		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "map size = " + map_values.est_size_bytes);

		long download_start_time = System.currentTimeMillis();

		String new_map_server = null;
		for (k = 0; k < num_threads; k++)
		{
			new_map_server = map_servers_used.get(k);

			if (new_map_server != null)
			{
				if (k == (num_threads - 1))
				{
					m[k] = new MultiStreamDownloaderThread(num_threads, handler, map_values, map_num3, k + 1, PATH, PATH2, fileName, final_fileName, new_map_server, up_map, bytes_diff * k, map_values.est_size_bytes - 1);
				}
				else
				{
					m[k] = new MultiStreamDownloaderThread(num_threads, handler, map_values, map_num3, k + 1, PATH, PATH2, fileName, final_fileName, new_map_server, up_map, bytes_diff * k, (bytes_diff * (k + 1)) - 1);
				}
				m[k].start();
			}
		}

		// wait for downloader threads to finish --------------------------
		for (k = 0; k < num_threads; k++)
		{
			try
			{
				m[k].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		long download_end_time = System.currentTimeMillis();
		long download_rate_avg = bytes_per_second_calc((download_end_time - download_start_time), map_values.est_size_bytes);

		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "map name = " + map_values.map_name);
		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "map size = " + map_values.est_size_bytes);

		if (mapdownload_error_code > 0)
		{
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download end [*ERROR*]");
			int k3;
			long l1 = 0;
			for (k3 = 0; k3 < mapdownload_already_read.length; k3++)
			{
				l1 = l1 + mapdownload_already_read[k3];
			}

			download_rate_avg = bytes_per_second_calc((download_end_time - download_start_time), l1);
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "downloaded [bytes] = " + l1);
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "downloaded [seconds] = " + ((download_end_time - download_start_time) / 1000));
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download rate average [bytes/second] = " + download_rate_avg);
		}
		else
		{
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download end [OK]");
			int k3;
			long l1 = 0;
			for (k3 = 0; k3 < mapdownload_already_read.length; k3++)
			{
				l1 = l1 + mapdownload_already_read[k3];
			}
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "downloaded [bytes] = " + l1);
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "downloaded [seconds] = " + ((download_end_time - download_start_time) / 1000));
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "download rate average [bytes/second] = " + download_rate_avg);
		}

		if (mapdownload_error_code > 0)
		{
			mapdownload_error_code_clear();
			return 97;
		}
		//
		//
		// calc md5sum on device on print it to STDOUT
		//if (!split_mapfile)
		//{

		// set progressbar to 100%
		Message msg_prog1 = new Message();
		Bundle b_prog1 = new Bundle();
		b_prog1.putInt("pg", 100);
		msg_prog1.what = 2;
		msg_prog1.setData(b_prog1);
		ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog1);

		for (k = 0; k < num_threads; k++)
		{
			// update speedbar
			Message msg_prog = new Message();
			Bundle b_prog = new Bundle();
			b_prog.putInt("speed_kb_per_sec", 0);
			b_prog.putInt("threadnum", k);
			msg_prog.what = 3;
			msg_prog.setData(b_prog);
			ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);
		}

		try
		{
			ZANaviMapDownloaderService.set_noti_text(Navit.get_text("checking map ..."), 0);
			ZANaviMapDownloaderService.set_large_text(Navit.get_text("checking map ..."));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("MD5 ok **start*");
		String md5sum_local_calculated = calc_md5sum_on_device(handler, my_dialog_num, map_values.est_size_bytes);
		if (!d_match_md5sums(md5sum_local_calculated, md5_server))
		{
			// some problem with download
			msg = handler.obtainMessage();
			b = new Bundle();
			msg.what = 2;
			b.putInt("dialog_num", my_dialog_num);
			b.putString("text", Navit.get_text("MD5 mismatch")); //TRANS
			msg.setData(b);
			handler.sendMessage(msg);

			outputFile.delete();
			File tmp_downloadfile_md5 = new File(Navit.MAPMD5_FILENAME_PATH, MD5_DOWNLOAD_TEMPFILE);
			tmp_downloadfile_md5.delete();

			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "md5 mismatch server=" + md5_server + " local=" + md5sum_local_calculated);

			Log.d("NavitMapDownloader", "MD5 mismatch!!");
			System.out.println("MD5 mismatch ######");
			return 12;
		}
		else
		{
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "md5 OK server=" + md5_server + " local=" + md5sum_local_calculated);

			Log.d("NavitMapDownloader", "MD5 ok");
			System.out.println("MD5 ok ******");
		}
		System.out.println("MD5 ok **end*");
		//}
		//
		File file = new File(PATH);
		File final_outputFile = new File(file, final_fileName);
		// delete an already final filename, first
		final_outputFile.delete();
		// delete split files
		for (int jkl = 1; jkl < 51; jkl++)
		{
			File outputFileSplit = new File(file, final_fileName + "." + String.valueOf(jkl));
			System.out.println("delete final filename:" + file + "/" + final_fileName + "." + String.valueOf(jkl));
			outputFileSplit.delete();
		}
		// rename file to final name
		outputFile.renameTo(final_outputFile);

		ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "final file name =" + final_outputFile.getAbsolutePath());

		System.out.println("rename1:" + outputFile + " to:" + final_outputFile);
		if (split_mapfile)
		{
			for (int jkl = 1; jkl < (num_splits + 1); jkl++)
			{
				File outputFileSplit = new File(file, final_fileName + "." + String.valueOf(jkl));
				File outputFileSplitSrc = new File(file99, fileName + "." + String.valueOf(jkl));
				System.out.println("rename2:" + outputFileSplitSrc + " to:" + outputFileSplit);
				outputFileSplitSrc.renameTo(outputFileSplit);
			}
		}

		// delete an already there md5 file, first
		File md5_final_filename = new File(Navit.MAPMD5_FILENAME_PATH + map_values.url + ".md5");
		md5_final_filename.delete();
		// rename file to final name
		File tmp_downloadfile_md5 = new File(Navit.MAPMD5_FILENAME_PATH, MD5_DOWNLOAD_TEMPFILE);
		tmp_downloadfile_md5.renameTo(md5_final_filename);

		// ------------ activate screen and CPU (and WLAN) -- before index download starts ---------------------
		// ------------ activate screen and CPU (and WLAN) -- before index download starts ---------------------
		final Thread thread_for_download_wakelock = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					if (Navit.wl != null)
					{
						Navit.wl.acquire();
						Log.e("Navit", "WakeLock: acquire 2a");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					// sleep for 10 seconds
					Thread.sleep(10000);
				}
				catch (Exception e)
				{
				}

				try
				{
					if (Navit.wl != null)
					{
						if (Navit.wl.isHeld())
						{
							Navit.wl.release();
							Log.e("Navit", "WakeLock: release 3a");
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		thread_for_download_wakelock.start();
		// ------------ activate screen and CPU (and WLAN) -- before index download starts ---------------------
		// ------------ activate screen and CPU (and WLAN) -- before index download starts ---------------------

		// don't try to download index file for coastline and border maps
		if ((!final_fileName.equals(MAP_FILENAME_BORDERS)) && (!final_fileName.equals(MAP_FILENAME_COASTLINE)))
		{
			// ------------ download idx file ------------
			// ------------ download idx file ------------
			zanavi_osm_map_values z_dummy_for_idx = new zanavi_osm_map_values("index file", map_values.url + ".idx", 400000000L, false, 3);
			ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "idx download start");

			boolean index_file_download = true;
			Log.d("NavitMapDownloader", "index_file_download(a)=" + index_file_download);

			final int index_download_max_retries = 10;
			int index_download_retries = 1;
			while (index_download_retries < index_download_max_retries) // while-loop until index is downloaded -------------------------
			{
				System.out.println("index download -> enter retry loop: " + index_download_retries + "/" + index_download_max_retries);

				long real_file_size_idx = d_get_real_download_filesize(z_dummy_for_idx, this_server_name, map_num3);
				ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "idx file size = " + real_file_size_idx);

				if (real_file_size_idx <= 0)
				{
					msg = handler.obtainMessage();
					b = new Bundle();
					msg.what = 2;
					b.putInt("dialog_num", my_dialog_num);
					b.putString("text", Navit.get_text("Error downloading index!")); //TRANS
					msg.setData(b);
					handler.sendMessage(msg);

					index_file_download = false;
				}
				else
				{
					z_dummy_for_idx.est_size_bytes = real_file_size_idx;
				}

				num_threads = 1;
				bytes_leftover = 0;
				bytes_diff = z_dummy_for_idx.est_size_bytes;

				Message msg_idx = handler.obtainMessage();
				Bundle b_idx = new Bundle();
				msg_idx.what = 1;
				b_idx.putInt("max", (int) (z_dummy_for_idx.est_size_bytes / 1024)); // use a dummy number here
				b_idx.putInt("cur", 0);
				b_idx.putInt("dialog_num", my_dialog_num);
				b_idx.putString("title", Navit.get_text("Index download")); //TRANS
				b_idx.putString("text", Navit.get_text("downloading indexfile")); //TRANS
				msg_idx.setData(b);
				handler.sendMessage(msg_idx);

				MultiStreamDownloaderThread[] m_idx = new MultiStreamDownloaderThread[num_threads];
				int k_idx;

				mapdownload_error_code_clear();
				mapdownload_already_read = new long[num_threads];
				mapdownload_byte_per_second_overall = new float[num_threads];
				mapdownload_byte_per_second_now = new float[num_threads];
				for (k_idx = 0; k_idx < num_threads; k_idx++)
				{
					mapdownload_already_read[k_idx] = 0;
					mapdownload_byte_per_second_overall[k_idx] = 0;
					mapdownload_byte_per_second_now[k_idx] = 0;
				}

				// start downloader threads here --------------------------
				// start downloader threads here --------------------------
				for (k_idx = 0; k_idx < num_threads; k_idx++)
				{
					if (k_idx == (num_threads - 1))
					{
						m_idx[k_idx] = new MultiStreamDownloaderThread(num_threads, handler, z_dummy_for_idx, map_num3, k_idx + 1, PATH, PATH2, fileName + ".idx", final_fileName + ".idx", this_server_name, "xyzdummydummy", bytes_diff * k_idx, z_dummy_for_idx.est_size_bytes);
					}
					else
					{
						m_idx[k_idx] = new MultiStreamDownloaderThread(num_threads, handler, z_dummy_for_idx, map_num3, k_idx + 1, PATH, PATH2, fileName + ".idx", final_fileName + ".idx", this_server_name, "xyzdummydummy", bytes_diff * k_idx, bytes_diff * (k_idx + 1));
					}
					m_idx[k_idx].start();
				}

				// wait for downloader threads to finish --------------------------
				for (k_idx = 0; k_idx < num_threads; k_idx++)
				{
					try
					{
						m_idx[k_idx].join();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				index_download_retries++;

				if (mapdownload_error_code == 0)
				{
					System.out.println("index download ok -> break retry loop");
					break;
				}

			} // while-loop until index is downloaded -------------------------

			System.out.println("index download -> ended retry loop");

			if (mapdownload_error_code > 0)
			{
				ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "idx download end [*ERROR*]");
			}
			else
			{
				ZANaviLogMessages.am(ZANaviLogMessages.STATUS_INFO, this.getClass().getSimpleName() + ":" + "idx download end [OK]");
			}

			if (mapdownload_error_code > 0)
			{
				mapdownload_error_code_clear();
				index_file_download = false;
			}
			else
			{
				// set progressbar to 100%
				Message msg_prog2 = new Message();
				Bundle b_prog2 = new Bundle();
				b_prog2.putInt("pg", 100);
				msg_prog2.what = 2;
				msg_prog2.setData(b_prog2);
				ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog2);

				int k_idx;
				for (k_idx = 0; k_idx < num_threads; k_idx++)
				{
					// update speedbar
					Message msg_prog = new Message();
					Bundle b_prog = new Bundle();
					b_prog.putInt("speed_kb_per_sec", 0);
					b_prog.putInt("threadnum", k_idx);
					msg_prog.what = 3;
					msg_prog.setData(b_prog);
					ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);
				}

				// delete an already there idx file, first
				//System.out.println("idx 001:" + Navit.MAP_FILENAME_PATH + final_fileName + ".idx");
				File idx_final_filename = new File(Navit.MAP_FILENAME_PATH + final_fileName + ".idx");
				idx_final_filename.delete();
				// rename file to final name
				File tmp_downloadfile_idx = new File(Navit.CFG_FILENAME_PATH, fileName + ".idx");
				//System.out.println("idx 002:" + Navit.CFG_FILENAME_PATH + fileName + ".idx");
				File final_outputFile_idx = new File(Navit.MAP_FILENAME_PATH + final_fileName + ".idx");
				//System.out.println("idx 003:" + Navit.MAP_FILENAME_PATH + final_fileName + ".idx");
				tmp_downloadfile_idx.renameTo(final_outputFile_idx);
			}

			// ------------ download idx file ------------
			// ------------ download idx file ------------
		}

		// remove
		NavitMapDownloader.remove_from_cat_file(up_map);
		// remove any duplicates (junk in file)
		NavitMapDownloader.remove_from_cat_file_disk_name(final_fileName);
		// add to the catalogue file for downloaded maps
		NavitMapDownloader.add_to_cat_file(final_fileName, map_values.url);

		//
		//

		msg = handler.obtainMessage();
		b = new Bundle();
		msg.what = 1;
		b.putInt("max", (int) (map_values.est_size_bytes / 1024));
		b.putInt("cur", (int) (map_values.est_size_bytes / 1024));
		b.putInt("dialog_num", my_dialog_num);
		b.putString("title", Navit.get_text("Mapdownload")); //TRANS
		b.putString("text", map_values.map_name + " " + Navit.get_text("ready")); //TRANS
		msg.setData(b);
		handler.sendMessage(msg);

		Log.d("NavitMapDownloader", "success");
		exit_code = 0;

		return exit_code;
	}

	static void default_ssl_cert()
	{
		HttpsURLConnection.setDefaultHostnameVerifier(hnv_default);
		HttpsURLConnection.setDefaultSSLSocketFactory(sslf_default);
	}

	static HostnameVerifier hnv_default = null;
	static SSLSocketFactory sslf_default = null;

	static void trust_Every_ssl_cert()
	{
		// NEVER enable this on a production release!!!!!!!!!!
		try
		{
			hnv_default = HttpsURLConnection.getDefaultHostnameVerifier();
			sslf_default = HttpsURLConnection.getDefaultSSLSocketFactory();

			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
			{
				public boolean verify(String hostname, SSLSession session)
				{
					Log.d("NavitMapDownloader", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
					// return true -> mean we trust this cert !! DANGER !! DANGER !!
					return true;
				}
			});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager()
			{
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return new java.security.cert.X509Certificate[0];
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
				{
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
				{
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// NEVER enable this on a production release!!!!!!!!!!
	}

	public String d_get_servername(boolean first_run, zanavi_osm_map_values map_values)
	{
		// this is only for debugging
		// NEVER enable this on a production release!!!!!!!!!!
		// NEVER enable this on a production release!!!!!!!!!!
		// ------- // trust_Every_ssl_cert();
		// NEVER enable this on a production release!!!!!!!!!!
		// NEVER enable this on a production release!!!!!!!!!!

		String servername = null;
		try
		{
			URL url = new URL(ZANAVI_MAPS_SEVERTEXT_URL);

			if (!first_run)
			{
				url = new URL(ZANAVI_MAPS_SEVERTEXT_URL + "?SUBREQ=1");
			}
			else
			{
				try
				{
					url = new URL(ZANAVI_MAPS_SEVERTEXT_URL + "?MAP=" + URLEncoder.encode(map_values.url, "UTF-8"));
				}
				catch (Exception e_url)
				{
				}
			}

			System.out.println(ZANAVI_MAPS_SEVERTEXT_URL);

			HttpURLConnection c = get_url_connection(url);

			String ua_ = Navit.UserAgentString_bind.replace("@__THREAD__@", "0" + "T" + MULTI_NUM_THREADS_LOCAL);
			c.addRequestProperty("User-Agent", ua_);
			c.addRequestProperty("Pragma", "no-cache");

			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.setReadTimeout(SOCKET_READ_TIMEOUT);
			c.setConnectTimeout(SOCKET_CONNECT_TIMEOUT);

			try
			{
				c.connect();
				BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()), 4096);
				String str;
				str = in.readLine();

				if (str != null)
				{
					if (str.length() > 2)
					{
						System.out.println("from server=" + str);
						servername = str;
					}
				}
				in.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			c.disconnect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return servername;
	}

	public String d_get_md5_from_server(zanavi_osm_map_values map_values, String servername, int map_num3)
	{
		// try to get MD5 file
		String md5_from_server = null;
		try
		{
			URL url = new URL(ZANAVI_MAPS_BASE_URL_PROTO + servername + ZANAVI_MAPS_BASE_URL_WO_SERVERNAME + map_values.url + ".md5");
			System.out.println("md5 url:" + ZANAVI_MAPS_BASE_URL_PROTO + servername + ZANAVI_MAPS_BASE_URL_WO_SERVERNAME + map_values.url + ".md5");
			HttpURLConnection c = get_url_connection(url);
			String ua_ = Navit.UserAgentString_bind.replace("@__THREAD__@", "0" + "T" + MULTI_NUM_THREADS_LOCAL);
			c.addRequestProperty("User-Agent", ua_);
			c.addRequestProperty("Pragma", "no-cache");

			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.setReadTimeout(SOCKET_READ_TIMEOUT);
			c.setConnectTimeout(SOCKET_CONNECT_TIMEOUT);
			try
			{
				c.connect();
				BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()), 4096);
				String str;
				str = in.readLine();
				if (str != null)
				{
					if (str.length() > 5)
					{
						File tmp_downloadfile_md5 = new File(Navit.MAPMD5_FILENAME_PATH, MD5_DOWNLOAD_TEMPFILE);
						tmp_downloadfile_md5.delete();
						System.out.println("md5 from server=" + str);
						FileOutputStream fos = null;
						try
						{
							fos = new FileOutputStream(tmp_downloadfile_md5);
							fos.write(str.getBytes());
							fos.close();
							md5_from_server = str;
						}
						catch (FileNotFoundException e1)
						{
							e1.printStackTrace();
						}
					}
				}
				in.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			c.disconnect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// try to get MD5 file
		return md5_from_server;
	}

	public long d_get_real_download_filesize(zanavi_osm_map_values map_values, String servername, int map_num3)
	{
		long real_size_bytes = 0;
		try
		{
			URL url = new URL(ZANAVI_MAPS_BASE_URL_PROTO + servername + ZANAVI_MAPS_BASE_URL_WO_SERVERNAME + map_values.url);
			System.out.println("url1:" + ZANAVI_MAPS_BASE_URL_PROTO + servername + ZANAVI_MAPS_BASE_URL_WO_SERVERNAME + map_values.url);
			HttpURLConnection c = get_url_connection(url);
			String ua_ = Navit.UserAgentString_bind.replace("@__THREAD__@", "0" + "T" + MULTI_NUM_THREADS_LOCAL);
			c.addRequestProperty("User-Agent", ua_);
			c.addRequestProperty("Pragma", "no-cache");

			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.setReadTimeout(SOCKET_READ_TIMEOUT);
			c.setConnectTimeout(SOCKET_CONNECT_TIMEOUT);
			// real_size_bytes = c.getContentLength(); -> only returns "int" value, its an android bug
			try
			{
				c.connect();
				System.out.println("header content-length=" + c.getHeaderField("content-length"));
				real_size_bytes = Long.parseLong(c.getHeaderField("content-length"));
				Log.d("NavitMapDownloader", "real_size_bytes=" + real_size_bytes);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.d("NavitMapDownloader", "error parsing content-length header field");
			}
			c.disconnect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return real_size_bytes;
	}

	public String d_get_md5_from_disk(zanavi_osm_map_values map_values, String servername, int map_num3)
	{
		String md5_on_disk = null;

		// try to read MD5 from disk - if it fails dont worry!!
		File md5_final_filename = new File(Navit.MAPMD5_FILENAME_PATH + map_values.url + ".md5");
		InputStream in2 = null;
		try
		{
			in2 = new BufferedInputStream(new FileInputStream(md5_final_filename));
			InputStreamReader inputreader = new InputStreamReader(in2);
			BufferedReader buffreader = new BufferedReader(inputreader, 4096);
			String tmp = buffreader.readLine();
			if (tmp != null)
			{
				if (tmp.length() > 5)
				{
					md5_on_disk = tmp;
					System.out.println("MD5 on disk=" + md5_on_disk);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (in2 != null)
			{
				try
				{
					in2.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		// try to read MD5 from disk - if it fails dont worry!!

		return md5_on_disk;
	}

	public Boolean d_match_md5sums(String md5_1, String md5_2)
	{
		Boolean md5_match = false;
		//
		// check md5 sum
		//
		if ((md5_1 != null) && (md5_2 != null) && (md5_1.equals(md5_2)))
		{
			md5_match = true;
		}
		//
		// check md5 sum
		//
		return md5_match;
	}

	public HttpURLConnection d_url_connect(zanavi_osm_map_values map_values, String servername, int map_num3, int current_thread_num)
	{
		URL url = null;
		HttpURLConnection c = null;
		try
		{
			url = new URL(ZANAVI_MAPS_BASE_URL_PROTO + servername + ZANAVI_MAPS_BASE_URL_WO_SERVERNAME + map_values.url);
			System.out.println("url2:" + ZANAVI_MAPS_BASE_URL_PROTO + servername + ZANAVI_MAPS_BASE_URL_WO_SERVERNAME + map_values.url);
			c = get_url_connection(url);
			String ua_ = Navit.UserAgentString_bind.replace("@__THREAD__@", "" + current_thread_num + "T" + MULTI_NUM_THREADS_LOCAL);
			c.addRequestProperty("User-Agent", ua_);
			// c.addRequestProperty("Pragma", "no-cache");
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.setReadTimeout(SOCKET_READ_TIMEOUT);
			c.setConnectTimeout(SOCKET_CONNECT_TIMEOUT);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			c = null;
		}
		return c;
	}

	public HttpURLConnection d_url_resume_download_at(HttpURLConnection c, long old_download_size, int num)
	{
		c.setRequestProperty("Range", "bytes=" + old_download_size + "-");
		Log.d("NavitMapDownloader", num + " resuming download at " + old_download_size + " bytes");
		System.out.println("DEBUG_MAP_DOWNLOAD::" + num + " resuming download at " + old_download_size + " bytes");
		return c;
	}

	public HttpURLConnection d_url_resume_download_at(HttpURLConnection c, long old_download_size, long end_size, int num)
	{
		c.setRequestProperty("Range", "bytes=" + old_download_size + "-" + end_size);
		Log.d("NavitMapDownloader", num + "resuming download at " + old_download_size + " bytes" + ":" + end_size);
		System.out.println("DEBUG_MAP_DOWNLOAD::" + num + "resuming download at " + old_download_size + " bytes" + ":" + end_size);

		return c;
	}

	public BufferedInputStream d_url_get_bif(HttpURLConnection c)
	// public InputStream d_url_get_bif(HttpURLConnection c)
	{
		InputStream is = null;
		BufferedInputStream bif = null;
		try
		{
			c.setUseCaches(false); // set header "Cache-Control: no-cache" ?
			c.connect();
			is = c.getInputStream();
			bif = new BufferedInputStream(is, MAP_READ_FILE_BUFFER); // buffer
		}
		catch (FileNotFoundException f)
		{
			// map file is not on server!!
			try
			{
				c.disconnect();
			}
			catch (Exception x)
			{
				x.printStackTrace();
			}
			System.out.println("map file is not on server!");
			f.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return bif;
		// return is;
	}

	public void d_url_disconnect(HttpURLConnection c)
	{
		try
		{
			c.disconnect();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void d_pre_create_file(String filename, long size, Handler handler, int my_dialog_num)
	{
		RandomAccessFile f = null;
		long size_2 = size - 2;

		if (size > MAX_SINGLE_BINFILE_SIZE)
		{
			// skip this step with large mapfiles (or implement handling of split files)
			return;
		}

		class FileProgress extends Thread
		{
			Handler handler;
			String file;
			int my_dialog_num;
			long file_size;
			Boolean running = false;

			FileProgress(Handler h, String f, int dn, long fsize)
			{
				handler = h;
				file = f;
				my_dialog_num = dn;
				file_size = fsize;
				running = false;
			}

			public void run()
			{
				Message msg;
				Bundle b;
				running = true;
				File f = null;
				while (running)
				{
					if (mapdownload_stop_all_threads)
					{
						System.out.println("FileProgress:mapdownload_stop_all_threads");
						break;
					}

					try
					{
						f = new File(file);
						long cur_size = f.length();
						// System.out.println("cur_size=" + cur_size);
						msg = handler.obtainMessage();
						b = new Bundle();
						msg.what = 1;
						b.putInt("max", (int) (file_size / 1000));
						b.putInt("cur", (int) (cur_size / 1000));
						b.putInt("dialog_num", my_dialog_num);
						b.putString("title", Navit.get_text("Mapdownload")); //TRANS
						b.putString("text", Navit.get_text("Creating outputfile, long time")); //TRANS
						msg.setData(b);
						handler.sendMessage(msg);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					if (running)
					{
						try
						{
							Thread.sleep(700);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
			}

			public void stop_me()
			{
				running = false;
			}
		}

		FileProgress fp = new FileProgress(handler, filename, my_dialog_num, size);
		fp.start();

		try
		{
			f = new RandomAccessFile(filename, "rw");
			if (size_2 > 1900000000L)
			{
				f.seek(1900000000L);
				System.out.println("pre 1");
				f.writeByte(1);
				System.out.println("pre 2");
				f.seek(1900000000L);
				System.out.println("pre 3");
				int buf_size = 1000 * 32;
				byte[] buffer_seek = new byte[buf_size];
				int num_loops = (int) ((size_2 - 1900000000L - 1) / buf_size);
				int j = 0;
				for (j = 0; j < num_loops; j++)
				{
					f.write(buffer_seek);
					try
					{
						Thread.sleep(2);
					}
					catch (Exception x)
					{
						x.printStackTrace();
					}
				}

				long this_fp = 1900000000L + (num_loops * buf_size);
				System.out.println("pre 4 " + this_fp);
				buf_size = (int) (size - this_fp) - 1;
				if (buf_size > 0)
				{
					buffer_seek = new byte[buf_size];
					f.write(buffer_seek);
				}
				System.out.println("pre 5");

				// int skipped = f.skipBytes((int) (size_2 - 1900000000L));
			}
			else
			{
				System.out.println("pre 6");
				f.seek(size_2);
				System.out.println("pre 7");
			}
			System.out.println("pre 8");
			f.writeByte(1);
			// ****EEEE**** W/System.err(  266): java.io.IOException: No space left on device
			System.out.println("pre 9");

			try
			{
				System.out.println("d_pre_create_file:f len=" + f.length());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			f.close();
			System.out.println("pre 10");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			// ****EEEE**** W/System.err(  266): java.io.IOException: No space left on device
		}

		fp.stop_me();

		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		msg.what = 1;
		b.putInt("max", (int) (size / 1000));
		b.putInt("cur", (int) (size / 1000));
		b.putInt("dialog_num", my_dialog_num);
		b.putString("title", Navit.get_text("Mapdownload")); //TRANS
		b.putString("text", Navit.get_text("Creating outputfile, wait")); //TRANS
		msg.setData(b);
		handler.sendMessage(msg);
	}

	public Object d_open_file(String filename, long pos, int my_num)
	{
		return (Object) d_open_file_real(filename, pos, my_num, true);
	}

	public Object d_open_file_real(String filename, long pos, int my_num, boolean randomaccess)
	{
		// if ((randomaccess == true) || (pos <= 1900000000L))
		{
			RandomAccessFile f = null;
			System.out.println("d_open_file(rnd): " + my_num + " " + filename + " seek (start):" + pos);
			try
			{
				f = new RandomAccessFile(filename, "rw");
				// FileChannel fc1 = f.getChannel();

				if (pos > 1900000000L)
				{
					System.out.println("open file: 1");
					f.seek(1900000000L);
					System.out.println("open file: 2");

					int buf_size = 1000 * 64;
					byte[] buffer_seek = new byte[buf_size];
					int num_loops = (int) ((pos - 1900000000L - 1) / buf_size);
					int j = 0;
					for (j = 0; j < num_loops; j++)
					{
						f.readFully(buffer_seek);
						//					try
						//					{
						//						Thread.sleep(2);
						//					}
						//					catch (Exception x)
						//					{
						//						x.printStackTrace();
						//					}
					}
					long this_fp = 1900000000L + (num_loops * buf_size);
					System.out.println("open file: 3 " + this_fp);
					buf_size = (int) (pos - this_fp);
					if (buf_size > 0)
					{
						buffer_seek = new byte[buf_size];
						f.readFully(buffer_seek);
					}
					System.out.println("open file: 4");

					// int skipped = f.skipBytes((int) (pos - 1900000000L));
					System.out.println("open file: 5");
				}
				else
				{
					System.out.println("open file: 6");
					f.seek(pos);
					System.out.println("open file: 7");
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("d_open_file:" + my_num + " seek (end):" + pos);

			try
			{
				System.out.println("d_open_file:" + my_num + " f len(seek)=" + f.length());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return (Object) f;
		}
	}

	public void d_close_file(RandomAccessFile f, int my_num)
	{
		try
		{
			System.out.println("d_close_file:" + my_num + " f len=" + f.length());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			f.close();
			System.out.println("d_close_file:" + my_num + " f.close()");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static synchronized void mapdownload_error_code_inc()
	{
		mapdownload_error_code++;
	}

	public static void mapdownload_error_code_clear()
	{
		mapdownload_error_code = 0;
	}

	public String calc_md5sum_on_device(Handler handler, int my_dialog_num, long size)
	{
		String md5sum = null;
		final int sleep_millis = 0;
		final int sleep_millis_long = 0; // 60;
		final int looper_mod = 100;
		int looper_count = 0;
		int old_percent_ = -1;
		int percent_ = -2;

		if (size > MAX_SINGLE_BINFILE_SIZE)
		{
			try
			{
				String s = "";
				Message msg = null;
				Bundle b = null;
				int size2 = (int) (size / 1000);
				long cur_pos = 0L;

				// Create MD5 Hash
				MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
				InputStream fis = null;
				boolean no_more_parts = false;

				// find all pieces of the large map file
				int parts = 0;

				// -------------- LOOP thru all the parts -------------
				// -------------- LOOP thru all the parts -------------
				// -------------- LOOP thru all the parts -------------

				try
				{
					if (Navit.wl_cpu != null)
					{
						Navit.wl_cpu.acquire();
						Log.e("Navit", "WakeLock CPU: acquire 2a");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				while (!no_more_parts)
				{
					try
					{
						if (parts == 0)
						{
							fis = new FileInputStream(Navit.CFG_FILENAME_PATH + DOWNLOAD_FILENAME);
							System.out.println("calc_md5sum_on_device(split):found file=" + Navit.CFG_FILENAME_PATH + DOWNLOAD_FILENAME);
						}
						else
						{
							fis = new FileInputStream(Navit.CFG_FILENAME_PATH + DOWNLOAD_FILENAME + "." + parts);
							System.out.println("calc_md5sum_on_device(split):found file=" + Navit.CFG_FILENAME_PATH + DOWNLOAD_FILENAME + "." + parts);
						}
						parts++;
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
						no_more_parts = true;
					}

					if (!no_more_parts)
					{
						old_percent_ = -1;
						percent_ = -2;

						byte[] buffer = new byte[1024 * MD5_CALC_BUFFER_KB];
						int numRead = 0;
						do
						{
							if (mapdownload_stop_all_threads)
							{
								System.out.println("calc_md5sum_on_device 1(split):mapdownload_stop_all_threads");
								break;
							}

							try
							{
								numRead = fis.read(buffer);
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}

							if (numRead > 0)
							{
								try
								{
									looper_count++;
									if (looper_count > looper_mod)
									{
										looper_count = 0;
										// allow to catch breath
										Thread.sleep(sleep_millis_long);
									}
									else
									{
										// allow to catch breath
										Thread.sleep(sleep_millis);
									}
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
								digest.update(buffer, 0, numRead);
								cur_pos = cur_pos + numRead;
							}

							// do not update notification too often
							old_percent_ = percent_;
							percent_ = calc_percent((int) (cur_pos / 1000), size2);

							if (percent_ != old_percent_)
							{
								msg = handler.obtainMessage();
								b = new Bundle();
								msg.what = 1;
								b.putInt("max", size2);
								b.putInt("cur", (int) (cur_pos / 1000));
								b.putInt("dialog_num", my_dialog_num);
								b.putString("title", Navit.get_text("Mapdownload")); //TRANS
								b.putString("text", Navit.get_text("generating MD5 checksum")); //TRANS
								msg.setData(b);
								handler.sendMessage(msg);

								try
								{
									ZANaviMapDownloaderService.set_noti_text(Navit.get_text("checking") + ": " + calc_percent((int) (cur_pos / 1000), size2) + "%", percent_);
									ZANaviMapDownloaderService.set_large_text(Navit.get_text("checking") + ": " + calc_percent((int) (cur_pos / 1000), size2) + "%");

									// update progressbar
									Message msg_prog = new Message();
									Bundle b_prog = new Bundle();
									b_prog.putInt("pg", calc_percent((int) (cur_pos / 1000), size2));
									msg_prog.what = 2;
									msg_prog.setData(b_prog);
									ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}

							}

						}
						while (numRead != -1);

						try
						{
							fis.close();
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

					if (mapdownload_stop_all_threads)
					{

						try
						{
							if (Navit.wl_cpu != null)
							{
								Navit.wl_cpu.release();
								Log.e("Navit", "WakeLock CPU: release 2a");
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						System.out.println("calc_md5sum_on_device 2(split):mapdownload_stop_all_threads");
						return null;
					}

				}

				try
				{
					if (Navit.wl_cpu != null)
					{
						Navit.wl_cpu.release();
						Log.e("Navit", "WakeLock CPU: release 2b");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// -------------- LOOP thru all the parts -------------
				// -------------- LOOP thru all the parts -------------
				// -------------- LOOP thru all the parts -------------

				msg = handler.obtainMessage();
				b = new Bundle();
				msg.what = 1;
				b.putInt("max", size2);
				b.putInt("cur", size2);
				b.putInt("dialog_num", my_dialog_num);
				b.putString("title", Navit.get_text("Mapdownload")); //TRANS
				b.putString("text", Navit.get_text("generating MD5 checksum")); //TRANS
				msg.setData(b);
				handler.sendMessage(msg);

				byte messageDigest[] = digest.digest();
				// Create Hex String
				StringBuffer hexString = new StringBuffer();
				String t = "";
				for (int i = 0; i < messageDigest.length; i++)
				{
					t = Integer.toHexString(0xFF & messageDigest[i]);
					if (t.length() == 1)
					{
						t = "0" + t;
					}
					hexString.append(t);
				}
				md5sum = hexString.toString();
				System.out.println("md5sum local(split)=" + md5sum);

			}
			catch (Exception e)
			{

				try
				{
					if (Navit.wl_cpu != null)
					{
						Navit.wl_cpu.release();
						Log.e("Navit", "WakeLock CPU: release 2c");
					}
				}
				catch (Exception e3)
				{
					e3.printStackTrace();
				}

				return md5sum;
			}

			return md5sum;
		}

		try
		{
			String s = "";
			Message msg = null;
			Bundle b = null;
			int size2 = (int) (size / 1000);
			long cur_pos = 0L;

			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");

			InputStream fis = null;

			try
			{
				if (Navit.wl_cpu != null)
				{
					Navit.wl_cpu.acquire();
					Log.e("Navit", "WakeLock CPU: acquire 3a");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				fis = new FileInputStream(Navit.CFG_FILENAME_PATH + DOWNLOAD_FILENAME);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}

			old_percent_ = -1;
			percent_ = -2;

			byte[] buffer = new byte[1024 * MD5_CALC_BUFFER_KB];
			int numRead = 0;
			do
			{
				if (mapdownload_stop_all_threads)
				{
					System.out.println("calc_md5sum_on_device 1:mapdownload_stop_all_threads");
					break;
				}

				try
				{
					numRead = fis.read(buffer);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				if (numRead > 0)
				{
					try
					{
						looper_count++;
						if (looper_count > looper_mod)
						{
							looper_count = 0;
							// allow to catch breath
							Thread.sleep(sleep_millis_long);
						}
						else
						{
							// allow to catch breath
							Thread.sleep(sleep_millis);
						}
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					digest.update(buffer, 0, numRead);
					cur_pos = cur_pos + numRead;
				}

				// do not update notification too often
				old_percent_ = percent_;
				percent_ = calc_percent((int) (cur_pos / 1000), size2);

				if (percent_ != old_percent_)
				{
					msg = handler.obtainMessage();
					b = new Bundle();
					msg.what = 1;
					b.putInt("max", size2);
					b.putInt("cur", (int) (cur_pos / 1000));
					b.putInt("dialog_num", my_dialog_num);
					b.putString("title", Navit.get_text("Mapdownload")); //TRANS
					b.putString("text", Navit.get_text("generating MD5 checksum")); //TRANS
					msg.setData(b);
					handler.sendMessage(msg);

					try
					{
						ZANaviMapDownloaderService.set_noti_text(Navit.get_text("checking") + ": " + calc_percent((int) (cur_pos / 1000), size2) + "%", percent_);
						ZANaviMapDownloaderService.set_large_text(Navit.get_text("checking") + ": " + calc_percent((int) (cur_pos / 1000), size2) + "%");

						// update progressbar
						Message msg_prog = new Message();
						Bundle b_prog = new Bundle();
						b_prog.putInt("pg", calc_percent((int) (cur_pos / 1000), size2));
						msg_prog.what = 2;
						msg_prog.setData(b_prog);
						ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg_prog);

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

			}
			while (numRead != -1);

			msg = handler.obtainMessage();
			b = new Bundle();
			msg.what = 1;
			b.putInt("max", size2);
			b.putInt("cur", size2);
			b.putInt("dialog_num", my_dialog_num);
			b.putString("title", Navit.get_text("Mapdownload")); //TRANS
			b.putString("text", Navit.get_text("generating MD5 checksum")); //TRANS
			msg.setData(b);
			handler.sendMessage(msg);

			try
			{
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (mapdownload_stop_all_threads)
			{
				try
				{
					if (Navit.wl_cpu != null)
					{
						Navit.wl_cpu.release();
						Log.e("Navit", "WakeLock CPU: release 3a");
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				System.out.println("calc_md5sum_on_device 2:mapdownload_stop_all_threads");
				return null;
			}

			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			String t = "";
			for (int i = 0; i < messageDigest.length; i++)
			{
				t = Integer.toHexString(0xFF & messageDigest[i]);
				if (t.length() == 1)
				{
					t = "0" + t;
				}
				hexString.append(t);
			}
			md5sum = hexString.toString();
			System.out.println("md5sum local=" + md5sum);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (Navit.wl_cpu != null)
			{
				Navit.wl_cpu.release();
				Log.e("Navit", "WakeLock CPU: release 3b");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return md5sum;
	}

	static int calc_percent(int cur, int max)
	{
		int percent = 0;

		try
		{
			percent = (int) ((float) cur / (float) max * 100f);
		}
		catch (Exception e)
		{
			percent = 0;
		}
		return percent;
	}

	static HttpURLConnection get_url_connection(URL u)
	{
		HttpURLConnection my_HttpURLConnection = null;

		if (USE_OKHTTPCLIENT)
		{
			// --- new ---
			// --- new ---
			if (http_client_new == null)
			{
				http_client_new = new okhttp3.OkHttpClient();
				http_client_new_urlfactory = new okhttp3.OkUrlFactory(http_client_new);
			}
			my_HttpURLConnection = http_client_new_urlfactory.open(u);
			// --- new ---
			// --- new ---
		}
		else
		{
			// --- old ---
			// --- old ---
			try
			{
				my_HttpURLConnection = (HttpURLConnection) u.openConnection();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			// --- old ---
			// --- old ---
		}

		return my_HttpURLConnection;
	}

	static long bytes_per_second_calc(long millis, long bs)
	{
		try
		{
			return (long) (((float) bs / ((float) millis / (float) 1000)));
		}
		catch (Exception e)
		{
			// catch division by zero
			return 0L;
		}
	}
}
