package com.sap.sailing.racecommittee.app.ui.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;

public class FlagsResources {

    private static int getResId(String res, int size) {
        switch (Flags.valueOf(res)) {
            case AP:
                switch (size) {
                    case 1:
                        return R.drawable.flag_ap_48dp;

                    case 3:
                        return R.drawable.flag_ap_96dp;

                    default:
                        return R.drawable.flag_ap_64dp;
                }

            case BLACK:
                switch (size) {
                    case 1:
                        return R.drawable.flag_black_48dp;

                    case 3:
                        return R.drawable.flag_black_96dp;

                    default:
                        return R.drawable.flag_black_64dp;
                }

            case BRAVO:
                switch (size) {
                    case 1:
                        return R.drawable.flag_bravo_48dp;

                    case 3:
                        return R.drawable.flag_bravo_96dp;

                    default:
                        return R.drawable.flag_bravo_64dp;
                }

            case BLUE:
                switch (size){
                    case 1:
                        return R.drawable.flag_blue_48dp;

                    case 3:
                        return R.drawable.flag_blue_96dp;

                    default:
                        return R.drawable.flag_blue_64dp;
                }

            case CLASS:
                switch (size) {
                    case 1:
                        return R.drawable.flag_class_48dp;

                    case 3:
                        return R.drawable.flag_class_96dp;

                    default:
                        return R.drawable.flag_class_64dp;
                }

            case ESSONE:
                return R.drawable.one_min_flag;

            case ESSTHREE:
                return R.drawable.three_min_flag;

            case ESSTWO:
                return R.drawable.two_min_flag;

            case FIRSTSUBSTITUTE:
                switch (size) {
                    case 1:
                        return R.drawable.flag_first_substitute_48dp;

                    case 3:
                        return R.drawable.flag_first_substitute_96dp;

                    default:
                        return R.drawable.flag_first_substitute_64dp;
                }

            case FOXTROTT:
                switch (size) {
                    case 1:
                        return R.drawable.flag_foxtrott_48dp;

                    case 3:
                        return R.drawable.flag_foxtrott_96dp;

                    default:
                        return R.drawable.flag_foxtrott_64dp;
                }

            case GOLF:
                switch (size) {
                    case 1:
                        return R.drawable.flag_golf_48dp;

                    case 3:
                        return R.drawable.flag_golf_96dp;

                    default:
                        return R.drawable.flag_golf_64dp;
                }

            case HOTEL:
                switch (size) {
                    case 1:
                        return R.drawable.flag_hotel_48dp;

                    case 3:
                        return R.drawable.flag_hotel_96dp;

                    default:
                        return R.drawable.flag_hotel_64dp;
                }

            case INDIA:
                switch (size) {
                    case 1:
                        return R.drawable.flag_india_48dp;

                    case 3:
                        return R.drawable.flag_india_96dp;

                    default:
                        return R.drawable.flag_india_64dp;
                }

            case JURY:
                return R.drawable.jury_flag;

            case NOVEMBER:
                switch (size) {
                    case 1:
                        return R.drawable.flag_november_48dp;

                    case 3:
                        return R.drawable.flag_november_96dp;

                    default:
                        return R.drawable.flag_november_64dp;
                }

            case PAPA:
                switch (size) {
                    case 1:
                        return R.drawable.flag_papa_48dp;

                    case 3:
                        return R.drawable.flag_papa_96dp;

                    default:
                        return R.drawable.flag_papa_64dp;
                }

            case XRAY:
                switch (size) {
                    case 1:
                        return R.drawable.flag_xray_48dp;

                    case 3:
                        return R.drawable.flag_xray_96dp;

                    default:
                        return R.drawable.flag_xray_64dp;
                }

            case ZULU:
                switch (size) {
                    case 1:
                        return R.drawable.flag_zulu_48dp;

                    case 3:
                        return R.drawable.flag_zulu_96dp;

                    default:
                        return R.drawable.flag_zulu_64dp;
                }

            default:
                switch (size) {
                    case 1:
                        return R.drawable.flag_alpha_48dp;

                    case 3:
                        return R.drawable.flag_alpha_96dp;

                    default:
                        return R.drawable.flag_alpha_64dp;
                }
        }
    }

    public static Drawable getFlagDrawable(Context context, String flag, int size) {
        return context.getResources().getDrawable(FlagsResources.getResId(flag, size));
    }
}
