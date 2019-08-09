#pragma once

#include <vlf/CoreDef.h>

namespace vlf
{
#ifndef DOXYGEN_SHOULD_SKIP_THIS

	/* Forward decalrations. */
	template<typename T> struct WeakRef;

#endif /* DOXYGEN_SHOULD_SKIP_THIS */

	/** @brief Smart pointer for reference counted objects.

		Automatically calls retain/release and provides safety
		assertions.

		@tparam T object interface (must be derived from IRefCounted)
	*/
	template<typename T>
	struct Ref : RefBase<T> {

		/** @brief Constructor.
			Initializes object pointer to nullptr.
		*/
		Ref() {}

		/** @brief Initializes object pointer to nullptr.
		*/
		Ref(std::nullptr_t) {}

		/** @brief Initializes object pointer with ptr and retains a reference.
			@note this shares ownership.
			@param [in] ptr raw pointer to initialize with.
		*/
		explicit Ref(T* ptr) { *this = ptr; }

		/** @brief Initializes object pointer with other and retains a reference.
			@note this shares ownership.
			@param [in] other pointer to initialize with.
		*/
		Ref(const Ref& other) { *this = other; }

	#ifndef DOXYGEN_SHOULD_SKIP_THIS

		/** @brief Initializes object pointer with other and retains a reference.
			@param [in] other pointer to initialize with.
		*/
		Ref(const WeakRef<T>& other) { *this = other; }

	#endif /* DOXYGEN_SHOULD_SKIP_THIS */

		/** @brief Releases reference being held (if any).
		*/
		~Ref() {
			reset();
		}

		/** @brief Access pointer.
			@note pointer is checked for null in runtime.
			@return pointer to object.
		*/
		T* operator -> () const {
			assert(!this->isNull());
			return this->get();
		}

		/** @brief Access pointer for initialization.
			@note previously held pointer is released.
			@return pointer to pointer to object.
		*/
		T** getInitReference() {
			if(this->get())
				this->get()->release();

			this->set(nullptr);

			return &this->m_ptr;
		}

		/** @brief Check if two refs are the same.
			@return true if two refs are the same.
			@param [in] other ref to check against.
		*/
		bool operator == (const Ref& other) const {
			return this->get() == other.get();
		}

		/** @brief Check if two refs are not the same.
			@return true if two refs are not the same.
			@param [in] other ref to check against.
		*/
		bool operator != (const Ref& other) const {
			return !(*this == other);
		}

	#ifndef DOXYGEN_SHOULD_SKIP_THIS

		/** @brief Assign a weak reference.
			@param [in] other weak reference.
			@return reference.
		*/
		Ref& operator = (const WeakRef<T>& other);

	#endif /* DOXYGEN_SHOULD_SKIP_THIS */

		/** @brief Assign a strong reference.
			@param [in] other strong reference.
			@return reference.
		*/
		Ref& operator = (const Ref& other) {
			return assign(other.get());
		}

		/** @brief Assign a raw pointer.
			@note this shares ownership.
			@param [in] ptr raw pointer.
			@return reference.
		*/
		Ref& operator = (T* ptr) {
			return assign(ptr);
		}

		/** @brief Assign a nullptr_t.
			@note this releases previously held reference (if any).
			@return reference.
		*/
		Ref& operator = (std::nullptr_t) {
			reset();
			return *this;
		}

		/** @brief Assign an object.
			Presumes shared ownership, increases reference count.
			@param [in] ptr raw object pointer to assign.
		*/
		Ref& assign(T* ptr) {
			if(this->get() != ptr)
			{
				if(this->get())
					this->get()->release();

				this->set(ptr);

				if(this->get())
					this->get()->retain();
			}

			return *this;
		}

		/** @brief Acquire ownership of the object.
			@param [in] ptr raw object pointer to acquire.
		*/
		Ref& acquire(T* ptr) {
			if(this->get() != ptr)
			{
				if(this->get())
					this->get()->release();

				this->set(ptr);
			}

			return *this;
		}

		/** @brief Reset reference counted object and assign
			nullptr to the pointer.
		*/
		void reset() {
			assign(nullptr);
		}

		/** @brief Make smart reference with relative type.
			@tparam S target interface (must be relative to T)
		*/
		template<typename S> Ref<S> as() const {
			return Ref<S>(static_cast<S*>(this->get()));
		}
	};


	/** @brief Make smart reference to a IRefCounted based object.
		@tparam T object interface (must be derived from IRefCounted).
		@param [in] ptr raw pointer.
	*/
	template<typename T>
	inline Ref<T> make_ref(T* ptr) {
		return Ref<T>(ptr);
	}


	/** @brief Make smart reference to a IRefCounted based object.
		@tparam S target interface (must be relative to T).
		@tparam T object interface (must be derived from IRefCounted).
		@param [in] ptr raw pointer.
	*/
	template<typename S, typename T>
	inline Ref<S> make_ref_as(T* ptr) {
		return Ref<S>(static_cast<S*>(ptr));
	}


	/** @brief Acquire ownership of IRefCounted based object.
		@tparam T object interface (must be derived from IRefCounted).
		@param [in] ptr raw pointer.
	*/
	template<typename T>
	inline Ref<T> acquire(T* ptr) {
		Ref<T> ref;
		ref.acquire(ptr);
		return ref;
	}


	/** @brief Acquire ownership of IRefCounted based object with a cast to a given type.
		@tparam S target interface (must be relative to T).
		@tparam T source interface (must be derived from IRefCounted).
		@param [in] ptr raw pointer.
	*/
	template<typename S, typename T>
	inline Ref<S> acquire_as(T* ptr) {
		Ref<S> ref;
		ref.acquire(static_cast<S*>(ptr));
		return ref;
	}
}
