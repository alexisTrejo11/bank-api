package io.github.alexistrejo11.bank.audit.api.controller;

import io.github.alexistrejo11.bank.audit.api.dto.response.AuditRecordsPageResponse;
import io.github.alexistrejo11.bank.audit.api.mapper.AuditApiMapper;
import io.github.alexistrejo11.bank.audit.application.handler.query.SearchAuditRecordsHandler;
import io.github.alexistrejo11.bank.audit.domain.model.AuditRecordFilters;
import io.github.alexistrejo11.bank.shared.api.ApiResponse;
import io.github.alexistrejo11.bank.shared.openapi.BankApiKeys;
import io.github.alexistrejo11.bank.shared.openapi.BankApiOperation;
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

	private final SearchAuditRecordsHandler searchAuditRecordsHandler;

	public AuditController(SearchAuditRecordsHandler searchAuditRecordsHandler) {
		this.searchAuditRecordsHandler = searchAuditRecordsHandler;
	}

	@GetMapping("/records")
	@BankApiOperation(BankApiKeys.AUDIT_LIST)
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
		var filters = new AuditRecordFilters(eventType, actorId, entityType, entityId, from, to);
		var page = searchAuditRecordsHandler.handle(filters, pageable.getPageNumber(), pageable.getPageSize());
		return ResponseEntity.ok(ApiResponse.success(AuditApiMapper.toPageResponse(page)));
	}
}
