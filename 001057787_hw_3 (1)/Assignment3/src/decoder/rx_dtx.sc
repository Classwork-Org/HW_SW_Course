#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "basic_op";


/*************************************************************************
 *
 *   FUNCTION NAME: rx_dtx
 *
 *   PURPOSE: DTX handler of the speech decoder. Determines when to update
 *            the reference comfort noise parameters (LSF and gain) at the
 *            end of the speech burst. Also classifies the incoming frames
 *            according to SID flag and BFI flag
 *            and determines when the transmission is active during comfort
 *            noise insertion. This function also initializes the pseudo
 *            noise generator shift register.
 *
 *            Operation of the RX DTX handler is based on measuring the
 *            lengths of speech bursts and the lengths of the pauses between
 *            speech bursts to determine when there exists a hangover period
 *            at the end of a speech burst. The idea is to keep in sync with
 *            the TX DTX handler to be able to update the reference comfort
 *            noise parameters at the same time instances.
 *
 *   INPUTS:      *rxdtx_ctrl   Old decoder DTX control word
 *                TAF           Time alignment flag
 *                bfi           Bad frame indicator flag
 *                SID_flag      Silence descriptor flag
 *
 *   OUTPUTS:     *rxdtx_ctrl   Updated decoder DTX control word
 *                rx_dtx_state  Updated state of comfort noise interpolation
 *                              period (global variable)
 *                L_pn_seed_rx  Initialized pseudo noise generator shift
 *                              register (global variable)
 *
 *   RETURN VALUE: none
 *
 *************************************************************************/

