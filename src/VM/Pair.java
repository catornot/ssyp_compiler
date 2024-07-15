package VM;

public class Pair<T1, T2> {
    private T1 first;
    private T2 second;

    Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 get0() {
        return first;
    }

    public T2 get1() {
        return second;
    }

    public T1 set0(T1 first) {
        return this.first = first;
    }

    public T2 set1(T2 second) {
        return this.second = second;
    }
}
