package cps.demo.jaegertracingservice;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author dienvt
 */
@org.springframework.context.annotation.Configuration
@Slf4j
public class AppConfiguration {


    @Bean
    public static JaegerTracer getTracer() {
        System.setProperty("ABC_TEST","abc_test");
        log.info("JAEGER_PREFIX: {}",System.getenv());
        log.info("JAEGER_PREFIX: {}",System.getProperty(Configuration.JAEGER_PREFIX));
        log.info("JAEGER_ENDPOINT: {}",System.getProperty(Configuration.JAEGER_ENDPOINT));
        log.info("JAEGER_AUTH_TOKEN: {}",System.getProperty(Configuration.JAEGER_AUTH_TOKEN));
        log.info("JAEGER_USER: {}",System.getProperty(Configuration.JAEGER_USER));
        log.info("JAEGER_PASSWORD: {}",System.getProperty(Configuration.JAEGER_PASSWORD));
        log.info("JAEGER_AGENT_HOST: {}",System.getProperty(Configuration.JAEGER_AGENT_HOST));
        log.info("JAEGER_AGENT_PORT: {}",System.getProperty(Configuration.JAEGER_AGENT_PORT));
        log.info("JAEGER_REPORTER_LOG_SPANS: {}",System.getProperty(Configuration.JAEGER_REPORTER_LOG_SPANS));
        log.info("JAEGER_REPORTER_MAX_QUEUE_SIZE: {}",System.getProperty(Configuration.JAEGER_REPORTER_MAX_QUEUE_SIZE));
        log.info("JAEGER_REPORTER_FLUSH_INTERVAL: {}",System.getProperty(Configuration.JAEGER_REPORTER_FLUSH_INTERVAL));
        log.info("JAEGER_SAMPLER_TYPE: {}",System.getProperty(Configuration.JAEGER_SAMPLER_TYPE));
        log.info("JAEGER_SAMPLER_PARAM: {}",System.getProperty(Configuration.JAEGER_SAMPLER_PARAM));
        log.info("JAEGER_SAMPLER_MANAGER_HOST_PORT: {}",System.getProperty(Configuration.JAEGER_SAMPLER_MANAGER_HOST_PORT));
        log.info("JAEGER_SERVICE_NAME: {}",System.getProperty(Configuration.JAEGER_SERVICE_NAME));
        log.info("JAEGER_TAGS: {}",System.getProperty(Configuration.JAEGER_TAGS));
        log.info("JAEGER_PROPAGATION: {}",System.getProperty(Configuration.JAEGER_PROPAGATION));
        log.info("JAEGER_SENDER_FACTORY: {}",System.getProperty(Configuration.JAEGER_SENDER_FACTORY));
        log.info("JAEGER_TRACEID_128BIT: {}",System.getProperty(Configuration.JAEGER_TRACEID_128BIT));
//        System.setProperty(Configuration.JAEGER_PREFIX, "");
//        System.setProperty(Configuration.JAEGER_ENDPOINT, "");
//        System.setProperty(Configuration.JAEGER_AUTH_TOKEN, "");
//        System.setProperty(Configuration.JAEGER_USER, "");
//        System.setProperty(Configuration.JAEGER_PASSWORD, "");
//        System.setProperty(Configuration.JAEGER_AGENT_HOST, "");
//        System.setProperty(Configuration.JAEGER_AGENT_PORT, "");
//        System.setProperty(Configuration.JAEGER_REPORTER_LOG_SPANS, "");
//        System.setProperty(Configuration.JAEGER_REPORTER_MAX_QUEUE_SIZE, "");
//        System.setProperty(Configuration.JAEGER_REPORTER_FLUSH_INTERVAL, "");
//        System.setProperty(Configuration.JAEGER_SAMPLER_TYPE, "");
//        System.setProperty(Configuration.JAEGER_SAMPLER_PARAM, "");
//        System.setProperty(Configuration.JAEGER_SAMPLER_MANAGER_HOST_PORT, "");
//        System.setProperty(Configuration.JAEGER_SERVICE_NAME, "");
//        System.setProperty(Configuration.JAEGER_TAGS, "");
//        System.setProperty(Configuration.JAEGER_PROPAGATION, "");
//        System.setProperty(Configuration.JAEGER_SENDER_FACTORY, "");
//        System.setProperty(Configuration.JAEGER_TRACEID_128BIT, "");

        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration("jaeger-demo").withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }
}
