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

interface BasicHandshake
{
	void Send(unsigned char);
	unsigned char Receive(void);
};

channel HandShake implements BasicHandshake
{
	unsigned char Data;
	event Valid, Ack;

	void Send(unsigned char _data)
	{
		Data = _data;
		notify Valid;
		wait Ack;
	}

	unsigned char Receive(void)
	{
		unsigned char _data;

		wait Valid;
		_data = Data;
		notify Ack;

		return _data;
	}
};


behavior Sender(BasicHandshake port)
{
	void main(void)
	{
		char msg[12] = {'H','E','L','L','O',' ','W','O','R','L','D','\0'};
		int i;
		printf("%s\n","Sender Starting...");

		for(i = 0; i<12; i++)
		{
			if(msg[i]!=0)
				printf("Sending \'%c\'\n", msg[i]);
			
			port.Send(msg[i]);
		}
	}
};

behavior Receiver(BasicHandshake port)
{
	void main(void)
	{
		unsigned char _data;

		printf("%s\n", "Receiver Starting...");
		do{

			_data = port.Receive();
				
			if (_data!=0)
				printf("Receiving \'%c\'\n", _data);

		}while(_data!='\0');
	}
};


behavior Main(void)
{

	HandShake	C;
	Sender		S(C);
	Receiver	R(C);

	int main(void)
	{
		printf("%s\n", "\n\nCHANNEL TEST\n\n");
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
