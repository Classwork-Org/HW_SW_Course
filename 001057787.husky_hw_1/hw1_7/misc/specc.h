/************************************************************************/
/* specc.h: SpecC run-time simulation library, compiler-level API	*/
/************************************************************************/
/* Author: Rainer Doemer			first version: 03/21/01 */
/************************************************************************/

/* last update: 11/05/14 */

/* modifications: (most recent first)
 *
 * 11/05/14 RD	removed THREADPOOLING which was "all over the place"
 *		implemented, but used only locally for pipelines and
 *		try-interrupt situations; real thread pooling (avoiding
 *		unnecessary thread/stack creation and deletions) should
 *		be handled only locally at the threading library level
 *		(e.g. PosixThread.h/cc) or in a new level right above
 * 09/18/14 RD	moved class _bitbus from bit/bit.h here
 * 09/18/14 RD	moved class _bit4bus from bit4/bit4.h here
 * 09/18/14 RD	moved class _rbit4bus from bit4/rbit4.h here
 *		(concatenated port mapping is simulator dependent)
 * 09/10/14 RD	merged parallel simulation support by WC into main branch
 * 02/29/12 WC  add support for thread pooling in _specc::pipe and _specc::tryTrapInterrupt
 *		enabled / disabled by macro THREADPOOLING below
 * 03/28/11 RD	patched for compilation with GCC 4.4.x
 * 04/05/06 PC  bug fix. toBit() conversion were missing for buffered bit4
 *              and buffered rbit4. Added them.
 * 02/17/06 EJ  added support for "-Ti" and "-To" tracing options
 * 02/01/06 EJ  added _SetCurrentMethod, _RestoreCurrentMethod, _SetDriver, 
 *              and _RestoreDriver methods
 * 12/01/05 EJ  added support for function call tracing
 * 11/22/05 PC  Added driverid to the class class type and new constructors
 * 11/17/05 PC  Changes for bit4
 * 11/03/05 EJ  added support for option to generate call stack info while tracing
 * 10/27/05 EJ  added VCD_WITH_STRINGS_LOG to TraceLogType
 * 10/24/05 EJ  removed overloaded version of _IsSliced method for sigbus class
 * 10/24/05 EJ  added overloaded version of _IsSliced method for sigbus class
 * 10/17/05 EJ  added support for SVC trace log writer
 * 10/12/05 EJ  removed update method for sigbus;  added virtual event::LogEvent 
 * 		and signal::LogEvent methods
 * 10/06/05 EJ  changed tracing of piped vars to only show 1 value
 * 09/30/05 EJ  added update method for sigbus for tracing
 * 09/26/05 EJ  added _LogInitialState method for piped Array specialization
 * 09/26/05 EJ  added _LogInitialState method for Array specializations
 * 09/21/05 EJ  changed _AddSymbol and _AddSymAlias to return SymReferenceProxy
 * 09/21/05 EJ  changed AddSymbol and AddReference to return SymReference *
 * 09/16/05 EJ  added forward declaration for TraceLogWriter class
 * 09/13/05 EJ  added _SetCurrentMethod and _RestoreCurrentMethod
 * 09/09/05 EJ  added DuplicateName function for tracing 
 * 09/07/05 EJ  added partial support for tracing piped vars
 * 09/02/05 EJ  added Triggered field to _specc::exception (for Tracing support)
 * 09/01/05 EJ  fixed bug in _LogInitialState
 * 09/01/05 EJ  added _LogInitialState method to classes derived from traced_object
 * 08/26/05 EJ  moved enum SymType and class traced_object into separate file
 * 08/26/05 EJ  minor changes for tracing features
 * 08/26/05 EJ  removed _Type enum from traced_object and replaced 
 * 		with _Trace::SymType
 * 08/26/05 EJ  started adding tracing functions 
 * 08/25/05 EJ  moved inline functions for traced_object here
 * 08/25/05 EJ  moved definition of traced_object::_SetID
 * 08/18/05 EJ  added placeholder versions of _AddSymbol and _AddSymAlias
 * 08/18/05 EJ  added traced_object inheritance.  Added _DupFullInstanceName()
 * 08/18/05 EJ  rollback to previous version + added traced_object
 * 08/07/05 RD	reorganized class _specc::class_type, i.e. updating of
 *		current/active instance pointers, etc.
 * 08/05/05 RD	formatted for consistency
 * 08/03/05 EJ	made behaviors and channels derive from class_type
 * 08/02/05 RD  added support for delta cycle counter
 * 07/13/05 EJ  changed size_t parameter in copyFullInstanceName to unsigned int
 * 07/12/05 EJ  added several new classes to support debug mode
 * 06/21/04 PC  Bugfix:(gcc-2.95.2)Fixed signal keyword used without specc scope
 * 05/16/04 PC  buffered and piped class was included into '_specc' scope
 * 05/14/04 PC  signal class was included into '_specc' scope
 * 05/19/03 RD	bug fix: added missing copy constructor for _specc::pipslice
 * 05/15/03 RD	arranged layout of _specc::event to satisfy sizeof() limitation
 * 03/20/03 RD	added support for signal/buffered bit vectors in port mapping
 * 03/07/03 RD	introduced template specialization of buffered class
 *		for bitvectors (which needs put() and get() methods)
 * 03/04/03 RD	fixed right operand for shifts, should be 'unsigned int'
 * 01/08/03 RD	added support for self-triggering events (_specc::auto_event)
 * 01/03/03 RD	added copy constructors to fix post-in/de-crement operators
 * 12/13/02 RD	added support for asynchronous reset of 'buffered' variables
 * 12/12/02 RD	extended event list helper functions
 * 12/06/02 RD	distinguish 'signal'/'buffered' variables in separate lists
 * 11/29/02 RD	class redesign: signal _is_ an event (not, _has_ an event);
 *		also, added support for signal edges in event lists
 * 06/27/02 RD	refined _specc::event_list::Triggers() to return the match
 * 06/26/02 RD	added 'wait' statement with AND semantics (specc::wait_and())
 * 05/20/02 RD	fixed some constructors to use _specc::event explicitly
 * 05/10/02 RD	added methods 'member' for struct/union member access
 * 04/10/02 RD	added handling of bitvector slices (_specc::bufslice,
 *		_specc::sigslice, _specc::pipslice<d>)
 * 04/09/02 RD	extended template classes 'piped', 'buffered', 'signal'
 *		(and their specializations) with special functions for
 *		bitvectors and longlongs
 * 04/02/02 RD	added conversion ops for 'piped', 'buffered', 'signal' arrays
 * 03/28/02 RD	allow 'piped' variables to be initialized
 * 03/27/02 RD	added template specialization for 'piped' arrays (bug fix)
 * 03/26/02 RD	reverted 'private' class members to 'public'
 * 03/26/02 RD	added template classes for 'signal' array accesses
 * 03/26/02 RD	added missing array-assignment operators
 * 03/22/02 RD	added template classes for 'buffered' array accesses
 * 03/08/02 RD	added template specialization for array types
 * 03/07/02 RD	changed 'buffered' constructors to fixed number of parameters
 *		in order to avoid ambiguities
 * 03/06/02 RD	made 'signal' and 'buffered' members private
 * 03/05/02 RD	refined 'signal' and 'buffered' templates (take out auto-
 *		conversion to event pointer; add explicit constructors for
 *		initialization)
 * 02/27/02 RD	added support for 'signal' and 'buffered' variables
 * 11/19/01 RD	bug fix: made channel destructor virtual
 * 09/07/01 RD	bug fix: modified 'piped' template to use static memory
 * 05/16/01 RD	resolved name space competition with user by moving more
 *		definitions into _specc name space and adding prefixes
 * 05/16/01 RD	separated common thread code (in thread.[h|cc]) from
 *		platform-specific thread code (in PosixThread.[h|cc])
 * 05/15/01 RD	cleaned up the source code
 * 05/14/01 RD	added _specc::exception to separate data on heap and stack
 * 05/09/01 RD	added support for exception handling
 * 04/25/01 RD	added copy-assignment to piped template (bug fix)
 * 04/16/01 RD	renamed "SIM_Time" to "sim_time"
 * 04/05/01 RD	added support for new 'pipe' syntax with termination
 * 04/04/01 RD	added support for 'pipe', 'piped'
 * 04/02/01 RD	added _specc::event_list
 * 04/02/01 RD	added _specc::event_ptr
 * 04/02/01 RD	replaced event_queue and ready_queue with generic queue
 * 03/30/01 RD	added generic classes _specc::queue and _specc::queue_elem
 * 03/30/01 RD	added _specc::event_queue
 * 03/29/01 RD	added _specc::ready_queue
 * 03/28/01 RD	added inclusion of "piped.h"
 * 03/21/01 RD	initial version
 */


#ifndef __SPECC_H
#define __SPECC_H

#include <sim.h>
#include <bit.h>
//PC:07/04/05 Changes for bit4
#include <bit4.h>
#include <rbit4.h>
#include <longlong.h>
#include <cassert>
#include "TracedObject.h"

// forward declaration
namespace _Trace
{
	// forward declarations
	class TracingEngine;	
	class TraceLogWriter;
}  // end namespace _Trace


/*** constants and macros ***********************************************/


/* #define GL_DELTA_SUPPORT */		/* "delta" is now obsolete! */

#ifndef NULL
#define NULL	0
#endif


/*** class declarations *************************************************/


	/**************/
	/*** _specc ***/
	/**************/


class _specc			/* wrapper class serving as name space */
{
public:

	/* wrapped types */

enum SignalEdge			/* type of edge for a signal event */
{
EDGE_ANY,
EDGE_FALLING,
EDGE_RISING
};
typedef enum SignalEdge EDGE;

// ejj 10/17/05 added for tracing
enum TraceLogType
{
VCD_LOG,
VCD_WITH_STRINGS_LOG,
SVC_LOG	
};

static const bool CALL_STACK_TRUE = true;
static const bool CALL_STACK_FALSE = false;

	/* wrapped sub-classes */

class event;			/* class for event types */
class auto_event;		/* class for self-triggering events */
class event_ptr;		/* class for a list of events */
class event_list;		/* class for a list of event lists */
class class_type;		/* common base class for behaviors and channels */
class behavior;			/* base class for behaviors */
class channel;			/* base class for channels */
class fork;			/* class for parallel behaviors */
class try_block;		/* class for exception-enabled behaviors */
class exception_block;		/* class for exception handler behaviors */
class exception;		/* class for exception handlers (dynamic) */
class queue;			/* class for queues of threads */
class queue_elem;		/* class for elements in thread queues */
class thread_base;		/* base class for threads (native threads) */
class thread;			/* class for threads */
class piped_base;		/* base class for 'piped' variables */
class buffered_base;		/* base class for 'buffered' variables */
template <class T, unsigned int d>
		   class pip_elem;	/* template for 'piped' arrays */
template <class T> class buf_elem;	/* template for 'buffered' arrays */
template <class T> class sig_elem;	/* template for 'signal' arrays */
template <unsigned int d>
	class pipslice;		/* template for 'piped' bitvector slices */
class bufslice;			/* class for 'buffered' bitvector slices */
class sigslice;			/* class for 'signal' bitvector slices */
template <_bit::len_t len, bool usign>
	class bufbus;		/* class for sliced 'buffered' port mapping */
template <_bit::len_t len, bool usign>
	class sigbus;		/* class for sliced 'signal' port mapping */
class bufbus_elem;		/* class for sliced 'buffered' port map */
class sigbus_elem;		/* class for sliced 'signal' port map */
//PC: 11/09/05 Changes for bit4 resolved
class buf4slice;                 /* class for 'buffered' bitvector slices */
class sig4slice;                 /* class for 'signal' bitvector slices */
template <_bit::len_t len, bool usign>
        class buf4bus;           /* class for sliced 'buffered' port mapping */
template <_bit::len_t len, bool usign>
        class sig4bus;           /* class for sliced 'signal' port mapping */
class buf4bus_elem;              /* class for sliced 'buffered' port map */
class sig4bus_elem;              /* class for sliced 'signal' port map */

//PC: 11/14/05 Changes for bit4 
class rbuf4slice;                 /* class for 'buffered' bitvector slices */
class rsig4slice;                 /* class for 'signal' bitvector slices */
template <_bit::len_t len, bool usign>
        class rbuf4bus;           /* class for sliced 'buffered' port mapping */
template <_bit::len_t len, bool usign>
        class rsig4bus;           /* class for sliced 'signal' port mapping */

//PC 05/14/04
template<class T>		/* class for signal */
	class signal;
//PC 05/16/04
template<class T>
	class buffered ;       /* template class for 'buffered' types */
template<class T, unsigned int d>
	class piped ;          /* template class for 'piped' variables */


