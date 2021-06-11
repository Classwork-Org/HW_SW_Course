// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: g_code.sc
// ------------------------------------------------------------------------
// 
// Compute fixed (algebraic) codebook gain.
//
//
// 07/21/98  A.Gerstlauer


#include "typedef.sh"
#include "cnst.sh"

import "basic_op";



behavior Gain_Code (in  Word16 xn2[L_SUBFR],
		    in  Word16 y2[L_SUBFR],
		    out Word16 gain)
{
  void main(void) 
  {
    Int i;
    Word16 g;
    Word16 j;
    Word16 xy, yy, exp_xy, exp_yy;
    Word16 scal_y2[L_SUBFR];
    Word32 s;

    /* Scale down Y[] by 2 to avoid overflow */

    for (i = 0; i < L_SUBFR; i++)
    {
        scal_y2[i] = shr (y2[i], 1);
    }

    /* Compute scalar product <X[],Y[]> */

    s = 1L; /* Avoid case of all zeros */
    for (i = 0; i < L_SUBFR; i++)
    {
        s = L_mac (s, xn2[i], scal_y2[i]);
    }
    exp_xy = norm_l (s);
    xy = extract_h (L_shl (s, exp_xy));

    /* If (xy < 0) gain = 0  */

    if (xy <= 0) {
      gain = ((Word16) 0); 
      return;
    }

    /* Compute scalar product <Y[],Y[]> */

    s = 0L;                      
    for (i = 0; i < L_SUBFR; i++)
    {
        s = L_mac (s, scal_y2[i], scal_y2[i]);
    }
    exp_yy = norm_l (s);
    yy = extract_h (L_shl (s, exp_yy));

    /* compute gain = xy/yy */

    xy = shr (xy, 1);                 /* Be sure xy < yy */
    g = div_s (xy, yy);

    /* Denormalization of division */
    j = add (exp_xy, 5);              /* 15-1+9-18 = 5 */
    j = sub (j, exp_yy);

    gain = shr (g, j);

  }
};
