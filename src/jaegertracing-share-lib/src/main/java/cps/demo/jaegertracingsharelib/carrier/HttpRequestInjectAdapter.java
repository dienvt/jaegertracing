package cps.demo.jaegertracingsharelib.carrier;

import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapInject;
import org.apache.http.HttpRequest;

import java.util.Iterator;
import java.util.Map;

/**
 * @author dienvt
 */
public class HttpRequestInjectAdapter implements TextMap {

    private final HttpRequest httpRequest;

    public HttpRequestInjectAdapter(HttpRequest httpRequest) {

        this.httpRequest = httpRequest;
    }

    @Override
    public void put(String key, String value) {
        this.httpRequest.addHeader(key, value);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("iterator should never be used with Tracer.inject()");
    }
}
