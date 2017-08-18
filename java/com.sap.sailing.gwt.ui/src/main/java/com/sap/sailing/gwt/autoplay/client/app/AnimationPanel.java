package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

public class AnimationPanel extends ComplexPanel {

    public static final int DELAY = 2000;

    public static final int ANIMATION_DURATION = 2000;

    interface Resources extends ClientBundle {
        @Source("AnimationPanel.gss")
        Styles style();
    }

    interface Styles extends CssResource {
        String root();

        String wrapper();

        String child();

        String old();

        String current();

        String next();
    }

    private static final Resources resources = GWT.create(Resources.class);
    
    private static final Styles style = resources.style();
    static {
        style.ensureInjected();
    }

    private DivElement currentWidgetHolder;
    private DivElement nextWidgetHolder;
    
    private Widget currentWidget;

    private Widget nextWidget;

    private final DivElement wrapper;

    private Timer showNextTimer = new Timer() {
        public void run() {
            currentWidgetHolder.removeClassName(style.current());
            currentWidgetHolder.addClassName(style.old());
            nextWidgetHolder.removeClassName(style.next());
            nextWidgetHolder.addClassName(style.current());

            removeOldTimer.schedule(ANIMATION_DURATION);
        }
    };

    private Timer removeOldTimer = new Timer() {
        public void run() {
            remove(currentWidget);
            currentWidgetHolder.removeFromParent();
            currentWidget = nextWidget;
            currentWidgetHolder = nextWidgetHolder;
            
            nextWidget = null;
            nextWidgetHolder = null;
        }
    };

    public AnimationPanel() {
        DivElement root = Document.get().createDivElement();
        root.addClassName(style.root());
        setElement(root);
        wrapper = Document.get().createDivElement();
        wrapper.addClassName(style.wrapper());
        root.appendChild(wrapper);
    }

    @Override
    public void add(final Widget child) {
        if(child == null) {
            return;
        }
        DivElement holder = Document.get().createDivElement();
        holder.addClassName(style.child());

        showNextTimer.cancel();
        removeOldTimer.cancel();

        if (currentWidget == null) {
            currentWidget = child;
            currentWidgetHolder = holder;
            holder.addClassName(style.current());
        } else {
            if (nextWidget != null) {
                remove(nextWidget);
                nextWidgetHolder.removeFromParent();
            }
            nextWidget = child;
            nextWidgetHolder = holder;
            holder.addClassName(style.next());

            showNextTimer.schedule(DELAY);
        }
        wrapper.appendChild(holder);
        add(child, holder);
    }

    @Override
    public boolean remove(Widget w) {
        boolean remove = super.remove(w);
        w.removeStyleName(style.child());
        w.removeStyleName(style.old());
        w.removeStyleName(style.current());
        w.removeStyleName(style.next());
        return remove;
    }

}
