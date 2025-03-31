package com.sap.sse.common;

import java.io.Serializable;

import com.sap.sse.common.impl.RGBColor;

public interface Color extends Serializable {
    static Color ofRgb(String rgb) {
        return new RGBColor(rgb);
    }
    
    final Color ALICE_BLUE = ofRgb("#F0F8FF");

    final Color ANTIQUE_WHITE = ofRgb("#FAEBD7");
    
    final Color AQUA = ofRgb("#00FFFF");

    final Color AQUAMARINE = ofRgb("#7FFFD4");

    final Color AZURE = ofRgb("#F0FFFF");
    
    final Color BEIGE = ofRgb("#F5F5DC");

    final Color BISQUE = ofRgb("#FFE4C4");

    final Color BLACK = ofRgb("#000000");
    
    final Color BLANCHED_ALMOND = ofRgb("#FFEBCD");

    final Color BLUE = ofRgb("#0000FF");

    final Color BLUE_VIOLET = ofRgb("#8A2BE2");
    
    final Color BROWN = ofRgb("#A52A2A");

    final Color BURLY_WOOD = ofRgb("#DEB887");

    final Color CADET_BLUE = ofRgb("#5F9EA0");
    
    final Color CHARTREUSE = ofRgb("#7FFF00");

    final Color CHOCOLATE = ofRgb("#D2691E");

    final Color CORAL = ofRgb("#FF7F50");
    
    final Color CORNFLOWER_BLUE = ofRgb("#6495ED");

    final Color CORNSILK = ofRgb("#FFF8DC");

    final Color CRIMSON = ofRgb("#DC143C");
    
    final Color CYAN = ofRgb("#00FFFF");

    final Color DARK_BLUE = ofRgb("#00008B");

    final Color DARK_CYAN = ofRgb("#008B8B");
    
    final Color DARK_GOLDEN_ROD = ofRgb("#B8860B");

    final Color DARK_GRAY = ofRgb("#A9A9A9");

    final Color DARK_GREY = ofRgb("#A9A9A9");
    
    final Color DARK_GREEN = ofRgb("#006400");

    final Color DARK_KHAKI = ofRgb("#BDB76B");

    final Color DARK_MAGENTA = ofRgb("#8B008B");
    
    final Color DARK_OLIVE_GREEN = ofRgb("#556B2F");

    final Color DARK_ORANGE = ofRgb("#FF8C00");

    final Color DARK_ORCHID = ofRgb("#9932CC");
    
    final Color DARK_RED = ofRgb("#8B0000");

    final Color DARK_SALMON = ofRgb("#E9967A");

    final Color DARK_SEA_GREEN = ofRgb("#8FBC8F");
    
    final Color DARK_SLATE_BLUE = ofRgb("#483D8B");

    final Color DARK_SLATE_GRAY = ofRgb("#2F4F4F");

    final Color DARK_SLATE_GREY = ofRgb("#2F4F4F");
    
    final Color DARK_TURQUOISE = ofRgb("#00CED1");

    final Color DARK_VIOLET = ofRgb("#9400D3");

    final Color DEEP_PINK = ofRgb("#FF1493");
    
    final Color DEEP_SKY_BLUE = ofRgb("#00BFFF");

    final Color DIM_GRAY = ofRgb("#696969");

    final Color DIM_GREY = ofRgb("#696969");
    
    final Color DODGER_BLUE = ofRgb("#1E90FF");

    final Color FIRE_BRICK = ofRgb("#B22222");

    final Color FLORAL_WHITE = ofRgb("#FFFAF0");
    
    final Color FOREST_GREEN = ofRgb("#228B22");

    final Color FUCHSIA = ofRgb("#FF00FF");

    final Color GAINSBORO = ofRgb("#DCDCDC");
    
    final Color GHOST_WHITE = ofRgb("#F8F8FF");

    final Color GOLD = ofRgb("#FFD700");

    final Color GOLDEN_ROD = ofRgb("#DAA520");
    
    final Color GRAY = ofRgb("#808080");

    final Color GREY = ofRgb("#808080");

    final Color GREEN = ofRgb("#008000");
    
    final Color GREEN_YELLOW = ofRgb("#ADFF2F");

    final Color HONEY_DEW = ofRgb("#F0FFF0");

    final Color HOT_PINK = ofRgb("#FF69B4");
    
    final Color INDIAN_RED = ofRgb("#CD5C5C");

    final Color INDIGO = ofRgb("#4B0082");

    final Color IVORY = ofRgb("#FFFFF0");
    
    final Color KHAKI = ofRgb("#F0E68C");

    final Color LAVENDER = ofRgb("#E6E6FA");

    final Color LAVENDER_BLUSH = ofRgb("#FFF0F5");
    
    final Color LAWN_GREEN = ofRgb("#7CFC00");

    final Color LEMON_CHIFFON = ofRgb("#FFFACD");

    final Color LIGHT_BLUE = ofRgb("#ADD8E6");
    
    final Color LIGHT_CORAL = ofRgb("#F08080");

    final Color LIGHT_CYAN = ofRgb("#E0FFFF");

    final Color LIGHT_GOLDEN_ROD_YELLOW = ofRgb("#FAFAD2");
    
    final Color LIGHT_GRAY = ofRgb("#D3D3D3");

    final Color LIGHT_GREY = ofRgb("#D3D3D3");

    final Color LIGHT_GREEN = ofRgb("#90EE90");
    
    final Color LIGHT_PINK = ofRgb("#FFB6C1");

