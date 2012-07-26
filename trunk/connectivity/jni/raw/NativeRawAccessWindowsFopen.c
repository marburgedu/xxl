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
 */

// #define DEBUG
#undef DEBUG
// The optimization is only implemented for read-operations so far.
#undef SEQ_OPT



#include <jni.h>		// For the java datatypes
#include <stdio.h>		// The prototypes of the interface
#include "xxl_core_io_raw_NativeRawAccess.h"
#include "raw.h"


JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_open(JNIEnv *env, jobject obj, jstring jfilename) {
	FILE *jfilep; 			// int of the file/device
	jlong sectors;
	fpos_t length;
	// BOOL isDevice=FALSE;
	const jbyte *filename;		// Java needs UTF-coded strings, c needs ASCII
	jint sectorSize;
	
	DEBUG_OUTPUT("Enter open",0);
	init(env, obj);
	
	// do not call get methods before init!
	jfilep = (FILE*) getfilep(env, obj);
	sectorSize = getsectorSize(env, obj);
	// Converts utf to ASCII
	filename = (*env)->GetStringUTFChars(env, jfilename, NULL);
	// got it?
	
	if (filename==NULL) {
		reportError(env,obj,"Filename NULL");
		return;
	}
	
	// Already a device open?
	if (jfilep!=0) {
		reportError(env,obj,"File already open");
		return;
	}

  	DEBUG_OUTPUT("Filename: %s",filename);
  	
  	//if ( (filename[0]=='\\') && (filename[1]=='\\') && (filename[2]=='.') && (filename[3]=='\\') )
  	//	isDevice=TRUE;
  		
	jfilep = fopen(filename,"rwbc");

	// Open failed?
	if (jfilep==NULL) {
		reportError(env,obj,"Open failed - file not found");
		return;
	}

	// Set the int inside the java object
	setfilep(env, obj, (jlong) jfilep);
  	DEBUG_OUTPUT("Filepointer: %d",(long) jfilep);

	setbuf(jfilep, NULL);

	if (fseek(jfilep,0,SEEK_END)!=0) {
		reportError(env,obj,"seek failed");
		return;
	}

	fgetpos(jfilep,&length);
	if (length==-1) {
		reportError(env,obj,"Size returned 0");
		return;
	}
	else  
		sectors = length/sectorSize;
		
  	DEBUG_OUTPUT("Sektoren: %d\n", (long) sectors);
	setsectors(env, obj, (jlong) sectors);
}

JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_close (JNIEnv *env, jobject obj){
	FILE *jfilep = (FILE*) getfilep(env, obj);
	if (fclose(jfilep)!=0) {
		reportError(env,obj,"Close failed");
		return;
	}
	setfilep(env, obj, 0);
}

JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_write (JNIEnv *env, jobject obj, jbyteArray jblock, jlong sector) {
	FILE *jfilep = (FILE*) getfilep(env, obj); 			// Device int
	jlong len = (*env)->GetArrayLength(env, jblock); 		// length of delivered java byte array
	jint sectorSize = getsectorSize(env, obj);
	jbyte *block = (*env)->GetByteArrayElements(env, jblock, 0); 	// Convert it to a c array our sectorblock
	jlong fpos; 							// Position for writing
	
	DEBUG_OUTPUT ("write: sector=%d", (long) sector);

	if (len!=sectorSize) {
		reportError(env,obj,"byte array does not hava sector size");
               	return;
	}
	
	if ((sector<0) || (sector >= getsectorSize(env, obj))) {
		reportError(env,obj,"filepointer outside area");
               	return;
	}

	// Position for writing
	fpos = (jlong) sector * sectorSize;
 
	// Set position
	if (fseek(jfilep,fpos,SEEK_SET)==-1) {
		reportError(env,obj,"filepointer could not be set");
		return;
	}

	// Write the block down to the device
	if (fwrite(block, 1, sectorSize, jfilep)!=sectorSize) {
		reportError(env,obj,"error writing block");
               	return;
	}
	
	if (fflush(jfilep)!=0) {
		reportError(env,obj,"error flushing buffers");
               	return;
	}

	(*env)->ReleaseByteArrayElements(env, jblock, block, 0);
}

JNIEXPORT void JNICALL
Java_xxl_core_io_raw_NativeRawAccess_read (JNIEnv *env, jobject obj, jbyteArray jblock, jlong sector) {
	FILE *jfilep = (FILE*) getfilep(env, obj);		// Get the device int
	jlong fpos;						// The position to read on the device
	jint sectorSize = getsectorSize(env, obj);
	jlong len = (*env)->GetArrayLength(env, jblock); 
	jbyte *block = (*env)->GetByteArrayElements(env, jblock, 0); 
#ifdef SEQ_OPT	
	jlong lastSector = getlastSector(env, obj);
#endif

	DEBUG_OUTPUT("read: sector==%d", (long) sector);
	
	if (jfilep==0) {
		reportError(env,obj,"file not open");
               	return;
	}

	// Is it exactly one block
	if (len!=sectorSize) {
		reportError(env,obj,"byte array does not have sector size");
               	return;
	}

#ifdef SEQ_OPT	
	if (sector != lastSector+1) {
		// non sequential access!
#endif
		fpos = (jlong) sector * sectorSize;
		if (fseek(jfilep,fpos,SEEK_SET)==-1) {
			reportError(env,obj,"filepointer could not be set");
        	       	return;
        	}
#ifdef SEQ_OPT	
	}
#endif

	DEBUG_OUTPUT("read the block",0);
	// Read the block
	if (fread(block, 1, sectorSize, jfilep)!=sectorSize) {
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
