# ------------------------------------------------------------------------
# Makefile: automated SCE Vocoder Tutorial           Author: Rainer Doemer
# ------------------------------------------------------------------------
#
# Modifications: (most recent first)
#
# RD 06/02/08	removed use of sir_import (not needed for FSMD, RTLC;
#		superseded by sce_import for RTL and all others)
# RD 06/02/08	fully integrated the TLM which now is correctly generated
# RD 05/29/08	enabled the TLM simulation (was broken)
# RD 05/29/08	enabled new optimization options for network and link
#		refinement (SCNROPT, SCCROPT)
# RD 05/19/08	in C code generation, changed Motorola_DSP56600_OS_0_DSP
#		to Motorola_DSP56600_OS_0_DSP_NET
# RD 05/19/08	adjusted link parameters from SlaveInt? to MasterInt?
# RD 05/19/08	removed obsolete SIR_IMPORTPROC, SIR_IMPORTBUS
#		(not needed due to use of sce_import instead of sir_import);
# RD 05/19/08	additional adjustments for new release (e.g. fixed comments
#		and messages to match what actually is happening)
# AG 05/14/08	added -c flag to SCAR to enable channel refinement
# AG 09/26/07 	changed to use of sce_import and added time measurements
# RD 08/23/06	added -d option to SCCOPT to avoid intermediate files
# AG 02/23/06 	integrated SystemC export
# DS 02/21/06   add two HW components of type HW_Virtual for stimulus/monitor 
# AG 02/18/06 	switched to HW_DSP (built-in weights) instead of HW_Standard
# AG 02/17/06	switched to new profiler, incl. custom weights in std. DB
# AG 02/08/06	adapted to ever-changing PE behavior names
# GS 02/08/06	changed SW top behavior to Motorola_DSP56600_OS_DSP
# DS 01/31/06	renamed HW_Standard_BF to HW_Standard_HW_BF
# RD 07/27/05	changed code generation options (added -psi -sn)
# RD 07/21/05	renamed HW_Standard_NET back to HW_Standard_BF
# RD 07/20/05	renamed c_link1 to c_link_DSP__HW
# RD 07/12/05	renamed HW_Standard_BF to HW_Standard_NET
# RD 07/12/05	added support to set link parameters (address, interrupt)
# RD 07/11/05	used option -xlx of scc to take care of addl. linker libs
# RD 07/08/05	added support for 'sce_connect'
# RD 09/20/04	switched to vocoder/db
# RD 09/16/04	added -lsc2c_os to libraries to be linked against
# RD 09/15/04	temporarily overclocked DSP to 120MHz!
# RD 07/28/04	exclude 'src' from final tar archive
# RD 07/28/04	set CACHE to local .sce directory and make it usable
# AG 01/22/04	renaming of Motorola bus in database
# JP 01/20/04	added network refinement section
# RD 07/22/03	modified RTL allocation for easier explanation and demo
# RD 07/22/03	renamed HW_Standard_wrap to HW_Standard_BF according to
#		changed conventions in RTL synthesis
# AG 07/03/03	added import of OS PE model before scheduling
# RD 05/27/03	completed support for C code generation (sc2c)
# RD 05/26/03	added targets 'final' and 'loc' (lines of code)
# RD 05/26/03	switched project file creation to official sce_project script
#		and added generation of vocoder.sce
# RD 05/25/03	added statistics displays in refinement flow
# RD 05/23/03	switched to official sce_scripts for allocation, etc.
# RD 04/22/03	fixed the comment for creating "vocoder_final.sce"
# RD 04/18/03	added creation of SCE project file "vocoder_final.sce";
#		added 'stats' target
# RD 04/17/03	adjusted for "official" use
# RD 04/16/03	completed design flow down to RTL
# RD 04/09/03	extended version down to scheduled model
# RD 04/08/03	initial version
# ------------------------------------------------------------------------


# --- DEFINITIONS --------------------------------------------------------

SCE_PATH	= $(SPECC)
SCEDB_PATH	= $(SCE_PATH)/share/sce/db
PROCDB_PATH	= $(SCEDB_PATH)/processors
BUSDB_PATH	= $(SCEDB_PATH)/busses

SYSC_PATH	= /opt/pkg/systemc-2.1.v1

PROC_CACHE	= .sce/processors
COMM_CACHE	= .sce/communication
BUS_CACHE	= .sce/busses
RTL_CACHE	= .sce/rtl

SRC		= src
#SRCPKG		= ../vocoder-spec-clean.tar.gz
FINALPKG	= final.tar.gz

MAINSIRFILES	= VocoderSpec.sir VocoderArch.sir VocoderSched.sir 	\
		VocoderNet.sir VocoderTlm.sir VocoderComm.sir	\
		VocoderFsmd.sir VocoderRTL.sir 			\
		VocoderRTLC.sir

SYSC		= g++ -m32
SYSCOPT		= -Di386
SYSCINC		= -I$(SYSC_PATH)/include $(SCCINC)
SYSCLIB		= -L$(SYSC_PATH)/lib-linux -lsystemc $(SCCLIB)

TIME		= /usr/bin/time

SCC		= scc
#SCCOPT		= -ww -vv -d -xlx -psi -sn
SCCOPT		= -ww -vv -d -xlx
SCCINC		= -I$(SRC)/common
SCCIMP		= -P$(SRC) -P$(SRC)/common -P$(SRC)/lp_analysis \
			-P$(SRC)/open_loop -P$(SRC)/closed_loop \
			-P$(SRC)/codebook -P$(SRC)/update -P$(SRC)/processing
SCCLIB		= $(SRC)/common/pack.o

SIR_RENAME	= sir_rename
SIR_RENAMEOPT	= -v

