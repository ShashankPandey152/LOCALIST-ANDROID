package com.vastukosh.android.localist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.vastukosh.android.localist.Adapters.AllLocationAdapter;
import com.vastukosh.android.localist.DataStructures.AllLocation;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        parseJSONLocations();
    }

    public void addToChecklist(View view) {

        EditText itemNameEditText = findViewById(R.id.itemNameEditText);
        String itemName = itemNameEditText.getText().toString();

        Spinner locationsSpinner = findViewById(R.id.locationsSpinner);
        String location = locationsSpinner.getSelectedItem().toString();
        location = location.split(":")[0];

        String emailString = getIntent().getStringExtra("email");

        if(itemName.matches("") || location.matches("--Select a location--")) {
            Toast.makeText(getApplicationContext(), "Complete the form!", Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(getApplicationContext(), "Adding item...", Toast.LENGTH_SHORT).show();

            final String url = "http://localist-com.stackstaging.com/?addItem=1&email=" + emailString +
                    "&store=" + devoidOfSpace(location) + "&item=" + devoidOfSpace(itemName);

            // Request a string response
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                String status = jsonObject.getString("status");

                                if(status.matches("1")) {
                                    Toast.makeText(getApplicationContext(), "Item added successfully!", Toast.LENGTH_SHORT).show();
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

    }

    private void parseJSONLocations() {

        String emailString = getIntent().getStringExtra("email");
        final ArrayList<String> locations = new ArrayList<>();
        locations.add("--Select a location--");

        final String url = "http://localist-com.stackstaging.com/?location=2&email=" + emailString;

        // Request a string response
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            final ArrayList<AllLocation> listLocation = new ArrayList<AllLocation>();
                            AllLocationAdapter adapter;

                            JSONArray id = jsonObject.getJSONArray("id");
                            JSONArray name = jsonObject.getJSONArray("name");

                            if(name.length() == 0) {
                                Toast.makeText(getApplicationContext(), "No locations to show!", Toast.LENGTH_SHORT).show();
                                Intent gotoHome = new Intent(getApplication(), HomeActivity.class);
                                startActivity(gotoHome);
                            }

                            int i = 0;
                            while(i < name.length()) {
                                locations.add(id.get(i).toString() + " : " + name.get(i).toString());
                                i++;
                            }

                            Spinner locationSpinner = findViewById(R.id.locationsSpinner);
                            ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), R.layout.my_spinner, locations);
                            locationSpinner.setAdapter(arrayAdapter);

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

    private String devoidOfSpace(String str) {
        return str.replace(" ", "%20");
    }

}
