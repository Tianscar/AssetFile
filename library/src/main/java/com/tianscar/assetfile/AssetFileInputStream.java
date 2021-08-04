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

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * An InputStream that packaging an AssetFileDescriptor#AutoCloseInputStream for use.
 */
public class AssetFileInputStream extends InputStream {

    private final FileInputStream STREAM;

    private volatile boolean closed = false;

    private final AssetFileDescriptor AFD;

    /**
     * Creates a AssetFileInputStream by opening a connection to an actual file in assets,
     * the file named by the name string.
     * A new AssetFileDescriptor object is created to represent this file connection.
     *
     * @param name the assetFile name.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileInputStream (@NonNull String name) throws IOException {
        this(Utils.getApplication().getAssets().openFd(name));
    }

    /**
     * Creates a AssetFileInputStream by opening a connection to an actual file in assets,
     * the file named by the AssetFile file.
     * A new AssetFileDescriptor object is created to represent this file connection.
     *
     * @param file the assetFile to be opened for reading.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileInputStream (@NonNull AssetFile file) throws IOException {
        this(Utils.getApplication().getAssets().openFd(file.getPath()));
    }

    /**
     * Creates a FileInputStream by using the asset file descriptor fdObj, which represents
     * an existing connection to an actual file in assets.
     *
     * @param fdObj the asset file descriptor to be opened for reading.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileInputStream (@NonNull AssetFileDescriptor fdObj) throws IOException {
        AFD = fdObj;
        STREAM = AFD.createInputStream();
    }

    /**
     * Repositions this stream to the position at the time the mark method was last called on this input stream.
     *
     * The general contract of reset is:
     *
     * If the method markSupported returns true, then:
     * If the method mark has not been called since the stream was created,
     * or the number of bytes read from the stream since mark was last called is larger than the argument to mark at that last call,
     * then an IOException might be thrown.
     * If such an IOException is not thrown,
     * then the stream is reset to a state such that all the bytes read since
     * the most recent call to mark (or since the start of the file, if mark has not been called)
     * will be resupplied to subsequent callers of the read method,
     * followed by any bytes that otherwise would have been the next input data as of the time of the call to reset.
     * If the method markSupported returns false, then:
     * The call to reset may throw an IOException.
     * If an IOException is not thrown, then the stream is reset to a fixed state that depends on the
     * particular type of the input stream and how it was created.
     * The bytes that will be supplied to subsequent callers of the read method depend on the particular type of the input stream.
     * The method reset for class InputStream does nothing except throw an IOException.
     *
     * @throws IOException If an I/O error occurred
     */
    @Override
    public synchronized void reset() throws IOException {
        STREAM.reset();
    }

    /**
     * Tests if this input stream supports the mark and reset methods. Whether or not mark and reset are supported
     * is an invariant property of a particular input stream instance.
     * The markSupported method of InputStream returns false.
     * @return true if this stream instance supports the mark and reset methods; false otherwise.
     */
    @Override
    public boolean markSupported() {
        return STREAM.markSupported();
    }

    /**
     * Marks the current position in this input stream.
     * A subsequent call to the reset method repositions this stream at the last marked position so that
     * subsequent reads re-read the same bytes.
     *
     * The readlimit arguments tells this input stream to allow that many bytes
     * to be read before the mark position gets invalidated.
     *
     * The general contract of mark is that, if the method markSupported returns true,
     * the stream somehow remembers all the bytes read after the call to mark and
     * stands ready to supply those same bytes again if and whenever the method reset is called.
     * However, the stream is not required to remember any data at all if more than readlimit bytes are read
     * from the stream before reset is called.
     *
     * Marking a closed stream should not have any effect on the stream.
     *
     * The mark method of InputStream does nothing.
     *
     * @param readlimit the maximum limit of bytes that can be read before the mark position becomes invalid.
     */
    @Override
    public synchronized void mark(int readlimit) {
        STREAM.mark(readlimit);
    }

