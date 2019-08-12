#pragma once

#include <vlf/CoreDef.h>

namespace vlf
{
#ifndef DOXYGEN_SHOULD_SKIP_THIS

	/** @brief Smart pointer for reference counted objects.

		@note this is for internal use; public version if IRefCounted
		does not contain aforementioned methods.

		@tparam T object interface (must be derived from IRefCountedImpl)
	*/
	template<typename T>
	struct WeakRef : RefBase<T> {

		/** @brief Initializes object pointer to nullptr.
		*/
		WeakRef() {}

		/** @brief Initializes object pointer to nullptr.
		*/
		WeakRef(std::nullptr_t) {}

		/** @brief Initializes object pointer with ptr and retains a weak reference.
			@param [in] ptr raw pointer to initialize with.
		*/
		explicit WeakRef(T* ptr) { *this = ptr; }

		/** @brief Initializes object pointer with other and retains a weak reference.
			@param [in] other pointer to initialize with.
		*/
		WeakRef(const WeakRef& other) { *this = other; }

		/** @brief Initializes object pointer with other and retains a weak reference.
			@param [in] other pointer to initialize with.
		*/
		WeakRef(const Ref<T>& other) { *this = other; }

		/** @brief Releases reference being held (if any).
		*/
		~WeakRef() {
			reset();
		}

		/** @brief Assign a strong reference.
			@param [in] other strong reference.
			@return reference.
		*/
		WeakRef& operator = (const Ref<T>& other) {
			return assign(other.get());
		}

		/** @brief Assign a weak reference.
			@param [in] other weak reference.
			@return reference.
		*/
		WeakRef& operator = (const WeakRef& other) {
			return assign(other.get());
		}

		/** @brief Assign a raw pointer.
			@note this shares ownership.
			@param [in] ptr raw pointer.
			@return reference.
		*/
		WeakRef& operator = (T* ptr) {
			return assign(ptr);
		}

		/** @brief Assign a nullptr_t.
			@note this releases previously held reference (if any).
			@return reference.
		*/
		WeakRef& operator = (std::nullptr_t) {
			reset();
			return *this;
		}

		/** @brief Assign an object.
			Presumes shared ownership, increases reference count.
			@param [in] ptr raw object pointer to assign.
		*/
		WeakRef& assign(T* ptr) {
			if(this->get() != ptr)
			{
				if(this->get())
					this->get()->releaseWeak();

				this->set(ptr);

				if(this->get())
					this->get()->retainWeak();
			}

			return *this;
		}

		/** @brief Reset reference counted object and assign
			nullptr to the pointer.
		*/
		void reset() {
			assign(nullptr);
		}

		/** @brief Get a strong reference counted smart pointer.
			@return strong reference is object is alive,
			null-initialized ref if object is expired.
		*/
		Ref<T> lock() const {
			return (!this->get() || this->isExpired()) ? Ref<T>() : Ref<T>(this->get());
		}

		/** @brief Make weak reference with relative type.
			@tparam S target interface (must be relative to T)
		*/
		template<typename S> WeakRef<S> as() {
			return WeakRef<S>(static_cast<S*>(this->get()));
		}
	};


	/** @brief Make weak reference to a IRefCounted based object.
		@tparam T object interface (must be derived from IRefCounted)
		@param [in] ptr raw pointer.
	*/
	template<typename T>
	inline WeakRef<T> make_weak_ref(T* ptr) {
		return WeakRef<T>(ptr);
	}


	/** @brief Make weak reference to a IRefCounted based object.
		@tparam S target interface (must be relative to T)
		@tparam T object interface (must be derived from IRefCounted)
		@param [in] ptr raw pointer.
	*/
	template<typename S, typename T>
	inline WeakRef<S> make_weak_ref_as(T* ptr) {
		return WeakRef<S>(static_cast<S*>(ptr));
	}

	/* Documented elsewhere. */
	template<
		typename T>
	Ref<T>&
	Ref<T>::operator = (const WeakRef<T>& other) {
		*this = other.lock();
		return *this;
	}

#endif /* DOXYGEN_SHOULD_SKIP_THIS */
}
