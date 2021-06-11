/*************************************************************************
 *   FUNCTION:  Q_plsf_5()
 *
 *   PURPOSE:  Quantization of 2 sets of LSF parameters using 1st order MA
 *             prediction and split by 5 matrix quantization (split-MQ)
 *
 *   DESCRIPTION:
 *
 *        p[i] = pred_factor*past_r2q[i];   i=0,...,m-1
 *        r1[i]= lsf1[i] - p[i];      i=0,...,m-1
 *        r2[i]= lsf2[i] - p[i];      i=0,...,m-1
 *   where:
 *        lsf1[i]           1st mean-removed LSF vector.
 *        lsf2[i]           2nd mean-removed LSF vector.
 *        r1[i]             1st residual prediction vector.
 *        r2[i]             2nd residual prediction vector.
 *        past_r2q[i]       Past quantized residual (2nd vector).
 *
 *   The residual vectors r1[i] and r2[i] are jointly quantized using
 *   split-MQ with 5 codebooks. Each 4th dimension submatrix contains 2
 *   elements from each residual vector. The 5 submatrices are as follows:
 *     {r1[0], r1[1], r2[0], r2[1]};  {r1[2], r1[3], r2[2], r2[3]};
 *     {r1[4], r1[5], r2[4], r2[5]};  {r1[6], r1[7], r2[6], r2[7]};
 *                    {r1[8], r1[9], r2[8], r2[9]};
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "reset";


import "basic_op";



#include "q_plsf_5.tab"         /* Codebooks of LSF prediction residual */
                                /* and Look-up table for transformations */



/* LSF_GAP  -> Minimum distance between LSF after quantization */
/*             50 Hz = 205                                     */
/* PRED_FAC -> Predcition factor                               */

#define LSF_GAP   205
#define PRED_FAC  21299


behavior Q_Plsf_5 (
  in  Word16 lsp1[M],          /* input : 1st LSP vector                     */
  in  Word16 lsp2[M],          /* input : 2nd LSP vector                     */
  out Word16 lsp1_q[M],        /* output: quantized 1st LSP vector           */
  out Word16 lsp2_q[M],        /* output: quantized 2nd LSP vector           */
  out Word16 indice[PRM_SIZE], /* output: quantization indices of 5 matrices */
  in  DTXctrl txdtx_ctrl,      /* input : tx dtx control word                */
  in  Flag   reset_flag		   
  )
