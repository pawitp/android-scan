package app.scan.output;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.scan.process.CroppedImage;

public class MediaStoreOutput {

    private final ContentResolver mContentResolver;

    public MediaStoreOutput(Context context) {
        mContentResolver = context.getContentResolver();
    }

    public Uri saveToStorage(CroppedImage image) throws IOException {
        ContentValues contentValues = new ContentValues();
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, date + "." + image.extension);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, image.mimeType);
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Scans");
        Uri contentUri = mContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        try (OutputStream os = mContentResolver.openOutputStream(contentUri)) {
            IOUtils.write(image.binary, os);
        }

        return contentUri;
    }

}
