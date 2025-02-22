package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint {

    private double x;
    private double y;

    public CloudPoint(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }

    public void updateAvgX(double otherX){
        this.x = (this.x + otherX)/2;
    }

    public void updateAvgY(double otherY){
        this.y = (this.y + otherY)/2;
    }

    @Override
    public String toString(){
        return "CloudPoint{" +
                "x='" + x + 
                ", y='" + y + 
                "}";
    }


}
