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

import java.util.HashMap;

import android.util.Log;

public class NavitTextTranslations
{

	// NLS Table compiled by Nick "Number6" Geoghegan
	// Not an exhaustive list, but supports 99% of all languages in Windows
	//{"LANGNAME", "CTRYNAME", "Language Code"},
	public static String[][] Navit_know_locales = { { "AFK", "ZAF", "af_ZA" }, // Afrikaans (South Africa)
			{ "SQI", "ALB", "sq_AL" }, // Albanian (Albania)
			{ "AMH", "ETH", "am_ET" }, // Amharic (Ethiopia)
			{ "ARG", "DZA", "ar_DZ" }, // Arabic (Algeria)
			{ "ARH", "BHR", "ar_BH" }, // Arabic (Bahrain)
			{ "ARE", "EGY", "ar_EG" }, // Arabic (Egypt)
			{ "ARI", "IRQ", "ar_IQ" }, // Arabic (Iraq)
			{ "ARJ", "JOR", "ar_JO" }, // Arabic (Jordan)
			{ "ARK", "KWT", "ar_KW" }, // Arabic (Kuwait)
			{ "ARB", "LBN", "ar_LB" }, // Arabic (Lebanon)
			{ "ARL", "LBY", "ar_LY" }, // Arabic (Libya)
			{ "ARM", "MAR", "ar_MA" }, // Arabic (Morocco)
			{ "ARO", "OMN", "ar_OM" }, // Arabic (Oman)
			{ "ARQ", "QAT", "ar_QA" }, // Arabic (Qatar)
			{ "ARA", "SAU", "ar_SA" }, // Arabic (Saudi Arabia)
			{ "ARS", "SYR", "ar_SY" }, // Arabic (Syria)
			{ "ART", "TUN", "ar_TN" }, // Arabic (Tunisia)
			{ "ARU", "ARE", "ar_AE" }, // Arabic (U.A.E.)
			{ "ARY", "YEM", "ar_YE" }, // Arabic (Yemen)
			{ "HYE", "ARM", "hy_AM" }, // Armenian (Armenia)
			{ "ASM", "IND", "as_IN" }, // Assamese (India)
			{ "BAS", "RUS", "ba_RU" }, // Bashkir (Russia)
			{ "EUQ", "ESP", "eu_ES" }, // Basque (Basque)
			{ "BEL", "BLR", "be_BY" }, // Belarusian (Belarus)
			{ "BNG", "BDG", "bn_BD" }, // Bengali (Bangladesh)
			{ "BNG", "IND", "bn_IN" }, // Bengali (India)
			{ "BRE", "FRA", "br_FR" }, // Breton (France)
			{ "BGR", "BGR", "bg_BG" }, // Bulgarian (Bulgaria)
			{ "CAT", "ESP", "ca_ES" }, // Catalan (Catalan)
			{ "ZHH", "HKG", "zh_HK" }, // Chinese (Hong Kong S.A.R.)
			{ "ZHM", "MCO", "zh_MO" }, // Chinese (Macao S.A.R.)
			{ "CHS", "CHN", "zh_CN" }, // Chinese (People's Republic of China)
			{ "ZHI", "SGP", "zh_SG" }, // Chinese (Singapore)
			{ "CHT", "TWN", "zh_TW" }, // Chinese (Taiwan)
			{ "COS", "FRA", "co_FR" }, // Corsican (France)
			{ "HRV", "HRV", "hr_HR" }, // Croatian (Croatia)
			{ "HRB", "BIH", "hr_BA" }, // Croatian (Latin, Bosnia and Herzegovina)
			{ "CSY", "CZE", "cs_CZ" }, // Czech (Czech Republic)
			{ "DAN", "DNK", "da_DK" }, // Danish (Denmark)
			{ "NLB", "BEL", "nl_BE" }, // Dutch (Belgium)
			{ "NLD", "NLD", "nl_NL" }, // Dutch (Netherlands)
			{ "ENA", "AUS", "en_AU" }, // English (Australia)
			{ "ENL", "BLZ", "en_BZ" }, // English (Belize)
			{ "ENC", "CAN", "en_CA" }, // English (Canada)
			{ "ENB", "CAR", "en_CB" }, // English (Caribbean)
			{ "ENN", "IND", "en_IN" }, // English (India)
			{ "ENI", "IRL", "en_IE" }, // English (Ireland)
			{ "ENJ", "JAM", "en_JM" }, // English (Jamaica)
			{ "ENM", "MYS", "en_MY" }, // English (Malaysia)
			{ "ENZ", "NZL", "en_NZ" }, // English (New Zealand)
			{ "ENP", "PHL", "en_PH" }, // English (Republic of the Philippines)
			{ "ENE", "SGP", "en_SG" }, // English (Singapore)
			{ "ENS", "ZAF", "en_ZA" }, // English (South Africa)
			{ "ENT", "TTO", "en_TT" }, // English (Trinidad and Tobago)
			{ "ENG", "GBR", "en_GB" }, // English (United Kingdom)
			{ "ENU", "USA", "en_US" }, // English (United States)
			{ "ENW", "ZWE", "en_ZW" }, // English (Zimbabwe)
			{ "ETI", "EST", "et_EE" }, // Estonian (Estonia)
			{ "FOS", "FRO", "fo_FO" }, // Faroese (Faroe Islands)
			{ "FIN", "FIN", "fi_FI" }, // Finnish (Finland)
			{ "FRB", "BEL", "fr_BE" }, // French (Belgium)
			{ "FRC", "CAN", "fr_CA" }, // French (Canada)
			{ "FRA", "FRA", "fr_FR" }, // French (France)
			{ "FRL", "LUX", "fr_LU" }, // French (Luxembourg)
			{ "FRM", "MCO", "fr_MC" }, // French (Principality of Monaco)
			{ "FRS", "CHE", "fr_CH" }, // French (Switzerland)
			{ "FYN", "NLD", "fy_NL" }, // Frisian (Netherlands)
			{ "GLC", "ESP", "gl_ES" }, // Galician (Galician)
			{ "KAT", "GEO", "ka_GE" }, // Georgian (Georgia)
			{ "DEA", "AUT", "de_AT" }, // German (Austria)
			{ "DEU", "DEU", "de_DE" }, // German (Germany)
			{ "DEC", "LIE", "de_LI" }, // German (Liechtenstein)
			{ "DEL", "LUX", "de_LU" }, // German (Luxembourg)
			{ "DES", "CHE", "de_CH" }, // German (Switzerland)
			{ "ELL", "GRC", "el_GR" }, // Greek (Greece)
			{ "KAL", "GRL", "kl_GL" }, // Greenlandic (Greenland)
			{ "GUJ", "IND", "gu_IN" }, // Gujarati (India)
			{ "HEB", "ISR", "he_IL" }, // Hebrew (Israel)
			{ "HIN", "IND", "hi_IN" }, // Hindi (India)
			{ "HUN", "HUN", "hu_HU" }, // Hungarian (Hungary)
			{ "ISL", "ISL", "is_IS" }, // Icelandic (Iceland)
			{ "IBO", "NGA", "ig_NG" }, // Igbo (Nigeria)
			{ "IND", "IDN", "id_ID" }, // Indonesian (Indonesia)
			{ "IRE", "IRL", "ga_IE" }, // Irish (Ireland)
			{ "XHO", "ZAF", "xh_ZA" }, // isiXhosa (South Africa)
			{ "ZUL", "ZAF", "zu_ZA" }, // isiZulu (South Africa)
			{ "ITA", "ITA", "it_IT" }, // Italian (Italy)
			{ "ITS", "CHE", "it_CH" }, // Italian (Switzerland)
			{ "JPN", "JPN", "ja_JP" }, // Japanese (Japan)
			{ "KDI", "IND", "kn_IN" }, // Kannada (India)
			{ "KKZ", "KAZ", "kk_KZ" }, // Kazakh (Kazakhstan)
			{ "KHM", "KHM", "km_KH" }, // Khmer (Cambodia)
			{ "KIN", "RWA", "rw_RW" }, // Kinyarwanda (Rwanda)
			{ "SWK", "KEN", "sw_KE" }, // Kiswahili (Kenya)
			{ "KOR", "KOR", "ko_KR" }, // Korean (Korea)
			{ "KYR", "KGZ", "ky_KG" }, // Kyrgyz (Kyrgyzstan)
			{ "LAO", "LAO", "lo_LA" }, // Lao (Lao P.D.R.)
			{ "LVI", "LVA", "lv_LV" }, // Latvian (Latvia)
			{ "LTH", "LTU", "lt_LT" }, // Lithuanian (Lithuania)
			{ "LBX", "LUX", "lb_LU" }, // Luxembourgish (Luxembourg)
			{ "MKI", "MKD", "mk_MK" }, // Macedonian (Former Yugoslav Republic of Macedonia)
			{ "MSB", "BRN", "ms_BN" }, // Malay (Brunei Darussalam)
			{ "MSL", "MYS", "ms_MY" }, // Malay (Malaysia)
			{ "MYM", "IND", "ml_IN" }, // Malayalam (India)
			{ "MLT", "MLT", "mt_MT" }, // Maltese (Malta)
			{ "MRI", "NZL", "mi_NZ" }, // Maori (New Zealand)
			{ "MAR", "IND", "mr_IN" }, // Marathi (India)
			{ "MON", "MNG", "mn_MN" }, // Mongolian (Cyrillic, Mongolia)
			{ "NEP", "NEP", "ne_NP" }, // Nepali (Nepal)
			{ "NOR", "NOR", "nb_NO" }, // Norwegian, Bokmå(Norway)
			{ "NON", "NOR", "nn_NO" }, // Norwegian, Nynorsk (Norway)
			{ "OCI", "FRA", "oc_FR" }, // Occitan (France)
			{ "ORI", "IND", "or_IN" }, // Oriya (India)
			{ "PAS", "AFG", "ps_AF" }, // Pashto (Afghanistan)
			{ "FAR", "IRN", "fa_IR" }, // Persian
			{ "PLK", "POL", "pl_PL" }, // Polish (Poland)
			{ "PTB", "BRA", "pt_BR" }, // Portuguese (Brazil)
			{ "PTG", "PRT", "pt_PT" }, // Portuguese (Portugal)
			{ "PAN", "IND", "pa_IN" }, // Punjabi (India)
			{ "ROM", "ROM", "ro_RO" }, // Romanian (Romania)
			{ "RMC", "CHE", "rm_CH" }, // Romansh (Switzerland)
			{ "RUS", "RUS", "ru_RU" }, // Russian (Russia)
			{ "SMG", "FIN", "se_FI" }, // Sami, Northern (Finland)
			{ "SME", "NOR", "se_NO" }, // Sami, Northern (Norway)
			{ "SMF", "SWE", "se_SE" }, // Sami, Northern (Sweden)
			{ "SAN", "IND", "sa_IN" }, // Sanskrit (India)
			{ "TSN", "ZAF", "tn_ZA" }, // Setswana (South Africa)
			{ "SIN", "LKA", "si_LK" }, // Sinhala (Sri Lanka)
			{ "SKY", "SVK", "sk_SK" }, // Slovak (Slovakia)
			{ "SLV", "SVN", "sl_SI" }, // Slovenian (Slovenia)
			{ "ESS", "ARG", "es_AR" }, // Spanish (Argentina)
			{ "ESB", "BOL", "es_BO" }, // Spanish (Bolivia)
			{ "ESL", "CHL", "es_CL" }, // Spanish (Chile)
			{ "ESO", "COL", "es_CO" }, // Spanish (Colombia)
			{ "ESC", "CRI", "es_CR" }, // Spanish (Costa Rica)
			{ "ESD", "DOM", "es_DO" }, // Spanish (Dominican Republic)
			{ "ESF", "ECU", "es_EC" }, // Spanish (Ecuador)
			{ "ESE", "SLV", "es_SV" }, // Spanish (El Salvador)
			{ "ESG", "GTM", "es_GT" }, // Spanish (Guatemala)
			{ "ESH", "HND", "es_HN" }, // Spanish (Honduras)
			{ "ESM", "MEX", "es_MX" }, // Spanish (Mexico)
			{ "ESI", "NIC", "es_NI" }, // Spanish (Nicaragua)
			{ "ESA", "PAN", "es_PA" }, // Spanish (Panama)
			{ "ESZ", "PRY", "es_PY" }, // Spanish (Paraguay)
			{ "ESR", "PER", "es_PE" }, // Spanish (Peru)
			{ "ESU", "PRI", "es_PR" }, // Spanish (Puerto Rico)
			{ "ESN", "ESP", "es_ES" }, // Spanish (Spain)
			{ "EST", "USA", "es_US" }, // Spanish (United States)
			{ "ESY", "URY", "es_UY" }, // Spanish (Uruguay)
			{ "ESV", "VEN", "es_VE" }, // Spanish (Venezuela)
			{ "SVF", "FIN", "sv_FI" }, // Swedish (Finland)
			{ "SVE", "SWE", "sv_SE" }, // Swedish (Sweden)
			{ "TAM", "IND", "ta_IN" }, // Tamil (India)
			{ "TTT", "RUS", "tt_RU" }, // Tatar (Russia)
			{ "TEL", "IND", "te_IN" }, // Telugu (India)
			{ "THA", "THA", "th_TH" }, // Thai (Thailand)
			{ "BOB", "CHN", "bo_CN" }, // Tibetan (PRC)
			{ "TRK", "TUR", "tr_TR" }, // Turkish (Turkey)
			{ "TUK", "TKM", "tk_TM" }, // Turkmen (Turkmenistan)
			{ "UIG", "CHN", "ug_CN" }, // Uighur (PRC)
			{ "UKR", "UKR", "uk_UA" }, // Ukrainian (Ukraine)
			{ "URD", "PAK", "ur_PK" }, // Urdu (Islamic Republic of Pakistan)
			{ "VIT", "VNM", "vi_VN" }, // Vietnamese (Vietnam)
			{ "CYM", "GBR", "cy_GB" }, // Welsh (United Kingdom)
			{ "WOL", "SEN", "wo_SN" }, // Wolof (Senegal)
			{ "III", "CHN", "ii_CN" }, // Yi (PRC)
			{ "YOR", "NGA", "yo_NG" } // Yoruba (Nigeria)
	};

