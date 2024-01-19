package lk.avn.irenttechs;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lk.avn.irenttechs.dto.LogInDTO;
import lk.avn.irenttechs.dto.SignUpDTO;
import lk.avn.irenttechs.service.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private EditText email;
    private EditText password;
    private static String errorName;
    private FirebaseAuth firebaseAuth;
    private SignInClient signInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.setStatusBarColor(Color.TRANSPARENT);

            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.slide_up);
        animation.setFillAfter(true);
        animation.setDuration(300);
        findViewById(R.id.lg_constrainer).startAnimation(animation);

        Animation animation2 = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.login_cart);
        animation2.setFillAfter(true);
        animation2.setDuration(1000);
        findViewById(R.id.su_splashView).startAnimation(animation2);

        findViewById(R.id.su_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);

            }
        });


        findViewById(R.id.lg_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Retrofit builder = new Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8080/irenttechs/")
                        .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                        .build();

                email = findViewById(R.id.su_email);
                password = findViewById(R.id.su_password);

                if (email.getText() == null || email.getText().toString().trim().isEmpty()) {
                    errorName = "Please Enter Your Email Address";
                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");

                } else if (!email.getText().toString().trim().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    errorName = "Please input a valid Email Address";
                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");

                } else if (password.getText() == null || password.getText().toString().trim().isEmpty()) {
                    errorName = "Please Enter Your Password";
                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");

                } else if (password.getText().toString().trim().length() < 6) {
                    errorName = "Password should have at least 6 characters";
                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");

                } else {


                    Service test = builder.create(Service.class);

                    LogInDTO logInDTO = new LogInDTO();

                    logInDTO.setEmail(email.getText().toString());
                    logInDTO.setPassword(password.getText().toString());

                    Call<Map<String, String>> request2 = test.sendauth(logInDTO);
                    request2.enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            if (response.isSuccessful()) {

                                Map<String, String> responseMSG = response.body();
                                Log.i(TAG, "Server Response: " + responseMSG.get("response"));


                                if (responseMSG.get("response").equals("emailnotRegistered")) {
                                    errorName = "This email is not a Registered. Please Create a Account";
                                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");
                                    Log.i(TAG, errorName);
                                } else if (responseMSG.get("response").equals("InvalidEmailPassword")) {
                                    errorName = "Invalid Email or Password";
                                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");
                                    Log.i(TAG, errorName);
                                } else if (responseMSG.get("response").equals("blocked")) {
                                    errorName = "Account has been blocked";
                                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");
                                    Log.i(TAG, errorName);
                                } else if (responseMSG.get("response").equals("verifyAccount")) {
                                    errorName = "Please verify your Account";
                                    new LoginActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");
                                    Log.i(TAG, errorName);
                                } else if (responseMSG.get("response").equals("Success")) {

                                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                    startActivity(intent);

                                    SharedPreferences preferences = getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor edit = preferences.edit();

                                    edit.putString("ID", responseMSG.get("id"));
                                    edit.putString("NAME", responseMSG.get("name"));
                                    edit.putString("EMAIL", responseMSG.get("email"));

                                    edit.apply();


                                    Log.i(TAG, "Register Success");
                                }
                            } else {
                                Log.e(TAG, "Unsuccessful response: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Log.e(TAG, "Failed to make the network request: " + t.getMessage());
                        }
                    });
                }
            }
        });



        firebaseAuth = FirebaseAuth.getInstance();

        signInClient = Identity.getSignInClient(getApplicationContext());

        findViewById(R.id.su_google_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSignInIntentRequest signInIntentRequest = GetSignInIntentRequest.builder()
                        .setServerClientId(getString(R.string.web_client_id)).build();

                Task<PendingIntent> signInIntent = signInClient.getSignInIntent(signInIntentRequest);
                signInIntent.addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                    @Override
                    public void onSuccess(PendingIntent pendingIntent) {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent).build();
                        signInLauncher.launch(intentSenderRequest);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        });

    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        Task<AuthResult> authResultTask = firebaseAuth.signInWithCredential(authCredential);
        authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    private void handleSignInResults(Intent intent) {
        try {
            SignInCredential signInCredential = signInClient.getSignInCredentialFromIntent(intent);
            String idToken = signInCredential.getGoogleIdToken();
            String name = signInCredential.getDisplayName();
            String email = signInCredential.getId();

            sendEmailAuthWithGoogle(name, email,idToken);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void sendEmailAuthWithGoogle(String name, String email,String idToken) {
        Log.i(TAG, "Name: " + name);
        Log.i(TAG, "Email: " + email);


        Retrofit builder = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/irenttechs/")
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .build();

        if (name == null || name.isEmpty()) {
            errorName = "Something Went wrong please try again";
            new SignupActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");
        } else if (email == null || email.isEmpty()) {
            errorName = "Something Went wrong please try again";
            new SignupActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");

        }  else {


            Service test = builder.create(Service.class);

            SignUpDTO signUpDTO = new SignUpDTO();
            signUpDTO.setName(name);
            signUpDTO.setEmail(email);


            Call<Map<String, String>> request2 = test.googleauth(signUpDTO);
            request2.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {

                        Map<String, String> responseMSG = response.body();
                        Log.i(TAG, "Server Response: " + responseMSG.get("response"));
                        Log.i(TAG, "Server Response: " + responseMSG.get("id"));
                        if (responseMSG.get("response").equals("EmailExists")) {
                            Log.i(TAG, "Email Already Exists");

                            SharedPreferences preferences = getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = preferences.edit();

                            edit.putString("ID", responseMSG.get("id"));
                            edit.putString("NAME", responseMSG.get("name"));
                            edit.putString("EMAIL", responseMSG.get("email"));

                            edit.apply();

                            firebaseAuthWithGoogle(idToken);

                        } else if (responseMSG.get("response").equals("Success")) {
                            Log.i(TAG, "Register Success");

                            SharedPreferences preferences = getSharedPreferences("AuthActivity", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = preferences.edit();

                            edit.putString("ID", responseMSG.get("id"));
                            edit.putString("NAME", responseMSG.get("name"));
                            edit.putString("EMAIL", responseMSG.get("email"));

                            edit.apply();

                            firebaseAuthWithGoogle(idToken);

                        }
                    } else {
                        Log.e(TAG, "Unsuccessful response: " + response.message());
                        errorName = response.message();
                        new SignupActivity.ErrorDialog().show(getSupportFragmentManager(), "Error");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Log.e(TAG, "Failed to make the network request: " + t.getMessage());
                }
            });
        }


    }


    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            handleSignInResults(o.getData());
                        }
                    });

    public static class ErrorDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.error_message, null);


            TextView messageTextView = customView.findViewById(R.id.success_dialog_message);
            messageTextView.setText(errorName);

            Button okButton = customView.findViewById(R.id.success_dialog_ok_button);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            builder.setView(customView);
            return builder.create();
        }
    }


}


