package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell that is clickable and renders its contents as HTML.
 * Especially useful if you want to render a ClickableTextCell
 * in a CellTable as an anchor.
 * 
 * Use it like this:
 * <pre>
 *         Column<RegattaOverviewEntryDTO, SafeHtml> raceStatusColumn = new Column<RegattaOverviewEntryDTO, SafeHtml>(new ClickableSafeHtmlCell()) {
 *           @Override
 *           public SafeHtml getValue(final RegattaOverviewEntryDTO entryDTO) {
 *               String status = "my string";
 *               SafeHtmlBuilder sb = new SafeHtmlBuilder();
 *               sb.appendHtmlConstant("<a>");
 *               sb.appendEscaped(status);
 *               sb.appendHtmlConstant("</a>");
 *               return sb.toSafeHtml();
 *           }
 *       };
 * </pre>
 * 
 * @author Simon Pamies
 */
public class ClickableSafeHtmlCell extends AbstractCell<SafeHtml> {

    public ClickableSafeHtmlCell() {
        super("click", "keydown");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, SafeHtml value, NativeEvent event,
            ValueUpdater<SafeHtml> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if ("click".equals(event.getType())) {
            onEnterKeyDown(context, parent, value, event, valueUpdater);
        }
    }

    @Override
    protected void onEnterKeyDown(Context context, Element parent, SafeHtml value,
            NativeEvent event, ValueUpdater<SafeHtml> valueUpdater) {
        if (valueUpdater != null) {
            valueUpdater.update(value);
        }
    }

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
            sb.append(value);
        }
    }

}