	static String main_language = "en";
	static String sub_language = "EN";
	static String fallback_language = "en";
	static String fallback_sub_language = "EN";
	private static HashMap<String, HashMap<String, String>> Navit_text_lookup = new HashMap<String, HashMap<String, String>>();

	public static void init()
	{
		Log.e("NavitTextTranslations", "initializing translated text ...");
		String k = null;
		String[] v = null;

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// change those in https://translations.launchpad.net/zanavi
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		k = "map download";
		v = new String[] { "en", "map download", "de", "karten runterladen", "fr", "télécharchez carte", "nl", "download kaarten", "hu", "Térkép letöltése", "cs", "Stáhnout mapu" };
		p(k, v);
		k = "Welcome to ZANavi";
		v = new String[] { "en", "Welcome to ZANavi", "de", "Willkommen bei ZANavi", "fr", "Bienvenue chez ZANavi", "nl", "Welkom bij ZANavi", "hu", "Üdvözöljük a ZANavi-ban!", "cs", "Vítejte v ZANavi" };
		p(k, v);
		k = "More info";
		v = new String[] { "en", "More info", "de", "Mehr infos", "fr", "plus d'infos", "nl", "Meer info", "hu", "Több információ", "cs", "Více informací" };
		p(k, v);
		k = "zoom in";
		v = new String[] { "en", "Zoom in", "de", "Zoom in", "fr", "zoom avant", "nl", "inzoomen", "hu", "nagyítás", "cs", "přiblížit" };
		p(k, v);
		k = "zoom out";
		v = new String[] { "en", "Zoom out", "de", "Zoom out", "fr", "zoom arrière", "nl", "uitzoomen", "hu", "kicsinyítés", "cs", "oddálit" };
		p(k, v);
		k = "exit navit";
		v = new String[] { "en", "Exit ZANavi", "de", "ZANavi Beenden", "fr", "Quittez ZANavi", "nl", "ZANavi afsluiten", "hu", "KIlépés a ZANavi-ból", "cs", "ukončit ZANavi" };
		p(k, v);
		k = "toggle POI";
		v = new String[] { "en", "toggle POI", "de", "POI ein/aus", "fr", "POI on/off", "nl", "POI aan/uit", "hu", "POI ki/be", "cs", "toogle POI" };
		p(k, v);
		k = "address search (offline)";
		v = new String[] { "en", "address search (offline)", "de", "Adressen suche (offline)", "nl", "adres zoeken (offline)", "hu", "Címkeresés (offline)", "cs", "Hledání (offline)" };
		p(k, v);
		k = "address search (online)";
		v = new String[] { "en", "address search (online)", "de", "Adressen suche (online)", "nl", "adres zoeken (online)", "hu", "Címkeresés (online)", "cs", "Hledání (online)" };
		p(k, v);
		k = "Mapdownload";
		v = new String[] { "en", "Mapdownload", "de", "Kartendownload", "fr", "télécharchez carte", "nl", "kaart download", "hu", "Térképletöltés", "cs", "Stahování mapy" };
		p(k, v);
		k = "downloading";
		v = new String[] { "en", "downloading", "de", "downloading", "nl", "downloaden", "hu", "letöltés", "cs", "stahování" };
		p(k, v);
		k = "ETA";
		v = new String[] { "en", "ETA", "de", "fertig in", "nl", "VAT", "hu", "Becsült hátralévő idő", "cs", "Zbýv. čas" };
		p(k, v);
		k = "Error downloading map!";
		v = new String[] { "en", "Error downloading map!", "de", "Fehler beim Kartendownload!", "nl", "Fout tijdens downloaden!", "hu", "Hiba térképletöltés közben!", "cs", "Chyba při stahování mapy!" };
		p(k, v);
		k = "Error downloading map, resuming";
		v = new String[] { "en", "Error downloading map\nresuming ...", "de", "Fehler beim Kartendownload\ndownload wird fortgesetzt ...", "nl", "Fout tijdens downloaden, opnieuw", "hu", "Hiba térképletöltés közben\nújrafelvétel", "cs", "Chyba při stahování mapy, zkouším znovu" };
		p(k, v);
		k = "ready";
		v = new String[] { "en", "ready", "de", "fertig", "nl", "gereed", "hu", "kész", "cs", "připraveno" };
		p(k, v);
		k = "Ok";
		v = new String[] { "en", "Ok", "de", "Ok", "fr", "Ok", "nl", "Ok", "hu", "Rendben", "cs", "Ok" };
		p(k, v);
		k = "No address found";
		v = new String[] { "en", "No address found", "de", "Keine Adresse gefunden", "nl", "Adres niet gevonden", "hu", "Cím nem található", "cs", "Adresa nenalezena" };
		p(k, v);
		k = "Enter: City and Street";
		v = new String[] { "en", "Enter: City and Street", "de", "Stadt und Straße:", "nl", "Invoer: plaatsnaam en straat", "hu", "Város, utca:", "cs", "Vložte: město a ulici" };
		p(k, v);
		k = "No search string entered";
		v = new String[] { "en", "No search string entered", "de", "Keine Eingabe", "nl", "geen waarde opgegeven", "hu", "Keresőmező üres", "cs", "Vyhledávací řetězec je prázdný" };
		p(k, v);
		k = "setting destination to";
		v = new String[] { "en", "setting destination to", "de", "neues Fahrziel", "nl", "bestemming ingesteld op", "hu", "Cél beállítva:", "cs", "Nastavuji cíl do" };
		p(k, v);
		k = "getting search results";
		v = new String[] { "en", "getting search results", "de", "lade Suchergebnisse", "nl", "haalt zoekresultaten op", "hu", "Eredmények keresése", "cs", "Získávám výsledky hledání" };
		p(k, v);
		k = "searching ...";
		v = new String[] { "en", "searching ...", "de", "Suche läuft ...", "nl", "zoekt ...", "hu", "keresés ...", "cs", "hledám...." };
		p(k, v);
		k = "No Results found!";
		v = new String[] { "en", "No Results found!", "de", "Suche liefert kein Ergebnis!", "nl", "Geen resultaat gevonden!", "hu", "Nincs találat!", "cs", "Nic nenalzeno" };
		p(k, v);
		k = "Map data (c) OpenStreetMap contributors, CC-BY-SA";
		v = new String[] { "en", "Map data (c) OpenStreetMap contributors, CC-BY-SA", "de", "Map data (c) OpenStreetMap contributors, CC-BY-SA", "fr", "Map data (c) OpenStreetMap contributors, CC-BY-SA", "nl", "Kaartgegevens (c) OpenStreetMap contributors, CC-BY-SA", "hu", "Térképadatok: (c) OpenStreetMap contributors, CC-BY-SA", "cs", "Mapové podklady (c) OpenStreetMap contributors, CC-BY-SA" };
		p(k, v);
		k = "partial match";
		v = new String[] { "en", "partial match", "de", "ungefähr", "nl", "zoek op gedeelte", "hu", "Részleges egyezés", "cs", "částečná shoda" };
		p(k, v);
		k = "Search";
		v = new String[] { "en", "Search", "de", "suchen", "nl", "Zoek", "hu", "Keresés", "zh", "搜索", "cs", "Hledání" };
		p(k, v);
		k = "drive here";
		v = new String[] { "en", "drive here", "de", "Ziel setzen", "fr", "conduisez", "nl", "hierheen rijden", "hu", "Ide menj!", "cs", "navigovat sem" };
		p(k, v);
		k = "loading search results";
		v = new String[] { "en", "loading search results", "de", "lade Suchergebnisse", "nl", "resultaten ophalen", "hu", "eredmények betöltése", "cs", "nahrávám výsledky hledání" };
		p(k, v);
		k = "towns";
		v = new String[] { "en", "towns", "de", "Städte", "nl", "steden", "hu", "városok", "cs", "města" };
		p(k, v);
		k = "delete maps";
		v = new String[] { "en", "delete maps", "de", "Karten löschen", "nl", "kaarten verwijderen", "hu", "térképek törlése", "cs", "smazat mapy" };
		p(k, v);
		k = "download maps";
		v = new String[] { "en", "download maps", "de", "Karten laden", "fr", "télécharchez carte", "nl", "download kaarten", "hu", "térképek letöltése", "cs", "stahování map" };
		p(k, v);
		k = "Map already up to date";
		v = new String[] { "en", "Map already up to date", "de", "Karte ist auf aktuellem Stand", "nl", "kaart is al up to date", "hu", "Térkép aktuális!", "cs", "Mapa je aktuální" };
		p(k, v);
		k = "search full mapfile [BETA]";
		v = new String[] { "en", "search full mapfile [BETA]", "de", "ganze Karte durchsuchen [BETA]", "nl", "zoek op volledige kaart [BETA]", "hu", "Teljes adatbázis keresése [BETA]", "cs", "Prohledat celý mapový podklad [BETA]" };
		p(k, v);
		k = "__PREF__title__use_fast_provider";
		v = new String[] { "en", "GSM/3g/Wireless", "de", "GSM/3g/Wireless", "nl", "GSM/3G/WiFi", "hu", "GSM/3g/Wireless", "cs", "GSM/3g/WiFi" };
		p(k, v);
		k = "__PREF__summ__use_fast_provider";
		v = new String[] { "en", "Use GSM/3g/Wireless networks for getting position (uses Internet)", "de", "Position auch mit GSM/3g/Wireless finden (braucht Internet)", "nl", "Gebruik GSM/3G/WiFi om de positie te bepalen (Internet)", "hu", "GSM/3g/Wireless (Internet!) használata a pozíciómeghatározáshoz", "cs", "K získání pozice použij síť GSM/3g/WiFi (používá Internet)" };
		p(k, v);
		k = "__PREF__title__follow_gps";
		v = new String[] { "en", "Follow", "de", "folgen", "nl", "Volg GPS", "hu", "Követés", "cs", "Následuj" };
		p(k, v);
		k = "__PREF__summ__follow_gps";
		v = new String[] { "en", "Center on GPS. activate this for Compass heading to work", "de", "Karte folgt Fahrzeug", "nl", "Volg GPS. Benodigd voor de kompas richting", "hu", "A térkép követi a helyzetet (sticky)", "cs", "Centruj mapu podle GPS. Aktivujte tuto volbu, pokud chcete používat směrování kompasem." };
		p(k, v);
		k = "__PREF__title__show_vehicle_in_center";
		v = new String[] { "en", "Vehicle in center", "de", "Fahrzeug", "nl", "Centreer op voertuig", "hu", "Pozíció a középpontban", "cs", "Centruj dle pozice" };
		p(k, v);
		k = "__PREF__summ__show_vehicle_in_center";
		v = new String[] { "en", "show vehicle in screen center, instead of the lower half", "de", "Fahrzeug in Displaymitte, statt weiter unten", "nl", "Toont het voertuig in het midden van het scherm in plaats van onderin.", "hu", "A pozíció a térkép közepére rögzül", "cs", "Zobrazuje pozici uprostřed obrazovky místo ve spodní části" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_base";
		v = new String[] { "en", "Compass", "de", "Kompass", "nl", "Kompas richting", "hu", "Iránytű", "cs", "Kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_base";
		v = new String[] { "en", "Get direction from compass. needs lots of CPU!", "de", "Kompass verwenden. braucht viel CPU !!", "nl", "Gebruik kompas voor de richting. Kost veel CPU! De wereldweergave zal afwijken!", "hu", "Iránytű mutatása; sok CPU-t zabál!", "cs", "Získej směr jízdy podle kompasu. Potřebuje hodně CPU (a baterie)." };
		p(k, v);
		k = "__PREF__title__use_compass_heading_always";
		v = new String[] { "en", "Compass always", "de", "Kompass immer", "nl", "Altijd kompas gebruiken", "hu", "Iránytű menet közben", "cs", "Vždy kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_always";
		v = new String[] { "en", "Get current heading from compass even at higher speeds", "de", "immer Kompassrichtung verwenden", "nl", "Gebruikt het kompas voor de rijrichting, ook op hoge snelheid", "hu", "Iránytű használata menet közben is", "cs", "Získávej směr jizdy vždy podle kompasu a to i v případě vyšších rychlostí" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_fast";
		v = new String[] { "en", "fast Compass", "de", "Kompass schnell", "nl", "Gebruik snel kompas", "hu", "gyors iránytű", "cs", "rychlý Kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_fast";
		v = new String[] { "en", "turns much smoother, WARNING: WILL EAT ALL your CPU!!", "de", "Kompass reagiert schnell. braucht noch viel mehr CPU!!", "nl", "Veel soepeler bij afslagen, WAARSCHUWING: gebruikt heel veel CPU!!!", "hu", "Az iránytű gyorsítása, még több CPU-t zabál!", "cs", "Kompas se točí velmi jemně. POZOR: vezme si veškerý výkon CPU" };
		p(k, v);
		k = "__PREF__title__use_imperial";
		v = new String[] { "en", "Imperial", "de", "Meilen/Fuss", "nl", "Engelse maten", "hu", "Mértékegységek angolszászra", "cs", "Imperial" };
		p(k, v);
		k = "__PREF__summ__use_imperial";
		v = new String[] { "en", "Use Imperial units instead of metric units", "de", "englische Masseinheiten", "nl", "Gebruik Engelse eenheden in plaats van metrische", "hu", "Mérföld, láb stb. km, méter helyett", "cs", "Používej imperiální jednotky (míle, ...) místo metrických." };
		p(k, v);
		k = "__PREF__title__show_3d_map";
		v = new String[] { "en", "3D", "de", "3D", "nl", "Kaart in 3D", "hu", "3D térkép", "cs", "3D" };
		p(k, v);
		k = "__PREF__summ__show_3d_map";
		v = new String[] { "en", "show map in 3D [BETA]", "de", "Karte in 3D [BETA]", "nl", "3D weergave van de kaart [BETA]", "hu", "3D térkép bekapcsolása 2D helyett [BETA]", "cs", "Zobrazení mapy v 3D [BETA]" };
		p(k, v);
		k = "__PREF__title__use_anti_aliasing";
		v = new String[] { "en", "AntiAlias", "de", "Antialiasing", "nl", "Anti Aliasing", "hu", "Kontúrsimítás", "cs", "Vyhlazování" };
		p(k, v);
		k = "__PREF__summ__use_anti_aliasing";
		v = new String[] { "en", "draw with AntiAlias, map is faster when this is OFF", "de", "Antialiasing einschalten. Karte zeichnet schneller ohne Antialiasing", "nl", "schakel anti aliasing in", "hu", "Kontúrsimítás bekapcsolása; enélkül a térkép gyorsabb.", "cs", "Při vykreslování mapy použij vyhlazování čar. Je rychlejší, pokud je vypnuto." };
		p(k, v);
		k = "__PREF__title__gui_oneway_arrows";
		v = new String[] { "en", "OneWay Arrows", "de", "Einbahn", "nl", "eenrichtingspijlen", "hu", "Egyirányú utcák", "cs", "Jednosměrky" };
		p(k, v);
		k = "__PREF__summ__gui_oneway_arrows";
		v = new String[] { "en", "show oneway street arrows [BETA]", "de", "Einbahnpfeile [BETA]", "nl", "Toon pijlen bij eenrichtings-straten [BETA]", "hu", "Egyirányú utcák jelzése [BETA]", "cs", "Ukazuj šipky u jednosměrných cest [BETA]" };
		p(k, v);
		k = "__PREF__title__show_debug_messages";
		v = new String[] { "en", "Debug Mgs", "de", "Debug", "nl", "Toon debug berichten", "hu", "Debug", "cs", "Ladící hlášky" };
		p(k, v);
		k = "__PREF__summ__show_debug_messages";
		v = new String[] { "en", "show Debug Messages [DEBUG]", "de", "Debugmeldungen [DEBUG]", "nl", "Toont debug berichten [DEBUG]", "hu", "Hibakövetési jelentések (debug) megjelenítése", "cs", "Zobrazuj ladící hlášky [DEBUG]" };
		p(k, v);
		k = "__PREF__title__navit_lang";
		v = new String[] { "en", "Language", "de", "Sprache", "nl", "Language", "hu", "Nyelv", "cs", "Jazyk" };
		p(k, v);
		k = "__PREF__summ__navit_lang";
		v = new String[] { "en", "Select Language for messages. needs a RESTART!!", "de", "Sprache der Applikation (Sprache und Meldungen) braucht RESTART!!", "nl", "Stel de taal in. Herstart is dan nodig!", "hu", "Írott és hangos nyelv kiválasztása; újraindítás szükséges", "cs", "Vyber komunikační jazky (pro aplikaci je nutný restart aplikace)" };
		p(k, v);
		k = "__PREF__title__use_lock_on_roads";
		v = new String[] { "en", "lock on Roads", "de", "Fahrzeug auf Strasse", "cs", "Jen po silnicích" };
		p(k, v);
		k = "__PREF__summ__use_lock_on_roads";
		v = new String[] { "en", "lock Vehicle on nearest Road. turn off if you are walking or driving offroad", "de", "Fahrzeug auf nächstgelegene Strasse fixieren. Zufuss oder Offroad bitte ausschalten", "cs", "Umístnit pozici vždy na nejbližší komunikaci. Vypněte, pokud se pohybujete mimo značené trasy." };
		p(k, v);
		k = "__PREF__title__use_route_highways";
		v = new String[] { "en", "prefer Highways", "de", "Autobahn bevorzugt", "cs", "Preferuj dálnice" };
		p(k, v);
		k = "__PREF__summ__use_route_highways";
		v = new String[] { "en", "prefer Highways for routing (switching this off uses more Memory!!)", "de", "Autobahnen werden bevorzugt (abschalten braucht viel mehr Speicher!!)", "cs", "Preferování dálnic. Při vypnuté volbě je potřeba více paměti." };
		p(k, v);
		k = "__PREF__title__save_zoomlevel";
		v = new String[] { "en", "Zoomlevel", "de", "Zoomstufe", "cs", "Úroveň přiblížení" };
		p(k, v);
		k = "__PREF__summ__save_zoomlevel";
		v = new String[] { "en", "save last Zoomlevel", "de", "Zoomstufe speichern", "cs", "Ukládat posledně použitou úroveň přiblížení." };
		p(k, v);
		k = "__PREF__title__show_sat_status";
		v = new String[] { "en", "Sat Status", "de", "Sat Status", "cs", "Stav statelitů" };
		p(k, v);
		k = "__PREF__summ__show_sat_status";
		v = new String[] { "en", "show Statellite Status", "de", "Satelliten Status anzeigen", "cs", "Ukazuj stav GPS satelitů" };
		p(k, v);
		k = "__PREF__title__use_agps";
		v = new String[] { "en", "aGPS", "de", "aGPS", "cs", "aGPS" };
		p(k, v);
		k = "__PREF__summ__use_agps";
		v = new String[] { "en", "use assisted GPS (uses Internet !!)", "de", "aGPS verwenden für schnellere Positionsfindung (braucht Internet !!)", "cs", "používej asistované GPS (potřebuje Internet)" };
		p(k, v);
		k = "__INFO_BOX_TITLE__";
		v = new String[] { "en", "Welcome to ZANavi", "de", "Willkommen bei ZANavi", "fr", "Bienvenue chez ZANavi", "nl", "Welkom bij ZANavi", "hu", "Üdvözöljük a ZANavi-ban!", "cs", "Vítejte v ZANavi" };
		p(k, v);
		k = "__INFO_BOX_TEXT__";
		v = new String[] {
				"en",
				"You are running ZANavi for the first time!\n\n To start select \"download maps\"\n from the menu, and download a map\n for your current Area.\n This will download a large file, so please\n make sure you have a flatrate or similar!\n\n Mapdata:\n CC-BY-SA OpenStreetMap Project\n\n For more information\n visit our Website\n http://zanavi.cc\n\n       Have fun using ZANavi.",
				"de",
				"Sie starten ZANavi zum ersten Mal!\n\n Zum loslegen im Menu \"Karten laden\"\n auswählen und Karte für die\n gewünschte Region runterladen.\n Die Kartendatei ist sehr gross,\n bitte flatrate oder ähnliches aktivieren!\n\n Kartendaten:\n CC-BY-SA OpenStreetMap Project\n\n Für mehr Infos\n bitte die Website besuchen\n http://zanavi.cc\n\n       Viel Spaß mit ZANavi.",
				"fr",
				"Vous exécutez ZANavi pour la première fois\n\n Pour commencer, sélectionnez \n \\\"\"télécharchez carte\"\\\"\n du menu et télechargez une carte\n de votre région.\n Les cartes sont volumineux, donc\n il est préférable d'avoir une connection\n internet illimitée!\n\n Cartes:\n CC-BY-SA OpenStreetMap Project\n\n Pour plus d'infos\n visitez notre site internet\n http://zanavi.cc\n\n       Amusez vous avec ZANavi.",
				"nl",
				"U voert ZANavi voor de eerste keer uit.\n\n Om te beginnen, selecteer  \n \"download kaarten\"\n uit het menu en download een kaart\n van je regio.\n De kaarten zijn groot,\n het is dus aangeraden om een \n ongelimiteerde internetverbinding te hebben!\n\n Kaartdata:\n CC-BY-SA OpenStreetMap Project\n\n Voor meer info\n bezoek onze site\n http://zanavi.cc\n\n       Nog veel plezier met ZANavi.",
				"hu",
				"Először használja a ZANavi-t!\n\n Kezdetnek válassza a \"Térképek letöltése\"\n menüpontot, és töltsön le egy térképet\n a kívánt területről.\n Ez egy nagy adatállományt fog letölteni, tehat kérjük\n bizonyosodjon meg, hogy átalánydíjas letöltéses (flatrate) vagy hasonló előfizetése van (különben sokba kerülhet)!\n\n Térképadatok:\n CC-BY-SA OpenStreetMap Project\n\n További információkért\n látogassa meg weblapunkat:\n http://zanavi.cc\n\n Sok örömet kívánunk a ZANavi használatával!",
				"cs", "ZANavi jste právě spustili poprvé!\n\nPrvně zvolte \"stáhnout mapy\"\n z menu a stáhněte mapu\n oblastí, kde se právě nacházíte.\n\n Mapové soubory jsou velké,\n proto se ujistěte, že máte datový paušál!\n\n Mapové podklady:\n CC-BY-SA OpenStreetMap Project\n\n Více informací získáte\n na našich stránkách\n http://zanavi.cc\n\n Přejme hodně zábavy se ZANavi." };
		p(k, v);
		k = "Announcer Off";
		v = new String[] { "en", "Announcer Off", "de", "Ansagen stumm", "nl", "spraak uit", "hu", "Hangbemondás ki", "cs", "Vypnout ohlašování" };
		p(k, v);
		k = "Announcer On";
		v = new String[] { "en", "Announcer On", "de", "Ansagen einschalten", "nl", "spraak aan", "hu", "Hangbemondás be", "cs", "Zapnout ohlašování" };
		p(k, v);
		k = "Language is not available for TTS! Using your phone's default settings";
		v = new String[] { "en", "Language is not available for TTS!\nUsing your phone's default settings", "de", "diese Sprache nicht als TTS verfügbar!\nVerwende Standardeinstellung", "nl", "Taal niet beschikbaar voor TTS!\nStandaard instellingen worden gebruikt", "hu", "A hangbemondás e nyelven nem létezik, a telefon alapértelmezését fogjuk " };
		p(k, v);
		k = "Using Voice for:";
		v = new String[] { "en", "Using Voice for:", "de", "Sprache für Ansagen:", "nl", "Gebruik taal voor:", "hu", "A hangbemondás nyelve:", "cs", "Používám hlas pro:" };
		p(k, v);
		k = "Settings";
		v = new String[] { "en", "Settings", "de", "Einstellungen", "nl", "Instellingen", "hu", "Beállitások", "zh", "设置", "cs", "Nastavení" };
		p(k, v);
		k = "Creating outputfile, wait";
		v = new String[] { "en", "Creating outputfile\nplease wait", "de", "Kartendatei anlegen\nBitte warten", "nl", "Bestand wordt gemaakt, even geduld", "hu", "Kimeneti adatállomány előállítása,\nkérem, várjon!", "cs", "Vytvářím výstupní soubor, čekejte prosím" };
		p(k, v);
		k = "Creating outputfile, long time";
		v = new String[] { "en", "Creating outputfile\nthis can take a long time", "de", "Kartendatei anlegen\nDas kann etwas dauern", "nl", "Bestand wordt gemaakt, kan even duren", "hu", "Kimeneti adatállomány előállítása,\nsokáig eltarthat, kérem, várjon!", "cs", "Vytvářím výstupní soubor, bude to trvat déle" };
		p(k, v);
		k = "MD5 mismatch";
		v = new String[] { "en", "MD5 mismatch", "de", "MD5 Prüfsumme stimmt nicht", "nl", "MD5 fout", "hu", "MD5 checksum hiba", "cs", "Chyba MD5 kontroly" };
		p(k, v);
		k = "generating MD5 checksum";
		v = new String[] { "en", "generating MD5 checksum", "de", "überprüfe MD5 Prüfsumme", "nl", "MD5sum genereren", "hu", "MD5 checksum kiszámítása", "cs", "Generuji MD5 kontrolní součet" };
		p(k, v);
		k = "Use as destination";
		v = new String[] { "en", "Use as destination", "de", "als Ziel setzen", "hu", "Állítsd be célként", "cs", "Použít jako cíl" };
		p(k, v);
		k = "back";
		v = new String[] { "en", "back", "de", "zurück", "hu", "vissza", "cs", "zpět" };
		p(k, v);
		k = "destination";
		v = new String[] { "en", "destination", "de", "Ziel", "hu", "Cél", "cs", "cíl" };
		p(k, v);
		k = "wait ...";
		v = new String[] { "en", "wait ...", "de", "warten ...", "hu", "Várj...", "cs", "čekejte..." };
		p(k, v);
		k = "Enter Destination";
		v = new String[] { "en", "Enter Destination", "de", "Zielort eingeben", "hu", "Úticél megadása", "cs", "Zadejte cíl" };
		p(k, v);
		k = "Zoom to Route";
		v = new String[] { "en", "Zoom to Route", "de", "ganze Route anzeigen", "cs", "Zobrazit celou trasu" };
		p(k, v);
		k = "Stop Navigation";
		v = new String[] { "en", "Stop Navigation", "de", "Navigation beenden", "cs", "Přestat navigovat" };
		p(k, v);
		k = "Streets";
		v = new String[] { "en", "Streets", "de", "Straßen", "cs", "Ulice" };
		p(k, v);
		k = "touch map to zoom";
		v = new String[] { "en", "touch map to zoom", "de", "auf Karte drücken zum zoomen", "cs", "pro přiblížení se dotkněte mapy" };
		p(k, v);
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// change those in https://translations.launchpad.net/zanavi
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		Log.e("NavitTextTranslations", "... ready");
	}

	private static void p(String key, String[] values)
	{
		HashMap<String, String> t = null;
		t = new HashMap<String, String>();
		Log.e("NavitTextTranslations", "trying: " + key);
		try
		{
			for (int i = 0; i < (int) (values.length / 2); i++)
			{
				t.put(values[i * 2], values[(i * 2) + 1]);
			}
			Navit_text_lookup.put(key, t);
		}
		catch (Exception e)
		{
			Log.e("NavitTextTranslations", "!!Error in translationkey: " + key);
		}
	}

	public static String get_text(String in)
	{
		String out = null;

		//Log.e("NavitTextTranslations", "lookup L:" + main_language + " T:" + in);
		try
		{
			out = Navit_text_lookup.get(in).get(main_language);
		}
		catch (Exception e)
		{
			// most likely there is not translation yet
			//Log.e("NavitTextTranslations", "lookup: exception");
			out = null;
		}

		if (out == null)
		{
			// always return a string for output (use fallback language)
			//Log.e("NavitTextTranslations", "using default language");
			try
			{
				out = Navit_text_lookup.get(in).get(fallback_language);
			}
			catch (Exception e)
			{
				//Log.e("NavitTextTranslations", "using default language: exception");
				// *** DEBUG *** Log.e("NavitTextTranslations", "missing translation for:" + in);
				// most likely there is not translation yet
				out = null;
			}
		}

		if (out == null)
		{
			// if we still dont have any text, use the ".mo" file and call the c-function gettext(in)
			out = NavitGraphics.getLocalizedString(in);
			//Log.e("NavitTextTranslations", "return the value from gettext() = " + out);
		}
		return out;
	}

}
