package cps.demo.jaegertracingsharelib.carrier;

import io.opentracing.propagation.TextMap;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dienvt
 */
public class HttpRequestExtractAdapter implements TextMap {

    private final Map<String, String> map = new HashMap<>();

    public HttpRequestExtractAdapter(HttpRequest httpRequest) {
        if (httpRequest == null) {
            return;
        }

        HeaderIterator headerIterator = httpRequest.headerIterator();
        while (headerIterator.hasNext()) {
            Header header = headerIterator.nextHeader();
            map.put(header.getName(), header.getValue());
        }
    }

    public HttpRequestExtractAdapter(javax.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return;
        }

        Enumeration<String> headerIterator = request.getHeaderNames();
        while (headerIterator.hasMoreElements()) {
            String headerName = headerIterator.nextElement();
            map.put(headerName, request.getHeader(headerName));
        }
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {

        return this.map.entrySet().iterator();
    }

    @Override
    public void put(String s, String s1) {
        throw new UnsupportedOperationException("HttpRequestExtractAdapter should be used with Tracer.inject()");
    }
}
