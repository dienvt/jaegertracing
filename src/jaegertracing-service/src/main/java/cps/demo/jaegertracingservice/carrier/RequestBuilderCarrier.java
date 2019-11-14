package cps.demo.jaegertracingservice.carrier;


import org.apache.http.client.methods.RequestBuilder;

import java.util.Iterator;
import java.util.Map;

/**
 * @author dienvt
 */
public class RequestBuilderCarrier implements io.opentracing.propagation.TextMap {
    private final RequestBuilder builder;

    public RequestBuilderCarrier(RequestBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("carrier is write-only");
    }

    @Override
    public void put(String key, String value) {
        builder.addHeader(key, value);
    }
}