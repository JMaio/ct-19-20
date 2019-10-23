struct vector {
    int x;
    int y;
    struct vector other;
};
struct point {
    int c;
};

void main () {
    int* i;
    char c[10];

    struct vector* v;
    struct point* p;

    // v.x;

    (*v).x = (int) 'a';
    (*v).other.x = (int) 'a';

    // // char -> int
    // (int) 'c';

    // // array -> pointer
    // (char*) "hello world";
    // (char*) c;

    // // pointer -> pointer
    // (char*) i;

    // (struct vector*) *p;

}