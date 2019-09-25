{ } ( ) [ ] ; , 
int void char 
if else while return struct sizeof
=

comments
// nested // here
after_comment
/* this // is a /* nested comment? */
after_nested_comment

// include
#include somefile

// something silly?
// \\

// strings
""
"hello"
"\nescape\nme\n...\n\\\"hello\" there\n" // pass
"\"perhaps\""
"newline\n"
"hello newline"

// chars
'x' 
'\n' 
'\\' 
'\'' 
'='


// ints
0
001
123
-1234 

// should only be parse, not lexer errors
1234e10

a0

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