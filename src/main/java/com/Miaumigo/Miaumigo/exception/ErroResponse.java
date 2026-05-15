package com.Miaumigo.Miaumigo.exception;

import java.util.List;

public record ErroResponse(String mensagem, List<String> erros) {
}
