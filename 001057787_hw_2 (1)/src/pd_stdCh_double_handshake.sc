#include <stdio.h>

import "c_double_handshake";	// import the standard channel

// sender behavior with external input Msg
behavior S(i_sender Port) {
	void main(void) 	{
		// constant string to be sent
		char msg[] = "Hello World";
		unsigned int i = 0;
		char txChar;

		printf("Sender starting...\n");

		// Send msg one char at a time
		do {
			txChar = msg[i];
			// annoying condition to avoid \0 to be printed, but I want to send it as a termintion character
			if (txChar != '\0') { 
				printf("Sending '%c'\n", txChar);
			}
			Port.send(&txChar,sizeof(txChar)); // send using the channel
			i++;           // increment position in string
		} while (txChar != '\0'); // strings are \0 terminated, stop after \0 is sent

	}
};

// receiver behavior
behavior R(i_receiver Port) {
	void main(void)	{
		char rxChar;
		printf("Receiver starting...\n");

		while(1) {
			Port.receive(&rxChar,sizeof(rxChar)); // receive using the channel 
			if (rxChar == '\0'){ // strings are \0 terminated, stop when \0 is received
				break;
			}
			printf("Received '%c'\n", rxChar);
		} 
	}
};

// main behavior
behavior Main
{
	c_double_handshake c;
	S s(c);
	R r(c);
	int main(void)	{
		par {
			r;
			s;
		}
		return 0;
	}
};
