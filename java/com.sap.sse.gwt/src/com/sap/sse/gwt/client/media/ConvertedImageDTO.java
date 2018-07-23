package com.sap.sse.gwt.client.media;

import java.util.Date;
import java.util.Map;

import com.google.gwt.json.client.JSONObject;

public class ConvertedImageDTO extends ImageDTO {

    private String fileType;
    Map<String,String> resizedImageUri;
    
    public ConvertedImageDTO(String imageRef, Date createdAtDate, String fileType, Map<String,String> resizedImageUri) {
        super(imageRef, createdAtDate);
        this.fileType = fileType;
        this.resizedImageUri = resizedImageUri;
    }
    
    public ConvertedImageDTO(String imageRef, Date createdAtDate) {
        super(imageRef,createdAtDate);
    }

    public String getFileType() {
        return fileType;
    }
    
    public String getResizedImgeUri(String key) {
        return resizedImageUri.get(key);
    }
    
    public Map<String,String> getUriMap() {
        return resizedImageUri;
    }
    
    protected ConvertedImageDTO() {
    }
}
