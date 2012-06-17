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
 * Copyright (C) 2010 Marcin Skruch <mskruch@gmail.com>
 *
 * http://code.google.com/p/gps-tools/source/browse/trunk/src/itcrowd/gps/util/DistanceUtil.java?r=7
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

package itcrowd.gps.util;

/**
 * Created by IntelliJ IDEA.
 * User: Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
 * Date: 01.11.11
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
public class DistanceUtil
{

	public static double distance(double lat1, double lon1, double lat2, double lon2)
	{

		//Wenn lat1=lat2 und lon1=lon2 (keine Distance) hier abfangen
		if ((lat1 == lat2) && (lon1 == lon2))
		{
			return 0;
		}
		else
			//Sonst Distance berechnen und zurückgeben
			return distance(lat1, lon1, lat2, lon2, 'K');
	}

	public static double distance(double lat1, double lon1, double lat2, double lon2, char unit)
	{
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K')
		{
			dist = dist * 1.609344;
		}
		else if (unit == 'N')
		{
			dist = dist * 0.8684;
		}
		return (dist);
	}

	private static double deg2rad(double deg)
	{
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad)
	{
		return (rad * 180.0 / Math.PI);
	}

}
