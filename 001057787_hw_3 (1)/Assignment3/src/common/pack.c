#ifndef _WIN32
#include <netinet/in.h>
#else
#include <winsock2.h>
#endif

/* convert from canonical byte order to host endianess */
unsigned short order_bytes(unsigned short s) { return ntohs(s); }
unsigned short unorder_bytes(unsigned short s) { return htons(s); }
