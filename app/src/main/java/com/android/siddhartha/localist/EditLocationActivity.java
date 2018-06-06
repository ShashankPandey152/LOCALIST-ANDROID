package com.android.siddhartha.localist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class EditLocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        Toast.makeText(getApplicationContext(), getIntent().getStringExtra("latitude"), Toast.LENGTH_SHORT).show();

        EditText nameEditText = findViewById(R.id.nameEditText);
        nameEditText.setText(getIntent().getStringExtra("name"));

        TextView latitudeText = findViewById(R.id.latitudeText);
        latitudeText.setText(getIntent().getStringExtra("latitude"));

        TextView longitudeText = findViewById(R.id.longitudeText);
        longitudeText.setText(getIntent().getStringExtra("longitude"));

    }

    public void editDeleteBtnClicked(View view) {

        String id = getIntent().getStringExtra("id");

        Toast.makeText(getApplicationContext(), "Deleting location...", Toast.LENGTH_SHORT).show();

        final String url = "http://localist-com.stackstaging.com/?edit=2&id=" + id;

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

    public void editSaveBtnClicked(View view) {

        String id = getIntent().getStringExtra("id");

        EditText nameEditText = findViewById(R.id.nameEditText);
        String name = nameEditText.getText().toString();

        if(name.matches(getIntent().getStringExtra("name"))) {
            Toast.makeText(getApplicationContext(), "No changes to make!", Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(getApplicationContext(), "Saving changes...", Toast.LENGTH_SHORT).show();

            final String url = "http://localist-com.stackstaging.com/?edit=1&id=" + id + "&name=" + devoidOfSpace(name);

            // Request a string response
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                String status = jsonObject.getString("status");

                                if(status.matches("1")) {
                                    Toast.makeText(getApplicationContext(), "Saved changes successfully!", Toast.LENGTH_SHORT).show();
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

    private String devoidOfSpace(String str) {
        return str.replace(" ", "%20");
    }

}
