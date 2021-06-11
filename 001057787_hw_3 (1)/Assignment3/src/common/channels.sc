#include "cnst.sh"
#include "typedef.sh"

import "i_sender";
import "i_receiver";
import "c_double_handshake";

#ifdef EXTERNAL_CONTROL
import "c_handshake";
#endif

#ifdef TYPED_CHANNELS

#include <c_typed_double_handshake.sh>	/* make the template available */

// define the channel data type 'Word16'
DEFINE_I_TYPED_SENDER(Word, Word16)
DEFINE_I_TYPED_RECEIVER(Word, Word16)
DEFINE_I_TYPED_TRANCEIVER(Word, Word16)
DEFINE_C_TYPED_DOUBLE_HANDSHAKE(Word, Word16)
      
// define the channel data type 'Flag'
DEFINE_I_TYPED_SENDER(Flag, Flag)
DEFINE_I_TYPED_RECEIVER(Flag, Flag)
DEFINE_I_TYPED_TRANCEIVER(Flag, Flag)
DEFINE_C_TYPED_DOUBLE_HANDSHAKE(Flag, Flag)

#endif
