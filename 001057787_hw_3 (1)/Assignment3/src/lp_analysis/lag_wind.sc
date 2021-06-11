/*************************************************************************
 *
 *  FUNCTION:  Lag_window()
 *
 *  PURPOSE:  Lag windowing of autocorrelations.
 *
 *  DESCRIPTION:
 *         r[i] = r[i]*lag_wind[i],   i=1,...,10
 *
 *     r[i] and lag_wind[i] are in special double precision format.
 *     See "oper_32b.c" for the format.
 *
 *************************************************************************/

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";




behavior Lag_Window (
    inout Word16 r_h[MP1],       /* (i/o)   : Autocorrelations  (msb) */
    inout Word16 r_l[MP1]        /* (i/o)   : Autocorrelations  (lsb) */
    )
{
  void main(void)
    {
      Int i;
      Word32 x;


      /*-----------------------------------------------------*
	| Table of lag_window for autocorrelation.            |
	| noise floor = 1.0001   = (0.9999  on r[1] ..r[10])  |
	| Bandwitdh expansion = 60 Hz                         |
	|                                                     |
	|                                                     |
	| lag_wind[0] =  1.00000000    (not stored)           |
	| lag_wind[1] =  0.99879038                           |
	| lag_wind[2] =  0.99546897                           |
	| lag_wind[3] =  0.98995781                           |
	| lag_wind[4] =  0.98229337                           |
	| lag_wind[5] =  0.97252619                           |
	| lag_wind[6] =  0.96072036                           |
	| lag_wind[7] =  0.94695264                           |
	| lag_wind[8] =  0.93131179                           |
	| lag_wind[9] =  0.91389757                           |
	| lag_wind[10]=  0.89481968                           |
	-----------------------------------------------------*/

      const Word16 lag_h[10] =
      {
	32728, 32619, 32438, 32187, 31867, 31480, 31029, 30517, 29946, 29321
      };

      const Word16 lag_l[10] =
      {
	11904, 17280, 30720, 25856, 24192, 28992, 24384,  7360, 19520, 14784
      };




      for (i = 1; i <= M; i++)
	{
	  x = Mpy_32 (r_h[i], r_l[i], lag_h[i - 1], lag_l[i - 1]);
	  L_Extract (x, &r_h[i], &r_l[i]);
	}
    }
};
