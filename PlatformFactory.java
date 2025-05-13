public class PlatformFactory {
    public static Platform createPlatform() {
        return new Platform();
    }
    public static Platform createPlatform(int y) {
        return new Platform(y);
    }
    public static Platform createPlatform(int x, int y, int length) {
        return new Platform(x, y, length);
    }
} 