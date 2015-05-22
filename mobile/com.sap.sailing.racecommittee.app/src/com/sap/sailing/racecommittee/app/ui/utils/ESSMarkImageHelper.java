package com.sap.sailing.racecommittee.app.ui.utils;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.racecommittee.app.R;

import java.util.ArrayList;

public class ESSMarkImageHelper extends MarkImageHelper {
    private static ESSMarkImageHelper instance = null;

    private ESSMarkImageHelper() {
        markImageDescriptors = new ArrayList<MarkImageDescriptor>();

        defaultCourseMarkDescriptor = createMarkImageDescriptor(R.drawable.buoy_buoy, MarkType.BUOY, "undefined", null, null);

        createMarkImageDescriptor(R.drawable.buoy_red, MarkType.BUOY, "red", null, null); 
        createMarkImageDescriptor(R.drawable.buoy_green, MarkType.BUOY, "green", null, null); 
        createMarkImageDescriptor(R.drawable.buoy_yellow, MarkType.BUOY, "yellow", null, null); 
        createMarkImageDescriptor(R.drawable.buoy_grey, MarkType.BUOY, "grey", null, null);
        createMarkImageDescriptor(R.drawable.buoy_white_grey, MarkType.BUOY, "white", null, null);
        createMarkImageDescriptor(R.drawable.buoy_white_cone, MarkType.BUOY, "white", "conical", null); 
        createMarkImageDescriptor(R.drawable.buoy_black, MarkType.BUOY, "black", null, null);
        createMarkImageDescriptor(R.drawable.buoy_black_cone, MarkType.BUOY, "black", "conical", null); 
        createMarkImageDescriptor(R.drawable.buoy_dark_orange, MarkType.BUOY, "orange", null, null);
        createMarkImageDescriptor(R.drawable.buoy_black_cyl, MarkType.BUOY, "black", "cylinder", "checkered"); 

        createMarkImageDescriptor(R.drawable.buoy_cameraboat, MarkType.CAMERABOAT, null, null, null);
        createMarkImageDescriptor(R.drawable.buoy_umpireboat, MarkType.UMPIREBOAT, null, null, null);
        createMarkImageDescriptor(R.drawable.buoy_rc_startboat, MarkType.STARTBOAT, null, null, null);

        createMarkImageDescriptor(R.drawable.buoy_finish_flag, MarkType.LANDMARK, "black", null, null);
    }

    public static ESSMarkImageHelper getInstance() {
        if (instance == null) {
            instance = new ESSMarkImageHelper();
        }
        return instance;
    }    

}
