// "Canny" edge detector code:
// ---------------------------

// This text file contains the source code for a "Canny" edge detector. It
// was written by Mike Heath (heath@csee.usf.edu) using some pieces of a
// Canny edge detector originally written by someone at Michigan State
// University.

// There are three 'C' source code files in this text file. They are named
// "canny_edge.c", "hysteresis.c" and "pgm_io.c". They were written and compiled
// under SunOS 4.1.3. Since then they have also been compiled under Solaris.
// To make an executable program: (1) Separate this file into three files with
// the previously specified names, and then (2) compile the code using

//   gcc -o canny_edge canny_edge.c hysteresis.c pgm_io.c -lm
//   (Note: You can also use optimization such as -O3)

// The resulting program, canny_edge, will process images in the PGM format.
// Parameter selection is left up to the user. A broad range of parameters to
// use as a starting point are: sigma 0.60-2.40, tlow 0.20-0.50 and,
// thigh 0.60-0.90.

// If you are using a Unix system, PGM file format conversion tools can be found
// at ftp://wuarchive.wustl.edu/graphics/graphics/packages/pbmplus/.
// Otherwise, it would be easy for anyone to rewrite the image I/O procedures
// because they are listed in the separate file pgm_io.c.

// If you want to check your compiled code, you can download grey-scale and edge
// images from http://marathon.csee.usf.edu/edge/edge_detection.html. You can use
// the parameters given in the edge filenames and check whether the edges that
// are output from your program match the edge images posted at that address.

// Mike Heath
// (10/29/96)

// <------------------------- begin canny_edge.c ------------------------->
/*******************************************************************************
* --------------------------------------------
*(c) 2001 University of South Florida, Tampa
* Use, or copying without permission prohibited.
* PERMISSION TO USE
* In transmitting this software, permission to use for research and
* educational purposes is hereby granted.  This software may be copied for
* archival and backup purposes only.  This software may not be transmitted
* to a third party without prior permission of the copyright holder. This
* permission may be granted only by Mike Heath or Prof. Sudeep Sarkar of
* University of South Florida (sarkar@csee.usf.edu). Acknowledgment as
* appropriate is respectfully requested.
* 
*  Heath, M., Sarkar, S., Sanocki, T., and Bowyer, K. Comparison of edge
*    detectors: a methodology and initial study, Computer Vision and Image
*    Understanding 69 (1), 38-54, January 1998.
*  Heath, M., Sarkar, S., Sanocki, T. and Bowyer, K.W. A Robust Visual
*    Method for Assessing the Relative Performance of Edge Detection
*    Algorithms, IEEE Transactions on Pattern Analysis and Machine
*    Intelligence 19 (12),  1338-1359, December 1997.
*  ------------------------------------------------------
*
* PROGRAM: canny_edge
* PURPOSE: This program implements a "Canny" edge detector. The processing
* steps are as follows:
*
*   1) Convolve the image with a separable gaussian filter.
*   2) Take the dx and dy the first derivatives using [-1,0,1] and [1,0,-1]'.
*   3) Compute the magnitude: sqrt(dx*dx+dy*dy).
*   4) Perform non-maximal suppression.
*   5) Perform hysteresis.
*
* The user must input three parameters. These are as follows:
*
*   sigma = The standard deviation of the gaussian smoothing filter.
*   tlow  = Specifies the low value to use in hysteresis. This is a 
*           fraction (0-1) of the computed high threshold edge strength value.
*   thigh = Specifies the high value to use in hysteresis. This fraction (0-1)
*           specifies the percentage point in a histogram of the gradient of
*           the magnitude. Magnitude values of zero are not counted in the
*           histogram.
*
* NAME: Mike Heath
*       Computer Vision Laboratory
*       University of South Floeida
*       heath@csee.usf.edu
*
* DATE: 2/15/96
*
* Modified: 5/17/96 - To write out a floating point RAW headerless file of
*                     the edge gradient "up the edge" where the angle is
*                     defined in radians counterclockwise from the x direction.
*                     (Mike Heath)
*******************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#define VERBOSE 0
#define BOOSTBLURFACTOR 90.00

// The dimensions of the input image.
#define rows 240
#define cols 320

// Standard deviation of the gaussian kernel.
#define sigma 0.6

// Fraction of the high threshold in hysteresis.
#define tlow 0.3

// High hysteresis threshold control. The actual
// threshold is the (100 * thigh) percentage point
// in the histogram of the magnitude of the
// gradient image that passes non-maximal
// suppression. 
#define thigh 0.8
#define maxval 255
#define WINSIZE 21

int read_pgm_image(char *infilename, unsigned char *image);
int write_pgm_image(char *outfilename, unsigned char *image);
void canny(unsigned char *image, unsigned char *edge);
void gaussian_smooth(unsigned char *image, short int *smoothedim);
int make_gaussian_kernel(float *kernel);
void derrivative_x_y(short int *smoothedim, short int *delta_x, short int *delta_y);
void magnitude_x_y(short int *delta_x, short int *delta_y, short int *magnitude);
void apply_hysteresis(short int *mag, unsigned char *nms, unsigned char *edge);
void follow_edges(unsigned char *edgemapptr, short *edgemagptr, short lowval);
void non_max_supp(short *mag, short *gradx, short *grady, unsigned char *result);

behavior Main(void)
{
   int main(int argc, char *argv[])
   {
      char *infilename = 0;               /* Name of the input image */
      char outfilename[128];              /* Name of the output "edge" image */
      unsigned char image[rows*cols];     /* The input image */
      unsigned char edge[rows*cols];      /* The output edge image */

      /****************************************************************************
      * Get image file name.
      ****************************************************************************/
      if(argc < 2)
      {
         printf("%s\n", "PLEASE SPECIFY INPUT IMAGE");
         return -1;
      }
      printf("%s\n", argv[1]);
      infilename = argv[1];

      /****************************************************************************
      * Read in the image.
      ****************************************************************************/
      if(VERBOSE) 
      {
         printf("Reading the image %s.\n", infilename);
      }      
      if(read_pgm_image(infilename, image) == 0){
         fprintf(stderr, "Error reading the input image, %s.\n", infilename);
         return -1;
      }

      /****************************************************************************
      * Perform the edge detection. All of the work takes place here.
      ****************************************************************************/
      if(VERBOSE)
      {
         printf("Starting Canny edge detection.\n");
      } 
      canny(image, edge);

      // ***************************************************************************
      // * Write out the edge image to a file.
      // ***************************************************************************
      sprintf(outfilename, "%s_s_%3.2f_l_%3.2f_h_%3.2f.pgm", infilename, sigma, tlow, thigh);
      if(VERBOSE)
      {
         printf("Writing the edge iname in the file %s.\n", outfilename);
      } 
      if(write_pgm_image(outfilename, edge) == 0){
         fprintf(stderr, "Error writing the edge image, %s.\n", outfilename);
         return 1;
      }
      return 0;
   }
};


