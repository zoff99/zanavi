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

package com.zoffcc.applications.zanavi;

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

	/**
	 * @return gega bytes of bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInGB(String directory)
	{
		return getExternalAvailableSpaceInBytes(directory) / SIZE_GB;
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
