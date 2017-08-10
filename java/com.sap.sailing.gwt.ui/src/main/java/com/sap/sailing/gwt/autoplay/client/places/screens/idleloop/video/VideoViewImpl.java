package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.video;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.media.PlayEvent;
import com.sap.sailing.gwt.ui.client.media.VideoJSPlayer;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoViewImpl extends Composite implements VideoView {

    private static IdleNextUpViewImplUiBinder uiBinder = GWT.create(IdleNextUpViewImplUiBinder.class);

    @UiField
    SimplePanel mainPanelUi;

    private VideoPresenter currentPresenter;

    private VideoJSPlayer player;

    interface IdleNextUpViewImplUiBinder extends UiBinder<Widget, VideoViewImpl> {
    }

    public VideoViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(VideoPresenter currentPresenter, AcceptsOneWidget panel) {
        this.currentPresenter = currentPresenter;
        panel.setWidget(this);
    }

    @Override
    public void playVideo(VideoDTO video) {
        player = new VideoJSPlayer(true, true);
        player.setVideo(video.getMimeType(), video.getSourceRef());
        player.addPlayHandler(new PlayEvent.Handler() {
            @Override
            public void onStart(PlayEvent event) {
                int duration = player.getDuration();
                currentPresenter.publishDuration(duration);
            }
        });
        mainPanelUi.clear();
        mainPanelUi.add(player);
    }



}
