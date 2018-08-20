package com.sap.sailing.gwt.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

public class FlagImageResolverImpl implements FlagImageResolver {

    private static final String NONE = "none";

    private final Map<String, ImageResource> flagImagesMap;
	
    private final FlagImageResources flagImageResources = GWT.create(FlagImageResources.class);

    public String[] flagImageNames = { "ad", "ae", "af", "ag", "ai", "al", "am", "an", "ao", "ar", "as", "at",
            "au", "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs",
            "bt", "bv", "bw", "by", "bz", "ca", "catalonia", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm",
            "cn", "co", "cr", "cs", "cu", "cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg",
            "eh", "england", "er", "es", "et", "europeanunion", "fam", "fi", "fj", "fk", "fm", "fo", "fr", "ga", "gb",
            "gd", "ge", "gf", "gh", "gi", "gl", "gm", "gn", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm",
            "hn", "hr", "ht", "hu", "id", "ie", "il", "in", "io", "iq", "ir", "is", "it", "jm", "jo", "jp", "ke", "kg",
            "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu",
            "lv", "ly", "ma", "mc", "md", "me", "mg", "mh", "mk", "ml", "mm", "mn", "mo", "mp", "mq", "mr", "ms", "mt",
            "mu", "mv", "mw", "mx", "my", "mz", "na", "nc", "ne", "nf", "ng", "ni", "nl", "no", NONE, "np", "nr", "nu",
            "nz", "om", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "ps", "pt", "pw", "py", "qa", "re",
            "ro", "rs", "ru", "rw", "sa", "sb", "sc", "scotland", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm",
            "sn", "so", "sr", "st", "sv", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to",
            "tr", "tt", "tv", "tw", "tz", "ua", "ug", "um", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", "vu",
            "wales", "wf", "ws", "ye", "yt", "za", "zm", "zw" };

