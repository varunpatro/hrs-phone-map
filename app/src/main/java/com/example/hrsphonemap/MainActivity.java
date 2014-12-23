package com.example.hrsphonemap;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


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

    public int flat_to_index(int num) {
        return ((num / 100) - 1 ) * 4 + (num % 100);
    }

    private long[][] phone_numbers = new long[17][45];

    public void set_phone_number(int block_num, int flat_num, long number) {
        int i = flat_to_index(flat_num);
        this.phone_numbers[block_num][i] = number;
    }

    public long get_phone_number(int a, int b) {
        return this.phone_numbers[a][flat_to_index(b)];
    }



    protected void makeCall() {
        Log.i("Make call", "");

        set_phone_number(11, 104, 9966004458L);
        set_phone_number(11, 503, 9505878874L);
        set_phone_number(11, 204, 9963042902L);
        set_phone_number(8, 901, 9963732901L);

        EditText block_text = (EditText)findViewById(R.id.block);
        int block = Integer.parseInt(block_text.getText().toString());

        EditText flat_text = (EditText)findViewById(R.id.flat);
        int flat = Integer.parseInt(flat_text.getText().toString());


        String tel = "";
        if (true) {
            tel = "tel:0" + get_phone_number(block, flat);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}