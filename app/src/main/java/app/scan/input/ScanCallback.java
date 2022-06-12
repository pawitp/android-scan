package app.scan.input;

public interface ScanCallback {
    void onComplete(byte[] binary);
    void onError(Throwable t);
}
