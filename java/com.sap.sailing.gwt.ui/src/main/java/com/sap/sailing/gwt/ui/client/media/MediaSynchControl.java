package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class MediaSynchControl {

    private static final int FAST = 1000;
    private static final int SLOW = 100;

    private final FlowPanel mainPanel;
    private final FlowPanel buttonPanel;
    private final MediaSynchAdapter mediaSynchAdapter;
    private final TextBox offsetEdit;
    private final CheckBox lockToggle;

    public MediaSynchControl(MediaSynchAdapter mediaSynchListener) {
        this.mediaSynchAdapter = mediaSynchListener;
        mainPanel = new FlowPanel();
        buttonPanel = new FlowPanel();
        Button fastRewindButton = new Button("<p>-1s &lt;&lt;</p>", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fastRewind();
            }
        });
        Button slowRewindButton = new Button("<p>-0.1s &lt;</p>", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                slowRewind();
            }
        });
        Button slowForwardButton = new Button("<p>&gt; +0.1s</p>", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                slowForward();
            }
        });
        Button fastForwardButton = new Button("<p>&gt;&gt; + 1s</p>", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fastForward();
            }
        });
        Button saveButton = new Button("Confirm", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                save();
            }
        });
        Button discardButton = new Button("Discard", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                discard();
            }
        });

        lockToggle = new CheckBox("Locked");
        lockToggle
                .setTitle("Uncheck to decouple video from race. Use video controls to adjust video/race synchronization. Re-check when finished.");
        lockToggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setLocked(isLocked());
            }

        });

        offsetEdit = new TextBox();

        buttonPanel.add(fastRewindButton);
        buttonPanel.add(slowRewindButton);
        buttonPanel.add(new Label("Offset:"));
        buttonPanel.add(offsetEdit);
        buttonPanel.add(slowForwardButton);
        buttonPanel.add(fastForwardButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(discardButton);
        mainPanel.add(buttonPanel);
        mainPanel.add(lockToggle);

        lockToggle.setValue(true);
        updateOffset();

    }

    protected void discard() {
        this.mediaSynchAdapter.discard();
        updateOffset();
    }

    protected void save() {
        this.mediaSynchAdapter.save();
    }

    private void setLocked(boolean isLocked) {
        for (int i = 0; i < buttonPanel.getWidgetCount(); i++) {
            Widget widget = buttonPanel.getWidget(i);
            if (widget instanceof FocusWidget) {
                ((FocusWidget) widget).setEnabled(isLocked());
            }
        }
        mediaSynchAdapter.setControlsVisible(!isLocked());

        mediaSynchAdapter.pauseMedia();
        mediaSynchAdapter.pauseRace();
        if (isLocked()) {
            mediaSynchAdapter.updateOffset();
        }
        updateOffset();
    }

    private boolean isLocked() {
        return lockToggle.getValue();
    }

    private void fastForward() {
        changeOffsetBy(FAST);
    }

    private void slowForward() {
        changeOffsetBy(SLOW);
    }

    private void slowRewind() {
        changeOffsetBy(-SLOW);
    }

    private void fastRewind() {
        changeOffsetBy(-FAST);
    }

    private void changeOffsetBy(int delta) {
        mediaSynchAdapter.changeOffsetBy(delta);
        updateOffset();
    }

    public void updateOffset() {
        offsetEdit.setText(TimeFormatUtil.milliSecondsToHrsMinSec(mediaSynchAdapter.getOffset()));
    }

    public Widget widget() {
        return mainPanel;
    }

}
