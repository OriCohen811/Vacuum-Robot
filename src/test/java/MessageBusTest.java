
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusTest {

    private MessageBus messageBus = MessageBusImpl.getInstance();
    private MicroService microServiceA;
    private MicroService microServiceB;

    @BeforeEach
    void setUp() {
        microServiceA = new MicroService("ServiceA") {
            @Override
            protected void initialize() {}
        };
        microServiceB = new MicroService("ServiceB") {
            @Override
            protected void initialize() {}
        };
    }

    @Test
    void testRegister() {
        messageBus.register(microServiceA);
        messageBus.register(microServiceB);

        assertEquals(true, messageBus.isRegistered(microServiceA));
        assertEquals(true, messageBus.isRegistered(microServiceB));
    }

    @Test
    void testSubscribeBroadcast() throws InterruptedException {
        

        messageBus.register(microServiceA);
        messageBus.register(microServiceB);

        messageBus.subscribeBroadcast(TickBroadcast.class, microServiceA);
        messageBus.subscribeBroadcast(CrashedBroadcast.class, microServiceB);

        assertEquals(true, messageBus.isRegisteredToBroacast(microServiceA, TickBroadcast.class));
        assertEquals(true, messageBus.isRegisteredToBroacast(microServiceB, CrashedBroadcast.class));

    }

    @Test
    void testRoundRobinEventDistribution() throws InterruptedException {
        class TestEvent implements Event<String> {}

        messageBus.register(microServiceA);
        messageBus.register(microServiceB);
        messageBus.subscribeEvent(TestEvent.class, microServiceA);
        messageBus.subscribeEvent(TestEvent.class, microServiceB);

        TestEvent event1 = new TestEvent();
        TestEvent event2 = new TestEvent();

        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);

        assertEquals(true, messageBus.isFirstAtQueue(event1,microServiceA));
        assertEquals(true, messageBus.isFirstAtQueue(event2,microServiceB));
    }

    void testUnRegister() {
        messageBus.unregister(microServiceA);
        messageBus.unregister(microServiceB);

        assertEquals(false, messageBus.isRegistered(microServiceA));
        assertEquals(false, messageBus.isRegistered(microServiceB));
    }
}
