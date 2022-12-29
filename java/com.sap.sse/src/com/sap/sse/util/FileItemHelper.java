package com.sap.sse.util;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.fileupload.FileItem;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;

public class FileItemHelper {
    /**
     * Determines the {@link Charset} for a {@link FileItem}. It does so by looking at the
     * {@link FileItem}'s {@link FileItem#getContentType() content type}, submitting it to
     * the {@link ContentType#parse(String)} method if not {@code null}. If {@code null}
     * or if the {@link ContentType#parse(String)} method throws an exception, the
     * {@link Charset#defaultCharset() default charset} is returned. Otherwise, the
     * charset is {@link ContentType#getCharset() fetched from the content type}.
     */
    public static Charset getCharset(FileItem fileItem) {
        return getCharset(fileItem, Charset.defaultCharset());
    }

    /**
     * Determines the {@link Charset} for a {@link FileItem}. It does so by looking at the {@link FileItem}'s
     * {@link FileItem#getContentType() content type}, submitting it to the {@link ContentType#parse(String)} method if
     * not {@code null}. If {@code null} or if the {@link ContentType#parse(String)} method throws an exception, the
     * {@code defaultCharset} is returned. Otherwise, the charset is {@link ContentType#getCharset() fetched from the
     * content type}.
     */
    public static Charset getCharset(FileItem fileItem, final Charset defaultCharset) {
        Charset result;
        try {
            final ContentType contentType = fileItem.getContentType() == null ? null : ContentType.parse(fileItem.getContentType());
            if (contentType != null) {
                result = contentType.getCharset();
            } else {
                result = defaultCharset;
            }
        } catch (ParseException | UnsupportedCharsetException e) {
            result = defaultCharset;
        }
        return result;
    }
}
