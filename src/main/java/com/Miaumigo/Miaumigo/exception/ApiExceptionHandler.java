package com.Miaumigo.Miaumigo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErroResponse tratarErroValidacao(MethodArgumentNotValidException exception) {
		List<String> erros = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
				.toList();

		return new ErroResponse("Dados inválidos", erros);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErroResponse tratarErroArgumento(IllegalArgumentException exception) {
		return new ErroResponse(exception.getMessage(), List.of());
	}

	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErroResponse tratarErroEstado(IllegalStateException exception) {
		return new ErroResponse(exception.getMessage(), List.of());
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErroResponse tratarRecursoNaoEncontrado(RecursoNaoEncontradoException exception) {
		return new ErroResponse(exception.getMessage(), List.of());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErroResponse tratarErroInterno(Exception exception) {
		return new ErroResponse("Erro interno no servidor", List.of());
	}
}
