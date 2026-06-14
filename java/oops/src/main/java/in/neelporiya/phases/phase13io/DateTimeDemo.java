package in.neelporiya.phases.phase13io;

import in.neelporiya.runner.Concept;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeDemo implements Concept {
    @Override
    public String title() {
        return "Date time in Java";
    }

    @Override
    public String description() {
        return "date and time in java";
    }

    @Override
    public void run() {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.of(14, 30);
        LocalDateTime dt = LocalDateTime.now();
        ZonedDateTime zoned = ZonedDateTime.now(ZoneId.of("America/New_York"));
        Instant now = Instant.now();

        LocalDate tomorrow = date.plusDays(1);
        LocalDate lastWeek = date.minusWeeks(1);
        System.out.println(date.isBefore(tomorrow));

        Period period = Period.between(date, tomorrow);
        Duration dur = Duration.between(Instant.now(), now);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String text = dt.format(fmt);
        LocalDate parsed = LocalDate.parse("2026-12-25");

        System.out.println(date);
        System.out.println(time);
        System.out.println(dt);
        System.out.println(zoned);
        System.out.println(now);
        System.out.println(tomorrow);
        System.out.println(lastWeek);
        System.out.println(period);
        System.out.println(dur);
        System.out.println(text);
        System.out.println(parsed);
    }
}
