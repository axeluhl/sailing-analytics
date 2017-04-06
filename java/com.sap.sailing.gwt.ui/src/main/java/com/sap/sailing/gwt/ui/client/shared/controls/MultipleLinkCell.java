package com.sap.sailing.gwt.ui.client.shared.controls;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.MOUSEDOWN;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 * Represent cell with multiple links. Link is represented as static nested class {@link CellLink}.
 * <p>
 * There are two behaviors depending on field isBlockLinkEvent:
 * <ul>
 * <li>If isBlockLinkEvent is true then click on link would work depending on onLinkClickHandler.</li>
 * <li>If isBlockLinkEvent is false then link was work as real link.</li>
 * </ul>
 * 
 * @author Alexander Tatarinovich (C5237435)
 */
public class MultipleLinkCell extends AbstractCell<List<MultipleLinkCell.CellLink>> {

    private ValueUpdater<String> onLinkClickHandler;

    private boolean isBlockLinkEvent = false;

    /**
     * Represent Link for cell.
     * <p>
     * There are three fields which use in template {@link LinkTemplates} for link.
     */
    public static class CellLink {
        private final String target;
        private final String href;
        private final String text;

        public String getTarget() {
            return target;
        }

        public String getHref() {
            return href;
        }

        public String getText() {
            return text;
        }

        public CellLink(String text) {
            super();
            this.text = text;
            this.href = "";
            this.target = "";
        }

        public CellLink(String target, String href, String text) {
            super();
            this.target = target;
            this.href = href;
            this.text = text;
        }
    }

    /**
     * Template for link.
     * <p>
     * &lt;a target=\"{Target}\" href=\"{Href}\"&gt{Text}&lt/a&gt.
     */
    interface LinkTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a target=\"{0}\" href=\"{1}\">{2}</a>")
        SafeHtml cell(String target, SafeUri href, String text);
    }

    private static LinkTemplates linkTemplates = GWT.create(LinkTemplates.class);

    public MultipleLinkCell() {
        super(CLICK, MOUSEDOWN);
    }

    public MultipleLinkCell(boolean isBlockLinkEvent) {
        super(CLICK, MOUSEDOWN);
        this.isBlockLinkEvent = isBlockLinkEvent;
    }

    public ValueUpdater<String> getOnLinkClickHandler() {
        return onLinkClickHandler;
    }

    public void setOnLinkClickHandler(ValueUpdater<String> handler) {
        this.onLinkClickHandler = handler;
    }

    public boolean isBlockLinkEvent() {
        return isBlockLinkEvent;
    }

    public void setBlockLinkEvent(boolean isBlockLinkEvent) {
        this.isBlockLinkEvent = isBlockLinkEvent;
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, List<MultipleLinkCell.CellLink> value,
            NativeEvent event, ValueUpdater<List<MultipleLinkCell.CellLink>> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        if (isBlockLinkEvent) {
            event.preventDefault();
            EventTarget eventTarget = event.getEventTarget();
            if (parent.isOrHasChild(Element.as(eventTarget))) {
                Element el = Element.as(eventTarget);
                if (el.getNodeName().equalsIgnoreCase("a")) {
                    onLinkClickHandler.update(el.getInnerText());
                }
            }
        }
    };

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, List<MultipleLinkCell.CellLink> links,
            SafeHtmlBuilder sb) {
        if (links.isEmpty()) {
            return;
        }
        SafeHtml rendered;
        boolean isFirst = true;
        for (CellLink link : links) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.appendHtmlConstant("<br>");
            }
            rendered = linkTemplates.cell(link.getTarget(), UriUtils.fromString(link.getHref()), link.getText());
            sb.append(rendered);
        }
    }

}
