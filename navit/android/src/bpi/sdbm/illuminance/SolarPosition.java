/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 - 2012 Zoff <zoff@zoff.cc>
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

package bpi.sdbm.illuminance;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 
 * Compute sun position for a given date/time and longitude/latitude.
 * 
 * This is a simple Java port of the "PSA" solar positioning algorithm, as
 * documented in:
 * 
 * Blanco-Muriel et al.: Computing the Solar Vector. Solar Energy Vol 70 No 5
 * pp 431-441. http://dx.doi.org/10.1016/S0038-092X(00)00156-0
 * 
 * According to the paper, "The algorithm allows .. the true solar vector to
 * be determined with an accuracy of 0.5 minutes of arc for the period 1999 - 2015."
 * 
 * @author Klaus A. Brunner
 * 
 */

public class SolarPosition
{

	private static final double dEarthMeanRadius = 6371.01; // in km
	private static final double dAstronomicalUnit = 149597890; // in km

	private static final double pi = Math.PI;
	private static final double twopi = (2 * pi);
	private static final double rad = (pi / 180);

	/** result wrapper class */
	public static class SunCoordinates
	{
		/**
		 * zenith angle, in degrees
		 */
		public double zenithAngle;

		/**
		 * azimuth, in degrees, 0 - 360 degrees clockwise from north
		 */
		public double azimuth;
	}

	/**
	 * Calculate sun position for a given time and location.
	 * 
	 * @param time
	 *            Note that it's unclear how well the algorithm performs before the year 1990 or after the year 2015.
	 * @param latitude
	 *            (positive east of Greenwich)
	 * @param longitude
	 *            (positive north of equator)
	 * @return
	 */
	public static SunCoordinates getSunPosition(final Date time, double latitude, double longitude)
	{

		SunCoordinates retval = new SunCoordinates();

		Calendar utcTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		utcTime.setTimeInMillis(time.getTime());

		// Main variables
		double dElapsedJulianDays;
		double dDecimalHours;
		double dEclipticLongitude;
		double dEclipticObliquity;
		double dRightAscension;
		double dDeclination;

		// Auxiliary variables
		double dY;
		double dX;

		// Calculate difference in days between the current Julian Day
		// and JD 2451545.0, which is noon 1 January 2000 Universal Time

		{
			long liAux1;
			long liAux2;
			double dJulianDate;
			// Calculate time of the day in UT decimal hours
			dDecimalHours = utcTime.get(Calendar.HOUR_OF_DAY) + (utcTime.get(Calendar.MINUTE) + utcTime.get(Calendar.SECOND) / 60.0) / 60.0;
			// Calculate current Julian Day
			liAux1 = (utcTime.get(Calendar.MONTH) + 1 - 14) / 12;
			liAux2 = (1461 * (utcTime.get(Calendar.YEAR) + 4800 + liAux1)) / 4 + (367 * (utcTime.get(Calendar.MONTH) + 1 - 2 - 12 * liAux1)) / 12 - (3 * ((utcTime.get(Calendar.YEAR) + 4900 + liAux1) / 100)) / 4 + utcTime.get(Calendar.DAY_OF_MONTH) - 32075;
			dJulianDate = (double) (liAux2) - 0.5 + dDecimalHours / 24.0;
			// Calculate difference between current Julian Day and JD 2451545.0
			dElapsedJulianDays = dJulianDate - 2451545.0;
		}

		// System.err.println("elapsed julian " + dElapsedJulianDays);
		// System.err.println("decimal hrs " + dDecimalHours);

		// Calculate ecliptic coordinates (ecliptic longitude and obliquity of
		// the
		// ecliptic in radians but without limiting the angle to be less than
		// 2*Pi
		// (i.e., the result may be greater than 2*Pi)
		{
			double dMeanLongitude;
			double dMeanAnomaly;
			double dOmega;
			dOmega = 2.1429 - 0.0010394594 * dElapsedJulianDays;
			dMeanLongitude = 4.8950630 + 0.017202791698 * dElapsedJulianDays; // Radians
			dMeanAnomaly = 6.2400600 + 0.0172019699 * dElapsedJulianDays;
			dEclipticLongitude = dMeanLongitude + 0.03341607 * Math.sin(dMeanAnomaly) + 0.00034894 * Math.sin(2 * dMeanAnomaly) - 0.0001134 - 0.0000203 * Math.sin(dOmega);
			dEclipticObliquity = 0.4090928 - 6.2140e-9 * dElapsedJulianDays + 0.0000396 * Math.cos(dOmega);
		}

		// System.err.println("ecl. longi. " + dEclipticLongitude);
		// System.err.println("ecl. obliq. " + dEclipticObliquity);

		// Calculate celestial coordinates ( right ascension and declination )
		// in radians
		// but without limiting the angle to be less than 2*Pi (i.e., the result
		// may be
		// greater than 2*Pi)
		{
			double dSin_EclipticLongitude;
			dSin_EclipticLongitude = Math.sin(dEclipticLongitude);
			dY = Math.cos(dEclipticObliquity) * dSin_EclipticLongitude;
			dX = Math.cos(dEclipticLongitude);
			dRightAscension = Math.atan2(dY, dX);
			if (dRightAscension < 0.0) dRightAscension = dRightAscension + 2 * Math.PI;
			dDeclination = Math.asin(Math.sin(dEclipticObliquity) * dSin_EclipticLongitude);
		}

		// System.err.println("right asc " + dRightAscension);
		// System.err.println("decl. " + dDeclination);

		// Calculate local coordinates ( azimuth and zenith angle ) in degrees
		{
			double dGreenwichMeanSiderealTime;
			double dLocalMeanSiderealTime;
			double dLatitudeInRadians;
			double dHourAngle;
			double dCos_Latitude;
			double dSin_Latitude;
			double dCos_HourAngle;
			double dParallax;
			dGreenwichMeanSiderealTime = 6.6974243242 + 0.0657098283 * dElapsedJulianDays + dDecimalHours;
			dLocalMeanSiderealTime = (dGreenwichMeanSiderealTime * 15 + longitude) * rad;
			dHourAngle = dLocalMeanSiderealTime - dRightAscension;
			dLatitudeInRadians = latitude * rad;
			dCos_Latitude = Math.cos(dLatitudeInRadians);
			dSin_Latitude = Math.sin(dLatitudeInRadians);
			dCos_HourAngle = Math.cos(dHourAngle);
			retval.zenithAngle = (Math.acos(dCos_Latitude * dCos_HourAngle * Math.cos(dDeclination) + Math.sin(dDeclination) * dSin_Latitude));
			dY = -Math.sin(dHourAngle);
			dX = Math.tan(dDeclination) * dCos_Latitude - dSin_Latitude * dCos_HourAngle;
			retval.azimuth = Math.atan2(dY, dX);
			if (retval.azimuth < 0.0) retval.azimuth = retval.azimuth + twopi;
			retval.azimuth = retval.azimuth / rad;
			// Parallax Correction
			dParallax = (dEarthMeanRadius / dAstronomicalUnit) * Math.sin(retval.zenithAngle);
			retval.zenithAngle = (retval.zenithAngle + dParallax) / rad;
		}

		return retval;
	}

	public static void main(String[] args)
	{

		SolarPosition.getSunPosition(new Date(), 48.2, 16.4);
		//System.out.println("current azimuth " + sc.azimuth);
		//System.out.println("current zenith angle " + sc.zenithAngle);

	}
}
