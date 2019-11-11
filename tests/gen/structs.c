struct pair {
    int a;
    int b;
};

struct point {
    int x;
    int y;
    int mag;
    struct pair p;
    char name[4];
};

struct point p1;

void main () {
    struct point p2;

    p1.x = 4;
    p1.y = 8;

    p1.mag = 12;

    p1.p.a = 16;
    p1.p.b = 20;

    // p1.name;
    p2.x = 1;

    // ((((a.b).c).d).e).f;
}