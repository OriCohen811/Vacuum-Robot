package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ServicesManager;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private static StatisticalFolder statistic = StatisticalFolder.getInstance();

    private int TickTime;
    private int Duration;
    private int counter;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.TickTime = TickTime*1000;
        this.Duration = Duration;
        this.counter = 0;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() { 
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
            public void call(TickBroadcast b){
                try{
                    if(counter < Duration && ServicesManager.getInstance().getActive()>2){
                        Thread.sleep(TickTime);
                        counter++;
                        statistic.increaseRuntime();
                        sendBroadcast(new TickBroadcast(counter));
                    }
                    else{
                        sendBroadcast(new TerminatedBroadcast(TimeService.this));
                        terminate();
                        ServicesManager.getInstance().decreaseActive();
                    }
                }catch(InterruptedException e){}
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            public void call(CrashedBroadcast b){
                terminate();
                ServicesManager.getInstance().decreaseActive();
            }
        });

        ServicesManager.getInstance().increaseActive();
        ServicesManager.getInstance().waitUntilAllActive();
        counter++;
        statistic.increaseRuntime();
        sendBroadcast(new TickBroadcast(counter));

    }

    @Override
    public String toString(){
        return "MicroService " + getName() + " , Duration: " + Duration + ", TickTime: " + TickTime; //<-------for test------->
    }
}
