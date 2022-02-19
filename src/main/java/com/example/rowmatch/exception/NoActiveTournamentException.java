package com.example.rowmatch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No active tournament was found.")
public class NoActiveTournamentException extends RuntimeException {
}