behavior Rx_Dtx(
    in  Word16 TAF,
    in  Word16 bfi,
    in  Word16 SID_flag,
        Word16 rxdtx_ctrl,
        Word16 rx_dtx_state,
    out Word32 L_pn_seed_rx)
  implements Ireset
{
  Word16 rxdtx_aver_period;/* Length of hangover period (VAD=0, SP=1) */
  Word16 rxdtx_N_elapsed;  /* Measured time from previous SID frame   */
  Word16 prev_SID_frames_lost; /* Counter for lost SID frames         */
  
  void init(void)
  {
    rxdtx_aver_period = DTX_HANGOVER;
    rxdtx_N_elapsed = 0x7fff;
    prev_SID_frames_lost = 0;
  }

  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    Word16 frame_type;

    /* Frame classification according to bfi-flag and ternary-valued
       SID flag. The frames between SID updates (not actually trans-
       mitted) are also classified here; they will be discarded later
       and provided with "NO TRANSMISSION"-flag */

    if ((sub (SID_flag, 2) == 0) && (bfi == 0))
    {
        frame_type = VALID_SID_FRAME;                    
    }
    else if ((SID_flag == 0) && (bfi == 0))
    {
        frame_type = GOOD_SPEECH_FRAME;                  
    }
    else if ((SID_flag == 0) && (bfi != 0))
    {
        frame_type = UNUSABLE_FRAME;                     
    }
    else
    {
        frame_type = INVALID_SID_FRAME;                  
    }

    /* Update of decoder state */
    /* Previous frame was classified as a speech frame */
    if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
    {
        if (sub (frame_type, VALID_SID_FRAME) == 0)
        {
            rxdtx_ctrl = RX_FIRST_SID_UPDATE;           
        }
        else if (sub (frame_type, INVALID_SID_FRAME) == 0)
        {
            rxdtx_ctrl = RX_FIRST_SID_UPDATE
                        | RX_INVALID_SID_FRAME;
        }
        else if (sub (frame_type, UNUSABLE_FRAME) == 0)
        {
            rxdtx_ctrl = RX_SP_FLAG;                    
        }
        else if (sub (frame_type, GOOD_SPEECH_FRAME) == 0)
        {
            rxdtx_ctrl = RX_SP_FLAG;                    
        }
    }
    else
    {
        if (sub (frame_type, VALID_SID_FRAME) == 0)
        {
            rxdtx_ctrl = RX_CONT_SID_UPDATE;        
        }
        else if (sub (frame_type, INVALID_SID_FRAME) == 0)
        {
            rxdtx_ctrl = RX_CONT_SID_UPDATE
                        | RX_INVALID_SID_FRAME;      
        }
        else if (sub (frame_type, UNUSABLE_FRAME) == 0)
        {
            rxdtx_ctrl = RX_CNI_BFI;                
        }
        else if (sub (frame_type, GOOD_SPEECH_FRAME) == 0)
        {
            /* If the previous frame (during CNI period) was muted,
               raise the RX_PREV_DTX_MUTING flag */
            if ((rxdtx_ctrl & RX_DTX_MUTING) != 0)
            {
                rxdtx_ctrl = RX_SP_FLAG | RX_FIRST_SP_FLAG
                            | RX_PREV_DTX_MUTING;  
                                                  
            }
            else
            {
                rxdtx_ctrl = RX_SP_FLAG | RX_FIRST_SP_FLAG; 
                                                             
            }
        }
    }


    if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
    {
        prev_SID_frames_lost = 0;                       
        rx_dtx_state = CN_INT_PERIOD - 1;                
    }
    else
    {
        /* First SID frame */
        if ((rxdtx_ctrl & RX_FIRST_SID_UPDATE) != 0)
        {
            prev_SID_frames_lost = 0;                   
            rx_dtx_state = CN_INT_PERIOD - 1;            
        }

        /* SID frame detected, but not the first SID */
        if ((rxdtx_ctrl & RX_CONT_SID_UPDATE) != 0)
        {
            prev_SID_frames_lost = 0;                   

            if (sub (frame_type, VALID_SID_FRAME) == 0)
            {
                rx_dtx_state = 0;                       
            }
            else if (sub (frame_type, INVALID_SID_FRAME) == 0)
            {
                if (sub(rx_dtx_state, (CN_INT_PERIOD - 1)) < 0)
                {
                    rx_dtx_state = add(rx_dtx_state, 1); 
                }
            }
        }

        /* Bad frame received in CNI mode */
        if ((rxdtx_ctrl & RX_CNI_BFI) != 0)
        {
            if (sub (rx_dtx_state, (CN_INT_PERIOD - 1)) < 0)
            {
                rx_dtx_state = add (rx_dtx_state, 1);   
            }

            /* If an unusable frame is received during CNI period
               when TAF == 1, the frame is classified as a lost
               SID frame */
            if (sub (TAF, 1) == 0)
            {
                rxdtx_ctrl = rxdtx_ctrl | RX_LOST_SID_FRAME;
                                           
                prev_SID_frames_lost = add (prev_SID_frames_lost, 1);
            }
            else /* No transmission occurred */
            {
                rxdtx_ctrl = rxdtx_ctrl | RX_NO_TRANSMISSION;
                                           
            }

            if (sub (prev_SID_frames_lost, 1) > 0)
            {
                rxdtx_ctrl = rxdtx_ctrl | RX_DTX_MUTING;
                                           
            }
        }
    }

    /* N_elapsed (frames since last SID update) is incremented. If SID
       is updated N_elapsed is cleared later in this function */

    rxdtx_N_elapsed = add (rxdtx_N_elapsed, 1);

    if ((rxdtx_ctrl & RX_SP_FLAG) != 0)
    {
        rxdtx_aver_period = DTX_HANGOVER;                
    }
    else
    {
        if (sub (rxdtx_N_elapsed, DTX_ELAPSED_THRESHOLD) > 0)
        {
            rxdtx_ctrl |= RX_UPD_SID_QUANT_MEM;          
            rxdtx_N_elapsed = 0;                         
            rxdtx_aver_period = 0;                       
            L_pn_seed_rx = PN_INITIAL_SEED;              
        }
        else if (rxdtx_aver_period == 0)
        {
            rxdtx_N_elapsed = 0;                         
        }
        else
        {
            rxdtx_aver_period = sub (rxdtx_aver_period, 1);
        }
    }

    return;
  }
};

