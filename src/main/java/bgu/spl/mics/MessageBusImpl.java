package bgu.spl.mics;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	private static class SingletonHolder{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private final Map< MicroService, ConcurrentLinkedQueue<Message> > msMessage;
	private final Map< Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService> > brdcstMS;
	private final Map< Class<? extends Event<?>> , ConcurrentLinkedQueue<MicroService> > eventsMS;
	private final Map< Event<?> , Future<?> > eventsFuture;

	private MessageBusImpl(){
		msMessage = new ConcurrentHashMap<>();
		brdcstMS = new ConcurrentHashMap<>();
		eventsMS = new ConcurrentHashMap<>();
		eventsFuture = new ConcurrentHashMap<>();
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventsMS.putIfAbsent(type, new ConcurrentLinkedQueue<MicroService>());
		eventsMS.get(type).add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		brdcstMS.putIfAbsent(type, new ConcurrentLinkedQueue<MicroService>());
		brdcstMS.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		@SuppressWarnings("unchecked")
		Future<T> future = (Future<T>) eventsFuture.get(e);
		future.resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {

		Queue<MicroService> msList = brdcstMS.get(b.getClass());
        if (msList != null) {
            synchronized (msList) {
                for (MicroService ms : msList) {
                    Queue<Message> msQueue = msMessage.get(ms);
                    if (msQueue != null) {
                        synchronized (msQueue) {
                            msQueue.add(b);
                            msQueue.notifyAll();
                        }
                    }
                }
            }
        }
	}
	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {

		Queue<MicroService> msQueue = eventsMS.get(e.getClass());
        MicroService ms;
		Future<T> future;
        synchronized (msQueue) {
			if (msQueue == null || msQueue.isEmpty()) {
				return null;
			}
            ms = msQueue.poll();
            msQueue.add(ms);
			future = new Future<>();
			eventsFuture.put(e, future);
        }

		Queue<Message> msMessages = msMessage.get(ms);
        synchronized (msMessages) {
            msMessages.add(e);
            msMessages.notifyAll();
        }

        return future;
	}

	@Override
	public void register(MicroService m) {
		msMessage.putIfAbsent(m, new ConcurrentLinkedQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		eventsMS.values().forEach(queue -> queue.remove(m));
		brdcstMS.values().forEach(queue -> queue.remove(m));

		synchronized(msMessage){
			msMessage.remove(m);
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Queue<Message> messages = msMessage.get(m);
		synchronized (messages) {
			while (messages.isEmpty()) {
				messages.wait();
			}
			return messages.poll();
		}
	}

	public static MessageBusImpl getInstance(){
		return SingletonHolder.instance;
	}

	//for test
	public boolean isRegistered(MicroService ms){
		return this.msMessage.containsKey(ms);
	}
	
	public boolean isRegisteredToBroacast(MicroService ms, Class<? extends Broadcast> b){
		Queue<MicroService> q = brdcstMS.get(b);
		for(MicroService x : q){
			if(x==ms){
				return true;
			}
		}
		return false;
	}

	public boolean isFirstAtQueue(Event<?> e, MicroService ms){
		return e == msMessage.get(ms).peek();
	}
}