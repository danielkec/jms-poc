package io.helidon.examples.jms.se;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.jms.ConnectionFactory;

import javax.jms.JMSException;
import javax.json.bind.Jsonb;

import io.smallrye.reactive.messaging.jms.JmsConnector;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

import io.helidon.config.Config;

@Connector(JmsConnector.CONNECTOR_NAME)
public class HackedJmsConnector extends JmsConnector {
    public HackedJmsConnector(Config config) {
        Config connectorConfig = config.get(JmsConnector.CONNECTOR_PREFIX + JmsConnector.CONNECTOR_NAME);

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        try {
            connectionFactory.setBrokerURL(connectorConfig.get("broker-uri").asString().orElse("tcp://localhost:61616"));
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
        this.setIntField("maxPoolSize", 10);
        this.setIntField("ttl", 60);
        this.setField("jsonb", new MockInstance<Jsonb>(List.of()));
        this.setField("factories", new MockInstance<ConnectionFactory>(List.of(connectionFactory)));
        this.init();
    }

    private void setField(String fieldName, Object value) {
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setIntField(String fieldName, int value) {
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MockInstance<T> implements Instance<T> {

        private Collection<T> collection;

        public MockInstance(Collection<T> collection) {

            this.collection = collection;
        }

        @Override
        public Instance<T> select(final Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends T> Instance<U> select(final Class<U> subtype, final Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends T> Instance<U> select(final TypeLiteral<U> subtype, final Annotation... qualifiers) {
            return null;
        }

        @Override
        public boolean isUnsatisfied() {
            return collection.size() == 0;
        }

        @Override
        public boolean isAmbiguous() {
            return false;
        }

        @Override
        public void destroy(final T instance) {

        }

        @Override
        public Iterator<T> iterator() {
            return collection.iterator();
        }

        @Override
        public T get() {
            return collection.iterator().next();
        }
    }
}
