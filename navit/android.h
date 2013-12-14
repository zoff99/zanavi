#ifdef HAVE_API_ANDROID

#include <jni.h>
extern JNIEnv *jnienv;
extern JavaVM *cachedJVM;
extern jobject *android_activity;
extern struct callback_list *android_activity_cbl;
extern int android_version;
int android_find_class_global(char *name, jclass *ret);
int android_find_method(jclass class, char *name, char *args, jmethodID *ret);
int android_find_static_method(jclass class, char *name, char *args, jmethodID *ret);
void send_osd_values(char *id, char *text1, char *text2, char *text3, int i1, int i2, int i3, int i4, float f1, float f2, float f3);
void set_vehicle_values_to_java(int x, int y, int angle, int speed);
void set_vehicle_values_to_java_delta(int dx, int dy, int dangle, int dzoom);

JNIEnv* jni_getenv();

struct jni_object
{
	JNIEnv* env;
	jobject jo;
	jmethodID jm;
};

#else

typedef int jobject;
typedef int jmethodID;

struct jni_object
{
	int dummy;
};

#endif
