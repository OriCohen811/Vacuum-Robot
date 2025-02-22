package bgu.spl.mics.application.objects;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    private static class SingletonHolder{
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;


    private StatisticalFolder(){
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }

    public void increaseRuntime(){
        systemRuntime++;
    }

    public void addDetectedObj(int n){
        this.numDetectedObjects= numDetectedObjects + n;
    }

    public void addTrackedObj(int n){
        this.numTrackedObjects= numTrackedObjects + n;
    }

    public void addLandmarks(int n){
        this.numLandmarks= numLandmarks + n;
    }

    public int getSystemRuntime(){
        return systemRuntime;
    }

    public int getNumDetectedObjects(){
        return numDetectedObjects;
    }

    public int getNumTrackedObjects(){
        return numTrackedObjects;
    }

    public int getNumLandmarks(){
        return numLandmarks;
    }

    public static StatisticalFolder getInstance(){
        return SingletonHolder.instance;
    }

    @Override
    public String toString() {
        return  "StatisticalFolder{" + 
                "systemRuntime=" + systemRuntime + 
                ", numDetectedObjects=" + numDetectedObjects + 
                ", numTrackedObjects=" + numTrackedObjects + 
                ", numLandmarks=" + numLandmarks + 
                '}';
    }
}
