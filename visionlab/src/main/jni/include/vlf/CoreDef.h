#pragma once

// Aling value up to align so that value % align == 0.
#define ALIGN_UP(value, align)				\
	(((value) & (align-1)) ?					\
    (((value) + (align-1)) & ~(align-1)) :	\
                                (value))

#if defined(_MSC_VER)
#define ALIGNED(x)		__declspec(align(x))
#define RESTRICT		__restrict
#define FORCE_INLINE	__forceinline
#else
#define ALIGNED(x)		__attribute__ ((aligned(x)))
#define RESTRICT		__restrict__
#define FORCE_INLINE	inline __attribute__ ((always_inline))
#endif

#define STRINGIZE_(x) #x
#define STRINGIZE(x) STRINGIZE_(x)

// Dummy type structure.
struct Dummy {};


#define LOW_8_BITS (x) ((x) & 0xff)
#define LOW_16_BITS(x) ((x) & 0xffff)
#define LOW_32_BITS(x) ((x) & 0x00000000ffffffff)


// System default path separators.
#define WIN32_PATH_SEPARATOR "\\"
#define LINUX_PATH_SEPARATOR "/"

#ifdef _WIN32
#define PATH_SEPARATOR WIN32_PATH_SEPARATOR
#else
#define PATH_SEPARATOR LINUX_PATH_SEPARATOR
#endif

#if defined (_WIN32)
	#if VLF_SHARED
		#if defined (VLF_DLL_EXPORTS)
			#define VLF_API __declspec(dllexport)
		#else
			#define VLF_API __declspec(dllimport)
		#endif
	#else
		#define VLF_API
	#endif
#else
	#if __GNUC__ >= 4
		#define VLF_API __attribute__ ((visibility ("default")))
	#else
		#define VLF_API
	#endif
#endif
