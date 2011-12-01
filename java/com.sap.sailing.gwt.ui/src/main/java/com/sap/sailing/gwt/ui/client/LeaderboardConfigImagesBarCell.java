package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class LeaderboardConfigImagesBarCell extends ImagesBarCell {

    interface ImagesBarTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div name=\"{0}\" style=\"{1}\">{2}</div>")
        SafeHtml cell(String name, SafeStyles styles, SafeHtml value);
    }

    public LeaderboardConfigImagesBarCell() {
        super();
    }

    public LeaderboardConfigImagesBarCell(SafeHtmlRenderer<String> renderer) {
        super();
    }

    private static ImagesBarTemplates templates = GWT.create(ImagesBarTemplates.class);

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private static final SafeHtml ICON_EDIT = makeImage(resources.editIcon());

    private static final SafeHtml ICON_OPEN_BROWSER = makeImage(resources.openBrowserIcon());

    private static final SafeHtml ICON_REMOVE = makeImage(resources.removeIcon());

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

        SafeHtml rendered = templates.cell("ACTION_EDIT", imgStyle, ICON_EDIT);
        sb.append(rendered);

        rendered = templates.cell("ACTION_OPEN_BROWSER", imgStyle, ICON_OPEN_BROWSER);
        sb.append(rendered);

        rendered = templates.cell("ACTION_REMOVE", imgStyle, ICON_REMOVE);
        sb.append(rendered);
    }
}