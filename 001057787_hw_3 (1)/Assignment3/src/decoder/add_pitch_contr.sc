
#include "cnst.sh"
#include "typedef.sh"

import "basic_op";


        /*-------------------------------------------------------*
         * - Add the pitch contribution to code[].               *
         *-------------------------------------------------------*/

behavior Add_pitch_contr(in  Word16 gain_pit,
			 in  Word16 T0,
			 in  Word16 exc[L_SUBFR],
			     Word16 code[L_SUBFR],
			 out Word16 excp[L_SUBFR],
			 out Word16 pit_sharp)
{
  void main(void)
  {
    int i;
    Word16 temp;
    Word32 L_temp;
    Word16 pit_s;
    
        /* pit_sharp = gain_pit;                   */
        /* if (pit_sharp > 1.0) pit_sharp = 1.0;   */

        pit_s = shl (gain_pit, 3);

        /* This loop is not entered when SP_FLAG is 0 since t0 = L_subfr*/
        for (i = T0; i < L_SUBFR; i++)
        {
            temp = mult (code[i - T0], pit_s);
            code[i] = add (code[i], temp);
        }
        /* post processing of excitation elements */

        if (sub (pit_s, 16384) > 0)
        {
            for (i = 0; i < L_SUBFR; i++)
            {
                temp = mult (exc[i], pit_s);
                L_temp = L_mult (temp, gain_pit);
                L_temp = L_shl (L_temp, 1);
                excp[i] = round (L_temp);
            }
        }
    
    pit_sharp = pit_s;
  }
};

