package com.sap.sailing.dashboards.gwt.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class ActionPanel extends AbsolutePanel {

    private List<ActionPanelListener> listeners;
    private List<Integer> eventsList;

    public ActionPanel(int... events) {
        super();
        listeners = new ArrayList<ActionPanelListener>();
        eventsList = new ArrayList<Integer>();
        for (int event : events) {
            sinkEvents(event);
            eventsList.add(Integer.valueOf(event));
        }
        this.getElement().setAttribute("style", "-webkit-tap-highlight-color: rgba(0,0,0,0);");
    }

    public void enable() {
        for(Integer eventAsInteger : eventsList) {
            sinkEvents(eventAsInteger.intValue());
        }
    }
    
    public void disable() {
        for(Integer eventAsInteger : eventsList) {
            unsinkEvents(eventAsInteger.intValue());
        }
    }

    public void addActionPanelListener(ActionPanelListener actionPanelListener) {
        listeners.add(actionPanelListener);
    }

    public void removeActionPanelListener(ActionPanelListener actionPanelListener) {
        listeners.remove(actionPanelListener);
    }

    private void notifyActionPanelListenerAboutTriggeredEvent(Event event) {
        for (ActionPanelListener actionPanelListener : listeners) {
            actionPanelListener.eventTriggered(event);
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        notifyActionPanelListenerAboutTriggeredEvent(event);
        super.onBrowserEvent(event);
    }

    public interface ActionPanelListener {
        public void eventTriggered(Event event);
    }
}
