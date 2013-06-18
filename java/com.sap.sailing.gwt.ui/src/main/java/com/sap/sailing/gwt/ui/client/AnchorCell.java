package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;

/**
 * A GWT cell rendering a simple anchor.
 * @author C5163874
 *
 */
public class AnchorCell extends AbstractCell<Anchor> {
    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Anchor a, SafeHtmlBuilder sb) {
        if (a.getHref() != null && a.getHref().trim().length() > 0 && a.isEnabled()) {
            sb.append(SafeHtmlUtils.fromTrustedString("<a target=\"_blank\" href=\""));
            sb.append(SafeHtmlUtils.fromTrustedString(a.getHref()));
            sb.append(SafeHtmlUtils.fromTrustedString("\">"));
            sb.append(SafeHtmlUtils.fromTrustedString(a.getText()));
            sb.append(SafeHtmlUtils.fromTrustedString("</a>"));
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString(a.getText()));
        }
    }
}
