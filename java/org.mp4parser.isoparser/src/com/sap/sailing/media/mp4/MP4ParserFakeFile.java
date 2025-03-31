package com.sap.sailing.media.mp4;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;

/**
 * This class pretends to be a complete larger file, while only carrying the first 10 and last 10 megabytes. It is
 * specifically meant to be used for the mp4Parser and only supports the required subset for it. It extends FileChannel
 * as the library uses that to skip input for skipped boxes
 */
public class MP4ParserFakeFile extends FileChannel {
    private final long totalSizeOfFile;
    private long currentPos = 0;
    private byte[] startOfFileByteArray;
    private byte[] endOfFileByteArray;

    public MP4ParserFakeFile(byte[] start, byte[] end, Long skipped) {
        this.startOfFileByteArray = start;
        this.endOfFileByteArray = end;
        this.totalSizeOfFile = start.length + end.length + skipped;
    }

    public MP4ParserFakeFile(File tmp) {
        try (SeekableByteChannel inChannel = Files.newByteChannel(tmp.toPath())) {
            totalSizeOfFile = tmp.length();
            if (totalSizeOfFile < MP4MediaParser.REQUIRED_SIZE_IN_BYTES * 2) {
                throw new IllegalStateException(
                        "The file is to small to be analysed < " + (MP4MediaParser.REQUIRED_SIZE_IN_BYTES * 2));
            } else {
                startOfFileByteArray = new byte[MP4MediaParser.REQUIRED_SIZE_IN_BYTES];
                endOfFileByteArray = new byte[MP4MediaParser.REQUIRED_SIZE_IN_BYTES];
                ByteBuffer startOfFileByteBuffer = ByteBuffer.wrap(startOfFileByteArray);
                ByteBuffer endOfFileByteBuffer = ByteBuffer.wrap(endOfFileByteArray);
                int startRead = inChannel.read(startOfFileByteBuffer);
                if (startRead != startOfFileByteBuffer.capacity()) {
                    throw new IllegalArgumentException("Could not read fileStart");
                }
                inChannel.position(totalSizeOfFile - (MP4MediaParser.REQUIRED_SIZE_IN_BYTES + 1));
                int endRead = inChannel.read(endOfFileByteBuffer);
                if (endRead != endOfFileByteBuffer.capacity()) {
                    throw new IllegalArgumentException("Could not read fileStart");
                }
            }
        } catch (Exception e) {
            // File system error? Re-throwing, cause it cannot be handled here.
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        // Ignore boundary crossings into nulled part, as it cannot be parsed in that case.
        // Only the actual movie data should be nulled.
        int read = 0;
        int toRead = dst.remaining();
        final long startOfEndSegment = totalSizeOfFile - (endOfFileByteArray.length + 1);
        if (currentPos < startOfFileByteArray.length) {
            // in start buffer, read one byte
            dst.put(startOfFileByteArray, (int) currentPos, toRead);
            read = toRead;
        } else if (currentPos < startOfEndSegment) {
            throw new IllegalArgumentException(
                    "The nulled part should only be in the skipped boxes, else reading would be corrupt");
        } else {
            int remaining = (int) (totalSizeOfFile - currentPos);
            int relative = endOfFileByteArray.length - remaining;
            if (currentPos + toRead > totalSizeOfFile) {
                toRead = (int) (totalSizeOfFile - currentPos);
            }
            if (toRead > 0) {
                dst.put(endOfFileByteArray, relative, toRead);
                read = toRead;
            } else {
                read = -1;
            }
        }
        currentPos += read;
        return read;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new IllegalArgumentException("MP4 files should only be read");
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public long position() throws IOException {
        return currentPos;
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        currentPos = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public void force(boolean metaData) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    protected void implCloseChannel() throws IOException {
    }

}
