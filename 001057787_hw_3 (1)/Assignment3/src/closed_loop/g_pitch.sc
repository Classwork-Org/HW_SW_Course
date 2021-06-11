/*************************************************************************
 *
 *  FUNCTION:  G_pitch
 *
 *  PURPOSE:  Compute the pitch (adaptive codebook) gain. Result in Q12
 *
 *  DESCRIPTION:
 *      The adaptive codebook gain is given by
 *
 *              g = <x[], y[]> / <y[], y[]>
 *
 *      where x[] is the target vector, y[] is the filtered adaptive
 *      codevector, and <> denotes dot product.
 *      The gain is limited to the range [0,1.2]
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";

#include "min_max.sh"




behavior G_Pitch ( 
  in Word16 xn[L_SUBFR],  /* (i)   : Pitch target.                           */
  in Word16 y1[L_SUBFR],  /* (i)   : Filtered adaptive codebook.             */
  out Word16 gain         /* (o)   : Gain of pitch lag saturated to 1.2      */
  )
{
  void main(void)
    {
      Int i;
      Word16 g;
      Word16 xy, yy, exp_xy, exp_yy;
      Word32 s;

      Word16 scaled_y1[80];   /* Usually dynamic allocation of (L_SUBFR) !!*/

      /* divide by 2 "y1[]" to avoid overflow */

      for (i = 0; i < L_SUBFR; i++)
	{
	  scaled_y1[i] = shr (y1[i], 2);  
	}

      /* Compute scalar product <y1[],y1[]> */

      s = 0L;                             /* Avoid case of all zeros */
      for (i = 0; i < L_SUBFR; i++)
	{
	  s = L_mac (s, y1[i], y1[i]);
	}
       
      if (L_sub (s, MAX_32) != 0L)       /* Test for overflow */
	{
	  s = L_add (s, 1L);             /* Avoid case of all zeros */
	  exp_yy = norm_l (s);
	  yy = round (L_shl (s, exp_yy));
	}
      else
	{
	  s = 1L;                         /* Avoid case of all zeros */
	  for (i = 0; i < L_SUBFR; i++)
	    {
	      s = L_mac (s, scaled_y1[i], scaled_y1[i]);
	    }
	  exp_yy = norm_l (s);
	  yy = round (L_shl (s, exp_yy));
	  exp_yy = sub (exp_yy, 4);
	}

      /* Compute scalar product <xn[],y1[]> */

      // Overflow = 0;
      clear_overflow();

      s = 1L;                             /* Avoid case of all zeros */
      for (i = 0; i < L_SUBFR; i++)
	{
	  // Carry = 0;
	  clear_carry();

	  s = L_macNs (s, xn[i], y1[i]);

	  
	  // if (Overflow != 0)
	  if (overflow())
	    {
	      break;
	    }
	}
       

      // if (Overflow == 0)
      if (!overflow())
	{
	  exp_xy = norm_l (s);
	  xy = round (L_shl (s, exp_xy));
	}
      else
	{
	  s = 1L;                         /* Avoid case of all zeros */
	  for (i = 0; i < L_SUBFR; i++)
	    {
	      s = L_mac (s, xn[i], scaled_y1[i]);
	    }
	  exp_xy = norm_l (s);
	  xy = round (L_shl (s, exp_xy));
	  exp_xy = sub (exp_xy, 2);
	}

      /* If (xy < 4) gain = 0 */

      i = sub (xy, 4);

       
      if (i < 0)
	{
	  //return ((Word16) 0);
	  gain = 0;
	  return;
	}

      /* compute gain = xy/yy */

      xy = shr (xy, 1);                  /* Be sure xy < yy */
      g = div_s (xy, yy);

      i = add (exp_xy, 3 - 1);           /* Denormalization of division */
      i = sub (i, exp_yy);

      g = shr (g, i);

      /* if(gain >1.2) gain = 1.2 */

       
      if (sub (g, 4915) > 0)
	{
	  g = 4915;                    
	}
      
      gain = g;
    }
};
