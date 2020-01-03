// Example of how to write an LLVM pass
// For more information see: http://llvm.org/docs/WritingAnLLVMPass.html

#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/InstIterator.h"
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

    bool hadDeadCode, changed = false;

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


    // for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
    //   for (BasicBlock::iterator inst = bb->begin(), e = bb->end(); inst != e; ++inst) {
    //     // iterate through operands of instruction (for use set)
    //     Instruction* I = &*inst;
    //     errs() << *inst << "\n";

    //     errs() << "Def: ";
    //     printSet(def[I]);

    //     errs() << "Use: ";
    //     printSet(use[I]);
        
    //     errs() << "\n";
    //   }
    // }


    map<Value*, set<Value*>> in;
    map<Value*, set<Value*>> out;

    map<Value*, set<Value*>> in_prev;
    map<Value*, set<Value*>> out_prev;

    // Instruction* prev_inst;

    do {
      // assume no changes
      changed = false;
      
      // previous "in" set
      set<Value*> tmp;
      
      // iterate through instructions in reverse
      for (inst_iterator B = inst_begin(F), inst = inst_end(F); inst != B;) {
        inst--;
        Instruction* I = &*inst;
        
        // save previous sets
        in_prev[I] = in[I];
        out_prev[I] = out[I];
        
        // calculate in / out sets
        out[I] = tmp;

        set<Value*> delta, res;
        // delta = out[n] - def[n]
        set_difference(out[I].begin(), out[I].end(),
                       def[I].begin(), def[I].end(),
                       inserter(delta, delta.begin()));
        // in[n] = res = use[n] U delta
        set_union(
          // first set
          use[I].begin(), use[I].end(),
          // second set
          delta.begin(), delta.end(),
          // insert into destination
          inserter(res, res.begin())
        );

        in[I] = res;
        tmp = in[I];
      }


      // final output
      for (inst_iterator inst = inst_begin(F), E = inst_end(F); inst != E; ++inst) {
        Instruction* I = &*inst;
        if (isa<PHINode>(I)) {
          continue;
        }
        // errs() << "printing set: \n";
        errs() << "\n";
        printSet(in[I]);
        errs() << *I << "\n";
        printSet(out[I]);


        changed |= (in[I] != in_prev[I]) || (out[I] != out_prev[I]);
      }

      errs() << "\n";

      
    } while (changed /* sets are not equal*/);

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

