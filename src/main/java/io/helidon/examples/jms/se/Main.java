/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

package io.helidon.examples.jms.se;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import io.helidon.config.Config;
import io.helidon.messaging.Messaging;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;

import io.smallrye.reactive.messaging.jms.JmsConnector;
import org.eclipse.microprofile.reactive.messaging.Message;


public final class Main {

    public static void main(final String[] args) throws IOException {
        startServer();
    }

    static WebServer startServer() throws IOException {
        setupLogging();
        Config config = Config.create();
        ServerConfiguration serverConfig = ServerConfiguration.create(config.get("server"));
        WebServer server = WebServer.create(serverConfig, createRouting(config));
        server.start()
                .thenAccept(ws -> System.out.println("WEB server is up! http://localhost:" + ws.port() + "/send/HelloWorld"));
        return server;
    }

    private static Routing createRouting(Config config) {

        JmsConnector jmsConnector = new HackedJmsConnector(config);

        Messaging messaging = Messaging.builder()
                .connector(jmsConnector)
                .config(config)
                // registration of emitter for sending messages
                .emitter("jms-outgoing-channel")
                // registration of handler for incoming messages
                .incomingAcked("jms-incoming-channel",
                        payload -> System.out.println("Received message from JMS: " + payload))
                .build();

        messaging.start();


        return Routing.builder()
                .register("/send", rules -> rules
                        .get("/{msg}", (req, res) -> {
                            String msg = req.path().param("msg");
                            // Usage of emitter eg. sending message directly to channel jms-outgoing-channel
                            messaging.send("jms-outgoing-channel", msg);
                            res.send("Message sent!");
                        }))
                .build();
    }

    private static void setupLogging() throws IOException {
        try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        }
    }

}
