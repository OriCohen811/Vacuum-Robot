package bgu.spl.mics.application.objects;

import java.util.List;

public class outputObjects {

    private final StatisticalFolder statistic = StatisticalFolder.getInstance();
    private List<LandMark> landmarks;

    public outputObjects(List<LandMark> landmarks){
        this.landmarks = landmarks;
    }

    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    @Override
    public String toString() {
        return "outputObjects{" + 
                ", statistics=" + statistic +
                ", landMarks=" + landmarks +
                '}';
                
    }
}
