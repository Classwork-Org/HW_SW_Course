//
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "array_op";

import "closed_loop";
import "codebook_cn";
import "update";



behavior Subframes_Init(
                         in  Word16 T0_min_1, 
                         in  Word16 T0_max_1, 
                         out Word16 ana[PRM_SIZE], 
                         out Word16 T0_min, 
                         out Word16 T0_max, 
			 out Word16 *p_ana,
                         out Int i,
                         out Int i_subfr, 
                         out Word16 mem_w0[M],
			 out Word16 mem_err[M],
                         in  Flag reset_flag, 
                         out Flag reset_flag_1
                         )
  implements Ireset
{
  void init(void)
    {
      Set_zero ((Word16*)mem_w0, M);
      Set_zero ((Word16*)mem_err, M);
    }

  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    reset_flag_1 = reset_flag;
    if (reset_flag) init();
    
    /* pointer to speech parameters */
    p_ana = &ana[5];   // LP parameters have already been written
                       // start on codebook parameters

    /* range for closed loop pitch search, subframes 1&2*/
    T0_min = T0_min_1;
    T0_max = T0_max_1;                                    

    i = 0;
    i_subfr = 0;
  }
};



behavior Subframes_Body1(
                         in  Int i,
                         in  Word16 A_t[4][MP1],
                         in  Word16 Aq_t[4][MP1],
                         out Word16 A_t_i[MP1],
                         out Word16 Aq_t_i[MP1]
                         )
{
  void main(void)
  {
    A_t_i  = A_t[i];     /* interpolated LPC parameters           */
    Aq_t_i = Aq_t[i];    /* interpolated quantized LPC parameters */    
  }
};



behavior Subframes_Body2(
                         in  Int i_subfr,
                         in  Word16 *p_exc,
                         in  Word16 *p_speech,
                         out Word16 exc_i[L_SUBFR],
                         out Word16 speech_i[L_SUBFR]
                         )
{
  void main(void)
  {
    Copy(p_exc + i_subfr, (Word16*)exc_i, L_SUBFR);
    Copy(p_speech + i_subfr, (Word16*)speech_i, L_SUBFR);    
  }
};



behavior Subframes_End(
                       in  Word16 cl_ana[2],
                       in  Word16 cb_ana[10],
                       in  Word16 up_ana,
                       in  Word16 exc_i[L_SUBFR],
                       in  Word16 synth_i[L_SUBFR],
                           Word16 *p_ana,
                           Int    i,
                           Int    i_subfr,
                           Word16 *p_exc,
                       out Word16 synth[L_FRAME],
		       out   Flag reset_flag_1
                       )
{
  void main(void)
  {
    *p_ana++ = cl_ana[0];
    *p_ana++ = cl_ana[1];
      
    Copy(cb_ana, p_ana, 10);
    p_ana += 10;
    
    *p_ana++ = up_ana;
        
    Copy(exc_i, p_exc + i_subfr, L_SUBFR);
    Copy(synth_i, (Word16*)synth + i_subfr, L_SUBFR);
     
    reset_flag_1 = false;
    
    i_subfr += L_SUBFR;
    i++;
  }
};




behavior Subframes(
       Word16 A_t[4][MP1],      /* (i/o): A(z) unquantized for the 4 frames  */
       Word16 Aq_t[4][MP1],     /* (i/o): A(z) quantized for the 4 subframes */
   in  Word16 *p_speech,
       Word16 *p_exc,
   in  Word16 T0_min_1,
   in  Word16 T0_max_1,
   in  Word16 T0_min_2,
   in  Word16 T0_max_2,
   out Word16 ana[PRM_SIZE],        /* output  : Analysis parameters */
   out Word16 synth[L_FRAME],       /* output  : Local synthesis     */
		     
   in  DTXctrl txdtx_ctrl,
       Word32 L_pn_seed_tx,
   in  Flag reset_flag		   
   )
implements Ireset
{
  Int i, i_subfr;
  
  Word16 h1[L_SUBFR];
    
  Word16 T0_min, T0_max;
  Word16 T0;

  Word16 exc_i[L_SUBFR];
  Word16 speech_i[L_SUBFR];
  Word16 synth_i[L_SUBFR];
  Word16 A_t_i[MP1], Aq_t_i[MP1];

  Word16 *p_ana;
  Word16 cl_ana[2];
  Word16 cb_ana[10];
  Word16 up_ana;

  Word16 gain_pit, gain_code;
  Word16 xn[L_SUBFR];            /* Target vector for pitch search        */
  Word16 res2[L_SUBFR];          /* Long term prediction residual         */
  Word16 code[L_SUBFR];          /* Fixed codebook excitation             */
  Word16 y1[L_SUBFR];            /* Filtered adaptive excitation          */
  Word16 y2[L_SUBFR];            /* Filtered fixed codebook excitation    */

  /* Filter's memory */
  Word16 mem_w0[M];
  Word16 mem_err[M];

  Word16 CN_excitation_gain;

  Flag reset_flag_1;

  
  Subframes_Init  for_init(T0_min_1, T0_max_1, ana, T0_min, T0_max, p_ana, i,
			   i_subfr, mem_w0, mem_err, reset_flag, reset_flag_1);
  
  Subframes_Body1 for_body1(i, A_t, Aq_t, A_t_i, Aq_t_i);
  
  Closed_Loop closed_loop(i_subfr, A_t_i, Aq_t_i, h1, p_speech, res2, p_exc,
			  xn, mem_err, mem_w0, T0_min, T0_max, T0_min_2, 
			  T0_max_2, T0, cl_ana, y1, gain_pit, txdtx_ctrl, 
			  CN_excitation_gain, reset_flag_1);

  Subframes_Body2 for_body2(i_subfr, p_exc, p_speech, exc_i, speech_i);
  
  Codebook_CN codebook_cn(xn, y1, gain_pit, exc_i, h1, T0, res2,
			  code, y2, gain_code, cb_ana, txdtx_ctrl,
			  L_pn_seed_tx);

  Update update(i_subfr, Aq_t_i, gain_pit, gain_code, exc_i, speech_i, synth_i,
		up_ana, xn, y1, y2, code, mem_err, mem_w0, txdtx_ctrl, 
		CN_excitation_gain, reset_flag_1);

  Subframes_End   for_end(cl_ana, cb_ana, up_ana, exc_i, synth_i, p_ana, i,
                          i_subfr, p_exc, synth, reset_flag_1);
  
  

  void reset(void)
    {
      for_init.reset();
      closed_loop.reset();
      update.reset();
    }

  void main(void)
    {
      fsm {
	for_init: {
	  if (i_subfr < L_FRAME) goto for_body1;
	  break;
	}

	for_body1:
        
	closed_loop: 
          
	for_body2:
          
	codebook_cn:
		
	update:
	
	for_end: {
	  if (i_subfr < L_FRAME) goto for_body1;
	  // break;
	}
	
      }
    }
};
