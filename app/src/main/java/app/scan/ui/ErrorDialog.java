package app.scan.ui;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

public class ErrorDialog {

    public static void show(Activity activity, Throwable t) {
        new AlertDialog.Builder(activity)
                .setTitle(t.getClass().getSimpleName())
                .setMessage(t.getMessage())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
