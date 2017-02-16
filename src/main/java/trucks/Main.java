package trucks;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int TRUCKS_AT_A = 10;
    private static final int TRUCKS_AT_B = 5;
    private static final int DELAY = 500;
    private static final int DISTANCE_AB = 1000;
    private static final int DISTANCE_BA = 3000;

    public static void main(String[] args) {
        Location a = new Location("A", 10);
        Location b = new Location("B", 5);
        a.setDistanceTo(b, DISTANCE_AB);
        b.setDistanceTo(a, DISTANCE_BA);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(TRUCKS_AT_A + TRUCKS_AT_B);
        for (int i = 0; i < TRUCKS_AT_A; i++) {
            executorService.submit(new Truck("A" + i, true, a, b));
        }
        for (int i = 0; i < TRUCKS_AT_B; i++) {
            executorService.schedule(new Truck("B" + i, true, b, a), DELAY, TimeUnit.MILLISECONDS);
        }
    }
}
