package com.example.hrsphonemap;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Created by varun on 2/20/15.
 */
public class MenuActivity extends ActionBarActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
}
