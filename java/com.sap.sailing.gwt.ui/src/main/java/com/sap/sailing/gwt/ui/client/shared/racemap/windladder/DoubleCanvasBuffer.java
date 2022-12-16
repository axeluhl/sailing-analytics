package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import java.util.function.Consumer;

import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;

public abstract class DoubleCanvasBuffer<T extends FullCanvasOverlay> {
    protected static final int BUFFER_SIZE = 2;

    protected T[] buffer;
    protected int activeBuffer = 0;

    protected void apply(Consumer<T> function) {
        for (T canvas : this.buffer) {
            function.accept(canvas);
        }
    }

    public void swap() {
        this.setVisible(false);
        this.activeBuffer = nextCanvasIndex();
        this.setVisible(true);
    }

    public T getActiveCanvas() {
        return this.buffer[this.activeBuffer];
    }

    public boolean isVisible() {
        return this.getActiveCanvas().isVisible();
    }

    public void setVisible(boolean visible) {
        this.getActiveCanvas().setVisible(visible);
    }

    public void addToMap() {
        this.apply(overlay -> overlay.addToMap());
    }

    protected int nextCanvasIndex() {
        return (this.activeBuffer + 1) % BUFFER_SIZE;
    }
}
