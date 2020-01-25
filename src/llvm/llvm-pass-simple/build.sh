export LLVM_DIR=~/ug3-ct/build

cmake3 -S . -B build

make -C build

~/ug3-ct/build/bin/clang -c -Xclang -disable-O0-optnone -emit-llvm -S ../dead.c -o dead.ll

~/ug3-ct/build/bin/opt -S -load build/src/libMyPass.so -mem2reg -mypass dead.ll -o dead-opt.ll

# ~/ug3-ct/build/bin/opt dead.ll -o dead-reg.bc --O3

# ~/ug3-ct/build/bin/llvm-dis -o=dead.disas.ll dead-reg.bc

# ~/ug3-ct/build/bin/clang -Xclang -load -Xclang build/src/libMyPass.so ~/ug3-ct/test.c
