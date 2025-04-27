package backend.academy.bot;

import backend.academy.bot.test.utils.TestUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import java.util.List;

// isolated from the "scrapper" module's containers!
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @RestartScope
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    }

    @Bean
    @RestartScope
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);
    }

    @Bean
    @RestartScope
    GenericContainer<?> wireMockContainer() {
        return new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:2.31.0"))
                .withExposedPorts(8080)
                .withCopyToContainer(
                        MountableFile.forClasspathResource("wiremock/mappings/"), "/home/wiremock/mappings/")
                .withCommand("--verbose");
    }

    @Bean
    DynamicPropertyRegistrar testPropertiesRegistrar(
            @Qualifier("wireMockContainer") GenericContainer<?> wiremockContainer) {
        return registry -> registry.add(
                "wiremock.url",
                () -> TestUtil.createHttpAddress(
                        wiremockContainer.getHost(), wiremockContainer.getMappedPort(8080)));
    }
}
