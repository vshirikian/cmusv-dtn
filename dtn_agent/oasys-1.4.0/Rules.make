#
# Rules.make: common makefile rules for oasys / DTN2
#
# Rules.make.  Generated from Rules.make.in by configure.

#
# Identifiers for cross-compilation
#
TARGET 		= native
BUILD_SYSTEM	= Linux

#
# Installation settings from the configure script
#
prefix		= /usr
exec_prefix	= ${prefix}
bindir		= ${exec_prefix}/bin
sysconfdir	= ${prefix}/etc
localstatedir	= ${prefix}/var
libdir		= ${exec_prefix}/lib
includedir	= ${prefix}/include
datarootdir	= ${prefix}/share
datadir		= ${datarootdir}
docdir		= ${datarootdir}/doc/${PACKAGE}
mandir		= ${datarootdir}/man
srcdir		= .

#
# Oasys directory substitutions
#
OASYS_INCDIR   = /home/vache/Desktop/cmusv-dtn/dtn_agent/oasys
OASYS_LIBDIR   = /home/vache/Desktop/cmusv-dtn/dtn_agent/oasys/lib
OASYS_ETCDIR   = /home/vache/Desktop/cmusv-dtn/dtn_agent/oasys
OASYS_VERSION  = 1.4.0

#
# Include system settings
#
include $(BUILDDIR)/System.make

#
# Compiler flags
#
CC		= gcc
CXX		= g++
DEBUG 		= -g -fno-inline
DEFS            = -DHAVE_CONFIG_H
DEPFLAGS	= -MMD -MP -MT "$*.o $*.E"
OPTIMIZE	= 
PROFILE         = 
PICFLAGS        = -fPIC -DPIC
SHLIBS		= yes
SHLIB_EXT	= so
LDFLAGS_SHLIB   = -shared -fPIC -DPIC
EXTLIB_CFLAGS   =  $(SYS_EXTLIB_CFLAGS)
EXTLIB_LDFLAGS  =  $(SYS_EXTLIB_LDFLAGS)
EXTRA_CFLAGS	= 
EXTRA_LDFLAGS	= 
INCFLAGS	= -I$(BUILDDIR) -I$(SRCDIR) -I$(OASYS_INCDIR) -I$(SRCDIR)/ext
WARN		= -Wall -W -Wcast-align 
CFLAGS_NOWARN	= $(DEBUG) $(OPTIMIZE) $(PICFLAGS) $(DEPFLAGS) $(PROFILE) \
	          $(DEFS) $(SYS_CFLAGS) $(INCFLAGS) $(EXTLIB_CFLAGS) \
		  $(EXTRA_CFLAGS) -w
CFLAGS    	= $(DEBUG) $(OPTIMIZE) $(PICFLAGS) $(DEPFLAGS) $(PROFILE) \
	          $(DEFS) $(SYS_CFLAGS) $(INCFLAGS) $(EXTLIB_CFLAGS) \
		  $(EXTRA_CFLAGS) $(WARN)
CXXFLAGS_NOWARN	= $(CFLAGS_NOWARN)
CXXFLAGS        = $(CFLAGS)
LDFLAGS         = -L. $(EXTRA_LDFLAGS)

OASYS_LDFLAGS        = /home/vache/Desktop/cmusv-dtn/dtn_agent/oasys/lib/liboasys-1.4.0.a
OASYS_LDFLAGS_STATIC = /home/vache/Desktop/cmusv-dtn/dtn_agent/oasys/lib/liboasys-1.4.0.a
OASYS_COMPAT_LDFLAGS = /home/vache/Desktop/cmusv-dtn/dtn_agent/oasys/lib/liboasyscompat-1.4.0.a

#
# Add a phony rule to make sure this isn't included before the default
#
.PHONY: catchdefault
catchdefault:
	@echo "Rules.make should be included after the default rule is set"
	@exit 1

