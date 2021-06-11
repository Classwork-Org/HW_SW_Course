/*************************************************************************
 *
 *  FUNCTION:  Prm2bits_12k2
 *
 *  PURPOSE:  converts the encoder parameter vector into a vector of serial
 *            bits. The most significant bits are written first.
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


#ifndef USE_BIT_PORTS
#define BIT_0     0
#define BIT_1     1
#define MASK      0x0001
#define PRM_NO    57
#endif



behavior Prm2bits_12k2 (
  in Word16 prm[PRM_SIZE],                     /* input: 57 analysis param */
#ifndef USE_BIT_PORTS
  out Word16 serial[BITS_PER_FRAME]
#else
  out unsigned bit[BITS_PER_FRAME-1:0] serial  /* output: 244 serial bits  */
#endif
  )
{

#ifndef USE_BIT_PORTS
 void Int2bin (
        Word16 value,       /* input : value to be converted to binary      */
        Word16 no_of_bits,  /* input : number of bits associated with value */
        Word16  *bitstream   /* output: address where bits are written       */
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
#endif

  void main(void)
    {
//	waitfor(8745 * DSP_CLOCK_PERIOD);

#ifndef USE_BIT_PORTS

      Word16 i;
      Word16 *p_bits;

      Word16 bits[BITS_PER_FRAME];

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

          for (i=0; i < (BITS_PER_FRAME); i++)
            {
              serial[i] = bits[i];
            }

#else
      serial = 
	prm[56][0:5-1] @
	prm[55][0:3-1] @
	prm[54][0:3-1] @
	prm[53][0:3-1] @
	prm[52][0:3-1] @
	prm[51][0:3-1] @
	prm[50][0:4-1] @
	prm[49][0:4-1] @
	prm[48][0:4-1] @
	prm[47][0:4-1] @
	prm[46][0:4-1] @
	prm[45][0:4-1] @
	prm[44][0:6-1] @

	prm[43][0:5-1] @
	prm[42][0:3-1] @
	prm[41][0:3-1] @
	prm[40][0:3-1] @
	prm[39][0:3-1] @
	prm[38][0:3-1] @
	prm[37][0:4-1] @
	prm[36][0:4-1] @
	prm[35][0:4-1] @
	prm[34][0:4-1] @
	prm[33][0:4-1] @
	prm[32][0:4-1] @
	prm[31][0:9-1] @

	prm[30][0:5-1] @
	prm[29][0:3-1] @
	prm[28][0:3-1] @
	prm[27][0:3-1] @
	prm[26][0:3-1] @
	prm[25][0:3-1] @
	prm[24][0:4-1] @
	prm[23][0:4-1] @
	prm[22][0:4-1] @
	prm[21][0:4-1] @
	prm[20][0:4-1] @
	prm[19][0:4-1] @
	prm[18][0:6-1] @

	prm[17][0:5-1] @
	prm[16][0:3-1] @
	prm[15][0:3-1] @
	prm[14][0:3-1] @
	prm[13][0:3-1] @
	prm[12][0:3-1] @
	prm[11][0:4-1] @
	prm[10][0:4-1] @
	prm[9][0:4-1] @
	prm[8][0:4-1] @
	prm[7][0:4-1] @
	prm[6][0:4-1] @
	prm[5][0:9-1] @

	prm[4][0:6-1] @
	prm[3][0:8-1] @
	prm[2][0:9-1] @
	prm[1][0:8-1] @
	prm[0][0:7-1];
#endif
           
    }
};

