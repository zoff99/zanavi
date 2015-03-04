/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2015 Zoff <zoff@zoff.cc>
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.os.StatFs;

/**
 * This class is designed to get available space in external storage of android.
 * It contains methods which provide you the available space in different units e.g
 * bytes, KB, MB, GB. OR you can get the number of available blocks on external storage.
 * 
 */
public class NavitAvailableSpaceHandler
{
	//********
	// Variables
	/**
	 * Number of bytes in one KB = 2<sup>10</sup>
	 */
	public final static long SIZE_KB = 1024L;

	/**
	 * Number of bytes in one MB = 2<sup>20</sup>
	 */
	public final static long SIZE_MB = SIZE_KB * SIZE_KB;

	/**
	 * Number of bytes in one GB = 2<sup>30</sup>
	 */
	public final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	//********
	// Methods

	/**
	 * @return Number of bytes available on specific dir
	 */
	public static long getExternalAvailableSpaceInBytes(String directory)
	{
		long availableSpace = -1L;
		try
		{
			StatFs stat = new StatFs(directory);
			availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return availableSpace;
	}

	/**
	 * @return Number of kilo bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInKB(String directory)
	{
		return getExternalAvailableSpaceInBytes(directory) / SIZE_KB;
	}

	/**
	 * @return Number of Mega bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInMB(String directory)
	{
		return getExternalAvailableSpaceInBytes(directory) / SIZE_MB;
	}

	public static float getExternalAvailableSpaceInMBformatted(String directory)
	{
		float drive_free = ((float) getExternalAvailableSpaceInBytes(directory)) / (float) (SIZE_MB);

		return drive_free;
	}

	public static String getExternalAvailableSpaceInMBformattedString(String directory)
	{
		float drive_free = getExternalAvailableSpaceInMBformatted(directory);
		String drive_free_string = SpaceCustomNumberFormat("#.#", drive_free);

		return drive_free_string;
	}

	/**
	 * @return giga bytes of bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInGB(String directory)
	{
		return getExternalAvailableSpaceInBytes(directory) / SIZE_GB;
	}

	public static float getExternalAvailableSpaceInGBformatted(String directory)
	{
		float drive_free = ((float) getExternalAvailableSpaceInBytes(directory)) / (float) (SIZE_GB);

		return drive_free;
	}

	static String SpaceCustomNumberFormat(String pattern, double value)
	{
		// NumberFormat myFormatter = DecimalFormat.getInstance(Locale.US);

		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat(pattern, otherSymbols);

		String output = df.format(value);
		return (output);
	}

	public static String getExternalAvailableSpaceInGBformattedString(String directory)
	{
		float drive_free = getExternalAvailableSpaceInGBformatted(directory);
		String drive_free_string = SpaceCustomNumberFormat("#.##", drive_free);

		return drive_free_string;
	}

	/**
	 * @return Total number of available blocks on external storage
	 */
	public static long getExternalStorageAvailableBlocks(String directory)
	{
		long availableBlocks = -1L;
		try
		{
			StatFs stat = new StatFs(directory);
			availableBlocks = stat.getAvailableBlocks();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return availableBlocks;
	}
}
