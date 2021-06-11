//
// FSM.sc:
// -------
//
// author:	Rainer Doemer
// last update:	08/02/05
//
// note:	this SpecC example describes a simple finite state machine;
//		state transitions are triggered by an external clock;
//		when executed, the FSM sequentially runs all states and
//		then terminates;


#include <sim.sh>
#include <stdio.h>


bool	Condition1 = false,
	Condition2 = true;


// definition of states as child behaviors

behavior State1 (event clk)
{
	void main(void)
	{
	sim_time_string	buf;
	printf("Time =%5s : State 1 active...\n", time2str(buf, now()));
	wait(clk);
	}
};

behavior State2 (event clk)
{
	void main(void)
	{
	sim_time_string	buf;
	printf("Time =%5s : State 2 active...\n", time2str(buf, now()));
	wait(clk);
	}
};

behavior State3 (event clk)
{
	void main(void)
	{
	sim_time_string	buf;
	printf("Time =%5s : State 3 active...\n", time2str(buf, now()));
	wait(clk);
	}
};

behavior State4 (event clk)
{
	void main(void)
	{
	sim_time_string	buf;
	printf("Time =%5s : State 4 active...\n", time2str(buf, now()));
	wait(clk);
	}
};

behavior State5 (event clk)
{
	void main(void)
	{
	sim_time_string	buf;
	printf("Time =%5s : State 5 active...\n", time2str(buf, now()));
	wait(clk);
	}
};

behavior State6 (event clk)
{
	void main(void)
	{
	sim_time_string	buf;
	printf("Time =%5s : State 6 active...\n", time2str(buf, now()));
	wait(clk);
	}
};


// definition of the FSM

behavior FSM (event clk)
{
	State1	S1(clk);
	State2	S2(clk);
	State3	S3(clk);
	State4	S4(clk);
	State5	S5(clk);
	State6	S6(clk);

	void main(void)
	{
	sim_time_string	buf;
	fsm{ S1: /* default: goto next state */
	     S2:
		{ if (Condition1)
			goto S2;
		  /* else */
			goto S3;
		 }
	     S3:
		{ if (Condition2 || Condition1)
			goto S4;
		  goto S2;
		 }
	     S4:
		{ /* default: goto next state */
		 }
	     S5:
		{ if (Condition1)
			goto S4;
		  if (! Condition2)
			goto S5;
		 }
	     S6:
		{ if (Condition1)
			goto S3;
		  if (Condition1 && !Condition2)
			goto S4;
		  break;
		 }
	    }
	printf("Time =%5s : FSM exiting...\n", time2str(buf, now()));
	}
};


// definition of the clock generator

behavior Clock (event clk)
{
	void main(void)
	{
	int		i;
	sim_time_string	buf;
	for(i=1; i<10; i++)	// the demo shouldn't run forever
		{
		waitfor(100);
		printf("Time =%5s : Clock-tick!\n", time2str(buf, now()));
		notify(clk);
		}
	}
};


// the testbench

behavior Main
{
	event	SystemClock;
	Clock	ClockGen(SystemClock);
	FSM	MyFSM(SystemClock);

	int main(void)
	{
	puts("Starting...");
	par {	ClockGen.main();
		MyFSM.main();
		}
	puts("Exiting...");
	return(0);
	}
};

// EOF
