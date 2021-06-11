// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: cor_h_x.sc
// ------------------------------------------------------------------------
// 
// Compute correlation between target x[] and impulse response h[]:
//    d[n] = \sum_{i=n}^{39}{ x[i] h[i-n] },  n = 0...39
//
// Also, d[] is normalized such that the sum of 5 maxima of d[] 
// corresponding to each position track does not saturate.
//
//
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 07/21/98  <gerstl>
// 08/08/98  <szhao>


#include "typedef.sh"
#include "cnst.sh"

import "basic_op";




behavior Cor_h_x (in  Word16 h[L_SUBFR],
		  in  Word16 x[L_CODE],
		  out Word16 dn[L_CODE])
{
  void main(void) 
  {
    Int i, k;
    Word16 j;
    Word32 s, y32[L_CODE], max, tot;

    /* first keep the result on 32 bits and find absolute maximum */

    tot = 5;

    for (k = 0; k < NB_TRACK; k++)
    {
        max = 0;
        for (i = k; i < L_CODE; i += STEP)
        {
            s = 0;
            for (j = i; j < L_CODE; j++)
                s = L_mac (s, x[j], h[j - i]);

            y32[i] = s;

            s = L_abs (s);

            if (L_sub (s, max) > (Word32) 0L)
                max = s;
        }
        tot = L_add (tot, L_shr (max, 1));
    }

    j = sub (norm_l (tot), 2);                   /* multiply tot by 4 */

    for (i = 0; i < L_CODE; i++)
    {
        dn[i] = round (L_shl (y32[i], j));
    }

  }
};
