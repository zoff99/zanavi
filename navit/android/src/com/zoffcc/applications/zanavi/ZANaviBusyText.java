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

package com.zoffcc.applications.zanavi;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class ZANaviBusyText extends TextView
{
	final int text_size = 23;

	public ZANaviBusyText(Context context)
	{
		super(context);
		this.setPadding(0, 0, 0, (ZANaviBusySpinner.spinner_size * 2) + (2 * NavitGraphics.sp_to_px(text_size)) + 5); // place text above spinner
		this.setText("");
		this.setTextColor(Color.parseColor("#0000CC"));
		// this.setBackgroundColor(Color.parseColor("#00eeeeee"));
		this.setTextSize(TypedValue.COMPLEX_UNIT_SP, text_size);
		// this.setShadowLayer(2, 2, NavitGraphics.dp_to_px(1), Color.TRANSPARENT);
	}

	public ZANaviBusyText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.setPadding(0, 0, 0, (ZANaviBusySpinner.spinner_size * 2) + (2 * NavitGraphics.sp_to_px(text_size)) + 5); // place text above spinner
		this.setText("");
		this.setTextColor(Color.parseColor("#0000CC"));
		// this.setBackgroundColor(Color.parseColor("#00eeeeee"));
		this.setTextSize(TypedValue.COMPLEX_UNIT_SP, text_size);
		// this.setShadowLayer(2, 2, NavitGraphics.dp_to_px(1), Color.TRANSPARENT);
	}

	public static Handler UIHandler;
	static
	{
		UIHandler = new Handler(Looper.getMainLooper());
	}

	public static void runOnUI(Runnable runnable)
	{
		UIHandler.post(runnable);
	}

	public void setText2(String t)
	{
		// System.out.println("ZANaviBusyText:" + "setText2 t=" + t);
		final String t2 = t;

		try
		{
			runOnUI(new Runnable()
			{
				public void run()
				{
					setVisibility(View.VISIBLE);
					bringToFront();
					setText(t2);
				}
			});
		}
		catch (Exception e)
		{
			System.out.println("ZANaviBusyText:" + "EX:" + e.getMessage());
		}
	}

	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
