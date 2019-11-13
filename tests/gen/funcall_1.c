int f (int a) {
    int b;
    b = a + a;
    return b;
}

void main () {
    int a;

    a = read_i();

    print_i(f(a));
    print_i(f(a));
    print_i(f(a));

}