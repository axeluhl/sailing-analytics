package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.gwt.ui.client.LeaderboardPanel.LeaderboardTableResources;

/**
 * Uses {@link SortableColumn#getHeaderStyle()} to optionally add a custom CSS style to the &lt;th&gt; table
 * header cell for the respective column.<p>
 * 
 * The implementation works by overriding {@link #renderRowValues(SafeHtmlBuilder, List, int, SelectionModel)} which
 * first calls the super implementation which is expected to render the header cells already into the implementation
 * {@link Element}, then uses {@link #updateColumnHeaderStyles()} to traverse the DOM tree to the &lt;th&gt; elements
 * and {@link Element#addClassName(String) adding} the class name as specified by the column, if not <code>null</code>.
 * 
 * @author Axel Uhl (D043530)
 */
public class CellTableWithStylableHeaders<T> extends CellTable<T> {
    public CellTableWithStylableHeaders(int pageSize, LeaderboardTableResources resources) {
        super(pageSize, resources);
    }
    
    @Override
    protected void renderRowValues(SafeHtmlBuilder sb, List<T> values, int start,
            SelectionModel<? super T> selectionModel) {
        super.renderRowValues(sb, values, start, selectionModel);
        updateColumnHeaderStyles();
    }
    
    private void updateColumnHeaderStyles() {
        Element tableElement = getElement();
        Element thead = tableElement.getElementsByTagName("thead").getItem(0);
        Node header = thead.getChild(0); // single tr header row
        NodeList<Node> headerColumns = header.getChildNodes();
        for (int i=0; i<headerColumns.getLength(); i++) {
            Element headerCell = (Element) headerColumns.getItem(i);
            String headerStyle = ((SortableColumn<T, ?>) getColumn(i)).getHeaderStyle();
            if (headerStyle != null) {
                headerCell.addClassName(headerStyle);
            }
        }
    }
    
}
