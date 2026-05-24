package com.Miaumigo.Miaumigo.exception;

public class EmailJaCadastradoException extends RuntimeException {

	public EmailJaCadastradoException() {
		super("Email já cadastrado.");
	}
}
