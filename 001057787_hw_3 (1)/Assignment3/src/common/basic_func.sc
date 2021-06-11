/*___________________________________________________________________________
 |                                                                           |
 | Basic (mathematical) functions.                                           |
 |___________________________________________________________________________|
*/

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";


/*************************************************************************
 *
 *  FUNCTION:   Convolut
 *
 *  PURPOSE:
 *     Perform the convolution between two vectors x[] and h[] and
 *     write the result in the vector y[]. All vectors are of length L
 *     and only the first L samples of the convolution are computed.
 *
 *  DESCRIPTION:
 *     The convolution is given by
 *
 *          y[n] = sum_{i=0}^{n} x[i] h[n-i],        n=0,...,L-1
 *
 *************************************************************************/

void Convolut (
    Word16 x[],        /* (i)     : input vector                           */
    Word16 h[],        /* (i)     : impulse response                       */
    Word16 y[],        /* (o)     : output vector                          */
    Word16 L           /* (i)     : vector size                            */
)
{
    Int i, n;
    Word32 s;

    for (n = 0; n < L; n++)
    {
        s = 0;             
        for (i = 0; i <= n; i++)
        {
            s = L_mac (s, x[i], h[n - i]);
        }
        s = L_shl (s, 3);
        y[n] = extract_h (s);
    }

    return;
}


/*************************************************************************
 *
 *   FUNCTION:   Log2()
 *
 *   PURPOSE:   Computes log2(L_x),  where   L_x is positive.
 *              If L_x is negative or zero, the result is 0.
 *
 *   DESCRIPTION:
 *        The function Log2(L_x) is approximated by a table and linear
 *        interpolation. The following steps are used to compute Log2(L_x)
 *
 *           1- Normalization of L_x.
 *           2- exponent = 30-exponent
 *           3- i = bit25-b31 of L_x;  32<=i<=63  (because of normalization).
 *           4- a = bit10-b24
 *           5- i -=32
 *           6- fraction = log_table[i]<<16-(log_table[i]-log_table[i+1])*a*2
 *
 *************************************************************************/

static const Word16 log_table[33] =
{
    0, 1455, 2866, 4236, 5568, 6863, 8124, 9352, 10549, 11716,
    12855, 13967, 15054, 16117, 17156, 18172, 19167, 20142, 21097, 22033,
    22951, 23852, 24735, 25603, 26455, 27291, 28113, 28922, 29716, 30497,
    31266, 32023, 32767
};


void Log2 (
    Word32 L_x,         /* (i) : input value                                 */
    Word16 *exponent,   /* (o) : Integer part of Log2.   (range: 0<=val<=30) */
    Word16 *fraction    /* (o) : Fractional part of Log2. (range: 0<=val<1) */
)
{
    Int i = 0;
    Word16 exp, a, tmp;
    Word32 L_y;

     
    if (L_x <= (Word32) 0)
    {
        *exponent = 0;           
        *fraction = 0;           
        return;
    }

    exp = norm_l (L_x);
    L_x = L_shl (L_x, exp);     /* L_x is normalized */

    *exponent = sub (30, exp);   

#ifndef USE_BIT_OPS
     L_x = L_shr (L_x, 9);
     i = extract_h (L_x);        /* Extract b25-b31 */
#else
     i = L_x[31:25];
#endif

#ifndef USE_BIT_OPS
     L_x = L_shr (L_x, 1);
     a = extract_l (L_x);        /* Extract b10-b24 of fraction */
     a = a & (Word16) 0x7fff;     
#else
     a = 0b @ L_x[24:10];
#endif


    i = sub (i, 32);

    L_y = L_deposit_h (log_table[i]);       /* log_table[i] << 16        */
    tmp = sub(log_table[i], log_table[i + 1]); 
    L_y = L_msu (L_y, tmp, a);  /* L_y -= tmp*a*2        */

    *fraction = extract_h (L_y); 

    return;
}


