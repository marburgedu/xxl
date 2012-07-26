/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2012 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

/* 
 * This is the JNI implementation of the direct disc access
 * with Windows NT, 2000 and XP
 * There exists a parallel version for Linux.
 * Format for Win32-Api
 * 				\\.\c:
 * or
 * 				\\.\PhysicalDrive0
 */

// #define DEBUG
#undef DEBUG
#undef SEQ_OPT
#define FLUSH_OPT

#define _WIN32_WINNT 0x500				// Needed for some functions in Windows 2000 and higher!

#include <jni.h>					// For the java datatypes
#include "xxl_core_io_raw_NativeRawAccess.h"		// The prototypes of the interface
#include <windows.h>					// We access devices with the method CreateDevice, there it come from:
#include <winioctl.h>
#include "raw.h"


void outputError() {
	LPTSTR lpMsgBuf;
	FormatMessage( 
	    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
	    FORMAT_MESSAGE_FROM_SYSTEM | 
	    FORMAT_MESSAGE_IGNORE_INSERTS,
	    NULL,
	    GetLastError(),
	    MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
	    (LPTSTR) &lpMsgBuf,
	    0,
	    NULL 
	);
	printf("Error: %s\n",lpMsgBuf);
	
	LocalFree(lpMsgBuf);
}

JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_open(JNIEnv *env, jobject obj, jstring jfilename) {
	HANDLE jfilep; 			// Handle of the file/device
	jlong sectors, length;
	BOOL isDevice=FALSE;
	DWORD lengthLower,lengthUpper;
	DWORD sectorspercluster;
	DWORD bytespersector;
	long bytesReturned;
	DISK_GEOMETRY dg;
	GET_LENGTH_INFORMATION li;
	const jbyte *filename;		// Java needs UTF-coded strings, c needs ASCII
	jint sectorSize;
	double sfactor;
	
	DEBUG_OUTPUT("Enter open",0);
	init(env, obj);
	
	// do not call get methods before init!
	jfilep = (HANDLE) getfilep(env, obj);
	sectorSize = getsectorSize(env, obj);
	// Converts utf to ASCII
	filename = (*env)->GetStringUTFChars(env, jfilename, NULL);
	// got it?
	
	if (filename==NULL) {
		reportError(env,obj,"Filename NULL");
		return;
	}
	
	// Already a device open?
	if (jfilep!=NULL) {
		reportError(env,obj,"File already open");
		return;
	}

  	DEBUG_OUTPUT("Filename: %s",filename);
  	
  	if ( (filename[0]=='\\') && (filename[1]=='\\') && (filename[2]=='.') && (filename[3]=='\\') )
  		isDevice=TRUE;
  		
	jfilep = (HANDLE) CreateFile(
		filename,
		GENERIC_READ | GENERIC_WRITE,
		FILE_SHARE_READ | FILE_SHARE_WRITE,
		0,
		OPEN_EXISTING,
		FILE_FLAG_WRITE_THROUGH | FILE_FLAG_NO_BUFFERING,
		NULL
	);
 	
  	DEBUG_OUTPUT("Filepointer: %d",(long) jfilep);

	// Open failed?
	if (jfilep==INVALID_HANDLE_VALUE) {
		reportError(env,obj,"Open failed - file not found");
		return;
	}

	// Set the handle inside the java object
	setfilep(env, obj, (jlong) jfilep);
  
	if (!isDevice) {
		DEBUG_OUTPUT("Filemode\n",0);
		
		lengthLower = GetFileSize((HANDLE) jfilep, &lengthUpper);
		DEBUG_OUTPUT("lower: %d\n",lengthLower);
		DEBUG_OUTPUT("upper: %d\n",lengthUpper);
		length = ((jlong) lengthLower) + (( (jlong) lengthUpper ) << 32);
		
		DEBUG_OUTPUT("lower: %d\n",(DWORD) length);//  && ( (((jlong)1)<<32)-1) );
		DEBUG_OUTPUT("upper: %d\n",(DWORD) (length>>32));
		sectors = length/sectorSize;
	}
	else {
		DEBUG_OUTPUT("Devicemode\n",0);
		
		if (!DeviceIoControl(
			(HANDLE) jfilep,			// handle to device
			IOCTL_DISK_GET_DRIVE_GEOMETRY,		// dwIoControlCode
			NULL,					// lpInBuffer
			0,					// nInBufferSize
			(LPVOID) &dg,				// output buffer
			(DWORD) sizeof(dg),			// size of output buffer
			(LPDWORD) &bytesReturned,		// number of bytes returned
			(LPOVERLAPPED) NULL			// OVERLAPPED structure
		)) {
			DEBUG_OUTPUT("Cannot get information for device",0);
			Java_xxl_core_io_raw_NativeRawAccess_close (env, obj);
                	return;
		}
		
		DEBUG_OUTPUT("Cylinders: %d\n",dg.Cylinders.LowPart);
		// MEDIA_TYPE  MediaType;
		DEBUG_OUTPUT("tpc: %d\n",(long) dg.TracksPerCylinder);
		DEBUG_OUTPUT("spt: %d\n",(long) dg.SectorsPerTrack); 
		DEBUG_OUTPUT("bps: %d\n",(long) dg.BytesPerSector); 

		// allow multiple sectorsizes of BytesPerSector
		sfactor = (double) sectorSize/dg.BytesPerSector;

		// test for .0
		if (sfactor-(int)sfactor>0.0) {
			Java_xxl_core_io_raw_NativeRawAccess_close (env, obj);
			reportError(env,obj,"sectorsize not allowed");
                	return;
		}
		
		if (!DeviceIoControl(
			(HANDLE) jfilep,			// handle to device
			IOCTL_DISK_GET_LENGTH_INFO,		// dwIoControlCode
			NULL,					// lpInBuffer
			0,					// nInBufferSize
			(LPVOID) &li,				// output buffer
			(DWORD) sizeof(li),			// size of output buffer
			(LPDWORD) &bytesReturned,		// number of bytes returned
			(LPOVERLAPPED) NULL			// OVERLAPPED structure
		)) {
			DEBUG_OUTPUT("Cannot get information for device",0);
			Java_xxl_core_io_raw_NativeRawAccess_close (env, obj);
                	return;
		}
	
		sectors = (jlong) li.Length.QuadPart / (jlong) sectorSize;
		// old: calculates the size of the whole drive - no partitions possible!
		// sectors (long) dg.Cylinders.LowPart * (long) dg.TracksPerCylinder * (long) dg.SectorsPerTrack / ((int) sfactor);
	}

	DEBUG_OUTPUT("Sektoren: %d\n", (long) sectors);
	setsectors(env, obj, (jlong) sectors);
}
  
JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_close (JNIEnv *env, jobject obj){
	HANDLE jfilep = (HANDLE) getfilep(env, obj); // Get the filepointer from the java-class
	if (!CloseHandle (jfilep)) {
		reportError(env,obj,"Close failed");
		return;
	}
	setfilep(env, obj, 0); 	// Set handle to NULL
}

JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_write (JNIEnv *env, jobject obj, jbyteArray jblock, jlong sector) {
	HANDLE jfilep = (HANDLE) getfilep(env, obj); 			// Device handle
	jlong len = (*env)->GetArrayLength(env, jblock); 		// length of delivered java byte array
	jint sectorSize = getsectorSize(env, obj);
	jbyte *block = (*env)->GetByteArrayElements(env, jblock, 0); 	// Convert it to a c array our sectorblock
	jlong fpos; 							// Position for writing
	LARGE_INTEGER li;
	DWORD nbytes=0;
	
	DEBUG_OUTPUT ("write: sector=%d", (long) sector);

	if (len!=sectorSize) {
		reportError(env,obj,"byte array does not hava sector size");
               	return;
	}
		
	if ((sector<0) || (sector >= getsectors(env,obj))) {
		reportError(env,obj,"filepointer outside area in write");
               	return;
	}

	// Position for writing
	fpos = (jlong) sector * sectorSize;
 
	li.QuadPart = fpos;

	// Set position
	if (INVALID_SET_FILE_POINTER==SetFilePointer( (HANDLE) jfilep, li.LowPart, &li.HighPart, FILE_BEGIN)) {
		reportError(env,obj,"filepointer could not be set");
		return;
	}

	// Write the block down to the device
	if (!WriteFile( (HANDLE) jfilep, block, sectorSize, &nbytes, NULL)) {
		reportError(env,obj,"error writing block");
               	return;
	}

#ifdef FLUSH_OPT
	if (getMode(env,cls) & 1) {
			if (!FlushFileBuffers( (HANDLE) jfilep)) {
			outputError();
			reportError(env,obj,"error flushing buffers");
	               	return;
		}
	}
#endif
	(*env)->ReleaseByteArrayElements(env, jblock, block, 0);
}

JNIEXPORT void JNICALL
Java_xxl_core_io_raw_NativeRawAccess_read (JNIEnv *env, jobject obj, jbyteArray jblock, jlong sector) {
	HANDLE jfilep = (HANDLE) getfilep(env, obj);		// Get the device handle
	LARGE_INTEGER fpos;					// The position to read on the device
	DWORD nbytes;
	jint sectorSize = getsectorSize(env, obj);
	jlong len = (*env)->GetArrayLength(env, jblock); 
	jbyte *block = (*env)->GetByteArrayElements(env, jblock, 0); 
#ifdef SEQ_OPT	
	jlong lastSector = getlastSector(env, obj);
#endif

	DEBUG_OUTPUT("read: sector==%d", (long) sector);
	
	if (jfilep==NULL) {
		reportError(env,obj,"file not open");
               	return;
	}

	// Is it exactly one block
	if (len!=sectorSize) {
		reportError(env,obj,"byte array does not have sector size");
               	return;
	}

	if ((sector<0) || (sector >= getsectors(env,obj))) {
		reportError(env,obj,"filepointer outside area in read");
               	return;
	}

#ifdef SEQ_OPT	
	if (sector != lastSector+1) {
		// non sequential access!
#endif
		// Calculate the position to read
		fpos.QuadPart = (jlong) sector * sectorSize;
		DEBUG_OUTPUT("readr: fileposition==%d",(long) fpos.QuadPart);
		if ( INVALID_SET_FILE_POINTER==SetFilePointer( (HANDLE) jfilep, fpos.LowPart, &fpos.HighPart, FILE_BEGIN) ) {
			reportError(env,obj,"filepointer could not be set");
	               	return;
		}
#ifdef SEQ_OPT	
	}
#endif

	DEBUG_OUTPUT("read the block",0);
	
	// Read the block
	if (!ReadFile ( (HANDLE) jfilep, block, sectorSize, &nbytes, NULL)) {
		reportError(env,obj,"read failed");
               	return;
	}
	
	// Convert the c block array to a java byte array
	(*env)->SetByteArrayRegion(env, jblock, 0, sectorSize, block);
#ifdef SEQ_OPT	
	setlastSector(env, obj, sector);
#endif
	(*env)->ReleaseByteArrayElements(env, jblock, block, 0);
}
