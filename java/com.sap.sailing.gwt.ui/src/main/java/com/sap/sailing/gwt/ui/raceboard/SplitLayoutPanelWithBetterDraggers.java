package com.sap.sailing.gwt.ui.raceboard;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class SplitLayoutPanelWithBetterDraggers extends SplitLayoutPanel {
    protected SplitLayoutPanelWithBetterDraggers() {
        super();
    }

    protected SplitLayoutPanelWithBetterDraggers(int splitterSize) {
        super(splitterSize);
    }

    @Override
    public void insert(Widget child, Direction direction, double size, Widget before) {
        Set<Widget> oldChildren = new HashSet<>();
        for (Widget oldChild : getChildren()) {
            oldChildren.add(oldChild);
        }
        super.insert(child, direction, size, before);
        Set<Widget> newChildren = new HashSet<>();
        for (Widget newChild : getChildren()) {
            newChildren.add(newChild);
        }
        newChildren.removeAll(oldChildren);
        for (Widget newChild : newChildren) {
            if (newChild != child) {
                // this has to be the splitter
                DockLayoutPanel.LayoutData layoutData = (LayoutData) newChild.getLayoutData();
                Element container = layoutData.layer.getContainerElement();
                container.getStyle().setOverflow(Overflow.VISIBLE);
                container.addClassName(getSplitterClassName(direction));
            }
        }
    }
    
    private String getSplitterClassName(Direction direction) {
        final String result;
        if (isHorizontal(direction)) {
            result = "SplitLayoutPanel-Divider-Horizontal";
        } else {
            result = "SplitLayoutPanel-Divider-Vertical";
        }
        return result;
    }

    private boolean isHorizontal(Direction direction) {
        switch (getResolvedDirection(direction)) {
        case WEST:
        case EAST:
            return true;
        case NORTH:
        case SOUTH:
            return false;
        default:
            throw new RuntimeException("Unexpected direction: "+direction);
        }
    }
}
