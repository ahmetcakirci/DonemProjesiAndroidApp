package donemprojesi.ahmetyesevi.pazarlamagpstakipsistemi;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements LocationListener {
    private TextView latituteField;
    private TextView longitudeField;
    private LocationManager locationManager;
    private String provider;

    private GoogleMap googleHarita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences mSharedPrefs = getSharedPreferences("gpstakipsistemi", MODE_PRIVATE);

        String name = mSharedPrefs.getString("name", "");
        String surname = mSharedPrefs.getString("surname", "");

        TextView txtName=(TextView)findViewById(R.id.textViewUserName);
        txtName.setText(name);
        TextView txtSurName=(TextView)findViewById(R.id.textViewUserSurname);
        txtSurName.setText(surname);

        latituteField = (TextView) findViewById(R.id.latituteField);
        longitudeField = (TextView) findViewById(R.id.longitudeField);

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            // set title
            alertDialogBuilder.setTitle("UYARI");
            // set dialog message
            alertDialogBuilder
                    .setMessage("Konum bilgisini almak için telefonuzun GPS ayarını aktif ediniz!")
                    .setCancelable(false)
                    .setPositiveButton("Ayarlar",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("İptal",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    });
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        final Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            onLocationChanged(location);
        } else {
            latituteField.setText("0,0");
            longitudeField.setText("0,0");
        }

        ImageButton btnGPSRefresh=(ImageButton)findViewById(R.id.btnGPSRefresh);
        btnGPSRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (location != null) {
                    onLocationChanged(location);
                }
            }
        });

        final Button btnServiceStart=(Button)findViewById(R.id.btnServiceStart);
        if(!serviceStatus()) {
            btnServiceStart.setText("Servisi Başlat");
        }else {
            btnServiceStart.setText("Servisi Durdur");
        }
        btnServiceStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!serviceStatus()) {
                    startService(new Intent(getApplicationContext(), LocationService.class));
                    btnServiceStart.setText("Servisi Durdur");
                }else {
                    stopService(new Intent(getApplicationContext(), LocationService.class));
                    btnServiceStart.setText("Servisi Başlat");
                }
            }
        });

        Button btnGecmisLokasyon=(Button)findViewById(R.id.btnGecmisLokasyon);
        btnGecmisLokasyon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getBaseContext(), LocationActivity.class);
                startActivity(myIntent);
            }
        });
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

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 200, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (googleHarita == null) {
            googleHarita = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.haritafragment))
                    .getMap();
            if (googleHarita != null) {
                LatLng istanbulKoordinat = new LatLng(location.getLatitude(),location.getLongitude());
                googleHarita.addMarker(new MarkerOptions().position(istanbulKoordinat).title("Anlık Lokasyon"));
                googleHarita.moveCamera(CameraUpdateFactory.newLatLngZoom(istanbulKoordinat, 13));
            }
        }
        latituteField.setText(String.format("%s %f", "Lat:",location.getLatitude()));
        longitudeField.setText(String.format("%s %f", "Long:",location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
}
