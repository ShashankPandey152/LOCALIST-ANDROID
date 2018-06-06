package com.android.siddhartha.localist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.siddhartha.localist.Adapters.AllLocationAdapter;
import com.android.siddhartha.localist.DataStructures.AllLocation;
import com.android.siddhartha.localist.Interfaces.CustomItemClickListener;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AllLocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_location);

        parseJSONLocations();

    }

    private void parseJSONLocations() {

        String emailString = "sdharchou@gmail.com";

        Toast.makeText(getApplicationContext(), "Fetching locations...", Toast.LENGTH_SHORT).show();

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
                                    Toast.makeText(getApplicationContext(), listLocation.get(position).getLocationId(),
                                            Toast.LENGTH_SHORT).show();
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
