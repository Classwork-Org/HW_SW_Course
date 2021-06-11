#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sim.sh>
#include <limits.h>
#include <math.h>

#include <c_typed_queue.sh>


#include "defs.sh"

// row based granularity 
typedef unsigned char tPixel;
typedef tPixel tRowPixel[cols];

typedef float tWeights[KERNEL_DIM][KERNEL_DIM];

signal unsigned int cycle = 0;

// define row queue to get started
DEFINE_IC_TYPED_QUEUE(row, tRowPixel)


// receive from queue and pass local data (for pipelined processing)
behavior QueueRx(i_row_receiver qIn, out tRowPixel rowOut) {
	int row_count = 0;
	void main(){
		qIn.receive(&rowOut);
		// printf("Cycle %u, qrx RCVD ROW: %u\n", cycle,row_count++);
	}
};

// receive pipeline and submit to queue
behavior QueueTx(in tRowPixel rowIn, i_row_sender qOut) {
	int row_count = 0;
	void main(){
		qOut.send(rowIn);
		// printf("Cycle %u, qtx SENT ROW: %u\n", cycle,row_count++);
	}
};


behavior Convolution(in tWeights kernel, in tRowPixel row0, in tRowPixel row1, in tRowPixel row2, out tRowPixel rowOut)
{
	int row_count = 0;
	void main(void)
	{
		int i,j,ii, pos;
		float sum;
	
		sum = 0.5;

		for(j = 0, pos = 1; j<=1 ;j++, pos++)
		{
			sum+=row2[j]*kernel[0][pos];
			sum+=row1[j]*kernel[1][pos];
			sum+=row0[j]*kernel[2][pos];
		}

		sum = (sum > 255)? 255 : sum;
		sum = (sum < 0)? 0 : sum;

		rowOut[0] = sum;
		
		for (i = 1; i < cols-1; ++i)
		{
			// printf("Convolving img: \n");
			// for(ii = i-1; ii<=i+1; ii++)
			// {
			// 	printf("%d ", row0[ii] );
			// }
			// printf("\n");
			// for(ii = i-1; ii<=i+1; ii++)
			// {
			// 	printf("%d ", row1[ii] );
			// }
			// printf("\n");
			// for(ii = i-1; ii<=i+1; ii++)
			// {
			// 	printf("%d ", row2[ii] );
			// }
			// printf("\n");
			

			sum = 0.5;

			for(j = i-1, pos = 0; j<=i+1 ;j++, pos++)
			{
				sum+=row2[j]*kernel[0][pos];
				sum+=row1[j]*kernel[1][pos];
				sum+=row0[j]*kernel[2][pos];
			}
			sum = (sum > 255)? 255 : sum;
			sum = (sum < 0)? 0 : sum;

			rowOut[i] = sum;

			// printf("Output: %d\n", sum);
		}

		sum = 0.5;

		for(j = cols-2, pos = 0; j<=cols-1 ;j++, pos++)
		{
			sum+=row2[j]*kernel[0][pos];
			sum+=row1[j]*kernel[1][pos];
			sum+=row0[j]*kernel[2][pos];
		}

		sum = (sum > 255)? 255 : sum;
		sum = (sum < 0)? 0 : sum;
		rowOut[cols-1] = (sum > 255)? 0 : sum;
		// printf("Cycle %u, conv RCVD ROW: %u\n", cycle,row_count++);
	}
};	

behavior PipeBuffer(in tRowPixel rowIn, out tRowPixel rowOut, in int name)
{
	int row_count = 0;
	void main(void)
	{
		int i;
		for(i = 0; i<cols; i++)
		{
			rowOut[i] = rowIn[i];
		}
		// printf("Cycle %u, PipeBuffer %d Passed row %u\n", cycle, name,row_count++);
	}
};
behavior Magnitude(in tRowPixel row0, in tRowPixel row1, out tRowPixel rowOut)
{
	void main(void)
	{
		int i;
		for(i = 0; i<cols; i++)
		{
			rowOut[i] = sqrt((float)row0[i]*(float)row0[i] + (float)row1[i]*(float)row1[i]);
		}
	}
};

