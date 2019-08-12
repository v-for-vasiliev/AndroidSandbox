#pragma once

#include <vlf/CoreDef.h>

namespace vlf
{
	/**	@brief Rectangle.
		*/
	struct Rect {
		int x;				//!< Upper left corner x-coordinate.
		int y;				//!< Upper left corner y-coordinate.
		int width;			//!< Rectangle width.
		int height;			//!< Rectangle height.

		/** @brief Initializes a default invalid rectangle.
		*/
		Rect()
			: x(0)
			, y(0)
			, width(0)
			, height(0)
		{}

		/** @brief Initializes a rectangle with given values.
			@param [in] x_ upper left corner x coordinate.
			@param [in] y_ upper left corner y coordinate.
			@param [in] w_ width.
			@param [in] h_ height.
		*/
		Rect(
			int x_,
			int y_,
			int w_,
			int h_)
			: x(x_)
			, y(y_)
			, width(w_)
			, height(h_)
		{}

		/** @brief Initializes a rectangle with given values.
			@param [in] topLeft top-left corner point.
			@param [in] bottomRight bottom-right corner point.
		*/
		Rect(
			const Vector2<int>& topLeft,
			const Vector2<int>& bottomRight)
		{
			set(topLeft, bottomRight);
		}

		/** @brief Copies another rect.
			@param [in] other another rect.
		*/
		Rect(const Rect& other) {
			*this = other;
		}

		/** @brief create new Rect by coordinates
			@param [in] left coord.
			@param [in] top coord.
			@param [in] right coord.
			@param [in] bottom coord.
		*/
		static Rect coords(int x0, int y0, int x1, int y1) {
			return Rect(x0, y0, x1-x0, y1-y0);
		}

		/** @brief Copies another rect.
			@param [in] other another rect.
			@return rect reference for call chaining.
		*/
		Rect& operator = (const Rect& other) {
			if(this != &other) {
				x = other.x;
				y = other.y;
				width = other.width;
				height = other.height;
			}
			return *this;
		}

		/** @brief Checks whether two rects are equal.
			@param [in] other another rect.
			@return true if rects are equal, false otherwise.
		*/
		bool operator == (const Rect& other) const {
			return
				x == other.x &&
				y == other.y &&
				width == other.width &&
				height == other.height;
		}

		/** @brief Checks whether two rects are not equal.
			@param [in] other another rect.
			@return true if rects are not equal, false otherwise.
		*/
		bool operator != (const Rect& other) const {
			return !(*this == other);
		}

		/** @brief Gets rect size (width, height).
			@return rect size.
		*/
		Vector2<int> size() const {
			return Vector2<int>(width, height);
		}

		/** @brief Gets rect top-left corner coordinates.
			@return coordinates vector.
		*/
		Vector2<int> topLeft() const {
			return Vector2<int>(x, y);
		}

		/** @brief Gets rect center coordinates.
			@return coordinates vector.
		*/
		Vector2<int> center() const {
			return Vector2<int>(
				x + width / 2,
				y + height / 2);
		}

		/** @brief Gets rect bottom-right corner coordinates.
			@return coordinates vector.
		*/
		Vector2<int> bottomRight() const {
			return Vector2<int>(x + width, y + height);
		}

		/** @brief Gets rect top y coordinate.
			@return coordinate.
		*/
		int top() const { return y; }

		/** @brief Gets rect bottom y coordinate.
			@return coordinate.
		*/
		int bottom() const { return y + height; }

		/** @brief Gets rect left x coordinate.
			@return coordinate.
		*/
		int left() const { return x; }

		/** @brief Gets rect right x coordinate.
			@return coordinate.
		*/
		int right() const { return x + width; }

		/** @brief Sets rect corner coordinates.
			@param [in] topLeft top-left corner point.
			@param [in] bottomRight bottom-right corner point.
		*/
		void set(const Vector2<int>& topLeft,
				const Vector2<int>& bottomRight) {
			x = topLeft.x;
			y = topLeft.y;
			width = bottomRight.x - x;
			height = bottomRight.y - y;
		}

		/** @brief Adjusts the rect by given amounts.
			@param [in] dx adjustment for upper left corner x coordinate.
			@param [in] dy adjustment for upper left corner y coordinate.
			@param [in] dw adjustment for width.
			@param [in] dh adjustment for height.
		*/
		void adjust(int dx, int dy, int dw, int dh) {
			x += dx;
			y += dy;
			width += dw;
			height += dh;
		}

		/** @brief Copies and adjusts the rect by given amounts.
			@param [in] dx adjustment for upper left corner x coordinate.
			@param [in] dy adjustment for upper left corner y coordinate.
			@param [in] dw adjustment for width.
			@param [in] dh adjustment for height.
			@return adjusted rect.
		*/
		Rect adjusted(int dx, int dy, int dw, int dh) const {
			Rect rect(*this);
			rect.adjust(dx, dy, dw, dh);
			return rect;
		}

		/** @brief Computes rect area (width x height).
			@return rect area.
		*/
		int getArea() const { return width * height; }

		/** @brief Checks whether this rect is inside of another rect.
			@param [in] other rect to check against.
			@return true if this rect is inside of another rect, false otherwise.
		*/
		bool inside(const Rect& other) const {
			return
				topLeft() >= other.topLeft() &&
				bottomRight() <= other.bottomRight();
		}


		/** @brief Checks whether a rect is valid.
			@details A rect is considered valid if it
			has positive width and weight.
			@return true if rect is valid, false otherwise.
		*/
		bool isValid() const {
			return width > 0 && height > 0;
		}
	};
}
