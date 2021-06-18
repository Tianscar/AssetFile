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

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * An OutputStreamWriter that packaging an AssetFileOutputStream for use.
 */
public class AssetFileWriter extends OutputStreamWriter {

    /**
     * Constructs an AssetFileWriter object given an assetFile name.
     *
     * @param fileName The assetFile name.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileWriter(@NonNull String fileName) throws IOException {
        super(new AssetFileOutputStream(fileName));
    }

    /**
     * Constructs an AssetFileWriter object given an AssetFile object.
     *
     * @param file an AssetFile object to write to.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileWriter(@NonNull AssetFile file) throws IOException {
        super(new AssetFileOutputStream(file));
    }

    /**
     * Constructs an AssetFileWriter object associated with an asset file descriptor.
     *
     * @param afd AssetFileDescriptor object to write to.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileWriter(@NonNull AssetFileDescriptor afd) throws IOException {
        super(new AssetFileOutputStream(afd));
    }

}
