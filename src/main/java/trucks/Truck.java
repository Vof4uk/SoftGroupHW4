package trucks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Truck implements Runnable{
    private String name;
    private int location = 0;
    private List<Location> route = new ArrayList<>();
    private boolean cyclicRoute;
    private boolean onRoute = true;

    public Truck(String name, boolean cyclicRoute, Location... route) {
        this.cyclicRoute = cyclicRoute;
        this.route.addAll(Arrays.asList(route));
        this.name = name;
    }

    @Override
    public void run() {
        if(route.size() < 2){
            return;
        }
        while (onRoute){
            System.out.printf("loading truck %s at %s%n", name, route.get(location).getName());
            load();
            System.out.printf("truck %s left %s%n", name, route.get(location).getName());
            moveToNextLocation();
            System.out.printf("truck %s arrived to %s%n", name, route.get(location).getName());
            unload();
            System.out.printf("unloading truck %s at %s%n", name, route.get(location).getName());
        }
    }

    private void unload() {
        route.get(location).unload();
    }

    private void moveToNextLocation(){
        int current = location;
        int next = current + 1;
        if(next >= route.size() && cyclicRoute){
            next = next % route.size();
        }else if(next >= route.size() &! cyclicRoute){
            onRoute = false;
            return;
        }

        int distance = route.get(current).getDistanceTo(route.get(next));
        try {
            Thread.sleep(distance);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        location = next;
    }

    private void load(){
        route.get(location).load();
    }
}