SIR_NOTE	= sir_note
SIR_NOTEOPT	= -v

SIR_STATS	= sir_stats
SIR_STATSOPT	=

SCSH		= scsh
SCSHOPT		=

SCE_IMPORT      = sce_import
SCE_IMPORTOPT   = -v

SCE_ALLOCATE	= sce_allocate
SCE_ALLOCATEOPT	= -v

SCE_TOP		= sce_top
SCE_TOPOPT	= -v

SCE_MAP		= sce_map
SCE_MAPOPT	= -v

SCE_SCHEDULE	= sce_schedule
SCE_SCHEDULEOPT	= -v

SCE_CONNECT	= sce_connect
SCE_CONNECTOPT	= -v

SCE_PROJECT	= sce_project
SCE_PROJECTOPT	= -v

SCPROF		= $(TIME) scprof
SCPROFOPT	= -v

SCAR		= $(TIME) scar
SCAROPT		=

SCOS		= $(TIME) scos
SCOSOPT		= -v

SCNR		= $(TIME) scnr
#SCNROPT	= -v
SCNROPT		= -v -O -falign -fmerge

SCCR		= $(TIME) sccr
#SCCROPT	= -v
SCCROPT		= -v -O

SCRTLPP		= scrtlpp
SCRTLPPOPT	= -v

SCRTLSTATS	= scrtl_stats
SCRTLSTATSOPT	= -v

SCRTLBIND	= scrtl_bind
SCRTLBINDOPT	= -v

SCRTLSCHED	= scrtl_sched
SCRTLSCHEDOPT	= -v

SCRTL		= $(TIME) scrtl
SCRTLOPT	= -v

SC2C		= $(TIME) sc2c
SC2COPT		= -v

SC2SYSC		= sc2sysc
SC2SYSCOPT	= -v -psi

RM		= rm -f
CP		= cp -f
MV		= mv -f
TAR		= gtar
MKDIR		= mkdir -p
TOUCH		= touch
DIFF		= diff -s
ECHO		= echo


# --- TARGET RULES -------------------------------------------------------


all:	VocoderRTLC.NODTX_OK

dtx:	VocoderSpec.DTX_OK VocoderArch.DTX_OK VocoderSched.DTX_OK 	\
	VocoderNet.DTX_OK VocoderTlm.DTX_OK \
	VocoderComm.DTX_OK VocoderFsmd.DTX_OK \
	VocoderRTL.DTX_OK VocoderRTLC.DTX_OK

final:	$(FINALPKG)

clean:
	$(RM) VocoderSpec VocoderArch VocoderSched VocoderNet		\
	      VocoderTlm VocoderComm VocoderFsmd VocoderRTL VocoderRTLC
	$(RM) vocoder.sce vocoder_final.sce
	$(RM) $(FINALPKG)
	$(RM) *.ins *.sysc
	$(RM) *.si *.sir *.cc *.h *.o *.cpp *.hpp
	$(RM) *.dpr *.prf
	$(RM) *.bit
	$(RM) *.NODTX_OK *.DTX_OK *.SYSC_NODTX_OK *.SYSC_DTX_OK
	$(RM) *.v
	$(RM) -r DSP
	$(RM) *.bak *.BAK *~
	$(RM) core
	$(RM) -r $(PROC_CACHE)
	$(RM) -r $(COMM_CACHE)
	$(RM) -r $(BUS_CACHE)
	$(RM) -r $(RTL_CACHE)

stats:		$(MAINSIRFILES)
	$(SIR_STATS) $(SIR_STATSOPT) $(MAINSIRFILES)

loc:		$(MAINSIRFILES)
	$(SIR_STATS) $(SIR_STATSOPT) -BCI $(MAINSIRFILES)


# --- GENERAL RULES ------------------------------------------------------


.SUFFIXES:
.SUFFIXES:	.ins.sir .ana.sir .sir .ins .cpp .sysc
.SUFFIXES:	.dtx.bit .nodtx.bit .DTX_OK .NODTX_OK
.SUFFIXES:	.sysc_dtx.bit .sysc_nodtx.bit .SYSC_DTX_OK .SYSC_NODTX_OK


# --- instrument for profiling

.sir.ins.sir:
	@$(ECHO) "***"
	@$(ECHO) "*** Instrumenting $* for profiling..."
	@$(ECHO) "***"
	$(SCPROF) -m $(SCPROFOPT) -i $< -o $@ $*

# --- compile for execution

.sir:
	@$(ECHO) "***"
	@$(ECHO) "*** Compiling $* for execution..."
	@$(ECHO) "***"
	$(SCC) $* -sir2out $(SCCOPT) $(SCCINC) $(SCCIMP) -xl $(SCCLIB)

# --- compile for execution with profiling

.ins.sir.ins:
	@$(ECHO) "***"
	@$(ECHO) "*** Compiling $* for execution with profiling..."
	@$(ECHO) "***"
	$(SCC) $* -sir2out $(SCCOPT) $(SCCINC) $(SCCIMP) -xl $(SCCLIB) \
		-i $< -o $@

# --- simulate instrumented code using nodtx mode

.nodtx.bit.NODTX_OK:	$*.ins
	@$(ECHO) "***"
	@$(ECHO) "*** Simulating $* (dtx off)..."
	@$(ECHO) "***"
	$(TIME) ./$*.ins $(SRC)/speechfiles/spch_unx.inp $< nodtx
	@$(ECHO) "***"
	@$(ECHO) "*** Checking $< for correctness..."
	@$(ECHO) "***"
	$(DIFF) $(SRC)/speechfiles/nodtx_good.bit $<
	@$(ECHO) "***"
	@$(ECHO) "*** Simulation successful!"
	@$(ECHO) "***"
	$(TOUCH) $@