// /*******************************************************************************
// * PROCEDURE: canny
// * PURPOSE: To perform canny edge detection.
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
void canny(unsigned char *image, unsigned char *edge)
{
   unsigned char nms[rows*cols] = {0};        /* Points that are local maximal magnitude. */
   short int smoothedim[rows*cols] = {0},     /* The image after gaussian smoothing.      */
             delta_x[rows*cols] = {0},        /* The first devivative image, x-direction. */
             delta_y[rows*cols] = {0},        /* The first derivative image, y-direction. */
             magnitude[rows*cols] = {0};      /* The magnitude of the gadient image.      */

   /****************************************************************************
   * Perform gaussian smoothing on the image using the input standard
   * deviation.
   ****************************************************************************/
   if(VERBOSE) {
      printf("Smoothing the image using a gaussian kernel.\n");
   }
   gaussian_smooth(image, smoothedim);
   /****************************************************************************
   * Compute the first derivative in the x and y directions.
   ****************************************************************************/
   if(VERBOSE) 
   {
      printf("Computing the X and Y first derivatives.\n");
   }
   derrivative_x_y(smoothedim, delta_x, delta_y);


   // /****************************************************************************
   // * Compute the magnitude of the gradient.
   // ****************************************************************************/
   if(VERBOSE)
   {
      printf("Computing the magnitude of the gradient.\n");
   }
   magnitude_x_y(delta_x, delta_y, magnitude);


   // /****************************************************************************
   // * Perform non-maximal suppression.
   // ****************************************************************************/
   if(VERBOSE)
   {
      printf("Doing the non-maximal suppression.\n");
   }
   non_max_supp(magnitude, delta_x, delta_y, nms);

   // /****************************************************************************
   // * Use hysteresis to mark the edge pixels.
   // ****************************************************************************/
   if(VERBOSE)
   {
      printf("Doing hysteresis thresholding.\n");
   }
   apply_hysteresis(magnitude, nms, edge);

}


