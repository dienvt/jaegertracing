package cps.demo.jaegertracingverify;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

/**
 * @author dienvt
 */
@org.springframework.context.annotation.Configuration
@Slf4j
public class AppConfiguration {

    @Bean
    public static JaegerTracer getTracer() {

        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration("jaeger-verify").withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }
}
