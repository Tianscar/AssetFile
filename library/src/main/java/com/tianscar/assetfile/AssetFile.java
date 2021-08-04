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

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A File-like class that provides an abstract representation of file and directory pathnames (in assets).
 *
 * Instances of the AssetFile class are immutable;
 * that is, once created, the abstract pathname represented by a File object will never change.
 */
public class AssetFile {

    private final String path;
    private final String name;

    private static final String ANDROID_ASSET_URI_PREFIX = "file:///android_asset/";

    /**
     * Creates a new AssetFile instance by converting the given pathname string into an abstract pathname.
     *
     * @param pathname A pathname string
     */
    public AssetFile(@NonNull String pathname) {
        if (pathname.equals("/")) {
            this.path = "";
        }
        else {
            if (pathname.startsWith("/")) {
                this.path = pathname.substring(1);
            }
            else {
                this.path = pathname;
            }
        }
        this.name = parseName(this.path);
    }

    /**
     * Creates a new AssetFile instance from a parent pathname string and a child pathname string.
     *
     * @param parent The parent pathname string
     * @param child  The child pathname string
     */
    public AssetFile(@NonNull String parent, @NonNull String child) {
        this (parsePath(parent, child));
    }

    /**
     * Creates a new AssetFile instance from a parent abstract pathname and a child pathname string.
     *
     * @param parent The parent abstract pathname
     * @param child  The child pathname string
     */
    public AssetFile(@NonNull AssetFile parent, @NonNull String child) {
        this (parent.getPath(), child);
    }

    /**
     * Creates a new AssetFile instance by converting the given file: Uri into an abstract pathname.
     *
     * @param uri Uri: An absolute, hierarchical Uri with a non-empty path component,
     *            and undefined authority, query, and fragment components
     * @throws IllegalArgumentException If the preconditions on the parameter do not hold
     */
    public AssetFile(@NonNull Uri uri) {
        this (parseUri(uri));
    }

    /**
     * Creates a new AssetFile instance by converting the given file: URI into an abstract pathname.
     *
     * @param uri URI: An absolute, hierarchical URI with a scheme equal to "file"
     *            and a scheme specific part starts with "///android_asset/"
     *            a non-empty path component, and undefined authority, query, and fragment components
     * @throws IllegalArgumentException If the preconditions on the parameter do not hold
     */
    public AssetFile(@NonNull URI uri) {
        this (parseURI(uri));
    }

    /**
     * Compares two abstract pathnames lexicographically.
     * The ordering defined by this method depends upon the underlying system.
     * Alphabetic case is significant in comparing pathnames.
     *
     * @param   pathname The abstract pathname to be compared to this abstract pathname
     * @return 	Zero if the argument is equal to this abstract pathname,
     * a value less than zero if this abstract pathname is lexicographically less than the argument,
     * or a value greater than zero if this abstract pathname is lexicographically greater than the argument
     */
    public int compareTo (@NonNull AssetFile pathname) {
        return pathname.getPath().compareTo(path);
    }

    private @Nullable File export (@NonNull File pathname) throws IOException {
        if (isFile()) {
            return exportFile(pathname, false);
        }
        else {
            return exportDir(pathname, false);
        }
    }

