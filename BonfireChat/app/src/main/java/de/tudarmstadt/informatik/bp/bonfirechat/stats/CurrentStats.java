package de.tudarmstadt.informatik.bp.bonfirechat.stats;

/**
 * Created by johannes on 10.07.15.
 *
 * singleton instance containing latest statistics
 */
public class CurrentStats {
    private static StatsEntry instance;

    public static void setInstance(StatsEntry stats) {
        instance = stats;
    }

    public static StatsEntry getInstance() {
        if (instance == null) {
            instance = new StatsEntry();
        }
        return instance;
    }
}
