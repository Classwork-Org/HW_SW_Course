#include "cnst.sh"
#include "typedef.sh"

import "reset";


import "par_weight";
import "imp_resp";
import "find_targetvec";

import "pitch_fr6";
import "enc_lag6";
import "pred_lt_6";
import "convolve";
import "g_pitch";
import "q_gain_pitch";



behavior Closed_Loop_Seq1(
                          in  Int    i_subfr,
                          out Word16 h1[L_SUBFR],
                          in  Word16 *p_exc,
                          in  Word16 *p_speech,
                          out Word16 *p_h1,
                          out Word16 *p_exc_i,
                          out Word16 *p_speech_i
                          )
{
  void main(void)
  {
    p_h1 = (Word16*)h1;
    p_exc_i = p_exc + i_subfr;
    p_speech_i = p_speech + i_subfr;     
  }
};


behavior Closed_Loop_Seq2(
			  in  Word16 Aq[MP1],
			  in  Word16 Ap1[MP1],
			  in  Word16 Ap2[MP1],
			  out Word16 h1[L_SUBFR],
			  in  Word16 *p_speech_i,
			  out Word16 res2[L_SUBFR],
			  out Word16 *p_exc_i,
			  out Word16 xn[L_SUBFR],
			  in  Word16 mem_err[M],
			      Word16 mem_w0[M],
			  in  DTXctrl txdtx_ctrl,    /* voice activity flags */
			  out Word16 CN_excitation_gain,
			  in  Flag   reset_flag
			  )
  implements Ireset
{
  Imp_Resp imp_resp(Aq, Ap1, Ap2, h1, txdtx_ctrl, reset_flag);
  Find_Targetvec find_targetvec(Aq, Ap1, Ap2, p_speech_i, res2, p_exc_i,
				xn, mem_err, mem_w0, txdtx_ctrl, 
				CN_excitation_gain);

  void reset(void)
    {	
      imp_resp.reset();
    }
    
  
  void main(void)
  {
      par
	{
	  imp_resp.main();
	  find_targetvec.main();
	}
  }
};


behavior Closed_Loop_Seq3(
                          in  Int i_subfr,
                          out Int pit_flag,
                          in  Word16 t0_min_2,
                          in  Word16 t0_max_2,
                          out Word16 t0_min,
                          out Word16 t0_max
                          )
{
  void main(void)
  {
    /*--------------------------------------------------------------*
     *                 Closed-loop fractional pitch search          *
     *--------------------------------------------------------------*/
	  
    /* flag for first and 3rd subframe (==0 for first and third subframe) */
    pit_flag = i_subfr;                                  
	  
    /* set t0_min and t0_max for 3th subf.*/
    if (sub (i_subfr, L_FRAME_BY2) == 0)
    {
      pit_flag = 0;
      t0_min = t0_min_2;
      t0_max = t0_max_2;                                    
    }
  }
};


behavior Closed_Loop_Seq4 (
                           in  Word16 ana_content,
                           in  Word16 T0,
                           out Word16 ana[2],
                           out Word16 t0
                           )
{
  void main(void)
  {
    ana[0] = ana_content;
    t0 = T0;
  }
};


behavior Closed_Loop_Seq5(out Word16 gain_pit)
{
  void main(void)
  {
    gain_pit = 0;                                        
  }
};


behavior Closed_Loop_Seq6 (
                           in  Word16 y1b[L_SUBFR],
                           in  Word16 gain_pitch,
                           in  Word16 ana_content,
                           out Word16 y1[L_SUBFR],
                           out Word16 gain_pit,
                           out Word16 ana[2])
{
  void main(void)
  {
    y1 = y1b;
    gain_pit = gain_pitch;
    ana[1]   = ana_content;
  }
};



