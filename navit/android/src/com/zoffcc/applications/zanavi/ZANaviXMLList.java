package com.zoffcc.applications.zanavi;

import java.util.ArrayList;

public class ZANaviXMLList
{

	private ArrayList<String> route_geometry = new ArrayList<String>();

	public ZANaviXMLList()
	{
		route_geometry.clear();
	}

	public ArrayList<String> getGeom()
	{
		return route_geometry;
	}

	public void setGeom(String name)
	{
		this.route_geometry.add(name);
	}
}
