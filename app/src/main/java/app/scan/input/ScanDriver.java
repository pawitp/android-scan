package app.scan.input;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import app.scan.process.ScannedImage;
import app.scan.util.XmlUtil;

public class ScanDriver {

    private static final String TAG = "ScanDriver";

    private final String mAddress;

    public ScanDriver(String address) {
        this.mAddress = address;
    }

    // Asynchronously start a scan and make progress callbacks
    // Note: callbacks will be from a different thread
    public void startScan(final ScanRequest request,
                          final ScanCallback callback) {
        new Thread(() -> {
            try {
                String jobPath = initiateScanRequest(request.toRequestXml());
                Log.v(TAG, "Job Path: " + jobPath);
                ImageMetadata metadata = queryMetadata(jobPath);
                Log.v(TAG, "Binary Path: " + metadata.path);
                byte[] binary = downloadBinary(metadata.path);
                ScannedImage image = metadata.isRaw ? ScannedImage.fromRaw(binary, metadata.width, metadata.height)
                        : ScannedImage.fromJpeg(binary);
                callback.onComplete(image);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    // Make scan request, return URL path of job
    private String initiateScanRequest(String requestXml) throws Exception {
        URL url = new URL("http://" + mAddress + ":8080/Scan/Jobs");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setConnectTimeout(5000);
            byte[] requestBytes = requestXml.getBytes(StandardCharsets.UTF_8);
            urlConnection.setDoOutput(true);
            urlConnection.setFixedLengthStreamingMode(requestBytes.length);
            urlConnection.setRequestProperty("Content-Type", "application/xml");
            urlConnection.getOutputStream().write(requestBytes);

            if (urlConnection.getResponseCode() == 201) {
                // Ignore hostname as the returned hostname/protocol may not match the one we used
                URL responseUrl = new URL(urlConnection.getHeaderField("Location"));
                return responseUrl.getPath();
            } else {
                throw new IOException(urlConnection.getResponseMessage());
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private ImageMetadata queryMetadata(String jobPath) throws Exception {
        URL url = new URL("http://" + mAddress + ":8080" + jobPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setConnectTimeout(5000);
            String result = IOUtils.toString(urlConnection.getInputStream(), StandardCharsets.UTF_8);

            // Parse metadata from XML
            Document document = XmlUtil.parseString(result);
            String binaryPath = XmlUtil.evaluateXPath("//*[\"BinaryURL\"=local-name()]", document);
            int width = Integer.parseInt(XmlUtil.evaluateXPath("//*[\"ImageWidth\"=local-name()]", document));
            int height = Integer.parseInt(XmlUtil.evaluateXPath("//*[\"ImageHeight\"=local-name()]", document));
            String format = XmlUtil.evaluateXPath("//*[\"Format\"=local-name()]", document);

            return new ImageMetadata(binaryPath, width, height, "Raw".equals(format));
        } finally {
            urlConnection.disconnect();
        }
    }

    private byte[] downloadBinary(String binaryPath) throws Exception {
        URL url = new URL("http://" + mAddress + ":8080" + binaryPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setConnectTimeout(5000);
            return IOUtils.toByteArray(urlConnection.getInputStream());
        } finally {
            urlConnection.disconnect();
        }
    }

}
