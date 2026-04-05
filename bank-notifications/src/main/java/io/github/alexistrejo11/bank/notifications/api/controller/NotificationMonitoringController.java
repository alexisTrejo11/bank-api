package io.github.alexistrejo11.bank.notifications.api.controller;

import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationRecordsPageResponse;
import io.github.alexistrejo11.bank.notifications.api.dto.response.NotificationSummaryResponse;
import io.github.alexistrejo11.bank.notifications.api.mapper.NotificationApiMapper;
import io.github.alexistrejo11.bank.notifications.application.handler.query.GetNotificationSummaryHandler;
import io.github.alexistrejo11.bank.notifications.application.handler.query.ListNotificationRecordsHandler;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationChannel;
import io.github.alexistrejo11.bank.notifications.domain.model.NotificationRecordFilters;
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

	private final ListNotificationRecordsHandler listNotificationRecordsHandler;
	private final GetNotificationSummaryHandler getNotificationSummaryHandler;

	public NotificationMonitoringController(
			ListNotificationRecordsHandler listNotificationRecordsHandler,
			GetNotificationSummaryHandler getNotificationSummaryHandler
	) {
		this.listNotificationRecordsHandler = listNotificationRecordsHandler;
		this.getNotificationSummaryHandler = getNotificationSummaryHandler;
	}

	@GetMapping("/records")
	@PreAuthorize("hasAuthority('notifications:read')")
	public ResponseEntity<ApiResponse<NotificationRecordsPageResponse>> records(
			@RequestParam(required = false) NotificationStatus status,
			@RequestParam(required = false) NotificationChannel channel,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		var page = listNotificationRecordsHandler.handle(
				new NotificationRecordFilters(status, channel),
				pageable.getPageNumber(),
				pageable.getPageSize()
		);
		return ResponseEntity.ok(ApiResponse.success(NotificationApiMapper.toPageResponse(page)));
	}

	@GetMapping("/summary")
	@PreAuthorize("hasAuthority('notifications:read')")
	public ResponseEntity<ApiResponse<NotificationSummaryResponse>> summary() {
		return ResponseEntity.ok(ApiResponse.success(NotificationApiMapper.toSummaryResponse(getNotificationSummaryHandler.handle())));
	}
}
