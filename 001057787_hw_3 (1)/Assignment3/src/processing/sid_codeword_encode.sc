#include "cnst.sh"
#include "typedef.sh"



/*************************************************************************
 *
 *   FUNCTION NAME: sid_codeword_encoder
 *
 *   PURPOSE:  Encoding of the SID codeword into the SID frame. The SID
 *             codeword consists of 95 bits, all set to '1'.
 *
 *   INPUTS:      ser1[0..243]  Serial-mode speech parameter frame before
 *                              writing SID codeword into it
 *
 *   OUTPUTS:     ser2[0..243]  Serial-mode speech parameter frame with
 *                              SID codeword written into it
 *
 *   RETURN VALUE: none
 *
 *************************************************************************/

behavior Sid_Codeword_Encoder (
#ifndef USE_BIT_PORTS
 			       in Word16 ser1[BITS_PER_FRAME],
#else
			       in  unsigned bit[BITS_PER_FRAME-1:0] ser1,
#endif
			       in  DTXctrl  txdtx_ctrl,
#ifndef USE_BIT_PORTS
 				out Word16 ser2[BITS_PER_FRAME]
#else
			       out unsigned bit[BITS_PER_FRAME-1:0] ser2
#endif
			      )
{
  void main(void)
    {
      Int i;

      /* Index map for encoding and detecting SID codeword */
      const Word16 SID_codeword_bit_idx[95] =
      {
	45,  46,  48,  49,  50,  51,  52,  53,  54,  55,
	56,  57,  58,  59,  60,  61,  62,  63,  64,  65,
	66,  67,  68,  94,  95,  96,  98,  99, 100, 101,
	102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
	112, 113, 114, 115, 116, 117, 118, 148, 149, 150,
	151, 152, 153, 154, 155, 156, 157, 158, 159, 160,
	161, 162, 163, 164, 165, 166, 167, 168, 169, 170,
	171, 196, 197, 198, 199, 200, 201, 202, 203, 204,
	205, 206, 207, 208, 209, 212, 213, 214, 215, 216,
	217, 218, 219, 220, 221
      };
      
      /* copy frame bits */
#ifndef USE_BIT_PORTS
	for(i=0; i < BITS_PER_FRAME ; i++)
		ser2[i]=ser1[i];

#else
      ser2 = ser1;
#endif
      
      /* encode codeword if necessary */
      if ((txdtx_ctrl & TX_SP_FLAG) == 0) 
      {
	for (i = 0; i < 95; i++)
	{
	  ser2[SID_codeword_bit_idx[i]] = 1;
	}
      }
    }
};


