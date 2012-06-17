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
import android.view.Gravity;
import android.widget.TextView;

public class ZANaviBusyText extends TextView
{
	final int text_size = 18;

	public ZANaviBusyText(Context context)
	{
		super(context);
		this.setGravity(Gravity.CENTER);
		this.setPadding(0, 0, 0, (ZANaviBusySpinner.spinner_size * 2) + (2 * text_size) + 5); // place text above spinner
		this.setText("");
		this.setTextColor(Color.parseColor("#FF0000CF"));
		this.setTextSize(text_size);
	}

	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
