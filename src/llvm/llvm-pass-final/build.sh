export LLVM_DIR=~/ug3-ct/build

cmake3 -S . -B build

make -C build

~/ug3-ct/build/bin/clang -S -emit-llvm -Xclang -disable-O0-optnone ../dead.c -o dead.ll

~/ug3-ct/build/bin/opt -S -load build/src/libMyPass.so -mem2reg -mypass dead.ll -o dead-opt.ll


# ~/ug3-ct/build/bin/opt -S -load build/src/libMyPass.so -mem2reg -mypass ../snippet.ll -o snippet-opt.ll
