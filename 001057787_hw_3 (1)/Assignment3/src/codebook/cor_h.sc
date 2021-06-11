// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: cor_h.sc
// ------------------------------------------------------------------------
// 
// Compute the matrix of correlations of h[] and include the sign
// information in it
//   rr[i][j] = ( \sum_{n=i}^{39}{ h[n-i] h[n-j] } ) sign[i] sign[j]
//
//
// 09/13/02  <gerstl>	Merged in fixes for input of scrtl tool
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 01/08/02  <gerstl>	Updated to comply with extended port checking
// 07/21/98  <gerstl>
// 08/12/98  <szhao>


#include "typedef.sh"
#include "cnst.sh"

import "basic_op";
import "basic_func";



behavior Cor_h (in  Word16 h[L_SUBFR],
		in  Word16 sign[L_SUBFR],
		out Word16 rr[L_SUBFR * L_SUBFR])
{
  void main(void) 
  {
    Int i, k, l, dec;
    Word16 j, h2[L_CODE], tmp;
    Word32 s;

    /* Scaling for maximum precision */
    s = 2;
    for (i = 0; i < L_CODE; i++)
        s = L_mac (s, h[i], h[i]);

    j = sub (extract_h (s), 32767);

    if (j == 0)
    {
        for (i = 0; i < L_CODE; i++)
        {
            h2[i] = shr (h[i], 1);
        }
    }
    else
    {
        s = L_shr (s, 1);
        k = extract_h (L_shl (Inv_sqrt (s), 7));
        k = mult (k, 32440);                     /* k = 0.99*k */

        for (i = 0; i < L_CODE; i++)
        {
            h2[i] = round (L_shl (L_mult (h[i], k), 9));

        }
    }


    /* build matrix rr[] */
    s = 0;
    i = L_CODE - 1;
    for (k = 0; k < L_CODE; k++, i--)
    {
        s = L_mac (s, h2[k], h2[k]);
        rr[i * L_SUBFR + i] = round (s);
    }

    for (dec = 1; dec < L_CODE; dec++)
    {
        s = 0;
        l = L_CODE - 1;
        i = l - dec;
        for (k = 0; k < (L_CODE - dec); k++, i--, l--)
        {
            s = L_mac (s, h2[k], h2[k + dec]);
            tmp = mult (round (s), mult (sign[i], sign[l]));
            rr[l * L_SUBFR + i] = tmp;
            rr[i * L_SUBFR + l] = tmp;
        }
    }
  }
};
