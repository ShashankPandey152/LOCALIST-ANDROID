package com.vastukosh.app.localist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class EmailVerifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verify);

        Button logoutBtn = findViewById(R.id.logoutBtn);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(gotoLogin);
            }
        });

        Button resendBtn = findViewById(R.id.resendBtn);

        resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = getIntent().getStringExtra("email");

                Toast.makeText(getApplicationContext(), "Sending mail...", Toast.LENGTH_SHORT).show();

                final String url = "http://localist.000webhostapp.com/?resend=1&email=" + emailString;

                // Request a string response
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    String status = jsonObject.getString("status");

                                    if(status.matches("1")) {
                                        Toast.makeText(getApplicationContext(), "Mail sent successfully!", Toast.LENGTH_SHORT).show();
                                        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(loginIntent);

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
                Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
            }
        });
    }

}
