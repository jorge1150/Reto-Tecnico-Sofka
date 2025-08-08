package com.sofka.account.api.dto;

import com.sofka.account.domain.model.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovimientoUpdateRequest {
    @NotNull private LocalDate fecha;
    @NotNull private TipoMovimiento tipo;
}
