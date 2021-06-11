/*************************************************************************
 *
 *  FUNCTION:   Convolve
 *
 *  PURPOSE:
 *     Perform the convolution between two vectors x[] and h[] and
 *     write the result in the vector y[]. All vectors are of length L
 *     and only the first L samples of the convolution are computed.
 *
 *  DESCRIPTION:
 *     The convolution is given by
 *
 *          y[n] = sum_{i=0}^{n} x[i] h[n-i],        n=0,...,L-1
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_func";



behavior Convolve (
   in Word16 *x,          /* (i): input vector                           */
   in Word16 *h,          /* (i): impulse response                       */
   out Word16 y[L_SUBFR]  /* (o): output vector                          */
   )
{
  void main(void)
    {
      Word16 *p_y;

      p_y = &y[0];
      Convolut(x, h, p_y, L_SUBFR);
    }
};
