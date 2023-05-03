package com.example.projectv1.db;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;

@Database(entities = {Upgrade.class}, version = 1, exportSchema = false)
public abstract class ProjectDatabase extends RoomDatabase {
    public abstract UpgradeDAO upgradeDAO();
    private static ProjectDatabase instance;

    //return a reference to the database
    public static ProjectDatabase getDatabase(final Context context){
        System.out.println("getting db");
        if(instance != null) return instance;
        synchronized (ProjectDatabase.class){
            if(instance == null){
                System.out.println("creating db");
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        ProjectDatabase.class, "project_database")
                        .addCallback(createDbCallback).build();
            }
        }
        return instance;
    }


    //for returning an individual upgrade
    public interface UpgradeListener{
        void onUpgradeReturned(Upgrade upgrade);
    }

    //for returning a list of upgrades
    public interface UpgradesListener{
        void onUpgradesReturned(List<Upgrade> upgrade);
    }

    //populate the upgrade table with information in upgradeInfo
    private static void createUpgradeTable(){
        upgradeInfo defaults = new upgradeInfo();
        System.out.println("creating Upgrade table");
        int counter = 0;
        for(upgradeInfo.innerUpgrade i : defaults.upgrades){
            System.out.println("adding upgrade");
            Upgrade newUpgrade = new Upgrade(0, i.name, i.description, i.price, i.priority,
                    false, false, i.canDisable);
            insertUpgrade(newUpgrade);
            counter++;
        }
    }

    //get an upgrade by id
    public static void getUpgrade(int id, UpgradeListener listener){
        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message message){
                super.handleMessage(message);
                listener.onUpgradeReturned((Upgrade) message.obj);
            }
        };

        new Thread(() -> {
           Message message = handler.obtainMessage();
           message.obj = instance.upgradeDAO().getById(id);
           handler.sendMessage(message);
        });
    }

    //get a list of all enabled upgrades
    public static void getEnabledUpgrades(UpgradesListener listener){
        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message message){
                super.handleMessage(message);
                listener.onUpgradesReturned((List<Upgrade>) message.obj);
            }
        };

        new Thread(() -> {
            Message message = handler.obtainMessage();
            message.obj = instance.upgradeDAO().getEnabled();
            handler.sendMessage(message);
        });
    }

    //called when db is created
    private static RoomDatabase.Callback createDbCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            System.out.println("ON CREATE");
            createUpgradeTable();
        }
    };

    public static void insertUpgrade(Upgrade upgrade) {
        (new Thread(()-> instance.upgradeDAO().insert(upgrade))).start();
    }

    public static void updateUpgrade(Upgrade upgrade) {
        (new Thread(()-> instance.upgradeDAO().update(upgrade))).start();

    }
}