# --- simulate using dtx mode

.dtx.bit.DTX_OK:	$*
	@$(ECHO) "***"
	@$(ECHO) "*** Simulating $* (dtx on)..."
	@$(ECHO) "***"
	$(TIME) ./$* $(SRC)/speechfiles/spch_unx.inp $< dtx
	@$(ECHO) "***"
	@$(ECHO) "*** Checking $< for correctness..."
	@$(ECHO) "***"
	$(DIFF) $(SRC)/speechfiles/dtx_good.bit $<
	@$(ECHO) "***"
	@$(ECHO) "*** Simulation successful!"
	@$(ECHO) "***"
	$(TOUCH) $@

# --- export SystemC code

.sir.cpp:
	@$(ECHO) "***"
	@$(ECHO) "*** Exporting SystemC code..."
	@$(ECHO) "***"
	$(SC2SYSC) $* $(SC2SYSCOPT) -i $< -oc $*.cpp -oh $*.hpp

# --- compile SystemC code for execution

.cpp.sysc:
	@$(ECHO) "***"
	@$(ECHO) "*** Compiling SystemC code for execution..."
	@$(ECHO) "***"
	$(SYSC) $(SYSCOPT) $< -o $@ $(SYSCINC) $(SYSCLIB)

# --- simulate SystemC using nodtx mode

.sysc_nodtx.bit.SYSC_NODTX_OK:	$.sysc
	@$(ECHO) "***"
	@$(ECHO) "*** Simulating $* SystemC code (dtx off)..."
	@$(ECHO) "***"
	$(TIME) ./$*.sysc $(SRC)/speechfiles/spch_unx.inp $< nodtx
	@$(ECHO) "***"
	@$(ECHO) "*** Checking $< for correctness..."
	@$(ECHO) "***"
	$(DIFF) $(SRC)/speechfiles/nodtx_good.bit $<
	@$(ECHO) "***"
	@$(ECHO) "*** Simulation successful!"
	@$(ECHO) "***"
	$(TOUCH) $@

# --- simulate SystemC using dtx mode

.sysc_dtx.bit.SYSC_DTX_OK:	$.sysc
	@$(ECHO) "***"
	@$(ECHO) "*** Simulating $* SystemC code (dtx on)..."
	@$(ECHO) "***"
	./$*.sysc $(SRC)/speechfiles/spch_unx.inp $< dtx
	@$(ECHO) "***"
	@$(ECHO) "*** Checking $< for correctness..."
	@$(ECHO) "***"
	$(DIFF) $(SRC)/speechfiles/dtx_good.bit $<
	@$(ECHO) "***"
	@$(ECHO) "*** Simulation successful!"
	@$(ECHO) "***"
	$(TOUCH) $@

# --- back-annotate profiling data and run estimation

.sir.ana.sir:		$*.NODTX_OK
	@$(ECHO) "***"
	@$(ECHO) "*** Back-annotating profiling data and running estimation..."
	@$(ECHO) "***"
	$(SCPROF) -E $(SCPROFOPT) -i $< -o $@ $*


# --- SPECIFIC RULES -----------------------------------------------------


## --- unpack the sources
#
#$(SRC)/testbench.sc:	$(SRCPKG)
#	@$(ECHO) "***"
#	@$(ECHO) "*** Unpacking the sources..."
#	@$(ECHO) "***"
#	$(MKDIR) $(SRC)
#	$(TAR) xvzfC vocoder.tar.gz $(SRC)
#	$(TOUCH) $(SRC)/testbench.sc

# --- compile the sources
VocoderSpec.sir:	$(SRC)/testbench.sc
	@$(ECHO) "***"
	@$(ECHO) "*** Compiling the sources..."
	@$(ECHO) "***"
	$(SCC) testbench -sc2sir $(SCCOPT) $(SCCINC) $(SCCIMP) -xl $(SCCLIB) \
		-i $(SRC)/testbench.sc -o testbench.sir
	$(SIR_RENAME) $(SIR_RENAMEOPT) testbench VocoderSpec
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderSpec.ins.sir:	VocoderSpec.sir

# --- opt.: compile for execution
VocoderSpec:		VocoderSpec.sir

# --- opt.: simulate using dtx mode
VocoderSpec.dtx.bit:	VocoderSpec

# --- opt.: check simulation (dtx)
VocoderSpec.DTX_OK:	VocoderSpec.dtx.bit

# --- compile for execution with profiling
VocoderSpec.ins:	VocoderSpec.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderSpec.nodtx.bit:	VocoderSpec.ins

# --- check simulation (nodtx)
VocoderSpec.NODTX_OK:	VocoderSpec.nodtx.bit

# --- back-annotate raw profiling data
VocoderSpec.prof.sir:	VocoderSpec.sir VocoderSpec.NODTX_OK
	@$(ECHO) "***"
	@$(ECHO) "*** Back-annotating raw profiling data..."
	@$(ECHO) "***"
	$(SCPROF) -p $(SCPROFOPT) -i VocoderSpec.sir -o VocoderSpec.prof.sir \
		VocoderSpec

# --- set top level of design under test
VocoderSpec.2.sir:	VocoderSpec.prof.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Setting top level of design under test..."
	@$(ECHO) "***"
	$(SCE_TOP) $(SCE_TOPOPT) -s Vocoder \
		-i VocoderSpec.prof.sir -o VocoderSpec.2.sir VocoderSpec

# --- allocate the SW component
VocoderSpec.3.sir:	VocoderSpec.2.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Allocating the SW component..."
	@$(ECHO) "***"
	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) \
		-p DSP=Motorola_DSP56600 \
		-i VocoderSpec.2.sir -o VocoderSpec.3.sir VocoderSpec
