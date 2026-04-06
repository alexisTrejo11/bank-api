package io.github.alexistrejo11.bank.infrastructure.messaging.kafka;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DlqRingBuffer {

	private static final int MAX = 200;

	private final Deque<DlqRecord> records = new ArrayDeque<>();

	public synchronized void add(DlqRecord record) {
		while (records.size() >= MAX) {
			records.removeFirst();
		}
		records.addLast(record);
	}

	public synchronized List<DlqRecord> snapshotNewestFirst() {
		return new ArrayList<>(records).reversed();
	}
}
