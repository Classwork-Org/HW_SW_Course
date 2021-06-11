/*************************************************************************
 *
 *  FUNCTION:   dec_10i40_35bits()
 *
 *  PURPOSE:  Builds the innovative codevector from the received
 *            index of algebraic codebook.
 *
 *   See  c1035pf.c  for more details about the algebraic codebook structure.
 *
 *************************************************************************/

#include "cnst.sh"
#include "typedef.sh"

import "basic_op";


behavior dec_10i40_35bits (
     in  Word16 *index,    /* (i)     : index of 10 pulses (sign+position)       */
     out Word16 code[L_SUBFR] /* (o)     : algebraic (fixed) codebook excitation    */
)
{  
  void main(void)
  {
    Word16 cod[L_SUBFR];
    Word16 i, j, pos1, pos2, sign, tmp;

    const Word16 dgray[8] = {0, 1, 3, 2, 5, 6, 4, 7};
    
    for (i = 0; i < L_CODE; i++)
    {
        cod[i] = 0;                                      
    }

    /* decode the positions and signs of pulses and build the codeword */

    for (j = 0; j < NB_TRACK; j++)
    {
        /* compute index i */

        tmp = index[j];                                 
        i = tmp & 7;                                     
        i = dgray[i];                                    

        i = extract_l (L_shr (L_mult (i, 5), 1));
        pos1 = add (i, j); /* position of pulse "j" */

        i = shr (tmp, 3) & 1;                            
        if (i == 0)
        {
            sign = 4096;                                 /* +1.0 */
        }
        else
        {
            sign = -4096;                                /* -1.0 */
        }

        cod[pos1] = sign;                                

        /* compute index i */

        i = index[add (j, 5)] & 7;                       
        i = dgray[i];                                    
        i = extract_l (L_shr (L_mult (i, 5), 1));

        pos2 = add (i, j);      /* position of pulse "j+5" */

        if (sub (pos2, pos1) < 0)
        {
            sign = negate (sign);
        }
        cod[pos2] = add (cod[pos2], sign);               
    }

    code = cod;
  }
};