# (alt.: set DSP clock frequency to 120MHz)
#	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) \
#		-f -a clock=1.2e8 -p DSP=Motorola_DSP56600 \
#		-i VocoderSpec.2.sir -o VocoderSpec.3.sir VocoderSpec

# --- optional: set custom weights for native operations in the SW component
VocoderSpec.4.sir:	VocoderSpec.3.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Setting custom operation weights for the SW component..."
	@$(ECHO) "***"
	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) -p DSP		\
		-wo L_Comp,int=1,2 -wo L_Extract,void=3,3	\
		-wo L_abs,int=1,2 -wo L_add,int=1,2		\
		-wo L_add_c,int=1,2 -wo L_mac,int=1,1		\
		-wo L_macNs,int=1,1 -wo L_msu,int=1,1		\
		-wo L_mult,int=1,1 -wo L_negate,int=1,2		\
		-wo L_shl,int=2,2 -wo L_shr,int=1,2		\
		-wo L_shr_r,int=3,3 -wo L_sub,int=1,2		\
		-wo L_deposit_h,int=1,2 -wo L_deposit_l,int=1,2 \
		-wo Mpy_32,int=7,7 -wo Mpy_32_16,int=3,3	\
		-wo "abs_s,short int=1,1" -wo "add,short int=1,1"	\
		-wo "div_s,short int=3,18" -wo "mac_r,short int=1,2"	\
		-wo "msu_r,short int=1,2" -wo "mult,short int=2,2"	\
		-wo "mult_r,short int=1,2" -wo "negate,short int=1,1"	\
		-wo "norm_l,short int=1,1" -wo "norm_s,short int=1,1"	\
		-wo "extract_l,short int=1,1" -wo "extract_h,short int=1,1" \
		-wo overflow,bool=2,3 -wo "round,short int=1,1"		\
		-wo "saturate,short int=1,3" -wo "shl,short int=2,1"	\
		-wo "shr,short int=2,1" -wo "shr_r,short int=2,2"	\
		-wo "sub,short int=1,1"				\
		-i VocoderSpec.3.sir -o VocoderSpec.4.sir VocoderSpec

# --- map the entire coder onto the SW component
VocoderSpec.5.sir:      VocoderSpec.4.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Mapping the entire coder on the SW component..."
	@$(ECHO) "***"
	$(SCE_MAP) $(SCE_MAPOPT) -p Coder=DSP \
		-i VocoderSpec.3.sir -o VocoderSpec.5.sir VocoderSpec

# --- analyze the profile given the SW mapping
VocoderSpec.ana1.sir:	VocoderSpec.5.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Analyzing the SW mapping..."
	@$(ECHO) "***"
	$(SCPROF) -a $(SCPROFOPT) -i VocoderSpec.5.sir \
		-o VocoderSpec.ana1.sir VocoderSpec

# --- check the estimation result ....

# --- allocate the HW component
VocoderSpec.6.sir:	VocoderSpec.ana1.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Allocating the HW component..."
	@$(ECHO) "***"
	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) -a clock=1e8 -p HW=HW_DSP \
		-i VocoderSpec.ana1.sir -o VocoderSpec.6.sir VocoderSpec

# --- optional: set custom weights for native operations in the HW component
VocoderSpec.7.sir:	VocoderSpec.6.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Setting custom operation weights for the HW component..."
	@$(ECHO) "***"
	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) -p HW		\
		-wo L_Comp,int=1,2 -wo L_Extract,void=3,3	\
		-wo L_abs,int=1,2 -wo L_add,int=1,2		\
		-wo L_add_c,int=1,2 -wo L_mac,int=1,1		\
		-wo L_macNs,int=1,1 -wo L_msu,int=1,1		\
		-wo L_mult,int=1,1 -wo L_negate,int=1,2		\
		-wo L_shl,int=2,2 -wo L_shr,int=1,2		\
		-wo L_shr_r,int=3,3 -wo L_sub,int=1,2		\
		-wo L_deposit_h,int=1,2 -wo L_deposit_l,int=1,2 \
		-wo Mpy_32,int=7,7 -wo Mpy_32_16,int=3,3	\
		-wo "abs_s,short int=1,1" -wo "add,short int=1,1"	\
		-wo "div_s,short int=3,18" -wo "mac_r,short int=1,2"	\
		-wo "msu_r,short int=1,2" -wo "mult,short int=2,2"	\
		-wo "mult_r,short int=1,2" -wo "negate,short int=1,1"	\
		-wo "norm_l,short int=1,1" -wo "norm_s,short int=1,1"	\
		-wo "extract_l,short int=1,1" -wo "extract_h,short int=1,1" \
		-wo overflow,bool=2,3 -wo "round,short int=1,1"		\
		-wo "saturate,short int=1,3" -wo "shl,short int=2,1"	\
		-wo "shr,short int=2,1" -wo "shr_r,short int=2,2"	\
		-wo "sub,short int=1,1"				\
		-i VocoderSpec.6.sir -o VocoderSpec.7.sir VocoderSpec

# --- map the codebook search onto the HW component
VocoderSpec.8.sir:      VocoderSpec.7.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Mapping the codebook search onto the HW component..."
	@$(ECHO) "***"
	$(SCE_MAP) $(SCE_MAPOPT) -p Codebook=HW \
		-i VocoderSpec.6.sir -o VocoderSpec.8.sir VocoderSpec

# --- analyze the profile given the HW/SW mapping
VocoderSpec.ana2.sir:	VocoderSpec.8.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Analyze the HW/SW mapping..."
	@$(ECHO) "***"
	$(SCPROF) -a $(SCPROFOPT) -i VocoderSpec.8.sir \
		-o VocoderSpec.ana2.sir VocoderSpec

