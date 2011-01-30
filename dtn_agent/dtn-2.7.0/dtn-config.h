/* dtn-config.h.  Generated from dtn-config.h.in by configure.  */
/* dtn-config.h.in.  Generated from configure.ac by autoheader.  */

/* whether Bundle Security Protocol support is enabled */
/* #undef BSP_ENABLED */

/* always defined so code can ensure that dtn-config.h is properly included */
#define DTN_CONFIG_STATE 1

/* whether external convergence layer support is enabled */
#define EXTERNAL_CL_ENABLED 1

/* whether external decision plane support is enabled */
#define EXTERNAL_DP_ENABLED 1

/* Define to 1 if you have the <dns_sd.h> header file. */
#define HAVE_DNS_SD_H 1

/* Define to 1 if you have the <inttypes.h> header file. */
#define HAVE_INTTYPES_H 1

/* Define to 1 if you have the `crypto' library (-lcrypto). */
/* #undef HAVE_LIBCRYPTO */

/* Define to 1 if you have the <memory.h> header file. */
#define HAVE_MEMORY_H 1

/* Define to 1 if you have the <stdint.h> header file. */
#define HAVE_STDINT_H 1

/* Define to 1 if you have the <stdlib.h> header file. */
#define HAVE_STDLIB_H 1

/* Define to 1 if you have the <strings.h> header file. */
#define HAVE_STRINGS_H 1

/* Define to 1 if you have the <string.h> header file. */
#define HAVE_STRING_H 1

/* Define to 1 if you have the <sys/stat.h> header file. */
#define HAVE_SYS_STAT_H 1

/* Define to 1 if you have the <sys/types.h> header file. */
#define HAVE_SYS_TYPES_H 1

/* Define to 1 if you have the <unistd.h> header file. */
#define HAVE_UNISTD_H 1

/* directory for local state (default /var) */
#define INSTALL_LOCALSTATEDIR "/var"

/* directory for config files (default /etc) */
#define INSTALL_SYSCONFDIR "/etc"

/* whether LTP support is enabled */
/* #undef LTP_ENABLED */

/* whether norm support is enabled */
/* #undef NORM_ENABLED */

/* whether bonjour support is enabled */
#define OASYS_BONJOUR_ENABLED 1

/* whether oasys lock debugging is enabled */
#define OASYS_DEBUG_LOCKING_ENABLED 1

/* whether oasys memory debugging is enabled */
/* #undef OASYS_DEBUG_MEMORY_ENABLED */

/* Define to the address where bug reports for this package should be sent. */
#define PACKAGE_BUGREPORT ""

/* Define to the full name of this package. */
#define PACKAGE_NAME ""

/* Define to the full name and version of this package. */
#define PACKAGE_STRING ""

/* Define to the one symbol short name of this package. */
#define PACKAGE_TARNAME ""

/* Define to the home page for this package. */
#define PACKAGE_URL ""

/* Define to the version of this package. */
#define PACKAGE_VERSION ""

/* Define to 1 if you have the ANSI C header files. */
#define STDC_HEADERS 1

/* Include oasys configuration state */
#include <../oasys/oasys-config.h>
