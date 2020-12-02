package com.muzamilhussain.i170191_i170228;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class search_contacts extends AppCompatActivity {

    RecyclerView rv;
    List<userProfile> contacts, filteredContacts;
    List <String> contactNumbersList;
    ImageButton backButton;
    EditText search_ac_asc;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    searchContactAdapter adapter;
    RecyclerView.LayoutManager lm;


    public void getContactsFromContactsDirectory() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //contacts.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    userProfile userProfileData = data.getValue(userProfile.class);
                    for (int i=0 ;i<contactNumbersList.size();i++) {
                        if (userProfileData.getPhoneNumber().equals(contactNumbersList.get(i))
                        && (!userProfileData.getId().equals(currentUser.getUid()))) {
                            contactNumbersList.remove(i);
                            contacts.add(userProfileData);
                        }
                    }
                }

                adapter = new searchContactAdapter(contacts, search_contacts.this);
                lm = new LinearLayoutManager(search_contacts.this);
                rv.setLayoutManager(lm);
                rv.setAdapter(adapter);



//                searchContactAdapter adapter = new searchContactAdapter(contacts, search_contacts.this);
//                RecyclerView.LayoutManager lm = new LinearLayoutManager(search_contacts.this);
//                rv.setLayoutManager(lm);
//                rv.setAdapter(adapter);
                //userProfile fetchedUserProfile = dataSnapshot.getValue(userProfile.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(search_contacts.this,
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


    public void filterContacts (String currentText) {

        filteredContacts.clear();
        for (userProfile singleContact: contacts) {
            String fullName = singleContact.getFirstName() + " " + singleContact.getLastName();
            if (currentText.length()>0 && fullName.contains(currentText)) {
                filteredContacts.add(singleContact);
            }
        }

        if (currentText.length()>0) {
            adapter = new searchContactAdapter(filteredContacts, search_contacts.this);
            lm = new LinearLayoutManager(search_contacts.this);
            rv.setLayoutManager(lm);
            rv.setAdapter(adapter);
        }
        else {
            adapter = new searchContactAdapter(contacts, search_contacts.this);
            lm = new LinearLayoutManager(search_contacts.this);
            rv.setLayoutManager(lm);
            rv.setAdapter(adapter);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contacts);

        search_ac_asc = findViewById(R.id.search_ac_asc);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        currentUser = mAuth.getCurrentUser();

        if (currentUser==null) {
            Intent notLoggedInIntent = new Intent(search_contacts.this,
                    MainActivity.class);
            startActivity(notLoggedInIntent);
            finish();
        }

        myRef = database.getReference("user_profiles");


        myRef.child(currentUser.getUid()).child("isOnline").setValue("true");
        myRef.child(currentUser.getUid()).child("isOnline").onDisconnect().setValue("false");

        backButton = findViewById(R.id.back_button_asc);
        rv = findViewById(R.id.rv_asc);
        contacts = new ArrayList<>();
        filteredContacts = new ArrayList<>();
        contactNumbersList = new ArrayList<>();

        final Cursor contactNumbers = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        assert contactNumbers != null;
        while (contactNumbers.moveToNext())
        {
            //String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = contactNumbers.getString(contactNumbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            contactNumbersList.add(phoneNumber);
        }
        contactNumbers.close();

        requestContactPermission();

        search_ac_asc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterContacts(s.toString());
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userChatListIntent = new Intent(search_contacts.this, users_chat_list.class);
                startActivity(userChatListIntent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backBtnIntent = new Intent(search_contacts.this,users_chat_list.class);
        startActivity(backBtnIntent);
        finish();
    }
}
