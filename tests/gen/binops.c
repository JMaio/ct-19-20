#include "../minic-stdlib.h"

void main () {
    int a;
    int b;

    a = read_i();
    b = read_i();

    print_i(a + b);
    print_c(';');
    print_i(a - b);
    print_c(';');

    print_i(a * b);
    print_c(';');
    print_i(a / b);
    print_c(';');
    print_i(a % b);
    print_c(';');

    print_i(a > b);
    print_c(';');
    print_i(a >= b);
    print_c(';');

    print_i(a < b);
    print_c(';');
    print_i(a <= b);
    print_c(';');

    print_i(a != b);
    print_c(';');
    print_i(a == b);
    print_c(';');

    print_i(a || b);
    print_c(';');
    print_i(a && b);

}