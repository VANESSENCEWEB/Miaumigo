package com.Miaumigo.Miaumigo.exception;

public class CpfJaCadastradoException extends RuntimeException {

	public CpfJaCadastradoException() {
		super("CPF já cadastrado.");
	}
}
