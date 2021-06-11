
#include "cnst.sh"
#include "typedef.sh"

#ifndef USE_BIT_PORTS
import "channels";
#endif

import "reset";

import "stimulus";
import "monitor";
import "coder";
#ifdef ENABLE_DECODER
import "decoder";
#endif


#ifdef EXTERNAL_CONTROL
behavior Vocoder_Control(i_receive run_start,
                         i_receive run_stop,
                         out Flag run_flag,
#ifdef ENABLE_DECODER                         
                         i_send decoder_start,
#endif                         
                         i_send coder_start)
{  
  void main(void)
  {
    run_flag = false;
    while(true) {
      run_start.receive();
      run_flag = true;
      coder_start.send();
#ifdef ENABLE_DECODER        
      decoder_start.send();
#endif
      run_stop.receive();
    }
  }
};
#endif


#ifdef ENABLE_DECODER
behavior Vocoder (
#ifdef EXTERNAL_CONTROL
                  i_receive run_start,
                  i_receive run_stop,
#endif                  
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
                  i_Word_receiver speech_in,
                  i_Flag_receiver dtx_mode,
                  i_Word_sender   serial_out,
                  i_Word_sender   txdtx_ctrl,
                  i_Word_receiver serial_in,
                  i_Word_sender   speech_out
# else                  
                  i_receiver speech_in,
                  i_receiver dtx_mode,
                  i_sender   serial_out,
                  i_sender   txdtx_ctrl,
                  i_receiver serial_in,
                  i_sender   speech_out
# endif                  
#else
                  in  bit[SAMPLE_WIDTH-1:0] speech_in[L_FRAME],
                  in  Flag dtx_mode,
                  in  event new_speech_in,
                  out unsigned bit[BITS_PER_FRAME-1:0] serial_out,
                  out DTXctrl txdtx_ctrl,
                  out event serial_out_ready,
                  in  Word16 serial_in[SERIAL_SIZE+2],
                  in  event new_serial_in,		 
                  out Word16 speech_out[L_FRAME],
                  out event speech_out_ready
#endif
                  )
  implements Ireset
{
#ifdef EXTERNAL_CONTROL
  Flag run_flag = 0;
  c_handshake decoder_start;
  c_handshake coder_start;
  
  Vocoder_Control control(run_start, run_stop,
                          run_flag, decoder_start, coder_start);
# ifndef USE_BIT_PORTS
  Coder coder(run_flag, coder_start, 
              speech_in, dtx_mode, serial_out, txdtx_ctrl);
  Decoder decoder(run_flag, decoder_start, serial_in, speech_out);
# else
  Coder coder(run_flag, coder_start,
              speech_in, dtx_mode, new_speech_in, serial_out, txdtx_ctrl,
	      serial_out_ready);
  Decoder decoder(run_flag, decoder_start,
                  serial_in, new_serial_in, speech_out, speech_out_ready);
# endif  
#else  
# ifndef USE_BIT_PORTS
  Coder coder(speech_in, dtx_mode, serial_out, txdtx_ctrl);
  Decoder decoder(serial_in, speech_out);
# else
  Coder coder(speech_in, dtx_mode, new_speech_in, serial_out, txdtx_ctrl,
	      serial_out_ready);
  Decoder decoder(serial_in, new_serial_in, speech_out, speech_out_ready);
# endif
#endif  

  void reset(void)
    {
      coder.reset();
      decoder.reset();
    }

  void main(void)
  {
    par {
#ifdef EXTERNAL_CONTROL
      control.main();
#endif
      coder.main();
      decoder.main();
    }
  }
};

