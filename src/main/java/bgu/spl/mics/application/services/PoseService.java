package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.ServicesManager;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
            public void call(TickBroadcast b){
                Pose pose = gpsimu.getPoseAtTime(b.getCurrTime());
                if(pose!=null){
                    sendEvent(new PoseEvent(pose));
                }
                else{
                    sendBroadcast(new TerminatedBroadcast(PoseService.this));
                    terminate();
                    ServicesManager.getInstance().decreaseActive();
                }
            }

        });
        
        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            public void call(CrashedBroadcast b){
                terminate();
                ServicesManager.getInstance().decreaseActive();
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            public void call(TerminatedBroadcast b){
                if(b.getMs() instanceof TimeService){
                    terminate();
                    ServicesManager.getInstance().decreaseActive();
                }
            }
        });
        ServicesManager.getInstance().increaseActive();
    }

    @Override
    public String toString(){
        return "MicroService " + getName();
    }
}
