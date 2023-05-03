package com.example.projectv1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.projectv1.db.ProjectDatabase;
import com.example.projectv1.db.Upgrade;
import com.example.projectv1.db.UpgradeViewModel;
import com.example.projectv1.db.upgradeInfo;

import java.util.List;


public class UpgradesActivity extends AppCompatActivity {
    //separate viewmodels for two separate recycler views
    private UpgradeViewModel unownedUpgradeViewModel;
    private UpgradeViewModel ownedUpgradeViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrades);

        RecyclerView availableUpgradesView = findViewById(R.id.availableRecyclerView);
        RecyclerView ownedUpgradesView = findViewById(R.id.ownedRecyclerView);
        UpgradeListAdapter unownedAdapter = new UpgradeListAdapter(this);
        OwnedUpgradeListAdapter ownedAdapter = new OwnedUpgradeListAdapter(this);
        availableUpgradesView.setAdapter(unownedAdapter);
        ownedUpgradesView.setAdapter(ownedAdapter);
        availableUpgradesView.setLayoutManager(new LinearLayoutManager(this));
        ownedUpgradesView.setLayoutManager(new LinearLayoutManager(this));


        unownedUpgradeViewModel = new ViewModelProvider(this).get(UpgradeViewModel.class);
        unownedUpgradeViewModel.getAllByOwned(false).observe(this, unownedAdapter::setUpgrades);

        ownedUpgradeViewModel = new ViewModelProvider(this).get(UpgradeViewModel.class);
        ownedUpgradeViewModel.getAllByOwned(true).observe(this, ownedAdapter::setUpgrades);




    }


    //adater for upgrades in the unowned recyclerview
    public class UpgradeListAdapter extends RecyclerView.Adapter<UpgradeListAdapter.UpgradeViewHolder>{


        private final LayoutInflater layoutInflater;
        private List<Upgrade> upgrades;

        public UpgradeListAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        class UpgradeViewHolder extends RecyclerView.ViewHolder{
            private final TextView nameView;
            private final TextView descriptionView;
            private final TextView priceView;
            private final Button buttonView;

            private Upgrade upgrade;

            public UpgradeViewHolder(@NonNull View itemView) {
                super(itemView);

                //name, description, price, and a button to buy the upgrade are displayed
                nameView = itemView.findViewById(R.id.upgradeName);
                descriptionView = itemView.findViewById(R.id.upgradeDescription);
                priceView = itemView.findViewById(R.id.upgradePrice);
                buttonView = itemView.findViewById(R.id.upgradeButton);


            }
        }

        @NonNull
        @Override
        public UpgradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.available_list_item, parent, false);
            return new UpgradeViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull UpgradeViewHolder holder, int position) {
            if(upgrades != null){

                Upgrade current = upgrades.get(position);
                holder.upgrade = current;


                holder.nameView.setText(current.name);
                holder.descriptionView.setText(current.description);

                holder.priceView.setText(ScoreManager.getInstance(UpgradesActivity.this).formatNumber(current.price, UpgradesActivity.this) + " points");
                //maybe add logic to gray out buy buttons that aren't affordable

                //when purchase button is clicked
                holder.buttonView.setOnClickListener(view -> {
                    ScoreManager score = ScoreManager.getInstance(UpgradesActivity.this);
                    if(score.getScore() < current.price){
                        return;
                    }
                    score.subtractScore(current.price, UpgradesActivity.this);

                    current.enabled = true;
                    current.owned = true;
                    upgradeInfo.applyOnBuy(UpgradesActivity.this, current.name);
                    ProjectDatabase.updateUpgrade(current); //upgrade automatically moved to other recyclerview


                });

            }
        }


        @Override
        public int getItemViewType(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public int getItemCount() {
            if(upgrades != null){
                return upgrades.size();
            }
            return 0;
        }

        public void setUpgrades(List<Upgrade> upgrades){
            this.upgrades = upgrades;
            notifyDataSetChanged();
        }

    }

















    //adapter for upgrades that are in the Owned recyclerview

    public class OwnedUpgradeListAdapter extends RecyclerView.Adapter<OwnedUpgradeListAdapter.OwnedUpgradeViewHolder>{


        private final LayoutInflater layoutInflater;
        private List<Upgrade> upgrades;

        public OwnedUpgradeListAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        class OwnedUpgradeViewHolder extends RecyclerView.ViewHolder{
            private final TextView nameView;
            private final TextView descriptionView;
            private final ToggleButton enableView;

            private Upgrade upgrade;

            public OwnedUpgradeViewHolder(@NonNull View itemView) {
                super(itemView);

                //the name, description, and a button to toggle the upgrade
                nameView = itemView.findViewById(R.id.upgradeName);
                descriptionView = itemView.findViewById(R.id.upgradeDescription);
                enableView = itemView.findViewById(R.id.upgradeToggle);


            }
        }

        @NonNull
        @Override
        public OwnedUpgradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.owned_list_item, parent, false);
            return new OwnedUpgradeViewHolder(itemView);
        }


        //these overrides stop toggle buttons from disappearing for some reason
        @Override
        public int getItemViewType(int position) {
            return position;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull OwnedUpgradeViewHolder holder, int position) {

            System.out.println(position);
            System.out.println(holder.nameView.getText());
            if(upgrades != null){

                Upgrade current = upgrades.get(position);
                holder.upgrade = current;


                holder.nameView.setText(current.name);
                holder.descriptionView.setText(current.description);
                if (current.canDisable) {
                    holder.enableView.setChecked(current.enabled);
                    holder.enableView.setOnClickListener(view -> {
                        current.enabled = !current.enabled;
                        ProjectDatabase. updateUpgrade(current);
                        holder.enableView.setChecked(current.enabled);
                    });
                }
                else{
                    holder.enableView.setVisibility(View.GONE);
                }


            }
        }

        @Override
        public int getItemCount() {
            if(upgrades != null){
                return upgrades.size();
            }
            return 0;
        }

        public void setUpgrades(List<Upgrade> upgrades){
            this.upgrades = upgrades;
            notifyDataSetChanged();
        }

    }

    public void exitClicked(View button){
        finish();
    }



}




