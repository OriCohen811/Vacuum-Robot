package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

/**
 * TerminatedBroadcast should include the MicroService that terminated
 */
public class TerminatedBroadcast implements Broadcast{
    private MicroService _ms;

    public TerminatedBroadcast(MicroService ms){
        this._ms = ms;
    }

    public MicroService getMs(){
        return _ms;
    }
}
