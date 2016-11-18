/**
 * Author: Jeroen
 * Date created: 18-11-16
 */
class User {

    private final String username;

    private final String colour;

    User(String username, String colour) {
        this.username = username;
        this.colour = colour;
    }

    String getUsername() {
        return username;
    }

    String getColour() {
        return colour;
    }
}
