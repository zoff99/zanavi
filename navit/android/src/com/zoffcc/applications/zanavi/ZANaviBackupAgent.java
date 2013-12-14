/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2013 Zoff <zoff@zoff.cc>
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

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class ZANaviBackupAgent extends BackupAgentHelper
{
	// An arbitrary string used within the BackupAgentHelper implementation to
	// identify the SharedPreferenceBackupHelper's data.
	static final String MY_PREFS_BACKUP_PREF = "zanavi_prefs";
	static final String MY_PREFS_BACKUP_FILE1 = "zanavi_file1";
	static final String MY_PREFS_BACKUP_FILE2 = "zanavi_file2";

	// Simply allocate a helper and install it
	@Override
	public void onCreate()
	{
		// get the name of the default prefs file ----------------------------------------
		// String prefsActivityFullFileName = this.getPackageName() + "_preferences";
		String default_prefs_group = "com.zoffcc.applications.zanavi_preferences";
		// -------------------------------------------------------------------------------

		try
		{
			SharedPreferencesBackupHelper prefs_helper = new SharedPreferencesBackupHelper(this, default_prefs_group);
			addHelper(MY_PREFS_BACKUP_PREF, prefs_helper);

			String data_dir = this.getFilesDir().getPath();
			// System.out.println("XXYY data dir for backup=" + data_dir);
			String my_DATA_SHARE_DIR = data_dir + "/share";

			FileBackupHelper file1_helper = new FileBackupHelper(this, my_DATA_SHARE_DIR + Navit.Navit_CENTER_FILENAME);
			addHelper(MY_PREFS_BACKUP_FILE1, file1_helper);

			FileBackupHelper file2_helper = new FileBackupHelper(this, my_DATA_SHARE_DIR + Navit.Navit_DEST_FILENAME);
			addHelper(MY_PREFS_BACKUP_FILE2, file2_helper);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	//public void doBackup()
	//{
	//
	//}
}
