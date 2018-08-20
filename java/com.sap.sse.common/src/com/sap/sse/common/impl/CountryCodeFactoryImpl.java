package com.sap.sse.common.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;

public class CountryCodeFactoryImpl implements CountryCodeFactory {
    private final Map<String, CountryCode> byThreeLetterIOCName;
    private final Map<String, CountryCode> byTwoLetterISOName;
    private final Map<String, CountryCode> byThreeLetterISOName;
    private final Map<String, CountryCode> byIanaInternet;
    private final Map<String, CountryCode> byUNVehicle;
    private final Set<CountryCode> all;

    public CountryCodeFactoryImpl() {
        byThreeLetterIOCName = new HashMap<String, CountryCode>();
        byTwoLetterISOName = new HashMap<String, CountryCode>();
        byThreeLetterISOName = new HashMap<String, CountryCode>();
        byUNVehicle = new HashMap<String, CountryCode>();
        byIanaInternet = new HashMap<String, CountryCode>();
        all = new HashSet<CountryCode>();
        add(new CountryCodeImpl("AFGHANISTAN", "AF", "AFG", "af", "AFG", "AFG", "004", "93"));
        add(new CountryCodeImpl("ÅLAND ISLANDS", "AX", "ALA", "ax", null, null, "248", null));
        add(new CountryCodeImpl("ALBANIA", "AL", "ALB", "al", "AL", "ALB", "008", "355"));
        add(new CountryCodeImpl("ALDERNEY", null, null, null, "GBA", null, null, null));
        add(new CountryCodeImpl("ALGERIA (El Djazaïr)", "DZ", "DZA", "dz", "DZ", "ALG", "012", "213"));
        add(new CountryCodeImpl("AMERICAN SAMOA", "AS", "ASM", "as", null, "ASA", "016", "1-684"));
        add(new CountryCodeImpl("ANDORRA", "AD", "AND", "ad", "AND", "AND", "020", "376"));
        add(new CountryCodeImpl("ANGOLA", "AO", "AGO", "ao", null, "ANG", "024", "244"));
        add(new CountryCodeImpl("ANGUILLA", "AI", "AIA", "ai", null, null, "660", "1-264"));
        add(new CountryCodeImpl("ANTARCTICA", "AQ", "ATA", "aq", null, null, "010", null));
        add(new CountryCodeImpl("ANTIGUA AND BARBUDA", "AG", "ATG", "ag", null, "ANT", "028", "1-268"));
        add(new CountryCodeImpl("ARGENTINA", "AR", "ARG", "ar", "RA", "ARG", "032", "54"));
        add(new CountryCodeImpl("ARMENIA", "AM", "ARM", "am", "AM", "ARM", "051", "7"));
        add(new CountryCodeImpl("ARUBA", "AW", "ABW", "aw", null, "ARU", "533", "297"));
        add(new CountryCodeImpl("ASCENSION ISLAND", null, null, "ac", null, null, null, "247"));
        add(new CountryCodeImpl("AUSTRALIA", "AU", "AUS", "au", "AUS", "AUS", "036", "61"));
        add(new CountryCodeImpl("AUSTRIA", "AT", "AUT", "at", "A", "AUT", "040", "43"));
        add(new CountryCodeImpl("AZERBAIJAN", "AZ", "AZE", "az", "AZ", "AZE", "031", "994"));
        add(new CountryCodeImpl("BAHAMAS", "BS", "BHS", "bs", "BS", "BAH", "044", "1-242"));
        add(new CountryCodeImpl("BAHRAIN", "BH", "BHR", "bh", "BRN", "BRN", "048", "973"));
        add(new CountryCodeImpl("BANGLADESH", "BD", "BGD", "bd", "BD", "BAN", "050", "880"));
        add(new CountryCodeImpl("BARBADOS", "BB", "BRB", "bb", "BDS", "BAR", "052", "1-246"));
        add(new CountryCodeImpl("BELARUS", "BY", "BLR", "by", "BY", "BLR", "112", "375"));
        add(new CountryCodeImpl("BELGIUM", "BE", "BEL", "be", "B", "BEL", "056", "32"));
        add(new CountryCodeImpl("BELIZE", "BZ", "BLZ", "bz", "BH", "BIZ", "084", "501"));
        add(new CountryCodeImpl("BENIN", "BJ", "BEN", "bj", "DY", "BEN", "204", "229"));
        add(new CountryCodeImpl("BERMUDA", "BM", "BMU", "bm", null, "BER", "060", "1-441"));
        add(new CountryCodeImpl("BHUTAN", "BT", "BTN", "bt", null, "BHU", "064", "975"));
        add(new CountryCodeImpl("BOLIVIA", "BO", "BOL", "bo", "BOL", "BOL", "068", "591"));
        add(new CountryCodeImpl("BONAIRE, ST. EUSTATIUS, AND SABA", "BQ", "BES", "bq", null, null, "535", "599"));
        add(new CountryCodeImpl("BOSNIA AND HERZEGOVINA", "BA", "BIH", "ba", "BIH", "BIH", "070", "387"));
        add(new CountryCodeImpl("BOTSWANA", "BW", "BWA", "bw", "BW", "BOT", "072", "267"));
        add(new CountryCodeImpl("BOUVET ISLAND", "BV", "BVT", "bv", null, null, "074", null));
        add(new CountryCodeImpl("BRAZIL", "BR", "BRA", "br", "BR", "BRA", "076", "55"));
        add(new CountryCodeImpl("BRITISH INDIAN OCEAN TERRITORY", "IO", "IOT", "io", null, null, "086", null));
        add(new CountryCodeImpl("BRUNEI DARUSSALAM", "BN", "BRN", "bn", "BRU", "BRU", "096", "673"));
        add(new CountryCodeImpl("BULGARIA", "BG", "BGR", "bg", "BG", "BUL", "100", "359"));
        add(new CountryCodeImpl("BURKINA FASO", "BF", "BFA", "bf", "BF", "BUR", "854", "226"));
        add(new CountryCodeImpl("BURUNDI", "BI", "BDI", "bi", "RU", "BDI", "108", "257"));
        add(new CountryCodeImpl("CAMBODIA", "KH", "KHM", "kh", "K", "CAM", "116", "855"));
        add(new CountryCodeImpl("CAMEROON", "CM", "CMR", "cm", "CAM", "CMR", "120", "237"));
        add(new CountryCodeImpl("CANADA", "CA", "CAN", "ca", "CDN", "CAN", "124", "1"));
        add(new CountryCodeImpl("CAPE VERDE", "CV", "CPV", "cv", null, "CPV", "132", "238"));
        add(new CountryCodeImpl("CAYMAN ISLANDS", "KY", "CYM", "ky", null, "CAY", "136", "1-345"));
        add(new CountryCodeImpl("CENTRAL AFRICAN REPUBLIC", "CF", "CAF", "cf", "RCA", "CAF", "140", "236"));
        add(new CountryCodeImpl("CHAD (Tchad)", "TD", "TCD", "td", "TCH", "CHA", "148", "235"));
        add(new CountryCodeImpl("CHANNEL ISLANDS", null, null, null, null, null, "830", null));
        add(new CountryCodeImpl("CHILE", "CL", "CHL", "cl", "RCH", "CHI", "152", "56"));
        add(new CountryCodeImpl("CHINA", "CN", "CHN", "cn", null, "CHN", "156", "86"));
        add(new CountryCodeImpl("CHRISTMAS ISLAND", "CX", "CXR", "cx", null, null, "162", null));
        add(new CountryCodeImpl("COCOS (KEELING) ISLANDS", "CC", "CCK", "cc", null, null, "166", null));
        add(new CountryCodeImpl("COLOMBIA", "CO", "COL", "co", "CO", "COL", "170", "57"));
        add(new CountryCodeImpl("COMOROS", "KM", "COM", "km", null, "COM", "174", "269"));
        add(new CountryCodeImpl("CONGO, REPUBLIC OF", "CG", "COG", "cg", "RCB", "CGO", "178", "242"));
        add(new CountryCodeImpl("CONGO, THE DEMOCRATIC REPUBLIC OF THE (formerly Zaire)", "CD", "COD", "cd", "ZRE", "COD", "180", "243"));
        add(new CountryCodeImpl("COOK ISLANDS", "CK", "COK", "ck", null, "COK", "184", "682"));
        add(new CountryCodeImpl("COSTA RICA", "CR", "CRI", "cr", "CR", "CRC", "188", "506"));
        add(new CountryCodeImpl("CÔTE D'IVOIRE (Ivory Coast)", "CI", "CIV", "ci", "CI", "CIV", "384", "225"));
        add(new CountryCodeImpl("CROATIA (Hrvatska)", "HR", "HRV", "hr", "HR", "CRO", "191", "385"));
        add(new CountryCodeImpl("CUBA", "CU", "CUB", "cu", "CU", "CUB", "192", "53"));
        add(new CountryCodeImpl("CURAÇAO", "CW", "CUW", "cw", null, null, "531", "599"));
        add(new CountryCodeImpl("CYPRUS", "CY", "CYP", "cy", "CY", "CYP", "196", "357"));
        add(new CountryCodeImpl("CZECH REPUBLIC", "CZ", "CZE", "cz", "CZ", "CZE", "203", "420"));
        add(new CountryCodeImpl("DENMARK", "DK", "DNK", "dk", "DK", "DEN", "208", "45"));
        add(new CountryCodeImpl("DJIBOUTI", "DJ", "DJI", "dj", null, "DJI", "262", "253"));
        add(new CountryCodeImpl("DOMINICA", "DM", "DMA", "dm", "WD", "DMA", "212", "1-767"));
        add(new CountryCodeImpl("DOMINICAN REPUBLIC", "DO", "DOM", "do", "DOM", "DOM", "214", "1-809"));
        add(new CountryCodeImpl("ECUADOR", "EC", "ECU", "ec", "EC", "ECU", "218", "593"));
        add(new CountryCodeImpl("EGYPT", "EG", "EGY", "eg", "ET", "EGY", "818", "20"));
        add(new CountryCodeImpl("EL SALVADOR", "SV", "SLV", "sv", "ES", "ESA", "222", "503"));
        add(new CountryCodeImpl("EQUATORIAL GUINEA", "GQ", "GNQ", "gq", null, "GEQ", "226", "240"));
        add(new CountryCodeImpl("ERITREA", "ER", "ERI", "er", null, "ERI", "232", "291"));
        add(new CountryCodeImpl("ESTONIA", "EE", "EST", "ee", "EST", "EST", "233", "372"));
        add(new CountryCodeImpl("ETHIOPIA", "ET", "ETH", "et", "ETH", "ETH", "231", "251"));
        add(new CountryCodeImpl("EUROPEAN UNION", null, null, "eu", null, null, null, null));
        add(new CountryCodeImpl("FAEROE ISLANDS", "FO", "FRO", "fo", "FR", null, "234", "298"));
        add(new CountryCodeImpl("FALKLAND ISLANDS (MALVINAS)", "FK", "FLK", "fk", null, null, "238", "500"));
        add(new CountryCodeImpl("FIJI", "FJ", "FJI", "fj", "FJI", "FIJ", "242", "679"));
        add(new CountryCodeImpl("FINLAND", "FI", "FIN", "fi", "FIN", "FIN", "246", "358"));
        add(new CountryCodeImpl("FRANCE", "FR", "FRA", "fr", "F", "FRA", "250", "33"));
        add(new CountryCodeImpl("FRENCH GUIANA", "GF", "GUF", "gf", null, null, "254", "594"));
        add(new CountryCodeImpl("FRENCH POLYNESIA", "PF", "PYF", "pf", null, null, "258", "689"));
        add(new CountryCodeImpl("FRENCH SOUTHERN TERRITORIES", "TF", "ATF", "tf", null, null, "260", null));
        add(new CountryCodeImpl("GABON", "GA", "GAB", "ga", "G", "GAB", "266", "241"));
        add(new CountryCodeImpl("GAMBIA, THE", "GM", "GMB", "gm", "WAG", "GAM", "270", "220"));
        add(new CountryCodeImpl("GEORGIA", "GE", "GEO", "ge", "GE", "GEO", "268", null));
        add(new CountryCodeImpl("GERMANY (Deutschland)", "DE", "DEU", "de", "D", "GER", "276", "49"));
        add(new CountryCodeImpl("GHANA", "GH", "GHA", "gh", "GH", "GHA", "288", "233"));
        add(new CountryCodeImpl("GIBRALTAR", "GI", "GIB", "gi", "GBZ", null, "292", "350"));
        add(new CountryCodeImpl("GREAT BRITAIN (United Kingdom)", "GB", "GBR", "uk", "GB", "GBR", "826", "44"));
        add(new CountryCodeImpl("GREECE", "GR", "GRC", "gr", "GR", "GRE", "300", "30"));
        add(new CountryCodeImpl("GREENLAND", "GL", "GRL", "gl", null, null, "304", "299"));
        add(new CountryCodeImpl("GRENADA", "GD", "GRD", "gd", "WG", "GRN", "308", "1-473"));
        add(new CountryCodeImpl("GUADELOUPE", "GP", "GLP", "gp", null, null, "312", "590"));
        add(new CountryCodeImpl("GUAM", "GU", "GUM", "gu", null, "GUM", "316", "1-671"));
        add(new CountryCodeImpl("GUATEMALA", "GT", "GTM", "gt", "GCA", "GUA", "320", "502"));
        add(new CountryCodeImpl("GUERNSEY", "GG", "GGY", "gg", "GBG", null, null, null));
        add(new CountryCodeImpl("GUINEA", "GN", "GIN", "gn", "RG", "GUI", "324", "224"));
        add(new CountryCodeImpl("GUINEA-BISSAU", "GW", "GNB", "gw", null, "GBS", "624", "245"));
        add(new CountryCodeImpl("GUYANA", "GY", "GUY", "gy", "GUY", "GUY", "328", "592"));
        add(new CountryCodeImpl("HAITI", "HT", "HTI", "ht", "RH", "HAI", "332", "509"));
        add(new CountryCodeImpl("HEARD ISLAND AND MCDONALD ISLANDS", "HM", "HMD", "hm", null, null, "334", null));
        add(new CountryCodeImpl("HONDURAS", "HN", "HND", "hn", null, "HON", "340", "504"));
        add(new CountryCodeImpl("HONG KONG (Special Administrative Region of China)", "HK", "HKG", "hk", null, "HKG", "344", "852"));
        add(new CountryCodeImpl("HUNGARY", "HU", "HUN", "hu", "H", "HUN", "348", "36"));
        add(new CountryCodeImpl("ICELAND", "IS", "ISL", "is", "IS", "ISL", "352", "354"));
        add(new CountryCodeImpl("INDIA", "IN", "IND", "in", "IND", "IND", "356", "91"));
        add(new CountryCodeImpl("INDONESIA", "ID", "IDN", "id", "RI", "INA", "360", "62"));
        add(new CountryCodeImpl("INTERNATIONAL ORGANIZATIONS", null, null, "int", null, null, null, null));
        add(new CountryCodeImpl("IRAN (Islamic Republic of Iran)", "IR", "IRN", "ir", "IR", "IRI", "364", "98"));
        add(new CountryCodeImpl("IRAQ", "IQ", "IRQ", "iq", "IRQ", "IRQ", "368", "964"));
        add(new CountryCodeImpl("IRELAND", "IE", "IRL", "ie", "IRL", "IRL", "372", "353"));
        add(new CountryCodeImpl("ISLE OF MAN", "IM", "IMN", "im", "GBM", null, "833", null));
        add(new CountryCodeImpl("ISRAEL", "IL", "ISR", "il", "IL", "ISR", "376", "972"));
        add(new CountryCodeImpl("ITALY", "IT", "ITA", "it", "I", "ITA", "380", "39"));
        add(new CountryCodeImpl("JAMAICA", "JM", "JAM", "jm", "JA", "JAM", "388", "1-876"));
        add(new CountryCodeImpl("JAPAN", "JP", "JPN", "jp", "J", "JPN", "392", "81"));
        add(new CountryCodeImpl("JERSEY", "JE", "JEY", "je", "GBJ", null, null, null));
        add(new CountryCodeImpl("JORDAN (Hashemite Kingdom of Jordan)", "JO", "JOR", "jo", "HKJ", "JOR", "400", "962"));
        add(new CountryCodeImpl("KAZAKHSTAN", "KZ", "KAZ", "kz", "KZ", "KAZ", "398", "7"));
        add(new CountryCodeImpl("KENYA", "KE", "KEN", "ke", "EAK", "KEN", "404", "254"));
        add(new CountryCodeImpl("KIRIBATI", "KI", "KIR", "ki", null, "KIR", "296", "686"));
        add(new CountryCodeImpl("KOREA (Democratic Peoples Republic of [North] Korea)", "KP", "PRK", "kp", null, "PRK", "408", "850"));
        add(new CountryCodeImpl("KOREA (Republic of [South] Korea)", "KR", "KOR", "kr", "ROK", "KOR", "410", "82"));
        add(new CountryCodeImpl("KUWAIT", "KW", "KWT", "kw", "KWT", "KUW", "414", "965"));
        add(new CountryCodeImpl("KYRGYZSTAN", "KG", "KGZ", "kg", "KS", "KGZ", "417", "996"));
        add(new CountryCodeImpl("LAO PEOPLE'S DEMOCRATIC REPUBLIC", "LA", "LAO", "la", "LAO", "LAO", "418", "856"));
        add(new CountryCodeImpl("LATVIA", "LV", "LVA", "lv", "LV", "LAT", "428", "371"));
        add(new CountryCodeImpl("LEBANON", "LB", "LBN", "lb", "RL", "LIB", "422", "961"));
        add(new CountryCodeImpl("LESOTHO", "LS", "LSO", "ls", "LS", "LES", "426", "266"));
        add(new CountryCodeImpl("LIBERIA", "LR", "LBR", "lr", "LB", "LBR", "430", "231"));
        add(new CountryCodeImpl("LIBYA (Libyan Arab Jamahirya)", "LY", "LBY", "ly", "LAR", "LBA", "434", "218"));
        add(new CountryCodeImpl("LIECHTENSTEIN (Fürstentum Liechtenstein)", "LI", "LIE", "li", "FL", "LIE", "438", "423"));
        add(new CountryCodeImpl("LITHUANIA", "LT", "LTU", "lt", "LT", "LTU", "440", "370"));
        add(new CountryCodeImpl("LUXEMBOURG", "LU", "LUX", "lu", "L", "LUX", "442", "352"));
        add(new CountryCodeImpl("MACAO (Special Administrative Region of China)", "MO", "MAC", "mo", null, null, "446", "853"));
        add(new CountryCodeImpl("MACEDONIA (Former Yugoslav Republic of Macedonia)", "MK", "MKD", "mk", "MK", "MKD", "807", "389"));
        add(new CountryCodeImpl("MADAGASCAR", "MG", "MDG", "mg", "RM", "MAD", "450", "261"));
        add(new CountryCodeImpl("MALAWI", "MW", "MWI", "mw", "MW", "MAW", "454", "265"));
        add(new CountryCodeImpl("MALAYSIA", "MY", "MYS", "my", "MAL", "MAS", "458", "60"));
        add(new CountryCodeImpl("MALDIVES", "MV", "MDV", "mv", null, "MDV", "462", "960"));
        add(new CountryCodeImpl("MALI", "ML", "MLI", "ml", "RMM", "MLI", "466", "223"));
        add(new CountryCodeImpl("MALTA", "MT", "MLT", "mt", "M", "MLT", "470", "356"));
        add(new CountryCodeImpl("MARSHALL ISLANDS", "MH", "MHL", "mh", null, "MHL", "584", "692"));
        add(new CountryCodeImpl("MARTINIQUE", "MQ", "MTQ", "mq", null, null, "474", "596"));
        add(new CountryCodeImpl("MAURITANIA", "MR", "MRT", "mr", "RIM", "MTN", "478", "222"));
        add(new CountryCodeImpl("MAURITIUS", "MU", "MUS", "mu", "MS", "MRI", "480", "230"));
        add(new CountryCodeImpl("MAYOTTE", "YT", "MYT", "yt", null, null, "175", "269"));
        add(new CountryCodeImpl("MEXICO", "MX", "MEX", "mx", "MEX", "MEX", "484", "52"));
        add(new CountryCodeImpl("MICRONESIA (Federated States of Micronesia)", "FM", "FSM", "fm", null, "FSM", "583", "691"));
        add(new CountryCodeImpl("MOLDOVA", "MD", "MDA", "md", "MD", "MDA", "498", "373"));
        add(new CountryCodeImpl("MONACO", "MC", "MCO", "mc", "MC", "MON", "492", "377"));
        add(new CountryCodeImpl("MONGOLIA", "MN", "MNG", "mn", "MGL", "MGL", "496", "976"));
        add(new CountryCodeImpl("MONTENEGRO", "ME", "MNE", "me", "MNE", "MNE", "499", "382"));
        add(new CountryCodeImpl("MONTSERRAT", "MS", "MSR", "ms", null, null, "500", "1-664"));
        add(new CountryCodeImpl("MOROCCO", "MA", "MAR", "ma", "MA", "MAR", "504", "212"));
        add(new CountryCodeImpl("MOZAMBIQUE (Moçambique)", "MZ", "MOZ", "mz", "MOC", "MOZ", "508", "258"));
        add(new CountryCodeImpl("MYANMAR (formerly Burma)", "MM", "MMR", "mm", "BUR", "MYA", "104", "95"));
        add(new CountryCodeImpl("NAMIBIA", "NA", "NAM", "na", "NAM", "NAM", "516", "264"));
        add(new CountryCodeImpl("NAURU", "NR", "NRU", "nr", "NAU", "NRU", "520", "674"));
        add(new CountryCodeImpl("NEPAL", "NP", "NPL", "np", "NEP", "NEP", "524", "977"));
        add(new CountryCodeImpl("NETHERLANDS", "NL", "NLD", "nl", "NL", "NED", "528", "31"));
        add(new CountryCodeImpl("NETHERLANDS ANTILLES (obsolete)", "AN", "ANT", "an", "NA", "AHO", "530", "599"));
        add(new CountryCodeImpl("NEW CALEDONIA", "NC", "NCL", "nc", null, null, "540", "687"));
        add(new CountryCodeImpl("NEW ZEALAND", "NZ", "NZL", "nz", "NZ", "NZL", "554", "64"));
        add(new CountryCodeImpl("NICARAGUA", "NI", "NIC", "ni", "NIC", "NCA", "558", "505"));
        add(new CountryCodeImpl("NIGER", "NE", "NER", "ne", "RN", "NIG", "562", "227"));
        add(new CountryCodeImpl("NIGERIA", "NG", "NGA", "ng", "WAN", "NGR", "566", "234"));
        add(new CountryCodeImpl("NIUE", "NU", "NIU", "nu", null, null, "570", "683"));
        add(new CountryCodeImpl("NORFOLK ISLAND", "NF", "NFK", "nf", null, null, "574", null));
        add(new CountryCodeImpl("NORTHERN MARIANA ISLANDS", "MP", "MNP", "mp", null, null, "580", "1-670"));
        add(new CountryCodeImpl("NORWAY", "NO", "NOR", "no", "N", "NOR", "578", "47"));
        add(new CountryCodeImpl("OMAN", "OM", "OMN", "om", null, "OMA", "512", "968"));
        add(new CountryCodeImpl("PAKISTAN", "PK", "PAK", "pk", "PK", "PAK", "586", "92"));
        add(new CountryCodeImpl("PALAU", "PW", "PLW", "pw", null, "PLW", "585", "680"));
        add(new CountryCodeImpl("PALESTINIAN TERRITORIES", "PS", "PSE", "ps", null, "PLE", "275", "970"));
        add(new CountryCodeImpl("PANAMA", "PA", "PAN", "pa", "PA", "PAN", "591", "507"));
        add(new CountryCodeImpl("PAPUA NEW GUINEA", "PG", "PNG", "pg", "PNG", "PNG", "598", "675"));
        add(new CountryCodeImpl("PARAGUAY", "PY", "PRY", "py", "PY", "PAR", "600", "595"));
        add(new CountryCodeImpl("PERU", "PE", "PER", "pe", "PE", "PER", "604", "51"));
        add(new CountryCodeImpl("PHILIPPINES", "PH", "PHL", "ph", "RP", "PHI", "608", "63"));
        add(new CountryCodeImpl("PITCAIRN", "PN", "PCN", "pn", null, null, "612", null));
        add(new CountryCodeImpl("POLAND", "PL", "POL", "pl", "PL", "POL", "616", "48"));
        add(new CountryCodeImpl("PORTUGAL", "PT", "PRT", "pt", "P", "POR", "620", "351"));
        add(new CountryCodeImpl("PUERTO RICO", "PR", "PRI", "pr", null, "PUR", "630", "1"));
        add(new CountryCodeImpl("QATAR", "QA", "QAT", "qa", "Q", "QAT", "634", "974"));
        add(new CountryCodeImpl("RÉUNION", "RE", "REU", "re", null, null, "638", "262"));
        add(new CountryCodeImpl("ROMANIA", "RO", "ROU", "ro", "RO", "ROU", "642", "40"));
        add(new CountryCodeImpl("RUSSIAN FEDERATION", "RU", "RUS", "ru", "RUS", "RUS", "643", "7"));
        add(new CountryCodeImpl("RWANDA", "RW", "RWA", "rw", "RWA", "RWA", "646", "250"));
        add(new CountryCodeImpl("SAINT BARTHÉLEMY", "BL", "BLM", "bl", null, null, "652", null));
        add(new CountryCodeImpl("SAINT HELENA", "SH", "SHN", "sh", null, null, "654", "290"));
        add(new CountryCodeImpl("SAINT KITTS AND NEVIS", "KN", "KNA", "kn", null, "SKN", "659", "1-869"));
        add(new CountryCodeImpl("SAINT LUCIA", "LC", "LCA", "lc", "WL", "LCA", "662", "1-758"));
        add(new CountryCodeImpl("SAINT MARTIN (French portion)", "MF", "MAF", "mf", null, null, "663", null));
        add(new CountryCodeImpl("SAINT PIERRE AND MIQUELON", "PM", "SPM", "pm", null, null, "666", "508"));
        add(new CountryCodeImpl("SAINT VINCENT AND THE GRENADINES", "VC", "VCT", "vc", "WV", "VIN", "670", "1-784"));
        add(new CountryCodeImpl("SAMOA (formerly Western Samoa)", "WS", "WSM", "ws", "WS", "SAM", "882", "685"));
        add(new CountryCodeImpl("SAN MARINO (Republic of)", "SM", "SMR", "sm", "RSM", "SMR", "674", "378"));
        add(new CountryCodeImpl("SAO TOME AND PRINCIPE", "ST", "STP", "st", null, "STP", "678", "239"));
        add(new CountryCodeImpl("SAUDI ARABIA (Kingdom of Saudi Arabia)", "SA", "SAU", "sa", "SA", "KSA", "682", "966"));
        add(new CountryCodeImpl("SENEGAL", "SN", "SEN", "sn", "SN", "SEN", "686", "221"));
        add(new CountryCodeImpl("SERBIA (Republic of Serbia)", "RS", "SRB", "rs", null, "SRB", "688", "381"));
        add(new CountryCodeImpl("SEYCHELLES", "SC", "SYC", "sc", "SY", "SEY", "690", "248"));
        add(new CountryCodeImpl("SIERRA LEONE", "SL", "SLE", "sl", "WAL", "SLE", "694", "232"));
        add(new CountryCodeImpl("SINGAPORE", "SG", "SGP", "sg", "SGP", "SGP", "702", "65"));
        add(new CountryCodeImpl("SINT MAARTEN", "SX", "SXM", "sx", null, null, "534", "599"));
        add(new CountryCodeImpl("SLOVAKIA (Slovak Republic)", "SK", "SVK", "sk", "SK", "SVK", "703", "421"));
        add(new CountryCodeImpl("SLOVENIA", "SI", "SVN", "si", "SLO", "SLO", "705", "386"));
        add(new CountryCodeImpl("SOLOMON ISLANDS", "SB", "SLB", "sb", null, "SOL", "90", "677"));
        add(new CountryCodeImpl("SOMALIA", "SO", "SOM", "so", "SO", "SOM", "706", "252"));
        add(new CountryCodeImpl("SOUTH AFRICA (Zuid Afrika)", "ZA", "ZAF", "za", "ZA", "RSA", "710", "27"));
        add(new CountryCodeImpl("SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS", "GS", "SGS", "gs", null, null, "239", null));
        add(new CountryCodeImpl("SOVIET UNION (Internet code still used)", null, null, "su", null, null, null, null));
        add(new CountryCodeImpl("SPAIN (España)", "ES", "ESP", "es", "E", "ESP", "724", "34"));
        add(new CountryCodeImpl("SRI LANKA (formerly Ceylon)", "LK", "LKA", "lk", "CL", "SRI", "144", "94"));
        add(new CountryCodeImpl("SUDAN", "SD", "SDN", "sd", "SUD", "SUD", "736", "249"));
        add(new CountryCodeImpl("SURINAME", "SR", "SUR", "sr", "SME", "SUR", "740", "597"));
        add(new CountryCodeImpl("SVALBARD AND JAN MAYEN", "SJ", "SJM", "sj", null, null, "744", null));
        add(new CountryCodeImpl("SWAZILAND", "SZ", "SWZ", "sz", "SD", "SWZ", "748", "268"));
        add(new CountryCodeImpl("SWEDEN", "SE", "SWE", "se", "S", "SWE", "752", "46"));
        add(new CountryCodeImpl("SWITZERLAND (Confederation of Helvetia)", "CH", "CHE", "ch", "CH", "SUI", "756", "41"));
        add(new CountryCodeImpl("SYRIAN ARAB REPUBLIC", "SY", "SYR", "sy", "SYR", "SYR", "760", "963"));
        add(new CountryCodeImpl("TAIWAN (\"Chinese Taipei\" for IOC)", "TW", "TWN", "tw", null, "TPE", "158", "886"));
        add(new CountryCodeImpl("TAJIKISTAN", "TJ", "TJK", "tj", "TJ", "TJK", "762", "992"));
        add(new CountryCodeImpl("TANGANYIKA", null, null, null, "EAT", null, null, null));
        add(new CountryCodeImpl("TANZANIA", "TZ", "TZA", "tz", null, "TAN", "834", "255"));
        add(new CountryCodeImpl("THAILAND", "TH", "THA", "th", "T", "THA", "764", "66"));
        add(new CountryCodeImpl("TIMOR-LESTE (formerly East Timor)", "TL", "TLS", "tl", null, "TLS", "626", "670"));
        add(new CountryCodeImpl("TOGO", "TG", "TGO", "tg", "TG", "TOG", "768", "228"));
        add(new CountryCodeImpl("TOKELAU", "TK", "TKL", "tk", null, null, "772", "690"));
        add(new CountryCodeImpl("TONGA", "TO", "TON", "to", null, "TGA", "776", "676"));
        add(new CountryCodeImpl("TRINIDAD AND TOBAGO", "TT", "TTO", "tt", "TT", "TTO", "780", "1-868"));
        add(new CountryCodeImpl("TUNISIA", "TN", "TUN", "tn", "TN", "TUN", "788", "216"));
        add(new CountryCodeImpl("TURKEY", "TR", "TUR", "tr", "TR", "TUR", "792", "90"));
        add(new CountryCodeImpl("TURKMENISTAN", "TM", "TKM", "tm", "TM", "TKM", "795", "993"));
        add(new CountryCodeImpl("TURKS AND CAICOS ISLANDS", "TC", "TCA", "tc", null, null, "796", "1-649"));
        add(new CountryCodeImpl("TUVALU", "TV", "TUV", "tv", null, "TUV", "798", "688"));
        add(new CountryCodeImpl("UGANDA", "UG", "UGA", "ug", "EAU", "UGA", "800", "256"));
        add(new CountryCodeImpl("UKRAINE", "UA", "UKR", "ua", "UA", "UKR", "804", "380"));
        add(new CountryCodeImpl("UNITED ARAB EMIRATES", "AE", "ARE", "ae", null, "UAE", "784", "971"));
        add(new CountryCodeImpl("UNITED KINGDOM", "GB", "GBR", "uk", null, "GBR", "826", "44"));
        add(new CountryCodeImpl("UNITED STATES", "US", "USA", "us", "USA", "USA", "840", "1"));
        add(new CountryCodeImpl("UNITED STATES MINOR OUTLYING ISLANDS", "UM", "UMI", "um", null, null, "581", null));
        add(new CountryCodeImpl("URUGUAY", "UY", "URY", "uy", "ROU", "URU", "858", "598"));
        add(new CountryCodeImpl("UZBEKISTAN", "UZ", "UZB", "uz", "UZ", "UZB", "860", "998"));
        add(new CountryCodeImpl("VANUATU", "VU", "VUT", "vu", null, "VAN", "548", "678"));
        add(new CountryCodeImpl("VATICAN CITY (Holy See)", "VA", "VAT", "va", "V", "VAT", "336", "379"));
        add(new CountryCodeImpl("VENEZUELA", "VE", "VEN", "ve", "YV", "VEN", "862", "58"));
        add(new CountryCodeImpl("VIET NAM", "VN", "VNM", "vn", "VN", "VIE", "704", "84"));
        add(new CountryCodeImpl("VIRGIN ISLANDS, BRITISH", "VG", "VGB", "vg", "BVI", "IVB", "92", "1-284"));
        add(new CountryCodeImpl("VIRGIN ISLANDS, U.S.", "VI", "VIR", "vi", null, "ISV", "850", "1-340"));
        add(new CountryCodeImpl("YUGOSLAVIA (Internet code still used)", null, null, "yu", null, null, null, null));
        add(new CountryCodeImpl("WALLIS AND FUTUNA", "WF", "WLF", "wf", null, null, "876", "681"));
        add(new CountryCodeImpl("WESTERN SAHARA (formerly Spanish Sahara)", "EH", "ESH", "eh", null, null, "732", null));
        add(new CountryCodeImpl("YEMEN (Yemen Arab Republic)", "YE", "YEM", "ye", "YAR", "YEM", "887", "967"));
        add(new CountryCodeImpl("ZAMBIA (formerly Northern Rhodesia)", "ZM", "ZMB", "zm", "RNR", "ZAM", "894", "260"));
        add(new CountryCodeImpl("ZANZIBAR", null, null, null, "EAZ", null, null, null));
        add(new CountryCodeImpl("ZIMBABWE", "ZW", "ZWE", "zw", "ZW", "ZIM", "716", "263"));
    }

