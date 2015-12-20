package donemprojesi.ahmetyesevi.pazarlamagpstakipsistemi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    ProgressDialog pDialog;
    final Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText email=(EditText)findViewById(R.id.editTextEmail);
        final EditText password=(EditText)findViewById(R.id.editTextPassword);
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        final String IMEI=telephonyManager.getDeviceId();
        TextView textViewIMEI=(TextView)findViewById(R.id.textViewIMEI);
        textViewIMEI.setText("IMEI: " + IMEI);

        Button button = (Button) findViewById(R.id.btnLogin);
        button.setOnClickListener(new OnClickListener() {
            @Override

            public void onClick(View view) {
                if (email.getText().toString().trim().length() == 0)
                    Toast.makeText(LoginActivity.this, "EMail Adresini Giriniz", Toast.LENGTH_LONG).show();
                else if (password.getText().toString().trim().length() == 0)
                    Toast.makeText(LoginActivity.this, "Password Giriniz", Toast.LENGTH_LONG).show();
                else {
                    new loginDataTask().execute("http://gpstakipsistemi.ahmetcakirci.com/services/login/" + email.getText().toString() + '/' + password.getText().toString() + '/' + IMEI);
                }
            }
        });
    }

    private class loginDataTask extends AsyncTask < String, String, String > {
        JSONArray dataJsonArr = null;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Create a progressbar
            pDialog = new ProgressDialog(LoginActivity.this);
            // Set progressbar title
            pDialog.setTitle("Lütfen Bekleyiniz...");
            // Set progressbar message
            pDialog.setMessage("Kontrol ediliyor.....");
            pDialog.setIndeterminate(false);
            // Show progressbar
            pDialog.show();
        }

        @Override
        protected String doInBackground(String...params) {
            OkHttpClient client = new OkHttpClient();
            final String login = "admin";
            final String password = "admin";
            String credential = Credentials.basic(login, password);
            Request request = new Request.Builder().header("Authorization", credential)
                    .url(params[0])
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            if (data != null) {
                JSONObject object = null;
                String ImageUrl = null;
                try {
                    object = new JSONObject(data);
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, "JSON OBJECT EXCEPTION" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                try {
                    if (object != null) {
                        String idusers=null,name=null,surname=null;
                        dataJsonArr = object.getJSONArray("properties");
                        for (int i = 0; i < dataJsonArr.length(); i++) {

                            JSONObject item = dataJsonArr.getJSONObject(i);
                             idusers = item.getString("idusers");
                             name = item.getString("name");
                             surname = item.getString("surname");
                        }

                        if(dataJsonArr.length()==0 && !Boolean.parseBoolean(object.getString("success"))) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setCancelable(true);
                            builder.setTitle("UYARI");
                            builder.setMessage(object.getString("message"));
                            builder.setInverseBackgroundForced(true);
                            builder.setPositiveButton("TAMAM",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }else{
                            if(idusers!=null && name!=null && surname!=null){
                                SharedPreferences sharedpreferences = getSharedPreferences("gpstakipsistemi", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("login", "true");
                                editor.putString("idusers", idusers);
                                editor.putString("name", name);
                                editor.putString("surname", surname);
                                editor.commit();

                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pDialog.dismiss();
            }
        }
    }
}
