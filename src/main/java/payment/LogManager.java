package payment;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import Seat.UsageSession;

public class LogManager implements ILogManager {
    private static final String LOG_DIRECTORY = "logs";
    private static final String PAYMENTS_LOG_FILE = LOG_DIRECTORY + "/payments.jsonl";
    private static final String USAGE_LOG_FILE = LOG_DIRECTORY + "/usage.jsonl";

    // 11/24 주문 로그 파일 .jsonl
    private static final String ORDER_LOG_FILE = LOG_DIRECTORY + "/order.jsonl";
    private final Gson gson;

    public LogManager() {
        this.gson = new Gson();

        File dir = new File(LOG_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void savePaymentLog(Payment payment) {
        String logEntry = gson.toJson(payment);
        appendLogToFile(PAYMENTS_LOG_FILE, logEntry);
    }

    @Override
    public void saveUsageLog(UsageSession session) {
        String logEntry = gson.toJson(session);
        appendLogToFile(USAGE_LOG_FILE, logEntry);
    }

    // 11/24 ILogManager 인터페이스에서 정의한 saveOrderLog 구현
    @Override
    public void saveOrderLog(String logEntry) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try {
            appendLogToFile(ORDER_LOG_FILE, logEntry);
        } catch (Exception e) {
            System.err.println("주문 로그 파일 기록 중 오류 발생: " + ORDER_LOG_FILE);
            e.printStackTrace();
        }
    }

    private synchronized void appendLogToFile(String filePath, String logEntry) {
        try (FileWriter fw = new FileWriter(filePath, true);
             PrintWriter pw = new PrintWriter(fw)) {
            File file = new File(filePath);
            if (file.length() > 0) {
                pw.println();
            }
            pw.print(logEntry);
        } catch (IOException e) {
            System.err.println("로그 파일 쓰기 실패: " + filePath);
            e.printStackTrace();
        }
    }
}
