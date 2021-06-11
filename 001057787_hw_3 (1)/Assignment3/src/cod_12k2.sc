/***************************************************************************
 *   FUNCTION:   Coder_12k2
 *
 *   PURPOSE:  Principle encoder routine.
 *
 *   DESCRIPTION: This function is called every 20 ms speech frame,
 *       operating on the newly read 160 speech samples. It performs the
 *       principle encoding functions to produce the set of encoded parameters
 *       which include the LSP, adaptive codebook, and fixed codebook
 *       quantization indices (addresses and gains).
 *
 *   INPUTS:
 *       No input arguments are passed to this function. However, before
 *       calling this function, 160 new speech data samples should be copied to
 *       the vector new_speech[]. This is a global pointer which is declared in
 *       this file (it points to the end of speech buffer minus 160).
 *
 *   OUTPUTS:
 *
 *       ana[]:     vector of analysis parameters.
 *       synth[]:   Local synthesis speech (for debugging purposes)
 *
 ***************************************************************************/


#include "cnst.sh"
#include "typedef.sh"


import "reset";

import "array_op";

import "lp_analysis";
import "open_loop";
import "subframes";
import "shift_signals";



behavior Coder_12k2_Seq1(
			 in  Word16 speech_proc[L_FRAME],
			     Word16 old_speech[L_TOTAL],
			     Word16 *speech,
			 out Word16 *p_window,
			     Word16 old_wsp[L_FRAME + PIT_MAX],
			 out Word16 *wsp,
			     Word16 old_exc[L_FRAME + PIT_MAX + L_INTERPOL],
			 out Word16 *exc,
			 out Flag   ptch,
			 out DTXctrl txdtx_ctrl,
			 in  Flag   reset_flag
			 )
  implements Ireset
{
  void init(void)
    {
      /*--------------------------------------------------------------------------*
       *          Initialize pointers to speech vector.                           *
       *--------------------------------------------------------------------------*/

      speech = old_speech + L_TOTAL - L_FRAME;    /* New speech     */
      p_window = old_speech + L_TOTAL - L_WINDOW; /* For LPC window */

      /* Initialize pointers */

      wsp = old_wsp + PIT_MAX;
      exc = old_exc + PIT_MAX + L_INTERPOL;

      /* vectors to zero */

      Set_zero (old_speech, L_TOTAL);
      Set_zero (old_exc, PIT_MAX + L_INTERPOL);
      Set_zero (old_wsp, PIT_MAX);

      txdtx_ctrl = TX_SP_FLAG | TX_VAD_FLAG;
      ptch = 1;
    }
  
  
  void reset(void)
  {
    init();
  }
  
  
  void main(void)
  {
    if (reset_flag == true)    /* last frame was a homing frame */
    {
      init();
    }      

    Copy(speech_proc, speech, L_FRAME);    
  }
};




behavior Coder_12k2 (
     in  Word16 speech_proc[L_FRAME],  /* input   : preprocessed speech */
     out Word16 ana[PRM_SIZE],         /* output  : Analysis parameters */
     out Word16 synth[L_FRAME],        /* output  : Local synthesis     */
     in  Flag   dtx_mode,
     out DTXctrl txdtx_ctrl,
     in  Flag   reset_flag
     )
  implements Ireset

{
  /*--------------------------------------------------------*
   *         Static memory allocation.                      *
   *--------------------------------------------------------*/

  /* Speech vector */
  Word16 old_speech[L_TOTAL];
  Word16 *speech;
  Word16 *p_window;
  
  /* Weight speech vector */
  Word16 old_wsp[L_FRAME + PIT_MAX];
  Word16 *wsp;
  
  /* Excitation vector */
  Word16 old_exc[L_FRAME + PIT_MAX + L_INTERPOL];
  Word16 *exc;
      
  /* unquantified LP filter coefficients for the 4 subframes */
  Word16 A_t[4][MP1];
  /*   quantified LP filter coefficients for the 4 subframes */
  Word16 Aq_t[4][MP1];         

  Word16 T0_min_1, T0_max_1;
  Word16 T0_min_2, T0_max_2;

  Flag ptch;                /* flag to indicate a periodic signal component */

  DTXctrl txdtx_ctrl_cur;
  Word32 L_pn_seed_tx;

  
  Coder_12k2_Seq1 seq1(speech_proc, old_speech, speech, p_window, old_wsp, 
		       wsp, old_exc, exc, ptch, txdtx_ctrl_cur, reset_flag);
  
  LP_Analysis lp_analysis(p_window, A_t, Aq_t, ana, ptch, txdtx_ctrl_cur,
			  dtx_mode, L_pn_seed_tx, reset_flag);
  Open_Loop open_loop(speech, A_t, wsp, T0_min_1, T0_max_1, T0_min_2, 
		      T0_max_2, ptch, txdtx_ctrl_cur, dtx_mode, reset_flag);

  Subframes subframes(A_t, Aq_t, speech, exc, T0_min_1, T0_max_1, 
		      T0_min_2, T0_max_2, ana, synth, txdtx_ctrl_cur,
		      L_pn_seed_tx, reset_flag);

  Shift_Signals shift_signals(old_speech, old_wsp, old_exc,
 			      txdtx_ctrl_cur, txdtx_ctrl);



  void reset(void)
    {
      seq1.reset();
      lp_analysis.reset();
      open_loop.reset();
      subframes.reset(); 
    }


  void main(void)
    {

      seq1.main();


     /*----------------------------------------------------------------------*
      *  - Perform LPC analysis: (twice per frame)                           *
      *       * autocorrelation + lag windowing                              *
      *       * Levinson-Durbin algorithm to find a[]                        *
      *       * convert a[] to lsp[]                                         *
      *       * quantize and code the LSPs                                   *
      *       * find the interpolated LSPs and convert to a[] for all        *
      *         subframes (both quantized and unquantized)                   *
      *----------------------------------------------------------------------*/
  
      lp_analysis.main();




     /*----------------------------------------------------------------------*
      * - Find the weighted input speech wsp[] for the whole speech frame    *
      * - Find the open-loop pitch delay for first 2 subframes               *
      * - Set the range for searching closed-loop pitch in 1st subframe      *
      * - Find the open-loop pitch delay for last 2 subframes                *
      *----------------------------------------------------------------------*/
  
      open_loop.main();




     /*----------------------------------------------------------------------*
      *          Loop for every subframe in the analysis frame               *
      *----------------------------------------------------------------------*
      *  To find the pitch and innovation parameters. The subframe size is   *
      *  L_SUBFR and the loop is repeated L_FRAME/L_SUBFR times.             *
      *     - find the weighted LPC coefficients                             *
      *     - find the LPC residual signal res[]                             *
      *     - compute the target signal for pitch search                     *
      *     - compute impulse response of weighted synthesis filter (h1[])   *
      *     - find the closed-loop pitch parameters                          *
      *     - encode the pitch delay                                         *
      *     - update the impulse response h1[] by including pitch            *
      *     - find target vector for codebook search                         *
      *     - codebook search                                                *
      *     - encode codebook address                                        *
      *     - VQ of pitch and codebook gains                                 *
      *     - find synthesis speech                                          *
      *     - update states of weighting filter                              *
      *----------------------------------------------------------------------*/

      subframes.main();




      /*--------------------------------------------------*
       * Update signal for next frame.                    *
       * -> shift to the left by L_FRAME:                 *
       *     speech[], wsp[] and  exc[]                   *
       *--------------------------------------------------*/

      shift_signals.main();


    }
};
