package com.android.siddhartha.localist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void signupBtnClicked(View view) {

        EditText name = findViewById(R.id.signNameText);
        String nameString = name.getText().toString();
        EditText email = findViewById(R.id.signEmailText);
        String emailString = email.getText().toString();
        EditText password = findViewById(R.id.signPasswordText);
        String passwordString = password.getText().toString();
        EditText confirmPassword = findViewById(R.id.signConfirmPasswordText);
        String confirmPasswordString = confirmPassword.getText().toString();

        CheckBox terms = findViewById(R.id.agreeToTerms);

        Boolean agreed = false;
        if(terms.isChecked()) {
            agreed = !agreed;
        }

        if(nameString.matches("") || emailString.matches("") || passwordString.matches("") ||
                confirmPasswordString.matches("") || !agreed) {
            Toast.makeText(getApplicationContext(), "Complete the form!", Toast.LENGTH_SHORT).show();
        } else {

            String errorString = "";

            if(!isEmailValid(emailString)) {
                errorString += "Invalid email address!\r\n";
            }

            if(!passwordString.equals(confirmPasswordString)) {
                errorString += "Passwords do not match!\r\n";
            } else if(passwordString.length() < 8) {
                errorString += "Password cannot be less than 8 characters!";
            }

            if(errorString.length() == 0) {

                Toast.makeText(getApplicationContext(), "Signing you up...", Toast.LENGTH_SHORT).show();

                final String url = "http://localist-com.stackstaging.com/?signup=1&email=" +
                        emailString + "&password=" + passwordString + "&name=" + nameString;

                // Request a string response
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    String status = jsonObject.getString("status");

                                    if(status.matches("1")) {
                                        Toast.makeText(getApplicationContext(), "Signup successful!\r\nVerify Email address!", Toast.LENGTH_SHORT).show();
                                    } else if(status.matches("0")) {
                                        Toast.makeText(getApplicationContext(), "Oops, there was some error. Please come back later!", Toast.LENGTH_SHORT).show();
                                    } else if(status.matches("2")) {
                                        Toast.makeText(getApplicationContext(), "Account already exists!", Toast.LENGTH_SHORT).show();
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

            } else {
                Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
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

    public String encodeString(String str) {
        str = str.replace(" ", "%20");
        return str;
    }
}
