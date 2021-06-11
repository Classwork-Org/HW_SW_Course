#include <sim.sh>
#include "cnst.sh" 
#include "typedef.sh"

import "std_includes";

#ifndef USE_BIT_PORTS
import "channels";
#endif

interface ISpeechStimulus {
#ifndef USE_BIT_PORTS
  int open_speech_infile(char* speechfile_name, Flag dtx_mode);
#else  
  int open_speech_infile (char* speechfile_name);
#endif
};


behavior Encoder_Stimulus(
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
                  i_Word_sender speech_samples,
                  i_Flag_sender dtx_mode
# else                  
                  i_sender speech_samples,
                  i_sender dtx_mode
# endif                  
#else
		/*	out Word16 speech_samples[L_FRAME],*/
		  out bit[SAMPLE_WIDTH-1:0] speech_samples[L_FRAME],
		  out event new_frame
#endif
		  )
  implements ISpeechStimulus
{
  Flag local_dtx_mode;
#ifdef EXTERNAL_CONTROL  
  signal FILE *f_speech = 0;       /* File of speech data */
#else
  FILE *f_speech = 0;
#endif
  
#ifndef USE_BIT_PORTS  
  int open_speech_infile(char* speechfile_name, Flag dtx)
  {
    FILE *f;
    
    local_dtx_mode = dtx;
#else  
  int open_speech_infile(char* speechfile_name)
  {
    FILE *f;    
#endif
    /* open file with raw speech data */
    if ((f = fopen (speechfile_name, "rb")) == NULL)
    {
      printf ("Error opening input file  %s !!\n", speechfile_name);
      return (0);
    }
    printf (" Input speech file:  %s\n", speechfile_name);
    f_speech = f;
    return (1);
  }
    
  void main()
  {
    Int frame_count = 0;
    Int sample_count;
    sim_time f_start = 0;
    
    /* input file consists of 16 bit speech samples */
    short samples[L_FRAME];

#ifndef USE_BIT_PORTS
    Word16 tmp[L_FRAME];

#ifdef EXTERNAL_CONTROL
    while(!f_speech) wait(f_speech);
#endif      

#ifdef TYPED_CHANNELS
    dtx_mode.send(local_dtx_mode);
# else    
    dtx_mode.send(&local_dtx_mode,sizeof(local_dtx_mode));
# endif    
#endif
    
    f_start = now();      
      
    /* read file one frame at a time */
    while (fread (samples, sizeof(short), L_FRAME, f_speech) == L_FRAME)
    {
      /* new speech frame every 20ms  */
      waitfor 20000000000llu - ((now() - f_start) % 20000000000llu);
      f_start = now();
        
#ifdef ENABLE_DECODER      
      printf("encoding frame=%3d\n", ++frame_count);
#else      
      printf("\nframe=%3d ", ++frame_count);
#endif      
      
      /* Delete the 3 LSBs (coder takes 13-bit input) */
      /* and perform byte swapping if necessary       */
      for (sample_count = 0; sample_count < L_FRAME; sample_count++) {
        samples[sample_count]        = order_bytes(samples[sample_count]);
#ifndef USE_BIT_PORTS
        tmp[sample_count] =  samples[sample_count][15:3];
#else
        speech_samples[sample_count] = samples[sample_count][15:3];
#endif
      }

#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
      /* send frame to coder */
      for(sample_count = 0; sample_count < L_FRAME; sample_count++) {
        speech_samples.send(tmp[sample_count]);
      }
# else        
      speech_samples.send(tmp,sizeof(tmp));
# endif        
#else
      /* tell coder a new sample has arrived */
      notify new_frame;
#endif
    }

    fclose(f_speech);
    f_speech = 0;
    
#ifdef PIPED_CODER
    /* flush coder pipeline */
    waitfor 20000000000ull;
    printf("\n          ");
# ifndef USE_BIT_PORTS
#  ifdef TYPED_CHANNELS
    for(sample_count = 0; sample_count < L_FRAME; sample_count++) {          
      speech_samples.send(tmp[sample_count]);
    }
#  else    
    speech_samples.send(tmp,sizeof(tmp));
#  endif    
# else      
    notify new_frame;
# endif      
#endif    
    
    /* end of file reached, exit testbench */
    waitfor 20000000000ull;              
#ifdef ENABLE_DECODER    
    printf("%d frames encoded\n", frame_count);
#else    
    printf("\n\ndone, %d frames encoded\n\n", frame_count);

#ifndef EXTERNAL_CONTROL
    exit(0);
#endif    
#endif
  }
};

#ifdef ENABLE_DECODER
interface ISerialStimulus {
  int  open_serial_infile (char* serialfile_name);
};
  
behavior Decoder_Stimulus(
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
                          i_Word_sender serial_bits
# else                          
                          i_sender serial_bits
# endif                          
#else                          
                          out Word16 serial[SERIAL_SIZE+2],
                          out event new_serial
#endif                          
                          )
  implements ISerialStimulus
{
#ifdef EXTERNAL_CONTROL  
  signal FILE *f_serial = 0;       /* File of speech data */
#else
  FILE *f_serial;
#endif  
  
  int open_serial_infile(char* serialfile_name)
  {
    FILE *f;
    
    /* open file with raw speech data */
    if ((f = fopen (serialfile_name, "rb")) == NULL)
    {
      printf ("Error opening input file  %s !!\n", serialfile_name);
      return (0);
    }
    printf (" Input serial file:  %s\n", serialfile_name);
    f_serial = f;
    return (1);
  }

  
  void main()
  {
    Int frame_count = 0;
    Int sample_count;
    sim_time f_start = 0;

#ifndef USE_BIT_PORTS
    Word16 serial[SERIAL_SIZE+2];
#endif    
    
    /* input file consists of 16 bit serial samples */
    short data[SERIAL_SIZE+2];

#ifdef EXTERNAL_CONTROL
    while(!f_serial) wait(f_serial);
#endif
      
    waitfor 5000000000ull;
    f_start = now();
      
    while (fread (data, sizeof(short), SERIAL_SIZE+2, f_serial) == SERIAL_SIZE+2)
    {
      /* new serial frame every 20ms */
      waitfor 20000000000llu - ((now() - f_start) % 20000000000llu);
      f_start = now();

      printf("decoding frame=%3d\n", ++frame_count);
      
      for (sample_count = 0; sample_count < SERIAL_SIZE+2; sample_count++) {
        serial[sample_count] = order_bytes(data[sample_count]);
      }  

#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
      /* send frame to coder */
      for(sample_count = 0; sample_count < SERIAL_SIZE+2; sample_count++) {
        serial_bits.send(serial[sample_count]);
      }        
# else        
      serial_bits.send(serial, sizeof(serial));
# endif        
#else        
      /* tell coder a new sample has arrived */
      notify new_serial;
#endif        
    }
    
    fclose(f_serial);
    f_serial = 0;
    
    /* end of file reached, exit testbench */
    waitfor 200000000000ull;
    printf("%d frames decoded\n", frame_count);
    printf("Done\n");
    
#ifndef EXTERNAL_CONTROL    
    exit(0);
#endif    
  }
};

#endif
