//
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "typedef.sh"
#include "cnst.sh"

import "autocorr";
import "lag_wind";
import "levinson";

behavior Find_Az (
  in  Word16 *x,             /* (i): input signal                            */
  in  Word16 wind[L_WINDOW], /* (i): window for LPC analysis                 */
      Word16 r_h[MP1],       /* (o): Autocorrelations  (msb)                 */
      Word16 r_l[MP1],       /* (o): Autocorrelations  (lsb)                 */
      Word16 old_A[MP1],     /* Last A(z) for case of unstable filter        */
  out Word16 scal_fac,       /* (o): scaling factor for the autocorrelations */
  out Word16 A_t[MP1],       /* (o): A[M]  LPC coefficients  (M = 10)        */
  out Word16 rc[4]           /* (o): rc[4]   First 4 reflection coefficients */
)
{
  Autocorr autocorr(x, wind, r_h, r_l, scal_fac);
  Lag_Window lag_window(r_h, r_l);
  Levinson levinson(r_h, r_l, old_A, A_t, rc);

  void main(void)
    {
      autocorr.main();
      lag_window.main();
      levinson.main();
    }
};