behavior GSM_Vocoder(
#ifdef USE_BIT_PORTS                     
                     in  Flag dtx_mode
# ifdef EXTERNAL_CONTROL
                     ,
# endif                     
#endif                     
#ifdef EXTERNAL_CONTROL
                     i_receive run_start,
                     i_receive run_stop
#endif                     
                     )
  implements Ireset, ISpeechStimulus, ISerialMonitor, ISerialStimulus, ISpeechMonitor
{
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
  c_Flag_double_handshake local_dtx_mode;
  c_Word_double_handshake speech_in;
  c_Word_double_handshake serial_in;
  
  c_Word_double_handshake serial_out;
  c_Word_double_handshake txdtx_ctrl;
  c_Word_double_handshake speech_out;
# else  
  c_double_handshake local_dtx_mode;
  c_double_handshake speech_in;
  c_double_handshake serial_in;
  
  c_double_handshake serial_out;
  c_double_handshake txdtx_ctrl;
  c_double_handshake speech_out;
# endif  
  
  Encoder_Stimulus speech_stimulus(speech_in, local_dtx_mode);
  Decoder_Stimulus serial_stimulus(serial_in);
# ifdef EXTERNAL_CONTROL
  Vocoder vocoder(run_start, run_stop,
                  speech_in, local_dtx_mode, serial_out, txdtx_ctrl,
                  serial_in, speech_out);
# else  
  Vocoder vocoder(speech_in, local_dtx_mode, serial_out, txdtx_ctrl,
                  serial_in, speech_out);
# endif  
  Encoder_Monitor serial_monitor(serial_out, txdtx_ctrl);
  Decoder_Monitor speech_monitor(speech_out);

  int open_speech_infile(char* speechfile_name, Flag dtx_mode) {
    return speech_stimulus.open_speech_infile(speechfile_name, dtx_mode);
  }
  
  int open_serial_infile(char* serialfile_name) {
    return serial_stimulus.open_serial_infile(serialfile_name);
  }    
#else
  bit[SAMPLE_WIDTH-1:0] speech_in[L_FRAME]; 
  Word16 serial_in[SERIAL_SIZE+2];
  event new_speech_in, new_serial_in;
  
  unsigned bit[BITS_PER_FRAME-1:0] serial_out;
  Word16 speech_out[L_FRAME];
  event serial_out_ready, speech_out_ready;
  DTXctrl txdtx_ctrl;                /* Encoder DTX control word */
  
  Encoder_Stimulus speech_stimulus(speech_in, new_speech_in);
  Decoder_Stimulus serial_stimulus(serial_in, new_serial_in);
# ifdef EXTERNAL_CONTROL
  Vocoder vocoder(run_start, run_stop,
                  speech_in, dtx_mode, new_speech_in, serial_out, txdtx_ctrl,
                  serial_out_ready, serial_in, new_serial_in,
                  speech_out, speech_out_ready);
# else  
  Vocoder vocoder(speech_in, dtx_mode, new_speech_in, serial_out, txdtx_ctrl,
                  serial_out_ready, serial_in, new_serial_in,
                  speech_out, speech_out_ready);
# endif  
  Encoder_Monitor serial_monitor(serial_out, txdtx_ctrl, serial_out_ready);
  Decoder_Monitor speech_monitor(speech_out, speech_out_ready);
  
  int open_speech_infile(char* speechfile_name) {
    return speech_stimulus.open_speech_infile(speechfile_name);
  }
  
  int open_serial_infile(char* serialfile_name) {
    return serial_stimulus.open_serial_infile(serialfile_name);
  }    
#endif  

  int open_serial_outfile (char* serialfile_name) {
    return serial_monitor.open_serial_outfile(serialfile_name);
  }

  int open_speech_outfile (char* speechfile_name) {
    return speech_monitor.open_speech_outfile(speechfile_name);
  }
  
  void reset(void)
  {
    vocoder.reset();
  }

  void main (void)
  {
    par
    {
      speech_stimulus.main();
      serial_stimulus.main();
      vocoder.main();
      serial_monitor.main();
      speech_monitor.main();
    }
  }
};

#else // ENABLE_DECODER

#ifndef USE_BIT_PORTS
#ifdef TYPED_CHANNELS
behavior SpeechIn(i_Word_receiver speech_in, i_Word_sender speech, 
                  i_Flag_receiver dtx_mode_in, i_Flag_sender dtx_mode)
{
  void main(void) 
  {
    Word16 sample;
    Flag mode;

    dtx_mode_in.receive(&mode);
    dtx_mode.send(mode);

    while (true) {
      speech_in.receive(&sample);
      speech.send(sample);
    }
  }
};

behavior SerialOut(i_Word_receiver serial, i_Word_sender serial_out,
                   i_Word_receiver txdtx, i_Word_sender txdtx_out)
{
  void main(void) 
  {
    int i;
    Word16 data;
    Word16 txdtx_ctrl;

    while (true) {
      for(i = 0; i < BITS_PER_FRAME; i++) {
        serial.receive(&data);
        serial_out.send(data);
      }
      txdtx.receive(&txdtx_ctrl);
      txdtx_out.send(txdtx_ctrl);
    }
  }
};
#else  // TYPED_CHANNELS
behavior SpeechIn(i_receiver speech_in, i_sender speech, 
                  i_receiver dtx_mode_in, i_sender dtx_mode)
{
  void main(void) 
  {
    short samples[L_FRAME];    
    Flag mode;

    dtx_mode_in.receive(&mode, sizeof(mode));
    dtx_mode.send(&mode, sizeof(mode));

    while (true) {
      speech_in.receive(samples, sizeof(samples));
      speech.send(samples, sizeof(samples));
    }
  }
};

behavior SerialOut(i_receiver serial, i_sender serial_out,
                   i_receiver txdtx, i_sender txdtx_out)
{
  void main(void) 
  {
    Word16 data[BITS_PER_FRAME];
    DTXctrl txdtx_ctrl;

    while (true) {
      serial.receive(data, sizeof (data));
      txdtx.receive(&txdtx_ctrl, sizeof (txdtx_ctrl));
      serial_out.send(data, sizeof (data));
      txdtx_out.send(&txdtx_ctrl, sizeof (txdtx_ctrl));
    }
  }
};
#endif  // TYPED_CHANNELS