# --- estimate the profile given the HW/SW mapping
VocoderSpec.ana3.sir:	VocoderSpec.ana2.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Estimate the HW/SW mapping..."
	@$(ECHO) "***"
	$(SCPROF) -e $(SCPROFOPT) -i VocoderSpec.ana2.sir \
		-o VocoderSpec.ana3.sir VocoderSpec

# --- allocate virtual components for stimulus and monitor
VocoderSpec.9.sir:	VocoderSpec.ana3.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Allocating virtual components for testbench..."
	@$(ECHO) "***"
	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) -a clock=1e8 -p HW_IN=HW_Virtual \
		-a clock=1e8 -p HW_OUT=HW_Virtual \
		-i VocoderSpec.ana3.sir -o VocoderSpec.9.sir VocoderSpec

# --- map the stimulus and monitor blocks to virtual hardware components
VocoderSpec.10.sir:      VocoderSpec.9.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Mapping stimulus and monitor to the virtual components..."
	@$(ECHO) "***"
	$(SCE_MAP) $(SCE_MAPOPT) -p SpeechIn=HW_IN \
		-p SerialOut=HW_OUT \
		-i VocoderSpec.9.sir -o VocoderSpec.10.sir VocoderSpec

# --- import the processing elements into the design
VocoderSpec.arch.in.sir:	VocoderSpec.10.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Importing the PEs into the design..."
	@$(ECHO) "***"
	$(SCE_IMPORT) $(SCE_IMPORTOPT) -a \
		-i VocoderSpec.10.sir -o VocoderSpec.arch.in.sir \
		VocoderSpec

# --- perform architecture refinement
VocoderArch.sir:	VocoderSpec.arch.in.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing architecture refinement..."
	@$(ECHO) "***"
	$(SCAR) VocoderSpec -b -m -c -w $(SCAROPT) \
		-i VocoderSpec.arch.in.sir -o VocoderSpec.arch.sir
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderSpec.arch.sir \
		VocoderSpec VocoderArch
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderArch.ins.sir:	VocoderArch.sir

# --- opt.: compile for execution
VocoderArch:		VocoderArch.sir

# --- opt.: simulate using dtx mode
VocoderArch.dtx.bit:	VocoderArch

# --- opt.: check simulation (dtx)
VocoderArch.DTX_OK:	VocoderArch.dtx.bit

# --- compile for execution with profiling
VocoderArch.ins:	VocoderArch.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderArch.nodtx.bit:	VocoderArch.ins

# --- check simulation (nodtx)
VocoderArch.NODTX_OK:	VocoderArch.nodtx.bit

# --- back-annotate profiling data and run estimation
VocoderArch.ana.sir:	VocoderArch.sir VocoderArch.NODTX_OK

# --- check the estimation result ....

# --- annotate the scheduling decisions
VocoderArch.2.sir:	VocoderArch.ana.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Deciding for all static scheduling..."
	@$(ECHO) "***"
	$(SCE_SCHEDULE) $(SCE_SCHEDULEOPT) \
		-t Motorola_DSP56600_0_DSP -r -s \
		-t HW_DSP_HW -r -s \
		-i VocoderArch.ana.sir -o VocoderArch.2.sir VocoderArch

# --- import components for scheduling refinement
VocoderArch.sched.in.sir:	VocoderArch.2.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Importing components needed for scheduling..."
	@$(ECHO) "***"
	$(SCE_IMPORT) $(SCE_IMPORTOPT) -s \
		-i VocoderArch.2.sir -o VocoderArch.sched.in.sir \
		VocoderArch

# --- perform static scheduling refinement
VocoderArch.sched.tmp.sir:	VocoderArch.sched.in.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing static scheduling refinement..."
	@$(ECHO) "***"
	$(SCAR) VocoderArch -s $(SCAROPT) \
		-i VocoderArch.sched.in.sir -o VocoderArch.sched.tmp.sir

# --- perform dynamic scheduling refinement
VocoderSched.sir:	VocoderArch.sched.tmp.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing dynamic scheduling refinement..."
	@$(ECHO) "***"
	$(SCOS) VocoderArch $(SCOSOPT) \
		-i VocoderArch.sched.tmp.sir -o VocoderArch.sched.sir
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderArch.sched.sir \
		VocoderArch VocoderSched
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderSched.ins.sir:	VocoderSched.sir

# --- opt.: compile for execution
VocoderSched:		VocoderSched.sir

# --- opt.: simulate using dtx mode
VocoderSched.dtx.bit:	VocoderSched

# --- opt.: check simulation (dtx)
VocoderSched.DTX_OK:	VocoderSched.dtx.bit

# --- compile for execution with profiling
VocoderSched.ins:	VocoderSched.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderSched.nodtx.bit:	VocoderSched.ins

# --- check simulation (nodtx)
VocoderSched.NODTX_OK:	VocoderSched.nodtx.bit

# --- export SystemC code
VocoderSched.cpp:	VocoderSched.sir VocoderSched.NODTX_OK

# --- opt.: compile SystemC for execution
VocoderSched.sysc:	VocoderSched.cpp

# --- opt.: simulate SystemC code using dtx mode
VocoderSched.sysc_dtx.bit:	VocoderSched.sysc

# --- opt.: check SystemC simulation (dtx)
VocoderSched.SYSC_DTX_OK:	VocoderSched.sysc_dtx.bit

# --- opt.: simulate SystemC code using nodtx mode
VocoderSched.sysc_nodtx.bit:	VocoderSched.sysc

# --- opt.: check SystemC simulation (nodtx)
VocoderSched.SYSC_NODTX_OK:	VocoderSched.sysc_nodtx.bit

