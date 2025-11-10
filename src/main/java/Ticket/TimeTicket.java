package Ticket;

public class TimeTicket {
	int remainingMinutes;
	String isValid(int remainingMinutes) {
		if (remainingMinutes>0)
			return "TRUE";
		else
			return "False";	
	}
	
	void deductTime(int long_minutes) {
		remainingMinutes-=long_minutes;
	}
}
