#includea       // 1
#includea123    // 2

"this is \very wrong" // 3
"break 
here"           // 4

''              // 5
'\'\''          // 6
' \' \' \\ \' ' // 7
'\a'            // 8
'=12345'        // 9
'abcdef'        // 10
'abc\''         // 11

1234e10         // 12 (not actually valid mini-c)

123abc          // 13
123abc;         // 14

12a123a3a56a01  // 15
123abc123;      // 16
