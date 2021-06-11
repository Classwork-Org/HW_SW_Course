/*************************************************************************
 *
 *  FUNCTION:  Prm2bits_12k2
 *
 *  PURPOSE:  converts the encoder parameter vector into a vector of serial
 *                      bits.
 *
 *  DESCRIPTION: The encoder parameters are:
 *
 *     LPC:
 *              1st codebook             7 bit
 *              2nd codebook             8 bit
 *              3rd codebook             8+1 bit
 *              4th codebook             8 bit
 *              5th codebook             6 bit
 *
 *     1st and 3rd subframes:
 *           pitch period                9 bit
 *           pitch gain                  4 bit
 *           codebook index              35 bit
 *           codebook gain               5 bit
 *
 *     2nd and 4th subframes:
 *           pitch period                6 bit
 *           pitch gain                  4 bit
 *           codebook index              35 bit
 *           codebook gain               5 bit
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";



#define BIT_0     0
#define BIT_1     1
#define MASK      0x0001
#define PRM_NO    57


behavior Prm2bits_12k2 (
  in Word16 prm[PRM_SIZE],       /* input : analysis parameters  (57 param) */
  out unsigned bit[BITS_PER_FRAME-1:0] serial /* output: 244 serial bits    */
  )
{


  /*************************************************************************
   *
   *  FUNCTION:  Int2bin
   *
   *  PURPOSE:  convert integer to binary and write the bits to the array
   *            bitstream[]. The most significant bits are written first.
   *
   *************************************************************************/

  void Int2bin (
	Word16 value,       /* input : value to be converted to binary      */
	Word16 no_of_bits,  /* input : number of bits associated with value */
	Word16 *bitstream   /* output: address where bits are written       */
	)
    {
      Word16 *pt_bitstream;
      Word16 i, bt;

      pt_bitstream = &bitstream[no_of_bits];  

      for (i = 0; i < no_of_bits; i++)
	{
	  bt = value & MASK;

	  if (bt == 0)
	    {
	      *--pt_bitstream = BIT_0;
	    }
	  else
	    {
	      *--pt_bitstream = BIT_1;
	    }
	  value = shr (value, 1);
	}
    }




  void main(void)
    {
      Word16 i;
      Word16 *p_bits;

      Word16 bits[SERIAL_SIZE-1];

      static const Word16 bitno[PRM_NO] =
      {
        7, 8, 9, 8, 6,                          /* LSP VQ          */
        9, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 5,  /* first subframe  */
        6, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 5,  /* second subframe */
        9, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 5,  /* third subframe  */
        6, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 5   /* fourth subframe */
      };

      p_bits = bits;

      for (i = 0; i < PRM_NO; i++)
	{
	  Int2bin (prm[i], bitno[i], p_bits);
	  p_bits += bitno[i];
	}

	  for (i=0; i < (SERIAL_SIZE-1); i++)
	    {
	      serial[i] = bits[i];
	    }

    }
};

