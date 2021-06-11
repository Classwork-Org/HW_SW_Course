#include "cnst.sh"
#include "typedef.sh"



behavior Nodtx_Setflags(
			out Flag VAD_flag,
			out DTXctrl txdtx_ctrl
			)
{
  void main(void)
    {
      VAD_flag = 1;
      txdtx_ctrl = TX_VAD_FLAG | TX_SP_FLAG;
    }
};