	/* wrapped variables */

static sim_time CurrentTime;		/* current simulation time */
static sim_delta CurrentDelta;		/* current delta cycle */
#ifdef GL_DELTA_SUPPORT
static sim_time ScheduleTime;		/* scheduler real time stamp */
#endif /* GL_DELTA_SUPPORT */

static thread	*RootThread;		/* the root thread */

static thread	*CurrentThread;		/* the current thread */

static queue	ReadyQueue;		/* the ready queue */

static queue	WaitforQueue;		/* the event queue for 'waitfor' */

static queue	WaitQueue;		/* the queue for 'wait'ing threads */

static queue	NotifiedQueue;		/* the queue for 'notify'ed threads */

static queue	TryQueue;		/* the queue of 'try'ing threads */

static queue	SuspendQueue;		/* the queue of suspended threads */

static event	*NotifiedEvents;	/* list of notified events */

static event_list *Notify1List;		/* list of 'notifyone'ed events */

static buffered_base *BufferedVars;	/* list of 'buffered' variables */

static buffered_base *SignalVars;	/* list of 'signal' variables */

static auto_event *FirstAutoEvent;	/* first self-triggering event */

static auto_event *LastAutoEvent;	/* last self-triggering event */

static _Trace::TracingEngine * TraceEngine;  /* API level for Tracing features */


	/* wrapped functions */

/* initialize the simulation engine */
static void start(const char * DoFileName = 0, 
			const char * TraceFileName = 0,
			const TraceLogType LogType = SVC_LOG,
			const bool LogStackTrace = false);

static void end(void);		/* clean up the simulation engine */

static void startTrace(const char * DoFileName, /* initialization for tracing*/
			const char * TraceFileName,
			const TraceLogType LogType,
			const bool LogStackTrace); 

static void endTrace(void);    /* clean up for tracing*/

// ejj 09/09/2005
static char * DuplicateName(const char * Name);  /* helper function for tracing*/

static void abort(		/* cleanly abort with a message */
	const char	*Format,	/* (arguments as for printf) */
	...		);

static void par(		/* SpecC 'par' replacement */
	fork		*First,		/* (NULL-terminated list of forks) */
	...		);

static void pipe(		/* SpecC 'pipe' replacement (infinite, old) */
	fork		*First,		/* (NULL-terminated list of forks, */
	...		);		/* NULL-term. list of piped vars.) */

static void pipe(		/* SpecC 'pipe' replacement (finite, new) */
	unsigned int	NumStages,	/* total number of pipeline stages */
	unsigned int	FirstStage,	/* first active stage */
	unsigned int	LastStage,	/* last active stage */
	...		);		/* (list of forks (length NumStages), */
					/* NULL-termin. list of piped vars.)  */

static void tryTrapInterrupt(	/* SpecC 'try-trap-interrupt' replacement */
	try_block	*TryBlock,
	exception_block	*First,
	...		);

static void waitfor(		/* SpecC 'waitfor' replacement */
	sim_time	Delay);

static void wait(		/* SpecC 'wait' replacement (OR semantics) */
	event_ptr	*First,		/* (NULL-terminated list of events) */
	...		);

static void wait_and(		/* SpecC 'wait' replacement (AND semantics) */
	event_ptr	*First,		/* (NULL-terminated list of events) */
	...		);

static void notify(		/* SpecC 'notify' replacement */
	event_ptr	*First,		/* (NULL-terminated list of events) */
	...		);

static void notifyone(		/* SpecC 'notifyone' replacement */
	event_ptr	*First,		/* (NULL-terminated list of events) */
	...		);

static sim_time getCurrentTime(	/* obtain current simulation time */
	void);

static sim_delta getCurrentDelta(	/* obtain current delta cycle */
	void);


#ifdef GL_DELTA_SUPPORT
static sim_time getDeltaTime(	/* SpecC 'delta' replacement */
	void);
static sim_time getRealTime(	/* obtain real time in SpecC time */
	void);
#endif /* GL_DELTA_SUPPORT */

static void fatalError(		/* fatal error, abort simulation */
	const char	*Msg,
	int		ErrorNo);

}; // end of class _specc 


	/*********************/
	/*** _specc::event ***/
	/*********************/

/* class for event types */
// EJ 08/18/05 -- added traced_object inheritance
class _specc::event : public virtual _Trace::traced_object
{
public:
event		*Next;		/* pointer to next notified event (or NULL) */
bool		Notified;	/* flag whether this event has been notified */
bool		Triggered;	/* flag whether this event has been triggered */


event(void);			/* constructor #1 */

virtual ~event(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState(void);

// EJ 10/10/05 -- log an event if tracing is active
// the signal class should override the event method
virtual void LogEvent(void);

virtual bool IsNotified(void);	/* check whether this event has been notified */

virtual void Notify(void);	/* notify this event */

static void UnNotifyAll(void);	/* un-notify all notified events */


virtual bool IsValidEdge(	/* signal edge event filter */
	EDGE	Edge);		/* (dummy to be overloaded) */

virtual bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (dummy to be overloaded) */

virtual event_ptr *MappedEvents(/* get list of port-mapped events (or NULL) */
	void);			/* (dummy to be overloaded) */
};


	/**************************/
	/*** _specc::auto_event ***/
	/**************************/


class _specc::auto_event	/* class for self-triggering events */
	: public _specc::event	/* (every auto-event is also an event) */
{
public:
sim_time	Period;		/* cycle time of the self-trigger */
sim_time	TriggerTime;	/* time of the next triggering */
auto_event	*Pred;		/* predecessor in auto-event list */
auto_event	*Succ;		/* successor in auto-event list */


auto_event(			/* constructor #1 */
	sim_time	Period);

~auto_event(void);		/* destructor */


void Insert(void);		/* insert into the sorted list */

void Delete(void);		/* take out of the sorted list */

void TriggerAndUpdate(void);	/* trigger, update and re-order this event */
};


	/*************************/
	/*** _specc::event_ptr ***/
	/*************************/


class _specc::event_ptr		/* class for a list of events */
{
public:
event_ptr	*Next;		/* pointer to the next element (or NULL) */
event		*Event;		/* pointer to the event */
EDGE		Edge;		/* signal edge */


event_ptr(			/* constructor #1 */
	event		*Event,
	EDGE		Edge = EDGE_ANY,
	event_ptr	*Next = NULL);

~event_ptr(void);		/* destructor */
};


	/**************************/
	/*** _specc::event_list ***/
	/**************************/


class _specc::event_list	/* class for a list of event lists */
{
public:
event_list	*Next;		/* pointer to next event list (or NULL) */
event_ptr	*EventList;	/* pointer to the first element of this list */


event_list(			/* constructor #1 */
	event_ptr	*EventList);

~event_list(void);		/* destructor */


event *Triggers(		/* check whether any event matches this list */
	event_ptr	*EventList2);	/* (returns first match, or NULL) */
};


	/**************************/
	/*** _specc::class_type ***/
	/**************************/

// EJ 08/18/05 -- added traced_object inheritance
/* base class for behaviors and channels */
class _specc::class_type : public _Trace::traced_object
{
public:
const char	*_ClassName;	/* run-time debugging infos */
const char	*_InstanceName;
class_type	*_Parent;

unsigned int _DriverID;
class_type(			/* constructor #1 */
	const char	*Class_Name,
	const char	*InstanceName,
	class_type	*Parent);

class_type(void);		/* constructor #2 */

//PC: 11/17/05 Changes for resolved bit4
class_type(                     /* constructor #3 */
        unsigned int     driverid);
   
class_type(                     /* constructor #4 */
        unsigned int     driverid,
        const char      *Class_Name,
        const char      *InstanceName,
        class_type      *Parent);

~class_type(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState();


// Copy the full name which includes the names of all parent instances:
// "Main.Adder.xor1"  
// If NumChars is less than the full length of the 
// instance name, then the rightmost chars will be copied.  The thought is that the 
// leftmost chars: "Main. ..." are not as important as the rightmost chars: "... xor1"
char *_CopyFullInstanceName(
	char		*Dest,
	unsigned int	NumChars);

// ej 12/01/05 -- changed function signature to support function call traces
// update thread pointer before all public methods
// inputs: MethodName, (and "this" ptr)
// outputs: updates the pPrevInst and pPrevMethodName pointers
void _SetCurrentInst(const char * MethodName, 
	_specc::class_type ** pPrevInst, 
	const char ** pPrevMethodName);

// ej 12/01/05 -- changed function signature to support function call traces
// restore thread pointer after all public methods
void _RestoreCurrentInst(		
	class_type *PreviousInst,
	const char * PreviousMethod);

// ej 12/01/05 -- changed function signature to support function call traces
// update thread pointers in 'main' methods
// inputs: MethodName, (and "this" ptr)
// outputs: updates the pPrevInst and pPrevMethodName pointers
void _SetActiveInst(const char * MethodName, 	
	_specc::class_type ** pPrevInst, 
	const char ** pPrevMethodName);
	
// ej 12/01/05 -- changed function signature to support function call traces
// restore thread pointer after all public methods
void _RestoreActiveInst(		
	class_type *PreviousInst,
	const char * PreviousMethod);
	
// inputs: MethodName, (and "this" ptr)
// outputs: updates the pPrevMethodName pointer
void _SetCurrentMethod(const char * MethodName, 
	const char ** pPrevMethodName);	
	
// restore method name after method call
void _RestoreCurrentMethod(		
	const char * PreviousMethod);
	
// update thread pointers in 'main' methods when using resolved bit4 type
// inputs: ("this" ptr)
// outputs: updates the pPrevInst pointer
void _SetDriver(_specc::class_type ** pPrevInst);
	
// restore thread pointer after all 'main' method when using resolved bit4 type
void _RestoreDriver(class_type *PreviousInst);	
	
protected:
// EJ 08/18/05 -- added DupFullInstanceName
// Returns a heap-allocated copy of the full instance name.
// The caller is then responsible for deleting the pointer!!!
char *_DupFullInstanceName() const;

// overloaded version that creates a copy of a child instance's name
char *_DupFullInstanceName(const char * ChildName) const;
};


	/************************/
	/*** _specc::behavior ***/
	/************************/


class _specc::behavior: public _specc::class_type /* base class for behaviors */
{
public:
behavior(void);			/* constructor #1 */

behavior(			/* constructor #2 */
//PC: 11/17/05 Changes for resolved bit4 
	unsigned int     driverid,
	const char	*Bhvr_Name,
	const char	*Inst_Name,
	class_type	*Parent);

//PC: 11/17/05 Changes for resolved bit4
behavior(                       /* constructor #3 */
        unsigned int     driverid);


virtual ~behavior(void);	/* destructor */

virtual void main(void) = 0;	/* mandatory main method */

};


	/***********************/
	/*** _specc::channel ***/
	/***********************/


class _specc::channel: public _specc::class_type	/* base class for channels */
{
public:

channel(void);			/* constructor #1 */

channel(			/* constructor #2 */
	const char	*Bhvr_Name,
	const char	*Inst_Name,
	class_type	*Parent);

virtual ~channel(void);		/* destructor */
};


	/********************/
	/*** _specc::fork ***/
	/********************/


class _specc::fork		/* class for parallel behaviors */
{
public:
behavior	*Behavior;	/* the concurrent behavior */


fork(				/* constructor #1 */
	behavior	*Behavior);

~fork(void);			/* destructor */
};


	/*************************/
	/*** _specc::try_block ***/
	/*************************/


class _specc::try_block		/* class for exception-enabled behaviors */
{
public:
behavior	*Behavior;	/* the sensitive behavior (NULL if empty) */


try_block(			/* constructor #1 */
	behavior	*Behavior);

~try_block(void);		/* destructor */
};


	/*******************************/
	/*** _specc::exception_block ***/
	/*******************************/


class _specc::exception_block	/* class for exception handler behaviors */
{
public:
bool		IsTrap;		/* flag for trap (true) or interrupt (false) */
behavior	*Behavior;	/* the exception handler (NULL if empty) */
event_ptr	*EventList;	/* list of triggering events */


exception_block(		/* constructor #1 */
	bool		IsTrap,
	behavior	*Behavior,
	event_ptr	*First,
	...);

~exception_block(void);		/* destructor */
};


	/*************************/
	/*** _specc::exception ***/
	/*************************/


class _specc::exception		/* class for exception handlers (dynamic) */
{
public:
exception	*Next;		/* next exception in list (or NULL) */
bool		IsTrap;		/* flag for trap (true) or interrupt (false) */
behavior	*Behavior;	/* the exception handler (NULL if empty) */
event_ptr	*EventList;	/* list of triggering events */
// ej 09/02/2005
event           *Triggered;     /* last event to trigger this exception */


exception(			/* constructor #1 */
	exception_block	*ExceptionBlock);

~exception(void);		/* destructor */


exception *FirstMatch(		/* obtain first match with this event list */
	event_ptr	*EventList2);	/* (or NULL if not found) */

exception *FirstMatch(		/* obtain first match with notified events */
	void);				/* (or NULL if not found) */
};


	/**************************/
	/*** _specc::piped_base ***/
	/**************************/

// EJ 08/18/05 -- added traced_object inheritance
/* base class for 'piped' variables */
class _specc::piped_base : public _Trace::traced_object
{
public:
piped_base(void);		/* constructor #1 */

virtual ~piped_base(void);	/* destructor */

virtual void update(void) = 0;	/* mandatory update method */

protected:
};