behavior Vocoder(
#ifdef EXTERNAL_CONTROL
                 i_receive run_start,
                 i_receive run_stop,
#endif                  
#ifdef TYPED_CHANNELS
                 i_Word_receiver samples_in, 
                 i_Flag_receiver dtx_mode_in, 
                 i_Word_sender serial_bits_out, 
                 i_Word_sender txdtx_ctrl_out
#else                 
                 i_receiver samples_in, 
                 i_receiver dtx_mode_in, 
                 i_sender serial_bits_out, 
                 i_sender txdtx_ctrl_out
#endif                 
                 )
  implements Ireset
{
#ifdef EXTERNAL_CONTROL
  Flag run_flag = 0;
  c_handshake coder_start;
  
  Vocoder_Control control(run_start, run_stop, run_flag, coder_start);
#endif  
  
#ifdef TYPED_CHANNELS  
  c_Word_double_handshake speech_samples;
  c_Flag_double_handshake dtx_mode;

  /* Output bitstream buffer  */
  c_Word_double_handshake serial_bits;
  c_Word_double_handshake txdtx_ctrl;
#else
  c_double_handshake speech_samples;
  c_double_handshake dtx_mode;

  /* Output bitstream buffer  */
  c_double_handshake serial_bits;
  c_double_handshake txdtx_ctrl;
#endif  
  
  SpeechIn speech(samples_in, speech_samples, dtx_mode_in, dtx_mode);
#ifdef EXTERNAL_CONTROL
  Coder coder(run_flag, coder_start,
              speech_samples, dtx_mode, serial_bits, txdtx_ctrl);
#else  
  Coder coder(speech_samples, dtx_mode, serial_bits, txdtx_ctrl);
#endif  
  SerialOut serial(serial_bits, serial_bits_out, txdtx_ctrl, txdtx_ctrl_out);
  
  void reset(void)
  {
    coder.reset();
  }

  void main (void)
  {
    par
    {
#ifdef EXTERNAL_CONTROL
      control.main();
#endif      
      speech.main();
      coder.main();
      serial.main();
    }
  }
};
#endif  // USE_BIT_PORTS

behavior GSM_Vocoder(
#ifdef USE_BIT_PORTS                     
                     in  Flag dtx_mode
# ifdef EXTERNAL_CONTROL
                     ,
# endif                     
#endif                     
#ifdef EXTERNAL_CONTROL
                     i_receive run_start,
                     i_receive run_stop
#endif                     
                     )
  implements Ireset, ISpeechStimulus, ISerialMonitor
{
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS  
  c_Word_double_handshake speech_samples;
  c_Flag_double_handshake local_dtx_mode;

  c_Word_double_handshake serial_bits;
  c_Word_double_handshake txdtx_ctrl;
# else
  c_double_handshake local_dtx_mode;
  c_double_handshake speech_samples;
  
  c_double_handshake serial_bits;
  c_double_handshake txdtx_ctrl;
# endif  
  
  Encoder_Stimulus stimulus(speech_samples, local_dtx_mode);
#ifdef EXTERNAL_CONTROL
  Vocoder vocoder(run_start, run_stop,
                  speech_samples, local_dtx_mode, serial_bits, txdtx_ctrl);
#else  
  Vocoder vocoder(speech_samples, local_dtx_mode, serial_bits, txdtx_ctrl);
#endif  
  Encoder_Monitor monitor(serial_bits, txdtx_ctrl);

  int open_speech_infile(char* speechfile_name, Flag dtx_mode) {
    return stimulus.open_speech_infile(speechfile_name, dtx_mode);
  }
#else  // USE_BIT_PORTS
  bit[SAMPLE_WIDTH-1:0] speech_samples[L_FRAME]; 
  event new_frame;
  
  unsigned bit[BITS_PER_FRAME-1:0] serial_bits;  
  event serialbits_ready;
  DTXctrl txdtx_ctrl;                /* Encoder DTX control word */
  
#ifdef EXTERNAL_CONTROL
  Flag run_flag = 0;
  c_handshake coder_start;
  
  Vocoder_Control control(run_start, run_stop, run_flag, coder_start);
  Stimulus stimulus(speech_samples, new_frame);
  Coder coder(run_flag, coder_start,
              speech_samples, dtx_mode, new_frame, serial_bits, txdtx_ctrl,
	      serialbits_ready);
#else
  Stimulus stimulus(speech_samples, new_frame);
  Coder coder(speech_samples, dtx_mode, new_frame, serial_bits, txdtx_ctrl,
	      serialbits_ready);
#endif  
  Monitor monitor(serial_bits, txdtx_ctrl, serialbits_ready);

  int open_speech_infile(char* speechfile_name) {
    return stimulus.open_speech_infile(speechfile_name);
  }
#endif  

  int open_serial_outfile (char* serialfile_name) {
    return monitor.open_serial_outfile(serialfile_name);
  }
  
  void reset(void)
  {
#ifndef USE_BIT_PORTS
    vocoder.reset();
#else    
    coder.reset();
#endif    
  }

  void main (void)
  {
    par
    {
      stimulus.main();
#ifndef USE_BIT_PORTS      
      vocoder.main();
#else      
# ifdef EXTERNAL_CONTROL
      control.main();
# endif      
      coder.main();
#endif
      monitor.main();
    }
  }
};

#endif
