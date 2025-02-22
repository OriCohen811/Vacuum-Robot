package bgu.spl.mics.application.objects;

public class ServicesManager {

    private static class SingletonHolder {
        private static final ServicesManager instance = new ServicesManager();
    }
    
    private int totalServices;
    private int activeServices;

    private ServicesManager() {
        totalServices = 0;
        activeServices = 0;
    }

    public synchronized void increaseTotal() {
        totalServices = totalServices + 1;
        notifyAll();
    }

    public synchronized void increaseTotal(int n) {
        totalServices = totalServices + n;
        notifyAll();
    }

    public synchronized void increaseActive() {
        activeServices = activeServices + 1;
        notifyAll();
    }

    public synchronized void decreaseActive() {
        activeServices = activeServices - 1;
        notifyAll();
    }

    public synchronized int getTotal() {
        return totalServices;
    }

    public synchronized int getActive() {
        return activeServices;
    }

    public synchronized void waitUntilAllActive() {
        while (activeServices != totalServices) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    public synchronized void waitUntilAllOthersInactive() {
        while (activeServices > 0) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    public static ServicesManager getInstance() {
        return SingletonHolder.instance;
    }
}
