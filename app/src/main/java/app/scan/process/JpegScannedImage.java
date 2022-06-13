package app.scan.process;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import jpegkit.Jpeg;

public class JpegScannedImage implements ScannedImage {

    // Original JPEG binary
    private final byte[] mBinary;

    public JpegScannedImage(byte[] binary) {
        mBinary = binary;
    }

    @Override
    public Bitmap getBitmap() {
        return BitmapFactory.decodeByteArray(mBinary, 0, mBinary.length);
    }

    @Override
    public CroppedImage getCroppedImage(Rect cropRect, int rotationDegrees) {
        // Lossless rotation and cropping with Jpegkit
        Jpeg jpeg = new Jpeg(mBinary);
        jpeg.crop(cropRect);
        jpeg.rotate(rotationDegrees);
        return new CroppedImage(jpeg.getJpegBytes(), "image/jpeg", "jpg");
    }

}