# --- back-annotate profiling data and run estimation
VocoderSched.ana.sir:	VocoderSched.sir VocoderSched.NODTX_OK VocoderSched.cpp

# --- check the estimation result ....

# --- allocate the system bus
VocoderSched.2.sir:	VocoderSched.ana.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Allocating the system bus..."
	@$(ECHO) "***"
	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) \
		-b DspBus=Motorola_DSP56600_PortA \
		-i VocoderSched.ana.sir -o VocoderSched.2.sir VocoderSched

# --- connect the DSP as the bus master
VocoderSched.3.sir:	VocoderSched.2.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Connecting the DSP as bus master..."
	@$(ECHO) "***"
	$(SCE_CONNECT) $(SCE_CONNECTOPT) \
		-c DSP,PortA=DspBus,Master \
		-i VocoderSched.2.sir -o VocoderSched.3.sir VocoderSched

# --- connect the HW as the bus slave
VocoderSched.4.sir:	VocoderSched.3.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Connecting the HW as bus slave..."
	@$(ECHO) "***"
	$(SCE_CONNECT) $(SCE_CONNECTOPT) \
		-c HW,Port0=DspBus,Slave \
		-c HW_IN,Port0=DspBus,Slave \
		-c HW_OUT,Port0=DspBus,Slave \
		-i VocoderSched.3.sir -o VocoderSched.4.sir VocoderSched

# --- map all communication to the system bus
VocoderSched.5.sir:      VocoderSched.4.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Mapping all communication onto the system bus..."
	@$(ECHO) "***"
	$(SCE_MAP) $(SCE_MAPOPT) -b Vocoder=DspBus \
		-i VocoderSched.4.sir -o VocoderSched.5.sir VocoderSched

# --- import components for network refinement
VocoderSched.net.in.sir:	VocoderSched.5.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Importing components needed for network refinement..."
	@$(ECHO) "***"
	$(SCE_IMPORT) $(SCE_IMPORTOPT) -e \
		-i VocoderSched.5.sir -o VocoderSched.net.in.sir \
		VocoderSched

# --- perform network refinement
VocoderNet.sir:	VocoderSched.net.in.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing network refinement..."
	@$(ECHO) "***"
	$(SCNR) VocoderSched $(SCNROPT) \
		-i VocoderSched.net.in.sir -o VocoderSched.net.sir
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderSched.net.sir \
		VocoderSched VocoderNet
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderNet.ins.sir:	VocoderNet.sir

# --- opt.: compile for execution
VocoderNet:		VocoderNet.sir

# --- opt.: simulate using dtx mode
VocoderNet.dtx.bit:	VocoderNet

# --- opt.: check simulation (dtx)
VocoderNet.DTX_OK:	VocoderNet.dtx.bit

# --- compile for execution with profiling
VocoderNet.ins:	VocoderNet.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderNet.nodtx.bit:	VocoderNet.ins

# --- check simulation (nodtx)
VocoderNet.NODTX_OK:	VocoderNet.nodtx.bit

# --- export SystemC code
VocoderNet.cpp:	VocoderNet.sir VocoderNet.NODTX_OK

# --- opt.: compile SystemC for execution
VocoderNet.sysc:	VocoderNet.cpp

# --- opt.: simulate SystemC code using dtx mode
VocoderNet.sysc_dtx.bit:	VocoderNet.sysc

# --- opt.: check SystemC simulation (dtx)
VocoderNet.SYSC_DTX_OK:	VocoderNet.sysc_dtx.bit

# --- opt.: simulate SystemC code using nodtx mode
VocoderNet.sysc_nodtx.bit:	VocoderNet.sysc

# --- opt.: check SystemC simulation (nodtx)
VocoderNet.SYSC_NODTX_OK:	VocoderNet.sysc_nodtx.bit

# --- back-annotate profiling data and run estimation
VocoderNet.ana.sir:	VocoderNet.sir VocoderNet.NODTX_OK VocoderNet.cpp

# --- check the estimation result ....

# --- define the link parameters
VocoderNet.2.sir:	VocoderNet.ana.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Defining the link parameters for the system bus..."
	@$(ECHO) "***"
	$(SCE_MAP) $(SCE_MAPOPT) -l c_link_DSP__HW=0x8000,MasterIntA \
		-l c_link_HW_IN__DSP=0x9000,MasterIntB \
		-l c_link_DSP__HW_OUT=0x9010,MasterIntC \
		-i VocoderNet.ana.sir -o VocoderNet.2.sir VocoderNet

# --- import the system bus into the design
VocoderNet.comm.in.sir:	VocoderNet.2.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Importing the system bus into the design..."
	@$(ECHO) "***"
	$(SCE_IMPORT) $(SCE_IMPORTOPT) -c \
		-i VocoderNet.2.sir -o VocoderNet.comm.in.sir \
		VocoderNet

# --- perform communication refinement to TLM
VocoderTlm.sir:	VocoderNet.comm.in.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing communication refinement to TLM..."
	@$(ECHO) "***"
	$(SCCR) VocoderNet $(SCCROPT) -t \
		-i VocoderNet.comm.in.sir -o VocoderNet.comm.sir
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderNet.comm.sir \
		VocoderNet VocoderTlm
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderTlm.ins.sir:	VocoderTlm.sir

# --- opt.: compile for execution
VocoderTlm:		VocoderTlm.sir

# --- opt.: simulate using dtx mode
VocoderTlm.dtx.bit:	VocoderTlm

# --- opt.: check simulation (dtx)
VocoderTlm.DTX_OK:	VocoderTlm.dtx.bit

