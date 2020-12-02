package com.muzamilhussain.i170191_i170228;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatActivity extends AppCompatActivity {

    ImageButton back_btn_ac, select_img_ac, send_msg_ac;
    TextView name_ac, online_status_ac;
    CircleImageView dp_ac;
    LinearLayout is_online_ac;
    RecyclerView messageRecyclerView;
    EditText msg_text_ac;

    ImageView is_online_inner_ac;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myDbRef, currentUserReference, chatReference;
    FirebaseUser currentUser;
    StorageReference mStorageRef;
    String msgText;

    List<message> chatMessages;
    List<message> tempChatMessages;

    message singleMessage;

    chatAdapter adapter;
    RecyclerView.LayoutManager lm;

    Uri imageUri;

    String userId;


    boolean containsMsg = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        back_btn_ac = findViewById(R.id.back_btn_ac);
        select_img_ac = findViewById(R.id.select_image_ac);
        send_msg_ac = findViewById(R.id.send_msg_ac);
        name_ac = findViewById(R.id.name_ac);
        online_status_ac = findViewById(R.id.online_status_ac);
        dp_ac = findViewById(R.id.dp_ac);
        is_online_ac = findViewById(R.id.is_online_ac);
        messageRecyclerView = findViewById(R.id.msg_rv);
        msg_text_ac = findViewById(R.id.msg_text_ac);
        is_online_inner_ac = findViewById(R.id.is_online_inner_ac);

        chatMessages = new ArrayList<>();


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();


        currentUserReference = database.getReference("user_profiles").child(currentUser.getUid());


        currentUserReference.child("isOnline").setValue("true");
        currentUserReference.child("isOnline").onDisconnect().setValue("false");

        userId = getIntent().getStringExtra("USERID");

        myDbRef = database.getReference("user_profiles").child(userId);


        if (currentUser.getUid().compareTo(userId) > 0) {
            chatReference = database.getReference("user_chats").child(userId + "+" + currentUser.getUid());
            mStorageRef = FirebaseStorage.getInstance().getReference("user_chats").child(userId + "+" + currentUser.getUid());
        } else if (currentUser.getUid().compareTo(userId) < 0) {
            chatReference = database.getReference("user_chats").child(currentUser.getUid() + "+" + userId);
            mStorageRef = FirebaseStorage.getInstance().getReference("user_chats").child(currentUser.getUid() + "+" + userId);
        }


        adapter = new chatAdapter(chatMessages, chatActivity.this);
        lm = new LinearLayoutManager(chatActivity.this);
        ((LinearLayoutManager) lm).setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(lm);
        messageRecyclerView.setAdapter(adapter);


        myDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userProfile userReceiver = dataSnapshot.getValue(userProfile.class);

                String nameText = userReceiver.getFirstName() + " " + userReceiver.getLastName();
                String isUserOnline = userReceiver.getIsOnline();

                Picasso.get().load(userReceiver.getProfilePicture()).into(dp_ac);
                name_ac.setText(nameText);

                if (isUserOnline.equals("true")) {
                    online_status_ac.setText("is online");
                    is_online_ac.setBackgroundResource(R.drawable.circle_white);
                    is_online_inner_ac.setBackgroundResource(R.drawable.circle_green);

                } else {
                    online_status_ac.setText("is offline");
                    is_online_ac.setBackgroundResource(0);
                    is_online_inner_ac.setBackgroundResource(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    chatMessages.clear();
                    List<String> chatMessagesKeys = new ArrayList<>();

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        chatMessages.add(data.getValue(message.class));
                        chatMessages.get(chatMessages.size()-1).setId(data.getKey());
                        chatMessagesKeys.add(data.getKey());
                    }

                    String latestMessageKey = chatMessagesKeys.get(chatMessagesKeys.size() - 1);
                    message latestMessage = chatMessages.get(chatMessages.size() - 1);

                    if (!latestMessage.getSenderId().equals(currentUser.getUid())) {
                        chatReference.child(latestMessageKey).child("isSeen").setValue("true");
                        chatMessages.get(chatMessages.size() - 1).setIsSeen("true");
                    }


                    if (dataSnapshot.getChildrenCount() > 1) {
                        final String secondLastMessageKey = chatMessagesKeys.get(chatMessagesKeys.size() - 2);
                        if (latestMessage.getSenderId().equals(chatMessages.get(chatMessages.size() - 2).getSenderId())) {
                            chatReference.child(secondLastMessageKey).child("isLast").setValue("false");
                            chatMessages.get(chatMessages.size() - 2).setIsLast("false");
                        }
                    }

                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    //chatMessages.clear();
//                    String latestKey = null;
////                    for (DataSnapshot data : dataSnapshot.getChildren()) {
////                        chatMessages.add(data.getValue(message.class));
////                        latestKey = data.getKey();
////                    }
//                    if (!chatMessages.get(chatMessages.size()-1).getSenderId().equals(currentUser.getUid())) {
//
//                        for (int i=0;i<chatMessages.size();i++) {
//                            if (!chatMessages.get(i).getSenderId().equals(currentUser.getUid())) {
//                                chatMessages.get(i).setIsSeen("true");
//                            }
//                        }
//                        chatReference
//                                .child(latestKey)
//                                .child("isSeen").
//                                setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                chatMessages.get(chatMessages.size() - 1).setIsSeen("true");
//                            }
//                        });
//                    }
//                    chatAdapter adapter = new chatAdapter(chatMessages, chatActivity.this);
//                    RecyclerView.LayoutManager lm = new LinearLayoutManager(chatActivity.this);
//                    ((LinearLayoutManager) lm).setStackFromEnd(true);
//                    messageRecyclerView.setLayoutManager(lm);
//                    messageRecyclerView.setAdapter(adapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


//        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//
//                    for (DataSnapshot data : dataSnapshot.getChildren()) {
//                        chatMessages.add(data.getValue(message.class));
//                        chatMessages.get(chatMessages.size()-1).setId(data.getKey());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(chatActivity.this,
//                        "Failed to retrieve messages",
//                        Toast.LENGTH_LONG).show();
//            }
//        });


        back_btn_ac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backBtnIntent = new Intent (chatActivity.this, users_chat_list.class);
                startActivity(backBtnIntent);
                finish();
            }
        });


        send_msg_ac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgText = msg_text_ac.getText().toString();

                if (!msgText.isEmpty()) {
                    msg_text_ac.getText().clear();
                    singleMessage = new message(currentUser.getUid(),
                            userId,
                            msgText);
                    //chatReference.push().setValue(singleMessage);
                    chatReference.push().setValue(singleMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(chatActivity.this,
                                            e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });


        select_img_ac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            if (imageUri != null) {
                Intent sendImageMessageIntent = new Intent(chatActivity.this, chat_msg_with_img.class);
                sendImageMessageIntent.putExtra("IMAGEURI", imageUri.toString());
                startActivityForResult(sendImageMessageIntent, 200);
            }
        }

        else {

            if (data!=null) {
                final String imgMsg = data.getStringExtra("RESULTANTMSG");


                String uniqueID = UUID.randomUUID().toString();

                final String[] msgContent = imgMsg.split("--msgimg--");

                if (!msgContent[1].equals("nomsg")) {
                    containsMsg = true;
                }

                mStorageRef.child(uniqueID).putFile(Uri.parse(msgContent[0]))
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //String dp = uri.toString();
                                        imageUri = uri;

                                        String finalMsg;
                                        if (containsMsg) {
                                            finalMsg = imageUri.toString() +"--msgimg--"+ msgContent[1];
                                        } else {
                                            finalMsg = imageUri.toString() +"--msgimg--"+ "nomsg";
                                        }

                                        singleMessage = new message(currentUser.getUid(),
                                                userId,
                                                finalMsg);
                                        //chatReference.push().setValue(singleMessage);
                                        chatReference.push().setValue(singleMessage);
                                    }
                                });
                            }
                        });
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backBtnIntent = new Intent(chatActivity.this, users_chat_list.class);
        startActivity(backBtnIntent);
        finish();
    }
}
