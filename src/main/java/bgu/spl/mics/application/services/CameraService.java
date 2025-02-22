package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Pair;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.ServicesManager;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.outputError;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private final Camera camera;
    private final StampedDetectedObjects[] waitingStampedDetectedObjects;
    private final static StatisticalFolder statistic = StatisticalFolder.getInstance();
    private int counterRoundToEnd = -1;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("Camera" + camera.getId());
        this.camera = camera;
        waitingStampedDetectedObjects = new StampedDetectedObjects[this.camera.getFrequency()];
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {

        if(camera.getFrequency()>0){
            subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
                public void call(TickBroadcast b){
                    StampedDetectedObjects prevDetectedObjs = waitingStampedDetectedObjects[b.getCurrTime()%camera.getFrequency()];
                    if(prevDetectedObjs!=null){
                        statistic.addDetectedObj(prevDetectedObjs.getDetectedObjectsList().size());
                        DetectObjectsEvent DOE = new DetectObjectsEvent(prevDetectedObjs.getTime(), prevDetectedObjs);
                        sendEvent(DOE);
                    }
                    
                    waitingStampedDetectedObjects[b.getCurrTime()%camera.getFrequency()] = null;

                    StampedDetectedObjects detectedObjs = camera.getStampedDetectedObjects(b.getCurrTime());
                    if (camera.getStatus() == STATUS.ERROR){
                        DetectedObject error = detectedObjs.getDetectedObjectsList().get(0);
                        sendBroadcast(new CrashedBroadcast(CameraService.this, error.getDescription(), b.getCurrTime()));
                        terminate();
                        
                        outputError outErr = outputError.getInstance();
                        outErr.setError(error.getDescription());
                        outErr.setFaultySensor(CameraService.this);
                        outErr.addLastCamerasFrame(getLastFrameList());
                        
                        ServicesManager.getInstance().decreaseActive();
                    }
                    else if(detectedObjs != null){
                        waitingStampedDetectedObjects[b.getCurrTime()%camera.getFrequency()] = detectedObjs;
                    }

                    if(camera.getStatus()==STATUS.DOWN){
                        if(counterRoundToEnd==0){
                            sendBroadcast(new TerminatedBroadcast(CameraService.this));
                            terminate();
                            ServicesManager.getInstance().decreaseActive();
                        }
                        else if(counterRoundToEnd==-1){ 
                            counterRoundToEnd = camera.getFrequency()-1;
                        }
                        else{
                            counterRoundToEnd--;
                        }
                    }
                }
            });
        }
        else{
            subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
                public void call(TickBroadcast b){
                    StampedDetectedObjects detectedObjs = camera.getStampedDetectedObjects(b.getCurrTime());
                    if(camera.getStatus() == STATUS.ERROR){
                        DetectedObject error = detectedObjs.getDetectedObjectsList().get(0);
                        sendBroadcast(new CrashedBroadcast(CameraService.this, error.getDescription(), b.getCurrTime()));
                        terminate();

                        outputError outErr = outputError.getInstance();
                        outErr.setError(error.getDescription());
                        outErr.setFaultySensor(CameraService.this);
                        outErr.addLastCamerasFrame(getLastFrameList());

                        ServicesManager.getInstance().decreaseActive();
                    }
                    else if(detectedObjs!=null){
                        statistic.addDetectedObj(detectedObjs.getDetectedObjectsList().size());
                        DetectObjectsEvent DOE = new DetectObjectsEvent(detectedObjs.getTime(), detectedObjs);
                        sendEvent(DOE);
                    }

                    if(camera.getStatus()==STATUS.DOWN){
                        sendBroadcast(new TerminatedBroadcast(CameraService.this));
                        terminate();
                        ServicesManager.getInstance().decreaseActive();
                    }
                }
            });
        }
        

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
                terminate();
                outputError.getInstance().addLastCamerasFrame(getLastFrameList());
                ServicesManager.getInstance().decreaseActive();
            }
        });
        ServicesManager.getInstance().increaseActive();
    }

    private Pair<String,StampedDetectedObjects> getLastFrameList(){
        return new Pair<String,StampedDetectedObjects>(camera.getCamera_key(), camera.getLastCamerasFrame());
    }

    @Override
    public String toString(){
        return "MicroService " + getName(); //<-------for test------->
    }
}
