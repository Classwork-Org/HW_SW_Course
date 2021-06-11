#include "cnst.sh"
#include "typedef.sh"


import "basic_op";



behavior Upd_Mem (
  in  Word16 speech[L_SUBFR],
  in  Word16 synth[L_SUBFR],
  in  Word16 xn[L_SUBFR],           /* Target vector for pitch search        */
  in  Word16 y1[L_SUBFR],
  in  Word16 y2[L_SUBFR],
  in  Word16 gain_pit,
  in  Word16 gain_code,
  out Word16 mem_err[M],
  out Word16 mem_w0[M],
  in  DTXctrl txdtx_ctrl
  )
{
  void main(void)
    {
      Int i, j;
      Word16 k;
      Word16 temp;

      if ((txdtx_ctrl & TX_SP_FLAG) != 0)
        {
	  
	  for (i = L_SUBFR - M, j = 0; i < L_SUBFR; i++, j++)
            {
	      mem_err[j] = sub (speech[i], synth[i]);
	      
	      temp = extract_h (L_shl (L_mult (y1[i], gain_pit), 3));
	      k = extract_h (L_shl (L_mult (y2[i], gain_code), 5));
	      mem_w0[j] = sub (xn[i], add (temp, k));          
            }
        }
      else
        {
	  for (j = 0; j < M; j++)
            {
	      mem_err[j] = 0;                                  
	      mem_w0[j] = 0;                                   
            }
        }
      
    }
};
