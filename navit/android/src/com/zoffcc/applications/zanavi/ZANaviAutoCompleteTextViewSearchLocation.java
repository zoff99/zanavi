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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class ZANaviAutoCompleteTextViewSearchLocation extends AutoCompleteTextView
{

	public ZANaviAutoCompleteTextViewSearchLocation(Context context)
	{
		super(context);

		this.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			@Override
			public void afterTextChanged(Editable arg0)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
		});

	}

	public ZANaviAutoCompleteTextViewSearchLocation(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		this.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			@Override
			public void afterTextChanged(Editable arg0)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
		});

	}

	public ZANaviAutoCompleteTextViewSearchLocation(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		this.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			@Override
			public void afterTextChanged(Editable arg0)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
		});

	}

	private Paint paint = new Paint();
	//	private int xx = 0;
	//	private float textHeight = 0;
	//	private float textOffset = 0;
	private float mt = 0;
	private int start_x = 0;
	private String this_text = "";
	private String text1 = "";
	private String text2 = "";
	private float space = 10;
	private int split_at = -1;

	private int color_street = 0xaa00ff00;
	private int color_town = 0xaa0000ff;

	private int line_thickness_in_dp = 5;

	protected void onDraw(Canvas c)
	{
		super.onDraw(c);

		try
		{
			if (Navit.use_index_search)
			{
				this_text = this.getText().toString();
				paint.setTextSize(this.getTextSize());
				// textHeight = paint.descent() - paint.ascent();
				// textOffset = (textHeight / 2) - paint.descent();

				split_at = this_text.lastIndexOf(" ");

				if ((this_text.length() > 1) && (split_at != -1) && (split_at != 0) && (split_at < (this_text.length() - 1)))
				{
					space = paint.measureText(" ");
					text1 = this_text.substring(0, split_at);
					text2 = this_text.substring(split_at);

					start_x = this.getPaddingLeft();
					mt = paint.measureText(text1);
					paint.setColor(color_street);
					paint.setStrokeWidth(NavitGraphics.dp_to_px(line_thickness_in_dp));
					paint.setStyle(Style.STROKE);
					c.drawLine(start_x, NavitGraphics.dp_to_px(line_thickness_in_dp), start_x + mt, NavitGraphics.dp_to_px(line_thickness_in_dp), paint);

					start_x = (int) (this.getPaddingLeft() + mt + space);
					mt = paint.measureText(text2);
					paint.setColor(color_town);
					c.drawLine(start_x, NavitGraphics.dp_to_px(line_thickness_in_dp), start_x + mt - space, NavitGraphics.dp_to_px(line_thickness_in_dp), paint);
				}
				else
				{
					start_x = this.getPaddingLeft();
					mt = paint.measureText(this_text);

					paint.setColor(color_street);
					paint.setStrokeWidth(NavitGraphics.dp_to_px(line_thickness_in_dp));
					paint.setStyle(Style.STROKE);
					c.drawLine(start_x, NavitGraphics.dp_to_px(line_thickness_in_dp), start_x + mt, NavitGraphics.dp_to_px(line_thickness_in_dp), paint);
				}
			}
		}
		catch (Exception e)
		{
		}
	}
}
