package com.android.siddhartha.localist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.siddhartha.localist.Adapters.AllItemsAdapter;
import com.android.siddhartha.localist.DataStructures.AllItems;
import com.android.siddhartha.localist.Interfaces.CustomItemClickListener;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static boolean firstRun = true;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sp = getSharedPreferences("checklist", Context.MODE_PRIVATE);

        if(firstRun) {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);

            showAlertSound();

            firstRun = !firstRun;
        }

        parseJSONItems();

        com.github.clans.fab.FloatingActionButton addLocationFabBtn = findViewById(R.id.addLocationFabBtn);

        addLocationFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddLocation = new Intent(getApplicationContext(), AddLocationActivity.class);
                startActivity(gotoAddLocation);
            }
        });

        com.github.clans.fab.FloatingActionButton allLocationFabBtn = findViewById(R.id.allLocationFabBtn);

        allLocationFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAllLocation = new Intent(getApplicationContext(), AllLocationActivity.class);
                startActivity(gotoAllLocation);
            }
        });

        com.github.clans.fab.FloatingActionButton addItemFabBtn = findViewById(R.id.addItemFabBtn);

        addItemFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddItem = new Intent(getApplicationContext(), AddItemActivity.class);
                startActivity(gotoAddItem);
            }
        });

    }

    private void parseJSONItems() {

        String emailString = "sdharchou@gmail.com";

        final String url = "http://localist-com.stackstaging.com/?getChecklist=1&email=" + emailString;

        // Request a string response
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            final ArrayList<AllItems> listItems = new ArrayList<>();
                            AllItemsAdapter adapter;

                            JSONObject storesAndItems = jsonObject.getJSONObject("items");

                            final HashMap<String, Boolean> selectedItem = new HashMap<>();

                            Iterator iterator = storesAndItems.keys();
                            while(iterator.hasNext()) {
                                String key = iterator.next().toString();
                                JSONArray items = (JSONArray)storesAndItems.get(key);
                                for(int i=0;i<items.length();i++) {
                                    listItems.add(new AllItems((String) items.get(i), key));
                                    selectedItem.put(items.get(i).toString(), false);sp.edit().putInt("checked", 0).apply();
                                }
                            }

                            if(storesAndItems.length() == 0) {
                                Toast.makeText(getApplicationContext(), "No items in checklist!", Toast.LENGTH_SHORT);
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
                                            deleteList();
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

    private void deleteList() {

        String emailString = "sdharchou@gmail.com";

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

}
