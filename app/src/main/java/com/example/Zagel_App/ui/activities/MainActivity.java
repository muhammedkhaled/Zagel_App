package com.example.Zagel_App.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.Zagel_App.ui.adapter.TabsAccessorAdapter;
import com.example.Zagel_App.firebase.util.Constance;
import com.example.Zagel_App.firebase.controller.FBUtils;
import com.example.Zagel_App.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Zagel App");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tab);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();


        if(currentUser != null){

            updateUserStatus(Constance.ONLINE);

            verifyUserExistence();
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null){
            updateUserStatus(Constance.OFFLINE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null){
            updateUserStatus(Constance.OFFLINE);
        }

    }

    private void verifyUserExistence() {
        FBUtils.getUsersCurrentIDRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child(Constance.NAME).exists())){
                    sendToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    // to edit with switch case
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){

            case R.id.main_logout_option:
                logOut();
                break;
            case R.id.main_settings_option:
                sendToSettingsActivity();
                break;
            case R.id.main_find_friends_option:
                sendUserToFindFriendActivity();
                break;
        }

        return true;
    }

    private void logOut() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        updateUserStatus(Constance.OFFLINE);

                        mAuth.signOut();
                        sendUserToLoginActivity();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setMessage("Are you Sure ?")
                .setPositiveButton("Yes logout", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }




    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    private void sendToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToFindFriendActivity() {
        Intent findFriendIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
//        findFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findFriendIntent);
    }



    private void updateUserStatus(String state){

        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put(Constance.TIME, saveCurrentTime);
        onlineStateMap.put(Constance.DATE, saveCurrentDate);
        onlineStateMap.put(Constance.STATE, state);

//        currentUserID = mAuth.getCurrentUser().getUid();
        FBUtils.getUsersCurrentIDStateRef()
                .updateChildren(onlineStateMap);
    }


}
