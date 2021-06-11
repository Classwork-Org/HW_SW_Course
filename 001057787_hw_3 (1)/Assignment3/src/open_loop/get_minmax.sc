#include "cnst.sh"
#include "typedef.sh"

import "basic_op";



behavior Get_Minmax (
		     in  Word16 T_op,
		     out Word16 T0_min,
		     out Word16 T0_max,
		     in  DTXctrl txdtx_ctrl        /* DTX control word */
		     )
{
  void main(void)
    {
      Word16 min, max;
      
      if ((txdtx_ctrl & TX_SP_FLAG) != 0)
	{
	  /* Range for closed loop pitch search */
	  
	  min = sub (T_op, 3);
	  
	  if (sub (min, PIT_MIN) < 0)
	    {
	      min = PIT_MIN;                                    
	    }
	  
	  max = add (min, 6);
	  
	  if (sub (max, PIT_MAX) > 0)
	    {
	      max = PIT_MAX;                                    
	      min = sub (max, 6);
	    }
	  
	  T0_min = min;
	  T0_max = max;
	}
      
    }
};
