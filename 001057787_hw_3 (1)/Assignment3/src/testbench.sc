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

#include "cnst.sh"             /* L_FRAME, SERIAL_SIZE    */
#include "typedef.sh"          /* Word16                  */


import "arg_handler";

import "stimulus";
import "vocoder";
import "monitor";
#ifndef USE_BIT_PORTS
import "channels";
#endif
#ifdef EXTERNAL_CONTROL
import "c_handshake";
#endif


behavior Main
{
  Flag dtx_mode;

#ifdef EXTERNAL_CONTROL
  c_handshake run_start, run_stop;
  
# ifndef USE_BIT_PORTS  
  GSM_Vocoder testbench(run_start, run_stop);
# else
  GSM_Vocoder testbench(dtx_mode, run_start, run_stop);
# endif  
#else    
# ifndef USE_BIT_PORTS  
  GSM_Vocoder testbench;
# else
  GSM_Vocoder testbench(dtx_mode);
# endif  
#endif
  
  int main (int argc, char *argv[])
  {
    char *speech_in_file_name;
    char *serial_out_file_name;
#ifdef ENABLE_DECODER
    char *serial_in_file_name;
    char *speech_out_file_name;
#endif    
    
    /* process the command line arguments */
#ifdef ENABLE_DECODER    
    dtx_mode = arg_handler(argc, argv, &speech_in_file_name, &serial_in_file_name,
                                       &speech_out_file_name, &serial_out_file_name);
#else    
    dtx_mode = arg_handler(argc, argv, &speech_in_file_name, 
                                       &serial_out_file_name);
#endif    
    
    /* Bring the encoder to the initial state */
#ifndef USE_BIT_PORTS
    if (!testbench.open_speech_infile(speech_in_file_name, dtx_mode)) exit(1);
#else    
    if (!testbench.open_speech_infile(speech_in_file_name)) exit(1);
#endif    
    if (!testbench.open_serial_outfile(serial_out_file_name)) exit(1);
#ifdef ENABLE_DECODER    
    if (!testbench.open_serial_infile(serial_in_file_name)) exit (1);
    if (!testbench.open_speech_outfile(speech_out_file_name)) exit (1);
#endif    
    
    testbench.reset();

#ifdef EXTERNAL_CONTROL
    run_start.send();
#endif    
    
    // run DSP system
    testbench.main();
 
    return (0);
  }
};
