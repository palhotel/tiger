#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
// The Gimple Garbage Collector.


//===============================================================//
// The Java Heap data structure.

/*   
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap
{
  int size;         // in bytes, note that this if for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize)
{
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"
	char* p=(char*)malloc(heapSize);
  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.
	heap.size=heapSize/2;
  // #3: initialize the "from" field (with what value?)
	heap.from=&p[0];
  // #4: initialize the "fromFree" field (with what value?)
	heap.fromFree=&p[0];
  // #5: initialize the "to" field (with what value?)
	heap.to=&p[heapSize/2];
  // #6: initizlize the "toStart" field with NULL;
	heap.toStart=heap.to;
  // #7: initialize the "toNext" field with NULL;
	heap.toNext=heap.to;;
  return;
}

// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
// 本页全局变量
void *prev = 0;
int beforegc=0;
int gc_round=0;

//===============================================================//
// Object Model And allocation


// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
   0  | vptr      ---|----> (points to the virtual method table)
      |--------------|
   1  | isObjOrArray | (0: for normal objects)
      |--------------|
   2  | length       | (this field should be empty(byte size) for normal objects)
      |--------------|
   3  | forwarding   | 
      |--------------|\
p---->| v_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)

//if define HEAP_MALLOC,use the malloc,else use the large array

static void Tiger_gc ();
int* obj_mem_alloc(int* pt,int size,void* vtable)
{//为普通对象分配空间,size是字节数,sizeof(int)*4=16
		pt=(int*)heap.fromFree;
		heap.fromFree+=(size+16);
		memset(pt,0,(size+16));
		pt[0]=(int)vtable;
		pt[1]=0;
		pt[2]=size;
		return pt;
}
void *Tiger_new (void *vtable, int size)
{
	int* pt=0;
	int remain=heap.size-(heap.fromFree-heap.from);
	
	if(remain>=(size+16))
	{
		pt=obj_mem_alloc(pt,size,vtable);
	}
	else {
		beforegc=remain;
		Tiger_gc();
		remain=heap.size-(heap.fromFree-heap.from);
		if(remain>=(size+16))
		{
		pt=obj_mem_alloc(pt,size,vtable);
		}
		else {
		printf("OutOfMemory\n");
		exit(0);
		}
	}
  return pt;
}


// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
   0  | vptr         | (this field should be empty for an array)
      |--------------|
   1  | isObjOrArray | (1: for array)
      |--------------|
   2  | length       |
      |--------------|
   3  | forwarding   | 
      |--------------|\
p---->| e_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
int* arr_mem_calloc(int* pt,int length)
{//为数组分配空间,length是数组大小，不是字节数，pt[2]存放数组大小
		pt=(int*)heap.fromFree;
		heap.fromFree+=(length*4+16);
		memset(pt,0,(length*4+16));
		pt[1]=1;
		pt[2]=length;
		return pt;
}
void *Tiger_new_array (int length)
{
	int* pt=0;
	int remain=heap.size-(heap.fromFree-heap.from);
	if(remain>=(length*4+16))
	{
		pt=arr_mem_calloc(pt,length);
	}
	else {
		beforegc=remain;
		Tiger_gc();
		//
		remain=heap.size-(heap.fromFree-heap.from);
		if(remain>=(length*4+16))
		{
		pt=arr_mem_calloc(pt,length);
		}
		else {
		printf("OutOfMemory\n");
		exit(0);
		}
	}
	return pt;
}

//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
void* Tiger_forward(void* p)
{
//两种指针，其实一个地址
	char* ptr=(char*)p;
	int* intptr=(int*)ptr;
	if(ptr!=0&&ptr<(heap.from+heap.size)&&ptr>=heap.from)
	{//p为指向from_space的指针
		if((char*)intptr[3]>=heap.toStart&&(char*)intptr[3]<heap.toNext){//指针p的forwarding指向to_space
			return (void*)intptr[3];//返回forwarding指针(转发指针)
		}
		else{//指针p的forwarding指向from_space，将p指向的字段复制到to_space中
			int nSize=0;
			//分为普通对象和数组两种情况计算要拷贝的字节数nSize
			if(intptr[1]==0)
				nSize=intptr[2]+16;
			else nSize=intptr[2]*4+16;
			//拷贝到to那部分
			//将转发指针指向新的地址
			intptr[3]=(int)heap.toNext;
			memset(heap.toNext,0,nSize);
			memcpy(heap.toNext,p,nSize);
			//printf("memcpy ok");
			
			heap.toNext+=nSize;
			return (void*)intptr[3];
		}
	}
	else return p;//p为非指向from_space的指针或非指针，不处理
}

/*frame的结构
* 0|*prev|遍历用的指针
* 1|arguments_gc_map
* 2|arguments_base_address,从右往左依次进栈的第一个参数this的地址
*		<-| arg3 | arg2 | arg1 | this |
*		<-----3------2------1-----0---
* 3|locals_count
* | local_0的地址
* | local_1的地址
* |.....
*/
static void Tiger_gc ()
{
	gc_round++;//轮数
	time(0);
	clock_t start,end;
	start=clock();
	
	char *scan=heap.toStart;//扫描指针，char*
	//heap.toNext=heap.toStart;

	//root
	int* intptr=(int*)prev;//指向当前Function的frame
	while(intptr!=0)
	{
		
		//形参
		char* argument_maps = (char*)(intptr[1]);
		if(argument_maps!=0){
		int *p_arg=(int*)intptr[2];//p_arg是第一个参数的地址,p_arg[0]是第一个参数
		int maps_count=0;
			for(maps_count = 0; argument_maps[maps_count] != '\0' ; maps_count++){
				if(argument_maps[maps_count] == '1'){
				p_arg[maps_count]=(int)Tiger_forward(&*(int*)p_arg[maps_count]);
				}
			}
		}
		//局部变量
		int local_num=intptr[3];
		if(local_num>0)
		{
			int local_i;
			int* plocal=&intptr[4];//plocal是第一个局部变量的地址,这里存储的每个局部变量又是一个指针,plocal[0]是第一个局部变量
			for(local_i=0;local_i<local_num;local_i++)
			{
				plocal[local_i]=(int)Tiger_forward((int*)plocal[local_i]);
			}
		}
		intptr=(int*)intptr[0];
	}
	
	//在to_space中扫描记录
	int* scan_ptr=(int*)scan;//int*，ptr[k]每次取一个整数4字节
	while(scan<heap.toNext)
	{
		int nSize=0;//字节数
		if(scan_ptr[1]==0)
		{//当前扫描记录为普通对象
			nSize=scan_ptr[2]+16;
			//获取class中field的map，在vptr所指向的虚函数表中
			//虚函数表中第1个是一个指向静态区域字符串的指针，需要这个指针
			char* class_gc_map = (char*)*(int*)scan_ptr[0];
			if(class_gc_map!=0)
			{
				int fieldsIndex;
				for(fieldsIndex=0;class_gc_map[fieldsIndex] != '\0'; fieldsIndex++)
				{
					if(class_gc_map[fieldsIndex]==1)
					{//该feild为引用类型,forward
						scan_ptr[4+fieldsIndex]=(int)Tiger_forward((int*)scan_ptr[4+fieldsIndex]);
					}
					//值类型不处理
				}
			}
			//向前移动字节数个
			scan = scan +nSize;
		}
		else
		{//当前扫描记录为数组
			nSize=scan[2]*4+16;
			scan = scan + nSize;
		}
	}
	// printf("before exchange:\n heap.from: %d,heap.fromFree: %d,heap.to: %d,heap.toStart %d,heap.toNext: %d\n",
	// (int)heap.from,(int)heap.fromFree,(int)heap.to,(int)heap.toStart,(int)heap.toNext);
	//交换from to
	char* temp=heap.from;
	heap.from=heap.toStart;
	heap.toStart=temp;
	heap.fromFree=heap.toNext;
	heap.toNext=heap.toStart;
	// printf("after:\n heap.from: %d,heap.fromFree: %d,heap.to: %d,heap.toStart %d,heap.toNext: %d\n",
	// (int)heap.from,(int)heap.fromFree,(int)heap.to,(int)heap.toStart,(int)heap.toNext);
	int freesize=(heap.size-beforegc)-(heap.fromFree-heap.from);
	end=clock();
  double time_t=(double)(end-start);
  //如果Enable_log为1，就输出log
  if(Enable_log)
  {
	printf("%d round of GC: %f ms, collected %d bytes\n",gc_round,time_t,freesize);
  }
  
}

