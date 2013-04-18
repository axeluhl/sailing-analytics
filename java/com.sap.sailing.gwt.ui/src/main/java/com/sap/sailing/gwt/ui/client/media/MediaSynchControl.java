package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class MediaSynchControl {

    private static final int FAST = 1000;
    private static final int SLOW = 100;
    
    private final HorizontalPanel mainPanel;
    private final  HorizontalPanel buttonPanel;
    private final MediaSynchAdapter mediaSynchAdapter;
    private final TextBox offsetEdit;
    private final ToggleButton lockButton;

    public MediaSynchControl(MediaSynchAdapter mediaSynchListener) {
        this.mediaSynchAdapter = mediaSynchListener;
        mainPanel = new HorizontalPanel();
        buttonPanel = new HorizontalPanel();
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
        
        lockButton = new ToggleButton("Locked", "Unlocked", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setLocked(!isLocked());
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
        mainPanel.add(lockButton);

        setLocked(true);
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
        if (isLocked != this.isLocked()) {
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
    }

    private boolean isLocked() {
        return !lockButton.isDown();
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
