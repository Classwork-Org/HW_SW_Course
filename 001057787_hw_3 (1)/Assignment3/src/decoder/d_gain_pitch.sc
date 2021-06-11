/*************************************************************************
 *
 *  FILE NAME:   d_gain_pitch.sc
 *
 *  FUNCTIONS DEFINED IN THIS FILE:
 *
 *        d_gain_pitch()
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
 *  FUNCTION:   d_gain_pitch
 *
 *  PURPOSE:  decodes the pitch gain using the received index.
 *
 *  DESCRIPTION:
 *       In case of no frame erasure, the gain is obtained from the
 *       quantization table at the given index; otherwise, a downscaled
 *       past gain is used.
 *
 *************************************************************************/

behavior D_gain_pitch ( /* out      : quantized pitch gain           */
    in  Word16 index,     /* in       : index of quantization          */
    in  Word16 bfi,       /* in       : bad frame indicator (good = 0) */
    in  Word16 state,
    in  Word16 prev_bf,
    in  Word16 rxdtx_ctrl,
    out Word16 gain_pitch)
  implements Ireset
{
  /* Variables used by d_gain_pitch: */
  Word16 pbuf[5], past_gain_pit, prev_gp;
  
  void init(void)
  {
    int i;
    
    for (i = 0; i < 5; i++)
    {
        pbuf[i] = 410;          /* Error concealment */
    }

    past_gain_pit = 0;          /* Error concealment */
    prev_gp = 4096;             /* Error concealment */
  }
  
  void reset(void)
  {
    init();
  }
    
  void main(void)
  {
    Word16 gain, tmp, i;
    
    static const Word16 pdown[7] =
    {
        32767, 32112, 32112, 26214,
        9830, 6553, 6553
    };


    if (bfi == 0)
    {
        if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
        {
            gain = shr (qua_gain_pitch[index], 2);              

            if (prev_bf != 0)
            {
                if (sub (gain, prev_gp) > 0)
                {
                    gain = prev_gp;
                }
            }
        }
        else
        {
            gain = 0;                                           
        }
        prev_gp = gain;                                         
    }
    else
    {
        if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
        {
            tmp = gmed5 (pbuf);                                 

            if (sub (tmp, past_gain_pit) < 0)
            {
                past_gain_pit = tmp;                            
            }
            gain = mult (pdown[state], past_gain_pit);
        }
        else
        {
            gain = 0;                                           
        }
    }

    past_gain_pit = gain;                                       

    if (sub (past_gain_pit, 4096) > 0)  /* if (past_gain_pit > 1.0) */
    {
        past_gain_pit = 4096;                                   
    }
    for (i = 1; i < 5; i++)
    {
        pbuf[i - 1] = pbuf[i];                                  
    }

    pbuf[4] = past_gain_pit;                                    

    gain_pitch = gain;
  }
};

