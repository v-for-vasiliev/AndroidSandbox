#pragma once

#include <vlf/CoreDef.h>

namespace vlf
{
#ifndef DOXYGEN_SHOULD_SKIP_THIS
	/** @brief Generic base class of a pointer for reference counted objects.

		Implements common features of Ref and WeakRef and is not meant
		to be used directly.

		@note This is to eliminate code duplication and should not be used directly.

		@tparam T object interface (must be derived from IRefCounted)
	*/
	template<typename T>
	struct RefBase {

		/** @brief Initializes object pointer to nullptr.
		*/
		RefBase() : m_ptr(nullptr) {}

		/** @brief Initializes object pointer with ptr.
			@param [in] ptr raw pointer to initialize with.
		*/
		explicit RefBase(T* ptr) : m_ptr(ptr) {}

		/** @brief Get raw object pointer.
			@note No checks are performed.
			@return raw object pointer.
		*/
		operator T* () const {
			return get();
		}

		/** @brief Dereference operator.
			@return reference to object being held.
		*/
		T& operator * () const {
			assert(!isNull());
			return *get();
		}

		/** @brief Implicit cast to bool.
			@return true if ref is not null.
		*/
		operator bool () const {
			return !isNull();
		}

		/** @brief Check if two refs are the same.
			@return true if two refs are the same.
			@param [in] other ref to check against.
		*/
		bool operator == (const RefBase& other) const {
			return get() == other.get();
		}

		/** @brief Check if two refs are not the same.
			@return true if two refs are not the same.
			@param [in] other ref to check against.
		*/
		bool operator != (const RefBase& other) const {
			return !(*this == other);
		}

		/** @brief Check for nullptr.
			@return true if referenced object pointer is nullptr.
		*/
		bool isNull() const {
			return get() == nullptr;
		}

		/** @brief Check is object is dead.
			@return true if referenced object has no references.
		*/
		bool isExpired() const {
			assert(!isNull());
			return get()->getRefCount() == 0;
		}

		/** @brief Check if object has only one strong reference.
			@return true if referenced object has exactly one reference.
		*/
		bool isUnique() const {
			assert(!isNull());
			return get()->getRefCount() == 1;
		}

		/** @brief Get current raw object pointer.
			@return current raw object pointer.
		*/
		T* get() const {
			return m_ptr;
		}

		/** @brief Replace object pointer without any checks or reference management.
			@note This operation is unsafe!
			@param [in] ptr raw object pointer to set.
		*/
		void set(T* ptr) {
			m_ptr = ptr;
		}

	protected:
		T* m_ptr;	//!< Raw pointer.
	};
#endif /* DOXYGEN_SHOULD_SKIP_THIS */
}
