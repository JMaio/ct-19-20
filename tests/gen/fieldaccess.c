struct point {
    int x;
    int y;
    int mag;
    char name[4];
};

void main () {
    struct point p;
    
    p.x = 1;
    p.y = 1;
    p.mag = 1;
    p.name[0] = 'v';
}
