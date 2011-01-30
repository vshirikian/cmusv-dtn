/* oasys-config.h.  Generated from oasys-config.h.in by configure.  */
/* oasys-config.h.in.  Generated from configure.ac by autoheader.  */

/* configured version of berkeley db */
#define BERKELEY_DB_VERSION 4.8

/* whether external data store support is enabled */
/* #undef EXTERNAL_DS_ENABLED */

/* whether google perftools support is enabled */
#define GOOGLE_PERFTOOLS_ENABLED 1

/* whether google profiling support is enabled */
#define GOOGLE_PROFILE_ENABLED 1

/* Define to 1 if you have the <bluetooth/bluetooth.h> header file. */
/* #undef HAVE_BLUETOOTH_BLUETOOTH_H */

/* Define to 1 if you have the `cfmakeraw' function. */
#define HAVE_CFMAKERAW 1

/* Define to 1 if you have the `cfsetspeed' function. */
#define HAVE_CFSETSPEED 1

/* Define to 1 if you have the <err.h> header file. */
#define HAVE_ERR_H 1

/* Define to 1 if you have the <execinfo.h> header file. */
#define HAVE_EXECINFO_H 1

/* Define to 1 if you have the `getaddrinfo' function. */
#define HAVE_GETADDRINFO 1

/* wether gethostbyname exists */
#define HAVE_GETHOSTBYNAME 1

/* wether gethostbyname_r exists */
#define HAVE_GETHOSTBYNAME_R 1

/* Define to 1 if you have the `getopt_long' function. */
#define HAVE_GETOPT_LONG 1

/* wether inet_aton exists */
#define HAVE_INET_ATON 1

/* wether inet_pton exists */
#define HAVE_INET_PTON 1

/* Define to 1 if you have the <inttypes.h> header file. */
#define HAVE_INTTYPES_H 1

/* Define to 1 if you have the <memory.h> header file. */
#define HAVE_MEMORY_H 1

/* whether pthread_setspecific is defined */
#define HAVE_PTHREAD_SETSPECIFIC 1

/* wether pthread_yield exists */
#define HAVE_PTHREAD_YIELD 1

/* Define to 1 if the system has the type `ptrdiff_t'. */
#define HAVE_PTRDIFF_T 1

/* Define to 1 if you have the <readline/readline.h> header file. */
#define HAVE_READLINE_READLINE_H 1

/* wether sched_yield exists */
#define HAVE_SCHED_YIELD 1

/* Define to 1 if you have the <stdint.h> header file. */
#define HAVE_STDINT_H 1

/* Define to 1 if you have the <stdlib.h> header file. */
#define HAVE_STDLIB_H 1

/* Define to 1 if you have the <strings.h> header file. */
#define HAVE_STRINGS_H 1

/* Define to 1 if you have the <string.h> header file. */
#define HAVE_STRING_H 1

/* Define to 1 if you have the <synch.h> header file. */
/* #undef HAVE_SYNCH_H */

/* Define to 1 if you have the <sys/cdefs.h> header file. */
#define HAVE_SYS_CDEFS_H 1

/* Define to 1 if you have the <sys/stat.h> header file. */
#define HAVE_SYS_STAT_H 1

/* Define to 1 if you have the <sys/types.h> header file. */
#define HAVE_SYS_TYPES_H 1

/* Define to 1 if the system has the type `uint32_t'. */
#define HAVE_UINT32_T 1

/* Define to 1 if you have the <unistd.h> header file. */
#define HAVE_UNISTD_H 1

/* Define to 1 if the system has the type `u_int32_t'. */
#define HAVE_U_INT32_T 1

/* whether xdr_u_int64_t exists */
/* #undef HAVE_XDR_U_INT64_T */

/* whether xdr_u_quad_t exists */
#define HAVE_XDR_U_QUAD_T 1

/* whether berkeley db storage support is enabled */
#define LIBDB_ENABLED 1

/* whether expat is enabled */
#define LIBEXPAT_ENABLED 1

/* whether the mysql embedded server is enabled */
/* #undef LIBMYSQLD_ENABLED */

/* whether mysql support is enabled */
/* #undef MYSQL_ENABLED */

/* whether atomic routines are implemented with a mutex */
/* #undef OASYS_ATOMIC_MUTEX */

/* whether non-atomic "atomic" routines are enabled */
/* #undef OASYS_ATOMIC_NONATOMIC */

/* whether bluetooth support is enabled */
/* #undef OASYS_BLUETOOTH_ENABLED */

/* defined so code can ensure that oasys-config.h is properly included */
#define OASYS_CONFIG_STATE 1

/* whether oasys lock debugging is enabled */
#define OASYS_DEBUG_LOCKING_ENABLED 1

/* whether oasys memory debugging is enabled */
/* #undef OASYS_DEBUG_MEMORY_ENABLED */

/* whether zlib support is enabled */
#define OASYS_ZLIB_ENABLED 1

/* whether zlib contains compressBound */
#define OASYS_ZLIB_HAS_COMPRESS_BOUND 1

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

/* whether postgres support is enabled */
/* #undef POSTGRES_ENABLED */

/* whether readline is actually BSD's libedit */
#define READLINE_IS_EDITLINE 1

/* Define as the return type of signal handlers (`int' or `void'). */
#define RETSIGTYPE void

/* The size of `off_t', as computed by sizeof. */
#define SIZEOF_OFF_T 8

/* whether some sql storage system is enabled */
/* #undef SQL_ENABLED */

/* Define to 1 if you have the ANSI C header files. */
#define STDC_HEADERS 1

/* whether tclreadline is enabled */
#define TCLREADLINE_ENABLED 1

/* whether xerces support is enabled */
#define XERCES_C_ENABLED 1

/* Define to empty if `const' does not conform to ANSI C. */
/* #undef const */

/* Define to `__inline__' or `__inline' if that's what the C compiler
   calls it, or to nothing if 'inline' is not supported under any name.  */
#ifndef __cplusplus
/* #undef inline */
#endif

/* Define to `int' if <sys/types.h> does not define. */
/* #undef mode_t */

/* Define to `unsigned int' if <sys/types.h> does not define. */
/* #undef size_t */

/* Define to empty if the keyword `volatile' does not work. Warning: valid
   code using `volatile' can become incorrect without. Disable with care. */
/* #undef volatile */
