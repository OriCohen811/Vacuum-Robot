package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    private Map<String,LandMark> landmarks;
    private Map<Integer,Pose> poses;

    private FusionSlam(){
        landmarks = new ConcurrentHashMap<String,LandMark>();
        poses = new ConcurrentHashMap<Integer,Pose>();
    }

    /** 
     * @PRE For each obj in trackedObjects :  poses.containsKey(obj.getTime()) == true
     * @POST new-lankmarks.size() == old-landmarks.size() + unique
     * @return amount of new unique land marks (for test)
    */
    public int getHandleTrackedObjectsList(List<TrackedObject> trackedObjects){
        int unique = 0;
        for(TrackedObject trackedObject: trackedObjects){
            List<CloudPoint> globalCoor = toGlobalCoordinates(trackedObject);
            LandMark landMark = new LandMark(trackedObject.getId(), trackedObject.getDescription() ,globalCoor);
            if(addLandMark(landMark)){
                unique++;
            }
        }
        return unique;
    }

    /**
     * @param landMark
     * @return {@code true} if landMark is unique, otherwise {@code false}
     */
    private boolean addLandMark(LandMark landMark){
        if (!landmarks.containsKey(landMark.getID())) {
            landmarks.put(landMark.getID(), landMark);
            return true;
        } else {
            List<CloudPoint> newList = landMark.getCoordinates();
            List<CloudPoint> oldList = landmarks.get(landMark.getID()).getCoordinates();

            int minSize = Math.min(oldList.size(), newList.size());

            for (int i=0; i<minSize; i++) {
                oldList.get(i).updateAvgX(newList.get(i).getX());
                oldList.get(i).updateAvgY(newList.get(i).getY());
            }
            
            if(minSize <oldList.size()){
                oldList.addAll(minSize, newList);
            }
            
            return false;
        }
    }

    public void addPose(Pose pose){
        poses.putIfAbsent(pose.getTime(), pose);
    }

    private Pose getCurrentPoseAtTime(int time){
        return poses.get(time);
    }

    private List<CloudPoint> toGlobalCoordinates(TrackedObject trackedObject) {
        Pose pose = getCurrentPoseAtTime(trackedObject.getTime());
        List<CloudPoint> globalPoints = new ArrayList<>();
        double radYaw = pose.getYam()*Math.PI / 180;
        double cosYam = Math.cos(radYaw);
        double sinYam = Math.sin(radYaw);

        for (CloudPoint point : trackedObject.getCoordinates()) {
            double xGlobal = pose.getX() + (cosYam * point.getX() - sinYam * point.getY());
            double yGlobal = pose.getY() + (sinYam * point.getX() + cosYam * point.getY());
            globalPoints.add(new CloudPoint(xGlobal, yGlobal));
        }

        return globalPoints;
    }

    public static FusionSlam getInstance(){
        return FusionSlamHolder.instance;
    }

    
    public List<LandMark> getLandmarksList() {
        return new ArrayList<>(landmarks.values());
    }

    public boolean validTime(int time){
        return poses.containsKey(time);
    }

    public List<Pose> getPosesList(){
        return new ArrayList<>(poses.values());
    }

}
