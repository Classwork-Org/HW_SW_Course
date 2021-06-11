/***************************************************************************
 *
 *  FILE NAME:   testbench.sc
 *
 *  Testbench for the EFR coder
 *
 *    Usage : testbench speech_file bitstream_file <dtx|nodtx>
 *
 *    Format for speech_file:
 *      Speech is read from a binary file of 16 bits data.
 *
 *    Format for bitstream_file:
 *      244  words (2-byte) containing 244 bits.
 *          Bit 0 = 0x0000 and Bit 1 = 0x0001
 *      One word (2-byte) for voice activity decision (VAD) flag bit
 *          0x0000 -> inactive (no detected speech activity);
 *          0x0001 -> active
 *      One word (2-byte) for speech (SP) flag bit
 *          0x0000 -> inactive (no transmission of speech frames);
 *          0x0001 -> active
 *
 *  03/07/99 Martin v. Weymarn
 ***************************************************************************/

#include "EFR_Coder_public.sh"


import "arg_handler";

import "stimulus";
import "EFR_Coder_public";
import "monitor";



behavior Main
{
  Flag dtx_mode;
  
  /* Input speech buffer      */
  bit[SAMPLE_WIDTH-1:0] speech_samples[L_FRAME]; 
  /* Output bitstream buffer  */
  unsigned bit[BITS_PER_FRAME-1:0] serial_bits;  

  DTXctrl txdtx_ctrl;                /* Encoder DTX control word */

  event new_frame;
  event serialbits_ready;

  
  Stimulus stimulus(speech_samples, new_frame);
  Coder coder(speech_samples, serial_bits, dtx_mode, txdtx_ctrl,
	      new_frame, serialbits_ready);
  Monitor monitor(serial_bits, txdtx_ctrl, serialbits_ready);


  int main (int argc, char *argv[])
  {
    char *speechfile_name;
    char *serialfile_name;

    /* process the command line arguments */
    dtx_mode = arg_handler(argc, argv, &speechfile_name, &serialfile_name);

    /* open the files */
    if (!stimulus.open_file(speechfile_name)) exit (1);
    if (!monitor.open_file(serialfile_name))  exit (1);
    
    /* Bring the encoder to the initial state */
    coder.reset();

    /* run the testbench */
    par
    {
      stimulus.main();
      
      coder.main();

      monitor.main();
    }

    return (0);
  }
};

