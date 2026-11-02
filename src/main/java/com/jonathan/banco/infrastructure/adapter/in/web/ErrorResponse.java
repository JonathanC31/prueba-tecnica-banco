package com.jonathan.banco.infrastructure.adapter.in.web;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(String mensaje, Map<String, String> errores, LocalDateTime fecha) {}
