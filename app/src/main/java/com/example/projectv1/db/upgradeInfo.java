package com.example.projectv1.db;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.projectv1.MainActivity;
import com.example.projectv1.ScoreManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


//for storing the default info about upgrades inside the database
public  class upgradeInfo {
    public static void applyOnBuy(Context context, String upgrade){
        if(onBuyMap == null) initOnBuyMap(context);
        OnBuyInterface onBuyInterface = onBuyMap.get(upgrade);
        if(onBuyInterface != null) onBuyInterface.onBuy(context);
    }

    public static long applyOnTap(Context context, String upgrade, long pointsGained, boolean isManual){
        if(onTapMap == null) initOnTapMap(context);
        OnTapInterface onTapInterface = onTapMap.get(upgrade);
        if(onTapInterface == null) return pointsGained;
        return onTapInterface.onTap(pointsGained, isManual, context);
    }
    private static HashMap<String, OnBuyInterface> onBuyMap;
    private static HashMap<String, OnTapInterface> onTapMap;
    private static void initOnBuyMap(Context context){
        onBuyMap = new HashMap<>();
        onBuyMap.put("Bonus", ctxt -> {ScoreManager.getInstance(ctxt).addScore(50, ctxt);});
        onBuyMap.put("Robot", ctxt -> {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctxt);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("autoclickerEnabled", true); //commit
            editor.apply();
        });
    }
    private static long lastClickTime;
    private static void initOnTapMap(Context context){
        onTapMap = new HashMap<>();
        onTapMap.put("+10", (pointsGained, isManual, ctxt) ->  {return pointsGained + 10;});
        onTapMap.put("Doubler", (pointsGained, isManual, ctxt) ->  {return pointsGained * 2;});
        onTapMap.put("Randomizer", (pointsGained, isManual, ctxt) -> {
                    Random random = new Random();
                    return pointsGained * random.nextInt(4);
                });

        onTapMap.put("Tripler", (pointsGained, isManual, ctxt) ->  {return pointsGained * 3;});
        onTapMap.put("Metronome", (pointsGained, isManual, ctxt) ->  {
            if(!isManual) return pointsGained;
            long retval;
            if(Math.abs((System.nanoTime() - lastClickTime) / 1E9 - 1) < 0.5 ) retval = pointsGained * 20;
            else retval = 0;
            lastClickTime  = System.nanoTime();
            return retval;
        });

        onTapMap.put("Automatic", (pointsGained, isManual, ctxt) ->  {
            if(isManual) return 0;
            return pointsGained * 5;
        });

        onTapMap.put("Manual", (pointsGained, isManual, ctxt) ->  {
            if(!isManual) return 0;
            return pointsGained * 5;
        });
        onTapMap.put("+100", (pointsGained, isManual, ctxt) ->  {return pointsGained + 100;});
        onTapMap.put("Exponentiator", (pointsGained, isManual, ctxt) ->  {return pointsGained + ScoreManager.getInstance(ctxt).getScore();});

    }
    public class innerUpgrade{
        public String name;
        public String description;
        public int price;
        public int priority;
        public boolean canDisable;
        innerUpgrade(String name, String description, int price, int priority, boolean canDisable){
            this.name = name;
            this.description = description;
            this.price = price;
            this.priority = priority;
            this.canDisable = canDisable;
        }
    }

    public  List<innerUpgrade> upgrades;
    public upgradeInfo(){
        initialize();
    }
    public  void initialize(){
        if(upgrades != null && !upgrades.isEmpty()) return;
        upgrades = new ArrayList<>();
        //upgrades are just placeholers for now
        upgrades.add(new innerUpgrade("Bonus", "50 free points", 10, 0, false));

        upgrades.add(new innerUpgrade("+10", "10 more points per tap", 30, 5, false));
        upgrades.add(new innerUpgrade("Doubler", "x2 points", 300, 10, false));
        upgrades.add(new innerUpgrade("Robot", "Taps the button every second", 500, 0, false));
        upgrades.add(new innerUpgrade("Randomizer", "Multiplies points by 0-3x", 1000, 11, true));

        upgrades.add(new innerUpgrade("Tripler", "x3 points", 2_000, 10, false));
        upgrades.add(new innerUpgrade("Metronome", "20x points, taps must be 1 second apart", 4_500, 31, true));
        upgrades.add(new innerUpgrade("Manual", "5x multiplier, disables autoclicker", 10_000, 30, true));
        upgrades.add(new innerUpgrade("Automatic", "5x multiplier, disables button", 10_000, 30, true));

        upgrades.add(new innerUpgrade("+100", "100 more points per tap", 50_000, 5, false));
        upgrades.add(new innerUpgrade("Exponentiator", "Points double every tap", 100_000, 1, false));




    }

    //for upgrades that have a one time effect when purchased
    public interface OnBuyInterface{
        public void onBuy(Context context);
    }

    //for upgrades that effect every tap
    public interface OnTapInterface{
        public long onTap(long pointsGained, boolean isManual, Context context);
    }


}
