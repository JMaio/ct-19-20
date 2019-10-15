void f () {
    if (-2) {}
    if (--2) {}
    if (-- 'a') {}
    if (-- "some string") {}
    
    if (*my_pointer) {}
    if (- *some_pointer[0]) {}
    if (- *some_pointer[0][0][0]) {}
    
    if (sizeof (struct my_struct)) {}
    if (- *sizeof (int)) {}
    
    if (---((char*)"hello")) {}
}