// /*******************************************************************************
// * PROCEDURE: magnitude_x_y
// * PURPOSE: Compute the magnitude of the gradient. This is the square root of
// * the sum of the squared derivative values.
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
void magnitude_x_y(short int *delta_x, short int *delta_y, short int *magnitude)
{
   int r, c, pos, sq1, sq2;

   for(r=0,pos=0;r<rows;r++){
      for(c=0;c<cols;c++,pos++){
         sq1 = (int)delta_x[pos] * (int)delta_x[pos];
         sq2 = (int)delta_y[pos] * (int)delta_y[pos];
         (magnitude)[pos] = (short)(0.5 + sqrt((float)sq1 + (float)sq2));
      }
   }

}

// /*******************************************************************************
// * PROCEDURE: derrivative_x_y
// * PURPOSE: Compute the first derivative of the image in both the x any y
// * directions. The differential filters that are used are:
// *
// *                                          -1
// *         dx =  -1 0 +1     and       dy =  0
// *                                          +1
// *
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
void derrivative_x_y(short int *smoothedim, short int *delta_x, short int *delta_y)
{
   int r, c, pos;

   /****************************************************************************
   * Compute the x-derivative. Adjust the derivative at the borders to avoid
   * losing pixels.
   ****************************************************************************/
   if(VERBOSE) printf("   Computing the X-direction derivative.\n");
   for(r=0;r<rows;r++){
      pos = r * cols;
      (delta_x)[pos] = smoothedim[pos+1] - smoothedim[pos];
      pos++;
      for(c=1;c<(cols-1);c++,pos++){
         (delta_x)[pos] = smoothedim[pos+1] - smoothedim[pos-1];
      }
      (delta_x)[pos] = smoothedim[pos] - smoothedim[pos-1];
   }

   /****************************************************************************
   * Compute the y-derivative. Adjust the derivative at the borders to avoid
   * losing pixels.
   ****************************************************************************/
   if(VERBOSE) printf("   Computing the Y-direction derivative.\n");
   for(c=0;c<cols;c++){
      pos = c;
      (delta_y)[pos] = smoothedim[pos+cols] - smoothedim[pos];
      pos += cols;
      for(r=1;r<(rows-1);r++,pos+=cols){
         (delta_y)[pos] = smoothedim[pos+cols] - smoothedim[pos-cols];
      }
      (delta_y)[pos] = smoothedim[pos] - smoothedim[pos-cols];
   }
}

// /*******************************************************************************
// * PROCEDURE: gaussian_smooth
// * PURPOSE: Blur an image with a gaussian filter.
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
void gaussian_smooth(unsigned char *image, short int *smoothedim)
{

   int r, c, rr, cc,     /* Counter variables. */
      center;            /* Half of the windowsize. */
   float tempim[rows*cols] = {0},        /* Buffer for separable filter gaussian smoothing. */
         kernel[WINSIZE] = {0},        /* A one dimensional gaussian kernel. */
         dot,            /* Dot product summing variable. */
         sum;            /* Sum of the kernel weights variable. */
   int windowsize;



   /****************************************************************************
   * Create a 1-dimensional gaussian smoothing kernel.
   ****************************************************************************/
   if(VERBOSE)
   {
         printf("Computing the gaussian smoothing kernel.\n");
   }
   windowsize = make_gaussian_kernel(kernel);
   center = windowsize / 2;

   /****************************************************************************
   * Blur in the x - direction.
   ****************************************************************************/
   if(VERBOSE) printf("   Bluring the image in the X-direction.\n");
   for(r=0;r<rows;r++){
      for(c=0;c<cols;c++){
         dot = 0.0;
         sum = 0.0;
         for(cc=(-center);cc<=center;cc++){ //you're starting at negative the center
            if(((c+cc) >= 0) && ((c+cc) < cols)){
               dot += (float)image[r*cols+(c+cc)] * kernel[center+cc]; 
               sum += kernel[center+cc];
            }
         }
         tempim[r*cols+c] = dot/sum;
      }
   }

   /****************************************************************************
   * Blur in the y - direction.
   ****************************************************************************/
   if(VERBOSE) printf("   Bluring the image in the Y-direction.\n");
   for(c=0;c<cols;c++){
      for(r=0;r<rows;r++){
         sum = 0.0;
         dot = 0.0;
         for(rr=(-center);rr<=center;rr++){
            if(((r+rr) >= 0) && ((r+rr) < rows)){
               dot += tempim[(r+rr)*cols+c] * kernel[center+rr];
               sum += kernel[center+rr];
            }
         }

         smoothedim[r*cols+c] = (short int)(((dot*BOOSTBLURFACTOR/sum + 0.5)));

      }
   }
}

