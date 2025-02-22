package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using
 * data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final LiDarDataBase DataBase;
    private final int id;
    private final int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    private final Map<Integer, List<TrackedObject>> _currentTrackedObjects;
    private int currentTime;

    public LiDarWorkerTracker(int id, int frequency, String lidars_data_path) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new LinkedList<>();
        this.DataBase = LiDarDataBase.getInstance(lidars_data_path);
        _currentTrackedObjects = new ConcurrentHashMap<Integer, List<TrackedObject>>();
        currentTime = 0;
    }

    public TrackedObjectsEvent getTrackedObjectsEvent(DetectObjectsEvent e){
        List<TrackedObject> relevantTrackedObjects = new LinkedList<>();
        List<TrackedObject> timeTrackedObjects = _currentTrackedObjects.get(e.getTime());
        List<DetectedObject> detectedObjects = e.getStampedDetectedObjects().getDetectedObjectsList();
        for(DetectedObject obj :detectedObjects){
            for(TrackedObject trackedObject: timeTrackedObjects){
                if(obj.getId().equals(trackedObject.getId())){
                    TrackedObject newTrackedObject = new TrackedObject(trackedObject.getId(), trackedObject.getTime(), obj.getDescription(), trackedObject.getCoordinates());
                    relevantTrackedObjects.add(newTrackedObject);
                    break;
                }
                else if(trackedObject.getId().equals("ERROR")){
                    error();
                    List<TrackedObject> errorList = new LinkedList<>();
                    errorList.add(trackedObject);
                    return new TrackedObjectsEvent(errorList, -1);
                }
            }
        }
        return new TrackedObjectsEvent(relevantTrackedObjects ,e.getTime());
    }

    public void updatesLastTrackedObjects(int time) {
        List<TrackedObject> newTrackedObjects = new LinkedList<>();

        if(DataBase.outOfRange(time)){
            finished();
            return;
        }

        List<StampedCloudPoints> SCPList = DataBase.getStampedCloudPoints(time);
        if(SCPList!=null) {
            for (StampedCloudPoints SCP : SCPList) {
                List<CloudPoint> coordinates = new LinkedList<>();
                for (List<Double> list : SCP.getCoordinatesList()) {
                    coordinates.add(new CloudPoint(list.get(0), list.get(1)));
                }
                TrackedObject trackedObject = new TrackedObject(SCP.getID(), SCP.getTime(), null, coordinates);
                newTrackedObjects.add(trackedObject);
            }
            _currentTrackedObjects.put(time, newTrackedObjects);
            lastTrackedObjects = newTrackedObjects;
        }

        this.currentTime = time;
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return this.status;
    }

    private void finished(){
        status = STATUS.DOWN;
    }

    private void error(){
        status = STATUS.ERROR;
    }

    public List<TrackedObject> getLastTrackedObjects(){
        return this.lastTrackedObjects;
    }

    @Override
    public String toString() {
        return "CamerasConfigurations{" +
                "id='" + id + '\'' +
                ", frequency='" + frequency + '\'' +
                '}';
    }
}