behavior Sobel(in tRowPixel row0, in tRowPixel row1, in tRowPixel row2, out tRowPixel rowOutgx,out tRowPixel rowOutgy)
{
	tWeights gx = {{+1,0,-1},{+2,0,-2},{+1,0,-1}};
	tWeights gy = {{+1,-2,-1},{0,0,0},{+1,2,1}};

	Convolution conv_gx(gx,row0,row1,row2,rowOutgx);
	Convolution conv_gy(gy,row0,row1,row2,rowOutgy);
	void main(void)
	{
		par
		{
			conv_gx;
			conv_gy;
		}
	}
};


// design under test 
behavior DUT(i_row_receiver qIn, i_row_sender qOut) {
	piped tRowPixel rowIn;
	piped tRowPixel rowInL;
	piped tRowPixel rowInLL;
	piped tRowPixel rowOutCanny;
	piped tRowPixel rowOutGx;
	piped tRowPixel rowOutGy;
	piped tRowPixel magxy;
	tWeights kernel = {{+1,0,-1},{+2,0,-2},{+1,0,-1}};
	QueueRx qRx(qIn,rowIn);
	PipeBuffer b1(rowIn, rowInL, 1);
	PipeBuffer b2(rowInL, rowInLL, 2);
	Convolution conv(kernel, rowIn, rowInL, rowInLL, rowOutCanny);
	// Sobel sobel(rowIn, rowInL, rowInLL, rowOutGx, rowOutGy);
	// Magnitude mag(rowOutGx, rowOutGy, rowOutCanny);
	QueueTx qTx(rowOutCanny,qOut);

	void main() {

		pipe(cycle = 0;;cycle++){
			qRx;
			b1;
			b2;
			// sobel;
			conv;
			// mag;
			qTx;
		}
	}
};

// data input proxy 
behavior DataIn(i_row_receiver qIn, i_row_sender qOut) {
	tRowPixel i;


	void main() {
		while (true) {
			qIn.receive(&i);
			qOut.send(i);
		}
	}
};

// data output proxy
behavior DataOut(i_row_receiver qIn, i_row_sender qOut) {
	tRowPixel i;

	void main() {
		while (true) {
			qIn.receive(&i);
			qOut.send(i);
		}
	}
};

behavior Platform(i_row_receiver qIn, i_row_sender qOut) {
	c_row_queue inToCanny(2ul);
	c_row_queue outFromCanny(2ul);

	DataIn dIn(qIn, inToCanny);
	DUT canny(inToCanny, outFromCanny);
	DataOut dOut(outFromCanny, qOut);

	void main() {
		par {dIn; canny; dOut;}
	}
};



