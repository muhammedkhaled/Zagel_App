package com.example.Zagel_App.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.Zagel_App.models.Contacts;
import com.example.Zagel_App.ui.adapter.ContactsAdapter;
import com.example.Zagel_App.firebase.controller.FBUtils;

import com.example.Zagel_App.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private DatabaseReference userRef;

    private RecyclerView mRecyclerView;
    private ContactsAdapter adapter;
    private List<Contacts> contactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        userRef = FBUtils.getUsersRef();

        mRecyclerView = findViewById(R.id.find_friends_recycler_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        getContacts();

        mToolBar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }


    private void getContacts() {
        FBUtils.getUsersRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactsList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String usersIDs = dataSnapshot1.getKey().toString();
                    FBUtils.getUsersRef().child(usersIDs).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Contacts contacts = dataSnapshot.getValue(Contacts.class);

                            if (!contacts.getuId().equals(FBUtils.getUserID())) {
                                contactsList.add(contacts);
                                adapter = new ContactsAdapter(contactsList, "FindFriend");
                                adapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(adapter);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}