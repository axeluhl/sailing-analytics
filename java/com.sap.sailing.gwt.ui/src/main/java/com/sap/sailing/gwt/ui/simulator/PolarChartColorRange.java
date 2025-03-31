package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;

public class PolarChartColorRange {
    private int k;
    private int n;
    private float[] h;
    private double[] s;
    private double[] v;

    public PolarChartColorRange(int Nr) {
        n = Nr;
        k = n / 2;
        h = new float[3];
        h[0] = Double.valueOf(4 / 12.0).floatValue();
        h[1] = Double.valueOf(2 / 12.0).floatValue();
        h[2] = Double.valueOf(0 / 12.0).floatValue();
        s = new double[] { 1.0, 1.0, 1.0 };
        v = new double[] { 0.5, 0.9, 0.75 };
    }

    private float[] GenerateSeq(float start, float finish, int n) {
        float[] f = new float[n];
        f[0] = start;
        f[n - 1] = finish;
        for (int i = 1; i < n - 1; i++) {
            f[i] = start + (finish - start) / (n - 1) * (i);
        }
        return f;
    }

    public ArrayList<String> GetColors() {
    	
        ArrayList<String> result = new ArrayList<String>();

        for (int i = 0; i < 2; i++) {
            float[] hs = GenerateSeq(h[i], h[i + 1], k);
            float[] ss = GenerateSeq(Double.valueOf(s[i]).floatValue(), Double.valueOf(s[i + 1]).floatValue(), k);
            float[] vs = GenerateSeq(Double.valueOf(v[i]).floatValue(), Double.valueOf(v[i + 1]).floatValue(), k);
            
            for (int j = 0; j < k; j++) {
            	
            	String colorAsHexString = Integer.toHexString(HSBtoRGB(hs[j], ss[j], vs[j]));
                
                result.add("#" + colorAsHexString.substring(2, colorAsHexString.length()));
            }
        }
        return result;
    }
    
    public static int HSBtoRGB(float hue, float saturation, float brightness) {
    	int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} 
		else {
		    float h = (hue - (float)Math.floor(hue)) * 6.0f;
		    float f = h - (float)java.lang.Math.floor(h);
		    float p = brightness * (1.0f - saturation);
		    float q = brightness * (1.0f - saturation * f);
		    float t = brightness * (1.0f - (saturation * (1.0f - f)));
		    
		    switch ((int) h) {
			    case 0: {
			    	r = (int) (brightness * 255.0f + 0.5f);
			    	g = (int) (t * 255.0f + 0.5f);
			    	b = (int) (p * 255.0f + 0.5f);
			    	break;
			    }
			    case 1: {
			    	r = (int) (q * 255.0f + 0.5f);
			    	g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (p * 255.0f + 0.5f);
					break;
			    }
			    case 2: {
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (brightness * 255.0f + 0.5f);
					b = (int) (t * 255.0f + 0.5f);
					break;
			    }
			    case 3: {
					r = (int) (p * 255.0f + 0.5f);
					g = (int) (q * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
					break;
			    }
			    case 4: {
					r = (int) (t * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (brightness * 255.0f + 0.5f);
					break;
			    }
			    case 5: {
					r = (int) (brightness * 255.0f + 0.5f);
					g = (int) (p * 255.0f + 0.5f);
					b = (int) (q * 255.0f + 0.5f);
					break;
			    }
		    }
		}
		return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
	}
}
