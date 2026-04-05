package io.github.alexistrejo11.bank.shared.web;

import io.github.alexistrejo11.bank.shared.exception.BankException;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * RFC 7807 {@link ProblemDetail} responses with an extra {@code errorCode} property for machine clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
		return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), request);
	}

	@ExceptionHandler(BankException.class)
	public ResponseEntity<ProblemDetail> handleBankException(BankException ex, WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), request);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ProblemDetail> handleMissingHeader(MissingRequestHeaderException ex, WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, "MISSING_REQUEST_HEADER", ex.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(FieldError::getField, fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
						(a, b) -> a));
		ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
		pd.setTitle("Validation failed");
		pd.setDetail("Request validation failed");
		pd.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
		pd.setProperty("errorCode", "VALIDATION_ERROR");
		pd.setProperty("fieldErrors", fieldErrors);
		return ResponseEntity.unprocessableEntity().body(pd);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, WebRequest request) {
		log.error("Unexpected error", ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request);
	}

	private static ResponseEntity<ProblemDetail> build(HttpStatus status, String errorCode, String detail, WebRequest request) {
		ProblemDetail pd = ProblemDetail.forStatus(status);
		pd.setTitle(status.getReasonPhrase());
		pd.setDetail(detail);
		pd.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
		pd.setProperty("errorCode", errorCode);
		return ResponseEntity.status(status).body(pd);
	}
}
