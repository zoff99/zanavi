/*
 * Copyright 2008-2009 Mike Reedell / LuckyCatLabs.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luckycatlabs.sunrisesunset.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.dto.Location2;

/**
 * Parent class of the Sunrise and Sunset calculator classes.
 */
public class SolarEventCalculator
{
	private Location2	location;
	private TimeZone	timeZone;

	/**
	 * Constructs a new <code>SolarEventCalculator</code> using the given parameters.
	 * 
	 * @param location
	 *           <code>Location</code> of the place where the solar event should be calculated from.
	 * @param timeZoneIdentifier
	 *           time zone identifier of the timezone of the location parameter. For example,
	 *           "America/New_York".
	 */
	public SolarEventCalculator(Location2 location, String timeZoneIdentifier)
	{
		this.location = location;
		this.timeZone = TimeZone.getTimeZone(timeZoneIdentifier);
	}

	public double div(double a, double b)
	{
		int x;
		int y;
		x = (int) a;
		y = (int) b;
		return (double) (x / y);
	}

	public double Rev(double input)
	{
		double x;
		x = input - Math.floor(input / 360.0) * 360;
		return (x);
	}

	public double Radians(double input)
	{
		return Math.toRadians(input);
	}

	public double Deg(double input)
	{
		return Math.toDegrees(input);
	}

	public double ElevationRefraction(double El_geometric)
	{
		double El_observed;
		double x, a0, a1, a2, a3, a4;
		a0 = 0.58804392;
		a1 = -0.17941557;
		a2 = 0.29906946E-1;
		a3 = -0.25187400E-2;
		a4 = 0.82622101E-4;
		El_observed = El_geometric;
		x = Math.abs(El_geometric + 0.589);
		double refraction = Math.abs(a0 + a1 * x + a2 * x * x + a3 * x * x * x + a4 * x * x * x * x);

		if (El_geometric > 10.2)
		{
			El_observed = El_geometric
					+ 0.01617
					* (Math.cos(Radians(Math.abs(El_geometric))) / Math.sin(Radians(Math
							.abs(El_geometric))));
		}
		else
		{
			El_observed = El_geometric + refraction;

		}
		return El_observed;
	}

	public double CalcJD(double day, double month, double year)
	{
		double jd = 2415020.5 - 64; // 1.1.1900 - correction of algorithm
		if (month <= 2)
		{
			year = year - 1;
			month += 12;
		}
		jd += (double) ((int) ((year - 1900) * 365.25));
		jd += (double) ((int) (30.6001 * (1 + month)));
		return (jd + day);
	}

	public double frac(double x)
	{
		return (x - Math.floor(x));
	}

	public double Mod(double a, double b)
	{
		return (a - Math.floor(a / b) * b);
	}

	public double GMST(double JD)
	{
		double UT = frac(JD - 0.5) * 24.; // UT in hours
		JD = Math.floor(JD - 0.5) + 0.5; // JD at 0 hours UT
		double T = (JD - 2451545.0) / 36525.0;
		double T0 = 6.697374558 + T * (2400.051336 + T * 0.000025862);
		return (Mod(T0 + UT * 1.002737909, 24.));
	}

	public double GMST2LMST(double gmst, double lon)
	{
		double RAD = 180.0 / Math.PI;
		double lmst = Mod(gmst + RAD * lon / 15, 24.);
		return (lmst);
	}

	public class cart_ret
	{
		double	x;
		double	y;
		double	z;
		double	radius;
		double	lat;
		double	lon;
	}

	public cart_ret EquPolar2Cart(double lon, double lat, double distance)
	{
		cart_ret cart = new cart_ret();
		double rcd = Math.cos(lat) * distance;
		cart.x = rcd * Math.cos(lon);
		cart.y = rcd * Math.sin(lon);
		cart.z = distance * Math.sin(lat);
		return (cart);
	}

	public cart_ret Observer2EquCart(double lon, double lat, double height, double gmst)
	{
		double flat = 298.257223563; // WGS84 flatening of earth
		double aearth = 6378.137; // GRS80/WGS84 semi major axis of earth ellipsoid
		cart_ret cart = new cart_ret();
		// Calculate geocentric latitude from geodetic latitude
		double co = Math.cos(lat);
		double si = Math.sin(lat);
		double fl = 1.0 - 1.0 / flat;
		fl = fl * fl;
		si = si * si;
		double u = 1.0 / Math.sqrt(co * co + fl * si);
		double a = aearth * u + height;
		double b = aearth * fl * u + height;
		double radius = Math.sqrt(a * a * co * co + b * b * si); // geocentric distance from earth center
		cart.y = Math.acos(a * co / radius); // geocentric latitude, rad
		cart.x = lon; // longitude stays the same
		if (lat < 0.0)
		{
			cart.y = -cart.y;
		} // adjust sign
		cart = EquPolar2Cart(cart.x, cart.y, radius); // convert from geocentric polar to geocentric cartesian, with regard to Greenwich
		// rotate around earth's polar axis to align coordinate system from Greenwich to vernal equinox
		double x = cart.x;
		double y = cart.y;
		double rotangle = gmst / 24 * 2 * Math.PI; // sideral time gmst given in hours. Convert to radians
		cart.x = x * Math.cos(rotangle) - y * Math.sin(rotangle);
		cart.y = x * Math.sin(rotangle) + y * Math.cos(rotangle);
		cart.radius = radius;
		cart.lon = lon;
		cart.lat = lat;
		return (cart);
	}

