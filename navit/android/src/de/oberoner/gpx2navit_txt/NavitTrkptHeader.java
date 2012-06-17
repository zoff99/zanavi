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

/**
 * Created by IntelliJ IDEA.
 * User: Dipl.-Ing.(FH) Thomas Schindel, Bad Elster
 * Date: 15.10.11
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */
public class NavitTrkptHeader
{

	protected String NavitType;
	protected String Label;
	protected String Desc;
	protected String Type;
	protected String Length;
	protected String Count;

	public NavitTrkptHeader()
	{
		NavitType = "gpx_track";
		Label = "";
		Desc = "\"\"";
		Type = "\"\"";
		Length = "";
		Count = "";
	}

	public String getNavitType()
	{
		return NavitType;
	}

	public void setNavitType(String navitType)
	{
		NavitType = navitType;
	}

	public String getLabel()
	{
		return Label;
	}

	public void setLabel(String label)
	{
		Label = label;
	}

	public String getDesc()
	{
		return Desc;
	}

	public void setDesc(String desc)
	{
		Desc = desc;
	}

	public String getType()
	{
		return Type;
	}

	public void setType(String type)
	{
		Type = type;
	}

	public String getLength()
	{
		return Length;
	}

	public void setLength(String length)
	{
		Length = length;
	}

	public String getCount()
	{
		return Count;
	}

	public void setCount(String count)
	{
		Count = count;
	}
}
