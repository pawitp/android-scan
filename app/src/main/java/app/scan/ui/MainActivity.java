package app.scan.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import app.scan.R;
import app.scan.input.ScanCallback;
import app.scan.input.ScanDriver;
import app.scan.input.ScanRequest;
import app.scan.output.MediaStoreOutput;
import app.scan.process.CroppedImage;
import app.scan.process.ScannedImage;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CROP = 1;

    private Preferences mPref;
    private MediaStoreOutput mOutput;
    private Spinner mSizeSpinner;
    private Spinner mQualitySpinner;
    private Spinner mColorSpinner;

    // Need to store image data because we need access to it
    // after the crop activity returns
    private ScannedImage mScannedImage;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                int rotation = data.getIntExtra("rotation", 0);
                Rect cropRect = data.getParcelableExtra("crop_rect");
                saveImage(rotation, cropRect);
            }
        }
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
            public void onComplete(ScannedImage image) {
                // Do bitmap decoding in background thread
                Bitmap bitmap = image.getBitmap();

                MainActivity.this.runOnUiThread(() -> {
                    progressDialog.cancel();
                    mScannedImage = image;
                    startCrop(bitmap);
                });
            }

            @Override
            public void onError(Throwable t) {
                MainActivity.this.runOnUiThread(() -> {
                    progressDialog.cancel();
                    ErrorDialog.show(MainActivity.this, t);
                });
            }
        });
    }

    private void startCrop(Bitmap bitmap) {
        CropActivity.setBitmap(bitmap);
        Intent intent = new Intent(this, CropActivity.class);
        startActivityForResult(intent, REQUEST_CROP);
    }

    private void saveImage(int rotation, Rect cropRect) {
        try {
            Log.v(TAG, "Rotate " + rotation + " Crop " + cropRect);
            CroppedImage croppedImage = mScannedImage.getCroppedImage(cropRect, rotation);

            // Save to external storage and show in default image viewer
            Uri contentUri = mOutput.saveToStorage(croppedImage);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, "image/*");
            startActivity(intent);
        } catch (Exception e) {
            ErrorDialog.show(this, e);
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

}