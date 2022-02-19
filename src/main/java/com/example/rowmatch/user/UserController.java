package com.example.rowmatch.user;

import com.example.rowmatch.exception.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> create() {
        UserDto user = userService.create();

        return ResponseEntity.ok().body(user);
    }

    @PatchMapping(value="/{id}/level_up")
    public ResponseEntity<UserDto> levelUp(@PathVariable int id) throws UserNotFoundException {
        UserDto user = userService.levelUp(id);

        return ResponseEntity.ok().body(user);
    }
}
