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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

#ifdef __RAW_H__
#define __RAW_H__
#else

#ifdef DEBUG
#define DEBUG_OUTPUT(format,s) { printf("raw.h:"); printf(format,s); printf("\n"); }
#else
#define DEBUG_OUTPUT(format,s) 
#endif

/* for faster access (initialized only once in init()) */
static jclass cls = NULL;
static jfieldID fidMode;
static jfieldID fidSectors;
static jfieldID fidSectorSize;
static jfieldID fidFilep;
#ifdef SEQ_OPT	
static jfieldID fidLastSector;
#endif

/* Accessing fields of the Java-object */
jint getMode(JNIEnv *env, jobject obj) {
	return (*env)->GetIntField(env, obj, fidMode);
}

/* not needed!
void setMode(JNIEnv *env, jobject obj, jint jmode) {
	(*env)->SetIntField(env, obj, fidMode, jmode);  
}
*/

jlong getsectors(JNIEnv *env, jobject obj) {
	return (*env)->GetLongField(env, obj, fidSectors);
}

void setsectors(JNIEnv *env, jobject obj, jlong jsec) {
	(*env)->SetLongField(env, obj, fidSectors, jsec);  
}

jint getsectorSize(JNIEnv *env, jobject obj) {
	return (*env)->GetIntField(env, obj, fidSectorSize);
}

/* not needed!
void setsectorSize(JNIEnv *env, jobject obj, jint jsec) {
	(*env)->SetIntField(env, obj, fidSectorSize, jsec);  
}
*/

jlong getfilep(JNIEnv *env, jobject obj) {
	return (*env)->GetLongField(env, obj, fidFilep);
}

void setfilep(JNIEnv *env, jobject obj, jlong jfilep) {
	(*env)->SetLongField(env, obj, fidFilep, jfilep);
}

#ifdef SEQ_OPT
jlong getlastSector(JNIEnv *env, jobject obj) {
	return (*env)->GetLongField(env, obj, fidLastSector);
}

void setlastSector(JNIEnv *env, jobject obj, jlong lsec) {
	(*env)->SetLongField(env, obj, fidLastSector, lsec);
}
#endif

/* reports an error */
void reportError(JNIEnv *env, jobject obj, char *errormsg) {
	jclass errorClass;
	jstring s;
	
	DEBUG_OUTPUT("Enter reportError. Message: %s",errormsg);
	s = (*env)->NewStringUTF(env,errormsg);
	
	errorClass = (*env)->FindClass(env, "xxl/io/raw/RawAccessError");
	
	DEBUG_OUTPUT("Errorclass %d",errorClass);
	(*env)->ThrowNew(env, errorClass, errormsg);
}

void init(JNIEnv *env, jobject obj) {
	cls=(*env)->GetObjectClass(env, obj);
	fidSectors=(*env)->GetFieldID(env, cls, "sectors", "J");
	fidSectorSize=(*env)->GetFieldID(env, cls, "sectorSize", "I");
	fidMode=(*env)->GetFieldID(env, cls, "mode", "I");
	fidFilep=(*env)->GetFieldID(env, cls, "filep", "J");
#ifdef SEQ_OPT	
	fidLastSector=(*env)->GetFieldID(env, cls, "lastSector", "J");
#endif
}

JNIEXPORT void JNICALL
JNI_UnLoad (JavaVM * vm, void * reserved) {
	cls = NULL;
}

#endif
