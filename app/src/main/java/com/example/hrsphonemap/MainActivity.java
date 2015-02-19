package com.example.hrsphonemap;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;



public class MainActivity extends ActionBarActivity {

    // Variables

    private static final String PORT_NUM = "5000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        local_read();

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


    public int flat_to_index(int num) {
        return ((num / 100) - 1 ) * 4 + (num % 10);
    }

    private long[][][] phone_numbers = new long[17][42][3];

    public void set_phone_number(int block_num, int flat_num, int tel_num, long number) {
        int i = flat_to_index(flat_num);
        this.phone_numbers[block_num][i][tel_num ] = number;
    }

    public long get_phone_number(int a, int b, int c) {
        return this.phone_numbers[a][flat_to_index(b)][c];
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

    private String validate_all_input(int block, int flat) {

         if (block > 16) {
             return getString(R.string.invalid_block);
         } else {
             if (flat < 100 || (flat % 100 == 0)) {
                 return "Invalid flat number. Please enter a valid flat number.";
             } else if (!validate_flat(block, flat)) {
                 return "Flat " + flat + " does not exist in Block " + block + ".";
             } else {
                 return "";
             }
         }
    }

    private String parse(JSONObject obj) {
        Integer tel_nos = 0;
        Integer flat_nos = 0;

        String temp = "";
//        temp = "";

        try {
            for(int i = 0; i < obj.names().length(); i++){
                int block_num = Integer.parseInt(obj.names().getString(i));
                JSONObject block_obj = obj.getJSONObject(obj.names().getString(i));
                for(int j = 0; j < block_obj.length(); j++){
                    int flat_num = Integer.parseInt(block_obj.names().getString(j));
                    JSONArray tel_array = block_obj.getJSONArray(block_obj.names().getString(j));
                    flat_nos++;
                    for(int z = 0; z < tel_array.length(); z++) {
                        if (!tel_array.getString(z).matches("")) {
                            long tel_no = tel_array.getLong(z);
                            set_phone_number(block_num, flat_num, z, tel_no);
                            tel_nos++;
                        }

                    }

                }
            }

        } catch (JSONException ex) {
            temp = ex.toString();
        }
        return temp + "" + flat_nos + " Flats with " + tel_nos + " numbers";

    }

    protected void sync() {
        final TextView display_text = (TextView)findViewById(R.id.display);
        final Button btn = (Button) findViewById(R.id.makeCall);
        btn.setEnabled(false);
        display_text.setText("Syncing...");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://vpatro.me:" + PORT_NUM + "/get";

        // Prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        display_text.setText(response.toString());
                        local_store(response);
                        display_text.setText("Sync Complete." + parse(response) + " updated.");
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
        String tel = "tel:0" + get_phone_number(block, flat, tel_num);

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

    protected void local_read() {
//        String data_string = "hello world!";
        final TextView display_text = (TextView)findViewById(R.id.display);

        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    openFileInput("DATA_FILENAME")));
            String inputString;
            StringBuffer stringBuffer = new StringBuffer();
            while ((inputString = inputReader.readLine()) != null) {
                stringBuffer.append(inputString + "\n");
            }
            String data_string = stringBuffer.toString();

            try {
                JSONObject data_json = new JSONObject(data_string);
                if (!data_string.equals("")) {
                    String parse_response = parse(data_json);
                    display_text.setText("Contacts for " + parse_response + " loaded.");
                } else {
                    display_text.setText("Unable to load data. Please Sync.");
                }

            } catch (JSONException ex) {
                Log.i("ex: ", ex.toString());
            }

        } catch (IOException e) {
            display_text.setText("Unable to load data. Please Sync.");
        }




    }
    protected void local_store(JSONObject data) {
        //        String data_string = "hello world!";


        try {
            FileOutputStream fos = openFileOutput("DATA_FILENAME", Context.MODE_PRIVATE);
            fos.write(data.toString().getBytes());
            fos.close();
        }
        catch(Exception e) {
            Log.i("error:"+ e, "");
        }
    }
    protected void local_delete() {

        try {
            FileOutputStream fos = openFileOutput("DATA_FILENAME", Context.MODE_PRIVATE);
            fos.write("No data".getBytes());
            fos.close();
            final TextView display_text = (TextView)findViewById(R.id.display);
            display_text.setText("Database Deleted.");

        }
        catch(Exception e) {
            Log.i("error:"+ e, "");
        }
    }



    protected void makeCall() {
        Log.i("Make call", "");

        final View call_btn = findViewById(R.id.makeCall);
        final View back_btn = findViewById(R.id.backBtn);
        final Button tel_btn1 = (Button) findViewById(R.id.tel1);
        final Button tel_btn2 = (Button) findViewById(R.id.tel2);
        final Button tel_btn3 = (Button) findViewById(R.id.tel3);

        View tel_btns[] = new View[3];
        tel_btns[0] = tel_btn1;
        tel_btns[1] = tel_btn2;
        tel_btns[2] = tel_btn3;

        EditText block_text = (EditText)findViewById(R.id.block);
        EditText flat_text = (EditText)findViewById(R.id.flat);
        TextView display_text = (TextView)findViewById(R.id.display);

//        set_data();

        if (block_text.getText().toString().matches("")) {
            display_text.setText("Please enter the block number");
        } else if (flat_text.getText().toString().matches("")) {
            display_text.setText("Please enter the flat number");
        } else {
            final int block = Integer.parseInt(block_text.getText().toString());
            final int flat = Integer.parseInt(flat_text.getText().toString());
            String validation = validate_all_input(block, flat);
            if (validation.matches("")) {
                if ((get_phone_number(block, flat, 0) == 0) && get_phone_number(block, flat, 1) == 0 && get_phone_number(block, flat, 2) == 0) {
                    display_text.setText("No phone number registered in this house.");
                } else {
                    display_text.setText("");
                    back_btn.setVisibility(View.VISIBLE);
                    back_btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            call_btn.setVisibility(View.VISIBLE);
                            back_btn.setVisibility(View.INVISIBLE);
                            tel_btn1.setVisibility(View.INVISIBLE);
                            tel_btn2.setVisibility(View.INVISIBLE);
                            tel_btn3.setVisibility(View.INVISIBLE);
//                            read_btn.setVisibility(View.VISIBLE);
//                            delete_btn.setVisibility(View.VISIBLE);
                        }
                    });
                    call_btn.setVisibility(View.INVISIBLE);
//                    read_btn.setVisibility(View.INVISIBLE);
//                    delete_btn.setVisibility(View.INVISIBLE);
                    for (int count = 0; count < 3; count++) {
                        if (get_phone_number(block, flat, count) != 0) {
                            final int temp = count;
                            tel_btns[count].setVisibility(View.VISIBLE);
                            tel_btns[count].setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    call(block, flat, temp);
                                }
                            });
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