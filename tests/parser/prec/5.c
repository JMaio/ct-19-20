int a;
int* b;
int* c;

void f () {
    *c = a <  b[0];
    *c = a <= b[0];
    *c = a >  b[0];
    *c = a >= b[0];
}