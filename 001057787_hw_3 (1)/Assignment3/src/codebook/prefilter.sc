// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: prefilter.sc
// ------------------------------------------------------------------------
// 
// Include the pitch prefilter contribution into a signal.
//
//
// 09/13/02  <gerstl>	Merged in fixes for input of scrtl tool
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 07/21/98  <gerstl>
// 08/07/98  <szhao>

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";



behavior Prefilter (in    Word16 T0,
		    in    Word16 gain_pit,
		    in    Word16 x_in[L_SUBFR],
                    out   Word16 x_out[L_SUBFR])
{
  void main(void) 
  {
    Int i;
    Word16 temp;
    Word16 pit_sharp;
    Word16 x[L_SUBFR];    
    Word16 temp_T0;
    Word16 temp_gain_pit;

    temp_T0 = T0;
    temp_gain_pit = gain_pit;

    //x = x_in;
    for (i = 0; i < L_SUBFR; i++)
	{
		x[i]=x_in[i]; 
	}

    pit_sharp = shl(temp_gain_pit, 3);       // from codebook.sc (R.D.)
    
    for (i = T0; i < L_SUBFR; i++)
    {
	temp = mult (x[i - T0], pit_sharp);
	x[i] = add (x[i], temp);
    }
    
    x_out = x;
  }
};