	public class moonCoor_ret
	{
		public double	lon;
		public double	lat;
		public double	orbitLon;
		public double	distance;
		public double	diameter;
		public double	parallax;
		public double	raGeocentric;
		public double	decGeocentric;
		public double	ra;
		public double	dec;
		public double	raTopocentric;
		public double	decTopocentric;
		public double	distanceTopocentric;
		public double	moonAge;
		public double	phase;
		public double	az;
		public double	alt;
		public String	moonPhase;
		public String	sign;
	}

	public double Mod2Pi(double x)
	{
		x = Mod(x, 2. * Math.PI);
		return (x);
	}

	public moonCoor_ret Ecl2Equ(moonCoor_ret coor, double TDT)
	{
		double pi = Math.PI;
		double DEG = pi / 180.0;

		double T = (TDT - 2451545.0) / 36525.; // Epoch 2000 January 1.5
		double eps = (23. + (26 + 21.45 / 60.) / 60. + T * (-46.815 + T * (-0.0006 + T * 0.00181))
				/ 3600.)
				* DEG;
		double coseps = Math.cos(eps);
		double sineps = Math.sin(eps);

		double sinlon = Math.sin(coor.lon);
		coor.ra = Mod2Pi(Math.atan2((sinlon * coseps - Math.tan(coor.lat) * sineps), Math
				.cos(coor.lon)));
		coor.dec = Math.asin(Math.sin(coor.lat) * coseps + Math.cos(coor.lat) * sineps * sinlon);

		return coor;
	}

	public moonCoor_ret GeoEqu2TopoEqu(moonCoor_ret coor, cart_ret observer, double lmst)
	{
		double cosdec = Math.cos(coor.dec);
		double sindec = Math.sin(coor.dec);
		double coslst = Math.cos(lmst);
		double sinlst = Math.sin(lmst);
		double coslat = Math.cos(observer.lat); // we should use geocentric latitude, not geodetic latitude
		double sinlat = Math.sin(observer.lat);
		double rho = observer.radius; // observer-geocenter in Kilometer

		double x = coor.distance * cosdec * Math.cos(coor.ra) - rho * coslat * coslst;
		double y = coor.distance * cosdec * Math.sin(coor.ra) - rho * coslat * sinlst;
		double z = coor.distance * sindec - rho * sinlat;

		coor.distanceTopocentric = Math.sqrt(x * x + y * y + z * z);
		coor.decTopocentric = Math.asin(z / coor.distanceTopocentric);
		coor.raTopocentric = Mod2Pi(Math.atan2(y, x));

		return coor;
	}

	public moonCoor_ret Equ2Altaz(moonCoor_ret coor, double TDT, double geolat, double lmst)
	{
		double cosdec = Math.cos(coor.dec);
		double sindec = Math.sin(coor.dec);
		double lha = lmst - coor.ra;
		double coslha = Math.cos(lha);
		double sinlha = Math.sin(lha);
		double coslat = Math.cos(geolat);
		double sinlat = Math.sin(geolat);

		double N = -cosdec * sinlha;
		double D = sindec * coslat - cosdec * coslha * sinlat;
		coor.az = Mod2Pi(Math.atan2(N, D));
		coor.alt = Math.asin(sindec * sinlat + cosdec * coslha * coslat);

		return coor;
	}

	public class sunCoor_ret
	{
		double	lon;
		double	anomalyMean;
	}

