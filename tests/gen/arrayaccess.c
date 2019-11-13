char hello[12]  ;

void main () {
    int i;

    i = 0;

    while (i < 12) {
        hello[i] = "hello world\0"[i];
        
        i = i + 1;
    }

    print_s((char*) hello);
}