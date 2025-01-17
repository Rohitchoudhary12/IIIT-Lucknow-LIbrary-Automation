package com.example.iiitlucknowlibrary.Authentication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.iiitlucknowlibrary.UserHome;
import com.example.iiitlucknowlibrary.R;
import com.example.iiitlucknowlibrary.HomeAdministration;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    EditText reg_name, reg_email, reg_enrolment;
    TextView txt_signIn, txt_signup, txt_add;
    CardView googleSignin;
    EditText email, pass;
    private FirebaseAuth mAuth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;
    private static final int RC_SIGN_IN = 106;
    GoogleSignInClient mGoogleSignInClient;
    Dialog myDialog;
    boolean f = false;
    String name, enrolment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txt_signIn = findViewById(R.id.txt_signin);
        txt_signup = findViewById(R.id.txt_signup);
        googleSignin = findViewById(R.id.Signinwg);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.login_email);
        pass = findViewById(R.id.login_pass);

        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        txt_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String Email = email.getText().toString();
                String Pass = pass.getText().toString();

                if(TextUtils.isEmpty(Email) || TextUtils.isEmpty(Pass)){
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "Enter valid data", Toast.LENGTH_SHORT).show();
                }else if(!((Email.endsWith("@iiitl.ac.in")) || Email.equals("admin@gmail.com"))){
                    progressDialog.dismiss();
                    email.setError("Invalid Email");
                    Toast.makeText(Login.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                }else if(Pass.length()<6){
                    progressDialog.dismiss();
                    pass.setError("Invalid Password");
                    Toast.makeText(Login.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.signInWithEmailAndPassword(Email, Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                progressDialog.dismiss();
                                if(Email.equals("admin@gmail.com")){
                                    Toast.makeText(Login.this, "Welcome to administration page.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Login.this, HomeAdministration.class));
                                }
                                else{
                                    Toast.makeText(Login.this, "Welcome to student page", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Login.this, UserHome.class));
                                }
                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(Login.this, "Error to login", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
        myDialog = new Dialog(this);
        googleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                fun();
                signIn();

            }
        });
        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Registration.class));
            }
        });

    }

    private void fun() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("297181861064-7ahb7gh8b3tacknplv05ak57avgte8oa.apps.googleusercontent.com")
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("...........", "firebaseAuthWithGoogle:" + account.getEmail());
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(account.getEmail()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        //Toast.makeText(Login.this, ""+task.getResult().getSignInMethods(), Toast.LENGTH_SHORT).show();
                        if(task.getResult().getSignInMethods().isEmpty()){
                            Log.d("...........", "firebaseAuthWithGoogle:" + account.getEmail()+" no");
                            progressDialog.dismiss();
                            ShowPopup(account.getIdToken(), account.getDisplayName());
                        }
                        else{
                            Log.d("...........", "firebaseAuthWithGoogle:" + account.getEmail()+" yes");
                            firebaseAuthWithGoogle(account.getIdToken(), "1");
                        }
                    }
                });

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("MainActivity", "Google sign in failed", e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken, String status) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(status=="1"){
                                progressDialog.dismiss();
                                startActivity(new Intent(Login.this, UserHome.class));
                                Toast.makeText(getApplicationContext(), "You are successfully signed in.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(mAuth.getUid());
                                Users users = new Users(mAuth.getUid(), name, mAuth.getCurrentUser().getEmail(), enrolment);
                                databaseReference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Registered Enrolment").child(enrolment);
                                            reference1.setValue(mAuth.getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(Login.this, UserHome.class));
                                                    Toast.makeText(getApplicationContext(), "Your account is created successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(Login.this, "Error in creating user at final", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }



    public void ShowPopup(String idToken, String userName) {
        //EditText reg_name, reg_email, reg_enrolment, reg_pass, reg_re_pass;
        myDialog.setContentView(R.layout.custompopup);
        //TextView txtclose = myDialog.findViewById(R.id.txtclose);
        reg_name = myDialog.findViewById(R.id.reg_name);
        reg_name.setText(userName);
        reg_enrolment = myDialog.findViewById(R.id.reg_enrolment);
        txt_add = myDialog.findViewById(R.id.txt_add);
        boolean f1 = false, f2 = false;
        txt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                name = reg_name.getText().toString();
                enrolment = reg_enrolment.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(enrolment)) {
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "Please enter a valid input", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Registered Enrolment").child(enrolment);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                progressDialog.dismiss();
                                Toast.makeText(Login.this, "An account already exists with this email.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                myDialog.dismiss();
                                firebaseAuthWithGoogle(idToken, "2");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                            Toast.makeText(Login.this, "Error: "+error+" occurred", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

    }

}