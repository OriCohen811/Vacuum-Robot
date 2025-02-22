package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private final List<Pose> poseList;
    
    //should be singleton? synchronized necessary?
    public GPSIMU(int currentTick, List<Pose> poseList){
        this.currentTick = currentTick;
        this.status = STATUS.UP;
        this.poseList = poseList;
    }

    public synchronized Pose getPoseAtTime(int currentTick){
        this.currentTick = currentTick;
        if(currentTick>=poseList.size()){
            status = STATUS.DOWN;
            return null;
        }
        else{
            return this.poseList.get(this.currentTick);
        }
    }

    public synchronized STATUS getStatus(){
        return this.status;
    }

    public synchronized int getCurrentTick(){
        return this.currentTick;
    }

}
