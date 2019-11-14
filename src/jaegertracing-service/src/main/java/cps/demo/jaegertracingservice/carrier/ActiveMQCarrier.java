package cps.demo.jaegertracingservice.carrier;

import io.opentracing.propagation.TextMap;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dienvt
 */
public class ActiveMQCarrier implements TextMap {

    private final Message message;

    public ActiveMQCarrier(Message message) {
        this.message = message;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("carrier is write-only");
    }

    @Override
    public void put(String key, String value) {
        try {
            message.setStringProperty(key, value);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
