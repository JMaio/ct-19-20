export LLVM_DIR=~/ug3-ct/build

cmake3 -S . -B build

make -C build

~/ug3-ct/build/bin/clang -S -emit-llvm -Xclang -disable-O0-optnone ../dead.c

~/ug3-ct/build/bin/opt -load build/src/libMyPass.so -mem2reg -mypass dead.ll


# ~/ug3-ct/build/bin/clang -Xclang -load -Xclang build/src/libMyPass.so ~/ug3-ct/test.c
