package data;

public enum Direction {
    N, S, E, W;

    public Direction rotateLeft() {
        switch (this) {
            case N: return W;
            case S: return E;
            case E: return N;
            case W: return S;
        }
        return this;
    }
    public Direction rotateRight() {
        switch (this) {
            case N: return E;
            case S: return W;
            case E: return S;
            case W: return N;
        }
        return this;
    }
}
