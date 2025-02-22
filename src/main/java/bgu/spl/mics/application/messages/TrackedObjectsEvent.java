package bgu.spl.mics.application.messages;

import java.util.List;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<Boolean> {
    
    private List<TrackedObject> trackedObjs;
    private int time;

    public TrackedObjectsEvent(List<TrackedObject> trackedObjs, int currentTime){
        this.trackedObjs = trackedObjs;
        this.time = currentTime;
    }

    public List<TrackedObject> getTrackedObjectsList() {
        return this.trackedObjs;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() { //<------------------------for test-------------------->
        StringBuilder output = new StringBuilder();
        trackedObjs.forEach(a -> output.append(a.toString()));
        return output.toString();
    }
}
