package app.scan.process;

public class CroppedImage {

    public final byte[] binary;
    public final String mimeType;
    public final String extension;

    public CroppedImage(byte[] binary, String mimeType, String extension) {
        this.binary = binary;
        this.mimeType = mimeType;
        this.extension = extension;
    }

}
