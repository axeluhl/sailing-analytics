package com.sap.sailing.racecommittee.app.utils;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.racecommittee.app.R;

public enum MarkImageHelper {
    INSTANCE;
    
    List<MarkImageDescriptor> markImageDescriptors;
    private MarkImageDescriptor defaultCourseMarkDescriptor;
    
    private MarkImageHelper() {
        markImageDescriptors = new ArrayList<MarkImageDescriptor>();
        
        defaultCourseMarkDescriptor = createMarkImageDescriptor(R.drawable.buoy_buoy, MarkType.BUOY, "undefined", null, null);

        createMarkImageDescriptor(R.drawable.buoy_red, MarkType.BUOY, "red", null, null); 
        createMarkImageDescriptor(R.drawable.buoy_green, MarkType.BUOY, "green", null, null); 
        createMarkImageDescriptor(R.drawable.buoy_yellow, MarkType.BUOY, "yellow", null, null); 
        createMarkImageDescriptor(R.drawable.buoy_grey, MarkType.BUOY, "grey", null, null);
        createMarkImageDescriptor(R.drawable.buoy_white_grey, MarkType.BUOY, "white", null, null);
        createMarkImageDescriptor(R.drawable.buoy_white_grey_cone, MarkType.BUOY, "white", "conical", null); 
        createMarkImageDescriptor(R.drawable.buoy_black, MarkType.BUOY, "black", null, null);
        createMarkImageDescriptor(R.drawable.buoy_black_cone, MarkType.BUOY, "black", "conical", null); 
        createMarkImageDescriptor(R.drawable.buoy_dark_orange, MarkType.BUOY, "orange", null, null);
        createMarkImageDescriptor(R.drawable.buoy_black_cone, MarkType.BUOY, "black", "cylinder", "checkered"); 
        
        createMarkImageDescriptor(R.drawable.buoy_cameraboat, MarkType.CAMERABOAT, null, null, null);
        createMarkImageDescriptor(R.drawable.buoy_umpireboat, MarkType.UMPIREBOAT, null, null, null);
        createMarkImageDescriptor(R.drawable.buoy_startboat, MarkType.STARTBOAT, null, null, null);

        createMarkImageDescriptor(R.drawable.buoy_landmark, MarkType.LANDMARK, null, null, null);
    }
    
    public int resolveMarkImage(Mark mark) {
        MarkImageDescriptor result = defaultCourseMarkDescriptor;
        int highestCompatibilityLevel = -1;
        
        for (MarkImageDescriptor imageDescriptor: markImageDescriptors) {
            int compatibilityLevel = imageDescriptor.getCompatibilityLevel(mark.getType(), mark.getColor(), mark.getShape(), mark.getPattern());
            if(compatibilityLevel > highestCompatibilityLevel) {
               result = imageDescriptor;
               highestCompatibilityLevel = compatibilityLevel;
               if(highestCompatibilityLevel == 3) {
                   break;
               }
            }
        }
        
        return result.getDrawableId();
    }
    
    private MarkImageDescriptor createMarkImageDescriptor(int drawableId, MarkType type, String color, String shape, String pattern) {
        MarkImageDescriptor markIconDescriptor = new MarkImageDescriptor(drawableId, type, color, shape, pattern);
        markImageDescriptors.add(markIconDescriptor);
        
        return markIconDescriptor;
    }

}
