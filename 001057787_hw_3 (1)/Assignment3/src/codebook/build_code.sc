// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: build_code.sc
// ------------------------------------------------------------------------
// 
// Given the pulse positions, build the codeword, encode it for transmission
// and compute the filtered codeword.
//
//
// 09/13/02  <gerstl>	Merged in fixes for input of scrtl tool
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 01/08/02  <gerstl>	Updated to comply with extended port checking
// 08/12/98  <gerstl>

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";



behavior Build_Code (in  Word16 codvec[M], 
		     in  Word16 sign[L_SUBFR],
		     out Word16 cod[L_SUBFR],
		     in  Word16 h[L_SUBFR],
		     out Word16 y[L_SUBFR],
		     out Word16 indx[10])
{
  void main(void) 
  {
    Int i, k;
    Word16 j, track, index, _sign[NB_PULSE], code[L_SUBFR], indices[10];
    Int p0, p1, p2, p3, p4, p5, p6, p7, p8, p9;    
    Word32 s;

    for (i = 0; i < L_CODE; i++)
    {
        code[i] = 0;
    }
    for (i = 0; i < NB_TRACK; i++)
    {
        indices[i] = -1;
    }

    for (k = 0; k < NB_PULSE; k++)
    {
        /* read pulse position */
        i = codvec[k];
        /* read sign           */
        j = sign[i];

        index = mult (i, 6554);                  /* index = pos/5       */
        /* track = pos%5 */
        track = sub (i, extract_l (L_shr (L_mult (index, 5), 1)));

        if (j > 0)
        {
            code[i] = add (code[i], 4096);
            _sign[k] = 8192;

        }
        else
        {
            code[i] = sub (code[i], 4096);
            _sign[k] = -8192;
            index = add (index, 8);
        }


        if (indices[track] < 0)
        {
            indices[track] = index;
        }
        else
        {

            if (((index ^ indices[track]) & 8) == 0)
            {
                /* sign of 1st pulse == sign of 2nd pulse */


                if (sub (indices[track], index) <= 0)
                {
                    indices[track + 5] = index;
                }
                else
                {
                    indices[track + 5] = indices[track];

                    indices[track] = index;
                }
            }
            else
            {
                /* sign of 1st pulse != sign of 2nd pulse */


                if (sub ((indices[track] & 7), (index & 7)) <= 0)
                {
                    indices[track + 5] = indices[track];

                    indices[track] = index;
                }
                else
                {
                    indices[track + 5] = index;
                }
            }
        }
    }
    
    p0 = - codvec[0];
    p1 = - codvec[1];
    p2 = - codvec[2];
    p3 = - codvec[3];
    p4 = - codvec[4];
    p5 = - codvec[5];
    p6 = - codvec[6];
    p7 = - codvec[7];
    p8 = - codvec[8];
    p9 = - codvec[9];
                    
    for (i = 0; i < L_CODE; i++)
    {
	s = 0;
	if (p0 >= 0) s = L_mac (s, h[p0], _sign[0]);
	if (p1 >= 0) s = L_mac (s, h[p1], _sign[1]);
	if (p2 >= 0) s = L_mac (s, h[p2], _sign[2]);
	if (p3 >= 0) s = L_mac (s, h[p3], _sign[3]);
	if (p4 >= 0) s = L_mac (s, h[p4], _sign[4]);
	if (p5 >= 0) s = L_mac (s, h[p5], _sign[5]);
	if (p6 >= 0) s = L_mac (s, h[p6], _sign[6]);
	if (p7 >= 0) s = L_mac (s, h[p7], _sign[7]);
	if (p8 >= 0) s = L_mac (s, h[p8], _sign[8]);
	if (p9 >= 0) s = L_mac (s, h[p9], _sign[9]);
	p0++;
	p1++;
	p2++;
	p3++;
	p4++;
	p5++;
	p6++;
	p7++;
	p8++;
	p9++;
	y[i] = round (s);
    }
    
    // output the data
    for (i = 0; i < 10; i++)
    	indx[i]= indices[i];
    for (i = 0; i < L_CODE; i++)
    	cod[i]  = code[i];
    
  }   
};
