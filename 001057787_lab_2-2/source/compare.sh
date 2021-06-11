#!/bin/bash

cmp -l ../img/beachbus-ref-edge.pgm ../img/beachbus.pgm_s_0.60_l_0.30_h_0.80.pgm | gawk '{printf "%03d:%03d %02X %02X\n", ($1)/320, $1%320, strtonum(0$2), strtonum(0$3)}'
