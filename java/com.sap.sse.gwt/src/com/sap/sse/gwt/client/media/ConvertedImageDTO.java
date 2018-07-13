package com.sap.sse.gwt.client.media;

import java.util.Date;
import java.util.Map;

public class ConvertedImageDTO extends ImageDTO {

    private String fileType;
    Map<String,String> map;
    
    public ConvertedImageDTO(String imageRef, Date createdAtDate, String fileType, Map<String,String> base64CodeMap) {
        super(imageRef, createdAtDate);
        this.fileType = fileType;
        this.map = base64CodeMap;
    }
    
    public ConvertedImageDTO(String imageRef, Date createdAtDate) {
        super(imageRef,createdAtDate);
    }

    public String getFileType() {
        return fileType;
    }
    
    public String getBase64Code(String key) {
        return map.get(key);
    }
    
    public Map<String,String> getMap() {
        return map;
    }
    
    protected ConvertedImageDTO() {
    }
}
