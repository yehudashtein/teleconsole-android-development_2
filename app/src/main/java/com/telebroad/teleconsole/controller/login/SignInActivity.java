package com.telebroad.teleconsole.controller.login;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.USE_SIP;
import static android.content.Intent.ACTION_SENDTO;
import static android.content.Intent.EXTRA_EMAIL;
import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.BLOCKED_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.DISABLED_USER_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.LOGIN_ERROR;
import static com.telebroad.teleconsole.helpers.TeleConsoleError.ServerError.NO_PHONES_ERROR;
import static com.telebroad.teleconsole.helpers.Utils.copyShareIntent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.Drive;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;
import com.telebroad.teleconsole.R;
import com.telebroad.teleconsole.controller.AppController;
import com.telebroad.teleconsole.controller.dashboard.DashboardActivity;
import com.telebroad.teleconsole.databinding.ActivitySigninBinding;
import com.telebroad.teleconsole.helpers.AlertsHelper;
import com.telebroad.teleconsole.helpers.SettingsHelper;
import com.telebroad.teleconsole.helpers.TeleConsoleError;
import com.telebroad.teleconsole.helpers.URLHelper;
import com.telebroad.teleconsole.helpers.Utils;
import com.telebroad.teleconsole.model.PubnubInfo;
import com.telebroad.teleconsole.model.TeleConsoleProfile;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class SignInActivity extends AppCompatActivity {

    private  final static String[] SCOPES = {"Files.Read"};
    final static String AUTHORITY = "https://login.microsoftonline.com/common";
    private static final int RC_SIGN_IN = 1000;
    private ISingleAccountPublicClientApplication mSingleAccountApp;
    private static final String TAG = "MSALTest";
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog progressDialog;
    private ActivitySigninBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initMSAL();
        addMicrosoftOnClick();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken("195037108591-rhoe55nmt91r9q9it1r6c0o5q92n6nnv.apps.googleusercontent.com")
                .requestIdToken("195037108591-ch7enof3bqph1pblti4rfch45i5m5f4a.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.googleButton.setOnClickListener(v -> {
            if (googleAccount == null) {
                startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
            }else{
                googleLogin(googleAccount);
            }
        });
        binding.signupButtonSi.setOnClickListener( event -> {
            Intent newAccountIntent = new Intent(ACTION_SENDTO);
            newAccountIntent.setData(Uri.parse("mailto:"));
            newAccountIntent.putExtra(EXTRA_EMAIL, new String[]{"sales@telebroad.com"});
            newAccountIntent.putExtra(EXTRA_SUBJECT, "New Account");
            newAccountIntent.putExtra(EXTRA_TEXT, " I would like to create a new Telebroad account for my office. Please send me a proposal, here are the details. \n \n " +
                    "Internet Provider: \n" +
                    "Internet Speed: \n" +
                    "Workstations/Extensions: \n" +
                    "Contact Number: \n");
            if (newAccountIntent.resolveActivity(getPackageManager()) == null){
                Toast.makeText(this, "You don't have any apps installed that can send emails. Please install one from your app store", Toast.LENGTH_LONG).show();
            }else {
                startActivity(newAccountIntent);
            }
        });
        binding.telebroadButton.setOnClickListener(v -> {
            Intent loginIntent = new Intent(this, TelebroadLoginActivity.class);
            copyShareIntent(getIntent(), loginIntent);
            startActivity(loginIntent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            googleLogin(account);
        }catch (ApiException e){
            Utils.logToFile(" error signing in with google" + e.getMessage());
            showErrorDialog("error signing in with google", e.getLocalizedMessage());
            //android.util.Log.d("GOOGTB", "sign in result error", e);
        }
    }

    private void googleLogin(GoogleSignInAccount account){
        //android.util.Log.d("GOOGTB", "id token " + account.getIdToken());
        showProgressDialog("Google");
        String token = account.getIdToken();
        if (token != null){
            login(URLHelper.GOOGLE_AUTH,
                    "195037108591-ch7enof3bqph1pblti4rfch45i5m5f4a.apps.googleusercontent.com",
                    token, "GOOGTB", account.getEmail());
        }
        mGoogleSignInClient.signOut();
    }

    private GoogleSignInAccount googleAccount;
    @Override
    protected void onStart() {
        googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        super.onStart();
    }

    private void addMicrosoftOnClick() {
        binding.microsoftButton.setOnClickListener((v -> {
            if(mSingleAccountApp == null){
                //Log.d("MSAL", "App is null");
                return;
            }
            if(accountActive) {
                mSingleAccountApp.acquireToken(SignInActivity.this, SCOPES, getAuthInteractiveCallback());
            }else {
                mSingleAccountApp.signIn(SignInActivity.this, null, SCOPES, getAuthInteractiveCallback());
            }
        }));
    }

    private void initMSAL() {
        PublicClientApplication.createSingleAccountPublicClientApplication(getApplicationContext(),
                R.raw.auth_config_single_account,
                new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(ISingleAccountPublicClientApplication application) {
                        //android.util.Log.d("MSAL"," app created");
                        mSingleAccountApp = application;
                        loadAccount();
                    }

                    @Override
                    public void onError(MsalException exception) {
                        displayError(exception);
                        android.content.pm.PackageInfo info = null;
                        try {
                            info = getPackageManager().getPackageInfo("com.telebroad.teleconsole", PackageManager.GET_SIGNATURES);
                            for (final android.content.pm.Signature signature : info.signatures) {
                                final java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("SHA");
                                messageDigest.update(signature.toByteArray());
                                final String signatureHash = android.util.Base64.encodeToString(messageDigest.digest(), android.util.Base64.NO_WRAP);
                                final Uri.Builder builder = new Uri.Builder();
                                final Uri uri = builder.scheme("msauth")
                                        .authority("com.telebroad.teleconsole")
                                        .appendPath(signatureHash)
                                        .build();
                               Utils.logToFile("MSAL correct url " + uri.toString());
                            }
                        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void displayError(MsalException exception) {
        //android.util.Log.e(TAG, "MSAL error", exception);
        Utils.logToFile("MASL error " + exception.getLocalizedMessage());
    }

    private boolean accountActive;
    public void loadAccount(){
        if (mSingleAccountApp == null){
            return;
        }
        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(@Nullable IAccount activeAccount) {
                //android.util.Log.d("MSAL", "account loaded " + activeAccount);
                accountActive = activeAccount != null;
            }

            @Override
            public void onAccountChanged(@Nullable IAccount priorAccount, @Nullable IAccount currentAccount) {}

            @Override
            public void onError(@NonNull MsalException exception) {}
        });
    }
    private AuthenticationCallback getAuthInteractiveCallback(){
        return new AuthenticationCallback() {
            @Override
            public void onCancel() {
                //Log.d("MSAL", "Canceled");
            }

            @Override
                public void onSuccess(IAuthenticationResult authenticationResult) {
                showProgressDialog("Microsoft");
                login(URLHelper.MICROSOFT_AUTH, "c43dc994-4085-47e3-862d-89a9751df20a", authenticationResult.getAccessToken(), "MSAL", authenticationResult.getAccount().getUsername());
                callGraphAPI(authenticationResult);
            }

            @Override
            public void onError(MsalException exception) {
                //Log.e("MSAL", "unable to sign in", exception);
                showErrorDialog("Error Signing in with microsoft", exception.getLocalizedMessage());
//                Toast.makeText(SignInActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        };
    }
    private SilentAuthenticationCallback getAuthSilentCallback() {
        return new SilentAuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
               // Log.d(TAG, "Successfully authenticated");
                callGraphAPI(authenticationResult);
            }
            @Override
            public void onError(MsalException exception) {
                //Log.d(TAG, "Authentication failed: " + exception.toString());
                displayError(exception);
            }
        };
    }
    private void callGraphAPI(IAuthenticationResult authenticationResult){
        final String accessToken = authenticationResult.getAccessToken();
        IGraphServiceClient graphClient = GraphServiceClient.builder()
                .authenticationProvider(request -> request.addHeader("Authorization", "Bearer " + accessToken))
                .buildClient();
        graphClient.me()
                .drive()
                .buildRequest()
                .get(new ICallback<Drive>() {
                    @Override
                    public void success(Drive drive) {
                       // Log.d(TAG, "Found drive " + drive.id);
                    }

                    @Override
                    public void failure(ClientException ex) {}
                });
    }

    private void login(String url, String clientID, String token, String tag, String username){
        //android.util.Log.d("MSAL", "logging in");
        SettingsHelper.putString(SettingsHelper.TELEBROAD_USERNAME, username);
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("clientID", clientID);
        params.put("returnJWT", "1");
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setMessage("Fetching Token");
            }
        });
        URLHelper.request(Request.Method.POST,url, params, tag,false, false, result ->{
            //android.util.Log.d(tag, "auth successful " + result.toString());
            saveJWT(result);
            goToDashboard();
        }, error -> {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                showErrorDialog(R.string.error_fetching_token, error);
            });
            Utils.logToFile("Error getting token " + error);
//            android.util.Log.d(tag, "auth error " + error);
        });
    }

    private void showProgressDialog(String method){
        progressDialog = new ProgressDialog(this);
        method = method == null || method.isEmpty() ? "": " " + getString(R.string.with_method) + " " + method;
        progressDialog.setMessage("Logging In" + method);
        progressDialog.show();
    }
    private void goToDashboard(){
        //android.util.Log.d("Login", "going to dashboard");
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setMessage("Fetching Data");
            }
        });
        TeleConsoleProfile.signIn((error -> {
            if (error != null) {
                progressDialog.dismiss();
                // Uh-oh there is an error
                if (error == LOGIN_ERROR) {
                    runOnUiThread(() -> {
                        Snackbar snackbar = AlertsHelper.getLongSnack(this, R.string.login_error);
                        snackbar.setAction(android.R.string.ok, (snackEvent) -> {});
                        snackbar.show();
                    });
                } else if (error == BLOCKED_ERROR) {
                    runOnUiThread(() -> URLHelper.showBlockedError(this));
                } else if (error == NO_PHONES_ERROR) {
                    showErrorDialogWithContactBtn(R.string.no_phones, error);
                } else if (error == DISABLED_USER_ERROR) {
                    showErrorDialogWithContactBtn(R.string.disabled_user, error);
                } else {
                    runOnUiThread(() -> {
                        if (isFinishing()){
                            return;
                        }
                        AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(R.string.loginError)
                                .setMessage(error.getFullErrorMessage()).setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            try {
                                dialog.dismiss();
                            } catch (IllegalArgumentException iae) {
                                Utils.logToFile(this, "Detached window error " + iae.getMessage());
                            }
                        }).create();
                        alert.setOnShowListener(dialog -> {
                            Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                            Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                            positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                            negativeButton.setTextColor(getResources().getColor(R.color.black,null));
                        });alert.show();
                    });
                }
            } else {
                // There is no error, continue to next activity
                Intent intent;
                // Decide which activity to proceed to, if we have permissions, we can continue to the dashboard immediately.
                 if (AppController.getInstance().hasPermissions(READ_CONTACTS, READ_PHONE_STATE, CALL_PHONE, RECORD_AUDIO, READ_EXTERNAL_STORAGE, USE_SIP, Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? BLUETOOTH : BLUETOOTH_CONNECT)) {
                    intent = new Intent(getApplicationContext(), DashboardActivity.class);
                } else {
                    intent = new Intent(this, PermissionsActivity.class);
                }
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                copyShareIntent(getIntent(), intent);
                if (progressDialog != null && progressDialog.isShowing()) {
                    try{
                        progressDialog.dismiss();
                    }catch (IllegalArgumentException iae){
                        Utils.logToFile(this, "Detached window error " + iae.getMessage());
                    }
                }
                startActivity(intent);
            }
        }));
    }

    private void showErrorDialog(@StringRes int title, TeleConsoleError error) {
        runOnUiThread(() -> {
            if(isFinishing()){
                return;
            }
            AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(title).setMessage(error.getFullErrorMessage())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
            alert.setOnShowListener(dialog -> {
                TextView messageView = alert.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setTextColor(ContextCompat.getColor(this, R.color.black));
                }
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });alert.show();

        });
    }
    private void showErrorDialog(String title, String error) {
        runOnUiThread(() -> {
            if(isFinishing()){
                return;
            }
            AlertDialog alert = new MaterialAlertDialogBuilder(this).setTitle(title).setMessage(error)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
        });
    }
    private void showErrorDialogWithContactBtn(@StringRes int title, TeleConsoleError error) {
        runOnUiThread(() -> {
            if(isFinishing()){
                return;
            }
            AlertDialog.Builder alertDialogBuilder = new MaterialAlertDialogBuilder(this).setTitle(title).setMessage(error.getFullErrorMessage())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                alertDialogBuilder.setPositiveButton(R.string.contact_button_title, ((dialog, which) -> {
                    Utils.callSupport(this);
                    dialog.dismiss();
                }));
            }
            alertDialogBuilder.create().setOnShowListener(dialog -> {
                TextView messageView = ((AlertDialog) dialog).findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setTextColor(ContextCompat.getColor(this, R.color.black));
                }
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black,null));
                negativeButton.setTextColor(getResources().getColor(R.color.black,null));
            });
            alertDialogBuilder.show();
        });
    }

    private void saveJWT(JsonElement element) {
        if (element.isJsonObject()) {
            //android.util.Log.d("JWT", "object " + element.getAsJsonObject().get("auth"));
            SettingsHelper.putString(SettingsHelper.JWT_TOKEN, element.getAsJsonObject().get("auth").getAsString());
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
           // android.util.Log.d("JWT", "string " + element.getAsString());
            SettingsHelper.putString(SettingsHelper.JWT_TOKEN, element.getAsString());
        }
    }
}