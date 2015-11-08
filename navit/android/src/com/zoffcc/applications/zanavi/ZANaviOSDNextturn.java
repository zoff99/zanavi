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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ZANaviOSDNextturn extends View
{

	private Paint paint = new Paint();
	private RectF f;

	public ZANaviOSDNextturn(Context context)
	{
		super(context);

		f = new RectF(0, 0, 1, 1);

		paint.setColor(Navit.OSD_blueish_bg_color);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(false);
	}

	public ZANaviOSDNextturn(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		f = new RectF(0, 0, 1, 1);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		f = new RectF(0, 0, w, h);
	}

	public void onDraw(Canvas c)
	{
		super.onDraw(c);

		try
		{
			if ((Navit.OSD_nextturn.nextturn_image_valid) && (NavitGraphics.CallbackDestinationValid2() > 0))
			{
				c.drawBitmap(Navit.OSD_nextturn.nextturn_image, null, f, null);
			}
			else
			{
				c.drawColor(Color.TRANSPARENT);
			}
		}
		catch (Exception e)
		{
		}
		
		// System.out.println("onDraw:OSDNextturn");

	}
}
