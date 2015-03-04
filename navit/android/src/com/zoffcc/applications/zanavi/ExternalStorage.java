/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2015 Zoff <zoff@zoff.cc>
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import android.os.Environment;

/*
 * 
 * thanks to: http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location
 * 
 * Usage:
 * 
 * Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
 * File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
 * File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);
 * 
 * 
 */
public class ExternalStorage
{

	public static final String SD_CARD = "sdCard";
	public static final String EXTERNAL_SD_CARD = "externalSdCard";

	/**
	 * @return True if the external storage is available. False otherwise.
	 */
	public static boolean isAvailable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
		{
			return true;
		}
		return false;
	}

	public static String getSdCardPath()
	{
		return Environment.getExternalStorageDirectory().getPath() + "/";
	}

	/**
	 * @return True if the external storage is writable. False otherwise.
	 */
	public static boolean isWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		}
		return false;

	}

	/**
	 * @return A map of all storage locations available
	 */
	public static Map<String, File> getAllStorageLocations()
	{
		Map<String, File> map = new HashMap<String, File>(10);

		List<String> mMounts = new ArrayList<String>(10);
		List<String> mVold = new ArrayList<String>(10);
		mMounts.add("/mnt/sdcard");
		mVold.add("/mnt/sdcard");

		try
		{
			File mountFile = new File("/proc/mounts");
			if (mountFile.exists())
			{
				Scanner scanner = new Scanner(mountFile);
				while (scanner.hasNext())
				{
					String line = scanner.nextLine();
					if (line.startsWith("/dev/block/vold/"))
					{
						String[] lineElements = line.split(" ");
						String element = lineElements[1];

						// don't add the default mount path
						// it's already in the list.
						if (!element.equals("/mnt/sdcard")) mMounts.add(element);
					}
				}
				scanner.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			File voldFile = new File("/system/etc/vold.fstab");
			if (voldFile.exists())
			{
				Scanner scanner = new Scanner(voldFile);
				while (scanner.hasNext())
				{
					String line = scanner.nextLine();
					if (line.startsWith("dev_mount"))
					{
						String[] lineElements = line.split(" ");
						String element = lineElements[2];

						if (element.contains(":")) element = element.substring(0, element.indexOf(":"));
						if (!element.equals("/mnt/sdcard")) mVold.add(element);
					}
				}
				scanner.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		for (int i = 0; i < mMounts.size(); i++)
		{
			String mount = mMounts.get(i);
			if (!mVold.contains(mount)) mMounts.remove(i--);
		}
		mVold.clear();

		List<String> mountHash = new ArrayList<String>(10);

		for (String mount : mMounts)
		{
			File root = new File(mount);
			if (root.exists() && root.isDirectory() && root.canWrite())
			{
				File[] list = root.listFiles();
				String hash = "[";
				if (list != null)
				{
					for (File f : list)
					{
						hash += f.getName().hashCode() + ":" + f.length() + ", ";
					}
				}
				hash += "]";
				if (!mountHash.contains(hash))
				{
					String key = SD_CARD + "_" + map.size();
					if (map.size() == 0)
					{
						key = SD_CARD;
					}
					else if (map.size() == 1)
					{
						key = EXTERNAL_SD_CARD;
					}
					mountHash.add(hash);
					map.put(key, root);
				}
			}
		}

		mMounts.clear();

		if (map.isEmpty())
		{
			map.put(SD_CARD, Environment.getExternalStorageDirectory());
		}
		return map;
	}
}
