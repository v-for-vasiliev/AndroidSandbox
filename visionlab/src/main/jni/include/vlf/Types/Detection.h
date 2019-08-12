#pragma once

#include <vlf/CoreDef.h>

namespace vlf {

	/**
	 * @brief Face detection.
	 * @detail Stores a detected face bounding box within a source image frame as well as detection confidence score.
	 * */
	struct Detection {

		Rect rect;		//!< Object bounding box
		float score;	//!< Object detection score

		/**
		 * @brief Initializes a default detection.
		 * @details Detection is initialized in an invalid state.
		 * */
		Detection()
			: score(0.f) {}

		/**
		 * @brief Checks whether a detection is valid.
		 * @details A detection is considered valid if it has a valid rect and score in [0..1] range.
		 * @return true if detection is valid, false otherwise.
		 * */
		bool isValid() const {
			return rect.isValid() && (score >= 0.f && score <= 1.f);
		}
	};
}