// /*******************************************************************************
// * PROCEDURE: make_gaussian_kernel
// * PURPOSE: Create a one dimensional gaussian kernel.
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
int make_gaussian_kernel(float *kernel)
{
   int i, center;
   float x, fx, sum=0.0;

   int windowsize;
   windowsize = 1 + 2 * ceil(2.5 * sigma);
   center = (windowsize) / 2;

   for(i=0;i<(windowsize);i++){
      x = (float)(i - center);
      fx = pow(2.71828, -0.5*x*x/(sigma*sigma)) / (sigma * sqrt(6.2831853));
      kernel[i] = fx;
      sum += fx;
   }

   for(i=0;i<(windowsize);i++) kernel[i] /= sum;

   if(VERBOSE){
      printf("The filter coefficients are:\n");
      for(i=0;i<(windowsize);i++)
         printf("kernel[%d] = %f\n", i, kernel[i]);
   }

   return windowsize;
}
// // <------------------------- end canny_edge.c ------------------------->

// // <------------------------- begin hysteresis.c ------------------------->
// /*******************************************************************************
// * FILE: hysteresis.c
// * This code was re-written by Mike Heath from original code obtained indirectly
// * from Michigan State University. heath@csee.usf.edu (Re-written in 1996).
// *******************************************************************************/

// //#define VERBOSE 0

#define NOEDGE 255
#define POSSIBLE_EDGE 128
#define EDGE 0

// /*******************************************************************************
// * PROCEDURE: follow_edges
// * PURPOSE: This procedure edges is a recursive routine that traces edgs along
// * all paths whose magnitude values remain above some specifyable lower
// * threshhold.
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
void follow_edges(unsigned char *edgemapptr, short *edgemagptr, short lowval)
{
   short *tempmagptr;
   unsigned char *tempmapptr;
   int i;
   int x[8] = {1,1,0,-1,-1,-1,0,1},
       y[8] = {0,1,1,1,0,-1,-1,-1};

   for(i=0;i<8;i++){
      tempmapptr = edgemapptr - y[i]*cols + x[i];
      tempmagptr = edgemagptr - y[i]*cols + x[i];

      if((*tempmapptr == POSSIBLE_EDGE) && (*tempmagptr > lowval)){
         *tempmapptr = (unsigned char) EDGE;
         follow_edges(tempmapptr,tempmagptr, lowval);
      }
   }
}

