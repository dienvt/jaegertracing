package cps.demo.jaegertracingservice.activemq;

import cps.demo.jaegertracingservice.carrier.JmsTextMapExtractAdapter;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author dienvt
 */
@Component
@Slf4j
public class OrderLogListener {

    @Autowired
    private JaegerTracer tracer;

    @JmsListener(destination = "orderlog")
    public void receiveMessage(final Message jsonMessage) throws JMSException, InterruptedException {
        String messageData = null;
        log.info("invoke by message: {}", jsonMessage);
        Span rootSpan = startConsumerSpan(jsonMessage, "COMSUMER:orderlog");

        rootSpan.log("save to DB");
        Thread.sleep(50);

        rootSpan.log("save cache");
        Thread.sleep(20);

        rootSpan.finish();
    }

    public Span startConsumerSpan(Message message, String operationName) {
        Tracer.SpanBuilder spanBuilder;

        try {
            SpanContext parentSpan = tracer.extract(Format.Builtin.TEXT_MAP, new JmsTextMapExtractAdapter(message));
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
