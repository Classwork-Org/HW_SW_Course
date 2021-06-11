// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: set_sign.sc
// ------------------------------------------------------------------------
// 
// Calculate the sign information:
//   sign[n] = sign(en[n])
// with e[] being the sum of normalized residual and correlations:
//   en[n] = (res2[n] / k_res2) + (dn[n] / k_dn)
// where
//   k_XX = \sqrt{ \sum_{i=0}^{39}{ XX[n] } }
//
// The sign information is then included into the correlation dn[]:
//   d[n] = d[n] sign[n]
//
// Also, the position pos_max[] with maximum correlation in each track
// is computed and the starting positions ipos[] for the search are set.
//
//
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 01/08/02  <gerstl>	Updated to comply with extended port checking
// 07/21/98  <gerstl>
// 08/08/98  <szhao>

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";
import "basic_func";




behavior Set_Sign (in    Word16 dn[L_SUBFR],
		   out   Word16 dn2[L_SUBFR],
		   in    Word16 cn[L_SUBFR],
		   out   Word16 sign[L_SUBFR],
		   out   Word16 pos_max[5],
		   out   Word16 ipos[M])
{
  void main(void) 
  {
    Int i, j;
    Word16 val, cor, k_cn, k_dn, max, max_of_all, pos, ipos0;
    Word16 en[L_CODE];                  /* correlation vector */
    Word32 s;

    /* calculate energy for normalization of cn[] and dn[] */

    s = 256;  
    for (i = 0; i < L_CODE; i++)
    {
        s = L_mac (s, cn[i], cn[i]);
    }
    s = Inv_sqrt (s);
    k_cn = extract_h (L_shl (s, 5));

    s = 256;
    for (i = 0; i < L_CODE; i++)
    {
        s = L_mac (s, dn[i], dn[i]);
    }
    s = Inv_sqrt (s);
    k_dn = extract_h (L_shl (s, 5));

    for (i = 0; i < L_CODE; i++)
    {
        val = dn[i];
        cor = round (L_shl (L_mac (L_mult (k_cn, cn[i]), k_dn, val), 10));


        if (cor >= 0)
        {
            sign[i] = 32767;                      /* sign = +1 */
        }
        else
        {
            sign[i] = -32767;                     /* sign = -1 */
            cor = negate (cor);
            val = negate (val);
        }
        /* modify dn[] according to the fixed sign */
        dn2[i] = val;
        en[i] = cor;
    }

    max_of_all = -1;
    for (i = 0; i < NB_TRACK; i++)
    {
        max = -1;

        for (j = i; j < L_CODE; j += STEP)
        {
            cor = en[j];
            val = sub (cor, max);

            if (val > 0)
            {
                max = cor;
                pos = j;
            }
        }
        /* store maximum correlation position */
        pos_max[i] = pos;
        val = sub (max, max_of_all);

        if (val > 0)
        {
            max_of_all = max;
            /* starting position for i0 */
            ipos0 = i;
        }
    }

    /*----------------------------------------------------------------*
     *     Set starting position of each pulse.                       *
     *----------------------------------------------------------------*/

    pos = ipos0;
    ipos[0] = ipos0;
    ipos[5] = ipos0;

    for (i = 1; i < NB_TRACK; i++)
    {
        pos = add (pos, 1);
        if (sub (pos, NB_TRACK) >= 0)
        {
            pos = 0;
        }
        ipos[i] = pos;
        ipos[i + 5] = pos;
    }

  }
};