    final Color LIGHT_SALMON = ofRgb("#FFA07A");

    final Color LIGHT_SEA_GREEN = ofRgb("#20B2AA");
    
    final Color LIGHT_SKY_BLUE = ofRgb("#87CEFA");

    final Color LIGHT_SLATE_GRAY = ofRgb("#778899");
    
    final Color LIGHT_SLATE_GREY = ofRgb("#778899");

    final Color LIGHT_STEEL_BLUE = ofRgb("#B0C4DE");

    final Color LIGHT_YELLOW = ofRgb("#FFFFE0");
    
    final Color LIME = ofRgb("#00FF00");

    final Color LIME_GREEN = ofRgb("#32CD32");

    final Color LINEN = ofRgb("#FAF0E6");
    
    final Color MAGENTA = ofRgb("#FF00FF");

    final Color MAROON = ofRgb("#800000");
    
    final Color MEDIUM_AQUA_MARINE = ofRgb("#66CDAA");

    final Color MEDIUM_BLUE = ofRgb("#0000CD");

    final Color MEDIUM_ORCHID = ofRgb("#BA55D3");
    
    final Color MEDIUM_PURPLE = ofRgb("#9370DB");

    final Color MEDIUM_SEA_GREEN = ofRgb("#3CB371");

    final Color MEDIUM_SLATE_BLUE = ofRgb("#7B68EE");
    
    final Color MEDIUM_SPRING_GREEN = ofRgb("#00FA9A");

    final Color MEDIUM_TURQUOISE = ofRgb("#48D1CC");
    
    final Color MEDIUM_VIOLET_RED = ofRgb("#C71585");

    final Color MIDNIGHT_BLUE = ofRgb("#191970");

    final Color MINT_CREAM = ofRgb("#F5FFFA");
    
    final Color MISTY_ROSE = ofRgb("#FFE4E1");

    final Color MOCCASIN = ofRgb("#FFE4B5");

    final Color NAVAJO_WHITE = ofRgb("#FFDEAD");
    
    final Color NAVY = ofRgb("#000080");

    final Color OLD_LACE = ofRgb("#FDF5E6");
    
    final Color OLIVE = ofRgb("#808000");

    final Color OLIVE_DRAB = ofRgb("#6B8E23");

    final Color ORANGE = ofRgb("#FFA500");
    
    final Color ORANGE_RED = ofRgb("#FF4500");

    final Color ORCHID = ofRgb("#DA70D6");

    final Color PALE_GOLDEN_ROD = ofRgb("#EEE8AA");
    
    final Color PALE_GREEN = ofRgb("#98FB98");

    final Color PALE_TURQUOISE = ofRgb("#AFEEEE");

    final Color PALE_VIOLET_RED = ofRgb("#DB7093");
    
    final Color PAPAYA_WHIP = ofRgb("#FFEFD5");

    final Color PEACH_PUFF = ofRgb("#FFDAB9");

    final Color PERU = ofRgb("#CD853F");
    
    final Color PINK = ofRgb("#FFC0CB");

    final Color PLUM = ofRgb("#DDA0DD");

    final Color POWDER_BLUE = ofRgb("#B0E0E6");
    
    final Color PURPLE = ofRgb("#800080");

    final Color REBECCA_PURPLE = ofRgb("#663399");

    final Color RED = ofRgb("#FF0000");
    
    final Color ROSY_BROWN = ofRgb("#BC8F8F");

    final Color ROYAL_BLUE = ofRgb("#4169E1");

    final Color SADDLER_BROWN = ofRgb("#8B4513");
    
    final Color SALMON = ofRgb("#FA8072");

    final Color SANDY_BROWN = ofRgb("#F4A460");

    final Color SEA_GREEN = ofRgb("#2E8B57");
    
    final Color SEA_SHELL = ofRgb("#FFF5EE");

    final Color SIENNA = ofRgb("#A0522D");

    final Color SILVER = ofRgb("#C0C0C0");
    
    final Color SKY_BLUE = ofRgb("#87CEEB");

    final Color SLATE_BLUE = ofRgb("#6A5ACD");

    final Color SLATE_GRAY = ofRgb("#708090");
    
    final Color SLATE_GREY = ofRgb("#708090");

    final Color SNOW = ofRgb("#FFFAFA");

    final Color SPRING_GREEN = ofRgb("#00FF7F");
    
    final Color STEEL_BLUE = ofRgb("#4682B4");

    final Color TAN = ofRgb("#D2B48C");

    final Color TEAL = ofRgb("#008080");
    
    final Color THISTLE = ofRgb("#D8BFD8");

    final Color TOMATO = ofRgb("#FF6347");

    final Color TURQUOISE = ofRgb("#40E0D0");
    
    final Color VIOLET = ofRgb("#EE82EE");

    final Color WHEAT = ofRgb("#F5DEB3");

    final Color WHITE = ofRgb("#FFFFFF");
    
    final Color WHITE_SMOKE = ofRgb("#F5F5F5");

    final Color YELLOW = ofRgb("#FFFF00");

    final Color YELLOW_GREEN = ofRgb("#9ACD32");
    
    com.sap.sse.common.Util.Triple<Integer, Integer, Integer> getAsRGB();

    com.sap.sse.common.Util.Triple<Float, Float, Float> getAsHSV();

    String getAsHtml();

    /**
     * @return an inverted color, computed as {@code rgb(255-R, 255-G, 255-B)}
     */
    Color invert();
}
