package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.events;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public class NavigatorColumn<T> extends Column<T, String> {

    public NavigatorColumn() {
        super(new NavigatorCell());
    }

    @Override
    public String getValue(T object) {
        return null;
    }

    private static class NavigatorCell extends ClickableTextCell {
        @Override
        public void render(Context context, String object, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<img src=\"" + SharedHomeResources.INSTANCE.arrowDownGrey().getSafeUri().asString()
                    + "\" style=\"\n" + "-webkit-transform: rotate(270deg);-moz-transform: rotate(270deg);"
                    + "-ms-transform: rotate(270deg);transform: rotate(270deg);width:1.33333333333em;cursor:pointer;\" /");
        };
    }

}
