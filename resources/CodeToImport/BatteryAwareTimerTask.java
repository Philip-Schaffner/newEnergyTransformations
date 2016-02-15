package energyRefactorings;

import java.util.TimerTask;

/**
 * Created by pip on 07.02.2016.
 */
public abstract class BatteryAwareTimerTask extends TimerTask{

    energyRefactorings.BatteryAwarenessCriteria criteria;
    long lastRunTimestamp;

    public BatteryAwareTimerTask(energyRefactorings.BatteryAwarenessCriteria criteria){
        super();
        this.criteria = criteria;
    }

    @Override
    public void run() {
        if(checkIfBatteryPermits()){
            runIfBatteryPermits();
            lastRunTimestamp = System.currentTimeMillis();
        }
    }

    public abstract void runIfBatteryPermits();

    public boolean checkIfBatteryPermits(){
        if (energyRefactorings.BatteryUtils.getPowerSaveStatus() && criteria.getSuspendIfInBatterySafeMode()){
            return false;
        } else if (!checkTimePassed()) {
            return false;
        } else if ((Integer)energyRefactorings.BatteryUtils.getBatteryPercentage() < criteria.getSuspendThreshold()){
            return false;
        } else {
            return true;
        }
    }

    private boolean checkTimePassed() {
        if (System.currentTimeMillis() - lastRunTimestamp > getSleepTimer()) {
            return true;
        } else {
            return false;
        }
    }

    public double getSleepTimer() {
        Double result = (double) 0;
        result = Math.pow(20 * (((100 - (energyRefactorings.BatteryUtils.getBatteryPercentage())) / 100)), 4) + 1;
        switch (criteria.getPowerSafeScheme()){
            case POWER_SAFE_LOW:
                return result;
            case POWER_SAFE_MEDIUM:
                return 2*result;
            case POWER_SAFE_HIGH:
                return 3*result;
        }
        return 0;
    }
}
