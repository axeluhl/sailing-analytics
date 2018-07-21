package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.media.MediaSynchControl.EditButtonProxy;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.WindowBox;
import com.sap.sse.security.ui.client.UserService;

public class VideoFloatingContainer extends AbstractVideoContainer implements VideoContainer {
    VideoContainerRessource res = GWT.create(VideoContainerRessource.class);

    private static final int MIN_WIDTH = 420;
    private final WindowBox dialogBox;
    private final MediaSynchControl mediaSynchControl;
    private final PopupPositionProvider popupPositionProvider;
    private Anchor edit;

    public VideoFloatingContainer(VideoSynchPlayer videoPlayer, PopupPositionProvider popupPositionProvider,
            UserService userservice, MediaServiceAsync mediaService, ErrorReporter errorReporter,
            PlayerCloseListener playerCloseListener, PopoutListener popoutListener) {
        super(new FlowPanel(), videoPlayer, popoutListener, playerCloseListener);

        this.popupPositionProvider = popupPositionProvider;

        rootPanel.addStyleName("video-root-panel");
        rootPanel.add(videoPlayer.getWidget());

        this.edit = new Anchor();
        this.edit.getElement().getStyle().setBackgroundImage("url('" + res.editIcon().getSafeUri().asString() + "')");
        EditButtonProxy proxy = new EditButtonProxy() {

            @Override
            public void setTitle(String string) {
                edit.setTitle(string);
            }

            @Override
            public void addAction(Runnable runnable) {
                edit.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        runnable.run();
                    }
                });
            }

            @Override
            public void setEnabled(boolean b) {
                edit.getElement().getStyle().setDisplay(b ? Display.BLOCK : Display.NONE);
            }
        };
        mediaSynchControl = new MediaSynchControl(this.videoPlayer, mediaService, errorReporter, proxy, userservice);
        mediaSynchControl.widget().addStyleName("media-synch-control");
        rootPanel.add(mediaSynchControl.widget());

        videoPlayer.setEditFlag(mediaSynchControl);

        this.dialogBox = new WindowBox(videoPlayer.getMediaTrack().title, videoPlayer.getMediaTrack().toString(),
                rootPanel, new WindowBox.PopoutHandler() {

                    @Override
                    public void popout() {
                        VideoFloatingContainer.this.popoutListener
                                .popoutVideo(VideoFloatingContainer.this.videoPlayer.getMediaTrack());
                    }
                });
        // values required for js and youtube player to stay useable
        dialogBox.setMinWidth(MIN_WIDTH);
        if (edit != null) {
            dialogBox.addBeforeBarButtons(edit);
        }
        dialogBox.addCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                VideoFloatingContainer.this.videoPlayer.pauseMedia();
                VideoFloatingContainer.this.popupCloseListener.playerClosed();
            }

        });

        show();

        // hook into the click events but relay them also
        // Event.sinkEvents(dialogBox.getElement(), Event.ONCLICK);
        EventListener originalListener = Event.getEventListener(dialogBox.getElement());
        Event.setEventListener(dialogBox.getElement(), new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if(event.getTypeInt() == Event.ONMOUSEDOWN || event.getTypeInt() == Event.ONTOUCHSTART) {
                    moveToTop();
                }
                originalListener.onBrowserEvent(event);
            }
        });
        
        moveToTop();
    }

    // z index less z ordering logic
    public void moveToTop() {
        Element self = dialogBox.getElement();
        Element parent = self.getParentElement();
        Node lastChild = parent.getLastChild();
        if (lastChild instanceof Element) {
            Element lastElement = (Element) lastChild;
            if (lastElement != self) {
                // we are already attached, so we will be only moved without getting events relating to attachment and
                // visibility status
                parent.appendChild(self);
            }
        }
    }
    
    @Override
    void show() {
        dialogBox.show();
        dialogBox.setVisible(false);

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                int absoluteTop = popupPositionProvider.getYPositionUiObject().getAbsoluteTop();
                int posY = absoluteTop - dialogBox.getOffsetHeight() - 40;
                dialogBox.setPopupPosition(5, posY);
                dialogBox.setPixelSize(videoPlayer.getDefaultWidth(), videoPlayer.getDefaultHeight());
                dialogBox.setVisible(true);
            }
        });

    }

    @Override
    void hide() {
        dialogBox.hide();
    }

}
