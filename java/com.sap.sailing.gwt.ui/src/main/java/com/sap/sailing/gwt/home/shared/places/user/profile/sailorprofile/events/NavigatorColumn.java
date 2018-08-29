package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events;

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

public abstract class NavigatorColumn<T> extends Column<T, Boolean> {

    public NavigatorColumn() {
        super(new NavigatorCell());
    }

    private static class NavigatorCell extends AbstractSafeHtmlCell<Boolean> {
        public NavigatorCell() {
            super(new SafeHtmlRenderer<Boolean>() {

                @Override
                public SafeHtml render(Boolean object) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    if (Boolean.TRUE.equals(object)) {
                        builder.appendHtmlConstant("<img src=\""
                                + SharedHomeResources.INSTANCE.arrowDownGrey().getSafeUri().asString() + "\" style=\"\n"
                                + "-webkit-transform: rotate(270deg);-moz-transform: rotate(270deg);"
                                + "-ms-transform: rotate(270deg);transform: rotate(270deg);width:1.33333333333em;cursor:pointer;min-width:1.33333333em\" /");
                    }
                    return builder.toSafeHtml();
                }

                @Override
                public void render(Boolean object, SafeHtmlBuilder builder) {
                    builder.append(render(object));
                }
            }, CLICK, KEYDOWN, TOUCHEND);
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, Boolean value, NativeEvent event,
                ValueUpdater<Boolean> valueUpdater) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(value);
            }
        }

        @Override
        protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
            sb.append(data);
        }
    }

}
