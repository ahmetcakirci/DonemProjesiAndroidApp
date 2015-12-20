package donemprojesi.ahmetyesevi.pazarlamagpstakipsistemi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
/**
 * Created by UserAhmet on 12/11/2015.
 */
public class SplashScreen extends Activity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        if(!isNetworkConnected())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("UYARI");
            builder.setMessage("Cihazınızda internet bağlantısı olmadığı için uygulama başlatılamadı!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            _exit();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                @Override
                public void run() {
                    SharedPreferences mSharedPrefs = getSharedPreferences("gpstakipsistemi", MODE_PRIVATE);

                    // This method will be executed once the timer is over
                    // Start your app main activity
                    String login = mSharedPrefs.getString("login", "false");

                    if (Boolean.parseBoolean(login)) {
                        Intent i = new Intent(SplashScreen.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(SplashScreen.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    // close this activity
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
    }

    public void _exit(){
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    private boolean isNetworkConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

