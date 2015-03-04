/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2014 Zoff <zoff@zoff.cc>
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

/** The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Improvements :
 * - save the value on positive button click, not on seekbar change
 * - handle @string/... values in xml file
 */

package fr.atcm.carpooling.views.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, OnClickListener
{
	// ------------------------------------------------------------------------------------------
	// Private attributes :
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mSeekBar;
	private TextView mSplashText, mValueText;
	private Context mContext;
	private Button b_plus;
	private Button b_minus;

	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mValue = 0;
	private int mMin = 0; // first part of "dialogMessage" before the first ":" char !!!!!!!!

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// Constructor :
	public SeekBarPreference(Context context, AttributeSet attrs)
	{

		super(context, attrs);
		mContext = context;

		// Get string value for dialogMessage :
		int mDialogMessageId = attrs.getAttributeResourceValue(androidns, "dialogMessage", 0);
		if (mDialogMessageId == 0)
		{
			mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
		}
		else
		{
			mDialogMessage = mContext.getString(mDialogMessageId);
		}

		String min_ = mDialogMessage.split(":", 2)[0];
		mMin = Integer.parseInt(min_);
		// System.out.println("XX min=" + mMin);
		mDialogMessage = mDialogMessage.split(":", 2)[1];

		// Get string value for suffix (text attribute in xml file) :
		int mSuffixId = attrs.getAttributeResourceValue(androidns, "text", 0);
		if (mSuffixId == 0)
		{
			mSuffix = attrs.getAttributeValue(androidns, "text");
		}
		else
		{
			mSuffix = mContext.getString(mSuffixId);
		}

		// Get default and max seekbar values :
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
		mMax = mMax - mMin; // subtract minimum value 
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// DialogPreference methods :
	@Override
	protected View onCreateDialogView()
	{

		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		mSplashText = new TextView(mContext);
		mSplashText.setPadding(30, 10, 30, 10);
		mSplashText.setGravity(Gravity.CENTER_HORIZONTAL);
		mSplashText.setTextSize(16);
		if (mDialogMessage != null) mSplashText.setText(mDialogMessage);
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(24);
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		RelativeLayout layout2 = new RelativeLayout(mContext);
		layout2.setPadding(6, 6, 6, 6);

		Button b_minus = new Button(mContext);
		b_minus.setGravity(Gravity.CENTER);
		b_minus.setBackgroundColor(Color.TRANSPARENT);
		b_minus.setTextSize(18);
		b_minus.setText("-");
		b_minus.setId(12300001);
		layout2.addView(b_minus, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		RelativeLayout.LayoutParams pp2 = (RelativeLayout.LayoutParams) b_minus.getLayoutParams();
		pp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		b_minus.setLayoutParams(pp2);
		b_minus.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					mSeekBar.setProgress((mSeekBar.getProgress() - 1));
				}
				catch (Exception e)
				{
				}
			}
		});

		Button b_plus = new Button(mContext);
		b_plus.setGravity(Gravity.CENTER);
		b_plus.setBackgroundColor(Color.TRANSPARENT);
		b_plus.setTextSize(18);
		b_plus.setText("+");
		b_plus.setId(12300003);
		layout2.addView(b_plus, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		RelativeLayout.LayoutParams pp = (RelativeLayout.LayoutParams) b_plus.getLayoutParams();
		pp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		b_plus.setLayoutParams(pp);
		b_plus.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					mSeekBar.setProgress((mSeekBar.getProgress() + 1));
				}
				catch (Exception e)
				{
				}
			}
		});

		View v1 = new View(mContext);
		v1.setBackgroundColor(Color.LTGRAY);
		v1.setId(12300002);
		v1.setPadding(14, 0, 14, 0);
		layout2.addView(v1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 4));
		RelativeLayout.LayoutParams pp3 = (RelativeLayout.LayoutParams) v1.getLayoutParams();
		pp3.addRule(RelativeLayout.CENTER_IN_PARENT);
		pp3.addRule(RelativeLayout.RIGHT_OF, 12300001);
		pp3.addRule(RelativeLayout.LEFT_OF, 12300003);
		v1.setLayoutParams(pp3);

		//		SeekBar mSeekBar2 = new SeekBar(mContext);
		//		// mSeekBar2.setBackgroundColor(color.darker_gray);
		//		layout2.addView(mSeekBar2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		//		mSeekBar2.setMax(100);
		//		mSeekBar2.setProgress(100);
		//		// mSeekBar2.setPadding(200, 0, 200, 0);
		//		RelativeLayout.LayoutParams pp3 = (RelativeLayout.LayoutParams) mSeekBar2.getLayoutParams();
		//		pp3.addRule(RelativeLayout.CENTER_IN_PARENT);
		//		// pp3.addRule(RelativeLayout.RIGHT_OF, b_minus.getId());
		//		mSeekBar2.setLayoutParams(pp3);

		//
		//
		layout.addView(layout2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));

		if (shouldPersist()) mValue = getPersistedInt(mDefault);

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);

		return layout;
	}

	@Override
	protected void onBindDialogView(View v)
	{
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)
	{
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
		{
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		}
		else
		{
			mValue = (Integer) defaultValue;
		}
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// OnSeekBarChangeListener methods :
	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
	{
		String t = String.valueOf(value + mMin);
		mValueText.setText(mSuffix == null ? t : t.concat(" " + mSuffix));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seek)
	{
	}

	@Override
	public void onStopTrackingTouch(SeekBar seek)
	{
	}

	public void setMax(int max)
	{
		mMax = max;
	}

	public int getMax()
	{
		return mMax;
	}

	public void setProgress(int progress)
	{
		mValue = progress;
		if (mSeekBar != null) mSeekBar.setProgress(progress);
	}

	public int getProgress()
	{
		return mValue;
	}

	// ------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------
	// Set the positive button listener and onClick action : 
	@Override
	public void showDialog(Bundle state)
	{

		super.showDialog(state);

		Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (shouldPersist())
		{

			mValue = mSeekBar.getProgress();
			persistInt(mSeekBar.getProgress());
			callChangeListener(Integer.valueOf(mSeekBar.getProgress()));
		}

		((AlertDialog) getDialog()).dismiss();
	}
	// ------------------------------------------------------------------------------------------
}