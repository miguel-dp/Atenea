package example.digitallife;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Activity_login extends AppCompatActivity {

    private SharedPreferences login_preference;
    private static final String FIRST_TIME = "FIRST_TIME";
    private static final String MAIN_KEY = "MAIN_KEY";
    public static final String RESET_KEY = "RESET_KEY";

    private InterstitialAd interstitial_login;

    private TextView tv_firstLogin;
    private EditText et_login;
    private Button b_setLogin;
    private Button ib_biometric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialized UI vars
        et_login = findViewById(R.id.et_login);
        tv_firstLogin = findViewById(R.id.tv_firstLogin);
        b_setLogin = findViewById(R.id.b_setLogin);
        ib_biometric = findViewById(R.id.ib_biometric);

        // load memory preference
        login_preference = getPreferences(Context.MODE_PRIVATE);

        // Post layout initialization, set the biometrics control
        biometric_layout();

        // control if main key is stabilized
        is_mainKey_stabilized();

        // Ad Banner
        AdView banner = findViewById(R.id.banner_login);
        AdRequest adRequest = new AdRequest.Builder().build();
        banner.loadAd(adRequest);

        // Interstitial Ad
        interstitial_login = new InterstitialAd(this);
        interstitial_login.setAdUnitId("ca-app-pub-9934738138092081/2614553925");
        interstitial_login.loadAd(new AdRequest.Builder().build());

    }

    private void biometric_layout() {
        if (Build.VERSION.SDK_INT >= 28) {

            final Executor executor = Executors.newSingleThreadExecutor();

            final BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle(getString(R.string.biometric_verification))
                    .setDescription(getString(R.string.biometric_desc))
                    .setNegativeButton(getString(R.string.gen_cancel), executor, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).build();

            ib_biometric.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    login(ib_biometric);
                                }
                            });
                        }
                    });
                }
            });

        } else {
            ib_biometric.setVisibility(View.GONE);
        }
    }

    /**
     * Checks if main key is stabilized.
     * If not, change the layout to give the user the power to do it
     */
    private void is_mainKey_stabilized() {

        boolean first_time = login_preference.getBoolean(FIRST_TIME, true);
        boolean reset_key = getIntent().getBooleanExtra(RESET_KEY, false);

        if (first_time || reset_key) {

            // Instructions visible and log in by biometric off
            tv_firstLogin.setVisibility(View.VISIBLE);
            ib_biometric.setVisibility(View.GONE);

            // principal button changed action to set the main key
            b_setLogin.setText(R.string.save);
            b_setLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setMainKey();
                }
            });

        } else {

            // Instructions invisible and log by biometric on
            tv_firstLogin.setVisibility(View.INVISIBLE);
            ib_biometric.setVisibility(View.VISIBLE);

            b_setLogin.setText(R.string.enter); // Reset of text to log on
        }
    }

    private void setMainKey() {

        String main_key = et_login.getText().toString();

        if (main_key.length() < 4) {
            Toast.makeText(this, R.string.main_key_must_length, Toast.LENGTH_SHORT).show();
            //Snackbar.make(b_setLogin, R.string.main_key_must_length, Snackbar.LENGTH_SHORT).show();
        } else {
            SharedPreferences.Editor editor = login_preference.edit();
            editor.putBoolean(FIRST_TIME, false);
            editor.putString(MAIN_KEY, main_key);
            editor.apply();

            Toast.makeText(this, R.string.main_key_set, Toast.LENGTH_SHORT).show();
            //Snackbar.make(b_setLogin, R.string.main_key_set, Snackbar.LENGTH_SHORT).show();

            // MAIN KEY STABILIZED: instructions updated, biometric is enabled and principal button now log in
            tv_firstLogin.setText(R.string.main_key_after_set);
            ib_biometric.setVisibility(View.VISIBLE);
            b_setLogin.setText(R.string.enter);
            b_setLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    login(view);
                }
            });
        }
    }

    public void login(View view) {

        String s_login = et_login.getText().toString();

        if (login_preference.getString(MAIN_KEY, "").equals(s_login) || view == ib_biometric) {
            Intent toMain = new Intent(this, Activity_main.class);
            startActivity(toMain);
            finish();

            // Interstitial ad show when correct log in
            if (interstitial_login.isLoaded()) {
                interstitial_login.show();
            }
        } else {

            Toast.makeText(this, R.string.main_key_error, Toast.LENGTH_SHORT).show();
            //Snackbar.make(view, R.string.main_key_error, Snackbar.LENGTH_SHORT).show();
            et_login.setText("");
        }
    }

}
