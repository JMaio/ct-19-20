struct pair {
    int a;
    int b;
};

struct point {
    int x;
    int y;
    int mag;
    struct pair p;
    int z;
};

struct point p1;
char* delimiter;

void main () {
    struct point p2;

    delimiter = (char*) "; ";

    p1.x = 4;
    print_i(p1.x == 4);
    print_s(delimiter);

    p1.y = 8;
    print_i(p1.y == 8);
    print_s(delimiter);

    p1.mag = 12;
    print_i(p1.mag == 12);
    print_s(delimiter);

    p1.p.a = 16;
    print_i(p1.p.a == 16);
    print_s(delimiter);
    p1.p.b = 20;
    print_i(p1.p.b == 20);
    print_s(delimiter);
    // offset p = 20
    // off(a)[point] = off(p)[point] - size[pair] + off(a)[pair]

    p1.z = 24;
    print_i(p1.z == 24);    
    print_s(delimiter);

    // p1.name;
    p2.x = 4 - 4;
    print_i(p2.x == 0);    

    // ((((a.b).c).d).e).f;
}