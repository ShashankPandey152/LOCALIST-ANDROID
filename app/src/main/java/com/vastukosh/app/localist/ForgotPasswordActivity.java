package com.vastukosh.app.localist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    public void forgotBtnClicked(View view) {

        EditText email = findViewById(R.id.forgotEmailText);
        String emailString = email.getText().toString();

        if(emailString.matches("")) {
            Toast.makeText(getApplicationContext(), "Complete the form!", Toast.LENGTH_SHORT).show();
        } else {

            if(!isEmailValid(emailString)) {
                Toast.makeText(getApplicationContext(), "Invalid email address!", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(getApplicationContext(), "Sending mail...", Toast.LENGTH_SHORT).show();

                final String url = "http://<website-link>/?forgot=1&email=" + emailString;

                // Request a string response
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    String status = jsonObject.getString("status");

                                    if(status.matches("1")) {
                                        Toast.makeText(getApplicationContext(), "Email sent successfully!", Toast.LENGTH_SHORT).show();
                                    } else if(status.matches("0")) {
                                        Toast.makeText(getApplicationContext(), "Account does not exist!", Toast.LENGTH_SHORT).show();
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

    }

    public Boolean isEmailValid(String email) {
        return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches();
    }
}
