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
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private String mVerificationId ;
    private PhoneAuthProvider.ForceResendingToken mResendToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        BeginSignInRequest signInRequest;

        oneTapClient = Identity.getSignInClient(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInRequest = BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true).setServerClientId(getString(R.string.default_web_client_id)).setFilterByAuthorizedAccounts(true).build()).build();

        MaterialButton loginbtn = (MaterialButton) findViewById(R.id.loginbtn);

        TextView username =(TextView) findViewById(R.id.username);
        TextView password =(TextView) findViewById(R.id.password);
        TextView phone =(TextView) findViewById(R.id.phone);
        TextView code =(TextView) findViewById(R.id.code);


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phonetxt = phone.getText().toString();
                String usernametxt = username.getText().toString();
                String passwordtxt = password.getText().toString();
                String codetxt = code.getText().toString();
                Log.d("createuser",usernametxt+" "+passwordtxt);
                if(!usernametxt.isEmpty()){
                    mAuth.signInWithEmailAndPassword(usernametxt, passwordtxt)
                            .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        showUI();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("Create User", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Login.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            );

                }
                else if (!phonetxt.isEmpty() && !codetxt.isEmpty()){
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codetxt);
                    signInWithPhoneAuthCredential(credential);

                }
                else {
                    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            // This callback will be invoked in two situations:
                            // 1 - Instant verification. In some cases the phone number can be instantly
                            //     verified without needing to send or enter a verification code.
                            // 2 - Auto-retrieval. On some devices Google Play services can automatically
                            //     detect the incoming verification SMS and perform verification without
                            //     user action.
                            Log.d("Phone auth", "onVerificationCompleted:" + credential);

                            signInWithPhoneAuthCredential(credential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            // This callback is invoked in an invalid request for verification is made,
                            // for instance if the the phone number format is not valid.
                            Log.w("phone auth", "onVerificationFailed", e);

                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid request
                            } else if (e instanceof FirebaseTooManyRequestsException) {
                                // The SMS quota for the project has been exceeded
                            }

                            // Show a message and update the UI
                        }

                        @Override
                        public void onCodeSent(@NonNull String verificationId,
                                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
                            // The SMS verification code has been sent to the provided phone number, we
                            // now need to ask the user to enter the code and then construct a credential
                            // by combining the code with a verification ID.
                            Log.d("Phone auth", "onCodeSent:" + verificationId);
                            Toast toast = Toast.makeText(Login.this, "Code Sent",Toast.LENGTH_LONG);
                            toast.show();

                            // Save verification ID and resending token so we can use them later
                            mVerificationId = verificationId;
                            mResendToken = token;
                        }
                    };
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phonetxt)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(Login.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }


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

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("PhoneUI", "signInWithCredential:success");
                            showphoneUI();
                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("Phone UI", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void showUI (){
        FirebaseUser user = mAuth.getCurrentUser();
        user.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult result) {
                Boolean isTeacherB = false;
                if (result.getClaims().get("teachers") != null){
                    String isTeacher = result.getClaims().get("teachers").toString();
                    isTeacherB = Boolean.valueOf(isTeacher);
                }
                if (isTeacherB) {
                    Intent intent = new Intent(Login.this, profile.class);
                    Login.this.startActivity(intent);

                } else {
                    Intent intent = new Intent(Login.this, profileStudent.class);
                    Login.this.startActivity(intent);
                }
            }
        });
    }

    public void showphoneUI (){
        Intent intent = new Intent(Login.this, profileStudent.class);
        Login.this.startActivity(intent);
    }





}