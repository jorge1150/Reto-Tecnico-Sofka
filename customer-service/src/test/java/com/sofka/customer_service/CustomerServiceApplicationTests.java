package com.sofka.customer_service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sofka.customer.api.dto.ClienteRequest;
import com.sofka.customer.api.dto.ClienteResponse;
import com.sofka.customer.api.mapper.ClienteMapper;
import com.sofka.customer.aplication.service.ClienteService;
import com.sofka.customer.domain.model.Cliente;
import com.sofka.customer.domain.repository.IClienteRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceApplicationTests {

	@Mock
	IClienteRepository repo;
	@Spy
	ClienteMapper mapper = new ClienteMapper();

	@InjectMocks
	ClienteService service;

	private ClienteRequest request() {
		ClienteRequest r = new ClienteRequest();
		r.setClienteId("CLI001");
		r.setNombre("Jose Lema");
		r.setGenero("M");
		r.setEdad(32);
		r.setIdentificacion("1717123456");
		r.setDireccion("Otavalo sn y principal");
		r.setTelefono("098254785");
		r.setPassword("1234");
		return r;
	}

	@Test
	void creaCliente_ok() {
		when(repo.existsByIdentificacion("1717123456")).thenReturn(false);
		when(repo.save(any(Cliente.class))).thenAnswer(inv -> {
			Cliente c = inv.getArgument(0);
			c.setId(1L);
			return c;
		});

		ClienteResponse res = service.crear(request());

		assertEquals("CLI001", res.getClienteId());
		assertEquals("Jose Lema", res.getNombre());
		verify(repo).save(any(Cliente.class));
	}

	@Test
	void rechazaDuplicadoIdentificacion() {
		when(repo.existsByIdentificacion("1717123456")).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> service.crear(request()));
		verify(repo, never()).save(any());
	}
}
