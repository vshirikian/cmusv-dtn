#
# System.make: settings extracted from the oasys configuration
#
# System.make.  Generated from System.make.in by configure.

#
# Programs
#
AR		= ar
RANLIB		= ranlib
DEPFLAGS	= -MMD -MP -MT "$*.o $*.E"
INSTALL 	= /usr/bin/install -c
INSTALL_PROGRAM = ${INSTALL}
INSTALL_DATA 	= ${INSTALL} -m 644
PYTHON		= /usr/bin/python
PYTHON_BUILD_EXT= yes
XSD_TOOL	= xmllint

#
# System-wide compilation flags including the oasys-dependent external
# libraries.
#
SYS_CFLAGS          = -D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64
SYS_EXTLIB_CFLAGS   =  -I/usr/include/tcl8.5 -I/usr/local/include
SYS_EXTLIB_LDFLAGS  =  -ldl -lm  -ltcl8.5 -L/usr/local/lib -Wl,-Bstatic -lprofiler -Wl,-Bdynamic -lexpat -lxerces-c -lreadline  -lz  -ldb-4.8 -lpthread 

#
# Library-specific compilation flags
TCL_LDFLAGS     = -ltcl8.5

