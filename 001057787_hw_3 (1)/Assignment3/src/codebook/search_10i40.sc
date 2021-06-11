// ------------------------------------------------------------------------
// GSM EFR Vocoder  -  File: search_10i40.sc
// ------------------------------------------------------------------------
// 
// Search for best pulse positions.
//
//
// 09/13/02  <gerstl>	Merged in fixes for input of scrtl tool
// 01/09/02  <gerstl>	Merged in port splitting from 'arch' branch
// 01/08/02  <gerstl>	Updated to comply with extended port checking
// 07/21/98  <gerstl>
// 08/12/98  <szhao>

#include "typedef.sh"
#include "cnst.sh"

import "basic_op";




#define _1_2    (Word16)(32768L/2)
#define _1_4    (Word16)(32768L/4)
#define _1_8    (Word16)(32768L/8)
#define _1_16   (Word16)(32768L/16)
#define _1_32   (Word16)(32768L/32)
#define _1_64   (Word16)(32768L/64)
#define _1_128  (Word16)(32768L/128)



behavior Search_10i40 (in  Word16 dn[L_SUBFR], 
		       in  Word16 rr[L_SUBFR * L_SUBFR],
		       in  Word16 ipos[M],
		       in  Word16 pos_max[5],
		       out Word16 codvec[M])
{
  void main(void) 
    {
      Word16 i0, i1;
      Int i2, i3, i4, i5, i6, i7, i8, i9;
      Int i, j, k;
      Word16 pos, ia, ib;
      Word16 psk, ps, ps0, ps1, ps2, sq, sq2;
      Word16 alpk, alp, alp_16;
      Word16 rrv[L_CODE];
      Word32 s, alp0, alp1, alp2;
      Word16 my_ipos[M];
      
      // make a local copy of this input
      for(i = 0; i < M; i++) {
        my_ipos[i] = ipos[i];
      }

      /* fix i0 on maximum of correlation position */

      i0 = pos_max[my_ipos[0]];

      /*------------------------------------------------------------------*
       * i1 loop:                                                         *
       *------------------------------------------------------------------*/

      /* Default value */
      psk = -1;
      alpk = 1;
      for (i = 0; i < NB_PULSE; i++)
	{
	  codvec[i] = i;
	}

      for (i = 1; i < NB_TRACK; i++)
	{
	  i1 = pos_max[my_ipos[1]];
	  ps0 = add (dn[i0], dn[i1]);
	  alp0 = L_mult (rr[i0 * L_SUBFR + i0], _1_16);
	  alp0 = L_mac (alp0, rr[i1 * L_SUBFR + i1], _1_16);
	  alp0 = L_mac (alp0, rr[i0 * L_SUBFR + i1], _1_8);

	  /*----------------------------------------------------------------*
	   * i2 and i3 loop:                                                *
	   *----------------------------------------------------------------*/

	  /* initialize 4 indices for next loop. */
	  /* initialize "rr[i3][i3]" pointer */
	  /* initialize "rr[i0][i3]" pointer */
	  /* initialize "rr[i1][i3]" pointer */
	  /* initialize "rrv[i3]" pointer    */

	  for (i3 = my_ipos[3]; i3 < L_CODE; i3 += STEP)
	    {
	      s = L_mult (rr[i3 * L_SUBFR + i3], _1_8);    /* index incr= STEP+L_CODE */
	      s = L_mac (s, rr[i0 * L_SUBFR + i3], _1_4);  /* index increment = STEP  */
	      s = L_mac (s, rr[i1 * L_SUBFR + i3], _1_4);  /* index increment = STEP  */
	      rrv[i3] = round (s);
	    }

	  /* Default value */
	  sq = -1;
	  alp = 1;
	  ps = 0;
	  ia = my_ipos[2];
	  ib = my_ipos[3];

	  /* initialize 4 indices for i2 loop. */
	  /* initialize "dn[i2]" pointer     */
	  /* initialize "rr[i2][i2]" pointer */
	  /* initialize "rr[i0][i2]" pointer */
	  /* initialize "rr[i1][i2]" pointer */

	  for (i2 = my_ipos[2]; i2 < L_CODE; i2 += STEP)
	    {
	      /* index increment = STEP  */
	      ps1 = add (ps0, dn[i2]);

	      /* index incr= STEP+L_CODE */
	      alp1 = L_mac (alp0, rr[i2 * L_SUBFR + i2], _1_16);
	      /* index increment = STEP  */
	      alp1 = L_mac (alp1, rr[i0 * L_SUBFR + i2], _1_8);
	      /* index increment = STEP  */
	      alp1 = L_mac (alp1, rr[i1 * L_SUBFR + i2], _1_8);

	      /* initialize 3 indices for i3 inner loop */
	      /* initialize "dn[i3]" pointer     */
	      /* initialize "rrv[i3]" pointer    */
	      /* initialize "rr[i2][i3]" pointer */

	      for (i3 = my_ipos[3]; i3 < L_CODE; i3 += STEP)
		{
		  /* index increment = STEP */
		  ps2 = add (ps1, dn[i3]);

		  /* index increment = STEP */
		  alp2 = L_mac (alp1, rrv[i3], _1_2);
		  /* index increment = STEP */
		  alp2 = L_mac (alp2, rr[i2 * L_SUBFR + i3], _1_8);

		  sq2 = mult (ps2, ps2);

		  alp_16 = round (alp2);

		  s = L_msu (L_mult (alp, sq2), sq, alp_16);

		  if (s > 0)
		    {
		      sq = sq2;
		      ps = ps2;
		      alp = alp_16;
		      ia = i2;
		      ib = i3;
		    }
		}
	    }
	  i2 = ia;
	  i3 = ib;

	  /*----------------------------------------------------------------*
	   * i4 and i5 loop:                                                *
	   *----------------------------------------------------------------*/

	  ps0 = ps;
	  alp0 = L_mult (alp, _1_2);

	  /* initialize 6 indices for next loop (see i2-i3 loop) */


	  for (i5 = my_ipos[5]; i5 < L_CODE; i5 += STEP)
	    {
	      s = L_mult (rr[i5 * L_SUBFR + i5], _1_8);
	      s = L_mac (s, rr[i0 * L_SUBFR + i5], _1_4);
	      s = L_mac (s, rr[i1 * L_SUBFR + i5], _1_4);
	      s = L_mac (s, rr[i2 * L_SUBFR + i5], _1_4);
	      s = L_mac (s, rr[i3 * L_SUBFR + i5], _1_4);
	      rrv[i5] = round (s);
	    }

	  /* Default value */
	  sq = -1;
	  alp = 1;
	  ps = 0;
	  ia = my_ipos[4];
	  ib = my_ipos[5];

	  /* initialize 6 indices for i4 loop (see i2-i3 loop) */


	  for (i4 = my_ipos[4]; i4 < L_CODE; i4 += STEP)
	    {
	      ps1 = add (ps0, dn[i4]);

	      alp1 = L_mac (alp0, rr[i4 * L_SUBFR + i4], _1_32);
	      alp1 = L_mac (alp1, rr[i0 * L_SUBFR + i4], _1_16);
	      alp1 = L_mac (alp1, rr[i1 * L_SUBFR + i4], _1_16);
	      alp1 = L_mac (alp1, rr[i2 * L_SUBFR + i4], _1_16);
	      alp1 = L_mac (alp1, rr[i3 * L_SUBFR + i4], _1_16);

	      /* initialize 3 indices for i5 inner loop (see i2-i3 loop) */


	      for (i5 = my_ipos[5]; i5 < L_CODE; i5 += STEP)
		{
		  ps2 = add (ps1, dn[i5]);

		  alp2 = L_mac (alp1, rrv[i5], _1_4);
		  alp2 = L_mac (alp2, rr[i4 * L_SUBFR + i5], _1_16);

		  sq2 = mult (ps2, ps2);

		  alp_16 = round (alp2);

		  s = L_msu (L_mult (alp, sq2), sq, alp_16);


		  if (s > 0)
		    {
		      sq = sq2;
		      ps = ps2;
		      alp = alp_16;
		      ia = i4;
		      ib = i5;
		    }
		}
	    }
	  i4 = ia;
	  i5 = ib;

	  /*----------------------------------------------------------------*
	   * i6 and i7 loop:                                                *
	   *----------------------------------------------------------------*/

	  ps0 = ps;
	  alp0 = L_mult (alp, _1_2);

	  /* initialize 8 indices for next loop (see i2-i3 loop) */



	  for (i7 = my_ipos[7]; i7 < L_CODE; i7 += STEP)
	    {
	      s = L_mult (rr[i7 * L_SUBFR + i7], _1_16);
	      s = L_mac (s, rr[i0 * L_SUBFR + i7], _1_8);
	      s = L_mac (s, rr[i1 * L_SUBFR + i7], _1_8);
	      s = L_mac (s, rr[i2 * L_SUBFR + i7], _1_8);
	      s = L_mac (s, rr[i3 * L_SUBFR + i7], _1_8);
	      s = L_mac (s, rr[i4 * L_SUBFR + i7], _1_8);
	      s = L_mac (s, rr[i5 * L_SUBFR + i7], _1_8);
	      rrv[i7] = round (s);
	    }

	  /* Default value */
	  sq = -1;
	  alp = 1;
	  ps = 0;
	  ia = my_ipos[6];
	  ib = my_ipos[7];

	  /* initialize 8 indices for i6 loop (see i2-i3 loop) */

	  for (i6 = my_ipos[6]; i6 < L_CODE; i6 += STEP)
	    {
	      ps1 = add (ps0, dn[i6]);

	      alp1 = L_mac (alp0, rr[i6 * L_SUBFR + i6], _1_64);
	      alp1 = L_mac (alp1, rr[i0 * L_SUBFR + i6], _1_32);
	      alp1 = L_mac (alp1, rr[i1 * L_SUBFR + i6], _1_32);
	      alp1 = L_mac (alp1, rr[i2 * L_SUBFR + i6], _1_32);
	      alp1 = L_mac (alp1, rr[i3 * L_SUBFR + i6], _1_32);
	      alp1 = L_mac (alp1, rr[i4 * L_SUBFR + i6], _1_32);
	      alp1 = L_mac (alp1, rr[i5 * L_SUBFR + i6], _1_32);

	      /* initialize 3 indices for i7 inner loop (see i2-i3 loop) */


	      for (i7 = my_ipos[7]; i7 < L_CODE; i7 += STEP)
		{
		  ps2 = add (ps1, dn[i7]);

		  alp2 = L_mac (alp1, rrv[i7], _1_4);
		  alp2 = L_mac (alp2, rr[i6 * L_SUBFR + i7], _1_32);

		  sq2 = mult (ps2, ps2);

		  alp_16 = round (alp2);

		  s = L_msu (L_mult (alp, sq2), sq, alp_16);


		  if (s > 0)
		    {
		      sq = sq2;
		      ps = ps2;
		      alp = alp_16;
		      ia = i6;
		      ib = i7;
		    }
		}
	    }
	  i6 = ia;
	  i7 = ib;

	  /*----------------------------------------------------------------*
	   * i8 and i9 loop:                                                *
	   *----------------------------------------------------------------*/

	  ps0 = ps;
	  alp0 = L_mult (alp, _1_2);

	  /* initialize 10 indices for next loop (see i2-i3 loop) */



	  for (i9 = my_ipos[9]; i9 < L_CODE; i9 += STEP)
	    {
	      s = L_mult (rr[i9 * L_SUBFR + i9], _1_16);
	      s = L_mac (s, rr[i0 * L_SUBFR + i9], _1_8);
	      s = L_mac (s, rr[i1 * L_SUBFR + i9], _1_8);
	      s = L_mac (s, rr[i2 * L_SUBFR + i9], _1_8);
	      s = L_mac (s, rr[i3 * L_SUBFR + i9], _1_8);
	      s = L_mac (s, rr[i4 * L_SUBFR + i9], _1_8);
	      s = L_mac (s, rr[i5 * L_SUBFR + i9], _1_8);
	      s = L_mac (s, rr[i6 * L_SUBFR + i9], _1_8);
	      s = L_mac (s, rr[i7 * L_SUBFR + i9], _1_8);
	      rrv[i9] = round (s);
	    }

	  /* Default value */
	  sq = -1;
	  alp = 1;
	  ps = 0;
	  ia = my_ipos[8];
	  ib = my_ipos[9];

	  /* initialize 10 indices for i8 loop (see i2-i3 loop) */
	  for (i8 = my_ipos[8]; i8 < L_CODE; i8 += STEP)
	    {
	      ps1 = add (ps0, dn[i8]);

	      alp1 = L_mac (alp0, rr[i8 * L_SUBFR + i8], _1_128);
	      alp1 = L_mac (alp1, rr[i0 * L_SUBFR + i8], _1_64);
	      alp1 = L_mac (alp1, rr[i1 * L_SUBFR + i8], _1_64);
	      alp1 = L_mac (alp1, rr[i2 * L_SUBFR + i8], _1_64);
	      alp1 = L_mac (alp1, rr[i3 * L_SUBFR + i8], _1_64);
	      alp1 = L_mac (alp1, rr[i4 * L_SUBFR + i8], _1_64);
	      alp1 = L_mac (alp1, rr[i5 * L_SUBFR + i8], _1_64);
	      alp1 = L_mac (alp1, rr[i6 * L_SUBFR + i8], _1_64);
	      alp1 = L_mac (alp1, rr[i7 * L_SUBFR + i8], _1_64);

	      /* initialize 3 indices for i9 inner loop (see i2-i3 loop) */


	      for (i9 = my_ipos[9]; i9 < L_CODE; i9 += STEP)
		{
		  ps2 = add (ps1, dn[i9]);

		  alp2 = L_mac (alp1, rrv[i9], _1_8);
		  alp2 = L_mac (alp2, rr[i8 * L_SUBFR + i9], _1_64);

		  sq2 = mult (ps2, ps2);

		  alp_16 = round (alp2);

		  s = L_msu (L_mult (alp, sq2), sq, alp_16);


		  if (s > 0)
		    {
		      sq = sq2;
		      ps = ps2;
		      alp = alp_16;
		      ia = i8;
		      ib = i9;
		    }
		}
	    }

	  /*----------------------------------------------------------------*
	   * memorise codevector if this one is better than the last one.   *
	   *----------------------------------------------------------------*/

	  s = L_msu (L_mult (alpk, sq), psk, alp);


	  if (s > 0)
	    {
	      psk = sq;
	      alpk = alp;
	      codvec[0] = i0;
	      codvec[1] = i1;
	      codvec[2] = i2;
	      codvec[3] = i3;
	      codvec[4] = i4;
	      codvec[5] = i5;
	      codvec[6] = i6;
	      codvec[7] = i7;
	      codvec[8] = ia;
	      codvec[9] = ib;
	    }
	  /*----------------------------------------------------------------*
	   * Cyclic permutation of i1,i2,i3,i4,i5,i6,i7,i8 and i9.          *
	   *----------------------------------------------------------------*/

	  pos = my_ipos[1];
	  for (j = 1, k = 2; k < NB_PULSE; j++, k++)
	    {
	      my_ipos[j] = my_ipos[k];
	    }
	  my_ipos[NB_PULSE - 1] = pos;
	}
 
    }
};


