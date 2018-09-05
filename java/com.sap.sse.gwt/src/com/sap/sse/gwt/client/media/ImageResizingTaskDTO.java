package com.sap.sse.gwt.client.media;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;

public class ImageResizingTaskDTO implements IsSerializable {
    
    ImageDTO image;
    List<MediaTagConstants> resizingTask;

    /** for GWT */
    @Deprecated
    protected ImageResizingTaskDTO() {
    }

    public ImageResizingTaskDTO(String imageRef, Date createdAtDate, List<MediaTagConstants> resizingTask) {
        this(new ImageDTO(imageRef, createdAtDate), resizingTask);
    }

    public ImageResizingTaskDTO(ImageDTO image, List<MediaTagConstants> resizingTask) {
        this.image = image;
        this.resizingTask = resizingTask;
    }

   public ImageDTO getImage() {
       return image;
   }
   
   public List<MediaTagConstants> getResizingTask(){
       return resizingTask;
   }

   public ImageDTO cloneImageDTO() {
       ImageDTO toReturn = new ImageDTO(image.getSourceRef(), image.getCreatedAtDate());
       toReturn.setTitle(image.getTitle());
       toReturn.setCopyright(image.getCopyright());
       toReturn.setLocale(image.getLocale());
       toReturn.setMimeType(MimeType.byName(image.getMimeType().name()));
       // creating a new mimetype object so they do not use the same reference
       toReturn.setSizeInPx(image.getWidthInPx(), image.getHeightInPx());
       toReturn.setSubtitle(image.getSubtitle());
       List<String> tags = new ArrayList<>();
       // Creating a new list, so they have the same tags, but not use the same reference to these tags, otherwise they
       // could affect each other by editing this list
       tags.addAll(image.getTags());
       toReturn.setTags(tags);
       return toReturn;
   }
}