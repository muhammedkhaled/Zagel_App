package com.example.Zagel_App.firebase.controller;

import androidx.annotation.NonNull;

import com.example.Zagel_App.firebase.util.Constance;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBUtils {

    public static void login(String email, String password, final OnCompletedListener onCompleteListener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        onCompleteListener.onCompleted(task.isSuccessful());
                    }
                });
    }

    public static DatabaseReference getRootRef(){
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getUsersRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.USERS);
    }

    public static DatabaseReference getMessagesRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.MESSAGES);
    }

    public static DatabaseReference getUsersCurrentIDRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.USERS).child(getUserID());
    }

    public static DatabaseReference getUsersCurrentIDStateRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.USERS)
                .child(getUserID()).child(Constance.USER_STATE);
    }

    public static DatabaseReference getGroupRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.GROUPS);
    }

    public static DatabaseReference getChatRequestRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.CHAT_REQUESTS);
    }

    public static DatabaseReference getNotificationsRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.NOTIFICATIONS);
    }

    public static DatabaseReference getContactsRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.CONTACTS);
    }

    public static DatabaseReference getUserDeviceTokenRef(){
        return FirebaseDatabase.getInstance().getReference().child(Constance.USERS)
                .child(getUserID()).child(Constance.DEVICE_TOKEN);
    }

    public static StorageReference getStorageRef(){
        return FirebaseStorage.getInstance().getReference();
    }

    public static StorageReference getProfileImageStorageRef(){
        return FirebaseStorage.getInstance().getReference().child(Constance.PROFILE_IMAGES);
    }


    public static String getUserID() {
        return FirebaseAuth.getInstance().getUid();
    }

    public interface OnCompletedListener {
        void onCompleted(boolean isCompletedSuccessfully);
    }
}
