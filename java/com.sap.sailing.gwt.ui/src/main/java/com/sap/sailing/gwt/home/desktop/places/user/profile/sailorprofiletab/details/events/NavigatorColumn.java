package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;
import static com.google.gwt.dom.client.BrowserEvents.TOUCHEND;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public abstract class NavigatorColumn<T> extends Column<T, String> {

    public NavigatorColumn() {
        super(new NavigatorCell());
    }

    private static class NavigatorCell extends AbstractSafeHtmlCell<String> {
        public NavigatorCell() {
            super(new SafeHtmlRenderer<String>() {

                @Override
                public SafeHtml render(String url) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    if (url != null) {
                        builder.appendHtmlConstant("<a href='");
                        builder.appendEscaped(url);
                        builder.appendHtmlConstant("'><img src='");
                        builder.appendHtmlConstant(
                                SharedHomeResources.INSTANCE.arrowDownGrey().getSafeUri().asString());
                        builder.appendHtmlConstant(
                                "' style='-webkit-transform: rotate(270deg);-moz-transform: rotate(270deg);"
                                        + "-ms-transform: rotate(270deg); transform: rotate(270deg);"
                                        + "width:1.33333333333em; cursor:pointer; min-width:1.33333333em'");
                        builder.appendHtmlConstant("/></a>");
                    }
                    return builder.toSafeHtml();
                }

                @Override
                public void render(String object, SafeHtmlBuilder builder) {
                    builder.append(render(object));
                }
            }, CLICK, KEYDOWN, TOUCHEND);
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
                ValueUpdater<String> valueUpdater) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(value);
            }
        }

        @Override
        protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
            if (data != null) {
                sb.append(data);
            }
        }
    }

}
