package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.racecommittee.app.R;

public class FlagPoleStateRenderer {
    
    private final Context context;
    private final LinearLayout upperFlagsViewGroup;
    private final LinearLayout lowerFlagsViewGroup;
    
    public FlagPoleStateRenderer(Context context, LinearLayout upperFlagsViewGroup, LinearLayout lowerFlagsViewGroup) {
        this.context = context;
        this.upperFlagsViewGroup = upperFlagsViewGroup;
        this.lowerFlagsViewGroup = lowerFlagsViewGroup;
    }
    
    public void render(FlagPoleState state) {
        upperFlagsViewGroup.removeAllViews();
        lowerFlagsViewGroup.removeAllViews();
        
        for (FlagPole pole : state.getCurrentState()) {
            int upperFlagResourceId = getFlagResourceId(pole.getUpperFlag());
            //int lowerFlagResourceId = getFlagResourceId(pole.getLowerFlag());
            if (pole.isDisplayed()) {
                upperFlagsViewGroup.addView(createFlagImageView(upperFlagResourceId));
            } else {
                lowerFlagsViewGroup.addView(createFlagImageView(upperFlagResourceId));
            }
        }
        
        /*FlagPoleState activeFlags = getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now());
        Flags startModeFlag = getRacingProcedure().getStartModeFlag();
        
        boolean isClassFlagUp = false;
        boolean isStartModeFlagUp = false;
        
        for (FlagPole pole : activeFlags.getCurrentState()) {
            if (pole.isDisplayed()) {
                if (pole.getUpperFlag().equals(Flags.CLASS)) {
                    isClassFlagUp = true;
                } else if (pole.getUpperFlag().equals(startModeFlag)) {
                    isStartModeFlagUp = true;
                }
            }
        }
        
        if (isClassFlagUp) {
            upperFlagsViewGroup.addView(classFlagImageView);
        } else {
            lowerFlagsViewGroup.addView(classFlagImageView);
        }
        if (isStartModeFlagUp) {
            upperFlagsViewGroup.addView(startModeFlagImageView);
        } else {
            lowerFlagsViewGroup.addView(startModeFlagImageView);
        }*/
    }
    
    private ImageView createFlagImageView(int flagDrawableId) {
        ImageView flagView = new ImageView(context);
        flagView.setLayoutParams(new LinearLayout.LayoutParams(200, 130));
        flagView.setImageResource(flagDrawableId);
        return flagView;
    }
    
    private int getFlagResourceId(Flags upperFlag) {
        return R.drawable.india_flag_mini;
    }
/*
    private int getStartModeImageResourceId() {
        switch (getRacingProcedure().getStartModeFlag()) {
        case PAPA:
            return R.drawable.papa_flag_mini;
        case INDIA:
            return R.drawable.india_flag_mini;
        case BLACK:
            return R.drawable.black_flag_mini;
        case ZULU:
            return R.drawable.zulu_flag_mini;
        default:
            return R.drawable.papa_flag_mini;
        }
    
    private int getClassmageResourceId() {
        // TODO: check boat class for specific image
        return R.drawable.generic_class;
    }

    private int getFleetColorId() {
        Triple<Integer, Integer, Integer> rgb = getRace().getFleet().getColor().getAsRGB();
        int color = Color.rgb(rgb.getA(), rgb.getB(), rgb.getC());
        return color;
    }
    }*/

}
