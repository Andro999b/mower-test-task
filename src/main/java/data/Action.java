package data;

public enum Action {
    F, B, L, R;

    public boolean isMoveAction() {
        return this == F || this == B;
    }

    static public Action fromChar(char ch) {
        switch (ch) {
            case 'F': return F;
            case 'B': return B;
            case 'L': return L;
            case 'R': return R;
        }

        throw new IllegalArgumentException("Direction action: " + ch);
    }
}
