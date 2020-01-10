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
  map<BasicBlock*, set<Value*>> blockOuts;


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
    
    if (debug) {
      for (inst_iterator inst = inst_begin(F), e = inst_end(F); inst != e; inst++) {
        printSet(def[&*inst]);
      }
      errs() << "\n";

      for (inst_iterator inst = inst_begin(F), e = inst_end(F); inst != e; inst++) {
        printSet(use[&*inst]);
      }
      errs() << "\n";
    }
    
    printLiveness(F);
    return true;
  }
  
  void printLiveness(Function &F) {
    for (Function::iterator bb = F.begin(), end = F.end(); bb != end; bb++) {
      for (BasicBlock::iterator i = bb->begin(), e = bb->end(); i != e; i++) {
        // skip phis
        if (dyn_cast<PHINode>(i))
          continue;
        
        errs() << "{";
        
        // /* UNCOMMENT AND ADAPT FOR YOUR "IN" SET
        auto operatorSet = in[&*i];
        for (auto oper = operatorSet.begin(); oper != operatorSet.end(); oper++) {
          auto op = *oper;
          if (oper != operatorSet.begin())
            errs() << ", ";
          (*oper)->printAsOperand(errs(), false);
        }
        // */
        
        errs() << "}\n";
      }
    }
    errs() << "{}\n";
  }

  bool computeLiveness(Function &F) {

    bool hadDeadCode, changed = false;

    // calculate def and use sets
    for (Function::iterator bb = F.begin(), e = F.end(); bb != e; ++bb) {
      for (BasicBlock::iterator inst = bb->begin(), e = bb->end(); inst != e; ++inst) {
        Instruction* I = &*inst;
        // errs() << "--------------------------------------------------------------------\n";
        // errs() << "inst: " << *I << "\n";

        if (isa<PHINode>(I)) {
          // phi node has predecessors - result depends on which one 
          PHINode* pnode = dyn_cast<PHINode>(I);
          
          blockPhiDef[I->getParent()].insert(pnode);
          
          // technically a use but actually a value?
          for (Use &u : pnode->incoming_values()) {
            // errs() << " phi --> " << *u << "\n";
            // BasicBlock* parentBlock = dyn_cast<BasicBlock>(&*u);
            // errs() << "def instruction: " << u << "\n";
            // errs() << "def instruction: " << *u << "\n";
            

            if (isa<Argument>(u) || isa<Instruction>(u)) {
              BasicBlock* b = pnode->getIncomingBlock(u);
              
              blockPhiUse[I->getParent()][b].insert(u);
              
              if (isa<Instruction>(*u)) {
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
                // blockOuts[b].insert(u);
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
                // errs() << "--- " << v << " into " << &*v << "\n";
                def[&*v].insert(v);
                // blockDefs[I->getParent()].insert(v);
              }
            }
          }
        }
      }
    }

    do {
      // assume no changes
      changed = false;
      
      // previous "in" set
      // set<Value*> succ_in;
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
        // set<Instruction*> succ;
        // set<PHINode*> PHIsucc;

        // if (I->isTerminator()) {
        //   // this node has one or more outward edges to other blocks
        //   for (int i = 0; i < I->getNumSuccessors(); ++i) {
        //     BasicBlock* bb = I->getSuccessor(i);
        //     BasicBlock::iterator blockIter = bb->begin();

        //     // Instruction* succInst = ;
        //     // store successor instruction(s)
        //     succ.insert(&*blockIter);

        //     // if this is a phi node, find the next non-phi node instruction and store the phi nodes in between
        //     while (isa<PHINode>(&*blockIter) && blockIter != bb->end()) {
        //       blockIter++;
        //       PHIsucc.insert(dyn_cast<PHINode>(&*blockIter));
        //     }  
        //   }
        // } else {
        //   succ.insert(prevInst);
        // }
        // // } else {
        //   out[I] = tmp;
        // }

        set<Value*> out_i;
        // set<Instruction*> succ;

        if (debug) {
          errs() << "--------------------------------------------------------------------\n";
          errs() << "inst: " << *I << "\n";
          errs() << "use: ";
          printSet(use[I]);
          errs() << "def: ";
          printSet(def[I]);
          errs() << "\n";
        }

        // set<Value*> phiExclude;
        if (I->isTerminator()) {
          // remember values used in successor blocks' phi node
          // multiple successors possible
          for (BasicBlock* bb : successors(I)) {
            // store which values are defined in this block via phi node

            if (debug) {
              errs() << "----------------------------------------------\n";
              errs() << *bb << "\n";
            }
            // for each successor block, find the "in" set
            // skip phis
            BasicBlock::iterator blockIter = bb->begin();
            while (isa<PHINode>(&*blockIter) && blockIter != bb->end()) {
              // blockPhiDefs[bb].insert(&*blockIter);
              // skip phi nodes
              ++blockIter;
            }
            Value* startInstruction = &*blockIter;
            set<Value*> succIn = in[startInstruction];
            set<Value*> succOut = out[startInstruction];

            // first non-phi node
            // succ.insert(&*blockIter);

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
              errs() << "block phi out set: ";
              printSet(blockOuts[bb]);
            }
            
            // // remove block phi out
            // set_difference(
            //   out_i.begin(), out_i.end(),
            //   // succIn.begin(), succIn.end(),
            //   blockOuts[I->getParent()].begin(), blockOuts[I->getParent()].end(),
            //   inserter(out_i, out_i.begin())
            // );

            // remove phi definitions
            set<Value*> delta;
            //   blockPhiDef[bb].begin(), blockPhiDef[bb].end(),
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


            // out_i.insert(succIn.begin(), succIn.end());
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
        // save this "in" set for the next "out" set
        // pre = in[I];
  
        // remember this as the "previous" instruction
        prevInst = I;
        
        changed |= (in[I] != in_prev[I]) || (out[I] != out_prev[I]);
      }

      // // // final output
      // for (inst_iterator inst = inst_begin(F), E = inst_end(F); inst != E; ++inst) {
      // //   Instruction* I = &*inst;
      // //   if (isa<PHINode>(I)) {
      // //     continue;
      // //   }
      // //   // errs() << "printing set: \n";
      // //   // errs() << "\n";
      // //   // printSet(in[I]);
      // //   // errs() << *I << "\n";
      // //   // printSet(out[I]);


      // }

      // // // errs() << "\n";
      if (debug) {
        errs() << "loop - changed = " << changed << "\n";
        errs() << "====================================================\n";
      }
      
    } while (changed /* sets are not equal*/);
    
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

