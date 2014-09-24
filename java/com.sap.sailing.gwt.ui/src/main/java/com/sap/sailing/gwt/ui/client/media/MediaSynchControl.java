package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.media.MediaSynchAdapter.EditFlag;

public class MediaSynchControl implements EditFlag {

    private static final int FAST = 1000;
    private static final int SLOW = 100;
    
    private final MediaServiceAsync mediaService;
    private final MediaSynchAdapter mediaSynchAdapter;
    private final  ErrorReporter errorReporter;
    private final MediaTrack backupVideoTrack;

    private final FlowPanel mainPanel;
    private final FlowPanel buttonPanel;
    private final FlowPanel fineTuningPanel;

    private final Button editButton;
    private final Button previewButton;
    private final Button saveButton;
    private final Button discardButton;

    private boolean isEditing = false;
    
    public MediaSynchControl(MediaSynchAdapter mediaSynchAdapter, MediaServiceAsync mediaService, ErrorReporter errorReporter) {
        this.mediaService = mediaService;
        this.mediaSynchAdapter = mediaSynchAdapter;
        this.errorReporter = errorReporter;
        MediaTrack videoTrack = this.mediaSynchAdapter.getMediaTrack(); 
        backupVideoTrack = new MediaTrack(null, videoTrack.title, videoTrack.url, videoTrack.startTime, videoTrack.duration, videoTrack.mimeType, videoTrack.assignedRaces);
        mainPanel = new FlowPanel();
        fineTuningPanel = new FlowPanel();
        fineTuningPanel.addStyleName("finetuning-panel");
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
        editButton = new Button("Edit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                edit();
            }
        });
        editButton.setTitle("Pauses race and decouples race from video playback. Use video controls, race time slider or fine tuning buttons for time alignment, then press Preview to re-couple race and video playback.");
        
        previewButton = new Button("Preview", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                preview();
            }
        });
        previewButton.setTitle("Re-couples race and video playback. If ok, press Save to write changes back to database. Press Cancel to reset the changes.");
        
        saveButton = new Button("Save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                save();
            }
        });
        saveButton.addStyleName("confirm-button");
        discardButton = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                discard();
            }
        });

        fineTuningPanel.add(fastRewindButton);
        fineTuningPanel.add(slowRewindButton);
        fineTuningPanel.add(slowForwardButton);
        fineTuningPanel.add(fastForwardButton);
        
        mainPanel.add(editButton);
        mainPanel.add(previewButton);
        mainPanel.add(fineTuningPanel);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(discardButton);
        
        
        mainPanel.add(buttonPanel);

        updateUiState();

    }

    protected void preview() {
        isEditing = false;
        pausePlayback();
        mediaSynchAdapter.updateOffset();
        updateUiState();
    }

    protected void edit() {
        pausePlayback();
        isEditing = true;
        updateUiState();
    }

    private void pausePlayback() {
        mediaSynchAdapter.pauseMedia();
        mediaSynchAdapter.pauseRace();
    }

    private void discard() {
        mediaSynchAdapter.getMediaTrack().startTime = backupVideoTrack.startTime;
        isEditing = false;
        pausePlayback();
        mediaSynchAdapter.forceAlign();
        updateUiState();
// For now, only start time can be changed.        
//      getMediaTrack().title = backupVideoTrack.title;
//      getMediaTrack().url = backupVideoTrack.url;
//      getMediaTrack().duration = backupVideoTrack.duration;
    }

    private void save() {
        
        if (backupVideoTrack.startTime != mediaSynchAdapter.getMediaTrack().startTime) {
            mediaService.updateStartTime(mediaSynchAdapter.getMediaTrack(), new AsyncCallback<Void>() {

                @Override
                public void onSuccess(Void result) {
                    // nothing to do
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.toString());
                }
            });
        }
    }

    private void updateUiState() {
        for (int i = 0; i < fineTuningPanel.getWidgetCount(); i++) {
            Widget widget = fineTuningPanel.getWidget(i);
            if (widget instanceof FocusWidget) {
                ((FocusWidget) widget).setEnabled(isEditing);
            }
        }
        mediaSynchAdapter.setControlsVisible(isEditing);
        
        editButton.setEnabled(!isEditing);
        previewButton.setEnabled(isEditing);
        
        saveButton.setEnabled(!isEditing && isDirty());
        discardButton.setEnabled(isEditing || isDirty());
    }

    private boolean isDirty() {
        return backupVideoTrack.startTime != mediaSynchAdapter.getMediaTrack().startTime;
    }

    private void fastForward() {
        changeOffsetBy(-FAST);
    }

    private void slowForward() {
        changeOffsetBy(-SLOW);
    }

    private void slowRewind() {
        changeOffsetBy(SLOW);
    }

    private void fastRewind() {
        changeOffsetBy(FAST);
    }

    private void changeOffsetBy(int delta) {
        mediaSynchAdapter.changeOffsetBy(delta);
    }

    public Widget widget() {
        return mainPanel;
    }

    @Override
    public boolean isEditing() {
        return isEditing;
    }

}
