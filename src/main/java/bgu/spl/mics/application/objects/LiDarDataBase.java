package bgu.spl.mics.application.objects;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private static class SingletonHolder{
        private static LiDarDataBase instance = null;
    }

    private final Map<Integer, List<StampedCloudPoints>> cloudPointsMap;
    private int mostLaterTime;
    

    private LiDarDataBase(List<StampedCloudPoints> cloudPoints){
        mostLaterTime = -1;
        this.cloudPointsMap = new ConcurrentHashMap<>();
        for(StampedCloudPoints SCP : cloudPoints){
            cloudPointsMap.putIfAbsent(SCP.getTime(), new LinkedList<>());
            cloudPointsMap.get(SCP.getTime()).add(SCP);
            mostLaterTime = Math.max(mostLaterTime, SCP.getTime());
        }
        
    }
    
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if(SingletonHolder.instance == null){
            List<StampedCloudPoints> cloudPointsList = jsonToStampedCloudPointsList(filePath);
            SingletonHolder.instance = new LiDarDataBase(cloudPointsList);
        }
        return SingletonHolder.instance;
    }

    private static List<StampedCloudPoints> jsonToStampedCloudPointsList(String lidars_data_path) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(lidars_data_path)) {
            Type listType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
            return gson.fromJson(reader, listType);
        }catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("lidars_data_path invalid ");
        }
    }

    public List<StampedCloudPoints> getStampedCloudPoints(int time){
        return cloudPointsMap.get(time);
    }

    public boolean outOfRange(int time){
        return time>mostLaterTime;
    }

    @Override
    public String toString() {
        return "DataBase " + cloudPointsMap.toString();
    }
}
