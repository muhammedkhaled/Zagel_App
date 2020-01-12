package com.example.Zagel_App.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.Zagel_App.firebase.controller.FBUtils;
import com.example.Zagel_App.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.Zagel_App.firebase.util.Constance.CONTACTS;
import static com.example.Zagel_App.firebase.util.Constance.IMAGE;
import static com.example.Zagel_App.firebase.util.Constance.NAME;
import static com.example.Zagel_App.firebase.util.Constance.STATUS;

public class ProfileActivity extends AppCompatActivity {

    private String receiveUserID, senderUserID, current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
//    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        senderUserID = FBUtils.getUserID();
        receiveUserID = getIntent().getExtras()
                .get("visit_user_id").toString();

        userRef = FBUtils.getUsersRef();
        chatRequestRef = FBUtils.getChatRequestRef();
        contactsRef = FBUtils.getContactsRef();
        notificationRef = FBUtils.getNotificationsRef();


        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);

        current_State = "new";


        if (!senderUserID.equals(receiveUserID)) {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);

                    if (current_State.equals("new")) {
                        sendChatRequest();
                    }

                    if (current_State.equals("request_send")) {

                        cancelChatRequest();
                    }

                    if (current_State.equals("request_received")) {

                        acceptChatRequest();
                    }
                    if (current_State.equals("friends")) {

                        removeContact();
                    }

                }
            });

        } else {

            sendMessageRequestButton.setVisibility((View.INVISIBLE));
        }

        retrieveUserInfo();


    }


    private void retrieveUserInfo() {
        userRef.child(receiveUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(IMAGE))) {

                    String userImage = dataSnapshot.child(IMAGE).getValue().toString();
                    String userName = dataSnapshot.child(NAME).getValue().toString();
                    String userStatus = dataSnapshot.child(STATUS).getValue().toString();

                    Picasso.get().load(userImage)
                            .placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();

                } else {
                    String userName = dataSnapshot.child(NAME).getValue().toString();
                    String userStatus = dataSnapshot.child(STATUS).getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {
        // i did (onChildEventListener instead of onValueEventListener) to notify the receiver of cancelRequest

        chatRequestRef.child(senderUserID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {

                    chatRequestRef.child(senderUserID)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiveUserID)) {

                                        String request_type = dataSnapshot.child(receiveUserID).child("request_type").getValue().toString();

                                        if (request_type.equals("sent")) {

                                            current_State = "request_sent";
                                            sendMessageRequestButton.setText("Cancel Request");
                                        } else if (request_type.equals("received")) {
                                            current_State = "request_received";
                                            sendMessageRequestButton.setText("Accept Request");

                                            declineMessageRequestButton.setVisibility(View.VISIBLE);
                                            declineMessageRequestButton.setEnabled(true);
                                            declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    cancelChatRequest();
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (current_State != "friends"){

                    //TODO fined how to know which action did (cancelChatRequest or acceptChatRequest)
                    sendMessageRequestButton.setEnabled(true);
                    current_State = "new";
                    sendMessageRequestButton.setText("Send Request");

                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                    declineMessageRequestButton.setEnabled(false);

                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        contactsRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiveUserID)) {

                            current_State = "friends";
                            sendMessageRequestButton.setText("Remove this Contact");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void removeContact() {

        contactsRef.child(senderUserID).child(receiveUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            contactsRef.child(receiveUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                sendMessageRequestButton.setEnabled(true);
                                                current_State = "new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserID).child(receiveUserID)
                .child(CONTACTS).setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            contactsRef.child(receiveUserID).child(senderUserID)
                                    .child(CONTACTS).setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                removeRequestsAfterFriendAccept();

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void removeRequestsAfterFriendAccept() {

        // after accept we must delete Requests from chatRequests
        chatRequestRef.child(senderUserID).child(receiveUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiveUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            sendMessageRequestButton.setEnabled(true);
                                            current_State = "friends";
                                            sendMessageRequestButton.setText("Remove this contact");

                                            declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            declineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }


    private void cancelChatRequest() {

        chatRequestRef.child(senderUserID).child(receiveUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiveUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                sendMessageRequestButton.setEnabled(true);
                                                current_State = "new";
                                                sendMessageRequestButton.setText("Send Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void sendChatRequest() {

        chatRequestRef.child(senderUserID).child(receiveUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiveUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                notificationRef.child(receiveUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_State = "request_send";
                                                                    sendMessageRequestButton.setText("Cancel Request");
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });

    }
}
