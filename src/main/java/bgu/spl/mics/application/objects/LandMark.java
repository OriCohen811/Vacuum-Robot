package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private String id;
    private String description;
    private List<CloudPoint> coordinates;

    public LandMark(String id, String description, List<CloudPoint> coordinates){
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getID(){
        return this.id;
    }

    public String getDescription(){
        return this.description;
    }

    public List<CloudPoint> getCoordinates(){
        return this.coordinates;
    }

    @Override
    public String toString(){
        return  "LandMark{" + "id='" + id + ", description='" + description + ", coordinates='" + coordinates + '}';
    }
}
