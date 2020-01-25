  ; {use} : {def}
  ; {out} : {in}
define i32 @sum(i32 %a, i32 %b) nounwind readnone ssp {
entry:
  ; {%a, %b} : {%0}
  ; 
  %0 = icmp slt i32 %a, %b

  ; {%0} : {}
  ;
  br i1 %0, label %bb.nph, label %bb2

bb.nph: ; preds = %entry
  ; {%b, %a} : {%tmp}
  ; 
  %tmp = sub i32 %b, %a
  

  ; {} : {}
  ; 
  br label %bb

bb: ; preds = %bb, %bb.nph
  ; {%indvar.next} : {%indvar}
  ; 
  %indvar = phi i32 [ 0, %bb.nph ], [ %indvar.next, %bb ]
  ; {%1} : {%res.05}
  ; 
  %res.05 = phi i32 [ 1, %bb.nph ], [ %1, %bb ]
  
  ; {%indvar, %a} : {%i.04}
  ; 
  %i.04 = add i32 %indvar, %a

  ; {%res.05, %i.04} : {%1}
  ; 
  %1 = mul nsw i32 %res.05, %i.04

  ; {%indvar} : {%indvar.next}
  ; {}
  %indvar.next = add i32 %indvar, 1
  
  ; {%indvar.next, %tmp} : {%exitcond}
  ; {%exitcond, %1} : {%indvar.next, %tmp, %1}
  %exitcond = icmp eq i32 %indvar.next, %tmp
  
  ; {%exitcond} : {}
  ; {%1} : {%exitcond, %1}
  br i1 %exitcond, label %bb2, label %bb

bb2: ; preds = %bb, %entry
  ; {%1} : {%res.0.lcssa}
  ; {%res.0.lcssa} : {%1}
  %res.0.lcssa = phi i32 [ 1, %entry ], [ %1, %bb ]
  
  ; {%res.0.lcssa} : {}
  ; {} : {%res.0.lcssa}
  ret i32 %res.0.lcssa


}