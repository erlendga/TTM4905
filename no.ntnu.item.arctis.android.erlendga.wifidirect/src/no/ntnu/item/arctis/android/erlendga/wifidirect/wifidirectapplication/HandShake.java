package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication;

public class HandShake {

	private final int IDLE = 0;
	private final int WAIT_SYN = 1;
	private final int WAIT_ACK = 2;
	int state = IDLE;
	
	public String processInput(String input) {
		String output = null;
		switch (state) {
			case IDLE:
				output = "HELLO";
				state = WAIT_SYN;
				break;
			case WAIT_SYN:
				if (input.equals("SYN")) {
					output = "SYN, ACK";
					state = WAIT_ACK;
				}
				else {
					output = "FAIL";
					state = IDLE;
				}
				break;
			case  WAIT_ACK:
				if (input.equals("ACK")) {
					output = "OK";
				}
				else {
					output = "FAIL";
				}
				state = IDLE;
			default:
				break;
			}
		return output ;
	}
}
