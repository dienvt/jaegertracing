package cps.demo.jaegertracingservice.activemq;

import cps.demo.jaegertracingservice.carrier.JmsTextMapInjectAdapter;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author dienvt
 */

@Component
public class ActiveMQProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private JaegerTracer tracer;

    public void sendMessage(String queueName, final String message, final Span rootSpan) {
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message result = session.createTextMessage(message);
                tracer.inject(rootSpan.context(), Format.Builtin.TEXT_MAP, new JmsTextMapInjectAdapter(result));
                return result;
            }
        };
        jmsTemplate.send(queueName, messageCreator);
    }

}
