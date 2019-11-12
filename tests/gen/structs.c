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

void main () {
    struct point p2;

    p1.x = 4;
    p1.y = 8;

    p1.mag = 12;

    p1.p.a = 16;
    p1.p.b = 20;
    // offset p = 20
    // off(a)[point] = off(p)[point] - size[pair] + off(a)[pair]

    p1.z = 24;
    

    // p1.name;
    p2.x = 4;

    // ((((a.b).c).d).e).f;
}