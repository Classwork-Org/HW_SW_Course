#include "EFR_Coder_public.sh"

#include <string.h>

import "std_includes";


Flag arg_handler(int argc, char **argv,
		 char* *speechfile_name, char* *serialfile_name)
{
  Flag dtx_mode = 0;          /* DTX disabled by default               */
  
  /* print a header with some info about the program */
  printf("\n/*******************************************************\n\n");
  printf("     European digital cellular telecommunications system\n");
  printf("                12200 bits/s speech codec for\n");
  printf("          enhanced full rate speech traffic channels\n\n");
  printf("     Bit-Exact SpecC Simulation Code - encoder\n");
  printf("     Version 1.0\n");
  printf("     March 13, 1999\n\n");
  printf("*********************************************************/\n\n");


  /* process command line arguments */
  if ((argc < 3) || (argc > 4))
  {
    printf ("   Usage:\n\n");
    printf( "      coder  speech_file  bitstream_file  <dtx|nodtx>\n");
    printf ("\n");
    exit (1);
  }

  if (argc == 4)
  {
    if (strcmp (argv[3], "nodtx") == 0)
    {
      dtx_mode = 0;
    }
    else if (strcmp (argv[3], "dtx") == 0)
    {
      dtx_mode = 1;
    }
    else
    {
      printf ("\nWrong DTX switch:  %s !!\n", argv[3]);
      exit (1);
    }
  }
  if (dtx_mode == 1)
  {
    printf (" DTX:  enabled\n");
  }
  else
  {
    printf (" DTX:  disabled\n");
  }
  
  *speechfile_name = argv[1];  
  *serialfile_name = argv[2];
  
  return dtx_mode;
}
