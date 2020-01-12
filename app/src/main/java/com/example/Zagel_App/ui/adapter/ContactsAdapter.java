package com.example.Zagel_App.ui.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Zagel_App.models.Contacts;
import com.example.Zagel_App.ui.activities.ChatActivity;
import com.example.Zagel_App.firebase.controller.FBUtils;
import com.example.Zagel_App.ui.activities.ProfileActivity;
import com.example.Zagel_App.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.Zagel_App.firebase.util.Constance.DATE;
import static com.example.Zagel_App.firebase.util.Constance.OFFLINE;
import static com.example.Zagel_App.firebase.util.Constance.ONLINE;
import static com.example.Zagel_App.firebase.util.Constance.STATE;
import static com.example.Zagel_App.firebase.util.Constance.TIME;
import static com.example.Zagel_App.firebase.util.Constance.USER_STATE;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private List<Contacts> list = new ArrayList<>();
    private String activityType;

    public ContactsAdapter(List<Contacts> list, String activityType) {
        this.list = list;
        this.activityType = activityType;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_display_layout, parent, false);

        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position) {
        final Contacts contacts = list.get(position);

        final String name = contacts.getName();
        final String status = contacts.getStatus();
        final String uId = contacts.getuId();
        final String image = contacts.getImage();

        holder.userName.setVisibility(View.VISIBLE);
        holder.userStatus.setVisibility(View.VISIBLE);

        holder.userName.setText(name);
        holder.userStatus.setText(status);

        if (image != null) {
            Picasso.get().load(image).placeholder(R.drawable.profile_image)
                    .into(holder.profileImage);

//            Glide.with(holder.itemView.getContext()).load(image).placeholder(R.drawable.profile_image)
//                    .into(holder.profileImage);
        }

        if (activityType.equals("contacts")) {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                    chatIntent.putExtra("visit_User_Id", uId);
                    chatIntent.putExtra("visit_user_name", name);
                    chatIntent.putExtra("visit_user_image", image);
                    holder.itemView.getContext().startActivity(chatIntent);
                }
            });

            FBUtils.getUsersRef().child(uId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child(USER_STATE).hasChild(STATE)) {
                            String state = dataSnapshot.child(USER_STATE).child(STATE)
                                    .getValue().toString();

                            if (state.equals(ONLINE)) {
                                holder.onlineIcon.setVisibility(View.VISIBLE);

                            } else if (state.equals(OFFLINE)) {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                        } else {
                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (activityType.equals("chat")) {

            FBUtils.getUsersRef().child(uId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

//                        holder.userStatus.setText("Last Seen: " + "\n" + "Date " + "Time");
                        if (dataSnapshot.child(USER_STATE).hasChild(STATE)) {

                            String state = dataSnapshot.child(USER_STATE).child(STATE).getValue().toString();
                            String date = dataSnapshot.child(USER_STATE).child(DATE).getValue().toString();
                            String time = dataSnapshot.child(USER_STATE).child(TIME).getValue().toString();

                            if (state.equals(ONLINE)) {
                                holder.userStatus.setText(ONLINE);

                            } else if (state.equals(OFFLINE)) {

                                holder.userStatus.setText("Last Seen: " + date + " " + time);
                            }

                        } else {
                            holder.userStatus.setText(ONLINE);
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_User_Id", uId);
                                chatIntent.putExtra("visit_user_name", name);
                                chatIntent.putExtra("visit_user_image", image);
                                holder.itemView.getContext().startActivity(chatIntent);

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {


                }
            });


        } else {


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String friend_ID = contacts.getuId();

                    Intent profileIntent =
                            new Intent(holder.itemView.getContext(),
                                    ProfileActivity.class);
                    profileIntent.putExtra("visit_user_id", friend_ID);
                    holder.itemView.getContext().startActivity(profileIntent);

                }
            });


        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);


        }
    }

//    public interface OnItemListener{
//        void OnItemClicked(int position);
//    }
}


