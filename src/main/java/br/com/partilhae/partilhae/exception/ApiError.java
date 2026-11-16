package br.com.partilhae.partilhae.exception;

import java.util.Map;

public record ApiError(String timestamp, int status, String error, String message,
                       String path, Map<String, String> fields) { }
