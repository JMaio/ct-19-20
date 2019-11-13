struct triple {
    int x;
    int y;
    int z;
};

struct triple t;

struct triple f_global () {
    t.x = 1;
    t.y = 2;
    t.z = 3;

    return t;
}

struct triple f_local (struct triple t) {
    
    t.x = 4;
    t.y = 5;
    t.z = 6;
    
    return t;
}

struct triple f_local_decl () {
    struct triple t;

    t.x = 7;
    t.y = 8;
    t.z = 9;

    return t;
}

void main () {
    struct triple t1;
    struct triple t2;
    struct triple t3;

    t1 = f_global();
    t2 = f_local(t2);
    t3 = f_local_decl();

    // print_i(t1.x);
    // print_i(t1.y);
    // print_i(t1.z);

    // print_i(t2.x);
    // print_i(t2.y);
    // print_i(t2.z);

    print_i(t3.x);
    print_i(t3.y);
    print_i(t3.z);

}