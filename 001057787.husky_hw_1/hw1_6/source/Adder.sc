//
// Adder.sc:
// ---------
//
// author:	Rainer Doemer
// last update:	08/07/05
//
// note:	this file is a simple example for the specification of
//		structural hierarchy and netlists;
//		an 8-bit adder is built from eight full-adders that
//		are built from half-adders which again are built from
//		simple gates; also, a testbench is included that can
//		be used to supply test vectors (summands) to the design
//		directly from the command shell (without recompiling);
//		please note that timing is ignored in this example
//		and the adder operates strictly sequential;
//		(see Adder2.sc for parallelism with proper signals)

#include <stdlib.h>
#include <stdio.h>

behavior OR2(			// two-port OR gate
	in bit[1]	a,
	in bit[1]	b,
	out bit[1]	c)
{
    void main(void)
    {
	c = a | b;
    }
};

behavior AND2(			// two-port AND gate
	in bit[1]	a,
	in bit[1]	b,
	out bit[1]	c)
{
    void main(void)
    {
	c = a & b;
    }
};

behavior XOR2(			// two-port XOR gate
	in bit[1]	a,
	in bit[1]	b,
	out bit[1]	c)
{
    void main(void)
    {
	c = a ^ b;
    }
};

behavior HA(			// half-adder
	in bit[1]	a,
	in bit[1]	b,
	out bit[1]	s,
	out bit[1]	c)
{
    XOR2	xor1(a, b, s);
    AND2	and1(a, b, c);

    void main(void)
    {
	xor1.main();
	and1.main();
    }
};

behavior FA(			// full-adder
	in bit[1]	a,
	in bit[1]	b,
	in bit[1]	c,
	out bit[1:0]	s)
{
    bit[1]	x, y, z;
    HA		ha1(a, b, y, x),
		ha2(y, c, s[0], z);
    OR2		or1(x, z, s[1]);

    void main(void)
    {
	ha1.main();
	ha2.main();
	or1.main();
    }
};

behavior ADD8(			// simple 8-bit adder
	in bit[1]	c_in,
	in bit[7:0]	a,
	in bit[7:0]	b,
	out bit[7:0]	s,
	out bit[1]	c_out)
{
    bit[1:7]	c = 0;

    FA	fa0(a[0], b[0], c_in, c[1] @ s[0]),
	fa1(a[1], b[1], c[1], c[2] @ s[1]),
	fa2(a[2], b[2], c[2], c[3] @ s[2]),
	fa3(a[3], b[3], c[3], c[4] @ s[3]),
	fa4(a[4], b[4], c[4], c[5] @ s[4]),
	fa5(a[5], b[5], c[5], c[6] @ s[5]),
	fa6(a[6], b[6], c[6], c[7] @ s[6]),
	fa7(a[7], b[7], c[7], c_out @ s[7]);

    void main(void)
    {
	fa0.main();
	fa1.main();
	fa2.main();
	fa3.main();
	fa4.main();
	fa5.main();
	fa6.main();
	fa7.main();
    }
};

behavior Main(void)		// testbench
{
    bit[1]	GND = 0,
		DC  = 0;
    bit[7:0]	In1 = 0,
		In2 = 0,
		Out = 0;

    ADD8	Adder(GND, In1, In2, Out, DC);

    int main(int argc, char **argv)
    {
	int		a, b;
	bit[8]		a8, b8, s8;

	if (argc == 3)
	   { a = atoi(argv[1]);
	     b = atoi(argv[2]);
	    } /* fi */
	else
	   { a = 27;
	     b = 15;
	    } /* esle */
	printf("8-bit adder: %d + %d =", a, b);

	In1 = a;
	In2 = b;
	Adder.main();

	printf(" %d", (int)Out);

	a8 = a;
	b8 = b;
	s8 = a8 + b8;
	if (Out != s8)
	   { printf(" FALSE!!! (should be %d)\n", (int)s8);
	     return(10);
	    } /* fi */
	else
	   { printf(" (correct)\n");
	    } /* esle */

	return(0);
    }
};

// EOF
