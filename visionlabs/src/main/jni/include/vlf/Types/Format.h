#pragma once

#include <vlf/CoreDef.h>

namespace vlf
{
/** @brief Image format.
*/
struct Format {

	/** @brief Format type enumeration.
	*/
	enum Type {
		Unknown,		//!< unknown format.
		B8G8R8X8,		//!< 3 channel 8, bit per channel, B-G-R color order format with 8 bit padding before next pixel.
		R8G8B8X8,		//!< 3 channel 8, bit per channel, R-G-B color order format with 8 bit padding before next pixel.
		B8G8R8,			//!< 3 channel 8, bit per channel, B-G-R color order format.
		R8G8B8,			//!< 3 channel 8, bit per channel, R-G-B color order format.
		R8				//!< 1 channel 8, bit per channel format.
	};

	/** @brief Get color channel count.
		@return color channel count.
		@note returns actual color channel count
		for padded formats; i.e. padding is @a not
		a channel.
	*/
	int getChannelCount() const {
		switch(m_type) {
		case B8G8R8X8:
		case R8G8B8X8:
		case B8G8R8:
		case R8G8B8:
			return 3;
		case R8:
			return 1;
		default:
			return 0;
		}
	}

	/** @brief Get channel step (number of bytes per pixel).
		@return channel step.
		@note padding bytes are considered spare channels.
	*/
	int getChannelStep() const {
		switch(m_type) {
		case B8G8R8X8:
		case R8G8B8X8:
			return 4;
		case B8G8R8:
		case R8G8B8:
			return 3;
		case R8:
			return 1;
		default:
			return 0;
		}
	}

	/** @brief Get color channel size in bits.
		@return color channel size in bits.
	*/
	int getChannelSize() const {
		switch(m_type) {
		case B8G8R8X8:
		case R8G8B8X8:
		case B8G8R8:
		case R8G8B8:
		case R8:
			return 8;
		default:
			return 0;
		}
	}

	/** @brief Get number of bits per pixel.
		@return number of bits per pixel.
	*/
	int getBitDepth() const {
		return getChannelStep() * getChannelSize();
	}

	/** @brief Get number of bytes per pixel.
		@return number of bytes per pixel.
	*/
	int getByteDepth() const {
		if(isBlock()) {
			assert(!"Not implemented");
			return 0;
		}
		else {
			return getBitDepth() >> 3;
		}
	}

	/** @brief Compute row size in bytes.
		@param [in] rowWidth row width in pixels.
		@return row size in bytes.
	*/
	int computePitch(int rowWidth) const {
		return rowWidth * getByteDepth();
	}

	/** @return true if image format has padding bytes.
	*/
	bool isPadded() const {
		switch(m_type) {
		case B8G8R8X8:
		case R8G8B8X8:
			return true;
		default:
			return false;
		}
	}

	/** @return true if image format has 3 channels
		in B-G-R order, false otherwise.
		@note padding is ignored for padded channels.
	*/
	bool isBGR() const {
		switch(m_type) {
		case B8G8R8X8:
		case B8G8R8:
			return true;
		default:
			return false;
		}
	}

	/** @return true if image format is one of block
		types, i.e. B8G8R8X8_BLOCK.
		@note this currently is a stub.
	*/
	bool isBlock() const {
		return false;
	}

	/** @return true if image format is one of valid
		types, i.e. not Unknown.
	*/
	bool isValid() const {
		return m_type != Unknown;
	}

	/** @brief Initializes format structure.
		@details Sets format type to Unknown.
	*/
	Format() : m_type(Unknown) {}

	/** @brief Initializes format structure.
		@param [in] type type value to set.
	*/
	Format(Type type) : m_type(type) {}

	/** @brief Implicit cast to Type. */
	operator Type () const {
		return m_type;
	}

protected:
	Type m_type;	//!< Format type.
};
}


