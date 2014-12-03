package com.sap.sailing.racecommittee.app.ui.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sse.common.Util;

public class FlagPoleStateRenderer {
    
    private final Context context;
    private final ManagedRace race;
    private final LinearLayout displayedFlagsViewGroup;
    private final LinearLayout removedFlagsViewGroup;
    
    private final FlagsBitmapCache bitmapCache;
    private FlagPoleState previousState;
    
    public FlagPoleStateRenderer(Context context, ManagedRace race,
            LinearLayout upperFlagsViewGroup, LinearLayout lowerFlagsViewGroup) {
        this.context = context;
        this.race = race;
        this.displayedFlagsViewGroup = upperFlagsViewGroup;
        this.removedFlagsViewGroup = lowerFlagsViewGroup;
        
        this.bitmapCache = new FlagsBitmapCache(context);
        this.previousState = null;
    }
    
    public void render(FlagPoleState state) {
        if (previousState != null && FlagPoleState.describesSameState(previousState, state)) {
            return;
        }
        ExLog.i(context, FlagPoleStateRenderer.class.getSimpleName(), "Re-Rendering flags.");
        previousState = state;
        
        displayedFlagsViewGroup.removeAllViews();
        removedFlagsViewGroup.removeAllViews();
        
        for (FlagPole pole : state.getCurrentState()) {
            if (pole.isDisplayed()) {
                displayedFlagsViewGroup.addView(createFlagImageView(pole.getUpperFlag(), pole.getLowerFlag()));
            } else {
                removedFlagsViewGroup.addView(createFlagImageView(pole.getUpperFlag(), pole.getLowerFlag()));
            }
        }
    }
    
    private ImageView createFlagImageView(final Flags upperFlag, final Flags lowerFlag) {
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(200, 130);
        layout.setMargins(20, 0, 20, 0);
        
        ImageView flagView = new ImageView(context);
        flagView.setLayoutParams(layout);
        flagView.setImageBitmap(bitmapCache.getBitmap(upperFlag, lowerFlag));
        flagView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), upperFlag.toString() + "|" + lowerFlag.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        if (upperFlag == Flags.CLASS && race.getFleet().getColor() != null) {
            flagView.setPadding(6, 6, 6, 6);
            flagView.setBackgroundColor(getFleetColorId());
        }
        return flagView;
    }
    
    private int getFleetColorId() {
        Util.Triple<Integer, Integer, Integer> rgb = race.getFleet().getColor() == null ? 
                new Util.Triple<Integer, Integer, Integer>(0, 0, 0) : race.getFleet().getColor().getAsRGB();
        return Color.rgb(rgb.getA(), rgb.getB(), rgb.getC());
    }
    
}
