package com.sap.sailing.gwt.ui.shared.media;

import java.net.URL;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.ImageSize;

public class ImageReferenceDTO implements IsSerializable {
    private int widthInPx;
    private int heightInPx;
    private String imageURL;

    @SuppressWarnings("unused")
    private ImageReferenceDTO() {
    }

    @GwtIncompatible
    public ImageReferenceDTO(URL imageURL, ImageSize size) {
        this(imageURL.toString(), size.getWidth(), size.getHeight());
    }

    public ImageReferenceDTO(String imageURL, int widthInPx, int heightInPx) {
        super();
        this.imageURL = imageURL;
        this.widthInPx = widthInPx;
        this.heightInPx = heightInPx;
    }

    public int getWidthInPx() {
        return widthInPx;
    }

    public void setWidthInPx(int widthInPx) {
        this.widthInPx = widthInPx;
    }

    public int getHeightInPx() {
        return heightInPx;
    }

    public void setHeightInPx(int heightInPx) {
        this.heightInPx = heightInPx;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((imageURL == null) ? 0 : imageURL.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImageReferenceDTO other = (ImageReferenceDTO) obj;
        if (imageURL == null) {
            if (other.imageURL != null)
                return false;
        } else if (!imageURL.equals(other.imageURL))
            return false;
        return true;
    }
}
