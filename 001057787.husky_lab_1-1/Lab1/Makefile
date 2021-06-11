CC=gcc
DEBUG=-pg -g
CFLAGS=-O3	
LIBS= -lm
IMG_DIR= ./img
SOURCE_DIR = ./source
BIN_DIR = ./bin
TEST_IMG = BOSTON.pgm
ANALYSIS_DIR = ./analysis
sigma = 0.60
tlow = 0.30
thigh = 0.80
MASSIF_PRINT=ms_print

canny:
	$(CC) $(CFLAGS) $(SOURCE_DIR)/$@.c $(LIBS) -o $(BIN_DIR)/$@

test: canny clean_test
	$(BIN_DIR)/canny $(IMG_DIR)/$(TEST_IMG) $(sigma) $(tlow) $(thigh); 
	eog $(IMG_DIR)/$(TEST_IMG)_s_$(sigma)_l_$(tlow)_h_$(thigh).pgm;

clean:
	rm -f $(BIN_DIR)/*; find $(IMG_DIR)/*.pgm*.pgm | xargs -t -I % rm %

clean_test:
	find $(IMG_DIR)/$(TEST_IMG)*.pgm | xargs -t -I % rm %

debug:
	$(CC) $(CFLAGS) $(DEBUG) $(SOURCE_DIR)/canny.c $(LIBS) -o $(BIN_DIR)/$@

debug_run: debug
	$(BIN_DIR)/debug $(IMG_DIR)/$(TEST_IMG) $(sigma) $(tlow) $(thigh);

mem: debug_run
	valgrind --tool=massif --time-unit=ms --threshold=0.1 $(BIN_DIR)/debug $(IMG_DIR)/$(TEST_IMG) $(sigma) $(tlow) $(thigh)| echo "0"; 
	find massif.out.* | xargs $(MASSIF_PRINT);
	find massif.out.* | xargs rm;
	rm gmon.out;

leak: debug_run
	valgrind --tool=memcheck --leak-check=yes --show-reachable=yes --num-callers=20 --track-fds=yes $(BIN_DIR)/debug  $(IMG_DIR)/$(TEST_IMG) $(sigma) $(tlow) $(thigh) | echo "0"; 
	rm gmon.out;

prof: debug_run
	touch $(ANALYSIS_DIR)/$^.$@;
	gprof --static-call-graph $(BIN_DIR)/debug gmon.out > $(ANALYSIS_DIR)/$^.$@;
	nano $(ANALYSIS_DIR)/$^.$@;
	rm gmon.out

graph: debug_run
	gprof $(BIN_DIR)/debug gmon.out | gprof2dot -n 0 -e 0| dot -Tpng -o $(ANALYSIS_DIR)/call_graph.png;
	eog $(ANALYSIS_DIR)/call_graph.png;
