void main () {
    char a;
    char b;

    a = 'a';
    b = 'b';
    
    print_s((char*) "a==a:");
    print_i(a == a);
    print_c(';');
    
    print_s((char*) "a==b:");
    print_i(a == b);
    
}