/*************************************************************************
 *
 *   FUNCTION:  Pow2()
 *
 *   PURPOSE: computes  L_x = pow(2.0, exponent.fraction)
 *
 *   DESCRIPTION:
 *       The function Pow2(L_x) is approximated by a table and linear
 *       interpolation.
 *          1- i = bit10-b15 of fraction,   0 <= i <= 31
 *          2- a = bit0-b9   of fraction   
 *          3- L_x = pow_table[i]<<16 - (pow_table[i] - pow_table[i+1]) * a * 2
 *          4- L_x = L_x >> (30-exponent)     (with rounding)
 *
 *************************************************************************/

static const Word16 pow_table[33] =
{
    16384, 16743, 17109, 17484, 17867, 18258, 18658, 19066, 19484, 19911,
    20347, 20792, 21247, 21713, 22188, 22674, 23170, 23678, 24196, 24726,
    25268, 25821, 26386, 26964, 27554, 28158, 28774, 29405, 30048, 30706,
    31379, 32066, 32767
};


Word32 Pow2 (           /* (o)  : result       (range: 0<=val<=0x7fffffff) */
    Word16 exponent,    /* (i)  : Integer part.      (range: 0<=val<=30)   */
    Word16 fraction     /* (i)  : Fractional part.  (range: 0.0<=val<1.0) */
)
{
    Int i = 0;
    Word16 exp, a, tmp;
    Word32 L_x;

#ifndef USE_BIT_OPS
    L_x = L_mult (fraction, 32);             /* L_x = fraction<<6           */
    i = extract_h (L_x);                     /* Extract b10-b16 of fraction */
#else
    i = fraction[15:10];
#endif


#ifndef USE_BIT_OPS
    L_x = L_shr (L_x, 1);
    a = extract_l (L_x);                     /* Extract b0-b9   of fraction */
    a = a & (Word16) 0x7fff;     
#else
    a = 0b @ fraction[9:0] @ 00000b;
#endif


    L_x = L_deposit_h (pow_table[i]);        /* pow_table[i] << 16        */
    tmp = sub (pow_table[i], pow_table[i + 1]);  
    L_x = L_msu (L_x, tmp, a);               /* L_x -= tmp*a*2        */

    exp = sub (30, exponent);
    L_x = L_shr_r (L_x, exp);

    return (L_x);
}



/*************************************************************************
 *
 *  FUNCTION:   Inv_sqrt
 *
 *  PURPOSE:   Computes 1/sqrt(L_x),  where  L_x is positive.
 *             If L_x is negative or zero, the result is 1 (3fff ffff).
 *
 *  DESCRIPTION:
 *       The function 1/sqrt(L_x) is approximated by a table and linear
 *       interpolation. The inverse square root is computed using the
 *       following steps:
 *          1- Normalization of L_x.
 *          2- If (30-exponent) is even then shift right once.
 *          3- exponent = (30-exponent)/2  +1
 *          4- i = bit25-b31 of L_x;  16<=i<=63  because of normalization.
 *          5- a = bit10-b24
 *          6- i -=16
 *          7- L_y = sqrt_table[i]<<16 - (sqrt_table[i] - sqrt_table[i+1])*a*2
 *          8- L_y >>= exponent
 *
 *************************************************************************/

static const Word16 sqrt_table[49] =
{

    32767, 31790, 30894, 30070, 29309, 28602, 27945, 27330, 26755, 26214,
    25705, 25225, 24770, 24339, 23930, 23541, 23170, 22817, 22479, 22155,
    21845, 21548, 21263, 20988, 20724, 20470, 20225, 19988, 19760, 19539,
    19326, 19119, 18919, 18725, 18536, 18354, 18176, 18004, 17837, 17674,
    17515, 17361, 17211, 17064, 16921, 16782, 16646, 16514, 16384
};

