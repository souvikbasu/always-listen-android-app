package in.squats.safeindiainitiative;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class HelperActivity extends Activity {
    private static final String TAG = HelperActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        Intent intent = getIntent();
        String lat = Double.toString(intent.getDoubleExtra("lat",0.0));
        String lng = Double.toString(intent.getDoubleExtra("lng",0.0));
        String helpSeekerFcm = intent.getStringExtra("helpSeekerFcm");

        Log.v(TAG, "starting Helper service");
        if(intent.getAction() == "in.squats.safeindiainitiative.actions.RECORD_HELPER") {
            HelperService.startActionWillHelp(getApplicationContext(), lat, lng, helpSeekerFcm);
        } else if(intent.getAction() == "in.squats.safeindiainitiative.actions.NO_HELP") {
            HelperService.startActionNoHelp(getApplicationContext(), helpSeekerFcm);
        }
    }
}
