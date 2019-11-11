struct vector {
    int a;
    char c;
    char d;
    struct vector* v;
};

struct alphabet {
    char a;
    char b;
    char c;
    char d;
    char e;
    char f;
};

struct listOchars {
    char l[22];
};

void main () {
    // 1
    print_i(sizeof(char));
    print_c(';');  
    
    // 4
    print_i(sizeof(int));
    print_c(';');  
    
    // 16
    print_i(sizeof(struct vector)); 
    print_c(';');

    // 4 * 6 = 24
    print_i(sizeof(struct alphabet)); 
    print_c(';');

    // 22 (rounded up to nearest) = 24
    print_i(sizeof(struct listOchars)); 

}