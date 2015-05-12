package com.sap.sailing.gwt.ui.regattaoverview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;

public class SailingFlagsBuilder {
    
    private final static SailingFlagsTemplates imageTemplate = GWT.create(SailingFlagsTemplates.class);
    private final static FlagImageResolver flagImageResolver = new FlagImageResolver();
    
    interface SailingFlagsTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div title=\"{0}\">{1}</div>")
        SafeHtml cell(String title, SafeHtml content);

        @SafeHtmlTemplates.Template("<div style=\"{0}\">{1}</div>")
        SafeHtml cell(SafeStyles styles, SafeHtml content);
    }
    
    public static SafeHtml render(Flags upperFlag, Flags lowerFlag, boolean isDisplayed, boolean displayStateChanged, String tooltip) {
        return render(upperFlag, lowerFlag, isDisplayed, displayStateChanged, 1, tooltip);
    }

    public static SafeHtml render(Flags upperFlag, Flags lowerFlag, boolean isDisplayed, boolean isDisplayedChanged,
            double scale, String tooltip) {
        SafeHtmlBuilder contentBuilder = new SafeHtmlBuilder();
        SafeHtmlBuilder flagsBuilder = new SafeHtmlBuilder();
        flagsBuilder.append(getFlagBackgroundImage(upperFlag, isDisplayed, isDisplayedChanged, scale));
        flagsBuilder.append(getFlagBackgroundImage(lowerFlag, isDisplayed, isDisplayedChanged, scale));

        SafeStylesBuilder flagStyleBuilder = new SafeStylesBuilder();
        flagStyleBuilder.display(Display.INLINE_BLOCK);
        flagStyleBuilder.verticalAlign(VerticalAlign.TOP);
        flagStyleBuilder.paddingRight(10 * scale, Unit.PX);
        contentBuilder.append(imageTemplate.cell(flagStyleBuilder.toSafeStyles(), flagsBuilder.toSafeHtml()));

        contentBuilder.append(getArrowBackgroundImage(isDisplayed, isDisplayedChanged, scale));
        return imageTemplate.cell(tooltip, contentBuilder.toSafeHtml());
    }

    public static SafeHtml render(FlagStateDTO flagState, double scale, String tooltip) {
        return render(flagState.getLastUpperFlag(), flagState.getLastLowerFlag(), flagState.isLastFlagsAreDisplayed(),
                flagState.isLastFlagsDisplayedStateChanged(), scale, tooltip);
    }

    private static SafeHtml getFlagBackgroundImage(Flags flag, boolean displayed, boolean displayedChanged, double scale) {
        ImageResource flagImage = flagImageResolver.resolveFlagToImage(flag, displayed, displayedChanged);
        return getBackgroundImage(flagImage, scale, new SafeStylesBuilder());
    }

    private static SafeHtml getArrowBackgroundImage(boolean displayed, boolean displayedChanged, double scale) {
        ImageResource flagImage = flagImageResolver.resolveFlagDirectionToImage(displayed, displayedChanged);
        SafeStylesBuilder arrowStyleBuilder = new SafeStylesBuilder();
        arrowStyleBuilder.display(Display.INLINE_BLOCK);
        arrowStyleBuilder.verticalAlign(VerticalAlign.TOP);
        return getBackgroundImage(flagImage, scale, arrowStyleBuilder);
    }

    private static SafeHtml getBackgroundImage(ImageResource imageResource, double scale,
            SafeStylesBuilder stylesBuilder) {
        if (imageResource != null) {
            stylesBuilder.backgroundImage(imageResource.getSafeUri());
            stylesBuilder.appendTrustedString("background-size: contain;");
            stylesBuilder.appendTrustedString("background-repeat: no-repeat;");
            stylesBuilder.width(imageResource.getWidth() * scale, Unit.PX);
            stylesBuilder.height(imageResource.getHeight() * scale, Unit.PX);
            return imageTemplate.cell(stylesBuilder.toSafeStyles(), SafeHtmlUtils.EMPTY_SAFE_HTML);
        }
        return SafeHtmlUtils.EMPTY_SAFE_HTML;
    }

}
