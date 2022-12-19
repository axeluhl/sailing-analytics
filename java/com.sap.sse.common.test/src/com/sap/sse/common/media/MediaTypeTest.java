package com.sap.sse.common.media;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MediaTypeTest {

    @Test
    public void testMediaTypeByContentType() {
        assertEquals(MimeType.mp4, MimeType.byContentType("video/mp4"));
        assertEquals(MimeType.ogv, MimeType.byContentType("video/ogg"));
        assertEquals(MimeType.ogg, MimeType.byContentType("audio/ogg"));
        assertEquals(MimeType.qt, MimeType.byContentType("video/quicktime"));
        assertEquals(MimeType.image, MimeType.byContentType("image/jpeg"));
        assertEquals(MimeType.image, MimeType.byContentType("image/gif"));
        assertEquals(MimeType.image, MimeType.byContentType("image/webp"));
        assertEquals(MimeType.image, MimeType.byContentType("image/png"));
        assertEquals(MimeType.unknown, MimeType.byContentType("image/xxxx"));
        assertEquals(MimeType.unknown, MimeType.byContentType("xxx/png"));
        assertEquals(MimeType.unknown, MimeType.byContentType("xyz"));
    }

    @Test
    public void testMediaTypeByExtension() {
        assertEquals(MimeType.mp4, MimeType.byExtension("video.mp4"));
        assertEquals(MimeType.ogv, MimeType.byExtension("https://exmaple.video.com/download/video.ogv"));
        assertEquals(MimeType.mov, MimeType.byExtension("video.mov"));
        assertEquals(MimeType.qt, MimeType.byExtension("video.qt"));
        assertEquals(MimeType.mp3, MimeType.byExtension("C:\\User\\Test\\Music\\audio.mp3"));
        assertEquals(MimeType.ogg, MimeType.byExtension("audio.ogg"));
        assertEquals(MimeType.image, MimeType.byExtension("https://exmaple.images.com/download/image.jpeg"));
        assertEquals(MimeType.image, MimeType.byExtension("image.jpg"));
        assertEquals(MimeType.image, MimeType.byExtension("image.gif"));
        assertEquals(MimeType.image, MimeType.byExtension("image.webp"));
        assertEquals(MimeType.image, MimeType.byExtension("image.png"));
        assertEquals(MimeType.unknown, MimeType.byExtension("unknownExtension.xxx"));
        assertEquals(MimeType.unknown, MimeType.byExtension("filenameWithoutExtension"));
    }

}