// /*******************************************************************************
// * PROCEDURE: apply_hysteresis
// * PURPOSE: This routine finds edges that are above some high threshhold or
// * are connected to a high pixel by a path of pixels greater than a low
// * threshold.
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
void apply_hysteresis(short int *mag, unsigned char *nms, unsigned char *edge)
{
   int r;
   int c;
   int pos;
   int numedges;
   int highcount;
   int lowthreshold;
   int highthreshold;
   int hist[32768];
   short int maximum_mag;

   /****************************************************************************
   * Initialize the edge map to possible edges everywhere the non-maximal
   * suppression suggested there could be an edge except for the border. At
   * the border we say there can not be an edge because it makes the
   * follow_edges algorithm more efficient to not worry about tracking an
   * edge off the side of the image.
   ****************************************************************************/
   for(r=0,pos=0;r<rows;r++){
      for(c=0;c<cols;c++,pos++){
	 if(nms[pos] == POSSIBLE_EDGE) edge[pos] = POSSIBLE_EDGE;
	 else edge[pos] = NOEDGE;
      }
   }

   for(r=0,pos=0;r<rows;r++,pos+=cols){
      edge[pos] = NOEDGE;
      edge[pos+cols-1] = NOEDGE;
   }
   pos = (rows-1) * cols;
   for(c=0;c<cols;c++,pos++){
      edge[c] = NOEDGE;
      edge[pos] = NOEDGE;
   }

   /****************************************************************************
   * Compute the histogram of the magnitude image. Then use the histogram to
   * compute hysteresis thresholds.
   ****************************************************************************/
   for(r=0;r<32768;r++) hist[r] = 0;
   for(r=0,pos=0;r<rows;r++){
      for(c=0;c<cols;c++,pos++){
	 if(edge[pos] == POSSIBLE_EDGE) hist[mag[pos]]++;
      }
   }

   /****************************************************************************
   * Compute the number of pixels that passed the nonmaximal suppression.
   ****************************************************************************/
   for(r=1,numedges=0;r<32768;r++){
      if(hist[r] != 0) maximum_mag = r;
      numedges += hist[r];
   }

   highcount = (int)(numedges * thigh + 0.5);

   /****************************************************************************
   * Compute the high threshold value as the (100 * thigh) percentage point
   * in the magnitude of the gradient histogram of all the pixels that passes
   * non-maximal suppression. Then calculate the low threshold as a fraction
   * of the computed high threshold value. John Canny said in his paper
   * "A Computational Approach to Edge Detection" that "The ratio of the
   * high to low threshold in the implementation is in the range two or three
   * to one." That means that in terms of this implementation, we should
   * choose tlow ~= 0.5 or 0.33333.
   ****************************************************************************/
   r = 1;
   numedges = hist[1];
   while((r<(maximum_mag-1)) && (numedges < highcount)){
      r++;
      numedges += hist[r];
   }
   highthreshold = r;
   lowthreshold = (int)(highthreshold * tlow + 0.5);

   if(VERBOSE){
      printf("The input low and high fractions of %f and %f computed to\n",
	 tlow, thigh);
      printf("magnitude of the gradient threshold values of: %d %d\n",
	 lowthreshold, highthreshold);
   }

   /****************************************************************************
   * This loop looks for pixels above the highthreshold to locate edges and
   * then calls follow_edges to continue the edge.
   ****************************************************************************/
   for(r=0,pos=0;r<rows;r++){
      for(c=0;c<cols;c++,pos++){
	 if((edge[pos] == POSSIBLE_EDGE) && (mag[pos] >= highthreshold)){
            edge[pos] = EDGE;
            follow_edges((edge+pos), (mag+pos), lowthreshold);
	 }
      }
   }

   /****************************************************************************
   * Set all the remaining possible edges to non-edges.
   ****************************************************************************/
   for(r=0,pos=0;r<rows;r++){
      for(c=0;c<cols;c++,pos++) if(edge[pos] != EDGE) edge[pos] = NOEDGE;
   }
}

