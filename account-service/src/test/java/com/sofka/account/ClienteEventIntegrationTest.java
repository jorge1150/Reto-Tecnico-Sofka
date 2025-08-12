package com.sofka.account;

import com.sofka.account.domain.event.ClienteActualizadoEvent;
import com.sofka.account.domain.model.Cuenta;
import com.sofka.account.domain.model.TipoCuenta;
import com.sofka.account.domain.repository.CuentaRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        // Kafka embebido
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.topics.customer-events=customer.events",

        // Consumer: ignora headers de tipo y usa la clase local
        "spring.kafka.consumer.group-id=account-service-test",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
        "spring.kafka.consumer.properties.spring.json.use.type.headers=false",
        "spring.kafka.consumer.properties.spring.json.value.default.type=com.sofka.account.api.event.ClienteActualizadoEvent",

        // JPA H2 en memoria para este test
        "spring.datasource.url=jdbc:h2:mem:acct;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false"
    }
)
@EmbeddedKafka(partitions = 3, topics = { "customer.events" })
@ActiveProfiles("test")
class ClienteEventIntegrationTest {

    @Autowired
    CuentaRepository cuentaRepo;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    // Producer de prueba (JsonSerializer) apuntando al broker embebido
    @TestConfiguration
    static class ProducerTestConfig {
        @Bean
        ProducerFactory<String, Object> producerFactory(org.springframework.core.env.Environment env) {
            Map<String, Object> props = KafkaTestUtils.producerProps(env.getProperty("spring.kafka.bootstrap-servers"));
            props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.common.serialization.StringSerializer.class);
            props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    org.springframework.kafka.support.serializer.JsonSerializer.class);
            // Opcional: mandar alias de tipo en headers si quisieras mapping
            return new DefaultKafkaProducerFactory<>(props);
        }
        @Bean
        KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
            return new KafkaTemplate<>(pf);
        }
    }

    @Test
    void cuandoPublicoClienteActualizado_entoncesSeActualizaElSnapshotEnCuentas() {
        // Arrange: existe una cuenta con snapshot viejo
        var cuenta = new Cuenta();
        cuenta.setNumero("ACC001");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(new BigDecimal("100.00"));
        cuenta.setEstado(true);
        cuenta.setClienteId("CLI001");
        cuenta.setClienteNombre("Nombre Antiguo");
        cuentaRepo.save(cuenta);

        // Act: publicamos evento de actualización de cliente
        var evt = new ClienteActualizadoEvent("CLI001", "Jose Lema (KAFKA)", true);
        kafkaTemplate.send("customer.events", evt.getClienteId(), evt);

        // Assert: esperamos hasta que el listener procese y actualice el snapshot
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    List<Cuenta> cuentas = cuentaRepo.findByClienteId("CLI001");
                    assertFalse(cuentas.isEmpty());
                    assertEquals("Jose Lema (KAFKA)", cuentas.get(0).getClienteNombre());
                });
    }
}

