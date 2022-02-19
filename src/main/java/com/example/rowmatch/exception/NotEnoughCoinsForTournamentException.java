package com.example.rowmatch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Not enough coins to participate.")
public class NotEnoughCoinsForTournamentException extends RuntimeException {
}
