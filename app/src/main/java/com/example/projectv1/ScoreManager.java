package com.example.projectv1;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.projectv1.db.ProjectDatabase;
import com.example.projectv1.db.Upgrade;
import com.example.projectv1.db.upgradeInfo;

import java.text.DecimalFormat;
import java.util.List;

//provides synchronization for the game's score
public class ScoreManager {
    private long score;
    private int prestige;
    private static ScoreManager instance;
    private ScoreManager(long score){
        this.score = score;
    }
    private ScoreManager(){
        this.score = 0;
    }



    public static synchronized ScoreManager getInstance(Context context){
        if(instance == null){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            instance =  new ScoreManager(sharedPreferences.getLong("score", 0));
        }
        return instance;

    }


    private synchronized void saveScore(Context context){
        SharedPreferences sharedPreferences = PreferenceManager. getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("score", score); //commit
        editor.apply();
    }

    public synchronized void updateScore(Boolean isManual, Context context, List<Upgrade> upgradeList){
        if (prestigeThread != null &&  prestigeThread.isAlive()) return; //upgrades are still being reset, don't attempt to upgdate score
        long amountToAdd = 1;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);


        if(upgradeList != null) {

            for (Upgrade upgrade : upgradeList) {

                amountToAdd = upgradeInfo.applyOnTap(context, upgrade.name, amountToAdd, isManual);
            }
        }


        score += amountToAdd * (long )Math.pow(2,sharedPreferences.getInt("prestige", 1) - 1);
        final long max_score = 1_000_000_000_000_000L;
        if(score > max_score) score = max_score; //stop overflow
        saveScore(context);
    }

    public synchronized void subtractScore(long amount, Context context){
        score -= amount;
        saveScore(context);
    }

    public synchronized void addScore(long amount, Context context){
        score += amount;
        saveScore(context);
    }

    public synchronized long getScore(){
        return score;
    }

    //only used for debugging
    public synchronized void doubleScore(Context context){
        score *= 2;
        saveScore(context);
    }
    public static Thread prestigeThread;
    public synchronized void prestige(Context context){
        if (prestigeThread != null && prestigeThread.isAlive()) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //
         prestigeThread = new Thread(() ->{


            List<Upgrade> upgradeList = ProjectDatabase.getDatabase(context).upgradeDAO().getAll();
            for(Upgrade upgrade : upgradeList){
                upgrade.enabled = false;
                upgrade.owned = false;
            }

            ProjectDatabase.getDatabase(context).upgradeDAO()
                    .update(upgradeList.toArray(new Upgrade[upgradeList.size()]));

        });
        prestigeThread.start();
        editor.putBoolean("autoclickerEnabled", false);
        editor.putInt("prestige", sharedPreferences.getInt("prestige",1) + 1);
        editor.apply();

        score = 0;
        saveScore(context);
    }

    public synchronized void resetPrestige(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("prestige", 1);
        editor.apply();

        saveScore(context);
    }

    public synchronized int getPrestige(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("prestige", 1);
    }

    public String formatNumber(long number, Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String notation = sharedPreferences.getString("notationSetting", "Standard");

        final long MAX_SCORE = 1_000_000_000_000_000L;
        if(number >= MAX_SCORE){
            return "Infinite";
        }

        final String[] words = {"", "Thousand", "Million", "Billion", "Trillion", "Quadrillion", "Quintillion"};
        switch(notation){
            case "Standard":
                String scoreString = String.valueOf(number);
                if(number < 1000){
                    return scoreString;
                }
                //otherwise format in word form (e.g. 12,731,521 displayed as "12.731 million"
                int num_digits = scoreString.length();
                String word = words[(num_digits - 1) / 3];
                int cutoffIndex = (num_digits - 1) % 3 +1;
                String main_segment = scoreString.substring(0, cutoffIndex);


                long truncatedSegment = Long.parseLong(scoreString.substring(cutoffIndex));
                int truncatedDigits =  scoreString.substring(cutoffIndex).length();
                int truncatedDivisorPower = truncatedDigits + 3 - ((truncatedDigits + 2) % 3);

                double truncatedDecimal = (truncatedSegment * 1.0) / Math.pow(10, truncatedDivisorPower - 1);
                String decimalSegment = String.format("%.3f", truncatedDecimal).substring(2);

                return main_segment + "." + decimalSegment + " " + word;

            case "Scientific":
                //scientific notation with 3 decimal places
                DecimalFormat decimalFormat = new DecimalFormat("0.###E0");
                return decimalFormat.format(number);
        }


        return "error";

    }


}
