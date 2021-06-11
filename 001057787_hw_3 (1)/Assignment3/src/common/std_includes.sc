#include <stdio.h>
#include <stdlib.h>

/* convert from canonical byte order to host endianess */
unsigned short order_bytes(unsigned short s);
unsigned short unorder_bytes(unsigned short s);
