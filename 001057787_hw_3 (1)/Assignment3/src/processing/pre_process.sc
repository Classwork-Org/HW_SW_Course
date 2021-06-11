#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "homing_test";
import "filter_and_scale";
#ifndef USE_BIT_PORTS
import "channels";
#endif

#ifdef DEBUGOUTPUT
  import "std_includes";         /* testing homing frame */
#endif


behavior Pre_Process_Seq1 (
#ifdef EXTERNAL_CONTROL
               in    Flag run_flag,
               i_receive  run_start,
#endif                           
               inout Flag reset_next,
               inout Flag reset_flag,
               out   Flag reset_flag_1,
#ifndef USE_BIT_PORTS
	       out   Flag reset_flag_2,
# ifdef TYPED_CHANNELS
               i_Word_receiver speech_frame,
               i_Flag_receiver dtx_mode,
# else                           
	       i_receiver speech_frame,
	       i_receiver dtx_mode,
# endif                           
	       Word16 local_speech_frame[L_FRAME],
	       Flag local_dtx_mode
#else
	       out   Flag reset_flag_2,
               in   event new_frame
#endif 
)
{
  void main(void)
  {
#ifndef USE_BIT_PORTS
    static bool First=true;
# ifdef TYPED_CHANNELS
    int i;
    Word16 sample;
# endif    
#endif

    reset_flag_1 = reset_flag = reset_next;
    reset_flag_2 = reset_flag;

    if (reset_next == 1)    /* last frame was a homing frame */
    {
#ifdef DEBUGOUTPUT
      /* testing homing frame */
      printf(" ****** homing frame detected -> reset! ******");
#endif

      reset_next = 0;
    }
    
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
    if (First)
    {
	dtx_mode.receive(&local_dtx_mode);
	First = false;
    }
# else    
    if (First)
    {
	dtx_mode.receive(&local_dtx_mode,sizeof(local_dtx_mode));
	First = false;
    }
# endif
#endif
    
#ifdef EXTERNAL_CONTROL
    if (run_flag != 1)
      run_start.receive();
#endif 
    
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS    
    for(i = 0; i < L_FRAME; i++) {
      speech_frame.receive(&sample);
      local_speech_frame[i] = sample;
    }    
# else    
    speech_frame.receive(local_speech_frame,sizeof(local_speech_frame));
# endif    
#else
    wait(new_frame);
#endif
  }
};


behavior Pre_Process (
#ifdef EXTERNAL_CONTROL
               in    Flag run_flag,
               i_receive  run_start,
#endif                           
#ifndef USE_BIT_PORTS
	      /*in Word16 speech_frame[L_FRAME],*/
# ifdef TYPED_CHANNELS
              i_Word_receiver speech_frame,
              i_Flag_receiver dtx_mode,
# else                      
	      i_receiver speech_frame,
              i_receiver dtx_mode,	
# endif                      
	      out Word16 speech_frame_filtered[L_FRAME],
	      out Flag reset_flag_1,
	      out Flag reset_flag_2,	
	      Flag local_dtx_mode     
#else
              in bit[SAMPLE_WIDTH-1:0] speech_frame[L_FRAME],
	      out Word16 speech_frame_filtered[L_FRAME],
	      out Flag reset_flag_1,
 	      out Flag reset_flag_2,
	      in event new_frame
#endif      
                      )
  implements Ireset

{
  Flag reset_flag;
  Flag reset_next = 0;
#ifndef USE_BIT_PORTS
  Word16 local_speech_frame[L_FRAME];
#endif

  
  Pre_Process_Seq1 seq1(
#ifdef EXTERNAL_CONTROL
        run_flag, run_start,
#endif                        
        reset_next, reset_flag, reset_flag_1,reset_flag_2, 
#ifndef USE_BIT_PORTS
	speech_frame,dtx_mode,local_speech_frame,local_dtx_mode);
#else
	new_frame);
#endif

#ifndef USE_BIT_PORTS
  Encoder_Homingframe_Test encoder_homingframe_test(local_speech_frame, reset_next);
  Filter_And_Scale filter_and_scale(reset_flag, local_speech_frame,speech_frame_filtered);
#else
  Encoder_Homingframe_Test encoder_homingframe_test(speech_frame, reset_next);
  Filter_And_Scale filter_and_scale(reset_flag, speech_frame,speech_frame_filtered);
#endif


  void reset(void)
    {
      filter_and_scale.reset();
    }


  void main(void)
    {
      seq1.main();

      encoder_homingframe_test.main();

      filter_and_scale.main();
    }
 
};
