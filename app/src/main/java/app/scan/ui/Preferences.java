package app.scan.ui;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private final SharedPreferences mPref;

    public Preferences(Context context) {
        mPref = context.getSharedPreferences("app.scan.settings", Context.MODE_PRIVATE);
    }

    public String getAddress() {
        return mPref.getString("address", null);
    }

    public void setAddress(String address) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("address", address);
        editor.apply();
    }

}
