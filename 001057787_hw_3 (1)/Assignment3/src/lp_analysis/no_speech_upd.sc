#include "typedef.sh"
#include "cnst.sh"


behavior No_Speech_Upd (
  in  Word16 A_t[4][MP1],        /* A(z) unquantized for the 4 subframes    */
  out Word16 Aq_t[4][MP1],       /* A(z) quantized for the 4 subframes      */
  out Word16 lsp_old[M],         /* old lsp[] (in case not found 10 roots)  */
  in  Word16 lsp_new[M],         /* LSPs at 4th subframe                    */
  out Word16 lsp_old_q[M],       /* old lsp[] (quantized)                   */
  in  Word16 lsp_new_q[M],       /* LSPs at 4th subframe (quantized)        */
  in  DTXctrl txdtx_ctrl         /* voice activity flags */
)
{
  void main(void)
    {
      Int i, j;

      if ((txdtx_ctrl & TX_SP_FLAG) == 0)
      {
	/* Use unquantized LPC parameters in case of no speech activity */
	for (i = 0; i < MP1; i++)
	{
          for(j = 0; j < 4; j++)            
            Aq_t[j][i] = A_t[j][i];
	}
	
	  /* update the LSPs for the next frame */
	  for (i = 0; i < M; i++)
          {
	      lsp_old[i] = lsp_new[i];                             
	      lsp_old_q[i] = lsp_new[i];
          }
      }
      else 
      {
	/* update the LSPs for the next frame */
	for (i = 0; i < M; i++)
	{
	  lsp_old[i] = lsp_new[i];                             
	  lsp_old_q[i] = lsp_new_q[i];                         
	}
      }
    }
};
