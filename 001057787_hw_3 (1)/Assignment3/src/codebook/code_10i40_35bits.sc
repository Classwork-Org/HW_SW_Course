// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: code_10i40_35bits.sc
// ------------------------------------------------------------------------
// 
// Search the algebraic (fixed) codebook to find the pulse indices.
//
//
// 09/13/02  <gerstl>	Merged in fixes for input of scrtl tool
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 01/08/02  <gerstl>	Updated to comply with extended port checking
// 07/21/98  <gerstl>

#include "cnst.sh"
#include "typedef.sh"


import "q_p";
import "cor_h_x";
import "set_sign";
import "cor_h";
import "search_10i40";
import "build_code";


behavior Code_10i40_35bits (in  Word16 x[L_SUBFR], 
			    in  Word16 cn[L_SUBFR],
			    in  Word16 h[L_SUBFR],
			    out Word16 cod[L_SUBFR],
			    out Word16 y[L_SUBFR],
			    out Word16 prm[10])
{
  Word16 dn[L_SUBFR], dn2[L_SUBFR];
  Word16 sign[L_SUBFR], pos_max[5], ipos[M], indx[10];
  Word16 rr[L_SUBFR * L_SUBFR];
  Word16 codevec[M];


  Cor_h_x      cor_h_x (h, x, dn);
  Set_Sign     set_sign (dn, dn2, cn, sign, pos_max, ipos);
  Cor_h        cor_h (h, sign, rr);
  Search_10i40 search_10i40 (dn2, rr, ipos, pos_max, codevec);
  Build_Code   build_code (codevec, sign, cod, h, y, indx);
  Q_p          q_p (indx, prm);

  void main(void) 
  {
    cor_h_x.main();
    set_sign.main();
    cor_h.main();
    search_10i40.main();
    build_code.main();
    q_p.main();
  }   
};
