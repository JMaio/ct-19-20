#include "../minic-stdlib.h"

int f (int a, int b, int c, int d, int e) {
    int r;

    r = a + b + c + d + e;
    return r;
}

int f2 (int a) {
    // print_i(a);
    return a;
}

void main () {
    int a;
    int b;
    int c;
    int d;
    int e;

    int r;

    a = 1;

    // f2(a);
    // print_s((char*) " hi");
    // print_i(f2(a));


    print_i(f(1, 2, 3, 4, 5));
    print_s((char*) "; ");

    a = read_i();
    b = read_i();
    c = read_i();
    d = read_i();
    e = read_i();

    print_i(f(a, b, c, d, e));
    print_s((char*) "; ");

    r = f(a, b, c, d, e);
    print_i(r);


}