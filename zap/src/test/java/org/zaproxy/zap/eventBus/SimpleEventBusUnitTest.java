/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.eventBus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit test for {@link SimpleEventBus}. */
public class SimpleEventBusUnitTest {

    SimpleEventBus seb;

    @BeforeEach
    public void setUp() throws Exception {
        seb = new SimpleEventBus();
    }

    @Test
    public void consumerOfAllEventsShouldReceiveThem() {
        // Given
        TestEventPublisher pub = new TestEventPublisher("Pub1");
        TestEventConsumer cons = new TestEventConsumer();
        Event event1 = new Event(pub, "Event1", null);
        Event event2 = new Event(pub, "Event2", null);
        Event event3 = new Event(pub, "Event3", null);
        // When
        seb.registerPublisher(pub, new String[] {"Event1", "Event2", "Event3"});
        seb.registerConsumer(cons, "Pub1");
        seb.publishSyncEvent(pub, event1);
        seb.publishSyncEvent(pub, event2);
        seb.publishSyncEvent(pub, event3);
        // Then
        assertTrue(cons.getEvents().contains(event1));
        assertTrue(cons.getEvents().contains(event2));
        assertTrue(cons.getEvents().contains(event3));
    }

    @Test
    public void consumerOfSomeEventsShouldReceiveJustThem() {
        // Given
        TestEventPublisher pub = new TestEventPublisher("Pub1");
        TestEventConsumer cons = new TestEventConsumer();
        Event event1 = new Event(pub, "Event1", null);
        Event event2 = new Event(pub, "Event2", null);
        Event event3 = new Event(pub, "Event3", null);
        // When
        seb.registerPublisher(pub, new String[] {"Event1", "Event2", "Event3"});
        seb.registerConsumer(cons, "Pub1", new String[] {"Event1", "Event2"});
        seb.publishSyncEvent(pub, event1);
        seb.publishSyncEvent(pub, event2);
        seb.publishSyncEvent(pub, event3);
        // Then
        assertTrue(cons.getEvents().contains(event1));
        assertTrue(cons.getEvents().contains(event2));
        assertFalse(cons.getEvents().contains(event3));
    }

    @Test
    public void consumerShouldNotReceiveEventsForOtherPublishers() {
        // Given
        TestEventPublisher pub1 = new TestEventPublisher("Pub1");
        TestEventPublisher pub2 = new TestEventPublisher("Pub2");
        TestEventConsumer cons = new TestEventConsumer();
        Event eventp1e1 = new Event(pub1, "Event1", null);
        Event eventp1e2 = new Event(pub1, "Event2", null);
        Event eventp1e3 = new Event(pub1, "Event3", null);
        Event eventp2e1 = new Event(pub2, "Event1", null);
        Event eventp2e2 = new Event(pub2, "Event2", null);
        Event eventp2e3 = new Event(pub2, "Event3", null);
        // When
        seb.registerPublisher(pub1, new String[] {"Event1", "Event2", "Event3"});
        seb.registerPublisher(pub2, new String[] {"Event1", "Event2", "Event3"});
        seb.registerConsumer(cons, "Pub1");
        seb.publishSyncEvent(pub1, eventp1e1);
        seb.publishSyncEvent(pub1, eventp1e2);
        seb.publishSyncEvent(pub1, eventp1e3);
        seb.publishSyncEvent(pub2, eventp2e1);
        seb.publishSyncEvent(pub2, eventp2e2);
        seb.publishSyncEvent(pub2, eventp2e3);
        // Then
        assertTrue(cons.getEvents().contains(eventp1e1));
        assertTrue(cons.getEvents().contains(eventp1e2));
        assertTrue(cons.getEvents().contains(eventp1e3));
        assertFalse(cons.getEvents().contains(eventp2e1));
        assertFalse(cons.getEvents().contains(eventp2e2));
        assertFalse(cons.getEvents().contains(eventp2e3));
    }

    @Test
    public void consumerShouldNotReceiveEventsAfterDisconnectingFromPublisher() {
        // Given
        TestEventPublisher pub = new TestEventPublisher("Pub1");
        TestEventConsumer cons = new TestEventConsumer();
        Event event1 = new Event(pub, "Event1", null);
        Event event2 = new Event(pub, "Event2", null);
        Event event3 = new Event(pub, "Event3", null);
        // When
        seb.registerPublisher(pub, new String[] {"Event1", "Event2", "Event3"});
        seb.registerConsumer(cons, "Pub1");
        seb.publishSyncEvent(pub, event1);
        seb.publishSyncEvent(pub, event2);
        seb.unregisterConsumer(cons, "Pub1");
        seb.publishSyncEvent(pub, event3);
        // Then
        assertTrue(cons.getEvents().contains(event1));
        assertTrue(cons.getEvents().contains(event2));
        assertFalse(cons.getEvents().contains(event3));
    }