implements Ireset
{
  Word16 past_r2_q[M];                /* Past quantized prediction error */
  Word16 lsf_old_tx[DTX_HANGOVER][M]; /* Comfort noise LSF averaging buffer  */


  void init(void)
    {
      Int i;
      
      for (i = 0; i < M; i++)
	{
	  past_r2_q[i] = 0;       /* Past quantized prediction error */
	}

      for (i = 0; i < DTX_HANGOVER; i++)
	{
	  lsf_old_tx[i][0] = 1384;
	  lsf_old_tx[i][1] = 2077;
	  lsf_old_tx[i][2] = 3420;
	  lsf_old_tx[i][3] = 5108;
	  lsf_old_tx[i][4] = 6742;
	  lsf_old_tx[i][5] = 8122;
	  lsf_old_tx[i][6] = 9863;
	  lsf_old_tx[i][7] = 11092;
	  lsf_old_tx[i][8] = 12714;
	  lsf_old_tx[i][9] = 13701;
	}    
    }


  void reset(void)
  {
    init();
  }




  /*************************************************************************
   *
   *   FUNCTION NAME: update_lsf_history
   *
   *   PURPOSE: Update the LSF parameter history. The LSF parameters kept
   *            in the buffer are used later for computing the reference
   *            LSF parameter vector and the averaged LSF parameter vector.
   *
   *   INPUTS:      lsf1[0..9]    LSF vector of the 1st half of the frame
   *                lsf2[0..9]    LSF vector of the 2nd half of the frame
   *                lsf_old[0..DTX_HANGOVER-1][0..M-1]
   *                              Old LSF history
   *
   *   OUTPUTS:     lsf_old[0..DTX_HANGOVER-1][0..M-1]
   *                              Updated LSF history
   *
   *   RETURN VALUE: none
   *
   *************************************************************************/

  void update_lsf_history (
			   Word16 lsf1[M],
			   Word16 lsf2[M],
			   Word16 lsf_old[DTX_HANGOVER][M]
			   )
    {
      Int i, j; 
      Word16 temp;

      /* shift LSF data to make room for LSFs from current frame */
      /* This can also be implemented by using circular buffering */

      for (i = DTX_HANGOVER - 1; i > 0; i--)
	{
	  for (j = 0; j < M; j++)
	    {
	      lsf_old[i][j] = lsf_old[i - 1][j];          
	    }
	}

      /* Store new LSF data to lsf_old buffer */

      for (i = 0; i < M; i++)
	{
	  temp = add (shr (lsf1[i], 1), shr (lsf2[i], 1));
	  lsf_old[0][i] = temp;                           
	}

      return;
    }

  /*************************************************************************
   *
   *   FUNCTION NAME: update_lsf_p_CN
   *
   *   PURPOSE: Update the reference LSF parameter vector. The reference
   *            vector is computed by averaging the quantized LSF parameter
   *            vectors which exist in the LSF parameter history.
   *
   *   INPUTS:      lsf_old[0..DTX_HANGOVER-1][0..M-1]
   *                                 LSF parameter history
   *
   *   OUTPUTS:     lsf_p_CN[0..9]   Computed reference LSF parameter vector
   *
   *   RETURN VALUE: none
   *
   *************************************************************************/

  void update_lsf_p_CN (
			Word16 lsf_old[DTX_HANGOVER][M],
			Word16 lsf_p_CN[M]
			)
    {
      Int i, j;
      Word32 L_temp;

      for (j = 0; j < M; j++)
	{
	  L_temp = L_mult (INV_DTX_HANGOVER, lsf_old[0][j]);
	  for (i = 1; i < DTX_HANGOVER; i++)
	    {
	      L_temp = L_mac (L_temp, INV_DTX_HANGOVER, lsf_old[i][j]);
	    }
	  lsf_p_CN[j] = round (L_temp);                   
	}

      return;
    }

  /*************************************************************************
   *
   *   FUNCTION NAME: aver_lsf_history
   *
   *   PURPOSE: Compute the averaged LSF parameter vector. Computation is
   *            performed by averaging the LSF parameter vectors which exist
   *            in the LSF parameter history, together with the LSF
   *            parameter vectors of the current frame.
   *
   *   INPUTS:      lsf_old[0..DTX_HANGOVER-1][0..M-1]
   *                                   LSF parameter history
   *                lsf1[0..M-1]       LSF vector of the 1st half of the frame
   *                lsf2[0..M-1]       LSF vector of the 2nd half of the frame
   *
   *   OUTPUTS:     lsf_aver[0..M-1]   Averaged LSF parameter vector
   *
   *   RETURN VALUE: none
   *
   *************************************************************************/

  void aver_lsf_history (
			 Word16 lsf_old[DTX_HANGOVER][M],
			 Word16 lsf1[M],
			 Word16 lsf2[M],
			 Word16 lsf_aver[M]
			 )
    {
      Int i, j;
      Word32 L_temp;

      for (j = 0; j < M; j++)
	{
	  L_temp = L_mult (0x3fff, lsf1[j]);
	  L_temp = L_mac (L_temp, 0x3fff, lsf2[j]);
	  L_temp = L_mult (INV_DTX_HANGOVER_P1, extract_h (L_temp));

	  for (i = 0; i < DTX_HANGOVER; i++)
	    {
	      L_temp = L_mac (L_temp, INV_DTX_HANGOVER_P1, lsf_old[i][j]);
	    }

	  lsf_aver[j] = extract_h (L_temp);               
	}

      return;
    }


  /* Quantization of a 4 dimensional subvector */

  Word16 Vq_subvec(      /* output: return quantization index     */
    Word16 *lsf_r1,     /* input : 1st LSF residual vector       */
    Word16 *lsf_r2,     /* input : and LSF residual vector       */
    const Word16 *dico, /* input : quantization codebook         */
    Word16 *wf1,        /* input : 1st LSF weighting factors     */
    Word16 *wf2,        /* input : 2nd LSF weighting factors     */
    Word16 dico_size    /* input : size of quantization codebook */
  )
    {
      Int i;
      Word16 index, temp;
      const Word16 *p_dico;
      Word32 dist_min, dist;

      dist_min = MAX_32;                                   
      p_dico = dico;                                       

      for (i = 0; i < dico_size; i++)
	{
	  temp = sub (lsf_r1[0], *p_dico++);
	  temp = mult (wf1[0], temp);
	  dist = L_mult (temp, temp);

	  temp = sub (lsf_r1[1], *p_dico++);
	  temp = mult (wf1[1], temp);
	  dist = L_mac (dist, temp, temp);

	  temp = sub (lsf_r2[0], *p_dico++);
	  temp = mult (wf2[0], temp);
	  dist = L_mac (dist, temp, temp);

	  temp = sub (lsf_r2[1], *p_dico++);
	  temp = mult (wf2[1], temp);
	  dist = L_mac (dist, temp, temp);

	   
	  if (L_sub (dist, dist_min) < (Word32) 0)
	    {
	      dist_min = dist;                             
	      index = i;                                   
	    }
	}

      /* Reading the selected vector */

      p_dico = &dico[shl (index, 2)];                      
      lsf_r1[0] = *p_dico++;                               
      lsf_r1[1] = *p_dico++;                               
      lsf_r2[0] = *p_dico++;                               
      lsf_r2[1] = *p_dico++;                               

      return index;

    }


  /* Quantization of a 4 dimensional subvector with a signed codebook */

  Word16 Vq_subvec_s (    /* output: return quantization index     */
      Word16 *lsf_r1,     /* input : 1st LSF residual vector       */
      Word16 *lsf_r2,     /* input : and LSF residual vector       */
      const Word16 *dico, /* input : quantization codebook         */
      Word16 *wf1,        /* input : 1st LSF weighting factors     */
      Word16 *wf2,        /* input : 2nd LSF weighting factors     */
      Word16 dico_size)   /* input : size of quantization codebook */
    {
      Int i;
      Word16 index, temp;
      Flag sign;
      const Word16 *p_dico;
      Word32 dist_min, dist;

      dist_min = MAX_32;                                   
      p_dico = dico;                                       

      for (i = 0; i < dico_size; i++)
	{
	  /* test positive */

	  temp = sub (lsf_r1[0], *p_dico++);
	  temp = mult (wf1[0], temp);
	  dist = L_mult (temp, temp);

	  temp = sub (lsf_r1[1], *p_dico++);
	  temp = mult (wf1[1], temp);
	  dist = L_mac (dist, temp, temp);

	  temp = sub (lsf_r2[0], *p_dico++);
	  temp = mult (wf2[0], temp);
	  dist = L_mac (dist, temp, temp);

	  temp = sub (lsf_r2[1], *p_dico++);
	  temp = mult (wf2[1], temp);
	  dist = L_mac (dist, temp, temp);

	   
	  if (L_sub (dist, dist_min) < (Word32) 0)
	    {
	      dist_min = dist;                             
	      index = i;                                   
	      sign = 0;                                    
	    }


	  /* test negative */

	  p_dico -= 4;                                     
	  temp = add (lsf_r1[0], *p_dico++);
	  temp = mult (wf1[0], temp);
	  dist = L_mult (temp, temp);

	  temp = add (lsf_r1[1], *p_dico++);
	  temp = mult (wf1[1], temp);
	  dist = L_mac (dist, temp, temp);

	  temp = add (lsf_r2[0], *p_dico++);
	  temp = mult (wf2[0], temp);
	  dist = L_mac (dist, temp, temp);

	  temp = add (lsf_r2[1], *p_dico++);
	  temp = mult (wf2[1], temp);
	  dist = L_mac (dist, temp, temp);

	   
	  if (L_sub (dist, dist_min) < (Word32) 0)
	    {
	      dist_min = dist;                             
	      index = i;                                   
	      sign = 1;                                    
	    }
	}

      /* Reading the selected vector */

      p_dico = &dico[shl (index, 2)];                      
       
      if (sign == 0)
	{
	  lsf_r1[0] = *p_dico++;                           
	  lsf_r1[1] = *p_dico++;                           
	  lsf_r2[0] = *p_dico++;                           
	  lsf_r2[1] = *p_dico++;                           
	}
      else
	{
	  lsf_r1[0] = negate (*p_dico++);                  
	  lsf_r1[1] = negate (*p_dico++);                  
	  lsf_r2[0] = negate (*p_dico++);                  
	  lsf_r2[1] = negate (*p_dico++);                  
	}

      index = shl (index, 1);
      index = add (index, sign);

      return index;

    }



  /****************************************************
   * FUNCTION  Lsf_wt                                                         *
   *                                                                          *
   ****************************************************
   * Compute LSF weighting factors                                            *
   *                                                                          *
   *  d[i] = lsf[i+1] - lsf[i-1]                                              *
   *                                                                          *
   *  The weighting factors are approximated by two line segment.             *
   *                                                                          *
   *  First segment passes by the following 2 points:                         *
   *                                                                          *
   *     d[i] = 0Hz     wf[i] = 3.347                                         *
   *     d[i] = 450Hz   wf[i] = 1.8                                           *
   *                                                                          *
   *  Second segment passes by the following 2 points:                        *
   *                                                                          *
   *     d[i] = 450Hz   wf[i] = 1.8                                           *
   *     d[i] = 1500Hz  wf[i] = 1.0                                           *
   *                                                                          *
   *  if( d[i] < 450Hz )                                                      *
   *    wf[i] = 3.347 - ( (3.347-1.8) / (450-0)) *  d[i]                      *
   *  else                                                                    *
   *    wf[i] = 1.8 - ( (1.8-1.0) / (1500-450)) *  (d[i] - 450)               *
   *                                                                          *
   *                                                                          *
   *  if( d[i] < 1843)                                                        *
   *    wf[i] = 3427 - (28160*d[i])>>15                                       *
   *  else                                                                    *
   *    wf[i] = 1843 - (6242*(d[i]-1843))>>15                                 *
   *                                                                          *
   *-------------------------------------------------------------------------*/

  void Lsf_wt (
	       Word16 *lsf,         /* input : LSF vector                  */
	       Word16 *wf)          /* output: square of weighting factors */
    {
      Word16 temp;
      Int i;


      /* wf[0] = lsf[1] - 0  */
      wf[0] = lsf[1];                                      
      for (i = 1; i < 9; i++)
	{
	  wf[i] = sub (lsf[i + 1], lsf[i - 1]);            
	}
      /* wf[9] = 0.5 - lsf[8] */    
      wf[9] = sub (16384, lsf[8]);      

      for (i = 0; i < 10; i++)
	{
	  temp = sub (wf[i], 1843);
	   
	  if (temp < 0)
	    {
	      wf[i] = sub (3427, mult (wf[i], 28160));     
	    }
	  else
	    {
	      wf[i] = sub (1843, mult (temp, 6242));       
	    }

	  wf[i] = shl (wf[i], 3);  
	}
      return;
    }



  /*************************************************************************
   *
   *   FUNCTIONS:  Lsp_lsf and Lsf_lsp
   *
   *   PURPOSE:
   *      Lsp_lsf:   Transformation lsp to lsf
   *      Lsf_lsp:   Transformation lsf to lsp
   *
   *   DESCRIPTION:
   *         lsp[i] = cos(2*pi*lsf[i]) and lsf[i] = arccos(lsp[i])/(2*pi)
   *
   *   The transformation from lsp[i] to lsf[i] and lsf[i] to lsp[i] are
   *   approximated by a look-up table and interpolation.
   *
   *************************************************************************/

  void lsf_lsp (
     Word16 lsf[],       /* (i) : lsf[m] normalized (range: 0.0<=val<=0.5) */
     Word16 *lsp,        /* (o) : lsp[m] (range: -1<=val<1)                */
     Word16 m            /* (i) : LPC order                                */
		)
    {
      Int i, ind;
      Word16 offset;
      Word32 L_tmp;

      for (i = 0; i < m; i++)
	{
	  ind = shr (lsf[i], 8);      /* ind    = b8-b15 of lsf[i] */
	  offset = lsf[i] & 0x00ff;    /* offset = b0-b7  of lsf[i] */

	  /* lsp[i] = table[ind]+ ((table[ind+1]-table[ind])*offset) / 256 */

	  L_tmp = L_mult (sub (table[ind + 1], table[ind]), offset);
	  lsp[i] = add (table[ind], extract_l (L_shr (L_tmp, 9)));
	   
	}
      return;
    }

  void lsp_lsf (
     Word16 lsp[],       /* (i)  : lsp[m] (range: -1<=val<1)                */
     Word16 *lsf,        /* (o)  : lsf[m] normalized (range: 0.0<=val<=0.5) */
     Word16 m            /* (i)  : LPC order                                */
		)
    {
      Int i, ind;
      Word32 L_tmp;

      ind = 63;                       /* begin at end of table -1 */

      for (i = m - 1; i >= 0; i--)
	{
	  /* find value in table that is just greater than lsp[i] */
	   
	  while (sub (table[ind], lsp[i]) < 0)
	    {
	      ind--;
	       
	    }

	  /* acos(lsp[i])= ind*256 + ( ( lsp[i]-table[ind] ) *
	     slope[ind] )/4096 */

	  L_tmp = L_mult (sub (lsp[i], table[ind]), slope[ind]);
	  /*(lsp[i]-table[ind])*slope[ind])>>12*/
	  lsf[i] = round (L_shl (L_tmp, 3));       
	  lsf[i] = add (lsf[i], shl (ind, 8));     
	}
      return;
    }



  /*************************************************************************
   *
   *  FUNCTION:  Reorder_lsf()
   *
   *  PURPOSE: To make sure that the LSFs are properly ordered and to keep a
   *           certain minimum distance between adjacent LSFs.
   *
   *           The LSFs are in the frequency range 0-0.5 and represented in Q15
   *
   *************************************************************************/
  

  void reorder_lsf (
    Word16 *lsf,        /* (i/o)     : vector of LSFs   (range: 0<=val<=0.5) */
    Word16 min_dist,    /* (i)       : minimum required distance             */
    Word16 n            /* (i)       : LPC order                             */
    )
    {
      Int i;
      Word16 lsf_min;
      
      lsf_min = min_dist;          
      for (i = 0; i < n; i++)
	{
	   
	  if (sub (lsf[i], lsf_min) < 0)
	    {
	      lsf[i] = lsf_min;    
	    }
	  lsf_min = add (lsf[i], min_dist);
	}
    }
  
  
  

  void main(void)
    {
      Int i;
      Word16 lsf1[M], lsf2[M], wf1[M], wf2[M], lsf_p[M], lsf_r1[M], lsf_r2[M];
      Word16 lsf1_q[M], lsf2_q[M];
      Word16 lsf_aver[M];
      static Word16 lsf_p_CN[M];

      if (reset_flag) init();
      
      /* convert LSFs to normalize frequency domain 0..16384  */

      lsp_lsf (lsp1, lsf1, M);
      lsp_lsf (lsp2, lsf2, M);

      /* Update LSF CN quantizer "memory" */

          
      if ((txdtx_ctrl & TX_SP_FLAG) == 0
	  && (txdtx_ctrl & TX_PREV_HANGOVER_ACTIVE) != 0)
	{
	  update_lsf_p_CN (lsf_old_tx, lsf_p_CN);
	}
        
      if ((txdtx_ctrl & TX_SID_UPDATE) != 0)
	{
	  /* New SID frame is to be sent:
	     Compute average of the current LSFs and the LSFs in the history */

	  aver_lsf_history (lsf_old_tx, lsf1, lsf2, lsf_aver);
	}
      /* Update LSF history with unquantized LSFs when no speech activity
	 is present */

        
      if ((txdtx_ctrl & TX_SP_FLAG) == 0)
	{
	  update_lsf_history (lsf1, lsf2, lsf_old_tx);
	}
        
      if ((txdtx_ctrl & TX_SID_UPDATE) != 0)
	{
	  /* Compute LSF weighting factors for lsf2, using averaged LSFs */
	  /* Set LSF weighting factors for lsf1 to zero */
	  /* Replace lsf1 and lsf2 by the averaged LSFs */

	  Lsf_wt (lsf_aver, wf2);
	  for (i = 0; i < M; i++)
	    {
	      wf1[i] = 0;                                  
	      lsf1[i] = lsf_aver[i];                       
	      lsf2[i] = lsf_aver[i];                       
	    }
	}
      else
	{
	  /* Compute LSF weighting factors */

	  Lsf_wt (lsf1, wf1);
	  Lsf_wt (lsf2, wf2);
	}

      /* Compute predicted LSF and prediction error */

        
      if ((txdtx_ctrl & TX_SP_FLAG) != 0)
	{
	  for (i = 0; i < M; i++)
	    {
	      lsf_p[i] = add (mean_lsf[i], mult (past_r2_q[i], PRED_FAC));
	       
	      lsf_r1[i] = sub (lsf1[i], lsf_p[i]);         
	      lsf_r2[i] = sub (lsf2[i], lsf_p[i]);         
	    }
	}
      else
	{
	  for (i = 0; i < M; i++)
	    {
	      lsf_r1[i] = sub (lsf1[i], lsf_p_CN[i]);      
	      lsf_r2[i] = sub (lsf2[i], lsf_p_CN[i]);      
	    }
	}

      /*---- Split-VQ of prediction error ----*/

      indice[0] = Vq_subvec (&lsf_r1[0], &lsf_r2[0], dico1_lsf,
			     &wf1[0], &wf2[0], DICO1_SIZE);
       

      indice[1] = Vq_subvec (&lsf_r1[2], &lsf_r2[2], dico2_lsf,
			     &wf1[2], &wf2[2], DICO2_SIZE);
       

      indice[2] = Vq_subvec_s (&lsf_r1[4], &lsf_r2[4], dico3_lsf,
			       &wf1[4], &wf2[4], DICO3_SIZE);
       

      indice[3] = Vq_subvec (&lsf_r1[6], &lsf_r2[6], dico4_lsf,
			     &wf1[6], &wf2[6], DICO4_SIZE);
       

      indice[4] = Vq_subvec (&lsf_r1[8], &lsf_r2[8], dico5_lsf,
			     &wf1[8], &wf2[8], DICO5_SIZE);
       

      /* Compute quantized LSFs and update the past quantized residual */
      /* In case of no speech activity, skip computing the quantized LSFs,
	 and set past_r2_q to zero (initial value) */

        
      if ((txdtx_ctrl & TX_SP_FLAG) != 0)
	{
	  for (i = 0; i < M; i++)
	    {
	      lsf1_q[i] = add (lsf_r1[i], lsf_p[i]);       
	      lsf2_q[i] = add (lsf_r2[i], lsf_p[i]);       
	      past_r2_q[i] = lsf_r2[i];                    
	    }

	  /* verification that LSFs has minimum distance of LSF_GAP */

	  reorder_lsf (lsf1_q, LSF_GAP, M);
	  reorder_lsf (lsf2_q, LSF_GAP, M);

	  /* Update LSF history with quantized LSFs
	     when hangover period is active */

	    
	  if ((txdtx_ctrl & TX_HANGOVER_ACTIVE) != 0)
	    {
	      update_lsf_history (lsf1_q, lsf2_q, lsf_old_tx);
	    }
	  /*  convert LSFs to the cosine domain */

	  lsf_lsp (lsf1_q, &lsp1_q[0], M);
	  lsf_lsp (lsf2_q, &lsp2_q[0], M);
	}
      else
	{
	  for (i = 0; i < M; i++)
	    {
	      past_r2_q[i] = 0;                            
	    }
	}
    }
};

