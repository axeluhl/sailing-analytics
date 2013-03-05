package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.TimeFormatUtil;

public class MediaSynchControl {

    private static final int FAST = 1000;
    private static final int SLOW = 100;
    
    private final HorizontalPanel mainPanel;
    private final  HorizontalPanel buttonPanel;
    private final MediaSynchListener mediaSynchListener;
    private final TextBox offsetEdit;
    private final ToggleButton lockButton;

    public MediaSynchControl(MediaSynchListener mediaSynchListener) {
        this.mediaSynchListener = mediaSynchListener;
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
                toggleLockState();
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
        this.mediaSynchListener.discard();
        updateOffset();
    }

    protected void save() {
        this.mediaSynchListener.save();
    }

    private void setLocked(boolean isLocked) {
        for (int i = 0; i < buttonPanel.getWidgetCount(); i++) {
            Widget widget = buttonPanel.getWidget(i);
            if (widget instanceof FocusWidget) {
                ((FocusWidget) widget).setEnabled(isLocked);
            }
        }
        mediaSynchListener.setControlsVisible(!isLocked);
    }

    private void toggleLockState() {
        setLocked(!lockButton.isDown());
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
        mediaSynchListener.setOffset(mediaSynchListener.getOffset() + delta);
        updateOffset();
    }

    public void updateOffset() {
        offsetEdit.setText(TimeFormatUtil.milliSecondsToHrsMinSec(mediaSynchListener.getOffset()));
    }

    public Widget widget() {
        return mainPanel;
    }

    private boolean isLocked() {
        return !lockButton.isDown();
    }

}
