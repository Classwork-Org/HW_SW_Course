/*************************************************************************
 *   FUNCTION:   Dec_lag6
 *
 *   PURPOSE:  Decoding of fractional pitch lag with 1/6 resolution.
 *             Extract the integer and fraction parts of the pitch lag from
 *             the received adaptive codebook index.
 *
 *    See "Enc_lag6.c" for more details about the encoding procedure.
 *
 *    The fractional lag in 1st and 3rd subframes is encoded with 9 bits
 *    while that in 2nd and 4th subframes is relatively encoded with 6 bits.
 *    Note that in relative encoding only 61 values are used. If the
 *    decoder receives 61, 62, or 63 as the relative pitch index, it means
 *    that a transmission error occurred. In this case, the pitch lag from
 *    previous subframe is used.
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";

import "reset";


behavior Dec_lag6 (             /* output: return integer pitch lag       */
    in Word16 index,      /* input : received pitch index           */
    in int    pit_min,    /* input : minimum pitch lag              */
    in int    pit_max,    /* input : maximum pitch lag              */
    in Word16 i_subfr,    /* input : subframe flag                  */
    in int    L_frame_by2,/* input : speech frame size divided by 2 */
    in Word16 bfi,         /* input : bad frame indicator            */
    out Word16 T0_frac,   /* output: fractional part of pitch lag   */
    out Word16 T0_val)
  implements Ireset
{ 
  /* Old integer lag */
  Word16 old_T0;
  
  Word16 T0_min, T0_max;
  
  
  void init(void)
  {
    old_T0 = 40;
  }
  
  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    Word16 pit_flag;
    Word16 T0, i;

    pit_flag = i_subfr;          /* flag for 1st or 3rd subframe */
    if (sub (i_subfr, L_frame_by2) == 0)
    {
        pit_flag = 0;            
    }
    if (pit_flag == 0)          /* if 1st or 3rd subframe */
    {
        if (bfi == 0)
        {                       /* if bfi == 0 decode pitch */
            if (sub (index, 463) < 0)
            {
                /* T0 = (index+5)/6 + 17 */
                T0 = add (mult (add (index, 5), 5462), 17);
                i = add (add (T0, T0), T0);
                /* *T0_frac = index - T0*6 + 105 */
                T0_frac = add (sub (index, add (i, i)), 105);
                                 
            }
            else
            {
                T0 = sub (index, 368);
                T0_frac = 0;    
            }
        }
        else
            /* bfi == 1 */
        {
            T0 = old_T0;         
            T0_frac = 0;        
        }

        /* find T0_min and T0_max for 2nd (or 4th) subframe */

        T0_min = sub (T0, 5);
        if (sub (T0_min, pit_min) < 0)
        {
            T0_min = pit_min;    
        }
        T0_max = add (T0_min, 9);
        if (sub (T0_max, pit_max) > 0)
        {
            T0_max = pit_max;    
            T0_min = sub (T0_max, 9);
        }
    }
    else
        /* second or fourth subframe */
    {
        /* if bfi == 0 decode pitch */
        if ((bfi == 0) && (sub (index, 61) < 0))
        {
            /* i = (index+5)/6 - 1 */
            i = sub (mult (add (index, 5), 5462), 1);
            T0 = add (i, T0_min);
            i = add (add (i, i), i);
            T0_frac = sub (sub (index, 3), add (i, i));
                                 
        }
        else
            /* bfi == 1  OR index >= 61 */
        {
            T0 = old_T0;         
            T0_frac = 0;        
        }
    }

    old_T0 = T0;                 

    T0_val = T0;
  }
};
