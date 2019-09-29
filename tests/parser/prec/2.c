void f () {
    if (-2) {}
    if (--2) {}
    
    if (*my_pointer) {}
    if (- *some_pointer[0]) {}
    
    if (sizeof (struct my_struct)) {}
    if (- *sizeof (int)) {}
    
    if (---((char*)"hello")) {}
}