struct val {
    int x;
};
struct node {
    int val;
    struct node next;
    struct val value;
};

void f () {
    struct node n1;
    n1.value.next = 2;
}