	public moonCoor_ret MoonPosition(sunCoor_ret sunCoor, double TDT, cart_ret observer, double lmst)
	{
		double D = TDT - 2447891.5;

		double pi = Math.PI;
		double DEG = pi / 180.0;

		// Mean Moon orbit elements as of 1990.0
		double l0 = 318.351648 * DEG;
		double P0 = 36.340410 * DEG;
		double N0 = 318.510107 * DEG;
		double i = 5.145396 * DEG;
		double e = 0.054900;
		double a = 384401; // km
		double diameter0 = 0.5181 * DEG; // angular diameter of Moon at a distance
		double parallax0 = 0.9507 * DEG; // parallax at distance a

		double l = 13.1763966 * DEG * D + l0;
		double MMoon = l - 0.1114041 * DEG * D - P0; // Moon's mean anomaly M
		double N = N0 - 0.0529539 * DEG * D; // Moon's mean ascending node longitude
		double C = l - sunCoor.lon;
		double Ev = 1.2739 * DEG * Math.sin(2 * C - MMoon);
		double Ae = 0.1858 * DEG * Math.sin(sunCoor.anomalyMean);
		double A3 = 0.37 * DEG * Math.sin(sunCoor.anomalyMean);
		double MMoon2 = MMoon + Ev - Ae - A3; // corrected Moon anomaly
		double Ec = 6.2886 * DEG * Math.sin(MMoon2); // equation of centre
		double A4 = 0.214 * DEG * Math.sin(2 * MMoon2);
		double l2 = l + Ev + Ec - Ae + A4; // corrected Moon's longitude
		double V = 0.6583 * DEG * Math.sin(2 * (l2 - sunCoor.lon));
		double l3 = l2 + V; // true orbital longitude;

		double N2 = N - 0.16 * DEG * Math.sin(sunCoor.anomalyMean);

		moonCoor_ret moonCoor = new moonCoor_ret();
		moonCoor.lon = Mod2Pi(N2 + Math.atan2(Math.sin(l3 - N2) * Math.cos(i), Math.cos(l3 - N2)));
		moonCoor.lat = Math.asin(Math.sin(l3 - N2) * Math.sin(i));
		moonCoor.orbitLon = l3;

		moonCoor = Ecl2Equ(moonCoor, TDT);
		// relative distance to semi mayor axis of lunar oribt
		moonCoor.distance = (1 - Math.sqrt(e)) / (1 + e * Math.cos(MMoon2 + Ec));
		moonCoor.diameter = diameter0 / moonCoor.distance; // angular diameter in radians
		moonCoor.parallax = parallax0 / moonCoor.distance; // horizontal parallax in radians
		moonCoor.distance *= a; // distance in km

		// Calculate horizonal coordinates of sun, if geographic positions is given
		if ((observer != null) && (lmst != 0.0))
		{
			// transform geocentric coordinates into topocentric (==observer based) coordinates
			moonCoor = GeoEqu2TopoEqu(moonCoor, observer, lmst);
			moonCoor.raGeocentric = moonCoor.ra; // backup geocentric coordinates
			moonCoor.decGeocentric = moonCoor.dec;
			moonCoor.ra = moonCoor.raTopocentric;
			moonCoor.dec = moonCoor.decTopocentric;
			moonCoor = Equ2Altaz(moonCoor, TDT, observer.lat, lmst); // now ra and dec are topocentric
		}

		// Age of Moon in radians since New Moon (0) - Full Moon (pi)
		moonCoor.moonAge = Mod2Pi(l3 - sunCoor.lon);
		moonCoor.phase = 0.5 * (1 - Math.cos(moonCoor.moonAge)); // Moon phase, 0-1

		//String[] phases = {"Neumond", "Zunehmende Sichel", "Erstes Viertel", "Zunehmender Mond",
		//		"Vollmond", "Abnehmender Mond", "Letztes Viertel", "Abnehmende Sichel", "Neumond"};
		double mainPhase = 1. / 29.53 * 360 * DEG; // show 'Newmoon, 'Quarter' for +/-1 day arond the actual event
		double p = Mod(moonCoor.moonAge, 90. * DEG);
		if (p < mainPhase || p > 90 * DEG - mainPhase)
			p = 2 * Math.round(moonCoor.moonAge / (90. * DEG));
		else
			p = 2 * Math.floor(moonCoor.moonAge / (90. * DEG)) + 1;
		// moonCoor.moonPhase = phases[p];

		// moonCoor.sign = Sign(moonCoor.lon);

		return (moonCoor);
	}

	public double Refraction(double alt)
	{
		double pi = Math.PI;
		double DEG = pi / 180.0;
		double RAD = 180.0 / pi;

		double altdeg = alt * RAD;
		if (altdeg < -2 || altdeg >= 90) return (0);

		double pressure = 1015;
		double temperature = 10;
		if (altdeg > 15) return (0.00452 * pressure / ((273 + temperature) * Math.tan(alt)));

		double y = alt;
		double D = 0.0;
		double P = (pressure - 80.) / 930.;
		double Q = 0.0048 * (temperature - 10.);
		double y0 = y;
		double D0 = D;

		for (int i = 0; i < 3; i++)
		{
			double N = y + (7.31 / (y + 4.4));
			N = 1. / Math.tan(N * DEG);
			D = N * P / (60. + Q * (N + 39.));
			N = y - y0;
			y0 = D - D0 - N;
			if ((N != 0.) && (y0 != 0.))
			{
				N = y - N * (alt + D - y) / y0;
			}
			else
			{
				N = alt + D;
			}
			y0 = y;
			D0 = D;
			y = N;
		}
		return (D); // Hebung durch Refraktion in radians
	}

	public moonCoor_ret computeMoonStats(Calendar date)
	{
		double pi = Math.PI;
		double DEG = pi / 180.0;
		double RAD = 180.0 / pi;

		date.setTimeZone(TimeZone.getTimeZone("UTC"));

		double JD0 = CalcJD((double) date.get(Calendar.DAY_OF_MONTH), (double) date
				.get(Calendar.MONTH) + 1, (double) date.get(Calendar.YEAR));
		double JD = JD0
				+ ((double) date.get(Calendar.HOUR_OF_DAY) + (double) date.get(Calendar.MINUTE) / 60 + (double) date
						.get(Calendar.SECOND) / 3600) / 24;
		// UTC
		double TDT = JD;
		//System.out.println("TDT=" + TDT);

		double lat = this.location.getLatitude().doubleValue() * DEG; // geodetic latitude of observer on WGS84
		double lon = this.location.getLongitude().doubleValue() * DEG; // latitude of observer
		double height = 0 * 0.001; // altiude of observer in meters above WGS84 ellipsoid (and converted to kilometers)
		double gmst = GMST(JD);
		double lmst = GMST2LMST(gmst, lon);

		cart_ret observerCart = Observer2EquCart(lon, lat, height, gmst); // geocentric cartesian coordinates of observer

		//		  sunCoor  = SunPosition(TDT, lat, lmst*15.*DEG);   // Calculate data for the Sun at given time
		sunCoor_ret sunCoor = new sunCoor_ret();
		BigDecimal longitudeHour = getLongitudeHour(date, true);
		BigDecimal meanAnomaly = getMeanAnomaly(longitudeHour);
		sunCoor.lon = longitudeHour.doubleValue();
		sunCoor.anomalyMean = meanAnomaly.doubleValue();
		moonCoor_ret moonCoor = MoonPosition(sunCoor, TDT, observerCart, lmst * 15. * DEG); // Calculate data for the Moon at given time
		moonCoor.az = moonCoor.az * RAD;
		moonCoor.alt = moonCoor.alt * RAD + Refraction(moonCoor.alt);
		//System.out.println("moon azimuth=" + moonCoor.az);
		//System.out.println("moon elevation=" + moonCoor.alt);

		return moonCoor;
	}

