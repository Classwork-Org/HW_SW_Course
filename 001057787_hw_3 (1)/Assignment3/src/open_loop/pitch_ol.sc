/*************************************************************************
 *
 *  FUNCTION:  Pitch_ol
 *
 *  PURPOSE: Compute the open loop pitch lag.
 *
 *  DESCRIPTION:
 *      The open-loop pitch lag is determined based on the perceptually
 *      weighted speech signal. This is done in the following steps:
 *        - find three maxima of the correlation <sw[n],sw[n-T]> in the
 *          follwing three ranges of T : [18,35], [36,71], and [72, 143]
 *        - divide each maximum by <sw[n-t], sw[n-t]> where t is the delay at
 *          that maximum correlation.
 *        - select the delay of maximum normalized correlation (among the
 *          three candidates) while favoring the lower delay ranges.
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

#include "min_max.sh"

import "basic_op";
import "basic_func";



#define THRESHOLD 27853



behavior Pitch_Ol (
  in  Word16 *sig,      /* input: signal used to compute the open loop pitch
                             signal[-pit_max] to signal[-1] should be known */
  out Word16 p_max1     /* output: open loop pitch lag                      */
)
{


  /*************************************************************************
 *
 *  FUNCTION:  Lag_max
 *
 *  PURPOSE: Find the lag that has maximum correlation of scal_sig[] in a
 *           given delay range.
 *
 *  DESCRIPTION:
 *      The correlation is given by
 *           cor[t] = <scal_sig[n],scal_sig[n-t]>,  t=lag_min,...,lag_max
 *      The functions outputs the maximum correlation after normalization
 *      and the corresponding lag.
 *
 *************************************************************************/

  Word16 Lag_max ( /* output: lag found                               */
    Word16 scal_sig[],  /* input : scaled signal.                          */
    Word16 scal_fac,    /* input : scaled signal factor.                   */
    Word16 L_frame,     /* input : length of frame to compute pitch        */
    Word16 lag_max,     /* input : maximum lag                             */
    Word16 lag_min,     /* input : minimum lag                             */
    Word16 *cor_max)    /* output: normalized correlation of selected lag  */
    {
      Int i, j;
      Word16 *p, *p1;
      Word32 max, t0;
      Word16 max_h, max_l, ener_h, ener_l;
      Word16 p_max;

      max = MIN_32;                

      for (i = lag_max; i >= lag_min; i--)
	{
	  p = scal_sig;            
	  p1 = &scal_sig[-i];      
	  t0 = 0;                  

	  for (j = 0; j < L_frame; j++, p++, p1++)
	    {
	      t0 = L_mac (t0, *p, *p1);
	    }
	   
	  if (L_sub (t0, max) >= 0)
	    {
	      max = t0;            
	      p_max = i;           
	    }
	}

      /* compute energy */

      t0 = 0;                      
      p = &scal_sig[-p_max];       
      for (i = 0; i < L_frame; i++, p++)
	{
	  t0 = L_mac (t0, *p, *p);
	}
      /* 1/sqrt(energy) */

      t0 = Inv_sqrt (t0);
      t0 = L_shl (t0, 1);

      /* max = max/sqrt(energy)  */

      L_Extract (max, &max_h, &max_l);
      L_Extract (t0, &ener_h, &ener_l);

      t0 = Mpy_32 (max_h, max_l, ener_h, ener_l);
      t0 = L_shr (t0, scal_fac);

      *cor_max = extract_h (L_shl (t0, 15));       /* divide by 2 */

      return (p_max);
    }



  void main(void)
    {
      Int i, j;
      Word16 max1, max2, max3;
      Word16 p_max2, p_max3;
      Word32 t0;
      
      /* Scaled signal                                                */
      /* Can be allocated with memory allocation of(PIT_MAX+L_FRAME_BY2)  */
      
      Word16 scaled_signal[512];
      Word16 *scal_sig, scal_fac;
      
      scal_sig = &scaled_signal[PIT_MAX];  
      
      t0 = 0L;                             
      for (i = -PIT_MAX; i < L_FRAME_BY2; i++)
	{
	  t0 = L_mac (t0, sig[i], sig[i]);
	}
      /*--------------------------------------------------------*
       * Scaling of input signal.                               *
       *                                                        *
       *   if Overflow        -> scal_sig[i] = signal[i]>>2     *
       *   else if t0 < 1^22  -> scal_sig[i] = signal[i]<<2     *
       *   else               -> scal_sig[i] = signal[i]        *
       *--------------------------------------------------------*/
      
      /*--------------------------------------------------------*
       *  Verification for risk of overflow.                    *
       *--------------------------------------------------------*/

        
      if (L_sub (t0, MAX_32) == 0L)               /* Test for overflow */
	{
	  for (i = -PIT_MAX; i < L_FRAME_BY2; i++)
	    {
	      scal_sig[i] = shr (sig[i], 3);    
	    }
	  scal_fac = 3;                            
	}
      else if (L_sub (t0, (Word32) 1048576L) < (Word32) 0)
        /* if (t0 < 2^20) */
	{
	  for (i = -PIT_MAX; i < L_FRAME_BY2; i++)
	    {
	      scal_sig[i] = shl (sig[i], 3);    
	    }
	  scal_fac = -3;                           
	}
      else
	{
	  for (i = -PIT_MAX; i < L_FRAME_BY2; i++)
	    {
	      scal_sig[i] = sig[i];             
	    }
	  scal_fac = 0;                            
	}

      /*--------------------------------------------------------------------*
       *  The pitch lag search is divided in three sections.                *
       *  Each section cannot have a pitch multiple.                        *
       *  We find a maximum for each section.                               *
       *  We compare the maximum of each section by favoring small lags.    *
       *                                                                    *
       *  First section:  lag delay = PIT_MAX     downto 4*PIT_MIN          *
       *  Second section: lag delay = 4*PIT_MIN-1 downto 2*PIT_MIN          *
       *  Third section:  lag delay = 2*PIT_MIN-1 downto PIT_MIN            *
       *-------------------------------------------------------------------*/
    
      j = shl (PIT_MIN, 2);
      p_max1 = Lag_max (scal_sig, scal_fac, L_FRAME_BY2, PIT_MAX, j, &max1);

      i = sub (j, 1);
      j = shl (PIT_MIN, 1);
      p_max2 = Lag_max (scal_sig, scal_fac, L_FRAME_BY2, i, j, &max2);

      i = sub (j, 1);
      p_max3 = Lag_max (scal_sig, scal_fac, L_FRAME_BY2, i, PIT_MIN, &max3);

      /*--------------------------------------------------------------------*
       * Compare the 3 sections maximum, and favor small lag.               *
       *-------------------------------------------------------------------*/
    
       
      if (sub (mult (max1, THRESHOLD), max2) < 0)
	{
	  max1 = max2;                        
	  p_max1 = p_max2;                    
	}
       
      if (sub (mult (max1, THRESHOLD), max3) < 0)
	{
	  p_max1 = p_max3;                    
	}
    }
};

