

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.objects.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Preparation process: 
 * - get StampedDetectedObjects from waitingStampedDetectedObjects array
 */
class CameraTest {

    private Camera camera;
    private List<StampedDetectedObjects> detectedObjectsList;

    @BeforeEach
    void setUp() {
        detectedObjectsList = new ArrayList<>();

        List<DetectedObject> objects1 = new ArrayList<>();
        objects1.add(new DetectedObject("1", "Wall"));
        objects1.add(new DetectedObject("2", "Chair"));

        List<DetectedObject> objects2 = new ArrayList<>();
        objects2.add(new DetectedObject("3", "Table"));

        detectedObjectsList.add(new StampedDetectedObjects(1, objects1));
        detectedObjectsList.add(new StampedDetectedObjects(2, objects2));

        camera = new Camera(1, 2, "camera1", detectedObjectsList);
    }

    @Test
    void testPreparationProcess() {
        
        StampedDetectedObjects frame1 = camera.getStampedDetectedObjects(1);
        assertNotNull(frame1);
        assertEquals(2, frame1.getDetectedObjectsList().size());

        StampedDetectedObjects frame2 = camera.getStampedDetectedObjects(2);
        assertNotNull(frame2);
        assertEquals(1, frame2.getDetectedObjectsList().size());

        StampedDetectedObjects frameNull = camera.getStampedDetectedObjects(3);
        assertNull(frameNull);


    }

}
