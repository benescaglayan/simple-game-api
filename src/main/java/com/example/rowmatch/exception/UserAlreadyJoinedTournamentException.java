package com.example.rowmatch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.ALREADY_REPORTED, reason = "Already participated.")
public class UserAlreadyJoinedTournamentException extends RuntimeException {
}
