#include "EFR_Coder_public.sh"

import "std_includes";


interface IStimulus 
{
  int open_file (char* speechfile_name);
};


behavior Stimulus(
		  out bit[SAMPLE_WIDTH-1:0] speech_samples[L_FRAME],
		  out event new_frame
		  )
  implements IStimulus
{
  FILE *f_speech;       /* File of speech data */
  
  
  int open_file(char* speechfile_name)
  {
    /* open file with raw speech data */
    if ((f_speech = fopen (speechfile_name, "rb")) == NULL)
    {
      printf ("Error opening input file  %s !!\n", speechfile_name);
      return (0);
    }
    printf (" Input speech file:  %s\n", speechfile_name);
    return (1);
  }
  
  
  void main()
  {
    Int frame_count = 0;
    Int sample_count;
    
    /* input file consists of 16 bit speech samples */
    short samples[L_FRAME];     
    
    /* read file one frame at a time */
    while (fread (samples, sizeof(short), L_FRAME, f_speech) == L_FRAME)
    {
      /* new speech frame every 20ms  */
      waitfor 20000000;              

      printf("\nframe=%3d ", ++frame_count);
      
      /* Delete the 3 LSBs (coder takes 13-bit input) */
      for (sample_count = 0; sample_count < L_FRAME; sample_count++) {
	speech_samples[sample_count] = samples[sample_count][15:3];
      }  
                  
      /* tell coder a new sample has arrived */
      notify new_frame;
    }
    
    fclose(f_speech);
    
    /* end of file reached, exit testbench */
    waitfor 20000000;              
    printf("\n\ndone, %d frames encoded\n\n", frame_count);
    
    exit(0);
        
  }
};