# --- compile for execution with profiling
VocoderTlm.ins:	VocoderTlm.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderTlm.nodtx.bit:	VocoderTlm.ins

# --- check simulation (nodtx)
VocoderTlm.NODTX_OK:	VocoderTlm.nodtx.bit

# --- back-annotate profiling data and run estimation
VocoderTlm.ana.sir:	VocoderTlm.sir VocoderTlm.NODTX_OK

# --- check the estimation result ....

# --- perform communication refinement to BFM
VocoderComm.sir:	VocoderNet.comm.in.sir VocoderTlm.ana.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing communication refinement to BFM..."
	@$(ECHO) "***"
	$(SCCR) VocoderNet $(SCCROPT) \
		-i VocoderNet.comm.in.sir -o VocoderNet.comm.sir
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderNet.comm.sir \
		VocoderNet VocoderComm
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderComm.ins.sir:	VocoderComm.sir

# --- opt.: compile for execution
VocoderComm:		VocoderComm.sir

# --- opt.: simulate using dtx mode
VocoderComm.dtx.bit:	VocoderComm

# --- opt.: check simulation (dtx)
VocoderComm.DTX_OK:	VocoderComm.dtx.bit

# --- compile for execution with profiling
VocoderComm.ins:	VocoderComm.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderComm.nodtx.bit:	VocoderComm.ins

# --- check simulation (nodtx)
VocoderComm.NODTX_OK:	VocoderComm.nodtx.bit

# --- back-annotate profiling data and run estimation
VocoderComm.ana.sir:	VocoderComm.sir VocoderComm.NODTX_OK

# --- check the estimation result ....

# --- perform FSMD generation (3-address code generation)
VocoderFsmd.sir:	VocoderComm.ana.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing FSMD (3-address-code) generation..."
	@$(ECHO) "***"
	$(SCRTLPP) $(SCRTLPPOPT) -p 10 -i VocoderComm.ana.sir \
		-o VocoderComm.fsmd.sir Code_10i40_35bits
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderComm.fsmd.sir \
		VocoderComm VocoderFsmd
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderFsmd.ins.sir:	VocoderFsmd.sir

# --- opt.: compile for execution
VocoderFsmd:		VocoderFsmd.sir

# --- opt.: simulate using dtx mode
VocoderFsmd.dtx.bit:	VocoderFsmd

# --- opt.: check simulation (dtx)
VocoderFsmd.DTX_OK:	VocoderFsmd.dtx.bit

# --- compile for execution with profiling
VocoderFsmd.ins:	VocoderFsmd.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderFsmd.nodtx.bit:	VocoderFsmd.ins

# --- check simulation (nodtx)
VocoderFsmd.NODTX_OK:	VocoderFsmd.nodtx.bit

# --- back-annotate profiling data and run estimation
VocoderFsmd.ana.sir:	VocoderFsmd.sir VocoderFsmd.NODTX_OK

# --- check the estimation result ....

# --- collect RTL statistics for RTL allocation
VocoderFsmd.stat1.sir:	VocoderFsmd.ana.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Collecting RTL statistics for RTL allocation..."
	@$(ECHO) "***"
	$(SCRTLSTATS) $(SCRTLSTATSOPT) -i VocoderFsmd.ana.sir \
		-o VocoderFsmd.stat1.sir Code_10i40_35bits

# --- check the RTL statistics ....

