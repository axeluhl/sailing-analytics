package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoEditDialog extends VideoDialog {
    public VideoEditDialog(VideoDTO video, StringMessages stringMessages,
            FileStorageServiceConnectionTestObservable storageServiceAvailable, DialogCallback<List<VideoDTO>> callback) {
        super(video.getCreatedAtDate(), new VideoParameterValidator(stringMessages), stringMessages,
                storageServiceAvailable, callback);
        createdAtLabel = new Label(video.getCreatedAtDate().toString());
        videoURLAndUploadComposite.setUri(video.getSourceRef());
        subtitleTextBox = createTextBox(video.getSubtitle());
        subtitleTextBox.setVisibleLength(50);
        copyrightTextBox = createTextBox(video.getCopyright());
        copyrightTextBox.setVisibleLength(50);
        List<String> tags = new ArrayList<String>();
        tags.addAll(video.getTags());
        tagsListEditor.setValue(tags);
        setSelectedLocale(video.getLocale());
        thumbnailURLAndUploadComposite.setUri(video.getThumbnailRef());
        initVideoTmpDatas(video.getSourceRef(), video.getTitle(), video.getLengthInSeconds(), video.getMimeType());
    }
}
