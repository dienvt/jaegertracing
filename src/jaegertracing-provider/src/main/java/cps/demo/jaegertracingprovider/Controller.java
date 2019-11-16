package cps.demo.jaegertracingprovider;

import com.google.common.collect.ImmutableMap;
import cps.demo.jaegertracingprovider.entity.ProviderDeliverRequest;
import cps.demo.jaegertracingprovider.entity.ProviderResponse;
import cps.demo.jaegertracingsharelib.carrier.HttpRequestExtractAdapter;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author dienvt
 */
@RestController
@ComponentScan("cps.demo.jaegertracingprovider.entity")
public class Controller {

    @Autowired
    private JaegerTracer tracer;

    @RequestMapping(value = "/provider/deliver", method = RequestMethod.POST)
    public ProviderResponse deliver(@RequestHeader Map<String, String> headers, @RequestBody ProviderDeliverRequest dataRequest, HttpServletRequest request) {

        //        Span rootSpan = tracer.buildSpan("POST:/provider/deliver")
//                .addReference(References.FOLLOWS_FROM, dataRequest.getSpanContext())
//                .start();
        Span rootSpan = startServerSpanv2(request, "POST:/provider/deliver");
        ProviderResponse response = new ProviderResponse();

//        SpanContext context = new JaegerSpanContext()

        try {

            String orderID = dataRequest.getOrderId();
            rootSpan.setTag("orderID", orderID);

            if (StringUtils.isEmpty(orderID)) {
                rootSpan.log("orderId empty");
                response.setCode(-101);
            } else {
                long lOrderID = Long.parseLong(orderID);
                Random random = new Random();

                Span redisChecking = tracer.buildSpan("get-redis").asChildOf(rootSpan).start();
                redisChecking.log(ImmutableMap.of("event", "checking redis"));
                Thread.sleep(random.nextInt(50));
                redisChecking.finish();

                Span callProvider = tracer.buildSpan("call-provider").asChildOf(rootSpan).start();
                callProvider.log("call-provider orderID: " + lOrderID);
                Thread.sleep(random.nextInt(100));
                callProvider.finish();
                // mock calling provider

                if (random.nextBoolean()) {
                    response.setCode(1);
                } else {
                    response.setCode(-100);
                }
            }
            return response;
        } catch (Exception ex) {
            response.setCode(0);
            rootSpan.log(String.format("%s", ex));
            return response;
        } finally {
            if (response.getCode() != 1) {
                rootSpan.setTag("result", "fail");
            } else {
                rootSpan.setTag("result", "success");
            }
            rootSpan.finish();
        }
    }

    public Span startServerSpan(Map<String, String> headers, String operationName) {
        // format the headers for extraction
//        Set<Map.Entry<String, List<String>>> rawHeaders = httpHeaders.entrySet();
//        final HashMap<String, String> headers = new HashMap<String, String>();
//        for (Map.Entry<String, List<String>> entry : rawHeaders) {
//            headers.put(entry.getKey(), entry.getValue().get(0));
//        }

        Tracer.SpanBuilder spanBuilder;
        try {
            SpanContext parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
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
