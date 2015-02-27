package com.sap.sailing.racecommittee.app.ui.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;

public class FlagsResources {

    private static int getResId(Context context, String res, int size) {
        String fileName;

        switch (Flags.valueOf(res)) {
            case ALPHA:
                fileName = "flag_alpha_" + size + "dp";
                break;

            case AP:
                fileName = "flag_ap_" + size + "dp";
                break;

            case BLACK:
                fileName = "flag_black_" + size + "dp";
                break;

            case BRAVO:
                fileName = "flag_bravo_" + size + "dp";
                break;

            case BLUE:
                fileName = "flag_blue_" + size + "dp";
                break;

            case CLASS:
                fileName = "flag_class_" + size + "dp";
                break;

            case ESSONE:
                fileName = "flag_ess1_" + size + "dp";
                break;

            case ESSTHREE:
                fileName = "flag_ess2_" + size + "dp";
                break;

            case ESSTWO:
                fileName = "flag_ess3_" + size + "dp";
                break;

            case FIRSTSUBSTITUTE:
                fileName = "flag_first_substitute_" + size + "dp";
                break;

            case FOXTROTT:
                fileName = "flag_foxtrott_" + size + "dp";
                break;

            case GOLF:
                fileName = "flag_golf_" + size + "dp";
                break;

            case HOTEL:
                fileName = "flag_hotel_" + size + "dp";
                break;

            case INDIA:
                fileName = "flag_india_" + size + "dp";
                break;

            case JURY:
                fileName = "flag_jury_" + size + "dp";
                break;

            case NOVEMBER:
                fileName = "flag_november_" + size + "dp";
                break;

            case PAPA:
                fileName = "flag_papa_" + size + "dp";
                break;

            case XRAY:
                fileName = "flag_xray_" + size + "dp";
                break;

            case ZULU:
                fileName = "flag_zulu_" + size + "dp";
                break;

            default:
                fileName = "flag_alpha_32dp";
        }

        return context.getResources().getIdentifier(fileName, "drawable", context.getPackageName());
    }

    public static Drawable getFlagDrawable(Context context, String flag, int size) {
        return context.getResources().getDrawable(FlagsResources.getResId(context, flag, size));
    }
}
