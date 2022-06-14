package app.scan.ui;

import android.app.Activity;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

public class ErrorDialog {

    private static final String TAG = "ErrorDialog";

    public static void show(Activity activity, Throwable t) {
        Log.e(TAG, "Exception thrown", t);
        new AlertDialog.Builder(activity)
                .setTitle(t.getClass().getSimpleName())
                .setMessage(t.getMessage())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
