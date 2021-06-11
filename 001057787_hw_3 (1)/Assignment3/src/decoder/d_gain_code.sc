/*************************************************************************
 *
 *  FILE NAME:   d_gain_code.sc
 *
 *  FUNCTIONS DEFINED IN THIS FILE:
 *
 *        d_gain_code()
 *
 * MA prediction is performed on the innovation energy
 * ( in dB/(20*log10(2)) ) with mean removed.
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";
import "basic_func";

import "gains_tb";

import "reset";


/*************************************************************************
 *
 *  FUNCTION:  d_gain_code
 *
 *  PURPOSE:  decode the fixed codebook gain using the received index.
 *
 *  DESCRIPTION:
 *      The received index gives the gain correction factor gamma.
 *      The quantized gain is given by   g_q = g0 * gamma
 *      where g0 is the predicted gain.
 *      To find g0, 4th order MA prediction is applied to the mean-removed
 *      innovation energy in dB.
 *      In case of frame erasure, downscaled past gain is used.
 *
 *************************************************************************/

/* average innovation energy.                             */
/* MEAN_ENER = 36.0/constant, constant = 20*Log10(2)      */
#define MEAN_ENER  783741L      /* 36/(20*log10(2))       */

behavior D_gain_code (
    in  Word16 index,      /* input : received quantization index */
    in  Word16 code[L_SUBFR],     /* input : innovation codevector       */
    out Word16 gcode,      /* output: decoded innovation gain     */
    in  Word16 bfi,        /* input : bad frame indicator         */
    in  Word16 state,
    in  Word16 prev_bf,
    in  Word16 rxdtx_ctrl,
    in  Word16 i_subfr,
    in  Word16 rx_dtx_state)
  implements Ireset
{
  Word16 buf_p_rx;         /* Circular buffer pointer for gain code 
                                   history update in rx                    */
  
  /*************************************************************************
   *
   *   FUNCTION NAME: update_gcode0_CN
   *
   *   PURPOSE: Update the reference fixed codebook gain parameter value.
   *            The reference value is computed by averaging the quantized
   *            fixed codebook gain parameter values which exist in the
   *            fixed codebook gain parameter history.
   *
   *   INPUTS:      gain_code_old[0..4*DTX_HANGOVER-1]
   *                              fixed codebook gain parameter history
   *
   *   OUTPUTS:     none
   *
   *   RETURN VALUE: Computed reference fixed codebook gain
   *
   *************************************************************************/
  
  Word16 update_gcode0_CN (
    Word16 gain_code_old[4 * DTX_HANGOVER]
   )
  {
    Word16 i, j;
    Word32 L_temp, L_ret;

    L_ret = 0L;                                         
    for (i = 0; i < DTX_HANGOVER; i++)
    {
        L_temp = L_mult (0x1fff, gain_code_old[4 * i]);
        for (j = 1; j < 4; j++)
        {
            L_temp = L_mac (L_temp, 0x1fff, gain_code_old[4 * i + j]);
        }
        L_ret = L_mac (L_ret, INV_DTX_HANGOVER, extract_h (L_temp));
    }

    return extract_h (L_ret);
  }
  
  /*************************************************************************
   *
   *   FUNCTION NAME: update_gain_code_history_rx
   *
   *   PURPOSE: Update the fixed codebook gain parameter history of the
   *            decoder. The fixed codebook gain parameters kept in the buffer
   *            are used later for computing the reference fixed codebook
   *            gain parameter value.
   *
   *   INPUTS:      new_gain_code   New fixed codebook gain value
   *
   *                gain_code_old_tx[0..4*DTX_HANGOVER-1]
   *                                Old fixed codebook gain history of decoder
   *
   *   OUTPUTS:     gain_code_old_tx[0..4*DTX_HANGOVER-1]
   *                                Updated fixed codebk gain history of decoder
   *
   *   RETURN VALUE: none
   *
   *************************************************************************/

  void update_gain_code_history_rx (
    Word16 new_gain_code,
    Word16 gain_code_old_rx[4 * DTX_HANGOVER]
    )
  {

    /* Circular buffer */
    gain_code_old_rx[buf_p_rx] = new_gain_code;         

    if (sub (buf_p_rx, (4 * DTX_HANGOVER - 1)) == 0)
    {
        buf_p_rx = 0;                                   
    }
    else
    {
        buf_p_rx = add (buf_p_rx, 1);
    }

    return;
  }
  
  Word16 gain_code_old_rx[4 * DTX_HANGOVER];
  
  /* Variables used by d_gain_code: */
  Word16 gbuf[5], past_gain_code, prev_gc;
  
  /* Static variables for CNI (used by d_gain_code) */
  Word16 gcode0_CN, gain_code_old_CN, gain_code_new_CN, gain_code_muting_CN;

  /* Memories of gain dequantization: */

  /* past quantized energies.      */
  /* initialized to -14.0/constant, constant = 20*Log10(2) */

  Word16 past_qua_en[4];

  /* MA prediction coeff   */
  Word16 pred[4];
  
  void init(void)
  {
    int i;
    
    buf_p_rx = 0;
    
    for (i = 0; i < 4 * DTX_HANGOVER; i++)
    {
        gain_code_old_rx[i] = 0;
    }
    
    for (i = 0; i < 5; i++)
    {
        gbuf[i] = 1;            /* Error concealment */
    }

    past_gain_code = 0;         /* Error concealment */
    prev_gc = 1;                /* Error concealment */
    gcode0_CN = 0;              /* CNI */
    gain_code_old_CN = 0;       /* CNI */
    gain_code_new_CN = 0;       /* CNI */
    gain_code_muting_CN = 0;    /* CNI */

    for (i = 0; i < 4; i++)
    {
        past_qua_en[i] = -2381; /* past quantized energies */
    }

    pred[0] = 44;               /* MA prediction coeff */
    pred[1] = 37;               /* MA prediction coeff */
    pred[2] = 22;               /* MA prediction coeff */
    pred[3] = 12;               /* MA prediction coeff */
  }
  
  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    static const Word16 cdown[7] =
    {
        32767, 32112, 32112, 32112,
        32112, 32112, 22937
    };

    Word16 i, tmp;
    Word16 gcode0, exp, frac, av_pred_en;
    Word32 ener, ener_code;
    Word16 gain_code;

    if (((rxdtx_ctrl & RX_UPD_SID_QUANT_MEM) != 0) && (i_subfr == 0))
    {
        gcode0_CN = update_gcode0_CN (gain_code_old_rx);        
        gcode0_CN = shl (gcode0_CN, 4);
    }

    /* Handle cases of comfort noise fixed codebook gain decoding in
       which past valid SID frames are repeated */

    if (((rxdtx_ctrl & RX_NO_TRANSMISSION) != 0)
        || ((rxdtx_ctrl & RX_INVALID_SID_FRAME) != 0)
        || ((rxdtx_ctrl & RX_LOST_SID_FRAME) != 0))
    {

        if ((rxdtx_ctrl & RX_NO_TRANSMISSION) != 0)
        {
            /* DTX active: no transmission. Interpolate gain values
            in memory */
            if (i_subfr == 0)
            {
                gain_code = interpolate_CN_param (gain_code_old_CN,
                                            gain_code_new_CN, rx_dtx_state);
                                                                
            }
            else
            {
                gain_code = prev_gc;                           
            }
        }
        else
        {                       /* Invalid or lost SID frame:
            use gain values from last good SID frame */
            gain_code_old_CN = gain_code_new_CN;                
            gain_code = gain_code_new_CN;                      

            /* reset table of past quantized energies */
            for (i = 0; i < 4; i++)
            {
                past_qua_en[i] = -2381;                         
            }
        }

        if ((rxdtx_ctrl & RX_DTX_MUTING) != 0)
        {
            /* attenuate the gain value by 0.75 dB in each subframe */
            /* (total of 3 dB per frame) */
            gain_code_muting_CN = mult (gain_code_muting_CN, 30057);
            gain_code = gain_code_muting_CN;                   
        }
        else
        {
            /* Prepare for DTX muting by storing last good gain value */
            gain_code_muting_CN = gain_code_new_CN;             
        }

        past_gain_code = gain_code;                            

        for (i = 1; i < 5; i++)
        {
            gbuf[i - 1] = gbuf[i];                              
        }

        gbuf[4] = past_gain_code;                               
        prev_gc = past_gain_code;                               

        return;
    }

    /*----------------- Test erasure ---------------*/

    if (bfi != 0)
    {
        tmp = gmed5 (gbuf);                                     
        if (sub (tmp, past_gain_code) < 0)
        {
            past_gain_code = tmp;                               
        }
        past_gain_code = mult (past_gain_code, cdown[state]);
        gain_code = past_gain_code;                            

        av_pred_en = 0;                                         
        for (i = 0; i < 4; i++)
        {
            av_pred_en = add (av_pred_en, past_qua_en[i]);
        }

        /* av_pred_en = 0.25*av_pred_en - 4/(20Log10(2)) */
        av_pred_en = mult (av_pred_en, 8192);   /*  *= 0.25  */

        /* if (av_pred_en < -14/(20Log10(2))) av_pred_en = .. */
        if (sub (av_pred_en, -2381) < 0)
        {
            av_pred_en = -2381;                                 
        }
        for (i = 3; i > 0; i--)
        {
            past_qua_en[i] = past_qua_en[i - 1];                
        }
        past_qua_en[0] = av_pred_en;                            
        for (i = 1; i < 5; i++)
        {
            gbuf[i - 1] = gbuf[i];                              
        }
        gbuf[4] = past_gain_code;                               

        /* Use the most recent comfort noise fixed codebook gain value
           for updating the fixed codebook gain history */
        if (gain_code_new_CN == 0)
        {
            tmp = prev_gc;                                     
        }
        else
        {
            tmp = gain_code_new_CN;
        }

        update_gain_code_history_rx (tmp, gain_code_old_rx);

        if (sub (i_subfr, (3 * L_SUBFR)) == 0)
        {
            gain_code_old_CN = gain_code;                      
        }
        return;
    }

    if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
    {

        /*-------------- Decode codebook gain ---------------*/

        /*-------------------------------------------------------------------*
         *  energy of code:                                                   *
         *  ~~~~~~~~~~~~~~~                                                   *
         *  ener_code = 10 * Log10(energy/lcode) / constant                   *
         *            = 1/2 * Log2(energy/lcode)                              *
         *                                           constant = 20*Log10(2)   *
         *-------------------------------------------------------------------*/

        /* ener_code = log10(ener_code/lcode) / (20*log10(2)) */
        ener_code = 0;                                          
        for (i = 0; i < L_SUBFR; i++)
        {
            ener_code = L_mac (ener_code, code[i], code[i]);
        }
        /* ener_code = ener_code / lcode */
        ener_code = L_mult (round (ener_code), 26214);

        /* ener_code = 1/2 * Log2(ener_code) */
        Log2 (ener_code, &exp, &frac);
        ener_code = L_Comp (sub (exp, 30), frac);

        /* predicted energy */

        ener = MEAN_ENER;                                       
        for (i = 0; i < 4; i++)
        {
            ener = L_mac (ener, past_qua_en[i], pred[i]);
        }

        /*-------------------------------------------------------------------*
         *  predicted codebook gain                                           *
         *  ~~~~~~~~~~~~~~~~~~~~~~~                                           *
         *  gcode0     = Pow10( (ener*constant - ener_code*constant) / 20 )   *
         *             = Pow2(ener-ener_code)                                 *
         *                                           constant = 20*Log10(2)   *
         *-------------------------------------------------------------------*/

        ener = L_shr (L_sub (ener, ener_code), 1);
        L_Extract (ener, &exp, &frac);

        gcode0 = extract_l (Pow2 (exp, frac));  /* predicted gain */

        gcode0 = shl (gcode0, 4);

        gain_code = mult (qua_gain_code[index], gcode0);       

        if (prev_bf != 0)
        {
            if (sub (gain_code, prev_gc) > 0)
            {
                gain_code = prev_gc;      
            }
        }
        /*-------------------------------------------------------------------*
         *  update table of past quantized energies                           *
         *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~                           *
         *  past_qua_en      = 20 * Log10(qua_gain_code) / constant           *
         *                   = Log2(qua_gain_code)                            *
         *                                           constant = 20*Log10(2)   *
         *-------------------------------------------------------------------*/

        for (i = 3; i > 0; i--)
        {
            past_qua_en[i] = past_qua_en[i - 1];                
        }
        Log2 (L_deposit_l (qua_gain_code[index]), &exp, &frac);

        past_qua_en[0] = shr (frac, 5);                         
        past_qua_en[0] = add (past_qua_en[0], shl (sub (exp, 11), 10));
         

        update_gain_code_history_rx (gain_code, gain_code_old_rx);

        if (sub (i_subfr, (3 * L_SUBFR)) == 0)
        {
            gain_code_old_CN = gain_code;                      
        }
    }
    else
    {
        if (((rxdtx_ctrl & RX_FIRST_SID_UPDATE) != 0) && (i_subfr == 0))
        {
            gain_code_new_CN = mult (gcode0_CN, qua_gain_code[index]);

            /*---------------------------------------------------------------*
             *  reset table of past quantized energies                        *
             *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~                        *
             *---------------------------------------------------------------*/

            for (i = 0; i < 4; i++)
            {
                past_qua_en[i] = -2381;                         
            }
        }
        if (((rxdtx_ctrl & RX_CONT_SID_UPDATE) != 0) && (i_subfr == 0))
        {
            gain_code_old_CN = gain_code_new_CN;                
            gain_code_new_CN = mult (gcode0_CN, qua_gain_code[index]);
                                                                
        }
        if (i_subfr == 0)
        {
            gain_code = interpolate_CN_param (gain_code_old_CN,
                                               gain_code_new_CN,
                                               rx_dtx_state);   
        }
        else
        {
            gain_code = prev_gc;                               
        }
    }

    past_gain_code = gain_code;                                

    for (i = 1; i < 5; i++)
    {
        gbuf[i - 1] = gbuf[i];                                  
    }
    gbuf[4] = past_gain_code;                                   
    prev_gc = past_gain_code;                                   

    gcode = gain_code;
    
    return;
  }
};
