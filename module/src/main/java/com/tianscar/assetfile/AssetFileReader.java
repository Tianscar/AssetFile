package com.tianscar.assetfile;

import android.content.res.AssetFileDescriptor;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * An InputStreamReader that packaging an AssetFileInputStream for use.
 */
public class AssetFileReader extends InputStreamReader {

    /**
     * Creates a new AssetFileReader, given the name of the assetFile to read from.
     *
     * @param fileName the name of the assetFile to read from
     * @throws IOException If an I/O error occurred
     */
    public AssetFileReader(String fileName) throws IOException {
        super(new AssetFileInputStream(fileName));
    }

    /**
     * Creates a new AssetFileReader, given the AssetFile to read from.
     *
     * @param file the AssetFile to read from
     * @throws IOException If an I/O error occurred
     */
    public AssetFileReader(@NonNull AssetFile file) throws IOException {
        super(new AssetFileInputStream(file));
    }

    /**
     * Creates a new AssetFileReader, given the AssetFileDescriptor to read from.
     *
     * @param afd the AssetFileDescriptor to read from
     * @throws IOException If an I/O error occurred
     */
    public AssetFileReader(@NonNull AssetFileDescriptor afd) throws IOException {
        super(new AssetFileInputStream(afd));
    }

}
