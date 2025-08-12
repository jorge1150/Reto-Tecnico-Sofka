package com.sofka.account;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.sofka.account.infrastructure.messaging.ClienteEventConsumer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.kafka.listener.auto-startup=false"
})
@Testcontainers
class AccountServiceApplicationTests {

	@LocalServerPort
	int port;
	@Autowired
	TestRestTemplate rest;

	@MockBean
	ClienteEventConsumer clienteEventConsumer;

	static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("accountdb")
			.withUsername("user")
			.withPassword("pass");

	@BeforeAll
	static void start() {
		pg.start();
	}

	@AfterAll
	static void stop() {
		pg.stop();
	}

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry r) {
		r.add("spring.datasource.url", pg::getJdbcUrl);
		r.add("spring.datasource.username", pg::getUsername);
		r.add("spring.datasource.password", pg::getPassword);
		r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
		r.add("spring.jpa.open-in-view", () -> "false");
	}

	private String base(String p) {
		return "http://localhost:" + port + p;
	}

	private HttpHeaders json() {
		var h = new HttpHeaders();
		h.setContentType(MediaType.APPLICATION_JSON);
		return h;
	}

	@Test
	void flujoCompleto() {
		// 1) Crear cuenta
		String cuenta = """
				{
				  "numero":"ACC001",
				  "tipo":"AHORROS",
				  "saldoInicial": 100.00,
				  "clienteId": "CLI001",
				  "clienteNombre": "Jose Lema"
				}""";
		ResponseEntity<Map> cRes = rest.postForEntity(base("/cuentas"),
				new HttpEntity<>(cuenta, json()), Map.class);
		assertEquals(HttpStatus.CREATED, cRes.getStatusCode());

		// 2) Depósito +50 => saldo 150
		String dep = """
				{ "fecha":"%s", "tipo":"DEPOSITO", "valor": 50.00 }
				""".formatted(LocalDate.now());
		ResponseEntity<Map> dRes = rest.postForEntity(
				base("/movimientos?cuenta=ACC001"),
				new HttpEntity<>(dep, json()),
				Map.class);
		assertEquals(HttpStatus.CREATED, dRes.getStatusCode());
		assertEquals(150.00, ((Number) dRes.getBody().get("saldo")).doubleValue(), 0.001);

		// 3) Retiro grande => 400 saldo_no_disponible
		String ret = """
				{ "fecha":"%s", "tipo":"RETIRO", "valor": -1000.00 }
				""".formatted(LocalDate.now());
		ResponseEntity<Map> rRes = rest.postForEntity(
				base("/movimientos?cuenta=ACC001"),
				new HttpEntity<>(ret, json()),
				Map.class);
		assertEquals(HttpStatus.BAD_REQUEST, rRes.getStatusCode());
		assertEquals("saldo_no_disponible", rRes.getBody().get("error"));

		// 4) Reporte por cliente y fechas (al menos 1 línea por el depósito)
		ResponseEntity<List> rep = rest.getForEntity(
				base("/reportes?clienteId=CLI001&desde=%s&hasta=%s"
						.formatted(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))),
				List.class);
		assertEquals(HttpStatus.OK, rep.getStatusCode());
		assertTrue(rep.getBody().size() >= 1);
	}
}
