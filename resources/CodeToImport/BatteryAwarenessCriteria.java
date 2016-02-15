package DialogElements;

/**
 * Created by pip on 07.02.2016.
 */
public class BatteryAwarenessCriteria {

    public enum PowerSaveScheme {POWER_SAFE_LOW, POWER_SAFE_MEDIUM, POWER_SAFE_HIGH};

    public void setSuspendIfInBatterySafeMode(boolean suspendIfInBatterySafeMode) {
        this.suspendIfInBatterySafeMode = suspendIfInBatterySafeMode;
    }

    public void setPowerSafeScheme(PowerSaveScheme powerSafeScheme) {
        this.powerSafeScheme = powerSafeScheme;
    }

    public void setSuspendThreshold(int suspendThreshold) {
        this.suspendThreshold = suspendThreshold;
    }

    private boolean suspendIfInBatterySafeMode;
    private PowerSaveScheme powerSafeScheme;
    private int suspendThreshold;

    public boolean getSuspendIfInBatterySafeMode() {
        return suspendIfInBatterySafeMode;
    }

    public PowerSaveScheme getPowerSafeScheme() {
        return powerSafeScheme;
    }

    public int getSuspendThreshold() { return suspendThreshold; }

    public BatteryAwarenessCriteria(PowerSaveScheme powerSafeScheme, boolean suspendIfInBatterySafeMode, int suspendThreshold){
        this.powerSafeScheme = powerSafeScheme;
        this.suspendIfInBatterySafeMode = suspendIfInBatterySafeMode;
        this.suspendThreshold = suspendThreshold;
    }

    public BatteryAwarenessCriteria(){
        this.powerSafeScheme = PowerSaveScheme.POWER_SAFE_MEDIUM;
        this.suspendIfInBatterySafeMode = false;
        this.suspendThreshold = 15;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder("Powersaving Scheme: ");
        switch (powerSafeScheme){
            case POWER_SAFE_LOW:
                stringBuilder.append("Low");
                break;
            case POWER_SAFE_MEDIUM:
                stringBuilder.append("Medium");
                break;
            case POWER_SAFE_HIGH:
                stringBuilder.append("High");
                break;
        }
        stringBuilder.append(", suspend if in powersaving mode: ");
        if (suspendIfInBatterySafeMode){
            stringBuilder.append("true");
        } else {
            stringBuilder.append("false");
        }
        stringBuilder.append(", suspend threshold: " + Integer.toString(suspendThreshold));

        return stringBuilder.toString();
    }

    public int getPowerSaveSchemeIndex(){
        switch (powerSafeScheme){
            case POWER_SAFE_HIGH:
                return 2;
            case POWER_SAFE_MEDIUM:
                return 1;
            default:
                return 0;
        }
    }

}
