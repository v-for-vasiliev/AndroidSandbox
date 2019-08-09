/** @file		AlignedMalloc.h
 *  @brief		Aligned malloc implementation.
 *  @copyright	VisionLabs LLC
 *  @date		11.08.2014
 */

#pragma once

#ifndef _WIN32
#include <cstddef> // size_t

// On glibc there is no _aligned_* family so we implement it.
extern "C"
{
	void* _aligned_malloc(size_t size, size_t alignment);
	void  _aligned_free(void *memblock);
	void* _aligned_realloc(void *memblock, size_t size, size_t alignment);
}

#else

// On MS CRT _aligned_* functions reside where original malloc is.
#include <malloc.h>

#endif
