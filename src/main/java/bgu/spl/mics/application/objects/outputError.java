package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Pair;

public class outputError{

    private static class SingletonHolder {
        private static final outputError instance = new outputError();
    }

    @SerializedName("error")
    private String error;
    @SerializedName("faultySensor")
    private String faultySensor;
    @SerializedName("lastCamerasFrame")
    private Map<String, StampedDetectedObjects> lastCamerasFrame;
    @SerializedName("lastLidarWorkerTrackersFrame")
    private Map<String, List<TrackedObject>> lastLiDarWorkerTrackersFrame;
    @SerializedName("poses")
    private List<Pose> poses;
    @SerializedName("statistics")
    private final StatisticalFolder statistic;
    @SerializedName("landmarks")
    private List<LandMark> landmarks;

    private outputError(){
        lastCamerasFrame = new HashMap<>();
        lastLiDarWorkerTrackersFrame = new HashMap<>();
        poses = new ArrayList<>();
        landmarks = new ArrayList<>();
        statistic = StatisticalFolder.getInstance();
    }  

    public void setError(String error) {
        this.error = error;
    }

    public void setFaultySensor(MicroService faultySensor) {
        this.faultySensor = faultySensor.getName();
    }

    public void addLastCamerasFrame(Pair<String,StampedDetectedObjects> lastCamerasFrame) {
        this.lastCamerasFrame.put(lastCamerasFrame.getKey(), lastCamerasFrame.getValue());
    }

    public void addLastLiDarWorkerTrackersFrame(Pair<String,List<TrackedObject>> lastLiDarWorkerTrackersFrame) {
        this.lastLiDarWorkerTrackersFrame.put(lastLiDarWorkerTrackersFrame.getKey(), lastLiDarWorkerTrackersFrame.getValue());
    }
    
    public void setPoses(List<Pose> poses) {
        this.poses = poses;
    }

    public void setLandmarks(List<LandMark> landmarks) {
        this.landmarks = landmarks;
    }

    public String getError() {
        return error;
    }
    public String getFaultySensor() {
        return faultySensor;
    }

    public Object getLastFrame() {
        return lastCamerasFrame;
    }
        
    public Object getlastCamerasFrame() {
        return lastCamerasFrame;
    }
    
    public Object getLastLiDarWorkerTrackersFrame() {
        return lastLiDarWorkerTrackersFrame;
    }

    public List<Pose> getPoses() {
        return poses;
    }
    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    // @Override
    // public String toString() {
    //     return  "outputError{" + 
    //             "error='" + error + '\'' + 
    //             ", faultySensor='" + faultySensor + '\'' + 
    //             ", lastCamerasFrame=" + lastCamerasFrame +  
    //             ", lastLiDarWorkerTrackersFrame=" + lastLiDarWorkerTrackersFrame +
    //             ", poses=" + poses + 
    //             ", statistics=" + statistic +
    //             ", landMarks=" + landmarks +
    //             '}';
    // }

    public static outputError getInstance() {
        return SingletonHolder.instance;
    }
}