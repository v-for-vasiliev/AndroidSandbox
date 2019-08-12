#pragma once

#include <cstdint>
#include <vlf/CoreDef.h>

namespace vlf
{
	/** @brief Generic stringification function.
		@tparam T any type that can be stringified.
		@param [in] value value that should be stringified.
		@return string.
	*/
	template<typename T>
	inline const char* toString(const T& value) {
		return "NOT AVAILABLE";
	}

	/** @brief A structure that encapsulates an action result
		enumeration.
		@details An enum should specify a result code. By
		default the result is in a special uninitialized
		state which should be interpreted as an error.
		Default success value shoud be defined to zero.
		@tparam enumeration to wrap.
	*/
	template<typename T>
	struct Result
	{
		//! Result value enumeration type.
		typedef T EnumType;

		/** @brief Special predefined values.
		*/
		enum {
			Success = 0,			//!< Result is successful.
			Undefined = (~0u)		//!< Result is initialized (possibly due to internal error).
		};

		/** @brief Initializes undefined result.
		*/
		Result()
			: m_result(Undefined)
		{}

		/** @brief Initializes result.
			@param [in] result value to set.
		*/
		Result(T result)
			: m_result(result)
		{
			// Clients shoud not use 'Undefined' for initialization.
			assert(!isUndefined());
		}

		/** @brief Copies result.
			@param [in] other another result.
		*/
		Result(const Result& other) {
			*this = other;
		}

		/** @brief Copies result.
			@param [in] other another result.
			@return reference for call chaining.
		*/
		Result& operator = (const Result& other) {
			m_result = other.m_result;
			return *this;
		}

		/** @brief Checks if two results are equal.
			@param [in] other another result.
			@return true if results are equal.
		*/
		bool operator == (T other) const {
			return *this == Result(other);
		}

		/** @brief Checks if two results are not equal.
			@param [in] other another result.
			@return true if results are not equal.
		*/
		bool operator != (T other) const {
			return *this != Result(other);
		}

		/** @brief Checks if two results are equal.
			@param [in] other another result.
			@return true if results are equal.
		*/
		bool operator == (const Result& other) const {
			return m_result == other.m_result;
		}

		/** @brief Checks if two results are not equal.
			@param [in] other another result.
			@return true if results are not equal.
		*/
		bool operator != (const Result& other) const {
			return *this != other;
		}

		/** @brief Gets actual result value.
			@return actual result value.
		*/
		T getResult() const {
			return static_cast<T>(m_result);
		}

		/** @brief Checks whether result is defined.
			@return true if result is undefined.
		*/
		bool isUndefined() const {
            return static_cast<int>(getResult()) == static_cast<int>(Undefined);
		}

		/** @brief Checks for an error.
			@return true if result represents an error.
		*/
		bool isError() const {
			return !isOk();
		}

		/** @brief Checks for a success.
			@return true if result represents a success.
		*/
		bool isOk() const {
            return static_cast<int>(getResult()) == static_cast<int>(Success);
		}

		/** @brief Checks for a success.
			@return true if result represents a success.
		*/
		operator bool () const {
			return isOk();
		}

		/** @brief Gets a textual description of the result.
			@note function toString() should be specialized
			for this template type T.
			@return description string.
		*/
		const char* what() const {
			return toString(getResult());
		}

	private:
		uint32_t m_result;	//!< Actual result.
	};
}
