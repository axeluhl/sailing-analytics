package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public class FlagImageRenderer {
    private static final int DEFAULT_FLAG_HEIGHT = 12;
    private static final int DEFAULT_FLAG_WIDTH = 18;
    private static final Template FLAG_RENDERER_TEMPLATE = GWT.create(Template.class);

    interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div style='{0}'></div>")
        SafeHtml image(SafeStyles style);

        @SafeHtmlTemplates.Template("<div title='{1}' style='{0}'></div>")
        SafeHtml imageWithTitle(SafeStyles style, String title);
    }

    private FlagImageRenderer() {
    }

    public static SafeHtml image(String url) {
        return FLAG_RENDERER_TEMPLATE.image(getStyle(DEFAULT_FLAG_WIDTH, DEFAULT_FLAG_HEIGHT, url));
    }

    private static SafeStyles getStyle(int width, int height, String backgroundImage) {
        return new SafeStylesBuilder().verticalAlign(VerticalAlign.MIDDLE)
                .append(SafeStylesUtils.fromTrustedNameAndValue("background-repeat", "no-repeat"))
                .append(SafeStylesUtils.fromTrustedNameAndValue("background-size", "contain"))
                .display(Display.INLINE_BLOCK).width(width, Unit.PX).height(height, Unit.PX)
                .trustedBackgroundImage("url(" + backgroundImage + ")").toSafeStyles();
    }

    public static SafeHtml imageWithTitle(String flagImageURL, String name) {
        return FLAG_RENDERER_TEMPLATE.imageWithTitle(getStyle(DEFAULT_FLAG_WIDTH, DEFAULT_FLAG_HEIGHT, flagImageURL),
                name);
    }
}
