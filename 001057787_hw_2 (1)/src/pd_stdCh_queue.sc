#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sim.sh>
#include <limits.h>
#include <math.h>

import "c_queue";

const unsigned long SIZE = 1;	// number of bytes available in the queue

// sender behavior with external input Msg
behavior S(i_sender Port) {
	unsigned int i = 0;
	signal int idx;
	signal char txChar_signal;
	// signal char txChar_signal_test;
	signal int time;
	void main(void) 	{
		// constant string to be sent
		char msg[] = "Hello World";
		char txChar;
		printf("Sender starting...\n");

		// Send msg one char at a time
		do {
			idx = i;
			txChar_signal = msg[i];
			txChar = msg[i];
			// txChar_signal_test = txChar;
			// annoying condition to avoid \0 to be printed, but I want to send it as a termintion character
			if (txChar != '\0') { 
				printf("Sending '%c'\n", txChar);
			}
			time = now();
			Port.send(&txChar,sizeof(txChar)); // send using the channel
			waitfor(5);
			i++;           // increment position in string
		} while (txChar != '\0'); // strings are \0 terminated, stop after \0 is sent

	}
};

// receiver behavior
behavior R(i_receiver Port) {
	signal char rxChar_signal;
	// signal int time;
	void main(void)	{
		char rxChar;
		printf("Receiver starting...\n");

		while(1) {
			// time = now();
			Port.receive(&rxChar,sizeof(rxChar)); // receive using the channel 
			rxChar_signal = rxChar;	
			waitfor(10);
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
	c_queue c(SIZE);
	S s(c);
	R r(c);
	signal int temp_time;
	int main(void)	{
		// temp_time = now();
		// waitfor(1);
		// temp_time = now();
		par {
			r;
			s;
		}
		printf("%s\n", "DONE!");
		printf("%d\n", now());
		return 0;
	}
};
