package io.github.alexistrejo11.bank.notifications.api.controller;

import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationRecordsPageResponse;
import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationSummaryResponse;
import io.github.alexistrejo11.bank.notifications.application.NotificationMonitoringService;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationStatus;
import io.github.alexistrejo11.bank.shared.api.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications/monitoring")
public class NotificationMonitoringController {

	private final NotificationMonitoringService notificationMonitoringService;

	public NotificationMonitoringController(NotificationMonitoringService notificationMonitoringService) {
		this.notificationMonitoringService = notificationMonitoringService;
	}

	@GetMapping("/records")
	@PreAuthorize("hasAuthority('notifications:read')")
	public ResponseEntity<ApiResponse<NotificationRecordsPageResponse>> records(
			@RequestParam(required = false) NotificationStatus status,
			@RequestParam(required = false) NotificationChannel channel,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		var page = notificationMonitoringService.list(status, channel, pageable);
		var body = new NotificationRecordsPageResponse(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements()
		);
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@GetMapping("/summary")
	@PreAuthorize("hasAuthority('notifications:read')")
	public ResponseEntity<ApiResponse<NotificationSummaryResponse>> summary() {
		return ResponseEntity.ok(ApiResponse.success(notificationMonitoringService.summary()));
	}
}
