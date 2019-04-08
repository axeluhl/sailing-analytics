package com.sap.sailing.gwt.ui.adminconsole.resulthandling;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.adminconsole.resulthandling.AbstractDataImportResponse.ErrorMessage;

public class AbstractDataImportResultDialog {

    protected static final String DIV_BOLD = "<div style='font-weight:bold;'>", DIV_END = "</div>";
    protected static final String DIV_MARGIN = "<div style='margin:40px;'>", UL = "<ul>", UL_END = "</ul>";
    protected static final String LI = "<li>", LI_END = "</liv>", P = "<p>", P_END = "</p>";

    protected static void appendErrorMessagesIfPresent(SafeHtmlBuilder builder, AbstractDataImportResponse response) {
        if (response.hasErrors()) {
            appendParagraph(builder, "Error messages");
            response.getErrors().forEach(errorMessage -> appendErrorMessage(builder, errorMessage));
        }
    }

    protected static void appendErrorMessage(SafeHtmlBuilder builder, ErrorMessage errorMessage) {
        builder.appendHtmlConstant(DIV_BOLD).appendEscaped(errorMessage.getMessage()).appendHtmlConstant(DIV_END);
        builder.appendHtmlConstant(UL);
        appendLineItemIfValueNotNull(builder, "ServerLog UUID", errorMessage.getExUUID());
        appendLineItemIfValueNotNull(builder, "Exception Classname", errorMessage.getClassName());
        appendLineItemIfValueNotNull(builder, "Filename", errorMessage.getFilename());
        appendLineItemIfValueNotNull(builder, "Requested Importer", errorMessage.getRequestedImporter());
        builder.appendHtmlConstant(UL_END);
    }

    protected static void appendParagraph(SafeHtmlBuilder builder, String text) {
        builder.appendHtmlConstant(P).appendEscaped(text).appendHtmlConstant(P_END);
    }
    
    protected static void appendLineItem(SafeHtmlBuilder builder, String text) {
        builder.appendHtmlConstant(LI).appendEscaped(text).appendHtmlConstant(LI_END);
    }

    protected static void appendLineItemIfValueNotNull(SafeHtmlBuilder builder, String label, String value) {
        if (value != null && !value.isEmpty()) {
            builder.appendHtmlConstant(LI);
            builder.appendEscaped(label).appendEscaped(": ").appendEscaped(value);
            builder.appendHtmlConstant(LI_END);
        }
    }
    
    protected static void show(SafeHtmlBuilder builder) {
        final DialogBox errorBox = new DialogBox(false, true);
        final VerticalPanel content = new VerticalPanel();
        content.add(new HTML(builder.toSafeHtml()));
        final Button close = new Button("Close", (ClickHandler) e -> errorBox.hide());
        content.add(close);
        content.setSpacing(10);
        errorBox.setGlassEnabled(true);
        errorBox.setWidget(content);
        errorBox.center();
    }

}