	public void computeMoonStats2(Calendar date)
	{
		date.setTimeZone(TimeZone.getTimeZone("UTC"));

		double Year = date.get(Calendar.YEAR);
		double Month = date.get(Calendar.MONTH) + 1; // month starts from "0" zero !!
		double Day = date.get(Calendar.DAY_OF_MONTH);
		double Hour = date.get(Calendar.HOUR_OF_DAY);
		double Minute = date.get(Calendar.MINUTE);
		double Second = date.get(Calendar.SECOND);
		double d = 367 * Year - div((7 * (Year + (div((Month + 9), 12)))), 4) + div((275 * Month), 9)
				+ Day - 730530;
		//System.out.println("dd=" + d);
		d = d + Hour / 24 + Minute / (60 * 24) + Second / (24 * 60 * 60); // OK

		//		System.out.println("y=" + Year);
		//		System.out.println("m=" + Month);
		//		System.out.println("d=" + Day);
		//		System.out.println("h=" + Hour);
		//		System.out.println("m=" + Minute);
		//		System.out.println("s=" + Second);
		//		System.out.println("dd=" + d);
		//		System.out.println("c=" + date.getTimeInMillis());

		double N = 125.1228 - 0.0529538083 * d;
		double i = 5.1454;

		double w = 318.0634 + 0.1643573223 * d; //OK
		double a = 60.2666;
		//a=6.6107940559473451507806351067866;
		//a=0;

		//a=149476000; //km average distance
		double e = 0.054900;
		double M = 115.3654 + 13.0649929509 * d;

		w = Rev(w);
		M = Rev(M);
		N = Rev(N);

		double E = M + (180 / Math.PI) * e * Math.sin(Math.toRadians(M))
				* (1 + e * Math.cos(Math.toRadians(M)));
		E = Rev(E); // OK

		double Ebeforeit = E;
		// now iterate until difference between E0 and E1 is less than 0.005_deg
		// use E0, calculate E1

		int Iterations = 0;
		double E_error = 9;
		double E0;
		double E1;
		//double Eafterit;
		//double E_ErrorBefore;

		while ((E_error > 0.0005) && (Iterations < 20)) // ok - itererer korrekt
		{
			Iterations = Iterations + 1;
			E0 = E;
			E1 = E0 - (E0 - (180 / Math.PI) * e * Math.sin(Math.toRadians(E0)) - M)
					/ (1 - e * Math.cos(Math.toRadians(E0)));
			//alert('1 E0='+E0+'\nNew E1='+E1+'\nE='+E+'\Diff='+Rev(E0-E1));
			E = Rev(E1);
			//alert(Math.abs(E-E0));

			//Eafterit = E;

			if (E < E0)
			{
				E_error = E0 - E;
			}
			else
			{
				E_error = E - E0;
			}

			//			if (E < Ebeforeit)
			//			{
			//				//E_ErrorBefore = Ebeforeit - E;
			//			}
			//
			//			else
			//			{
			//				//E_ErrorBefore = E - Ebeforeit;
			//			}

			//System.out.println("(loop) E=" + E);
		}

		double x = a * (Math.cos(Math.toRadians(E)) - e);
		double y = a * Math.sin(Math.toRadians(Rev(E))) * Math.sqrt(1 - e * e);
		double r = Math.sqrt(x * x + y * y);
		double v = Math.toDegrees(Math.atan2(y, x));

		//System.out.println("E=" + E);

		x = a * (Math.cos(Math.toRadians(E)) - e);
		y = a * Math.sin(Math.toRadians(Rev(E))) * Math.sqrt(1 - e * e);
		r = Math.sqrt(x * x + y * y);
		v = Math.toDegrees(Math.atan2(y, x));

		//alert('E='+E);

		// ok så langt

		double sunlon = Rev(v + w); // trolig ok

		x = r * Math.cos(Math.toRadians(sunlon));
		y = r * Math.sin(Math.toRadians(sunlon));
		double z = 0;

		double xeclip = r
				* (Math.cos(Math.toRadians(N)) * Math.cos(Math.toRadians(v + w)) - Math.sin(Math
						.toRadians(N))
						* Math.sin(Math.toRadians(v + w)) * Math.cos(Math.toRadians(i)));
		double yeclip = r
				* (Math.sin(Math.toRadians(N)) * Math.cos(Math.toRadians(v + w)) + Math.cos(Math
						.toRadians(N))
						* Math.sin(Math.toRadians(v + w)) * Math.cos(Math.toRadians(i)));
		double zeclip = r * Math.sin(Math.toRadians(v + w)) * Math.sin(Math.toRadians(i));

		double moon_longitude = Rev(Math.toDegrees(Math.atan2(yeclip, xeclip))); // OK
		double moon_latitude = Math.toDegrees(Math.atan2(zeclip, Math.sqrt(xeclip * xeclip + yeclip
				* yeclip))); // trolig OK

		// ----------- SUN -----------
		// ----------- SUN -----------
		// date.setTimeZone(this.timeZone);
		BigDecimal longitudeHour = getLongitudeHour(date, true);
		BigDecimal meanAnomaly = getMeanAnomaly(longitudeHour);
		//BigDecimal sunTrueLong = getSunTrueLongitude(meanAnomaly);
		//BigDecimal cosineSunLocalHour = getCosineSunLocalHour(sunTrueLong, Zenith.OFFICIAL);

//		System.out.println("Sun MA=" + meanAnomaly);

		// geschätzt!!!!!
		// see -> http://www.obliquity.com/info/meaning.html
		double sun_Obliquity = 23.45;
		// geschätzt!!!!!

		// sunangles[11] ????
		double w_S = 282.9404 + 4.70935E-5 * d; //OK
		//double a_S = 1;
		//a=6.6107940559473451507806351067866;
		//a=0;

		//a=149476000; //km average distance
		//double e_S = 0.016709 - 1.151E-9 * d;
		double M_S = 356.0470 + 0.9856002585 * d;
		double oblecl_S = 23.4393 - 3.563E-7 * d;
		double L_S = w_S + Rev(M_S);
		double GMST0_sun = (L_S + 180);

		L_S = Rev(L_S);
		sun_Obliquity = oblecl_S;

//		System.out.println("GMST0_sun=" + GMST0_sun);
//		System.out.println("L_S=" + L_S);
//		System.out.println("oblecl_S=" + oblecl_S);
		// sunangles[11] ????
		// ----------- SUN -----------
		// ----------- SUN -----------


		double Mm = Rev(M); // Moons mean anomaly
		double Lm = Rev(N + w + M); // moon mean longitude
		double Ms = meanAnomaly.doubleValue(); // sun mean anomaly
		//double Ls = sunTrueLong.doubleValue(); // sun mean longtitude
		double Ls = L_S;
		double D = Rev(Lm - Ls); //Moons mean elongation
		double F = Rev(Lm - N); //Moons argument of latitude


		// Perbutations Moons Longitude

		double P_lon1 = -1.274 * Math.sin(Radians(Mm - 2 * D)); //  (Evection)
		double P_lon2 = +0.658 * Math.sin(Radians(2 * D)); //    (Variation)
		double P_lon3 = -0.186 * Math.sin(Radians(Ms)); //    (Yearly equation)
		double P_lon4 = -0.059 * Math.sin(Radians(2 * Mm - 2 * D));
		double P_lon5 = -0.057 * Math.sin(Radians(Mm - 2 * D + Ms));
		double P_lon6 = +0.053 * Math.sin(Radians(Mm + 2 * D));
		double P_lon7 = +0.046 * Math.sin(Radians(2 * D - Ms));
		double P_lon8 = +0.041 * Math.sin(Radians(Mm - Ms));
		double P_lon9 = -0.035 * Math.sin(Radians(D)); //      (Parallactic equation)
		double P_lon10 = -0.031 * Math.sin(Radians(Mm + Ms));
		double P_lon11 = -0.015 * Math.sin(Radians(2 * F - 2 * D));
		double P_lon12 = +0.011 * Math.sin(Radians(Mm - 4 * D));
		// Perbutations Moons Latitude


		double P_lat1 = -0.173 * Math.sin(Radians(F - 2 * D));
		double P_lat2 = -0.055 * Math.sin(Radians(Mm - F - 2 * D));
		double P_lat3 = -0.046 * Math.sin(Radians(Mm + F - 2 * D));
		double P_lat4 = +0.033 * Math.sin(Radians(F + 2 * D));
		double P_lat5 = +0.017 * Math.sin(Radians(2 * Mm + F));

		double P_lon = P_lon1 + P_lon2 + P_lon3 + P_lon4 + P_lon5 + P_lon6 + P_lon7 + P_lon8 + P_lon9
				+ P_lon10 + P_lon11 + P_lon12;
		double P_lat = P_lat1 + P_lat2 + P_lat3 + P_lat4 + P_lat5;
		double P_moondistance = -0.58 * Math.cos(Radians(Mm - 2 * D)) - 0.46
				* Math.cos(Radians(2 * D));

		//alert('P_lon='+P_lon+'\nP_lat='+P_lat+'\nP_moondistance='+P_moondistance);

		moon_longitude = moon_longitude + P_lon;
		moon_latitude = moon_latitude + P_lat;
		r = r + P_moondistance;

		// OK so far
		// now calculate RA & Decl
		// get the Eliptic coordinates

		double xh = r * Math.cos(Radians(moon_longitude)) * Math.cos(Radians(moon_latitude));
		double yh = r * Math.sin(Radians(moon_longitude)) * Math.cos(Radians(moon_latitude));
		double zh = r * Math.sin(Radians(moon_latitude));
		// rotate to rectangular equatorial coordinates
		double xequat = xh;

		double yequat = yh * Math.cos(Radians(sun_Obliquity)) - zh * Math.sin(Radians(sun_Obliquity));
		double zequat = yh * Math.sin(Radians(sun_Obliquity)) + zh * Math.cos(Radians(sun_Obliquity));
		double Moon_RA = Rev(Deg(Math.atan2(yh, xh))); // OK
		double Moon_Decl = Deg(Math.atan2(zh, Math.sqrt(xh * xh + yh * yh))); // trolig OK

		Moon_RA = Rev(Deg(Math.atan2(yequat, xequat))); // OK
		Moon_Decl = Deg(Math.atan2(zequat, Math.sqrt(xequat * xequat + yequat * yequat))); // trolig OK

//		System.out.println("Moon Ra=" + Moon_RA);

//		System.out.println("Ls=" + Ls);
		// war "+180" mit "-180" funkts aber besser :-)
		double GMST0 = (Ls - 180);
//		System.out.println("GMST0=" + GMST0);


		//*********CALCULATE TIME *********************

//		System.out.println("d1=" + d);
		double UT = d - Math.floor(d);
		//UT = 0.9;
//		System.out.println("d1=" + UT);
		//alert("UT="+UT);

		// ???????????????????
		// ???????????????????
		double SiteLon = this.location.getLatitude().doubleValue();
		double SiteLat = this.location.getLongitude().doubleValue();
		// ???????????????????
		// ???????????????????

		double SIDEREALTIME = GMST0 + UT * 360 + SiteLon; // ok 
		double HourAngle = SIDEREALTIME - Moon_RA; // trolig ok
		//		System.out.println("GMST0 + UT * 360 + SiteLon=" + GMST0 + " " + UT + " " + SiteLon);
		//		System.out.println("SIDEREALTIME - Moon_RA=" + SIDEREALTIME + " " + Moon_RA);
		//		System.out.println("SIDEREALTIME=" + SIDEREALTIME);
		//		System.out.println("HourAngle=" + HourAngle);


		// make things easier!!
		double pi = Math.PI;

		x = Math.cos(HourAngle * Math.PI / 180) * Math.cos(Moon_Decl * Math.PI / 180);
		y = Math.sin(HourAngle * Math.PI / 180) * Math.cos(Moon_Decl * Math.PI / 180);
		z = Math.sin(Moon_Decl * Math.PI / 180);

		double xhor = x * Math.sin(SiteLat * pi / 180) - z * Math.cos(SiteLat * pi / 180);
		//alert('sitelat='+SiteLat+'\nsitelon='+SiteLon);
		double yhor = y;
		double zhor = x * Math.cos(SiteLat * pi / 180) + z * Math.sin(SiteLat * pi / 180);


		double MoonElevation = Deg(Math.asin(zhor)); // ok regner ikke måne elevation helt riktig...
		//System.out.println("MoonElevation=" + MoonElevation);

		MoonElevation = MoonElevation - Deg(Math.asin(1 / r * Math.cos(Radians(MoonElevation))));
		//System.out.println("MoonElevation=" + MoonElevation);

		double GeometricElevation = MoonElevation;
		MoonElevation = ElevationRefraction(MoonElevation); // atmospheric refraction

		double MoonAzimuth = Deg(Math.atan2(yhor, xhor));

		//		System.out.println("MoonElevation=" + MoonElevation);
		//		System.out.println("MoonAzimuth=" + MoonAzimuth);
		//		System.out.println("GeometricElevation=" + GeometricElevation);
		//		System.out.println("Moon_Decl=" + Moon_Decl);
		//		System.out.println("moon_longitude=" + moon_longitude);
		//		System.out.println("moon_latitude=" + moon_latitude);
		//		System.out.println("P_moondistance" + P_moondistance);
		//		System.out.println("r=" + r);
	}

