#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "prm2bits";
import "cn_encoding";
import "sid_codeword_encode";

#ifndef USE_BIT_PORTS
import "channels";
#endif

behavior Post_Process_Seq1(in  DTXctrl txdtx_ctrl,
#ifndef USE_BIT_PORTS
		in Word16 local_serial[BITS_PER_FRAME],
# ifdef TYPED_CHANNELS
                i_Word_sender txdtx_ctrl_out,
                i_Word_sender serial
# else                           
		i_sender txdtx_ctrl_out,
		i_sender serial
# endif                           
#else
                out DTXctrl txdtx_ctrl_out,
                out event serialbits_ready
#endif
                           )
{
  void main(void)
  {   
#ifndef USE_BIT_PORTS
# ifdef TYPED_CHANNELS
    Word16 tmp;
    int i;
        
    for(i = 0; i < BITS_PER_FRAME; i++) {
      tmp = local_serial[i];
      serial.send(tmp);
    }
    txdtx_ctrl_out.send(txdtx_ctrl);    
# else    
    serial.send(local_serial,sizeof(local_serial));
    txdtx_ctrl_out.send(&txdtx_ctrl,sizeof(txdtx_ctrl));
# endif    
#else
    txdtx_ctrl_out = txdtx_ctrl;
    notify serialbits_ready;
#endif
  }
};



behavior Post_Process (
		       in  Word16 prm[PRM_SIZE],
		       in  DTXctrl txdtx_ctrl,
		       in  Flag reset_flag,
#ifndef USE_BIT_PORTS
		       /*out Word16 serial[BITS_PER_FRAME],*/
# ifdef TYPED_CHANNELS
                       i_Word_sender serial,
                       i_Word_sender txdtx_ctrl_out
# else                       
		       i_sender serial,
		       i_sender txdtx_ctrl_out
# endif                       
#else
		       out unsigned bit[BITS_PER_FRAME-1:0] serial,
		       out DTXctrl txdtx_ctrl_out,
		       out event serialbits_ready
#endif

		       )
  implements Ireset

{
  Word16 params[PRM_SIZE];
#ifndef USE_BIT_PORTS
  Word16  ser[BITS_PER_FRAME];
  Word16  local_serial[BITS_PER_FRAME];
#else
  unsigned bit[BITS_PER_FRAME-1:0] ser;
#endif

  Cn_Encoder cn_encoder(prm, params, txdtx_ctrl, reset_flag);
  Prm2bits_12k2 prm2bits_12k2(params, ser);

#ifndef USE_BIT_PORTS
  Sid_Codeword_Encoder sid_codeword_encoder(ser, txdtx_ctrl,local_serial);
  Post_Process_Seq1 seq1(txdtx_ctrl, local_serial, txdtx_ctrl_out, serial);
#else
  Sid_Codeword_Encoder sid_codeword_encoder(ser, txdtx_ctrl, serial);
  Post_Process_Seq1 seq1(txdtx_ctrl, txdtx_ctrl_out, serialbits_ready);
#endif


  void reset(void)
    {
      cn_encoder.reset();
    }

  void main(void)
  {
      /* Write comfort noise parameters into the parameter frame.  */
      /* Use old parameters in case SID frame is not to be updated */
      cn_encoder.main();

      /* Parameters to serial bits */
      prm2bits_12k2.main();

      /* Insert SID codeword into the serial parameter frame */
      sid_codeword_encoder.main();

      seq1.main();
  }

};
