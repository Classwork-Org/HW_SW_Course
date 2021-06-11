/*************************************************************************
 *
 *  FILE NAME:   post_filter.sc
 *
 * Performs adaptive postfiltering on the synthesis speech
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";
import "basic_func";
import "array_op";

import "reset";
import "F_gamma";

import "weight_ai";
import "residu";
import "syn_filt";


  /*************************************************************************
   *
   *  FUNCTION:  agc
   *
   *  PURPOSE: Scales the postfilter output on a subframe basis by automatic
   *           control of the subframe gain.
   *
   *  DESCRIPTION:
   *   sig_out[n] = sig_out[n] * gain[n];
   *   where gain[n] is the gain at the nth sample given by
   *     gain[n] = agc_fac * gain[n-1] + (1 - agc_fac) g_in/g_out
   *   g_in/g_out is the square root of the ratio of energy at the input
   *   and output of the postfilter.
   *
   *************************************************************************/
behavior Agc(
    in  Word16 *sig_in,             /* (i)     : postfilter input signal  */
    in  Word16 sig_tmp[L_SUBFR],    /* (i/o)   : postfilter output signal */
    out Word16 sig_out[L_SUBFR],	     
    in  int    agc_fac)             /* (i)     : AGC factor               */
  implements Ireset
{
  Word16 past_gain;               /* initial value of past_gain = 1.0  */
  
  void init(void)
  {
    past_gain = 4096;
  }

  void reset(void)
  {
    init();
  }
  
  void main(void)
  {
    Word16 i, exp;
    Word16 gain_in, gain_out, g0, gain;
    Word32 s;

    Word16 temp;


    /* calculate gain_out with exponent */

    temp = shr (sig_tmp[0], 2);
    s = L_mult (temp, temp);

    for (i = 1; i < L_SUBFR; i++)
    {
        temp = shr (sig_tmp[i], 2);
        s = L_mac (s, temp, temp);
    }

    if (s == 0)
    {
        past_gain = 0;           
        return;
    }
    exp = sub (norm_l (s), 1);
    gain_out = round (L_shl (s, exp));

    /* calculate gain_in with exponent */

    temp = shr (sig_in[0], 2);
    s = L_mult (temp, temp);

    for (i = 1; i < L_SUBFR; i++)
    {
        temp = shr (sig_in[i], 2);
        s = L_mac (s, temp, temp);
    }

    if (s == 0)
    {
        g0 = 0;                 
    }
    else
    {
        i = norm_l (s);
        gain_in = round (L_shl (s, i));
        exp = sub (exp, i);

        /*---------------------------------------------------*
         *  g0 = (1-agc_fac) * sqrt(gain_in/gain_out);       *
         *---------------------------------------------------*/

        s = L_deposit_l (div_s (gain_out, gain_in));
        s = L_shl (s, 7);       /* s = gain_out / gain_in */
        s = L_shr (s, exp);     /* add exponent */

        s = Inv_sqrt (s);
        i = round (L_shl (s, 9));

        /* g0 = i * (1-agc_fac) */
        g0 = mult (i, sub (32767, agc_fac));
    }

    /* compute gain[n] = agc_fac * gain[n-1]
                        + (1-agc_fac) * sqrt(gain_in/gain_out) */
    /* sig_out[n] = gain[n] * sig_out[n]                        */

    gain = past_gain;        

    for (i = 0; i < L_SUBFR; i++)
    {
        gain = mult (gain, agc_fac);
        gain = add (gain, g0);
        sig_out[i] = extract_h (L_shl (L_mult (sig_tmp[i], gain), 3));
    }

    past_gain = gain;

    return;
  }
};



/*---------------------------------------------------------------*
 *    Postfilter constant parameters (defined in "cnst.h")       *
 *---------------------------------------------------------------*
 *   L_FRAME     : Frame size.                                   *
 *   L_SUBFR     : Sub-frame size.                               *
 *   M           : LPC order.                                    *
 *   MP1         : LPC order+1                                   *
 *   MU          : Factor for tilt compensation filter           *
 *   AGC_FAC     : Factor for automatic gain control             *
 *---------------------------------------------------------------*/

#define L_H 22  /* size of truncated impulse response of A(z/g1)/A(z/g2) */


