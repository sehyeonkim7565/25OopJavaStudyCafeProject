package payment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PriceDataLoader {
    public Map<TicketProduct, Integer> loadPricesFromJson(String filePath) {
        Gson gson = new Gson();
        Map<TicketProduct, Integer> priceList = new HashMap<>();

        // 워킹 디렉터리가 어디든 읽을 수 있도록 기본 경로+fallback 경로를 모두 시도
        java.nio.file.Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            path = Paths.get("src/main/java", filePath);
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            Type stringMapType = new TypeToken<Map<String, Integer>>() {}.getType();
            Map<String, Integer> stringPriceMap = gson.fromJson(reader, stringMapType);

            for (Map.Entry<String, Integer> entry : stringPriceMap.entrySet()) {
                try {
                    TicketProduct product = TicketProduct.valueOf(entry.getKey());
                    priceList.put(product, entry.getValue());
                } catch (IllegalArgumentException e) {
                    System.err.println("경고: " + entry.getKey() + "는 이용권 목록에 없습니다.");
                }
            }
        } catch (IOException e) {
            System.err.println("오류: 가격 파일을 읽을 수 없습니다. 경로: " + filePath);
            e.printStackTrace();
        }

        return priceList;
    }

}
