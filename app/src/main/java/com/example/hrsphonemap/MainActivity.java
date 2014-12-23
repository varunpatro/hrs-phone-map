package com.example.hrsphonemap;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = (Button) findViewById(R.id.makeCall);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                makeCall();
            }
        });

    }

    //Variable to handle text fields in the main design
//    public

    public int flat_to_index(int num) {
        return ((num / 100) - 1 ) * 4 + (num % 10);
    }

    private long[][] phone_numbers = new long[17][42];

    public void set_phone_number(int block_num, int flat_num, long number) {
        int i = flat_to_index(flat_num);
        this.phone_numbers[block_num][i] = number;
    }

    public long get_phone_number(int a, int b) {
        return this.phone_numbers[a][flat_to_index(b)];
    }

    public boolean validate_flat(int block, int flat) {
        if (block < 4) {
            return ((flat / 100) < 12) && ((flat % 100) < 5);
        } else if (block < 8) {
            return ((flat / 100) < 12) && ((flat % 100) < 5);
        } else if (block == 8) {
            return ((flat / 100) < 10) && ((flat % 100) < 3);
        } else if (block < 17) {
            return ((flat / 100) < 12) && ((flat % 100) < 5);
        } else {
            return false;
        }
    }

    private void set_data() {
        set_phone_number(11, 104, 9966004458L);
        set_phone_number(11, 503, 9505878874L);
        set_phone_number(11, 204, 9963042902L);
        set_phone_number(8, 901, 9963732901L);
    }

    private void validate_all_input() {
        EditText block_text = (EditText)findViewById(R.id.block);
        EditText flat_text = (EditText)findViewById(R.id.flat);
        TextView display_text = (TextView)findViewById(R.id.display);
        String message = "Block and Flat both valid.";

        if (block_text.getText().toString().matches("")) {
            message = "Please enter the block number";
        } else if (flat_text.getText().toString().matches("")) {
            message = "Please enter the flat number";
        } else {
            int block = Integer.parseInt(block_text.getText().toString());
            int flat = Integer.parseInt(flat_text.getText().toString());

            if (block > 16) {
                message = getString(R.string.invalid_block);
            } else {
                if (flat < 100) {
                    message = "Invalid flat number. Please enter a valid flat number.";
                } else if (!validate_flat(block, flat)) {
                    message = "Flat " + flat + " does not exist in Block " + block + ".";
                }
                ;
            }
        }

        display_text.setText(message);

    }





    protected void makeCall() {
        Log.i("Make call", "");

        set_data();
//        validate_all_input();

        final TextView display_text = (TextView)findViewById(R.id.display);
//        display_text.setText(res);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://vpatro.me:3000/get";

//      prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        display_text.setText(response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        display_text.setText(error.toString());
                    }
                }
        );

//      add it to the RequestQueue
        queue.add(getRequest);






//
//        String tel = "";
//        if (true) {
//            tel = "tel:0" + get_phone_number(block, flat);
//        }
//
//        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
//        phoneIntent.setData(Uri.parse(tel));
//
//
//
//
//        try {
//            startActivity(phoneIntent);
//            finish();
//            Log.i("Finished making a call...", "");
//        } catch (android.content.ActivityNotFoundException ex) {
//            Toast.makeText(MainActivity.this,
//                    "Call failed, please try again later.", Toast.LENGTH_SHORT).show();
//        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}