    {
        flagImagesMap = new HashMap<String, ImageResource>();
        flagImagesMap.put("ad", flagImageResources.flagAD());
        flagImagesMap.put("ae", flagImageResources.flagAE());
        flagImagesMap.put("af", flagImageResources.flagAF());
        flagImagesMap.put("ag", flagImageResources.flagAG());
        flagImagesMap.put("ai", flagImageResources.flagAI());
        flagImagesMap.put("al", flagImageResources.flagAL());
        flagImagesMap.put("am", flagImageResources.flagAM());
        flagImagesMap.put("an", flagImageResources.flagAN());
        flagImagesMap.put("ao", flagImageResources.flagAO());
        flagImagesMap.put("ar", flagImageResources.flagAR());
        flagImagesMap.put("as", flagImageResources.flagAS());
        flagImagesMap.put("at", flagImageResources.flagAT());
        flagImagesMap.put("au", flagImageResources.flagAU());
        flagImagesMap.put("aw", flagImageResources.flagAW());
        flagImagesMap.put("ax", flagImageResources.flagAX());
        flagImagesMap.put("az", flagImageResources.flagAZ());
        flagImagesMap.put("ba", flagImageResources.flagBA());
        flagImagesMap.put("bb", flagImageResources.flagBB());
        flagImagesMap.put("bd", flagImageResources.flagBD());
        flagImagesMap.put("be", flagImageResources.flagBE());
        flagImagesMap.put("bf", flagImageResources.flagBF());
        flagImagesMap.put("bg", flagImageResources.flagBG());
        flagImagesMap.put("bh", flagImageResources.flagBH());
        flagImagesMap.put("bi", flagImageResources.flagBI());
        flagImagesMap.put("bj", flagImageResources.flagBJ());
        flagImagesMap.put("bm", flagImageResources.flagBM());
        flagImagesMap.put("bn", flagImageResources.flagBN());
        flagImagesMap.put("bo", flagImageResources.flagBO());
        flagImagesMap.put("br", flagImageResources.flagBR());
        flagImagesMap.put("bs", flagImageResources.flagBS());
        flagImagesMap.put("bt", flagImageResources.flagBT());
        flagImagesMap.put("bv", flagImageResources.flagBV());
        flagImagesMap.put("bw", flagImageResources.flagBW());
        flagImagesMap.put("by", flagImageResources.flagBY());
        flagImagesMap.put("bz", flagImageResources.flagBZ());
        flagImagesMap.put("ca", flagImageResources.flagCA());
        flagImagesMap.put("catalonia", flagImageResources.flagCATALONIA());
        flagImagesMap.put("cc", flagImageResources.flagCC());
        flagImagesMap.put("cd", flagImageResources.flagCD());
        flagImagesMap.put("cf", flagImageResources.flagCF());
        flagImagesMap.put("cg", flagImageResources.flagCG());
        flagImagesMap.put("ch", flagImageResources.flagCH());
        flagImagesMap.put("ci", flagImageResources.flagCI());
        flagImagesMap.put("ck", flagImageResources.flagCK());
        flagImagesMap.put("cl", flagImageResources.flagCL());
        flagImagesMap.put("cm", flagImageResources.flagCM());
        flagImagesMap.put("cn", flagImageResources.flagCN());
        flagImagesMap.put("co", flagImageResources.flagCO());
        flagImagesMap.put("cr", flagImageResources.flagCR());
        flagImagesMap.put("cs", flagImageResources.flagCS());
        flagImagesMap.put("cu", flagImageResources.flagCU());
        flagImagesMap.put("cv", flagImageResources.flagCV());
        flagImagesMap.put("cx", flagImageResources.flagCX());
        flagImagesMap.put("cy", flagImageResources.flagCY());
        flagImagesMap.put("cz", flagImageResources.flagCZ());
        flagImagesMap.put("de", flagImageResources.flagDE());
        flagImagesMap.put("dj", flagImageResources.flagDJ());
        flagImagesMap.put("dk", flagImageResources.flagDK());
        flagImagesMap.put("dm", flagImageResources.flagDM());
        flagImagesMap.put("do", flagImageResources.flagDO());
        flagImagesMap.put("dz", flagImageResources.flagDZ());
        flagImagesMap.put("ec", flagImageResources.flagEC());
        flagImagesMap.put("ee", flagImageResources.flagEE());
        flagImagesMap.put("eg", flagImageResources.flagEG());
        flagImagesMap.put("eh", flagImageResources.flagEH());
        flagImagesMap.put("england", flagImageResources.flagENGLAND());
        flagImagesMap.put("er", flagImageResources.flagER());
        flagImagesMap.put("es", flagImageResources.flagES());
        flagImagesMap.put("et", flagImageResources.flagET());
        flagImagesMap.put("europeanunion", flagImageResources.flagEUROPEANUNION());
        flagImagesMap.put("fam", flagImageResources.flagFAM());
        flagImagesMap.put("fi", flagImageResources.flagFI());
        flagImagesMap.put("fj", flagImageResources.flagFJ());
        flagImagesMap.put("fk", flagImageResources.flagFK());
        flagImagesMap.put("fm", flagImageResources.flagFM());
        flagImagesMap.put("fo", flagImageResources.flagFO());
        flagImagesMap.put("fr", flagImageResources.flagFR());
        flagImagesMap.put("ga", flagImageResources.flagGA());
        flagImagesMap.put("gb", flagImageResources.flagGB());
        flagImagesMap.put("gd", flagImageResources.flagGD());
        flagImagesMap.put("ge", flagImageResources.flagGE());
        flagImagesMap.put("gf", flagImageResources.flagGF());
        flagImagesMap.put("gh", flagImageResources.flagGH());
        flagImagesMap.put("gi", flagImageResources.flagGI());
        flagImagesMap.put("gl", flagImageResources.flagGL());
        flagImagesMap.put("gm", flagImageResources.flagGM());
        flagImagesMap.put("gn", flagImageResources.flagGN());
        flagImagesMap.put("gp", flagImageResources.flagGP());
        flagImagesMap.put("gq", flagImageResources.flagGQ());
        flagImagesMap.put("gr", flagImageResources.flagGR());
        flagImagesMap.put("gs", flagImageResources.flagGS());
        flagImagesMap.put("gt", flagImageResources.flagGT());
        flagImagesMap.put("gu", flagImageResources.flagGU());
        flagImagesMap.put("gw", flagImageResources.flagGW());
        flagImagesMap.put("gy", flagImageResources.flagGY());
        flagImagesMap.put("hk", flagImageResources.flagHK());
        flagImagesMap.put("hm", flagImageResources.flagHM());
        flagImagesMap.put("hn", flagImageResources.flagHN());
        flagImagesMap.put("hr", flagImageResources.flagHR());
        flagImagesMap.put("ht", flagImageResources.flagHT());
        flagImagesMap.put("hu", flagImageResources.flagHU());
        flagImagesMap.put("id", flagImageResources.flagID());
        flagImagesMap.put("ie", flagImageResources.flagIE());
        flagImagesMap.put("il", flagImageResources.flagIL());
        flagImagesMap.put("in", flagImageResources.flagIN());
        flagImagesMap.put("io", flagImageResources.flagIO());
        flagImagesMap.put("iq", flagImageResources.flagIQ());
        flagImagesMap.put("ir", flagImageResources.flagIR());
        flagImagesMap.put("is", flagImageResources.flagIS());
        flagImagesMap.put("it", flagImageResources.flagIT());
        flagImagesMap.put("jm", flagImageResources.flagJM());
        flagImagesMap.put("jo", flagImageResources.flagJO());
        flagImagesMap.put("jp", flagImageResources.flagJP());
        flagImagesMap.put("ke", flagImageResources.flagKE());
        flagImagesMap.put("kg", flagImageResources.flagKG());
        flagImagesMap.put("kh", flagImageResources.flagKH());
        flagImagesMap.put("ki", flagImageResources.flagKI());
        flagImagesMap.put("km", flagImageResources.flagKM());
        flagImagesMap.put("kn", flagImageResources.flagKN());
        flagImagesMap.put("kp", flagImageResources.flagKP());
        flagImagesMap.put("kr", flagImageResources.flagKR());
        flagImagesMap.put("kw", flagImageResources.flagKW());
        flagImagesMap.put("ky", flagImageResources.flagKY());
        flagImagesMap.put("kz", flagImageResources.flagKZ());
        flagImagesMap.put("la", flagImageResources.flagLA());
        flagImagesMap.put("lb", flagImageResources.flagLB());
        flagImagesMap.put("lc", flagImageResources.flagLC());
        flagImagesMap.put("li", flagImageResources.flagLI());
        flagImagesMap.put("lk", flagImageResources.flagLK());
        flagImagesMap.put("lr", flagImageResources.flagLR());
        flagImagesMap.put("ls", flagImageResources.flagLS());
        flagImagesMap.put("lt", flagImageResources.flagLT());
        flagImagesMap.put("lu", flagImageResources.flagLU());
        flagImagesMap.put("lv", flagImageResources.flagLV());
        flagImagesMap.put("ly", flagImageResources.flagLY());
        flagImagesMap.put("ma", flagImageResources.flagMA());
        flagImagesMap.put("mc", flagImageResources.flagMC());
        flagImagesMap.put("md", flagImageResources.flagMD());
        flagImagesMap.put("me", flagImageResources.flagME());
        flagImagesMap.put("mg", flagImageResources.flagMG());
        flagImagesMap.put("mh", flagImageResources.flagMH());
        flagImagesMap.put("mk", flagImageResources.flagMK());
        flagImagesMap.put("ml", flagImageResources.flagML());
        flagImagesMap.put("mm", flagImageResources.flagMM());
        flagImagesMap.put("mn", flagImageResources.flagMN());
        flagImagesMap.put("mo", flagImageResources.flagMO());
        flagImagesMap.put("mp", flagImageResources.flagMP());
        flagImagesMap.put("mq", flagImageResources.flagMQ());
        flagImagesMap.put("mr", flagImageResources.flagMR());
        flagImagesMap.put("ms", flagImageResources.flagMS());
        flagImagesMap.put("mt", flagImageResources.flagMT());
        flagImagesMap.put("mu", flagImageResources.flagMU());
        flagImagesMap.put("mv", flagImageResources.flagMV());
        flagImagesMap.put("mw", flagImageResources.flagMW());
        flagImagesMap.put("mx", flagImageResources.flagMX());
        flagImagesMap.put("my", flagImageResources.flagMY());
        flagImagesMap.put("mz", flagImageResources.flagMZ());
        flagImagesMap.put("na", flagImageResources.flagNA());
        flagImagesMap.put("nc", flagImageResources.flagNC());
        flagImagesMap.put("ne", flagImageResources.flagNE());
        flagImagesMap.put("nf", flagImageResources.flagNF());
        flagImagesMap.put("ng", flagImageResources.flagNG());
        flagImagesMap.put("ni", flagImageResources.flagNI());
        flagImagesMap.put("nl", flagImageResources.flagNL());
        flagImagesMap.put("no", flagImageResources.flagNO());
        flagImagesMap.put(NONE, flagImageResources.flagNONE());
        flagImagesMap.put("np", flagImageResources.flagNP());
        flagImagesMap.put("nr", flagImageResources.flagNR());
        flagImagesMap.put("nu", flagImageResources.flagNU());
        flagImagesMap.put("nz", flagImageResources.flagNZ());
        flagImagesMap.put("om", flagImageResources.flagOM());
        flagImagesMap.put("pa", flagImageResources.flagPA());
        flagImagesMap.put("pe", flagImageResources.flagPE());
        flagImagesMap.put("pf", flagImageResources.flagPF());
        flagImagesMap.put("pg", flagImageResources.flagPG());
        flagImagesMap.put("ph", flagImageResources.flagPH());
        flagImagesMap.put("pk", flagImageResources.flagPK());
        flagImagesMap.put("pl", flagImageResources.flagPL());
        flagImagesMap.put("pm", flagImageResources.flagPM());
        flagImagesMap.put("pn", flagImageResources.flagPN());
        flagImagesMap.put("pr", flagImageResources.flagPR());
        flagImagesMap.put("ps", flagImageResources.flagPS());
        flagImagesMap.put("pt", flagImageResources.flagPT());
        flagImagesMap.put("pw", flagImageResources.flagPW());
        flagImagesMap.put("py", flagImageResources.flagPY());
        flagImagesMap.put("qa", flagImageResources.flagQA());
        flagImagesMap.put("re", flagImageResources.flagRE());
        flagImagesMap.put("ro", flagImageResources.flagRO());
        flagImagesMap.put("rs", flagImageResources.flagRS());
        flagImagesMap.put("ru", flagImageResources.flagRU());
        flagImagesMap.put("rw", flagImageResources.flagRW());
        flagImagesMap.put("sa", flagImageResources.flagSA());
        flagImagesMap.put("sb", flagImageResources.flagSB());
        flagImagesMap.put("sc", flagImageResources.flagSC());
        flagImagesMap.put("scotland", flagImageResources.flagSCOTLAND());
        flagImagesMap.put("sd", flagImageResources.flagSD());
        flagImagesMap.put("se", flagImageResources.flagSE());
        flagImagesMap.put("sg", flagImageResources.flagSG());
        flagImagesMap.put("sh", flagImageResources.flagSH());
        flagImagesMap.put("si", flagImageResources.flagSI());
        flagImagesMap.put("sj", flagImageResources.flagSJ());
        flagImagesMap.put("sk", flagImageResources.flagSK());
        flagImagesMap.put("sl", flagImageResources.flagSL());
        flagImagesMap.put("sm", flagImageResources.flagSM());
        flagImagesMap.put("sn", flagImageResources.flagSN());
        flagImagesMap.put("so", flagImageResources.flagSO());
        flagImagesMap.put("sr", flagImageResources.flagSR());
        flagImagesMap.put("st", flagImageResources.flagST());
        flagImagesMap.put("sv", flagImageResources.flagSV());
        flagImagesMap.put("sy", flagImageResources.flagSY());
        flagImagesMap.put("sz", flagImageResources.flagSZ());
        flagImagesMap.put("tc", flagImageResources.flagTC());
        flagImagesMap.put("td", flagImageResources.flagTD());
        flagImagesMap.put("tf", flagImageResources.flagTF());
        flagImagesMap.put("tg", flagImageResources.flagTG());
        flagImagesMap.put("th", flagImageResources.flagTH());
        flagImagesMap.put("tj", flagImageResources.flagTJ());
        flagImagesMap.put("tk", flagImageResources.flagTK());
        flagImagesMap.put("tl", flagImageResources.flagTL());
        flagImagesMap.put("tm", flagImageResources.flagTM());
        flagImagesMap.put("tn", flagImageResources.flagTN());
        flagImagesMap.put("to", flagImageResources.flagTO());
        flagImagesMap.put("tr", flagImageResources.flagTR());
        flagImagesMap.put("tt", flagImageResources.flagTT());
        flagImagesMap.put("tv", flagImageResources.flagTV());
        flagImagesMap.put("tw", flagImageResources.flagTW());
        flagImagesMap.put("tz", flagImageResources.flagTZ());
        flagImagesMap.put("ua", flagImageResources.flagUA());
        flagImagesMap.put("ug", flagImageResources.flagUG());
        flagImagesMap.put("um", flagImageResources.flagUM());
        flagImagesMap.put("us", flagImageResources.flagUS());
        flagImagesMap.put("uy", flagImageResources.flagUY());
        flagImagesMap.put("uz", flagImageResources.flagUZ());
        flagImagesMap.put("va", flagImageResources.flagVA());
        flagImagesMap.put("vc", flagImageResources.flagVC());
        flagImagesMap.put("ve", flagImageResources.flagVE());
        flagImagesMap.put("vg", flagImageResources.flagVG());
        flagImagesMap.put("vi", flagImageResources.flagVI());
        flagImagesMap.put("vn", flagImageResources.flagVN());
        flagImagesMap.put("vu", flagImageResources.flagVU());
        flagImagesMap.put("wales", flagImageResources.flagWALES());
        flagImagesMap.put("wf", flagImageResources.flagWF());
        flagImagesMap.put("ws", flagImageResources.flagWS());
        flagImagesMap.put("ye", flagImageResources.flagYE());
        flagImagesMap.put("yt", flagImageResources.flagYT());
        flagImagesMap.put("za", flagImageResources.flagZA());
        flagImagesMap.put("zm", flagImageResources.flagZM());
        flagImagesMap.put("zw", flagImageResources.flagZW());
    }
    
    private static FlagImageResolver INSTANCE;
    
    private  FlagImageResolverImpl() {
    }
    
    public static FlagImageResolver get() {
        if (INSTANCE == null) {
            INSTANCE = new FlagImageResolverImpl();
        }
        return INSTANCE;
    }

    @Override
    public ImageResource getFlagImageResource(String twoLetterIsoCountryCode) {
        return flagImagesMap.get(twoLetterIsoCountryCode.toLowerCase());
    }

    @Override
    public ImageResource getEmptyFlagImageResource() {
        return flagImagesMap.get(NONE);
    }
    
    @Override
    public SafeUri getFlagImageUri(String flagImageUrl, String twoLetterIsoCountryCode) {
        if (flagImageUrl == null || flagImageUrl.isEmpty()) {
            if (twoLetterIsoCountryCode == null || twoLetterIsoCountryCode.isEmpty() || getFlagImageResource(twoLetterIsoCountryCode) == null) {
                return getEmptyFlagImageResource().getSafeUri();
            }
            return getFlagImageResource(twoLetterIsoCountryCode).getSafeUri();
        }
        return UriUtils.fromTrustedString(flagImageUrl);
    }

}
