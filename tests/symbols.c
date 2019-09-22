// { } ( ) [ ] ; , 
// int void char 
// if else while return struct sizeof
// =

comments
// nested // here
after_comment
/* this // is a /* nested comment? */
after_nested_comment

// include
#include somefile
#includea       // 1
#includea123    // 2

// something silly?
// \\

// strings
"" // pass
"hello"  // pass
"\nescape\nme\n...\n\\\"hello\" there\n" // pass
"\"perhaps\""
"this is \very wrong" // 3

// chars
''              // 4
'x' 
'\n' 
'\\' 
'\'' 
'\'\''          // 5
' \' \' \\ \' ' // 6
'\a'            // 7
'='
'=12345'        // 8
'abcdef'        // 9
'abc\''         // 10

// ints
0
001
123
-1234 
1234e10         // 11 (not actually valid mini-c)

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
123abc          // 12
123abc;         // 13

12a123a3a56a01  // 14
123abc123;      // 15

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