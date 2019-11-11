#include "../minic-stdlib.h"
int d;

void dec2bin() {
    int factor;
    int overflow;

    int pow;
    int bits;

    pow = 0;
    bits = 16;
    factor = 1;


    // set factor to highest order binary digit
    while (pow < bits - 1) {
        factor = factor * 2;
        pow = pow + 1;
    }
    if (d > factor * 2) {
        print_s((char*) "overflow!");
        return;
    }

    while (factor > 0) {
        int div;
        int rem;
        
        div = d / factor;
        rem = d % factor;
        if (div) {
            // this is one of the powers, subtract it
            d = d - factor;
        }

        // b[factor] = div;
        print_i(div);
        
        factor = factor / 2;
    }

}

void main () {

    d = read_i();

    dec2bin();
}