    private @Nullable File exportFile (@NonNull File pathname, boolean override) throws IOException {
        if (isDirectory()) {
            return null;
        }
        if (pathname.exists() && pathname.canWrite()) {
            if (!override) {
                return null;
            }
        }
        if (!pathname.createNewFile()) {
            return null;
        }
        try {
            InputStream input = Utils.getApplication().getAssets().open(getPath());
            int length = input.available();
            byte[] data = new byte[length];
            int count = input.read(data, 0, length);
            while (count < length) {
                count += input.read(data, count, length - count);
            }
            input.close();
            FileOutputStream output = new FileOutputStream(pathname);
            output.write(data);
            output.close();
            return pathname;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by its name and a parent pathname string
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param parent The parent pathname string
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File createNewFile (@NonNull String parent) throws IOException {
        return exportFile(new File(parent, getName()), false);
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by its name and a parent abstract pathname
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param parent The parent abstract pathname
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File createNewFile (@NonNull File parent) throws IOException
    {
        return exportFile(new File(parent, getName()), false);
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by its name and a parent Uri (into an abstract pathname)
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param parent The parent uri - an absolute, valid android Uri that can be converted to path
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File createNewFile (@NonNull Uri parent) throws IOException {
        return exportFile(new File(Utils.uri2path(parent), getName()), false);
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by its name and a parent URI (into an abstract pathname)
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param parent The parent URI - An absolute, hierarchical URI with a scheme equal to "file",
     *             a non-empty path component, and undefined authority, query, and fragment components
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     * @throws IllegalArgumentException If the preconditions on the parameter do not hold
     */
    public @Nullable File createNewFile (@NonNull URI parent) throws IOException {
        return exportFile(new File(new File(parent), getName()), false);
    }

    /**
     * Creates a new file that copies the given assetFile 's data in the specified directory,
     * using the given prefix and suffix strings to generate its name.
     * If this method returns successfully then it is guaranteed that:
     *
     * The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * Neither this method nor any of its variants will return the same abstract pathname
     * again in the current invocation of the virtual machine.
     * This method provides only part of a temporary-file facility.
     * To arrange for a file created by this method to be deleted automatically, use the deleteOnExit() method.
     * The prefix argument must be at least three characters long.
     * It is recommended that the prefix be a short, meaningful string such as "hjb" or "mail".
     * The suffix argument may be null, in which case the suffix ".tmp" will be used.
     *
     * To create the new file, the prefix and the suffix may first be adjusted to fit the limitations
     * of the underlying platform. If the prefix is too long then it will be truncated,
     * but its first three characters will always be preserved.
     * If the suffix is too long then it too will be truncated,
     * but if it begins with a period character ('.') then the period and the first three characters
     * following it will always be preserved. Once these adjustments have been made the name
     * of the new file will be generated by concatenating the prefix, five or more internally-generated characters, and the suffix.
     *
     * If the directory argument is null then the system-dependent default temporary-file directory will be used.
     * The default temporary-file directory is specified by the system property java.io.tmpdir.
     * The default value of this property is typically "/tmp" or "/var/tmp"
     *
     * @param prefix    The prefix string to be used in generating the file's name; must be at least three characters long
     * @param suffix    The suffix string to be used in generating the file's name; may be null,
     *                  in which case the suffix ".tmp" will be used
     * @param directory The directory in which the file is to be created,
     *                  or null if the default temporary-file directory is to be used
     * @return 	An abstract pathname denoting a newly-created empty file
     * @throws IllegalArgumentException	If the prefix argument contains fewer than three characters
     * @throws IOException 	If a file could not be created
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    @NonNull
    public static File createTempFile (@NonNull String prefix, @Nullable String suffix, @Nullable File directory,
                                       @NonNull AssetFile file) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix, directory);
        file.exportFile(tempFile, true);
        return tempFile;
    }

    /**
     * Creates an new file that copies the given assetFile 's data in the default temporary-file directory,
     * using the given prefix and suffix to generate its name.
     * Invoking this method is equivalent to invoking createTempFile(prefix, suffix, null).
     *
     * The Files.createTempFile method provides an alternative method to create an empty file
     * in the temporary-file directory.
     * Files created by that method may have more restrictive access permissions to files created
     * by this method and so may be more suited to security-sensitive applications.
     *
     * @return An abstract pathname denoting a newly-created empty file
     * @throws IllegalArgumentException	If the prefix argument contains fewer than three characters
     * @throws IOException 	If a file could not be created
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    @NonNull
    public static File createTempFile (@NonNull String prefix, @Nullable String suffix,
                                       @NonNull AssetFile file) throws IOException {
        return createTempFile(prefix, suffix, null, file);
    }

    /**
     * Tests whether the file or directory denoted by this abstract pathname exists (in assets).
     *
     * @return true if and only if the file or directory denoted by this abstract pathname exists (in assets);
     * false otherwise
     */
    public boolean exists () {
        if (isRootDir()) {
            return true;
        }
        try {
            Utils.getApplication().getAssets().open(getPath());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the name of the file or directory denoted by this abstract pathname.
     * This is just the last name in the pathname's name sequence.
     * If the pathname's name sequence is empty, then the empty string is returned.
     *
     * @return The name of the file or directory denoted by this abstract pathname,
     * or the empty string if this pathname's name sequence is empty
     */
    public @NonNull String getName () {
        return name;
    }

    /**
     * Returns the pathname string of this abstract pathname's parent,
     * or null if this pathname does not name a parent directory.
     *
     * The parent of an abstract pathname consists of the pathname's prefix,
     * if any, and each name in the pathname's name sequence except for the last.
     * If the name sequence is empty then the pathname does not name a parent directory.
     *
     * @return 	The pathname string of the parent directory named by this abstract pathname,
     * or null if this pathname does not name a parent
     */
    public @Nullable String getParent () {
        if (path.equals("")) {
            return null;
        }
        int index = path.lastIndexOf("/");
        if (index != -1) {
            return path.substring(0, index);
        }
        else {
            return "";
        }
    }

    /**
     * Returns the abstract pathname of this abstract pathname's parent,
     * or null if this pathname does not name a parent directory.
     *
     * The parent of an abstract pathname consists of the pathname's prefix,
     * if any, and each name in the pathname's name sequence except for the last.
     * If the name sequence is empty then the pathname does not name a parent directory.
     *
     * @return 	The abstract pathname of the parent directory named by this abstract pathname,
     * or null if this pathname does not name a parent
     */
    public @Nullable
    AssetFile getParentFile () {
        String parent = getParent();
        if (parent == null) {
            return null;
        }
        return new AssetFile(parent);
    }

    /**
     * Converts this abstract pathname into a pathname string.
     * The resulting string uses the default name-separator character
     * to separate the names in the name sequence.
     *
     * @return The string form of this abstract pathname
     */
    public @NonNull String getPath() {
        return path;
    }

    /**
     * Tests this abstract pathname for equality with the given object.
     * Returns true if and only if the argument is not null
     * and is an abstract pathname that denotes the same file or directory as this abstract pathname.
     *
     * @param obj - Object: The object to be compared with this abstract pathname
     * @return true if and only if the objects are the same; false otherwise
     */
    @Override
    public boolean equals (Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AssetFile)) return false;
        AssetFile file = (AssetFile) obj;
        return isDirectory() == file.isDirectory() &&
                getPath().equals(file.getPath()) &&
                getName().equals(file.getName());
    }

    /**
     * Computes a hash code for this abstract pathname.
     * The hash code of an abstract pathname is equal to the exclusive or of the hash code of its pathname string
     * and the decimal value 1234321.
     *
     * @return A hash code for this abstract pathname
     */
    @Override
    public int hashCode () {
        return Arrays.hashCode(new Object[] {getPath(), getName(), 1234321});
    }

    /**
     * Tests whether the directory denoted by this abstract pathname is the assets root dir.
     *
     * @return true if and only if the directory is the assets root dir;
     * false otherwise
     */
    public boolean isRootDir () {
        return getPath().equals("");
    }

    /**
     * Tests whether the file denoted by this abstract pathname is a directory.
     *
     * @return true if and only if the file denoted by this abstract pathname exists and is a directory;
     * false otherwise
     */
    public boolean isDirectory () {
        if (!exists()) {
            return false;
        }
        return !isFile();
    }

    /**
     * Tests whether the file denoted by this abstract pathname is a file.
     *
     * @return 	true if and only if the file denoted by this abstract pathname exists and is a file;
     * false otherwise
     */
    public boolean isFile () {
        try {
            return Utils.getApplication().getAssets().open(getPath()).available() >= 0;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the length of the file denoted by this abstract pathname.
     * The return value is unspecified if this pathname denotes a directory.
     * Where it is required to distinguish an I/O exception from the case that 0 is returned.
     *
     * @return The length, in bytes, of the file denoted by this abstract pathname, or 0 if the file does not exist.
     */
    public int length () {
        int result = 0;
        if (isDirectory()) {
            return result;
        }
        try {
            InputStream stream = Utils.getApplication().getAssets().open(path);
            result = stream.available();
            stream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns an array of strings naming the files (and directories if is root dir)
     * in the directory denoted by this abstract pathname.
     *
     * If this abstract pathname does not denote a directory,
     * then this method returns null.
     * Otherwise an array of strings is returned,
     * one for each file (and directories if is root dir) in the directory.
     * Names denoting the directory itself and the directory's parent directory are
     * not included in the result.
     * Each string is a file name rather than a complete path.
     *
     * There is no guarantee that the name strings in the resulting array will appear in any specific order;
     * they are not, in particular, guaranteed to appear in alphabetical order.
     *
     * @return An array of strings naming the files (and directories if is root dir) in the directory denoted by this abstract pathname.
     * The array will be empty if the directory is empty.
     * Returns null if this abstract pathname does not denote a directory,
     * or if an I/O error occurs.
     */
    public @Nullable String[] list () {
        return list(null);
    }

    /**
     * Returns an array of strings naming the files (and directories if is root dir) in the directory
     * denoted by this abstract pathname that satisfy the specified filter.
     * The behavior of this method is the same as that of the list() method,
     * except that the strings in the returned array must satisfy the filter.
     * If the given filter is null then all names are accepted. Otherwise,
     * a name satisfies the filter if and only if the value true results when
     * the FilenameFilter#accept method of the filter is invoked on this abstract pathname and
     * the name of a file or directory in the directory that it denotes.
     *
     * @param filter An asset filename filter
     * @return 	An array of strings naming the files (and directories if is root dir) in the
     * directory denoted by this abstract pathname that were accepted by the given filter.
     * The array will be empty if the directory is empty or if no names were accepted by the filter.
     * Returns null if this abstract pathname does not denote a directory,
     * or if an I/O error occurs.
     */
    public @Nullable String[] list (@Nullable AssetFilenameFilter filter) {
        if (!isDirectory()) {
            return null;
        }
        String[] result = null;
        try {
            String[] nameArray = Utils.getApplication().getAssets().list(path);
            if (filter == null) {
                return nameArray;
            }
            else {
                if (nameArray != null) {
                    List<String> nameList = new ArrayList<>();
                    for (String name : nameArray) {
                        if (filter.accept(new AssetFile(path, name), name)) {
                            nameList.add(name);
                        }
                    }
                    result = (String[]) nameList.toArray();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    /**
     * Returns an array of abstract pathnames denoting the files (and directories if is root dir)
     * in the directory denoted by this abstract pathname.
     *
     * If this abstract pathname does not denote a directory,
     * then this method returns null.
     * Otherwise an array of AssetFile objects is returned,
     * one for each file (or directory if is root dir) in the directory.
     * Pathnames denoting the directory itself and the directory's parent directory
     * are not included in the result.
     * Each resulting pathname is relative to the assets root dir.
     *
     * There is no guarantee that the name strings in the resulting array will appear in any specific order;
     * they are not, in particular, guaranteed to appear in alphabetical order.
     *
     * @return An array abstract pathnames denoting the files (and directories if is root dir) in the
     * directory denoted by this abstract pathname.
     * The array will be empty if the directory is empty or if no names were accepted by the filter.
     * Returns null if this abstract pathname does not denote a directory,
     * or if an I/O error occurs.
     */
    public @Nullable AssetFile[] listFiles () {
        return listFiles((AssetFileFilter) null);
    }

    /**
     * Returns an array of abstract pathnames denoting the files (and directories if is root dir)
     * in the directory denoted by this abstract pathname that satisfy the specified filter.
     * The behavior of this method is the same as that of the listFiles() method,
     * except that the pathnames in the returned array must satisfy the filter.
     * If the given filter is null then all pathnames are accepted. Otherwise,
     * a pathname satisfies the filter if and only if the value true results
     * when the AssetFileFilter#accept method of the filter is invoked on the pathname.
     *
     * @param filter An asset file filter
     * @return An array abstract pathnames denoting the files (and directories if is root dir) in the
     * directory denoted by this abstract pathname.
     * The array will be empty if the directory is empty or if no names were accepted by the filter.
     * Returns null if this abstract pathname does not denote a directory,
     * or if an I/O error occurs.
     */
    public @Nullable AssetFile[] listFiles (@Nullable AssetFileFilter filter) {
        if (!isDirectory()) {
            return null;
        }
        List<AssetFile> fileList = new ArrayList<>();
        try {
            String[] nameArray = Utils.getApplication().getAssets().list(path);
            if (nameArray != null) {
                for (String name : nameArray) {
                    AssetFile file = new AssetFile(path, name);
                    if (filter == null) {
                        fileList.add(file);
                    }
                    else {
                        if (filter.accept(file)) {
                            fileList.add(file);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return (AssetFile[]) fileList.toArray();
    }

    /**
     * Returns an array of abstract pathnames denoting the files (and directories if is root dir)
     * in the directory denoted by this abstract pathname that satisfy the specified filter.
     * The behavior of this method is the same as that of the listFiles() method,
     * except that the pathnames in the returned array must satisfy the filter.
     * If the given filter is null then all pathnames are accepted.
     * Otherwise, a pathname satisfies the filter if and only if the value true results
     * when the FilenameFilter#accept method of the filter is invoked on this abstract pathname
     * and the name of a file or directory in the directory that it denotes.
     *
     * @param filter An asset filename filter
     * @return An array abstract pathnames denoting the files (and directories if is root dir) in the
     * directory denoted by this abstract pathname.
     * The array will be empty if the directory is empty or if no names were accepted by the filter.
     * Returns null if this abstract pathname does not denote a directory,
     * or if an I/O error occurs.
     */
    public @Nullable AssetFile[] listFiles (@Nullable AssetFilenameFilter filter) {
        if (!isDirectory()) {
            return null;
        }
        List<AssetFile> fileList = new ArrayList<>();
        try {
            String[] nameArray = Utils.getApplication().getAssets().list(path);
            if (nameArray != null) {
                for (String name : nameArray) {
                    AssetFile file = new AssetFile(path, name);
                    if (filter == null) {
                        fileList.add(file);
                    }
                    else {
                        if (filter.accept(file.getParentFile(), file.getName())) {
                            fileList.add(file);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return (AssetFile[]) fileList.toArray();
    }

    private @Nullable File exportDir (@NonNull File pathname, boolean mkdirs) throws IOException {
        if (!isDirectory()) {
            return null;
        }
        if (pathname.exists()) {
            return null;
        }
        if (mkdirs) {
            if (!pathname.mkdirs()) {
                return null;
            }
        }
        else {
            if (!pathname.mkdir()) {
                return null;
            }
        }
        AssetFile[] files = listFiles();
        if (files != null) {
            for (AssetFile asset : files) {
                if (asset.isFile()) {
                    asset.createNewFile(pathname);
                }
                else {
                    asset.exportDir(new File(pathname, asset.getPath()), mkdirs);
                }
            }
        }
        return pathname;
    }

    /**
     * Creates the directory from a parent pathname string that copies all the files in the directory.
     *
     * @param parent The parent pathname string
     * @return The directory if and only if the directory was created; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdir (@NonNull String parent) throws IOException {
        return exportDir(new File(new File(parent), getPath()), false);
    }

    /**
     * Creates the directory from a parent abstract pathname that copies all the files in the directory.
     *
     * @param parent The parent abstract pathname
     * @return The directory if and only if the directory was created; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdir (@NonNull File parent) throws IOException {
        return exportDir(new File(parent, getPath()), false);
    }

    /**
     * Creates the directory from a parent Uri (into an abstract pathname) that copies all the files in the directory.
     *
     * @param parent The parent uri - an absolute, valid android Uri that can be converted to path
     * @return The directory if and only if the directory was created; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdir (@NonNull Uri parent) throws IOException {
        String dirPath = Utils.uri2path(parent);
        if (dirPath != null) {
            return exportDir(new File(new File(dirPath), getPath()), false);
        }
        return null;
    }

    /**
     * Creates the directory from a parent URI (into an abstract pathname) that copies all the files in the directory.
     *
     * @param parent The parent URI - An absolute, hierarchical URI with a scheme equal to "file",
     *               a non-empty path component, and undefined authority, query, and fragment components
     * @return The directory if and only if the directory was created; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdir (@NonNull URI parent) throws IOException {
        return exportDir(new File(new File(parent), getPath()), false);
    }

    /**
     * Creates the directory from a parent pathname string that copies all the files in the directory,
     * including any necessary but nonexistent parent directories.
     * Note that if this operation fails it may have succeeded in creating some of the necessary parent directories.
     *
     * @param parent The parent pathname string
     * @return The directory if and only if the directory was created,
     * along with all necessary parent directories; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdirs (@NonNull String parent) throws IOException {
        return exportDir(new File(new File(parent), getPath()), true);
    }

    /**
     * Creates the directory from a parent abstract pathnamethat copies all the files in the directory,
     * including any necessary but nonexistent parent directories.
     * Note that if this operation fails it may have succeeded in creating some of the necessary parent directories.
     *
     * @param parent The parent abstract pathname
     * @return The directory if and only if the directory was created,
     * along with all necessary parent directories; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdirs (@NonNull File parent) throws IOException {
        return exportDir(new File(parent, getPath()), true);
    }

    /**
     * Creates the directory from a parent Uri (into an abstract pathname) that copies all the files in the directory,
     * including any necessary but nonexistent parent directories.
     * Note that if this operation fails it may have succeeded in creating some of the necessary parent directories.
     *
     * @param parent The parent uri - an absolute, valid android Uri that can be converted to path
     * @return The directory if and only if the directory was created,
     * along with all necessary parent directories; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdirs (@NonNull Uri parent) throws IOException {
        String dirPath = Utils.uri2path(parent);
        if (dirPath != null) {
            return exportDir(new File(new File(dirPath), getPath()), true);
        }
        return null;
    }

    /**
     * Creates the directory from a parent URI (into an abstract pathname) that copies all the files in the directory,
     * including any necessary but nonexistent parent directories.
     * Note that if this operation fails it may have succeeded in creating some of the necessary parent directories.
     *
     * @param parent The parent URI - An absolute, hierarchical URI with a scheme equal to "file",
     *               a non-empty path component, and undefined authority, query, and fragment components
     * @return The directory if and only if the directory was created,
     * along with all necessary parent directories; null otherwise
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and
     * its SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created
     */
    public @Nullable File mkdirs (@NonNull URI parent) throws IOException {
        return exportDir(new File(new File(parent), getPath()), true);
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by the given abstract pathname
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param dest The abstract pathname
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File renameTo (@NonNull File dest) throws IOException {
        return export(dest);
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by the given pathname string into an abstract pathname
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param dest The pathname string
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File renameTo (@NonNull String dest) throws IOException {
        return export(new File(dest));
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by the given pathname and a parent pathname string
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param parent The parent pathname string
     * @param dest The pathname string
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File renameTo (@NonNull String parent, @NonNull String dest) throws IOException {
        return export(new File(parent, dest));
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by the given pathname and a parent abstract pathname
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param parent The parent abstract pathname
     * @param dest The abstract pathname string
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File renameTo (@NonNull File parent, @NonNull String dest) throws IOException
    {
        return export(new File(parent, dest));
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by the given URI (into an abstract pathname)
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param dest An absolute, valid android Uri that can be converted to path
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     */
    public @Nullable File renameTo (@NonNull Uri dest) throws IOException {
        String pathname = Utils.uri2path(dest);
        if (pathname == null) {
            return null;
        }
        return export(new File(pathname));
    }

    /**
     * Atomically creates a new file that copies the assetFile 's data by the given URI (into an abstract pathname)
     * if and only if a file with this name does not yet exist.
     * The check for the existence of the file and the creation of the file
     * if it does not exist are a single operation that is atomic
     * with respect to all other filesystem activities that might affect the file.
     *
     * Note: this method should not be used for file-locking, as the resulting protocol cannot be made to work reliably.
     * The FileLock facility should be used instead.
     *
     * @param dest An absolute, hierarchical URI with a scheme equal to "file",
     *             a non-empty path component, and undefined authority, query, and fragment components
     * @return The file (or null if create failed)
     * @throws IOException 	If an I/O error occurred
     * @throws SecurityException If a security manager exists and its SecurityManager.checkWrite(java.lang.String)
     * method denies write access to the file
     * @throws IllegalArgumentException If the preconditions on the parameter do not hold
     */
    public @Nullable File renameTo (@NonNull URI dest) throws IOException {
        return export(new File(dest));
    }

    /**
     * Returns the pathname string of this abstract pathname.
     * This is just the string returned by the getPath() method.
     *
     * @return The string form of this abstract pathname
     */
    @NonNull
    public String toString () {
        return getPath();
    }

    /**
     * Constructs a file:///android_asset/ Uri that represents this abstract pathname.
     * If it can be determined that the file denoted by this abstract pathname is a directory,
     * then the resulting URI will end with a slash.
     *
     * @return An absolute, hierarchical Uri with a scheme equal to "file"
     * and a scheme specific part starts with "///android_asset/",
     * a path representing this abstract pathname, and undefined authority, query, and fragment components
     */
    public @NonNull Uri toUri () {
        return Uri.parse(ANDROID_ASSET_URI_PREFIX + (isRootDir() ? "" : path) + (isDirectory() ? "/" : ""));
    }

    /**
     * Constructs a file:///android_asset/ URI that represents this abstract pathname.
     * If it can be determined that the file denoted by this abstract pathname is a directory,
     * then the resulting URI will end with a slash.
     *
     * @return An absolute, hierarchical URI with a scheme equal to "file"
     * and a scheme specific part starts with "///android_asset/",
     * a path representing this abstract pathname, and undefined authority, query, and fragment components
     */
    @NonNull
    public URI toURI () {
        return URI.create(ANDROID_ASSET_URI_PREFIX + (isRootDir() ? "" : path) + (isDirectory() ? "/" : ""));
    }

    /**
     * This method was deprecated.
     * This method does not automatically escape characters that are illegal in URLs.
     * It is recommended that new code convert an abstract pathname into a URL by first converting it into a URI,
     * via the toURI method, and then converting the URI into a URL via the URI.toURL method.
     *
     * Converts this abstract pathname into a file:///android_asset/ URL.
     * If it can be determined that the file denoted by this abstract pathname is a directory,
     * then the resulting URL will end with a slash.
     *
     * @return A URL object representing the equivalent file URL
     * @throws MalformedURLException If the path cannot be parsed as a URL
     */
    @Deprecated
    public URL toURL () throws MalformedURLException {
        return new URL(ANDROID_ASSET_URI_PREFIX + (isRootDir() ? "" : path) + (isDirectory() ? "/" : ""));
    }

    @NonNull
    private static String parsePath (@NonNull String parent, @NonNull String child) {
        if (parent.length() < 1) {
            return child;
        }
        return parent + "/" + child;
    }

    @NonNull
    private static String parseName (@NonNull String path) {
        if (path.length() < 1) {
            return "";
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    @NonNull
    private static String parseUri (@NonNull Uri uri) {
        if (uri.isAbsolute()) {
            String pathname = uri.getScheme() + ":" + uri.getSchemeSpecificPart();
            if (pathname.startsWith(ANDROID_ASSET_URI_PREFIX)) {
                return pathname.substring(ANDROID_ASSET_URI_PREFIX.length());
            }
            else {
                throw new IllegalArgumentException("Invalid Uri: " + uri);
            }
        }
        throw new IllegalArgumentException("Invalid Uri: " + uri);
    }

    @NonNull
    private static String parseURI (@NonNull URI uri) {
        if (uri.isAbsolute()) {
            String pathname = uri.getScheme() + ":" + uri.getSchemeSpecificPart();
            if (pathname.startsWith(ANDROID_ASSET_URI_PREFIX)) {
                return pathname.substring(ANDROID_ASSET_URI_PREFIX.length());
            }
            else {
                throw new IllegalArgumentException("Invalid URI: " + uri);
            }
        }
        throw new IllegalArgumentException("Invalid URI: " + uri);
    }

}
