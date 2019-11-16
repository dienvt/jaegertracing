package cps.demo.jaegertracingverify;

import cps.demo.jaegertracingsharelib.carrier.HttpRequestExtractAdapter;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author dienvt
 */
@RestController
public class Controller {

    @Autowired
    private JaegerTracer tracer;

    @PostMapping("/verify")
    public String verifyUser(HttpServletRequest request) throws InterruptedException {
        Span vSpan = startServerSpanv2(request, "POST:/verify");

        vSpan.log("verify invoke");

        Span cacheSpan = tracer.buildSpan("cheking").asChildOf(vSpan).start();
        Thread.sleep(50);

        cacheSpan.finish();

        vSpan.finish();
        return "";
    }

    public Span startServerSpanv2(HttpServletRequest request, String operationName) {

        Tracer.SpanBuilder spanBuilder;
        try {
            SpanContext parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, new HttpRequestExtractAdapter(request));
            if (parentSpan == null) {
                spanBuilder = tracer.buildSpan(operationName);
            } else {
                spanBuilder = tracer.buildSpan(operationName).asChildOf(parentSpan);
            }
        } catch (IllegalArgumentException e) {
            spanBuilder = tracer.buildSpan(operationName);
        }
        return spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
    }

}
