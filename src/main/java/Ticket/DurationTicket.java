package Ticket;
import java.time.LocalDate;

public class DurationTicket {
	int expiryDate; //후에 달, 일로 바꿈
	int isValid(int expiryDate) {
		LocalDate now=new LocalDate();
		int month=now.getMonthValue();
		int day=now.getDayOfMonth();
		if (expiryDateMonth<=month && expiryDateDay<=day) //후에 expiryDateMonth 및 Day 생성예정
			return "만료 이전입니다";
		else
			return "만료됐습니다";
	}
}
