package com.sap.sse.gwt.client.shared.components;

import java.util.ArrayList;

public class ComponentPathDeterminer {
    private ComponentPathDeterminer() {
    }

    /**
     * Helper that generates a path array, based on the parent's in a component, can probably be removed one java8 can
     * be used, with a default implementation in component
     * 
     * @param abstractComponent
     * @return
     */
    static ArrayList<String> determinePath(Component<?> start) {
        ArrayList<String> path = new ArrayList<>();
        Component<?> cur = start;
        while (cur != null) {
            path.add(cur.getId());
            cur = cur.getParentComponent();
        }
        return path;
    }
}
