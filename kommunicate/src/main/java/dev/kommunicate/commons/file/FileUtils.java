/*
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.kommunicate.commons.file;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import dev.kommunicate.commons.commons.core.utils.Utils;
import dev.kommunicate.commons.commons.image.ImageUtils;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import annotations.CleanUpRequired;


/**
 * @author Peli
 * @author paulburke (ipaulpro)
 * @version 2013-12-11
 */
@SuppressLint("NewApi")
public class FileUtils {

    public static final String MIME_TYPE_AUDIO = "audio/*";
    public static final String MIME_TYPE_TEXT = "text/*";
    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_APP = "application/*";
    public static final String HIDDEN_PREFIX = ".";
    private static final String CONTENT_PUBLIC_DOWNLOADS = "content://downloads/public_downloads";
    private static final String COM_ANDROID_EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    private static final String COM_ANDROID_DOWNLOADS = "com.android.providers.downloads.documents";
    private static final String COM_ANDROID_MEDIA = "com.android.providers.media.documents";
    private static final String COM_ANDROID_APP_PHOTOS = "com.google.android.apps.photos.content";
    private static final String KOMMUNICATE_SETTINGS_JSON = "kommunicate-settings.json";
    private static final String APPLOZIC_SETTINGS_JSON = "applozic-settings.json";

    public enum GalleryFilterOptions {
        ALL_FILES,
        IMAGE_VIDEO,
        IMAGE_ONLY,
        AUDIO_ONLY,
        VIDEO_ONLY;
    }

