package com.upc.fib.racopocket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView welcomeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        welcomeName = (TextView) findViewById(R.id.welcome_name);

        String studentData = "";
        try {
            InputStream inputStream = openFileInput("info-personal.json");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                studentData = stringBuilder.toString();
            }
        } catch (IOException e) {
            Log.e("FILE", "Can not read file: " + e.toString());
            e.printStackTrace();
        }

        try {
            JSONObject object = new JSONArray(studentData).getJSONObject(0);
            welcomeName.setText(object.getString("username"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Boolean isFragment = true;

        int id = item.getItemId();
        Fragment newFragment = null;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (id == R.id.nav_timetable) {
            newFragment = new TimetableMainMenu();
        } else if (id == R.id.nav_notifications) {
            newFragment = new NotificationsMainMenu();
        } else if (id == R.id.nav_schedule) {
            newFragment = new ScheduleMainMenu();
        } else if (id == R.id.nav_class_availability) {
            newFragment = new ClassAvailabilityMainMenu();
        } else if (id == R.id.nav_subject_info) {
            newFragment = new SubjectInfoMainMenu();
        } else if (id == R.id.nav_language) {
            isFragment = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.language).setItems(R.array.languages_array, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    if (which == 0) {
                        setLocale("ca");
                    } else if (which == 1) {
                        setLocale("es");
                    } else {
                        setLocale("en");
                    }
                    finish();
                    Intent intent = new Intent(MainMenuActivity.this, MainMenuActivity.class);
                    startActivity(intent);
                }
            });
            builder.show();
        } else if (id == R.id.nav_share) {
            isFragment = false;
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "RacoPocket");
            String sAux = "\nLet me recommend you this application\n\n";
            sAux = sAux + "https://play.google.com/store/\n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "choose one"));
        } else if (id == R.id.nav_about) {
            isFragment = false;
            Intent intent = new Intent(MainMenuActivity.this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            isFragment = false;

            SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
            sharedPreferences.edit().remove("accessToken").apply();
            sharedPreferences.edit().remove("language").apply();

            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

        if (newFragment != null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_menu_fragment_container, newFragment).commit();
            transaction.replace(R.id.main_menu_fragment_container, newFragment);
            transaction.addToBackStack(null);
        } else if (isFragment) {
            Toast.makeText(MainMenuActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
        }
        transaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (exit) {
                finish();
            } else {
                Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 3000);
            }
        }
    }

    public void setActionBarDesign(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setLocale(String localeCode) {

        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        this.getResources().updateConfiguration(configuration, this.getResources().getDisplayMetrics());
        Toast.makeText(this, getResources().getString(R.string.new_language), Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPreferences = getSharedPreferences("racopocket.preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", localeCode);
        editor.apply();

    }

}