    /**
     * Returns an estimate of the number of remaining bytes that can be read (or skipped over)
     * from this input stream without blocking by the next invocation of a method for this input stream.
     * Returns 0 when the file position is beyond EOF. The next invocation might be the same thread or another thread.
     * A single read or skip of this many bytes will not block, but may read or skip fewer bytes.
     *
     * In some cases, a non-blocking read (or skip) may appear to be blocked when it is merely slow,
     * for example when reading large files over slow networks.
     * @return an estimate of the number of remaining bytes that can be read (or skipped over)
     * from this input stream without blocking.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public int available() throws IOException {
        return STREAM.available();
    }

    /**
     * Closes this file input stream and releases any system resources associated with the stream.
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
     * Reads up to b.length bytes of data from this input stream into an array of bytes.
     * This method blocks until some input is available.
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer,
     * or -1 if there is no more data because the end of the file has been reached.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public int read(byte[] b) throws IOException {
        return STREAM.read(b);
    }

    /**
     * Reads up to len bytes of data from this input stream into an array of bytes.
     * If len is not zero, the method blocks until some input is available;
     * otherwise, no bytes are read and 0 is returned.
     * @param b the buffer into which the data is read.
     * @param off the start offset in the destination array b
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer,
     * or -1 if there is no more data because the end of the file has been reached.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return STREAM.read(b, off, len);
    }

    /**
     * Reads a byte of data from this input stream. This method blocks if no input is yet available.
     * @return the next byte of data, or -1 if the end of the file is reached.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public int read() throws IOException {
        return STREAM.read();
    }

    /**
     * Skips over and discards n bytes of data from the input stream.
     *
     * The skip method may, for a variety of reasons,
     * end up skipping over some smaller number of bytes,
     * possibly 0. If n is negative, the method will try to skip backwards.
     * In case the backing file does not support backward skip at its current position,
     * an IOException is thrown. The actual number of bytes skipped is returned.
     * If it skips forwards, it returns a positive value. If it skips backwards, it returns a negative value.
     *
     * This method may skip more bytes than what are remaining in the backing file.
     * This produces no exception and the number of bytes skipped may include some number of bytes
     * that were beyond the EOF of the backing file. Attempting to read from the stream
     * after skipping past the end will result in -1 indicating the end of the file.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException If an I/O error occurred
     */
    @Override
    public long skip(long n) throws IOException {
        return STREAM.skip(n);
    }

    /**
     * Returns the unique FileChannel object associated with this asset file input stream.
     *
     * The initial position of the returned channel will be equal to the number of bytes read from the file so far.
     * Reading bytes from this stream will increment the channel's position.
     * Changing the channel's position, either explicitly or by reading, will change this stream's file position.
     *
     * @return the file channel associated with this asset file input stream
     */
    public FileChannel getChannel() {
        return STREAM.getChannel();
    }

    /**
     * Returns the FileDescriptor object that represents the connection to the actual file
     * in assets being used by this AssetFileInputStream.
     *
     * @return the file descriptor object associated with this stream.
     * @throws IOException if an I/O error occurs.
     */
    public final FileDescriptor getFD() throws IOException {
        return STREAM.getFD();
    }

    /**
     * Returns the AssetFileDescriptor object that represents the connection to the actual file
     * in assets being used by this AssetFileInputStream.
     *
     * @return the asset file descriptor object associated with this stream.
     */
    public final AssetFileDescriptor getAFD() {
        return AFD;
    }

    /**
     * Ensures that the close method of this asset file input stream is called when there are no more references to it.
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
        if (!(o instanceof AssetFileInputStream)) return false;
        AssetFileInputStream that = (AssetFileInputStream) o;
        return closed == that.closed &&
                STREAM.equals(that.STREAM) &&
                getAFD().equals(that.getAFD());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {STREAM, closed, getAFD()});
    }

}
