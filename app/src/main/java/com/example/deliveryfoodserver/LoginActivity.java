package com.example.deliveryfoodserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deliveryfoodserver.Common.Common;
import com.example.deliveryfoodserver.Model.User;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {
    private EditText edtphone,password;
    private Button login;
    private TextView txtSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtphone= findViewById(R.id.edtUsername);
        password= findViewById(R.id.edtPassword);
        txtSignUp=findViewById(R.id.txtSignUpNewAc);
        login= findViewById(R.id.btnLogin);
        setupUI(findViewById(R.id.parent));
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        final DatabaseReference table_user=firebaseDatabase.getReference("User");
//        txtSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(LoginActivity.this,SignUpActivity));
//            }
//        });
        //Firebase.setAndroidContext(LoginActivity.this);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Login(table_user);


    }

    public void Login(final DatabaseReference table_user) {
        login.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                final ProgressDialog progress=new ProgressDialog(LoginActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                final String usernameTemp = edtphone.getText().toString();
                final String passwordTemp = password.getText().toString();
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (usernameTemp.equals("") || passwordTemp.equals("")) {
                            progress.dismiss();
                            Toast.makeText(LoginActivity.this, "Please check your username and password", Toast.LENGTH_SHORT).show();
                        } else {
                            if (dataSnapshot.child(edtphone.getText().toString()).exists()) {
                                User uUser = dataSnapshot.child(edtphone.getText().toString()).getValue(User.class);//connect to child of Phone include Username, Password
                                uUser.setPhone(edtphone.getText().toString());
                                if(Boolean.parseBoolean(uUser.getIsStaff())==true) {
                                    if (uUser.getPassword().equals(password.getText().toString())) {
                                        progress.dismiss();
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        Common.currentUser = uUser;
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        progress.dismiss();
                                        Toast.makeText(LoginActivity.this, "Please check your username and password", Toast.LENGTH_SHORT).show();
                                        //  edtphone.setText(uUser.getPassword());
                                    }
                                }
                                else {
                                    Toast.makeText(LoginActivity.this,"Please login with Staff account",Toast.LENGTH_SHORT).show();
                                    progress.dismiss();
                                }
                            } else {
                                progress.dismiss();
                                Toast.makeText(LoginActivity.this, "This account is not exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(LoginActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

}