    private void add(CountryCode countryCode) {
        if (countryCode.getThreeLetterIOCCode() != null) {
            byThreeLetterIOCName.put(countryCode.getThreeLetterIOCCode().toUpperCase(), countryCode);
        }
        if (countryCode.getTwoLetterISOCode() != null) {
            byTwoLetterISOName.put(countryCode.getTwoLetterISOCode().toUpperCase(), countryCode);
        }
        if (countryCode.getThreeLetterISOCode() != null) {
            byThreeLetterISOName.put(countryCode.getThreeLetterISOCode().toUpperCase(), countryCode);
        }
        if (countryCode.getUNVehicle() != null) {
            byUNVehicle.put(countryCode.getUNVehicle().toUpperCase(), countryCode);
        }
        if (countryCode.getIANAInternet() != null) {
            byIanaInternet.put(countryCode.getIANAInternet().toLowerCase(), countryCode);
        }
        all.add(countryCode);
    }

    @Override
    public CountryCode getFromIANAInternet(String ianaInternet) {
        return byIanaInternet.get(ianaInternet.toLowerCase());
    }

    @Override
    public CountryCode getFromUNVehicle(String unVehicle) {
        return byUNVehicle.get(unVehicle.toUpperCase());
    }

    @Override
    public CountryCode getFromThreeLetterISOName(String threeLetterISOName) {
        return byThreeLetterISOName.get(threeLetterISOName.toUpperCase());
    }

    @Override
    public CountryCode getFromThreeLetterIOCName(String threeLetterIOCName) {
        return byThreeLetterIOCName.get(threeLetterIOCName.toUpperCase());
    }

    @Override
    public CountryCode getFromTwoLetterISOName(String twoLetterISOName) {
        return byTwoLetterISOName.get(twoLetterISOName.toUpperCase());
    }

    @Override
    public Iterable<CountryCode> getAll() {
        return Collections.unmodifiableCollection(all);
    }

}
