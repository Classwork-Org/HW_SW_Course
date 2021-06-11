//
// Callback.sc:
// ------------
//
// author:	Rainer Doemer
// last update:	08/02/05
//
// note:	this SpecC example demonstrates a call-back communication
//		between a receiver r and a sender s via a channel c;
//		two interfaces IS and IR are used to let the channel
//		call-back the sender and the receiver

// some adjustable constants

#define BLOCKS		16	/* number of blocks to transmit */
#define BLOCK_SIZE	10	/* size of each block */

#define VERBOSE			/* tell me what you're doing */
//#define VERY_VERBOSE		/* tell me exactly what you're doing */

#define TIMED			/* enable timing */
#define SEND_CYCLE	10	/* sending speed (cycle time) */
#define RECEIVE_CYCLE	10	/* receiving speed (cycle time) */
#define PRODUCER_DELAY	1000	/* time to compute the data to be sent */


#include <sim.sh>		/* include simulation API */


#ifdef VERY_VERBOSE
#ifndef VERBOSE
#define VERBOSE
#endif
#endif /* VERY_VERBOSE */
#ifdef VERBOSE
#include <stdio.h>
#endif /* VERBOSE */


// interface of the sender

interface IS
{
	int get_data(void);		/* sender supplies data */
};


// interface of the receiver

interface IR
{
	void put_data(int Data);	/* receiver stores data */
};


// interface of the channel

interface IC
{
	void send_block(	/* sends data supplied through IS */
		IS	s,
		int	Size);
	void receive_block(	/* receives data to be stored trough IR */
		IR	r);
};


// the channel with the callback feature

channel C implements IC
{
	int	DataWire;		/* communication media */
	bool	ValidWire = false;
	event	SyncWire;

	void send_word(int Word)	/* internal sender routine */
		{
#ifdef VERY_VERBOSE
		sim_time_string	buf;
		printf("Time =%5sns: C::send_word(%d)\n",
			time2str(buf, now()), Word);
#endif /* VERY_VERBOSE */
		while(ValidWire)
		   { wait SyncWire;
		    }
		DataWire = Word;
		ValidWire = true;
#ifdef TIMED
		waitfor SEND_CYCLE;
#endif /* TIMED */
		notify(SyncWire);
		}

	int receive_word(void)		/* internal receiver routine */
		{
		int	ReceivedData;

		while (! ValidWire)
		   { wait SyncWire;
		    }
		ReceivedData = DataWire;
		ValidWire = false;
#ifdef TIMED
		waitfor RECEIVE_CYCLE;
#endif /* TIMED */
		notify(SyncWire);
#ifdef VERY_VERBOSE
		sim_time_string	buf;
		printf("Time =%5sns: C::receive_word() returns %d\n",
			time2str(buf, now()), ReceivedData);
#endif /* VERY_VERBOSE */
		return(ReceivedData);
		}

	void send_block(IS s, int Size)	/* exported sender method */
		{
		int	i;

#ifdef VERBOSE
		sim_time_string	buf;
		printf("Time =%5sns: C::send_block(%d)\n",
			time2str(buf, now()), Size);
#endif /* VERBOSE */
		send_word(Size);
		for(i=0; i<Size; i++)
		   { send_word(s.get_data());
		    }
		}

	void receive_block(IR r)	/* exported receiver method */
		{
		int	Size;
		int	i;
	  
#ifdef VERBOSE
		sim_time_string	buf;
		printf("Time =%5sns: C::receive_block()\n",
			time2str(buf, now()));
#endif /* VERBOSE */
		Size = receive_word();
		for(i=0; i<Size; i++)
		   { r.put_data(receive_word());
		    }
		}
};


// the sender behavior

behavior S (IC p1) implements IS
{
	const int	BlockSize = BLOCK_SIZE;
	const int	Size = BLOCKS * BlockSize;
	int		Memory[Size];
	int		Offset = 0;

	int get_data(void)		/* public callback method */
		{
		return(Memory[Offset++]);
		}

	void main(void)			/* exported functionality */
		{
		int	i;
	  
#ifdef VERBOSE
		sim_time_string	buf;
		printf("Time =%5sns: S::main()\n",
			time2str(buf, now()));
#endif /* VERBOSE */
		for(i=0; i<Size; i++)		/* fill the memory */
		   { Memory[i] = 1000001 + i;
		    }
#ifdef TIMED
		waitfor PRODUCER_DELAY;
#endif /* TIMED */
		for(i=0; i<Size/BlockSize; i++)	/* send block by block */
		   { p1.send_block(this, BlockSize);
		    }
		}
};


// behavior acting as a receiver

behavior R (IC p1) implements IR
{
	const int	Size = BLOCKS * BLOCK_SIZE;
	int		Memory[Size];
	int		Offset = 0;

	void put_data(int Data)		/* public callback method */
		{
		Memory[Offset++] = Data;
		}

	void main(void)			/* exported functionality */
		{
#ifdef VERBOSE
		sim_time_string	buf;
		printf("Time =%5sns: R::main()\n",
			time2str(buf, now()));
#endif /* VERBOSE */
		while(Offset < BLOCKS * BLOCK_SIZE)	/* receive all blocks */
		   { p1.receive_block(this);
		    }
		}
};


// the testbench

behavior Main
{	
	C	c;	/* using channel c	*/
	R	r(c);	/* connect a receiver r	*/
	S	s(c);	/* with a sender s	*/

	int main(void)
	{
#ifdef VERBOSE
	sim_time_string	buf;
	printf("Time =%5sns: Main::main(): Starting S and R in parallel...\n",
		time2str(buf, now()));
#endif /* VERBOSE */
	par {	s.main();	/* sender and receiver run in parallel */
		r.main();
		}
#ifdef VERBOSE
	printf("Time =%5sns: Main::main(): Exiting...\n",
		time2str(buf, now()));
#endif /* VERBOSE */
	return(0);
	}
};


// EOF
