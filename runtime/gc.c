#include <stdio.h>
#include <stdlib.h>
#include <string.h>
// "new" a new object, do necessary initializations, and
// return the pointer (reference).

//if define HEAP_MALLOC,use the malloc,else use the large array
#define HEAP_MALLOC
//
#ifdef HEAP_MALLOC
void *Tiger_new (void *vtable, int size)
{
  //// You should write 4 statements for this function.
  //// #1: "malloc" a chunk of memory of size "size":
  void* pt=malloc(size);
  // #2: clear this chunk of memory (zero off it):
  memset(pt,0,size);
  // #3: set up the "vtable" pointer properly:
  memcpy(pt,&vtable,sizeof(vtable));
  // #4: return the pointer 
  return pt;
  //or:
  //
  //int* pt=(int *)malloc(size);
  //memset(pt,0,size);
  //pt[0]=(int)(vtable);
  //return pt;
  //
}
#else
char heap[100000000]={'\0'};
int hp;
void *Tiger_new (void *vtable, int size)
{
int* pt=(int*)(heap+hp);
hp=hp+size;
pt[0]=(int)(vtable);
return pt;
}
#endif