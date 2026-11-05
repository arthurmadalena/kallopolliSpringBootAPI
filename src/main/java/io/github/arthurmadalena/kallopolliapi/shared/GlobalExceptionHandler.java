
package io.github.arthurmadalena.kallopolliapi.shared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RecursoNaoEncontradoException.class)
  public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
    return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validacao(MethodArgumentNotValidException ex) {

    String detalhe = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
            .collect(Collectors.joining("; "));

    return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            detalhe
    );
  }

  @ExceptionHandler(RegraNegocioException.class)
  public ProblemDetail regraNegocio(RegraNegocioException ex) {
    return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
    );
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail argumentoInvalido(IllegalArgumentException ex) {
    return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
    );
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail estadoInvalido(IllegalStateException ex) {
    return ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
    );
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail erroInterno(Exception ex) {
    return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocorreu um erro interno. Entre em contato com o suporte."
    );
  }
}

