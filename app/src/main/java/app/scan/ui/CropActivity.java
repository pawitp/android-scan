package app.scan.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImageView;

import app.scan.R;

public class CropActivity extends AppCompatActivity {

    // Static Bitmap to avoid passing large data through activity Parcel
    private static Bitmap sBitmap;

    private CropImageView mCropImageView;

    public static void setBitmap(Bitmap bitmap) {
        sBitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_crop);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCropImageView = findViewById(R.id.cropImageView);
        mCropImageView.setImageBitmap(sBitmap);
        mCropImageView.setCropRect(new Rect(0, 0, sBitmap.getWidth(), sBitmap.getHeight()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            Intent result = new Intent();
            result.putExtra("rotation", mCropImageView.getRotatedDegrees());
            result.putExtra("crop_rect", mCropImageView.getCropRect());
            setResult(RESULT_OK, result);
            finish();
            return true;
        } else if (id == R.id.action_rotate) {
            mCropImageView.rotateImage(-90);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Are you sure you want to discard your scanned image?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.cancel();
        });
        builder.show();
    }

}