package com.sap.sse.common.media;

public interface WithImages {
    /**
     * Returns a non-<code>null</code> live but unmodifiable collection of image resources that can be
     * used to represent the event, e.g., on a web page.
     * 
     * @return a non-<code>null</code> value which may be empty
     */
    Iterable<ImageDescriptor> getImages();

    /**
     * Replaces the {@link #getImages() current contents of the image sequence by the images in
     * <code>images</code>.
     * 
     * @param images
     *            if <code>null</code>, the internal sequence of images is cleared but remains valid (non-
     *            <code>null</code>)
     */
    void setImages(Iterable<ImageDescriptor> images);

    void addImage(ImageDescriptor image);
    
    void removeImage(ImageDescriptor image);
}
