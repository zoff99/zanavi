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
		v = new String[] { "en", "map download", "cs", "Stáhnout mapu", "de", "Karten herunterladen", "fr", "télécharchez carte", "hu", "Térkép letöltése", "nl", "download kaarten", "pl", "pobierz mapy", "sk", "Sťahovanie mapy" };
		p(k, v);
		k = "Welcome to ZANavi";
		v = new String[] { "en", "Welcome to ZANavi", "cs", "Vítejte v ZANavi", "de", "Willkommen bei ZANavi", "fr", "Bienvenue chez ZANavi", "hu", "Üdvözöljük a ZANavi-ban!", "nl", "Welkom bij ZANavi", "pl", "Witaj w ZANavi", "sk", "Vitaje v ZANavi" };
		p(k, v);
		k = "More info";
		v = new String[] { "en", "More info", "cs", "Více informací", "de", "Mehr infos", "fr", "plus d'infos", "hu", "Több információ", "nl", "Meer info", "pl", "Więcej informacji", "sk", "Viac info" };
		p(k, v);
		k = "zoom in";
		v = new String[] { "en", "Zoom in", "cs", "přiblížit", "de", "Zoom in", "fr", "zoom avant", "hu", "nagyítás", "nl", "inzoomen", "pl", "przybliż", "sk", "Priblížiť" };
		p(k, v);
		k = "zoom out";
		v = new String[] { "en", "Zoom out", "cs", "oddálit", "de", "Zoom out", "fr", "zoom arrière", "hu", "kicsinyítés", "nl", "uitzoomen", "pl", "oddal", "sk", "Oddialiť" };
		p(k, v);
		k = "exit navit";
		v = new String[] { "en", "Exit ZANavi", "cs", "Ukončit ZANavi", "de", "ZANavi beenden", "fr", "Quittez ZANavi", "hu", "KIlépés a ZANavi-ból", "nl", "ZANavi afsluiten", "pl", "wyjdź z ZANavi", "sk", "Ukončiť ZANavi" };
		p(k, v);
		k = "toggle POI";
		v = new String[] { "en", "toggle POI", "cs", "toogle POI", "de", "POI ein/aus", "fr", "POI on/off", "hu", "POI ki/be", "nl", "POI aan/uit", "pl", "włącz/wyłącz POI", "sk", "Prepnúť POI" };
		p(k, v);
		k = "address search (offline)";
		v = new String[] { "en", "address search (offline)", "cs", "Hledání (offline)", "de", "Adressensuche (offline)", "hu", "Címkeresés (offline)", "nl", "adres zoeken (offline)", "pl", "wyszukaj adres (offline)", "sk", "Hľadať adresu (offline)" };
		p(k, v);
		k = "address search (online)";
		v = new String[] { "en", "address search (online)", "cs", "Hledání (online)", "de", "Adressensuche (online)", "hu", "Címkeresés (online)", "nl", "adres zoeken (online)", "pl", "wyszukaj adres (online)", "sk", "Hľadať adresu (online)" };
		p(k, v);
		k = "Mapdownload";
		v = new String[] { "en", "Mapdownload", "cs", "Stahování mapy", "de", "Kartendownload", "fr", "télécharchez carte", "hu", "Térképletöltés", "nl", "kaart download", "pl", "Pobieranie map", "sk", "Sťahovanie mapy" };
		p(k, v);
		k = "downloading";
		v = new String[] { "en", "downloading", "cs", "stahování", "de", "downloading", "hu", "letöltés", "nl", "downloaden", "pl", "Pobieranie", "sk", "Sťahuje sa" };
		p(k, v);
		k = "ETA";
		v = new String[] { "en", "ETA", "cs", "Zbýv. čas", "de", "fertig in", "hu", "Becsült hátralévő idő", "nl", "VAT", "pl", "Pozostały czas", "sk", "Zostáva" };
		p(k, v);
		k = "Error downloading map!";
		v = new String[] { "en", "Error downloading map!", "cs", "Chyba při stahování mapy!", "de", "Fehler beim Kartendownload!", "hu", "Hiba térképletöltés közben!", "nl", "Fout tijdens downloaden!", "pl", "Błąd przy pobieraniu map", "sk", "Chyba počas sťahovania mapy" };
		p(k, v);
		k = "Error downloading map, resuming";
		v = new String[] { "en", "Error downloading map\nresuming ...", "cs", "Chyba při stahování mapy, zkouším znovu", "de", "Fehler beim Kartendownload\nDownload wird fortgesetzt ...", "hu", "Hiba térképletöltés közben\nújrafelvétel", "nl", "Fout tijdens downloaden, opnieuw", "pl", "Błąd przy pobieraniu map, ponawiam", "sk", "Chyba počas sťahovania mapy, pokračuje sa" };
		p(k, v);
		k = "ready";
		v = new String[] { "en", "ready", "cs", "připraveno", "de", "fertig", "hu", "kész", "nl", "gereed", "pl", "gotowe", "sk", "Pripravený" };
		p(k, v);
		k = "Ok";
		v = new String[] { "en", "Ok", "cs", "Ok", "de", "Ok", "fr", "Ok", "hu", "Rendben", "nl", "Ok", "pl", "Ok", "sk", "Ok" };
		p(k, v);
		k = "No address found";
		v = new String[] { "en", "No address found", "cs", "Adresa nenalezena", "de", "Keine Adresse gefunden", "hu", "Cím nem található", "nl", "Adres niet gevonden", "pl", "Nie znaleziono adresu", "sk", "Nenájdená žiadna adresa" };
		p(k, v);
		k = "Enter: City and Street";
		v = new String[] { "en", "Enter: City and Street", "cs", "Vložte: město a ulici", "de", "Stadt und Straße:", "hu", "Város, utca:", "nl", "Invoer: plaatsnaam en straat", "pl", "Wprowadź: Miasto oraz Ulica", "sk", "Zadjate: Mesto a ulicu" };
		p(k, v);
		k = "No search string entered";
		v = new String[] { "en", "No search string entered", "cs", "Vyhledávací řetězec je prázdný", "de", "Keine Eingabe", "hu", "Keresőmező üres", "nl", "geen waarde opgegeven", "pl", "Nie wprowadzono żadnych znaków", "sk", "Nezadaný žiadny výraz" };
		p(k, v);
		k = "setting destination to";
		v = new String[] { "en", "setting destination to", "cs", "Nastavuji cíl do", "de", "neues Fahrziel:", "hu", "Cél beállítva:", "nl", "bestemming ingesteld op", "pl", "ustawianie celu trasy do", "sk", "Nastavuje sa cieľ na" };
		p(k, v);
		k = "getting search results";
		v = new String[] { "en", "getting search results", "cs", "Získávám výsledky hledání", "de", "lade Suchergebnisse", "hu", "Eredmények keresése", "nl", "haalt zoekresultaten op", "pl", "pobieranie wyników wyszukiwania", "sk", "Získava sa výsledok hľadania" };
		p(k, v);
		k = "searching ...";
		v = new String[] { "en", "searching ...", "cs", "hledám....", "de", "Suche läuft ...", "hu", "keresés ...", "nl", "zoekt ...", "pl", "wyszukuję ...", "sk", "Hľadá sa ..." };
		p(k, v);
		k = "No Results found!";
		v = new String[] { "en", "No Results found!", "cs", "Nic nenalzeno", "de", "Suche liefert kein Ergebnis!", "hu", "Nincs találat!", "nl", "Geen resultaat gevonden!", "pl", "Nic nie znaleziono!", "sk", "Žiadny výsledok nenájdený!" };
		p(k, v);
		k = "Map data (c) OpenStreetMap contributors, CC-BY-SA";
		v = new String[] { "en", "Map data (c) OpenStreetMap contributors, CC-BY-SA", "cs", "Mapové podklady (c) OpenStreetMap contributors, CC-BY-SA", "de", "Kartendaten: (c) OpenStreetMap contributors, CC-BY-SA", "fr", "Map data (c) OpenStreetMap contributors, CC-BY-SA", "hu", "Térképadatok: (c) OpenStreetMap contributors, CC-BY-SA", "nl", "Kaartgegevens (c) OpenStreetMap contributors, CC-BY-SA", "pl", "Dane map (c) OpenStreetMap contributors, CC-BY-SA", "sk",
				"Mapové údaje (c) OpenStreetMap contributors, CC-BY-SA" };
		p(k, v);
		k = "partial match";
		v = new String[] { "en", "partial match", "cs", "částečná shoda", "de", "ungefähr", "hu", "Részleges egyezés", "nl", "zoek op gedeelte", "pl", "częściowe dopasowanie", "sk", "Čiastočný výskyt" };
		p(k, v);
		k = "Search";
		v = new String[] { "en", "Search", "cs", "Hledání", "de", "suchen", "hu", "Keresés", "nl", "Zoek", "pl", "Wyszukaj", "sk", "Hľadať", "zh", "搜索" };
		p(k, v);
		k = "drive here";
		v = new String[] { "en", "drive here", "cs", "navigovat sem", "de", "Ziel setzen", "fr", "conduisez", "hu", "Ide menj!", "nl", "hierheen rijden", "pl", "Jedź tutaj", "sk", "Cestovať sem" };
		p(k, v);
		k = "loading search results";
		v = new String[] { "en", "loading search results", "cs", "nahrávám výsledky hledání", "de", "lade Suchergebnisse", "hu", "eredmények betöltése", "nl", "resultaten ophalen", "pl", "wczytywanie wyników wyszukiwania", "sk", "Načítavajú sa výsledky hľadania" };
		p(k, v);
		k = "towns";
		v = new String[] { "en", "towns", "cs", "města", "de", "Städte", "hu", "városok", "nl", "steden", "pl", "miasta", "sk", "Mestá" };
		p(k, v);
		k = "delete maps";
		v = new String[] { "en", "delete maps", "cs", "smazat mapy", "de", "Karten löschen", "hu", "térképek törlése", "nl", "kaarten verwijderen", "pl", "usuń mapy", "sk", "Zmazať mapy" };
		p(k, v);
		k = "download maps";
		v = new String[] { "en", "download maps", "cs", "stahování map", "de", "Karten laden", "fr", "télécharchez carte", "hu", "térképek letöltése", "nl", "download kaarten", "pl", "Pobierz mapy", "sk", "Stiahnuť mapy" };
		p(k, v);
		k = "Map already up to date";
		v = new String[] { "en", "Map already up to date", "cs", "Mapa je aktuální", "de", "Karte ist auf aktuellem Stand", "hu", "Térkép aktuális!", "nl", "kaart is al up to date", "pl", "Mapa jest już w najnowszej wersji", "sk", "Mapa je aktuálna" };
		p(k, v);
		k = "search full mapfile [BETA]";
		v = new String[] { "en", "search full mapfile [BETA]", "cs", "Prohledat celý mapový podklad [BETA]", "de", "ganze Karte durchsuchen [BETA]", "hu", "Teljes adatbázis keresése [BETA]", "nl", "zoek op volledige kaart [BETA]", "pl", "przeszukaj cały plik map [BETA]", "sk", "Prehľadávať celý mapový súbor [BETA]" };
		p(k, v);
		k = "__PREF__title__use_fast_provider";
		v = new String[] { "en", "GSM/3g/Wireless", "cs", "GSM/3g/WiFi", "de", "GSM/3g/Wireless", "hu", "GSM/3g/Wireless", "nl", "GSM/3G/WiFi", "sk", "GSM/3g/Bezdrôtové" };
		p(k, v);
		k = "__PREF__summ__use_fast_provider";
		v = new String[] { "en", "Use GSM/3g/Wireless networks for getting position (uses Internet)", "cs", "K získání pozice použij síť GSM/3g/WiFi (používá Internet)", "de", "Position auch mit GSM/3g/Wireless finden (braucht Internet)", "hu", "GSM/3g/Wireless (Internet!) használata a pozíciómeghatározáshoz", "nl", "Gebruik GSM/3G/WiFi om de positie te bepalen (Internet)", "sk", "Použiť GSM/3g/Bezdrôtové siete na získanie pozície (použije Internet)" };
		p(k, v);
		k = "__PREF__title__follow_gps";
		v = new String[] { "en", "Follow", "cs", "Následuj", "de", "folgen", "hu", "Követés", "nl", "Volg GPS", "sk", "Nasledovať" };
		p(k, v);
		k = "__PREF__summ__follow_gps";
		v = new String[] { "en", "Center on GPS. activate this for Compass heading to work", "cs", "Centruj mapu podle GPS. Aktivujte tuto volbu, pokud chcete používat směrování kompasem.", "de", "Karte folgt Fahrzeug", "hu", "A térkép követi a helyzetet (sticky)", "nl", "Volg GPS. Benodigd voor de kompas richting", "sk", "Centrovať na GPS. Aktivujte toto pre smerovanie podľa kompasu." };
		p(k, v);
		k = "__PREF__title__show_vehicle_in_center";
		v = new String[] { "en", "Vehicle in center", "cs", "Centruj dle pozice", "de", "Fahrzeug", "hu", "Pozíció a középpontban", "nl", "Centreer op voertuig", "sk", "Vozidlo v strede" };
		p(k, v);
		k = "__PREF__summ__show_vehicle_in_center";
		v = new String[] { "en", "show vehicle in screen center, instead of the lower half", "cs", "Zobrazuje pozici uprostřed obrazovky místo ve spodní části", "de", "Fahrzeug in Displaymitte, statt weiter unten", "hu", "A pozíció a térkép közepére rögzül", "nl", "Toont het voertuig in het midden van het scherm in plaats van onderin.", "sk", "Zobraziť vozidlo v strede obrazovky, namiesto spodnej polovice" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_base";
		v = new String[] { "en", "Compass", "cs", "Kompas", "de", "Kompass", "hu", "Iránytű", "nl", "Kompas richting", "sk", "Kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_base";
		v = new String[] { "en", "Get direction from compass. needs lots of CPU!", "cs", "Získej směr jízdy podle kompasu. Potřebuje hodně CPU (a baterie).", "de", "Kompass verwenden. braucht viel CPU !!", "hu", "Iránytű mutatása; sok CPU-t zabál!", "nl", "Gebruik kompas voor de richting. Kost veel CPU! De wereldweergave zal afwijken!", "sk", "Získať smer z kompasu. Veľmi zaťaží CPU!" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_always";
		v = new String[] { "en", "Compass always", "cs", "Vždy kompas", "de", "Kompass immer", "hu", "Iránytű menet közben", "nl", "Altijd kompas gebruiken", "sk", "Vždy kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_always";
		v = new String[] { "en", "Get current heading from compass even at higher speeds", "cs", "Získávej směr jizdy vždy podle kompasu a to i v případě vyšších rychlostí", "de", "immer Kompassrichtung verwenden", "hu", "Iránytű használata menet közben is", "nl", "Gebruikt het kompas voor de rijrichting, ook op hoge snelheid", "sk", "Získať aktuálne smerovanie z kompasu aj vo vysokej rýchlosti" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_fast";
		v = new String[] { "en", "fast Compass", "cs", "Rychlý kompas", "de", "Kompass schnell", "hu", "gyors iránytű", "nl", "Gebruik snel kompas", "sk", "Rýchly kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_fast";
		v = new String[] { "en", "turns much smoother, WARNING: WILL EAT ALL your CPU!!", "cs", "Kompas se točí velmi jemně. POZOR: vezme si veškerý výkon CPU", "de", "Kompass reagiert schnell. braucht noch viel mehr CPU!!", "hu", "Az iránytű gyorsítása, még több CPU-t zabál!", "nl", "Veel soepeler bij afslagen, WAARSCHUWING: gebruikt heel veel CPU!!!", "sk", "Oveľa plynulejšie otáčanie, VAROVANIE: ÚPLNE ZAŤAŽÍ CPU!!" };
		p(k, v);
		k = "__PREF__title__use_imperial";
		v = new String[] { "en", "Imperial", "cs", "Imperial", "de", "Meilen/Fuss", "hu", "Mértékegységek angolszászra", "nl", "Engelse maten", "sk", "Imperiálne jednotky" };
		p(k, v);
		k = "__PREF__summ__use_imperial";
		v = new String[] { "en", "Use Imperial units instead of metric units", "cs", "Používej imperiální jednotky (míle, ...) místo metrických.", "de", "Englische Maßeinheiten wie Meilen benutzen", "hu", "Mérföld, láb stb. km, méter helyett", "nl", "Gebruik Engelse eenheden in plaats van metrische", "sk", "Použiť imperiálne jednotky namiesto metrických" };
		p(k, v);
		k = "__PREF__title__show_3d_map";
		v = new String[] { "en", "3D", "cs", "3D", "de", "3D", "hu", "3D térkép", "nl", "Kaart in 3D", "sk", "3D" };
		p(k, v);
		k = "__PREF__summ__show_3d_map";
		v = new String[] { "en", "show map in 3D [BETA]", "cs", "Zobrazení mapy v 3D [BETA]", "de", "Karte in 3D [BETA]", "hu", "3D térkép bekapcsolása 2D helyett [BETA]", "nl", "3D weergave van de kaart [BETA]", "sk", "Zobraziť mapu v 3D [BETA]" };
		p(k, v);
		k = "__PREF__title__use_anti_aliasing";
		v = new String[] { "en", "AntiAlias", "cs", "Vyhlazování", "de", "Antialiasing", "hu", "Kontúrsimítás", "nl", "Anti Aliasing", "sk", "Vyhladzovanie" };
		p(k, v);
		k = "__PREF__summ__use_anti_aliasing";
		v = new String[] { "en", "draw with AntiAlias, map is faster when this is OFF", "cs", "Při vykreslování mapy použij vyhlazování čar. Je rychlejší, pokud je vypnuto.", "de", "Antialiasing einschalten. Karte zeichnet schneller ohne Antialiasing", "hu", "Kontúrsimítás bekapcsolása; enélkül a térkép gyorsabb.", "nl", "schakel anti aliasing in", "sk", "Vykresľovať mapu s vyhladzovaním. Mapa je rýchlejšia, ak je toto vypnuté." };
		p(k, v);
		k = "__PREF__title__gui_oneway_arrows";
		v = new String[] { "en", "OneWay Arrows", "cs", "Jednosměrky", "de", "Einbahn", "hu", "Egyirányú utcák", "nl", "eenrichtingspijlen", "sk", "Šípky jednosmeriek" };
		p(k, v);
		k = "__PREF__summ__gui_oneway_arrows";
		v = new String[] { "en", "show oneway street arrows [BETA]", "cs", "Ukazuj šipky u jednosměrných cest [BETA]", "de", "Einbahnpfeile [BETA]", "hu", "Egyirányú utcák jelzése [BETA]", "nl", "Toon pijlen bij eenrichtings-straten [BETA]", "sk", "Zobraziť šípky jednosmerných ulíc [BETA]" };
		p(k, v);
		k = "__PREF__title__show_debug_messages";
		v = new String[] { "en", "Debug Mgs", "cs", "Ladící hlášky", "de", "Debug", "hu", "Debug", "nl", "Toon debug berichten", "sk", "Ladiace správy" };
		p(k, v);
		k = "__PREF__summ__show_debug_messages";
		v = new String[] { "en", "show Debug Messages [DEBUG]", "cs", "Zobrazuj ladící hlášky [DEBUG]", "de", "Debugmeldungen [DEBUG]", "hu", "Hibakövetési jelentések (debug) megjelenítése", "nl", "Toont debug berichten [DEBUG]", "sk", "Zobraziť ladiace správy [DEBUG]" };
		p(k, v);
		k = "__PREF__title__navit_lang";
		v = new String[] { "en", "Language", "cs", "Jazyk", "de", "Sprache", "hu", "Nyelv", "nl", "Language", "sk", "Jazyk" };
		p(k, v);
		k = "__PREF__summ__navit_lang";
		v = new String[] { "en", "Select Language for messages. needs a RESTART!!", "cs", "Vyber komunikační jazky (pro aplikaci je nutný restart aplikace)", "de", "Sprache der Applikation (Sprache und Meldungen) braucht RESTART!!", "hu", "Írott és hangos nyelv kiválasztása; újraindítás szükséges", "nl", "Stel de taal in. Herstart is dan nodig!", "sk", "Vybrať jazyk hlášok. Vyžaduje REŠTART!!" };
		p(k, v);
		k = "__PREF__title__use_lock_on_roads";
		v = new String[] { "en", "lock on Roads", "cs", "Jen po silnicích", "de", "Fahrzeug auf Strasse", "hu", "Maradj az úton", "nl", "plaats op de weg", "sk", "Prichytiť k ceste" };
		p(k, v);
		k = "__PREF__summ__use_lock_on_roads";
		v = new String[] { "en", "lock Vehicle on nearest Road. turn off if you are walking or driving offroad", "cs", "Umístnit pozici vždy na nejbližší komunikaci. Vypněte, pokud se pohybujete mimo značené trasy.", "de", "Fahrzeug auf nächstgelegene Strasse fixieren. Zu Fuß oder Offroad bitte ausschalten", "hu", "A pointert az úton tartja akkor is, ha esetleg a koordináta eltér (autós beállítás, gyalog vagy terepen kikapcsolandó)", "nl",
				"Plaatst het voertuig op de dichtsbijzijnde weg. Zet deze uit voor wandelen of offroad rijden", "sk", "Prichytiť vozidlo k najbližšej ceste. Vypnite ak kráčate alebo jazdíte v teréne." };
		p(k, v);
		k = "__PREF__title__use_route_highways";
		v = new String[] { "en", "prefer Highways", "cs", "Preferuj dálnice", "de", "Autobahn bevorzugen", "hu", "Autópályák", "nl", "voorkeur voor snelwegen", "sk", "Uprednostňovať diaľnice" };
		p(k, v);
		k = "__PREF__summ__use_route_highways";
		v = new String[] { "en", "prefer Highways for routing (switching this off uses more Memory!!)", "cs", "Preferování dálnic. Při vypnuté volbě je potřeba více paměti.", "de", "Autobahnen werden bevorzugt (abschalten braucht viel mehr Speicher!!)", "hu", "Részesítsd előnyben az autópályák használatát", "nl", "geef voorkeur aan snelwegen voor de route (uitschakelen gebruikt meer geheugen!!)", "sk", "Uprednostniť diaľnice pri navigovaní (vypnutie spôsobí väčšiu spotrebu pamäti!!)" };
		p(k, v);
		k = "__PREF__title__save_zoomlevel";
		v = new String[] { "en", "Zoomlevel", "cs", "Úroveň přiblížení", "de", "Zoomstufe", "hu", "Kivágás (zoom) tárolása", "nl", "zoomniveau", "sk", "Úroveň priblíženia" };
		p(k, v);
		k = "__PREF__summ__save_zoomlevel";
		v = new String[] { "en", "save last Zoomlevel", "cs", "Ukládat posledně použitou úroveň přiblížení.", "de", "Zoomstufe speichern", "hu", "Tárold el az utolsó kivágás (zoom) méretet", "nl", "bewaar laatste zoomniveau", "sk", "Zapamätať poslednú úroveň priblíženia" };
		p(k, v);
		k = "__PREF__title__show_sat_status";
		v = new String[] { "en", "Sat Status", "cs", "Stav statelitů", "de", "Sat Status", "hu", "Műholdak", "nl", "satteliet status", "sk", "Stav satelitov" };
		p(k, v);
		k = "__PREF__summ__show_sat_status";
		v = new String[] { "en", "show Statellite Status", "cs", "Ukazuj stav GPS satelitů", "de", "Satelliten Status anzeigen", "hu", "Mutasd a műholdak állapotát", "nl", "weergeven satteliet status", "sk", "Zobraziť stav satelitov" };
		p(k, v);
		k = "__PREF__title__use_agps";
		v = new String[] { "en", "aGPS", "cs", "aGPS", "de", "aGPS", "hu", "aGPS használata", "nl", "aGPS", "sk", "aGPS" };
		p(k, v);
		k = "__PREF__summ__use_agps";
		v = new String[] { "en", "use assisted GPS (uses Internet !!)", "cs", "Používej asistované GPS (potřebuje Internet)", "de", "aGPS verwenden für schnellere Positionsfindung (braucht Internet !!)", "hu", "Assisted GPS használata (internet kapcsolat!)", "nl", "gebruik assisted GPS (gebruikt Internet !!)", "sk", "Použiť asistované GPS (použije Internet !!)" };
		p(k, v);
		k = "__INFO_BOX_TITLE__";
		v = new String[] { "en", "Welcome to ZANavi", "cs", "Vítejte v ZANavi", "de", "Willkommen bei ZANavi", "fr", "Bienvenue chez ZANavi", "hu", "Üdvözöljük a ZANavi-ban!", "nl", "Welkom bij ZANavi", "sk", "Vitajte v ZANavi" };
		p(k, v);
		k = "__INFO_BOX_TEXT__";
		v = new String[] { "en", "You are running ZANavi for the first time!\n\nTo start select \"download maps\"\nfrom the menu, and download a map\nfor your current Area.\nThis will download a large file, so please\nmake sure you have a flatrate or similar!\n\nMapdata:\nCC-BY-SA OpenStreetMap Project\n\nFor more information\nvisit our Website\nhttp://zanavi.cc\n\n       Have fun using ZANavi.", "cs",
				"ZANavi jste právě spustili poprvé!\n\nPrvně zvolte \"stáhnout mapy\"\nz menu a stáhněte mapu\noblastí, kde se právě nacházíte.\n\nMapové soubory jsou velké,\nproto se ujistěte, že máte datový paušál!\n\nMapové podklady:\nCC-BY-SA OpenStreetMap Project\n\nVíce informací získáte\nna našich stránkách\nhttp://zanavi.cc\n\nPřejme hodně zábavy se ZANavi.", "de",
				"Sie starten ZANavi zum ersten Mal!\n\nZum loslegen im Menu \"Karten laden\"\nauswählen und Karte für die\ngewünschte Region runterladen.\nDie Kartendatei ist sehr gross,\nbitte flatrate oder ähnliches aktivieren!\n\nKartendaten:\nCC-BY-SA OpenStreetMap Project\n\nFür mehr Infos\nbitte die Website besuchen\nhttp://zanavi.cc\n\n       Viel Spaß mit ZANavi.", "fr",
				"Vous exécutez ZANavi pour la première fois\n\nPour commencer, sélectionnez \n\\\"\"télécharchez carte\"\\\"\ndu menu et télechargez une carte\nde votre région.\nLes cartes sont volumineux, donc\nil est préférable d'avoir une connection\ninternet illimitée!\n\nCartes:\n CC-BY-SA OpenStreetMap Project\n\nPour plus d'infos\nvisitez notre site internet\nhttp://zanavi.cc\n\n       Amusez vous avec ZANavi.", "hu",
				"Először használja a ZANavi-t!\n\nKezdetnek válassza a \"Térképek letöltése\"\nmenüpontot, és töltsön le egy térképet\na kívánt területről.\nEz egy nagy adatállományt fog letölteni, tehat kérjük\nbizonyosodjon meg, hogy átalánydíjas letöltéses (flatrate) vagy hasonló előfizetése van (különben sokba kerülhet)!\n\nTérképadatok:\n CC-BY-SA OpenStreetMap Project\n\nTovábbi információkért\nlátogassa meg weblapunkat:\nhttp://zanavi.cc\n\nSok örömet kívánunk a ZANavi használatával!",
				"nl", "U voert ZANavi voor de eerste keer uit.\n\nOm te beginnen, selecteer\n\"download kaarten\"\nuit het menu en download een kaart\nvan je regio.\nDe kaarten zijn groot,\nhet is dus aangeraden om een\nongelimiteerde internetverbinding te hebben!\n\nKaartdata:\nCC-BY-SA OpenStreetMap Project\n\nVoor meer info\nbezoek onze site\nhttp://zanavi.cc\n\n       Nog veel plezier met ZANavi.", "sk",
				"Používate ZANavi prvýkrát!\n\nNa začatie svoľte \\\"Stiahnuť mapy\\\"\nz ponuky, a stiahnite si mapu\npre Vašu aktuálnu oblasť.\nToto stiahne veľký súbor, takže\nsa uistite, že máte neobmedzený alebo podobný internet!\n\nMapové údaje:\nCC-BY-SA OpenStreetMap Project\n\nPre viac informácií\nnavštívte našu webstránku\nhttp://zanavi.cc\n\n       Bavte sa pri používaní ZANavi." };
		p(k, v);
		k = "Announcer Off";
		v = new String[] { "en", "Announcer Off", "cs", "Vypnout ohlašování", "de", "Ansagen stumm", "hu", "Hangbemondás ki", "nl", "spraak uit", "sk", "Hlásateľ Vyp" };
		p(k, v);
		k = "Announcer On";
		v = new String[] { "en", "Announcer On", "cs", "Zapnout ohlašování", "de", "Ansagen einschalten", "hu", "Hangbemondás be", "nl", "spraak aan", "sk", "Hlásateľ Zap" };
		p(k, v);
		k = "Language is not available for TTS! Using your phone's default settings";
		v = new String[] { "en", "Language is not available for TTS!\nUsing your phone's default settings", "de", "Diese Sprache nicht für Sprachansage verfügbar!\nVerwende ", "hu", "A hangbemondás e nyelven nem létezik, a telefon alapértelmezését fogjuk ", "nl", "Taal niet beschikbaar voor TTS!\nStandaard instellingen worden gebruikt", "pl", "Język nie jest wspierany przez TTS! Przełączam na ustawienia domyślne ", "sk", "Jazyk nie je dostupný pre TTS! Použijú sa predvolené nastavenia Vášho " };
		p(k, v);
		k = "Using Voice for:";
		v = new String[] { "en", "Using Voice for:", "cs", "Používám hlas pro:", "de", "Sprache für Ansagen:", "hu", "A hangbemondás nyelve:", "nl", "Gebruik taal voor:", "pl", "Używam głosu dla:", "sk", "Použiť hlas pre:" };
		p(k, v);
		k = "Settings";
		v = new String[] { "en", "Settings", "cs", "Nastavení", "de", "Einstellungen", "hu", "Beállitások", "nl", "Instellingen", "pl", "Ustawienia", "sk", "Nastavenia", "zh", "设置" };
		p(k, v);
		k = "Creating outputfile, wait";
		v = new String[] { "en", "Creating outputfile\nplease wait", "cs", "Vytvářím výstupní soubor, čekejte prosím", "de", "Kartendatei anlegen\nBitte warten", "hu", "Kimeneti adatállomány előállítása,\nkérem, várjon!", "nl", "Bestand wordt gemaakt, even geduld", "pl", "Tworzę plik wyjściowy, czekaj", "sk", "Vytvára sa výstupný súbor, čakajte" };
		p(k, v);
		k = "Creating outputfile, long time";
		v = new String[] { "en", "Creating outputfile\nthis can take a long time", "cs", "Vytvářím výstupní soubor, bude to trvat déle", "de", "Kartendatei anlegen\nDas kann etwas dauern", "hu", "Kimeneti adatállomány előállítása,\nsokáig eltarthat, kérem, várjon!", "nl", "Bestand wordt gemaakt, kan even duren", "pl", "Tworzę plik wyjściowy, długi czas oczekiwania", "sk", "Vytvára sa výstupný súbor" };
		p(k, v);
		k = "MD5 mismatch";
		v = new String[] { "en", "MD5 mismatch", "cs", "Chyba MD5 kontroly", "de", "MD5 Prüfsumme stimmt nicht", "hu", "MD5 checksum hiba", "nl", "MD5 fout", "pl", "skrót MD5 niepoprawny", "sk", "Nesúhlasí MD5" };
		p(k, v);
		k = "generating MD5 checksum";
		v = new String[] { "en", "generating MD5 checksum", "cs", "Generuji MD5 kontrolní součet", "de", "überprüfe MD5 Prüfsumme", "hu", "MD5 checksum kiszámítása", "nl", "MD5sum genereren", "pl", "generowanie skrótu MD5", "sk", "Generuje sa kontrolný súčet MD5" };
		p(k, v);
		k = "Use as destination";
		v = new String[] { "en", "Use as destination", "cs", "Použít jako cíl", "de", "als Ziel setzen", "hu", "Állítsd be célként", "nl", "gebruik als bestemming", "pl", "Użyj jako celu", "sk", "Použiť ako cieľ" };
		p(k, v);
		k = "back";
		v = new String[] { "en", "back", "cs", "zpět", "de", "zurück", "hu", "vissza", "nl", "terug", "pl", "wstecz", "sk", "Späť" };
		p(k, v);
		k = "destination";
		v = new String[] { "en", "destination", "cs", "cíl", "de", "Ziel", "hu", "Cél", "nl", "bestemming", "pl", "Cel", "sk", "Cieľ" };
		p(k, v);
		k = "wait ...";
		v = new String[] { "en", "wait ...", "cs", "čekejte...", "de", "warten ...", "hu", "Várj...", "nl", "wachten ...", "pl", "czekaj ...", "sk", "Čakajte ..." };
		p(k, v);
		k = "Enter Destination";
		v = new String[] { "en", "Enter Destination", "cs", "Zadejte cíl", "de", "Zielort eingeben", "hu", "Úticél megadása", "nl", "voer bestemming in", "pl", "Wprowadź cel", "sk", "Zadajťe cieľ" };
		p(k, v);
		k = "Zoom to Route";
		v = new String[] { "en", "Zoom to Route", "cs", "Zobrazit celou trasu", "de", "ganze Route anzeigen", "hu", "Ráközelítés az útvonalra", "nl", "Zoom naar route", "pl", "Przybliż do trasy", "sk", "Priblížiť na trasu" };
		p(k, v);
		k = "Stop Navigation";
		v = new String[] { "en", "Stop Navigation", "cs", "Přestat navigovat", "de", "Navigation beenden", "hu", "Navigálás vége", "nl", "Stop navigatie", "pl", "Zatrzymaj nawigację", "sk", "Zastaviť navigáciu" };
		p(k, v);
		k = "Streets";
		v = new String[] { "en", "Streets", "cs", "Ulice", "de", "Straßen", "hu", "Utcák", "nl", "Straten", "pl", "Ulice", "sk", "Ulice" };
		p(k, v);
		k = "touch map to zoom";
		v = new String[] { "en", "touch map to zoom", "cs", "pro přiblížení se dotkněte mapy", "de", "auf Karte drücken zum zoomen", "hu", "Érintsd meg a térképet nagyításhoz (zoom)", "nl", "raak kaart aan om te zoomen", "pl", "dotknij mapy aby przybliżyć", "sk", "Ťuknite na mapu pre priblíženie" };
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
