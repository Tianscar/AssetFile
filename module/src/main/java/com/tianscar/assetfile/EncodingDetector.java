package com.tianscar.assetfile;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

final class EncodingDetector {

    public static final class Constants {
        static final String CHARSET_ISO_2022_JP  = "ISO-2022-JP";
        static final String CHARSET_ISO_2022_CN  = "ISO-2022-CN";
        static final String CHARSET_ISO_2022_KR  = "ISO-2022-KR";
        static final String CHARSET_ISO_8859_5   = "ISO-8859-5";
        static final String CHARSET_ISO_8859_7   = "ISO-8859-7";
        static final String CHARSET_ISO_8859_8   = "ISO-8859-8";
        static final String CHARSET_BIG5         = "BIG5";
        static final String CHARSET_GB18030      = "GB18030";
        static final String CHARSET_EUC_JP       = "EUC-JP";
        static final String CHARSET_EUC_KR       = "EUC-KR";
        static final String CHARSET_EUC_TW       = "EUC-TW";
        static final String CHARSET_SHIFT_JIS    = "SHIFT_JIS";
        static final String CHARSET_IBM855       = "IBM855";
        static final String CHARSET_IBM866       = "IBM866";
        static final String CHARSET_KOI8_R       = "KOI8-R";
        static final String CHARSET_MACCYRILLIC  = "MACCYRILLIC";
        static final String CHARSET_WINDOWS_1251 = "WINDOWS-1251";
        static final String CHARSET_WINDOWS_1252 = "WINDOWS-1252";
        static final String CHARSET_WINDOWS_1253 = "WINDOWS-1253";
        static final String CHARSET_WINDOWS_1255 = "WINDOWS-1255";
        static final String CHARSET_UTF_8        = "UTF-8";
        static final String CHARSET_UTF_16BE     = "UTF-16BE";
        static final String CHARSET_UTF_16LE     = "UTF-16LE";
        static final String CHARSET_UTF_32BE     = "UTF-32BE";
        static final String CHARSET_UTF_32LE     = "UTF-32LE";

        // WARNING: Listed below are charsets which Java does not support.
        static final String CHARSET_HZ_GB_2312   = "HZ-GB-2312".intern(); // Simplified Chinese
        static final String CHARSET_X_ISO_10646_UCS_4_3412 = "X-ISO-10646-UCS-4-3412".intern(); // Malformed UTF-32
        static final String CHARSET_X_ISO_10646_UCS_4_2143 = "X-ISO-10646-UCS-4-2143".intern(); // Malformed UTF-32
    }

    private EncodingDetector(){}

    public static @Nullable String getEncoding (@NonNull String path) {
        return getEncoding(new File(path));
    }

    public static @Nullable String getEncoding (@NonNull String path, int readLength) {
        return getEncoding(new File(path), readLength);
    }

    public static @Nullable String getEncoding (@NonNull File file) {
        return getEncoding(file, (int) file.length());
    }

    public static @Nullable String getEncoding (@NonNull File file, int readLength) {
        if (!file.exists()) {
            return null;
        }
        if (file.isDirectory()) {
            return null;
        }
        try {
            return getEncoding(new FileInputStream(file), readLength);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable String getEncoding (@NonNull InputStream stream) {
        try {
            return getEncoding(stream, stream.available());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable String getEncoding (@NonNull InputStream stream, boolean forceClose) {
        try {
            return getEncoding(stream, stream.available(), forceClose);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable String getEncoding (@NonNull InputStream stream, int readLength) {
        return getEncoding(stream, readLength, false);
    }

    public static @Nullable String getEncoding (@NonNull InputStream stream, int readLength, boolean forceClose) {
        String charset = null;
        try {
            readLength = MathUtils.clamp(readLength, 0, stream.available());
            if ((!forceClose) && stream.markSupported()) {
                stream.mark(readLength);
            }
            byte[] data = new byte[readLength];
            int count = stream.read(data, 0, readLength);
            while (count < readLength) {
                count += stream.read(data, count, readLength - count);
            }
            if ((!forceClose) && stream.markSupported()) {
                stream.reset();
            }
            else {
                stream.close();
            }
            UniversalDetector detector = new UniversalDetector(null);
            detector.handleData(data, 0, readLength);
            detector.dataEnd();
            charset = detector.getDetectedCharset();
            detector.reset();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return charset;
    }

    public static @Nullable String getEncoding (@NonNull byte[] data) {
        return getEncoding(data, data.length);
    }

    public static @Nullable String getEncoding (@NonNull byte[] data, int readLength) {
        String charset;
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(data, 0, readLength);
        detector.dataEnd();
        charset = detector.getDetectedCharset();
        detector.reset();
        return charset;
    }

    public static @Nullable String getEncoding (@NonNull Uri uri) {
        return getEncoding(UriUtils.uri2path(uri));
    }

    public static @Nullable String getEncoding (@NonNull Uri uri, int readLength) {
        return getEncoding(UriUtils.uri2path(uri), readLength);
    }

    public static @Nullable String getEncoding (@NonNull URI uri) {
        try {
            return getEncoding(uri.toURL());
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable String getEncoding (@NonNull URI uri, int readLength) {
        try {
            return getEncoding(uri.toURL(), readLength);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable String getEncoding (@NonNull URL url) {
        try {
            return getEncoding(url.openStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static @Nullable String getEncoding (@NonNull URL url, int readLength) {
        try {
            return getEncoding(url.openStream(), readLength);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
