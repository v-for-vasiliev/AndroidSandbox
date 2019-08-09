#pragma once

#include <vlf/CoreDef.h>

namespace vlf
{
	/**	@brief Image.

		Image objects embed an implicit reference counter to
		automatically track consumers perform assignments
		without unnecessary reallocations in a thread-safe manner.

		Every time you assign or set an other image as the source
		of the current image, the current image data reference
		count is decreased, new data pointer is referenced, and it's
		counter is increased. When data reference count drops to
		zero, the data is deallocated.

		Image can also reference external data without an actual
		ownership. This way you can operate on your data buffer
		using methods of Image and still own it and cotrol it's
		life time.

		@note be careful when initializing Image via set() or create()
		and do NOT provide pointer to data owned by an other image,
		since this way the destination image will be unaware of data
		reference counter which will lead to undefined behavior.
		Always use appropriate overloads to set image data.
	*/
	struct Image {

		/** @brief Initializes an empty image.
		*/
		VLF_API Image();

		/** @brief Initializes an empty image and
			preallocates storage buffer of given size.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
		*/
		VLF_API Image(
			int width,
			int height,
			Format format);

		/** @brief Initializes an empty image and
			preallocates storage buffer of given size.
			Fills image contents with provided data.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@param [in] data image data.
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
		*/
		VLF_API Image(
			int width,
			int height,
			Format format,
			const void* data);

		/** @brief Initializes an image.
			If copy is true, this allocates a new buffer
			of given size and fills image contents with
			provided data (copies it).
			If copy is false (which is default) this will
			just reference the provided data.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@param [in] data image data.
			@param [in] copy [optional] whether to copy or
			reference data.
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
		*/
		VLF_API Image(
			int width,
			int height,
			Format format,
			void* data,
			bool copy = false);

		/** @brief Initializes an image as a reference to
			an other image.
			@param [in] other other image.
		*/
		VLF_API Image(const Image& other);

		/** @brief Initializes an image with contents of an
			other image. Source image is then invalidated.
			@param [in] other other image.
		*/
		VLF_API Image(Image&& other);

		/** @brief Initializes an image with contents of a sub
			image. Since sub images do not handle data life
			time, image contents are always copied.
			@param [in] subImage sub image.
		*/
		VLF_API explicit Image(const SubImage& subImage);

		VLF_API ~Image();

		/** @brief Initializes an empty image and
			preallocates storage buffer of given size.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
			@return true in case of success, false otherwise.
		*/
		VLF_API bool create(int width, int height, Format format);

		/** @brief Initializes an empty image and
			preallocates storage buffer of given size.
			Fills image contents with provided data.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@param [in] data image data.
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
			@return true in case of success, false otherwise.
		*/
		VLF_API bool create(int width, int height, Format format, const void* data);

		/** @brief Initializes an image.
			If copy is true, this allocates a new buffer
			of given size and fills image contents with
			provided data (copies it).
			If copy is false (which is default) this will
			just reference the provided data.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@param [in] data image data.
			@param [in] copy whether to copy or reference data
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
			@return true in case of success, false otherwise.
		*/
		VLF_API bool create(int width, int height, Format format, void* data, bool copy = false);

		/** @brief Initializes an image with provided data.
			If current image is not  empty and it's size
			and format match the provided ones, no memory
			reallocation is performed. Otherwise image is
			re-created to match requirements.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@param [in] data image data.
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
			@return true in case of success, false otherwise.
		*/
		VLF_API bool set(int width, int height, Format format, const void* data);

		/** @brief Initializes an image with provided data.
			If copy is true and if current image is not
			empty and it's size and format match the
			provided ones, no memory reallocation is
			performed. Otherwise image is re-created to
			match requirements.
			If copy is false (which is default) this will
			just reference the provided data.
			@param [in] width image width.
			@param [in] height image height.
			@param [in] format image format.
			@param [in] data image data.
			@param [in] copy whether to copy or reference data.
			@note if memory allocation fails, no exception
			is thrown; function call results in empty image.
			@return true in case of success, false otherwise.
		*/
		VLF_API bool set(int width, int height, Format format, void* data, bool copy = false);

		/** @brief Initializes an image as a reference to
			an other image.
			@param [in] other other image.
			@return true in case of success, false otherwise.
		*/
		VLF_API bool set(const Image& other);

