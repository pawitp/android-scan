package app.scan.process;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class BitmapScannedImage implements ScannedImage {

    private final BitmapImage mBitmapImage;

    public BitmapScannedImage(byte[] binary, int width, int height) {
        mBitmapImage = new BitmapImage(binary, width, height);
    }

    @Override
    public Bitmap getBitmap() {
        return mBitmapImage.toBitmap();
    }

    @Override
    public CroppedImage getCroppedImage(Rect cropRect, int rotationDegrees) {
        BitmapImage processedImage = mBitmapImage.crop(cropRect);
        if (rotationDegrees == 90) {
            processedImage = processedImage.rotate90();
        } else if (rotationDegrees == 180) {
            processedImage = processedImage.rotate90().rotate90();
        } else if (rotationDegrees == 270) {
            processedImage = processedImage.rotate90().rotate90().rotate90();
        }

        return new CroppedImage(processedImage.toPng(), "image/png", "png");
    }
}
