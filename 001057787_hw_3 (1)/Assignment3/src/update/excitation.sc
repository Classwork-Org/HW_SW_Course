//
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";


behavior Excitation (
     in Word16 gain_pit,
     in Word16 gain_code,
     in Word16 code[L_SUBFR],      /* (i)      : fixed codebook excitation */
        Word16 exc_i[L_SUBFR]
)
{
void main(void)
  {
    Int i;
    Word32 L_temp;

    /*------------------------------------------------------*
     * - Find the total excitation                          *
     * - find synthesis speech corresponding to exc[]       *
     * - update filter memories for finding the target      *
     *   vector in the next subframe                        *
     *   (update mem_err[] and mem_w0[])                    *
     *------------------------------------------------------*/
    
    for (i = 0; i < L_SUBFR; i++)
      {
	/* exc[i] = gain_pit*exc[i] + gain_code*code[i]; */
	
	L_temp = L_mult (exc_i[i], gain_pit);
	L_temp = L_mac (L_temp, code[i], gain_code);
	L_temp = L_shl (L_temp, 3);
	exc_i[i] = round (L_temp);                   
      }
    
  }
};
