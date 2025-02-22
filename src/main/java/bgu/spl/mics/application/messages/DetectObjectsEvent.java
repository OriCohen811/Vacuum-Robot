package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * DetectObjectsEvent represents an Message of object that detected by the camera.
 * It contains information such as the DetectedObject and detection's time.
 */
public class DetectObjectsEvent implements Event<Boolean>{
    private final int time;
    private final StampedDetectedObjects objsAtTime;

    public DetectObjectsEvent(int time, StampedDetectedObjects objsAtTime){
        this.time = time;
        this.objsAtTime = objsAtTime;
    }

    public int getTime(){
        return time;
    }

    public StampedDetectedObjects getStampedDetectedObjects(){
        return objsAtTime;
    }
}
