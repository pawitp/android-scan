package app.scan.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.apache.commons.io.IOUtils;

import java.io.OutputStream;

import app.scan.R;
import app.scan.input.ScanCallback;
import app.scan.input.ScanDriver;
import app.scan.input.ScanRequest;
import app.scan.output.MediaStoreOutput;

public class MainActivity extends AppCompatActivity {

    private Preferences mPref;
    private MediaStoreOutput mOutput;
    private Spinner mSizeSpinner;
    private Spinner mQualitySpinner;
    private Spinner mColorSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPref = new Preferences(getApplicationContext());
        mOutput = new MediaStoreOutput(getApplicationContext());

        mSizeSpinner = findViewById(R.id.size_spinner);
        setSpinnerContent(mSizeSpinner, R.array.size_array);

        mQualitySpinner = findViewById(R.id.quality_spinner);
        setSpinnerContent(mQualitySpinner, R.array.quality_array);

        mColorSpinner = findViewById(R.id.color_spinner);
        setSpinnerContent(mColorSpinner, R.array.color_array);

        // Ask for server address on initial start
        if (mPref.getAddress() == null) {
            configureScannerAddress();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            configureScannerAddress();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startScan(View view) {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Scanning", "Scanning...");
        ScanDriver driver = new ScanDriver(mPref.getAddress());
        ScanRequest request = new ScanRequest(
                mSizeSpinner.getSelectedItemPosition(),
                mQualitySpinner.getSelectedItemPosition(),
                mColorSpinner.getSelectedItemPosition());
        driver.startScan(request, new ScanCallback() {
            @Override
            public void onComplete(byte[] binary) {
                MainActivity.this.runOnUiThread(() -> {
                    progressDialog.cancel();
                    saveToStorage(binary);
                });
            }

            @Override
            public void onError(Throwable t) {
                MainActivity.this.runOnUiThread(() -> {
                    progressDialog.cancel();
                    showErrorDialog(t);
                });
            }
        });
    }

    // Save image to storage with scoped storage support
    private void saveToStorage(byte[] binary) {
        try {
            Uri contentUri = mOutput.saveToStorage(binary);

            // Open in default viewer
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "image/*");
            startActivity(intent);
        } catch (Exception e) {
            showErrorDialog(e);
        }
    }

    // Show dialog to configure scanner address
    private void configureScannerAddress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanner Address");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(mPref.getAddress());
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> mPref.setAddress(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Initialize spinner content
    private void setSpinnerContent(Spinner spinner, int resource) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                resource, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showErrorDialog(Throwable t) {
        new AlertDialog.Builder(this)
                .setTitle(t.getClass().getSimpleName())
                .setMessage(t.getMessage())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}