		/** @brief Initializes an image with contents of a sub
			image. Since sub images do not handle data life
			time, image contents are always copied.
			@param [in] subImage sub image.
		*/
		VLF_API bool set(const SubImage& subImage);

		/** @brief Map image contents to a given area.
			@note specifying out of bounds area will result in
			runtime error.
			@param [in] x horisontal coordinate of top left
			corner of image rect to map.
			@param [in] y vertical coordinate of top left
			corner of image rect to map.
			@param [in] width width of image rect to map
			@param [in] height height of image rect to map
			@return sub image corresponding to the given area.
		*/
		VLF_API SubImage map(int x, int y, int width, int height) const;

		/** @brief Map image contents to a given area.
			@note specifying out of bounds area will result in
			runtime error.
			@param [in] rect image rect to extract
			@return sub image corresponding to the given area.
		*/
		SubImage map(const Rect& rect) const {
			return map(rect.x, rect.y, rect.width, rect.height);
		}

		/** @brief Map image contents to a given area.
			@note specifying out of bounds area will result in
			runtime error.
			@note sub image origin is assumed at the top-left
			corner of the existing one.
			@param [in] size image size to map.
			@return sub image corresponding to the given area.
		*/
		SubImage map(const Size& size) const {
			return map(Point2i(0, 0), size);
		}

		/** @brief Map image contents to a given area.
			@note specifying out of bounds area will result in
			runtime error.
			@param [in] origin sub image image origin.
			@param [in] size sub image size to extract.
			@return sub image corresponding to the given area.
		*/
		SubImage map(const Point2i& origin, const Size& size) const {
			return map(origin.x, origin.y, size.x, size.y);
		}

		/** @brief Extract a sub image of this image.
			The new image will have it's own reference count.
			@param [in] x horisontal coordinate of top left
			corner of image rect to extract.
			@param [in] y vertical coordinate of top left
			corner of image rect to extract.
			@param [in] width width of image rect to extract.
			@param [in] height height of image rect to extract.
			@return new image with copied data.
			@note empty image returned in case of an memory
			allocation error.
		*/
		VLF_API Image extract(int x, int y, int width, int height) const;

		/** @brief Extract a sub image of this image.
			The new image will have it's own reference count.
			@param [in] rect image rect to extract.
			@return new image with copied data.
			@note empty image returned in case of an memory
			allocation error.
		*/
		Image extract(const Rect& rect) const {
			return extract(rect.x, rect.y, rect.width, rect.height);
		}

		/** @brief Extract a sub image of this image.
			The new image will have it's own reference count.
			@note new image origin is assumed at the top-left
			corner of the existing one.
			@param [in] size image size to extract.
			@return new image with copied data.
			@note empty image returned in case of an memory
			allocation error.
		*/
		Image extract(const Size& size) const {
			return extract(Point2i(0, 0), size);
		}

		/** @brief Extract a sub image of this image.
			The new image will have it's own reference count.
			@param [in] origin extracted image origin.
			@param [in] size image size to extract.
			@return new image with copied data.
			@note empty image returned in case of an memory
			allocation error.
		*/
		Image extract(const Point2i& origin, const Size& size) const {
			return extract(origin.x, origin.y, size.x, size.y);
		}

		/** @brief Create a copy of this image.
			The new image will have it's own reference count.
			@return new image with copied data.
			@note empty image returned in case of an error.
		*/
		Image clone() const {
			return Image(
				getWidth(),
				getHeight(),
				getFormat(),
				getData());
		}

		/** @brief Convert image format.
			@note specifying out of bounds area will result in
			runtime error.
			@param [inout] dest destination image.
			@param [in] x horisontal coordinate of top left
			corner of image rect to convert.
			@param [in] y vertical coordinate of top left
			corner of image rect to convert.
			@param [in] width width of image rect to convert.
			@param [in] height height of image rect to convert.
			@param [in] format new format.
			@note dest should not be the same as this image.
			@note memory will not be reallocated if dest was
			already created with appropriate dimensions and format.
			@return true if succeeded, false otherwise.
		*/
		VLF_API bool convert(Image& dest, int x, int y, int width, int height, Format format) const;

		/** @brief Convert image format.
			@note specifying out of bounds area will result in
			runtime error.
			@param [inout] dest destination image.
			@param [in] origin converted image origin.
			@param [in] size image size to convert.
			@param [in] format new format.
			@note dest should not be the same as this image.
			@note memory will not be reallocated if dest was
			already created with appropriate dimensions and format.
			@return true if succeeded, false otherwise.
		*/
		bool convert(Image& dest, const Point2i& origin, const Size& size, Format format) const {
			return convert(dest, origin.x, origin.y, size.x, size.y, format);
		}

		/** @brief Convert image format.
			@note specifying out of bounds area will result in
			runtime error.
			@note new image origin is assumed at the top-left
			corner of the existing one.
			@param [inout] dest destination image.
			@param [in] format new format.
			@param [in] size image area size to convert.
			@note dest should not be the same as this image.
			@note memory will not be reallocated if dest was
			already created with appropriate dimensions and format.
			@return true if succeeded, false otherwise.
		*/
		bool convert(Image& dest, const Size& size, Format format) const {
			return convert(dest, Point2i(0, 0), size, format);
		}

		/** @brief Convert image format.
			@note specifying out of bounds area will result in
			runtime error.
			@param [inout] dest destination image.
			@param [in] format new format.
			@param [in] rect image area rect to convert.
			@note dest should not be the same as this image.
			@note memory will not be reallocated if dest was
			already created with appropriate dimensions and format.
			@return true if succeeded, false otherwise.
		*/
		bool convert(Image& dest, const Rect& rect, Format format) const {
			return convert(dest, rect.x, rect.y, rect.width, rect.height, format);
		}

		/** @brief Convert image format.
			@param [inout] dest destination image.
			@param [in] format new format.
			@note dest should not be the same as this image.
			@note memory will not be reallocated if dest was
			already created with appropriate dimensions and format.
			@return true if succeeded, false otherwise.
		*/
		bool convert(Image& dest, Format format) const {
			return convert(dest, getRect(), format);
		}

		/** @brief Rescale image keeping proportions.
			@note Performs bilinear interpolation.
			@param [in] scale factor. Must be positive.
			@return scaled image.
		*/
		VLF_API Image rescale(float scale) const;

		/** @brief Save image as PPM file.
			@details This function saves image contents to disk in PPM format.
			This function is mainly intended for debugging purposes as PPM
			itself is not production format.
			The resulting PPM image will be saved in raw format (`P6`).
			Since PPM supports only GRB 8 bit per channel data, the image
			will be implicitly copied and converted to R8G8B8 format before
			saving to disk. The function might fail if the conversion fails.
			@param path output file path.
			@return true if succeeded, false otherwise.
		*/
		VLF_API bool saveAsPPM(const char* path) const;

		/** @brief Load image from PPM file.
			@details This function loads image contents from disk.
			This function is mainly intended for debugging purposes. It is
			not robust for production use and supports only raw PPM files
			(that start from `P6` header).
			Resulting image format will be R8G8B8 as per PPM spec.
			If image is not empty, it's content will be freed and memory
			will be reallocated to fit in the data from the file if:
			- the file can be opened
			- the file is a raw PPM (`P6`) with 1 byte per color channel
			If an IO error occurs that prevents data to be read from disk,
			the image will be cleaned and the function will return false.
			@param path output file path.
			@return true if succeeded, false otherwise.
		*/
		VLF_API bool loadFromPPM(const char* path);

		/** @brief Assign an other image.
			@param [in] other image to assign.
		*/
		Image& operator = (const Image& other) {
			set(other);

			return *this;
		}

		/** @brief Move an other image.
			@param [in] other image to move.
		*/
		Image& operator = (Image&& other) {
			if(this != &other) {
				release();
				swap(other);
			}

			return *this;
		}

		/** @return true if image data is not allocated. */
		bool isNull() const {
			return getData() == nullptr;
		}

		/** @return true if image is not null and has valid
			dimenstions and format. @see isNull.
		*/
		bool isValid() const {
			return !isNull() &&
				getHeight() > 0 &&
				getWidth() > 0 &&
				getFormat().isValid();
		}

		/** @brief Implicit cast to boolean; results in true if
			image is valid, false if not. @see isValid.
		*/
		operator bool () const {
			return isValid();
		}

