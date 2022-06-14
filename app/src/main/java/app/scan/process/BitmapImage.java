package app.scan.process;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineByte;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;

// Raw Bitmap image with basic manipulation support
public class BitmapImage {

    private final byte[] mBytes;
    private final int mWidth;
    private final int mHeight;
    private final int mBitPerPixel;
    private final Converter mConverter;

    public BitmapImage(byte[] bytes, int width, int height) {
        mBytes = bytes;
        mWidth = width;
        mHeight = height;
        mBitPerPixel = bytes.length / (mWidth * mHeight);
        mConverter = mBitPerPixel == 1 ? new GrayscaleConverter() : new RgbConverter();
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Bitmap toBitmap() {
        return mConverter.toBitmap(mBytes, mWidth, mHeight);
    }

    public byte[] toPng() {
        return mConverter.toPng(mBytes, mWidth, mHeight);
    }

    // Return new cropped BitmapImage
    public BitmapImage crop(Rect rect) {
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        byte[] cropped = new byte[width * height * mBitPerPixel];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < mBitPerPixel; k++) {
                    int srcIdx = (rect.top + i) * mWidth * mBitPerPixel + (rect.left + j) * mBitPerPixel + k;
                    int dstIdx = i * width * mBitPerPixel + j * mBitPerPixel + k;
                    cropped[dstIdx] = mBytes[srcIdx];
                }
            }
        }

        return new BitmapImage(cropped, width, height);
    }

    // Return new rotated BitmapImage
    public BitmapImage rotate90() {
        int width = mHeight;
        int height = mWidth;

        byte[] rotated = new byte[width * height * mBitPerPixel];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < mBitPerPixel; k++) {
                    int srcIdx = (mHeight - j - 1) * mWidth * mBitPerPixel + i * mBitPerPixel + k;
                    int dstIdx = i * width * mBitPerPixel + j * mBitPerPixel + k;
                    rotated[dstIdx] = mBytes[srcIdx];
                }
            }
        }

        return new BitmapImage(rotated, width, height);
    }

    private interface Converter {
        // Convert to Android Bitmap for crop/rotate
        Bitmap toBitmap(byte[] bytes, int width, int height);

        // Convert to PNG file
        byte[] toPng(byte[] bytes, int width, int height);
    }

    private static class RgbConverter implements Converter {
        @Override
        public Bitmap toBitmap(byte[] bytes, int width, int height) {
            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] argb = new int[width * height];
            for (int i = 0; i < argb.length; i++) {
                int j = i * 3;
                argb[i] = (0xff) << 24 |              // A
                        (bytes[j + 2] & 0xff) << 16 | // B
                        (bytes[j + 1] & 0xff) << 8 |  // G
                        (bytes[j] & 0xff);            // R
            }
            bm.copyPixelsFromBuffer(IntBuffer.wrap(argb));
            return bm;
        }

        @Override
        public byte[] toPng(byte[] bytes, int width, int height) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PngWriter pngWriter = new PngWriter(baos, new ImageInfo(width, height, 8, false, false, false));
            ImageLineInt line = new ImageLineInt(pngWriter.imgInfo);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int srcIdx = (i * width + j) * 3;
                    ImageLineHelper.setPixelRGB8(line, j, bytes[srcIdx], bytes[srcIdx + 1], bytes[srcIdx + 2]);
                }
                pngWriter.writeRow(line);
            }
            pngWriter.end();

            return baos.toByteArray();
        }
    }

    private static class GrayscaleConverter implements Converter {
        @Override
        public Bitmap toBitmap(byte[] bytes, int width, int height) {
            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] argb = new int[width * height];
            for (int i = 0; i < argb.length; i++) {
                argb[i] = (0xff) << 24 |          // A
                        (bytes[i] & 0xff) << 16 | // B
                        (bytes[i] & 0xff) << 8 |  // G
                        (bytes[i] & 0xff);        // R
            }
            bm.copyPixelsFromBuffer(IntBuffer.wrap(argb));
            return bm;
        }

        @Override
        public byte[] toPng(byte[] bytes, int width, int height) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PngWriter pngWriter = new PngWriter(baos, new ImageInfo(width, height, 8, false, true, false));
            ImageLineByte line = new ImageLineByte(pngWriter.imgInfo);

            for (int i = 0; i < height; i++) {
                byte[] scanline = line.getScanline();
                System.arraycopy(bytes, i * width, scanline, 0, width);
                pngWriter.writeRow(line);
            }
            pngWriter.end();

            return baos.toByteArray();
        }
    }
}
