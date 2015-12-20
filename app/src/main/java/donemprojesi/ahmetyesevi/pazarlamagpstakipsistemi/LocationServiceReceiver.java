package donemprojesi.ahmetyesevi.pazarlamagpstakipsistemi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, LocationService.class);
        context.startService(myIntent);

    }
}