behavior Closed_Loop (
                      in  Int    i_subfr,
                      in  Word16 A[MP1],
                      in  Word16 Aq[MP1],
                      out Word16 h1[L_SUBFR],
                      in  Word16 *p_speech,
                      out Word16 res2[L_SUBFR],
                          Word16 *p_exc,
                          Word16 xn[L_SUBFR],
                      in  Word16 mem_err[M],
                          Word16 mem_w0[M],
                          Word16 t0_min,
                          Word16 t0_max,
                      in  Word16 t0_min_2,
                      in  Word16 t0_max_2,
                      out Word16 t0,
                      out Word16 ana[2],
                      out Word16 y1[L_SUBFR],
                      out Word16 gain_pit,
                      in  DTXctrl txdtx_ctrl,       /* voice activity flags */
                      out Word16 CN_excitation_gain,
                      in  Flag   reset_flag
		      )
  implements Ireset
{
  Word16 Ap1[MP1];
  Word16 Ap2[MP1];
  Word16 *p_speech_i;
  Word16 *p_exc_i;
  Word16 *p_h1;
  Word16 ana_content;
  Int pit_flag;
  Word16 T0;
  Word16 T0_frac;
  Word16 y1b[L_SUBFR];
  Word16 gain_pitch;

    
  Closed_Loop_Seq1 seq1(i_subfr, h1, p_exc, p_speech, p_h1, p_exc_i, 
                        p_speech_i);
  
  Par_Weight par_weight(A, Ap1, Ap2);

  Closed_Loop_Seq2 seq2(Aq, Ap1, Ap2, h1, p_speech_i, res2,
			p_exc_i, xn,  mem_err, mem_w0, txdtx_ctrl, 
			CN_excitation_gain, reset_flag);
  
  Closed_Loop_Seq3 seq3(i_subfr, pit_flag, t0_min_2, t0_max_2, t0_min, t0_max);
  
  Pitch_Fr6 pitch_fr6(p_exc_i, xn, p_h1, t0_min, t0_max, pit_flag, T0_frac, T0);
  Enc_Lag6  enc_lag6(T0, T0_frac, t0_min, t0_max, pit_flag, ana_content);
  
  Closed_Loop_Seq4 seq4(ana_content, T0, ana, t0);
  Closed_Loop_Seq5 seq5(gain_pit);
  
  Pred_Lt_6 pred_lt_6(p_exc_i, T0, T0_frac);

  Convolve  convolve(p_exc_i, p_h1, y1b);

  G_Pitch   g_pitch(xn, y1b, gain_pitch);
  Q_Gain_Pitch q_gain_pitch(gain_pitch, ana_content);

  Closed_Loop_Seq6 seq6(y1b, gain_pitch, ana_content, y1, gain_pit, ana);

  
  void reset(void)
  {
    seq2.reset();
  }



  void main(void)
  {
    fsm {
      
      seq1: {
	if ((txdtx_ctrl & TX_SP_FLAG) != 0) goto par_weight;
	goto seq2;
      }
      
      par_weight:
	  
      seq2: {
	if ((txdtx_ctrl & TX_SP_FLAG) != 0) goto seq3;
	goto seq5;
      }
      
      seq3:
      
      pitch_fr6: 

      enc_lag6: 

      seq4: {
	if ((txdtx_ctrl & TX_SP_FLAG) != 0) goto pred_lt_6;
	break;
      }
 	
      seq5: {
	if ((txdtx_ctrl & TX_SP_FLAG) != 0) goto pred_lt_6;
	break;
      }
      
      /*---------------------------------------------------------------*
       * - find unity gain pitch excitation (adaptive codebook entry)  *
       *   with fractional interpolation.                              *
       * - find filtered pitch exc. y1[]=exc[] convolved with h1[]     *
       * - compute pitch gain and limit between 0 and 1.2              *
       * - update target vector for codebook search                    *
       * - find LTP residual.                                          *
       *---------------------------------------------------------------*/
      
      pred_lt_6:
      convolve:
      g_pitch:

      q_gain_pitch:
      
      seq6:

    }
  }
};
