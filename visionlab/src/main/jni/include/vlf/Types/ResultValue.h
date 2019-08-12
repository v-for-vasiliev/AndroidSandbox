#pragma once

#include <vlf/CoreDef.h>

namespace vlf
{
	/**	@brief Addon for Result to output some value aside
		the result.
		@tparam R result enumeration type.
		@tparam V result value type.
		@note All rules for Result template parameter aplly
		to R.
		@note V instance is always held by value despite ->
		operators.
	*/
	template<
		typename R,
		typename V>
	struct ResultValue : Result<R>
	{
		//! Result enumeration type.
		typedef R EnumType;

		//! Result value type.
		typedef V ValueType;

		/** @brief Initializes undefined result.
		*/
		ResultValue()
		{}

		/** @brief Initializes result.
			@param [in] result value to set.
		*/
		ResultValue(R result)
			: Result<R>(result)
		{}

		/** @brief Initializes result.
			@param [in] result result to set.
			@param [in] value value to set.
		*/
		ResultValue(R result, const V& value)
			: Result<R>(result)
			, m_value(value)
		{}

		/** @brief Initializes result.
			@param [in] result result to set.
			@param [in] value value to set.
		*/
		ResultValue(R result, V&& value)
			: Result<R>(result)
			, m_value(std::move(value))
		{}

		/** @brief Gets result value.
			@note Result validated in runtime.
			@return Value.
		*/
		const V& getValue() const {
            assert(this->isOk() && "Acessing value of erroneous result.");
			return m_value;
		}

		/** @brief Gets result value.
			@note Result validated in runtime.
			@return Value.
		*/
		const V* operator -> () const {
			return &getValue();
		}

	protected:
		V m_value;	//!< Actual value.
	};

	/**
	 * @brief Addon for Result to output some value aside the result.
	 * @tparam R result enumeration type.
	 * @tparam V result value type.
	 * @note All rules for Result template parameter apply to R.
	 * @note V instance is always held by value despite -> operators.
	 * @param result the result.
	 * @param value the value.
	 * @return result value struct.
	 * */
	template<
		typename R,
		typename V>
	inline ResultValue<R, V> makeResultValue(R result, const V& value) {
		return ResultValue<R, V>(result, value);
	};
}
