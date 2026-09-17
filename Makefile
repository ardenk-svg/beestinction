# Sample makefile for Unit Test Frameworks

CXX ?= g++
CXXFLAGS ?= -Wall -Werror -pedantic -g --std=c++17 -Wno-sign-compare -Wno-comment

# Compile the tests executable
tutorial_tests.exe: tutorial_tests.cpp tutorial.cpp
	$(CXX) $(CXXFLAGS) tutorial_tests.cpp tutorial.cpp -o tutorial_tests.exe

# Remove automatically generated files
clean :
	rm -rvf *.exe *~ *.out *.dSYM *.stackdump

# Disable built-in rules
.SUFFIXES:
