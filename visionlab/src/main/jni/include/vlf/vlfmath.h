#pragma once
#include <vlf/Types.h>

#include <climits>	// CHAR_BIT
#include <cmath>

#define MATH_PI 3.1415926535897932384626433832795

namespace vlf {
namespace math {

	int
	inline
	select(int a, int b, int mask) {
		return (b & mask) | (a & ~mask); // a if mask is 0, b if ~0
	}

	inline
	unsigned int
	signext(int x) {
		// return an integer where the sign bit of it's
		// argument have been copied in all the bits.
		return x >> (CHAR_BIT * sizeof(int) - 1);
	}

	int
	inline
	sign(int x) {
		return 1 | signext(x);  // if x < 0 then -1, else +1
	}

	int
	inline
	abs(int x) {
		int const mask = signext(x);
		return (x + mask) ^ mask;
	}

	int
	inline
	double_to_int(double d, double magic) {
		volatile union {
			double d;
			long l;
		} cast;

		cast.d = d + magic;
		return cast.l;
	}

	int
	inline
	round_int(double d) {
		return double_to_int(d, 6755399441055744.0);
	}

	int
	inline
	truncate_int(double d) {
		return double_to_int(d, 6755399441055743.5);
	}

	template<typename T>
	inline T min(const T& a, const T& b) {
		return a < b ? a : b;
	}

	template<typename T>
	inline T max(const T& a, const T& b) {
		return a > b ? a : b;
	}

	template<>
	inline int min(const int& a, const int& b){
		return b + ((a - b) & signext(a - b));
	}

	template<>
	inline int max(const int& a, const int& b){
		return a + ((b - a) & ~signext(b - a));
	}

	template<typename T>
	inline Vector2<T> min(const Vector2<T>& a, const Vector2<T>& b) {
		return Vector2<T>(
			min(a.x, b.x),
			min(a.y, b.y));
	}

	template<typename T>
	inline Vector2<T> max(const Vector2<T>& a, const Vector2<T>& b) {
		return Vector2<T>(
			max(a.x, b.x),
			max(a.y, b.y));
	}

	template<typename T>
	inline T clamp(const T& x, const T& a, const T& b) {
		return max(a, min(b, x));
	}

	int
	inline
	roundToGrid1D(int x, int spacing) {
		return (spacing > 1) ? round_int((float)x / (float)spacing) * spacing : x;
	}

	Vector2<int>
	inline
	roundToGrid2D(
		const Vector2<int>& v,
		const Vector2<int>& spacing) {
		return Vector2<int>(
			roundToGrid1D(v.x, spacing.x),
			roundToGrid1D(v.y, spacing.y));
	}

	Rect
	inline
	roundToGrid(
		const Rect& rect,
		const Vector2<int>& spacing) {
		return Rect(
			roundToGrid2D(rect.topLeft(), spacing),
			roundToGrid2D(rect.bottomRight(), spacing));
	}

	template<typename T>
	inline T lerp(const T& a, const T& b, float t) {
		return t * a + (1.f - t) * b;
	}

	template<>
	inline Rect lerp(const Rect& a, const Rect& b, float t) {
		return Rect(
			round_int(lerp((float)a.x, (float)b.x, t)),
			round_int(lerp((float)a.y, (float)b.y, t)),
			round_int(lerp((float)a.width, (float)b.width, t)),
			round_int(lerp((float)a.height, (float)b.height, t)));
	}

	Rect
	inline
	unite(const Rect& a, const Rect& b) {
		return Rect(
			math::min(a.topLeft(), b.topLeft()),
			math::max(a.bottomRight(), b.bottomRight()));
	}

	Rect
	inline
	intersect(const Rect& a, const Rect& b) {
		return Rect(
			math::max(a.topLeft(), b.topLeft()),
			math::min(a.bottomRight(), b.bottomRight()));
	}

	int
	inline
	area(const Rect& rect) {
		return rect.isValid() ? (rect.width * rect.height) : 0;
	}

	Rect
	inline
	shift(const Rect& rect, int dx, int dy) {
		return Rect(
			rect.x + dx,
			rect.y + dy,
			rect.width,
			rect.height);
	}

	Rect
	inline
	scale(const Rect& rect, float k) {
		return Rect(
			round_int(k * rect.x),
			round_int(k * rect.y),
			round_int(k * rect.width),
			round_int(k * rect.height));
	}

	/** Area of intersecion over area of union of two rects. */
	float
	inline
	intersectionToUnion(const Rect& a, const Rect& b) {
		int s = area(intersect(a, b));
		int t = area(a) + area(b) - s;
		return (s && t) ? ((float)s / (float)t) : 0.f;
	}

	/** Area of intersecion over area of minimum of two rects. */
	float
	inline
	intersectionToMin(const Rect& a, const Rect& b) {
		int m = min(area(a), area(b));
		int s = area(intersect(a, b));
		return (s && m) ? ((float)s / (float)m) : 0.f;
	}


	/** Cancel fraction */
	void
	inline
	cancel(std::pair<int,int>& fraction)
	{
		int a = fraction.first,
			b = fraction.second;

		while(a != 0 && b != 0) {
			if(a >= b) a = a % b;
			else b = b % a;
		}

		int c = a + b;

		fraction.first /= c;
		fraction.second /= c;
	}

	/** Round */
	double
	inline
	round(double N)
	{
		double d = double(int64_t(N));
		double D = ceil(N);

		if(D - N > 0.5) return d;
		else return D;
	}

	/** Round */
	float
	inline
	round(float N)
	{
		float d = float(int(N));
		float D = ceil(N);

		if(D - N > 0.5f) return d;
		else return D;
	}

	/** Square (product of the value and itself) */
	template<typename T>
	inline T square(const T& x) { return x * x; }

}
}
