#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int System_out_println (int i)
{
  printf ("%d\n", i);
  return 0;
}

int* System_malloc(int size)
{
     int* p = (int*)malloc((size+1) * sizeof(int));
     p[0]=size;
     return p + 1;
} 
