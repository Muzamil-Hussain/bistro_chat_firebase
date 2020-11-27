package com.muzamilhussain.i170191_i170228;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class create_profile extends AppCompatActivity {

    EditText firstName, lastName, phone, bio, dob;
    Button male, female, none, save;
    CircleImageView profileImage;
    RelativeLayout selectImage;
    Uri imageUri, checkUri;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference myDBRef;
    private StorageReference mStorageRef;
    private String gender;

    private boolean profileExists = false;
    private boolean isPictureUploaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);


        firstName = findViewById(R.id.first_name_et);
        lastName = findViewById(R.id.last_name_et);
        phone = findViewById(R.id.phone_no_et);
        bio = findViewById(R.id.bio_et);
        dob = findViewById(R.id.dob_et);
        selectImage = findViewById(R.id.select_img_acp);
        profileImage = findViewById(R.id.profile_img_acp);

        male = findViewById(R.id.male_button);
        female = findViewById(R.id.female_button);
        none = findViewById(R.id.none_button);
        save = findViewById(R.id.save_create_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myDBRef = database.getReference("user_profiles");

        currentUser = mAuth.getCurrentUser();

        checkUri = Uri.parse("dummy uri");


        if (currentUser !=null) {
            myDBRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        myDBRef.child(currentUser.getUid()).child("isOnline").setValue("true");
                        myDBRef.child(currentUser.getUid()).child("isOnline").onDisconnect().setValue("false");
                        profileExists = true;
                        userProfile fetchedUserProfile = dataSnapshot.getValue(userProfile.class);
                        firstName.setText(fetchedUserProfile.getFirstName());
                        lastName.setText(fetchedUserProfile.getLastName());
                        phone.setText(fetchedUserProfile.getPhoneNumber());
                        dob.setText(fetchedUserProfile.getDob());
                        bio.setText(fetchedUserProfile.getBio());

                        imageUri = Uri.parse(fetchedUserProfile.getProfilePicture());
                        checkUri = imageUri;
                                Picasso.get().load(fetchedUserProfile.getProfilePicture()).into(profileImage);

                        if (fetchedUserProfile.getGender().equals("male")) {
                            gender = "male";
                            male.setBackgroundResource(R.drawable.gender_rectangle_button_selected);
                            male.setTextColor(Color.parseColor("#ffffff"));
                        }
                        else if (fetchedUserProfile.getGender().equals("female")) {
                            gender= "female";
                            female.setBackgroundResource(R.drawable.gender_rectangle_button_selected);
                            female.setTextColor(Color.parseColor("#ffffff"));
                        }
                        else {
                            gender = "none";
                            none.setBackgroundResource(R.drawable.gender_rectangle_button_selected);
                            none.setTextColor(Color.parseColor("#ffffff"));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(create_profile.this,
                            "Failed to retrieve data",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), 1);
            }
        });

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "male";
                male.setBackgroundResource(R.drawable.gender_rectangle_button_selected);
                male.setTextColor(Color.parseColor("#ffffff"));

                female.setBackgroundResource(R.drawable.gender_rectangle_button);
                female.setTextColor(Color.parseColor("#00d664"));
                none.setBackgroundResource(R.drawable.gender_rectangle_button);
                none.setTextColor(Color.parseColor("#00d664"));
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "female";
                female.setBackgroundResource(R.drawable.gender_rectangle_button_selected);
                female.setTextColor(Color.parseColor("#ffffff"));

                male.setBackgroundResource(R.drawable.gender_rectangle_button);
                male.setTextColor(Color.parseColor("#00d664"));
                none.setBackgroundResource(R.drawable.gender_rectangle_button);
                none.setTextColor(Color.parseColor("#00d664"));
            }
        });
        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "none";
                none.setBackgroundResource(R.drawable.gender_rectangle_button_selected);
                none.setTextColor(Color.parseColor("#ffffff"));

                female.setBackgroundResource(R.drawable.gender_rectangle_button);
                female.setTextColor(Color.parseColor("#00d664"));

                male.setBackgroundResource(R.drawable.gender_rectangle_button);
                male.setTextColor(Color.parseColor("#00d664"));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (imageUri != null) {

//                    if (!profileExists) {
//                        mStorageRef = FirebaseStorage.getInstance().getReference("user_profiles");
//                        mStorageRef = mStorageRef.child(currentUser.getUid()).child("/ProfilePicture/dp");
//                        mStorageRef.putFile(imageUri)
//                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                        Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
//                                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                            @Override
//                                            public void onSuccess(Uri uri) {
//                                                //String dp = uri.toString();
//                                                imageUri = uri;
//
//
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Toast.makeText(create_profile.this,
//                                                        "Failed to upload profile image",
//                                                        Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(create_profile.this,
//                                                "Failed to upload profile image",
//                                                Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }


                if (isPictureUploaded || (imageUri.toString().equals(checkUri.toString()))) {
                    Toast.makeText(create_profile.this,
                            imageUri.toString(),
                            Toast.LENGTH_LONG).show();
                    userProfile newUserProfile = new userProfile(firstName.getText().toString().trim(),
                            lastName.getText().toString().trim(),
                            dob.getText().toString(),
                            gender, phone.getText().toString().trim(),
                            bio.getText().toString(),
                            imageUri.toString(),
                            currentUser.getUid());


                    myDBRef.child(currentUser.getUid()).setValue(newUserProfile)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(create_profile.this,
                                            "User Info Updated",
                                            Toast.LENGTH_SHORT).show();

                                    Intent usersChatListIntent = new Intent(create_profile.this,
                                            users_chat_list.class);
                                    startActivity(usersChatListIntent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(create_profile.this,
                                            e.toString(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }

//                }
//                else {
//                    Toast.makeText(create_profile.this,
//                            "Please Select Profile Image",
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent loginScreenIntent = new Intent(create_profile.this, signin.class);
            startActivity(loginScreenIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            if (imageUri != null) {
                profileImage.setImageURI(imageUri);

                    mStorageRef = FirebaseStorage.getInstance().getReference("user_profiles");
                    mStorageRef = mStorageRef.child(currentUser.getUid()).child("/ProfilePicture/dp");
                    mStorageRef.putFile(imageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //String dp = uri.toString();
                                            imageUri = uri;
                                            isPictureUploaded =true;


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(create_profile.this,
                                                    "Failed to upload profile image",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(create_profile.this,
                                            "Failed to upload profile image",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

            }
            else {
                Toast.makeText(create_profile.this,
                        "Please Select Profile Image",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
