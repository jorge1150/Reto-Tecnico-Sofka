package com.sofka.customer_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sofka.customer.api.dto.ClienteRequest;
import com.sofka.customer.api.dto.ClienteResponse;
import com.sofka.customer.api.mapper.ClienteMapper;
import com.sofka.customer.aplication.service.ClienteService;
import com.sofka.customer.domain.model.Cliente;
import com.sofka.customer.domain.repository.IClienteRepository;
import com.sofka.customer.infrastructure.messaging.ClienteEventPublisher;
import com.sofka.customer.domain.event.ClienteActualizadoEvent; 

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceApplicationTests {

    @Mock
    IClienteRepository repo;

    @Spy
    ClienteMapper mapper = new ClienteMapper();

    @Mock
    ClienteEventPublisher eventPublisher;

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
        // arrange
        when(repo.existsByIdentificacion("1717123456")).thenReturn(false);
        when(repo.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        // act
        ClienteResponse res = service.crear(request());

        // assert
        assertEquals("CLI001", res.getClienteId());
        assertEquals("Jose Lema", res.getNombre());
        verify(repo).save(any(Cliente.class));

        // y se publica el evento 
        verify(eventPublisher, times(1)).publicarClienteActualizado(any(ClienteActualizadoEvent.class));
    }

    @Test
    void rechazaDuplicadoIdentificacion() {
        // arrange
        when(repo.existsByIdentificacion("1717123456")).thenReturn(true);

        // act + assert
        assertThrows(IllegalArgumentException.class, () -> service.crear(request()));

        // no se debe guardar ni publicar
        verify(repo, never()).save(any());
        verify(eventPublisher, never()).publicarClienteActualizado(any());
    }
}
