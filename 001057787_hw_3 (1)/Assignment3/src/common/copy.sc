/*___________________________________________________________________________
 |                                                                           |
 | Behaviors for copying of data (connectors/splitters)                      |
 |___________________________________________________________________________|
*/

#include "cnst.sh"
#include "typedef.sh"


behavior Nop(void)
{
  void main(void)
  {
  }
};



behavior CopySubfr(in  Word16 x[L_SUBFR],
                   out Word16 y[L_SUBFR])
{
    void main(void)
    {
        y = x;
    }
};

