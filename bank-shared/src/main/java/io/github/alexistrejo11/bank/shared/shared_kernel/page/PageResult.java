package io.github.alexistrejo11.bank.shared.shared_kernel.page;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
}
