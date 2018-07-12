package com.sap.sse.gwt.client.media;

import java.util.Date;

public class ConvertedImageDTO extends ImageDTO {

    private String base64Code;
    private String fileType;
    private String sizeTag;
    
    public ConvertedImageDTO(String imageRef, Date createdAtDate, String base64Code, String fileType, String sizeTag) {
        super(imageRef, createdAtDate);
        this.base64Code = base64Code;
        this.fileType = fileType;
        this.sizeTag = sizeTag;
    }
    
    public ConvertedImageDTO(String imageRef, Date createdAtDate) {
        super(imageRef,createdAtDate);
    }
    
    public String getBase64Code() {
        return base64Code;
    }

    public String getFileType() {
        return fileType;
    }

    public String getSizeTag() {
        return sizeTag;
    }
    
    protected ConvertedImageDTO() {
    }
}
