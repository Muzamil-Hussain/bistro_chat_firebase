package com.muzamilhussain.i170191_i170228;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class signin extends AppCompatActivity {

    EditText emailAddress, password;
    Button signIn, register;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    final String url = "http://192.168.43.173/bistro_chat/login.php";
    final String profileUrl = "http://192.168.43.173/bistro_chat/check_profile_exists.php";

    private  boolean profileExists = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);


        emailAddress = findViewById(R.id.email_et_sic);
        password = findViewById(R.id.password_et_sic);
        signIn = findViewById(R.id.sign_in_button_sic);
        register = findViewById(R.id.sign_up_button_sic);


        mAuth = FirebaseAuth.getInstance();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailText = emailAddress.getText().toString();
                final String passwordText = password.getText().toString();



                if (emailText.length()>0 && passwordText.length()>0) {
                    mAuth.signInWithEmailAndPassword(emailText, passwordText)
                            .addOnCompleteListener(signin.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
//                                Log.d(TAG, "signInWithEmail:success");
                                //FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);

                                database = FirebaseDatabase.getInstance();
                                myRef = database.getReference("user_profiles").child(mAuth.getCurrentUser().getUid());

                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            myRef.child("isOnline").setValue("true");
                                            myRef.child("isOnline").onDisconnect().setValue("false");
                                            Intent loginSuccessfulIntent = new Intent (signin.this, users_chat_list.class);
                                            startActivity(loginSuccessfulIntent);
                                            finish();
                                        }else {
                                            Intent loginSuccessfulIntent = new Intent (signin.this, create_profile.class);
                                            startActivity(loginSuccessfulIntent);
                                            Toast.makeText(signin.this, "Complete Profile",
                                                    Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                // If sign in fails, display a message to the user.
                                //Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(signin.this, "Authentication failed. Please check your credentials or Internet Connection",
                                        Toast.LENGTH_LONG).show();
                                //updateUI(null);
                            }

                            // ...
                        }
                    });
//
//                     StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url,
//
//                            new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String response) {
//                                    try {
//                                        final JSONObject res=new JSONObject(response);
//
//                                        if (res.getString("response").equals("200")) {
//                                            final String user  = res.getString("id");
//
//                                            Toast.makeText(signin.this, "Login Successful-> userId: " + user,Toast.LENGTH_LONG).show();
//
//                                        }
//
//                                    } catch (JSONException e) {
//                                        Toast.makeText(signin.this,e.toString(),Toast.LENGTH_LONG).show();
//                                        e.printStackTrace();
//                                    }
//                                    Log.d("MyStringRequest",response);
//                                }
//                            },
//                            new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    //This code is executed if there is an error.
//                                    Toast.makeText(signin.this,error.toString(),Toast.LENGTH_LONG).show();
//                                }
//                            }) {
//                        protected Map<String, String> getParams() {
//                            Map<String, String> data = new HashMap<String, String>();
//                            data.put("email", emailText);
//                            data.put("password", passwordText);
//                            return data;
//                        }
//                    };
//                    Volley.newRequestQueue(signin.this).add(MyStringRequest);

                }
                else {
                    if (emailText.length()<1) {
                        Toast.makeText(signin.this, "Please Enter Email Address",Toast.LENGTH_SHORT).show();
                    }

                    if (passwordText.length()<1) {
                        Toast.makeText(signin.this, "Please Enter Password",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent (signin.this, signup.class);
                startActivity(registerIntent);
                finish();
            }
        });

    }
}
