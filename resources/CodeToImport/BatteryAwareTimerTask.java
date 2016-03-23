package energyTransformations;

import android.util.Log;
import java.util.TimerTask;

/**
 * Created by pip on 07.02.2016.
 */
public abstract class BatteryAwareTimerTask extends TimerTask {

    final double MAX_SLEEP_FACTOR = 10; //time between updates in ms. currently set to 10x
    final double BATTERY_LOW = 25;
    final double BATTERY_MED = 50;
    final double BATTERY_HIGH = 75;
    final double LOWEST_UPDATE_TIME = 3600000; //lower boundary for recheck time. currently set to 1h
    final long defaultWaitTime;

    BatteryAwarenessCriteria criteria;
    long lastRunTimestamp;

    public BatteryAwareTimerTask(BatteryAwarenessCriteria criteria, long defaultWaitTime) {
        super();
        this.criteria = criteria;
        this.defaultWaitTime = defaultWaitTime;
    }

    @Override
    public void run() {
        runIfBatteryPermits();
        lastRunTimestamp = System.currentTimeMillis();
    }

    public abstract void runIfBatteryPermits();

    public boolean checkIfBatteryPermits() {
        if (energyTransformations.BatteryUtils.getPowerSaveStatus() && criteria.getSuspendIfInBatterySafeMode()) {
            return false;
        } else if ((Integer) energyTransformations.BatteryUtils.getBatteryPercentage() < criteria.getSuspendThreshold()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkTimePassed() {
        if (lastRunTimestamp == 0 || System.currentTimeMillis() - lastRunTimestamp > getSleepTimer()) {
            return true;
        } else {
            return false;
        }
    }

    public long getSleepTimer() {
        Double result = (double) 1;
        int batteryPercentage = energyTransformations.BatteryUtils.getBatteryPercentage();
        if (!checkIfBatteryPermits()){
            result = LOWEST_UPDATE_TIME / (double) defaultWaitTime;
        } else {
            switch (criteria.getPowerSafeScheme()) {
                case POWER_SAFE_LOW:
                    if (BATTERY_LOW - batteryPercentage > 0) {
                        result = (BATTERY_LOW - batteryPercentage) / ((double) BATTERY_LOW / MAX_SLEEP_FACTOR) + 1;
                    }
                    break;
                case POWER_SAFE_MEDIUM:
                    if (BATTERY_MED - batteryPercentage > 0) {
                        result = (BATTERY_MED - batteryPercentage) / ((double) BATTERY_MED / MAX_SLEEP_FACTOR) + 1;
                    }
                    break;
                case POWER_SAFE_HIGH:
                    if (BATTERY_HIGH - batteryPercentage > 0) {
                        result = (BATTERY_HIGH - batteryPercentage) / ((double) BATTERY_HIGH / MAX_SLEEP_FACTOR) + 1;
                    }
                    break;
            }
        }
        Log.d("getSleepTimeFactor", result.toString());
        return result.longValue();
    }

    public long getLastRunTimestamp(){
        return lastRunTimestamp;
    }
}