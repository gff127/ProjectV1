package com.example.projectv1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DialogFragment;
import androidx.preference.PreferenceManager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.projectv1.db.ProjectDatabase;
import com.example.projectv1.db.Upgrade;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private static List<Upgrade> upgradeList;
    private static ScoreManager scoreManager;

    private int red; private int green; private int blue; //button colors



    private Thread upgradeGetterThread; //for retrieving a list of upgrades from UpgradeDAO
    private Thread backgroundThread; // handles background activities that occur regularlry (autoclicking, updating points per secoond)
    private Handler backgroundHandler; //handler for background thread
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ProjectDatabase x = ProjectDatabase.getDatabase(getApplication());

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        scoreManager = ScoreManager.getInstance(MainActivity.this);


        initBackgroundThread();

        if(savedInstanceState != null){
            red = savedInstanceState.getInt("r");
            blue = savedInstanceState.getInt("b");
            green = savedInstanceState.getInt("g");
            findViewById(R.id.mainButton).setBackgroundTintList(ColorStateList
                    .valueOf(Color.argb(255, red, blue, green)));
        }





    }

    private void initBackgroundThread(){
        if(backgroundThread != null){
            backgroundThread.interrupt();
            backgroundThread = null;
        }
        if(backgroundThread == null){
            backgroundThread = new Thread(() -> {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                while(true){
                    //System.out.println("THREAD STILL RUNNING");
                    try {
                        long pointsPrev = scoreManager.getScore();
                        Thread.sleep(1000);
                        if(sharedPreferences.getBoolean("autoclickerEnabled", false) && !upgradeGetterThread.isAlive()){
                            scoreManager.updateScore(false, MainActivity.this, upgradeList);

                        }

                        Message msg = backgroundHandler.obtainMessage();
                        long newScore = scoreManager.getScore();
                        String s1 = scoreManager.formatNumber(newScore, MainActivity.this);
                        String s2 = scoreManager.formatNumber(newScore - pointsPrev, MainActivity.this);
                        if(newScore -pointsPrev < 0) s2 = scoreManager.formatNumber(0, MainActivity.this);
                        String[] arg = {s1, s2};
                        msg.obj = arg;
                        backgroundHandler.sendMessage(msg);


                    } catch (InterruptedException e) {
                        break;
                    }


                }
            });

            backgroundThread.start();
        }

        backgroundHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                String[] message = (String[]) msg.obj;
                ((TextView) findViewById(R.id.score)).setText(message[0]);
                ((TextView) findViewById(R.id.pps)).setText(message[1] + " pts/s");
                ((TextView) findViewById(R.id.prestigeInfo)).setText("current prestige: " + scoreManager.getPrestige(MainActivity.this));

            }
        };
    }

    @Override
    protected void onResume() {
        System.out.println("ONRESUME");

        super.onResume();
        ( (TextView) findViewById(R.id.score)).setText(scoreManager.formatNumber(scoreManager.getScore(), MainActivity.this));
        ((TextView) findViewById(R.id.prestigeInfo)).setText("current prestige: " + scoreManager.getPrestige(MainActivity.this));


        upgradeGetterThread = new Thread(() -> {
            //don't attempt to get upgrades when upgrades are still being reset
            while(ScoreManager.prestigeThread != null && ScoreManager.prestigeThread.isAlive()){}
            upgradeList = ProjectDatabase.getDatabase(MainActivity.this).upgradeDAO().getEnabled();
        }
        );
        upgradeGetterThread.start();

        if(backgroundThread == null || !backgroundThread.isAlive()){
            initBackgroundThread();
        }
    }

    public void gotoUpgrades(View button){
        startActivity(new Intent(this, UpgradesActivity.class));
    }
    public void gotoSettings(View button){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void mainButtonClicked(View button){
        //make sure upgradeGetterThread finished running before passing upgradeList
        if(!upgradeGetterThread.isAlive()) {
            scoreManager.updateScore(true, MainActivity.this, upgradeList);
        }
        else scoreManager.updateScore(true, MainActivity.this, null);
        Random random = new Random();
        red = random.nextInt(256);blue = random.nextInt(256);
        green = random.nextInt(256);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, red, blue, green)));
        //button.setBackgroundColor(Color.parseColor("#eFFacc"));

        //play sound effect if game isn't muted
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if(!sharedPreferences.getBoolean("muteSetting", false)){

            MediaPlayer mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int request = audioManager.requestAudioFocus(focusChange -> {}, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            if(request == audioManager.AUDIOFOCUS_REQUEST_GRANTED){
                mp.create(this, R.raw.pickupcoin).start();
            }

            if(mp != null) mp.release();
            mp = null;
        }
        ( (TextView) findViewById(R.id.score)).setText(scoreManager.formatNumber(scoreManager.getScore(), MainActivity.this));

    }

    public void prestigeButtonClicked(View button){
        final long PRESTIGE_SCORE = 1_000_000_000;
        if(scoreManager.getScore() < PRESTIGE_SCORE){
            Toast.makeText(this, "1 Trillion Points Required For Prestige", Toast.LENGTH_SHORT).show();
        }
        else {
            scoreManager.prestige(MainActivity.this);
            upgradeList = null;
            ((TextView) findViewById(R.id.prestigeInfo)).setText("current prestige: " + scoreManager.getPrestige(MainActivity.this));
        }


    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("r", red);
        bundle.putInt("b", blue);
        bundle.putInt("g", green);
    }
    @Override
    public void onStop(){
        System.out.println("ONSTOP");
        super.onStop();
        backgroundThread.interrupt(); //thread only runs when app is open with main activity active

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }



    //confirmation before deleting save file
    public static class DeleteDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Are you sure you want to delete all save data?");
            builder.setPositiveButton("Confirm", (dialog, id) -> {
                scoreManager.prestige(getContext());
                scoreManager.resetPrestige(getContext());

                upgradeList = null;

            });
            builder.setNegativeButton("Cancel", (dialog, id) -> {});
            return builder.create();
        }

        @Override
        public void onAttach(Context context){
            super.onAttach(context);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;


            case R.id.action_delete:
                DialogFragment dialogFragment = new DeleteDialog();
                dialogFragment.show(getFragmentManager(), "tag");
                return true;



    default:
                return super.onOptionsItemSelected(item);

        }

    }




}

