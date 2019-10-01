//  empty block
void f () {}

// vardecls
void f () {
    int a;
    int* b[20];
    char c;
}

// stmts
void f () {
    while (a) {}
    if (a) {}
    if (a) {} else {}
    
    return;
    return a;

    a = 3;
    a;
}

// kitchen sink
void f () {
    int a;
    int* b[20];
    char c;

    while (a) {}
    if (a) {}
    if (a) {} else {}
    
    return;
    return a;

    a = 3;
    a;
}