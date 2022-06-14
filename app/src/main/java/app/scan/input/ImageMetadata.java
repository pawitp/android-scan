package app.scan.input;

public class ImageMetadata {

    public final String path;
    public final int width;
    public final int height;
    public final boolean isRaw;

    public ImageMetadata(String path, int width, int height, boolean isRaw) {
        this.path = path;
        this.width = width;
        this.height = height;
        this.isRaw = isRaw;
    }

}
