package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.gwt.ui.client.ColorMap;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

public class ColorMapTest {

	public static String[] flagImageNames = { "ad", "ae", "af", "ag", "ai", "al", "am", "an",
		"ao", "ar", "as", "at", "au", "aw", "ax", "az", "ba", "bb", "bd",
		"be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs",
		"bt", "bv", "bw", "by", "bz", "ca", "catalonia", "cc", "cd", "cf",
		"cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cr", "cs", "cu",
		"cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec",
		"ee", "eg", "eh", "england", "er", "es", "et", "europeanunion",
		"fam", "fi", "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge",
		"gf", "gh", "gi", "gl", "gm", "gn", "gp", "gq", "gr", "gs", "gt",
		"gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie",
		"il", "in", "io", "iq", "ir", "is", "it", "jm", "jo", "jp", "ke",
		"kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la",
		"lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma",
		"mc", "md", "me", "mg", "mh", "mk", "ml", "mm", "mn", "mo", "mp",
		"mq", "mr", "ms", "mt", "mu", "mv", "mw", "mx", "my", "mz", "na",
		"nc", "ne", "nf", "ng", "ni", "nl", "no", "np", "nr", "nu", "nz",
		"om", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr",
		"ps", "pt", "pw", "py", "qa", "re", "ro", "rs", "ru", "rw", "sa",
		"sb", "sc", "scotland", "sd", "se", "sg", "sh", "si", "sj", "sk",
		"sl", "sm", "sn", "so", "sr", "st", "sv", "sy", "sz", "tc", "td",
		"tf", "tg", "th", "tj", "tk", "tl", "tm", "tn", "to", "tr", "tt",
		"tv", "tw", "tz", "ua", "ug", "um", "us", "uy", "uz", "va", "vc",
		"ve", "vg", "vi", "vn", "vu", "wales", "wf", "ws", "ye", "yt",
		"za", "zm", "zw" };


	@Test
    public void createImageResourcesOfFlags() {
		for (String flagName: flagImageNames) {
//			System.out.println("@Source(\"com/sap/sailing/gwt/ui/client/images/flags/" + flagName + ".png\")");
//			System.out.println("ImageResource flag" + flagName.toUpperCase() + "();");
//			System.out.println("");
			System.out.println("flagImagesMap.put(\"" + flagName + "\", flagImageResources.flag" + flagName.toUpperCase() + "());");
		}
	}
	
    @Test
    public void testHundredDistinctColors() {
        ColorMap<Integer> colorMap = new ColorMap<Integer>();
        List<String> existingColors = new ArrayList<String>(); 
        int amountOfDistinctColorsToCreate = 100;
        for(int i = 1; i <= amountOfDistinctColorsToCreate; i++) {
            String colorByID = colorMap.getColorByID(i);
            if(!existingColors.contains(colorByID)) {
                existingColors.add(colorByID);
            }
        }
        assertEquals(amountOfDistinctColorsToCreate, existingColors.size());
    }
    
    /**
     * A function only useful for visual tests.
     * It creates an HTML file (as string) with 100 distinct colors on top of the water color of the google map.
     */
    public void createColorMapAsHtml() {
        ColorMap<Integer> colorMap = new ColorMap<Integer>();
        int amountOfDistinctColorsToCreate = 100;
        
        String colorMapAsHtml = "<html><head></head><body style='background-color: #A5BFDD'>";       
     
        for(int i = 1; i <= amountOfDistinctColorsToCreate; i++) {
            colorMapAsHtml += "<div style='height:3px; background-color:" + colorMap.getColorByID(i) + "'></div><br/>";
        }
        
        colorMapAsHtml += "</body></html>";
        System.out.println(colorMapAsHtml);
    }
}
