void f () {
    if (g()) {}
    if (g()[0]) {}

    if (arr[0]) {}
    if (arr[0][0]) {}
    if ((arr[0][0])) {}

    if (obj.exists) {}
    if (obj.exists.truthval.isgood[0][1]) {}
    if (g(obj.exists[42]).truthval.isgood[0][1]) {}
}
