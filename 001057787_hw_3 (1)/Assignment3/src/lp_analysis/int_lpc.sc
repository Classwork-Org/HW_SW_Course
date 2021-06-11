/*************************************************************************
 *
 *  FUNCTION:  Int_lpc()
 *
 *  PURPOSE:  Interpolates the LSPs and converts to LPC parameters to get
 *            a different LP filter in each subframe.
 *
 *  DESCRIPTION:
 *     The 20 ms speech frame is divided into 4 subframes.
 *     The LSPs are quantized and transmitted at the 2nd and 4th subframes
 *     (twice per frame) and interpolated at the 1st and 3rd subframe.
 *
 *          |------|------|------|------|
 *             sf1    sf2    sf3    sf4
 *       F0            Fm            F1
 *
 *     sf1:   1/2 Fm + 1/2 F0         sf3:   1/2 F1 + 1/2 Fm
 *     sf2:       Fm                  sf4:       F1
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";
import "lsp_az";



behavior Int_Lpc (
    in Word16 lsp_old[M],   /* input : LSP vector at the 4th subframe
                                       of past frame    */
    in Word16 lsp_mid[M],   /* input : LSP vector at the 2nd subframe
                                       of present frame */
    in Word16 lsp_new[M],   /* input : LSP vector at the 4th subframe of
                                       present frame */
    out Word16 A[4][MP1]    /* output: interpolated LP parameters in
                                       all subframes */
)
{

  void main(void)
    {
      Int i;
      Word16 lsp[M];

      /*  lsp[i] = lsp_mid[i] * 0.5 + lsp_old[i] * 0.5 */

      for (i = 0; i < M; i++)
	{
	  lsp[i] = add (shr (lsp_mid[i], 1), shr (lsp_old[i], 1));
	   
	}

      Lsp_Az (lsp, (Word16*)(A[0]));           /* Subframe 1 */
      Lsp_Az (lsp_mid, (Word16*)(A[1]));       /* Subframe 2 */

      for (i = 0; i < M; i++)
	{
	  lsp[i] = add (shr (lsp_mid[i], 1), shr (lsp_new[i], 1));
	   
	}

      Lsp_Az (lsp, (Word16*)(A[2]));           /* Subframe 3 */
      Lsp_Az (lsp_new, (Word16*)(A[3]));       /* Subframe 4 */
    }
};



/*----------------------------------------------------------------------*
 * Function Int_lpc2()                                                  *
 * ~~~~~~~~~~~~~~~~~~                                                   *
 * Interpolation of the LPC parameters.                                 *
 * Same as the previous function but we do not recompute Az() for       *
 * subframe 2 and 4 because it is already available.                    *
 *----------------------------------------------------------------------*/

behavior Int_Lpc2 (
             in Word16 lsp_old[M],  /* input : LSP vector at the 4th subframe
                                               of past frame    */
             in Word16 lsp_mid[M],  /* input : LSP vector at the 2nd subframe
                                               of present frame */
             in Word16 lsp_new[M],  /* input : LSP vector at the 4th subframe
                                               of present frame */
             out Word16 A[4][MP1]   /* output: interpolated LP parameters
                                               in subframes 1 and 3 */
)
{
  void main(void)
    {
      Int i;
      Word16 lsp[M];

      /*  lsp[i] = lsp_mid[i] * 0.5 + lsp_old[i] * 0.5 */

      for (i = 0; i < M; i++)
	{
	  lsp[i] = add (shr (lsp_mid[i], 1), shr (lsp_old[i], 1));
	   
	}

      Lsp_Az (lsp, (Word16*)(A[0]));           /* Subframe 1 */

      for (i = 0; i < M; i++)
	{
	  lsp[i] = add (shr (lsp_mid[i], 1), shr (lsp_new[i], 1));
	   
	}
      Lsp_Az (lsp, (Word16*)(A[2]));           /* Subframe 3 */

    }
};
