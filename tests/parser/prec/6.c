int a;
int* b;
int* c;

void f () {
    *c = a == b + b;
    *c = a != b < a;
}