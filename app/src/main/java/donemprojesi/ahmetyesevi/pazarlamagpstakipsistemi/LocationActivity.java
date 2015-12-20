package donemprojesi.ahmetyesevi.pazarlamagpstakipsistemi;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationActivity  extends FragmentActivity {
    ProgressDialog pDialog;
    final Context context = this;
    private GoogleMap googleHarita;

    private ArrayList<String> locations=new ArrayList<String>();
    private Spinner spinnerLocations;
    private ArrayAdapter<String> dataAdapterForLocations;

    public static List<Locations> LIST = new ArrayList<Locations>();
    private static String selectedItem=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if (googleHarita == null) {
            googleHarita = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.haritafragment)).getMap();
        }

        Button btnAnlikLokasyon=(Button)findViewById(R.id.btnAnlikLokasyon);
        btnAnlikLokasyon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(myIntent);
            }
        });

        Button btnGecmisLokasyon=(Button)findViewById(R.id.btnGecmisLokasyon);
        btnGecmisLokasyon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button btnLocation=(Button)findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItem != null) {
                    for (Locations item : LIST) {
                        if (selectedItem == item.get_time()) {
                            if (googleHarita != null) {
                                googleHarita.clear();
                                googleHarita.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(item.get_latitude()), Double.parseDouble(item.get_longitude()))).title(item.get_time()));
                                googleHarita.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(item.get_latitude()), Double.parseDouble(item.get_longitude())), 10));
                            }
                        }
                    }
                }
            }
        });

        SharedPreferences mSharedPrefs = getSharedPreferences("gpstakipsistemi", MODE_PRIVATE);
        final String idusers = mSharedPrefs.getString("idusers", "0");
        new locationsDataTask().execute("http://gpstakipsistemi.ahmetcakirci.com/services/locations/" + idusers);

        Button btnLocationAll=(Button)findViewById(R.id.btnLocationAll);
        btnLocationAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new locationsDataTask().execute("http://gpstakipsistemi.ahmetcakirci.com/services/locations/" + idusers);
            }
        });

        spinnerLocations = (Spinner) findViewById(R.id.spinnerLocations);
        spinnerLocations.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedItem=parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Bilgi");
        builder.setMessage("Oturum kapatmak istiyor musunuz?");

        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                _logout();
                dialog.dismiss();
            }

        });

        builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean serviceStatus(){
        ActivityManager appmng=(ActivityManager)getSystemService(ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo bilgi:appmng.getRunningServices(Integer.MAX_VALUE)){
            if(getApplication().getPackageName().equals(bilgi.service.getPackageName())){
                return true;
            }
        }
        return false;
    }

    public void _logout(){
        if(serviceStatus()) {
            stopService(new Intent(getApplicationContext(), LocationService.class));
        }
        File deletePrefFile = new File("/data/data/donemprojesi.ahmetyesevi.pazarlamagpstakipsistemi/shared_prefs/gpstakipsistemi.xml");
        deletePrefFile.delete();

        Intent myIntent = new Intent(this,SplashScreen.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(myIntent);

        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    private class locationsDataTask extends AsyncTask< String, String, String> {
        JSONArray dataJsonArr = null;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Create a progressbar
            pDialog = new ProgressDialog(LocationActivity.this);
            // Set progressbar title
            pDialog.setTitle("Lütfen Bekleyiniz...");
            // Set progressbar message
            pDialog.setMessage("Kontrol ediliyor.....");
            pDialog.setIndeterminate(false);
            // Show progressbar
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (data != null) {
                JSONObject object = null;

                try {
                    object = new JSONObject(data);
                } catch (JSONException e) {
                    Toast.makeText(LocationActivity.this, "JSON OBJECT EXCEPTION" + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                try {
                    if (object != null) {
                        LIST.clear();
                        dataJsonArr = object.getJSONArray("locations");
                        for (int i = 0; i < dataJsonArr.length(); i++) {
                            LIST.add(convertContact(dataJsonArr.getJSONObject(i)));
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
                            locations.clear();
                            int i=1;
                            for(Locations item : LIST) {
                                locations.add(item.get_time());
                                if (googleHarita != null) {
                                    googleHarita.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(item.get_latitude()) , Double.parseDouble(item.get_longitude()))).title(Integer.toString(i)+". "+item.get_time()));
                                    googleHarita.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(item.get_latitude()),Double.parseDouble(item.get_longitude())), 5));
                                }
                                i++;
                            }

                            dataAdapterForLocations = new ArrayAdapter<String>(LocationActivity.this, android.R.layout.simple_list_item_activated_1, locations);
                            spinnerLocations.setAdapter(dataAdapterForLocations);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pDialog.dismiss();
            }
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

        private Locations convertContact(JSONObject obj) throws JSONException
        {
            String latitude = obj.getString("latitude");
            String longitude = obj.getString("longitude");
            String time= obj.getString("time");

            return new Locations(latitude, longitude,time);
        }
    }
}
