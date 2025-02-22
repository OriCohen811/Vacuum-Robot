package bgu.spl.mics.application.services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.ServicesManager;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.outputError;
import bgu.spl.mics.application.objects.outputObjects;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    
    private final static FusionSlam fusionSlam = FusionSlam.getInstance();
    private final static StatisticalFolder statistic = StatisticalFolder.getInstance();
    private final Queue<TrackedObjectsEvent> waitingEvents;

    /**
     * Constructor for FusionSlamService.
//     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService() {
        super("FusionSlamService");
        waitingEvents = new ConcurrentLinkedQueue<>();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        
        subscribeEvent(TrackedObjectsEvent.class, new Callback<TrackedObjectsEvent>() {
            public void call(TrackedObjectsEvent e){
                if (fusionSlam.validTime(e.getTime())) {
                    handleTrackedObjectsEvent(e);
                }
                else{
                    waitingEvents.add(e);
                }
            }
        });

        subscribeEvent(PoseEvent.class, new Callback<PoseEvent>() {
            public void call(PoseEvent e){
                    fusionSlam.addPose(e.getPose());
                    complete(e, true);
                    while(!waitingEvents.isEmpty() && fusionSlam.validTime(waitingEvents.peek().getTime())){    
                        handleTrackedObjectsEvent(waitingEvents.poll());
                    }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            public void call(TerminatedBroadcast b){
                if(b.getMs() instanceof TimeService){
                    ServicesManager.getInstance().decreaseActive();
                    ServicesManager.getInstance().waitUntilAllOthersInactive();
                    
                    List<LandMark> landMarksList = fusionSlam.getLandmarksList();
                    statistic.addLandmarks(landMarksList.size());
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try (FileWriter writer = new FileWriter("output_file.json")) {
                        outputObjects output = new outputObjects(new ArrayList<LandMark>(landMarksList));
                        gson.toJson(output, writer);
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                       
                    terminate();
                }
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            public void call(CrashedBroadcast b){
                ServicesManager.getInstance().decreaseActive();
                ServicesManager.getInstance().waitUntilAllOthersInactive();

                List<LandMark> landMarksList = fusionSlam.getLandmarksList();
                statistic.addLandmarks(landMarksList.size());
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer = new FileWriter("OutputError.json")) {
                    outputError output = outputError.getInstance();
                    output.setLandmarks(landMarksList);
                    output.setPoses(fusionSlam.getPosesList());
                    gson.toJson(output, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                terminate();
            }
        });
        ServicesManager.getInstance().increaseActive();
    }

    private void handleTrackedObjectsEvent(TrackedObjectsEvent e){
        List<TrackedObject> trackedObjects = e.getTrackedObjectsList();
        fusionSlam.getHandleTrackedObjectsList(trackedObjects);
        complete(e, true);
    }

    @Override
    public String toString(){
        return "MicroService " + getName();
    }
}