#
# Dump out the build options
#
.PHONY: buildopts
buildopts:
	@echo "Build options:"
	@echo "  TARGET is: $(TARGET)"
	@echo "  CC: $(CC)"
	@echo "  CXX: $(CXX)"
	@echo " "
	@echo "Options:"
	@echo "  DEBUG: $(DEBUG)"
	@echo "  OPTIMIZE: $(OPTIMIZE)"
	@echo "  PICFLAGS: $(PICFLAGS)"
	@echo "  PROFILE: $(PROFILE)"
	@echo "  DEFS: $(DEFS)"
	@echo "  SYS_CFLAGS: $(SYS_CFLAGS)"
	@echo "  INCFLAGS: $(INCFLAGS)"
	@echo "  EXTRA_CFLAGS: $(EXTRA_CFLAGS)"
	@echo "  LDFLAGS: $(LDFLAGS)"
	@echo " "
	@echo ""
	@echo "Oasys Configuration:"
	@echo "  OASYS_INCDIR: $(OASYS_INCDIR)"
	@echo "  OASYS_LIBDIR: $(OASYS_LIBDIR)"
	@echo "  OASYS_ETCDIR: $(OASYS_ETCDIR)"
	@echo "  OASYS_VERSION: $(OASYS_VERSION)"
	@echo ""
	@echo "External Packages:"
	@echo "  EXTLIB_CFLAGS: $(EXTLIB_CFLAGS)"
	@echo "  EXTLIB_LDFLAGS: $(EXTLIB_LDFLAGS)"


#
# Rule for generating a .o file from the corresponding .cc file;
# automatically creates dependencies via the dependency flags.
#
%.o: %.cc
	@rm -f $@; mkdir -p $(@D)
	$(CXX) $(CXXFLAGS) -c $< -o $@

%.o: %.c
	@rm -f $@; mkdir -p $(@D)
	$(CC) $(CFLAGS) -c $< -o $@

#
# Generate cpp output in .E files.
#
%.E: %.cc
	@rm -f $@; mkdir -p $(@D)
	$(CXX) $(CXXFLAGS) -c $< -E -o $@

%.E: %.c
	@rm -f $@; mkdir -p $(@D)
	$(CC) $(CFLAGS) -c $< -E -o $@

#
# Make stripped binaries from debug binaries
#
%.stripped: %.exe
	cp $< $@ && strip $@

#
# Always include all the .d files that we can find, based on all the
# source files we can find (unless they're specified).
#
ifeq ($(ALLSRCS),)
ALLSRCS := $(shell find . -name \*.cc -o -name \*.c)
endif
DEPFILES := $(ALLSRCS:%.cc=%.d)
DEPFILES := $(DEPFILES:%.c=%.d)
ifneq ($(DEPFILES),)
-include $(DEPFILES)
endif

echosrcdir:
	@echo "srcdir: $(SRCDIR)"

echodep:
	@echo "allsrcs: $(ALLSRCS)"
	@echo "depfiles: $(DEPFILES)"

#
# Some rules for cleaning object files
#
clean: $(SUBDIRS) objclean depclean genclean libclean binclean $(CLEAN_OTHER)

.PHONY: $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -w -C $@ $(MAKECMDGOALS)

.PHONY: objclean
objclean:
	@echo "removing object files..."
	@find . \( -name '*.[Eo]' \) -print | xargs rm -f

.PHONY: depclean
depclean:
	@test -z "$(DEPFILES)" || \
		(echo "removing dependency files..." && \
		rm -f $(DEPFILES))

.PHONY: genclean
genclean:
	@test -z "$(GENFILES)" || \
		(echo "removing generated files: $(GENFILES)..." && \
		rm -f $(GENFILES))

.PHONY: libclean
libclean:
	@test -z "$(LIBFILES)" || \
		(echo "removing library files: $(LIBFILES)..." && \
		rm -f $(LIBFILES))

.PHONY: binclean
binclean:
	@test -z "$(BINFILES)" || \
		(echo "removing binary files: $(BINFILES)..." && \
		rm -f $(BINFILES))

.PHONY: distclean
distclean: clean
	@echo "removing configure generated files: $(CFGFILES)..."
	@test -z "$(CFGFILES)" || rm -f $(CFGFILES)

	@test -z "$(CFGDIRS)" || \
		(echo "removing configure generated directories: $(CFGDIRS)..." && \
		rmdir $(CFGDIRS))
