package io.github.arthurmadalena.kallopolliapi.shared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

public class GlobalExceptionHandler{
@ExceptionHandler (RecursoNaoEncontradoException.class)
  public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
}
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validacao(MethodArgumentNotValidException ex) {
    var detalhe = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalhe);
  }

}
