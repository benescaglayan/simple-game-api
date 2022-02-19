package com.example.rowmatch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Too low-ranked to participate.")
public class RankTooLowForTournamentException extends RuntimeException {
}
