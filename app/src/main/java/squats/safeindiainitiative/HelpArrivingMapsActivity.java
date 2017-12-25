package squats.safeindiainitiative;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HelpArrivingMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView help_arriving_message;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_arriving_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        help_arriving_message = (TextView) this.findViewById(R.id.help_arriving_message);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat", 0);
        double lng = intent.getDoubleExtra("long", 0);

        // Add a marker in Sydney and move the camera
        LatLng userLocation = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Help seeker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

        help_arriving_message.setText("Help reaching soon...");

    }


    protected void onStart() {
        super.onStart();

        final Handler handler = new Handler();
        final int delay = 10000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                String fcm = FirebaseInstanceId.getInstance().getToken();
                String helperUrl = "https://safe-india-initiative-api.herokuapp.com/api/helpers?helpSeekerFcm=" + fcm;

                new SendGetRequest(mMap).execute(helperUrl);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }


    class SendGetRequest extends AsyncTask<String, Void, String> {

        private GoogleMap mMap;
        public SendGetRequest(GoogleMap map) {
            mMap = map;
        }
        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL(arg0[0]);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("API call failed. URL: " + arg0[0] + " Response code: " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();

            JSONArray helpers = null;
            try {
                helpers = new JSONArray(result);

                for(int i = 0; i < helpers.length(); i++) {
                    JSONObject helper = (JSONObject) helpers.get(i);
                    Double lat = (Double) helper.get("lat");
                    Double lng = (Double) helper.get("long");
                    LatLng userLocation = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Helper"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
