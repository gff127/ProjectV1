package com.example.projectv1.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UpgradeViewModel extends AndroidViewModel {

    private LiveData<List<Upgrade>> unownedUpgrades;
    private LiveData<List<Upgrade>> ownedUpgrades;


    public UpgradeViewModel(@NonNull Application application) {
        super(application);
        ownedUpgrades = ProjectDatabase.getDatabase(getApplication()).upgradeDAO().getByOwned(true);
        unownedUpgrades  = ProjectDatabase.getDatabase(getApplication()).upgradeDAO().getByOwned(false);
    }

    public LiveData<List<Upgrade>> getAllByOwned(Boolean owned){
        if(owned) return ownedUpgrades;
        else return unownedUpgrades;
    }
}
