package com.sap.sse.common.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.sap.sse.common.impl.AbstractColor;
import com.sap.sse.common.impl.HSVColor;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util.Triple;

import junit.framework.Assert;

public class AbstractColorTest {

    @Test
    public void testShortRGBColor() {
        Color color = AbstractColor.getCssColor("#0");
        Assert.assertEquals("#000000", color.getAsHtml());
        color = AbstractColor.getCssColor("#00F");
        Assert.assertEquals("#0000FF", color.getAsHtml());
        color = AbstractColor.getCssColor("F");
        Assert.assertEquals("#0F0F0F", color.getAsHtml());
        color = AbstractColor.getCssColor("0F0");
        Assert.assertEquals("#00FF00", color.getAsHtml());
    }
    
    @Test
    public void testRGBColor() throws Exception {
        Color color = AbstractColor.getCssColor("rgb(255,255,255)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("rgb(1, 2, 3)");
        Assert.assertEquals("#010203", color.getAsHtml());
        color = AbstractColor.getCssColor("rgb(100%,100%,100%)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("rgb(255,255,255,777,354)");
        Assert.assertEquals(null, color);
        color = AbstractColor.getCssColor("rgb(255   ,255,255)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("rgb(3666 ,255,255)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("rgb(3666.98 ,255,255)");
        Assert.assertEquals(null, color);
    }

    @Test
    public void testRGBA() throws Exception {
        Color color = AbstractColor.getCssColor("rgba(255,255,255,0.3)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("rgba(255   ,255,255,90)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("rgba(3666 ,255,255,0.3)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("rgba(3666.98 ,255,255,0.777)");
        Assert.assertEquals(null, color);
    }

    @Test
    public void testHSL() throws Exception {
        Color color = AbstractColor.getCssColor("hsl(360, 100%, 100%)");
        Assert.assertEquals("#FF00FF", color.getAsHtml());
        color = AbstractColor.getCssColor("HSL(360,     1.0, 1.0)");
        Assert.assertEquals("#FF00FF", color.getAsHtml());
        color = AbstractColor.getCssColor("hsl(360,     1.0, 1.0,435,789)");
        Assert.assertEquals(null, color);
    }

    @Test
    public void testHSLA() throws Exception {
        Color color = AbstractColor.getCssColor("hsla(360, 100%, 100%,hjhb)");
        Assert.assertEquals("#FF00FF", color.getAsHtml());
        color = AbstractColor.getCssColor("HSLa(360,     1.0, 1.0,0.4)");
        Assert.assertEquals("#FF00FF", color.getAsHtml());
        color = AbstractColor.getCssColor("hsla(360,     1.0, 1.0,435,789)");
        Assert.assertEquals(null, color);
    }

    @Test
    public void testColorName() {
        Color color = AbstractColor.getCssColor("BrOwn");
        Assert.assertEquals(Color.BROWN, color);
        color = AbstractColor.getCssColor("caffee");
        Assert.assertEquals("#CAFFEE", color.getAsHtml());
    }
    
    @Test
    public void testRgbWithoutParentheses() {
        assertNull(AbstractColor.getCssColor("rgb"));
    }
    
    @Test
    public void testHSVConversion() {
        Color color = new RGBColor(5, 6, 7);
        Triple<Float, Float, Float> hsvValues = color.getAsHSV();
        HSVColor hsv = new HSVColor(hsvValues.getA(), hsvValues.getB(), hsvValues.getC());
        Triple<Integer, Integer, Integer> rgbValues = hsv.getAsRGB();
        assertEquals(5, (int) rgbValues.getA());
        assertEquals(6, (int) rgbValues.getB());
        assertEquals(7, (int) rgbValues.getC());
        // according to http://www.rapidtables.com/convert/color/rgb-to-hsv.htm rgb(5, 6, 7) equals
        // hsv(210, 0.286, 0.027)
        assertEquals(210.0, hsvValues.getA(), 0.01);
        assertEquals(0.286, hsvValues.getB(), 0.01);
        assertEquals(0.027, hsvValues.getC(), 0.01);
    }

}
