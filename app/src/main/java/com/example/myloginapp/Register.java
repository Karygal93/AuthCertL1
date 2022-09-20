package com.example.myloginapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        BeginSignInRequest signInRequest;

                oneTapClient = Identity.getSignInClient(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        signInRequest = BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true).setServerClientId(getString(R.string.default_web_client_id)).setFilterByAuthorizedAccounts(true).build()).build();

        MaterialButton registerbtn = (MaterialButton) findViewById(R.id.registerbtn);

        TextView username =(TextView) findViewById(R.id.username);
        TextView password =(TextView) findViewById(R.id.password);
        TextView phone =(TextView) findViewById(R.id.phone);
        TextView code =(TextView) findViewById(R.id.code);


        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().equals("admin") && password.getText().toString().equals("admin")) {
                    //corrects
                    Toast.makeText(Register.this, "REGISTER SUCCESSFUL", Toast.LENGTH_SHORT).show();
                }
                String usernametxt = username.getText().toString();
                String passwordtxt = password.getText().toString();
                Log.d("createuser",usernametxt+" "+passwordtxt);

                mAuth.createUserWithEmailAndPassword(usernametxt, passwordtxt)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                  showUI();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("Create User", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }


        });


        ImageView imggoogle = (ImageView) findViewById(R.id.imggoogle);

        imggoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Google sing in","Start popup");
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener( new OnSuccessListener<BeginSignInResult>() {
                            @Override
                            public void onSuccess(BeginSignInResult result) {
                                activityResultLauncher.launch(new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build());

                            }
                        })
                        .addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // No saved credentials found. Launch the One Tap sign-up flow, or
                                // do nothing and continue presenting the signed-out UI.
                                Log.d("Google singIn", e.getLocalizedMessage());
                            }
                        });

            }
        });


    }
    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("GooglesingIn",""+result.getResultCode());
                    Log.d("Google singIn",""+result.getData());
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            SignInCredential googleCredential = oneTapClient.getSignInCredentialFromIntent(data);
                            String idToken = googleCredential.getGoogleIdToken();
                            fireSingIn(idToken);
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w( "SingIn Google","Google sign in failed", e);
                        }
                    }
                }
            });

    private void fireSingIn (String idToken){
        if (idToken !=  null) {
            // Got an ID token from Google. Use it to authenticate
            // with Firebase.
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Google sing In", "signInWithCredential:success");
                                showUI();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Google SingIn", "signInWithCredential:failure", task.getException());
                            }
                        }
                    });
        }

    }

    public void showUI (){
        Toast toast = Toast.makeText(Register.this, "Register Done",Toast.LENGTH_LONG);
        toast.show();
        Intent intent = new Intent(Register.this, MainActivity.class);
        Register.this.startActivity(intent);
    }





}