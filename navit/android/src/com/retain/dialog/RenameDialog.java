/**
 * from:
 * http://code.google.com/p/retain-android/
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 */
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

package com.retain.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.retain.dialog.RenameHandlerInterface.OnRenameItemListener;

public class RenameDialog extends AlertDialog
{

	private EditText mET;
	private long mRowId;
	private OnRenameItemListener mRenameListener;
	private Activity mActivity;

	public RenameDialog(Activity activity, String title, String name, OnRenameItemListener renameListener)
	{
		super(activity);

		mET = new EditText(activity);
		mET.setText(name);
		mRenameListener = renameListener;
		mActivity = activity;

		setView(mET);
		setCancelable(true);
		setTitle(title);

		setButton("Ok", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String title = mET.getText().toString();
				mRenameListener.onRenameItem(title);
				return;
			}
		});
		setButton2("Cancel", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});

	}

}
