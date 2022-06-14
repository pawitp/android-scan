package app.scan.input;

// Build a scan request to send to the scanner
public class ScanRequest {

    private static int[][] SIZES = new int[][]{
            new int[]{2480, 3508}, // A4
            new int[]{2550, 3508}, // Full
    };

    private static int[] COMPRESSION_FACTORS = new int[]{
            35, // "Normal" quality (JPEG 65)
            15, // "High" quality (JPEG 85)
            5,  // "Very High" quality (JPEG 95)
            0,  // Placeholder for lossless (see mFormat)
    };

    private static String[] COLOR_SPACES = new String[]{
            "Color",
            "Gray"
    };

    private final int mWidth;
    private final int mHeight;
    private final int mCompressionFactor;
    private final String mColorSpace;
    private final String mFormat;

    public ScanRequest(int sizeId, int qualityId, int colorId) {
        int[] size = SIZES[sizeId];
        mWidth = size[0];
        mHeight = size[1];
        mCompressionFactor = COMPRESSION_FACTORS[qualityId];
        mColorSpace = COLOR_SPACES[colorId];
        mFormat = mCompressionFactor == 0 ? "Raw" : "Jpeg";
    }

    public String toRequestXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ScanSettings xmlns=\"http://www.hp.com/schemas/imaging/con/cnx/scan/2008/08/19\">\n" +
                "   <XResolution>300</XResolution>\n" +
                "   <YResolution>300</YResolution>\n" +
                "   <XStart>0</XStart>\n" +
                "   <Width>" + mWidth + "</Width>\n" +
                "   <YStart>0</YStart>\n" +
                "   <Height>" + mHeight + "</Height>\n" +
                "   <Format>" + mFormat + "</Format>\n" +
                "   <CompressionQFactor>" + mCompressionFactor + "</CompressionQFactor>\n" +
                "   <ColorSpace>" + mColorSpace + "</ColorSpace>\n" +
                "   <BitDepth>8</BitDepth>\n" +
                "   <InputSource>Platen</InputSource>\n" +
                "   <InputSourceType>Platen</InputSourceType>\n" +
                "   <GrayRendering>NTSC</GrayRendering>\n" +
                "   <ToneMap>\n" +
                "      <Gamma>0</Gamma>\n" +
                "      <Brightness>1000</Brightness>\n" +
                "      <Contrast>1000</Contrast>\n" +
                "      <Highlite>179</Highlite>\n" +
                "      <Shadow>25</Shadow>\n" +
                "   </ToneMap>\n" +
                "   <ContentType>Photo</ContentType>\n" +
                "</ScanSettings>";
    }

}
