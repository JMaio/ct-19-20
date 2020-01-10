// #include <stdio.h>

// int main() {
//   printf("%i", example());
//   return 0;
// }

// int example() {
//   int a = 0, b = 0, c = 0;
//   while (a < 9) {
//     b = a + 1;
//     c = c + b;
//     a = b * 2;
//   }
//   return c;
// }

// int foo() {
//   int a = 7;
//   int b = a * 2;
//   int c = b - a;   // dead 
//   int d = c / a;   // dead
//   return b;
// }

// int sum(int a, int b) {
//     int i;
//     int res = 1;

//     for (i = a; i < b; i++) {
//         res *= i;
//     }

//     return res;
// }

int foo(int n, int m) {
  int sum = 0;
  int c0;

  for (c0 = n; c0 > 0; c0--) {
    int c1 = m;
    for (; c1 > 0; c1--) {
      sum += c0 > c1 ? 1 : 0;
    }
  }

  return sum;
}