    /**
     * TAG for log messages.
     */
    static final String TAG = "FileUtils";
    private static final boolean DEBUG = false; // Set to true to enable logging
    /**
     * File (not directories) filter.
     *
     * @author paulburke
     */
    public static FileFilter sFileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return files only (not directories) and skip hidden files
            return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };
    /**
     * Folder (directories) filter.
     *
     * @author paulburke
     */
    public static FileFilter sDirFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return directories only and skip hidden directories
            return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };
    /**
     * File and folder comparator. TODO Expose sorting option method
     *
     * @author paulburke
     */
    public static Comparator<File> sComparator = new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
            // Sort alphabetically by lower case, which is much cleaner
            return f1.getName().toLowerCase().compareTo(
                    f2.getName().toLowerCase());
        }
    };

    private FileUtils() {
    } //private constructor to enforce Singleton pattern

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    public static boolean deleteFile(Uri uri) {
        if (uri == null) {
            Log.w(TAG, "Cannot delete file: Uri is null");
            return false;
        }
        if (uri.getPath() == null) {
            Log.w(TAG, "Cannot delete file: Uri path is null");
            return false;
        }
        File file = new File(uri.getPath());
        try {
            boolean deleted = file.delete();
            if (!deleted) {
                Log.w(TAG, "Failed to delete file: " + uri.getPath());
            }
            return deleted;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception while deleting file: " + uri.getPath(), e);
            return false;
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    public static boolean isLocal(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return true;
        }
        return false;
    }

    /**
     * @return True if Uri is a MediaStore Uri.
     * @author paulburke
     */
    public static boolean isMediaUri(Uri uri) {
        return "media".equalsIgnoreCase(uri.getAuthority());
    }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    public static Uri getUri(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();

                // Construct path without file name.
                String pathwithoutname = filepath.substring(0,
                        filepath.length() - filename.length());
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
                }
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    /**
     * @return The MIME type for the given file.
     */
    public static String getMimeType(File file) {

        String extension = getExtension(file.getName());

        if (extension.length() > 0)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

        return "application/octet-stream";
    }

    /**
     * @return The MIME type for the give Uri.
     */
    public static String getMimeType(Context context, Uri uri) {
        String filePath = getPath(context, uri);
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            return getMimeType(file);
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is {@link LocalStorageProvider}.
     * @author paulburke
     */
    public static boolean isLocalStorageDocument(Uri uri) {
        return LocalStorageProvider.AUTHORITY.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return COM_ANDROID_EXTERNAL_STORAGE.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return COM_ANDROID_DOWNLOADS.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return COM_ANDROID_MEDIA.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return COM_ANDROID_APP_PHOTOS.equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     * @see #isLocal(String)
     * @see #getFile(android.content.Context, android.net.Uri)
     */

    public static String getPath(final Context context, final Uri uri) {

        if (DEBUG)
            Utils.printLog(context, TAG + " File -",
                    "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isLocalStorageDocument(uri)) {
                // The path is the id
                return DocumentsContract.getDocumentId(uri);
            }
            // ExternalStorageProvider
            else if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                try {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse(CONTENT_PUBLIC_DOWNLOADS), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return DocumentsContract.getDocumentId(uri);
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Convert Uri into File, if possible.
     *
     * @return file A local file that the Uri was pointing to, or null if the
     * Uri is unsupported or pointed to a remote resource.
     * @author paulburke
     * @see #getPath(android.content.Context, android.net.Uri)
     */
    public static File getFile(Context context, Uri uri) {
        if (uri != null) {
            String path = getPath(context, uri);
            if (path != null && isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

    public static void writeSettingsToFile(Context context, String setting) {
        try {
            String fileName = "/" + "kommunicate-settings" + ".json";
            BufferedWriter bufferedWriter = null;
            try {
                File dir = new File(context.getFilesDir().getAbsolutePath());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, fileName);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileWriter writer = new FileWriter(file, true);
                bufferedWriter = new BufferedWriter(writer);
                bufferedWriter.append(setting);
                bufferedWriter.append("\r\n\n");
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String loadSettingsJsonFile(Context context) {
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        String line;
        try {
            if (Arrays.asList(context.getAssets().list("")).contains(KOMMUNICATE_SETTINGS_JSON)) {
                br = new BufferedReader(new InputStreamReader(context.getAssets().open(
                        KOMMUNICATE_SETTINGS_JSON), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(context.getAssets().open(
                        APPLOZIC_SETTINGS_JSON), "UTF-8"));
            }
            if (br != null) {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException ioe) {
            File dir = new File(context.getFilesDir().getAbsolutePath());
            File file = new File(dir, KOMMUNICATE_SETTINGS_JSON);
            try {
                br = new BufferedReader(new FileReader(file));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }

    public static List<String> loadRestrictedWordsFile(Context context) {

        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();

        try {
            br = new BufferedReader(new InputStreamReader(context.getAssets().open(
                    "restrictWords.txt"), "UTF-8"));
            String line;
            if (br != null) {

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException ioe) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
        }
        String outputString = sb.toString();
        String[] words = outputString.split(",");
        List<String> wordList = Arrays.asList(words);
        return wordList;
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     * @author paulburke
     */
    public static String getReadableFileSize(int size) {
        final int BYTES_IN_KILOBYTES = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KILOBYTES = " KB";
        final String MEGABYTES = " MB";
        final String GIGABYTES = " GB";
        float fileSize = 0;
        String suffix = KILOBYTES;

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = size / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    suffix = GIGABYTES;
                } else {
                    suffix = MEGABYTES;
                }
            }
        }
        return String.valueOf(dec.format(fileSize) + suffix);
    }

    /**
     * Attempt to retrieve the thumbnail of given File from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param file
     * @return
     * @author paulburke
     */
    public static Bitmap getThumbnail(Context context, File file) {
        return getThumbnail(context, getUri(file), getMimeType(file));
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param uri
     * @return
     * @author paulburke
     */
    public static Bitmap getThumbnail(Context context, Uri uri) {
        return getThumbnail(context, uri, getMimeType(context, uri));
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param uri
     * @param mimeType
     * @return
     * @author paulburke
     */
    public static Bitmap getThumbnail(Context context, Uri uri, String mimeType) {
        if (DEBUG)
            Utils.printLog(context, TAG, "Attempting to get thumbnail");

        if (!isMediaUri(uri)) {
            Utils.printLog(context, TAG, "You can only retrieve thumbnails for images and videos.");
            return null;
        }

        Bitmap bm = null;
        if (uri != null) {
            final ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    final int id = cursor.getInt(0);
                    if (DEBUG)
                        Utils.printLog(context, TAG, "Got thumb ID: " + id);

                    if (mimeType.contains("video")) {
                        bm = MediaStore.Video.Thumbnails.getThumbnail(
                                resolver,
                                id,
                                MediaStore.Video.Thumbnails.MINI_KIND,
                                null);
                    } else if (mimeType.contains(FileUtils.MIME_TYPE_IMAGE)) {
                        bm = MediaStore.Images.Thumbnails.getThumbnail(
                                resolver,
                                id,
                                MediaStore.Images.Thumbnails.MINI_KIND,
                                null);
                    }
                }
            } catch (Exception e) {
                if (DEBUG)
                    Log.e(TAG, "getThumbnail", e);
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        return bm;
    }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     * @author paulburke
     */
    @Deprecated
    @CleanUpRequired(reason = "Migrated the this use case to MediaPicker.kt")
    public static Intent createGetContentIntent(GalleryFilterOptions choosenOption, PackageManager packageManager, boolean isMultipleSectionEnabled) {
        Intent intent = new Intent();
        ArrayList<String> mimeType = new ArrayList<>();
        switch (choosenOption) {
            case ALL_FILES:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                return intent;
            case IMAGE_VIDEO:
                /// Multiple mimetypes are not supported in ACTION_PICK
                mimeType.add(MIME_TYPE_IMAGE);
                mimeType.add(MIME_TYPE_VIDEO);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    if (isMultipleSectionEnabled) {
                        intent.addCategory(Intent.ACTION_SEND_MULTIPLE);
                    }
                    intent.setType(MIME_TYPE_IMAGE);
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{MIME_TYPE_IMAGE, MIME_TYPE_VIDEO});
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    break;
                }
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (isMultipleSectionEnabled) {
                    intent.addCategory(Intent.ACTION_SEND_MULTIPLE);
                }
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{MIME_TYPE_IMAGE, MIME_TYPE_VIDEO});
                break;
            case IMAGE_ONLY:
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (isMultipleSectionEnabled) {
                    intent.addCategory(Intent.ACTION_SEND_MULTIPLE);
                }
                intent.setType(MIME_TYPE_IMAGE);
                mimeType.add(MIME_TYPE_IMAGE);
                break;
            case AUDIO_ONLY:
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                if (isMultipleSectionEnabled) {
                    intent.addCategory(Intent.ACTION_SEND_MULTIPLE);
                }
                intent.setType(MIME_TYPE_AUDIO);
                mimeType.add(MIME_TYPE_AUDIO);
                break;
            case VIDEO_ONLY:
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                if (isMultipleSectionEnabled) {
                    intent.addCategory(Intent.ACTION_SEND_MULTIPLE);
                }
                intent.setType(MIME_TYPE_VIDEO);
                mimeType.add(MIME_TYPE_VIDEO);
                break;
        }
        if (intent.resolveActivity(packageManager) == null) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(mimeType.get(0));
            if (mimeType.size() > 1) {
                String mimeTypes = "";
                for (String type : mimeType) {
                    mimeTypes += type + "|";
                }
                intent.setType(mimeTypes);
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType.get(1));
            }
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            return intent;
        }
        return intent;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (TextUtils.isEmpty(extension) && url.contains(".")) {
            extension = url.substring(url.lastIndexOf('.') + 1).toLowerCase();
        }

        if (!TextUtils.isEmpty(extension)) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension.toLowerCase());
        }

        return type;
    }

    public static Bitmap getPreview(String filePath, int reqWidth, int reqHeight, boolean enabled, String mimeType) {

        if (mimeType.startsWith("video")) {
            return ThumbnailUtils.createVideoThumbnail(filePath, 1);
        }
        return getPreview(filePath, reqWidth, reqHeight);
    }


    public static Bitmap getPreview(String filePath, int reqWidth, int reqHeight) {
        File image = new File(filePath);
        Bitmap bitmap;
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1)) {
            return null;
        }
        bounds.inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight);
        bounds.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(image.getPath(), bounds);
        return ImageUtils.getImageRotatedBitmap(bitmap, image.getPath(), bitmap.getWidth(), bitmap.getHeight());
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((height / inSampleSize) > reqHeight && (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static boolean isFileExist(String filePath) {
        return new File(filePath).exists();

    }

    public static String getFileFormat(String fileName) {

        String fileType = "";

        int i = fileName.lastIndexOf('.');

        if (i > 0) {
            fileType = fileName.substring(i + 1);
        }
        return fileType;
    }

    /**
     * This method will compressed Image to a pre-configured files.
     *
     * @param filePath
     * @param newFileName
     * @param maxFileSize
     * @return
     */
    public static File compressImageFiles(String filePath, String newFileName, int maxFileSize) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = actualWidth / actualHeight;
        int maxHeight = (2 * actualHeight) / 3;
        int maxWidth = (2 * actualWidth) / 3;

        float maxRatio = maxWidth / maxHeight;
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxHeight / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }
        options.inSampleSize = ImageUtils.calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;

        options.inTempStorage = new byte[16 * 1024];

        try {
            bitmap = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }

        int streamLength = maxFileSize;
        int compressQuality = 100;// Maximum 20 loops to retry to maintain quality.
        ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
        while (streamLength >= maxFileSize && compressQuality > 50) {

            try {
                bmpStream.flush();
                bmpStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
            compressQuality -= 3;

        }

        FileOutputStream fo;
        try {
            fo = new FileOutputStream(newFileName);
            fo.write(bmpStream.toByteArray());
            fo.flush();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(newFileName);
    }

    /**
     * @param directory
     * @return
     */

    public static File getLastModifiedFile(String directory) {
        File dir = new File(directory);
        File[] allFiles = dir.listFiles();

        if (allFiles == null || allFiles.length == 0) {
            return null;
        }

        File lastModifiedFile = allFiles[0];

        for (int i = 1; i < allFiles.length; i++) {
            if (lastModifiedFile.lastModified() < allFiles[i].lastModified()) {
                lastModifiedFile = allFiles[i];
            }
        }
        return lastModifiedFile;
    }

    /**
     * @param uri
     * @param context
     * @return
     */
    public static String getFileName(Context context, Uri uri) {

        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) {
            return null;
        }
        File file = new File(uri.toString());
        return file.getName();
    }

    public static String getMimeTypeByContentUriOrOther(Context context, Uri uri) {
        if (context == null) {
            return null;
        }
        String mimeType = context.getContentResolver().getType(uri);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = getMimeType(context, uri);
        }
        return mimeType;
    }

    public static String getSize(Context context, Uri uri) {

        String sizeInMB = null;
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);

        if (returnCursor != null && returnCursor.moveToFirst()) {

            int columnIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            Long fileSize = returnCursor.getLong(columnIndex);
            if (fileSize < 1024) {
                sizeInMB = (int) (fileSize / (1024 * 1024)) + " B";

            } else if (fileSize < 1024 * 1024) {
                sizeInMB = (int) (fileSize / (1024)) + " KB";
            } else {
                sizeInMB = (int) (fileSize / (1024 * 1024)) + " MB";
            }
        }

        return sizeInMB;
    }

    public static String getName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        int pos = name.lastIndexOf(".");

        if (pos == -1) {
            return name;
        }
        return name.substring(0, pos);
    }

    public static boolean isMaxUploadSizeReached(Context context, Uri uri, long maxFileSize) {
        try {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null) {
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                Long fileSize = returnCursor.getLong(sizeIndex);
                returnCursor.close();
                return fileSize > maxFileSize;
            }

        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isContentScheme(Uri uri) {
        if (uri == null) {
            return false;
        }
        return "content".equalsIgnoreCase(uri.getScheme());
    }

    public static Uri compressImage(Uri uri, Context context, String fileName) {
        Bitmap originalBitmap = null;
        ByteArrayOutputStream outputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            // Load and compress the bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            originalBitmap = BitmapFactory.decodeStream(
                    context.getContentResolver().openInputStream(uri), null, options);

            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

            // Write the compressed bitmap to a cache file
            File cacheFile = new File(context.getCacheDir(), fileName);
            fileOutputStream = new FileOutputStream(cacheFile);
            fileOutputStream.write(outputStream.toByteArray());
            fileOutputStream.flush();

            // Return the URI
            return Uri.fromFile(cacheFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
           if (originalBitmap != null && !originalBitmap.isRecycled()) {
              originalBitmap.recycle();
           }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing output stream", e);
            }
        }
    }

    public static Uri compressVideo(Context context, Uri uri, File file) {
        String filePath = null;
        try {
            filePath = SiliCompressor.with(context).compressVideo(uri, file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.fromFile(new File(filePath));
    }

    public static boolean isCompressionNeeded(Context context, Uri uri, long fileSize, boolean isImageCompressionEnabled,
                                              int minimumCompressionThresholdForImagesInMB, boolean isVideoCompressionEnabled,
                                              int minimumCompressionThresholdForVideosInMB) {
        if (!isImageCompressionEnabled && !isVideoCompressionEnabled) {
            return false;
        }
        String mimeType = FileUtils.getMimeTypeByContentUriOrOther(context, uri);
        if (!TextUtils.isEmpty(mimeType) && mimeType.contains("image/") && isImageCompressionEnabled) {
            long limitForCompression = (long) minimumCompressionThresholdForImagesInMB * 1024 * 1024;
            return fileSize >= limitForCompression;
        } else if (!TextUtils.isEmpty(mimeType) && mimeType.contains("video/") && isVideoCompressionEnabled) {
            long limitForCompression = (long) minimumCompressionThresholdForVideosInMB * 1024 * 1024;
            return fileSize >= limitForCompression;
        }
        return false;
    }
}
