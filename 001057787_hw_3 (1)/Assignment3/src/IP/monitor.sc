#include <sim.sh>

#include "EFR_Coder_public.sh"


import "std_includes";



/* position of the VAD and SP bits in the output stream */
#define VAD 244
#define SP  245


interface Imonitor
{
  int open_file (char* serialfile_name);
};


behavior Monitor(
		 in unsigned bit[BITS_PER_FRAME-1:0] serial_bits,
		 in DTXctrl txdtx_ctrl,
		 in event serialbits_ready
		 )
  implements Imonitor
{
  FILE *f_serial;             /* File of serial bits for transmission  */
  long long time_mark = 0;
  
  
  int open_file (char* serialfile_name)
  {
    /* open output file and handle errors */
    if ((f_serial = fopen (serialfile_name, "wb")) == NULL)
    {
      printf ("Error opening output bitstream file %s !!\n", 
	      serialfile_name);
      return (0);
    }
    printf (" Output bitstream file:  %s\n", serialfile_name);
    return (1);
  }
  
  
  void main(void)
  {
    long long f_start = 0;
    
    /* wider bitvector to accommodate VAD and SP flag */
    unsigned bit[BITS_PER_FRAME+1:0] serbits;
    
    
    while(true)
    {
      /* wait for arrival of new encoded block */
      wait serialbits_ready;
      f_start += 20000000;
      
      printf("\t\t encoding delay = %10lld ns", now() - f_start);
	
      /* copy the new block to the wider bitvector */
      serbits[BITS_PER_FRAME-1:0] = serial_bits;
	
      
      /* write the VAD- and SP-flags to file after the speech
       *             parameter bit stream */
      serbits[VAD] = (txdtx_ctrl & TX_VAD_FLAG) != 0;
      serbits[SP]  = (txdtx_ctrl & TX_SP_FLAG)  != 0;
      
      
      /* Write the bit stream to file */
      fwrite (&serbits, sizeof (serbits), 1, f_serial);
    }      
  }
};
