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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


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

        Button syncBtn = (Button) findViewById(R.id.sync);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sync();
            }
        });

    }

    //Variable to handle text fields in the main design
//    public

    public int flat_to_index(int num) {
        return ((num / 100) - 1 ) * 4 + (num % 10);
    }

    private long[][][] phone_numbers = new long[17][42][3];

    public void set_phone_number(int block_num, int flat_num, int tel_num, long number) {
        int i = flat_to_index(flat_num);
        this.phone_numbers[block_num][i][tel_num] = number;
    }

    public long get_phone_number(int a, int b, int c) {
        return this.phone_numbers[a][flat_to_index(b)][c-1];
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
        set_phone_number(11, 104, 1, 9966004458L);
        set_phone_number(11, 503, 1, 9505878874L);
        set_phone_number(11, 204, 1, 9963042902L);
        set_phone_number(8, 901, 1, 9963732901L);
    }

    private String validate_all_input(int block, int flat) {

         if (block > 16) {
             return getString(R.string.invalid_block);
         } else {
             if (flat < 100) {
                 return "Invalid flat number. Please enter a valid flat number.";
             } else if (!validate_flat(block, flat)) {
                 return "Flat " + flat + " does not exist in Block " + block + ".";
             } else {
                 return "";
             }
         }
    }

    private String parse(JSONObject obj) {
        String temp = "Sync Complete";

        try {
            for(int i = 0; i < obj.names().length(); i++){
                int block_num = Integer.parseInt(obj.names().getString(i));
                JSONObject block_obj = obj.getJSONObject(obj.names().getString(i));
                for(int j = 0; j < block_obj.length(); j++){
                    int flat_num = Integer.parseInt(block_obj.names().getString(j));
                    JSONArray tel_array = block_obj.getJSONArray(block_obj.names().getString(j));
                    for(int z = 0; z < tel_array.length(); z++) {
                        if (!tel_array.getString(z).matches("")) {
                            long tel_no = tel_array.getLong(z);
                            set_phone_number(block_num, flat_num, z, tel_no);
                        }
                    }



                }
            }

        } catch (JSONException ex) {
            temp = ex.toString();
        }
        return temp;

    }

    protected void sync() {
        final TextView display_text = (TextView)findViewById(R.id.display);
        final Button btn = (Button) findViewById(R.id.makeCall);
        btn.setEnabled(false);
        display_text.setText("Syncing...");
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
//                        display_text.setText(response.toString());
                        display_text.setText(parse(response));
                        display_text.setText("Sync complete.");
                        btn.setEnabled(true);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        display_text.setText("Error: " + "Cannot connect to server.");
                        btn.setEnabled(true);
                    }
                }

        );

//      add it to the RequestQueue
        queue.add(getRequest);


    }

    private void call(int block, int flat, int tel_num) {
        String tel = "";
        if (true) {
            tel = "tel:0" + get_phone_number(block, flat, tel_num);
        }

        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse(tel));

        try {
            startActivity(phoneIntent);
            finish();
            Log.i("Finished making a call...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "Call failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }





    protected void makeCall() {
        Log.i("Make call", "");

        View call_btn = findViewById(R.id.makeCall);
        View tel_btn1 = findViewById(R.id.tel1);
        View tel_btn2 = findViewById(R.id.tel2);
        View tel_btn3 = findViewById(R.id.tel3);


        EditText block_text = (EditText)findViewById(R.id.block);
        EditText flat_text = (EditText)findViewById(R.id.flat);
        TextView display_text = (TextView)findViewById(R.id.display);
        String message = "Block and Flat both valid.";


//        set_data();

        if (block_text.getText().toString().matches("")) {
            display_text.setText("Please enter the block number");
        } else if (flat_text.getText().toString().matches("")) {
            display_text.setText("Please enter the flat number");
        } else {
            int block = Integer.parseInt(block_text.getText().toString());
            int flat = Integer.parseInt(flat_text.getText().toString());
            String validation = validate_all_input(block, flat);
            if (validation.matches("")) {
                if ((get_phone_number(block, flat, 1) == 0) && get_phone_number(block, flat, 2) == 0 && get_phone_number(block, flat, 3) == 0) {
                    display_text.setText("No phone number registered in this house.");
                } else {
                    call_btn.setVisibility(View.GONE);
                    for (int count = 1; count <= 3; count++) {
                        if (get_phone_number(block, flat, count) != 0) {
                            tel_btn1.setVisibility(View.VISIBLE);
                        }
                    }
//                    call(block, flat, 1);
                }
            } else {
                display_text.setText(validation);
            }
        }









//

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}