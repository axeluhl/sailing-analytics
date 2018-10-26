package com.sap.sailing.racecommittee.app.ui.utils;

import java.util.ArrayList;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.impl.AbstractColor;

import android.content.Context;

public class ESSMarkImageHelper extends MarkImageHelper {
    private static ESSMarkImageHelper instance = null;

    private ESSMarkImageHelper(Context context) {
        markImageDescriptors = new ArrayList<>();
        defaultCourseMarkDescriptor = createMarkImageDescriptor(context, 0, MarkType.BUOY,
                /* use the mark color dynamically */ null, null, null);
        createMarkImageDescriptor(context, R.drawable.buoy_white_cone, MarkType.BUOY,
                AbstractColor.getCssColor("white"), "conical", null);
        createMarkImageDescriptor(context, R.drawable.buoy_black_cone, MarkType.BUOY,
                AbstractColor.getCssColor("black"), "conical", null);
        createMarkImageDescriptor(context, R.drawable.buoy_black_cyl, MarkType.BUOY, AbstractColor.getCssColor("black"),
                "cylinder", "checkered");

        createMarkImageDescriptor(context, R.drawable.buoy_cameraboat, MarkType.CAMERABOAT, null, null, null);
        createMarkImageDescriptor(context, R.drawable.buoy_umpireboat, MarkType.UMPIREBOAT, null, null, null);
        createMarkImageDescriptor(context, R.drawable.buoy_rc_startboat, MarkType.STARTBOAT, null, null, null);

        createMarkImageDescriptor(context, R.drawable.buoy_finish_flag, MarkType.LANDMARK,
                AbstractColor.getCssColor("black"), null, null);
    }

    public static ESSMarkImageHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ESSMarkImageHelper(context);
        }
        return instance;
    }

}
