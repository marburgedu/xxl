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
#include <windows.h>

/**
 * Implements the Timer interface with JNI under Windows.
 * The methods QueryPerformanceFrequency and QueryPerformanceCounter
 * are used for this purpose.
 */
#include "xxl_core_util_timers_JNITimer.h"

// number of ticks per second:
static jlong frequency;
static jclass cls;
static jfieldID fid;

JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * vm, void * reserved) {
	LARGE_INTEGER counterFrequency;
	QueryPerformanceFrequency(&counterFrequency);
	frequency = counterFrequency.QuadPart;

	return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL
Java_xxl_core_util_timers_JNITimer_start (JNIEnv *env, jobject obj) {
	LARGE_INTEGER counterReading;
	cls=(*env)->GetObjectClass(env, obj);
	fid=(*env)->GetFieldID(env, cls, "starttime", "J");

	QueryPerformanceCounter(&counterReading);
	(*env)->SetLongField(env, obj, fid, counterReading.QuadPart);
}

JNIEXPORT jlong JNICALL
Java_xxl_core_util_timers_JNITimer_getDuration (JNIEnv *env, jobject obj) {
	LARGE_INTEGER counterReading;

	QueryPerformanceCounter(&counterReading);
	return counterReading.QuadPart-((*env)->GetLongField(env, obj, fid));
}

JNIEXPORT jlong JNICALL
Java_xxl_core_util_timers_JNITimer_getTicksPerSecond (JNIEnv *env, jobject obj) {
	return frequency;
}
