package com.sap.sse.gwt.client.fileupload;

import com.google.gwt.user.client.ui.FileUpload;

import elemental2.core.ArrayBuffer;
import elemental2.core.Uint8Array;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.HTMLInputElement;
import elemental2.promise.Promise;

/**
 * A GWT component wrapping a {@link FileUpload} form {@code input} element of type {@code file} which grants access to
 * the contents of the files the user has selected for upload. The {@code input} element of type {@code file} has a
 * {@code files} property of type {@link FileList}, an array of {@code File} JavaScript objects, each of which having,
 * e.g., an {@code arrayBuffer()} method that delivers a {@code Promise} resolving with an {@code ArrayBuffer}. See
 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Blob/arrayBuffer">here</a>. This wrapper around a
 * {@link FileUpload} input element grants access to the files currently selected.
 * <p>
 * 
 * This can also be combined with adding a {@code change}
 * {@link FileUpload#addChangeHandler(com.google.gwt.event.dom.client.ChangeHandler) handler} to the {@link FileUpload}
 * element which is triggered in particular as the user changes the file selection.
 * <p>
 * 
 * Use like this:
 * 
 * <pre>
 *      final FileUpload someFileUpload = ...;
 *      final FileUploadWithLocalFileContent fuwlfc = new FileUploadWithLocalFileContent(someFileUpload);
        someFileUpload.addChangeHandler(e->{
            if (fuwlfc.getFileList().length == 1) {
                fuwlfc.getFileContents(0, (byte[] contents)->{
                    // some function using the file contents as a byte array
                });
            }
        });
 * </pre>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class FileUploadWithLocalFileContent {
    private final FileUpload fileUpload;

    @FunctionalInterface
    public static interface FileContentCallback {
        void fileContentsRead(byte[] fileContents);
    }
    
    public FileUploadWithLocalFileContent(FileUpload fileUpload) {
        super();
        this.fileUpload = fileUpload;
    }
    
    public FileList getFileList() {
        return getFileUploadAsHtmlInputElement().files;
    }
    
    /**
     * @param i
     *            number of the file, starting with 0; use 0 if it's a single file upload field
     */
    public void getFileContents(int i, FileContentCallback callback) {
        final FileList fileList = getFileList();
        final File file = fileList.item((double) i);
        final Promise<ArrayBuffer> arrayBufferPromise = file.arrayBuffer();
        arrayBufferPromise.then(arrayBuffer->{
            callback.fileContentsRead(uint8ArrayBufferToByteArray(new Uint8Array(arrayBuffer)));
            return null;
        });
    }
    
    public static native byte[] uint8ArrayBufferToByteArray(Uint8Array buffer) /*-{
        return buffer;
    }-*/;
    
    private HTMLInputElement getFileUploadAsHtmlInputElement() {
        return (HTMLInputElement) (Object) fileUpload.getElement();
    }
}
