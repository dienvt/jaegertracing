package cps.demo.jaegertracingservice;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import cps.demo.jaegertracingservice.activemq.ActiveMQProducer;
import cps.demo.jaegertracingservice.carrier.RequestBuilderCarrier;
import cps.demo.jaegertracingservice.entity.*;
import cps.demo.jaegertracingsharelib.carrier.HttpRequestInjectAdapter;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

/**
 * @author dienvt
 */
@RestController
public class Controller {
    private static final int NUM_PARTS = 3;
    private static final long PART_TERMS = 100000000;

    @Autowired
    private JaegerTracer tracer;
    @Autowired
    private Gson gson;
    @Autowired
    private ActiveMQProducer activeMQProducer;

    @GetMapping("/ping")
    public String ping() throws InterruptedException {

        String serviceName = System.getenv("JAEGER_SERVICE_NAME");
        if (null == serviceName || serviceName.isEmpty()) {
            serviceName = "vertx-create-span";
        }
        System.setProperty("JAEGER_SERVICE_NAME", serviceName);
//        MicrometerMetricsFactory metricsReporter = new MicrometerMetricsFactory();
        Tracer tracer = Configuration.fromEnv().getTracer();
        Span parentSpan = tracer.buildSpan("root").start();
        parentSpan.setTag("ping", NUM_PARTS * PART_TERMS);
        try (Scope ignored = tracer.activateSpan(parentSpan)) {
            Random ran = new Random();
            int delay = ran.nextInt(1000);
            Thread.sleep(delay);
        }

        parentSpan.finish();
        tracer.close();
        return "pong";
    }

    @GetMapping("/resource")
    public ResourceEntity getResource(HttpServletRequest request) {
        String path = request.getServletPath();
        Span span = null;

        try {
            span = tracer.buildSpan(path).start();
            span.setTag("totalTerms", NUM_PARTS * PART_TERMS);
        } finally {
            if (span != null) {
                span.finish();
            }
        }

        return ResourceEntity.builder()
                .env("LOCAL")
                .build();
    }

    @RequestMapping(value = "/deliver", method = RequestMethod.POST)
    public BaseResponse deliver(@RequestBody DeliverRequest dataRequest, HttpServletRequest request) {
        String path = request.getServletPath();
        Span rootSpan = tracer.buildSpan(path).start();
        try {

            rootSpan.setTag("totalSpan", NUM_PARTS * PART_TERMS);

            rootSpan.log("verify request");
            Span verifySpan = tracer.buildSpan("/verify/accesstoken").asChildOf(rootSpan).start();
            verifySpan.log("send verify");
            callVerify(verifySpan);
            verifySpan.finish();

            rootSpan.log(ImmutableMap.of("event", "call deliver"));
            Span deliverSpan = tracer.buildSpan("/provider/deliver").asChildOf(rootSpan).start();
            deliverSpan.setTag("providerCode", "PAYOO");

            ProviderResponse response = callProviderv2(dataRequest.getOrderid(), deliverSpan);

            deliverSpan.finish();

            if (response != null && response.getCode() == 1) {
                rootSpan.setTag("result", "success");
                return BaseResponse.builder()
                        .returnCode(1)
                        .returnMessage("success")
                        .build();
            } else {
                rootSpan.setTag("result", "fail");
                String message = response == null ? "null" : String.valueOf(response.getCode());
                return BaseResponse.builder()
                        .returnCode(-1)
                        .returnMessage("fail, provider code: " + message)
                        .build();
            }

        } finally {
            activeMQProducer.sendMessage("orderlog", "mockOrderInfo", rootSpan);

            if (rootSpan != null) {
                rootSpan.finish();
            }
        }
    }

    private void callVerify(Span verifySpan) {
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead
        String url = "http://localhost:8020/verify";
        try {
            StringEntity params = new StringEntity("");
            RequestBuilder requestBuilder = RequestBuilder.post()
                    .addHeader("content-type", "application/json")
                    .setUri(url)
                    .setEntity(params);

            verifySpan.setTag(Tags.HTTP_METHOD, "POST");
            verifySpan.setTag(Tags.HTTP_URL, url);
            HttpUriRequest request = requestBuilder.build();
            tracer.inject(verifySpan.context(), Format.Builtin.HTTP_HEADERS, new HttpRequestInjectAdapter(request));


            HttpResponse httpResponse = httpClient.execute(request);
            String response = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
        } catch (Exception ex) {

        }
    }

    private ProviderResponse callProvider(String orderId, Span deliverSpan) {

        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead
        String url = "http://localhost:8010/provider/deliver";
        try {
            StringEntity params = new StringEntity(gson.toJson(ProviderDeliverRequest.builder().orderId(orderId).build()));
            RequestBuilder requestBuilder = RequestBuilder.post()
                    .addHeader("content-type", "application/json")
                    .setUri(url)
                    .setEntity(params);

            deliverSpan.setTag(Tags.HTTP_METHOD, "POST");
            deliverSpan.setTag(Tags.HTTP_URL, url);
            tracer.inject(deliverSpan.context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderCarrier(requestBuilder));

            HttpUriRequest request = requestBuilder.build();

            HttpResponse httpResponse = httpClient.execute(request);
            String response = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
            return gson.fromJson(response, ProviderResponse.class);
        } catch (Exception ex) {

        } finally {
            //Deprecated
            //httpClient.getConnectionManager().shutdown();
        }
        return null;

    }

    private ProviderResponse callProviderv2(String orderId, Span deliverSpan) {

        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead
        String url = "http://localhost:8010/provider/deliver";
        try {
            StringEntity params = new StringEntity(gson.toJson(ProviderDeliverRequest.builder().orderId(orderId).build()));
            RequestBuilder requestBuilder = RequestBuilder.post()
                    .addHeader("content-type", "application/json")
                    .setUri(url)
                    .setEntity(params);

            deliverSpan.setTag(Tags.HTTP_METHOD, "POST");
            deliverSpan.setTag(Tags.HTTP_URL, url);
            HttpUriRequest request = requestBuilder.build();

            tracer.inject(deliverSpan.context(), Format.Builtin.HTTP_HEADERS, new HttpRequestInjectAdapter(request));

            HttpResponse httpResponse = httpClient.execute(request);
            String response = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
            return gson.fromJson(response, ProviderResponse.class);
        } catch (Exception ex) {

        } finally {
            //Deprecated
            //httpClient.getConnectionManager().shutdown();
        }
        return null;
    }
}
