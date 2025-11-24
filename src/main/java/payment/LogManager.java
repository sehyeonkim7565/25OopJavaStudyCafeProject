package payment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import Seat.UsageSession;

public class LogManager implements ILogManager {
    private static final String LOG_DIRECTORY = "logs";
    private static final String PAYMENTS_LOG_FILE = LOG_DIRECTORY + "/payments.jsonl";
    private static final String USAGE_LOG_FILE = LOG_DIRECTORY + "/usage.jsonl";
    private static final String ORDER_LOG_FILE = LOG_DIRECTORY + "/orders.jsonl";

    private final Gson gson;

    public LogManager() {
        this.gson = new Gson();
        ensureDirectory(resolvePath(LOG_DIRECTORY));
    }

    @Override
    public void savePaymentLog(Payment payment) {
        appendLogToFile(PAYMENTS_LOG_FILE, gson.toJson(payment));
    }

    @Override
    public void saveOrderLog(OrderLogEntry entry) {
        if (entry == null) return;
        appendLogToFile(ORDER_LOG_FILE, gson.toJson(entry));
    }

    @Override
    public void saveUsageStart(UsageSession session) {
        UsageLogEntry entry = UsageLogEntry.startOf(session);
        appendLogToFile(USAGE_LOG_FILE, gson.toJson(entry));
    }

    @Override
    public void updateUsageEnd(UsageSession session) {
        Path path = resolvePath(USAGE_LOG_FILE);
        ensureDirectory(path.getParent());

        List<String> lines = new ArrayList<>();
        try {
            if (Files.exists(path)) {
                lines = Files.readAllLines(path);
            }
        } catch (IOException e) {
            System.err.println("사용 로그 읽기 실패: " + path);
        }

        UsageLogEntry updated = UsageLogEntry.endOf(session);
        boolean replaced = false;
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) continue;
            try {
                UsageLogEntry entry = gson.fromJson(line, UsageLogEntry.class);
                if (entry != null
                        && session.getUserId().equals(entry.memberId)
                        && entry.endTime == null
                        && (entry.type == null || "SESSION".equals(entry.type))) {
                    lines.set(i, gson.toJson(updated));
                    replaced = true;
                    break;
                }
            } catch (JsonSyntaxException ignore) {
            }
        }

        if (!replaced) {
            lines.add(gson.toJson(updated));
        }

        try (FileWriter fw = new FileWriter(path.toFile(), false);
             PrintWriter pw = new PrintWriter(fw)) {
            for (int i = 0; i < lines.size(); i++) {
                pw.print(lines.get(i));
                if (i < lines.size() - 1) pw.println();
            }
        } catch (IOException e) {
            System.err.println("사용 로그 업데이트 실패: " + path);
            e.printStackTrace();
        }
    }

    @Override
    public void refreshOngoingUsageDurations() {
        Path path = resolvePath(USAGE_LOG_FILE);
        if (!Files.exists(path)) return;

        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            return;
        }

        boolean changed = false;
        List<String> updated = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                updated.add(line);
                continue;
            }
            try {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                String type = obj.has("type") ? obj.get("type").getAsString() : "SESSION";
                if ("MOVE".equals(type)) {
                    updated.add(line);
                    continue;
                }

                UsageLogEntry entry = gson.fromJson(obj, UsageLogEntry.class);
                if (entry != null && entry.endTime == null && entry.startTime != null) {
                    try {
                        LocalDateTime start = LocalDateTime.parse(entry.startTime, UsageLogEntry.FORMATTER);
                        entry.durationMinutes = Math.max(0, Duration.between(start, LocalDateTime.now()).toMinutes());
                        changed = true;
                    } catch (Exception ignore) {
                    }
                }
                updated.add(gson.toJson(entry));
            } catch (Exception ex) {
                updated.add(line);
            }
        }

        if (!changed) return;

        try (FileWriter fw = new FileWriter(path.toFile(), false);
             PrintWriter pw = new PrintWriter(fw)) {
            for (int i = 0; i < updated.size(); i++) {
                pw.print(updated.get(i));
                if (i < updated.size() - 1) pw.println();
            }
        } catch (IOException e) {
            System.err.println("사용 로그 경과시간 갱신 실패: " + path);
        }
    }

    @Override
    public void logSeatMove(String memberId, String fromSeat, String toSeat, LocalDateTime movedAt) {
        SeatMoveLogEntry entry = new SeatMoveLogEntry(memberId, fromSeat, toSeat, movedAt);
        appendLogToFile(USAGE_LOG_FILE, gson.toJson(entry));
    }

    private synchronized void appendLogToFile(String filePath, String logEntry) {
        Path path = resolvePath(filePath);
        ensureDirectory(path.getParent());
        try (FileWriter fw = new FileWriter(path.toFile(), true);
             PrintWriter pw = new PrintWriter(fw)) {
            if (Files.exists(path) && Files.size(path) > 0) {
                pw.println();
            }
            pw.print(logEntry);
        } catch (IOException e) {
            System.err.println("로그 파일 쓰기 실패: " + path);
            e.printStackTrace();
        }
    }

    private static class UsageLogEntry {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private String type; // SESSION
        private String memberId;
        private String seatNumber;
        private String startTime;
        private String endTime;
        private long durationMinutes;

        private UsageLogEntry(String memberId, String seatNumber, String startTime, String endTime, long durationMinutes) {
            this.type = "SESSION";
            this.memberId = memberId;
            this.seatNumber = seatNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = durationMinutes;
        }

        static UsageLogEntry startOf(UsageSession session) {
            return new UsageLogEntry(
                    session.getUserId(),
                    session.getSeatNumber(),
                    formatTime(session.getStartTime()),
                    null,
                    0
            );
        }

        static UsageLogEntry endOf(UsageSession session) {
            return new UsageLogEntry(
                    session.getUserId(),
                    session.getSeatNumber(),
                    formatTime(session.getStartTime()),
                    formatTime(session.getEndTime()),
                    session.getDurationInMinutes()
            );
        }

        private static String formatTime(LocalDateTime time) {
            return time == null ? null : time.format(FORMATTER);
        }
    }

    private static class SeatMoveLogEntry {
        private final String type = "MOVE";
        private final String memberId;
        private final String fromSeat;
        private final String toSeat;
        private final String movedAt;

        SeatMoveLogEntry(String memberId, String fromSeat, String toSeat, LocalDateTime movedAt) {
            this.memberId = memberId;
            this.fromSeat = fromSeat;
            this.toSeat = toSeat;
            this.movedAt = UsageLogEntry.formatTime(movedAt);
        }
    }

    private Path resolvePath(String filePath) {
        String[] candidates = new String[] {
                filePath,
                "src/main/java/" + filePath,
                "../" + filePath
        };
        for (String cand : candidates) {
            Path p = Paths.get(cand).normalize();
            if (Files.exists(p) || (p.getParent() != null && Files.exists(p.getParent()))) {
                return p;
            }
        }
        return Paths.get(filePath).normalize();
    }

    private void ensureDirectory(Path dir) {
        if (dir == null) return;
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("로그 디렉터리 생성 실패: " + dir);
        }
    }
}
