package backend.academy.scrapper;

import backend.academy.scrapper.test.util.TestUtil;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    private static final Network TEST_NETWORK = Network.newNetwork();

    @Bean
    @RestartScope
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    }

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:17-alpine")
                .withExposedPorts(5432)
                .withDatabaseName("scrapper")
                .withUsername("postgres")
                .withPassword("test")
                .withNetwork(TEST_NETWORK)
                .withNetworkAliases("postgres");
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
        return new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:3.13.0-alpine"))
                .withExposedPorts(8080)
                .withCopyToContainer(
                        MountableFile.forClasspathResource("wiremock/mappings/"), "/home/wiremock/mappings/")
                .withCommand("--verbose");
    }

    @Bean
    DynamicPropertyRegistrar wiremockPropertyRegistrar(GenericContainer<?> wireMockContainer) {
        String host = wireMockContainer.getHost();
        Integer port = wireMockContainer.getMappedPort(8080);
        WireMock.configureFor(host, port);
        return registry -> registry.add("wiremock.url", () -> TestUtil.createHttpAddress(host, port));
    }
}
