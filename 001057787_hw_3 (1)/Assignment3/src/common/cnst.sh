/*--------------------------------------------------------------------------*
*       Codec constant parameters                                           *
*---------------------------------------------------------------------------*/

#define NULL 0

#define  L_TOTAL      320       /* Total size of speech buffer.             */
#define  L_WINDOW     240       /* Window size in LP analysis               */
#define  L_FRAME      160       /* Frame size                               */
#define  L_FRAME_BY2  80        /* Frame size divided by 2                  */
#define  L_SUBFR      40        /* Subframe size                            */
#define  M            10        /* Order of LP filter                       */
#define  MP1          (M+1)     /* Order of LP filter + 1                   */
#define  AZ_SIZE      (4*M+4)   /* Size of array of LP filters in 4 subfr.s */
#define  PIT_MIN      18        /* Minimum pitch lag                        */
#define  PIT_MAX      143       /* Maximum pitch lag                        */
#define  L_INTERPOL   (10+1)    /* Length of filter for interpolation       */
#define  MU       26214         /* Factor for tilt compensation filter 0.8  */
#define  AGC_FAC  29491         /* Factor for automatic gain control 0.9    */


#define  PRM_SIZE      57       /* Size of vector of analysis parameters    */
#define BITS_PER_FRAME 244      /* Size of encoded block                    */
#define SAMPLE_WIDTH   13       /* Bit width of input speech samples        */
#define SERIAL_SIZE (BITS_PER_FRAME+1)



/* innovatiove codebook stuff */

#define L_CODE    40
#define NB_TRACK  5
#define NB_PULSE  10
#define STEP      5



#define MAX_32 (Word32)0x7fffffffL
#define MIN_32 (Word32)0x80000000uL

#define MAX_16 (Word16)0x7fff
#define MIN_16 (Word16)0x8000


#define EHF_MASK 0000000000001B /* Encoder Homing Frame pattern */


/*--------------------------------------------------------------------------*
*           definitions of constants used in DTX functions.                 *
*---------------------------------------------------------------------------*/

/* Period when SP=1 although VAD=0. Used for comfort noise averaging */

#define DTX_HANGOVER 7                
                                         


/* Inverse values of DTX hangover period and DTX hangover period + 1 */

#define INV_DTX_HANGOVER (0x7fff / DTX_HANGOVER)
#define INV_DTX_HANGOVER_P1 (0x7fff / (DTX_HANGOVER+1))



/* Encoder DTX control flags */

#define TX_SP_FLAG               0x0001
#define TX_VAD_FLAG              0x0002
#define TX_HANGOVER_ACTIVE       0x0004
#define TX_PREV_HANGOVER_ACTIVE  0x0008
#define TX_SID_UPDATE            0x0010
#define TX_USE_OLD_SID           0x0020


/* Frame classification constants */

#define VALID_SID_FRAME          1
#define INVALID_SID_FRAME        2
#define GOOD_SPEECH_FRAME        3
#define UNUSABLE_FRAME           4

/* Decoder DTX control flags */

#define RX_SP_FLAG               0x0001
#define RX_UPD_SID_QUANT_MEM     0x0002
#define RX_FIRST_SID_UPDATE      0x0004
#define RX_CONT_SID_UPDATE       0x0008
#define RX_LOST_SID_FRAME        0x0010
#define RX_INVALID_SID_FRAME     0x0020
#define RX_NO_TRANSMISSION       0x0040
#define RX_DTX_MUTING            0x0080
#define RX_PREV_DTX_MUTING       0x0100
#define RX_CNI_BFI               0x0200
#define RX_FIRST_SP_FLAG         0x0400

#define PN_INITIAL_SEED 0x70816958L   /* Pseudo noise generator seed value  */

#define CN_INT_PERIOD 24              /* Comfort noise interpolation period
                                         (nbr of frames between successive
                                         SID updates in the decoder) */

/* Constant DTX_ELAPSED_THRESHOLD is used as threshold for allowing
   SID frame updating without hangover period in case when elapsed
   time measured from previous SID update is below 24 */

#define DTX_ELAPSED_THRESHOLD (24 + DTX_HANGOVER - 1)
