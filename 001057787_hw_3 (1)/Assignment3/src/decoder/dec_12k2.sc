
#include "cnst.sh"
#include "typedef.sh"

import "basic_op";

import "rx_dtx";
import "decode_lsp";
import "decoder_subframe";
import "post_filter";


behavior Decoder_12k2_init(in  Word16 bfi,
		           in  Word16 rxdtx_ctrl,
		               Word16 state,
		               Word16 prev_bf)
{    
  void main(void)
  {

    /* Set state machine */

    if (bfi != 0)
    {
        state = add (state, 1);
    }
    else if (sub (state, 6) == 0)
    {
        state = 5;
    }
    else
    {
        state = 0;
    }

    if (sub (state, 6) > 0)
    {
        state = 6;
    }

    /* If this frame is the first speech frame after CNI period,     */
    /* set the BFH state machine to an appropriate state depending   */
    /* on whether there was DTX muting before start of speech or not */
    /* If there was DTX muting, the first speech frame is muted.     */
    /* If there was no DTX muting, the first speech frame is not     */
    /* muted. The BFH state machine starts from state 5, however, to */
    /* keep the audible noise resulting from a SID frame which is    */
    /* erroneously interpreted as a good speech frame as small as    */
    /* possible (the decoder output in this case is quickly muted)   */
    if ((rxdtx_ctrl & RX_FIRST_SP_FLAG) != 0)
    {
        if ((rxdtx_ctrl & RX_PREV_DTX_MUTING) != 0)
        {
            state = 5;    
            prev_bf = 1;  
        }
        else
        {
            state = 5;    
            prev_bf = 0;  
        }
    }
  }
};


behavior Decoder_12k2(in  Word16 TAF,
		      in  Word16 SID_flag,
		      in  Word16 prm[PRM_SIZE+1],
		      out Word16 speech[L_FRAME],
		      in  Flag   reset_flag)
  implements Ireset
{
  Word16 state;
  Word16 bfi;
  Word16 prev_bf;
  Word16 *parm;
  Word16 i_subfr;
  
  Word16 A_t[4][MP1];
  Word16 Az[MP1];

  Word16 old_exc[L_FRAME + PIT_MAX + L_INTERPOL];
  Word16 *exc;
  
  Word16 *p_syn_i;
  Word16 synth[L_SUBFR];
  Word16 syn_buf[L_FRAME + M];
  Word16 synth_pst[L_SUBFR];
  
  Word16 rxdtx_ctrl;
  Word16 rx_dtx_state;
  Word32 L_pn_seed_rx;
  
  Rx_Dtx rx_dtx(TAF, bfi, SID_flag, rxdtx_ctrl, rx_dtx_state, L_pn_seed_rx);
  Decoder_12k2_init dec_init(bfi, rxdtx_ctrl, state, prev_bf);
  
  Decode_Lsp decode_lsp(parm, bfi, rxdtx_ctrl, rx_dtx_state, A_t);
  
  Decoder_Subframe subframe(state, i_subfr, bfi, prev_bf, Az, parm, exc, 
			    synth, rxdtx_ctrl, rx_dtx_state, L_pn_seed_rx);

  Post_Filter post_filter(p_syn_i, synth_pst, Az);

  
  void init(void)
  {
    int i;
    
    for (i = 0; i < M; i++)
    {
        syn_buf[i] = 0;
    }
    
    /* Initialize static pointer */

    exc = old_exc + PIT_MAX + L_INTERPOL;

    /* Static vectors to zero */

    Set_zero (old_exc, PIT_MAX + L_INTERPOL);
    
    /* Initialize memories of bad frame handling */

    prev_bf = 0;
    state = 0;

    rxdtx_ctrl = RX_SP_FLAG;
    L_pn_seed_rx = PN_INITIAL_SEED;
    rx_dtx_state = CN_INT_PERIOD - 1;
  }
  
  void reset(void)
  {
    init();
    rx_dtx.reset();
    decode_lsp.reset();
    subframe.reset();
    post_filter.reset();
  }
  
  
  void main(void)
  {
    int i;
    Word16 *syn;
    Word16 syn_pst[L_FRAME];
    
    if(reset_flag) reset();
    
    syn = syn_buf + M;
    
    /* Test bad frame indicator (bfi) */

    bfi = prm[0];
    parm = &prm[1];

    dec_init.main();
    rx_dtx.main();
    decode_lsp.main();
    
    for (i = 0, i_subfr = 0; i_subfr < L_FRAME; i_subfr += L_SUBFR, i++)
    {
      Az = A_t[i];
      
      subframe.main();
      
      Copy(synth, &syn[i_subfr], L_SUBFR);
      
      p_syn_i = &syn[i_subfr];
      post_filter.main();
      
      Copy(synth_pst, &syn_pst[i_subfr], L_SUBFR);
    }
    
    /* update syn[] buffer */
    
    Copy (&syn[L_FRAME - M], &syn[-M], M);
    
    /* overwrite synthesis speech by postfiltered synthesis speech */

    Copy (syn_pst, &speech[0], L_FRAME);
    
    /*--------------------------------------------------*
     * Update signal for next frame.                    *
     * -> shift to the left by L_FRAME  exc[]           *
     *--------------------------------------------------*/

    Copy (&old_exc[L_FRAME], &old_exc[0], PIT_MAX + L_INTERPOL);
    prev_bf = bfi;
  }  
};