	/*****************************/
	/*** _specc::buffered_base ***/
	/*****************************/

// EJ 08/18/05 -- added traced_object inheritance
/* base class for 'buffered' variables */
class _specc::buffered_base : public virtual _Trace::traced_object
{
public:
buffered_base	*Next;		/* next 'buffered' variable (or NULL) */
buffered_base	*Prev;		/* previous 'buffered' variable (or NULL) */
event_ptr	*UpdateEvents;	/* list of events that update the buffer */
event		*ResetSignal;	/* asynchronous reset signal (or NULL) */
bool		ResetActiveHi;	/* flag for reset active lo/hi (false/true) */


buffered_base(			/* constructor #1 (for 'buffered' variables) */
	event_ptr	*UpdateEvents,
	event		*ResetSignal = NULL,
	bool		ResetActiveHi = false);

buffered_base(			/* constructor #2 (for 'signal' variables) */
	event		*SignalEvent);

virtual ~buffered_base(void);	/* destructor */


virtual void update(void) = 0;	/* mandatory update method */

virtual void reset(void) = 0;	/* mandatory reset method */
};


	/****************************************/
	/*** piped template (general version) ***/
	/****************************************/


template<class T, unsigned int d>
class _specc::piped :			/* template class for 'piped' variables */
	public _specc::piped_base	/* is based on piped_base */
{
public:
T		Value[d+1];	/* static, FIFO-type storage */


inline piped(void);		/* constructor #1 */

explicit inline piped(		/* constructor #2 (with init) */
	const T		Init);	/* initializer */

inline piped(			/* copy constructor (#3) */
	const piped<T,d> &Orig);

inline ~piped(void);		/* destructor */

//ejj 09/07/05
inline void _LogInitialState(void);

inline operator T() const;	/* conversion operator (read access) */


inline _specc::piped<T,d> &operator=(	/* assignment operator #1 */
	T		Var);

inline _specc::piped<T,d> &operator=(	/* assignment operator #1b */
	const _specc::piped<T,d> &PipedVar);

inline _specc::piped<T,d> &operator+=(	/* assignment operator #2 */
	T		Var);

inline _specc::piped<T,d> &operator-=(	/* assignment operator #3 */
	T		Var);

inline _specc::piped<T,d> &operator*=(	/* assignment operator #4 */
	T		Var);

inline _specc::piped<T,d> &operator/=(	/* assignment operator #5 */
	T		Var);

inline _specc::piped<T,d> &operator%=(	/* assignment operator #6 */
	T		Var);

inline _specc::piped<T,d> &operator^=(	/* assignment operator #7 */
	T		Var);

inline _specc::piped<T,d> &operator&=(	/* assignment operator #8 */
	T		Var);

inline _specc::piped<T,d> &operator|=(	/* assignment operator #9 */
	T		Var);

inline _specc::piped<T,d> &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline _specc::piped<T,d> &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline _specc::piped<T,d> &operator++();/* increment operator #1 (pre-) */

inline _specc::piped<T,d> operator++(	/* increment operator #2 (post-) */
	int);

inline _specc::piped<T,d> &operator--();/* decrement operator #1 (pre-) */

inline _specc::piped<T,d> operator--(	/* decrement operator #2 (post-) */
	int);


void update(void);		/* shift the data in the pipeline */


// special pass-through functions for bitvectors and longlongs

inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

// bitvector slices

inline _specc::pipslice<d> operator()(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::pipslice<d> operator()(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::pipslice<d> operator()(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::pipslice<d> operator()(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// struct/union member access

template <class MT>
inline _specc::pip_elem<MT,d> member(		/* member access "operator" */
	int		CharOffset);

template <class MT>
inline const _specc::pip_elem<MT,d> member(	/* const member access "op." */
	int		CharOffset) const;
};


	/*******************************************************/
	/*** piped template (specialization for array types) ***/
	/*******************************************************/


template<int i, class T, unsigned int d>
class _specc::piped<T[i],d> :		/* special template for 'piped' arrays */
	public _specc::piped_base	/* is based on piped_base */
{
public:
T		Value[d+1][i];	/* static, FIFO-type storage */


inline piped(void);			/* constructor #1 */

explicit inline piped(			/* constructor #2 (with init) */
	const T		Init[i]);	/* initializer */

inline piped(				/* copy constructor (#3) */
	const _specc::piped<T[i],d> &Orig);

inline ~piped(void);			/* destructor */

//ejj 09/26/05
inline void _LogInitialState(void);


inline operator const T*() const;	/* conversion operator (read access) */


inline _specc::piped<T[i],d> &operator=(	/* assignment operator #1 */
	T		Var[i]);

inline _specc::piped<T[i],d> &operator=(	/* assignment operator #1b */
	const _specc::piped<T[i],d> &PipedVar);

inline _specc::piped<T[i],d> &operator=(	/* assignment operator #1c */
	const _specc::pip_elem<T[i],d> &PipElemVar);


inline _specc::pip_elem<T,d> operator[](/* array access operator */
	int		Index);


void update(void);		/* make the next value the current value */
};


	/*******************************************/
	/*** pip_elem template (general version) ***/
	/*******************************************/


template <class T, unsigned int d>
class _specc::pip_elem		/* template class for 'piped' arrays */
{
public:
T		*ValuePtrs[d+1];/* pointer array to FIFO type storage */


inline pip_elem(		/* constructor #1 */
	void	*ValuePtrs[d+1]);

// default copy constructor is just fine

inline ~pip_elem(void);		/* destructor */


inline operator T() const;	/* conversion operator (read access) */


inline pip_elem<T,d> &operator=(	/* assignment operator #1 */
	T		Var);

inline pip_elem<T,d> &operator=(	/* assignment operator #1b */
	const pip_elem<T,d> &PipElem);

inline pip_elem<T,d> &operator+=(	/* assignment operator #2 */
	T		Var);

inline pip_elem<T,d> &operator-=(	/* assignment operator #3 */
	T		Var);

inline pip_elem<T,d> &operator*=(	/* assignment operator #4 */
	T		Var);

inline pip_elem<T,d> &operator/=(	/* assignment operator #5 */
	T		Var);

inline pip_elem<T,d> &operator%=(	/* assignment operator #6 */
	T		Var);

inline pip_elem<T,d> &operator^=(	/* assignment operator #7 */
	T		Var);

inline pip_elem<T,d> &operator&=(	/* assignment operator #8 */
	T		Var);

inline pip_elem<T,d> &operator|=(	/* assignment operator #9 */
	T		Var);

inline pip_elem<T,d> &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline pip_elem<T,d> &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline pip_elem<T,d> &operator++();	/* increment operator #1 (pre-) */

inline pip_elem<T,d> operator++(	/* increment operator #2 (post-) */
	int);

inline pip_elem<T,d> &operator--();	/* decrement operator #1 (pre-) */

inline pip_elem<T,d> operator--(	/* decrement operator #2 (post-) */
	int);


// special pass-through functions for bitvectors and longlongs

inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

// bitvector slices

inline _specc::pipslice<d> operator()(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::pipslice<d> operator()(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::pipslice<d> operator()(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::pipslice<d> operator()(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// struct/union member access

template <class MT>
inline _specc::pip_elem<MT,d> member(		/* member access "operator" */
	int		CharOffset);

template <class MT>
inline const _specc::pip_elem<MT,d> member(	/* const member access "op." */
	int		CharOffset) const;
};


	/**********************************************************/
	/*** pip_elem template (specialization for array types) ***/
	/**********************************************************/


template<int i, class T, unsigned int d>
class _specc::pip_elem<T[i],d>	/* special template for multi-dim. arrays */
{
public:
T		*ValuePtrs[d+1];/* pointer array to FIFO type storage */


inline pip_elem(		/* constructor #1 */
	void	*ValuePtrs[d+1]);

// default copy constructor is just fine

inline ~pip_elem(void);		/* destructor */


inline operator const T*() const;	/* conversion operator (read access) */


inline pip_elem<T[i],d> &operator=(	/* assignment operator #1 */
	T		Var[i]);

inline pip_elem<T[i],d> &operator=(	/* assignment operator #1b */
	const pip_elem<T[i],d> &PipElem);

inline pip_elem<T[i],d> &operator=(	/* assignment operator #1c */
	const _specc::piped<T[i],d> &PipedVar);


inline pip_elem<T,d> operator[](	/* array access operator */
	int		Index);
};


	/*********************************/
	/*** _specc::pipslice template ***/
	/*********************************/


template<unsigned int d>
class _specc::pipslice		/* template for 'piped' bitvector slices */
{
public:
_bitslice	*Slices[d+1];	/* slice array of FIFO type */


inline pipslice(		/* constructor #1 */
	_bit::chunk	*VecAddr[d+1],
	bool		VecUnsigned,
	_bit::len_t	VecLen,
	_bit::len_t	Left,
	_bit::len_t	Right,
	bool		Unsigned);

inline pipslice(		/* copy constructor */
	const pipslice<d> &Orig);

inline ~pipslice(void);		/* destructor */


inline operator _bit() const;	/* conversion operator (read access) */


inline pipslice<d> &operator=(	/* assignment operator #1 */
	_bit		Var);

inline pipslice<d> &operator=(	/* assignment operator #1b */
	const pipslice<d> &PipSlice);

inline pipslice<d> &operator+=(	/* assignment operator #2 */
	_bit		Var);

inline pipslice<d> &operator-=(	/* assignment operator #3 */
	_bit		Var);

inline pipslice<d> &operator*=(	/* assignment operator #4 */
	_bit		Var);

inline pipslice<d> &operator/=(	/* assignment operator #5 */
	_bit		Var);

inline pipslice<d> &operator%=(	/* assignment operator #6 */
	_bit		Var);

inline pipslice<d> &operator^=(	/* assignment operator #7 */
	_bit		Var);

inline pipslice<d> &operator&=(	/* assignment operator #8 */
	_bit		Var);

inline pipslice<d> &operator|=(	/* assignment operator #9 */
	_bit		Var);

inline pipslice<d> &operator<<=(/* assignment operator #10 */
	unsigned int	Var);

inline pipslice<d> &operator>>=(/* assignment operator #11 */
	unsigned int	Var);


inline pipslice<d> &operator++();	/* increment operator #1 (pre-) */

inline pipslice<d> operator++(		/* increment operator #2 (post-) */
	int);

inline pipslice<d> &operator--();	/* decrement operator #1 (pre-) */

inline pipslice<d> operator--(		/* decrement operator #2 (post-) */
	int);


inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */


inline _specc::pipslice<d> operator()(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::pipslice<d> operator()(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::pipslice<d> operator()(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::pipslice<d> operator()(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;
};


	/*******************************************/
	/*** buffered template (general version) ***/
	/*******************************************/

//PC 05/16/04
template<class T>
class _specc::buffered :	/* template class for 'buffered' types */
	public _specc::buffered_base	/* is based on buffered_base */
{
public:
T		InitValue;	/* initial value (for reset) */
T		CurrValue;	/* current value (for read operations) */
T		NextValue;	/* next value (for write operations) */


explicit inline buffered(	/* constructor #1 */
	_specc::event_ptr *UpdateEvents); /* list of updating events */

inline buffered(		/* constructor #2 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi);

inline buffered(		/* constructor #3 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	const T		Init);		/* initializer */

inline buffered(		/* constructor #4 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi,
	const T		Init);		/* initializer */

explicit inline buffered(	/* constructor #5 (for signals) */
	_specc::event	*SignalEvent);	/* signal updating event */

inline buffered(		/* constructor #6 (for signals) */
	_specc::event	*SignalEvent,	/* signal updating event */
	const T		Init);		/* initializer */

inline buffered(		/* copy constructor (#7) */
	const _specc::buffered<T> &Orig);

inline ~buffered(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState(void); 

inline operator T() const;	/* conversion operator (read access) */


inline _specc::buffered<T> &operator=(	/* assignment operator #1 */
	T		Var);

inline _specc::buffered<T> &operator=(	/* assignment operator #1b */
	const buffered<T> &BufferedVar);

inline _specc::buffered<T> &operator+=(	/* assignment operator #2 */
	T		Var);

inline _specc::buffered<T> &operator-=(	/* assignment operator #3 */
	T		Var);

inline _specc::buffered<T> &operator*=(	/* assignment operator #4 */
	T		Var);

inline _specc::buffered<T> &operator/=(	/* assignment operator #5 */
	T		Var);

inline _specc::buffered<T> &operator%=(	/* assignment operator #6 */
	T		Var);

inline _specc::buffered<T> &operator^=(	/* assignment operator #7 */
	T		Var);

inline _specc::buffered<T> &operator&=(	/* assignment operator #8 */
	T		Var);

inline _specc::buffered<T> &operator|=(	/* assignment operator #9 */
	T		Var);

inline _specc::buffered<T> &operator<<=(/* assignment operator #10 */
	unsigned int	Var);

inline _specc::buffered<T> &operator>>=(/* assignment operator #11 */
	unsigned int	Var);


inline _specc::buffered<T> &operator++();/* increment operator #1 (pre-) */

inline _specc::buffered<T> operator++(	/* increment operator #2 (post-) */
	int);

inline _specc::buffered<T> &operator--();/* decrement operator #1 (pre-) */

inline _specc::buffered<T> operator--(	/* decrement operator #2 (post-) */
	int);


void update(void);		/* make the next value the current value */

void reset(void);		/* make the next value the initial value */


// special pass-through functions for longlong type

inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */


// struct/union member access

template <class MT>
inline _specc::buf_elem<MT> member(		/* member access "operator" */
	int		CharOffset);

template <class MT>
inline const _specc::buf_elem<MT> member(	/* const member access "op." */
	int		CharOffset) const;
};


	/*********************************************************/
	/*** buffered template (specialization for bitvectors) ***/
	/*********************************************************/


template<_bit::len_t l, bool s>
class _specc::buffered<bit<l,s> > :
                           /* special template for 'buffered' bitvectors */
	public _specc::buffered_base	/* is based on buffered_base */
{
public:
bit<l,s>	InitValue;	/* initial value (for reset) */
bit<l,s>	CurrValue;	/* current value (for read operations) */
bit<l,s>	NextValue;	/* next value (for write operations) */


explicit inline buffered(	/* constructor #1 */
	_specc::event_ptr *UpdateEvents); /* list of updating events */

inline buffered(		/* constructor #2 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi);

inline buffered(		/* constructor #3 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	const bit<l,s>	Init);		/* initializer */

inline buffered(		/* constructor #4 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi,
	const bit<l,s>	Init);		/* initializer */

explicit inline buffered(	/* constructor #5 (for signals) */
	_specc::event	*SignalEvent);	/* signal updating event */

inline buffered(		/* constructor #6 (for signals) */
	_specc::event	*SignalEvent,	/* signal updating event */
	const bit<l,s>	Init);		/* initializer */

inline buffered(		/* copy constructor (#7) */
	const _specc::buffered<bit<l,s> > &Orig);

inline ~buffered(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState(void); 

inline operator bit<l,s>() const;	/* conversion operator (read access) */


inline _specc::buffered<bit<l,s> > &operator=(	/* assignment operator #1 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator=(	/* assignment operator #1b */
	const _specc::buffered<bit<l,s> > &BufferedVar);

inline _specc::buffered<bit<l,s> > &operator+=(	/* assignment operator #2 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator-=(	/* assignment operator #3 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator*=(	/* assignment operator #4 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator/=(	/* assignment operator #5 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator%=(	/* assignment operator #6 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator^=(	/* assignment operator #7 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator&=(	/* assignment operator #8 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator|=(	/* assignment operator #9 */
	bit<l,s>	Var);

inline _specc::buffered<bit<l,s> > &operator<<=(/* assignment operator #10 */
	unsigned int	Var);

inline _specc::buffered<bit<l,s> > &operator>>=(/* assignment operator #11 */
	unsigned int	Var);


inline _specc::buffered<bit<l,s> > &operator++();
                                    /* increment operator #1 (pre-) */

inline _specc::buffered<bit<l,s> > operator++(	
                      /* increment operator #2 (post-) */
	int);

inline _specc::buffered<bit<l,s> > &operator--();
                      /* decrement operator #1 (pre-) */

inline _specc::buffered<bit<l,s> > operator--(	
                     /* decrement operator #2 (post-) */
	int);


void update(void);		/* make the next value the current value */

void reset(void);		/* make the next value the initial value */


// special pass-through functions for bitvectors

inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

// bitvector slices

virtual inline _specc::bufslice buf_slice(	/* single bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::bufslice buf_slice(	/* slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::bufslice buf_slice( /* const bit of a bitvec. */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::bufslice buf_slice( /* const slice of a bitvec.*/
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::bufbus_elem* bufbus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::bufbus_elem* bufbus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;		/* store value for bufbus */
						/* (dummy to be overloaded) */

virtual inline void Get(void) const;		/* load value for bufbus */
						/* (dummy to be overloaded) */
};

//PC: 11/09/05 Changes for bit4 resolved
	/*********************************************************/
	/*** buffered template (specialization for bit4vectors) ***/
	/*********************************************************/


template<_bit::len_t l, bool s>
class _specc::buffered<bit4<l,s> > :
                           /* special template for 'buffered' bit4vectors */
	public _specc::buffered_base	/* is based on buffered_base */
{
public:
bit4<l,s>	InitValue;	/* initial value (for reset) */
bit4<l,s>	CurrValue;	/* current value (for read operations) */
bit4<l,s>	NextValue;	/* next value (for write operations) */


explicit inline buffered(	/* constructor #1 */
	_specc::event_ptr *UpdateEvents); /* list of updating events */

inline buffered(		/* constructor #2 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi);

inline buffered(		/* constructor #3 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	const bit4<l,s>	Init);		/* initializer */

inline buffered(		/* constructor #4 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi,
	const bit4<l,s>	Init);		/* initializer */

explicit inline buffered(	/* constructor #5 (for signals) */
	_specc::event	*SignalEvent);	/* signal updating event */

inline buffered(		/* constructor #6 (for signals) */
	_specc::event	*SignalEvent,	/* signal updating event */
	const bit4<l,s>	Init);		/* initializer */

inline buffered(		/* copy constructor (#7) */
	const _specc::buffered<bit4<l,s> > &Orig);

inline ~buffered(void);		/* destructor */

// EJ 02/01/2006 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState(void); 

inline operator bit4<l,s>() const;	/* conversion operator (read access) */


inline _specc::buffered<bit4<l,s> > &operator=(	/* assignment operator #1 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator=(	/* assignment operator #1b */
	const _specc::buffered<bit4<l,s> > &BufferedVar);

inline _specc::buffered<bit4<l,s> > &operator+=(	/* assignment operator #2 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator-=(	/* assignment operator #3 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator*=(	/* assignment operator #4 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator/=(	/* assignment operator #5 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator%=(	/* assignment operator #6 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator^=(	/* assignment operator #7 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator&=(	/* assignment operator #8 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator|=(	/* assignment operator #9 */
	bit4<l,s>	Var);

inline _specc::buffered<bit4<l,s> > &operator<<=(/* assignment operator #10 */
	unsigned int	Var);

inline _specc::buffered<bit4<l,s> > &operator>>=(/* assignment operator #11 */
	unsigned int	Var);


inline _specc::buffered<bit4<l,s> > &operator++();
                                    /* increment operator #1 (pre-) */

inline _specc::buffered<bit4<l,s> > operator++(	
                      /* increment operator #2 (post-) */
	int);

inline _specc::buffered<bit4<l,s> > &operator--();
                      /* decrement operator #1 (pre-) */

inline _specc::buffered<bit4<l,s> > operator--(	
                     /* decrement operator #2 (post-) */
	int);


void update(void);		/* make the next value the current value */

void reset(void);		/* make the next value the initial value */


// special pass-through functions for bit4vectors

inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

//PC:04/05/06
//Fix to bug reported by gunar
inline _bit toBit(void)const;			/* read access to bit */

// bit4vector slices

virtual inline _specc::buf4slice buf4_slice(	/* single bit4 of a bit4vector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::buf4slice buf4_slice(	/* slice of a bit4vector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::buf4slice buf4_slice( /* const bit of a bit4vec. */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::buf4slice buf4_slice( /* const slice of a bit4vec.*/
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::buf4bus_elem* buf4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::buf4bus_elem* buf4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;		/* store value for bufbus */
						/* (dummy to be overloaded) */

virtual inline void Get(void) const;		/* load value for bufbus */
						/* (dummy to be overloaded) */
};


//PC: 11/14/05 Changes for bit4 
	/*********************************************************/
	/*** buffered template (specialization for resolved bit4vectors) ***/
	/*********************************************************/


template<_bit::len_t l, bool s>
class _specc::buffered<rbit4<l,s> > :
                           /* special template for 'buffered' bit4vectors */
	public _specc::buffered_base	/* is based on buffered_base */
{
public:
rbit4<l,s>	InitValue;	/* initial value (for reset) */
rbit4<l,s>	CurrValue;	/* current value (for read operations) */
rbit4<l,s>	NextValue;	/* next value (for write operations) */


explicit inline buffered(	/* constructor #1 */
	_specc::event_ptr *UpdateEvents); /* list of updating events */

inline buffered(		/* constructor #2 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi);

inline buffered(		/* constructor #3 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	const bit4<l,s>	Init);		/* initializer */

//PC: 11/14/05 Changes for bit4 
inline buffered(                /* constructor #3a */
        _specc::event_ptr *UpdateEvents,/* list of updating events */
        const rbit4<l,s> Init);          /* initializer */

inline buffered(		/* constructor #4 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi,
	const bit4<l,s>	Init);		/* initializer */

//PC: 11/14/05 Changes for bit4 
inline buffered(                /* constructor #4a */
        _specc::event_ptr *UpdateEvents,/* list of updating events */
        _specc::event   *ResetSignal,   /* async. reset specifier */
        bool            ResetActiveHi,
        const rbit4<l,s> Init);          /* initializer */

explicit inline buffered(	/* constructor #5 (for signals) */
	_specc::event	*SignalEvent);	/* signal updating event */

inline buffered(		/* constructor #6 (for signals) */
	_specc::event	*SignalEvent,	/* signal updating event */
	const bit4<l,s>	Init);		/* initializer */

//PC: 11/14/05 Changes for bit4 
inline buffered(                /* constructor #6a (for signals) */
        _specc::event   *SignalEvent,   /* signal updating event */
        const rbit4<l,s> Init);          /* initializer */


inline buffered(		/* copy constructor (#7) */
	const _specc::buffered<rbit4<l,s> > &Orig);

//PC: 11/14/05 Changes for bit4 
inline buffered(                /* constructor (#7a) */
        const _specc::buffered<bit4<l,s> > &Orig);


inline ~buffered(void);		/* destructor */

// EJ 02/01/2006 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState(void); 

inline operator bit4<l,s>() const;	/* conversion operator (read access) */

inline operator rbit4<l,s>() const;	/* conversion operator (read access) */


inline _specc::buffered<rbit4<l,s> > &operator=(	/* assignment operator #1 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator=(	/* assignment operator #1b */
	const _specc::buffered<bit4<l,s> > &BufferedVar);

inline _specc::buffered<rbit4<l,s> > &operator+=(	/* assignment operator #2 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator-=(	/* assignment operator #3 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator*=(	/* assignment operator #4 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator/=(	/* assignment operator #5 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator%=(	/* assignment operator #6 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator^=(	/* assignment operator #7 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator&=(	/* assignment operator #8 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator|=(	/* assignment operator #9 */
	bit4<l,s>	Var);

inline _specc::buffered<rbit4<l,s> > &operator<<=(/* assignment operator #10 */
	unsigned int	Var);

inline _specc::buffered<rbit4<l,s> > &operator>>=(/* assignment operator #11 */
	unsigned int	Var);


inline _specc::buffered<rbit4<l,s> > &operator++();
                                    /* increment operator #1 (pre-) */

inline _specc::buffered<rbit4<l,s> > operator++(	
                      /* increment operator #2 (post-) */
	int);

inline _specc::buffered<rbit4<l,s> > &operator--();
                      /* decrement operator #1 (pre-) */

inline _specc::buffered<rbit4<l,s> > operator--(	
                     /* decrement operator #2 (post-) */
	int);


void update(void);		/* make the next value the current value */

void reset(void);		/* make the next value the initial value */


// special pass-through functions for bit4vectors

inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

//PC:04/05/06
//Fix to bug reported by gunar
inline _bit toBit(void)const;			/* read access to bit */

// bit4vector slices

virtual inline _specc::rbuf4slice rbuf4_slice(	/* single bit4 of a bit4vector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::rbuf4slice rbuf4_slice(	/* slice of a bit4vector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::rbuf4slice rbuf4_slice( /* const bit of a bit4vec. */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::rbuf4slice rbuf4_slice( /* const slice of a bit4vec.*/
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::buf4bus_elem* buf4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::buf4bus_elem* buf4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;		/* store value for bufbus */
						/* (dummy to be overloaded) */

virtual inline void Get(void) const;		/* load value for bufbus */
						/* (dummy to be overloaded) */
};


//PC: 11/14/05 Changes for bit4 
	/**********************************************************/
	/*** buffered template (specialization for array types) ***/
	/**********************************************************/


template<int i, class T>
class _specc::buffered<T[i]> :	/* special template for 'buffered' arrays */
	public _specc::buffered_base	/* is based on buffered_base */
{
public:
T		InitValue[i];	/* initial value (for reset) */
T		CurrValue[i];	/* current value (for read operations) */
T		NextValue[i];	/* next value (for write operations) */


explicit inline buffered(	/* constructor #1 */
	_specc::event_ptr *UpdateEvents); /* list of updating events */

inline buffered(		/* constructor #2 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi);

inline buffered(		/* constructor #3 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	T		Init[i]);	/* initializer */

inline buffered(		/* constructor #4 */
	_specc::event_ptr *UpdateEvents,/* list of updating events */
	_specc::event	*ResetSignal,	/* async. reset specifier */
	bool		ResetActiveHi,
	T		Init[i]);	/* initializer */

explicit inline buffered(	/* constructor #5 (for signals) */
	_specc::event	*SignalEvent);	/* signal updating event */

inline buffered(		/* constructor #6 (for signals) */
	_specc::event	*SignalEvent,	/* signal updating event */
	T		Init[i]);	/* initializer */

inline buffered(		/* copy constructor (#7) */
	const _specc::buffered<T[i]> &Orig);

inline ~buffered(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState(void); 


inline operator const T*() const;	/* conversion operator (read access) */


inline _specc::buffered<T[i]> &operator=(	/* assignment operator #1 */
	T		Var[i]);

inline _specc::buffered<T[i]> &operator=(	/* assignment operator #1b */
	const _specc::buffered<T[i]> &BufferedVar);

inline _specc::buffered<T[i]> &operator=(	/* assignment operator #1c */
	const _specc::buf_elem<T[i]> &BufElemVar);


inline _specc::buf_elem<T> operator[](	/* array access operator */
	int		Index);


void update(void);		/* make the next value the current value */

void reset(void);		/* make the next value the initial value */
};


	/*******************************************/
	/*** buf_elem template (general version) ***/
	/*******************************************/


template <class T>
class _specc::buf_elem		/* template class for 'buffered' arrays */
{
public:
T		*CurrPtr;	/* pointer to current value (for read ops.) */
T		*NextPtr;	/* pointer to next value (for write ops.) */


inline buf_elem(		/* constructor #1 */
	void	*CurrPtr,
	void	*NextPtr);

// default copy constructor is just fine

inline ~buf_elem(void);		/* destructor */


inline operator T() const;	/* conversion operator (read access) */


inline buf_elem<T> &operator=(	/* assignment operator #1 */
	T		Var);

inline buf_elem<T> &operator=(	/* assignment operator #1b */
	const buf_elem<T> &BufElem);

inline buf_elem<T> &operator+=(	/* assignment operator #2 */
	T		Var);

inline buf_elem<T> &operator-=(	/* assignment operator #3 */
	T		Var);

inline buf_elem<T> &operator*=(	/* assignment operator #4 */
	T		Var);

inline buf_elem<T> &operator/=(	/* assignment operator #5 */
	T		Var);

inline buf_elem<T> &operator%=(	/* assignment operator #6 */
	T		Var);

inline buf_elem<T> &operator^=(	/* assignment operator #7 */
	T		Var);

inline buf_elem<T> &operator&=(	/* assignment operator #8 */
	T		Var);

inline buf_elem<T> &operator|=(	/* assignment operator #9 */
	T		Var);

inline buf_elem<T> &operator<<=(/* assignment operator #10 */
	unsigned int	Var);

inline buf_elem<T> &operator>>=(/* assignment operator #11 */
	unsigned int	Var);


inline buf_elem<T> &operator++();/* increment operator #1 (pre-) */

inline buf_elem<T> operator++(	/* increment operator #2 (post-) */
	int);

inline buf_elem<T> &operator--();/* decrement operator #1 (pre-) */

inline buf_elem<T> operator--(	/* decrement operator #2 (post-) */
	int);


// special pass-through functions for bitvectors and longlongs

inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

// bitvector slices

inline _specc::bufslice buf_slice(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::bufslice buf_slice(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::bufslice buf_slice(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::bufslice buf_slice(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

//PC: 11/10/05 Changes for bit4 resolved
inline _specc::buf4slice buf4_slice(              /* single bit of a bitvector */
        _bit::bnd_t     l,
        _bit::bnd_t     r,
        _bit::bnd_t     i);

inline _specc::buf4slice buf4_slice(              /* slice of a bitvector */
        _bit::bnd_t     l,
        _bit::bnd_t     r,
        _bit::bnd_t     sl,
        _bit::bnd_t     sr);

inline const _specc::buf4slice buf4_slice(        /* const bit of a bitvector */
        _bit::bnd_t     l,
        _bit::bnd_t     r,
        _bit::bnd_t     i) const;

inline const _specc::buf4slice buf4_slice(        /* const slice of a bitvector */
        _bit::bnd_t     l,
        _bit::bnd_t     r,
        _bit::bnd_t     sl,
        _bit::bnd_t     sr) const;


// struct/union member access

template <class MT>
inline _specc::buf_elem<MT> member(		/* member access "operator" */
	int		CharOffset);

template <class MT>
inline const _specc::buf_elem<MT> member(	/* const member access "op." */
	int		CharOffset) const;
};


	/**********************************************************/
	/*** buf_elem template (specialization for array types) ***/
	/**********************************************************/


template<int i, class T>
class _specc::buf_elem<T[i]>	/* special template for multi-dim. arrays */
{
public:
T		*CurrPtr;	/* pointer to current value (for read ops.) */
T		*NextPtr;	/* pointer to next value (for write ops.) */


inline buf_elem(		/* constructor #1 */
	void	*CurrPtr,
	void	*NextPtr);

// default copy constructor is just fine

inline ~buf_elem(void);		/* destructor */


inline operator const T*() const;	/* conversion operator (read access) */


inline buf_elem<T[i]> &operator=(	/* assignment operator #1 */
	T		Var[i]);

inline buf_elem<T[i]> &operator=(	/* assignment operator #1b */
	const buf_elem<T[i]> &BufElem);

inline buf_elem<T[i]> &operator=(	/* assignment operator #1c */
	const _specc::buffered<T[i]> &BufferedVar);


inline buf_elem<T> operator[](		/* array access operator */
	int		Index);
};


	/************************/
	/*** _specc::bufslice ***/
	/************************/


class _specc::bufslice		/* class for 'buffered' bitvector slices */
{
public:
_bitslice	CurrSlice;	/* handle for current slice (read ops.) */
_bitslice	NextSlice;	/* handle for next slice (write ops.) */
bufbus_elem	*BusSlices;	/* list of bus slices (or NULL) */


inline bufslice(		/* constructor #1 */
	const _bit	&CurrVec,
	const _bit	&NextVec,
	_bit::len_t	Left,
	_bit::len_t	Right,
	bool		Unsigned,
	bufbus_elem	*Slices = NULL); /* only for slice of bufbus */

inline bufslice(		/* copy constructor */
	const bufslice	&Orig);

inline ~bufslice(void);		/* destructor */


inline operator _bit() const;	/* conversion operator (read access) */


inline bufslice &operator=(	/* assignment operator #1 */
	_bit		Var);

inline bufslice &operator=(	/* assignment operator #1b */
	const bufslice &BufSlice);

inline bufslice &operator+=(	/* assignment operator #2 */
	_bit		Var);

inline bufslice &operator-=(	/* assignment operator #3 */
	_bit		Var);

inline bufslice &operator*=(	/* assignment operator #4 */
	_bit		Var);

inline bufslice &operator/=(	/* assignment operator #5 */
	_bit		Var);

inline bufslice &operator%=(	/* assignment operator #6 */
	_bit		Var);

inline bufslice &operator^=(	/* assignment operator #7 */
	_bit		Var);

inline bufslice &operator&=(	/* assignment operator #8 */
	_bit		Var);

inline bufslice &operator|=(	/* assignment operator #9 */
	_bit		Var);

inline bufslice &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline bufslice &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline bufslice &operator++();	/* increment operator #1 (pre-) */

inline bufslice operator++(	/* increment operator #2 (post-) */
	int);

inline bufslice &operator--();	/* decrement operator #1 (pre-) */

inline bufslice operator--(	/* decrement operator #2 (post-) */
	int);


inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

inline _specc::bufslice buf_slice(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::bufslice buf_slice(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::bufslice buf_slice(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::bufslice buf_slice(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;


inline void Put(void) const;			/* store value for bufbus */

inline void Get(void) const;			/* load value for bufbus */
};


//PC: 11/10/05 Changes for bit4 resolved
	/************************/
	/*** _specc::buf4slice ***/
	/************************/


class _specc::buf4slice		/* class for 'buffered' bitvector slices */
{
public:
_bit4slice	CurrSlice;	/* handle for current slice (read ops.) */
_bit4slice	NextSlice;	/* handle for next slice (write ops.) */
buf4bus_elem	*BusSlices;	/* list of bus slices (or NULL) */

inline buf4slice(		/* constructor #1 */
	const _bit4	&CurrVec,
	const _bit4	&NextVec,
	_bit::len_t	Left,
	_bit::len_t	Right,
	bool		Unsigned,
	buf4bus_elem	*Slices = NULL); /* only for slice of bufbus */

inline buf4slice(		/* copy constructor */
	const buf4slice	&Orig);

inline ~buf4slice(void);		/* destructor */


inline operator _bit4() const;	/* conversion operator (read access) */


inline buf4slice &operator=(	/* assignment operator #1 */
	_bit4		Var);

inline buf4slice &operator=(	/* assignment operator #1b */
	const buf4slice &BufSlice);

inline buf4slice &operator+=(	/* assignment operator #2 */
	_bit4		Var);

inline buf4slice &operator-=(	/* assignment operator #3 */
	_bit4		Var);

inline buf4slice &operator*=(	/* assignment operator #4 */
	_bit4		Var);

inline buf4slice &operator/=(	/* assignment operator #5 */
	_bit4		Var);

inline buf4slice &operator%=(	/* assignment operator #6 */
	_bit4		Var);

inline buf4slice &operator^=(	/* assignment operator #7 */
	_bit4		Var);

inline buf4slice &operator&=(	/* assignment operator #8 */
	_bit4		Var);

inline buf4slice &operator|=(	/* assignment operator #9 */
	_bit4		Var);

inline buf4slice &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline buf4slice &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline buf4slice &operator++();	/* increment operator #1 (pre-) */

inline buf4slice operator++(	/* increment operator #2 (post-) */
	int);

inline buf4slice &operator--();	/* decrement operator #1 (pre-) */

inline buf4slice operator--(	/* decrement operator #2 (post-) */
	int);


inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

//PC:04/05/06
//Fix to bug reported by gunar
inline _bit toBit(void)const;			/* read access to bit */


inline _specc::buf4slice buf4_slice(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::buf4slice buf4_slice(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::buf4slice buf4_slice(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::buf4slice buf4_slice(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;


inline void Put(void) const;			/* store value for buf4bus */

inline void Get(void) const;			/* load value for buf4bus */
};

//PC: 11/14/05 Changes for resolved bit4 
	/************************/
	/*** _specc::rbuf4slice ***/
	/************************/


class _specc::rbuf4slice		/* class for 'buffered' bitvector slices */
{
public:
_rbit4slice	CurrSlice;	/* handle for current slice (read ops.) */
_rbit4slice	NextSlice;	/* handle for next slice (write ops.) */
buf4bus_elem	*BusSlices;	/* list of bus slices (or NULL) */

inline rbuf4slice(		/* constructor #1 */
	const _bit4	&CurrVec,
	const _bit4	&NextVec,
	_bit::len_t	Left,
	_bit::len_t	Right,
	bool		Unsigned,
	buf4bus_elem	*Slices = NULL); /* only for slice of bufbus */

inline rbuf4slice(		/* copy constructor */
	const rbuf4slice	&Orig);

inline ~rbuf4slice(void);		/* destructor */


inline operator _rbit4() const;	/* conversion operator (read access) */


inline rbuf4slice &operator=(	/* assignment operator #1 */
	_bit4		Var);

inline rbuf4slice &operator=(	/* assignment operator #1b */
	const buf4slice &BufSlice);

inline rbuf4slice &operator+=(	/* assignment operator #2 */
	_bit4		Var);

inline rbuf4slice &operator-=(	/* assignment operator #3 */
	_bit4		Var);

inline rbuf4slice &operator*=(	/* assignment operator #4 */
	_bit4		Var);

inline rbuf4slice &operator/=(	/* assignment operator #5 */
	_bit4		Var);

inline rbuf4slice &operator%=(	/* assignment operator #6 */
	_bit4		Var);

inline rbuf4slice &operator^=(	/* assignment operator #7 */
	_bit4		Var);

inline rbuf4slice &operator&=(	/* assignment operator #8 */
	_bit4		Var);

inline rbuf4slice &operator|=(	/* assignment operator #9 */
	_bit4		Var);

inline rbuf4slice &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline rbuf4slice &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline rbuf4slice &operator++();	/* increment operator #1 (pre-) */

inline rbuf4slice operator++(	/* increment operator #2 (post-) */
	int);

inline rbuf4slice &operator--();	/* decrement operator #1 (pre-) */

inline rbuf4slice operator--(	/* decrement operator #2 (post-) */
	int);


inline bool test(void) const;			/* read access to bool */

inline int toInt(void) const;			/* read access to int */

inline unsigned int toUInt(void) const;		/* read access to u. int */

inline long toLong(void) const;			/* read access to long */

inline unsigned long toULong(void) const;	/* read access to u. long */

inline _LONGLONG toLLong(void) const;		/* read access to long long */

inline _ULONGLONG toULLong(void) const;		/* read access to u.long long */

inline double toDouble(void) const;		/* read access to double */

inline long double toLDouble(void) const;	/* read access to long double */

//PC:04/05/06
//Fix to bug reported by gunar
inline _bit toBit(void)const;			/* read access to bit */


inline _specc::rbuf4slice rbuf4_slice(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::rbuf4slice rbuf4_slice(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::rbuf4slice rbuf4_slice(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::rbuf4slice rbuf4_slice(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;


inline void Put(void) const;			/* store value for buf4bus */

inline void Get(void) const;			/* load value for buf4bus */
};


	/**********************/
	/*** _specc::bufbus ***/
	/**********************/


template <_bit::len_t len, bool usign>
class _specc::bufbus :		/* class for sliced 'buffered' port mapping */
	public _specc::buffered<bit<len,usign> > 
                                   /* is based on 'buffered' bitvectors */
{
public:
bufbus_elem	*Slices;	/* list of bus slices */


inline bufbus(			/* constructor #1 */
	bufbus_elem	*Slices);

inline ~bufbus(void);		/* destructor */


// bitvector slices

virtual inline _specc::bufslice buf_slice(	/* single bit of a bufbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::bufslice buf_slice(	/* slice of a bufbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::bufslice buf_slice( /* const bit of a bufbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::bufslice buf_slice( /* const slice of a bufbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::bufbus_elem* bufbus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::bufbus_elem* bufbus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;		/* store value for bufbus */
						/* (override dummy method) */

virtual inline void Get(void) const;		/* load value for bufbus */
						/* (override dummy method) */
};


//PC: 11/10/05 Changes for bit4 resolved
	/**********************/
	/*** _specc::buf4bus ***/
	/**********************/


template <_bit::len_t len, bool usign>
class _specc::buf4bus :		/* class for sliced 'buffered' port mapping */
	public _specc::buffered<bit4<len,usign> > 
                                   /* is based on 'buffered' bit4vectors */
{
public:
buf4bus_elem	*Slices;	/* list of bus slices */


inline buf4bus(			/* constructor #1 */
	buf4bus_elem	*Slices);

inline ~buf4bus(void);		/* destructor */


// bit4vector slices

virtual inline _specc::buf4slice buf4_slice(	/* single bit of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::buf4slice buf4_slice(	/* slice of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::buf4slice buf4_slice( /* const bit of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::buf4slice buf4_slice( /* const slice of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::buf4bus_elem* buf4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::buf4bus_elem* buf4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;		/* store value for buf4bus */
						/* (override dummy method) */

virtual inline void Get(void) const;		/* load value for buf4bus */
						/* (override dummy method) */
};


//PC: 11/14/05 Changes for resolved bit4
	/**********************/
	/*** _specc::rbuf4bus ***/
	/**********************/


template <_bit::len_t len, bool usign>
class _specc::rbuf4bus :		/* class for sliced 'buffered' port mapping */
	public _specc::buffered<rbit4<len,usign> > 
                                   /* is based on 'buffered' bit4vectors */
{
public:
buf4bus_elem	*Slices;	/* list of bus slices */


inline rbuf4bus(			/* constructor #1 */
	buf4bus_elem	*Slices);

inline ~rbuf4bus(void);		/* destructor */


// bit4vector slices

virtual inline _specc::rbuf4slice rbuf4_slice(	/* single bit of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::rbuf4slice rbuf4_slice(	/* slice of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::rbuf4slice rbuf4_slice( /* const bit of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::rbuf4slice rbuf4_slice( /* const slice of a buf4bus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::buf4bus_elem* buf4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::buf4bus_elem* buf4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;		/* store value for buf4bus */
						/* (override dummy method) */

virtual inline void Get(void) const;		/* load value for buf4bus */
						/* (override dummy method) */
};


	/***************************/
	/*** _specc::bufbus_elem ***/
	/***************************/


class _specc::bufbus_elem	/* class for sliced 'buffered' port map */
{
public:
bufbus_elem	*Next;		/* next slice (or NULL) */
__bitslice	CurrSlice;	/* reference to this slice with current value */
__bitslice	NextSlice;	/* reference to this slice with future value */


inline bufbus_elem(		/* constructor #1 */
	const _bit	&CurrVal,
	const _bit	&NextVal,
	_bit::len_t	sl,
	_bit::len_t	sr,
	bufbus_elem	*Next = NULL);

inline ~bufbus_elem(void);	/* destructor */


inline bufbus_elem *concat(	/* concatenation */
	bufbus_elem	*List);

inline bufbus_elem *slice(	/* slicing */
	_bit::len_t	l,
	_bit::len_t	r);

inline void Put(		/* synchronization with original */
	const _bit	&Buf);	/* (store tmp. value into next value) */

inline void Get(		/* synchronization with original */
	const _bit	&Buf);	/* (load tmp. value from curr. value) */

inline void GetNextValue(	/* synchronization with original */
	const _bit	&Buf);	/* (load tmp. value from next value) */
};

//PC: 11/10/05 Changes for bit4 resolved
	/***************************/
	/*** _specc::buf4bus_elem ***/
	/***************************/


class _specc::buf4bus_elem	/* class for sliced 'buffered' port map */
{
public:
buf4bus_elem	*Next;		/* next slice (or NULL) */
__bit4slice	CurrSlice;	/* reference to this slice with current value */
__bit4slice	NextSlice;	/* reference to this slice with future value */


inline buf4bus_elem(		/* constructor #1 */
	const _bit4	&CurrVal,
	const _bit4	&NextVal,
	_bit::len_t	sl,
	_bit::len_t	sr,
	buf4bus_elem	*Next = NULL);

inline ~buf4bus_elem(void);	/* destructor */


inline buf4bus_elem *concat(	/* concatenation */
	buf4bus_elem	*List);

inline buf4bus_elem *slice(	/* slicing */
	_bit::len_t	l,
	_bit::len_t	r);

inline void Put(		/* synchronization with original */
	const _bit4	&Buf);	/* (store tmp. value into next value) */

inline void Get(		/* synchronization with original */
	const _bit4	&Buf);	/* (load tmp. value from curr. value) */

inline void GetNextValue(	/* synchronization with original */
	const _bit4	&Buf);	/* (load tmp. value from next value) */
};


	/*****************************************/
	/*** signal template (general version) ***/
	/*****************************************/


template<class T>
class _specc::signal :			/* template class for 'signal' types */
	public _specc::buffered<T>,	/* is based on 'buffered' template */
	public _specc::event		/* and _is_ an event */
{
public:

inline signal(void);		/* constructor #1 */

explicit inline signal(		/* constructor #2 */
	const T		Init);		/* initializer */

inline signal(			/* copy constructor (#3) */
	const signal<T> &Orig);

inline ~signal(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
inline virtual void _LogInitialState(void);

// EJ 10/10/05 -- log an event if tracing is active
// the signal class should override the event method
inline virtual void LogEvent(void);


inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides dummy in event class) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides dummy in event class) */


inline signal<T> &operator=(	/* assignment operator #1 */
	T		Var);

inline signal<T> &operator=(	/* assignment operator #1b */
	const signal<T> &SignalVar);

inline signal<T> &operator+=(	/* assignment operator #2 */
	T		Var);

inline signal<T> &operator-=(	/* assignment operator #3 */
	T		Var);

inline signal<T> &operator*=(	/* assignment operator #4 */
	T		Var);

inline signal<T> &operator/=(	/* assignment operator #5 */
	T		Var);

inline signal<T> &operator%=(	/* assignment operator #6 */
	T		Var);

inline signal<T> &operator^=(	/* assignment operator #7 */
	T		Var);

inline signal<T> &operator&=(	/* assignment operator #8 */
	T		Var);

inline signal<T> &operator|=(	/* assignment operator #9 */
	T		Var);

inline signal<T> &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline signal<T> &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline signal<T> &operator++();	/* increment operator #1 (pre-) */

inline signal<T> operator++(	/* increment operator #2 (post-) */
	int);

inline signal<T> &operator--();	/* decrement operator #1 (pre-) */

inline signal<T> operator--(	/* decrement operator #2 (post-) */
	int);

// struct/union member access

template <class MT>
inline _specc::sig_elem<MT> member(		/* member access "operator" */
	int		CharOffset);

template <class MT>
inline const _specc::sig_elem<MT> member(	/* const member access "op." */
	int		CharOffset) const;
};


	/*******************************************************/
	/*** signal template (specialization for bitvectors) ***/
	/*******************************************************/


template<_bit::len_t l, bool s>
class _specc::signal<bit<l,s> > :/* special template for 'signal' bitvectors */
	public _specc::buffered<bit<l,s> >,/* is based on 'buffered' template */
	public _specc::event		/* and _is_ an event */
{
public:

inline signal(void);		/* constructor #1 */

explicit inline signal(		/* constructor #2 */
	const bit<l,s>	Init);		/* initializer */

inline signal(			/* copy constructor (#3) */
	const signal<bit<l,s> > &Orig);

inline ~signal(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
inline virtual void _LogInitialState(void);

// EJ 10/10/05 -- log an event if tracing is active
// the signal class should override the event method
inline virtual void LogEvent(void);

inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides dummy in event class) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides dummy in event class) */


inline signal<bit<l,s> > &operator=(	/* assignment operator #1 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator=(	/* assignment operator #1b */
	const signal<bit<l,s> > &SignalVar);

inline signal<bit<l,s> > &operator+=(	/* assignment operator #2 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator-=(	/* assignment operator #3 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator*=(	/* assignment operator #4 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator/=(	/* assignment operator #5 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator%=(	/* assignment operator #6 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator^=(	/* assignment operator #7 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator&=(	/* assignment operator #8 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator|=(	/* assignment operator #9 */
	bit<l,s>	Var);

inline signal<bit<l,s> > &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline signal<bit<l,s> > &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline signal<bit<l,s> > &operator++();	/* increment operator #1 (pre-) */

inline signal<bit<l,s> > operator++(	/* increment operator #2 (post-) */
	int);

inline signal<bit<l,s> > &operator--();	/* decrement operator #1 (pre-) */

inline signal<bit<l,s> > operator--(	/* decrement operator #2 (post-) */
	int);


// bitvector slices

virtual inline _specc::sigslice sig_slice(	/* single bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::sigslice sig_slice(	/* slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::sigslice sig_slice(/* const bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::sigslice sig_slice(/* const slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::sigbus_elem* sigbus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::sigbus_elem* sigbus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);
};


//PC: 11/09/05 Changes for bit4 resolved
	/********************************************************/
	/*** signal template (specialization for bit4vectors) ***/
	/********************************************************/


template<_bit::len_t l, bool s>
class _specc::signal<bit4<l,s> > :/* special template for 'signal' bitvectors */
	public _specc::buffered<bit4<l,s> >,/* is based on 'buffered' template */
	public _specc::event		/* and _is_ an event */
{
public:

inline signal(void);		/* constructor #1 */

explicit inline signal(		/* constructor #2 */
	const bit4<l,s>	Init);		/* initializer */

inline signal(			/* copy constructor (#3) */
	const signal<bit4<l,s> > &Orig);

inline ~signal(void);		/* destructor */

// EJ 02/01/06 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
inline virtual void _LogInitialState(void);

// EJ 02/01/06 -- log an event if tracing is active
// the signal class should override the event method
inline virtual void LogEvent(void);


inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides dummy in event class) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides dummy in event class) */


inline signal<bit4<l,s> > &operator=(	/* assignment operator #1 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator=(	/* assignment operator #1b */
	const signal<bit4<l,s> > &SignalVar);

inline signal<bit4<l,s> > &operator+=(	/* assignment operator #2 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator-=(	/* assignment operator #3 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator*=(	/* assignment operator #4 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator/=(	/* assignment operator #5 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator%=(	/* assignment operator #6 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator^=(	/* assignment operator #7 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator&=(	/* assignment operator #8 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator|=(	/* assignment operator #9 */
	bit4<l,s>	Var);

inline signal<bit4<l,s> > &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline signal<bit4<l,s> > &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline signal<bit4<l,s> > &operator++();	/* increment operator #1 (pre-) */

inline signal<bit4<l,s> > operator++(	/* increment operator #2 (post-) */
	int);

inline signal<bit4<l,s> > &operator--();	/* decrement operator #1 (pre-) */

inline signal<bit4<l,s> > operator--(	/* decrement operator #2 (post-) */
	int);


// bitvector slices

virtual inline _specc::sig4slice sig4_slice(	/* single bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::sig4slice sig4_slice(	/* slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::sig4slice sig4_slice(/* const bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::sig4slice sig4_slice(/* const slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::sig4bus_elem* sig4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::sig4bus_elem* sig4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);
};


//PC: 11/14/05 Changes for resolved bit4 
	/*****************************************************************/
	/*** signal template (specialization for resolved bit4vectors) ***/
	/*****************************************************************/


template<_bit::len_t l, bool s>
class _specc::signal<rbit4<l,s> > :/* special template for 'signal' bitvectors */
	public _specc::buffered<rbit4<l,s> >,/* is based on 'buffered' template */
	public _specc::event		/* and _is_ an event */
{
public:


inline signal(void);		/* constructor #1 */

explicit inline signal(		/* constructor #2 */
	const rbit4<l,s>	Init);		/* initializer */

explicit inline signal(		/* constructor #2a */
	const bit4<l,s>	Init);		/* initializer */

inline signal(			/* copy constructor (#3) */
	const signal<rbit4<l,s> > &Orig);

inline signal(			/* constructor (#3a) */
	const signal<bit4<l,s> > &Orig);

inline ~signal(void);		/* destructor */

// EJ 02/01/06 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
inline virtual void _LogInitialState(void);

// EJ 02/01/06 -- log an event if tracing is active
// the signal class should override the event method
inline virtual void LogEvent(void);

inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides dummy in event class) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides dummy in event class) */


inline signal<rbit4<l,s> > &operator=(	/* assignment operator #1 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator=(	/* assignment operator #1b */
	const signal<rbit4<l,s> > &SignalVar);

inline signal<rbit4<l,s> > &operator=(	/* assignment operator #1c */
	const signal<bit4<l,s> > &SignalVar);

inline signal<rbit4<l,s> > &operator+=(	/* assignment operator #2 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator-=(	/* assignment operator #3 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator*=(	/* assignment operator #4 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator/=(	/* assignment operator #5 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator%=(	/* assignment operator #6 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator^=(	/* assignment operator #7 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator&=(	/* assignment operator #8 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator|=(	/* assignment operator #9 */
	bit4<l,s>	Var);

inline signal<rbit4<l,s> > &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline signal<rbit4<l,s> > &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline signal<rbit4<l,s> > &operator++();	/* increment operator #1 (pre-) */

inline signal<rbit4<l,s> > operator++(	/* increment operator #2 (post-) */
	int);

inline signal<rbit4<l,s> > &operator--();	/* decrement operator #1 (pre-) */

inline signal<rbit4<l,s> > operator--(	/* decrement operator #2 (post-) */
	int);


// bitvector slices

virtual inline _specc::rsig4slice rsig4_slice(	/* single bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::rsig4slice rsig4_slice(	/* slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::rsig4slice rsig4_slice(/* const bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::rsig4slice rsig4_slice(/* const slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::sig4bus_elem* sig4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::sig4bus_elem* sig4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);
};


	/********************************************************/
	/*** signal template (specialization for array types) ***/
	/********************************************************/


template<int i, class T>
class _specc::signal<T[i]> :	/* special template class for 'signal' arrays */
	public _specc::buffered<T[i]>,	/* is based on 'buffered' template */
	public _specc::event	/* and _is_ an event */
{
public:

inline signal(void);		/* constructor #1 */

explicit inline signal(		/* constructor #2 */
	T		Init[i]);	/* initializer */

inline signal(			/* copy constructor (#3) */
	const signal<T[i]> &Orig);

inline ~signal(void);		/* destructor */

// EJ 08/31/05 -- inherited from traced_object
// log the object's initial state before tracing starts
// Note: this is not always at time = 0	
virtual void _LogInitialState(void); 

inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides dummy in event class) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides dummy in event class) */


inline signal<T[i]> &operator=(		/* assignment operator #1 */
	T		Var[i]);

inline signal<T[i]> &operator=(		/* assignment operator #1b */
	const signal<T[i]> &SignalVar);

inline signal<T[i]> &operator=(		/* assignment operator #1c */
	const _specc::sig_elem<T[i]> &SigElemVar);


inline _specc::sig_elem<T> operator[](	/* array access operator */
	int		Index);
};


	/*******************************************/
	/*** sig_elem template (general version) ***/
	/*******************************************/


template <class T>
class _specc::sig_elem :	/* template class for 'signal' arrays */
	public _specc::buf_elem<T>	/* is based on 'buf_elem' template */
{
public:
_specc::event	*SigEventPtr;	/* pointer to event of this signal */


inline sig_elem(		/* constructor #1 */
	_specc::event	*SigEventPtr,
	void		*CurrPtr,
	void		*NextPtr);

// default copy constructor is just fine

inline ~sig_elem(void);		/* destructor */


inline sig_elem<T> &operator=(	/* assignment operator #1 */
	T		Var);

inline sig_elem<T> &operator=(	/* assignment operator #1b */
	const sig_elem<T> &SigElem);

inline sig_elem<T> &operator+=(	/* assignment operator #2 */
	T		Var);

inline sig_elem<T> &operator-=(	/* assignment operator #3 */
	T		Var);

inline sig_elem<T> &operator*=(	/* assignment operator #4 */
	T		Var);

inline sig_elem<T> &operator/=(	/* assignment operator #5 */
	T		Var);

inline sig_elem<T> &operator%=(	/* assignment operator #6 */
	T		Var);

inline sig_elem<T> &operator^=(	/* assignment operator #7 */
	T		Var);

inline sig_elem<T> &operator&=(	/* assignment operator #8 */
	T		Var);

inline sig_elem<T> &operator|=(	/* assignment operator #9 */
	T		Var);

inline sig_elem<T> &operator<<=(/* assignment operator #10 */
	unsigned int	Var);

inline sig_elem<T> &operator>>=(/* assignment operator #11 */
	unsigned int	Var);


inline sig_elem<T> &operator++();/* increment operator #1 (pre-) */

inline sig_elem<T> operator++(	/* increment operator #2 (post-) */
	int);

inline sig_elem<T> &operator--();/* decrement operator #1 (pre-) */

inline sig_elem<T> operator--(	/* decrement operator #2 (post-) */
	int);


// bitvector slices

inline _specc::sigslice sig_slice(		/* single bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::sigslice sig_slice(		/* slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::sigslice sig_slice(	/* const bit of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::sigslice sig_slice(	/* const slice of a bitvector */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;


//PC: 11/10/05 Changes for bit4 resolved
// bit4vector slices

inline _specc::sig4slice sig4_slice(              /* single bit of a bitvector */
        _bit::bnd_t     ll,
        _bit::bnd_t     r,
        _bit::bnd_t     i);

inline _specc::sig4slice sig4_slice(              /* slice of a bitvector */
        _bit::bnd_t     ll,
        _bit::bnd_t     r,
        _bit::bnd_t     sl,
        _bit::bnd_t     sr);

inline const _specc::sig4slice sig4_slice(        /* const bit of a bitvector */
        _bit::bnd_t     ll,
        _bit::bnd_t     r,
        _bit::bnd_t     i) const;

inline const _specc::sig4slice sig4_slice(        /* const slice of a bitvector */
        _bit::bnd_t     ll,
        _bit::bnd_t     r,
        _bit::bnd_t     sl,
        _bit::bnd_t     sr) const;


// struct/union member access

template <class MT>
inline _specc::sig_elem<MT> member(		/* member access "operator" */
	int		CharOffset);

template <class MT>
inline const _specc::sig_elem<MT> member(	/* const member access "op." */
	int		CharOffset) const;
};


	/**********************************************************/
	/*** sig_elem template (specialization for array types) ***/
	/**********************************************************/


template<int i, class T>
class _specc::sig_elem<T[i]> :	/* special template for multi-dim. arrays */
	public _specc::buf_elem<T[i]>	/* is based on 'buf_elem' template */
{
public:
_specc::event	*SigEventPtr;	/* pointer to event of this signal */


inline sig_elem(		/* constructor #1 */
	_specc::event	*SigEventPtr,
	void		*CurrPtr,
	void		*NextPtr);

// default copy constructor is just fine

inline ~sig_elem(void);		/* destructor */

inline sig_elem<T[i]> &operator=(	/* assignment operator #1 */
	T		Var[i]);

inline sig_elem<T[i]> &operator=(	/* assignment operator #1b */
	const sig_elem<T[i]> &SigElem);

//PC 06/21/04
inline sig_elem<T[i]> &operator=(	/* assignment operator #1c */
	const _specc::signal<T[i]> &SignalVar);


inline sig_elem<T> operator[](		/* array access operator */
	int		Index);
};


	/************************/
	/*** _specc::sigslice ***/
	/************************/


class _specc::sigslice :	/* class for 'signal' bitvector slices */
	public _specc::bufslice		/* is based on 'buffered' slices */
{
public:
_specc::event	*SigEventPtr;	/* pointer to event of this signal */


inline sigslice(		/* constructor #1 */
	_specc::event	*SigEventPtr,
	const _bit	&CurrVec,
	const _bit	&NextVec,
	_bit::len_t	Left,
	_bit::len_t	Right,
	bool		Unsigned,
	sigbus_elem	*Slices = NULL); /* only for slice of sigbus */

inline sigslice(		/* copy constructor */
	const sigslice	&Orig);

inline ~sigslice(void);		/* destructor */


inline sigslice &operator=(	/* assignment operator #1 */
	_bit		Var);

inline sigslice &operator=(	/* assignment operator #1b */
	const sigslice &SigSlice);

inline sigslice &operator+=(	/* assignment operator #2 */
	_bit		Var);

inline sigslice &operator-=(	/* assignment operator #3 */
	_bit		Var);

inline sigslice &operator*=(	/* assignment operator #4 */
	_bit		Var);

inline sigslice &operator/=(	/* assignment operator #5 */
	_bit		Var);

inline sigslice &operator%=(	/* assignment operator #6 */
	_bit		Var);

inline sigslice &operator^=(	/* assignment operator #7 */
	_bit		Var);

inline sigslice &operator&=(	/* assignment operator #8 */
	_bit		Var);

inline sigslice &operator|=(	/* assignment operator #9 */
	_bit		Var);

inline sigslice &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline sigslice &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline sigslice &operator++();	/* increment operator #1 (pre-) */

inline sigslice operator++(	/* increment operator #2 (post-) */
	int);

inline sigslice &operator--();	/* decrement operator #1 (pre-) */

inline sigslice operator--(	/* decrement operator #2 (post-) */
	int);


inline _specc::sigslice sig_slice(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::sigslice sig_slice(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::sigslice sig_slice(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::sigslice sig_slice(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;


inline void Notify(void) const;			/* notify a signal slice */
};


//PC: 11/10/05 Changes for bit4 resolved
	/************************/
	/*** _specc::sig4slice ***/
	/************************/


class _specc::sig4slice :	/* class for 'signal' bitvector slices */
       public _specc::buf4slice		/* is based on 'buffered' slices */
{
public:
_specc::event	*SigEventPtr;	/* pointer to event of this signal */


inline sig4slice(		/* constructor #1 */
	_specc::event	*SigEventPtr,
	const _bit4	&CurrVec,
	const _bit4	&NextVec,
	_bit::len_t	Left,
	_bit::len_t	Right,
	bool		Unsigned,
	sig4bus_elem	*Slices = NULL); /* only for slice of sigbus */

inline sig4slice(		/* copy constructor */
	const sig4slice	&Orig);

inline ~sig4slice(void);		/* destructor */


inline sig4slice &operator=(	/* assignment operator #1 */
	_bit4		Var);

inline sig4slice &operator=(	/* assignment operator #1b */
	const sig4slice &SigSlice);

inline sig4slice &operator+=(	/* assignment operator #2 */
	_bit4		Var);

inline sig4slice &operator-=(	/* assignment operator #3 */
	_bit4		Var);

inline sig4slice &operator*=(	/* assignment operator #4 */
	_bit4		Var);

inline sig4slice &operator/=(	/* assignment operator #5 */
	_bit4		Var);

inline sig4slice &operator%=(	/* assignment operator #6 */
	_bit4		Var);

inline sig4slice &operator^=(	/* assignment operator #7 */
	_bit4		Var);

inline sig4slice &operator&=(	/* assignment operator #8 */
	_bit4		Var);

inline sig4slice &operator|=(	/* assignment operator #9 */
	_bit4		Var);

inline sig4slice &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline sig4slice &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline sig4slice &operator++();	/* increment operator #1 (pre-) */

inline sig4slice operator++(	/* increment operator #2 (post-) */
	int);

inline sig4slice &operator--();	/* decrement operator #1 (pre-) */

inline sig4slice operator--(	/* decrement operator #2 (post-) */
	int);


inline _specc::sig4slice sig4_slice(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::sig4slice sig4_slice(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::sig4slice sig4_slice(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::sig4slice sig4_slice(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;


inline void Notify(void) const;			/* notify a signal slice */
};

//PC: 11/14/05 Changes for resolved bit4 
	/************************/
	/*** _specc::rsig4slice ***/
	/************************/


class _specc::rsig4slice :	/* class for 'signal' bitvector slices */
       public _specc::rbuf4slice		/* is based on 'buffered' slices */
{
public:
_specc::event	*SigEventPtr;	/* pointer to event of this signal */


inline rsig4slice(		/* constructor #1 */
	_specc::event	*SigEventPtr,
	const _bit4	&CurrVec,
	const _bit4	&NextVec,
	_bit::len_t	Left,
	_bit::len_t	Right,
	bool		Unsigned,
	sig4bus_elem	*Slices = NULL); /* only for slice of sigbus */

inline rsig4slice(		/* copy constructor */
	const rsig4slice	&Orig);

inline ~rsig4slice(void);		/* destructor */


inline rsig4slice &operator=(	/* assignment operator #1 */
	_bit4		Var);

inline rsig4slice &operator=(	/* assignment operator #1b */
	const sig4slice &SigSlice);

inline rsig4slice &operator+=(	/* assignment operator #2 */
	_bit4		Var);

inline rsig4slice &operator-=(	/* assignment operator #3 */
	_bit4		Var);

inline rsig4slice &operator*=(	/* assignment operator #4 */
	_bit4		Var);

inline rsig4slice &operator/=(	/* assignment operator #5 */
	_bit4		Var);

inline rsig4slice &operator%=(	/* assignment operator #6 */
	_bit4		Var);

inline rsig4slice &operator^=(	/* assignment operator #7 */
	_bit4		Var);

inline rsig4slice &operator&=(	/* assignment operator #8 */
	_bit4		Var);

inline rsig4slice &operator|=(	/* assignment operator #9 */
	_bit4		Var);

inline rsig4slice &operator<<=(	/* assignment operator #10 */
	unsigned int	Var);

inline rsig4slice &operator>>=(	/* assignment operator #11 */
	unsigned int	Var);


inline rsig4slice &operator++();	/* increment operator #1 (pre-) */

inline rsig4slice operator++(	/* increment operator #2 (post-) */
	int);

inline rsig4slice &operator--();	/* decrement operator #1 (pre-) */

inline rsig4slice operator--(	/* decrement operator #2 (post-) */
	int);


inline _specc::rsig4slice rsig4_slice(		/* single bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

inline _specc::rsig4slice rsig4_slice(		/* slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

inline const _specc::rsig4slice rsig4_slice(	/* const bit of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

inline const _specc::rsig4slice rsig4_slice(	/* const slice of a bitvector */
	_bit::bnd_t	l,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;


inline void Notify(void) const;			/* notify a signal slice */
};

	/**********************/
	/*** _specc::sigbus ***/
	/**********************/


template <_bit::len_t len, bool usign>
class _specc::sigbus :		/* class for sliced 'signal' port mapping */
	public _specc::signal<bit<len,usign> > 
                                /* is based on 'signal' bitvectors */
{
public:
sigbus_elem	*Slices;	/* list of bus slices */


inline sigbus(			/* constructor #1 */
	sigbus_elem	*Slices);

inline ~sigbus(void);		/* destructor */

inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides event and signal classes) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides event and signal classes) */

// bitvector slices

virtual inline _specc::sigslice sig_slice(	/* single bit of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::sigslice sig_slice(	/* slice of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::sigslice sig_slice( /* const bit of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::sigslice sig_slice( /* const slice of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::sigbus_elem* sigbus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::sigbus_elem* sigbus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;	/* store value for sigbus */
					/* (override dummy method) */

virtual inline void Get(void) const;	/* load value for sigbus */
					/* (override dummy method) */

virtual inline void Notify(void);	/* notify all bus events */
					/* (override event method) */

virtual inline bool IsNotified(void);	/* check if this has been notified */

virtual inline _specc::event_ptr *MappedEvents(	/* get port-mapped events */
	void);					/* (override event method) */
};


//PC: 11/10/05 Changes for bit4 resolved
	/**********************/
	/*** _specc::sig4bus ***/
	/**********************/


template <_bit::len_t len, bool usign>
class _specc::sig4bus :		/* class for sliced 'signal' port mapping */
	public _specc::signal<bit4<len,usign> > 
                                /* is based on 'signal' bitvectors */
{
public:
sig4bus_elem	*Slices;	/* list of bus slices */


inline sig4bus(			/* constructor #1 */
	sig4bus_elem	*Slices);

inline ~sig4bus(void);		/* destructor */


inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides event and signal classes) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides event and signal classes) */


// bit4vector slices

virtual inline _specc::sig4slice sig4_slice(	/* single bit of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::sig4slice sig4_slice(	/* slice of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::sig4slice sig4_slice( /* const bit of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::sig4slice sig4_slice( /* const slice of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::sig4bus_elem* sig4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::sig4bus_elem* sig4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;	/* store value for sigbus */
					/* (override dummy method) */

virtual inline void Get(void) const;	/* load value for sigbus */
					/* (override dummy method) */

virtual inline void Notify(void);	/* notify all bus events */
					/* (override event method) */

virtual inline bool IsNotified(void);	/* check if this has been notified */

virtual inline _specc::event_ptr *MappedEvents(	/* get port-mapped events */
	void);					/* (override event method) */
};

//PC: 11/14/05 Changes for resolved bit4 
	/**********************/
	/*** _specc::rsig4bus ***/
	/**********************/


template <_bit::len_t len, bool usign>
class _specc::rsig4bus :		/* class for sliced 'signal' port mapping */
	public _specc::signal<rbit4<len,usign> > 
                                /* is based on 'signal' bitvectors */
{
public:
sig4bus_elem	*Slices;	/* list of bus slices */


inline rsig4bus(			/* constructor #1 */
	sig4bus_elem	*Slices);

inline ~rsig4bus(void);		/* destructor */


inline bool IsValidEdge(	/* signal edge event filter */
	_specc::EDGE	Edge);	/* (overrides event and signal classes) */

inline bool ResetIsActive(	/* asynchronous reset signal check */
	bool	ResetActiveHi);	/* (overrides event and signal classes) */


// bit4vector slices

virtual inline _specc::rsig4slice rsig4_slice(	/* single bit of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i);

virtual inline _specc::rsig4slice rsig4_slice(	/* slice of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);

virtual inline const _specc::rsig4slice rsig4_slice( /* const bit of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	i) const;

virtual inline const _specc::rsig4slice rsig4_slice( /* const slice of a sigbus */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr) const;

// bitvector bus slices

virtual inline _specc::sig4bus_elem* sig4bus_slice(/* bus slice in a port map #1*/
	_bit::len_t	sl,
	_bit::len_t	sr);

inline _specc::sig4bus_elem* sig4bus_slice(	/* bus slice in a port map #2 */
	_bit::bnd_t	ll,
	_bit::bnd_t	r,
	_bit::bnd_t	sl,
	_bit::bnd_t	sr);


virtual inline void Put(void) const;	/* store value for sigbus */
					/* (override dummy method) */

virtual inline void Get(void) const;	/* load value for sigbus */
					/* (override dummy method) */

virtual inline void Notify(void);	/* notify all bus events */
					/* (override event method) */

virtual inline bool IsNotified(void);	/* check if this has been notified */

virtual inline _specc::event_ptr *MappedEvents(	/* get port-mapped events */
	void);					/* (override event method) */
};

	/***************************/
	/*** _specc::sigbus_elem ***/
	/***************************/


class _specc::sigbus_elem :	/* class for sliced 'signal' port map */
	public _specc::bufbus_elem
{
public:
_specc::event	*EventPtr;	/* reference to event for this slice */

inline sigbus_elem(		/* constructor #1 */
	const _bit	&CurrVal,
	const _bit	&NextVal,
	_bit::len_t	sl,
	_bit::len_t	sr,
	_specc::event	*EventPtr,
	sigbus_elem	*Next = NULL);

inline ~sigbus_elem(void);	/* destructor */


inline sigbus_elem *concat(	/* concatenation */
	sigbus_elem	*List);

inline sigbus_elem *slice(	/* slicing */
	_bit::len_t	l,
	_bit::len_t	r);

inline void Notify(void);	/* notify all events in this list */
};


//PC: 11/10/05 Changes for bit4 resolved
	/***************************/
	/*** _specc::sig4bus_elem ***/
	/***************************/


class _specc::sig4bus_elem :	/* class for sliced 'signal' port map */
	public _specc::buf4bus_elem
{
public:
_specc::event	*EventPtr;	/* reference to event for this slice */

inline sig4bus_elem(		/* constructor #1 */
	const _bit4	&CurrVal,
	const _bit4	&NextVal,
	_bit::len_t	sl,
	_bit::len_t	sr,
	_specc::event	*EventPtr,
	sig4bus_elem	*Next = NULL);

inline ~sig4bus_elem(void);	/* destructor */


inline sig4bus_elem *concat(	/* concatenation */
	sig4bus_elem	*List);

inline sig4bus_elem *slice(	/* slicing */
	_bit::len_t	l,
	_bit::len_t	r);

inline void Notify(void);	/* notify all events in this list */
};


	/***************************************/
	/*** _bitbus (moved here from bit.h) ***/
	/***************************************/

/* --- Bit vector bus (concatenated bit slices) --- */

template<_bit::len_t len, bool usign=false> class _bitbus
  : public bit<len,usign>
{
  typedef _bit::len_t len_t;  
  typedef _bit::bnd_t bnd_t;
  
protected:
  _bitbus_element* lst;

public:
  // Constructor
  _bitbus(_bitbus_element* list): lst(list)                                 { }
  
  // Destructor
  virtual ~_bitbus() 
     { for(_bitbus_element* el=lst; el; el=lst)  { lst=el->next; delete el; } }
  
  // Create a piece for a bit vector bus
  virtual _bitbus_element* bus_slice(len_t sl, len_t sr) 
                                                 { return lst->slice(sl, sr); }
protected:
  // Create a slice
  virtual _bitslice slice(len_t l, len_t r, bool usgn = usign)
                  { return _bitslice(this->vec, len, usign, l, r, usgn, lst); }
  virtual const _bitslice slice(len_t l, len_t r, bool usgn = usign) const
                  { return _bitslice(const_cast<_bit::chunk*>(this->vec), 
                    		                len, usign, l, r, usgn, lst); }
  // Sync with original vectors
  virtual _bitbus& put()               { lst->put(this->ref()); return *this; }
  virtual _bitbus& get()               { lst->get(this->ref()); return *this; }
};


	/*****************************************/
	/*** _bit4bus (moved here from bit4.h) ***/
	/*****************************************/

/* --- Bit vector bus (concatenated bit4 slices) --- */

template<_bit::len_t len, bool usign=false> class _bit4bus
  : public bit4<len,usign>
{
  typedef _bit::len_t len_t;  
  typedef _bit::bnd_t bnd_t;
  
protected:
  _bit4bus_element* lst;

public:
  // Constructor
  _bit4bus(_bit4bus_element* list): lst(list)                               { }
  
  // Desctructor
  virtual ~_bit4bus() 
     { for(_bit4bus_element* el=lst; el; el=lst) { lst=el->next; delete el; } }
  
  // Create a piece for a bit vector bus
  virtual _bit4bus_element* bus_slice(len_t sl, len_t sr) 
                                                 { return lst->slice(sl, sr); }
protected:
  // Create a slice
  virtual _bit4slice slice(len_t l, len_t r, bool usgn = usign)
    { return _bit4slice(this->vec, this->vecxz, len, usign, l, r, usgn, lst); }
  virtual const _bit4slice slice(len_t l, len_t r, bool usgn = usign) const
    { return _bit4slice(const_cast<_bit::chunk*>(this->vec),
         const_cast<_bit::chunk*>(this->vecxz), len, usign, l, r, usgn, lst); }
  // Sync with original vectors
  virtual _bit4bus& put()              { lst->put(this->ref()); return *this; }
  virtual _bit4bus& get()              { lst->get(this->ref()); return *this; }
};


	/*******************************************/
	/*** _rbit4bus (moved here from rbit4.h) ***/
	/*******************************************/

/* --- Bit vector bus (concatenated rbit4 slices) --- */

template<_bit::len_t len, bool usign=false> class _rbit4bus
  : public rbit4<len,usign>
{
  typedef _bit::len_t len_t;  
  typedef _bit::bnd_t bnd_t;
  
protected:
  _bit4bus_element* lst;

public:
  // Constructor
  _rbit4bus(_bit4bus_element* list): lst(list)                              { }
  
  // Desctructor
  virtual ~_rbit4bus() 
    { for(_bit4bus_element* el=lst; el; el=lst)  { lst=el->next; delete el; } }
  
  // Create a piece for a bit vector bus
  virtual _bit4bus_element* bus_slice(len_t sl, len_t sr) 
                                                 { return lst->slice(sl, sr); }
protected:
  // Create a slice
  virtual _rbit4slice Slice(len_t l, len_t r, bool usgn = usign)
    { return _rbit4slice(this->vec, this->vecxz, len, usign, l, r, usgn, 
                                                                 *this, lst); }
  virtual const _rbit4slice Slice(len_t l, len_t r, bool usgn = usign) const
    { return _rbit4slice(const_cast<_bit::chunk*>(this->vec),
         const_cast<_bit::chunk*>(this->vecxz), len, usign, l, r, usgn, 
                                                                 *this, lst); }
  // Sync with original vectors
  virtual _rbit4bus& put()             { lst->put(this->ref()); return *this; }
  virtual _rbit4bus& get()             { lst->get(this->ref()); return *this; }
};


/*** exported variables *************************************************/


	/* (none) */


/************************************************************************/
/*** prototype definitions for exported functions		      ***/
/************************************************************************/


	/*** event lists ***/


inline _specc::event_ptr *event(	/* any event (for 'wait', 'notify'...)*/
	_specc::event	*Event,
	_specc::event_ptr *Next = NULL)
{

return(new _specc::event_ptr(Event, _specc::EDGE_ANY, Next));

} /* end of event */


inline _specc::event_ptr *rising(	/* rising signal (for 'wait'...) */
	_specc::event	*Event,
	_specc::event_ptr *Next = NULL)
{

return(new _specc::event_ptr(Event, _specc::EDGE_RISING, Next));

} /* end of rising */


inline _specc::event_ptr *falling(	/* falling signal (for 'wait'...) */
	_specc::event	*Event,
	_specc::event_ptr *Next = NULL)
{

return(new _specc::event_ptr(Event, _specc::EDGE_FALLING, Next));

} /* end of falling */


/*** "implementation" ***************************************************/


#include <piped.h>	/* insert 'piped' template implementation */
#include <signals.h>	/* insert 'signal' template implementation */


#endif /* __SPECC_H */

/* EOF specc.h */