		/** @brief Get image scanline data.
			@param [in] y scanline number.
			@return pointer to raw data.
		*/
		VLF_API void* getScanLine(int y);

		/** @brief Get image scanline data.
			@param [in] y scanline number.
			@return pointer to raw data.
		*/
		VLF_API const void* getScanLine(int y) const;

		/** @brief Get image scanline data.
			@param [in] y scanline number.
			@return pointer image data cast to a given type.
		*/
		template<typename T>
		T* getScanLineAs(int y) {
			return reinterpret_cast<T*>(getScanLine(y));
		}

		/** @brief Get image scanline data.
			@param [in] y scanline number.
			@return pointer image data cast to a given type.
		*/
		template<typename T>
		const T* getScanLineAs(int y) const {
			return reinterpret_cast<const T*>(getScanLine(y));
		}

		/** @return pointer to raw image data.
		*/
		void* getData() {
			return m_data;
		}

		/** @return pointer to raw image data.
		*/
		const void* getData() const {
			return m_data;
		}

		/** @return pointer to image data cast to a given type.
		*/
		template<typename T>
		T* getDataAs() {
			return reinterpret_cast<T*>(getData());
		}

		/** @return pointer to image data cast to a given type.
		*/
		template<typename T>
		const T* getDataAs() const {
			return reinterpret_cast<const T*>(getData());
		}

		/** @return size of image pixel row in bytes. */
		int getRowSize() const {
			return getFormat().computePitch(getWidth());
		}

		/** @return image width. */
		int getWidth() const {
			return m_width;
		}

		/** @return image height. */
		int getHeight() const {
			return m_height;
		}

		/** @return image aspect ratio (width to height). */
		float getAspectRatio() const {
			return
				static_cast<float>(getWidth()) /
				static_cast<float>(getHeight());
		}

		/** @return image format. */
		Format getFormat() const {
			return m_format;
		}

		/** @return image size. */
		Size getSize() const {
			return Size(getWidth(), getHeight());
		}

		/** @return image rectangle.
			@note resulting rectangle top left corner is lways at (0, 0).
		*/
		Rect getRect() const {
			return Rect(0, 0, getWidth(), getHeight());
		}

		/** @return actual image size in bytes.
		*/
		int getDataSize() const {
			return getFormat().computePitch(getWidth() * getHeight());
		}

		/** @brief Get actual image size in bytes.
			@param [inout] sizer sizer to append result to.
		*/
		void getDataSize(Sizer& sizer) const {
			sizer.append(getDataSize());
		}

		/** @return true, if this image data was allocated by
			the image itself (via ctor or create()). Otherwise
			returns false.
		*/
		bool ownsData() const {
			return !!m_ref;
		}

		/** @return true if this image shares the same memory
			chunk for it's data as the other one.
			@param [in] other other image to check againts.
		*/
		bool isSharedWith(const Image& other) const {
			return getData() == other.getData();
		}

		/** @brief Swap contents with another image.
			@param [inout] other image to swap with.
		*/
		void swap(Image& other) {
			std::swap(m_data, other.m_data);
			std::swap(m_ref, other.m_ref);
			std::swap(m_height, other.m_height);
			std::swap(m_width, other.m_width);
			std::swap(m_format, other.m_format);
		}

		/** @brief Reset image contents.
		*/
		void reset() {
			Image().swap(*this);
		}

	protected:
		void* m_data;			//!< raw image data.
		int* m_ref;				//!< reference counter. nullptr if image does not own data.
		int m_height;			//!< image height.
		int m_width;			//!< image width.

		Format m_format;		//!< image format (@see Format).

		/** @brief Allocate memory.
			@param [in] size memory region size in bytes.
			@return Memory region pointer.
		*/
		VLF_API static void* allocate(int size);

		/** @brief Free memory.
			@param [in] memory memory region pointer.
		*/
		VLF_API static void deallocate(void* memory);

		/** @brief Increase reference count.
			@return Current reference count.
		*/
		VLF_API int retain();

		/** @brief Decrease reference count.
			@return Current reference count.
		*/
		VLF_API int release();

		/** @brief Obtain reference count.
			@return Current reference count.
		*/
		VLF_API int getRefCount() const;
	};
}
