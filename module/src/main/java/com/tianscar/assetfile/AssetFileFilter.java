package com.tianscar.assetfile;

/**
 * A filter for abstract pathnames.
 *
 * Instances of this interface may be passed to the AssetFile#listFiles method of the AssetFile class.
 */
public interface AssetFileFilter {

    /**
     * Tests whether or not the specified abstract pathname should be included in a pathname list.
     *
     * @param pathname The abstract pathname to be tested
     * @return true if and only if pathname should be included
     */
    boolean accept(AssetFile pathname);

}
