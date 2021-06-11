#include "typedef.sh"
#include "cnst.sh"

import "F_gamma";

import "weight_ai";

behavior Par_Weight (
		     in  Word16 A[MP1],
		     out Word16 Ap1[MP1],
		     out Word16 Ap2[MP1]
		     )
{
  Weight_Ai weight_1(A, F_gamma1, Ap1);
  Weight_Ai weight_2(A, F_gamma2, Ap2);
  
  void main(void)
    {
      par
	{
	  weight_1.main();
	  weight_2.main();
	}
    }
};
