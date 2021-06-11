/****************************************************************************
 *
 *     FUNCTION:  periodicity_update
 *
 *     PURPOSE:   Computes the ptch flag needed for the threshold
 *                adaptation decision for the next frame.
 *
 *     INPUTS:    lags[0..1]       speech encoder long term predictor lags
 *
 *     OUTPUTS:   *ptch            Boolean voiced / unvoiced decision
 *
 *     RETURN VALUE: none
 *
 ***************************************************************************/



#include "cnst.sh"
#include "typedef.sh"

import "reset";

import "basic_op";



#define LTHRESH 2
#define NTHRESH 4


behavior Period_Upd (
     in Word16 lags[2],
     out Flag ptch,        /* flag to indicate a periodic signal component */
     in Flag dtx_mode,		     
     in Flag reset_flag		     
     )
implements Ireset
{
  Word16 oldlagcount, veryoldlagcount, oldlag;


  void init(void)
    {
      /* Initialize periodicity detection variables */
      oldlagcount = 0;
      veryoldlagcount = 0;
      oldlag = 18;
    }

  void reset(void)
  {
    init();
  }

  void main(void)
    {
      Word16 minlag, maxlag, lagcount, temp;
      Int i;

      if (reset_flag) init();

      if (dtx_mode != 1) return;
      
      /*** Run loop for the two halves in the frame ***/

      lagcount = 0;                

      for (i = 0; i <= 1; i++)
	{
	  /*** Search the maximum and minimum of consecutive lags ***/

	   
	  if (sub (oldlag, lags[i]) > 0)
	    {
	      minlag = lags[i];    
	      maxlag = oldlag;     
	    }
	  else
	    {
	      minlag = oldlag;     
	      maxlag = lags[i];    
	    }

	  temp = sub (maxlag, minlag);

	   
	  if (sub (temp, LTHRESH) < 0)
	    {
	      lagcount = add (lagcount, 1);
	    }
	  /*** Save the current LTP lag ***/

	  oldlag = lags[i];        
	}

      /*** Update the veryoldlagcount and oldlagcount ***/

      veryoldlagcount = oldlagcount;
       
      oldlagcount = lagcount;      

      /*** Make ptch decision ready for next frame ***/

      temp = add (oldlagcount, veryoldlagcount);

       
      if (sub (temp, NTHRESH) >= 0)
	{
	  ptch = 1;               
	}
      else
	{
	  ptch = 0;               
	}
    }
};
