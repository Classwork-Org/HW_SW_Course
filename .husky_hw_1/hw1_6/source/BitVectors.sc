//
// BitVectors.sc:
// --------------
//
// author:	Rainer Doemer
// last update:	05/18/01
//
// note:	this SpecC example demonstrates the use of bitvectors
//		(without making much more sense)


#include <stdio.h>
#include <assert.h>


behavior Reverse16(in bit[15:0] In, out bit[15:0] Out)
{
	void main(void)
	{
	Out = In[0:15];	// reverse slice access
	}
};


behavior Adder8x16(in bit[8] a, in bit[16] b, out bit[16] c)
{
	void main(void)
	{
	c = a + b;	// automatic extension from 8 to 16 bits for a
	}
};


behavior Multiplier16x16(in bit[16] a, in bit[16] b, out bit[32] c)
{
	void main(void)
	{
	bit[32]		tmp_a,
			tmp_b;

	tmp_a = a;		// automatic signed extension
	tmp_b = b;
	c = tmp_a * tmp_b;	// return full 32 bit result
	}
};


behavior OnesCounter64(in bit[64] b, out int Ones)
{
	void main(void)
	{
	int	i, n;

	n = 0;
	for(i=0; i<64; i++)
		n += b[i];
	Ones = n;
	}
};


behavior Encoder32(in bit[31:0] In, out bit[31:0] Out, in int Key)
{
	bit[7:0] ByteMagic(bit[3:0] Nibble1, bit[3:0] Nibble2)
	{
	bit[7:0] Tmp;

	Tmp = Nibble1[0:1] @ Nibble2[2:3] @ Nibble2[0:1] @ Nibble1[2:3];
	Tmp += 42;
	Tmp[3] = (Tmp[5] ^ 1) | (Tmp[1:2] @ Tmp[5:6])[2];
	Tmp[3:6] ^= Tmp[1:4];
	return(Tmp ^ 01010101b);
	}

	void main(void)
	{
	bit[31:0]	Tmp;

	Tmp = In + Key;
	Tmp[ 0: 7] = ByteMagic(Tmp[ 7: 4], Tmp[ 3: 0]);
	Tmp[ 8:15] = ByteMagic(Tmp[15:12], Tmp[11: 8]);
	Tmp[16:23] = ByteMagic(Tmp[23:20], Tmp[19:16]);
	Tmp[24:31] = ByteMagic(Tmp[31:28], Tmp[27:24]);
	Out = Tmp;
	}
};


// testbench

behavior Main
{
bit[8]		InB8, InC8;
bit[16]		InA16, Bus16A, Bus16B, Bus16C;
bit[32]		Bus32, Out32;
bit[64]		InD64;
int		I;

Reverse16	R1(InA16, Bus16A);
Adder8x16	A1(InB8, Bus16A, Bus16B),
		A2(InC8, Bus16A, Bus16C);
Multiplier16x16	M1(Bus16B, Bus16C, Bus32);
OnesCounter64	O1(InD64, I);
Encoder32	E1(Bus32, Out32, I);


	int main(void)
	{
	const long int	Offset = 31819831 + 42;  // don't ask why!  :-)
	long int	Result;

	puts("Starting...");

	puts("Defining input values...");
	InA16 = 1111000011110000b;
	InB8  = 01110001b;
	InC8  = 10000001b;
	InD64 = 010101010000011111000000111110101010111110001111b;

	puts("Computing...");
	par { R1.main(); O1.main(); }
	assert(Bus16A == 0000111100001111b);	/* verify Reverse */
	assert(I == 26);			/* verify OnesCounter */
	par { A1.main(); A2.main(); }
	assert(Bus16B == /* 0000111100001111b + 01110001b */
				3968);		/* verify Adder */
	assert(Bus16C == /* 0000111100001111b + 10000001b */
				3728);		/* verify Adder */
	M1.main();
	assert(Bus32 == 3968 * 3728);		/* verify Multiplier */
	E1.main();

	puts("Reading the output...");
	Result = Out32 + Offset;
	printf("Result = %ld (42? Of course!!)\n", Result);
	assert(Result == 42);			/* "verify" Encoder */

	puts("Exiting...");
	return(0);
	}

}; /* end of Main */

// EOF
