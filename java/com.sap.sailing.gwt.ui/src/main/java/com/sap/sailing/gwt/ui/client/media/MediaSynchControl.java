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
    private final FlowPanel offsetPanel;
    private final MediaSynchAdapter mediaSynchAdapter;
    private final TextBox offsetEdit;
    private final CheckBox lockToggle;

    public MediaSynchControl(MediaSynchAdapter mediaSynchListener) {
        this.mediaSynchAdapter = mediaSynchListener;
        mainPanel = new FlowPanel();
        offsetPanel = new FlowPanel();
        offsetPanel.addStyleName("offset-panel");
        buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("button-panel");
        Button fastRewindButton = new Button("-1s &#171;", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fastRewind();
            }
        });
        Button slowRewindButton = new Button("-0.1s &#8249;", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                slowRewind();
            }
        });
        Button slowForwardButton = new Button("&#8250; +0.1s", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                slowForward();
            }
        });
        Button fastForwardButton = new Button("&#187; + 1s", new ClickHandler() {
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
        saveButton.addStyleName("confirm-button");
        Button discardButton = new Button("Discard", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                discard();
            }
        });

        lockToggle = new CheckBox("Auto Playback");
        lockToggle.addStyleName("raceBoardNavigation-standaloneElement");
        lockToggle
                .setTitle("Uncheck to decouple video from race. Use video controls to adjust video/race synchronization. Re-check when finished.");
        lockToggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setLocked(isLocked());
                if (isLocked()) {
                    lockToggle.setText("Auto Playback");
                } else {
                    lockToggle.setText("Manual Playback");
                }
            }

        });

        offsetEdit = new TextBox();

        mainPanel.add(lockToggle);
        offsetPanel.add(new Label("Offset:"));
        offsetPanel.add(offsetEdit);

        offsetPanel.add(fastRewindButton);
        offsetPanel.add(slowRewindButton);

        offsetPanel.add(slowForwardButton);
        offsetPanel.add(fastForwardButton);
        
        buttonPanel.add(offsetPanel);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(discardButton);
        
        
        mainPanel.add(buttonPanel);

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
