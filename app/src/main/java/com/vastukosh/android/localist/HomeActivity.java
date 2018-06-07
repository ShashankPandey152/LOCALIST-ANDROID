package com.vastukosh.android.localist;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vastukosh.android.localist.Adapters.AllItemsAdapter;
import com.vastukosh.android.localist.DataStructures.AllItems;
import com.vastukosh.android.localist.Interfaces.CustomItemClickListener;
import com.vastukosh.android.localist.services.LocationMonitoringService;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static boolean firstRun = true;

    private static final String TAG = HomeActivity.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private boolean mAlreadyStartedService = false;

    SharedPreferences sp, sp1;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sp = getSharedPreferences("checklist", Context.MODE_PRIVATE);
        sp1 = getSharedPreferences("login", Context.MODE_PRIVATE);

        tts = new TextToSpeech(this, this);

        final HashMap<String, Double> latitudes = new HashMap<>();
        final HashMap<String, Double> longitudes = new HashMap<>();

        final  HashMap<String, Integer> countItem = new HashMap<>();

        final HashMap<String, Integer> completed = new HashMap<>();

        final int[] emptyList = {0};

        final String emailString = sp1.getString("email", "");

        final String url = "http://localist-com.stackstaging.com/?getChecklist=1&email=" + emailString;

        // Request a string response
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            JSONObject storeAndLocations = jsonObject.getJSONObject("locations");

                            Iterator iterator = storeAndLocations.keys();


                            while(iterator.hasNext()) {
                                String key = iterator.next().toString();
                                JSONArray location = (JSONArray) storeAndLocations.get(key);
                                for (int i = 0; i < location.length(); i++) {
                                    latitudes.put(key, Double.parseDouble(location.get(0).toString()));
                                    longitudes.put(key, Double.parseDouble(location.get(1).toString()));
                                    completed.put(key, 0);
                                    countItem.put(key, 0);
                                }
                            }

                            final ArrayList<AllItems> listItems = new ArrayList<>();
                            AllItemsAdapter adapter;

                            JSONObject storesAndItems = jsonObject.getJSONObject("items");

                            Iterator iterator1 = storesAndItems.keys();
                            while (iterator1.hasNext()) {
                                String key = iterator1.next().toString();
                                if(key.equals("trash")) {
                                    emptyList[0] = 1;
                                }
                            }

                            if(emptyList[0] == 1) {
                                Toast.makeText(getApplicationContext(), "Empty list!", Toast.LENGTH_SHORT).show();
                            } else {
                                final HashMap<String, Boolean> selectedItem = new HashMap<>();

                                iterator = storesAndItems.keys();
                                while(iterator.hasNext()) {
                                    String key = iterator.next().toString();
                                    JSONArray items = (JSONArray)storesAndItems.get(key);
                                    countItem.put(key, items.length());
                                    for(int i=0;i<items.length();i++) {
                                        listItems.add(new AllItems((String) items.get(i), key));
                                        selectedItem.put(items.get(i).toString(), false);
                                        sp.edit().putInt("checked", 0).apply();
                                    }
                                }

                                adapter = new AllItemsAdapter(getApplicationContext(), listItems, new CustomItemClickListener() {
                                    @Override
                                    public void onItemClick(View v, int position) {

                                        String currentItem = listItems.get(position).getItemName();
                                        if(!selectedItem.get(currentItem)) {
                                            selectedItem.put(currentItem, true);
                                            int currentChecked = sp.getInt("checked", -1);
                                            sp.edit().putInt("checked", currentChecked + 1).apply();
                                            v.setBackgroundColor(Color.parseColor("#408c40"));
                                            if(sp.getInt("checked", -1) == listItems.size()) {
                                                Toast.makeText(getApplicationContext(), "All complete!", Toast.LENGTH_SHORT).show();
                                                deleteList(emailString);
                                            }
                                        } else {
                                            selectedItem.put(currentItem, false);
                                            int currentChecked = sp.getInt("checked", -1);
                                            sp.edit().putInt("checked", currentChecked - 1).apply();
                                            v.setBackgroundColor(Color.parseColor("#ffffff"));
                                        }
                                    }
                                });

                                RecyclerView itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
                                itemsRecyclerView.setAdapter(adapter);

                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                                itemsRecyclerView.setLayoutManager(layoutManager);
                                itemsRecyclerView.setHasFixedSize(true);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                System.out.println("Something went wrong!");
                error.printStackTrace();

            }
        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String latitude = intent.getStringExtra(LocationMonitoringService.EXTRA_LATITUDE);
                        String longitude = intent.getStringExtra(LocationMonitoringService.EXTRA_LONGITUDE);

                        if(emptyList[0] == 0) {
                            double distance;
                            Iterator iterator = latitudes.entrySet().iterator();
                            Iterator iterator1 = longitudes.entrySet().iterator();

                            while(iterator.hasNext()) {
                                Map.Entry pair = (Map.Entry)iterator.next();
                                Map.Entry pair1 = (Map.Entry)iterator1.next();
                                distance = countDistance(Double.parseDouble(latitude), Double.parseDouble(longitude),
                                        Double.valueOf(pair.getValue().toString()), Double.valueOf(pair1.getValue().toString()));

                                String key = pair.getKey().toString();

                                if(distance < 1.2 && completed.get(key) == 0 && countItem.get(key) > 0) {
                                    String item = "item";
                                    if(countItem.get(key) > 1) {
                                        item = "items";
                                    }
                                    speakOut("You have to buy!" + countItem.get(key) + item + " from " + key);
                                    completed.put(pair.getKey().toString(), 1);
                                }

                            }
                        }

                    }
                }, new IntentFilter(LocationMonitoringService.ACTION_LOCATION_BROADCAST)
        );

        if(firstRun) {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);

            showAlertSound();

            firstRun = !firstRun;
        }

        com.github.clans.fab.FloatingActionButton addLocationFabBtn = findViewById(R.id.addLocationFabBtn);

        addLocationFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddLocation = new Intent(getApplicationContext(), AddLocationActivity.class);
                gotoAddLocation.putExtra("email", emailString);
                startActivity(gotoAddLocation);
            }
        });

        com.github.clans.fab.FloatingActionButton allLocationFabBtn = findViewById(R.id.allLocationFabBtn);

        allLocationFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAllLocation = new Intent(getApplicationContext(), AllLocationActivity.class);
                gotoAllLocation.putExtra("email", emailString);
                startActivity(gotoAllLocation);
            }
        });

        com.github.clans.fab.FloatingActionButton addItemFabBtn = findViewById(R.id.addItemFabBtn);

        addItemFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddItem = new Intent(getApplicationContext(), AddItemActivity.class);
                gotoAddItem.putExtra("email", emailString);
                startActivity(gotoAddItem);
            }
        });

        Button logoutBtn = findViewById(R.id.logoutBtn);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoLogin = new Intent(getApplicationContext(), LoginActivity.class);
                sp1.edit().putBoolean("logged", false).apply();
                startActivity(gotoLogin);
            }
        });

    }

    private Double countDistance(double lat1, double lon1, double lat2, double lon2) {

        int R = 6371;

        double x = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2);
        double y = (lat2 - lat1);

        double distance = Math.sqrt(x * x + y * y) * R;

        return distance;

    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.UK);

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported!");
            } else {
                speakOut("");
            }

        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    private void speakOut(String msg) {
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deleteList(String emailString) {

        final String url = "http://localist-com.stackstaging.com/?deleteChecklist=1&email=" + emailString;

        // Request a string response
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String status = jsonObject.getString("status");

                            if(status.matches("1")) {
                                Toast.makeText(getApplicationContext(), "Deleted successfully!", Toast.LENGTH_SHORT).show();
                                Intent gotoHome = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(gotoHome);
                            } else if(status.matches("-1")) {
                                Toast.makeText(getApplicationContext(), "Oops, there was some error. Please come back later!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                System.out.println("Something went wrong!");
                error.printStackTrace();

            }
        });

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void showAlertSound() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Make sure your phone's sound is on!");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert11 = builder1.create();

        alert11.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alert11.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#1919ff"));
            }
        });
        alert11.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        startStep1();
    }

    /**
     * Step 1: Check Google Play services
     */
    private void startStep1() {

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {

            //Passing null to indicate that it is executing for the first time.
            startStep2(null);

        } else {
            Toast.makeText(getApplicationContext(), R.string.no_google_playservice_available, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Step 2: Check & Prompt Internet connection
     */
    private Boolean startStep2(DialogInterface dialog) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }


        if (dialog != null) {
            dialog.dismiss();
        }

        //Yes there is active internet connection. Next check Location is granted by user or not.

        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            startStep3();
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(R.string.title_alert_no_intenet);
        builder.setMessage(R.string.msg_alert_no_internet);

        String positiveText = getString(R.string.btn_label_refresh);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        //Block the Application Execution until user grants the permissions
                        if (startStep2(dialog)) {

                            //Now make sure about location permission.
                            if (checkPermissions()) {

                                //Step 2: Start the Location Monitor Service
                                //Everything is there to start the service.
                                startStep3();
                            } else if (!checkPermissions()) {
                                requestPermissions();
                            }

                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Step 3: Start the Location Monitor Service
     */
    private void startStep3() {

        //And it will be keep running until you close the entire application from task manager.
        //This method will executed only once.

        if (!mAlreadyStartedService) {

            //Start location sharing service to app server.........
            Intent intent = new Intent(this, LocationMonitoringService.class);
            startService(intent);

            mAlreadyStartedService = true;
            //Ends................................................
        }
    }

    /**
     * Return the availability of GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStep3();

            } else {
                // Permission denied.

                // Notify the img_user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the img_user for permission (device policy or "Never ask
                // again" prompts). Therefore, a img_user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }


    @Override
    public void onDestroy() {

        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }

        //Stop location sharing service to app server.........

        stopService(new Intent(this, LocationMonitoringService.class));
        mAlreadyStartedService = false;
        //Ends................................................


        super.onDestroy();
    }

}
