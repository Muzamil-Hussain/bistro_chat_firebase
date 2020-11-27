package com.muzamilhussain.i170191_i170228;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class users_chat_list extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private TextView name, email;
    CircleImageView dp,dpSearchBar;
    ImageButton search;
    AutoCompleteTextView searchBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirebaseDatabase database;
    private DatabaseReference myRef,myChatsRef,isChatRef;

    userProfile fetchedUserProfile;

    private TextView name_sp,phone_sp,info_sp,bio_sp,title_sp;
    CircleImageView bottomSheetDp;



    RecyclerView rv;
    List<userProfile> contacts;
    List<String> contactNumbersList;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;




    public void getContactsFromContactsDirectory() {
        myChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final chatListAdapter adapter = new chatListAdapter(contacts, users_chat_list.this);
                RecyclerView.LayoutManager lm = new LinearLayoutManager(users_chat_list.this);
                rv.setLayoutManager(lm);
                rv.setAdapter(adapter);
                //contacts.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        final userProfile userProfileData = data.getValue(userProfile.class);
                        for (int i=0 ;i<contactNumbersList.size();i++) {
                            if (userProfileData.getPhoneNumber().equals(contactNumbersList.get(i))
                                    && (!userProfileData.getId().equals(currentUser.getUid()))) {

                                isChatRef = database.getReference("user_chats");
                                if (userProfileData.getId().compareTo(currentUser.getUid()) >0) {
                                    isChatRef = isChatRef.child(currentUser.getUid() +"+"+ userProfileData.getId());
                                } else if (userProfileData.getId().compareTo(currentUser.getUid())<0) {
                                    isChatRef = isChatRef.child(userProfileData.getId() +"+"+ currentUser.getUid());
                                }

                                isChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            contacts.add(userProfileData);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }
                }
                //userProfile fetchedUserProfile = dataSnapshot.getValue(userProfile.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(users_chat_list.this,
                        "Can not fetch user info",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Read Contacts permission");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {android.Manifest.permission.READ_CONTACTS}
                                    , PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            } else {
                getContactsFromContactsDirectory();
            }
        } else {
            getContactsFromContactsDirectory();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_chat_list);

        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawer);
        search = findViewById(R.id.search_button_ucl);
        dpSearchBar = findViewById(R.id.img_ucl);
        searchBar = findViewById(R.id.search_ac_ucl);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        currentUser = mAuth.getCurrentUser();

        rv = findViewById(R.id.rv_ucl);
        contacts = new ArrayList<>();
        contactNumbersList = new ArrayList<>();


        if (currentUser==null) {
            Intent notLoggedInIntent = new Intent(users_chat_list.this,
                    MainActivity.class);
            startActivity(notLoggedInIntent);
            finish();
        }

        myRef = database.getReference("user_profiles").child(mAuth.getCurrentUser().getUid());
        myChatsRef = database.getReference("user_profiles");


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(users_chat_list.this,
                            "Please Complete Your Profile",
                            Toast.LENGTH_SHORT).show();

                    Intent notCreatedProfile = new Intent(users_chat_list.this,
                            create_profile.class);
                    startActivity(notCreatedProfile);
                    finish();
                }

                myRef.child("isOnline").setValue("true");
                myRef.child("isOnline").onDisconnect().setValue("false");

                fetchedUserProfile = dataSnapshot.getValue(userProfile.class);
                View hView =  navigationView.getHeaderView(0);
                name = hView.findViewById(R.id.username_ui);
                email = hView.findViewById(R.id.email_ui);
                dp = hView.findViewById(R.id.dp_ui);
                String nameText = fetchedUserProfile.getFirstName()
                        + " " +
                        fetchedUserProfile.getLastName();
                name.setText(nameText);
                email.setText(currentUser.getEmail());

                Picasso.get().load(fetchedUserProfile.getProfilePicture()).into(dp);
                Picasso.get().load(fetchedUserProfile.getProfilePicture()).into(dpSearchBar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(users_chat_list.this,
                        "Can not fetch user info",
                        Toast.LENGTH_SHORT).show();
            }
        });


        toolbar.setTitle("BistroChat");
        toolbar.setTitleTextColor(Color.parseColor("#00d664"));
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawer(GravityCompat.START);
                switch (item.getItemId()) {
                    case R.id.logout_btn_drawer:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(users_chat_list.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                        myRef.child("isOnline").setValue("false");
                        Intent logoutSuccessIntent = new Intent (users_chat_list.this,MainActivity.class);
                        startActivity(logoutSuccessIntent);
                        finish();
                        break;
                }
                return true;
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchContactsIntent = new Intent (users_chat_list.this,search_contacts.class);
                startActivity(searchContactsIntent);
            }
        });

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchContactsIntent = new Intent (users_chat_list.this,search_contacts.class);
                startActivity(searchContactsIntent);
            }
        });

        dpSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        users_chat_list.this,
                        R.style.BottomSheetDialogTheme
                );
                final View bottomSheetView = LayoutInflater.from(getApplicationContext())
                        .inflate(
                                R.layout.show_profile,
                                (RelativeLayout)findViewById(R.id.bottom_sheet_profile_sp)
                        );

//                bottomSheetView.findViewById(R.id.name_sp)




                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        fetchedUserProfile = dataSnapshot.getValue(userProfile.class);
                        String nameText = fetchedUserProfile.getFirstName() + " " + fetchedUserProfile.getLastName();

                        String [] splittedDate = fetchedUserProfile.getDob().split("/");
                        int age = 2020-Integer.parseInt(splittedDate[2]);
                        String infoText = fetchedUserProfile.getGender().toUpperCase()
                                + " " +
                                age;

                        String titleText = "MY PROFILE";

                        title_sp = bottomSheetView.findViewById(R.id.title_sp);
                        title_sp.setText(titleText);

                        name_sp = bottomSheetView.findViewById(R.id.name_sp);
                        name_sp.setText(nameText);

                        phone_sp = bottomSheetView.findViewById(R.id.contact_no_sp);
                        phone_sp.setText(fetchedUserProfile.getPhoneNumber());

                        info_sp = bottomSheetView.findViewById(R.id.info_sp);
                        info_sp.setText(infoText);

                        bio_sp = bottomSheetView.findViewById(R.id.bio_sp);
                        bio_sp.setText(fetchedUserProfile.getBio());


                        bottomSheetDp = bottomSheetView.findViewById(R.id.dp_sp);
                        Picasso.get().load(fetchedUserProfile.getProfilePicture()).into(bottomSheetDp);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(users_chat_list.this,
                                "Failed to fetch data",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                bottomSheetView.findViewById(R.id.hide_sp)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bottomSheetDialog.dismiss();
                            }
                        });

                bottomSheetView.findViewById(R.id.edit_sp).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editProfileIntent = new Intent(v.getContext(),create_profile.class);
                        startActivity(editProfileIntent);
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });



        final Cursor contactNumbers = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (contactNumbers.moveToNext())
        {
            //String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = contactNumbers.getString(contactNumbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactNumbersList.add(phoneNumber);
        }
        contactNumbers.close();


        requestContactPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if (currentUser==null) {
            Intent notLoggedInIntent = new Intent(users_chat_list.this,
                    MainActivity.class);
            startActivity(notLoggedInIntent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

}