	/**
	 * Computes the sunrise time for the given zenith at the given date.
	 * 
	 * @param solarZenith
	 *           <code>Zenith</code> enum corresponding to the type of sunrise to compute.
	 * @param date
	 *           <code>Calendar</code> object representing the date to compute the sunrise for.
	 * @return the sunrise time, in HH:MM format (24-hour clock), 00:00 if the sun does not rise on the given
	 *         date.
	 */
	public String computeSunriseTime(Zenith solarZenith, Calendar date)
	{
		return computeSolarEventTime(solarZenith, date, true);
	}

	/**
	 * Computes the sunset time for the given zenith at the given date.
	 * 
	 * @param solarZenith
	 *           <code>Zenith</code> enum corresponding to the type of sunset to compute.
	 * @param date
	 *           <code>Calendar</code> object representing the date to compute the sunset for.
	 * @return the sunset time, in HH:MM format (24-hour clock), 00:00 if the sun does not set on the given
	 *         date.
	 */
	public String computeSunsetTime(Zenith solarZenith, Calendar date)
	{
		return computeSolarEventTime(solarZenith, date, false);
	}

	private String computeSolarEventTime(Zenith solarZenith, Calendar date, boolean isSunrise)
	{
		date.setTimeZone(this.timeZone);
		BigDecimal longitudeHour = getLongitudeHour(date, isSunrise);

		BigDecimal meanAnomaly = getMeanAnomaly(longitudeHour);
		BigDecimal sunTrueLong = getSunTrueLongitude(meanAnomaly);
		BigDecimal cosineSunLocalHour = getCosineSunLocalHour(sunTrueLong, solarZenith);
		if ((cosineSunLocalHour.doubleValue() < -1.0) || (cosineSunLocalHour.doubleValue() > 1.0)) { return "99:99"; }

		BigDecimal sunLocalHour = getSunLocalHour(cosineSunLocalHour, isSunrise);
		BigDecimal localMeanTime = getLocalMeanTime(sunTrueLong, longitudeHour, sunLocalHour);
		BigDecimal localTime = getLocalTime(localMeanTime, date);
		return getLocalTimeAsString(localTime);
	}

