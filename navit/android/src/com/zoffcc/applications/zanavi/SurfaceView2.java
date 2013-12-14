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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class SurfaceView2 extends SurfaceView implements SurfaceHolder.Callback
{
	private SurfaceHolder holder;

	public SurfaceView2(Context context)
	{
		super(context);
		// this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		holder = getHolder();
		holder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{
	}

	@SuppressLint("WrongCall")
	public void paint_me()
	{
		System.out.println("XY:paint_me");

		Canvas c = null;
		try
		{
			c = holder.lockCanvas(null);
			synchronized (holder)
			{
				// System.out.println(String.valueOf(c));
				onDraw(c);
			}
		}
		finally
		{
			if (c != null)
			{
				holder.unlockCanvasAndPost(c);
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
	}

}
