<!DOCTYPE SCE>
<sce>
 <project>vocoder.sce</project>
 <compiler>
  <option name="libpath" ></option>
  <option name="libs" >-xl src/common/pack.o</option>
  <option name="incpath" >src/common</option>
  <option name="importpath" >src:src/common:src/lp_analysis:src/open_loop:src/closed_loop:src/codebook:src/update:src/processing:.</option>
  <option name="defines" ></option>
  <option name="undefines" ></option>
  <option verbosity="0" name="options" warning="0" > -v</option>
 </compiler>
 <simulator>
  <option type="0" name="tracing" debug="False" calls="False" ></option>
  <option type="2" name="terminal" >xterm -title %e -e</option>
  <option name="logging" enabled="False" >.log</option>
  <option name="command" >time ./%e src/speechfiles/spch_unx.inp nodtx.bit nodtx &amp;&amp; 	    diff -s src/speechfiles/nodtx_good.bit nodtx.bit</option>
 </simulator>
 <models>
  <item type="" name="vocoderSpec.sir" >
   <item input="vocoderArch.in.sir" type="scar,vocoderSpec,-b,-m,-c,-w,-i,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderArch.in.sir,-o,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderArch.sir" level="0x700L" name="vocoderArch.sir" >
    <item input="vocoderSched.in.sir" type="scar,vocoderArch,-s,-i,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderSched.in.sir,-o,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderArch.sched.tmp.sir;scos,vocoderArch,-v,-i,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderArch.sched.tmp.sir,-o,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderSched.sir" level="0x1f00L" name="vocoderSched.sir" >
     <item input="vocoderNet.in.sir" type="scnr,vocoderSched,-v,-O,-falign,-fmerge,-i,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderNet.in.sir,-o,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderNet.sir" level="0x71f00L" name="vocoderNet.sir" >
      <item input="vocoderPam.in.sir" type="sccr,vocoderNet,-v,-O,-i,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderPam.in.sir,-o,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderPam.sir" level="0x3071f00L" name="vocoderPam.sir" />
      <item input="vocoderTlm.in.sir" type="sccr,vocoderNet,-v,-t,-O,-i,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderTlm.in.sir,-o,/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderTlm.sir" level="0x1071f00L" name="vocoderTlm.sir" />
     </item>
    </item>
   </item>
  </item>
 </models>
 <imports>
  <item name="F_gamma" />
  <item name="arg_handler" />
  <item name="array_op" />
  <item name="autocorr" />
  <item name="az_lsp" />
  <item name="basic_func" />
  <item name="basic_op" />
  <item name="build_cn_code" />
  <item name="build_code" />
  <item name="c_double_handshake" />
  <item name="closed_loop" />
  <item name="cn_encoding" />
  <item name="cod_12k2" />
  <item name="code_10i40_35bits" />
  <item name="codebook" />
  <item name="codebook_cn" />
  <item name="coder" />
  <item name="compute_CN_excitation_gain" />
  <item name="convolve" />
  <item name="copy" />
  <item name="cor_h" />
  <item name="cor_h_x" />
  <item name="enc_lag6" />
  <item name="ex_syn_upd_sh" />
  <item name="excitation" />
  <item name="filter_and_scale" />
  <item name="find_az" />
  <item name="find_targetvec" />
  <item name="g_code" />
  <item name="g_pitch" />
  <item name="get_minmax" />
  <item name="homing_test" />
  <item name="i_receiver" />
  <item name="i_sender" />
  <item name="i_tranceiver" />
  <item name="imp_resp" />
  <item name="int_lpc" />
  <item name="lag_wind" />
  <item name="levinson" />
  <item name="lp_analysis" />
  <item name="lsp_az" />
  <item name="monitor" />
  <item name="no_speech_upd" />
  <item name="nodtx_setflags" />
  <item name="ol_lag_est" />
  <item name="open_loop" />
  <item name="par_weight" />
  <item name="period_upd" />
  <item name="pitch_contr" />
  <item name="pitch_fr6" />
  <item name="pitch_ol" />
  <item name="post_process" />
  <item name="pre_process" />
  <item name="pred_lt_6" />
  <item name="prefilter" />
  <item name="prm2bits" />
  <item name="q_gain_code" />
  <item name="q_gain_pitch" />
  <item name="q_p" />
  <item name="q_plsf_5" />
  <item name="q_plsf_and_intlpc" />
  <item name="reset" />
  <item name="residu" />
  <item name="search_10i40" />
  <item name="set_sign" />
  <item name="shift_signals" />
  <item name="sid_codeword_encode" />
  <item name="std_includes" />
  <item name="stimulus" />
  <item name="subframes" />
  <item name="syn_filt" />
  <item name="tx_dtx" />
  <item name="upd_mem" />
  <item name="update" />
  <item name="vad_comp" />
  <item name="vad_lp" />
  <item name="vocoder" />
  <item name="weight_ai" />
  <item name="c_handshake" />
  <item name="dsp56600_0" />
  <item name="i_receive" />
  <item name="i_send" />
  <item name="stdHW" />
  <item name="OSNone" />
  <item name="i_os_api" />
  <item name="c_mutex" />
  <item name="dsp56600bf_0" />
  <item name="dsp566portA_2_0" />
  <item name="i_semaphore" />
 </imports>
 <sources>
  <item name="/ECEnet/Apps1/sce/latest/inc/bits/pthreadtypes.h" />
  <item name="/ECEnet/Apps1/sce/latest/inc/sim.sh" />
  <item name="/ECEnet/Apps1/sce/latest/inc/sim/bit.sh" />
  <item name="/ECEnet/Apps1/sce/latest/inc/sim/longlong.sh" />
  <item name="/ECEnet/Apps1/sce/latest/inc/sim/sim.sh" />
  <item name="/ECEnet/Apps1/sce/latest/inc/sim/time.sh" />
  <item name="/usr/include/_G_config.h" />
  <item name="/usr/include/alloca.h" />
  <item name="/usr/include/bits/sigset.h" />
  <item name="/usr/include/bits/sys_errlist.h" />
  <item name="/usr/include/bits/time.h" />
  <item name="/usr/include/bits/types.h" />
  <item name="/usr/include/libio.h" />
  <item name="/usr/include/stdio.h" />
  <item name="/usr/include/stdlib.h" />
  <item name="/usr/include/string.h" />
  <item name="/usr/include/sys/select.h" />
  <item name="/usr/include/sys/types.h" />
  <item name="/usr/include/time.h" />
  <item name="/usr/include/wchar.h" />
  <item name="/usr/lib/gcc/x86_64-redhat-linux/4.4.7/include/stddef.h" />
  <item name="/usr/lib/gcc/x86_64-redhat-linux/4.8.5/include/stdarg.h" />
  <item name="/usr/lib/gcc/x86_64-redhat-linux/4.8.5/include/stddef.h" />
  <item name="c_double_handshake.sc" />
  <item name="i_receiver.sc" />
  <item name="i_sender.sc" />
  <item name="i_tranceiver.sc" />
  <item name="src/EFR_Coder_public.sc" />
  <item name="src/arg_handler.sc" />
  <item name="src/closed_loop/closed_loop.sc" />
  <item name="src/closed_loop/compute_CN_excitation_gain.sc" />
  <item name="src/closed_loop/convolve.sc" />
  <item name="src/closed_loop/enc_lag6.sc" />
  <item name="src/closed_loop/find_targetvec.sc" />
  <item name="src/closed_loop/g_pitch.sc" />
  <item name="src/closed_loop/imp_resp.sc" />
  <item name="src/closed_loop/par_weight.sc" />
  <item name="src/closed_loop/pitch_fr6.sc" />
  <item name="src/closed_loop/pred_lt_6.sc" />
  <item name="src/closed_loop/q_gain_pitch.sc" />
  <item name="src/cod_12k2.sc" />
  <item name="src/codebook/build_cn_code.sc" />
  <item name="src/codebook/build_code.sc" />
  <item name="src/codebook/code_10i40_35bits.sc" />
  <item name="src/codebook/codebook.sc" />
  <item name="src/codebook/codebook_cn.sc" />
  <item name="src/codebook/cor_h.sc" />
  <item name="src/codebook/cor_h_x.sc" />
  <item name="src/codebook/g_code.sc" />
  <item name="src/codebook/pitch_contr.sc" />
  <item name="src/codebook/prefilter.sc" />
  <item name="src/codebook/q_p.sc" />
  <item name="src/codebook/search_10i40.sc" />
  <item name="src/codebook/set_sign.sc" />
  <item name="src/coder.sc" />
  <item name="src/common/F_gamma.sc" />
  <item name="src/common/array_op.sc" />
  <item name="src/common/basic_func.sc" />
  <item name="src/common/basic_op.sc" />
  <item name="src/common/copy.sc" />
  <item name="src/common/lsp_az.sc" />
  <item name="src/common/reset.sc" />
  <item name="src/common/residu.sc" />
  <item name="src/common/std_includes.sc" />
  <item name="src/common/syn_filt.sc" />
  <item name="src/common/typedef.sh" />
  <item name="src/common/weight_ai.sc" />
  <item name="src/lp_analysis/autocorr.sc" />
  <item name="src/lp_analysis/az_lsp.sc" />
  <item name="src/lp_analysis/find_az.sc" />
  <item name="src/lp_analysis/int_lpc.sc" />
  <item name="src/lp_analysis/lag_wind.sc" />
  <item name="src/lp_analysis/levinson.sc" />
  <item name="src/lp_analysis/lp_analysis.sc" />
  <item name="src/lp_analysis/lp_analysis.tab" />
  <item name="src/lp_analysis/no_speech_upd.sc" />
  <item name="src/lp_analysis/nodtx_setflags.sc" />
  <item name="src/lp_analysis/q_plsf_5.sc" />
  <item name="src/lp_analysis/q_plsf_5.tab" />
  <item name="src/lp_analysis/q_plsf_and_intlpc.sc" />
  <item name="src/lp_analysis/tx_dtx.sc" />
  <item name="src/lp_analysis/vad_comp.sc" />
  <item name="src/lp_analysis/vad_lp.sc" />
  <item name="src/monitor.sc" />
  <item name="src/open_loop/get_minmax.sc" />
  <item name="src/open_loop/ol_lag_est.sc" />
  <item name="src/open_loop/open_loop.sc" />
  <item name="src/open_loop/period_upd.sc" />
  <item name="src/open_loop/pitch_ol.sc" />
  <item name="src/processing/cn_encoding.sc" />
  <item name="src/processing/filter_and_scale.sc" />
  <item name="src/processing/homing_test.sc" />
  <item name="src/processing/post_process.sc" />
  <item name="src/processing/pre_process.sc" />
  <item name="src/processing/prm2bits.sc" />
  <item name="src/processing/sid_codeword_encode.sc" />
  <item name="src/stimulus.sc" />
  <item name="src/subframes.sc" />
  <item name="src/update/ex_syn_upd_sh.sc" />
  <item name="src/update/excitation.sc" />
  <item name="src/update/q_gain_code.sc" />
  <item name="src/update/shift_signals.sc" />
  <item name="src/update/upd_mem.sc" />
  <item name="src/update/update.sc" />
  <item name="src/vocoder.sc" />
  <item name="testbench.sc" />
  <item name="/ECEnet/Apps1/sce/latest/share/sce/db/processors/dsp/dsp56600.sc" />
  <item name="c_handshake.sc" />
  <item name="i_receive.sc" />
  <item name="i_send.sc" />
  <item name="stdHW.sc" />
  <item name="/ECEnet/Apps1/sce/latest/share/sce/db/processors/dsp/dsp56600os.sc" />
  <item name="OSNone.sc" />
  <item name="global.sh" />
  <item name="i_os_api.sc" />
  <item name="/ECEnet/Apps1/sce/latest/share/sce/db/busses/processor/dsp566portA.sc" />
  <item name="/ECEnet/Apps1/sce/latest/share/sce/db/processors/dsp/dsp56600bf.sc" />
  <item name="c_mutex.sc" />
  <item name="i_semaphore.sc" />
  <item name="/Users/Grad/asultan/Documents/hwsw/Assignment3/vocoderArch.si" />
 </sources>
 <metrics>
  <option name="types" >bit[13],short int[10],pthread_condattr_t,int[6],__fsid_t,pthread_mutexattr_t,short int[512],int[40],short int,fd_set,short int[57],short int[240],void*,short int[95],void,short int[61],short int[1024],char[4],int[9],short int[50],unsigned short int[7],char[32],pthread_rwlock_t,short int[6],char[20],short int[320],unsigned long long int,short int[25],short int[1600],struct _IO_marker,short int[8],char[1],unsigned short int,short int[16],event,__sigset_t,short int[65],div_t,int[27],ldiv_t,short int[314],_G_fpos_t,pthread_barrier_t,short int[24],short int[4][11],unsigned char,long int[16],short int[3],unsigned int,short int[40],short int[64],unsigned long int,enum __codecvt_result,pthread_attr_t,long int,short int[33],float,char[40],int[2],struct __pthread_internal_list,short int[244],char[48],long double,struct random_data,char[56],short int[256],__mbstate_t,struct drand48_data,_G_fpos64_t,long long int,short int[9],struct timespec,short int[49],pthread_barrierattr_t,short int[2],struct timeval,unsigned short int[3],double,short int[303],int[36],char[8],pthread_cond_t,int,short int[80],char,struct _IO_FILE,lldiv_t,short int[5],bool,char[21],short int[11],struct _fp,short int[32],short int[28],pthread_rwlockattr_t,unsigned long int[16],short int[160],short int[7][10],short int[4],pthread_mutex_t,unsigned char[4],unsigned char[3],unsigned char[484],unsigned char[2],unsigned char[182],signal unsigned bit[1],unsigned bit[16],signal unsigned bit[16],unsigned bit[2],signal unsigned bit[24],unsigned bit[24],unsigned bit[4],unsigned char[1],unsigned bit[8],signal unsigned bit[4],unsigned bit[1]</option>
  <option name="operations" >Convolut,Copy,Div_32,Get_lsp_pol,Inv_sqrt,L_Comp,L_Extract,L_abs,L_add,L_add_c,L_deposit_h,L_deposit_l,L_mac,L_macNs,L_msu,L_mult,L_negate,L_shl,L_shr,L_shr_r,L_sub,Log2,Lsp_Az,Mpy_32,Mpy_32_16,Pow2,Set_zero,abs_s,add,arg_handler,clear_carry,clear_overflow,div_s,extract_h,extract_l,gmed5,interpolate_CN_param,mac_r,msu_r,mult,mult_r,negate,norm_l,norm_s,overflow,round,saturate,shl,shr,shr_r,sub</option>
 </metrics>
</sce>
