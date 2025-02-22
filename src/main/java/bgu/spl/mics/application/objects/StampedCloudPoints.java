package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {

    private int time;
    private String id;
    private List<List<Double>> cloudPoints;

    public StampedCloudPoints(int time, String id, List<List<Double>> coordinates){
        this.time = time;
        this.id = id;
        this.cloudPoints = coordinates;
        
    }

    public String getID(){
        return id;
    }

    public int getTime(){
        return time;
    } 

    public List<List<Double>> getCoordinatesList(){
        return cloudPoints;
    }

    @Override
    public String toString(){
        return "CloudPoint{" +
                "time='" + time + 
                "id='" + id + 
                "cloudPoints=" + cloudPoints + 
                "}";
    }

}