	/**
	 * Computes the base longitude hour, lngHour in the algorithm.
	 * 
	 * @return the longitude of the location of the solar event divided by 15 (deg/hour), in
	 *         <code>BigDecimal</code> form.
	 */
	private BigDecimal getBaseLongitudeHour()
	{
		return divideBy(location.getLongitude(), BigDecimal.valueOf(15));
	}

	/**
	 * Computes the longitude time, t in the algorithm.
	 * 
	 * @return longitudinal time in <code>BigDecimal</code> form.
	 */
	private BigDecimal getLongitudeHour(Calendar date, Boolean isSunrise)
	{
		int offset = 18;
		if (isSunrise)
		{
			offset = 6;
		}
		BigDecimal dividend = BigDecimal.valueOf(offset).subtract(getBaseLongitudeHour());
		BigDecimal addend = divideBy(dividend, BigDecimal.valueOf(24));
		BigDecimal longHour = getDayOfYear(date).add(addend);
		return setScale(longHour);
	}

	/**
	 * Computes the mean anomaly of the Sun, M in the algorithm.
	 * 
	 * @return the suns mean anomaly, M, in <code>BigDecimal</code> form.
	 */
	private BigDecimal getMeanAnomaly(BigDecimal longitudeHour)
	{
		BigDecimal meanAnomaly = multiplyBy(new BigDecimal("0.9856"), longitudeHour).subtract(
				new BigDecimal("3.289"));
		return setScale(meanAnomaly);
	}

