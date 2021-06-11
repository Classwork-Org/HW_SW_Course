#include "typedef.sh"


/*************************************************************************
 *
 *  FUNCTION:   Set zero()
 *
 *  PURPOSE:  Set vector x[] to zero
 *
 *************************************************************************/


void Set_zero (
    Word16 x[],         /* (o)    : vector to clear     */
    Word16 L            /* (i)    : length of vector    */
)
{
    Int i;

    for (i = 0; i < L; i++)
    {
        x[i] = 0;
    }

    return;
}



/*************************************************************************
 *
 *   FUNCTION:   Copy
 *
 *   PURPOSE:   Copy vector x[] to y[]
 *
 *
 *************************************************************************/


void Copy (
    Word16 x[],         /* (i)   : input vector   */
    Word16 y[],         /* (o)   : output vector  */
    Word16 L            /* (i)   : vector length  */
)
{
    Int i;

    for (i = 0; i < L; i++)
    {
        y[i] = x[i];
    }

    return;
}
