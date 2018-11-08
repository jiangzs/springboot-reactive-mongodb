package com.tutorial.springbootreactivemongodb;

import io.micrometer.core.annotation.Incubating;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(PushgatewayConfiguration.PushgatewayProperties.class)
public class PushgatewayConfiguration {

    @ConfigurationProperties(prefix = "management.metrics.export.prometheus.pushgateway")
    public static class PushgatewayProperties {
        /**
         * Enable publishing via a Prometheus Pushgateway.
         */
        private Boolean enabled = false;

        /**
         * Required host:port or ip:port of the Pushgateway.
         */
        private String baseUrl = "localhost:9091";

        /**
         * Required identifier for this application instance.
         */
        private String job;

        /**
         * Frequency with which to push metrics to Pushgateway.
         */
        private Duration pushRate = Duration.ofMinutes(1);

        /**
         * Push metrics right before shut-down. Mostly useful for batch jobs.
         */
        private boolean pushOnShutdown = true;

        /**
         * Delete metrics from Pushgateway when application is shut-down
         */
        private boolean deleteOnShutdown = true;

        /**
         * Used to group metrics in pushgateway. A common example is setting
         */
        private Map<String, String> groupingKeys = new HashMap<>();

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }

        public Duration getPushRate() {
            return pushRate;
        }

        public void setPushRate(Duration pushRate) {
            this.pushRate = pushRate;
        }

        public boolean isPushOnShutdown() {
            return pushOnShutdown;
        }

        public void setPushOnShutdown(boolean pushOnShutdown) {
            this.pushOnShutdown = pushOnShutdown;
        }

        public boolean isDeleteOnShutdown() {
            return deleteOnShutdown;
        }

        public void setDeleteOnShutdown(boolean deleteOnShutdown) {
            this.deleteOnShutdown = deleteOnShutdown;
        }

        public Map<String, String> getGroupingKeys() {
            return groupingKeys;
        }

        public void setGroupingKeys(Map<String, String> groupingKeys) {
            this.groupingKeys = groupingKeys;
        }
    }

    static class PrometheusPushGatewayEnabledCondition extends AllNestedConditions {
        public PrometheusPushGatewayEnabledCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(value = "management.metrics.export.prometheus.enabled", matchIfMissing = true)
        static class PrometheusMeterRegistryEnabled {
            //
        }

        @ConditionalOnProperty("management.metrics.export.prometheus.pushgateway.enabled")
        static class PushGatewayEnabled {
            //
        }
    }

    /**
     * Configuration for
     * <a href="https://github.com/prometheus/pushgateway">Prometheus
     * Pushgateway</a>.
     *
     * @author David J. M. Karlsen
     */
    @Configuration
    @ConditionalOnClass(PushGateway.class)
    @Conditional(PrometheusPushGatewayEnabledCondition.class)
    @Incubating(since = "1.0.0")
    public class PrometheusPushGatewayConfiguration {
        private final Logger logger = LoggerFactory.getLogger(PrometheusPushGatewayConfiguration.class);
        private final CollectorRegistry collectorRegistry;
        private final PushgatewayProperties pushgatewayProperties;
        private final PushGateway pushGateway;
        private final Environment environment;
        private final ScheduledExecutorService executorService;

        PrometheusPushGatewayConfiguration(CollectorRegistry collectorRegistry,
                                           PushgatewayProperties pushgatewayProperties, Environment environment) {
            this.collectorRegistry = collectorRegistry;
            this.pushgatewayProperties = pushgatewayProperties;
            this.pushGateway = new PushGateway(pushgatewayProperties.getBaseUrl());
            this.environment = environment;
            this.executorService = Executors.newSingleThreadScheduledExecutor((r) -> {
                final Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("micrometer-pushgateway");
                return thread;
            });
            executorService.scheduleAtFixedRate(this::push, 0, pushgatewayProperties.getPushRate().toMillis(),
                    TimeUnit.MILLISECONDS);
        }

        void push() {
            try {
                pushGateway.pushAdd(collectorRegistry, job(), pushgatewayProperties.getGroupingKeys());
            } catch (UnknownHostException e) {
                logger.error("Unable to locate host '" + pushgatewayProperties.getBaseUrl()
                        + "'. No longer attempting metrics publication to this host");
                executorService.shutdown();
            } catch (Throwable t) {
                logger.error("Unable to push metrics to Prometheus Pushgateway, url: {}  error: {}", pushgatewayProperties.getBaseUrl(), t.getMessage());
            }
        }

        @PreDestroy
        void shutdown() {
            executorService.shutdown();
            if (pushgatewayProperties.isPushOnShutdown()) {
                push();
            }
            if (pushgatewayProperties.isDeleteOnShutdown()) {
                try {
                    pushGateway.delete(job(), pushgatewayProperties.getGroupingKeys());
                } catch (Throwable t) {
                    logger.error("Unable to delete metrics from Prometheus Pushgateway", t);
                }
            }
        }

        private String job() {
            String job = pushgatewayProperties.getJob();
            if (job == null) {
                job = environment.getProperty("spring.application.name");
            }
            if (job == null) {
                // There's a history of Prometheus spring integration defaulting the job name to
                // "spring" from when
                // Prometheus integration didn't exist in Spring itself.
                job = "spring";
            }
            return job;
        }
    }
}
