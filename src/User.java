/**
 * Author: Jeroen
 * Date created: 18-11-16
 */
class User {

    private String username;

    private String colour;

    User(String username, String colour) {
        this.username = username;
        this.colour = colour;
    }

    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    String getColour() {
        return colour;
    }

    void setColour(String colour) {
        this.colour = colour;
    }
}