	/**
	 * Computes the true longitude of the sun, L in the algorithm, at the given location, adjusted to fit in
	 * the range [0-360].
	 * 
	 * @param meanAnomaly
	 *           the suns mean anomaly.
	 * @return the suns true longitude, in <code>BigDecimal</code> form.
	 */
	private BigDecimal getSunTrueLongitude(BigDecimal meanAnomaly)
	{
		BigDecimal sinMeanAnomaly = new BigDecimal(Math.sin(convertDegreesToRadians(meanAnomaly)
				.doubleValue()));
		BigDecimal sinDoubleMeanAnomaly = new BigDecimal(Math.sin(multiplyBy(
				convertDegreesToRadians(meanAnomaly), BigDecimal.valueOf(2)).doubleValue()));

		BigDecimal firstPart = meanAnomaly.add(multiplyBy(sinMeanAnomaly, new BigDecimal("1.916")));
		BigDecimal secondPart = multiplyBy(sinDoubleMeanAnomaly, new BigDecimal("0.020")).add(
				new BigDecimal("282.634"));
		BigDecimal trueLongitude = firstPart.add(secondPart);

		if (trueLongitude.doubleValue() > 360)
		{
			trueLongitude = trueLongitude.subtract(BigDecimal.valueOf(360));
		}
		return setScale(trueLongitude);
	}

	/**
	 * Computes the suns right ascension, RA in the algorithm, adjusting for the quadrant of L and turning it
	 * into degree-hours. Will be in the range [0,360].
	 * 
	 * @param sunTrueLong
	 *           Suns true longitude, in <code>BigDecimal</code>
	 * @return suns right ascension in degree-hours, in <code>BigDecimal</code> form.
	 */
	private BigDecimal getRightAscension(BigDecimal sunTrueLong)
	{
		BigDecimal tanL = new BigDecimal(Math.tan(convertDegreesToRadians(sunTrueLong).doubleValue()));

		BigDecimal innerParens = multiplyBy(convertRadiansToDegrees(tanL), new BigDecimal("0.91764"));
		BigDecimal rightAscension = new BigDecimal(Math.atan(convertDegreesToRadians(innerParens)
				.doubleValue()));
		rightAscension = setScale(convertRadiansToDegrees(rightAscension));

		if (rightAscension.doubleValue() < 0)
		{
			rightAscension = rightAscension.add(BigDecimal.valueOf(360));
		}
		else if (rightAscension.doubleValue() > 360)
		{
			rightAscension = rightAscension.subtract(BigDecimal.valueOf(360));
		}

		BigDecimal ninety = BigDecimal.valueOf(90);
		BigDecimal longitudeQuadrant = sunTrueLong.divide(ninety, 0, RoundingMode.FLOOR);
		longitudeQuadrant = longitudeQuadrant.multiply(ninety);

		BigDecimal rightAscensionQuadrant = rightAscension.divide(ninety, 0, RoundingMode.FLOOR);
		rightAscensionQuadrant = rightAscensionQuadrant.multiply(ninety);

		BigDecimal augend = longitudeQuadrant.subtract(rightAscensionQuadrant);
		return divideBy(rightAscension.add(augend), BigDecimal.valueOf(15));
	}

	private BigDecimal getCosineSunLocalHour(BigDecimal sunTrueLong, Zenith zenith)
	{
		BigDecimal sinSunDeclination = getSinOfSunDeclination(sunTrueLong);
		BigDecimal cosineSunDeclination = getCosineOfSunDeclination(sinSunDeclination);

		BigDecimal zenithInRads = convertDegreesToRadians(zenith.degrees());
		BigDecimal cosineZenith = BigDecimal.valueOf(Math.cos(zenithInRads.doubleValue()));
		BigDecimal sinLatitude = BigDecimal.valueOf(Math.sin(convertDegreesToRadians(
				location.getLatitude()).doubleValue()));
		BigDecimal cosLatitude = BigDecimal.valueOf(Math.cos(convertDegreesToRadians(
				location.getLatitude()).doubleValue()));

		BigDecimal sinDeclinationTimesSinLat = sinSunDeclination.multiply(sinLatitude);
		BigDecimal dividend = cosineZenith.subtract(sinDeclinationTimesSinLat);
		BigDecimal divisor = cosineSunDeclination.multiply(cosLatitude);

		return setScale(divideBy(dividend, divisor));
	}

