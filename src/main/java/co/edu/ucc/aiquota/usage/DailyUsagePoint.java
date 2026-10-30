package co.edu.ucc.aiquota.usage;

import java.time.LocalDate;

public record DailyUsagePoint(LocalDate date, long tokens, int requests) {
}