Word32 Inv_sqrt (       /* (o) : output value   */
    Word32 L_x          /* (i) : input value    */
)
{
    Int i = 0;
    Word16 exp, a, tmp;
    Word32 L_y;

     
    if (L_x <= (Word32) 0)
        return ((Word32) 0x3fffffffL);

    exp = norm_l (L_x);
    L_x = L_shl (L_x, exp);     /* L_x is normalize */

    exp = sub (30, exp);
      
    if ((exp & 1) == 0)         /* If exponent even -> shift right */
    {
        L_x = L_shr (L_x, 1);
    }
    exp = shr (exp, 1);
    exp = add (exp, 1);

#ifndef USE_BIT_OPS
    L_x = L_shr (L_x, 9);
    i = extract_h (L_x);        /* Extract b25-b31 */
#else
    i = L_x[31:25];
#endif

#ifndef USE_BIT_OPS
     L_x = L_shr (L_x, 1);
     a = extract_l (L_x);        /* Extract b10-b24 */
     a = a & (Word16) 0x7fff;     
#else
     a = 0b @ L_x[24:10];
#endif


    i = sub (i, 16);

    L_y = L_deposit_h (sqrt_table[i]);       /* sqrt_table[i] << 16          */
    tmp = sub (sqrt_table[i], sqrt_table[i + 1]);
    L_y = L_msu (L_y, tmp, a);  /* L_y -=  tmp*a*2         */

    L_y = L_shr (L_y, exp);     /* denormalization */

    return (L_y);
}


/*************************************************************************
 *
 *  FUNCTION:   gmed5
 *
 *  PURPOSE:    calculates 5-point median.
 *
 *  DESCRIPTION:
 *             
 *************************************************************************/

Word16 gmed5 (        /* out      : index of the median value (0...4) */
    Word16 ind[]      /* in       : Past gain values                  */
)
{
    Word16 i, j, ix = 0, tmp[5];
    Word16 max, tmp2[5];

    for (i = 0; i < 5; i++)
    {
        tmp2[i] = ind[i];                                       
    }

    for (i = 0; i < 5; i++)
    {
        max = -8192;                                            
        for (j = 0; j < 5; j++)
        {
            if (sub (tmp2[j], max) >= 0)
            {
                max = tmp2[j];                                  
                ix = j;                                         
            }
        }
        tmp2[ix] = -16384;                                      
        tmp[i] = ix;                                            
    }

    return (ind[tmp[2]]);
}

/*************************************************************************
 *
 *   FUNCTION NAME: interpolate_CN_param
 *
 *   PURPOSE: Interpolate a comfort noise parameter value over the comfort
 *            noise update period.
 *
 *   INPUTS:      old_param     The older parameter of the interpolation
 *                              (the endpoint the interpolation is started
 *                              from)
 *                new_param     The newer parameter of the interpolation
 *                              (the endpoint the interpolation is ended to)
 *                rx_dtx_state  State of the comfort noise insertion period
 *
 *   OUTPUTS:     none
 *
 *   RETURN VALUE: Interpolated CN parameter value
 *
 *************************************************************************/

Word16 interpolate_CN_param (
    Word16 old_param,
    Word16 new_param,
    Word16 rx_dtx_state
)
{
    static const Word16 interp_factor[CN_INT_PERIOD] =
    {
        0x0555, 0x0aaa, 0x1000, 0x1555, 0x1aaa, 0x2000,
        0x2555, 0x2aaa, 0x3000, 0x3555, 0x3aaa, 0x4000,
        0x4555, 0x4aaa, 0x5000, 0x5555, 0x5aaa, 0x6000,
        0x6555, 0x6aaa, 0x7000, 0x7555, 0x7aaa, 0x7fff};
    Word16 temp;
    Word32 L_temp;

    L_temp = L_mult (interp_factor[rx_dtx_state], new_param);
    temp = sub (0x7fff, interp_factor[rx_dtx_state]);
    temp = add (temp, 1);
    L_temp = L_mac (L_temp, temp, old_param);
    temp = round (L_temp);

    return temp;
}

