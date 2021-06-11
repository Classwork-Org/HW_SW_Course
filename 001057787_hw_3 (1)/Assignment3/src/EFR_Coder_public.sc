#include "EFR_Coder_public.sh"

import "reset";
#ifndef USE_BIT_PORTS
import "channels";
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

  implements Ireset;


note Coder.comment = "GSM Enhanced Full Rate speech encoder (GSM 06.60)";

note Coder.functionality = "The EFR Coder maps 160 speech samples in 13-bit uniform PCM format to encoded blocks of 244 bits according to the GSM technical specification GSM 06.60, version 5.1.1.";

note Coder.scc_ReservedSize = 20000u;     // actual value 10764u! (but we keep it secret)
note Coder.scc_Public = true;
