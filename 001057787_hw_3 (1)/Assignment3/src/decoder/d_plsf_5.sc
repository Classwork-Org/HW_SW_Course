/*************************************************************************
 *
 *  FUNCTION:   D_plsf_5()
 *
 *  PURPOSE: Decodes the 2 sets of LSP parameters in a frame using the
 *           received quantization indices.
 *
 *  DESCRIPTION:
 *           The two sets of LSFs are quantized using split by 5 matrix
 *           quantization (split-MQ) with 1st order MA prediction.
 *
 *   See "q_plsf_5.c" for more details about the quantization procedure
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";
import "basic_func";

import "reset";

import "q_plsf_5";         /* Codebooks of LSF prediction residual */


/* M  ->order of linear prediction filter                      */
/* LSF_GAP  -> Minimum distance between LSF after quantization */
/*             50 Hz = 205                                     */
/* PRED_FAC -> Prediction factor = 0.65                        */
/* ALPHA    ->  0.9                                            */
/* ONE_ALPHA-> (1.0-ALPHA)                                     */

#define M         10
#define LSF_GAP   205
#define PRED_FAC  21299
#define ALPHA     31128
#define ONE_ALPHA 1639


behavior D_plsf_5 (
    Word16 *indice,       /* input : quantization indices of 5 submatrices */
    out Word16 lsp1_o[M],    /* output: quantized 1st LSP vector              */
    out Word16 lsp2_o[M],    /* output: quantized 2nd LSP vector              */
    in  Word16 bfi,          /* input : bad frame indicator (set to 1 if a bad
                                     frame is received)                    */
    in  Word16 rxdtx_ctrl,   /* input : RX DTX control word                   */
    in  Word16 rx_dtx_state  /* input : state of the comfort noise insertion
                                     period                                */
)
  implements Ireset
{
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
    Word16 i, j;
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
   *   FUNCTION NAME:  interpolate_CN_lsf
   *
   *   PURPOSE: Interpolate comfort noise LSF parameter vector over the comfort
   *            noise update period.
   *
   *   INPUTS:      lsf_old_CN[0..9]
   *                              The older LSF parameter vector of the
   *                              interpolation (the endpoint the interpolation
   *                              is started from)
   *                lsf_new_CN[0..9]
   *                              The newer LSF parameter vector of the
   *                              interpolation (the endpoint the interpolation
   *                              is ended to)
   *                rx_dtx_state  State of the comfort noise insertion period
   *
   *   OUTPUTS:     lsf_interp_CN[0..9]
   *                              Interpolated LSF parameter vector
   *
   *   RETURN VALUE: none
   *
   *************************************************************************/

  void interpolate_CN_lsf (
    Word16 lsf_old_CN[M],
    Word16 lsf_new_CN[M],
    Word16 lsf_interp_CN[M],
    Word16 rx_dtx_state
  )
  {
    Word16 i;

    for (i = 0; i < M; i++)
    {
        lsf_interp_CN[i] = interpolate_CN_param (lsf_old_CN[i],
                                                 lsf_new_CN[i],
                                                 rx_dtx_state);  
    }

    return;
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
    Word16 i, j, temp;

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
   *  FUNCTION:  Reorder_lsf()
   *
   *  PURPOSE: To make sure that the LSFs are properly ordered and to keep a
   *           certain minimum distance between adjacent LSFs.                               *
   *           The LSFs are in the frequency range 0-0.5 and represented in Q15
   *
   *************************************************************************/

  void Reorder_lsf (
    Word16 *lsf,        /* (i/o)     : vector of LSFs   (range: 0<=val<=0.5) */
    Word16 min_dist,    /* (i)       : minimum required distance             */
    Word16 n            /* (i)       : LPC order                             */
  )
  {
    Word16 i;
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
  
  void Lsf_lsp (
    Word16 lsf[],       /* (i) : lsf[m] normalized (range: 0.0<=val<=0.5) */
    Word16 lsp[],       /* (o) : lsp[m] (range: -1<=val<1)                */
    Word16 m            /* (i) : LPC order                                */
  )
  {
    Word16 i, ind, offset;
    Word32 L_tmp;

    for (i = 0; i < m; i++)
    {
        ind = shr (lsf[i], 8);      /* ind    = b8-b15 of lsf[i] */
        offset = lsf[i] & 0x00ff;   /* offset = b0-b7  of lsf[i] */

        /* lsp[i] = table[ind]+ ((table[ind+1]-table[ind])*offset) / 256 */

        L_tmp = L_mult (sub (table[ind + 1], table[ind]), offset);
        lsp[i] = add (table[ind], extract_l (L_shr (L_tmp, 9)));
                                     
    }
    return;
  }
  
  /* Past quantized prediction error */

  Word16 past_r2_q[M];
  
  /* Past dequantized lsfs */

  Word16 past_lsf_q[M];

  /* Reference LSF parameter vector (comfort noise) */

  Word16 lsf_p_CN[M];

  /*  LSF memories for comfort noise interpolation */

  Word16 lsf_old_CN[M], lsf_new_CN[M];

  /* LSF parameter buffer */

  Word16 lsf_old_rx[DTX_HANGOVER][M];

  
  void init(void)
  {
    Word16 i;

    for (i = 0; i < DTX_HANGOVER; i++)
    {
        lsf_old_rx[i][0] = 1384;
        lsf_old_rx[i][1] = 2077;
        lsf_old_rx[i][2] = 3420;
        lsf_old_rx[i][3] = 5108;
        lsf_old_rx[i][4] = 6742;
        lsf_old_rx[i][5] = 8122;
        lsf_old_rx[i][6] = 9863;
        lsf_old_rx[i][7] = 11092;
        lsf_old_rx[i][8] = 12714;
        lsf_old_rx[i][9] = 13701;
    }

    for (i = 0; i < M; i++)
    {
        past_r2_q[i] = 0;               /* Past quantized prediction error */
        past_lsf_q[i] = mean_lsf[i];    /* Past dequantized lsfs */
        lsf_p_CN[i] = mean_lsf[i];      /* CNI */
        lsf_new_CN[i] = mean_lsf[i];    /* CNI */
        lsf_old_CN[i] = mean_lsf[i];    /* CNI */
    }
  }
  
  void reset(void)
  {
    init();
  }

  
  void main(void)
  {  
    Word16 i;
    const Word16 *p_dico;
    Word16 temp, sign;
    Word16 lsf1_r[M], lsf2_r[M];
    Word16 lsf1_q[M], lsf2_q[M];
    Word16 lsp1_q[M], lsp2_q[M];


    /* Update comfort noise LSF quantizer memory */
    if ((rxdtx_ctrl & RX_UPD_SID_QUANT_MEM) != 0)
    {
        update_lsf_p_CN (lsf_old_rx, lsf_p_CN);
    }
    /* Handle cases of comfort noise LSF decoding in which past
    valid SID frames are repeated */

    if (((rxdtx_ctrl & RX_NO_TRANSMISSION) != 0)
        || ((rxdtx_ctrl & RX_INVALID_SID_FRAME) != 0)
        || ((rxdtx_ctrl & RX_LOST_SID_FRAME) != 0))
    {

        if ((rxdtx_ctrl & RX_NO_TRANSMISSION) != 0)
        {
            /* DTX active: no transmission. Interpolate LSF values in memory */
            interpolate_CN_lsf (lsf_old_CN, lsf_new_CN, lsf2_q, rx_dtx_state);
        }
        else
        {                       /* Invalid or lost SID frame: use LSFs
                                   from last good SID frame */
            for (i = 0; i < M; i++)
            {
                lsf_old_CN[i] = lsf_new_CN[i];   
                lsf2_q[i] = lsf_new_CN[i];       
                past_r2_q[i] = 0;                
            }
        }

        for (i = 0; i < M; i++)
        {
            past_lsf_q[i] = lsf2_q[i];
        }

        /*  convert LSFs to the cosine domain */
        Lsf_lsp (lsf2_q, lsp2_q, M);

        return;
    }

    if (bfi != 0)                               /* if bad frame */
    {
        /* use the past LSFs slightly shifted towards their mean */

        for (i = 0; i < M; i++)
        {
            /* lsfi_q[i] = ALPHA*past_lsf_q[i] + ONE_ALPHA*mean_lsf[i]; */

            lsf1_q[i] = add (mult (past_lsf_q[i], ALPHA),
                             mult (mean_lsf[i], ONE_ALPHA));

            lsf2_q[i] = lsf1_q[i];
        }

        /* estimate past quantized residual to be used in next frame */

        for (i = 0; i < M; i++)
        {
            /* temp  = mean_lsf[i] +  past_r2_q[i] * PRED_FAC; */

            temp = add (mean_lsf[i], mult (past_r2_q[i], PRED_FAC));

            past_r2_q[i] = sub (lsf2_q[i], temp);
        }
    }
    else
        /* if good LSFs received */
    {
        /* decode prediction residuals from 5 received indices */

        p_dico = &dico1_lsf[shl (indice[0], 2)];
        lsf1_r[0] = *p_dico++;                   
        lsf1_r[1] = *p_dico++;                   
        lsf2_r[0] = *p_dico++;                   
        lsf2_r[1] = *p_dico++;                   

        p_dico = &dico2_lsf[shl (indice[1], 2)];
        lsf1_r[2] = *p_dico++;                   
        lsf1_r[3] = *p_dico++;                   
        lsf2_r[2] = *p_dico++;                   
        lsf2_r[3] = *p_dico++;                   

        sign = indice[2] & 1;                  
        i = shr (indice[2], 1);
        p_dico = &dico3_lsf[shl (i, 2)];         

        if (sign == 0)
        {
            lsf1_r[4] = *p_dico++;               
            lsf1_r[5] = *p_dico++;               
            lsf2_r[4] = *p_dico++;               
            lsf2_r[5] = *p_dico++;               
        }
        else
        {
            lsf1_r[4] = negate (*p_dico++);      
            lsf1_r[5] = negate (*p_dico++);      
            lsf2_r[4] = negate (*p_dico++);      
            lsf2_r[5] = negate (*p_dico++);      
        }

        p_dico = &dico4_lsf[shl (indice[3], 2)]; 
        lsf1_r[6] = *p_dico++;                   
        lsf1_r[7] = *p_dico++;                   
        lsf2_r[6] = *p_dico++;                   
        lsf2_r[7] = *p_dico++;                   

        p_dico = &dico5_lsf[shl (indice[4], 2)]; 
        lsf1_r[8] = *p_dico++;                   
        lsf1_r[9] = *p_dico++;                   
        lsf2_r[8] = *p_dico++;                   
        lsf2_r[9] = *p_dico++;                   

        /* Compute quantized LSFs and update the past quantized residual */
        /* Use lsf_p_CN as predicted LSF vector in case of no speech
           activity */

        if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
        {
            for (i = 0; i < M; i++)
            {
                temp = add (mean_lsf[i], mult (past_r2_q[i], PRED_FAC));
                lsf1_q[i] = add (lsf1_r[i], temp);
                                                 
                lsf2_q[i] = add (lsf2_r[i], temp);
                                                 
                past_r2_q[i] = lsf2_r[i];        
            }
        }
        else
        {                       /* Valid SID frame */
            for (i = 0; i < M; i++)
            {
                lsf2_q[i] = add (lsf2_r[i], lsf_p_CN[i]);
                                                 

                /* Use the dequantized values of lsf2 also for lsf1 */
                lsf1_q[i] = lsf2_q[i];           

                past_r2_q[i] = 0;                
            }
        }
    }

    /* verification that LSFs have minimum distance of LSF_GAP Hz */

    Reorder_lsf (lsf1_q, LSF_GAP, M);
    Reorder_lsf (lsf2_q, LSF_GAP, M);

    if ((rxdtx_ctrl & RX_FIRST_SID_UPDATE) != 0)
    {
        for (i = 0; i < M; i++)
        {
            lsf_new_CN[i] = lsf2_q[i];           
        }
    }
    if ((rxdtx_ctrl & RX_CONT_SID_UPDATE) != 0)
    {
        for (i = 0; i < M; i++)
        {
            lsf_old_CN[i] = lsf_new_CN[i];       
            lsf_new_CN[i] = lsf2_q[i];           
        }
    }
    if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
    {
        /* Update lsf history with quantized LSFs
           when speech activity is present. If the current frame is
           a bad one, update with most recent good comfort noise LSFs */

        if (bfi==0)
        {
            update_lsf_history (lsf1_q, lsf2_q, lsf_old_rx);
        }
        else
        {
            update_lsf_history (lsf_new_CN, lsf_new_CN, lsf_old_rx);
        }

        for (i = 0; i < M; i++)
        {
            lsf_old_CN[i] = lsf2_q[i];           
        }
    }
    else
    {
        interpolate_CN_lsf (lsf_old_CN, lsf_new_CN, lsf2_q, rx_dtx_state);
    }

    for (i = 0; i < M; i++)
    {
        past_lsf_q[i] = lsf2_q[i];               
    }

    /*  convert LSFs to the cosine domain */

    Lsf_lsp (lsf1_q, lsp1_q, M);
    Lsf_lsp (lsf2_q, lsp2_q, M);

    lsp1_o = lsp1_q;
    lsp2_o = lsp2_q;
  }
};
