#include <stdio.h>
#include <sim.sh>

import "c_queue";
import "c_double_handshake";	// import the standard channel

const unsigned long SIZE = 5;	// number of bytes available in the queue

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

behavior QueueTest
{
	c_queue c(SIZE);
	S s(c);
	R r(c);
	void main(void)	{
		printf("%s\n", "QUEUE TEST STARTING!");
		par {
			r;
			s;
		}
		printf("%s\n", "QUEUE TEST DONE!");
	}
};

behavior DoubleHandshakeTest
{
	c_double_handshake c;
	S s(c);
	R r(c);
	void main(void)	{
		printf("%s\n", "DOUBLE HANDSHAKE STARTING!");
		par {
			r;
			s;
		}
		printf("%s\n", "DOUBLE HANDSHAKE TEST DONE!");
	}
};

behavior Main
{
	QueueTest Q;
	DoubleHandshakeTest DH;
	int main(void)	{
		Q.main();
		DH.main();
		printf("%s\n", "MAIN DONE!");
		return 0;
	}
};
