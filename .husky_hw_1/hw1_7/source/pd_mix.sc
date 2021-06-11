//
// Handshaking1.sc:
// ----------------
//
// author:	Rainer Doemer
// last update:	05/18/01
//
// note:	this file is an example for a communication protocol
//		using two-way handshaking; the sender sends 10 blocks
//		of data to the receiver, performing a double-handshake
//		for each single data element transmitted;
//		this communication protocol is only safe when both
//		partners call the channel at the same time;
//		(see also Handshaking2.sc)


#include <stdio.h>
#include <stdlib.h>

behavior Sender(out event req, in event ack, out unsigned char data)
{
	void main(void)
	{
		unsigned char msg[12] = {'H','E','L','L','O',' ','W','O','R','L','D','\0'};
		int i;
		printf("%s\n","Sender Starting...");

		for(i = 0; i<12; i++)
		{
			if(msg[i]!=0)
				printf("Sending \'%c\'\n", msg[i]);
			
			data = msg[i];
			notify req;
			wait ack;
		}
	}
};

behavior Receiver(in event req, out event ack, in unsigned char data)
{
	void main(void)
	{
		printf("%s\n", "Receiver Starting...");
		do{
			wait req;			
			if (data!=0)
				printf("Receiving \'%c\'\n", data);
			notify ack;


		}while(data!='\0');
	}
};


behavior Main(void)
{

	event ack, req;
	unsigned char data;
	Receiver R(req,ack,data);
	Sender S(req,ack,data);

	int main(void)
	{
		printf("%s\n", "\n\nEVENT TEST\n\n");
		par 
		{	
			S.main();
			R.main();
		}
		printf("Exiting.\n");
		return 0;
	}
};

// EOF
