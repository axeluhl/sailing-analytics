package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.media.MediaSynchAdapter.EditFlag;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.UserDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.UserStatusEventHandler;

public class MediaSynchControl implements EditFlag {

    private static final int FAST = 1000;
    private static final int SLOW = 100;

    private final MediaServiceAsync mediaService;
    private final MediaSynchAdapter mediaSynchAdapter;
    private final ErrorReporter errorReporter;
    private final MediaTrack backupVideoTrack;

    private final FlowPanel mainPanel;
    private final FlowPanel editPanel;
    private final FlowPanel commitPanel;
    private final FlowPanel fineTuningPanel;

    private final TextBox titleEdit;
    private final EditButtonProxy editButton;
    private final Button previewButton;
    private final Button saveButton;
    private final Button discardButton;

    private boolean isEditing = false;
    private boolean isEditingAllowed = false;
    private UserService userservice;

    /**
     * We dont want to force the caller to use a specific button or anchor class for future flexibility
     */
    interface EditButtonProxy {
        void addAction(Runnable runnable);

        void setTitle(String string);

        void setEnabled(boolean b);
    }

    public MediaSynchControl(MediaSynchAdapter mediaSynchAdapter, MediaServiceAsync mediaService,
            ErrorReporter errorReporter, EditButtonProxy editButtonProxy, UserService userservice) {
        this.mediaService = mediaService;
        this.mediaSynchAdapter = mediaSynchAdapter;
        this.errorReporter = errorReporter;
        this.userservice = userservice;
        MediaTrack videoTrack = this.mediaSynchAdapter.getMediaTrack();
        isEditingAllowed = hasRightToEdit(videoTrack);
        backupVideoTrack = new MediaTrack(videoTrack.title, videoTrack.url, videoTrack.startTime, videoTrack.duration,
                videoTrack.mimeType, videoTrack.assignedRaces);
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("main-panel");
        editPanel = new FlowPanel();
        fineTuningPanel = new FlowPanel();
        fineTuningPanel.addStyleName("finetuning-panel");
        commitPanel = new FlowPanel();
        commitPanel.addStyleName("button-panel");
        this.editButton = editButtonProxy;
        titleEdit = new TextBox();
        titleEdit.setText(videoTrack.title);
        titleEdit.addStyleName("title-edit");
        titleEdit.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String text = titleEdit.getText();
                MediaSynchControl.this.mediaSynchAdapter.getMediaTrack().title = text;
                updateUiState();
            }
        });
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
        editButton.addAction(new Runnable() {

            @Override
            public void run() {
                edit();
            }
        });
        editButton.setTitle(
                "Pauses race and decouples race from video playback. Use video controls, race time slider or fine tuning buttons for time alignment, then press Preview to re-couple race and video playback.");
        previewButton = new Button("Preview", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                preview();
            }
        });
        previewButton.setTitle(
                "Re-couples race and video playback. If ok, press Save to write changes back to database. Press Cancel to reset the changes.");
        saveButton = new Button("Save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                preview();
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
        mainPanel.add(titleEdit);
        editPanel.add(previewButton);
        mainPanel.add(editPanel);
        mainPanel.add(fineTuningPanel);
        commitPanel.add(saveButton);
        commitPanel.add(discardButton);
        mainPanel.add(commitPanel);
        UserStatusEventHandler userHandler = new UserStatusEventHandler() {
            @Override
            public void onUserStatusChange(UserDTO user, boolean preAuthenticated) {
                if (user == null) {
                    //discard already updates ui state!
                    discard();
                } else {
                    updateUiState();
                }
            }
        };
        mainPanel.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    userservice.addUserStatusEventHandler(userHandler);
                } else {
                    userservice.removeUserStatusEventHandler(userHandler);
                }
            }
        });
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
        mediaSynchAdapter.getMediaTrack().title = backupVideoTrack.title;
        titleEdit.setText(mediaSynchAdapter.getMediaTrack().title);
        mediaSynchAdapter.getMediaTrack().startTime = backupVideoTrack.startTime;
        isEditing = false;
        pausePlayback();
        mediaSynchAdapter.forceAlign();
        updateUiState();
    }

    private void save() {
        if (!backupVideoTrack.startTime.equals(mediaSynchAdapter.getMediaTrack().startTime)) {
            mediaService.updateStartTime(mediaSynchAdapter.getMediaTrack(), new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    backupVideoTrack.startTime = mediaSynchAdapter.getMediaTrack().startTime;
                    updateUiState();
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.toString());
                    updateUiState();
                }
            });
        }
        if (!backupVideoTrack.title.equals(mediaSynchAdapter.getMediaTrack().title)) {
            mediaService.updateTitle(mediaSynchAdapter.getMediaTrack(), new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    backupVideoTrack.title = mediaSynchAdapter.getMediaTrack().title;
                    updateUiState();
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError(caught.toString());
                    updateUiState();
                }
            });
        }
    }

    private void updateUiState() {
        final boolean showEditUI = isEditing || isDirty();
        for (int i = 0; i < fineTuningPanel.getWidgetCount(); i++) {
            Widget widget = fineTuningPanel.getWidget(i);
            if (widget instanceof FocusWidget) {
                ((FocusWidget) widget).setEnabled(showEditUI);
            }
        }
        mediaSynchAdapter.setControlsVisible(showEditUI);
        previewButton.setEnabled(showEditUI);
        editButton.setEnabled(isEditingAllowed && !showEditUI);
        mainPanel.getElement().getStyle().setDisplay(showEditUI && isEditingAllowed ? Display.BLOCK : Display.NONE);
        boolean isDirty = isDirty();
        saveButton.setEnabled(showEditUI || isDirty);
        discardButton.setEnabled(showEditUI || isDirty);
    }

    private boolean hasRightToEdit(MediaTrack video) {
        return userservice.hasPermission(
                SecuredDomainType.MEDIA_TRACK.getPermissionForObject(DefaultActions.UPDATE, video),
                /* TODO ownership */ null);
    }

    private boolean isDirty() {
        return !backupVideoTrack.startTime.equals(mediaSynchAdapter.getMediaTrack().startTime)
                || !backupVideoTrack.title.equals(titleEdit.getText());
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
        updateUiState();
    }

    public Widget widget() {
        return mainPanel;
    }

    @Override
    public boolean isEditing() {
        return isEditing;
    }
}
