package com.example.Zagel_App.ui.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Zagel_App.firebase.controller.FBUtils;
import com.example.Zagel_App.ui.activities.ImageViewActivity;
import com.example.Zagel_App.models.Messages;
import com.example.Zagel_App.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.Zagel_App.firebase.util.Constance.IMAGE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;

    public MessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messageSenderID = FBUtils.getUserID();
        Messages messages = userMessagesList.get(position);

        String fromUsersID = messages.getFrom();
        String fromMessageType = messages.getType();

        FBUtils.getUsersRef().child(fromUsersID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(IMAGE)) {

                    String receiverImage = dataSnapshot.child(IMAGE).getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).
                            into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.messageReceiverFile.setVisibility(View.GONE);
        holder.messageSenderFile.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {

            if (fromUsersID.equals(messageSenderID)) {

                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage() + "\n \n"
                        + messages.getTime() + "-" + messages.getDate());

            } else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);


                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiverMessageText.setText(messages.getMessage() + "\n \n"
                        + messages.getTime() + "-" + messages.getDate());

            }
        } else if (fromMessageType.equals("image")) {

            if (fromUsersID.equals(messageSenderID)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).fit().into(holder.messageSenderPicture);

            } else {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).fit().into(holder.messageReceiverPicture);
            }

        } else if (fromMessageType.equals("pdf") || (fromMessageType.equals("docx"))) {

            if (fromUsersID.equals(messageSenderID)) {

                holder.messageSenderFile.setVisibility(View.VISIBLE);
                holder.messageSenderFile.setBackgroundResource(R.drawable.file_icon);

            } else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageSenderFile.setVisibility(View.VISIBLE);
                holder.messageReceiverFile.setVisibility(View.VISIBLE);

                holder.messageReceiverFile.setBackgroundResource(R.drawable.file_icon);
            }

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final int i = holder.getAdapterPosition();
                if (i != RecyclerView.NO_POSITION && userMessagesList.get(i).getType().equals("pdf") ||
                        userMessagesList.get(i).getType().equals("docx")) {

                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(userMessagesList.get(i).getMessage()));
                    holder.itemView.getContext().startActivity(intent);



                } else if (userMessagesList.get(i).getType().equals("image")) {


                    Intent intentImage = new Intent(holder.itemView.getContext(),
                            ImageViewActivity.class);

                    intentImage.putExtra("url",
                            userMessagesList.get(i).getMessage());
                    holder.itemView.getContext().startActivity(intentImage);


                }
            }
        });


        //TODO we must do this in View holder best practice not on bind view
        if (fromUsersID.equals(messageSenderID)) {
            // onLongClick to prefent fast click on item after deleted and thats crash the app
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int i = holder.getAdapterPosition();
                    if (i != RecyclerView.NO_POSITION && userMessagesList.get(i).getType().equals("pdf") ||
                            userMessagesList.get(i).getType().equals("docx")) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download and View This Document",
                                "Cancel",
                                "Delete For Everyone"
                        };

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:

                                        deleteSentMessage(i, holder);
                                        break;
                                    case 1:

                                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(userMessagesList.get(i).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                        break;
                                    case 3:

                                        deleteMessageForEveryOne(i, holder);
                                        break;
                                }
                            }
                        });

                        builder.show();

                    }

                    else if (userMessagesList.get(i).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Cancel",
                                "Delete For Everyone"
                        };

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:

                                        deleteSentMessage(i, holder);
                                        break;
                                    case 2:

                                        deleteMessageForEveryOne(i, holder);
                                        break;
                                }
                            }
                        });

                        builder.show();

                    }

                    else if (userMessagesList.get(i).getType().equals("image")) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "View This Image",
                                "Cancel",
                                "Delete For Everyone"
                        };

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:

                                        deleteSentMessage(i, holder);
                                        break;
                                    case 1:

                                        Intent intentImage = new Intent(holder.itemView.getContext(),
                                                ImageViewActivity.class);

                                        intentImage.putExtra("url",
                                                userMessagesList.get(i).getMessage());
                                        holder.itemView.getContext().startActivity(intentImage);
                                        break;
                                    case 3:

                                        deleteMessageForEveryOne(i, holder);
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }

                    return true;
                }
            });
        } else {





            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int i = holder.getAdapterPosition();
                    if (i != RecyclerView.NO_POSITION &&
                            userMessagesList.get(i).getType().equals("pdf") ||
                            userMessagesList.get(i).getType().equals("docx")) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download and View This Document",
                                "Cancel",
                        };

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:

                                        deleteReceiveMessage(i, holder);
                                        break;
                                    case 1:

                                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(userMessagesList.get(i).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                        break;
                                }
                            }
                        });

                        builder.show();

                    } else if (userMessagesList.get(i).getType().equals("text")) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Cancel",
                        };

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:

                                        deleteReceiveMessage(i, holder);
                                        break;
                                }
                            }
                        });

                        builder.show();

                    } else if (userMessagesList.get(i).getType().equals("image")) {

                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "View This Image",
                                "Cancel",
                        };

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:

                                        deleteReceiveMessage(i, holder);
                                        break;
                                    case 1:

                                        Intent intentImage = new Intent(holder.itemView.getContext(),
                                                ImageViewActivity.class);

                                        intentImage.putExtra("url",
                                                userMessagesList.get(i).getMessage());
                                        holder.itemView.getContext().startActivity(intentImage);

                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                    return true;

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        FBUtils.getMessagesRef()
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(holder.itemView.getContext(),
                            "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                } else {


                    Toast.makeText(holder.itemView.getContext(),
                            "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }


    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {
        FBUtils.getMessagesRef()
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(holder.itemView.getContext(),
                            "Deleted Successfully", Toast.LENGTH_SHORT).show();

                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                } else {


                    Toast.makeText(holder.itemView.getContext(),
                            "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void deleteMessageForEveryOne(final int position, final MessageViewHolder holder) {
        FBUtils.getMessagesRef()
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FBUtils.getMessagesRef()
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getMessageID())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(holder.itemView.getContext(),
                                                        "Deleted Successfully", Toast.LENGTH_SHORT).show();

                                                userMessagesList.remove(position);
                                                notifyItemRemoved(position);
                                            }
                                        }
                                    });

                        } else {

                            Toast.makeText(holder.itemView.getContext()
                                    , "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView senderMessageText, receiverMessageText;
        private CircleImageView receiverProfileImage;
        private ImageView messageSenderPicture, messageReceiverPicture, messageSenderFile,
                messageReceiverFile;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverFile = itemView.findViewById(R.id.message_receiver_file_view);
            messageSenderFile = itemView.findViewById(R.id.message_sender_file_view);

        }
    }
}
