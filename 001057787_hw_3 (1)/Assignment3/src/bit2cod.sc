//#include "cnst.sh"
//#include "typedef.sh"

#include <stdio.h>
#include <stdlib.h>

typedef short Word16;

#define BITNO 244+2


behavior Main()

{
  int main (int argc, char *argv[])
    {	      
      FILE *f_bits;
      FILE *f_serial;

      char format[32];
      char buf[BITNO+1];
      Word16 word16bits[BITNO];

      int frames = 0;
      int res, i;

      printf("\n*** bit2cod: convert bit file to cod file ***\n\n");

      if (argc != 3)
	{
	  fprintf (stderr, "Usage: bit2cod bitfile codfile\n\n");
	  exit (1);
	}
      
      if ((f_bits = fopen (argv[1], "r")) == NULL)
	{
	  fprintf (stderr, "Error opening input file %s !\n\n", argv[1]);
	  exit (1);
	}
      
      if ((f_serial = fopen (argv[2], "wb")) == NULL)
	{
	  fprintf (stderr, "Error opening output file %s !\n\n",argv[2]);
	  exit (1);
	}
      
      printf("Input file:  %s\n", argv[1]);
      printf("Output file: %s\n", argv[2]);


      printf("\nConverting");

      sprintf(format, "%%%ds\n", BITNO);
      while( (res = fscanf (f_bits, format, buf)) != EOF )
	{
          if (res != 1) {
            fprintf(stderr, "\nInvalid bit string in line %d\n", frames);
            exit(1);
          }
          
	  for (i=0; i < BITNO; i++)
	    {
              switch(buf[i]) {
                case '0':  word16bits[i] = 0; break;
                case '1':  word16bits[i] = 1; break;
                case '\0': 
                  fprintf(stderr, "\nPremature end of frame %d at bit %d\n",
                                  frames, i);
                  exit(1);
                default:
                  fprintf(stderr, "\nInvalid bit %d in frame %d\n",
                                  i, frames);
                  exit(1);
              }
	    }
	  
	  fwrite (word16bits, sizeof (Word16), BITNO, f_serial);

	  printf("."); fflush(stdout);
	  frames++;
	}

      printf("\n\ndone, %d frames converted.\n", frames);

      fclose (f_bits);
      fclose (f_serial);

      return(0);
    }
};
