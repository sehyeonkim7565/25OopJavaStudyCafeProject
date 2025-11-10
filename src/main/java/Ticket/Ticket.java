package Ticket;

public class Ticket {
	int Duration;
	int Time;
	String isValid(int Time) {
		if (Time>0)
			return "YES";
		else 
			return "NO";
	}
	void use() {
		
	}
	
}
