package app.scan.process;

import android.graphics.Bitmap;
import android.graphics.Rect;

public interface ScannedImage {

    // Get Bitmap for preview or cropping
    Bitmap getBitmap();

    // Get cropped and rotated image
    CroppedImage getCroppedImage(Rect cropRect, int rotationDegrees);

    static ScannedImage fromJpeg(byte[] binary) {
        return new JpegScannedImage(binary);
    }

    static ScannedImage fromRaw(byte[] binary, int width, int height) {
        return new BitmapScannedImage(binary, width, height);
    }

}