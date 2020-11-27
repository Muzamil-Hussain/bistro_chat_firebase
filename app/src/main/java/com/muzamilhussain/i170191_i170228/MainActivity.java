package com.muzamilhussain.i170191_i170228;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button signIn, signUp;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();


        signIn= findViewById(R.id.sign_in_button);
        signUp= findViewById(R.id.sign_up_button);




        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null) {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference dbRef = database.getReference("user_profiles").child(currentUser.getUid());

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Intent userLoggedInIntent = new Intent(MainActivity.this,users_chat_list.class);
                        startActivity(userLoggedInIntent);
                        finish();
                    }
//                    else {
//                        Intent userLoggedInIntent = new Intent(MainActivity.this,create_profile.class);
//                        startActivity(userLoggedInIntent);
//                        Toast.makeText(MainActivity.this,
//                                "Please Complete Your Profile",
//                                Toast.LENGTH_LONG).show();
//                        finish();
//                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(MainActivity.this, signin.class);
                startActivity(signInIntent);
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(MainActivity.this, signup.class);
                startActivity(signUpIntent);
            }
        });
    }

}
