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
        super(new AssetFileOutputStream(fileName),
                EncodingDetector.getEncoding(new AssetFileInputStream(fileName), true));
    }

    /**
     * Constructs an AssetFileWriter object given an AssetFile object.
     *
     * @param file an AssetFile object to write to.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileWriter(@NonNull AssetFile file) throws IOException {
        super(new AssetFileOutputStream(file),
                EncodingDetector.getEncoding(new AssetFileInputStream(file), true));
    }

    /**
     * Constructs an AssetFileWriter object associated with an asset file descriptor.
     *
     * @param afd AssetFileDescriptor object to write to.
     * @throws IOException If an I/O error occurred
     */
    public AssetFileWriter(@NonNull AssetFileDescriptor afd) throws IOException {
        super(new AssetFileOutputStream(afd),
                EncodingDetector.getEncoding(new AssetFileInputStream(afd), true));
    }

}
