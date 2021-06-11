
#include "cnst.sh"
#include "typedef.sh"

import "basic_op";
import "basic_func";
import "array_op";

import "reset";

import "syn_filt";


        /*-------------------------------------------------------*
         * - Find the total excitation.                          *
         * - Find synthesis speech corresponding to exc[].       *
         *-------------------------------------------------------*/


behavior Find_exc(    Word16 exc[L_SUBFR],
		      Word16 excp[L_SUBFR],
		  in  Word16 code[L_SUBFR],
		  in  Word16 gain_pit,
		  in  Word16 gain_code,
		  in  Word16 pit_sharp)
{
  void main(void)
  {
    int i;
    Word32 L_temp;
    
        for (i = 0; i < L_SUBFR; i++)
        {
            /* exc[i] = gain_pit*exc[i] + gain_code*code[i]; */

            L_temp = L_mult (exc[i], gain_pit);
            L_temp = L_mac (L_temp, code[i], gain_code);
            L_temp = L_shl (L_temp, 3);

            exc[i] = round (L_temp);
        }
    
        if (sub (pit_sharp, 16384) > 0)
        {
            for (i = 0; i < L_SUBFR; i++)
            {
                excp[i] = add (excp[i], exc[i]);
            }
	}
  }
};


behavior Agc2(in  Word16 sig_in[L_SUBFR],        /* (i)     : postfilter input signal  */
	          Word16 sig_out[L_SUBFR])       /* (i/o)   : postfilter output signal */
{
  void main(void)
  {
    Word16 i, exp;
    Word16 gain_in, gain_out, g0;
    Word32 s;

    Word16 temp;

    /* calculate gain_out with exponent */

    temp = shr (sig_out[0], 2);
    s = L_mult (temp, temp);
    for (i = 1; i < L_SUBFR; i++)
    {
        temp = shr (sig_out[i], 2);
        s = L_mac (s, temp, temp);
    }

    if (s == 0)
    {
        return;
    }
    exp = sub (norm_l (s), 1);
    gain_out = round (L_shl (s, exp));

    /* calculate gain_in with exponent */

    temp = shr (sig_in[0], 2);
    s = L_mult (temp, temp);
    for (i = 1; i < L_SUBFR; i++)
    {
        temp = shr (sig_in[i], 2);
        s = L_mac (s, temp, temp);
    }

    if (s == 0)
    {
        g0 = 0;
    }
    else
    {
        i = norm_l (s);
        gain_in = round (L_shl (s, i));
        exp = sub (exp, i);

        /*---------------------------------------------------*
         *  g0 = sqrt(gain_in/gain_out);                     *
         *---------------------------------------------------*/

        s = L_deposit_l (div_s (gain_out, gain_in));
        s = L_shl (s, 7);       /* s = gain_out / gain_in */
        s = L_shr (s, exp);     /* add exponent */

        s = Inv_sqrt (s);
        g0 = round (L_shl (s, 9));
    }

    /* sig_out(n) = gain(n) sig_out(n) */

    for (i = 0; i < L_SUBFR; i++)
    {
        sig_out[i] = extract_h (L_shl (L_mult (sig_out[i], g0), 3));
    }

    return;
  }
};



behavior Synth_speech(in  Word16 pit_sharp,
		      in  Word16 gain_pit,
		      in  Word16 gain_code,
		      in  Word16 Az[MP1],
		      in  Word16 code[L_SUBFR],
		          Word16 exc[L_SUBFR],
		          Word16 excp[L_SUBFR],
		      out Word16 synth[L_SUBFR])
  implements Ireset
{
  Word16 mem_syn[M];
  
  Find_exc find_exc(exc, excp, code, gain_pit, gain_code, pit_sharp);
  Agc2 agc2(exc, excp);
  Syn_Filt syn_filt1(Az, excp, synth, mem_syn, true);
  Syn_Filt syn_filt2(Az, exc, synth, mem_syn, true);
  
  
  void init(void)
  {
    Set_zero (mem_syn, M);
  }
  
  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    fsm 
    {
      find_exc: {
        if (sub (pit_sharp, 16384) > 0) goto agc2;
      }

      syn_filt2: {
	break;
      }
      
      agc2: {
      }
      
      syn_filt1: {
      }
    }
  }
};
