/*
 * MIT License
 *
 * Copyright (c) 2021 Tianscar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.tianscar.assetfile;

import android.content.res.AssetFileDescriptor;

import androidx.annotation.NonNull;

import com.tianscar.module.ApplicationUtils;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * An OutputStream that packaging an AssetFileDescriptor#AutoCloseOutputStream for use.
 */
public class AssetFileOutputStream extends OutputStream {

    private final FileOutputStream STREAM;

    private volatile boolean closed = false;

    private final AssetFileDescriptor AFD;

    /**
     * Creates a AssetFileOutputStream by opening a connection to an actual file in assets,
     * the file named by the name string.
     * A new AssetFileDescriptor object is created to represent this file connection.
     *
     * @param name the assetFile name.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileOutputStream (@NonNull String name) throws IOException {
        this(ApplicationUtils.getAssets().openFd(name));
    }

    /**
     * Creates a AssetFileOutputStream by opening a connection to an actual file in assets,
     * the file named by the AssetFile file.
     * A new AssetFileDescriptor object is created to represent this file connection.
     *
     * @param file the assetFile to be opened for reading.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileOutputStream (@NonNull AssetFile file) throws IOException {
        this(ApplicationUtils.getAssets().openFd(file.getPath()));
    }

    /**
     * Creates a FileOutputStream by using the asset file descriptor fdObj, which represents
     * an existing connection to an actual file in assets.
     *
     * @param fdObj the asset file descriptor to be opened for reading.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileOutputStream (@NonNull AssetFileDescriptor fdObj) throws IOException {
        AFD = fdObj;
        STREAM = AFD.createOutputStream();
    }

    /**
     * Closes this asset file output stream and releases any system resources associated with this stream.
     * This asset file output stream may no longer be used for writing bytes.
     *
     * If this stream has an associated channel then the channel is closed as well.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        STREAM.close();
        closed = true;
    }

    /**
     * Writes the specified byte to this file output stream. Implements the write method of OutputStream.
     * @param b the byte to be written.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public void write(int b) throws IOException {
        STREAM.write(b);
    }

    /**
     * Writes b.length bytes from the specified byte array to this file output stream.
     * @param b the data.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public void write(byte[] b) throws IOException {
        STREAM.write(b);
    }

    /**
     * Writes len bytes from the specified byte array starting at offset off to this file output stream.
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        STREAM.write(b, off, len);
    }

    /**
     * Flushes this asset output stream and forces any buffered output bytes to be written out.
     * The general contract of flush is that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output stream,
     * such bytes should immediately be written to their intended destination.
     *
     * If the intended destination of this stream is an abstraction provided by the underlying operating system,
     * for example a file, then flushing the stream guarantees only that bytes previously written to
     * the stream are passed to the operating system for writing;
     * it does not guarantee that they are actually written to a physical device such as a disk drive.
     *
     * The flush method of OutputStream does nothing.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        STREAM.flush();
    }

    /**
     * Returns the unique FileChannel object associated with this asset file output stream.
     *
     * The initial position of the returned channel will be equal to the number of bytes written to the file
     * so far unless this stream is in append mode,
     * in which case it will be equal to the size of the file.
     * Writing bytes to this stream will increment the channel's position accordingly.
     * Changing the channel's position, either explicitly or by writing, will change this stream's file position.
     *
     * @return 	the file channel associated with this asset file output stream
     */
    public FileChannel getChannel() {
        return STREAM.getChannel();
    }

    /**
     * Returns the file descriptor associated with this stream.
     *
     * @return the FileDescriptor object that represents the connection to the file
     * in assets being used by this AssetFileOutputStream object.
     * @throws IOException if an I/O error occurs.
     */
    public final FileDescriptor getFD() throws IOException {
        return STREAM.getFD();
    }

    /**
     * Returns the asset file descriptor associated with this stream.
     *
     * @return the AssetFileDescriptor object that represents the connection to the file
     * in assets being used by this AssetFileOutputStream object.
     */
    public final AssetFileDescriptor getAFD() {
        return AFD;
    }

    /**
     * Cleans up the connection to the file,
     * and ensures that the close method of this asset file output stream
     * is called when there are no more references to this stream.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void finalize() throws IOException {
        if (!closed) {
            close();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetFileOutputStream)) return false;
        AssetFileOutputStream that = (AssetFileOutputStream) o;
        return closed == that.closed &&
                STREAM.equals(that.STREAM) &&
                getAFD().equals(that.getAFD());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {STREAM, closed, getAFD()});
    }

}
