// Example of how to write an LLVM pass
// For more information see: http://llvm.org/docs/WritingAnLLVMPass.html

#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"

#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"

#include <vector>
#include <map>
#include <set>


using namespace llvm;
using namespace std;

namespace {
struct MyPass : public FunctionPass {

  static char ID;
  MyPass() : FunctionPass(ID) {}

  void printSet(set<Value*> s) {
    string de = "";
    errs() << "{";
    for (Value* v : s) {
      errs() << de;

      v->printAsOperand(errs(), false);
      de = ",";
    }
    errs() << "}\n";
  }
  
  virtual bool runOnFunction(Function &F) {

    bool hadDeadCode = false;

    // declare each set
    map<Value*, set<Value*>> def;
    map<Value*, set<Value*>> use;
    // set<Value*> use;

    // calculate def and use sets
    for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
      for (BasicBlock::iterator inst = bb->begin(), e = bb->end(); inst != e; ++inst) {
        // iterate through operands of instruction (for use set)
        Instruction* I = &*inst;
        // errs() << *inst << "\n";

        for (User::op_iterator u = inst->op_begin(), oe = inst->op_end(); u != oe; ++u) {
          Value* v = u->get();

          if (isa<Argument>(v) || isa<Instruction>(v)) { 
            // is not a label:
            // %a => Argument
            // %0 => Instruction
            use[I].insert(v);
            // if instruction, must have definition point at that instruction:
            if (isa<Instruction>(*v)) {
              def[&*v].insert(v);
            }
          }
        }
      }
    }


    for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
      for (BasicBlock::iterator inst = bb->begin(), e = bb->end(); inst != e; ++inst) {
        // iterate through operands of instruction (for use set)
        Instruction* I = &*inst;
        errs() << *inst << "\n";

        errs() << "Def: ";
        printSet(def[I]);

        errs() << "Use: ";
        printSet(use[I]);
        
        errs() << "\n";
      }
    }


    map<Value*, set<Value*>> in;
    map<Value*, set<Value*>> out;

    map<Value*, set<Value*>> in_prev;
    map<Value*, set<Value*>> out_prev;

    set<Value*> tmp;

    do {
      // store previous "in" to use as "out" set

      for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
        for (BasicBlock::iterator i = bb->end(), top = bb->begin(); i != top; --i) {
          Instruction* I = &*i;


        }
      }

      hadDeadCode = false;
    } while (hadDeadCode /* sets are not equal*/);

    // // while (hasDeadInstructions(F)) {
    // //   hadDeadCode = true;
    // // }
    
    return hadDeadCode;
  }
};

char MyPass::ID = 0;
// static RegisterPass<MyPass> X("opCounter", "Counts opcodes per functions");
static RegisterPass<MyPass> X("mypass", "My liveness analysis and dead code elimination pass");

static RegisterStandardPasses Y(
    PassManagerBuilder::EP_EarlyAsPossible,
    [](const PassManagerBuilder &Builder,
       legacy::PassManagerBase &PM) { PM.add(new MyPass()); });

}

