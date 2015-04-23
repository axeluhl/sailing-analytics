package com.sap.sailing.racecommittee.app.ui.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.android.shared.logging.ExLog;

public class FlagsBitmapCache {

    private static final Map<Pair<Flags, Flags>, Bitmap> bitmapCache = new HashMap<Pair<Flags,Flags>, Bitmap>();
    private final Context context;
    
    public FlagsBitmapCache(Context context) {
        this.context = context;
    }
    
    public void clearCache() {
        bitmapCache.clear();
    }
    
    public Bitmap getBitmap(Flags upperFlag, Flags lowerFlag) {
        Pair<Flags, Flags> query = new Pair<Flags, Flags>(upperFlag, lowerFlag);
        Bitmap flagBitmap = bitmapCache.get(query);
        if (flagBitmap == null) {
            ExLog.i(context, FlagsBitmapCache.class.getSimpleName(), 
                    String.format("Creating Bitmap for %s|%s flag.", upperFlag, lowerFlag));
            flagBitmap = createFlagBitmap(upperFlag, lowerFlag);
            bitmapCache.put(query, flagBitmap);
        }
        return flagBitmap;
    }
    
    private Bitmap createFlagBitmap(Flags flag, Flags lowerFlag) {
        int resourceId = 0;
        switch (flag) {
        case CLASS:
            resourceId = R.drawable.generic_class;
            break;
        case ALPHA:
            resourceId = R.drawable.alpha_flag;
            break;
        case AP:
            resourceId = R.drawable.ap_flag;
            break;
        case BLACK:            
            resourceId = R.drawable.black_flag_mini;
            break;
        case BLUE:            
            resourceId = R.drawable.blue_flag_mini;
            break;
        case BRAVO:            
            resourceId = R.drawable.bravo_mini;
            break;
        case ESSONE:            
            resourceId = R.drawable.one_min_flag;
            break;
        case ESSTHREE:            
            resourceId = R.drawable.three_min_flag;
            break;
        case ESSTWO:            
            resourceId = R.drawable.two_min_flag;
            break;
        case FIRSTSUBSTITUTE:            
            resourceId = R.drawable.first_substitute_flag;
            break;
        case FOXTROTT:            
            resourceId = R.drawable.foxtrott_flag;
            break;
        case GOLF:            
            resourceId = R.drawable.golf_flag;
            break;
        case HOTEL:            
            resourceId = R.drawable.hotel_flag;
            break;
        case INDIA:            
            resourceId = R.drawable.india_flag_mini;
            break;
        case INDIA_ZULU:
            resourceId = R.drawable.india_zulu_flag_mini;
            break;
        case JURY:            
            resourceId = R.drawable.jury_flag_mini;
            break;
        case NONE:            
            resourceId = R.drawable.jury_flag_mini;
            break;
        case NOVEMBER:            
            resourceId = R.drawable.november_flag;
            break;
        case PAPA:            
            resourceId = R.drawable.papa_flag_mini;
            break;
        case UNIFORM:            
            resourceId = R.drawable.uniform_flag_mini;
            break;
        case XRAY:            
            resourceId = R.drawable.xray_flag;
            break;
        case ZULU:            
            resourceId = R.drawable.zulu_flag_mini;
            break;
        default:
            resourceId = R.drawable.india_flag_mini;
            break;
        }
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }

}
