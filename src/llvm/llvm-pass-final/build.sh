export LLVM_DIR=~/ug3-ct/build

cmake3 -S . -B build

make -C build

~/ug3-ct/build/bin/clang -S -emit-llvm -Xclang -disable-O0-optnone ../dead.c -o dead.ll

# run my final pass
~/ug3-ct/build/bin/opt -S -load build/src/libMyPass.so -mem2reg -mypass dead.ll -o dead-opt-mypass.ll

# run llvm dce pass
~/ug3-ct/build/bin/opt -S -mem2reg -dce dead.ll -o dead-opt.ll

# ~/ug3-ct/build/bin/opt -S -load build/src/libMyPass.so -mem2reg -mypass ../snippet.ll -o snippet-opt.ll

echo "compiled code..."
echo "checking md5:"
MYPASS_MD5="$(md5sum dead-opt-mypass.ll)"
LLVM_MD5="$(md5sum dead-opt.ll)"

echo $MYPASS_MD5
echo $LLVM_MD5

~/ug3-ct/build/bin/opt -O0 -load build/src/libMyPass.so -mem2reg -mypass ../snippet.ll -S -o student_result.ll 2>student.stderr
