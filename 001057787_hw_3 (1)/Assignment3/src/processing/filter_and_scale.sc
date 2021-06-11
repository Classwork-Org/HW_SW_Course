/*************************************************************************
 *
 *  BEHAVIOR:  filter_and_scale
 *
 *  PURPOSE: Preprocessing of input speech.
 *
 *  DESCRIPTION:
 *     - 2nd order high pass filtering with cut off frequency at 80 Hz.
 *     - Divide input by two.
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";




import "reset";




behavior Filter_And_Scale (
		      in Flag reset_flag,
#ifndef USE_BIT_PORTS
		      in Word16 input_frame[L_FRAME],
#else
		      in bit[SAMPLE_WIDTH-1:0] input_frame[L_FRAME],
#endif
		      out Word16 sig_out[L_FRAME]
		      ) 
  implements Ireset

{
  /* Values to be preserved between calls      */
  /* y[] values are kept in double precision   */
  Word16 y2_hi, y2_lo, y1_hi, y1_lo, x0, x1;
  

  void init(void)
    {
      y2_hi = 0;
      y2_lo = 0;
      y1_hi = 0;
      y1_lo = 0;
      x0 = 0;
      x1 = 0;
    }
  
  void reset(void)
  {
    init();
  }
  

  /*------------------------------------------------------------------------*
   *                                                                        *
   * Algorithm:                                                             *
   *                                                                        *
   *  y[i] = b[0]*x[i]/2 + b[1]*x[i-1]/2 + b[2]*x[i-2]/2                    *
   *                     + a[1]*y[i-1]   + a[2]*y[i-2];                     *
   *                                                                        *
   *                                                                        *
   *  Input is divided by two in the filtering process.                     *
   *------------------------------------------------------------------------*/
  
  void main(void)
    {
      Int i;
      Word16 x2;
      Word32 L_tmp;

      /* filter coefficients (fc = 80 Hz, coeff. b[] is divided by 2) */
      const Word16 b[3] = {1899, -3798, 1899};
      const Word16 a[3] = {4096, 7807, -3733};
   
      if (reset_flag) init();

      
      for (i = 0; i < L_FRAME; i++)
	{
	  x2 = x1;    
	  x1 = x0;          
#ifndef USE_BIT_PORTS
	  x0 = (input_frame[i] << 3);
#else
	  x0 = (input_frame[i] @ 000B);   
#endif


	  /*  y[i] = b[0]*x[i]/2 + b[1]*x[i-1]/2 + b140[2]*x[i-2]/2  */
	  /*                     + a[1]*y[i-1] + a[2] * y[i-2];      */

	  L_tmp = Mpy_32_16 (y1_hi, y1_lo, a[1]);
	  L_tmp = L_add (L_tmp, Mpy_32_16 (y2_hi, y2_lo, a[2]));
	  L_tmp = L_mac (L_tmp, x0, b[0]);
	  L_tmp = L_mac (L_tmp, x1, b[1]);
	  L_tmp = L_mac (L_tmp, x2, b[2]);
	  L_tmp = L_shl (L_tmp, 3);
	  sig_out[i] = round (L_tmp);

	  y2_hi = y1_hi;  
	  y2_lo = y1_lo;  
	  L_Extract (L_tmp, &y1_hi, &y1_lo);
	}
    }

};
