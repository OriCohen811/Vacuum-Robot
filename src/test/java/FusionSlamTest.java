
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class FusionSlamTest {

    private FusionSlam fusionSlam = FusionSlam.getInstance();

    @BeforeEach
    void setUp() {}

    @Test
    void testGetHandleTrackedObjectsList() {
        
        int oldLandmarksize = fusionSlam.getLandmarksList().size();

        Pose pose = new Pose(4, (float) 10.0, (float) 10.0, (float) Math.PI / 3);
        fusionSlam.addPose(pose);
        
        List<CloudPoint> localCoordinates = new ArrayList<>();
        localCoordinates.add(new CloudPoint(2.0, 2.0));

        TrackedObject trackedObject = new TrackedObject("Wall_1", 4, "AddLandMark", localCoordinates);
        List<TrackedObject> trackedObjectList = new ArrayList<>();
        trackedObjectList.add(trackedObject);
        
        int unique = fusionSlam.getHandleTrackedObjectsList(trackedObjectList);
        int newSize = oldLandmarksize + unique;

        assertEquals(newSize, fusionSlam.getLandmarksList().size());
        assertEquals("Wall_1", fusionSlam.getLandmarksList().get(fusionSlam.getLandmarksList().size() - 1).getID());
        assertEquals(1, fusionSlam.getLandmarksList().get(fusionSlam.getLandmarksList().size() - 1).getCoordinates().size());
        assertEquals("AddLandMark", fusionSlam.getLandmarksList().get(fusionSlam.getLandmarksList().size() - 1).getDescription());
    }


    @Test
    void testUpdateExistingLandmark() {
        int oldLandmarksize = fusionSlam.getLandmarksList().size();
        
        Pose pose = new Pose(5, (float)0.0, (float)0.0, (float)0.0);
        fusionSlam.addPose(pose);

        List<CloudPoint> localCoordinates1 = new ArrayList<>();
        localCoordinates1.add(new CloudPoint(3.0, 3.0));

        List<CloudPoint> localCoordinates2 = new ArrayList<>();
        localCoordinates1.add(new CloudPoint(4.0, 4.0));

        TrackedObject trackedObject1 = new TrackedObject("3", 5, "UpdateExistingLandmark", localCoordinates1);
        TrackedObject trackedObject2 = new TrackedObject("3",5, "UpdateExistingLandmark",  localCoordinates2);

        List<TrackedObject> trackedObjectList1 = new ArrayList<>();
        List<TrackedObject> trackedObjectList2 = new ArrayList<>();

        trackedObjectList1.add(trackedObject1);
        trackedObjectList2.add(trackedObject2);

        int i = fusionSlam.getHandleTrackedObjectsList(trackedObjectList1);
        int j = fusionSlam.getHandleTrackedObjectsList(trackedObjectList2);

        assertEquals(0, j);
        assertEquals(oldLandmarksize + i, fusionSlam.getLandmarksList().size());

        LandMark updatedLandmark = fusionSlam.getLandmarksList().get(fusionSlam.getLandmarksList().size()-1);

        assertEquals(2, updatedLandmark.getCoordinates().size());
    }

    @Test
    void testSingletonInstance() {
        FusionSlam anotherInstance = FusionSlam.getInstance();
        assertSame(fusionSlam, anotherInstance);
    }
}
