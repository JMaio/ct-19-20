int counter;

void main () {
    counter = 1;
    print_i(counter);

    print_c(';');
    {
        int counter;
        counter = 2;
        print_i(counter);
    }    
}