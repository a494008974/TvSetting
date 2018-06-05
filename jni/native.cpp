/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "tvsettings.native.cpp"

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <android/log.h>
#include <sys/ioctl.h>
#include <asm/types.h>
#include <cutils/log.h>
#include <errno.h>

#include <utils/Log.h>
#include "jni.h"
#include <math.h>
#include "TVInfo.h"
#include "Vop.h"



typedef  unsigned int u32;
#define PICO_MAGIC 'x'
#define PICO_MAX_NR 18
#define PICO_IOCTL_SET_POWER_LVL  _IOW(PICO_MAGIC,1,u32 *)
#define PICO_IOCTL_SET_FLIP_NOR	  _IO(PICO_MAGIC,2)
#define PICO_IOCTL_SET_FLIP_H     _IO(PICO_MAGIC,3)
#define PICO_IOCTL_SET_FLIP_V     _IO(PICO_MAGIC,4)
#define PICO_IOCTL_SET_INTF_TYPE  _IOW(PICO_MAGIC,5,u32 *)
#define PICO_IOCTL_SET_RES		  _IOW(PICO_MAGIC,6,u32 *)
#define PICO_IOCTL_SEL_SEQ		  _IOW(PICO_MAGIC,7,u32 *)
#define PICO_IOCTL_SET_LED_ON     _IO(PICO_MAGIC,8)
#define PICO_IOCTL_SET_LED_OFF     _IO(PICO_MAGIC,9)
#define PICO_IOCTL_SET_LED_LOW     _IO(PICO_MAGIC,10)
#define PICO_IOCTL_SYS_RESTART     _IO(PICO_MAGIC,12)
#define PICO_IOCTL_SYS_ONLINE     _IO(PICO_MAGIC,13)
#define PICO_IOCTL_SYS_POWER_DOWN     _IO(PICO_MAGIC,14)
#define PICO_IOCTL_SYS_SET_POWER     _IO(PICO_MAGIC,15)
#define PICO_IOCTL_SYS_GET_POWER     _IO(PICO_MAGIC,16)
#define PICO_IOCTL_SYS_SET_MODE     _IO(PICO_MAGIC,17)
#define PICO_IOCTL_SYS_GET_MODE     _IO(PICO_MAGIC,18)
#undef LOG
#define LOG


#define PICO_PATH "/dev/pico"
#define MTD_PATH "/dev/block/mmcblk0p14"

#define FLIP_NOR   0
#define FLIP_H	   1
#define FLIP_V	   2

#define POWER_0 200
#define POWER_1 290
#define POWER_2 380
#define POWER_3 470
#define POWER_4 560
#define POWER_5 650
#define POWER_6 740
#define POWER_7 820
#define POWER_8 900
#define POWER_9 980
#define DEFAULT_CURRENT_LEVEL 6

#define MIN(a,b)                        ((a) <= (b) ? (a):(b))
#define MAX(a,b)                        ((a) >= (b) ? (a):(b))
#define ROUND(a)						(int)(a+0.5)


static int str_startsWith(char * str, char * search_str)
{
    if ((str == NULL) || (search_str == NULL)) return 0;
    return (strstr(str, search_str) == str);
}

static int save_pico_mode_to_mtd(int mode)
{
	int ret;
	int mode_fd;
	char buf[64]={0};
	mode_fd = open(MTD_PATH, O_RDONLY);
	if( mode_fd < 0 ){
		ALOGD("Can't open %s, errno = %d", MTD_PATH, errno);
		return -1;
	}
	memset(buf, 0, 64);
	if( 0 == read(mode_fd, buf, 64) ){
		 perror("read failed");
		 close(mode_fd);
		 goto done;
	 }
	close(mode_fd);
	ALOGD("zhangyi jni mode=%s",buf);
	if(str_startsWith(buf, "start:")){
		buf[11]='0';
		buf[12]='x';

		if(mode == 1){
			buf[13]='0';
			buf[14]='0';
		}else if(mode == 2){
			buf[13]='1';
			buf[14]='0';
		}else if(mode == 3){
			buf[13]='2';
			buf[14]='0';
		}else if(mode == 4){
			buf[13]='3';
			buf[14]='0';
		}
	}else{
			if(mode == 1){
				sprintf(buf,"%s","start:flip=0x00:current=0x02:end");
			}else if(mode == 2){
				sprintf(buf,"%s","start:flip=0x10:current=0x02:end");
			}else if(mode == 3){
				sprintf(buf,"%s","start:flip=0x20:current=0x02:end");
			}else if(mode == 4){
				sprintf(buf,"%s","start:flip=0x30:current=0x02:end");
			}
	}
	close(mode_fd);
	ALOGD("save_pico_mode_to_mtd=%s",buf);

	mode_fd = open(MTD_PATH, O_RDWR);
	if(mode_fd < 0 ){
		ALOGD("Can't open %s, errno = %d", MTD_PATH, errno);
		return -1;
	}
	if( -1 == write(mode_fd, buf, 64) ){
		ALOGD("write failed");
		close(mode_fd);
		return 0;
	}
	close(mode_fd);

done:
	return -1;
}


