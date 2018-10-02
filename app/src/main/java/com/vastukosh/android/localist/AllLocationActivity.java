package com.vastukosh.android.localist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.vastukosh.android.localist.Adapters.AllLocationAdapter;
import com.vastukosh.android.localist.DataStructures.AllLocation;
import com.vastukosh.android.localist.Interfaces.CustomItemClickListener;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllLocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_location);

        parseJSONLocations();

    }

    private void parseJSONLocations() {

        final String emailString = getIntent().getStringExtra("email");

        Toast.makeText(getApplicationContext(), "Fetching locations...", Toast.LENGTH_SHORT).show();

        final String url = "http://localist.000webhostapp.com/?location=2&email=" + emailString;

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
                            JSONArray latitude = jsonObject.getJSONArray("latitude");
                            JSONArray longitude = jsonObject.getJSONArray("longitude");

                            int i = 0;

                            while(i < id.length()) {
                                listLocation.add(new AllLocation(name.get(i).toString(), latitude.get(i).toString(),
                                        longitude.get(i).toString(), id.get(i).toString()));
                                i++;
                            }

                            if(name.length() == 0) {
                                Toast.makeText(getApplicationContext(), "No locations to show!", Toast.LENGTH_SHORT).show();
                                Intent gotoHome = new Intent(getApplication(), HomeActivity.class);
                                startActivity(gotoHome);
                            }


                            adapter = new AllLocationAdapter(getApplicationContext(), listLocation, new CustomItemClickListener() {
                                @Override
                                public void onItemClick(View v, int position) {
                                    Intent gotoEditLocation = new Intent(AllLocationActivity.this,
                                            EditLocationActivity.class);
                                    gotoEditLocation.putExtra("name", listLocation.get(position).getLocationName());
                                    gotoEditLocation.putExtra("latitude", listLocation.get(position).getLatitude());
                                    gotoEditLocation.putExtra("longitude", listLocation.get(position).getLongitude());
                                    gotoEditLocation.putExtra("id", listLocation.get(position).getLocationId());
                                    startActivity(gotoEditLocation);
                                }
                            });

                            RecyclerView allLocationRecyclerView = findViewById(R.id.allLocationRecylcerView);
                            allLocationRecyclerView.setAdapter(adapter);

                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            allLocationRecyclerView.setLayoutManager(layoutManager);
                            allLocationRecyclerView.setHasFixedSize(true);


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
