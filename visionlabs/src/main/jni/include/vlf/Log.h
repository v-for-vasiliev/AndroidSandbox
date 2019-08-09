#pragma once
#include <vlf/CoreDef.h>

namespace vlf{
namespace log {

	/** @brief Custom print function type.
	 */
	typedef void (*PrintFunction)(int severity, const char* message);

	/** @brief Log message severity enumeration.
	 */
	enum Severity
	{
		SV_ERROR,				//!< Critical error.
		SV_WARN,				//!< Recoverable error / non-critical issue.
		SV_INFO,				//!< Generic information.
		SV_DEBUG,				//!< Extended (debugging) information (verbose).

	#ifndef NDEBUG
		SV_DEFAULT = SV_DEBUG	//!< Default severity.
	#else
		SV_DEFAULT = SV_INFO	//!< Default severity.
	#endif
	};

	/** @brief Compose formatted log line message and write it.
	 *	@details Will call custom PrintFunction if present. If
	 *	not, will fall back to default output method.
	 *	@see Severity, PrintFunction.
	 *	@param [in] severity message severity.
	 *	@param [in] format message format string.
	 */
	void VLF_API write(int severity, const char* format, ...);

	/** @brief Compose formatted log line message and write it.
	 *	@details Will call write() with severity set to SV_INFO.
	 *	@see write() for details.
	 *	@param [in] format message format string.
	 */
	void VLF_API info(const char* format, ...);

	/** @brief Compose formatted log line message and write it.
	 *	@details Will call write() with severity set to SV_WARN.
	 *	@see write() for details.
	 *	@param [in] format message format string.
	 */
	void VLF_API warn(const char* format, ...);

	/** @brief Compose formatted log line message and write it.
	 *	@details Will call write() with severity set to SV_ERROR.
	 *	@see write() for details.
	 *	@param [in] format message format string.
	 */
	void VLF_API error(const char* format, ...);

	/** @brief Compose formatted log line message and write it.
	 *	@details Will call write() with severity set to SV_DEBUG.
	 *	@see write() for details.
	 *	@param [in] format message format string.
	 */
	void VLF_API debug(const char* format, ...);

	/** @brief Get log severity filter.
	 *	@returns severity level. @see Severity.
	 */
	int VLF_API getSeverityFilter();

	/** @brief Set log severity filter.
	 *	@details Less important messages will be ignored.
	 *	@param [in] severity new severity level. @see Severity.
	 */
	void VLF_API setSeverityFilter(int severity);

	/** @brief Set print function pointer.
	 *	@param [in] function new function pointer. @see PrintFunction.
	 */
	void VLF_API setPrintFunction(PrintFunction function);

	/** @brief Get print function pointer.
	 *	@returns  print function pointer. @see PrintFunction.
	 */
	VLF_API PrintFunction getPrintFunction();

	/** @brief Get default print function pointer.
	 *	@returns  print function pointer. @see PrintFunction.
	 */
	VLF_API PrintFunction getDefaultPrintFunction();

	/** @brief Helper function to restore default print function.
	 *	@detais This is equivalent to:
	 *	@code
	 *		setPrintFunction(getDefaultPrintFunction());
	 *	@endcode
	 */
	inline void restorePrintFunction() {
		setPrintFunction(getDefaultPrintFunction());
	}
}
}
