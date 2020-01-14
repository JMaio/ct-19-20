// Example of how to write an LLVM pass
// For more information see: http://llvm.org/docs/WritingAnLLVMPass.html

#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include <vector>
#include <map>


#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"

using namespace llvm;
using namespace std;

namespace {
struct MyPass : public FunctionPass {
  map<string, int> opCounter;
  static char ID;
  MyPass() : FunctionPass(ID) {}

  bool hasDeadInstructions(Function &F) {
    SmallVector<Instruction*, 64> Worklist;

    for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
      for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++i) {
        Instruction* I = &*i;
        if (isInstructionTriviallyDead(I)) {
          Worklist.push_back(I);
        }
      }
    }

    bool cutInstruction = !Worklist.empty();

    while (!Worklist.empty()) {
        Instruction* i = Worklist.pop_back_val();
        errs() << "removed instruction " << i->getOpcodeName() << "\n";
        i->eraseFromParent();
    }
    
    return cutInstruction;
  }
  
  virtual bool runOnFunction(Function &F) {

    bool hadDeadCode = false;

    while (hasDeadInstructions(F)) {
      hadDeadCode = true;
    }
    
    return hadDeadCode;
  }
};
}

char MyPass::ID = 0;
// static RegisterPass<MyPass> X("opCounter", "Counts opcodes per functions");
static RegisterPass<MyPass> X("mypass", "My liveness analysis and dead code elimination pass");

// static RegisterStandardPasses Y(
//     PassManagerBuilder::EP_EarlyAsPossible,
//     [](const PassManagerBuilder &Builder,
//        legacy::PassManagerBase &PM) { PM.add(new MyPass()); });

