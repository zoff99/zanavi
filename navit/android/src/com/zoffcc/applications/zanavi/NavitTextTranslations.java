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
	public final static String[][] Navit_know_locales = { { "AFK", "ZAF", "af_ZA" }, // Afrikaans (South Africa)
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
	static final String fallback_language = "en";
	static final String fallback_sub_language = "EN";
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
		k = "Donate";
		v = new String[] { "en","Donate", "ca","Donatiu", "fr","Faites un don", "de","Spenden", "el","Δωρεά", "sk","Prispieť"};
		p(k, v);
		k = "buy UDonate Version";
		v = new String[] { "en","buy UDonate Version", "de","UDonate Version kaufen"};
		p(k, v);
		k = "buy Donate Version";
		v = new String[] { "en","buy Donate Version", "ca","Adquiriu la versió Donació", "pt_BR","comprar Versão de Doação", "fr","Achetez la version payante", "de","Donate Version kaufen", "el","αγορά έκδοσης Με δωρεά", "es","Quiero donar para la versión"};
		p(k, v);
		k = "buy large-map Donate Version";
		v = new String[] { "en","buy large-map Donate Version", "pt_BR","comprar Versão de Doação mapa-grande", "fr","Achetez la version haute résolution payante", "de","large-map Donate Version kaufen", "el","αγορά έκδοσης ολόκληρου-χάρτη με Δωρεά", "es","Quiero donar para adquirir una versión grande del mapa"};
		p(k, v);
		k = "Donate with the offcial Donate App";
		v = new String[] { "en","Donate with the offcial Donate App", "pt_BR","Doar com o Aplicativo oficial de Doação", "fr","Faire un don en utilisant la version donateur officielle", "de","offizielle Spenden App starten", "el","Δωρεά μέσω της επίσημης εφρμογής Δωρεάς", "es","Donar para la App oficial"};
		p(k, v);
		k = "_long_text_large_map_donate_version_";
		v = new String[] { "en","This Donate Version activates index search and allows you to download the larger maps (World, Europe, USA).\nKeep the Donate Version installed and then delete and re-download all your maps to activate the index search", "fr","La version donateur permet la recherche indexée et l'utilisation de carte plus grandes (monde entier, europe, USA),  \nAfin d'activer la recherche indexée, gardez la version donateur installée puis effacer et re-télécharger toutes les cartes.", "de","Diese Donate Version aktiviert die Indexsuche und erlaubt das laden der großen Karten (Welt, Europa, USA).\nNach Installation der Donate Version bitte alle Karten löschen und nochmals runterladen, damit wird die Indexsuche aktiviert"};
		p(k, v);
		k = "_long_text_donate_version_";
		v = new String[] { "en","With this Donate Version you activate the faster index search.\nKeep the Donate Version installed and then delete and re-download all your maps to activate the index search", "fr","La version donateur permet la recherche indexée beaucoup plus rapide. \nAfin d'activer la recherche indexée, gardez la version donateur installée puis effacer et re-télécharger toutes les cartes.", "de","Mit dieser Donate Version wird die Indexsuche aktiviert.\nNach Installation der Donate Version bitte alle Karten löschen und nochmals runterladen, damit wird die Indexsuche aktiviert"};
		p(k, v);
		k = "_long_text_donate_app_";
		v = new String[] { "en","Donate any Amount with the official Donate App", "fr","Faites une donation avec l'appli donateur", "de","Einen beliebigen Betrag mit der offiziellen Donate App spenden"};
		p(k, v);
		k = "ZANavi UDonate Version already installed";
		v = new String[] { "en","ZANavi UDonate Version already installed", "de","UDonate Version ist bereits installiert"};
		p(k, v);
		k = "ZANavi Donate Version already installed";
		v = new String[] { "en","ZANavi Donate Version already installed", "pt_BR","ZANavi Versão de Doação já instalada", "fr","Zanavi version donateur est déja installée", "de","Donate Version ist bereits installiert", "el","Η έκδση του ZANavi με Δωρεά είναι ήδη εγκατεστημένη", "es","La versión donada de Zanavi, ya está instalada"};
		p(k, v);
		k = "ZANavi large map Donate Version already installed";
		v = new String[] { "en","ZANavi large map Donate Version already installed", "pt_BR","ZANavi mapa grande Versão de Doação já instalada", "fr","La version donateur \"grande carte\" de Zanavi est déjà installée", "de","large-map Donate Version ist bereits installiert", "el","Η έκδοση ολόκληρου-χάρτη με Δωρεά είναι ήδη εγκατεστημένη", "es","La versión larga del mapa donado, ya está instalado"};
		p(k, v);
		k = "There is no Donate App on Amazon-device";
		v = new String[] { "en","There is no Donate App on Amazon-device", "pt_BR","Não há Aplicação de Doação no dispositivo-Amazon", "fr","Pas de version donateur sur les appareils Amazon", "de","Auf dem Amazongerät gibt es keine Donate App", "el","Δεν υπάρχει εφαρμογή Δωρεάς στή συσκευή-Amazon"};
		p(k, v);
		k = "starting Donate App";
		v = new String[] { "en","starting Donate App", "fr","Démarrage de Zanavi édition donateur", "de","offizielle Spenden App wird gestartet", "el","έναρξη εφαρμογής δωρεάς", "es","empezando a Donar a app"};
		p(k, v);
		k = "Map Preview";
		v = new String[] { "en","Map Preview", "fr","Aperçu de la carte", "de","Kartenvorschau", "el","Προεπισκόπηση χάρτη"};
		p(k, v);
		k = "Use as destination";
		v = new String[] { "en","Use as destination", "ca","Utilitza com a destinació", "ar","اختر مقصد", "pt","Usar como destino", "pt_BR","Usar como destino", "zh","设为目的地", "zh_HK","以其為目的地", "cs","Použít jako cíl", "nl","gebruik als bestemming", "fr","Utiliser comme destination", "de","als Ziel setzen", "el","Χρήση ως προορισμός", "hu","Állítsd be célként", "it","Usa come destinazione", "pl","Użyj jako celu", "ru","Использовать как пункт назначения", "sk","Použiť ako cieľ", "es","Usar como destino"};
		p(k, v);
		k = "back";
		v = new String[] { "en","back", "ca","enrere", "ar","رجوع", "pt","voltar", "pt_BR","voltar", "zh","后退", "zh_HK","返回", "cs","zpět", "nl","terug", "fr","retour", "de","zurück", "el","πίσω", "hu","vissza", "it","indietro", "pl","wstecz", "sk","Späť", "es","Atrás"};
		p(k, v);
		k = "show destination on map";
		v = new String[] { "en","show destination on map", "ca","mostrar destinació al mapa", "ar","عرض المقصد علي الخريطة", "pt","mostrar destino no mapa", "pt_BR","mostrar destino no mapa", "zh","在地图上显示目的地", "zh_HK","於地圖顯示目的地", "cs","zobrazit cíl na mapě", "nl","toon bestemming op kaart", "fr","afficher la destination sur la carte", "de","Ziel auf Karte anzeigen", "el","εμφάνιση προορισμού στο χάρτη", "hu","Mutasd az úticélt a térképen", "it","mostra destinazione sulla mappa", "pl","pokaż cel na mapie", "ru","показать пункт назначения на карте", "sk","Zobraziť cieľ na mape", "es","mostrar destino en el mapa"};
		p(k, v);
		k = "touch map to zoom";
		v = new String[] { "en","touch map to zoom", "ca","toqueu el mapa per ampliar", "ar","المس الشاشة للتكبير", "pt","toque o mapa para ampliar", "pt_BR","toque o mapa para ampliar", "zh","点击缩放", "zh_HK","觸碰地圖放大", "cs","pro přiblížení se dotkněte mapy", "nl","raak kaart aan om te zoomen", "fr","touchez la carte pour zoomer", "de","auf Karte drücken zum zoomen", "el","αγγίξτε το χάρτη για εστίαση", "hu","Érintsd meg a térképet nagyításhoz (zoom)", "it","tocca la mappa per ingrandire", "pl","dotknij mapy aby przybliżyć", "ru","нажать на карту, чтобы увеличить", "sk","Ťuknite na mapu pre priblíženie", "es","toque el mapa para ampliar"};
		p(k, v);
		k = "downloading";
		v = new String[] { "en","downloading", "ca","S'està baixant...", "ar","تحميل", "pt","baixando", "pt_BR","baixando", "zh","正在下载", "zh_HK","下載", "cs","stahování", "nl","downloaden", "fr","transfert en cours", "el","λήψη", "hu","letöltés", "it","scaricamento", "pl","Pobieranie", "sk","Sťahuje sa", "es","descargando"};
		p(k, v);
		k = "Error downloading map!";
		v = new String[] { "en","Error downloading map!", "ca","Error descarregant mapa!", "ar","خطأ أثناء تحميل الخريطة!", "pt","Erro ao baixar o mapa!", "pt_BR","Erro ao baixar o mapa!", "zh","下载地图出错！", "zh_HK","下載地圖時出錯！", "cs","Chyba při stahování mapy!", "nl","Fout tijdens downloaden!", "fr","Erreur de téléchargement de carte!", "de","Fehler beim Kartendownload!", "el","Σφάλμα λήψης χάρτη!", "hu","Hiba térképletöltés közben!", "it","Errore scaricamento le mappe!", "pl","Błąd przy pobieraniu map", "ru","Ошибка загрузки карты!", "sk","Chyba počas sťahovania mapy", "es","Error al descargar el mapa"};
		p(k, v);
		k = "Mapdownload";
		v = new String[] { "en","Mapdownload", "ca","Baixa el mapa", "ar","تحميل الخريطة", "pt","Baixar mapa", "pt_BR","Baixar mapa", "zh","地图下载", "zh_HK","地圖下載", "cs","Stahování mapy", "nl","kaart download", "fr","Téléchargement de carte", "de","Kartendownload", "el","Λήψη χάρτη", "hu","Térképletöltés", "it","Scarica mappa", "pl","Pobieranie map", "ru","загрузка карты", "sk","Sťahovanie mapy", "es","Descargar mapa"};
		p(k, v);
		k = "ETA";
		v = new String[] { "en","ETA", "ar","المدة المتبقية للوصول", "cs","Zbýv. čas", "nl","VAT", "de","fertig in", "el","ΕΧΑ", "hu","Becsült hátralévő idő", "pl","Pozostały czas", "sk","Zostáva"};
		p(k, v);
		k = "Error downloading map, resuming";
		v = new String[] { "en","Error downloading map\nresuming ...", "ca","Error descarregant mapa, continuant", "ar","خطأ أثناء تحميل الخريطة، عودة", "pt","Erro ao baixar o mapa, continuando", "pt_BR","Erro ao baixar o mapa, continuando", "zh","下载地图出错，恢复中", "zh_HK","下載地圖時出錯，正在重新下載", "cs","Chyba při stahování mapy, zkouším znovu", "nl","Fout tijdens downloaden, opnieuw", "fr","Erreur de téléchargement de carte, raison", "de","Fehler beim Kartendownload\nDownload wird fortgesetzt ...", "el","Σφάλμα λήψης χάρτη, συνέχεια", "hu","Hiba térképletöltés közben\nújrafelvétel", "it","Errore scaricamento mappe, riprendendo", "pl","Błąd przy pobieraniu map, ponawiam", "ru","Ошибка при загрузке карты, восстановление", "sk","Chyba počas sťahovania mapy, pokračuje sa", "es","Error descargando mapa, reaunudando"};
		p(k, v);
		k = "Map already up to date";
		v = new String[] { "en","Map already up to date", "ar","الخريطة علي آخر تحديث", "pt","O mapa já está atualizado", "pt_BR","O mapa já está atualizado", "zh","地图已经是最新版本", "zh_HK","已是最新地圖", "cs","Mapa je aktuální", "nl","Kaart is al actueel", "fr","Carte déjà à jour", "de","Karte ist auf aktuellem Stand", "el","Ο χάρτης είναι ενημερωμένος", "hu","Térkép aktuális!", "it","Mappa già aggiornata", "pl","Mapa jest już w najnowszej wersji", "ru","Установлена последняя карта", "sk","Mapa je aktuálna", "es","El mapa ya está actualizado"};
		p(k, v);
		k = "checking map ...";
		v = new String[] { "en","checking map ...", "fr","Carte en cours de vérification", "de","prüfe Karte ...", "el","έλεγχος χάρτη...", "hu","Térkép ellenőrzése...", "es","Revisando el mapa ..."};
		p(k, v);
		k = "MD5 mismatch";
		v = new String[] { "en","MD5 mismatch", "pt","Falha na integridade do arquivo (MD5 não confere)", "pt_BR","Falha na integridade do arquivo (MD5 não confere)", "zh","MD5校验出错", "zh_HK","MD5 不符", "cs","Chyba MD5 kontroly", "nl","MD5 fout", "fr","MD5 incohérent", "de","MD5 Prüfsumme stimmt nicht", "el","αναντιστοιχία MD5", "hu","MD5 checksum hiba", "it","MD5 non corrispondente", "pl","skrót MD5 niepoprawny", "ru","несоответствие MD5", "sk","Nesúhlasí MD5", "es","MD5 inválido"};
		p(k, v);
		k = "Error downloading index!";
		v = new String[] { "en","Error downloading index!", "fr","Erreur lors du téléchargement de l'index", "de","Fehler beim index download!", "el","Σφάλμα κατά τη λήψη του ευρετηρίου!", "hu","Hiba az index letöltése közben!", "pl","Błąd ładowania indeksu!", "ru","Ошибка при загрузке индекса!", "es","¡Error al descargar el índice!"};
		p(k, v);
		k = "Index download";
		v = new String[] { "en","Index download", "fr","Index telechargé", "el","λήψη ευρετηρίου", "hu","Index letöltése", "es","Descargando el índice"};
		p(k, v);
		k = "downloading indexfile";
		v = new String[] { "en","downloading indexfile", "fr","Fichier d'index en cours de téléchargement", "de","lade indexfile", "el","λήψη αρχείου ευρετηρίου", "hu","Indexfile letöltése folyamatban", "pl","ładowanie pliku indeksu", "es","Descargando el archivo-índice"};
		p(k, v);
		k = "ready";
		v = new String[] { "en","ready", "ca","preparat", "ar","جاهز", "pt","pronto", "pt_BR","pronto", "zh","就绪", "zh_HK","就緒", "cs","připraveno", "nl","gereed", "fr","prêt", "de","fertig", "el","έτοιμο", "hu","kész", "it","pronto", "pl","gotowe", "sk","Pripravený", "es","listo"};
		p(k, v);
		k = "Creating outputfile, long time";
		v = new String[] { "en","Creating outputfile\nthis can take a long time", "ar","إنشاء مخرجات لملف، تأخذ وقت طويل", "pt","Criando arquivo, pode levar alguns minutos", "pt_BR","Criando arquivo, pode levar alguns minutos", "zh","正在创建输出文件，可能耗时较长", "zh_HK","建立輸出檔，要很長時間", "cs","Vytvářím výstupní soubor, bude to trvat déle", "nl","Bestand wordt gemaakt, kan even duren", "fr","Création du fichier de sortie, très long", "de","Kartendatei anlegen\nDas kann etwas dauern", "el","Δημιουργία εξαγόμενου αρχείου, αρκετός χρόνος", "hu","Kimeneti adatállomány előállítása,\nsokáig eltarthat, kérem, várjon!", "it","Creando outputfile, molto tempo", "pl","Tworzę plik wyjściowy, długi czas oczekiwania", "ru","Создание выходного файла, долго", "sk","Vytvára sa výstupný súbor", "es","Creando el fichero de salida, mucho tiempo"};
		p(k, v);
		k = "Creating outputfile, wait";
		v = new String[] { "en","Creating outputfile\nplease wait", "ar","إنشاء مخرجات لملف، أنتظر", "pt","Criando arquivo, aguarde", "pt_BR","Criando arquivo, aguarde", "zh","正在创建输出文件，请稍等", "zh_HK","建立輸出檔，請稍候", "cs","Vytvářím výstupní soubor, čekejte prosím", "nl","Bestand wordt gemaakt, even geduld", "fr","Création du fichier de sortie, patientez", "de","Kartendatei anlegen\nBitte warten", "el","Δημιουργία εξαγόμενου αρχείου, περιμένετε", "hu","Kimeneti adatállomány előállítása,\nkérem, várjon!", "it","Creando outputfile, attendere", "pl","Tworzę plik wyjściowy, czekaj", "ru","Создание выходного файла, ждите", "sk","Vytvára sa výstupný súbor, čakajte", "es","Creando fichero de salida, espere"};
		p(k, v);
		k = "generating MD5 checksum";
		v = new String[] { "en","generating MD5 checksum", "pt","gerando código de integridade (soma MD5)", "pt_BR","gerando código de integridade (soma MD5)", "zh","正在生成MD5校验和", "zh_HK","產生 MD5 checksum", "cs","Generuji MD5 kontrolní součet", "nl","MD5sum genereren", "fr","Génération du checksum MD5", "de","überprüfe MD5 Prüfsumme", "el","δημιουργία MD5 checksum", "hu","MD5 checksum kiszámítása", "it","creazione checksum MD5", "pl","generowanie skrótu MD5", "ru","создание контрольной суммы MD5", "sk","Generuje sa kontrolný súčet MD5", "es","Generando comprobación MD5"};
		p(k, v);
		k = "checking";
		v = new String[] { "en","checking", "fr","contrôle en cours", "de","prüfe", "el","έλεγχος", "es","comprobando"};
		p(k, v);
		k = "download maps";
		v = new String[] { "en","download maps", "ca","descarregar mapes", "ar","تحميل خرائط", "pt","baixar mapas", "pt_BR","baixar mapas", "zh","下载地图", "zh_HK","下載地圖", "cs","stahování map", "nl","download kaarten", "fr","télécharger les cartes", "de","Karten laden", "el","λήψη χαρτών", "hu","térképek letöltése", "it","scarica mappe", "pl","Pobierz mapy", "ru","скачать карты", "sk","Stiahnuť mapy", "es","descargar mapas"};
		p(k, v);
		k = "Your GPS is disabled, do you want to enable it?";
		v = new String[] { "en","Your GPS is disabled, do you want to enable it?", "fr","GPS désactivé, voulez vous l'activer ?", "de","GPS ist nicht aktiviert. Soll es jetzt aktiviert werden?", "el","Το GPS είναι απενεργοποιημένο, θέλετε να ενεργοποιηθεί;", "es","Su GPS está desactivado. ¿Desea activarlo?"};
		p(k, v);
		k = "Yes";
		v = new String[] { "en","Yes", "nl","Ja", "fr","Oui", "de","Ja", "el","Ναι", "hu","Igen", "es","Sí"};
		p(k, v);
		k = "No";
		v = new String[] { "en","No", "nl","Nee", "fr","Non", "de","Nein", "el","Όχι", "hu","Nem"};
		p(k, v);
		k = "No Maps installed";
		v = new String[] { "en","No Maps installed", "nl","geen kaarten geinstalleerd", "fr","Aucune carte installée", "de","Keine Karten installiert", "el","Δεν έχουν εγκατασταθεί χάρτες", "es","Los mapas no encontrados"};
		p(k, v);
		k = "Please download a map";
		v = new String[] { "en","Please download a map", "fr","Veuillez télécharger une carte", "de","Bitte eine Karte runterladen", "el","Παρακαλώ κάντε λήψη ενός χάρτη", "es","Por favor, descargue un mapa"};
		p(k, v);
		k = "__INFO_BOX_TITLE__";
		v = new String[] { "en","Welcome to ZANavi", "ar","مرحباً بك في زانافي", "pt","Bem-vindo ao ZANavi", "pt_BR","Bem-vindo ao ZANavi", "zh","欢迎使用ZANavi", "zh_HK","歡迎使用 ZANavi", "cs","Vítejte v ZANavi", "nl","Welkom bij ZANavi", "fr","Bienvenue sur ZANavi", "de","Willkommen bei ZANavi", "el","Καλώς ήλθατε στο ZANavi", "hu","Üdvözöljük a ZANavi-ban!", "pl","Witamy w ZaNavi", "sk","Vitajte v ZANavi"};
		p(k, v);
		k = "driving to Home Location";
		v = new String[] { "en","driving to Home Location", "fr","Retour à la maison", "de","navigiere zur Heimadresse", "el","οδήγηση στο Σπίτι", "es","conduciendo hacia la ubicación de inicio"};
		p(k, v);
		k = "No Home Location set";
		v = new String[] { "en","No Home Location set", "fr","Aucun domicile définit", "de","keine Heimadresse gesetzt", "el","Δεν έχει οριστεί τοποθεσία Σπιτιού", "es","No hay un conjunto de ubicaciones de inicio"};
		p(k, v);
		k = "Some Maps are too old!";
		v = new String[] { "en","Some Maps are too old!", "nl","sommige kaarten zijn te oud", "fr","Certaines cartes sont trop anciennes !", "de","Einge Karten sind zu alt!", "el","Κάποιοι χάρτες είναι πολύ παλιοί", "es","Algun mapa está desactualizado"};
		p(k, v);
		k = "Please update your maps";
		v = new String[] { "en","Please update your maps", "fr","Veuillez mettre à jour vos cartes", "de","Bitte Karten updaten", "el","Παρακαλώ ενημερώστε τους χάρτες σας", "es","Por favor, actualice los mapas"};
		p(k, v);
		k = "No Index for some Maps";
		v = new String[] { "en","No Index for some Maps", "fr","Pas d'index pour certaines cartes", "de","Kein Indexfile bei manchen Karten"};
		p(k, v);
		k = "Share Destination";
		v = new String[] { "en","Share Destination", "fr","Partager votre destination", "de","Teile Ziel", "el","Διαμοιρασμός Προορισμού", "hu","Cél megosztása", "es","Compartir destino"};
		p(k, v);
		k = "Share my Location";
		v = new String[] { "en","Share my Location", "fr","Partager ma localisation", "de","teile Standort", "el","Διαμοιρασμός Τοποθεσίας μου", "es","Compartir mi ubicación"};
		p(k, v);
		k = "address search (offline)";
		v = new String[] { "en","address search (offline)", "ar","بحث عنوان (غير متصل)", "pt","procurar endereço (offline)", "pt_BR","procurar endereço (offline)", "zh","搜索地点（离线）", "zh_HK","搜尋地址 (網下)", "cs","Hledání (offline)", "nl","adres zoeken (offline)", "fr","recherche d'adresse (hors ligne)", "de","Adressensuche (offline)", "el","αναζήτηση (χωρίς σύνδεση)", "hu","Címkeresés (offline)", "it","cerca indirizzo (offline)", "pl","wyszukaj adres (offline)", "ru","адресный поиск (офлайн)", "sk","Hľadať adresu (offline)", "es","Buscar dirección (offline)"};
		p(k, v);
		k = "address search (online)";
		v = new String[] { "en","address search (online)", "ca","cerca adreça (en línia)", "ar","بحث عنوان (متصل)", "pt","procurar endereço (online)", "pt_BR","procurar endereço (online)", "zh","搜索地点（联网）", "zh_HK","搜尋地址 (網上)", "cs","Hledání (online)", "nl","adres zoeken (online)", "fr","recherche d'adresse(en ligne)", "de","Adressensuche (online)", "el","αναζήτηση (με σύνδεση)", "hu","Címkeresés (online)", "it","cerca indirizzo (online)", "pl","wyszukaj adres (online)", "ru","адресный поиск (онлайн)", "sk","Hľadať adresu (online)", "es","Buscar dirección (online)"};
		p(k, v);
		k = "Recent destinations";
		v = new String[] { "en","Recent destinations", "ca","Destinacions recents", "ar","آخر الوجهات", "pt","Destinos recentes", "pt_BR","Destinos recentes", "zh","最近的目的地", "zh_HK","最近的目的地", "cs","Poslední cíle", "nl","recente bestemmingen", "fr","Destinations récentes", "de","Letzte Ziele", "el","Πρόσφατοι προορισμοί", "hu","Legutóbbi úticélok", "it","Destinazioni recenti", "pl","Ostatnie cele", "ru","Последние места назначения", "sk","Nedávne ciele", "es","Destinos recientes"};
		p(k, v);
		k = "Settings";
		v = new String[] { "en","Settings", "ca","Configuració", "ar","إعدادات", "pt","Configurações", "pt_BR","Configurações", "zh","设置", "zh_HK","設定", "cs","Nastavení", "nl","Instellingen", "fr","Paramètres", "de","Einstellungen", "el","Ρυθμίσεις", "hu","Beállitások", "it","Impostazioni", "pl","Ustawienia", "sk","Nastavenia", "es","Configuración"};
		p(k, v);
		k = "Search";
		v = new String[] { "en","Search", "ca","Cerca", "ar","بحث", "pt","Procurar", "pt_BR","Procurar", "zh","搜索", "zh_HK","搜尋", "cs","Hledání", "nl","Zoek", "fr","Rechercher", "de","suchen", "el","Αναζήτηση", "hu","Keresés", "it","Cerca", "pl","Wyszukaj", "sk","Hľadať", "es","Buscar"};
		p(k, v);
		k = "downloading map";
		v = new String[] { "en","downloading map", "fr","téléchargement de la carte", "de","lade Karte", "el","λήψη χάρτη", "es","descargando el mapa"};
		p(k, v);
		k = "Zoom to Route";
		v = new String[] { "en","Zoom to Route", "ar","كبر الطريق", "pt","Zoom para a rota", "pt_BR","Zoom para a rota", "zh","缩小查看线路", "zh_HK","放大至路線", "cs","Zobrazit celou trasu", "nl","Zoom naar route", "fr","Zoomer sur l'itinéraire", "de","ganze Route anzeigen", "el","Εστίαση στη Διαδρομή", "hu","Ráközelítés az útvonalra", "it","Ingrandimento per Route", "pl","Przybliż do trasy", "sk","Priblížiť na trasu", "es","Zoom a ruta"};
		p(k, v);
		k = "Donate with Google Play";
		v = new String[] { "en","Donate with Google Play", "fr","Faites un don avec Google play", "de","Spenden mit Google Play", "el","Δωρεά μέσω Google Play", "es","Donar con Google Play"};
		p(k, v);
		k = "Donate with Bitcoin";
		v = new String[] { "en","Donate with Bitcoin", "fr","Faites un don avec Bitcoin", "de","Spenden mit Bitcoin", "el","Δωρεά με Bitcoin", "es","Donar con Bitcoin"};
		p(k, v);
		k = "exit navit";
		v = new String[] { "en","Exit ZANavi", "ar","خروج من زنافي", "pt","sair", "pt_BR","sair", "zh","退出ZANvi", "zh_HK","結束 ZANavi", "cs","Ukončit ZANavi", "nl","ZANavi afsluiten", "fr","Quitter ZANavi", "de","ZANavi beenden", "el","Έξοδος από το ZANavi", "hu","KIlépés a ZANavi-ból", "it","esci da ZANavi", "pl","wyjdź z ZANavi", "ru","выйти", "sk","Ukončiť ZANavi", "es","Salir de ZANavi"};
		p(k, v);
		k = "toggle POI";
		v = new String[] { "en","toggle POI", "pt","alternar POI", "pt_BR","alternar POI", "zh","显示/隐藏兴趣点", "zh_HK","切換 POI", "cs","toogle POI", "nl","POI aan/uit", "fr","POI on/off", "de","POI ein/aus", "el","εναλλαγή POI", "hu","POI ki/be", "it","attiva/disattiva POI", "pl","włącz/wyłącz POI", "ru","переключение POI", "sk","Prepnúť POI", "es","Conmutar POI"};
		p(k, v);
		k = "Announcer On";
		v = new String[] { "en","Announcer On", "ar","فتح الإخباري", "pt","Locutor ativado", "pt_BR","Locutor ativado", "zh","语音播报已启用", "zh_HK","Announcer 開", "cs","Zapnout ohlašování", "nl","spraak aan", "fr","Voix activée", "de","Ansagen einschalten", "el","Εκφώνηση Ανοικτή", "hu","Hangbemondás be", "it","Annunciatore Acceso", "pl","Włącz komunikaty", "ru","Включить голос", "sk","Hlásateľ Zap", "es","Encender la locución"};
		p(k, v);
		k = "Announcer Off";
		v = new String[] { "en","Announcer Off", "ar","إغلاق الإخباري", "pt","Locutor desativado", "pt_BR","Locutor desativado", "zh","语音播报未启用", "zh_HK","Announcer 關", "cs","Vypnout ohlašování", "nl","spraak uit", "fr","Voix désactivée", "de","Ansagen stumm", "el","Εκφώνηση Κλειστή", "hu","Hangbemondás ki", "it","Annunciatore Spento", "pl","Wyłącz komunikaty", "ru","Отключить голос", "sk","Hlásateľ Vyp", "es","Apagar la locución"};
		p(k, v);
		k = "delete maps";
		v = new String[] { "en","delete maps", "ca","esborrar mapes", "ar","حذف خرائط", "pt","apagar mapas", "pt_BR","apagar mapas", "zh","删除地图", "zh_HK","刪除地圖", "cs","smazat mapy", "nl","kaarten verwijderen", "fr","supprimer les cartes", "de","Karten löschen", "el","διαγραφή χαρτών", "hu","térképek törlése", "it","cancella mappe", "pl","usuń mapy", "ru","удаление карты", "sk","Zmazať mapy", "es","eliminar mapas"};
		p(k, v);
		k = "show Maps age";
		v = new String[] { "en","show Maps age", "pt_BR","mostrar idade do mapa", "zh_HK","顯示地圖建立日期表", "nl","toon ouderdom kaarten", "fr","Afficher la date des cartes", "de","Karten Erstelldatum", "el","Εμφάνιση παλαιότητας χάρτη", "hu","Térkép aktualitása", "pl","Pokaż wiek map", "sk","Zobraziť vek máp", "es","Mostrar la antigüedad de los mapas"};
		p(k, v);
		k = "Coord Dialog";
		v = new String[] { "en","Coord Dialog", "pt_BR","Coordenadas", "fr","coordonnées gps", "de","Koordinaten eingeben", "el","Συντεταγμένες", "hu","Koordináta-párbeszéd", "pl","Wprowadź współrzędne", "sk","Zadanie súradníc", "es","Coordinar el diálogo"};
		p(k, v);
		k = "add Traffic block";
		v = new String[] { "en","add Traffic block", "fr","ajouter un embouteillage", "de","Baustelle markieren", "el","προσθήκη μποτιλιαρίσματος", "hu","Forgalmi akadály hozzáadása", "pl","Zaznacz blokadę ruchu", "sk","Pridať prekážku v premávke", "es","Añadir bloqueo de tráfico"};
		p(k, v);
		k = "clear Traffic blocks";
		v = new String[] { "en","clear Traffic blocks", "fr","supprimer embouteillage", "de","alle Baustellen entfernen", "el","καθαρισμός μποτιλιαρισμάτων", "hu","Forgalmi akadály eltávolítása", "pl","Skasuj wszystkie blokady ruchu", "sk","Zmazať prekážky v premávke", "es","Eliminar el bloqueo de tráfico"};
		p(k, v);
		k = "convert GPX file";
		v = new String[] { "en","convert GPX file", "pt_BR","converter arquivo GPX", "zh_HK","轉換成GPX檔案", "fr","convertir le fichier GPX", "de","GPX file konvertieren", "el","μετατροπή αρχείου GPX", "hu","GPX file konverzió", "pl","Konwertuj plik GPX", "sk","Skonvertovať GPX súbor", "es","Convertir fichero GPX"};
		p(k, v);
		k = "replay a ZANavi gps file";
		v = new String[] { "en","replay a ZANavi gps file", "fr","relire un fichier gps ZANavi", "el","αναπαραγωγή αρχείου gps ZANavi ξανά"};
		p(k, v);
		k = "run YAML tests";
		v = new String[] { "en","run YAML tests"};
		p(k, v);
		k = "clear GPX map";
		v = new String[] { "en","clear GPX map", "fr","effacer la carte GPX", "de","GPX routen entfernen", "el","καθαρισμός χάρτη GPX", "hu","GPX térkép törlése", "sk","Zmazať GPX mapu", "es","Borrar el mapa GPX"};
		p(k, v);
		k = "Demo Vehicle";
		v = new String[] { "en","Demo Vehicle", "ca","Vehicle Demo", "pt","Demonstração", "pt_BR","Demonstração", "zh","演示汽车", "zh_HK","Demo 車輛", "nl","Demo voertuig", "fr","véhicule de démonstration", "de","Demo Fahrzeug", "el","Όχημα Επίδειξης", "hu","Demo jármű", "pl","Pojazd demo", "ru","Демо-автомобиль", "sk","Predvádzacie vozidlo", "es","Vehículo Demo"};
		p(k, v);
		k = "Speech Texts";
		v = new String[] { "en","Speech Texts", "ar","خاصية قراءة النصوص", "pt_BR","Textos falados", "zh","朗读文本", "zh_HK","話音文字", "nl","Gesproken tekst", "fr","prononcer les textes", "de","Texte für Sprache", "el","Εκφώνηση Κειμένων", "hu","Beszéd szövegek", "pl","Czytaj teksty", "ru","Речевые Тексты", "sk","Rečové texty", "es","Reproduce los textos"};
		p(k, v);
		k = "Nav. Commands";
		v = new String[] { "en","Nav. Commands", "ar","أوامر التجول", "pt","Comandos", "pt_BR","Comandos", "zh","导航命令", "zh_HK","導航指令", "nl","Nav. Opdrachten", "de","Nav. Kommandos", "el","Εντ. Πλοήγησης", "hu","Navigációs utasítások", "pl","Komendy nawigacji", "ru","комманды Nav.", "sk","Nav. povely", "es","Comandos de Nav."};
		p(k, v);
		k = "toggle Routegraph";
		v = new String[] { "en","toggle Routegraph", "fr","afficher le graphique routier", "de","Routegraph anzeigen", "hu","Útvonal gfáf ki/be", "es","Conmuta el gráfico de la ruta"};
		p(k, v);
		k = "export Destinations";
		v = new String[] { "en","export Destinations", "fr","exporter les destinations", "de","Ziele exportieren", "es","Exportar la lista de destinos"};
		p(k, v);
		k = "import Destinations";
		v = new String[] { "en","import Destinations", "fr","importer les destinations", "de","Ziele importieren", "es","Importar la lista de destinos"};
		p(k, v);
		k = "send feedback";
		v = new String[] { "en","send feedback", "fr","envoyer un commentaire", "de","feedback senden", "hu","Vélemény küldése", "es","enviar comentarios"};
		p(k, v);
		k = "online Help";
		v = new String[] { "en","online Help", "ar","المساعدة (أونلاين)", "pt","ajuda online", "pt_BR","ajuda online", "zh","在线帮助", "zh_HK","網上求助", "nl","online help", "fr","aide en ligne", "de","online Hilfe", "el","online Βοήθεια", "hu","Online súgó", "it","Aiuto online", "pl","Pomoc online", "ru","Справка (онлайн)", "sk","Online pomoc", "es","Ayuda en línea"};
		p(k, v);
		k = "About";
		v = new String[] { "en","About", "de","Über"};
		p(k, v);
		k = "Target in gmaps";
		v = new String[] { "en","Target in gmaps", "ca","Destinació a gmaps", "ar","الهدف في الخريطة", "pt","Mostrar no gmaps", "pt_BR","Mostrar no gmaps", "zh","在谷歌地图中显示", "zh_HK","用 Google 地圖顯示目標", "nl","Plaats in gmaps", "fr","Cible dans Google Maps", "de","zeige in gmaps", "el","Στόχος στο gmaps", "hu","Mutasd a célt a GMaps-ben", "pl","Pokaż w GMaps", "ru","Текущ. цель в gmaps", "sk","Cieľ na Google mapách", "es","Destino en gmaps"};
		p(k, v);
		k = "Share";
		v = new String[] { "en","Share", "fr","Partager", "de","Teilen", "hu","Megosztás", "es","Recurso compartido"};
		p(k, v);
		k = "Stop Navigation";
		v = new String[] { "en","Stop Navigation", "ca","Atura la navegació", "ar","وقف الملاحة", "pt","Parar Navegação", "pt_BR","Parar Navegação", "zh","停止导航", "zh_HK","停止導航", "cs","Přestat navigovat", "nl","Stop navigatie", "fr","Arrêter la navigation", "de","Navigation beenden", "el","Διακοπή πλοήγησης", "hu","Navigálás vége", "it","Interrompi Navigazione", "pl","Zatrzymaj nawigację", "sk","Zastaviť navigáciu", "es","Detener navegación"};
		p(k, v);
		k = "No address found";
		v = new String[] { "en","No address found", "ar","لم يتم إيجاد عنوان", "pt","Endereço não encontrado", "pt_BR","Endereço não encontrado", "zh","没有找到任何地点", "zh_HK","沒有地址", "cs","Adresa nenalezena", "nl","Adres niet gevonden", "fr","Adresse non trouvée", "de","Keine Adresse gefunden", "el","Δεν βρέθηκε διεύθυνση", "hu","Cím nem található", "it","Nessun indirizzo trovato", "pl","Nie znaleziono adresu", "sk","Nenájdená žiadna adresa", "es","No se encontró ninguna dirección"};
		p(k, v);
		k = "Meeting Point";
		v = new String[] { "en","Meeting Point", "fr","Point de rencontre", "de","Treffpunkt", "hu","Találkozás helye", "es","Punto de encuentro"};
		p(k, v);
		k = "my Location";
		v = new String[] { "en","my Location", "fr","ma localisation", "de","Standort", "es","mi Localización"};
		p(k, v);
		k = "Enter: City and Street";
		v = new String[] { "en","Enter: City and Street", "ar","أدخل: المدينة و الشارع", "pt","Informe: Cidade e Rua", "pt_BR","Informe: Cidade e Rua", "zh","输入：城市和街道", "zh_HK","輸入：城市及街道", "cs","Vložte: město a ulici", "nl","Invoer: plaatsnaam en straat", "de","Stadt und Straße:", "el","Εισαγωγή: Πόλη και Οδός", "hu","Város, utca:", "it","Città e Via", "pl","Wprowadź: Miasto oraz Ulica", "ru","Ввести: Город и Улицу", "sk","Zadjate: Mesto a ulicu", "es","Introduzca: ciudad y calle"};
		p(k, v);
		k = "No search string";
		v = new String[] { "en","No search string", "fr","Recherche vide", "de","Kein Suchtext", "hu","Nincs keresési szöveg", "es","Sin cadena de búsqueda"};
		p(k, v);
		k = "No search string entered";
		v = new String[] { "en","No search string entered", "ar","لا يوجد قيمة للبحث", "pt","Não há texto a ser procurado", "pt_BR","Não há texto a ser procurado", "zh","没有输入任何搜索", "zh_HK","未輸入搜尋文字", "cs","Vyhledávací řetězec je prázdný", "nl","geen waarde opgegeven", "fr","aucun élément entré", "de","Keine Eingabe", "el","Δεν εισήχθηκε αλφαριθμητικό αναζήτησης", "hu","Keresőmező üres", "it","Nessuna stringa di ricerca inserita", "pl","Nie wprowadzono żadnych znaków", "ru","Строка поиска не введена", "sk","Nezadaný žiadny výraz", "es","No se introdujo una cadena de búsqueda"};
		p(k, v);
		k = "there was a problem with sending feedback";
		v = new String[] { "en","there was a problem with sending feedback", "fr","Il y a eu un problème lors de l'envoi des commentaires", "de","Problem beim senden von feedback", "hu","A vélemény küldése közben hiba lépett fel", "es","Hubo un problema con el envío de comentarios"};
		p(k, v);
		k = "getting search results";
		v = new String[] { "en","getting search results", "ar","عرض نتيجة البحث", "pt","obtendo resultados da busca", "pt_BR","obtendo resultados da busca", "zh","正在获取搜索结果", "zh_HK","取得搜尋結果", "cs","Získávám výsledky hledání", "nl","haalt zoekresultaten op", "fr","recherche en cours", "de","lade Suchergebnisse", "el","λήψη αποτελεσμάτων αναζήτησης", "hu","Eredmények keresése", "it","ricevendo risultati ricerca", "pl","pobieranie wyników wyszukiwania", "ru","получить результат поиска", "sk","Získava sa výsledok hľadania", "es","obteniendo resultados de la búsqueda"};
		p(k, v);
		k = "searching ...";
		v = new String[] { "en","searching ...", "ca","cercant ...", "ar","بحث ...", "pt","procurando...", "pt_BR","procurando...", "zh","搜索中.....", "zh_HK","搜尋 ...", "cs","hledám....", "nl","zoekt ...", "fr","recherche ...", "de","Suche läuft ...", "el","αναζήτηση ...", "hu","keresés ...", "it","ricerca ...", "pl","wyszukuję ...", "ru","поиск ...", "sk","Hľadá sa ...", "es","buscando ..."};
		p(k, v);
		k = "loading search results";
		v = new String[] { "en","loading search results", "ar","تحميل نتيجة البحث", "pt","carregando resultados da busca", "pt_BR","carregando resultados da busca", "zh","正在加载搜索结果", "zh_HK","載入搜尋結果", "cs","nahrávám výsledky hledání", "nl","resultaten ophalen", "fr","chargement des résultats", "de","lade Suchergebnisse", "el","φόρτωση αποτελεσμάτων αναζήτησης", "hu","eredmények betöltése", "it","caricamento risultati ricerca", "pl","wczytywanie wyników wyszukiwania", "ru","загрузить результат поиска", "sk","Načítavajú sa výsledky hľadania", "es","cargando resultados de búsqueda"};
		p(k, v);
		k = "towns";
		v = new String[] { "en","towns", "ar","قري", "pt","cidades", "pt_BR","cidades", "zh","城镇", "zh_HK","市鎮", "cs","města", "nl","steden", "fr","villes", "de","Städte", "el","πόλεις", "hu","városok", "it","città", "pl","miasta", "ru","города", "sk","Mestá", "es","ciudades"};
		p(k, v);
		k = "Streets";
		v = new String[] { "en","Streets", "ca","Carrers", "ar","الشوارع", "pt","Ruas", "pt_BR","Ruas", "zh","街道", "zh_HK","街道", "cs","Ulice", "nl","Straten", "fr","Rues", "de","Straßen", "el","Οδοί", "hu","Utcák", "it","Strade", "pl","Ulice", "sk","Ulice", "es","Calles"};
		p(k, v);
		k = "POI";
		v = new String[] { "en","POI", "fr","PdI", "hu","POI (ÉP)"};
		p(k, v);
		k = "No Results found!";
		v = new String[] { "en","No Results found!", "ar","لم يتمكن من إيجاد نتيجة!", "pt","Nada foi encontrado!", "pt_BR","Nada foi encontrado!", "zh","没有找到结果！", "zh_HK","無結果！", "cs","Nic nenalzeno", "nl","Geen resultaat gevonden!", "fr","Aucun résultat trouvé !", "de","Suche liefert kein Ergebnis!", "el","Δεν βρέθηκαν Αποτελέσματα!", "hu","Nincs találat!", "it","Nessun risultato trovato!", "pl","Nic nie znaleziono!", "ru","Ничего не найдено!", "sk","Žiadny výsledok nenájdený!", "es","No se encontraron resultados!"};
		p(k, v);
		k = "ERROR";
		v = new String[] { "en","ERROR", "de","FEHLER"};
		p(k, v);
		k = "Possibly not enough space on your device!";
		v = new String[] { "en","Possibly not enough space on your device!", "fr","Peut-être pas assez d'espace libre sur votre périphérique !", "de","Möglicherweise ist nicht genung Platz vorhanden!", "el","Πιθανόν όχι αρκετός χώρος στη συσκευή", "hu","Feltehetöen nincs elég hely az eszközön!", "pl","Prawdopodobnie brak miejsca na Twoim urządzeniu!", "sk","Pravdepodobne nedostatok voľného miesta vo Vašom zariadení!", "es","¡Posiblemente no hay suficiente espacio en su dispositivo!"};
		p(k, v);
		k = "Ok";
		v = new String[] { "en","Ok", "ar","تم", "zh","确定", "zh_HK","好", "el","Εντάξει", "hu","Rendben"};
		p(k, v);
		k = "device space";
		v = new String[] { "en","device space", "fr","Espace libre", "de","Speicherplatz", "el","χώρος στη συσκευή", "hu","Eszköz szabad tárhely", "sk","Voľné miesto v zariadení", "es","espacio en su dispositivo"};
		p(k, v);
		k = "loading ...";
		v = new String[] { "en","loading ...", "de","laden ..."};
		p(k, v);
		k = "Cancel";
		v = new String[] { "en","Cancel", "fr","Annuler", "de","Abbrechen", "hu","Mégsem", "es","Cancelar"};
		p(k, v);
		k = "Send feedback via email ...";
		v = new String[] { "en","Send feedback via email ...", "fr","Envoyer les commentaires par courriel", "de","Feedback per Email senden ...", "es","Envíe sus comentarios por correo electrónico ..."};
		p(k, v);
		k = "Send email with attachments";
		v = new String[] { "en","Send email with attachments", "de","Email mit Anhang senden"};
		p(k, v);
		k = "No Email App found";
		v = new String[] { "en","No Email App found", "de","kein Email App installiert"};
		p(k, v);
		k = "Please press BACK again to Exit";
		v = new String[] { "en","Please press BACK again to Exit", "fr","Appuyez une deuxième fois sur retour pour quitter", "de","nochmal BACK drücken um App zu beenden", "es","Por favor, pulse de nuevo ATRÁS"};
		p(k, v);
		k = "google search API is not working at this moment, try offline search";
		v = new String[] { "en","google search API is not working at this moment, try offline search", "fr","L'API de recherche Google ne fonctionne pas en ce moment, essayez une recherche hors-ligne", "de","Google Suche funktioniert im Moment nicht, bitte offline Suche verwenden", "hu","A Google keresö API nem müködik, próbáld késöbb", "es","La búsqueda de la API de google no está funcionando en este momento, busque fuera de línea"};
		p(k, v);
		k = "saving route to GPX-file";
		v = new String[] { "en","saving route to GPX-file", "fr","enregistrement de l'itinéraire vers le fichier GPX", "es","Guardando la ruta en archivo GPX"};
		p(k, v);
		k = "saving route to GPX-file failed";
		v = new String[] { "en","saving route to GPX-file failed", "fr","l'enregistrement de l'itinéraire vers le fichier GPX a échoué", "de","saving route to GPX-file", "es","No fue posible guardar la ruta en archivo GPX"};
		p(k, v);
		k = "setting destination to";
		v = new String[] { "en","setting destination to", "ar","تحديد الوجهة إلي", "pt","definindo destino para", "pt_BR","definindo destino para", "zh","将目的地设为", "zh_HK","將目的地設為", "cs","Nastavuji cíl do", "nl","bestemming ingesteld op", "fr","destination vers", "de","neues Fahrziel:", "el","καθορισμός προορισμού στο", "hu","Cél beállítva:", "it","imposta destinazione a", "pl","ustawianie celu trasy do", "sk","Nastavuje sa cieľ na", "es","estableciendo el destino en"};
		p(k, v);
		k = "new Waypoint";
		v = new String[] { "en","new Waypoint", "ar","نقطة طريق جديدة", "pt","novo ponto de partida", "pt_BR","novo ponto de partida", "zh","新建路径点", "zh_HK","新 Waypoint", "cs","nový waypoint", "nl","nieuw routepunt", "fr","nouvelle destination", "de","neues Zwischenziel", "el","νέο σημείο", "hu","Új útpont", "pl","Nowy punkt", "ru","Новая Точка", "sk","nový cestovný bod", "es","Nuevo Punto de Interés"};
		p(k, v);
		k = "Search results";
		v = new String[] { "en","Search results", "nl","Zoekresultaten", "fr","Résultats de la recherche", "de","Suchergebnisse"};
		p(k, v);
		k = "Destination set";
		v = new String[] { "en","Destination set", "nl","Bestemming ingesteld", "fr","Fixer la destination", "de","Ziel gesetzt", "el","Ορισμός προορισμού", "hu","Cél beadva", "sk","Cieľ bol určený", "es","Conjunto de destinos"};
		p(k, v);
		k = "No route found / Route blocked";
		v = new String[] { "en","No route found / Route blocked", "nl","geen route gevonden", "fr","Aucun itinéraire trouvé/itinéraire bloqué", "de","keine Route gefunden / blockiert", "el","Δεν βέθηκε διαδρομή / μπλοκαρισμένη διαδρομή", "hu","Nem található útvonal / útvonal lezárva", "pl","Nie znaleziono drogi / Droga zablokowana", "sk","Nenašla sa žiadna trasa / Trasa zablokovaná", "es","No se encontró una ruta / Ruta bloqueada"};
		p(k, v);
		k = "Building route path";
		v = new String[] { "en","Building route path", "fr","Création de l'itinéraire", "de","generiere Route Path", "hu","Útvonal kiszámolása", "it","Calcolando l'itinerario", "pl","Generowanie trasy", "es","Creando la trayectoria de la ruta"};
		p(k, v);
		k = "Building route graph";
		v = new String[] { "en","Building route graph", "fr","Construction du graphique routier", "de","generiere Route Graph", "hu","Útvonal ábra kiszámolása", "pl","Generowanie wykresu trasy", "sk","Zostavuje sa graf trasy", "es","Creando el gráfico de la ruta"};
		p(k, v);
		k = "downloading, please wait ...";
		v = new String[] { "en","downloading, please wait ...", "fr","téléchargement, veuillez patienter...", "de","laden, bitte warten ...", "hu","Letöltés ... kérlek, várj!", "es","descargando, espere, por favor"};
		p(k, v);
		k = "__PREF__title__use_fast_provider";
		v = new String[] { "en","GSM/3g/Wireless", "pt","GSM/3G/Sem fio", "pt_BR","GSM/3G/Sem fio", "zh","GSM/3g/无线", "cs","GSM/3g/WiFi", "nl","GSM/3G/WiFi", "fr","GSM/3G/Sans-fil", "de","GSM/3g/Wireless", "el","GSM/3g/Ασύρματο", "hu","GSM/3g/Wireless", "pl","GSM/3g/WiFi", "sk","GSM/3g/Bezdrôtové"};
		p(k, v);
		k = "__PREF__summ__use_fast_provider";
		v = new String[] { "en","Use GSM/3g/Wireless networks for getting position (uses Internet)", "pt","Utilizar redes GSM/3G/Sem fio para obter posição (usa a Internet)", "pt_BR","Utilizar redes GSM/3G/Sem fio para obter posição (usa a Internet)", "zh","使用GSM/3g/无线网路定位（需要联网）", "cs","K získání pozice použij síť GSM/3g/WiFi (používá Internet)", "nl","Gebruik GSM/3G/WiFi om de positie te bepalen (Internet)", "fr","GSM/3G/Sans-fil pour déterminer la position (utilise internet)", "de","Position auch mit GSM/3g/Wireless finden (braucht Internet)", "el","Χρήση GSM/3g/Ασύρματων δυκτων για λήψη τοποθεσίας (χρήση Internet)", "hu","GSM/3g/Wireless (Internet!) használata a pozíciómeghatározáshoz", "pl","Użyj sieci GSM/3g/WiFi aby ustalić pozycję (wykorzystuje Internet)", "sk","Použiť GSM/3g/Bezdrôtové siete na získanie pozície (použije Internet)"};
		p(k, v);
		k = "__PREF__title__use_agps";
		v = new String[] { "en","aGPS", "zh","辅助GPS", "cs","aGPS", "nl","aGPS", "fr","aGPS", "de","aGPS", "el","aGPS", "hu","aGPS használata", "pl","aGPS", "sk","aGPS"};
		p(k, v);
		k = "__PREF__summ__use_agps";
		v = new String[] { "en","use assisted GPS (uses Internet !!)", "zh","使用辅助GPS（需要联网！）", "cs","Používej asistované GPS (potřebuje Internet)", "nl","gebruik assisted GPS (gebruikt Internet !!)", "fr","Utilise le GPS assisté (utilise internet)", "de","aGPS verwenden für schnellere Positionsfindung (braucht Internet !!)", "el","χρήση aGPS (με χρήση Internet !!)", "hu","Assisted GPS használata (internet kapcsolat!)", "pl","Użyj assisted GPS (wykorzystuje Internet!!)", "sk","Použiť asistované GPS (použije Internet !!)"};
		p(k, v);
		k = "__PREF__title__follow_gps";
		v = new String[] { "en","Follow", "pt","Seguir", "pt_BR","Seguir", "zh","跟随GPS", "cs","Následuj", "nl","Volg GPS", "fr","Suivi GPS", "de","folgen", "el","Ακολούθησε", "hu","Követés", "pl","Podążaj za GPS", "sk","Nasledovať"};
		p(k, v);
		k = "__PREF__summ__follow_gps";
		v = new String[] { "en","Center on GPS. activate this for Compass heading to work", "pt","Sincronizar com GPS. Ative isso para que a bússola funcione", "pt_BR","Sincronizar com GPS. Ative isso para que a bússola funcione", "zh","居中显示GPS。使用罗盘导航需要激活此功能。", "cs","Centruj mapu podle GPS. Aktivujte tuto volbu, pokud chcete používat směrování kompasem.", "nl","Volg GPS. Benodigd voor de kompas richting", "fr","Centré sur la position GPS, Activez cette option pour le suivi du cap", "de","Karte folgt Fahrzeug", "el","Κεντράρισμα στο GPS. ενεργοποιήστε για τη λειτουργία κατεύθυνσης πυξίδας", "hu","A térkép követi a helyzetet (sticky)", "pl","Podążaj za pozycją GPS. Włącz aby działał kompas.", "sk","Centrovať na GPS. Aktivujte toto pre smerovanie podľa kompasu."};
		p(k, v);
		k = "__PREF__title__use_lock_on_roads";
		v = new String[] { "en","lock on Roads", "zh","锁定道路", "cs","Jen po silnicích", "nl","plaats op de weg", "fr","Bloqué sur les routes", "de","Fahrzeug auf Strasse", "el","κλείδωμα στο Δρόμο", "hu","Maradj az úton", "pl","Przyciągaj do drogi", "sk","Prichytiť k ceste"};
		p(k, v);
		k = "__PREF__summ__use_lock_on_roads";
		v = new String[] { "en","lock Vehicle on nearest Road. turn off if you are walking or driving offroad", "zh","将汽车锁定到最近的道路。如果您是步行或越野，请关闭此选项。", "cs","Umístnit pozici vždy na nejbližší komunikaci. Vypněte, pokud se pohybujete mimo značené trasy.", "nl","Plaatst het voertuig op de dichtsbijzijnde weg. Zet deze uit voor wandelen of offroad rijden", "fr","Localise exclusivement le véhicule sur les routes les plus proches à désactiver si vous marchez ou roulez hors route", "de","Fahrzeug auf nächstgelegene Strasse fixieren. Zu Fuß oder Offroad bitte ausschalten", "el","κλείδωμα του οχήματος στον κοντινότερο Δρόμο. απενεργοποιήστε αν περπατάτε ή οδηγείτε εκτός δρόμου", "hu","A pointert az úton tartja akkor is, ha esetleg a koordináta eltér (autós beállítás, gyalog vagy terepen kikapcsolandó)", "pl","Przyciągaj pojazd do najbliższej drogi. Wyłącz jeśli idziesz pieszo lub jedziesz w terenie", "sk","Prichytiť vozidlo k najbližšej ceste. Vypnite ak kráčate alebo jazdíte v teréne."};
		p(k, v);
		k = "__PREF__title__show_vehicle_in_center";
		v = new String[] { "en","Vehicle in center", "pt","Veículo no centro", "pt_BR","Veículo no centro", "zh","居中显示汽车", "cs","Centruj dle pozice", "nl","Centreer op voertuig", "fr","Véhicule au centre", "de","Fahrzeug", "el","Όχημα στο κέντρο", "hu","Pozíció a középpontban", "pl","Pojazd na środku", "sk","Vozidlo v strede"};
		p(k, v);
		k = "__PREF__summ__show_vehicle_in_center";
		v = new String[] { "en","show vehicle in screen center, instead of the lower half", "pt","mostra o veículo no centro da tela ao invés de exibir na parte inferior", "pt_BR","mostra o veículo no centro da tela ao invés de exibir na parte inferior", "zh","在屏幕居中显示汽车，而不是屏幕下半部分显示", "cs","Zobrazuje pozici uprostřed obrazovky místo ve spodní části", "nl","Toont het voertuig in het midden van het scherm in plaats van onderin.", "fr","Affiche le véhicule au centre de l'écran, au lieu d'en bas", "de","Fahrzeug in Displaymitte, statt weiter unten", "el","εμφάνιση του οχήματος στο κέντρο της οθόνης, αντί για το κάτω μισό", "hu","A pozíció a térkép közepére rögzül", "pl","Pokaż pojazd na środku ekranu zamiast w dolnej części", "sk","Zobraziť vozidlo v strede obrazovky, namiesto spodnej polovice"};
		p(k, v);
		k = "__PREF__title__show_sat_status";
		v = new String[] { "en","Sat Status", "zh","卫星状态", "cs","Stav statelitů", "nl","satteliet status", "fr","État du GPS", "de","Sat Status", "el","Κατάσταση δορυφόρων", "hu","Műholdak", "pl","Status satelitów", "sk","Stav satelitov"};
		p(k, v);
		k = "__PREF__summ__show_sat_status";
		v = new String[] { "en","show Statellite Status", "zh","显示卫星状态", "cs","Ukazuj stav GPS satelitů", "nl","weergeven satteliet status", "fr","Affiche l'état de la réception satellite", "de","Satelliten Status anzeigen", "el","εμφάνιση κατάστασης δορυφόρων", "hu","Mutasd a műholdak állapotát", "pl","Pokaż status satelitów", "sk","Zobraziť stav satelitov"};
		p(k, v);
		k = "__PREF__title__use_compass_heading_base";
		v = new String[] { "en","Compass", "pt","Bússola", "pt_BR","Bússola", "zh","罗盘", "cs","Kompas", "nl","Kompas richting", "fr","Cap", "de","Kompass", "el","Πυξίδα", "hu","Iránytű", "pl","Kompas", "sk","Kompas"};
		p(k, v);
		k = "__PREF__summ__use_compass_heading_base";
		v = new String[] { "en","Get direction from compass. needs lots of CPU!", "pt","Obter direção da bússola. Exige muito da CPU!", "pt_BR","Obter direção da bússola. Exige muito da CPU!", "zh","从罗盘中获取方位。需要较多CPU！", "cs","Získej směr jízdy podle kompasu. Potřebuje hodně CPU (a baterie).", "nl","Gebruik kompas voor de richting. Kost veel CPU! De wereldweergave zal afwijken!", "fr","Prends la direction en suivant le cap. Prends beaucoup de CPU !", "de","Kompass verwenden. braucht viel CPU !!", "el","Λήψη κατεύθυνσης από την πυξίδα. χρήση αρκετής CPU!", "hu","Iránytű mutatása; sok CPU-t zabál!", "pl","Ustal kierunki na podstawie kompasu. Intensywnie wykorzystuje CPU!", "sk","Získať smer z kompasu. Veľmi zaťaží CPU!"};
		p(k, v);
		k = "__PREF__title__use_compass_heading_always";
		v = new String[] { "en","Compass always", "pt","Bússola sempre", "pt_BR","Bússola sempre", "zh","总是使用罗盘", "cs","Vždy kompas", "nl","Altijd kompas gebruiken", "fr","Cap permanent", "de","Kompass immer", "el","Χρήση πυξίδας πάντα", "hu","Iránytű menet közben", "pl","Zawsze użyj kompasu", "sk","Vždy kompas"};
		p(k, v);
		k = "__PREF__summ__use_compass_heading_always";
		v = new String[] { "en","Get current heading from compass even at higher speeds", "pt","Obter posição atual da bússola mesmo a altas velocidades", "pt_BR","Obter posição atual da bússola mesmo a altas velocidades", "zh","即使在高速行驶中也使用罗盘获取线路", "cs","Získávej směr jizdy vždy podle kompasu a to i v případě vyšších rychlostí", "nl","Gebruikt het kompas voor de rijrichting, ook op hoge snelheid", "fr","Garde toujours le cap y compris à grande vitesse", "de","immer Kompassrichtung verwenden", "el","Λήψη τρέχουσας κατεύθυνσης από την πυξίδα ακόμη και σε υψηλότερες ταχύτητες", "hu","Iránytű használata menet közben is", "pl","Używa kompasu  nawet przy dużych prędkościach", "sk","Získať aktuálne smerovanie z kompasu aj vo vysokej rýchlosti"};
		p(k, v);
		k = "__PREF__title__use_compass_heading_fast";
		v = new String[] { "en","fast Compass", "pt","Bússola rápida", "pt_BR","Bússola rápida", "zh","快速罗盘", "cs","Rychlý kompas", "nl","Gebruik snel kompas", "fr","Cap rapide", "de","Kompass schnell", "el","Γρήγορη Πυξίδα", "hu","gyors iránytű", "pl","Szybki kompas", "sk","Rýchly kompas"};
		p(k, v);
		k = "__PREF__summ__use_compass_heading_fast";
		v = new String[] { "en","turns much smoother, WARNING: WILL EAT ALL your CPU!!", "pt","Movimentos ficam mais suaves, CUIDADO: Utiliza TODA a CPU!", "pt_BR","Movimentos ficam mais suaves, CUIDADO: Utiliza TODA a CPU!", "zh","平滑转弯。警告：可能耗尽CPU！", "cs","Kompas se točí velmi jemně. POZOR: vezme si veškerý výkon CPU", "nl","Veel soepeler bij afslagen, WAARSCHUWING: gebruikt heel veel CPU!!!", "fr","Plus fluide, ATTENTION: Utilise TOUTE la PUISSANCE CPU!!!", "de","Kompass reagiert schnell. braucht noch viel mehr CPU!!", "el","πιο ήπια περιστροφή, ΠΡΟΕΙΔΟΠΟΙΗΣΗ: ΘΑ ΦΑΕΙ ΟΛΗ ΤΗΝ CPU!!", "hu","Az iránytű gyorsítása, még több CPU-t zabál!", "pl","Zakręty bardzo płynne. UWAGA! ZJADA CAŁĄ MOC CPU!", "sk","Oveľa plynulejšie otáčanie, VAROVANIE: ÚPLNE ZAŤAŽÍ CPU!!"};
		p(k, v);
		k = "__PREF__title__use_imperial";
		v = new String[] { "en","Imperial", "pt","Medidas Britânicas", "pt_BR","Medidas Britânicas", "zh","英制单位", "cs","Imperial", "nl","Engelse maten", "fr","Impérial", "de","Meilen/Fuss", "el","Αγγλοσαξονικό", "hu","Mértékegységek angolszászra", "pl","Jednostki imperialne", "sk","Imperiálne jednotky", "es","Imperial"};
		p(k, v);
		k = "__PREF__summ__use_imperial";
		v = new String[] { "en","Use Imperial units instead of metric units", "pt","Usa sistema imperial ao invés do sistema métrico", "pt_BR","Usa sistema imperial ao invés do sistema métrico", "zh","使用英制单位，而不是公制单位", "cs","Používej imperiální jednotky (míle, ...) místo metrických.", "nl","Gebruik Engelse eenheden in plaats van metrische", "fr","Utilise les unités impériales au lieu des unités métriques", "de","Englische Maßeinheiten wie Meilen benutzen", "el","Χρήση Αγγλικών μονάδων αντί για μετρικές", "hu","Mérföld, láb stb. km, méter helyett", "pl","Użyj jednostek imperialnych (brytyjskich) zamiast metrycznych", "sk","Použiť imperiálne jednotky namiesto metrických"};
		p(k, v);
		k = "__PREF__title__use_route_highways";
		v = new String[] { "en","prefer Highways", "zh","高速公路优先", "cs","Preferuj dálnice", "nl","voorkeur voor snelwegen", "fr","Préférer les routes", "de","Autobahn bevorzugen", "el","προτίμηση Αυτοκινητοδρόμων", "hu","Autópályák", "pl","Preferuj autostrady", "sk","Uprednostňovať diaľnice"};
		p(k, v);
		k = "__PREF__summ__use_route_highways";
		v = new String[] { "en","prefer Highways for routing (switching this off uses more Memory!!)", "zh","优先选择高速公路（关闭此选项会耗用更多内存）。", "cs","Preferování dálnic. Při vypnuté volbě je potřeba více paměti.", "nl","geef voorkeur aan snelwegen voor de route (uitschakelen gebruikt meer geheugen!!)", "fr","Préfère les routes pour le calcul des trajets (Désactiver cette option utilise plus de mémoire !!)", "de","Autobahnen werden bevorzugt (abschalten braucht viel mehr Speicher!!)", "el","προτίμηση Αυτοκινητοδρόμων για δρομολόγηση (απενεργοποιώντας αυτό, χρησιμοποιεί περισσότερη μνήμη!!)", "hu","Részesítsd előnyben az autópályák használatát", "pl","Preferuj autostrady przy nawigacji (wyłączenie zwiększa zużycie pamięci!!)", "sk","Uprednostniť diaľnice pri navigovaní (vypnutie spôsobí väčšiu spotrebu pamäti!!)"};
		p(k, v);
		k = "__PREF__title__use_index_search";
		v = new String[] { "en","index search", "fr","Recherche indexée", "de","index suche", "hu","Indexelt keresés"};
		p(k, v);
		k = "__PREF__summ__use_index_search";
		v = new String[] { "en","use faster and better index search [donate version]", "fr","recherche indexée, plus rapide et precise [appli donateur]", "de","schnellere index suche verwenden [donate version]", "hu","Indexelt keresés"};
		p(k, v);
		k = "__PREF__title__trafficlights_delay";
		v = new String[] { "en","traffic lights", "fr","Feux tricolores", "de","Ampeln", "el","φανάρια", "hu","Jelzölámpa késlekedés"};
		p(k, v);
		k = "__PREF__summ__trafficlights_delay";
		v = new String[] { "en","set delay of traffic lights for routing", "fr","Ralentissement lié aux feux tricolores", "de","Verzögerung durch Amplen auf der Route", "el","ορίστε την  καθυστέρηση των φαναριών για την δρομολόγηση", "hu","Jelzölámpa késlekedés"};
		p(k, v);
		k = "__PREF__title__speak_street_names";
		v = new String[] { "en","speak Streetnames", "zh","播报街道名字", "nl","Uitspreken straatnamen", "fr","Annonce des rues", "de","Strassennamen", "el","εκφώνηση ονομάτων δρόμων", "hu","Utcanevek bemondása", "pl","Czytaj nazwy ulic", "sk","Hovoriť názvy ulíc"};
		p(k, v);
		k = "__PREF__summ__speak_street_names";
		v = new String[] { "en","speak Streetnames when navigating", "zh","导航时播报街道名字", "fr","Annonce des rues durant la navigation", "de","Strassennamen beim Navigieren sprechen", "el","εκφώνηση ονομάτων δρόμων κατά την πλοήγηση", "hu","Bemondja az utcaneveket", "pl","Czytaj nazwy ulic podczas nawigacji", "sk","Hovoriť názvy ulíc počas navigácie"};
		p(k, v);
		k = "__PREF__title__speak_filter_special_chars";
		v = new String[] { "en","filter special chars", "fr","filtrer les caractères spéciaux", "de","sonderzeichen filtern", "hu","Ékezetes betük szürése"};
		p(k, v);
		k = "__PREF__summ__speak_filter_special_chars";
		v = new String[] { "en","filter special chars when speaking streetnames", "fr","Ne pas prononcer les caractères spéciaux dans les noms de rues", "de","sonderzeichen beim sprechen von Straßennamen herausfiltern", "hu","Ékezetes betük szürése"};
		p(k, v);
		k = "__PREF__title__route_style";
		v = new String[] { "en","route style", "fr","Style des routes", "de","Routenstil", "hu","Útvonal típusa", "pl","Styl trasy", "sk","Štýl trasy"};
		p(k, v);
		k = "__PREF__summ__route_style";
		v = new String[] { "en","graphic style of route", "fr","Style graphique pour l'affichage des routes", "de","wählt den graphischen Stil wie die Route gezeichnet wird", "hu","Útvonal típusa", "pl","Styl rysowania trasy", "sk","Vykreslenie trasy na mape v rôznych štýloch"};
		p(k, v);
		k = "__PREF__title__show_3d_map";
		v = new String[] { "en","3D", "pt","3D", "pt_BR","3D", "zh","3D", "cs","3D", "nl","Kaart in 3D", "fr","3D", "de","3D", "el","3D", "hu","3D térkép", "pl","3D", "sk","3D"};
		p(k, v);
		k = "__PREF__summ__show_3d_map";
		v = new String[] { "en","show map in 3D [BETA]", "pt","mostra mapa em 3D [BETA]", "pt_BR","mostra mapa em 3D [BETA]", "zh","显示3D地图[BETA]", "cs","Zobrazení mapy v 3D [BETA]", "nl","3D weergave van de kaart [BETA]", "fr","Afficher la carte en 3D [BETA]", "de","Karte in 3D [BETA]", "el","εμφάνιση χάρτη σε 3D [BETA]", "hu","3D térkép bekapcsolása 2D helyett [BETA]", "pl","Pokaż mapę w 3D [BETA]", "sk","Zobraziť mapu v 3D [BETA]"};
		p(k, v);
		k = "__PREF__title__show_2d3d_toggle";
		v = new String[] { "en","2D/3D toggle", "fr","Basculer 2D/3D", "de","2D/3D Knopf", "hu","2D / 3d átkapcsolás"};
		p(k, v);
		k = "__PREF__summ__show_2d3d_toggle";
		v = new String[] { "en","show 2D/3D toggle instead of Map off/on toggle", "fr","Affiche un bouton 2D/3D à la place de carte on/off.", "de","2D/3D Knopf statt Karte-aus-knopf anzeigen", "hu","2D / 3d átkapcsolás"};
		p(k, v);
		k = "__PREF__title__save_zoomlevel";
		v = new String[] { "en","Zoomlevel", "zh","缩放级别", "cs","Úroveň přiblížení", "nl","zoomniveau", "fr","Niveau de zoom", "de","Zoomstufe", "el","Κλίμακα εστίασης", "hu","Kivágás (zoom) tárolása", "pl","Powiększenie", "sk","Úroveň priblíženia"};
		p(k, v);
		k = "__PREF__summ__save_zoomlevel";
		v = new String[] { "en","save last Zoomlevel", "zh","保存上次缩放级别", "cs","Ukládat posledně použitou úroveň přiblížení.", "nl","bewaar laatste zoomniveau", "fr","Sauvegarder le niveau de zoom précédent", "de","Zoomstufe speichern", "el","αποθήκευση τελευταίας κλίμακας εστίασης", "hu","Tárold el az utolsó kivágás (zoom) méretet", "pl","Zapamiętaj powiększenie", "sk","Zapamätať poslednú úroveň priblíženia"};
		p(k, v);
		k = "__PREF__title__autozoom_flag";
		v = new String[] { "en","Autozoom", "fr","Zoom auto", "de","Autozoom", "hu","Automatikus zoom ki / be"};
		p(k, v);
		k = "__PREF__summ__autozoom_flag";
		v = new String[] { "en","set map zoom automatically according to driving speed", "fr","Réglage du zoom automatique en fonction de la vitesse.", "de","Kartenzoom nach Fahrtgeschwindigkeit automatisch einstellen", "hu","Automatikus zoom ki / be"};
		p(k, v);
		k = "__PREF__title__use_anti_aliasing";
		v = new String[] { "en","AntiAlias", "zh","反走样", "cs","Vyhlazování", "nl","Anti Aliasing", "fr","AntiAlias", "de","Antialiasing", "el","Εξομάλυνση", "hu","Kontúrsimítás", "pl","Antyaliasing", "sk","Vyhladzovanie"};
		p(k, v);
		k = "__PREF__summ__use_anti_aliasing";
		v = new String[] { "en","draw with AntiAlias, map is faster when this is OFF", "zh","反走样绘制地图。关闭此选项，地图将绘制更快。", "cs","Při vykreslování mapy použij vyhlazování čar. Je rychlejší, pokud je vypnuto.", "nl","schakel anti aliasing in", "fr","Afficher la carte avec de l'AntiAlias. L'affichage est plus rapide que cette option est désactivée", "de","Antialiasing einschalten. Karte zeichnet schneller ohne Antialiasing", "el","σχεδίαση με εξομάλυνση, ο χάρτης είναι πιο γρήγορος όταν δεν είναι ενεργοποιημένο", "hu","Kontúrsimítás bekapcsolása; enélkül a térkép gyorsabb.", "pl","Rysuj z antyaliasingiem, mapa jest szybsza, gdy to jest wyłączone", "sk","Vykresľovať mapu s vyhladzovaním. Mapa je rýchlejšia, ak je toto vypnuté."};
		p(k, v);
		k = "__PREF__title__use_map_filtering";
		v = new String[] { "en","filtering", "fr","Re-traitement de la carte", "de","filtering", "hu","Térkép szürés"};
		p(k, v);
		k = "__PREF__summ__use_map_filtering";
		v = new String[] { "en","filtering draws map more smoothly, but needs more CPU", "fr","Affichage plus net et fluide mais solicite plus le processeur.", "de","filtering einschalten, zeichnet die Karte schöner. braucht aber mehr Leistung", "hu","Térkép szürés"};
		p(k, v);
		k = "__PREF__title__use_custom_font";
		v = new String[] { "en","custom Font", "zh","自定义字体", "fr","Polices dédiées", "de","custom Font", "el","χρήση προσαρμοσμένης γραμματοσειράς", "hu","Saját font használata", "pl","Czcionka własna", "sk","Vlastné písmo"};
		p(k, v);
		k = "__PREF__summ__use_custom_font";
		v = new String[] { "en","use Font included in ZANavi (otherwise use System Font)", "zh","使用ZANavi内置的字体（否则使用系统字体）", "fr","Utilise les polices incluses dans ZANavi (sinon les polices du système sont utilisées)", "de","Font von ZANavi verwenden (sonst Systemschriftart verwenden)", "el","χρήση γραμματοσειράς που περιέχεται στο ZANavi (αλλιώς, χρήση γραμματοσειράς συστήματος)", "hu","Saját font használatának engedélyezése", "pl","Użyj własnej czcionki ZANavi zamiast systemowej", "sk","Použiť písmo zahrnuté v ZANavi (inak použiť systémové písmo)"};
		p(k, v);
		k = "__PREF__title__use_smooth_drawing";
		v = new String[] { "en","smooth driving", "fr","Fluidifier l'affichage", "de","flüssiger fahren", "pl","Płynne przewijanie"};
		p(k, v);
		k = "__PREF__summ__use_smooth_drawing";
		v = new String[] { "en","scroll map more smoothly when driving", "fr","Deplacements dans la carte plus fluides.", "de","verschiebt die Karte beim Fahren schöner und flüssiger, braucht jedoch mehr Leistung!", "pl","płynne przewijanie mapy podczas jazdy"};
		p(k, v);
		k = "__PREF__title__use_more_smooth_drawing";
		v = new String[] { "en","Even Smoother Driving", "fr","Fluidifier l'affichage encore plus", "de","noch flüssiger fahren", "pl","Jeszcze płynniejsze przewijanie"};
		p(k, v);
		k = "__PREF__summ__use_more_smooth_drawing";
		v = new String[] { "en","Much smoother driving, WARNING: WILL EAT ALL your CPU!!", "fr","Deplacements encore plus fluides. ATTENTION: Utilise TOUTE la PUISSANCE CPU!!!", "de","Verschiebt die Karte noch flüssiger. WARNUNG: braucht sehr viel Leistung!!", "pl","Bardzo płynne przewijanie, UWAGA: ZJADA CAŁĄ MOC CPU!!"};
		p(k, v);
		k = "__PREF__title__show_multipolygons";
		v = new String[] { "en","Multipolygons", "fr","Polygônes multiples", "de","Multipolygone", "hu","Multipoligonok mutatása", "pl","Wielokąty"};
		p(k, v);
		k = "__PREF__summ__show_multipolygons";
		v = new String[] { "en","draw lines and areas from multipolygons", "fr","Affiche les lignes et plans en utilisant des polygônes multiples", "de","Multipolygone (wie Flüsse und grosse Häuser) anzeigen", "hu","Multipoligonok mutatása", "pl","rysuj linie i obszary za pomocą wielokątów"};
		p(k, v);
		k = "__PREF__title__show_vehicle_3d";
		v = new String[] { "en","Vehicle 3D", "fr","Véhicule 3D", "de","3D Fahrzeug", "hu","Jármü 3D", "pl","Pojazd 3D"};
		p(k, v);
		k = "__PREF__summ__show_vehicle_3d";
		v = new String[] { "en","show vehicle correctly in 3D mode", "fr","Affiche le véhicule correctement en mode 3D", "de","zeigt das Fahrzeug im 3D Modus perspektivisch korrekt an", "hu","Jármü 3D", "pl","pokazuj poprawnie pojazd w 3D"};
		p(k, v);
		k = "__PREF__title__map_font_size";
		v = new String[] { "en","Map Font size", "zh","地图字体大小", "fr","Taille de la police pour les cartes", "de","Karten Font Größe", "el","Μέγεθος γραμματοσειράς χάρτη", "hu","Térkép font méret", "pl","Rozmiar czcionki", "sk","Veľkosť písma na mape"};
		p(k, v);
		k = "__PREF__summ__map_font_size";
		v = new String[] { "en","set Font size for Map Texts (streetnames, etc.)", "zh","设置地图的字体大小（例如街道名等）", "fr","Réglage de la taille des polices pour l'affichage de la carte (nom des rues, etc...).", "de","Größe der Karten Texte (Straßennamen, usw.)", "el","μέγεθος γραμματοσειράς χάρτη (ονόματα δρόμων, κλπ.)", "hu","A térkép betűméret beállítása", "pl","Rozmiar czcionki na mapie (nazwy ulic itp.)", "sk","Nastaviť veľkosť písma pre text na mape (názvy ulíc, atď.)"};
		p(k, v);
		k = "__PREF__title__drawatorder";
		v = new String[] { "en","Draw more Detail", "zh","绘制更多细节", "fr","Affiche plus de details", "de","Mehr Detail", "el","Σχεδίαση περισσότερων λεπτομερειών", "hu","Több részlet", "pl","Więcej szczegółów", "sk","Detailnejšie vykresľovanie"};
		p(k, v);
		k = "__PREF__summ__drawatorder";
		v = new String[] { "en","Draw more Detail on map. change only on fast devices!", "zh","在地图上绘制更多细节。请只在高端设备上启用。", "fr","Affiche plus de details sur la carte. ATTENTION: Utilise TOUTE la PUISSANCE CPU!!!", "de","Zeichnet mehr Details auf der Karte. Nur für schnelle Geräte!", "el","Σχεδιάζει περισσότερες λεπτομέρειες στο χάρτη. Αλλάξτε το μόνο σε γρήγορες συσκευές.", "hu","Részletesebb térképet rajzol. Csak gyors készülékekre!", "pl","Więcej szczegółów na mapie. Tylko szybkie urządzenia!", "sk","Vykresliť viac podrobností na mape. Zmenťe len na rýchlych zariadeniach!"};
		p(k, v);
		k = "__PREF__title__more_map_detail";
		v = new String[] { "en","more Map Detail", "fr","Cartes plus détaillée", "de","mehr Details anzeigen", "hu","Részletesebb térkép", "pl","Więcej szczegółów"};
		p(k, v);
		k = "__PREF__summ__more_map_detail";
		v = new String[] { "en","show more Map Detail [needs RESTART]", "fr","Affiche plus de details sur la carte. NECESSITE UN REDEMARRAGE]", "de","Zeigt noch mehr Details auf der Karte an, braucht aber mehr Leistung! [braucht RESTART]", "pl","pokazuj więcej szczegółów na mapie [wymaga RESTARTU]"};
		p(k, v);
		k = "__PREF__title__mapcache";
		v = new String[] { "en","Mapcache", "zh","地图缓存", "fr","Mémoire cache des cartes.", "de","Mapcache", "hu","Térkép gyorsítótár", "pl","Bufor mapy", "sk","Vyrovnávacia pamäť mapy"};
		p(k, v);
		k = "__PREF__summ__mapcache";
		v = new String[] { "en","Size of Mapcache [needs RESTART]", "zh","地图缓存大小[需要重启]", "fr","Taille de la mémoire cache pour les cartes [NECESSITE UN REDEMARRAGE]", "de","Größe des Caches für Karten [braucht RESTART]", "hu","Gyorsítótár méretének megadása (újraindítás kell!)", "pl","Rozmiar bufora mapy (zmiana wymaga RESTARTU!)", "sk","Veľkosť vyr. pamäte pre mapu [vyžaduje REŠTART]"};
		p(k, v);
		k = "__PREF__title__streetsearch_r";
		v = new String[] { "en","Searchradius", "zh","搜索半径", "fr","Rayon de recherche", "de","Suchradius", "el","Ακτίνα αναζήτησης", "hu","Keresésí sugár", "pl","Promień szukania", "sk","Rádius hľadania"};
		p(k, v);
		k = "__PREF__summ__streetsearch_r";
		v = new String[] { "en","Searchradius for streets inside a town. Bigger radius will find streets further away from town center", "zh","街道的搜索半径。更大的搜索半径将会搜索远离城镇中心的街道。", "fr","Rayon de recherche pour les rues. Plus la valeur sera haute, plus les resultats pourront êtres éloignés du centre ville", "de","Suchradius für Strassen in Städten. Grösserer Radius findet Strassen die weiter vom Stadtzentrum entfernt sind", "el","Ακτίνα αναζήτησης δρόμων μέσα σε πόλη, μεγαλύτερη ακτίνα βρίσκει δρόμους που είναι πιό μακρυά απο το κέντρο της πόλης.", "hu","Nagyobb keresési sugár a városközponttól távolabbi utcákat is megtalál", "pl","Promień szukania ulic w miastach. Większy promień pozwala znaleźć ulice położone dalej od centrum.", "sk","Hľadací rádius pre ulice v rámci mesta. Väčší rádius nájde ulice ďalej od stredu mesta."};
		p(k, v);
		k = "__PREF__title__gui_oneway_arrows";
		v = new String[] { "en","OneWay Arrows", "zh","单向箭头", "cs","Jednosměrky", "nl","eenrichtingspijlen", "fr","Flèches de sens unique", "de","Einbahn", "el","Βέλη μονόδρομων", "hu","Egyirányú utcák", "pl","Strzałki jednokierunkowe", "sk","Šípky jednosmeriek"};
		p(k, v);
		k = "__PREF__title__shrink_on_high_dpi";
		v = new String[] { "en","High DPI", "fr","Ecrans HD", "de","High DPI", "hu","Szükítés nagy felbontásnál"};
		p(k, v);
		k = "__PREF__summ__shrink_on_high_dpi";
		v = new String[] { "en","use 240dpi on high density displays (> 320dpi). needs RESTART!", "fr","Limite la resolution a 240DPI sur les écrans HD (>320DPI)", "de","240 dpi auf Hochauflösenden Displays (> 320dpi) verwenden. braucht RESTART!"};
		p(k, v);
		k = "__PREF__title__streets_only";
		v = new String[] { "en","Streets only", "fr","Rues uniquement", "de","Nur Straßen", "hu","Csak utcák"};
		p(k, v);
		k = "__PREF__summ__streets_only";
		v = new String[] { "en","show only streets on map. map display is faster with this setting", "fr","N'affiche ques les rues sur la cartes. Ce réglage ACCELERE beaucoup l'affichage", "de","auf der Karte nur Straßen anzeigen. keine anderen Objekte. Karte wird dadurch etwas schneller gezeichnet"};
		p(k, v);
		k = "__PREF__summ__gui_oneway_arrows";
		v = new String[] { "en","show oneway street arrows [BETA]", "zh","在街道使用单向箭头[BETA]", "cs","Ukazuj šipky u jednosměrných cest [BETA]", "nl","Toon pijlen bij eenrichtings-straten [BETA]", "fr","Affiche les voies en sens unique [BETA]", "de","Einbahnpfeile [BETA]", "el","εμφάνιση βελών κατεύθυνσης μονόδρομων [BETA]", "hu","Egyirányú utcák jelzése [BETA]", "pl","Pokaż strzałki na ulicach jednokierunkowych [BETA]", "sk","Zobraziť šípky jednosmerných ulíc [BETA]"};
		p(k, v);
		k = "__PREF__title__show_debug_messages";
		v = new String[] { "en","Debug Mgs", "zh","调试信息", "cs","Ladící hlášky", "nl","Toon debug berichten", "fr","Msg. de debug", "de","Debug", "el","Μην. Αποσφαλμάτωσης", "hu","Debug", "pl","Komunikaty debuggera", "sk","Ladiace správy"};
		p(k, v);
		k = "__PREF__summ__show_debug_messages";
		v = new String[] { "en","show Debug Messages [DEBUG]", "zh","显示调试信息[DEBUG]", "cs","Zobrazuj ladící hlášky [DEBUG]", "nl","Toont debug berichten [DEBUG]", "fr","Affiche les messages des debug [DEBUG]", "de","Debugmeldungen [DEBUG]", "el","Εμφάνιση μηνυμάτων αποσφαλμάτωσης", "hu","Hibakövetési jelentések (debug) megjelenítése", "pl","Pokaż komunikaty debuggera", "sk","Zobraziť ladiace správy [DEBUG]"};
		p(k, v);
		k = "__PREF__title__enable_debug_functions";
		v = new String[] { "en","Enable Debug Functions", "zh","启用调试功能", "nl","Debug functies aan", "fr","Activer les fonctions de debug", "de","Debug Funktionen", "el","Ενεργοποίηση Λειτουργιών Αποσφαλμάτωσης", "hu","Debug funkciók engedélyezése", "pl","Włącz funkcje debugujące", "sk","Zapnúť ladiace funkcie"};
		p(k, v);
		k = "__PREF__summ__enable_debug_functions";
		v = new String[] { "en","Enable Debug Functions in Android Menu", "zh","在菜单中显示调试功能", "fr","Active les fonction de debogage dans le menu android", "de","Debug Funktionen im Menü anzeigen", "el","Ενεργοποίηση των λειτουργιών αποσφαλμάτωσης στο μενού του Android", "hu","Engedélyezi a hibanyomkövetési funkciókat", "pl","Włącz funkcje debugujące w menu", "sk","Zapnúť ladiace funkcie v Android ponuke"};
		p(k, v);
		k = "__PREF__title__navit_lang";
		v = new String[] { "en","Language", "zh","语言", "cs","Jazyk", "nl","Language", "fr","Langue", "de","Sprache", "el","Γλώσσα", "hu","Nyelv", "pl","Język", "sk","Jazyk"};
		p(k, v);
		k = "__PREF__summ__navit_lang";
		v = new String[] { "en","Select Language for messages. needs a RESTART!!", "zh","选择界面语言。需要重启！", "cs","Vyber komunikační jazky (pro aplikaci je nutný restart aplikace)", "nl","Stel de taal in. Herstart is dan nodig!", "fr","Sélection de la langue pour les messages, redémarrez l'application pour la prise en compte !!", "de","Sprache der Applikation (Sprache und Meldungen) braucht RESTART!!", "el","Επιλογή Γλώσσας μηνυμάτων. απαιτείται ΕΠΑΝΕΚΚΙΝΗΣΗ!!", "hu","Írott és hangos nyelv kiválasztása; újraindítás szükséges", "pl","Wybierz język komunikatów. Wymaga RESTARTU!", "sk","Vybrať jazyk hlášok. Vyžaduje REŠTART!!"};
		p(k, v);
		k = "__PREF__title__map_directory";
		v = new String[] { "en","Map directory", "zh","地图目录", "fr","Répertoire des cartes", "de","Karten Verzeichnis", "el","Κατάλογος χάρτη", "hu","Térkép könyvtár", "pl","Katalog map", "sk","Adresár pre mapy"};
		p(k, v);
		k = "__PREF__summ__map_directory";
		v = new String[] { "en","activated only after RESTART", "zh","将在重启后应用", "fr","Choix du répertoire ou stocker les cartes, l'appli doit être redémarrée pour être pris en compte", "de","Änderung braucht RESTART", "el","ενεργοποιείται μόνο μετά από ΕΠΑΝΕΚΚΙΝΗΣΗ", "hu","Csak RESTART után él!", "pl","Katalog z mapami (zmiana wymaga RESTARTU!)", "sk","Aktivovaný iba po REŠTARTE"};
		p(k, v);
		k = "__PREF__dialogtitle__map_directory";
		v = new String[] { "en","Map Directory", "zh","地图目录", "fr","Répertoire des cartes", "de","Karten Verzeichnis", "el","Κατάλογος Χάρτη", "hu","Térkép könyvtár", "pl","Katalog map", "sk","Adresár pre mapy"};
		p(k, v);
		k = "__PREF__dialogcancel__map_directory";
		v = new String[] { "en","Cancel", "zh","取消", "fr","Annuler", "de","Abbrechen", "el","Ακύρωση", "hu","Mégse; Elvet", "pl","Anuluj", "sk","Zrušiť"};
		p(k, v);
		k = "__PREF__dialogok__map_directory";
		v = new String[] { "en","Ok", "zh","确定", "fr","Ok", "de","Ok", "el","Εντάξει", "hu","OK", "pl","Ok", "sk","Ok"};
		p(k, v);
		k = "__PREF__dialogmsg__map_directory";
		v = new String[] { "en","Enter map directory", "zh","请输入地图目录", "fr","Entrez le répertoire des cartes", "de","Verzeichnis eingeben", "el","Καθορισμός καταλόγου χάρτη", "hu","Térkép könyvtár megerősítése", "pl","Wpisz ścieżkę do katalogu map", "sk","Zadajte adresár pre mapy"};
		p(k, v);
		k = "delete Destination";
		v = new String[] { "en","delete Destination", "ca","esborra destinació", "ar","حذف المقصد", "pt","apagar destino", "pt_BR","apagar destino", "zh","删除目的地", "zh_HK","刪除目的地", "cs","smazat cíl", "nl","Bestemming verwijderen", "fr","supprimer la destination", "de","Ziel löschen", "el","διαγραφή Προορισμού", "hu","Cél törlése", "it","cancella Destinazione", "pl","usuń Cel", "sk","Zmazať cieľ", "es","eliminar destino"};
		p(k, v);
		k = "rename Destination";
		v = new String[] { "en","rename Destination", "ar","إعادة تسمية المقصد", "pt","renomear destino", "pt_BR","renomear destino", "zh","重命名目的地", "zh_HK","重新命名目的地", "cs","přejmenovat cíl", "nl","Bestemming hernoemen", "fr","renommer la destination", "de","Ziel umbenennen", "el","μετονομασία Προορισμού", "hu","Cél átnevezése", "it","rinomina Destinazione", "pl","zmień nazwę celu", "sk","Premenovať cieľ", "es","renombrar destino"};
		p(k, v);
		k = "set as Home Location";
		v = new String[] { "en","set as Home Location", "nl","als thuislocatie instellen", "fr","définir comme domicile", "de","als Heimadresse setzen", "es","Establecer como ubicación de inicio"};
		p(k, v);
		k = "Rename Destination";
		v = new String[] { "en","Rename Destination", "ar","إعادة تسمية المقصد", "pt","Renomear Destino", "pt_BR","Renomear Destino", "zh","重命名目的地", "zh_HK","重新命名目的地", "cs","Přejmenovat cíl", "nl","Bestemming hernoemen", "fr","Renommer la destination", "de","Ziel umbenennen", "el","Μετονομασία Προορισμού", "hu","Cél átnevezése", "it","Rinomina Destinazione", "pl","Zmień nazwę celu", "sk","Premenovať cieľ", "es","Renombrar destino"};
		p(k, v);
		k = "address search";
		v = new String[] { "en","address search", "fr","recherche d'adresse", "de","Adresssuche", "es","Búsqueda por dirección"};
		p(k, v);
		k = "Enter Destination";
		v = new String[] { "en","Enter Destination", "ar","أدخل الوجهة", "pt","Informar Destino", "pt_BR","Informar Destino", "zh","输入目的地", "zh_HK","輸入目的地", "cs","Zadejte cíl", "nl","voer bestemming in", "fr","Entrez une destination", "de","Zielort eingeben", "el","Εισαγωγή Προορισμού", "hu","Úticél megadása", "it","Inserire Destinazione", "pl","Wprowadź cel", "sk","Zadajťe cieľ", "es","Introducir destino"};
		p(k, v);
		k = "Housenumber";
		v = new String[] { "en","Housenumber", "nl","Huisnummer", "fr","Numéro de rue", "de","Hausnummer", "hu","Házszám", "pl","Numer domu", "es","Portal"};
		p(k, v);
		k = "partial match";
		v = new String[] { "en","partial match", "ar","إنسجام جزئي", "pt","resultado aproximado", "pt_BR","resultado aproximado", "zh","部分匹配", "zh_HK","部份符合", "cs","částečná shoda", "nl","gedeeltelijke overeenkomst", "fr","recherche partielle", "de","ungefähr", "el","μερική αντιστοίχηση", "hu","Részleges egyezés", "it","corrispondemza parziale", "pl","częściowe dopasowanie", "sk","Čiastočný výskyt", "es","coincidencia parcial"};
		p(k, v);
		k = "hide duplicates";
		v = new String[] { "en","hide duplicates", "ca","amaga duplicats", "ar","إخفاء المكرر", "pt","ocultar duplicados", "pt_BR","ocultar duplicados", "zh","隐藏重复项", "zh_HK","隱藏重覆結果", "cs","skrýt duplicity", "nl","duplikaten verbergen", "fr","masquer les doublons", "de","keine doppelten", "el","απόκρυψη διπλών", "hu","Duplikátumok elrejtése", "it","nascondi duplicati", "pl","ukryj duplikaty", "sk","Skryť duplikáty", "es","ocultar duplicados"};
		p(k, v);
		k = "click to activate Index Search";
		v = new String[] { "en","click to activate Index Search", "fr","Cliquer pour activer la recherche indexée", "de","hier drücken um den Suchindex zu aktivieren"};
		p(k, v);
		k = "search full mapfile [BETA]";
		v = new String[] { "en","search full mapfile [BETA]", "ca","cerca en tot el mapa [BETA]", "ar","بحث كل ملف الخريطة", "pt","procurar no arquivo inteiro [BETA]", "pt_BR","procurar no arquivo inteiro [BETA]", "zh","搜索全部地图文件 [BETA]", "zh_HK","搜尋完整地圖檔 [仍在測試]", "cs","Prohledat celý mapový podklad [BETA]", "nl","zoek op volledige kaart [BETA]", "fr","Recherche sur toute la carte [BETA]", "de","ganze Karte durchsuchen [BETA]", "el","αναζήτηση σε όλο το χάρτη [BETA]", "hu","Teljes adatbázis keresése [BETA]", "it","cerca file mappa completo [BETA]", "pl","przeszukaj cały plik map [BETA]", "sk","Prehľadávať celý mapový súbor [BETA]", "es","buscar en todo el mapa [BETA]"};
		p(k, v);
		k = "last searches";
		v = new String[] { "en","last searches", "fr","dernières recherches", "de","letzte Eingaben", "es","últimas búsquedas"};
		p(k, v);
		k = "Streetname";
		v = new String[] { "en","Streetname", "nl","Straatnaam", "fr","Nom de rue", "de","Straße", "hu","Utcanév", "es","Nombre de la calle"};
		p(k, v);
		k = "Town";
		v = new String[] { "en","Town", "nl","Plaats", "fr","Ville", "de","Ort", "hu","Város", "es","Ciudad"};
		p(k, v);
		k = "Address or POI-Name";
		v = new String[] { "en","Address or POI-Name", "fr","Adresse ou point d'interêt", "de","Adresse oder POI-Name", "hu","Cím vagy POI-név", "es","Dirección del nombre del punto de interés"};
		p(k, v);
		k = "Report Bugs";
		v = new String[] { "en","Report Bugs", "de","Fehler melden"};
		p(k, v);
		k = "F-Droid";
		v = new String[] { "en","F-Droid"};
		p(k, v);
		k = "Welcome to ZANavi offline Navigation";
		v = new String[] { "en","Welcome to ZANavi offline Navigation", "de","Willkommen bei ZANavi offline Navigation"};
		p(k, v);
		k = "OpenStreetMap data is available under the Open Database Licence";
		v = new String[] { "en","OpenStreetMap data is available under the Open Database Licence"};
		p(k, v);
		k = "Download";
		v = new String[] { "en","Download"};
		p(k, v);
		k = "Stop map download?";
		v = new String[] { "en","Stop map download?", "nl","Kaart download stoppen?", "fr","Arrêter le téléchargement de la carte?", "de","Kartendownload beenden?", "hu","Térkép letöltés leállítása", "es","¿Parar la descarga del mapa?"};
		p(k, v);
		k = "press HOME to download in the background";
		v = new String[] { "en","press HOME to download in the background", "fr","Appuyer sur HOME pour télécharger en arriere plan", "de","drücke HOME um die Karten im Hintergrund zu laden", "es","pulsar HOME para descargar en segundo plano"};
		p(k, v);
		k = "Enter your Feedback text";
		v = new String[] { "en","Enter your Feedback text", "fr","Saisissez votre commentaire", "de","Feedbacktext eingeben", "hu","Vélemény beírása", "es","Introduzca el texto en Comentarios"};
		p(k, v);
		k = "send";
		v = new String[] { "en","send", "nl","verzend", "fr","envoyer", "de","senden", "hu","Küldés", "es","enviar"};
		p(k, v);
		k = "Do you want to delete this map?";
		v = new String[] { "en","Do you want to delete this map?", "de","Karte löschen?"};
		p(k, v);
		k = "copy Bitcoinaddress to Clipboard";
		v = new String[] { "en","copy Bitcoinaddress to Clipboard", "fr","copier l'adresse Bitcoin dans le presse-papier", "de","Bitcoin Adresse in Zwischenablage kopieren", "es","Copiar la dirección bitcoin en el portapapeles"};
		p(k, v);
		k = "send Bitcoinaddress and QR Code as Email";
		v = new String[] { "en","send Bitcoinaddress and QR Code as Email", "fr","envoyer l'adresse Bitcoin et le code QR par courriel", "de","Bitcoin Adresse und QR Code als Email senden", "es","enviar la dirección Bitcoin y el Código QR por Email"};
		p(k, v);
		k = "Donate with Bitcoin Wallet App";
		v = new String[] { "en","Donate with Bitcoin Wallet App", "fr","Faites un don avec l'appli Bitcoin Wallet", "de","Spende mit Bitcoin Wallet App", "es","Donar con Bitcoin Wallet App"};
		p(k, v);
		k = "ZANavi Donation with Bitcoin";
		v = new String[] { "en","ZANavi Donation with Bitcoin", "fr","Faites un don à ZANavi par Bitcoin", "de","ZANavi Spenden mit Bitcoin", "es","Donar a ZANavi con Bitcoin"};
		p(k, v);
		k = "Bitcoin address";
		v = new String[] { "en","Bitcoin address", "fr","Adresse Bitcoin", "de","Bitcoin Adresse", "es","Dirección Bitcoin"};
		p(k, v);
		k = "generate QR code";
		v = new String[] { "en","generate QR code", "nl","QR code genereren", "fr","générer un code QR", "de","generiere QR Code", "es","Generar un código QR"};
		p(k, v);
		k = "Send Bitcoin address to email ...";
		v = new String[] { "en","Send Bitcoin address to email ...", "fr","Envoyer l'adresse Bitcoin à l'adresse électronique ...", "de","sende Bitcoin Adresse als Email ...", "es","Enviar la dirección Bitcoin por email"};
		p(k, v);
		k = "Bitcoinaddress copied to Clipboard";
		v = new String[] { "en","Bitcoinaddress copied to Clipboard", "fr","Adresse Bitcoin copiée dans le presse-papier", "de","Bitcoin Adresse in Zwischenablage kopiert", "es","La dirección Bitcoin se copió en el portapapeles"};
		p(k, v);
		k = "OK";
		v = new String[] { "en","OK"};
		p(k, v);
		k = "ZANavi has recently crashed, please submit the crashlog";
		v = new String[] { "en","ZANavi has recently crashed, please submit the crashlog", "de","ZANavi ist abgestürzt, bitte Crashlog senden"};
		p(k, v);
		k = "Log may contain private and sensitive data!";
		v = new String[] { "en","Log may contain private and sensitive data!", "de","Log kann sensible Daten enthalten!"};
		p(k, v);
		k = "Grant Permissions";
		v = new String[] { "en","Grant Permissions"};
		p(k, v);
		k = "you have just updated ZANavi";
		v = new String[] { "en","you have just updated ZANavi", "de","ZANavi wurde upgedated"};
		p(k, v);
		k = "select your storage and download a Map for your Area";
		v = new String[] { "en","select your storage and download a Map for your Area", "de","Speicherort wählen und Karte für deine Region laden"};
		p(k, v);
		k = "Index missing, please delete your maps and download them again";
		v = new String[] { "en","Index missing, please delete your maps and download them again", "de","Kartenindex fehlt, bitte Karten löschen und nochmals laden"};
		p(k, v);
		k = "Next";
		v = new String[] { "en","Next", "de","Weiter"};
		p(k, v);
		k = "Permissions";
		v = new String[] { "en","Permissions", "de","Berechtigungen"};
		p(k, v);
		k = "ZANavi needs all the Permissions granted";
		v = new String[] { "en","ZANavi needs all the Permissions granted", "de","ZANavi braucht diese Berechtigungen"};
		p(k, v);
		k = "Submit Log";
		v = new String[] { "en","Submit Log", "de","Log senden"};
		p(k, v);
		k = "reading crash info ...";
		v = new String[] { "en","reading crash info ...", "de","lade Logdaten ..."};
		p(k, v);
		k = "No, thanks";
		v = new String[] { "en","No, thanks", "de","Nein Danke"};
		p(k, v);
		k = "Maps";
		v = new String[] { "en","Maps", "de","Karten"};
		p(k, v);
		k = "Index";
		v = new String[] { "en","Index"};
		p(k, v);
		k = "Crashlog";
		v = new String[] { "en","Crashlog", "de","Logdaten"};
		p(k, v);
		k = "Preparing Storage";
		v = new String[] { "en","Preparing Storage", "de","Speicherort vorbereiten"};
		p(k, v);
		k = "please wait ...";
		v = new String[] { "en","please wait ...", "de","bitte warten ..."};
		p(k, v);
		k = "Language is not available for TTS! Using your phone's default settings";
		v = new String[] { "en","Language is not available for TTS!\nUsing your phone's default settings", "pt","Idioma indisponível para TTS! Usando as configurações padrão de seu telefone", "pt_BR","Idioma indisponível para TTS! Usando as configurações padrão de seu telefone", "zh","TTS语言不可用！请使用您电话上的默认设置", "zh_HK","非TTS支持的語言！請用電話的原本設定", "cs","Není dostupný jazyk pro TTS. Použivám implicitní nastavení telefonu.", "nl","Taal niet beschikbaar voor TTS!\nStandaard instellingen worden gebruikt", "fr","Langue non disponible pour le TTS ! Utilisez les paramètres par défaut de votre téléphone", "de","Diese Sprache nicht für Sprachansage verfügbar!\nVerwende Standardeinstellung.", "el","Η γλώσσα δεν είναι διαθέσιμη για εκφώνηση! Χρήση των προκαθορισμένων ρυθμίσεων του τηλεφώνου", "hu","A hangbemondás e nyelven nem létezik, a telefon alapértelmezését fogjuk használni.", "it","Lingua non disponibile per TTS! Verrà utilizzata l'impostazione standard del vostro telefono", "pl","Język nie jest wspierany przez TTS! Przełączam na ustawienia domyślne telefonu", "sk","Jazyk nie je dostupný pre TTS! Použijú sa predvolené nastavenia Vášho telefónu", "es","El idioma no está disponible para TTS. Se usará la configuración predeterminada de su teléfono"};
		p(k, v);
		k = "Using Voice for:";
		v = new String[] { "en","Using Voice for:", "pt","Usando Voz para:", "pt_BR","Usando Voz para:", "zh","启用语音：", "zh_HK","用語音", "cs","Používám hlas pro:", "nl","Gebruik taal voor:", "fr","Utiliser la voix pour:", "de","Sprache für Ansagen:", "el","Χρήση Γλώσσας για:", "hu","A hangbemondás nyelve:", "it","Utilizzare Voce per:", "pl","Używam głosu dla:", "sk","Použitý hlas:", "es","Usando voz para:"};
		p(k, v);
		k = "Voice Search";
		v = new String[] { "en","Voice Search", "fr","Recherche vocale", "de","Sprachsuche"};
		p(k, v);
		k = "Click to talk";
		v = new String[] { "en","Click to talk", "fr","Appuyer puis parlez", "de","zum sprechen drücken", "hu","Beszélgetéshez katt ide", "es","Pulse aquí para hablar"};
		p(k, v);
		k = "Voice Search, speak your Destination";
		v = new String[] { "en","Voice Search, speak your Destination", "fr","Recherche vocale de la destination", "de","Sprachsuche", "hu","Hang alapú keresés, mondd a célt", "es","Búsqueda por voz, diga su Destino"};
		p(k, v);
		k = "Google Speechrecognition not found";
		v = new String[] { "en","Google Speechrecognition not found", "fr","Reconnaissance automatique de la parole Google non trouvée", "de","Google Text-in-Sprache nicht installiert", "hu","Nem találom a Google beszédfelismeröt!", "es","No se ha encontrado el reconocimiento por voz de google"};
		p(k, v);
		k = "you said:";
		v = new String[] { "en","you said:", "fr","vous avez dit :", "de","Sie sagten:", "hu","Ezt mondtad:", "es","Usted dijo:"};
		p(k, v);
		k = "searching for the location";
		v = new String[] { "en","searching for the location", "fr","recherche de l'emplacement", "de","Ort wird gesucht", "hu","Hely keresése", "es","buscando la ubicación"};
		p(k, v);
		k = "Find Destination";
		v = new String[] { "en","Find Destination", "fr","Rechercher une destination", "de","Ort finden", "hu","Hely keresése", "es","Buscar destino"};
		p(k, v);
		k = "Google did not find this location. Please search again";
		v = new String[] { "en","Google did not find this location. Please search again", "fr","Google n'a pas trouvé cet emplacement.", "de","Dieser Ort wurde von Google nicht gefunden. Bitte versuchen Sie es nochmals", "hu","A Google nem találja azt a helyet. Keress újra!", "es","Google no encontró esta ubicación. Por favor, busque de nuevo"};
		p(k, v);
		k = "What's here";
		v = new String[] { "en","What's here", "fr","Autour de moi", "de","Was ist hier", "el","Τι είναι εδώ", "hu","Mi van itt?", "pl","Co tu jest", "sk","Čo je tu", "es","¿Que hay aquí?"};
		p(k, v);
		k = "More info";
		v = new String[] { "en","More info", "ca","Més informació", "ar","معلومات إضافية", "pt","Mais informações", "pt_BR","Mais informações", "zh","更多信息", "zh_HK","更多資訊", "cs","Více informací", "nl","Meer info", "fr","plus d'infos", "de","Mehr Info", "el","Περισσότερες πληροφορίες", "hu","Több információ", "it","Maggiori informazioni", "pl","Więcej informacji", "sk","Viac info", "es","Más información"};
		p(k, v);
		k = "Map data (c) OpenStreetMap contributors, CC-BY-SA";
		v = new String[] { "en","Map data (c) OpenStreetMap contributors", "ar","بيانات الخريطة محفوظة لـ OpenStreetMap، تحت رخصة CC-BY-SA", "zh","地图数据 (c) OpenStreetMap contributors, CC-BY-SA", "zh_HK","地圖 資料(c) OpenStreetMap contributors, CC-BY-SA", "cs","Mapové podklady (c) OpenStreetMap contributors, CC-BY-SA", "nl","Kaartgegevens (c) OpenStreetMap contributors, CC-BY-SA", "de","Kartendaten (c) OpenStreetMap contributors", "el","Δεδομένα χάρτη (c) OpenStreetMap contributors, CC-BY-SA", "hu","Térképadatok: (c) OpenStreetMap contributors, CC-BY-SA", "it","Dati mappa (c) OpenStreetMap contributors, CC-BY-SA", "pl","Dane map (c) OpenStreetMap contributors, CC-BY-SA", "sk","Mapové údaje (c) OpenStreetMap contributors, CC-BY-SA", "es","Datos de mapas (c) OpenStreetMap contributors, CC-BY-SA"};
		p(k, v);
		k = "__INFO_BOX_TEXT__";
		v = new String[] { "en","You are running ZANavi for the first time!\n\nTo start select \"download maps\"\nfrom the menu, and download a map\nfor your current Area.\nThis will download a large file, so please\nmake sure you have a flatrate or similar!\n\nMap data (c) OpenStreetMap contributors\n\nFor more information\nvisit our Website\nhttp://zanavi.cc\n\n       Have fun using ZANavi.", "pt","Você está executando o ZANavi pela primeira vez!\n\nPara começar, selecione \"baixar mapas\"\nno menu e baixe um mapa\nda sua localidade.\nIsso fará o download de um arquivo grande,\nlogo, recomenda-se taxa de transferência estável!\n\nMapdata:\nCC-BY-SA OpenStreetMap Project\n\nPara mais informações\nvisite nosso website\nhttp://zanavi.cc\n\n Divirta-se com o ZANavi.", "pt_BR","Você está executando o ZANavi pela primeira vez!\n\nPara começar, selecione \"baixar mapas\"\nno menu e baixe um mapa\nda sua localidade.\nIsso fará o download de um arquivo grande,\nlogo, recomenda-se taxa de transferência estável!\n\nMapdata:\nCC-BY-SA OpenStreetMap Project\n\nPara mais informações\nvisite nosso website\nhttp://zanavi.cc\n\n Divirta-se com o ZANavi.", "zh","这是您第一次使用ZANavi！ \n\n首先从菜单中选择“下载地图”，将下载地图文件。\n地图文件可能很大，请确保你的服务商是按时长计费（而不是按流量）或相近方式！\n\n地图数据：CC-BY-SA OpenStreetMap Project\n\n更多信息，请访问我们的网站\nhttp://zanavi.cc\n\n祝您使用愉快。", "zh_HK","這是你首次使用 ZANavi！\n\n一開始先在選單選「下載地圖」\n並為當前地區下載地圖。\n此舉會下載一個大檔案，故請\n確保你的手機收費不會太高！\n\n地圖資料：\nCC-BY-SA OpenStreetMap Project\n\n詳情\n請瀏覽我們的網站\nhttp://zanavi.cc\n\n Have fun using ZANavi.", "cs","ZANavi jste právě spustili poprvé!\n\nPrvně zvolte \"stáhnout mapy\"\nz menu a stáhněte mapu\noblastí, kde se právě nacházíte.\n\nMapové soubory jsou velké,\nproto se ujistěte, že máte datový paušál!\n\nMapové podklady:\nCC-BY-SA OpenStreetMap Project\n\nVíce informací získáte\nna našich stránkách\nhttp://zanavi.cc\n\nPřejme hodně zábavy se ZANavi.", "nl","U voert ZANavi voor de eerste keer uit.\n\nOm te beginnen, selecteer\n\"download kaarten\"\nuit het menu en download een kaart\nvan je regio.\nDe kaarten zijn groot,\nhet is dus aangeraden om een\nongelimiteerde internetverbinding te hebben!\n\nKaartdata:\nCC-BY-SA OpenStreetMap Project\n\nVoor meer info\nbezoek onze site\nhttp://zanavi.cc\n\n       Nog veel plezier met ZANavi.", "fr","Vous exécutez ZANavi pour la première fois\n\nPour commencer, sélectionnez \n\"télécharchez carte\"\ndu menu et télechargez une carte\nde votre région.\nLes cartes sont volumineux, donc\nil est préférable d'avoir une connection\ninternet illimitée!\n\nCartes:\n CC-BY-SA OpenStreetMap Project\n\nPour plus d'infos\nvisitez notre site internet\nhttp://zanavi.cc\n\n       Amusez vous avec ZANavi.", "de","Sie starten ZANavi zum ersten Mal!\n\nZum loslegen im Menu \"Karten laden\"\nauswählen und Karte für die\ngewünschte Region runterladen.\nDie Kartendatei ist sehr gross,\nbitte flatrate oder ähnliches aktivieren!\n\nKartendaten (c) OpenStreetMap contributors\n\nFür mehr Infos\nbitte die Website besuchen\nhttp://zanavi.cc\n\n       Viel Spaß mit ZANavi.", "el","Τρέχετε το ZANavi για πρώτη φορά!\n\nΓια να ξεκινήσετε επιλέξτε \"λήψη χαρτών\"\nαπό το μενού, και κατεβάστε ένα χάρτη για \nτην περιοχή σας.\nΑυτό θα κατεβάσει ένα πολύ μεγάλο αρχείο, \nγιαυτό παρακαλώ υπολογίστε τις χρεώσεις \nτου παροχέα σας internet!\n\nΔεδομένα Χάρτη:\nCC-BY-SA OpenStreetMap Project\n\nΓια περισσότερες πληροφορίες \nεπισκεφτείτε την Ιστοσελίδα μας\nhttp://zanavi.cc\n\nΕυχαριστηθείτε την χρήση του ZANavi.", "hu","Először használja a ZANavi-t!\n\nKezdetnek válassza a \"Térképek letöltése\"\nmenüpontot, és töltsön le egy térképet\na kívánt területről.\nEz egy nagy adatállományt fog letölteni, tehat kérjük\nbizonyosodjon meg, hogy átalánydíjas letöltéses (flatrate) vagy hasonló előfizetése van (különben sokba kerülhet)!\n\nTérképadatok:\n CC-BY-SA OpenStreetMap Project\n\nTovábbi információkért\nlátogassa meg weblapunkat:\nhttp://zanavi.cc\n\nSok örömet kívánunk a ZANavi használatával!", "pl","Używasz ZANavi po raz pierwszy!\n\nNa początku wybierz opcję \"Pobieranie map\"\nz menu i pobierz mapę\ndla twojego obszaru.\nTo spowoduje pobranie dużego pliku, dlatego proszę\nupewnij się czy masz pakiet internetowy lub połączenie WiFi!\n\nMapy:\nCC-BY-SA OpenStreetMap \n\nWięcej informacji\nna naszej stronie\nhttp://zanavi.cc\n\nMiłej zabawy przy ZANavi!", "sk","Používate ZANavi prvýkrát!\n\nNa začatie svoľte \"Stiahnuť mapy\"\nz ponuky, a stiahnite si mapu\npre Vašu aktuálnu oblasť.\nToto stiahne veľký súbor, takže\nsa uistite, že máte neobmedzený alebo podobný internet!\n\nMapové údaje:\nCC-BY-SA OpenStreetMap Project\n\nPre viac informácií\nnavštívte našu webstránku\nhttp://zanavi.cc\n\n       Bavte sa pri používaní ZANavi."};
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
		//Log.e("NavitTextTranslations", "trying: " + key);
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
		// try full lang code (e.g. "pt_BR")
		try
		{
			out = Navit_text_lookup.get(in).get(main_language + "_" + sub_language);
		}
		catch (Exception e)
		{
			// most likely there is no translation yet
			//Log.e("NavitTextTranslations", "lookup: exception 1");
			out = null;
		}

		if (out == null)
		{
			// try only main language (e.g. "pt")
			try
			{
				out = Navit_text_lookup.get(in).get(main_language);
			}
			catch (Exception e)
			{
				// most likely there is no translation yet
				//Log.e("NavitTextTranslations", "lookup: exception 2");
				out = null;
			}
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
