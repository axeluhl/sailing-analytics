package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LeaderboardGroupConfigImagesBarCell extends ImagesBarCell {

    public LeaderboardGroupConfigImagesBarCell(StringMessages stringConstants) {
        super();
        this.stringConstants = stringConstants;
    }

    public LeaderboardGroupConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringConstants) {
        super();
        this.stringConstants = stringConstants;
    }

    private static ImagesBarTemplates templates = GWT.create(ImagesBarTemplates.class);

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private static final SafeHtml ICON_EDIT = makeImage(resources.editIcon());

    private static final SafeHtml ICON_REMOVE = makeImage(resources.removeIcon());

    private StringMessages stringConstants;
    
    @Override
    protected void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
        /*
         * Always do a null check on the value. Cell widgets can pass null to
         * cells if the underlying data contains a null, or if the data arrives
         * out of order.
         */
        if (data == null) {
            return;
        }

        SafeStyles imgStyle = SafeStylesUtils.fromTrustedString("float:left;cursor:hand;cursor:pointer;padding-right:5px;");

        SafeHtml rendered = templates.cell("ACTION_EDIT", imgStyle, stringConstants.actionEdit(), ICON_EDIT);
        sb.append(rendered);

        rendered = templates.cell("ACTION_REMOVE", imgStyle, stringConstants.actionRemove(), ICON_REMOVE);
        sb.append(rendered);
    }
}