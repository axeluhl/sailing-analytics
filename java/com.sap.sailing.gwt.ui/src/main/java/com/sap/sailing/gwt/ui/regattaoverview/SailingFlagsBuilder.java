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
        return render(upperFlag, lowerFlag, isDisplayed, displayStateChanged, 0.75, tooltip);
    }

    public static SafeHtml render(Flags upperFlag, Flags lowerFlag, boolean isDisplayed, boolean isDisplayedChanged,
            double scale, String tooltip) {
        Scales scales = new Scales(scale, upperFlag, lowerFlag);
        SafeHtmlBuilder contentBuilder = new SafeHtmlBuilder();
        SafeHtmlBuilder flagsBuilder = new SafeHtmlBuilder();
        flagsBuilder.append(getFlagBackgroundImage(upperFlag, isDisplayed, isDisplayedChanged, scales));
        flagsBuilder.append(getFlagBackgroundImage(lowerFlag, isDisplayed, isDisplayedChanged, scales));

        SafeStylesBuilder flagStyleBuilder = new SafeStylesBuilder();
        flagStyleBuilder.display(Display.INLINE_BLOCK);
        flagStyleBuilder.verticalAlign(VerticalAlign.TOP);
        flagStyleBuilder.paddingRight(10 * scales.defaultScale, Unit.PX);
        contentBuilder.append(imageTemplate.cell(flagStyleBuilder.toSafeStyles(), flagsBuilder.toSafeHtml()));

        contentBuilder.append(getArrowBackgroundImage(isDisplayed, isDisplayedChanged, scales));
        return imageTemplate.cell(tooltip, contentBuilder.toSafeHtml());
    }

    public static SafeHtml render(FlagStateDTO flagState, double scale, String tooltip) {
        return render(flagState.getLastUpperFlag(), flagState.getLastLowerFlag(), flagState.isLastFlagsAreDisplayed(),
                flagState.isLastFlagsDisplayedStateChanged(), scale, tooltip);
        // return render(Flags.AP, Flags.ALPHA, true, true, scale, tooltip);
    }

    private static SafeHtml getFlagBackgroundImage(Flags flag, boolean displayed, boolean displayedChanged,
            Scales scales) {
        ImageResource flagImage = flagImageResolver.resolveFlagToImage(flag, displayed, displayedChanged);
        return getBackgroundImage(flagImage, scales.flagsScale, new SafeStylesBuilder());
    }

    private static SafeHtml getArrowBackgroundImage(boolean displayed, boolean displayedChanged, Scales scales) {
        ImageResource flagImage = flagImageResolver.resolveFlagDirectionToImage(displayed, displayedChanged);
        SafeStylesBuilder arrowStyleBuilder = new SafeStylesBuilder();
        arrowStyleBuilder.display(Display.INLINE_BLOCK);
        arrowStyleBuilder.verticalAlign(VerticalAlign.TOP);
        arrowStyleBuilder.marginTop(flagImage.getHeight() * scales.arrowPadding, Unit.PX);
        return getBackgroundImage(flagImage, scales.arrowScale, arrowStyleBuilder);
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

    private static class Scales {
        private final double arrowScale, arrowPadding, flagsScale, defaultScale;

        private Scales(double defaultScale, Flags upperFlag, Flags lowerFlag) {
            this.defaultScale = defaultScale;
            boolean singleFlag = isSingleFlag(defaultScale, upperFlag, lowerFlag);
            this.arrowScale = defaultScale * (singleFlag ? 1.5 : 1.5);
            this.arrowPadding = defaultScale * (singleFlag ? 0 : 0.25);
            this.flagsScale = defaultScale * (singleFlag ? 1.5 : 1);
        }

        private boolean isSingleFlag(double defaultScale, Flags upperFlag, Flags lowerFlag) {
            return (upperFlag == null || upperFlag == Flags.NONE || lowerFlag == null || lowerFlag == Flags.NONE);
        }

    }

}
