struct vector {
    int x;
    int y;
};
struct point {
    int c;
};

void f () {
    int* i;
    char c[10];

    struct vector* v;
    struct point* p;

    v.x = (int) 'a';

    // char -> int
    (int) 'c';

    // array -> pointer
    (char*) "hello world";
    (char*) c;

    // pointer -> pointer
    (char*) i;

    (struct vector*) p;

}