
#include "cnst.sh"
#include "typedef.sh"

import "bits2prm";
import "homing_test";
import "dec_12k2";

#ifndef USE_BIT_PORTS
import "channels";
#endif


/* These constants define the number of consecutive parameters
   that function decoder_homing_frame_test() checks */

#define WHOLE_FRAME 57
#define TO_FIRST_SUBFRAME 18

behavior Pre_Decoder(
#ifdef EXTERNAL_CONTROL
                     in   Flag run_flag,
                     i_receive run_start,
#endif                                                
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS                     
                     i_Word_receiver inparam,
# else
                     i_receiver inparam,
# endif                     
#else                     
                     in  Word16 serial[SERIAL_SIZE+2],
                     in  event  new_serial,
#endif                     
		     in  Flag   reset_flag_old,
		     out Flag   reset_flag,
		     out Word16 prm[PRM_SIZE+1],
		     out Word16 TAF,
		     out Word16 SID_Flag)
{
#ifndef USE_BIT_PORTS
  Word16 serial[SERIAL_SIZE+2];
#endif  
  Word16 *p_prm;
  Word16 param[PRM_SIZE+1];
  
  Bits2prm_12k2 bits2prm(serial, param);
  Decoder_Homingframe_Test d_homing1(p_prm, TO_FIRST_SUBFRAME, reset_flag);
  Decoder_Homingframe_Test d_homing2(p_prm, WHOLE_FRAME, reset_flag);

  void main(void)
  {
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
    Int i;
# endif
#endif
    
#ifdef EXTERNAL_CONTROL
    if (run_flag != 1)
      run_start.receive();
#endif
    
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
    for(i = 0; i < SERIAL_SIZE+2; i++) {
      inparam.receive(&serial[i]);
    }    
# else
    inparam.receive(serial, sizeof(serial));
# endif    
#else    
    wait(new_serial);
#endif    
    
    p_prm = &param[1];

    SID_Flag = serial[245];         /* Receive SID flag */
    TAF      = serial[246];         /* Receive TAF flag */
    
    reset_flag = 0;
    
    bits2prm.main();
    
    if (param[0] == 0)               /* BFI == 0, perform DHF check */
    {
      if (reset_flag_old == 1)    /* Check for second and further
                                           successive DHF (to first subfr.) */
      {
	d_homing1.main();
      }
    }
    
    /* BFI == 0, perform check for first DHF (whole frame) */
    if ((param[0] == 0) && (reset_flag_old == 0))
    {
      d_homing2.main();
    }
    
    prm = param;
  }
};


behavior Post_Decoder(in  Word16 synth[L_FRAME],
		      in  Flag reset_flag,
		          Flag reset_flag_old,
		      out Flag reset_next,
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS                      
                      i_Word_sender outframe
# else
                      i_sender outframe
# endif                      
#else
		      out Word16 speech[L_FRAME],
		      out event speech_ready
#endif                      
                      )
{
  void main(void)
  {
    int i;
    Word16 temp;
#ifndef USE_BIT_PORTS
# ifndef TYPED_CHANNELS
    Word16 speech[L_FRAME];
# endif
#endif    
    
    if ((reset_flag != 0) && (reset_flag_old != 0))
    {
      /* Force the output to be the encoder homing frame pattern */
      for (i = 0; i < L_FRAME; i++)
      {
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS        
        temp = EHF_MASK;
        outframe.send(temp);
# else
	speech[i] = EHF_MASK;
# endif        
#else        
	speech[i] = EHF_MASK;
#endif        
      }
    }
    else
    {
      for (i = 0; i < L_FRAME; i++) 
	/* Upscale the 15 bit linear PCM to 16 bits,
	 then truncate to 13 bits */
      {
	temp = shl (synth[i], 1);
        temp = temp & 0xfff8;
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS        
        outframe.send(temp);
# else
	speech[i] = temp;
# endif        
#else        
	speech[i] = temp;
#endif        
      }
    }

#ifdef USE_BIT_PORTS    
    /* send result */
    notify speech_ready;
#else
# ifndef TYPED_CHANNELS
    outframe.send(speech, sizeof(speech));
# endif
#endif    
        
    if (reset_flag != 0)
    {
      /* Bring the decoder and receive DTX to the home state */
      reset_next = 1;
    }
    reset_flag_old = reset_flag;
  }
};


/*-----------------------------------------------------------------*
 *            Main decoder routine                                 *
 *-----------------------------------------------------------------*/

behavior Decoder(
#ifdef EXTERNAL_CONTROL
                 in   Flag run_flag,
                 i_receive run_start,
#endif                                                
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS                 
                 i_Word_receiver inparam, 
                 i_Word_sender outframe
# else 
                 i_receiver inparam,
                 i_sender outframe
# endif                 
#else                 
                 in  Word16 serial[SERIAL_SIZE+2],
		 in  event new_serial,		 
		 out Word16 speech[L_FRAME],
		 out event speech_ready
#endif                 
                 )
  implements Ireset
{
  Flag reset_flag, reset_flag_old;
  Flag reset_next = 0;
  
  Word16 TAF, SID_Flag;
  
  Word16 synth[L_FRAME];
  Word16 prm[PRM_SIZE+1];
  
#ifndef USE_BIT_PORTS
# ifdef EXTERNAL_CONTROL
  Pre_Decoder pre_process(run_flag, run_start,
                          inparam, reset_flag_old, reset_flag, prm,
                          TAF, SID_Flag);
# else  
  Pre_Decoder pre_process(inparam, reset_flag_old, reset_flag, prm,
                          TAF, SID_Flag);
# endif
  
  Decoder_12k2 dec_12k2(TAF, SID_Flag, prm, synth, reset_next);
  
  Post_Decoder post_process(synth, reset_flag, reset_flag_old, reset_next,
                            outframe);  
#else
# ifdef EXTERNAL_CONTROL
  Pre_Decoder pre_process(run_flag, run_start,
                          serial, new_serial, reset_flag_old, reset_flag, prm, 
                          TAF, SID_Flag);
# else  
  Pre_Decoder pre_process(serial, new_serial, reset_flag_old, reset_flag, prm, 
                          TAF, SID_Flag);
# endif  

  Decoder_12k2 dec_12k2(TAF, SID_Flag, prm, synth, reset_next);
  
  Post_Decoder post_process(synth, reset_flag, reset_flag_old, reset_next,
			    speech, speech_ready);
#endif
  
  void reset(void)
  {
    dec_12k2.reset();
  }

  void main(void)
  {
    while(true)
    {
      pre_process.main();
      
      dec_12k2.main();
      
      post_process.main();
    }
  }
};
