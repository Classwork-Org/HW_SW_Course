// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: pitch_contr.sc
// ------------------------------------------------------------------------
// 
// Subtract the adaptive codebook (pitch filter) effect from a signal.
//
//
// 09/13/02  <gerstl>	Merged in fixes for input of scrtl tool
// 07/21/98  <gerstl>
// 08/07/98  <szhao>


#include "cnst.sh"
#include "typedef.sh"

import "basic_op";


behavior Pitch_Contr (in  Word16 x[L_SUBFR],
		      in  Word16 v[L_SUBFR],
		      in  Word16 gain_pit,
		      out Word16 y[L_SUBFR])
{
  void main(void) 
  {
    Int i;
    Word32 L_temp;
    Word16 temp_gain_pit;
    
    temp_gain_pit = gain_pit;

    for (i = 0; i < L_SUBFR; i++) 
	{
	 L_temp = L_mult (v[i], temp_gain_pit);
         L_temp = L_shl (L_temp, 3);
         y[i] = sub (x[i], extract_h (L_temp));
	}
  }
};
