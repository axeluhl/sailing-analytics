package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class FlagPoleStateRenderer {
    
    private final Context context;
    private final ManagedRace race;
    private final LinearLayout displayedFlagsViewGroup;
    private final LinearLayout removedFlagsViewGroup;
    
    private FlagPoleState previousState;
    
    public FlagPoleStateRenderer(Context context, ManagedRace race,
            LinearLayout upperFlagsViewGroup, LinearLayout lowerFlagsViewGroup) {
        this.context = context;
        this.race = race;
        this.displayedFlagsViewGroup = upperFlagsViewGroup;
        this.removedFlagsViewGroup = lowerFlagsViewGroup;
        this.previousState = null;
    }
    
    public void render(FlagPoleState state) {
        if (previousState != null && FlagPoleState.describesSameState(previousState, state)) {
            return;
        }
        ExLog.i(FlagPoleStateRenderer.class.getSimpleName(), "Rendering flags.");
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
        ImageView flagView = new ImageView(context);
        flagView.setLayoutParams(new LinearLayout.LayoutParams(200, 130));
        flagView.setImageBitmap(getFlagBitmap(upperFlag));
        flagView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), upperFlag.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        if (upperFlag == Flags.CLASS) {
            flagView.setPadding(6, 6, 6, 6);
            flagView.setBackgroundColor(getFleetColorId());
        }
        return flagView;
    }
    
    private Bitmap getFlagBitmap(Flags flag) {
        int resourceId = 0;
        switch (flag) {
        case CLASS:
            resourceId = R.drawable.generic_class;
            break;
        case ALPHA:
            resourceId = R.drawable.alpha_flag;
            break;
        case AP:
            resourceId = R.drawable.ap_flag_mini;
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
    
    private int getFleetColorId() {
        Triple<Integer, Integer, Integer> rgb = race.getFleet().getColor().getAsRGB();
        return Color.rgb(rgb.getA(), rgb.getB(), rgb.getC());
    }
    
}
