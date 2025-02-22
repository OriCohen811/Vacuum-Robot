package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

public class CrashedBroadcast implements Broadcast {
    
    private final MicroService ms;
    private final String errorMsg;
    private final int time;

    public CrashedBroadcast(MicroService ms, String errorMsg , int time){
        this.ms = ms;
        this.errorMsg = errorMsg;
        this.time = time;
    }

    public MicroService getMs() {
        return ms;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public int getTime(){
        return time;
    }
}
