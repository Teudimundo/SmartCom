/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.PushTask;
import at.ac.tuwien.dsg.smartcom.adapter.util.TaskScheduler;
import at.ac.tuwien.dsg.smartcom.adapters.rest.JsonMessageDTO;
import at.ac.tuwien.dsg.smartcom.broker.InputPublisher;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class RESTInputAdapterTest {

    private Client client;
    private ExecutorService executor;
    private RESTInputAdapter adapter;
    private Publisher publisher;

    private int port;

    @Before
    public void setUp() throws Exception {
        publisher = new Publisher();

        port = FreePortProviderUtil.getFreePort();

        adapter = new RESTInputAdapter(port, "test");
        adapter.setInputPublisher(publisher);
        adapter.setScheduler(new Scheduler());
        adapter.init();

        client = ClientBuilder.newBuilder().register(JacksonFeature.class).register(new ApplicationBinder(publisher)).build();
        //client.register(new LoggingFilter(java.util.logging.Logger.getLogger("Jersey"), true));
        executor = Executors.newFixedThreadPool(5);
    }

    @After
    public void tearDown() throws Exception {
        adapter.cleanUp();
        executor.shutdown();
    }

    @Test(timeout = 20000l)
    public void testRESTInputAdapter() throws Exception {
        final WebTarget target = client.target("http://localhost:"+port+"/test");

        final Message message = new Message.MessageBuilder()
                .setId(Identifier.message("testId"))
                .setContent("testContent")
                .setType("testType")
                .setSubtype("testSubType")
                .setSenderId(Identifier.peer("sender"))
                .setReceiverId(Identifier.peer("receiver"))
                .setConversationId("conversationId")
                .setTtl(3)
                .setLanguage("testLanguage")
                .setSecurityToken("securityToken")
                .create();

        final CountDownLatch latch = new CountDownLatch(20);

        publisher.setLatch(latch);

        for (int i = 0; i < 20; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(new JsonMessageDTO(message)), Response.class);
                    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
                }
            });
        }

        latch.await();

        for (Message msg : publisher.getMessages()) {
            assertEquals(message, msg);
        }


    }

    @Test(timeout = 20000l)
    public void testRESTInputAdapter_nullIds() throws Exception {
        final WebTarget target = client.target("http://localhost:"+port+"/test");

        final Message message = new Message.MessageBuilder()
                .setContent("testContent")
                .setType("testType")
                .setSubtype("testSubType")
                .setConversationId("conversationId")
                .setTtl(3)
                .setLanguage("testLanguage")
                .setSecurityToken("securityToken")
                .create();

        final CountDownLatch latch = new CountDownLatch(20);

        publisher.setLatch(latch);

        for (int i = 0; i < 20; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(new JsonMessageDTO(message)), Response.class);
                    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
                }
            });
        }

        latch.await();

        assertThat(publisher.getMessages(), Matchers.hasSize(20));

        for (Message msg : publisher.getMessages()) {
            assertNull(msg.getId());
            assertNull(msg.getReceiverId());
            assertNull(msg.getSenderId());
        }

    }

    private class Publisher implements InputPublisher {

        private CountDownLatch latch;
        private List<Message> messages = new ArrayList<>();

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void publishInput(Message message) {
            messages.add(message);
            latch.countDown();
        }

        public List<Message> getMessages() {
            return messages;
        }
    }

    private class Scheduler implements TaskScheduler {

        @Override
        public Future<?> schedule(PushTask task) {
            return null;
        }
    }

    private class ApplicationBinder extends AbstractBinder {

        private final Publisher publisher;

        private ApplicationBinder(Publisher publisher) {
            this.publisher = publisher;
        }

        @Override
        protected void configure() {
            bind(publisher).to(InputPublisher.class);
        }
    }
}