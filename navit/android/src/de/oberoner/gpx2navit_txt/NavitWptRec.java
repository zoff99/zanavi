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
 * Date: 16.10.11
 * Time: 12:53
 * To change this template use File | Settings | File Templates.
 */
public class NavitWptRec
{

	protected String type;
	protected String label;
	protected String description;
	protected String gc_type;
	protected String lat;
	protected String lon;

	public NavitWptRec()
	{
		type = "gpx_point";
		label = "";
		description = "";
		gc_type = "";
		lat = "";
		lon = "";
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getGc_type()
	{
		return gc_type;
	}

	public void setGc_type(String gc_type)
	{
		this.gc_type = gc_type;
	}

	public String getLat()
	{
		return lat;
	}

	public void setLat(String lat)
	{
		this.lat = lat;
	}

	public String getLon()
	{
		return lon;
	}

	public void setLon(String lon)
	{
		this.lon = lon;
	}
}