    @Test
    public void consumerShouldNotReceiveEventsAfterDisconnectingFromAllPublishers() {
        // Given
        TestEventPublisher pub1 = new TestEventPublisher("Pub1");
        TestEventPublisher pub2 = new TestEventPublisher("Pub2");
        TestEventPublisher pub3 = new TestEventPublisher("Pub3");
        TestEventConsumer cons = new TestEventConsumer();
        Event eventp1e1 = new Event(pub1, "Event1", null);
        Event eventp1e2 = new Event(pub1, "Event2", null);
        Event eventp1e3 = new Event(pub1, "Event3", null);
        Event eventp2e1 = new Event(pub2, "Event1", null);
        Event eventp2e2 = new Event(pub2, "Event2", null);
        Event eventp2e3 = new Event(pub2, "Event3", null);
        Event eventp3e3 = new Event(pub3, "Event3", null);
        // When
        seb.registerPublisher(pub1, new String[] {"Event1", "Event2", "Event3"});
        seb.registerPublisher(pub2, new String[] {"Event1", "Event2", "Event3"});
        seb.registerPublisher(pub3, new String[] {"Event3"});
        seb.registerConsumer(cons, "Pub1");
        seb.registerConsumer(cons, "Pub2");
        seb.publishSyncEvent(pub1, eventp1e1);
        seb.publishSyncEvent(pub1, eventp1e2);
        seb.publishSyncEvent(pub2, eventp2e1);
        seb.publishSyncEvent(pub2, eventp2e2);
        seb.unregisterConsumer(cons);
        seb.publishSyncEvent(pub1, eventp1e3);
        seb.publishSyncEvent(pub2, eventp2e3);
        seb.publishSyncEvent(pub3, eventp3e3);
        // Then
        assertTrue(cons.getEvents().contains(eventp1e1));
        assertTrue(cons.getEvents().contains(eventp1e2));
        assertTrue(cons.getEvents().contains(eventp2e1));
        assertTrue(cons.getEvents().contains(eventp2e2));
        assertFalse(cons.getEvents().contains(eventp1e3));
        assertFalse(cons.getEvents().contains(eventp2e3));
        assertFalse(cons.getEvents().contains(eventp3e3));
    }

    @Test
    public void consumersShouldReceiveEventsEvenIfRegisteredBeforePublisher() {
        // Given
        TestEventConsumer consumer1 = new TestEventConsumer();
        TestEventConsumer consumer2 = new TestEventConsumer();
        seb.registerConsumer(consumer1, "publisher");
        seb.registerConsumer(consumer2, "publisher");
        TestEventPublisher publisher = new TestEventPublisher("publisher");
        Event event = new Event(publisher, "event", null);
        // When
        seb.registerPublisher(publisher, new String[] {"event"});
        seb.publishSyncEvent(publisher, event);
        // Then
        assertThat(consumer1.getEvents(), contains(event));
        assertThat(consumer2.getEvents(), contains(event));
    }

    @Test
    public void consumerShouldBeAbleToRemoveItselfDuringEventConsumption() {
        // Given
        TestEventPublisher publisher = new TestEventPublisher("publisher");
        TestEventConsumer consumer1 =
                new TestEventConsumer() {

                    @Override
                    public void eventReceived(Event event) {
                        super.eventReceived(event);
                        seb.unregisterConsumer(this);
                    }
                };
        TestEventConsumer consumer2 = new TestEventConsumer();
        seb.registerPublisher(publisher, new String[] {"event"});
        seb.registerConsumer(consumer1, "publisher");
        seb.registerConsumer(consumer2, "publisher");
        Event event1 = new Event(publisher, "event", null);
        Event event2 = new Event(publisher, "event", null);
        // When
        seb.publishSyncEvent(publisher, event1);
        seb.publishSyncEvent(publisher, event2);
        // Then
        assertThat(consumer1.getEvents(), contains(event1));
        assertThat(consumer2.getEvents(), contains(event1, event2));
    }

    private class TestEventConsumer implements EventConsumer {

        private List<Event> events = new ArrayList<>();

        @Override
        public void eventReceived(Event event) {
            this.events.add(event);
        }

        public List<Event> getEvents() {
            return events;
        }
    }

    private class TestEventPublisher implements EventPublisher {

        private String name;

        public TestEventPublisher(String name) {
            this.name = name;
        }

        @Override
        public String getPublisherName() {
            return this.name;
        }
    }
}
