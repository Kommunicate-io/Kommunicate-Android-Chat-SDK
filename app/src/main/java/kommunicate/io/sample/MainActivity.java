package kommunicate.io.sample;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicommons.commons.core.utils.Utils;

import io.kommunicate.KmHelper;
import io.kommunicate.app.BuildConfig;
import io.kommunicate.callbacks.KmPushNotificationHandler;
import io.kommunicate.utils.KMPermissionUtils;
import io.kommunicate.users.KMUser;
import io.kommunicate.Kommunicate;
import io.kommunicate.app.R;
import io.kommunicate.callbacks.KMLoginHandler;

public class MainActivity extends AppCompatActivity {

    private EditText mUserId, mPassword;
    private LinearLayout layout;
    private boolean exit = false;
    private static final int REQUEST_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Kommunicate.init(this, BuildConfig.KOMMUNICATE_APP_ID);
        createView();
        if (Utils.hasMarshmallow()) {
            showRunTimePermission();
        }
    }

    private void createView() {
        layout = findViewById(R.id.footerSnack);
        mUserId = findViewById(R.id.userId_editText);
        mPassword = findViewById(R.id.password_editText);
        AppCompatButton loginButton = findViewById(R.id.btn_signup);
        TextView txtViewPrivacyPolicy = findViewById(R.id.txtPrivacyPolicy);
        txtViewPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        loginButton.setOnClickListener(loginListener);
    }

    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                final String mUserIdText = mUserId.getText().toString().trim();
                if (TextUtils.isEmpty(mUserIdText) || mUserId.getText().toString().trim().length() == 0) {
                    Toast.makeText(getBaseContext(), "Enter UserId ", Toast.LENGTH_SHORT).show();
                    return;
                }
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Logging in..");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                initLoginData(mUserId.getText().toString().trim(), mPassword.getText().toString().trim(), progressDialog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void showRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestContactsPermissions();
        }
    }

    private void requestContactsPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_CONTACTS)) {
            Snackbar.make(layout, R.string.contact_permission,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(MainActivity.this, PERMISSIONS_CONTACT,
                                            REQUEST_CONTACTS);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CONTACTS) {
            if (KMPermissionUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.contact_permission_granted);
            } else {
                showSnackBar(R.string.contact_permission_granted);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showSnackBar(int resId) {
        Snackbar.make(layout, resId,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);
        }
    }

    public void initLoginData(String userId, String password, final ProgressDialog progressDialog) {
        final KMUser user = new KMUser();
        user.setUserId(userId);
        user.setApplicationId(KmHelper.APP_KEY);
        if (!TextUtils.isEmpty(password)) {
            user.setPassword(password);
        }
        login(progressDialog, user);
    }

    private void login(final ProgressDialog progressDialog, KMUser user) {
        Kommunicate.login(MainActivity.this, user, new KMLoginHandler() {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                ApplozicClient.getInstance(context).hideActionMessages(true);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Kommunicate.registerForPushNotification(context, new KmPushNotificationHandler() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse) { }
                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) { }
                });
                Kommunicate.openConversation(context);
                finish();
            }
            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }
}
