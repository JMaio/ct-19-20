struct a {
    int x;
};

struct b {
    struct a a_field;
};

struct c {
    struct b b_field;
};

void main () {
    struct a my_a;
    struct b my_b;
    struct c my_c;

    // my_a.x = 0;

    // my_b.a_field = my_a;

    // my_c.b_field = my_b;

    // print_i(my_c.b_field.a_field.x);
    my_c.b_field.a_field.x;
    return;
}