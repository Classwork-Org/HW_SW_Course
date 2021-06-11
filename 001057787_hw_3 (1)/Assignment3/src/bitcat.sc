#include <stdio.h>
#include <stdlib.h>

#define BITNO 244+2


behavior Main()

{
  int main (int argc, char *argv[])
    {	      
      FILE *f_bits;

      char format[32];
      char buf[BITNO+1];

      int frames = 0;
      int res, i;

      printf("\n*** bitcat: display contents of bit file ***\n\n");

      if (argc != 2)
	{
	  fprintf (stderr, "Usage: bitcat bitfile\n\n");
	  exit (1);
	}
      
      if ((f_bits = fopen (argv[1], "r")) == NULL)
	{
	  fprintf (stderr, "Error opening input file %s !\n\n", argv[1]);
	  exit (1);
	}
      
      
      printf("Input file:  %s\n\n", argv[1]);

      sprintf(format, "%%%ds\n", BITNO);
      while( (res = fscanf (f_bits, format, buf)) != EOF )
	{
          if (res != 1) {
            fprintf(stderr, "\nInvalid bit string in line %d\n", frames);
            exit(1);
          }
          
	  printf("frame %d:\n", ++frames);	

	  for (i=0; i < BITNO; i++)
	    {
              switch(buf[i]) {
                case '0':
                case '1':  printf("%c ", buf[i]); break;
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
	  printf("\n\n");
	}


      fclose (f_bits);
      return(0);
    }
};
