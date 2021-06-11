//
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 01/08/02  <gerstl>	Updated to comply with extended port checking

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";



behavior Q_p (
	      in  Word16 indi[10],        /* Pulse position */
              out Word16 indo[10]
	      )
{
  void main(void)
    {
      static const Word16 gray[8] = {0, 1, 3, 2, 6, 4, 5, 7};
      Word16 tmp;
      Int i;

      for(i=0; i<10; i++)
	{
	  tmp = indi[i];

	  if (i < 5)
	    {
	      tmp = (tmp & 0x8) | gray[tmp & 0x7];     
                                                
	    }
	  else
	    {
	      tmp = gray[tmp & 0x7];                   
	    }

	  indo[i] = tmp;                                  
	}
    }
};
