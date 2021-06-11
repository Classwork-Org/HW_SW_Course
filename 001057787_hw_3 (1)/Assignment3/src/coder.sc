#include "cnst.sh"
#include "typedef.sh"



/* public declaration */
#include "EFR_Coder_public.sc"

import "pre_process";
import "cod_12k2";
import "post_process";
#ifndef USE_BIT_PORTS
import "i_receiver";
import "i_sender";
#endif



behavior Coder (  
#ifdef EXTERNAL_CONTROL
        in    Flag run_flag,
        i_receive  run_start,
#endif                           
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
 	i_Word_receiver speech_samples,
   	i_Flag_receiver dtx_mode,
	i_Word_sender   serial,
	i_Word_sender   txdtx_ctrl
# else                  
 	i_receiver speech_samples,
   	i_receiver dtx_mode,
	i_sender   serial,
	i_sender   txdtx_ctrl
# endif                  
#else
        in  bit[SAMPLE_WIDTH-1:0] speech_samples[L_FRAME],
        in  Flag dtx_mode,
        in  event new_frame,
        out unsigned bit[BITS_PER_FRAME-1:0] serial,
	out DTXctrl txdtx_ctrl,
	out event serialbits_ready
#endif
		)
  implements Ireset
{
#ifdef PIPED_CODER
  piped Word16 speech_frame[L_FRAME];
  piped Word16 prm[PRM_SIZE];          /* Analysis parameters.            */

  piped DTXctrl txdtx_ctrl_val;
  
  piped Flag reset_flag_1;
  piped piped Flag reset_flag_2;
#else  
  Word16 speech_frame[L_FRAME];
  Word16 prm[PRM_SIZE];          /* Analysis parameters.            */

  DTXctrl txdtx_ctrl_val;
  
  Flag reset_flag_1;
  Flag reset_flag_2;
#endif  
  
  Word16 syn[L_FRAME];              /* Buffer for synthesis speech (debug)   */

#ifndef USE_BIT_PORTS
  Flag local_dtx_mode;
# ifdef EXTERNAL_CONTROL  
  Pre_Process pre_process(run_flag, run_start,
                          speech_samples, dtx_mode, speech_frame, reset_flag_1, 
			  reset_flag_2, local_dtx_mode);
# else
  Pre_Process pre_process(speech_samples, dtx_mode, speech_frame, reset_flag_1, 
			  reset_flag_2, local_dtx_mode);
# endif                          
  Coder_12k2 coder_12k2(speech_frame, prm, syn, local_dtx_mode, txdtx_ctrl_val,
			reset_flag_1);
  Post_Process post_process(prm, txdtx_ctrl_val, reset_flag_2, serial,
			    txdtx_ctrl);
#else
# ifdef EXTERNAL_CONTROL
  Pre_Process pre_process(run_flag, run_start,
                          speech_samples, speech_frame, reset_flag_1, 
			  reset_flag_2, new_frame);
# else  
  Pre_Process pre_process(speech_samples, speech_frame, reset_flag_1, 
			  reset_flag_2, new_frame);
# endif  
  Coder_12k2 coder_12k2(speech_frame, prm, syn, dtx_mode, txdtx_ctrl_val,
			reset_flag_1);
  Post_Process post_process(prm, txdtx_ctrl_val, reset_flag_2, serial,
			    txdtx_ctrl, serialbits_ready);
#endif

  void reset(void)
    {
      pre_process.reset();
      coder_12k2.reset();
      post_process.reset();
    }
  


  void main(void)
  {
#ifdef PIPED_CODER
    pipe
    {
      /* filter + downscaling      */
      pre_process.main();

      /* Find speech parameters    */
      coder_12k2.main();

      /* insert comfort noise and convert parameters to serial bits */
      post_process.main();
    }
#else
    fsm
    {
      /* filter + downscaling      */
      pre_process:

      /* Find speech parameters    */
      coder_12k2:

      /* insert comfort noise and convert parameters to serial bits */
      post_process: {
        goto pre_process;
      }
    }
#endif

  }
};
