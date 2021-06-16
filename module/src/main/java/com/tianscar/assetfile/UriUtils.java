/*
 * Copyright 2021 Tianscar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tianscar.assetfile;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * A utility class providing functions about uri.
 */
final class UriUtils {

    /**
     * Convert android Uri to absolute file path.
     *
     * @param uri An android Uri.
     * @return the file path; null if the Uri cannot be converted to an absolute file path.
     */
    public static @Nullable String uri2path(Uri uri) {
        String path = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(ApplicationUtils.getApplication(), uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] splits = docId.split(":");
                String type = null;
                String id = null;
                if (splits.length == 2) {
                    type = splits[0];
                    id = splits[1];
                }
                String authority = uri.getAuthority();
                if (authority != null) {
                    if (authority.equals("com.android.externalstorage.documents")) {
                        if( "primary".equals(type)) {
                            path = Environment.getExternalStorageDirectory() + File.separator + id;
                        }
                    }
                    if (authority.equals("com.android.providers.downloads.documents")) {
                        if("raw".equals(type)) {
                            path = id;
                        }
                        else {
                            Uri contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                            path = uri2contentPath(contentUri, null, null);
                        }
                    }
                    if (authority.equals("com.android.providers.media.documents")) {
                        Uri externalUri = null;
                        if (type != null) {
                            if (type.equals("image")) {
                                externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            }
                            if (type.equals("video")) {
                                externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            }
                            if (type.equals("audio")) {
                                externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            }
                        }
                        if (externalUri != null) {
                            String selection = "_id=?";
                            String[] selectionArgs = new String[]{id};
                            path = uri2contentPath(externalUri, selection, selectionArgs);
                        }
                    }
                }
                return path == null ? null : (new File(path).exists() ? path : null);
            }
        }
        if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            path = uri2contentPath(uri, null, null);
        }
        if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }
        return path == null ? null : (new File(path).exists() ? path : null);
    }

    private static String uri2contentPath(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String path;
        path = uri.getPath();
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if ((path == null) || (!new File(path).exists())) {
            ContentResolver resolver = ApplicationUtils.getApplication().getContentResolver();
            String[] projection = new String[]{MediaStore.MediaColumns.DATA};
            Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    try {
                        int index = cursor.getColumnIndexOrThrow(projection[0]);
                        if (index != -1) path = cursor.getString(index);
                    }
                    catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        path = null;
                    }
                    finally {
                        cursor.close();
                    }
                }
            }
        }
        else {
            if(!path.startsWith(sdPath)) {
                int sepIndex = path.indexOf(File.separator, 1);
                if(sepIndex == -1) path = null;
                else {
                    path = sdPath + path.substring(sepIndex);
                }
            }
        }
        return path;
    }

}
