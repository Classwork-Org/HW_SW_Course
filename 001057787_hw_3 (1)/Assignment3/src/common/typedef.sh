/*_____________________
 |                     |
 | Basic types.        |
 |_____________________|
*/

#ifndef USE_BIT_OPS
    typedef short Word16;
    typedef int Word32;
    typedef short DTXctrl;
    typedef bool Flag;
#else
    typedef bit[15:0] Word16;
    typedef bit[31:0] Word32;
    typedef unsigned bit[5:0] DTXctrl;
    typedef unsigned bit[0:0] Flag;
#endif


/* machine natural type for loops etc. */
typedef int Int;

