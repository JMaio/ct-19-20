#include "math.h" // for lols

struct vector {
    int x;
    int y;
};

int x;
int y;

int mag2 (struct vector v) {
    int m;
    int x2;

    // set centre
    x = 0; 
    y = 0;

    m = (v.x - x) * (v.x - x) + (v.y - y) * (v.y - y); 
    return m;
}

void main () {
    int m;
    struct vector my_v;

    my_v.x = 3;
    my_v.y = 4;

    m = mag2(my_v);
    print_i(m);
}
