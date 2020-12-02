package com.muzamilhussain.i170191_i170228;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter .MyViewHolder> {
    List<userProfile> contacts;
    Context c;


    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference chatReference, userReference;

    public chatListAdapter (List<userProfile> contacts, Context c) {
        this.contacts = contacts;
        this.c = c;
    }

    @NonNull
    @Override
    public chatListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(c).inflate(R.layout.chat_row_received,parent,false);
//        View itemView = LayoutInflater.from(c).inflate(R.layout.contactrow,parent,false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        String nameText = contacts.get(position).getFirstName()
                + " " +
                contacts.get(position).getLastName();




        final String userId = contacts.get(position).getId();


        if (currentUser.getUid().compareTo(userId) > 0) {
            //Log.i("UserIds",userId + "+" + currentUser.getUid());
            chatReference = database.getReference("user_chats").child(userId + "+" + currentUser.getUid());
        } else if (currentUser.getUid().compareTo(userId) < 0) {
            //Log.i("UserIds",currentUser.getUid() + "+" + userId);
            chatReference = database.getReference("user_chats").child(currentUser.getUid() + "+" + userId);
        }


        chatReference.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    message singleMessage = null;
                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        singleMessage = childSnapshot.getValue(message.class);
                    }


                    String latestText = singleMessage.getMessage();


                    if (latestText.contains("--msgimg--")) {

                        String [] breakText = latestText.split("--msgimg--");

                        if (breakText[1].equals("nomsg")) {
                            holder.info.setText("Sent you an image, open to see.");
                        } else {
                            if (breakText[1].length() > 30) {
                                holder.info.setText(breakText[1].substring(0, 30) + "...");
                            }
                            else {
                                holder.info.setText(breakText[1]);
                            }
                        }

                    } else {
                        if (latestText.length() > 30) {
                            holder.info.setText(latestText.substring(0,30) + "...");
                        }
                        else {
                            holder.info.setText(latestText);
                        }
                    }

                    if (!singleMessage.getIsSeen().equals("true")) {
                        holder.info.setTypeface(null, Typeface.BOLD);
                    }
                    else {
                        holder.info.setTypeface(null, Typeface.NORMAL);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        holder.name.setText(nameText);
        Picasso.get().load(contacts.get(position).getProfilePicture()).into(holder.photo);

        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        v.getContext(),
                        R.style.BottomSheetDialogTheme
                );

                final View bottomSheetView = LayoutInflater.from(c.getApplicationContext())
                        .inflate(
                                R.layout.show_profile,
                                null
                        );

                ImageButton hide_sp;
                final TextView title_sp, name_sp,contact_no_sp,info_sp,bio_sp;
                CircleImageView dp_sp;
                LinearLayout edit_sp;

                hide_sp = bottomSheetView.findViewById(R.id.hide_sp);
                title_sp = bottomSheetView.findViewById(R.id.title_sp);
                name_sp = bottomSheetView.findViewById(R.id.name_sp);
                contact_no_sp = bottomSheetView.findViewById(R.id.contact_no_sp);
                info_sp = bottomSheetView.findViewById(R.id.info_sp);
                bio_sp = bottomSheetView.findViewById(R.id.bio_sp);
                dp_sp = bottomSheetView.findViewById(R.id.dp_sp);
                edit_sp = bottomSheetView.findViewById(R.id.edit_sp);

                title_sp.setText("USER PROFILE");

                name_sp.setText(contacts.get(position).getFirstName() + " " + contacts.get(position).getLastName());
                contact_no_sp.setText(contacts.get(position).getPhoneNumber());

                info_sp.setText("Male");

                bio_sp.setText(contacts.get(position).getBio());

                Picasso.get().load(Uri.parse(contacts.get(position).getProfilePicture())).into(dp_sp);

                hide_sp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                edit_sp.setVisibility(View.GONE);


                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        holder.singleChatRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(v.getContext(),chatActivity.class);
                chatIntent.putExtra("USERID", userId);
                c.startActivity(chatIntent);
                ((users_chat_list)v.getContext()).finish();
            }
        });

        holder.singleChatRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        v.getContext(),
                        R.style.BottomSheetDialogTheme
                );

                final View bottomSheetView = LayoutInflater.from(c.getApplicationContext())
                        .inflate(
                                R.layout.delete_conv_view,
                                null
                        );

                ImageButton hide_dcv;
                Button delete_btn_dcv;


                hide_dcv = bottomSheetView.findViewById(R.id.hide_dcv);
                delete_btn_dcv = bottomSheetView.findViewById(R.id.delete_btn_dcv);


                hide_dcv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });


                delete_btn_dcv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        chatReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                bottomSheetDialog.dismiss();
                                contacts.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(v.getContext(),"Chat Deleted",Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();


                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name, info;
        CircleImageView photo;
        LinearLayout singleChatRow;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.chat_contact_name);
            info = itemView.findViewById(R.id.chat_info);
            photo = itemView.findViewById(R.id.chat_photo);
            singleChatRow = itemView.findViewById(R.id.chat_row);
        }
    }
}