// /*******************************************************************************
// * PROCEDURE: non_max_supp
// * PURPOSE: This routine applies non-maximal suppression to the magnitude of
// * the gradient image.
// * NAME: Mike Heath
// * DATE: 2/15/96
// *******************************************************************************/
void non_max_supp(short *mag, short *gradx, short *grady, unsigned char *result) 
{
    int rowcount, colcount,count;
    short *magrowptr,*magptr;
    short *gxrowptr,*gxptr;
    short *gyrowptr,*gyptr,z1,z2;
    short m00,gx,gy;
    float mag1,mag2,xperp,yperp;
    unsigned char *resultrowptr, *resultptr;


   /****************************************************************************
   * Zero the edges of the result image.
   ****************************************************************************/
    for(count=0,resultrowptr=result,resultptr=result+cols*(rows-1); 
        count<cols; resultptr++,resultrowptr++,count++){
        *resultrowptr = *resultptr = (unsigned char) 0;
    }

    for(count=0,resultptr=result,resultrowptr=result+cols-1;
        count<rows; count++,resultptr+=cols,resultrowptr+=cols){ // unfortunate array access
        *resultptr = *resultrowptr = (unsigned char) 0;
    }

   /****************************************************************************
   * Suppress non-maximum points.
   ****************************************************************************/
   for(rowcount=1,magrowptr=mag+cols+1,gxrowptr=gradx+cols+1,
      gyrowptr=grady+cols+1,resultrowptr=result+cols+1;
      rowcount<=rows-2; 
      rowcount++,magrowptr+=cols,gyrowptr+=cols,gxrowptr+=cols,
      resultrowptr+=cols){   
      for(colcount=1,magptr=magrowptr,gxptr=gxrowptr,gyptr=gyrowptr,
         resultptr=resultrowptr;colcount<=cols-2; 
         colcount++,magptr++,gxptr++,gyptr++,resultptr++){   
         m00 = *magptr;
         if(m00 == 0){
            *resultptr = (unsigned char) NOEDGE;
         }
         else{
            xperp = -(gx = *gxptr)/((float)m00);
            yperp = (gy = *gyptr)/((float)m00);
         }

         if(gx >= 0){
            if(gy >= 0){
                    if (gx >= gy)
                    {  
                        /* 111 */
                        /* Left point */
                        z1 = *(magptr - 1);
                        z2 = *(magptr - cols - 1);

                        mag1 = (m00 - z1)*xperp + (z2 - z1)*yperp;

                        /* Right point */
                        z1 = *(magptr + 1);
                        z2 = *(magptr + cols + 1);

                        mag2 = (m00 - z1)*xperp + (z2 - z1)*yperp;
                    }
                    else
                    {    
                        /* 110 */
                        /* Left point */
                        z1 = *(magptr - cols);
                        z2 = *(magptr - cols - 1);

                        mag1 = (z1 - z2)*xperp + (z1 - m00)*yperp;

                        /* Right point */
                        z1 = *(magptr + cols);
                        z2 = *(magptr + cols + 1);

                        mag2 = (z1 - z2)*xperp + (z1 - m00)*yperp; 
                    }
                }
                else
                {
                    if (gx >= -gy)
                    {
                        /* 101 */
                        /* Left point */
                        z1 = *(magptr - 1);
                        z2 = *(magptr + cols - 1);

                        mag1 = (m00 - z1)*xperp + (z1 - z2)*yperp;

                        /* Right point */
                        z1 = *(magptr + 1);
                        z2 = *(magptr - cols + 1);

                        mag2 = (m00 - z1)*xperp + (z1 - z2)*yperp;
                    }
                    else
                    {    
                        /* 100 */
                        /* Left point */
                        z1 = *(magptr + cols);
                        z2 = *(magptr + cols - 1);

                        mag1 = (z1 - z2)*xperp + (m00 - z1)*yperp;

                        /* Right point */
                        z1 = *(magptr - cols);
                        z2 = *(magptr - cols + 1);

                        mag2 = (z1 - z2)*xperp  + (m00 - z1)*yperp; 
                    }
                }
            }
            else
            {
                if ((gy = *gyptr) >= 0)
                {
                    if (-gx >= gy)
                    {          
                        /* 011 */
                        /* Left point */
                        z1 = *(magptr + 1);
                        z2 = *(magptr - cols + 1);

                        mag1 = (z1 - m00)*xperp + (z2 - z1)*yperp;

                        /* Right point */
                        z1 = *(magptr - 1);
                        z2 = *(magptr + cols - 1);

                        mag2 = (z1 - m00)*xperp + (z2 - z1)*yperp;
                    }
                    else
                    {
                        /* 010 */
                        /* Left point */
                        z1 = *(magptr - cols);
                        z2 = *(magptr - cols + 1);

                        mag1 = (z2 - z1)*xperp + (z1 - m00)*yperp;

                        /* Right point */
                        z1 = *(magptr + cols);
                        z2 = *(magptr + cols - 1);

                        mag2 = (z2 - z1)*xperp + (z1 - m00)*yperp;
                    }
                }
                else
                {
                    if (-gx > -gy)
                    {
                        /* 001 */
                        /* Left point */
                        z1 = *(magptr + 1);
                        z2 = *(magptr + cols + 1);

                        mag1 = (z1 - m00)*xperp + (z1 - z2)*yperp;

                        /* Right point */
                        z1 = *(magptr - 1);
                        z2 = *(magptr - cols - 1);

                        mag2 = (z1 - m00)*xperp + (z1 - z2)*yperp;
                    }
                    else
                    {
                        /* 000 */
                        /* Left point */
                        z1 = *(magptr + cols);
                        z2 = *(magptr + cols + 1);

                        mag1 = (z2 - z1)*xperp + (m00 - z1)*yperp;

                        /* Right point */
                        z1 = *(magptr - cols);
                        z2 = *(magptr - cols - 1);

                        mag2 = (z2 - z1)*xperp + (m00 - z1)*yperp;
                    }
                }
            } 

            /* Now determine if the current point is a maximum point */

            if ((mag1 > 0.0) || (mag2 > 0.0))
            {
                *resultptr = (unsigned char) NOEDGE;
            }
            else
            {    
                if (mag2 == 0.0)
                    *resultptr = (unsigned char) NOEDGE;
                else
                    *resultptr = (unsigned char) POSSIBLE_EDGE;
            }
        } 
    }
}
// // <------------------------- end hysteresis.c ------------------------->

