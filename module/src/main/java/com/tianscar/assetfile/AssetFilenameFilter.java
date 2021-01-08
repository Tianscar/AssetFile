package com.tianscar.assetfile;

/**
 * Instances of classes that implement this interface are used to filter filenames.
 * These instances are used to filter directory listings in the list method of class AssetFile.
 */
public interface AssetFilenameFilter {

    /**
     * Tests if a specified assetFile should be included in a file list.
     *
     * @param dir the directory in which the file was found.
     * @param name the name of the file.
     * @return true if and only if the name should be included in the file list; false otherwise.
     */
    boolean accept(AssetFile dir, String name);

}