int a;
int b;
int* c;

void f () {
    c = a + b;

    a = -b - -*c;

    b = *c * "wow"[-b] + a % 2 * (int) 'a';
}