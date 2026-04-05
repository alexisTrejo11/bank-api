package io.github.alexistrejo11.bank.audit.api.controller;

import io.github.alexistrejo11.bank.audit.application.AuditQueryService;
import io.github.alexistrejo11.bank.audit.application.AuditRecordQuery;
import io.github.alexistrejo11.bank.audit.api.dto.response.AuditRecordsPageResponse;
import io.github.alexistrejo11.bank.shared.api.ApiResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

	private final AuditQueryService auditQueryService;

	public AuditController(AuditQueryService auditQueryService) {
		this.auditQueryService = auditQueryService;
	}

	@GetMapping("/records")
	@PreAuthorize("hasAuthority('audit:read')")
	public ResponseEntity<ApiResponse<AuditRecordsPageResponse>> listRecords(
			@RequestParam(required = false) String eventType,
			@RequestParam(required = false) UUID actorId,
			@RequestParam(required = false) String entityType,
			@RequestParam(required = false) UUID entityId,
			@RequestParam(required = false) Instant from,
			@RequestParam(required = false) Instant to,
			@PageableDefault(size = 20) Pageable pageable
	) {
		AuditRecordQuery query = new AuditRecordQuery(eventType, actorId, entityType, entityId, from, to);
		return ResponseEntity.ok(ApiResponse.success(auditQueryService.search(query, pageable)));
	}
}