# --- allocate the RTL components
VocoderFsmd.2.sir:	VocoderFsmd.stat1.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Allocating RTL components..."
	@$(ECHO) "***"
	$(RM) $(RTL_CACHE)/*
	$(SCE_ALLOCATE) $(SCE_ALLOCATEOPT) -t HW_DSP_HW_BF \
		-g BITWIDTH=32 -r ALU1=alu \
		-g BITWIDTH=32 -r ALU2=L_unit \
		-g BITWIDTH=32 -r ALU3=op_unit \
		-g BITWIDTH=32 -g SIZE=32 -g ADDRWIDTH=5 -r RF0=RF \
		-g BITWIDTH=32 -g SIZE=32 -g ADDRWIDTH=5 -r RF1=RF \
		-g BITWIDTH=32 -g SIZE=32 -g ADDRWIDTH=5 -r RF2=RF \
		-g BITWIDTH=32 -g SIZE=2048 -g ADDRWIDTH=11 -r Mem1=mem \
		-g BITWIDTH=32 -r Bus1=bus \
		-g BITWIDTH=32 -r Bus2=bus \
		-g BITWIDTH=32 -r Bus3=bus \
		-g BITWIDTH=32 -r Bus4=bus \
		-g BITWIDTH=32 -r Bus5=bus \
		-g BITWIDTH=32 -r Bus6=bus \
		-i VocoderFsmd.stat1.sir -o VocoderFsmd.2.sir VocoderFsmd

# --- collect RTL statistics to confirm RTL allocation
VocoderFsmd.stat2.sir:	VocoderFsmd.2.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Collecting RTL statistics to confirm RTL allocation..."
	@$(ECHO) "***"
	$(SCRTLSTATS) $(SCRTLSTATSOPT) -i VocoderFsmd.2.sir \
		-o VocoderFsmd.stat2.sir Code_10i40_35bits

# --- check the RTL statistics ....

# --- make RTL synthesis decisions
VocoderFsmd.syn.sir:	VocoderFsmd.stat2.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Making RTL synthesis decisions..."
	@$(ECHO) "***"
	$(SCRTLBIND) $(SCRTLBINDOPT) -p 10 -s -i VocoderFsmd.stat2.sir \
		-o VocoderFsmd.syn.sir Code_10i40_35bits

# --- import RTL components
VocoderFsmd.rtl.in.sir:	VocoderFsmd.syn.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Importing RTL components..."
	@$(ECHO) "***"
	$(SCE_IMPORT) $(SCE_IMPORTOPT) -t HW_DSP_HW_BF -r \
		-i VocoderFsmd.syn.sir -o VocoderFsmd.rtl.in.sir \
		VocoderFsmd

# --- perform RTL refinement
VocoderRTL.sir:		VocoderFsmd.rtl.in.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing RTL refinement..."
	@$(ECHO) "***"
	$(SCRTL) $(SCRTLOPT) -p 10 -s 5 -i VocoderFsmd.rtl.in.sir \
		-o VocoderFsmd.rtl.sir -V VocoderRTL.v \
		Code_10i40_35bits
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderFsmd.rtl.sir \
		VocoderFsmd VocoderRTL
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderRTL.ins.sir:	VocoderRTL.sir

# --- opt.: compile for execution
VocoderRTL:		VocoderRTL.sir

# --- opt.: simulate using dtx mode
VocoderRTL.dtx.bit:	VocoderRTL

# --- opt.: check simulation (dtx)
VocoderRTL.DTX_OK:	VocoderRTL.dtx.bit

# --- compile for execution with profiling
VocoderRTL.ins:	VocoderRTL.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderRTL.nodtx.bit:	VocoderRTL.ins

# --- check simulation (nodtx)
VocoderRTL.NODTX_OK:	VocoderRTL.nodtx.bit

# --- back-annotate profiling data and run estimation
VocoderRTL.ana.sir:	VocoderRTL.sir VocoderRTL.NODTX_OK

# --- check the estimation result ....

# --- perform software refinement (C code generation)
VocoderRTLC.sir:	VocoderRTL.ana.sir
	@$(ECHO) "***"
	@$(ECHO) "*** Performing software refinement (C code generation)..."
	@$(ECHO) "***"
	-$(MKDIR) DSP
	$(SC2C) $(SC2COPT) VocoderRTL -t Motorola_DSP56600_OSNONE_0_DSP_NET \
		-c DSP/Motorola_DSP56600_OS_DSP.c \
		-h DSP/Motorola_DSP56600_OS_DSP.h \
		-i VocoderRTL.ana.sir -o VocoderRTL.rtlc.sir
	$(SIR_RENAME) $(SIR_RENAMEOPT) -i VocoderRTL.rtlc.sir \
		VocoderRTL VocoderRTLC
	$(SIR_STATS) $(SIR_STATSOPT) $@

# --- instrument for profiling
VocoderRTLC.ins.sir:	VocoderRTLC.sir

# --- opt.: compile for execution
VocoderRTLC:		VocoderRTLC.sir

# --- opt.: simulate using dtx mode
VocoderRTLC.dtx.bit:	VocoderRTLC

# --- opt.: check simulation (dtx)
VocoderRTLC.DTX_OK:	VocoderRTLC.dtx.bit

# --- compile for execution with profiling
VocoderRTLC.ins:	VocoderRTLC.ins.sir

# --- simulate instrumented code using nodtx mode
VocoderRTLC.nodtx.bit:	VocoderRTLC.ins

# --- check simulation (nodtx)
VocoderRTLC.NODTX_OK:	VocoderRTLC.nodtx.bit


# --- PACKAGING RULES ----------------------------------------------------


# --- create an intial SCE project file (for demo start)
vocoder.sce:
	@$(ECHO) "***"
	@$(ECHO) "*** Creating an initial SCE project file..."
	@$(ECHO) "***"
	$(SCE_PROJECT) $(SCE_PROJECTOPT) -c \
		-e "./%e src/speechfiles/spch_unx.inp nodtx.bit nodtx && \
		    diff -s src/speechfiles/nodtx_good.bit nodtx.bit" \
		-I "src/common" \
		-P "src:src/common:src/lp_analysis:src/open_loop" \
		-P "src/closed_loop:src/codebook:src/update:src/processing" \
		-l "-xl src/common/pack.o" \
		-O " -v" \
		vocoder.sce

# --- create the final SCE project file (for demo ressurrection)
vocoder_final.sce:	$(MAINSIRFILES)
	@$(ECHO) "***"
	@$(ECHO) "*** Creating the final SCE project file..."
	@$(ECHO) "***"
	$(SCE_PROJECT) $(SCE_PROJECTOPT) -c \
		-e "./%e src/speechfiles/spch_unx.inp nodtx.bit nodtx && \
		    diff -s src/speechfiles/nodtx_good.bit nodtx.bit" \
		-I "src/common" \
		-P "src:src/common:src/lp_analysis:src/open_loop" \
		-P "src/closed_loop:src/codebook:src/update:src/processing" \
		-l "-xl src/common/pack.o" \
		-O " -v" \
		-a VocoderSpec.sir,,Spec \
		-a VocoderArch.sir,VocoderSpec.sir,Arch \
		-a VocoderSched.sir,VocoderArch.sir,Sched \
		-a VocoderNet.sir,VocoderSched.sir,Net \
		-a VocoderTlm.sir,VocoderNet.sir,TLM \
		-a VocoderComm.sir,VocoderNet.sir,Comm \
		-a VocoderFsmd.sir,VocoderComm.sir,RTLFSMD \
		-a VocoderRTL.sir,VocoderFsmd.sir,RTL \
		-a VocoderRTLC.sir,VocoderRTL.sir,SWCode \
		vocoder_final.sce

# --- package the entire file set (for demo ressurrection)
$(FINALPKG):	vocoder.sce vocoder_final.sce
	$(RM) $(FINALPKG)
	$(TAR) cvzf $(FINALPKG) --exclude src * .sce

# ------------------------------------------------------------------------
# EOF
# ------------------------------------------------------------------------
