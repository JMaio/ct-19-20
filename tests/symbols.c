{ } ( ) [ ] ; , 
int void char 
if else while return struct sizeof
=
// include
#include somefile
#includea 
#includea123

// something silly?
// \\

// strings
"" // pass
"hello"  // pass
"\nescape\nme\n...\n\\\\\"hello\" there\n" // pass
"\"perhaps\""
"this is \very wrong"

// chars
'' 
'x' 
'\n' 
'\\' 
'\'' 
'\'\'' 
' \' \' \\ \' ' 
'\a'
// 'abcdef'
// 'abc\''

// ints
0
001
123
-1234 
1234e10 // not actually valid mini-c

// idents
hello 
p123
my_ident
_ident
ident_
_ident_ = 1
_ = 2
__ = 3

// operators
&& ||
== != < > <= >=
+ - * / %
.

// invalid
123abc
123abc;

123abc123a3a56a01
123abc123;

// line comment 
[]
//
/* multiline 
comment */
/**/
{}
*
// comment at the end
/**/