package project.brianle.securestorage.domain;

import org.springframework.http.HttpStatus;

import java.util.Map;

public record Response(String time, int code, String path, HttpStatus status, String message, String exception, Map<?, ?> data) {}
