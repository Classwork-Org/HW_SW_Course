//
// Behaviors.sc:
// -------------
//
// author:	Rainer Doemer
// last update:	05/18/01
//
// note:	this SpecC example just shows the syntax
//		of so-called "clean" SpecC behaviors;
//		it has no other useful functionality


behavior B_sub;


behavior B_leaf(in int a, out int b)	/* typical leaf behavior */
{
	void main(void)
	{
	b = 2*a + 1;
	}
};

behavior B_par				/* typical parallel composition */
{
	B_sub	b1, b2, b3;

	void main(void)
	{
	par{	b1.main();
		b2.main();
		b3.main();
		}
	}
};

behavior B_pipe				/* typical pipelined composition */
{
	B_sub	b1, b2, b3;

	void main(void)
	{
	pipe{	b1.main();
		b2.main();
		b3.main();
		}
	}
};

behavior B_seq				/* simple sequential composition */
{
	B_sub	b1, b2, b3;

	void main(void)
	{
		b1.main();
		b2.main();
		b3.main();
	}
};

behavior B_fsm(in int a)		/* typical finite state machine */
{
	B_sub	b1, b2, b3;

	void main(void)
	{
	fsm{	b1:	{ 		goto b2;	}
		b2:	{ if (a > 0)	goto b1;
					goto b3;	}
		b3:	{ 		break;		}
		}
	}
};

behavior B_exception(			/* typical exception handler */
	in event e1, in event e2)
{
	B_sub	b1, b2, b3;

	void main(void)
	{
	try {	b1.main(); }
		interrupt (e1)	{ b2.main(); }
		trap (e2)	{ b3.main(); }
	}
};

behavior B_extern(in int a, out int b)	/* typical external behavior */

// unknown body (e.g. IP, treated as black box)
;


/* the rest exists just to make the example runnable */


behavior B_sub		/* dummy sub-behavior */
{
void main(void)
	{ /* do nothing */ }
};

behavior Main		/* Main behavior */
{
	int main(void)
	{
	/* no functionality */
	return(0);
	}
};

// EOF
