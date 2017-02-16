package trucks;

import java.util.Map;
import java.util.concurrent.*;

public class Location {
    private Map<Location, Integer> distances = new ConcurrentHashMap<>();
    private BlockingQueue<Integer> parcels = new PriorityBlockingQueue<>();
    private String name;

    public Location(String name, int inintialParcels) {
        this.name = name;
        for (int i = 0; i < inintialParcels; i++) {
            try {
                parcels.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getDistanceTo(Location location){
        Integer d = distances.get(location);
        if(d != null) {
            return d;
        }else throw new RuntimeException("no data for this location");
    }

    public  void  setDistanceTo(Location location, int distance){
        distances.put(location, distance);
    }

    public void unload() {
        try {
            parcels.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            parcels.put(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
