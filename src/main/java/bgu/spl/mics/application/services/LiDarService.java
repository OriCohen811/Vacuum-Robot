package bgu.spl.mics.application.services;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Pair;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.ServicesManager;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.outputError;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker LiDarWorkerTracker;
    private int _currTime;
    private final Queue<DetectObjectsEvent> _waitingDetectObjectsEvents;
    private final static StatisticalFolder statistic = StatisticalFolder.getInstance();

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDarWorkerTracker" + LiDarWorkerTracker.getId());
        this.LiDarWorkerTracker = LiDarWorkerTracker;
        _currTime = 0;
        _waitingDetectObjectsEvents = new ConcurrentLinkedQueue<DetectObjectsEvent>();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
            public void call(TickBroadcast b){

                _currTime = b.getCurrTime();
                while(!_waitingDetectObjectsEvents.isEmpty() && readyToSended(_waitingDetectObjectsEvents.peek().getTime())){
                    DetectObjectsEvent e = _waitingDetectObjectsEvents.poll();
                    sendTrackedObjectsEvent(e);
                }
                if(LiDarWorkerTracker.getStatus()==STATUS.DOWN && _waitingDetectObjectsEvents.isEmpty()){
                    terminate();
                    ServicesManager.getInstance().decreaseActive();
                }
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
        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            public void call(CrashedBroadcast b){
                LiDarWorkerTracker.updatesLastTrackedObjects(b.getTime());
                terminate();
                outputError.getInstance().addLastLiDarWorkerTrackersFrame(getLastFrameList());
                ServicesManager.getInstance().decreaseActive();
            }
        });
        subscribeEvent(DetectObjectsEvent.class, new Callback<DetectObjectsEvent>() {
            public void call(DetectObjectsEvent e){
                LiDarWorkerTracker.updatesLastTrackedObjects(e.getTime());
                if(readyToSended(e.getTime())){
                    sendTrackedObjectsEvent(e);
                }
                else{
                    _waitingDetectObjectsEvents.add(e);
                }
            }
        });
        ServicesManager.getInstance().increaseActive();
    }

    private void sendTrackedObjectsEvent(DetectObjectsEvent e){
        if(LiDarWorkerTracker.getStatus()==STATUS.DOWN || LiDarWorkerTracker.getStatus()==STATUS.ERROR){
            return;
        }
        TrackedObjectsEvent TOE = LiDarWorkerTracker.getTrackedObjectsEvent(e);
        if (TOE.getTime() == -1) {
            complete(e, false);
            sendBroadcast(new CrashedBroadcast(LiDarService.this, TOE.getTrackedObjectsList().get(0).getId(), e.getTime()));
            
            outputError outErr = outputError.getInstance();
            outErr.setError(TOE.getTrackedObjectsList().get(0).getDescription());
            outErr.setFaultySensor(LiDarService.this);
            outErr.addLastLiDarWorkerTrackersFrame(getLastFrameList());

            terminate();
            ServicesManager.getInstance().decreaseActive();
        } else {
            complete(e, true);
            statistic.addTrackedObj(TOE.getTrackedObjectsList().size());
            sendEvent(TOE);
        }
    }

    /** 
     * @param time : for DetectObjectsEvent, time represents the time that is detected
     * @return {@code true} if LiDarWorkerTracker synchronized for time, otherwise {@code false}
     */
    private boolean readyToSended(int time){
        return _currTime >= time +LiDarWorkerTracker.getFrequency();
    }

    public  Pair<String, List<TrackedObject>> getLastFrameList(){
        String name = "LiDarWorkerTracker" + LiDarWorkerTracker.getId();
        return new Pair<String,List<TrackedObject>>(name, LiDarWorkerTracker.getLastTrackedObjects());
    }
    
    @Override
    public String toString(){
        return "MicroService " + getName();
    } 
}
