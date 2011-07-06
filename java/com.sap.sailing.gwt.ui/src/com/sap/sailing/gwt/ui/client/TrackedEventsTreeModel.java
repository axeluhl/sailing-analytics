package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;

public class TrackedEventsTreeModel implements TreeViewModel {
    private final MultiSelectionModel<?> selectionModel;
    
    private final ListDataProvider<EventDAO> events;
    
    public TrackedEventsTreeModel(ListDataProvider<EventDAO> eventsList) {
        this.events = eventsList;
        selectionModel = new MultiSelectionModel<Object>();
    }
    
    @SuppressWarnings("unchecked")
    public <T> MultiSelectionModel<T> getSelectionModel() {
        return (MultiSelectionModel<T>) selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            return new DefaultNodeInfo<EventDAO>(events, new EventCell(),
                    getSelectionModel(), /* valueUpdater */ null);
        } else if (value instanceof EventDAO) {
            return new DefaultNodeInfo<RegattaDAO>(new ListDataProvider<RegattaDAO>(((EventDAO) value).regattas), new RegattaCell(),
                    getSelectionModel(), /* valueUpdater */ null);
        } else if (value instanceof RegattaDAO) {
            return new DefaultNodeInfo<RaceDAO>(new ListDataProvider<RaceDAO>(((RegattaDAO) value).races), new RaceCell(),
                    getSelectionModel(), /* valueUpdater */ null);
        } else if (value instanceof RaceDAO) {
            return null; // TODO do we need to return something more "useful" here?
        } else {
            throw new RuntimeException("Unknown object type in event tree: "+value+" ("+value.getClass().getName()+")");
        }
    }

    @Override
    public boolean isLeaf(Object value) {
        if (value == null) { // root
            return false;
        } else if (value instanceof EventDAO) {
            return false;
        } else if (value instanceof RegattaDAO) {
            return ((RegattaDAO) value).races.isEmpty();
        } else {
            return true;
        }
    }

    public Object getRoot() {
        return null;
    }
    
    private class EventCell extends AbstractCell<EventDAO> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, EventDAO value, SafeHtmlBuilder sb) {
            sb.appendEscaped(value.name);
        }
    }

    private class RegattaCell extends AbstractCell<RegattaDAO> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, RegattaDAO value, SafeHtmlBuilder sb) {
            sb.appendEscaped(value.boatClass.name);
        }
    }

    private class RaceCell extends AbstractCell<RaceDAO> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, RaceDAO value, SafeHtmlBuilder sb) {
            sb.appendEscaped(value.name);
        }
    }
}
