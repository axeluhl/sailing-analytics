package com.sap.sse.common.impl.test;

import org.junit.Test;

import com.sap.sse.common.impl.AbstractColor;
import com.sap.sse.common.Color;

import junit.framework.Assert;

public class AbstractColorTest {

    @Test
    public void testRGBColor() throws Exception {
        Color color = AbstractColor.getCssColor("rgb(255,255,255)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
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

    public void testHSL() throws Exception {
        Color color = AbstractColor.getCssColor("hsl(360, 100%, 100%)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("HSL(360,     1.0, 1.0)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("hsl(360,     1.0, 1.0,435,789)");
        Assert.assertEquals(null, color);
    }

    public void testHSLA() throws Exception {
        Color color = AbstractColor.getCssColor("hsla(360, 100%, 100%,hjhb)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("HSLa(360,     1.0, 1.0,0.4)");
        Assert.assertEquals("#FFFFFF", color.getAsHtml());
        color = AbstractColor.getCssColor("hsla(360,     1.0, 1.0,435,789)");
        Assert.assertEquals(null, color);
    }

    public void testColorName() {
        Color color = AbstractColor.getCssColor("BrOwn");
        Assert.assertEquals(Color.BROWN, color);
        color = AbstractColor.getCssColor("caffee");
        Assert.assertEquals("#CAFFEE", color.getAsHtml());
    }

}
