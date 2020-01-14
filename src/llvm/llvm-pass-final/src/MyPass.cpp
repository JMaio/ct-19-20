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

  bool debug = false;

  // declare each set
  map<Value*, set<Value*>> def;
  map<Value*, set<Value*>> use;

  map<Value*, set<Value*>> in;
  map<Value*, set<Value*>> out;

  map<Value*, set<Value*>> in_prev;
  map<Value*, set<Value*>> out_prev;

  // phi-node parent block, incoming block
  map<BasicBlock*, map<BasicBlock*, set<Value*>>> blockPhiUse;
  map<BasicBlock*, set<Value*>> blockPhiDef;


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

  bool runOnFunction(Function &F) override {
    // compute the sets
    computeLiveness(F);
    // print first-run liveness set (before any code elimination)
    printLiveness(F);

    while (removeDeadCode(F)) {
      computeLiveness(F);

      if (debug) {
        errs() << "computed liveness\n";

        for (inst_iterator inst = inst_begin(F), e = inst_end(F); inst != e; inst++) {
          printSet(def[&*inst]);
        }
        errs() << "\n";

        for (inst_iterator inst = inst_begin(F), e = inst_end(F); inst != e; inst++) {
          printSet(use[&*inst]);
        }
        errs() << "\n";
      }
    }

    return true;
  }

  bool isInstructionDeadAndRemovable(Instruction* I) {
    return 
      out[I].find(I) == out[I].end()
      &&
      !I->isTerminator()
      &&
      !I->mayHaveSideEffects()
    ;
  }

  bool removeDeadCode(Function &F) {
    SmallVector<Instruction*, 16> Worklist;

    for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
      for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; ++i) {
        Instruction* I = &*i;

        if (isInstructionDeadAndRemovable(I)) {
          Worklist.push_back(I);
        }
      }
    }

    bool cutInstruction = !Worklist.empty();

    while (!Worklist.empty()) {
        Instruction* i = Worklist.pop_back_val();
        if (debug) {
          errs() << "removed instruction " << i->getOpcodeName() << "\n";
        }
        i->eraseFromParent();
    }
    
    return cutInstruction;
  }
  
  void printLiveness(Function &F) {
    for (Function::iterator bb = F.begin(), end = F.end(); bb != end; bb++) {
      for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; i++) {
        // skip phis
        if (dyn_cast<PHINode>(i))
          continue;
        
        errs() << "{";
        
        auto operatorSet = in[&*i];
        for (auto oper = operatorSet.begin(); oper != operatorSet.end(); oper++) {
          auto op = *oper;
          if (oper != operatorSet.begin())
            errs() << ",";
          (*oper)->printAsOperand(errs(), false);
        }
        
        errs() << "}\n";
      }
    }
    errs() << "{}\n";
  }

  bool computeLiveness(Function &F) {
    // -- reset sets --
            def.clear();
            use.clear();
             in.clear();
            out.clear();
        in_prev.clear();
       out_prev.clear();
    blockPhiUse.clear();
    blockPhiDef.clear();

    bool hadDeadCode, changed = false;

    // calculate def and use sets
    for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
      for (BasicBlock::iterator inst = bb->begin(), e = bb->end(); inst != e; ++inst) {
        Instruction* I = &*inst;

        if (isa<PHINode>(I)) {
          // phi node has predecessors - result depends on which one 
          PHINode* pnode = dyn_cast<PHINode>(I);
          
          blockPhiDef[I->getParent()].insert(pnode);
          
          // technically a use but actually a value?
          for (Use &u : pnode->incoming_values()) {

            if (isa<Argument>(u) || isa<Instruction>(u)) {
              BasicBlock* b = pnode->getIncomingBlock(u);
              
              blockPhiUse[I->getParent()][b].insert(u);
              
              if (isa<Instruction>(u)) {
                def[u].insert(u);
                // store the value in the origin (predecessor) block
              }
              if (debug) {
                errs() << "current block: " << b << "\n";
                errs() << " parent block: " << dyn_cast<Instruction>(u)->getParent() << "\n";
                errs() << "phi par block: " << pnode->getParent() << "\n";
              }

              // only considered use if not defined in the same block
              if (b != pnode->getParent()) {
                use[I].insert(u);
              }
            }
          }
        } else {
          // not a phi node - standard use/def
          for (User::op_iterator u = inst->op_begin(), oe = inst->op_end(); u != oe; ++u) {
            Value* v = u->get();

            // labels don't count:
            if (isa<Argument>(v) || isa<Instruction>(v)) { 
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
    }
    // calculate in and out sets
    do {
      // assume no changes
      changed = false;
      
      // store previous instruction
      Instruction* prevInst;

      // iterate through instructions in reverse
      for (inst_iterator B = inst_begin(F), inst = inst_end(F); inst != B;) {
        inst--;
        Instruction* I = &*inst;
        
        // save previous sets
        in_prev[I] = in[I];
        out_prev[I] = out[I];

        // if this is a terminator instruction
        // there may be multiple successors
        // finding successors from map of this phi node to point to the instruction
        // and use that instruction to get the in set for that line
        
        // if terminator (end of block) -> look for next branch location
        // skip phis until first "live" line of code
        // use that line of code to access the in set since it is the successor
        // if not terminator -> use previous in set

        // out set ------------------------
        set<Value*> out_i;

        if (debug) {
          errs() << "--------------------------------------------------------------------\n";
          errs() << "inst: " << *I << "\n";
          errs() << "use: ";
          printSet(use[I]);
          errs() << "def: ";
          printSet(def[I]);
          errs() << "\n";
        }

        if (I->isTerminator()) {
          // remember values used in successor blocks' phi node
          // multiple successors possible
          for (BasicBlock* bb : successors(I)) {
            // store which values are defined in this block via phi node

            if (debug) {
              errs() << "----------------------------------------------\n";
              errs() << *bb << "\n";
            }

            BasicBlock::iterator blockIter = bb->begin();
            while (isa<PHINode>(&*blockIter) && blockIter != bb->end()) {
              // blockPhiDefs[bb].insert(&*blockIter);
              // skip phi nodes
              ++blockIter;
            }

            Value* startInstruction = &*blockIter;
            set<Value*> succIn = in[startInstruction];
            set<Value*> succOut = out[startInstruction];

            if (debug) {
              // errs() << "block starting instruction:\n";
              errs() << "block starting instruction:\n";
              errs() << *startInstruction << "\n";
              errs() << "start instruction in set: ";
              printSet(succIn);
              errs() << "                 out set: ";
              printSet(succOut);
              errs() << "block phi use: ";
              printSet(blockPhiUse[bb][I->getParent()]);
              errs() << "block phi def: ";
              printSet(blockPhiDef[bb]);
            }

            // remove phi definitions
            set<Value*> delta;
            set_difference(
              succIn.begin(), succIn.end(),
              blockPhiDef[bb].begin(), blockPhiDef[bb].end(),
              inserter(delta, delta.begin())
            );
            
            // add to this set the vars needed for successor phis
            set_union(
              delta.begin(), delta.end(),
              blockPhiUse[bb][I->getParent()].begin(), blockPhiUse[bb][I->getParent()].end(),
              // succIn.begin(), succIn.end(),
              inserter(out_i, out_i.begin())
            );
          }

          out[I] = out_i;
        } else {
          // only one successor: the previous instruction
          out[I] = in[prevInst];
        }

        // in set -------------------------
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

        if (debug) {
          errs() << "set difference:\n   -> ";
          printSet(delta);
          errs() << "in set:\n   -> ";
          printSet(res);
          errs() << "out set:\n   -> ";
          printSet(out[I]);
        }

        in[I] = res;
  
        // remember this as the "previous" instruction
        prevInst = I;
        
        changed |= (in[I] != in_prev[I]) || (out[I] != out_prev[I]);
      }

      if (debug) {
        errs() << "loop - changed = " << changed << "\n";
        errs() << "====================================================\n";
      }
      
    } while (changed /* sets are not equal*/);
    
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
