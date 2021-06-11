/*--------------------------------------------------------------------------*
 * Function q_gain_pitch()                                                  *
 * ~~~~~~~~~~~~~~~~~~~~~~~~                                                 *
 * Scalar quantization of the pitch gain.                                   *
 *--------------------------------------------------------------------------*/


#include "cnst.sh"
#include "typedef.sh"

import "basic_op";



#define NB_QUA_PITCH 16


behavior Q_Gain_Pitch ( 
    inout Word16 gain,      /* Pitch gain to quantize  */
    out Word16 index        /* index of quantization */
)
{
  void main(void)
    {
      Int i;
      Word16 gain_q14, err, err_min;

      /*--------------------------------------------------------------------*
       *  Scalar quantization tables of the pitch gain.                     *
       *--------------------------------------------------------------------*/
      const Word16 qua_gain_pitch[NB_QUA_PITCH] =
      {
	0, 3277, 6556, 8192, 9830, 11469, 12288, 13107,
	13926, 14746, 15565, 16384, 17203, 18022, 18842, 19661
      };

      
      gain_q14 = shl (gain, 2);
      
      err_min = abs_s (sub (gain_q14, qua_gain_pitch[0]));
      index = 0;                                   
      
      for (i = 1; i < NB_QUA_PITCH; i++)
	{
	  err = abs_s (sub (gain_q14, qua_gain_pitch[i]));
	  
	   
	  if (sub (err, err_min) < 0)
	    {
	      err_min = err;                       
	      index = i;                           
	    }
	}
      
      gain = shr (qua_gain_pitch[index], 2);      
    }
};


