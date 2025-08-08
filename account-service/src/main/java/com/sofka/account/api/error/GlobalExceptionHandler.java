package com.sofka.account.api.error;

import com.sofka.account.shared.SaldoNoDisponibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 * Manejador centralizado de errores. Devuelve application/problem+json
 * Mantén tus controladores/servicios limpios y consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoNoDisponibleException.class)
    public ProblemDetail handleSaldoNoDisponible(SaldoNoDisponibleException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Saldo no disponible");
        pd.setType(URI.create("https://errors.demo.com/banking/saldo-no-disponible"));
        pd.setProperty("error", "saldo_no_disponible");
        pd.setProperty("cuenta", ex.getCuentaNumero());
        pd.setProperty("valor", ex.getIntentoValor());
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    // Ejemplo: validaciones @Valid fallidas -> 400 con detalles de campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Solicitud inválida");
        pd.setDetail("Error de validación en la solicitud.");
        pd.setProperty("error", "validation_failed");
        pd.setProperty("fields", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    // Fallback: cualquier IllegalArgumentException -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Solicitud inválida");
        pd.setProperty("error", "bad_request");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    // Fallback genérico -> 500 (no revelar detalles internos)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Error interno");
        pd.setDetail("Ha ocurrido un error inesperado. Inténtalo más tarde.");
        pd.setProperty("error", "unexpected_error");
        pd.setProperty("timestamp", OffsetDateTime.now());
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleConflict(IllegalStateException ex) {
        var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Operación no permitida");
        pd.setDetail(ex.getMessage());
        pd.setProperty("error", "operation_not_allowed");
        return pd;
    }
}