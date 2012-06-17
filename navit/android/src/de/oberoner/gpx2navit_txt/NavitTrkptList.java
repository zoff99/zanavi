/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2012 Zoff <zoff@zoff.cc>
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
 * Copyright (C) 2011 Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
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

package de.oberoner.gpx2navit_txt;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
 * Date: 16.10.11
 * Time: 13:05
 * To change this template use File | Settings | File Templates.
 */
public class NavitTrkptList
{

	protected Vector<NavitTrkptRec> navitTrkptList;

	// Konstruktor
	public NavitTrkptList()
	{
		navitTrkptList = new Vector<NavitTrkptRec>();
	}

	public int size()
	{
		return navitTrkptList.size();
	}

	public NavitTrkptRec get(int index)
	{
		return navitTrkptList.get(index);
	}

	public void add(NavitTrkptRec listItem)
	{
		navitTrkptList.add(listItem);
	}

}