static int save_pico_power_level_to_mtd(int level)
{
	int ret;
	int power_level_fd;
	char buf[64]={0};
	power_level_fd = open(MTD_PATH, O_RDONLY);
	if( power_level_fd < 0 ){
		ALOGD("Can't open %s, errno = %d", MTD_PATH, errno);
		return -1;
	}
	memset(buf, 0, 64);
	if( 0 == read(power_level_fd, buf, 64) ){
		 perror("read failed");
		 close(power_level_fd);
		 goto done;
	 }
	close(power_level_fd);
	ALOGD("zhangyi jni power_level=%s",buf);
	if(str_startsWith(buf, "start:")){
		buf[24]='0';
		buf[25]='x';
		buf[26]='0';
		buf[27]='0' + level;
	}else{
		sprintf(buf,"start:flip=0x00:current=0x%02x:end", level);
	}
	close(power_level_fd);
	ALOGD("level_to_mtd=%s\n",buf);
	power_level_fd = open(MTD_PATH, O_RDWR);
	if( power_level_fd < 0 ){
		ALOGD("Can't open %s, errno = %d", MTD_PATH, errno);
		return -1;
	}
	if( -1 == write(power_level_fd, buf, 64) ){
		ALOGD("write failed");
		close(power_level_fd);
		return 0;
	}
	close(power_level_fd);

done:
	return -1;
}


static jint setProjectorLight(JNIEnv *env, jclass clz, jint level)
{
	ALOGI("=============pico open it  +++++++++");
	int fd,num;
	u32 backlight;
	fd=open(PICO_PATH,O_RDWR);
    if(fd == -1){
		ALOGE("Can't open %s, errno = %d", PICO_PATH, errno);
        return -1;
    }
	num=ioctl(fd,PICO_IOCTL_SYS_SET_POWER, &level);
	save_pico_power_level_to_mtd(level);
	close(fd);
    return num;
}

static jint fetchProjectorLight(JNIEnv *env, jclass clz)
{
	ALOGI("=============pico open it  +++++++++");
		int ret;
		int current_fd;
		char buf[64]={0};
		char *ptr = NULL;
		char pico_current[5]={0};
		int level = DEFAULT_CURRENT_LEVEL;
		memset(pico_current,0x00,5);
		current_fd = open(MTD_PATH, O_RDONLY);
		if( current_fd < 0 ){
			ALOGE("Can't open %s, errno = %d", MTD_PATH, errno);
			return level;
		}
		memset(buf, 0, 64);
		if( 0 == read(current_fd, buf, 64) ){
			ALOGE("Read from %s failed!, errno = %d", MTD_PATH, errno);
			close(current_fd);
			goto done;
		}
		close(current_fd);
		ALOGD("zhangyi jni pico_current=%s",buf);
		if(str_startsWith(buf, "start:")){
				ptr = strstr(buf,"start:flip=");
				ptr = ptr + 24;
				strncpy(pico_current,ptr,4);
				ALOGD("pico_current=%s",pico_current);
				sscanf(pico_current, "%x", &level);
				ALOGD("level=[%d]",level);
				return level;
		}else{
			return level;
		}

	done:
		return level;
}


