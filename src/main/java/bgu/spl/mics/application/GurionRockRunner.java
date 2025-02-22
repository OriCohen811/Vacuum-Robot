package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.ServicesManager;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the
     *             path to the configuration file.
     */
    public static void main(String[] args) {
        if(args.length<1){
            throw new IllegalArgumentException("Invalid configuration file");
        }
        ServicesManager servManager  = ServicesManager.getInstance();
        String configFile = args[0];
        System.out.println("Loading configuration file: "+configFile);
        Objects objectsList = jsonToObjects(configFile);

        int TickTime = objectsList.getTickTime();
        int Duration = objectsList.getDuration();
        servManager.increaseTotal();
        TimeService timeService = new TimeService(TickTime, Duration);
        Thread timeThread = new Thread(timeService);

        String poseJsonFile = fixPath(configFile, objectsList.getPoseJsonFile());
        List<Pose> poseList = jsonToPosesList(poseJsonFile);
        GPSIMU gpsimu = new GPSIMU(0, poseList);
        PoseService poseService = new PoseService(gpsimu);
        servManager.increaseTotal();
        Thread pose_Thread = new Thread(poseService);

        LiDarWorkers LiDarWorkers = objectsList.getLiDarWorkers();
        String lidars_data_path = fixPath(configFile, LiDarWorkers.getLidars_data_path());
        List<LidarConfigurations> lidarsList = LiDarWorkers.getLidarConfigurations();
        servManager.increaseTotal(lidarsList.size());
        Thread[] lidarsThreadArray = new Thread[lidarsList.size()];
        int i = 0;
        for (LidarConfigurations lidarConfig : lidarsList) {
            LiDarService liDarService = new LiDarService( new LiDarWorkerTracker(lidarConfig.getId(), lidarConfig.getFrequency(), lidars_data_path));
            lidarsThreadArray[i] = new Thread(liDarService);
            i++;
        }

        Cameras Cameras = objectsList.getCameras();
        String camera_datas_path = fixPath(configFile, Cameras.getCamera_datas_path());
        Map<String, List<StampedDetectedObjects>> camerasDataMap = jsonToStampedDetectedObjectsList(camera_datas_path);
        List<Camera> CamerasConfigurations = Cameras.getCamerasConfigurations();
        servManager.increaseTotal(CamerasConfigurations.size());
        Thread[] cameraThreadArray = new Thread[CamerasConfigurations.size()];
        i = 0;
        for (Camera camera : CamerasConfigurations) {
            camera.putDetectedObjectsList(camerasDataMap.get(camera.getCamera_key()));
            CameraService cameraService = new CameraService(camera);
            cameraThreadArray[i] = new Thread(cameraService);
            i++;
        }

        FusionSlamService fusionSlamService = new FusionSlamService();
        servManager.increaseTotal();
        Thread fusionSLAM_Thread = new Thread(fusionSlamService);

        fusionSLAM_Thread.start();
        pose_Thread.start();
        for(Thread thread: lidarsThreadArray){
            thread.start();
        }
        for(Thread thread: cameraThreadArray){
            thread.start();
        }

        timeThread.start();


    }

    public static String fixPath(String configFile, String path){
        StringBuilder s = new StringBuilder();
        for(int i=configFile.length()-1; i>=0; i--){
            if(configFile.charAt(i)=='/' || configFile.charAt(i)=='\\'){
                s.append(configFile.substring(0, i));
                break;
            }
        }
        s.append(path.substring(1));
        return s.toString();
    }

    private static Map<String, List<StampedDetectedObjects>> jsonToStampedDetectedObjectsList(String camera_datas_path) {

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(camera_datas_path)) {
            Type mapType = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            return gson.fromJson(reader, mapType);
        }catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("camera_datas_path invalid ");
        }

    }

    private static Objects jsonToObjects(String configuration_file) {
        Objects objectsList = null;

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configuration_file)) {
            objectsList = gson.fromJson(reader, Objects.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("configuration_file invalid ");
        }
        return objectsList;
    }

    private static List<Pose> jsonToPosesList(String poseJsonFile) {

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(poseJsonFile)) {
            Type poseListType = new TypeToken<List<Pose>>() {}.getType();
            List<Pose> posesList = gson.fromJson(reader, poseListType);
            posesList.add(0, null); // represent time 0
            return posesList;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("poseJsonFile invalid ");
        }
    }

    protected class Objects {
        private Cameras Cameras;
        private LiDarWorkers LiDarWorkers;
        private String poseJsonFile;
        private int TickTime;
        private int Duration;

        public Cameras getCameras() {
            return Cameras;
        }

        public LiDarWorkers getLiDarWorkers() {
            return LiDarWorkers;
        }

        public String getPoseJsonFile() {
            return poseJsonFile;
        }

        public int getTickTime() {
            return TickTime;
        }

        public int getDuration() {
            return Duration;
        }

        @Override
        public String toString() {
            return "Objects{" +
                    "Cameras='" + Cameras + '\'' +
                    ", LiDarWorkers='" + LiDarWorkers + '\'' +
                    ", poseJsonFile='" + poseJsonFile + '\'' +
                    ", TickTime='" + TickTime + '\'' +
                    ", Duration='" + Duration + '\'' +
                    '}';

        }
    }

    protected class Cameras {
        private List<Camera> CamerasConfigurations;
        private String camera_datas_path;

        public List<Camera> getCamerasConfigurations() {
            return CamerasConfigurations;
        }

        public String getCamera_datas_path() {
            return camera_datas_path;
        }

        public String toString() {
            return "Cameras{" +
                    "CamerasConfigurations=" + CamerasConfigurations + '\'' +
                    ", camera_datas_path='" + camera_datas_path +
                    '}';
        }
    }

    protected class LiDarWorkers {
        private List<LidarConfigurations> LidarConfigurations;
        private String lidars_data_path;

        public List<LidarConfigurations> getLidarConfigurations() {
            return LidarConfigurations;
        }

        public String getLidars_data_path() {
            return lidars_data_path;
        }

        public String toString() {
            return "LiDarWorkers{" +
                    "LidarConfigurations=" + LidarConfigurations + '\'' +
                    ", lidars_data_path='" + lidars_data_path +
                    '}';
        }
    }

    protected class LidarConfigurations {
        private int id;
        private int frequency;

        public int getId() {
            return id;
        }

        public int getFrequency() {
            return frequency;
        }

        @Override
        public String toString() {
            return "CamerasConfigurations{" +
                    "id='" + id + '\'' +
                    ", frequency='" + frequency + '\'' +
                    '}';
        }
    }

}
