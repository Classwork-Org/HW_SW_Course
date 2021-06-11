/* EFR_Coder.sh: Defines and typedefs for the EFR speech encoder simulation library  */


#define NULL 0


/* bit width of the input samples */
#define SAMPLE_WIDTH 13

/* Frame size */
#define  L_FRAME  160       

/* number of bits in the encoded block */
#define BITS_PER_FRAME 244

/* Encoder DTX control flags */
#define TX_SP_FLAG               0x0001
#define TX_VAD_FLAG              0x0002



/* Flag type */
typedef bool Flag;

/* dtx control word */
#ifndef USE_BIT_OPS
    typedef short DTXctrl;
#else
    typedef unsigned bit[5:0] DTXctrl;
#endif

/* machine natural type for loops etc. */
typedef int Int;





