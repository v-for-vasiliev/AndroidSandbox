#pragma once

#include <utility>
#include <cstddef>
#include <vlf/CoreDef.h>

namespace vlf
{
	/**	@brief Helper entity to measure size of dynamic objects in memory.
	*/
	struct Sizer {

		/** @brief Initializes sizer with zero.
		*/
		Sizer() {
			reset();
		}

		/** @brief Initializes sizer with another sizer value.
			@param [in] other another sizer.
		*/
		Sizer(const Sizer& other) {
			*this = other;
		}

		/** @brief Append bytes to current byte count.
			@param [in] bytes number of bytes to append.
		*/
		void append(size_t bytes) {
			m_bytes += bytes;
		}

		/** @brief Append other sizer byte count to current byte count.
			@param [in] other sizer to take bytes from.
		*/
		void append(const Sizer& other) {
			append(other.m_bytes);
		}

		/** @brief Reset byte count to zero. */
		void reset() {
			m_bytes = 0u;
		}

		/** @brief Get current size.
			@return current byte count.
		*/
		size_t getBytes() const {
			return m_bytes;
		}

		/** @brief Get current size.
			@return current size in kbytes (truncated).
		*/
		size_t getKBytes() const {
			return getBytes() >> 10;
		}

		/** @brief Get current size.
			@return current size in mbytes (truncated).
		*/
		size_t getMBytes() const {
			return getKBytes() >> 10;
		}

		/** @brief Get current size.
			@return current size in gbytes (truncated).
		*/
		size_t getGBytes() const {
			return getMBytes() >> 10;
		}

		/** @brief Cast to size type. */
		operator size_t () const {
			return m_bytes;
		}

		/** @brief Check whether size is zero.
			@return true if current byte count is zero.
		*/
		bool isEmpty() const {
			return m_bytes == 0u;
		}

		/** @brief Check whether size is zero.
			@return true if current byte count is not zero.
		*/
		operator bool () const {
			return !isEmpty();
		}

		/** @brief Append bytes to current byte count.
			@param [in] bytes number of bytes to append.
			@return sizer reference for call chaining.
		*/
		Sizer& operator << (size_t bytes) {
			append(bytes);
			return *this;
		}

		/** @brief Append other sizer byte count to current byte count.
			@param [in] other sizer to take bytes from.
			@return sizer reference for call chaining.
		*/
		Sizer& operator << (const Sizer& other) {
			append(other);
			return *this;
		}

		/** @brief Assign value of another sizer.
			@param [in] other sizer to take bytes from.
			@return sizer reference for call chaining.
		*/
		Sizer& operator = (const Sizer& other) {
			m_bytes = other.m_bytes;
			return *this;
		}

		/** @brief Check if two sizers are equal.
			@param [in] other sizer.
			@return true if sizers are equal.
		*/
		bool operator == (const Sizer& other) const {
			return m_bytes == other.m_bytes;
		}

		/** @brief Check if two sizers are not equal.
			@param [in] other sizer.
			@return true if sizers are not equal.
		*/
		bool operator != (const Sizer& other) const {
			return !(*this == other);
		}

		/** Swap contents with an other sizer.
			@param [inout] other sizer to swap with.
		*/
		void swap(Sizer& other) {
			std::swap(m_bytes, other.m_bytes);
		}

	protected:
		size_t m_bytes;	//!< Current measured size in bytes.
	};
}

