package cps.demo.jaegertracingservice.carrier;

import io.opentracing.propagation.TextMap;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dienvt
 */
public class JmsTextMapExtractAdapter implements TextMap {
    private final Map<String, String> map = new HashMap<>();

    public JmsTextMapExtractAdapter(Message message) {
        if (message == null) {
            return;
        }
        try {
            Enumeration enumeration = message.getPropertyNames();
            if (enumeration != null) {
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    Object value = message.getObjectProperty(key);
                    if (value instanceof String) {
                        map.put(decodeDash(key), (String) value);
                    }
                }
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException(
                "JmsTextMapExtractAdapter should only be used with Tracer.extract()");
    }

    /**
     * Decode dashes (encoded in {@link JmsTextMapInjectAdapter}
     */
    private String decodeDash(String key) {
        return key.replace(JmsTextMapInjectAdapter.DASH, "-");
    }
}
