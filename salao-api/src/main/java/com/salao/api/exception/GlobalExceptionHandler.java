package com.salao.api.exception;

import com.salao.common.exception.AcessoNegadoException;
import com.salao.common.exception.EntidadeNaoEncontradaException;
import com.salao.common.exception.PagamentoDuplicadoException;
import com.salao.common.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tratamento centralizado de exceções da API REST.
 *
 * <p>Todas as respostas de erro seguem o formato JSON padronizado:
 * <pre>{
 *   "timestamp": "...",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "...",
 *   "path": "/api/..."
 * }</pre></p>
 *
 * <p><strong>Issue 12 — CWE-209:</strong> Nenhum stack trace é exposto nas respostas HTTP.
 * Os detalhes completos do erro são logados no servidor (nível WARN/ERROR).</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 404 Not Found ──────────────────────────────────────────────

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleEntidadeNaoEncontrada(
            EntidadeNaoEncontradaException ex, HttpServletRequest request) {
        log.warn("Entidade não encontrada: {} — path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ── 400 Bad Request ────────────────────────────────────────────

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleRegraNegocio(
            RegraNegocioException ex, HttpServletRequest request) {
        log.warn("Regra de negócio violada: {} — path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argumento inválido: {} — path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        log.warn("Estado inválido: {} — path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Bean Validation errors — retorna lista de campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validação falhou: {} — path: {}", mensagem, request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    /**
     * Tipo errado no parâmetro (ex: enum inválido em @RequestParam).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String mensagem = String.format(
                "Valor inválido para o parâmetro '%s': '%s'. Valores aceitos: %s",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null && ex.getRequiredType().isEnum()
                        ? java.util.Arrays.toString(ex.getRequiredType().getEnumConstants())
                        : "ver documentação");
        log.warn("Tipo inválido no parâmetro: {} — path: {}", mensagem, request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    // ── 403 Forbidden ──────────────────────────────────────────────

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, Object>> handleAcessoNegado(
            RuntimeException ex, HttpServletRequest request) {
        log.warn("Acesso negado: {} — path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, "Acesso negado.", request);
    }

    // ── 409 Conflict ───────────────────────────────────────────────

    @ExceptionHandler(PagamentoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handlePagamentoDuplicado(
            PagamentoDuplicadoException ex, HttpServletRequest request) {
        log.warn("Pagamento duplicado: {} — path: {}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // ── 500 Internal Server Error (fallback) ───────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        // Log completo no servidor — sem stack trace na resposta HTTP
        log.error("Erro interno não tratado — path: {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Por favor, tente novamente mais tarde.", request);
    }

    // ── Utilitário ─────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
