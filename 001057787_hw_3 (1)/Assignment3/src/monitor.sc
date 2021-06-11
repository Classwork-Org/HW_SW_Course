#include <sim.sh>

#include "cnst.sh" 
#include "typedef.sh"


import "std_includes";
#ifndef USE_BIT_PORTS
import "channels";
#endif


/* position of the VAD and SP bits in the output stream */
#define VAD 1
#define SP  0


interface ISerialMonitor {
    int open_serial_outfile(char* serialfile_name);
};

behavior Encoder_Monitor(
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
                 i_Word_receiver serial_bits,
                 i_Word_receiver c_txdtx_ctrl
# else                 
                 i_receiver serial_bits,
                 i_receiver c_txdtx_ctrl
# endif                 
#else
		/* in Word16  serial_bits[BITS_PER_FRAME],*/
		 in unsigned bit[BITS_PER_FRAME-1:0] serial_bits,
		 in DTXctrl txdtx_ctrl,
		 in event serialbits_ready
#endif
		 )
  implements ISerialMonitor
{
  FILE *f_serial;     /* File of serial bits for transmission  */

  int open_serial_outfile (char* serialfile_name)
  {
    /* open output file and handle errors */
    if ((f_serial = fopen (serialfile_name, "w")) == NULL)
    {
      printf ("Error opening output bitstream file %s !!\n", 
	      serialfile_name);
      return (0);
    }
    printf (" Output bitstream file:  %s\n", serialfile_name);
    return (1);
  }
  
  
  void main(void)
  {
    sim_time f_start = 0;
#ifndef USE_BIT_PORTS
    int i;
# ifdef TYPED_CHANNELS
    Word16  txdtx_ctrl_tmp;
# endif    
    Word16  tmp[BITS_PER_FRAME];
    DTXctrl txdtx_ctrl;	
#else    
    /* wider bitvector to accommodate VAD and SP flag */
    unsigned bit[BITS_PER_FRAME+1:0] serbits;
    char buf[BITS_PER_FRAME+3];
#endif
    
    while(true)
    {
      /* wait for arrival of new encoded block */
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
      for(i = 0; i < BITS_PER_FRAME; i++) {
        serial_bits.receive(&tmp[i]);
      }
      c_txdtx_ctrl.receive(&txdtx_ctrl_tmp);
      txdtx_ctrl = txdtx_ctrl_tmp;
# else      
      serial_bits.receive(tmp,sizeof(tmp));
      c_txdtx_ctrl.receive(&txdtx_ctrl,sizeof(txdtx_ctrl));
# endif      
#else
      wait serialbits_ready;
#endif

#ifdef EXTERNAL_CONTROL      
      f_start = (now() / 20000000000llu) * 20000000000llu;
#else
      f_start += 20000000000llu;
#endif      
      
#ifdef ENABLE_DECODER      
      printf("\t\t encoding delay = %2.2f ms\n",
	     (double)(now() - f_start) / (double)1000000000.0);
#else      
      printf("\t\t encoding delay = %2.2f ms", 
	     (double)(now() - f_start) / (double)1000000000.0);
#endif

#ifndef USE_BIT_PORTS
      /* Write the bit stream to file */
      for(i = 0; i < BITS_PER_FRAME; i++)
          fprintf (f_serial, "%d", tmp[i]);
      
      /* write the VAD- and SP-flags to file after the speech
       *             parameter bit stream */
      fprintf (f_serial, "%d", (int)((txdtx_ctrl & TX_VAD_FLAG) != 0));
      fprintf (f_serial, "%d", (int)((txdtx_ctrl & TX_SP_FLAG) != 0));
      fprintf (f_serial, "\n");
#else
      /* copy the new block to the wider bitvector */
      serbits[2:BITS_PER_FRAME+1] = serial_bits;	
      
      /* write the VAD- and SP-flags to file after the speech
       *             parameter bit stream */
      serbits[VAD] = (txdtx_ctrl & TX_VAD_FLAG) != 0;
      serbits[SP]  = (txdtx_ctrl & TX_SP_FLAG)  != 0;
      
      
      /* Write the bit stream to file */
      fprintf (f_serial, "%s\n", ubit2str(2, &buf[BITS_PER_FRAME+2], serbits));
#endif
    }      
  }
};

#ifdef ENABLE_DECODER
interface ISpeechMonitor {
    int open_speech_outfile(char* speechfile_name);
};

behavior Decoder_Monitor(
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
                         i_Word_receiver speech_samples
# else                         
                         i_receiver speech_samples
# endif                         
#else                         
                         in Word16 speech[L_FRAME],
                         in event speech_ready
#endif                         
                         )
  implements ISpeechMonitor
{
  FILE *f_speech;  /* File of speech bits for transmission  */
  
  int open_speech_outfile (char* speechfile_name)
  {
    /* open output file and handle errors */
    if ((f_speech = fopen (speechfile_name, "wb")) == NULL)
    {
      printf ("Error opening output speech file %s !!\n",
	      speechfile_name);
      return (0);
    }
    printf (" Output speech file:  %s\n", speechfile_name);
    return (1);
  }
  
  
  void main(void)
  {
    Int sample_count;
    sim_time f_start = 5000000000ull;

#ifndef USE_BIT_PORTS
    Word16 speech[L_FRAME];
#endif    
    short speech_out[L_FRAME];

    while(true)
    {
      /* wait for arrival of new encoded block */
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
      for(sample_count = 0; sample_count < L_FRAME; sample_count++) {
        speech_samples.receive(&speech[sample_count]);
      }      
# else      
      speech_samples.receive(speech, sizeof(speech));
# endif      
#else      
      wait speech_ready;
#endif      
      
#ifdef EXTERNAL_CONTROL      
      f_start = ((now()-5000000000ull) / 20000000000llu) * 20000000000llu;
      f_start += 5000000000ull;
#else
      f_start += 20000000000ull;
#endif      
      
      printf("\t\t decoding delay = %2.2f ms\n",
	     (double)(now() - f_start) / (double)1000000000.0);
	
      /* perform byte swapping if necessary       */
      for (sample_count = 0; sample_count < L_FRAME; sample_count++) {
        speech_out[sample_count] = unorder_bytes(speech[sample_count]);
      }
      
      fwrite (speech_out, sizeof (short), L_FRAME, f_speech);
    }      
  }
};

#endif
