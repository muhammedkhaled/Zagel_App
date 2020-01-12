package com.example.Zagel_App.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.Zagel_App.firebase.controller.FBUtils;
import com.example.Zagel_App.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.Zagel_App.firebase.util.Constance.IMAGE;
import static com.example.Zagel_App.firebase.util.Constance.NAME;
import static com.example.Zagel_App.firebase.util.Constance.STATUS;
import static com.example.Zagel_App.firebase.util.Constance.USER_ID;

public class SettingsActivity extends AppCompatActivity {
    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private StorageReference userProfileImageRef;

    private Uri imagePath;
    private String profileImage;

    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        currentUserID = FBUtils.getUserID();
        userProfileImageRef = FBUtils.getProfileImageStorageRef();

        initializeFields();

//        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });

        retrieveUserInfo();
    }




    private void initializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);

        settingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imagePath = result.getUri();

            final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

            UploadTask uploadTask = filePath.putFile(imagePath);

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        profileImage = task.getResult().toString();

                        FBUtils.getUsersCurrentIDRef().child(IMAGE)
                                .setValue(profileImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Glide.with(SettingsActivity.this).load(profileImage).into(userProfileImage);
                                    Toast.makeText(SettingsActivity.this, "IMAGE saved to database", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this, "Please write your user status...", Toast.LENGTH_SHORT).show();
        } else {
            // to replace with userModel bestPractice
            HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put(USER_ID, currentUserID);
                    profileMap.put(NAME, setUserName);
                    profileMap.put(STATUS, setUserStatus);
//                    profileMap.put("IMAGE", profileImage);

            FBUtils.getUsersCurrentIDRef().updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this
                                        , "profile updated successful", Toast.LENGTH_SHORT).show();
                            }else {
                                String massage = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+massage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void retrieveUserInfo() {
        FBUtils.getUsersCurrentIDRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(NAME)) && (dataSnapshot.hasChild(IMAGE))){

                            String retrieveUserName = dataSnapshot.child(NAME).getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child(STATUS).getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child(IMAGE).getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

//                            Picasso.get().load(retrieveProfileImage)
//                                    .resize(250,250)
//                                    .placeholder(R.drawable.profile_image).into(userProfileImage);

                            Glide.with(SettingsActivity.this).load(retrieveProfileImage)
                                    .centerCrop()
                                    .placeholder(R.drawable.profile_image).into(userProfileImage);
                        }
                        else
                            if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(NAME))){

                            String retrieveUserName = dataSnapshot.child(NAME).getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child(STATUS).getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                        }else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(IMAGE))){

                            String retrieveProfileImage = dataSnapshot.child(IMAGE).getValue().toString();
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
//                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Write your name ", Toast.LENGTH_SHORT).show();

                        }
                        else {
//                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please update your profile information", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        // these flags to prvent user from back to this screen
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
