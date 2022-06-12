package app.scan.input;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import app.scan.util.SslUtil;

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
                String binaryPath = queryBinaryPath(jobPath);
                Log.v(TAG, "Binary Path: " + jobPath);
                byte[] binary = downloadBinary(binaryPath);
                callback.onComplete(binary);
            } catch (Exception e) {
                Log.e(TAG, "Error scanning", e);
                callback.onError(e);
            }
        }).start();
    }

    // Make scan request, return URL path of job
    private String initiateScanRequest(String requestXml) throws Exception {
        URL url = new URL("https://" + mAddress + "/Scan/Jobs");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            SslUtil.setInsecureHttpsConnection(urlConnection);
            urlConnection.setConnectTimeout(5000);
            byte[] requestBytes = requestXml.getBytes(StandardCharsets.UTF_8);
            urlConnection.setDoOutput(true);
            urlConnection.setFixedLengthStreamingMode(requestBytes.length);
            urlConnection.setRequestProperty("Content-Type", "application/xml");
            urlConnection.getOutputStream().write(requestBytes);

            if (urlConnection.getResponseCode() == 201) {
                // Ignore hostname as the HTTP URL will be returned even if HTTPS is used
                URL responseUrl = new URL(urlConnection.getHeaderField("Location"));
                return responseUrl.getPath();
            } else {
                throw new IOException(urlConnection.getResponseMessage());
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private String queryBinaryPath(String jobPath) throws Exception {
        URL url = new URL("https://" + mAddress + jobPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            SslUtil.setInsecureHttpsConnection(urlConnection);
            urlConnection.setConnectTimeout(5000);
            String result = IOUtils.toString(urlConnection.getInputStream(), StandardCharsets.UTF_8);

            // Parse BinaryURL from XML
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            NodeList urls = (NodeList) xPath.evaluate("//*[\"BinaryURL\"=local-name()]",
                    new InputSource(new StringReader(result)), XPathConstants.NODESET);
            return urls.item(0).getTextContent();
        } finally {
            urlConnection.disconnect();
        }
    }

    private byte[] downloadBinary(String binaryPath) throws Exception {
        URL url = new URL("https://" + mAddress + binaryPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            SslUtil.setInsecureHttpsConnection(urlConnection);
            urlConnection.setConnectTimeout(5000);
            return IOUtils.toByteArray(urlConnection.getInputStream());
        } finally {
            urlConnection.disconnect();
        }
    }

}