behavior Stimulus(i_row_sender q, in char *infilename) {
	unsigned char image[rows][cols];     /* The full input image */

	int read_pgm_image();

	void main() {
		unsigned short rowNr; 
		int i;
		int j;
		tRowPixel blankRow = {0};
		/****************************************************************************
		* Read in the image. This read function allocates memory for the image.
		****************************************************************************/
		if (VERBOSE) printf("Reading the image %s.\n", infilename);
		// reads the complete image into behavior variable image 
		// TODO imporove read row by row
		if (read_pgm_image() == 0) {
			fprintf(stderr, "Error reading the input image, %s.\n", infilename);
			exit(1);
		}

		// for(i = 0; i<rows; i++)
		// {
		// 	for(j = 0; j<cols; j++)
		// 	{
		// 		printf("%d \n", image[i][j]);
		// 	}
		// 	printf("\n");
		// }

		waitfor(IMGTIME);
		q.send(blankRow);

		// send out the data row by row 
		for (rowNr=0;rowNr<rows; rowNr++) {
			q.send(image[rowNr]);
			// printf("Stimulus sent row %d\n",rowNr );
		}

		while(1)
		{
			q.send(blankRow);
		}
		
	}

	/******************************************************************************
	* Function: read_pgm_image
	* Purpose: This function reads in an image in PGM format. The image can be
	* read in from either a file or from standard input. The image is only read
	* from standard input when infilename = NULL. Because the PGM format includes
	* the number of columns and the number of rows in the image, these are read
	* from the file. Memory to store the image is allocated in this function.
	* All comments in the header are discarded in the process of reading the
	* image. Upon failure, this function returns 0, upon sucess it returns 1.
	******************************************************************************/
	int read_pgm_image() {
		FILE *fp;
		char buf[71];

		/***************************************************************************
		* Open the input image file for reading if a filename was given. If no
		* filename was provided, set fp to read from standard input.
		***************************************************************************/
		if (infilename == NULL) fp = stdin;
		else if ((fp = fopen(infilename, "r")) == NULL) {
			fprintf(stderr, "Error reading the file %s in read_pgm_image().\n",
			        infilename);
			return(0);
		}

		/***************************************************************************
		* Verify that the image is in PGM format, read in the number of columns
		* and rows in the image and scan past all of the header information.
		***************************************************************************/
		fgets(buf, 70, fp);
		if (strncmp(buf,"P5",2) != 0) {
			fprintf(stderr, "The file %s is not in PGM format in ", infilename);
			fprintf(stderr, "read_pgm_image().\n");
			if (fp != stdin) fclose(fp);
			return(0);
		}
		do {fgets(buf, 70, fp);} while (buf[0] == '#');  /* skip all comment lines */
		do {fgets(buf, 70, fp);} while (buf[0] == '#');  /* skip all comment lines */

		/***************************************************************************
		* Read the image from the file.
		***************************************************************************/
		if (rows != fread(image, cols, rows, fp)) {
			fprintf(stderr, "Error reading the image data in read_pgm_image().\n");
			if (fp != stdin) fclose(fp);
			return(0);
		}

		if (fp != stdin) fclose(fp);
		return(1);
	}
};



behavior Monitor(i_row_receiver q, in char *outfilename) {
	unsigned char edge[rows][cols];     /* The full output image but with dimensions*/

	int write_pgm_image(const char *comment, int maxval);

	void main() {
		unsigned short rowNr;
		
		// receive the data row by row 
		for (rowNr=0;rowNr<rows; rowNr++) {
			q.receive(&edge[rowNr]);
			// printf("MONITOR RCVD ROW: %d\n",rowNr );
		}
		
		printf("LAST ROW RECIEVED AT TIME: %llu\n", now());

		/****************************************************************************
		* Write out the edge image to a file.
		****************************************************************************/

		if (write_pgm_image("Created by IrfanView", 255) == 0) {
			fprintf(stderr, "Error writing the edge image, %s.\n", outfilename);
			exit(1);
		}

		exit(0);
	}

	/******************************************************************************
	* Function: write_pgm_image
	* Purpose: This function writes an image in PGM format. The file is either
	* written to the file specified by outfilename or to standard output if
	* outfilename = NULL. A comment can be written to the header if coment != NULL.
	******************************************************************************/
	int write_pgm_image(const char *comment, int maxval) {
		FILE *fp;

		/***************************************************************************
		* Open the output image file for writing if a filename was given. If no
		* filename was provided, set fp to write to standard output.
		***************************************************************************/
		if(outfilename == NULL) fp = stdout;
		else if ((fp = fopen(outfilename, "w")) == NULL) {
			fprintf(stderr, "Error writing the file %s in write_pgm_image().\n",
			        outfilename);
			return(0);
		}

		/***************************************************************************
		* Write the header information to the PGM file.
		***************************************************************************/
		fprintf(fp, "P5\n");
		if (comment != NULL && strlen(comment) <= 70) fprintf(fp, "# %s\n", comment);
		fprintf(fp, "%d %d\n", cols, rows);
		fprintf(fp, "%d\n", maxval);

		/***************************************************************************
		* Write the image data to the file.
		***************************************************************************/
		if (rows != fwrite(edge, cols, rows, fp)) {
			fprintf(stderr, "Error writing the image data in write_pgm_image().\n");
			if (fp != stdout) fclose(fp);
			return(0);
		}

		if (fp != stdout) fclose(fp);
		return(1);
	}
};


