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
		k = "Use as destination";
		v = new String[] { "ar", "اختر مقصد", "pt", "Usar como destino", "pt_BR", "Usar como destino", "zh", "设为目的地", "zh_HK", "以其為目的地", "cs", "Použít jako cíl", "nl", "gebruik als bestemming", "fr", "Utiliser comme destination", "de", "als Ziel setzen", "el", "Χρήση ως προορισμός", "hu", "Állítsd be célként", "it", "Usa come destinazione", "pl", "Użyj jako celu", "sk", "Použiť ako cieľ", "es", "Usar como destino" };
		p(k, v);
		k = "back";
		v = new String[] { "ar", "رجوع", "pt", "voltar", "pt_BR", "voltar", "zh", "后退", "zh_HK", "返回", "cs", "zpět", "nl", "terug", "fr", "retour", "de", "zurück", "el", "πίσω", "hu", "vissza", "it", "indietro", "pl", "wstecz", "sk", "Späť", "es", "atrás" };
		p(k, v);
		k = "show destination on map";
		v = new String[] { "ar", "عرض المقصد علي الخريطة", "pt", "mostrar destino no mapa", "pt_BR", "mostrar destino no mapa", "zh", "在地图上显示目的地", "zh_HK", "於地圖顯示目的地", "cs", "zobrazit cíl na mapě", "nl", "toon bestemming op kaart", "fr", "voir la destination sur la carte", "de", "Ziel auf Karte anzeigen", "el", "εμφάνιση προορισμού στο χάρτη", "hu", "Mutasd az úticélt a térképen", "it", "mostra destinazione sulla mappa", "pl", "pokaż cel na mapie", "sk", "Zobraziť cieľ na mape", "es",
				"mostrar destino en el mapa" };
		p(k, v);
		k = "touch map to zoom";
		v = new String[] { "ar", "المس الشاشة للتكبير", "pt", "toque o mapa para ampliar", "pt_BR", "toque o mapa para ampliar", "zh", "点击缩放", "zh_HK", "觸碰地圖放大", "cs", "pro přiblížení se dotkněte mapy", "nl", "raak kaart aan om te zoomen", "fr", "touchez la carte pour zoomer", "de", "auf Karte drücken zum zoomen", "el", "αγγίξτε το χάρτη για εστίαση", "hu", "Érintsd meg a térképet nagyításhoz (zoom)", "it", "tocca la mappa per ingrandire", "pl", "dotknij mapy aby przybliżyć", "sk",
				"Ťuknite na mapu pre priblíženie", "es", "toque el mapa para ampliar" };
		p(k, v);
		k = "Error downloading map!";
		v = new String[] { "ar", "خطأ أثناء تحميل الخريطة!", "pt", "Erro ao baixar o mapa!", "pt_BR", "Erro ao baixar o mapa!", "zh", "下载地图出错！", "zh_HK", "下載地圖時出錯！", "cs", "Chyba při stahování mapy!", "nl", "Fout tijdens downloaden!", "fr", "Erreur de téléchargement de carte!", "de", "Fehler beim Kartendownload!", "el", "Σφάλμα λήψης χάρτη!", "hu", "Hiba térképletöltés közben!", "it", "Errore scaricamento le mappe!", "pl", "Błąd przy pobieraniu map", "sk", "Chyba počas sťahovania mapy", "es",
				"Error al descargar el mapa" };
		p(k, v);
		k = "Mapdownload";
		v = new String[] { "ar", "تحميل الخريطة", "pt", "Baixar mapa", "pt_BR", "Baixar mapa", "zh", "地图下载", "cs", "Stahování mapy", "nl", "kaart download", "fr", "télécharchez carte", "de", "Kartendownload", "el", "Λήψη χάρτη", "hu", "Térképletöltés", "it", "Scarica mappa", "pl", "Pobieranie map", "sk", "Sťahovanie mapy", "es", "Descargar mapa" };
		p(k, v);
		k = "downloading";
		v = new String[] { "ar", "تحميل", "pt", "baixando", "pt_BR", "baixando", "zh", "正在下载", "zh_HK", "下載", "cs", "stahování", "nl", "downloaden", "fr", "téléchargement", "el", "λήψη", "hu", "letöltés", "it", "scaricamento", "pl", "Pobieranie", "sk", "Sťahuje sa", "es", "descargando" };
		p(k, v);
		k = "ETA";
		v = new String[] { "ar", "المدة المتبقية للوصول", "cs", "Zbýv. čas", "nl", "VAT", "de", "fertig in", "el", "ΕΧΑ", "hu", "Becsült hátralévő idő", "pl", "Pozostały czas", "sk", "Zostáva" };
		p(k, v);
		k = "Error downloading map, resuming";
		v = new String[] { "ar", "خطأ أثناء تحميل الخريطة، عودة", "pt", "Erro ao baixar o mapa, continuando", "pt_BR", "Erro ao baixar o mapa, continuando", "zh", "下载地图出错，恢复中", "zh_HK", "下載地圖時出錯，正在重新下載", "cs", "Chyba při stahování mapy, zkouším znovu", "nl", "Fout tijdens downloaden, opnieuw", "fr", "Erreur de téléchargement de carte, raison", "de", "Fehler beim Kartendownload\nDownload wird fortgesetzt ...", "el", "Σφάλμα λήψης χάρτη, συνέχεια", "hu",
				"Hiba térképletöltés közben\nújrafelvétel", "it", "Errore scaricamento mappe, riprendendo", "pl", "Błąd przy pobieraniu map, ponawiam", "sk", "Chyba počas sťahovania mapy, pokračuje sa", "es", "Error descargando mapa, reaunudando" };
		p(k, v);
		k = "Map already up to date";
		v = new String[] { "ar", "الخريطة علي آخر تحديث", "pt", "O mapa já está atualizado", "pt_BR", "O mapa já está atualizado", "zh", "地图已经是最新版本", "zh_HK", "已是最新地圖", "cs", "Mapa je aktuální", "nl", "Kaart is al actueel", "fr", "Carte déjà à jour", "de", "Karte ist auf aktuellem Stand", "el", "Ο χάρτης είναι ενημερωμένος", "hu", "Térkép aktuális!", "it", "Mappa già aggiornata", "pl", "Mapa jest już w najnowszej wersji", "sk", "Mapa je aktuálna", "es", "El mapa ya está actualizado" };
		p(k, v);
		k = "ready";
		v = new String[] { "ar", "جاهز", "pt", "pronto", "pt_BR", "pronto", "zh", "就绪", "zh_HK", "就緒", "cs", "připraveno", "nl", "gereed", "fr", "prêt", "de", "fertig", "el", "έτοιμο", "hu", "kész", "it", "pronto", "pl", "gotowe", "sk", "Pripravený", "es", "listo" };
		p(k, v);
		k = "Creating outputfile, long time";
		v = new String[] { "ar", "إنشاء مخرجات لملف، تأخذ وقت طويل", "pt", "Criando arquivo, pode levar alguns minutos", "pt_BR", "Criando arquivo, pode levar alguns minutos", "zh", "正在创建输出文件，可能耗时较长", "zh_HK", "建立輸出檔，要很長時間", "cs", "Vytvářím výstupní soubor, bude to trvat déle", "nl", "Bestand wordt gemaakt, kan even duren", "fr", "Création du fichier de sortie, très long", "de", "Kartendatei anlegen\nDas kann etwas dauern", "el", "Δημιουργία εξαγόμενου αρχείου, αρκετός χρόνος", "hu",
				"Kimeneti adatállomány előállítása,\nsokáig eltarthat, kérem, várjon!", "it", "Creando outputfile, molto tempo", "pl", "Tworzę plik wyjściowy, długi czas oczekiwania", "sk", "Vytvára sa výstupný súbor" };
		p(k, v);
		k = "MD5 mismatch";
		v = new String[] { "pt", "Falha na integridade do arquivo (MD5 não confere)", "pt_BR", "Falha na integridade do arquivo (MD5 não confere)", "zh", "MD5校验出错", "zh_HK", "MD5 不符", "cs", "Chyba MD5 kontroly", "nl", "MD5 fout", "fr", "MD5 incohérent", "de", "MD5 Prüfsumme stimmt nicht", "el", "αναντιστοιχία MD5", "hu", "MD5 checksum hiba", "it", "MD5 non corrispondente", "pl", "skrót MD5 niepoprawny", "sk", "Nesúhlasí MD5", "es", "MD5 inválido" };
		p(k, v);
		k = "Creating outputfile, wait";
		v = new String[] { "ar", "إنشاء مخرجات لملف، أنتظر", "pt", "Criando arquivo, aguarde", "pt_BR", "Criando arquivo, aguarde", "zh", "正在创建输出文件，请稍等", "zh_HK", "建立輸出檔，請稍候", "cs", "Vytvářím výstupní soubor, čekejte prosím", "nl", "Bestand wordt gemaakt, even geduld", "fr", "Création du fichier de sortie, patientez", "de", "Kartendatei anlegen\nBitte warten", "el", "Δημιουργία εξαγόμενου αρχείου, περιμένετε", "hu", "Kimeneti adatállomány előállítása,\nkérem, várjon!", "it",
				"Creando outputfile, attendere", "pl", "Tworzę plik wyjściowy, czekaj", "sk", "Vytvára sa výstupný súbor, čakajte", "es", "Creando fichero de salida, espere" };
		p(k, v);
		k = "generating MD5 checksum";
		v = new String[] { "pt", "gerando código de integridade (soma MD5)", "pt_BR", "gerando código de integridade (soma MD5)", "zh", "正在生成MD5校验和", "zh_HK", "產生 MD5 checksum", "cs", "Generuji MD5 kontrolní součet", "nl", "MD5sum genereren", "fr", "Génération du checksum MD5", "de", "überprüfe MD5 Prüfsumme", "el", "δημιουργία MD5 checksum", "hu", "MD5 checksum kiszámítása", "it", "creazione checksum MD5", "pl", "generowanie skrótu MD5", "sk", "Generuje sa kontrolný súčet MD5", "es",
				"Generando comprobación MD5" };
		p(k, v);
		k = "__INFO_BOX_TITLE__";
		v = new String[] { "ar", "مرحباً بك في زانافي", "pt", "Bem-vindo ao ZANavi", "pt_BR", "Bem-vindo ao ZANavi", "zh", "欢迎使用ZANavi", "zh_HK", "歡迎使用 ZANavi", "cs", "Vítejte v ZANavi", "nl", "Welkom bij ZANavi", "fr", "Bienvenue chez ZANavi", "de", "Willkommen bei ZANavi", "el", "Καλώς ήλθατε στο ZANavi", "hu", "Üdvözöljük a ZANavi-ban!", "pl", "Witamy w ZaNavi", "sk", "Vitajte v ZANavi" };
		p(k, v);
		k = "Ok";
		v = new String[] { "ar", "تم", "zh", "确定", "zh_HK", "好", "el", "Εντάξει", "hu", "Rendben" };
		p(k, v);
		k = "More info";
		v = new String[] { "ar", "معلومات إضافية", "pt", "Mais informações", "pt_BR", "Mais informações", "zh", "更多信息", "zh_HK", "更多資訊", "cs", "Více informací", "nl", "Meer info", "fr", "plus d'infos", "de", "Mehr Info", "el", "Περισσότερες πληροφορίες", "hu", "Több információ", "it", "Maggiori informazioni", "pl", "Więcej informacji", "sk", "Viac info", "es", "Más información" };
		p(k, v);
		k = "__INFO_BOX_TEXT__";
		v = new String[] { "pt", "Você está executando o ZANavi pela primeira vez!\n\nPara começar, selecione \"baixar mapas\"\nno menu e baixe um mapa\nda sua localidade.\nIsso fará o download de um arquivo grande,\nlogo, recomenda-se taxa de transferência estável!\n\nMapdata:\nCC-BY-SA OpenStreetMap Project\n\nPara mais informações\nvisite nosso website\nhttp://zanavi.cc\n\n Divirta-se com o ZANavi.", "pt_BR",
				"Você está executando o ZANavi pela primeira vez!\n\nPara começar, selecione \"baixar mapas\"\nno menu e baixe um mapa\nda sua localidade.\nIsso fará o download de um arquivo grande,\nlogo, recomenda-se taxa de transferência estável!\n\nMapdata:\nCC-BY-SA OpenStreetMap Project\n\nPara mais informações\nvisite nosso website\nhttp://zanavi.cc\n\n Divirta-se com o ZANavi.", "zh",
				"这是您第一次使用ZANavi！ \n\n首先从菜单中选择“下载地图”，将下载地图文件。\n地图文件可能很大，请确保你的服务商是按时长计费（而不是按流量）或相近方式！\n\n地图数据：CC-BY-SA OpenStreetMap Project\n\n更多信息，请访问我们的网站\nhttp://zanavi.cc\n\n祝您使用愉快。", "zh_HK", "這是你首次使用 ZANavi！\n\n一開始先在選單選「下載地圖」\n並為當前地區下載地圖。\n此舉會下載一個大檔案，故請\n確保你的手機收費不會太高！\n\n地圖資料：\nCC-BY-SA OpenStreetMap Project\n\n詳情\n請瀏覽我們的網站\nhttp://zanavi.cc\n\n Have fun using ZANavi.", "cs",
				"ZANavi jste právě spustili poprvé!\n\nPrvně zvolte \"stáhnout mapy\"\nz menu a stáhněte mapu\noblastí, kde se právě nacházíte.\n\nMapové soubory jsou velké,\nproto se ujistěte, že máte datový paušál!\n\nMapové podklady:\nCC-BY-SA OpenStreetMap Project\n\nVíce informací získáte\nna našich stránkách\nhttp://zanavi.cc\n\nPřejme hodně zábavy se ZANavi.", "nl",
				"U voert ZANavi voor de eerste keer uit.\n\nOm te beginnen, selecteer\n\"download kaarten\"\nuit het menu en download een kaart\nvan je regio.\nDe kaarten zijn groot,\nhet is dus aangeraden om een\nongelimiteerde internetverbinding te hebben!\n\nKaartdata:\nCC-BY-SA OpenStreetMap Project\n\nVoor meer info\nbezoek onze site\nhttp://zanavi.cc\n\n       Nog veel plezier met ZANavi.", "fr",
				"Vous exécutez ZANavi pour la première fois\n\nPour commencer, sélectionnez \n\"télécharchez carte\"\ndu menu et télechargez une carte\nde votre région.\nLes cartes sont volumineux, donc\nil est préférable d'avoir une connection\ninternet illimitée!\n\nCartes:\n CC-BY-SA OpenStreetMap Project\n\nPour plus d'infos\nvisitez notre site internet\nhttp://zanavi.cc\n\n       Amusez vous avec ZANavi.", "de",
				"Sie starten ZANavi zum ersten Mal!\n\nZum loslegen im Menu \"Karten laden\"\nauswählen und Karte für die\ngewünschte Region runterladen.\nDie Kartendatei ist sehr gross,\nbitte flatrate oder ähnliches aktivieren!\n\nKartendaten:\nCC-BY-SA OpenStreetMap Project\n\nFür mehr Infos\nbitte die Website besuchen\nhttp://zanavi.cc\n\n       Viel Spaß mit ZANavi.", "el",
				"Τρέχετε το ZANavi για πρώτη φορά!\n\nΓια να ξεκινήσετε επιλέξτε \"λήψη χαρτών\"\nαπό το μενού, και κατεβάστε ένα χάρτη για \nτην περιοχή σας.\nΑυτό θα κατεβάσει ένα πολύ μεγάλο αρχείο, \nγιαυτό παρακαλώ υπολογίστε τις χρεώσεις \nτου παροχέα σας internet!\n\nΔεδομένα Χάρτη:\nCC-BY-SA OpenStreetMap Project\n\nΓια περισσότερες πληροφορίες \nεπισκεφτείτε την Ιστοσελίδα μας\nhttp://zanavi.cc\n\nΕυχαριστηθείτε την χρήση του ZANavi.", "hu",
				"Először használja a ZANavi-t!\n\nKezdetnek válassza a \"Térképek letöltése\"\nmenüpontot, és töltsön le egy térképet\na kívánt területről.\nEz egy nagy adatállományt fog letölteni, tehat kérjük\nbizonyosodjon meg, hogy átalánydíjas letöltéses (flatrate) vagy hasonló előfizetése van (különben sokba kerülhet)!\n\nTérképadatok:\n CC-BY-SA OpenStreetMap Project\n\nTovábbi információkért\nlátogassa meg weblapunkat:\nhttp://zanavi.cc\n\nSok örömet kívánunk a ZANavi használatával!",
				"pl", "Używasz ZANavi po raz pierwszy!\n\nNa początku wybierz opcję \"Pobieranie map\"\nz menu i pobierz mapę\ndla twojego obszaru.\nTo spowoduje pobranie dużego pliku, dlatego proszę\nupewnij się czy masz pakiet internetowy lub połączenie WiFi!\n\nMapy:\nCC-BY-SA OpenStreetMap \n\nWięcej informacji\nna naszej stronie\nhttp://zanavi.cc\n\nMiłej zabawy przy ZANavi!", "sk",
				"Používate ZANavi prvýkrát!\n\nNa začatie svoľte \"Stiahnuť mapy\"\nz ponuky, a stiahnite si mapu\npre Vašu aktuálnu oblasť.\nToto stiahne veľký súbor, takže\nsa uistite, že máte neobmedzený alebo podobný internet!\n\nMapové údaje:\nCC-BY-SA OpenStreetMap Project\n\nPre viac informácií\nnavštívte našu webstránku\nhttp://zanavi.cc\n\n       Bavte sa pri používaní ZANavi." };
		p(k, v);
		k = "address search (online)";
		v = new String[] { "ar", "بحث عنوان (متصل)", "pt", "procurar endereço (online)", "pt_BR", "procurar endereço (online)", "zh", "搜索地点（联网）", "zh_HK", "搜尋地址 (網上)", "cs", "Hledání (online)", "nl", "adres zoeken (online)", "fr", "recherche (online)", "de", "Adressensuche (online)", "el", "αναζήτηση διεύθυνσης (με σύνδεση)", "hu", "Címkeresés (online)", "it", "cerca indirizzo (online)", "pl", "wyszukaj adres (online)", "sk", "Hľadať adresu (online)", "es", "Buscar dirección (online)" };
		p(k, v);
		k = "address search (offline)";
		v = new String[] { "ar", "بحث عنوان (غير متصل)", "pt", "procurar endereço (offline)", "pt_BR", "procurar endereço (offline)", "zh", "搜索地点（离线）", "zh_HK", "搜尋地址 (網下)", "cs", "Hledání (offline)", "nl", "adres zoeken (offline)", "fr", "recherche (offline)", "de", "Adressensuche (offline)", "el", "αναζήτηση διεύθυνσης (χωρίς σύνδεση)", "hu", "Címkeresés (offline)", "it", "cerca indirizzo (offline)", "pl", "wyszukaj adres (offline)", "sk", "Hľadať adresu (offline)", "es",
				"Buscar dirección (offline)" };
		p(k, v);
		k = "Stop Navigation";
		v = new String[] { "ar", "وقف الملاحة", "pt", "Parar Navegação", "pt_BR", "Parar Navegação", "zh", "停止导航", "zh_HK", "停止導航", "cs", "Přestat navigovat", "nl", "Stop navigatie", "de", "Navigation beenden", "el", "Διακοπή πλοήγησης", "hu", "Navigálás vége", "it", "Interrompi Navigazione", "pl", "Zatrzymaj nawigację", "sk", "Zastaviť navigáciu", "es", "Detener navegación" };
		p(k, v);
		k = "Zoom to Route";
		v = new String[] { "ar", "كبر الطريق", "pt", "Zoom para a rota", "pt_BR", "Zoom para a rota", "zh", "缩小查看线路", "zh_HK", "放大至路線", "cs", "Zobrazit celou trasu", "nl", "Zoom naar route", "fr", "Zoomer sur la route", "de", "ganze Route anzeigen", "el", "Εστίαση στη Διαδρομή", "hu", "Ráközelítés az útvonalra", "it", "Ingrandimento per Route", "pl", "Przybliż do trasy", "sk", "Priblížiť na trasu" };
		p(k, v);
		k = "Target in gmaps";
		v = new String[] { "ar", "الهدف في الخريطة", "pt", "Mostrar no gmaps", "pt_BR", "Mostrar no gmaps", "zh", "在谷歌地图中显示", "nl", "Plaats in gmaps", "de", "zeige in gmaps", "el", "Στόχος στο gmaps", "hu", "Mutasd a célt a GMaps-ben", "pl", "Pokaż w GMaps", "sk", "Cieľ na Google mapách" };
		p(k, v);
		k = "exit navit";
		v = new String[] { "ar", "خروج من زنافي", "pt", "sair", "pt_BR", "sair", "zh", "退出ZANvi", "zh_HK", "結束 ZANavi", "cs", "Ukončit ZANavi", "nl", "ZANavi afsluiten", "fr", "Quittez ZANavi", "de", "ZANavi beenden", "el", "Έξοδος από το ZANavi", "hu", "KIlépés a ZANavi-ból", "it", "esci da ZANavi", "pl", "wyjdź z ZANavi", "sk", "Ukončiť ZANavi", "es", "Salir de ZANavi" };
		p(k, v);
		k = "toggle POI";
		v = new String[] { "pt", "alternar POI", "pt_BR", "alternar POI", "zh", "显示/隐藏兴趣点", "zh_HK", "切換 POI", "cs", "toogle POI", "nl", "POI aan/uit", "fr", "POI on/off", "de", "POI ein/aus", "el", "εναλλαγή POI", "hu", "POI ki/be", "it", "attiva/disattiva POI", "pl", "włącz/wyłącz POI", "sk", "Prepnúť POI" };
		p(k, v);
		k = "Settings";
		v = new String[] { "ar", "إعدادات", "pt", "Configurações", "pt_BR", "Configurações", "zh", "设置", "zh_HK", "設定", "cs", "Nastavení", "nl", "Instellingen", "fr", "Réglages", "de", "Einstellungen", "el", "Ρυθμίσεις", "hu", "Beállitások", "it", "Impostazioni", "pl", "Ustawienia", "sk", "Nastavenia", "es", "Configuración" };
		p(k, v);
		k = "Announcer Off";
		v = new String[] { "ar", "إغلاق الإخباري", "pt", "Locutor desativado", "pt_BR", "Locutor desativado", "zh", "语音播报未启用", "zh_HK", "Announcer 關", "cs", "Vypnout ohlašování", "nl", "spraak uit", "fr", "Voix Off", "de", "Ansagen stumm", "el", "Εκφώνηση Κλειστή", "hu", "Hangbemondás ki", "it", "Annunciatore Spento", "pl", "Wyłącz komunikaty", "sk", "Hlásateľ Vyp" };
		p(k, v);
		k = "Announcer On";
		v = new String[] { "ar", "فتح الإخباري", "pt", "Locutor ativado", "pt_BR", "Locutor ativado", "zh", "语音播报已启用", "zh_HK", "Announcer 開", "cs", "Zapnout ohlašování", "nl", "spraak aan", "fr", "Voix On", "de", "Ansagen einschalten", "el", "Εκφώνηση Ανοικτή", "hu", "Hangbemondás be", "it", "Annunciatore Acceso", "pl", "Włącz komunikaty", "sk", "Hlásateľ Zap" };
		p(k, v);
		k = "Recent destinations";
		v = new String[] { "ar", "آخر الوجهات", "pt", "Destinos recentes", "pt_BR", "Destinos recentes", "zh", "最近的目的地", "zh_HK", "最近的目的地", "cs", "Poslední cíle", "nl", "Recente locaties", "fr", "Destinations récentes", "de", "Letzte Ziele", "el", "Πρόσφατοι προορισμοί", "hu", "Legutóbbi úticélok", "it", "Destinazioni recenti", "pl", "Ostatnie cele", "sk", "Nedávne ciele", "es", "Destinos recientes" };
		p(k, v);
		k = "download maps";
		v = new String[] { "ar", "تحميل خرائط", "pt", "baixar mapas", "pt_BR", "baixar mapas", "zh", "下载地图", "zh_HK", "下載地圖", "cs", "stahování map", "nl", "download kaarten", "fr", "télécharchez carte", "de", "Karten laden", "el", "λήψη χαρτών", "hu", "térképek letöltése", "it", "scarica mappe", "pl", "Pobierz mapy", "sk", "Stiahnuť mapy", "es", "descargar mapas" };
		p(k, v);
		k = "delete maps";
		v = new String[] { "ar", "حذف خرائط", "pt", "apagar mapas", "pt_BR", "apagar mapas", "zh", "删除地图", "zh_HK", "刪除地圖", "cs", "smazat mapy", "nl", "kaarten verwijderen", "fr", "supprimer des cartes", "de", "Karten löschen", "el", "διαγραφή χαρτών", "hu", "térképek törlése", "it", "cancella mappe", "pl", "usuń mapy", "sk", "Zmazať mapy", "es", "eliminar mapas" };
		p(k, v);
		k = "Demo Vehicle";
		v = new String[] { "pt", "Demonstração", "pt_BR", "Demonstração", "zh", "演示汽车", "zh_HK", "Demo 車輛", "nl", "Demo voertuig", "de", "Demo Fahrzeug", "el", "Όχημα Επίδειξης", "hu", "Demo jármű", "pl", "Pojazd demo", "sk", "Predvádzacie vozidlo" };
		p(k, v);
		k = "Speech Texts";
		v = new String[] { "ar", "خاصية قراءة النصوص", "zh", "朗读文本", "zh_HK", "話音文字", "nl", "Gesproken tekst", "de", "Texte für Sprache", "el", "Εκφώνηση Κειμένων", "hu", "Beszéd szövegek", "pl", "Czytaj teksty", "sk", "Rečové texty" };
		p(k, v);
		k = "Nav. Commands";
		v = new String[] { "ar", "أوامر التجول", "pt", "Comandos", "pt_BR", "Comandos", "zh", "导航命令", "zh_HK", "導航指令", "nl", "Nav. Opdrachten", "de", "Nav. Kommandos", "el", "Εντ. Πλοήγησης", "hu", "Navigációs utasítások", "pl", "Komendy nawigacji", "sk", "Nav. povely" };
		p(k, v);
		k = "online Help";
		v = new String[] { "ar", "المساعدة (أونلاين)", "pt", "ajuda online", "pt_BR", "ajuda online", "zh", "在线帮助", "zh_HK", "網上求助", "nl", "online help", "fr", "aide en ligne", "de", "online Hilfe", "hu", "Online súgó", "pl", "Pomoc online", "sk", "Online pomoc" };
		p(k, v);
		k = "No address found";
		v = new String[] { "ar", "لم يتم إيجاد عنوان", "pt", "Endereço não encontrado", "pt_BR", "Endereço não encontrado", "zh", "没有找到任何地点", "zh_HK", "沒有地址", "cs", "Adresa nenalezena", "nl", "Adres niet gevonden", "fr", "Pas d'adresse trouvée", "de", "Keine Adresse gefunden", "el", "Δεν βρέθηκε διεύθυνση", "hu", "Cím nem található", "it", "Nessun indirizzo trovato", "pl", "Nie znaleziono adresu", "sk", "Nenájdená žiadna adresa", "es", "No se encontró ninguna dirección" };
		p(k, v);
		k = "Enter: City and Street";
		v = new String[] { "ar", "أدخل: المدينة و الشارع", "pt", "Informe: Cidade e Rua", "pt_BR", "Informe: Cidade e Rua", "zh", "输入：城市和街道", "zh_HK", "輸入：城市及街道", "cs", "Vložte: město a ulici", "nl", "Invoer: plaatsnaam en straat", "fr", "Ville et Rue", "de", "Stadt und Straße:", "el", "Εισαγωγή: Πόλη και Οδός", "hu", "Város, utca:", "it", "Città e Via", "pl", "Wprowadź: Miasto oraz Ulica", "sk", "Zadjate: Mesto a ulicu", "es", "Introduzca: ciudad y calle" };
		p(k, v);
		k = "No search string entered";
		v = new String[] { "ar", "لا يوجد قيمة للبحث", "pt", "Não há texto a ser procurado", "pt_BR", "Não há texto a ser procurado", "zh", "没有输入任何搜索", "zh_HK", "未輸入搜尋文字", "cs", "Vyhledávací řetězec je prázdný", "nl", "geen waarde opgegeven", "fr", "aucun élément entré", "de", "Keine Eingabe", "el", "Δεν εισήχθηκε αλφαριθμητικό αναζήτησης", "hu", "Keresőmező üres", "it", "Nessuna stringa di ricerca inserita", "pl", "Nie wprowadzono żadnych znaków", "sk", "Nezadaný žiadny výraz" };
		p(k, v);
		k = "setting destination to";
		v = new String[] { "ar", "تحديد الوجهة إلي", "pt", "definindo destino para", "pt_BR", "definindo destino para", "zh", "将目的地设为", "zh_HK", "將目的地設為", "cs", "Nastavuji cíl do", "nl", "bestemming ingesteld op", "fr", "destination vers", "de", "neues Fahrziel:", "el", "καθορισμός προορισμού στο", "hu", "Cél beállítva:", "it", "imposta destinazione a", "pl", "ustawianie celu trasy do", "sk", "Nastavuje sa cieľ na" };
		p(k, v);
		k = "new Waypoint";
		v = new String[] { "ar", "نقطة طريق جديدة", "pt", "novo ponto de partida", "pt_BR", "novo ponto de partida", "zh", "新建路径点", "zh_HK", "新 Waypoint", "cs", "nový waypoint", "nl", "nieuw routepunt", "fr", "nouvelle destination", "de", "neues Zwischenziel", "el", "νέο σημείο", "hu", "Új útpont", "pl", "Nowy punkt", "sk", "nový cestovný bod" };
		p(k, v);
		k = "getting search results";
		v = new String[] { "ar", "عرض نتيجة البحث", "pt", "obtendo resultados da busca", "pt_BR", "obtendo resultados da busca", "zh", "正在获取搜索结果", "zh_HK", "取得搜尋結果", "cs", "Získávám výsledky hledání", "nl", "haalt zoekresultaten op", "fr", "recherche en cours", "de", "lade Suchergebnisse", "el", "λήψη αποτελεσμάτων αναζήτησης", "hu", "Eredmények keresése", "it", "ricevendo risultati ricerca", "pl", "pobieranie wyników wyszukiwania", "sk", "Získava sa výsledok hľadania", "es",
				"obteniendo resultados de la búsqueda" };
		p(k, v);
		k = "searching ...";
		v = new String[] { "ar", "بحث ...", "pt", "procurando...", "pt_BR", "procurando...", "zh", "搜索中.....", "zh_HK", "搜尋 ...", "cs", "hledám....", "nl", "zoekt ...", "fr", "recherche ...", "de", "Suche läuft ...", "el", "αναζήτηση ...", "hu", "keresés ...", "it", "ricerca ...", "pl", "wyszukuję ...", "sk", "Hľadá sa ...", "es", "buscando ..." };
		p(k, v);
		k = "loading search results";
		v = new String[] { "ar", "تحميل نتيجة البحث", "pt", "carregando resultados da busca", "pt_BR", "carregando resultados da busca", "zh", "正在加载搜索结果", "zh_HK", "載入搜尋結果", "cs", "nahrávám výsledky hledání", "nl", "resultaten ophalen", "fr", "chargement des résultats", "de", "lade Suchergebnisse", "el", "φόρτωση αποτελεσμάτων αναζήτησης", "hu", "eredmények betöltése", "it", "caricamento risultati ricerca", "pl", "wczytywanie wyników wyszukiwania", "sk", "Načítavajú sa výsledky hľadania", "es",
				"cargando resultados de búsqueda" };
		p(k, v);
		k = "towns";
		v = new String[] { "ar", "قري", "pt", "cidades", "pt_BR", "cidades", "zh", "城镇", "zh_HK", "市鎮", "cs", "města", "nl", "steden", "fr", "villes", "de", "Städte", "el", "πόλεις", "hu", "városok", "it", "città", "pl", "miasta", "sk", "Mestá", "es", "ciudades" };
		p(k, v);
		k = "Streets";
		v = new String[] { "ar", "الشوارع", "pt", "Ruas", "pt_BR", "Ruas", "zh", "街道", "zh_HK", "街道", "cs", "Ulice", "nl", "Straten", "fr", "Rues", "de", "Straßen", "el", "Οδοί", "hu", "Utcák", "it", "Strade", "pl", "Ulice", "sk", "Ulice", "es", "Calles" };
		p(k, v);
		k = "No Results found!";
		v = new String[] { "ar", "لم يتمكن من إيجاد نتيجة!", "pt", "Nada foi encontrado!", "pt_BR", "Nada foi encontrado!", "zh", "没有找到结果！", "zh_HK", "無結果！", "cs", "Nic nenalzeno", "nl", "Geen resultaat gevonden!", "fr", "Pas de résultat trouvé!", "de", "Suche liefert kein Ergebnis!", "el", "Δεν βρέθηκαν Αποτελέσματα!", "hu", "Nincs találat!", "it", "Nessun risultato trovato!", "pl", "Nic nie znaleziono!", "sk", "Žiadny výsledok nenájdený!", "es", "No se encontraron resultados!" };
		p(k, v);
		k = "Map data (c) OpenStreetMap contributors, CC-BY-SA";
		v = new String[] { "ar", "بيانات الخريطة محفوظة لـ OpenStreetMap، تحت رخصة CC-BY-SA", "zh", "地图数据 (c) OpenStreetMap contributors, CC-BY-SA", "cs", "Mapové podklady (c) OpenStreetMap contributors, CC-BY-SA", "nl", "Kaartgegevens (c) OpenStreetMap contributors, CC-BY-SA", "de", "Kartendaten: (c) OpenStreetMap contributors, CC-BY-SA", "el", "Δεδομένα χάρτη (c) OpenStreetMap contributors, CC-BY-SA", "hu", "Térképadatok: (c) OpenStreetMap contributors, CC-BY-SA", "it",
				"Dati mappa (c) OpenStreetMap contributors, CC-BY-SA", "pl", "Dane map (c) OpenStreetMap contributors, CC-BY-SA", "sk", "Mapové údaje (c) OpenStreetMap contributors, CC-BY-SA", "es", "Datos de mapas (c) OpenStreetMap contributors, CC-BY-SA" };
		p(k, v);
		k = "__PREF__title__use_fast_provider";
		v = new String[] { "pt", "GSM/3G/Sem fio", "pt_BR", "GSM/3G/Sem fio", "zh", "GSM/3g/无线", "cs", "GSM/3g/WiFi", "nl", "GSM/3G/WiFi", "fr", "GSM/3G/Sans-fil", "de", "GSM/3g/Wireless", "el", "GSM/3g/Ασύρματο", "hu", "GSM/3g/Wireless", "pl", "GSM/3g/WiFi", "sk", "GSM/3g/Bezdrôtové" };
		p(k, v);
		k = "__PREF__summ__use_fast_provider";
		v = new String[] { "pt", "Utilizar redes GSM/3G/Sem fio para obter posição (usa a Internet)", "pt_BR", "Utilizar redes GSM/3G/Sem fio para obter posição (usa a Internet)", "zh", "使用GSM/3g/无线网路定位（需要联网）", "cs", "K získání pozice použij síť GSM/3g/WiFi (používá Internet)", "nl", "Gebruik GSM/3G/WiFi om de positie te bepalen (Internet)", "fr", "GSM/3G/Sans-fil pour déterminer la position (utilise internet)", "de", "Position auch mit GSM/3g/Wireless finden (braucht Internet)", "el",
				"Χρήση GSM/3g/Ασύρματων δυκτων για λήψη τοποθεσίας (χρήση Internet)", "hu", "GSM/3g/Wireless (Internet!) használata a pozíciómeghatározáshoz", "pl", "Użyj sieci GSM/3g/WiFi aby ustalić pozycję (wykorzystuje Internet)", "sk", "Použiť GSM/3g/Bezdrôtové siete na získanie pozície (použije Internet)" };
		p(k, v);
		k = "__PREF__title__follow_gps";
		v = new String[] { "pt", "Seguir", "pt_BR", "Seguir", "zh", "跟随GPS", "cs", "Následuj", "nl", "Volg GPS", "fr", "Suivi GPS", "de", "folgen", "el", "Ακολούθησε", "hu", "Követés", "pl", "Podążaj za GPS", "sk", "Nasledovať" };
		p(k, v);
		k = "__PREF__summ__follow_gps";
		v = new String[] { "pt", "Sincronizar com GPS. Ative isso para que a bússola funcione", "pt_BR", "Sincronizar com GPS. Ative isso para que a bússola funcione", "zh", "居中显示GPS。使用罗盘导航需要激活此功能。", "cs", "Centruj mapu podle GPS. Aktivujte tuto volbu, pokud chcete používat směrování kompasem.", "nl", "Volg GPS. Benodigd voor de kompas richting", "fr", "Centré sur la position GPS, Activez cette option pour le suivi du cap", "de", "Karte folgt Fahrzeug", "el",
				"Κεντράρισμα στο GPS. ενεργοποιήστε για τη λειτουργία κατεύθυνσης πυξίδας", "hu", "A térkép követi a helyzetet (sticky)", "pl", "Podążaj za pozycją GPS. Włącz aby działał kompas.", "sk", "Centrovať na GPS. Aktivujte toto pre smerovanie podľa kompasu." };
		p(k, v);
		k = "__PREF__title__show_vehicle_in_center";
		v = new String[] { "pt", "Veículo no centro", "pt_BR", "Veículo no centro", "zh", "居中显示汽车", "cs", "Centruj dle pozice", "nl", "Centreer op voertuig", "fr", "Véhicule au centre", "de", "Fahrzeug", "el", "Όχημα στο κέντρο", "hu", "Pozíció a középpontban", "pl", "Pojazd na środku", "sk", "Vozidlo v strede" };
		p(k, v);
		k = "__PREF__summ__show_vehicle_in_center";
		v = new String[] { "pt", "mostra o veículo no centro da tela ao invés de exibir na parte inferior", "pt_BR", "mostra o veículo no centro da tela ao invés de exibir na parte inferior", "zh", "在屏幕居中显示汽车，而不是屏幕下半部分显示", "cs", "Zobrazuje pozici uprostřed obrazovky místo ve spodní části", "nl", "Toont het voertuig in het midden van het scherm in plaats van onderin.", "fr", "Affiche le véhicule au centre de l'écran, au lieu d'en bas", "de", "Fahrzeug in Displaymitte, statt weiter unten", "el",
				"εμφάνιση του οχήματος στο κέντρο της οθόνης, αντί για το κάτω μισό", "hu", "A pozíció a térkép közepére rögzül", "pl", "Pokaż pojazd na środku ekranu zamiast w dolnej części", "sk", "Zobraziť vozidlo v strede obrazovky, namiesto spodnej polovice" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_base";
		v = new String[] { "pt", "Bússola", "pt_BR", "Bússola", "zh", "罗盘", "cs", "Kompas", "nl", "Kompas richting", "fr", "Cap", "de", "Kompass", "el", "Πυξίδα", "hu", "Iránytű", "pl", "Kompas", "sk", "Kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_base";
		v = new String[] { "pt", "Obter direção da bússola. Exige muito da CPU!", "pt_BR", "Obter direção da bússola. Exige muito da CPU!", "zh", "从罗盘中获取方位。需要较多CPU！", "cs", "Získej směr jízdy podle kompasu. Potřebuje hodně CPU (a baterie).", "nl", "Gebruik kompas voor de richting. Kost veel CPU! De wereldweergave zal afwijken!", "fr", "Prends la direction en suivant le cap. Prends beaucoup de CPU !", "de", "Kompass verwenden. braucht viel CPU !!", "el",
				"Λήψη κατεύθυνσης από την πυξίδα. χρήση αρκετής CPU!", "hu", "Iránytű mutatása; sok CPU-t zabál!", "pl", "Ustal kierunki na podstawie kompasu. Intensywnie wykorzystuje CPU!", "sk", "Získať smer z kompasu. Veľmi zaťaží CPU!" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_always";
		v = new String[] { "pt", "Bússola sempre", "pt_BR", "Bússola sempre", "zh", "总是使用罗盘", "cs", "Vždy kompas", "nl", "Altijd kompas gebruiken", "fr", "Cap permanent", "de", "Kompass immer", "el", "Χρήση πυξίδας πάντα", "hu", "Iránytű menet közben", "pl", "Zawsze użyj kompasu", "sk", "Vždy kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_always";
		v = new String[] { "pt", "Obter posição atual da bússola mesmo a altas velocidades", "pt_BR", "Obter posição atual da bússola mesmo a altas velocidades", "zh", "即使在高速行驶中也使用罗盘获取线路", "cs", "Získávej směr jizdy vždy podle kompasu a to i v případě vyšších rychlostí", "nl", "Gebruikt het kompas voor de rijrichting, ook op hoge snelheid", "fr", "Garde toujours le cap y compris à grande vitesse", "de", "immer Kompassrichtung verwenden", "el",
				"Λήψη τρέχουσας κατεύθυνσης από την πυξίδα ακόμη και σε υψηλότερες ταχύτητες", "hu", "Iránytű használata menet közben is", "pl", "Używa kompasu  nawet przy dużych prędkościach", "sk", "Získať aktuálne smerovanie z kompasu aj vo vysokej rýchlosti" };
		p(k, v);
		k = "__PREF__title__use_compass_heading_fast";
		v = new String[] { "pt", "Bússola rápida", "pt_BR", "Bússola rápida", "zh", "快速罗盘", "cs", "Rychlý kompas", "nl", "Gebruik snel kompas", "fr", "Cap rapide", "de", "Kompass schnell", "el", "Γρήγορη Πυξίδα", "hu", "gyors iránytű", "pl", "Szybki kompas", "sk", "Rýchly kompas" };
		p(k, v);
		k = "__PREF__summ__use_compass_heading_fast";
		v = new String[] { "pt", "Movimentos ficam mais suaves, CUIDADO: Utiliza TODA a CPU!", "pt_BR", "Movimentos ficam mais suaves, CUIDADO: Utiliza TODA a CPU!", "zh", "平滑转弯。警告：可能耗尽CPU！", "cs", "Kompas se točí velmi jemně. POZOR: vezme si veškerý výkon CPU", "nl", "Veel soepeler bij afslagen, WAARSCHUWING: gebruikt heel veel CPU!!!", "de", "Kompass reagiert schnell. braucht noch viel mehr CPU!!", "el", "πιο ήπια περιστροφή, ΠΡΟΕΙΔΟΠΟΙΗΣΗ: ΘΑ ΦΑΕΙ ΟΛΗ ΤΗΝ CPU!!", "hu",
				"Az iránytű gyorsítása, még több CPU-t zabál!", "pl", "Zakręty bardzo płynne. UWAGA! ZJADA CAŁĄ MOC CPU!", "sk", "Oveľa plynulejšie otáčanie, VAROVANIE: ÚPLNE ZAŤAŽÍ CPU!!" };
		p(k, v);
		k = "__PREF__title__use_imperial";
		v = new String[] { "pt", "Medidas Britânicas", "pt_BR", "Medidas Britânicas", "zh", "英制单位", "cs", "Imperial", "nl", "Engelse maten", "fr", "Impérial", "de", "Meilen/Fuss", "el", "Αγγλοσαξονικό", "hu", "Mértékegységek angolszászra", "pl", "Jednostki imperialne", "sk", "Imperiálne jednotky", "es", "Imperial" };
		p(k, v);
		k = "__PREF__summ__use_imperial";
		v = new String[] { "pt", "Usa sistema imperial ao invés do sistema métrico", "pt_BR", "Usa sistema imperial ao invés do sistema métrico", "zh", "使用英制单位，而不是公制单位", "cs", "Používej imperiální jednotky (míle, ...) místo metrických.", "nl", "Gebruik Engelse eenheden in plaats van metrische", "fr", "Utilise les unités impériales au lieu des unités métriques", "de", "Englische Maßeinheiten wie Meilen benutzen", "el", "Χρήση Αγγλικών μονάδων αντί για μετρικές", "hu",
				"Mérföld, láb stb. km, méter helyett", "pl", "Użyj jednostek imperialnych (brytyjskich) zamiast metrycznych", "sk", "Použiť imperiálne jednotky namiesto metrických" };
		p(k, v);
		k = "__PREF__title__show_3d_map";
		v = new String[] { "pt", "3D", "pt_BR", "3D", "zh", "3D", "cs", "3D", "nl", "Kaart in 3D", "fr", "3D", "de", "3D", "el", "3D", "hu", "3D térkép", "pl", "3D", "sk", "3D" };
		p(k, v);
		k = "__PREF__summ__show_3d_map";
		v = new String[] { "pt", "mostra mapa em 3D [BETA]", "pt_BR", "mostra mapa em 3D [BETA]", "zh", "显示3D地图[BETA]", "cs", "Zobrazení mapy v 3D [BETA]", "nl", "3D weergave van de kaart [BETA]", "fr", "Afficher la carte en 3D [BETA]", "de", "Karte in 3D [BETA]", "el", "εμφάνιση χάρτη σε 3D [BETA]", "hu", "3D térkép bekapcsolása 2D helyett [BETA]", "pl", "Pokaż mapę w 3D [BETA]", "sk", "Zobraziť mapu v 3D [BETA]" };
		p(k, v);
		k = "__PREF__title__use_anti_aliasing";
		v = new String[] { "zh", "反走样", "cs", "Vyhlazování", "nl", "Anti Aliasing", "fr", "AntiAlias", "de", "Antialiasing", "el", "Εξομάλυνση", "hu", "Kontúrsimítás", "pl", "Antyaliasing", "sk", "Vyhladzovanie" };
		p(k, v);
		k = "__PREF__summ__use_anti_aliasing";
		v = new String[] { "zh", "反走样绘制地图。关闭此选项，地图将绘制更快。", "cs", "Při vykreslování mapy použij vyhlazování čar. Je rychlejší, pokud je vypnuto.", "nl", "schakel anti aliasing in", "fr", "Afficher la carte avec de l'AntiAlias. L'affichage est plus rapide que cette option est désactivée", "de", "Antialiasing einschalten. Karte zeichnet schneller ohne Antialiasing", "el", "σχεδίαση με εξομάλυνση, ο χάρτης είναι πιο γρήγορος όταν αυτό δεν είναι ενεργοποιημένο", "hu",
				"Kontúrsimítás bekapcsolása; enélkül a térkép gyorsabb.", "pl", "Rysuj z antyaliasingiem, mapa jest szybsza, gdy to jest wyłączone", "sk", "Vykresľovať mapu s vyhladzovaním. Mapa je rýchlejšia, ak je toto vypnuté." };
		p(k, v);
		k = "__PREF__title__gui_oneway_arrows";
		v = new String[] { "zh", "单向箭头", "cs", "Jednosměrky", "nl", "eenrichtingspijlen", "fr", "Flèches de sens unique", "de", "Einbahn", "el", "Βέλη Μονόδρομων", "hu", "Egyirányú utcák", "pl", "Strzałki jednokierunkowe", "sk", "Šípky jednosmeriek" };
		p(k, v);
		k = "__PREF__summ__gui_oneway_arrows";
		v = new String[] { "zh", "在街道使用单向箭头[BETA]", "cs", "Ukazuj šipky u jednosměrných cest [BETA]", "nl", "Toon pijlen bij eenrichtings-straten [BETA]", "fr", "Affiche les voies en sens unique [BETA]", "de", "Einbahnpfeile [BETA]", "el", "εμφάνιση βελών κατεύθυνσης μονόδρομων [BETA]", "hu", "Egyirányú utcák jelzése [BETA]", "pl", "Pokaż strzałki na ulicach jednokierunkowych [BETA]", "sk", "Zobraziť šípky jednosmerných ulíc [BETA]" };
		p(k, v);
		k = "__PREF__title__show_debug_messages";
		v = new String[] { "zh", "调试信息", "cs", "Ladící hlášky", "nl", "Toon debug berichten", "fr", "Msg. de debug", "de", "Debug", "el", "Μην. Αποσφαλμάτωσης", "hu", "Debug", "pl", "Komunikaty debuggera", "sk", "Ladiace správy" };
		p(k, v);
		k = "__PREF__summ__show_debug_messages";
		v = new String[] { "zh", "显示调试信息[DEBUG]", "cs", "Zobrazuj ladící hlášky [DEBUG]", "nl", "Toont debug berichten [DEBUG]", "fr", "Affiche les messages des debug [DEBUG]", "de", "Debugmeldungen [DEBUG]", "el", "Εμφάνιση μηνυμάτων αποσφαλμάτωσης", "hu", "Hibakövetési jelentések (debug) megjelenítése", "pl", "Pokaż komunikaty debuggera", "sk", "Zobraziť ladiace správy [DEBUG]" };
		p(k, v);
		k = "__PREF__title__navit_lang";
		v = new String[] { "zh", "语言", "cs", "Jazyk", "nl", "Language", "fr", "Langue", "de", "Sprache", "el", "Γλώσσα", "hu", "Nyelv", "pl", "Język", "sk", "Jazyk" };
		p(k, v);
		k = "__PREF__summ__navit_lang";
		v = new String[] { "zh", "选择界面语言。需要重启！", "cs", "Vyber komunikační jazky (pro aplikaci je nutný restart aplikace)", "nl", "Stel de taal in. Herstart is dan nodig!", "fr", "Sélection de la langue pour les messages, redémarrez l'application pour la prise en compte !!", "de", "Sprache der Applikation (Sprache und Meldungen) braucht RESTART!!", "el", "Επιλογή Γλώσσας μηνυμάτων. απαιτείται ΕΠΑΝΕΚΚΙΝΗΣΗ!!", "hu", "Írott és hangos nyelv kiválasztása; újraindítás szükséges", "pl",
				"Wybierz język komunikatów. Wymaga RESTARTU!", "sk", "Vybrať jazyk hlášok. Vyžaduje REŠTART!!" };
		p(k, v);
		k = "__PREF__title__use_lock_on_roads";
		v = new String[] { "zh", "锁定道路", "cs", "Jen po silnicích", "nl", "plaats op de weg", "fr", "Bloqué sur les routes", "de", "Fahrzeug auf Strasse", "el", "κλείδωμα στους Δρόμους", "hu", "Maradj az úton", "pl", "Przyciągaj do drogi", "sk", "Prichytiť k ceste" };
		p(k, v);
		k = "__PREF__summ__use_lock_on_roads";
		v = new String[] { "zh", "将汽车锁定到最近的道路。如果您是步行或越野，请关闭此选项。", "cs", "Umístnit pozici vždy na nejbližší komunikaci. Vypněte, pokud se pohybujete mimo značené trasy.", "nl", "Plaatst het voertuig op de dichtsbijzijnde weg. Zet deze uit voor wandelen of offroad rijden", "fr", "Localise exclusivement le véhicule sur les routes les plus proches à désactiver si vous marchez ou roulez hors route", "de", "Fahrzeug auf nächstgelegene Strasse fixieren. Zu Fuß oder Offroad bitte ausschalten", "el",
				"κλείδωμα του οχήματος στον κοντινότερο Δρόμο. απενεργοποιήστε αν περπατάτε ή οδηγείτε εκτός δρόμου", "hu", "A pointert az úton tartja akkor is, ha esetleg a koordináta eltér (autós beállítás, gyalog vagy terepen kikapcsolandó)", "pl", "Przyciągaj pojazd do najbliższej drogi. Wyłącz jeśli idziesz pieszo lub jedziesz w terenie", "sk", "Prichytiť vozidlo k najbližšej ceste. Vypnite ak kráčate alebo jazdíte v teréne." };
		p(k, v);
		k = "__PREF__title__use_route_highways";
		v = new String[] { "zh", "高速公路优先", "cs", "Preferuj dálnice", "nl", "voorkeur voor snelwegen", "fr", "Préférer les routes", "de", "Autobahn bevorzugen", "el", "προτίμηση Αυτοκινητοδρόμων", "hu", "Autópályák", "pl", "Preferuj autostrady", "sk", "Uprednostňovať diaľnice" };
		p(k, v);
		k = "__PREF__summ__use_route_highways";
		v = new String[] { "zh", "优先选择高速公路（关闭此选项会耗用更多内存）。", "cs", "Preferování dálnic. Při vypnuté volbě je potřeba více paměti.", "nl", "geef voorkeur aan snelwegen voor de route (uitschakelen gebruikt meer geheugen!!)", "fr", "Préfère les routes pour le calcul des trajets (Désactiver cette option utilise plus de mémoire !!)", "de", "Autobahnen werden bevorzugt (abschalten braucht viel mehr Speicher!!)", "el",
				"προτίμηση Αυτοκινητοδρόμων για δρομολόγηση (απενεργοποιώντας αυτό, χρησιμοποιεί περισσότερη μνήμη!!)", "hu", "Részesítsd előnyben az autópályák használatát", "pl", "Preferuj autostrady przy nawigacji (wyłączenie zwiększa zużycie pamięci!!)", "sk", "Uprednostniť diaľnice pri navigovaní (vypnutie spôsobí väčšiu spotrebu pamäti!!)" };
		p(k, v);
		k = "__PREF__title__save_zoomlevel";
		v = new String[] { "zh", "缩放级别", "cs", "Úroveň přiblížení", "nl", "zoomniveau", "fr", "Niveau de zoom", "de", "Zoomstufe", "el", "Κλίμακα εστίασης", "hu", "Kivágás (zoom) tárolása", "pl", "Powiększenie", "sk", "Úroveň priblíženia" };
		p(k, v);
		k = "__PREF__summ__save_zoomlevel";
		v = new String[] { "zh", "保存上次缩放级别", "cs", "Ukládat posledně použitou úroveň přiblížení.", "nl", "bewaar laatste zoomniveau", "fr", "Sauvegarder le niveau de zoom précédent", "de", "Zoomstufe speichern", "el", "αποθήκευση τελευταίας Κλίμακας εστίασης", "hu", "Tárold el az utolsó kivágás (zoom) méretet", "pl", "Zapamiętaj powiększenie", "sk", "Zapamätať poslednú úroveň priblíženia" };
		p(k, v);
		k = "__PREF__title__show_sat_status";
		v = new String[] { "zh", "卫星状态", "cs", "Stav statelitů", "nl", "satteliet status", "fr", "Etat du GPS", "de", "Sat Status", "el", "Κατάσταση Δορυφόρων", "hu", "Műholdak", "pl", "Status satelitów", "sk", "Stav satelitov" };
		p(k, v);
		k = "__PREF__summ__show_sat_status";
		v = new String[] { "zh", "显示卫星状态", "cs", "Ukazuj stav GPS satelitů", "nl", "weergeven satteliet status", "fr", "Affiche l'état de la réception satellite", "de", "Satelliten Status anzeigen", "el", "εμφάνιση Κατάστασης Δορυφόρων", "hu", "Mutasd a műholdak állapotát", "pl", "Pokaż status satelitów", "sk", "Zobraziť stav satelitov" };
		p(k, v);
		k = "__PREF__title__use_agps";
		v = new String[] { "zh", "辅助GPS", "cs", "aGPS", "nl", "aGPS", "fr", "aGPS", "de", "aGPS", "el", "aGPS", "hu", "aGPS használata", "pl", "aGPS", "sk", "aGPS" };
		p(k, v);
		k = "__PREF__summ__use_agps";
		v = new String[] { "zh", "使用辅助GPS（需要联网！）", "cs", "Používej asistované GPS (potřebuje Internet)", "nl", "gebruik assisted GPS (gebruikt Internet !!)", "fr", "Utilise le GPS assisté (utilise internet)", "de", "aGPS verwenden für schnellere Positionsfindung (braucht Internet !!)", "el", "χρήση aGPS (με χρήση Internet !!)", "hu", "Assisted GPS használata (internet kapcsolat!)", "pl", "Użyj assisted GPS (wykorzystuje Internet!!)", "sk", "Použiť asistované GPS (použije Internet !!)" };
		p(k, v);
		k = "__PREF__title__enable_debug_functions";
		v = new String[] { "zh", "启用调试功能", "nl", "Debug functies aan", "fr", "Activer les fonctions de debug", "de", "Debug Funktionen", "el", "Ενεργοποίηση Λειτουργιών Αποσφαλμάτωσης", "hu", "Debug funkciók engedélyezése", "pl", "Włącz funkcje debugujące", "sk", "Zapnúť ladiace funkcie" };
		p(k, v);
		k = "__PREF__summ__enable_debug_functions";
		v = new String[] { "zh", "在菜单中显示调试功能", "de", "Debug Funktionen im Menü anzeigen", "el", "Ενεργοποίηση των λειτουργιών αποσφαλμάτωσης στο μενού του Android", "hu", "Engedélyezi a hibanyomkövetési funkciókat", "pl", "Włącz funkcje debugujące w menu", "sk", "Zapnúť ladiace funkcie v Android ponuke" };
		p(k, v);
		k = "__PREF__title__speak_street_names";
		v = new String[] { "zh", "播报街道名字", "nl", "Uitspreken straatnamen", "fr", "Annonce des rues", "de", "Strassennamen", "hu", "Utcanevek bemondása", "pl", "Czytaj nazwy ulic", "sk", "Hovoriť názvy ulíc" };
		p(k, v);
		k = "__PREF__summ__speak_street_names";
		v = new String[] { "zh", "导航时播报街道名字", "nl", "__PREF__summ__Uitspreken_straatnamen", "fr", "Annonce des rues durant la navigation", "de", "Strassennamen beim Navigieren sprechen", "hu", "Bemondja az utcaneveket", "pl", "Czytaj nazwy ulic podczas nawigacji", "sk", "Hovoriť názvy ulíc počas navigácie" };
		p(k, v);
		k = "__PREF__title__map_font_size";
		v = new String[] { "zh", "地图字体大小", "de", "Karten Font Größe", "hu", "Térkép font méret", "pl", "Rozmiar czcionki", "sk", "Veľkosť písma na mape" };
		p(k, v);
		k = "__PREF__summ__map_font_size";
		v = new String[] { "zh", "设置地图的字体大小（例如街道名等）", "de", "Größe der Karten Texte (Straßennamen, usw.)", "hu", "A térkép betűméret beállítása", "pl", "Rozmiar czcionki na mapie (nazwy ulic itp.)", "sk", "Nastaviť veľkosť písma pre text na mape (názvy ulíc, atď.)" };
		p(k, v);
		k = "__PREF__title__use_custom_font";
		v = new String[] { "zh", "自定义字体", "fr", "Polices dédiées", "de", "custom Font", "hu", "Saját font használata", "pl", "Czcionka własna", "sk", "Vlastné písmo" };
		p(k, v);
		k = "__PREF__summ__use_custom_font";
		v = new String[] { "zh", "使用ZANavi内置的字体（否则使用系统字体）", "fr", "Utilise les polices incluses dans ZANavi (sinon les polices du système sont utilisées)", "de", "Font von ZANavi verwenden (sonst Systemschriftart verwenden)", "hu", "Saját font használatának engedélyezése", "pl", "Użyj własnej czcionki ZANavi zamiast systemowej", "sk", "Použiť písmo zahrnuté v ZANavi (inak použiť systémové písmo)" };
		p(k, v);
		k = "__PREF__title__cancel_map_drawing_timeout";
		v = new String[] { "zh", "地图绘制超时时间", "de", "Kartenaufbau Timeout", "hu", "Térképrajzolás időkorlát lejárata", "pl", "Ogr. czasu rysowania mapy", "sk", "Časový limit vykresľovania mapy" };
		p(k, v);
		k = "__PREF__summ__cancel_map_drawing_timeout";
		v = new String[] { "zh", "地图绘制的超时时间。请只在高端设备上启用“长”。", "de", "nur bei schnellen Geräten auf \"long\" setzen", "hu", "Leállítja a térképrajzolás időtúllépését", "pl", "Ograniczenie czasu rysowania mapy (ustaw na \"długi\" tylko na szybkich urządzeniach)", "sk", "Časový limit vykresľovania mapy. Nastavte ho na \"dlhý\" iba na rýchlych zariadeniach!" };
		p(k, v);
		k = "__PREF__title__mapcache";
		v = new String[] { "zh", "地图缓存", "de", "Mapcache", "hu", "Térkép gyorsítótár", "pl", "Bufor mapy", "sk", "Vyrovnávacia pamäť mapy" };
		p(k, v);
		k = "__PREF__summ__mapcache";
		v = new String[] { "zh", "地图缓存大小[需要重启]", "de", "Größe des Caches für Karten [braucht RESTART]", "hu", "Gyorsítótár méretének megadása (újraindítás kell!)", "pl", "Rozmiar bufora mapy (zmiana wymaga RESTARTU!)", "sk", "Veľkosť vyr. pamäte pre mapu [vyžaduje REŠTART]" };
		p(k, v);
		k = "__PREF__title__drawatorder";
		v = new String[] { "zh", "绘制更多细节", "de", "Mehr Detail", "hu", "Több részlet", "pl", "Więcej szczegółów", "sk", "Detailnejšie vykresľovanie" };
		p(k, v);
		k = "__PREF__summ__drawatorder";
		v = new String[] { "zh", "在地图上绘制更多细节。请只在高端设备上启用。", "de", "Zeichnet mehr Details auf der Karte. Nur für schnelle Geräte!", "hu", "Részletesebb térképet rajzol. Csak gyors készülékekre!", "pl", "Więcej szczegółów na mapie. Tylko szybkie urządzenia!", "sk", "Vykresliť viac podrobností na mape. Zmenťe len na rýchlych zariadeniach!" };
		p(k, v);
		k = "__PREF__title__streetsearch_r";
		v = new String[] { "zh", "搜索半径", "de", "Suchradius", "hu", "Keresésí sugár", "pl", "Promień szukania", "sk", "Rádius hľadania" };
		p(k, v);
		k = "__PREF__summ__streetsearch_r";
		v = new String[] { "zh", "街道的搜索半径。更大的搜索半径将会搜索远离城镇中心的街道。", "de", "Suchradius für Strassen in Städten. Grösserer Radius findet Strassen die weiter vom Stadtzentrum entfernt sind", "hu", "Nagyobb keresési sugár a városközponttól távolabbi utcákat is megtalál", "pl", "Promień szukania ulic w miastach. Większy promień pozwala znaleźć ulice położone dalej od centrum.", "sk", "Hľadací rádius pre ulice v rámci mesta. Väčší rádius nájde ulice ďalej od stredu mesta." };
		p(k, v);
		k = "__PREF__title__map_directory";
		v = new String[] { "zh", "地图目录", "fr", "Répertoire des cartes", "de", "Karten Verzeichnis", "el", "Κατάλογος χάρτη", "hu", "Térkép könyvtár", "pl", "Katalog map", "sk", "Adresár pre mapy" };
		p(k, v);
		k = "__PREF__summ__map_directory";
		v = new String[] { "zh", "将在重启后应用", "fr", "Choix du répertoire ou stocker les cartes, l'appli doit être redémarrée pour être pris en compte", "de", "Änderung braucht RESTART", "el", "ενεργοποιείται μόνο μετά από ΕΠΑΝΕΚΚΙΝΗΣΗ", "hu", "Csak RESTART után él!", "pl", "Katalog z mapami (zmiana wymaga RESTARTU!)", "sk", "Aktivovaný iba po REŠTARTE" };
		p(k, v);
		k = "__PREF__dialogtitle__map_directory";
		v = new String[] { "zh", "地图目录", "fr", "Répertoire des cartes", "de", "Karten Verzeichnis", "el", "Κατάλογος Χάρτη", "hu", "Térkép könyvtár", "pl", "Katalog map", "sk", "Adresár pre mapy" };
		p(k, v);
		k = "__PREF__dialogcancel__map_directory";
		v = new String[] { "zh", "取消", "fr", "Annuler", "de", "Abbrechen", "el", "Ακύρωση", "hu", "Mégse; Elvet", "pl", "Anuluj", "sk", "Zrušiť" };
		p(k, v);
		k = "__PREF__dialogok__map_directory";
		v = new String[] { "zh", "确定", "fr", "Ok", "de", "Ok", "el", "Εντάξει", "hu", "OK", "pl", "Ok", "sk", "Ok" };
		p(k, v);
		k = "__PREF__dialogmsg__map_directory";
		v = new String[] { "zh", "请输入地图目录", "fr", "Entrez le répertoire des cartes", "de", "Verzeichnis eingeben", "el", "Καθορισμός καταλόγου χάρτη", "hu", "Térkép könyvtár megerősítése", "pl", "Wpisz ścieżkę do katalogu map", "sk", "Zadajte adresár pre mapy" };
		p(k, v);
		k = "delete Destination";
		v = new String[] { "ar", "حذف المقصد", "pt", "apagar destino", "pt_BR", "apagar destino", "zh", "删除目的地", "zh_HK", "刪除目的地", "cs", "smazat cíl", "nl", "Bestemming verwijderen", "fr", "supprimer la destination", "de", "Ziel löschen", "el", "διαγραφή Προορισμού", "hu", "Cél törlése", "it", "cancella Destinazione", "pl", "usuń Cel", "sk", "Zmazať cieľ", "es", "eliminar destino" };
		p(k, v);
		k = "rename Destination";
		v = new String[] { "ar", "إعادة تسمية المقصد", "pt", "renomear destino", "pt_BR", "renomear destino", "zh", "重命名目的地", "zh_HK", "重新命名目的地", "cs", "přejmenovat cíl", "nl", "Bestemming hernoemen", "fr", "renommer la destination", "de", "Ziel umbenennen", "el", "μετονομασία Προορισμού", "hu", "Cél átnevezése", "it", "rinomina Destinazione", "pl", "zmień nazwę celu", "sk", "Premenovať cieľ", "es", "renombrar destino" };
		p(k, v);
		k = "Rename Destination";
		v = new String[] { "ar", "إعادة تسمية المقصد", "pt", "Renomear Destino", "pt_BR", "Renomear Destino", "zh", "重命名目的地", "zh_HK", "重新命名目的地", "cs", "Přejmenovat cíl", "nl", "Bestemming hernoemen", "fr", "Renommer la destination", "de", "Ziel umbenennen", "el", "Μετονομασία Προορισμού", "hu", "Cél átnevezése", "it", "Rinomina Destinazione", "pl", "Zmień nazwę celu", "sk", "Premenovať cieľ", "es", "Renombrar destino" };
		p(k, v);
		k = "Enter Destination";
		v = new String[] { "ar", "أدخل الوجهة", "pt", "Informar Destino", "pt_BR", "Informar Destino", "zh", "输入目的地", "zh_HK", "輸入目的地", "cs", "Zadejte cíl", "nl", "voer bestemming in", "fr", "Entrez une destination", "de", "Zielort eingeben", "el", "Εισαγωγή Προορισμού", "hu", "Úticél megadása", "it", "Inserire Destinazione", "pl", "Wprowadź cel", "sk", "Zadajťe cieľ", "es", "Introducir destino" };
		p(k, v);
		k = "partial match";
		v = new String[] { "ar", "إنسجام جزئي", "pt", "resultado aproximado", "pt_BR", "resultado aproximado", "zh", "部分匹配", "zh_HK", "部份符合", "cs", "částečná shoda", "nl", "zoek op gedeelte", "fr", "recherche partielle", "de", "ungefähr", "el", "μερική αντιστοίχηση", "hu", "Részleges egyezés", "it", "corrispondemza parziale", "pl", "częściowe dopasowanie", "sk", "Čiastočný výskyt", "es", "coincidencia parcial" };
		p(k, v);
		k = "hide duplicates";
		v = new String[] { "ar", "إخفاء المكرر", "pt", "ocultar duplicados", "pt_BR", "ocultar duplicados", "zh", "隐藏重复项", "zh_HK", "隱藏重覆結果", "cs", "skrýt duplicity", "nl", "dubelle verbergen", "fr", "cacher les doublons", "de", "keine doppelten", "el", "απόκρυψη διπλών", "hu", "Duplikátumok elrejtése", "it", "nascondi duplicati", "pl", "ukryj duplikaty", "sk", "Skryť duplikáty", "es", "ocultar duplicados" };
		p(k, v);
		k = "Search";
		v = new String[] { "ar", "بحث", "pt", "Procurar", "pt_BR", "Procurar", "zh", "搜索", "zh_HK", "搜尋", "cs", "Hledání", "nl", "Zoek", "fr", "Chercher", "de", "suchen", "el", "Αναζήτηση", "hu", "Keresés", "it", "Cerca", "pl", "Wyszukaj", "sk", "Hľadať", "es", "Buscar" };
		p(k, v);
		k = "Language is not available for TTS! Using your phone's default settings";
		v = new String[] { "pt", "Idioma indisponível para TTS! Usando as configurações padrão de seu telefone", "pt_BR", "Idioma indisponível para TTS! Usando as configurações padrão de seu telefone", "zh", "TTS语言不可用！请使用您电话上的默认设置", "cs", "Není dostupný jazyk pro TTS. Použivám implicitní nastavení telefonu.", "nl", "Taal niet beschikbaar voor TTS!\nStandaard instellingen worden gebruikt", "fr", "Langue non disponible pour le TTS ! Utilisez les paramètres par défaut de votre téléphone", "de",
				"Diese Sprache nicht für Sprachansage verfügbar!\nVerwende Standardeinstellung.", "el", "Η γλώσσα δεν είναι διαθέσιμη για εκφώνηση! Χρήση των προκαθορισμένων ρυθμίσεων του τηλεφώνου", "hu", "A hangbemondás e nyelven nem létezik, a telefon alapértelmezését fogjuk használni.", "it", "Lingua non disponibile per TTS! Verrà utilizzata l'impostazione standard del vostro telefono", "pl", "Język nie jest wspierany przez TTS! Przełączam na ustawienia domyślne telefonu", "sk",
				"Jazyk nie je dostupný pre TTS! Použijú sa predvolené nastavenia Vášho telefónu", "es", "El idioma no está disponible para TTS. Se usará la configuración predeterminada de su teléfono" };
		p(k, v);
		k = "Using Voice for:";
		v = new String[] { "pt", "Usando Voz para:", "pt_BR", "Usando Voz para:", "zh", "启用语音：", "cs", "Používám hlas pro:", "nl", "Gebruik taal voor:", "fr", "Utiliser la voix pour:", "de", "Sprache für Ansagen:", "el", "Χρήση Γλώσσας για:", "hu", "A hangbemondás nyelve:", "it", "Utilizzare Voce per:", "pl", "Używam głosu dla:", "sk", "Použitý hlas:", "es", "Usando voz para:" };
		p(k, v);
		k = "drive here";
		v = new String[] { "ar", "قيادة إلي هنا", "pt", "dirija para cá", "pt_BR", "dirija para cá", "zh", "驱车前往此处", "cs", "navigovat sem", "nl", "hierheen rijden", "fr", "conduisez", "de", "Ziel setzen", "el", "οδήγησε εδώ", "hu", "Ide menj!", "it", "guida qui", "pl", "Jedź tutaj", "sk", "Cestovať sem" };
		p(k, v);
		k = "wait ...";
		v = new String[] { "ar", "أنتظر", "pt", "aguarde...", "pt_BR", "aguarde...", "zh", "请稍等.....", "zh_HK", "稍候 ...", "cs", "čekejte...", "nl", "wachten ...", "fr", "patientez ...", "de", "warten ...", "el", "περιμένετε ...", "hu", "Várj...", "it", "attendere ...", "pl", "czekaj ...", "sk", "Čakajte ...", "es", "espere ..." };
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
		// try full lang code (e.g. "pt_BR")
		try
		{
			out = Navit_text_lookup.get(in).get(main_language + "_" + sub_language);
		}
		catch (Exception e)
		{
			// most likely there is not translation yet
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
				// most likely there is not translation yet
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
