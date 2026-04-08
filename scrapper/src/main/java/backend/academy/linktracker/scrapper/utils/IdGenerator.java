package backend.academy.linktracker.scrapper.utils;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(System.nanoTime());

    private IdGenerator() {}

    public static long nextId() {
        return COUNTER.incrementAndGet();
    }
}