// simple counting simtulus to have known dimentions 
behavior Stimulus_Count(i_row_sender q, in char *infilename) {

	tRowPixel outRow;
	tRowPixel blankRow = {0};
	void main(void){
		unsigned int x,y;

		// generate pixels with the numbering scheme of
		// hexa decimal output first byte row, second byte column 
		// only works for images up to 16 cols wide
		// printf("stim sent blank row %d\n", -1);
		q.send(blankRow);
		waitfor(1);

		for(y=0;y<rows;y++) {
			// printf("stim sent row %u\n", y);
			for(x=0;x<cols;x++) {
				outRow[x] = (unsigned char) y*16 +x;
				// printf("%d ", (unsigned char) y*10 +x);
			}
			// printf("\n");
			q.send(outRow);
			waitfor(1);
		}

		while(1)
		{
			// printf("stim sent blank row %d\n", -1);
			q.send(blankRow);
			waitfor(1);
		}
	}
};

behavior Monitor_Count(i_row_receiver q, in char *outfilename) {

	tRowPixel edge;
	void main() {
		unsigned short rowNr,x;
		
		// receive the data row by row and print on console 
		for (rowNr=0;rowNr<rows; rowNr++) {
			q.receive(&edge);
			printf("RX %d: ", rowNr);
			for (x=0;x<cols;x++) {
				printf("%.02x ",edge[x]);
			}
			printf(" (%llu)\n",now());
		}
		
		exit(0);
	}
};



behavior Main {
	c_row_queue inToPlatform(2ul);
	c_row_queue outFromPlatform(2ul);

	char *infilename;
	char *outfilename;

	Platform plat(inToPlatform, outFromPlatform);
#ifdef COUNT_STIM
	Stimulus_Count stim(inToPlatform, infilename);
	Monitor_Count mon(outFromPlatform, outfilename);
#else
	Stimulus stim(inToPlatform, infilename);
	Monitor mon(outFromPlatform, outfilename);
#endif
	void remove_suffix(char *s, const char *suf);

	int main(int argc, char *argv[]) {
		char outfilename_buf[128];
		char filename[128];  /* Name of the input image without extension */
		char fNameDefault[] = "../img/beachbus.pgm";

		/****************************************************************************
		* Get the command line arguments.
		****************************************************************************/
		if (argc < 2) {
			// if no file name given, assume default filenname
			infilename = fNameDefault;
		} else {
			infilename = argv[1];
		}

		outfilename = 0;

		if (strcmp(infilename, "-") == 0) {
			// use stdin
			infilename = 0;
			outfilename = 0;
		} else {
			// get the filename without pgm extension
			strncpy(filename, infilename, sizeof(filename));
			filename[sizeof(filename) - 1] = 0;
			remove_suffix(filename, ".pgm");
			snprintf(outfilename_buf, sizeof(outfilename_buf), "%s-edges.pgm", filename);
			outfilename = outfilename_buf;
		}

		par {stim; plat; mon;}

		return 0;
	}

	// remove suffix suf from string s, if it exists.
	// otherwise, don't modify s.
	// s and suf must be null terminated strings, *or else*.
	void remove_suffix(char *s, const char *suf) {
		size_t len_s;
		size_t len_suf;
		size_t offset;

		len_s = strlen(s);
		len_suf = strlen(suf);

		if (len_suf < len_s) {
			offset = len_s - len_suf;
			if (strcmp(s + offset, suf) == 0) s[offset] = 0;
		}
	}
};
