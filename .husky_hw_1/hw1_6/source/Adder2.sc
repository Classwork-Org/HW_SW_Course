//
// Adder2.sc:
// ----------
//
// author:	Rainer Doemer
// last update:	09/17/14
//
// note:	this file is an example for the specification of
//		structural und parallel hierarchy and netlists;
//		an 8-bit adder is built from eight full-adders that
//		are built from half-adders which again are built from
//		simple gates; also, a testbench is included that can
//		be used to supply test vectors (summands) to the design
//		directly from the command shell (without recompiling);
//		please note that timing is ignored in this example;
//		compared to Adder.sc which is strictly sequential,
//		this Adder2.sc incorporates parallelism and signals;

#include <stdlib.h>
#include <stdio.h>

behavior OR2(			// two-port OR gate
	in  signal bit[1] a,
	in  signal bit[1] b,
	out signal bit[1] c)
{
    void main(void)
    {
	while(true)
	{
	    c = a | b;
	    wait a rising, a falling, b rising, b falling;
	}
    }
};

behavior AND2(			// two-port AND gate
	in  signal bit[1] a,
	in  signal bit[1] b,
	out signal bit[1] c)
{
    void main(void)
    {
	while(true)
	{
	    c = a & b;
	    wait a rising, a falling, b rising, b falling;
	}
    }
};

behavior XOR2(			// two-port XOR gate
	in  signal bit[1] a,
	in  signal bit[1] b,
	out signal bit[1] c)
{
    void main(void)
    {
	while(true)
	{
	    c = a ^ b;
	    wait a rising, a falling, b rising, b falling;
	}
    }
};

behavior HA(			// half-adder
	in  signal bit[1] a,
	in  signal bit[1] b,
	out signal bit[1] s,
	out signal bit[1] c)
{
    XOR2	xor1(a, b, s);
    AND2	and1(a, b, c);

    void main(void)
    {
	par { xor1; and1; }
    }
};

behavior FA(			// full-adder
	in  signal bit[1] a,
	in  signal bit[1] b,
	in  signal bit[1] c,
	out signal bit[2] s)
{
    signal bit[1] x, y, z;
    HA		ha1(a, b, y, x),
		ha2(y, c, s[0], z);
    OR2		or1(x, z, s[1]);

    void main(void)
    {
	par { ha1; ha2; or1; }
    }
};

behavior ADD8(			// 8-bit adder
	in  signal bit[1] c_in,
	in  signal bit[8] a,
	in  signal bit[8] b,
	out signal bit[8] s,
	out signal bit[1] c_out)
{
    signal bit[1] c1, c2, c3, c4, c5, c6, c7;

    FA	fa0(a[0], b[0], c_in, c1 @ s[0]),
	fa1(a[1], b[1], c1, c2 @ s[1]),
	fa2(a[2], b[2], c2, c3 @ s[2]),
	fa3(a[3], b[3], c3, c4 @ s[3]),
	fa4(a[4], b[4], c4, c5 @ s[4]),
	fa5(a[5], b[5], c5, c6 @ s[5]),
	fa6(a[6], b[6], c6, c7 @ s[6]),
	fa7(a[7], b[7], c7, c_out @ s[7]);

    void main(void)
    {
	par { fa0; fa1; fa2; fa3; fa4; fa5; fa6; fa7; }
    }
};

behavior Testbench(
    in  int		a,
    in  int		b,
    out signal bit[8]	In1,
    out signal bit[8]	In2,
    in  signal bit[8]	Out)
{
    void main(void)
    {
	bit[8]		a8, b8, s8;

	printf("8-bit adder: %d + %d =", a, b);

	In1 = a;
	In2 = b;
	waitfor 1;

	printf(" %d", (int)Out);

	a8 = a;
	b8 = b;
	s8 = a8 + b8;
	if (Out != s8)
	   { printf(" FALSE!!! (should be %d)\n", (int)s8);
	     exit(10);
	    } /* fi */
	else
	   { printf(" (correct)\n");
	    } /* esle */

	exit(0);	// terminate simulation
    }
};

behavior Main
{
    int			a, b;
    signal bit[8]	In1, In2, Out;

    ADD8	Add08(0b, In1, In2, Out, );
    Testbench	Tb(a, b, In1, In2, Out);

    int main(int argc, char **argv)
    {
	if (argc == 3)
	   { a = atoi(argv[1]);
	     b = atoi(argv[2]);
	    } /* fi */
	else
	   { a = 27;
	     b = 15;
	    } /* esle */

	par { Add08; Tb; }

	return(10);	// never reached
    }
};

// EOF
