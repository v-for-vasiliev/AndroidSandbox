#pragma once
#ifdef _WIN32
#include <intrin.h>
#endif
#include <cstdint>

namespace vlf {
namespace atomic {
#ifdef _WIN32
	// These return new value.
	inline int inc(volatile int32_t* ptr) { return _InterlockedIncrement(reinterpret_cast<volatile long*>(ptr)); }
	inline int dec(volatile int32_t* ptr) { return _InterlockedDecrement(reinterpret_cast<volatile long*>(ptr)); }
	inline int cas(volatile int32_t* ptr, int32_t expected, int32_t value) {
		return _InterlockedCompareExchange(reinterpret_cast<volatile long*>(ptr), value, expected);
	}
	inline void fence() { _ReadWriteBarrier(); }
#else // We currently assume Linux here
	// These return new value.
	inline int add(volatile int32_t* ptr, int32_t value) { return __sync_add_and_fetch(ptr, value); }
	inline int inc(volatile int32_t* ptr) { return add(ptr, 1); }
	inline int dec(volatile int32_t* ptr) { return add(ptr,-1); }
	inline int cas(volatile int32_t* ptr, int32_t expected, int32_t value) {
		return __sync_val_compare_and_swap(ptr, expected, value);
	}
	inline void fence() { __sync_synchronize(); }
#endif
}
}
