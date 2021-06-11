//
// Exception.sc:
// -------------
//
// author:	Rainer Doemer
// last update:	08/02/05
//
// note:	this SpecC example demonstrates exception handling;
//		two behaviors A and B run in parallel; behavior B issues
//		events to behavior A, which then reacts with interrupts
//		and abortion;


#include <sim.sh>
#include <stdio.h>
#include <assert.h>


behavior A1
{
	void main(void)
	{
	sim_time_string	buf;
	printf("A1   (time = %s): Starting...\n", time2str(buf, now()));
	waitfor 100;
	printf("A1   (time = %s): Done.\n", time2str(buf, now()));
	}
};

behavior AT
{
	void main(void)
	{
	sim_time_string	buf;
	printf("AT   (time = %s): Trapped!\n", time2str(buf, now()));
	waitfor 20;
	printf("AT   (time = %s): Trap exit.\n", time2str(buf, now()));
	}
};

behavior AI
{
	void main(void)
	{
	sim_time_string	buf;
	printf("AI   (time = %s): Interrupt!\n", time2str(buf, now()));
	waitfor 10;
	printf("AI   (time = %s): Interrupt done.\n", time2str(buf, now()));
	}
};

behavior A(in event e1, in event e2)
{
	A1	a1;
	AT	at;
	AI	ai;

	void main(void)
	{
	sim_time_string	buf;
	printf("A    (time = %s): Starting...\n", time2str(buf, now()));
	try {	a1.main();
		}
	trap e1
	    {	at.main();
		}
	interrupt e2
	    {	ai.main();
		};
	printf("A    (time = %s): Done.\n", time2str(buf, now()));
	}
};

behavior B(out event e1, out event e2)
{
	void main(void)
	{
	sim_time_string	buf;
	printf("B    (time = %s): Starting...\n", time2str(buf, now()));
	waitfor 20;
	printf("B    (time = %s): Notifying interrupt...\n", time2str(buf, now()));
	notify e2;
	waitfor 30;
	printf("B    (time = %s): Notifying interrupt...\n", time2str(buf, now()));
	notify e2;
	waitfor 20;
	printf("B    (time = %s): Notifying abortion...\n", time2str(buf, now()));
	notify e1;
	printf("B    (time = %s): Done.\n", time2str(buf, now()));
	}
};

behavior Main
{
	event	e1, e2;
	A	a(e1, e2);
	B	b(e1, e2);

	int main(void)
	{
	sim_time_string	buf;
	printf("Main (time = %s): Starting...\n", time2str(buf, now()));

	par {	a.main();
		b.main();
		}

	printf("Main (time = %s): Done.\n", time2str(buf, now()));
	return(0);
	}
};

// EOF
