package Parsing;

import java.util.Optional;

public class Either<R,L> {
    private final L left;
    private final R right;

    public static <L,R> Either<L,R> left(L left) {
        return new Either(left, null);
    }
    public static <L,R> Either<L,R> right(R right) {
        return new Either(null, right);
    }
    private Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public Optional<L> getLeft() {
        return Optional.ofNullable(this.left);
    }

    public Optional<R> getRight() {
        return Optional.ofNullable(this.right);
    }

    @Override
    public String toString() {
        if (left != null) {
            return left.toString();
        } else {
            return right.toString();
        }
    }
}