behavior Post_Filter (
    in  Word16 *syn,    /* in/out: synthesis speech (postfiltered is output)    */
    out Word16 syn_pst[L_SUBFR],
    in  Word16 Az[MP1]) /* input: interpolated LPC parameters in all subframes  */
  implements Ireset
{
  /*------------------------------------------------------------*
   *   static vectors                                           *
   *------------------------------------------------------------*/

  /* inverse filtered synthesis */
  Word16 res2[L_SUBFR];

  /* memory of filter 1/A(z/0.75) */
  Word16 mem_syn_pst[M];


  /*************************************************************************
   *
   *  FUNCTION:  Syn_filt:
   *
   *  PURPOSE:  Perform synthesis filtering through 1/A(z).
   *
   *************************************************************************/

/* m = LPC order == 10 */
#define m 10

  void Syn_post_filt (
    Word16 a[],     /* (i)     : a[m+1] prediction coefficients   (m=10)  */
    Word16 x[],     /* (i)     : input signal                             */
    Word16 y[],     /* (o)     : output signal                            */
    Word16 lg,      /* (i)     : size of filtering                        */
    Word16 mem[],   /* (i/o)   : memory associated with this filtering.   */
    Word16 update   /* (i)     : 0=no update, 1=update of memory.         */
  )
  {
    Word16 i, j;
    Word32 s;
    Word16 tmp[80];   /* This is usually done by memory allocation (lg+m) */
    Word16 *yy;

    /* Copy mem[] to yy[] */

    yy = tmp;                            

    for (i = 0; i < m; i++)
    {
        *yy++ = mem[i];                  
    } 

    /* Do the filtering. */

    for (i = 0; i < lg; i++)
    {
        s = L_mult (x[i], a[0]);
        for (j = 1; j <= m; j++)
        {
            s = L_msu (s, a[j], yy[-j]);
        }
        s = L_shl (s, 3);
        *yy++ = round (s);               
    }

    for (i = 0; i < lg; i++)
    {
        y[i] = tmp[i + m];               
    }

    /* Update of memory if update==1 */

    if (update != 0)
    {
        for (i = 0; i < m; i++)
        {
            mem[i] = y[lg - m + i];      
        }
    }
    return;
  }
  
  
  /*---------------------------------------------------------------------*
   * routine preemphasis()                                               *
   * ~~~~~~~~~~~~~~~~~~~~~                                               *
   * Preemphasis: filtering through 1 - g z^-1                           *
   *---------------------------------------------------------------------*/

  Word16 mem_pre;

  void preemphasis (
    Word16 *signl, /* (i/o)   : input signal overwritten by the output */
    Word16 g,       /* (i)     : preemphasis coefficient                */
    Word16 L        /* (i)     : size of filtering                      */
  )
  {
    Word16 *p1, *p2, temp, i;

    p1 = signl + L - 1;                     
    p2 = p1 - 1;                             
    temp = *p1;                              

    for (i = 0; i <= L - 2; i++)
    {
        *p1 = sub (*p1, mult (g, *p2--));    
        p1--;
    }

    *p1 = sub (*p1, mult (g, mem_pre));      

    mem_pre = temp;                          

    return;
  }
  
  /*************************************************************************
   *  FUNCTION:  Post_Filter()
   *
   *  PURPOSE:  postfiltering of synthesis speech.
   *
   *  DESCRIPTION:
   *      The postfiltering process is described as follows:
   *
   *          - inverse filtering of syn[] through A(z/0.7) to get res2[]
   *          - tilt compensation filtering; 1 - MU*k*z^-1
   *          - synthesis filtering through 1/A(z/0.75)
   *          - adaptive gain control
   *
   *************************************************************************/

  Word16 Ap3[MP1], Ap4[MP1];  /* bandwidth expanded LP parameters */
  Word16 syn_tmp[L_SUBFR];
  
  Weight_Ai weight_ai_3(Az, F_gamma3, Ap3);
  Weight_Ai weight_ai_4(Az, F_gamma4, Ap4);

  Residu    residu(Ap3, syn, res2);
  Syn_Filt  syn_filt(Ap4, res2, syn_tmp, mem_syn_pst, true);
  Agc       agc(syn, syn_tmp, syn_pst, AGC_FAC);
  
  /*************************************************************************
   *
   *  FUNCTION:   Init_Post_Filter
   *
   *  PURPOSE: Initializes the postfilter parameters.
   *
   *************************************************************************/

  void init(void)
  {
    mem_pre = 0;                /* Filter memory */
    
    Set_zero (mem_syn_pst, M);

    Set_zero (res2, L_SUBFR);

    return;
  }

  void reset(void)
  {
    init();
    agc.reset();
  }
  
  void main(void)
  {
    /*-------------------------------------------------------------------*
     *           Declaration of parameters                               *
     *-------------------------------------------------------------------*/

    Word16 h[L_H];

    Word16 i;
    Word16 temp1, temp2;
    Word32 L_tmp;


    /*-----------------------------------------------------*
     * Post filtering                                      *
     *-----------------------------------------------------*/

        /* Find weighted filter coefficients Ap3[] and ap[4] */
      
        weight_ai_3.main();
        weight_ai_4.main();

        /* filtering of synthesis speech by A(z/0.7) to find res2[] */
        residu.main();

        /* tilt compensation filter */

        /* impulse response of A(z/0.7)/A(z/0.75) */

        Copy (Ap3, h, M + 1);
        Set_zero (&h[M + 1], L_H - M - 1);
        Syn_post_filt (Ap4, h, h, L_H, &h[M + 1], 0);

        /* 1st correlation of h[] */

        L_tmp = L_mult (h[0], h[0]);
        for (i = 1; i < L_H; i++)
        {
            L_tmp = L_mac (L_tmp, h[i], h[i]);
        }
        temp1 = extract_h (L_tmp);

        L_tmp = L_mult (h[0], h[1]);
        for (i = 1; i < L_H - 1; i++)
        {
            L_tmp = L_mac (L_tmp, h[i], h[i + 1]);
        }
        temp2 = extract_h (L_tmp);

        if (temp2 <= 0)
        {
            temp2 = 0;           
        }
        else
        {
            temp2 = mult (temp2, MU);
            temp2 = div_s (temp2, temp1);
        }

        preemphasis (res2, temp2, L_SUBFR);

        /* filtering through  1/A(z/0.75) */
        syn_filt.main();

        /* scale output to input */
	agc.main();
  }
};

