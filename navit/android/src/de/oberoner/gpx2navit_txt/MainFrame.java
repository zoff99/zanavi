/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2012 Zoff <zoff@zoff.cc>
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
 * Copyright (C) 2011 Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
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
 * 
 * author allowed the use, and the licensing under GPLv2 (per email)
 * 
 */

package de.oberoner.gpx2navit_txt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;

import de.oberoner.gpx2navit_txt.GpxDOM.GPX_Track;

/******************************************************************************
 * Created by IntelliJ IDEA.
 * User: Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
 * Date: 15.10.11
 * Time: 16:19
 * 
 */
public class MainFrame
{
	public static String InFilePath;
	public static String OutFilePath;

	public static String remove_special_chars(String in)
	{
		String out = in;
		try
		{
			out = out.replaceAll("\n", " ");
		}
		catch (Exception e)
		{

		}
		try
		{
			out = out.replaceAll("\r", " ");
		}
		catch (Exception e)
		{

		}
		return out;
	}

	public static int do_conversion(String in_file, String out_file)
	{
		final DecimalFormat format = new DecimalFormat("#.000");
		String s;
		int error_code = 0;

		// konvertieren
		InFilePath = in_file;
		OutFilePath = out_file;

		// Das GPX-File in das DOM parsen
		GpxDOM gpxDOM = new GpxDOM(InFilePath);

		System.out.println("do_conversion 001 " + gpxDOM);

		if (gpxDOM.gpxDoc != null)
		{
			// Liste der Wegpunkte aus dem gpx-File lesen
			NavitWptList wptList = gpxDOM.getWptList();

			// get all tracks (including items)
			GPX_Track obj = gpxDOM.getTrackLabel();

			// get all routes (including items) [convert "routes" to "tracks"]
			GPX_Track obj2 = gpxDOM.getRouteLabel();

			// Trackpoint-Header
			for (int kk = 0; kk < obj.h.size(); kk++)
			{
				// Die Anzahl der Trackpunkte im Navit-Trkpt-Header setzen
				s = String.valueOf(obj.i.get(kk).size());
				while (s.length() < 5)
				{
					s = " " + s;
				}
				obj.h.get(kk).setCount("\"" + remove_special_chars(s) + "\"");

				// Die Entfernung in Meter berechnen und Headereintrag schreiben
				double trackDistance = gpxDOM.calculateDistance(obj.i.get(kk));

				s = "\"" + format.format(trackDistance) + "\"";
				s = s.replace(",", ".");
				obj.h.get(kk).setLength(s);
			}

			// Route-Header
			for (int kk = 0; kk < obj2.h.size(); kk++)
			{
				// Die Anzahl der Trackpunkte im Navit-Route-Header setzen
				s = String.valueOf(obj2.i.get(kk).size());
				while (s.length() < 5)
				{
					s = " " + s;
				}
				obj2.h.get(kk).setCount("\"" + remove_special_chars(s) + "\"");

				// Die Entfernung in Meter berechnen und Headereintrag schreiben
				double rteDistance = gpxDOM.calculateDistance(obj2.i.get(kk));

				s = "\"" + format.format(rteDistance) + "\"";
				s = s.replace(",", ".");
				obj2.h.get(kk).setLength(s);
			}

			// write Navit-file
			try
			{
				FileWriter fstream = new FileWriter(OutFilePath);
				BufferedWriter outFile = new BufferedWriter(fstream);

				// waypoints
				if (wptList != null && wptList.size() != 0)
				{
					gpxDOM.writeWptList(wptList, outFile);
				}

				// tracks
				for (int kk = 0; kk < obj.h.size(); kk++)
				{
					if (obj.i.get(kk).size() > 0)
					{
						gpxDOM.writeTrkptList(obj.h.get(kk), obj.i.get(kk), outFile);
					}
				}

				// routes
				for (int kk = 0; kk < obj2.h.size(); kk++)
				{
					if (obj2.i.get(kk).size() > 0)
					{
						gpxDOM.writeTrkptList(obj2.h.get(kk), obj2.i.get(kk), outFile);
					}
				}

				// close output file
				outFile.close();
			}
			catch (Exception ex)
			{
				// some error
				ex.printStackTrace();
				error_code = 3;
			}

		}
		else
		{
			// parsing error (not a gpx file)
			System.out.println("do_conversion 098");
			error_code = 4;
		}

		return error_code;
	}
}
