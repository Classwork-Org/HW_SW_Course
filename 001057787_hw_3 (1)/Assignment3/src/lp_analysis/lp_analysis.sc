#include "cnst.sh"
#include "typedef.sh"

#include "lp_analysis.tab"    // lpc window

import "reset";

import "find_az";
import "az_lsp";
import "vad_lp";
import "int_lpc";
import "q_plsf_and_intlpc";
import "no_speech_upd";



behavior LP_Analysis_Init(
			  in  Flag   reset_flag,
			  out Word16 old_A[MP1],
			  out Word16 lsp_old[M],
			  out Word16 lsp_old_q[M]
			  )
  implements Ireset
{
  
  void init(void)
    {
      Int i;
      Word16 lsp[M];
      
      old_A[0] = 4096;            /* Last A(z) for case of unstable filter */
      for (i = 1; i < MP1; i++)
	{
	  old_A[i] = 0;
	}

      /* Initialize lsp_old[], lsp_old_q */
      lsp[0] = 30000;
      lsp[1] = 26000;
      lsp[2] = 21000;
      lsp[3] = 15000;
      lsp[4] = 8000;
      lsp[5] = 0;
      lsp[6] = -8000;
      lsp[7] = -15000;
      lsp[8] = -21000;
      lsp[9] = -26000;
      
      lsp_old   = lsp;
      lsp_old_q = lsp;
      
    }

  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    if (reset_flag) init();
  }
};


behavior LP_Analysis_Seq1(
			  in  Word16* p_window,
			      Word16 r_h[MP1],
			      Word16 r_l[MP1],
			      Word16 old_A[MP1],
			  out Word16 scal_fac,
			  out Word16 A_t_1[MP1],
			  out Word16 A_t_3[MP1],
			  out Word16 rc[4]
			  )
{
  Word16 dummy_fac;
  Word16 dummy_rc[4];
  Word16 dummy_r_h[MP1];
  Word16 dummy_r_l[MP1];
  
  /* LP analysis centered at 2nd subframe */
  Find_Az find_az_1(p_window, window_160_80, dummy_r_h, dummy_r_l, old_A, 
		    dummy_fac, A_t_1, dummy_rc);

  /* LP analysis centered at 4th subframe */
  Find_Az find_az_2(p_window, window_232_8,  r_h, r_l, old_A, scal_fac, 
		    A_t_3, rc);

  void main(void)
  {
    par 
    {	  
      find_az_1.main(); 
      find_az_2.main(); 
    }
  }
};



behavior LP_Analysis_Seq2(
			  in  Word16 lsp_old[M],
			  in  Word16 lsp_mid[M],
			  in  Word16 lsp_new[M],
			      Word16 lsp_old_q[M],
			      Word16 lsp_mid_q[M],
			      Word16 lsp_new_q[M],
			  out Word16 A_t[4][MP1],
			  out Word16 Aq_t[4][MP1],
			  out Word16 ana[PRM_SIZE],
			  in  DTXctrl txdtx_ctrl,
			  in  Flag reset_flag
			  )
  implements Ireset
{
  Int_Lpc2 int_lpc2(lsp_old, lsp_mid, lsp_new, A_t);

  Q_Plsf_And_Intlpc q_plsf_and_intlpc(Aq_t, lsp_old, lsp_mid, lsp_new, 
				      lsp_old_q, lsp_mid_q, lsp_new_q, ana, 
				      txdtx_ctrl, reset_flag);

  
  void reset(void)
  {
    q_plsf_and_intlpc.reset();
  }
  
  void main(void)
  {
      par
	{
	  int_lpc2.main();
	  q_plsf_and_intlpc.main();     
	}
  }
};



behavior LP_Analysis_Copy1(
			   in  Word16 A_t_1[MP1],
                           in  Word16 A_t_3[MP1],
                           out Word16 A_t[4][MP1]
                           )
{
  void main(void)
  {
      A_t[1] = A_t_1;
      A_t[3] = A_t_3;
  }
};



behavior LP_Analysis_Copy2(
			   in  Word16 A[4][MP1],
                           out Word16 A_t[4][MP1]
                           )
{
  void main(void)
  {
      A_t = A;
  }
};



behavior LP_Analysis (
  in  Word16 *p_window,     /* (i): input signal                            */
  out Word16 A_t[4][MP1],   /* (o): A(z) unquantized for the 4 subframes    */
  out Word16 Aq_t[4][MP1],  /* (o): A(z) quantized for the 4 subframes      */
  out Word16 ana[PRM_SIZE], /* (o): quantization indices (ana[0..4])        */
  in  Flag   ptch,          /* flag to indicate a periodic signal component */
      DTXctrl txdtx_ctrl,   /* voice activity flags */
  in  Flag   dtx_mode,
  out Word32 L_pn_seed_tx,
  in  Flag   reset_flag
  )
implements Ireset
{
  Word16 old_A[MP1];               /* Last A(z) for case of unstable filter */

  Word16 lsp_old[M], lsp_old_q[M];
  Word16 lsp_new[M], lsp_new_q[M];      /* LSPs at 4th subframe             */
  Word16 lsp_mid[M], lsp_mid_q[M];      /* LSPs at 2nd subframe             */

  Word16 rc[4];                         /* First 4 reflection coefficients  */

  Word16 A_t_1[MP1];
  Word16 A_t_3[MP1];
  Word16 A[4][MP1];
  Word16 scal_fac;
  Word16 r_l[MP1], r_h[MP1];

  LP_Analysis_Init init(reset_flag, old_A, lsp_old, lsp_old_q);
  
  LP_Analysis_Seq1 seq1(p_window, r_h, r_l, old_A, scal_fac, A_t_1, A_t_3, rc);
    
  Az_Lsp az_lsp_1(A_t_1, lsp_mid, lsp_old);
  Az_Lsp az_lsp_2(A_t_3, lsp_new, lsp_mid);

  LP_Analysis_Copy1 copy1(A_t_1, A_t_3, A);
  
  Vad_Lp vad_lp(r_l, r_h, scal_fac, rc, ptch, txdtx_ctrl, dtx_mode,
		L_pn_seed_tx, reset_flag);

  LP_Analysis_Seq2 seq2(lsp_old, lsp_mid, lsp_new, lsp_old_q, lsp_mid_q, 
			lsp_new_q, A, Aq_t, ana, txdtx_ctrl, reset_flag);
  
  No_Speech_Upd no_speech_upd(A, Aq_t, lsp_old, lsp_new, lsp_old_q, 
			      lsp_new_q, txdtx_ctrl);

  LP_Analysis_Copy2 copy2(A, A_t);
  

  void reset(void)
    {
      init.reset();
      seq2.reset();
      vad_lp.reset();
    }
  

  void main(void)
  {
      init.main(); 
      
      seq1.main();
      
      az_lsp_1.main();
      az_lsp_2.main();

      copy1.main();
    
      vad_lp.main();

      seq2.main();

      no_speech_upd.main();
    
      copy2.main();
  }
};