	private BigDecimal getSinOfSunDeclination(BigDecimal sunTrueLong)
	{
		BigDecimal sinTrueLongitude = BigDecimal.valueOf(Math
				.sin(convertDegreesToRadians(sunTrueLong).doubleValue()));
		BigDecimal sinOfDeclination = sinTrueLongitude.multiply(new BigDecimal("0.39782"));
		return setScale(sinOfDeclination);
	}

	private BigDecimal getCosineOfSunDeclination(BigDecimal sinSunDeclination)
	{
		BigDecimal arcSinOfSinDeclination = BigDecimal.valueOf(Math.asin(sinSunDeclination
				.doubleValue()));
		BigDecimal cosDeclination = BigDecimal
				.valueOf(Math.cos(arcSinOfSinDeclination.doubleValue()));
		return setScale(cosDeclination);
	}

	private BigDecimal getSunLocalHour(BigDecimal cosineSunLocalHour, Boolean isSunrise)
	{
		BigDecimal arcCosineOfCosineHourAngle = getArcCosineFor(cosineSunLocalHour);
		BigDecimal localHour = convertRadiansToDegrees(arcCosineOfCosineHourAngle);
		if (isSunrise)
		{
			localHour = BigDecimal.valueOf(360).subtract(localHour);
		}
		return divideBy(localHour, BigDecimal.valueOf(15));
	}

	private BigDecimal getLocalMeanTime(BigDecimal sunTrueLong, BigDecimal longitudeHour,
			BigDecimal sunLocalHour)
	{
		BigDecimal rightAscension = this.getRightAscension(sunTrueLong);
		BigDecimal innerParens = longitudeHour.multiply(new BigDecimal("0.06571"));
		BigDecimal localMeanTime = sunLocalHour.add(rightAscension).subtract(innerParens);
		localMeanTime = localMeanTime.subtract(new BigDecimal("6.622"));

		if (localMeanTime.doubleValue() < 0)
		{
			localMeanTime = localMeanTime.add(BigDecimal.valueOf(24));
		}
		else if (localMeanTime.doubleValue() > 24)
		{
			localMeanTime = localMeanTime.subtract(BigDecimal.valueOf(24));
		}
		return setScale(localMeanTime);
	}

	private BigDecimal getLocalTime(BigDecimal localMeanTime, Calendar date)
	{
		BigDecimal utcTime = localMeanTime.subtract(getBaseLongitudeHour());
		BigDecimal utcOffSet = getUTCOffSet(date);
		BigDecimal utcOffSetTime = utcTime.add(utcOffSet);
		return adjustForDST(utcOffSetTime, date);
	}

	private BigDecimal adjustForDST(BigDecimal localMeanTime, Calendar date)
	{
		BigDecimal localTime = localMeanTime;
		if (timeZone.inDaylightTime(date.getTime()))
		{
			localTime = localTime.add(BigDecimal.ONE);
		}
		if (localTime.doubleValue() > 24.0)
		{
			localTime = localTime.subtract(BigDecimal.valueOf(24));
		}
		return localTime;
	}

	/**
	 * Returns the local rise/set time in the form HH:MM.
	 * 
	 * @param localTime
	 *           <code>BigDecimal</code> representation of the local rise/set time.
	 * @return <code>String</code> representation of the local rise/set time in HH:MM format.
	 */
	private String getLocalTimeAsString(BigDecimal localTime)
	{
		String[] timeComponents = localTime.toPlainString().split("\\.");
		int hour = Integer.parseInt(timeComponents[0]);

		BigDecimal minutes = new BigDecimal("0." + timeComponents[1]);
		minutes = minutes.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_EVEN);
		if (minutes.intValue() == 60)
		{
			minutes = BigDecimal.ZERO;
			hour += 1;
		}

		String minuteString = minutes.intValue() < 10 ? "0" + minutes.toPlainString() : minutes
				.toPlainString();
		String hourString = (hour < 10) ? "0" + String.valueOf(hour) : String.valueOf(hour);
		return hourString + ":" + minuteString;
	}

	/** ******* UTILITY METHODS (Should probably go somewhere else. ***************** */

	private BigDecimal getDayOfYear(Calendar date)
	{
		return new BigDecimal(date.get(Calendar.DAY_OF_YEAR));
	}

	private BigDecimal getUTCOffSet(Calendar date)
	{
		int offSetInMillis = date.get(Calendar.ZONE_OFFSET);
		BigDecimal offSet = new BigDecimal(offSetInMillis / 3600000);
		return offSet.setScale(0, RoundingMode.HALF_EVEN);
	}

	private BigDecimal getArcCosineFor(BigDecimal radians)
	{
		BigDecimal arcCosine = BigDecimal.valueOf(Math.acos(radians.doubleValue()));
		return setScale(arcCosine);
	}

	private BigDecimal convertRadiansToDegrees(BigDecimal radians)
	{
		return multiplyBy(radians, new BigDecimal(180 / Math.PI));
	}

	private BigDecimal convertDegreesToRadians(BigDecimal degrees)
	{
		return multiplyBy(degrees, BigDecimal.valueOf(Math.PI / 180.0));
	}

	private BigDecimal multiplyBy(BigDecimal multiplicand, BigDecimal multiplier)
	{
		return setScale(multiplicand.multiply(multiplier));
	}

	private BigDecimal divideBy(BigDecimal dividend, BigDecimal divisor)
	{
		return dividend.divide(divisor, 4, RoundingMode.HALF_EVEN);
	}

	private BigDecimal setScale(BigDecimal number)
	{
		return number.setScale(4, RoundingMode.HALF_EVEN);
	}
}
