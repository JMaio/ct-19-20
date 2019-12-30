// Example of how to write an LLVM pass
// For more information see: http://llvm.org/docs/WritingAnLLVMPass.html

#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include <vector>
#include <map>


#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"

using namespace llvm;
using namespace std;

namespace {
struct CountOp : public FunctionPass {
  map<string, int> opCounter;
  static char ID;
  CountOp() : FunctionPass(ID) {}

  virtual bool runOnFunction(Function &F) {
    errs() << "Function " << F.getName() << '\n';
    for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
      for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++i) {
        if (opCounter.find(i->getOpcodeName()) == opCounter.end()) {
          opCounter[i->getOpcodeName()] = 1;
        } else {
          opCounter[i->getOpcodeName()] += 1;
        }
      }
    }

    map<string, int>::iterator i = opCounter.begin();
    map<string, int>::iterator e = opCounter.end();

    while (i != e) {
      errs() << i->first << ": " << i->second << "\n";
      i++;
    }
    
    errs() << "\n";
    opCounter.clear();

    return false;
  }
};


// struct MyPass : public FunctionPass {
//   static char ID;
//   MyPass() : FunctionPass(ID) {}

//   int counter = 0;

//   bool runOnFunction(Function &F) override {
//     errs() << "I saw a function called " << F.getName() << "!\n";
//     return false;
//   }
// };
}

char CountOp::ID = 0;
static RegisterPass<CountOp> X("opCounter", "Counts opcodes per functions");
// static RegisterPass<MyPass> X("mypass", "My liveness analysis and dead code elimination pass");

static RegisterStandardPasses Y(
    PassManagerBuilder::EP_EarlyAsPossible,
    [](const PassManagerBuilder &Builder,
       legacy::PassManagerBase &PM) { PM.add(new CountOp()); });

