// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: codebook.sc
// ------------------------------------------------------------------------
// 
// Algebraic fixed codebook search
//
//
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 08/21/98  <szhao>

#include "typedef.sh"
#include "cnst.sh"



import "pitch_contr";
import "prefilter";
import "code_10i40_35bits";
import "g_code";



behavior Codebook_Seq1(
		       in  Word16 xn[L_SUBFR],
		       in  Word16 y1[L_SUBFR],
		       in  Word16 gain_pit,
		       in  Word16 exc[L_SUBFR],
                       in  Word16 h1[L_SUBFR],
		       in  Word16 T0,
		       in  Word16 res2[L_SUBFR],
		       out Word16 xn2[L_SUBFR],
                       out Word16 h1b[L_SUBFR],
		       out Word16 res2b[L_SUBFR]
		       )
{
  Pitch_Contr       upd_target (xn, y1, gain_pit, xn2);
  Pitch_Contr       upd_res (res2, exc, gain_pit, res2b);
  Prefilter         filter_h (T0, gain_pit, h1, h1b);
  
  void main(void)
  {
      par 
	{
	  upd_target.main();
	  upd_res.main();
	  filter_h.main();
	}
  }
};


behavior Codebook_Seq2(
		       in  Word16 T0,
		       in  Word16 gain_pit,
		       in  Word16 codeb[L_SUBFR],
		       in  Word16 xn2[L_SUBFR],
		       in  Word16 y2[L_SUBFR],
                       out Word16 code[L_SUBFR],
		       out Word16 gain_code
		       )
{ 
  Prefilter         filter_c (T0, gain_pit, codeb, code);
  Gain_Code         g_code (xn2, y2, gain_code);
  
  void main(void)
  {
    par
	{
	  filter_c.main();
	  g_code.main();
	}
  }
};
		       

behavior Codebook(
	in  Word16 xn[L_SUBFR],
	in  Word16 y1[L_SUBFR],
	in  Word16 gain_pit,
	in  Word16 exc[L_SUBFR],
	in  Word16 h1[L_SUBFR],
	in  Word16 T0,
	in  Word16 res2[L_SUBFR],
	out Word16 code[L_SUBFR],
	    Word16 y2[L_SUBFR],
	out Word16 gain_code,
	out Word16 ana[10])
{
  Word16 xn2[L_SUBFR];
  Word16 res2b[L_SUBFR];
  Word16 h1b[L_SUBFR];
  Word16 codeb[L_SUBFR];

  Codebook_Seq1 seq1(xn, y1, gain_pit, exc, h1, T0, res2, xn2, h1b, res2b);
				       
  Code_10i40_35bits code_10i40 (xn2, res2b, h1b, codeb, y2, ana);

  Codebook_Seq2 seq2(T0, gain_pit, codeb, xn2, y2, code, gain_code);
  
  
  void main(void)
    {
      seq1.main();
      
      code_10i40.main();
      
      seq2.main();
    }
};
