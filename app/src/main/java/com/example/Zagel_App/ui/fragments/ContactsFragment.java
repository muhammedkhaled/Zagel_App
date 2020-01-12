package com.example.Zagel_App.ui.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.Zagel_App.ui.adapter.ContactsAdapter;

import com.example.Zagel_App.firebase.controller.FBUtils;
import com.example.Zagel_App.models.Contacts;
import com.example.Zagel_App.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView mRecyclerView;
    private ContactsAdapter adapter;
    private List<Contacts> contactsList = new ArrayList<>();

    public ContactsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mRecyclerView = contactsView.findViewById(R.id.contacts_recycler_list);

        getContacts();

        return contactsView;
    }


    private void getContacts() {
        FBUtils.getContactsRef().child(FBUtils.getUserID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactsList.clear();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    String usersIDs = dataSnapshot1.getKey().toString();
                    FBUtils.getUsersRef().child(usersIDs).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Contacts contacts = dataSnapshot.getValue(Contacts.class);

                            contactsList.add(contacts);
                            adapter = new ContactsAdapter(contactsList, "contacts");
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            mRecyclerView.setHasFixedSize(true);
                            adapter.notifyDataSetChanged();
                            mRecyclerView.setAdapter(adapter);
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


