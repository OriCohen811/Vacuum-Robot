package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    
    private int id;
    private int frequency;
    private String camera_key;

    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;
    private int lastIndex;
    private StampedDetectedObjects lastCamerasFrame;

    public Camera(int id, int frequency, String camera_key){
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.camera_key = camera_key;
        detectedObjectsList = new ArrayList<StampedDetectedObjects>();
        lastIndex = 0;
        lastCamerasFrame = null;
    }

    public Camera(int id, int frequency, String camera_key, List<StampedDetectedObjects> detectedObjectsList){
        this(id, frequency, camera_key);
        putDetectedObjectsList(detectedObjectsList);
    }

    private StampedDetectedObjects getStampedDetectedObjectsAtTime(int time){
        if(lastIndex < detectedObjectsList.size() && detectedObjectsList.get(lastIndex).getTime() == time){
            StampedDetectedObjects output =  detectedObjectsList.get(lastIndex);
            lastIndex++;
            return output;
        }
        else{
            if(lastIndex == detectedObjectsList.size()){
                finished();
            }
            return null;
        }
    }

    public StampedDetectedObjects getStampedDetectedObjects(int time){
        if(status == STATUS.DOWN){
            return null;
        }
        StampedDetectedObjects detectedObjs = getStampedDetectedObjectsAtTime(time);
        if (detectedObjs != null && !detectedObjs.isEmpty()) {
            DetectedObject error = recognizeError(detectedObjs);
            if(error==null){
                lastCamerasFrame = detectedObjs;
                return detectedObjs;
            }
            else{
                error();
                List<DetectedObject> errDetObj = new LinkedList<>();
                errDetObj.add(error);
                StampedDetectedObjects err = new StampedDetectedObjects(time, errDetObj);
                return err;
            }
        }
        else{
            return null;
        }
    }
    
    private DetectedObject recognizeError(StampedDetectedObjects SDO){
        for(DetectedObject obj: SDO.getDetectedObjectsList()){
            if(obj.getId().equals("ERROR")){
                error();
                return obj;
            }
        }
        return null;
    }

    public void putDetectedObjectsList(List<StampedDetectedObjects> detectedObjectsList){
        this.detectedObjectsList = detectedObjectsList;
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getCamera_key() {
        return camera_key;
    }

    public STATUS getStatus(){
        return status;
    }

    private void finished(){
        status = STATUS.DOWN;
    }

    private void error(){
        status = STATUS.ERROR;
    }

    public StampedDetectedObjects getLastCamerasFrame() {
        return lastCamerasFrame;
    }

    @Override
    public String toString() {
        return "CamerasConfigurations{" +
                "id='" + id + "\'" +
                ", frequency='" + frequency + '\'' +
                ", camera_key='" + camera_key + '\'' +
                '}';
    }

    public String detectedObjectsListToString(){ //<-----------for test-------->
        StringBuilder output = new StringBuilder();
        detectedObjectsList.forEach(a -> output.append('\n' + a.toString()));
        return output.toString();
    }
}
