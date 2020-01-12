package com.example.Zagel_App.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Zagel_App.models.Messages;
import com.example.Zagel_App.ui.adapter.MessageAdapter;
import com.example.Zagel_App.firebase.controller.FBUtils;
import com.example.Zagel_App.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.Zagel_App.firebase.util.Constance.DATE;
import static com.example.Zagel_App.firebase.util.Constance.DOCUMENT_FILES;
import static com.example.Zagel_App.firebase.util.Constance.FROM;
import static com.example.Zagel_App.firebase.util.Constance.MESSAGE;
import static com.example.Zagel_App.firebase.util.Constance.MESSAGE_ID;
import static com.example.Zagel_App.firebase.util.Constance.NAME;
import static com.example.Zagel_App.firebase.util.Constance.OFFLINE;
import static com.example.Zagel_App.firebase.util.Constance.ONLINE;
import static com.example.Zagel_App.firebase.util.Constance.STATE;
import static com.example.Zagel_App.firebase.util.Constance.TIME;
import static com.example.Zagel_App.firebase.util.Constance.TO;
import static com.example.Zagel_App.firebase.util.Constance.TYPE;
import static com.example.Zagel_App.firebase.util.Constance.USER_STATE;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;

    private ImageButton sendMessageButton, sendFilesButton;
    private EditText messageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageSenderID = FBUtils.getUserID();

        messageReceiverID = getIntent().getExtras().getString("visit_User_Id");
        messageReceiverName = getIntent().getExtras().getString("visit_user_name");
        messageReceiverImage = getIntent().getExtras().getString("visit_user_image");

        initializeControls();


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).
                placeholder(R.drawable.profile_image).into(userImage);
//
//        Glide.with(this).load(messageReceiverImage).
//                placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        displayLastSeen();


        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Image",
                        "PDF Files",
                        "Ms Word Files"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                checker = "image";

                                Intent intentImage = new Intent();
                                intentImage.setAction(Intent.ACTION_GET_CONTENT);
                                intentImage.setType("image/*");
                                startActivityForResult(intentImage.createChooser(intentImage, "Select Image"), 438);

                                break;
                            case 1:

                                Intent intentPdf = new Intent();
                                intentPdf.setAction(Intent.ACTION_GET_CONTENT);
                                intentPdf.setType("application/pdf");
                                startActivityForResult(intentPdf.createChooser(intentPdf, "Select PDF File"), 438);

                                checker = "pdf";
                                break;
                            case 2:
                                Intent intentMsWord = new Intent();
                                intentMsWord.setAction(Intent.ACTION_GET_CONTENT);
                                intentMsWord.setType("application/msword");
                                startActivityForResult(intentMsWord.createChooser(intentMsWord, "Select Ms Word File"), 438);
                                checker = "docx";
                                break;
                            default:
                                // code block
                        }


                    }
                });

                builder.show();
            }
        });

        displayMessages();
    }

    private void initializeControls() {

        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        sendMessageButton = findViewById(R.id.send_private_message_button);
        sendFilesButton = findViewById(R.id.send_files_button);
        messageInputText = findViewById(R.id.input_private_message);
        userMessagesList = findViewById(R.id.private_messages_list_of_users);

        loadingBar = new ProgressDialog(this);

        messageAdapter = new MessageAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //TODO make loading bare visible
            loadingBar.setTitle("Uploading");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")) {


                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = FBUtils.getMessagesRef()
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = FBUtils.getStorageRef().child(DOCUMENT_FILES).child(messagePushID + "." + checker);

                uploadTask = filePath.putFile(fileUri);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double p = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        loadingBar.setMessage((int) p + " % Uploading....");

                    }
                }).continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (task.isSuccessful()) {

                            return filePath.getDownloadUrl();
                        } else {
                            throw task.getException();
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String myFileUrl = downloadUri.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put(MESSAGE, myFileUrl);
                            messageImageBody.put(NAME, fileUri.getLastPathSegment());
                            messageImageBody.put(TYPE, checker);
                            messageImageBody.put(FROM, messageSenderID);
                            messageImageBody.put(TO, messageReceiverID);
                            messageImageBody.put(MESSAGE_ID, messagePushID);
                            messageImageBody.put(TIME, saveCurrentTime);
                            messageImageBody.put(DATE, saveCurrentDate);

                            // or replace mapMessageTextBody with model like this Messages messages = new Messages(messageSenderID, messageText,"text");

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            FBUtils.getRootRef().updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else if (checker.equals("image")) {

                StorageReference storageReference = FBUtils.getStorageRef().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = FBUtils.getMessagesRef()
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (task.isSuccessful()) {

                            return filePath.getDownloadUrl();
                        } else {
                            throw task.getException();
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put(MESSAGE, myUrl);
                            messageImageBody.put(NAME, fileUri.getLastPathSegment());
                            messageImageBody.put(TYPE, checker);
                            messageImageBody.put(FROM, messageSenderID);
                            messageImageBody.put(TO, messageReceiverID);
                            messageImageBody.put(MESSAGE_ID, messagePushID);
                            messageImageBody.put(TIME, saveCurrentTime);
                            messageImageBody.put(DATE, saveCurrentDate);

                            // or replace mapMessageTextBody with model like this Messages messages = new Messages(messageSenderID, messageText,"text");

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            FBUtils.getRootRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()) {
                                        //TODO make loading bar invisible
                                        Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }

                                    messageInputText.setText("");
                                }
                            });

                            loadingBar.dismiss();
                        }
                    }
                });


            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error:", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void displayLastSeen() {
        FBUtils.getUsersRef().child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(USER_STATE).hasChild(STATE)) {

                    String state = dataSnapshot.child(USER_STATE).child(STATE).getValue().toString();
                    String date = dataSnapshot.child(USER_STATE).child(DATE).getValue().toString();
                    String time = dataSnapshot.child(USER_STATE).child(TIME).getValue().toString();

                    if (state.equals(ONLINE)) {
                        userLastSeen.setText(ONLINE);

                    } else if (state.equals(OFFLINE)) {
                        userLastSeen.setText("Last Seen: " + date + " " + time);

                    }

                } else {
                    userLastSeen.setText(OFFLINE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void displayMessages() {

        FBUtils.getMessagesRef().child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {
        String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "first write your message", Toast.LENGTH_SHORT).show();
        } else {

            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = FBUtils.getMessagesRef()
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put(MESSAGE, messageText);
            messageTextBody.put(TYPE, "text");
            messageTextBody.put(FROM, messageSenderID);
            messageTextBody.put(TO, messageReceiverID);
            messageTextBody.put(MESSAGE_ID, messagePushID);
            messageTextBody.put(TIME, saveCurrentTime);
            messageTextBody.put(DATE, saveCurrentDate);

            // or replace mapMessageTextBody with model like this Messages messages = new Messages(messageSenderID, messageText,"text");

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            FBUtils.getRootRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (!task.isSuccessful()) {

                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    messageInputText.setText("");
                }
            });
        }
    }
}
