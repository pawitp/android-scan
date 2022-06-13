package app.scan.input;

import app.scan.process.ScannedImage;

public interface ScanCallback {
    void onComplete(ScannedImage scannedImage);
    void onError(Throwable t);
}