static jint SetProjectorMode(JNIEnv *env, jclass clz, jint mode)
{
	int fd, num, io_mode;
	char read_buff[8];

	fd=open(PICO_PATH, O_RDWR);
    if(fd == -1){
		ALOGE("Can't open %s, errno = %d", PICO_PATH, errno);
		return -1;
    }
	num=ioctl(fd, PICO_IOCTL_SYS_SET_MODE, &mode);
	save_pico_mode_to_mtd(mode);

	close(fd);

	return num;
}

static jint fetchProjectorMode(JNIEnv *env, jclass clz)
{
	int ret;
	int mode_fd;
	char buf[64]={0};
	char *ptr = NULL;
	char pico_flip[5]={0};
	memset(pico_flip,0x00,5);
	mode_fd = open(MTD_PATH, O_RDONLY);
	if( mode_fd < 0 ){
		LOG("Can't open %s, errno = %d", MTD_PATH, errno);
		return -1;
	}
	memset(buf, 0, 64);
	if( 0 == read(mode_fd, buf, 64) ){
		 perror("read failed");
		 close(mode_fd);
		 goto done;
	 }
	close(mode_fd);
	ALOGD("zhangyi jni mode=%s",buf);
	if(str_startsWith(buf, "start:")){
			ptr = strstr(buf,"start:flip=");
			ptr = ptr + 11;
			strncpy(pico_flip,ptr,4);
			ALOGD("pico_flip=%s",pico_flip);
			if(!strncmp(pico_flip,"0x10",4))
				return 2;
			else if(!strncmp(pico_flip,"0x20",4))
				return 3;
			else if(!strncmp(pico_flip,"0x30",4))
				return 4;
			else return 1;
	}else{
		return 1;
	}

done:
	return -1;
}



static int bt1886eotf(int* segYn, double maxLumi, double minLumi)
{
	
	double Lw, Lb;
	double r = 2.4;
	double a, b;
	
	double xIndex;
	double yIndex;

	unsigned char i;
	int xBitmask, yBitmask;
	int segXn[65] = {0, 
		512,	1024,	1536,	2048,	2560,	3072,	3584,	4096,
		4608,	5120,	5632,	6144,	6656,	7168,	7680,	8192,
		8704,	9216,   9728,	10240,	10496,	10752,	11008,	11264,
		11520,	11776,	12032,	12288,	12544,	12800,	13056,	13312,
		13440,	13568,	13696,	13824,	13952,	14080,	14208,	14336,
		14464,	14592,	14720,	14848,	14976,	15104,	15232,	15360,
		15424,	15488,	15552,	15616,	15680,	15744,	15808,	15872,
		15936,	16000,	16064,	16128,	16192,	16256,	16320,	16383};


	Lw = maxLumi / 10000;
	Lb = minLumi / 10000;

	a = pow((pow(Lw, 1/r) - pow(Lb, 1/r)), r);
	b = pow(Lb, 1/r) / (pow(Lw, 1/r) - pow(Lb, 1/r));

	xBitmask = 16383;
	yBitmask = 262143;
	for (i = 0; i < 65; i++)
	{
		xIndex = segXn[i]*1.0/xBitmask;
		xIndex = MAX(xIndex+b, 0);
		yIndex = a * pow(xIndex, r) * yBitmask;
		segYn[i] = ROUND(yIndex);
		
	}
	return 0;
}


static int st2084oetf(int* segYn, double coef, double nFac)
{
	int segXn[65] = {0,
		1,	2,	4,	8,	16,	24,	32,	64,
		96,	128,	256,	384,	512,	640,	768,	896,
		1024,	1280,	1536,	1792,	2048,	2304,	2560,	2816,
		3072,	3584,	4096,	4608,	5120,	6144,	7168,	8192,
		9216,	10240,	11264,	12288,	14336,	16384,	18432,	20480,
		22528,	24576,	26624,	28672,	30720,	32768,	36864,	40960,
		45056,	49152,	53248,	57344,	61440,	65536,	73728,	81920,
		90112,	98304,	114688,	131072,	163840,	196608,	229376,	262143
	};

	int xBitmask, yBitmask;
	unsigned char i;

	double xIndex, yIndex;
	double c1, c2, c3;
	double m, n;

	c1 = 3424 * 1.0 / 4096;
	c2 = 2413 * 1.0 / 4096 * 32;
	c3 = 2392 * 1.0 / 4096 * 32;
	m = 2523 * 1.0 / 4096 * 128;
	n = 2610 * 1.0 / 4096 * (1.0/nFac);
	
	xBitmask = 262143;
	yBitmask = 16383;

	for (i = 0; i < 65; i++)
	{
		xIndex = segXn[i] * 1.0 / xBitmask;
		yIndex = coef * pow((c1 + c2*pow(xIndex, n))/(1 + c3*pow(xIndex, n)), m);
		yIndex = ROUND(yIndex * yBitmask);
		segYn[i] = MIN(yIndex, yBitmask);
	}
	return 0;
}


