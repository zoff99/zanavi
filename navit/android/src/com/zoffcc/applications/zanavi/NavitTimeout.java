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

/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2008 Navit Team
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

import java.util.Random;

public class NavitTimeout extends Thread
{
	boolean event_multi;
	private int event_callbackid;
	private int event_timeout;
	Boolean running;
	private int randnum = 0;

	final Random myRandom = new Random();

	// public native void TimeoutCallback(int del, int id);

	NavitTimeout(int timeout, boolean multi, int callbackid)
	{
		this.randnum = myRandom.nextInt();
		//Log.e("Navit", "Create New Event #"+randnum+" - to=" + timeout + " multi=" + multi + " cid=" + callbackid);
		event_timeout = timeout;
		event_multi = multi;
		event_callbackid = callbackid;
		running = true;
		this.start();
		//Log.e("Navit", "Create New Event - READY");
	}

	public void run()
	{
		//Log.e("Navit", "Handle Event #"+randnum+" - run");

		while (running)
		{
			try
			{
				//Log.e("Navit", "Handle Event - sleep " + event_timeout + " millis");
				Thread.sleep(event_timeout, 0);
			}
			catch (InterruptedException e)
			{
				//e.printStackTrace();
			}

			if (running)
			{
				if (event_multi)
				{
					//Log.e("Navit", "Handle Event #"+randnum+" - to=" + event_timeout + " multi=" + event_multi + " cid=" + event_callbackid);
					Navit.cwthr.TimeoutCallback2(this, 0, event_callbackid);
				}
				else
				{
					//Log.e("Navit", "Handle Event #"+randnum+" - to=" + event_timeout + " multi=" + event_multi + " cid=" + event_callbackid);
					running = false;
					Navit.cwthr.TimeoutCallback2(this, 1, event_callbackid);
				}
			}
		}

		//Log.e("Navit", "Handle Event #"+randnum+" - end cid=" + event_callbackid);

		try
		{
			Thread.sleep(event_timeout, 1000); // sleep 1 secs. to wait for timeout remove call (in C code)
		}
		catch (InterruptedException e)
		{
		}

		//Log.e("Navit", "Handle Event #"+randnum+" - finish cid=" + event_callbackid);

	}

	public void remove()
	{
		//Log.e("Navit", "remove Event #"+randnum+" - to=" + event_timeout + " multi=" + event_multi + " cid=" + event_callbackid);
		running = false;
		this.interrupt();
	}
}
