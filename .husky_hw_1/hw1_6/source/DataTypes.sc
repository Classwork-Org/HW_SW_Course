//
// DataTypes.sc:
// -------------
//
// author:	Rainer Doemer
// last update:	08/02/05
//
// note:	this SpecC example lists the basic datatypes supported by SpecC
//		along with their (system dependent) value ranges;
//		except for debugging purposes, it has no useful functionality;


#include <sim.sh>
#include <stdio.h>


behavior Main
{

	// standard C/C++ datatypes

typedef void	   Void;	//  0 bit
bool		   Boolean;	//  1 bit,           [false=0, true=1]
char		   Character;	//  8 bit, signed,   [-128, 127]
unsigned char	   UCharacter;	//  8 bit, unsigned, [0, 255]
short		   Short;	// 16 bit, signed,   [-32768, 32767]
unsigned short	   UShort;	// 16 bit, unsigned, [0, 65535]
int		   Integer;	// 32 bit, signed,   [-2147483648, 2147483647]
unsigned int	   UInteger;	// 32 bit, unsigned, [0, 4294967295]
long		   Long;	// 32 bit, signed,   [-2147483648, 2147483647]
unsigned long	   ULong;	// 32 bit, unsigned, [0, 4294967295]
float		   Float;	// 32 bit
double		   Double;	// 64 bit
long double	   LongDouble;	// 96 bit


	// special SpecC datatypes

const int l = 42;
const int r =  1;
const int n = (l > r) ? (l-r+1) : (r-l+1);

bit[l:r]	   BitVector;	//  n bit, signed,   [-(1<<(n-1)), (1<<(n-1)-1)]
unsigned bit[l:r]  UBitVector;	//  n bit, unsigned, [0, (1<<n-1)]

long long	   LongLong;	// 64 bit, signed,   [-9223372036854775808,
				//			9223372036854775807]
unsigned long long ULongLong;	// 64 bit, unsigned, [0, 18446744073709551615]

sim_time	   TimeNS;	// alias for 'unsigned long long',
				// typically in nanoseconds (now: picoseconds!)

event		   Event;	// no value


	// demonstration of SpecC constants

int main(void)
{
char		Buffer[n+1];
sim_time_string	buf;

// Void		no value
Boolean		= true;
Character	= 'c';
UCharacter	= '\x3f';
Short		= (short) -12345;
UShort		= (unsigned short) 12345;
Integer		= -123123123;
UInteger	=  123123123u;
Long		= -123123123l;
ULong		=  123123123ul;
Float		=  3.1415f;
Double		=  1234.56789e-33;
LongDouble	=  123456789.0123456789012345e+123l;
BitVector	=  010101010000111111111100000001111100011110b;
UBitVector	=  100000000000000000000000000000000000000000ub;
LongLong	= -1234567890123456789ll;
ULongLong	=  1234567890123456789ull;
TimeNS		=  365ull*24ull*60ull*60ull*1000ull*1000ull*1000ull;
// Event	no value


	// output the constants

printf("Boolean    = %s\n", (Boolean ? "true" : "false"));
printf("Character  = '%c'\n", Character);
printf("UCharacter = '%c'\n", UCharacter);
printf("Short      = (short) %d\n", Short);
printf("UShort     = (unsigned short) %d\n", UShort);
printf("Integer    = %d\n", Integer);
printf("UInteger   = %uu\n", UInteger);
printf("Long       = %ldl\n", Long);
printf("ULong      = %luul\n", ULong);
printf("Float      = %.6ef\n", Float);
printf("Double     = %.15e\n", Double);
printf("LongDouble = %.24Lel\n", LongDouble);
printf("BitVector  = %sb\n", bit2str(2, &Buffer[n], BitVector));
printf("UBitVector = %sub\n", ubit2str(2, &Buffer[n], UBitVector));
printf("LongLong   = %sll\n", ll2str(10, &Buffer[n], LongLong));
printf("ULongLong  = %sull\n", ull2str(10, &Buffer[n], ULongLong));
printf("TimeNS     = %s\n", time2str(buf, TimeNS));

return(0);
}

}; /* end of Main */

// EOF