static jintArray get(JNIEnv *env, jobject thiz, jdouble x, jdouble y) {
    ALOGI("%lf : %lf", x, y);
	
    jintArray intArray = env->NewIntArray(65);
    jint* result = new jint[65];
	bt1886eotf(result,x,y);
    env->SetIntArrayRegion(intArray, 0, 65, result);
    delete []result;
    return intArray;
}


static jintArray getOther(JNIEnv *env, jobject thiz, jdouble x, jdouble y) {
    ALOGI("%lf : %lf", x, y);
    jintArray intArray = env->NewIntArray(65);
    jint* result = new jint[65];
	st2084oetf(result,x,y);
    env->SetIntArrayRegion(intArray, 0, 65, result);
    delete []result;
    return intArray;
}



static jboolean isSupportHDR(JNIEnv *env, jobject thiz){
	int supportType = HdmiSupportedDataSpace();
	ALOGI("%d", HdmiSupportedDataSpace());
	return (supportType & HAL_DATASPACE_TRANSFER_ST2084) != 0;
}


static void setHDREnable(JNIEnv *env, jobject thiz, jint enable){
	ALOGI("setHDREnable");
	setHdmiHDR(enable);
}

static jintArray getEetf(JNIEnv *env, jobject thiz, jfloat maxDst, jfloat minDst) {
	ALOGI("%f : %f", maxDst, minDst);
	jintArray array = env->NewIntArray(33);
	jint* result = new jint[33];
	makeHDR2SDREETF(1200, 0.02, maxDst, minDst, result);
	env->SetIntArrayRegion(array, 0, 33, result);
	delete[] result;
	return array;
}

static jintArray getOetf(JNIEnv *env, jobject thiz, jfloat maxDst, jfloat minDst) {
	ALOGI("%f : %f", maxDst, minDst);
	jintArray array = env->NewIntArray(33);
	jint* result = new jint[33];
	makeHDR2SDROETF(maxDst, minDst, result);
	env->SetIntArrayRegion(array, 0, 33, result);
	delete[] result;
	return array;
}


static jintArray getMaxMin(JNIEnv *env, jobject thiz, jfloat maxDst, jfloat minDst){
	ALOGI("%f : %f", maxDst, minDst);
	jintArray array = env->NewIntArray(2);
	jint* result = new jint[2];
	makeMaxMin(1200, 0.02, maxDst, minDst, result);
	env->SetIntArrayRegion(array, 0, 2, result);
	delete[] result;
	return array;
}

static const char *classPathName = "com/android/tv/settings/util/JniCall";

static JNINativeMethod methods[] = {
  {"get", "(DD)[I", (void*)get },
  {"getOther", "(DD)[I", (void*)getOther },
  {"isSupportHDR", "()Z", (void*)isSupportHDR },
  {"setHDREnable", "(I)V", (void*)setHDREnable },
  {"getEetf", "(FF)[I", (void*)getEetf },
  {"getOetf", "(FF)[I", (void*)getOetf },
  {"getMaxMin", "(FF)[I", (void*)getMaxMin },

  {"setProjectorLight", "(I)I", (void*)setProjectorLight },
  {"SetProjectorMode", "(I)I", (void*)SetProjectorMode },
  {"fetchProjectorMode", "()I", (void*)fetchProjectorMode },
  {"fetchProjectorLight", "()I", (void*)fetchProjectorLight },
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}


// ----------------------------------------------------------------------------

/*
 * This is called by the VM when the shared library is first loaded.
 */
 
typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;
    
    ALOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        ALOGE("ERROR: registerNatives failed");
        goto bail;
    }
    
    result = JNI_VERSION_1_4;
    
bail:
    return result;
}