// // <------------------------- begin pgm_io.c------------------------->
// /*******************************************************************************
// * FILE: pgm_io.c
// * This code was written by Mike Heath. heath@csee.usf.edu (in 1995).
// *******************************************************************************/

// /******************************************************************************
// * Function: read_pgm_image
// * Purpose: This function reads in an image in PGM format. The image can be
// * read in from either a file or from standard input. The image is only read
// * from standard input when infilename = 0. Because the PGM format includes
// * the number of columns and the number of rows in the image, these are read
// * from the file. Memory to store the image is allocated in this function.
// * All comments in the header are discarded in the process of reading the
// * image. Upon failure, this function returns 0, upon sucess it returns 1.
// ******************************************************************************/
int read_pgm_image(char *infilename, unsigned char *image)
{
   FILE *fp;
   char buf[71];

   /***************************************************************************
   * Open the input image file for reading if a filename was given. If no
   * filename was provided, set fp to read from standard input.
   ***************************************************************************/
   if(infilename == 0)
   {
      fp = stdin;
   }
   else
   {
      if((fp = fopen(infilename, "r")) == 0)
      {
         fprintf(stderr, "Error reading the file %s in read_pgm_image().\n", \
            infilename);
         return(0);
      }
   }

   /***************************************************************************
   * Verify that the image is in PGM format.
   ***************************************************************************/
   fgets(buf, 70, fp);
   if(strncmp(buf,"P5",2) != 0)
   {
      fprintf(stderr, "The file %s is not in PGM format in ", infilename);
      fprintf(stderr, "read_pgm_image().\n");
      if(fp != stdin)
      {
         fclose(fp);
      }
      return(0);
   }

   do
   { 
      fgets(buf, 70, fp); 
   }while(buf[0] == '#');  /* skip all comment lines */
      
   do{
      fgets(buf, 70, fp);
   }while(buf[0] == '#');  /* skip all comment lines */

   /***************************************************************************
   * Read Image and set image buffer.
   ***************************************************************************/
   if((rows) != fread((image), (cols), (rows), fp))
   {
      fprintf(stderr, "Error reading the image data in read_pgm_image().\n");
      if(fp != stdin)
      {
         fclose(fp);
      };
      return(0);
   }

   if(fp != stdin)
   {
      fclose(fp);
   }
   return(1);
}

// /******************************************************************************
// * Function: write_pgm_image
// * Purpose: This function writes an image in PGM format. The file is either
// * written to the file specified by outfilename or to standard output if
// * outfilename = 0. A comment can be written to the header if coment != 0.
// ******************************************************************************/
int write_pgm_image(char *outfilename, unsigned char *image)
{
   FILE *fp;

   /***************************************************************************
   * Open the output image file for writing if a filename was given. If no
   * filename was provided, set fp to write to standard output.
   ***************************************************************************/
   if(outfilename == 0)
   {
         fp = stdout;
   }
   else
   {
      if((fp = fopen(outfilename, "w")) == 0){
         fprintf(stderr, "Error writing the file %s in write_pgm_image().\n",
            outfilename);
         return(0);
      }
   }

   /***************************************************************************
   * Write the header information to the PGM file.
   ***************************************************************************/
   fprintf(fp, "P5\n%d %d\n", cols, rows);
   fprintf(fp, "%d\n", maxval);

   /***************************************************************************
   * Write the image data to the file.
   ***************************************************************************/
   if(rows != fwrite(image, cols, rows, fp)){
      fprintf(stderr, "Error writing the image data in write_pgm_image().\n");
      if(fp != stdout) fclose(fp);
      return(0);
   }

   if(fp != stdout) 
   {
      fclose(fp);
   }
   return(1);
}