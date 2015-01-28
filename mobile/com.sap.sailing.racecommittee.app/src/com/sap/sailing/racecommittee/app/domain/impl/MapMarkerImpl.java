package com.sap.sailing.racecommittee.app.domain.impl;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.MapMarker;
import com.sap.sse.common.Color;

import java.util.ArrayList;
import java.util.UUID;

//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.ColorFilter;
//import android.graphics.ColorMatrix;
//import android.graphics.ColorMatrixColorFilter;
//import android.graphics.PorterDuff;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.util.Log;
//import com.sap.sailing.android.shared.logging.ExLog;
//import com.sap.sailing.domain.common.impl.RGBColor;

public class MapMarkerImpl implements MapMarker {

    private String name;
    private UUID id;
    private ArrayList<TrackMeasurement> track;
    private Marker marker;

    public MapMarkerImpl(String name, UUID id, ArrayList<TrackMeasurement> track) {
        this.name = name;
        this.id = id;
        this.track = track;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArrayList<TrackMeasurement> getTrack() {
        return track;
    }

    @Override
    public MarkType getType() {
        return MarkType.BUOY;
    }

    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public Color getColor() {
        String name = this.name.toLowerCase();
        if (name.startsWith("white")) {
            return Color.WHITE;
        } else if (name.startsWith("gray")) {
            return Color.GRAY;
        } else if (name.startsWith("black")) {
            return Color.BLACK;
        } else if (name.startsWith("yellow")) {
            return Color.YELLOW;
        } else if (this.name.startsWith("pink")) {
            return Color.PINK;
        } else if (this.name.startsWith("orange")) {
            return Color.ORANGE;
        } else if (this.name.startsWith("green")) {
            return Color.GREEN;
        } else if (this.name.startsWith("magenta")) {
            return Color.MAGENTA;
        } else if (this.name.startsWith("cyan")) {
            return Color.CYAN;
        } else {
            return Color.ORANGE;
        }
    }


    @Override
    public Marker getMarker() {
        return marker;
    }

    @Override
    public void setMarker(Marker m) {
        this.marker = m;
    }

    @Override
    public BitmapDescriptor getMarkerIcon(Context context) {
        String name = this.name.toLowerCase();
        // buoys
        if (name.startsWith("white")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_white_grey);
        } else if (name.startsWith("gray")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_grey);
        } else if (name.startsWith("black cone")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black_cone);
        } else if (name.startsWith("black cyl")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black_cyl);
        } else if (name.startsWith("black")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black);
        } else if (name.startsWith("yellow")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_yellow);
        } else if (name.startsWith("orange")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_dark_orange);
        } else if (name.startsWith("green")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_green);
        } else if (name.startsWith("red")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_red);


        } else if (name.startsWith("finish")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_finish_flag);
        } else {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_buoy);
        }

//         ALTERNATIVE METHOD THAT LETS YOU SET THE COLOR IN CODE ( BUT SOMEHOW PHUQS UP )
//        Drawable toFill = context.getResources().getDrawable(R.drawable.buoy_innards);
//
//
//        //toFill.setColorFilter( android.graphics.Color.RED, PorterDuff.Mode.SRC_ATOP);
//        toFill.setColorFilter(adjustHue(android.graphics.Color.RED));
//
//        Bitmap body    = BitmapFactory.decodeResource(context.getResources(), R.drawable.buoy_border);
//        Bitmap innards = drawableToBitmap(toFill);
//
//        Log.d("TAG",getColor().getAsHtml());
//
//        Bitmap complete = Bitmap.createBitmap(body.getWidth(), body.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(complete);
//        canvas.drawBitmap(body, 0, 0, null);
//        canvas.drawBitmap(innards, 0, 0, null);
//
//        return BitmapDescriptorFactory.fromBitmap(complete);
    }
}


//    public static Bitmap drawableToBitmap (Drawable drawable) {
//        if (drawable instanceof BitmapDrawable) {
//            return ((BitmapDrawable)drawable).getBitmap();
//        }
//
//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//
//        return bitmap;
//    }
//
//    /**
//     * Creates a HUE ajustment ColorFilter
//     * @param value degrees to shift the hue.
//     * @return
//     */
//    public static ColorFilter adjustHue( float value )
//    {
//        ColorMatrix cm = new ColorMatrix();
//
//        adjustHue(cm, value);
//
//        return new ColorMatrixColorFilter(cm);
//    }
//
//    /**
//     * @param cm
//     * @param value
//     */
//    public static void adjustHue(ColorMatrix cm, float value)
//    {
//        value = cleanValue(value, 180f) / 180f * (float) Math.PI;
//        if (value == 0)
//        {
//            return;
//        }
//        float cosVal = (float) Math.cos(value);
//        float sinVal = (float) Math.sin(value);
//        float lumR = 0.213f;
//        float lumG = 0.715f;
//        float lumB = 0.072f;
//        float[] mat = new float[]
//                {
//                        lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
//                        lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
//                        lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
//                        0f, 0f, 0f, 1f, 0f,
//                        0f, 0f, 0f, 0f, 1f };
//        cm.postConcat(new ColorMatrix(mat));
//    }
//
//    protected static float cleanValue(float p_val, float p_limit)
//    {
//        return Math.min(p_limit, Math.max(-p_limit, p_val));
//    }
//