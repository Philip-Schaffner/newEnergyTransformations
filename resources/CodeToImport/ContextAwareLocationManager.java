package energyRefactorings;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.BatteryManager;

/**
 * Created by pip on 01.02.2016.
 */
public class ContextAwareLocationManager {

    private static ContextAwareLocationManager instance = null;
    protected ContextAwareLocationManager() {
        // Exists only to defeat instantiation.
    }
    public static ContextAwareLocationManager getInstance() {
        if(instance == null) {
            instance = new ContextAwareLocationManager();
        }
        return instance;
    }

    public static Application getApplicationUsingReflection() throws Exception {
        return (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, (Object[]) null);
    }

    public static int getBatteryPercentage() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getApplicationUsingReflection().registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level / (float) scale;
            return (int) batteryPct;
        } catch (Exception e) {
            System.out.println(e);
            return 100;
        }
    }

    public static Criteria getContextAwareCriteria(){
        int batteryPercentage = getBatteryPercentage();
        Criteria c = new Criteria();
        if (batteryPercentage < 20) {
            c.setAccuracy(Criteria.ACCURACY_COARSE);
            c.setPowerRequirement(Criteria.POWER_LOW);
        } else if (batteryPercentage < 50){
            c.setAccuracy(Criteria.ACCURACY_COARSE);
            c.setPowerRequirement(Criteria.POWER_MEDIUM);
        } else {
            c.setAccuracy(Criteria.ACCURACY_FINE);
            c.setPowerRequirement(Criteria.POWER_HIGH);
        }
        return c;
    }

    public static String getContextAwareProvider(){
        int batteryPercentage = getBatteryPercentage();
        if (batteryPercentage < 50) {
            return LocationManager.NETWORK_PROVIDER;
        } else {
            return LocationManager.GPS_PROVIDER;
        }
    }
}
